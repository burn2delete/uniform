(ns c11-public-cleanup-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [gravity.bootstrap :as bootstrap]))

(defn- rejection-data [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest public-c11-closures-survive-lexical-owner-cleanup
  ;; The temporary constructor helpers are deliberately gone from the public
  ;; namespace, while the closures they created remain callable.
  (doseq [temporary '[__gravity_bootstrap_lexical_119416_construct-authenticated-c11-mir
                      __gravity_bootstrap_lexical_119416_invoke-pinned-c11-builder
                      __gravity_bootstrap_lexical_119416_authenticate-checked-core-ingress
                      __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier
                      __gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair
                      __gravity_bootstrap_lexical_values_119416]]
    (is (nil? (ns-resolve 'gravity.bootstrap temporary))))
  (is (fn? bootstrap/p15-s23-stage2-c11-mir-verify!))
  (is (fn? bootstrap/p15-s23-stage2-c11-mir-authentic?))
  (is (false? (bootstrap/p15-s23-stage2-c11-mir-authentic? :foreign-carrier)))
  (let [data (rejection-data
              #(bootstrap/p15-s23-stage2-c11-mir-verify!
                nil nil {:source-path "cleanup-control.gravity"}))]
    (is (= "C11-MODULE" (:id data)))
    (is (= :authenticated-checked-core-ingress-pair (:missing-fact data))))
  ;; The predicate route retains its containment behavior for a malformed
  ;; explicit call rather than exposing a cleaned lexical owner or callback.
  (is (false? (bootstrap/p15-s23-stage2-c11-mir-authentic?
               nil nil {:source-path "cleanup-control.gravity"}
               {:interface :gravity/c11-mir :version 3}))))

(let [result (run-tests 'c11-public-cleanup-test)]
  (println {:scope :public-c11-closure-and-cleanup-controls
            :temporary-lexical-owners :removed
            :public-verify-and-authentic-closures :actual
            :source-authentication false :native-callback-execution false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
