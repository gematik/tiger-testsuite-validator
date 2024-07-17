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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class TitusReportValidatorTest {

  Map<String, String> featureFilesMap = new HashMap<>();
  static final Path zipPath = Paths.get("testdata/titus/report.zip");

  @BeforeEach
  void packTestData() throws IOException {
    Path titusPath = Paths.get("testdata/titus");
    if (!Files.exists(titusPath)) {
      Files.createDirectory(titusPath);
    } else {
      if (Files.exists(zipPath)) {
        Files.delete(zipPath);
      }
    }
    ZipOutputStream archive = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
    List<String> jsonFiles = new ArrayList<>();
    TestHelper.addReportFiles("tiger-test-lib", jsonFiles);
    for (String jsonFile : jsonFiles) {
        Path jsonPath = Paths.get(jsonFile);
        archive.putNextEntry(new ZipEntry(jsonPath.getFileName().toString()));
      archive.write(Files.readAllBytes(jsonPath));
      archive.closeEntry();
    }
    archive.close();
  }

  @BeforeEach
  void initList() throws IOException {
    List<String> featureFiles = new ArrayList<>();
    TestHelper.addFeatureFiles("tiger-test-lib", featureFiles);
    featureFiles.forEach(
        file -> {
          try {
            featureFilesMap.put(file, Files.readString(Paths.get(file)));
          } catch (IOException e) {
            log.error("Error reading file '{}'", file, e);
          }
        });
  }

  void removeEntriesFromZipReport(String... entryPaths) throws IOException {
    Map<String, String> zip_properties = Map.of("create", "false");
    try (FileSystem zipfs = FileSystems.newFileSystem(zipPath, zip_properties)) {
      Arrays.stream(entryPaths)
          .toList()
          .forEach(
              entryPath -> {
                Path pathInZipfile = zipfs.getPath(entryPath);
                try {
                  Files.delete(pathInZipfile);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }

  void addEntryToZipReport(String filePath, String entryPath) throws IOException {
    Map<String, String> zip_properties = Map.of("create", "true");
    try (FileSystem zipfs = FileSystems.newFileSystem(zipPath, zip_properties)) {
      Path pathInZipfile = zipfs.getPath(entryPath);
      try {
          Files.delete(pathInZipfile);
          Files.copy(Paths.get(filePath), pathInZipfile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  void readAndValidateMultipleFilesWithFailure_EXC() {

    Throwable thrown =
        catchThrowable(
            () ->
                ReportValidator.parseTitusReport(
                    new ZipInputStream(
                        Files.newInputStream(Paths.get("testdata/titus/report.zip"))),
                    featureFilesMap));
    log.info("Exception {}", thrown.getMessage());
    assertThat(thrown)
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("not reported as successful");
  }

  @Test
  void readAndValidateMultipleFilesWithError_EXC() throws Exception {

    // remove the failure json report data of first optional scenario as first fails and second
    // errors
    // report.getScenarioResults().remove("HttpGlueCodeTestTest find last request");
    // 0ff6c45977f119a676e68067b247cea0c821cd7acd6d9b4607d5c6551323b423.json
    removeEntriesFromZipReport("0ff6c45977f119a676e68067b247cea0c821cd7acd6d9b4607d5c6551323b423.json");
    Throwable thrown =
        catchThrowable(
            () ->
                ReportValidator.parseTitusReport(
                    new ZipInputStream(
                        Files.newInputStream(Paths.get("testdata/titus/report.zip"))),
                    featureFilesMap));
    log.info("Exception {}", thrown.getMessage());
    assertThat(thrown)
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("not reported as successful");
  }

  @Test
  void readAndValidateMultipleFilesWithSuccessNoOptionalFailedResults_OK() throws IOException {

    // report.getScenarioResults().remove("HttpGlueCodeTestTest find last request");
    // 0ff6c45977f119a676e68067b247cea0c821cd7acd6d9b4607d5c6551323b423.json
    // report.getScenarioResults().remove("HttpGlueCodeTestTest deactivate followRedirects");
    // 74537195ff5a286cd8461687706738bf4001c48f00e90c7ab935c06747a63fba.json
    removeEntriesFromZipReport(
        "0ff6c45977f119a676e68067b247cea0c821cd7acd6d9b4607d5c6551323b423.json",
        "74537195ff5a286cd8461687706738bf4001c48f00e90c7ab935c06747a63fba.json");
    ReportValidator.parseTitusReport(
        new ZipInputStream(Files.newInputStream(Paths.get("testdata/titus/report.zip"))),
        featureFilesMap);
    log.info("Report validated successfully");
  }

  @Test
  void readAndValidateMultipleFilesWithStepMismatch_EXC() throws IOException {
    TestReport report = new TestReport();
    report.parseReport("testdata/tiger-test-lib/report");
    // replace with "invalid" version where a step in the report mismatches
    // report.getScenarioResults().remove("HttpGlueCodeTestGet Request with custom and default
    // header, check body, application type url encoded");
    addEntryToZipReport(
        "testdata/tiger-test-lib/invalid_reports/mismatchstep-0aae10f52620e93989108b4ffc08275c23bc333ce332df436639004736e2730d.json",
        "0aae10f52620e93989108b4ffc08275c23bc333ce332df436639004736e2730d.json");

      Throwable thrown =
              catchThrowable(
                      () ->
                              ReportValidator.parseTitusReport(
                                      new ZipInputStream(
                                              Files.newInputStream(Paths.get("testdata/titus/report.zip"))),
                                      featureFilesMap));
      log.info("Exception {}", thrown.getMessage());
      assertThat(thrown)
              .isInstanceOf(ReportValidationException.class)
              .hasMessageContaining("mismatching step");
  }
}