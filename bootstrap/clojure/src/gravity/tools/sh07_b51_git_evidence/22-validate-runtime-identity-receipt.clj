
(defn validate-runtime-identity-receipt!
  "Validate an identity receipt only after its complete runtime joins exist.

  Existing-object replay intentionally uses the two-argument envelope helper
  above because replay fixtures predate the runtime discharge.  This public
  path is the authority-bearing validation: it requires complete E1
  EVIDENCE-ELIGIBILITY and RUNTIME-CONTRACT-DISCHARGE values, recomputes both
  nested identities, and rejects later-phase or enclosing receipt cycles.

  The context must provide the independently decoded Q eligibility receipt and
  the ordered L/M/A/B participant checks.  This path never invents missing Q
  or participant values."
  ([content receipt]
   (fail! "SH07-B51-RECEIPT-JOIN"
          :runtime-identity-context-required))
  ([content receipt context]
   (validate-identity-receipt-envelope! content receipt)
   (let [allowed-context-keys #{:eligibility-receipt :participant-results}]
     (exact-keys! context allowed-context-keys
                  "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-context-keys)
     (let [eligibility (:evidence-eligibility receipt)
           discharge (:runtime-contract-discharge receipt)
           q-receipt (:eligibility-receipt context)
           participant-results (:participant-results context)]
       (let [events (:coordinator-events receipt)
             children (:child-process-receipts receipt)
             payload-joins (:payload-joins receipt)]
         (when-not (and (vector? events) (= 11 (count events))
                        (every? #(and (map? %) (seq %)) events)
                        (vector? children) (= 2 (count children))
                        (every? #(and (map? %) (seq %)) children)
                        (map? (:launcher-receipt receipt))
                        (seq (:launcher-receipt receipt))
                        (map? (:coordinator-receipt receipt))
                        (seq (:coordinator-receipt receipt)))
           (fail! "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-process-joins))
         (exact-keys! payload-joins
                      #{:checker-identity-raw-sha256s
                        :checker-output-raw-sha256s
                        :certificate-pair-raw-sha256
                        :linked-carrier-raw-sha256}
                      "SH07-B51-RECEIPT-JOIN"
                      :runtime-identity-payload-joins)
         (when-not
          (and (vector? (:checker-identity-raw-sha256s payload-joins))
               (= 2 (count (:checker-identity-raw-sha256s payload-joins)))
               (every? sha256-id? (:checker-identity-raw-sha256s payload-joins))
               (vector? (:checker-output-raw-sha256s payload-joins))
               (= 2 (count (:checker-output-raw-sha256s payload-joins)))
               (every? sha256-id? (:checker-output-raw-sha256s payload-joins))
               (sha256-id? (:certificate-pair-raw-sha256 payload-joins))
               (sha256-id? (:linked-carrier-raw-sha256 payload-joins))
               (= (:coordinator-event-root receipt)
                  (codec/H [:gravity/sh07-b51-coordinator-event-root-v1
                            events])))
           (fail! "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-process-joins))
         (reject-runtime-back-edges! events [:coordinator-events])
         (reject-runtime-back-edges! children [:child-process-receipts])
         (reject-runtime-back-edges! (:launcher-receipt receipt)
                                     [:launcher-receipt])
         (reject-runtime-back-edges! (:coordinator-receipt receipt)
                                     [:coordinator-receipt])
         (reject-runtime-back-edges! payload-joins [:payload-joins]))
       (when-not (and (map? eligibility) (map? discharge))
         (fail! "SH07-B51-RECEIPT-JOIN"
                :runtime-identity-missing-joins))
       (when-not (and (map? q-receipt) (vector? participant-results))
         (fail! "SH07-B51-RECEIPT-JOIN"
                :runtime-identity-missing-q-joins))
       (validate-evidence-eligibility!
        eligibility {:content content
                     :receipt q-receipt
                     :participant-results participant-results})
       (validate-runtime-contract-discharge! discharge)
       (let [carriers (get-in discharge [:evidence :carriers])
             expected-id
             (codec/H
              [:gravity/sh07-b51-checker-process-identity-receipt-id-v1
               (dissoc receipt :receipt-id)])]
         (when-not
          (and (= (:pre-discharge-id eligibility)
                  (:pre-discharge-id receipt))
               (= (:pre-discharge-id eligibility)
                  (get-in discharge [:prior-ref :discharge-id]))
               (= eligibility (:evidence-eligibility carriers))
               (= content (:content carriers))
               (= identity-receipt-normalization
                  (:receipt-id-normalization receipt))
               (= expected-id (:receipt-id receipt)))
           (fail! "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-join))
         ;; The enclosing discharge and its prior-ref are required above, but
         ;; only the non-enclosing carrier values are traversed here.  This
         ;; keeps their required Q prior-ref/discharge IDs legal while making
         ;; E/B/G/P and enclosing identity/discharge back-edges impossible.
         (reject-runtime-back-edges! eligibility [:evidence-eligibility])
         (reject-runtime-back-edges!
          carriers [:runtime-contract-discharge :evidence :carriers])
         receipt)))))

(defn- existing-object-descriptor
  [path row blob]
  {:path path :mode (:mode row) :type (:type row) :blob-oid (:oid row)
   :byte-count (get-in blob [:parsed-result :byte-count])
   :raw-sha256 (get-in blob [:parsed-result :raw-sha256])})

(defn- cat-file-value-and-bytes!
  [repository oid]
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository ["cat-file" "blob" oid])
          :directory repository :allowed-exit-codes #{0}
          :stdout-role :response :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-GIT-RELOAD"})
        bytes (:stdout-bytes result)
        stderr (:stderr-bytes result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-GIT-RELOAD" :git-stderr))
    {:bytes bytes
     :observation
     {:operation :cat-file-blob :argv (:argv result)
      :stdin-byte-count 0 :stdin-raw-sha256 empty-raw-sha256
      :exit-code 0 :stdout-byte-count (alength ^bytes bytes)
      :stdout-raw-sha256 (codec/R bytes)
      :stderr-byte-count 0 :stderr-raw-sha256 empty-raw-sha256
      :parsed-result {:oid oid :byte-count (alength ^bytes bytes)
                      :raw-sha256 (codec/R bytes)
                      :value ((:strict-read-wire transport-api)
                              :response bytes)}}}))

(defn load-existing-evidence-objects!
  "Load E9 from exact E/TE Git objects without accepting or resolving a final
  filesystem path.  The result is storage evidence only."
  [{:keys [repository-root content existing-evidence]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-RELOAD" :content)
  (exact-keys! existing-evidence #{:commit :tree :parents}
               "SH07-B51-GIT-RELOAD" :existing-evidence)
  (doseq [[field value] [[:content-commit (:commit content)]
                         [:content-tree (:tree content)]
                         [:evidence-commit (:commit existing-evidence)]
                         [:evidence-tree (:tree existing-evidence)]]]
    (require-git-id! value field "SH07-B51-GIT-RELOAD"))
  (when-not (= [(:commit content)] (:parents existing-evidence))
    (fail! "SH07-B51-GIT-RELOAD" :existing-parent))
  (let [repository (canonical-directory! repository-root :repository-root
                                         "SH07-B51-GIT-RELOAD")
        c-show (show-operation! repository (:commit content))
        e-show (show-operation! repository (:commit existing-evidence))
        diff (diff-name-status-operation! repository
                                          (:commit content)
                                          (:commit existing-evidence))]
    (when-not
     (and (= content (select-keys (:parsed-result c-show) [:commit :tree]))
          (= existing-evidence (:parsed-result e-show))
          (= (set evidence-paths)
             (set (map :path (get-in diff [:parsed-result :entries]))))
          (= 7 (count (get-in diff [:parsed-result :entries]))))
      (fail! "SH07-B51-GIT-RELOAD" :existing-coordinate-or-delta))
    (let [objects
          (mapv
           (fn [path]
             (let [tree (ls-tree-operation!
                         repository (:commit existing-evidence) [path])
                   row (first (get-in tree [:parsed-result :rows]))]
               (when-not (and (:present row) (= "100644" (:mode row))
                              (= "blob" (:type row)) (git-id? (:oid row)))
                 (fail! "SH07-B51-GIT-RELOAD" :existing-tree-row
                        {:path path :row row}))
               (let [{:keys [bytes observation]}
                     (cat-file-value-and-bytes! repository (:oid row))]
                 {:path path :tree-observation tree
                  :blob-observation observation
                  :descriptor (existing-object-descriptor path row observation)
                  :value (get-in observation [:parsed-result :value])
                  :bytes bytes})))
           evidence-paths)
          values (mapv :value objects)
          receipt (peek values)]
      (validate-identity-receipt-envelope! content receipt)
      {:content content
       :existing-evidence existing-evidence
       :objects objects
       :values values
       :artifact-descriptors (mapv :descriptor objects)
       :observations
       (vec (concat [(assoc c-show :ordinal 0)
                     (assoc e-show :ordinal 1)
                     (assoc diff :ordinal 2)]
                    (mapcat
                     (fn [index object]
                       [(assoc (:tree-observation object)
                               :ordinal (+ 3 (* 2 index)))
                        (assoc (:blob-observation object)
                               :ordinal (+ 4 (* 2 index)))])
                     (range) objects)))
       :final-write-count 0
       :outcome :loaded-existing-objects
       :authority :none})))

(defn verify-existing-object-replay!
  "Compare fresh A/B/M payloads to E's first six W byte vectors and compare the
  freshly constructed normalized receipt ID to E's stored receipt ID.  No
  filesystem publication path is accepted by this API, so final writes are
  structurally impossible."
  [{:keys [fresh-payload-values fresh-identity-receipt-id] :as options}]
  (when-not (and (vector? fresh-payload-values)
                 (= 6 (count fresh-payload-values))
                 (sha256-id? fresh-identity-receipt-id))
    (fail! "SH07-B51-GIT-RELOAD" :fresh-replay-input))
  (let [loaded (load-existing-evidence-objects! options)
        objects (subvec (:objects loaded) 0 6)
        fresh-bytes (mapv codec/W fresh-payload-values)
        payloads-equal
        (every? true?
                (map #(Arrays/equals ^bytes %1 ^bytes (:bytes %2))
                     fresh-bytes objects))
        receipt-id (get-in loaded [:values 6 :receipt-id])]
    (when-not (and payloads-equal (= fresh-identity-receipt-id receipt-id))
      (fail! "SH07-B51-GIT-RELOAD" :existing-semantic-replay
             {:payloads-equal payloads-equal
              :receipt-id-equal (= fresh-identity-receipt-id receipt-id)}))
    (assoc loaded
           :payloads-equal true
           :receipt-id-equal true
           :final-write-count 0
           :outcome :verified-existing
           :authority :none)))

(defn- create-new-bytes!
  [^Path path bytes]
  (Files/write path bytes
               (into-array OpenOption
                           [StandardOpenOption/CREATE_NEW
                            StandardOpenOption/WRITE]))
  path)

(defn- current-directory-state
  [^Path directory]
  (if (and (Files/isDirectory directory no-link-options)
           (not (Files/isSymbolicLink directory)))
    {:file-key (file-key directory "SH07-B51-GIT-PUBLICATION-DELTA")
     :names (list-entry-names! directory
                               "SH07-B51-GIT-PUBLICATION-DELTA")}
    {:file-key nil :names nil}))
