

(defn p15-s23-closed-core-recomputed-node-type
  [node node-by-id]
  (let [operand-node #(get node-by-id %)
        operand-type #(some-> % operand-node :type)]
    (case (:source-operation node)
      :implicit-nil :gravity/nil
      :literal (p15-s23-closed-core-scalar-literal-type
                (get-in node [:attributes :value]))
      :quote (p15-s23-closed-core-scalar-literal-type
              (get-in node [:attributes :value]))
      :local (operand-type (first (:operands node)))
      :let-binding (operand-type (first (:operands node)))
      :truthy :gravity/bool
      :if (p15-s23-closed-core-type-join
           (operand-type (second (:operands node)))
           (operand-type (nth (:operands node) 2 nil)))
      :do (operand-type (last (:operands node)))
      :let (operand-type (last (:operands node)))
      :function
      {:params []
       :return (operand-type (last (:operands node)))
       :latent-effects (:effects node)
       :capabilities (:capabilities node)
       :throws #{}
       :ownership-constraints #{:persistent-immutable-shareable}
       :profile-constraints #{:hosted}}
      ;; These are recognized only so C7 can diagnose them before the C8
      ;; runtime residual.  No accepted artifact may contain them.
      :str :gravity/string
      :println :gravity/nil
      nil)))

(defn p15-s23-closed-core-printable-type?
  [type]
  (if (and (map? type) (= :gravity/union (:kind type)))
    (let [members (:members type)]
      (and (vector? members)
           (seq members)
           (= members (vec (sort-by pr-str (set members))))
           (every? p15-s23-closed-core-printable-type? members)))
    (contains? #{:gravity/nil :gravity/string :gravity/bool :gravity/char
                 :gravity/keyword :gravity/symbol}
               type)))

(defn p15-s23-closed-core-managed-allocation-check
  [source-content-hash path source]
  (let [input {:kind :managed-allocation-result
               :source-content-hash source-content-hash
               :path path
               :origin-id (:origin-id source)
               :effect :memory/allocate
               :capability :memory/allocator
               :provider :gravity.reference/jvm-managed-allocator
               :failure :gravity/allocation-error}]
    (assoc input
           :artifact :gravity/runtime-check
           :check-id (p15-s23-closed-core-digest input)
           :status :required)))

(defn p15-s23-closed-core-transcript-delivery-check
  [source-content-hash path source]
  (let [input {:kind :reference-transcript-delivery
               :source-content-hash source-content-hash
               :path path
               :origin-id (:origin-id source)
               :effect :io/write
               :capability :io/stdout
               :provider :gravity.reference/transcript-capture
               :handler :gravity.bootstrap/reference-harness
               :delivery :in-memory-reference-transcript
               :live-external-io? false
               :failure :gravity/transcript-capture-error}]
    (assoc input
           :artifact :gravity/runtime-check
           :check-id (p15-s23-closed-core-digest input)
           :status :required)))

(defn p15-s23-closed-core-fact-union
  [& values]
  (apply set/union #{} (map set values)))

(defn p15-s23-closed-core-persistent-ownership
  [role extra]
  (merge
   {:model :persistent-immutable-value
    :role role
    :shareability :shared
    :alias-policy :immutable-sharing
    :mutation :forbidden
    :managed-reachability :value-and-program-reachability
    :cleanup-policy :no-explicit-cleanup
    :provider-requirement :not-required
    :allocator-requirement :not-required
    :escape-policy :safe-persistent-value
    :derived? true}
   extra))

(defn p15-s23-closed-core-intrinsic-effects
  [source-operation]
  (case source-operation
    :str #{:memory/allocate}
    :println #{:io/write}
    #{}))

(defn p15-s23-closed-core-intrinsic-capabilities
  [source-operation]
  (case source-operation
    :str #{:memory/allocator}
    :println #{:io/stdout}
    #{}))

(defn p15-s23-closed-core-safety-basis
  [source-operation]
  (case source-operation
    :implicit-nil :compiler-generated-nil
    :literal :closed-scalar-literal
    :quote :closed-quote
    :local :resolved-lexical-local
    :let-binding :sequential-let-binding
    :truthy :gravity-truthiness
    :if :closed-conditional
    :do :ordered-sequence
    :let :sequential-lexical-scope
    :function :authenticated-closed-function
    :str :closed-str-managed-allocation
    :println :closed-println-reference-transcript
    :not-applicable))

(defn p15-s23-closed-core-pure-safety-proof
  [source-content-hash path source-operation source profile type effects
   capabilities ownership basis]
  (let [base
        {:artifact :gravity/p15-s23-pure-safety-proof
         :rule-version "p15-s23-pure-safety-v1"
         :source-content-hash source-content-hash
         :plan-path path
         :source-operation source-operation
         :basis basis
         :source-origin {:origin-id (:origin-id source)
                         :span (:span source)
                         :plan-path path}
         :profile profile
         :source-target :jvm
         :type type
         :effects (set effects)
         :capabilities (set capabilities)
         :ownership-model (:model ownership)
         :ownership-digest (p15-s23-closed-core-digest ownership)
         :specialized-safe-rule
         :persistent-immutable-pure-closed-operation
         :effect-requirements #{}
         :capability-requirements #{}
         :invalidation-conditions
         [:source-origin-change :type-change :effect-change
          :capability-change :ownership-change :profile-change
          :source-target-change :rule-version-change]
         :status :proved-for-pure-closed-slice}]
    (assoc base :proof-id (p15-s23-closed-core-digest base))))

(defn p15-s23-closed-core-pure-safety-proof-valid?
  [proof]
  (and (map? proof)
       (= (:proof-id proof)
          (p15-s23-closed-core-digest (dissoc proof :proof-id)))
       (= :gravity/p15-s23-pure-safety-proof (:artifact proof))
       (= :proved-for-pure-closed-slice (:status proof))
       (= #{} (:effect-requirements proof))
       (= #{} (:capability-requirements proof))))

(defn p15-s23-closed-core-structural-safety-proof
  [source-content-hash path source-operation source profile type effects
   capabilities ownership basis child-obligations]
  (let [base
        {:artifact :gravity/p15-s23-structural-safety-proof
         :rule-version "p15-s23-structural-safety-v1"
         :source-content-hash source-content-hash
         :plan-path path
         :source-operation source-operation
         :basis basis
         :source-origin {:origin-id (:origin-id source)
                         :span (:span source)
                         :plan-path path}
         :profile profile
         :source-target :jvm
         :type type
         :effects (set effects)
         :capabilities (set capabilities)
         :ownership-model (:model ownership)
         :ownership-digest (p15-s23-closed-core-digest ownership)
         :child-obligations (vec child-obligations)
         :effect-requirements (set effects)
         :capability-requirements (set capabilities)
         :proof-condition
         :authenticated-child-runtime-check-and-capability-obligations
         :invalidation-conditions
         [:source-origin-change :operand-change :type-change :effect-change
          :capability-change :ownership-change :profile-change
          :source-target-change :rule-version-change]
         :status
         :proved-conditional-on-authenticated-child-obligations}]
    (assoc base :proof-id (p15-s23-closed-core-digest base))))

(defn p15-s23-closed-core-structural-safety-proof-valid?
  [proof node expected-child-obligations]
  (and
   (map? proof)
   (= :gravity/p15-s23-structural-safety-proof (:artifact proof))
   (= :proved-conditional-on-authenticated-child-obligations
      (:status proof))
   (= (vec expected-child-obligations) (:child-obligations proof))
   (= (:effects node) (:effects proof) (:effect-requirements proof))
   (= (:capabilities node)
      (:capabilities proof)
      (:capability-requirements proof))
   (= (:proof-id proof)
      (p15-s23-closed-core-digest (dissoc proof :proof-id)))))