package net.runelite.client.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TargetPoint {
    // Identity
    private final Integer id;            // object or NPC id
    private final String name;       // display name/label

    // World/scene info
    private final Integer worldX;        // WorldPoint.getX()
    private final Integer worldY;        // WorldPoint.getY()
    private final Integer plane;         // 0..3
    private final Integer sceneX;    // 0..103 (nullable if outside scene)
    private final Integer sceneY;    // 0..103 (nullable)
    private final Integer regionId;      // WorldPoint.getRegionID()

    // Minimap (only if within minimap circle)
    private final Integer minimapX;  // pixel; nullable
    private final Integer minimapY;  // pixel; nullable
    private final boolean inMinimap;

    // Canvas (only if on-screen)
    private final Integer canvasX;   // pixel center; nullable
    private final Integer canvasY;   // pixel center; nullable
    private final BBox canvasBox;    // nullable
    private final boolean inCanvas;

    // Optional: distance to local player in tiles
    private final Integer distToPlayer;

    @Data
    @AllArgsConstructor
    public static class BBox {
        private final Integer x;
        private final Integer y;
        private final Integer w;
        private final Integer h;
    }
}
