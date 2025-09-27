package net.runelite.client.server;

@lombok.Data
public class DialogOptionDto {
    private final int idx;
    private final String text;
    private final int x;   // canvas click X (center of line)
    private final int y;   // canvas click Y
}
