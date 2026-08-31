(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-publish-via-provider!
  [candidate gate-a contextual transaction payload publication-target
   source-path success-projector]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :prepare-descriptor-relative-c17-publication)
  (if-not publication-target
    (let [receipt
          {:status :ephemeral-conformance-artifacts
           :actual-output-directory nil :sidecar-hashes {}}
          finalized
          (p15-s23-b2-c17-gate-b-final-record
           gate-a contextual transaction receipt)]
      (p15-s23-b2-c17-gate-b-integrity-preflight! source-path finalized)
      (success-projector finalized))
    (let [preliminary
          ;; Receipt and actual paths are outside semantic identity.  Stable
          ;; preliminary IDs make the canonical sidecars cycle-free.
          (p15-s23-b2-c17-gate-b-final-record
           gate-a contextual transaction nil)
          material
          (p15-s23-b2-c17-gate-b-publication-material
           preliminary payload source-path)
          staged
          (p15-s23-b2-c17-gate-b-provider-call!
           source-path :stage
           #(darwin-publication/stage-bundle!
             publication-target (:file-specs material)))
          receipt
          (p15-s23-b2-c17-gate-b-canonical-provider-receipt
           (:publication-receipt staged) material source-path)
          finalized
          (p15-s23-b2-c17-gate-b-final-record
           gate-a contextual transaction receipt)
          _
          (p15-s23-b2-c17-gate-b-integrity-preflight!
           source-path finalized)
          final-material
          (p15-s23-b2-c17-gate-b-publication-material
           finalized payload source-path)
          _
          (when-not
           (p15-s23-b2-c17-gate-b-publication-material-equivalent?
            material final-material)
            (p15-s23-c-backend-fail!
             "B13-HASH" source-path finalized
             {:missing-fact :precommit-final-c17-sidecar-parity}))
          success-result
          ;; The caller's projection is completed while the complete bundle
          ;; remains private.  A projection failure is cleaned by the outer
          ;; provider lifecycle before any destination can exist.
          (success-projector finalized)]
      (p15-s23-b2-c17-gate-b-provider-call!
       source-path :commit
       #(darwin-publication/commit-staged-bundle!
         staged success-result)))))

(defn- p15-s23-b2-c17-gate-b-verify-via-provider!
  [candidate artifact transaction source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :verify-descriptor-relative-c17-publication)
  (let [receipt (:publication-receipt artifact)
        intent? (get-in artifact
                        [:toolchain-evidence :publication-intent?])]
    (if-not intent?
      (do
        (when-not
         (= {:status :ephemeral-conformance-artifacts
             :actual-output-directory nil :sidecar-hashes {}}
            receipt)
          (p15-s23-c-backend-fail!
           "B13-SCHEMA" source-path artifact
           {:missing-fact :canonical-ephemeral-c17-publication-receipt}))
        {:status :passed :publication :ephemeral
         :core-artifact-count 4 :sidecar-count 0})
      (let [material
            (p15-s23-b2-c17-gate-b-publication-material
             artifact (:publication-payload transaction) source-path)
            raw-receipt
            (p15-s23-b2-c17-gate-b-raw-provider-receipt
             receipt material source-path)
            verification
            (p15-s23-b2-c17-gate-b-provider-call!
             source-path :verify
             #(darwin-publication/verify-published-bundle!
               raw-receipt (:file-specs material)))]
        (when-not
         (and (map? verification)
              (= #{:status :publication :file-count :file-records
                   :publisher-evidence}
                 (set (keys verification)))
              (= :passed (:status verification))
              (= :descriptor-relative-exclusive-rename
                 (:publication verification))
              (= 7 (:file-count verification)))
          (p15-s23-c-backend-fail!
           "B13-SCHEMA" source-path artifact
           {:missing-fact
            :canonical-descriptor-relative-c17-verification-envelope}))
        (when-not
         (and (= (:file-records material)
                 (:file-records verification))
              (= (:publisher-evidence receipt)
                 (:publisher-evidence verification)))
          (p15-s23-c-backend-fail!
           "B13-HASH" source-path artifact
           {:missing-fact
            :exact-descriptor-relative-c17-publication-verification}))
        {:status :passed :publication :atomically-published
         :core-artifact-count 4 :sidecar-count 3}))))

