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

import java.text.MessageFormat;
import java.util.Map;

public class ReportValidationException extends RuntimeException {

  public enum MessageId {
    SCENARIO_NOT_OK,
    UNEXPECTED_STEP,
    STEP_NOT_OK,
    STEP_MISMATCH,
    EMPTY_SUITE,
    EMPTY_REPORT,
    MANDATORY_SCENARIO_NOT_FOUND,
    ERR_READ_ZIP,
    NO_VALID_BUNDLE_VERSION,
    BUNDLE_VERSION_MISMATCH,
    NO_BUNDLE_VERSION_FILE
  }

  public static final Map<ReportValidationException.MessageId, String> ERROR_MESSAGES =
      Map.ofEntries(
          Map.entry(
              MessageId.SCENARIO_NOT_OK,
              "Das Szenario ''{0}'' ist nicht als erfolgreich hinterlegt"),
          Map.entry(
              MessageId.UNEXPECTED_STEP,
              "Unerwarteter Testschritt im Szenario ''{0}'', Variante {1}\nErwartet: ''{2}''\nErhalten: ''{3}''"),
          Map.entry(
              MessageId.STEP_NOT_OK,
              "Der Testschritt ''{0}'' im Szenario ''{1}'' ist nicht als erfolgreich hinterlegt"),
          Map.entry(
              MessageId.STEP_MISMATCH,
              "Im Testbericht für das Szenario ''{0}'' sind die berichteten Schritte nicht identisch mit den erwarteten Schritten\nErwartet: ''{1}''\nErhalten: ''{2}''"),
          Map.entry(MessageId.EMPTY_SUITE, "Eine leere Testsuite ist ungültig!"),
          Map.entry(MessageId.EMPTY_REPORT, "Ein leerer Testbericht ist ungültig!"),
          Map.entry(
              MessageId.MANDATORY_SCENARIO_NOT_FOUND,
              "Das Szenario ''{0}'' ist als Pflichtszenario markiert, wurde aber nicht gefunden"),
          Map.entry(MessageId.ERR_READ_ZIP, "Der gezippte Testbericht konnte nicht gelesen werden"),
          Map.entry(
              MessageId.NO_VALID_BUNDLE_VERSION,
              "Keine lesbare Bundle Version im Testbericht gefunden"),
          Map.entry(
              MessageId.BUNDLE_VERSION_MISMATCH, "Ungültige Bundle Version ''{0}'' im Testbericht"),
          Map.entry(MessageId.NO_BUNDLE_VERSION_FILE, "Keine pom.xml Datei im Testbericht"));

  public ReportValidationException(MessageId msgId, Object... args) {
    super(MessageFormat.format(ERROR_MESSAGES.get(msgId), args));
  }

  public ReportValidationException(MessageId msgId, Throwable cause) {
    super(MessageFormat.format(ERROR_MESSAGES.get(msgId), (Object) null), cause);
  }
}
