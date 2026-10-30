(ns compiler-domain-source-test
  (:require [clojure.edn :as edn] [clojure.string :as str]
            [clojure.java.shell :as shell] [clojure.test :refer [deftest is run-tests]]))
(binding [*command-line-args* nil] (load-file "tools/compiler_api_task_projection.clj")
  (load-file "tools/sh_inventory.clj"))
(def composition (edn/read-string (slurp "specs/compiler-api-workflow/domain-contract-source-composition.edn")))
(defn sha [text] (sh-inventory/sha256 text))
(def git-source
  (memoize (fn [commit path]
             (let [result (shell/sh "git" "show" (str commit ":" path))]
               (assert (zero? (:exit result))) (:out result)))))
(defn span [w text]
  (assert (= (:file-sha256 w) (sha text)))
  (let [bytes (.getBytes ^String text "UTF-8") start (:start-byte w) end (:end-byte w)]
    (assert (<= 0 start end (alength bytes)))
    (let [raw (String. (java.util.Arrays/copyOfRange bytes (int start) (int end)) "UTF-8")]
      (assert (= (:raw-sha256 w) (sha raw))) raw)))
(defn immutable-input [r]
  (let [raw (git-source (:commit r) (:path r))]
    (assert (= (:sha256 r) (sha raw))) (edn/read-string raw)))
(defn pending-only? [r]
  (and (= :pending-independent-affected-composition-review (:current-main-composition-review r))
       (= :not-conferred (:integration-eligibility r))
       (= :none (:implementation-credit r)) (= :not-adopted (:global-denominator r))))

(deftest exact-frozen-reviews-and-current-predicate-bytes
  (is (= #{:C17 :C12} (set (map :id (:cohorts composition)))))
  (is (= 223 (reduce + (map :source-facets (:cohorts composition)))))
  (doseq [cohort (:cohorts composition)
          :let [receipt (immutable-input (:receipt cohort)) adoption (immutable-input (:qualified-adoption cohort))]]
    (is (= :scoped-pass (:verdict receipt)))
    (is (= (:source-facets cohort) (count (:facet-verdicts receipt))))
    (is (= :not-accepted (:whole-current-head adoption) (:final-denominator adoption)))
    (is (= (get-in cohort [:normative-source :file-sha256])
           (sha (slurp (get-in cohort [:normative-source :path])))))
    (doseq [[role {:keys [reviewed-witness current-witness]}] (:criteria cohort)
            :let [current (slurp (:path current-witness))
                  reviewed (span reviewed-witness (git-source (:source-commit reviewed-witness) (:path reviewed-witness)))]]
      (is (= reviewed-witness (get-in receipt [:criterion-verdicts role :witness])))
      (is (= current (git-source (:source-commit current-witness) (:path current-witness))))
      (is (= reviewed (span current-witness current)))
      (is (= (.indexOf ^String current ^String reviewed) (.lastIndexOf ^String current ^String reviewed))))))

(deftest prior-main-outcomes-and-historical-statuses-survive
  (let [path compiler-api-task-projection/task-path
        current (compiler-api-task-projection/records (slurp path))
        previous (task-projection/records
                   {:component "Compiler API" :task-marker "compiler-api-implementation-tasks"
                    :record-pattern compiler-api-task-projection/record-pattern
                    :semantic-ids (set (:prior-main-outcomes composition))
                    :record-keys [:id :title :status :projection]}
                   (git-source (:base-commit composition) path))]
    (is (= (:current-outcomes composition) (mapv :id current)))
    (is (every? #(= "Open" (:status %)) current))
    (doseq [old previous :let [fresh (first (filter #(= (:id old) (:id %)) current))]]
      (is (= (dissoc old :projection) (dissoc fresh :projection)))
      (is (str/starts-with? (:projection fresh) (:projection old))))
    (is (compiler-api-task-projection/check-projection (slurp path) (slurp compiler-api-task-projection/roadmap-path)))))

(deftest coherent-byte-mutations-and-forged-pins-refuse
  (doseq [cohort (:cohorts composition) [_ {:keys [current-witness]}] (:criteria cohort)
          :let [text (slurp (:path current-witness)) raw (span current-witness text)
                cut (+ 2 (.indexOf ^String raw ". "))]]
    (is (< 2 cut (count raw)))
    (let [altered (subs raw cut) full (str/replace text raw altered)
          fresh (assoc current-witness :file-sha256 (sha full) :raw-sha256 (sha altered)
                      :end-byte (+ (:start-byte current-witness) (count (.getBytes ^String altered "UTF-8"))))]
      (is (= altered (span fresh full)))
      (is (not= raw (span fresh full)))
      (is (thrown? AssertionError (span (assoc current-witness :raw-sha256 (apply str (repeat 64 "0"))) text))))))

(deftest scope-and-affected-paths-do-not-confer-main-acceptance
  (is (pending-only? composition))
  (doseq [bad [(assoc composition :integration-eligibility :accepted)
               (assoc composition :implementation-credit :accepted)
               (assoc composition :global-denominator :accepted)
               (assoc composition :current-main-composition-review :accepted)]]
    (is (not (pending-only? bad))))
  (is (not (.exists (java.io.File. "specs/requirement-reconciliation"))))
  (let [result (shell/sh "git" "diff" "--name-only" (:base-commit composition))
        paths (remove str/blank? (str/split-lines (:out result)))]
    (is (zero? (:exit result)))
    (is (every? (set (:owned-paths composition)) paths))
    (is (not-any? #(or (str/starts-with? % "bootstrap/") (str/starts-with? % "contracts/")) paths))))

(let [result (run-tests 'compiler-domain-source-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
