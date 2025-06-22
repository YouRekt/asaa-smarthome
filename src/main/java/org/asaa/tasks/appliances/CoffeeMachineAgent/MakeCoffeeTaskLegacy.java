//package org.asaa.tasks.appliances.CoffeeMachineAgent;
//
//import jade.core.behaviours.WakerBehaviour;
//import org.asaa.agents.appliances.CoffeeMachineAgent;
//import org.asaa.tasks.Task;
//
//public final class MakeCoffeeTask extends Task {
//    private final CoffeeMachineAgent agent;
//    private final long duration = 10000;
//
//    public MakeCoffeeTask(CoffeeMachineAgent agent) {
//        super(agent, false, false);
//
//        this.agent = agent;
//    }
//
//    @Override
//    protected void execute() {
//        agent.getLogger().info("Making coffee");
//        agent.addBehaviour(new WakerBehaviour(agent, duration) {
//            @Override
//            protected void onWake() {
//                agent.getLogger().info("Coffee made! Enjoy");
//                end(true);
//            }
//        });
//    }
//}
