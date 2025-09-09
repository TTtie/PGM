package tc.oc.pgm.util.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PgmLingeringPotionSplashEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final Entity cloud;
  private final ThrownPotion potion;
  private final Player owner;

  public PgmLingeringPotionSplashEvent(Entity cloud, ThrownPotion potion, Player owner) {
    this.cloud = cloud;
    this.potion = potion;
    this.owner = owner;
  }

  public Entity getCloud() {
    return cloud;
  }

  public ThrownPotion getPotion() {
    return potion;
  }

  public Player getOwner() {
    return owner;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
