package net.runelite.client.server;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BankWidget {
    public static final int BANK_KEYPAD_A_ID = 13959184;
    public static final int BANK_KEYPAD_B_ID = 13959186;
    public static final int BANK_KEYPAD_C_ID = 13959188;
    public static final int BANK_KEYPAD_D_ID = 13959190;
    public static final int BANK_KEYPAD_E_ID = 13959192;
    public static final int BANK_KEYPAD_F_ID = 13959194;
    public static final int BANK_KEYPAD_G_ID = 13959196;
    public static final int BANK_KEYPAD_H_ID = 13959198;
    public static final int BANK_KEYPAD_I_ID = 13959200;
    public static final int BANK_KEYPAD_J_ID = 13959202;

    public static final List<Integer> ALL_KEYS = List.of(BANK_KEYPAD_A_ID, BANK_KEYPAD_B_ID, BANK_KEYPAD_C_ID,
            BANK_KEYPAD_D_ID, BANK_KEYPAD_E_ID, BANK_KEYPAD_F_ID, BANK_KEYPAD_G_ID, BANK_KEYPAD_H_ID,
            BANK_KEYPAD_I_ID, BANK_KEYPAD_J_ID);

    public static final int BANK_CLOSE_ID = 786434;

    public static boolean isBankLoginVisible(Client client) {
        Widget bankLogin = client.getWidget(ComponentID.BANK_PIN_CONTAINER);
        return bankLogin != null && !bankLogin.isHidden();
    }

    public static boolean isBankVisible(Client client) {
        Widget bankMain = client.getWidget(ComponentID.BANK_CONTAINER);
        return bankMain != null && !bankMain.isHidden();
    }

    @Nullable
    public static Widget findVisibleBankFirstSlot(Client client) {
        Widget cont = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
        if (cont == null || cont.isHidden()) return null;

        // Bank items are typically dynamic children
        Widget[] slots = cont.getDynamicChildren();
        if (slots == null || slots.length == 0) {
            slots = cont.getChildren(); // fallback if your client version uses children
        }
        if (slots == null || slots.length == 0) return null;

        Rectangle view = cont.getBounds(); // canvas-space viewport of the scrolled area
        Widget best = null;
        int bestY = Integer.MAX_VALUE, bestX = Integer.MAX_VALUE;

        for (Widget w : slots) {
            if (w == null) continue;
            // skip empties; for bank item slots, getItemId() works
            if (w.getItemId() <= 0) continue;

            Rectangle r = w.getBounds();
            if (r == null || r.width <= 0 || r.height <= 0) continue;
            if (!r.intersects(view)) continue; // not actually visible (scrolled off)

            // pick the top-left visible item
            if (r.y < bestY || (r.y == bestY && r.x < bestX)) {
                bestY = r.y; bestX = r.x; best = w;
            }
        }
        return best;
    }


    public static Widget getCloseButton(Client client) {
        Widget w = client.getWidget(BANK_CLOSE_ID);
        if (w == null) return null;
        return w.getChild(3);
    }

    public static List<Widget> getKeyWidgets(Client client, String pin) {
        boolean[] want = new boolean[10];
        for (int i = 0; i < pin.length(); i++) {
            char c = pin.charAt(i);
            if (c >= '0' && c <= '9') want[c - '0'] = true;
        }

        List<Widget> bankPinWidgets = new ArrayList<>();

        for (int keyId : BankWidget.ALL_KEYS) {
            Widget w = client.getWidget(keyId);
            if (w == null || w.isHidden()) continue;

            Widget sw = w.getChild(1);
            if (sw == null) continue;
            String txt = sw.getText();
            if (txt == null) continue;

            String num = txt.trim();
            if (num.length() == 1) {
                char d = num.charAt(0);
                if (d >= '0' && d <= '9' && want[d - '0']) {
                    bankPinWidgets.add(w);
                }
            }
        }
        return bankPinWidgets;
    }
}
