(ns ci.run
  "CI orchestration only. Receipts are observations, never compiler authority."
  (:refer-clojure :exclude [run! descendants])
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.lang ProcessHandle]
           [java.util.concurrent TimeUnit]))

(def output "target/ci")
(def runtime-deps "{:deps {org.clojure/clojure {:mvn/version \"1.12.5\"}}}")
(def semantic-modes #{"static" "contracts" "bootstrap" "macro" "resolution" "checked-core-runtime"})
(def always-pr-modes #{"static" "contracts"})
(def stage-order ["bootstrap" "macro" "resolution" "checked-core-runtime"])
(def leaf-modes #{"leaves-portable" "leaves-darwin"})

;; These are the existing owner boundaries exposed by the CI matrix.  Keep
;; this table deliberately small: an unlisted path is cross-cutting and must
;; retain the full matrix rather than being guessed into one stage.
(def stage-path-prefixes
  {"bootstrap" ["bootstrap/gravity/src/gravity/reader.gravity"
                "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
                "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity"
                "bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity"
                "bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity"
                "bootstrap/clojure/src/gravity/reader.clj"
                "bootstrap/clojure/src/gravity/reader_primitives.clj"
                "bootstrap/clojure/src/gravity/reader_cursor.clj"
                "bootstrap/clojure/src/gravity/reader_host_oracle.clj"
                "bootstrap/clojure/src/gravity/reader_diagnostic_policy.clj"
                "bootstrap/clojure/src/gravity/reader_namespace.clj"
                "bootstrap/clojure/src/gravity/c2_reader_product_projection"
                "bootstrap/clojure/src/gravity/c2_reader_diagnostics"
                "bootstrap/clojure/src/gravity/c3_literal_projection"
                "bootstrap/clojure/src/gravity/c3_reader_expansions.clj"
                "bootstrap/clojure/src/gravity/c3_reader_integrity"
                "bootstrap/clojure/src/gravity/c3_reader_integrity.clj"
                "bootstrap/clojure/src/gravity/c3_syntax_construction"
                "bootstrap/clojure/src/gravity/c3_syntax_construction.clj"
                "bootstrap/clojure/src/gravity/c3_syntax_diagnostics"
                "bootstrap/clojure/src/gravity/c3_syntax_diagnostics.clj"
                "bootstrap/clojure/src/gravity/c3_syntax_evidence.clj"
                "bootstrap/clojure/src/gravity/c3_syntax_verification"
                "bootstrap/clojure/src/gravity/c3_syntax_verification.clj"
                "bootstrap/clojure/src/gravity/c3_artifact_identity.clj"
                "bootstrap/clojure/src/gravity/syntax.clj"
                "bootstrap/clojure/src/gravity/syntax_object_stream.clj"
                "bootstrap/clojure/src/gravity/syntax_origin.clj"
                "bootstrap/clojure/test/gravity/reader"
                "bootstrap/clojure/test/gravity/reader_"
                "bootstrap/clojure/test/gravity/c2_reader_"
                "bootstrap/clojure/test/gravity/c3_"
                "bootstrap/clojure/test/gravity/syntax"
                "bootstrap/clojure/test/gravity/syntax_"
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj"
                "bootstrap/clojure/test/gravity/self_hosting/sh03_"
                "bootstrap/clojure/test/gravity/self_hosting/sh04_"
                "bootstrap/clojure/fixtures/self-hosting/sh-03"
                "bootstrap/clojure/fixtures/self-hosting/sh-04"]
   "macro" ["bootstrap/gravity/src/gravity/macro.gravity"
            "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity"
            "bootstrap/clojure/src/gravity/macro.clj"
            "bootstrap/clojure/src/gravity/macro_run_storage.clj"
            "bootstrap/clojure/src/gravity/macro_expansion"
            "bootstrap/clojure/src/gravity/c4_macro_evidence.clj"
            "bootstrap/clojure/test/gravity/macro"
            "bootstrap/clojure/test/gravity/macro_"
            "bootstrap/clojure/test/gravity/c4_"
            "bootstrap/clojure/test/gravity/bootstrap_compatibility/c4_test.clj"
            "bootstrap/clojure/test/gravity/self_hosting/sh05_"
            "bootstrap/clojure/test/gravity/self_hosting/macro_run_storage_test.clj"
            "bootstrap/clojure/fixtures/self-hosting/sh-05"]
   "resolution" ["bootstrap/gravity/src/gravity/resolution.gravity"
                 "bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity"
                 "bootstrap/clojure/src/gravity/resolution.clj"
                 "bootstrap/clojure/src/gravity/c5_name_resolution"
                 "bootstrap/clojure/src/gravity/c5_name_resolution.clj"
                 "bootstrap/clojure/test/gravity/resolution"
                 "bootstrap/clojure/test/gravity/resolution_"
                 "bootstrap/clojure/test/gravity/c5_"
                 "bootstrap/clojure/test/gravity/bootstrap_compatibility/c5_test.clj"
                 "bootstrap/clojure/test/gravity/self_hosting/sh06_"
                 "bootstrap/clojure/test/gravity/self_hosting/resolution_"
                 "bootstrap/clojure/fixtures/self-hosting/sh-06"]
   "checked-core-runtime" ["bootstrap/gravity/src/gravity/checked_core.gravity"
                           "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity"
                           "bootstrap/gravity/src/gravity/compiler/c6_call_arity_legality.gravity"
                           "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity"
                           "bootstrap/clojure/src/gravity/checked_core.clj"
                           "bootstrap/clojure/src/gravity/checked_core"
                           "bootstrap/clojure/src/gravity/c6_core_lowering"
                           "bootstrap/clojure/src/gravity/c6_core_lowering.clj"
                           "bootstrap/clojure/test/gravity/checked_core"
                           "bootstrap/clojure/test/gravity/c6_"
                           "bootstrap/clojure/test/gravity/self_hosting/sh07_var_reference_test.clj"
                           "bootstrap/clojure/test/gravity/self_hosting/sh07_mutation_test.clj"
                           "bootstrap/clojure/fixtures/self-hosting/sh-07"]})

(defn command [heap & args]
  (into ["clojure" (str "-J-Xmx" heap) "-Srepro" "-Sdeps" runtime-deps] args))

(defn descendants [^Process process]
  (with-open [stream (.descendants (.toHandle process))]
    (vec (.toArray stream))))

(defn stop! [^Process process children]
  (let [errors (atom [])]
    (try
      (doseq [^ProcessHandle child (reverse children)]
        (try
          (when (.isAlive child)
            (.destroyForcibly child)
            (.get (.onExit child) 5 TimeUnit/SECONDS))
          (catch Exception error (swap! errors conj (str error)))))
      (finally
        (when (.isAlive process) (.destroyForcibly process))
        (.waitFor process 5 TimeUnit/SECONDS)))
    (when (seq @errors)
      (throw (ex-info "Owned process cleanup failed" {:errors @errors})))))

(defn await! [^Process process seconds]
  (let [deadline (+ (System/nanoTime) (* seconds 1000000000))
        children (atom #{})]
    (try
      (loop []
        (swap! children into (descendants process))
        (cond
          (.waitFor process 100 TimeUnit/MILLISECONDS)
          {:status (if (zero? (.exitValue process)) :passed :failed)
           :exit-code (.exitValue process) :pid (.pid process)
           :descendant-pids (mapv #(.pid ^ProcessHandle %) @children)}
          (>= (System/nanoTime) deadline)
          {:status :timeout :exit-code 124 :pid (.pid process)
           :descendant-pids (mapv #(.pid ^ProcessHandle %) @children)}
          :else (recur)))
      (finally (stop! process @children)))))

(defn execute!
  ([id seconds argv] (execute! id seconds argv nil))
  ([id seconds argv stdin-path]
   (.mkdirs (io/file output))
   (let [started (System/currentTimeMillis)
         log (io/file output (str id ".log"))
         receipt (io/file output (str id ".edn"))
         initial {:id id :argv argv :timeout-seconds seconds
                  :started-ms started :status :running
                  :commit (System/getenv "GITHUB_SHA")
                  :run-id (System/getenv "GITHUB_RUN_ID")
                  :run-attempt (System/getenv "GITHUB_RUN_ATTEMPT")
                  :stdin-path stdin-path}]
     (spit receipt (pr-str initial))
     (println "CI command" id argv)
     (let [result
           (try
             (let [builder (doto (ProcessBuilder. ^java.util.List argv)
                             (.redirectErrorStream true)
                             (.redirectOutput log))
                   _ (when stdin-path (.redirectInput builder (io/file stdin-path)))
                   process (.start builder)]
               (await! process seconds))
             (catch Exception error
               (spit log (str error) :append true)
               {:status :launch-error :exit-code 125}))
           result (merge initial result {:finished-ms (System/currentTimeMillis)})]
       (spit receipt (str (pr-str result) "\n"))
       (print (slurp log))
       (flush)
       result))))

(defn script [id path & args]
  [id 90 (apply command "512m" "-M" path args)])

(defn base-ref [base]
  (if (and base (re-matches #"[0-9a-f]{40}" base)
           (not= base (apply str (repeat 40 "0")))) base "HEAD^"))

(defn comparison-base [event before pr-head parents]
  (if (= event "pull_request")
    (do
      (when-not (and (= 2 (count parents))
                     (re-matches #"[0-9a-f]{40}" (or pr-head ""))
                     (= pr-head (second parents)))
        (throw (ex-info "PR checkout must be the tested merge of the event head"
                        {:pr-head pr-head :parents parents})))
      (first parents))
    (base-ref before)))

(defn checkout-base []
  (let [result (execute! "checkout-parents" 30
                         ["git" "show" "-s" "--format=%P" "HEAD"])]
    (when-not (= :passed (:status result))
      (throw (ex-info "Cannot verify checkout parents" result)))
    (comparison-base (System/getenv "GITHUB_EVENT_NAME")
                     (System/getenv "CI_BASE_SHA")
                     (System/getenv "CI_PR_HEAD_SHA")
                     (str/split (str/trim (slurp (io/file output "checkout-parents.log"))) #"\s+"))))

(defn parse-raw-diff [raw]
  (if (str/blank? raw)
    []
    (let [tokens (str/split raw #"\u0000")]
      (when (odd? (count tokens))
        (throw (ex-info "Malformed raw Git diff output" {:token-count (count tokens)})))
      (mapv (fn [[header path]]
              (let [[_ old-mode new-mode _old-object _new-object status]
                    (or (re-matches #":(\d{6}) (\d{6}) ([0-9a-f]+) ([0-9a-f]+) ([A-Z])" header)
                        (throw (ex-info "Malformed raw Git diff header" {:header header :path path})))]
                {:old-mode old-mode
                 :new-mode new-mode
                 :status status
                 :path path}))
            (partition 2 tokens)))))

(defn changed-file-records [base]
  (let [result (execute! "changed-path-discovery" 30
                         ["git" "diff" "--raw" "--no-renames" "-z" base "HEAD"])]
    (when-not (= :passed (:status result))
      (throw (ex-info "Cannot determine changed files; selection failed closed" result)))
    (parse-raw-diff (slurp (io/file output "changed-path-discovery.log")))))

(defn changed-paths [base]
  (mapv :path (changed-file-records base)))

(defn changed-clojure-paths [base]
  (->> (changed-file-records base)
       (filter #(and (contains? #{"A" "M"} (:status %))
                     (or (str/ends-with? (:path %) ".clj")
                         (str/ends-with? (:path %) ".edn"))))
       (mapv :path)))

(defn docs-only-path? [path]
  (and (string? path) (str/ends-with? path ".md")))

(defn cross-cutting-path? [path]
  (and (string? path)
       (or (str/starts-with? path ".github/")
           (str/starts-with? path "tools/")
           (str/starts-with? path "bin/")
           (contains? #{"deps.edn" "deps-lock.edn" "project.clj" "build.boot"
                        "bb.edn" "shadow-cljs.edn"} path))))

(defn docs-only-record? [{:keys [old-mode new-mode status path]}]
  (and (docs-only-path? path)
       (not (or (str/starts-with? path "contracts/")
                (str/starts-with? path "specs/")))
       (not (cross-cutting-path? path))
       (= "100644" new-mode)
       (case status
         "A" (= "000000" old-mode)
         "M" (= "100644" old-mode)
         false)))

(defn prefix-path? [path prefix]
  (and (string? path)
       (or (= path prefix)
           (and (str/ends-with? prefix "/")
                (str/starts-with? path prefix))
           (and (str/ends-with? prefix "_")
                (str/starts-with? path prefix))
           (and (not (re-find #"\.(?:clj|cljc|cljs|edn|gravity)$" prefix))
                (or (str/starts-with? path (str prefix "/"))
                    (str/starts-with? path (str prefix ".")))))))

(defn stage-owner [path]
  (some (fn [stage]
          (when (some #(prefix-path? path %) (get stage-path-prefixes stage)) stage))
        stage-order))

(defn stage-closure [stage]
  (let [index (.indexOf ^java.util.List stage-order stage)]
    (if (neg? index)
      []
      (subvec (vec stage-order) index))))

(defn regular-added-or-modified? [{:keys [old-mode new-mode status]}]
  (and (contains? #{"A" "M"} status)
       (= "100644" new-mode)
       (or (and (= "M" status) (= "100644" old-mode))
           (and (= "A" status) (= "000000" old-mode)))))

(defn contract-or-spec-path? [path]
  (and (string? path)
       (or (str/starts-with? path "contracts/")
           (str/starts-with? path "specs/"))))

(defn selected-modes [stages]
  (into always-pr-modes (mapcat stage-closure stages)))

(defn stage-selection [changes]
  (let [changes (vec changes)
        paths (mapv :path changes)
        stages (keep stage-owner paths)
        unknown-path? (some (fn [path]
                              (and (not (docs-only-path? path))
                                   (nil? (stage-owner path))))
                            paths)]
    (cond
      (empty? changes)
      {:kind :full :reason :no-changes :paths paths}

      (some #(not (regular-added-or-modified? %)) changes)
      {:kind :full :reason :non-regular-change :paths paths}

      (some contract-or-spec-path? paths)
      {:kind :full :reason :contract-or-spec-change :paths paths}

      (some cross-cutting-path? paths)
      {:kind :full :reason :cross-cutting-change :paths paths}

      unknown-path?
      {:kind :full :reason :unknown-or-cross-stage-path :paths paths}

      :else
      {:kind :selected
       :reason :affected-stage
       :paths paths
       :stages (vec (distinct stages))
       :modes (selected-modes stages)})))

(defn docs-only-change? [event base changes]
  (and (= "pull_request" event)
       (re-matches #"[0-9a-f]{40}" (or base ""))
       (seq changes)
       (every? docs-only-record? changes)))

(defn pull-request-selection []
  (if (not= "pull_request" (System/getenv "GITHUB_EVENT_NAME"))
    {:kind :full :reason :non-pull-request}
    (let [base (checkout-base)]
      (try
        (let [changes (changed-file-records base)
              paths (mapv :path changes)
              clojure-paths (->> changes
                                 (filter #(and (contains? #{"A" "M"} (:status %))
                                               (or (str/ends-with? (:path %) ".clj")
                                                   (str/ends-with? (:path %) ".edn"))))
                                 (mapv :path))
              docs-only? (docs-only-change? "pull_request" base changes)]
          (merge (if docs-only?
                   {:kind :docs-only
                    :reason :known-documentation-only
                    :modes always-pr-modes}
                   (stage-selection changes))
                 {:base base
                  :changes changes
                  :paths paths
                  :clojure-paths clojure-paths}))
        (catch Exception error
          ;; A missing or malformed diff must not silently narrow the matrix.
          ;; Keep the verified base and run every mode; changed-file lint is
          ;; intentionally omitted because its input was not trustworthy.
          {:kind :full
           :reason :path-discovery-failed
           :base base
           :changes []
           :paths []
           :clojure-paths []
           :selection-error (str error)})))))

(defn skip-for-docs-only? [mode selection]
  (and (contains? semantic-modes mode)
       (= :docs-only (:kind selection))
       (not (contains? #{"static" "contracts"} mode))))

(defn skip-for-stage-selection? [mode selection]
  (and (= :selected (:kind selection))
       (contains? semantic-modes mode)
       (not (contains? (:modes selection) mode))))

(defn selection-error-result [mode selection]
  (when (and (= "static" mode) (:selection-error selection))
    {:id "static-selection-discovery"
     :status :failed
     :reason :path-discovery-failed
     :selection-error (:selection-error selection)}))

(defn contract-commands []
  [(script "docs" "tools/validate_gravity_docs.clj")
   (script "roadmap" "tools/validate_full_language_roadmap.clj")
   (script "roadmap-negative-controls" "tools/validate_full_language_roadmap.clj" "--self-test")
   (script "dependency-controls" "tools/acceptance_dependencies_test.clj")])

(defn platform-leaf-catalog [platform catalog]
  (when-not (#{"portable" "darwin"} platform)
    (throw (ex-info "Unknown leaf platform" {:platform platform})))
  (filterv #(= (= platform "darwin") (= "darwin-publication" (:id %))) catalog))

(defn leaf-namespace-path [namespace-symbol]
  (let [relative (str (-> (str namespace-symbol)
                          (str/replace "." "/")
                          (str/replace "-" "_")) ".clj")
        candidates [(str "bootstrap/clojure/src/" relative)
                    (str "bootstrap/clojure/test/" relative)]]
    (or (some #(when (.isFile (io/file %)) %) candidates)
        (throw (ex-info "Leaf namespace has no project source file"
                        {:namespace namespace-symbol
                         :candidates candidates})))))

(def leaf-dynamic-dependency-symbols
  '#{require clojure.core/require use clojure.core/use
     load clojure.core/load
     load-file clojure.core/load-file load-string clojure.core/load-string
     requiring-resolve clojure.core/requiring-resolve
     resolve clojure.core/resolve})

(defn leaf-dynamic-dependency-form? [form]
  (or (and (symbol? form) (contains? leaf-dynamic-dependency-symbols form))
      (and (coll? form) (some leaf-dynamic-dependency-form? form))))

(defn leaf-source-forms [path]
  (binding [*read-eval* false]
    (let [eof (Object.)]
      (with-open [reader (java.io.PushbackReader. (io/reader path))]
        (loop [forms []]
          (let [form (read {:eof eof} reader)]
            (if (identical? eof form)
              forms
              (recur (conj forms form)))))))))

(defn leaf-valid-reference-target? [value]
  (or (= :all value)
      (and (vector? value) (every? symbol? value))))

(defn leaf-valid-rename-map? [value]
  (and (map? value)
       (every? (fn [[from to]] (and (symbol? from) (symbol? to))) value)))

(defn leaf-validate-require-options! [spec clause]
  (loop [options (seq (rest spec))]
    (when options
      (let [option (first options)
            value (second options)]
        (when-not (and (keyword? option) (next options))
          (throw (ex-info "Unsupported leaf require option syntax"
                          {:spec spec :clause clause})))
        (when-not (case option
                    :as (symbol? value)
                    :as-alias (symbol? value)
                    :refer (leaf-valid-reference-target? value)
                    :refer-macros (leaf-valid-reference-target? value)
                    :rename (leaf-valid-rename-map? value)
                    :include-macros (boolean? value)
                    false)
          (throw (ex-info "Unsupported leaf require option"
                          {:option option :value value :spec spec :clause clause})))
        (recur (nnext options))))))

(defn leaf-required-namespaces [form]
  (when-not (and (seq? form) (= 'ns (first form)))
    (throw (ex-info "Leaf namespace source must begin with ns" {:form form})))
  (mapcat (fn [clause]
            (if-not (seq? clause)
              []
              (case (first clause)
                :require
                (mapcat (fn [spec]
                          (let [dependency (cond
                                             (symbol? spec) spec
                                             (vector? spec)
                                             (do
                                               (leaf-validate-require-options! spec clause)
                                               (first spec))
                                             :else nil)]
                            (when-not (symbol? dependency)
                              (throw (ex-info "Unsupported leaf require specification"
                                              {:spec spec :clause clause})))
                            (when (and (= 'clojure.core dependency)
                                       (vector? spec)
                                       (seq (rest spec)))
                              (throw (ex-info "Unsupported clojure.core loader alias or rename"
                                              {:spec spec :clause clause})))
                            (if (str/starts-with? (str dependency) "gravity.")
                              [dependency]
                              [])))
                        (rest clause))

                :use
                (throw (ex-info "Leaf namespace uses unsupported :use dependencies"
                                {:clause clause}))

                :import []
                :refer-clojure
                (do
                  (when (some #{:rename} (rest clause))
                    (throw (ex-info "Unsupported clojure.core loader alias or rename"
                                    {:clause clause})))
                  [])
                :gen-class []

                (throw (ex-info "Unsupported leaf namespace clause"
                                {:clause clause})))))
          (drop 2 form)))

(defn leaf-require-closure [entry]
  (loop [pending [(:namespace entry)]
         paths (set (keep identity [(:source-path entry) (:test-path entry)]))
         visited #{}]
    (if-let [namespace-symbol (peek pending)]
      (let [path (leaf-namespace-path namespace-symbol)]
        (if (contains? visited path)
          (recur (pop pending) paths visited)
          (let [forms (leaf-source-forms path)
                form (first forms)]
            (when-not (and (seq? form) (= 'ns (first form)))
              (throw (ex-info "Leaf namespace source must begin with ns"
                              {:namespace namespace-symbol :path path})))
            (when (some leaf-dynamic-dependency-form? (rest forms))
              (throw (ex-info "Leaf namespace has dynamic dependency loading"
                              {:namespace namespace-symbol :path path})))
            (recur (into (pop pending) (leaf-required-namespaces form))
                   (conj paths path)
                   (conj visited path)))))
      paths)))

(defn leaf-selection-for-changes [platform catalog changes]
  (let [changes (vec changes)
        paths (mapv :path changes)
        platform-catalog (platform-leaf-catalog platform catalog)]
    (cond
      (empty? changes)
      {:kind :full :reason :no-changes :paths paths :entries platform-catalog}

      (some #(not (regular-added-or-modified? %)) changes)
      {:kind :full :reason :non-regular-change :paths paths :entries platform-catalog}

      (some contract-or-spec-path? paths)
      {:kind :full :reason :contract-or-spec-change :paths paths :entries platform-catalog}

      (some cross-cutting-path? paths)
      {:kind :full :reason :cross-cutting-change :paths paths :entries platform-catalog}

      (every? docs-only-record? changes)
      {:kind :docs-only :reason :known-documentation-only :paths paths :entries []}

      :else
      (try
        (let [closures (mapv (fn [entry]
                               {:entry entry :paths (leaf-require-closure entry)}) catalog)
              closure-paths (into #{} (mapcat :paths closures))
              unknown-path? (some (fn [{:keys [path]}]
                                    (and (not (docs-only-path? path))
                                         (not (contains? closure-paths path))))
                                  changes)]
          (if unknown-path?
            {:kind :full
             :reason :unknown-or-unowned-leaf-path
             :paths paths
             :entries platform-catalog}
            (let [matching-ids (->> closures
                                    (filter #(some (set paths) (:paths %)))
                                    (map (comp :id :entry))
                                    set)]
              {:kind :selected
               :reason :affected-leaf
               :paths paths
               :entries (filterv #(contains? matching-ids (:id %)) platform-catalog)})))
        (catch Exception error
          {:kind :full
           :reason :leaf-closure-failed
           :paths paths
           :selection-error (str error)
           :entries platform-catalog})))))

(defn leaf-selection
  ([platform catalog]
   (platform-leaf-catalog platform catalog))
  ([platform catalog changes]
   (:entries (leaf-selection-for-changes platform catalog changes))))

(defn load-leaf-catalog []
  (binding [*command-line-args* nil]
    (load-file "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj"))
  (var-get (requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/catalog)))

(defn leaf-command
  ([platform] (leaf-command platform nil))
  ([platform selection]
   (let [catalog (load-leaf-catalog)
         result (or (:leaf-selection selection)
                    (if (contains? selection :changes)
                      (leaf-selection-for-changes platform catalog (:changes selection))
                      {:kind :full :reason :non-pull-request
                       :entries (platform-leaf-catalog platform catalog)}))
         selected (:entries result)]
     (when (empty? selected)
       (throw (ex-info "Empty platform leaf selection" {:platform platform :result result})))
     (apply command "512m" "-M:leaf-test"
            (mapcat (fn [entry] ["--namespace" (str (:namespace entry))]) selected)))))

(declare commands-for-selection)

(defn commands [mode]
  (commands-for-selection mode nil))

(defn commands-for-selection [mode selection]
  (case mode
    "static" (let [base (or (:base selection) (checkout-base))
                   paths (if (contains? selection :clojure-paths)
                           (:clojure-paths selection)
                           (if (contains? selection :paths)
                             (filterv #(or (str/ends-with? % ".clj") (str/ends-with? % ".edn"))
                                      (:paths selection))
                             (changed-clojure-paths base)))]
               (concat [["whitespace" 30 ["git" "diff" "--check" base "HEAD"]]
                        ["workflow-lint" 30 ["actionlint" "-color"]]
                        ["clojure-lint" 60 ["clj-kondo" "--lint" "tools/ci"]]
                        ["clojure-format" 60 ["cljfmt" "check" "tools/ci"]]
                        ["harness-controls" 60 (command "256m" "-M" "tools/ci/run_test.clj")]
                        ["macro-context-controls" 60 (command "256m" "-M" "tools/ci/lint_macro_test.clj")]
                        ["facade-context-controls" 60 (command "256m" "-M" "tools/ci/lint_facade_test.clj")]]
                       (when (some #{"tools/ci/snapshot.clj" "tools/ci/snapshot_test.clj"} paths)
                         [["snapshot-controls" 120
                           (command "256m" "-M" "tools/ci/snapshot_test.clj")]])
                       (when (seq paths)
                         [["changed-clojure-lint" 120
                           (apply command "256m" "-M" "tools/ci/lint.clj" base paths)]])))
    "contracts" (contract-commands)
    "bootstrap" [["cold-bootstrap-load" 120
                  (command "1g" "-M" "-e"
                           "(assert (nil? (find-ns 'gravity.bootstrap))) (require 'gravity.bootstrap) (println :cold-bootstrap-loaded)")]
                 ["reader-regression" 120
                  (command "1g" "-M:dev-test"
                           "--namespace" "gravity.reader.artifact-test"
                           "--namespace" "gravity.reader-primitives-test"
                           "--namespace" "gravity.c3-reader-expansions-test"
                           "--namespace" "gravity.cli.launcher-test")]
                 ["syntax-regression" 600
                  (command "2g" "-M:dev-test"
                           "--namespace" "gravity.syntax.pipeline-test"
                           "--namespace" "gravity.bootstrap-compatibility.c3-test"
                           "--namespace" "gravity.self-hosting.sh04-fresh-c2-construction-test"
                           "--namespace" "gravity.self-hosting.sh04-form-index-test"
                           "--namespace" "gravity.self-hosting.sh04-literal-index-test")]
                 ["language-boundary" 90
                  (command "512m" "-M:test" "--namespace"
                           "gravity.self-hosting.sh01-language-boundary-test")]
                 ["catalog-controls" 120
                  (command "512m" "-M:test" "--namespace"
                           "gravity.self-hosting.sh01-stage0-leaf-test-runner-test")]
                 ["ownership-inventory" 90
                  (command "512m" "-M:test" "--namespace"
                           "gravity.self-hosting.sh01-ownership-test")]]
    "macro" [["macro-regression" 900
              (command "2g" "-M:dev-test"
                       "--namespace" "gravity.macro.pipeline-test"
                       "--namespace" "gravity.bootstrap-compatibility.c4-test")]]
    "resolution" [["resolution-regression" 900
                   (command "2g" "-M:dev-test"
                            "--namespace" "gravity.resolution.pipeline-test"
                            "--namespace" "gravity.self-hosting.sh06-unmetered-resolution-test"
                            "--namespace" "gravity.self-hosting.sh06-invocation-selection-test")]]
    "checked-core-runtime" [["checked-core-runtime-regression" 180
                             (command "2g" "-M:dev-test"
                                      "--namespace" "gravity.checked-core.pipeline-test")]
                            ["checked-core-current-binding-regression" 180
                             (command "2g" "-M:dev-test"
                                      "--namespace" "gravity.self-hosting.sh07-var-reference-test"
                                      "--prefix" "sh07-current-binding"
                                      "--exact" "sh07-b5-qualified-var-development-kernel")]
                            ["checked-core-projection-kernel-regression" 180
                             (command "2g" "-M:dev-test"
                                      "--namespace" "gravity.self-hosting.sh07-mutation-test"
                                      "--exact" "sh07-character-source-occurrence-development-kernel"
                                      "--exact" "sh07-map-source-order-development-kernel"
                                      "--exact" "sh07-map-quoted-duplicate-source-development-kernel")]]
    "leaves-portable" [["fresh-portable-leaf-catalog" 900 (leaf-command "portable" selection)]]
    "darwin-probe" [["darwin-host" 15 ["sw_vers"]]
                    ["darwin-kernel" 15 ["uname" "-a"]]
                    ["darwin-build" 15 ["sysctl" "kern.version" "kern.osversion"]]
                    ["darwin-sdk" 15 ["xcrun" "--show-sdk-version"]]
                    ["darwin-abi" 30 (command "256m" "-J--enable-native-access=ALL-UNNAMED"
                                              "-M" "tools/ci/darwin_probe.clj")]]
    "darwin-unsupported" [["darwin-host" 15 ["sw_vers"]]
                          ["darwin-kernel" 15 ["uname" "-a"]]
                          ["darwin-build" 15 ["sysctl" "kern.version" "kern.osversion"]]
                          ["darwin-sdk" 15 ["xcrun" "--show-sdk-version"]]
                          ["darwin-unsupported-refusal" 30
                           (command "256m" "-J--enable-native-access=ALL-UNNAMED"
                                    "-M" "tools/ci/darwin_unsupported_test.clj")]]
    "leaves-darwin" [["fresh-darwin-leaf-catalog" 180 (leaf-command "darwin" selection)]]
    (throw (ex-info "Unknown CI mode" {:mode mode}))))

(defn docs-only-static-commands [selection]
  [["whitespace" 30 ["git" "diff" "--check" (:base selection) "HEAD"]]])

(defn leaf-mode? [mode]
  (contains? leaf-modes mode))

(defn leaf-platform-for-mode [mode]
  (case mode
    "leaves-portable" "portable"
    "leaves-darwin" "darwin"
    nil))

(defn leaf-selection-for-mode [mode selection]
  (when-let [platform (leaf-platform-for-mode mode)]
    (let [catalog (load-leaf-catalog)]
      (if (contains? selection :changes)
        (leaf-selection-for-changes platform catalog (:changes selection))
        {:kind :full
         :reason :non-pull-request
         :entries (platform-leaf-catalog platform catalog)}))))

(defn run! [mode]
  (let [selection (if (or (contains? semantic-modes mode) (leaf-mode? mode))
                    (pull-request-selection)
                    {:kind :full :reason :non-semantic-mode})
        skipped? (skip-for-docs-only? mode selection)
        stage-skipped? (skip-for-stage-selection? mode selection)
        docs-only? (= :docs-only (:kind selection))
        leaf-selection (leaf-selection-for-mode mode selection)
        leaf-skipped? (and leaf-selection
                           (or docs-only?
                               (and (= :selected (:kind leaf-selection))
                                    (empty? (:entries leaf-selection)))))
        selection-error (selection-error-result mode selection)
        command-selection (cond-> selection
                            leaf-selection (assoc :leaf-selection leaf-selection))
        results (cond-> (cond
                          (or skipped? stage-skipped? leaf-skipped?)
                          [{:id (str mode (cond
                                            skipped? "-docs-only-skip"
                                            stage-skipped? "-unaffected-skip"
                                            :else "-unaffected-leaf-skip"))
                            :status :skipped
                            :reason (cond
                                      skipped? :docs-only-pr
                                      stage-skipped? :unaffected-stage
                                      :else :unaffected-leaf)
                            :selection (:kind selection)}]
                          (and docs-only? (= "static" mode))
                          (mapv #(apply execute! %) (docs-only-static-commands selection))
                          :else (mapv #(apply execute! %)
                                      (commands-for-selection mode command-selection)))
                  selection-error (conj selection-error))
        summary (if (= :full (:kind selection))
                  results
                  {:mode mode :selection selection :results results})]
    (spit (io/file output (str mode "-summary.edn")) (str (pr-str summary) "\n"))
    (when skipped?
      (println "CI mode" mode "skipped for known documentation-only PR; required matrix job retained"))
    (when (and docs-only? (= "static" mode))
      (println "CI mode static ran the docs-only whitespace check; remaining semantic checks are retained by the matrix"))
    (if (every? #(contains? #{:passed :skipped} (:status %)) results) 0 1)))

(when (seq *command-line-args*)
  (System/exit (run! (first *command-line-args*))))
