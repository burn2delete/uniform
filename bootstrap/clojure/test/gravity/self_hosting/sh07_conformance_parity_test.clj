(ns gravity.self-hosting.sh07-conformance-parity-test
  "Focused tests for the bounded SH-07 semantic parity projection.

  These tests exercise selection, normalization, and an injected oracle.  They
  do not launch the full fixture table; a representative real legacy oracle
  invocation is an explicit, separately coordinated smoke check."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-conformance-parity :as parity]
            [gravity.self-hosting.sh07-conformance-table :as table]))

(defn- error-id
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:id (ex-data error)))))

(defn- fake-oracle
  "A deterministic injected oracle for bounded harness tests.

  It mirrors the shape of the legacy C6 diagnostic while deriving every
  expected field from the reviewed row.  The production default remains the
  real `gravity.bootstrap/sh07-core-file-artifact` adapter."
  [_path row]
  (if (= :accepted (:status row))
    {:status :accepted
     :artifact-id (str "sha256:" (apply str (repeat 64 "a")))
     :diagnostics []}
    (let [oracle (:oracle row)
          facts (merge (:facts oracle)
                       (when-let [reason (:reason oracle)] {:reason reason})
                       (when-let [semantic-rule (:semantic-rule oracle)]
                         {:semantic-rule semantic-rule}))]
      {:exception-data
       {:artifact :gravity/sh07-core-diagnostic
        :rule (:rule oracle)
        :stage (:stage oracle)
        :severity (:severity oracle)
        :remediation (:remediation oracle)
        :diagnostic-id-request (str "sha256:" (apply str (repeat 64 "b")))
        :facts facts}})))

