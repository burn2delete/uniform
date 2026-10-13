
(defn- p15-s23-sh07-b51-c2-unit
  [descriptor artifact]
  (let [ordinal (:ordinal descriptor)
        token-entries
        (p15-s23-sh07-b51-path-neutral-tokens (:token-stream artifact))
        form-entries
        (p15-s23-sh07-b51-path-neutral-forms (:form-tree artifact))
        source-map-entries
        (mapv #(update % :span p15-s23-sh07-b51-path-neutral-span)
              (:reader-source-map artifact))
        diagnostic-entries
        (p15-s23-sh07-b51-path-neutral-diagnostics
         (:reader-diagnostics artifact))
        literal-records
        (mapv p15-s23-sh07-b51-path-neutral-literal
              (:literal-decoding-records artifact))
        reader-extensions
        (p15-s23-sh07-b51-path-neutral-extensions
         (:reader-extension-invocation-records artifact))]
    (merge
     {:ordinal ordinal}
     (p15-s23-sh07-b51-unit-descriptor descriptor artifact)
     {:tokens {:entries token-entries
               :root (p15-s23-sh07-b51-semantic-member-root
                      :c2-tokens token-entries)}
      :forms {:entries form-entries
              :root (p15-s23-sh07-b51-semantic-member-root
                     :c2-forms form-entries)}
      :source-map {:entries source-map-entries
                   :root (p15-s23-sh07-b51-semantic-member-root
                          :c2-source-map source-map-entries)}
      :diagnostics {:entries diagnostic-entries
                    :root (p15-s23-sh07-b51-semantic-member-root
                           :c2-diagnostics diagnostic-entries)}
      :literal-records literal-records
      :reader-extensions reader-extensions})))

(defn- p15-s23-sh07-b51-l1-unit
  [descriptor artifact c2-unit]
  (let [entries (p15-s23-sh07-b51-path-neutral-seeds
                 (:syntax-seed-stream artifact))]
    {:ordinal (:ordinal descriptor)
     :descriptor (select-keys
                  c2-unit
                  [:path :extension :project-relative-path :encoding
                   :source-id :bytes-hash :project-root :reader-options
                   :features :source-kind])
     :source-order (mapv :form-id (:syntax-seed-stream artifact))
     :syntax {:entries entries
              :root (p15-s23-sh07-b51-semantic-member-root
                     :l1-syntax entries)}}))

