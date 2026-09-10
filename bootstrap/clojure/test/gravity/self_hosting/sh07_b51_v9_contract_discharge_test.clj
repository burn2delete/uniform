(ns gravity.self-hosting.sh07-b51-v9-contract-discharge-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]
            [gravity.tools.sh07-b51-git-evidence :as evidence]
            [gravity.tools.sh07-b51-v9-codec :as codec]))

(def ^:private checked-fuel-keys
  [:maximum-forms :maximum-top-level-forms :maximum-fragment-forms
   :maximum-form-children :maximum-form-depth :maximum-origin-entries
   :maximum-metadata-entries :maximum-alias-records
   :maximum-carrier-nodes :maximum-carrier-depth :maximum-carrier-width
   :maximum-scalar-bytes :maximum-fragments :maximum-module-bindings
   :maximum-bindings :maximum-resolutions :maximum-module-resolutions
   :maximum-effects :maximum-capabilities :maximum-exports
   :maximum-module-core-nodes :maximum-resolved-core-carrier-nodes
   :maximum-resolved-core-carrier-depth :maximum-resolved-core-carrier-width
   :maximum-resolved-core-scalar-bytes :maximum-template-carrier-nodes
   :maximum-template-carrier-depth :maximum-template-carrier-width
   :maximum-template-scalar-bytes :maximum-generated-digest-carrier-nodes
   :maximum-generated-digest-carrier-depth
   :maximum-generated-digest-carrier-width
   :maximum-generated-digest-scalar-bytes :maximum-digest-requests])

(defn- checked-fuel []
  (into {} (map-indexed (fn [index key] [key (+ 100 index)])
                        checked-fuel-keys)))

(defn- proof-fuel [checked]
  (reduce-kv
   (fn [result proof-key executable-key]
     (-> result
         (dissoc executable-key)
         (assoc proof-key (get checked executable-key))))
   checked materialize/fixed-fuel-executable-aliases))

(defn- pre-stages []
  {:l1 {:document :document :source-units [] :grammar-change :none}
   :c2 {:snapshot :snapshot :units [] :source-map {:entries []}
        :diagnostics {:entries []} :literal-records []
        :reader-extensions []}
   :l2 {:owner-selection :owners :core-forms {:entries []}
        :evaluation-order-root "sha256:evaluation"
        :effects-root "sha256:effects"
        :capabilities-root "sha256:capabilities"}
   :c6 {:ast {:entries []} :syntax-core-map {:entries []}
        :desugaring-trace {:entries []} :origins-facts-order []}
   :boot2 {:source :source :compiler :compiler :backend :no-backend
           :dependencies {:entries []} :runtime-assumptions :jvm}
   :boot3 {:modules
           [{:ordinal 0 :module 'example
             :input-artifacts [] :output-artifacts [] :effects [] :facts []}]
           :profile :meta :stage-advanced false}
   :boot4 {:module-manifest {:entries []} :effects {:entries []}
           :capabilities {:entries []} :fact-provenance {:entries []}}})

(defn- collections-by-owner []
  {:l1 {:module-forms [] :top-level-forms [] :fragment-forms []
        :form-children [] :form-depths [] :origin-entries []
        :metadata-entries [] :alias-records []}
   :c2 {:module-carriers []}
   :l2 {:fragments [] :module-bindings [] :bindings []
        :fragment-resolutions [] :module-resolutions [] :effects []
        :capabilities [] :exports []}
   :c6 {:module-core [] :resolved-core-carriers []}
   :boot2 {:template-carriers []}
   :boot3 {:generated-digest-carriers []}
   :boot4 {:module-digest-requests []}})

(def ^:private fuel-source-roots
  {:source-descriptors-root "sha256:source"
   :event-root "sha256:events"
   :semantic-algorithm-source-root "sha256:algorithm"
   :sh06-authority-carrier-id "sha256:sh06"
   :bundle-core-id "sha256:bundle"})

(deftest fixed-fuel-is-the-exact-34-key-checked-core-projection
  (let [checked (checked-fuel)
        proof (proof-fuel checked)]
    (is (= 34 (count checked) (count proof)))
    (is (= checked (materialize/fixed-fuel-executable proof checked)))
    (testing "alias collisions fail before subject construction"
      (is (= :fixed-fuel-executable-mismatch
             (try
               (materialize/fixed-fuel-executable
                (assoc proof :maximum-forms 999) checked)
               nil
               (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error)))))))))

