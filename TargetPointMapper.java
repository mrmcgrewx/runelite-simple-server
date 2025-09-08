package net.runelite.client.server;

import java.awt.Rectangle;
import java.awt.Shape;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

public final class TargetPointMapper {
    private TargetPointMapper() {}

    public static TargetPoint fromGameObject(Client client, GameObject obj) {
        if (obj == null) return null;
        int id = obj.getId();
        String name = safeObjectName(client, id);
        return mapCommon(client, id, name, obj.getWorldLocation(), obj.getCanvasTilePoly(), null);
    }

    public static TargetPoint fromGroundObject(Client client, GroundObject obj) {
        if (obj == null) return null;
        int id = obj.getId();
        String name = safeObjectName(client, id);
        return mapCommon(client, id, name, obj.getWorldLocation(), obj.getCanvasTilePoly(), null);
    }

    public static TargetPoint fromNPC(Client client, NPC npc) {
        if (npc == null) return null;
        int id = npc.getId();
        String name = safeNpcName(npc, id);
        Shape hull = npc.getConvexHull();

        // Fallback single canvas point if hull is null
        Point fallback = null;
        LocalPoint lp = npc.getLocalLocation();
        if (lp != null) {
            Point p = Perspective.localToCanvas(client, lp, client.getPlane());
            if (p != null) fallback = p;
        }
        return mapCommon(client, id, name, npc.getWorldLocation(), hull, fallback);
    }

    /* --------- Core mapping --------- */

    public static TargetPoint mapCommon(
            Client client,
            int id,
            String name,
            WorldPoint wp,
            Shape canvasShape,
            Point canvasFallback
    ) {
        if (wp == null) return null;

        // World
        final int worldX = wp.getX();
        final int worldY = wp.getY();
        final int plane  = wp.getPlane();
        final int regionId = wp.getRegionID();

        // Scene + minimap + dist
        Integer sceneX = null, sceneY = null, distToPlayer = null;
        Integer minimapX = null, minimapY = null; boolean inMinimap = false;

        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp != null) {
            sceneX = lp.getSceneX();
            sceneY = lp.getSceneY();

            Point mm = Perspective.localToMinimap(client, lp);
            if (mm != null) {
                minimapX = mm.getX();
                minimapY = mm.getY();
                inMinimap = true;
            }

            Player me = client.getLocalPlayer();
            if (me != null && me.getWorldLocation() != null) {
                distToPlayer = me.getWorldLocation().distanceTo(wp);
            }
        }

        // Canvas (only if actually visible)
        Integer canvasX = null, canvasY = null;
        TargetPoint.BBox box = null;
        boolean inCanvas = false;

        Rectangle viewport = new Rectangle(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
        Rectangle r = null;

        if (canvasShape != null) {
            r = canvasShape.getBounds();
        } else if (canvasFallback != null) {
            r = new Rectangle(canvasFallback.getX() - 2, canvasFallback.getY() - 2, 4, 4);
        }

        if (r != null && r.width > 0 && r.height > 0 && r.intersects(viewport)) {
            inCanvas = true;
            canvasX = r.x + r.width / 2;
            canvasY = r.y + r.height / 2;
            box = new TargetPoint.BBox(r.x, r.y, r.width, r.height);
        }

        return new TargetPoint(
                id, name,
                worldX, worldY, plane, sceneX, sceneY, regionId,
                minimapX, minimapY, inMinimap,
                canvasX, canvasY, box, inCanvas,
                distToPlayer
        );
    }

    /* --------- Name helpers --------- */

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

    public static TargetPoint fromWallObject(Client client, WallObject obj) {
        if (obj == null) return null;
        WorldPoint wp = obj.getWorldLocation();
        Shape hull = obj.getConvexHull();
        Point fallback = null;
        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp != null) {
            Point p = Perspective.localToCanvas(client, lp, client.getPlane());
            if (p != null) fallback = p;
        }
        int id = obj.getId();
        String name = safeObjectName(client, id); // or "Barrier"
        return mapCommon(client, id, name, wp, hull, fallback);
    }

    public static TargetPoint fromDecorativeObject(Client client, DecorativeObject obj) {
        if (obj == null) return null;
        WorldPoint wp = obj.getWorldLocation();
        Shape hull = obj.getConvexHull();
        Point fallback = null;
        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp != null) {
            Point p = Perspective.localToCanvas(client, lp, client.getPlane());
            if (p != null) fallback = p;
        }
        int id = obj.getId();
        String name = safeObjectName(client, id);
        return mapCommon(client, id, name, wp, hull, fallback);
    }

    // --- NEW: convenience for any TileObject
    public static TargetPoint fromTileObject(Client client, TileObject to) {
        if (to == null) return null;
        if (to instanceof GameObject)       return fromGameObject(client, (GameObject) to);
        if (to instanceof GroundObject)     return fromGroundObject(client, (GroundObject) to);
        if (to instanceof WallObject)       return fromWallObject(client, (WallObject) to);
        if (to instanceof DecorativeObject) return fromDecorativeObject(client, (DecorativeObject) to);
        return null;
    }
}
