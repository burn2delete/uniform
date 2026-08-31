(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn- p15-s23-b4-wasm-build-internal!
  [supplied-c11 checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)
        checked-core-sorted-policy
        (p15-s23-c11-carrier-sorted-policy checked-core)]
    (p15-s23-c11-mir-require-trusted-carrier!
     source-path :b4-c11-ingress supplied-c11
     checked-core-sorted-policy)
    (let [report (p15-s23-stage2-c11-mir-verification-report
                  supplied-c11 checked-core context)]
      (when-not (= :passed (:status report))
        (p15-s23-b4-wasm-fail!
         "B1-INPUT" source-path supplied-c11
         {:missing-fact :fresh-context-bound-c11-parity}))
      (let [fresh (p15-s23-stage2-c11-mir-artifact checked-core context)
            _
            (when-not
             (and (= supplied-c11 fresh)
                  (= (p15-s23-c11-mir-semantic-input supplied-c11)
                     (p15-s23-c11-mir-semantic-input fresh)))
              (p15-s23-b4-wasm-fail!
               "B1-INPUT" source-path supplied-c11
               {:missing-fact :fresh-context-bound-c11-parity}))
            wasm-packet
            (p15-s23-c13-c14-b1-wasm-build-authorized!
             fresh checked-core context)
            preflight (p15-s23-b4-wasm-preflight! wasm-packet)
            binding (p15-s23-b4-wasm-source-binding!
                     p15-s23-b4-wasm-authority-token source-path)
            reconstruction (p15-s23-b4-wasm-reconstruct preflight)
            lowering (p15-s23-b4-wasm-invoke-builder!
                      p15-s23-b4-wasm-authority-token binding
                      (:b1 wasm-packet) source-path reconstruction)
            parser (p15-s23-b4-wasm-parse-module!
                    (:wasm-bytes lowering) reconstruction)
            invocation-audit (atom {:starts 0})
            node (p15-s23-b4-wasm-run-node!
                  p15-s23-b4-wasm-authority-token source-path
                  (:wasm-bytes lowering) (:expected-result reconstruction)
                  invocation-audit)]
        (let [artifact
              (p15-s23-b4-wasm-final-record
               fresh checked-core context report wasm-packet binding lowering
               reconstruction parser node)]
          (p15-s23-b4-wasm-verify-integrity!
           artifact fresh context binding preflight)
          artifact)))))

(defn p15-s23-stage2-b4-wasm-artifact-from-c11!
  [c11 checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-b4-wasm-build-internal! c11 checked-core context)
      (catch StackOverflowError _
        (p15-s23-b4-wasm-fail!
         "B1-INPUT" source-path {}
         {:missing-fact :bounded-hostile-b4-ingress-host-stack}))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread)) (throw interrupted))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-b4-diagnostic exception))
      (catch Exception error
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-b4-host-failure error)))))

(defn p15-s23-stage2-b4-wasm-source-artifact!
  [source-path source-text]
  (try
    (let [upstream-diagnostic-owner (Object.)
          [checked-core context]
          (binding [*p15-s23-c11-upstream-diagnostic-owner*
                    upstream-diagnostic-owner
                    *p15-s23-c11-mir-diagnostic-context*
                    {:requested-target :wasm}
                    *additional-bootstrap-targets*
                    stage2-runtime-derived-source-targets]
            (try
              (let [context
                    (p15-s23-stage2-gravity-checked-core-context
                     source-path source-text :wasm)]
                [(p15-s23-stage2-gravity-checked-core-source-artifact
                  context)
                 context])
              (catch InterruptedException interrupted
                (.interrupt (Thread/currentThread))
                (throw interrupted))
              (catch clojure.lang.ExceptionInfo exception
                (let [data
                      (p15-s23-backend-trusted-exception-data
                       exception 65536 128)]
                  (if (and
                       data
                       (p15-s23-c11-mir-owned-upstream-diagnostic? data))
                    (p15-s23-c11-mir-contain-checked-core-exception!
                     source-path :b4-source-checked-core-diagnostic exception)
                    (throw exception))))))
          c11 (p15-s23-stage2-c11-mir-artifact checked-core context)]
      (p15-s23-stage2-b4-wasm-artifact-from-c11!
       c11 checked-core context))
    (catch StackOverflowError _
      (p15-s23-b4-wasm-fail!
       "B1-INPUT" source-path {}
       {:missing-fact :bounded-hostile-b4-source-host-stack}))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread)) (throw interrupted))
    (catch clojure.lang.ExceptionInfo exception
      (p15-s23-b4-wasm-contain-exception!
       source-path :contained-b4-source-diagnostic exception))
    (catch Exception error
      (p15-s23-b4-wasm-contain-exception!
       source-path :contained-b4-source-host-failure error))))

