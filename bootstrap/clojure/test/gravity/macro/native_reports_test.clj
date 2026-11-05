(ns gravity.macro.native-reports-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
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

(deftest native-diagnostic-policy-is-stage-owned-with-compatible-facades
  (doseq [[stage seed] [[macro/c4-macro-diagnostic-ids b/c4-macro-diagnostic-ids]
                       [macro/c4-macro-governing-document b/c4-macro-governing-document]
                       [macro/c4-macro-rejected-designs b/c4-macro-rejected-designs]
                       [macro/c4-macro-override-diagnostics b/c4-macro-override-diagnostics]]]
    (is (identical? stage seed)))
  (let [value {:native-module true} path (String. "native.qst") subject (Object.) extra (Object.)]
    (with-redefs [macro/source-overrides identity macro/diagnostic-message identity
                  macro/emit-native-diagnostic! vector macro/validate-native-overrides! vector]
      (is (identical? value (b/c4-macro-source-overrides value)))
      (is (= "C4-TRACE" (b/c4-macro-message "C4-TRACE")))
      (is (= ["C4-TRACE" path subject extra #'b/c4-macro-message #'b/source-span
              #'b/c4-macro-governing-document #'b/fail!]
             (b/c4-macro-fail! "C4-TRACE" path subject extra)))
      (is (= [path value #'b/c4-macro-override-diagnostics #'b/source-span #'b/c4-macro-fail!]
             (b/c4-macro-validate-overrides! path value)))))
  (is (= {} (macro/source-overrides {})))
  (doseq [value [nil false {} {:fail :trace}]]
    (is (= value (macro/source-overrides {:metadata {:compiler {:c4-macro value}}})))))

(deftest actual-paired-compatibility-fixture-metadata-retains-each-refusal
  ;; Only inert fixture metadata is read. These are not semantic expansion proofs.
  (doseq [extension [".gravity" ".qst"]
          {:keys [diagnostic fixture]} b/c4-macro-rejected-designs
          :let [file (io/file (str/replace fixture #"\.gravity$" extension))
                path (.getCanonicalPath file)
                header (binding [*read-eval* false] (read-string (slurp file)))
                metadata (second (first (filter #(and (seq? %) (= :metadata (first %))) header)))
                overrides (b/c4-macro-source-overrides {:metadata metadata})
                data (ex-data (caught #(b/c4-macro-validate-overrides! path overrides)))]]
    (is (= diagnostic (:id data)))
    (is (= path (get-in data [:source-span :source])))
    (is (= [(:fail overrides)] (:missing-fields data)))
    (is (= :c4-macro-expansion (:diagnostic-family data)))
    (is (= ["C4" :macro-expansion :hosted :jvm]
           ((juxt :document-id :stage :active-profile :target) data)))))

(deftest native-diagnostic-retains-context-span-precedence-and-extra-facts
  (let [path "native.gravity" source {:source path :form-index 3}
        call {:source path :form-index 4} definition {:source path :form-index 1}
        generated {:source path :form-index 7}
        subject {:source-span source :call-span call :definition-span definition :generated-span generated
                 :macro 'native/m :macro-version "v1" :profile :kernel :target :llvm
                 :build-effects #{:build/read-file} :capabilities #{:fs/read} :hygiene {:marks ["m1"]}}
        diagnostic (fn [subject extra] (ex-data (caught #(b/c4-macro-fail! "C4-CAPTURE" path subject extra))))
        data (diagnostic subject {:native-fact :retained})]
    (is (= {:id "C4-CAPTURE" :bootstrap-stage :stage0 :source-span source :diagnostic-family :c4-macro-expansion
            :stage :macro-expansion :document-id "C4" :expected-document b/c4-macro-governing-document
            :macro 'native/m :macro-version "v1" :definition-span definition :call-site-span call
            :generated-span generated :active-profile :kernel :target :llvm
            :build-effects #{:build/read-file} :capabilities #{:fs/read} :hygiene-context {:marks ["m1"]}
            :native-fact :retained}
           (dissoc data :message :remediation)))
    (is (string? (:remediation data)))
    (is (= call (:source-span (diagnostic (dissoc subject :source-span) {}))))
    (is (= (b/source-span path 0) (:source-span (diagnostic {} {}))))
    (is (= nil (:source-span (diagnostic subject {:source-span nil}))))
    (is (= :override (:stage (diagnostic subject {:stage :override}))))
    (is (= "macro expansion engine failed" (macro/diagnostic-message "C4-UNKNOWN")))))

(deftest diagnostic-owner-and-configuration-reads-retain-original-evaluation-order
  (let [calls (atom []) late (fn [& args] (swap! calls conj [:late args]))]
    (with-redefs [b/fail! (fn [& args] (swap! calls conj [:first args]))
                  b/c4-macro-message (fn [_] (alter-var-root #'b/fail! (constantly late)) "message")
                  b/source-span (fn [path index]
                                  (alter-var-root #'b/c4-macro-governing-document (constantly "after-span"))
                                  {:source path :form-index index})
                  b/c4-macro-governing-document "before-span"]
      (b/c4-macro-fail! "C4-TRACE" "ordered.qst" {} {})
      (b/c4-macro-fail! "C4-TRACE" "ordered.qst" {} {})
      (is (= [:first :late] (mapv first @calls)))
      (is (= ["after-span" "after-span"] (mapv #(:expected-document (nth (second %) 2)) @calls)))))
  (with-redefs [b/fail! (fn [_ _ data] data)
                b/source-span (fn [& _] :before-message)
                b/c4-macro-message (fn [_]
                                     (alter-var-root #'b/source-span (constantly (fn [& _] :after-message)))
                                     "message")]
    (is (= :after-message (:source-span (b/c4-macro-fail! "C4-TRACE" "ordered.qst" {} {})))))
  (with-redefs [b/fail! (fn [_ _ data] data)
                b/source-span (fn [& _] (throw (AssertionError. "Span must remain short circuited")))]
    (is (= :source (:source-span (b/c4-macro-fail! "C4-TRACE" "skip.gravity" {:source-span :source} {}))))
    (is (= :call (:source-span (b/c4-macro-fail! "C4-TRACE" "skip.gravity" {:source-span false :call-span :call} {})))))
  (let [marker (ex-info "native span failure" {:native true})]
    (with-redefs [b/source-span (fn [& _] (throw marker))]
      (is (identical? marker (caught #(b/c4-macro-fail! "C4-TRACE" "throw.gravity" {} {})))))))

(deftest override-lookup-and-refusal-owner-remain-late-and-before-span-arguments
  (let [calls (atom []) later (fn [& args] (swap! calls conj [:later args]) :later)
        overrides (reify clojure.lang.ILookup
                    (valAt [_ key] (when (= :fail key)
                                    (alter-var-root #'b/c4-macro-override-diagnostics
                                                    (constantly {:custom "C4-CAPTURE"}))
                                    :custom)))]
    (with-redefs [b/c4-macro-override-diagnostics {}
                  b/c4-macro-fail! (fn [& args] (swap! calls conj [:first args]) :first)
                  b/source-span (fn [path index]
                                  (alter-var-root #'b/c4-macro-fail! (constantly later))
                                  {:source path :form-index index})]
      (is (= :first (b/c4-macro-validate-overrides! "native.qst" overrides)))
      (is (= :later (b/c4-macro-validate-overrides! "native.qst" overrides)))
      (is (= [:first :later] (mapv first @calls)))
      (is (= ["C4-CAPTURE" "C4-CAPTURE"] (mapv #(first (second %)) @calls)))
      (is (= [{:missing-fields [:custom]} {:missing-fields [:custom]}]
             (mapv #(nth (second %) 3) @calls)))))
  (with-redefs [b/source-span (fn [& _] (throw (AssertionError. "Unknown override must not read a span")))
                b/c4-macro-fail! (fn [& _] (throw (AssertionError. "Unknown override must not refuse")))]
    (doseq [overrides [nil false {} {:fail nil} {:fail false} {:fail :unknown}]]
      (is (nil? (b/c4-macro-validate-overrides! "unknown.gravity" overrides))))))

(deftest native-diagnostic-operation-ports-retain-function-values-and-exceptions
  (let [path (String. "functions.gravity") span {:source path :form-index 0}
        doc (atom "native-document") publish (fn [& args] args)]
    (is (= ["C4-RETURN" "native-message" span]
           (let [[id message data] (macro/emit-native-diagnostic!
                                    "C4-RETURN" path {} {} (constantly "native-message")
                                    (fn [& _] span) doc publish)]
             [id message (:source-span data)])))
    (is (= ["C4-RETURN" path {:source-span span :macro 'fixture/return :macro-version "fixture"
                            :profile :hosted :target :jvm :build-effects #{} :capabilities #{}
                            :hygiene {:marks []}} {:missing-fields [:return]}]
           (macro/validate-native-overrides! path {:fail :return} (atom {:return "C4-RETURN"})
                                             (fn [& _] span) publish))))
  (let [marker (ex-info "native publication failure" {:native true})]
    (with-redefs [b/fail! (fn [& _] (throw marker))]
      (is (identical? marker (caught #(b/c4-macro-fail! "C4-RETURN" "identity.qst" {} {})))))))
