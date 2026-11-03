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

(defn- finite [members]
  {:kind :gravity.type/list :shape :literal :element-types members})

(defn- element-result [type]
  [(body 'body-ij-element type)
   (stage2 'p15-s23-ij-element [type])])

(defn- checked-join [left right ordinal]
  (let [{:keys [core node nodes types bindings forms]} (list-input left right ordinal)
        derived (c7 'sh08-ij-derive [core node nodes types bindings forms])
        row (c7 'sh08-ij-row [(:premise derived)])
        facts {:type-table (assoc types (:node-id node) (:type derived))
               :constraint-ledger [row]}
        source-forms (vec (vals forms))]
    {:core core :node node :nodes nodes :types types :bindings bindings :forms forms
     :derived derived :row row :facts facts :source-forms source-forms}))

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
