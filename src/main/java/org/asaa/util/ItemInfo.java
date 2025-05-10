package org.asaa.util;

import lombok.Getter;
import lombok.Setter;

public class ItemInfo {
    @Setter
    @Getter
    private int count;
    @Setter
    @Getter
    private int priority;

    public ItemInfo(final int count, final int priority) {
        this.count = count;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "ItemInfo [count=" + count + ", priority=" + priority + "]";
    }

    public void increaseCount(int delta) {
        this.count += delta;
    }
}