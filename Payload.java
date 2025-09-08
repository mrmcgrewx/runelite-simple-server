package net.runelite.client.server;

import lombok.Data;

@Data
public abstract class Payload {
    public long seq;
    public long ts;
    public boolean loggedIn;

    public InvSummary inv;

    // Canvas meta (handy for renderers)
    public Integer canvasW;
    public Integer canvasH;

    public Integer canvasScreenX;
    public Integer canvasScreenY;

    public PlayerSnapshot player;
}
