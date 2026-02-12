package tc.oc.pgm.platform.modern.modules.compatibility;

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

@NullMarked
@ListenerScope(MatchScope.LOADED)
public class CompatibilityMatchModule implements MatchModule, Listener {
  private final Match match;
  private final CompatibilityOptions options;

  public CompatibilityMatchModule(Match match, CompatibilityOptions options) {
    this.match = match;
    this.options = options;
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
  public void on(EntitySpawnEvent ev) {
    if (ev.getEntityType() == EntityType.TNT && !options.randomTntOffset()) {
      ev.getEntity().setVelocity(new Vector(0, ev.getEntity().getVelocity().getY(), 0));
    }
  }
}
