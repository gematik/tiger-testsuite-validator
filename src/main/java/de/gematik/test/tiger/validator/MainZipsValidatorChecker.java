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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainZipsValidatorChecker {

  static Map<String, String> featureFilesMap = new HashMap<>();

  /**
   *
   * @param args path to bundle zip and path to report zip
   * @throws IOException if reading from zip archives fails
   */
  public static void main(String... args) throws IOException {
    if (args.length != 2) {
      throw new IllegalArgumentException("You need to supply two paths, to bundle zip (0) and to report zip (1)");
    }
    ZipInputStream bundleInputStream = new ZipInputStream(Files.newInputStream(Paths.get(args[0])));

    ZipEntry entry;
    while ((entry = bundleInputStream.getNextEntry()) != null) {
      if (entry.getName().endsWith(".feature")) {
        featureFilesMap.put(entry.getName(), new String(bundleInputStream.readAllBytes(), StandardCharsets.UTF_8));
      }
    }
    ZipInputStream reportStream = new ZipInputStream(Files.newInputStream(Paths.get(args[1])));
    ReportValidator.parseTitusReport(reportStream, featureFilesMap);
  }
}
