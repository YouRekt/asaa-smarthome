package org.asaa.behaviours.appliances.DishwasherAgent;

import org.asaa.agents.appliances.DishwasherAgent;
import org.asaa.behaviours.appliances.tasks.TaskBehaviour;
import org.asaa.behaviours.appliances.tasks.TaskInfo;

public class WashDishesTask extends TaskBehaviour<DishwasherAgent> {
    private final long nonResumableStartTime;
    private final long nonResumableEndTime;
    private final double nonResumableStartPercent;
    private final double nonResumableEndPercent;
    private long fullWashTime;
    private long remainingWashTime;
    private long washStartTime;
    private boolean firstCycle = true;

    public WashDishesTask(DishwasherAgent agent, long fullWashTime, double nonResumableStartPercent, double nonResumableEndPercent) {
        super(agent, "wash-dishes-task", 1, true, false, TaskInfo.Type.USER_COMFORT);
        this.fullWashTime = fullWashTime;
        this.remainingWashTime = fullWashTime;
        this.nonResumableStartTime = (long) (this.fullWashTime * nonResumableStartPercent);
        this.nonResumableEndTime = (long) (this.fullWashTime * nonResumableEndPercent);
        this.nonResumableStartPercent = nonResumableStartPercent;
        this.nonResumableEndPercent = nonResumableEndPercent;
    }

    private WashDishesTask(DishwasherAgent agent, int priority, long fullWashTime, double nonResumableStartPercent, double nonResumableEndPercent) {
        super(agent, "wash-dishes-task", priority, true, false, TaskInfo.Type.USER_COMFORT);
        this.fullWashTime = fullWashTime;
        this.remainingWashTime = fullWashTime;
        this.nonResumableStartTime = (long) (this.fullWashTime * nonResumableStartPercent);
        this.nonResumableEndTime = (long) (this.fullWashTime * nonResumableEndPercent);
        this.nonResumableStartPercent = nonResumableStartPercent;
        this.nonResumableEndPercent = nonResumableEndPercent;
    }

    @Override
    protected TaskBehaviour<?> resumeWith(int priority) {
        return new WashDishesTask(agent, priority, fullWashTime, nonResumableStartPercent, nonResumableEndPercent);
    }

    @Override
    protected boolean execute() {
        if (remainingWashTime <= 0) {
            agent.getLogger().info("Wash complete!");
            return true;
        } else if (firstCycle) {
            agent.getLogger().info("Wash {} for {}ms", (remainingWashTime != fullWashTime ? "resumed" : "started"), remainingWashTime);
            washStartTime = System.currentTimeMillis();
            fullWashTime = remainingWashTime;
            firstCycle = false;
        }

        remainingWashTime = Math.max(0, fullWashTime - System.currentTimeMillis() + washStartTime);
        if (remainingWashTime <= nonResumableStartTime && remainingWashTime >= nonResumableEndTime && pausable) {
            agent.getLogger().info("Wash Dishes Task entering an unpausable state");
            pausable = false;
        } else if (remainingWashTime < nonResumableEndTime && !pausable) {
            agent.getLogger().info("Wash Dishes Task may be paused again");
            pausable = true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        remainingWashTime = Math.max(0, fullWashTime - System.currentTimeMillis() + washStartTime);
        firstCycle = true;
    }
}
