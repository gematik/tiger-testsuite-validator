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

package de.gematik.test.tiger.validator;

import de.gematik.test.tiger.validator.model.ScenarioResult;
import de.gematik.test.tiger.validator.model.TestReport;
import de.gematik.test.tiger.validator.model.TestResult;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipInputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * Checks for all mandatory and optional scenarios of the given SuiteParser that there is a test
 * report result and asks the scenario result object to validate against the parsed scenario.
 */
@Slf4j
public class ReportValidator {

  private final SuiteParser suiteParser;
  private final TestReport testReport;

  public static void parseTitusReport(ZipInputStream reportStream, Map<String, String> featureFiles) {
    SuiteParser suiteParser = new SuiteParser();
    suiteParser.parseTestsuiteFromTitus(featureFiles);
    TestReport testReport = new TestReport();
    testReport.parseReportFromTitus(reportStream);
    ReportValidator reportValidator = new ReportValidator(suiteParser, testReport);
    reportValidator.validateReport();
  }

  public ReportValidator(SuiteParser suiteParser, TestReport report) {
    this.suiteParser = suiteParser;
    this.testReport = report;
  }

  public TestResult validateReport() {
    AtomicReference<TestResult> validationResult = new AtomicReference<>(TestResult.SUCCESS);
    if (suiteParser.getFileFeatureMap().isEmpty()) {
      throw new ReportValidationException("Empty testsuite is invalid!");
    }
    suiteParser
        .getFileFeatureMap()
        .forEach(
            (filename, feature) -> {
              log.info("Validating Feature {}", filename);
              boolean mandatoryFeature = isMandatory(feature.getTags());

              Optional<Background> backgroundOptional =
                  feature.getChildren().stream()
                      .filter(child -> child.getBackground().isPresent())
                      .map(child -> child.getBackground().get())
                      .findFirst();

              feature.getChildren().stream()
                  .map(FeatureChild::getScenario)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .filter(scenario -> mandatoryFeature || isRelevant(scenario.getTags()))
                  .forEach(
                      scenario ->
                          validateScenario(
                              filename, scenario, validationResult, backgroundOptional));
            });
    return validationResult.get();
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private void validateScenario(
      String filename,
      Scenario scenario,
      AtomicReference<TestResult> validationResult,
      Optional<Background> backgroundOptional) {
    if (validationResult.get() != TestResult.SUCCESS) {
      return;
    }
    ScenarioResult scenarioResult =
        testReport.getScenarioResults().get(filename + scenario.getName());
    if (scenarioResult == null) {
      if (isMandatory(scenario.getTags())) {
        throw new ReportValidationException(
            "Mandatory Scenario '" + scenario.getName() + "' missing in report!");
      }
    } else {
      scenarioResult.validateSteps(scenario, backgroundOptional);
    }
  }

  public boolean isRelevant(List<Tag> tags) {
    return tags.stream()
        .map(Tag::getName)
        .anyMatch(name -> name.equals("@Optional") || name.equals("@Mandatory"));
  }

  public boolean isMandatory(List<Tag> tags) {
    return tags.stream().map(Tag::getName).anyMatch(name -> name.equals("@Mandatory"));
  }
}
