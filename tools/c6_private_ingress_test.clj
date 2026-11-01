(ns c6-private-ingress-test
  (:require [clojure.test :refer [deftest is run-tests]]))

(binding [*ns* *ns*] (load-file "tools/c6_reader_policy_test.clj"))
(def controls (find-ns 'c6-reader-policy-test))
(def owner (var-get (ns-resolve controls 'owner)))
(defn control [name & args] (apply (ns-resolve controls name) args))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f] (control 'diagnostic f))
(binding [*ns* owner]
  (eval '(declare qst-or-gravity-source? reader-project-context-for-source
                  p15-s23-stage2-c2-c3-front-end-products p15-s23-c6c10-literal-authentication
                  p15-s23-c6c10-project-front-end validate-ns-syntax! parse-module
                  stage0-function-table p15-s23-c6c10-resolved-parameter
                  p15-s23-stage2-compiler-artifact-plan p15-s23-c6c10-private-stage2-plan
                  p15-s23-c6c10-project-plan p15-s23-c6c10-project-authoritative-module
                  p15-s23-c6c10-carrier-validation c-backend-stage2-plan-emitter-source-rule!)))
(control 'evaluate "00716-p15-s23-c6c10-normalize-deferred-ratios-in-plan.clj"
         (control 'named? '#{p15-s23-c6c10-private-ingress-products}))
(def reader-path (var-get (ns-resolve controls 'reader-path)))
(def events (atom []))
(defn with-ingress [options f]
  (let [product (control 'products)
        raw (assoc (:raw product) :source-unit-record {:bytes-hash "synthetic-input-hash"}
                   :forms [:synthetic-forms])
        envelope-owner (var-get (ns-resolve owner 'p15-s23-c6c10-current-reader-envelope-state!))
        step (fn [tag value] (fn [& _] (swap! events conj tag) value))]
    (reset! events [])
    (control
     'with-envelope-oracle :passed
     #(control
       'redefine
       {'qst-or-gravity-source? (fn [path] (.endsWith ^String path ".gravity"))
        'sh03-reader-resolve-source-path (constantly reader-path)
        'reader-project-context-for-source (step :project-context :synthetic-project-context)
        'p15-s23-stage2-c2-c3-front-end-products
        (fn [& args]
          (swap! events conj [:front-end args])
          (when (:front-end-fails options) (throw (ex-info "Synthetic upstream refusal" {:id "SYNTHETIC-C2"})))
          raw)
        'p15-s23-c6c10-literal-authentication (step :literal-authentication :synthetic-literal-authentication)
        'p15-s23-c6c10-project-front-end (step :front-end-projection {:synthetic-front-end true})
        'validate-ns-syntax! (step :syntax-validation nil)
        'parse-module (step :module-parse :synthetic-module)
        'stage0-function-table (step :function-table {'main {:arity (or (:arity options) 0)}})
        'p15-s23-c6c10-resolved-parameter (step :parameter :synthetic-parameter)
        'c-backend-stage2-plan-emitter-source-rule! (step :emitter-owner {:emitter :synthetic-emitter
                                                                        :source-rule-hash "synthetic-rule-hash"
                                                                        :rule-record {:synthetic-rule true}})
        'p15-s23-c6c10-private-stage2-plan (step :zero-arity-plan {:plan-id "synthetic-plan"})
        'p15-s23-stage2-compiler-artifact-plan (step :parameter-plan {:plan-id "synthetic-plan"})
        'p15-s23-c6c10-project-plan (step :plan-projection {:synthetic-plan true})
        'p15-s23-c6c10-project-authoritative-module (step :module-projection {:synthetic-module true})
        'p15-s23-c6c10-carrier-validation
        (fn [& _]
          (swap! events conj :carrier-validation)
          (when (:carrier-fails options) (throw (ex-info "Synthetic projection refusal" {:id "SYNTHETIC-CARRIER"})))
          :synthetic-carrier-validation)
        'p15-s23-c6c10-current-reader-envelope-state!
        (fn [& args] (swap! events conj :envelope-owner) (apply envelope-owner args))}
       (fn [] (f product))))))

(deftest versioned-ingress-consumes-one-real-envelope-policy-helper-after-projections
  (doseq [version [2 3] arity [0 1]]
    (with-ingress
      {:arity arity}
      (fn [product]
        (let [result (call 'p15-s23-c6c10-private-ingress-products
                           "synthetic.gravity" "synthetic source" reader-path version (:binding product))
              receipt (:source-policy-receipt result)
              front (filter #(and (vector? %) (= :front-end (first %))) @events)]
          (is (= 1 (count front)))
          (is (= ["synthetic.gravity" "synthetic source" :synthetic-project-context true reader-path]
                 (second (first front))))
          (is (= :envelope-owner (last @events)))
          (is (< (.indexOf @events :carrier-validation) (.indexOf @events :envelope-owner)))
          (is (= version (get-in receipt [:source-interface :version])))
          (is (= :development-unadmitted (:admission-status receipt)))
          (is (false? (:self-hosted? receipt)))
          (is (= (pos? arity) (contains? result :resolved-parameter)))
          (is (= 1 (count (filter #{(if (pos? arity) :parameter-plan :zero-arity-plan)} @events))))
          (is (not-any? #{(if (pos? arity) :zero-arity-plan :parameter-plan)} @events)))))))

(deftest invocation-version-path-refusals-precede-front-end-work
  (doseq [[path version fact]
          [[reader-path 1 :selected-current-reader-invocation-policy]
           [reader-path (int 3) :explicit-c6-source-contract-version]
           [reader-path 4 :explicit-c6-source-contract-version]
           [nil 3 :selected-current-reader-invocation-policy]
           ["other-reader.gravity" 3 :selected-current-reader-invocation-policy]]]
    (with-ingress {}
      (fn [product]
        (is (= fact (:missing-fact (diagnostic #(call 'p15-s23-c6c10-private-ingress-products
                                                      "synthetic.gravity" "source" path version (:binding product))))))
        (is (empty? @events)))))
  (with-ingress {}
    (fn [product]
      (is (= :co-canonical-source-path-and-text
             (:missing-fact (diagnostic #(call 'p15-s23-c6c10-private-ingress-products
                                              "invalid.txt" "source" reader-path 3 (:binding product))))))
      (is (empty? @events)))))

(deftest canonical-reader-invocation-path-does-not-become-artifact-authority
  (with-ingress {}
    (fn [product]
      (let [path (str (.getParent (java.io.File. reader-path)) "/./" (.getName (java.io.File. reader-path)))]
        ;; Invocation selection compares canonical paths, but the envelope
        ;; owner still requires its exact canonical selected-program path.
        (is (= :actual-selected-current-reader-products
               (:missing-fact (diagnostic #(call 'p15-s23-c6c10-private-ingress-products
                                                "synthetic.gravity" "source" path 3 (:binding product))))))
        (is (= :envelope-owner (last @events)))
        (is (= 1 (count (filter #(and (vector? %) (= :front-end (first %))) @events))))))))

(deftest upstream-refusals-do-not-produce-a-policy-receipt
  (doseq [[options rule] [[{:front-end-fails true} "SYNTHETIC-C2"]
                         [{:carrier-fails true} "SYNTHETIC-CARRIER"]]]
    (with-ingress options
      (fn [product]
        (is (= rule (:id (diagnostic #(call 'p15-s23-c6c10-private-ingress-products
                                           "synthetic.gravity" "source" reader-path 3 (:binding product))))))
        (is (not-any? #{:envelope-owner} @events)))))
  (with-ingress {}
    (fn [product]
      (is (some? (diagnostic #(call 'p15-s23-c6c10-private-ingress-products
                                   "synthetic.gravity" "source" reader-path 3
                                   (assoc (:binding product) :source-content-hash "forged")))))
      (is (= :envelope-owner (last @events))))))

(deftest legacy-overloads-retain-no-policy-receipt
  (doseq [args [["synthetic.gravity" "source"]
                ["synthetic.gravity" "source" reader-path]]]
    (with-ingress {}
      (fn [_]
        (let [result (apply call 'p15-s23-c6c10-private-ingress-products args)]
          (is (= "synthetic-input-hash" (:source-content-hash result)))
          (is (not (contains? result :source-policy-receipt)))
          (is (not-any? #{:envelope-owner} @events)))))))

(let [result (run-tests 'c6-private-ingress-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :private-ingress-routing-and-envelope-policy-mechanism
            :selected-version-and-envelope-policy-helper :actual
            :upstream-products-and-projection-owners :declared-oracles
            :source-authentication false :reader-execution false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
