(ns gravity.compiler-probe
  "Current-source development observation for the existing Seedless controller.

  Executes the ordinary authenticated typed frontend on the complete six-owner
  cohort already selected by the SH07 plan. This is a prerequisite of the staged
  Linux LLVM candidate, not a native build, migration admission or release verifier."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str])
  (:import [java.nio.file Files]
           [java.security MessageDigest]))

(def command ["clojure" "-J-Xmx2g" "-M:compiler-probe"])
(def catalog-path "bootstrap/clojure/fixtures/self-hosting/sh-25/component_build_engine.gravity")
(def reader-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
(def syntax-path "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")
(def emitter-path "bootstrap/gravity/p15_s23/emitter.gravity")
(def cohort
  ;; docs/self-hosting-sh07-completion-plan.md, candidate-scope decision.
  ;; SH25 is the existing inventory; optional target entries are not a Linux
  ;; build dependency merely because they appear in that inventory.
  [:authenticated-envelope :bootstrap-reader :bootstrap-syntax
   :bootstrap-macro :bootstrap-resolution :checked-core])

(defn sha256 [bytes]
  (apply str (map #(format "%02x" (bit-and 255 %))
                  (.digest (MessageDigest/getInstance "SHA-256") bytes))))

(defn digest [value]
  (sha256 (.getBytes (pr-str value) "UTF-8")))

(defn input-sha256
  "Hash actual bytes, including every descendant of a declared directory.
  Directory identities are ordered [relative-path byte-hash] rows."
  [root path]
  (let [root-path (.toPath (.getCanonicalFile (io/file root)))
        file (io/file root path)
        canonical (.toPath (.getCanonicalFile file))]
    (when (or (not (.startsWith canonical root-path))
              (Files/isSymbolicLink (.toPath file)))
      (throw (ex-info "Probe input escapes its source root" {:path path})))
    (if (.isDirectory file)
      (let [entries (rest (file-seq file))]
        (doseq [entry entries]
          (when (Files/isSymbolicLink (.toPath entry))
            (throw (ex-info "Probe input contains a symbolic link" {:path (str entry)}))))
        (digest (->> entries
                     (filter #(.isFile %))
                     (map (fn [entry]
                            [(str/replace (str (.relativize canonical (.toPath (.getCanonicalFile entry)))) "\\" "/")
                             (sha256 (Files/readAllBytes (.toPath entry)))]))
                     (sort-by first)
                     vec)))
      (sha256 (Files/readAllBytes (.toPath file))))))

(def input-paths
  ;; Bind the complete seed and current owner sources, not only the failing
  ;; function. Retain runtime/library/build inputs even at this frontend edge.
  ["deps.edn" "AGENTS.md" "bootstrap/clojure/src" "bootstrap/gravity"
   "language/compiler/src" "language/runtime/src" catalog-path
   "bootstrap/clojure/fixtures/self-hosting/sh-19/minimal_runtime_engine.gravity"
   "contracts"
   "docs/self-hosting-milestone-completion-plan.md"
   "docs/self-hosting-sh07-completion-plan.md"
   "docs/self-hosting-current-migration-cohort.md"
   "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_kernel_support.clj"
   "tools/gravity/compiler_probe.clj"])

(defn inputs [root]
  (mapv (fn [path] {:path path :sha256 (input-sha256 root path)}) input-paths))

(defn- api [name & arguments]
  (apply (requiring-resolve (symbol "gravity.bootstrap" (str name))) arguments))

(defn- canonical [root path]
  (.getCanonicalPath (io/file root path)))

(defn- progress! [event]
  (binding [*out* *err*]
    (prn event)
    (flush)))

(defn- compile-current [root relative]
  (let [path (canonical root relative)
        text (slurp path :encoding "UTF-8")
        emitter (:emitter (api 'c-backend-stage2-plan-emitter-source-rule! path :jvm))]
    (api 'p15-s23-stage2-compiler-artifact-plan emitter path text)))

(defn- current-emitter [root]
  ;; The existing development owner checks the current source snapshot, ABI,
  ;; emitter rule join and recomputable plan. Historical pins stay unchanged.
  (api 'p15-s23-stage2-current-expression-development-binding!
       (canonical root emitter-path)))

(defn- catalog [root]
  (api 'p15-s23-stage2-runtime-execute-function
       {:engine :seedless-current-source-observation :compiler-artifact-plan? true}
       (compile-current root catalog-path) 'sh25-authoritative-component-catalog []))

(defn- module-header [root component]
  ;; Header routing only. The actual C2/C3/SH05/SH06 route below rereads and
  ;; authenticates complete source; this host declaration is not a C5 product.
  (let [path (canonical root (:source-path component))
        records (api 'read-source-form-records path (slurp path :encoding "UTF-8"))]
    (api 'parse-module path (mapv :form records))))

(defn- module-source-name [component]
  ;; Only locate a declared source candidate. Its actual header is checked
  ;; below, and C5 still authenticates the entire imported source product.
  (-> (:source-path component)
      (str/replace #"^(bootstrap/gravity/src/|language/compiler/src/|language/runtime/src/)" "")
      (str/replace #"\.gravity$" "")
      (str/replace "/" ".")
      (str/replace "_" "-")
      symbol))

(defn- source-closure [root inventory]
  ;; SH25's historical catalog omits the real Effects import that the existing
  ;; C8 module route already requires. Preserve every root and that existing
  ;; implementation dependency; stage-order edges are not namespace imports.
  (let [effects {:component-id :effects-implementation :category :analyzer
                 :source-path "language/compiler/src/gravity/effects.gravity"
                 :dependencies []}
        components (conj inventory effects)
        by-id (into {} (map (juxt :component-id identity)) components)
        roots (mapv (fn [id]
                      (or (get by-id id)
                          (throw (ex-info "Selected frontend owner is absent from the existing inventory"
                                          {:id "COMPILER-PROBE-DEPENDENCY" :component id
                                           :reason :missing-cohort-source})))) cohort)
        by-module (into {} (map (juxt module-source-name identity)) components)
        ordered (atom [])
        visited (atom #{})]
    (letfn [(visit! [component ancestors required-module]
              (let [id (:component-id component)
                    header (module-header root component)]
                (when (and required-module (not= required-module (:module header)))
                  (throw (ex-info "Declared import source has a different namespace"
                                  {:id "COMPILER-PROBE-DEPENDENCY" :component id
                                   :reason :import-source-namespace :expected required-module
                                   :actual (:module header)})))
                (when (contains? ancestors id)
                  (throw (ex-info "Declared compiler imports contain a cycle"
                                  {:id "COMPILER-PROBE-DEPENDENCY" :component id :reason :import-cycle})))
                (when-not (contains? @visited id)
                  (doseq [dependency (:requires header)]
                    (if-let [producer (get by-module (:module dependency))]
                      (visit! producer (conj ancestors id) (:module dependency))
                      (throw (ex-info "Declared compiler import has no selected source"
                                      {:id "COMPILER-PROBE-DEPENDENCY" :component id
                                       :reason :missing-import-source :dependency dependency}))))
                  (swap! visited conj id)
                  (swap! ordered conj (assoc component
                                             :source-module (:module header)
                                             :source-imports (mapv :module (:requires header)))))))]
      (doseq [component roots] (visit! component #{} nil))
      @ordered)))

(defn module-inputs [component resolved]
  {:artifact :gravity/sh06-module-inputs :schema-version 1
   :module-artifacts
   (mapv (fn [module]
           (or (get resolved module)
               (throw (ex-info "Required compiler module has no accepted C5 product"
                               {:id "COMPILER-PROBE-DEPENDENCY" :reason :missing-module-product
                                :module module :component (:component-id component)}))))
         (:source-imports component))})

(defn route [closure]
  {:command command
   :stage :current-source-function-types
   :target :llvm-x86_64-linux
   :triple "x86_64-unknown-linux-gnu"
   :abi :sysv-amd64
   :toolchain "LLVM 20.1.8"
   :execution-host {:clojure (clojure-version)
                    :java (System/getProperty "java.version")
                    :jvm-arguments (vec (.getInputArguments
                                         (java.lang.management.ManagementFactory/getRuntimeMXBean)))
                    :max-heap-bytes (.maxMemory (Runtime/getRuntime))
                    :os (System/getProperty "os.name")
                    :architecture (System/getProperty "os.arch")}
   :frontend-profile :meta
   :frontend-target :jvm
   :catalog catalog-path
   :closure closure
   :scope :complete-six-owner-typed-frontend-prerequisite
   :closure-basis "docs/self-hosting-sh07-completion-plan.md: candidate-scope decision"
   :remaining-boundary :effects-ownership-safety-mir-native-runtime-library-build-rebuild-release
   :build-graph-status :actual-native-compiler-build-recipe-unresolved})

(defn- type-api [function & arguments]
  (apply (requiring-resolve
          (symbol "gravity.self-hosting.sh08-function-call-kernel-support" (str function)))
         arguments))

(defn- type-request [component core report requests]
  ;; The existing B47 constructor retains the actual wrapper, independently
  ;; replayed SH07 report and digest binding. No supplied type facts or fixture
  ;; Core replaces the complete source product.
  (let [coordinator (type-api 'coordinator-verification core report)
        context (type-api 'b47-context core report coordinator)]
    (cond-> {:canonical-core-artifact (:canonical-core-artifact context)
             :b47-context context
             :coordinator-verification coordinator}
      (seq (:source-imports component))
      (assoc :module-type-inputs
             {:artifact :gravity/sh08-module-type-inputs :schema-version 1
              :module-requests
              (mapv (fn [module]
                      (or (get requests module)
                          (throw (ex-info "Imported compiler source has no verified C7 request"
                                          {:id "COMPILER-PROBE-DEPENDENCY"
                                           :reason :missing-module-type-product :module module}))))
                    (:source-imports component))}))))

(defn- require-type-result! [result expected]
  (when-not (= expected (:status result))
    (let [diagnostic (first (:diagnostics result))]
      (throw (ex-info "Current C7 did not accept the complete compiler module"
                      (merge diagnostic
                             {:id (or (:rule diagnostic) (:id diagnostic) "COMPILER-PROBE-TYPES")
                              :compiler-result-status (:status result)
                              :diagnostic diagnostic}))))))

(defn- diagnostic [exception]
  (let [data (ex-data exception)]
    (merge (select-keys data [:id :reason :operator :function :expected-arity :actual-arity
                              :stage :source-span :facts :remediation :missing-fact :missing-fields
                              :cause-diagnostic :cause-message :contained-diagnostic :resource-failure?
                              :diagnostic :compiler-result-status])
           {:message (.getMessage exception)
            :class (.getName (class exception))})))

(defn classify-failure
  "Unknown failures remain blockers until their actual owning edge is traced.
  A final-release refusal, a stale pin and an unavailable process cannot be
  promoted into compiler implementation work by a diagnostic name alone."
  [component stage exception]
  {:status :blocked
   :failure {:id (or (:id (ex-data exception)) "COMPILER-PROBE-UNCLASSIFIED")
             :kind (cond
                     (= "P18T04007" (:id (ex-data exception))) :release-refusal
                     (or (instance? OutOfMemoryError exception)
                         (instance? StackOverflowError exception)
                         (:resource-failure? (ex-data exception))) :resource
                     (instance? java.io.IOException exception) :environment
                     :else :unclassified)
             :source (:source-path component)
             :producer (str stage)
             :consumer "Current-source canonical Core"
             :reason "The terminal diagnostic needs a source-grounded owning implementation before dispatch."
             :diagnostic (diagnostic exception)}})

(defn observe
  "Run the declared development route. Full source is never replaced by an
  extracted helper; stop at the first failed producer/consumer handoff."
  [root]
  (let [revision-result (shell/sh "git" "rev-parse" "HEAD" :dir root)
        source-revision (str/trim (:out revision-result))
        _ (when-not (and (zero? (:exit revision-result))
                         (re-matches #"[0-9a-f]{40}" source-revision))
            (throw (ex-info "Compiler source revision is unavailable"
                            {:id "COMPILER-PROBE-SOURCE" :terminal revision-result})))
        source-inputs (inputs root)
        stage (atom :catalog)
        current (atom nil)
        completed (atom [])
        type-requests (atom {})
        subject (atom nil)
        closure (atom [])
        outcome
        (try
          (reset! closure (catalog root))
          (when-not (and (vector? @closure) (seq @closure)
                         (every? #(and (:component-id %) (string? (:source-path %))) @closure))
            (throw (ex-info "Existing SH25 source catalog is malformed" {:id "COMPILER-PROBE-CATALOG"})))
          (reset! closure (source-closure root @closure))
          (reset! stage :current-emitter)
          (let [emitter (current-emitter root)
                reader (canonical root reader-path)
                invocation {:reader-source-path reader
                            :authenticated-envelope-options {:source :development-current}
                            :syntax-source-selection {:schema :gravity/sh04-development-syntax-selection-v1
                                                      :source-path (canonical root syntax-path)}
                            :macro-source-mode :development-current}]
            (with-bindings {(requiring-resolve 'gravity.bootstrap/*p15-s23-stage2-compiler-artifact-binding*) emitter}
              (loop [remaining @closure resolved {}]
                (if-let [component (first remaining)]
                  (let [path (canonical root (:source-path component))
                        text (slurp path :encoding "UTF-8")
                        _ (reset! current component)
                        _ (reset! subject {:source-path (:source-path component)
                                           :source-sha256 (sha256 (.getBytes text "UTF-8"))})
                        _ (reset! stage :current-frontend)
                        _ (progress! {:component (:component-id component) :stage @stage :status :started})
                        resolution (api 'compiler-c5-resolution-source-artifact
                                        path text
                                        (module-inputs component resolved)
                                        reader invocation)
                        _ (reset! stage :canonical-core)
                        _ (progress! {:component (:component-id component) :stage @stage :status :started})
                        core (api 'sh07-core-from-resolution-artifact resolution invocation)]
                    (when-not (and (= :accepted (:status resolution) (:status core))
                                   (= :passed (get-in core [:gravity-core-boundary :template-verification :status])
                                      (get-in core [:gravity-core-boundary :resolved-verification :status])))
                      (throw (ex-info "Current-source frontend did not produce verified canonical Core"
                                      {:id "COMPILER-PROBE-CORE" :resolution-status (:status resolution)
                                       :core-status (:status core)})))
                    (reset! stage :core-verification)
                    (progress! {:component (:component-id component) :stage @stage :status :started})
                    (let [report (api 'sh07-core-artifact-verification core invocation)
                          _ (when-not (= :passed (:status report))
                              (throw (ex-info "Fresh public SH07 verification failed"
                                              {:id "COMPILER-PROBE-CORE-VERIFICATION" :report report})))
                          _ (swap! subject assoc
                                   :core-artifact-id (:artifact-id core)
                                   :core-verification (:status report)
                                   :recursion-components (get-in core [:gravity-core-boundary :canonical-core-artifact :recursion-components])
                                   :functions (mapv #(select-keys % [:ordinal :definition-name :function-syntax-id :function-core-node-id])
                                                    (get-in core [:gravity-core-boundary :canonical-core-artifact :function-records])))
                          request (type-request component core report @type-requests)
                          _ (reset! stage :function-types)
                          _ (progress! {:component (:component-id component) :stage @stage :status :started})
                          typed (type-api 'invoke-c7 'sh08-function-type-core-artifact [request])]
                      (require-type-result! typed :accepted)
                      (reset! stage :type-verification)
                      (progress! {:component (:component-id component) :stage @stage :status :started})
                      (require-type-result!
                       (type-api 'invoke-c7 'sh08-verify-function-type-result [request typed]) :passed)
                      (swap! type-requests assoc (:source-module component) request))
                    (swap! completed conj {:component-id (:component-id component)
                                           :source-path (:source-path component)
                                           :source-sha256 (sha256 (.getBytes text "UTF-8"))
                                           :status :accepted})
                    (progress! {:component (:component-id component) :stage @stage :status :accepted})
                    (recur (next remaining) (assoc resolved (:source-module component) resolution)))
                  {:status :scoped-pass}))))
          (catch OutOfMemoryError exception
            (classify-failure @current @stage exception))
          (catch StackOverflowError exception
            (classify-failure @current @stage exception))
          (catch Exception exception
            (classify-failure @current @stage exception)))
        observation-route (route @closure)
        unchanged? (= source-inputs (inputs root))
        outcome (if unchanged?
                  outcome
                  {:status :blocked
                   :failure {:id "COMPILER-PROBE-INPUT-CHANGED" :kind :stale-evidence
                             :reason "Relevant source bytes changed during execution."}})]
    (merge {:source-revision source-revision
            :inputs source-inputs :route observation-route
            :input-id (digest {:inputs source-inputs :route observation-route})
            :completed @completed
            :subject @subject
            :position {:component (count @completed) :stage @stage}
            :terminal {:status (if (= :scoped-pass (:status outcome)) :passed :failed)
                       :exit (if (= :scoped-pass (:status outcome)) 0 1)}}
           outcome)))

(defn -main [& arguments]
  (when (seq arguments)
    (throw (ex-info "compiler-probe uses the declared current source closure without filters" {:arguments arguments})))
  (let [observation (observe (System/getProperty "user.dir"))]
    (prn observation)
    (flush)
    (shutdown-agents)
    (System/exit (get-in observation [:terminal :exit]))))
