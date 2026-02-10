package tc.oc.pgm.platform.modern.modules;

import static tc.oc.pgm.util.platform.Supports.Variant.PAPER;

import org.jspecify.annotations.NullMarked;
import tc.oc.pgm.api.Modules;
import tc.oc.pgm.platform.modern.modules.compatibility.CompatibilityMatchModule;
import tc.oc.pgm.platform.modern.modules.compatibility.CompatibilityModule;
import tc.oc.pgm.platform.modern.modules.trim.TrimMatchModule;
import tc.oc.pgm.platform.modern.modules.trim.TrimModule;
import tc.oc.pgm.platform.modern.modules.waypoint.WaypointMatchModule;
import tc.oc.pgm.util.platform.Supports;

@NullMarked
@Supports(PAPER)
public class ModernModuleRegistrar implements Modules.ModuleRegistrar {
  @Override
  public void registerModules(Modules modules) {
    modules.register(TrimModule.class, TrimMatchModule.class, new TrimModule.Factory());
    modules.register(WaypointMatchModule.class, WaypointMatchModule::new);
    modules.register(
        CompatibilityModule.class,
        CompatibilityMatchModule.class,
        new CompatibilityModule.Factory());
  }
}
