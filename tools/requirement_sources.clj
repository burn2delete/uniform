(ns requirement-sources
  "Exact pinned source views across documented authority-only layout transfers."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def platform-authority
  (str "Implementation task authority: [Spec Kit platform ledger](../../specs/platform-workflow/tasks.md#implementation-tasks).\n"
       "Edit the ledger and run `clojure -M tools/platform_task_projection.clj --write`;\n"
       "the project table and full-product scope below are generated.\n\n"))
(def platform-markers
  (for [region ["platform-task-projection" "platform-product-projection"] side ["start" "end"]]
    (str "<!-- " region ":" side " -->\n")))

(def std3-accounting-kind :stdlib-accounting-policy-semantic-projection)
(def std3-accounting-path
  "docs/phase-16-standard-library/213-std3-collections-library-specification.md")
(def std3-accounting-baseline-ref
  "a1b991be499d50579a8b4366976c519ae8949224")
(def std3-accounting-applying-ref
  "5cf644298b95940b6499c52af1260dfdb609e7e0")
(def std3-accounting-baseline-sha256
  "58d096cc16d922ad72d560c5b3e4d60d093c9527a9472c2414d63deb98630e3d")
(def std3-accounting-live-sha256
  "af847392b2ff50155d61a9a9c93bb6e2f1cc573220ceb11920de4dd483bd4160")

;; These are the six reviewed source regions, not a text diff computed from the
;; checkout.  The exact baseline/live fragment hashes make an omitted hunk,
;; retained-semantic edit, or additional source mutation fail closed.
(def std3-accounting-projection-hunks
  [{:lineage-id
    "STD3/source/c0d68f7417c5026e583fc882d1ef84f94a93602a1917ee86bafd7bfa24700add"
    :facet-ids ["library/STD3/facet-lines-112-124"]
    :baseline-start-line 112 :baseline-end-line 124
    :live-start-line 112 :live-end-line 121
    :baseline-fragment-sha256
    "04048bcc61900fe2b948e68cbb284907e9d08d84ea13fceae5a48941de81bac0"
    :live-fragment-sha256
    "6f50c16e7bf77c39b55fc9380cda6838d8a8e4dd9024567f1f92182bbf8a5cab"}
   {:lineage-id
    "STD3/source/aecb86c0a5694c16e471c5ac4121a538eb02797764f2286b72425433778d820a"
    :facet-ids ["library/STD3/facet-lines-313-316"]
    :baseline-start-line 313 :baseline-end-line 316
    :live-start-line 310 :live-end-line 313
    :baseline-fragment-sha256
    "6c96de00b8181ff5c8f317e6a32966a6bc0f903e0a5a85d0084817a645236627"
    :live-fragment-sha256
    "f29ac12289fd27f2fff92db7e70e36390408a026b0c0d14d1ff7113b7ae5cedc"}
   {:lineage-id
    "STD3/source/fb024105ccc2bca425c08ad66b8b4b712b61b252baa8ad1946812879e1f0976e"
    :facet-ids ["library/STD3/facet-lines-332-341"]
    :baseline-start-line 332 :baseline-end-line 341
    :live-start-line 329 :live-end-line 338
    :baseline-fragment-sha256
    "cb7faced2f9c61f9c9ee5c2d0faaad4aac8648b47a8816c854d314ff5525018a"
    :live-fragment-sha256
    "b650cc70ac19658e03bd75691c062bf8a75f25563b28562e0873a95c152d487f"}
   {:lineage-id
    "STD3/source/d06b51d0b75765c6d2636f3768c83ac62d6915344d229bdc028174dcb09c0224"
    :facet-ids ["library/STD3/facet-lines-353-357"]
    :baseline-start-line 353 :baseline-end-line 357
    :live-start-line 350 :live-end-line 354
    :baseline-fragment-sha256
    "6db8045cb192b447aaab6e5429cd85c8a0ddd7b560fc937ad5e1434b5ad8f27c"
    :live-fragment-sha256
    "8fba07359e9bf3b800e4685897a3dd418ad7d2cafd8b0fa9b71f05a5acddad2b"}
   {:lineage-id
    "STD3/source/138372855ff20dec2623a643a41bc8891a601b7251a67711d7a9342035961ddb"
    :facet-ids ["library/STD3/facet-lines-359-366"]
    :baseline-start-line 359 :baseline-end-line 366
    :live-start-line 356 :live-end-line 363
    :baseline-fragment-sha256
    "ad4982e5c6a9cfb0067db0c580923f7240394e3782dac683f340b483a99015a9"
    :live-fragment-sha256
    "1d951bbba50073fc1351c0a3637e93ec01aa129acd7c5ae68d20410cf3aabecb"}
   {:lineage-id
    "STD3/source/21f80d05376fa7244a3b47ce45415409177a7da74f97d0e412d92244394dd4e8"
    :facet-ids (mapv #(format "library/STD3/facet-lines-%d-%d" % %)
                     (range 497 528))
    :baseline-start-line 497 :baseline-end-line 527
    :live-start-line 494 :live-end-line 524
    :baseline-fragment-sha256
    "ac7dc6454f761f0572ad2407a78baf4bd395a85680f2ad5ce44082b7cc25c7f4"
    :live-fragment-sha256
    "ffca258d344464aa30dd1107d91c940106f89a1d4858c18beb3ccc8f55ac15f6"}])

