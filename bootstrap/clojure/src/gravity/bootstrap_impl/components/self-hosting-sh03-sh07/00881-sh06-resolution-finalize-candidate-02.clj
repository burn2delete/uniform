
(defn sh06-resolution-deserialize
  [serialized]
  (let [maximum (:maximum-serialized-bytes
                 sh06-resolution-artifact-bounds)
        serialized-byte-count
        (cond
          (string? serialized)
          (sh06-resolution-utf8-byte-count-up-to serialized maximum)

          (= (class serialized) (Class/forName "[B"))
          (alength ^bytes serialized)

          :else nil)
        _
        (when-not (and (some? serialized-byte-count)
                       (<= serialized-byte-count maximum))
          (sh06-resolution-boundary-fail!
           "C5-UNRESOLVED" "<sh06-resolution-deserialize>"
           :bounded-resolution-serialization-bytes
           {:serialized-class
            (when (some? serialized) (.getName (class serialized)))
            :observed-serialized-bytes serialized-byte-count
            :maximum-serialized-bytes maximum}
           {:aggregate-artifact-bounds sh06-resolution-artifact-bounds}))
        artifact
        (try
          (let [serialized-text
                (if (string? serialized)
                  serialized
                  (String. ^bytes serialized
                           java.nio.charset.StandardCharsets/UTF_8))]
            (edn/read-string
             {:readers *data-readers* :default tagged-literal}
             serialized-text))
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Throwable error
            (sh06-resolution-boundary-fail!
             "C5-UNRESOLVED" "<sh06-resolution-deserialize>"
             :canonical-resolution-serialization
             {:serialized-class
              (when (some? serialized) (.getName (class serialized)))}
             {:contained-host-error (.getName (class error))})))
        report (sh06-resolution-artifact-verification artifact)]
    (when-not (= :passed (:status report))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED"
       (or (get-in artifact [:provenance :source-path])
           "<sh06-resolution-deserialize>")
       :verified-resolution-artifact-after-deserialization
       (:failed-checks report) {}))
    artifact))
