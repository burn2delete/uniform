(ns gravity.reader
  "Native source intake, strict UTF-8 decoding and stage0 reader artifact assembly.

  This namespace owns native intake through an explicit byte owner, strict decoding, source-path
  diagnostic data, existing artifact envelopes and hosted literal/token/form construction.
  Admission, canonical parsing, syntax, source identity and diagnostic publication remain
  with their supplied owners. Operations are trusted seed dependencies, not evidence of
  canonical reader authentication or self-hosting.")

(def ^:private namespace-contract
  {:namespace 'gravity.reader
   :governing-contracts [:D1 :D2 :D3 :D8 :D9 :L1 :C1 :C2 :C3 :C15 :BOOT1 :BOOT8 :TEST2]
   :contract-boundary :native-reader-intake-literals-tokenization-forms-products-report-validation-and-document-projection
   :public-api
   {'classify-compatibility-token
    {:arglists '([starts-with-var token]) :returns :native-literal-kind
     :admission :none :authenticates-inputs? false :opens-source? false
     :operation-inputs [:starts-with-var] :callback-type-checks? false
     :test-owner 'gravity.reader.literals-test}
    'reject-compatibility-numeric!
    {:arglists '([fail-var source-path raw span token-id cause])
     :returns :original-numeric-owner-refusal :admission :none :authenticates-inputs? false :opens-source? false
     :operation-inputs [:fail-var] :callback-type-checks? false
     :test-owner 'gravity.reader.literals-test}
    'decode-compatibility-atom
    {:arglists '([operations source-path kind raw span token-id])
     :returns :native-literal-value-or-deferred-ratio :admission :none :authenticates-inputs? false :opens-source? false
     :operation-inputs [:starts-with-var :split-var :malformed-numeric-var]
     :callback-type-checks? false :test-owner 'gravity.reader.literals-test}
    'decode-compatibility-string
    {:arglists '([read-string-var fail-var source-path raw span token-id])
     :returns :native-seed-read-value-or-owner-refusal :admission :none :authenticates-inputs? false :opens-source? false
     :operation-inputs [:read-string-var :fail-var] :read-eval false
     :callback-type-checks? false :test-owner 'gravity.reader.literals-test}
    'decode-compatibility-character
    {:arglists '([read-string-var fail-var source-path raw span token-id])
     :returns :native-character-or-owner-refusal :admission :none :authenticates-inputs? false :opens-source? false
     :operation-inputs [:read-string-var :fail-var] :read-eval false :requires-character-result true
     :callback-type-checks? false :test-owner 'gravity.reader.literals-test}
    'build-compatibility-forms
    {:arglists '([source-path source-text table token-stream operations])
     :contract-boundary :hosted-native-token-stream-form-construction
     :returns :hosted-reader-form-products
     :admission :none :authenticates-inputs? false :opens-source? false
     :native-join-checks-authenticate? false :callback-type-checks? false
     :private-context-operation-key :reader-operations
     :operation-inputs [:digest-var :pipeline-fail-var :line-starts-var :token-index-var
                        :span-var :canonical-hash-var :decode-atom-var :fail-var
                        :read-tagged-var :form-kind-var]
     :metadata-target-class 'clojure.lang.IObj :test-owner 'gravity.reader.forms-test}
    'tokenize-compatibility-source
    {:arglists '([source-path source-text table reader-options operations])
     :contract-boundary :hosted-compatibility-source-tokenization
     :returns :gravity/stage1-reader-token-stream
     :admission :none :authenticates-inputs? false :opens-source? false
     :operation-inputs [:line-starts-var :digest-var :line-terminator-var :span-var
                        :fail-var :decode-string-var :token-kind-var :decode-atom-var
                        :decode-character-var]
     :callback-type-checks? false :test-owner 'gravity.reader.tokenizer-test}
    'native-document-projection
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
                     :historical-reader-table :tokenizer-late-owner-vars
                     :native-token-stream :form-builder-late-owner-vars
                     :raw-literal :literal-kind :literal-span :literal-token-id :literal-late-owner-vars
                     :captured-source-bytes :raw-byte-hash-var :diagnostic-var
                     :extension-predicate-var :path-policy-refusal-var :decoder-var :source-byte-reader-var :co-canonical-extension-set-var]
   :artifact-outputs [:gravity/stage0-reader-artifact :decoded-utf8-text
                      :hosted-compatibility-reader-products :legacy-native-reader-complete-marker
                      :gravity/native-c2-document-projection :gravity/stage1-reader-token-stream
                      :hosted-reader-form-products :native-literal-value :deferred-ratio-literal
                      :original-literal-owner-refusal
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
    :hosted-tokenizer {:owns [:lexical-branch-selection :token-realization :tokenizer-sequencing]
                      :rule-ids ["STAGE1READER003" "STAGE1READER004"]
                      :literal-policy-owner :supplied-literal-decoder-vars
                      :span-owner :supplied-span-var :construction-owner :supplied-failure-var
                      :authenticates-inputs? false}
    :hosted-form-builder {:owns [:native-token-joins :form-construction :legacy-set-duplicate-rule
                                :metadata-shape :abbreviation-expansion :parent-child-root-links]
                         :rule-ids ["STAGE1PIPE004" "STAGE1READER001" "STAGE1READER002"
                                    "STAGE1READER004" "STAGE1READER005"
                                    "C2-SET" "C2-METADATA" "C2-ABBREV"]
                         :literal-owner :supplied-decoder-vars :span-owner :supplied-span-var
                         :construction-owners [:supplied-pipeline-failure-var :supplied-failure-var]
                         :authenticates-inputs? false}
    :hosted-literals {:owns [:legacy-kind-selection :legacy-literal-decoding :numeric-refusal-data]
                     :rule-ids ["STAGE1READER003" "STAGE1READER007"]
                     :construction-owner :supplied-failure-var
                     :numeric-catch-owner :supplied-malformed-numeric-var
                     :authenticates-inputs? false}
    :does-not-own [:namespace-shape-policy :c15-schema
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
                 :tokenizer-text-table-options-callback-and-output-authentication
                 :native-form-source-table-token-callback-and-output-authentication
                 :literal-raw-kind-provenance-and-callback-authentication
                 :complete-literal-grammar :gravity-numeric-and-unicode-character-semantics
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

(defn- compatibility-reader-token-context
  [ops source-path ^String source-text table reader-options]
  (let [line-starts ((:line-starts-var ops) source-text)
        whitespace (set (:whitespace table))
        delimiters (:delimiters table)
        dispatch (:dispatch table)
        comment-marker (:line-comment table)
        string-delimiter (:string-delimiter table)
        retain-trivia? (true? (:retain-comments reader-options))
        source-length (count source-text)
        table-id (str "sha256:" ((:digest-var ops) (pr-str table)))]
    {:source-path source-path
     :source-text source-text
     :line-starts line-starts
     :whitespace whitespace
     :delimiters delimiters
     :dispatch dispatch
     :comment-marker comment-marker
     :string-delimiter string-delimiter
     :retain-trivia? retain-trivia?
     :source-length source-length
     :table-id table-id}))

(defn- compatibility-reader-char-string
  [{:keys [^String source-text]} idx]
  (str (.charAt source-text idx)))

(defn- compatibility-reader-ignored?
  [ops {:keys [source-length whitespace] :as context} idx]
  (and (< idx source-length)
       (contains? whitespace
                  ((:char-string ops) context idx))))

(defn- compatibility-reader-comment?
  [ops {:keys [source-length comment-marker] :as context} idx]
  (and (< idx source-length)
       (= comment-marker ((:char-string ops) context idx))))

(defn- compatibility-reader-skip-ignored
  [ops {:keys [source-length ^String source-text] :as context} idx]
  (loop [idx idx]
    (cond
      (>= idx source-length) idx
      ((:ignored? ops) ops context idx) (recur (inc idx))
      ((:comment? ops) ops context idx)
      (recur (loop [j idx]
               (if (and (< j source-length)
                        (not ((:line-terminator-var ops) (.charAt source-text j))))
                 (recur (inc j))
                 j)))
      :else idx)))

(defn- compatibility-reader-whitespace-token
  [ops {:keys [source-path ^String source-text line-starts source-length]
    :as context}
   idx token-index]
  (let [end (loop [end idx]
              (if (and (< end source-length)
                       ((:ignored? ops) ops context end))
                (recur (inc end))
                end))
        raw (subs source-text idx end)]
    [{:index token-index
      :kind :whitespace
      :lexeme raw
      :raw raw
      :decoded nil
      :trivia? true
      :span ((:span-var ops) source-path source-text
                                line-starts idx end)}
     end]))

(defn- compatibility-reader-comment-token
  [ops {:keys [source-path ^String source-text line-starts source-length]}
   idx token-index]
  (let [end (loop [end idx]
              (if (and (< end source-length)
                       (not ((:line-terminator-var ops)
                             (.charAt source-text end))))
                (recur (inc end))
                end))
        raw (subs source-text idx end)]
    [{:index token-index
      :kind :comment
      :lexeme raw
      :raw raw
      :decoded nil
      :trivia? true
      :span ((:span-var ops) source-path source-text
                                line-starts idx end)}
     end]))

(defn- compatibility-reader-atom-end
  [ops {:keys [source-length string-delimiter delimiters dispatch] :as context}
   idx]
  (loop [idx idx]
    (if (or (>= idx source-length)
            ((:ignored? ops) ops context idx)
            ((:comment? ops) ops context idx)
            (= string-delimiter ((:char-string ops) context idx))
            (contains? #{"'" "`" "~" "^" "@" "\\"}
                       ((:char-string ops) context idx))
            (contains? delimiters
                       ((:char-string ops) context idx))
            (contains? dispatch
                       ((:char-string ops) context idx)))
      idx
      (recur (inc idx)))))

(defn- compatibility-reader-string-token
  [ops {:keys [source-path ^String source-text line-starts source-length
           string-delimiter] :as context}
   idx token-index]
  (let [start idx]
    (loop [idx (inc idx)]
      (cond
        (>= idx source-length)
        ((:fail-var ops)
         "STAGE1READER003" source-path nil
         {:source-span ((:span-var ops) source-path source-text
                                           line-starts start idx)
          :token-id (keyword (str "tok-" token-index))
          :raw (subs source-text start idx)})

        (= string-delimiter ((:char-string ops) context idx))
        (let [end (inc idx)
              raw (subs source-text start end)
              span ((:span-var ops) source-path source-text
                                       line-starts start end)
              decoded ((:decode-string-var ops)
                       source-path raw span
                       (keyword (str "tok-" token-index)))]
          [{:index token-index
            :kind :string
            :lexeme decoded
            :raw raw
            :decoded decoded
            :span span}
           end])

        (= \\ (.charAt source-text idx))
        (if (< (inc idx) source-length)
          (recur (+ idx 2))
          ((:fail-var ops)
           "STAGE1READER003" source-path nil
           {:source-span ((:span-var ops) source-path source-text
                                             line-starts start idx)
            :token-id (keyword (str "tok-" token-index))
            :raw (subs source-text start idx)}))
        :else (recur (inc idx))))))

(defn- compatibility-reader-atom-token
  [ops {:keys [source-path ^String source-text line-starts] :as context}
   idx token-index]
  (let [start idx
        end ((:atom-end ops) ops context idx)
        token (subs source-text start end)
        kind ((:token-kind-var ops) token)
        token-id (keyword (str "tok-" token-index))
        span ((:span-var ops) source-path source-text
                                 line-starts start end)]
    [{:index token-index
      :kind kind
      :lexeme token
      :raw token
      :decoded ((:decode-atom-var ops) source-path kind token
                                          span token-id)
      :span span}
     end]))

(defn- compatibility-reader-character-token
  [ops {:keys [source-path ^String source-text line-starts source-length
           delimiters] :as context}
   idx token-index]
  (let [word-start (inc idx)
        end (cond
              (>= word-start source-length) word-start
              (or ((:ignored? ops) ops context word-start)
                  ((:comment? ops) ops context word-start))
              word-start
              (contains? delimiters
                         ((:char-string ops) context word-start))
              (inc word-start)
              :else ((:atom-end ops) ops context word-start))
        raw (subs source-text idx end)
        span ((:span-var ops) source-path source-text
                                 line-starts idx end)
        decoded ((:decode-character-var ops)
                 source-path raw span
                 (keyword (str "tok-" token-index)))]
    [{:index token-index
      :kind :character
      :lexeme decoded
      :raw raw
      :decoded decoded
      :span span}
     end]))

(defn- compatibility-reader-abbreviation-token
  [ops {:keys [source-path ^String source-text line-starts source-length]
    :as context}
   idx token-index]
  (let [splice? (and (= "~" ((:char-string ops) context idx))
                     (< (inc idx) source-length)
                     (= "@" ((:char-string ops) context (inc idx))))
        end (+ idx (if splice? 2 1))
        raw (subs source-text idx end)
        abbrev (case raw
                 "'" :quote
                 "`" :syntax-quote
                 "~" :unquote
                 "~@" :splice-unquote
                 "^" :metadata
                 "@" :deref)]
    [{:index token-index
      :kind :abbreviation
      :abbrev abbrev
      :lexeme raw
      :raw raw
      :decoded abbrev
      :span ((:span-var ops) source-path source-text
                                line-starts idx end)}
     end]))

(defn- compatibility-reader-dispatch-token
  [ops {:keys [source-path ^String source-text line-starts source-length dispatch]
    :as context}
   idx token-index]
  (let [dispatch-token ((:char-string ops) context idx)
        next-token (when (< (inc idx) source-length)
                     ((:char-string ops) context (inc idx)))
        dispatch-entry (get-in dispatch [dispatch-token next-token])]
    (if (= :set-open (:kind dispatch-entry))
      (let [end (+ idx 2)]
        [{:index token-index
          :kind (:kind dispatch-entry)
          :lexeme (str dispatch-token next-token)
          :raw (str dispatch-token next-token)
          :decoded :set
          :dispatch dispatch-token
          :close-token (:closes dispatch-entry)
          :span ((:span-var ops) source-path source-text
                                    line-starts idx end)}
         end])
      (let [end ((:atom-end ops) ops context (inc idx))
            raw (subs source-text idx end)
            tag (subs raw 1)]
        (if (contains? #{"inst" "uuid"} tag)
          [{:index token-index
            :kind :tag
            :tag (symbol tag)
            :lexeme raw
            :raw raw
            :decoded (symbol tag)
            :span ((:span-var ops) source-path source-text
                                      line-starts idx end)}
           end]
          ((:fail-var ops)
           "STAGE1READER004" source-path dispatch-token
           {:source-span
            ((:span-var ops) source-path source-text line-starts idx
                                (min source-length (max (inc idx) end)))
            :token-id (keyword (str "tok-" token-index))
            :raw raw
            :extension-tag (when (seq tag) (symbol tag))}))))))

(defn- compatibility-reader-delimiter-token
  [ops {:keys [source-path source-text line-starts] :as context}
   idx token-index delimiter]
  (let [token ((:char-string ops) context idx)]
    [{:index token-index
      :kind (:kind delimiter)
      :lexeme token
      :raw token
      :decoded token
      :close-token (:closes delimiter)
      :span ((:span-var ops) source-path source-text
                                line-starts idx (inc idx))}
     (inc idx)]))

(defn- compatibility-reader-next-token
  [ops {:keys [retain-trivia? string-delimiter delimiters dispatch] :as context}
   idx token-index]
  (let [token ((:char-string ops) context idx)
        delimiter (get delimiters token)]
    (cond
      (and retain-trivia? ((:ignored? ops) ops context idx))
      ((:whitespace-token ops) ops context idx token-index)

      (and retain-trivia? ((:comment? ops) ops context idx))
      ((:comment-token ops) ops context idx token-index)

      (= string-delimiter token)
      ((:string-token ops) ops context idx token-index)

      (= "\\" token)
      ((:character-token ops) ops context idx token-index)

      (contains? #{"'" "`" "~" "^" "@"} token)
      ((:abbreviation-token ops) ops context idx token-index)

      (contains? dispatch token)
      ((:dispatch-token ops) ops context idx token-index)

      delimiter
      ((:delimiter-token ops) ops context idx token-index delimiter)

      :else
      ((:atom-token ops) ops context idx token-index))))

(defn- compatibility-reader-tokenize
  [ops {:keys [retain-trivia? source-length source-path source-text table-id]
    :as context}]
  (loop [idx 0
         token-index 0
         tokens []]
    (let [idx (if retain-trivia?
                idx
                ((:skip-ignored ops) ops context idx))]
      (if (>= idx source-length)
        {:kind :gravity/stage1-reader-token-stream
         :source-path source-path
         :source-id (str "sha256:" ((:digest-var ops) source-text))
         :reader-table-id table-id
         :token-count (count tokens)
         :tokens tokens
         :status :complete}
        (let [[token-record next-idx]
              ((:next-token ops) ops context idx token-index)]
          (recur next-idx
                 (inc token-index)
                 (conj tokens token-record)))))))

(def ^:private compatibility-reader-token-operations
  {:char-string compatibility-reader-char-string
   :ignored? compatibility-reader-ignored?
   :comment? compatibility-reader-comment?
   :skip-ignored compatibility-reader-skip-ignored
   :whitespace-token compatibility-reader-whitespace-token
   :comment-token compatibility-reader-comment-token
   :atom-end compatibility-reader-atom-end
   :string-token compatibility-reader-string-token
   :atom-token compatibility-reader-atom-token
   :character-token compatibility-reader-character-token
   :abbreviation-token compatibility-reader-abbreviation-token
   :dispatch-token compatibility-reader-dispatch-token
   :delimiter-token compatibility-reader-delimiter-token
   :next-token compatibility-reader-next-token
   :tokenize compatibility-reader-tokenize})

(defn tokenize-compatibility-source
  "Tokenize already-decoded text through the hosted compatibility seed.

  Explicit owner Vars retain their late lookup and original failure behavior.
  Text, table, options, callbacks and output receive no authentication."
  [source-path source-text table reader-options operations]
  (let [ops (merge compatibility-reader-token-operations
                   (select-keys operations
                                [:line-starts-var :digest-var :line-terminator-var :span-var
                                 :fail-var :decode-string-var :token-kind-var :decode-atom-var
                                 :decode-character-var]))]
    (compatibility-reader-tokenize
     ops (compatibility-reader-token-context ops source-path source-text table reader-options))))

(defn- compatibility-form-products-context
  [reader-operations source-path source-text table token-stream]
  (let [expected-source-id (str "sha256:" ((:digest-var reader-operations) source-text))
        expected-table-id (str "sha256:" ((:digest-var reader-operations) (pr-str table)))
        tokens (:tokens token-stream)]
    (when-not (= :gravity/stage1-reader-token-stream (:kind token-stream))
      ((:pipeline-fail-var reader-operations) "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:kind]}))
    (when-not (= source-path (:source-path token-stream))
      ((:pipeline-fail-var reader-operations) "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:source-path]}))
    (when-not (= expected-source-id (:source-id token-stream))
      ((:pipeline-fail-var reader-operations) "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:source-id]}))
    (when-not (= expected-table-id (:reader-table-id token-stream))
      ((:pipeline-fail-var reader-operations) "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:reader-table-id]}))
    (when-not (and (vector? tokens)
                   (= (count tokens) (:token-count token-stream)))
      ((:pipeline-fail-var reader-operations) "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:tokens
                                                      :token-count]}))
    (let [line-starts ((:line-starts-var reader-operations) source-text)
          token-count (count tokens)
          token-index (delay ((:token-index-var reader-operations) tokens))
          nodes (atom {})
          form-order (atom [])
          next-form-index (atom 0)
          token-id (fn [token]
                     (or (:token-id token)
                         (keyword (str "tok-" (:index token)))))]
      {:source-path source-path
       :source-text source-text
       :line-starts line-starts
       :tokens tokens
       :token-count token-count
       :token-index token-index
       :nodes nodes
       :form-order form-order
       :next-form-index next-form-index
       :token-id token-id
       :reader-operations reader-operations})))

(defn- compatibility-form-new-form-id
  [{:keys [next-form-index form-order]}]
  (let [idx (swap! next-form-index inc)
        id (keyword (str "form-" (dec idx)))]
    (swap! form-order conj id)
    id))

(defn- compatibility-form-combined-span
  [{:keys [reader-operations source-path source-text line-starts]} start-token end-token]
  ((:span-var reader-operations) source-path source-text line-starts
                      (get-in start-token [:span :start :char])
                      (get-in end-token [:span :end :char])))

(defn- compatibility-form-skip-trivia
  [{:keys [tokens token-count token-id]} idx]
  (loop [idx idx
         trivia []]
    (if (and (< idx token-count)
             (true? (:trivia? (tokens idx))))
      (recur (inc idx)
             (conj trivia (token-id (tokens idx))))
      [idx trivia])))

(defn- compatibility-form-trivia-hash
  [{:keys [reader-operations token-index]} ids]
  (let [by-id @token-index]
    ((:canonical-hash-var reader-operations)
     (mapv (fn [id]
             (let [token (by-id id)]
               {:kind (:kind token)
                :raw (:raw token)
                :span (dissoc (:span token) :source :file)}))
           ids))))

(defn- compatibility-form-parse-token-literal
  [{:keys [reader-operations source-path token-id]} token]
  (if (contains? token :decoded)
    (:decoded token)
    ((:decode-atom-var reader-operations)
     source-path (:kind token) (:lexeme token) (:span token)
     (token-id token))))

(defn- compatibility-form-literal-set-key?
  [{:keys [nodes] :as context} node]
  (or (contains? #{:nil :boolean :integer :ratio :decimal
                   :string :character :symbol :keyword
                   :tagged-literal}
                 (:kind node))
      (and (contains? #{:list :vector :map :set
                        :abbreviation :metadata-wrapper}
                      (:kind node))
           (every? #(compatibility-form-literal-set-key?
                     context (@nodes %))
                   (:children node)))))

(defn- compatibility-form-set-value
  [{:keys [reader-operations source-path nodes] :as context} child-ids values]
  (loop [pairs (map vector child-ids values)
         seen {}]
    (if-let [[child-id value] (first pairs)]
      (let [node (@nodes child-id)]
        (if (and (compatibility-form-literal-set-key? context node)
                 (contains? seen value))
          (let [first-id (seen value)
                first-node (@nodes first-id)]
            ((:fail-var reader-operations)
             "C2-SET" source-path value
             {:source-span (:span node)
              :token-id (:open-token node)
              :form-id child-id
              :raw (:raw node)
              :related [{:role :first-literal
                         :span (:span first-node)
                         :artifact first-id}]
              :facts {:duplicate-value (pr-str value)
                      :first-form-id first-id
                      :duplicate-form-id child-id}}))
          (recur (rest pairs)
                 (if (compatibility-form-literal-set-key? context node)
                   (assoc seen value child-id)
                   seen))))
      (set values))))

(defn- compatibility-form-metadata-map
  [{:keys [reader-operations source-path]} value node]
  (cond
    (map? value) value
    (keyword? value) {value true}
    (or (symbol? value) (string? value)) {:tag value}
    :else
    ((:fail-var reader-operations)
     "C2-METADATA" source-path value
     {:source-span (:span node)
      :token-id (:open-token node)
      :form-id (:form-id node)
      :raw (:raw node)
      :facts {:metadata-kind (:kind node)}})))

(defn- compatibility-form-attach-metadata
  [{:keys [reader-operations source-path]} metadata target target-node]
  (if (instance? clojure.lang.IObj target)
    (with-meta target (merge (meta target) metadata))
    ((:fail-var reader-operations)
     "C2-METADATA" source-path target
     {:source-span (:span target-node)
      :token-id (:open-token target-node)
      :form-id (:form-id target-node)
      :raw (:raw target-node)
      :facts {:target-kind (:kind target-node)}})))

(declare compatibility-form-read-form)

(defn- compatibility-form-read-delimited
  [{:keys [reader-operations source-path source-text line-starts tokens token-count nodes token-id]
    :as context}
   idx collection-kind close-token open-token leading-trivia form-id]
  (loop [idx idx
         child-ids []
         values []]
    (let [[idx between-trivia]
          (compatibility-form-skip-trivia context idx)]
      (if (>= idx token-count)
        (let [eof-span ((:span-var reader-operations)
                        source-path source-text line-starts
                        (count source-text) (count source-text))]
          ((:fail-var reader-operations)
           "STAGE1READER002" source-path nil
           {:source-span eof-span
            :token-id (token-id open-token)
            :form-id form-id
            :raw (:raw open-token)
            :expected close-token
            :facts {:expected-delimiter close-token
                    :open-token
                    (token-id open-token)}}))
        (let [token (tokens idx)]
          (cond
            (and (= :close (:kind token))
                 (= close-token (:lexeme token)))
            (let [span (compatibility-form-combined-span
                        context open-token token)
                  value (case collection-kind
                          :list (apply list values)
                          :vector (vec values)
                          :set (compatibility-form-set-value
                                context child-ids values)
                          :map (do
                                 (when (odd? (count values))
                                   ((:fail-var reader-operations)
                                    "STAGE1READER005" source-path values
                                    {:source-span span
                                     :token-id
                                     (token-id token)
                                     :form-id form-id
                                     :raw (:raw token)
                                     :facts {:entry-count (count values)}}))
                                 (apply hash-map values)))
                  node {:form-id form-id
                        :kind collection-kind
                        :collection-kind collection-kind
                        :open-token
                        (token-id open-token)
                        :close-token
                        (token-id token)
                        :children child-ids
                        :span span
                        :metadata {}
                        :origin {:kind :source
                                 :reader :stage1-token-parser}
                        :leading-trivia-token-ids leading-trivia
                        :leading-trivia-hash
                        (compatibility-form-trivia-hash
                         context leading-trivia)
                        :trailing-trivia-token-ids between-trivia
                        :trailing-trivia-hash
                        (compatibility-form-trivia-hash
                         context between-trivia)
                        :raw (subs source-text
                                   (get-in open-token [:span :start :char])
                                   (get-in token [:span :end :char]))
                        :value value}]
              (swap! nodes assoc form-id node)
              [value (inc idx) span form-id])

            (= :close (:kind token))
            ((:fail-var reader-operations)
             "STAGE1READER001" source-path (:lexeme token)
             {:source-span (:span token)
              :token-id (token-id token)
              :form-id form-id
              :raw (:raw token)
              :expected close-token
              :actual (:lexeme token)
              :facts {:expected-delimiter close-token
                      :actual-delimiter (:lexeme token)}})

            :else
            (let [[value next-idx _span child-id]
                  (compatibility-form-read-form
                   context idx between-trivia)]
              (recur next-idx
                     (conj child-ids child-id)
                     (conj values value)))))))))

(defn- compatibility-form-read-abbreviation
  [{:keys [reader-operations source-path source-text tokens token-count nodes token-id] :as context}
   idx token leading-trivia form-id]
  (let [abbrev (:abbrev token)
        [metadata-value metadata-end _ metadata-id]
        (when (= :metadata abbrev)
          (let [[child-idx child-trivia]
                (compatibility-form-skip-trivia context (inc idx))]
            (when (>= child-idx token-count)
              ((:fail-var reader-operations)
               "C2-METADATA" source-path abbrev
               {:source-span (:span token)
                :token-id (token-id token)
                :form-id form-id
                :raw (:raw token)
                :facts {:abbreviation abbrev}}))
            (compatibility-form-read-form
             context child-idx child-trivia)))
        next-start (if (= :metadata abbrev)
                     metadata-end
                     (inc idx))
        [child-idx child-trivia]
        (compatibility-form-skip-trivia context next-start)]
    (when (>= child-idx token-count)
      ((:fail-var reader-operations)
       (if (= :metadata abbrev) "C2-METADATA" "C2-ABBREV")
       source-path abbrev
       {:source-span (:span token)
        :token-id (token-id token)
        :form-id form-id
        :raw (:raw token)
        :facts {:abbreviation abbrev}}))
    (let [[child-value next-idx child-span child-id]
          (compatibility-form-read-form context child-idx child-trivia)
          child-node (@nodes child-id)
          metadata-node (when metadata-id (@nodes metadata-id))
          metadata (when metadata-id
                     (compatibility-form-metadata-map
                      context metadata-value metadata-node))
          value (case abbrev
                  :quote (list 'quote child-value)
                  :syntax-quote (list 'syntax-quote child-value)
                  :unquote (list 'unquote child-value)
                  :splice-unquote (list 'splice-unquote child-value)
                  :deref (list 'deref child-value)
                  :metadata (compatibility-form-attach-metadata
                             context metadata child-value child-node))
          span (compatibility-form-combined-span
                context token (tokens (dec next-idx)))
          children (cond-> []
                     metadata-id (conj metadata-id)
                     true (conj child-id))
          node {:form-id form-id
                :kind (if (= :metadata abbrev)
                        :metadata-wrapper
                        :abbreviation)
                :abbrev abbrev
                :open-token (token-id token)
                :close-token (:close-token child-node)
                :children children
                :span span
                :surface-span (:span token)
                :metadata (or metadata {})
                :origin {:kind :source :reader :stage1-token-parser}
                :generated-origin
                [{:kind :generated
                  :producer :reader-abbreviation
                  :reason abbrev
                  :from (:span token)
                  :child-form-id child-id}]
                :leading-trivia-token-ids leading-trivia
                :leading-trivia-hash
                (compatibility-form-trivia-hash context leading-trivia)
                :raw (subs source-text
                           (get-in token [:span :start :char])
                           (get-in child-span [:end :char]))
                :expanded-form value
                :value value}]
      (swap! nodes assoc form-id node)
      [value next-idx span form-id])))

(defn- compatibility-form-read-tagged
  [{:keys [reader-operations source-path source-text tokens token-count nodes token-id] :as context}
   idx token leading-trivia form-id]
  (let [[child-idx child-trivia]
        (compatibility-form-skip-trivia context (inc idx))]
    (when (>= child-idx token-count)
      ((:fail-var reader-operations)
       "STAGE1READER004" source-path (:raw token)
       {:source-span (:span token)
        :token-id (token-id token)
        :form-id form-id
        :raw (:raw token)
        :extension-tag (:tag token)}))
    (let [[child-value next-idx child-span child-id]
          (compatibility-form-read-form context child-idx child-trivia)
          child-node (@nodes child-id)
          raw (str (:raw token) " " (pr-str child-value))
          value (try
                  ((:read-tagged-var reader-operations) raw)
                  (catch Exception ex
                    ((:fail-var reader-operations)
                     "STAGE1READER004" source-path raw
                     {:source-span (:span token)
                      :token-id (token-id token)
                      :form-id form-id
                      :raw raw
                      :extension-tag (:tag token)
                      :cause-message (.getMessage ex)})))
          span (compatibility-form-combined-span
                context token (tokens (dec next-idx)))
          node {:form-id form-id
                :kind :tagged-literal
                :tag (:tag token)
                :open-token (token-id token)
                :close-token (:close-token child-node)
                :children [child-id]
                :span span
                :metadata {}
                :origin {:kind :source :reader :stage1-token-parser}
                :leading-trivia-token-ids leading-trivia
                :leading-trivia-hash
                (compatibility-form-trivia-hash context leading-trivia)
                :raw (subs source-text
                           (get-in token [:span :start :char])
                           (get-in child-span [:end :char]))
                :value value}]
      (swap! nodes assoc form-id node)
      [value next-idx span form-id])))

(defn- compatibility-form-read-literal
  [{:keys [nodes token-id] :as context} idx token leading-trivia form-id]
  (let [value (compatibility-form-parse-token-literal context token)
        span (:span token)
        node {:form-id form-id
              :kind (:kind token)
              :open-token (token-id token)
              :close-token (token-id token)
              :token-id (token-id token)
              :children []
              :span span
              :metadata {}
              :origin {:kind :source :reader :stage1-token-parser}
              :leading-trivia-token-ids leading-trivia
              :leading-trivia-hash
              (compatibility-form-trivia-hash context leading-trivia)
              :raw (:raw token)
              :value value}]
    (swap! nodes assoc form-id node)
    [value (inc idx) span form-id]))

(defn- compatibility-form-read-form
  [{:keys [reader-operations source-path tokens token-count token-id] :as context}
   idx leading-trivia]
  (when (>= idx token-count)
    ((:fail-var reader-operations) "STAGE1READER002" source-path nil
                         {:source-span {:source source-path}
                          :expected :form}))
  (let [token (tokens idx)
        form-id (compatibility-form-new-form-id context)]
    (case (:kind token)
      :close
      ((:fail-var reader-operations)
       "STAGE1READER001" source-path (:lexeme token)
       {:source-span (:span token)
        :token-id (token-id token)
        :form-id form-id
        :raw (:raw token)
        :actual (:lexeme token)
        :facts {:actual-delimiter (:lexeme token)}})

      :list-open
      (compatibility-form-read-delimited
       context (inc idx) :list (:close-token token)
       token leading-trivia form-id)

      :vector-open
      (compatibility-form-read-delimited
       context (inc idx) :vector (:close-token token)
       token leading-trivia form-id)

      :map-open
      (compatibility-form-read-delimited
       context (inc idx) :map (:close-token token)
       token leading-trivia form-id)

      :set-open
      (compatibility-form-read-delimited
       context (inc idx) :set (:close-token token)
       token leading-trivia form-id)

      :abbreviation
      (compatibility-form-read-abbreviation
       context idx token leading-trivia form-id)

      :tag
      (compatibility-form-read-tagged
       context idx token leading-trivia form-id)

      (compatibility-form-read-literal
       context idx token leading-trivia form-id))))

(defn- compatibility-form-products
  [{:keys [reader-operations token-count nodes form-order] :as context}]
  (loop [idx 0
         form-index 0
         root-ids []
         records []]
    (let [[idx leading-trivia]
          (compatibility-form-skip-trivia context idx)]
      (if (>= idx token-count)
        (let [raw-tree (mapv @nodes @form-order)
              parent-by-child
              (reduce (fn [parents parent]
                        (reduce #(assoc %1 %2 (:form-id parent))
                                parents
                                (:children parent)))
                      {}
                      raw-tree)
              form-tree
              (mapv #(assoc % :parent-form-id
                            (get parent-by-child (:form-id %)))
                    raw-tree)
              node-by-id (into {} (map (juxt :form-id identity)
                                       form-tree))
              records
              (mapv (fn [record]
                      (assoc record
                             :kind (:kind (node-by-id (:form-id record)))
                             :parent-form-id nil))
                    records)]
          {:records records
           :form-tree form-tree
           :root-form-ids root-ids
           :parsed-values (mapv :form records)})
        (let [[form next-idx span form-id]
              (compatibility-form-read-form context idx leading-trivia)]
          (recur next-idx
                 (inc form-index)
                 (conj root-ids form-id)
                 (conj records
                       {:form form
                        :kind ((:form-kind-var reader-operations) form)
                        :form-id form-id
                        :span (assoc span :form-index form-index)})))))))

(defn build-compatibility-forms
  "Build hosted form products from explicit native tokens and late owner Vars.

  Existing joins check consistency; they authenticate no input or owner."
  [source-path source-text table token-stream operations]
  (compatibility-form-products
   (compatibility-form-products-context
    (select-keys operations
                 [:digest-var :pipeline-fail-var :line-starts-var :token-index-var
                  :span-var :canonical-hash-var :decode-atom-var :fail-var
                  :read-tagged-var :form-kind-var])
    source-path source-text table token-stream)))

(defn classify-compatibility-token
  [starts-with-var token]
  (cond
    (= token "nil") :nil
    (= token "true") :boolean
    (= token "false") :boolean
    (starts-with-var token ":") :keyword
    (re-matches #"[+-]?(?:0[xX][0-9A-Fa-f]+|0[bB][01]+|[0-9]+)" token)
    :integer
    (re-matches #"[+-]?[0-9]+/[0-9]+" token) :ratio
    (re-matches #"[+-]?(?:(?:(?:[0-9]+\.[0-9]*)|(?:[0-9]*\.[0-9]+))(?:[eE][+-]?[0-9]+)?|(?:[0-9]+[eE][+-]?[0-9]+))"
                token)
    :decimal
    (re-find #"^[+-]?[0-9]" token) :malformed-number
    :else :symbol))


(defn reject-compatibility-numeric!
  [fail-var source-path raw span token-id cause]
  (fail-var
   "STAGE1READER007" source-path raw
   {:source-span span
    :token-id token-id
    :raw raw
    :cause-message cause
    :facts {:literal-kind :numeric
            :raw-spelling raw}}))


(defn decode-compatibility-atom
  [{:keys [starts-with-var split-var malformed-numeric-var]} source-path kind raw span token-id]
  (try
    (case kind
      :nil nil
      :boolean (= "true" raw)
      :keyword (keyword (subs raw 1))
      :integer
      (let [negative? (starts-with-var raw "-")
            positive? (starts-with-var raw "+")
            unsigned (if (or negative? positive?) (subs raw 1) raw)
            [radix digits] (cond
                             (re-find #"^0[xX]" unsigned)
                             [16 (subs unsigned 2)]
                             (re-find #"^0[bB]" unsigned)
                             [2 (subs unsigned 2)]
                             :else [10 unsigned])
            value (bigint (java.math.BigInteger. digits radix))]
        (if negative? (- value) value))
      :ratio
      (let [[numerator-spelling denominator-spelling] (split-var raw #"/" 2)
            numerator (bigint numerator-spelling)
            denominator (bigint denominator-spelling)]
        (if (zero? denominator)
          {:artifact :gravity/deferred-ratio-literal
           :kind :ratio
           :raw raw
           :numerator-spelling numerator-spelling
           :denominator-spelling denominator-spelling
           :numerator numerator
           :denominator denominator
           :semantic-validation :deferred
           :reason :zero-denominator}
          (/ numerator denominator)))
      :decimal (Double/parseDouble raw)
      :malformed-number
      (malformed-numeric-var
       source-path raw span token-id
       "numeric lexeme does not match a declared Gravity numeric shape")
      :symbol (symbol raw)
      raw)
    (catch NumberFormatException ex
      (malformed-numeric-var source-path raw span token-id
                                        (.getMessage ex)))
    (catch ArithmeticException ex
      (malformed-numeric-var source-path raw span token-id
                                        (.getMessage ex)))))


(defn decode-compatibility-string
  [read-string-var fail-var source-path raw span token-id]
  (try
    (binding [*read-eval* false]
      (read-string-var raw))
    (catch Exception ex
      (fail-var "STAGE1READER003" source-path raw
                           {:source-span span
                            :token-id token-id
                            :raw raw
                            :cause-message (.getMessage ex)}))))


(defn decode-compatibility-character
  [read-string-var fail-var source-path raw span token-id]
  (try
    (let [value (binding [*read-eval* false]
                  (read-string-var raw))]
      (when-not (char? value)
        (throw (ex-info "reader character token did not decode to a character"
                        {:raw raw})))
      value)
    (catch Exception ex
      (fail-var "STAGE1READER003" source-path raw
                           {:source-span span
                            :token-id token-id
                            :raw raw
                            :cause-message (.getMessage ex)}))))
(doseq
 [name (keys (:public-api namespace-contract))
  :let [operation (get (ns-interns *ns*) name)]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
