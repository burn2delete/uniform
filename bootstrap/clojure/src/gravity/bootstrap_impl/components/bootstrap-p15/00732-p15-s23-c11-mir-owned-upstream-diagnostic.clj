

(defn p15-s23-c11-mir-owned-upstream-diagnostic?
  [candidate]
  (let [data
        (p15-s23-trusted-diagnostic-data
         candidate p15-s23-c11-mir-max-final-artifact-carrier-nodes
         p15-s23-c11-mir-max-carrier-depth)
        rule (:id data)
        contract (p15-s23-c11-mir-diagnostic-rule-contract rule)]
    (and (map? data)
         contract
         (not (contains? (set c11-mir-diagnostic-ids) rule))
         (some? *p15-s23-c11-upstream-diagnostic-owner*)
         (contains? data ::c11-upstream-diagnostic-owner)
         (identical? *p15-s23-c11-upstream-diagnostic-owner*
                     (::c11-upstream-diagnostic-owner data))
         (= :stage0 (:bootstrap-stage data))
         (= (:stage contract) (:stage data))
         (= (:family contract) (:diagnostic-family data))
         (= (:document-id contract) (:document-id data))
         (= (:expected-document contract) (:expected-document data))
         (and (string? (:diagnostic-id data))
              (re-matches #"sha256:[0-9a-f]{64}"
                          (:diagnostic-id data)))
         (map? (:source-span data))
         (string? (or (get-in data [:source-span :source])
                      (get-in data [:source-span :file])))
         (keyword? (:profile data))
         (keyword? (:target data))
         (keyword? (:missing-fact data))
         (vector? (:generated-origin-chain data))
         (or (and (string? (:remediation data))
                  (not (str/blank? (:remediation data))))
             (and (vector? (:remediation data))
                  (seq (:remediation data))
                  (every? map? (:remediation data)))))))

(defn p15-s23-c11-mir-contain-checked-core-exception!
  [source-path boundary exception]
  (let [data
        (p15-s23-backend-trusted-exception-data
         exception p15-s23-c11-mir-max-final-artifact-carrier-nodes
         p15-s23-c11-mir-max-carrier-depth)]
    (if (p15-s23-c11-mir-owned-upstream-diagnostic? data)
      (let [rule (:id data)
            contract (p15-s23-c11-mir-diagnostic-rule-contract rule)
            facts
            (p15-s23-c11-mir-safe-diagnostic-facts
             data
             (merge *p15-s23-c11-mir-diagnostic-context*
                    (select-keys data
                                 [:syntax-id :core-node-id
                                  :operation-id :origin-id])
                    {:producer-diagnostic-id (:diagnostic-id data)}))
            subject
            {:source-span (:source-span data)
             :syntax-id (:syntax-id data)
             :core-node-id (:core-node-id data)
             :operation-id (:operation-id data)
             :origin-id (:origin-id data)
             :origin-chain (:generated-origin-chain data)
             :profile (:profile data)
             :target-request (:target data)}
            record
            (p15-s23-c11-mir-diagnostic-record-from-components
             rule (:stage contract) :error source-path subject facts
             (:profile data)
             (or (:requested-target
                  *p15-s23-c11-mir-diagnostic-context*)
                 (:target data))
             [{:kind :host-exception-details
               :status :redacted
               :policy :allowlisted-semantic-facts-only}
              (p15-s23-c11-mir-containment-observation)])]
        (p15-s23-c11-mir-throw-record! record))
      (p15-s23-c11-mir-fail!
       "C11-VERIFY" source-path {}
       {:missing-fact boundary
        :diagnostic-severity :internal-error
        :contained-host-error-hash
        (str "sha256:" (sha256-hex (.getName (class exception))))}))))

(defn p15-s23-c11-mir-path-neutral-value
  "Remove only physical path strings from a C11 identity input.  Structural
  checked-core :path vectors and semantic source/origin identifiers remain.
  Actual paths are committed separately by :actual-path-binding-id."
  [value]
  (walk/postwalk
   (fn [item]
     (if (map? item)
       (if (let [item-keys (set (keys item))]
             (every? item-keys
                     [:origin-id :provenance-binding-hash
                      :actual-path-binding-hash]))
         ;; A checked-core raw-origin record has a separately verified,
         ;; path-neutral semantic binding and a path-bearing physical binding.
         ;; MIR identity keeps the former; the complete latter is retained in
         ;; artifact provenance and committed through checked-core
         ;; :actual-path-binding-id at the outer C11 binding layer.
         (select-keys item [:origin-id :provenance-binding-hash])
         (reduce-kv
          (fn [result key child]
            (if (or (= key :actual-paths)
                    (= key :actual-path-binding-hash)
                    (= key :actual-path-binding-id)
                    (and (contains? #{:source :file :source-path
                                      :actual-source-path :actual-path}
                                    key)
                         (string? child)
                         (not (re-matches #"sha256:[0-9a-f]{64}"
                                          child))))
              result
              (assoc result key child)))
          (empty item)
          item))
       item))
   value))

