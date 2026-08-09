(ns gravity.bootstrap-compatibility.c2-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c2-artifact-identity :as c2-artifact-identity]
            [gravity.c2-source-identity :as c2-source-identity]
            [gravity.c2-reader-diagnostics :as c2-reader-diagnostics]
            [gravity.c2-reader-product-projection :as c2-reader-product-projection]
            [gravity.c2-lexical-validation :as c2-lexical-validation]))

(deftest c2-reader-product-projection-compatibility-wrappers-preserve-interposition
  (doseq [[wrapper-var expected]
          [[#'bootstrap/c2-syntax-seed-stream
            '([source-path products module-context])]
           [#'bootstrap/c2-deferred-semantic-literals '([form-tree])]
           [#'bootstrap/c2-top-level-products '([artifact])]
           [#'bootstrap/c2-reader-capability-proof '([artifact])]
           [#'bootstrap/c2-reader-overrides-from-forms '([forms])]
           [#'bootstrap/c2-reader-extension-invocations '([form-tree])]]]
    (is (= expected (:arglists (meta wrapper-var)))))
  (let [span {:byte-start 0 :byte-end 1}
        form-tree [{:form-id :n :kind :integer :raw "1" :span span
                    :value {:semantic-validation :deferred
                            :artifact :gravity/deferred-numeric-literal}}]
        artifact {:form-tree [{:form-id :f :open-token :t}]
                  :token-stream [{:token-id :t}]
                  :top-level-form-ids [:f]}
        forms [(list 'ns 'demo
                     (list :metadata {:compiler {:c2-reader {:mode :strict}}}))]]
    (is (= (c2-reader-product-projection/c2-deferred-semantic-literals form-tree)
           (bootstrap/c2-deferred-semantic-literals form-tree)))
    (is (= (c2-reader-product-projection/c2-top-level-products artifact)
           (bootstrap/c2-top-level-products artifact)))
    (is (= (c2-reader-product-projection/c2-reader-overrides-from-forms forms)
           (bootstrap/c2-reader-overrides-from-forms forms))))
  (doseq [[leaf-var invoke]
          [[#'c2-reader-product-projection/c2-syntax-seed-stream
            #(bootstrap/c2-syntax-seed-stream "src.g" {} {})]
           [#'c2-reader-product-projection/c2-deferred-semantic-literals
            #(bootstrap/c2-deferred-semantic-literals [])]
           [#'c2-reader-product-projection/c2-top-level-products
            #(bootstrap/c2-top-level-products {})]
           [#'c2-reader-product-projection/c2-reader-capability-proof
            #(bootstrap/c2-reader-capability-proof {})]
           [#'c2-reader-product-projection/c2-reader-overrides-from-forms
            #(bootstrap/c2-reader-overrides-from-forms [])]
           [#'c2-reader-product-projection/c2-reader-extension-invocations
            #(bootstrap/c2-reader-extension-invocations [])]]]
    (with-redefs-fn {leaf-var (fn [& _] :interposed)}
      #(is (= :interposed (invoke))))))

(deftest c2-source-identity-compatibility-wrappers-preserve-interposition
  (doseq [[wrapper-var expected]
          [[#'bootstrap/reader-normalize-relative-path '([path])]
           [#'bootstrap/reader-platform-neutral-absolute-path? '([path])]
           [#'bootstrap/reader-valid-project-relative-path? '([path])]
           [#'bootstrap/reader-explicit-project-context '([project-context])]
           [#'bootstrap/reader-valid-options? '([reader-options])]
           [#'bootstrap/reader-validate-options! '([reader-options])]
           [#'bootstrap/reader-project-root-record '([project-context])]
           [#'bootstrap/reader-source-identity-inputs
            '([source-text reader-options project-context])]
           [#'bootstrap/c2-source-unit-record
            '([source-path source-text reader-options]
              [source-path source-text reader-options project-context])]
           [#'bootstrap/c2-token-record '([token source-unit])]
           [#'bootstrap/c2-form-record '([record source-unit])]
           [#'bootstrap/c2-literal-records '([form-tree])]
           [#'bootstrap/c2-trivia-records '([token-stream])]]]
    (is (= expected (:arglists (meta wrapper-var)))))
  (is (= '([source-path])
         (:arglists (meta #'bootstrap/reader-project-context-for-source))))
  (is (nil? (get (ns-publics 'gravity.c2-source-identity)
                 'reader-project-context-for-source)))
  (let [source-path "/project/src/demo.gravity"
        source-text "alpha"
        project-root-id (str "sha256:" (apply str (repeat 64 "a")))
        extension-policy (str "sha256:" (apply str (repeat 64 "b")))
        context {:project-root-id project-root-id
                 :project-root-path "/project"
                 :project-relative-path "src/./demo.gravity"
                 :retained :context}
        normalized-context (assoc context
                                  :project-relative-path "src/demo.gravity")
        reader-options {:retain-comments true
                        :enabled-features #{:standard-reader}
                        :extension-policy extension-policy}
        identity-operations
        {:sha256-hex bootstrap/sha256-hex
         :reader-canonical-hash bootstrap/reader-canonical-hash
         :gravity-source-extension bootstrap/gravity-source-extension
         :gravity-source-kind bootstrap/gravity-source-kind}
        leaf-identity-inputs
        (c2-source-identity/with-operations
         {:sha256-hex bootstrap/sha256-hex}
         #(c2-source-identity/reader-source-identity-inputs
           source-text reader-options context))
        wrapper-identity-inputs
        (bootstrap/reader-source-identity-inputs
         source-text reader-options context)
        leaf-source-unit
        (c2-source-identity/with-operations
         identity-operations
         #(c2-source-identity/c2-source-unit-record
           source-path source-text reader-options context))
        wrapper-source-unit
        (bootstrap/c2-source-unit-record
         source-path source-text reader-options context)
        token {:index 7
               :kind :symbol
               :raw "alpha"
               :decoded 'alpha
               :span {:source source-path :byte-start 0 :byte-end 5}
               :retained :token}
        form {:form-id :form-0
              :kind :symbol
              :raw "alpha"
              :value 'alpha
              :span {:source source-path :byte-start 0 :byte-end 5}
              :origin {:reader :synthetic}
              :retained :form}
        form-tree [form
                   {:form-id :form-1
                    :kind :integer
                    :raw "0x10"
                    :value 16
                    :span {:source source-path :byte-start 6 :byte-end 10}}
                   {:form-id :form-2 :kind :list :raw "()" :value '()}]
        trivia-stream [{:token-id :tok-0
                        :kind :whitespace
                        :raw " "
                        :span {:source source-path :byte-start 5 :byte-end 6}
                        :source-id (:source-id wrapper-source-unit)
                        :source-path source-path
                        :trivia? true}
                       {:token-id :tok-1
                        :kind :symbol
                        :raw "beta"
                        :trivia? false}]]
    (is (= {:policy :gravity/standard-reader
            :version 1
            :registered-tags ['inst 'uuid]
            :ambient-authority :denied}
           bootstrap/standard-reader-policy))
    (is (= {:retain-comments true
            :enabled-features #{:standard-reader}
            :extension-policy
            (bootstrap/reader-canonical-hash
             bootstrap/standard-reader-policy)}
           bootstrap/standard-reader-options))
    (is (nil? (get (ns-publics 'gravity.c2-source-identity)
                   'standard-reader-policy)))
    (is (nil? (get (ns-publics 'gravity.c2-source-identity)
                   'standard-reader-options)))
    (is (= (c2-source-identity/reader-normalize-relative-path
            "src/./nested/../demo.gravity")
           (bootstrap/reader-normalize-relative-path
            "src/./nested/../demo.gravity")))
    (is (= (c2-source-identity/reader-platform-neutral-absolute-path?
            "C:\\project\\demo.gravity")
           (bootstrap/reader-platform-neutral-absolute-path?
            "C:\\project\\demo.gravity")))
    (is (= (c2-source-identity/reader-valid-project-relative-path?
            "src/../src/demo.gravity")
           (bootstrap/reader-valid-project-relative-path?
            "src/../src/demo.gravity")))
    (is (= (c2-source-identity/reader-explicit-project-context context)
           (bootstrap/reader-explicit-project-context context)
           normalized-context))
    (is (= (c2-source-identity/reader-valid-options? reader-options)
           (bootstrap/reader-valid-options? reader-options)))
    (is (= (c2-source-identity/reader-validate-options! reader-options)
           (bootstrap/reader-validate-options! reader-options)))
    (is (= (c2-source-identity/reader-project-root-record context)
           (bootstrap/reader-project-root-record context)))
    (is (= leaf-identity-inputs wrapper-identity-inputs))
    (is (= leaf-source-unit wrapper-source-unit))
    (is (= (c2-source-identity/c2-token-record token wrapper-source-unit)
           (bootstrap/c2-token-record token wrapper-source-unit)))
    (is (= (c2-source-identity/c2-form-record form wrapper-source-unit)
           (bootstrap/c2-form-record form wrapper-source-unit)))
    (is (= (c2-source-identity/c2-literal-records form-tree)
           (bootstrap/c2-literal-records form-tree)))
    (is (= (c2-source-identity/c2-trivia-records trivia-stream)
           (bootstrap/c2-trivia-records trivia-stream)))
    (let [normalize-calls (atom [])
          absolute-calls (atom [])]
      (with-redefs [bootstrap/reader-normalize-relative-path
                    (fn [path]
                      (swap! normalize-calls conj path)
                      "interposed/path.gravity")
                    bootstrap/reader-platform-neutral-absolute-path?
                    (fn [path]
                      (swap! absolute-calls conj path)
                      false)]
        (is (true?
             (bootstrap/reader-valid-project-relative-path?
              "raw/path.gravity"))))
      (is (= ["raw/path.gravity"] @normalize-calls))
      (is (= ["raw/path.gravity"] @absolute-calls)))
    (let [context-calls (atom [])
          options-calls (atom [])
          hash-calls (atom [])]
      (with-redefs [bootstrap/reader-explicit-project-context
                    (fn [value]
                      (swap! context-calls conj value)
                      normalized-context)
                    bootstrap/reader-validate-options!
                    (fn [value]
                      (swap! options-calls conj value)
                      reader-options)
                    bootstrap/sha256-hex
                    (fn [value]
                      (swap! hash-calls conj value)
                      (apply str (repeat 64 "c")))]
        (is (= (str "sha256:" (apply str (repeat 64 "c")))
               (:bytes-hash
                (bootstrap/reader-source-identity-inputs
                 source-text reader-options context)))))
      (is (= [context] @context-calls))
      (is (= [reader-options] @options-calls))
      (is (= [source-text] @hash-calls)))
    (let [context-calls (atom [])
          identity-calls (atom [])
          root-calls (atom [])
          canonical-calls (atom [])
          extension-calls (atom [])
          kind-calls (atom [])
          interposed-inputs
          (assoc leaf-identity-inputs :bytes-hash "sha256:interposed-bytes")]
      (with-redefs [bootstrap/reader-explicit-project-context
                    (fn [value]
                      (swap! context-calls conj value)
                      normalized-context)
                    bootstrap/reader-source-identity-inputs
                    (fn [& args]
                      (swap! identity-calls conj args)
                      interposed-inputs)
                    bootstrap/reader-project-root-record
                    (fn [value]
                      (swap! root-calls conj value)
                      {:path "/interposed"
                       :project-root-id project-root-id})
                    bootstrap/reader-canonical-hash
                    (fn [value]
                      (swap! canonical-calls conj value)
                      "sha256:interposed-source")
                    bootstrap/gravity-source-extension
                    (fn [path]
                      (swap! extension-calls conj path)
                      ".interposed")
                    bootstrap/gravity-source-kind
                    (fn [path]
                      (swap! kind-calls conj path)
                      :interposed-source)]
        (let [unit (bootstrap/c2-source-unit-record
                    source-path source-text reader-options context)]
          (is (= "sha256:interposed-source" (:source-id unit)))
          (is (= ".interposed" (:extension unit)))
          (is (= :interposed-source (:source-kind unit)))
          (is (= "sha256:interposed-bytes" (:bytes-hash unit)))))
      (is (= [context] @context-calls))
      (is (= [[source-text reader-options normalized-context]]
             @identity-calls))
      (is (= [normalized-context] @root-calls))
      (is (= [interposed-inputs] @canonical-calls))
      (is (= [source-path] @extension-calls))
      (is (= [source-path] @kind-calls)))
    (let [original bootstrap/c2-source-unit-record
          visited-arities (atom [])]
      (with-redefs [bootstrap/reader-project-context-for-source
                    (fn [path]
                      (is (= source-path path))
                      context)
                    bootstrap/c2-source-unit-record
                    (fn [& args]
                      (swap! visited-arities conj (count args))
                      (apply original args))]
        (is (= wrapper-source-unit
               (bootstrap/c2-source-unit-record
                source-path source-text reader-options))))
      (is (= [3 4] @visited-arities)))
    (let [original bootstrap/reader-normalize-relative-path
          calls (atom [])]
      (with-redefs [bootstrap/reader-normalize-relative-path
                    (fn [path]
                      (swap! calls conj path)
                      (original path))]
        (is (true?
             (bootstrap/reader-valid-project-relative-path?
              "src/./demo.gravity"))))
      (is (= ["src/./demo.gravity"] @calls)))))

(deftest c2-reader-diagnostics-compatibility-wrappers-preserve-interposition
  (is (= c2-reader-diagnostics/c2-reader-diagnostic-ids
         bootstrap/c2-reader-diagnostic-ids))
  (is (= c2-reader-diagnostics/c2-reader-governing-document
         bootstrap/c2-reader-governing-document))
  (is (= c2-reader-diagnostics/c2-reader-rejected-designs
         bootstrap/c2-reader-rejected-designs))
  (is (= c2-reader-diagnostics/c2-reader-override-diagnostics
         bootstrap/c2-reader-override-diagnostics))
  (doseq [[wrapper-var expected]
          [[#'bootstrap/c2-reader-source-overrides '([module])]
           [#'bootstrap/c2-reader-message '([id])]
           [#'bootstrap/c2-reader-fail!
            '([id source-path subject extra])]
           [#'bootstrap/c2-reader-remap-exception! '([source-path ex])]
           [#'bootstrap/c2-reader-validate-overrides!
            '([source-path overrides source-unit token-stream])]]]
    (is (= expected (:arglists (meta wrapper-var)))))
  (let [source-path "compatibility.gravity"
        source-id "sha256:compatibility-source"
        reader-options {:retain-comments true
                        :enabled-features #{:standard-reader}
                        :extension-policy "sha256:compatibility-policy"}
        fallback-span {:source source-path
                       :byte-start 0
                       :byte-end 1
                       :start {:line 1 :column 1}
                       :end {:line 1 :column 2}}
        subject {:source-id source-id
                 :token-id :tok-0
                 :raw "x"
                 :reader-options reader-options
                 :facts {:subject true}}
        extra {:facts {:extra true}}
        module {:metadata {:compiler {:c2-reader {:fail :hash}}}}]
    (is (= (c2-reader-diagnostics/c2-reader-source-overrides module)
           (bootstrap/c2-reader-source-overrides module)))
    (is (= (c2-reader-diagnostics/c2-reader-message "C2-HASH")
           (bootstrap/c2-reader-message "C2-HASH")))
    (let [leaf-failure (atom nil)
          wrapper-failure (atom nil)
          operations {:fail! (fn [& args]
                               (reset! leaf-failure args)
                               :leaf-failed)
                      :source-span (fn [& _] fallback-span)
                      :reader-canonical-hash
                      (constantly "sha256:compatibility-diagnostic")}
          leaf-result
          (c2-reader-diagnostics/with-operations
           operations
           #(c2-reader-diagnostics/c2-reader-fail!
             "C2-HASH" source-path subject extra))
          wrapper-result
          (with-redefs [bootstrap/fail!
                        (fn [& args]
                          (reset! wrapper-failure args)
                          :leaf-failed)
                        bootstrap/source-span (fn [& _] fallback-span)
                        bootstrap/reader-canonical-hash
                        (constantly "sha256:compatibility-diagnostic")]
            (bootstrap/c2-reader-fail!
             "C2-HASH" source-path subject extra))]
      (is (= leaf-result wrapper-result))
      (is (= @leaf-failure @wrapper-failure)))
    (let [upstream
          (ex-info
           "malformed string"
           {:id "STAGE1READER003"
            :message "malformed string"
            :source-id source-id
            :source-span fallback-span
            :token-id :tok-0
            :raw "bad"
            :facts {:reason :malformed-string}})
          leaf-call (atom nil)
          wrapper-call (atom nil)
          leaf-result
          (c2-reader-diagnostics/with-operations
           {:standard-reader-options reader-options
            :c2-reader-fail!
            (fn [& args]
              (reset! leaf-call args)
              :remapped)}
           #(c2-reader-diagnostics/c2-reader-remap-exception!
             source-path upstream))
          wrapper-result
          (with-redefs [bootstrap/standard-reader-options reader-options
                        bootstrap/c2-reader-fail!
                        (fn [& args]
                          (reset! wrapper-call args)
                          :remapped)]
            (bootstrap/c2-reader-remap-exception! source-path upstream))]
      (is (= leaf-result wrapper-result))
      (is (= @leaf-call @wrapper-call)))
    (let [overrides {:fail :hash}
          source-unit {:source-id source-id :reader-options reader-options}
          token-stream [{:token-id :tok-0
                         :decoded :hash
                         :raw ":hash"
                         :span fallback-span}]
          leaf-call (atom nil)
          wrapper-call (atom nil)
          leaf-result
          (c2-reader-diagnostics/with-operations
           {:c2-reader-fail!
            (fn [& args]
              (reset! leaf-call args)
              :override-rejected)}
           #(c2-reader-diagnostics/c2-reader-validate-overrides!
             source-path overrides source-unit token-stream))
          wrapper-result
          (with-redefs [bootstrap/c2-reader-fail!
                        (fn [& args]
                          (reset! wrapper-call args)
                          :override-rejected)]
            (bootstrap/c2-reader-validate-overrides!
             source-path overrides source-unit token-stream))]
      (is (= leaf-result wrapper-result))
      (is (= @leaf-call @wrapper-call)))
    (let [failure (atom nil)
          message-calls (atom [])
          hash-calls (atom [])
          span-calls (atom [])]
      (with-redefs [bootstrap/c2-reader-message
                    (fn [id]
                      (swap! message-calls conj id)
                      "interposed C2 message")
                    bootstrap/c2-reader-governing-document
                    "docs/interposed-c2.md"
                    bootstrap/source-span
                    (fn [& args]
                      (swap! span-calls conj args)
                      fallback-span)
                    bootstrap/reader-canonical-hash
                    (fn [value]
                      (swap! hash-calls conj value)
                      "sha256:interposed-diagnostic")
                    bootstrap/fail!
                    (fn [& args]
                      (reset! failure args)
                      :failed)]
        (is (= :failed
               (bootstrap/c2-reader-fail!
                "C2-HASH" source-path subject extra))))
      (is (= ["C2-HASH"] @message-calls))
      (is (= [[source-path 0]] @span-calls))
      (is (= 1 (count @hash-calls)))
      (let [[id message payload] @failure]
        (is (= "C2-HASH" id))
        (is (= "interposed C2 message" message))
        (is (= "sha256:interposed-diagnostic" (:diagnostic-id payload)))
        (is (= "docs/interposed-c2.md" (:expected-document payload)))
        (is (= reader-options (:reader-options payload)))
        (is (= {:subject true :extra true} (:facts payload)))
        (is (= source-id (get-in payload [:source-span :file])))))
    (let [interposed-options {:retain-comments false
                              :enabled-features #{:standard-reader}
                              :extension-policy "sha256:interposed-policy"}
          remap-call (atom nil)
          upstream
          (ex-info "interposed"
                   {:id "C2-INTERPOSED"
                    :message "interposed"
                    :source-span fallback-span})]
      (with-redefs [bootstrap/c2-reader-diagnostic-ids ["C2-INTERPOSED"]
                    bootstrap/standard-reader-options interposed-options
                    bootstrap/c2-reader-fail!
                    (fn [& args]
                      (reset! remap-call args)
                      :remapped)]
        (is (= :remapped
               (bootstrap/c2-reader-remap-exception!
                source-path upstream))))
      (let [[id path subject payload :as captured] @remap-call]
        (is (= 4 (count captured)))
        (is (= "C2-INTERPOSED" id))
        (is (= source-path path))
        (is (= fallback-span (:source-span subject)))
        (is (= interposed-options (:reader-options payload)))))
    (let [validate-call (atom nil)]
      (with-redefs [bootstrap/c2-reader-override-diagnostics
                    {:interposed "C2-HASH"}
                    bootstrap/c2-reader-fail!
                    (fn [& args]
                      (reset! validate-call args)
                      :validated)]
        (is (= :validated
               (bootstrap/c2-reader-validate-overrides!
                source-path
                {:fail :interposed :extension-tag 'demo/tag}
                {:source-id source-id :reader-options reader-options}
                [{:token-id :tok-interposed
                  :decoded :interposed
                  :raw ":interposed"
                  :span fallback-span}]))))
      (let [[id path subject payload :as captured] @validate-call]
        (is (= 4 (count captured)))
        (is (= "C2-HASH" id))
        (is (= source-path path))
        (is (= :tok-interposed (:token-id subject)))
        (is (= [:interposed] (:missing-fields payload)))))
    (let [original bootstrap/c2-reader-fail!
          replacement-calls (atom 0)
          failure (atom nil)]
      (with-redefs [bootstrap/c2-reader-fail!
                    (fn [& args]
                      (swap! replacement-calls inc)
                      (apply original args))
                    bootstrap/source-span (fn [& _] fallback-span)
                    bootstrap/reader-canonical-hash
                    (constantly "sha256:captured-original")
                    bootstrap/fail!
                    (fn [& args]
                      (reset! failure args)
                      :delegated)]
        (is (= :delegated
               (bootstrap/c2-reader-fail!
                "C2-HASH" source-path subject extra))))
      (is (= 1 @replacement-calls))
      (let [[id message payload :as captured] @failure]
        (is (= 3 (count captured)))
        (is (= "C2-HASH" id))
        (is (string? message))
        (is (= source-path (get-in payload [:origin-chain 0 :path])))
        (is (= "sha256:captured-original" (:diagnostic-id payload)))))
    (let [original-remap bootstrap/c2-reader-remap-exception!
          replacement-calls (atom 0)
          failure-call (atom nil)
          upstream
          (ex-info "delimiter"
                   {:id "STAGE1READER001"
                    :message "delimiter"
                    :source-span fallback-span})]
      (with-redefs [bootstrap/c2-reader-remap-exception!
                    (fn [& args]
                      (swap! replacement-calls inc)
                      (apply original-remap args))
                    bootstrap/c2-reader-fail!
                    (fn [& args]
                      (reset! failure-call args)
                      :remapped)]
        (is (= :remapped
               (bootstrap/c2-reader-remap-exception!
                source-path upstream))))
      (is (= 1 @replacement-calls))
      (is (= "C2-DELIMITER" (first @failure-call))))
    (let [original-validate bootstrap/c2-reader-validate-overrides!
          replacement-calls (atom 0)
          failure-call (atom nil)]
      (with-redefs [bootstrap/c2-reader-validate-overrides!
                    (fn [& args]
                      (swap! replacement-calls inc)
                      (apply original-validate args))
                    bootstrap/c2-reader-fail!
                    (fn [& args]
                      (reset! failure-call args)
                      :validated)]
        (is (= :validated
               (bootstrap/c2-reader-validate-overrides!
                source-path {:fail :hash}
                {:source-id source-id :reader-options reader-options}
                [{:token-id :tok-0 :decoded :hash
                  :raw ":hash" :span fallback-span}]))))
      (is (= 1 @replacement-calls))
      (is (= "C2-HASH" (first @failure-call))))
    (let [subject-options {:reader-policy :subject}
          extra-options {:reader-policy :extra}
          standard-options {:retain-comments false
                            :enabled-features #{:standard-reader}
                            :extension-policy "sha256:precedence-policy"}
          failure (atom nil)]
      (with-redefs [bootstrap/reader-canonical-hash
                    (constantly "sha256:reader-options-precedence")
                    bootstrap/fail!
                    (fn [& args]
                      (reset! failure args)
                      :failed)]
        (is (= :failed
               (bootstrap/c2-reader-fail!
                "C2-HASH" source-path
                {:source-id source-id
                 :source-span fallback-span
                 :reader-options subject-options}
                {:reader-options extra-options}))))
      (let [[id message payload :as captured] @failure]
        (is (= 3 (count captured)))
        (is (= "C2-HASH" id))
        (is (string? message))
        (is (= source-path (get-in payload [:origin-chain 0 :path])))
        (is (= subject-options (:reader-options payload))))
      (reset! failure nil)
      (with-redefs [bootstrap/standard-reader-options standard-options
                    bootstrap/reader-canonical-hash
                    (constantly "sha256:remapped-reader-options-precedence")
                    bootstrap/fail!
                    (fn [& args]
                      (reset! failure args)
                      :failed)]
        (is (= :failed
               (bootstrap/c2-reader-remap-exception!
                source-path
                (ex-info "source extension"
                         {:id "L1-SOURCE-EXTENSION"
                          :message "source extension"
                          :source-id source-id
                          :source-span fallback-span
                          :reader-options subject-options})))))
      (let [[id message payload :as captured] @failure]
        (is (= 3 (count captured)))
        (is (= "C2-EXTENSION" id))
        (is (string? message))
        (is (= source-path (get-in payload [:origin-chain 0 :path])))
        (is (= subject-options (:reader-options payload)))))))

(deftest c2-lexical-validation-compatibility-wrappers-preserve-interposition
  (doseq [[wrapper-var expected]
          [[#'bootstrap/c2-utf8-slice
            '([source-bytes byte-start byte-end])]
           [#'bootstrap/c2-span-encloses? '([parent child])]
           [#'bootstrap/c2-spans-source-ordered? '([spans])]
           [#'bootstrap/c2-form-graph-metrics '([form-tree])]
           [#'bootstrap/c2-lexical-product-validation
            '([source-text token-stream form-tree root-form-ids])]]]
    (is (= expected (:arglists (meta wrapper-var)))))
  (let [bytes (.getBytes "λx" java.nio.charset.StandardCharsets/UTF_8)
        parent {:byte-start 0 :byte-end 3}
        child {:byte-start 1 :byte-end 2}
        forms [{:form-id :form-0 :children [:form-1]}
               {:form-id :form-1 :children []}]]
    (is (= (c2-lexical-validation/c2-utf8-slice bytes 0 2)
           (bootstrap/c2-utf8-slice bytes 0 2)))
    (is (= (c2-lexical-validation/c2-span-encloses? parent child)
           (bootstrap/c2-span-encloses? parent child)))
    (is (= (c2-lexical-validation/c2-spans-source-ordered?
            [parent {:byte-start 3 :byte-end 4}])
           (bootstrap/c2-spans-source-ordered?
            [parent {:byte-start 3 :byte-end 4}])))
    (is (= (c2-lexical-validation/c2-form-graph-metrics forms)
           (bootstrap/c2-form-graph-metrics forms))))
  (let [calls (atom [])]
    (with-redefs [bootstrap/c2-form-graph-metrics
                  (fn [forms]
                    (swap! calls conj forms)
                    {:acyclic? false
                     :processed-form-count 0
                     :max-form-depth 0})]
      (let [report
            (bootstrap/c2-lexical-product-validation "" [] [] [])]
        (is (= :failed (:status report)))
        (is (false? (:acyclic? report)))
        (is (= [[]] @calls))))))

(deftest c2-artifact-identity-load-order-initializes-standard-reader-options
  (testing "the policy hash is initialized only after strict C2 ops are bound"
    (is (bound? #'bootstrap/standard-reader-options))
    (is (= {:retain-comments true
            :enabled-features #{:standard-reader}}
           (select-keys bootstrap/standard-reader-options
                        [:retain-comments :enabled-features])))
    (is (= (c2-artifact-identity/with-operations
             {:sha256-hex bootstrap/sha256-hex}
             #(c2-artifact-identity/reader-canonical-hash
               bootstrap/standard-reader-policy))
           (:extension-policy bootstrap/standard-reader-options)))
    ;; The load-order fix must not relax the leaf's eager operation contract.
    (is (thrown? clojure.lang.ExceptionInfo
                 (c2-artifact-identity/with-operations
                  {:c2-incremental-hashes :not-a-function}
                  (constantly :unreachable))))))

(deftest c2-artifact-identity-compatibility-wrappers-preserve-interposition
  (doseq [[wrapper-var expected]
          [[#'bootstrap/reader-canonical-value '([value])]
           [#'bootstrap/reader-canonical-hash '([value])]
           [#'bootstrap/c2-semantic-form-hash-input '([form-tree])]
           [#'bootstrap/c2-path-neutral-span '([span])]
           [#'bootstrap/c2-token-hash-input '([token-stream])]
           [#'bootstrap/c2-form-hash-input '([form-tree])]
           [#'bootstrap/c2-syntax-seed-hash-input '([syntax-seeds])]
           [#'bootstrap/c2-extension-hash-input
            '([extension-invocations])]
           [#'bootstrap/c2-diagnostic-hash-input '([diagnostics])]
           [#'bootstrap/c2-incremental-hashes
            '([source-unit token-stream form-tree syntax-seeds
               extension-invocations diagnostics])]
           [#'bootstrap/c2-reader-product-integrity-record
            '([source-unit top-level-form-ids incremental-hashes
               literal-records deferred-literal-records])]
           [#'bootstrap/c2-reader-artifact-id '([artifact])]]]
    (is (= expected (:arglists (meta wrapper-var)))))
  (let [source-path "/logical-project/src/example.gravity"
        span {:source source-path
              :file "sha256:source"
              :byte-start 0
              :byte-end 1}
        source-unit {:path source-path
                     :source-id "sha256:source"
                     :identity-inputs
                     {:project-relative-path "src/example.gravity"}
                     :bytes-hash "sha256:bytes"
                     :reader-options {:retain-comments true}}
        token-stream [{:token-id :tok-0
                       :source-path source-path
                       :span span}]
        form-tree [{:form-id :form-0
                    :kind :symbol
                    :children []
                    :parent-form-id nil
                    :source-path source-path
                    :span span
                    :surface-span span
                    :origin {:source-path source-path}
                    :generated-origin [{:from span}]}]
        syntax-seeds [{:syntax-id :seed-0
                       :span span
                       :generated-origin [{:from span}]}]
        extension-invocations [{:source-path source-path
                                :span span
                                :invocations [{:span span}]}]
        diagnostics [{:source-span span
                      :primary {:span span}
                      :related [{:span span}]
                      :origin-chain [{:path source-path :span span}]}]
        canonical-value {:z [3 2 1] :a #{:b :a}}
        leaf-canonical-operations {:sha256-hex bootstrap/sha256-hex}
        leaf-incremental-operations
        (assoc leaf-canonical-operations
               :c2-form-graph-metrics
               c2-lexical-validation/c2-form-graph-metrics
               :max-reader-form-graph-depth
               bootstrap/max-reader-form-graph-depth)
        incremental-hashes
        (bootstrap/c2-incremental-hashes
         source-unit token-stream form-tree syntax-seeds
         extension-invocations diagnostics)
        integrity-record
        (bootstrap/c2-reader-product-integrity-record
         source-unit [:form-0] incremental-hashes
         [{:span span}] [{:span span}])
        artifact {:kind :gravity/stage0-c2-reader-artifact
                  :task "C2"
                  :document-set ["C2"]
                  :source-unit-record source-unit
                  :reader-product-integrity integrity-record
                  :incremental-reader-hashes incremental-hashes
                  :representation-boundary {:kind :reader-products}
                  :source-overrides {:mode :default}
                  :capability-based-proof {:status :partial}}]
    (is (= (c2-artifact-identity/with-operations
            leaf-canonical-operations
            #(c2-artifact-identity/reader-canonical-value canonical-value))
           (bootstrap/reader-canonical-value canonical-value)))
    (is (= (c2-artifact-identity/with-operations
            leaf-canonical-operations
            #(c2-artifact-identity/reader-canonical-hash canonical-value))
           (bootstrap/reader-canonical-hash canonical-value)))
    (is (= (c2-artifact-identity/c2-semantic-form-hash-input form-tree)
           (bootstrap/c2-semantic-form-hash-input form-tree)))
    (is (= (c2-artifact-identity/c2-path-neutral-span span)
           (bootstrap/c2-path-neutral-span span)))
    (is (= (c2-artifact-identity/c2-token-hash-input token-stream)
           (bootstrap/c2-token-hash-input token-stream)))
    (is (= (c2-artifact-identity/c2-form-hash-input form-tree)
           (bootstrap/c2-form-hash-input form-tree)))
    (is (= (c2-artifact-identity/c2-syntax-seed-hash-input syntax-seeds)
           (bootstrap/c2-syntax-seed-hash-input syntax-seeds)))
    (is (= (c2-artifact-identity/c2-extension-hash-input
            extension-invocations)
           (bootstrap/c2-extension-hash-input extension-invocations)))
    (is (= (c2-artifact-identity/c2-diagnostic-hash-input diagnostics)
           (bootstrap/c2-diagnostic-hash-input diagnostics)))
    (is (= (c2-artifact-identity/with-operations
            leaf-incremental-operations
            #(c2-artifact-identity/c2-incremental-hashes
              source-unit token-stream form-tree syntax-seeds
              extension-invocations diagnostics))
           incremental-hashes))
    (is (= (c2-artifact-identity/with-operations
            leaf-canonical-operations
            #(c2-artifact-identity/c2-reader-product-integrity-record
              source-unit [:form-0] incremental-hashes
              [{:span span}] [{:span span}]))
           integrity-record))
    (is (= (c2-artifact-identity/with-operations
            leaf-canonical-operations
            #(c2-artifact-identity/c2-reader-artifact-id artifact))
           (bootstrap/c2-reader-artifact-id artifact)))
    (let [original bootstrap/reader-canonical-hash
          expected-hash (original canonical-value)
          replacement-calls (atom 0)
          actual-hash
          (with-redefs [bootstrap/reader-canonical-hash
                        (fn [value]
                          (swap! replacement-calls inc)
                          (original value))]
            (bootstrap/reader-canonical-hash canonical-value))]
      (is (= expected-hash actual-hash))
      (is (= 1 @replacement-calls)))
    (let [nested-list (list :inner 1)
          nested-value [{:outer nested-list}]
          original bootstrap/reader-canonical-value
          visited-values (atom [])
          actual-value
          (with-redefs [bootstrap/reader-canonical-value
                        (fn [value]
                          (swap! visited-values conj value)
                          (let [canonical-value (original value)]
                            (if (= 1 value)
                              :nested-scalar-interposed
                              canonical-value)))]
            (bootstrap/reader-canonical-value nested-value))]
      (is (= [nested-value
              {:outer nested-list}
              :outer
              nested-list
              :inner
              1]
             @visited-values))
      (is (= [:vector
              [[:map
                [[:outer
                  [:list [:inner :nested-scalar-interposed]]]]]]]
             actual-value)))
    (let [path-calls (atom [])]
      (with-redefs [bootstrap/c2-path-neutral-span
                    (fn [value]
                      (swap! path-calls conj value)
                      (assoc value :interposed true))]
        (is (= [{:token-id :tok-0
                 :span (assoc span :interposed true)}]
               (bootstrap/c2-token-hash-input token-stream))))
      (is (= [span] @path-calls)))
    (let [hash-calls (atom [])]
      (with-redefs [bootstrap/reader-canonical-hash
                    (fn [value]
                      (swap! hash-calls conj value)
                      "sha256:interposed")]
        (let [record
              (bootstrap/c2-reader-product-integrity-record
               source-unit [:form-0] incremental-hashes
               [{:span span}] [{:span span}])]
          (is (= "sha256:interposed" (:integrity-hash record)))
          (is (= "sha256:interposed"
                 (get-in record [:input :literal-records-hash])))
          (is (= "sha256:interposed"
                 (get-in record [:input :deferred-literal-records-hash])))))
      (is (= 3 (count @hash-calls))))
    (let [metric-calls (atom [])
          failure-calls (atom [])]
      (with-redefs [bootstrap/c2-form-graph-metrics
                    (fn [forms]
                      (swap! metric-calls conj forms)
                      {:acyclic? false
                       :processed-form-count 0
                       :max-form-depth 0})
                    bootstrap/c2-reader-fail!
                    (fn [& args]
                      (swap! failure-calls conj args))]
        (let [hashes (bootstrap/c2-incremental-hashes
                      source-unit [] [] [] [] [])]
          (is (= :stable (:status hashes)))))
      (is (= [[]] @metric-calls))
      (is (= 1 (count @failure-calls)))
      (let [[id path _ extra] (first @failure-calls)]
        (is (= "C2-HASH" id))
        (is (= source-path path))
        (is (= :reader-form-cycle
               (get-in extra [:facts :failure-kind])))))
    (is (= (inc bootstrap/max-reader-form-depth)
           bootstrap/max-reader-form-graph-depth))
    (is (integer? bootstrap/max-reader-form-graph-depth))
    (is (pos? bootstrap/max-reader-form-graph-depth))
    (is (= :accepted
           (c2-artifact-identity/with-operations
            {:max-reader-form-graph-depth
             bootstrap/max-reader-form-graph-depth}
            (constantly :accepted))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (c2-artifact-identity/with-operations
                  {:max-reader-form-graph-depth 0}
                  (constantly :unreachable))))
    (let [failure-calls (atom [])]
      (with-redefs [bootstrap/c2-form-graph-metrics
                    (fn [_]
                      {:acyclic? true
                       :processed-form-count 0
                       :max-form-depth bootstrap/max-reader-form-graph-depth})
                    bootstrap/c2-reader-fail!
                    (fn [& args]
                      (swap! failure-calls conj args))]
        (bootstrap/c2-incremental-hashes source-unit [] [] [] [] []))
      (is (empty? @failure-calls)))
    (let [failure-calls (atom [])]
      (with-redefs [bootstrap/c2-form-graph-metrics
                    (fn [_]
                      {:acyclic? true
                       :processed-form-count 0
                       :max-form-depth (inc bootstrap/max-reader-form-graph-depth)})
                    bootstrap/c2-reader-fail!
                    (fn [& args]
                      (swap! failure-calls conj args))]
        (bootstrap/c2-incremental-hashes source-unit [] [] [] [] []))
      (is (= 1 (count @failure-calls)))
      (let [[id path _ extra] (first @failure-calls)]
        (is (= "C2-HASH" id))
        (is (= source-path path))
        (is (= :reader-resource-depth-limit
               (get-in extra [:facts :failure-kind])))
        (is (= (inc bootstrap/max-reader-form-graph-depth)
               (get-in extra [:facts :observed-form-depth])))
        (is (= bootstrap/max-reader-form-graph-depth
               (get-in extra [:facts :maximum-form-depth])))))))