(deftest subjects-precede-the-exact-seven-operation-specs
  (let [subjects
        (materialize/build-fuel-subjects fuel-source-roots (pre-stages)
                                         (collections-by-owner))
        operations (materialize/build-fixed-fuel-operation-specs subjects)]
    (is (= 7 (count subjects) (count operations)))
    (is (= (vec (range 7)) (mapv :ordinal subjects) (mapv :ordinal operations)))
    (is (= [:l1 :c2 :l2 :c6 :boot2 :boot3 :boot4]
           (mapv :owner subjects) (mapv :owner operations)))
    (is (= (mapv :subject-id subjects) (mapv :subject-id operations)))
    (is (every? #(= #{:ordinal :owner :kind :source-roots :collections
                      :collection-roots :stage-semantic-root :subject-id}
                    (set (keys %)))
                subjects))
    (is (every? #(not-any? (set (keys %))
                           [:receipt-id :operation-id :witness-id
                            :certificate-id :checker-id])
                subjects))))

(deftest pre-stage-and-source-root-smuggling-fails-closed
  (testing "a later field cannot enter an exact receipt-free prefix"
    (is (= :pre-stage-prefix-shape
           (try
             (materialize/build-fuel-subjects
              fuel-source-roots
              (assoc-in (pre-stages) [:l2 :authority-receipt-id]
                        "sha256:forged")
              (collections-by-owner))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:reason (ex-data error)))))))
  (testing "fuel-source roots are a closed five-key map"
    (is (= :fuel-source-roots-shape
           (try
             (materialize/build-fuel-subjects
              (assoc fuel-source-roots :host-path "/tmp/escape")
              (pre-stages) (collections-by-owner))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:reason (ex-data error))))))))

