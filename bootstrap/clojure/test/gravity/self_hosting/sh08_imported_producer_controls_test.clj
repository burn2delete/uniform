(ns gravity.self-hosting.sh08-imported-producer-controls-test
  "Finite imported-producer validation controls; no public source admission."
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- invoke-c7
  [function arguments]
  (kernel-support/invoke-c7 function arguments))

(defn- fixture-inputs
  []
  (let [id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        module-id "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        c5 {:artifact-id module-id}
        request {:b47-context {:artifact-id id
                               :authenticated-wrapper
                               {:sh06-resolution-artifact c5}}}
        memo {id {:request request :result {:status :accepted}}}]
    {:c5 c5 :id id :memo memo :request request}))

(deftest imported-producer-identity-and-schema-controls-current
  ;; These normalized DATA records exercise the current Gravity joins.  They
  ;; are not authenticated source or public C7 admission authority.
  (let [{:keys [c5 id memo request]} (fixture-inputs)]
    (is (= {:status :accepted}
           (:result (invoke-c7 'sh08-im-derive [request memo {}]))))
    (is (= :substituted-module-type-request
           (get-in (invoke-c7 'sh08-im-derive
                              [(assoc request :extra true) memo {}])
                   [:result :diagnostics 0 :reason])))
    (is (= :cyclic-module-type-request
           (get-in (invoke-c7 'sh08-im-derive [request {} {id true}])
                   [:result :diagnostics 0 :reason])))
    (is (= :undeclared-module-type-producer
           (:reason (invoke-c7 'sh08-im-producers
                               [[request] memo {} []]))))
    (is (= :undeclared-module-type-producer
           (:reason (invoke-c7 'sh08-im-producers
                               [[request] memo {} [(assoc c5 :forged true)]]))))
    (is (= :duplicate-or-invalid-module-type-producer
           (:reason (invoke-c7 'sh08-im-producers
                               [[request request] memo {} [c5]]))))
    ;; This is a finite no-quota control.  It accepts the input container only;
    ;; producer identity and source authentication remain separate checks.
    (is (= :accepted
           (:status (invoke-c7 'sh08-im-inputs-check
                               [{:artifact :gravity/sh08-module-type-inputs
                                 :schema-version 1
                                 :module-requests (vec (repeat 4097 request))}]))))
    (doseq [inputs [{:artifact :gravity/sh08-module-type-inputs
                     :schema-version 2
                     :module-requests []}
                    {:artifact :gravity/sh08-module-type-inputs
                     :schema-version 1
                     :module-requests '()}
                    {:artifact :gravity/sh08-module-type-inputs
                     :schema-version 1
                     :module-requests []
                     :forged true}]]
      (is (= :malformed-module-type-inputs
             (:reason (invoke-c7 'sh08-im-inputs-check [inputs])))))))

(deftest imported-producer-finite-data-does-not-authenticate-current
  ;; A large finite DATA carrier must not become producer/source authority.
  ;; The size is an observation of accounting removal, not a new allowance.
  (let [{:keys [c5 request]} (fixture-inputs)
        duplicated-proof (String. (char-array 16777217 \x))
        supplied {:artifact :gravity/sh08-module-type-inputs
                  :schema-version 1
                  :module-requests
                  [(assoc request :coordinator-verification
                          {:raw-proof duplicated-proof})]}]
    ;; Current C7 exposes the four-argument producer path rather than the
    ;; retired carrier-preflight wrapper; the input-shape check still accepts
    ;; this finite DATA container before producer authentication runs.
    (is (= :accepted
           (:status (invoke-c7 'sh08-im-inputs-check [supplied]))))
    (is (= :rejected
           (get-in (invoke-c7 'sh08-im-producers
                              [(:module-requests supplied) {} {} [c5]])
                   [:status])))))
