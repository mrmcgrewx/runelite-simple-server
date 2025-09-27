package net.runelite.client.server;

import lombok.Value;

/** Canvas-relative box for an inventory slot (center + bounds). */
@Value
public class InvSlotPoint {
    Integer itemId;     // actual item id in this slot
    String name;    // item name (ItemComposition#getName)
    Integer qty;        // stack size
    Integer x;
    Integer y;
}
