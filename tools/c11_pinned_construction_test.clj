(ns c11-pinned-construction-test
  (:require [clojure.test :refer [deftest is run-tests]]))

(binding [*ns* *ns*] (load-file "tools/c11_artifact_data_test.clj"))
(def controls (find-ns 'c11-artifact-data-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn control [name & args] (apply (ns-resolve controls name) args))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] (control 'diagnostic f))
(defn evaluate [file predicate] (control 'evaluate file predicate))
(defn named? [names] (control 'named? names))
(defn redefine [functions f]
  (with-redefs-fn (into {} (map (fn [[name function]] [(ns-resolve owner name) function]) functions)) f))
(binding [*ns* owner]
  (eval '(declare p15-s23-stage2-runtime-execute-function p15-s23-c11-mir-source-binding!)))
(evaluate "00740-p15-s23-c11-mir-validate-effect-edge-placement.clj" (named? '#{p15-s23-c11-mir-verify-flat-produced!}))
(doseq [file ["00744-opaque-c11-construction-token-authority-holder.clj"
              "00744-opaque-c11-construction-token-helper-01-classify-checked-core-ingress-pair.clj"
              "00744-opaque-c11-construction-token-helper-04-invoke-pinned-c11-builder.clj"
              "00744-opaque-c11-construction-token-helper-05-construct-authenticated-c11-mir.clj"]]
  (evaluate file (constantly true)))
(def context (var-get (ns-resolve controls 'context)))
(def token (first (var-get (ns-resolve owner '__gravity_bootstrap_lexical_values_119416))))
(def events (atom []))
(def checks [:single-function-linear :blocks-terminators-and-cfg-edges
             :same-block-definitions-uses-and-return-dominance :type-fact-preservation
             :effect-order-preservation :ownership-capability-and-profile-preservation
             :safety-outcome-preservation :source-origin-preservation])
(def preserves [:type-facts :effect-facts :ownership-facts :capability-facts :safety-facts
                :profile-target-facts :source-spans :source-origin-map :generated-origin-chains
                :control-flow-edges :data-flow-edges :dominance-facts])
(defn gravity-report [core mir]
  {:artifact :gravity/sh12-branch-join-mir-report :schema-version 1 :phase :verification
   :scope :closed-flat-bool-literal-sequence :status :passed :diagnostics []
   :checked-core-artifact-id (:artifact-id core) :mir-module-id (:module-id mir)
   :checks checks :preserves preserves
   :nonclaims [:generic-c11-verifier :independent-acceptance :optimizer-credit :backend-credit :self-hosted?]})
(defn with-producers [version options f]
  (let [core (control 'checked-core version)
        expected (control 'artifact version)
        source-binding (merge (call 'p15-s23-c11-mir-contract-binding version)
                              {:source-path "synthetic-c11.gravity" :plan {:synthetic-version version}})
        raw (control 'raw-mir core)
        recomputations (var-get (ns-resolve controls 'recomputations))]
    (reset! events [])
    (reset! recomputations 0)
    (redefine
     {'p15-s23-c11-mir-source-binding!
      (fn [& args]
        (swap! events conj [:source-binding args])
        ((or (:binding-change options) identity) source-binding))
      'p15-s23-stage2-runtime-execute-function
      (fn [engine plan function args]
        (swap! events conj [:gravity engine plan function args])
        (if (= function (call 'p15-s23-c11-mir-selected-verifier-function 3))
          ((or (:report-change options) identity) (gravity-report (first args) (second args)))
          ((or (:builder-change options) identity) raw)))}
     #(f core (:checked-core-ingress expected) expected recomputations))))
(defn construct [core ingress version]
  (call '__gravity_bootstrap_lexical_119416_construct-authenticated-c11-mir core context ingress token version))

(deftest selected-builder-and-flat-verifier-feed-actual-relative-finalization
  (doseq [version [1 2 3]]
    (with-producers version {}
      (fn [core ingress expected recomputations]
        (let [actual (construct core ingress version)
              invocations (filter #(= :gravity (first %)) @events)]
          (is (= expected actual))
          (is (= [:source-binding [(get context :source-path) :jvm version]] (first @events)))
          (is (= (if (= 3 version) 2 1) (count invocations)))
          (is (= (call 'p15-s23-c11-mir-selected-builder-function version) (nth (first invocations) 3)))
          (when (= 3 version)
            (is (= (call 'p15-s23-c11-mir-selected-verifier-function 3) (nth (second invocations) 3)))
            (is (= [core (control 'raw-mir core)] (nth (second invocations) 4))))
          (is (= 2 @recomputations))
          (is (= :public-verifier-required (get-in actual [:construction-record :semantic-replay-parity])))
          (is (false? (get-in actual [:construction-record :live-invocation-claim?]))))))))

(deftest wrong-construction-token-refuses-before-source-or-runtime-owners
  (with-producers 3 {}
    (fn [core ingress _ _]
      (is (= :opaque-c11-constructor-entry
             (:missing-fact (diagnostic #(call '__gravity_bootstrap_lexical_119416_construct-authenticated-c11-mir
                                              core context ingress (Object.) 3)))))
      (is (= :opaque-c11-construction-token
             (:missing-fact (diagnostic #(call '__gravity_bootstrap_lexical_119416_invoke-pinned-c11-builder
                                              core context {} :synthetic-mode (Object.))))))
      (is (empty? @events)))))

(deftest selected-gravity-report-substitutions-refuse-before-host-finalization
  (doseq [change [#(assoc % :status :failed) #(assoc % :scope :other-family)
                  #(assoc % :checked-core-artifact-id "crossed-core") #(assoc % :mir-module-id "crossed-mir")
                  #(assoc % :checks []) #(assoc % :preserves []) #(assoc % :nonclaims [])
                  #(assoc % :diagnostics [:unexpected]) #(assoc % :extra true)]]
    (with-producers 3 {:report-change change}
      (fn [core ingress _ recomputations]
        (is (= :actual-flat-gravity-verifier-report
               (:missing-fact (diagnostic #(construct core ingress 3)))))
        (is (zero? @recomputations))))))

(deftest builder-carrier-and-declared-rejection-stop-before-verifier
  (doseq [[change fact]
          [[#(assoc % :foreign (fn [] :unreachable)) :trusted-comparator-free-c11-carrier]
           [(constantly {:status :rejected :missing-fact :synthetic-builder-rejection}) :synthetic-builder-rejection]]]
    (with-producers 3 {:builder-change change}
      (fn [core ingress _ recomputations]
        (let [result (diagnostic #(construct core ingress 3))]
          (is (= fact (:missing-fact result)))
          (is (= 1 (count (filter #(= :gravity (first %)) @events))))
          (is (zero? @recomputations)))))))

(deftest source-binding-tamper-is-not-accepted-by-final-artifact-validation
  (with-producers 3 {:binding-change #(assoc % :source-content-hash "changed-source")}
    (fn [core ingress _ _]
      (is (some? (diagnostic #(construct core ingress 3)))))))

(deftest legacy-overload-retains-explicit-one-behavior
  (with-producers 1 {}
    (fn [core ingress expected _]
      (is (= expected (call '__gravity_bootstrap_lexical_119416_construct-authenticated-c11-mir
                            core context ingress token)))
      (is (= 1 (last (second (first @events))))))))

(let [result (run-tests 'c11-pinned-construction-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :pinned-construction-and-relative-artifact-mechanisms
            :lexical-token-dispatch-carrier-report-and-final-artifact-checks :actual
            :source-binding-gravity-runtime-and-low-level-mir-verifier :declared-oracles
            :fixtures :synthetic :source-authentication false :gravity-execution false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
