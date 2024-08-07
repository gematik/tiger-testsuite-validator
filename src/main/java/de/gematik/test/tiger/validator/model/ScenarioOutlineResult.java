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
import io.cucumber.messages.types.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScenarioOutlineResult extends ScenarioResult {
  public ScenarioOutlineResult(JsonObject json) {
    super(json);
  }

  @Override
  public void validateSteps(Scenario scenario, Optional<Background> backgroundOptional) {
    log.debug("Validating Scenario outline {}", scenario.getName());

    Optional<TableRow> headersOptional = scenario.getExamples().get(0).getTableHeader();

    AtomicInteger rowCtr = new AtomicInteger();
    // iterate over all rows of all examples
    scenario.getExamples().stream()
        .flatMap(examples -> examples.getTableBody().stream())
        .forEach(
            tableRow -> {
              JsonObject variantStep = steps.get(rowCtr.get()).asObject();

              // check variant result
              TestResult result =
                  TestResult.valueOf(variantStep.getString("result", "undefined result"));
              if (result != TestResult.SUCCESS) {
                throw new ReportValidationException(
                    ReportValidationException.MessageId.SCENARIO_NOT_OK, scenario.getName());
              }

              // get all expected steps, replace all parameters <%s> in text and
              // check all of them to match testSteps[rowCtr].children

              List<Step> expectedSteps = new ArrayList<>();
              backgroundOptional.ifPresent(
                  background -> expectedSteps.addAll(background.getSteps()));
              expectedSteps.addAll(scenario.getSteps());

              AtomicInteger stepCtr = new AtomicInteger();
              expectedSteps.forEach(
                  step -> {
                    validateStep(
                        scenario,
                        getStepTextAndReplaceParamTokens(tableRow, step, headersOptional),
                        variantStep,
                        stepCtr.get(),
                        rowCtr.get());
                    stepCtr.getAndIncrement();
                  });

              rowCtr.getAndIncrement();
            });
  }

  private static void validateStep(
      Scenario scenario,
      String stepText,
      JsonObject variantStep,
      int stepCtr,
      int rowCtr) {

    String reportedStep =
        variantStep
            .get("children")
            .asArray()
            .get(stepCtr)
            .asObject()
            .getString("description", "undefined step description");

    if (!reportedStep.equals(stepText)) {
      throw new ReportValidationException(
          ReportValidationException.MessageId.UNEXPECTED_STEP,
          scenario.getName(),
          rowCtr,
          stepText,
          reportedStep);
    }
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static String getStepTextAndReplaceParamTokens(
      TableRow tableRow, Step step, Optional<TableRow> headersOptional) {
    String stepText = ScenarioHelper.getStepDescription(step);

    if (headersOptional.isPresent()) {
      List<TableCell> cells = headersOptional.get().getCells();
      for (int headerIdx = 0; headerIdx < cells.size(); headerIdx++) {
        stepText =
            stepText.replace(
                "<" + cells.get(headerIdx).getValue() + ">",
                tableRow.getCells().get(headerIdx).getValue());
      }
    }
    return stepText;
  }
}
