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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SuiteParserTest extends MethodStartLoggerTestClass {
  List<String> featureFiles;

  @BeforeEach
  void initList() throws IOException {
    featureFiles = new ArrayList<>();
    TestHelper.addFeatureFiles("tiger-test-lib", featureFiles);
  }

  @Test
  void readMultipleFeatureFiles_OK()  {
    SuiteParser parser = new SuiteParser();
    parser.parseTestsuite(featureFiles);
    assertThat(parser.getFileFeatureMap()).hasSize(7);
  }

  @Test
  void readMultipleFeatureFilesWithInvalidFileInbetween_OK() {
    featureFiles.add(2, "testdata/tiger-test-lib/invalid_features/invalidtestcontext.feature");
    SuiteParser parser = new SuiteParser();
    parser.parseTestsuite(featureFiles);
    assertThat(parser.getFileFeatureMap()).hasSize(7);
  }
  @Test
  void readMultipleFeatureFilesWithDublette_NOK() {
    featureFiles.add("testdata/tiger-test-lib/invalid_features/HttpGlueCodeTest.feature");
    SuiteParser parser = new SuiteParser();
    assertThatThrownBy(() -> parser.parseTestsuite(featureFiles)).isInstanceOf(ParserException.class);
  }
}
