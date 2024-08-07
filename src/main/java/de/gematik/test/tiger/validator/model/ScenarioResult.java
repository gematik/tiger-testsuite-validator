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

import de.gematik.test.tiger.validator.ParserException;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonArray;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonObject;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Scenario;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;

/** base class for scenario / scenario outline as read from a test report JSON file */
@Getter
public abstract class ScenarioResult {
  protected String filename;
  protected String scenarioName;
  protected TestResult overallResult;
  protected final JsonArray steps;

  static final String TEST_STEPS = "testSteps";

  protected ScenarioResult(JsonObject json) {
    JsonArray pathElements = json.get("userStory").asObject().get("pathElements").asArray();
    filename =
        pathElements
            .get(pathElements.size() - 1)
            .asObject()
            .getString("name", "undefined path element");

    scenarioName = json.getString("name", "undefined scenario name");
    overallResult = TestResult.valueOf(json.getString("result", "FAILURE"));
    steps = json.get(TEST_STEPS).asArray();
  }

  public static ScenarioResult createResult(JsonObject json, Path jsonPath) {
    if (json.get(TEST_STEPS) == null) {
      throw new ParserException(
          ParserException.MessageId.STEPLIST_EMPTY,
          json.getString("name", "undefined result"),
          jsonPath);
    }
    JsonArray steps = json.get(TEST_STEPS).asArray();
    if (steps.isEmpty()) {
      throw new ParserException(
          ParserException.MessageId.STEPLIST_EMPTY,
          json.getString("name", "undefined result"),
          jsonPath);
    }
    if (steps.get(0).asObject().get("children") == null) {
      return new SimpleScenarioResult(json);
    } else {
      return new ScenarioOutlineResult(json);
    }
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public abstract void validateSteps(Scenario scenario, Optional<Background> backgroundOptional);

  protected boolean checkTestResult(JsonObject json) {
    return json.get("result").asString().equals(TestResult.SUCCESS.name());
  }
}
