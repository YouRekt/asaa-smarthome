package org.asaa.tasks.appliances.DishwasherAgent;

import org.asaa.agents.appliances.DishwasherAgent;
import org.asaa.behaviours.appliances.TaskBehaviour;

public class WashDishesTask extends TaskBehaviour<DishwasherAgent> {
    private final long endTime;
    private final long nonResumableStartTime;
    private final long nonResumableEndTime;

    private final long duration;
    private final double nonResumableStartPercent;
    private final double nonResumableEndPercent;

    public WashDishesTask(DishwasherAgent agent, long duration, double nonResumableStartPercent, double nonResumableEndPercent) {
        super(agent, "wash-dishes-task", 1, true, false);
        this.endTime = System.currentTimeMillis() + duration;
        this.nonResumableStartTime = (long)(endTime * nonResumableStartPercent);
        this.nonResumableEndTime = (long)(endTime * nonResumableEndPercent);
        this.duration = duration;
        this.nonResumableStartPercent = nonResumableStartPercent;
        this.nonResumableEndPercent = nonResumableEndPercent;
    }

    private WashDishesTask(DishwasherAgent agent, int priority, long duration, double nonResumableStartPercent, double nonResumableEndPercent) {
        super(agent, "wash-dishes-task", priority, true, false);
        this.endTime = System.currentTimeMillis() + duration;
        this.nonResumableStartTime = (long)(endTime * nonResumableStartPercent);
        this.nonResumableEndTime = (long)(endTime * nonResumableEndPercent);
        this.duration = duration;
        this.nonResumableStartPercent = nonResumableStartPercent;
        this.nonResumableEndPercent = nonResumableEndPercent;
    }

    @Override
    protected TaskBehaviour<?> resumeWith(int priority) {
        return new WashDishesTask(agent, priority, duration, nonResumableStartPercent, nonResumableEndPercent);
    }

    @Override
    protected boolean execute() {
        long currentTime = System.currentTimeMillis();

        if (endTime <= currentTime) {
            agent.getLogger().info("Wash complete!");
            return true;
        }

        if (nonResumableStartTime <= currentTime && nonResumableEndTime >= currentTime && pausable) {
            agent.getLogger().info("Dishwasher can not be paused now for {}ms", nonResumableEndTime - currentTime);
            pausable = false;
        } else if (nonResumableEndTime <= currentTime && !pausable) {
            agent.getLogger().info("Dishwasher may be paused again");
            pausable = true;
        }

        return false;
    }
}
