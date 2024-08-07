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
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** contains a map of all parsed json result file data */
@Getter
@Slf4j
public class TestReport {
  /**
   * Map containing all result data from parsed JSON files.
   * Key of the map is a concatenation of feature file name (without path and extension) and scenario name
   */
  Map<String, ScenarioResult> scenarioResults = new HashMap<>();

  public void parseReportFromTitus(ZipInputStream reportStream) {
    try {
      ZipEntry entry;
      while ((entry = reportStream.getNextEntry()) != null) { //NOSONAR - security checks are done on titus side
        if (entry.getName().endsWith(".json")
            && !(entry.getName().startsWith("bootstrap") || entry.getName().startsWith("nivo"))) {
          log.info("Parsing zip report file '{}'", entry.getName());
          parseScenarioJsonResult(new String(reportStream.readAllBytes()), entry.getName());
        }
      }
    } catch (IOException ioe) {
      throw new ReportValidationException(ReportValidationException.MessageId.ERR_READ_ZIP, ioe);
    }
  }

  /**
   * Parses all JSON files in the given folder (NO RECURSION) and adds the result data to the {@link
   * #scenarioResults} map.
   *
   * @param folder path to the folder containing the JSON files
   * @throws IOException if the folder does not exist or is not readable
   */
  public void parseReport(String folder) throws IOException {
    File f = Paths.get(folder).toFile();
    if (!f.exists() || !f.isDirectory()) {
      throw new IOException("Folder '" + folder + "' does not exist or is not a directory");
    }
    for (File file : Objects.requireNonNull(f.listFiles())) {
      if (file.getName().endsWith(".json")) {
        log.info("Parsing report file '{}'", file.getAbsolutePath());
        parseScenarioJsonResult(Paths.get(file.toURI()));
      }
    }
    log.info("Parsed {} scenario test results", scenarioResults.size());
  }

  public void parseScenarioJsonResult(Path jsonPath) throws IOException {
    JsonObject json = Json.parse(Files.readString(jsonPath)).asObject();
    ScenarioResult scenario = ScenarioResult.createResult(json, jsonPath);
    scenarioResults.put(scenario.getFilename() + scenario.getScenarioName(), scenario);
  }

  public void parseScenarioJsonResult(String json, String entryName) {
    ScenarioResult scenario =
        ScenarioResult.createResult(Json.parse(json).asObject(), Path.of("ZIP Archiv", entryName));
    scenarioResults.put(scenario.getFilename() + scenario.getScenarioName(), scenario);
  }
}
