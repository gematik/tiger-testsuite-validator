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

import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

/**
 * Parses all given feature files and creates a map of all parsed features with their filenames
 * (without the extension) as the key.
 * If any file is invalid, the parsing continues with the next file(s).
 * If there are two files detected with the same name will abort. See Readme.md
 */
@Getter
@Slf4j
public class SuiteParser {
  private final Map<String, Feature> fileFeatureMap = new HashMap<>();

  /**
   *
   * @param featureFiles mapo with absolute file path as key and content as value
   */
  public void parseTestsuiteFromTitus(Map<String, String> featureFiles) {
    featureFiles.forEach((filePath, content) -> {
      log.debug("Parsing file '{}'", filePath);
        parseFeatureFile(filePath, content);
    });
    log.info("Added {} features", fileFeatureMap.size());
  }

  public void parseTestsuite(List<String> featureFilePaths) {
    featureFilePaths.forEach(filePath -> {
      Path file = Paths.get(filePath);
      log.debug("Parsing file '{}'", file.toUri());
        try {
        parseFeatureFile(file.toUri().toString(), Files.readString(file));
      } catch (IOException ioe) {
        log.error("Parse error for file '{}'", file.toUri(), ioe);
      }
    });
    log.info("Added {} features", fileFeatureMap.size());
  }

  private void parseFeatureFile(String filePathUri, String content) {


    Feature dubletteFeature = null;
    try {
      final Envelope envelope =
          Envelope.of(
              new Source(
                  filePathUri, content, TEXT_X_CUCUMBER_GHERKIN_PLAIN));
      GherkinDocument gherkinDocument =
          GherkinParser.builder()
              .includeSource(false)
              .includePickles(false)
              .build()
              .parse(envelope)
              .findFirst()
              .orElseThrow(() -> new ParserException(ParserException.MessageId.NO_ENVELOPE, filePathUri))
              .getGherkinDocument()
              .orElseThrow(() -> new ParserException(ParserException.MessageId.NO_GHERKIN_DOC, filePathUri));


          dubletteFeature = fileFeatureMap.put(
              FilenameUtils.removeExtension(Paths.get(filePathUri).toFile().getName()),
              gherkinDocument
                  .getFeature()
                  .orElseThrow(() -> new ParserException(ParserException.MessageId.NO_FEATURE_PICKLE, filePathUri)));
          if (dubletteFeature == null) {
            log.info("Added Feature {}", gherkinDocument.getFeature().get().getName()); //NOSONAR
          }
    } catch (ParserException e) {
      log.error("Parse error for file '{}'", filePathUri, e);
    }
    if (dubletteFeature != null) {
      throw new ParserException(ParserException.MessageId.DUPLICATE_FILE, filePathUri);
    }
  }
}
