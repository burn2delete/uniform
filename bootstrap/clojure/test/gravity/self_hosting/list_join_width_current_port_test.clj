(ns gravity.self-hosting.list-join-width-current-port-test
  "Current-source controls for the List-if join width and consumer contract.

  The production width predicates are already present on current main.  These
  tests keep the useful PR611/637 checks at the current API boundary without
  reopening the owned SH09/SH01 test files or claiming an authoritative C8,
  C7, or runtime migration result.
  "
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- resolved [namespace name]
  (or (ns-resolve namespace name)
      (throw (ex-info "Missing current test helper" {:namespace namespace :name name}))))

(defn- list-input [left right ordinal]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  ((resolved 'gravity.self-hosting.sh08-function-call-type-test 'list-if-join-input)
   left right ordinal))

(defn- c7 [function arguments]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  ((resolved 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
   function arguments))

(defn- body [function & arguments]
  (require 'gravity.self-hosting.sh09-body-effect-test)
  (apply (resolved 'gravity.self-hosting.sh09-body-effect-test 'leaf) function arguments))

(def ^:private stage2-plan
  (delay
    (let [source (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- stage2 [function arguments]
  (require 'gravity.self-hosting.sh01-stage2-runtime-iteration-test)
  (let [runtime @(resolved 'gravity.self-hosting.sh01-stage2-runtime-iteration-test 'runtime)
        plan @stage2-plan]
    (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec arguments))))

(def ^:private typed-runtime-plan
  (delay
    (require 'gravity.self-hosting.sh01-stage2-runtime-iteration-test)
    (:plan ((resolved 'gravity.self-hosting.sh01-stage2-runtime-iteration-test
                      'typed-catch-runtime-projection)))))

(defn- typed-evaluate [instruction environment]
  (require 'gravity.self-hosting.sh01-stage2-runtime-iteration-test)
  (let [runtime @(resolved 'gravity.self-hosting.sh01-stage2-runtime-iteration-test 'runtime)]
    (bootstrap/p15-s23-stage2-runtime-execute-function
     runtime @typed-runtime-plan 'p15-s23-runtime-evaluate-closed-instruction
     [instruction environment ""])))

(defn- finite [members]
  {:kind :gravity.type/list :shape :literal :element-types members})

(defn- element-result [type]
  [(body 'body-ij-element type)
   (stage2 'p15-s23-ij-element [type])])

(defn- checked-join [left right ordinal]
  (let [{:keys [core node nodes types bindings forms nids refs]} (list-input left right ordinal)
        derived (c7 'sh08-ij-derive [core node nodes types bindings forms])
        row (c7 'sh08-ij-row [(:premise derived)])
        facts {:type-table (assoc types (:node-id node) (:type derived))
               :constraint-ledger [row]}
        source-forms (vec (vals forms))]
    {:core core :node node :nodes nodes :types types :bindings bindings :forms forms
     :nids nids :refs refs :derived derived :row row :facts facts :source-forms source-forms}))

(deftest current-list-join-element-widths
  (let [s :gravity.type/string
        expected (fn [width]
                   (if (zero? width)
                     {:status :accepted :element-type nil
                      :relation :empty-positional-list :checked-member-count 0}
                     {:status :accepted :element-type s
                      :relation :all-positional-elements-match
                      :checked-member-count width}))]
    (doseq [width [0 1 1024 1025 2049]]
      (testing (str "finite positional width " width)
        (let [[body-result emitter-result] (element-result (finite (vec (repeat width s))))]
          (is (= (expected width) body-result))
          (is (= body-result emitter-result)))))
    (doseq [type [(finite (list s))
                  (assoc (finite [s]) :extra :forged)
                  (finite (assoc (vec (repeat 1025 s)) 1024 :gravity.type/keyword))
                  :gravity.type/unknown
                  nil]]
      (testing (str "reject malformed member representation " (pr-str (select-keys (if (map? type) type {}) [:kind :shape :extra])))
        (let [[body-result emitter-result] (element-result type)]
          (is (not= :accepted (:status body-result)))
          (is (= body-result emitter-result)))))
    (doseq [[type reason]
            [[(finite (list s)) :independent-list-element-required]
             [(finite [{:kind :gravity.type/list :shape :literal :element-types [s]}])
              :independent-atomic-element-required]
             [(assoc (finite [s]) :shape :homogeneous :element-type s :element-types [s])
              :independent-list-element-required]
             [(finite (assoc (vec (repeat 2049 s)) 2048 :gravity.type/keyword))
              :incompatible-list-elements]]]
      (let [[body-result emitter-result] (element-result type)
            expected {:status :not-applicable :reason reason}]
        (is (= expected (select-keys body-result [:status :reason])))
        (is (= expected (select-keys emitter-result [:status :reason])))
        (is (= body-result emitter-result))))
    (let [[body-result emitter-result]
          (element-result {:kind :gravity.type/list :shape :homogeneous
                           :element-type s})]
      (is (= {:status :accepted :element-type s
              :relation :homogeneous-list-identity :checked-member-count nil}
             body-result))
      (is (= body-result emitter-result)))))

(deftest current-list-join-wide-consumer-controls
  (let [s :gravity.type/string
        wide (finite (vec (repeat 1025 s)))
        empty (finite [])]
    (doseq [[left right branch ordinal]
            [[wide empty 0 31] [empty wide 1 32]]]
      (testing (str "wide branch " branch)
        (let [{:keys [core node nodes types bindings forms derived facts source-forms]}
              (checked-join left right ordinal)
              premise (:premise derived)
              branch-id (get-in premise [:branch-node-ids branch])
              result (body 'body-ij-context core facts bindings source-forms)
              lowered (stage2 'p15-s23-ij-context [core facts bindings source-forms])]
          (is (= :accepted (:status derived)) (pr-str derived))
          (is (= 1025 (get-in premise [:branch-compatibility branch :checked-member-count])))
          (is (= :accepted (:status result)) (pr-str result))
          (is (= :accepted (:status lowered)) (pr-str lowered))
          (is (= (:status result) (:status lowered)))
          (doseq [bad [(assoc-in types [branch-id :element-types 1024] :gravity.type/keyword)
                       (assoc-in types [branch-id :extra] :forged)
                       (assoc-in types [branch-id :element-types] (apply list (get-in types [branch-id :element-types])))]]
            (let [bad-derived (c7 'sh08-ij-derive [core node nodes bad bindings forms])
                  bad-body (body 'body-ij-derive core node nodes bad bindings forms)
                  bad-lowered (stage2 'p15-s23-ij-derive [core node nodes bad bindings forms])]
              (is (= :not-applicable (:status bad-derived)) (pr-str bad-derived))
              (is (= :not-applicable (:status bad-body)) (pr-str bad-body))
              (is (= :not-applicable (:status bad-lowered)) (pr-str bad-lowered))))
          (let [forged (assoc-in facts [:constraint-ledger 0 :dependencies
                                         :branch-compatibility branch :checked-member-count] 1024)
                forged-body (body 'body-ij-context core forged bindings source-forms)
                forged-lowered (stage2 'p15-s23-ij-context [core forged bindings source-forms])]
            (is (= :rejected (:status forged-body)) (pr-str forged-body))
            (is (= :rejected (:status forged-lowered)) (pr-str forged-lowered))))))))

(defn- typed-join-context [data facts]
  (let [{:keys [core node nodes bindings forms nids refs]} data
        checked (stage2 'p15-s23-ij-context [core facts bindings forms])
        types (:type-table facts)]
    {:checked checked
     :context {:core core :types types :nodes nodes :forms forms :bindings bindings
               :ledger (:constraint-ledger facts) :if-join-context (:context checked)
               :locals {(refs :condition) 'include
                        (refs :then) 'left
                        (refs :else) 'right}
               :parameter-types (into {}
                                      (for [[role id] refs]
                                        [id (get types (nids role))]))}}))

(deftest current-list-join-wide-typed-compile-and-runtime
  (let [s :gravity.type/string
        wide (finite (vec (repeat 1025 s)))
        empty (finite [])
        expected {:op :if :test {:op :local :name 'include}
                  :then {:op :local :name 'left}
                  :else {:op :local :name 'right}}]
    (doseq [[left right branch ordinal]
            [[wide empty 0 41] [empty wide 1 42]]]
      (let [data (checked-join left right ordinal)
            {:keys [core node types bindings forms facts derived nids]} data
            {:keys [checked context]} (typed-join-context data facts)
            lowered (stage2 'p15-s23-typed-compile-node [context (:node-id node)])
            wide-value (vec (repeat 1025 :wide-value))
            empty-value []
            left-value (if (zero? branch) wide-value empty-value)
            right-value (if (= 1 branch) wide-value empty-value)
            true-result (typed-evaluate (:value lowered)
                                        {'include true 'left left-value 'right right-value})
            false-result (typed-evaluate (:value lowered)
                                         {'include false 'left left-value 'right right-value})
            branch-id (get-in (:premise derived) [:branch-node-ids branch])
            original (get types branch-id)]
        (is (= :accepted (:status (:derived data))) (pr-str (:derived data)))
        (is (= :accepted (:status checked)) (pr-str checked))
        (is (= 1025 (get-in checked [:context :premises (:node-id node)
                                     :branch-compatibility branch :checked-member-count])))
        (is (= :complete (:status lowered)) (pr-str lowered))
        (is (= expected (:value lowered)))
        (is (= [left-value "" :normal] true-result))
        (is (= [right-value "" :normal] false-result))
        (is (identical? left-value (first true-result)))
        (is (identical? right-value (first false-result)))
        (doseq [[bad reason]
                [[(assoc-in original [:element-types 1024] :gravity.type/keyword)
                  :incompatible-list-elements]
                 [(assoc original :extra :forged)
                  :independent-list-element-required]
                 [(assoc original :element-types
                         (apply list (:element-types original)))
                  :independent-list-element-required]
                 [(assoc original :element-types
                         (assoc (vec (:element-types original)) 0
                                {:kind :gravity.type/list :shape :literal
                                 :element-types [s]}))
                  :independent-atomic-element-required]]]
          (let [bad-types (assoc types branch-id bad)
                bad-facts (assoc facts :type-table
                                 (assoc bad-types (:node-id node) (:type derived)))
                bad-derived (c7 'sh08-ij-derive
                                [core node (:nodes data) bad-types bindings forms])
                bad-context (typed-join-context data bad-facts)
                bad-lowered (stage2 'p15-s23-typed-compile-node
                                    [(assoc context :types (:type-table bad-facts))
                                     (:node-id node)])]
            (is (= :not-applicable (:status bad-derived)) (pr-str bad-derived))
            (is (= reason (:reason bad-derived)) (pr-str bad-derived))
            (is (= :rejected (:status (:checked bad-context))) (pr-str bad-context))
            (is (= :list-if-join-result-type-mismatch
                   (:reason (:checked bad-context)))
                (pr-str (:checked bad-context)))
            (is (= :rejected (:status bad-lowered)) (pr-str bad-lowered))
            (is (= :typed-if-join-evidence-mismatch
                   (get-in bad-lowered [:facts :reason]))
                (pr-str bad-lowered))))
        (let [forged-facts (update facts :constraint-ledger
                                   (fn [ledger]
                                     [(assoc-in (first ledger)
                                                [:dependencies :branch-compatibility branch
                                                 :checked-member-count] 1024)]))
              forged-context (typed-join-context data forged-facts)
              forged-lowered (stage2 'p15-s23-typed-compile-node
                                     [(assoc context :ledger (:constraint-ledger forged-facts))
                                      (:node-id node)])]
          (is (= :rejected (:status (:checked forged-context)))
              (pr-str forged-context))
          (is (= :list-if-join-constraint-census
                 (:reason (:checked forged-context)))
              (pr-str (:checked forged-context)))
          (is (= :rejected (:status forged-lowered)) (pr-str forged-lowered))
          (is (= :typed-if-join-evidence-mismatch
                 (get-in forged-lowered [:facts :reason]))
              (pr-str forged-lowered)))))))
