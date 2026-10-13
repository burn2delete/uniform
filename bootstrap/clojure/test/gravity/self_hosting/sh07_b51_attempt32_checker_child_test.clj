(ns gravity.self-hosting.sh07-b51-attempt32-checker-child-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.tools.sh07-b51-attempt32-checker-child :as child]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]
            [gravity.tools.sh07-b51-v9-codec :as codec]))

(def sha-a
  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def sha-b
  "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
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

(defn- private-var [name]
  (ns-resolve 'gravity.tools.sh07-b51-attempt32-checker-child name))

(defn- eligibility-result []
  (let [coordinate (eligibility)
        observations (mapv (fn [ordinal] {:ordinal ordinal}) (range 5))
        pre-observations (mapv (fn [ordinal] {:ordinal ordinal}) (range 209))
        check
        {:coordinate coordinate
         :observations observations
         :pre-discharge-observations pre-observations
         :decode-count 1
         :receipt-id-check-count 1
         :pre-discharge-reconstruction-count 1
         :pre-discharge-git-operation-count 209
         :pre-discharge-document-descriptor-count 33
         :pre-discharge-source-descriptor-count 13
         :pre-discharge-pre-masked-file-count 2
         :pre-discharge-owned-c-artifact-count 160
         :pre-discharge-a9-artifact-count 3
         :pre-discharge-baseline-boot5-count 1
         :pre-discharge-static-decode-count 196
         :content-match true
         :source-descriptors-match true
         :source-descriptors-root-match true
         :pre-masked-root-match true
         :owned-c-root-match true
         :pre-discharge-match true}
        receipt
        {:artifact :gravity/sh07-b51-evidence-eligibility-receipt-v2
         :schema-version 2
         :content {:commit git-a :tree git-b}
         :freeze {:active-record-ref
                  {:transition {:architecture-report-sha256 sha-a}}}
         :source-descriptors []
         :source-descriptors-root sha-a
         :pre-masked-root sha-a
         :pre-evidence-contract-discharge {}
         :review {}
         :state :evidence-eligible
         :self-id-omits [[:receipt-id]]
         :nonclaims []
         :receipt-id sha-a}]
    {:coordinate coordinate
     :receipt receipt
     :observations observations
     :operation-count 5
     :pre-object-snapshot {}
     :pre-discharge-observations pre-observations
     :pre-discharge-git-operation-count 209
     :scope :immutable-q-coordinate-and-pre-discharge
     :eligibility-check check
     :eligibility-check-root
     (codec/H [:gravity/sh07-b51-eligibility-check-root-v1 check])
     :remaining-gaps []
     :authority :none}))

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
     :counters {:acquire-count 13 :decode-count 13
                :event-count 40 :finalize-count 1}
     :finalized true}))

(defn- products [role]
  {:m0s (vec (repeat 7 {}))
   :m1 {}
   :m2s
   (vec (repeat 7 {:compiler {:source-content-hash sha-a
                              :semantic-hash sha-b}}))
   :bundle-core {}
   :execution-bundle-core-id sha-a :source-forms (vec (repeat 7 []))
   :module-transactions (vec (repeat 7 {}))
   :sh06-resolution-plan-authority {}
   :sh06-authority-carrier {}
   :source-semantic-carrier {}
   :source-read-receipt (source-receipt role)})

(defn- algorithm-preflight []
  (let [rows
        (mapv (fn [ordinal path]
                {:ordinal ordinal :path path :mode "100644" :blob-oid git-a
                 :byte-count 1 :raw-sha256 sha-a})
              (range) child/semantic-algorithm-source-paths-e3)
        objects
        (into {}
              (map (fn [row]
                     [(:path row)
                      (assoc (dissoc row :ordinal) :bytes (byte-array [0]))]))
              rows)]
    {:pre-object-snapshot {:owned-c-descriptors rows}
     :source-authority {:source-objects objects}}))

