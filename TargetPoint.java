package net.runelite.client.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TargetPoint {
    private final String name;
    // Minimap (only if within minimap circle)
    private final Integer minimapX;  // pixel; nullable
    private final Integer minimapY;  // pixel; nullable
    private final boolean inMinimap;

    // Canvas (only if on-screen)
    private final Integer x;
    private final Integer y;
    private final boolean inCanvas;

    // Approach point if target is out of minimap and canvas
    private final Integer approachX;
    private final Integer approachY;

    private final Integer distToPlayer;
}
