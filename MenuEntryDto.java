package net.runelite.client.server;

@lombok.Data
public class MenuEntryDto {
    private final int idx;       // entry index
    private final String option; // e.g., "Repair", "Talk-to"
    private final String target; // e.g., "Apprentice"
    private final int x;         // canvas click X (center of row)
    private final int y;         // canvas click Y
}
