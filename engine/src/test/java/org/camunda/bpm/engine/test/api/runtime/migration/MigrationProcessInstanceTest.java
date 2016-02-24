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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationProcessInstanceTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @Test
  public void testNotMigrateProcessInstanceOfWrongProcessDefinition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition wrongProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(wrongProcessDefinition.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.startsWith("ENGINE-23002"));
    }
  }

  @Test
  public void testNotMigrateUnknownProcessInstance() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan, Collections.singletonList("unknown"));
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.startsWith("ENGINE-23005"));
    }
  }

  @Test
  public void testNotMigrateNullProcessInstance() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan, Collections.<String>singletonList(null));
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance id is null"));
    }
  }

}
