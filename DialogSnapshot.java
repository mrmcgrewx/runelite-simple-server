package net.runelite.client.server;

import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.annotation.Nullable;

@lombok.Data
public class DialogSnapshot {
    private final boolean hasDialog;
    private final boolean canContinue;
    private final Integer continueX;
    private final Integer continueY;
    private final java.util.List<DialogOptionDto> options;

    @Nullable
    public static DialogSnapshot buildDialogSnapshot(Client client) {
        boolean hasDialog = false;
        boolean canContinue = false;
        Integer contX = null, contY = null;
        java.util.List<DialogOptionDto> options = new java.util.ArrayList<>();

        // Continue widgets (NPC/Player)
        Widget npcCont = client.getWidget(ComponentID.DIALOG_NPC_TEXT);
        Widget plyCont = client.getWidget(ComponentID.DIALOG_PLAYER_TEXT);
        Widget cont = npcCont != null && !npcCont.isHidden() ? npcCont :
                plyCont != null && !plyCont.isHidden() ? plyCont : null;

        if (cont != null && !cont.isHidden()) {
            java.awt.Rectangle r = cont.getBounds();
            if (r != null && r.width > 0 && r.height > 0) {
                canContinue = true; hasDialog = true;
                contX = r.x + r.width / 2;
                contY = r.y + r.height / 2;
            }
        }

        // Option list (variable number of lines)
        Widget opts = client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);
        if (opts != null && !opts.isHidden()) {
            hasDialog = true;
            Widget[] children = opts.getChildren();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    Widget w = children[i];
                    if (w == null || w.isHidden()) continue;
                    String text = w.getText();
                    java.awt.Rectangle r = w.getBounds();
                    if (r != null && r.width > 0 && r.height > 0 && text != null && !text.isEmpty()) {
                        int cx = r.x + r.width / 2;
                        int cy = r.y + r.height / 2;
                        options.add(new DialogOptionDto(i, text, cx, cy));
                    }
                }
            }
        }

        if (!hasDialog && !canContinue) return null;
        return new DialogSnapshot(hasDialog, canContinue, contX, contY, options);
    }

}