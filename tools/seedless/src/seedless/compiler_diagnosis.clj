(ns seedless.compiler-diagnosis
  "Read-only source investigation is separate from repair and progress authority."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.compiler-probe :as probe]
            [seedless.compiler-observation :as compiler]
            [simplify.runtime :as rt]
            [simplify.schema :as s]))

(def ownership-path "docs/self-hosting-slice-ownership.edn")
(def requirements-path "docs/self-hosting-milestone-completion-plan.md")
(def reference
  (s/object-of {:path s/text :sha256 s/text
                :line-start {:type "integer" :minimum 1}
                :line-end {:type "integer" :minimum 1} :excerpt s/text}))
(def semantic-check
  (s/object-of {:kind {:type "string" :enum ["positive" "negative"]}
                :command (assoc (s/array-of s/text) :minItems 1) :establishes s/text}))
(def schema
  (s/object-of
   {:outcome {:type "string" :enum ["repair" "prerequisite" "investigate" "reuse" "blocked"]}
    :source-revision s/text :input-id s/text :ownership-id s/text
    :summary s/text :cause s/text :producer-refs (s/array-of reference)
    :trigger-refs (s/array-of reference)
    :owners (s/array-of
             (s/object-of {:role {:type "string" :enum ["repair" "prerequisite"]}
                           :milestone s/text :task s/text :paths (assoc (s/array-of s/text) :minItems 1)
                           :requirement reference}))
    :checks (s/array-of semantic-check) :preserve s/text
    :candidates (s/array-of
                 (s/object-of {:candidate s/text
                               :disposition {:type "string" :enum ["reuse" "wait" "independent-successor"]}
                               :reason s/text}))
    :missing-evidence (s/array-of s/text)}))

(def progress-schema
  (s/object-of
   {:outcome {:type "string" :enum ["established" "pending"]}
    :base s/text :candidate s/text :tree s/text
    :before-input-id s/text :after-input-id s/text :summary s/text
    :original-obstruction
    (s/object-of {:removed {:type "boolean"} :evidence s/text
                  :before-refs (s/array-of reference) :after-refs (s/array-of reference)})
    :preceding-behavior (s/object-of {:preserved {:type "boolean"} :evidence s/text})
    :checks (s/array-of semantic-check) :missing-evidence (s/array-of s/text)}))

(defn- read-one [text]
  (let [reader (java.io.PushbackReader. (java.io.StringReader. text)) eof (Object.)
        options {:eof eof :readers {}
                 :default (fn [tag _] (rt/fail! "Unsupported diagnosis evidence tag" {:tag tag}))}
        value (edn/read options reader)]
    (when (or (identical? eof value) (not (identical? eof (edn/read options reader))))
      (rt/fail! "Diagnosis evidence must contain exactly one EDN value" {}))
    value))

