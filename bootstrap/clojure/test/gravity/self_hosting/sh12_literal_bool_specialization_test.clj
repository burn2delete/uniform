(ns gravity.self-hosting.sh12-literal-bool-specialization-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(defn- helper [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test name)))

(defn- existing [name] ((helper 'existing) name))
(defn- invoke [name & args] (apply (helper 'invoke) name args))

(defn- source-pair [condition extension]
  ;; Current original source/C6 replay, with actual emitter identities.
  (binding [bootstrap/*p15-s23-stage2-compiler-artifact-binding* @(helper 'current-emitter)]
    (let [path (str (.resolve @(existing 'root)
                             (str "bootstrap/clojure/fixtures/self-hosting/sh-12/literal-bool-specialization/"
                                  condition "." extension)))
          text (slurp path)
          reader (str (.resolve @(existing 'root) "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
          products ((existing 'parameter-source-products) path text nil reader)
          call (existing 'invoke) plan (existing 'parameter-c6-plan)
          raw (call plan 'c6-c10-build-checked-core-template [(:envelope products)])
          fresh ((existing 'parameter-source-products) path text (:compiled-binding-data products) reader)
          replay (call plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
          upstream (call plan 'c6-c10-verify-checked-core-template
                         [(:envelope fresh) (:artifact-template raw) (:digest-requests replay)])
          sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (bootstrap/p15-s23-c6c10-public-artifact
                context (:compiled-binding-data products) (:ingress products) sealed)]
      {:raw raw :upstream upstream :core core :before (invoke 'sh12-build-branch-join-mir core)})))

(defn- specialize [before condition]
  ;; Independent test oracle: use original CFG predecessor/value pairings.
  ;; This does not call the C11 expectation helper or any C13 implementation.
  (let [entry (get-in before [:functions 'main :entry])
        incoming (get-in before [:control-flow-graph :join :incoming])
        [then-id else-id] (mapv :predecessor incoming)
        selected (if condition then-id else-id) dead (if condition else-id then-id)
        join (get-in before [:control-flow-graph :join :block-id])
        term-path [:functions 'main :blocks entry :terminator]
        condition-id (first (get-in before (conj term-path :operands)))
        term-id (get-in before (conj term-path :terminator-id))
        remove-condition-use #(filterv (fn [use] (not (and (= :terminator (:consumer-kind use))
                                                           (= term-id (:consumer-id use))))) %)
        dominators {entry [entry] selected [entry selected] dead [dead] join [entry selected join]}]
    (-> (reduce-kv (fn [mir block dom] (assoc-in mir [:functions 'main :blocks block :dominators] dom))
                   before dominators)
        (update-in term-path merge {:kind :branch :operands [] :successors [selected] :facts {}})
        (assoc-in [:functions 'main :blocks entry :successors] [selected])
        (assoc-in [:functions 'main :blocks dead :predecessors] [])
        (update-in [:control-flow-graph :edges]
                   #(filterv (fn [edge] (not (and (= entry (:from edge)) (= dead (:to edge))))) %))
        (assoc-in [:control-flow-graph :dominance] dominators)
        (update-in [:data-flow-graph :terminator-uses] remove-condition-use)
        (update-in [:data-flow-graph :values condition-id :uses] remove-condition-use))))

(defn- check-source [condition extension negatives?]
  (let [{:keys [raw upstream core before]} (source-pair (str condition) extension)
        after (specialize before condition)
        verify #(invoke 'sh12-verify-literal-bool-branch-specialization core before %)
        report (verify after)
        entry (get-in before [:functions 'main :entry])
        blocks-path [:functions 'main :blocks]
        incoming (get-in before [:control-flow-graph :join :incoming])
        [then-id else-id] (mapv :predecessor incoming)
        taken (if condition then-id else-id) dead (if condition else-id then-id)
        join (get-in before [:control-flow-graph :join :block-id])
        term-path (into blocks-path [entry :terminator])
        node-id (get-in core [:core-nodes 1 :node-id])
        execution (invoke 'sh13-run-branch-join-mir-with-arguments core before [])]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream)))
    (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core before))))
    (is (= :accepted (:status execution)))
    (is (= condition (get-in execution [:execution :value])))
    (is (= :passed (:status report)) (pr-str report))
    (is (= :gravity/sh12-literal-bool-branch-specialization-report (:artifact report)))
    (is (= 1 (:schema-version report)))
    (is (every? (set (:nonclaims report))
                [:independent-acceptance :optimizer-credit :admission-credit
                 :optimized-execution :c14-credit :backend-credit :self-hosted?]))
    (is (= (:artifact-id core) (get-in report [:input-bindings :checked-core-artifact-id])))
    (is (= (:module-id before) (get-in report [:input-bindings :before-module-id])
           (get-in report [:input-bindings :after-module-id])))
    (is (= :pending (:verification-status after)))
    (is (= :constructed-unverified (get-in after [:pass-execution-record :status])))
    (is (= 4 (count (get-in after blocks-path))))
    (is (= 6 (reduce + (map #(count (:instructions %)) (vals (get-in after blocks-path))))))
    (is (= 2 (count (get-in after [:control-flow-graph :join :incoming]))))
    (is (= incoming (get-in after [:control-flow-graph :join :incoming])))
    (is (= [entry taken join] (get-in after [:control-flow-graph :dominance join])))
    (is (= [] (get-in after (into blocks-path [dead :predecessors]))))
    (is (= 1 (count (get-in after [:data-flow-graph :values node-id :uses]))))
    (is (= :rejected (:status (invoke 'sh12-verify-branch-join-mir core after))))
    (is (= :rejected (:status (invoke 'sh13-run-branch-join-mir-with-arguments core after []))))
    (when negatives?
      (doseq [[label candidate code]
              [[:unchanged before "C11-VERIFY"]
               [:wrong-arm (specialize before (not condition)) "C11-VERIFY"]
               [:cfg (assoc after :control-flow-graph (:control-flow-graph before)) "C11-VERIFY"]
               [:dominance (assoc-in after [:control-flow-graph :dominance join] [entry join]) "C11-VERIFY"]
               [:block-dominance (assoc-in after (into blocks-path [dead :dominators]) [entry dead]) "C11-VERIFY"]
               [:predecessor (assoc-in after (into blocks-path [dead :predecessors]) [entry]) "C11-VERIFY"]
               [:successor (assoc-in after (into blocks-path [entry :successors]) [then-id else-id]) "C11-VERIFY"]
               [:term-use (assoc-in after [:data-flow-graph :terminator-uses]
                                   (get-in before [:data-flow-graph :terminator-uses])) "C11-VERIFY"]
               [:value-use (assoc-in after [:data-flow-graph :values node-id :uses]
                                    (get-in before [:data-flow-graph :values node-id :uses])) "C11-VERIFY"]
               [:ordinary-use (assoc-in after [:data-flow-graph :values node-id :uses] []) "C11-VERIFY"]
               [:ordinary-edge (assoc-in after [:data-flow-graph :edges] []) "C11-VERIFY"]
               [:join (assoc-in after [:control-flow-graph :join :incoming] [(first incoming)]) "C11-VERIFY"]
               [:dead-arm (update-in after blocks-path dissoc dead) "C11-VERIFY"]
               [:term-facts (assoc-in after (conj term-path :facts) (get-in before (conj term-path :facts))) "C11-VERIFY"]
               [:term-operand (assoc-in after (conj term-path :operands) [node-id]) "C11-VERIFY"]
               [:literal (update-in after (into blocks-path [entry :instructions 0 :constant-payload :value]) not) "C11-VERIFY"]
               [:arm (update-in after (into blocks-path [dead :instructions 0 :constant-payload :value]) not) "C11-VERIFY"]
               [:type (assoc-in after (into blocks-path [entry :instructions 0 :type]) :gravity/integer) "C11-TYPE"]
               [:effect (assoc-in after (into blocks-path [entry :instructions 0 :effects]) #{:io}) "C11-EFFECT"]
               [:origin (assoc-in after (into blocks-path [entry :instructions 0 :source]) {}) "C11-ORIGIN"]
               [:check (assoc after :runtime-check-table {:forged :passed}) "C11-SAFETY"]
               [:proof (assoc after :proof-certificate-table {:forged :passed}) "C11-SAFETY"]
               [:type-table (assoc after :type-table {}) "C11-TYPE"]
               [:effect-table (assoc after :effect-table {}) "C11-EFFECT"]
               [:safety-table (assoc after :safety-table {}) "C11-SAFETY"]
               [:profile (assoc after :profile :realtime) "C11-VERIFY"]
               [:target (assoc after :source-target :llvm) "C11-VERIFY"]
               [:identity (assoc after :module-id "other") "C11-VERIFY"]
               [:status (assoc after :verification-status :passed) "C11-VERIFY"]
               [:forged-report (assoc after :c13-report {:status :passed :checks [:all]}) "C11-VERIFY"]
               [:nil nil "C11-VERIFY"]
               [:false false "C11-VERIFY"]
               [:vector [] "C11-VERIFY"]
               [:malformed (assoc after :functions []) "C11-VERIFY"]]]
        (testing (name label)
          (let [rejected (verify candidate)]
            (is (= :rejected (:status rejected)))
            (is (= code (:diagnostic-id (first (:diagnostics rejected))))))))
      ;; Recompute, rather than consume a supplied :passed report.
      (is (= :rejected (:status (verify (assoc report :status :passed)))))
      (is (= "other" (get-in (verify (assoc after :module-id "other")) [:input-bindings :after-module-id])))
      (doseq [bad-core [(assoc core :profile :realtime)
                        (assoc-in core [:target-request-metadata :requested-target] :llvm)
                        (assoc-in core [:core-nodes 0 :source-operation] :local)
                        (assoc-in core [:core-nodes 5 :type :params] [:gravity/bool])]]
        (is (= :rejected (:status (invoke 'sh12-verify-literal-bool-branch-specialization
                                          bad-core before after)))))
      (is (= :rejected (:status (invoke 'sh12-verify-literal-bool-branch-specialization
                                        core (assoc before :extra :unverified) after))))
      (doseq [position (range 3)
              hostile [(iterate vector nil) (nth (iterate vector nil) 258)
                       (vec (repeat 1025 nil)) (zipmap (range 513) (repeat nil))
                       (vec (repeat 33 (vec (repeat 1024 nil))))]]
        (let [inputs (assoc [core before after] position hostile)
              rejected (apply invoke 'sh12-verify-literal-bool-branch-specialization inputs)]
          (is (= :rejected (:status rejected)))
          (is (= :bounded-specialization-input-carriers
                 (:missing-fact (first (:diagnostics rejected)))))))
      (is (= report (verify after))))))

(deftest sh12-literal-bool-specialization-true (check-source true "gravity" true))
(deftest sh12-literal-bool-specialization-false (check-source false "gravity" false))
(deftest sh12-literal-bool-specialization-true-qst (check-source true "qst" false))
(deftest sh12-literal-bool-specialization-false-qst (check-source false "qst" false))

(deftest sh12-literal-bool-specialization-rejects-valid-normalized-family
  (let [core ((existing 'checked-core) ".gravity")
        mir (invoke 'sh12-build-branch-join-mir core)]
    (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
    (let [report (invoke 'sh12-verify-literal-bool-branch-specialization core mir mir)]
      (is (= :rejected (:status report)))
      (is (= :original-zero-arity-literal-bool-diamond
             (:missing-fact (first (:diagnostics report))))))
    (doseq [diagnostics [42 [{:actual-source-path (apply str (repeat 4096 "x"))}]]]
      (let [before (assoc mir :diagnostics diagnostics)
            rejected (invoke 'sh12-verify-literal-bool-branch-specialization core before mir)]
        (is (= :rejected (:status rejected)))
        (is (= :original-mir-diagnostic-carrier (:missing-fact (first (:diagnostics rejected)))))
        (is (< (count (pr-str rejected)) 2048))))))

(defn- reject-original-nested [extension]
  (let [{:keys [raw upstream core before]} (source-pair "parameter-nested" extension)]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream)))
    (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core before))))
    (is (= 7 (count (get-in before [:functions 'main :blocks]))))
    (is (= 1 (count (get-in before [:functions 'main :params]))))
    (doseq [[argument value] [[true 22] [false 33]]]
      (let [result (invoke 'sh13-run-branch-join-mir-with-arguments core before [argument])]
        (is (= :accepted (:status result)))
        (is (= value (get-in result [:execution :value])))))
    (let [report (invoke 'sh12-verify-literal-bool-branch-specialization core before before)]
      (is (= :rejected (:status report)))
      (is (= :original-zero-arity-literal-bool-diamond
             (:missing-fact (first (:diagnostics report))))))))

(deftest sh12-literal-bool-specialization-rejects-original-nested-parameter
  (reject-original-nested "gravity"))
(deftest sh12-literal-bool-specialization-rejects-original-nested-parameter-qst
  (reject-original-nested "qst"))

