

(defn p15-s23-c6c10-gravity-candidate-verification!
  [source-path fresh candidate-raw]
  (let [source-binding (:private-source-binding fresh)
        envelope (:private-envelope fresh)
        raw-result (:raw-result fresh)
        report
        (p15-s23-c6c10-invoke-pinned-source-function!
         source-path source-binding p15-s23-c6c10-verifier-function
         [envelope candidate-raw (:digest-requests raw-result)]
         :gravity-source-candidate-verifier)]
    (if (= :rejected (:status report))
      (p15-s23-c6c10-throw-sealed-rejection!
       source-path
       (p15-s23-c6c10-seal-digest-request-result!
        source-path report))
      (let [expected-keys
            #{:artifact-template :checks :diagnostics :digest-graph-root
              :digest-graph-roots :request-count :semantic-authority :status}]
        (p15-s23-c6c10-canonical-record source-path report)
        (when-not
         (and (map? report)
              (= expected-keys (set (keys report)))
              (= :passed (:status report))
              (= :gravity-source (:semantic-authority report))
              (= (count (:digest-requests raw-result))
                 (:request-count report))
              (vector? (:diagnostics report))
              (empty? (:diagnostics report))
              (vector? (:checks report))
              (= [:fresh-template-replay
                  :exact-digest-request-replay
                  :bounded-pure-c6-c10-scope]
                 (:checks report))
              (p15-s23-c6c10-canonical-identical?
               source-path candidate-raw (:artifact-template report))
              (p15-s23-c6c10-canonical-identical?
               source-path (:digest-graph-root raw-result)
               (:digest-graph-root report))
              (p15-s23-c6c10-canonical-identical?
               source-path (:digest-graph-roots raw-result)
               (:digest-graph-roots report)))
          (p15-s23-c6c10-host-fail!
           "C6-VERIFY" source-path
           :fresh-gravity-candidate-verifier-replay
           {:status (:status report)
            :request-count (:request-count report)}))
        (select-keys report
                     [:status :request-count :semantic-authority
                      :checks])))))

(defn p15-s23-stage2-gravity-checked-core-verification-report
  [artifact context]
  (let [context (p15-s23-c6c10-validate-public-context! context)
        source-path (:source-path context)]
    (p15-s23-c6c10-require-trusted-carrier!
     source-path :gravity-checked-core-artifact artifact)
    (p15-s23-c6c10-canonical-record source-path artifact)
    (when (p15-s23-c6c10-exact-digest-ref-present? artifact)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :public-candidate-free-of-raw-digest-refs
       {:artifact-kind (when (map? artifact) (:kind artifact))}))
    (let [{expected :artifact :as fresh}
          (p15-s23-c6c10-fresh-construction context)
          semantic-view
          (fn [value]
            (if (map? value)
              (apply dissoc value p15-s23-c6c10-physical-artifact-keys)
              value))
          candidate-raw
          (p15-s23-c6c10-rehydrate-candidate-template!
           source-path
           (get-in fresh [:raw-result :artifact-template])
           (get-in fresh [:sealed-result :sealed-artifact-template])
           (semantic-view artifact)
           (get-in fresh [:sealed-result :resolved-digests]))
          gravity-candidate-verification
          (p15-s23-c6c10-gravity-candidate-verification!
           source-path fresh candidate-raw)
          semantic-check
          (p15-s23-c6c10-strict-structure!
           source-path (semantic-view expected) (semantic-view artifact)
           :fresh-type-sensitive-semantic-artifact-zipper)
          target-check
          (p15-s23-c6c10-strict-structure!
           source-path (:target-request-metadata expected)
           (:target-request-metadata artifact)
           :exact-physical-target-request-metadata)
          provenance-check
          (p15-s23-c6c10-strict-structure!
           source-path (:physical-provenance expected)
           (:physical-provenance artifact)
           :exact-physical-source-provenance)]
      (when-not (= (:artifact-id expected) (:artifact-id artifact))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :fresh-sealed-semantic-root-parity
         {:expected-artifact-id (:artifact-id expected)
          :observed-artifact-id (:artifact-id artifact)}))
      {:artifact :gravity/p15-s23-c6-c10-verification-report
       :status :passed
       :artifact-id (:artifact-id artifact)
       :source-content-hash (:source-content-hash context)
       :semantic-structure semantic-check
       :target-request-metadata target-check
       :physical-provenance provenance-check
       :source-binding (:source-binding fresh)
       :binding-pins (:binding-pins fresh)
       :graph-proof (:graph-proof fresh)
       :gravity-source-verification (:gravity-verification fresh)
       :gravity-candidate-verification gravity-candidate-verification
       :actual-path-context
       {:source source-path
        :gravity-c6-c10-source
        (get-in expected [:physical-provenance :actual-paths
                          :gravity-c6-c10-source])}
       :clojure-seed-boundary? true
       :self-hosted? false})))

(defn p15-s23-stage2-gravity-checked-core-verify!
  [artifact context]
  (let [report
        (p15-s23-stage2-gravity-checked-core-verification-report
         artifact context)]
    (when-not (= :passed (:status report))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" (:source-path context)
       :gravity-checked-core-verification-report-status
       {:status (:status report)}))
    :passed))

(defn p15-s23-stage2-gravity-checked-core-authentic?
  ([artifact] false)
  ([artifact context]
   (try
     (= :passed
        (p15-s23-stage2-gravity-checked-core-verify!
         artifact context))
     (catch StackOverflowError _ false)
     (catch InterruptedException error
       (.interrupt (Thread/currentThread))
       (throw error))
     (catch Exception _ false))))

;; ---------------------------------------------------------------------------
;; Authenticated checked-core -> Gravity C11 MIR bridge (FL-P06-T03 slice)
;; ---------------------------------------------------------------------------

(def p15-s23-c11-mir-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")

(def p15-s23-c11-mir-builder-function
  'c11-build-target-independent-mir)

(def p15-s23-c11-mir-verifier-function
  'verify-c11-mir-module)

(def p15-s23-c11-mir-source-byte-count 330835)

(def p15-s23-c11-mir-expected-source-content-hash
  "sha256:cf7af2e3a7709bcc3ce5056cf75bab1bb0b4ac01c6627fe3d1f3d90d5c83c0aa")

(def p15-s23-c11-mir-expected-plan-semantic-hash
  "sha256:ec6ed386b8802f6cf9ef6e693f41d780a591cf7cc1d23ca42b3f08d78ff6e234")

(def p15-s23-c11-mir-expected-functions-semantic-hash
  "sha256:c15b126cfd712cefc6bea53a6a63ee44df38a6944e55030478d811cd4223d62a")

(def p15-s23-c11-mir-expected-builder-semantic-hash
  "sha256:594d0e4042bf0bf4274fe70a3b914ab79d9a40d7111084434e0aa56a5808bdb2")

(def p15-s23-c11-mir-expected-verifier-semantic-hash
  "sha256:13a4cbc1f63e62728aa821a75e85626a4fd14b4d14d6017ac3d5ca47531e4079")