package net.runelite.client.server;

import java.awt.*;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;

import javax.annotation.Nullable;

@Slf4j
public final class TargetPointMapper {
    private TargetPointMapper() {}

    public static TargetPoint fromTileObject(Client client, TileObject obj) {
        return fromTileObject(client, obj, null);
    }

    public static @Nullable TargetPoint fromTileObject(
            Client client, @Nullable TileObject obj, @Nullable String name)
    {
        if (obj == null) return null;

        // Try clickbox, but it can throw inside RL during certain frames.
        Shape shape = null;
        try {
            shape = obj.getClickbox();
        } catch (RuntimeException ex) {
            // Seen when object is (de)spawning or model data is transiently null.
            // Log at debug to avoid spam.
            // log.debug("clickbox failed for id={} wp={} : {}", obj.getId(), safeWp(obj), ex.toString());
        }

        if (shape == null) {
            // getCanvasTilePoly is safer, but can also be null (off-screen / not built yet)
            try {
                shape = obj.getCanvasTilePoly();
            } catch (RuntimeException ignored) {}
        }

        WorldPoint wp = null;
        try {
            wp = obj.getWorldLocation();
        } catch (RuntimeException ignored) {}

        final String label = (name != null ? name : safeObjectName(client, obj.getId()));
        return TargetPointMapper.mapCommon(client, wp, shape, label);
    }

    // optional tiny helper for logging
    private static @Nullable String safeWp(TileObject o) {
        try { WorldPoint w = o.getWorldLocation(); return (w != null ? w.toString() : "null"); }
        catch (Exception e) { return "err"; }
    }

    public static TargetPoint fromNPC(Client client, NPC npc) {
        if (npc == null) return null;
        Shape hull = npc.getConvexHull();
        return mapCommon(client, npc.getWorldLocation(), hull, null);
    }

    public static @Nullable TargetPoint fromWidget(Client client, @Nullable Widget w) {
        return fromWidget(client, w, null);
    }

    public static @Nullable TargetPoint fromWidget(Client client, @Nullable Widget w, @Nullable String name) {
        if (w == null || w.isHidden()) return null;
        final Rectangle rect = w.getBounds();
        return mapCommon(client, null, rect, name);
    }

    /* --------- Core mapping --------- */

    public static TargetPoint mapCommon(
            Client client,
            @Nullable WorldPoint wp,
            @Nullable Shape clickBox,
            @Nullable String name) {

        // ---- World/minimap/scene ----
        Integer distToPlayer = null;
        Integer minimapX = null, minimapY = null;
        boolean inMinimap = false;
        Integer x = null;
        Integer y = null;
        boolean inCanvas = false;
        Point canvas = null;
        Integer approachX = null;
        Integer approachY = null;

        try {
            canvas = client.getCanvas().getLocationOnScreen();
        } catch (java.awt.IllegalComponentStateException ex) {
            // window not realized/visible
        }

        if (wp != null) {
            LocalPoint lp = LocalPoint.fromWorld(client, wp);
            if (lp != null) {
                net.runelite.api.Point mm = Perspective.localToMinimap(client, lp);
                if (mm != null) {
                    if (canvas == null) return null;
                    minimapX = canvas.x + mm.getX();
                    minimapY = canvas.y + mm.getY();
                    inMinimap = true;
                }
            }

            Player me = client.getLocalPlayer();
            if (me != null && me.getWorldLocation() != null) {
                distToPlayer = me.getWorldLocation().distanceTo(wp);
            }
        }

        Rectangle viewport = client.getCanvas().getBounds();
        Rectangle r = null;

        if (clickBox != null) {
            r = clickBox.getBounds();
        }

        if (r != null && r.width > 0 && r.height > 0 && r.intersects(viewport) && !underWidget(client, r)) {
            inCanvas = true;
            int cx = r.x + r.width / 2;
            int cy = r.y + r.height / 2;

            if (canvas != null) {
                x = canvas.x + cx;
                y = canvas.y + cy;
            }
        }

        if (!inMinimap && !inCanvas) {
            Point approach = minimapEdgeToward(client, wp);
            if (approach != null) {
                approachX = approach.x;
                approachY = approach.y;
            }
        }

        return new TargetPoint(
                name,
                minimapX, minimapY, inMinimap,
                x, y, inCanvas,
                approachX, approachY,
                distToPlayer
        );
    }

