(ns ci.lint
  "Compare warnings at unchanged source locations; every error remains blocking."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import [java.nio.file Files StandardCopyOption]))

(load-file "tools/ci/lint_context.clj")
(require '[ci.lint-context :as context])
(load-file "tools/ci/lint_macros.clj")
(require '[ci.lint-macros :as macros])

(defn hunks [patch]
  (mapv (fn [[_ old-start old-count new-start new-count]]
          {:old-start (parse-long old-start) :old-count (if old-count (parse-long old-count) 1)
           :new-start (parse-long new-start) :new-count (if new-count (parse-long new-count) 1)})
        (re-seq #"(?m)^@@ -(\d+)(?:,(\d+))? \+(\d+)(?:,(\d+))? @@" patch)))

(defn old-row [row changes]
  (when (pos-int? row)
    (loop [changes changes delta 0]
      (if-let [{:keys [new-start new-count old-count]} (first changes)]
        (cond
          (if (zero? new-count) (<= row new-start) (< row new-start)) (+ row delta)
          (< row (+ new-start new-count)) nil
          :else (recur (next changes) (+ delta (- old-count new-count))))
        (+ row delta)))))

(defn mapped-warning [finding {:keys [changes before after]}]
  (let [start (:row finding) end (:end-row finding)
        col (:col finding) end-col (:end-col finding)]
    (when (and (= :warning (:level finding)) (every? pos-int? [start end col end-col])
               (<= start end (count after))
               (or (< start end) (<= col end-col))
               (<= col (inc (count (nth after (dec start)))))
               (<= end-col (inc (count (nth after (dec end))))))
      (let [rows (range start (inc end))
            old-rows (mapv #(old-row % changes) rows)]
        (when (and (every? pos-int? old-rows)
                   (= old-rows (vec (range (first old-rows) (inc (last old-rows)))))
                   (every? (fn [[new old]]
                             (and (<= new (count after)) (<= old (count before))
                                  (= (nth after (dec new)) (nth before (dec old)))))
                           (map vector rows old-rows)))
          (cond-> (assoc finding :row (first old-rows))
            (:end-row finding) (assoc :end-row (last old-rows))))))))

(def definition-symbols
  '#{def defn defn- defmacro defonce defmulti defmethod defprotocol deftype defrecord})

(defn definition-identity [form]
  (let [head (first form)
        name (second form)]
    (when (and (seq? form)
               (contains? definition-symbols head)
               (symbol? name))
      [head name])))

(defn source-definitions [lines]
  (if-not (seq lines)
    []
    (try
      (binding [*read-eval* false
                *data-readers* {}
                *default-data-reader-fn* (fn [tag _]
                                           (throw (ex-info "Unsupported definition source tag"
                                                           {:tag tag})))]
        (let [reader (clojure.lang.LineNumberingPushbackReader.
                      (java.io.StringReader. (str/join "\n" lines)))
              eof (Object.)]
          (with-open [_ reader]
            (loop [definitions [] previous-end 0]
              (let [reader-line (.getLineNumber reader)
                    form (read {:eof eof} reader)]
                (if (identical? eof form)
                  definitions
                  (let [start (max (if (pos? previous-end) (inc previous-end) 1)
                                   reader-line)
                        end (max start (.getLineNumber reader))
                        identity (definition-identity form)]
                    (recur (if identity
                             (conj definitions
                                   {:identity identity
                                    :form form
                                    :start start
                                    :end end
                                    :source-lines (subvec (vec lines)
                                                          (dec start)
                                                          (min end (count lines)))})
                             definitions)
                           end))))))))
      (catch Exception _ []))))

(defn moved-definition-warning
  [finding {:keys [before after before-definitions after-definitions]}]
  (when (and (= :warning (:level finding))
             (seq before) (seq after)
             (pos-int? (:row finding))
             (pos-int? (:end-row finding))
             (<= (:row finding) (:end-row finding)))
    (let [start (:row finding)
          end (:end-row finding)
          current-definitions (or after-definitions (source-definitions after))
          baseline-definitions (or before-definitions (source-definitions before))
          current-candidates (filter #(and (<= (:start %) start)
                                           (<= end (:end %)))
                                     current-definitions)
          matches (keep (fn [current]
                          (let [identity (:identity current)
                                current-same (filter #(= identity (:identity %))
                                                     current-definitions)
                                baseline-same (filter #(= identity (:identity %))
                                                      baseline-definitions)
                                baseline (first baseline-same)]
                            (when (and (= 1 (count current-same))
                                       (= 1 (count baseline-same))
                                       (= (:form current) (:form baseline))
                                       (= (:source-lines current)
                                          (:source-lines baseline)))
                              [current baseline])))
                        current-candidates)]
      (when (= 1 (count matches))
        (let [[current baseline] (first matches)
              offset (- (:start baseline) (:start current))
              mapped-start (+ start offset)
              mapped-end (+ end offset)]
          (when (and (pos-int? mapped-start)
                     (pos-int? mapped-end)
                     (<= mapped-start mapped-end (count before)))
            (assoc finding :row mapped-start :end-row mapped-end)))))))

(defn classify [baseline current sources]
  (let [sources (into {}
                      (map (fn [[filename source]]
                             [filename (assoc source
                                              :before-definitions
                                              (source-definitions (:before source))
                                              :after-definitions
                                              (source-definitions (:after source)))])
                           sources))]
    (loop [findings current available (frequencies baseline) existing [] blocking []]
      (if-let [finding (first findings)]
        (let [source (get sources (:filename finding))
              mapped (or (mapped-warning finding source)
                         (moved-definition-warning finding source))
              matched? (pos? (get available mapped 0))]
          (recur (next findings) (if matched? (update available mapped dec) available)
                 (cond-> existing matched? (conj finding))
                 (cond-> blocking (and (not matched?) (#{:warning :error} (:level finding))) (conj finding))))
        {:baseline-warnings existing :blocking blocking}))))

(defn read-one! [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (let [eof (Object.)
          result (edn/read {:eof eof} reader)]
      (when (or (identical? eof result)
                (not (identical? eof (edn/read {:eof eof} reader))))
        (throw (ex-info "Expected exactly one EDN observation" {:path (str path)})))
      result)))

(defn findings! [path allowed-filenames rename]
  (let [result (read-one! path)
        receipt (read-one! (io/file (.getParentFile path)
                                    (str/replace (.getName path) #"\.log$" ".edn")))
        counts (frequencies (map :level (:findings result)))
        expected-exit (cond (pos? (get counts :error 0)) 3
                            (pos? (get counts :warning 0)) 2 :else 0)]
    (when-not (and (vector? (:findings result)) (map? (:summary result))
                   (= (get counts :error 0) (get-in result [:summary :error]))
                   (= (get counts :warning 0) (get-in result [:summary :warning]))
                   (= expected-exit (:exit-code receipt))
                   (every? #(and (contains? allowed-filenames (:filename %))
                                 (#{:info :warning :error} (:level %))) (:findings result)))
      (throw (ex-info "Malformed or out-of-scope lint output" {:path (str path)})))
    (mapv #(update % :filename rename) (:findings result))))

(defn run-comparison! [execute! output base paths]
  (let [root (io/file output "lint-base")
        _ (when-not (.mkdirs root)
            (throw (ex-info "Lint baseline directory must be fresh" {:path (str root)})))
        run (fn [id argv allowed-exits & [stdin-path]]
              (let [result (if stdin-path (execute! id 120 argv stdin-path) (execute! id 120 argv))]
                (when-not (and (#{:passed :failed} (:status result))
                               (contains? allowed-exits (:exit-code result)))
                  (throw (ex-info "Lint observation failed closed" result)))
                (io/file output (str id ".log"))))
        inventory (run "lint-base-inventory"
                       (into ["git" "ls-tree" "-rz" "--name-only" base "--"] paths) #{0})
        present (remove str/blank? (str/split (slurp inventory) #"\u0000"))
        copied (into {} (map-indexed
                         (fn [index path]
                           (when-not (some #{path} paths)
                             (throw (ex-info "Unexpected baseline path" {:path path})))
                           (let [source (run (str "lint-base-source-" index)
                                             ["git" "show" (str base ":" path)] #{0})
                                 target (io/file root path)]
                             (when-not (str/starts-with? (.getCanonicalPath target)
                                                         (str (.getCanonicalPath root) java.io.File/separator))
                               (throw (ex-info "Baseline path escapes its directory" {:path path})))
                             (io/make-parents target)
                             (Files/copy (.toPath source) (.toPath target)
                                         (make-array StandardCopyOption 0))
                             [path (.getPath target)])) present))
        lint-argv (fn [files]
                    (into ["clj-kondo" "--repro" "--cache" "false"
                           "--config-dir" macros/config-directory
                           "--config" "{:output {:format :edn}}" "--lint"] files))
        macro-current (context/local-bytes! macros/source)
        _ (Files/write (.toPath (io/file output "lint-macro-current-source.clj"))
                       ^bytes macro-current (make-array java.nio.file.OpenOption 0))
        macro-contract (let [observations (atom {:status :running :base-commit base})]
                         (try
                           (swap! observations assoc :model-inputs
                                  (into (sorted-map)
                                        (for [path ["tools/ci/lint_macros.clj" "tools/ci/kondo/config.edn"
                                                    "tools/ci/kondo/hooks/c2.clj"]]
                                          [path (context/sha256 (context/local-bytes! path))])))
                           (let [macro-base (run "lint-macro-base-source"
                                                 ["git" "show" (str base ":" macros/source)] #{0})]
                             (swap! observations assoc :base (macros/contract! (Files/readAllBytes (.toPath macro-base)))))
                           (swap! observations assoc :current (macros/contract! macro-current) :status :admitted)
                           @observations
                           (catch Exception error
                             (swap! observations assoc :status :rejected :error {:message (.getMessage error) :data (ex-data error)})
                             (throw error))
                           (finally
                             (spit (io/file output "lint-macro-contract.edn") (str (pr-str @observations) "\n")))))
        context-requested? (some context/context-path? paths)
        current-plan (when context-requested? (context/selected-plan! context/local-bytes! paths))
        context-paths (set (:paths current-plan))
        _ (doseq [path paths :when (and context-requested?
                                        (str/ends-with? path ".clj")
                                        (str/starts-with? path (str context/source-root "gravity/bootstrap_impl/"))
                                        (not= context/loader path))]
            (when-not (contains? context-paths path)
              (throw (ex-info "Changed Bootstrap fragment is absent from load manifest" {:path path}))))
        base-plan (when current-plan
                    (let [archive (io/file root "context.zip")
                          inventory (run "lint-context-base-inventory"
                                         ["git" "ls-tree" "-rz" base "--" context/source-root] #{0})]
                      (run "lint-context-base-archive"
                           ["git" "archive" "--format=zip" (str "--output=" archive)
                            base "--" context/source-root] #{0})
                      (context/archived-plan! archive inventory present)))
        contextual (fn [id plan selected]
                     (if-not plan []
                             (let [view (context/assemble! (io/file output id) plan)
                                   log (run id (lint-argv ["-"]) #{0 2 3} (:file view))
                                   all (mapv #(context/remap! view %)
                                             (findings! log #{"<stdin>"} identity))]
                               (doseq [finding all :when (and (= :syntax (:type finding))
                                                              (not (contains? selected (:filename finding))))]
                                 (throw (ex-info "Syntax failure in lint context dependency" finding)))
                               (filterv #(contains? selected (:filename %)) all))))
        normal (fn [id files]
                 (vec (mapcat (fn [[index path]]
                                (let [log (run (str id "-" index) (lint-argv [path]) #{0 2 3})]
                                  (findings! log #{path} #(get (set/map-invert copied) % %))))
                              (map-indexed vector files))))
        whole-context? (some context/whole-context-path? paths)
        baseline (into (normal "lint-baseline" (vals (apply dissoc copied context-paths)))
                       (contextual "lint-context-baseline" base-plan
                                   (if whole-context? (set (:paths base-plan)) (set (keys copied)))))
        current (into (normal "lint-current" (remove context-paths paths))
                      (contextual "lint-context-current" current-plan
                                  (if whole-context? context-paths (set paths))))
        source-paths (vec (distinct (concat paths (when whole-context? (:paths current-plan)))))
        sources (into {}
                      (for [[index path] (map-indexed vector source-paths)
                            :let [before (if-let [bytes (get-in base-plan [:inputs path])]
                                           (String. ^bytes bytes java.nio.charset.StandardCharsets/UTF_8)
                                           (when-let [file (get copied path)] (slurp file)))
                                  after (if-let [bytes (get-in current-plan [:inputs path])]
                                          (String. ^bytes bytes java.nio.charset.StandardCharsets/UTF_8)
                                          (slurp path))]
                            :when before]
                        (let [patch (when-not (= before after)
                                      (run (str "lint-diff-" index)
                                           ["git" "diff" "--no-ext-diff" "--no-textconv" "--no-renames"
                                            "--text" "--unified=0" base "HEAD" "--" path] #{0}))]
                          [path {:changes (if patch (hunks (slurp patch)) [])
                                 :before (str/split-lines before)
                                 :after (str/split-lines after)}])))
        config-diff (run "lint-config-diff" ["git" "diff" "--name-only" base "HEAD" "--"
                                             ".clj-kondo" "tools/ci/kondo" "tools/ci/lint_macros.clj"] #{0})
        config-unchanged? (str/blank? (slurp config-diff))
        usage-groups (fn [plan files]
                       (cond-> (mapv (fn [[path file]]
                                       {:paths [path] :inputs {path (Files/readAllBytes (.toPath (io/file file)))}})
                                     (filter #(str/ends-with? (first %) ".clj") files))
                         plan (conj plan)))
        baseline-usage (context/resolve-private-usages
                        baseline (context/private-var-usages
                                  (usage-groups base-plan (apply dissoc copied context-paths))))
        current-usage (context/resolve-private-usages
                       current (context/private-var-usages
                                (usage-groups current-plan (mapv #(vector % %) (remove context-paths paths)))))
        report (merge {:base base :commit (System/getenv "GITHUB_SHA")
                       :policy :unchanged-source-warnings-only :config-unchanged? config-unchanged?
                       :macro-contract macro-contract
                       :resolved-private-usages {:base (:resolved baseline-usage) :current (:resolved current-usage)}
                       :context-selection (when current-plan
                                            {:whole-context? (boolean whole-context?)
                                             :current (vec (sort (if whole-context? context-paths
                                                                     (filter context-paths paths))))
                                             :base (vec (sort (if whole-context? (:paths base-plan)
                                                                  (filter (set (:paths base-plan)) (keys copied)))))})}
                      (classify (if config-unchanged? (:findings baseline-usage) []) (:findings current-usage) sources))]
    (spit (io/file output "changed-lint-report.edn") (str (pr-str report) "\n"))
    (println "Changed-file lint:" (count (:baseline-warnings report)) "unchanged baseline warnings;"
             (count (:blocking report)) "blocking warnings/errors. All errors block.")
    (if (empty? (:blocking report)) 0 1)))

(when (seq *command-line-args*)
  (let [[base & paths] *command-line-args*]
    (binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
    (System/exit (run-comparison! (requiring-resolve 'ci.run/execute!) "target/ci" base (vec paths)))))
