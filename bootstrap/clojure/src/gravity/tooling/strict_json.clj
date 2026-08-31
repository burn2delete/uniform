(ns gravity.tooling.strict-json
  "Small strict JSON reader for repository tooling.

  The reader rejects duplicate object keys, malformed UTF-8, trailing input,
  and JSON numbers outside Clojure's exact integer/decimal representations."
  (:import (java.io PushbackReader StringReader)
           (java.nio ByteBuffer)
           (java.nio.charset CharacterCodingException CodingErrorAction StandardCharsets)
           (java.nio.file Files Path)))

(defn- invalid! [message data]
  (throw (ex-info message (assoc data :diagnostic "TOOL-JSON-001"))))

(defn- skip-whitespace! [^PushbackReader reader]
  (loop [value (.read reader)]
    (if (and (not= -1 value)
             (contains? #{\space \tab \newline \return} (char value)))
      (recur (.read reader))
      value)))

(defn- unread! [^PushbackReader reader value]
  (when-not (= -1 value) (.unread reader value)))

(defn- read-string! [^PushbackReader reader]
  (let [output (StringBuilder.)]
    (loop []
      (let [value (.read reader)]
        (cond
          (= -1 value) (invalid! "Unterminated JSON string" {})
          (= \" (char value)) (str output)
          (= \\ (char value))
          (let [escaped (.read reader)]
            (when (= -1 escaped) (invalid! "Unterminated JSON escape" {}))
            (case (char escaped)
              \" (.append output \" )
              \\ (.append output \\)
              \/ (.append output \/)
              \b (.append output \backspace)
              \f (.append output \formfeed)
              \n (.append output \newline)
              \r (.append output \return)
              \t (.append output \tab)
              \u (let [values (repeatedly 4 #(.read reader))]
                   (when (some #(= -1 %) values)
                     (invalid! "Unterminated JSON unicode escape" {}))
                   (let [digits (apply str (map char values))]
                     (when-not (re-matches #"[0-9A-Fa-f]{4}" digits)
                       (invalid! "Malformed JSON unicode escape" {:digits digits}))
                     (.append output (char (Integer/parseInt digits 16)))))
              (invalid! "Unsupported JSON escape" {:escape (str (char escaped))}))
            (recur))
          (< value 0x20)
          (invalid! "JSON string contains an unescaped control character"
                    {:codepoint value})
          :else (do (.append output (char value)) (recur)))))))

(declare read-value!)

(defn- read-literal! [^PushbackReader reader first-character suffix value]
  (doseq [expected suffix]
    (let [actual (.read reader)]
      (when (or (= -1 actual) (not= expected (char actual)))
        (invalid! "Malformed JSON literal"
                  {:literal (str first-character suffix)}))))
  value)

(defn- read-number! [^PushbackReader reader first-character]
  (let [token
        (loop [output (StringBuilder. (str first-character))]
          (let [value (.read reader)]
            (if (and (not= -1 value)
                     (re-matches #"[0-9eE+\-.]" (str (char value))))
              (recur (.append output (char value)))
              (do (unread! reader value) (str output)))))]
    (when-not (re-matches #"-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"
                          token)
      (invalid! "Malformed JSON number" {:token token}))
    (try
      (if (re-find #"[.eE]" token) (bigdec token) (bigint token))
      (catch NumberFormatException exception
        (invalid! "JSON number is out of range"
                  {:token token :cause (.getMessage exception)})))))

(defn- read-array! [^PushbackReader reader]
  (let [first-value (skip-whitespace! reader)]
    (if (= (int \]) first-value)
      []
      (do
        (unread! reader first-value)
        (loop [values []]
          (let [value (read-value! reader)
                delimiter (skip-whitespace! reader)]
            (cond
              (= (int \,) delimiter) (recur (conj values value))
              (= (int \]) delimiter) (conj values value)
              :else (invalid! "JSON array requires comma or closing bracket"
                              {:delimiter delimiter}))))))))

(defn- read-object! [^PushbackReader reader]
  (let [first-key (skip-whitespace! reader)]
    (if (= (int \}) first-key)
      {}
      (do
        (unread! reader first-key)
        (loop [result {}]
          (let [quote-value (skip-whitespace! reader)]
            (when-not (= (int \") quote-value)
              (invalid! "JSON object key must be a string" {:value quote-value}))
            (let [key (read-string! reader)
                  colon (skip-whitespace! reader)]
              (when (contains? result key)
                (invalid! "JSON object repeats a key" {:key key}))
              (when-not (= (int \:) colon)
                (invalid! "JSON object key requires a colon" {:key key}))
              (let [value (read-value! reader)
                    delimiter (skip-whitespace! reader)
                    updated (assoc result key value)]
                (cond
                  (= (int \,) delimiter) (recur updated)
                  (= (int \}) delimiter) updated
                  :else (invalid! "JSON object requires comma or closing brace"
                                  {:key key :delimiter delimiter}))))))))))

(defn- read-value! [^PushbackReader reader]
  (let [value (skip-whitespace! reader)]
    (when (= -1 value) (invalid! "JSON value is missing" {}))
    (let [character (char value)]
      (case character
        \{ (read-object! reader)
        \[ (read-array! reader)
        \" (read-string! reader)
        \t (read-literal! reader character "rue" true)
        \f (read-literal! reader character "alse" false)
        \n (read-literal! reader character "ull" nil)
        (if (or (= character \-) (Character/isDigit character))
          (read-number! reader character)
          (invalid! "Unsupported JSON value" {:value (str character)}))))))

(defn read-strict-json [text]
  (with-open [reader (PushbackReader. (StringReader. (str text)))]
    (let [value (read-value! reader)
          trailing (skip-whitespace! reader)]
      (when-not (= -1 trailing)
        (invalid! "Trailing data follows the JSON document" {:value trailing}))
      value)))

(defn- strict-utf8 [bytes]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (ByteBuffer/wrap bytes)))
      (catch CharacterCodingException exception
        (invalid! "JSON document is not UTF-8"
                  {:cause (.getMessage exception)})))))

(defn load-json [^Path path]
  (read-strict-json (strict-utf8 (Files/readAllBytes path))))
