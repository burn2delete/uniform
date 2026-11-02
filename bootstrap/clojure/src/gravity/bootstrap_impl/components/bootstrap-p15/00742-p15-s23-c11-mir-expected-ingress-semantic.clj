

(defn p15-s23-c11-mir-expected-ingress-semantic
  [checked-core context]
  (let [checked-core-map?
        (p15-s23-c11-exact-bounded-map? checked-core 128)
        context-map? (p15-s23-c11-exact-bounded-map? context 5)
        artifact-kind (when checked-core-map? (:kind checked-core))
        context-kind (when context-map? (:kind context))
        context-keys (when context-map? (set (keys context)))
        source-core-input
        (when checked-core-map? (:source-core-input checked-core))
        legacy-artifact-mode
        (when (p15-s23-c11-exact-bounded-map? source-core-input 32)
          (:mode source-core-input))]
    (cond
      (and (= :gravity/p15-s23-stage2-gravity-checked-core-artifact
              artifact-kind)
           (= :gravity/p15-s23-stage2-gravity-checked-core-context
              context-kind)
           (= p15-s23-c6c10-public-context-keys context-keys))
      {:schema-version 1
       :checked-core-artifact-kind artifact-kind
       :checked-core-context-kind context-kind
       :checked-core-ingress-mode :gravity-source-pure
       :checked-core-semantic-authority :gravity-source
       :checked-core-verification-status :passed}

      (and (= :gravity/p15-s23-stage2-closed-checked-core-artifact
              artifact-kind)
           (= p15-s23-c11-legacy-effectful-context-keys
              context-keys)
           (= :effectful-reference legacy-artifact-mode))
      {:schema-version 1
       :checked-core-artifact-kind artifact-kind
       :checked-core-context-kind :legacy-effectful-reference-context
       :checked-core-ingress-mode :effectful-reference
       :checked-core-semantic-authority
       :legacy-effectful-reference-verifier
       :checked-core-verification-status :passed}

      :else nil)))

(defn p15-s23-c11-mir-expected-request-binding-provenance
  [checked-core context]
  (case (:kind checked-core)
    :gravity/p15-s23-stage2-gravity-checked-core-artifact
    {:kind :gravity/c6-c10-physical-request-binding
     :request-binding-id
     (get-in checked-core [:physical-provenance :request-binding-id])
     :source-content-hash
     (get-in checked-core [:physical-provenance :source-content-hash])
     :digest-graph-proof-id
     (get-in checked-core [:physical-provenance :digest-graph-proof-id])
     :requested-target (:requested-target context)
     :actual-paths
     (get-in checked-core [:physical-provenance :actual-paths])}

    :gravity/p15-s23-stage2-closed-checked-core-artifact
    {:kind :gravity/legacy-checked-core-actual-path-binding
     :actual-path-binding-id (:actual-path-binding-id checked-core)
     :requested-target (:requested-target context)
     :actual-paths (get-in checked-core [:provenance :actual-paths])}

    nil))

(defn p15-s23-c11-mir-expected-provenance
  [checked-core context c11-source-path]
  {:actual-paths {:source (:source-path context)
                  :c11-source c11-source-path}
   :checked-core-request-binding
   (p15-s23-c11-mir-expected-request-binding-provenance
    checked-core context)
   :checked-core-mapping-id (:mapping-id checked-core)
   :checked-core-provenance-binding-id
   (:provenance-binding-id checked-core)
   :checked-core-origin-closure (:origin-closure checked-core)})

