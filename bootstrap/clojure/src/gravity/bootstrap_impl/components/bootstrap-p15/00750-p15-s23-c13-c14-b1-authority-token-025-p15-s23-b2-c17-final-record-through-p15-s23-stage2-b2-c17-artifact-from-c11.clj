(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-b2-c17-final-record
  [source-path c11-artifact c-packet binding raw]
  (let [actual-path-provenance
        {:source source-path
         :c11-source (get-in c11-artifact
                             [:provenance :actual-paths :c11-source])
         :c13-source (get-in c-packet
                             [:actual-path-provenance :c13-source])
         :c14-source (get-in c-packet
                             [:actual-path-provenance :c14-source])
         :b1-source (get-in c-packet
                            [:actual-path-provenance :b1-source])
         :b2-source (:source-path binding)}]
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/b2-bounded-hosted-c17
     raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.backend/b2-c-backend binding
      p15-s23-b2-c17-builder-function)
     actual-path-provenance)))

(defn- p15-s23-b2-c17-build-internal!
  [candidate c11-artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)
        c-packet
        (p15-s23-c13-c14-b1-c-build-internal!
         candidate c11-artifact checked-core context)
        b1-record (:b1 c-packet)
        binding (p15-s23-b2-c17-source-binding! candidate source-path)
        expected
        (p15-s23-b2-c17-independent-raw
         source-path c11-artifact c-packet)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-b2-c17-builder-function
         [b1-record] "B1-INPUT")]
    (p15-s23-b2-c17-assert-exact!
     source-path expected raw :independent-b2-c17-reconstruction)
    (when-not (and (= p15-s23-b2-c17-raw-artifact-keys
                      (set (keys raw)))
                   (= expected raw)
                   (= (:artifact-id b1-record)
                      (get-in raw [:input-bindings :b1-artifact-id]))
                   (= (:semantic-id b1-record)
                      (get-in raw [:input-bindings :b1-semantic-id]))
                   (= (:actual-path-binding-id b1-record)
                      (get-in raw
                              [:input-bindings
                               :b1-actual-path-binding-id])))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path raw
       {:missing-fact :exact-capability-gated-b1-bound-b2-output}))
    (p15-s23-b2-c17-final-record
     source-path c11-artifact c-packet binding raw)))

(defn- p15-s23-b2-c17-semantic-id
  [artifact]
  (p15-s23-c11-mir-digest
   {:kind :gravity/b2-bounded-hosted-c17
    :record (p15-s23-c13-c14-b1-stage-semantic-input artifact)}))

(defn- p15-s23-b2-c17-artifact-id
  [artifact]
  (p15-s23-c11-mir-digest
   {:kind :gravity/b2-bounded-hosted-c17
    :schema-version 1
    :semantic-id (:semantic-id artifact)}))

(defn- p15-s23-b2-c17-actual-path-binding-id
  [artifact]
  (p15-s23-c13-c14-b1-actual-path-binding-id
   (:semantic-id artifact) (:actual-path-provenance artifact)))

(defn- p15-s23-b2-c17-require-trusted-final!
  [source-path artifact]
  (let [validation
        (p15-s23-trusted-carrier-validation
         artifact :default-only
         p15-s23-c13-c14-b1-max-carrier-nodes
         p15-s23-c13-c14-b1-max-carrier-depth
         p15-s23-c13-c14-b1-max-carrier-nodes)]
    (when-not (= :passed (:status validation))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       (merge
        {:missing-fact :trusted-bounded-b2-c17-final-carrier}
        (select-keys validation
                     [:reason :observed-nodes :observed-depth
                      :maximum-nodes :maximum-depth :maximum-width]))))
    (try
      (p15-s23-c11-mir-bounded-value!
       source-path :b2-c17-final-artifact artifact
       p15-s23-c13-c14-b1-max-carrier-nodes
       p15-s23-c13-c14-b1-max-carrier-depth)
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-c-backend-fail!
         "B2-MANIFEST" source-path {}
         {:missing-fact :bounded-b2-c17-final-carrier
          :bounded-reason
          (or (:missing-fact (ex-data exception))
              :contained-b2-final-carrier-diagnostic)})))
    :passed))

(defn- p15-s23-b2-c17-verification-preflight!
  [source-path artifact]
  (p15-s23-b2-c17-require-trusted-final! source-path artifact)
  (let [top-level-class (when (some? artifact)
                          (.getName (class artifact)))]
    (when-not
     (and (map? artifact)
          (contains? p15-s23-trusted-carrier-map-classes
                     top-level-class)
          (= p15-s23-b2-c17-final-artifact-keys
             (set (keys artifact)))
          (= :gravity/b2-bounded-hosted-c17 (:artifact artifact))
          (= 1 (:schema-version artifact))
          (= :constructed-unverified (:status artifact))
          (= p15-s23-b2-c17-policy-record (:policy artifact))
          (= p15-s23-b2-c17-dialect-selection-record
             (:dialect-selection artifact))
          (= [] (:diagnostics artifact))
          (true? (:clojure-seed-boundary? artifact))
          (false? (:whole-b2? artifact))
          (false? (:public? artifact))
          (false? (:release? artifact))
          (false? (:self-hosted? artifact))
          (map? (:source-rule artifact))
          (map? (:actual-path-provenance artifact))
          (= #{:source :c11-source :c13-source
               :c14-source :b1-source :b2-source}
             (set (keys (:actual-path-provenance artifact))))
          (every? string?
                  (vals (:actual-path-provenance artifact)))
          (every? string?
                  ((juxt :semantic-id :artifact-id
                         :actual-path-binding-id)
                   artifact)))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path artifact
       {:missing-fact :exact-b2-c17-final-envelope})))
  (let [semantic-id (p15-s23-b2-c17-semantic-id artifact)
        expected
        [semantic-id
         (p15-s23-c11-mir-digest
          {:kind :gravity/b2-bounded-hosted-c17
           :schema-version 1 :semantic-id semantic-id})
         (p15-s23-c13-c14-b1-actual-path-binding-id
          semantic-id (:actual-path-provenance artifact))]]
    (when-not
     (= expected
        ((juxt :semantic-id :artifact-id :actual-path-binding-id)
         artifact))
      (p15-s23-c-backend-fail!
       "B13-HASH" source-path artifact
       {:missing-fact :recomputable-b2-c17-final-identities})))
  :passed)

(defn p15-s23-stage2-b2-c17-artifact-from-c11!
  [c11-artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-b2-c17-build-internal!
       p15-s23-c13-c14-b1-authority-token
       c11-artifact checked-core context)
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-c-backend-fail!
         "B2-MANIFEST" source-path {}
         {:missing-fact :bounded-hostile-b2-c17-host-stack}))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-c-backend-contain-exception!
         source-path :contained-b2-c17-diagnostic exception))
      (catch Exception exception
        (p15-s23-c-backend-contain-exception!
         source-path :contained-b2-c17-host-failure exception))))))
