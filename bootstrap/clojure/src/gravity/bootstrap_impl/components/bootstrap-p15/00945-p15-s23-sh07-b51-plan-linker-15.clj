
(defn- p15-s23-sh07-b51-c5-authority-preimage-from-session
  [commit tree descriptors manifest]
  (let [entries (mapv p15-s23-sh07-b51-governed-source-entry
                      (range 3 10))
        content {:commit commit :tree tree}
        content-proof (:content-object-proof (first entries))
        source-descriptors (mapv :descriptor entries)
        module-source-inputs
        (mapv #(select-keys % [:descriptor :source-object-proof]) entries)
        preimage
        {:schema :gravity/sh07-b51-c5-authority-preimage-v1
         :content content
         :content-object-proof content-proof
         :source-descriptors source-descriptors
         :module-source-inputs module-source-inputs
         :module-source-descriptors descriptors
         :link-manifest manifest}]
    (when-not
     (and (= 7 (count entries))
          (every? #(= content-proof (:content-object-proof %)) entries)
          (= source-descriptors
             (subvec (:descriptors
                      (p15-s23-sh07-b51-active-source-session)) 3 10)))
      (p15-s23-sh07-b51-c5-authority-fail!
       :session-preimage-join {}))
    (p15-s23-sh07-b51-validate-c5-authority-preimage! preimage)
    preimage))

(defn- p15-s23-sh07-b51-replay-returned-source-authority-events!
  [m0s link-manifest resolution-plan-authority source-read-receipt]
  (let [events (:events source-read-receipt)
        source-descriptors (:source-descriptors source-read-receipt)]
    (when-not (and (= 7 (count m0s))
                   (= 13 (count source-descriptors))
                   (= 40 (count events)))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-authority-event-replay-shape
       {:m0-count (count m0s)
        :source-descriptor-count (count source-descriptors)
        :event-count (count events)}))
    (doseq [module-ordinal (range 7)]
      (let [event-ordinal (+ 25 module-ordinal)
            event (nth events event-ordinal)
            expected-coordinate
            (nth p15-s23-sh07-b51-source-event-plan event-ordinal)
            m0 (nth m0s module-ordinal)
            source-descriptor
            (nth source-descriptors (+ 3 module-ordinal))
            product
            (p15-s23-sh07-b51-source-authority-product-from-m0-m1
             m0 link-manifest)
            replayed-id
            (p15-s23-sh07-b51-source-event-product-id event product)]
        (when-not
         (and (= expected-coordinate
                 (select-keys event
                              [:ordinal :domain :source-ordinal
                               :module-ordinal :consumer]))
              (= (:module-ordinal m0) module-ordinal)
              (= (:logical-source-path m0) (:path source-descriptor))
              (= (:source-sha256 m0) (:raw-sha256 source-descriptor)
                 (:source-raw-sha256 event))
              (= replayed-id (:product-id event)))
          (p15-s23-sh07-b51-governed-read-fail!
           :source-authority-event-replay
           {:module-ordinal module-ordinal
            :event-ordinal event-ordinal
            :expected-product-id replayed-id
            :actual-product-id (:product-id event)}))))
    (let [event (nth events 39)
          expected-coordinate (nth p15-s23-sh07-b51-source-event-plan 39)
          descriptor (nth source-descriptors 12)
          replayed-id
          (p15-s23-sh07-b51-source-event-product-id
           event resolution-plan-authority)]
      (when-not
       (and (p15-s23-sh07-b51-resolution-plan-authority-valid?
             resolution-plan-authority)
            (= expected-coordinate
               (select-keys event
                            [:ordinal :domain :source-ordinal
                             :module-ordinal :consumer]))
            (= (:raw-sha256 descriptor) (:source-raw-sha256 event))
            (= replayed-id (:product-id event)))
        (p15-s23-sh07-b51-governed-read-fail!
         :resolution-plan-authority-event-replay
         {:expected-product-id replayed-id
          :actual-product-id (:product-id event)})))
    true))

(def ^:private p15-s23-sh07-b51-source-semantic-carrier-keys
  #{:source-descriptors :source-events :l1-document :l1-source-units
    :l1-grammar-change :c2-snapshot :c2-units :c2-source-map
    :c2-diagnostics :c2-literal-records :c2-reader-extensions})

(def ^:private p15-s23-sh07-b51-v9-builder-receipted-product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id
    :source-forms :module-transactions :source-read-receipt
    :sh06-resolution-plan-authority :source-semantic-carrier})

(def ^:private p15-s23-sh07-b51-c2-unit-keys
  #{:ordinal :path :extension :project-relative-path :encoding :source-id
    :bytes-hash :project-root :reader-options :features :source-kind
    :tokens :forms :source-map :diagnostics :literal-records
    :reader-extensions})

