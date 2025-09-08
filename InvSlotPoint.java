package net.runelite.client.server;

import lombok.Value;

/** Canvas-relative box for an inventory slot (center + bounds). */
@Value
public class InvSlotPoint {
    //int slot;       // 0..27
    int itemId;     // actual item id in this slot
    String name;    // item name (ItemComposition#getName)
    int qty;        // stack size
    int canvasX;    // top-left of slot in canvas coords
    int canvasY;
    int w;
    int h;

    public int centerX() { return canvasX + w / 2; }
    public int centerY() { return canvasY + h / 2; }
}
