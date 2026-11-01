(declare ^:private p15-s23-c6c10-digest-validated-form)

(defn- sh03-reader-authenticated-envelope-options
  ([source-path invocation] (sh03-reader-authenticated-envelope-options source-path invocation nil))
  ([source-path invocation expected-binding]
  (when (some? invocation)
    (when-not
     (and (= :passed (:status (p15-s23-trusted-carrier-validation invocation :default-only)))
          (= #{:reader-source-path :authenticated-envelope-options} (set (keys invocation)))
          (string? (:reader-source-path invocation))
          (= {:source :development-current} (:authenticated-envelope-options invocation)))
      (sh03-reader-boundary-fail!
       source-path :explicit-sh03-reader-invocation-source-selection nil {}))
    (sh03-reader-invocation-source! source-path (:reader-source-path invocation) expected-binding)
    (:authenticated-envelope-options invocation))))

(defn- sh03-reader-validate-envelope-options! [source-path options]
  (when (some? options)
    (when-not (and (= :passed (:status (p15-s23-trusted-carrier-validation options :default-only)))
                   (= {:source :development-current} options))
      (sh03-reader-boundary-fail!
       source-path :explicit-sh03-authenticated-envelope-selection nil {})))
  options)

(defn- sh03-reader-verification-invocation! [source-path invocation]
  (if (nil? invocation)
    {:binding (sh03-reader-current-binding! source-path) :envelope-options nil}
    (do
      (sh03-reader-authenticated-envelope-options source-path invocation)
      {:binding (sh03-reader-current-binding! source-path (:reader-source-path invocation))
       :envelope-options (:authenticated-envelope-options invocation)})))

(defn sh03-reader-canonical-identity [source-path value]
  (p15-s23-c6c10-canonical-identity source-path value))

(defn sh03-reader-canonical-digest [source-path value]
  (sh03-reader-canonical-stream-digest source-path value))

(defn- sh03-reader-resolve-digest-request-sequence!
  [source-path raw-result source-bytes]
  (let [requests (:digest-requests raw-result)
        request-count (count requests)
        expected-names
        (if (= :accepted (:status raw-result))
          sh03-reader-accepted-digest-request-names
          sh03-reader-rejected-digest-request-names)]
    (when-not (= expected-names (mapv :key requests))
      (sh03-reader-boundary-fail!
       source-path :exact-sh03-reader-digest-request-set
       (mapv :key requests) {:expected expected-names}))
    (loop [ordinal 0
           digests []
           resolved-requests []]
      (if (= ordinal request-count)
        {:resolved-digests digests :resolved-requests resolved-requests}
        (let [request (get requests ordinal)
              expected-keys (if (zero? ordinal)
                              sh03-reader-source-digest-request-keys
                              sh03-reader-digest-request-keys)]
          (when-not
           (and (map? request)
                (= expected-keys (set (keys request)))
                (= ordinal (:ordinal request))
                (= (get expected-names ordinal) (:key request))
                (= :sha256 (:algorithm request))
                (vector? (:depends-on request)))
            (sh03-reader-boundary-fail!
             source-path :exact-sh03-reader-digest-request-schema
             (dissoc request :preimage) {:ordinal ordinal}))
          (let [references
                (p15-s23-c6c10-collect-digest-ref-ordinals!
                 source-path (:preimage request) request-count ordinal)
                dependencies (:depends-on request)]
            (when-not
             (and (= dependencies (vec (sort (distinct dependencies))))
                  (= (set dependencies) (set references))
                  (every? #(and (integer? %) (<= 0 %) (< % ordinal))
                          dependencies))
              (sh03-reader-boundary-fail!
               source-path :prior-only-sh03-reader-digest-dependencies
               request {:ordinal ordinal :references references}))
            (let [resolved-preimage
                  (p15-s23-c6c10-resolve-digest-references!
                   source-path (:preimage request) request-count ordinal digests)
                  raw-request? (zero? ordinal)
                  _
                  (when-not (= (if raw-request?
                                 :raw-byte-vector-v1
                                 :gravity-canonical-edn-v1)
                               (:encoding request))
                    (sh03-reader-boundary-fail!
                     source-path :declared-sh03-reader-digest-encoding
                     request {:ordinal ordinal}))
                  _
                  (when (and raw-request?
                             (not= (sh03-reader-byte-vector source-bytes)
                                   resolved-preimage))
                    (sh03-reader-boundary-fail!
                     source-path :source-bound-sh03-reader-digest-request
                     request {:ordinal ordinal}))
                  digest
                  (if raw-request?
                    (str "sha256:"
                         (sha256-bytes-hex
                          (sh03-reader-byte-array resolved-preimage)))
                    ;; Validate the complete original-order preimage, then
                    ;; stream the same canonical EDN v1 bytes for its digest.
                    (sh03-reader-canonical-digest source-path resolved-preimage))]
              ;; Equal canonical forms have identical fixed encoding and digest.
              ;; Digest uniqueness therefore also rejects equal forms; this
              ;; implication does not require the hash to be injective.
              (when (or (some #{digest} digests)
                        (and raw-request? (not= digest (:observed-id request))))
                (sh03-reader-boundary-fail!
                 source-path :unique-bound-sh03-reader-digest
                 request {:ordinal ordinal :digest digest}))
              (recur
               (inc ordinal)
               (conj digests digest)
               (conj resolved-requests
                     (assoc request
                            :preimage resolved-preimage
                            :digest digest))))))))))

(defn sh03-reader-resolve-digest-requests!
  [source-path raw-result source-bytes]
  ;; Complete the identity-checking frame before allocating the final result.
  (let [{:keys [resolved-digests resolved-requests]}
        (sh03-reader-resolve-digest-request-sequence! source-path raw-result source-bytes)
        resolved-result
        (p15-s23-c6c10-resolve-digest-references!
         source-path (assoc raw-result :digest-requests [])
         (count resolved-requests) nil resolved-digests)
        resolved-result
        (assoc resolved-result :digest-requests resolved-requests
               :incremental-reader-hashes
               (assoc (:incremental-reader-hashes resolved-result) :status
                      (if (= :accepted (:status raw-result)) :stable :rejected)))]
    {:result resolved-result :resolved-digests resolved-digests
     :resolved-requests resolved-requests :reader-result-id (last resolved-digests)}))

(defn sh03-reader-resolve-diagnostic-id
  [source-path diagnostic]
  (diagnostics/resolve-request-id
   #'sh03-reader-canonical-digest
   (fn [path value]
     (sh03-reader-boundary-fail! path :sh03-reader-diagnostic-id-request value {}))
   source-path diagnostic))

(defn sh03-reader-resolved-result!
  ([source-path source-bytes project-context reader-options]
   (sh03-reader-resolved-result! source-path source-bytes project-context reader-options nil))
  ([source-path source-bytes project-context reader-options reader-source-path]
  (when-not (qst-or-gravity-source? source-path)
    (try
      (source-path-policy-fail! source-path source-bytes)
      (catch clojure.lang.ExceptionInfo ex
        (c2-reader-remap-exception! source-path ex))))
  (let [binding (if (nil? reader-source-path) (sh03-reader-current-binding! source-path)
                 (sh03-reader-current-binding! source-path reader-source-path))
        plan (:plan binding)
        source-unit
        (sh03-reader-input-source-unit
         source-path source-bytes project-context)
        reader-policy (sh03-reader-input-policy reader-options)
        source-byte-vector (sh03-reader-byte-vector source-bytes)
        arguments [source-unit source-byte-vector reader-policy]
        raw-result
        (sh03-reader-execute-plan!
         source-path plan sh03-reader-entrypoint arguments)
        _ (sh03-reader-result-preflight!
           source-path source-unit reader-policy raw-result)
        verifier-result
        (sh03-reader-execute-plan!
         source-path plan sh03-reader-verifier
         [source-unit source-byte-vector reader-policy raw-result])
        _ (sh03-reader-verifier-preflight!
           source-path raw-result verifier-result)
        resolved
        (sh03-reader-resolve-digest-requests!
         source-path raw-result source-bytes)
        result
        (update (:result resolved) :diagnostics
                #(mapv (partial sh03-reader-resolve-diagnostic-id source-path)
                       %))]
    (assoc resolved
           :result result
           :raw-result raw-result
           :verification-report verifier-result
           :invocation-context
           (when (some? reader-source-path)
             {:reader-source-path (:source-path binding)
              :authenticated-envelope-options {:source :development-current}})
           :plan-binding (dissoc binding :plan)))))

(defn sh03-reader-decode-raw-bytes!
  [source-path raw bytes]
  (try
    (str
     (.decode
      (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
        (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
        (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
      (java.nio.ByteBuffer/wrap bytes)))
    (catch java.nio.charset.CharacterCodingException error
      (sh03-reader-boundary-fail!
       source-path :valid-sh03-reader-raw-utf8 raw
       {:cause-message (.getMessage error)}))))

(defn sh03-reader-source-scalar-boundaries!
  [source-path source-text source-bytes]
  (loop [utf16-index 0
         byte-index 0
         scalar-index 0
         boundaries {0 0}]
    (if (= utf16-index (.length source-text))
      (do
        (when-not (= byte-index (alength source-bytes))
          (sh03-reader-boundary-fail!
           source-path :complete-sh03-reader-source-scalar-index
           {:observed-byte-count byte-index}
           {:expected-byte-count (alength source-bytes)}))
        boundaries)
      (let [codepoint (.codePointAt source-text utf16-index)
            utf16-width (Character/charCount codepoint)
            utf8-width (cond
                         (<= codepoint 0x7f) 1
                         (<= codepoint 0x7ff) 2
                         (<= codepoint 0xffff) 3
                         :else 4)
            next-byte (+ byte-index utf8-width)]
        (recur (+ utf16-index utf16-width)
               next-byte
               (inc scalar-index)
               (assoc boundaries next-byte (inc scalar-index)))))))

;; Trusted HOST connection only; none of these functions are Gravity builtins.
;; The source sandbox cannot manufacture the original issuer or scope objects.
(require '[gravity.build.declared-inputs])
(declare compiler-c2-reader-source-artifact sh03-reader-codepoints-text! standard-reader-options)
(declare ^:private sh03-c3-precomputed-c2-verify! sh03-reader-internal-product-authority)
(def ^:private ^:dynamic *reader-resource-invocation* nil)

(defn- reader-resource-policy []
  {:artifact :gravity/sh03-reader-policy :schema-version 1 :retain-trivia true
   :enabled-reader-tags [[114 101 115 111 117 114 99 101]]
   :extension-policy :gravity/declared-input-reader})

(defn- reader-resource-policy-id []
  (reader-canonical-hash
   {:policy (reader-resource-policy) :handler 'gravity.reader/read-input
    :provider 'gravity.build/hermetic-inputs :provider-version "1"
    :profile :meta :target :jvm :phase :build :lifetime :invocation
    :effects #{:build/read-file} :capabilities #{:fs/read}
    :audit :required :replay :recorded}))

(defn- reader-resource-options? [options]
  (= (assoc standard-reader-options :extension-policy (reader-resource-policy-id)) options))

(defn- reader-resource-admission-required? [artifact]
  (or (#'reader-pure-text/marker-at?
       artifact [:source-unit-record :reader-options :extension-policy] (reader-resource-policy-id))
      (#'reader-pure-text/marker-at?
       artifact [:reader-extension-invocation-records :gravity.reader.pure-text/each :schema-version] 3)
      (#'reader-pure-text/marker-at? artifact [:form-tree :gravity.reader.pure-text/each :tag] 'resource)))

(defn- reader-resource-context! [source-path source-text project-context]
  (sh03-reader-require-result-carrier!
   source-path :resource-original-source
   {:source-path source-path :source-text source-text :project-context project-context})
  (let [context *reader-resource-invocation*]
    (when-not (and context
                   (= source-path (:source-path context))
                   (= source-text (:source-text context))
                   (= project-context (:project-context context)))
      (sh03-reader-boundary-fail! source-path :original-resource-invocation {} {}))
    ;; Recheck original Object membership, thread and lifetime, never a flag.
    (#'gravity.build.declared-inputs/live! (:capability context))
    context))

(defn- reader-resource-handler-binding! [request-source]
  (let [path "bootstrap/gravity/src/gravity/reader.gravity"
        bytes (java.nio.file.Files/readAllBytes (.toPath (io/file path)))
        text (sh03-reader-strict-source-text! request-source path bytes)
        source-hash (str "sha256:" (sha256-bytes-hex bytes))
        macro-artifact (macro-source-artifact path text)
        module-base (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))
        table (stage0-function-table module-base)
        definition (get table 'read-input)
        _ (when-not (and (= 'gravity.reader (:module module-base))
                          (= :meta (:profile module-base)) (= :jvm (:target module-base))
                          (= #{:build/read-file} (:effects module-base))
                          (= #{:fs/read} (:capabilities module-base))
                          (= :safe (:safety module-base))
                          (= '[read-input] (:exports module-base))
                          (= #{'read-input} (set (keys table)))
                          (= '[seed] (:params definition)))
            (sh03-reader-boundary-fail! request-source :fixed-resource-handler-module {} {}))
        module (assoc module-base :function-table table)
        emitter-binding (c-backend-stage2-plan-emitter-source-rule! path :jvm)
        emitter (p15-s23-stage2-compiler-artifact-augmented-emitter (:emitter emitter-binding))
        function (select-keys (p15-s23-stage2-seed-compile-function emitter module definition)
                              sh03-reader-function-keys)
        functions {'read-input function}
        audit (sh03-reader-plan-instruction-summary path functions)
        plan-base {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
                   :entrypoint 'read-input
                   :source {:sha256 source-hash :byte-count (alength bytes)}
                   :compiler {:owner :gravity-source :compiled-by :clojure-stage0-seed
                              :emitter-source-rule-hash (:source-rule-hash emitter-binding)
                              :generic-bridge-residual? true :self-hosted? false}
                   :module (select-keys module [:module :profile :target :effects :capabilities :exports :safety])
                   :functions functions :instruction-summary (:instruction-summary audit)
                   :effect-summary {:declared #{:build/read-file} :inferred #{:build/read-file}
                                    :capabilities #{:fs/read}}}
        _ (sh03-reader-require-result-carrier! path :resource-handler-plan plan-base)
        plan (assoc plan-base :plan-id (reader-canonical-hash plan-base))
        ;; Retain the ORIGINAL compiled occurrence, not a reconstructed map.
        occurrences (filter #(and (map? %) (= :builtin-call (:op %))
                                   (= 'build-read-declared-input (:function %)))
                            (tree-seq coll? seq (:instructions function)))]
    (sh03-reader-require-result-carrier! path :resource-handler-plan plan)
    (when-not (and (= 1 (count occurrences)) (= 1 (:arity function))
                   (= #{:build/read-file} (get-in function [:binding :effects]))
                   (= #{:fs/read} (get-in function [:binding :capabilities])))
      (sh03-reader-boundary-fail! request-source :fixed-resource-handler-occurrence {} {}))
    (when-not (java.util.Arrays/equals
               ^bytes bytes ^bytes (java.nio.file.Files/readAllBytes (.toPath (io/file path))))
      (sh03-reader-boundary-fail! request-source :unchanged-resource-handler-source {} {}))
    {:plan plan :instruction (first occurrences)
     :identity {:source-content-hash source-hash :plan-id (:plan-id plan)
                :emitter-source-rule-hash (:source-rule-hash emitter-binding)}}))

(defn- reader-resource-prepare! [source-path source-text bytes project-context binding]
  (let [unit (sh03-reader-input-source-unit source-path bytes project-context)
        byte-vector (sh03-reader-byte-vector bytes)
        plan (:plan binding)
        preparation (sh03-reader-execute-plan! source-path plan 'reader-prepare-resources [unit byte-vector])]
    (sh03-reader-require-result-carrier! source-path :resource-preparation preparation)
    (let [report (sh03-reader-execute-plan!
                  source-path plan 'reader-verify-resource-preparation [unit byte-vector preparation])]
      (sh03-reader-require-result-carrier! source-path :resource-preparation-verification report)
      (when-not (and (= :prepared (:status preparation)) (= :accepted (:status report))
                     (= unit (:source-unit preparation)) (= byte-vector (:source-bytes preparation))
                     (= (str "sha256:" (sha256-bytes-hex bytes)) (:source-content-hash unit))
                     (= source-text (sh03-reader-strict-source-text! source-path source-path bytes)))
        (sh03-reader-boundary-fail! source-path :original-byte-resource-preparation {} {})))
    {:source-unit unit :source-bytes byte-vector :preparation preparation}))

(defn- reader-resource-requests! [capability prepared implementation]
  (let [grant (:grant (#'gravity.build.declared-inputs/live! capability))
        unit (:source-unit prepared)
        policy (reader-resource-policy)
        source-id (sh03-reader-canonical-digest "<resource-source>"
                   {:domain :gravity/sh03-source-unit-v1 :project-root-id (:project-root-id unit)
                    :logical-source-id (:logical-source-id unit)
                    :source-content-id (:source-content-hash unit) :encoding :utf-8 :reader-policy policy})
        _ (when-not (and (= implementation (:implementation grant))
                          (= (reader-resource-policy-id) (:policy-id grant)))
            (sh03-reader-boundary-fail! "<resource-input>" :current-resource-implementation-and-policy {} {}))
        requests
        (mapv (fn [resource]
                (merge (select-keys grant [:principal :provider :provider-version :profile :target
                                          :phase :lifetime :handler :implementation :policy-id])
                       {:source-id source-id :source-content-hash (:source-content-hash unit)
                        :form-id (:form-id resource) :payload-form-id (:payload-form-id resource)
                        :span (:span resource)
                        :input (sh03-reader-codepoints-text! "<resource-input>" (:input-codepoints resource))}))
              (get-in prepared [:preparation :resources]))]
    ;; This operation checks the ENTIRE population before any handler executes.
    (gravity.build.declared-inputs/preflight! capability requests)
    requests))

(defn- reader-resource-invocations! [context prepared requests mode]
  (let [{:keys [runtime handler scope]} context]
    (mapv
     (fn [resource request]
       (let [seed {:source-id (:source-id request) :source-content-id (:source-content-hash request)
                   :form-id (:form-id resource) :payload-form-id (:payload-form-id resource)
                   :span (:span resource) :datum (:input request)}
             returned (#'gravity.build.reader-bridge/call-with-form
                       scope request mode
                       #(p15-s23-stage2-runtime-execute-function-scoped
                         runtime (:plan handler) 'read-input [seed] scope))]
         (sh03-reader-require-result-carrier! (:source-path context) :resource-returned-seed returned)
         ;; Require the original private receipt independently of the handler's
         ;; return path. This lookup verifies only; it never supplies the seed
         ;; datum or accesses the input store/provider.
         (let [original (#'gravity.build.reader-bridge/call-with-form
                         scope request :verify
                         #(gravity.build.reader-bridge/read!
                           runtime (:plan handler) (:instruction handler) [(:input request)] scope))]
         (when-not (and (= String (class (:datum returned)))
                        (= original (:datum returned))
                        (= returned (assoc seed :datum (:datum returned))))
           (sh03-reader-boundary-fail! (:source-path context) :original-resource-returned-seed {} {})))
         {:form-id (:form-id resource) :payload-form-id (:payload-form-id resource)
          :span (:span resource) :raw (:raw resource) :input-seed seed :returned-seed returned
          :invocation {:handler 'gravity.reader/read-input :policy (reader-resource-policy)
                       :request request :build-effects #{:build/read-file} :capabilities #{:fs/read}
                       :status :invoked}}))
     (get-in prepared [:preparation :resources]) requests)))

(defn- reader-resource-resolved-result! [source-path source-text bytes project-context]
  (let [context (reader-resource-context! source-path source-text project-context)
        binding (sh03-reader-current-binding! source-path sh03-reader-source-relative-path)
        fresh-handler (reader-resource-handler-binding! source-path)
        implementation (reader-canonical-hash
                        {:reader (dissoc binding :plan) :handler (:identity fresh-handler)})
        _ (when-not (and (= (dissoc binding :plan) (dissoc (:binding context) :plan))
                          (= (:identity fresh-handler) (get-in context [:handler :identity]))
                          (= implementation (:implementation context)))
            (sh03-reader-boundary-fail! source-path :unchanged-original-resource-plans {} {}))
        prepared (reader-resource-prepare! source-path source-text bytes project-context binding)
        requests (reader-resource-requests! (:capability context) prepared implementation)
        _ (when-not (= requests (:requests context))
            (sh03-reader-boundary-fail! source-path :complete-original-resource-requests {} {}))
        actual (reader-resource-invocations! context prepared requests (:mode context))
        {:keys [source-unit source-bytes preparation]} prepared
        plan (:plan binding)
        raw (sh03-reader-execute-plan! source-path plan 'reader-complete-resources
                                      [source-unit source-bytes preparation actual])
        _ (sh03-reader-result-preflight! source-path source-unit (reader-resource-policy) raw)
        ;; Fresh preparation and ORIGINAL receipt lookup, with no provider/store
        ;; access in verify mode. Candidate data never select this mode.
        fresh (reader-resource-prepare! source-path source-text bytes project-context binding)
        fresh-requests (reader-resource-requests! (:capability context) fresh implementation)
        _ (when-not (= requests fresh-requests)
            (sh03-reader-boundary-fail! source-path :fresh-original-resource-requests {} {}))
        verified (reader-resource-invocations! context fresh fresh-requests :verify)
        report (sh03-reader-execute-plan!
                source-path plan 'reader-verify-resource-result
                [source-unit source-bytes (:preparation fresh) verified raw])
        _ (sh03-reader-verifier-preflight! source-path raw report)
        resolved (sh03-reader-resolve-digest-requests! source-path raw bytes)]
    (assoc resolved :raw-result raw :verification-report report
           :resource-functions
           {:builder-semantic-hash (reader-canonical-hash (get-in plan [:functions 'reader-complete-resources]))
            :verifier-semantic-hash (reader-canonical-hash (get-in plan [:functions 'reader-verify-resource-result]))}
           :plan-binding (dissoc binding :plan)
           :invocation-context {:reader-source-path sh03-reader-source-relative-path
                                :authenticated-envelope-options {:source :development-current}})))

(defn reader-declared-input-build!
  "Trusted local host entry requiring the original live command capability.
  Constructs and freshly consumes C2 inside its lexical issuer/handler scope."
  [capability request]
  (sh03-reader-require-result-carrier! "<resource-build>" :resource-build-request request)
  (when-not (and (= String (class (get-in request [:source :path])))
                 (= String (class (get-in request [:source :text]))))
    (sh03-reader-boundary-fail! "<resource-build>" :native-resource-source {} {}))
  (let [{:keys [path text]} (:source request)
        bytes (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)
        _ (when-not (= text (sh03-reader-strict-source-text! path path bytes))
            (sh03-reader-boundary-fail! path :original-resource-utf8 {} {}))
        project-context (reader-project-context-for-source path)
        binding (sh03-reader-current-binding! path sh03-reader-source-relative-path)
        handler (reader-resource-handler-binding! path)
        implementation (reader-canonical-hash {:reader (dissoc binding :plan) :handler (:identity handler)})
        prepared (reader-resource-prepare! path text bytes project-context binding)
        requests (reader-resource-requests! capability prepared implementation)
        runtime {:engine :gravity-declared-input-reader-host-runner :compiler-artifact-plan? true}
        rows (mapv #(hash-map :instruction (:instruction handler) :request %) requests)]
    (#'gravity.build.reader-bridge/call-with-scope
     capability runtime (:plan handler) rows
     (fn [scope]
       (clojure.core/binding [*reader-resource-invocation*
                 {:capability capability :scope scope :runtime runtime :binding binding
                  :handler handler :implementation implementation :requests requests
                  :source-path path :source-text text :project-context project-context :mode :construct}]
         (let [artifact (compiler-c2-reader-source-artifact
                         path text project-context nil nil sh03-reader-source-relative-path
                         {:reader-source-path sh03-reader-source-relative-path
                          :authenticated-envelope-options {:source :development-current}}
                         (assoc standard-reader-options :extension-policy (reader-resource-policy-id)))]
           ;; Existing private C2 authority is retained. The consumer below
           ;; independently reconstructs with original receipt lookup.
           (sh03-c3-precomputed-c2-verify!
            sh03-reader-internal-product-authority path artifact
            {:reader-source-path sh03-reader-source-relative-path
             :authenticated-envelope-options {:source :development-current}})))))))

(declare sh03-reader-adapt-products!)
(defn- reader-resource-products! [source-path source-text reader-options project-context]
  (reader-resource-context! source-path source-text project-context)
  (let [bytes (.getBytes ^String source-text java.nio.charset.StandardCharsets/UTF_8)
        resolved (reader-resource-resolved-result! source-path source-text bytes project-context)]
    (sh03-reader-adapt-products! source-path source-text bytes reader-options project-context resolved)))

(defn- reader-resource-record! [result]
  (sh03-reader-require-result-carrier! "<resource-result>" :resource-result result)
  (let [record (first (:reader-extension-invocation-records result))
        forms (filterv #(= :tagged-literal (:kind %)) (:form-tree result))
        invocations (:invocations record)]
    (when-not (and (= 1 (count (:reader-extension-invocation-records result)))
                   (= :gravity/reader-extension-invocation (:artifact record))
                   (= 3 (:schema-version record))
                   (= (reader-resource-policy) (:policy record)
                      (get-in result [:source-unit :reader-options]))
                   (= 'gravity.reader/read-input (:handler record))
                   (= [114 101 115 111 117 114 99 101] (:tag-codepoints record))
                   (= #{:build/read-file} (:build-effects record))
                   (= #{:fs/read} (:capabilities record)) (= #{:meta} (:profiles record))
                   (= (if (seq forms) :invoked :registered-not-invoked) (:status record))
                   (= (mapv :form-id forms) (mapv :form-id invocations)))
      (sh03-reader-boundary-fail! "<resource-result>" :resource-invocation-record {} {}))
    (doseq [[form invocation] (map vector forms invocations)]
      (let [seed (:input-seed invocation) returned (:returned-seed invocation)
            request (get-in invocation [:invocation :request])]
        (when-not (and (= [114 101 115 111 117 114 99 101] (:tag-codepoints form))
                       (= 1 (count (:children form)))
                       (= (:form-id form) (:form-id invocation) (:form-id seed) (:form-id request))
                       (= (first (:children form)) (:payload-form-id invocation)
                          (:payload-form-id seed) (:payload-form-id request))
                       (= (:span form) (:span invocation) (:span seed) (:span request))
                       (= (:raw form) (:raw invocation))
                       (= (get-in result [:source-unit :source-id]) (:source-id seed) (:source-id request))
                       (= (get-in result [:source-unit :bytes-hash]) (:source-content-id seed)
                          (:source-content-hash request))
                       (= (:datum seed) (:input request))
                       (= returned (assoc seed :datum (:datum returned)))
                       (= String (class (:datum returned))))
          (sh03-reader-boundary-fail! "<resource-result>" :resource-invocation-join {} {}))))
    record))

(defn- reader-resource-consume! [candidate source-path artifact invocation]
  (when-not (and (identical? candidate sh03-reader-internal-product-authority)
                 invocation *reader-resource-invocation*)
    (sh03-reader-boundary-fail! source-path :original-resource-consumer-authority {} {}))
  (let [{:keys [source-text project-context]} *reader-resource-invocation*]
    (reader-resource-context! source-path source-text project-context)
    (binding [*reader-resource-invocation* (assoc *reader-resource-invocation* :mode :verify)]
      (let [fresh (compiler-c2-reader-source-artifact
                   source-path source-text project-context nil nil sh03-reader-source-relative-path
                   {:reader-source-path sh03-reader-source-relative-path
                    :authenticated-envelope-options {:source :development-current}}
                   (assoc standard-reader-options :extension-policy (reader-resource-policy-id)))]
        (when-not (= fresh artifact)
          (sh03-reader-boundary-fail! source-path :fresh-original-resource-artifact {} {})))))
  artifact)
