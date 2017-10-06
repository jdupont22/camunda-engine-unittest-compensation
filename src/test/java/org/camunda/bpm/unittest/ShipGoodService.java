package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ShipGoodService implements JavaDelegate {

    private static Integer count = 0;

    public void execute(final DelegateExecution delegateExecution) throws Exception {
        System.out.println("call shipping (Supplier) micro service");
    }
}
