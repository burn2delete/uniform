(ns requirement-reconciliation
  "Read-only source conservation and requirement projection. Never executes compiler proofs."
  (:require [clojure.edn :as edn] [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str] [gravity.tooling.strict-json :as json]))

(load-file "tools/requirement_evidence.clj")
(load-file "tools/requirement_facets.clj")
(load-file "tools/requirement_sources.clj")
(require 'requirement-evidence 'requirement-facets 'requirement-sources)

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

(defn validate-scope-lineage! [manifest]
  (let [prior (:prior-scope manifest) amendments (:scope-amendments manifest)]
    (when (or prior (seq amendments))
      (require! (and (string? (:version prior)) (seq (:review-reference prior))
                     (vector? (:active-ids prior))) "Missing prior approved scope")
      (let [last-scope
            (reduce (fn [{:keys [version active-ids]} amendment]
                      (validate-amendment! amendment)
                      (require! (and (= version (:from-version amendment))
                                     (= (set active-ids) (set (:previous-ids amendment)))
                                     (string? (:to-version amendment))
                                     (not= version (:to-version amendment)))
                                "Scope amendment is detached from prior version")
                      {:version (:to-version amendment) :active-ids (:active-ids amendment)})
                    prior amendments)]
        (require! (and (= (:scope-version manifest) (:version last-scope))
                       (= (set (map :id (:requirements manifest))) (set (:active-ids last-scope))))
                  "Scope amendment is detached from actual canonical IDs/version")))))

(defn validate-supersession! [manifest decision occurrence-id by-source]
  (let [authority-id (:authority-source-occurrence-id decision)
        lineage (:lineage decision)]
    (require! (and (= :accepted (:state decision))
                   (= (:scope-version manifest) (:scope-version decision))
                   (not (str/blank? (:authority decision)))
                   (not (str/blank? (:reason decision)))
                   (contains? by-source authority-id)
                   (contains? (set (:source-occurrence-ids decision)) occurrence-id)
                   (= occurrence-id (:superseded-source-occurrence-id lineage))
                   (= authority-id (:authority-source-occurrence-id lineage))
                   (= (:scope-version manifest) (:scope-version lineage)))
              "Supersession lacks source-bound authority, rationale or versioned lineage")))

(defn validate-scope-decisions! [manifest by-source]
  (let [decisions (:scope-decisions manifest) ids (mapv :id decisions)]
    (require! (and (every? #(and (string? %) (not (str/blank? %))) ids)
                   (= (count ids) (count (set ids)))) "Missing or duplicate scope decision ID")
    (doseq [decision decisions :when (= :accepted (:state decision))]
      (let [authority (get by-source (:authority-source-occurrence-id decision))]
        (require! (and (= (:scope-version manifest) (:scope-version decision))
                       (string? (:authority decision)) (not (str/blank? (:authority decision)))
                       (string? (:reason decision)) (not (str/blank? (:reason decision)))
                       (= :source-block (:structural-kind authority))
                       (or (not (contains? decision :source-id))
                           (= (:source-id decision) (:document-id authority)))
                       (or (not (contains? decision :authority-sha256))
                           (= (:authority-sha256 decision) (:raw-sha256 authority))))
                  "Accepted scope decision lacks exact source authority, rationale or version")))))

(defn project
  "Pure non-authoritative preview of declared scope. Readiness is not an audited
  or independently accepted denominator: audit must verify checkout bytes and
  bridge snapshots before this projection is used as current-source evidence."
  ([manifest sources]
   (project manifest sources {}))
  ([manifest sources source-options]
   (let [documents (concat (:original-documents manifest) (:extension-sources manifest))
         ids (mapv :id documents)]
     (require! (and (every? #(and (string? %) (not (str/blank? %))) ids)
                    (= (count ids) (count (set ids)))) "Missing or duplicate declared source ID")
     (require! (= (set ids) (set (keys sources))) "Supplied sources differ from declared source population"))
   (require! (and (string? (:scope-version manifest)) (not (str/blank? (:scope-version manifest))))
             "Missing scope version")
   (let [views (requirement-sources/views manifest sources sha256 require! source-options)
        sources (requirement-sources/texts views)
        requirements (:requirements manifest) ids (mapv :id requirements)
        occurrences (mapcat (fn [[id text]] (source-occurrences id text)) sources)
        by-source (into {} (map (juxt :id identity) occurrences))
        dispositions (:source-dispositions manifest)
        req-set (set ids)
        _ (require! (every? #(and (string? %) (not (str/blank? %))) ids)
                    "Missing or invalid semantic requirement ID")
        _ (require! (= (count ids) (count req-set)) "Duplicate semantic requirement ID")
        _ (require! (= (count dispositions) (count (set (map :occurrence-id dispositions))))
                    "Duplicate source disposition")
        _ (validate-aliases! requirements (:aliases manifest))
        _ (validate-scope-lineage! manifest)
        _ (validate-scope-decisions! manifest by-source)
        mapped (into {} (map (juxt :occurrence-id identity) dispositions))
        facet-result (requirement-facets/validate! manifest sources by-source sha256)]
    (doseq [{:keys [occurrence-id kind requirement-ids rationale decision-id]} dispositions]
      (require! (contains? by-source occurrence-id) "Disposition targets missing source occurrence")
      (require! (#{:requirement :alias :refinement :nonnormative :superseded :scope-decision :unresolved} kind)
                "Unknown disposition kind")
      (when (#{:requirement :alias :refinement} kind)
        (require! (and (= :source-block (:structural-kind (get by-source occurrence-id)))
                       (seq requirement-ids) (every? req-set requirement-ids))
                  "Disposition has dangling requirement"))
      (when (#{:alias :refinement :nonnormative :unresolved} kind)
        (require! (not (str/blank? rationale)) "Missing semantic disposition rationale"))
      (when (#{:superseded :scope-decision} kind)
        (require! (contains? (set (map :id (:scope-decisions manifest))) decision-id)
                  "Missing versioned scope decision"))
      (when (= :superseded kind)
        (validate-supersession! manifest
                               (first (filter #(= decision-id (:id %)) (:scope-decisions manifest)))
                               occurrence-id by-source)))
    (require! (= req-set (set (mapcat :requirement-ids
                                     (filter #(#{:requirement :alias :refinement} (:kind %)) dispositions))))
              "Canonical requirement has no source provenance")
    (let [unresolved (vec (for [o occurrences
                               :when (and (= :source-block (:structural-kind o))
                                          (or (nil? (get mapped (:id o)))
                                              (not= :reviewed (:review-state (get mapped (:id o))))
                                              (contains? (:unresolved-occurrence-ids facet-result) (:id o))
                                              (#{:scope-decision :unresolved} (:kind (get mapped (:id o))))))]
                           (:id o)))
          unresolved-reqs (filter #(not (and (= :reviewed (:atomicity %))
                                             (= :reviewed (:nonoverlap %)))) requirements)
          pending-decisions (filter #(not= :accepted (:state %)) (:scope-decisions manifest))
          pending-aliases (filter #(not= :reviewed (:review-state %)) (:aliases manifest))
          bridges (vec (keep :bridge (vals views)))
          ready? (and (= :reviewed (:scope-state manifest)) (seq ids)
                      (empty? unresolved) (empty? unresolved-reqs) (empty? pending-decisions) (empty? pending-aliases)
                      (every? #(= :reviewed (:review-state %)) bridges))]
      {:authority :source-conservation-and-canonical-traceability-only
       :scope-version (:scope-version manifest)
       :source-documents (count sources) :source-occurrences (count occurrences)
       :source-version-bridges bridges
       :source-facet-count (:facets facet-result)
       :historical-superseded-facet-count (:historical-superseded-facets facet-result)
       :acceptance-qualification-facet-count (:acceptance-qualified-facets facet-result)
       :source-dispositions {:reviewed (count (filter #(= :reviewed (:review-state %)) dispositions))
                             :proposed (count (filter #(= :proposed (:review-state %)) dispositions))}
       :unresolved-source-occurrences unresolved
       :proposed-requirement-ids ids :denominator-ready? (boolean ready?)
       :alias-count (count (:aliases manifest)) :unreviewed-alias-count (count pending-aliases)
       :denominator (when ready? (count req-set))
       :acceptance-projection :not-yet-implemented
       :overall-rate nil
       :scope-decisions (:scope-decisions manifest)
       :limits [:source-blocks-are-not-atomic-requirements :no-implementation-credit
                :projection-requires-current-source-audit
                :historical-checkboxes-are-not-current-acceptance
                :no-compiler-replay :no-dashboard-write]}))))

(defn checked-path [root path]
  (require! (and (string? path) (not (.isAbsolute (io/file path)))) "Source path must be relative")
  (let [file (.getCanonicalFile (io/file root path))]
    (require! (and (.startsWith (.toPath file) (.toPath (.getCanonicalFile (io/file root))))
                   (.isFile file)) "Source path is absent or escapes checkout")
    file))
(defn read-source [file]
  (java.nio.file.Files/readString (.toPath (io/file file)) java.nio.charset.StandardCharsets/UTF_8))

(defn git-tree [root ref require!]
  (let [process (.start (.redirectErrorStream (ProcessBuilder. ^java.util.List
                                                               ["git" "-C" (str root)
                                                                "rev-parse" (str ref "^{tree}")]) true))
        output (with-open [input (.getInputStream process)] (slurp input))
        exit (.waitFor process)]
    (require! (zero? exit) "Task source version tree cannot be read")
    (str/trim output)))

(defn task-record-digest [record sha256]
  (sha256 (pr-str (select-keys record [:id :checked? :status :acceptance]))))

(defn validate-task-source-versions! [root manifest]
  (let [bridges (:task-source-version-bridges manifest)
        by-path (into {} (map (juxt :path identity) bridges))
        refs (mapcat (fn [requirement]
                       (concat (when-let [task (:task-reference requirement)] [task])
                               (:additional-task-interfaces requirement)))
                     (:requirements manifest))]
    (require! (and (vector? bridges) (seq bridges)
                   (= (count bridges) (count by-path)))
              "Missing or duplicate task source version bridge")
    (doseq [{:keys [path historical-ref historical-sha256 current-ref current-tree current-sha256
                    marker changed-records current-only-records change-class qualification]} bridges]
      (require! (and (string? path) (not (.isAbsolute (io/file path)))
                     (re-matches #"[0-9a-f]{40}" (or historical-ref ""))
                     (re-matches #"[0-9a-f]{40}" (or current-ref ""))
                     (re-matches #"[0-9a-f]{64}" (or historical-sha256 ""))
                     (re-matches #"[0-9a-f]{64}" (or current-sha256 ""))
                     (re-matches #"[0-9a-f]{40}" (or current-tree ""))
                     (string? marker) (not (str/blank? marker))
                     (keyword? change-class) (string? qualification) (not (str/blank? qualification)))
              (str "Invalid task source version bridge: " path))
      (require! (= current-tree (git-tree root current-ref require!))
                (str "Task source version tree drift: " path))
      (let [historical (requirement-sources/git-source root historical-ref path require!)
            current (requirement-sources/git-source root current-ref path require!)]
        (require! (= historical-sha256 (sha256 historical))
                  (str "Historical task source bridge hash drift: " path))
        (require! (= current-sha256 (sha256 current))
                  (str "Current task source bridge hash drift: " path))
        (require! (not= historical current)
                  (str "Task source bridge does not identify a change: " path))
        (doseq [{record-id :id :keys [historical-record-sha256 current-record-sha256]} changed-records]
          (let [old-record (requirement-evidence/task-record historical marker record-id)
                current-record (requirement-evidence/task-record current marker record-id)]
            (require! (= historical-record-sha256 (task-record-digest old-record sha256))
                      (str "Historical task record digest drift: " path "#" record-id))
            (require! (= current-record-sha256 (task-record-digest current-record sha256))
                      (str "Current task record digest drift: " path "#" record-id))))
        (doseq [{record-id :id current-record-sha256 :current-record-sha256} current-only-records]
          (let [current-record (requirement-evidence/task-record current marker record-id)]
            (require! (= current-record-sha256 (task-record-digest current-record sha256))
                      (str "Current-only task record digest drift: " path "#" record-id))
            (require! (and (false? (:checked? current-record))
                           (= "Open" (:status current-record)))
                      (str "Current-only task record is not explicitly unassessed: " path "#" record-id))))))
    (doseq [{:keys [path] expected-sha :sha256} refs]
      (if-let [bridge (get by-path path)]
        (require! (= expected-sha (:historical-sha256 bridge))
                  (str "Task reference lost historical identity: " path))
        (let [current (read-source (checked-path root path))]
          (require! (= expected-sha (sha256 current))
                    (str "Task/evidence source drift: " path)))))
    bridges))

(defn validate-bridge-projections! [manifest projected-views baseline-texts]
  (doseq [bridge (:source-version-bridges manifest)
          :when (requirement-sources/baseline-projection-kind? (:kind bridge))]
    (let [id (:document-id bridge)
          projected (get-in projected-views [id :text])
          baseline (get baseline-texts id)
          projected-occurrences (source-occurrences id projected)
          baseline-occurrences (source-occurrences id baseline)
          expected-facets (set (mapcat :facet-ids (:projection-hunks bridge)))
          declared-facets (if (= requirement-sources/std3-accounting-kind (:kind bridge))
                           (set (mapcat :facet-ids
                                        (get-in manifest [:stdlib-reconciliation-context :accounting-lineage
                                                          :mixed-std3-paragraphs])))
                           (set (map :id (:source-facets manifest))))]
      (require! (= baseline projected)
                "Source projection does not recover the authenticated baseline bytes")
      (require! (= (mapv :id baseline-occurrences) (mapv :id projected-occurrences))
                "Source projection changes source occurrence identity")
      (require! (set/subset? expected-facets declared-facets)
                "Source projection references an undeclared source facet"))))

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
    (let [task-source-bridges (validate-task-source-versions! root manifest)
          task-source-by-path (into {} (map (juxt :path identity) task-source-bridges))
          raw (into (sorted-map) (for [{:keys [id path]} sources] [id (read-source (checked-path root path))]))
          documents (into {} (map (juxt :id identity) sources))
          bridges (mapv (fn [bridge]
                          (let [document (get documents (:document-id bridge))]
                            (require! (and document (or (nil? (:path bridge))
                                                        (= (:path document) (:path bridge))))
                                      (str "Source bridge path does not match declared document: " (:document-id bridge)))
                            (requirement-sources/validate-snapshots!
                             (assoc bridge :path (:path document))
                             (fn [ref path] (requirement-sources/git-source root ref path require!))
                             sha256 require!)))
                        (:source-version-bridges manifest))
          baseline-texts (into {}
                              (for [bridge bridges
                                    :when (requirement-sources/baseline-projection-kind? (:kind bridge))]
                                [(:document-id bridge)
                                 (requirement-sources/git-source root (:baseline-ref bridge)
                                                                 (:path bridge) require!)]))
          source-options {:baseline-texts baseline-texts
                          :verified-baselines? (set (keys baseline-texts))}
          projected-views (requirement-sources/views manifest raw sha256 require! source-options)
          texts (requirement-sources/texts projected-views)
          result (project manifest raw source-options)]
      (validate-bridge-projections! manifest projected-views baseline-texts)
      (require! (= (count sources) (count texts)) "Duplicate original/extension source ID")
      (assoc result :source-baseline (:source-baseline manifest)
             :source-version-bridges bridges
             :task-source-version-bridges task-source-bridges
             :evidence-projections
             (mapv (fn [requirement]
                     (requirement-evidence/project
                      requirement
                      (fn [{:keys [path sha256]}]
                        (if-let [bridge (get task-source-by-path path)]
                          (let [working (read-source (checked-path root path))
                                current (requirement-sources/git-source root (:current-ref bridge)
                                                                      path require!)]
                            (require! (= (:current-sha256 bridge) (requirement-reconciliation/sha256 current))
                                      (str "Current task source bridge hash drift: " path))
                            (require! (= working current)
                                      (str "Working task source differs from bound current ref: " path))
                            (require! (= sha256 (:historical-sha256 bridge))
                                      (str "Task reference lost historical identity: " path))
                            current)
                          (let [text (read-source (checked-path root path))]
                            (require! (= sha256 (requirement-reconciliation/sha256 text))
                                      (str "Task/evidence source drift: " path))
                            text))) sha256 (:source-baseline manifest)))
                   (:requirements manifest))
             :manifest-sha256 (sha256 (read-source (checked-path root manifest-path)))
             :source-digests (into (sorted-map) (map (fn [[id text]] [id (sha256 text)]) texts))))))

(defn run [args]
  (require! (or (= ["--check"] (vec args)) (= "--source-map" (first args)))
            "Usage: clojure -M tools/requirement_reconciliation.clj --check | --source-map DOCUMENT_ID")
  (let [report (audit ".")]
    (if (= ["--check"] (vec args))
      (prn (-> report (dissoc :source-digests :unresolved-source-occurrences
                             :evidence-projections :proposed-requirement-ids)
               (assoc :proposed-requirement-count (count (:proposed-requirement-ids report))
                      :evidence-projection-count (count (:evidence-projections report))
                      :unresolved-source-occurrence-count (count (:unresolved-source-occurrences report)))))
      (let [[_ id & extra] args
            manifest (edn/read-string (read-source manifest-path))
            source (first (filter #(= id (:id %)) (concat (:original-documents manifest) (:extension-sources manifest))))
            bridge (first (filter #(= id (:document-id %)) (:source-version-bridges manifest)))
            baseline-texts (if (= requirement-sources/std3-accounting-kind (:kind bridge))
                            {id (requirement-sources/git-source "." (:baseline-ref bridge)
                                                               (:path bridge) require!)}
                            {})]
        (require! (and source (empty? extra)) "Unknown source ID or extra argument")
        (prn {:document-id id
              :path (:path source)
              :source-ref (:source-baseline manifest)
              :source-occurrences
              (source-occurrences
               id
               (:text
                (requirement-sources/view manifest source
                                          (read-source (checked-path "." (:path source)))
                                          sha256 require!
                                          {:baseline-texts baseline-texts
                                           :verified-baselines? (set (keys baseline-texts))})))})))))

(when (seq *command-line-args*) (run *command-line-args*))
