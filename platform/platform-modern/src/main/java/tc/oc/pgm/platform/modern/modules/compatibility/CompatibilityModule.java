package tc.oc.pgm.platform.modern.modules.compatibility;

import java.util.logging.Logger;
import org.jdom2.Document;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tc.oc.pgm.api.map.MapModule;
import tc.oc.pgm.api.map.factory.MapFactory;
import tc.oc.pgm.api.map.factory.MapModuleFactory;
import tc.oc.pgm.api.match.Match;
import tc.oc.pgm.api.module.exception.ModuleLoadException;
import tc.oc.pgm.util.Version;
import tc.oc.pgm.util.xml.InvalidXMLException;

@NullMarked
public class CompatibilityModule implements MapModule<CompatibilityMatchModule> {
  protected static final Version VERSION_1_8_8 = new Version(1, 8, 8);
  private final CompatibilityOptions options;

  private CompatibilityModule(CompatibilityOptions options) {
    this.options = options;
  }

  @Override
  public @Nullable CompatibilityMatchModule createMatchModule(Match match)
      throws ModuleLoadException {
    return new CompatibilityMatchModule(match, options);
  }

  public static class Factory implements MapModuleFactory<CompatibilityModule> {
    @Override
    public @Nullable CompatibilityModule parse(MapFactory factory, Logger logger, Document doc)
        throws InvalidXMLException {
      final var isLegacy = factory.supportsVersion(VERSION_1_8_8);
      var parser = factory.getParser();
      boolean relativeProjectileVelocity = !isLegacy;
      boolean randomTntOffset = false;
      boolean tntMovesInWater = false;
      boolean invertCollisionOrder = true;

      for (var element : doc.getRootElement().getChildren("compatibility")) {
        relativeProjectileVelocity = parser
            .parseBool(element, "relative-projectile-velocity")
            .child()
            .optional(relativeProjectileVelocity);
        randomTntOffset =
            parser.parseBool(element, "random-tnt-offset").child().optional(randomTntOffset);
        tntMovesInWater =
            parser.parseBool(element, "tnt-moves-in-water").child().optional(tntMovesInWater);
        invertCollisionOrder = parser
            .parseBool(element, "invert-entity-collision-order")
            .child()
            .optional(invertCollisionOrder);
      }

      return new CompatibilityModule(new CompatibilityOptions(
          relativeProjectileVelocity, randomTntOffset, tntMovesInWater, invertCollisionOrder));
    }
  }
}
