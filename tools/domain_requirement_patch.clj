(ns domain-requirement-patch
  "Read-only conservation and stale-edit controls for the bounded domain proposal."
  (:require [clojure.edn :as edn] [clojure.string :as str]
            [clojure.java.shell :as shell] [clojure.set :as set]))

(def proposal-path "specs/requirement-reconciliation/domain-family-proposal.edn")
(def doc-ids (set (map #(str "DOM" %) (range 1 22))))
(defn require! [p message]
  (when-not p (throw (ex-info message {:diagnostic "DOMAIN-REQUIREMENT-PATCH"}))))
(defn sha [s]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                        (.getBytes ^String s "UTF-8")))))
(defn git-text [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) "Pinned Git source missing") out))
(defn index! [rows k]
  (let [m (into {} (map (juxt k identity) rows))]
    (require! (= (count rows) (count m)) "Duplicate stable identity") m))
(defn slice [s start end]
  (let [b (.getBytes ^String s "UTF-8")]
    (require! (<= 0 start end (alength b)) "Invalid byte span")
    (String. (java.util.Arrays/copyOfRange b (int start) (int end)) "UTF-8")))
(defn task! [{:keys [path sha256 marker id]}]
  (let [s (slurp path) start (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")
        a (.indexOf s start) b (.indexOf s end)]
    (require! (= sha256 (sha s)) "Wrong applying task hash")
    (require! (and (<= 0 a) (< a b)
                   (= a (.lastIndexOf s start)) (= b (.lastIndexOf s end))) "Wrong task marker")
    (require! (some #(= id (second %))
                   (re-seq #"(?m)^- \[[ x]\] `([^`]+)` " (subs s a b))) "Missing or renamed literal task ID")))
(defn apply-edits [manifest edits]
  (require! (= (count edits) (count (set (map (juxt :collection :id :field-path) edits)))) "Duplicate field operation")
  (reduce
    (fn [m {:keys [collection id field-path before-present? before after]}]
      (require! (and (= :requirements collection)
                    (#{[:task-reference] [:task-gap]} field-path)) "Out-of-scope field edit")
      (let [rows (get m collection) i (.indexOf (mapv :id rows) id)]
        (require! (<= 0 i) "Missing requirement identity")
        (let [r (nth rows i) k (first field-path)]
          (require! (and (= before-present? (contains? r k)) (= before (get r k))) "Stale or repeated field operation")
          (assoc-in m [collection i k] after)))) manifest edits))
(defn validate! [p manifest]
  (require! (= "requirement-reconciliation/phase-09-domain-specific-computing-coverage" (:scope-key p)) "Wrong family")
  (require! (and (= :proposal-only (:authority p)) (= :unassessed (:current-acceptance p))) "Unsupported acceptance")
  (let [input-text (git-text (get-in p [:input-snapshot :ref]) (get-in p [:input-snapshot :path]))
        inputs (edn/read-string input-text)
        _ (require! (= (sha input-text) (get-in p [:input-snapshot :sha256])) "Wrong frozen input snapshot")
        _ (require! (= (:source-documents p) (filterv #(doc-ids (:id %)) (:original-documents inputs))) "Wrong document ID/path/pin")
        _ (require! (= (select-keys p [:source-commit :source-tree :original-source-commit])
                       (select-keys inputs [:source-commit :source-tree :original-source-commit])) "Wrong source tuple")
        docs (index! (:source-documents p) :id)
        facets (index! (:source-facets p) :id)
        occurrences (index! (:source-occurrences p) :id)
        units (index! (:units p) :proposed-id)
        originals (index! (:requirements manifest) :id)
        expected-facets (filterv #(doc-ids (:document-id %)) (:source-facets manifest))
        expected-ids (set (map :id (filter #(some (set (keys facets)) (:source-facet-ids %)) (:requirements manifest))))]
    (require! (= doc-ids (set (keys docs))) "Lost or invented source document")
    (require! (= expected-facets (:source-facets p)) "Lost facet, source condition or delivered lineage")
    (require! (= expected-ids (set (keys units))) "Lost or resurrected semantic identity")
    (require! (= (set (map :id (remove #(= :layout (:structural-kind %)) (vals occurrences))))
                 (set (map :occurrence-id (:source-dispositions p)))) "Nonlayout occurrence/disposition conservation failure")
    (require! (= (:source-dispositions p)
                 (filterv #(doc-ids (first (str/split (:occurrence-id %) #"/"))) (:source-dispositions manifest)))
              "Disposition or prior decision changed")
    (doseq [[id d] docs
            :let [original (git-text (:original-source-commit p) (:path d))
                  current (git-text (:source-commit p) (:path d))
                  os (sort-by :start-byte (filter #(= id (:document-id %)) (vals occurrences)))]]
      (require! (= (:original-sha256 d) (sha original)) "Wrong original source hash")
      (require! (= (:current-sha256 d) (sha current) (sha (slurp (:path d)))) "Current source drift")
      (require! (= (mapv :start-byte os) (into [0] (map :end-byte (butlast os)))) "Lost source bytes")
      (require! (= (:end-byte (last os)) (alength (.getBytes original "UTF-8"))) "Incomplete source coverage")
      (doseq [o os]
        (require! (= (:source-sha256 o) (sha original)) "Wrong occurrence source")
        (require! (= (:raw-sha256 o) (sha (slice original (:start-byte o) (:end-byte o)))) "Wrong raw occurrence hash")
        (require! (= (:id o) (str id "/source/" (sha (str id "\u0000" (sha original) "\u0000" (:start-byte o) ":" (:end-byte o))))) "Invented occurrence ID"))
      (doseq [f (filter #(= id (:document-id %)) (vals facets))]
        (require! (contains? occurrences (:source-occurrence-id f)) "Invented facet occurrence")
        (require! (= (:raw-sha256 f) (sha (slice original (:start-byte f) (:end-byte f)))) "Wrong raw facet hash")))
    (doseq [[id u] units :let [r (originals id)]]
      (require! (= (:original-record-sha256 u) (sha (pr-str r))) "Prior semantic record or receipt changed")
      (require! (= (:obligation u) (:obligation r)) "Lost obligation or negative condition")
      (require! (and (= :unassessed (:current-acceptance u)) (= :unreviewed (:nonoverlap u))
                    (empty? (:equivalence-proposals u)) (seq (:task-gaps u))) "Unreviewed alias/context or acceptance promotion")
      (require! (= (:acceptance-facets u)
                   (mapv #(select-keys % [:id :source-occurrence-id :start-byte :end-byte :start-line :end-line :raw-sha256])
                         (filter #(some #{(:id %)} (:source-facet-ids r)) (:source-facets p)))) "Lost unit facet or raw source condition")
      (require! (= (:historical-support u) (:historical-support r)) "Historical credit changed"))
    (let [mapped (set (map :id (filter #(= [:task-reference] (:field-path %)) (:edits p))))]
      (require! (= (set (:unresolved-stable-ids p)) (set/difference (set (keys units)) mapped)) "Residual stable IDs lost"))
    (doseq [e (:edits p)]
      (require! (contains? units (:id e)) "Edit outside assigned family")
      (when (= [:task-reference] (:field-path e))
        (require! (= [(:after e)] (:task-references (units (:id e)))) "Unit/task operation disagreement")
        (task! (:after e))))
    (apply-edits manifest (:edits p))
    {:documents (count docs) :occurrences (count occurrences) :facets (count facets)
     :units (count units) :field-operations (count (:edits p))
     :implementation-acceptance :unassessed :canonical-adoption :not-performed}))
(defn run [args]
  (require! (= 1 (count args)) "Usage: clojure -M tools/domain_requirement_patch.clj EXACT-COMPOSED-MANIFEST.edn")
  (let [p (edn/read-string (slurp proposal-path)) s (slurp (first args))]
    (require! (= (get-in p [:manifest :sha256]) (sha s)) "Wrong composed input manifest")
    (prn (validate! p (edn/read-string s)))))
(when (and (seq *command-line-args*) (str/ends-with? *file* "domain_requirement_patch.clj"))
  (try (run *command-line-args*) (finally (shutdown-agents))))
