

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
  (gravity.safety/managed-allocation-check #'p15-s23-closed-core-digest source-content-hash path source))

(defn p15-s23-closed-core-transcript-delivery-check
  [source-content-hash path source]
  (gravity.safety/transcript-delivery-check #'p15-s23-closed-core-digest source-content-hash path source))

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
  (gravity.safety/safety-basis source-operation))

(defn p15-s23-closed-core-pure-safety-proof
  [source-content-hash path source-operation source profile type effects capabilities ownership basis]
  (gravity.safety/pure-safety-proof #'p15-s23-closed-core-digest source-content-hash path source-operation source profile type effects capabilities ownership basis))

(defn p15-s23-closed-core-pure-safety-proof-valid?
  [proof]
  (gravity.safety/pure-safety-proof-valid? #'p15-s23-closed-core-digest proof))

(defn p15-s23-closed-core-structural-safety-proof
  [source-content-hash path source-operation source profile type effects capabilities ownership basis child-obligations]
  (gravity.safety/structural-safety-proof #'p15-s23-closed-core-digest source-content-hash path source-operation source profile type effects capabilities ownership basis child-obligations))

(defn p15-s23-closed-core-structural-safety-proof-valid?
  [proof node expected-child-obligations]
  (gravity.safety/structural-safety-proof-valid? #'p15-s23-closed-core-digest proof node expected-child-obligations))