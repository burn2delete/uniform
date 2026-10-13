
(defn validate-source-object-proof!
  "Purely validate a complete Git tree-inclusion chain and leaf blob bytes."
  [content descriptor proof]
  (exact-keys! content #{:commit :tree} git-object-proof-id :content)
  (exact-keys! proof source-object-proof-keys
               git-object-proof-id :source-object-proof-keys)
  (when-not (and (map? descriptor)
                 (safe-relative-path? (:path descriptor))
                 (= "100644" (:mode descriptor))
                 (git-id? (:blob-oid descriptor))
                 (integer? (:byte-count descriptor))
                 (not (neg? (:byte-count descriptor)))
                 (sha256-id? (:raw-sha256 descriptor)))
    (fail! git-object-proof-id :source-descriptor))
  (let [components (str/split (:path descriptor) #"/" -1)
        chain (:tree-chain proof)
        blob-bytes (unsigned-vector->bytes! (:blob-bytes proof)
                                            :blob-byte-vector)]
    (when-not (and (= :gravity/sh07-b51-git-tree-inclusion-proof-v1
                      (:schema proof))
                   (= (:path descriptor) (:path proof))
                   (= (:tree content) (:root-tree proof))
                   (git-id? (:root-tree proof))
                   (vector? chain)
                   (= (count components) (count chain)))
      (fail! git-object-proof-id :source-object-coordinate))
    (doseq [[ordinal component row] (map vector (range) components chain)]
      (exact-keys! row tree-chain-row-keys
                   git-object-proof-id :tree-chain-row-keys)
      (require-git-id! (:tree-oid row) :tree-oid git-object-proof-id)
      (require-git-id! (:selected-oid row) :selected-oid git-object-proof-id)
      (let [tree-bytes (unsigned-vector->bytes! (:tree-bytes row)
                                                :tree-byte-vector)
            entries (parse-git-tree-bytes! (:tree-bytes row))
            selected (filter #(= component (:name %)) entries)
            final? (= ordinal (dec (count components)))
            next-row (nth chain (inc ordinal) nil)]
        (when-not (and (= component (:component row))
                       (= (if (zero? ordinal) (:tree content)
                              (:selected-oid (nth chain (dec ordinal))))
                          (:tree-oid row))
                       (= (:tree-oid row) (git-object-id "tree" tree-bytes))
                       (= 1 (count selected))
                       (= [(:mode (first selected)) (:oid (first selected))]
                          [(:selected-mode row) (:selected-oid row)])
                       (if final?
                         (and (= "100644" (:selected-mode row))
                              (= (:blob-oid descriptor) (:selected-oid row)))
                         (and (= "40000" (:selected-mode row))
                              (= (:selected-oid row) (:tree-oid next-row)))))
          (fail! git-object-proof-id :tree-chain-coordinate
                 {:ordinal ordinal :component component}))))
    (when-not (and (= (:blob-oid descriptor)
                      (git-object-id "blob" blob-bytes))
                   (= (:byte-count descriptor) (alength ^bytes blob-bytes))
                   (= (:raw-sha256 descriptor) (codec/R blob-bytes)))
      (fail! git-object-proof-id :blob-coordinate)))
  proof)

(defn validate-governed-source-entry!
  "Purely validate one exact five-key E2 source-session entry."
  [content entry]
  (exact-keys! entry
               #{:descriptor :bytes :text :content-object-proof
                 :source-object-proof}
               git-object-proof-id :source-entry-keys)
  (let [descriptor (:descriptor entry)
        bytes (unsigned-vector->bytes! (:bytes entry) :source-entry-bytes)]
    (validate-content-object-proof! content (:content-object-proof entry))
    (validate-source-object-proof! content descriptor
                                   (:source-object-proof entry))
    (when-not (and (= (:bytes entry)
                      (get-in entry [:source-object-proof :blob-bytes]))
                   (= (:byte-count descriptor) (alength ^bytes bytes))
                   (= (:raw-sha256 descriptor) (codec/R bytes))
                   (= (:text entry)
                      (decode-utf8! bytes git-object-proof-id
                                    :source-entry-utf8)))
      (fail! git-object-proof-id :source-entry-coordinate)))
  entry)

(defn build-source-session-aggregate!
  "Join the independently observed A/B/M E2 session counters exactly."
  [participant-counters]
  (when-not (and (map? participant-counters)
                 (= #{:a :b :m} (set (keys participant-counters)))
                 (every? #(= source-session-counters-e2
                             (get participant-counters %))
                         [:a :b :m]))
    (fail! git-object-proof-id :source-session-counters
           {:participant-counters participant-counters}))
  (let [sessions
        (mapv (fn [participant]
                (assoc (get participant-counters participant)
                       :participant participant))
              [:a :b :m])
        aggregate
        (merge {:sessions sessions}
               (into {}
                     (map (fn [key]
                            [key (reduce + (map key sessions))])
                          [:acquire-count :decode-count
                           :event-count :finalize-count])))]
    (when-not (= source-aggregate-counters-e2
                 (dissoc aggregate :sessions))
      (fail! git-object-proof-id :source-session-aggregate))
    aggregate))

(defn- cat-file-object-bytes!
  [repository kind oid]
  (when-not (contains? #{"blob" "commit" "tree"} kind)
    (fail! git-object-proof-id :object-kind {:kind kind}))
  (require-git-id! oid :oid git-object-proof-id)
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository ["cat-file" kind oid])
          :directory repository
          :allowed-exit-codes #{0}
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id git-object-proof-id})]
    (when-not (zero? (alength ^bytes (:stderr-bytes result)))
      (fail! git-object-proof-id :git-stderr
             {:kind kind :oid oid
              :stderr-raw-sha256 (codec/R (:stderr-bytes result))}))
    (:stdout-bytes result)))

