(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-c13-c14-b1-wasm-build-internal!
  [candidate c11-artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)
        sorted-policy (p15-s23-c11-carrier-sorted-policy checked-core)]
    (p15-s23-c13-c14-b1-require-trusted!
     source-path :c13-c14-b1-wasm-c11-ingress
     c11-artifact sorted-policy)
    (let [c11-report
          (p15-s23-stage2-c11-mir-verification-report
           c11-artifact checked-core context)]
      (when-not (= :passed (:status c11-report))
        (p15-s23-b4-wasm-fail!
         "B1-INPUT" source-path c11-artifact
         {:missing-fact :fresh-c11-before-c13-c14-b1-wasm}))
      (let [bindings
            (p15-s23-c13-c14-b1-wasm-source-bindings!
             candidate source-path)
            c13-record
            (p15-s23-c13-build-for-target!
             candidate source-path c11-artifact c11-report
             (:c13 bindings) :wasm)
            c14-record
            (p15-s23-c14-wasm-build!
             candidate source-path c11-artifact checked-core c11-report
             c13-record (:c14 bindings))
            _ (p15-s23-c13-c14-b1-wasm-preflight!
               source-path c11-artifact)
            b1-record
            (p15-s23-b1-wasm-build!
             candidate source-path c11-artifact c13-record c14-record
             (:b1 bindings))]
        (p15-s23-c13-c14-b1-wasm-final-record
         source-path c11-artifact checked-core c11-report bindings
         c13-record c14-record b1-record)))))

(defn p15-s23-stage2-c13-c14-b1-wasm-packet-from-c11!
  [c11-artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-c13-c14-b1-wasm-build-authorized!
       c11-artifact checked-core context)
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread)) (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-b4-wasm-fail!
         "B1-INPUT" source-path {}
         {:missing-fact :bounded-hostile-c13-c14-b1-wasm-host-stack}))
      (catch AssertionError error
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-assertion error))
      (catch LinkageError error
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-linkage error))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-diagnostic exception))
      (catch Exception exception
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-host-failure exception)))))

(defn- p15-s23-c13-c14-b1-wasm-verification-preflight!
  [source-path packet]
  (p15-s23-c13-c14-b1-require-trusted!
   source-path :c13-c14-b1-wasm-final-packet packet :default-only)
  (p15-s23-c11-mir-bounded-value!
   source-path :c13-c14-b1-wasm-final-packet packet
   p15-s23-c13-c14-b1-max-carrier-nodes
   p15-s23-c13-c14-b1-max-carrier-depth)
  (when-not
   (and (map? packet)
        (= p15-s23-c13-c14-b1-wasm-final-packet-keys
           (set (keys packet)))
        (= :gravity/p15-s23-c13-c14-b1-wasm-authenticated-packet
           (:kind packet))
        (= 1 (:schema-version packet))
        (= :accepted-for-bounded-wasm (:status packet))
        (= :gravity-source (:semantic-authority packet))
        (= :clojure-stage0-independent-reconstruction
           (:verification-tcb packet))
        (= [] (:diagnostics packet))
        (= p15-s23-c13-c14-b1-wasm-final-packet-scope
           (:scope packet))
        (every? map?
                ((juxt :c11 :c13 :c14 :b1 :optimized-mir
                       :actual-path-provenance) packet))
        (every? string?
                ((juxt :semantic-id :artifact-id :actual-path-binding-id)
                 packet)))
    (p15-s23-b4-wasm-fail!
     "B1-METADATA" source-path packet
     {:missing-fact :bounded-c13-c14-b1-wasm-final-envelope}))
  (let [semantic-id (p15-s23-c13-c14-b1-semantic-id packet)]
    (when-not
     (= [semantic-id
         (p15-s23-c11-mir-digest
          {:kind (:kind packet) :schema-version 1 :semantic-id semantic-id})
         (p15-s23-c13-c14-b1-actual-path-binding-id
          semantic-id (:actual-path-provenance packet))]
        ((juxt :semantic-id :artifact-id :actual-path-binding-id) packet))
      (p15-s23-b4-wasm-fail!
       "B1-METADATA" source-path packet
       {:missing-fact :recomputable-c13-c14-b1-wasm-identities})))
  :passed)

(defn p15-s23-stage2-c13-c14-b1-wasm-verification-report
  [packet checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-c13-c14-b1-wasm-verification-preflight!
       source-path packet)
      (let [fresh-c11
            (p15-s23-stage2-c11-mir-artifact checked-core context)
            expected
            (p15-s23-c13-c14-b1-wasm-build-authorized!
             fresh-c11 checked-core context)]
        (p15-s23-c11-mir-require-strict-structure!
         source-path expected packet
         :contextual-fresh-c13-c14-b1-wasm-reconstruction)
        (p15-s23-c13-c14-b1-contextual-report-record packet))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread)) (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-b4-wasm-fail!
         "B1-INPUT" source-path {}
         {:missing-fact :bounded-hostile-c13-c14-b1-wasm-verifier-stack}))
      (catch AssertionError error
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-verifier-assertion error))
      (catch LinkageError error
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-verifier-linkage error))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-verifier-diagnostic exception))
      (catch Exception exception
        (p15-s23-b4-wasm-contain-exception!
         source-path :contained-c13-c14-b1-wasm-verifier-failure exception)))))

(defn p15-s23-stage2-c13-c14-b1-wasm-verify!
  [packet checked-core context]
  (let [report
        (p15-s23-stage2-c13-c14-b1-wasm-verification-report
         packet checked-core context)]
    (when-not (= :passed (:status report))
      (p15-s23-b4-wasm-fail!
       "B1-INPUT" (p15-s23-c11-ingress-source-path context) packet
       {:missing-fact :c13-c14-b1-wasm-verification-status}))
    :passed))

(defn p15-s23-stage2-c13-c14-b1-wasm-authentic?
  ([packet] false)
  ([packet checked-core context]
   (try
     (= :passed
        (p15-s23-stage2-c13-c14-b1-wasm-verify!
         packet checked-core context))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread)) (throw interrupted))
     (catch StackOverflowError _ false)
     (catch AssertionError _ false)
     (catch LinkageError _ false)
     (catch Exception _ false)))))
