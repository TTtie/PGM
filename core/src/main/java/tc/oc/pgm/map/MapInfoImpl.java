package tc.oc.pgm.map;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static tc.oc.pgm.api.map.MapSource.DEFAULT_VARIANT;
import static tc.oc.pgm.util.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import java.lang.ref.SoftReference;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Difficulty;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tc.oc.pgm.api.map.Contributor;
import tc.oc.pgm.api.map.Gamemode;
import tc.oc.pgm.api.map.MapContext;
import tc.oc.pgm.api.map.MapInfo;
import tc.oc.pgm.api.map.MapModule;
import tc.oc.pgm.api.map.MapSource;
import tc.oc.pgm.api.map.MapTag;
import tc.oc.pgm.api.map.Phase;
import tc.oc.pgm.api.map.WorldInfo;
import tc.oc.pgm.ffa.FreeForAllModule;
import tc.oc.pgm.map.contrib.PlayerContributor;
import tc.oc.pgm.map.contrib.PseudonymContributor;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.teams.TeamModule;
import tc.oc.pgm.util.StreamUtils;
import tc.oc.pgm.util.StringUtils;
import tc.oc.pgm.util.Version;
import tc.oc.pgm.util.named.MapNameStyle;
import tc.oc.pgm.util.named.NameStyle;
import tc.oc.pgm.util.text.TextFormatter;
import tc.oc.pgm.util.xml.InvalidXMLException;
import tc.oc.pgm.util.xml.Node;
import tc.oc.pgm.util.xml.XMLUtils;

public class MapInfoImpl implements MapInfo {
  // See #parseWorld, this class actually is responsible for terrain tag.
  private static final MapTag TERRAIN = new MapTag("terrain", "Terrain");

  private final MapSource source;

  private final String id;
  private final String variantId;
  private final Map<String, VariantInfo> variants;

  private final String worldFolder;
  private final Range<Version> serverVersion;
  private final Version proto;
  private final Version version;
  private final Phase phase;
  private final String name;
  private final String normalizedName;
  private final String description;
  private final LocalDate created;
  private final Collection<Contributor> authors;
  private final Collection<Contributor> contributors;
  private final Collection<String> rules;
  private final Component gamemode;
  private final int difficulty;
  private final WorldInfo world;
  private final boolean friendlyFire;

  // May be set after loading the whole context
  protected Collection<Gamemode> gamemodes;

  // Must be set after loading the whole context
  protected Collection<MapTag> tags = ImmutableSortedSet.of();
  protected Collection<Integer> players = ImmutableList.of();
  protected SoftReference<MapContext> context = null;

  public MapInfoImpl(MapSource source, Element root) throws InvalidXMLException {
    this.source = source;
    this.variantId = source.getVariantId();
    this.variants = createVariantMap(root);

    VariantInfo variant = variants.get(variantId);
    if (variant == null) throw new InvalidXMLException("Could not find variant definition", root);

    this.worldFolder = variant.getWorld();
    this.serverVersion = variant.getServerVersions();

    this.name = variant.getMapName();
    this.normalizedName = StringUtils.normalize(name);

    this.id = assertNotNull(variant.getMapId());

    this.proto = assertNotNull(XMLUtils.parseSemanticVersion(Node.fromRequiredAttr(root, "proto")));
    this.version =
        assertNotNull(XMLUtils.parseSemanticVersion(Node.fromRequiredChildOrAttr(root, "version")));
    this.description = assertNotNull(
        Node.fromRequiredChildOrAttr(root, "objective", "description").getValueNormalize());
    this.created = XMLUtils.parseDate(Node.fromChildOrAttr(root, "created"));
    this.authors = parseContributors(root, "author");
    this.contributors = parseContributors(root, "contributor");
    this.rules = parseRules(root);
    this.difficulty = XMLUtils.parseEnum(
            Node.fromLastChildOrAttr(root, "difficulty"), Difficulty.class, Difficulty.NORMAL)
        .ordinal();
    this.world = parseWorld(root);
    this.gamemode = XMLUtils.parseFormattedText(Node.fromLastChildOrAttr(root, "game"));
    this.gamemodes = parseGamemodes(root);
    this.phase =
        XMLUtils.parseEnum(Node.fromLastChildOrAttr(root, "phase"), Phase.class, Phase.PRODUCTION);
    this.friendlyFire = XMLUtils.parseBoolean(
        Node.fromLastChildOrAttr(root, "friendlyfire", "friendly-fire"), false);
  }

