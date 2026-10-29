
(defn- revalidate-payload?
  "Re-run the owning stage's bounded verifier before accepting a fast hit."
  ([stage source-path payload]
   (revalidate-payload? stage source-path payload nil))
  ([stage source-path payload operation]
   (try
     (if-let [invocation (:invocation operation)]
       (and
        (case stage
          :resolution (= (:module-inputs operation) (get-in payload [:gravity-resolution-boundary :module-inputs]))
          :checked-core (= (:module-inputs operation) (get-in payload [:sh06-resolution-artifact :gravity-resolution-boundary :module-inputs]))
          true)
        (case stage
          :source true
          :reader (do (bootstrap-call 'c2-reader-validate! source-path payload)
                      (bootstrap-call 'c3-reader-artifact-view payload invocation) true)
          :syntax (do (bootstrap-call 'c3-syntax-validate! source-path payload invocation) true)
          :macroexpansion (= :passed (:status (bootstrap-call 'sh05-macro-artifact-verification payload invocation)))
          :resolution (= :passed (:status (bootstrap-call 'sh06-resolution-artifact-verification payload :gravity.bootstrap/legacy invocation)))
          :checked-core (= :passed (:status (bootstrap-call 'sh07-core-artifact-verification payload invocation)))
          false))
       (case stage
         :source true
         :reader (do (bootstrap-call 'c2-reader-validate! source-path payload)
                     true)
         :syntax (do (bootstrap-call 'c3-syntax-validate! source-path payload)
                     true)
         :macroexpansion (= :passed
                            (:status
                             (bootstrap-call 'sh05-macro-artifact-verification
                                             payload)))
         :resolution (= :passed
                        (:status
                         (bootstrap-call 'sh06-resolution-artifact-verification
                                         payload)))
         :checked-core (= :passed
                          (:status
                           (bootstrap-call 'sh07-core-artifact-verification
                                           payload)))
         false))
     (catch clojure.lang.ExceptionInfo _ false)
     (catch IllegalArgumentException _ false))))

(defn- authenticate-reader-source!
  "Bind the C2 reader payload to the source identity selected by this run.

  C2's own `:source-id` is intentionally a different identity domain from the
  facade's source snapshot id.  We therefore compare the complete bounded
  source identity projection (bytes, project root, and project-relative path)
  and require the source-unit record/provenance witnesses to be present."
  [source-path source-id payload]
  (let [record (:source-unit-record payload)
        identity-inputs (:identity-inputs record)
        root-record (:project-root-record record)]
    (when-not (map? record)
      (fail! "ARTIFACT-SOURCE-PARENT"
             "reader artifact is missing its authenticated source-unit record"
             {:source-path source-path}))
    (when-not (and (= (:bytes-hash source-id) (:bytes-hash record))
                   (= (:bytes-hash source-id) (:bytes-hash identity-inputs))
                   (= (:project-relative-path source-id)
                      (:project-relative-path record))
                   (= (:project-relative-path source-id)
                      (:project-relative-path identity-inputs))
                   (= (:project-root-id source-id) (:project-root record))
                   (= (:project-root-id source-id)
                      (:project-root-id root-record))
                   (= source-path (:path record))
                   (string? (:source-id record)))
      (fail! "ARTIFACT-SOURCE-PARENT"
             "reader artifact source identity does not match the snapshot"
             {:source-path source-path
              :expected (select-keys source-id
                                     [:bytes-hash :project-root-id
                                      :project-relative-path])
              :observed (select-keys record
                                     [:bytes-hash :project-root
                                      :project-relative-path :path])}))
    payload))

(defn- parent-ids
  [stage previous]
  (let [ids (if (= :reader stage)
              (if-let [source-envelope (first previous)]
                [(or (get-in source-envelope [:payload :source-identity-id])
                     (fail! "ARTIFACT-PARENT"
                            "source stage did not emit an identity binding"
                            {:stage stage}))]
                [])
              (->> previous
                   (keep (fn [envelope]
                           ;; Parent edges are authenticated in the existing
                           ;; stage's identity domain, not in this wrapper's
                           ;; envelope identity domain.
                           (or (:payload-artifact-id envelope)
                               (:artifact-id envelope))))
                   vec))]
    (when (and (pos? (get stage-index stage)) (empty? ids))
      (fail! "ARTIFACT-PARENT"
             "non-source artifact is missing its authenticated parent"
             {:stage stage}))
    ids))

(defn- cache-key-preimage
  [stage lane source-id parents request]
  (cond-> {:artifact :gravity/authenticated-artifact-cache-key
           :schema-version pipeline-schema-version
           :stage stage
           :lane lane
           :source source-id
           :parents parents
           :compiler implementation-binding
   ;; These are bounded, authenticated inputs to the cache identity.  They are
   ;; deliberately timestamp-free so a stable source and toolchain can reuse a
   ;; product while any compiler/tool/contract/policy change forces rebuild.
           :tool-binding tool-binding
           :contract-binding contract-binding
           :policy-binding policy-binding
           :pass-id (get stage-pass-ids stage)
           :policy {:cache-eligible? (= lane :fast)
                    :fresh-required? (= lane :fresh)
                    :source-authentication :bounded-secure-double-snapshot
                    :mutation-check :post-execution-secure-resnapshot}
           :request-policy (select-keys request [:target-stage :mode])}
    (:reader-operation request)
    (assoc :reader-operation (select-keys (:reader-operation request) [:binding :module-artifact-ids]))))

(defn- receipt
  [stage lane artifact-id parents cache-hit? cache-key-id source-id provenance]
  (let [base {:artifact :gravity/authenticated-artifact-receipt
              :schema-version 1
              :stage stage
              :mode lane
              :artifact-id artifact-id
              :parent-artifact-ids parents
              :cache {:eligible? (= lane :fast)
                      :hit? (boolean cache-hit?)
                      :authoritative? false}
              :source-content-hash (:bytes-hash source-id)
              :actual-path (:actual-path provenance)
              :cache-key-id cache-key-id
              :authority {:authoritative? false
                          :ceiling :development-only
                          :nonclaims [:integration :proof :release :self-hosting
                                      :seed-retirement :safety :performance]}}
        receipt-id (canonical-id! stage base)]
    (assoc base :receipt-id receipt-id)))

(defn- envelope
  [stage lane source-id provenance parents payload cache-hit? cache-key-id]
  (let [payload-id (:artifact-id payload)
        payload-artifact-id (or payload-id
                                (canonical-id! stage
                                               (select-keys payload
                                                            [:artifact
                                                             :schema-version
                                                             :source-identity
                                                             :source-identity-id
                                                             :source-authentication
                                                             :diagnostics
                                                             :authority])))
        ;; Every delegated compiler artifact already carries a stage-owned,
        ;; authenticated artifact id.  Re-hashing an entire checked-core tree
        ;; would exceed C16's bounded canonicalizer (and would duplicate the
        ;; owning verifier); combine that id with a bounded independent shape
        ;; witness.  The source envelope has no upstream id, so its semantic
        ;; projection is canonically encoded by `payload-integrity`.
        payload-integrity-id (payload-integrity stage payload)
        stage-facts (get stage-fact-contracts stage)
        parent-consumption (case stage
                             :source :none
                             :reader :authenticated-source-identity
                             :syntax :authenticated-parent
                             :macroexpansion :authenticated-immediate-parent
                             :resolution :authenticated-immediate-parent
                             :checked-core :authenticated-parent)
        recomputation-boundary? false
        base {:artifact :gravity/authenticated-compiler-artifact
              :schema-version pipeline-schema-version
              :pipeline pipeline-id
              :stage stage
              :status :accepted
              :mode lane
              :artifact-id nil
              :input-artifact-ids parents
              :payload-artifact-id payload-artifact-id
              ;; This full payload digest is for mutation detection only.  It
              ;; is intentionally absent from the semantic artifact identity;
              ;; provenance and lane policy are operational, not semantics.
              :payload-integrity-id payload-integrity-id
              :payload-size-witness (payload-size-witness stage payload)
              :source-identity source-id
              :provenance provenance
              :pass {:id (get stage-pass-ids stage)
                     :input (case stage
                              :source :source-bytes
                              :reader :authenticated-source-identity
                              :syntax :authenticated-parent
                              :macroexpansion :authenticated-immediate-parent
                              :resolution :authenticated-immediate-parent
                              :checked-core :authenticated-parent)
                     :output stage
                     :parent-consumption parent-consumption
                     :preserves (:preserves stage-facts)
                     :invalidates (:invalidates stage-facts)}
              :payload payload
              :diagnostics (vec (or (:diagnostics payload)
                                    (:reader-diagnostics payload)
                                    []))
              :authentication {:status :accepted
                               :source-content-hash (:bytes-hash source-id)
                               :parent-artifact-ids parents
                               :mutation-check :post-execution-secure-resnapshot
                               :path-rebinding-check :actual-path-bound
                               :parent-consumption parent-consumption}
              :recomputation-boundary? recomputation-boundary?
              :execution-boundary {:fresh-cache-bypassed? (= lane :fresh)
                                   ;; This facade is intentionally not an
                                   ;; isolated acceptance executor.  Fresh is
                                   ;; a same-JVM cache bypass only.
                                   :isolation :same-jvm
                                   :isolated? false
                                   :acceptance-authority? false}
              :cache-policy {:eligible? (= lane :fast)
                             :observed-hit? (boolean cache-hit?)
                             :authoritative? false}
              :cache-key-id cache-key-id
              :authority {:authoritative? false
                          :ceiling :development-only
                          :nonclaims [:integration :proof :release :self-hosting
                                      :seed-retirement :safety :performance]}}
        semantic-preimage
        (semantic-preimage stage source-id parents payload-artifact-id
                           (canonical-id! stage (:diagnostics base)))
        artifact-id (canonical-id! stage semantic-preimage)]
    (assoc base
           :semantic-preimage semantic-preimage
           :artifact-id artifact-id
           :receipt (receipt stage lane artifact-id parents cache-hit?
                             cache-key-id source-id provenance))))

(defn- envelope-id-preimage
  [artifact]
  (:semantic-preimage artifact))
