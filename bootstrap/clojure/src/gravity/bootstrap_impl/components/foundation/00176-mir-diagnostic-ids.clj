

(def mir-diagnostic-ids
  ["C11-MODULE"
   "C11-BLOCK"
   "C11-DOMINANCE"
   "C11-TYPE"
   "C11-EFFECT"
   "C11-SAFETY"
   "C11-ORIGIN"
   "C11-DOMAIN"
   "C11-TARGET-LEAK"
   "C11-VERIFY"])

(def mir-diagnostic-messages
  {"C11-MODULE" "MIR module record is malformed"
   "C11-BLOCK" "MIR block is malformed or unterminated"
   "C11-DOMINANCE" "MIR operation uses a value before definition"
   "C11-TYPE" "MIR operation is missing type evidence"
   "C11-EFFECT" "effectful MIR operation is missing ordering evidence"
   "C11-SAFETY" "safety-sensitive MIR operation is missing outcome evidence"
   "C11-ORIGIN" "MIR operation is missing source or generated origin"
   "C11-DOMAIN" "MIR domain anchor is invalid"
   "C11-TARGET-LEAK" "target-specific opcode appeared in generic MIR"
   "C11-VERIFY" "MIR verifier failed"})

(def mir-override-diagnostics
  {:module ["C11-MODULE" :module]
   :block ["C11-BLOCK" :block]
   :dominance ["C11-DOMINANCE" :data-flow]
   :type ["C11-TYPE" :operation]
   :effect ["C11-EFFECT" :operation]
   :safety ["C11-SAFETY" :operation]
   :origin ["C11-ORIGIN" :operation]
   :domain ["C11-DOMAIN" :domain-anchor]
   :target-leak ["C11-TARGET-LEAK" :operation]
   :verify ["C11-VERIFY" :verifier]})

(def mir-required-operation-families
  [:constant
   :local
   :call
   :closure
   :dispatch
   :data-constructor
   :field-index-buffer
   :numeric
   :memory
   :region
   :linear-resource
   :control-flow
   :error
   :ffi
   :concurrency
   :workflow
   :ai-tool
   :domain-anchor
   :runtime-check
   :proof-reference])

(defn mir-source-overrides
  [module]
  (get-in module [:metadata :compiler :mir] {}))

(defn mir-fail!
  [id source-path artifact subject extra]
  (fail! id
         (get mir-diagnostic-messages id "MIR validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (source-span source-path 0))
                 :diagnostic-family :mir-verifier
                 :stage :build-mir
                 :profile (get-in artifact [:mir-module :profile])
                 :target (get-in artifact [:mir-module :target-request])
                 :artifact-id (get-in artifact [:mir-module :module])
                 :operation (:op-id subject)
                 :missing-fact (:missing-fact subject)
                 :remediation "Regenerate MIR from checked core with type, effect, ownership, capability, safety, and origin evidence."}
                extra)))

(defn mir-opcode
  [core-kind]
  (case core-kind
    :def :mir/def
    :fn :mir/fn
    :if :mir/branch
    :do :mir/sequence
    :call :mir/call
    :literal :mir/constant
    :symbol :mir/local
    :let :mir/let
    :match :mir/match
    :throw :mir/throw
    :mir/unknown))

