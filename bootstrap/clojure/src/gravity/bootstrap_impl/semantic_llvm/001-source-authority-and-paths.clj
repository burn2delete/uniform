(declare ^:private p15-s23-b3-llvm-file-snapshot!)

(defn p15-s23-c13-c14-b1-path-neutral-value
  "Recursively remove only physical bridge provenance and binding carriers.
  Semantic source/origin data remains and is normalized by the C11 neutralizer."
  [value]
  (p15-s23-c11-mir-path-neutral-value
   (walk/postwalk
    (fn [item]
      (if (map? item)
        (dissoc item
                :actual-path-provenance
                :actual-path-binding-id
                :b1-actual-path-binding-id
                :sealed-b1-actual-path-binding-id
                :actual-paths
                :actual-path-binding-hash)
        item))
    value)))

(defn p15-s23-c13-c14-b1-stage-semantic-input
  [record]
  (p15-s23-c13-c14-b1-path-neutral-value
   (dissoc record :semantic-id :artifact-id)))

(defn p15-s23-c13-c14-b1-stage-semantic-projection
  [record]
  (assoc (p15-s23-c13-c14-b1-stage-semantic-input record)
         :semantic-id (:semantic-id record)
         :artifact-id (:artifact-id record)))

(defn p15-s23-c13-c14-b1-semantic-input
  [packet]
  (p15-s23-c13-c14-b1-path-neutral-value
   (-> packet
       (dissoc :semantic-id :artifact-id)
       (update :c13 p15-s23-c13-c14-b1-stage-semantic-projection)
       (update :c14 p15-s23-c13-c14-b1-stage-semantic-projection)
       (update :b1 p15-s23-c13-c14-b1-stage-semantic-projection)
       (update :optimized-mir
               p15-s23-c13-c14-b1-path-neutral-value))))

(defn p15-s23-c13-c14-b1-reproducible-projection
  [packet]
  {:semantic-id (:semantic-id packet)
   :artifact-id (:artifact-id packet)
   :c13-semantic-id (get-in packet [:c13 :semantic-id])
   :c13-artifact-id (get-in packet [:c13 :artifact-id])
   :c14-request-id (get-in packet [:c14 :request :request-id])
   :c14-semantic-id (get-in packet [:c14 :semantic-id])
   :c14-artifact-id (get-in packet [:c14 :artifact-id])
   :b1-semantic-id (get-in packet [:b1 :semantic-id])
   :b1-artifact-id (get-in packet [:b1 :artifact-id])
   :semantic-input (p15-s23-c13-c14-b1-semantic-input packet)})

(defn p15-s23-c13-c14-b1-contextual-report-record
  [packet]
  (let [base
        {:artifact :gravity/c13-c14-b1-contextual-verification-report
         :schema-version 1
         :status :passed
         :packet-id (:artifact-id packet)
         :semantic-id (:semantic-id packet)
         :fresh-c11-mir-id (get-in packet [:c11 :mir-id])
         :c13-artifact-id (get-in packet [:c13 :artifact-id])
         :c14-request-id (get-in packet [:c14 :request :request-id])
         :c14-artifact-id (get-in packet [:c14 :artifact-id])
         :b1-artifact-id (get-in packet [:b1 :artifact-id])
         :c11 :passed :c13 :passed :c14 :passed :b1 :passed
         :gravity-source-replay :passed
         :independent-reconstruction :passed
         :self-hosted? false}]
    (assoc base :report-id
           (p15-s23-c11-mir-digest
            {:kind :gravity/c13-c14-b1-contextual-verification-report
             :schema-version 1 :report base}))))

(defn- p15-s23-b3-llvm-require-authority!
  [candidate source-path operation]
  (when-not (identical? candidate p15-s23-b3-llvm-finalization-token)
    (p15-s23-b3-llvm-fail!
     "B3-MANIFEST" source-path {}
     {:missing-fact :opaque-authenticated-b3-side-effect-authority
      :bounded-reason operation})))

(defn- p15-s23-b3-llvm-resolve-source-path
  [candidate request-source]
  (p15-s23-b3-llvm-require-authority!
   candidate request-source :resolve-pinned-b3-source)
  (let [relative p15-s23-b3-llvm-source-relative-path
        c11-path (java.io.File. (p15-s23-c11-mir-resolve-source-path))
        repository-root
        (loop [directory (.getParentFile c11-path)]
          (if (or (nil? directory)
                  (.isFile (java.io.File. directory relative)))
            directory
            (recur (.getParentFile directory))))]
    (if repository-root
      (.getPath (java.io.File. repository-root relative))
      relative)))

(defn- p15-s23-b3-llvm-source-binding!
  [candidate request-source]
  (p15-s23-b3-llvm-require-authority!
   candidate request-source :load-pinned-b3-source)
  (let [source-path
        (p15-s23-b3-llvm-resolve-source-path candidate request-source)
        source-file (java.io.File. source-path)
        source-file-path (.toPath source-file)
        source-root (.getParent source-file-path)]
    (when-not source-root
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :pinned-gravity-b3-source}))
    (let [snapshot
          (p15-s23-b3-llvm-file-snapshot!
           candidate source-root source-file-path request-source
           :read-pinned-gravity-b3-source (* 1024 1024))
          source-bytes (:byte-count snapshot)
          source-text
          (String. ^bytes (:bytes snapshot)
                   java.nio.charset.StandardCharsets/UTF_8)
          source-content-hash (str "sha256:" (sha256-hex source-text))]
      (when-not (and (= p15-s23-b3-llvm-source-byte-count source-bytes)
                     (= p15-s23-b3-llvm-expected-source-content-hash
                        source-content-hash))
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" request-source {}
         {:missing-fact :pinned-gravity-b3-source-identity
          :b3-source-content-hash source-content-hash}))
      (let [emitter-rule
            (c-backend-stage2-plan-emitter-source-rule!
             request-source :llvm-x86_64-linux)
            plan
            (p15-s23-stage2-compiler-artifact-plan
             (:emitter emitter-rule) source-path source-text)
            functions (:functions plan)
            observed-shapes
            (into {}
                  (map (fn [[name _]]
                         [name (select-keys (get functions name)
                                            [:arity :params])]))
                  p15-s23-b3-llvm-required-functions)
            plan-hash
            (p15-s23-c11-mir-digest
             (p15-s23-stage2-compiler-artifact-semantic-input plan))
            functions-hash (p15-s23-c11-mir-digest functions)
            builder-hash
            (p15-s23-c11-mir-digest
             (get functions p15-s23-b3-llvm-builder-function))]
        (when-not (and (= observed-shapes
                          p15-s23-b3-llvm-required-functions)
                       (= plan-hash
                          p15-s23-b3-llvm-expected-plan-semantic-hash)
                       (= functions-hash
                          p15-s23-b3-llvm-expected-functions-semantic-hash)
                       (= builder-hash
                          p15-s23-b3-llvm-expected-builder-semantic-hash))
          (p15-s23-b3-llvm-fail!
           "B1-INPUT" request-source {}
           {:missing-fact :pinned-gravity-b3-function-identity
            :b3-source-content-hash source-content-hash}))
        {:source-path source-path
         :source-content-hash source-content-hash
         :source-byte-count source-bytes
         :plan-semantic-hash plan-hash
         :functions-semantic-hash functions-hash
         :builder-semantic-hash builder-hash
         :function-shapes observed-shapes
         :plan plan}))))
