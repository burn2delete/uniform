(ns gravity.self-hosting.sh03-decode-reference
  "Independent UTF-8/scalar/location oracle. No Reader or Bootstrap dependency.")

(defn sha256 [bytes]
  (str "sha256:" (format "%064x" (java.math.BigInteger. 1
                   (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)))))

(defn positions [text]
  (loop [utf16 0 byte 0 scalar 0 line 1 column 1 result {}]
    (let [result (assoc result byte {:byte byte :scalar scalar :utf16 utf16
                                     :line line :column column})]
      (if (= utf16 (.length ^String text)) result
        (let [cp (.codePointAt ^String text utf16)
              width (Character/charCount cp)
              next (+ utf16 width)
              crlf? (and (= cp 13) (< next (.length ^String text))
                         (= 10 (.codePointAt ^String text next)))]
          (when (<= 0xd800 cp 0xdfff)
            (throw (ex-info "Reference input is not a Unicode scalar string" {})))
          (recur next
                 (+ byte (alength (.getBytes (String. (Character/toChars cp))
                                             java.nio.charset.StandardCharsets/UTF_8)))
                 (inc scalar)
                 (if (or (= cp 10) (and (= cp 13) (not crlf?))) (inc line) line)
                 (cond (= cp 10) 1 (= cp 13) (if crlf? (inc column) 1) :else (inc column))
                 result))))))

(defn slice [bytes span]
  (String. (java.util.Arrays/copyOfRange bytes (int (:byte-start span)) (int (:byte-end span)))
           java.nio.charset.StandardCharsets/UTF_8))

(defn strict-text [bytes]
  (str (.decode (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                  (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                  (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
                (java.nio.ByteBuffer/wrap bytes))))