(defn- owner-transaction []
  (let [top-form-id "sha256:top-form"
        top-syntax-id "sha256:top-syntax"
        fn-form-id "sha256:fn-form"
        fn-syntax-id "sha256:fn-syntax"
        body-form-id "sha256:body-form"
        body-syntax-id "sha256:body-syntax"
        def-node-id "sha256:def-node"
        fn-node-id "sha256:fn-node"
        body-node-id "sha256:body-node"
        binding-id "sha256:binding"
        q {:lineage {:sh06-semantic-projection-id "sha256:source"}
           :top-level-form-ids [top-form-id]
           :forms [{:form-id top-form-id :syntax-id top-syntax-id
                    :parent-form-id nil :child-form-ids [fn-form-id]}
                   {:form-id fn-form-id :syntax-id fn-syntax-id
                    :parent-form-id top-form-id
                    :child-form-ids [body-form-id]}
                   {:form-id body-form-id :syntax-id body-syntax-id
                    :parent-form-id fn-form-id :child-form-ids []
                    :declared-name 'target}]
           :macro-origin-traces
           [{:macro 'defn :output-def-syntax-id top-syntax-id
             :introduced-fn-syntax-id fn-syntax-id}]}
        rc {:nodes [{:node-id def-node-id :children [fn-node-id]}
                    {:node-id fn-node-id :children [body-node-id]}
                    {:node-id body-node-id :children []}]
            :definitions [{:name 'target :binding-id binding-id
                           :core-node-id def-node-id
                           :value-node-id fn-node-id}]
            :function-records
            [{:function-core-node-id fn-node-id
              :function-form-id fn-form-id
              :function-syntax-id fn-syntax-id
              :body-core-node-id body-node-id
              :fixed-arity 1 :definition-kind :named-top-level
              :definition-binding-id binding-id
              :definition-core-node-id def-node-id
              :definition-name 'target}]}]
    {:module-ordinal 0 :authenticated-core-request q
     :resolved-core-carrier rc}))

(deftest authentic-owner-selection-follows-the-top-level-defn-root
  (let [transaction (owner-transaction)
        registry (materialize/build-owner-registry [transaction])
        root-specs [{:root-ordinal 1 :function 'target :arity 1
                     :module-ordinal 0}]
        plan-id "sha256:plan"
        bundle-core
        {:module-plans [{:module-ordinal 0 :plan-id plan-id}]
         :public-roots [{:root-ordinal 1 :function 'target :arity 1
                         :plan-id plan-id}]}
        selection
        (materialize/build-owner-selection root-specs bundle-core registry)]
    (is (= 1 (count registry)))
    (is (= 0 (:top-level-form-ordinal (first registry))))
    (is (= "sha256:def-node" (:defn-id (first registry))))
    (is (= "sha256:body-node" (:declared-body-root-id (first registry))))
    (is (= (:registry-id (first registry))
           (get-in selection [:selections 0 :registry-id])))
    (is (= [] (:rejected selection)))
    (testing "a duplicate authentic owner is ambiguity, never scan-first"
      (is (= :owner-root-candidate-count
             (try
               (materialize/build-owner-selection
                root-specs bundle-core (conj registry (first registry)))
               nil
               (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error)))))))
    (testing "an untraced top-level root is rejected, not filtered"
      (let [forged
            (-> transaction
                (update-in [:authenticated-core-request :top-level-form-ids]
                           conj "sha256:extra-top")
                (update-in [:authenticated-core-request :forms]
                           conj {:form-id "sha256:extra-top"
                                 :syntax-id "sha256:extra-syntax"
                                 :parent-form-id nil :child-form-ids []}))]
        (is (= :owner-top-level-trace-count
               (try
                 (materialize/build-owner-registry [forged])
                 nil
                 (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error))))))))))

(defn- row-predicate-specs
  [row-ordinal]
  (filterv #(= row-ordinal (:row-ordinal %)) evidence/predicate-specs))

(defn- expected-predicate-triples
  [row]
  (vec
   (for [phase evidence/phases
         obligation (:obligations row)
         :let [bindings
               (filterv #(and (= phase (:phase %))
                              (some #{obligation} (:obligations %)))
                        (:binding-specs row))]
         :when (seq bindings)]
     [phase obligation (mapv :path bindings)])))

(defn- assert-row-contract!
  [row-ordinal]
  (let [row (nth evidence/row-plan row-ordinal)
        specs (row-predicate-specs row-ordinal)]
    (is (= #{:ordinal :id :document-ordinals :sections :disposition
             :obligations :binding-specs :test-ids :diagnostic}
           (set (keys row))))
    (is (= row-ordinal (:ordinal row)))
    (is (= (expected-predicate-triples row)
           (mapv (juxt :phase :obligation :required-subject-paths) specs)))
    (is (every?
         (fn [spec]
           (= (:predicate-id spec)
              (codec/H
               [:gravity/sh07-b51-contract-predicate-id-v1
                {:row-plan-id evidence/row-plan-id
                 :row-ordinal row-ordinal
                 :phase (:phase spec)
                 :obligation (:obligation spec)
                 :program (:program spec)}])))
         specs))))

(defn- first-pre-subject
  []
  (let [row (first evidence/row-plan)
        binding (first (:binding-specs row))
        value {:ordinal 0 :path "AGENTS.md"}
        without-id (assoc binding :row-ordinal 0 :value value)]
    (assoc without-id :value-id
           (codec/H [:gravity/sh07-b51-contract-binding-v2 without-id]))))

(deftest contract-agents
  (assert-row-contract! 0)
  (let [spec (first (filter #(and (= 0 (:row-ordinal %))
                                  (= :pre-evidence (:phase %))
                                  (= :normative-priority (:obligation %)))
                            evidence/predicate-specs))
        subject (first-pre-subject)
        env {:context {:row-ordinal 0 :row-id :agents
                       :phase :pre-evidence
                       :obligation :normative-priority}
             :subjects [subject]
             :carriers
             {:document-read-receipt
              {:documents [{:ordinal 0 :path "AGENTS.md"}]}}}]
    (is (true? (evidence/evaluate-contract-predicate! spec env)))
    (is (false?
         (evidence/evaluate-contract-predicate!
          spec (assoc-in env [:subjects 0 :value-id]
                         (codec/H [:gravity/test :wrong])))))
    (is (= :predicate-spec-or-env
           (try
             (evidence/evaluate-contract-predicate!
              (assoc spec :program [:literal true]) env)
             nil
             (catch clojure.lang.ExceptionInfo error
               (:reason (ex-data error))))))))

(deftest contract-foundation (assert-row-contract! 1))
(deftest contract-l1 (assert-row-contract! 2))
(deftest contract-l2 (assert-row-contract! 3))
(deftest contract-c2 (assert-row-contract! 4))
(deftest contract-c6 (assert-row-contract! 5))
(deftest contract-c11-nonclaim (assert-row-contract! 6))
(deftest contract-boot1 (assert-row-contract! 7))
(deftest contract-boot2 (assert-row-contract! 8))
(deftest contract-boot3 (assert-row-contract! 9))
(deftest contract-boot4 (assert-row-contract! 10))
(deftest contract-boot5 (assert-row-contract! 11))
(deftest contract-boot6-8 (assert-row-contract! 12))
(deftest contract-test10 (assert-row-contract! 13))
(deftest contract-test11 (assert-row-contract! 14))
(deftest contract-test13 (assert-row-contract! 15))
(deftest proof-contract-scope (assert-row-contract! 16))
(deftest backlog-ownership (assert-row-contract! 17))
(deftest catalog-governance (assert-row-contract! 18))
