(ns gravity.c15-c16-pass-cache.validation
  "Exact validation and diagnostics for C15/C16 cache inputs.")

(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")

(def ^:private context-fields
  #{:c14-artifact-id :semantic-bindings :dependency-graph-id
    :build-effect-replay-id :profile-id :target-id :policy-ids :provenance
    :diagnostic-stream-ids :producer-binding-ids :validation-binding-ids
    :authority-scope})

(def ^:private operation-fields
  #{:produce-c15! :validate-c15! :produce-c16! :validate-c16!
    :artifact-id-of})

(def ^:private stage-binding-fields #{:c15 :c16})

(defn fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id
                          :stage :c15-c16-pass-cache
                          :release-authority? false
                          :proof-authority? false
                          :self-hosting-authority? false}
                         data))))

(defn sha256-id?
  [value]
  (and (string? value) (boolean (re-matches sha256-pattern value))))

(defn require-sha256!
  [field value]
  (when-not (sha256-id? value)
    (fail! "C16-KEY" "C15/C16 cache identity must be lowercase SHA-256"
           {:field field :value value}))
  value)

(defn exact-map!
  [value expected field]
  (when-not (and (map? value) (= expected (set (keys value))))
    (fail! "C16-KEY" "C15/C16 cache input has unknown or missing fields"
           {:field field :expected expected
            :observed (when (map? value) (set (keys value)))}))
  value)

(defn- validate-stage-bindings!
  [field bindings]
  (exact-map! bindings stage-binding-fields field)
  (doseq [[stage value] bindings]
    (require-sha256! [field stage] value))
  bindings)

(defn validate-context!
  [context]
  (exact-map! context context-fields :context)
  (require-sha256! :c14-artifact-id (:c14-artifact-id context))
  (exact-map! (:semantic-bindings context)
              #{:compiler-id :capability-policy-id :facet-set-id
                :provider-manifest-id :package-lock-id :diagnostic-schema-id}
              :semantic-bindings)
  (doseq [[field value] (:semantic-bindings context)]
    (require-sha256! field value))
  (doseq [field [:dependency-graph-id :build-effect-replay-id
                 :profile-id :target-id]]
    (require-sha256! field (get context field)))
  (when-not (and (vector? (:policy-ids context))
                 (= (:policy-ids context) (vec (sort (:policy-ids context))))
                 (= (count (:policy-ids context))
                    (count (distinct (:policy-ids context))))
                 (every? sha256-id? (:policy-ids context)))
    (fail! "C16-KEY" "C15/C16 policy identities must be sorted and unique"
           {:policy-ids (:policy-ids context)}))
  (validate-stage-bindings! :diagnostic-stream-ids
                            (:diagnostic-stream-ids context))
  (validate-stage-bindings! :producer-binding-ids
                            (:producer-binding-ids context))
  (validate-stage-bindings! :validation-binding-ids
                            (:validation-binding-ids context))
  (when-not (keyword? (:authority-scope context))
    (fail! "C16-POLICY" "C15/C16 authority scope must be a keyword"
           {:authority-scope (:authority-scope context)}))
  context)

(defn validate-operations!
  [operations]
  (exact-map! operations operation-fields :operations)
  (doseq [[field operation] operations]
    (when-not (fn? operation)
      (fail! "C16-ENTRY" "C15/C16 cache operation must be a function"
             {:field field})))
  operations)
