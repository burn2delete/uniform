

(declare p15-s23-b2-c17-source-relative-path
         p15-s23-b2-c17-source-byte-count
         p15-s23-b2-c17-expected-source-content-hash
         p15-s23-b2-c17-expected-plan-semantic-hash
         p15-s23-b2-c17-expected-functions-semantic-hash
         p15-s23-b2-c17-expected-builder-semantic-hash
         p15-s23-b2-c17-builder-function
         p15-s23-b2-c17-required-functions
         p15-s23-b4-wasm-expected-source-content-hash)

(def p15-s23-c13-c14-b1-max-carrier-nodes 65536)
(def p15-s23-c13-c14-b1-max-carrier-depth 512)

(defn p15-s23-c13-c14-b1-source-rule
  [owner binding builder]
  {:artifact :gravity/pinned-gravity-source-rule
   :owner owner
   :source-content-hash (:source-content-hash binding)
   :source-byte-count (:source-byte-count binding)
   :plan-semantic-hash (:plan-semantic-hash binding)
   :functions-semantic-hash (:functions-semantic-hash binding)
   :builder-function builder
   :builder-semantic-hash (:builder-semantic-hash binding)
   :function-shapes (:function-shapes binding)
   :semantic-authority :gravity-source
   :compiled-by :clojure-stage0-seed
   :executed-by :clojure-stage0-rule-runner
   :self-hosted? false})

(defn p15-s23-c13-c14-b1-sidecar-evidence!
  [artifact]
  (let [packet (:c13-c14-b1-packet artifact)
        c13 (:c13 packet)
        c14 (:c14 packet)
        b1 (:b1 packet)
        projection
        (p15-s23-c13-c14-b1-reproducible-projection packet)
        packet-semantic-id
        (p15-s23-c11-mir-digest (:semantic-input projection))
        packet-artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:kind packet)
          :schema-version (:schema-version packet)
          :semantic-id packet-semantic-id})
        c13-semantic-id
        (p15-s23-c11-mir-digest
         {:kind (:artifact c13)
          :record (p15-s23-c13-c14-b1-stage-semantic-input c13)})
        c13-artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:artifact c13)
          :schema-version (:schema-version c13)
          :semantic-id c13-semantic-id})
        c14-request-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/c14-bounded-llvm-lowering-request
          :request (dissoc (:request c14) :request-id)})
        c14-semantic-id
        (p15-s23-c11-mir-digest
         {:kind (:artifact c14)
          :record (p15-s23-c13-c14-b1-stage-semantic-input c14)})
        c14-artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:artifact c14)
          :schema-version (:schema-version c14)
          :semantic-id c14-semantic-id})
        b1-semantic-id
        (p15-s23-c11-mir-digest
         {:kind (:artifact b1)
          :record (p15-s23-c13-c14-b1-stage-semantic-input b1)})
        b1-artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:artifact b1)
          :schema-version (:schema-version b1)
          :semantic-id b1-semantic-id})
        source-path
        (or (get-in artifact [:actual-path-provenance :source])
            "<bounded-llvm-artifact>")]
    (when-not
     (and
      (= #{:semantic-id :artifact-id
           :c13-semantic-id :c13-artifact-id
           :c14-request-id :c14-semantic-id :c14-artifact-id
           :b1-semantic-id :b1-artifact-id :semantic-input}
         (set (keys projection)))
      (= [(:semantic-id packet) (:artifact-id packet)
          (:semantic-id c13) (:artifact-id c13)
          (get-in c14 [:request :request-id])
          (:semantic-id c14) (:artifact-id c14)
          (:semantic-id b1) (:artifact-id b1)]
         [packet-semantic-id packet-artifact-id
          c13-semantic-id c13-artifact-id
          c14-request-id c14-semantic-id c14-artifact-id
          b1-semantic-id b1-artifact-id]))
      (p15-s23-b3-llvm-fail!
       "B13-HASH" source-path packet
       {:missing-fact :reproducible-c13-c14-b1-sidecar-identity}))
    projection))

(defn p15-s23-c13-c14-b1-semantic-id
  [packet]
  (p15-s23-c11-mir-digest
   (p15-s23-c13-c14-b1-semantic-input packet)))

(defn p15-s23-c13-c14-b1-actual-path-binding-id
  [semantic-id actual-path-provenance]
  (p15-s23-c11-mir-digest
   {:kind :gravity/c13-c14-b1-actual-path-binding
    :semantic-id semantic-id
    :actual-path-provenance actual-path-provenance}))

(defn p15-s23-c13-c14-b1-require-trusted!
  [source-path carrier value sorted-policy]
  (let [validation
        (p15-s23-trusted-carrier-validation
         value sorted-policy
         p15-s23-c13-c14-b1-max-carrier-nodes
         p15-s23-c13-c14-b1-max-carrier-depth
         p15-s23-c13-c14-b1-max-carrier-nodes)]
    (when-not (= :passed (:status validation))
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" source-path {}
       (merge {:missing-fact :trusted-bounded-c13-c14-b1-carrier
               :carrier carrier}
              (select-keys validation
                           [:reason :observed-nodes :observed-depth
                            :maximum-nodes :maximum-depth :maximum-width]))))
    validation))