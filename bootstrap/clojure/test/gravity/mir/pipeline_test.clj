(ns gravity.mir.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.mir :as mir]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

(defn- fixture-helper [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))

(def ^:private products
  (delay
    (mapv
      (fn [extension]
        (let [source (.getCanonicalPath (io/file (get (fixture-helper 'accepted-fixtures) extension)))
              checked ((fixture-helper 'checked-core) extension)
              emitted ((fixture-helper 'build-mir) checked)
              function (get-in emitted [:functions (:entrypoint checked)])
              blocks (:blocks function)
              operations (vec (mapcat :instructions (vals blocks)))
              exit (get-in emitted [:control-flow-graph :join :block-id])]
          {:extension extension :source source :checked checked :mir emitted
           :function function :blocks blocks :operations operations
           :by-id (into {} (map (juxt :op-id identity)) operations)
           :positions (into {} (mapcat (fn [[_ block]]
                                        (map-indexed (fn [index op] [(:op-id op) index])
                                                     (:instructions block)))) blocks)
           :envelope {:function function :return-id (get-in blocks [exit :terminator :operands 0])}
           :operation-products {:blocks blocks :nodes (:core-nodes checked)}}))
      [".gravity" ".qst"])))

(defn- failure [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest contract-identifies-the-native-rule-boundary
  (is (empty? (ns-aliases 'gravity.mir)))
  (is (= #{'validate-terminator! 'validate-cfg! 'valid-dominance-edge? 'valid-terminator-use?
           'runtime-check-fact-record 'add-runtime-check-facts 'reindex-fact-table
           'profile-target-table 'capability-proof-table 'safety-proof-table
           'downstream-ids 'expected-block-operation-ids 'expected-data-flow 'expected-uses
           'expected-terminator-uses 'expected-terminator-value-uses 'expected-value
           'expected-runtime-check-value 'validate-effect-edge-placement! 'validate-data-flow!
           'validate-operations! 'validate-fact-tables! 'validate-module-envelope!
           'module-keys 'function-keys 'node-opcode 'node-facts 'node-operands
           'runtime-check-operation-id 'runtime-check-token-id 'runtime-check-source 'runtime-check-fact-id
           'validate-carrier! 'metadata-free? 'require-strict-structure! 'require!
           'pass-contract-record 'canonical-block-order 'operation-sequence 'path-prefix?
           'block-keys 'operation-keys 'terminator-keys 'allowed-opcodes 'allowed-source-operations}
         (set (keys (ns-publics 'gravity.mir)))))
  (let [contract (:gravity/namespace-contract (meta #'mir/validate-cfg!))]
    (is (= (set (keys (:public-api contract)))
           (set (keep (fn [[name operation]]
                        (when (:gravity/namespace-contract (meta operation)) name))
                      (ns-interns 'gravity.mir)))))
    (is (every? #(= contract (:gravity/namespace-contract (meta %)))
                (vals (ns-publics 'gravity.mir))))
    (is (every? #(not (:private (meta %)))
                (vals (ns-publics 'gravity.mir)))))
  (doseq [[name operation] (ns-publics 'gravity.mir)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.mir (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (some #{:general-cfgs} (:unsupported contract))))))

(deftest native-operation-vocabularies-match-real-emission-and-reject-corrupted-operations
  (doseq [{:keys [source checked operations] emitted :mir} @products]
    (doseq [operation operations]
      (is (contains? mir/allowed-opcodes (:opcode operation)))
      (is (contains? mir/allowed-source-operations (:source-operation operation))))
    (is (map? (b/p15-s23-c11-mir-validate-operations! source checked emitted)))
    (let [block-id (first (for [[id block] (get-in emitted [:functions (:entrypoint checked) :blocks])
                               :when (seq (:instructions block))] id))]
      (doseq [[field id] [[:opcode "C11-TARGET-LEAK"] [:source-operation "C11-MODULE"]]]
        (let [changed (assoc-in emitted [:functions (:entrypoint checked) :blocks block-id :instructions 0 field]
                                :llvm/add)
              rejected (failure #(b/p15-s23-c11-mir-validate-operations! source checked changed))]
          (is (= id (:id rejected)))
          (is (= :checked-core-operation-parity (:missing-fact rejected)))))))
  (is (not (contains? mir/allowed-opcodes :llvm/add)))
  (is (not (contains? mir/allowed-source-operations :llvm/add))))

(deftest native-operation-vocabulary-aliases-preserve-snapshots-and-call-time-values
  (doseq [[stage legacy] [[#'mir/allowed-opcodes #'b/p15-s23-c11-mir-allowed-opcodes]
                          [#'mir/allowed-source-operations #'b/p15-s23-c11-mir-allowed-source-operations]]]
    (let [original (var-get legacy)]
      (is (set? original))
      (is (identical? (var-get stage) original))
      (with-redefs-fn {stage #{}}
        #(is (identical? original (var-get legacy))))))
  (let [observed (atom nil) marker (Object.)]
    (with-redefs [b/p15-s23-c11-mir-allowed-opcodes #{:changed-opcode}
                  b/p15-s23-c11-mir-allowed-source-operations #{:changed-source-operation}
                  mir/validate-operations! (fn [ports & _] (reset! observed ports) marker)]
      (is (identical? marker (b/p15-s23-c11-mir-validate-operations! :source :core :mir)))
      (is (= #{:changed-opcode} (:allowed-opcodes @observed)))
      (is (= #{:changed-source-operation} (:allowed-source-operations @observed))))))

(deftest native-block-operation-and-terminator-key-schemas-match-emission
  (doseq [{:keys [source checked envelope operation-products] emitted :mir} @products]
    (let [blocks (get-in emitted [:functions (:entrypoint checked) :blocks])
          block-id (first (for [[id block] blocks :when (seq (:instructions block))] id))
          changed (assoc-in emitted [:functions (:entrypoint checked) :blocks block-id :extra] true)
          rejected (failure #(b/p15-s23-c11-mir-validate-operations! source checked changed))]
      (doseq [block (vals blocks)]
        (is (= mir/block-keys (set (keys block))))
        (is (= mir/terminator-keys (set (keys (:terminator block)))))
        (doseq [operation (:instructions block)]
          (is (= mir/operation-keys (set (keys operation))))))
      (is (map? (b/p15-s23-c11-mir-validate-operations! source checked emitted)))
      (is (map? (b/p15-s23-c11-mir-validate-cfg! source checked emitted envelope operation-products)))
      (is (= "C11-BLOCK" (:id rejected)))
      (is (= :well-formed-mir-block (:missing-fact rejected))))))

(deftest native-key-schema-aliases-preserve-legacy-snapshot-and-call-time-behavior
  (doseq [[stage legacy] [[#'mir/block-keys #'b/p15-s23-c11-mir-block-keys]
                          [#'mir/operation-keys #'b/p15-s23-c11-mir-operation-keys]
                          [#'mir/terminator-keys #'b/p15-s23-c11-mir-terminator-keys]]]
    (let [original (var-get legacy)]
      (is (set? original))
      (is (identical? (var-get stage) original))
      (with-redefs-fn {stage #{}}
        #(is (identical? original (var-get legacy))))))
  (let [observed (atom nil) marker (Object.)]
    (with-redefs [b/p15-s23-c11-mir-block-keys #{:changed-block}
                  b/p15-s23-c11-mir-operation-keys #{:changed-operation}
                  mir/validate-operations! (fn [ports & _] (reset! observed ports) marker)]
      (is (identical? marker (b/p15-s23-c11-mir-validate-operations! :source :core :mir)))
      (is (= #{:changed-block} (:block-keys @observed)))
      (is (= #{:changed-operation} (:operation-keys @observed)))))
  (let [observed (atom nil) marker (Object.)]
    (with-redefs [b/p15-s23-c11-mir-terminator-keys #{:changed-terminator}
                  mir/validate-terminator! (fn [& args] (reset! observed args) marker)]
      (is (identical? marker (b/p15-s23-c11-mir-validate-terminator! :source :core :block :expected)))
      (is (= #{:changed-terminator} (second @observed))))))

(deftest native-pass-and-order-records-match-real-emitted-mir
  (doseq [{:keys [source checked] emitted :mir} @products]
    (let [block-order (mir/canonical-block-order emitted)
          operations (mir/operation-sequence #'b/p15-s23-c11-mir-canonical-block-order emitted)
          block-id (first (for [[id block] (get-in emitted [:functions (:entrypoint checked) :blocks])
                               :when (< 1 (count (:instructions block)))] id))
          changed (update-in emitted [:functions (:entrypoint checked) :blocks block-id :instructions]
                             #(vec (reverse %)))
          rejected (failure #(b/p15-s23-c11-mir-validate-operations! source checked changed))]
      (is (= 4 (count block-order)))
      (is (= block-order (b/p15-s23-c11-mir-canonical-block-order emitted)))
      (is (= 5 (count operations)))
      (is (= operations (b/p15-s23-c11-mir-operation-sequence emitted)))
      (is (= operations (:operations (b/p15-s23-c11-mir-validate-operations! source checked emitted))))
      (is (= (:pass-contract emitted) (mir/pass-contract-record)))
      (is (= (mir/pass-contract-record) (b/p15-s23-c11-mir-pass-contract-record)))
      (is (some? block-id))
      (is (not= emitted changed))
      (is (= "C11-DOMINANCE" (:id rejected)))
      (is (= :canonical-checked-core-instruction-order-and-placement (:missing-fact rejected))))))

(deftest native-order-and-path-helpers-retain-relative-data-semantics
  (let [first-op (Object.) second-op (Object.)
        module {:source-core "core"
                :functions {"function" {:blocks {"core:mir:entry" {:instructions [first-op]}
                                                 "core:mir:join" {:instructions [second-op]}}}}}
        calls (atom [])
        sequence (mir/operation-sequence (fn [value]
                                          (swap! calls conj value)
                                          ["core:mir:join" "missing" "core:mir:entry" "core:mir:join"])
                                        module)]
    (is (= ["core:mir:entry" "core:mir:then" "core:mir:else" "core:mir:join"]
           (mir/canonical-block-order module)))
    (is (= ["core:mir:entry"]
           (mir/canonical-block-order (update-in module [:functions "function" :blocks] dissoc "core:mir:join"))))
    (is (= [module] @calls))
    (is (identical? module (first @calls)))
    (is (vector? sequence))
    (is (= 3 (count sequence)))
    (is (every? true? (map identical? [second-op first-op second-op] sequence))))
  (doseq [[prefix path expected] [[[] [] true] [[] [:a] true] [[:a] [:a :b] true]
                                  [[:a :b] [:a] false] [[:a] [:b] false]
                                  [nil [] false] [[:a] '(:a) false] ['(:a) [:a] false]]]
    (is (= expected (mir/path-prefix? prefix path)))
    (is (= expected (b/p15-s23-c11-mir-path-prefix? prefix path)))))

(deftest native-order-wrappers-retain-arguments-and-live-block-order-var
  (doseq [[stage wrapper args ports]
          [[#'mir/pass-contract-record #'b/p15-s23-c11-mir-pass-contract-record [] []]
           [#'mir/canonical-block-order #'b/p15-s23-c11-mir-canonical-block-order [:mir] []]
           [#'mir/operation-sequence #'b/p15-s23-c11-mir-operation-sequence [:mir]
            [#'b/p15-s23-c11-mir-canonical-block-order]]
           [#'mir/path-prefix? #'b/p15-s23-c11-mir-path-prefix? [:prefix :path] []]]]
    (let [observed (atom nil) marker (Object.)]
      (with-redefs-fn {stage (fn [& supplied] (reset! observed supplied) marker)}
        #(is (identical? marker (apply wrapper args))))
      (is (= (vec (concat ports args)) (vec @observed)))
      (is (every? true? (map identical? ports (take (count ports) @observed))))))
  (let [operation #'b/p15-s23-c11-mir-canonical-block-order error (ex-info "late block order" {})]
    (with-redefs [b/p15-s23-c11-mir-canonical-block-order (fn [& _] (throw error))]
      (is (identical? error (try (mir/operation-sequence operation {})
                                nil (catch clojure.lang.ExceptionInfo e e)))))))

(defn- carrier-validation-ports []
  {:trusted-carrier-validation #'b/p15-s23-trusted-carrier-validation
   :valid-unicode-string? #'b/p15-s23-c6c10-valid-unicode-string?
   :fail! #'b/p15-s23-c11-mir-fail!
   :contain-exception! #'b/p15-s23-c11-mir-contain-exception!})

(deftest native-carrier-rules-check-real-emission-and-refuse-invalid-data
  (doseq [{:keys [source] emitted :mir} @products]
    (let [ports (carrier-validation-ports)
          validate #(mir/validate-carrier! ports source :c11-module %)]
      (is (= {:status :passed} (validate emitted)))
      (is (= (validate emitted) (b/p15-s23-c11-mir-bounded-value! source :c11-module emitted)))
      (is (= {:status :passed} (b/p15-s23-c11-mir-bounded-value! source :c11-module emitted 0 -1)))
      (is (true? (mir/metadata-free? emitted)))
      (doseq [[changed missing]
              [[(assoc emitted :module (str (char 0xD800))) :well-formed-unicode-c11-carrier]
               [(assoc emitted :module (char 0xDFFF)) :well-formed-unicode-c11-carrier]
               [(assoc emitted :module (Object.)) :valid-native-c11-carrier]
               [(with-meta emitted {:extra true}) :valid-native-c11-carrier]]]
        (let [stage (failure #(validate changed))
              legacy (failure #(b/p15-s23-c11-mir-bounded-value! source :c11-module changed))]
          (is (= "C11-VERIFY" (:id stage)))
          (is (= missing (:missing-fact stage)))
          (is (= stage legacy))))
      (is (nil? (mir/require-strict-structure!
                 #'b/p15-s23-c6c10-strict-first-mismatch #'b/p15-s23-c11-mir-fail!
                 source emitted emitted :carrier-parity)))
      (let [changed (assoc emitted :module "changed")
            rejected (failure #(mir/require-strict-structure!
                                #'b/p15-s23-c6c10-strict-first-mismatch #'b/p15-s23-c11-mir-fail!
                                source emitted changed :carrier-parity))]
        (is (= "C11-VERIFY" (:id rejected)))
        (is (= :carrier-parity (:missing-fact rejected)))
        (is (= rejected (failure #(b/p15-s23-c11-mir-require-strict-structure!
                                   source emitted changed :carrier-parity))))))))

(deftest native-metadata-condition-and-structure-rules-preserve-owner-operations
  (doseq [container [#(hash-map % :value) #(hash-map :key %) vector list hash-set]]
    (is (false? (mir/metadata-free? (container (with-meta [] {:extra true}))))))
  (is (false? (mir/metadata-free? (with-meta [] {}))))
  (is (true? (mir/metadata-free? {:key [nil true false 1 1.5 :value 'symbol \a]})))
  (let [calls (atom []) marker (Object.)
        mismatch (fn [& args] (swap! calls conj [:mismatch args]) :different)
        fail (fn [& args] (swap! calls conj [:fail args]) marker)
        subject (Object.)]
    (is (nil? (mir/require! fail true "C11-VERIFY" :source subject :fact)))
    (is (empty? @calls))
    (is (identical? marker (mir/require! fail false "C11-VERIFY" :source subject :fact)))
    (is (= [[:fail ["C11-VERIFY" :source subject {:missing-fact :fact}]]] @calls))
    (reset! calls [])
    (is (identical? marker (mir/require-strict-structure! mismatch fail :source :expected :actual :fact)))
    (is (= [[:mismatch [:source :expected :actual []]]
            [:fail ["C11-VERIFY" :source {} {:missing-fact :fact}]]] @calls))
    (reset! calls [])
    (is (nil? (mir/require-strict-structure! (constantly nil) fail :source :same :same :fact)))
    (is (empty? @calls))))

(deftest native-carrier-containment-keeps-original-host-error-and-interruption
  (doseq [[error reason] [[(StackOverflowError. "stack") :contained-c11-carrier-host-stack]
                          [(AssertionError. "assert") :contained-c11-carrier-assertion]
                          [(LinkageError. "link") :contained-c11-carrier-linkage]
                          [(ex-info "diagnostic" {}) :contained-c11-carrier-diagnostic]
                          [(Exception. "host") :contained-c11-carrier-host-failure]]]
    (let [observed (atom nil) marker (Object.)
          ports (assoc (carrier-validation-ports)
                       :trusted-carrier-validation (fn [& _] (throw error))
                       :contain-exception! (fn [& args] (reset! observed args) marker))]
      (is (identical? marker (mir/validate-carrier! ports :source :definition :value)))
      (is (= [:source reason error] @observed))
      (is (identical? error (last @observed)))))
  (let [error (InterruptedException. "interrupted") called (atom false)
        ports (assoc (carrier-validation-ports)
                     :trusted-carrier-validation (fn [& _] (throw error))
                     :contain-exception! (fn [& _] (reset! called true)))
        observed (try (mir/validate-carrier! ports :source :definition :value)
                      nil
                      (catch InterruptedException e
                        {:error e :interrupted? (.isInterrupted (Thread/currentThread))})
                      (finally (Thread/interrupted)))]
    (is (identical? error (:error observed)))
    (is (true? (:interrupted? observed)))
    (is (false? @called))))

(deftest native-carrier-wrappers-preserve-live-vars-and-legacy-arity
  (let [observed (atom nil) marker (Object.)]
    (with-redefs [mir/validate-carrier! (fn [& args] (reset! observed args) marker)]
      (is (identical? marker (b/p15-s23-c11-mir-bounded-value! :source :definition :value)))
      (is (= (carrier-validation-ports) (first @observed)))
      (is (= [:source :definition :value] (vec (rest @observed))))))
  (doseq [[stage wrapper ports args]
          [[#'mir/metadata-free? #'b/p15-s23-c11-mir-metadata-free? [] [:value]]
           [#'mir/require! #'b/p15-s23-c11-mir-require!
            [#'b/p15-s23-c11-mir-fail!] [:condition :id :source :subject :fact]]
           [#'mir/require-strict-structure! #'b/p15-s23-c11-mir-require-strict-structure!
            [#'b/p15-s23-c6c10-strict-first-mismatch #'b/p15-s23-c11-mir-fail!]
            [:source :expected :actual :fact]]]]
    (let [observed (atom nil) marker (Object.)]
      (with-redefs-fn {stage (fn [& supplied] (reset! observed supplied) marker)}
        #(is (identical? marker (apply wrapper args))))
      (is (= (concat ports args) @observed))
      (is (every? true? (map identical? ports (take (count ports) @observed))))))
  (let [original b/p15-s23-c11-mir-bounded-value! observed (atom nil) marker (Object.)]
    (with-redefs [b/p15-s23-c11-mir-bounded-value! (fn [& args] (reset! observed args) marker)]
      (is (identical? marker (original :source :definition :value :ignored :ignored))))
    (is (= [:source :definition :value] @observed))))

(deftest native-node-records-match-real-emitted-operations
  (doseq [{:keys [checked by-id]} @products node (:core-nodes checked)
          :let [operation (get by-id (:node-id node))] :when operation]
    (is (= (:opcode operation) (mir/node-opcode node)))
    (is (= (:operands operation) (mir/node-operands #'b/p15-s23-c11-mir-runtime-check-token-id node)))
    (is (= (:facts operation) (mir/node-facts checked node))))
  (is (= :unsupported-checked-core-operation (mir/node-opcode {:source-operation :unknown}))))

(deftest native-fact-and-check-records-preserve-order-source-and-id-data
  (let [span {:source "native" :form-index 2}
        check {:check-id "check" :failure :panic}
        operands ["left" "right"]
        node {:node-id "node" :source {:origin-id "origin" :span span :enclosing-syntax-origin-id "syntax"}
              :operands operands :safety {:check check :proof {:proof-id "safe"}}}
        core {:type-facts {"node" {:fact-id "type"}} :effect-facts {"node" {:fact-id "effect"}}
              :ownership-facts {"node" {:fact-id "owner"}}
              :capability-facts {"node" {:capability-proof {:proof-ids ["cap" "cap"]}}}}
        facts (mir/node-facts core node) generated (mir/runtime-check-source node)
        calls (atom [])]
    (is (= ["type" "effect" "owner" "node:capability-fact" "node:safety-outcome" "node:profile-target-fact"]
           ((juxt :type-fact-id :effect-fact-id :ownership-fact-id :capability-fact-id
                  :safety-outcome-id :profile-target-fact-id) facts)))
    (is (= ["cap" "cap"] (:capability-proof-ids facts)))
    (is (= ["cap" "cap" "safe"] (:proof-certificate-ids facts)))
    (is (= ["check" :panic "origin" "safe"]
           ((juxt :runtime-check-id :failure-behavior :source-origin-id :safety-proof-id) facts)))
    (is (= ["origin" "syntax" true] ((juxt :origin-id :enclosing-syntax-origin-id :generated?) generated)))
    (is (identical? span (:span generated)))
    (is (= [{:role :runtime-check-generated :producer-operation-id "node" :runtime-check-id "check"}]
           (:generated-origin generated)))
    (is (= ["token" "left" "right"]
           (mir/node-operands (fn [value] (swap! calls conj value) "token") node)))
    (is (= [check] @calls))
    (is (identical? operands (mir/node-operands nil (assoc node :safety {}))))
    (is (= "check:mir:runtime-check" (mir/runtime-check-operation-id check)))
    (is (= "check:mir:token" (mir/runtime-check-token-id check)))
    (doseq [[kind suffix] [[:type ":mir:type-fact"] [:effect ":mir:effect-fact"]
                         [:capability ":mir:capability-fact"] [:ownership ":mir:ownership-fact"]
                         [:safety ":mir:safety-outcome"] [:profile-target ":mir:profile-target-fact"]]]
      (is (= (str "check" suffix) (mir/runtime-check-fact-id check kind))))
    (let [without (mir/node-facts {} (assoc node :safety {}))]
      (is (= [:not-applicable :not-applicable :not-applicable [] []]
             ((juxt :runtime-check-id :failure-behavior :safety-proof-id :capability-proof-ids
                    :proof-certificate-ids) without)))))
  (is (thrown? IllegalArgumentException (mir/runtime-check-fact-id {} :unknown))))

(deftest native-node-wrappers-retain-arguments-and-live-token-operation
  (doseq [[name args ports]
          [['node-opcode [:node] []] ['node-facts [:core :node] []]
           ['runtime-check-operation-id [:check] []] ['runtime-check-token-id [:check] []]
           ['node-operands [:node] [#'b/p15-s23-c11-mir-runtime-check-token-id]]
           ['runtime-check-source [:node] []] ['runtime-check-fact-id [:check :type] []]]]
    (let [stage (ns-resolve 'gravity.mir name)
          wrapper (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-c11-mir-" name)))
          marker (Object.) observed (atom nil)]
      (with-redefs-fn {stage (fn [& arguments] (reset! observed arguments) marker)}
        #(is (identical? marker (apply wrapper args))))
      (is (= (concat ports args) @observed))
      (is (every? true? (map identical? ports (take (count ports) @observed))))))
  (let [error (ex-info "late native token id" {})
        operation #'b/p15-s23-c11-mir-runtime-check-token-id
        caught (with-redefs [b/p15-s23-c11-mir-runtime-check-token-id (fn [& _] (throw error))]
                 (try (mir/node-operands operation {:safety {:check {:check-id "check"}}})
                      nil (catch clojure.lang.ExceptionInfo e e)))]
    (is (identical? error caught))))

(defn- envelope-validation-ports []
  {:entry-parameter! #'b/p15-s23-c11-mir-entry-parameter!
   :module-keys b/p15-s23-c11-mir-module-keys
   :function-keys b/p15-s23-c11-mir-function-keys
   :require! #'b/p15-s23-c11-mir-require!
   :require-strict-structure! #'b/p15-s23-c11-mir-require-strict-structure!
   :pass-contract-record #'b/p15-s23-c11-mir-pass-contract-record})

(deftest native-envelope-rules-preserve-real-records-and-refuse-substitution
  (doseq [{:keys [source checked] emitted :mir} @products]
    (let [ports (envelope-validation-ports)
          accepted (mir/validate-module-envelope! ports source checked emitted)
          function-path [:functions (:entrypoint checked)]]
      (is (= accepted (b/p15-s23-c11-mir-validate-module-envelope! source checked emitted)))
      (is (= #{:function :root-node :return-node :return-id} (set (keys accepted))))
      (is (identical? (get-in emitted function-path) (:function accepted)))
      (doseq [key [:root-node :return-node]]
        (is (some #(identical? % (get accepted key)) (:core-nodes checked))))
      (doseq [[changed id missing]
              [[(assoc emitted :source-core "forged") "C11-MODULE" :authenticated-c11-module-envelope]
               [(assoc emitted :extra true) "C11-MODULE" :authenticated-c11-module-envelope]
               [(assoc emitted :target-independent? false) "C11-MODULE" :authenticated-c11-module-envelope]
               [(assoc-in emitted (conj function-path :name) 'other) "C11-MODULE" :authenticated-c11-function-envelope]
               [(assoc-in emitted [:provenance :checked-core-artifact-id] "forged")
                "C11-ORIGIN" :checked-core-provenance-binding]
               [(assoc-in emitted [:construction :status] :accepted) "C11-VERIFY" :gravity-owned-mir-construction]
               [(assoc-in emitted [:pass-execution-record :status] :passed)
                "C11-VERIFY" :gravity-owned-build-mir-pass-contract-and-pending-execution]
               [(assoc-in emitted [:b1-preflight :backend-credit?] true) "C11-VERIFY" :bounded-c11-and-b1-scope]
               [(assoc-in emitted [:scope :release-credit?] true) "C11-VERIFY" :bounded-c11-and-b1-scope]]]
        (let [rejected (failure #(mir/validate-module-envelope! ports source checked changed))]
          (is (= id (:id rejected)))
          (is (= missing (:missing-fact rejected)))
          (is (= rejected (failure #(b/p15-s23-c11-mir-validate-module-envelope! source checked changed)))))))))

(deftest envelope-schemas-and-wrapper-retain-data-aliases-and-live-owner-vars
  (is (identical? mir/module-keys b/p15-s23-c11-mir-module-keys))
  (is (identical? mir/function-keys b/p15-s23-c11-mir-function-keys))
  (is (set? mir/module-keys))
  (is (set? mir/function-keys))
  (let [original b/p15-s23-c11-mir-module-keys]
    (with-redefs [mir/module-keys #{}]
      (is (identical? original b/p15-s23-c11-mir-module-keys))))
  (let [marker (Object.) observed (atom nil)]
    (with-redefs [mir/validate-module-envelope! (fn [& args] (reset! observed args) marker)]
      (is (identical? marker (b/p15-s23-c11-mir-validate-module-envelope! :source :core :mir))))
    (is (= [:source :core :mir] (vec (rest @observed))))
    (let [expected (envelope-validation-ports) ports (first @observed)]
      (is (= expected ports))
      (is (every? true? (map #(identical? (get expected %) (get ports %)) (keys expected))))
      (let [error (ex-info "late envelope callback" {})
            caught (with-redefs [b/p15-s23-c11-mir-entry-parameter! (fn [& _] (throw error))]
                     (try (mir/validate-module-envelope! ports :source :core :mir)
                          nil (catch clojure.lang.ExceptionInfo e e)))]
        (is (identical? error caught))))
    (let [changed #{:changed-schema}]
      (with-redefs [b/p15-s23-c11-mir-module-keys changed
                    mir/validate-module-envelope! (fn [& args] (reset! observed args) marker)]
        (b/p15-s23-c11-mir-validate-module-envelope! :source :core :mir))
      (is (identical? changed (:module-keys (first @observed)))))))

(deftest envelope-operation-order-is-preserved
  (let [{:keys [source checked] emitted :mir} (first @products)
        expected (envelope-validation-ports) events (atom [])
        observed (into {} (for [[key value] expected]
                            [key (if (var? value)
                                   (fn [& args] (swap! events conj key) (apply value args)) value)]))]
    (is (= (mir/validate-module-envelope! expected source checked emitted)
           (mir/validate-module-envelope! observed source checked emitted)))
    (is (= [:entry-parameter! :require! :require! :require! :require-strict-structure!
            :require! :require-strict-structure! :pass-contract-record :require!
            :pass-contract-record :require-strict-structure! :require-strict-structure!
            :require! :require-strict-structure! :require-strict-structure!]
           @events))))

(defn- fact-validation-ports []
  {:add-runtime-check-facts #'b/p15-s23-c11-mir-add-runtime-check-facts
   :reindex-fact-table #'b/p15-s23-c11-mir-reindex-fact-table
   :profile-target-table #'b/p15-s23-c11-mir-profile-target-table
   :capability-proof-table #'b/p15-s23-c11-mir-capability-proof-table
   :safety-proof-table #'b/p15-s23-c11-mir-safety-proof-table
   :require! #'b/p15-s23-c11-mir-require!})

(deftest native-fact-table-equality-accepts-emission-and-rejects-table-substitution
  (doseq [{:keys [source checked] emitted :mir} @products]
    (let [ports (fact-validation-ports)]
      (is (nil? (mir/validate-fact-tables! ports source checked emitted)))
      (is (nil? (b/p15-s23-c11-mir-validate-fact-tables! source checked emitted)))
      (doseq [[key id missing]
              [[:type-table "C11-TYPE" :id-indexed-c7-type-facts]
               [:effect-table "C11-EFFECT" :id-indexed-c8-effect-facts]
               [:ownership-table "C11-VERIFY" :id-indexed-c9-ownership-facts]
               [:capability-table "C11-EFFECT" :id-indexed-c8-capability-facts]
               [:safety-table "C11-SAFETY" :id-indexed-c10-safety-outcomes]
               [:profile-target-table "C11-MODULE" :id-indexed-profile-target-facts]
               [:capability-proof-table "C11-EFFECT" :id-indexed-capability-proofs]
               [:proof-certificate-table "C11-SAFETY" :id-indexed-authenticated-safety-proof-certificates]]]
        (let [changed (assoc emitted key {:substituted :native-data})
              rejected (failure #(mir/validate-fact-tables! ports source checked changed))]
          (is (= id (:id rejected)))
          (is (= missing (:missing-fact rejected)))
          (is (= rejected (failure #(b/p15-s23-c11-mir-validate-fact-tables! source checked changed))))))
      (let [changed (assoc emitted :effect-order-graph {})]
        (is (nil? (mir/validate-fact-tables! ports source checked changed)))
        (is (= :authenticated-partial-effect-order-graph
               (:missing-fact (failure #(b/p15-s23-c11-mir-validate-fact-tables! source checked changed)))))))))

(deftest fact-table-wrapper-preserves-live-ports-and-separate-graph-rule
  (let [{:keys [source checked] emitted :mir} (first @products)
        observed (atom nil) marker (Object.)
        expected (fact-validation-ports)]
    (with-redefs [mir/validate-fact-tables! (fn [& args] (reset! observed args) marker)]
      (is (nil? (b/p15-s23-c11-mir-validate-fact-tables! source checked emitted))))
    (is (= [expected source checked emitted] (vec @observed)))
    (is (every? true? (map #(identical? (get expected %) (get (first @observed) %)) (keys expected))))
    (let [error (ex-info "late fact-table callback" {})
          caught (with-redefs [b/p15-s23-c11-mir-require! (fn [& _] (throw error))]
                   (try (mir/validate-fact-tables! (first @observed) source checked emitted)
                        nil (catch clojure.lang.ExceptionInfo e e)))]
      (is (identical? error caught)))
    (let [events (atom [])
          ports (into {} (for [[key value] expected]
                           [key (fn [& args] (swap! events conj key) (apply value args))]))]
      (is (nil? (mir/validate-fact-tables! ports source checked emitted)))
      (is (= (vec (concat (mapcat (fn [_] [:reindex-fact-table :add-runtime-check-facts]) (range 5))
                         [:profile-target-table :add-runtime-check-facts :capability-proof-table :safety-proof-table]
                         (repeat 8 :require!))) @events)))))

(defn- operation-validation-ports []
  {:entry-parameter! #'b/p15-s23-c11-mir-entry-parameter!
   :operation-sequence #'b/p15-s23-c11-mir-operation-sequence
   :runtime-check-operation-id #'b/p15-s23-c11-mir-runtime-check-operation-id
   :expected-block-operation-ids #'b/p15-s23-c11-mir-expected-block-operation-ids
   :require! #'b/p15-s23-c11-mir-require!
   :block-keys b/p15-s23-c11-mir-block-keys
   :node-facts #'b/p15-s23-c11-mir-node-facts
   :operation-keys b/p15-s23-c11-mir-operation-keys
   :node-opcode #'b/p15-s23-c11-mir-node-opcode
   :allowed-opcodes b/p15-s23-c11-mir-allowed-opcodes
   :allowed-source-operations b/p15-s23-c11-mir-allowed-source-operations
   :node-operands #'b/p15-s23-c11-mir-node-operands
   :expected-runtime-check-operation #'b/p15-s23-c11-mir-expected-runtime-check-operation
   :set-union #'set/union})

(deftest native-operation-rules-accept-real-emission-and-reject-changed-records
  (doseq [{:keys [source checked] emitted :mir} @products]
    (let [ports (operation-validation-ports)
          accepted (mir/validate-operations! ports source checked emitted)
          block-id (first (for [[id block] (get-in emitted [:functions (:entrypoint checked) :blocks])
                               :when (seq (:instructions block))] id))
          block-path [:functions (:entrypoint checked) :blocks block-id]
          reorder-block-id (first (for [[id block] (get-in emitted [:functions (:entrypoint checked) :blocks])
                                       :when (< 1 (count (:instructions block)))] id))
          reorder-path [:functions (:entrypoint checked) :blocks reorder-block-id :instructions]
          op-path (conj block-path :instructions 0)]
      (is (= accepted (b/p15-s23-c11-mir-validate-operations! source checked emitted)))
      (is (= 5 (count (:operations accepted))))
      (is (identical? (:core-nodes checked) (:nodes accepted)))
      (is (empty? (:runtime-nodes accepted)))
      (is (some? reorder-block-id))
      (doseq [[changed id missing]
              [[(assoc-in emitted (conj op-path :opcode) :llvm/add)
                "C11-TARGET-LEAK" :checked-core-operation-parity]
               [(assoc-in emitted (conj op-path :type) :gravity/string)
                "C11-TYPE" :resolvable-id-referenced-operation-facts]
               [(assoc-in emitted (conj op-path :source) {:origin-id :forged})
                "C11-ORIGIN" :checked-core-source-origin]
               [(assoc-in emitted (conj op-path :facts :source-origin-id) :forged)
                "C11-SAFETY" :linked-c6-c10-id-references]
               [(assoc-in emitted (conj op-path :effects) #{:io})
                "C11-EFFECT" :direct-intrinsic-effect-capability-ordering]
               [(assoc-in emitted (conj block-path :extra) true)
                "C11-BLOCK" :well-formed-mir-block]
               [(update-in emitted (conj block-path :instructions) #(vec (rest %)))
                "C11-MODULE" :one-core-operation-plus-one-derived-operation-per-runtime-check]
               [(update-in emitted reorder-path #(vec (reverse %)))
                "C11-DOMINANCE" :canonical-checked-core-instruction-order-and-placement]]]
        (let [rejected (failure #(mir/validate-operations! ports source checked changed))]
          (is (= id (:id rejected)))
          (is (= missing (:missing-fact rejected)))
          (is (= rejected (failure #(b/p15-s23-c11-mir-validate-operations! source checked changed)))))))))

(deftest operation-wrapper-retains-live-owner-vars-and-schema-data
  (let [marker (Object.) observed (atom nil)]
    (with-redefs [mir/validate-operations! (fn [& args] (reset! observed args) marker)]
      (is (identical? marker (b/p15-s23-c11-mir-validate-operations! :source :core :mir))))
    (is (= [:source :core :mir] (vec (rest @observed))))
    (let [ports (first @observed) expected (operation-validation-ports)]
      (is (= expected ports))
      (is (every? true? (map #(identical? (get expected %) (get ports %)) (keys expected))))
      (let [error (ex-info "late owner callback" {})
            caught (with-redefs [b/p15-s23-c11-mir-entry-parameter! (fn [& _] (throw error))]
                     (try (mir/validate-operations! ports :source :core :mir)
                          nil (catch clojure.lang.ExceptionInfo e e)))]
        (is (identical? error caught)))))
  (let [{:keys [source checked] emitted :mir} (first @products)
        ports (operation-validation-ports)
        events (atom [])
        observed (into {} (for [[key value] ports]
                            [key (if (var? value)
                                   (fn [& args] (swap! events conj key) (apply value args)) value)]))]
    (is (= (mir/validate-operations! ports source checked emitted)
           (mir/validate-operations! observed source checked emitted)))
    (is (= [:entry-parameter! :operation-sequence :expected-block-operation-ids :set-union :require! :require!]
           (vec (take 6 @events))))))

(deftest fact-tables-preserve-ids-order-and-borrowed-data
  (let [source {:origin-id "origin" :generated-origin [:macro]}
        proof {:proof-id "proof" :status :supplied-data}
        node {:node-id "node" :source source :profile :hosted :safety {:proof proof}}
        profile {:profile :hosted :evidence :supplied-data}
        core {:core-nodes [node] :source-target :jvm :profile-facts {"node" profile}
              :capability-proof-records [proof]
              :target-request-metadata {:requested-target :c}}
        original {:core-node-id "node" :fact-id "original" :source source}
        indexed (mir/reindex-fact-table [node] {"node" original} :fact-id ":type-fact")
        fallback (mir/reindex-fact-table [node] {} :fact-id ":type-fact")
        target (get (mir/profile-target-table core) "node:profile-target-fact")]
    (is (sorted? indexed))
    (is (= {"original" original} indexed))
    (is (identical? source (get-in indexed ["original" :source])))
    (is (= {"node:type-fact" {:fact-id "node:type-fact"}} fallback))
    (is (identical? profile (:profile-fact target)))
    (is (= [:hosted :jvm :c] ((juxt :profile :source-target :target-request) target)))
    (is (identical? proof (get (mir/capability-proof-table core) "proof")))
    (is (identical? proof (get (mir/safety-proof-table core) "proof")))
    (is (= {} (mir/safety-proof-table (assoc core :core-nodes [(dissoc node :safety)]))))))

(deftest runtime-check-facts-retain-owner-ports-and-native-classification
  (let [check {:check-id "check" :capability-proof-id "proof" :authority-record-id "authority"
               :failure :panic}
        node {:node-id "node" :source {:origin-id "origin"} :profile :hosted :safety {:check check}}
        core {:core-nodes [node] :source-target :jvm :profile-facts {"node" :profile-data}
              :target-request-metadata {:requested-target :c}}
        events (atom [])
        fact-id (fn [value kind] (swap! events conj [:fact value kind]) "fact")
        op-id (fn [value] (swap! events conj [:operation value]) "operation")
        token-id (fn [value] (swap! events conj [:token value]) "token")]
    (doseq [kind [:type :effect :capability :ownership :safety :profile-target]]
      (reset! events [])
      (let [record (mir/runtime-check-fact-record fact-id op-id token-id core node kind)]
        (is (= [[:fact check kind] [:operation check] [:token check]] @events))
        (is (= "fact" (or (:fact-id record) (:outcome-id record))))
        (is (= (when-not (= :profile-target kind) "origin") (:source-origin-id record)))
        (is (= :hosted (:profile record)))
        (is (true? (:derived? record)))
        (case kind
          :effect (is (= [:sequence true #{}] ((juxt :ordering :non-dce? :direct) record)))
          :capability (is (= [["proof"] "authority" #{}]
                             ((juxt :capability-proof-ids :authority-id :granted) record)))
          :safety (is (= [:runtime-checked "check" :panic]
                         ((juxt :outcome :runtime-check :failure-behavior) record)))
          :ownership (is (= ["token" :single-guard-use :forbidden]
                            ((juxt :value-id :shareability :mutation) record)))
          :type (is (= :gravity/runtime-check-token (:type record)))
          :profile-target (is (= [:profile-data :c] ((juxt :profile-fact :target-request) record))))))
    (let [table (sorted-map "old" :retained) marker (Object.) calls (atom [])]
      (is (= (assoc table "fact" marker)
             (mir/add-runtime-check-facts
              fact-id (fn [& args] (swap! calls conj args) marker) core :type table)))
      (is (= [[core node :type]] @calls))
      (is (identical? table (mir/add-runtime-check-facts fact-id nil (assoc core :core-nodes [{}]) :type table))))
    (let [error (ex-info "owner id failure" {})
          caught (try (mir/runtime-check-fact-record (fn [& _] (throw error)) op-id token-id core node :type)
                      nil (catch clojure.lang.ExceptionInfo e e))]
      (is (identical? error caught)))))

(deftest fact-table-wrappers-retain-late-owner-var-identity
  (doseq [[name arguments ports]
          [['runtime-check-fact-record [:core :node :type]
            [#'b/p15-s23-c11-mir-runtime-check-fact-id #'b/p15-s23-c11-mir-runtime-check-operation-id
             #'b/p15-s23-c11-mir-runtime-check-token-id]]
           ['add-runtime-check-facts [:core :type :table]
            [#'b/p15-s23-c11-mir-runtime-check-fact-id #'b/p15-s23-c11-mir-runtime-check-fact-record]]
           ['reindex-fact-table [:nodes :table :key :suffix] []]
           ['profile-target-table [:core] []] ['capability-proof-table [:core] []]
           ['safety-proof-table [:core] []]]]
    (let [stage (ns-resolve 'gravity.mir name)
          wrapper (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-c11-mir-" name)))
          marker (Object.) observed (atom nil)]
      (with-redefs-fn {stage (fn [& args] (reset! observed args) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (= (concat ports arguments) @observed))
      (is (every? true? (map identical? ports (take (count ports) @observed)))))))

(deftest native-use-records-preserve-order-indices-and-guard-edges
  (let [nodes [{:node-id :late :operands [:middle]} {:node-id :middle :operands [:root]}
               {:node-id :unrelated :operands []}]]
    (is (= #{:root :middle :late} (mir/downstream-ids nodes :root)))
    (is (= (mir/downstream-ids nodes :root) (mir/downstream-ids (reverse nodes) :root))))
  (let [check {:check-id :check}
        nodes [{:node-id :node :operands [:first :second] :safety {:check check}}]
        observed (atom [])
        flow (mir/expected-data-flow (fn [value] (swap! observed conj value) :token)
                                     nodes {:node {:block-id :block}})]
    (is (= [check] @observed))
    (is (= [:token :first :second] (mapv :from flow)))
    (is (= [0 1 2] (mapv :operand-index flow)))
    (is (= [:runtime-check-guard :operand :operand] (mapv :edge-kind flow)))
    (is (= [{:use-kind :instruction :consumer-kind :operation :consumer-id :node
             :consumer-block :block :operand-index 2 :edge-kind :operand}]
           (mir/expected-uses flow :second)))
    (is (= [] (mir/expected-uses flow :absent)))
    (is (= [{:use-kind :terminator :consumer-kind :terminator :consumer-id :return
             :consumer-block :block :operand-index 0 :edge-kind :return-value}]
           (mir/expected-terminator-value-uses
            [{:value-id :second :consumer-kind :terminator :consumer-id :return
              :consumer-block :block :operand-index 0 :edge-kind :return-value}] :second)))))

(deftest native-value-records-retain-source-facts-and-owner-callbacks
  (let [source {:origin-id :origin :span {:source "value.gravity"} :generated-origin [:macro]}
        ownership {:model :persistent} effects #{:error/throw} safety {:outcome :runtime-checked}
        node {:node-id :value :source-operation :literal :attributes {:value false} :type :gravity/bool
              :source source :ownership ownership :effects effects :safety safety}
        events (atom []) uses [(Object.)] terminator-uses [(Object.)]
        opcode (fn [value] (swap! events conj [:opcode value]) :constant)
        use-op (fn [flow id] (swap! events conj [:uses flow id]) uses)
        term-op (fn [flow id] (swap! events conj [:terminators flow id]) terminator-uses)
        value (mir/expected-value opcode use-op term-op node :flow :terminators {:block-id :block})]
    (is (= [[:opcode node] [:uses :flow :value] [:terminators :terminators :value]] @events))
    (is (= {:present? true :value false} (:constant-payload value)))
    (is (= {:value-id :value :operation-id :value :block-id :block} (:defined-by value)))
    (is (= (vec (concat uses terminator-uses)) (:uses value)))
    (doseq [[key original] [[:source source] [:ownership ownership] [:effects effects] [:safety safety]]]
      (is (identical? original (get value key))))
    (let [definition {:parameter-index 0 :block-id :entry}]
      (reset! events [])
      (let [parameter (mir/expected-value opcode use-op term-op node :flow :terminators nil
                                          {:definition definition})]
        (is (= :parameter (:kind parameter)))
        (is (= :not-applicable (:constant-payload parameter)))
        (is (identical? definition (:defined-by parameter)))
        (is (= [[:uses :flow :value] [:terminators :terminators :value]] @events))))
    (let [error (ex-info "owner opcode" {})
          caught (try (mir/expected-value (fn [_] (throw error)) use-op term-op node nil nil nil)
                      nil (catch clojure.lang.ExceptionInfo e e))]
      (is (identical? error caught)))))

(deftest native-use-wrappers-retain-ports-and-optional-parameter-dispatch
  (doseq [[name arguments ports]
          [['downstream-ids [:nodes :if] []]
           ['expected-block-operation-ids [:core]
            [#'b/p15-s23-c11-mir-entry-parameter! #'b/p15-s23-c11-mir-runtime-check-operation-id
             #'b/p15-s23-c11-mir-path-prefix?]]
           ['expected-data-flow [:nodes :operations] [#'b/p15-s23-c11-mir-runtime-check-token-id]]
           ['expected-uses [:flow :value] []] ['expected-terminator-uses [:core] []]
           ['expected-terminator-value-uses [:uses :value] []]
           ['expected-value [:node :flow :uses :operation :parameter]
            [#'b/p15-s23-c11-mir-node-opcode #'b/p15-s23-c11-mir-expected-uses
             #'b/p15-s23-c11-mir-expected-terminator-value-uses]]
           ['expected-runtime-check-value [:node :flow :operation]
            [#'b/p15-s23-c11-mir-runtime-check-token-id #'b/p15-s23-c11-mir-runtime-check-operation-id
             #'b/p15-s23-c11-mir-expected-uses #'b/p15-s23-c11-mir-runtime-check-source]]]]
    (let [stage (ns-resolve 'gravity.mir name)
          wrapper (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-c11-mir-" name)))
          marker (Object.) observed (atom nil)]
      (with-redefs-fn {stage (fn [& args] (reset! observed args) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (= (concat ports arguments) @observed))
      (is (every? true? (map identical? ports (take (count ports) @observed))))))
  (let [original b/p15-s23-c11-mir-expected-value observed (atom nil)]
    (with-redefs [b/p15-s23-c11-mir-expected-value (fn [& args] (reset! observed args) :five-arguments)]
      (is (= :five-arguments (original :node :flow :uses :operation)))
      (is (= [:node :flow :uses :operation nil] @observed)))))

(defn- flow-operations []
  (into {} (for [key [:require! :entry-parameter! :expected-data-flow :expected-terminator-uses
                      :runtime-check-token-id :runtime-check-operation-id :expected-value
                      :expected-runtime-check-value :runtime-check-source
                      :valid-dominance-edge? :valid-terminator-use?]]
             [key (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-c11-mir-" (name key))))])))

(deftest native-data-flow-rules-accept-emitted-mir-and-refuse-changed-tables
  (doseq [{:keys [source checked operations by-id operation-products] emitted :mir} @products]
    (let [inputs (assoc operation-products :operation-by-id by-id :operations operations)
          actual (mir/validate-data-flow! (flow-operations) source checked emitted inputs)]
      (is (= {:edge-count 4 :terminator-use-count 4 :definition-count 5 :value-count 5} actual))
      (is (= actual (b/p15-s23-c11-mir-validate-data-flow! source checked emitted inputs)))
      (doseq [[changed rule missing]
              [[(assoc-in emitted [:data-flow-graph :edges 0 :from] "not-defined")
                "C11-DOMINANCE" :exact-checked-core-data-flow]
               [(assoc-in emitted [:data-flow-graph :terminator-uses] [])
                "C11-DOMINANCE" :exact-terminator-use-sites]
               [(assoc-in emitted [:data-flow-graph :definitions] {})
                "C11-DOMINANCE" :exact-ssa-definition-table]
               [(assoc-in emitted [:data-flow-graph :values] {})
                "C11-TYPE" :exact-ssa-value-table]
               [(assoc emitted :source-map {}) "C11-ORIGIN" :exact-checked-core-source-map]]]
        (let [error (failure #(mir/validate-data-flow! (flow-operations) source checked changed inputs))]
          (is (= rule (:id error)))
          (is (= missing (:missing-fact error)))))
      (let [changed (assoc-in inputs [:operations 0 :effects] #{:memory/raw})
            error (failure #(mir/validate-data-flow! (flow-operations) source checked emitted changed))]
        (is (= "C11-EFFECT" (:id error)))
        (is (= :direct-effectful-operation-boundary (:missing-fact error)))))))

(deftest native-effect-placement-checks-dominance-join-pairs-and-instruction-order
  ;; These event-edge variants are supplied geometry data. The original
  ;; Gravity fixture has a pure graph and no effect-event edges; these calls
  ;; confer no effect legality or MIR admission.
  (doseq [{:keys [source by-id operation-products] emitted :mir} @products]
    (let [inputs (assoc operation-products :operation-by-id by-id)
          edges (mapv (fn [edge] {:before (:from edge) :after (:consumer-id edge)})
                      (get-in emitted [:data-flow-graph :edges]))
          supplied (assoc emitted :effect-order-graph {:event-edges edges})]
      (is (= :passed (mir/validate-effect-edge-placement! #'b/p15-s23-c11-mir-require! source emitted inputs)))
      (is (= :passed (mir/validate-effect-edge-placement! #'b/p15-s23-c11-mir-require! source supplied inputs)))
      (doseq [edge edges]
        (let [changed (assoc-in supplied [:effect-order-graph :event-edges]
                                [{:before (:after edge) :after (:before edge)}])
              error (failure #(mir/validate-effect-edge-placement!
                                #'b/p15-s23-c11-mir-require! source changed inputs))]
          (is (= "C11-EFFECT" (:id error)))
          (is (= :effect-edge-cfg-placement-and-order (:missing-fact error)))))
      (let [changed (assoc-in supplied [:effect-order-graph :event-edges 0 :before] "not-defined")]
        (is (= "C11-EFFECT"
               (:id (failure #(mir/validate-effect-edge-placement!
                                #'b/p15-s23-c11-mir-require! source changed inputs)))))))))

(deftest native-flow-rules-preserve-port-order-refusal-and-wrapper-identities
  (let [{:keys [source checked operations by-id operation-products] emitted :mir} (first @products)
        inputs (assoc operation-products :operation-by-id by-id :operations operations)
        events (atom []) owner (flow-operations)
        observe (fn [key]
                  (fn [& args] (swap! events conj key) (apply (get owner key) args)))
        supplied (merge owner (into {} (map #(vector % (observe %))
                                           [:entry-parameter! :expected-data-flow :expected-terminator-uses
                                            :expected-value])))
        error (ex-info "owner require failure" {:kind :owner})
        caught (try
                 (mir/validate-data-flow!
                  (assoc supplied :require! (fn [& _] (swap! events conj :require!) (throw error)))
                  source checked emitted inputs)
                 nil (catch clojure.lang.ExceptionInfo e e))]
    (is (identical? error caught))
    (is (= (vec (concat [:entry-parameter! :expected-data-flow :expected-terminator-uses]
                        (repeat (count (:core-nodes checked)) :expected-value) [:require!])) @events))
    (let [observed (atom []) original @(get owner :expected-data-flow)]
      (with-redefs [b/p15-s23-c11-mir-expected-data-flow
                    (fn [& args] (swap! observed conj args) (apply original args))]
        (is (= 4 (:edge-count (mir/validate-data-flow! owner source checked emitted inputs)))))
      (is (= [[(:nodes inputs) (:operation-by-id inputs)]] @observed))))
  (let [arguments (vec (repeatedly 4 #(Object.))) marker (Object.) observed (atom nil)]
    (with-redefs [mir/validate-data-flow! (fn [& args] (reset! observed args) marker)]
      (is (identical? marker (apply b/p15-s23-c11-mir-validate-data-flow! arguments))))
    (is (= (flow-operations) (first @observed)))
    (is (every? true? (map identical? arguments (rest @observed)))))
  (let [arguments (vec (repeatedly 3 #(Object.))) marker (Object.) observed (atom nil)]
    (with-redefs [mir/validate-effect-edge-placement! (fn [& args] (reset! observed args) marker)]
      (is (identical? marker (apply b/p15-s23-c11-mir-validate-effect-edge-placement! arguments))))
    (is (identical? #'b/p15-s23-c11-mir-require! (first @observed)))
    (is (every? true? (map identical? arguments (rest @observed))))))

(deftest paired-gravity-fixture-emits-mir-and-keeps-native-rule-results
  ;; Actual Gravity fixture/builder execution on normalized data. The fixture's
  ;; proof/status labels do not authenticate source/Core or admit a MIR artifact.
  (doseq [{:keys [extension source checked function blocks by-id positions envelope operation-products]
           emitted :mir} @products]
    (is (.endsWith source extension))
    (is (= :gravity/mir-module (:artifact emitted)))
    (is (= 4 (count blocks)))
    (is (= :passed (:status ((fixture-helper 'verify-mir) checked emitted))))
    (let [rejected ((fixture-helper 'verify-mir) checked
                    (assoc-in emitted [:data-flow-graph :edges 0 :from] "not-defined"))]
      (is (= :rejected (:status rejected)))
      (is (= "C11-DOMINANCE" (get-in rejected [:diagnostics 0 :diagnostic-id]))))
    (let [direct (mir/validate-cfg! #'b/p15-s23-c11-mir-require! #'b/p15-s23-c11-mir-validate-terminator!
                                  source checked emitted envelope operation-products)]
      (is (= direct (b/p15-s23-c11-mir-validate-cfg! source checked emitted envelope operation-products)))
      (is (= 1 (:conditional-count direct)))
      (is (= (:entry function) (:entry-id direct))))
    (doseq [edge (get-in emitted [:data-flow-graph :edges])]
      (let [args [edge blocks by-id (get-in emitted [:data-flow-graph :definitions]) positions
                  (get-in emitted [:control-flow-graph :join])]]
        (is (apply mir/valid-dominance-edge? args))
        (is (= (apply mir/valid-dominance-edge? args)
               (apply b/p15-s23-c11-mir-valid-dominance-edge? args)))))
    (doseq [use (get-in emitted [:data-flow-graph :terminator-uses])]
      (is (mir/valid-terminator-use? use blocks by-id positions))
      (is (= (mir/valid-terminator-use? use blocks by-id positions)
             (b/p15-s23-c11-mir-valid-terminator-use? use blocks by-id positions))))))

(deftest malformed-terminators-and-cfgs-reject-with-the-existing-c11-rule
  (doseq [{:keys [source checked function blocks envelope operation-products] emitted :mir} @products
          change [#(assoc-in % [(:entry function) :terminator :effects] #{:io/write})
                  #(assoc-in % [(:entry function) :terminator :ordering] :sequence)
                  #(assoc-in % [(:entry function) :terminator :profile] :other)
                  #(assoc-in % [(:entry function) :terminator :source] {})
                  #(assoc-in % [(:entry function) :terminator :operands] [])
                  #(assoc-in % [(:entry function) :terminator :verifier-status] :passed)
                  #(assoc-in % [(:entry function) :terminator :forged] true)
                  #(assoc-in % [(:entry function) :dominators] [])]]
    (let [error (failure #(b/p15-s23-c11-mir-validate-cfg!
                           source checked emitted envelope (assoc operation-products :blocks (change blocks))))]
      (is (= "C11-BLOCK" (:id error)))
      (is (some? (:missing-fact error)))))
  (doseq [{:keys [source checked envelope operation-products] emitted :mir} @products]
    (is (= "C11-BLOCK"
           (:id (failure #(b/p15-s23-c11-mir-validate-cfg!
                            source checked (assoc-in emitted [:control-flow-graph :edges] [])
                            envelope operation-products)))))))

(deftest forged-value-uses-do-not-pass-native-dominance
  (doseq [{:keys [blocks by-id positions] emitted :mir} @products]
    (let [edge (first (get-in emitted [:data-flow-graph :edges]))
          definitions (get-in emitted [:data-flow-graph :definitions])
          join (get-in emitted [:control-flow-graph :join])
          use (first (get-in emitted [:data-flow-graph :terminator-uses]))]
      (doseq [changed [(assoc edge :from "not-defined")
                       (assoc edge :consumer-id "not-defined")
                       (assoc edge :consumer-kind :terminator)
                       (assoc edge :consumer-block "not-a-block")]]
        (is (not (mir/valid-dominance-edge? changed blocks by-id definitions positions join))))
      (doseq [changed [(assoc use :operand-index -1) (assoc use :operand-index 999)
                       (assoc use :consumer-id "not-a-terminator")
                       (assoc use :consumer-kind :operation) (assoc use :edge-kind :unknown)
                       (assoc use :value-id "not-defined") (assoc use :extra true)]]
        (is (not (mir/valid-terminator-use? changed blocks by-id positions)))))))

(deftest wrappers-keep-var-interposition-and-the-six-to-seven-argument-dispatch
  (let [{:keys [source checked envelope operation-products] emitted :mir} (first @products)
        calls (atom [])]
    (with-redefs [b/p15-s23-c11-mir-validate-terminator!
                  (fn [& args] (swap! calls conj args) :observed)]
      (is (= 1 (:conditional-count (b/p15-s23-c11-mir-validate-cfg!
                                    source checked emitted envelope operation-products))))
      (is (= 4 (count @calls)))
      (is (every? #(= source (first %)) @calls))))
  (let [original b/p15-s23-c11-mir-valid-dominance-edge? args [:edge :blocks :operations :definitions :positions :join]
        observed (atom nil)]
    (with-redefs [b/p15-s23-c11-mir-valid-dominance-edge?
                  (fn [& values] (reset! observed (vec values)) :seven-argument-port)]
      (is (= :seven-argument-port (apply original args)))
      (is (= (conj args nil) @observed)))))
