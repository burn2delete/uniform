

(defn p15-s23-c11-mir-map-blocks
  [mir transform]
  (let [entrypoint (first (keys (:functions mir)))]
    (update-in
     mir [:functions entrypoint :blocks]
     (fn [blocks]
       (into (empty blocks)
             (map (fn [[block-id block]]
                    [block-id (transform block)]))
             blocks)))))

(defn p15-s23-c11-mir-scope-record
  []
  {:task :FL-P06-T03
   :slice :authenticated-checked-core-to-target-independent-mir
   :maximum-conditionals 1
   :maximum-module-carrier-nodes p15-s23-c11-mir-max-carrier-nodes
   :maximum-final-artifact-carrier-nodes
   p15-s23-c11-mir-max-final-artifact-carrier-nodes
   :maximum-carrier-depth p15-s23-c11-mir-max-carrier-depth
   :whole-c11? false
   :target-lowering-credit? false
   :backend-credit? false
   :llvm-credit? false
   :release-credit? false
   :whole-language? false
   :self-hosted? false})

(defn p15-s23-c11-independent-verifier-record
  [verifier]
  (let [base
        (assoc verifier
               :artifact :gravity/c11-independent-verifier-report
               :verification-status :passed
               :verified-semantic-constructor :gravity-source
               :verifier-implementation :clojure-stage0-seed
               :verification-tcb :clojure-stage0-rule-runner)
        report-hash (p15-s23-c11-mir-digest base)
        report-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/c11-independent-verifier-report-id
          :report-hash report-hash})]
    (assoc base
           :report-id report-id
           :report-hash report-hash)))

(defn p15-s23-c11-mir-module-content-id
  [mir]
  (p15-s23-c11-mir-digest
   {:kind :gravity/c11-path-neutral-mir-content
    :module
     (p15-s23-c11-mir-path-neutral-value
     (dissoc mir :b1-preflight :pass-execution-record))}))

(defn p15-s23-c11-mir-pass-execution-record
  [mir checked-core verifier-record]
  (let [base
        {:artifact :gravity/build-mir-pass-execution-record
         :pass-id :c11-build-mir-bounded-slice
         :pass-contract-hash
         (p15-s23-c11-mir-digest (p15-s23-c11-mir-pass-contract-record))
         :input-artifact-id (:artifact-id checked-core)
         :output-content-id (p15-s23-c11-mir-module-content-id mir)
         :verifier-report-id (:report-id verifier-record)
         :verifier-report-hash (:report-hash verifier-record)
         :diagnostics []
         :status :verified-by-independent-seed}]
    (assoc base :record-id
           (p15-s23-c11-mir-digest
            {:kind :gravity/build-mir-pass-execution-record-id
             :record base}))))

(defn p15-s23-c11-b1-candidate-record
  [mir checked-core verifier-record scope]
  {:artifact :gravity/verified-mir-candidate-for-b1
   :status :verified-mir-candidate-for-b1
   :input-kind :gravity/mir
   :requires-verifier-status :passed
   :mir-verifier-status :passed
   :binding
   {:mir-content-id (p15-s23-c11-mir-module-content-id mir)
    :checked-core-artifact-id (:artifact-id checked-core)
    :independent-verifier-report-id (:report-id verifier-record)
    :independent-verifier-report-hash (:report-hash verifier-record)
    :pass-execution-record-id
    (get-in mir [:pass-execution-record :record-id])
    :scope-hash (p15-s23-c11-mir-digest scope)}
   :target-lowering-credit? false
   :backend-credit? false
   :llvm-credit? false})

(defn p15-s23-c11-mir-finalize
  [mir checked-core verifier-record scope]
  (let [verified
        (-> mir
            (p15-s23-c11-mir-map-blocks
             (fn [block]
               (-> block
                   (update :instructions
                           #(mapv (fn [operation]
                                    (assoc operation
                                           :verifier-status :passed))
                                  %))
                   (update :terminator assoc
                           :verifier-status :passed))))
            (assoc :verification-status :passed)
            (assoc :b1-preflight nil)
            (assoc :pass-execution-record nil))
        with-execution
        (assoc verified :pass-execution-record
               (p15-s23-c11-mir-pass-execution-record
                verified checked-core verifier-record))]
    (assoc with-execution :b1-preflight
           (p15-s23-c11-b1-candidate-record
            with-execution checked-core verifier-record scope))))

(defn p15-s23-c11-mir-pending-view
  [mir]
  (-> mir
      (p15-s23-c11-mir-map-blocks
       (fn [block]
         (-> block
             (update :instructions
                     #(mapv (fn [operation]
                              (assoc operation :verifier-status :pending))
                            %))
             (update :terminator assoc :verifier-status :pending))))
      (assoc :verification-status :pending)
      (assoc :pass-execution-record
             {:artifact :gravity/build-mir-pass-execution-record
              :pass-id :c11-build-mir-bounded-slice
              :pass-contract-hash :pending-source-binding
              :input-artifact-id (:source-core mir)
              :output-content-id :pending-independent-verifier
              :verifier-report-id :pending-independent-verifier
              :verifier-report-hash :pending-independent-verifier
              :diagnostics []
              :status :constructed-unverified
              :record-id :pending-independent-verifier})
      (assoc :b1-preflight
             {:input-kind :gravity/mir
              :requires-verifier-status :passed
              :status :pending-c11-verifier
              :backend-credit? false})))

(defn p15-s23-c11-mir-source-rule-record
  [binding]
  {:artifact :gravity/c11-pinned-source-rule
   :owner :gravity-source
   :source-content-hash (:source-content-hash binding)
   :source-byte-count (:source-byte-count binding)
   :plan-semantic-hash (:plan-semantic-hash binding)
   :functions-semantic-hash (:functions-semantic-hash binding)
   :builder-function p15-s23-c11-mir-builder-function
   :builder-semantic-hash (:builder-semantic-hash binding)
   :verifier-function p15-s23-c11-mir-verifier-function
   :verifier-semantic-hash (:verifier-semantic-hash binding)
   :function-shapes (:function-shapes binding)
   :compiled-by :clojure-stage0-seed
   :executed-by :clojure-stage0-rule-runner
   :clojure-seed-boundary? true
   :self-hosted? false})

(def p15-s23-c11-legacy-effectful-context-keys
  #{:source-path :source-text :source-content-hash
    :requested-target :authority-record})

(def p15-s23-c11-ingress-map-classes
  ;; The ingress classifier must not invoke attacker-controlled ordering code.
  ;; In particular PersistentTreeMap lookup can execute a supplied comparator.
  ;; Genuine public C6-C10 artifacts/contexts use only the two comparator-free
  ;; built-in persistent map representations below.
  #{"clojure.lang.PersistentArrayMap"
    "clojure.lang.PersistentHashMap"})

(defn p15-s23-c11-exact-bounded-map?
  [candidate maximum-count]
  (and (map? candidate)
       (contains? p15-s23-c11-ingress-map-classes
                  (.getName (class candidate)))
       (nil? (meta candidate))
       (<= (count candidate) maximum-count)))

(defn p15-s23-c11-ingress-source-path
  [context]
  (if (p15-s23-c11-exact-bounded-map? context 5)
    (p15-s23-c11-mir-safe-source-path (:source-path context))
    "<c11-mir>"))

(defn p15-s23-c11-carrier-sorted-policy
  [checked-core]
  (if (and (p15-s23-c11-exact-bounded-map? checked-core 128)
           (= :gravity/p15-s23-stage2-closed-checked-core-artifact
              (:kind checked-core)))
    :default-only
    :reject))