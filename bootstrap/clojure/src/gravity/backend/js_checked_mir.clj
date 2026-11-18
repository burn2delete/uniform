(ns gravity.backend.js-checked-mir
  "Production current-source B1/C14/B6 composition on the Clojure seed.
   Uniform owners check target legality and construct the static program. This
   host binds their current source, transports fresh verification and publishes
   complete files. No caller-supplied acceptance report or token is trusted."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.backend.js :as js]
            [gravity.backend.js-scalar :as printer]
            [gravity.bootstrap :as bootstrap]
            [gravity.c2-pass-cache :as source-cache])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(def ^:private source-owners
  {:c14 {:path "language/compiler/src/gravity/compiler/c14_target_lowering_architecture.gravity"
         :builder 'c14-build-checked-js-lowering :builder-arity 2
         :verifier 'c14-verify-checked-js-lowering :verifier-arity 3
         :report :gravity/c14-checked-js-verification
         :checks [:fresh-target-lowering-reconstruction]}
   :b1 {:path "language/compiler/src/gravity/backend/b1_backend_interface_specification.gravity"
        :builder 'b1-build-checked-js-input :builder-arity 2
        :verifier 'b1-verify-checked-js-input :verifier-arity 3
        :report :gravity/b1-checked-js-verification
        :checks [:fresh-backend-input-reconstruction]}
   :b6 {:path "language/compiler/src/gravity/backend/b6_javascript_typescript_backend_design.gravity"
        :builder 'b6-build-checked-js-program :builder-arity 3
        :verifier 'b6-verify-checked-js-program :verifier-arity 4
        :report :gravity/b6-checked-js-verification
        :checks [:fresh-static-program-lowering]}})

(defn- fail! [id source-path missing-fact & [data]]
  (bootstrap/js-ts-backend-fail!
   id "Checked JavaScript module compilation rejected" source-path nil
   (merge {:missing-fact missing-fact} data)))

(defn- snapshot! [path]
  (let [snapshot (source-cache/source-snapshot! path)
        text (str (.decode (doto (.newDecoder StandardCharsets/UTF_8)
                            (.onMalformedInput CodingErrorAction/REPORT)
                            (.onUnmappableCharacter CodingErrorAction/REPORT))
                          (ByteBuffer/wrap (:bytes snapshot))))]
    {:source-path (:canonical-path snapshot)
     :source-content-hash (:bytes-hash snapshot)
     :source-byte-count (:byte-count snapshot) :text text}))

(defn- owner-path [relative]
  ;; Resolve from the existing C11 source selection, independently of CWD.
  (let [c11-file (io/file (bootstrap/p15-s23-c11-mir-resolve-source-path))
        root (bootstrap/reader-project-root-path (.getPath c11-file))]
    (.getCanonicalPath (io/file root relative))))

(defn- identity! [source-path expected actual missing]
  (bootstrap/p15-s23-c11-mir-require-strict-structure! source-path expected actual missing))

(defn- compile-owner! [source-path emitter-rule owner contract]
  (let [source (snapshot! (owner-path (:path contract)))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan
              (:emitter emitter-rule) (:source-path source) (:text source))
        functions (:functions plan)
        builder (:builder contract) verifier (:verifier contract)]
    (when-not (and (= (:source-content-hash source) (get-in plan [:source :sha256]))
                   (= [:meta :jvm #{} #{} :safe]
                      ((juxt :profile :target :effects :capabilities :safety) (:module plan)))
                   (= (:builder-arity contract) (get-in functions [builder :arity]))
                   (= (:verifier-arity contract) (get-in functions [verifier :arity])))
      (fail! "B1-INPUT" source-path :current-target-owner-source-and-api {:owner owner}))
    (identity! source-path source (snapshot! (:source-path source)) :stable-current-target-owner)
    {:plan plan :contract contract
     :metadata (merge (dissoc source :text)
                      {:owner owner :source-mode :development-current
                       :semantic-authority :gravity-source
                       :compiler-host :clojure-stage0-seed
                       :admission-status :development-unadmitted
                       :builder-function builder :verifier-function verifier
                       :plan-semantic-hash
                       (bootstrap/p15-s23-c11-mir-digest
                        (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input plan))
                       :functions-semantic-hash (bootstrap/p15-s23-c11-mir-digest functions)
                       :builder-semantic-hash (bootstrap/p15-s23-c11-mir-digest (get functions builder))
                       :verifier-semantic-hash (bootstrap/p15-s23-c11-mir-digest (get functions verifier))
                       :function-shapes (select-keys
                                         (into {} (map (fn [[name function]]
                                                         [name (select-keys function [:arity :params])]) functions))
                                         [builder verifier])
                       :seed-emitter (select-keys emitter-rule [:source-rule-hash :rule-record])})}))