    /* --------- Name helpers --------- */

    public static boolean underWidget(Client client, Rectangle r) {
        Rectangle mapR = getWidgetBounds(client.getWidget(ComponentID.MINIMAP_CONTAINER));
        Rectangle chatboxR = getWidgetBounds(client.getWidget(ComponentID.CHATBOX_CONTAINER));
        Rectangle invR = getWidgetBounds(client.getWidget(ComponentID.INVENTORY_CONTAINER));

        return intersect(r, mapR) || intersect(r, chatboxR) || intersect(r, invR);
    }

    private static Rectangle getWidgetBounds(Widget w) {
        if (w != null && !w.isHidden()) {
            return w.getBounds();
        }
        return null;
    }

    private static boolean intersect(Rectangle r, Rectangle w) {
        if (w != null) {
            return r.intersects(w);
        }
        return false;
    }

    @Nullable
    static java.awt.Point minimapEdgeToward(Client client, WorldPoint targetWp) {
        Player me = client.getLocalPlayer();
        if (me == null || me.getWorldLocation() == null) return null;
        Widget drawArea = getMinimapDrawArea(client);
        if (drawArea == null) return null;

        WorldPoint meWp = me.getWorldLocation();
        int dx = targetWp.getX() - meWp.getX();
        int dy = targetWp.getY() - meWp.getY();
        if (dx == 0 && dy == 0) return null;

        // Bounding square for circular map
        Rectangle b = drawArea.getBounds();
        int cx = b.x + b.width / 2;
        int cy = b.y + b.height / 2;

        // radius with a small safety margin so we click inside the ring
        int radius = Math.min(b.width, b.height) / 2 - 6;

        // World → screen direction, compensating minimap rotation by camera yaw
        // RuneLite yaw is 0..2047 for 0..360°. Convert to radians.
        double yaw = (client.getCameraYaw() & 2047) * (2 * Math.PI / 2048.0);

        // OSRS world Y increases to the south; you may need to flip dy depending on
        // how your test looks. Start with this and adjust sign if it’s 180° off.
        double worldTheta = Math.atan2(dy, dx);
        double screenTheta = worldTheta - yaw;

        int ex = (int)Math.round(cx + radius * Math.cos(screenTheta));
        int ey = (int)Math.round(cy + radius * Math.sin(screenTheta));
        return new java.awt.Point(ex, ey);
    }


    @Nullable
    private static Widget getMinimapDrawArea(Client client) {
        int[] ids = new int[] {
                ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MINIMAP_DRAW_AREA,
//                ComponentID.RESIZABLE_VIEWPORT_MINIMAP_DRAW_AREA,
//                ComponentID.FIXED_VIEWPORT_MINIMAP_DRAW_AREA
        };
        for (int id : ids) {
            Widget w = client.getWidget(id);
            if (w != null && !w.isHidden()) return w;
        }
        return null;
    }

    public static String safeObjectName(Client client, int objectId) {
        ObjectComposition def = client.getObjectDefinition(objectId);
        String n = (def != null) ? def.getName() : null;
        if (n == null || n.equalsIgnoreCase("null") || n.isEmpty()) n = "Object " + objectId;
        return n;
    }

    private static String safeNpcName(NPC npc, int npcId) {
        String n = npc.getName();
        if (n == null || n.equalsIgnoreCase("null") || n.isEmpty()) n = "NPC " + npcId;
        return n;
    }
}