(defn- captured [observation]
  (let [result (:execution observation)
        text (reduce (fn [remaining warning]
                       (if (and (re-matches #"\[[0-9]+\.[0-9]+s\]\[warning\]\[perf,memops\] Cannot use file /tmp/hsperfdata_[A-Za-z0-9_.-]+/[0-9]+ because it is locked by another process \(errno = 11\)\r?\n" warning)
                                (str/starts-with? remaining warning))
                         (subs remaining (count warning))
                         (rt/fail! "Compiler capture warnings do not match retained output" {})))
                     (or (:stdout result) "") (:execution-warnings observation))]
    (when (or (:capture-truncated result) (not= 1 (:exitCode result)))
      (rt/fail! "Investigation needs a complete normally terminated rejection capture" {}))
    (read-one text)))

(defn authentic-rejection?
  "Check the retained execution itself. A structured compiler result rejection
  grants investigation only; host exceptions and unattributed failures do not."
  [cwd observation argv]
  (try
    (let [raw (captured observation) closure (get-in raw [:route :closure])
          index (get-in raw [:position :component])
          component (when (nat-int? index) (get closure index))
          source (get-in raw [:failure :source])
          stable [:source-revision :input-id :inputs :route :position :completed :subject :terminal]]
      (and (not (:restricted observation))
           (= :blocked (:status raw)) (= :unclassified (get-in raw [:failure :kind]))
           ;; Preserve the collector's existing final-release boundary even
           ;; when validating a retained capture directly during recovery.
           (not= "P18T04007" (get-in raw [:failure :id]))
           (= :rejected (get-in raw [:failure :diagnostic :compiler-result-status]))
           (= {:status :failed :exit 1} (:terminal raw))
           (re-matches #"[0-9a-f]{40}" (or (:source-revision raw) ""))
           (= (mapv :path (:inputs raw)) (vec probe/input-paths))
           (= (select-keys raw stable) (select-keys observation stable))
           (= (get-in raw [:failure :id]) (get-in observation [:failure :id]))
           (= (get-in raw [:failure :diagnostic]) (get-in observation [:failure :diagnostic]))
           (= source (get-in observation [:failure :source]))
           (compiler/current? cwd raw argv)
           (seq closure) (= (count (:completed raw)) index)
           (= (mapv :component-id (:completed raw))
              (mapv :component-id (take index closure)))
           (#{:current-frontend :canonical-core :core-verification :function-types :type-verification}
            (get-in raw [:position :stage]))
           (= source (:source-path component) (get-in raw [:subject :source-path]))
           (= (get-in raw [:subject :source-sha256]) (compiler/input-sha256 cwd source))
           (= :llvm-x86_64-linux (get-in raw [:route :target]))
           (= "x86_64-unknown-linux-gnu" (get-in raw [:route :triple]))))
    (catch Exception _ false)))

(defn investigable? [cwd observation argv]
  (and (= :blocked (:status observation)) (= :unclassified (get-in observation [:failure :kind]))
       (authentic-rejection? cwd observation argv)))

(defn- file-identity [cwd path]
  {:path path :sha256 (compiler/input-sha256 cwd path)})

(defn- ownership-context [cwd]
  (try
    (let [inputs (mapv #(file-identity cwd %) [ownership-path requirements-path])]
      {:id (rt/digest (pr-str inputs)) :inputs inputs})
    (catch Exception e {:id "unavailable" :inputs [] :reason (.getMessage e)})))

(defn context [cwd observation known open-prs]
  {:observation observation
   :ownership (ownership-context cwd)
   :requirements-path requirements-path :ownership-path ownership-path
   :candidates (compiler/candidate-context known open-prs)
   :instructions
   "Trace the producing implementation and triggering program with exact current file hashes and line excerpts. Match existing SH task text and ownership, including current migrated paths. A diagnostic token alone is not causal attribution. Preserve malformed-input rejection. Name missing runtime facts precisely; do not invent them. Existing candidates are retained evidence, not absent work. Source inspection cannot authorize a repair or prove progress by itself."})

(defn- reference! [contents ref]
  (rt/safe-path! (:path ref))
  (let [text (contents (:path ref)) lines (vec (str/split text #"\r?\n" -1))
        start (:line-start ref) end (:line-end ref)]
    (when-not (and (pos-int? start) (pos-int? end) (<= start end (count lines))
                   (= (:sha256 ref) (probe/sha256 (.getBytes text "UTF-8")))
                   (= (:excerpt ref) (str/join "\n" (subvec lines (dec start) end))))
      (rt/fail! "Source reference does not match its exact file identity and line range" {:reference ref})))
  ref)

(defn- current-contents [cwd path]
  ;; Check canonical containment and symbolic links before reading text.
  (compiler/input-sha256 cwd path)
  (slurp (io/file cwd path) :encoding "UTF-8"))

(defn- covered? [observation path]
  (some #(or (= path (:path %)) (str/starts-with? path (str (:path %) "/")))
        (:inputs observation)))

(defn- ownership-record [cwd]
  (let [record (read-one (current-contents cwd ownership-path))]
    (when-not (= :gravity/self-hosting-slice-ownership-v1 (:schema record))
      (rt/fail! "Current implementation ownership registry is unavailable" {}))
    record))

(defn- permitted-path? [record milestone path]
  (let [[_ kind stem] (re-matches #"bootstrap/clojure/(src|test)/gravity/([^/.]+)(?:/[^.].*|\.clj)" path)
        module (when stem (str/replace (if (= kind "test") (str/replace stem #"_test$" "") stem) "_" "-"))
        owner (get-in record [:slice-owners milestone :leaf-owner])
        recorded (or (get-in record [:module-owners path])
                     (get-in record [:standalone-test-owners path :owner])
                     (get-in record [:reserved-leaf-modules module]))
        digits (subs milestone 3)
        conventions (:future-path-conventions record)]
    (and owner
         (if recorded (= owner recorded)
             (or (and (some #{:new-fixture} (:worker-may-create conventions))
                      (str/starts-with? path (str "bootstrap/clojure/fixtures/self-hosting/sh-" digits "/"))
                      (re-find #"/(accepted|rejected)/" path))
                 (and (some #{:dedicated-test} (:worker-may-create conventions))
                      (re-matches (re-pattern (str "bootstrap/clojure/test/gravity/self_hosting/sh" digits "_[A-Za-z0-9_]+_test\\.clj")) path)))))))

(defn- validate-owner! [cwd record owner]
  (let [{:keys [milestone task paths requirement]} owner]
    (when-not (and (re-matches #"SH-(?:[01][0-9]|2[0-9])" milestone)
                   (re-matches (re-pattern (str (java.util.regex.Pattern/quote milestone) "\\.[1-9][0-9]*")) task)
                   (= requirements-path (:path requirement))
                   (str/includes? (:excerpt requirement) (str "**" task ":**")))
      (rt/fail! "Diagnosis names no existing numbered SH requirement" {:owner owner}))
    (reference! #(current-contents cwd %) requirement)
    (doseq [path paths]
      (rt/safe-path! path)
      (when-not (permitted-path? record milestone path)
        (rt/fail! "Diagnosis path is not owned by the existing milestone" {:path path :milestone milestone})))
    (when-not (= (count paths) (count (set paths)))
      (rt/fail! "Diagnosis duplicates an owned path" {:owner owner})))
  owner)

(defn- failure-identity [observation]
  [(get-in observation [:failure :source]) (:position observation)
   (get-in observation [:failure :id])
   (select-keys (get-in observation [:failure :diagnostic]) [:reason :function :operator])
   ;; Local span coordinates distinguish two occurrences of one diagnostic.
   ;; Absolute checkout paths and whole-product node IDs are not work identity.
   (select-keys (get-in observation [:failure :diagnostic :source-span])
                [:byte-start :byte-end :line-start :line-end :column-start :column-end])])

(defn- candidate-snapshot [known open-prs]
  (->> (compiler/candidate-context known open-prs)
       (map #(select-keys % [:candidate-id :key :number :paths :status]))
       (sort-by :candidate-id) vec))

(defn- trigger-attributed? [observation references]
  (let [span (get-in observation [:failure :diagnostic :source-span])]
    (and (every? #(= (get-in observation [:failure :source]) (:path %)) references)
         (if (and (pos-int? (:line-start span)) (pos-int? (:line-end span)))
           (some #(<= (:line-start %) (:line-start span) (:line-end span) (:line-end %)) references)
           true))))

(defn validate!
  "Validate model output against the observation, current source, governing
  ownership and retained candidates before authorizing normal planning."
  [cwd argv observation proposal known open-prs]
  (s/validate! schema proposal)
  (when-not (investigable? cwd observation argv)
    (rt/fail! "Compiler rejection is not current, complete investigation evidence" {}))
  (let [ownership (ownership-context cwd) implementation? (#{"repair" "prerequisite"} (:outcome proposal))
        references (concat (:producer-refs proposal) (:trigger-refs proposal))
        owners (:owners proposal) repair (filter #(= "repair" (:role %)) owners)
        prerequisites (filter #(= "prerequisite" (:role %)) owners)
        candidates (into {} (map (juxt :candidate-id identity) (compiler/candidate-context known open-prs)))
        candidate-ids (map :candidate (:candidates proposal))
        retained (candidate-snapshot known open-prs)]
    (when-not (and (= (:source-revision observation) (:source-revision proposal))
                   (= (:input-id observation) (:input-id proposal))
                   (= (:id ownership) (:ownership-id proposal)))
      (rt/fail! "Diagnosis does not bind the observed revision, inputs and ownership" {}))
    (when-not (= (count candidate-ids) (count (set candidate-ids)))
      (rt/fail! "Diagnosis repeats a retained candidate disposition" {}))
    (doseq [id candidate-ids]
      (when-not (contains? candidates id)
        (rt/fail! "Diagnosis names an unknown retained candidate" {:candidate id})))
    (doseq [ref references]
      (when-not (covered? observation (:path ref))
        (rt/fail! "Diagnosis source reference is outside declared compiler inputs" {:reference ref}))
      (reference! #(current-contents cwd %) ref))
    (when (and (= "reuse" (:outcome proposal))
               (not-any? #(= "reuse" (:disposition %)) (:candidates proposal)))
      (rt/fail! "Reuse requires the exact existing candidate identity" {}))
    (when (and (#{"blocked" "investigate"} (:outcome proposal)) (empty? (:missing-evidence proposal)))
      (rt/fail! "Unresolved investigation must name the missing evidence or capability" {}))
    (when implementation?
      (when-not (and (not= "unavailable" (:id ownership))
                     (seq (:producer-refs proposal)) (seq (:trigger-refs proposal))
                     (= 1 (count repair))
                     (if (= "prerequisite" (:outcome proposal)) (seq prerequisites) true)
                     (empty? (:missing-evidence proposal))
                     (= #{"positive" "negative"} (set (map :kind (:checks proposal))))
                     (trigger-attributed? observation (:trigger-refs proposal)))
        (rt/fail! "Repair diagnosis lacks source attribution, ownership or semantic checks" {}))
      (let [record (ownership-record cwd) owner (first repair)]
        (doseq [declared owners] (validate-owner! cwd record declared))
        (when-not (some (set (:paths owner)) (map :path (:producer-refs proposal)))
          (rt/fail! "Repair owner does not own the producing implementation" {}))
        (doseq [prerequisite prerequisites]
          (when-not (some (set (:paths prerequisite)) (map :path references))
            (rt/fail! "Prerequisite ownership has no observed source relationship" {:owner prerequisite})))
        (doseq [candidate (vals candidates)
                :when (some #(rt/overlap? (:paths %) (:paths candidate)) owners)]
          (when-not (some #{(:candidate-id candidate)} candidate-ids)
            (rt/fail! "Diagnosis omitted an overlapping retained candidate" {:candidate (:candidate-id candidate)})))))
    (let [inputs (vec (distinct (concat (:inputs ownership)
                                       (map #(select-keys % [:path :sha256]) references)
                                       (map #(select-keys (:requirement %) [:path :sha256]) owners))))
          diagnosis {:id (rt/digest (pr-str [(:input-id observation) proposal inputs retained]))
                     :proposal proposal :inputs inputs :candidates retained :raw-observation observation}
          diagnosed (assoc observation :diagnosis diagnosis)]
      (if-not implementation?
        (assoc diagnosed :reason (:summary proposal))
        (let [owner (first repair)
              owner-record (fn [owner] (-> owner (dissoc :role :requirement)
                                           (assoc :record requirements-path)))
              key (str "compiler/diagnosed/" (rt/digest (pr-str [(failure-identity observation) (:task owner)])))]
          (-> diagnosed
              (assoc :status :actionable-failure)
              (update :failure merge
                      {:kind :implementation :work-key key :reason (:cause proposal)
                       :producer (:summary proposal) :consumer (str (get-in observation [:position :stage]))
                       :owner (owner-record owner) :prerequisites (mapv owner-record prerequisites)})))))))

(defn current-diagnosis? [cwd observation]
  (try
    (let [diagnosis (:diagnosis observation)
          known (into {} (map (fn [candidate] [(:key candidate) candidate])
                              (filter :key (:candidates diagnosis))))
          prs (filterv #(and (:number %) (not (:key %))) (:candidates diagnosis))
          raw (:raw-observation diagnosis)]
      (and diagnosis (seq (:inputs diagnosis))
           (authentic-rejection? cwd observation (get-in observation [:route :command]))
           (let [validated (validate! cwd (get-in observation [:route :command]) raw
                                      (:proposal diagnosis) known prs)]
             (and (= (select-keys observation [:status :failure]) (select-keys validated [:status :failure]))
                  (= (select-keys diagnosis [:id :proposal :inputs :candidates])
                     (select-keys (:diagnosis validated) [:id :proposal :inputs :candidates]))))))
    (catch Exception _ false)))

(defn validate-selection! [cwd observation slice]
  (when (:diagnosis observation)
    (when-not (current-diagnosis? cwd observation)
      (rt/fail! "Diagnosis evidence changed before implementation admission" {:compiler-observation-stale true}))
    (let [owner (first (filter #(and (= (:compiler-role slice) (:role %))
                                     (= (:compiler-task slice) (:task %))
                                     (= (:stage slice) (:milestone %)))
                               (get-in observation [:diagnosis :proposal :owners])))]
      (when-not (and owner (seq (:paths slice))
                     (every? (set (:paths owner)) (:paths slice))
                     (some (set (:paths slice))
                           (map :path (concat (get-in observation [:diagnosis :proposal :producer-refs])
                                              (get-in observation [:diagnosis :proposal :trigger-refs])))))
        (rt/fail! "Selected implementation exceeds diagnosed source ownership" {:slice (:key slice)}))))
  slice)

(defn- revision-contents [ctx cwd revision path]
  (rt/safe-path! path)
  (when-not (re-matches #"[0-9a-f]{40}" revision)
    (rt/fail! "Progress source reference needs an immutable revision" {:revision revision}))
  (let [entry (rt/git ctx cwd "ls-tree" revision "--" path)]
    (when-not (re-find #"^100(?:644|755) blob " entry)
      (rt/fail! "Progress source reference is not a regular committed file" {:path path :revision revision})))
  ;; run!/git intentionally trim output; source hashing needs exact blob bytes.
  (let [argv ["git" "show" (str revision ":" path)] key (rt/digest (pr-str argv))
        _ (rt/assert-authorized! ctx :command key)
        _ (when (get-in @(:state ctx) [:restricted-commands key])
            (rt/fail! "Restricted source inspection cannot be retried" {:restricted true :argv argv}))
        result (rt/command ctx cwd argv)]
    (when-not (zero? (:exit result))
      (let [restricted (rt/authorization-denied? (str (:err result) "\n" (:out result)))]
        (when restricted (rt/remember-denial! ctx :command key {:argv argv :error (:err result)}))
        (rt/fail! "Progress source revision is unavailable"
                  {:path path :revision revision :restricted restricted :result result})))
    (:out result)))

(defn validate-progress!
  "Validate an independently produced progress assessment against actual check
  receipts and immutable source. This never substitutes for ordinary review."
  [ctx task proposal]
  (s/validate! progress-schema proposal)
  (let [before (:compiler-observation task) after (:compiler-result task)
        cwd (:worktree task) claim (:original-obstruction proposal)
        before-refs (:before-refs claim) after-refs (:after-refs claim)
        checks (:checks task) commands (set (:commands checks))
        before-source-identities (concat (:inputs before) (get-in before [:diagnosis :inputs])
                                         [{:path (get-in before [:subject :source-path])
                                           :sha256 (get-in before [:subject :source-sha256])}
                                          (get-in before [:failure :witness :consumer-source])])
        recorded-paths (into #{} (keep #(when (and (string? (:path %))
                                                   (re-matches #"[0-9a-f]{64}" (or (:sha256 %) "")))
                                          (:path %))) before-source-identities)
        original-paths (set (filter recorded-paths
                                   (concat [(get-in before [:failure :source])
                                            (get-in before [:failure :witness :consumer-source :path])]
                                           (get-in before [:failure :owner :paths])
                                           (map :path (get-in before [:diagnosis :proposal :producer-refs])))))]
    (when-not (and (= (select-keys task [:base :candidate :tree]) (select-keys proposal [:base :candidate :tree]))
                   (= (:input-id before) (:before-input-id proposal))
                   (= (:input-id after) (:after-input-id proposal)))
      (rt/fail! "Progress assessment does not bind the exact candidate and compiler executions" {}))
    (if (= "pending" (:outcome proposal))
      (do (when (empty? (:missing-evidence proposal))
            (rt/fail! "Pending progress assessment must name missing evidence" {})) false)
      (do
        (let [raw (captured before)
              bound [:source-revision :input-id :inputs :route :position :completed :subject :terminal]]
          (when-not (and (= (:input-id before) (compiler/input-id before))
                         (= (select-keys raw bound) (select-keys before bound))
                         (= (get-in raw [:failure :diagnostic]) (get-in before [:failure :diagnostic]))
                         (= (get-in raw [:failure :source]) (get-in before [:failure :source]))
                         (= (get-in raw [:failure :id]) (get-in before [:failure :id])))
            (rt/fail! "Progress baseline is not bound to its retained compiler capture" {})))
        (when-not (and (compiler/current? cwd after (get-in ctx [:config :compiler-observation-command]))
                       (or (= :scoped-pass (:status after))
                           (authentic-rejection? cwd after (get-in ctx [:config :compiler-observation-command])))
                       (compiler/nonregressing? before after)
                       (not= (failure-identity before) (failure-identity after))
                       (not= (:input-id before) (:input-id after))
                       (:removed claim) (get-in proposal [:preceding-behavior :preserved])
                       (seq before-refs) (seq after-refs)
                       (empty? (:missing-evidence proposal)))
          (rt/fail! "Progress needs removal of the original obstruction on a comparable complete route" {}))
        (when-not (and (= :passed (:result checks)) (= (:candidate task) (:candidate checks))
                       (= (:tree task) (:tree checks))
                       (= #{"positive" "negative"} (set (map :kind (:checks proposal))))
                       (every? commands (map :command (:checks proposal)))
                       (every? commands (map :command (get-in before [:diagnosis :proposal :checks]))))
          (rt/fail! "Progress needs actual positive and negative semantic check receipts for this candidate" {}))
        (doseq [ref before-refs]
          (when-not (some #(= (select-keys ref [:path :sha256]) (select-keys % [:path :sha256]))
                          before-source-identities)
            (rt/fail! "Progress before-source reference has no recorded compiler input identity" {:reference ref}))
          (reference! #(revision-contents ctx cwd (:source-revision before) %) ref))
        (doseq [ref after-refs]
          (reference! #(revision-contents ctx cwd (:candidate task) %) ref)
          (reference! #(current-contents cwd %) ref))
        (when-not (some (fn [old]
                          (and (original-paths (:path old))
                               (some #(and (= (:path old) (:path %)) (not= (:sha256 old) (:sha256 %))) after-refs)))
                        before-refs)
          (rt/fail! "Progress lacks changed source evidence at the original producing or triggering implementation" {}))
        true))))
