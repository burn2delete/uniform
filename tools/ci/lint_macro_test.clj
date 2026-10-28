(ns ci.lint-macro-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "tools/ci/lint.clj")
  (load-file "tools/ci/run.clj"))
(require '[ci.lint :as lint] '[ci.lint-context :as context] '[ci.lint-macros :as macros] '[ci.run :as ci])

(def source-text (slurp macros/source))
(def observed (macros/contract! (.getBytes source-text "UTF-8")))
(def recipe (str/join "\n" (subvec (vec (str/split-lines source-text))
                                   (dec (:start observed)) (:end observed))))
(def header
  (str "(ns gravity.c2-artifact-identity)\n"
       "(def ^:dynamic *bypass-next-operation-keys* #{})\n"
       "(def ^:dynamic *active-operation-keys* #{})\n"
       "(defn- current-operation [key] (get {} key))\n" recipe "\n"))
(def counter (atom 0))

(defn findings! [program & [config-directory]]
  (let [id (str "macro-fixture-" (swap! counter inc))
        input (io/file ci/output (str id "-source.clj"))]
    (io/make-parents input)
    (spit input program)
    (let [result (ci/execute! id 15
                              ["clj-kondo" "--repro" "--cache" "false"
                               "--config-dir" (or config-directory macros/config-directory) "--config" "{:output {:format :edn}}"
                               "--lint" "-" "--filename" macros/source] (.getPath input))]
      (is (#{0 2 3} (:exit-code result)) (pr-str result))
      (lint/findings! (io/file ci/output (str id ".log")) #{macros/source} identity))))

(deftest precise-wrapper-context-and-body-diagnostics
  (let [positive (str header "(definterposable generated :different-key [value] (inc value))\n(generated 1)\n")
        unused (str header "(definterposable generated :key [value]\n  (let [unused 1] value))\n")]
    (is (empty? (findings! positive)))
    ;; An argument used only by the real override branch is still used.
    (is (empty? (findings! (str header "(definterposable generated :key [value] :constant)\n"))))
    (doseq [[program expected]
            [[(str header "(definterposable generated :key [value] (not-defined value))\n") :unresolved-symbol]
             [unused :unused-binding]
             [(str header "(definterposable generated :key [value] value)\n(generated 1 2)\n") :invalid-arity]
             [(str header "(definterposable generated :key [value] (unknown-alias/call value))\n") :unresolved-namespace]
             [(str/replace positive "(defn- current-operation [key] (get {} key))\n" "") :unresolved-var]
             [(str/replace positive "(def ^:dynamic *active-operation-keys* #{})\n" "") :unresolved-var]]]
      (let [findings (findings! program)
            selected (filter #(= expected (:type %)) findings)
            classified (lint/classify [] findings {})]
        (is (seq selected) (pr-str findings))
        (is (= (count findings) (count (:blocking classified))))
        (doseq [finding selected]
          (is (= macros/source (:filename finding)))
          (is (every? pos-int? ((juxt :row :end-row :col :end-col) finding))))))
    (let [before (findings! unused)
          after (findings! (str "\n" unused))
          classified (lint/classify before after
                                    {macros/source {:before (str/split-lines unused)
                                                    :after (str/split-lines (str "\n" unused))
                                                    :changes [{:old-start 1 :old-count 0 :new-start 1 :new-count 1}]}})]
      (is (pos? (count before)))
      (is (= (count before) (count (:baseline-warnings classified))))
      (is (empty? (:blocking classified))))))

(deftest unknown-invocations-are-errors
  (doseq [call ["(definterposable :bad-name :key [value] value)"
                "(definterposable generated not-a-key [value] value)"
                "(definterposable generated :key [value value] value)"
                "(definterposable generated :key [other/value] value)"
                "(definterposable generated :key [value & more] value)"
                "(definterposable generated :key [{:keys [value]}] value)"
                "(definterposable generated :key)"]]
    (let [findings (findings! (str header call "\n"))]
      (is (some #(and (= :ci/unsupported-interposable (:type %)) (= :error (:level %))) findings)
          (pr-str findings)))))

(deftest source-recipe-and-support-guards
  (is (= macros/recipe-sha256 (:macro-sha256 (macros/contract! (.getBytes header "UTF-8")))))
  (doseq [program [(str/replace header "(ns gravity.c2-artifact-identity)" "(ns different.namespace)")
                   (str header recipe "\n")
                   (str/replace header "(do ~@body)" "(do :changed ~@body)")
                   (str/replace header "(defn- current-operation [key] (get {} key))\n" "")
                   (str/replace header "(def ^:dynamic *active-operation-keys* #{})\n" "")
                   (str/replace header "(def ^:dynamic *bypass-next-operation-keys* #{})\n" "")
                   (str header "(clojure.core/defmacro definterposable [& _] nil)\n")
                   (str header "(def definterposable identity)\n")
                   (str header "(clojure.core/def current-operation identity)\n")
                   (str header "(def ^:dynamic *active-operation-keys* #{})\n")
                   (str header "(def shadow (do (def definterposable identity) nil))\n")
                   (str header "(definterposable definterposable :key [value] value)\n")
                   (str header "(in-ns 'different.namespace)\n")]]
    (is (thrown? Exception (macros/contract! (.getBytes program "UTF-8"))))))

(deftest source-body-is-never-executed
  (let [marker (io/file ci/output "macro-body-must-not-execute")]
    (is (not (.exists marker)))
    (is (empty? (findings! (str header "(definterposable generated :key [value] (spit "
                                (pr-str (.getCanonicalPath marker)) " value))\n"))))
    (is (not (.exists marker)))
    (is (nil? (find-ns 'gravity.c2-artifact-identity)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest project-configuration-remains-active
  (let [root (io/file ci/output "macro-project-config")
        config (io/file root "tools/ci/kondo")]
    (doseq [relative ["config.edn" "hooks/c2.clj"]]
      (let [target (io/file config relative)]
        (io/make-parents target)
        (spit target (slurp (io/file macros/config-directory relative)))))
    (let [project-config (io/file root ".clj-kondo/config.edn")
          retained (io/file root "project-config.edn")]
      (io/make-parents project-config)
      (spit project-config "{:linters {:unresolved-namespace {:level :error}}}")
      ;; upload-artifact omits hidden files. Retain exact fixture bytes visibly.
      (spit retained (slurp project-config))
      (spit (io/file root "project-config-provenance.edn")
            (pr-str {:runtime-path (.getPath project-config) :retained-path (.getPath retained)
                     :sha256 (context/sha256 (java.nio.file.Files/readAllBytes (.toPath project-config)))}))
      (try
        (let [findings (findings! (str header "(definterposable generated :key [value] (missing-alias/call value))\n")
                                  (.getPath config))]
          (is (some #(and (= :unresolved-namespace (:type %)) (= :error (:level %))) findings))
          (is (not-any? #(= :unresolved-symbol (:type %)) findings)))
        (finally
          (java.nio.file.Files/delete (.toPath project-config))
          (java.nio.file.Files/delete (.toPath (.getParentFile project-config)))
          (is (not (.exists project-config)))
          (is (= (:sha256 (lint/read-one! (io/file root "project-config-provenance.edn")))
                 (context/sha256 (java.nio.file.Files/readAllBytes (.toPath retained))))))))))

(deftest rejected-recipes-retain-both-source-observations
  (let [valid (.getBytes header "UTF-8")
        changed (.getBytes (str/replace header "(do ~@body)" "(do :changed ~@body)") "UTF-8")
        read-local context/local-bytes!]
    (doseq [[base-bytes current-bytes] [[valid changed] [changed valid]]]
      (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                                "uniform-macro-rejection-" (make-array java.nio.file.attribute.FileAttribute 0)))
            commands (atom [])
            execute (fn [id _ _]
                      (swap! commands conj id)
                      (case id
                        "lint-base-inventory" (spit (io/file directory (str id ".log")) "")
                        "lint-macro-base-source" (java.nio.file.Files/write
                                                  (.toPath (io/file directory (str id ".log")))
                                                  ^bytes base-bytes (make-array java.nio.file.OpenOption 0))
                        (throw (ex-info "Linter must not execute after rejected recipe" {:id id})))
                      {:status :passed :exit-code 0})]
        (try
          (with-redefs [context/local-bytes! (fn [path] (if (= path macros/source) current-bytes (read-local path)))]
            (is (thrown? Exception (lint/run-comparison! execute directory "fixture-base" []))))
          (is (= ["lint-base-inventory" "lint-macro-base-source"] @commands))
          (is (= (seq current-bytes)
                 (seq (java.nio.file.Files/readAllBytes (.toPath (io/file directory "lint-macro-current-source.clj"))))))
          (is (= (seq base-bytes)
                 (seq (java.nio.file.Files/readAllBytes (.toPath (io/file directory "lint-macro-base-source.log"))))))
          (let [receipt (lint/read-one! (io/file directory "lint-macro-contract.edn"))]
            (is (= :rejected (:status receipt)))
            (is (string? (get-in receipt [:error :message]))))
          (finally
            (doseq [file (reverse (file-seq directory))] (.delete file))))))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
