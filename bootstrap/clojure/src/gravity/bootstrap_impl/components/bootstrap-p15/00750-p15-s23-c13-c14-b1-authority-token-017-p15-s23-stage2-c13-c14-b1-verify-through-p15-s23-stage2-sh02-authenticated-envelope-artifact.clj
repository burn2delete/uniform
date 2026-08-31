(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-stage2-c13-c14-b1-verify!
  [packet checked-core context]
  (let [report
        (p15-s23-stage2-c13-c14-b1-verification-report
         packet checked-core context)]
    (when-not (= :passed (:status report))
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" (p15-s23-c11-ingress-source-path context) packet
       {:missing-fact :c13-c14-b1-verification-status}))
    :passed))

(defn p15-s23-stage2-c13-c14-b1-authentic?
  ([packet] false)
  ([packet checked-core context]
   (try
     (= :passed
        (p15-s23-stage2-c13-c14-b1-verify!
         packet checked-core context))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread)) (throw interrupted))
     (catch StackOverflowError _ false)
     (catch AssertionError _ false)
     (catch LinkageError _ false)
     (catch Exception _ false))))

(defn- p15-s23-sh02-final-preflight!
  [source-path artifact]
  ;; Establish exact built-in carrier classes before equality, hashing, or
  ;; contextual source replay. Request vectors receive their explicit 2,048
  ;; entry exception in the per-stage canonical carrier validation below.
  (p15-s23-sh02-require-bounded-carrier!
   source-path :sh02-final-artifact artifact)
  (p15-s23-c11-mir-bounded-value!
   source-path :sh02-final-artifact
   artifact
   (:maximum-carrier-nodes p15-s23-sh02-authenticated-envelope-bounds)
   (:maximum-carrier-depth p15-s23-sh02-authenticated-envelope-bounds))
  (let [envelopes (:envelopes artifact)
        source-rule (:source-rule artifact)
        actual-paths (:actual-path-provenance artifact)]
    (when-not
     (and (map? artifact)
          (= p15-s23-sh02-final-artifact-keys
             (set (keys artifact)))
          (= :gravity/sh02-reusable-authenticated-envelopes
             (:artifact artifact))
          (= 1 (:schema-version artifact))
          (= :accepted (:status artifact))
          (= :gravity-source (:semantic-authority artifact))
          (= [] (:diagnostics artifact))
          (= p15-s23-sh02-final-scope (:scope artifact))
          (map? (:host-tcb artifact))
          (map? source-rule)
          (= :gravity/pinned-authenticated-envelope-source-rule
             (:artifact source-rule))
          (= p15-s23-sh02-expected-source-content-hash
             (:source-content-hash source-rule))
          (= p15-s23-sh02-source-byte-count
             (:source-byte-count source-rule))
          (= p15-s23-sh02-expected-plan-semantic-hash
             (:plan-semantic-hash source-rule))
          (= p15-s23-sh02-expected-functions-semantic-hash
             (:functions-semantic-hash source-rule))
          (= p15-s23-sh02-builder-function
             (:builder-function source-rule))
          (= p15-s23-sh02-expected-builder-semantic-hash
             (:builder-semantic-hash source-rule))
          (= p15-s23-sh02-verifier-function
             (:verifier-function source-rule))
          (= p15-s23-sh02-expected-verifier-semantic-hash
             (:verifier-semantic-hash source-rule))
          (map? (:function-shapes source-rule))
          (= p15-s23-sh02-expected-function-count
             (count (:function-shapes source-rule)))
          (map? envelopes)
          (= #{:c13 :b1} (set (keys envelopes)))
          (map? actual-paths)
          (= #{:source :workspace-root :invocation-root
               :envelope-source :c13-source :b1-source}
             (set (keys actual-paths)))
          (every? #(and (string? %) (not (empty? %)))
                  (vals actual-paths))
          (every? p15-s23-sh02-sha256-id?
                  ((juxt :packet-id :packet-semantic-id
                         :semantic-id :artifact-id
                         :actual-path-binding-id)
                   artifact)))
      (p15-s23-sh02-fail!
       source-path artifact :exact-sh02-final-envelope
       {:observed-keys (when (map? artifact)
                         (set (keys artifact)))}))
    (doseq [stage [:c13 :b1]]
      (let [envelope (get envelopes stage)
            replay (:gravity-template-replay envelope)]
        (when-not
         (and (map? envelope)
              (= p15-s23-sh02-stage-envelope-keys
                 (set (keys envelope)))
              (= :gravity/sh02-stage-authenticated-envelope
                 (:artifact envelope))
              (= 1 (:schema-version envelope))
              (= :accepted (:status envelope))
              (= stage (:stage envelope))
              (map? (:sealed-artifact envelope))
              (vector? (:identity-checks envelope))
              (integer? (:request-count envelope))
              (pos? (:request-count envelope))
              (= (:request-count envelope)
                 (:request-count replay))
              (every? p15-s23-sh02-sha256-id?
                      ((juxt :semantic-envelope-id
                             :provenance-binding-id :request-graph-id)
                       envelope))
              (= [] (:diagnostics envelope))
              (= :gravity-source (:semantic-authority envelope))
              (map? (:host-tcb envelope))
              (false? (:self-hosted? envelope))
              (= source-rule (:source-rule envelope))
              (map? replay)
              (= p15-s23-sh02-template-replay-summary-keys
                 (set (keys replay)))
              (= :template-replay-passed (:status replay))
              (= :pending-host-resolution
                 (:identity-enforcement replay))
              (false? (:eligible-for-contextual-acceptance? replay))
              (= :gravity-source (:semantic-authority replay))
              (= p15-s23-sh02-verifier-checks (:checks replay))
              (= [] (:diagnostics replay)))
          (p15-s23-sh02-fail!
           source-path envelope :exact-sh02-stage-envelope-preflight
           {:stage stage}))
        ;; The public carrier stores only the resolved semantic envelope and
        ;; the leaf replay summary. The request graph is reconstructed from
        ;; current inputs before strict equality, never interpreted from the
        ;; candidate carrier.
        ))
    (let [semantic-id (p15-s23-sh02-final-semantic-id artifact)
          artifact-id (p15-s23-sh02-final-artifact-id semantic-id)
          path-binding-id
          (p15-s23-sh02-final-actual-path-binding-id
           semantic-id actual-paths)]
      (when-not
       (= [semantic-id artifact-id path-binding-id]
          ((juxt :semantic-id :artifact-id :actual-path-binding-id)
           artifact))
        (p15-s23-sh02-fail!
         source-path artifact :recomputable-sh02-final-identities {})))
    :passed))

(defn p15-s23-stage2-sh02-authenticated-envelope-artifact
  [packet checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-sh02-build-internal!
       p15-s23-c13-c14-b1-authority-token
       packet checked-core context)
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-sh02-fail!
         source-path {} :bounded-sh02-constructor-host-stack {}))
      (catch AssertionError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-constructor-assertion error))
      (catch LinkageError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-constructor-linkage error))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-constructor-diagnostic exception))
      (catch Exception exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-sh02-constructor-host-failure exception))))))
