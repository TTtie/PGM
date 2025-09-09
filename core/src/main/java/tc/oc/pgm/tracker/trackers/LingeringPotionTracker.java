package tc.oc.pgm.tracker.trackers;

import org.bukkit.event.EventHandler;
import tc.oc.pgm.api.match.Match;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.api.tracker.info.PotionInfo;
import tc.oc.pgm.tracker.TrackerMatchModule;
import tc.oc.pgm.tracker.info.ThrownPotionInfo;
import tc.oc.pgm.util.event.entity.PgmLingeringPotionSplashEvent;

public class LingeringPotionTracker extends AbstractTracker<PotionInfo> {

  public LingeringPotionTracker(TrackerMatchModule tmm, Match match) {
    super(PotionInfo.class, tmm, match);
  }

  @EventHandler
  public void onLingeringPotionSplash(PgmLingeringPotionSplashEvent event) {
    final MatchPlayer owner = match.getPlayer(event.getOwner());
    if (owner != null) {
      entities()
          .trackEntity(
              event.getCloud(),
              new ThrownPotionInfo(event.getPotion(), owner.getParticipantState()));
    }
  }
}
