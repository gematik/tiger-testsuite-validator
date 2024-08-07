/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.tiger.validator.model;

import de.gematik.test.tiger.validator.ReportValidationException;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonObject;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleScenarioResult extends ScenarioResult {


  public SimpleScenarioResult(JsonObject json) {
    super(json);
  }

  @Override
  public void validateSteps(Scenario scenario, Optional<Background> backgroundOptional) {
    log.debug("Validating Scenario {}", scenario.getName());

    if (overallResult != TestResult.SUCCESS) {
      throw new ReportValidationException(
          ReportValidationException.MessageId.SCENARIO_NOT_OK, scenario.getName());
    }
    steps.forEach(
        step -> {
          if (!step.asObject().getString("result", "UNKNOWN").equals("SUCCESS")) {
            throw new ReportValidationException(
                ReportValidationException.MessageId.STEP_NOT_OK,
                step.asObject().getString("description", "undefined step"),
                scenario.getName());
          }
        });

    AtomicInteger stepIndex = new AtomicInteger();

    List<Step> expectedSteps = new ArrayList<>();
    backgroundOptional.ifPresent(background -> expectedSteps.addAll(background.getSteps()));
    expectedSteps.addAll(scenario.getSteps());

    expectedSteps.forEach(
        step -> {
          String reportedStep = steps.get(stepIndex.get()).asObject().getString("description", "");

          if (!checkTestResult(steps.get(stepIndex.get()).asObject())) {
            throw new ReportValidationException(
                ReportValidationException.MessageId.STEP_NOT_OK, reportedStep, scenario.getName());
          }

          if (!ScenarioHelper.getStepDescription(step).equals(reportedStep)) {
            throw new ReportValidationException(
                ReportValidationException.MessageId.STEP_MISMATCH,
                scenario.getName(),
                ScenarioHelper.getStepDescription(step),
                reportedStep);
          }
          stepIndex.getAndIncrement();
        });
  }
}
