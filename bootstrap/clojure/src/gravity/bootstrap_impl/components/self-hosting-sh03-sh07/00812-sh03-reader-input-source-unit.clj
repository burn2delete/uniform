

(defn sh03-reader-input-source-unit
  [source-path source-bytes project-context]
  (let [context (reader-explicit-project-context project-context)
        extension (gravity-source-extension source-path)
        identity-fields
        {:project-root-id (:project-root-id context)
         :logical-source-id (:project-relative-path context)
         :actual-path source-path}]
    (doseq [[field value] identity-fields]
      (let [utf8-byte-count
            (when (string? value)
              (alength (.getBytes ^String value
                                 java.nio.charset.StandardCharsets/UTF_8)))]
        (when-not (and (string? value)
                       (<= (.length ^String value)
                           sh03-reader-input-maximum-identity-code-units)
                       (<= utf8-byte-count
                           sh03-reader-input-maximum-identity-utf8-bytes))
          (sh03-reader-boundary-fail!
           "<sh03-source-identity>" :bounded-sh03-reader-source-identity
           {:field field}
           {:maximum-code-units
            sh03-reader-input-maximum-identity-code-units
            :maximum-utf8-bytes
            sh03-reader-input-maximum-identity-utf8-bytes
            :observed-code-units
            (when (string? value) (.length ^String value))
            :observed-utf8-bytes utf8-byte-count}))))
    {:artifact :gravity/sh03-source-unit
     :schema-version 1
     :project-root-id (:project-root-id context)
     :logical-source-id (:project-relative-path context)
     :source-content-hash
     (str "sha256:" (sha256-bytes-hex source-bytes))
     :source-byte-count (alength source-bytes)
     :encoding :utf-8
     :actual-path-provenance
     {:path source-path
      :extension extension
      :source-kind (case extension ".gravity" :gravity ".qst" :qst nil)}}))

(defn sh03-reader-input-policy
  [reader-options]
  (reader-validate-options! reader-options)
  {:artifact :gravity/sh03-reader-policy
   :schema-version 1
   :retain-trivia (true? (:retain-comments reader-options))
   :enabled-reader-tags
   (mapv sh03-reader-tag-codepoints (:registered-tags standard-reader-policy))})

(defn sh03-reader-require-result-carrier!
  [source-path carrier value]
  (let [validation
        (p15-s23-trusted-carrier-validation
         value :default-only sh03-reader-result-maximum-nodes
         sh03-reader-result-maximum-depth sh03-reader-result-maximum-width)]
    (when-not (= :passed (:status validation))
      (sh03-reader-boundary-fail!
       source-path :trusted-bounded-sh03-reader-result
       {} (merge {:carrier carrier}
                 (select-keys validation
                              [:reason :observed-nodes :observed-depth
                               :maximum-nodes :maximum-depth
                               :maximum-width]))))
    validation))

