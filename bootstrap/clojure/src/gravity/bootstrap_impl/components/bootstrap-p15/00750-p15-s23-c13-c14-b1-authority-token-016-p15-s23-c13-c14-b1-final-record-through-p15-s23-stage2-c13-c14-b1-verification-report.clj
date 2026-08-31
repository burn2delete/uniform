(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-c13-c14-b1-final-record
  [source-path c11-artifact checked-core c11-report bindings
   c13-record c14-record b1-record]
  (let [actual-path-provenance
        {:source source-path
         :c11-source (get-in c11-artifact
                             [:provenance :actual-paths :c11-source])
         :c13-source (get-in bindings [:c13 :source-path])
         :c14-source (get-in bindings [:c14 :source-path])
         :b1-source (get-in bindings [:b1 :source-path])}
        base
        {:kind :gravity/p15-s23-c13-c14-b1-authenticated-packet
         :schema-version 1 :status :accepted-for-bounded-llvm
         :c11
         {:artifact-id (:artifact-id c11-artifact)
          :mir-id (:mir-id c11-artifact)
          :module-id (get-in c11-artifact [:mir-module :module-id])
          :checked-core-artifact-id (:artifact-id checked-core)
          :verifier-record
          (p15-s23-b3-llvm-c11-verifier-record c11-report)}
         :c13 c13-record :c14 c14-record :b1 b1-record
         :optimized-mir (:optimized-mir c13-record)
         :actual-path-provenance actual-path-provenance
         :diagnostics []
         :semantic-authority :gravity-source
         :verification-tcb :clojure-stage0-independent-reconstruction
         :scope
         {:bounded-llvm? true :whole-c13? false :whole-c14? false
          :whole-b1? false :whole-b3? false :public? false
          :release? false :self-hosted? false}}
        semantic-id (p15-s23-c13-c14-b1-semantic-id base)
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:kind base) :schema-version 1 :semantic-id semantic-id})]
    (assoc base :semantic-id semantic-id :artifact-id artifact-id
           :actual-path-binding-id
           (p15-s23-c13-c14-b1-actual-path-binding-id
            semantic-id actual-path-provenance))))

(defn- p15-s23-c13-c14-b1-build-internal!
  [candidate c11-artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)
        sorted-policy (p15-s23-c11-carrier-sorted-policy checked-core)]
    (p15-s23-c13-c14-b1-require-trusted!
     source-path :c13-c14-b1-c11-ingress c11-artifact sorted-policy)
    (let [c11-report
          (p15-s23-stage2-c11-mir-verification-report
           c11-artifact checked-core context)]
      (when-not (= :passed (:status c11-report))
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" source-path c11-artifact
         {:missing-fact :fresh-c11-before-c13-c14-b1}))
      (let [bindings
            (p15-s23-c13-c14-b1-source-bindings! candidate source-path)
            c13-record
            (p15-s23-c13-build!
             candidate source-path c11-artifact c11-report (:c13 bindings))
            c14-record
            (p15-s23-c14-build!
             candidate source-path c11-artifact checked-core c11-report
             c13-record (:c14 bindings))
            ;; C14 is the first owner of target-surface eligibility.  The
            ;; backend mirror runs only after the Gravity C14 record accepts
            ;; and before B1 or any backend/tool effect.
            _ (p15-s23-b3-llvm-preflight! c11-artifact)
            b1-record
            (p15-s23-b1-build!
             candidate source-path c11-artifact c13-record c14-record
             (:b1 bindings))]
        (p15-s23-c13-c14-b1-final-record
         source-path c11-artifact checked-core c11-report bindings
         c13-record c14-record b1-record)))))

(defn p15-s23-stage2-c13-c14-b1-packet-from-c11!
  [c11-artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-c13-c14-b1-build-internal!
       p15-s23-c13-c14-b1-authority-token
       c11-artifact checked-core context)
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" source-path {}
         {:missing-fact :bounded-hostile-c13-c14-b1-host-stack}))
      (catch AssertionError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-assertion error))
      (catch LinkageError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-linkage error))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-diagnostic exception))
      (catch Exception exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-host-failure exception)))))

(def p15-s23-c13-c14-b1-final-packet-keys
  #{:kind :schema-version :status :c11 :c13 :c14 :b1 :optimized-mir
    :actual-path-provenance :diagnostics :semantic-authority
    :verification-tcb :scope :semantic-id :artifact-id
    :actual-path-binding-id})

