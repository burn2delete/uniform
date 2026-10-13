
(defn validate-source-read-receipt!
  "Reject any nonliteral descriptor/event schedule or broken raw/product/root
  join for one finalized checker-local source session. Products for events 0,
  1, 2, and 24-39 remain opaque; their IDs are bound by the exact event root."
  ([role request receipt]
   (validate-source-read-receipt! (:H (load-codec!)) role request receipt))
  ([H role request receipt]
   (let [counters (:counters receipt)
         content (:content request)
         descriptors (:source-descriptors receipt)
         events (:events receipt)
         descriptor-shas (mapv :raw-sha256 descriptors)]
    (when-not
     (and (exact-keys? receipt source-receipt-keys)
          (= :gravity/sh07-b51-source-read-receipt-v3 (:schema receipt))
          (= role (:role receipt))
          (= (:commit content) (:commit receipt))
          (= (:tree content) (:tree receipt))
          (vector? descriptors)
          (= 13 (count descriptors))
          (every? true?
                  (map (fn [spec descriptor]
                         (and (exact-keys? descriptor source-descriptor-keys)
                              (= spec (select-keys descriptor
                                                   [:ordinal :kind
                                                    :module-ordinal :path]))
                              (= "100644" (:mode descriptor))
                              (git-id? (:blob-oid descriptor))
                              (integer? (:byte-count descriptor))
                              (not (neg? (:byte-count descriptor)))
                              (sha256-id? (:raw-sha256 descriptor))))
                       source-specs descriptors))
          (= (H [:gravity/sh07-b51-source-descriptors-root-v1 descriptors])
             (:source-descriptors-root receipt))
          (vector? events)
          (= 40 (count events))
          (every? true?
                  (map (fn [plan event]
                         (let [source-ordinal (:source-ordinal event)
                               product (recomputable-event-product event)
                               product-id
                               (when (some? product)
                                 (H [:gravity/sh07-b51-source-event-product-id-v1
                                     {:ordinal (:ordinal event)
                                      :domain (:domain event)
                                      :consumer (:consumer event)
                                      :source-raw-sha256
                                      (:source-raw-sha256 event)
                                      :product product}]))]
                           (and (exact-keys? event source-event-keys)
                                (= plan (select-keys event
                                                     [:ordinal :domain
                                                      :source-ordinal
                                                      :module-ordinal
                                                      :consumer]))
                                (= (nth descriptor-shas source-ordinal nil)
                                   (:source-raw-sha256 event))
                                (sha256-id? (:product-id event))
                                (or (nil? product-id)
                                    (= product-id (:product-id event))))))
                       source-event-plan events))
          (= (H [:gravity/sh07-b51-source-event-root-v1 events])
             (:event-root receipt))
          (exact-keys? counters source-counter-keys)
          (= {:acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}
             counters)
          (true? (:finalized receipt)))
      (reject! :source-session {:role role}))
    receipt)))

(defn validate-receipted-products!
  "Validate the exact ten-key E3 builder return and its same-session semantic
  carrier before either selected checker receives the receipt-free handoff."
  ([role request products]
   (validate-receipted-products! (:H (load-codec!)) role request products))
  ([H role request products]
   (validate-source-read-receipt! H role request
                                  (:source-read-receipt products))
   (when-not
    (and (exact-keys? products product-keys)
         (vector? (:m0s products)) (= 7 (count (:m0s products)))
         (map? (:m1 products))
         (vector? (:m2s products)) (= 7 (count (:m2s products)))
         (map? (:bundle-core products))
         (sha256-id? (:execution-bundle-core-id products))
         (vector? (:source-forms products))
         (= 7 (count (:source-forms products)))
         (vector? (:module-transactions products))
         (= 7 (count (:module-transactions products)))
         (map? (:sh06-resolution-plan-authority products))
         (map? (:source-semantic-carrier products)))
     (reject! :source-session {:role role}))
   (let [carrier-valid?
         (resolve-public!
          bootstrap-namespace
          "p15-s23-sh07-b51-source-semantic-carrier-valid?")
         content (:content request)]
     (when-not
      (carrier-valid? content (:source-read-receipt products)
                      (:module-transactions products)
                      (:source-semantic-carrier products))
       (reject! :source-semantic-carrier {:role role})))
   products))

(defn- source-session-id [H receipt]
  (H [:gravity/sh07-b51-governed-source-session-id-v1
      {:role (:role receipt)
       :commit (:commit receipt)
       :tree (:tree receipt)
       :source-descriptors-root (:source-descriptors-root receipt)
       :event-root (:event-root receipt)
       :counters (:counters receipt)}]))

