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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * Checks for all mandatory and optional scenarios of the given SuiteParser that there is a test
 * report result and asks the scenario result object to validate against the parsed scenario.
 */
@Slf4j
public class ReportValidator {

  public static final String OPT_GROUP_TOKEN = "@OptGroup:";
  private final SuiteParser suiteParser;
  private final TestReport testReport;

  private boolean anyScenarioChecked;

  public static void parseTitusReport(
      ZipInputStream reportStream, Map<String, String> featureFiles) {
    SuiteParser suiteParser = new SuiteParser();
    suiteParser.parseTestsuiteFromTitus(featureFiles);
    TestReport testReport = new TestReport();
    testReport.parseReportFromTitus(reportStream, Optional.empty());
    ReportValidator reportValidator = new ReportValidator(suiteParser, testReport);
    reportValidator.validateReport();
  }

  public static void parseTitusReport(
      ZipInputStream reportStream, Map<String, String> featureFiles, String version) {
    SuiteParser suiteParser = new SuiteParser();
    suiteParser.parseTestsuiteFromTitus(featureFiles);
    TestReport testReport = new TestReport();
    testReport.parseReportFromTitus(reportStream, Optional.of(version));
    ReportValidator reportValidator = new ReportValidator(suiteParser, testReport);
    reportValidator.validateReport();
  }

  public ReportValidator(SuiteParser suiteParser, TestReport report) {
    this.suiteParser = suiteParser;
    this.testReport = report;
    anyScenarioChecked = false;
  }

  public TestResult validateReport() {
    AtomicReference<TestResult> validationResult = new AtomicReference<>(TestResult.SUCCESS);
    if (suiteParser.getFileFeatureMap().isEmpty()) {
      throw new ReportValidationException(ReportValidationException.MessageId.EMPTY_SUITE);
    }

    List<String> optGroupsInReport = new ArrayList<>();
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
                      scenario -> {
                        Optional<ScenarioResult> result =
                            validateScenario(
                                filename, scenario, validationResult, backgroundOptional);
                        // first remember all optgroups that have a result in the report
                        if (result.isPresent() && isOptGroup(scenario)) {
                          scenario.getTags().stream()
                              .map(Tag::getName)
                              .filter(name -> name.startsWith(OPT_GROUP_TOKEN))
                              .map(name -> name.substring(OPT_GROUP_TOKEN.length()))
                              .filter(groupName -> !optGroupsInReport.contains(groupName))
                              .forEach(optGroupsInReport::add);
                        }
                      });
            });

    if (!anyScenarioChecked) {
      throw new ReportValidationException(ReportValidationException.MessageId.EMPTY_REPORT);
    }

    if (!optGroupsInReport.isEmpty() && validationResult.get() == TestResult.SUCCESS) {
      log.info("Optional groups have been executed! {}", optGroupsInReport);
      suiteParser
          .getFileFeatureMap()
          .forEach(
              (filename, feature) -> {
                log.info("Validating Feature {} for optional groups...", filename);
                Optional<Background> backgroundOptional =
                    feature.getChildren().stream()
                        .filter(child -> child.getBackground().isPresent())
                        .map(child -> child.getBackground().get())
                        .findFirst();

                feature.getChildren().stream()
                    .map(FeatureChild::getScenario)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(scenario -> isInOptGroup(scenario, optGroupsInReport))
                    .forEach(
                            // check all scenarios of all executed opt groups have a SUCCESS result
                        scenario ->
                            validateScenario(
                                    filename, scenario, validationResult, backgroundOptional)
                                .orElseThrow(
                                    () ->
                                        new ReportValidationException(
                                            ReportValidationException.MessageId
                                                .OPTGROUP_SCENARIO_NOT_FOUND,
                                            scenario.getName(),
                                            scenario.getTags().stream()
                                                .map(Tag::getName)
                                                .filter(name -> name.startsWith(OPT_GROUP_TOKEN))
                                                .toList())));
              });
    }
    return validationResult.get();
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private Optional<ScenarioResult> validateScenario(
      String filename,
      Scenario scenario,
      AtomicReference<TestResult> validationResult,
      Optional<Background> backgroundOptional) {
    if (validationResult.get() != TestResult.SUCCESS) {
      return Optional.empty();
    }
    ScenarioResult scenarioResult =
        testReport.getScenarioResults().get(filename + scenario.getName());
    if (scenarioResult == null) {
      if (isMandatory(scenario.getTags())) {
        throw new ReportValidationException(
            ReportValidationException.MessageId.MANDATORY_SCENARIO_NOT_FOUND, scenario.getName());
      }
      return Optional.empty();
    } else {
      try {
        scenarioResult.validateSteps(scenario, backgroundOptional);
        anyScenarioChecked = true;
        return Optional.of(scenarioResult);
      } catch (ReportValidationException rve) {
        validationResult.set(TestResult.FAILURE);
        throw rve;
      }
    }
  }

  public boolean isRelevant(List<Tag> tags) {
    return tags.stream()
        .map(Tag::getName)
        .anyMatch(
            name ->
                name.equals("@Optional")
                    || name.equals("@Mandatory")
                    || name.startsWith(OPT_GROUP_TOKEN));
  }

  public boolean isOptGroup(Scenario scenario) {
    return scenario.getTags().stream()
        .map(Tag::getName)
        .anyMatch(name -> name.startsWith(OPT_GROUP_TOKEN));
  }

  public boolean isInOptGroup(Scenario scenario, List<String> optGroups) {
    return scenario.getTags().stream()
        .map(Tag::getName)
        .filter(name -> name.startsWith(OPT_GROUP_TOKEN))
        .map(name -> name.substring(OPT_GROUP_TOKEN.length()))
        .anyMatch(optGroups::contains);
  }

  public boolean isMandatory(List<Tag> tags) {
    return tags.stream().map(Tag::getName).anyMatch(name -> name.equals("@Mandatory"));
  }
}
