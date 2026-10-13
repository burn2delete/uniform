
(defn- validate-git-observation!
  [observation ordinal operation reason]
  (exact-keys! observation git-observation-keys
               "SH07-B51-RECEIPT-JOIN" reason)
  (let [argv (:argv observation)
        parsed (:parsed-result observation)]
    (when-not
     (and (= ordinal (:ordinal observation))
          (= operation (:operation observation))
          (vector? argv)
          (<= 3 (count argv))
          (= git-executable (first argv))
          (= "-C" (second argv))
          (string? (nth argv 2 nil))
          (not (str/blank? (nth argv 2 nil)))
          (= 0 (:stdin-byte-count observation))
          (= empty-raw-sha256 (:stdin-raw-sha256 observation))
          (= 0 (:exit-code observation))
          (integer? (:stdout-byte-count observation))
          (not (neg? (:stdout-byte-count observation)))
          (sha256-id? (:stdout-raw-sha256 observation))
          (= 0 (:stderr-byte-count observation))
          (= empty-raw-sha256 (:stderr-raw-sha256 observation))
          (map? parsed))
      (fail! "SH07-B51-RECEIPT-JOIN" reason
             {:ordinal ordinal :operation operation}))
    (case operation
      :show-tree
      (when-not
       (and (= 7 (count argv))
            (= ["show" "-s" "--format=%H%n%T%n%P"]
               (subvec argv 3 6))
            (git-id? (nth argv 6))
            (= #{:commit :tree :parents} (set (keys parsed)))
            (git-id? (:commit parsed))
            (git-id? (:tree parsed))
            (vector? (:parents parsed))
            (every? git-id? (:parents parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      :diff-tree-name-status-z
      (when-not
       (and (= 10 (count argv))
            (= ["diff-tree" "--no-commit-id" "--name-status" "-r" "-z"]
               (subvec argv 3 8))
            (git-id? (nth argv 8))
            (git-id? (nth argv 9))
            (= #{:parent :commit :entries} (set (keys parsed)))
            (= (nth argv 8) (:parent parsed))
            (= (nth argv 9) (:commit parsed))
            (vector? (:entries parsed))
            (every? #(and (= #{:status :path} (set (keys %)))
                          (= :added (:status %))
                          (safe-relative-path? (:path %)))
                    (:entries parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      :ls-tree-z-requested
      (when-not
       (and (<= 8 (count argv))
            (= ["ls-tree" "-z"] (subvec argv 3 5))
            (git-id? (nth argv 5))
            (= "--" (nth argv 6))
            (= (vec (drop 7 argv)) (:requested-paths parsed))
            (= #{:commit :requested-paths :rows} (set (keys parsed)))
            (= (nth argv 5) (:commit parsed))
            (vector? (:requested-paths parsed))
            (= (count (:requested-paths parsed))
               (count (distinct (:requested-paths parsed))))
            (every? safe-relative-path? (:requested-paths parsed))
            (vector? (:rows parsed))
            (= (count (:requested-paths parsed)) (count (:rows parsed)))
            (every?
             #(and (= #{:path :present :mode :type :oid} (set (keys %)))
                   (safe-relative-path? (:path %))
                   (boolean? (:present %))
                   (if (:present %)
                     (and (= "100644" (:mode %))
                          (= "blob" (:type %))
                          (git-id? (:oid %)))
                     (and (nil? (:mode %)) (nil? (:type %))
                          (nil? (:oid %)))))
             (:rows parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      :cat-file-blob
      (when-not
       (and (= 6 (count argv))
            (= ["cat-file" "blob"] (subvec argv 3 5))
            (git-id? (nth argv 5))
            (or (= #{:oid :byte-count :raw-sha256} (set (keys parsed)))
                (= #{:oid :byte-count :raw-sha256 :value}
                   (set (keys parsed))))
            (= (nth argv 5) (:oid parsed))
            (integer? (:byte-count parsed))
            (not (neg? (:byte-count parsed)))
            (sha256-id? (:raw-sha256 parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      (fail! "SH07-B51-RECEIPT-JOIN" reason
             {:ordinal ordinal :operation operation})))
  observation)

(defn- validate-git-observation-sequence!
  [observations operations reason]
  (when-not (and (vector? observations)
                 (= (count operations) (count observations)))
    (fail! "SH07-B51-RECEIPT-JOIN" reason
           {:expected-count (count operations)
            :actual-count (when (vector? observations)
                            (count observations))}))
  (mapv (fn [ordinal [observation operation]]
          (validate-git-observation!
           observation ordinal operation reason))
        (range)
        (map vector observations operations)))

(def ^:private eligibility-object-operation-sequence
  [:show-tree :diff-tree-name-status-z :ls-tree-z-requested
   :cat-file-blob :ls-tree-z-requested])

(def ^:private pre-discharge-operation-sequence
  (vec (concat [:show-tree :ls-tree-z-requested]
               (repeat (count pre-c-paths) :cat-file-blob)
               [:show-tree :ls-tree-z-requested :cat-file-blob])))

(declare reject-runtime-back-edges!)

(defn- validate-participant-checks!
  [coordinate participant-results]
  (when-not (and (vector? participant-results)
                 (= eligibility-participant-order
                    (mapv :participant participant-results)))
    (fail! "SH07-B51-RECEIPT-JOIN" :participant-order
           {:participants (when (vector? participant-results)
                            (mapv :participant participant-results))}))
  (doseq [[ordinal result] (map-indexed vector participant-results)]
    (exact-keys! result #{:participant :eligibility-check
                           :eligibility-check-root}
                 "SH07-B51-RECEIPT-JOIN" :participant-result-keys)
    (let [check (:eligibility-check result)]
      (exact-keys! check eligibility-check-keys
                   "SH07-B51-RECEIPT-JOIN" :eligibility-check-keys)
      (validate-git-observation-sequence!
       (:observations check) eligibility-object-operation-sequence
       :eligibility-observation-sequence)
      (validate-git-observation-sequence!
       (:pre-discharge-observations check)
       pre-discharge-operation-sequence
       :pre-discharge-observation-sequence)
      (when-not
       (and (= coordinate (:coordinate check))
            (= eligibility-check-count-values
               (select-keys check (keys eligibility-check-count-values)))
            (every? true? (map check eligibility-check-match-keys))
            (sha256-id? (:eligibility-check-root result))
            (= (:eligibility-check-root result)
               (codec/H [:gravity/sh07-b51-eligibility-check-root-v1
                         check])))
        (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-check
               {:participant (:participant result)
                :ordinal ordinal}))))
  participant-results)

(defn validate-evidence-eligibility!
  "Validate the normalized E1 EVIDENCE-ELIGIBILITY carrier.

  The optional context map is used by the builder and strict identity-receipt
  path to prove every value still equals the independently decoded Q receipt
  and the ordered L/M/A/B checks.  No runtime or later lifecycle value is
  accepted in this carrier."
  ([eligibility]
   (validate-evidence-eligibility! eligibility nil))
  ([eligibility {:keys [content receipt participant-results]}]
   (exact-keys! eligibility evidence-eligibility-keys
                "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-keys)
   (let [coordinate (:coordinate eligibility)
         pre-root (:pre-masked-root eligibility)]
     (validate-eligibility-coordinate! content coordinate)
     (validate-pre-masked-root! pre-root)
     (when-not (and (sha256-id? (:receipt-id eligibility))
                    (= (:receipt-id coordinate) (:receipt-id eligibility))
                    (= :frozen (:freeze-state eligibility))
                    (sha256-id? (:freeze-record-id eligibility))
                    (sha256-id? (:source-descriptors-root eligibility))
                    (sha256-id? (:pre-discharge-id eligibility))
                    (vector? (:participant-check-roots eligibility))
                    (= 4 (count (:participant-check-roots eligibility)))
                    (every? sha256-id? (:participant-check-roots eligibility)))
       (fail! "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-shape))
     (when receipt
       (validate-eligibility-receipt! (or content (:content receipt)) receipt)
       (let [pre-discharge (:pre-evidence-contract-discharge receipt)
             expected-record-id
             (get-in receipt [:freeze :active-record-ref :record-id])]
         (when-not
          (and (= (:receipt-id receipt) (:receipt-id eligibility))
               (= :frozen (get-in receipt [:freeze :state]))
               (= expected-record-id (:freeze-record-id eligibility))
               (= (:source-descriptors-root receipt)
                  (:source-descriptors-root eligibility))
               (= (:pre-masked-root receipt) pre-root)
               (= (:discharge-id pre-discharge)
                  (:pre-discharge-id eligibility))
               (= (:source-descriptors-root receipt)
                  (codec/H [:gravity/sh07-b51-source-descriptors-root-v1
                            (:source-descriptors receipt)])))
           ;; The coordinate/receipt relationship is checked below as a
           ;; complete map comparison; this branch is intentionally explicit
           ;; so a missing nested Q field cannot be treated as a nil default.
           (fail! "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-q-join
                  {:eligibility eligibility}))
         (when (and content (not= content (:content receipt)))
           (fail! "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-content))))
     (when participant-results
       (validate-participant-checks! coordinate participant-results)
       (when-not (= (mapv :eligibility-check-root participant-results)
                    (:participant-check-roots eligibility))
         (fail! "SH07-B51-RECEIPT-JOIN"
                :evidence-eligibility-participant-roots)))
     (reject-runtime-back-edges! eligibility [])
     eligibility)))
