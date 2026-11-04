(ns advance.catalog
  "Current source-bound Spec Kit tasks, with historical checklists excluded."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.runtime :as rt]))

(defn parse [path text]
  (let [lines (str/split-lines text)
        marked? (boolean (re-find #"<!-- [a-z0-9-]+-implementation-tasks:start -->" text))]
    (loop [remaining (map-indexed vector lines) region nil historical? false rows [] current nil]
      (if-let [[index line] (first remaining)]
        (let [[_ start] (re-matches #"<!-- ([a-z0-9-]+-implementation-tasks):start -->" line)
              [_ end] (re-matches #"<!-- ([a-z0-9-]+-implementation-tasks):end -->" line)
              historical? (or historical? (boolean (re-find #"(?i)^#+ .*historical" line)))
              row (re-matches #"- \[([ xX])\] (.+)" line)
              eligible? (if marked? (some? region) (not historical?))]
          (cond
            start (do (when region (rt/fail! "Nested implementation task regions" {:path path}))
                      (recur (rest remaining) start historical? (cond-> rows current (conj current)) nil))
            end (do (when-not (= end region) (rt/fail! "Unmatched task region" {:path path :region end}))
                    (recur (rest remaining) nil historical? (cond-> rows current (conj current)) nil))
            (and row eligible?)
            (let [[_ checked label] row
                  [_ id title] (or (re-matches #"`([^`]+)`\s*:?[ ]*(.*)" label)
                                   (re-matches #"([A-Z]+-?[0-9]+)\s+(.*)" label))
                  id (or id (str "item-" (rt/digest label)))]
              (recur (rest remaining) region historical? (cond-> rows current (conj current))
                     {:key (str path "#" id) :id id :path path :line (inc index)
                      :title (if (str/blank? title) label title) :checked (not= " " checked)
                      :authority (if marked? :implementation-region :unmarked)
                      :feature (str (.getParent (io/file path))) :body line :source-digest (rt/digest text)}))
            :else (recur (rest remaining) region historical? rows
                         (when current (if eligible? (update current :body str "\n" line) current)))))
        (do
          (when region (rt/fail! "Unclosed task region" {:path path :region region}))
          (let [result (cond-> rows current (conj current))]
            (when-not (= (count result) (count (set (map :key result))))
              (rt/fail! "Duplicate task identity" {:path path}))
            result))))))

(defn discover [root]
  (let [directory (io/file root "specs")
        files (sort-by str (filter #(and (.isFile %) (= "tasks.md" (.getName %))) (file-seq directory)))]
    (when (empty? files) (rt/fail! "No Spec Kit task files found" {:root root}))
    (let [entries (mapv (fn [file]
                          (let [path (str (.relativize (.toPath (io/file root)) (.toPath file)))
                                text (slurp file)]
                            {:path path :digest (rt/digest text) :tasks (parse path text)})) files)]
      {:files (mapv #(dissoc % :tasks) entries)
       :tasks (vec (mapcat :tasks entries))
       :unclassified (mapv :path (filter #(empty? (:tasks %)) entries))})))

(defn index [catalog]
  (update catalog :tasks #(mapv (fn [row] (dissoc row :body)) %)))

(defn validate-selection! [catalog selected known]
  (let [by-key (into {} (map (juxt :key identity) (:tasks catalog)))
        keys (map :key selected)]
    (when-not (= (count keys) (count (set keys))) (rt/fail! "Duplicate increment keys" {}))
    (doseq [increment selected]
      (let [source (get by-key (:task increment))]
        (when-not (and source (not (:checked source)))
          (rt/fail! "Selection must name a current open task" {:task (:task increment)}))
        (when (contains? known (:key increment))
          (rt/fail! "A previous increment cannot be selected again" {:key (:key increment)}))
        (when-not (some #{(:path source)} (:paths increment))
          (rt/fail! "Increment must own its source task file to record scoped evidence" {:task (:task increment)}))))
    selected))
