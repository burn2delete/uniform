
(defn- validate-pre-discharge-envelope!
  [content receipt]
  (exact-keys! receipt pre-discharge-keys
               "SH07-B51-RECEIPT-JOIN" :pre-discharge-keys)
  (exact-keys! (:evidence receipt)
               #{:schema :phase :row-plan-id :carriers}
               "SH07-B51-RECEIPT-JOIN" :pre-phase-evidence-keys)
  (let [carriers (get-in receipt [:evidence :carriers])]
    (exact-keys! carriers pre-carrier-keys
                 "SH07-B51-RECEIPT-JOIN" :pre-carrier-keys)
    (exact-keys! (:pre-masked-root carriers)
                 #{:coordinates :preimage-spans :preimage-files
                   :preimage-root}
                 "SH07-B51-RECEIPT-JOIN" :pre-masked-root-keys)
    (let [resolved (resolve-contract-phase! :pre-evidence carriers)]
      (when-not
     (and (= :gravity/sh07-b51-pre-evidence-contract-discharge-v1
             (:artifact receipt))
          (= 1 (:schema-version receipt))
          (= :pre-evidence (:phase receipt))
          (= row-plan-id (:row-plan-id receipt))
          (nil? (:prior-ref receipt))
          (= :gravity/sh07-b51-phase-evidence-v1
             (get-in receipt [:evidence :schema]))
          (= :pre-evidence (get-in receipt [:evidence :phase]))
          (= row-plan-id (get-in receipt [:evidence :row-plan-id]))
          (= content (:content carriers))
          (= row-plan (:row-plan carriers))
          (= [[:discharge-id]] (:self-id-omits receipt))
          (= (:discharge-id receipt)
             (codec/H
              [:gravity/sh07-b51-pre-evidence-contract-discharge-v1
               (dissoc receipt :discharge-id)]))
          (= (:resolved-root resolved) (:resolved-root receipt))
          (= (:resolved-rows resolved) (:resolved-rows receipt)))
        (fail! "SH07-B51-RECEIPT-JOIN" :pre-discharge-envelope))))
  receipt)

(defn- validate-eligibility-receipt!
  [content receipt]
  (exact-keys! receipt eligibility-receipt-keys
               "SH07-B51-RECEIPT-JOIN" :eligibility-receipt-keys)
  (exact-keys! (:freeze receipt)
               #{:state :candidate-commit :candidate-tree
                 :active-record-ref :frozen-at}
               "SH07-B51-RECEIPT-JOIN" :eligibility-freeze-keys)
  (exact-keys! (get-in receipt [:freeze :active-record-ref])
               #{:container :value-path :record-id :transition}
               "SH07-B51-RECEIPT-JOIN" :active-record-ref-keys)
  (exact-keys! (get-in receipt [:freeze :active-record-ref :container])
               #{:commit :tree :parents :path :mode :type :blob-oid
                 :byte-count :raw-sha256}
               "SH07-B51-RECEIPT-JOIN" :active-record-container-keys)
  (exact-keys! (get-in receipt [:freeze :active-record-ref :transition])
               #{:workstream-id :from :to :candidate-commit :candidate-tree
                 :architecture-report-sha256 :history-ordinal}
               "SH07-B51-RECEIPT-JOIN" :active-record-transition-keys)
  (exact-keys! (:review receipt)
               #{:reviewer :candidate-commit :candidate-tree :artifact-path
                 :artifact-raw-sha256 :result :completed-at}
               "SH07-B51-RECEIPT-JOIN" :eligibility-review-keys)
  (validate-source-descriptors! (:source-descriptors receipt))
  (exact-keys! (:pre-masked-root receipt)
               #{:coordinates :preimage-spans :preimage-files
                 :preimage-root}
               "SH07-B51-RECEIPT-JOIN" :pre-masked-root-keys)
  (validate-pre-discharge-envelope!
   content (:pre-evidence-contract-discharge receipt))
  (let [expected-id
        (codec/H
         [:gravity/sh07-b51-evidence-eligibility-receipt-v2
          (dissoc receipt :receipt-id)])]
    (when-not
     (and (= :gravity/sh07-b51-evidence-eligibility-receipt-v2
             (:artifact receipt))
          (= 2 (:schema-version receipt))
          (= content (:content receipt))
          (= :evidence-eligible (:state receipt))
          (= [[:receipt-id]] (:self-id-omits receipt))
          (= nonclaims (:nonclaims receipt))
          (= expected-id (:receipt-id receipt))
          (= :frozen (get-in receipt [:freeze :state]))
          (= [(:commit content) (:tree content)]
             [(get-in receipt [:freeze :candidate-commit])
              (get-in receipt [:freeze :candidate-tree])])
          (= [(:commit content) (:tree content)]
             [(get-in receipt [:review :candidate-commit])
              (get-in receipt [:review :candidate-tree])])
          (= :evidence-eligible (get-in receipt [:review :result]))
          (= "1970-01-01T00:00:00Z"
             (get-in receipt [:review :completed-at]))
          (string? (get-in receipt [:review :reviewer]))
          (not (str/blank? (get-in receipt [:review :reviewer])))
          (safe-relative-path? (get-in receipt [:review :artifact-path]))
          (sha256-id? (get-in receipt [:review :artifact-raw-sha256]))
          (= (codec/H
              [:gravity/sh07-b51-source-descriptors-root-v1
               (:source-descriptors receipt)])
             (:source-descriptors-root receipt))
          (= (:source-descriptors receipt)
             (get-in receipt
                     [:pre-evidence-contract-discharge :evidence :carriers
                      :source-descriptors]))
          (= (:source-descriptors-root receipt)
             (get-in receipt
                     [:pre-evidence-contract-discharge :evidence :carriers
                      :source-descriptors-root]))
          (= (:pre-masked-root receipt)
             (get-in receipt
                     [:pre-evidence-contract-discharge :evidence :carriers
                      :pre-masked-root])))
      (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-receipt)))
  receipt)

