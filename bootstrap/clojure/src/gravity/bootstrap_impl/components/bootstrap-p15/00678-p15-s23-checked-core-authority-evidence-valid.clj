

(defn p15-s23-checked-core-authority-evidence-valid?
  [evidence]
  (and
   (map? evidence)
   (= p15-s23-checked-core-authority-evidence-keys
      (set (keys evidence)))
   (= :gravity/p15-s23-checked-core-authority-evidence (:kind evidence))
   (= :authenticated-and-consumed (:status evidence))
   (string? (:source-content-hash evidence))
   (string? (:plan-id evidence))
   (symbol? (:module evidence))
   (set? (:structural-operation-set evidence))
   (set? (:required-effects evidence))
   (set? (:required-capabilities evidence))
   (map? (:provider-bindings evidence))
   (map? (:grant-bindings evidence))
   (= (set (keys (:provider-bindings evidence)))
      (set (keys (:grant-bindings evidence))))
   (= (:required-capabilities evidence)
      (set (keys (:provider-bindings evidence)))
      (set (keys (:grant-bindings evidence))))
   (every? set? (map evidence
                     [:runtime-provider-ids :runtime-grant-ids
                      :handler-provider-ids :handler-grant-ids]))
   (true? (:single-invocation? evidence))
   (true? (:reference-interpreter? evidence))
   (false? (:deployment-runtime? evidence))
   (false? (:live-external-io? evidence))
   (false? (:active-authority? evidence))
   (true? (:non-authorizing-projection? evidence))
   (= (:evidence-id evidence)
      (p15-s23-closed-core-digest (dissoc evidence :evidence-id)))))

(def p15-s23-checked-core-reference-execution-evidence-keys
  #{:kind :authority-record-id :authority-evidence-id :plan-id
    :runtime-artifact-hash :runtime-function :runtime-function-hash
    :adapter-record-hash :decision-record-ids :action-record-ids
    :entrypoint :entrypoint-result :entrypoint-result-hash
    :transcript :transcript-hash :transcript-mode :capture-action-status
    :invocation-count :invocation-count-scope
    :verification-replays-excluded? :reference-interpreter?
    :deployment-runtime? :live-external-io? :clojure-seed-boundary?
    :self-hosted? :status :evidence-id})

(defn p15-s23-checked-core-reference-execution-evidence
  [authority-evidence adapter-output]
  (let [result (:result adapter-output)
        adapter-record (:adapter-record adapter-output)
        transcript (:stdout result)
        capture-action
        (first
         (filter #(= :gravity.reference/action-transcript-capture
                     (:action-id %))
                 (:action-records adapter-record)))
        base
        {:kind :gravity/p15-s23-checked-core-reference-execution-evidence
         :authority-record-id (:authority-record-id authority-evidence)
         :authority-evidence-id (:evidence-id authority-evidence)
         :plan-id (:plan-id adapter-record)
         :runtime-artifact-hash (:runtime-artifact-hash adapter-record)
         :runtime-function (:function adapter-record)
         :runtime-function-hash (:function-hash adapter-record)
         :adapter-record-hash (:record-hash adapter-record)
         :decision-record-ids
         (mapv :decision-id (:decision-records adapter-record))
         :action-record-ids (mapv :record-id (:action-records adapter-record))
         :entrypoint (:entrypoint result)
         :entrypoint-result (:entrypoint-result result)
         :entrypoint-result-hash
         (p15-s23-closed-core-digest (:entrypoint-result result))
         :transcript transcript
         :transcript-hash (str "sha256:" (sha256-hex transcript))
         :transcript-mode :in-memory-reference-only
         :capture-action-status
         (if capture-action (:action-status capture-action) :not-applicable)
         :invocation-count 1
         :invocation-count-scope :authoritative-artifact-construction
         :verification-replays-excluded? true
         :reference-interpreter? true
         :deployment-runtime? false
         :live-external-io? false
         :clojure-seed-boundary? true
         :self-hosted? false
         :status :complete}]
    (assoc base :evidence-id (p15-s23-closed-core-digest base))))

