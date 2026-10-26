(ns gravity.self-hosting.sh01-native-bool-parameter-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.c6-parameter-reader-invocation-test]
            [gravity.self-hosting.sh12-branch-join-mir-test]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(defn- owner [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))
(defn- caller [name]
  (var-get (ns-resolve 'gravity.self-hosting.c6-parameter-reader-invocation-test name)))
(defn- diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error
                 (select-keys (ex-data error) [:id :missing-fact]))))
(defn- phase [label f]
  (prn {:native-parameter-phase label :event :begin}) (flush)
  (let [start (System/nanoTime) result (f)]
    (prn {:native-parameter-phase label :event :complete
          :elapsed-ms (quot (- (System/nanoTime) start) 1000000)}) (flush) result))

(defn current-case [] ((caller 'current-parameter-case) nil))

(defn check-native-negatives! [path core mir]
  (let [binder (get-in core [:core-nodes 0 :node-id])
        root (last (:root-node-ids core)) entry (get-in mir [:functions 'main :entry])
        local (get-in core [:core-nodes 1 :node-id])
        value-path [:data-flow-graph :values binder]
        definition-path [:data-flow-graph :definitions binder]
        type-fact (get-in core [:type-facts binder :fact-id])
        safety-fact (:safety-outcome-id (b/p15-s23-c11-mir-node-facts core (first (:core-nodes core))))
        validate #(b/p15-s23-c11-mir-validate-constructed! path %1 %2)]
    (is (contains? (:safety-table mir) safety-fact))
    (when-not (contains? (:safety-table mir) safety-fact)
      (throw (ex-info "Missing genuine binder safety row" {:id "NATIVE-PARAMETER-TEST-PREMISE"})))
    (doseq [[label changed id missing]
            [[:missing-parameter (assoc-in mir [:functions 'main :params] [])
              "C11-MODULE" :authenticated-c11-function-envelope]
             [:duplicate-parameter (assoc-in mir [:functions 'main :params] [binder binder])
              "C11-MODULE" :authenticated-c11-function-envelope]
             [:parameter-as-instruction
              (update-in mir [:functions 'main :blocks entry :instructions] conj {:op-id binder})
              "C11-MODULE" :one-core-operation-plus-one-derived-operation-per-runtime-check]
             [:definition-index (assoc-in mir (conj definition-path :parameter-index) 1)
              "C11-DOMINANCE" :exact-ssa-definition-table]
             [:definition-block (assoc-in mir (conj definition-path :block-id) "wrong-block")
              "C11-DOMINANCE" :exact-ssa-definition-table]
             [:definition-operation (assoc-in mir (conj definition-path :operation-id) binder)
              "C11-DOMINANCE" :exact-ssa-definition-table]
             [:value-kind (assoc-in mir (conj value-path :kind) :instruction-result)
              "C11-TYPE" :exact-ssa-value-table]
             [:value-type (assoc-in mir (conj value-path :type) :gravity/integer)
              "C11-TYPE" :exact-ssa-value-table]
             [:value-source (assoc-in mir (conj value-path :source) (:source (last (:core-nodes core))))
              "C11-TYPE" :exact-ssa-value-table]
             [:value-uses (assoc-in mir (conj value-path :uses) [])
              "C11-TYPE" :exact-ssa-value-table]
             [:source-map (assoc-in mir [:source-map binder] (:source (last (:core-nodes core))))
              "C11-ORIGIN" :exact-checked-core-source-map]
             [:parameter-type-fact (assoc-in mir [:type-table type-fact :type] :gravity/integer)
              "C11-TYPE" :id-indexed-c7-type-facts]
             [:parameter-safety-fact (assoc-in mir [:safety-table safety-fact :runtime-check] :injected)
              "C11-SAFETY" :id-indexed-c10-safety-outcomes]
             [:injected-runtime-table (assoc-in mir [:runtime-check-table :injected]
                                               {:guarded-operation-id binder})
              "C11-SAFETY" :canonical-runtime-check-table]
             [:module-provenance (assoc-in mir [:provenance :checked-core-artifact-id] "wrong-core")
              "C11-ORIGIN" :checked-core-provenance-binding]
             [:source-core (assoc mir :source-core "wrong-core")
              "C11-MODULE" :authenticated-c11-module-envelope]]]
      (testing (name label)
        (is (= {:id id :missing-fact missing} (diagnostic #(validate core changed))))))
    ;; These are Native checked-Core premise controls, not new C6 ingress
    ;; acceptance. A malformed Core cannot grant a parameter exception.
    (doseq [[label changed missing]
            [[:wrong-binder-index (assoc-in core [:core-nodes 0 :attributes :parameter-index] 1)
              :resolved-bool-entry-parameter-declaration]
             [:wrong-binder-path (assoc-in core [:core-nodes 0 :path] [:functions 'main :parameters 1 :binder])
              :single-bool-entry-parameter-diamond]
             [:non-bool-binder (assoc-in core [:core-nodes 0 :type] :gravity/i64)
              :single-bool-entry-parameter-diamond]
             [:non-bool-return (assoc-in core [:core-nodes 6 :type :return] :gravity/integer)
              :single-bool-entry-parameter-diamond]
             [:extra-parameter (update core :core-nodes conj (first (:core-nodes core)))
              :single-bool-entry-parameter-diamond]
             [:let-family (assoc-in core [:core-nodes 5 :source-operation] :let)
              :single-bool-entry-parameter-diamond]
             [:nested-family (assoc-in core [:core-nodes 3 :source-operation] :if)
              :single-bool-entry-parameter-diamond]
             [:binding-substitution (assoc-in core [:resolved-parameter :parameter-binding :binding-id]
                                             (get-in core [:resolved-parameter :type-binding :binding-id]))
              :resolved-bool-entry-parameter-declaration]
             [:source-origin-substitution (assoc-in core [:core-nodes 0 :source :origin-id]
                                                   (get-in core [:core-nodes 1 :source :origin-id]))
              :resolved-bool-entry-parameter-declaration]]]
      (testing (name label)
        (is (= {:id (if (= missing :single-bool-entry-parameter-diamond) "C11-MODULE" "C11-ORIGIN")
                :missing-fact missing}
               (diagnostic #(validate changed mir))))))
    (testing "coherent Core/MIR mutation does not establish binder semantics"
      (doseq [[changed-core changed-mir]
              [[(assoc-in core [:type-facts binder :type] :gravity/integer)
                (assoc-in mir [:type-table type-fact :type] :gravity/integer)]
               [(assoc-in core [:core-nodes 0 :safety :outcome] :unsafe-island)
                (assoc-in mir (conj value-path :safety :outcome) :unsafe-island)]
               [(assoc-in core [:core-nodes 0 :ownership :mutation] :allowed)
                (assoc-in mir (conj value-path :ownership :mutation) :allowed)]
               [(assoc-in core [:core-nodes 0 :safety :proof :basis] :supplied)
                (assoc-in mir (conj value-path :safety :proof :basis) :supplied)]
               [(assoc-in core [:ownership-facts binder :owner-id] (str "sha256:" (apply str (repeat 64 "0"))))
                (assoc-in mir [:ownership-table (get-in core [:ownership-facts binder :fact-id]) :owner-id]
                          (str "sha256:" (apply str (repeat 64 "0"))))]
               [(assoc-in core [:type-facts root :return] :gravity/integer)
                (assoc-in mir [:type-table (get-in core [:type-facts root :fact-id]) :return] :gravity/integer)]]]
        (is (= {:id "C11-SAFETY" :missing-fact :entry-parameter-checked-core-semantic-facts}
               (diagnostic #(validate changed-core changed-mir))))))
    (testing "binder runtime checks cannot be supplied as safety authority"
      (let [changed (assoc-in core [:core-nodes 0 :safety :check] {:check-id :injected})
            changed-mir (assoc-in mir [:safety-table safety-fact :check] {:check-id :injected})]
        ;; Direct runtime owner isolates its intended check from earlier full
        ;; fact parity. Complete validator controls above still run unchanged.
        (is (= {:id "C11-SAFETY" :missing-fact :entry-parameter-checked-core-semantic-facts}
               (diagnostic #(b/p15-s23-c11-mir-validate-runtime-checks!
                             path changed changed-mir {:operation-by-id {}}))))))))

(defn verify-original-and-fresh! [raw fresh]
  (binding [b/*p15-s23-stage2-compiler-artifact-binding*
            @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'current-emitter))]
    (let [{:keys [path text products]} fresh replay (:raw fresh)
          invoke (owner 'invoke) plan (owner 'parameter-c6-plan)]
      (is (= raw replay))
      (when-not (= raw replay) (throw (ex-info "Complete C6 replay mismatch" {:id "NATIVE-PARAMETER-REPLAY"})))
      (let [verified (phase :c6-verifier #(invoke plan 'c6-c10-verify-checked-core-template
                                                 [(:envelope products) (:artifact-template raw) (:digest-requests replay)]))]
        (is (= :passed (:status verified)))
        (when-not (= :passed (:status verified))
          (throw (ex-info "C6 fresh verifier rejected" {:id "NATIVE-PARAMETER-C6"}))))
      (let [sealed (b/p15-s23-c6c10-seal-digest-request-result! path raw)
            context (b/p15-s23-stage2-gravity-checked-core-context path text :jvm)
            core (b/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
            mir (phase :gravity-build #(invoke (owner 'c11-plan) 'sh12-build-branch-join-mir [core]))
            gravity (phase :gravity-verifier #(invoke (owner 'c11-plan) 'sh12-verify-branch-join-mir [core mir]))
            native (phase :native-verifier #(b/p15-s23-c11-mir-validate-constructed! path core mir))
            binder (get-in core [:core-nodes 0 :node-id])
            operations (mapcat :instructions (vals (get-in mir [:functions 'main :blocks])))]
        (is (= :passed (:status gravity) (:status native)))
        (when-not (= :passed (:status gravity) (:status native))
          (throw (ex-info "Independent owner verification rejected" {:id "NATIVE-PARAMETER-OWNERS"})))
        (is (= 7 (count (:core-nodes core)) (count (get-in mir [:data-flow-graph :values]))))
        (is (= 6 (count operations)))
        (is (= 4 (count (get-in mir [:functions 'main :blocks]))))
        (is (= [binder] (get-in mir [:functions 'main :params])))
        (is (not-any? #(= binder (:op-id %)) operations))
        (check-native-negatives! path core mir)))))

(deftest original-bool-entry-parameter-is-a-value-not-an-instruction
  (let [original (current-case) fresh (current-case)]
    (verify-original-and-fresh! (:raw original) fresh)))
