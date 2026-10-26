(ns requirement-reconciliation
  "Read-only source conservation and requirement projection. Never executes compiler proofs."
  (:require [clojure.edn :as edn] [clojure.java.io :as io]
            [clojure.string :as str] [gravity.tooling.strict-json :as json]))

(def manifest-path "contracts/requirement-reconciliation.edn")
(defn require! [condition message]
  (when-not condition (throw (ex-info message {:diagnostic "REQUIREMENT-RECONCILIATION"}))))
(defn sha256 [text]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String text "UTF-8")))))
(defn source-occurrences [document-id text]
  (let [file-sha (sha256 text)
        lines (remove empty? (re-seq #"[^\n]*\n|[^\n]+$" text))
        groups (partition-by str/blank? lines)]
    (loop [groups groups offset 0 line 1 headings [] fence nil result []]
      (if-let [group (first groups)]
        (let [raw (apply str group) size (alength (.getBytes raw "UTF-8"))
              contexts
              (loop [ls group n line hs headings fc fence out []]
                (if-let [s (first ls)]
                  (let [heading (re-find #"^(#{1,6}) (.+?)(?:\n)?$" s)
                        hs (if (and heading (nil? fc))
                             (conj (vec (take-while #(< (:level %) (count (second heading))) hs))
                                   {:level (count (second heading)) :text (nth heading 2)}) hs)
                        marker (second (re-find #"^\s*(`{3,}|~{3,})" s))
                        before fc
                        fc (cond (and marker (nil? fc)) marker
                                 (and marker fc (= (first marker) (first fc))
                                      (>= (count marker) (count fc))) nil :else fc)]
                    (recur (next ls) (inc n) hs fc
                           (conj out {:line n :headings hs :fence before
                                      :fence-marker marker
                                      :table-row? (str/starts-with? (str/triml s) "|")})))
                  {:lines out :headings hs :fence fc}))
              identity (str document-id "\u0000" file-sha "\u0000" offset ":" (+ offset size))
              occurrence {:id (str document-id "/source/" (sha256 identity))
                          :document-id document-id :source-sha256 file-sha
                          :start-byte offset :end-byte (+ offset size)
                          :start-line line :end-line (+ line (dec (count group)))
                          :raw-sha256 (sha256 raw) :context (:lines contexts)
                          :structural-kind (if (str/blank? raw) :layout :source-block)}]
          (recur (next groups) (+ offset size) (+ line (count group))
                 (:headings contexts) (:fence contexts) (conj result occurrence)))
        (do (require! (= offset (alength (.getBytes ^String text "UTF-8"))) "Source span hole")
            result)))))

(defn validate-aliases! [requirements aliases]
  (let [ids (set (map :id requirements)) aids (mapv :id aliases)
        routes (into {} (map (juxt :id :target) aliases))]
    (require! (= (count aids) (count (set aids))) "Duplicate alias ID")
    (require! (empty? (filter ids aids)) "Alias duplicates canonical requirement")
    (doseq [id aids]
      (loop [current id seen #{}]
        (require! (not (contains? seen current)) "Alias cycle")
        (if-let [next (get routes current)]
          (recur next (conj seen current))
          (require! (contains? ids current) "Dangling alias"))))))

(defn validate-amendment! [{:keys [previous-ids active-ids added removed authority reason]}]
  (let [before (set previous-ids) after (set active-ids)]
    (require! (and (string? authority) (not (str/blank? authority))
                   (string? reason) (not (str/blank? reason))) "Unapproved scope amendment")
    (require! (= (set added) (set (remove before after))) "Scope additions differ")
    (require! (= (set removed) (set (remove after before))) "Scope removals differ")))

(defn project [manifest sources]
  (let [requirements (:requirements manifest) ids (mapv :id requirements)
        occurrences (mapcat (fn [[id text]] (source-occurrences id text)) sources)
        by-source (into {} (map (juxt :id identity) occurrences))
        dispositions (:source-dispositions manifest)
        req-set (set ids)
        _ (require! (= (count ids) (count req-set)) "Duplicate semantic requirement ID")
        _ (require! (= (count dispositions) (count (set (map :occurrence-id dispositions))))
                    "Duplicate source disposition")
        _ (validate-aliases! requirements (:aliases manifest))
        _ (doseq [a (:scope-amendments manifest)] (validate-amendment! a))
        mapped (into {} (map (juxt :occurrence-id identity) dispositions))]
    (doseq [{:keys [occurrence-id kind requirement-ids rationale decision-id]} dispositions]
      (require! (contains? by-source occurrence-id) "Disposition targets missing source occurrence")
      (require! (#{:requirement :alias :refinement :nonnormative :superseded :scope-decision} kind)
                "Unknown disposition kind")
      (when (#{:requirement :alias :refinement} kind)
        (require! (and (seq requirement-ids) (every? req-set requirement-ids))
                  "Disposition has dangling requirement"))
      (when (#{:alias :refinement :nonnormative} kind)
        (require! (not (str/blank? rationale)) "Missing semantic disposition rationale"))
      (when (#{:superseded :scope-decision} kind)
        (require! (contains? (set (map :id (:scope-decisions manifest))) decision-id)
                  "Missing versioned scope decision")))
    (let [unresolved (vec (for [o occurrences
                               :when (and (= :source-block (:structural-kind o))
                                          (or (nil? (get mapped (:id o)))
                                              (= :scope-decision (:kind (get mapped (:id o))))))]
                           (:id o)))
          unresolved-reqs (filter #(not (and (= :reviewed (:atomicity %))
                                             (= :reviewed (:nonoverlap %)))) requirements)
          pending-decisions (filter #(not= :accepted (:state %)) (:scope-decisions manifest))
          ready? (and (= :reviewed (:scope-state manifest)) (seq ids)
                      (empty? unresolved) (empty? unresolved-reqs) (empty? pending-decisions))]
      {:authority :source-conservation-and-canonical-traceability-only
       :scope-version (:scope-version manifest)
       :source-documents (count sources) :source-occurrences (count occurrences)
       :unresolved-source-occurrences unresolved
       :proposed-requirement-ids ids :denominator-ready? (boolean ready?)
       :denominator (when ready? (count req-set))
       :acceptance-projection :not-yet-implemented
       :overall-rate nil
       :scope-decisions (:scope-decisions manifest)
       :limits [:source-blocks-are-not-atomic-requirements :no-implementation-credit
                :historical-checkboxes-are-not-current-acceptance
                :no-compiler-replay :no-dashboard-write]})))

(defn checked-path [root path]
  (require! (and (string? path) (not (.isAbsolute (io/file path)))) "Source path must be relative")
  (let [file (.getCanonicalFile (io/file root path))]
    (require! (and (.startsWith (.toPath file) (.toPath (.getCanonicalFile (io/file root))))
                   (.isFile file)) "Source path is absent or escapes checkout")
    file))
(defn read-source [file]
  (java.nio.file.Files/readString (.toPath (io/file file)) java.nio.charset.StandardCharsets/UTF_8))
(defn audit [root]
  (let [manifest (edn/read-string (read-source (checked-path root manifest-path)))
        inventory-path (checked-path root "docs/document-inventory.json")
        inventory (json/load-json (.toPath inventory-path))
        original (:original-documents manifest)
        sources (concat original (:extension-sources manifest))]
    (require! (= 240 (count inventory) (count original) (count (set (map :id original))))
              "Original 240-contract population changed")
    (require! (= (mapv #(select-keys % ["id" "path" "sequence"]) inventory)
                 (mapv (fn [d] {"id" (:id d) "path" (:path d) "sequence" (:sequence d)}) original))
              "Original inventory identities/order changed")
    (require! (= (:inventory-sha256 manifest) (sha256 (read-source inventory-path))) "Inventory source drift")
    (let [texts (into (sorted-map)
                      (for [{:keys [id path sha256]} sources
                            :let [text (read-source (checked-path root path))]]
                        (do (require! (= sha256 (requirement-reconciliation/sha256 text))
                                      (str "Source drift: " id)) [id text])))
          result (project manifest texts)]
      (require! (= (count sources) (count texts)) "Duplicate original/extension source ID")
      (assoc result :source-baseline (:source-baseline manifest)
             :manifest-sha256 (sha256 (read-source (checked-path root manifest-path)))
             :source-digests (into (sorted-map) (map (fn [[id text]] [id (sha256 text)]) texts))))))

(defn run [args]
  (require! (or (= ["--check"] (vec args)) (= "--source-map" (first args)))
            "Usage: clojure -M tools/requirement_reconciliation.clj --check | --source-map DOCUMENT_ID")
  (let [report (audit ".")]
    (if (= ["--check"] (vec args))
      (prn (-> report (dissoc :source-digests :unresolved-source-occurrences)
               (assoc :unresolved-source-occurrence-count (count (:unresolved-source-occurrences report)))))
      (let [[_ id & extra] args
            manifest (edn/read-string (read-source manifest-path))
            source (first (filter #(= id (:id %)) (concat (:original-documents manifest) (:extension-sources manifest))))]
        (require! (and source (empty? extra)) "Unknown source ID or extra argument")
        (prn {:document-id id :path (:path source)
              :source-occurrences (source-occurrences id (read-source (checked-path "." (:path source))))})))))

(when (seq *command-line-args*) (run *command-line-args*))