(defn sh03-reader-result-preflight!
  [source-path source-unit reader-policy result]
  (sh03-reader-require-result-carrier!
   source-path :gravity-reader-result result)
  (let [bounds (:bounds result)
        carrier-responsibility
        (get-in result
                [:execution-boundary :clojure-boundary-responsibility])
        input-responsibility
        (get-in result [:execution-boundary :gravity-input-responsibility])
        accepted? (= :accepted (:status result))]
    (when-not
     (and (map? result)
          (= sh03-reader-result-keys (set (keys result)))
          (= :gravity/sh03-reader-result (:artifact result))
          (= 1 (:schema-version result))
          (contains? #{:accepted :rejected} (:status result))
          (= (:actual-path-provenance source-unit)
             (:actual-path-provenance result))
          (= reader-policy
             (get-in result [:source-unit :reader-options]))
          (= (:source-content-hash source-unit)
             (get-in result [:source-unit :bytes-hash]))
          (= (:source-byte-count source-unit)
             (get-in result [:source-unit :source-byte-count]))
          (vector? (:token-stream result))
          (vector? (:form-tree result))
          (vector? (:top-level-form-ids result))
          (vector? (:top-level-parsed-records result))
          (vector? (:parsed-semantic-values result))
          (vector? (:semantic-value-table result))
          (vector? (:literal-decoding-records result))
          (vector? (:reader-extension-invocation-records result))
          (vector? (:digest-requests result))
          (vector? (:diagnostics result))
          (map? bounds)
          (= sh03-reader-result-maximum-nodes
             (:maximum-result-carrier-nodes bounds))
          (= sh03-reader-result-maximum-depth
             (:maximum-result-carrier-depth bounds))
          (= sh03-reader-result-maximum-width
             (:maximum-result-carrier-width bounds))
          (= sh03-reader-canonical-maximum-scalar-bytes
             (:maximum-canonical-scalar-bytes bounds))
          (= sh03-reader-canonical-maximum-total-scalar-bytes
             (:maximum-canonical-total-scalar-bytes bounds))
          (= sh03-reader-input-maximum-identity-utf8-bytes
             (:maximum-source-identity-utf8-bytes bounds))
          (map? carrier-responsibility)
          (= sh03-reader-result-maximum-nodes
             (:maximum-nodes carrier-responsibility))
          (= sh03-reader-result-maximum-depth
             (:maximum-depth carrier-responsibility))
          (= sh03-reader-result-maximum-width
             (:maximum-width carrier-responsibility))
          (= sh03-reader-canonical-maximum-scalar-bytes
             (:maximum-canonical-scalar-bytes carrier-responsibility))
          (= sh03-reader-canonical-maximum-total-scalar-bytes
             (:maximum-canonical-total-scalar-bytes
              carrier-responsibility))
          (map? input-responsibility)
          (= sh03-reader-input-maximum-identity-utf8-bytes
             (:maximum-source-identity-utf8-bytes
              input-responsibility))
          (= sh03-reader-input-maximum-identity-code-units
             (:maximum-logical-source-id-code-units
              input-responsibility))
          (= sh03-reader-input-maximum-identity-code-units
             (:maximum-actual-path-code-units input-responsibility))
          (= (:maximum-source-bytes bounds)
             (:maximum-source-bytes input-responsibility))
          (= :bounded-summary
             (:invalid-input-carrier-policy input-responsibility))
          (integer? (:maximum-tokens bounds))
          (integer? (:maximum-forms bounds))
          (pos-int? (:maximum-semantic-work-units bounds))
          (pos-int? (:maximum-numeric-semantic-scalars bounds))
          (pos-int? (:maximum-numeric-semantic-work-units bounds))
          (map? (:result-carrier-node-budget bounds))
          (<= (get-in bounds
                      [:result-carrier-node-budget :derived-maximum-nodes])
              (:maximum-result-carrier-nodes bounds))
          (<= (count (:token-stream result)) (:maximum-tokens bounds))
          (<= (count (:form-tree result)) (:maximum-forms bounds))
          (<= (count (:digest-requests result)) 6)
          (if accepted?
            (and (empty? (:diagnostics result))
                 (= 6 (count (:digest-requests result)))
                 (= (count (:enabled-reader-tags reader-policy))
                    (count (:reader-extension-invocation-records result)))
                 (= (count (:top-level-form-ids result))
                    (count (:top-level-parsed-records result))
                    (count (:parsed-semantic-values result))))
            (and (= 1 (count (:diagnostics result)))
                 (= 2 (count (:digest-requests result)))
                 (empty? (:token-stream result))
                 (empty? (:form-tree result))
                 (empty? (:top-level-form-ids result))
                 (empty? (:semantic-value-table result))
                 (empty? (:reader-extension-invocation-records result)))))
      (sh03-reader-boundary-fail!
       source-path :exact-sh03-reader-result-envelope
       result {:observed-keys (when (map? result) (set (keys result)))}))
    result))

(defn sh03-reader-verifier-preflight!
  [source-path raw-result report]
  (sh03-reader-require-result-carrier!
   source-path :gravity-reader-verification-report report)
  (when-not
   (and (map? report)
        (= sh03-reader-verification-report-keys (set (keys report)))
        (= :gravity/sh03-reader-verification-report (:artifact report))
        (= 1 (:schema-version report))
        (= :accepted (:status report))
        (true? (:verified? report))
        (= (:status raw-result) (:reader-result-status report))
        (= (:semantic-reader-template raw-result)
           (:semantic-reader-template report))
        (= (mapv #(select-keys
                   % [:key :ordinal :algorithm :encoding :depends-on])
                 (:digest-requests raw-result))
           (:digest-requests report))
        (= [] (:diagnostics report))
        (= (:bounds raw-result) (:bounds report))
        (= (:execution-boundary raw-result)
           (:execution-boundary report)))
    (sh03-reader-boundary-fail!
     source-path :fresh-gravity-sh03-reader-result-replay
     report {:reader-result-status (:status raw-result)}))
  report)

(defn sh03-reader-execute-plan!
  [source-path plan function arguments]
  (try
    (p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh03-pinned-reader-host-runner
      :compiler-artifact-plan? true}
     plan function arguments)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (sh03-reader-boundary-fail!
       source-path :bounded-sh03-reader-host-stack
       function {:contained-host-error (.getName (class error))}))
    (catch AssertionError error
      (sh03-reader-boundary-fail!
       source-path :contained-sh03-reader-assertion
       function {:contained-host-error (.getName (class error))}))
    (catch LinkageError error
      (sh03-reader-boundary-fail!
       source-path :contained-sh03-reader-linkage
       function {:contained-host-error (.getName (class error))}))
    (catch clojure.lang.ExceptionInfo error
      (sh03-reader-boundary-fail!
       source-path :contained-sh03-reader-runtime-diagnostic
       function {:contained-diagnostic (:id (ex-data error))
                 :cause-message (.getMessage error)}))
    (catch Exception error
      (sh03-reader-boundary-fail!
       source-path :contained-sh03-reader-host-failure
       function {:contained-host-error (.getName (class error))
                 :cause-message (.getMessage error)}))))

(def sh03-reader-canonical-maximum-nodes 167772160)
(def sh03-reader-canonical-maximum-depth 2048)
(def sh03-reader-canonical-maximum-width 1048576)
(def sh03-reader-canonical-maximum-scalar-bytes 1048576)
(def sh03-reader-canonical-maximum-total-scalar-bytes 67108864)