(defn- recheck-owners! [source-path bindings]
  (doseq [[_ binding] bindings]
    (let [metadata (:metadata binding)]
      (identity! source-path
                 (select-keys metadata [:source-path :source-content-hash :source-byte-count])
                 (dissoc (snapshot! (:source-path metadata)) :text)
                 :stable-current-target-owner))))

(defn- invoke! [source-path binding function arguments]
  (let [result (bootstrap/p15-s23-stage2-runtime-execute-function
                {:engine :gravity-checked-js-current-source :compiler-artifact-plan? true}
                (:plan binding) function arguments)]
    (bootstrap/p15-s23-c11-mir-require-trusted-carrier!
     source-path :current-js-owner-result result :reject)
    (when (= :rejected (:status result))
      (let [diagnostic (first (:diagnostics result))]
        (bootstrap/js-ts-backend-fail!
         (or (:diagnostic-id diagnostic) "B1-INPUT")
         "Uniform target owner rejected checked JavaScript lowering"
         source-path diagnostic diagnostic)))
    result))

(defn- build-and-verify! [source-path binding arguments]
  (let [contract (:contract binding)
        result (invoke! source-path binding (:builder contract) arguments)
        report (invoke! source-path binding (:verifier contract) (conj arguments result))]
    (when-not (and (= :accepted (:status result)) (= :passed (:status report))
                   (= [] (:diagnostics result)) (= [] (:diagnostics report)))
      (fail! "B1-INPUT" source-path :fresh-target-owner-verification))
    (identity! source-path
               {:artifact (:report contract) :status :passed
                :application-id (:artifact-id (first arguments))
                :checks (:checks contract) :diagnostics []}
               report :current-target-verifier-result-binding)
    {:product result :verification report}))

