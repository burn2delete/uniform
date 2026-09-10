(ns gravity.self-hosting.sh07-b51-attempt32-checker-child-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-attempt32-checker-child :as child]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]
            [gravity.tools.sh07-b51-v9-codec :as codec]))

(def sha-a
  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def git-a "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def git-b "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")

(defn- eligibility []
  {:schema :gravity/sh07-b51-external-review-object-v1
   :commit git-b :tree git-a :parents [git-a]
   :path
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn"
   :mode "100644" :type "blob" :blob-oid git-b :byte-count 1
   :raw-sha256 sha-a :receipt-id sha-a})

(defn- request [role]
  {:schema :gravity/sh07-b51-checker-child-request-v4
   :role role
   :content {:commit git-a :tree git-b}
   :eligibility (eligibility)
   :evidence {:architecture-report-sha256 sha-a
              :profile-bounds child/profile-bounds}
   :limits child/transport-limits})

(defn- private-value [name]
  (var-get (ns-resolve
            'gravity.tools.sh07-b51-attempt32-checker-child name)))

(defn- source-receipt [role]
  (let [descriptors
        (mapv (fn [spec]
                (merge spec {:mode "100644" :blob-oid git-a
                             :byte-count (inc (:ordinal spec))
                             :raw-sha256
                             (codec/R (byte-array [(:ordinal spec)]))}))
              (private-value 'source-specs))
        events
        (mapv (fn [plan]
                (let [source-raw-sha256
                      (:raw-sha256
                       (nth descriptors (:source-ordinal plan)))
                      product-id
                      (if (<= 3 (:ordinal plan) 23)
                        (codec/H
                         [:gravity/sh07-b51-source-event-product-id-v1
                          {:ordinal (:ordinal plan) :domain (:domain plan)
                           :consumer (:consumer plan)
                           :source-raw-sha256 source-raw-sha256
                           :product true}])
                        sha-a)]
                  (assoc plan :source-raw-sha256 source-raw-sha256
                         :product-id product-id)))
              (private-value 'source-event-plan))]
    {:schema :gravity/sh07-b51-source-read-receipt-v3
     :role role :commit git-a :tree git-b
     :source-descriptors descriptors
     :source-descriptors-root
     (codec/H [:gravity/sh07-b51-source-descriptors-root-v1 descriptors])
     :events events
     :event-root (codec/H [:gravity/sh07-b51-source-event-root-v1 events])
     :counters {:acquire-count 12 :decode-count 12
                :event-count 39 :finalize-count 1}
     :finalized true}))

(defn- products [role]
  {:m0s (vec (repeat 7 {})) :m1 {} :m2s (vec (repeat 7 {}))
   :bundle-core {}
   :execution-bundle-core-id sha-a :source-forms (vec (repeat 7 []))
   :module-transactions (vec (repeat 7 {}))
   :source-read-receipt (source-receipt role)})

(deftest checker-child-role-and-request-boundary-is-closed
  (is (= :producer-census
         (child/parse-role-arguments!
          ["--role" "producer-census"])))
  (is (= :independent-verifier-census
         (child/parse-role-arguments!
          ["--role" "independent-verifier-census"])))
  (doseq [arguments [["producer-census"]
                     ["--role" ":producer-census"]
                     ["--role" "producer-census" "extra"]]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (child/parse-role-arguments! arguments))))
  (is (= (request :producer-census)
         (child/validate-checker-request!
          :producer-census (request :producer-census))))
  (is (thrown? clojure.lang.ExceptionInfo
               (child/validate-checker-request!
                :producer-census
                (assoc (request :producer-census)
                       :m0s [:semantic-envelope-must-not-cross]))))
  (is (thrown? clojure.lang.ExceptionInfo
               (child/validate-checker-request!
                :producer-census
                (assoc (request :producer-census)
                       :role :independent-verifier-census)))))

(deftest checker-child-requires-finalized-process-local-source-role
  (is (= (products :producer-census)
         (child/validate-receipted-products!
          :producer-census (request :producer-census)
          (products :producer-census))))
  (testing "a neutral or opposite-role receipt cannot be repaired by the child"
    (doseq [role [nil :m :independent-verifier-census]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (child/validate-receipted-products!
                    :producer-census (request :producer-census)
                    (products role))))))
  (testing "checker semantics cannot begin over an unfinalized session"
    (is (thrown? clojure.lang.ExceptionInfo
                 (child/validate-receipted-products!
                  :producer-census (request :producer-census)
                  (assoc-in (products :producer-census)
                            [:source-read-receipt :finalized] false)))))
  (testing "descriptor, event, product, raw, and root joins are not ordinal-only"
    (doseq [mutate
            [#(assoc-in % [:source-read-receipt :source-descriptors 3 :path]
                        "forged.gravity")
             #(assoc-in % [:source-read-receipt :events 10 :consumer]
                        'forged/consumer)
             #(assoc-in % [:source-read-receipt :events 10 :product-id]
                        sha-a)
             #(assoc-in % [:source-read-receipt :events 10
                            :source-raw-sha256] sha-a)
             #(assoc-in % [:source-read-receipt :source-descriptors-root]
                        sha-a)
             #(assoc-in % [:source-read-receipt :event-root] sha-a)]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (child/validate-receipted-products!
                    :producer-census (request :producer-census)
                    (mutate (products :producer-census))))))))

(defn- authority-fixture []
  (let [m1-row {:ordinal 0 :caller 'consumer/f :callee 'provider/g}
        base {:ordinal 0
              :manifest-id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
              :m1-edge m1-row
              :consumer-module-ordinal 1
              :consumer-source-id "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
              :consumer-fragment-id "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
              :consumer-reference-syntax-id "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
              :consumer-binding-id "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
              :symbol 'provider/g
              :resolution-order 0
              :provider-module-ordinal 2
              :provider-source-id "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
              :provider-fragment-id "sha256:1111111111111111111111111111111111111111111111111111111111111111"
              :provider-binding-id "sha256:2222222222222222222222222222222222222222222222222222222222222222"}
        expected-row
        (assoc base :edge-id
               (materialize/semantic-witness-member-id
                :l2-expected-edge-census base))
        bad-base
        (assoc base
               :consumer-module-ordinal (:provider-module-ordinal base)
               :consumer-source-id (:provider-source-id base)
               :consumer-fragment-id (:provider-fragment-id base)
               :consumer-binding-id (:provider-binding-id base)
               :provider-module-ordinal (:consumer-module-ordinal base)
               :provider-source-id (:consumer-source-id base)
               :provider-fragment-id (:consumer-fragment-id base)
               :provider-binding-id (:consumer-binding-id base))
        qs [{:module :consumer} {:module :provider}]
        m0 [{:module :consumer} {:module :provider}]
        m1 {:manifest-id (:manifest-id base) :edges [m1-row]}
        owner [{:owner :consumer} {:owner :provider}]
        expected [expected-row]
        context {:carrier-valid? true
                 :pairing-valid? true
                 :expected-valid? true
                 :authentic-qs qs
                 :authentic-m0 m0
                 :authentic-m1 m1
                 :owner-registry owner
                 :expected-authority expected}]
    {:base base :bad-base bad-base :qs qs :m0 m0 :m1 m1 :owner owner
     :expected expected :context context}))

(deftest receipt-free-unauthorized-cross-fragment-verifier
  (let [{:keys [base bad-base qs m0 m1 owner expected context]}
        (authority-fixture)
        verifier
        (materialize/build-unauthorized-cross-fragment-verifier context)
        actual (verifier qs m0 m1 owner expected bad-base)]
    (is (= materialize/bad-actual-keyset (set (keys actual))))
    (is (= {:status :rejected
            :code "SH07-B51-EDGE-AUTHORITY"
            :rule "L2-UNKNOWN-CORE-FORM"
            :reason :unauthenticated-b51-external-link
            :input-id (:edge-id (first expected))
            :mutated-id
            (materialize/semantic-witness-member-id
             :l2-expected-edge-census bad-base)
            :expected (materialize/consumer-endpoint base)
            :actual (materialize/consumer-endpoint bad-base)
            :committed-row nil
            :output nil}
           actual))))

(deftest verifier-rejects-any-nonauthentic-or-nonendpoint-mutation
  (let [{:keys [bad-base qs m0 m1 owner expected context]}
        (authority-fixture)
        verifier
        (materialize/build-unauthorized-cross-fragment-verifier context)]
    (testing "authenticated inputs are exact, not caller assertions"
      (is (= :unauthorized-cross-fragment-verifier-precondition
             (try
               (verifier (conj qs {:module :forged}) m0 m1 owner expected
                         bad-base)
               nil
               (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error)))))))
    (testing "only the eight endpoint fields may change"
      (is (= :unauthorized-cross-fragment-verifier-precondition
             (try
               (verifier qs m0 m1 owner expected
                         (assoc bad-base :resolution-order 1))
               nil
               (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error)))))))
    (testing "no receipt or derived evidence can enter the candidate"
      (is (= :unauthorized-cross-fragment-verifier-precondition
             (try
               (verifier qs m0 m1 owner expected
                         (assoc bad-base :fuel-receipt-id "sha256:forged"))
               nil
               (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error)))))))))

(deftest diagnostic-and-fixture-are-derived-after-receipt-free-rejection
  (let [{:keys [base context]} (authority-fixture)
        operation-id "sha256:3333333333333333333333333333333333333333333333333333333333333333"
        receipt-id "sha256:4444444444444444444444444444444444444444444444444444444444444444"
        result
        (materialize/construct-negative-edge-evidence
         context [base] [{:owner :l2 :operation-id operation-id}]
         [{:operation-id operation-id :receipt-id receipt-id}])
        bad-actual (:bad-actual result)
        diagnostic (:diagnostic result)
        fixture (:fixture result)]
    (is (= bad-actual (:bad-actual-expected result)))
    (is (nil? (:committed-row bad-actual)))
    (is (nil? (:output bad-actual)))
    (is (not (contains? diagnostic :fixture-id)))
    (is (= (:diagnostic-id diagnostic) (:diagnostic-id fixture)))
    (is (= receipt-id (:fuel-receipt-id fixture)))
    (is (= [] (:rejected-edge-ids result)))
    (is (= [diagnostic] (get-in result [:l2-diagnostics :entries])))
    (is (= [fixture] (get-in result [:l2-negative-fixtures :entries])))))
