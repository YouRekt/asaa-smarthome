[1mdiff --cc src/main/java/org/asaa/tasks/appliances/CoffeeMachineAgent/MakeCoffeeTask.java[m
[1mindex 986728b,00a291b..0000000[m
[1m--- a/src/main/java/org/asaa/tasks/appliances/CoffeeMachineAgent/MakeCoffeeTask.java[m
[1m+++ b/src/main/java/org/asaa/tasks/appliances/CoffeeMachineAgent/MakeCoffeeTask.java[m
[36m@@@ -1,28 -1,23 +1,52 @@@[m
  package org.asaa.tasks.appliances.CoffeeMachineAgent;[m
  [m
[32m++<<<<<<< Updated upstream[m
[32m +import jade.core.behaviours.WakerBehaviour;[m
[32m +import org.asaa.agents.appliances.CoffeeMachineAgent;[m
[32m +import org.asaa.tasks.Task;[m
[32m +[m
[32m +public final class MakeCoffeeTask extends Task {[m
[32m +    private final CoffeeMachineAgent agent;[m
[32m +    private final long duration = 10000;[m
[32m +[m
[32m +    public MakeCoffeeTask(CoffeeMachineAgent agent) {[m
[32m +        super(agent, false, false);[m
[32m +[m
[32m +        this.agent = agent;[m
[32m +    }[m
[32m +[m
[32m +    @Override[m
[32m +    protected void execute() {[m
[32m +        agent.getLogger().info("Making coffee");[m
[32m +        agent.addBehaviour(new WakerBehaviour(agent, duration) {[m
[32m +            @Override[m
[32m +            protected void onWake() {[m
[32m +                agent.getLogger().info("Coffee made! Enjoy");[m
[32m +                end(true);[m
[32m +            }[m
[32m +        });[m
[32m +    }[m
[32m +}[m
[32m++=======[m
[32m+ import org.asaa.agents.appliances.CoffeeMachineAgent;[m
[32m+ import org.asaa.behaviours.appliances.tasks.TaskBehaviour;[m
[32m+ [m
[32m+ public class MakeCoffeeTask extends TaskBehaviour<CoffeeMachineAgent> {[m
[32m+     private final long endTime;[m
[32m+ [m
[32m+     public MakeCoffeeTask(CoffeeMachineAgent agent, long duration) {[m
[32m+         super(agent, "make-coffee-task", 1, false, false);[m
[32m+         this.endTime = System.currentTimeMillis() + duration;[m
[32m+     }[m
[32m+ [m
[32m+     @Override[m
[32m+     protected boolean execute() {[m
[32m+         if (endTime <= System.currentTimeMillis()) {[m
[32m+             agent.getLogger().info("Coffee has been made! Enjoy!");[m
[32m+             return true;[m
[32m+         }[m
[32m+ [m
[32m+         return false;[m
[32m+     }[m
[31m -}[m
[32m++}[m
[32m++>>>>>>> Stashed changes[m
[1mdiff --cc src/main/java/org/asaa/tasks/appliances/DishwasherAgent/WashDishesTask.java[m
[1mindex 14028b7,df937a9..0000000[m
[1m--- a/src/main/java/org/asaa/tasks/appliances/DishwasherAgent/WashDishesTask.java[m
[1m+++ b/src/main/java/org/asaa/tasks/appliances/DishwasherAgent/WashDishesTask.java[m
[36m@@@ -1,68 -1,59 +1,126 @@@[m
  package org.asaa.tasks.appliances.DishwasherAgent;[m
  [m
[32m++<<<<<<< Updated upstream[m
[32m +import jade.core.behaviours.TickerBehaviour;[m
[32m +import org.asaa.agents.appliances.DishwasherAgent;[m
[32m +import org.asaa.tasks.Task;[m
[32m +[m
[32m +public final class WashDishesTask extends Task {[m
[32m +    private final DishwasherAgent agent;[m
[32m +    private TickerBehaviour washBehaviour;[m
[32m +    private final long updateDelay;[m
[32m +    private final long noninterruptibleStartTime;[m
[32m +    private final long noninterruptibleEndTime;[m
[32m +    private long fullWashTime;[m
[32m +    private long remainingWashTime;[m
[32m +    private long washStartTime;[m
[32m +[m
[32m +    public WashDishesTask(DishwasherAgent agent, long updateDelay, double noninterruptibleStartPercent, double noninterruptibleEndPercent, long fullWashTime) {[m
[32m +        super(agent, true, false);[m
[32m +        this.agent = agent;[m
[32m +        this.updateDelay = updateDelay;[m
[32m +        this.noninterruptibleStartTime = (long)(noninterruptibleStartPercent * fullWashTime);[m
[32m +        this.noninterruptibleEndTime = (long)(noninterruptibleEndPercent * fullWashTime);[m
[32m +        this.fullWashTime = fullWashTime;[m
[32m +        this.remainingWashTime = fullWashTime;[m
[32m +    }[m
[32m +[m
[32m +    @Override[m
[32m +    protected void execute() {[m
[32m +        if (remainingWashTime <= 0) {[m
[32m +            agent.getLogger().info("Nothing to do: no remaining time");[m
[32m +            end(true);[m
[32m +        }[m
[32m +        washStartTime = System.currentTimeMillis();[m
[32m +        fullWashTime = remainingWashTime;[m
[32m +[m
[32m +        washBehaviour = new TickerBehaviour(agent, updateDelay) {[m
[32m +            @Override[m
[32m +            protected void onTick() {[m
[32m +                remainingWashTime = Math.max(0, fullWashTime - System.currentTimeMillis() + washStartTime);[m
[32m +//                agent.getLogger().info("Wash Dishes Task: {}ms remain", remainingWashTime);[m
[32m +                if (remainingWashTime <= 0) {[m
[32m +                    agent.getLogger().info("Wash complete!");[m
[32m +                    end(true);[m
[32m +                    agent.removeBehaviour(this);[m
[32m +                } else if (remainingWashTime <= noninterruptibleStartTime && remainingWashTime >= noninterruptibleEndTime && resumable) {[m
[32m +                    agent.getLogger().info("Wash Dishes Task entering an unpausable phase!");[m
[32m +                    resumable = false;[m
[32m +                } else if (remainingWashTime < noninterruptibleEndTime && !resumable) {[m
[32m +                    agent.getLogger().info("Dishwasher may be paused again");[m
[32m +                    resumable = true;[m
[32m +                }[m
[32m +            }[m
[32m +        };[m
[32m +        agent.addBehaviour(washBehaviour);[m
[32m +        agent.getLogger().info("Wash {} for {}ms", (remainingWashTime != fullWashTime ? "resumed" : "started"), remainingWashTime);[m
[32m +    }[m
[32m +[m
[32m +    @Override[m
[32m +    public void pause(boolean isCfpCall) {[m
[32m +        if (!paused && resumable && washBehaviour != null) {[m
[32m +            remainingWashTime = Math.max(0, fullWashTime - System.currentTimeMillis() + washStartTime);[m
[32m +            agent.removeBehaviour(washBehaviour);[m
[32m +            washBehaviour = null;[m
[32m +            agent.getLogger().info("Wash paused, {}ms left", remainingWashTime);[m
[32m +        }[m
[32m +        super.pause(isCfpCall);[m
[32m++=======[m
[32m+ import org.asaa.agents.appliances.DishwasherAgent;[m
[32m+ import org.asaa.behaviours.appliances.tasks.TaskBehaviour;[m
[32m+ [m
[32m+ public class WashDishesTask extends TaskBehaviour<DishwasherAgent> {[m
[32m+     private final long endTime;[m
[32m+     private final long nonResumableStartTime;[m
[32m+     private final long nonResumableEndTime;[m
[32m+ [m
[32m+     private final long duration;[m
[32m+     private final double nonResumableStartPercent;[m
[32m+     private final double nonResumableEndPercent;[m
[32m+ [m
[32m+     public WashDishesTask(DishwasherAgent agent, long duration, double nonResumableStartPercent, double nonResumableEndPercent) {[m
[32m+         super(agent, "wash-dishes-task", 1, true, false);[m
[32m+         this.endTime = System.currentTimeMillis() + duration;[m
[32m+         this.nonResumableStartTime = (long)(endTime * nonResumableStartPercent);[m
[32m+         this.nonResumableEndTime = (long)(endTime * nonResumableEndPercent);[m
[32m+         this.duration = duration;[m
[32m+         this.nonResumableStartPercent = nonResumableStartPercent;[m
[32m+         this.nonResumableEndPercent = nonResumableEndPercent;[m
[32m+     }[m
[32m+ [m
[32m+     private WashDishesTask(DishwasherAgent agent, int priority, long duration, double nonResumableStartPercent, double nonResumableEndPercent) {[m
[32m+         super(agent, "wash-dishes-task", priority, true, false);[m
[32m+         this.endTime = System.currentTimeMillis() + duration;[m
[32m+         this.nonResumableStartTime = (long)(endTime * nonResumableStartPercent);[m
[32m+         this.nonResumableEndTime = (long)(endTime * nonResumableEndPercent);[m
[32m+         this.duration = duration;[m
[32m+         this.nonResumableStartPercent = nonResumableStartPercent;[m
[32m+         this.nonResumableEndPercent = nonResumableEndPercent;[m
[32m+     }[m
[32m+ [m
[32m+     @Override[m
[32m+     protected TaskBehaviour<?> resumeWith(int priority) {[m
[32m+         return new WashDishesTask(agent, priority, duration, nonResumableStartPercent, nonResumableEndPercent);[m
[32m+     }[m
[32m+ [m
[32m+     @Override[m
[32m+     protected boolean execute() {[m
[32m+         long currentTime = System.currentTimeMillis();[m
[32m+ [m
[32m+         if (endTime <= currentTime) {[m
[32m+             agent.getLogger().info("Wash complete!");[m
[32m+             return true;[m
[32m+         }[m
[32m+ [m
[32m+         if (nonResumableStartTime <= currentTime && nonResumableEndTime >= currentTime && pausable) {[m
[32m+             agent.getLogger().info("Dishwasher can not be paused now for {}ms", nonResumableEndTime - currentTime);[m
[32m+             pausable = false;[m
[32m+         } else if (nonResumableEndTime <= currentTime && !pausable) {[m
[32m+             agent.getLogger().info("Dishwasher may be paused again");[m
[32m+             pausable = true;[m
[32m+         }[m
[32m+ [m
[32m+         return false;[m
[32m++>>>>>>> Stashed changes[m
      }[m
  }[m
* Unmerged path src/main/java/org/asaa/behaviours/appliances/TaskBehaviour.java
