(ns c6-fresh-construction-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [gravity.c6-c10.source-interface :as interface]
            [gravity.c6-c10.source-contract :as contract]
            [gravity.checked-core.bool-branch-join :as family]))

(binding [*ns* *ns*] (load-file "tools/c6_reader_policy_test.clj"))
(def controls (find-ns 'c6-reader-policy-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn control [name & args] (apply (ns-resolve controls name) args))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] (control 'diagnostic f))
(binding [*ns* owner]
  (alias 'c6-source-interface (ns-name (:ns (meta #'interface/interface-version!))))
  (alias 'bool-branch-join (ns-name (:ns (meta #'family/source-family?))))
  (eval '(def ^:dynamic *p15-s23-stage2-compiler-artifact-binding* nil))
  (eval '(declare p15-s23-c6c10-validate-public-context! p15-s23-c6c10-source-binding!
                  p15-s23-stage2-compiler-artifact-binding! c-backend-stage2-plan-emitter-source-rule!
                  p15-s23-c6c10-private-ingress-products p15-s23-c6c10-private-builder-envelope
                  p15-s23-c6c10-invoke-pinned-source-function! p15-s23-c6c10-seal-digest-request-result!
                  p15-s23-c6c10-throw-sealed-rejection! p15-s23-c6c10-gravity-replay-verification!
                  p15-s23-c6c10-public-artifact p15-s23-stage2-gravity-checked-core-context)))
