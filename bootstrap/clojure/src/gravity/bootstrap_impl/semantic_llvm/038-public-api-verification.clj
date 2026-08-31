(defn p15-s23-b3-llvm-contain-exception!
  [source-path boundary exception]
  (let [data
        (p15-s23-backend-trusted-exception-data
         exception 65536 128)
        b3-diagnostic
        (p15-s23-b3-llvm-sanitized-complete-diagnostic data)
        c11-diagnostic
        (p15-s23-c11-mir-sanitized-complete-diagnostic data)]
    (cond
      b3-diagnostic
      (p15-s23-b3-llvm-throw-record! b3-diagnostic)

      c11-diagnostic
      (p15-s23-c11-mir-throw-record! c11-diagnostic)

      :else
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path {}
       {:missing-fact boundary
        :stderr-hash
        (str "sha256:"
             (sha256-hex (.getName (class exception))))}))))

(defn p15-s23-stage2-b3-llvm-verification-report
  "Replay the canonical Linux C11 -> C13 -> C14 -> B1 -> B3 chain.
  Development Docker/ELF observations are explicitly non-authoritative until
  the later integration supplies their runtime identities."
  [artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (when-not (and (map? artifact)
                     (= (:final-artifact-kind p15-s23-b3-llvm-policy)
                        (:kind artifact))
                     (= :llvm-x86_64-linux (:target artifact))
                     (= true (:clojure-seed-boundary? artifact))
                     (= false (:public-target? artifact))
                     (= false (:release-credit? artifact))
                     (= false (:self-hosted? artifact)))
        (p15-s23-b3-llvm-fail!
         "B3-TARGET" source-path artifact
         {:missing-fact :canonical-linux-b3-artifact-envelope}))
      ;; A static candidate is intentionally non-claiming.  Do not rebuild a
      ;; report from labels or placeholder records: only a complete, observed
      ;; Docker/LLVM/ELF transaction can enter the passed verifier.
      (when-not (and (= :development-emulation-observed (:status artifact))
                     (= :complete
                        (get-in artifact [:toolchain-evidence :status]))
                     (= :development-emulation-observed
                        (get-in artifact [:b3-record :status]))
                     (= :development-emulation-observed
                        (get-in artifact [:b13-record :status]))
                     (= :development-emulation-observed
                        (get-in artifact [:b14-record :status]))
                     (= :internal-experimental-observed
                        (get-in artifact [:c18-record :status]))
                     (true? (get-in artifact [:b14-record :same-result?]))
                     (= :observed
                        (get-in artifact [:toolchain-evidence :b13]))
                     (= :observed
                        (get-in artifact [:toolchain-evidence :b14]))
                     (= :observed
                        (get-in artifact [:toolchain-evidence :c18]))
                     (= :elf
                        (get-in artifact [:b13-record :artifact-files
                                          :object :format]))
                     (= :elf
                        (get-in artifact [:b13-record :artifact-files
                                          :executable :format]))
                     (string?
                      (get-in artifact [:b13-record :artifact-files
                                        :object :content-hash]))
                     (string?
                      (get-in artifact [:b13-record :artifact-files
                                        :executable :content-hash]))
                     (= true
                        (get-in artifact [:toolchain-evidence
                                          :process-result :matched?])))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path artifact
         {:missing-fact :complete-linux-development-evidence-required}))
      ;; Recompute local identities and emitted bytes before any fresh Docker
      ;; replay.  Tampering must fail with zero new tool observations.
      (p15-s23-b3-llvm-linux-evidence-integrity!
       artifact source-path)
      (let [fresh-c11
            (p15-s23-stage2-c11-mir-artifact checked-core context)
            expected
            (p15-s23-b3-llvm-linux-build-from-c11!
             fresh-c11 checked-core context
             {:run-linux-development-tools? true})]
        (when-not
         (and (= (:kind expected) (:kind artifact))
              (= (:target expected) (:target artifact))
              (= (:replay-projection-id expected)
                 (:replay-projection-id artifact))
              (= (:replay-projection expected)
                 (:replay-projection artifact)))
          (p15-s23-b3-llvm-fail!
           "B3-MANIFEST" source-path artifact
           {:missing-fact :contextual-fresh-replay-stable-projection-parity
            :expected-projection-id (:replay-projection-id expected)
            :observed-projection-id (:replay-projection-id artifact)}))
        (let [base {:artifact :gravity/b3-contextual-authenticity-report
                    :schema-version 1 :status :passed
                    :artifact-id (:artifact-id artifact)
                    :semantic-id (:semantic-id artifact)
                    :fresh-c11-mir-id (:mir-id fresh-c11)
                    :c13-artifact-id (get-in expected [:c13-c14-b1-packet
                                                        :c13 :artifact-id])
                    :c14-artifact-id (get-in expected [:c13-c14-b1-packet
                                                        :c14 :artifact-id])
                    :b1-artifact-id (get-in expected [:c13-c14-b1-packet
                                                       :b1 :artifact-id])
                    :c13-c14-b1-contextual-replay :passed
                    :gravity-b3-replay :passed
                    :independent-lowering-reconstruction :passed
                    :development-toolchain-replay
                    (get-in artifact [:toolchain-evidence :status])
                    :seed-boundary? true :self-hosted? false}]
          (assoc base :report-id
                 (p15-s23-c11-mir-digest
                  {:kind :gravity/b3-contextual-authenticity-report
                   :schema-version 1 :report base}))))
      (catch StackOverflowError error
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" source-path {}
         {:missing-fact :bounded-public-b3-verifier-host-stack}))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch AssertionError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-b3-verifier-assertion error))
      (catch LinkageError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-b3-verifier-linkage error))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-unstructured-b3-verifier-diagnostic
         exception))
      (catch Exception exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-b3-verifier-host-failure exception)))))

(defn p15-s23-stage2-b3-llvm-verify!
  [artifact checked-core context]
  (let [report
        (p15-s23-stage2-b3-llvm-verification-report
         artifact checked-core context)]
    (when-not (= :passed (:status report))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" (p15-s23-c11-ingress-source-path context) artifact
       {:missing-fact :contextual-b3-verification-report-status}))
    :passed))
