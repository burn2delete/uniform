(ns gravity.self-hosting.sh08-loop-recur-type-test
  (:require [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.string :as str]
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
                         (:type-table typed) {}]))))
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
        checked (c7-invoke 'sh08-ft-loop-type-step [core table {}])
        module {:profile :meta :target :jvm :effects [] :capabilities []}
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        rows (mapv (fn [p] (effects 'body-loop-row p (get nodes (or (:initializer-node-id p)
                                                            (get-in p [:source-transfer :recur-core-node-id]))) module))
                   (:constraints checked))]
    [core {:module module :type-table table :constraint-ledger rows :function-type-table []} ids]))

(deftest current-gravity-declared-self-caller-loop-effects-development-kernel
  (let [[self-core self-facts self-bindings] ((body-helper 'declared-self-kernel-data))
        [old-loop old-facts old-ids] (effect-loop-data)
        replacements (zipmap (vals old-ids)
                             (map #(str "sha256:" (format "%064x" %)) (range 1000 1100)))
        [loop-core loop-facts ids] (walk/postwalk-replace replacements [old-loop old-facts old-ids])
        caller (ids :function-target)
        call-id (get-in self-core [:calls 1 :core-node-id])
        caller-nodes (mapv (fn [node]
                            (cond
                              (= (ids :result) (:node-id node))
                              (-> node (assoc :core-form :do :children [call-id] :attributes {} :resolved-binding-ids [])
                                  (assoc :evaluation {:kind :left-to-right :owner-function-syntax-id caller
                                    :order [{:index 0 :core-node-id call-id}]}))
                              (contains? #{:fn :def} (:core-form node)) node
                              :else (assoc-in node [:evaluation :owner-function-syntax-id] caller)))
                          (:nodes loop-core))
        external-ids (set (map #(get-in self-core [:nodes % :node-id]) [8 9 10]))
        self-nodes (mapv #(if (contains? external-ids (:node-id %))
                           (assoc-in % [:evaluation :owner-function-syntax-id] caller) %) (:nodes self-core))
        core (-> self-core
                 (assoc :nodes (into self-nodes caller-nodes))
                 (merge (select-keys loop-core [:loop-bindings :recur-targets :recur-transfers :identity-preimage]))
                 (update :binding-table into (:binding-table loop-core))
                 (update :function-records conj {:function-syntax-id caller :function-core-node-id caller
                   :body-core-node-id (ids :loop-target) :definition-kind :named-top-level :fixed-arity 0 :parameter-binding-ids []})
                 (assoc-in [:call-edges 1 :caller-function-syntax-id] caller)
                 (assoc-in [:call-edges 1 :caller-function-core-node-id] caller))
        caller-function {:function-id caller :body-core-node-id (ids :loop-target) :parameters []
                         :return :gravity.type/string :latent-effects [:error/throw] :thrown-error-effects [:pending-sh09]}
        bindings (into self-bindings (map (juxt :binding-id identity) (:binding-table loop-core)))
        c7-work (+ 32 (* 8 (count (:nodes core))) (* 8 (count (:calls core))) (* 4 (count (:call-edges core)))
                   (* 4 (count bindings)) (* 4 (count (:function-records core)))
                   (* 2 (reduce + (map (comp count :argument-node-ids) (:calls core))))
                   (* (count (:calls core)) (count bindings)))
        facts (-> self-facts
                  (assoc :module (:module self-core) :constraint-ledger (:constraint-ledger loop-facts))
                  (update :type-table merge (:type-table loop-facts)
                          {(ids :result) :gravity.type/string (ids :branch) :gravity.type/string
                           (ids :loop-target) :gravity.type/string caller caller-function})
                  (update :function-type-table conj caller-function)
                  (assoc-in [:recursive-proof :work-count] c7-work))
        derived (effects 'body-derive-module-nodes core facts {} 65536)
        graph (:nodes derived)
        checked (effects 'body-tc-verify core facts bindings graph {:active? true} 65536)
        self-call (get-in facts [:recursive-proof :self-call-core-node-id])]
    (is (true? (effects 'body-recursive-proof-valid? core facts)))
    (is (true? (effects 'body-ds-captures-valid? core facts bindings)))
    (is (= :accepted (:status derived)) (pr-str derived))
    (is (true? (:recur-control? derived)))
    (is (= :effects-and-escaping-error-types (:summary-domain derived)))
    (is (= :effects-with-lexical-child-source-errors (:loop-summary-domain derived)))
    (is (= [:gravity.type/string] (get-in graph [self-call :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [(ids :loop-target) :escaping-thrown-error-types])))
    (is (= #{:error/throw} (get-in graph [(ids :transfer) :transitive])))
    ;; Recur's source errors remain its lexical argument errors. Function
    ;; error propagation is a separate fixed point, not a loop error backedge.
    (is (= [] (effects 'body-error-types graph (ids :transfer))))
    (is (true? (effects 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status checked)))
    (is (= [:gravity.type/string] (get-in checked [:functions caller])))
    (is (= :accepted (:status (effects 'body-derive-module-nodes core facts {} (:work-count derived)))))
    (is (= :body-deferred-work-limit
           (:reason (effects 'body-derive-module-nodes core facts {} (dec (:work-count derived))))))
    (doseq [budget [0 1 10]]
      (is (= :body-deferred-work-limit (:reason (effects 'body-derive-module-nodes core facts {} budget)))))
    (doseq [bad [#{:error/throw} nil {:transitive #{:error/throw}}
                 {:transitive #{} :escaping-thrown-errors #{} :escaping-thrown-error-types [:gravity.type/string]}]]
      (is (= :malformed-declared-self-summary
             (:reason (effects 'body-derive-nodes-pass core facts {} 65536
                                (assoc (:recursive-proof facts) :loop-summaries {}) bad)))))
    (is (= :rejected
           (:status (effects 'body-tc-verify core facts bindings
                              (assoc-in graph [call-id :escaping-thrown-error-types] [])
                              {:active? true} 65536))))))

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
        checked (c7-invoke 'sh08-ft-loop-type-step [working table {}])]
    (is (not (contains? canonical :binding-table)))
    (is (= canonical (dissoc working :binding-table)))
    (is (= bindings (:binding-table working)))
    (is (= :accepted (:status checked)))
    (is (= (c7-invoke 'sh08-ft-loop-type-step [normalized table {}]) checked))
    (doseq [invalid [nil {} [] [(assoc (first bindings) :scope-id :forged)]]]
      (let [bad-working (c7-invoke 'sh08-ft-loop-working-core [canonical invalid])
            result (c7-invoke 'sh08-ft-loop-type-step [bad-working table {}])]
        (is (= invalid (:binding-table bad-working)))
        (is (= :rejected (:status result)))))
    (is (not (contains? canonical :binding-table)))))

(deftest loop-slot-transfer-development-kernel
  (let [[core table] (loop-data) result (c7-invoke 'sh08-ft-loop-type-step [core table {}])]
    (is (= :accepted (:status result)))
    (is (= {:again :gravity.type/bool :value :gravity.type/integer} (:bindings result)))
    (is (= [:solved :solved :solved] (mapv :status (:constraints result))))
    (is (= :gravity.type/never (:result-type (last (:constraints result)))))
    (let [bad (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/string) {}])]
      (is (= ["C7-TYPE-MISMATCH" :next-value :gravity.type/integer :gravity.type/string]
             ((juxt :rule :core-node-id :expected :actual) (first (:issues bad))))))
    (let [pending (c7-invoke 'sh08-ft-loop-type-step [core (dissoc table :value-init) {}])]
      (is (= :gravity.type/unknown (get-in pending [:bindings :value])))
      (is (= :pending (:status (last (:constraints pending))))))
    (let [never (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/never) {}])]
      (is (= :accepted (:status never)))
      (is (= [:gravity.type/bool :gravity.type/never] (:argument-types (last (:constraints never)))))
      (is (= false (:normal-transfer? (last (:constraints never)))))
      (is (= :solved (:status (last (:constraints never))))))
    (let [sequential (-> core (assoc-in [:nodes 1 :core-form] :reference)
                              (assoc-in [:nodes 1 :attributes] {:binding-id :again}))
          checked (c7-invoke 'sh08-ft-loop-type-step [sequential (assoc table :value-init :gravity.type/bool :next-value :gravity.type/bool) {}])]
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
      (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [bad table {}])))))))

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
        result (c7-invoke 'sh08-ft-loop-type-step [core {:transfer :gravity.type/never} {}])]
    (is (= :accepted (:status result)))
    (is (= :solved (:status (first (:constraints result)))))
    (is (= [] (:argument-types (first (:constraints result)))))
    (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-transfers 0 :target-id] :outer) {} {}]))))
    (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-targets 0 :parent-target-id] nil) {} {}]))))))

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

(defn- typed-throw-data []
  (let [attrs {:value-child-index 0 :evaluation-order :value-then-transfer
               :construction-order :postorder-after-value :runtime-reachability :not-asserted-by-sh07-b7
               :transfer-policy :error-transfer :result-policy :never :required-effect :error/throw
               :authenticated-sh06-artifact-id :authenticated :sh06-semantic-projection-id :projection
               :type-legality :pending-sh08 :effect-registry-legality :pending-sh09
               :effect-profile-capability-legality :pending-sh09 :profile-error-lowering-legality :pending-sh09
               :ownership-legality :pending-sh10 :safety-classification :pending-sh11}
        source (fn [id] {:form-id (keyword (str (name id) "-form")) :syntax-id id
                         :semantic-span nil :origin-chain nil :generated-origin nil})
        payload (assoc (node :payload :reference [] :function-body {:binding-id :problem}) :source (source :payload))
        transfer-node (-> (node :transfer :throw [:payload] :function-body attrs)
                          (assoc :source (source :transfer))
                          (assoc :evaluation {:kind :value-then-transfer :order [{:index 0 :core-node-id :payload}]
                                              :region :function-body :owner-function-syntax-id :function}))
        function (assoc (node :function :fn [:transfer] :namespace {}) :source (source :function))
        definition (assoc (node :definition :def [:function] :namespace {}) :source (source :definition))
        transfer (merge (dissoc attrs :value-child-index :evaluation-order)
                        {:ordinal 0 :core-node-id :transfer :form-id :transfer-form :syntax-id :transfer
                         :value-core-node-id :payload :evaluated-children [:payload] :evaluation-region :function-body
                         :evaluation-owner-function-syntax-id :function :ordered-steps [:evaluate-value :transfer-error]})
        forms [{:form-id :transfer-form :syntax-id :transfer :kind :list :value '(throw problem)
                :child-form-ids [:operator-form :payload-form]}
               {:form-id :operator-form :syntax-id :operator :kind :symbol :value 'throw :child-form-ids [] :parent-form-id :transfer-form}
               {:form-id :payload-form :syntax-id :payload :kind :symbol :value 'problem :child-form-ids [] :parent-form-id :transfer-form}]
        core {:nodes [payload transfer-node function definition] :root-core-node-ids [:definition]
              :module {:profile :meta :target :jvm :effects [:error/throw]}
              :error-transfers [transfer] :identity-preimage {:error-transfers [(assoc transfer :authenticated-sh06-artifact-id :projection)]}
              :calls [] :call-edges [] :error-handlers []}
        types {:payload :gravity.type/string :transfer :gravity.type/never}]
    [core forms types]))

(deftest direct-typed-throw-source-and-type-development-kernel
  (let [[core forms types] (typed-throw-data)
        checked (c7-invoke 'sh08-th-products [core forms types])
        invoke #(c7-invoke 'sh08-th-products [% forms types])]
    (is (= :accepted (:status checked)))
    (is (= {:function [:gravity.type/string]} (:throws checked)))
    (is (= :sh08-direct-typed-throw-v1 (-> checked :constraints first :producer-rule)))
    (is (= :equality (-> checked :constraints first :kind)))
    (is (contains? (first (:constraints checked)) :origin))
    (is (= [:error-transfers :throw-source :payload-source :payload-type :function-owner :evaluation-order]
           (-> checked :constraints first :invalidation)))
    (is (= :gravity.type/never (c7-invoke 'sh08-ft-infer-node-nominal [(second (:nodes core)) types {} {} [] {}])))
    (is (= :gravity.type/string (c7-invoke 'sh08-ft-normal-return-type [:if [:gravity.type/bool :gravity.type/never :gravity.type/string] :gravity.type/unknown])))
    (doseq [bad [(assoc core :error-transfers [])
                 (update core :error-transfers conj (first (:error-transfers core)))
                 (assoc core :error-transfers 7)
                 (assoc-in core [:error-transfers 0 :value-core-node-id] :function)
                 (assoc-in core [:error-transfers 0 :evaluation-owner-function-syntax-id] :forged)
                 (assoc-in core [:error-transfers 0 :ordered-steps] [:transfer-error :evaluate-value])
                 (assoc-in core [:nodes 1 :evaluation :order 0 :core-node-id] :function)]]
      (is (= "C7-VERIFY" (-> (invoke bad) :diagnostics first :rule))))
    (is (= ["C7-ANNOTATION" :typed-throw-payload-type-required]
           ((juxt :rule :reason) (-> (c7-invoke 'sh08-th-products [core forms (assoc types :payload :gravity.type/unknown)]) :diagnostics first))))
    (is (= "C7-VERIFY" (-> (c7-invoke 'sh08-th-products [core (assoc-in forms [1 :value] 'identity) types]) :diagnostics first :rule)))
    (let [never (c7-invoke 'sh08-th-products [core forms (assoc types :payload :gravity.type/never)])]
      (is (= {:function []} (:throws never)))
      (is (false? (-> never :constraints first :solution :normal-transfer?))))
    (is (= [{:function-id :plain :thrown-error-effects [:pending-sh09]}]
           (c7-invoke 'sh08-th-functions [[{:function-id :plain :thrown-error-effects [:pending-sh09]}] (:throws checked)])))
    (is (= "C7-ANNOTATION" (-> (c7-invoke 'sh08-tc-scope
      [(assoc core :call-edges [{:classification :nonlocal-or-nonfunction :caller-function-syntax-id :function}]) (:throws checked)])
      :diagnostics first :rule)))
    (is (false? (:active? (c7-invoke 'sh08-tc-scope
      [(assoc core :call-edges [{:classification :local-function :caller-function-syntax-id :other :callee-function-syntax-id :plain}]) (:throws checked)]))))))

(deftest direct-typed-throw-effect-replay-development-kernel
  ;; Exercise the real coverage guard with distinct authenticated occurrence
  ;; and semantic projection ids, exactly as the existing C6 producer emits.
  (let [[core _ types] (typed-throw-data)
        record {:function-syntax-id :function :function-core-node-id :function}
        core (merge core {:artifact-id :canonical-core :function-records [record] :definitions []
                          :lineage {:authenticated-sh06-artifact-id :authenticated :sh06-semantic-projection-id :projection}
                          :lexical-bindings [] :match-branch-records [] :evaluation-order [] :recursion-components []})
        core (assoc-in core [:identity-preimage :lineage]
                       {:authenticated-sh06-artifact-id :projection :sh06-semantic-projection-id :projection})
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                          :lexical-bindings :match-branch-records :evaluation-order])
                        :lineage (get-in core [:identity-preimage :lineage])
                        :nodes (effects 'body-throw-semantic-nodes core)
                        :error-transfers (effects 'body-throw-semantic-transfers core))
        core (assoc core :identity-preimage preimage)
        facts {:module (:module core) :sh07-shaped-artifact-id :canonical-core
               :function-type-table [{:function-id :function :parameters [] :parameter-binding-ids []
                                      :latent-effects [:error/throw] :capabilities []}]
               :type-table (assoc types :function :fn :definition :fn) :call-type-facts [] :local-binding-facts []
               :function-records [record] :call-edges [] :recursion-components [] :lexical-bindings []}]
    (is (not= (:nodes core) (get-in core [:identity-preimage :nodes])))
    (is (true? (effects 'body-shape-valid? core facts)))
    (doseq [bad [(assoc-in core [:identity-preimage :lineage :sh06-semantic-projection-id] :wrong-projection)
                 (assoc-in core [:nodes 1 :attributes :result-policy] :normal-value)
                 (assoc-in core [:nodes 1 :attributes :forged-field] true)
                 (-> core (assoc-in [:nodes 1 :attributes :authenticated-sh06-artifact-id] :wrong-authenticated)
                          (assoc-in [:error-transfers 0 :authenticated-sh06-artifact-id] :wrong-authenticated))]]
      (is (false? (effects 'body-shape-valid? bad facts)))))
  (let [[core forms types] (typed-throw-data)
        c7 (c7-invoke 'sh08-th-products [core forms types])
        facts {:constraint-ledger (:constraints c7) :type-table types
               :function-type-table [{:function-id :function :thrown-error-effects [:gravity.type/string]}]}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms forms}}}
        check #(effects 'body-throw-joins %1 %2 65536)]
    (is (= :accepted (:status (check request facts))))
    (is (= :accepted (:status (check (assoc-in request [:canonical-core-artifact :call-edges]
                                             [{:caller-function-syntax-id :other :callee-function-syntax-id :plain}]) facts))))
    (doseq [bad [(assoc-in request [:canonical-core-artifact :error-transfers] [])
                 (assoc-in request [:canonical-core-artifact :error-transfers] 7)
                 (update-in request [:canonical-core-artifact :error-transfers] conj (first (:error-transfers core)))
                 (assoc-in request [:b47-context :authenticated-core-request :forms 1 :value] 'identity)
                 (assoc-in request [:canonical-core-artifact :call-edges] [{:classification :nonlocal-or-nonfunction :caller-function-syntax-id :function}])]]
      (is (= :rejected (:status (check bad facts)))))
    (doseq [bad [(assoc facts :constraint-ledger [])
                 (assoc-in facts [:constraint-ledger 0 :solution :payload-type] :gravity.type/bool)
                 (assoc-in facts [:type-table :payload] :gravity.type/bool)
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [])
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [:pending-sh09])]]
      (is (= :rejected (:status (check request bad)))))
    (doseq [ledger [7 nil {} [7]]
            throw? [true false]]
      (let [request (if throw? request (assoc request :canonical-core-artifact {:nodes [] :error-transfers []}))
            rejected (check request (assoc facts :constraint-ledger ledger))]
        (is (= :rejected (:status rejected)))
        (is (= :malformed-thrown-error-constraint-ledger (:reason rejected)))))
    (let [node (second (:nodes core)) graph {:payload {:type :gravity.type/string :transitive #{} :escaping-thrown-errors #{}}}
          derived (effects 'body-control-node-record node nil graph :gravity.type/never facts {} core nil)]
      (is (= #{:error/throw} (:direct derived) (:transitive derived) (:escaping derived)))
      (is (= #{:gravity.type/string} (:direct-thrown-errors derived) (:escaping-thrown-errors derived)))
      (is (= [:evaluate-value :transfer-error] (get-in derived [:error-transfer :ordered-steps])))
      (is (= (first (:error-transfers core)) (get-in derived [:error-transfer :source-transfer])))
      (let [outer (effects 'body-control-node-record node nil (assoc-in graph [:payload :type] :gravity.type/never)
                      :gravity.type/never facts {} core nil)]
        (is (= #{} (:direct outer) (:direct-thrown-errors outer)))
        (is (false? (get-in outer [:error-transfer :normal-transfer?])))))))

(deftest direct-typed-throw-nested-and-allowance-development-kernel
  (let [[core forms types] (typed-throw-data)
        inner (second (:nodes core))
        outer (-> inner (assoc :node-id :outer-transfer :children [:transfer])
                  (assoc :source {:form-id :outer-transfer-form :syntax-id :outer-transfer
                                  :semantic-span nil :origin-chain nil :generated-origin nil})
                  (assoc-in [:evaluation :order] [{:index 0 :core-node-id :transfer}]))
        outer-record (-> (first (:error-transfers core))
                         (assoc :ordinal 1 :core-node-id :outer-transfer :form-id :outer-transfer-form
                                :syntax-id :outer-transfer :value-core-node-id :transfer :evaluated-children [:transfer]))
        core (-> core (assoc :nodes [(first (:nodes core)) inner outer
                                    (assoc (nth (:nodes core) 2) :children [:outer-transfer]) (last (:nodes core))])
                      (update :error-transfers conj outer-record))
        core (assoc-in core [:identity-preimage :error-transfers]
                       (mapv #(assoc % :authenticated-sh06-artifact-id :projection) (:error-transfers core)))
        forms (-> forms (assoc-in [0 :parent-form-id] :outer-transfer-form)
                        (conj {:form-id :outer-transfer-form :syntax-id :outer-transfer :kind :list :value '(throw (throw problem))
                               :child-form-ids [:outer-operator-form :transfer-form]}
                              {:form-id :outer-operator-form :syntax-id :outer-operator :kind :symbol :value 'throw
                               :child-form-ids [] :parent-form-id :outer-transfer-form}))
        types (assoc types :outer-transfer :gravity.type/never)
        checked (c7-invoke 'sh08-th-products [core forms types])
        facts {:constraint-ledger (:constraints checked) :type-table types
               :function-type-table [{:function-id :function :thrown-error-effects [:gravity.type/string]}]}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms forms}}}
        inner-graph (effects 'body-control-node-record inner nil {:payload {:type :gravity.type/string :transitive #{}}}
                             :gravity.type/never facts {} core nil)
        outer-graph (effects 'body-control-node-record outer nil {:transfer inner-graph}
                             :gravity.type/never facts {} core nil)]
    (is (= :accepted (:status checked)))
    (is (= 2 (count (:constraints checked))))
    (is (= {:function [:gravity.type/string]} (:throws checked)))
    (is (false? (get-in checked [:constraints 1 :solution :normal-transfer?])))
    (is (= :accepted (:status (effects 'body-throw-joins request facts 65536))))
    (is (= #{} (:direct outer-graph) (:direct-thrown-errors outer-graph)))
    (is (= #{:error/throw} (:escaping outer-graph)))
    (is (= #{:gravity.type/string} (:escaping-thrown-errors outer-graph))))
  (let [[core forms types] (typed-throw-data)
        outer (-> (nth (:nodes core) 2) (assoc :node-id :outer :children [:function])
                  (assoc-in [:source :syntax-id] :outer) (assoc-in [:source :form-id] :outer-form))
        nested (-> core (update :nodes #(vec (concat (take 3 %) [outer (assoc (last %) :children [:outer])]))))
        checked (c7-invoke 'sh08-th-products [nested forms types])]
    (is (= :accepted (:status checked)))
    (is (= {:function [:gravity.type/string]} (:throws checked)))
    (is (= [{:function-id :outer :thrown-error-effects [:pending-sh09]}
            {:function-id :function :thrown-error-effects [:gravity.type/string]}]
           (c7-invoke 'sh08-th-functions [[{:function-id :outer :thrown-error-effects [:pending-sh09]}
                                           {:function-id :function :thrown-error-effects [:pending-sh09]}] (:throws checked)]))))
  (let [core {:nodes [{:node-id :first} {:node-id :second} {:node-id :top}]
              :function-records [{:function-syntax-id :first} {:function-syntax-id :second}]}
        facts {:module {:effects []} :function-type-table [{:function-id :second :function-core-node-id :second}
                                                          {:function-id :first :function-core-node-id :first}]}
        graph {:functions {:first {:declared #{} :inferred #{:error/throw}}
                           :second {:declared #{} :inferred #{:error/throw}}}
               :nodes {:first {:direct #{}} :second {:direct #{}} :top {:direct #{:error/throw}}}}
        first-failure (effects 'body-allowance-check core facts graph 65536)
        namespace-failure (effects 'body-allowance-check core facts
                            (-> graph (assoc-in [:functions :first :declared] #{:error/throw})
                                      (assoc-in [:functions :second :declared] #{:error/throw})) 65536)]
    (is (= "C8-UNDECLARED" (:rule first-failure) (:rule namespace-failure)))
    (is (= :first (get-in first-failure [:node :node-id])))
    (is (= :first (:function first-failure)))
    (is (= {:rule "C8-UNDECLARED" :function :first :core-node-id :first}
           (select-keys (-> (effects 'body-error-result facts first-failure) :diagnostics first)
                        [:rule :function :core-node-id])))
    (is (= :top (get-in namespace-failure [:node :node-id])))
    (is (= :error/throw (:effect namespace-failure)))
    (is (= "C8-UNDECLARED" (-> (effects 'body-error-result facts namespace-failure) :diagnostics first :rule)))
    (is (= "C8-VERIFY" (-> (effects 'body-error-result facts {:reason :forged}) :diagnostics first :rule)))
    (is (= :rejected (:status (effects 'body-allowance-check core facts graph 0))))))

(defn- local-throw-kernel-data []
  (let [id (fn [n] ((body-helper 'sha) n))
        fid #(id (+ 100 %)) fnid #(id (+ 200 %)) defid #(id (+ 300 %)) binding #(id (+ 400 %)) syntax #(id (+ 500 %))
        name-syntax #(id (+ 700 %)) function-name #(symbol (str "function-" %))
        make (fn [n kind children owner attrs]
               {:node-id (id n) :core-form kind :children (mapv id children) :attributes attrs :resolved-binding-ids []
                :source {:form-id (id (+ 10000 n)) :syntax-id (id (+ 20000 n))
                         :semantic-span {:start-byte n :end-byte (inc n)} :origin-chain [] :generated-origin nil}
                :evaluation {:kind (if (= kind :fn) :function-creation :left-to-right)
                             :owner-function-syntax-id (when owner (fid owner))
                             :order (mapv (fn [index child] {:index index :core-node-id (id child)})
                                          (range) (if (= kind :fn) [] children))}})
        call (fn [n operator args caller callee]
               (assoc (make n :call (into [operator] args) caller {}) :resolved-binding-ids [(binding callee)]))
        bodies {1 19 2 4 3 12 4 6 5 10 6 15 7 16}
        nodes [(make 1 :literal [] 1 {:literal-kind :string :value "problem"}) (make 2 :throw [1] 1 {})
               (make 3 :reference [] 2 {:binding-id (binding 1)}) (call 4 3 [] 2 1)
               (make 11 :reference [] 3 {:binding-id (binding 2)}) (call 12 11 [] 3 2)
               (make 5 :literal [] 4 {:literal-kind :boolean :value true}) (make 6 :throw [5] 4 {})
               (make 7 :reference [] 5 {:binding-id (binding 4)})
               (make 8 :literal [] 5 {:literal-kind :string :value "argument"}) (make 9 :throw [8] 5 {}) (call 10 7 [9] 5 4)
               (make 13 :literal [] 6 {:literal-kind :string :value "isolated"})
               (make 14 :throw [13] 6 {}) (make 15 :loop [14] 6 {})
               (make 16 :literal [] 7 {:literal-kind :string :value "pure"})
               (make 17 :reference [] 1 {:binding-id (binding 7)}) (call 18 17 [] 1 7) (make 19 :do [18 2] 1 {})]
        records (mapv (fn [f] {:ordinal (dec f) :function-syntax-id (fid f) :function-core-node-id (fnid f)
                               :definition-core-node-id (defid f) :definition-binding-id (binding f)
                               :body-core-node-id (id (bodies f)) :fixed-arity (if (= f 4) 1 0) :parameter-binding-ids []}) (range 1 8))
        definitions (mapv (fn [f] {:name (function-name f) :binding-id (binding f) :core-node-id (defid f) :syntax-id (syntax f)
                                   :value-node-id (fnid f)}) (range 1 8))
        functions (into {} (map (fn [f] [(fid f) {:artifact :gravity/c7-function-type :function-id (fid f)
                            :body-core-node-id (id (bodies f)) :function-core-node-id (fnid f)
                            :parameters (if (= f 4) [{:binding-id (id 900) :type :gravity.type/bool}] [])
                            :return :gravity.type/string :fixed-arity (if (= f 4) 1 0) :latent-effects [:error/throw]
                            :capabilities [] :thrown-error-effects [:pending-sh09]}]) (range 1 8)))
        specs [[4 3 [] 2 1] [12 11 [] 3 2] [10 7 [9] 5 4] [18 17 [] 1 7]]
        calls (mapv (fn [[n operator args _ callee]]
                       {:core-node-id (id n) :operator-node-id (id operator) :operator-binding-id (binding callee)
                        :argument-node-ids (mapv id args) :ordered-evaluation-node-ids (mapv id (into [operator] args))
                        :evaluation-order :operator-then-arguments :result-policy :call-result}) specs)
        edges (mapv (fn [ordinal [n operator args caller callee]]
                       {:ordinal ordinal :call-core-node-id (id n) :caller-function-syntax-id (fid caller)
                        :caller-function-core-node-id (fnid caller) :callee-binding-id (binding callee)
                        :callee-definition-syntax-id (name-syntax callee) :callee-function-syntax-id (fid callee)
                        :callee-function-core-node-id (fnid callee) :argument-core-node-ids (mapv id args)
                        :ordered-evaluation-node-ids (mapv id (into [operator] args)) :evaluation-order :operator-then-arguments
                        :classification :local-function}) (range) specs)
        nodes (into nodes (mapcat (fn [f]
                         [(-> (make (+ 200 f) :fn [(bodies f)] nil {}) (assoc-in [:source :syntax-id] (fid f)))
                          (-> (make (+ 300 f) :def [(+ 200 f)] nil {:name (function-name f) :name-syntax-id (name-syntax f)})
                              (assoc :resolved-binding-ids [(binding f)]) (assoc-in [:source :syntax-id] (syntax f)))]) (range 1 8)))
        types (into {} (map (fn [node] [(:node-id node) :gravity.type/string]) nodes))
        types (merge types {(id 2) :gravity.type/never (id 5) :gravity.type/bool (id 6) :gravity.type/never
                            (id 9) :gravity.type/never (id 10) :gravity.type/never (id 14) :gravity.type/never})
        core {:nodes nodes :module {:profile :meta :target :jvm :effects [:error/throw]}
              :root-core-node-ids (mapv defid (range 1 8)) :function-records records :definitions definitions
              :calls calls :call-edges edges :recursion-components []
              :error-transfers (mapv (fn [[n value]] {:core-node-id (id n) :value-core-node-id (id value)}) [[2 1] [6 5] [9 8] [14 13]])}
        direct {:status :accepted :throws {(fid 1) [:gravity.type/string] (fid 4) [:gravity.type/bool]
                                          (fid 5) [:gravity.type/string] (fid 6) [:gravity.type/string]}
                :constraints [] :diagnostics [] :work-count 0}
        bindings (mapv (fn [f] {:name (function-name f) :binding-id (binding f) :definition-syntax-id (name-syntax f)}) (range 1 8))]
    {:core core :types types :functions functions :direct direct :bindings bindings :id id :fid fid}))

(deftest loop-called-source-error-development-kernel
  ;; Normalized source graph DATA, not an authenticated input. The existing
  ;; call joins are unchanged; loop/recur add only lexical child dependencies.
  (let [{:keys [core types functions direct bindings id fid]} (local-throw-kernel-data)
        backedge (node :backedge :recur [(id 2)] :loop-body {:target-kind :loop})
        core (update core :nodes #(mapv (fn [n] (if (= (id 19) (:node-id n))
                                                (assoc n :core-form :loop :children [(id 18) :backedge]) n)) (conj % backedge)))
        types (assoc types :backedge :gravity.type/never (id 19) :gravity.type/never)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct ])]
    (is (= :accepted (:status result)))
    (doseq [f [1 2 3]] (is (= [:gravity.type/string] (get-in result [:throws (fid f)]))))
    (is (= 4 (count (:constraints result))))
    (let [row (first (filter #(= (id 4) (get-in % [:lhs :core-node-id])) (:constraints result)))]
      (is (= [:gravity.type/string] (get-in row [:dependencies :callee-error-types])))
      (is (= [:gravity.type/string] (get-in row [:solution :thrown-error-effects]))))
    (is (= "C7-GENERIC" (get-in (c7-invoke 'sh08-tc-products [core types functions bindings direct ]) [:diagnostics 0 :rule])))
    (is (= :unsupported-throwing-body-operation
           (get-in (c7-invoke 'sh08-tc-products
                     [(update core :nodes #(mapv (fn [n] (if (= :backedge (:node-id n)) (assoc n :core-form :set!) n)) %))
                      types functions bindings direct ]) [:diagnostics 0 :reason])))))

(deftest local-throw-call-closure-development-kernel
  ;; Normalized DATA exercises the actual Gravity algorithms, not authentication.
  (let [{:keys [core types functions direct bindings id fid]} (local-throw-kernel-data)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct ])]
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (doseq [f [1 2 3 5 6]] (is (= [:gravity.type/string] (get-in result [:throws (fid f)]))))
    (is (= [:gravity.type/bool] (get-in result [:throws (fid 4)])))
    (is (= [] (get-in result [:throws (fid 7)])))
    (is (= 4 (count (:constraints result))))
    (let [row (first (filter #(= (id 10) (get-in % [:lhs :core-node-id])) (:constraints result)))]
      (is (= {:core-node-id (id 10) :projection :thrown-error-effects} (:lhs row)))
      (is (false? (get-in row [:dependencies :normal-invocation?])))
      (is (= [] (get-in row [:dependencies :callee-error-types])))
      (is (= :gravity.type/never (get-in row [:solution :normal-result-type]))))
    (is (= (c7-invoke 'sh08-tc-products [core types functions bindings (assoc direct :throws {}) ])
           (assoc direct :throws {})))
    (is (= "C7-GENERIC" (-> (c7-invoke 'sh08-tc-products [core types functions bindings direct ]) :diagnostics first :rule)))
    (let [cost (:work-count result)]
      (is (= :accepted (:status (c7-invoke 'sh08-tc-products [core types functions bindings direct ])))))
    (let [direct (assoc direct :work-count 17)
          baseline (:work-count result)
          accepted (c7-invoke 'sh08-tc-products [core types functions bindings direct ])]
      (is (= :accepted (:status accepted)))
      (is (= (+ 17 baseline) (:work-count accepted)))
      (is (= "C7-GENERIC" (-> (c7-invoke 'sh08-tc-products [core types functions bindings direct ]) :diagnostics first :rule))))
    (let [isolated (assoc direct :throws {(fid 6) [:gravity.type/string]} :work-count 17)
          no-touch (c7-invoke 'sh08-tc-products [core types functions bindings isolated ])]
      (is (= (:throws isolated) (:throws no-touch)))
      (is (= (+ 17 (count (:call-edges core))) (:work-count no-touch))))
    (let [scoped (c7-invoke 'sh08-tc-scope [core (:throws direct)])]
      (is (set? (:scope scoped)))
      (is (true? (:active? scoped))))
    (is (= "C7-VERIFY" (-> (c7-invoke 'sh08-tc-products [(assoc-in core [:call-edges 0 :callee-binding-id] :forged) types functions bindings direct ]) :diagnostics first :rule)))
    (let [definition (first (:definitions core)) binding (first bindings)
          def-index (first (keep-indexed #(when (= (:core-node-id definition) (:node-id %2)) %1) (:nodes core)))]
      (is (not= (:definition-syntax-id binding) (:syntax-id definition)))
      (doseq [[bad-core bad-bindings]
              [[(assoc-in core [:call-edges 0 :callee-definition-syntax-id] (:syntax-id definition)) bindings]
               [core (assoc-in bindings [0 :definition-syntax-id] (:syntax-id definition))]
               [(assoc-in core [:nodes def-index :attributes :name-syntax-id] (:syntax-id definition)) bindings]
               [(assoc-in core [:definitions 0 :syntax-id] (:definition-syntax-id binding)) bindings]
               [(assoc-in core [:function-records 0 :body-core-node-id] (id 6)) bindings]]]
        (let [bad (c7-invoke 'sh08-tc-products [bad-core types functions bad-bindings direct ])]
          (is (= "C7-VERIFY" (-> bad :diagnostics first :rule)))
          (is (= (id 4) (get-in bad [:diagnostics 0 :node :node-id]))))))
    (is (= "C7-ANNOTATION" (-> (c7-invoke 'sh08-tc-products [(assoc core :recursion-components [{:function-syntax-ids [(fid 1)]}]) types functions bindings direct ]) :diagnostics first :rule)))))

(deftest local-throw-call-existing-type-guards-development-kernel
  (let [{:keys [core types functions id]} (local-throw-kernel-data)
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        call (last (butlast (:calls core))) edge (last (butlast (:call-edges core)))
        invoke (fn [call edge] (c7-invoke 'sh08-ft-call-constraints-with-context
                                [[call] [edge] nodes types {(id 900) :gravity.type/bool} functions {}]))]
    (is (= [] (:diagnostics (invoke call edge))))
    (is (= :gravity.type/never
           (c7-invoke 'sh08-ft-infer-node-with-context [(get nodes (id 10)) types {} functions (:call-edges core) {}])))
    (is (= :call-arity-mismatch
           (-> (invoke (assoc call :argument-node-ids []) (assoc edge :argument-core-node-ids [])) :diagnostics first :reason)))
    (is (= :call-argument-type-mismatch
           (-> (invoke (assoc call :argument-node-ids [(id 1)]) (assoc edge :argument-core-node-ids [(id 1)])) :diagnostics first :reason)))))

(defn- typed-handler-kernel-data []
  (let [[core forms types] (typed-throw-data)
        source (fn [id] {:form-id (keyword (str (name id) "-form")) :syntax-id id
                         :semantic-span nil :origin-chain nil :generated-origin nil})
        clause {:clause-ordinal 0 :handler-child-index 1 :catch-clause-form-id :catch-form :catch-clause-syntax-id :catch
                :error-type-form-id :type-form :error-type-syntax-id :type :error-type-binding-id :string-type
                :catch-binding-form-id :binder-form :catch-binding-syntax-id :binder :catch-binding-id :caught :catch-binding-scope-id :catch-scope}
        attrs {:protected-child-index 0 :handler-count 1 :handler-child-indexes [1] :handler-clauses [clause]
               :evaluation-order :protected-then-ordered-typed-handler-candidates :runtime-reachability :not-asserted-by-sh07-b9
               :selection-policy :source-ordered-typed-handler-candidates :result-policy :protected-or-selected-handler-last
               :authenticated-sh06-artifact-id :authenticated :sh06-semantic-projection-id :projection
               :type-coverage-legality :pending-sh08 :result-type-join-legality :pending-sh08
               :effect-registry-legality :pending-sh09 :effect-profile-capability-legality :pending-sh09
               :profile-error-lowering-legality :pending-sh09 :ownership-legality :pending-sh10 :safety-classification :pending-sh11}
        handler (assoc (node :handler :reference [] :catch-scope {:binding-id :caught}) :source (source :handler))
        try-node (-> (node :try :try [:transfer :handler] :function-body attrs) (assoc :source (source :try))
                     (assoc :resolved-binding-ids [:string-type :caught])
                     (assoc :evaluation {:kind :protected-then-ordered-typed-handler-candidates
                                         :order [{:index 0 :core-node-id :transfer}] :region :function-body :owner-function-syntax-id :function}))
        record (merge (dissoc attrs :protected-child-index :handler-count :handler-child-indexes :handler-clauses :evaluation-order)
                      (dissoc clause :clause-ordinal)
                      {:ordinal 0 :clause-ordinal 0 :clause-count 1 :core-node-id :try :form-id :try-form :syntax-id :try
                       :protected-core-node-id :transfer :handler-core-node-id :handler :catch-binding-use-syntax-ids [:handler]
                       :candidate-error-transfers [{:ordinal 0 :core-node-id :transfer}]
                       :evaluation-region :function-body :evaluation-owner-function-syntax-id :function
                       :ordered-steps [:evaluate-protected :conditionally-select-source-ordered-typed-handler]
                       :construction-order :protected-then-handlers-source-order-then-try
                       :runtime-evaluation :protected-then-conditional-handler-chain})
        core (-> core (assoc :nodes [(first (:nodes core)) (second (:nodes core)) handler try-node
                                    (assoc (nth (:nodes core) 2) :children [:try]) (last (:nodes core))])
                      (assoc :error-handlers [record]
                             :function-records [{:function-syntax-id :function :function-core-node-id :function :body-core-node-id :try}])
                      (assoc-in [:identity-preimage :error-handlers] [(assoc record :authenticated-sh06-artifact-id :projection)]))
        forms (-> forms (assoc-in [0 :parent-form-id] :try-form)
                  (into [{:form-id :try-form :syntax-id :try :kind :list :value '(try (throw problem) (catch String e e))
                          :child-form-ids [:try-op-form :transfer-form :catch-form]}
                         {:form-id :try-op-form :syntax-id :try-op :kind :symbol :value 'try :child-form-ids [] :parent-form-id :try-form}
                         {:form-id :catch-form :syntax-id :catch :kind :list :value '(catch String e e)
                          :child-form-ids [:catch-op-form :type-form :binder-form :handler-form] :parent-form-id :try-form}
                         {:form-id :catch-op-form :syntax-id :catch-op :kind :symbol :value 'catch :child-form-ids [] :parent-form-id :catch-form}
                         {:form-id :type-form :syntax-id :type :kind :symbol :value 'String :child-form-ids [] :parent-form-id :catch-form}
                         {:form-id :binder-form :syntax-id :binder :kind :symbol :value 'e :child-form-ids [] :parent-form-id :catch-form :scope-id :catch-scope}
                         {:form-id :handler-form :syntax-id :handler :kind :symbol :value 'e :child-form-ids [] :parent-form-id :catch-form :scope-id :catch-scope}]))
        bindings [{:binding-id :string-type :name 'String :binding-class :core :kind :type :namespace 'gravity.core}
                  {:binding-id :caught :name 'e :binding-class :lexical :kind :local :definition-syntax-id :binder :scope-id :catch-scope}]]
    {:core core :forms forms :bindings bindings :types (assoc types :handler :gravity.type/string :try :gravity.type/string)}))

(defn- loop-handler-kernel-data []
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        loop-node (-> (node :protected-loop :loop [:transfer] :function-body
                           {:target-id :protected-loop :binding-count 0 :body-count 1 :initializer-child-indexes []
                            :body-child-indexes [0] :result-policy {:kind :last-body :child-index 0}})
                      (assoc-in [:evaluation :kind] :initializers-then-body-left-to-right)
                      (assoc :source {:form-id :protected-loop-form :syntax-id :protected-loop
                                      :semantic-span nil :origin-chain nil :generated-origin nil}))
        target (fn [id kind parent] {:target-id id :target-kind kind :owner-core-node-id id
                                     :owner-form-id (keyword (str (name id) "-form")) :owner-syntax-id id
                                     :arity 0 :binding-ids [] :body-scope-id :function-body :parent-target-id parent
                                     :type-compatibility :pending-sh08})
        record (assoc (first (:error-handlers core)) :protected-core-node-id :protected-loop)
        core (-> core
                 (update :nodes #(vec (mapcat (fn [n] (cond
                           (= :try (:node-id n)) [loop-node (-> n (assoc-in [:children 0] :protected-loop)
                                                              (assoc-in [:evaluation :order 0 :core-node-id] :protected-loop))]
                           (= :function (:node-id n)) [(assoc-in n [:attributes :recur-target-id] :function)]
                           :else [n])) %)))
                 (assoc :loop-bindings [] :recur-transfers [] :binding-table []
                        :recur-targets [(target :protected-loop :loop :function) (target :function :function nil)]
                        :error-handlers [record])
                 (assoc-in [:identity-preimage :error-handlers] [(assoc record :authenticated-sh06-artifact-id :projection)]))
        forms (into (mapv (fn [f] (cond
                         (= :transfer-form (:form-id f)) (assoc f :parent-form-id :protected-loop-form)
                         (= :try-form (:form-id f)) (assoc f :value '(try (loop [] (throw problem)) (catch String e e))
                                                                        :child-form-ids [:try-op-form :protected-loop-form :catch-form])
                         :else f)) forms)
                [{:form-id :protected-loop-form :syntax-id :protected-loop :kind :list :value '(loop [] (throw problem))
                  :child-form-ids [:loop-op-form :loop-bindings-form :transfer-form] :parent-form-id :try-form}
                 {:form-id :loop-op-form :syntax-id :loop-op :kind :symbol :value 'loop :child-form-ids [] :parent-form-id :protected-loop-form}
                 {:form-id :loop-bindings-form :syntax-id :loop-bindings :kind :vector :value [] :child-form-ids [] :parent-form-id :protected-loop-form}])]
    {:core core :forms forms :bindings bindings :types (assoc types :protected-loop :gravity.type/never)}))

(deftest loop-handler-source-error-development-kernel
  ;; No calls: a handler owner alone activates the actual source-error graph.
  (let [{:keys [core forms bindings types]} (loop-handler-kernel-data)
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {}])
        source (c7-invoke 'sh08-eh-source-products [core forms bindings])
        independent (effects 'body-eh-source-products core forms bindings 65536)
        direct (c7-invoke 'sh08-th-products [core forms types])
        products (c7-invoke 'sh08-tc-products [core types {} bindings direct ])
        owners (c7-invoke 'sh08-th-owners [core])
        derived (c7-invoke 'sh08-tc-graph [core types (:owners owners) #{:function} (:groups source)])
        rows (filterv #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraints products))
        facts {:type-table types :constraint-ledger rows :atomic-typed-handlers (:groups independent)}
        graph (reduce (fn [g n] (assoc g (:node-id n)
                                 (effects 'body-control-node-record n nil g (get types (:node-id n)) facts {} core nil))) {} (:nodes core))
        request {:b47-context {:authenticated-core-request {:forms forms :binding-table bindings
                    :resolution-table [{:reference-syntax-id :type :position :type :symbol 'String :binding-id :string-type}
                                       {:reference-syntax-id :handler :position :value :symbol 'e :binding-id :caught}]}}}
        verify #(effects 'body-eh-verify request core %1 (:groups independent) %2 65536)]
    (is (= :accepted (:status checked)))
    (is (= :accepted (:status source) (:status independent) (:status direct) (:status products) (:status derived))
        (pr-str {:source source :independent independent}))
    (is (= [] (:call-edges core)))
    (is (= [:gravity.type/string] (get-in derived [:nodes :protected-loop])))
    (is (= [] (get-in derived [:nodes :try]) (get-in products [:throws :function])))
    (is (= 1 (count rows)))
    (is (= [:gravity.type/string] (get-in rows [0 :dependencies :child-source-error-types 0])))
    (is (= [] (get-in rows [0 :solution :thrown-error-effects])))
    (is (= :gravity.type/string (get-in rows [0 :solution :normal-result-type])))
    (is (= :accepted (:status (verify facts graph))))
    (is (= #{:error/throw} (get-in graph [:try :escaping])))
    (doseq [bad [(assoc facts :constraint-ledger [])
                 (assoc-in facts [:constraint-ledger 0 :dependencies :child-source-error-types 0] [])
                 (assoc-in facts [:constraint-ledger 0 :solution :thrown-error-effects] [:gravity.type/bool])
                 (assoc-in facts [:constraint-ledger 0 :dependencies :source-node :source :syntax-id] :forged)]]
      (is (= :rejected (:status (verify bad graph)))))
    (is (= :rejected (:status (verify facts (assoc-in graph [:protected-loop :escaping-thrown-error-types] [])))))
    (let [clauses [{:record :first :error-type :gravity.type/string :handler-core-node-id :rethrow}
                   {:record :sibling :error-type :gravity.type/bool :handler-core-node-id :sibling}]
          g (assoc (:nodes derived) :rethrow [:gravity.type/bool] :sibling [])
          partition (c7-invoke 'sh08-eh-partition [(get g :protected-loop) clauses g])]
      (is (= [:gravity.type/bool] (:residual partition)))
      (is (= [[:gravity.type/string] []] (mapv :matched-error-types (:handled partition)))))
    (is (= "C7-GENERIC" (get-in (c7-invoke 'sh08-tc-products [core types {} bindings direct ]) [:diagnostics 0 :rule])))))

(deftest atomic-typed-handler-source-development-kernel
  (let [{:keys [core forms bindings]} (typed-handler-kernel-data)
        check #(c7-invoke 'sh08-eh-source-products [%1 %2 %3])
        result (check core forms bindings)]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= :accepted (:status (effects 'body-eh-source-products core forms bindings 65536))))
    (is (= {:caught :gravity.type/string} (:seeds result)))
    (is (= [:gravity.type/string] (mapv :error-type (get-in result [:groups :try]))))
    (doseq [[bad-core bad-forms bad-bindings]
            [[(assoc-in core [:error-handlers 0 :candidate-error-transfers] []) forms bindings]
             [(assoc-in core [:error-handlers 0 :clause-ordinal] 1) forms bindings]
             [(assoc-in core [:error-handlers 0 :catch-binding-id] :other) forms bindings]
             [(assoc-in core [:nodes 3 :attributes :handler-child-indexes] [0]) forms bindings]
             [(assoc core :error-handlers 7) forms bindings]
             [(assoc-in core [:nodes 3 :children] 7) forms bindings]
             [(assoc-in core [:nodes 3 :attributes :handler-clauses] 7) forms bindings]
             [(assoc-in core [:nodes 3 :resolved-binding-ids] [:caught :string-type]) forms bindings]
             [(assoc-in core [:nodes 3 :evaluation :owner-function-syntax-id] :other) forms bindings]
             [(assoc-in core [:nodes 0 :attributes :binding-id] :caught) forms bindings]
             [core forms (assoc-in bindings [0 :kind] :function)]
             [core forms (assoc-in bindings [1 :scope-id] :sibling)]
             [core (assoc-in forms [7 :value] 'Bool) bindings]]]
      (is (= "C7-VERIFY" (get-in (check bad-core bad-forms bad-bindings) [:diagnostics 0 :rule])))
      (is (= :rejected (:status (effects 'body-eh-source-products bad-core bad-forms bad-bindings 65536)))))
    (let [plain (assoc core :nodes [(first (:nodes core))] :error-handlers [])
          result (check plain [] [])]
      (is (= {:status :accepted :groups {} :seeds {} :diagnostics []} result)))))

(deftest atomic-typed-handler-source-scope-integration
  (doseq [name ["atomic-catch-unresolved-type" "atomic-catch-protected-scope" "atomic-catch-sibling-scope" "atomic-catch-outside-scope"]
          extension [".gravity" ".qst"]]
    (let [failure (try (bootstrap/compiler-c5-resolution-file-artifact (path (fixture "rejected" name extension)))
                       nil (catch clojure.lang.ExceptionInfo error (ex-data error)))]
      (is (= "C5-UNRESOLVED" (:id failure)) (pr-str failure)))))

(deftest atomic-typed-handler-downstream-boundary-development-kernel
  (let [emitter-path (path "bootstrap/gravity/p15_s23/emitter.gravity")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter emitter-path (slurp emitter-path))
        lowered (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :typed-catch-downstream-boundary} compiled 'p15-s23-compile-expr
                  [emitter {:module 'example.try :profile :meta :target :jvm} '#{problem} '(try (throw problem) (catch String e e))])
        runtime-path (path "bootstrap/gravity/src/gravity/runtime.gravity")
        runtime-text (slurp runtime-path)
        runtime-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter runtime-path runtime-text)
        result (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :typed-catch-downstream-boundary} runtime-plan
                 'p15-s23-runtime-evaluate-closed-instruction
                 [{:op :try :body [{:op :println :args [{:op :literal :value "later"}] :effect :io/write :capability :io/stdout}]} {} ""])]
    (is (= :rejected (:status lowered)))
    (is (= :unauthenticated-b51-external-link (get-in lowered [:facts :reason])))
    (is (= (str "sha256:" (bootstrap/sha256-hex runtime-text)) (get-in runtime-plan [:source :sha256])))
    (is (= 22 (count (:functions runtime-plan))))
    (is (= ["" :rejected] (subvec result 1)))
    (is (= "P15S23X002" (get-in result [0 :diagnostic])))))

(deftest atomic-typed-handler-products-development-kernel
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        direct {:status :accepted :throws {:function [:gravity.type/string]} :constraints [] :diagnostics [] :work-count 0}
        products (c7-invoke 'sh08-tc-products [core types {} bindings direct ])
        source (c7-invoke 'sh08-eh-source-products [core forms bindings])
        initial (c7-invoke 'sh08-eh-seed-bindings [{:problem :gravity.type/string} (:seeds source)])
        inferred (c7-invoke 'sh08-ft-infer-pass-with-context [(:nodes core) [] [] [] [] {} initial {}])]
    (is (= :accepted (:status products)) (pr-str products))
    (is (= [] (get-in products [:throws :function])))
    (is (= 1 (count (filter #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraints products)))))
    (is (= :gravity.type/string (get-in inferred [:type-table :handler]) (get-in inferred [:type-table :try])))
    (is (= :gravity.type/never (get-in inferred [:type-table :transfer])))
    (let [core (-> core
                   (update :nodes #(mapv (fn [node] (if (= :try (:node-id node)) (assoc node :children [:payload :handler]) node))
                                        (remove (fn [node] (= :transfer (:node-id node))) %)))
                   (assoc :error-transfers [])
                   (assoc-in [:error-handlers 0 :protected-core-node-id] :payload)
                   (assoc-in [:error-handlers 0 :candidate-error-transfers] []))
          products (c7-invoke 'sh08-tc-products [core (dissoc types :transfer) {} bindings (assoc direct :throws {}) ])]
      (is (= :accepted (:status products)) (pr-str products))
      (is (= [] (get-in products [:throws :function])))
      (is (= 1 (count (filter #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraints products))))))))

(deftest atomic-typed-handler-diagnostic-provenance-development-kernel
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        source (c7-invoke 'sh08-eh-source-products [core forms bindings])
        span {:source "handler-source.gravity" :byte-start 21 :byte-end 99}
        origins [{:kind :macro-expansion :syntax-id :generated-parent}]
        generated {:kind :macro-expansion :from-syntax-id :source-try}
        context {:scope-id :original-try-scope :region :function-body}
        original (-> (nth (:nodes core) 3)
                     (assoc :binding-context context)
                     (assoc-in [:source :semantic-span] span)
                     (assoc-in [:source :origin-chain] origins)
                     (assoc-in [:source :generated-origin] generated))
        nodes (assoc (into {} (map (juxt :node-id identity) (:nodes core))) :try original)
        checked (c7-invoke 'sh08-eh-type-checks [(:groups source) (assoc types :transfer :gravity.type/string :handler :gravity.type/bool) nodes])
        diagnostic (first (:diagnostics checked))
        rendered (first (c7-invoke 'sh08-ft-render-inference-diagnostics [(:diagnostics checked) nodes (:module core)]))]
    (is (= original (:node diagnostic)))
    (is (= context (get-in diagnostic [:node :binding-context])))
    (is (= ["C7-TYPE-MISMATCH" :try-normal-result-type-mismatch :compatible-protected-and-handler-normal-results
            [:gravity.type/string :gravity.type/bool]] ((juxt :rule :reason :expected :actual) diagnostic)))
    (is (= :try (:syntax-id rendered)))
    (is (= span (:source-span rendered)))
    (is (= origins (:origin-chain rendered)))
    (is (= generated (:generated-origin rendered)))
    (is (= :string-type (:binding-id rendered)))
    (is (= (select-keys rendered [:syntax-id :source-span :origin-chain :generated-origin :binding-id :reason :rule :expected-type :actual-type])
           (select-keys (:diagnostic-id-request rendered) [:syntax-id :source-span :origin-chain :generated-origin :binding-id :reason :rule :expected-type :actual-type])))
    (is (= {:diagnostics []} (c7-invoke 'sh08-eh-type-checks [(:groups source) types nodes])))))

(deftest atomic-typed-handler-body-development-kernel
  (let [{:keys [core forms bindings types]} (typed-handler-kernel-data)
        source (c7-invoke 'sh08-eh-source-products [core forms bindings])
        independent (effects 'body-eh-source-products core forms bindings 65536)
        owners (c7-invoke 'sh08-th-owners [core])
        derived (c7-invoke 'sh08-tc-graph [core types (:owners owners) #{:function} (:groups source)])
        rows (c7-invoke 'sh08-eh-constraints [core (:groups source) types (:nodes derived) []])
        facts {:type-table types :constraint-ledger rows :atomic-typed-handlers (:groups independent)}
        graph (reduce (fn [graph node]
                        (assoc graph (:node-id node)
                               (effects 'body-control-node-record node nil graph (get types (:node-id node)) facts {} core nil))) {} (:nodes core))
        request {:b47-context {:authenticated-core-request {:forms forms :binding-table bindings
                    :resolution-table [{:reference-syntax-id :type :position :type :symbol 'String :binding-id :string-type}
                                       {:reference-syntax-id :handler :position :value :symbol 'e :binding-id :caught}]}}}
        verify #(effects 'body-eh-verify request core %1 (:groups independent) %2 65536)]
    (is (= :accepted (:status derived)))
    (is (= [] (get-in derived [:nodes :try])))
    (is (= :accepted (:status (verify facts graph))) (pr-str (verify facts graph)))
    (is (= #{:error/throw} (get-in graph [:try :transitive]) (get-in graph [:try :escaping])))
    (is (= [] (get-in graph [:try :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [:try :covered-source-errors 0 :matched-error-types])))
    (doseq [mutate [(fn [f] (assoc f :constraint-ledger []))
                    (fn [f] (assoc-in f [:constraint-ledger 0 :solution :thrown-error-effects] [:gravity.type/bool]))
                    (fn [f] (assoc-in f [:type-table :handler] :gravity.type/bool))
                    (fn [f] (assoc-in f [:type-table :try] :gravity.type/never))]]
      (is (= :rejected (:status (verify (mutate facts) graph)))))
    (is (= :rejected (:status (verify facts (assoc-in graph [:try :escaping-thrown-error-types] [:gravity.type/bool])))))
    (is (= :rejected (:status (effects 'body-eh-verify (assoc-in request [:b47-context :authenticated-core-request :resolution-table 0 :position] :value)
                              core facts (:groups independent) graph 65536))))
    (is (= :gravity.type/string (c7-invoke 'sh08-ft-normal-return-type [:try [:gravity.type/never :gravity.type/string] :gravity.type/unknown])))
    (is (= "C7-TYPE-MISMATCH" (get-in (c7-invoke 'sh08-eh-type-checks [(:groups source) (assoc types :transfer :gravity.type/string :handler :gravity.type/bool)
                                            (into {} (map (juxt :node-id identity) (:nodes core)))]) [:diagnostics 0 :rule])))))

(deftest atomic-typed-handler-partition-development-kernel
  (let [string :gravity.type/string bool :gravity.type/bool
        clauses [{:record :first :error-type string :handler-core-node-id :handler-a}
                 {:record :second :error-type string :handler-core-node-id :handler-b}
                 {:record :third :error-type bool :handler-core-node-id :handler-c}]
        errors {:handler-a [string] :handler-b [bool] :handler-c []}
        graph (into {} (map (fn [[id errors]] [id {:escaping-thrown-error-types errors}]) errors))
        check (fn [protected clauses]
                (let [typed (c7-invoke 'sh08-eh-partition [protected clauses errors])
                      effected (effects 'body-eh-partition protected clauses graph)]
                  (is (= typed effected)) typed))]
    (is (= [] (:residual (check [] clauses))))
    (let [result (check [string] clauses)]
      (is (= [string] (:residual result)))
      (is (= [[string] [] []] (mapv :matched-error-types (:handled result))))
      (is (= [[string] [] []] (mapv :handler-error-types (:handled result)))))
    (is (= [bool string] (:residual (check [string bool] (subvec clauses 0 2)))))
    (is (= [string] (:residual (check [string bool] clauses))))
    (is (= [] (:residual (check [string] [(assoc (first clauses) :handler-core-node-id :handler-c)])))))
  (let [core {:nodes [] :error-handlers []}]
    (is (= :accepted (:status (c7-invoke 'sh08-eh-source-products [core [] []]))))
    (is (= "C7-VERIFY" (get-in (c7-invoke 'sh08-eh-source-products [(assoc core :error-handlers [{}]) [] []]) [:diagnostics 0 :rule])))))

(deftest local-throw-final-signature-status-development-kernel
  (let [function {:parameters [{:binding-id :unused :type :gravity.type/unknown}]
                  :return :gravity.type/string :status :inferred}]
    (is (= :unresolved (:status (first (c7-invoke 'sh08-ft-final-functions [[function]])))))
    (is (= :inferred (:status (first (c7-invoke 'sh08-ft-final-functions
                                       [[(assoc-in function [:parameters 0 :type] :gravity.type/bool)]])))))))

(deftest local-throw-source-body-index-parity-development-kernel
  (let [{:keys [core types functions direct bindings]} (local-throw-kernel-data)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct ])
        facts {:function-type-table (c7-invoke 'sh08-th-functions [(vec (vals functions)) (:throws result)])
               :type-table types :constraint-ledger (:constraints result)}
        [ho-core ho-facts _] ((body-helper 'callable-kernel-data))]
    ;; Compare the old unique-vector lookup with the indexed input, then rebuild
    ;; every record using the old lookup against the complete derived graph.
    (doseq [[core facts] [[core facts] [ho-core ho-facts]]]
      (let [table (:function-type-table facts) index (effects 'body-index table :function-id)
            edges (effects 'body-index (:call-edges core) :call-core-node-id)
            derived (effects 'body-derive-nodes-with-foreign core facts {} 65536) graph (:nodes derived)
            old-source #(get (effects 'body-by table :function-id (:callee-function-syntax-id %)) :body-core-node-id)
            indexed-source #(get-in index [(:callee-function-syntax-id %) :body-core-node-id])
            record (fn [node edge source]
                     (let [target (when (and (= :call (:core-form node))
                                             (effects 'body-call-invokes? node (:type-table facts)))
                                    (:body-core-node-id (effects 'body-selected-function edge facts index)))]
                       (effects 'body-control-node-record node target graph (get (:type-table facts) (:node-id node))
                                facts edge core source)))
            old-graph (into {} (map (fn [node] (let [id (:node-id node) edge (get edges id)]
                                               [id (record node edge (old-source edge))])) (:nodes core)))]
        (is (= :accepted (:status derived)))
        (is (= graph old-graph))
        (is (true? (effects 'body-graph-equations-valid? core facts {} old-graph)))
        (doseq [node (:nodes core)
                edge [(get edges (:node-id node)) {} {:callee-function-syntax-id :missing} nil]]
          (is (= (old-source edge) (indexed-source edge)))
          (is (= (record node edge (old-source edge)) (record node edge (indexed-source edge)))))))
    (is (nil? (effects 'body-index (conj (:function-type-table facts) (first (:function-type-table facts))) :function-id))))
  (let [module {:namespace 'example.body :profile :meta :target :jvm :effects [] :capabilities []}
        sha (body-helper 'sha) make (body-helper 'node)
        nodes [(make 1 :literal []) (make 2 :fn [1]) (make 3 :def [2])]
        core {:module module :nodes nodes :root-core-node-ids [(sha 3)] :definitions []
              :function-records [{:function-syntax-id (sha 12) :function-core-node-id (sha 2)}]
              :call-edges [] :calls [] :recursion-components [] :lexical-bindings [] :match-branch-records []
              :evaluation-order [{:core-node-id (sha 3) :index 0}]}
        core (assoc core :identity-preimage (select-keys core [:nodes :root-core-node-ids :definitions :function-records
                                                              :call-edges :calls :evaluation-order :lexical-bindings :match-branch-records]))
        facts {:module module :function-records (:function-records core) :call-edges [] :recursion-components [] :lexical-bindings []
               :function-type-table [{:function-id (sha 12) :parameters [] :parameter-binding-ids [] :latent-effects [] :capabilities []}]
               :type-table {(sha 1) :int (sha 2) :fn (sha 3) :fn} :local-binding-facts [] :call-type-facts []}]
    (is (true? (effects 'body-shape-valid? core facts)))
    (is (false? (effects 'body-shape-valid? core (update facts :function-type-table conj (first (:function-type-table facts))))))))

(defn- local-throw-loop-effect-data []
  ;; Current C7/C8 algorithms on normalized DATA, not source authentication.
  (let [[base base-facts ids] (effect-loop-data)
        extra (into {} (map-indexed (fn [i name] [name ((body-helper 'sha) (+ 1000 i))])
                           [:problem :operator :call :handler :try :caller :caller-definition :callee-binding :caller-binding]))
        ids (merge ids extra) callee (ids :function-target) caller (ids :caller)
        make (fn [name kind children attrs]
               {:node-id (ids name) :core-form kind :children (mapv ids children) :attributes attrs :resolved-binding-ids []
                :binding-context (if (= name :problem) :loop-body caller)
                :source {:form-id (ids name) :syntax-id (ids name) :semantic-span nil :origin-chain [] :generated-origin nil}
                :evaluation {:kind (if (= kind :fn) :function-creation :left-to-right)
                             :order (mapv (fn [i child] {:index i :core-node-id (ids child)})
                                          (range) (cond (= kind :fn) [] (= kind :try) [(first children)] :else children))}})
        problem (make :problem :literal [] {:literal-kind :string :value "loop source error"})
        stop (-> (first (filter #(= (ids :result) (:node-id %)) (:nodes base)))
                 (assoc :core-form :throw :children [(ids :problem)] :attributes {} :resolved-binding-ids [])
                 (assoc :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id (ids :problem)}]}))
        added [(make :operator :reference [] {:binding-id (ids :callee-binding)})
               (make :call :call [:operator] {}) (make :handler :literal [] {:literal-kind :string :value "handled"})
               (make :try :try [:call :handler] {})
               (make :caller :fn [:try] {:recur-target-id caller})
               (make :caller-definition :def [:caller] {})]
        core (-> base
                 (update :nodes #(into (vec (mapcat (fn [node] (if (= (ids :result) (:node-id node)) [problem stop] [node])) %)) added))
                 (assoc :root-core-node-ids [(ids :definition) (ids :caller-definition)]
                        :module {:profile :meta :target :jvm :effects [:error/throw] :capabilities []}
                        :function-records [{:function-syntax-id callee :function-core-node-id callee :body-core-node-id (ids :loop-target)
                                            :definition-binding-id (ids :callee-binding) :fixed-arity 0 :parameter-binding-ids []}
                                           {:function-syntax-id caller :function-core-node-id caller :body-core-node-id (ids :try)
                                            :definition-binding-id (ids :caller-binding) :fixed-arity 0 :parameter-binding-ids []}]
                        :definitions [] :recursion-components []
                        :calls [{:core-node-id (ids :call) :operator-node-id (ids :operator) :operator-binding-id (ids :callee-binding)
                                 :argument-node-ids [] :ordered-evaluation-node-ids [(ids :operator)]
                                 :evaluation-order :operator-then-arguments :result-policy :call-result}]
                        :call-edges [{:ordinal 0 :call-core-node-id (ids :call) :caller-function-syntax-id caller :caller-function-core-node-id caller
                                      :callee-binding-id (ids :callee-binding) :callee-definition-syntax-id callee
                                      :callee-function-syntax-id callee :callee-function-core-node-id callee :argument-core-node-ids []
                                      :ordered-evaluation-node-ids [(ids :operator)] :evaluation-order :operator-then-arguments :classification :local-function}]
                        :error-transfers [{:core-node-id (ids :result) :value-core-node-id (ids :problem)}])
                 (update :recur-targets conj {:target-id caller :target-kind :function :owner-core-node-id caller
                                             :owner-form-id caller :owner-syntax-id caller :arity 0 :binding-ids []
                                             :body-scope-id caller :parent-target-id nil :type-compatibility :pending-sh08}))
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))
        types (merge (:type-table base-facts)
                     {(ids :problem) :gravity.type/string (ids :result) :gravity.type/never
                      (ids :branch) :gravity.type/never (ids :loop-target) :gravity.type/never
                      (ids :operator) :function (ids :call) :gravity.type/never (ids :handler) :gravity.type/string
                      (ids :try) :gravity.type/string caller :function (ids :caller-definition) :function})
        table [{:function-id callee :function-core-node-id callee :body-core-node-id (ids :loop-target)
                :parameters [] :return :gravity.type/never :fixed-arity 0 :latent-effects [:error/throw] :capabilities [] :thrown-error-effects [:gravity.type/string]}
               {:function-id caller :function-core-node-id caller :body-core-node-id (ids :try)
                :parameters [] :return :gravity.type/string :fixed-arity 0 :latent-effects [:error/throw] :capabilities [] :thrown-error-effects []}]
        bindings {(ids :callee-binding) {:binding-id (ids :callee-binding)}}
        groups {(ids :try) [{:record :string-catch :error-type :gravity.type/string :handler-core-node-id (ids :handler)}]}
        owners (effects 'body-throw-owners core 65536)
        scoped (effects 'body-tc-scope core {callee [:gravity.type/string]} 65536)
        joined (assoc scoped :direct-throws {callee [:gravity.type/string]} :owners (:owners owners))
        source (c7-invoke 'sh08-tc-graph [core types (:owners owners) (:scope scoped) groups])
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {}])
        loop-rows (mapv (fn [premise] (effects 'body-loop-row premise (get nodes (or (:initializer-node-id premise)
                                                                                 (get-in premise [:source-transfer :recur-core-node-id]))) (:module core)))
                        (:constraints checked))
        call-row (c7-invoke 'sh08-tc-constraint [core (first (:call-edges core)) (into {} (map (juxt :core-node-id identity) (:calls core)))
                                               nodes (into {} (map (juxt :function-syntax-id identity) (:function-records core)))
                                               bindings types (into {} (map (juxt :function-id identity) table)) (:nodes source)])
        facts {:module (:module core) :type-table types :function-type-table table
               :constraint-ledger (conj loop-rows call-row) :atomic-typed-handlers groups}]
    {:core core :facts facts :bindings bindings :joined joined :ids ids :checked checked :source source}))

(deftest local-throw-call-effect-equations-development-kernel
  (let [{:keys [core types functions direct bindings id fid]} (local-throw-kernel-data)
        result (c7-invoke 'sh08-tc-products [core types functions bindings direct ])
        function-table (c7-invoke 'sh08-th-functions [(vec (vals functions)) (:throws result)])
        facts {:function-type-table function-table :type-table types :constraint-ledger (:constraints result)}
        scoped (effects 'body-tc-scope core (:throws direct) 65536)
        owners (effects 'body-throw-owners core 65536)
        joined (assoc scoped :direct-throws (:throws direct) :owners (:owners owners))
        derived (effects 'body-derive-nodes core facts) graph (:nodes derived)
        verify #(effects 'body-tc-verify core % (into {} (map (juxt :binding-id identity) bindings)) %2 joined 65536)]
    (is (= :accepted (:status derived)))
    (is (not (contains? (:scope scoped) (fid 6))))
    (is (= :accepted (:status (verify facts graph))))
    (is (= [] (get-in (verify facts graph) [:functions (fid 7)])))
    (let [pure-index (first (keep-indexed #(when (= (fid 7) (:function-id %2)) %1) function-table))]
      (is (= :rejected (:status (verify (assoc-in facts [:function-type-table pure-index :thrown-error-effects] [:pending-sh09]) graph)))))
    (is (= [:gravity.type/string] (get-in graph [(id 12) :escaping-thrown-error-types])))
    (is (= #{} (get-in graph [(id 12) :direct-thrown-errors])))
    (is (= [:gravity.type/string] (get-in graph [(id 10) :escaping-thrown-error-types])))
    (is (nil? (get-in graph [(id 10) :call-body-dependency])))
    (is (false? (get-in graph [(id 10) :throw-call-dependency :normal-invocation?])))
    (doseq [bad [(assoc facts :constraint-ledger [])
                 (update facts :constraint-ledger conj (first (:constraint-ledger facts)))
                 (assoc-in facts [:constraint-ledger 0 :dependencies :callee-source-body-id] :forged)
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [:gravity.type/integer])]]
      (is (= :rejected (:status (verify bad graph)))))
    (is (= :rejected (:status (verify facts (assoc-in graph [(id 12) :escaping-thrown-error-types] [])))))
    (let [node {:core-form :call :children [:operator :argument]} graph {:operator [] :argument [:gravity.type/string :gravity.type/bool]
                                                                       :target [:gravity.type/string :gravity.type/integer]}]
      (is (= [:gravity.type/string :gravity.type/bool :gravity.type/integer]
             (c7-invoke 'sh08-tc-node-errors [node graph :target {:argument :gravity.type/string} {}])))
      (is (= [:gravity.type/string :gravity.type/bool]
             (c7-invoke 'sh08-tc-node-errors [node graph nil {:argument :gravity.type/never} {}])))))
  (let [{:keys [core facts bindings joined ids checked source]} (local-throw-loop-effect-data)
        derive #(effects 'body-derive-module-nodes %1 %2 {} 65536)
        derived (derive core facts) graph (:nodes derived)
        verify #(effects 'body-tc-verify %1 %2 bindings %3 joined 65536)
        replace-node (fn [c id f] (update c :nodes #(mapv (fn [node] (if (= id (:node-id node)) (f node) node)) %)))
        reseal (fn [c] (assoc c :identity-preimage (select-keys c [:loop-bindings :recur-targets :recur-transfers])))]
    (is (= :accepted (:status checked) (:status source) (:status derived)) (pr-str {:checked checked :source source :derived derived}))
    (is (true? (:recur-control? derived)))
    (is (true? (effects 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status (effects 'body-loop-joins core facts 65536))))
    (is (= :accepted (:status (verify core facts graph))))
    (is (= {(:function-target ids) [:gravity.type/string] (:caller ids) []} (:functions (verify core facts graph))))
    (doseq [name [:result :branch :loop-target :call]]
      (is (= [:gravity.type/string] (get-in graph [(ids name) :escaping-thrown-error-types]))))
    (is (= [] (effects 'body-error-types graph (ids :transfer))))
    (is (= [] (get-in graph [(ids :try) :escaping-thrown-error-types])))
    (is (= #{:error/throw} (get-in graph [(ids :try) :transitive]) (get-in graph [(ids :transfer) :transitive])))
    (is (= (first (:recur-transfers core)) (get-in graph [(ids :transfer) :loop-backedge :source-transfer])))
    (when (= :accepted (:status derived))
      (is (= derived (effects 'body-derive-module-nodes core facts {} (:work-count derived))))
      (is (= :body-deferred-work-limit (:reason (effects 'body-derive-module-nodes core facts {} (dec (:work-count derived)))))))
    (doseq [bad [(assoc-in facts [:constraint-ledger (dec (count (:constraint-ledger facts))) :dependencies :callee-error-types] [])
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [])
                 (assoc-in facts [:function-type-table 1 :thrown-error-effects] [:gravity.type/string])]]
      (is (= :rejected (:status (verify core bad graph)))))
    (doseq [name [:loop-target :call]]
      (is (= :rejected (:status (verify core facts (assoc-in graph [(ids name) :escaping-thrown-error-types] []))))))
    (is (false? (effects 'body-graph-equations-valid? core facts {}
                        (assoc-in graph [(ids :result) :escaping-thrown-error-types] []))))
    (doseq [bad [(assoc-in core [:recur-transfers 0 :argument-node-ids] [(ids :next-value) (ids :next-again)])
                 (assoc-in core [:recur-transfers 0 :target-id] (ids :function-target))
                 (assoc-in core [:recur-targets 0 :binding-ids] [(ids :value) (ids :again)])]]
      (is (= :rejected (:status (derive (reseal bad) facts)))))
    (let [bad (replace-node core (ids :problem) #(assoc % :core-form :set!))]
      (is (= :rejected (:status (derive bad facts))))
      (is (= :unsupported-throwing-call-component (:reason (verify bad facts graph)))))))

(defn- local-throw-change-summary [typed function-id errors]
  (-> typed
      (update :function-type-table #(mapv (fn [f] (if (= function-id (:function-id f)) (assoc f :thrown-error-effects errors) f)) %))
      (update :type-table #(into {} (map (fn [[id type]] [id (if (and (map? type) (= function-id (:function-id type)))
                                                                            (assoc type :thrown-error-effects errors) type)])) %))))

(deftest atomic-typed-catch-authenticated-paired-composition
    (doseq [extension [".gravity" ".qst"]]
      (let [relative (fixture "accepted" "atomic-typed-catch" extension)
            p ((body-helper 'fresh-products) relative) typed (:typed p)
            core (get-in p [:request :canonical-core-artifact])
            result ((body-helper 'build-leaf) p) graph (:effect-graph result)
            functions (into {} (map (juxt :definition-name identity) (:function-type-table typed)))
            rows (filterv #(= :sh08-atomic-typed-catch-v1 (:producer-rule %)) (:constraint-ledger typed))]
        (is (= :passed (:status (:report p)) (:status (:verification p))))
        (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
        (when-not (= :accepted (:status typed) (:status result))
          (throw (ex-info "Atomic typed catch composition failed" {:typed (:diagnostics typed) :effects (:diagnostics result)})))
        (is (= 8 (count rows)))
        (is (= 10 (count (:error-handlers core))))
        (doseq [[name errors] [['recover []] ['partial [:gravity.type/bool]] ['nested []]
                              ['ordered [:gravity.type/string]] ['rethrow [:gravity.type/string]] ['bypass []] ['call-catch []]]]
          (let [function (functions name)]
            (is (= :gravity.type/string (:return function)))
            (is (= errors (:thrown-error-effects function)))
            (is (= errors (get-in graph [:functions (:function-id function) :throws])))))
        (doseq [row rows]
          (let [id (get-in row [:lhs :core-node-id])]
            (is (= (:rhs row) (get-in graph [:nodes id :escaping-thrown-error-types])))
            (is (= (get-in row [:solution :normal-result-type]) (get-in typed [:type-table id])))
            (is (= (get-in row [:solution :handled-source-errors]) (get-in graph [:nodes id :covered-source-errors])))
            (is (= #{:error/throw} (get-in graph [:nodes id :transitive]) (get-in graph [:nodes id :escaping])))
            (is (= :pending-profile-handler-capability-authority (get-in graph [:nodes id :effect-label-discharge])))))
        (doseq [record (:error-handlers core)
                syntax (:catch-binding-use-syntax-ids record)]
          (let [reference (first (filter #(= syntax (get-in % [:source :syntax-id])) (:nodes core)))
                binding (first (filter #(= (:error-type-binding-id record) (:binding-id %))
                                      (get-in p [:request :b47-context :authenticated-core-request :binding-table])))
                expected (c7-invoke 'sh08-ft-atomic-type-id [(:name binding)])]
            (is (= expected (get-in typed [:type-table (:node-id reference)])))))
        (let [ordered-id (:body-core-node-id (functions 'ordered))
              index (first (keep-indexed #(when (= ordered-id (get-in %2 [:lhs :core-node-id])) %1) (:constraint-ledger typed)))
              ordered-row (get (:constraint-ledger typed) index)
              recover-id (:body-core-node-id (functions 'recover))
              recover-index (first (keep-indexed #(when (= recover-id (get-in %2 [:lhs :core-node-id])) %1) (:constraint-ledger typed)))
              reference-id (:node-id (first (filter #(= (first (:catch-binding-use-syntax-ids (first (:error-handlers core))))
                                                              (get-in % [:source :syntax-id])) (:nodes core))))
              mutations [#(assoc-in % [:constraint-ledger index :lhs :core-node-id] recover-id)
                         #(assoc-in % [:constraint-ledger index :dependencies :catch-types 0] :gravity.type/bool)
                         #(assoc-in % [:constraint-ledger index :dependencies :source-handlers 0 :catch-binding-id] :forged)
                         #(assoc-in % [:constraint-ledger index :dependencies :source-handlers 0 :handler-child-index] 0)
                         #(update-in % [:constraint-ledger index :dependencies :source-handlers] (comp vec reverse))
                         #(assoc-in % [:constraint-ledger index :dependencies :partition :handled] [])
                         #(local-throw-change-summary % (:function-id (functions 'partial)) [])
                         #(local-throw-change-summary % (:function-id (functions 'rethrow)) [])
                         #(assoc-in % [:type-table reference-id] :gravity.type/bool)
                         #(assoc-in % [:constraint-ledger recover-index :solution :normal-result-type] :gravity.type/never)]]
          (is (= [[:gravity.type/string] [:gravity.type/bool] []]
                 (mapv :matched-error-types (get-in ordered-row [:solution :handled-source-errors]))))
          (is (= [:gravity.type/string] (:rhs ordered-row)))
          (doseq [mutate mutations]
            (let [coherent ((body-helper 'coherent-typed-mutation) p mutate)]
              (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [(:request p) (:typed coherent)]))))
              (is (= :rejected (:status ((body-helper 'build-leaf) coherent))))))
          (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
          (doseq [bad [(assoc-in graph [:nodes recover-id :transitive] #{})
                       (assoc-in graph [:nodes recover-id :escaping] #{})
                       (assoc-in graph [:nodes ordered-id :escaping-thrown-error-types] [])
                       (assoc-in graph [:nodes ordered-id :covered-source-errors] [])]]
            (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p)
                                       ((body-helper 'reseal-effect-graph) result bad)))))))
        ))
    (doseq [extension [".gravity" ".qst"]]
      (doseq [[name rule reason] [["atomic-catch-result-type" "C7-TYPE-MISMATCH" :try-normal-result-type-mismatch]
                                ["atomic-catch-unsupported-type" "C7-ANNOTATION" :unsupported-atomic-catch-type]]]
        (let [p ((body-helper 'fresh-products) (fixture "rejected" name extension))]
          (is (= :rejected (get-in p [:typed :status])))
          (is (= [rule reason] ((juxt :rule :reason) (first (get-in p [:typed :diagnostics])))))))
      (let [p ((body-helper 'fresh-products) (fixture "rejected" "atomic-catch-undeclared" extension))
            result ((body-helper 'build-leaf) p)]
        (is (= :accepted (get-in p [:typed :status])))
        (is (= :passed (get-in p [:verification :status])))
        (is (= :rejected (:status result)))
        (is (= "C8-UNDECLARED" (get-in result [:diagnostics 0 :rule])))))
    (doseq [[disposition name] [["accepted" "atomic-typed-catch"] ["rejected" "atomic-catch-result-type"]
                              ["rejected" "atomic-catch-unsupported-type"] ["rejected" "atomic-catch-undeclared"]]]
      (is (= (slurp (path (fixture disposition name ".gravity"))) (slurp (path (fixture disposition name ".qst")))))))

(deftest local-throw-propagation-authenticated-paired-composition
  (doseq [extension [".gravity" ".qst"]]
    (let [relative (fixture "accepted" "local-throw-propagation" extension)
          p ((body-helper 'fresh-products) relative) typed (:typed p)
          core (get-in p [:request :canonical-core-artifact]) result ((body-helper 'build-leaf) p) graph (:effect-graph result)
          functions (into {} (map (juxt :definition-name identity) (:function-type-table typed)))
          rows (filterv #(= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule %)) (:constraint-ledger typed))]
      (is (= :passed (:status (:report p)) (:status (:verification p))))
      (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
      (when-not (= :accepted (:status typed) (:status result))
        (throw (ex-info "Local typed throw propagation failed" {:typed-diagnostics (:diagnostics typed) :effects-diagnostics (:diagnostics result)})))
      (doseq [name '[choose relay relay2 abort]]
        (let [function (functions name)]
          (is (= :gravity.type/string (:return function)))
          (is (= [:gravity.type/string] (:thrown-error-effects function)))
          (is (= [:gravity.type/string] (get-in graph [:functions (:function-id function) :throws])))))
      (is (= [:gravity.type/bool] (:thrown-error-effects (functions 'bool-choice))))
      (is (= [] (:thrown-error-effects (functions 'pure-value))))
      (is (= [] (get-in graph [:functions (:function-id (functions 'pure-value)) :throws])))
      (is (= 5 (count rows)))
      (is (= #{} (get-in graph [:namespace :inferred])))
      (doseq [function (vals functions)]
        (is (= #{} (get-in graph [:nodes (:function-core-node-id function) :transitive]))))
      (let [abort-id (:body-core-node-id (functions 'abort))
            relay-id (:body-core-node-id (functions 'relay2))
            row-index (first (keep-indexed #(when (and (= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule %2))
                                                       (= abort-id (get-in %2 [:lhs :core-node-id]))) %1) (:constraint-ledger typed)))
            row (get (:constraint-ledger typed) row-index)]
        (is (= :gravity.type/never (get (:type-table typed) abort-id)))
        (is (= :gravity.type/never (get-in row [:solution :normal-result-type])))
        (is (false? (get-in row [:dependencies :normal-invocation?])))
        (is (= [] (get-in row [:dependencies :callee-error-types])))
        (is (= [:gravity.type/string] (:rhs row)))
        (is (nil? (get-in graph [:nodes abort-id :call-body-dependency])))
        (is (false? (get-in graph [:nodes abort-id :throw-call-dependency :normal-invocation?])))
        (is (= [:gravity.type/string] (get-in graph [:nodes abort-id :escaping-thrown-error-types])))
        (is (= #{} (get-in graph [:nodes relay-id :direct-thrown-errors])))
        (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
        (doseq [bad [(local-throw-change-summary typed (:function-id (functions 'relay2)) [])
                     (update typed :constraint-ledger #(filterv (fn [row] (not= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule row))) %))
                     (assoc-in typed [:constraint-ledger row-index :dependencies :callee-source-body-id] relay-id)
                     (assoc-in typed [:constraint-ledger row-index :dependencies :source-edge :callee-binding-id] :forged)
                     (update-in typed [:constraint-ledger row-index :dependencies :source-edge :ordered-evaluation-node-ids] #(vec (reverse %)))]]
          (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [(:request p) bad])))))
        (doseq [mutate [#(local-throw-change-summary % (:function-id (functions 'relay2)) [])
                        #(local-throw-change-summary % (:function-id (functions 'pure-value)) [:pending-sh09])
                        #(update % :constraint-ledger (fn [rows] (filterv (fn [row] (not= :sh08-acyclic-local-thrown-errors-v1 (:producer-rule row))) rows)))
                        #(assoc-in % [:constraint-ledger row-index :dependencies :callee-source-body-id] relay-id)
                        #(assoc-in % [:constraint-ledger row-index :dependencies :source-edge :callee-binding-id] :forged)]]
          (is (= :rejected (:status ((body-helper 'build-leaf) ((body-helper 'coherent-typed-mutation) p mutate))))))
        (doseq [bad-graph [(assoc-in graph [:nodes relay-id :call-body-dependency] nil)
                           (assoc-in graph [:nodes relay-id :escaping-thrown-error-types] [])
                           (assoc-in graph [:nodes abort-id :throw-call-dependency :normal-invocation?] true)
                           (assoc-in graph [:functions (:function-id (functions 'pure-value)) :throws] [:pending-sh09])]]
          (let [stale (assoc result :effect-graph bad-graph)
                coherent ((body-helper 'reseal-effect-graph) result bad-graph)]
            (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) stale))))
            (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) coherent)))))))
      ;; Seed observations reuse these accepted bytes and products; no second
      ;; compiler composition is constructed for the execution observations.
      (let [source (path relative) text (slurp source)
            emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
            transport (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw!) original (var-get transport)
            token (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw-token))
            payload (String. "problem") seen (atom [])
            escape (fn [name args] (try (bootstrap/p15-s23-stage2-runtime-execute-function
                                         {:engine :local-typed-throw-seed-observation} compiled name args)
                                       nil (catch clojure.lang.ExceptionInfo error error)))
            [normal thrown aborted]
            (with-redefs-fn {transport (fn [active-plan value] (swap! seen conj value) (original active-plan value))}
              #(vector (bootstrap/p15-s23-stage2-runtime-execute-function
                         {:engine :local-typed-throw-seed-observation} compiled 'relay2 [false payload])
                       (escape 'relay2 [true payload]) (escape 'abort [])))]
        (is (= "8649cc5e1742c00befa785f740840ce2e73ec917028c03f1d4243780659a65a2" (bootstrap/sha256-hex text)))
        (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in compiled [:source :sha256])))
        (is (identical? payload normal))
        (is (identical? payload (:payload (ex-data thrown))))
        (is (= "argument" (:payload (ex-data aborted))))
        (is (identical? token (:gravity.bootstrap/source-throw (ex-data thrown))))
        (is (identical? token (:gravity.bootstrap/source-throw (ex-data aborted))))
        (is (= [payload "argument"] @seen))))
    (doseq [[name rule] [["local-throw-argument-type" "C7-TYPE-MISMATCH"] ["local-throw-result-type" "C7-TYPE-MISMATCH"]
                         ["local-throw-unknown-slot" "C7-GENERIC"]]]
      (let [p ((body-helper 'fresh-products) (fixture "rejected" name extension))]
        (is (= :rejected (get-in p [:typed :status])))
        (is (= rule (get-in p [:typed :diagnostics 0 :rule])) (pr-str (get-in p [:typed :diagnostics])))
        (when (= name "local-throw-unknown-slot")
          (is (= :unresolved-function-type (get-in p [:typed :diagnostics 0 :reason]))))))
    (let [p ((body-helper 'fresh-products) (fixture "rejected" "local-throw-undeclared" extension))
          result ((body-helper 'build-leaf) p)]
      (is (= :accepted (get-in p [:typed :status])))
      (is (= :passed (get-in p [:verification :status])))
      (is (= :rejected (:status result)))
      (is (= "C8-UNDECLARED" (get-in result [:diagnostics 0 :rule])) (pr-str (:diagnostics result)))))
  (doseq [[disposition name] [["accepted" "local-throw-propagation"] ["rejected" "local-throw-argument-type"]
                              ["rejected" "local-throw-result-type"] ["rejected" "local-throw-unknown-slot"] ["rejected" "local-throw-undeclared"]]]
    (is (= (slurp (path (fixture disposition name ".gravity"))) (slurp (path (fixture disposition name ".qst")))))))

(deftest direct-typed-throw-authenticated-paired-composition
  ;; Actual source/compiler products; no seed execution or host exception claim.
  (doseq [extension [".gravity" ".qst"]]
    (let [p ((body-helper 'fresh-products) (fixture "accepted" "direct-typed-throw" extension))
          typed (:typed p) core (get-in p [:request :canonical-core-artifact])
          transfer (first (:error-transfers core)) id (:core-node-id transfer) payload (:value-core-node-id transfer)
          rows (filterv #(= :sh08-direct-typed-throw-v1 (:producer-rule %)) (:constraint-ledger typed))
          result ((body-helper 'build-leaf) p) graph (:effect-graph result)
          function (first (:function-type-table typed))]
      (is (= :passed (:status (:report p)) (:status (:verification p))))
      (is (= :accepted (:status typed) (:status result)) (pr-str [(:diagnostics typed) (:diagnostics result)]))
      (when-not (= :accepted (:status typed) (:status result))
        (throw (ex-info "Direct typed throw composition failed" {:typed typed :result result})))
      (is (= :gravity.type/string (get (:type-table typed) payload) (:return function)))
      (is (= :gravity.type/never (get (:type-table typed) id)))
      (is (= [:gravity.type/string] (:thrown-error-effects function)))
      (is (= 1 (count rows)))
      (is (= transfer (get-in (first rows) [:dependencies :source-transfer])))
      (is (= #{:error/throw} (get-in graph [:nodes id :direct]) (get-in graph [:nodes id :escaping])
             (get-in graph [:functions (:function-id function) :inferred])))
      (is (= #{:gravity.type/string} (get-in graph [:nodes id :direct-thrown-errors])
             (get-in graph [:nodes id :escaping-thrown-errors])))
      (is (= [:gravity.type/string] (get-in graph [:functions (:function-id function) :throws])))
      (is (= [:evaluate-value :transfer-error] (get-in graph [:nodes id :error-transfer :ordered-steps])))
      (is (= :passed (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) result))))
      (doseq [bad [(assoc-in typed [:type-table payload] :gravity.type/bool)
                   (assoc typed :constraint-ledger (filterv #(not= :sh08-direct-typed-throw-v1 (:producer-rule %)) (:constraint-ledger typed)))
                   (assoc-in typed [:function-type-table 0 :thrown-error-effects] [])]]
        (is (= :rejected (:status (c7-invoke 'sh08-verify-function-type-result [(:request p) bad])))))
      (doseq [bad [(assoc-in result [:effect-graph :nodes id :direct] #{})
                   (assoc-in result [:effect-graph :nodes id :escaping-thrown-errors] #{:gravity.type/bool})
                   (assoc-in result [:effect-graph :nodes id :error-transfer :ordered-steps] [:transfer-error :evaluate-value])
                   (assoc-in result [:effect-graph :functions (:function-id function) :throws] [])]]
        (is (= :rejected (:status (effects 'verify-body-effect-result typed (:verification p) (:binding p) bad)))))
      (doseq [mutate [#(assoc-in % [:function-type-table 0 :thrown-error-effects] [])
                      #(assoc % :constraint-ledger (filterv (fn [row] (not= :sh08-direct-typed-throw-v1 (:producer-rule row))) (:constraint-ledger %)))]]
        (is (= :rejected (:status ((body-helper 'build-leaf) ((body-helper 'coherent-typed-mutation) p mutate)))))))
    (doseq [[basename expected] [["direct-throw-unknown-payload" "C7-ANNOTATION"]
                                 ["direct-throw-result-type" "C7-TYPE-MISMATCH"]]]
      (let [p ((body-helper 'fresh-products) (fixture "rejected" basename extension))]
        (is (= :rejected (get-in p [:typed :status])))
        (is (= expected (get-in p [:typed :diagnostics 0 :rule])) (pr-str (get-in p [:typed :diagnostics])))))
    (let [p ((body-helper 'fresh-products) (fixture "rejected" "direct-throw-undeclared" extension))
          result ((body-helper 'build-leaf) p)]
      (is (= :accepted (get-in p [:typed :status])))
      (is (= :passed (get-in p [:verification :status])))
      (is (= :rejected (:status result)))
      (is (= "C8-UNDECLARED" (get-in result [:diagnostics 0 :rule])) (pr-str (:diagnostics result)))))
  (doseq [[disposition basename] [["accepted" "direct-typed-throw"] ["rejected" "direct-throw-unknown-payload"]
                                  ["rejected" "direct-throw-result-type"] ["rejected" "direct-throw-undeclared"]]]
    (is (= (slurp (path (fixture disposition basename ".gravity")))
           (slurp (path (fixture disposition basename ".qst")))))))

(deftest direct-typed-throw-annotated-seed-execution
  ;; Reuse completed paired C5-C8 typing/body-effect/source-allowance evidence for these exact
  ;; bytes. This observes the temporary single-source Clojure seed, not a JVM ABI.
  (let [transport (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw!)
        token (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw-token))
        original (var-get transport)
        expected "755e5b876b8fde5f1be48b0dce2cea96c17da24d967b4d9e641c720a46617f23"
        gravity (slurp (path (fixture "accepted" "direct-typed-throw" ".gravity")))]
    (doseq [extension [".gravity" ".qst"]]
      (let [source (path (fixture "accepted" "direct-typed-throw" extension)) text (slurp source)
            emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source text)
            payload (String. "problem") seen (atom [])
            [normal failure]
            (with-redefs-fn {transport (fn [active-plan value] (swap! seen conj value) (original active-plan value))}
              #(let [normal (bootstrap/p15-s23-stage2-runtime-execute-function
                              {:engine :direct-typed-throw-annotated-seed-observation} compiled 'choose [false payload])
                     failure (try (bootstrap/p15-s23-stage2-runtime-execute-function
                                    {:engine :direct-typed-throw-annotated-seed-observation} compiled 'choose [true payload])
                                  nil (catch clojure.lang.ExceptionInfo error error))]
                 [normal failure]))]
        (is (= gravity text))
        (is (= expected (bootstrap/sha256-hex text)))
        (is (= (str "sha256:" expected) (get-in compiled [:source :sha256])))
        (is (= {:parameters '[flag :- Bool problem :- String] :return '[typed/return (quote String)]}
               (get-in compiled [:functions 'choose :source-declarations])))
        (is (= '[flag problem] (get-in compiled [:functions 'choose :params])))
        (is (identical? payload normal))
        (is (instance? clojure.lang.ExceptionInfo failure))
        (is (identical? token (:gravity.bootstrap/source-throw (ex-data failure))))
        (is (identical? payload (:payload (ex-data failure))))
        (is (= [payload] @seen))
        (is (= (:source compiled) (:source (ex-data failure))))
        (is (= :clojure-stage0-seed (get-in compiled [:compiler :owner])))
        (is (= :temporary-single-source-seed-throw (get-in compiled [:compiler :lowering-boundary])))
        (is (= :pending-seed-effect-inference (get-in compiled [:effect-summary :inferred])))))))

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

(deftest loop-lexical-source-error-development-kernel
  ;; The actual validating loop kernel runs before the actual source-error
  ;; graph here, as in public C7. These normalized premises confer no authority.
  (let [[base table] (loop-data)
        locations [[:value-init :initializers :gravity.type/string]
                   [:next-value :loop-body :gravity.type/bool]
                   [:result :loop-body :gravity.type/keyword]]
        branches (into {} (map (fn [[id scope _]]
                                 [id (node id :if [(keyword (str (name id) "-test"))
                                                   (keyword (str (name id) "-throw"))
                                                   (keyword (str (name id) "-normal"))] scope {})]) locations))
        additions (mapcat (fn [[id scope _]]
                            [(node (keyword (str (name id) "-test")) :literal [] scope {})
                             (node (keyword (str (name id) "-payload")) :literal [] scope {})
                             (node (keyword (str (name id) "-throw")) :throw [(keyword (str (name id) "-payload"))] scope {})
                             (node (keyword (str (name id) "-normal")) :literal [] scope {})]) locations)
        core (assoc base :nodes (into (vec additions) (map #(get branches (:node-id %) %) (:nodes base))))
        types (reduce (fn [out [id _ payload-type]]
                        (assoc out (keyword (str (name id) "-test")) :gravity.type/bool
                                   (keyword (str (name id) "-payload")) payload-type
                                   (keyword (str (name id) "-throw")) :gravity.type/never
                                   (keyword (str (name id) "-normal")) :gravity.type/integer)) table locations)
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {}])
        owners (zipmap (map :node-id (:nodes core)) (repeat :function-target))
        derive #(c7-invoke 'sh08-tc-graph [%1 %2 owners #{:function-target} {}])
        graph (derive core types)]
    (is (= :accepted (:status checked)))
    (is (= true (:normal-transfer? (last (:constraints checked)))))
    (is (= :accepted (:status graph)))
    (is (= [:gravity.type/bool] (get-in graph [:nodes :transfer])))
    (is (= [:gravity.type/string :gravity.type/bool :gravity.type/keyword] (get-in graph [:nodes :loop-target])))
    (is (= [] (get-in graph [:nodes :function-target])))
    (is (not (some #{:gravity.type/never} (mapcat val (:nodes graph)))))
    (let [never-core (update base :nodes
                       #(into [(node :inner-problem :literal [] :loop-body {})
                               (node :inner-throw :throw [:inner-problem] :loop-body {})]
                          (map (fn [n] (if (= :next-value (:node-id n))
                                         (node :next-value :throw [:inner-throw] :loop-body {}) n)) %)))
          never-types (assoc table :next-value :gravity.type/never :inner-problem :gravity.type/string :inner-throw :gravity.type/never)
          never (c7-invoke 'sh08-ft-loop-type-step [never-core never-types {}])
          errors (c7-invoke 'sh08-tc-graph [never-core never-types
                    (zipmap (map :node-id (:nodes never-core)) (repeat :function-target)) #{:function-target} {}])]
      (is (= :accepted (:status never)))
      (is (= false (:normal-transfer? (last (:constraints never)))))
      (is (= [:gravity.type/string] (get-in errors [:nodes :transfer])))
      (is (= [:gravity.type/string] (get-in errors [:nodes :loop-target])))
      (is (not (some #{:gravity.type/never} (mapcat val (:nodes errors))))))
    (doseq [bad [(assoc-in core [:recur-transfers 0 :target-id] :function-target)
                 (assoc-in core [:recur-transfers 0 :tail-position] false)
                 (assoc-in core [:loop-bindings 1 :initializer-syntax-id] :forged)]]
      (is (= :rejected (:status (c7-invoke 'sh08-ft-loop-type-step [bad types {}])))))
    (is (= "C7-TYPE-MISMATCH"
           (get-in (c7-invoke 'sh08-ft-loop-type-step [core (assoc types :next-value :gravity.type/string) {}]) [:issues 0 :rule])))
    (is (= :unsupported-throwing-body-operation
           (get-in (derive (update core :nodes #(mapv (fn [n] (if (= :transfer (:node-id n)) (assoc n :core-form :set!) n)) %)) types)
                   [:diagnostics 0 :reason]))))
  (let [[base table] (function-recur-data)
        payload (node :problem :literal [] :function-body {})
        core (update base :nodes #(into [payload] (map (fn [n] (if (= :next-value (:node-id n))
                                                               (node :next-value :throw [:problem] :function-body {}) n)) %)))
        types (assoc table :problem :gravity.type/string :next-value :gravity.type/never)
        checked (c7-invoke 'sh08-ft-loop-type-step [core types {}])
        graph (c7-invoke 'sh08-tc-graph [core types (zipmap (map :node-id (:nodes core)) (repeat :function-target))
                                         #{:function-target} {}])]
    (is (= :accepted (:status checked)))
    (is (= false (:normal-transfer? (first (:constraints checked)))))
    (is (= :accepted (:status graph)))
    (is (= [:gravity.type/string] (get-in graph [:nodes :transfer])))
    (is (= :gravity.type/never (:transfer types)))
    (is (= :gravity.type/string (:branch types)))))

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
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core joined {}]))))
    (is (= :gravity.type/never
           (c7-invoke 'sh08-ft-infer-node-with-context
             [(assoc-in transfer [:attributes :target-kind] :loop) table {} {} {} {}])))
    (is (= :gravity.type/unknown
           (c7-invoke 'sh08-ft-infer-node-with-context
             [(assoc-in transfer [:attributes :target-kind] :forged) table {} {} {} {}])))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(assoc-in core [:recur-transfers 0 :target-id] :forged) joined {}]) [:issues 0 :rule])))
    (is (= "C7-ANNOTATION"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                      [(update-in core [:nodes 6 :attributes] dissoc :signature) joined
                      {:flag :gravity.type/bool :value :gravity.type/string}]) [:issues 0 :rule])))))

(deftest function-recur-slots-development-kernel
  (let [[core table] (function-recur-data)
        result (c7-invoke 'sh08-ft-loop-type-step [core table {}])
        premise (first (:constraints result))]
    (is (= :accepted (:status result)))
    (is (= [:gravity.type/bool :gravity.type/string] (:slot-types premise) (:argument-types premise)))
    (is (= :gravity.type/never (:result-type premise)))
    (is (= :solved (:status premise)))
    (doseq [[id type expected] [[:next-value :gravity.type/bool :gravity.type/string]
                               [:next-flag :gravity.type/string :gravity.type/bool]]]
      (let [bad (c7-invoke 'sh08-ft-loop-type-step [core (assoc table id type) {}])]
        (is (= ["C7-TYPE-MISMATCH" :function-recur-slot-type-mismatch id expected type]
               ((juxt :rule :reason :core-node-id :expected :actual) (first (:issues bad)))))))
    (let [unannotated (update-in core [:nodes 6 :attributes] dissoc :signature)
          caller-types {:flag :gravity.type/bool :value :gravity.type/string}
          pending (c7-invoke 'sh08-ft-loop-type-step [unannotated table caller-types])]
      (is (= ["C7-ANNOTATION" :function-recur-parameter-types-required]
             ((juxt :rule :reason) (first (:issues pending)))))
      (is (= "C7-VERIFY"
             (get-in (c7-invoke 'sh08-ft-loop-type-step
                       [(assoc-in unannotated [:recur-transfers 0 :target-id] :forged) table caller-types]) [:issues 0 :rule]))))
    (let [never (c7-invoke 'sh08-ft-loop-type-step [core (assoc table :next-value :gravity.type/never) {}])]
      (is (= :accepted (:status never)))
      (is (= false (:normal-transfer? (first (:constraints never))))))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step
                     [(assoc-in core [:binding-table 1 :name] 'forged) table {}]) [:issues 0 :rule])))))

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
        checked (c7-invoke 'sh08-ft-loop-type-step [core table {}])
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
    (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [core {} {}]))))
    (is (= "C7-VERIFY"
           (get-in (c7-invoke 'sh08-ft-loop-type-step [(assoc-in core [:recur-transfers 0 :target-id] :forged) {} {}]) [:issues 0 :rule])))
    (let [outer (assoc-in (node :outer :loop [:function-target] :namespace
                                {:target-id :outer :binding-count 0 :body-count 1 :initializer-child-indexes []
                                 :body-child-indexes [0] :result-policy {:kind :last-body :child-index 0}})
                          [:evaluation :kind] :initializers-then-body-left-to-right)
          outer-target {:target-id :outer :target-kind :loop :owner-core-node-id :outer :owner-form-id :outer-form
                        :owner-syntax-id :outer :arity 0 :binding-ids [] :body-scope-id :outer-body
                        :parent-target-id nil :type-compatibility :pending-sh08}
          nested (-> core (assoc-in [:nodes 1 :binding-context] :outer-body) (update :nodes conj outer)
                         (update :recur-targets conj outer-target))]
      (is (= :accepted (:status (c7-invoke 'sh08-ft-loop-type-step [nested {} {}]))))
      (is (= "C7-VERIFY" (get-in (c7-invoke 'sh08-ft-loop-type-step [(-> nested
                                     (assoc-in [:recur-transfers 0 :target-id] :outer)
                                     (assoc-in [:recur-transfers 0 :target-kind] :loop)
                                     (assoc-in [:nodes 0 :attributes :target-id] :outer)
                                     (assoc-in [:nodes 0 :attributes :target-kind] :loop)) {} {}]) [:issues 0 :rule]))))))

(defn- symbol-set-loop-data []
  ;; Source-linked normalized DATA exercises existing owners; it is not source admission.
  (require 'gravity.self-hosting.sh01-stage2-runtime-iteration-test)
  (let [module {:namespace 'example.symbol-loop :profile :meta :target :jvm :effects [:memory/allocate] :capabilities []}
        base ((ns-resolve 'gravity.self-hosting.sh01-stage2-runtime-iteration-test 'typed-loop-kernel-data) module)
        original (update (:context base) :nodes
                         (fn [nodes] (into {} (map (fn [[id node]]
                           [id (if (map? (:evaluation node)) node
                                 (assoc node :evaluation
                                   {:kind (case (:core-form node) :if :condition-then-exclusive-branch :fn :function-creation :value)
                                    :owner-function-syntax-id nil
                                    :order (mapv (fn [index child] {:index index :core-node-id child}) (range)
                                             (case (:core-form node) :fn [] :if [(first (:children node))] (:children node)))}))]) nodes))))
        empty-type {:kind :gravity.type/set :shape :literal :element-types []}
        carrier {:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/symbol}
        insertion ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'list-prepend-input) carrier :gravity.type/symbol)
        forms (merge (:forms original) (:forms insertion))
        forms (-> forms
                  (assoc-in [:value-init-form :kind] :list) (assoc-in [:value-init-form :value] '(quote #{}))
                  (assoc-in [:value-init-form :child-form-ids] ["sha256:0000000000000000000000000000000000000000000000000000000000001b59" "sha256:0000000000000000000000000000000000000000000000000000000000001b5a"])
                  (assoc-in [:transfer-form :child-form-ids] [:recur-op :next-again-form :call])
                  (assoc-in [:call :parent-form-id] :transfer-form) (assoc-in [:call :scope-id] :value-scope)
                  (assoc-in [:arg0 :value] 'value) (assoc-in [:arg0 :scope-id] :value-scope)
                  (assoc-in [:arg1 :kind] :list) (assoc-in [:arg1 :value] '(quote assoc))
                  (assoc-in [:arg1 :scope-id] :value-scope) (assoc-in [:arg1 :child-form-ids] ["sha256:0000000000000000000000000000000000000000000000000000000000001b5b" "sha256:0000000000000000000000000000000000000000000000000000000000001b5c"])
                  (assoc "sha256:0000000000000000000000000000000000000000000000000000000000001b59" {:form-id "sha256:0000000000000000000000000000000000000000000000000000000000001b59" :syntax-id "sha256:0000000000000000000000000000000000000000000000000000000000001b59" :kind :symbol :value 'quote :parent-form-id :value-init-form :child-form-ids []}
                         "sha256:0000000000000000000000000000000000000000000000000000000000001b5a" {:form-id "sha256:0000000000000000000000000000000000000000000000000000000000001b5a" :syntax-id "sha256:0000000000000000000000000000000000000000000000000000000000001b5a" :kind :set :value #{} :parent-form-id :value-init-form :child-form-ids []}
                         "sha256:0000000000000000000000000000000000000000000000000000000000001b5b" {:form-id "sha256:0000000000000000000000000000000000000000000000000000000000001b5b" :syntax-id "sha256:0000000000000000000000000000000000000000000000000000000000001b5b" :kind :symbol :value 'quote :parent-form-id :arg1 :child-form-ids []}
                         "sha256:0000000000000000000000000000000000000000000000000000000000001b5c" {:form-id "sha256:0000000000000000000000000000000000000000000000000000000000001b5c" :syntax-id "sha256:0000000000000000000000000000000000000000000000000000000000001b5c" :kind :symbol :value 'assoc :parent-form-id :arg1 :child-form-ids []}))
        forms (into {} (map (fn [[id form]] [id (merge {:form-id id :syntax-id id :origin-chain [] :generated-origin []} form)]) forms))
        source (fn [form-id] (let [form (get forms form-id)]
                              {:form-id form-id :syntax-id (:syntax-id form) :semantic-span (:source-span form)
                               :origin-chain (:origin-chain form) :generated-origin (:generated-origin form)}))
        primitive (into {} (map (fn [[id node]] [id (assoc node :binding-context :value-scope :source (source id))]) (:nodes insertion)))
        quote-node (fn [node form-id payload-id kind value]
                     (assoc node :core-form :quote :children [] :resolved-binding-ids [] :source (source form-id)
                            :attributes {:quoted-form-id payload-id :quoted-syntax-id payload-id :quoted-kind kind :quoted-value value}
                            :evaluation {:kind :no-evaluation :order []}))
        nodes (-> (merge (:nodes original) primitive) (dissoc :next-value)
                  (assoc :value-init (quote-node (get-in original [:nodes :value-init]) :value-init-form "sha256:0000000000000000000000000000000000000000000000000000000000001b5a" :set #{})
                         :arg1 (quote-node (get primitive :arg1) :arg1 "sha256:0000000000000000000000000000000000000000000000000000000000001b5c" :symbol 'assoc))
                  (assoc-in [:arg0 :attributes] {:binding-id :value}) (assoc-in [:arg0 :resolved-binding-ids] [:value])
                  (assoc-in [:transfer :children] [:next-again :call])
                  (assoc-in [:transfer :evaluation :order] [{:index 0 :core-node-id :next-again} {:index 1 :core-node-id :call}]))
        bindings (merge (:bindings original) (:bindings insertion))
        transfer (assoc (get-in original [:transfers :transfer]) :argument-node-ids [:next-again :call])
        ids [:again-init :value-init :condition :next-again :operator :arg0 :arg1 :call :transfer :result :branch :loop-node :fn-node]
        core {:module module :nodes (mapv nodes ids) :loop-bindings (:loop-bindings original)
              :recur-targets [(get-in original [:targets "function-target"]) (get-in original [:targets "loop-target"])]
              :recur-transfers [transfer] :lexical-bindings [] :function-records [] :definitions []
              :calls [(:call insertion)] :call-edges [(:edge insertion)] :binding-table (vec (vals bindings))}
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))
        types (merge (:types original) {:value-init empty-type :arg0 carrier :arg1 :gravity.type/symbol
                                       :call carrier :result carrier :branch carrier :loop-node carrier})
        derived (c7-invoke 'sh08-pl-derive [core (nodes :call) (:call insertion) (:edge insertion) nodes bindings types forms])
        descriptor (:descriptor derived) types (assoc types :operator descriptor)
        quotes (c7-invoke 'sh08-qd-products [(:nodes core) (vec (vals forms)) module])
        working (assoc core :loop-member-source {:forms forms :bindings bindings :nodes nodes})
        checked (c7-invoke 'sh08-ft-loop-type-step [working types {}])
        rows (c7-invoke 'sh08-ft-loop-constraints [(:constraints checked) nodes module])
        ledger (into (into (vec rows) (:constraints quotes))
                     [(c7-invoke 'sh08-pl-constraint [descriptor])])
        context (-> original (assoc :core core :nodes nodes :forms forms :bindings bindings :types types
                                    :calls {:call (:call insertion)} :edges {:call (:edge insertion)} :call-order {:call 0}
                                    :call-facts {:call (c7-invoke 'sh08-pl-call-fact [descriptor module])}
                                    :ledger ledger :loop-rows (into {} (map (fn [row] [[(get-in row [:dependencies :loop-premise :kind]) (:lhs row)] row]) rows))
                                    :transfers {:transfer transfer}))]
    {:core core :working working :types types :nodes nodes :forms forms :bindings bindings
     :carrier carrier :empty-type empty-type :derived derived :quotes quotes :checked checked :rows rows :context context}))

(defn- symbol-set-loop-alias-data []
  (let [{:keys [core types nodes forms bindings context] :as original} (symbol-set-loop-data)
        record {:binding-id :member :mutability :immutable :initializer-node-id :arg1
                :initializer-form-id :arg1 :initializer-syntax-id :arg1
                :definition-syntax-id :member-name :binding-scope-id :value-scope}
        alias-node {:node-id :member-use :core-form :reference :children [] :binding-context :value-scope
                    :resolved-binding-ids [:member] :attributes {:binding-id :member}
                    :source {:form-id :member-use :syntax-id :member-use :semantic-span nil :origin-chain [] :generated-origin []}
                    :evaluation {:kind :value :owner-function-syntax-id nil :order []}}
        forms (-> forms (assoc :member-use {:form-id :member-use :syntax-id :member-use :kind :symbol :value 'member
                                            :scope-id :value-scope :parent-form-id :call :child-form-ids []
                                            :origin-chain [] :generated-origin []})
                        (assoc-in [:call :child-form-ids] [:operator :arg0 :member-use]))
        bindings (assoc bindings :member {:binding-id :member :name 'member :kind :local :binding-class :lexical
                                         :scope-id :value-scope :definition-syntax-id :member-name})
        nodes (-> nodes (assoc :member-use alias-node)
                        (assoc-in [:call :children] [:operator :arg0 :member-use])
                        (assoc-in [:call :evaluation :order] [{:index 0 :core-node-id :operator}
                           {:index 1 :core-node-id :arg0} {:index 2 :core-node-id :member-use}]))
        call (assoc (first (:calls core)) :argument-node-ids [:arg0 :member-use]
                                         :ordered-evaluation-node-ids [:operator :arg0 :member-use])
        edge (assoc (first (:call-edges core)) :argument-core-node-ids [:arg0 :member-use]
                                              :ordered-evaluation-node-ids [:operator :arg0 :member-use])
        core (assoc core :nodes (into [alias-node] (mapv #(nodes (:node-id %)) (:nodes core)))
                         :lexical-bindings [record] :binding-table (vec (vals bindings)) :calls [call] :call-edges [edge])
        types (assoc types :member-use :gravity.type/symbol)
        derived (c7-invoke 'sh08-pl-derive [core (nodes :call) call edge nodes bindings types forms])
        descriptor (:descriptor derived) types (assoc types :operator descriptor)
        working (assoc core :loop-member-source {:forms forms :bindings bindings :nodes nodes})
        checked (c7-invoke 'sh08-ft-loop-type-step [working types {}])
        rows (c7-invoke 'sh08-ft-loop-constraints [(:constraints checked) nodes (:module core)])
        ledger (into (into rows (:constraints (:quotes original))) [(c7-invoke 'sh08-pl-constraint [descriptor])])
        context (-> context (assoc :core core :nodes nodes :forms forms :bindings bindings :types types
                                   :calls {:call call} :edges {:call edge} :ledger ledger
                                   :call-facts {:call (c7-invoke 'sh08-pl-call-fact [descriptor (:module core)])}
                                   :loop-rows (into {} (map (fn [row] [[(get-in row [:dependencies :loop-premise :kind]) (:lhs row)] row]) rows)))
                    (assoc-in [:locals :member] 'member) (assoc-in [:parameter-types :member] :gravity.type/symbol))]
    (assoc original :core core :working working :types types :nodes nodes :forms forms :bindings bindings
                    :derived derived :checked checked :rows rows :context context)))

(deftest symbol-set-loop-invariant-development-kernel
  (let [{:keys [core working types nodes forms bindings carrier empty-type derived quotes checked rows]} (symbol-set-loop-data)
        invariant (get-in checked [:member-invariants :value])
        check #(c7-invoke 'sh08-ft-loop-type-step [%1 %2 {}])
        source {:nodes nodes :forms forms :bindings bindings}
        solved (c7-invoke 'sh08-lsi-solve [core types source])]
    (is (= :accepted (:status derived) (:status checked) (:status solved)))
    (is (= [] (:diagnostics quotes)))
    (is (= carrier (get-in checked [:bindings :value])))
    (is (= empty-type (:value-init types) (:initializer-type invariant)))
    (is (= #{:schema :target-id :binding-id :initializer-node-id :initializer-type :invariant-type :grounding :entries}
           (set (keys invariant))))
    (is (= :gravity/sh08-loop-symbol-set-invariant-v2 (:schema invariant)))
    (is (= carrier (:invariant-type invariant)))
    (is (= [:call] (get-in invariant [:grounding :result-path])))
    (is (= [:arg0] (get-in invariant [:grounding :receiver-path])))
    (is (= [:arg1] (get-in invariant [:grounding :member-path])))
    (is (= [{:transfer-node-id :transfer :argument-node-id :call :argument-type carrier
             :normal-transfer? true :compatibility :exact-carrier}] (:entries invariant)))
    (is (= [:sh08-loop-target-types-v1 :sh08-loop-target-types-v2 :sh08-loop-target-types-v2] (mapv :producer-rule rows)))
    (is (= [:value] (get-in (last rows) [:solution :member-invariant-bindings])))
    (is (= invariant (get-in (second rows) [:solution :member-invariant])))
    (doseq [changed [(assoc types :next-again :gravity.type/never) (assoc types :call :gravity.type/never)
                     (assoc types :arg1 :gravity.type/unknown)
                     (assoc-in types [:operator :version] :sh08-symbol-set-insertion-v1)]]
      (is (empty? (:invariants (c7-invoke 'sh08-lsi-solve [core changed source])))))
    (is (= :rejected (:status (c7-invoke 'sh08-lsi-ground
          [(first (:recur-transfers core)) 1 :other-binding
           {:core core :types types :nodes nodes :forms forms :bindings bindings :aliases {}
            :calls (into {} (map (juxt :core-node-id identity) (:calls core)))
            :edges (into {} (map (juxt :call-core-node-id identity) (:call-edges core)))}]))))
    (is (= :incoherent-loop-transfer
           (get-in (check (assoc-in working [:recur-transfers 0 :tail-position] false) types) [:issues 0 :reason])))
    (let [empty (assoc types :arg0 empty-type :result empty-type :branch empty-type :loop-node empty-type)
          transfer (assoc (first (:recur-transfers core)) :argument-node-ids [:next-again :arg0])
          self-core (-> core (assoc :calls [] :call-edges [] :recur-transfers [transfer])
                        (update :nodes #(mapv (fn [node]
                          (if (= :transfer (:node-id node))
                            (-> node (assoc :children [:next-again :arg0])
                                (assoc-in [:evaluation :order] [{:index 0 :core-node-id :next-again} {:index 1 :core-node-id :arg0}])) node))
                          (remove (fn [node] (contains? #{:operator :arg1 :call} (:node-id node))) %))))
          self-core (assoc self-core :loop-member-source {:forms forms :bindings bindings})
          exact (check self-core empty)]
      (is (= :accepted (:status exact)))
      (is (= empty-type (get-in exact [:bindings :value])))
      (is (empty? (:member-invariants exact)))
      (is (every? #(= :sh08-loop-target-types-v1 (:producer-rule %))
                  (c7-invoke 'sh08-ft-loop-constraints [(:constraints exact) nodes (:module core)]))))
    (let [extra (assoc (first (:recur-transfers core)) :recur-core-node-id :other-transfer :argument-node-ids [:next-again :bad])
          bad-type {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword]}
          changed (assoc core :recur-transfers (conj (:recur-transfers core) extra))
          rejected (c7-invoke 'sh08-lsi-solve [changed (assoc types :bad bad-type) source])]
      (is (= :rejected (:status rejected)))
      (is (= :loop-recur-slot-type-mismatch (:reason rejected)))
      (is (= [:bad carrier bad-type] ((juxt :core-node-id :expected :actual) rejected))))
    (doseq [mutate [#(assoc-in % [:forms (get-in nodes [:arg1 :attributes :quoted-form-id]) :value] "assoc")
                    #(assoc-in % [:forms (get-in nodes [:arg1 :attributes :quoted-form-id]) :parent-form-id] :other)
                    #(assoc-in % [:nodes :arg0 :resolved-binding-ids] [:again])
                    #(assoc-in % [:nodes :arg0 :binding-context] :other)]]
      (is (empty? (:invariants (c7-invoke 'sh08-lsi-solve [core types (mutate source)])))))
    (let [alias-record {:binding-id :member :mutability :immutable :initializer-node-id :arg1
                        :initializer-form-id :arg1 :initializer-syntax-id :arg1
                        :definition-syntax-id :member-name :binding-scope-id :value-scope}
          alias-node {:node-id :alias-use :core-form :reference :children [] :binding-context :value-scope
                      :resolved-binding-ids [:member] :attributes {:binding-id :member}
                      :source {:form-id :alias-form :syntax-id :alias-form}}
          env {:nodes (assoc nodes :alias-use alias-node) :forms (assoc forms :alias-form
                {:form-id :alias-form :syntax-id :alias-form :kind :symbol :value 'member :scope-id :value-scope})
               :bindings (assoc bindings :member {:name 'member :scope-id :value-scope :definition-syntax-id :member-name})
               :types (assoc types :alias-use :gravity.type/symbol) :aliases {:member alias-record}}
          walk #(c7-invoke 'sh08-lsi-path [:alias-use :member :value %]) accepted (walk env)]
      (is (= :accepted (:status accepted)))
      (is (= [:alias-use :arg1] (:path accepted)))
      (is (= :rejected (:status (walk (assoc-in env [:aliases :member :mutability] :mutable)))))
      (is (= :rejected (:status (walk (assoc-in env [:types :alias-use] :gravity.type/unknown)))))
      (is (= :circular-loop-member-grounding
             (:reason (walk (update-in env [:aliases :member] assoc :initializer-node-id :alias-use
                             :initializer-form-id :alias-form :initializer-syntax-id :alias-form))))))
    (let [transfer (assoc (first (:recur-transfers core)) :recur-core-node-id :empty-transfer
                         :argument-node-ids [:next-again :empty-argument])
          result (c7-invoke 'sh08-lsi-solve [(update core :recur-transfers conj transfer)
                    (assoc types :empty-argument empty-type) source])]
      (is (= :accepted (:status result)))
      (is (= [:exact-carrier :empty-set-to-symbol-carrier]
             (mapv :compatibility (get-in result [:invariants :value :entries])))))
    (let [transfers [{:target-id "loop-target" :argument-node-ids [:before :call :after]}]]
      (doseq [changed [(assoc types :before :gravity.type/never :after :gravity.type/bool)
                      (assoc types :before :gravity.type/bool :after :gravity.type/never)]]
        (is (false? (get-in (c7-invoke 'sh08-lsi-entry-index [transfers changed])
                            [:index "loop-target" 0 :normal?])))))
    (let [alias (symbol-set-loop-alias-data)]
      (is (= :accepted (get-in alias [:derived :status]) (get-in alias [:checked :status])))
      (is (= [:member-use :arg1] (get-in alias [:checked :member-invariants :value :grounding :member-path]))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "quote child hash")))
                                    (equals [_] (swap! callbacks inc) (throw (AssertionError. "quote child equality")))
                                    (toString [] (swap! callbacks inc) (throw (AssertionError. "quote child printing"))))]
      (doseq [bad [opaque [opaque] {:opaque opaque}] coordinate [0 1]]
        (let [changed (assoc-in forms [:arg1 :child-form-ids coordinate] bad)
              env {:nodes nodes :forms changed :bindings bindings :types types :aliases {}}]
          (is (false? (c7-invoke 'sh08-lsi-leaf? [(nodes :arg1) changed :member])))
          (is (= :rejected (:status (c7-invoke 'sh08-lsi-path [:arg1 :member :value env])))))
        (is (zero? @callbacks))))

    ;; Unknown normal re-entry cannot publish a carrier fact; exact slots still reject the known conflict.
    (let [bad-type {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword]}
          bad-transfer (assoc (first (:recur-transfers core)) :recur-core-node-id :pending-bad-transfer
                              :argument-node-ids [:next-again :pending-bad])
          pending-types (assoc types :call :gravity.type/unknown :pending-bad bad-type)
          pending (c7-invoke 'sh08-lsi-solve [(update core :recur-transfers conj bad-transfer) pending-types source])
          target (first (filter #(= "loop-target" (:target-id %)) (:recur-targets core)))
          exact (c7-invoke 'sh08-ft-loop-kernel-transfer-types
                  [bad-transfer target nodes pending-types [:gravity.type/bool empty-type] (:invariants pending)])
          diagnostic (first (:issues exact))]
      (is (= :accepted (:status pending)))
      (is (empty? (:invariants pending)))
      (is (= :rejected (:status exact)))
      (is (= :loop-recur-slot-type-mismatch (:reason diagnostic)))
      (is (= empty-type (:expected diagnostic)))
      (is (= bad-type (:actual diagnostic)))
      (is (= :value (:parameter-binding-id diagnostic))))
))

(deftest symbol-set-loop-effect-invariant-development-kernel
  (let [{:keys [core types nodes forms bindings carrier rows context]} (symbol-set-loop-data)
        facts {:module (:module core) :type-table types :constraint-ledger rows :function-type-table []
               :append-context {:forms forms :bindings bindings}}
        checked (effects 'body-loop-joins core facts 65536)]
    (is (= :accepted (:status checked)) (pr-str checked))
    (is (= rows (:rows checked)))
    (is (= checked (effects 'body-loop-joins core facts (:work-count checked))))
    (is (= :body-deferred-work-limit (:reason (effects 'body-loop-joins core facts (dec (:work-count checked))))))
    (doseq [mutate [#(assoc-in % [:type-table :operator :version] :sh08-symbol-set-insertion-v1)
                    #(assoc-in % [:constraint-ledger 1 :producer-rule] :sh08-loop-target-types-v1)
                    #(assoc-in % [:constraint-ledger 1 :constraint-id-request :domain] :gravity/sh08-loop-type-constraint-v1)
                    #(assoc-in % [:constraint-ledger 1 :dependencies :loop-premise :member-invariant :initializer-type] carrier)
                    #(assoc-in % [:constraint-ledger 1 :dependencies :loop-premise :member-invariant :grounding :member-path] [:other])
                    #(update-in % [:constraint-ledger 1 :dependencies :loop-premise :member-invariant :entries] pop)
                    #(update-in % [:constraint-ledger 1 :dependencies :loop-premise :member-invariant :entries] into
                        (get-in % [:constraint-ledger 1 :dependencies :loop-premise :member-invariant :entries]))
                    #(assoc-in % [:constraint-ledger 2 :dependencies :loop-premise :member-invariant-bindings] [])
                    #(assoc-in % [:type-table :arg0] {:kind :gravity.type/set :shape :literal :element-types []})]]
      (is (= :rejected (:status (effects 'body-loop-joins core (mutate facts) 65536)))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "loop proof hash")))
                                    (equals [_] (swap! callbacks inc) (throw (AssertionError. "loop proof equality")))
                                    (toString [] (swap! callbacks inc) (throw (AssertionError. "loop proof printing"))))]
      (doseq [bad [opaque [opaque] {:opaque opaque}]]
        (is (= :rejected (:status (effects 'body-loop-joins core
          (assoc-in facts [:constraint-ledger 1 :dependencies :loop-premise :member-invariant :grounding :member-path] bad) 65536))))
        (is (zero? @callbacks))))
    (let [ids (zipmap (concat (keys nodes) [:again :value "loop-target" "function-target"])
                     (map #(str "sha256:" (format "%064x" %)) (range 3000 3100)))
          rename (fn rename [field value]
                   (cond
                     (map? value) (into {} (map (fn [[key item]]
                       [(if (contains? #{:nodes :types :forms :bindings :calls :edges :call-order :call-facts :loop-rows
                                         :loop-binding-index :targets :target-owners :transfers :parameter-types :locals} field)
                          (walk/postwalk-replace ids key) key)
                        (rename key item)]) value))
                     (vector? value) (mapv #(rename nil %) value)
                     (seq? value) (apply list (map #(rename nil %) value))
                     (set? value) (set (map #(rename nil %) value))
                     (contains? #{:core-form :kind :position :literal-kind :value :quoted-value :name} field) value
                     :else (get ids value value)))
          [converted converted-context] (rename nil [core context])
          converted (assoc converted :identity-preimage (select-keys converted [:loop-bindings :recur-targets :recur-transfers]))
          source-facts {:module (:module converted) :type-table (:types converted-context)
                        :constraint-ledger (:ledger converted-context) :function-type-table []
                        :call-type-facts (vec (vals (:call-facts converted-context)))}
          request {:canonical-core-artifact converted :b47-context {:authenticated-core-request {:forms (vec (vals (:forms converted-context)))}}}
          prepared (effects 'body-va-context request source-facts (:bindings converted-context) 65536)
          source-facts (assoc source-facts :append-context (:context prepared))
          graph (effects 'body-derive-loop-nodes converted source-facts {} 65536)
          transfer (ids :transfer)]
      (is (= :accepted (:status prepared) (:status graph)) (pr-str graph))
      (is (= [(ids :next-again) (ids :call) (ids :branch)] (get-in graph [:nodes transfer :dependencies])))
      (is (= #{:memory/allocate} (get-in graph [:nodes transfer :transitive])))
      (is (= [(ids :branch)] (get-in graph [:nodes transfer :loop-backedge :body-core-node-ids])))
      (is (true? (effects 'body-graph-equations-valid? converted source-facts {} (:nodes graph)))))
    (let [alias (symbol-set-loop-alias-data)
          alias-facts {:module (get-in alias [:core :module]) :type-table (:types alias) :constraint-ledger (:rows alias)
                       :function-type-table [] :append-context {:forms (:forms alias) :bindings (:bindings alias)}}
          result (effects 'body-loop-joins (:core alias) alias-facts 65536)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= (:rows alias) (:rows result))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "quote child hash")))
                                    (equals [_] (swap! callbacks inc) (throw (AssertionError. "quote child equality")))
                                    (toString [] (swap! callbacks inc) (throw (AssertionError. "quote child printing"))))]
      (doseq [bad [opaque [opaque] {:opaque opaque}] coordinate [0 1]]
        (let [changed (assoc-in forms [:arg1 :child-form-ids coordinate] bad)
              env {:nodes nodes :forms changed :bindings bindings :types types :aliases {}}]
          (is (false? (effects 'body-lsi-leaf? (nodes :arg1) changed :member)))
          (doseq [quota [0 65536]] (is (= :rejected (:status (effects 'body-lsi-path :arg1 :member :value env quota))))))
        (is (zero? @callbacks))))
))

(defn- unquoted-symbol-set-loop-data []
  ;; C6 sh07-lower-literal emits an evaluated Set with these three attributes.
  (let [{:keys [core nodes forms types bindings context] :as data} (symbol-set-loop-alias-data)
        forms (-> forms (assoc-in [:value-init-form :kind] :set)
                        (assoc-in [:value-init-form :value] #{})
                        (assoc-in [:value-init-form :child-form-ids] []))
        initializer (-> (nodes :value-init) (assoc :core-form :collection-literal
                        :attributes {:literal-kind :set :element-count 0 :source-value #{}}
                        :evaluation {:kind :left-to-right :owner-function-syntax-id nil :order []}))
        nodes (assoc nodes :value-init initializer)
        core (assoc core :nodes (mapv #(nodes (:node-id %)) (:nodes core)))
        working (assoc core :loop-member-source {:nodes nodes :forms forms :bindings bindings})
        checked (c7-invoke 'sh08-ft-loop-type-step [working types {}])
        rows (c7-invoke 'sh08-ft-loop-constraints [(:constraints checked) nodes (:module core)])
        quotes (c7-invoke 'sh08-qd-products [(:nodes core) (vec (vals forms)) (:module core)])
        ledger (into (into rows (:constraints quotes))
                     [(c7-invoke 'sh08-pl-constraint [(:descriptor (:derived data))])])
        context (assoc context :core core :nodes nodes :forms forms :ledger ledger
                               :loop-rows (into {} (map (fn [row] [[(get-in row [:dependencies :loop-premise :kind]) (:lhs row)] row]) rows)))]
    (assoc data :core core :working working :nodes nodes :forms forms :checked checked :rows rows :quotes quotes :context context)))

(deftest unquoted-empty-symbol-set-loop-development-kernel
  (let [{:keys [core working nodes forms types bindings checked rows empty-type carrier]} (unquoted-symbol-set-loop-data)
        node (nodes :value-init)
        invoke #(c7-invoke 'sh08-ft-loop-type-step [% types {}])]
    (is (= empty-type (c7-invoke 'sh08-ft-infer-node-nominal [node {} {} {} [] {}])))
    (is (true? (c7-invoke 'sh08-lsi-leaf? [node forms :initializer])))
    (is (true? (c7-invoke 'sh08-lsi-leaf? [node forms :receiver])))
    (is (false? (c7-invoke 'sh08-lsi-leaf? [node forms :member])))
    (is (= :accepted (:status checked)) (pr-str checked))
    (is (= empty-type (get-in checked [:member-invariants :value :initializer-type])))
    (is (= carrier (get-in checked [:bindings :value])))
    (is (= [:member-use :arg1] (get-in checked [:member-invariants :value :grounding :member-path])))
    (is (= [:sh08-loop-target-types-v1 :sh08-loop-target-types-v2 :sh08-loop-target-types-v2] (mapv :producer-rule rows)))
    (doseq [mutate [#(assoc-in % [:attributes :source-value] #{:bad})
                    #(assoc-in % [:attributes :element-count] 1)
                    #(update % :attributes dissoc :source-value)
                    #(assoc-in % [:attributes :extra] true)
                    #(assoc-in % [:evaluation :kind] :no-evaluation)
                    #(assoc-in % [:evaluation :order] [{:index 0 :core-node-id :other}])
                    #(assoc % :children [:other])
                    #(assoc % :resolved-binding-ids [:value])]]
      (is (false? (c7-invoke 'sh08-lsi-leaf? [(mutate node) forms :initializer]))))
    (doseq [mutate [#(assoc-in % [:value-init-form :kind] :vector)
                    #(assoc-in % [:value-init-form :value] #{:bad})
                    #(assoc-in % [:value-init-form :child-form-ids] [:other])
                    #(assoc-in % [:value-init-form :syntax-id] :other)]]
      (is (false? (c7-invoke 'sh08-lsi-leaf? [node (mutate forms) :initializer]))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "empty Set source hash")))
                                    (equals [_] (swap! callbacks inc) (throw (AssertionError. "empty Set source equality")))
                                    (toString [] (swap! callbacks inc) (throw (AssertionError. "empty Set source printing"))))]
      (doseq [bad [opaque [opaque] {:opaque opaque}]
              [changed source] [[(assoc-in node [:source :form-id] bad) forms]
                                [(assoc-in node [:source :syntax-id] bad) forms]
                                [(assoc-in node [:attributes :element-count] bad) forms]
                                [(assoc-in node [:attributes :source-value] bad) forms]
                                [node (assoc-in forms [:value-init-form :value] bad)]
                                [node (assoc-in forms [:value-init-form :syntax-id] bad)]]]
        (let [env {:nodes {:value-init changed} :forms source :types {:value-init empty-type} :aliases {}}]
          (is (false? (c7-invoke 'sh08-lsi-empty-constructor? [changed source])))
          (is (= :rejected (:status (c7-invoke 'sh08-lsi-path [:value-init :initializer :value env]))))
          (is (zero? @callbacks)))))
))

(deftest unquoted-empty-symbol-set-loop-effects-development-kernel
  (let [{:keys [core nodes forms types bindings context checked rows empty-type]} (unquoted-symbol-set-loop-data)
        node (nodes :value-init)
        facts {:module (:module core) :type-table types :constraint-ledger rows :function-type-table []
               :append-context {:forms forms :bindings bindings}}
        joined (effects 'body-loop-joins core facts 65536)]
    (is (= :accepted (:status checked) (:status joined)) (pr-str joined))
    (is (= rows (:rows joined)))
    (when (= :accepted (:status joined))
      (is (= joined (effects 'body-loop-joins core facts (:work-count joined))))
      (is (= :body-deferred-work-limit (:reason (effects 'body-loop-joins core facts (dec (:work-count joined)))))))
    (is (true? (effects 'body-lsi-empty-constructor? node forms)))
    (doseq [changed [(assoc-in facts [:append-context :forms :value-init-form :value] #{:bad})
                    (assoc-in facts [:constraint-ledger 1 :producer-rule] :sh08-loop-target-types-v1)]]
      (is (= :rejected (:status (effects 'body-loop-joins core changed 65536)))))
    (let [ids (zipmap (concat (keys nodes) [:again :value "loop-target" "function-target"])
                     (map #(str "sha256:" (format "%064x" %)) (range 4000 4100)))
          rename (fn rename [field value]
                   (cond
                     (map? value) (into {} (map (fn [[key item]]
                       [(if (contains? #{:nodes :types :forms :bindings :calls :edges :call-order :call-facts :loop-rows
                                         :loop-binding-index :targets :target-owners :transfers :parameter-types :locals} field)
                          (walk/postwalk-replace ids key) key)
                        (rename key item)]) value))
                     (vector? value) (mapv #(rename nil %) value)
                     (seq? value) (apply list (map #(rename nil %) value))
                     (set? value) (set (map #(rename nil %) value))
                     (contains? #{:core-form :kind :position :literal-kind :value :quoted-value :name} field) value
                     :else (get ids value value)))
          [converted converted-context] (rename nil [core context])
          converted (assoc converted :identity-preimage (select-keys converted [:loop-bindings :recur-targets :recur-transfers]))
          source-facts {:module (:module converted) :type-table (:types converted-context)
                        :constraint-ledger (:ledger converted-context) :function-type-table []
                        :call-type-facts (vec (vals (:call-facts converted-context)))}
          request {:canonical-core-artifact converted :b47-context {:authenticated-core-request {:forms (vec (vals (:forms converted-context)))}}}
          prepared (effects 'body-va-context request source-facts (:bindings converted-context) 65536)
          source-facts (assoc source-facts :append-context (:context prepared))
          graph (effects 'body-derive-loop-nodes converted source-facts {} 65536)]
      (is (= :accepted (:status prepared) (:status graph)) (pr-str graph))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :value-init) :direct])))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :transfer) :transitive])))
      (is (true? (effects 'body-graph-equations-valid? converted source-facts {} (:nodes graph)))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                    (hashCode [] (swap! callbacks inc) (throw (AssertionError. "empty Set source hash")))
                                    (equals [_] (swap! callbacks inc) (throw (AssertionError. "empty Set source equality")))
                                    (toString [] (swap! callbacks inc) (throw (AssertionError. "empty Set source printing"))))]
      (doseq [bad [opaque [opaque] {:opaque opaque}]
              [changed source] [[(assoc-in node [:source :form-id] bad) forms]
                                [(assoc-in node [:source :syntax-id] bad) forms]
                                [(assoc-in node [:attributes :element-count] bad) forms]
                                [(assoc-in node [:attributes :source-value] bad) forms]
                                [node (assoc-in forms [:value-init-form :value] bad)]
                                [node (assoc-in forms [:value-init-form :syntax-id] bad)]]]
        (let [env {:nodes {:value-init changed} :forms source :types {:value-init empty-type} :aliases {}}]
          (is (false? (effects 'body-lsi-empty-constructor? changed source)))
          (doseq [quota [15 16]] (is (= :rejected (:status (effects 'body-lsi-path :value-init :initializer :value env quota)))))
          (is (zero? @callbacks)))))
))

(defn- vector-first-symbol-loop-data
  ([] (vector-first-symbol-loop-data []))
  ([tails]
  ;; Source-linked normalized records exercise the current query before loop grounding.
  (let [{:keys [core types nodes forms bindings context empty-type] :as data} (symbol-set-loop-data)
        query-input ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'list-query-input)
                     'first [{:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/symbol]}])
        binding (assoc (get-in query-input [:bindings :ordering-binding]) :binding-id :first-binding
                                                                             :definition-syntax-id :first-definition)
        tail-ids (mapv #(keyword (str "vector-tail-" %)) (range (count tails)))
        raw-values (into [(get-in forms [:arg1 :value])] (map :value tails))
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin []})
        form (fn [id kind value parent children]
               {:form-id id :syntax-id id :kind kind :value value :parent-form-id parent :child-form-ids children
                :scope-id :value-scope :source-span nil :origin-chain [] :generated-origin []})
        vector-children (into [:arg1] tail-ids)
        forms (-> forms (assoc-in [:value-init-form :kind] :set) (assoc-in [:value-init-form :value] #{})
                        (assoc-in [:value-init-form :child-form-ids] [])
                        (assoc-in [:arg1 :parent-form-id] :symbol-vector-form)
                        (assoc-in [:call :child-form-ids] [:operator :arg0 :vector-first-form])
                        (assoc :first-operator-form (form :first-operator-form :symbol 'first :vector-first-form [])
                               :symbol-vector-form (form :symbol-vector-form :vector raw-values :vector-first-form vector-children)
                               :vector-first-form (form :vector-first-form :list (list 'first raw-values) :call
                                                        [:first-operator-form :symbol-vector-form])))
        forms (into forms (map (fn [id spec] [id (form id (:kind spec) (:value spec) :symbol-vector-form [])]) tail-ids tails))
        tail-nodes (mapv (fn [id spec]
                          {:node-id id :core-form :literal :children [] :resolved-binding-ids [] :binding-context :value-scope
                           :source (source id) :attributes {:literal-kind (:kind spec) :value (:value spec)}
                           :evaluation {:kind :value :owner-function-syntax-id nil :order []}}) tail-ids tails)
        first-children [:first-operator :symbol-vector]
        first-node {:node-id :vector-first :core-form :call :children first-children :resolved-binding-ids [:first-binding]
                    :binding-context :value-scope :source (source :vector-first-form)
                    :attributes {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1]
                                 :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
                    :evaluation {:kind :operator-then-arguments :owner-function-syntax-id nil
                                 :order [{:index 0 :core-node-id :first-operator} {:index 1 :core-node-id :symbol-vector}]}}
        first-operator {:node-id :first-operator :core-form :reference :children [] :resolved-binding-ids [:first-binding]
                        :binding-context :value-scope :source (source :first-operator-form)
                        :attributes {:symbol 'first :position :operator :binding-id :first-binding :binding-class :core
                                     :definition-syntax-id :first-definition}
                        :evaluation {:kind :value :owner-function-syntax-id nil :order []}}
        vector-node {:node-id :symbol-vector :core-form :collection-literal :children vector-children :resolved-binding-ids []
                     :binding-context :value-scope :source (source :symbol-vector-form)
                     :attributes {:literal-kind :vector :element-count (count vector-children) :source-value raw-values}
                     :evaluation {:kind :left-to-right :owner-function-syntax-id nil
                                  :order (mapv (fn [i id] {:index i :core-node-id id}) (range) vector-children)}}
        initializer (assoc (nodes :value-init) :core-form :collection-literal
                           :attributes {:literal-kind :set :element-count 0 :source-value #{}}
                           :evaluation {:kind :left-to-right :owner-function-syntax-id nil :order []})
        nodes (-> nodes (assoc :first-operator first-operator :symbol-vector vector-node :vector-first first-node :value-init initializer)
                        (assoc-in [:call :children] [:operator :arg0 :vector-first])
                        (assoc-in [:call :evaluation :order] [{:index 0 :core-node-id :operator}
                          {:index 1 :core-node-id :arg0} {:index 2 :core-node-id :vector-first}]))
        nodes (into nodes (map (juxt :node-id identity) tail-nodes))
        first-call {:core-node-id :vector-first :operator-node-id :first-operator :operator-binding-id :first-binding
                    :argument-node-ids [:symbol-vector] :ordered-evaluation-node-ids first-children
                    :evaluation-order :operator-then-arguments :result-policy :call-result}
        first-edge {:ordinal 0 :call-core-node-id :vector-first :caller-function-syntax-id nil :caller-function-core-node-id nil
                    :callee-binding-id :first-binding :callee-definition-syntax-id :first-definition
                    :callee-function-syntax-id nil :callee-function-core-node-id nil :argument-core-node-ids [:symbol-vector]
                    :ordered-evaluation-node-ids first-children :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}
        insertion-call (assoc (first (:calls core)) :argument-node-ids [:arg0 :vector-first]
                                                     :ordered-evaluation-node-ids [:operator :arg0 :vector-first])
        insertion-edge (assoc (first (:call-edges core)) :ordinal 1 :argument-core-node-ids [:arg0 :vector-first]
                                                       :ordered-evaluation-node-ids [:operator :arg0 :vector-first])
        bindings (assoc bindings :first-binding binding)
        ids (into (into [:again-init :value-init :condition :next-again :operator :arg0 :first-operator :arg1] tail-ids)
                  [:symbol-vector :vector-first :call :transfer :result :branch :loop-node :fn-node])
        core (assoc core :nodes (mapv nodes ids) :calls [first-call insertion-call] :call-edges [first-edge insertion-edge]
                         :binding-table (vec (vals bindings)))
        vector-type {:kind :gravity.type/vector :shape :literal :element-types (into [:gravity.type/symbol] (map :type tails))}
        types (into (assoc types :symbol-vector vector-type :vector-first :gravity.type/symbol)
                    (map (fn [id spec] [id (:type spec)]) tail-ids tails))
        first-derived (c7-invoke 'sh08-lq-derive [core first-node first-call first-edge nodes bindings types forms ])
        first-descriptor (:descriptor first-derived) types (assoc types :first-operator first-descriptor)
        insertion (c7-invoke 'sh08-pl-derive [core (nodes :call) insertion-call insertion-edge nodes bindings types forms])
        descriptor (:descriptor insertion) types (assoc types :operator descriptor)
        working (assoc core :loop-member-source {:nodes nodes :forms forms :bindings bindings})
        checked (c7-invoke 'sh08-ft-loop-type-step [working types {}])
        rows (c7-invoke 'sh08-ft-loop-constraints [(:constraints checked) nodes (:module core)])
        quotes (c7-invoke 'sh08-qd-products [(:nodes core) (vec (vals forms)) (:module core)])
        ledger (into (into rows (:constraints quotes))
                     [(c7-invoke 'sh08-lq-constraint [first-descriptor]) (c7-invoke 'sh08-pl-constraint [descriptor])])
        context (assoc context :core core :nodes nodes :forms forms :bindings bindings :types types
                               :calls {:vector-first first-call :call insertion-call} :edges {:vector-first first-edge :call insertion-edge}
                               :call-order {:vector-first 0 :call 1}
                               :call-facts {:vector-first (c7-invoke 'sh08-lq-call-fact [first-descriptor (:module core)])
                                            :call (c7-invoke 'sh08-pl-call-fact [descriptor (:module core)])}
                               :ledger ledger :loop-rows (into {} (map (fn [row] [[(get-in row [:dependencies :loop-premise :kind]) (:lhs row)] row]) rows)))]
    (assoc data :core core :working working :nodes nodes :forms forms :bindings bindings :types types
                :first-derived first-derived :derived insertion :checked checked :rows rows :quotes quotes :context context))))

(deftest vector-first-symbol-loop-witness-development-kernel
  (let [{:keys [working core nodes forms types checked rows first-derived derived empty-type carrier]} (vector-first-symbol-loop-data)]
    (is (= :accepted (:status first-derived) (:status derived)))
    (is (= :gravity.type/symbol (:type first-derived)))
    (is (= :sh08-vector-first-v1 (get-in first-derived [:descriptor :version])))
    (is (= :gravity.type/symbol (get-in first-derived [:descriptor :bounds-proof :selected-type])))
    (is (= 0 (get-in first-derived [:descriptor :bounds-proof :selected-index])))
    (is (= :accepted (:status checked)) (pr-str checked))
    (is (= carrier (get-in checked [:bindings :value])))
    (is (= empty-type (get-in checked [:member-invariants :value :initializer-type])))
    (is (= [:vector-first :symbol-vector :arg1] (get-in checked [:member-invariants :value :grounding :member-path])))
    (is (= [:sh08-loop-target-types-v1 :sh08-loop-target-types-v2 :sh08-loop-target-types-v2] (mapv :producer-rule rows)))))

(defn- vector-first-symbol-loop-env [{:keys [core nodes forms bindings types]}]
  {:core core :nodes nodes :forms forms :bindings bindings :types types
   :aliases (into {} (map (juxt :binding-id identity) (:lexical-bindings core)))
   :calls (into {} (map (juxt :core-node-id identity) (:calls core)))
   :edges (into {} (map (juxt :call-core-node-id identity) (:call-edges core)))})

(defn- vector-first-symbol-loop-alias-env [data]
  (let [env (vector-first-symbol-loop-env data)
        reference (fn [id bid] {:node-id id :core-form :reference :children [] :binding-context :value-scope
                                :resolved-binding-ids [bid] :attributes {:binding-id bid}
                                :source {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin []}
                                :evaluation {:kind :value :owner-function-syntax-id nil :order []}})
        form (fn [id value parent] {:form-id id :syntax-id id :kind :symbol :value value :scope-id :value-scope
                                    :parent-form-id parent :child-form-ids [] :source-span nil :origin-chain [] :generated-origin []})
        record (fn [bid initializer form-id] {:binding-id bid :mutability :immutable :initializer-node-id initializer
                                             :initializer-form-id form-id :initializer-syntax-id form-id
                                             :definition-syntax-id (keyword (str (name bid) "-name")) :binding-scope-id :value-scope})
        binding (fn [bid] {:binding-id bid :name (symbol (name bid)) :kind :local :binding-class :lexical
                          :scope-id :value-scope :definition-syntax-id (keyword (str (name bid) "-name"))})]
    (-> env
        (assoc-in [:nodes :head-use] (reference :head-use :head))
        (assoc-in [:nodes :member-use] (reference :member-use :member))
        (assoc-in [:forms :head-use] (form :head-use 'head :symbol-vector-form))
        (assoc-in [:forms :member-use] (form :member-use 'member :call))
        (assoc-in [:forms :arg1 :parent-form-id] :head-initializer)
        (assoc-in [:forms :symbol-vector-form :child-form-ids] [:head-use])
        (assoc-in [:forms :symbol-vector-form :value] ['head])
        (assoc-in [:forms :vector-first-form :value] (list 'first ['head]))
        (assoc-in [:nodes :symbol-vector :children] [:head-use])
        (assoc-in [:nodes :symbol-vector :attributes :source-value] ['head])
        (assoc-in [:nodes :symbol-vector :evaluation :order] [{:index 0 :core-node-id :head-use}])
        (assoc-in [:types :head-use] :gravity.type/symbol)
        (assoc-in [:types :member-use] :gravity.type/symbol)
        (assoc-in [:bindings :head] (binding :head))
        (assoc-in [:bindings :member] (binding :member))
        (assoc-in [:aliases :head] (record :head :arg1 :arg1))
        (assoc-in [:aliases :member] (record :member :vector-first :vector-first-form)))))

(deftest vector-first-symbol-loop-witness-controls-development-kernel
  (let [data (vector-first-symbol-loop-data) env (vector-first-symbol-loop-env data)
        walk (fn [changed _budget] (c7-invoke 'sh08-lsi-path [:vector-first :member :value changed]))
        accepted (walk env 65536)
        alias-env (vector-first-symbol-loop-alias-env data)]
    (is (= :accepted (:status accepted)) (pr-str accepted))
    (is (= [:vector-first :symbol-vector :arg1] (:path accepted)))
    (let [result (c7-invoke 'sh08-lsi-path [:member-use :member :value alias-env])]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= [:member-use :vector-first :symbol-vector :head-use :arg1] (:path result))))
    (doseq [tails [[{:kind :boolean :value false :type :gravity.type/bool}]
                   [{:kind :keyword :value :tail :type :gravity.type/keyword}
                    {:kind :nil :value nil :type :gravity.type/nil}
                    {:kind :string :value "tail" :type :gravity.type/string}
                    {:kind :integer :value 17 :type :gravity.type/integer}]]]
      (let [mixed (vector-first-symbol-loop-data tails)]
        (is (= :accepted (get-in mixed [:checked :status])) (pr-str (:checked mixed)))
        (is (= :gravity.type/symbol (get-in mixed [:first-derived :type])))
        (is (= (:empty-type mixed) (get-in mixed [:checked :member-invariants :value :initializer-type])))))
    (let [member 'library/member q (list 'quote member)
          changed (-> env (assoc-in [:nodes :arg1 :attributes :quoted-value] member)
                      (assoc-in [:forms :arg1 :value] q)
                      (assoc-in [:forms (get-in env [:nodes :arg1 :attributes :quoted-form-id]) :value] member)
                      (assoc-in [:nodes :symbol-vector :attributes :source-value] [q])
                      (assoc-in [:forms :symbol-vector-form :value] [q])
                      (assoc-in [:forms :vector-first-form :value] (list 'first [q])))]
      (is (= :accepted (:status (walk changed 65536)))))
    (doseq [mutate [#(assoc-in % [:nodes :symbol-vector :core-form] :quote)
                    #(assoc-in % [:nodes :symbol-vector :core-form] :reference)
                    #(assoc-in % [:nodes :symbol-vector :core-form] :call)
                    #(assoc-in % [:nodes :symbol-vector :attributes :extra] true)
                    #(assoc-in % [:nodes :symbol-vector :attributes :element-count] 0)
                    #(assoc-in % [:nodes :symbol-vector :attributes :source-value] [])
                    #(assoc-in % [:nodes :symbol-vector :children] [])
                    #(assoc-in % [:nodes :symbol-vector :evaluation :kind] :no-evaluation)
                    #(assoc-in % [:nodes :symbol-vector :evaluation :order] [])
                    #(assoc-in % [:forms :symbol-vector-form :value] ['wrong])
                    #(assoc-in % [:forms :symbol-vector-form :child-form-ids] [:other])
                    #(assoc-in % [:types :first-operator :application] :not-invoked)
                    #(assoc-in % [:types :first-operator :version] :sh08-list-query-v1)
                    #(assoc-in % [:types :first-operator :bounds-proof :selected-index] 1)
                    #(assoc-in % [:types :first-operator :bounds-proof :selected-type] :gravity.type/keyword)
                    #(assoc-in % [:types :first-operator :bounds-proof :vector-length] 0)
                    #(assoc-in % [:types :first-operator :source-call :argument-node-ids] [:other])
                    #(assoc-in % [:types :first-operator :source-binding :namespace] 'foreign.core)
                    #(assoc-in % [:calls :vector-first :argument-node-ids] [:other])
                    #(assoc-in % [:calls :vector-first :argument-node-ids] (vec (repeat 1024 :symbol-vector)))
                    #(assoc-in % [:calls :vector-first :ordered-evaluation-node-ids] (vec (repeat 1024 :first-operator)))
                    #(assoc-in % [:core :module :profile] [:meta])
                    #(assoc-in % [:core :module :target] [:jvm])
                    #(assoc-in % [:edges :vector-first :callee-binding-id] :other)
                    #(assoc-in % [:nodes :arg1 :core-form] :call)
                    #(assoc-in % [:types :arg1] :gravity.type/unknown)
                    #(assoc-in % [:types :arg1] :gravity.type/keyword)
                    #(assoc-in % [:nodes :arg1 :attributes :quoted-value] :wrong)]]
      (is (= :rejected (:status (walk (mutate env) 65536)))))
    (doseq [bad [:gravity.type/unknown :gravity.type/never]]
      (let [mixed (vector-first-symbol-loop-data [{:kind :nil :value nil :type :gravity.type/nil}])
            changed (-> (vector-first-symbol-loop-env mixed)
                        (assoc-in [:types :vector-tail-0] bad)
                        (assoc-in [:types :symbol-vector :element-types 1] bad))]
        (is (= :rejected (:status (walk changed 65536))))))
    (let [mixed (vector-first-symbol-loop-data [{:kind :symbol :value 'tail :type :gravity.type/symbol}])
          changed (-> (vector-first-symbol-loop-env mixed)
                      (assoc-in [:types :arg1] :gravity.type/keyword)
                      (assoc-in [:types :symbol-vector :element-types 0] :gravity.type/keyword))]
      (is (= :rejected (:status (walk changed 65536))))
      (is (= :rejected (:status (c7-invoke 'sh08-lsi-path [:vector-first :literal-head :value env])))))
    (doseq [changed [(assoc-in alias-env [:aliases :head :mutability] :mutable)
                      (assoc-in alias-env [:nodes :head-use :binding-context] :other)
                      (assoc-in alias-env [:forms :head-use :syntax-id] :other)
                      (assoc-in alias-env [:nodes :arg1 :core-form] :call)
                      (update-in alias-env [:aliases :head] assoc :initializer-node-id :member-use
                                 :initializer-form-id :member-use :initializer-syntax-id :member-use)
                      (update-in alias-env [:aliases :head] assoc :initializer-node-id :head-use
                                 :initializer-form-id :head-use :initializer-syntax-id :head-use)]]
      (is (= :rejected (:status (c7-invoke 'sh08-lsi-path [:member-use :member :value changed])))))
    (let [extra (assoc (first (get-in data [:core :call-edges])) :ordinal 2)]
      (is (= :rejected (:status (c7-invoke 'sh08-lsi-solve
        [(update (:core data) :call-edges conj extra) (:types data)
         (select-keys data [:nodes :forms :bindings])])))))
    (let [callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "first witness hash")))
                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "first witness equality")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "first witness printing"))))
          lazy-value (lazy-seq (swap! callbacks inc) (list 'opaque))]
      (doseq [bad [opaque [opaque] {:opaque opaque}]
              mutate [#(assoc-in % [:nodes :vector-first :source :form-id] bad)
                      #(assoc-in % [:nodes :vector-first :source :syntax-id] bad)
                      #(assoc-in % [:nodes :vector-first :children 1] bad)
                      #(assoc-in % [:forms :vector-first-form :child-form-ids 1] bad)
                      #(assoc-in % [:nodes :symbol-vector :children 0] bad)
                      #(assoc-in % [:forms :symbol-vector-form :child-form-ids 0] bad)
                      #(assoc-in % [:nodes :symbol-vector :attributes :source-value] bad)
                      #(assoc-in % [:forms :symbol-vector-form :value] bad)
                      #(assoc-in % [:types :first-operator :bounds-proof :operand-node-id] bad)]]
        (is (= :rejected (:status (walk (mutate env) nil))))
        (is (zero? @callbacks)))
      (is (= :rejected (:status (c7-invoke 'sh08-lsi-first-data [lazy-value]))))
      (is (zero? @callbacks))
      (let [wide (vec (repeat 1024 opaque))]
        (is (= :rejected (:status (c7-invoke 'sh08-lsi-first-data [wide]))))
        (is (zero? @callbacks)))
      (doseq [mutate [#(assoc-in % [:nodes :head-use :source :form-id] opaque)
                      #(assoc-in % [:nodes :head-use :source :syntax-id] opaque)
                      #(assoc-in % [:nodes :head-use :binding-context] opaque)
                      #(assoc-in % [:forms :head-use :scope-id] opaque)
                      #(assoc-in % [:aliases :head :initializer-node-id] opaque)]]
        (is (= :rejected (:status (c7-invoke 'sh08-lsi-path [:member-use :member :value (mutate alias-env)]))))
        (is (zero? @callbacks))))
))

(deftest vector-first-symbol-loop-witness-effects-development-kernel
  (let [{:keys [core types nodes forms bindings checked rows context]} (vector-first-symbol-loop-data)
        facts {:module (:module core) :type-table types :constraint-ledger rows :function-type-table []
               :append-context {:forms forms :bindings bindings}}
        joined (effects 'body-loop-joins core facts 65536)
        env (vector-first-symbol-loop-env {:core core :types types :nodes nodes :forms forms :bindings bindings})
        path (effects 'body-lsi-path :vector-first :member :value env 65536)]
    (is (= :accepted (:status checked) (:status joined) (:status path)) (pr-str joined))
    (is (= rows (:rows joined)))
    (is (= [:vector-first :symbol-vector :arg1] (:path path)))
    (when (= :accepted (:status joined))
      (is (= joined (effects 'body-loop-joins core facts (:work-count joined))))
      (is (= :body-deferred-work-limit (:reason (effects 'body-loop-joins core facts (dec (:work-count joined)))))))
    (doseq [changed [(assoc-in facts [:type-table :first-operator :bounds-proof :selected-index] 1)
                    (assoc-in facts [:type-table :first-operator :application] :not-invoked)
                    (assoc-in facts [:append-context :forms :symbol-vector-form :value] ['wrong])
                    (assoc-in facts [:constraint-ledger 1 :solution :member-invariant :grounding :member-path] [:vector-first])
                    (assoc-in facts [:constraint-ledger 1 :producer-rule] :sh08-loop-target-types-v1)]]
      (is (= :rejected (:status (effects 'body-loop-joins core changed 65536)))))
    (is (= :rejected (:status (effects 'body-lsi-path :vector-first :member :value
                               (assoc-in env [:nodes :symbol-vector :evaluation :order] []) 65536))))
    (let [ids (zipmap (concat (keys nodes) [:again :value "loop-target" "function-target"])
                     (map #(str "sha256:" (format "%064x" %)) (range 6000 6100)))
          rename (fn rename [field value]
                   (cond
                     (map? value) (into {} (map (fn [[key item]]
                       [(if (contains? #{:nodes :types :forms :bindings :calls :edges :call-order :call-facts :loop-rows
                                         :loop-binding-index :targets :target-owners :transfers :parameter-types :locals} field)
                          (walk/postwalk-replace ids key) key)
                        (rename key item)]) value))
                     (vector? value) (mapv #(rename nil %) value)
                     (seq? value) (apply list (map #(rename nil %) value))
                     (set? value) (set (map #(rename nil %) value))
                     (contains? #{:core-form :kind :position :literal-kind :value :quoted-value :name :projection} field) value
                     :else (get ids value value)))
          [converted converted-context] (rename nil [core context])
          converted (assoc converted :identity-preimage (select-keys converted [:loop-bindings :recur-targets :recur-transfers]))
          source-facts {:module (:module converted) :type-table (:types converted-context)
                        :constraint-ledger (:ledger converted-context) :function-type-table []
                        :call-type-facts (vec (vals (:call-facts converted-context)))}
          request {:canonical-core-artifact converted :b47-context {:authenticated-core-request {:forms (vec (vals (:forms converted-context)))}}}
          prepared (effects 'body-va-context request source-facts (:bindings converted-context) 65536)
          queries (effects 'body-lq-context request source-facts (:bindings converted-context) 65536)
          source-facts (assoc source-facts :append-context (:context prepared) :list-query-context (:context queries))
          graph (effects 'body-derive-loop-nodes converted source-facts {} 65536)]
      (is (= :accepted (:status prepared) (:status queries) (:status graph)) (pr-str graph))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :value-init) :direct])))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :transfer) :transitive])))
      (is (= #{:memory/allocate} (get-in graph [:nodes (ids :symbol-vector) :direct])))
      (is (= #{} (get-in graph [:nodes (ids :vector-first) :direct])))
      (is (true? (effects 'body-graph-equations-valid? converted source-facts {} (:nodes graph)))))
))

(defn- vector-first-symbol-loop-unused-metadata-env [env value node?]
  ;; Exercise this private proof's readset, not whole-Core authentication.
  (if node?
    (update env :nodes #(into {} (map (fn [[id node]]
      [id (assoc node :artifact :gravity/sh07-core-node :generated? false
                      :module {:namespace 'self-hosting.sh08.vector-first-symbol-loop :profile :meta :target :jvm}
                      :metadata value :preserved-declarations {:effects [:memory/allocate] :capabilities [] :safety :safe}
                      :pending-fact-families [:types :effects :ownership :safety] :lowering-rule-version :sh07-b11-v12)]) %)))
    (update env :forms #(into {} (map (fn [[id form]] [id (assoc form :metadata value)]) %)))))

(deftest vector-first-symbol-loop-witness-reached-metadata-development-kernel
  (let [env (vector-first-symbol-loop-env (vector-first-symbol-loop-data))
        callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "unused metadata hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "unused metadata equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "unused metadata printing"))))
        large (vec (repeat 1024 {:unused (vec (range 20))}))
        walks [(fn [env _budget] (c7-invoke 'sh08-lsi-path [:vector-first :member :value env]))
               (fn [env budget] (effects 'body-lsi-path :vector-first :member :value env budget))]]
    (doseq [walk walks]
      (let [baseline (walk env 65536)]
        (is (= :accepted (:status baseline)))
        (doseq [value [large opaque] node? [true false]]
          (let [changed (vector-first-symbol-loop-unused-metadata-env env value node?)]
            (is (= baseline (walk changed 65536)))
            (is (zero? @callbacks))))
        (doseq [changed [(assoc-in env [:nodes :vector-first :source :semantic-span] opaque)
                        (assoc-in env [:forms :vector-first-form :source-span] opaque)
                        (assoc-in env [:nodes :symbol-vector :attributes :source-value] opaque)
                        (assoc-in env [:nodes :symbol-vector :evaluation :order] opaque)
                        (assoc-in env [:types :first-operator :source-binding :metadata] opaque)
                        (assoc-in env [:types :first-operator :extra] opaque)]]
          (is (= :rejected (:status (walk changed 65536))))
          (is (zero? @callbacks)))))))

(defn- vector-first-symbol-loop-cold-data []
  (let [base (dissoc (vector-first-symbol-loop-alias-env (vector-first-symbol-loop-data)) :types)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin []})
        form (fn [id kind value parent children] {:form-id id :syntax-id id :kind kind :value value :scope-id :value-scope
                    :parent-form-id parent :child-form-ids children :source-span nil :origin-chain [] :generated-origin []})
        node (fn [id kind children attrs] {:node-id id :core-form kind :children children :resolved-binding-ids []
                    :binding-context :value-scope :source (source (keyword (str (name id) "-form"))) :attributes attrs
                    :evaluation {:kind :left-to-right :owner-function-syntax-id "function-target"
                                 :order (mapv (fn [i n] {:index i :core-node-id n}) (range) children)}})
        reference (fn [id bid symbol]
                    (assoc (node id :reference [] {:binding-id bid}) :resolved-binding-ids [bid]))
        catalog (into {} (map (juxt :name identity) (bootstrap/sh06-resolution-core-records)))
        selected {:first-binding 'first :ordering-binding 'conj :contains-binding 'contains?}
        core-bindings (into {} (map (fn [[id name]] [id (assoc (catalog name) :binding-id id)]) selected))
        catalog-extra (into {} (for [[name record] catalog :when (not (contains? (set (vals selected)) name))
                                    :let [id (bootstrap/reader-canonical-hash {:domain :normalized-core-binding :name name})]]
                                [id (assoc record :binding-id id)]))
        bindings (merge (:bindings base) core-bindings catalog-extra
                        {:main-binding {:binding-id :main-binding :name 'main :kind :function :binding-class :namespace :namespace 'self-hosting.sh08.vector-first-symbol-loop
                                        :scope-id :namespace :definition-syntax-id :main-name}})
        bindings (-> bindings (assoc-in [:value :name] 'seen) (assoc-in [:again :name] 'pending))
        first-definition (:definition-syntax-id (bindings :first-binding))
        insertion-definition (:definition-syntax-id (bindings :ordering-binding))
        contains-definition (:definition-syntax-id (bindings :contains-binding))
        forms (-> (:forms base)
                  (assoc-in [:arg1 :value] '(quote library/member))
                  (assoc-in [(get-in base [:nodes :arg1 :attributes :quoted-form-id]) :value] 'library/member)
                  (assoc-in [:arg1 :parent-form-id] :outer-let-form)
                  (assoc-in [:branch-form :parent-form-id] :inner-let-form)
                  (assoc-in [:vector-first-form :parent-form-id] :inner-let-form)
                  (assoc-in [:loop-form :parent-form-id] :outer-let-form)
                  (assoc-in [:loop-form :child-form-ids] [:loop-op :binding-vector :inner-let-form])
                  (assoc-in [:branch-form :child-form-ids] [:if-op :condition-form :transfer-form :contains-call-form])
                  (assoc-in [:member-use :parent-form-id] :call)
                  (assoc-in [:result-form :parent-form-id] :contains-call-form)
                  (assoc-in [:call :child-form-ids] [:operator :arg0 :member-use])
                  (assoc :if-op (form :if-op :symbol 'if :branch-form [])
                         :outer-let-form (form :outer-let-form :list '(let [head (quote library/member)] :body) :fn-form [:arg1 :loop-form])
                         :inner-let-form (form :inner-let-form :list '(let [member (first [head])] :body) :loop-form [:vector-first-form :branch-form])
                         :contains-operator-form (form :contains-operator-form :symbol 'contains? :contains-call-form [])
                         :head-query-form (form :head-query-form :symbol 'head :contains-call-form [])
                         :contains-call-form (form :contains-call-form :list '(contains? seen head) :branch-form [:contains-operator-form :result-form :head-query-form])
                         :def-main-form (form :def-main-form :list '(defn main [] :body) nil [:fn-form])))
        forms (into {} (map (fn [[id f]] [id (if (symbol? (:value f))
                          (update f :value #(case % value 'seen again 'pending %)) f)]) forms))
        nodes (-> (:nodes base)
                  (assoc-in [:arg1 :attributes :quoted-value] 'library/member)
                  (assoc-in [:call :children] [:operator :arg0 :member-use])
                  (assoc-in [:call :evaluation :order] [{:index 0 :core-node-id :operator} {:index 1 :core-node-id :arg0} {:index 2 :core-node-id :member-use}])
                  (assoc-in [:operator :attributes :definition-syntax-id] insertion-definition)
                  (assoc-in [:first-operator :attributes :definition-syntax-id] first-definition)
                  (assoc-in [:branch :children] [:condition :transfer :contains-call])
                  (assoc-in [:loop-node :children] [:again-init :value-init :inner-let])
                  (assoc-in [:loop-node :evaluation :order] [{:index 0 :core-node-id :again-init} {:index 1 :core-node-id :value-init} {:index 2 :core-node-id :inner-let}])
                  (assoc-in [:fn-node :children] [:outer-let])
                  (assoc :outer-let (assoc (node :outer-let :let [:arg1 :loop-node] {}) :binding-context :function-scope)
                         :inner-let (node :inner-let :let [:vector-first :branch] {})
                         :head-query (reference :head-query :head 'head)
                         :contains-operator (assoc (reference :contains-operator :contains-binding 'contains?)
                           :attributes {:symbol 'contains? :position :operator :binding-id :contains-binding
                                        :binding-class :core :definition-syntax-id contains-definition})
                         :contains-call (assoc (node :contains-call :call [:contains-operator :result :head-query]
                           {:operator-child-index 0 :argument-count 2 :argument-child-indexes [1 2]
                            :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call})
                           :resolved-binding-ids [:contains-binding]
                           :evaluation {:kind :operator-then-arguments :owner-function-syntax-id "function-target"
                                        :order [{:index 0 :core-node-id :contains-operator} {:index 1 :core-node-id :result} {:index 2 :core-node-id :head-query}]})
                         :def-main (node :def-main :def [:fn-node] {})))
        nodes (into {} (map (fn [[id n]] [id (if (= :fn (:core-form n)) n
                          (assoc-in n [:evaluation :owner-function-syntax-id] "function-target"))]) nodes))
        calls [(get-in base [:calls :vector-first])
               (assoc (get-in base [:calls :call]) :argument-node-ids [:arg0 :member-use] :ordered-evaluation-node-ids [:operator :arg0 :member-use])
               {:core-node-id :contains-call :operator-node-id :contains-operator :operator-binding-id :contains-binding
                :argument-node-ids [:result :head-query] :ordered-evaluation-node-ids [:contains-operator :result :head-query]
                :evaluation-order :operator-then-arguments :result-policy :call-result}]
        edges (mapv (fn [i call] {:ordinal i :call-core-node-id (:core-node-id call)
                         :caller-function-syntax-id "function-target" :caller-function-core-node-id :fn-node
                         :callee-binding-id (:operator-binding-id call)
                         :callee-definition-syntax-id (:definition-syntax-id (bindings (:operator-binding-id call)))
                         :callee-function-syntax-id nil :callee-function-core-node-id nil
                         :argument-core-node-ids (:argument-node-ids call) :ordered-evaluation-node-ids (:ordered-evaluation-node-ids call)
                         :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}) (range) calls)
        record {:function-syntax-id "function-target" :function-core-node-id :fn-node :body-core-node-id :outer-let
                :definition-name 'main :definition-binding-id :main-binding :definition-syntax-id :main-name
                :fixed-arity 0 :parameter-binding-ids []}
        definition {:core-node-id :def-main :value-node-id :fn-node :binding-id :main-binding :name 'main}
        order [:arg1 :again-init :value-init :first-operator :head-use :symbol-vector :vector-first :member-use :condition
               :next-again :operator :arg0 :call :transfer :contains-operator :result :head-query :contains-call :branch
               :inner-let :loop-node :outer-let :fn-node :def-main]
        loop-bindings (mapv #(update % :name (fn [name] (case name value 'seen again 'pending name))) (get-in base [:core :loop-bindings]))
        core (assoc (:core base) :module (assoc (get-in base [:core :module]) :namespace 'self-hosting.sh08.vector-first-symbol-loop) :nodes (mapv nodes order) :calls calls :call-edges edges :binding-table (vec (vals bindings))
                    :lexical-bindings (vec (vals (:aliases base))) :function-records [record] :definitions [definition]
                    :loop-bindings loop-bindings)
        core (assoc core :identity-preimage (select-keys core [:loop-bindings :recur-targets :recur-transfers]))]
    {:core core :nodes nodes :forms forms :bindings bindings :order order}))

(defn- vector-first-symbol-loop-cold-source-shape [{:keys [core nodes forms bindings order]}]
  (let [scalar-id-fields #{:node-id :form-id :syntax-id :scope-id :parent-form-id :binding-id :definition-syntax-id
                           :binding-scope-id :initializer-form-id :initializer-syntax-id :initializer-node-id :target-id :parent-target-id
                           :function-syntax-id :function-core-node-id :body-core-node-id :definition-binding-id :owner-core-node-id
                           :owner-form-id :owner-syntax-id :body-scope-id :loop-core-node-id :recur-core-node-id :recur-form-id :recur-syntax-id}
        all (tree-seq coll? seq [core forms bindings])
        id-values (distinct (concat (keys nodes) (keys forms) (keys bindings)
                        (for [m all :when (map? m) [key value] m :when (and (contains? scalar-id-fields key) (or (keyword? value) (string? value)))] value)))
        ids (into {} (map (fn [id] [id (bootstrap/reader-canonical-hash {:domain :normalized-source-shape-id :id id})]) id-values))
        rename (fn rename [field value]
                 (cond
                   (map? value) (into {} (map (fn [[key item]]
                     [(if (contains? #{:nodes :forms :bindings} field) (get ids key key) key) (rename key item)]) value))
                   (vector? value) (mapv #(rename nil %) value)
                   (seq? value) (apply list (map #(rename nil %) value))
                   (set? value) (set (map #(rename nil %) value))
                   (contains? #{:core-form :kind :position :literal-kind :value :quoted-value :name :projection :definition-name} field) value
                   :else (get ids value value)))
        renamed (rename nil {:core core :nodes nodes :forms forms :bindings bindings :order order})
        span {:source "/Users/matt/.codex/worktrees/1ee2/uniform/bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/vector-first-symbol-set-loop.gravity"
              :form-index 1 :start {:line 10 :column 1 :char 150 :byte 150}
              :end {:line 15 :column 44 :char 389 :byte 389} :byte-start 150 :byte-end 389}
        nodes (into {} (map (fn [[id n]] [id (-> n (assoc-in [:source :semantic-span] span)
                         (assoc :artifact :gravity/sh07-core-node :generated? false
                                :module {:namespace 'self-hosting.sh08.vector-first-symbol-loop :profile :meta :target :jvm}
                                :metadata nil :preserved-declarations {:effects [:memory/allocate] :capabilities [] :safety :safe}
                                :pending-fact-families [:types :effects :ownership :safety] :lowering-rule-version :sh07-b11-v12))]) (:nodes renamed)))
        forms (into {} (map (fn [[id f]] [id (assoc f :source-span span)]) (:forms renamed)))
        core (assoc (:core renamed) :nodes (mapv nodes (:order renamed)))]
    (assoc renamed :core core :nodes nodes :forms forms)))

(deftest vector-first-symbol-loop-witness-cold-solver-development-kernel
  ;; Normalized source-shaped DATA; original owners run cold, with no final type seeds.
  (let [input (vector-first-symbol-loop-cold-source-shape (vector-first-symbol-loop-cold-data)) core (:core input)
        quotes (c7-invoke 'sh08-qd-products [(:nodes core) (vec (vals (:forms input))) (:module core)])
        skeletons (c7-invoke 'sh08-ft-function-skeletons [(:function-records core) (:module core)])
        context {:ordering-core core :loop-core core :lookup-nodes (:nodes input) :source-node-table (:nodes input)
                 :lookup-forms (vec (vals (:forms input))) :lookup-bindings (vec (vals (:bindings input)))
                 :boolean-matches [] :quote-types (:type-table quotes)
                 :shape-call-products {:module (:module core) :calls (:calls core) :call-edges (:call-edges core)
                   :definitions (:definitions core) :function-index (into {} (map (juxt :function-syntax-id identity) (:function-records core)))
                   :recursion-components []}}
        run (fn [] (c7-invoke 'sh08-ft-infer-acyclic-with-context
                           [(:nodes core) (:nodes input) (:lexical-bindings core) (:definitions core)
                            (:calls core) (:call-edges core) skeletons {} context]))
        scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped) original @scoped
        round-number (atom 0) observations (atom [])
        tracked '#{sh08-ft-infer-pass-products-prepared sh08-ft-loop-type-step sh08-lsi-slot sh08-lsi-ground
                   sh08-lsi-vector-first sh08-lsi-first-call-input sh08-lsi-first-data}
        result (with-redefs-fn
          {scoped (fn [runtime plan callee args scope]
                    (when (= 'sh08-ft-infer-pass-products-prepared callee) (swap! round-number inc))
                    (let [round @round-number
                          value (original runtime plan callee args scope)]
                      (when (contains? tracked callee)
                        (swap! observations conj {:round round :helper callee :status (:status value)
                                                  :reason (:reason value)})) value))}
          #(run))]
    (is (= [3 2 1 1] (mapv #(count (get core %)) [:calls :lexical-bindings :function-records :definitions])))
    (is (= [] (:diagnostics quotes)))
    (is (= [:gravity.type/unknown] (mapv :return (vals skeletons))))
    (is (= :converged (:convergence-status result)) (pr-str (select-keys result [:diagnostics])))
    (is (= [] (:diagnostics result)))
    (is (= [:gravity.type/bool] (mapv :return (vals (:function-types result)))))
    (is (= 1 (count (:loop-member-products result))))
    (println (pr-str {:vector-first-symbol-loop-normalized-cold-solver (select-keys result [:convergence-status])
                     :source-evidence false :final-type-seeds false :round-observations @observations}))))

(defn- vector-first-symbol-loop-source-shaped-env []
  ;; Independent quote/first products only; this is normalized DATA, not source admission.
  (let [{:keys [core nodes forms bindings]} (vector-first-symbol-loop-cold-source-shape (vector-first-symbol-loop-cold-data))
        call (first (:calls core)) edge (first (:call-edges core)) ctor-id (first (:argument-node-ids call))
        head-id (first (:children (nodes ctor-id)))
        alias-for (fn [name] (first (filter #(= name (:name (bindings (:binding-id %)))) (:lexical-bindings core))))
        head (alias-for 'head) member (alias-for 'member) quote-id (:initializer-node-id head)
        member-use (first (filter #(and (= :reference (:core-form %)) (= (:binding-id member) (get-in % [:attributes :binding-id]))) (:nodes core)))
        quoted (c7-invoke 'sh08-qd-products [(:nodes core) (vec (vals forms)) (:module core)])
        head-type (get-in quoted [:type-table quote-id])
        types {quote-id head-type head-id head-type ctor-id {:kind :gravity.type/vector :shape :literal :element-types [head-type]}}
        derived (c7-invoke 'sh08-lq-derive [core (nodes (:core-node-id call)) call edge nodes bindings types forms ])
        types (assoc types (:operator-node-id call) (:descriptor derived) (:core-node-id call) (:type derived) (:node-id member-use) (:type derived))
        slot (first (filter #(= 'seen (:name %)) (:loop-bindings core)))]
    {:env {:core core :nodes nodes :forms forms :bindings bindings :types types
           :aliases (into {} (map (juxt :binding-id identity) (:lexical-bindings core)))
           :calls (into {} (map (juxt :core-node-id identity) (:calls core)))
           :edges (into {} (map (juxt :call-core-node-id identity) (:call-edges core)))}
     :start (:node-id member-use) :binding (:binding-id slot) :operator (:operator-node-id call) :derived derived :quotes quoted}))

(defn- vector-first-symbol-loop-hostile-warm-values [normal opaque callbacks]
  [[:opaque opaque] [:vector [opaque]] [:map {:opaque opaque}]
   [:lazy (lazy-seq (swap! callbacks inc) (list opaque))]
   [:opaque-extra (assoc normal :extra opaque)] [:ordinary-extra (assoc normal :extra true)]
   [:nested-source (assoc-in normal [:source-call :argument-node-ids] [opaque])]
   [:deep (assoc normal :extra (reduce (fn [out _] [out]) :bottom (range 129)))]
   [:wide (assoc normal :extra (vec (repeat 1025 :item))) ]
   [:wrong-kind (assoc normal :kind :spoof)] [:wrong-source (assoc normal :source :spoof)]
   [:wrong-version (assoc normal :version :spoof)] [:wrong-bounds-kind (assoc-in normal [:bounds-proof :kind] :spoof)]
   [:wide-map (into {} (map (fn [i] [(keyword (str "field-" i)) i]) (range 1025)))]])

(deftest vector-first-symbol-loop-witness-final-warm-descriptor-development-kernel
  (let [{:keys [env start binding operator derived quotes]} (vector-first-symbol-loop-source-shaped-env)
        callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "warm descriptor hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "warm descriptor equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "warm descriptor printing"))))
        values (vector-first-symbol-loop-hostile-warm-values (get-in env [:types operator]) opaque callbacks)
        walks [(fn [env] (c7-invoke 'sh08-lsi-path [start :member binding env]))
               (fn [env] (effects 'body-lsi-path start :member binding env 65536))]]
    (is (= :accepted (:status derived)))
    (is (= [] (:diagnostics quotes)))
    (doseq [walk walks]
      (let [accepted (walk env)]
        (is (= :accepted (:status accepted)) (pr-str accepted))
        (is (= 5 (count (:path accepted))))
        (doseq [[label value] values]
          (is (= :rejected (:status (walk (assoc-in env [:types operator] value)))) (str label))
          (is (zero? @callbacks) (str label)))))))

(defn- vector-first-symbol-loop-observe-data-work [function thunk]
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        builtin (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-invoke-builtin)
        artifact (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-artifact-invoke)
        formatter @(ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-artifact-function)
        original-scoped @scoped original-builtin @builtin original-artifact @artifact depth (atom 0) plans (atom [])
        formats (atom {:keyword 0 :symbol 0 :string 0})
        record-format (fn [value] (cond (keyword? value) (swap! formats update :keyword inc)
                                       (symbol? value) (swap! formats update :symbol inc)
                                       (string? value) (swap! formats update :string inc)))
        result (with-redefs-fn
          {scoped (fn [runtime plan callee args scope]
                    (if (= function callee)
                      (do (swap! depth inc) (swap! plans conj plan)
                          (try (original-scoped runtime plan callee args scope)
                               (finally (swap! plans pop) (swap! depth dec))))
                      (original-scoped runtime plan callee args scope)))
           builtin (fn [& args]
                     (let [callee (nth args (- (count args) 2)) values (last args) value (first values)]
                       (when (and (pos? @depth) (= 'str callee)
                                  (identical? (peek @plans) (nth args (- (count args) 3))))
                         (record-format value))
                       (apply original-builtin args)))
           artifact (fn [runtime callee args]
                      (when (and (pos? @depth) (= formatter callee) (:runtime-artifact-plan runtime))
                        (record-format (first args)))
                      (original-artifact runtime callee args))} thunk)]
    {:result result :formats @formats}))

(defn- vector-first-symbol-loop-keyword-cases []
  [[:ordinary :ordinary] [:qualified :alpha.beta/gamma-delta?]
   [:unusual (keyword "odd namespace" "punctuation?!")]
   [:long (keyword "qualified.ns" (apply str (repeat 66000 "x")))]])

(deftest vector-first-symbol-loop-witness-data-callback-controls-development-kernel
  ;; Data validation must not render or otherwise materialize host values.
  (doseq [[label value] (vector-first-symbol-loop-keyword-cases)]
    (let [observed (vector-first-symbol-loop-observe-data-work
                    'sh08-lsi-first-data
                    #(c7-invoke 'sh08-lsi-first-data [value]))]
      (is (= :accepted (get-in observed [:result :status])) (str label))
      (is (= {:keyword 0 :symbol 0 :string 0} (:formats observed)))))
  (let [{:keys [env start binding]} (vector-first-symbol-loop-source-shaped-env)
        observed (vector-first-symbol-loop-observe-data-work
                  'sh08-lsi-path
                  #(c7-invoke 'sh08-lsi-path [start :member binding env]))]
    (is (= :accepted (get-in observed [:result :status])))
    (is (= {:keyword 0 :symbol 0 :string 0} (:formats observed)))))

(deftest vector-first-empty-diagnostic-oracle-development-kernel
  ;; Original derivation and diagnostic rendering on normalized DATA, not source replay.
  (let [owner 'gravity.self-hosting.sh08-function-call-type-test
        input (ns-resolve owner 'list-query-input) derive (ns-resolve owner 'list-query-derived)
        empty-vector {:kind :gravity.type/vector :shape :literal :element-types []}
        span {:line-start 1 :column-start 1 :line-end 1 :column-end 11}]
    (doseq [[arity rule reason constraint?]
            [[1 "C7-ANNOTATION" :vector-first-nonempty-complete-positional-type-required false]
             [2 "C7-TYPE-MISMATCH" :vector-first-arity-mismatch true]]]
      (let [data (input 'first (vec (repeat arity empty-vector))) rejected (derive data)
            node (-> (:node data) (assoc-in [:source :semantic-span] span)
                     (assoc-in [:source :origin-chain] [:normalized-origin])
                     (assoc-in [:source :generated-origin] []))
            issue (assoc (select-keys rejected [:rule :reason :expected :actual]) :node node)
            diagnostics (c7-invoke 'sh08-ft-render-inference-diagnostics [[issue] {(:node-id node) node} (:module data)])
            diagnostic (first diagnostics)]
        (is (= :rejected (:status rejected)))
        (is (= 1 (count diagnostics)))
        (is (= [rule reason] ((juxt :rule :reason) diagnostic)))
        (is (= (vec (repeat arity empty-vector)) (:actual-type diagnostic)))
        (is (= (:node-id node) (:core-node-id diagnostic)))
        (is (= (get-in node [:source :syntax-id]) (:syntax-id diagnostic)))
        (is (= (first (:resolved-binding-ids node)) (:binding-id diagnostic)))
        (is (= span (:source-span diagnostic)))
        (is (= (select-keys (:module data) [:profile :target]) (select-keys diagnostic [:profile :target])))
        (is (= [:normalized-origin] (:origin-chain diagnostic)))
        (is (= [] (:generated-origin diagnostic)))
        (is (contains? diagnostic :constraint-id))
        (is (if constraint? (map? (:constraint-id diagnostic)) (nil? (:constraint-id diagnostic))))
        (is (contains? (:diagnostic-id-request diagnostic) :constraint-id))
        (is (= (:constraint-id diagnostic) (get-in diagnostic [:diagnostic-id-request :constraint-id])))
        (is (string? (:diagnostic-id diagnostic)))))))
