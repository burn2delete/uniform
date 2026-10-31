(ns compiler-domain-source-test
  (:require [clojure.edn :as edn] [clojure.string :as str]
            [clojure.java.shell :as shell] [clojure.test :refer [deftest is run-tests]]))
(binding [*command-line-args* nil]
  (load-file "tools/sh_inventory.clj") (load-file "tools/sh_crosswalk.clj"))
(require '[sh-inventory] '[sh-crosswalk])
(def composition (edn/read-string (slurp "specs/compiler-api-workflow/domain-contract-source-composition.edn")))
(def compiler-api-task-path "specs/compiler-api-workflow/tasks.md")
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
  (let [raw (if (= :archived-exact-bytes (:reference-kind r)) (slurp (:path r))
              (git-source (:commit r) (:path r)))]
    (assert (= (:sha256 r) (sha raw))) (edn/read-string raw)))
(defn mode-and-blob [path]
  (let [r (shell/sh "git" "ls-tree" "HEAD" "--" path)]
    (assert (zero? (:exit r))) (str/trim (:out r))))
(defn pending-only? [r]
  (and (= :pending-independent-affected-composition-review (:current-main-composition-review r))
       (= :not-conferred (:integration-eligibility r))
       (= :none (:implementation-credit r)) (= :not-adopted (:global-denominator r))))

(deftest exact-frozen-reviews-and-current-predicate-bytes
  (is (= #{:C17 :C12} (set (map :id (:cohorts composition)))))
  (is (= 223 (reduce + (map :source-facets (:cohorts composition)))))
  (doseq [cohort (:cohorts composition)
          :let [receipt (immutable-input (:receipt cohort))
                reviewed-source (slurp (get-in cohort [:reviewed-task-source :path]))]]
    (is (= :scoped-pass (:verdict receipt)))
    (is (= (:source-facets cohort) (count (:facet-verdicts receipt))))
    (is (every? (set (:exclusions receipt))
                [:implementation :integration
                 (case (:id cohort) :C17 :final-count-or-denominator :C12 :final-count-denominator)]))
    (is (= (get-in cohort [:normative-source :file-sha256])
           (sha (slurp (get-in cohort [:normative-source :path])))))
    (doseq [[role {:keys [reviewed-witness current-witness]}] (:criteria cohort)
            :let [current (slurp (:path current-witness))
                  reviewed (span reviewed-witness reviewed-source)]]
      (is (= reviewed-witness (get-in receipt [:criterion-verdicts role :witness])))
      (is (= current (git-source (:source-commit current-witness) (:path current-witness))))
      (is (= reviewed (span current-witness current)))
      (is (= (.indexOf ^String current ^String reviewed) (.lastIndexOf ^String current ^String reviewed))))))

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
  (is (= ["domain-gap-completion.edn"]
         (vec (sort (.list (java.io.File. "specs/requirement-reconciliation"))))))
  (let [result (shell/sh "git" "diff" "--name-only" (:base-commit composition))
        paths (remove str/blank? (str/split-lines (:out result)))]
    (is (zero? (:exit result)))
    (is (every? (set (:owned-paths composition)) paths))
    (is (not-any? #(or (str/starts-with? % "bootstrap/") (str/starts-with? % "contracts/")) paths))))

(deftest named-producer-source-dependency-is-complete-and-scoped
  (let [ref (:selected-producer-source-context composition) text (slurp (:path ref))
        context (edn/read-string text) record (first (:records context))
        source-facets (:source-evidence record)
        tasks (slurp compiler-api-task-path)
        named (set (re-seq #"specs/requirement-reconciliation/[a-z-]+\.edn" tasks))]
    (is (= (:sha256 ref) (sha text)))
    (is (= :finite-producer-criterion-source-context-excerpt (:kind context)))
    (is (= 1 (count (:records context))))
    (is (= "domain/mir-pass-integration" (:historical-id record) (:selected-criterion context)))
    (is (= (:selected-record-sha256 context) (sha (pr-str record))))
    (is (= 10 (count source-facets)))
    (is (= #{(:path ref)} named))
    (is (str/includes? tasks (:criterion-text record)))
    (is (= :none (:implementation-credit context)))
    (is (= :not-conferred (:current-whole-owner-acceptance context)))
    (doseq [f source-facets
            :let [text (slurp (:path f)) bytes (.getBytes ^String text "UTF-8")
                  raw (String. (java.util.Arrays/copyOfRange bytes (int (:start-byte f)) (int (:end-byte f))) "UTF-8")]]
      (is (= (:original-source-sha256 f) (sha text)))
      (is (= (:raw-sha256 f) (sha raw))))
    (is (not= (set (map :id source-facets)) (set (map :id (pop source-facets)))))
    (is (= ["pipeline" "opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts"] (:dependencies record)))
    (is (str/includes? (slurp "specs/opt-workflow/tasks.md")
                      "`opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts`"))))

(deftest git-modes-and-blobs-preserve-extracted-source-and-normative-inputs
  (doseq [[path pin] (:source-file-pins composition)]
    (is (= (:mode-and-blob pin) (mode-and-blob path)))
    (is (= (:sha256 pin) (sha (slurp path)))))
  (doseq [[path expected] (:unchanged-normative-mode-and-blob-pins composition)]
    (is (= expected (mode-and-blob path)))))

(deftest current-SH-composition-reference-authenticates-the-live-artifact
  (let [r (:compiler-domain-current-source sh-crosswalk/mapping-review)
        raw (slurp (:path r))]
    (is (= (:sha256 r) (sha raw)))
    (is (= raw (git-source (:source-commit r) (:path r))))
    (is (= :none (:implementation-credit r)))
    (is (= :pending-independent-affected-composition-review (:status r)))))

(let [result (run-tests 'compiler-domain-source-test)]
  (shutdown-agents)
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