(defn- expected-std3-bridge? [bridge]
  (and (= std3-accounting-kind (:kind bridge))
       (= "STD3" (:document-id bridge))
       (= std3-accounting-path (:path bridge))
       (= std3-accounting-baseline-ref (:baseline-ref bridge))
       (= std3-accounting-applying-ref (:applying-ref bridge))
       (= std3-accounting-baseline-sha256 (:baseline-sha256 bridge))
       (= std3-accounting-live-sha256 (:live-sha256 bridge))
       (= "ca89df08c128c25d23a4a0781da79dd5e37abd74" (:baseline-blob bridge))
       (= "19581f5720fea820ff97a1afc392cf3446f44bc3" (:applying-blob bridge))
       (= std3-accounting-projection-hunks (:projection-hunks bridge))))

(defn- line-segments [text]
  (let [parts (str/split text #"\n" -1)
        terminal-newline? (= "" (last parts))
        content (if terminal-newline? (butlast parts) parts)
        n (count content)]
    (mapv (fn [[idx line]]
            (str line (when (or terminal-newline? (< idx (dec n))) "\n")))
          (map-indexed vector content))))

(defn- line-fragment [text start-line end-line require!]
  (let [segments (line-segments text)]
    (require! (and (integer? start-line) (integer? end-line)
                   (pos? start-line) (<= start-line end-line) (<= end-line (count segments)))
              "Source projection line span is outside the authenticated snapshot")
    (apply str (subvec segments (dec start-line) end-line))))

(defn- replace-line-fragment [text start-line end-line replacement require!]
  (let [segments (line-segments text)]
    (require! (and (pos? start-line) (<= start-line end-line) (<= end-line (count segments)))
              "Source projection replacement span is outside the live source")
    (apply str (concat (subvec segments 0 (dec start-line))
                       replacement
                       (subvec segments end-line)))))

(defn- std3-lineage-records [manifest require!]
  (let [records (get-in manifest [:stdlib-reconciliation-context :accounting-lineage :mixed-std3-paragraphs])
        by-id (into {} (map (juxt :id identity) records))]
    (require! (= (count records) (count by-id)) "Duplicate STD3 accounting lineage ID")
    (require! (= (set (keys by-id)) (set (map :lineage-id std3-accounting-projection-hunks)))
              "STD3 accounting lineage contains an unprojected or missing region")
    (doseq [{:keys [lineage-id facet-ids baseline-start-line baseline-end-line
                    baseline-fragment-sha256]} std3-accounting-projection-hunks]
      (let [record (get by-id lineage-id)]
        (require! (and record
                       (= "STD3" (:document-id record))
                       (= std3-accounting-baseline-sha256 (:source-sha256 record))
                       (= :source-block (:structural-kind record))
                       (= :mixed-policy-supersession (:disposition record))
                       (= baseline-start-line (:start-line record))
                       (= baseline-end-line (:end-line record))
                       (= baseline-fragment-sha256 (:raw-sha256 record))
                       (= facet-ids (:facet-ids record)))
                  (str "STD3 accounting lineage is incomplete or changed: " lineage-id))))
    (mapv #(get by-id (:lineage-id %)) std3-accounting-projection-hunks)))

(defn- std3-accounting-view [manifest bridge text baseline sha256 require! verified?]
  (require! (expected-std3-bridge? bridge) "STD3 accounting bridge identity or projection contract changed")
  (require! (= (:source-baseline manifest) (:baseline-ref bridge))
            "STD3 accounting bridge is detached from the manifest source baseline")
  (std3-lineage-records manifest require!)
  (require! (string? baseline) "STD3 accounting bridge requires a verified baseline snapshot")
  (require! (= std3-accounting-baseline-sha256 (sha256 baseline))
            "STD3 accounting baseline snapshot bytes differ from the declared source")
  (require! (= std3-accounting-live-sha256 (sha256 text))
            "STD3 accounting live source bytes differ from the declared applying snapshot")
  (let [projected
        (reduce (fn [current hunk]
                  (let [live-fragment (line-fragment current (:live-start-line hunk)
                                                     (:live-end-line hunk) require!)
                        baseline-fragment (line-fragment baseline (:baseline-start-line hunk)
                                                         (:baseline-end-line hunk) require!)]
                    (require! (= (:live-fragment-sha256 hunk) (sha256 live-fragment))
                              "STD3 accounting bridge encountered unlisted or retained-semantic live changes")
                    (require! (= (:baseline-fragment-sha256 hunk) (sha256 baseline-fragment))
                              "STD3 accounting bridge baseline facet bytes differ")
                    (replace-line-fragment current (:live-start-line hunk) (:live-end-line hunk)
                                           (line-segments baseline-fragment) require!)))
                text
                (sort-by :live-start-line > std3-accounting-projection-hunks))]
    (require! (= std3-accounting-baseline-sha256 (sha256 projected))
              "STD3 accounting projection leaves unlisted source drift")
    {:text projected
     :bridge (assoc bridge :span-basis :authenticated-stdlib-baseline-view
                    :snapshot-provenance (if verified? :git-bytes-verified :unverified))}))

(defn remove-once [text addition require!]
  (let [parts (str/split text (re-pattern (java.util.regex.Pattern/quote addition)) -1)]
    (require! (= 2 (count parts)) "Source transfer addition is missing or duplicated")
    (apply str parts)))

(defn view
  ([manifest document text sha256 require!]
   (view manifest document text sha256 require! {}))
  ([manifest document text sha256 require! {:keys [baseline-texts verified-baselines?]}]
   (let [live-sha (sha256 text)]
     (if (or (nil? document) (= (:sha256 document) live-sha))
       {:text text}
       (let [bridges (filter #(= (:id document) (:document-id %)) (:source-version-bridges manifest))
             b (first bridges)]
         (cond
           (and (= 1 (count bridges)) (= std3-accounting-kind (:kind b)))
           (std3-accounting-view manifest b text (get baseline-texts (:id document)) sha256 require!
                                  (contains? verified-baselines? (:id document)))

           :else
           (do
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
                                            :snapshot-provenance :unverified)}))))))))

(defn views
  ([manifest sources sha256 require!]
   (views manifest sources sha256 require! {}))
  ([manifest sources sha256 require! opts]
   (let [documents (into {} (map (juxt :id identity)
                                 (concat (:original-documents manifest) (:extension-sources manifest))))]
     (into (sorted-map)
           (map (fn [[id text]] [id (view manifest (get documents id) text sha256 require! opts)]) sources)))))

(defn texts [views]
  (into (sorted-map) (map (fn [[id v]] [id (:text v)]) views)))

(defn git-source [root ref path require!]
  (let [process (.start (.redirectErrorStream (ProcessBuilder. ^java.util.List ["git" "-C" (str root) "show" (str ref ":" path)]) true))
        out (with-open [input (.getInputStream process)] (slurp input :encoding "UTF-8"))
        exit (.waitFor process)]
    (require! (zero? exit) "Source version bridge commit/path cannot be read")
    out))

(defn validate-snapshots! [bridge read-ref sha256 require!]
  (require! (and (string? (:path bridge)) (not (.isAbsolute (io/file (:path bridge)))))
            "Source version bridge path must be relative")
  (doseq [[ref expected] [[(:baseline-ref bridge) (:baseline-sha256 bridge)]
                          [(:applying-ref bridge) (:live-sha256 bridge)]]]
    (require! (boolean (re-matches #"[0-9a-f]{40}" (or ref ""))) "Invalid bridge source identity")
    (let [text (read-ref ref (:path bridge))]
      (require! (and (string? text) (= expected (sha256 text))) "Bridge snapshot bytes differ from declared source")))
  (assoc bridge :snapshot-provenance :git-bytes-verified))
