(ns c11-public-interface-test
  (:require [clojure.test :refer [deftest is run-tests]]))

(binding [*ns* *ns*] (load-file "tools/c11_pinned_construction_test.clj"))
(def controls (find-ns 'c11-pinned-construction-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn control [name & args] (apply (ns-resolve controls name) args))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] (control 'diagnostic f))
(binding [*ns* owner]
  (eval '(def ^:dynamic *p15-s23-c11-upstream-diagnostic-owner* nil))
  (eval '(declare p15-s23-stage2-gravity-checked-core-verification-report
                  p15-s23-stage2-closed-checked-core-verification-report
                  p15-s23-c11-mir-contain-checked-core-exception!
                  __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier)))
(doseq [file ["00744-opaque-c11-construction-token-helper-02-invoke-checked-core-verifier.clj"
              "00744-opaque-c11-construction-token-helper-03-authenticate-checked-core-ingress.clj"
              "00744-opaque-c11-construction-token-definitions.clj"]]
  ;; Keep the public definition's outer let intact: it captures the helpers.
  (doseq [form ((ns-resolve 'c11-relative-policy-test 'forms) file)]
    (binding [*ns* owner] (eval form))))
;; Apply the unchanged cleanup form with only its namespace target adapted to
;; this test owner. No gravity.bootstrap namespace or production token exists.
(let [source (slurp "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00744-opaque-c11-construction-token-cleanup.clj")]
  (binding [*ns* owner]
    (eval (read-string (.replace ^String source "gravity.bootstrap" (str (ns-name owner)))))))

