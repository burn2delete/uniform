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
  (is (= #{'validate-terminator! 'validate-cfg! 'valid-dominance-edge? 'valid-terminator-use?}
         (set (keys (ns-publics 'gravity.mir)))))
  (doseq [[name operation] (ns-publics 'gravity.mir)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.mir (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (some #{:general-cfgs} (:unsupported contract))))))

(deftest paired-gravity-fixture-emits-mir-and-keeps-native-rule-results
  ;; Actual Gravity fixture/builder execution on normalized data. The fixture's
  ;; proof/status labels do not authenticate source/Core or admit a MIR artifact.
  (doseq [{:keys [extension source checked function blocks by-id positions envelope operation-products]
           emitted :mir} @products]
    (is (.endsWith source extension))
    (is (= :gravity/mir-module (:artifact emitted)))
    (is (= 4 (count blocks)))
    (is (= :passed (:status ((fixture-helper 'verify-mir) checked emitted))))
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
