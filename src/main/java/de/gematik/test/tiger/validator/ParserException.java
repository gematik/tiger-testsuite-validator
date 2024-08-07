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

public class ParserException extends RuntimeException {

  public enum MessageId {
    NO_ENVELOPE,
    NO_GHERKIN_DOC,
    NO_FEATURE_PICKLE,
    STEPLIST_EMPTY,
    DUPLICATE_FILE
  }

  public static final Map<MessageId, String> ERROR_MESSAGES =
      Map.of(
          MessageId.NO_ENVELOPE,
          "Die Feature Datei ''{0}'' scheint nicht korrekt zu sein (NO_ENVELOPE)",
          MessageId.NO_GHERKIN_DOC,
          "Die Feature Datei ''{0}'' scheint nicht korrekt zu sein (NO_GHERKIN_DOC)",
          MessageId.NO_FEATURE_PICKLE,
          "Die Feature Datei ''{0}'' scheint nicht korrekt zu sein (NO_FEATURE_PICKLE)",
          MessageId.STEPLIST_EMPTY,
          "Testergebnis für den Schritt ''{0}'' in der JSON Datei ''{1}'' ist leer",
          MessageId.DUPLICATE_FILE,
          "Eine Feature Datei mit einem identen Namen wurde in folgendem Ordner ''{0}'' gefunden");

  public ParserException(MessageId msgId, Object... args) {
    super(MessageFormat.format(ERROR_MESSAGES.get(msgId), args));
  }
}
