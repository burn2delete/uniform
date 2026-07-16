(ns gravity.diagnostics-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.diagnostics :as diagnostics]))

(defn- captured-exception-info
  [f]
  (try
    (f)
    ::unexpected-success
    (catch clojure.lang.ExceptionInfo exception
      exception)))

(deftest base-exception-info-carrier-is-extracted-with-bootstrap-parity
  (testing "ordinary and nil data preserve the stage0 carrier"
    (doseq [data [{:fact :ordinary :count 2} nil]]
      (let [direct (diagnostics/diagnostic "D1-TEST" "carrier message" data)
            compatibility
            (bootstrap/diagnostic "D1-TEST" "carrier message" data)
            expected
            (merge {:id "D1-TEST"
                    :message "carrier message"
                    :bootstrap-stage :stage0}
                   data)]
        (doseq [exception [direct compatibility]]
          (is (instance? clojure.lang.ExceptionInfo exception))
          (is (= "carrier message" (ex-message exception)))
          (is (= expected (ex-data exception)))
          (is (nil? (ex-cause exception))))
        (is (= (ex-data direct) (ex-data compatibility))))))
  (testing "caller data retains the historical override precedence"
    (let [data {:id nil
                :message :caller-owned-message
                :bootstrap-stage nil
                :fact :retained}
          direct
          (diagnostics/diagnostic "D1-DEFAULT" "positional message" data)
          compatibility
          (bootstrap/diagnostic "D1-DEFAULT" "positional message" data)]
      (doseq [exception [direct compatibility]]
        (is (= "positional message" (ex-message exception)))
        (is (= {:id nil
                :message :caller-owned-message
                :bootstrap-stage nil
                :fact :retained}
               (ex-data exception))))))
  (testing "both fail functions throw the same unwrapped carrier"
    (let [data {:fact :rejection}
          direct
          (captured-exception-info
           #(diagnostics/fail! "D1-FAIL" "rejected" data))
          compatibility
          (captured-exception-info
           #(bootstrap/fail! "D1-FAIL" "rejected" data))]
      (doseq [exception [direct compatibility]]
        (is (instance? clojure.lang.ExceptionInfo exception))
        (is (= "rejected" (ex-message exception)))
        (is (= {:id "D1-FAIL"
                :message "rejected"
                :bootstrap-stage :stage0
                :fact :rejection}
               (ex-data exception))))
      (is (= (ex-data direct) (ex-data compatibility)))))
  (testing "bootstrap fail remains a thin compatibility seam"
    (let [sentinel (ex-info "sentinel" {:sentinel true})
          observed
          (with-redefs [bootstrap/diagnostic
                        (fn [_ _ _] sentinel)]
            (captured-exception-info
             #(bootstrap/fail! "ignored" "ignored" {})))]
      (is (identical? sentinel observed))))
  (testing "the extracted carrier has no namespace dependency back to bootstrap"
    (is (empty? (ns-aliases 'gravity.diagnostics)))
    (is (= '([id message data]) (:arglists (meta #'diagnostics/diagnostic))))
    (is (= '([id message data]) (:arglists (meta #'diagnostics/fail!))))
    (is (= #{'diagnostic 'fail!}
           (set (keys (ns-publics 'gravity.diagnostics))))))
  (testing "the namespace contract states its API and ownership boundary"
    (let [contract-var
          (get (ns-interns 'gravity.diagnostics) 'namespace-contract)
          contract (var-get contract-var)]
      (is (true? (:private (meta contract-var))))
      (is (= 'gravity.diagnostics (:namespace contract)))
      (is (= :stage0-base-exception-info-carrier
             (:contract-boundary contract)))
      (is (= #{'diagnostic 'fail!} (set (keys (:public-api contract)))))
      (is (= [:rule-id :human-message :structured-data]
             (:artifact-inputs contract)))
      (is (= [:stage0-exception-info-carrier]
             (:artifact-outputs contract)))
      (is (= ['clojure.core]
             (get-in contract [:dependency-direction :requires])))
      (is (= ['gravity.bootstrap]
             (get-in contract [:dependency-direction :forbids])))
      (is (some #{:c15-schema}
                (get-in contract [:diagnostic-ownership :does-not-own])))
      (is (true? (:bootstrap-hosted? contract)))
      (is (true? (:clojure-seed-boundary? contract)))
      (is (false? (:self-hosted? contract))))))
