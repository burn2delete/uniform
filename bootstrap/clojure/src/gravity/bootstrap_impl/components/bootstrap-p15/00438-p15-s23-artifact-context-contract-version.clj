

(def ^:private p15-s23-artifact-context-contract-version
  :p15-s23-proof-artifact-dag-v2)

(defn p15-s23-artifact-context-key
  [kind source-path]
  (let [file (java.io.File. source-path)]
    ;; The context is request-scoped, but metadata-only keys can still alias
    ;; two same-size edits made within one request.  Hash the source bytes so
    ;; a fresh authoritative request never reuses an artifact for different
    ;; source content.
    ;; `kind` identifies the fixed builder closure.  A context cannot outlive
    ;; the current request/process, so code, policy, and tool inputs cannot
    ;; change underneath it; recursively requested sibling artifacts are
    ;; content-keyed by this same function.
    [p15-s23-artifact-context-contract-version
     kind
     (.getCanonicalPath file)
     (sha256-hex (slurp file))]))

(defn p15-s23-with-artifact-build-context
  [build-fn]
  (if *p15-s23-artifact-build-context*
    (build-fn)
    (binding [*p15-s23-artifact-build-context*
              (atom {:artifacts {} :in-flight {}})]
      (build-fn))))

(declare p15-s23-context-artifact)

(defn p15-s23-context-source-data
  [source-path build-fn]
  (if-not *p15-s23-artifact-build-context*
    (build-fn)
    (p15-s23-context-artifact :source-data source-path build-fn)))

(defn p15-s23-context-artifact
  [kind source-path build-fn]
  (if-not *p15-s23-artifact-build-context*
    (p15-s23-with-artifact-build-context
     #(p15-s23-context-artifact kind source-path build-fn))
    (let [key (p15-s23-artifact-context-key kind source-path)
          context *p15-s23-artifact-build-context*]
      (loop []
        (let [state @context]
          (cond
            (contains? (:artifacts state) key)
            (get-in state [:artifacts key])

            (contains? (:in-flight state) key)
            (let [[status value] @(get-in state [:in-flight key])]
              (if (= :ok status)
                value
                (throw value)))

            :else
            (let [pending (promise)
                  next-state (assoc-in state [:in-flight key] pending)]
              (if (compare-and-set! context state next-state)
                (try
                  (let [artifact (build-fn)]
                    (swap! context
                           #(-> %
                                (update :in-flight dissoc key)
                                (assoc-in [:artifacts key] artifact)))
                    (deliver pending [:ok artifact])
                    artifact)
                  (catch Throwable error
                    (swap! context update :in-flight dissoc key)
                    (deliver pending [:error error])
                    (throw error)))
                (recur)))))))))

(defn p15-s23-whole-language-self-hosting-gate-source-artifact
  [path source-text]
  (p15-s23-cached-source-artifact
   [:p15-s23-whole-language-self-hosting-gate (sha256-hex source-text)]
   path
   #(p15-s23-whole-language-self-hosting-gate-source-artifact*
     path source-text)))

(defn p15-s23-whole-language-self-hosting-gate-file-artifact
  [path]
  (p15-s23-with-artifact-build-context
   #(p15-s23-whole-language-self-hosting-gate-source-artifact
     path
     (slurp path))))

(def p15-s23-compiler-source-path
  "bootstrap/gravity/p15_s23/compiler.gravity")

(def p15-s23-canonical-compiler-pipeline
  [:read-source
   :build-syntax
   :macro-expand
   :resolve-names
   :lower-to-core
   :type-check
   :effect-check
   :profile-validate
   :safety-analyze
   :build-mir
   :verify-mir
   :optimize-mir
   :lower-domain-ir
   :verify-domain-ir
   :lower-target
   :emit-artifacts])

(def p15-s23-compiler-source-components
  #{:reader :syntax :diagnostics :source-frontend
    :syntax-object-model :macro-expansion :name-resolution
    :core-semantics
    :core-lowering
    :type-checker
    :effect-checker
    :ownership-checker
    :safety-analysis
    :mir-specification
    :domain-ir-architecture
    :mir-optimization
    :target-lowering
    :compiler-diagnostics
    :incremental-compilation
    :compiler-plugin-pass-api
    :compiler-verification
    :backend-interface
    :c-backend
    :llvm-backend
    :wasm-backend
    :jvm-backend
    :js-ts-backend
    :mlir-backend
    :gpu-backend
    :hdl-backend
    :workflow-backend
    :query-backend
    :mobile-backend
    :compiler-source-inventory})

(def p15-s23-compiler-source-inventory-diagnostic-messages
  {"P15S23C001" "P15-S23 compiler source stage record is missing"
   "P15S23C002" "P15-S23 compiler source canonical pipeline is incomplete"
   "P15S23C003" "P15-S23 compiler source inventory is incomplete"
   "P15S23C004" "P15-S23 compiler source required evidence list is incomplete"
   "P15S23C005" "P15-S23 compiler source makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-compiler-source-inventory-diagnostic-ids
  ["P15S23C001" "P15S23C002" "P15S23C003" "P15S23C004"
   "P15S23C005"])

(defn p15-s23-compiler-source-inventory-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-compiler-source-inventory-diagnostic-messages
              id
              "P15-S23 compiler source inventory failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-compiler-source-inventory
                 :diagnostic-family
                 :p15-s23-compiler-source-inventory
                 :value value
                 :remediation "Keep the P15-S23 compiler stage in Gravity-owned source, preserve the C1 canonical pipeline, enumerate all self-hosting evidence, and keep full self-hosting claims false until the complete evidence bundle exists."}
                data)))

(defn p15-s23-compiler-source-form-record
  [source-path]
  (p15-s23-context-source-data
   source-path
   #(let [source-text (slurp source-path)
          records (read-source-form-records source-path source-text)
          forms (mapv :form records)
          _ (validate-ns-syntax! source-path forms)
          module (parse-module source-path forms)]
      {:source-text source-text
       :records records
       :forms forms
       :module module})))

(defn p15-s23-compiler-def-value
  [source-path forms symbol-name]
  (let [definition
        (first (filter #(and (seq? %)
                             (= 'def (first %))
                             (= symbol-name (second %)))
                       forms))]
    (when-not (and definition (= 3 (count definition)))
      (p15-s23-compiler-source-inventory-fail!
       "P15S23C001" source-path definition
       {:missing-fields [symbol-name]}))
    (nth definition 2)))

(defn p15-s23-required-evidence-keys
  []
  (conj (set (map :key
                  p15-s23-whole-language-self-hosting-required-evidence))
        :clojure-seed-retired))

(defn p15-s23-compiler-source-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-compiler-source-inventory
   :source-span {:source source-path}
   :message (get p15-s23-compiler-source-inventory-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_gravity_compiler_source_inventory})

(defn p15-s23-source-module-present?
  [source-module]
  (let [path (:path source-module)]
    (and (string? path)
         (.isFile (java.io.File. path)))))