
(defn sh06-resolution-deserialize
  ([serialized]
  (let [_
        (when-not (or (string? serialized)
                      (= (class serialized) (Class/forName "[B")))
          (sh06-resolution-boundary-fail!
           "C5-UNRESOLVED" "<sh06-resolution-deserialize>"
           :resolution-serialization-input
           {:serialized-class
              (when (some? serialized) (.getName (class serialized)))} {}))
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
  ([serialized invocation]
   (sh06-resolution-with-reader-invocation
    "<sh06-resolution-deserialize>" invocation
    #(sh06-resolution-deserialize serialized))))
(defn sh06-resolution-module-input-views!
  [source-path inputs]
  (when-not (and (map? inputs) (= #{:artifact :schema-version :module-artifacts} (set (keys inputs)))
                 (= :gravity/sh06-module-inputs (:artifact inputs)) (= 1 (:schema-version inputs))
                 (vector? (:module-artifacts inputs))
                 (= :passed (:status (sh06-resolution-carrier-validation inputs sh06-resolution-artifact-bounds))))
    (sh06-resolution-boundary-fail! "C5-UNRESOLVED" source-path :ordinary-module-inputs inputs
                                  {:reason :malformed-module-inputs}))
  (mapv
   (fn [artifact]
     (let [boundary (:gravity-resolution-boundary artifact)
           request (:authenticated-resolution-request boundary)]
       (when-not (and (= :gravity/sh06-resolution-artifact (:kind artifact))
                      (= :accepted (:status artifact)) (= :gravity-source (:owner boundary))
                      (= :gravity/sh06-ordinary-module-resolution-request (:artifact request))
                      (= 2 (:schema-version request))
                      (= :passed (:status (sh06-resolution-artifact-verification artifact))))
         (sh06-resolution-boundary-fail! "C5-UNRESOLVED" source-path :verified-ordinary-module-artifact
                                       (:artifact-id artifact) {:reason :invalid-module-artifact}))
       {:artifact-id (:artifact-id artifact) :analysis (:resolved-analysis boundary) :request request
        :raw-analysis (:raw-analysis boundary) :digest-requests (:digest-requests boundary)
        :resolved-digests (:resolved-digests boundary) :definitions (:definitions request)}))
   (:module-artifacts inputs)))

(defn-
 resolution-module-request-from-base
 [source-path sh05-artifact inputs base]
 (let
  [views (sh06-resolution-module-input-views! source-path inputs)]
  (gravity.resolution/module-request sh05-artifact base views)))

(defn sh06-resolution-module-request
  [source-path sh05-artifact inputs]
  (resolution-module-request-from-base
   source-path sh05-artifact inputs
   (sh06-resolution-base-request-data source-path sh05-artifact {})))