(def p15-s23-c13-c14-b1-final-packet-scope
  {:bounded-llvm? true :whole-c13? false :whole-c14? false
   :whole-b1? false :whole-b3? false :public? false
   :release? false :self-hosted? false})

(defn- p15-s23-c13-c14-b1-verification-preflight!
  [source-path packet]
  ;; Exact carrier classes are established before the general bounded-value
  ;; walk so no custom comparator, lazy sequence, hashCode, or equals method
  ;; can execute at this untrusted public boundary.
  (p15-s23-c13-c14-b1-require-trusted!
   source-path :c13-c14-b1-final-packet packet :default-only)
  ;; The trusted-carrier walk bounds shape only.  Apply the shared C11 scalar,
  ;; magnitude, collection-width, and total-byte limits before any fresh C11
  ;; replay or Gravity source-plan reconstruction.
  (p15-s23-c11-mir-bounded-value!
   source-path :c13-c14-b1-final-packet packet
   p15-s23-c13-c14-b1-max-carrier-nodes
   p15-s23-c13-c14-b1-max-carrier-depth)
  (let [top-level-class (when (some? packet) (.getName (class packet)))]
    (when-not
     (and (map? packet)
          (contains? p15-s23-trusted-carrier-map-classes top-level-class)
          (= p15-s23-c13-c14-b1-final-packet-keys
             (set (keys packet)))
          (= :gravity/p15-s23-c13-c14-b1-authenticated-packet
             (:kind packet))
          (= 1 (:schema-version packet))
          (= :accepted-for-bounded-llvm (:status packet))
          (= :gravity-source (:semantic-authority packet))
          (= :clojure-stage0-independent-reconstruction
             (:verification-tcb packet))
          (= [] (:diagnostics packet))
          (= p15-s23-c13-c14-b1-final-packet-scope (:scope packet))
          (every? map?
                  ((juxt :c11 :c13 :c14 :b1 :optimized-mir
                         :actual-path-provenance)
                   packet))
          (every? string?
                  ((juxt :semantic-id :artifact-id :actual-path-binding-id)
                   packet)))
      (p15-s23-b3-llvm-fail!
       "B1-METADATA" source-path packet
       {:missing-fact :bounded-c13-c14-b1-final-envelope})))
  (let [semantic-id (p15-s23-c13-c14-b1-semantic-id packet)
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:kind packet) :schema-version 1 :semantic-id semantic-id})
        actual-path-binding-id
        (p15-s23-c13-c14-b1-actual-path-binding-id
         semantic-id (:actual-path-provenance packet))]
    (when-not
     (= [semantic-id artifact-id actual-path-binding-id]
        ((juxt :semantic-id :artifact-id :actual-path-binding-id) packet))
      (p15-s23-b3-llvm-fail!
       "B1-METADATA" source-path packet
       {:missing-fact :recomputable-c13-c14-b1-final-identities})))
  :passed)

(defn p15-s23-stage2-c13-c14-b1-verification-report
  [packet checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-c13-c14-b1-verification-preflight! source-path packet)
      (let [fresh-c11 (p15-s23-stage2-c11-mir-artifact checked-core context)
            expected
            (p15-s23-c13-c14-b1-build-internal!
             p15-s23-c13-c14-b1-authority-token
             fresh-c11 checked-core context)]
        (p15-s23-c11-mir-require-strict-structure!
         source-path expected packet
         :contextual-fresh-c13-c14-b1-reconstruction)
        (when-not
         (and (= expected packet)
              (= (:semantic-id packet)
                 (p15-s23-c13-c14-b1-semantic-id packet))
              (= (:artifact-id packet)
                 (p15-s23-c11-mir-digest
                  {:kind (:kind packet) :schema-version 1
                   :semantic-id (:semantic-id packet)}))
              (= (:actual-path-binding-id packet)
                 (p15-s23-c13-c14-b1-actual-path-binding-id
                  (:semantic-id packet) (:actual-path-provenance packet))))
          (p15-s23-b3-llvm-fail!
           "B1-METADATA" source-path packet
           {:missing-fact :fresh-context-bound-c13-c14-b1-packet}))
        (p15-s23-c13-c14-b1-contextual-report-record packet))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" source-path {}
         {:missing-fact :bounded-hostile-c13-c14-b1-verifier-stack}))
      (catch AssertionError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-verifier-assertion error))
      (catch LinkageError error
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-verifier-linkage error))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-verifier-diagnostic exception))
      (catch Exception exception
        (p15-s23-b3-llvm-contain-exception!
         source-path :contained-c13-c14-b1-verifier-failure exception))))))
