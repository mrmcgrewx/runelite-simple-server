package net.runelite.client.server;

import lombok.Value;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Immutable snapshot of the local player for downstream automation.
 */
@Value
public class PlayerSnapshot {
    // Position
    int worldX;
    int worldY;
    int plane;
    int sceneX;
    int sceneY;

    // Pathing (destination tile if any)
    Integer destWorldX;
    Integer destWorldY;

    // Activity
    boolean moving;       // derived by comparing last tick vs now
    int animationId;      // -1 = idle
    String action;        // "idle" | "moving" | "animating"

    // Interacting target (Actors only: NPC/Player)
    Interacting interacting;

    @Value
    public static class Interacting {
        String kind;        // "NPC" | "Player" | "Actor"
        String name;        // may be null for some actors
        Integer id;         // NPC id if NPC, else null
        Integer worldX;
        Integer worldY;
        Integer plane;
        Integer distance;   // tiles from player
    }

    /**
     * Capture a snapshot. Supply lastWp from the previous tick to compute "moving".
     */
    public static PlayerSnapshot capture(Client client, WorldPoint lastWp) {
        final Player me = client.getLocalPlayer();
        if (me == null) {
            return new PlayerSnapshot(0, 0, 0, 0, 0, null, null, false, -1, "unknown", null);
        }

        final WorldPoint now = me.getWorldLocation();
        final boolean moving = (lastWp != null) && !now.equals(lastWp);

        // Destination (path target) if any
        Integer dwx = null, dwy = null;
        final LocalPoint dest = client.getLocalDestinationLocation();
        if (dest != null) {
            final WorldPoint dwp = WorldPoint.fromLocal(client, dest);
            if (dwp != null) {
                dwx = dwp.getX();
                dwy = dwp.getY();
            }
        }

        // Action label
        final int anim = me.getAnimation(); // -1 = idle
        final String action = (anim != -1) ? "animating" : (moving || dest != null) ? "moving" : "idle";

        // Interacting (Actor only)
        Interacting ix = null;
        final Actor a = me.getInteracting();
        if (a != null) {
            final String kind = (a instanceof NPC) ? "NPC" : (a instanceof Player) ? "Player" : "Actor";
            final String name = a.getName();
            final Integer id = (a instanceof NPC) ? ((NPC) a).getId() : null;
            final WorldPoint awp = a.getWorldLocation();
            final Integer dist = (awp != null) ? awp.distanceTo(now) : null;

            ix = new Interacting(
                    kind,
                    name,
                    id,
                    (awp != null) ? awp.getX() : null,
                    (awp != null) ? awp.getY() : null,
                    (awp != null) ? awp.getPlane() : null,
                    dist
            );
        }

        return new PlayerSnapshot(
                now.getX(), now.getY(), now.getPlane(),
                me.getLocalLocation().getSceneX(), me.getLocalLocation().getSceneY(),
                dwx, dwy,
                moving,
                anim,
                action,
                ix
        );
    }
}