(defn- checker-provenance
  [H role request checker-manifest]
  (let [spec (get role-specs role)
        snapshot (:content request)
        checker (:checker checker-manifest)
        dependency-ids
        {:local-source-record-ids
         (mapv #(H [:gravity/sh07-b51-materializer-local-source-v1
                    {:schema-version 1 :record %}])
               (get-in checker-manifest
                       [:dependencies :local-source-records]))
         :external-runtime-record-ids
         (mapv #(H [:gravity/sh07-b51-materializer-external-runtime-v1
                    {:schema-version 1 :record %}])
               (get-in checker-manifest
                       [:dependencies :external-runtime-records]))}
        tool-domain
        (case role
          :producer-census
          :gravity/sh07-b51-producer-census-materializer-tool-closure-id-v2
          :independent-verifier-census
          :gravity/sh07-b51-independent-census-materializer-tool-closure-id-v2)
        role-name (case role
                    :producer-census :producer
                    :independent-verifier-census :independent)]
    {:invocation-id
     (H [:gravity/sh07-b51-attempt32-checker-invocation-id-v1
         {:schema-version 1 :role role-name :snapshot snapshot
          :checker-executable-id (:executable-id checker)
          :completed-at completed-at
          :evidence-relative-root evidence-relative-root}])
     :tool-closure-id
     (H [tool-domain
         {:schema-version 2 :role role
          :checker-identity-manifest-id
          (:identity-manifest-id checker-manifest)
          :materializer-dependency-commitments dependency-ids}])
     :reproduction-command-id
     (H [:gravity/sh07-b51-attempt32-tier0-reproduction-command-id-v1
         {:schema-version 1 :snapshot snapshot :completed-at completed-at
          :evidence-relative-root evidence-relative-root
          :main 'gravity.tools.sh07-b51-attempt32-checker-child}])
     :evidence-relative-path
     (str evidence-relative-root "/" (:output-name spec))
     :completed-at completed-at}))

(defn- receipted-product-emitter-identities!
  [products]
  (let [plans (:m2s products)
        source-ids (mapv #(get-in % [:compiler :source-content-hash]) plans)
        semantic-ids (mapv #(get-in % [:compiler :semantic-hash]) plans)
        source-id (first source-ids)
        semantic-id (first semantic-ids)]
    (when-not
     (and (vector? plans) (= 7 (count plans))
          (sha256-id? source-id) (sha256-id? semantic-id)
          (= (vec (repeat 7 source-id)) source-ids)
          (= (vec (repeat 7 semantic-id)) semantic-ids))
      (reject! :emitter-identity-join
               {:plan-count (when (vector? plans) (count plans))
                :source-id-count (count (set source-ids))
                :semantic-id-count (count (set semantic-ids))}))
    {:emitter-source-id source-id
     :emitter-semantic-id semantic-id}))

(defn semantic-algorithm-source-authority!
  "Select the seven E3 algorithm rows from the already reconstructed PRE
  snapshot and join them to the same C-bound classpath source objects. This
  performs no Git operation, strict decode, or governed source acquisition."
  [H preflight]
  (let [owned (get-in preflight [:pre-object-snapshot
                                 :owned-c-descriptors])
        objects (get-in preflight [:source-authority :source-objects])
        by-path (group-by :path owned)
        descriptors
        (mapv
         (fn [ordinal path]
           (let [matches (get by-path path)
                 owned-row (first matches)
                 object-row (get objects path)
                 base (select-keys owned-row
                                   [:path :mode :blob-oid :byte-count
                                    :raw-sha256])]
             (when-not
              (and (= 1 (count matches))
                   (= base
                      (select-keys object-row
                                   [:path :mode :blob-oid :byte-count
                                    :raw-sha256]))
                   (bytes? (:bytes object-row))
                   (= (:byte-count object-row)
                      (alength ^bytes (:bytes object-row))))
               (reject! :semantic-algorithm-source-authority {:path path}))
             (assoc base :ordinal ordinal)))
         (range 7) semantic-algorithm-source-paths-e3)
        root (H [:gravity/sh07-b51-semantic-algorithm-source-root-v1
                 descriptors])]
    {:descriptors descriptors :root root}))

(defn build-checker-envelope
  "Build the semantic checker input only inside the selected child process."
  [H role request products checker-manifest preflight]
  (let [m0s (:m0s products)
        {:keys [emitter-source-id emitter-semantic-id]}
        (receipted-product-emitter-identities! products)
        algorithm (semantic-algorithm-source-authority! H preflight)]
    {:architecture-report-sha256
     (get-in request [:evidence :architecture-report-sha256])
     :snapshot-commit (get-in request [:content :commit])
     :snapshot-tree (get-in request [:content :tree])
     :predecessor-executable-descriptor-id
     predecessor-executable-descriptor-id
     :module-source-identities (mapv :source-sha256 m0s)
     :module-source-byte-counts (mapv :source-byte-count m0s)
     :emitter-source-id emitter-source-id
     :emitter-semantic-id emitter-semantic-id
     :m0s m0s
     :m1 (:m1 products)
     :m2s (:m2s products)
     :bundle-core (:bundle-core products)
     :source-forms (:source-forms products)
     :module-transactions (:module-transactions products)
     :sh06-resolution-plan-authority
     (:sh06-resolution-plan-authority products)
     :source-semantic-carrier (:source-semantic-carrier products)
     :semantic-algorithm-source-descriptors (:descriptors algorithm)
     :semantic-algorithm-source-root (:root algorithm)
     :checker (:checker checker-manifest)
     :provenance
     (checker-provenance H role request checker-manifest)}))
