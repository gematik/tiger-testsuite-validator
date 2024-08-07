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
    MANDATORY_SCENARIO_NOT_FOUND,
    ERR_READ_ZIP
  }

    public static final Map<ReportValidationException.MessageId, String> ERROR_MESSAGES =
      Map.of(
          MessageId.SCENARIO_NOT_OK,
          "Das Szenario ''{0}'' ist nicht als erfolgreich hinterlegt",
          MessageId.UNEXPECTED_STEP,
          "Unerwarteter Testschritt im Szenario ''{0}'', Variante {1}\nErwartet: ''{2}''\nErhalten: ''{3}''",
          MessageId.STEP_NOT_OK,
          "Der Testschritt ''{0}'' im Szenario ''{1}'' ist nicht als erfolgreich hinterlegt",
          MessageId.STEP_MISMATCH,
          "Im Testbericht für das Szenario ''{0}'' sind die berichteten Schritte nicht identisch mit den erwarteten Schritten\nErwartet: ''{1}''\nErhalten: ''{2}''",
          MessageId.EMPTY_SUITE,
          "Eine leere Testsuite ist ungültig!",
          MessageId.MANDATORY_SCENARIO_NOT_FOUND,
          "Das Szenario ''{0}'' ist als Pflichtszenario markiert, wurde aber nicht gefunden",
          MessageId.ERR_READ_ZIP,
          "Der gezippte Testbericht konnte nicht gelesen werden");

  public ReportValidationException(MessageId msgId, Object... args) {
    super(MessageFormat.format(ERROR_MESSAGES.get(msgId), args));
  }

  public ReportValidationException(MessageId msgId, Throwable cause) {
    super(MessageFormat.format(ERROR_MESSAGES.get(msgId), (Object) null), cause);
  }
}
