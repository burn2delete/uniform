(ns requirement-current-task-sources
  "Authenticate explicitly current task references without renewing evidence."
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def current-view :qualified-current-task-source-v1)
(def bridge-transition-kind :qualified-task-source-version-transition-v1)
(def bridge-successor-class :qualified-current-task-source-successor)

(defn current-reference? [reference]
  (= current-view (:source-view reference)))

(defn- digest? [size value]
  (and (string? value) (boolean (re-matches (re-pattern (str "[0-9a-f]{" size "}")) value))))

(defn- byte-count [text]
  (alength (.getBytes ^String text "UTF-8")))

(defn task-body-span [text marker task-id require!]
  (let [begin (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")
        occurrences #(count (re-seq (re-pattern (java.util.regex.Pattern/quote %)) text))
        a (str/index-of text begin) z (str/index-of text end)]
    (require! (and (= 1 (occurrences begin) (occurrences end)) a z (< a z))
              "Current task reference has an absent or ambiguous authority region")
    (let [region (subs text a z)
          matcher (re-matcher #"(?m)^- \[[ xX]\] `([^`]+)`" region)
          rows (loop [result []]
                 (if (.find matcher)
                   (recur (conj result {:id (.group matcher 1) :start (.start matcher)})) result))
          positions (keep-indexed #(when (= task-id (:id %2)) %1) rows)]
      (require! (= 1 (count positions)) "Current task reference has an absent or duplicate task identity")
      (let [n (first positions)
            start (+ a (:start (nth rows n)))
            finish (+ a (if (< (inc n) (count rows)) (:start (nth rows (inc n))) (count region)))]
        {:start-byte (byte-count (subs text 0 start)) :end-byte (byte-count (subs text 0 finish))}))))

(defn- source-span [text {:keys [start-byte end-byte]} require!]
  (let [bytes (.getBytes ^String text "UTF-8")]
    (require! (and (integer? start-byte) (integer? end-byte)
                   (<= 0 start-byte) (< start-byte end-byte) (<= end-byte (alength bytes)))
              "Current task criterion has invalid byte boundaries")
    (let [selected (java.util.Arrays/copyOfRange bytes (int start-byte) (int end-byte))
          result (String. selected "UTF-8")]
      (require! (java.util.Arrays/equals selected (.getBytes result "UTF-8"))
                "Current task criterion splits a UTF-8 code point")
      result)))

(defn- record-digest [record sha256]
  (sha256 (pr-str (select-keys record [:id :checked? :status :acceptance]))))

(defn- task-ids [text marker require!]
  (let [begin (str "<!-- " marker ":start -->") end (str "<!-- " marker ":end -->")
        a (str/index-of text begin) z (str/index-of text end)]
    (require! (and a z (< a z)) "Task source transition lacks its authority region")
    (let [ids (mapv second (re-seq #"(?m)^- \[[ xX]\] `([^`]+)`" (subs text a z)))]
      (require! (and (seq ids) (= (count ids) (count (set ids))))
                "Task source transition has absent or duplicate task identities")
      ;; The existing body reader also requires exactly one begin/end marker.
      (task-body-span text marker (first ids) require!)
      ids)))

(defn- validate-body-observation! [text marker id observation sha256 require!]
  (let [actual (task-body-span text marker id require!)]
    (require! (= actual (select-keys observation [:start-byte :end-byte]))
              "Task source transition body observation changes its task boundaries")
    (require! (= (or (:whole-record-body-sha256 observation) (:sha256 observation))
                 (sha256 (source-span text actual require!)))
              "Task source transition body observation digest drift")))

(defn validate-bridge-transition!
  "Authenticate the complete prior bridge table and task-body observations.
  This extends existing source-view history; it confers no evidence acceptance.
  The latest transition entry governs the current table; earlier entries remain
  preserved history. Source readers have the same strict commit contract as refs."
  [manifest {:keys [read-ref read-commit-tree task-record sha256 require!]}]
  (let [bridges (:task-source-version-bridges manifest)
        frames (filter #(= bridge-transition-kind (:kind %)) (:source-view-review-history manifest))
        successors (filter #(or (= bridge-successor-class (:change-class %)) (:predecessor-bridge %)) bridges)]
    (when (or (seq successors) (seq frames))
      (let [{:keys [source-commit source-tree manifest-sha256 prior-task-source-version-bridges qualification] :as frame} (last frames)
            by-path (into {} (map (juxt :path identity) bridges))]
        (require! (and frame (digest? 40 source-commit) (digest? 40 source-tree)
                       (digest? 64 manifest-sha256) (vector? prior-task-source-version-bridges)
                       (seq prior-task-source-version-bridges)
                       (string? qualification) (not (str/blank? qualification))
                       (ifn? read-commit-tree))
                  "Task source successor lacks its complete authenticated predecessor table")
        (require! (= source-tree (read-commit-tree source-commit))
                  "Task source predecessor model tree drift")
        (let [text (read-ref source-commit "contracts/requirement-reconciliation.edn")
              prior-model (edn/read-string text)
              prior-table (:task-source-version-bridges prior-model)
              prior-paths (set (map :path prior-table))]
          (require! (= manifest-sha256 (sha256 text)) "Task source predecessor model file digest drift")
          (require! (= prior-task-source-version-bridges prior-table)
                    "Task source transition does not preserve the complete prior bridge table")
          (require! (and (= frame (last (:source-view-review-history manifest)))
                         (= (vec (butlast (:source-view-review-history manifest)))
                            (vec (:source-view-review-history prior-model))))
                    "Task source transition rewrites prior source-view review history")
          (require! (= (count prior-table) (count prior-paths)) "Task source predecessor has duplicate bridge paths")
          (require! (every? by-path prior-paths) "Task source transition drops a prior bridge")
          (doseq [old prior-table :let [current (by-path (:path old))] :when (not= old current)]
            (require! (and (= bridge-successor-class (:change-class current)) (= old (:predecessor-bridge current)))
                      "Changed task source bridge loses its complete predecessor")
            (require! (= (select-keys old [:path :marker :historical-ref :historical-sha256])
                         (select-keys current [:path :marker :historical-ref :historical-sha256]))
                      "Task source successor rewrites historical identity")
            (require! (= (mapv #(select-keys % [:id :historical-record-sha256]) (:changed-records old))
                         (mapv #(select-keys % [:id :historical-record-sha256]) (:changed-records current)))
                      "Task source successor rewrites historical record identities")
            (require! (= (mapv :id (:current-only-records old)) (mapv :id (:current-only-records current)))
                      "Task source successor rewrites prior current-only record identities"))
          (doseq [{:keys [path marker historical-ref historical-sha256 current-ref current-tree current-sha256
                         predecessor-bridge previously-declared-record-observations record-observations]
                   :as bridge} successors]
            (require! (= bridge-successor-class (:change-class bridge)) "Unknown task source successor class")
            (require! (= (contains? prior-paths path) (some? predecessor-bridge))
                      "Task source successor invents or omits a predecessor")
            (require! (= current-tree (read-commit-tree current-ref)) "Task source successor current commit/tree drift")
            (read-commit-tree historical-ref)
            (let [previous-ref (or (:current-ref predecessor-bridge) historical-ref)
                  previous (read-ref previous-ref path)
                  historical (read-ref historical-ref path)
                  current (read-ref current-ref path)
                  observations (or previously-declared-record-observations record-observations)
                  expected-ids (if predecessor-bridge
                                 (set (concat (map :id (:changed-records predecessor-bridge))
                                              (map :id (:current-only-records predecessor-bridge))))
                                 (set (map :id (:changed-records bridge))))
                  old-ids (task-ids previous marker require!) new-ids (task-ids current marker require!)]
              (when predecessor-bridge
                (require! (= (:current-tree predecessor-bridge) (read-commit-tree previous-ref)) "Prior task source commit/tree drift")
                (require! (= (:current-sha256 predecessor-bridge) (sha256 previous)) "Prior task source file digest drift"))
              (require! (= historical-sha256 (sha256 historical)) "Task source successor historical file digest drift")
              (require! (= current-sha256 (sha256 current)) "Task source successor current file digest drift")
              (require! (= (set old-ids) (set new-ids)) "Task source successor changes task identities")
              (doseq [id old-ids]
                (let [old-record (task-record previous marker id) new-record (task-record current marker id)]
                  (require! (= ((juxt :checked? :status) old-record) ((juxt :checked? :status) new-record))
                            "Task source successor relabels task acceptance")))
              (require! (and (vector? observations) (= (count observations) (count (set (map :id observations)))))
                        "Task source successor has absent or duplicate body observations")
              (require! (every? (set (map :id observations)) expected-ids)
                        "Task source successor omits a declared record observation")
              (doseq [{:keys [id prior-captured-record-sha256 current-captured-record-sha256 prior-whole-body current-whole-body]} observations]
                (let [old-record (task-record previous marker id) new-record (task-record current marker id)]
                  (require! (= prior-captured-record-sha256 (record-digest old-record sha256)) "Task source successor prior captured record drift")
                  (require! (= current-captured-record-sha256 (record-digest new-record sha256)) "Task source successor current captured record drift")
                  (validate-body-observation! previous marker id prior-whole-body sha256 require!)
                  (validate-body-observation! current marker id current-whole-body sha256 require!))))))
        {:kind bridge-transition-kind :source-commit source-commit :source-tree source-tree
         :prior-bridge-count (count prior-task-source-version-bridges)
         :implementation-acceptance :unassessed}))))

(defn validate!
  "Check current Git/file/record/criterion identity only. The caller supplies
  authenticated source readers and its existing strict task-record parser.
  read-commit-tree must reject Git objects whose actual type is not commit.
  Historical references must continue through their separate legacy checks."
  [reference bridge {:keys [read-ref read-commit-tree read-current task-record sha256 require!]}]
  (let [{:keys [path marker id source-commit source-tree observed-status observed-checked?
                source-record-sha256 criterion-source detailed-acceptance qualification]
         file-sha256 :sha256} reference]
    (require! (current-reference? reference) "Unknown current task reference view")
    (require! (and (string? path) (not (str/blank? path))
                   (not (.isAbsolute (io/file path)))
                   (not (str/includes? path "\\"))
                   (not-any? #{"" "." ".."} (str/split path #"/" -1))
                   (string? marker) (not (str/blank? marker))
                   (string? id) (not (str/blank? id))
                   (digest? 40 source-commit) (digest? 40 source-tree)
                   (digest? 64 file-sha256) (digest? 64 source-record-sha256)
                   (string? qualification) (not (str/blank? qualification))
                   (= :unassessed detailed-acceptance)
                   (map? criterion-source) (digest? 64 (:sha256 criterion-source)))
              "Current task reference lacks its explicit source and evidence qualification")
    (when bridge
      (require! (= [path marker source-commit source-tree file-sha256]
                   ((juxt :path :marker :current-ref :current-tree :current-sha256) bridge))
                "Current task reference differs from its qualified current bridge"))
    (require! (ifn? read-commit-tree) "Current task reference lacks a commit-object reader")
    (require! (= source-tree (read-commit-tree source-commit)) "Current task reference tree drift")
    (let [snapshot (read-ref source-commit path)
          current (read-current path)
          record (task-record snapshot marker id)
          body (task-body-span snapshot marker id require!)
          criterion (source-span snapshot criterion-source require!)]
      (require! (= file-sha256 (sha256 snapshot)) "Current task reference Git file digest drift")
      (require! (= snapshot current) "Current task reference is not bound to the current file")
      (require! (= source-record-sha256 (sha256 (pr-str (select-keys record [:id :checked? :status :acceptance]))))
                "Current task reference captured record digest drift")
      (require! (= [observed-status observed-checked?] ((juxt :status :checked?) record))
                "Current task reference relabels the observed task state")
      (require! (<= (:start-byte body) (:start-byte criterion-source)
                     (:end-byte criterion-source) (:end-byte body))
                "Current task criterion belongs to another task")
      (require! (= (:sha256 criterion-source) (sha256 criterion)) "Current task criterion digest drift")
      {:source-view current-view :path path :id id :source-commit source-commit :source-tree source-tree
       :sha256 file-sha256 :source-record-sha256 source-record-sha256 :criterion-source criterion-source
       :observed-status observed-status :observed-checked? observed-checked?
       :authority :current-task-source-interface-only :implementation-acceptance :unassessed})))
