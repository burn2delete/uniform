(ns gravity.self-hosting.sh08-loop-recur-type-test
  (:require [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- repository-root []
  (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh08_loop_recur_type_test.clj")))]
    (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file)))))
(defn- path [relative] (.getPath (io/file (repository-root) relative)))
(defn- effects [function & args]
  (apply (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf) function args))

(defn- body-helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh09-body-effect-test name)
      (throw (ex-info "Missing existing body-effect test helper" {:helper name}))))

(defn- c7-invoke [function arguments]
  ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7) function arguments))

(defn- fixture [disposition basename extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename extension))

(defn- seed-run [relative]
  (let [source (path relative) text (slurp source)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
        observed (atom []) original bootstrap/p15-s23-stage2-runtime-recur-signal
        value (with-redefs [bootstrap/p15-s23-stage2-runtime-recur-signal
                            (fn [values] (swap! observed conj (vec values)) (original values))]
                (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :loop-recur-explicit-seed-interpreter} plan 'run []))]
    {:value value :transfers @observed}))

(defn- node [id kind children scope attrs]
  {:node-id id :core-form kind :children children :binding-context scope :attributes attrs :resolved-binding-ids []
   :source {:form-id (keyword (str (name id) "-form")) :syntax-id id}
   :evaluation {:kind :left-to-right
                :order (mapv (fn [i child] {:index i :core-node-id child}) (range)
                             (cond (= kind :fn) [] (= kind :if) [(first children)] :else children))}})

(defn- loop-data []
  (let [nodes [(node :again-init :literal [] :initializers {}) (node :value-init :literal [] :initializers {})
               (node :condition :reference [] :loop-body {:binding-id :again})
               (node :next-again :literal [] :loop-body {}) (node :next-value :reference [] :loop-body {:binding-id :value})
               (-> (node :transfer :recur [:next-again :next-value] :loop-body
                         {:target-id :loop-target :target-kind :loop :arity 2 :type-compatibility :pending-sh08})
                   (assoc :resolved-binding-ids [:again :value])
                   (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
               (node :result :reference [] :loop-body {:binding-id :value})
               (node :branch :if [:condition :transfer :result] :loop-body {})
               (-> (node :loop-target :loop [:again-init :value-init :branch] :function-body
                         {:target-id :loop-target :binding-count 2 :body-count 1 :initializer-child-indexes [0 1]
                          :body-child-indexes [2] :result-policy {:kind :last-body :child-index 2}})
                   (assoc :resolved-binding-ids [:again :value])
                   (assoc-in [:evaluation :kind] :initializers-then-body-left-to-right))
               (node :function-target :fn [:loop-target] :namespace {:recur-target-id :function-target})
               (node :definition :def [:function-target] :namespace {})]
        target (fn [id kind bindings scope parent]
                 {:target-id id :target-kind kind :owner-core-node-id id :owner-form-id (keyword (str (name id) "-form"))
                  :owner-syntax-id id :arity (count bindings) :binding-ids bindings :body-scope-id scope
                  :parent-target-id parent :type-compatibility :pending-sh08})
        record (fn [id init ordinal prior]
                 {:loop-core-node-id :loop-target :ordinal ordinal :name (symbol (name id)) :binding-id id
                  :definition-form-id id :definition-syntax-id id :binding-scope-id :loop-body
                  :initializer-form-id (keyword (str (name init) "-form")) :initializer-syntax-id init
                  :initializer-scope-id :initializers :initializer-node-id init :visible-prior-binding-ids prior :mutability :immutable})
        core {:nodes nodes :loop-bindings [(record :again :again-init 0 []) (record :value :value-init 1 [:again])]
              :recur-targets [(target :loop-target :loop [:again :value] :loop-body :function-target)
                             (target :function-target :function [] :function-body nil)]
              :recur-transfers [{:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                                :target-id :loop-target :target-kind :loop :argument-node-ids [:next-again :next-value] :arity 2
                                :tail-position true :evaluation-order :arguments-left-to-right
                                :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}]
              :binding-table [{:binding-id :again :name 'again :scope-id :loop-body :definition-syntax-id :again}
                              {:binding-id :value :name 'value :scope-id :loop-body :definition-syntax-id :value}]}
        table {:again-init :gravity.type/bool :value-init :gravity.type/integer
               :condition :gravity.type/bool :next-again :gravity.type/bool :next-value :gravity.type/integer
               :transfer :gravity.type/never :result :gravity.type/integer :branch :gravity.type/integer
               :loop-target :gravity.type/integer}]
    [core table]))

(deftest loop-recur-authenticated-paired-composition
  ;; This selector authenticates real user products through current C7 and C8.
  ;; Kernel selectors exercise the same C7 source with normalized DATA only.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [products (mapv #((body-helper 'fresh-products) (fixture "accepted" "loop-recur-types" %)) [".gravity" ".qst"])
        results (mapv #((body-helper 'build-leaf) %) products)]
    (is (= (slurp (path (fixture "accepted" "loop-recur-types" ".gravity")))
           (slurp (path (fixture "accepted" "loop-recur-types" ".qst")))))
    (doseq [[p result extension] (map vector products results [".gravity" ".qst"])]
      (let [typed (:typed p) core (get-in p [:request :canonical-core-artifact])
            transfer (first (:recur-transfers core)) id (:recur-core-node-id transfer)
            target (first (filter #(= (:target-id transfer) (:target-id %)) (:recur-targets core)))
            graph (:effect-graph result)]
        (is (= :passed (:status (:report p)) (:status (:verification p))))
        (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
        (when-not (= :accepted (:status typed) (:status result))
          (throw (ex-info "Actual loop typing/effects rejected"
                         {:typed-status (:status typed) :effect-status (:status result)
                          :type-diagnostics (:diagnostics typed) :effect-diagnostics (:diagnostics result)})))
        (is (= :gravity.type/integer (:return (first (:function-type-table typed)))))
        (is (= :gravity.type/never (get-in typed [:type-table id])))
        (is (= 3 (count (filter #(= :sh08-loop-target-types-v1 (:producer-rule %)) (:constraint-ledger typed)))))
        (is (= :checked (get-in graph [:loop-fixed-point :equations])))
        (is (= target (get-in graph [:nodes id :loop-backedge :source-target])))
        (is (= transfer (get-in graph [:nodes id :loop-backedge :source-transfer])))
        (is (= (:request p) (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (is (= {:value 7 :transfers [[false 7]]} (seed-run (fixture "accepted" "loop-recur-types" extension))))
        (doseq [candidate [((body-helper 'reseal-effect-graph) result (update graph :nodes dissoc id))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :loop-backedge :target-id] "forged"))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :dependencies] (:argument-node-ids transfer)))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :transitive] #{:compiler/read-ir}))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:loop-fixed-point :equations] :unchecked))]]
          (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) candidate)))))))
    (is (= (get-in products [0 :typed :identity-input]) (get-in products [1 :typed :identity-input])))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (let [p (first products) typed (:typed p) request (:request p) core (:canonical-core-artifact request)
          transfer (first (:recur-transfers core)) slot (first (:loop-bindings core))
          id (:recur-core-node-id transfer)]
      (is (= :gravity.type/integer
             (c7-invoke 'sh08-ft-normal-return-type [:if [:gravity.type/bool :gravity.type/never :gravity.type/integer]
                                                    :gravity.type/unknown])))
      (is (= :accepted
             (:status (c7-invoke 'sh08-ft-loop-type-step
                        [(c7-invoke 'sh08-ft-loop-working-core
                           [core (get-in request [:b47-context :authenticated-core-request :binding-table])])
                         (:type-table typed) {} 65536]))))
      (doseq [candidate [(assoc-in typed [:type-table (:initializer-node-id slot)] :gravity.type/string)
                         (assoc-in typed [:type-table id] :gravity.type/integer)
                         (update typed :type-table dissoc id)
                         (update typed :constraint-ledger
                           #(mapv (fn [row] (if (= id (:lhs row))
                                             (assoc-in row [:dependencies :loop-premise :source-target :target-id] "forged") row)) %))
                         (update typed :constraint-ledger #(filterv (fn [row] (not= :sh08-loop-target-types-v1 (:producer-rule row))) %))
                         (update typed :constraint-ledger
                           #(mapv (fn [row] (if (= id (:lhs row))
                                             (assoc-in row [:dependencies :loop-premise :source-transfer :argument-node-ids]
                                                       (vec (reverse (:argument-node-ids transfer)))) row)) %))]]
        (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [request candidate]))))))
    (doseq [[basename reason] [["loop-recur-slot-type" :loop-recur-slot-type-mismatch]
                               ["loop-recur-return-type" :function-result-type-mismatch]]
            extension [".gravity" ".qst"]]
      (let [c5 (bootstrap/compiler-c5-resolution-file-artifact (path (fixture "rejected" basename extension)))
            request ((body-helper 'fresh-request) c5)
            result (c7-invoke 'sh08-function-type-core-artifact [request]) diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= ["C7-TYPE-MISMATCH" reason] [(:rule diagnostic) (:reason diagnostic)]))
        (when (= basename "loop-recur-slot-type")
          (let [node (first (filter #(= (:core-node-id diagnostic) (:node-id %))
                                   (get-in request [:canonical-core-artifact :nodes])))]
            (is (= "wrong" (get-in node [:attributes :value])))))))))

(deftest loop-c6-controls-precede-types-current-source
  (require 'gravity.self-hosting.sh07-loop-recur-test)
  (let [diagnostic-result (ns-resolve 'gravity.self-hosting.sh07-loop-recur-test 'diagnostic-result)
        diagnostic-data (ns-resolve 'gravity.self-hosting.sh07-loop-recur-test 'diagnostic-data)]
    (doseq [[basename reason] [["recur-without-target" :recur-target-required]
                               ["loop-recur-arity-mismatch" :recur-arity-mismatch]
                               ["recur-not-tail" :recur-tail-position-required]
                               ["outer-loop-hidden-by-function" :recur-arity-mismatch]]]
      (let [source (path (str "bootstrap/clojure/fixtures/self-hosting/sh-07-b4/rejected/" basename ".gravity"))
            diagnostic (diagnostic-data (diagnostic-result #(bootstrap/sh07-core-file-artifact source)))]
        (is (= ["C6-VERIFY" reason] [(:rule diagnostic) (get-in diagnostic [:facts :reason])])))))
  (doseq [[disposition basename] [["accepted" "loop-recur-types"]
                                  ["rejected" "loop-recur-slot-type"]
                                  ["rejected" "loop-recur-return-type"]]]
    (is (= (slurp (path (fixture disposition basename ".gravity")))
           (slurp (path (fixture disposition basename ".qst")))))))

(deftest normal-return-bottom-development-kernel
  (doseq [[form children nominal expected]
          [[:if [:gravity.type/bool :gravity.type/never :gravity.type/integer] :gravity.type/unknown :gravity.type/integer]
           [:if [:gravity.type/integer :gravity.type/string :gravity.type/never] :gravity.type/unknown :gravity.type/string]
           [:if [:gravity.type/never :gravity.type/integer :gravity.type/integer] :gravity.type/unknown :gravity.type/never]
           [:if [:gravity.type/nil :gravity.type/never :gravity.type/never] :gravity.type/unknown :gravity.type/never]
           [:if [:gravity.type/bool :gravity.type/unknown :gravity.type/never] :gravity.type/unknown :gravity.type/unknown]
           [:do [:gravity.type/never :gravity.type/integer] :gravity.type/integer :gravity.type/never]
           [:let [:gravity.type/never :gravity.type/integer] :gravity.type/integer :gravity.type/never]
           [:collection-literal [:gravity.type/integer :gravity.type/never] :collection :gravity.type/never]
           [:alternatives [] :gravity.type/unknown :gravity.type/unknown]
           [:fn [:gravity.type/never] :function-value :function-value]]]
    (is (= expected (c7-invoke 'sh08-ft-normal-return-type [form children nominal])))))

(defn- effect-loop-data []
  (let [[core table] (loop-data)
        ids (zipmap (concat (map :node-id (:nodes core)) [:again :value])
                    (map #(str "sha256:" (format "%064x" %)) (range 1 100)))
        [core table] (walk/postwalk-replace ids [core table])
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))
        checked (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])
        module {:profile :meta :target :jvm :effects [] :capabilities []}
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        rows (mapv (fn [p] (effects 'body-loop-row p (get nodes (or (:initializer-node-id p)
                                                            (get-in p [:source-transfer :recur-core-node-id]))) module))
                   (:constraints checked))]
    [core {:module module :type-table table :constraint-ledger rows :function-type-table []} ids]))

(deftest loop-effect-backedge-development-kernel
  (let [[core facts ids] (effect-loop-data)
        result (effects 'body-derive-loop-nodes core facts {} 65536)
        graph (:nodes result) transfer (ids :transfer) body (ids :branch)]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= [(ids :next-again) (ids :next-value) body] (get-in graph [transfer :dependencies])))
    (is (= [body] (get-in graph [transfer :loop-backedge :body-core-node-ids])))
    (is (= (first (:recur-transfers core)) (get-in graph [transfer :loop-backedge :source-transfer])))
    (is (= :gravity.type/never (get-in graph [transfer :type])))
    (is (= #{} (get-in graph [transfer :transitive])))
    (is (= (mapv ids [:again-init :value-init :branch]) (get-in graph [(ids :loop-target) :evaluated-children])))
    (is (true? (effects 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status (effects 'body-derive-loop-nodes core facts {} (:work-count result)))))
    (is (= :body-deferred-work-limit
           (:reason (effects 'body-derive-loop-nodes core facts {} (dec (:work-count result))))))
    (doseq [budget [0 2 6]]
      (is (= :body-deferred-work-limit (:reason (effects 'body-loop-rows core facts budget)))))
    (doseq [indexes [[0] [1] [2 2] [3] [-1] ["2"] [nil] 2]]
      (let [bad (update core :nodes #(mapv (fn [node] (if (= :loop (:core-form node))
                                                        (assoc-in node [:attributes :body-child-indexes] indexes) node)) %))]
        (is (= :incoherent-loop-result-type (:reason (effects 'body-derive-loop-nodes bad facts {} 65536))))))
    (let [seal (fn [c] (assoc c :identity-preimage (select-keys c [:loop-bindings :recur-targets :recur-transfers])))
          recur-node (first (filter #(= :recur (:core-form %)) (:nodes core)))
          duplicate (seal (-> core (update :nodes conj (assoc recur-node :node-id "sha256:duplicate-recur"))
                                  (update :recur-transfers conj (first (:recur-transfers core)))))
          duplicate-facts (update facts :constraint-ledger conj (last (:constraint-ledger facts)))]
      (is (= :incomplete-loop-control-coverage (:reason (effects 'body-loop-joins duplicate duplicate-facts 65536))))
      (doseq [bad [(assoc core :recur-targets [7])
                   (assoc-in core [:recur-targets 0 :arity] nil)
                   (assoc-in core [:recur-targets 0 :arity] "two")
                   (assoc-in core [:recur-targets 0 :binding-ids] 7)
                   (assoc-in core [:loop-bindings 0 :ordinal] "zero")
                   (assoc-in core [:recur-transfers 0 :argument-node-ids] 7)
                   (-> core (assoc :loop-bindings [])
                            (assoc-in [:recur-targets 0 :binding-ids] [])
                            (update-in [:recur-targets 0] dissoc :arity))]]
        (is (= :rejected (:status (effects 'body-derive-loop-nodes (seal bad) facts {} 65536))))))
    (doseq [[c f] [[core (assoc-in facts [:type-table (ids :value-init)] :gravity.type/string)]
                   [core (assoc-in facts [:type-table transfer] :gravity.type/integer)]
                   [core (assoc-in facts [:type-table (ids :loop-target)] :gravity.type/string)]
                   [core (assoc facts :constraint-ledger [])]
                   [core (assoc-in facts [:constraint-ledger 0 :solution :type] :gravity.type/string)]
                   [(assoc-in core [:recur-transfers 0 :target-id] (ids :function-target)) facts]
                   [(update core :nodes #(filterv (fn [n] (not= body (:node-id n))) %)) facts]]]
      (is (= :rejected (:status (effects 'body-derive-loop-nodes c f {} 65536)))))
    (is (false? (effects 'body-graph-equations-valid? core facts {}
                        (assoc-in graph [transfer :loop-backedge :body-core-node-ids] []))))
    (let [inflated (:nodes (effects 'body-derive-nodes-pass core facts {} 65536
                              {:loop-summaries {body #{:compiler/read-ir}}} #{}))]
      (is (true? (effects 'body-graph-equations-valid? core facts {} inflated)))
      (is (not= graph inflated)))
    (is (true? (effects 'body-normal-compatible? :gravity.type/never :gravity.type/integer)))
    (is (false? (effects 'body-normal-compatible? :gravity.type/unknown :gravity.type/integer)))))

(deftest loop-working-core-retains-authenticated-bindings-development-kernel
  (let [[normalized table] (loop-data)
        canonical (dissoc normalized :binding-table)
        bindings (:binding-table normalized)
        working (c7-invoke 'sh08-ft-loop-working-core [canonical bindings])
        checked (c7-invoke 'sh08-ft-loop-type-step [working table {} 65536])]
    (is (not (contains? canonical :binding-table)))
    (is (= canonical (dissoc working :binding-table)))
    (is (= bindings (:binding-table working)))
    (is (= :accepted (:status checked)))
    (is (= (c7-invoke 'sh08-ft-loop-type-step [normalized table {} 65536]) checked))
    (doseq [invalid [nil {} [] [(assoc (first bindings) :scope-id :forged)]]]
      (let [bad-working (c7-invoke 'sh08-ft-loop-working-core [canonical invalid])
            result (c7-invoke 'sh08-ft-loop-type-step [bad-working table {} 65536])]
        (is (= invalid (:binding-table bad-working)))
        (is (= :rejected (:status result)))))
    (is (not (contains? canonical :binding-table)))))

(deftest loop-slot-transfer-development-kernel
  (let [[core table] (loop-data) result (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])]
    (is (= :accepted (:status result)))
    (is (= {:again :gravity.type/bool :value :gravity.type/integer} (:bindings result)))
    (is (= [:solved :solved :solved] (mapv :status (:constraints result))))
    (is (= :gravity.type/never (:result-type (last (:constraints result)))))
    (is (= :loop-type-work-limit (get-in (c7-invoke 'sh08-ft-loop-type-step [core table {} (dec (:work-count result))]) [:issues 0 :reason])))
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core table {} (:work-count result)]))))
    (let [bad (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/string) {} 65536])]
      (is (= ["C7-TYPE-MISMATCH" :next-value :gravity.type/integer :gravity.type/string]
             ((juxt :rule :core-node-id :expected :actual) (first (:issues bad))))))
    (let [pending (c7-invoke 'sh08-ft-loop-type-step [core (dissoc table :value-init) {} 65536])]
      (is (= :gravity.type/unknown (get-in pending [:bindings :value])))
      (is (= :pending (:status (last (:constraints pending))))))
    (let [never (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/never) {} 65536])]
      (is (= :accepted (:status never)))
      (is (= [:gravity.type/bool :gravity.type/never] (:argument-types (last (:constraints never)))))
      (is (= false (:normal-transfer? (last (:constraints never)))))
      (is (= :solved (:status (last (:constraints never))))))
    (let [sequential (-> core (assoc-in [:nodes 1 :core-form] :reference)
                              (assoc-in [:nodes 1 :attributes] {:binding-id :again}))
          checked (c7-invoke 'sh08-ft-loop-type-step [sequential (assoc table :value-init :gravity.type/bool :next-value :gravity.type/bool) {} 65536])]
      (is (= :accepted (:status checked)))
      (is (= :gravity.type/bool (get-in checked [:bindings :value]))))
    (doseq [bad [(assoc-in core [:loop-bindings 1 :visible-prior-binding-ids] [])
                 (assoc-in core [:recur-transfers 0 :target-id] :function-target)
                 (assoc-in core [:recur-transfers 0 :tail-position] false)
                 (assoc-in core [:recur-transfers 0 :argument-node-ids] [:next-value :next-again])
                 (assoc-in core [:nodes 5 :attributes :target-id] :wrong)
                 (assoc-in core [:loop-bindings 0 :initializer-node-id] :value-init)
                 (assoc-in core [:recur-targets 0 :body-scope-id] :forged)
                 (-> core (assoc-in [:nodes 0 :core-form] :reference) (assoc-in [:nodes 0 :attributes] {:binding-id :value}))
                 (-> core (assoc-in [:recur-transfers 0 :argument-node-ids] [:next-again])
                     (assoc-in [:nodes 5 :children] [:next-again])
                     (assoc-in [:nodes 5 :evaluation :order] [{:index 0 :core-node-id :next-again}]))]]
      (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [bad table {} 65536])))))))

(deftest zero-slot-nested-nearest-development-kernel
  (let [transfer (-> (node :transfer :recur [] :inner-body
                           {:target-id :inner :target-kind :loop :arity 0 :type-compatibility :pending-sh08})
                     (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
        loop-node (fn [id child scope]
                    (assoc-in (node id :loop [child] scope {:target-id id :binding-count 0 :body-count 1
                                                           :initializer-child-indexes [] :body-child-indexes [0]
                                                           :result-policy {:kind :last-body :child-index 0}})
                              [:evaluation :kind] :initializers-then-body-left-to-right))
        target (fn [id scope parent]
                 {:target-id id :target-kind :loop :owner-core-node-id id :owner-form-id (keyword (str (name id) "-form"))
                  :owner-syntax-id id :arity 0 :binding-ids [] :body-scope-id scope :parent-target-id parent :type-compatibility :pending-sh08})
        core {:nodes [transfer (loop-node :inner :transfer :outer-body) (loop-node :outer :inner :namespace)]
              :loop-bindings [] :binding-table [] :recur-targets [(target :inner :inner-body :outer) (target :outer :outer-body nil)]
              :recur-transfers [{:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                :target-id :inner :target-kind :loop :argument-node-ids [] :arity 0 :tail-position true
                :evaluation-order :arguments-left-to-right :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}]}
        result (c7-invoke 'sh08-ft-loop-type-step [core {:transfer :gravity.type/never} {} 65536])]
    (is (= :accepted (:status result)))
    (is (= :solved (:status (first (:constraints result)))))
    (is (= [] (:argument-types (first (:constraints result)))))
    (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-transfers 0 :target-id] :outer) {} {} 65536]))))
    (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-targets 0 :parent-target-id] nil) {} {} 65536]))))))

(deftest function-recur-authenticated-paired-composition
  ;; This checks compiler acceptance/rejection. Annotated seed execution is
  ;; a separate boundary and is not replaced with an untyped runtime fixture.
  (let [products (mapv #((body-helper 'fresh-products) (fixture "accepted" "function-recur-types" %)) [".gravity" ".qst"])
        results (mapv #((body-helper 'build-leaf) %) products)]
    (is (= (slurp (path (fixture "accepted" "function-recur-types" ".gravity")))
           (slurp (path (fixture "accepted" "function-recur-types" ".qst")))))
    (doseq [[p result] (map vector products results)]
      (let [typed (:typed p) request (:request p) core (:canonical-core-artifact request)
            transfer (first (:recur-transfers core)) id (:recur-core-node-id transfer)
            target (first (filter #(= (:target-id transfer) (:target-id %)) (:recur-targets core)))
            function (first (filter #(= 'again (:definition-name %)) (:function-type-table typed)))
            function-index (first (keep-indexed #(when (= 'again (:definition-name %2)) %1) (:function-type-table typed)))
            rows (filter #(= :sh08-loop-target-types-v1 (:producer-rule %)) (:constraint-ledger typed))
            graph (:effect-graph result)]
        (is (= :passed (:status (:report p)) (:status (:verification p))))
        (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
        (when-not (= :accepted (:status typed) (:status result))
          (throw (ex-info "Actual function recur typing/effects rejected"
                         {:type-diagnostics (:diagnostics typed) :effect-diagnostics (:diagnostics result)})))
        (is (= :function (:target-kind transfer) (:target-kind target)))
        (is (= [:gravity.type/bool :gravity.type/string] (mapv :type (:parameters function))))
        (is (= (:binding-ids target) (:parameter-binding-ids function)))
        (is (= :gravity.type/string (:return function)))
        (is (= :gravity.type/never (get-in typed [:type-table id])))
        (is (= 1 (count rows)))
        (is (= [:gravity.type/bool :gravity.type/string] (get-in (first rows) [:solution :slot-types])))
        (is (= :checked (get-in graph [:loop-fixed-point :equations])))
        (is (= [(:body-core-node-id function)] (get-in graph [:nodes id :loop-backedge :body-core-node-ids])))
        (is (= target (get-in graph [:nodes id :loop-backedge :source-target])))
        (is (= transfer (get-in graph [:nodes id :loop-backedge :source-transfer])))
        (is (= request (get-in p [:binding :digest-resolution :preimage :request])))
        (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (doseq [candidate [(assoc-in typed [:type-table id] :gravity.type/string)
                           (assoc-in typed [:function-type-table function-index :parameters 1 :type] :gravity.type/bool)
                           (update typed :constraint-ledger #(filterv (fn [row] (not= :sh08-loop-target-types-v1 (:producer-rule row))) %))
                           (update typed :constraint-ledger
                             #(mapv (fn [row] (if (= id (:lhs row))
                                               (assoc-in row [:dependencies :loop-premise :source-target :target-kind] :loop) row)) %))]]
          (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [request candidate])))))
        (doseq [candidate [((body-helper 'reseal-effect-graph) result (update graph :nodes dissoc id))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :loop-backedge :target-id] "forged"))
                           ((body-helper 'reseal-effect-graph) result
                            (assoc-in graph [:nodes id :loop-backedge :body-core-node-ids] (:argument-node-ids transfer)))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:nodes id :dependencies] (:argument-node-ids transfer)))
                           ((body-helper 'reseal-effect-graph) result (assoc-in graph [:loop-fixed-point :equations] :unchecked))]]
          (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) candidate)))))))
    (is (= (get-in products [0 :typed :identity-input]) (get-in products [1 :typed :identity-input])))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (doseq [[basename reason rule] [["function-recur-slot-type" :function-recur-slot-type-mismatch "C7-TYPE-MISMATCH"]
                                    ["function-recur-return-type" :function-result-type-mismatch "C7-TYPE-MISMATCH"]
                                    ["function-recur-unresolved-slot" :function-recur-parameter-types-required "C7-ANNOTATION"]]
            extension [".gravity" ".qst"]]
      (let [source (fixture "rejected" basename extension)
            c5 (bootstrap/compiler-c5-resolution-file-artifact (path source))
            request ((body-helper 'fresh-request) c5)
            result (c7-invoke 'sh08-function-type-core-artifact [request])
            diagnostic (first (filter #(= reason (:reason %)) (:diagnostics result)))]
        (is (= :rejected (:status result)))
        (is (= [rule reason] ((juxt :rule :reason) diagnostic)) (pr-str (:diagnostics result)))
        (is (= (slurp (path (fixture "rejected" basename ".gravity")))
               (slurp (path (fixture "rejected" basename ".qst")))))))))

(deftest function-recur-annotated-seed-execution
  ;; Legality is covered by function-recur-authenticated-paired-composition
  ;; over these exact files. This selector executes their unchanged bytes.
  (let [gravity (slurp (path (fixture "accepted" "function-recur-types" ".gravity")))
        qst (slurp (path (fixture "accepted" "function-recur-types" ".qst")))]
    (is (= gravity qst))
    (is (= "c9068acec85998d8dcba1b22e165b66ca9e73f5fae04f17229611ed6ad1d63f9"
           (bootstrap/sha256-hex gravity)))
    (doseq [extension [".gravity" ".qst"]]
      (let [source (path (fixture "accepted" "function-recur-types" extension)) text (slurp source)
            emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
            observed (atom []) original bootstrap/p15-s23-stage2-runtime-recur-signal
            value (with-redefs [bootstrap/p15-s23-stage2-runtime-recur-signal
                               (fn [values] (swap! observed conj values) (original values))]
                    (bootstrap/p15-s23-stage2-runtime-execute-function
                     {:engine :function-recur-explicit-annotated-seed-interpreter} compiled 'run []))]
        (is (= text gravity))
        (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in compiled [:source :sha256])))
        (is (= {:parameters '[flag :- Bool value :- String] :return '[typed/return (quote String)]}
               (get-in compiled [:functions 'again :source-declarations])))
        (is (= '[flag value] (get-in compiled [:functions 'again :params])))
        (is (= "done" value))
        (is (= [[false "done"]] @observed))))))

(defn- function-recur-data []
  (let [signature {:parameters [{:type-id :gravity.type/bool} {:type-id :gravity.type/string}]
                   :return {:type-id :gravity.type/string}}
        nodes [(node :condition :reference [] :function-body {:binding-id :flag})
               (node :next-flag :literal [] :function-body {})
               (node :next-value :reference [] :function-body {:binding-id :value})
               (-> (node :transfer :recur [:next-flag :next-value] :function-body
                         {:target-id :function-target :target-kind :function :arity 2 :type-compatibility :pending-sh08})
                   (assoc :resolved-binding-ids [:flag :value])
                   (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
               (node :result :reference [] :function-body {:binding-id :value})
               (node :branch :if [:condition :transfer :result] :function-body {})
               (assoc (node :function-target :fn [:branch] :namespace
                            {:parameters ['flag 'value] :recur-target-id :function-target :signature signature})
                      :resolved-binding-ids [:flag :value])]
        target {:target-id :function-target :target-kind :function :owner-core-node-id :function-target
                :owner-form-id :function-target-form :owner-syntax-id :function-target :arity 2
                :binding-ids [:flag :value] :body-scope-id :function-body :parent-target-id nil :type-compatibility :pending-sh08}
        transfer {:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                  :target-id :function-target :target-kind :function :argument-node-ids [:next-flag :next-value] :arity 2
                  :tail-position true :evaluation-order :arguments-left-to-right
                  :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}
        core {:nodes nodes :loop-bindings [] :recur-targets [target] :recur-transfers [transfer]
              :binding-table [{:binding-id :flag :name 'flag} {:binding-id :value :name 'value}]}
        table {:condition :gravity.type/bool :next-flag :gravity.type/bool :next-value :gravity.type/string
               :transfer :gravity.type/never :result :gravity.type/string :branch :gravity.type/string}]
    [core table]))

(deftest function-recur-node-inference-development-kernel
  (let [[core prefilled] (function-recur-data)
        table (dissoc prefilled :transfer)
        transfer (first (filter #(= :recur (:core-form %)) (:nodes core)))
        branch (first (filter #(= :if (:core-form %)) (:nodes core)))
        nominal (c7-invoke 'sh08-ft-infer-node-nominal [transfer table {} {} {} {}])
        inferred (c7-invoke 'sh08-ft-infer-node-with-context [transfer table {} {} {} {}])
        joined (assoc table :transfer inferred)]
    (is (not (contains? table :transfer)))
    (is (= :gravity.type/never nominal inferred))
    (is (= :gravity.type/string
           (c7-invoke 'sh08-ft-infer-node-with-context [branch joined {} {} {} {}])))
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core joined {} 65536]))))
    (is (= :gravity.type/never
           (c7-invoke 'sh08-ft-infer-node-with-context
             [(assoc-in transfer [:attributes :target-kind] :loop) table {} {} {} {}])))
    (is (= :gravity.type/unknown
           (c7-invoke 'sh08-ft-infer-node-with-context
             [(assoc-in transfer [:attributes :target-kind] :forged) table {} {} {} {}])))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(assoc-in core [:recur-transfers 0 :target-id] :forged) joined {} 65536]) [:issues 0 :rule])))
    (is (= "C7-ANNOTATION"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(update-in core [:nodes 6 :attributes] dissoc :signature) joined
                      {:flag :gravity.type/bool :value :gravity.type/string} 65536]) [:issues 0 :rule])))))

(deftest function-recur-slots-development-kernel
  (let [[core table] (function-recur-data)
        result (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])
        premise (first (:constraints result))]
    (is (= :accepted (:status result)))
    (is (= [:gravity.type/bool :gravity.type/string] (:slot-types premise) (:argument-types premise)))
    (is (= :gravity.type/never (:result-type premise)))
    (is (= :solved (:status premise)))
    (is (= :loop-type-work-limit
           (get-in (c7-invoke 'sh08-ft-loop-type-step [core table {} (dec (:work-count result))]) [:issues 0 :reason])))
    (is (= result (c7-invoke 'sh08-ft-loop-type-step [core table {} (:work-count result)])))
    (doseq [[id type expected] [[:next-value :gravity.type/bool :gravity.type/string]
                               [:next-flag :gravity.type/string :gravity.type/bool]]]
      (let [bad (c7-invoke 'sh08-ft-loop-type-step [core (assoc table id type) {} 65536])]
        (is (= ["C7-TYPE-MISMATCH" :function-recur-slot-type-mismatch id expected type]
               ((juxt :rule :reason :core-node-id :expected :actual) (first (:issues bad)))))))
    (let [unannotated (update-in core [:nodes 6 :attributes] dissoc :signature)
          caller-types {:flag :gravity.type/bool :value :gravity.type/string}
          pending (c7-invoke 'sh08-ft-loop-type-step [unannotated table caller-types 65536])]
      (is (= ["C7-ANNOTATION" :function-recur-parameter-types-required]
             ((juxt :rule :reason) (first (:issues pending)))))
      (is (= "C7-VERIFY"
             (get-in (c7-invoke 'sh08-ft-loop-type-step
                       [(assoc-in unannotated [:recur-transfers 0 :target-id] :forged) table caller-types 65536]) [:issues 0 :rule]))))
    (let [never (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/never) {} 65536])]
      (is (= :accepted (:status never)))
      (is (= false (:normal-transfer? (first (:constraints never))))))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(assoc-in core [:binding-table 1 :name] 'forged) table {} 65536]) [:issues 0 :rule])))))

(deftest function-recur-backedge-development-kernel
  (let [[core table] (function-recur-data)
        ids (zipmap (concat (map :node-id (:nodes core)) [:flag :value])
                    (map #(str "sha256:" (format "%064x" %)) (range 101 200)))
        [core table] (walk/postwalk-replace ids [core table])
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))
        owner (last (:nodes core)) target (first (:recur-targets core))
        function {:function-id (:node-id owner) :function-core-node-id (:node-id owner)
                  :body-core-node-id (ids :branch) :parameter-binding-ids (mapv ids [:flag :value])
                  :parameters [{:binding-id (ids :flag) :type :gravity.type/bool}
                               {:binding-id (ids :value) :type :gravity.type/string}]
                  :signature (get-in owner [:attributes :signature])}
        checked (c7-invoke 'sh08-ft-loop-type-step [core table {} 65536])
        module {:profile :meta :target :jvm :effects [] :capabilities []}
        transfer-node (first (filter #(= :recur (:core-form %)) (:nodes core)))
        rows (mapv #(effects 'body-loop-row % transfer-node module) (:constraints checked))
        facts {:module module :type-table table :constraint-ledger rows :function-type-table [function]}
        derived (effects 'body-derive-module-nodes core facts {} 65536)
        graph (:nodes derived)]
    (is (= [:gravity.type/bool :gravity.type/string] (effects 'body-function-recur-slot-types target owner function)))
    (is (empty? (filter #(= :loop (:core-form %)) (:nodes core))))
    (is (true? (:recur-control? derived)))
    (is (= :accepted (:status derived)) (pr-str derived))
    (is (= [(ids :branch)] (get-in graph [(ids :transfer) :loop-backedge :body-core-node-ids])))
    (is (= (first (:recur-transfers core)) (get-in graph [(ids :transfer) :loop-backedge :source-transfer])))
    (is (= target (get-in graph [(ids :transfer) :loop-backedge :source-target])))
    (is (true? (effects 'body-graph-equations-valid? core facts {} graph)))
    (is (false? (effects 'body-graph-equations-valid? core facts {}
                  (assoc-in graph [(ids :transfer) :dependencies] [(ids :next-flag) (ids :next-value)]))))
    (doseq [bad [(assoc-in facts [:function-type-table 0 :parameters 1 :type] :gravity.type/bool)
                 (assoc-in facts [:function-type-table 0 :body-core-node-id] (ids :next-value))
                 (assoc-in facts [:function-type-table 0 :parameter-binding-ids] (mapv ids [:value :flag]))
                 (assoc-in facts [:constraint-ledger 0 :dependencies :loop-premise :slot-types] [])]]
      (is (= :rejected (:status (effects 'body-loop-joins core bad 65536)))))
    (is (nil? (effects 'body-function-recur-slot-types target owner (assoc function :parameters 3))))
    (doseq [[kind value reason] [[:empty [] :incomplete-loop-control-coverage]
                                 [:nil nil :incomplete-loop-control-tables]
                                 [:absent nil :incomplete-loop-control-tables]]]
      (let [missing (if (= kind :absent)
                      (-> core (dissoc :recur-transfers) (update :identity-preimage dissoc :recur-transfers))
                      (-> core (assoc :recur-transfers value) (assoc-in [:identity-preimage :recur-transfers] value)))
            rejected (effects 'body-derive-module-nodes missing facts {} 65536)]
        (is (true? (:recur-control? rejected)))
        (is (= [:rejected reason] ((juxt :status :reason) rejected)))))
    (doseq [malformed [7 {} "bad"]]
      (is (= [true :rejected :incomplete-loop-control-tables]
             ((juxt :recur-control? :status :reason)
              (effects 'body-derive-module-nodes (assoc core :recur-transfers malformed) facts {} 65536)))))
    (let [plain {:nodes [(first (:nodes core))] :call-edges [] :recur-transfers []}
          plain-facts (assoc facts :function-type-table [])]
      (doseq [ordinary [plain (dissoc plain :recur-transfers) (assoc plain :recur-transfers nil)]]
        (let [derived (effects 'body-derive-module-nodes ordinary plain-facts {} 65536)]
          (is (= :accepted (:status derived)))
          (is (false? (:recur-control? derived)))
          (is (= (effects 'body-derive-nodes-with-foreign ordinary plain-facts {} 65536) (dissoc derived :recur-control?))))))
    (let [[loop-core loop-facts] (effect-loop-data)
          derived (effects 'body-derive-module-nodes loop-core loop-facts {} 65536)]
      (is (true? (:recur-control? derived)))
      (is (= (effects 'body-derive-loop-nodes loop-core loop-facts {} 65536) (dissoc derived :recur-control?))))
    (let [[recursive-core recursive-facts] ((body-helper 'recursive-kernel-data))
          derived (effects 'body-derive-module-nodes recursive-core recursive-facts {} 65536)]
      (is (false? (:recur-control? derived)))
      (is (= (effects 'body-derive-recursive-nodes recursive-core recursive-facts {} 65536)
             (dissoc derived :recur-control?))))))

(deftest function-target-reset-is-not-forgery-development-kernel
  (let [transfer (-> (node :transfer :recur [] :function-body
                           {:target-id :function-target :target-kind :function :arity 0 :type-compatibility :pending-sh08})
                     (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
        owner (node :function-target :fn [:transfer] :namespace {:recur-target-id :function-target})
        target {:target-id :function-target :target-kind :function :owner-core-node-id :function-target
                :owner-form-id :function-target-form :owner-syntax-id :function-target :arity 0 :binding-ids []
                :body-scope-id :function-body :parent-target-id nil :type-compatibility :pending-sh08}
        record {:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer
                :target-id :function-target :target-kind :function :argument-node-ids [] :arity 0 :tail-position true
                :evaluation-order :arguments-left-to-right :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}
        core {:nodes [transfer owner] :binding-table [] :loop-bindings [] :recur-targets [target] :recur-transfers [record]}]
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core {} {} 65536]))))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-transfers 0 :target-id] :forged) {} {} 65536]) [:issues 0 :rule])))
    (let [outer (assoc-in (node :outer :loop [:function-target] :namespace
                                {:target-id :outer :binding-count 0 :body-count 1 :initializer-child-indexes []
                                 :body-child-indexes [0] :result-policy {:kind :last-body :child-index 0}})
                          [:evaluation :kind] :initializers-then-body-left-to-right)
          outer-target {:target-id :outer :target-kind :loop :owner-core-node-id :outer :owner-form-id :outer-form
                        :owner-syntax-id :outer :arity 0 :binding-ids [] :body-scope-id :outer-body
                        :parent-target-id nil :type-compatibility :pending-sh08}
          nested (-> core (assoc-in [:nodes 1 :binding-context] :outer-body) (update :nodes conj outer)
                         (update :recur-targets conj outer-target))]
      (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [nested {} {} 65536]))))
      (is (= "C7-VERIFY" (get-in (c7-invoke 'sh08-ft-loop-type-step [(-> nested
                                     (assoc-in [:recur-transfers 0 :target-id] :outer)
                                     (assoc-in [:recur-transfers 0 :target-kind] :loop)
                                     (assoc-in [:nodes 0 :attributes :target-id] :outer)
                                     (assoc-in [:nodes 0 :attributes :target-kind] :loop)) {} {} 65536]) [:issues 0 :rule]))))))
