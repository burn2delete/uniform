(ns gravity.evolution.namespace-proposals-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.evolution.namespace-proposals :as proposals]))

(def request
  {:visibility :public
   :registry {:id "gravity-public" :visibility :public}
   :namespace {:id "gravity/http" :visibility :public}
   :requester {:id "namespace-maintainer" :visibility :public}
   :reason "Reserve for the standard library"
   :policy-reference "policy:namespace-reservations-v1"
   :provenance [{:id "source:reservation-request-v1" :visibility :public}]
   :audit-history
   [{:id "audit:initial-request" :visibility :public
     :action "requested" :actor "namespace-maintainer"
     :reason "Record the original request"
     :policy-reference "policy:namespace-reservations-v1"
     :provenance [{:id "source:initial-request" :visibility :public}]}]})

(defn- expected [input]
  {:artifact :gravity/namespace-reservation-proposal
   :schema :gravity/namespace-reservation-proposal-v1
   :status :proposed :authority :non-authorizing :request input})

(defn- reject [diagnostic rule]
  {:status :rejected :diagnostics [{:diagnostic diagnostic :rule rule}]})

(defn- roundtrip [value] (edn/read-string (pr-str value)))

(deftest complete-reservation-proposal
  (let [before (roundtrip request)
        result (proposals/propose-reservation request)]
    (is (= (expected before) result))
    (is (= before request))
    (is (= result (roundtrip result)))
    (is (= result (proposals/propose-reservation request)))))

(deftest changed-reason-changes-product
  (let [changed (assoc request :reason "Reserve for security response")
        before (roundtrip changed)
        original (proposals/propose-reservation request)
        result (proposals/propose-reservation changed)]
    (is (= (expected request) original))
    (is (= (expected before) result))
    (is (not= original result))
    (is (= before changed))))

(deftest reordered-input-map-determinism
  (letfn [(reorder [value]
            (cond
              (map? value) (into {} (map (fn [[k v]] [k (reorder v)])
                                       (reverse (seq value))))
              (vector? value) (mapv reorder value)
              :else value))]
    (let [reordered (reorder request)
          before (roundtrip reordered)
          original (proposals/propose-reservation request)
          result (proposals/propose-reservation reordered)]
      (is (= (expected request) original result))
      (is (= (pr-str original) (pr-str result)))
      (is (= before reordered)))))

(deftest retained-audit-history-unchanged
  (let [old (first (:audit-history request))
        approved (assoc old :id "audit:earlier-approval" :action "approved")
        input (assoc request :audit-history [old approved old])
        before (roundtrip input)
        result (proposals/propose-reservation input)]
    (is (= (expected before) result))
    (is (= [old approved old] (get-in result [:request :audit-history])))
    (is (= before input)))
  (let [input (assoc request :audit-history [])]
    (is (= (expected input) (proposals/propose-reservation input)))))

(deftest missing-registry-namespace-or-requester
  (doseq [field [:registry :namespace :requester]
          input [(dissoc request field)
                 (assoc request field nil)
                 (assoc request field "not-a-reference")
                 (assoc-in request [field :id] " ")
                 (update request field dissoc :id)]]
    (let [before (roundtrip input)]
      (is (= (reject "GOV10001" :identity-required)
             (proposals/propose-reservation input)))
      (is (= before input)))))

(deftest missing-reason-or-policy-reference
  (doseq [field [:reason :policy-reference]
          input [(dissoc request field) (assoc request field nil)
                 (assoc request field " ") (assoc request field true)]]
    (let [before (roundtrip input)]
      (is (= (reject "GOV10007" :reason-and-policy-required)
             (proposals/propose-reservation input)))
      (is (= before input)))))

(deftest private-metadata-cannot-escape
  (doseq [path [[] [:registry] [:namespace] [:requester] [:provenance 0]
                [:audit-history 0] [:audit-history 0 :provenance 0]]
          field [:visibility :private-metadata]]
    (let [input (assoc-in request (conj path field)
                          (if (= field :visibility) :private "private-sentinel"))
          before (roundtrip input)]
      (is (= (reject "PKG9002" :public-fields-required)
             (proposals/propose-reservation input)))
      (is (= before input))))
  (let [input (-> request
                  (assoc-in [:namespace :id] "private/secret-namespace")
                  (assoc-in [:namespace :visibility] :private))
        result (proposals/propose-reservation input)]
    (is (= (reject "PKG9002" :public-fields-required) result))
    (is (not (.contains (pr-str result) "private/secret-namespace")))))

