(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-sh02-resolve-builder-result!
  [source-path descriptor raw-result]
  (p15-s23-sh02-validate-builder-envelope! source-path raw-result)
  (let [requests (:digest-requests raw-result)
        roots (:digest-graph-roots raw-result)
        request-count (count requests)
        preimage-identities
        (mapv
         (fn [ordinal request]
           (when-not
            (and (map? request)
                 (= p15-s23-c6c10-digest-request-keys
                    (set (keys request)))
                 (= ordinal (:key request) (:ordinal request))
                 (= :sha256 (:algorithm request))
                 (= :gravity/canonical-edn-v1 (:encoding request))
                 (vector? (:depends-on request)))
            (p15-s23-sh02-fail!
             source-path request :exact-sh02-digest-request-schema
             {:request-ordinal ordinal}))
           (let [references
                 (p15-s23-c6c10-collect-digest-ref-ordinals!
                  source-path (:preimage request) request-count ordinal)
                 dependencies (:depends-on request)]
             (when-not
              (and (every? #(and (integer? %)
                                 (<= 0 %)
                                 (< % ordinal))
                            dependencies)
                   (= dependencies
                      (vec (sort (distinct dependencies))))
                   (= (set dependencies) (set references)))
              (p15-s23-sh02-fail!
               source-path request
               :prior-only-sh02-digest-request-dependencies
               {:request-ordinal ordinal
                :declared-dependencies dependencies
                :observed-references (vec (sort (set references)))})))
           (p15-s23-c6c10-canonical-identity
            source-path (:preimage request)))
         (range request-count) requests)
        root-ordinals
        (mapv
         #(p15-s23-c6c10-exact-digest-ref-ordinal!
           source-path % request-count nil)
         roots)
        semantic-ordinal (first root-ordinals)
        provenance-ordinal (second root-ordinals)
        semantic-request (get requests semantic-ordinal)
        provenance-request (get requests provenance-ordinal)
        semantic-preimage (:preimage semantic-request)
        provenance-preimage (:preimage provenance-request)]
    (when-not
     (and (= 2 (count (distinct root-ordinals)))
          (= semantic-ordinal (- request-count 2))
          (= provenance-ordinal (dec request-count))
          (= request-count (count (distinct preimage-identities)))
          (map? semantic-preimage)
          (= #{:domain :semantic-envelope}
             (set (keys semantic-preimage)))
          (= :gravity/authenticated-envelope-semantic-root-v1
             (:domain semantic-preimage))
          (map? provenance-preimage)
          (= #{:domain :semantic-envelope-id :actual-path-provenance}
             (set (keys provenance-preimage)))
          (= :gravity/authenticated-envelope-provenance-binding-v1
             (:domain provenance-preimage))
          (= (:semantic-envelope-root raw-result)
             (:semantic-envelope-id provenance-preimage))
          (= (:actual-path-provenance descriptor)
             (:actual-path-provenance provenance-preimage))
          (p15-s23-sh02-semantic-root-path-neutral?
           semantic-preimage (:actual-path-provenance descriptor))
          (= (set (range request-count))
             (p15-s23-c6c10-request-graph-reachable-ordinals
              requests root-ordinals)))
      (p15-s23-sh02-fail!
       source-path raw-result :rooted-path-separated-sh02-digest-graph
       {:request-count request-count
        :root-ordinals root-ordinals}))
    (loop [ordinal 0
           resolved-digests []
           resolved-identities []]
      (if (= ordinal request-count)
        (let [resolve-complete
              (fn [value]
                (p15-s23-c6c10-resolve-digest-references!
                 source-path value request-count nil resolved-digests))
              sealed-artifact
              (resolve-complete (:artifact-template raw-result))
              resolved-roots (resolve-complete roots)
              resolved-checks
              (resolve-complete (:identity-checks raw-result))
              residual-references
              (p15-s23-c6c10-collect-digest-ref-ordinals!
               source-path
               {:artifact sealed-artifact
                :roots resolved-roots
                :identity-checks resolved-checks}
               request-count nil)]
          (when-not
           (and (empty? residual-references)
                (= 2 (count resolved-roots))
                (= (first resolved-roots)
                   (get sealed-artifact :semantic-envelope-id))
                (= (second resolved-roots)
                   (get sealed-artifact :provenance-binding-id))
                (= (first resolved-roots)
                   (get resolved-digests semantic-ordinal))
                (= (second resolved-roots)
                   (get resolved-digests provenance-ordinal))
                (every?
                 (fn [check]
                   (and (map? check)
                        (= #{:name :domain :computed-id :observed-id}
                           (set (keys check)))
                        (p15-s23-sh02-sha256-id? (:computed-id check))
                        (= (:computed-id check) (:observed-id check))))
                 resolved-checks))
            (p15-s23-sh02-fail!
             source-path raw-result
             :resolved-sh02-envelope-and-identity-consistency
             {:request-count request-count
              :residual-reference-count (count residual-references)}))
          {:sealed-artifact sealed-artifact
           :semantic-envelope-id (first resolved-roots)
           :provenance-binding-id (second resolved-roots)
           :identity-checks resolved-checks
           :resolved-digests resolved-digests
           :request-count request-count
           :request-graph-id
           (p15-s23-c6c10-canonical-digest
            source-path
            {:domain :gravity/sh02-resolved-digest-graph-v1
             :request-count request-count
             :root-digests resolved-roots
             :resolved-digests resolved-digests})})
        (let [request (get requests ordinal)
              resolved-preimage
              (p15-s23-c6c10-resolve-digest-references!
               source-path (:preimage request) request-count ordinal
               resolved-digests)
              resolved-identity
              (p15-s23-c6c10-canonical-identity
               source-path resolved-preimage)
              digest
              (p15-s23-c6c10-canonical-digest
               source-path resolved-preimage)]
          (when (or (some #{resolved-identity} resolved-identities)
                    (some #{digest} resolved-digests))
            (p15-s23-sh02-fail!
             source-path request
             :unique-sh02-resolved-preimages-and-digests
             {:request-ordinal ordinal :digest digest}))
          (recur (inc ordinal)
                 (conj resolved-digests digest)
                 (conj resolved-identities resolved-identity)))))))

(declare ^:private p15-s23-sh02-source-binding!)

(defn- p15-s23-sh02-source-rule
  [binding]
  (assoc
   (p15-s23-c13-c14-b1-source-rule
    :gravity.compiler/authenticated-envelope binding
    'authenticated-envelope-build-template)
   :artifact :gravity/pinned-authenticated-envelope-source-rule
   :verifier-function 'authenticated-envelope-verify-template
   :verifier-semantic-hash (:verifier-semantic-hash binding)))

(defn- p15-s23-sh02-validate-verifier-result!
  [source-path raw-result verifier-result]
  (p15-s23-sh02-require-bounded-carrier!
   source-path :gravity-verifier-result verifier-result)
  (p15-s23-c11-mir-bounded-value!
   source-path :sh02-gravity-verifier-result verifier-result
   (:maximum-carrier-nodes p15-s23-sh02-authenticated-envelope-bounds)
   (:maximum-carrier-depth p15-s23-sh02-authenticated-envelope-bounds))
  (when-not
   (and (map? verifier-result)
        (= p15-s23-sh02-verifier-result-keys
           (set (keys verifier-result)))
        (= :template-replay-passed (:status verifier-result))
        (= (:artifact-template raw-result)
           (:artifact-template verifier-result))
        (= (:digest-graph-root raw-result)
           (:digest-graph-root verifier-result))
        (= (:digest-graph-roots raw-result)
           (:digest-graph-roots verifier-result))
        (= (:semantic-envelope-root raw-result)
           (:semantic-envelope-root verifier-result))
        (= (:provenance-binding-root raw-result)
           (:provenance-binding-root verifier-result))
        (= (:identity-checks raw-result)
           (:identity-checks verifier-result))
        (= (count (:digest-requests raw-result))
           (:request-count verifier-result))
        (= :pending-host-resolution
           (:identity-enforcement verifier-result))
        (false? (:eligible-for-contextual-acceptance? verifier-result))
        (= :gravity-source (:semantic-authority verifier-result))
        (= p15-s23-sh02-verifier-checks (:checks verifier-result))
        (= [] (:diagnostics verifier-result)))
    (p15-s23-sh02-fail!
     source-path verifier-result :exact-sh02-gravity-verifier-replay
     {:observed-status (:status verifier-result)
      :observed-keys (when (map? verifier-result)
                       (set (keys verifier-result)))}))
  verifier-result))
