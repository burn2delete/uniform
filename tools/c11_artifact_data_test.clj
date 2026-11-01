(ns c11-artifact-data-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.walk :as walk]
            [gravity.digest :as digest]
            [gravity.checked-core.bool-branch-join :as family]))

;; Reuse the existing actual carrier/canonical/policy controls and their
;; explicitly synthetic fixtures. They execute once as this composite's base.
(binding [*ns* *ns*] (load-file "tools/c11_relative_policy_test.clj"))
(def controls (find-ns 'c11-relative-policy-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn evaluate [file predicate] ((ns-resolve controls 'evaluate) file predicate))
(defn named? [names] ((ns-resolve controls 'named?) names))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] ((ns-resolve controls 'diagnostic) f))
(def recomputations (atom 0))
(def verifier {:status :passed :synthetic-unit-verifier true})
(binding [*ns* owner]
  (alias 'walk (ns-name (:ns (meta #'walk/postwalk))))
  (alias 'bool-branch-join (ns-name (:ns (meta #'family/source-family?))))
  (intern owner 'sha256-hex digest/sha256-hex)
  (eval '(def ^:dynamic *p15-s23-c11-mir-diagnostic-context* {}))
  (intern owner 'p15-s23-c11-mir-diagnostic-context (fn [& _] {}))
  (intern owner 'p15-s23-c11-mir-resolve-source-path (constantly "synthetic-c11.gravity"))
  (intern owner 'p15-s23-c11-mir-contain-exception! (fn [_ _ error] (throw error)))
  ;; This is a declared low-level MIR verifier oracle. No fresh Core owner,
  ;; Gravity producer, source compiler or public verifier is invoked.
  (intern owner 'p15-s23-c11-mir-validate-constructed!
          (fn [& _] (swap! recomputations inc) verifier))
  (eval '(declare p15-s23-c11-mir-parameter-facts-valid?)))
(evaluate "../backends/00640-c-backend-fail.clj" (named? '#{c-backend-canonical-value}))
(evaluate "00727-p15-s23-c11-mir-required-functions.clj" (named? '#{p15-s23-c11-mir-digest}))
(evaluate "00721-p15-s23-c6c10-digest-ref-shape.clj" (named? '#{p15-s23-c6c10-canonical-identity}))
(evaluate "00723-p15-s23-c6c10-seal-digest-request-result.clj" (named? '#{p15-s23-c6c10-public-context-keys}))
(evaluate "00725-p15-s23-c6c10-exact-digest-ref-present.clj"
          (named? '#{p15-s23-c6c10-structure-kind p15-s23-c6c10-strict-map-index
                     p15-s23-c6c10-strict-first-mismatch}))
(evaluate "00732-p15-s23-c11-mir-owned-upstream-diagnostic.clj"
          (named? '#{p15-s23-c11-mir-path-neutral-value p15-s23-c11-mir-versioned-record
                     p15-s23-c11-mir-selected-builder-function p15-s23-c11-mir-selected-verifier-function
                     p15-s23-c11-mir-contract-binding}))
(evaluate "00733-p15-s23-c11-mir-source-binding.clj"
          (named? '#{p15-s23-c11-mir-bounded-value! p15-s23-c11-mir-metadata-free?
                     p15-s23-c11-mir-require-strict-structure!}))
(evaluate "00734-p15-s23-c11-mir-runtime-check-fact-record.clj"
          (named? '#{p15-s23-c11-mir-pass-contract-record}))
(evaluate "00735a-p15-s23-c11-mir-entry-parameter.clj" (constantly true))
(evaluate "00735-p15-s23-c11-mir-downstream-ids.clj" (named? '#{p15-s23-c11-mir-module-keys}))
(evaluate "00740-p15-s23-c11-mir-validate-effect-edge-placement.clj"
          (named? '#{p15-s23-c11-mir-v2-source-family!}))
(evaluate "00741-p15-s23-c11-mir-map-blocks.clj"
          #(and ((ns-resolve controls 'definition?) %)
                (not= 'p15-s23-c11-ingress-source-path (second %))))
(evaluate "00742-p15-s23-c11-mir-expected-ingress-semantic.clj" (constantly true))
(evaluate "00743-p15-s23-c11-mir-validate-final-artifact.clj" (constantly true))

(def context {:kind :gravity/p15-s23-stage2-gravity-checked-core-context
              :source-path "synthetic.gravity" :source-text "synthetic"
              :source-content-hash "synthetic-source-hash" :requested-target :jvm})
(def fn-type {:params [] :return :gravity/bool :latent-effects #{} :capabilities #{}
              :throws #{} :ownership-constraints #{:persistent-immutable-shareable}
              :profile-constraints #{:hosted}})
(defn checked-core [version]
  (let [flat [{:node-id "literal" :source-operation :literal :type :gravity/bool
               :path [:functions 'main :body 0 :body 0] :operands [] :attributes {:value true}}
              {:node-id "sequence" :source-operation :do :type :gravity/bool
               :path [:functions 'main :body 0] :operands ["literal"] :attributes {:body-count 1}}
              {:node-id "root" :source-operation :function :type fn-type
               :path [:functions 'main] :operands ["sequence"]}]
        branch (mapv (fn [index op]
                       (cond-> {:node-id (str "node" index) :source-operation op
                                :type (if (= :function op) fn-type :gravity/bool)}
                         (= :literal op) (assoc :attributes {:value true})))
                     (range) [:literal :truthy :literal :literal :if :function])]
    {:kind :gravity/p15-s23-stage2-gravity-checked-core-artifact
     :artifact-id "synthetic-core-id" :core-nodes (if (= 2 version) branch flat)
     :lexical-binding-records [] :root-node-ids [(if (= 2 version) "node5" "root")]}))
(defn policy [core version]
  (let [record ((ns-resolve controls 'fixture) version)]
    (assoc record :source-policy-binding-id
           (call 'p15-s23-c6c10-canonical-digest
                 {:domain (if (= 3 version) :gravity/c6-c10-source-policy-v3 :gravity/c6-c10-source-policy-v2)
                  :artifact-id (:artifact-id core) :context context :policy (:source-policy-receipt record)}))))
(defn raw-mir [core]
  (merge (zipmap @(ns-resolve owner 'p15-s23-c11-mir-module-keys) (repeat nil))
         {:artifact :gravity/mir-module :schema-version 1 :source-core (:artifact-id core)
          :module-id "synthetic-mir" :diagnostics []
          :pass-contract (call 'p15-s23-c11-mir-pass-contract-record)
          :functions {'main {:blocks {"entry" {:instructions [{:op-id "literal" :verifier-status :pending}]
                                                :terminator {:opcode :return :verifier-status :pending}}}}}}))
(defn artifact [version]
  (let [core (checked-core version)
        ingress (cond-> (call 'p15-s23-c11-mir-expected-ingress-semantic core context)
                  (not= 1 version) (assoc :schema-version version :source-policy (policy core version)))
        binding (assoc (call 'p15-s23-c11-mir-contract-binding version) :source-path "synthetic-c11.gravity")]
    (call 'p15-s23-c11-mir-final-artifact-base core context ingress binding (raw-mir core) verifier version)))
(defn validate [candidate version]
  (call 'p15-s23-c11-mir-validate-final-artifact! candidate (checked-core version) context version))
(defn rehash [candidate version]
  (let [core (checked-core version) id (call 'p15-s23-c11-mir-recomputed-id candidate)
        candidate (assoc candidate :mir-id id
                         :artifact-id (call 'p15-s23-c11-mir-digest
                                            {:kind (:kind candidate) :schema-version (:schema-version candidate) :mir-id id}))]
    (assoc candidate :actual-path-binding-id
           (call 'p15-s23-c11-mir-recomputed-actual-path-binding-id candidate core context))))

(deftest versioned-relative-finalization-and-verifier-recomputation
  (doseq [version [1 2 3] :let [candidate (artifact version)]]
    (reset! recomputations 0)
    (is (= :passed (validate candidate version)))
    (is (= 1 @recomputations))
    (is (= version (:schema-version candidate)))
    (is (= :public-verifier-required (get-in candidate [:construction-record :semantic-replay-parity])))
    (is (false? (get-in candidate [:construction-record :live-invocation-claim?])))
    (is (= :passed (get-in candidate [:mir-module :functions 'main :blocks "entry" :terminator :verifier-status])))
    (when (not= 1 version)
      (let [expected (call 'p15-s23-c11-mir-core-policy-binding version (get-in candidate [:checked-core-ingress :source-policy]))]
        (is (= expected (get-in candidate [:verification-report :checked-core-source-policy-binding])))
        (is (= expected (get-in candidate [:mir-module :pass-execution-record :checked-core-source-policy-binding])))
        (is (= expected (get-in candidate [:b1-preflight :binding :checked-core-source-policy-binding])))))))

(deftest legacy-helper-arities-equal-explicit-version-one
  (let [core (checked-core 1) candidate (artifact 1)]
    (is (= (call 'p15-s23-c11-mir-scope-record) (call 'p15-s23-c11-mir-scope-record 1)))
    (is (= (call 'p15-s23-c11-independent-verifier-record verifier)
           (call 'p15-s23-c11-independent-verifier-record verifier 1)))
    (is (= candidate (call 'p15-s23-c11-mir-final-artifact-base
                          core context (:checked-core-ingress candidate)
                          (assoc (call 'p15-s23-c11-mir-contract-binding 1) :source-path "synthetic-c11.gravity")
                          (raw-mir core) verifier)))
    (is (= :passed (call 'p15-s23-c11-mir-validate-final-artifact! candidate core context)))))

(deftest semantic-family-rejects-invalid-topology-and-preserves-version-scope
  (is (family/source-family? (checked-core 2) false))
  (is (family/flat-sequence-source-family? (checked-core 3)))
  (doseq [change [#(assoc-in % [:core-nodes 0 :attributes :value] 1)
                  #(assoc-in % [:core-nodes 1 :operands] ["wrong"])
                  #(assoc-in % [:core-nodes 0 :path] [:wrong])
                  #(assoc % :resolved-parameter {})]]
    (is (not (family/flat-sequence-source-family? (change (checked-core 3))))))
  (is (= :closed-flat-bool-literal-sequence (:supported-source-family (call 'p15-s23-c11-mir-scope-record 3)))))

(deftest artifact-policy-source-and-provenance-tampering-rejects
  (doseq [version [2 3] mutate [#(assoc % :schema-version 1)
                              #(assoc-in % [:source-rule :source-content-hash] "changed")
                              #(assoc-in % [:checked-core-ingress :source-policy :source-policy-binding-id] "changed")
                              #(assoc-in % [:scope :whole-c11?] true)
                              #(assoc-in % [:construction-record :live-invocation-claim?] true)
                              #(assoc-in % [:provenance :actual-paths :source] "changed")
                              #(assoc % :actual-path-binding-id "changed")]]
    (is (contains? #{"C11-MODULE" "C11-VERIFY" "C11-ORIGIN"}
                   (:id (diagnostic #(validate (mutate (artifact version)) version)))))))

(deftest typed-collection-parity-and-recomputed-verifier-cannot-be-rehashed-away
  (let [candidate (artifact 3)
        changed (-> candidate
                    (assoc-in [:mir-module :pass-execution-record :diagnostics] '())
                    (rehash 3))]
    (is (= :type-sensitive-content-bound-build-mir-pass-execution
           (:missing-fact (diagnostic #(validate changed 3))))))
  (let [candidate (rehash (assoc-in (artifact 3) [:verification-report :status] :fabricated) 3)]
    (reset! recomputations 0)
    (is (= :independently-recomputed-verifier-report
           (:missing-fact (diagnostic #(validate candidate 3)))))
    (is (= 1 @recomputations))))

(deftest final-artifact-native-carrier-checks-precede-payload-and-verifier
  (let [callbacks (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! callbacks inc) {})
                  (hashCode [] (swap! callbacks inc) 0)
                  (equals [_] (swap! callbacks inc) true)
                  (toString [] (swap! callbacks inc) "foreign"))
        comparator (reify java.util.Comparator
                     (compare [_ _ _] (swap! callbacks inc) 0))
        candidate (artifact 3)]
    (reset! recomputations 0)
    (doseq [bad [foreign (with-meta candidate {:caller true})
                 (assoc candidate :foreign foreign)
                 (assoc candidate :foreign (sorted-map-by comparator))]]
      (is (= :trusted-comparator-free-c11-carrier
             (:missing-fact (diagnostic #(validate bad 3))))))
    (is (zero? @callbacks))
    (is (zero? @recomputations))))

(let [result (run-tests 'c11-artifact-data-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :relative-artifact-data-only :carrier-digests-and-zipper :actual
            :fixture-authority :synthetic :low-level-mir-verifier :declared-oracle
            :source-authentication false :gravity-replay false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