(defn p15-s23-checked-core-reference-execution-evidence-valid?
  [evidence authority-evidence]
  (and
   (map? evidence)
   (= p15-s23-checked-core-reference-execution-evidence-keys
      (set (keys evidence)))
   (= :gravity/p15-s23-checked-core-reference-execution-evidence
      (:kind evidence))
   (= (:authority-record-id authority-evidence)
      (:authority-record-id evidence))
   (= (:evidence-id authority-evidence)
      (:authority-evidence-id evidence))
   (= p15-s23-stage2-runtime-artifact-expected-artifact-hash
      (:runtime-artifact-hash evidence))
   (= p15-s23-stage2-runtime-artifact-closed-plan-function
      (:runtime-function evidence))
   (= (get p15-s23-reference-runtime-expected-function-hashes
           p15-s23-stage2-runtime-artifact-closed-plan-function)
      (:runtime-function-hash evidence))
   (string? (:transcript evidence))
   (= (:transcript-hash evidence)
      (str "sha256:" (sha256-hex (:transcript evidence))))
   (= (:entrypoint-result-hash evidence)
      (p15-s23-closed-core-digest (:entrypoint-result evidence)))
   (vector? (:decision-record-ids evidence))
   (every? string? (:decision-record-ids evidence))
   (vector? (:action-record-ids evidence))
   (every? string? (:action-record-ids evidence))
   (contains? #{:committed :not-invoked :not-applicable}
              (:capture-action-status evidence))
   (= 1 (:invocation-count evidence))
   (= :authoritative-artifact-construction
      (:invocation-count-scope evidence))
   (true? (:verification-replays-excluded? evidence))
   (true? (:reference-interpreter? evidence))
   (false? (:deployment-runtime? evidence))
   (false? (:live-external-io? evidence))
   (true? (:clojure-seed-boundary? evidence))
   (false? (:self-hosted? evidence))
   (= :complete (:status evidence))
   (= (:evidence-id evidence)
      (p15-s23-closed-core-digest (dissoc evidence :evidence-id)))))

(defn p15-s23-checked-core-expected-reference-execution-evidence
  [authority-evidence execution-evidence]
  (let [writes-stdout?
        (contains? (:structural-operation-set authority-evidence) :println)
        transcript (:transcript execution-evidence)
        result
        {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record
         :clojure-seed-boundary? true
         :entrypoint (:entrypoint execution-evidence)
         :entrypoint-result (:entrypoint-result execution-evidence)
         :self-hosted? false
         :status :complete
         :stdout transcript}
        adapter-record
        (p15-s23-reference-runtime-success-adapter-record
         (:plan-id authority-evidence)
         (:source-content-hash authority-evidence)
         writes-stdout? (boolean (seq transcript)))]
    (p15-s23-checked-core-reference-execution-evidence
     authority-evidence {:result result :adapter-record adapter-record})))

(defn p15-s23-checked-core-bind-execution-audit-to-facts
  [facts nodes execution-evidence]
  (if-not (map? execution-evidence)
    facts
    (let [decision-ids (:decision-record-ids execution-evidence)
          action-ids (:action-record-ids execution-evidence)]
      (update
       facts :safety-facts
       (fn [safety-facts]
         (reduce
          (fn [records node]
            (if (= :runtime-checked (get-in node [:safety :outcome]))
              (let [println? (= :println (:source-operation node))
                    capture-status (:capture-action-status execution-evidence)
                    check (get-in node [:safety :check])
                    audit-base
                    {:authority-record-id
                     (:authority-record-id execution-evidence)
                     :authority-evidence-id
                     (:authority-evidence-id execution-evidence)
                     :adapter-record-hash
                     (:adapter-record-hash execution-evidence)
                     :execution-evidence-id (:evidence-id execution-evidence)
                     :runtime-check-id (:check-id check)
                     :capability-proof-id (:capability-proof-id check)
                     :program-provider-selection-id
                     (:program-provider-selection-id check)
                     :program-provider-id (:program-provider-id check)
                     :program-grant-id (:program-grant-id check)
                     :runtime-provider-id (:runtime-provider-id check)
                     :runtime-grant-id (:runtime-grant-id check)
                     :runtime-handler-provider-id
                     (:runtime-handler-provider-id check)
                     :runtime-handler-grant-id
                     (:runtime-handler-grant-id check)
                     :aggregate-decision-count (count decision-ids)
                     :aggregate-action-count (count action-ids)
                     :structural-operation (:source-operation node)
                     :audit-scope :invocation-aggregate
                     :node-invocation-state :unknown-or-not-observed
                     :aggregate-action-status
                     (if println?
                       capture-status
                       :managed-allocator-authority-committed)
                     :transcript-hash (:transcript-hash execution-evidence)
                     :live-external-io? false
                     :status :bound-to-single-reference-execution}
                    audit
                    (assoc audit-base :audit-id
                           (p15-s23-closed-core-digest audit-base))]
                (assoc-in records [(:node-id node) :runtime-check-audit]
                          audit))
              records))
          safety-facts nodes))))))