

(defn check-call-node
  [checker ctx node]
  (let [operator (:operator node)]
    (call-specific-diagnostic! operator node)
    (cond
      (= 'effect/handle operator)
      (check-effect-handle-node checker ctx node)

      (= 'task/scope operator)
      (let [child (child-context ctx)]
        (swap! child assoc
               :in-task-scope? true
               :task-scope-id (:node-id node))
        (let [args (mapv #(check-typed-node checker child %) (:arguments node))
              effects (collect-fact-effects args)
              capabilities (collect-fact-capabilities args)
              spec (get call-specs operator)]
          (check-effects-and-capabilities! checker ctx node effects capabilities)
          (record-concurrency-call! checker child node operator args effects
                                    capabilities spec)
          (typed-fact checker node (:return-type spec) effects capabilities
                      {:children args
                       :callee operator})))
      :else
      (let [args (mapv #(check-typed-node checker ctx %) (:arguments node))
            spec (get call-specs operator)]
        (if (nil? spec)
          (do
            (when (contains? #{:native :kernel :firmware :hardware :formal} (:profile @ctx))
              (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                                 "constrained profile requires type facts for this call"
                                 node
                                 "Add a checked declaration or provider fact before using this call in a constrained profile."
                                 {:profile (:profile @ctx)
                                  :operator operator}))
            (typed-fact checker node "Dynamic"
                        (collect-fact-effects args)
                        (collect-fact-capabilities args)
                        {:children args
                         :callee operator}))
          (do
            (when (and (= 'dynamic/value operator)
                       (contains? no-dynamic-profiles (:profile @ctx)))
              (typed-diagnostic! "L5-DYNAMIC-FORBIDDEN"
                                 "dynamic value is forbidden in this profile"
                                 node
                                 "Use a statically typed value or add a profile-approved runtime check boundary."
                                 {:profile (:profile @ctx)}))
            (when (= 'typed/assert operator)
              (check-typed-assert! node args))
            (when (= 'typed/value operator)
              (check-typed-value! node args))
            (when (= 'typed/return operator)
              (check-typed-return! node args))
            (when (= 'raw/deref operator)
              (typed-diagnostic! "L10-RAW-SAFE"
                                 "raw pointer operation appears in safe core without an unsafe island"
                                 node
                                 "Wrap raw memory behind an audited unsafe island or safe checked API."))
            (when (= 'task/spawn operator)
              (when (contains? no-scheduler-profiles (:profile @ctx))
                (typed-diagnostic! "L11-SCHEDULER"
                                   "active profile lacks required scheduler/runtime support"
                                   node
                                   "Use a profile-supported scheduler provider or remove task spawning."
                                   {:profile (:profile @ctx)}))
              (when-not (:in-task-scope? @ctx)
                (typed-diagnostic! "L11-TASK-SCOPE"
                                   "task spawn escapes structured task scope"
                                   node
                                   "Spawn tasks inside task/scope or transfer the handle through an explicit contract.")))
            (when (= 'resource/close operator)
              (consume-linear-resource! checker ctx node args))
            (let [effects (set/union (collect-fact-effects args) (or (:effects spec) #{}))
                  capabilities (set/union (collect-fact-capabilities args) (or (:capabilities spec) #{}))
                  return-type (asserted-return-type operator args spec)]
              (when (:compile-time-kind spec)
                (check-compile-time-policy! ctx node args effects spec))
              (check-effects-and-capabilities! checker ctx node effects capabilities
                                               (select-keys spec [:capability-diagnostic
                                                                  :build-grant-diagnostic]))
              (record-memory-call! checker ctx node operator args effects
                                   capabilities return-type spec)
              (record-concurrency-call! checker ctx node operator args effects
                                        capabilities spec)
              (record-compile-time-call! checker ctx node operator args effects
                                         capabilities return-type spec)
	              (record-standard-library-call! checker ctx node operator args effects
	                                             capabilities return-type spec)
	              (record-facet-call! checker ctx node operator args effects
	                                  capabilities return-type spec)
	              (record-provider-call! checker ctx node operator args effects
	                                     capabilities return-type spec)
	              (record-alternative-macro-call! checker ctx node operator args effects
	                                              capabilities return-type spec)
	              (record-alternative-type-call! checker ctx node operator args effects
	                                             capabilities return-type spec)
	              (record-alternative-memory-call! checker ctx node operator args effects
	                                               capabilities return-type spec)
	              (record-interop-call! checker ctx node operator args effects
	                                    capabilities return-type spec)
	              (record-safe1-call! checker ctx node operator args effects
	                                  capabilities return-type spec)
	              (record-safe-memory-call! checker ctx node operator args effects
	                                        capabilities return-type spec)
	              (record-safe6-call! checker ctx node operator args effects
	                                  capabilities return-type spec)
	              (record-boundary-safety-call! checker ctx node operator args
	                                            effects capabilities
	                                            return-type spec)
		              (record-safe-capability-call! checker ctx node operator args
		                                            effects capabilities
		                                            return-type spec)
		              (record-safety-conformance-call! checker ctx node operator args
		                                               effects capabilities
		                                               return-type spec)
		              (when (or (= "Dynamic" return-type) (= 'dynamic/cast operator))
	                (record-checker! checker :dynamic-boundary-records
	                                 {:node-id (:node-id node)
                                  :profile (:profile @ctx)
                                  :boundary operator
                                  :effects (:effects spec #{})
                                  :runtime-check (when (= 'dynamic/cast operator) :checked)}))
              (when (= 'dynamic/cast operator)
                (record-checker! checker :runtime-check-records
                                 {:node-id (:node-id node)
                                  :kind :dynamic-cast
                                  :target-type return-type
                                  :source-type (:type (second args))
                                  :status :runtime-checked}))
              (when (contains? #{'schema/derive 'schema/validate} operator)
                (record-checker! checker :schema-type-links
                                 {:node-id (:node-id node)
                                  :schema (:value (first args))
                                  :type return-type
                                  :schema-identity (:value (first args))
                                  :validation-boundary (if (= 'schema/validate operator)
                                                         :validated
                                                         :preserved)
                                  :nullability :preserved
                                  :optionality :preserved
                                  :bounds :preserved
                                  :refinements :preserved
                                  :taint :preserved}))
              (when (= 'generic/id operator)
                (record-checker! checker :generic-instantiation-records
                                 {:node-id (:node-id node)
                                  :generic 'generic/id
                                  :type-arguments (mapv :type args)
                                  :result-type return-type}))
              (record-dispatch-call! checker node operator args effects capabilities)
              (record-error-call! checker node operator args effects capabilities)
              (typed-fact checker node return-type effects capabilities
                          {:children args
                           :callee operator}))))))))

(def l7-required-pattern-families
  [:literal :record :map :vector :constructor :enum :guard :default :schema
   :linear])

(defn wildcard-pattern?
  [pattern]
  (= '_ pattern))

(defn constructor-pattern?
  [pattern]
  (and (seq? pattern) (symbol? (first pattern))))

(defn constructor-name
  [pattern]
  (when (constructor-pattern? pattern)
    (name (first pattern))))

(defn pattern-kind
  [pattern]
  (cond
    (wildcard-pattern? pattern) :wildcard
    (vector? pattern) :vector
    (map? pattern) :map
    (constructor-pattern? pattern) :constructor
    (or (nil? pattern) (true? pattern) (false? pattern)
        (number? pattern) (string? pattern) (keyword? pattern)) :literal
    (symbol? pattern) :binding
    :else :unknown))

(defn pattern-family
  [pattern]
  (cond
    (wildcard-pattern? pattern) :default
    (and (constructor-pattern? pattern)
         (#{"Borrow" "MoveLinear"} (constructor-name pattern))) :linear
    (and (constructor-pattern? pattern)
         (#{"Vec3" "User"} (constructor-name pattern))) :record
    (constructor-pattern? pattern) :constructor
    (keyword? pattern) :enum
    :else (pattern-kind pattern)))

(defn pattern-bindings
  [pattern]
  (cond
    (or (wildcard-pattern? pattern)
        (nil? pattern)
        (true? pattern)
        (false? pattern)
        (number? pattern)
        (string? pattern)
        (keyword? pattern)) []
    (symbol? pattern) [pattern]
    (vector? pattern) (mapcat pattern-bindings pattern)
    (map? pattern) (mapcat pattern-bindings (vals pattern))
    (constructor-pattern? pattern) (mapcat pattern-bindings (rest pattern))
    :else []))

(defn duplicate-binding
  [pattern]
  (first (for [[binding n] (frequencies (pattern-bindings pattern))
               :when (> n 1)]
           binding)))

(defn literal-pattern-type
  [pattern]
  (when (= :literal (pattern-kind pattern))
    (literal-type pattern)))

(defn literal-pattern-incompatible?
  [scrutinee-type pattern]
  (let [pattern-type (literal-pattern-type pattern)]
    (and pattern-type
         (#{"Nil" "Boolean" "Integer" "String" "Keyword"} scrutinee-type)
         (not= pattern-type scrutinee-type))))

(defn untrusted-type?
  [type-name]
  (and (string? type-name) (str/starts-with? type-name "Untrusted[")))