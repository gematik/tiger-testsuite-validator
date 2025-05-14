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

import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Step;

import java.util.Optional;

public class ScenarioHelper {

  private ScenarioHelper() {}

  public static String getStepDescription(Step step) {
    String expectedStep = step.getKeyword() + step.getText();
    Optional<DataTable> dataTableOpt = step.getDataTable();
    if (dataTableOpt.isPresent()) {
        StringBuilder dataTableStr = new StringBuilder();
      dataTableOpt
          .get()
          .getRows()
          .forEach(
              row -> {
                dataTableStr.append("\n");
                row.getCells()
                    .forEach(cell -> dataTableStr.append("| ").append(cell.getValue()).append(" "));
                dataTableStr.append("|");
              });
      expectedStep = expectedStep + dataTableStr;
    }
    if (step.getDocString().isPresent()) {
      expectedStep = expectedStep + "\n" + step.getDocString().get().getContent();
    }
    return expectedStep.replace("\r\n", "\n");
  }
}
