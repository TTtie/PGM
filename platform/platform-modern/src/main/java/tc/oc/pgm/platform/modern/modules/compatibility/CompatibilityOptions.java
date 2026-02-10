package tc.oc.pgm.platform.modern.modules.compatibility;

/**
 * @param relativeProjectileVelocity Whether relative projectile velocity is enabled (restores
 *     projectiles to pre-1.9 behavior if disabled)
 * @param randomTntOffset Whether to make TNT jump off when ignited (disabling enables easier cannon
 *     building)
 * @param tntMovesInWater Whether TNT should be affected by water (disabling enables easier cannon
 *     building)
 */
public record CompatibilityOptions(
    boolean relativeProjectileVelocity, boolean randomTntOffset, boolean tntMovesInWater) {}
