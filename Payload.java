package net.runelite.client.server;

import lombok.Data;

@Data
public abstract class Payload {
    public long seq;
    public long ts;

    public boolean loggedIn;

    public InvSummary inv;

    public PlayerSnapshot player;
    public MenuSnapshot menu;
    public DialogSnapshot dialog;
}
