(ns c11-core-authentication-test
  (:require [clojure.test :refer [deftest is run-tests]]))

(binding [*ns* *ns*] (load-file "tools/c11_relative_policy_test.clj"))
(def controls (find-ns 'c11-relative-policy-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn control [name & args] (apply (ns-resolve controls name) args))
(defn redefine [functions f]
  (with-redefs-fn (into {} (map (fn [[name function]] [(ns-resolve owner name) function]) functions)) f))
(defn evaluate [file names] (control 'evaluate file (control 'named? names)))
(binding [*ns* owner]
  (eval '(def ^:dynamic *p15-s23-c11-upstream-diagnostic-owner* nil))
  (eval '(def ^:dynamic *p15-s23-c11-mir-diagnostic-context* nil))
  (eval '(declare p15-s23-stage2-gravity-checked-core-verification-report
                  p15-s23-stage2-closed-checked-core-verification-report
                  p15-s23-c11-mir-contain-checked-core-exception!
                  p15-s23-c11-mir-diagnostic-context
                  __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier)))
(evaluate "00723-p15-s23-c6c10-seal-digest-request-result.clj" '#{p15-s23-c6c10-public-context-keys})
(evaluate "00741-p15-s23-c11-mir-map-blocks.clj"
          '#{p15-s23-c11-legacy-effectful-context-keys p15-s23-c11-ingress-map-classes p15-s23-c11-exact-bounded-map?})
(evaluate "00742-p15-s23-c11-mir-expected-ingress-semantic.clj" '#{p15-s23-c11-mir-expected-ingress-semantic})
(doseq [file ["00744-opaque-c11-construction-token-authority-holder.clj"
              "00744-opaque-c11-construction-token-helper-01-classify-checked-core-ingress-pair.clj"
              "00744-opaque-c11-construction-token-helper-02-invoke-checked-core-verifier.clj"
              "00744-opaque-c11-construction-token-helper-03-authenticate-checked-core-ingress.clj"]]
  (control 'evaluate file (constantly true)))
(def context {:kind :gravity/p15-s23-stage2-gravity-checked-core-context :source-path "synthetic.gravity"
              :source-text "synthetic" :source-content-hash "synthetic-input-hash" :requested-target :jvm})
(def core {:kind :gravity/p15-s23-stage2-gravity-checked-core-artifact :artifact-id "synthetic-core"})
(def events (atom []))
(defn fixture-report [version]
  (let [policy (control 'fixture (if (= 1 version) 2 version))]
    (assoc policy :status :passed
           :source-binding (:compiler-program policy)
           :source-policy-binding-id
           (call 'p15-s23-c6c10-canonical-digest
                 {:domain (if (= 3 version) :gravity/c6-c10-source-policy-v3 :gravity/c6-c10-source-policy-v2)
                  :artifact-id (:artifact-id core) :context context :policy (:source-policy-receipt policy)})
           :gravity-source-verification {:status :passed :semantic-authority :gravity-source}
           :gravity-candidate-verification {:status :passed :semantic-authority :gravity-source})))
(defn with-owners [version options f]
  (let [projection (var-get (ns-resolve owner 'p15-s23-c11-mir-core-policy-projection))]
    (reset! events [])
    (redefine
     {'p15-s23-stage2-gravity-checked-core-verification-report
      (fn [& args]
        (swap! events conj [:fresh-core args (var-get (ns-resolve owner '*p15-s23-c11-upstream-diagnostic-owner*))])
        ((or (:report-change options) identity) (fixture-report version)))
      'p15-s23-stage2-closed-checked-core-verification-report
      (fn [& args] (swap! events conj [:effectful-owner args]) {:mode :effectful-reference :status :passed})
      'p15-s23-c11-mir-core-policy-projection
      (fn [& args] (swap! events conj :policy-projection) (apply projection args))
      'p15-s23-c11-mir-diagnostic-context (constantly {:synthetic-diagnostic-context true})
      'p15-s23-c11-mir-contain-checked-core-exception!
      (fn [_ boundary error] (throw (ex-info "Synthetic diagnostic containment"
                                             (assoc (ex-data error) :boundary boundary) error)))}
     f)))
(defn authenticate [version]
  (call '__gravity_bootstrap_lexical_119416_authenticate-checked-core-ingress core context :synthetic-boundary version))

(deftest lexical-authentication-calls-fresh-core-owner-before-policy-projection
  (doseq [version [2 3]]
    (with-owners version {}
      (fn []
        (let [first-result (authenticate version) second-result (authenticate version)
              owner-calls (filter vector? @events)]
          (is (= first-result second-result))
          (is (= 2 (count owner-calls)))
          (is (= [core context {:interface :gravity/c6-c10 :version version}] (second (first owner-calls))))
          (is (= :policy-projection (second @events)))
          (is (= version (:schema-version first-result)))
          (is (= version (get-in first-result [:source-policy :source-interface :version])))
          (is (= :development-unadmitted (get-in first-result [:source-policy :admission-status])))
          (is (false? (get-in first-result [:source-policy :source-policy-receipt :self-hosted?])))
          (is (identical? (nth (var-get (ns-resolve owner '__gravity_bootstrap_lexical_values_119416)) 1)
                          (nth (first owner-calls) 2)))
          (is (nil? (var-get (ns-resolve owner '*p15-s23-c11-upstream-diagnostic-owner*)))))))))

(deftest report-authority-refusal-precedes-policy-projection
  (doseq [change [#(assoc % :status :failed)
                  #(assoc-in % [:gravity-source-verification :semantic-authority] :host-seed)
                  #(assoc-in % [:gravity-candidate-verification :status] :failed)]]
    (with-owners 3 {:report-change change}
      (fn []
        (is (= :gravity-source-checked-core-semantic-authority
               (:missing-fact (control 'diagnostic #(authenticate 3)))))
        (is (= 1 (count @events)))))))

(deftest fresh-report-policy-substitution-refuses-at-actual-data-consumer
  (doseq [change [#(assoc-in % [:source-interface :version] 2)
                  #(assoc-in % [:source-policy-receipt :expression-binding :source-content-hash] "forged")
                  #(assoc % :source-policy-binding-id "crossed-core-context-binding")]]
    (with-owners 3 {:report-change change}
      (fn []
        (is (= :current-core-source-policy-data (:missing-fact (control 'diagnostic #(authenticate 3)))))
        (is (= :policy-projection (last @events)))))))

(deftest foreign-carrier-refusal-precedes-fresh-owner-and-policy
  (with-owners 3 {}
    (fn []
      (is (some? (control 'diagnostic
                         #(call '__gravity_bootstrap_lexical_119416_authenticate-checked-core-ingress
                                (assoc core :foreign (fn [] :unreachable)) context :synthetic-boundary 3))))
      (is (empty? @events)))))

(deftest versioned-effectful-ingress-refuses-before-legacy-owner
  (with-owners 3 {}
    (fn []
      (is (= :current-core-pure-ingress-only
             (:missing-fact (control 'diagnostic
                                    #(call '__gravity_bootstrap_lexical_119416_invoke-checked-core-verifier
                                           core context :effectful-reference :synthetic-boundary 3)))))
      (is (empty? @events)))))

(deftest legacy-overload-and-lexical-capture-retain-existing-routing
  (with-owners 1 {}
    (fn []
      (let [result (call '__gravity_bootstrap_lexical_119416_authenticate-checked-core-ingress
                         core context :synthetic-boundary)]
        (is (= 1 (:schema-version result)))
        (is (not (contains? result :source-policy)))
        (is (= [core context] (second (first @events)))))))
  (with-owners 3 {}
    #(redefine {'__gravity_bootstrap_lexical_119416_invoke-checked-core-verifier
                 (fn [& _] (throw (ex-info "Replaced Var must not replace captured helper" {})))}
       (fn [] (is (= 3 (:schema-version (authenticate 3))))))))

(let [result (run-tests 'c11-core-authentication-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :lexical-core-authentication-routing-and-policy-data
            :lexical-helpers-native-carriers-and-policy-validation :actual
            :fresh-core-owner-and-diagnostic-transport :declared-oracles
            :source-authentication false :gravity-execution false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