(defn p15-s23-stage2-b4-wasm-verification-report
  [artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-b4-wasm-require-trusted-final-carrier!
       source-path artifact)
      (when-not (map? artifact)
        (p15-s23-b4-wasm-fail!
         "B4-MANIFEST" source-path {}
         {:missing-fact :bounded-b4-final-artifact-map}))
      (p15-s23-c11-mir-bounded-value!
       source-path :b4-public-final-artifact-ingress artifact
       p15-s23-b4-wasm-max-final-artifact-carrier-nodes
       p15-s23-b4-wasm-max-final-artifact-carrier-depth)
      (p15-s23-b4-wasm-verify-frozen-envelope! artifact source-path)
      (let [fresh (p15-s23-stage2-c11-mir-artifact checked-core context)
            fresh-report (p15-s23-stage2-c11-mir-verification-report
                          fresh checked-core context)
            _ (when-not (= :passed (:status fresh-report))
                (p15-s23-b4-wasm-fail!
                 "B1-INPUT" source-path fresh
                 {:missing-fact :fresh-c11-before-static-b4-integrity}))
            wasm-packet
            (p15-s23-c13-c14-b1-wasm-build-authorized!
             fresh checked-core context)
            preflight (p15-s23-b4-wasm-preflight! wasm-packet)
            binding (p15-s23-b4-wasm-source-binding!
                     p15-s23-b4-wasm-authority-token source-path)
            _ (p15-s23-b4-wasm-verify-integrity!
               artifact fresh context binding preflight)
            expected (p15-s23-b4-wasm-build-internal!
                      fresh checked-core context)
            local-start-count
            (get-in expected
                    [:node-conformance :invocation-local-start-count])]
        (when-not (= 1 local-start-count)
          (p15-s23-b4-wasm-fail!
           "B4-MANIFEST" source-path artifact
           {:missing-fact :exactly-one-contextual-node-process
            :invocation-local-start-count local-start-count}))
        (when-not (= (p15-s23-b4-wasm-semantic-input artifact)
                     (p15-s23-b4-wasm-semantic-input expected))
          (p15-s23-b4-wasm-fail!
           "B4-MANIFEST" source-path artifact
           {:missing-fact :contextual-fresh-b4-replay-parity}))
        {:artifact :gravity/b4-contextual-authenticity-report
         :status :passed :artifact-id (:artifact-id artifact)
         :semantic-id (:semantic-id artifact)
         :fresh-c11 :passed :fresh-c13 :passed :fresh-c14 :passed
         :fresh-b1 :passed :gravity-b4-replay :passed
         :independent-reconstruction :passed
         :raw-module-verification :passed :pinned-node-replay :passed
         :invocation-local-start-count local-start-count
         :self-hosted? false})
      (catch StackOverflowError _
        (p15-s23-b4-wasm-fail!
         "B4-MANIFEST" source-path {}
         {:missing-fact :bounded-hostile-b4-verification-host-stack}))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread)) (throw interrupted))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-b4-verification-diagnostic exception))
      (catch Exception error
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-b4-verification-host-failure error)))))

(defn p15-s23-stage2-b4-wasm-verify! [artifact checked-core context]
  (let [report (p15-s23-stage2-b4-wasm-verification-report
                artifact checked-core context)]
    (when-not (= :passed (:status report))
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" (p15-s23-c11-ingress-source-path context) artifact
       {:missing-fact :contextual-b4-verification-status}))
    :passed))

(defn p15-s23-stage2-b4-wasm-authentic?
  ([artifact] false)
  ([artifact checked-core context]
   (try
     (= :passed (p15-s23-stage2-b4-wasm-verify!
                 artifact checked-core context))
     (catch StackOverflowError _ false)
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread)) (throw interrupted))
     (catch Exception _ false)))))