(defn- p15-s23-sh07-b51-source-semantic-carrier-from-session!
  [commit tree module-transactions]
  (let [state (p15-s23-sh07-b51-active-source-session)
        descriptors (:descriptors state)
        events @(:events state)
        artifacts
        (mapv
         (fn [ordinal]
           (if (<= 3 ordinal 9)
             (:c2-reader-artifact
              (nth module-transactions (- ordinal 3)))
             (let [entry (nth (:entries state) ordinal)
                   descriptor (:descriptor entry)]
               (compiler-c2-reader-source-artifact
                (:path descriptor) (:text entry)
                (p15-s23-sh07-b51-project-context
                 commit tree (:path descriptor))))))
         (range 2 12))
        c2-units (mapv p15-s23-sh07-b51-c2-unit
                       (subvec descriptors 2 12) artifacts)
        l1-units (mapv p15-s23-sh07-b51-l1-unit
                       (subvec descriptors 2 12) artifacts c2-units)
        source-map-entries (vec (mapcat #(get-in % [:source-map :entries])
                                        c2-units))
        diagnostic-entries
        (vec (mapcat #(get-in % [:diagnostics :entries]) c2-units))
        carrier
        {:source-descriptors descriptors
         :source-events events
         :l1-document (first descriptors)
         :l1-source-units l1-units
         :l1-grammar-change :none
         :c2-snapshot
         {:commit commit :tree tree
          :source-descriptors-root (:descriptors-root state)
          :event-root
          (p15-s23-sh07-b51-canonical-id
           [:gravity/sh07-b51-source-event-root-v1 events])}
         :c2-units c2-units
         :c2-source-map
         {:entries source-map-entries
          :root (p15-s23-sh07-b51-semantic-member-root
                 :c2-source-map source-map-entries)}
         :c2-diagnostics
         {:entries diagnostic-entries
          :root (p15-s23-sh07-b51-semantic-member-root
                 :c2-diagnostics diagnostic-entries)}
         :c2-literal-records
         (vec (mapcat :literal-records c2-units))
         :c2-reader-extensions
         (vec (mapcat :reader-extensions c2-units))}]
    (when-not (= 40 @(:cursor state) (count events))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-semantic-carrier-event-census
       {:cursor @(:cursor state) :event-count (count events)}))
    carrier))

(defn p15-s23-sh07-b51-source-semantic-carrier-valid?
  "Validate the complete receipt-free E3 source carrier against one finalized
  checker-local source receipt and its seven authenticated transactions."
  [content source-read-receipt module-transactions carrier]
  (try
    (let [descriptors (:source-descriptors source-read-receipt)
          events (:events source-read-receipt)
          c2-units (:c2-units carrier)
          l1-units (:l1-source-units carrier)
          source-map-entries
          (vec (mapcat #(get-in % [:source-map :entries]) c2-units))
          diagnostic-entries
          (vec (mapcat #(get-in % [:diagnostics :entries]) c2-units))]
      (boolean
       (and
        (p15-s23-sh07-b51-exact-keys?
         carrier p15-s23-sh07-b51-source-semantic-carrier-keys)
        (= 13 (count descriptors))
        (= 40 (count events))
        (= descriptors (:source-descriptors carrier))
        (= events (:source-events carrier))
        (= (first descriptors) (:l1-document carrier))
        (= :none (:l1-grammar-change carrier))
        (= 10 (count c2-units) (count l1-units))
        (= (vec (range 2 12)) (mapv :ordinal c2-units)
           (mapv :ordinal l1-units))
        (every? #(p15-s23-sh07-b51-exact-keys?
                  % p15-s23-sh07-b51-c2-unit-keys)
                c2-units)
        (every? #(p15-s23-sh07-b51-exact-keys?
                  % p15-s23-sh07-b51-l1-unit-keys)
                l1-units)
        (= {:commit (:commit content) :tree (:tree content)
            :source-descriptors-root
            (:source-descriptors-root source-read-receipt)
            :event-root (:event-root source-read-receipt)}
           (:c2-snapshot carrier))
        (= (:source-descriptors-root source-read-receipt)
           (p15-s23-sh07-b51-canonical-id
            [:gravity/sh07-b51-source-descriptors-root-v1 descriptors]))
        (= (:event-root source-read-receipt)
           (p15-s23-sh07-b51-canonical-id
            [:gravity/sh07-b51-source-event-root-v1 events]))
        (= (vec (repeat 10 true))
           (mapv
          (fn [descriptor c2-unit l1-unit]
            (let [unit-descriptor
                  (select-keys
                   c2-unit
                   [:path :extension :project-relative-path :encoding
                    :source-id :bytes-hash :project-root :reader-options
                    :features :source-kind])]
              (and (= (:path descriptor) (:path c2-unit)
                      (:project-relative-path c2-unit))
                   (= (:raw-sha256 descriptor) (:bytes-hash c2-unit))
                   (= (:project-root-id
                       (p15-s23-sh07-b51-project-context
                        (:commit content) (:tree content) (:path descriptor)))
                      (:project-root c2-unit))
                   (= unit-descriptor (:descriptor l1-unit))
                   (= (:source-order l1-unit)
                      (mapv :form-id (get-in l1-unit [:syntax :entries])))
                   (= (get-in l1-unit [:syntax :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :l1-syntax (get-in l1-unit [:syntax :entries])))
                   (= (get-in c2-unit [:tokens :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-tokens (get-in c2-unit [:tokens :entries])))
                   (= (get-in c2-unit [:forms :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-forms (get-in c2-unit [:forms :entries])))
                   (= (get-in c2-unit [:source-map :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-source-map
                       (get-in c2-unit [:source-map :entries])))
                   (= (get-in c2-unit [:diagnostics :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-diagnostics
                       (get-in c2-unit [:diagnostics :entries]))))))
            (subvec descriptors 2 12) c2-units l1-units))
        (= source-map-entries
           (get-in carrier [:c2-source-map :entries]))
        (= (p15-s23-sh07-b51-semantic-member-root
            :c2-source-map source-map-entries)
           (get-in carrier [:c2-source-map :root]))
        (= diagnostic-entries
           (get-in carrier [:c2-diagnostics :entries]))
        (= (p15-s23-sh07-b51-semantic-member-root
            :c2-diagnostics diagnostic-entries)
           (get-in carrier [:c2-diagnostics :root]))
        (= (vec (mapcat :literal-records c2-units))
           (:c2-literal-records carrier))
        (= (vec (mapcat :reader-extensions c2-units))
           (:c2-reader-extensions carrier))
        (= 7 (count module-transactions))
        (= (vec (repeat 7 true))
           (mapv
          (fn [module-ordinal transaction]
            (= (nth c2-units (inc module-ordinal))
               (p15-s23-sh07-b51-c2-unit
                (nth descriptors (+ 3 module-ordinal))
                (:c2-reader-artifact transaction))))
            (range 7) module-transactions)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))
