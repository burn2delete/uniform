(ns runtime-requirement-family
  "Read-only checks for the bounded R1-R12 proposal, never compiler execution."
  (:require [clojure.edn :as edn] [clojure.java.io :as io] [clojure.string :as str]))

(binding [*command-line-args* nil] (load-file "tools/runtime_task_projection.clj"))
(def proposal-path "specs/requirement-reconciliation/runtime-family.edn")
(def scope-key "requirement-reconciliation/phase-08-runtime-architecture")
(defn require! [ok message] (when-not ok (throw (ex-info message {}))))
(defn sha256 [text]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes ^String text "UTF-8")))))
(defn file-sha256 [path]
  (with-open [in (io/input-stream path)]
    (let [d (java.security.MessageDigest/getInstance "SHA-256") b (byte-array 8192)]
      (loop [] (let [n (.read in b)] (when (pos? n) (.update d b 0 n) (recur))))
      (format "%064x" (BigInteger. 1 (.digest d))))))
(defn read-edn [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))] (edn/read r)))
(defn git-source [ref path]
  (let [p (.start (ProcessBuilder. ^java.util.List ["git" "show" (str ref ":" path)]))
        result (slurp (.getInputStream p)) error (slurp (.getErrorStream p))]
    (require! (zero? (.waitFor p)) (str "Missing immutable source: " ref ":" path " " error)) result))
