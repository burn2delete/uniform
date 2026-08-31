

(defn p15-s23-c6c10-validate-digest-request-graph!
  [source-path raw-result]
  (p15-s23-c6c10-validate-builder-result-envelope!
   source-path raw-result)
  (let [requests (:digest-requests raw-result)
        roots (:digest-graph-roots raw-result)
        request-count (when (vector? requests) (count requests))]
    (when-not (and (vector? requests)
                   (<= request-count p15-s23-c6c10-max-digest-requests)
                   (vector? roots)
                   (if (= :accepted (:status raw-result))
                     (and (pos? request-count)
                          (= 1 (count roots))
                          (= (:digest-graph-root raw-result) (first roots)))
                     (if (contains? raw-result :propagated-upstream?)
                       (and (empty? requests)
                            (empty? roots)
                            (true? (:propagated-upstream? raw-result)))
                       (and (pos? request-count)
                            (= 1 (count roots))
                            (= 1 (count (:diagnostics raw-result)))))))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :bounded-rooted-digest-request-graph
       {:status (:status raw-result)
        :request-count request-count
        :root-count (when (vector? roots) (count roots))}))
    (let [preimage-identities
          (mapv
           (fn [expected-ordinal request]
             (when-not
              (and (map? request)
                   (= p15-s23-c6c10-digest-request-keys
                      (set (keys request)))
                   (= expected-ordinal (:key request))
                   (= expected-ordinal (:ordinal request))
                   (= :sha256 (:algorithm request))
                   (= :gravity/canonical-edn-v1 (:encoding request))
                   (vector? (:depends-on request)))
              (p15-s23-c6c10-host-fail!
               "C6-VERIFY" source-path :exact-ordered-digest-request-schema
               {:expected-ordinal expected-ordinal
                :observed-request
                (when (map? request)
                  (dissoc request :preimage))}))
             (let [dependencies (:depends-on request)
                   references
                   (p15-s23-c6c10-collect-digest-ref-ordinals!
                    source-path (:preimage request) request-count
                    expected-ordinal)]
               (when-not
                (and (= dependencies
                        (vec (sort (distinct dependencies))))
                     (every? #(and (integer? %)
                                   (<= 0 %)
                                   (< % expected-ordinal))
                             dependencies)
                     (= (set dependencies) (set references)))
                 (p15-s23-c6c10-host-fail!
                  "C6-VERIFY" source-path
                  :exact-prior-digest-request-dependency-closure
                  {:ordinal expected-ordinal
                   :declared-dependencies dependencies
                   :observed-references (vec (sort (set references)))})))
             (p15-s23-c6c10-canonical-identity
              source-path (:preimage request)))
           (range request-count)
           requests)
          root-ordinals
          (mapv #(p15-s23-c6c10-exact-digest-ref-ordinal!
                  source-path % request-count nil)
                roots)]
      (when-not (= root-ordinals (vec (sort (distinct root-ordinals))))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :unique-ordered-digest-graph-roots
         {:root-ordinals root-ordinals}))
      (when-not (= request-count (count (distinct preimage-identities)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :unique-raw-digest-request-preimages
         {:request-count request-count
          :unique-preimage-count (count (distinct preimage-identities))}))
      (when (pos? request-count)
        (let [reachable
              (p15-s23-c6c10-request-graph-reachable-ordinals
               requests root-ordinals)]
          (when-not (= (set (range request-count)) reachable)
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :root-reachable-digest-request-closure
             {:request-count request-count
              :root-ordinals root-ordinals
              :unreachable-ordinals
              (vec (sort (remove reachable (range request-count))))}))))
      (when (= :accepted (:status raw-result))
        (let [root-ordinal (first root-ordinals)
              root-request (get requests root-ordinal)
              root-preimage (:preimage root-request)
              template (:artifact-template raw-result)]
          (when-not
           (and (= root-ordinal (dec request-count))
                (= #{:domain :semantic-artifact}
                   (set (keys root-preimage)))
                (= :gravity/c6-c10-checked-core-artifact-v1
                   (:domain root-preimage))
                (= (p15-s23-c6c10-canonical-identity
                    source-path (:semantic-artifact root-preimage))
                   (p15-s23-c6c10-canonical-identity
                    source-path (dissoc template :artifact-id)))
                (= (p15-s23-c6c10-canonical-identity
                    source-path (first roots))
                   (p15-s23-c6c10-canonical-identity
                    source-path (:artifact-id template))))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path
             :root-bound-gravity-artifact-template
             {:root-ordinal root-ordinal
              :request-count request-count}))))
      (when (and (= :rejected (:status raw-result))
                 (not (contains? raw-result :propagated-upstream?)))
        (let [root-ordinal (first root-ordinals)
              root-preimage (:preimage (get requests root-ordinal))
              diagnostic (first (:diagnostics raw-result))
              expected-identity
              {:domain :gravity/c6-c10-diagnostic-v1
               :rule (:rule diagnostic)
               :stage (:stage diagnostic)
               :primary (:primary diagnostic)
               :involved-artifacts (:involved-artifacts diagnostic)
               :facts (:facts diagnostic)}]
          (when-not
           (and (= root-ordinal (dec request-count))
                (= (p15-s23-c6c10-canonical-identity
                    source-path (first roots))
                   (p15-s23-c6c10-canonical-identity
                    source-path (:diagnostic-id diagnostic)))
                (= (p15-s23-c6c10-canonical-identity
                    source-path expected-identity)
                   (p15-s23-c6c10-canonical-identity
                    source-path root-preimage)))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path
             :root-bound-gravity-diagnostic-identity
             {:root-ordinal root-ordinal
              :request-count request-count}))))
      {:request-count request-count
       :root-ordinals root-ordinals
       :raw-preimage-identities preimage-identities})))

(defn p15-s23-c6c10-resolve-digest-request-graph!
  [source-path raw-result]
  (let [{:keys [request-count root-ordinals] :as validation}
        (p15-s23-c6c10-validate-digest-request-graph!
         source-path raw-result)
        requests (:digest-requests raw-result)]
    (loop [ordinal 0
           resolved-digests []
           resolved-preimage-identities []
           resolved-requests []]
      (if (= ordinal request-count)
        (let [root-digests (mapv #(get resolved-digests %) root-ordinals)]
          {:validation validation
           :resolved-digests resolved-digests
           :resolved-requests resolved-requests
           :root-digests root-digests})
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
          (when (or (some #{resolved-identity}
                          resolved-preimage-identities)
                    (some #{digest} resolved-digests))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path
             :unique-resolved-digest-request-preimages-and-digests
             {:ordinal ordinal :digest digest}))
          (recur
           (inc ordinal)
           (conj resolved-digests digest)
           (conj resolved-preimage-identities resolved-identity)
           (conj resolved-requests
                 (assoc request
                        :preimage resolved-preimage
                        :digest digest))))))))