(defn- p15-s23-stage2-b2-c17-gate-b-artifact!
  "Execute the authenticated hosted-C17 Gate B from a context-bound Gate A.
  The optional output directory uses exclusive seven-file publication."
  ([gate-a checked-core context]
   (p15-s23-stage2-b2-c17-gate-b-artifact!
    gate-a checked-core context {}))
  ([gate-a checked-core context options]
   (p15-s23-stage2-b2-c17-gate-b-artifact!
    gate-a checked-core context options
    p15-s23-b2-c17-gate-b-authority-token identity))
  ([gate-a checked-core context options projector-authority
    success-projector]
   (let [source-path (p15-s23-c11-ingress-source-path context)]
     (try
       (p15-s23-b2-c17-gate-b-require-authority!
        projector-authority source-path
        :authenticated-c17-success-projector)
       (when-not (ifn? success-projector)
         (p15-s23-c-backend-fail!
          "B2-MANIFEST" source-path {}
          {:missing-fact :callable-authenticated-c17-success-projector}))
       (let [{:keys [options gate-a-contextual-report]}
             (p15-s23-b2-c17-gate-b-pre-effect-gate!
              gate-a checked-core context options)
             execute
             (fn [publication-context]
               (let [transaction
                     (p15-s23-b2-c17-gate-b-toolchain-transaction!
                      p15-s23-b2-c17-gate-b-authority-token gate-a
                      source-path (boolean publication-context))
                     payload (:publication-payload transaction)]
                 (when-not
                  (and (= #{:source :header :object :executable}
                          (set (keys payload)))
                       (every? bytes? (vals payload)))
                   (p15-s23-c-backend-fail!
                    "B13-HASH" source-path gate-a
                    {:missing-fact
                     :exact-private-c17-publication-payload}))
                 (p15-s23-b2-c17-gate-b-publish-via-provider!
                  p15-s23-b2-c17-gate-b-authority-token
                  gate-a gate-a-contextual-report transaction payload
                  publication-context source-path success-projector)))
             publication-context
             (p15-s23-b2-c17-gate-b-output-preflight!
              p15-s23-b2-c17-gate-b-authority-token
              (:output-directory options) source-path)]
         (try
           (execute publication-context)
           (catch Throwable error
             (if publication-context
               (p15-s23-b2-c17-gate-b-abort-after-failure!
                publication-context source-path error)
               (throw error)))))
       (catch InterruptedException interrupted
         (.interrupt (Thread/currentThread))
         (throw interrupted))
       (catch java.nio.channels.ClosedByInterruptException interrupted
         (.interrupt (Thread/currentThread))
         (throw interrupted))
       (catch java.io.InterruptedIOException interrupted
         (.interrupt (Thread/currentThread))
         (throw interrupted))
       (catch StackOverflowError _
         (p15-s23-c-backend-fail!
          "B2-MANIFEST" source-path {}
          {:missing-fact :bounded-hostile-c17-gate-b-constructor-stack}))
       (catch clojure.lang.ExceptionInfo exception
         (p15-s23-c-backend-contain-exception!
          source-path :contained-c17-gate-b-constructor-diagnostic exception))
       (catch Exception exception
       (p15-s23-c-backend-contain-exception!
        source-path :contained-c17-gate-b-constructor-host-failure
          exception))))))

(defn p15-s23-stage2-b2-c17-gate-b-artifact-from-c11!
  ([c11-artifact checked-core context]
   (p15-s23-stage2-b2-c17-gate-b-artifact-from-c11!
    c11-artifact checked-core context {}))
  ([c11-artifact checked-core context options]
   (let [source-path (p15-s23-c11-ingress-source-path context)]
     (try
       ;; Options are rejected before even the pure Gate-A reconstruction.
       (let [validated-options
             (p15-s23-b2-c17-gate-b-validated-options! source-path options)
             gate-a
             (p15-s23-stage2-b2-c17-artifact-from-c11!
              c11-artifact checked-core context)]
         (p15-s23-stage2-b2-c17-gate-b-artifact!
          gate-a checked-core context validated-options))
       (catch InterruptedException interrupted
         (.interrupt (Thread/currentThread))
         (throw interrupted))
       (catch StackOverflowError _
         (p15-s23-c-backend-fail!
          "B2-MANIFEST" source-path {}
          {:missing-fact :bounded-hostile-c17-gate-b-c11-ingress-stack}))
       (catch clojure.lang.ExceptionInfo exception
         (p15-s23-c-backend-contain-exception!
          source-path :contained-c17-gate-b-c11-ingress-diagnostic exception))
       (catch Exception exception
         (p15-s23-c-backend-contain-exception!
          source-path :contained-c17-gate-b-c11-ingress-host-failure
          exception)))))))