(deftest mutable-host-map-rejected-before-access
  (let [calls (atom 0)
        hostile (proxy [java.util.HashMap] []
                  (get [_] (swap! calls inc) (throw (AssertionError.)))
                  (entrySet [] (swap! calls inc) (throw (AssertionError.)))
                  (toString [] (swap! calls inc) (throw (AssertionError.))))]
    (doseq [input [hostile (assoc request :registry hostile)
                   (assoc request :audit-history [hostile])
                   (assoc request :provenance [hostile])]]
      (is (= (reject "PKG9002" :untrusted-carrier)
             (proposals/propose-reservation input))))
    (is (zero? @calls)))
  (let [host-map (java.util.HashMap. request)
        before (java.util.HashMap. host-map)]
    (is (= (reject "PKG9002" :untrusted-carrier)
           (proposals/propose-reservation host-map)))
    (is (= before host-map))))

(deftest caller-approved-status-cannot-promote-proposal
  (doseq [status [:approved :reserved :published :proposed nil]]
    (let [input (assoc request :status status)
          before (roundtrip input)]
      (is (= (reject "GOV10007" :caller-status-forbidden)
             (proposals/propose-reservation input)))
      (is (= before input)))))

(deftest every-supplied-identity-and-audit-field-binds-the-product
  (doseq [path [[:registry :id] [:namespace :id] [:requester :id]
                [:policy-reference] [:provenance 0 :id]
                [:audit-history 0 :id] [:audit-history 0 :action]
                [:audit-history 0 :actor] [:audit-history 0 :reason]
                [:audit-history 0 :policy-reference]
                [:audit-history 0 :provenance 0 :id]]]
    (let [input (update-in request path str "-changed")
          before (roundtrip input)
          result (proposals/propose-reservation input)]
      (is (= (expected before) result))
      (is (not= (proposals/propose-reservation request) result))
      (is (= before input)))))

(deftest missing-provenance-and-malformed-history
  (doseq [input [(dissoc request :provenance)
                 (assoc request :provenance [])
                 (assoc request :provenance {})
                 (assoc request :provenance [{:visibility :public}])
                 (update-in request [:audit-history 0] dissoc :provenance)]]
    (is (= (reject "GOV10002" :public-provenance-required)
           (proposals/propose-reservation input))))
  (doseq [input [(dissoc request :audit-history)
                 (assoc request :audit-history {})]]
    (is (= (reject "GOV10007" :audit-history-required)
           (proposals/propose-reservation input))))
  (doseq [field [:id :action :actor :reason :policy-reference]]
    (is (= (reject "GOV10007" :audit-record-required)
           (proposals/propose-reservation
            (update-in request [:audit-history 0] dissoc field))))))

(deftest metadata-unicode-and-nondecoded-carriers
  (doseq [input [(with-meta request {:private "metadata-sentinel"})
                 (assoc request :provenance
                        (with-meta (:provenance request) {:private true}))
                 (assoc request :reason (fn [] (throw (AssertionError.))))
                 (assoc request :audit-history (lazy-seq (throw (AssertionError.))))]]
    (is (= (reject "PKG9002" :untrusted-carrier)
           (proposals/propose-reservation input))))
  (doseq [text [(str (char 0xd800)) (str (char 0xdc00))]]
    (is (= (reject "PKG9002" :invalid-unicode)
           (proposals/propose-reservation (assoc request :reason text)))))
  (let [input (assoc request :reason (str "Public reason " (char 0xd83c) (char 0xdf0d)))]
    (is (= (expected input) (proposals/propose-reservation input)))))

(deftest native-backing-storage-is-detached-and-cycles-reject
  (let [backing (object-array [:id "gravity/http" :visibility :public])
        reference (clojure.lang.PersistentArrayMap. backing)
        input (assoc request :namespace reference)
        result (proposals/propose-reservation input)]
    (is (= (expected request) result))
    (is (= request input))
    (aset backing 1 "changed-after-proposal")
    (is (= (expected request) result)))
  (let [backing (object-array [:cycle nil])
        cyclic (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 1 cyclic)
    (is (= (reject "PKG9002" :untrusted-carrier)
           (proposals/propose-reservation cyclic)))))