(defn- emission-request! [source-path options]
  (bootstrap/p15-s23-c11-mir-require-trusted-carrier!
   source-path :checked-js-emission-request options :reject)
  (when-not (and (map? options)
                 (= #{:output-path :target :runtime :language :module-format} (set (keys options)))
                 (string? (:output-path options)) (not (str/blank? (:output-path options))))
    (fail! "B6-TARGET" source-path :explicit-source-only-emission-options))
  (let [request (-> (dissoc options :output-path)
                    (update :target bootstrap/js-ts-backend-canonical-target))]
    (when-not (= {:target :js-ts :runtime :node20 :language :es2022 :module-format :esm} request)
      (fail! "B6-TARGET" source-path :node20-es2022-esm-target))
    request))

(defn- check-source! [application]
  (let [source (:source application)
        snapshot (snapshot! (:path source))]
    (identity! (:path source)
               {:source-path (:path source) :source-content-hash (:content-hash source)
                :source-byte-count (:byte-count source)}
               (dissoc snapshot :text) :checked-js-original-source-bytes)
    (:text snapshot)))

(defn- check-javascript! [source-path javascript]
  ;; Syntax validation only. Compilation never invokes an exported source
  ;; function or discovers a value to embed. Runtime evidence comes from tests.
  (let [directory (Files/createTempDirectory "uniform-checked-js-syntax-" (make-array FileAttribute 0))
        module (.resolve directory "program.mjs")]
    (try
      (spit (.toFile module) javascript :encoding "UTF-8")
      (bootstrap/js-ts-backend-run-node-process!
       ["--check" (str module)] source-path "B6-TARGET" "Generated ESM failed Node syntax validation")
      (finally (js/delete-tree! directory)))))

(defn- text-artifact [filename text]
  {:file filename :sha256 (str "sha256:" (bootstrap/sha256-hex text))
   :byte-count (alength (.getBytes ^String text StandardCharsets/UTF_8))})

(defn- edn-source [value]
  (binding [*print-length* nil *print-level* nil *print-meta* false
            *print-dup* false *print-readably* true *print-namespace-maps* false]
    ;; Canonical identity preimages may normalize collections; published facts
    ;; must retain the actual sets, vectors and maps supplied by the owners.
    (str (pr-str value) "\n")))

(def ^:private host-transport-paths
  ["bootstrap/clojure/src/gravity/backend/js_checked_mir.clj"
   "bootstrap/clojure/src/gravity/backend/js_scalar.clj"
   "bootstrap/clojure/src/gravity/backend/js.clj"])

(def ^:private loaded-host-transports
  (into (sorted-map)
        (for [relative host-transport-paths]
          [relative (dissoc (snapshot! (owner-path relative)) :text)])))

(defn- recheck-host-transports! [source-path]
  (doseq [[_ expected] loaded-host-transports]
    (identity! source-path expected
               (dissoc (snapshot! (:source-path expected)) :text)
               :stable-loaded-js-host-transport)))

(let [authenticate-handoff! bootstrap/p15-s23-authenticate-current-source-mir-handoff!]
(defn emit-verified-module!
  "Internal backend handoff. The supplied closure must be the exact live
   authenticator registered by the original source construction authority.
   Arbitrary callbacks, accepted-looking products and expired contexts reject.
   Public compiler callers use compile-source! with source and policy only."
  [authenticate! application options]
  (authenticate-handoff! authenticate! application)
  (let [source-path (get-in application [:source :path])
        request (emission-request! source-path options)
        output (:output-path options)
        _ (when-not (bootstrap/c-backend-output-path-allowed? output)
            (fail! "C14-INPUT" source-path :output-path-containment))
        _ (when (.exists (io/file output))
            (fail! "C14-INPUT" source-path :fresh-output-path))
        _ (recheck-host-transports! source-path)
        observed-node (bootstrap/js-ts-backend-node-version! source-path)
        source-text (check-source! application)
        ;; This existing emitter selects the seed program that compiles the
        ;; current Uniform owners; its JVM host target is not artifact target.
        emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm)
        bindings (into {} (for [[owner contract] (sort-by key source-owners)]
                            [owner (compile-owner! source-path emitter owner contract)]))
        c14 (build-and-verify! source-path (:c14 bindings) [application request])
        b1 (build-and-verify! source-path (:b1 bindings) [application (:product c14)])
        b6 (build-and-verify! source-path (:b6 bindings) [application (:product c14) (:product b1)])
        program (:product b6)
        emitted (printer/program-source program)
        paths (js/output-paths output)
        code {:javascript (:javascript emitted)
              :typescript-declarations (printer/declaration-source program)
              :source-map (printer/source-map-source program source-text emitted)
              :package-metadata
              (str (printer/json-source
                    {:name "uniform-checked-scalar-module" :private true
                     :type "module" :sideEffects false :engines {:node ">=20 <21"}
                     :types "./program.d.ts"
                     :exports {"." {:types "./program.d.ts" :import "./program.mjs"}}}) "\n")}
        _ (check-javascript! source-path (:javascript code))
        artifact-records (into (sorted-map)
                              (for [[kind text] code]
                                [kind (text-artifact (.getName (io/file (get paths kind))) text)]))
        target-owners (into (sorted-map) (map (fn [[owner binding]] [owner (:metadata binding)]) bindings))
        reports {:c14 (:verification c14) :b1 (:verification b1) :b6 (:verification b6)}
        provenance {:artifact :gravity/checked-js-module-provenance :schema-version 1
                    :source (:source application) :source-target (:source-target application)
                    :source-policy-receipt (:source-policy-receipt application)
                    :compiler-host (:compiler-host application)
                    :compiler-bindings (merge (:compiler-bindings application) target-owners)
                    :host-transport-sources loaded-host-transports
                    :host-source-observation :captured-at-backend-load-and-rechecked-before-publication
                    :input (:input program) :target request
                    :requested-target (:target options) :canonical-target (:target request)
                    :core-verification (:core-verification application)
                    :mir-verification (:mir-verification application)
                    :target-verification reports
                    :target-products {:c14 (bootstrap/p15-s23-c11-mir-digest (:product c14))
                                      :b1 (bootstrap/p15-s23-c11-mir-digest (:product b1))
                                      :b6 (bootstrap/p15-s23-c11-mir-digest program)}
                    :artifact-bytes artifact-records
                    :runtime-observation {:node-version observed-node :validation :syntax-only}
                    :semantic-authority :gravity-source
                    :host-role :source-binding-syntax-printing-hashing-and-atomic-publication
                    :validation-scope {:node-validation :syntax-check
                                       :export-execution :not-performed
                                       :clean-checkout-acceptance :separate-development-evidence}
                    :nonclaims [:full-language :release :self-hosting :seed-retirement]}
        provenance-text (edn-source provenance)
        artifacts (assoc artifact-records :provenance (text-artifact "provenance.edn" provenance-text))
        manifest-base {:artifact :gravity/checked-js-module-manifest :schema-version 1 :status :emitted
                       :backend :gravity.backend/js-ts :source (:source application)
                       :source-target (:source-target application) :profile :hosted :target request
                       :input (:input program) :compiler-bindings (:compiler-bindings provenance)
                       :host-transport-sources loaded-host-transports
                       :compiler-host (:compiler-host application)
                       :abi (:abi program) :runtime (:runtime program) :numeric (:numeric program)
                       :error-contract (:error-contract program)
                       :evidence (:evidence program)
                       :target-eligibility (:eligibility (:product c14))
                       :backend-eligibility (:eligibility (:product b1))
                       :target-verification reports :artifacts artifacts
                       :source-map :source-map :provenance :provenance
                       :exports (:exports program) :diagnostics []
                       :validation-scope {:node-validation :syntax-check
                                          :export-execution :not-performed
                                          :clean-checkout-acceptance :separate-development-evidence}
                       :nonclaims [:full-language :release :self-hosting :seed-retirement]}
        manifest (assoc manifest-base :artifact-id
                        (bootstrap/p15-s23-c11-mir-digest manifest-base))
        contents (assoc code :provenance provenance-text :manifest (edn-source manifest))]
    ;; No files have been published. Check source/Core/MIR/owners again after
    ;; compilation and formatting and immediately before the atomic operation.
    (authenticate-handoff! authenticate! application)
    (recheck-owners! source-path bindings)
    (recheck-host-transports! source-path)
    (bootstrap/js-ts-backend-stage-files! output paths contents source-path)
    {:artifact :gravity/checked-js-module :status :emitted
     :application application :paths paths :manifest manifest :provenance provenance
     :target-eligibility (:product c14) :backend-input (:product b1) :program program
     :verification reports :runtime-observation {:node-version observed-node}
     :artifact-inventory (assoc artifacts :manifest (text-artifact "manifest.edn" (:manifest contents)))})))

(defn- compile-source*
  [source-path policy options discover-policy?]
  (emission-request! source-path options)
  (let [emit (fn [application authenticate!]
               (emit-verified-module! authenticate! application options))]
    (if discover-policy?
      (bootstrap/p15-s23-call-with-current-source-verified-mir source-path emit)
      (bootstrap/p15-s23-call-with-current-source-verified-mir source-path policy emit))))

(defn compile-source!
  "Compile actual source with discovered or explicit policy into a fresh directory.
   Discovery reads policy from the first authoritative ingress; construction
   still performs the genuine source replay before target emission."
  ([source-path options]
   (compile-source* source-path nil options true))
  ([source-path policy options]
   (compile-source* source-path policy options false)))