(defn- private-var
  "Resolve a parity helper without widening its production visibility."
  [name]
  (or (ns-resolve 'gravity.self-hosting.sh07-conformance-parity name)
      (throw (ex-info "Focused parity helper is absent" {:name name}))))

(defn- b15-enriched-diagnostic
  "Construct one producer-shaped B15 diagnostic for contract-only tests.

  This is a bounded injected value; it does not invoke the SH-07 compiler or
  either co-canonical fixture.  The extra lineage facts mirror the producer's
  enriched payload while the row oracle remains intentionally compact."
  []
  (let [oracle (:oracle (parity/reviewed-row
                         "sh07-b15/keyword-lookup-zero-arity"))]
    (parity/normalize-diagnostic
     {:artifact :gravity/sh07-core-diagnostic
      :rule (:rule oracle)
      :stage (:stage oracle)
      :severity (:severity oracle)
      :lowering-rule :sh07-b47-function-call-recursion-products
      :syntax-id
      "sha256:1111111111111111111111111111111111111111111111111111111111111111"
      :form-id
      "sha256:2222222222222222222222222222222222222222222222222222222222222222"
      ;; B15 rejects before a core node is allocated; C6 still requires the
      ;; key so the nil is an explicit producer decision rather than omission.
      :core-node-id nil
      :source-span {:source "/tmp/sh07-b15/keyword-lookup-zero-arity.gravity"
                    :line-start 24
                    :column-start 1}
      :generated-origin-chain []
      :namespace 'self-hosting.sh07-b15.reject.keyword-lookup-zero-arity
      :profile :meta
      :target :jvm
      :facts {:reason (:reason oracle)
              :semantic-rule nil
              :rule-specific (merge (:facts oracle)
                                    {:reason (:reason oracle)
                                     :operator :missing-map})
              :source-revision-id
              "sha256:rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr"
              :sh06-artifact-id
              "sha256:ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"
              :fail-closed true}
      :remediation (:remediation oracle)
      :diagnostic-id-request
      "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})))

(deftest reviewed-row-addresses-stable-fixture
  (let [row (parity/reviewed-row "sh07-b1/control-flow-order")]
    (is (= "sh07-b1/control-flow-order" (:id row)))
    (is (= :accepted (:status row)))
    (is (= "control-flow-order.gravity" (:gravity row)))
    (is (= "control-flow-order.qst" (:qst row)))))

(deftest all-reviewed-rows-split-into-bounded-suite-batches
  (let [suite-ids table/expected-suite-ids
        batches (mapcat #(parity/exact-batch-row-ids % 8) suite-ids)]
    (is (= (set (map :id (table/rows (table/table))))
           (set (mapcat identity batches))))
    (is (= (count (mapcat identity batches))
           (count (set (mapcat identity batches)))))
    (is (every? #(<= 1 (count %) parity/maximum-batch-size) batches))
    (is (= "SH07-PARITY-BATCH-BOUND"
           (error-id #(parity/exact-batch-row-ids "sh07-b1"
                                                  (inc parity/maximum-batch-size)))))))

(deftest representative-accepted-rejected-parity-is-exact
  (let [report
        (parity/prove-exact-batch
         "sh07-b1"
         ["sh07-b1/control-flow-order" "sh07-b1/if-missing-branch"]
         {:oracle fake-oracle
          :oracle-id parity/simulated-oracle-id
          :simulated? true})]
    (is (= parity/parity-schema (:schema report)) report)
    (is (= :simulated (:status report)) report)
    (is (false? (:authoritative? report)) report)
    (is (= {:selected 2 :passed 2 :failed 0} (:counts report)) report)
    (is (= 2 (count (:records report))))
    (is (every? true? (map :co-canonical? (:records report))))
    (is (every? false? (map :exact? (:records report))))
    (is (every? true? (map :simulated-parity? (:records report))))
    (is (every? #(= (:row-id %) (get-in % [:legacy :row-id]))
                (:records report)))))

(deftest parity-mismatch-is-reported-without-authority
  (let [wrong-oracle (fn [_path _row]
                       {:status :accepted
                        :artifact-id (str "sha256:" (apply str (repeat 64 "c")))
                        :diagnostics []})
        report (parity/prove-exact-batch
                "sh07-b1"
                ["sh07-b1/if-missing-branch"]
                {:oracle wrong-oracle
                 :oracle-id parity/simulated-oracle-id
                 :simulated? true})]
    (is (= :simulated (:status report)) report)
    (is (= {:selected 1 :passed 0 :failed 1} (:counts report)) report)
    (is (false? (:authoritative? report)) report)
    (is (false? (get-in report [:records 0 :exact?])))))

(deftest real-legacy-oracle-without-lease-is-blocked
  (let [suite (some #(when (= "sh07-b1" (:id %)) %)
                    (:suites (table/table)))
        row (parity/reviewed-row "sh07-b1/control-flow-order")
        result (parity/run-row suite row)]
    (is (= :blocked (get-in result [:outcome :status])) result)
    (is (true? (:blocked? result)) result)
    (is (false? (:simulated? result)) result)
    (is (= "SH07-PARITY-RESOURCE"
           (get-in result [:outcome :diagnostics 0 :rule])) result)))

(deftest diagnostic-normalization-retains-order-and-path-neutrality
  (let [value (parity/normalize-diagnostic
               {:artifact :gravity/sh07-core-diagnostic
                :rule "C6-CORE-SHAPE"
                :stage :core-lowering
                :severity :error
                :source-span {:source "/checkout/a.gravity" :line-start 1}
                :facts {:reason :if-arity :fail-closed true}
                :remediation "fix"
                :diagnostic-id-request "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})]
    (is (= "<co-canonical-source>"
           (get-in value [:source-span :source])))
    (is (= :if-arity (:reason value)))
    (is (= #{:artifact :rule :stage :severity :source-span :facts :remediation
             :diagnostic-id-request :reason :fail-closed?}
           (set (keys value))))))

(deftest b15-compact-oracle-accepts-complete-enriched-diagnostic
  (let [row (parity/reviewed-row "sh07-b15/keyword-lookup-zero-arity")
        outcome {:status :rejected
                 :diagnostics [(b15-enriched-diagnostic)]}
        table-parity (private-var 'table-parity)
        contract-complete (private-var 'diagnostic-contract-complete?)
        diagnostic (first (:diagnostics outcome))
        oracle (:oracle row)
        result (table-parity row outcome)]
    (is (true? (:status? result)) result)
    (is (true? (:diagnostics? result)) result)
    (is (true? (:declared-contract-match? result)) result)
    (is (true? (:diagnostic-contract-complete? result)) result)
    (is (true? (:exact? result)) result)
    ;; Keep the compact oracle compact, but preserve conditional exactness if
    ;; a future reviewed row elects to carry its own diagnostic identity.
    (is (true? (contract-complete
                (assoc oracle :diagnostic-id-request
                       (:diagnostic-id-request diagnostic))
                diagnostic)))
    (is (false? (contract-complete
                 (assoc oracle :diagnostic-id-request
                        "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
                 diagnostic)))))

(deftest b15-diagnostic-contract-rejects-missing-or-invalid-fields
  (let [row (parity/reviewed-row "sh07-b15/keyword-lookup-zero-arity")
        complete (b15-enriched-diagnostic)
        table-parity (private-var 'table-parity)
        tamperings
        {"missing namespace" #(dissoc % :namespace)
         "missing syntax id" #(dissoc % :syntax-id)
         "missing form id" #(dissoc % :form-id)
         "missing core node id" #(dissoc % :core-node-id)
         "nil syntax id" #(assoc % :syntax-id nil)
         "nil form id" #(assoc % :form-id nil)
         "syntax id is not a string" #(assoc % :syntax-id 1)
         "form id is not a string" #(assoc % :form-id 1)
         "core node id has invalid type" #(assoc % :core-node-id 1)
         "missing lowering rule" #(dissoc % :lowering-rule)
         "missing diagnostic id" #(dissoc % :diagnostic-id-request)
         "invalid diagnostic id" #(assoc % :diagnostic-id-request "not-a-sha256-id")
         "malformed source span" #(assoc % :source-span [:not-a-span])
         "source span source is not a string"
         #(assoc-in % [:source-span :source] 7)
         "malformed generated origin chain"
         #(assoc % :generated-origin-chain {:not "a-vector"})
         "generated origin entry is not a map"
         #(assoc % :generated-origin-chain [:not-a-map])
         "malformed facts" #(assoc % :facts [:not-a-facts-map])
         "nil lowering rule" #(assoc % :lowering-rule nil)
         "wrong lowering rule"
         #(assoc % :lowering-rule :sh07-b46-public-routing)
         "mismatched arity fact"
         #(assoc-in % [:facts :rule-specific :actual-operand-count] 1)}]
    (doseq [[label tamper] tamperings]
      (testing label
        (let [diagnostic (tamper complete)
              result (table-parity row {:status :rejected
                                         :diagnostics [diagnostic]})]
          (is (false? (:diagnostic-contract-complete? result)) result)
          (is (false? (:exact? result)) result))))))
