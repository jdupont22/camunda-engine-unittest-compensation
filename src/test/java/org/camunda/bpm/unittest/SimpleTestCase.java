/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() {
    // Given we create a new process instance
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess");

    List<Execution> messagesShipping = executionQuery().messageEventSubscriptionName("Message_shipping").list();
    int count = 0;
    while (!messagesShipping.isEmpty()) {
      Map<String, Object> variables = new HashMap<String, Object>();
      if (count++ == 12) {
        System.out.println("launch ship good compensation");
        variables.put("success", false);
      } else {
        variables.put("success", true);
      }

      runtimeService().messageEventReceived("Message_shipping", messagesShipping.get(0).getId(), variables);
      messagesShipping = executionQuery().messageEventSubscriptionName("Message_shipping").list();
    }
  }

  @Test
  @Deployment(resources = {"testProcess2.bpmn"})
  public void shouldExecuteProcess2() {
    // Given we create a new process instance
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess2");

    List<Execution> messagesShipping = executionQuery().processInstanceId(processInstance.getId()).messageEventSubscriptionName("Message_shipping").active().list();
    int count = 0;
    while (!messagesShipping.isEmpty()) {
      Map<String, Object> variables = new HashMap<String, Object>();
      if (count++ == 25) {
        System.out.println("launch ship good compensation");
        variables.put("success", false);
      } else {
        variables.put("success", true);
      }

      runtimeService().messageEventReceived("Message_shipping", messagesShipping.get(0).getId(), variables);
      messagesShipping = executionQuery().messageEventSubscriptionName("Message_shipping").list();
    }
  }

  @Test
  @Deployment(resources = {"testProcess3.bpmn"})
  public void shouldExecuteProcess3() {
    // Given we create a new process instance
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess3");

    List<Execution> messagesShipping = getExecutions(processInstance);
    for (int i = 0; i < messagesShipping.size(); i++) {
      Map<String, Object> variables = new HashMap<String, Object>();
//      if (count++ == 2) {
//        MessageCorrelationResult compensateAll = runtimeService().createMessageCorrelation("CompensateAll").processInstanceId(processInstance.getId()).correlateWithResult();
//        System.out.println("==== "+compensateAll.getResultType().name());
//      } else {
//        runtimeService().messageEventReceived("Message_shipping", messagesShipping.get(0).getId(), variables);
//      }
      runtimeService().messageEventReceived("Message_shipping", messagesShipping.get(i).getId(), variables);

      messagesShipping = getExecutions(processInstance);
    }

    System.out.println("--=> start compensation...");
    MessageCorrelationResult compensateAll = runtimeService().createMessageCorrelation("CompensateAll").processInstanceId(processInstance.getId()).correlateWithResult();
    System.out.println("--=> "+compensateAll.getResultType().name());
  }

  @Test
  @Deployment(resources = {"testProcess4.bpmn"})
  public void shouldExecuteProcess4() {
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess4");

    List<Execution> messagesShipping = getExecutions(processInstance); // cardinality is 5 for the subprocess
    for (int i = 0; i < messagesShipping.size(); i++) {
      Map<String, Object> variables = new HashMap<String, Object>();
      if (i == 3) { // set i == 30 to avoid sub process signal and go to the global compensation
        System.out.println("-------------------");
        System.out.println("(back-end) send step 2 ship good compensation !!!!");
        System.out.println("-------------------");
        variables.put("success", false);
      } else {
        variables.put("success", true);
      }
      // compensate received message added programmatically by SmartShipGoodService
      // some message sending can failed due to sub process signal launch and the compensation already append
      runtimeService().messageEventReceived("Message_shipping", messagesShipping.get(i).getId(), variables);
    }
  }

  private List<Execution> getExecutions(ProcessInstance processInstance) {
    return executionQuery().processInstanceId(processInstance.getId()).messageEventSubscriptionName("Message_shipping").list();
  }
}
