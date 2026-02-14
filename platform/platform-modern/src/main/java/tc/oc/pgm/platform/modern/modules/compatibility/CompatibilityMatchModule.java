package tc.oc.pgm.platform.modern.modules.compatibility;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.object;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.object.ObjectContents.sprite;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import tc.oc.pgm.api.match.Match;
import tc.oc.pgm.api.match.MatchModule;
import tc.oc.pgm.api.match.MatchScope;
import tc.oc.pgm.api.module.exception.ModuleLoadException;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerJoinMatchEvent;

@NullMarked
@ListenerScope(MatchScope.LOADED)
public class CompatibilityMatchModule implements MatchModule, Listener {
  private final Match match;
  private final CompatibilityOptions options;
  // Used to compare against options to display the differences
  private final boolean isLegacy;
  private final CompatibilityOptions mapDefaults;

  public CompatibilityMatchModule(Match match, CompatibilityOptions options) {
    this.match = match;
    this.options = options;
    isLegacy = match.getMap().getServerVersions().contains(CompatibilityModule.VERSION_1_8_8);
    mapDefaults = CompatibilityOptions.defaultOptions(isLegacy);
  }

  public CompatibilityOptions getOptions() {
    return options;
  }

  @Override
  public void load() throws ModuleLoadException {
    var nmsWorld = ((CraftWorld) match.getWorld()).getHandle();
    var paperConfig = nmsWorld.paperConfig();
    paperConfig.misc.disableRelativeProjectileVelocity = !options.relativeProjectileVelocity();
    paperConfig.fixes.preventTntFromMovingInWater = !options.tntMovesInWater();
    paperConfig.misc.invertEntityCollisionOrder = options.invertEntityCollisionOrder();
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void on(PlayerJoinMatchEvent ev) {
    // TODO: refactor this to be more maintainable
    List<Component> extraLines = new ArrayList<>(5);

    if (isLegacy) {
      extraLines.add(text()
          .color(NamedTextColor.GRAY)
          .append(object(sprite(Key.key("items"), Key.key("item/diamond_sword"))))
          .append(space())
          .append(text("Legacy map defaults"))
          .append(space())
          .append(text("enabled", NamedTextColor.GREEN))
          .build());
    }
    if (mapDefaults.relativeProjectileVelocity() != options.relativeProjectileVelocity()) {
      extraLines.add(text()
          .color(NamedTextColor.GRAY)
          .append(object(sprite(Key.key("items"), Key.key("item/arrow"))))
          .append(space())
          .append(text("Legacy projectile mechanics"))
          .append(space())
          .append(text(
              !options.relativeProjectileVelocity() ? "enabled" : "disabled",
              !options.relativeProjectileVelocity() ? NamedTextColor.GREEN : NamedTextColor.RED))
          .build());
    }
    if (mapDefaults.tntMovesInWater() != options.tntMovesInWater()) {
      extraLines.add(text()
          .color(NamedTextColor.GRAY)
          .append(object(sprite(Key.key("blocks"), Key.key("block/tnt_side"))))
          .append(space())
          .append(text("TNT"))
          .append(space())
          .append(text(
              options.tntMovesInWater() ? "moves" : "doesn't move",
              options.tntMovesInWater() ? NamedTextColor.GREEN : NamedTextColor.RED))
          .append(space())
          .append(text("in water"))
          .build());
    }
    if (mapDefaults.randomTntOffset() != options.randomTntOffset()) {
      extraLines.add(text()
          .color(NamedTextColor.GRAY)
          .append(object(sprite(Key.key("blocks"), Key.key("block/tnt_side"))))
          .append(space())
          .append(text("Randomized TNT offset on ignite"))
          .append(space())
          .append(text(
              options.randomTntOffset() ? "enabled" : "disabled",
              options.randomTntOffset() ? NamedTextColor.GREEN : NamedTextColor.RED))
          .build());
    }
    if (mapDefaults.invertEntityCollisionOrder() != options.invertEntityCollisionOrder()) {
      extraLines.add(text()
          .color(NamedTextColor.GRAY)
          .append(object(sprite(Key.key("blocks"), Key.key("block/tnt_side"))))
          .append(space())
          .append(text("Legacy collision mechanics"))
          .append(space())
          .append(text(
              options.invertEntityCollisionOrder() ? "enabled" : "disabled",
              options.invertEntityCollisionOrder() ? NamedTextColor.GREEN : NamedTextColor.RED))
          .build());
    }
    if (!extraLines.isEmpty()) {
      ev.getExtraLines().add(empty());
      ev.getExtraLines().addAll(extraLines);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void on(EntitySpawnEvent ev) {
    if (ev.getEntityType() == EntityType.TNT && !options.randomTntOffset()) {
      ev.getEntity().setVelocity(new Vector(0, ev.getEntity().getVelocity().getY(), 0));
    }
  }
}
