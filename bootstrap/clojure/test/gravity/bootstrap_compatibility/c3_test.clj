(ns gravity.bootstrap-compatibility.c3-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c3-artifact-identity :as c3-artifact-identity]
            [gravity.c3-literal-projection :as c3-literal-projection]
            [gravity.c3-reader-integrity :as c3-reader-integrity]
            [gravity.c3-syntax-construction :as c3-syntax-construction]
            [gravity.c3-syntax-diagnostics :as c3-syntax-diagnostics]
            [gravity.c3-syntax-evidence :as c3-syntax-evidence]
            [gravity.c3-syntax-verification :as c3-syntax-verification]
            [gravity.syntax-object-stream :as syntax-object-stream]
            [gravity.syntax-origin :as syntax-origin]))

(deftest syntax-object-stream-compatibility-wrapper-preserves-arity-and-output
  (let [records [{:form 'value
                  :span {:source "source.gravity"}
                  :form-id nil}]
        context {:module 'syntax.compatibility :profile :hosted}]
    (is (= '([source-path form-records]
             [source-path form-records module-context])
           (:arglists (meta #'bootstrap/syntax-object-stream))))
    (is (= (:arglists (meta #'syntax-object-stream/syntax-object-stream))
           (:arglists (meta #'bootstrap/syntax-object-stream))))
    (is (= (syntax-object-stream/syntax-object-stream "source.gravity" records)
           (bootstrap/syntax-object-stream "source.gravity" records)))
    (is (= (syntax-object-stream/syntax-object-stream
            "source.gravity" records context)
           (bootstrap/syntax-object-stream
            "source.gravity" records context)))
    (is (contains? (first (bootstrap/syntax-object-stream
                           "source.gravity" records context))
                   :form-id))))

(deftest c3-origin-chain-compatibility-wrapper-preserves-arity-and-output
  (let [seed {:syntax-id "syntax-3"
              :span {:form-index 3}
              :generated-origin [{:reader-abbreviation :quote
                                  :from {:form-index 3}}]}
        source-unit {:source-id "sha256:source"}]
    (is (= '([seed source-unit])
           (:arglists (meta #'bootstrap/c3-origin-chain))))
    (is (= (:arglists (meta #'syntax-origin/c3-origin-chain))
           (:arglists (meta #'bootstrap/c3-origin-chain))))
    (is (= (syntax-origin/c3-origin-chain seed source-unit)
           (bootstrap/c3-origin-chain seed source-unit)))))

(deftest c3-syntax-evidence-compatibility-wrappers-preserve-output-and-interposition
  (let [stream [{:syntax/id :source
                 :form {:kind :symbol}
                 :profile :hosted
                 :metadata {:doc "source"}
                 :hygiene {:marks []}
                 :origin [{:kind :source}]
                 :prior-syntax-ids []}
                {:syntax/id :generated
                 :form {:kind :generated-form}
                 :profile :hosted
                 :metadata {:generated true}
                 :hygiene {:marks [:m]}
                 :origin [{:kind :generated :inputs [:source]}]
                 :prior-syntax-ids [:source]}]]
    (is (= '([]) (:arglists (meta #'bootstrap/c3-syntax-schema))))
    (doseq [[wrapper-var leaf]
            [[#'bootstrap/c3-hygiene-context-map
              c3-syntax-evidence/c3-hygiene-context-map]
             [#'bootstrap/c3-origin-chain-graph
              c3-syntax-evidence/c3-origin-chain-graph]
             [#'bootstrap/c3-metadata-ledger
              c3-syntax-evidence/c3-metadata-ledger]
             [#'bootstrap/c3-fact-ledger
              c3-syntax-evidence/c3-fact-ledger]
             [#'bootstrap/c3-generated-syntax-report
              c3-syntax-evidence/c3-generated-syntax-report]]]
      (is (= '([syntax-stream]) (:arglists (meta wrapper-var))))
      (is (= (leaf stream) (@wrapper-var stream))))
    (is (= (c3-syntax-evidence/c3-syntax-schema)
           (bootstrap/c3-syntax-schema)))
    (is (= '([syntax-stream])
           (:arglists (meta #'bootstrap/c3-syntax-serialization-fixture))))
    (is (= (c3-syntax-evidence/c3-syntax-serialization-fixture
            stream bootstrap/reader-canonical-hash)
           (bootstrap/c3-syntax-serialization-fixture stream)))
    (is (= '([span]) (:arglists (meta #'bootstrap/c3-resolvable-span?))))
    (is (= (c3-syntax-evidence/c3-resolvable-span?
            {:primary "generated:compat:1"})
           (bootstrap/c3-resolvable-span?
            {:primary "generated:compat:1"})))
    (with-redefs [bootstrap/c3-required-form-kinds [:interposed-kind]]
      (is (= [:interposed-kind]
             (:form-kinds (bootstrap/c3-syntax-schema)))))
    (with-redefs [bootstrap/reader-canonical-hash
                  (constantly "sha256:interposed-serialization")]
      (is (= "sha256:interposed-serialization"
             (:hash (bootstrap/c3-syntax-serialization-fixture stream)))))))

(deftest c3-syntax-construction-compatibility-wrappers-preserve-interposition
  (let [seed {:syntax-id :seed
              :form '(demo x)
              :span {:source "demo.gravity" :form-index 0}
              :namespace 'demo.core
              :phase :read
              :profile :hosted
              :metadata {}
              :reader-origin {:raw-excerpt "(demo x)"}
              :generated-origin []}
        form-record {:form-id :form-0
                     :kind :list
                     :open-token :tok-0
                     :close-token :tok-2}
        token-record {:token-id :tok-0}
        source-unit {:source-id "sha256:source"}
        integrity-report {:authentic? false}
        c2-artifact {}
        object (bootstrap/c3-syntax-object
                seed form-record token-record source-unit c2-artifact
                integrity-report)]
    (doseq [[wrapper-var expected]
            [[#'bootstrap/c3-path-neutral-origin '([origin])]
             [#'bootstrap/c3-identity-input
              '([seed origin namespace-context hygiene-context source-form-kind])]
             [#'bootstrap/c3-stable-syntax-id '([identity-input])]
             [#'bootstrap/c3-syntax-object
              '([seed form-record token-record source-unit c2-artifact
                 integrity-report])]
             [#'bootstrap/c3-generated-syntax-object '([base-object])]]]
      (is (= expected (:arglists (meta wrapper-var)))))
    (is (= :gravity/syntax-object (:artifact object)))
    (is (= :list (get-in object [:form :kind])))
    (is (= "sha256:source" (get-in object [:source :source-id])))
    (let [expected
          (c3-syntax-construction/with-operations
           {:c2-path-neutral-span bootstrap/c2-path-neutral-span
            :sha256-hex bootstrap/sha256-hex
            :c3-origin-chain bootstrap/c3-origin-chain
            :c3-source-form-kind bootstrap/c3-source-form-kind
            :c3-source-facts bootstrap/c3-source-facts}
           #(c3-syntax-construction/c3-syntax-object
             seed form-record token-record source-unit c2-artifact
             integrity-report))]
      (is (= expected object)))
    (with-redefs [bootstrap/c3-origin-chain (fn [& _] [:origin])
                  bootstrap/c3-source-form-kind (fn [& _] :interposed-kind)
                  bootstrap/c3-source-facts (fn [& _] {:interposed true})
                  bootstrap/c3-identity-input (fn [& _] {:identity :interposed})
                  bootstrap/c3-stable-syntax-id (fn [_] "syntax:interposed")
                  bootstrap/sha256-hex (constantly "input-hash")]
      (let [interposed (bootstrap/c3-syntax-object
                        seed form-record token-record source-unit c2-artifact
                        integrity-report)]
        (is (= "syntax:interposed" (:syntax/id interposed)))
        (is (= "sha256:input-hash"
               (get-in interposed [:identity :input-hash])))
        (is (= :interposed-kind (get-in interposed [:form :kind])))
        (is (= {:interposed true} (:facts interposed)))
        (is (= [:origin] (:origin interposed)))))
    (with-redefs [bootstrap/c2-path-neutral-span
                  (fn [span] (assoc span :path-neutral :interposed))]
      (is (= :interposed
             (:path-neutral
              (:span (bootstrap/c3-identity-input
                      seed [] {} {} :list))))))))

(deftest c3-syntax-verification-compatibility-wrappers-preserve-interposition
  (let [stream [{:syntax/id "sha256:compatibility"
                 :form {:kind :symbol}
                 :span {:primary {:source "compatibility.gravity"
                                  :byte-start 0
                                  :byte-end 1}}
                 :source {}
                 :namespace {}
                 :phase :read
                 :profile :hosted
                 :metadata {}
                 :hygiene {}
                 :origin [{:kind :source}]
                 :facts {}
                 :reader-binding {}
                 :reader-source-revision "sha256:reader"
                 :ownership {}
                 :version 1
                 :prior-syntax-ids []
                 :immutable? true}]
        serialization (bootstrap/c3-syntax-serialization-fixture stream)
        operations
        {:c3-syntax-schema bootstrap/c3-syntax-schema
         :c3-resolvable-span? bootstrap/c3-resolvable-span?
         :c3-syntax-serialization-fixture
         bootstrap/c3-syntax-serialization-fixture
         :c3-syntax-stream-reader-products-authentic?
         bootstrap/c3-syntax-stream-reader-products-authentic?
         :c3-syntax-verification-report
         bootstrap/c3-syntax-verification-report
         :c3-syntax-capability-proof bootstrap/c3-syntax-capability-proof
         :c3-syntax-validate! bootstrap/c3-syntax-validate!
         :c3-syntax-fail! bootstrap/c3-syntax-fail!
         :c3-syntax-diagnostic-ids bootstrap/c3-syntax-diagnostic-ids}]
    (is (= '([syntax-stream serialization]
             [syntax-stream serialization c2-artifact]
             [syntax-stream serialization c2-artifact gravity-boundary])
           (:arglists (meta #'bootstrap/c3-syntax-verification-report))))
    (is (= '([artifact])
           (:arglists (meta #'bootstrap/c3-syntax-capability-proof))))
    (is (= '([source-path artifact])
           (:arglists (meta #'bootstrap/c3-syntax-validate!))))
    (is (= (c3-syntax-verification/with-operations
            operations
            #(c3-syntax-verification/c3-syntax-verification-report
              stream serialization))
           (bootstrap/c3-syntax-verification-report stream serialization)))
    (let [calls (atom [])
          interposed-serialization {:roundtrip? true :sentinel :serialization}]
      (with-redefs [bootstrap/c3-syntax-schema
                    (fn []
                      (swap! calls conj :schema)
                      {:required-fields [:sentinel]})
                    bootstrap/c3-resolvable-span?
                    (fn [_] (swap! calls conj :span) true)
                    bootstrap/c3-syntax-serialization-fixture
                    (fn [_]
                      (swap! calls conj :serialization)
                      interposed-serialization)
                    bootstrap/c3-syntax-stream-reader-products-authentic?
                    (fn [& _] (swap! calls conj :authentication) true)]
        (let [report
              (bootstrap/c3-syntax-verification-report
               [{:sentinel true
                 :form {:kind :symbol}
                 :span :interposed
                 :hygiene {}
                 :metadata {}
                 :namespace {}
                 :phase :read}]
               interposed-serialization
               {:artifact-id :c2}
               {:slice :SH-04})]
          (is (= :passed (:status report)))
          (is (= #{:schema :span :serialization :authentication}
                 (set @calls)))))
      (reset! calls [])
      (let [complete-proof
            (zipmap [:construction-from-reader-seeds?
                     :stable-syntax-ids?
                     :source-and-generated-origins?
                     :hygiene-propagated?
                     :intentional-capture-recorded?
                     :metadata-preservation-and-change?
                     :fact-invalidation-recorded?
                     :serialization-round-trips?
                     :reader-products-authentic?
                     :syntax-verifier-current?
                     :syntax-verifier-passed?
                     :gravity-authoritative-products-current?
                     :diagnostics-covered?]
                    (repeat true))]
        (with-redefs [bootstrap/c3-syntax-capability-proof
                      (fn [_]
                        (assoc complete-proof :stable-syntax-ids? false))
                      bootstrap/c3-syntax-fail!
                      (fn [& args] (swap! calls conj args))]
          (is (= :complete
                 (bootstrap/c3-syntax-validate!
                  "compatibility.gravity" {})))
          (is (= [["C3-ID"
                   "compatibility.gravity"
                   {:stage :syntax-object-model}
                   {:missing-fields [:stable-syntax-ids?]}]]
                 @calls)))))))

(deftest c3-syntax-diagnostics-compatibility-wrappers-preserve-interposition
  (let [overrides {:fail :origin}
        module {:metadata {:compiler {:c3-syntax overrides}}}
        forms [(list 'ns 'demo.core
                     (list :metadata {:compiler {:c3-syntax overrides}}))]]
    (is (= c3-syntax-diagnostics/c3-syntax-diagnostic-ids
           bootstrap/c3-syntax-diagnostic-ids))
    (is (= c3-syntax-diagnostics/c3-syntax-governing-document
           bootstrap/c3-syntax-governing-document))
    (is (= c3-syntax-diagnostics/c3-syntax-rejected-designs
           bootstrap/c3-syntax-rejected-designs))
    (is (= c3-syntax-diagnostics/c3-syntax-override-diagnostics
           bootstrap/c3-syntax-override-diagnostics))
    (doseq [[wrapper-var expected]
            [[#'bootstrap/c3-syntax-source-overrides '([module])]
             [#'bootstrap/c3-syntax-overrides-from-forms '([forms])]
             [#'bootstrap/c3-syntax-message '([id])]
             [#'bootstrap/c3-syntax-fail!
              '([id source-path subject extra])]
             [#'bootstrap/c3-syntax-validate-overrides!
              '([source-path overrides])]]]
      (is (= expected (:arglists (meta wrapper-var)))))
    (is (= (c3-syntax-diagnostics/c3-syntax-source-overrides module)
           (bootstrap/c3-syntax-source-overrides module)))
    (is (= (c3-syntax-diagnostics/c3-syntax-overrides-from-forms forms)
           (bootstrap/c3-syntax-overrides-from-forms forms)))
    (is (= (c3-syntax-diagnostics/c3-syntax-message "C3-ORIGIN")
           (bootstrap/c3-syntax-message "C3-ORIGIN")))
    (let [failure (atom nil)]
      (with-redefs [bootstrap/c3-syntax-message (constantly "interposed")
                    bootstrap/c3-syntax-governing-document
                    "docs/interposed-c3.md"
                    bootstrap/source-span (fn [& _] :interposed-span)
                    bootstrap/fail! (fn [& args] (reset! failure args))]
        (bootstrap/c3-syntax-fail! "C3-ID" "source.gravity" {} {})
        (is (= "interposed" (second @failure)))
        (is (= :interposed-span (:source-span (nth @failure 2))))
        (is (= "docs/interposed-c3.md"
               (:expected-document (nth @failure 2)))))
      (reset! failure nil)
      (with-redefs [bootstrap/c3-syntax-override-diagnostics
                    {:interposed "C3-ID"}
                    bootstrap/source-span (fn [& _] :override-span)
                    bootstrap/c3-syntax-fail!
                    (fn [& args] (reset! failure args))]
        (bootstrap/c3-syntax-validate-overrides!
         "source.gravity" {:fail :interposed})
        (is (= ["C3-ID"
                "source.gravity"
                {:source-span :override-span
                 :producer :fixture-override
                 :form-kind :interposed
                 :hygiene {:marks [] :captures []}}
                {:missing-fields [:interposed]}]
               @failure))))))

(deftest c3-reader-integrity-compatibility-wrappers-preserve-interposition
  (is (= '([c2-artifact])
         (:arglists (meta #'bootstrap/c3-c2-reader-integrity-report))))
  (is (= '([source-path c2-artifact])
         (:arglists (meta #'bootstrap/c3-validate-c2-reader-artifact!))))
  (let [expected
        {:authentic? false
         :failures [:reader-product-validation-exception]
         :cause-class "java.lang.IllegalStateException"}
        throwing-operation
        (fn [& _] (throw (IllegalStateException. "interposed")))]
    (is (= expected
           (c3-reader-integrity/with-operations
            {:c2-lexical-product-validation throwing-operation}
            #(c3-reader-integrity/c3-c2-reader-integrity-report
              {:token-stream [{:raw "x"}]}))))
    (with-redefs [bootstrap/c2-lexical-product-validation throwing-operation]
      (is (= expected
             (bootstrap/c3-c2-reader-integrity-report
              {:token-stream [{:raw "x"}]})))))
  (let [failure (atom nil)
        base-report {:authentic? false :failures [:interposed-integrity]}]
    (with-redefs [bootstrap/c3-c2-reader-integrity-report
                  (constantly base-report)
                  bootstrap/source-span (fn [& _] :interposed-span)
                  bootstrap/c3-syntax-fail!
                  (fn [& args] (reset! failure args))]
      (let [report
            (bootstrap/c3-validate-c2-reader-artifact!
             "source.gravity"
             {:source-unit-record {:path "source.gravity"}
              :form-tree []})]
        (is (false? (:authentic? report)))
        (is (true? (:source-path-binding-valid? report)))
        (is (= [:interposed-integrity] (:failures report)))
        (is (= ["C3-FACT-STALE"
                "source.gravity"
                {:source-span :interposed-span
                 :producer :c2-reader-artifact
                 :form-kind nil}
                {:missing-fields [:interposed-integrity]
                 :facts {:reader-product-integrity report}}]
               @failure))))))

(deftest c3-literal-projection-compatibility-wrappers-preserve-interposition
  (let [span {:source "ratio.gravity" :byte-start 0 :byte-end 3}
        value 1/2
        form-record {:form-id :form-0
                     :kind :ratio
                     :open-token :tok-0
                     :close-token :tok-0
                     :raw "1/2"
                     :value value
                     :span span}
        token-record {:token-id :tok-0
                      :kind :ratio
                      :raw "1/2"
                      :lexeme "1/2"
                      :decoded value
                      :span span}
        seed {:syntax-id :seed
              :form-id :form-0
              :form value
              :span (assoc span :form-index 0)
              :reader-origin {:raw-excerpt "1/2" :raw-form-kind :ratio}
              :generated-origin []}
        artifact {:form-tree [form-record]
                  :token-stream [token-record]
                  :literal-decoding-records
                  [{:form-id :form-0
                    :decoded value
                    :raw "1/2"
                    :span span
                    :facts {:numerator-spelling "1"
                            :denominator-spelling "2"
                            :exact? true}}]
                  :semantic-error-deferment-record
                  {:deferred-literal-records []}
                  :reader-product-integrity {:integrity-hash :integrity}
                  :source-unit-record {:source-id :source}}
        integrity {:authentic? true}]
    (doseq [[wrapper-var expected]
            [[#'bootstrap/c3-deferred-ratio-descriptor-from-raw '([raw])]
             [#'bootstrap/c3-ratio-descriptor-from-raw '([raw])]
             [#'bootstrap/c3-lossless-literal-descriptor
              '([seed form-record c2-artifact integrity-report])]
             [#'bootstrap/c3-tagged-literal-descriptor
              '([seed form-record c2-artifact integrity-report])]
             [#'bootstrap/c3-source-form-kind
              '([seed form-record c2-artifact integrity-report])]
             [#'bootstrap/c3-source-facts
              '([seed form-record c2-artifact integrity-report])]]]
      (is (= expected (:arglists (meta wrapper-var)))))
    (is (= (c3-literal-projection/c3-ratio-descriptor-from-raw "1/2")
           (bootstrap/c3-ratio-descriptor-from-raw "1/2")))
    (is (= (c3-literal-projection/c3-lossless-literal-descriptor
            seed form-record artifact integrity)
           (bootstrap/c3-lossless-literal-descriptor
            seed form-record artifact integrity)))
    (is (= :ratio
           (bootstrap/c3-source-form-kind
            seed form-record artifact integrity)))
    (is (= :ratio
           (:reader-literal-kind
            (bootstrap/c3-source-facts
             seed form-record artifact integrity))))
    (with-redefs [bootstrap/c3-lossless-literal-descriptor
                  (fn [& _] {:kind :interposed :raw "x"})]
      (is (= :record-kind
             (bootstrap/c3-source-form-kind
              seed {:kind :record-kind} artifact integrity)))
      (is (= :interposed
             (:reader-literal-kind
              (bootstrap/c3-source-facts
               seed form-record artifact integrity)))))
    (let [calls (atom 0)]
      (with-redefs [bootstrap/c3-c2-reader-integrity-report
                    (fn [_] (swap! calls inc) {:authentic? false})]
        (is (nil? (bootstrap/c3-lossless-literal-descriptor
                   seed form-record artifact nil)))
        (is (= 1 @calls))))))

(deftest c3-artifact-identity-compatibility-wrappers-preserve-interposition
  (doseq [[wrapper-var expected]
          [[#'bootstrap/c3-path-neutral-reader-artifact-view '([c2-view])]
           [#'bootstrap/c3-path-neutral-syntax-object '([syntax])]
           [#'bootstrap/c3-gravity-syntax-boundary-identity-view
            '([boundary])]
           [#'bootstrap/c3-artifact-identity-input '([artifact])]
           [#'bootstrap/c3-artifact-id '([artifact])]]]
    (is (= expected (:arglists (meta wrapper-var)))))
  (let [syntax {:span {:primary {:source "source.gravity" :form-index 0}
                       :all [{:source "source.gravity" :form-index 0}]}
                :origin []}
        expected
        (c3-artifact-identity/with-operations
         {:c2-path-neutral-span bootstrap/c2-path-neutral-span
          :c3-path-neutral-origin bootstrap/c3-path-neutral-origin}
         #(c3-artifact-identity/c3-path-neutral-syntax-object syntax))]
    (is (= expected (bootstrap/c3-path-neutral-syntax-object syntax))))
  (with-redefs [bootstrap/c3-path-neutral-reader-artifact-view
                (constantly :reader-view)
                bootstrap/c3-path-neutral-syntax-object
                (constantly :syntax-view)]
    (let [preimage
          (bootstrap/c3-artifact-identity-input
           {:artifact-id :old
            :c2-reader-artifact :reader
            :syntax-object-stream [:syntax]
            :origin-chain-graph {:nodes []}
            :gravity-origin-chain-graph {:nodes []}})]
      (is (= :reader-view (:c2-reader-artifact preimage)))
      (is (= [:syntax-view] (:syntax-object-stream preimage)))))
  (with-redefs [bootstrap/c3-artifact-identity-input
                (constantly {:identity :interposed})
                bootstrap/reader-canonical-hash
                (fn [value]
                  (is (= {:identity :interposed} value))
                  "sha256:interposed")]
    (is (= "sha256:interposed" (bootstrap/c3-artifact-id {}))))
  (with-redefs [bootstrap/c2-path-neutral-span
                (fn [span] (assoc span :interposed true))]
    (is (true? (get-in (bootstrap/c3-path-neutral-syntax-object
                        {:span {:primary {} :all []} :origin []})
                       [:span :primary :interposed]))))
  (let [boundary {:slice :SH-04
                  :owner :gravity-source
                  :resolved-syntax-result {:status :accepted}}
        expected
        (c3-artifact-identity/c3-gravity-syntax-boundary-identity-view
         boundary)]
    (is (= expected
           (bootstrap/c3-gravity-syntax-boundary-identity-view boundary)))))
