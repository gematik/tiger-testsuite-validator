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

import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import io.cucumber.junit.platform.engine.Constants;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Testing is done with four feature files:
 * 01: contains mainly Test1 optgroup scenarios and an unused opt group Test2
 * 02: contains failed test for Test1
 * 03: contains Test3 scenarios and one scenario being in BOTH groups Test1 and Test3
 * 04: contains failed test for Test3
 * <p>
 * The failed are in extra feature files as the parser does not "respect" and filter tags from the execution but will always parse all scenarios.
 * Thus, if I filter "and not @Failed" in the execution, the validator barks at a missing scenario from opt group (the failed one).
 * So moving those to extra feature files allows us to remove those from the list for the validator and perform OK tests.
 * This is a workaround for testing. In real life tests of an opt group that fail will cause a validation failure and is desired ;)
 */
class OptGroupDummyTest {

  static Map<String, String> featureFilesMap = new HashMap<>();
  static File serenityFolder = Paths.get("target", "site", "serenity").toFile();

  @BeforeEach
  void createFeatureBundle() {
    List<String> featureFiles = new ArrayList<>();
    try {
      Files.walkFileTree(
          Paths.get("src", "test", "resources", "features"),
          new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult visitFile(Path path, @NotNull BasicFileAttributes basicFileAttributes) {
              if (path.toFile().getName().endsWith(".feature")) {
                featureFiles.add(path.toFile().getAbsolutePath());
              }
              return FileVisitResult.CONTINUE;
            }
          });
      featureFiles.forEach(
          file -> {
            try {
              featureFilesMap.put(file, Files.readString(Paths.get(file)));
            } catch (IOException e) {
              throw new RuntimeException("Error reading file '" + file + "'", e);
            }
          });

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterAll
  static void cleanUp() {
    try {
      FileUtils.deleteDirectory(serenityFolder);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
      //noinspection ResultOfMethodCallIgnored
      Paths.get("target", "testReport.zip").toFile().delete();
  }

  @Test
  @SneakyThrows
  void testOptGroup1_OK() {
    removeFeatureFile("02"); // removed the failed scenario
    removeFeatureFile("03"); // removed the scenario that is in Test1 AND Test3
    TestExecutionSummary summary = performTest("@OptGroup:Test1 and not @Failed");
    assertThat(summary.getFailures()).isEmpty();

    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    ReportValidator.parseTitusReport(reportStream, featureFilesMap);
  }

  @Test
  @SneakyThrows
  void testOptGroup1_oneFailed_NOK() {
    TestExecutionSummary summary = performTest("@OptGroup:Test1");
    assertThat(summary.getFailures()).hasSize(1);

    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    assertThatThrownBy(() -> ReportValidator.parseTitusReport(reportStream, featureFilesMap))
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("scenario2-01")
        .hasMessageContaining("nicht als erfolgreich hinterlegt");
  }

  @Test
  @SneakyThrows
  void testOptGroup1_missing_NOK() {
    removeFeatureFile("02"); // removed the failed scenario
    performTest("@OptGroup:Test1 and not @SkipExecForTest3 and not @Failed");

    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    assertThatThrownBy(() -> ReportValidator.parseTitusReport(reportStream, featureFilesMap))
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("scenario1-03")
        .hasMessageContaining("[@OptGroup:Test1]")
        .hasMessageContaining("wurde aber nicht gefunden");
  }

  @Test
  @SneakyThrows
  void testOptGroup1n3_OK() {
    removeFeatureFile("02"); // removed the failed scenario
    removeFeatureFile("04"); // removed the failed scenario
    performTest("(@OptGroup:Test1 or @OptGroup:Test3)");
    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    ReportValidator.parseTitusReport(reportStream, featureFilesMap);
  }

  @Test
  @SneakyThrows
  void testOptGroup1n3_missing3_NOK() {
    removeFeatureFile("02"); // removed the failed scenario
    removeFeatureFile("04"); // removed the failed scenario
    performTest("(@OptGroup:Test1 or @OptGroup:Test3) and not @SkipExecForTest4");
    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    assertThatThrownBy(() -> ReportValidator.parseTitusReport(reportStream, featureFilesMap))
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("scenario3-02")
        .hasMessageContaining("[@OptGroup:Test3]")
        .hasMessageContaining("wurde aber nicht gefunden");
  }

  @Test
  @SneakyThrows
  void testOptGroup1n3_missing1_NOK() {
    removeFeatureFile("02"); // removed the failed scenario
    removeFeatureFile("04"); // removed the failed scenario
    performTest("(@OptGroup:Test1 or @OptGroup:Test3) and not @SkipExecForTest3");
    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    assertThatThrownBy(() -> ReportValidator.parseTitusReport(reportStream, featureFilesMap))
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("scenario1-03")
        .hasMessageContaining("[@OptGroup:Test1]")
        .hasMessageContaining("wurde aber nicht gefunden");
  }

  @Test
  @SneakyThrows
  void testOptGroup1n3_oneFailed3_NOK() {
    removeFeatureFile("02"); // removed the failed scenario for Test1
    TestExecutionSummary summary = performTest("@OptGroup:Test1 or @OptGroup:Test3");
    assertThat(summary.getFailures()).hasSize(2);

    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    assertThatThrownBy(() -> ReportValidator.parseTitusReport(reportStream, featureFilesMap))
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("scenario4-01")
        .hasMessageContaining("nicht als erfolgreich hinterlegt");
  }

  @Test
  @SneakyThrows
  void testOptGroup1n3_oneFailed1_NOK() {
    removeFeatureFile("04"); // removed the failed scenario for Test3
    TestExecutionSummary summary = performTest("@OptGroup:Test1 or @OptGroup:Test3");
    assertThat(summary.getFailures()).hasSize(2);

    var reportStream =
        new ZipInputStream(Files.newInputStream(Paths.get("target", "testReport.zip")));
    assertThatThrownBy(() -> ReportValidator.parseTitusReport(reportStream, featureFilesMap))
        .isInstanceOf(ReportValidationException.class)
        .hasMessageContaining("scenario2-01")
        .hasMessageContaining("nicht als erfolgreich hinterlegt");
  }

  private void removeFeatureFile(String index) {
    featureFilesMap.remove(
        Paths.get("src", "test", "resources", "features").toFile().getAbsolutePath()
            + "/TestOptGroup"
            + index
            + ".feature");
  }

  private TestExecutionSummary performTest(String tagFilter) {
    cleanUp();
    Launcher launcher = LauncherFactory.create();
    final SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
    LauncherDiscoveryRequest request =
        request()
            .selectors(DiscoverySelectors.selectClasspathResource("/features"))
            .configurationParameter(GLUE_PROPERTY_NAME, "de.gematik.test.tiger.validator.glue")
            .configurationParameter(
                Constants.PLUGIN_PROPERTY_NAME, "io.cucumber.core.plugin.SerenityReporterParallel")
            .configurationParameter(FILTER_TAGS_PROPERTY_NAME, tagFilter)
            .build();
    launcher.execute(request, summaryListener);

    pack(serenityFolder.toPath(), "target/testReport.zip", ".json");
    return summaryListener.getSummary();
  }

  private void pack(Path sourceDirPath, String zipFilePath, String extension) {
    try {
      Path p = Files.createFile(Paths.get(zipFilePath));
      try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p));
          Stream<Path> paths = Files.walk(sourceDirPath)) {
        paths
            .filter(path -> !Files.isDirectory(path))
            .filter(path -> path.toString().endsWith(extension))
            .forEach(
                path -> {
                  ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                  try {
                    zs.putNextEntry(zipEntry);
                    Files.copy(path, zs);
                    zs.closeEntry();
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                });
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