(control 'evaluate "00701-p15-s23-c6c10-source-relative-path.clj"
         (control 'named? '#{p15-s23-c6c10-builder-function}))
(control 'evaluate "00724-p15-s23-stage2-gravity-checked-core-context.clj"
         (control 'named? '#{p15-s23-c6c10-fresh-construction p15-s23-stage2-gravity-checked-core-source-artifact}))

(def context {:source-path "synthetic.gravity" :source-text "synthetic source" :requested-target :jvm})
(def reader-path (var-get (ns-resolve controls 'reader-path)))
(def expression
  (merge (select-keys contract/current-expression-program
                      [:source-content-hash :semantic-hash :functions-semantic-hash :artifact-hash :function-count])
         {:source-contract-version 2 :source-path "synthetic-emitter.gravity" :self-hosted? false
          :functions (into (sorted-map) (:required-functions contract/current-expression-program))
          :declared-plan-compiler-labels (:declared-plan-compiler-labels contract/current-expression-program)
          :plan {:synthetic-private-plan true}}))
(defn core [version]
  (let [fn-type {:params [] :return :gravity/bool :latent-effects #{} :capabilities #{} :throws #{}
                 :ownership-constraints #{:persistent-immutable-shareable} :profile-constraints #{:hosted}}
        branch (mapv (fn [i op] (cond-> {:node-id (str i) :source-operation op
                                         :type (if (= :function op) fn-type :gravity/bool)}
                                  (= :literal op) (assoc :attributes {:value true})))
                     (range) [:literal :truthy :literal :literal :if :function])
        flat [{:node-id "literal" :source-operation :literal :type :gravity/bool
               :path [:functions 'main :body 0 :body 0] :operands [] :attributes {:value true}}
              {:node-id "sequence" :source-operation :do :type :gravity/bool
               :path [:functions 'main :body 0] :operands ["literal"] :attributes {:body-count 1}}
              {:node-id "root" :source-operation :function :type fn-type
               :path [:functions 'main] :operands ["sequence"]}]]
    {:artifact-id "synthetic-core" :core-nodes (if (= 2 version) branch flat)
     :lexical-binding-records [] :root-node-ids [(if (= 2 version) "5" "root")]}))
(def events (atom []))
(defn with-construction [version options f]
  (let [product (control 'products)
        source-binding {:source-path "synthetic-c6.gravity" :source-contract-version version
                        :plan :synthetic-plan :function-manifest :synthetic-functions}
        selected-expression (or (:expression options) expression)
        binding-var (ns-resolve owner '*p15-s23-stage2-compiler-artifact-binding*)
        record (fn [event] (swap! events conj event))]
    (reset! events [])
    (control 'with-envelope-oracle :passed
      #(control
        'redefine
        {'p15-s23-c6c10-validate-public-context! (fn [value] (record :context) value)
         'p15-s23-stage2-gravity-checked-core-context
         (fn [path text target] {:source-path path :source-text text :requested-target target})
         'p15-s23-c6c10-source-binding!
         (fn [path selected] (record [:source-binding path selected]) source-binding)
         'p15-s23-c6c10-current-reader-program!
         (fn [path selected]
           (record [:reader-program path selected])
           (when (:reader-fails options) (throw (ex-info "Synthetic Reader refusal" {:id "SYNTHETIC-READER"})))
           {:binding (assoc (:binding product) :plan :synthetic-reader-plan)
            :invocation {:reader-source-path reader-path}})
         'c-backend-stage2-plan-emitter-source-rule! (fn [& _] (record :emitter) {:emitter :synthetic-emitter})
         'p15-s23-stage2-compiler-artifact-binding!
         (fn [& args] (record [:expression-owner args]) selected-expression)
         'p15-s23-c6c10-private-ingress-products
         (fn [& args]
           (record [:ingress args (var-get binding-var)])
           (cond-> {:synthetic-ingress true}
             (#{2 3} version) (assoc :source-policy-receipt (control 'receipt product version))))
         'p15-s23-c6c10-private-builder-envelope
         (fn [& _] (record :envelope) {:binding-pins :synthetic-pins})
         'p15-s23-c6c10-invoke-pinned-source-function!
         (fn [& args] (record [:builder args]) {:synthetic-raw-result true})
         'p15-s23-c6c10-seal-digest-request-result!
         (fn [& _] (record :seal) {:status (if (:rejected options) :rejected :accepted)
                                  :graph-proof :synthetic-proof})
         'p15-s23-c6c10-throw-sealed-rejection!
         (fn [& _] (throw (ex-info "Synthetic sealed rejection" {:id "SYNTHETIC-SEALED"})))
         'p15-s23-c6c10-gravity-replay-verification!
         (fn [& _] (record :gravity-replay) {:status :passed :request-count 1
                                             :semantic-authority :synthetic-replay :checks [:synthetic]})
         'p15-s23-c6c10-public-artifact
         (fn [& _] (record :public-artifact) (or (:artifact options) (core version)))}
        f))))

(deftest versioned-construction-keeps-genuine-owner-call-routing-and-receipt-projection
  (doseq [version [2 3]]
    (with-construction version {}
      (fn []
        (let [result (call 'p15-s23-c6c10-fresh-construction context version)
              ingress (first (filter #(and (vector? %) (= :ingress (first %))) @events))
              receipt (:source-policy-receipt result)]
          (is (= ["synthetic.gravity" "synthetic source" reader-path version
                  (:binding (control 'products))] (second ingress)))
          (is (= expression (nth ingress 2)))
          (is (some #{[:expression-owner [:synthetic-emitter "synthetic.gravity" :jvm 2]]} @events))
          (is (= [:gravity-replay :public-artifact] (take-last 2 @events)))
          (is (= version (get-in receipt [:source-interface :version])))
          (is (= :development-unadmitted (:admission-status receipt)))
          (is (false? (:self-hosted? receipt)))
          (is (= (control 'expression-projection) (:expression-binding receipt)))
          (is (not (contains? (:source-binding result) :plan)))
          (is (not (contains? (:source-binding result) :function-manifest)))
          (is (= :synthetic-proof (:graph-proof result)))
          (is (nil? (var-get (ns-resolve owner '*p15-s23-stage2-compiler-artifact-binding*)))))))))

(deftest public-constructor-headers-select-version-before-owner-work
  (doseq [version [2 3]]
    (with-construction version {}
      (fn []
        (let [header {:interface :gravity/c6-c10 :version version}]
          (is (= (core version) (call 'p15-s23-stage2-gravity-checked-core-source-artifact context header)))
          (reset! events [])
          (is (= (core version) (call 'p15-s23-stage2-gravity-checked-core-source-artifact
                                     "synthetic.gravity" "synthetic source" :jvm header)))))))
  (doseq [header [nil {} {:interface :gravity/c11-mir :version 3}
                  {:interface :gravity/c6-c10 :version 1}
                  {:interface :gravity/c6-c10 :version (int 3)}
                  (with-meta {:interface :gravity/c6-c10 :version 3} {:foreign true})]]
    (with-construction 3 {}
      (fn []
        (is (= "C6-VERIFY" (:id (diagnostic #(call 'p15-s23-stage2-gravity-checked-core-source-artifact context header)))))
        (is (empty? @events))))))

(deftest semantic-family-and-expression-metadata-refusals-remain-owning-checks
  (doseq [[version options fact]
          [[2 {:artifact (core 3)} :v2-bool-branch-join-source-family]
           [3 {:artifact (core 2)} :v3-flat-bool-sequence-source-family]
           [3 {:expression (assoc expression :foreign-callback (fn [] :unreachable))} :trusted-current-reader-owner-metadata]]]
    (with-construction version options
      (fn []
        (is (= fact (:missing-fact (diagnostic #(call 'p15-s23-c6c10-fresh-construction context version)))))
        (is (nil? (var-get (ns-resolve owner '*p15-s23-stage2-compiler-artifact-binding*))))))))

(deftest upstream-and-sealed-refusals-precede-replay
  (doseq [[options rule] [[{:reader-fails true} "SYNTHETIC-READER"]
                         [{:rejected true} "SYNTHETIC-SEALED"]]]
    (with-construction 3 options
      (fn []
        (is (= rule (:id (diagnostic #(call 'p15-s23-c6c10-fresh-construction context 3)))))
        (is (not-any? #{:gravity-replay :public-artifact} @events))
        (is (nil? (var-get (ns-resolve owner '*p15-s23-stage2-compiler-artifact-binding*))))))))

(deftest projection-cannot-authenticate-a-declared-owner-oracle
  (with-construction 3 {:expression (assoc expression :source-content-hash "synthetic-owner-lie")}
    (fn []
      ;; The real expression owner authenticates this before construction.
      ;; A synthetic owner can lie; projection must not be reported as proof.
      (let [result (call 'p15-s23-c6c10-fresh-construction context 3)]
        (is (= "synthetic-owner-lie" (get-in result [:source-policy-receipt :expression-binding :source-content-hash])))
        (is (false? (get-in result [:source-policy-receipt :self-hosted?])))))))

(deftest legacy-entrypoints-select-one-and-retain-no-policy
  (doseq [args [[context] ["synthetic.gravity" "synthetic source" :jvm]]]
    (with-construction 1 {}
      (fn []
        (is (= (core 1) (apply call 'p15-s23-stage2-gravity-checked-core-source-artifact args)))
        (is (some #{[:source-binding "synthetic.gravity" 1]} @events))
        (is (not-any? #(and (vector? %) (#{:reader-program :expression-owner} (first %))) @events)))))
  (with-construction 1 {}
    #(is (not (contains? (call 'p15-s23-c6c10-fresh-construction context) :source-policy-receipt)))))

(let [result (run-tests 'c6-fresh-construction-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :fresh-construction-routing-mechanism :header-carrier-family-and-metadata-projection :actual
            :producer-compiler-replay-and-source-owners :declared-oracles
            :actual-core-construction false :source-authentication false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
