(ns ai-requirement-proposal-test
  "Source-only controls for the bounded A1-A11 task-interface proposal."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell] [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/domains_task_projection.clj"))
(require '[domains-task-projection])
(def proposal-path "specs/requirement-reconciliation/phase-11-proposal.edn")
(def source-ref "e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30")
(defn check! [ok message] (when-not ok (throw (ex-info message {}))))
(defn sha [text]
  (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes ^String text "UTF-8")))))
(defn pinned [path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str source-ref ":" path))]
    (check! (zero? exit) "Pinned proposal source absent") out))

;; Evaluate only the three pure API definitions from the preserved source.
;; The namespace's loaders, audit entrypoint and compiler are never evaluated.
(let [api-ns (create-ns 'ai-proposal-source-api)
      forms (with-open [r (java.io.PushbackReader. (java.io.StringReader.
                                                   (pinned "tools/requirement_reconciliation.clj")))]
              (doall (take-while #(not= ::eof %)
                                 (repeatedly #(read {:eof ::eof} r)))))
      selected (filter #(and (seq? %) (= 'defn (first %))
                             (#{'require! 'sha256 'source-occurrences} (second %))) forms)]
  (check! (= 3 (count selected)) "Source occurrence API changed")
  (binding [*ns* api-ns]
    (clojure.core/refer 'clojure.core)
    (require '[clojure.string :as str])
    (doseq [form selected] (eval form))))
(def occurrences (ns-resolve 'ai-proposal-source-api 'source-occurrences))
(def proposal (edn/read-string (slurp proposal-path)))
(check! (= 2 (count *command-line-args*))
        "Supply preserved applying-manifest and prompt-authority-bridge paths; source reads only")
(def applying-text (slurp (first *command-line-args*)))
(def bridge-text (slurp (second *command-line-args*)))
(check! (= "aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980" (sha applying-text)) "Applying manifest drift")
(check! (= "4785ba6180bfc193bfe3f5677aab1c323a8b142d2fd81895285a6be21364a76d" (sha bridge-text)) "Prompt bridge drift")
(def applying (edn/read-string applying-text))
(def bridge (edn/read-string bridge-text))
(def document-ids (set (map #(str "A" %) (range 1 12))))
(def expected-docs (filterv #(document-ids (:id %)) (:original-documents applying)))
(def expected-facets (filterv #(document-ids (:document-id %)) (:source-facets applying)))
(def expected-ids (set (mapcat :unit-ids expected-facets)))
(def expected-records
  (into {} (for [r (:requirements applying) :when (expected-ids (:id r))]
             [(:id r) (if (= (:id r) (get-in bridge [:target-edit :id]))
                        (get-in bridge [:target-edit :after]) r)])))
(def task-source-ref "cac16db049f191f0f44310f4da15c3fe2222d066")
(def task-text
  (let [{:keys [exit out]} (shell/sh "git" "show" (str task-source-ref ":specs/domains-workflow/tasks.md"))]
    (check! (zero? exit) "Reviewed AI task source unavailable") out))
(def task-rows (into {} (map (juxt :id identity) (domains-task-projection/records task-text))))
(def source-bytes (into {} (map (fn [{:keys [id path]}] [id (.getBytes (slurp path) "UTF-8")]) expected-docs)))
(defn qualified [f]
  (assoc f :qualification
         (String. (java.util.Arrays/copyOfRange ^bytes (source-bytes (:document-id f))
                                              (int (:start-byte f)) (int (:end-byte f))) "UTF-8")))
(defn slot [r k] (if (contains? r k) {:present? true :value (get r k)} {:present? false}))
(defn task-reference! [r]
  (check! (= "specs/domains-workflow/tasks.md" (:path r)) "Wrong task source")
  (check! (= (sha task-text) (:sha256 r)) "Task hash drift")
  (check! (= "domains-implementation-tasks" (:marker r)) "Missing task marker")
  (check! (contains? task-rows (:id r)) "Missing or renamed task")
  (check! (= "Open" (:status (task-rows (:id r)))) "Unexpected implementation acceptance"))
(defn validate! [p]
  (check! (= expected-docs (:source-documents p)) "Original source pins lost")
  (check! (= expected-facets (:source-facets p)) "Facet or negative-condition loss")
  (check! (= expected-ids (set (map :proposed-id (:units p)))) "Semantic IDs changed")
  (check! (= (count expected-ids) (count (:units p))) "Duplicate semantic ID")
  (let [os (vec (mapcat (fn [{:keys [id path sha256]}]
                          (let [text (slurp path)]
                            (check! (= sha256 (sha text)) "Original source hash drift")
                            (occurrences id text))) expected-docs))
        dispositions (:source-dispositions p)]
    (check! (= os (:source-occurrences p)) "Invented or changed source occurrence")
    (check! (= (set (map :id os)) (set (map :occurrence-id dispositions))) "Lost occurrence disposition")
    (check! (= (count os) (count dispositions)) "Repeated disposition")
    (check! (= (:inherited-source-dispositions p)
                (filterv #(document-ids (first (str/split (:occurrence-id %) #"/")))
                         (:source-dispositions applying))) "Source disposition history lost")
    (doseq [d (:inherited-source-dispositions p)]
      (check! (= (assoc d :unit-ids (:requirement-ids d))
                 (first (filter #(= (:occurrence-id d) (:occurrence-id %)) dispositions)))
              "Source context or negative-condition disposition changed")))
  (doseq [{:keys [path id]} expected-docs
          :let [bytes (.getBytes (slurp path) "UTF-8")]
          f (:source-facets p) :when (= id (:document-id f))]
    (check! (= (:raw-sha256 f)
                (sha (String. (java.util.Arrays/copyOfRange bytes (int (:start-byte f))
                                                          (int (:end-byte f))) "UTF-8")))
            "Raw facet byte hash differs"))
  (doseq [u (:units p)
          :let [id (:proposed-id u) r (expected-records id)]]
    (check! (= r (:inherited-record u)) "Lost inherited context, alias target or history")
    (check! (= (:obligation r) (:obligation u)) "Changed source obligation")
    (check! (= (mapv qualified (filter #(some #{id} (:unit-ids %)) expected-facets)) (:acceptance-facets u))
            "Lost acceptance facet or negative condition")
    (check! (and (= :unassessed (:current-acceptance u)) (= :unreviewed (:nonoverlap u))
                 (= (:atomicity r) (:atomicity u))) "Unsupported review or completion")
    (check! (= 1 (count (:task-references u))) "Missing task interface")
    (check! (= "ai" (:id (first (:task-references u)))) "Wrong outcome")
    (doseq [ref (:task-references u)] (task-reference! ref)))
  (check! (= (mapv :id (filter :unresolved expected-facets)) (:unresolved-facet-ids p))
          "Mixed accounting residual silently resolved")
  (check! (= (vec (mapcat (fn [u]
                           (let [r (:inherited-record u)]
                             [{:requirement-id (:id r) :field :task-reference
                               :before (slot r :task-reference)
                               :after {:present? true :value (first (:task-references u))}}
                              {:requirement-id (:id r) :field :task-gap
                               :before (slot r :task-gap) :after {:present? false}}])) (:units p)))
             (:field-operations p)) "Field patch changes unrelated state or predecessor")
  (doseq [ref (:task-dependencies p)] (task-reference! ref))
  true)
(defn rejects? [p] (try (validate! p) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest conserved-family-and-task-projection
  (is (validate! proposal))
  (is (= [11 697 1174 110 220 21]
         (mapv #(count (% proposal)) [:source-documents :source-occurrences :source-facets
                                     :units :field-operations :unresolved-facet-ids])))
  (let [current (slurp domains-task-projection/task-path)
        rows (into {} (map (juxt :id identity) (domains-task-projection/records current)))]
    (is (= (set (keys task-rows)) (set (keys rows))))
    (is (str/includes? (:projection (rows "ai")) (:projection (task-rows "ai"))))
    (is (= "Open" (:status (rows "ai"))))
    (is (domains-task-projection/check-projection current (slurp domains-task-projection/roadmap-path)))))

(deftest refuses-source-and-task-pin-drift
  (doseq [p [(assoc-in proposal [:source-documents 0 :sha256] "wrong")
             (assoc-in proposal [:source-occurrences 0 :id] "invented")
             (assoc-in proposal [:source-facets 0 :raw-sha256] "wrong")
             (assoc-in proposal [:units 0 :task-references 0 :sha256] "wrong")
             (assoc-in proposal [:units 0 :task-references 0 :marker] "missing")
             (assoc-in proposal [:units 0 :task-references 0 :id] "renamed")
             (assoc-in proposal [:units 0 :task-references 0 :path] "missing.md")]]
    (is (rejects? p))))

(deftest refuses-context-and-negative-condition-loss
  (let [negative (first (keep-indexed #(when (= :negative-acceptance (:kind %2)) %1) expected-facets))]
    (is (rejects? (update proposal :source-facets #(vec (concat (take negative %) (drop (inc negative) %)))))))
  (doseq [p [(assoc-in proposal [:units 0 :acceptance-facets] [])
             (assoc-in proposal [:units 0 :inherited-record :obligation] "alias with different context")
             (assoc-in proposal [:units 0 :current-acceptance] :accepted)
             (assoc-in proposal [:field-operations 0 :before] {:present? false :value "different"})
             (assoc proposal :unresolved-facet-ids [])
             (update proposal :source-dispositions pop)]]
    (is (rejects? p))))

(let [r (run-tests)]
  (shutdown-agents)
  (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
