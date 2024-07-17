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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class TestHelper {
    public static void addFeatureFiles(String project, final List<String> featureFiles) throws IOException {
        Files.walkFileTree(
                Paths.get("testdata"+ File.separator + project + File.separator+ "features"),
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
                        if (path.toFile().getName().endsWith(".feature")) {
                            featureFiles.add(path.toFile().getAbsolutePath());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
    }
    public static void addReportFiles(String project, final List<String> jsonFiles) throws IOException {
        Files.walkFileTree(
                Paths.get("testdata"+File.separator+ project + File.separator + "report"),
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
                        if (path.toFile().getName().endsWith(".json")) {
                            jsonFiles.add(path.toFile().getAbsolutePath());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
    }
}