(defn p15-s23-c11-mir-semantic-input
  [artifact]
  {:kind :gravity/p15-s23-c11-authenticated-mir
   :schema-version (:schema-version artifact)
   :source-core-artifact-id (:source-core-artifact-id artifact)
   :checked-core-ingress (:checked-core-ingress artifact)
   :mir-module
   (p15-s23-c11-mir-path-neutral-value (:mir-module artifact))
   :source-rule (:source-rule artifact)
   :construction-record (:construction-record artifact)
   :verification-report
   (p15-s23-c11-mir-path-neutral-value
    (:verification-report artifact))
   :provenance
   (p15-s23-c11-mir-path-neutral-value
    (dissoc (:provenance artifact)
            :actual-paths :checked-core-request-binding))
   :scope (:scope artifact)
   :b1-preflight (:b1-preflight artifact)
   :mir-derived? (:mir-derived? artifact)
   :clojure-seed-boundary? (:clojure-seed-boundary? artifact)
   :self-hosted? (:self-hosted? artifact)})

(defn p15-s23-c11-mir-recomputed-id
  [artifact]
  ;; C11 semantic identity is collection-type-sensitive.  The general C11
  ;; digest remains unchanged for pinned source-plan compatibility, while the
  ;; authenticated artifact root uses the bounded canonical encoder that
  ;; distinguishes vectors, lists, sets, and maps.
  (p15-s23-c6c10-canonical-digest
   "<c11-semantic-identity>"
   (p15-s23-c11-mir-semantic-input artifact)))

(defn p15-s23-c11-mir-recomputed-actual-path-binding-id
  [artifact checked-core context]
  (p15-s23-c11-mir-digest
   {:kind :gravity/c11-mir-actual-path-binding
    :mir-id (:mir-id artifact)
    :source-path (:source-path context)
    :checked-core-request-binding
    (p15-s23-c11-mir-expected-request-binding-provenance
     checked-core context)
    :c11-source-path
    (get-in artifact [:provenance :actual-paths :c11-source])}))

(def p15-s23-c11-mir-artifact-keys
  #{:kind :schema-version :artifact-id :mir-id
    :actual-path-binding-id :source-core-artifact-id :mir-module
    :checked-core-ingress
    :source-rule :construction-record :verification-report :b1-preflight
    :provenance :scope :diagnostics :mir-derived?
    :clojure-seed-boundary? :self-hosted?})

(defn p15-s23-c11-mir-final-artifact-base
  [checked-core context ingress binding constructed verifier]
  (let [scope (p15-s23-c11-mir-scope-record)
        verifier-record
        (p15-s23-c11-independent-verifier-record verifier)
        mir-module
        (p15-s23-c11-mir-finalize
         constructed checked-core verifier-record scope)
        source-rule (p15-s23-c11-mir-source-rule-record binding)
        base
        {:kind :gravity/p15-s23-c11-authenticated-mir-artifact
         :schema-version 1
         :source-core-artifact-id (:artifact-id checked-core)
         :checked-core-ingress ingress
         :mir-module mir-module
         :source-rule source-rule
         :construction-record
         {:semantic-constructor
          {:owner :gravity-source
           :function p15-s23-c11-mir-builder-function
           :builder-semantic-hash (:builder-semantic-hash source-rule)}
          :execution-tcb
          {:runner :clojure-stage0-rule-runner
           :compiled-by :clojure-stage0-seed
           :seed-boundary? true}
          :semantic-replay-parity :public-verifier-required
          :invocation-audit :not-available
          :execution-count :not-claimed
          :live-invocation-claim? false
          :self-hosted? false}
         :verification-report verifier-record
         :b1-preflight (:b1-preflight mir-module)
         :provenance
         (p15-s23-c11-mir-expected-provenance
          checked-core context (:source-path binding))
         :scope scope
         :diagnostics []
         :mir-derived? true
         :clojure-seed-boundary? true
         :self-hosted? false}
        mir-id (p15-s23-c11-mir-recomputed-id base)
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:kind base) :schema-version 1 :mir-id mir-id})
        with-id (assoc base :mir-id mir-id :artifact-id artifact-id)]
    (assoc with-id
           :actual-path-binding-id
           (p15-s23-c11-mir-recomputed-actual-path-binding-id
            with-id checked-core context))))