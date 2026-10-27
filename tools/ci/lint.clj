(ns ci.lint
  "Compare warnings at unchanged source locations; every error remains blocking."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import [java.nio.file Files StandardCopyOption]))

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

(defn classify [baseline current sources]
  (loop [findings current available (frequencies baseline) existing [] blocking []]
    (if-let [finding (first findings)]
      (let [mapped (mapped-warning finding (get sources (:filename finding)))
            matched? (pos? (get available mapped 0))]
        (recur (next findings) (if matched? (update available mapped dec) available)
               (cond-> existing matched? (conj finding))
               (cond-> blocking (and (not matched?) (#{:warning :error} (:level finding))) (conj finding))))
      {:baseline-warnings existing :blocking blocking})))

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
        run (fn [id argv allowed-exits]
              (let [result (execute! id 120 argv)]
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
                           "--config" "{:output {:format :edn}}" "--lint"] files))
        baseline-log (run "lint-baseline" (lint-argv (if (seq copied) (vals copied) [(str root)])) #{0 2 3})
        current-log (run "lint-current" (lint-argv paths) #{0 2 3})
        baseline (findings! baseline-log (set (vals copied)) (set/map-invert copied))
        current (findings! current-log (set paths) identity)
        sources (into {}
                      (for [path paths :when (contains? copied path)]
                        (let [patch (run (str "lint-diff-" (.indexOf paths path))
                                         ["git" "diff" "--no-ext-diff" "--no-textconv" "--no-renames"
                                          "--text" "--unified=0" base "HEAD" "--" path] #{0})]
                          [path {:changes (hunks (slurp patch))
                                 :before (str/split-lines (slurp (get copied path)))
                                 :after (str/split-lines (slurp path))}])))
        config-diff (run "lint-config-diff" ["git" "diff" "--name-only" base "HEAD" "--" ".clj-kondo"] #{0})
        config-unchanged? (str/blank? (slurp config-diff))
        report (merge {:base base :commit (System/getenv "GITHUB_SHA")
                       :policy :unchanged-source-warnings-only :config-unchanged? config-unchanged?}
                      (classify (if config-unchanged? baseline []) current sources))]
    (spit (io/file output "changed-lint-report.edn") (str (pr-str report) "\n"))
    (println "Changed-file lint:" (count (:baseline-warnings report)) "unchanged baseline warnings;"
             (count (:blocking report)) "blocking warnings/errors. All errors block.")
    (if (empty? (:blocking report)) 0 1)))

(when (seq *command-line-args*)
  (let [[base & paths] *command-line-args*]
    (binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
    (System/exit (run-comparison! (requiring-resolve 'ci.run/execute!) "target/ci" base (vec paths)))))
