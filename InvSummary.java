package net.runelite.client.server;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class InvSummary {
    int emptySlots;
}