(defn build-content-object-proof!
  "Acquire a complete raw commit payload and return its validated C proof."
  [{:keys [repository-root content]}]
  (let [repository (canonical-directory! repository-root :repository-root
                                         git-object-proof-id)
        raw (cat-file-object-bytes! repository "commit" (:commit content))
        proof {:oid (:commit content) :raw-bytes (bytes->unsigned-vector raw)}]
    (validate-content-object-proof! content proof)))

(defn build-source-object-proof!
  "Acquire and validate the complete tree chain and blob for DESCRIPTOR."
  [{:keys [repository-root content descriptor]}]
  (let [repository (canonical-directory! repository-root :repository-root
                                         git-object-proof-id)
        components (str/split (:path descriptor) #"/" -1)
        chain
        (loop [tree-oid (:tree content) remaining components rows []]
          (let [tree-raw (cat-file-object-bytes! repository "tree" tree-oid)
                tree-vector (bytes->unsigned-vector tree-raw)
                component (first remaining)
                matches (filter #(= component (:name %))
                                (parse-git-tree-bytes! tree-vector))]
            (when-not (= 1 (count matches))
              (fail! git-object-proof-id :tree-component
                     {:component component :tree-oid tree-oid}))
            (let [selected (first matches)
                  row {:tree-oid tree-oid :tree-bytes tree-vector
                       :component component :selected-mode (:mode selected)
                       :selected-oid (:oid selected)}]
              (if (next remaining)
                (do
                  (when-not (= "40000" (:mode selected))
                    (fail! git-object-proof-id :tree-component-mode
                           {:component component :mode (:mode selected)}))
                  (recur (:oid selected) (next remaining) (conj rows row)))
                (conj rows row)))))
        blob-raw (cat-file-object-bytes! repository "blob"
                                         (:selected-oid (peek chain)))
        proof {:schema :gravity/sh07-b51-git-tree-inclusion-proof-v1
               :path (:path descriptor)
               :root-tree (:tree content)
               :tree-chain chain
               :blob-bytes (bytes->unsigned-vector blob-raw)}]
    (validate-source-object-proof! content descriptor proof)))

(defn build-governed-source-entry!
  "Acquire one complete E2 source entry from immutable Git objects only."
  [{:keys [repository-root content descriptor]}]
  (let [content-proof
        (build-content-object-proof!
         {:repository-root repository-root :content content})
        source-proof
        (build-source-object-proof!
         {:repository-root repository-root :content content
          :descriptor descriptor})
        bytes (:blob-bytes source-proof)
        raw (unsigned-vector->bytes! bytes :source-entry-bytes)
        entry {:descriptor descriptor
               :bytes bytes
               :text (decode-utf8! raw git-object-proof-id
                                   :source-entry-utf8)
               :content-object-proof content-proof
               :source-object-proof source-proof}]
    (validate-governed-source-entry! content entry)))

(def ^:private document-binding-ids
  (mapv
   (fn [[document-ordinal _]]
     (->> row-plan
          (filter #(some #{document-ordinal} (:document-ordinals %)))
          (mapcat :binding-specs)
          (mapv :id)))
   document-paths))

(defn- require-present-blob-row!
  [row path id]
  (when-not (= {:path path :present true :mode "100644" :type "blob"}
               (select-keys row [:path :present :mode :type]))
    (fail! id :pre-c-path
           {:path path :row row}))
  (require-git-id! (:oid row) :blob-oid id)
  row)

(defn- blob-descriptor
  [ordinal path row bytes]
  {:ordinal ordinal
   :path path
   :mode "100644"
   :blob-oid (:oid row)
   :byte-count (alength ^bytes bytes)
   :raw-sha256 (codec/R bytes)})

(defn- build-document-read-receipt
  [content rows-by-path bytes-by-path]
  (let [documents
        (mapv (fn [[ordinal path]]
                (blob-descriptor ordinal path (get rows-by-path path)
                                 (get bytes-by-path path)))
              document-paths)
        events
        (mapv (fn [{:keys [ordinal path raw-sha256]} binding-ids]
                {:ordinal ordinal
                 :document-ordinal ordinal
                 :path path
                 :raw-sha256 raw-sha256
                 :binding-ids binding-ids})
              documents document-binding-ids)]
    {:schema :gravity/sh07-b51-contract-read-receipt-v1
     :content content
     :documents documents
     :events events
     :event-root
     (codec/H [:gravity/sh07-b51-contract-document-event-root-v1 events])
     :counters {:acquire-count 33 :decode-count 33
                :event-count 33 :finalize-count 1}
     :finalized true}))
