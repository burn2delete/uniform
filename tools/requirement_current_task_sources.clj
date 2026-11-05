(ns requirement-current-task-sources
  "Authenticate explicitly current task references without renewing evidence."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def current-view :qualified-current-task-source-v1)

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

(defn validate!
  "Check current Git/file/record/criterion identity only. The caller supplies
  authenticated source readers and its existing strict task-record parser.
  Historical references must continue through their separate legacy checks."
  [reference bridge {:keys [read-ref read-tree read-current task-record sha256 require!]}]
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
    (require! (= source-tree (read-tree source-commit)) "Current task reference tree drift")
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
