package tc.oc.pgm.wool;

import static net.kyori.adventure.text.Component.text;
import static tc.oc.pgm.util.material.ColorUtils.COLOR_UTILS;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import tc.oc.pgm.api.feature.FeatureInfo;
import tc.oc.pgm.api.region.Region;
import tc.oc.pgm.goals.ProximityGoalDefinition;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.goals.ShowOptions;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.util.bukkit.BukkitUtils;
import tc.oc.pgm.util.material.MaterialData;
import tc.oc.pgm.util.material.MaterialMatcher;
import tc.oc.pgm.util.text.TextFormatter;

@FeatureInfo(name = "wool")
public class MonumentWoolFactory extends ProximityGoalDefinition {
  protected static final MaterialMatcher WOOL = MaterialMatcher.of(m -> m.name().endsWith("WOOL"));

  protected final DyeColor color;
  protected final Vector location;
  protected final Region placement;
  protected final boolean craftable;
  protected final Component componentName;

  public static String makeColorName(DyeColor color) {
    String[] name = StringUtils.split(color.toString(), '_');
    for (int i = 0; i < name.length; i++) {
      name[i] = (i > 0 ? " " : "") + StringUtils.capitalize(name[i].toLowerCase());
    }
    return StringUtils.join(name);
  }

  public static String makeName(DyeColor color) {
    return makeColorName(color) + " Wool";
  }

  public static Component makeComponentName(DyeColor color) {
    return text(makeName(color), TextFormatter.convert((BukkitUtils.dyeColorToChatColor(color))));
  }

  public MonumentWoolFactory(
      @Nullable String id,
      @Nullable Boolean required,
      ShowOptions showOptions,
      TeamFactory owner,
      @Nullable ProximityMetric woolProximityMetric,
      @Nullable ProximityMetric monumentProximityMetric,
      DyeColor color,
      Vector location,
      Region placement,
      boolean craftable) {

    super(
        id,
        makeName(color),
        required,
        showOptions,
        owner,
        woolProximityMetric,
        monumentProximityMetric);
    this.color = color;
    this.location = location;
    this.placement = placement;
    this.craftable = craftable;
    this.componentName = makeComponentName(color);
  }

  @Override
  public String toString() {
    return "MonumentWoolFactory{owner="
        + this.getOwner().getDefaultName()
        + ", color="
        + this.color
        + ", location="
        + this.location
        + ", placement="
        + this.placement
        + ", craftable="
        + this.craftable
        + ", visible="
        + this.getShowOptions()
        + "}";
  }

  @Override
  public String getColoredName() {
    return BukkitUtils.dyeColorToChatColor(this.color) + this.getName();
  }

  public String getColorName() {
    return makeColorName(this.color);
  }

  public DyeColor getColor() {
    return this.color;
  }

  @Override
  public Component getComponentName() {
    return componentName;
  }

  public Vector getLocation() {
    return this.location;
  }

  public Region getPlacementRegion() {
    return this.placement;
  }

  public boolean isCraftable() {
    return this.craftable;
  }

  public boolean isObjectiveWool(ItemStack stack) {
    return stack != null
        && WOOL.matches(stack.getType())
        && COLOR_UTILS.isColor(MaterialData.item(stack), color);
  }

  public boolean isObjectiveWool(BlockState block) {
    return block != null
        && WOOL.matches(block.getType())
        && COLOR_UTILS.isColor(MaterialData.block(block), color);
  }

  public boolean isHolding(InventoryHolder holder) {
    return this.isHolding(holder.getInventory());
  }

  public boolean isHolding(Inventory inv) {
    for (ItemStack stack : inv.getContents()) {
      if (this.isObjectiveWool(stack)) return true;
    }
    return false;
  }
}
