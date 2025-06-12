package org.asaa.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemInfo {
    private int count;
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