(def core-events (atom []))
(defn with-public [version options f]
  (reset! core-events [])
  (control 'with-producers version options
    (fn [core ingress expected recomputations]
      (control 'redefine
        {'p15-s23-stage2-gravity-checked-core-verification-report
         (fn [& args]
           (swap! core-events conj args)
           ((or (:core-report-change options) identity)
            (merge (:source-policy ingress)
                   {:status :passed :source-binding (get-in ingress [:source-policy :compiler-program])
                    :gravity-source-verification {:status :passed :semantic-authority :gravity-source}
                    :gravity-candidate-verification {:status :passed :semantic-authority :gravity-source}})))
         'p15-s23-stage2-closed-checked-core-verification-report
         (fn [& _] (throw (ex-info "Legacy effectful owner not selected by these fixtures" {})))
         'p15-s23-c11-mir-contain-checked-core-exception!
         (fn [_ boundary error] (throw (ex-info "Synthetic upstream diagnostic transport"
                                                (assoc (ex-data error) :boundary boundary) error)))}
        #(f core expected recomputations)))))
(defn header [version] {:interface :gravity/c11-mir :version version})
(defn runtime-events [] @(var-get (ns-resolve controls 'events)))

(deftest public-versioned-construction-and-separate-report-retain-real-lexical-chain
  (doseq [version [2 3]]
    (with-public version {}
      (fn [core expected _]
        (let [context (var-get (ns-resolve controls 'context))
              actual (call 'p15-s23-stage2-c11-mir-artifact core context (header version))
              report (call 'p15-s23-stage2-c11-mir-verification-report actual core context (header version))
              gravity-calls (filter #(= :gravity (first %)) (runtime-events))]
          (is (= expected actual))
          (is (= :passed (:status report)))
          (is (= :passed (:semantic-replay-parity report)))
          (is (= version (get-in report [:checked-core-ingress :schema-version])))
          (is (= (get-in actual [:checked-core-ingress :source-policy])
                 (get-in report [:checked-core-ingress :source-policy])))
          (is (= 2 (count @core-events)))
          (is (every? #(= [core context {:interface :gravity/c6-c10 :version version}] %) @core-events))
          (is (= (if (= 3 version) 4 2) (count gravity-calls)))
          (is (= 2 (count (filter #(= :source-binding (first %)) (runtime-events)))))
          (is (= :not-claimed (:execution-count report)))
          (is (false? (:live-invocation-claim? report)))
          (is (= :development-unadmitted (get-in report [:checked-core-ingress :source-policy :admission-status]))))))))

(deftest native-header-and-mixed-artifact-refusals-precede-owner-work
  (doseq [version [2 3]]
    (with-public version {}
      (fn [core artifact _]
        (let [context (var-get (ns-resolve controls 'context))]
          (doseq [bad [nil {} {:interface :gravity/c11-mir :version (int version)}
                       {:interface :gravity/c6-c10 :version version}]]
            (is (some? (diagnostic #(call 'p15-s23-stage2-c11-mir-artifact core context bad))))
            (is (some? (diagnostic #(call 'p15-s23-stage2-c11-mir-verification-report artifact core context bad)))))
          (is (some? (diagnostic #(call 'p15-s23-stage2-c11-mir-verification-report
                                       artifact core context (header (if (= 2 version) 3 2))))))
          (is (some? (diagnostic #(call 'p15-s23-stage2-c11-mir-verification-report artifact core context))))
          (is (empty? @core-events))
          (is (empty? (runtime-events))))))))

(deftest actual-public-consumer-rejects-crossed-core-policy-before-building
  (doseq [change [#(assoc-in % [:source-interface :version] 2)
                  #(assoc-in % [:source-policy-receipt :expression-binding :source-content-hash] "forged")
                  #(assoc % :source-policy-binding-id "crossed-context")]]
    (with-public 3 {:core-report-change change}
      (fn [core _ _]
        (is (= :current-core-source-policy-data
               (:missing-fact (diagnostic #(call 'p15-s23-stage2-c11-mir-artifact
                                                core (var-get (ns-resolve controls 'context)) (header 3))))))
        (is (= 1 (count @core-events)))
        (is (empty? (runtime-events)))))))

(deftest forged-supplied-verifier-report-does-not-become-public-acceptance-after-rehash
  (with-public 3 {}
    (fn [core artifact _]
      (let [candidate (control 'control 'rehash (assoc-in artifact [:verification-report :status] :fabricated) 3)]
        (is (= :independently-recomputed-verifier-report
               (:missing-fact (diagnostic #(call 'p15-s23-stage2-c11-mir-verification-report
                                                candidate core (var-get (ns-resolve controls 'context)) (header 3))))))
        (is (= 1 (count @core-events)))
        (is (empty? (runtime-events)))))))

(deftest crossed-core-context-pairs-reject-before-source-binding-or-replay
  (doseq [crossed [:core :context]]
    (with-public 3 {}
      (fn [core _ _]
        (let [context (var-get (ns-resolve controls 'context))
              candidate-core (cond-> core (= :core crossed) (assoc :artifact-id "crossed-core"))
              candidate-context (cond-> context (= :context crossed) (assoc :source-path "crossed.gravity"))]
          (is (= :current-core-source-policy-data
                 (:missing-fact (diagnostic #(call 'p15-s23-stage2-c11-mir-artifact
                                                  candidate-core candidate-context (header 3))))))
          (is (= 1 (count @core-events)))
          (is (empty? (runtime-events))))))))

(deftest changed-gravity-replay-refuses-at-public-parity-check
  (with-public 3 {:builder-change #(assoc % :module-id "changed-replay")}
    (fn [core artifact _]
      (is (= :independent-gravity-replay-semantic-parity
             (:missing-fact (diagnostic #(call 'p15-s23-stage2-c11-mir-verification-report
                                              artifact core (var-get (ns-resolve controls 'context)) (header 3))))))
      (is (= 1 (count @core-events)))
      (is (= 2 (count (filter #(= :gravity (first %)) (runtime-events))))))))

(deftest cleanup-retains-public-closures-without-temporary-owner-vars
  (doseq [name '[__gravity_bootstrap_lexical_values_119416
                 __gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair
                 __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier
                 __gravity_bootstrap_lexical_119416_authenticate-checked-core-ingress
                 __gravity_bootstrap_lexical_119416_invoke-pinned-c11-builder
                 __gravity_bootstrap_lexical_119416_construct-authenticated-c11-mir]]
    (is (nil? (ns-resolve owner name))))
  (with-public 3 {}
    (fn [core artifact _]
      (let [context (var-get (ns-resolve controls 'context))]
        (is (= :passed (call 'p15-s23-stage2-c11-mir-verify! artifact core context (header 3))))
        (is (true? (call 'p15-s23-stage2-c11-mir-authentic? artifact core context (header 3))))
        (is (false? (call 'p15-s23-stage2-c11-mir-authentic? artifact core context {})))
        (is (false? (call 'p15-s23-stage2-c11-mir-authentic? artifact)))))))

(deftest unversioned-public-overloads-retain-legacy-one
  (with-public 1 {}
    (fn [core expected _]
      (let [context (var-get (ns-resolve controls 'context))
            actual (call 'p15-s23-stage2-c11-mir-artifact core context)
            report (call 'p15-s23-stage2-c11-mir-verification-report actual core context)]
        (is (= expected actual))
        (is (= :passed (:status report)))
        (is (every? #(= [core context] %) @core-events))
        (is (not (contains? report :source-interface)))))))

(let [result (run-tests 'c11-public-interface-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :public-c11-versioned-lexical-routing-and-relative-replay
            :public-and-private-closures-headers-carriers-policy-and-final-artifact :actual
            :fresh-core-source-binding-gravity-runtime-and-low-level-verifier :declared-oracles
            :fixtures :synthetic :source-authentication false :gravity-execution false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
