(ns gravity.development-test-runner
  "Namespace-lazy, deterministic feedback runner for explicit development tests.

  The legacy catalog remains an independently reviewed compatibility oracle.
  Optional catalog and warm-worker queries validate the UEP projection.
  Explicit test selections do not require that admission metadata."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test])
  (:import [java.lang.management ManagementFactory]))

(def ^:private runner-loaded-at-epoch-ms (System/currentTimeMillis))
(def ^:dynamic *timing-events* nil)
(def ^:dynamic *machine-result-sink*
  "Optional sink for one closed, non-authoritative execution result.

  The normal CLI remains an integer-returning human-facing API.  The bounded
  warm worker binds this sink so it can transport the counters produced by
  `clojure.test` instead of inferring them from captured text or selectors."
  nil)

(def machine-result-schema :gravity/development-test-result-v1)

(def ^:private ^:dynamic *progress-state* nil)

(defn- progress-state []
  {:errors (java.util.ArrayList.)
   :inspected (java.util.IdentityHashMap.)
   :interrupt? (boolean-array [(.isInterrupted (Thread/currentThread))])})

(defn- retain-progress-error! [state error]
  (let [^java.util.ArrayList errors (:errors state)]
    (when-not (some #(identical? error %) (seq (.toArray errors)))
      (.add errors error))))

(defn- inspect-progress-error [root]
  (let [seen (java.util.IdentityHashMap.)]
    (loop [todo [root] interrupted? false faults []]
      (if-let [^Throwable error (peek todo)]
        (if (.containsKey seen error)
          (recur (pop todo) interrupted? faults)
          (do
            (.put seen error true)
            (let [cause (try {:value (.getCause error)}
                             (catch Throwable fault {:fault fault}))
                  suppressed (try {:value (vec (.getSuppressed error))}
                                  (catch Throwable fault {:fault fault}))
                  failures (vec (keep :fault [cause suppressed]))]
              (recur (into (cond-> (pop todo) (:value cause) (conj (:value cause)))
                           (:value suppressed))
                     (or interrupted? (instance? InterruptedException error)
                         (boolean (some #(instance? InterruptedException %) failures)))
                     (into faults failures)))))
        {:interrupted? interrupted? :faults faults :complete? (empty? faults)}))))

(defn- note-progress-interrupt! [state error]
  ;; Direct knowledge survives even if the inspection helper itself fails.
  (when (instance? InterruptedException error)
    (aset-boolean ^booleans (:interrupt? state) 0 true))
  (let [^java.util.IdentityHashMap inspected (:inspected state)]
    (when-not (.containsKey inspected error)
      (.put inspected error true)
      (let [report (try (inspect-progress-error error)
                        (catch Throwable fault
                          {:interrupted? (instance? InterruptedException fault)
                           :faults [fault] :complete? false}))]
        (when (seq (:faults report))
          ;; An unreadable edge is not an interrupted cause. Never inspect the
          ;; accessor/internal failures recursively or render their objects.
          (retain-progress-error! state error)
          (doseq [fault (:faults report)] (retain-progress-error! state fault)))
        (when (:interrupted? report)
          (aset-boolean ^booleans (:interrupt? state) 0 true))))))

(defn- emit-progress! [phase edge namespace test-var error]
  (try
    (let [frame (cond-> {:schema :gravity/development-test-progress-v1
                         :authority :non-authoritative :phase phase :edge edge
                         :namespace namespace}
                  test-var (assoc :test-var test-var)
                  error (assoc :error-class (.getName (class error))))]
      (.write ^java.io.Writer *err*
              (binding [*print-length* nil *print-level* nil *print-meta* false
                        *print-readably* true *print-dup* false]
                (str (pr-str frame) "\n")))
      (if (instance? java.io.PrintWriter *err*)
        ;; PrintWriter can hide IOException. checkError performs the flush;
        ;; the hidden exception identity is unavailable, never invented.
        (when (.checkError ^java.io.PrintWriter *err*)
          (throw (ex-info "Progress stderr reported a write/flush failure"
                          {:id "DEV-PROGRESS-WRITE" :original-IO-error :unavailable})))
        (.flush ^java.io.Writer *err*)))
    (catch Throwable diagnostic
      ;; Retain BEFORE any fallible accessor/causal inspection. Diagnostics
      ;; cannot skip a selected call or replace its value inside a fixture.
      (retain-progress-error! *progress-state* diagnostic)
      (note-progress-interrupt! *progress-state* diagnostic))))

(defn- observe-progress! [phase namespace test-var body]
  (emit-progress! phase :enter namespace test-var nil)
  (let [outcome (try {:value (body)}
                     (catch Throwable error
                       (note-progress-interrupt! *progress-state* error)
                       {:error error}))]
    (emit-progress! phase (if (:error outcome) :throw :return)
                    namespace test-var (:error outcome))
    (if-let [error (:error outcome)] (throw error) (:value outcome))))

(defmacro ^:private with-progress-phase [phase namespace test-var & body]
  `(if *progress-state*
     (observe-progress! ~phase (str ~namespace) ~test-var (fn [] ~@body))
     (do ~@body)))

(defn- require-progress-success! []
  (when (and *progress-state* (not (.isEmpty ^java.util.ArrayList (:errors *progress-state*))))
    (throw (.get ^java.util.ArrayList (:errors *progress-state*) 0))))

(defn- run-with-progress! [state body]
  (binding [*progress-state* state]
    (try
      (let [outcome (try {:value (body)}
                         (catch Throwable error
                           (note-progress-interrupt! state error)
                           {:error error}))
            errors (vec (.toArray ^java.util.ArrayList (:errors state)))
            primary (or (:error outcome) (first errors))]
        (if primary
          (do
            (doseq [error errors]
              (when-not (or (identical? primary error)
                            (some #(identical? error %) (.getSuppressed ^Throwable primary)))
                (.addSuppressed ^Throwable primary error)))
            (throw primary))
          (:value outcome)))
      (finally
        (when (aget ^booleans (:interrupt? state) 0)
          (.interrupt (Thread/currentThread)))))))

(defn- machine-result-file-sink
  "One completion delivery. Request equality is binding data, not source or
  effective-configuration attestation. Does not own the parent directory."
  [{:keys [file limit-bytes request] :as delivery} inherited]
  (when-not (and (= #{:file :limit-bytes :request} (set (keys delivery)))
                 (string? file) (.isAbsolute (io/file file))
                 (integer? limit-bytes) (pos? limit-bytes)
                 (< limit-bytes Integer/MAX_VALUE)
                 (= #{:id :source :selection} (set (keys request)))
                 (string? (:id request)) (seq (:id request))
                 (map? (:source request)) (map? (:selection request)))
    (throw (ex-info "Invalid development result delivery" {:id "DEV-RESULT-DELIVERY"})))
  (let [emitted? (atom false)]
    (fn [result]
      (when-not (compare-and-set! emitted? false true)
        (throw (ex-info "Development result was emitted more than once"
                        {:id "DEV-RESULT-DUPLICATE"})))
      (when-not (= (:selection request) (:selection result))
        (throw (ex-info "Development result selection differs from request"
                        {:id "DEV-RESULT-SELECTION"})))
      (let [bytes (.getBytes (str (pr-str {:schema :gravity/development-test-result-delivery-v1
                                          :request request :result result}) "\n") "UTF-8")]
        (when (> (alength bytes) limit-bytes)
          (throw (ex-info "Development result exceeds declared capture limit"
                          {:id "DEV-RESULT-LIMIT" :bytes (alength bytes) :limit limit-bytes})))
        (java.nio.file.Files/write
         (.toPath (io/file file)) bytes
         (into-array java.nio.file.OpenOption
                     [java.nio.file.StandardOpenOption/CREATE_NEW
                      java.nio.file.StandardOpenOption/WRITE java.nio.file.LinkOption/NOFOLLOW_LINKS])))
      (when inherited (inherited result)))))

(defn result-delivery-sink
  "Construct the existing one-shot normal completion writer in the child JVM.
  Reuses the closed delivery validator, supplied capture allowance and original
  IO/error behavior. Calls inherited only after the CREATE_NEW write succeeds.
  Owns neither the output directory nor semantic/custody admission."
  [delivery inherited]
  (machine-result-file-sink delivery inherited))


(def namespace-catalog
  [{:namespace 'gravity.bootstrap-test
    :path "bootstrap/clojure/test/gravity/bootstrap_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c2-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj"}
   {:namespace 'gravity.c2-pass-cache-test
    :path "bootstrap/clojure/test/gravity/c2_pass_cache_test.clj"
    :selectors
    ["gravity.c2-pass-cache-test/canonical-semantic-key-is-bounded-type-sensitive-and-path-scoped"
     "gravity.c2-pass-cache-test/bounded-source-snapshot-rejects-size-links-and-traversal"
     "gravity.c2-pass-cache-test/generic-v2-adapter-preserves-c2-results-and-ignores-v1"
     "gravity.c2-pass-cache-test/generic-v2-adapter-binds-current-c2-producer-policy"
     "gravity.c2-pass-cache-test/generic-v2-adapter-corruption-is-rejected-and-not-replaced"
     "gravity.c2-pass-cache-test/generic-v2-adapter-direct-store-mints-a-current-receipt"
     "gravity.c2-pass-cache-test/generic-v2-adapter-same-key-concurrency-executes-one-c2-producer"
     "gravity.c2-pass-cache-test/generic-v2-adapter-preserves-opaque-c2-size-and-depth-profile"
     "gravity.c2-pass-cache-test/leaf-contract-is-explicitly-local-and-nonauthoritative"
     "gravity.c2-pass-cache-test/ordinary-bootstrap-c2-path-reuses-without-reader-execution"
     "gravity.c2-pass-cache-test/ordinary-bootstrap-c2-path-invalidates-changed-source"]}
   {:namespace 'gravity.bootstrap-compatibility.c3-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.module-analysis-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/module_analysis_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.core-ast-lowering-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/core_ast_lowering_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.profile-validation-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/profile_validation_test.clj"
    :selectors
    ["gravity.bootstrap-compatibility.profile-validation-test/profile-facades-match-head-4921fbc-reference-table"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-head-reference-policy-denial-matrix-and-dynamic-seams"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-downstream-caller-artifacts-retain-head-shape"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-facades-preserve-public-arglists-and-exact-leaf-parity"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-policy-map-redefs-reach-the-leaf-through-the-central-seam"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-registry-function-seams-match-head-4921fbc-ownership"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-validation-facade-preserves-central-diagnostics-and-target-gates"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-captured-original-interposition-is-one-shot"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-leaf-operation-interposition-is-observable-through-facade"]}
   {:namespace 'gravity.bootstrap-compatibility.capability-validation-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/capability_validation_test.clj"
    :selectors
    ["gravity.bootstrap-compatibility.capability-validation-test/capability-facades-preserve-arglists-and-explicit-pass-parity"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-final-authority-narrows-trust-without-rewriting-legacy-row"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-policy-and-provider-seams-remain-interposable"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-diagnostic-policy-scalar-reaches-leaf-pass-contract"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-diagnostics-preserve-source-context-and-stable-ids"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-provider-name-matches-head-4921fbc-reference-table"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-captured-original-provider-interposition-is-one-shot"]}
   {:namespace 'gravity.bootstrap-compatibility.c4-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c4_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c5-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c5_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c6-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c6_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c7-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c7_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c8-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c8_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c9-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c9_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c10-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c10_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c11-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c11_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c12-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c12_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c13-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c13_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c14-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c14_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c15-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c15_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c16-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c16_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c17-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c17_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c18-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c18_test.clj"}])

(def ^:private default-test-namespace 'gravity.bootstrap-test)
;; Keep this binding as the immutable compatibility oracle.  It is deliberately
;; not rewritten when the manifest projection is read.
(def ^:private legacy-namespace-catalog namespace-catalog)
(def ^:private catalog-by-namespace
  (into {} (map (juxt :namespace identity) namespace-catalog)))

(def ^:private maximum-namespace-authority-entries 256)

(defn- authority-failure
  [id message data]
  (throw (ex-info message (assoc data :id id))))

(defn- generated-namespace-authority
  "Read and validate the manifest projection at the consumer boundary.

  Binding the projection to `uep/catalog` here keeps a stale or independently
  fabricated projection from becoming namespace authority."
  []
  (let [stable-exception? (fn [error]
                            (let [id (:id (ex-data error))]
                              (and (string? id)
                                   (or (str/starts-with? id "UEP-")
                                       (str/starts-with? id "DEV-")))))
        invoke-authority (fn [id message data f]
                           (try
                             (f)
                             (catch clojure.lang.ExceptionInfo error
                               (if (stable-exception? error)
                                 (throw error)
                                 (authority-failure id message
                                                    (assoc data :class (.getName (class error))))))
                             (catch Exception error
                               (authority-failure id message
                                                  (assoc data :class (.getName (class error)))))))
        resolve-var (fn [symbol]
                      (or (invoke-authority
                           "DEV-NAMESPACE-AUTHORITY-RESOLVE"
                           "manifest namespace authority could not be loaded"
                           {:symbol symbol}
                           #(requiring-resolve symbol))
                          (authority-failure
                           "DEV-NAMESPACE-AUTHORITY-RESOLVE"
                           "manifest namespace authority function is unavailable"
                           {:symbol symbol})))
        resolved-value (fn [symbol]
                         (let [resolved (resolve-var symbol)]
                           (if (var? resolved)
                             (invoke-authority
                              "DEV-NAMESPACE-AUTHORITY-RESOLVE"
                              "manifest namespace authority could not be evaluated"
                              {:symbol symbol}
                              #(var-get resolved))
                             resolved)))]
    (let [projection-fn (resolved-value
                         'gravity.self-hosting.unified-execution-plan/namespace-authority-projection)
          validator-fn (resolved-value
                        'gravity.self-hosting.unified-execution-plan/validate-namespace-authority-projection)
          catalog (resolved-value
                   'gravity.self-hosting.unified-execution-plan/catalog)]
      (when-not (fn? projection-fn)
        (authority-failure "DEV-NAMESPACE-AUTHORITY-RESOLVE"
                           "manifest projection resolver returned a non-callable value" {}))
      (when-not (fn? validator-fn)
        (authority-failure "DEV-NAMESPACE-AUTHORITY-RESOLVE"
                           "manifest projection validator returned a non-callable value" {}))
      (let [projection (invoke-authority
                        "DEV-NAMESPACE-AUTHORITY-RESOLVE"
                        "manifest namespace authority could not be evaluated"
                        {}
                        projection-fn)]
        (let [validated (invoke-authority
                         "DEV-NAMESPACE-AUTHORITY-VALIDATION"
                         "manifest namespace authority validation failed"
                         {}
                         #(validator-fn catalog projection))]
          (when-not (= validated projection)
            (authority-failure
             "DEV-NAMESPACE-AUTHORITY-VALIDATION"
             "manifest namespace authority validator returned a different projection"
             {})))
        projection))))

(defn- equivalent-namespace-entry?
  [legacy generated]
  (= (select-keys legacy [:namespace :path :selectors])
     (select-keys generated [:namespace :path :selectors])))

(defn- ensure-legacy-catalog-unchanged!
  []
  (when-not (= namespace-catalog legacy-namespace-catalog)
    (authority-failure
     "DEV-NAMESPACE-CATALOG-DRIFT"
     "public legacy namespace catalog differs from its reviewed oracle"
     {:expected-count (count legacy-namespace-catalog)
      :actual-count (count namespace-catalog)})))

(defn namespace-authority
  "Return the compatibility catalog plus validated fast-route entries.

  Existing legacy entries retain their ordering and shape.  On an intentional
  namespace overlap, the generated route ownership is merged only after the
  legacy path and selector set match exactly; otherwise admission fails closed.
  The result is non-authoritative and is used solely for bounded development
  selection and warm-worker admission."
  []
  (let [_ (ensure-legacy-catalog-unchanged!)
        live-legacy-namespace-catalog namespace-catalog
        generated (generated-namespace-authority)
        generated-by (into {} (map (juxt :namespace identity) (:entries generated)))
        legacy-by (into {} (map (juxt :namespace identity)
                                live-legacy-namespace-catalog))
        overlaps (set/intersection (set (keys legacy-by)) (set (keys generated-by)))]
    (doseq [namespace (sort-by str overlaps)]
      (when-not (equivalent-namespace-entry? (get legacy-by namespace)
                                              (get generated-by namespace))
        (authority-failure
         "DEV-NAMESPACE-AUTHORITY-PARITY"
         "legacy and manifest namespace authority entries disagree"
         {:namespace namespace
          :legacy (get legacy-by namespace)
          :generated (get generated-by namespace)})))
    (let [entries
          (vec
           (concat
            (mapv (fn [legacy]
                    (if-let [generated-entry (get generated-by (:namespace legacy))]
                      (merge legacy generated-entry)
                      legacy))
                  live-legacy-namespace-catalog)
            (->> (:entries generated)
                 (remove #(contains? legacy-by (:namespace %)))
                 (sort-by #(str (:namespace %))))))]
      (when-not (<= 1 (count entries) maximum-namespace-authority-entries)
        (authority-failure "DEV-NAMESPACE-AUTHORITY-BOUNDS"
                           "namespace authority entry count exceeds bound"
                           {:count (count entries)}))
      {:projection generated
       :entries entries
       :generated-namespaces
       (->> (:entries generated)
            (map :namespace)
            (map str)
            sort
            vec)})))

(def ^:private usage-text
  (str
   "Usage: clojure -M:<alias> -m gravity.development-test-runner [options]\n"
   "\n"
   "Runs gravity.bootstrap-test by default, or explicitly selected test namespaces.\n"
   "With no selector, all tests in the selected namespace set run.\n"
   "\n"
   "Options (repeatable):\n"
   "  --namespace NS    select a gravity.*-test resource on the test classpath\n"
   "  --exact NAME      select NAME or a selected namespace/NAME exactly\n"
   "  --regex REGEX     select names matching REGEX\n"
   "  --prefix PREFIX   select names beginning with PREFIX\n"
   "  --list            print selected qualified names without running tests\n"
   "  --catalog         print the reviewed namespace/path catalog without loading tests\n"
   "  --fail-fast       stop after the first test failure or error\n"
   "  --progress        write non-authoritative phase breadcrumbs to stderr\n"
   "  --timing-receipt FILE\n"
   "                    write non-authoritative machine-readable EDN timing telemetry\n"
   "  --machine-result EDN\n"
   "                    deliver one closed completion record to the parent request\n"
   "  --help            print this help without loading tests\n"
   "\n"
   "Qualified selectors resolve only within explicitly selected namespaces.\n"))

(defn- selector-value [option value]
  (let [value (some-> value str/trim)]
    (when (str/blank? value)
      (throw (ex-info (str option " requires a non-empty value")
                      {:type ::invalid-selector :option option})))
    value))

(defn- parse-args [args]
  (loop [remaining (seq args)
         options {:namespaces [] :exact [] :regex [] :prefix []
                  :list? false :catalog? false :fail-fast? false :help? false
                  :timing-receipt nil}]
    (if-not remaining
      options
      (let [argument (first remaining)
            tail (next remaining)]
        (cond
          (= argument "--help") (recur tail (assoc options :help? true))
          (= argument "--list") (recur tail (assoc options :list? true))
          (= argument "--catalog") (recur tail (assoc options :catalog? true))
          (= argument "--fail-fast") (recur tail (assoc options :fail-fast? true))
          (= argument "--progress") (recur tail (assoc options :progress? true))

          (= argument "--timing-receipt")
          (if (seq tail)
            (recur (next tail)
                   (assoc options :timing-receipt
                          (selector-value argument (first tail))))
            (throw (ex-info "--timing-receipt requires a non-empty value"
                            {:type ::invalid-selector :option argument})))

          (= argument "--machine-result")
          (if (and (seq tail) (not (contains? options :machine-result)))
            (recur (next tail)
                   (assoc options :machine-result (edn/read-string (first tail))))
            (throw (ex-info "--machine-result requires one unique delivery argument"
                            {:id "DEV-RESULT-DELIVERY"})))

          (= argument "--namespace")
          (if (seq tail)
            (recur (next tail)
                   (update options :namespaces conj
                           (symbol (selector-value argument (first tail)))))
            (throw (ex-info "--namespace requires a non-empty value"
                            {:type ::invalid-selector :option argument})))

          (or (= argument "--exact") (= argument "-e"))
          (if (seq tail)
            (recur (next tail)
                   (update options :exact conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector :option argument})))

          (or (= argument "--regex") (= argument "--pattern") (= argument "-r"))
          (if (seq tail)
            (recur (next tail)
                   (update options :regex conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector :option argument})))

          (or (= argument "--prefix") (= argument "-p"))
          (if (seq tail)
            (recur (next tail)
                   (update options :prefix conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector :option argument})))

          (= argument "--")
          (recur nil (update options :exact into
                             (map #(selector-value "selector" %) tail)))

          (str/starts-with? argument "-")
          (throw (ex-info (str "unknown option: " argument)
                          {:type ::usage-error :option argument}))

          :else
          (recur tail
                 (update options :exact conj
                         (selector-value "selector" argument))))))))
(defn- selected-namespace-records [{:keys [namespaces] :as options}]
  ;; Explicit local tests are development feedback, not an authority-admission
  ;; request. Keep manifest validation at namespace-authority's opt-in callers
  ;; (catalog/warm-worker); do not make it a prerequisite for editing a pass.
  (ensure-legacy-catalog-unchanged!)
  (mapv
   (fn [namespace]
     (or (get catalog-by-namespace namespace)
         (let [name (str namespace)
               resource (str (-> name (str/replace "." "/")
                                 (str/replace "-" "_")) ".clj")]
           (when-not (and (re-matches #"gravity(?:\.[A-Za-z0-9_-]+)+-test" name)
                          (io/resource resource))
             (throw (ex-info (str "unknown test namespace: " namespace)
                             {:type ::unknown-namespace :namespace namespace})))
           {:namespace namespace
            :path (str "bootstrap/clojure/test/" resource)})))
   (let [requested (if (seq namespaces) (set namespaces) #{default-test-namespace})]
     (concat (filter requested (map :namespace legacy-namespace-catalog))
             (sort (remove #(contains? catalog-by-namespace %) requested))))))

(defn- load-selected-namespaces! [namespace-records]
  (doseq [{:keys [namespace]} namespace-records]
    (if *timing-events*
      (let [already-loaded? (boolean (find-ns namespace))
            started (System/nanoTime)]
        (try
          (with-progress-phase :namespace-load namespace nil (require namespace))
          (swap! *timing-events* update :namespace-loads conj
                 {:namespace (str namespace)
                  :already-loaded? already-loaded?
                  :elapsed-ns (- (System/nanoTime) started)
                  :outcome :loaded})
          (catch Throwable ex
            (swap! *timing-events* update :namespace-loads conj
                   {:namespace (str namespace)
                    :already-loaded? already-loaded?
                    :elapsed-ns (- (System/nanoTime) started)
                    :outcome :error
                    :error-class (.getName (class ex))})
            (throw ex))))
      (with-progress-phase :namespace-load namespace nil (require namespace)))))

(defn- test-var-records [namespace-records]
  (->> namespace-records
       (mapcat
        (fn [{:keys [namespace]}]
          (let [namespace-object (the-ns namespace)]
            (keep (fn [[name test-var]]
                    (when (:test (meta test-var))
                      (let [short-name (str name)]
                        {:name short-name
                         :qualified-name (str namespace "/" short-name)
                         :namespace namespace
                         :namespace-object namespace-object
                         :var test-var})))
                  (ns-interns namespace-object)))))
       (sort-by :qualified-name)
       vec))

(defn- matches-name? [record selector]
  (or (= selector (:name record)) (= selector (:qualified-name record))))

(defn- matches-prefix? [record prefix]
  (or (str/starts-with? (:name record) prefix)
      (str/starts-with? (:qualified-name record) prefix)))

(defn- compile-patterns [patterns]
  (mapv (fn [pattern]
          (try
            (re-pattern pattern)
            (catch java.util.regex.PatternSyntaxException ex
              (throw (ex-info (str "invalid regex selector " (pr-str pattern) ": "
                                  (.getDescription ex))
                              {:type ::invalid-selector :selector pattern} ex)))))
        patterns))

(defn- matches-regex? [record patterns]
  (some #(or (re-find % (:name record))
             (re-find % (:qualified-name record)))
        patterns))

(defn- select-test-vars
  ([records options]
   (select-test-vars records options (compile-patterns (:regex options))))
  ([records {:keys [exact regex prefix]} compiled-patterns]
   (let [selector-supplied? (or (seq exact) (seq regex) (seq prefix))
         selected (->> records
                       (filter (fn [record]
                                 (or (not selector-supplied?)
                                     (some #(matches-name? record %) exact)
                                     (some #(matches-prefix? record %) prefix)
                                     (matches-regex? record compiled-patterns))))
                       vec)]
     (doseq [selector exact]
       (when-not (some #(matches-name? % selector) records)
         (throw (ex-info (str "unknown exact test selector: " selector)
                         {:type ::unknown-selector :selector selector :kind :exact}))))
     (doseq [selector prefix]
       (when-not (some #(matches-prefix? % selector) records)
         (throw (ex-info (str "selector matched no test vars: --prefix " selector)
                         {:type ::unknown-selector :selector selector :kind :prefix}))))
     (doseq [[selector pattern] (map vector regex compiled-patterns)]
       (when-not (some #(or (re-find pattern (:name %))
                            (re-find pattern (:qualified-name %))) records)
         (throw (ex-info (str "selector matched no test vars: --regex " selector)
                         {:type ::unknown-selector :selector selector :kind :regex}))))
     selected)))

(defn- report-summary [summary]
  (test/do-report (assoc summary :type :summary))
  summary)

(defn- run-selected-tests [namespace-records selected-records fail-fast?]
  (binding [test/*report-counters* (ref test/*initial-report-counters*)]
    (let [stopped? (atom false)
          selected-by-namespace (group-by :namespace selected-records)]
      (doseq [{:keys [namespace]} namespace-records
              :let [records (get selected-by-namespace namespace)]
              :when (and (seq records) (not @stopped?))]
        (let [namespace-object (the-ns namespace)
              namespace-started (when *timing-events* (System/nanoTime))
              namespace-counters-before (when *timing-events*
                                          @test/*report-counters*)
              namespace-events (when *timing-events* (atom []))
              once-fixture-fn (test/join-fixtures
                               (::test/once-fixtures (meta namespace-object)))
              each-fixture-fn (test/join-fixtures
                               (::test/each-fixtures (meta namespace-object)))]
          (test/do-report {:type :begin-test-ns :ns namespace-object})
          (with-progress-phase :namespace-fixture namespace nil
          (once-fixture-fn
           (fn []
             (doseq [{:keys [var qualified-name]} records :while (not @stopped?)]
               (if *timing-events*
                 (let [before @test/*report-counters*
                       started (System/nanoTime)]
                   (with-progress-phase :each-fixture namespace qualified-name
                     (each-fixture-fn #(with-progress-phase :test-var namespace qualified-name
                                        (test/test-var var))))
                   (let [after @test/*report-counters*
                         counts (merge-with - after before)
                         outcome (cond
                                   (pos? (:error counts)) :error
                                   (pos? (:fail counts)) :failed
                                   :else :passed)]
                     (swap! namespace-events conj
                            {:test-var qualified-name
                             :elapsed-ns (- (System/nanoTime) started)
                             :outcome outcome
                             :counts counts})))
                 (with-progress-phase :each-fixture namespace qualified-name
                   (each-fixture-fn #(with-progress-phase :test-var namespace qualified-name
                                      (test/test-var var)))))
               (when (and fail-fast?
                          (pos? (+ (:fail @test/*report-counters*)
                                   (:error @test/*report-counters*))))
                 (reset! stopped? true))))))
          (test/do-report {:type :end-test-ns :ns namespace-object})
          (when *timing-events*
            (let [counts (merge-with - @test/*report-counters*
                                     namespace-counters-before)]
              (swap! *timing-events* update :namespace-executions conj
                     {:namespace (str namespace)
                      :elapsed-ns (- (System/nanoTime) namespace-started)
                      :outcome (cond
                                 (pos? (:error counts)) :error
                                 (pos? (:fail counts)) :failed
                                 :else :passed)
                      :counts counts
                      :test-vars @namespace-events})))))
      (report-summary @test/*report-counters*))))

(defn- bounded-property [property]
  (let [value (System/getProperty property "")]
    (subs value 0 (min 160 (count value)))))

(defn- runtime-metadata []
  {:java-version (bounded-property "java.version")
   :java-vendor (bounded-property "java.vendor")
   :vm-name (bounded-property "java.vm.name")
   :os-name (bounded-property "os.name")
   :os-arch (bounded-property "os.arch")
   :available-processors (.availableProcessors (Runtime/getRuntime))
   :max-heap-bytes (.maxMemory (Runtime/getRuntime))})

(defn- write-timing-receipt!
  [path options namespace-records selected-records events summary elapsed-ns]
  (let [jvm-started-at (.getStartTime (ManagementFactory/getRuntimeMXBean))
        receipt
        {:schema "gravity/development-verification-timing-v1"
         :authority :non-authoritative
         :authoritative? false
         :purpose :development-scheduling-input
         :clock {:elapsed :monotonic-nanoseconds
                 :jvm-boundary :epoch-milliseconds}
         :metadata-bounds {:runtime-string-code-units 160}
         :runtime (runtime-metadata)
         :loading
         {:jvm-start-to-runner-load
          {:elapsed-ns (* 1000000 (max 0 (- runner-loaded-at-epoch-ms
                                             jvm-started-at)))
           :includes [:jvm-bootstrap :runner-namespace-loading]
           :separable? false
           :approximate? true
           :outcome :observed}
          :namespaces (:namespace-loads events)}
         :execution
         {:elapsed-ns elapsed-ns
          :namespaces (:namespace-executions events)}
         :selection
         {:namespaces (mapv (comp str :namespace) namespace-records)
          :test-vars (mapv :qualified-name selected-records)
          :fail-fast? (:fail-fast? options)}
         :outcome (cond
                    (pos? (:error summary)) :error
                    (pos? (:fail summary)) :failed
                    (= (:test summary) (count selected-records)) :passed
                    :else :incomplete)
         :summary summary}]
    (spit path (str (pr-str receipt) "\n"))))

(defn- print-selection [selected-records]
  (doseq [{:keys [qualified-name]} selected-records]
    (println qualified-name))
  (flush))

(defn- print-catalog []
  (doseq [{:keys [namespace path]} (:entries (namespace-authority))]
    (println (str namespace "\t" path)))
  (flush))

(defn- run-cli-with-options! [options]
  (let [sink (if-let [delivery (:machine-result options)]
               (result-delivery-sink delivery *machine-result-sink*)
               *machine-result-sink*)]
    (when (and (contains? options :machine-result)
               (or (nil? (:machine-result options)) (:help? options) (:catalog? options) (:list? options)))
      (throw (ex-info "Machine delivery requires an executed selection"
                      {:id "DEV-RESULT-DELIVERY"})))
    (binding [*machine-result-sink* sink]
    (cond
      (:help? options) (do (print usage-text) 0)
      (:catalog? options) (do (print-catalog) 0)
      :else
      (let [namespace-records (selected-namespace-records options)
            compiled-patterns (compile-patterns (:regex options))
            timing-events (when (:timing-receipt options)
                            (atom {:namespace-loads [] :namespace-executions []}))]
        (binding [*timing-events* timing-events]
          (load-selected-namespaces! namespace-records)
          (let [records (test-var-records namespace-records)
                selected-records (select-test-vars records options compiled-patterns)]
            (when (empty? selected-records)
              (throw (ex-info "selected namespaces contain no test vars"
                              {:type ::empty-selection
                               :namespaces (mapv :namespace namespace-records)})))
            (if (:list? options)
              (do (print-selection selected-records) 0)
              (let [execution-started (when (:timing-receipt options)
                                        (System/nanoTime))
                    summary (run-selected-tests namespace-records selected-records
                                                (:fail-fast? options))
                    elapsed-ns (when execution-started
                                 (- (System/nanoTime) execution-started))]
                (require-progress-success!)
                (when-let [path (:timing-receipt options)]
                  (write-timing-receipt! path options namespace-records
                                         selected-records @timing-events summary
                                         elapsed-ns))
                (let [passed? (and (zero? (:fail summary))
                                   (zero? (:error summary))
                                   (= (:test summary) (count selected-records)))
                      exit-code (if passed? 0 1)
                      result {:schema machine-result-schema
                              :authority :non-authoritative
                              :authoritative? false
                              :selection {:namespaces
                                          (mapv (comp str :namespace)
                                                namespace-records)
                                          :test-vars
                                          (mapv :qualified-name selected-records)
                                          :fail-fast? (:fail-fast? options)}
                              :summary (select-keys summary
                                                    [:test :pass :fail :error])
                              :outcome (if passed? :passed :failed)
                              :exit-code exit-code}]
                  (when *machine-result-sink*
                    (*machine-result-sink* result))
                  exit-code))))))))))

(defn run-cli! [args]
  (let [options (parse-args args)]
    (if (:progress? options)
      (run-with-progress! (progress-state) #(run-cli-with-options! options))
      (binding [*progress-state* nil]
        (run-cli-with-options! options)))))

(defn- report-cli-error [^Throwable ex]
  (binding [*out* *err*]
    (println (str "development-test-runner: " (.getMessage ex)))
    (when (= ::usage-error (:type (ex-data ex)))
      (println "Try --help for usage."))
    (flush)))

(defn -main [& args]
  (try
    (System/exit (run-cli! args))
    (catch clojure.lang.ExceptionInfo ex
      (report-cli-error ex)
      (System/exit 2))
    (catch Throwable ex
      (report-cli-error ex)
      (System/exit 2))))
