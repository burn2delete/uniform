(ns gravity.build.declared-inputs-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.build.declared-inputs :as inputs]))

;; These tests inject the missing trusted HOST entry, not a source authority.
;; They load neither Bootstrap nor Reader and perform no provider/resource IO.
(def hash-a (str "sha256:" (apply str (repeat 64 "a"))))
(def hash-b (str "sha256:" (apply str (repeat 64 "b"))))
(def grant
  {:id "local-schema" :principal "package/example"
   :provider 'gravity.build/hermetic-inputs :provider-version "1"
   :profile :meta :target :jvm :phase :build :lifetime :invocation
   :scope #{"schema"} :effects #{:build/read-file} :capabilities #{:fs/read}
   :audit :required :replay :recorded :handler 'gravity.reader/read-input
   :implementation hash-a :policy-id hash-b})
(def request
  (merge (select-keys grant [:principal :provider :provider-version :profile :target
                            :phase :lifetime :handler :implementation :policy-id])
         {:source-id hash-a :source-content-hash hash-b :form-id "f1"
          :payload-form-id "f2" :span {:byte-start 0 :byte-end 1} :input "schema"}))
(def declared [{:resource "schema" :content "schema text"}])
(defn host-call [grant declared run]
  (#'inputs/call-with-authorized-inputs grant declared run))
(defn reason [operation]
  (try (operation) :unexpected-success
       (catch clojure.lang.ExceptionInfo error (:reason (ex-data error)))))

(deftest original-result-verification-never-reads-provider-again
  (host-call grant declared
    (fn [cap]
      (inputs/preflight! cap [request])
      (let [{:keys [receipt record]} (inputs/read-input! cap request)]
        (is (= "schema text" (get-in record [:result :text])))
        (is (= (:input record) {:resource "schema"
                               :content-hash (get-in record [:result :content-hash])}))
        (with-redefs-fn {#'inputs/input-entry (fn [& _] (throw (Error. "provider replay")))}
          #(do
             (is (= (:result record)
                    (inputs/verify-original-result! cap request receipt record)))
             (is (= (:result record)
                    (inputs/verify-original-result! cap request receipt record)))))
        (is (= :original-result
               (reason #(inputs/verify-original-result!
                          cap request receipt (assoc-in record [:result :text] "tampered")))))
        (is (= :original-result
               (reason #(inputs/verify-original-result! cap request (Object.) record))))))))

(deftest identity-lifetime-and-source-joins-are-not-data-claims
  (let [escaped (atom nil)]
    (host-call grant declared
      (fn [cap]
        (let [{:keys [receipt record] :as original} (inputs/read-input! cap request)]
          (reset! escaped (assoc original :capability cap))
          (doseq [[key wrong] [[:source-id hash-b] [:source-content-hash hash-a]
                               [:form-id "other"] [:payload-form-id "other"]
                               [:span {:byte-start 1 :byte-end 2}]]]
            (is (= :original-result
                   (reason #(inputs/verify-original-result!
                              cap (assoc request key wrong) receipt record)))))
          (doseq [fake [nil {} grant {:capability cap} (Object.)]]
            (is (= :inactive-or-foreign-invocation
                   (reason #(inputs/read-input! fake request)))))
          (host-call grant declared
            (fn [other-cap]
              (is (= :inactive-or-foreign-invocation
                     (reason #(inputs/read-input! cap request))))
              (is (= :original-result
                     (reason #(inputs/verify-original-result! other-cap request receipt record))))))
          (is (= (:result record)
                 (inputs/verify-original-result! cap request receipt record))))))
    (is (= :inactive-or-foreign-invocation
           (reason #(inputs/verify-original-result!
                      (:capability @escaped) request (:receipt @escaped) (:record @escaped)))))))

(deftest wrong-scope-phase-provider-and-implementation-refuse
  (host-call grant declared
    (fn [cap]
      (doseq [[key wrong] [[:principal "package/other"] [:provider 'other/provider]
                           [:provider-version "2"] [:profile :hosted] [:target :wasm]
                           [:phase :runtime] [:lifetime :process]
                           [:handler 'other/handler] [:implementation hash-b]
                           [:policy-id hash-a]]]
        (is (= :request-binding
               (reason #(inputs/read-input! cap (assoc request key wrong))))))
      (is (= :request-scope
             (reason #(inputs/read-input! cap (assoc request :input "undeclared")))))
      (let [calls (atom 0)]
        (with-redefs-fn {#'inputs/input-entry (fn [& _] (swap! calls inc))}
          #(is (= :request-scope
                  (reason (fn [] (inputs/preflight!
                                   cap [request (assoc request :input "late-invalid")]))))))
        (is (zero? @calls)))))
  (doseq [[key wrong] [[:phase :runtime] [:lifetime :process] [:profile :hosted]
                       [:capabilities #{}] [:effects #{}] [:provider-version "2"]
                       [:audit :optional] [:replay :none]]]
    (is (= :grant (reason #(host-call (assoc grant key wrong) declared identity)))))
  (is (= :undeclared-input (reason #(host-call grant [] identity))))
  (is (= :duplicate-input (reason #(host-call grant (conj declared (first declared)) identity)))))

(deftest native-guards-precede-lookups-and-no-callback-is-accepted
  (let [called (atom 0)
        hostile (reify clojure.lang.ILookup
                  (valAt [_ _] (swap! called inc))
                  (valAt [_ _ _] (swap! called inc)))]
    (host-call grant declared
      (fn [cap]
        (is (= :native-carrier (reason #(inputs/read-input! cap hostile))))
        (is (= :native-carrier
               (reason #(inputs/read-input! cap (assoc request :span hostile)))))
        (is (= :native-carrier
               (reason #(inputs/read-input! cap (assoc request :invoke (fn [_] nil))))))
        (is (= :native-carrier
               (reason #(inputs/read-input! cap (with-meta request {:forged true})))))))
    (is (zero? @called)))
  (let [backing (object-array [:resource "schema" :content "original"])
        aliased (clojure.lang.PersistentArrayMap. backing)]
    (host-call grant [aliased]
      (fn [cap]
        (aset backing 3 "mutated caller backing")
        (is (= "original" (get-in (inputs/read-input! cap request) [:record :result :text]))))))
  (let [backing (object-array [:cycle nil])
        cyclic (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 1 cyclic)
    (is (= :native-carrier (reason #(host-call grant cyclic identity)))))
  (is (= :unicode (reason #(host-call grant [{:resource "schema" :content "\uD800"}] identity)))))

(deftest exception-unwinds-invocation-and-empty-input-is-legal
  (let [escaped (atom nil)]
    (is (= :test-error
           (try
             (host-call grant declared
               (fn [cap] (reset! escaped cap) (throw (ex-info "injected" {}))))
             (catch clojure.lang.ExceptionInfo _ :test-error))))
    (is (= :inactive-or-foreign-invocation (reason #(inputs/read-input! @escaped request)))))
  (host-call grant [{:resource "schema" :content ""}]
    (fn [cap]
      (is (= "" (get-in (inputs/read-input! cap request) [:record :result :text]))))))
