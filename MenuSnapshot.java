package net.runelite.client.server;

import net.runelite.api.Client;
import net.runelite.api.MenuEntry;

import javax.annotation.Nullable;

@lombok.Data
public class MenuSnapshot {
    private final boolean open;
    private final int x, y, w, h;
    private final java.util.List<MenuEntryDto> entries;

    @Nullable
    public static MenuSnapshot buildMenuSnapshot(Client client) {
        try {
            boolean open = client.isMenuOpen();
            if (!open) {
                return null;
            }

            final int mx = client.getMenuX();
            final int my = client.getMenuY();
            final int mw = client.getMenuWidth();
            final int mh = client.getMenuHeight();

            final MenuEntry[] me = client.getMenuEntries();
            if (me == null || me.length == 0) {
                return new MenuSnapshot(true, mx, my, mw, mh, java.util.Collections.emptyList());
            }

            // OSRS menu metrics (these are stable)
            final int HEADER = 19;
            final int ROW    = 15;

            java.util.List<MenuEntryDto> list = new java.util.ArrayList<>(me.length);

            // If your rows are misaligned, flip drawIdx = (me.length - 1 - i)
            for (int i = 0; i < me.length; i++) {
                int drawIdx = i; // flip if your menu draws reversed
                int cx = mx + mw / 2;
                int cy = my + HEADER + (ROW / 2) + drawIdx * ROW;

                String option = me[i].getOption();
                String target = me[i].getTarget();

                list.add(new MenuEntryDto(i,
                        option != null ? option : "",
                        target != null ? target : "",
                        cx, cy));
            }

            return new MenuSnapshot(true, mx, my, mw, mh, list);
        } catch (Throwable t) {
            return new MenuSnapshot(false, 0, 0, 0, 0, java.util.Collections.emptyList());
        }
    }

}


