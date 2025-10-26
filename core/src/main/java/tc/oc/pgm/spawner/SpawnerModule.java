package tc.oc.pgm.spawner;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.api.filter.Filter;
import tc.oc.pgm.api.map.MapModule;
import tc.oc.pgm.api.map.factory.MapFactory;
import tc.oc.pgm.api.map.factory.MapModuleFactory;
import tc.oc.pgm.api.match.Match;
import tc.oc.pgm.api.region.Region;
import tc.oc.pgm.filters.FilterModule;
import tc.oc.pgm.regions.RegionModule;
import tc.oc.pgm.spawner.objects.SpawnableItem;
import tc.oc.pgm.spawner.objects.SpawnablePotion;
import tc.oc.pgm.util.material.MaterialData;
import tc.oc.pgm.util.xml.InheritingElement;
import tc.oc.pgm.util.xml.InvalidXMLException;
import tc.oc.pgm.util.xml.XMLUtils;

public class SpawnerModule implements MapModule<SpawnerMatchModule> {

  private static final int SPLASH_BIT = 0x4000;

  private final List<SpawnerDefinition> spawnerDefinitions = new ArrayList<>();

  @Override
  public SpawnerMatchModule createMatchModule(Match match) {
    return new SpawnerMatchModule(match, spawnerDefinitions);
  }

  public static class Factory implements MapModuleFactory<SpawnerModule> {
    @Override
    public Collection<Class<? extends MapModule<?>>> getWeakDependencies() {
      return ImmutableList.of(RegionModule.class, FilterModule.class);
    }

    @Override
    public SpawnerModule parse(MapFactory factory, Logger logger, Document doc)
        throws InvalidXMLException {
      var parser = factory.getParser();
      SpawnerModule spawnerModule = new SpawnerModule();
      AtomicInteger spawnerIdSerial = new AtomicInteger(1);

      for (Element spawnerEl :
          XMLUtils.flattenElements(doc.getRootElement(), "spawners", "spawner")) {
        String id = spawnerEl.getAttributeValue("id");
        Region spawnRegion =
            parser.region(spawnerEl, "spawn-region").randomPoints().required();
        Region playerRegion = parser.region(spawnerEl, "player-region").required();

        var parsedMinDelay = parser.duration(spawnerEl, "min-delay").optional();
        var parsedMaxDelay = parser.duration(spawnerEl, "max-delay").optional();
        var delay = parser
            .duration(spawnerEl, "delay")
            .validate((dur, node) -> {
              if (parsedMinDelay.isPresent() || parsedMaxDelay.isPresent()) {
                throw new InvalidXMLException(
                    "Attribute 'min-delay' and 'max-delay' cannot be combined with 'delay'",
                    spawnerEl);
              }
            })
            .optional(Duration.ofSeconds(10));

        var effectiveMinDelay = parsedMinDelay.orElse(delay);
        var effectiveMaxDelay = parsedMaxDelay.orElse(delay);

        var comparisonResult = effectiveMaxDelay.compareTo(effectiveMinDelay);
        if (comparisonResult < 0) {
          throw new InvalidXMLException("Max-delay must be longer than min-delay", spawnerEl);
        }

        if (id == null) id = SpawnerDefinition.makeDefaultId(null, spawnerIdSerial);

        int maxEntities =
            parser.parseInt(spawnerEl, "max-entities").attr().optional(Integer.MAX_VALUE);
        Filter playerFilter = parser.filter(spawnerEl, "filter").orAllow();

        List<Spawnable> objects = new ArrayList<>();
        for (Element itemEl : XMLUtils.getChildren(spawnerEl, "item")) {
          ItemStack stack = parser.item(itemEl).required();
          SpawnableItem item = new SpawnableItem(stack, id);
          objects.add(item);
        }

        for (Element potionEl : XMLUtils.getChildren(spawnerEl, "potion")) {
          short dmg = parser.parseShort(potionEl, "damage").attr().optional((short) 0);
          ItemStack potion =
              MaterialData.item(Material.POTION, (short) (dmg | SPLASH_BIT)).toItemStack(1);
          PotionMeta meta = (PotionMeta) potion.getItemMeta();
          for (Element potionChild : potionEl.getChildren("effect")) {
            meta.addCustomEffect(
                XMLUtils.parsePotionEffect(new InheritingElement(potionChild)), false);
          }
          if (!meta.hasCustomEffects()) {
            throw new InvalidXMLException("Expected child effects, but found none", spawnerEl);
          }
          potion.setItemMeta(meta);
          objects.add(new SpawnablePotion(potion, id));
        }

        SpawnerDefinition spawnerDefinition = new SpawnerDefinition(
            id,
            objects,
            spawnRegion,
            playerRegion,
            playerFilter,
            effectiveMinDelay,
            comparisonResult == 0 ? effectiveMinDelay : effectiveMaxDelay,
            maxEntities);
        factory.getFeatures().addFeature(spawnerEl, spawnerDefinition);
        spawnerModule.spawnerDefinitions.add(spawnerDefinition);
      }

      return spawnerModule.spawnerDefinitions.isEmpty() ? null : spawnerModule;
    }
  }
}
