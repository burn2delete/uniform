

(defn sh04-syntax-registered-literal-projection-authentic?
  [c2-view gravity-boundary]
  (try
    (let [projection (:registered-literal-projection c2-view)
          bindings (:bindings projection)
          source-path (get-in c2-view [:source-unit-record :path])
          expected-projection-id
          (p15-s23-c6c10-canonical-digest
           source-path
           {:domain :gravity/sh04-registered-literal-projection-v1
            :bindings bindings})
          forms-by-id
          (into {} (map (juxt :form-id identity)) (:form-tree c2-view))
          literals-by-form
          (group-by :form-id (:literal-decoding-records c2-view))
          invocations-by-form
          (group-by
           :form-id
           (for [extension
                 (:reader-extension-invocation-records c2-view)
                 invocation (:invocations extension)]
             (assoc invocation :tag (:tag extension))))
          tagged-form-ids
          (set
           (map :form-id
                (filter #(and (= :tagged-literal (:kind %))
                              (contains? #{'inst 'uuid} (:tag %)))
                        (:form-tree c2-view))))
          binding-form-ids (set (map :form-id bindings))
          provenance
          (:reader-authentication-provenance gravity-boundary)
          carrier-validation
          (p15-s23-trusted-carrier-validation
           (sh04-syntax-strip-host-metadata c2-view)
           :default-only 1048576 256 4096)]
      (and
       (= #{:artifact :schema-version :projection-id :bindings
            :upstream-artifact-id :upstream-integrity-hash
            :upstream-product-binding :reader-binding
            :reader-source-revision}
          (set (keys projection)))
       (= :gravity/sh04-registered-literal-projection
          (:artifact projection))
       (= 1 (:schema-version projection))
       (= expected-projection-id (:projection-id projection))
       (= :passed (:status carrier-validation))
       (= tagged-form-ids binding-form-ids)
       (= (count bindings) (count binding-form-ids))
       (every?
        (fn [{:keys [form-id literal-id tag raw payload descriptor]}]
          (let [form-record (get forms-by-id form-id)
                payload-record
                (get forms-by-id (first (:children form-record)))
                literal-records (get literals-by-form form-id [])
                invocation-records (get invocations-by-form form-id [])
                literal-record (first literal-records)
                invocation-record (first invocation-records)]
            (and (= :tagged-literal (:kind form-record))
                 (= tag (:tag form-record)
                    (get-in literal-record [:facts :tag])
                    (:tag invocation-record))
                 (= 1 (count (:children form-record)))
                 (= :string (:kind payload-record))
                 (= payload (:value payload-record))
                 (= 1 (count literal-records))
                 (= literal-id (:literal-id literal-record))
                 (= 1 (count invocation-records))
                 (= raw (:raw form-record) (:raw literal-record)
                    (:raw invocation-record))
                 (= descriptor (:value form-record)
                    (:decoded literal-record)))))
        bindings)
       (= (:upstream-artifact-id projection)
          (:actual-c2-artifact-id provenance))
       (= (:upstream-integrity-hash projection)
          (:actual-reader-product-integrity-hash provenance))
       (= (:upstream-product-binding projection)
          (:actual-sh03-semantic-product-binding provenance))
       (= (:reader-binding projection)
          (:reader-semantic-binding gravity-boundary))
       (= (:reader-source-revision projection)
          (:reader-source-revision gravity-boundary))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))