(def ^:private p15-s23-sh07-b51-l1-unit-keys
  #{:ordinal :descriptor :source-order :syntax})

(defn- p15-s23-sh07-b51-semantic-member-root
  [kind entries]
  (p15-s23-sh07-b51-canonical-id
   [:gravity/sh07-b51-semantic-witness-member-root-v1
    {:kind kind :entries entries}]))

(defn- p15-s23-sh07-b51-path-neutral-span [span]
  (if (map? span) (dissoc span :source) span))

(defn- p15-s23-sh07-b51-path-neutral-tokens [tokens]
  (mapv #(-> %
             (dissoc :source-path)
             (update :span p15-s23-sh07-b51-path-neutral-span))
        tokens))

(defn- p15-s23-sh07-b51-path-neutral-forms [forms]
  (mapv
   (fn [form]
     (-> form
         (dissoc :source-path)
         (update :span p15-s23-sh07-b51-path-neutral-span)
         (update :surface-span p15-s23-sh07-b51-path-neutral-span)
         (update :origin #(when % (dissoc % :source-path)))
         (update :generated-origin
                 #(mapv (fn [origin]
                          (update origin :from
                                  p15-s23-sh07-b51-path-neutral-span))
                        (or % [])))))
   forms))

(defn- p15-s23-sh07-b51-path-neutral-seeds [seeds]
  (mapv
   (fn [seed]
     (cond-> (update seed :span p15-s23-sh07-b51-path-neutral-span)
       (contains? seed :generated-origin)
       (update :generated-origin
               #(mapv (fn [origin]
                        (cond-> origin
                          (contains? origin :from)
                          (update :from
                                  p15-s23-sh07-b51-path-neutral-span)))
                      %))))
   seeds))

(defn- p15-s23-sh07-b51-path-neutral-extensions [extensions]
  (let [semantic-span #(if (map? %) (dissoc % :source :file) %)]
    (mapv
     (fn [invocation]
       (cond-> (dissoc invocation :source-path)
         (contains? invocation :span) (update :span semantic-span)
         (contains? invocation :invocations)
         (update :invocations
                 #(mapv (fn [record]
                          (cond-> record
                            (contains? record :span)
                            (update :span semantic-span)))
                        %))))
     extensions)))

(defn- p15-s23-sh07-b51-path-neutral-diagnostics [diagnostics]
  (mapv
   (fn [diagnostic]
     (cond-> diagnostic
       (contains? diagnostic :source-span)
       (update :source-span p15-s23-sh07-b51-path-neutral-span)
       (get-in diagnostic [:primary :span])
       (update-in [:primary :span] p15-s23-sh07-b51-path-neutral-span)
       (contains? diagnostic :related)
       (update :related
               #(mapv (fn [related]
                        (cond-> related
                          (contains? related :span)
                          (update :span
                                  p15-s23-sh07-b51-path-neutral-span)))
                      %))
       (contains? diagnostic :origin-chain)
       (update :origin-chain
               #(mapv (fn [origin]
                        (cond-> (dissoc origin :path)
                          (contains? origin :span)
                          (update :span
                                  p15-s23-sh07-b51-path-neutral-span)))
                      %))))
   diagnostics))

(defn- p15-s23-sh07-b51-path-neutral-literal
  [literal]
  (if (contains? literal :span)
    (update literal :span p15-s23-sh07-b51-path-neutral-span)
    literal))

(defn- p15-s23-sh07-b51-unit-descriptor
  [descriptor artifact]
  (let [source-unit (:source-unit-record artifact)
        identity-inputs (:identity-inputs source-unit)
        projected
        {:path (:path descriptor)
         :extension (:extension source-unit)
         :project-relative-path (:project-relative-path source-unit)
         :encoding (:encoding identity-inputs)
         :source-id (:source-id source-unit)
         :bytes-hash (:bytes-hash source-unit)
         :project-root (:project-root source-unit)
         :reader-options (:reader-options source-unit)
         :features (:enabled-features source-unit)
         :source-kind (:source-kind source-unit)}]
    (when-not
     (and (= (:path descriptor) (:project-relative-path source-unit)
             (:project-relative-path identity-inputs))
          (= (:raw-sha256 descriptor) (:bytes-hash source-unit))
          (= (:project-root source-unit) (:project-root-id identity-inputs))
          (= {:path nil :project-root-id (:project-root source-unit)}
             (:project-root-record source-unit)))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-semantic-carrier-unit-descriptor
       {:ordinal (:ordinal descriptor) :path (:path descriptor)}))
    projected))
