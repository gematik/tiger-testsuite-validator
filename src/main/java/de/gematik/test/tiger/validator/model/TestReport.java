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
import io.cucumber.cienvironment.internal.com.eclipsesource.json.Json;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonObject;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** contains a map of all parsed json result file data */
@Getter
@Slf4j
public class TestReport {
  /**
   * Map containing all result data from parsed json files. Key of the map is concatenation of
   * feature file name (without path and extension) and scenario name
   */
  Map<String, ScenarioResult> scenarioResults = new HashMap<>();

  public void parseReportFromTitus(ZipInputStream reportStream) {
    try {
      ZipEntry entry;
      while ((entry = reportStream.getNextEntry()) != null) {
        if (entry.getName().endsWith(".json")) {
          log.info("Parsing zip report file '{}'", entry.getName());
          parseScenarioJsonResult(new String(reportStream.readAllBytes()));
        }
      }
    } catch (IOException ioe) {
      throw new ReportValidationException("Error reading zip file", ioe);
    }
  }

  public void parseReport(String folder) throws IOException {
    Files.walkFileTree(
        Paths.get(folder),
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
              throws IOException {
            if (path.toFile().getName().endsWith(".json")) {
              log.info("Parsing report file '{}'", path.toFile().getAbsolutePath());
              parseScenarioJsonResult(Files.readString(path));
            }
            return FileVisitResult.CONTINUE;
          }
        });
    log.info("Parsed {} scenario test results", scenarioResults.size());
  }

  public void parseScenarioJsonResult(String jsonContent) {
    JsonObject json = Json.parse(jsonContent).asObject();
      ScenarioResult scenario = ScenarioResult.createResult(json);
      scenarioResults.put(scenario.getFilename() + scenario.getScenarioName(), scenario);
  }
}
