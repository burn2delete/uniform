(ns requirement-sources
  "Exact pinned source views across documented authority-only layout transfers."
  (:require [clojure.string :as str]))

(def platform-authority
  (str "Implementation task authority: [Spec Kit platform ledger](../../specs/platform-workflow/tasks.md#implementation-tasks).\n"
       "Edit the ledger and run `clojure -M tools/platform_task_projection.clj --write`;\n"
       "the project table and full-product scope below are generated.\n\n"))
(def platform-markers
  (for [region ["platform-task-projection" "platform-product-projection"] side ["start" "end"]]
    (str "<!-- " region ":" side " -->\n")))

(defn remove-once [text addition require!]
  (let [parts (str/split text (re-pattern (java.util.regex.Pattern/quote addition)) -1)]
    (require! (= 2 (count parts)) "Source transfer addition is missing or duplicated")
    (apply str parts)))

(defn view [manifest document text sha256 require!]
  (let [live-sha (sha256 text)]
    (if (or (nil? document) (= (:sha256 document) live-sha))
      {:text text}
      (let [bridges (filter #(= (:id document) (:document-id %)) (:source-version-bridges manifest))
            b (first bridges)]
        (require! (and (= 1 (count bridges)) (= :platform-task-authority-layout-only (:kind b))
                       (= "platform-product" (:id document))
                       (= "docs/component-roadmaps/platform.md" (:path document))
                       (= (:source-baseline manifest) (:baseline-ref b))
                       (= (:sha256 document) (:baseline-sha256 b)) (= live-sha (:live-sha256 b))
                       (re-matches #"[0-9a-f]{40}" (or (:applying-ref b) ""))
                       (#{:proposed :reviewed} (:review-state b)))
                  (str "Source drift lacks an exact supported version bridge: " (:id document)))
        (let [pinned (reduce #(remove-once %1 %2 require!)
                             (remove-once text platform-authority require!) platform-markers)]
          (require! (= (:sha256 document) (sha256 pinned)) "Source bridge changes pinned normative bytes")
          {:text pinned :bridge (assoc b :path (:path document) :span-basis :pinned-baseline-view
                                       :snapshot-provenance :unverified)})))))

(defn views [manifest sources sha256 require!]
  (let [documents (into {} (map (juxt :id identity)
                                (concat (:original-documents manifest) (:extension-sources manifest))))]
    (into (sorted-map) (map (fn [[id text]] [id (view manifest (get documents id) text sha256 require!)]) sources))))

(defn texts [views]
  (into (sorted-map) (map (fn [[id v]] [id (:text v)]) views)))

(defn git-source [root ref path require!]
  (let [process (.start (.redirectErrorStream (ProcessBuilder. ^java.util.List ["git" "-C" (str root) "show" (str ref ":" path)]) true))
        out (with-open [input (.getInputStream process)] (slurp input :encoding "UTF-8"))
        exit (.waitFor process)]
    (require! (zero? exit) "Source version bridge commit/path cannot be read")
    out))

(defn validate-snapshots! [bridge read-ref sha256 require!]
  (doseq [[ref expected] [[(:baseline-ref bridge) (:baseline-sha256 bridge)]
                          [(:applying-ref bridge) (:live-sha256 bridge)]]]
    (require! (boolean (re-matches #"[0-9a-f]{40}" (or ref ""))) "Invalid bridge source identity")
    (let [text (read-ref ref (:path bridge))]
      (require! (and (string? text) (= expected (sha256 text))) "Bridge snapshot bytes differ from declared source")))
  (assoc bridge :snapshot-provenance :git-bytes-verified))
