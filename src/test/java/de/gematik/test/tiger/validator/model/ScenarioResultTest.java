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
import io.cucumber.cienvironment.internal.com.eclipsesource.json.Json;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonObject;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioResultTest {

    JsonObject jsonObject;
    Path path = Path.of("dummypath");

    @BeforeEach
    void initData() {
        jsonObject =
                Json.parse(
                        """
            {
                "name": "Test scenario",
                "result": "SUCCESS",
                "userStory": {
                    "pathElements": [
                    {
                        "name": "pathElement"
                    }
                    ]
                },
                "testSteps": [
                    {
                        "name": "Test step",
                        "result": "SUCCESS",
                        "children": []
                    }
                ]
            }
        """).asObject();
    }
    @Test
    void testScenarioResultWithStepsAndChildren_OK() {
        assertThat(ScenarioResult.createResult(jsonObject, Path.of("dummypath"))).isInstanceOf(ScenarioOutlineResult.class);
    }
    @Test
    void testSimpleScenarioResultWithSteps_OK() {
        jsonObject.get(ScenarioResult.TEST_STEPS).asArray().get(0).asObject().remove("children");
        assertThat(ScenarioResult.createResult(jsonObject, Path.of("dummypath"))).isInstanceOf(SimpleScenarioResult.class);
    }

    @Test
    void testScenarioResultWithStepsNull_NOK() {
        jsonObject.remove(ScenarioResult.TEST_STEPS);
        assertThatThrownBy(() -> ScenarioResult.createResult(jsonObject, path)).isInstanceOf(ParserException.class);
    }
    @Test
    void testScenarioResultWithStepsEmpty_NOK() {
        jsonObject.get(ScenarioResult.TEST_STEPS).asArray().remove(0);
        assertThatThrownBy(() -> ScenarioResult.createResult(jsonObject, path)).isInstanceOf(ParserException.class);
    }

}
