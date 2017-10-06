package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

public class SmartShipGoodService extends PublishSubscribeAdapter {

    private static Integer count = 0;

    public void execute(final ActivityExecution context) throws Exception {

        addMessageSubscription(context, "Message_shipping");

        System.out.println("(back-end | ShipGoodAdapter) call (Supplier) micro service n° "+context.getVariable("loopCounter"));
    }
}
