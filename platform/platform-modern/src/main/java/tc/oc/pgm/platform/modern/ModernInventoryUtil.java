package tc.oc.pgm.platform.modern;

import static tc.oc.pgm.util.platform.Supports.Variant.PAPER;

import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import tc.oc.pgm.util.inventory.InventoryUtils;
import tc.oc.pgm.util.platform.Supports;

@Supports(value = PAPER, minVersion = "1.20.6")
public class ModernInventoryUtil implements InventoryUtils.InventoryUtilsPlatform {

  @Override
  public Collection<PotionEffect> getPotionEffects(ItemStack item) {
    if (item.getItemMeta() instanceof PotionMeta potion) {
      var base = potion.getBasePotionType();
      if (base != null) return base.getPotionEffects();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isUnbreakable(ItemMeta meta) {
    return meta.isUnbreakable();
  }

  @Override
  public void setUnbreakable(ItemMeta meta, boolean unbreakable) {
    meta.setUnbreakable(unbreakable);
  }

  @Override
  public boolean openVillager(Villager villager, Player viewer) {
    viewer.openMerchant((Villager) villager.copy(), true);
    return true;
  }

  @Override
  public ItemStack craftItemCopy(ItemStack item) {
    return CraftItemStack.asCraftCopy(item);
  }

  @Override
  public void copyAttributeModifiers(ItemMeta destination, ItemMeta source) {
    var modifiers = source.getAttributeModifiers();
    if (modifiers != null) modifiers.forEach(destination::addAttributeModifier);
  }

  @Override
  public void applyAttributeModifiers(
      SetMultimap<Attribute, AttributeModifier> modifiers, ItemMeta meta) {
    for (var entry : modifiers.entries()) {
      meta.addAttributeModifier(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public EquipmentSlot getUsedHand(PlayerEvent event) {
    return switch (event) {
      case PlayerItemConsumeEvent e -> e.getHand();
      case PlayerInteractEvent e -> e.getHand();
      default -> EquipmentSlot.HAND;
    };
  }

  @Override
  public void setCanDestroy(ItemMeta itemMeta, Collection<Material> materials) {
    // TODO: PLATFORM 1.20 no support for can place/destroy
  }

  @Override
  public Set<Material> getCanDestroy(ItemMeta itemMeta) {
    // TODO: PLATFORM 1.20 no support for can place/destroy
    return Collections.emptySet();
  }

  @Override
  public void setCanPlaceOn(ItemMeta itemMeta, Collection<Material> materials) {
    // TODO: PLATFORM 1.20 no support for can place/destroy
  }

  @Override
  public Set<Material> getCanPlaceOn(ItemMeta itemMeta) {
    // TODO: PLATFORM 1.20 no support for can place/destroy
    return Collections.emptySet();
  }
}