  @NotNull
  private static Map<String, VariantInfo> createVariantMap(Element root)
      throws InvalidXMLException {
    ImmutableMap.Builder<String, VariantInfo> variants = ImmutableMap.builder();
    variants.put(DEFAULT_VARIANT, new VariantData(root, null));
    for (Element el : root.getChildren("variant")) {
      VariantData vd = new VariantData(root, el);
      variants.put(vd.variantId, vd);
    }
    return variants.build();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getVariantId() {
    return variantId;
  }

  @Override
  public Map<String, VariantInfo> getVariants() {
    return variants;
  }

  @Override
  public Range<Version> getServerVersion() {
    return serverVersion;
  }

  @Override
  public String getWorldFolder() {
    return worldFolder;
  }

  @Override
  public Version getProto() {
    return proto;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getNormalizedName() {
    return normalizedName;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public LocalDate getCreated() {
    return created;
  }

  @Override
  public Collection<Contributor> getAuthors() {
    return authors;
  }

  @Override
  public Collection<Contributor> getContributors() {
    return contributors;
  }

  @Override
  public Collection<String> getRules() {
    return rules;
  }

  @Override
  public int getDifficulty() {
    return difficulty;
  }

  @Override
  public Collection<MapTag> getTags() {
    return Collections.unmodifiableCollection(tags);
  }

  @Override
  public Collection<Integer> getMaxPlayers() {
    return players;
  }

  @Override
  public Component getGamemode() {
    return gamemode;
  }

  @Override
  public Collection<Gamemode> getGamemodes() {
    return gamemodes;
  }

  @Override
  public WorldInfo getWorld() {
    return world;
  }

  @Override
  public Phase getPhase() {
    return phase;
  }

  @Override
  public boolean getFriendlyFire() {
    return friendlyFire;
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MapInfo)) return false;
    return getId().equals(((MapInfo) obj).getId());
  }

  @Override
  public String toString() {
    return "MapInfo{id=" + this.id + ", version=" + this.version + "}";
  }

  @Override
  public MapSource getSource() {
    return source;
  }

  @Override
  public @Nullable MapContext getContext() {
    return context != null ? context.get() : null;
  }

  @Override
  public Component getStyledName(MapNameStyle style) {
    TextComponent.Builder name = text().content(getName());

    if (style.isColor) name.color(NamedTextColor.GOLD);
    if (style.isHighlight) name.decoration(TextDecoration.UNDERLINED, true);
    if (style.showAuthors) {
      return translatable(
          "misc.authorship",
          NamedTextColor.DARK_PURPLE,
          name.build(),
          TextFormatter.list(
              getAuthors().stream()
                  .map(c -> c.getName(NameStyle.PLAIN).color(NamedTextColor.RED))
                  .collect(Collectors.toList()),
              NamedTextColor.DARK_PURPLE));
    }

    return name.build();
  }

  private static @NotNull List<String> parseRules(Element root) {
    return XMLUtils.flattenElements(root, "rules", "rule").stream()
        .map(Element::getTextNormalize)
        .collect(StreamUtils.toImmutableList());
  }

  private static @NotNull List<Gamemode> parseGamemodes(Element root) throws InvalidXMLException {
    ImmutableList.Builder<Gamemode> gamemodes = ImmutableList.builder();
    for (Element gamemodeEl : root.getChildren("gamemode")) {
      Gamemode gm = Gamemode.byId(gamemodeEl.getText());
      if (gm == null) throw new InvalidXMLException("Unknown gamemode", gamemodeEl);
      gamemodes.add(gm);
    }
    return gamemodes.build();
  }

  private static @NotNull List<Contributor> parseContributors(Element root, String tag)
      throws InvalidXMLException {
    List<Contributor> contributors = new ArrayList<>();
    for (Element child : XMLUtils.flattenElements(root, tag + "s", tag)) {
      String name = XMLUtils.getNormalizedNullableText(child);
      UUID uuid = XMLUtils.parseUuid(Node.fromAttr(child, "uuid"));
      String contribution = XMLUtils.getNullableAttribute(child, "contribution", "contrib");

      if (name == null && uuid == null) {
        throw new InvalidXMLException("Contributor must have either a name or UUID", child);
      }

      contributors.add(
          uuid == null
              ? new PseudonymContributor(name, contribution)
              : new PlayerContributor(uuid, contribution));
    }
    return contributors;
  }

  private static @NotNull WorldInfo parseWorld(Element root) throws InvalidXMLException {
    final Element world = root.getChild("terrain");
    return world == null ? new WorldInfoImpl() : new WorldInfoImpl(world);
  }

  protected void setContext(MapContextImpl context) {
    // The first time context is loaded, set properties which can't be
    // parsed until after modules are parsed, like team sizes or tags.
    if (this.context == null) {
      ImmutableSortedSet.Builder<MapTag> tags = ImmutableSortedSet.naturalOrder();
      ImmutableList.Builder<Integer> players = ImmutableList.builder();

      for (MapModule<?> module : context.getModules()) {
        tags.addAll(module.getTags());

        if (module instanceof TeamModule)
          players.addAll(
              Iterables.transform(((TeamModule) module).getTeams(), TeamFactory::getMaxPlayers));

        if (module instanceof FreeForAllModule)
          players.add(((FreeForAllModule) module).getOptions().maxPlayers);
      }

      if (world.hasTerrain()) tags.add(TERRAIN);

      this.tags = tags.build();
      this.players = players.build();

      // If the map defines no game-modes manually, derive them from map tags, sorted by auxiliary
      // last.
      if (this.gamemodes.isEmpty()) {
        this.gamemodes = this.tags.stream()
            .filter(MapTag::isGamemode)
            .sorted(
                Comparator.comparing(MapTag::isAuxiliary).thenComparing(Comparator.naturalOrder()))
            .map(MapTag::getGamemode)
            .collect(StreamUtils.toImmutableList());
      }
    }
    this.context = new SoftReference<>(context);
  }

  private static class VariantData implements VariantInfo {
    private final String variantId;
    private final String mapName;
    private final String mapId;
    private final String world;
    private final Range<Version> serverVersions;

    public VariantData(Element root, @Nullable Element variantEl) throws InvalidXMLException {
      String name = assertNotNull(Node.fromRequiredChildOrAttr(root, "name").getValueNormalize());
      String slug = assertNotNull(root).getChildTextNormalize("slug");
      Node minVer = Node.fromAttr(root, "min-server-version");
      Node maxVer = Node.fromAttr(root, "max-server-version");

      if (variantEl == null) {
        this.variantId = DEFAULT_VARIANT;
        this.mapName = name;
        this.world = null;
      } else {
        this.variantId = Node.fromRequiredAttr(variantEl, "id").getValue();
        if (DEFAULT_VARIANT.equals(variantId)) {
          throw new InvalidXMLException("Default variant is not allowed", variantEl);
        }
        boolean override = XMLUtils.parseBoolean(Node.fromAttr(variantEl, "override"), false);
        this.mapName = (override ? "" : name + ": ") + variantEl.getTextNormalize();
        this.world = variantEl.getAttributeValue("world");

        String variantSlug = variantEl.getAttributeValue("slug");
        if (variantSlug != null) slug = variantSlug;
        else if (slug != null) slug += "_" + variantId;

        Node minVerVariant = Node.fromAttr(variantEl, "min-server-version");
        if (minVerVariant != null) minVer = minVerVariant;

        Node maxVerVariant = Node.fromAttr(variantEl, "max-server-version");
        if (maxVerVariant != null) minVer = minVerVariant;
      }
      this.mapId = assertNotNull(slug != null ? slug : StringUtils.slugify(mapName));

      this.serverVersions = XMLUtils.parseClosedRange(
          minVer, XMLUtils.parseSemanticVersion(minVer), XMLUtils.parseSemanticVersion(maxVer));
    }

    @Override
    public String getVariantId() {
      return variantId;
    }

    @Override
    public String getMapId() {
      return mapId;
    }

    @Override
    public String getMapName() {
      return mapName;
    }

    @Override
    public String getWorld() {
      return world;
    }

    @Override
    public Range<Version> getServerVersions() {
      return serverVersions;
    }
  }
}
