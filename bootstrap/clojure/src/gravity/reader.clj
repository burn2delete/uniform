(ns gravity.reader
  "Native source intake, strict UTF-8 decoding and stage0 reader artifact assembly.

  This namespace owns native intake through an explicit byte owner, strict decoding, source-path
  diagnostic data, existing artifact envelopes and a hosted compatibility oracle.
  Admission, parsing, syntax, source identity and diagnostic publication remain
  with their supplied owners. Operations are trusted seed dependencies, not evidence of
  canonical reader authentication or self-hosting.")

(def ^:private namespace-contract
  {:namespace 'gravity.reader
   :governing-contracts [:D1 :D2 :D3 :D8 :D9 :L1 :C1 :C2 :C3 :C15 :BOOT1 :BOOT8 :TEST2]
   :contract-boundary :native-reader-intake-products-report-validation-and-document-projection
   :public-api
   {'native-document-projection
    {:arglists '([source-path source-text project-context reader-options views operations])
     :returns :gravity/native-c2-document-projection
     :admission :none :authenticates-inputs? false
     :captured-view-fields [:overrides :products :module-context :source-unit
                            :lexical-token-stream? :nested-form-tree? :token-stream :form-tree
                            :top-level-form-ids :lexical-validation :syntax-seeds :literal-records
                            :extension-invocations :deferred-literals :diagnostics
                            :incremental-hashes :integrity-record]
     :operation-inputs [:governing-document-var :diagnostic-ids-var :uncredited-entrypoints-var
                        :trivia-records-var :rejected-designs-var :validate-var :capability-proof-var
                        :pure-text-options-var :resource-options-var :artifact-id-var]
     :closed-operation-keys? true :callback-type-checks? false
     :envelope-in-identity? false}
    'validate-native-reader-report!
    {:arglists '([proof-var fail-var source-span-var source-path artifact])
     :returns :legacy-complete-marker-or-owner-failure
     :admission :none :authenticates-inputs? false
     :operation-inputs [:proof-var :fail-var :source-span-var]}
    'compatibility-reader-products
    {:arglists '([source-path source-text reader-options project-context operations])
     :returns :hosted-compatibility-reader-products
     :admission :none :authenticates-inputs? false
     :operation-inputs [:source-unit-record-var :reader-table-var :token-stream-var
                        :token-record-var :parsed-products-var :form-record-var
                        :reader-fail-var :source-span-var]}
    'read-source-artifact
    {:arglists '([source-path source-text project-context operations])
     :returns :gravity/stage0-reader-artifact}
    'decode-source-bytes
    {:arglists '([sha256-bytes-hex-var fail-var path bytes])
     :returns :decoded-utf8-text :admission :none :opens-source? false
     :operation-inputs [:sha256-bytes-hex-var :fail-var]}
    'read-source-text
    {:arglists '([qst-or-gravity-source-var source-path-policy-fail-var decode-source-bytes-var read-target-source-bytes-var path])
     :returns :decoded-utf8-text :admission :none :opens-source? true
     :grants-file-authority? false :direct-file-access? false
     :source-byte-capture :explicit-existing-owner-var
     :operation-inputs [:qst-or-gravity-source-var :source-path-policy-fail-var :decode-source-bytes-var :read-target-source-bytes-var]}
    'reject-source-path!
    {:arglists '([gravity-source-extension-var co-canonical-source-extensions-var sha256-bytes-hex-var fail-var source-path bytes])
     :returns :unchanged-source-extension-failure :admission :none :opens-source? false
     :operation-inputs [:gravity-source-extension-var :co-canonical-source-extensions-var
                        :sha256-bytes-hex-var :fail-var]}}
   :artifact-inputs [:source-path :decoded-source-text :explicit-project-context
                     :captured-native-document-views-and-late-owner-vars
                     :compatibility-oracle-late-owner-vars
                     :seed-reader-operations :standard-reader-options
                     :captured-source-bytes :raw-byte-hash-var :diagnostic-var
                     :extension-predicate-var :path-policy-refusal-var :decoder-var :source-byte-reader-var :co-canonical-extension-set-var]
   :artifact-outputs [:gravity/stage0-reader-artifact :decoded-utf8-text
                      :hosted-compatibility-reader-products :legacy-native-reader-complete-marker
                      :gravity/native-c2-document-projection
                      :unchanged-source-encoding-failure :unchanged-source-extension-failure
                      :unchanged-source-byte-owner-failure]
   :preserves [:source-spans :metadata :reader-origin :profile-context
               :source-unit-identity :namespace-clause-syntax]
   :invalidates []
   :diagnostic-ownership
   {:owns ["L1-SOURCE-ENCODING" "L1-SOURCE-EXTENSION"]
    :construction :stage-owned-rule-data-with-explicit-existing-failure-var
    :admission-owner :supplied-caller
    :delegates ["C2-ENCODING"]
    :propagates [:L1 :C2 :stage1-reader-execution]
    :compatibility-oracle {:owns :native-host-stack-and-exception-containment
                           :host-stack-id "C2-HASH"
                           :malformed-source-ids :original-stage1-reader-ids
                           :policy-owner :supplied-reader-failure-var}
    :native-report-validation {:owns :ordered-missing-field-refusal-selection
                               :rule-ids ["C2-HASH" "C2-ABBREV" "C2-STRING" "C2-EXTENSION"]
                               :proof-owner :supplied-proof-var
                               :policy-owner :supplied-failure-var
                               :authenticates-inputs? false}
    :native-document-projection {:owns :assembly-and-late-port-sequencing
                                 :port-shape-id :native-c2-assembly-operations
                                 :delegates [:native-report-policy :identity-policy]
                                 :remapping-owner :guarded-source-facade
                                 :authenticates-inputs? false}
    :does-not-own [:token-and-form-policy :namespace-shape-policy :c15-schema
                   :diagnostic-identity :redaction :rendering]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :operation-inputs
   [:read-source-form-records :validate-ns-syntax! :reader-module-context
    :syntax-object-stream :l1-source-unit-artifacts :standard-reader-options
    :gravity-source-extension :gravity-source-kind :utf8-byte-count]
   :unsupported [:other-encodings :replacement-decoding :file-authority
                 :native-document-product-or-callback-authentication
                 :native-document-canonical-admission-and-publication
                 :native-report-authentication
                 :compatibility-oracle-product-authentication
                 :operation-authentication :complete-reader-conformance
                 :migration :release :seed-retirement]
   :test-owner 'gravity.reader.artifact-test
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-reader? false
   :authenticated-reader? false
   :self-hosted? false})

(defn read-source-artifact
  "Assemble the existing stage0 artifact using explicit owning operations.

  Operations retain the bootstrap's interposition and rejection behavior.
  This is a seed composition API; supplied operations confer no authority."
  [source-path source-text project-context
   {:keys [read-source-form-records validate-ns-syntax! reader-module-context
           syntax-object-stream l1-source-unit-artifacts standard-reader-options
           gravity-source-extension gravity-source-kind utf8-byte-count]}]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        context (reader-module-context forms)
        syntax (syntax-object-stream source-path records context)
        reader-details (l1-source-unit-artifacts source-path source-text
                                                 standard-reader-options
                                                 project-context)]
    (merge
     {:kind :gravity/stage0-reader-artifact
      :pass {:name :reader
             :input :source-bytes
             :output :syntax-object-stream
             :preserves [:source-spans :metadata :reader-origin
                         :profile-context :source-unit-identity]
             :rejects ["L1-DELIMITER" "L1-STRING" "L1-NUMERIC"
                       "L1-IDENTIFIER" "L1-MAP-ARITY"
                       "L1-METADATA" "L1-NS-SHAPE"
                       "L1-READER-EXTENSION" "L1-SOURCE-ENCODING"
                       "L1-SOURCE-EXTENSION"]}
      :source {:path source-path
               :extension (gravity-source-extension source-path)
               :encoding :utf-8
               :source-kind (gravity-source-kind source-path)
               :byte-count (utf8-byte-count source-text)
               :form-count (count records)}
      :module-context (dissoc context :namespace-clause-syntax)
      :syntax-object-stream syntax
      :namespace-clause-syntax (:namespace-clause-syntax context)
      :diagnostics []}
     reader-details)))