(deftest checker-envelope-derives-emitter-identities-from-all-seven-m2s
  (let [values (products :producer-census)
        identities
        ((private-value 'receipted-product-emitter-identities!) values)
        envelope
        (child/build-checker-envelope
         codec/H :producer-census (request :producer-census) values
         {:checker {} :dependencies {}} (algorithm-preflight))]
    (is (= {:emitter-source-id sha-a :emitter-semantic-id sha-b}
           identities))
    (is (= identities
           (select-keys envelope
                        [:emitter-source-id :emitter-semantic-id])))
    (testing "a nil, split, or incomplete M2 emitter identity cannot cross"
      (doseq [invalid
              [(assoc-in values [:m2s 0 :compiler :source-content-hash] nil)
               (assoc-in values [:m2s 3 :compiler :source-content-hash] sha-b)
               (assoc-in values [:m2s 4 :compiler :semantic-hash] sha-a)
               (update values :m2s pop)]]
        (is (thrown? clojure.lang.ExceptionInfo
                     ((private-value
                       'receipted-product-emitter-identities!)
                     invalid)))))))

(deftest checker-envelope-and-00947-use-the-exact-e3-product-boundary
  (let [values (products :producer-census)
        envelope
        (child/build-checker-envelope
         codec/H :producer-census (request :producer-census) values
         {:checker {} :dependencies {}} (algorithm-preflight))
        bootstrap-product-keys
        (var-get
         (ns-resolve 'gravity.bootstrap
                     'p15-s23-sh07-b51-v9-receipted-product-keys))]
    (is (= #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id
             :source-forms :module-transactions
             :sh06-resolution-plan-authority :sh06-authority-carrier
             :source-semantic-carrier
             :source-read-receipt}
           bootstrap-product-keys
           (set (keys values))))
    (is (= materialize/checker-semantic-input-keys-e3
           (set (keys envelope))))
    (is (= child/semantic-algorithm-source-paths-e3
           (mapv :path (:semantic-algorithm-source-descriptors envelope))))
    (is (= (:semantic-algorithm-source-root envelope)
           (codec/H
            [:gravity/sh07-b51-semantic-algorithm-source-root-v1
             (:semantic-algorithm-source-descriptors envelope)])))))

(deftest handoff-schema-rejects-every-missing-key-and-detached-extra
  (let [value (products :producer-census)
        invalids (concat (map #(dissoc value %) (keys value))
                         [(assoc value :unexpected true)
                          (assoc value :external-binding-authorities [])])]
    (is (= 11 (count value)))
    (doseq [invalid invalids]
      (doseq [operation
              [#(child/validate-receipted-products!
                 :producer-census (request :producer-census) invalid)
               #(child/build-checker-envelope codec/H :producer-census
                 (request :producer-census) invalid
                 {:checker {} :dependencies {}} (algorithm-preflight))]]
        (is (= :source-session
               (try (operation) nil
                    (catch clojure.lang.ExceptionInfo e
                      (:reason (ex-data e))))))))))

(deftest checker-child-never-promotes-a-legacy-census-certificate-to-v9
  (let [valid?
        (private-value 'v9-checker-output-shell?)
        witness
        {:schema :gravity/sh07-b51-authentic-lowering-witness-v1
         :witness-id sha-a}
        certificate
        {:inputs {:lowering-product-id sha-b}
         :semantic-witness witness}
        output {:status :accepted :certificate certificate}]
    (is (true? (valid? output)))
    (doseq [invalid
            [(update output :certificate dissoc :semantic-witness)
             (update-in output [:certificate :inputs]
                        dissoc :lowering-product-id)
             (assoc-in output [:certificate :semantic-witness :schema]
                       :gravity/legacy-witness)
             (assoc-in output [:certificate :inputs :lowering-product-id] nil)
             (assoc output :status :rejected)]]
      (is (false? (valid? invalid))))))

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

(deftest checker-child-reaches-the-shared-five-plus-eighty-eight-validator
  (let [calls (atom [])
        result (eligibility-result)
        validate!
        (fn [options]
          (swap! calls conj options)
          result)]
    (with-redefs-fn
      {(private-var 'load-git-evidence!)
       (fn [] {:validate-eligibility-object! validate!})}
      #(is (= result
              (child/validate-eligibility-five-operations!
               {:working-directory "/immutable/repository"}
               {:H codec/H}
               (request :producer-census)))))
    (is (= [{:repository-root "/immutable/repository"
             :content {:commit git-a :tree git-b}
             :eligibility-coordinate (eligibility)}]
           @calls))))

(deftest checker-child-rejects-partial-or-old-eligibility-results
  (let [base (eligibility-result)
        old-counter
        (-> base
            (assoc-in [:eligibility-check
                       :pre-discharge-masked-file-count] 2)
            (update :eligibility-check
                    dissoc :pre-discharge-pre-masked-file-count))
        old-receipt
        (-> base
            (assoc-in [:receipt :masked-root] sha-a)
            (update :receipt dissoc :pre-masked-root))
        partial
        (assoc base :remaining-gaps
               [:pre-discharge-84-operation-reconstruction])]
    (doseq [invalid [old-counter old-receipt partial]]
      (with-redefs-fn
        {(private-var 'load-git-evidence!)
         (fn [] {:validate-eligibility-object! (fn [_] invalid)})}
        #(is (thrown? clojure.lang.ExceptionInfo
                      (child/validate-eligibility-five-operations!
                       {:working-directory "/immutable/repository"}
                       {:H codec/H}
                       (request :producer-census))))))))

(deftest checker-child-requires-finalized-process-local-source-role
  (is (= (:source-read-receipt (products :producer-census))
         (child/validate-source-read-receipt!
          :producer-census (request :producer-census)
          (:source-read-receipt (products :producer-census)))))
  (testing "a neutral or opposite-role receipt cannot be repaired by the child"
    (doseq [role [nil :m :independent-verifier-census]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (child/validate-source-read-receipt!
                    :producer-census (request :producer-census)
                    (:source-read-receipt (products role)))))))
  (testing "checker semantics cannot begin over an unfinalized session"
    (is (thrown? clojure.lang.ExceptionInfo
                 (child/validate-source-read-receipt!
                  :producer-census (request :producer-census)
                  (assoc (:source-read-receipt (products :producer-census))
                         :finalized false)))))
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
                   (child/validate-source-read-receipt!
                    :producer-census (request :producer-census)
                    (:source-read-receipt
                     (mutate (products :producer-census))))))))
  (testing "the eleven-key unit transport boundary invokes both validators"
    (with-redefs [bootstrap/p15-s23-sh07-b51-sh06-authority-carrier-valid?
                  (fn [_ _ products carrier]
                    (= carrier (:sh06-authority-carrier products)))
                  bootstrap/p15-s23-sh07-b51-source-semantic-carrier-valid?
                  (fn [_ receipt transactions carrier]
                    (and (= receipt (:source-read-receipt
                                     (products :producer-census)))
                         (= 7 (count transactions))
                         (= {} carrier)))]
      (is (= (products :producer-census)
             (child/validate-receipted-products!
              :producer-census (request :producer-census)
              (products :producer-census)))))))

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