(defn mir-operation
  [module type-by-node effect-by-node safety-by-node ownership-by-node node]
  (let [node-id (:node-id node)
        effects (set (get-in effect-by-node [node-id :effects] #{}))
        safety (get safety-by-node node-id)
        ownership (get ownership-by-node node-id)
        op-type (get type-by-node node-id "Unit")]
    {:op-id (str "mir-op-" node-id)
     :opcode (mir-opcode (:kind node))
     :family (cond
               (= "SafetyClassificationRecord" op-type) :proof-reference
               (= "RuntimeCheckRecord" op-type) :runtime-check
               (str/includes? op-type "Buffer") :memory
               (= :call (:kind node)) :call
               (= :fn (:kind node)) :closure
               (#{:if :do :let :match} (:kind node)) :control-flow
               (= :literal (:kind node)) :constant
               :else :local)
     :operands []
     :result (when-not (= :literal (:kind node))
               (str "mir-value-" node-id))
     :type op-type
     :effects effects
     :ordering (if (seq effects) :sequence :none)
     :source {:core-node node-id
              :span (:source-span node)
              :origin-chain (or (:generated-origin node) [])}
     :profile (:profile module)
     :facts {:ownership (:fact ownership)
             :capabilities (set (get-in effect-by-node
                                         [node-id :capabilities] #{}))
             :safety (:outcome safety)
             :runtime-check (:runtime-check safety)
             :proofs (vec (remove nil? [(:proof-reference safety)]))}
     :domain-anchor nil
     :verifier-status :passed}))

(defn mir-family-coverage
  [operations]
  (let [families (set (map :family operations))]
    (mapv (fn [family]
            {:family family
             :status (if (contains? families family)
                       :represented-by-operation
                       :represented-by-stage0-contract)})
          mir-required-operation-families)))

(defn mir-validate-overrides!
  [source-path artifact]
  (when-let [fail-kind (get-in artifact [:source-overrides :fail])]
    (let [[id subject-kind] (get mir-override-diagnostics fail-kind)]
      (when id
        (mir-fail! id source-path artifact
                   {:stage subject-kind
                    :op-id (str "mir-invalid-" (name fail-kind))
                    :source-span (source-span source-path 0)
                    :missing-fact fail-kind}
                   {:missing-fields [fail-kind]})))))

(defn mir-validate!
  [source-path artifact]
  (mir-validate-overrides! source-path artifact)
  (let [module (:mir-module artifact)
        operations (:mir-operations artifact)
        blocks (vals (:blocks (first (vals (:functions module)))))
        domain-anchors (:domain-anchor-table artifact)]
    (when-not (and (= :gravity/mir-module (:artifact module))
                   (perf-present? (:source-core module))
                   (perf-present? (:functions module))
                   (perf-present? (:profile module))
                   (perf-present? (:target-request module)))
      (mir-fail! "C11-MODULE" source-path artifact module
                 {:missing-fields [:artifact :source-core :functions
                                   :profile :target-request]}))
    (when-not (every? #(perf-present? (:terminator %)) blocks)
      (mir-fail! "C11-BLOCK" source-path artifact (first blocks)
                 {:missing-fields [:terminator]}))
    (when-not (every? #(= :passed (:dominance-status %))
                      (:data-flow-graph artifact))
      (mir-fail! "C11-DOMINANCE" source-path artifact
                 (first (:data-flow-graph artifact))
                 {:missing-fields [:dominance-status]}))
    (when-not (every? #(perf-present? (:type %)) operations)
      (mir-fail! "C11-TYPE" source-path artifact
                 (first (remove #(perf-present? (:type %)) operations))
                 {:missing-fields [:type]}))
    (when-not (every? #(or (empty? (:effects %))
                           (not= :none (:ordering %)))
                      operations)
      (mir-fail! "C11-EFFECT" source-path artifact
                 (first (filter #(and (seq (:effects %))
                                      (= :none (:ordering %)))
                                operations))
                 {:missing-fields [:ordering]}))
    (when-not (perf-present? (:safety-outcome-table artifact))
      (mir-fail! "C11-SAFETY" source-path artifact (first operations)
                 {:missing-fields [:safety-outcome-table]}))
    (when-not (every? #(perf-present? (get-in % [:source :span])) operations)
      (mir-fail! "C11-ORIGIN" source-path artifact
                 (first (remove #(perf-present? (get-in % [:source :span]))
                                operations))
                 {:missing-fields [:source]}))
    (when-not (every? #(and (perf-present? (:anchor-id %))
                            (perf-present? (:fallback %)))
                      domain-anchors)
      (mir-fail! "C11-DOMAIN" source-path artifact (first domain-anchors)
                 {:missing-fields [:anchor-id :fallback]}))
    (when (some #(= :target-specific (:family %)) operations)
      (mir-fail! "C11-TARGET-LEAK" source-path artifact
                 (first (filter #(= :target-specific (:family %)) operations))
                 {:missing-fields [:target-independent-opcode]}))
    (when-not (= :passed (get-in artifact [:mir-verifier-report :status]))
      (mir-fail! "C11-VERIFY" source-path artifact
                 (:mir-verifier-report artifact)
                 {:missing-fields [:status]})))
  :complete)