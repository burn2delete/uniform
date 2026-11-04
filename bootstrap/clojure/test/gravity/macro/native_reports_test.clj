(ns gravity.macro.native-reports-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.macro :as macro]))

(def rules
  [[:syntax-input-output-valid? "C4-RETURN"]
   [:deterministic-expansion-trace? "C4-TRACE"]
   [:hygiene-and-capture-recorded? "C4-HYGIENE"]
   [:build-effects-authorized? "C4-BUILD-EFFECT"]
   [:generated-origin-present? "C4-TRACE"]
   [:generated-unsafe-checked? "C4-GENERATED-UNSAFE"]
   [:cache-replay-guarded? "C4-TRACE"]
   [:diagnostics-covered? "C4-TRACE"]
   [:self-hosting-comparison-ready? "C4-TRACE"]])

(defn- valid-artifact []
  {:c3-syntax-object-artifact {:syntax-object-stream [{:native-seed true}]}
   :expanded-syntax-stream [{:form '(def native 1)}]
   :macro-expansion-trace [{:output-syntax ["sha256:native-output"] :macro-version "native-version"
                           :hygiene {} :generated-origin [{:source "native.gravity"}]}]
   :hygiene-capture-records [{}]
   :build-effect-log {:records [{:authorization :granted} {:authorization :not-required}]}
   :macro-safety-report {:status :complete :generated-unsafe [{:native-record true}]}
   :expansion-cache-key {:hash "sha256:native-cache"}
   :trace-replay-report {:status :passed}
   :rejected-design-coverage (mapv #(hash-map :diagnostic %) b/c4-macro-diagnostic-ids)
   :self-hosting-comparison-inputs {:status :ready}})

(defn- proof [artifact]
  (macro/native-capability-proof artifact #'b/c4-macro-diagnostic-ids))

(defn- caught [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error error)))

(deftest native-report-preserves-existing-proof-and-facade-interposition
  (let [artifact (valid-artifact) expected (assoc (zipmap (map first rules) (repeat true)) :status :complete)]
    (is (= expected (proof artifact)))
    (is (= expected (b/c4-macro-capability-proof artifact)))
    (is (= :complete (b/c4-macro-validate! "native.gravity" artifact)))
    (let [sentinel {:native-stage-interposition true} observed (atom nil)]
      (with-redefs [macro/native-capability-proof
                    (fn [actual catalog] (reset! observed [actual catalog]) sentinel)]
        (is (identical? sentinel (b/c4-macro-capability-proof artifact)))
        (is (identical? artifact (first @observed)))
        (is (identical? #'b/c4-macro-diagnostic-ids (second @observed)))))
    (with-redefs [macro/validate-native! (fn [& arguments] arguments)]
      (let [[path actual proof-var failure-var] (b/c4-macro-validate! "native.qst" artifact)]
        (is (= "native.qst" path))
        (is (identical? artifact actual))
        (is (identical? #'b/c4-macro-capability-proof proof-var))
        (is (identical? #'b/c4-macro-fail! failure-var))))))

(deftest each-native-predicate-retains-its-own-counterexample
  (doseq [[field artifact]
          [[:syntax-input-output-valid? (assoc (valid-artifact) :expanded-syntax-stream [])]
           [:deterministic-expansion-trace? (assoc-in (valid-artifact) [:macro-expansion-trace 0 :macro-version] nil)]
           [:hygiene-and-capture-recorded? (assoc (valid-artifact) :hygiene-capture-records [])]
           [:build-effects-authorized? (assoc-in (valid-artifact) [:build-effect-log :records 0 :authorization] :denied)]
           [:generated-origin-present? (assoc-in (valid-artifact) [:macro-expansion-trace 0 :generated-origin] [])]
           [:generated-unsafe-checked? (assoc-in (valid-artifact) [:macro-safety-report :generated-unsafe] [])]
           [:cache-replay-guarded? (assoc-in (valid-artifact) [:trace-replay-report :status] :failed)]
           [:diagnostics-covered? (assoc (valid-artifact) :rejected-design-coverage [])]
           [:self-hosting-comparison-ready? (assoc-in (valid-artifact) [:self-hosting-comparison-inputs :status] :not-ready)]]]
    (let [actual (proof artifact)]
      (is (false? (get actual field)))
      (is (= [field] (vec (for [[key _] rules :when (false? (get actual key))] key)))))))

(deftest native-vacuity-and-readiness-labels-grant-no-extra-authority
  (let [without-records (-> (valid-artifact) (assoc :macro-expansion-trace nil :build-effect-log nil))
        actual (proof without-records)]
    (is (true? (:build-effects-authorized? actual)))
    (is (true? (:generated-origin-present? actual)))
    (is (false? (:deterministic-expansion-trace? actual)))
    (is (true? (:self-hosting-comparison-ready? actual)))
    (is (= :complete (:status actual))))
  (is (:diagnostics-covered?
       (proof (update (valid-artifact) :rejected-design-coverage
                      #(conj % (first %))))))
  (is (thrown? NullPointerException (proof (assoc (valid-artifact) :expansion-cache-key {})))))

(deftest catalog-is-read-after-native-input-realization-and-at-each-call
  (with-redefs [b/c4-macro-diagnostic-ids ["C4-OLD"]]
    (let [artifact (assoc (valid-artifact) :rejected-design-coverage
                          (lazy-seq
                           (alter-var-root #'b/c4-macro-diagnostic-ids (constantly ["C4-LATER"]))
                           [{:diagnostic "C4-LATER"}]))]
      (is (true? (:diagnostics-covered? (proof artifact))))
      (alter-var-root #'b/c4-macro-diagnostic-ids (constantly ["C4-NEXT"]))
      (is (false? (:diagnostics-covered? (proof artifact)))))))

(deftest ordered-native-refusals-preserve-real-diagnostics-and-exception-identity
  (doseq [[field id] rules]
    (with-redefs [b/c4-macro-capability-proof
                  (fn [_] (assoc (zipmap (map first rules) (repeat true)) field false))]
      (let [failure (caught #(b/c4-macro-validate! "actual-native.qst" (valid-artifact)))
            diagnostic (ex-data failure)]
        (is (= id (:id diagnostic)))
        (is (= [field] (:missing-fields diagnostic)))
        (is (= "actual-native.qst" (get-in diagnostic [:source-span :source]))))))
  (let [sentinel (ex-info "failure owner" {:sentinel true})]
    (with-redefs [b/c4-macro-capability-proof (fn [_] {})
                  b/c4-macro-fail! (fn [& _] (throw sentinel))]
      (is (identical? sentinel (caught #(b/c4-macro-validate! "first.gravity" {})))))))

(deftest validation-computes-proof-once-and-keeps-late-failure-owners
  (let [artifact (Object.) path (String. "returning.gravity") calls (atom []) proof-calls (atom [])]
    (with-redefs [b/c4-macro-capability-proof
                  (fn [actual]
                    (swap! proof-calls conj actual)
                    (alter-var-root #'b/c4-macro-capability-proof
                                    (constantly (fn [next-actual]
                                                  (swap! proof-calls conj next-actual)
                                                  (zipmap (map first rules) (repeat :truthy)))))
                    {})
                  b/c4-macro-fail!
                  (fn [& args]
                    (swap! calls conj [:first args])
                    (alter-var-root #'b/c4-macro-fail!
                                    (constantly (fn [& later] (swap! calls conj [:later later]))))
                    :returned-failure)]
      (is (= :complete (b/c4-macro-validate! path artifact)))
      (is (= (mapv second rules) (mapv #(first (second %)) @calls)))
      (is (= [:first :later :later :later :later :later :later :later :later] (mapv first @calls)))
      (is (= (mapv first rules) (mapv #(first (:missing-fields (nth (second %) 3))) @calls)))
      (doseq [[_ [_ actual-path subject _]] @calls]
        (is (identical? path actual-path))
        (is (= {:stage :macro-expansion} subject)))
      (is (= :complete (b/c4-macro-validate! path artifact)))
      (is (= 2 (count @proof-calls)))
      (is (every? #(identical? artifact %) @proof-calls)))))

(deftest truthy-native-proof-values-are-not-new-admission-checks
  (with-redefs [b/c4-macro-capability-proof (fn [_] (zipmap (map first rules) (cycle [0 [] {} :present])))
                b/c4-macro-fail! (fn [& _] (throw (AssertionError. "Truthy fields remain accepted")))]
    (is (= :complete (b/c4-macro-validate! "truthy.qst" {})))))