(defn validate-eligibility-coordinate!
  "Validate the closed Q external-review coordinate without reading Git.

  The two-argument form additionally binds the coordinate's parent to the
  supplied immutable C/T content.  Git-object replay remains the authority
  for the five observations in `validate-eligibility-object!`."
  ([coordinate]
   (validate-eligibility-coordinate! nil coordinate))
  ([content coordinate]
   (exact-keys! coordinate eligibility-coordinate-keys
                "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate-keys)
   (doseq [[field value] [[:commit (:commit coordinate)]
                          [:tree (:tree coordinate)]
                          [:blob-oid (:blob-oid coordinate)]]]
     (require-git-id! value field "SH07-B51-RECEIPT-JOIN"))
   (when-not
    (and (= :gravity/sh07-b51-external-review-object-v1
            (:schema coordinate))
         (vector? (:parents coordinate))
         (= 1 (count (:parents coordinate)))
         (git-id? (first (:parents coordinate)))
         (or (nil? content)
             (= [(:commit content)] (:parents coordinate)))
         (safe-relative-path? (:path coordinate))
         (= eligibility-receipt-path (:path coordinate))
         (= "100644" (:mode coordinate))
         (= "blob" (:type coordinate))
         (integer? (:byte-count coordinate))
         (not (neg? (:byte-count coordinate)))
         (sha256-id? (:raw-sha256 coordinate))
         (sha256-id? (:receipt-id coordinate)))
     (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate
            {:coordinate coordinate}))
   (when (and content (not= (:commit content) (:commit coordinate)))
     (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-content-parent
            {:content content :coordinate coordinate}))
   coordinate))

(def ^:private eligibility-check-count-values
  {:decode-count 1
   :receipt-id-check-count 1
   :pre-discharge-reconstruction-count 1
   :pre-discharge-git-operation-count 209
   :pre-discharge-document-descriptor-count 33
   :pre-discharge-source-descriptor-count 13
   :pre-discharge-pre-masked-file-count 2
   :pre-discharge-owned-c-artifact-count 160
   :pre-discharge-a9-artifact-count 3
   :pre-discharge-baseline-boot5-count 1
   :pre-discharge-static-decode-count 196})

(def ^:private eligibility-check-match-keys
  [:content-match :source-descriptors-match
   :source-descriptors-root-match :pre-masked-root-match
   :owned-c-root-match :pre-discharge-match])

(def ^:private git-observation-keys
  #{:ordinal :operation :argv :stdin-byte-count :stdin-raw-sha256
    :exit-code :stdout-byte-count :stdout-raw-sha256 :stderr-byte-count
    :stderr-raw-sha256 :parsed-result})