(defn decode-source-bytes
  "Decode one already-captured source snapshot without reopening its path."
  [sha256-bytes-hex-var fail-var path bytes]
  (let [decoder (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                  (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                  (.onUnmappableCharacter
                   java.nio.charset.CodingErrorAction/REPORT))
        input (java.nio.ByteBuffer/wrap bytes)
        output (java.nio.CharBuffer/allocate (max 1 (alength bytes)))
        result (.decode decoder input output true)]
    (when (.isError result)
      (let [start (.position input)
            end (+ start (max 1 (.length result)))
            bytes-hash (str "sha256:" ((deref sha256-bytes-hex-var) bytes))]
        ((deref fail-var) "L1-SOURCE-ENCODING"
               "source bytes cannot be decoded as UTF-8"
               {:severity :error
                :stage :read-source
                :source-id bytes-hash
                :source-span {:source path
                              :byte-start start
                              :byte-end end}
                :primary {:span {:source path
                                 :byte-start start
                                 :byte-end end}
                          :artifact bytes-hash}
                :related []
                :origin-chain [{:kind :source-bytes :path path}]
                :profile nil
                :target nil
                :facts {:declared-encoding :utf-8
                        :bytes-hash bytes-hash
                        :malformed-input-length (.length result)}
                :reader-state {:stage :source-decoding
                               :encoding :utf-8}
                :remediation "Save the source as valid UTF-8 without replacement decoding."})))
    (let [flush-result (.flush decoder output)]
      (when (.isError flush-result)
        (.throwException flush-result)))
    (.flip output)
    (.toString output)))

(defn read-source-text
  [qst-or-gravity-source-var source-path-policy-fail-var decode-source-bytes-var read-target-source-bytes-var path]
  (when-not ((deref qst-or-gravity-source-var) path)
    ((deref source-path-policy-fail-var) path))
  ((deref decode-source-bytes-var)
   path
   ((deref read-target-source-bytes-var) path)))

(defn reject-source-path!
  "Construct the existing native extension refusal from supplied bytes.

  This performs no file access or admission and confers no authority."
  [gravity-source-extension-var co-canonical-source-extensions-var sha256-bytes-hex-var fail-var source-path bytes]
   (let [bytes-hash (when bytes
                      (str "sha256:" ((deref sha256-bytes-hex-var) bytes)))
         span {:source source-path :byte-start 0 :byte-end 0}]
     ((deref fail-var) "L1-SOURCE-EXTENSION"
            "Gravity source path does not use a co-canonical source extension"
            (cond->
             {:severity :error
              :stage :read-source
              :source-span span
              :primary (cond-> {:span span}
                         bytes-hash (assoc :artifact bytes-hash))
              :related []
              :origin-chain
              [(cond-> {:kind :source-path :path source-path}
                 bytes-hash (assoc :source-id bytes-hash))]
              :profile nil
              :target nil
              :facts
              (cond->
               {:actual-extension ((deref gravity-source-extension-var) source-path)
                :allowed-extensions
                (vec (sort (deref co-canonical-source-extensions-var)))}
                bytes-hash (assoc :bytes-hash bytes-hash
                                  :byte-count (alength bytes)))
              :reader-state {:stage :source-unit-policy}
              :remediation
              "Rename the source to .qst or .gravity; both are canonical and first-class."}
              bytes-hash (assoc :source-id bytes-hash)))))

(defn compatibility-reader-products
  "Construct the existing hosted comparison-oracle products using late owner Vars.

  This does not invoke or replace canonical C2/SH03 reader admission."
  [source-path source-text reader-options project-context
   {:keys [source-unit-record-var reader-table-var token-stream-var
           token-record-var parsed-products-var form-record-var
           reader-fail-var source-span-var]}]
  (let [source-unit ((deref source-unit-record-var)
                     source-path source-text reader-options project-context)
        table ((deref reader-table-var))]
    (try
      (let [stage1-stream ((deref token-stream-var)
                           source-path source-text table reader-options)
            token-stream (mapv #((deref token-record-var) % source-unit)
                               (:tokens stage1-stream))
            parser-stream (assoc stage1-stream :tokens token-stream)
            parsed ((deref parsed-products-var)
                    source-path source-text table parser-stream)
            form-tree (mapv #((deref form-record-var) % source-unit)
                            (:form-tree parsed))]
        {:source-unit source-unit
         :token-stream token-stream
         :form-tree form-tree
         :root-form-ids (:root-form-ids parsed)
         :parsed-records (:records parsed)
         :parsed-values (:parsed-values parsed)})
      (catch StackOverflowError error
        ((deref reader-fail-var)
         "C2-HASH" source-path
         {:stage :read-source
          :source-id (:source-id source-unit)
          :source-span ((deref source-span-var) source-path 0)
          :reader-options reader-options}
         {:missing-fields [:stack-safe-reader-form-construction]
          :facts {:failure-kind :reader-host-stack-overflow
                  :contained-host-error (.getName (class error))}}))
      (catch clojure.lang.ExceptionInfo ex
        (throw
         (ex-info (.getMessage ex)
                  (merge {:source-id (:source-id source-unit)
                          :reader-options reader-options}
                         (ex-data ex))
                  ex))))))

(defn validate-native-reader-report!
  "Preserve native report-field refusal order; the complete marker grants no admission."
  [proof-var fail-var source-span-var source-path artifact]
  (let [proof ((deref proof-var) artifact)]
    (doseq [[field id] [[:source-unit-hash-stable? "C2-HASH"]
                      [:token-and-form-spans-present? "C2-HASH"]
                      [:abbreviation-origins-present? "C2-ABBREV"]
                      [:literal-facts-present? "C2-STRING"]
                      [:trivia-retained? "C2-HASH"]
                      [:extension-policy-recorded? "C2-EXTENSION"]
                      [:incremental-hashes-stable? "C2-HASH"]
                      [:diagnostics-covered? "C2-HASH"]
                      [:lexical-token-stream? "C2-HASH"]
                      [:nested-form-tree? "C2-HASH"]]]
      (when-not (get proof field)
        ((deref fail-var) id source-path
         {:stage :read-source
          :source-id (get-in artifact [:source-unit-record :source-id])
          :source-span (or (get-in artifact [:token-stream 0 :span])
                           ((deref source-span-var) source-path 0))
          :reader-options (get-in artifact [:source-unit-record :reader-options])}
         {:missing-fields [field]}))))
  :complete)

(defn native-document-projection
  "Project the native C2 document tail without admission or input authentication."
  [source-path source-text project-context reader-options views operations]
  (let [required #{:governing-document-var :diagnostic-ids-var :uncredited-entrypoints-var
                   :trivia-records-var :rejected-designs-var :validate-var :capability-proof-var
                   :pure-text-options-var :resource-options-var :artifact-id-var}
        actual (set (keys operations))]
    (when-not (= required actual)
      (throw (ex-info "Native C2 document projection requires exactly ten owner ports"
                      {:id :native-c2-assembly-operations :required-keys required :actual-keys actual}))))
  (let [{:keys [overrides products module-context source-unit lexical-token-stream?
                nested-form-tree? token-stream form-tree top-level-form-ids
                lexical-validation syntax-seeds literal-records extension-invocations
                deferred-literals diagnostics incremental-hashes integrity-record]} views
        {:keys [governing-document-var diagnostic-ids-var uncredited-entrypoints-var
                trivia-records-var rejected-designs-var validate-var capability-proof-var
                pure-text-options-var resource-options-var artifact-id-var]} operations
        artifact-base
        {:kind :gravity/stage0-c2-reader-document-artifact
         :task "P06-D081"
         :document-set ["C2"]
         :governing-document (deref governing-document-var)
         :pass {:name :c2-reader-document-coverage
                :input :source-bytes
                :output :reader-document-proof
                :requires [:source-unit :reader-policy]
                :preserves [:source-spans :raw-literal-facts
                            :reader-origin :trivia :diagnostics]
                :emits [:source-unit-record :token-stream :form-tree
                        :syntax-seed-stream :reader-source-map
                        :literal-decoding-records
                        :reader-extension-invocation-records
                        :reader-diagnostics :incremental-reader-hash]
                :rejects (deref diagnostic-ids-var)}
         :source-overrides overrides
           ;; Diagnostic observations only; never SH03 authentication authority.
         :extension-dispatch-observations
         (:extension-dispatch-observations products)
         :module (assoc (dissoc module-context :namespace-clause-syntax)
                        :source-path source-path)
         :source-unit-record source-unit
         :gravity-reader-boundary
         {:slice :SH-03
          :owner :gravity-source
          :plan-binding (:sh03-reader-plan-binding products)
          :resolved-reader-result
          (select-keys (:sh03-reader-result products)
                       [:artifact :schema-version :status
                        :actual-path-provenance
                        :incremental-reader-hashes
                        :semantic-reader-template
                        :execution-boundary])
          :adapter-contract (:sh03-reader-adapter-contract products)
          :uncredited-source-models
          {:status :not-executed
           :entrypoints (deref uncredited-entrypoints-var)
           :self-hosting-credit? false
           :seed-retirement-credit? false
           :release-credit? false}
          :semantic-value-table-id
          (:sh03-semantic-value-table-id products)
          :authenticated-envelope-descriptor
          (:sh02-reader-envelope-descriptor products)
          :authenticated-envelope (:sh02-reader-envelope products)
          :target-source-reread? false
          :clojure-adapter-residual? true
          :self-hosted? false}
         :representation-boundary
         {:token-stream :ordered-utf8-lexical-token-stream
          :form-tree :recursive-delimiter-linked-form-tree
          :lexical-token-stream? lexical-token-stream?
          :nested-form-tree? nested-form-tree?
          :remaining-reader-boundaries
          [:full-language-literal-surface
           :full-language-reader-abbreviation-surface
           :full-language-reader-extension-registry
           :host-and-seed-retirement]
          :sh03-bootstrap-subset-status :complete
          :status (if (and lexical-token-stream? nested-form-tree?)
                    :complete-for-slice
                    :failed)}
         :token-stream token-stream
         :form-tree form-tree
         :top-level-form-ids top-level-form-ids
         :parsed-semantic-values (:parsed-values products)
         :lexical-product-validation lexical-validation
         :syntax-seed-stream syntax-seeds
         :reader-source-map (mapv #(select-keys % [:syntax-id :form-id :span])
                                  syntax-seeds)
         :gravity-reader-source-map (:gravity-reader-source-map products)
         :literal-decoding-records literal-records
         :trivia-retention-records ((deref trivia-records-var) token-stream)
         :reader-extension-policy
         {:artifact :gravity/reader-extension-policy
          :extensions
          [{:tag 'inst
            :handler 'gravity.reader.standard/read-inst
            :build-effects #{}
            :capabilities #{}
            :profiles #{:kernel :core :hosted :meta}
            :output :syntax-seed}
           {:tag 'uuid
            :handler 'gravity.reader.standard/read-uuid
            :build-effects #{}
            :capabilities #{}
            :profiles #{:kernel :core :hosted :meta}
            :output :syntax-seed}]
          :status :registered}
         :reader-extension-invocation-records extension-invocations
         :semantic-error-deferment-record
         {:artifact :gravity/semantic-error-deferment
          :forms-retained [:unknown-symbol :profile-illegal-form
                           :zero-denominator-ratio
                           :host-independent-decimal-range]
          :deferred-literal-records deferred-literals
          :semantic-analysis-in-reader? false
          :module-parser-invoked? false
          :deferred? true
          :owner-phases [:namespace-analysis :type-check :profile-validate
                         :numeric-mode-validation]}
         :reader-diagnostics diagnostics
         :incremental-reader-hashes incremental-hashes
         :reader-product-integrity integrity-record
         :rejected-design-coverage (deref rejected-designs-var)
         :diagnostics []}
        _ ((deref validate-var) source-path artifact-base)
        capability-proof ((deref capability-proof-var) artifact-base)
        conformance {:documents ["C2"]
                     :task "P06-D081"
                     :required-diagnostic-ids (deref diagnostic-ids-var)
                     :source-unit-status :complete
                     :token-stream-status :complete-for-slice
                     :form-tree-status :complete-for-slice
                     :span-status :exact-utf8-byte-and-line-column
                     :abbreviation-status :complete-bootstrap-subset
                     :literal-status :complete-bootstrap-subset
                     :trivia-status :reader-option-sensitive
                     :extension-status :complete-bootstrap-subset
                     :incremental-hash-status :complete-for-slice
                     :diagnostic-status :complete-bootstrap-subset
                     :semantic-deferment-status :complete-for-slice
                     :status :partial}
        artifact (assoc artifact-base
                        :capability-based-proof capability-proof
                        :c2-reader-results conformance)
        artifact
        (if ((deref pure-text-options-var) reader-options)
          (-> artifact
              (assoc-in [:reader-extension-policy :extensions]
                        [{:tag 'text :handler 'sh03-reader-read-text
                          :build-effects #{} :capabilities #{} :profiles #{:meta}
                          :output :syntax-seed}])
              (assoc-in [:gravity-reader-boundary :pure-text-reconstruction-input]
                        {:source-text source-text :project-context project-context}))
          (if ((deref resource-options-var) reader-options)
            (assoc-in artifact [:reader-extension-policy :extensions]
                      [{:tag 'resource :handler 'gravity.reader/read-input
                        :build-effects #{:build/read-file} :capabilities #{:fs/read}
                        :profiles #{:meta} :output :syntax-seed}])
            artifact))]
    {:kind :gravity/native-c2-document-projection :admission :none :authenticates-inputs? false :artifact (assoc artifact :artifact-id ((deref artifact-id-var) artifact))}))

(doseq
 [name (keys (:public-api namespace-contract))
  :let [operation (get (ns-interns *ns*) name)]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
