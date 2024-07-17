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

import static org.assertj.core.api.Assertions.*;

import de.gematik.test.tiger.validator.model.TestReport;
import de.gematik.test.tiger.validator.model.TestResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class ReportValidatorTest {

    List<String> featureFiles;

    @BeforeEach
    void initList() throws IOException {
        featureFiles = new ArrayList<>();
        TestHelper.addFeatureFiles("tiger-test-lib", featureFiles);
    }

    @Test
    void readAndValidateMultipleFilesWithFailure_EXC() throws IOException {
        SuiteParser parser = new SuiteParser();
        parser.parseTestsuite(featureFiles);
        assertThat(parser.getFileFeatureMap()).hasSize(7);

        TestReport report = new TestReport();
        report.parseReport("testdata/tiger-test-lib/report");

        ReportValidator validator = new ReportValidator(parser, report);
        runAndCheckException(validator, "not reported as successful");
    }

    @Test
    void readAndValidateMultipleFilesWithError_EXC() throws IOException {
        SuiteParser parser = new SuiteParser();
        parser.parseTestsuite(featureFiles);
        assertThat(parser.getFileFeatureMap()).hasSize(7);

        TestReport report = new TestReport();
        report.parseReport("testdata/tiger-test-lib/report");
        // remove the failure json report data of first optional scenario as first fails and second errors
        report.getScenarioResults().remove("HttpGlueCodeTestTest find last request");

        ReportValidator validator = new ReportValidator(parser, report);
        runAndCheckException(validator, "not reported as successful");
    }
    @Test
    void readAndValidateMultipleFilesWithSuccessNoOptionalFailedResults_OK() throws IOException {
        SuiteParser parser = new SuiteParser();
        parser.parseTestsuite(featureFiles);
        assertThat(parser.getFileFeatureMap()).hasSize(7);

        TestReport report = new TestReport();
        report.parseReport("testdata/tiger-test-lib/report");
        // remove the failure json report data of two optional scenarios as first fails and second errors
        // third optional has state SUCCESS, so we keep it to also check for optional testresults being included
        report.getScenarioResults().remove("HttpGlueCodeTestTest find last request");
        report.getScenarioResults().remove("HttpGlueCodeTestTest deactivate followRedirects");

        ReportValidator validator = new ReportValidator(parser, report);
        TestResult result = validator.validateReport();
        assertThat(result).isEqualByComparingTo(TestResult.SUCCESS);
        log.info("Report validated successfully");
    }

    @Test
    void readAndValidateMultipleFilesWithStepMismatch_EXC() throws IOException {
        SuiteParser parser = new SuiteParser();
        parser.parseTestsuite(featureFiles);
        assertThat(parser.getFileFeatureMap()).hasSize(7);

        TestReport report = new TestReport();
        report.parseReport("testdata/tiger-test-lib/report");
        // replace with "invalid" version where a step in the report mismatches
        report.getScenarioResults().remove("HttpGlueCodeTestGet Request with custom and default header, check body, application type url encoded");
        report.parseReport("testdata/tiger-test-lib/invalid_reports");

        ReportValidator validator = new ReportValidator(parser, report);
        runAndCheckException(validator, "mismatching step");
    }

    private void runAndCheckException(ReportValidator validator, String partOfMessage) {
        Throwable thrown = catchThrowable(validator::validateReport);
        log.info("Exception {}", thrown.getMessage());
        assertThat(thrown).isInstanceOf(ReportValidationException.class).hasMessageContaining(partOfMessage);
    }

}