(defn occurrence-api [pin]
  (let [text (git-source (:ref pin) (:path pin))]
    (require! (= (:sha256 pin) (sha256 text)) "Occurrence API hash drift")
    (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
      (loop []
        (let [form (read {:eof nil} r)]
          (if form
            (if (and (seq? form) (= 'defn (first form)) (= 'source-occurrences (second form)))
              ;; Import only this exact pinned pure API, not the CLI or compiler.
              (binding [*ns* (the-ns 'runtime-requirement-family)] (eval form) (ns-resolve *ns* 'source-occurrences))
              (recur))
            (throw (ex-info "Missing source-occurrences API" {}))))))))
(defn guarded-apply [records edits]
  (reduce
    (fn [records {:keys [collection id field-path before-present? before after record-guard-sha256]}]
      (require! (= :requirements collection) "Wrong patch collection")
      (require! (= [:family-task-bindings scope-key] field-path) "Patch exceeds family field")
      (let [r (get records id) parent (get-in r (butlast field-path)) k (last field-path)]
        (require! r "Missing/renamed semantic target")
        (require! (= record-guard-sha256 (sha256 (pr-str r))) "Stale semantic predecessor/context")
        (require! (and (= before-present? (contains? parent k)) (= before (get-in r field-path))) "Stale or duplicate patch")
        (assoc records id (assoc-in r field-path after)))) records edits))
(defn check [p manifest]
  (let [docs (:source-documents p) ids (set (map :id docs))
        frozen-text (git-source (get-in p [:frozen-input :ref]) (get-in p [:frozen-input :path]))
        frozen (edn/read-string frozen-text)
        facets (filterv #(ids (:document-id %)) (:source-facets manifest))
        uids (set (mapcat :unit-ids facets))
        reqs (filterv #(uids (:id %)) (:requirements manifest))
        os (:source-occurrences p) oids (set (map :id os))
        actual-occurrences (occurrence-api (:source-occurrence-api p))
        rows (runtime-task-projection/records (slurp runtime-task-projection/task-path))
        by-task (into {} (map (juxt :id identity) rows))
        by-unit (into {} (map (juxt :id identity) reqs))]
    (require! (= scope-key (:scope-key p)) "Wrong family")
    (require! (= (get-in p [:frozen-input :sha256]) (sha256 frozen-text)) "Frozen input hash drift")
    (require! (= (:source-baseline p) (:source-commit frozen)) "Wrong task baseline")
    (require! (= docs (filterv #(<= 112 (:sequence %) 123) (:original-documents frozen))) "Source inventory path/identity drift")
    (require! (= (set (map #(str "R" %) (range 1 13))) ids) "Lost or foreign original document")
    (doseq [d docs]
      (let [text (slurp (:path d)) original (git-source (:original-source-baseline p) (:path d))]
        (require! (= (:current-sha256 d) (sha256 text) (:original-sha256 d) (sha256 original)) "Original/current source hash drift")
        (require! (= (mapv #(dissoc % :context) (actual-occurrences (:id d) text))
                     (filterv #(= (:id d) (:document-id %)) os)) "Invented or lost occurrence ID/span")))
    (require! (= facets (:source-facets p)) "Lost or changed source facet/negative condition/context")
    (let [by-occurrence (into {} (map (juxt :id identity) os))
          bytes (into {} (map (fn [d] [(:id d) (.getBytes (slurp (:path d)) "UTF-8")]) docs))]
      (doseq [f facets]
        (let [o (by-occurrence (:source-occurrence-id f)) b (bytes (:document-id f))]
          (require! (and o (= (:document-id f) (:document-id o))
                         (<= (:start-byte o) (:start-byte f) (:end-byte f) (:end-byte o))
                         (<= (:start-line o) (:start-line f) (:end-line f) (:end-line o))) "Facet escapes source occurrence")
          (require! (= (:raw-sha256 f) (sha256 (String. ^bytes b (int (:start-byte f)) (int (- (:end-byte f) (:start-byte f))) "UTF-8"))) "Wrong raw facet hash"))))
    (require! (= (filterv #(oids (:occurrence-id %)) (:source-dispositions manifest)) (:source-dispositions p)) "Lost or changed disposition")
    (require! (= (mapv #(dissoc % :source-facet-ids) reqs) (get-in p [:preserved-lineage :requirements])) "Applying semantic lineage drift")
    (require! (= (filterv #(or (uids (:id %)) (uids (:target %))) (:aliases manifest)) (get-in p [:preserved-lineage :aliases])) "Alias context differs")
    (require! (= uids (set (map :proposed-id (:units p))) (set (map :id (:edits p)))) "Lost or invented semantic unit")
    (require! (= (count uids) (count (:units p)) (count (:edits p))) "Duplicate semantic unit/edit")
    (doseq [u (:units p)]
      (let [r (by-unit (:proposed-id u)) fs (filterv #(some #{(:id r)} (:unit-ids %)) facets)
            e (first (filter #(= (:id r) (:id %)) (:edits p))) b (:after e)]
        (require! (= (:obligation r) (:obligation u)) "Changed source predicate")
        (require! (= fs (:acceptance-facets u)) "Lost acceptance/negative facet")
        (require! (= (mapv :id fs) (:source-facet-ids b)) "Binding loses source context")
        (require! (= (mapv :id (filter :unresolved fs)) (:unresolved-facet-ids b)) "Unresolved facet hidden")
        (require! (= :unassessed (:current-acceptance u) (:current-acceptance b)) "Unsupported acceptance")
        (require! (= [(:task-reference b)] (:task-references u)) "Task interface differs")
        (let [ref (:task-reference b) row (by-task (:id ref))]
          (require! (= runtime-task-projection/task-path (:path ref)) "Wrong task path")
          (require! (= "runtime-implementation-tasks" (:marker ref)) "Wrong task marker")
          (require! row "Missing/renamed task ID")
          (require! (= (:sha256 ref) (file-sha256 (:path ref))) "Wrong task source hash")
          (require! (= (:acceptance row) (:acceptance b)) "Task acceptance drift")
          (require! (= (:acceptance-sha256 ref) (sha256 (:acceptance row))) "Task acceptance hash drift")
          (require! (= "Open" (:status row) (:observed-status ref)) "Task status drift"))))
    (let [before-rows (runtime-task-projection/records (git-source (:source-baseline p) runtime-task-projection/task-path))]
      (require! (= (mapv :id before-rows) (mapv :id rows) (mapv :id (:task-amendments p))) "Task population drift")
      (doseq [[a before after] (map vector (:task-amendments p) before-rows rows)]
        (require! (and (= :canonical-tasks (:collection a)) (= [:acceptance] (:field-path a)) (:before-present? a)) "Task amendment field drift")
        (require! (= (:before-source-sha256 a) (sha256 (git-source (:source-baseline p) runtime-task-projection/task-path))) "Task predecessor hash drift")
        (require! (= (:after-source-sha256 a) (file-sha256 runtime-task-projection/task-path)) "Task successor hash drift")
        (require! (= (:before a) (:acceptance before)) "Stale task amendment")
        (require! (= (:after a) (:acceptance after)) "Wrong task amendment")
        (require! (= (dissoc before :acceptance) (dissoc after :acceptance)) "Unrelated task mutation")))
    (guarded-apply by-unit (:edits p))
    {:documents (count docs) :occurrences (count os) :facets (count facets) :units (count uids)
     :task-amendments (count rows) :unresolved-facets (count (filter :unresolved facets))
     :implementation-acceptance :unassessed :adoption :central-owner-only}))
(defn run [args]
  (require! (= 1 (count args)) "Usage: clojure -M tools/runtime_requirement_family.clj APPLYING_MANIFEST")
  (let [p (read-edn proposal-path) path (first args)]
    (require! (= (get-in p [:manifest-input :applying-overlay-sha256]) (file-sha256 path)) "Wrong applying manifest")
    (prn (check p (read-edn path)))))
(when (seq *command-line-args*) (run *command-line-args*))