(defn p15-s23-c11-mir-resolve-source-path
  "Resolve the pinned C11 source independently of the caller's CWD."
  []
  (let [relative p15-s23-c11-mir-source-relative-path
        classpath-roots
        (keep (fn [entry]
                (let [file (java.io.File. entry)]
                  (when (.isDirectory file) file)))
              (str/split (System/getProperty "java.class.path" "")
                         (re-pattern (java.io.File/pathSeparator))))
        roots (distinct (cons (java.io.File.
                               (System/getProperty "user.dir"))
                              classpath-roots))]
    (or (some (fn [root]
                (loop [directory root]
                  (let [candidate (java.io.File. directory relative)]
                    (cond
                      (.isFile candidate) (.getPath candidate)
                      (.getParentFile directory)
                      (recur (.getParentFile directory))
                      :else nil))))
              roots)
        relative)))

(defn p15-s23-c11-mir-source-binding!*
  "Compile and authenticate the exact Gravity-authored C11 builder source.
  The executable plan is retained only for the opaque constructor/replay
  boundary; public artifacts receive hashes and function shapes, not a mutable
  plan that can be substituted after authentication."
  [request-source requested-target]
  (let [source-path (p15-s23-c11-mir-resolve-source-path)]
    (when-not (.isFile (java.io.File. source-path))
      (p15-s23-c11-mir-fail!
       "C11-MODULE" request-source {}
       {:missing-fact :pinned-gravity-c11-source
        :requested-target requested-target}))
    (let [source-file (java.io.File. source-path)
          observed-file-bytes
          (java.nio.file.Files/size (.toPath source-file))
          _
          (when-not (= p15-s23-c11-mir-source-byte-count
                       observed-file-bytes)
            (p15-s23-c11-mir-fail!
             "C11-VERIFY" request-source {}
             {:missing-fact :pinned-gravity-c11-source-byte-size
              :expected-source-bytes p15-s23-c11-mir-source-byte-count
              :observed-source-bytes observed-file-bytes
              :requested-target requested-target}))
          source-text (slurp source-file)
          source-bytes (alength (.getBytes
                                 source-text
                                 java.nio.charset.StandardCharsets/UTF_8))
          source-content-hash (str "sha256:" (sha256-hex source-text))]
      (when-not (and (= p15-s23-c11-mir-source-byte-count source-bytes)
                     (= p15-s23-c11-mir-expected-source-content-hash
                        source-content-hash))
        (p15-s23-c11-mir-fail!
         "C11-VERIFY" request-source {}
         {:missing-fact :pinned-gravity-c11-source-identity
          :expected-source-bytes p15-s23-c11-mir-source-byte-count
          :observed-source-bytes source-bytes
          :expected-source-content-hash
          p15-s23-c11-mir-expected-source-content-hash
          :observed-source-content-hash source-content-hash}))
      (let [emitter-rule
            (c-backend-stage2-plan-emitter-source-rule!
             request-source requested-target)
            plan
            (try
              (p15-s23-stage2-compiler-artifact-plan
               (:emitter emitter-rule) source-path source-text)
              (catch clojure.lang.ExceptionInfo ex
                (p15-s23-c11-mir-fail!
                 "C11-VERIFY" request-source {}
                 {:missing-fact :gravity-c11-source-compilation
                  :cause-diagnostic (:id (ex-data ex))})))
            functions (:functions plan)
            observed-shapes
            (into {}
                  (map (fn [[name _]]
                         [name (select-keys (get functions name)
                                            [:arity :params])]))
                  p15-s23-c11-mir-required-functions)
            plan-semantic-hash
            (p15-s23-c11-mir-digest
             (p15-s23-stage2-compiler-artifact-semantic-input plan))
            functions-semantic-hash
            (p15-s23-c11-mir-digest functions)
            builder-semantic-hash
            (p15-s23-c11-mir-digest
             (get functions p15-s23-c11-mir-builder-function))
            verifier-semantic-hash
            (p15-s23-c11-mir-digest
             (get functions p15-s23-c11-mir-verifier-function))]
        (when-not (= p15-s23-c11-mir-required-functions observed-shapes)
          (p15-s23-c11-mir-fail!
           "C11-MODULE" request-source {}
           {:missing-fact :pinned-gravity-c11-function-shapes
            :observed-function-shapes observed-shapes}))
        (when-not
         (and (= p15-s23-c11-mir-expected-plan-semantic-hash
                 plan-semantic-hash)
              (= p15-s23-c11-mir-expected-functions-semantic-hash
                 functions-semantic-hash)
              (= p15-s23-c11-mir-expected-builder-semantic-hash
                 builder-semantic-hash)
              (= p15-s23-c11-mir-expected-verifier-semantic-hash
                 verifier-semantic-hash))
          (p15-s23-c11-mir-fail!
           "C11-VERIFY" request-source {}
           {:missing-fact :pinned-gravity-c11-function-identity
            :observed-plan-semantic-hash plan-semantic-hash
            :observed-functions-semantic-hash functions-semantic-hash
            :observed-builder-semantic-hash builder-semantic-hash
            :observed-verifier-semantic-hash verifier-semantic-hash}))
        {:source-path source-path
         :source-content-hash source-content-hash
         :source-byte-count source-bytes
         :plan-semantic-hash plan-semantic-hash
         :functions-semantic-hash functions-semantic-hash
         :builder-semantic-hash builder-semantic-hash
         :verifier-semantic-hash verifier-semantic-hash
         :function-shapes observed-shapes
         :plan plan}))))