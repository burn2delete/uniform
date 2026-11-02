(ns gravity.mir.pipeline-test
  (:require [clojure.java.io :as io]
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
           'expected-runtime-check-value}
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
