(ns gravity.c2-reader-diagnostics-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.c2-reader-diagnostics :as diagnostics]))

(def source-path "src/example.gravity")
(def source-id "sha256:source")
(def standard-options
  {:retain-comments false
   :enabled-features #{:fixture-reader}
   :extension-policy "sha256:fixture-reader-policy"})

(defn- span
  ([start end]
   (span start end nil))
  ([start end file]
   (cond-> {:source source-path
            :byte-start start
            :byte-end end
            :start {:line 3 :column 2}
            :end {:line 3 :column 7}}
     file (assoc :file file))))

(def expected-diagnostic-ids
  ["C2-ENCODING"
   "C2-DELIMITER"
   "C2-STRING"
   "C2-NUMERIC"
   "C2-IDENTIFIER"
   "C2-NS-SHAPE"
   "C2-MAP"
   "C2-SET"
   "C2-METADATA"
   "C2-ABBREV"
   "C2-EXTENSION"
   "C2-HASH"])

(def expected-messages
  {"C2-ENCODING" "source decoding failed or used an undeclared encoding"
   "C2-DELIMITER" "reader delimiter structure is malformed"
   "C2-STRING" "string or character literal is malformed"
   "C2-NUMERIC" "numeric candidate fails every enabled numeric literal grammar"
   "C2-IDENTIFIER" "symbol or keyword has an invalid surface spelling"
   "C2-NS-SHAPE" "namespace clause has invalid reader-level syntax shape"
   "C2-MAP" "map literal has odd arity"
   "C2-SET" "literal set contains duplicate entries decidable at read time"
   "C2-METADATA" "metadata is unattached or has invalid reader shape"
   "C2-ABBREV" "reader abbreviation placement is invalid"
   "C2-EXTENSION" "source extension is noncanonical or reader extension is unknown, disallowed, or effect-violating"
   "C2-HASH" "reader artifact identity is unstable or incomplete"})

(def expected-rejected-designs
  [{:diagnostic "C2-ENCODING"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-encoding.gravity"
    :rejected-design :nondeterministic-source-decoding}
   {:diagnostic "C2-DELIMITER"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-delimiter.gravity"
    :rejected-design :malformed-delimiter-tree}
   {:diagnostic "C2-STRING"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-string.gravity"
    :rejected-design :lost-string-escape-facts}
   {:diagnostic "C2-NUMERIC"
    :fixture "bootstrap/clojure/fixtures/self-hosting/sh-03/rejected/malformed-numeric.gravity"
    :rejected-design :malformed-numeric-reclassified-or-host-parsed}
   {:diagnostic "C2-IDENTIFIER"
    :fixture "bootstrap/clojure/fixtures/self-hosting/sh-03/rejected/malformed-identifier.gravity"
    :rejected-design :malformed-symbol-or-keyword-spelling}
   {:diagnostic "C2-NS-SHAPE"
    :fixture "bootstrap/clojure/fixtures/self-hosting/sh-03/rejected/namespace-missing-name.gravity"
    :rejected-design :host-owned-or-malformed-namespace-clause-shape}
   {:diagnostic "C2-MAP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-map.gravity"
    :rejected-design :odd-map-literal}
   {:diagnostic "C2-SET"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-set.gravity"
    :rejected-design :duplicate-literal-set-entry}
   {:diagnostic "C2-METADATA"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-metadata.gravity"
    :rejected-design :unattached-or-invalid-metadata}
   {:diagnostic "C2-ABBREV"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-abbrev.gravity"
    :rejected-design :invalid-reader-abbreviation}
   {:diagnostic "C2-EXTENSION"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-extension.gravity"
    :rejected-design :ambient-reader-extension-authority}
   {:diagnostic "C2-HASH"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-hash.gravity"
    :rejected-design :unstable-reader-artifact-identity}])

(defn- capture-fail-operations
  [calls extra]
  (merge {:c2-reader-fail!
          (fn [& args]
            (swap! calls conj (vec args))
            :captured)}
         extra))

(defn- capture-terminal-fail-operations
  [calls extra]
  (merge {:fail!
          (fn [& args]
            (swap! calls conj (vec args))
            :captured)}
         extra))

(defn- capture-remap
  [data]
  (let [calls (atom [])]
    (diagnostics/with-operations
     {:c2-reader-fail!
      (fn [& args]
        (swap! calls conj (vec args))
        :captured)
      :standard-reader-options standard-options}
     #(let [result
            (diagnostics/c2-reader-remap-exception!
             source-path (ex-info "reader failure" data))]
        {:result result :calls @calls}))))

(deftest diagnostic-catalog-messages-and-source-overrides-are-exact
  (is (= expected-diagnostic-ids diagnostics/c2-reader-diagnostic-ids))
  (is (= (str "docs/phase-06-compiler-architecture/"
              "081-c2-reader-implementation-design.md")
         diagnostics/c2-reader-governing-document))
  (is (= expected-rejected-designs diagnostics/c2-reader-rejected-designs))
  (is (= expected-messages
         (into {} (map (fn [id] [id (diagnostics/c2-reader-message id)])
                       expected-diagnostic-ids))))
  (is (= "reader document coverage failed"
         (diagnostics/c2-reader-message "unknown-diagnostic")))
  (is (= {:encoding "C2-ENCODING"
          :abbrev "C2-ABBREV"
          :hash "C2-HASH"}
         diagnostics/c2-reader-override-diagnostics))
  (is (= {:fail :hash :extension-tag 'fixture/tag}
         (diagnostics/c2-reader-source-overrides
          {:metadata {:compiler {:c2-reader
                                 {:fail :hash
                                  :extension-tag 'fixture/tag}}}})))
  (is (= {} (diagnostics/c2-reader-source-overrides nil)))
  (is (= {} (diagnostics/c2-reader-source-overrides {}))))

(deftest reader-failure-payload-preserves-precedence-and-hash-preimage
  (let [subject-span (span 4 9)
        extra-span (span 11 13)
        subject {:source-span subject-span
                 :source-id source-id
                 :primary {:artifact "sha256:subject-primary"}
                 :raw "subject-raw"
                 :raw-spelling "subject-spelling"
                 :token-id :subject-token
                 :form-id :subject-form
                 :facts {:subject true :shared :subject}
                 :reader-options :subject-options
                 :extension-tag :subject-extension}
        extra {:source-span extra-span
               :source-id "sha256:extra"
               :primary {:artifact "sha256:extra-primary"}
               :raw "extra-raw"
               :raw-spelling "extra-spelling"
               :token-id :extra-token
               :form-id :extra-form
               :facts {:extra true :shared :extra}
               :reader-options :extra-options
               :extension-tag :extra-extension
               :severity :warning
               :related [:caller-related]
               :unknown-extra {:retained true}}
        hash-calls (atom [])
        failures (atom [])
        operations
        (capture-terminal-fail-operations
         failures
         {:source-span
          (fn [& args]
            (throw (ex-info "source-span should not be used" {:args args})))
          :reader-canonical-hash
          (fn [value]
            (swap! hash-calls conj value)
            "sha256:diagnostic")})
        result
        (diagnostics/with-operations
         operations
         #(diagnostics/c2-reader-fail! "C2-STRING" source-path
                                       subject extra))
        injected-span (assoc subject-span :file source-id)
        expected-hash-input
        {:rule :C2-STRING
         :primary-artifact source-id
         :stage :read-source
         :span (dissoc injected-span :source)
         :token-id :subject-token
         :form-id :subject-form
         :facts {:subject true :shared :extra :extra true}}
        expected-payload
        {:artifact :gravity/diagnostic
         :diagnostic-id "sha256:diagnostic"
         :rule :C2-STRING
         :severity :error
         :source-id source-id
         :source-span injected-span
         :primary {:span injected-span :artifact source-id}
         :related [:caller-related]
         :origin-chain [{:kind :source
                         :source-id source-id
                         :path source-path}]
         :profile nil
         :target nil
         :facts {:subject true :shared :extra :extra true}
         :diagnostic-family :c2-reader
         :stage :read-source
         :document-id "C2"
         :expected-document diagnostics/c2-reader-governing-document
         :involved-artifacts [source-id]
         :token-id :subject-token
         :form-id :subject-form
         :raw-spelling "subject-raw"
         :reader-options :subject-options
         :extension-tag :subject-extension
         :raw "extra-raw"
         :reader-state {:artifact :gravity/reader-state
                        :stage :read-source
                        :byte-offset 4
                        :line 3
                        :column 2
                        :token-id :subject-token
                        :form-id :subject-form}
         :redactions []
         :lifecycle :active
         :remediation
         "Regenerate reader artifacts with deterministic decoding, spans, raw literal facts, extension policy, and stable incremental hashes."
         :remediation-records [{:kind :fix-reader-source}]
         :unknown-extra {:retained true}}]
    (is (= :captured result))
    (is (= 1 (count @failures)))
    (is (= ["C2-STRING"
            "string or character literal is malformed"
            expected-payload]
           (first @failures)))
    (is (= [expected-hash-input] @hash-calls))
    (is (= :extra (get-in expected-payload [:facts :shared])))
    (is (= source-id (get-in expected-payload [:source-span :file])))
    (is (= :subject-options (:reader-options expected-payload)))
    (is (= :error (:severity expected-payload)))
    (is (= [:caller-related] (get-in (first @failures) [2 :related])))))

(deftest reader-failure-uses-extra-identity-and-source-span-fallback
  (let [fallback-span {:source source-path :byte-start 0 :byte-end 0}
        source-span-calls (atom [])
        hash-calls (atom [])
        failures (atom [])
        operations
        (capture-terminal-fail-operations
         failures
         {:source-span
          (fn [path offset]
            (swap! source-span-calls conj [path offset])
            fallback-span)
          :reader-canonical-hash
          (fn [value]
            (swap! hash-calls conj value)
            "sha256:fallback-diagnostic")})
        result
        (diagnostics/with-operations
         operations
         #(diagnostics/c2-reader-fail!
           "C2-HASH" source-path
           {:primary {:artifact "sha256:subject-primary"}}
           {:source-id "sha256:extra-id"}))
        [_ _ payload] (first @failures)
        injected-fallback-span (assoc fallback-span :file "sha256:extra-id")]
    (is (= :captured result))
    (is (= [[source-path 0]] @source-span-calls))
    (is (= "sha256:extra-id" (:source-id payload)))
    (is (= injected-fallback-span (:source-span payload)))
    (is (= [{:rule :C2-HASH
             :primary-artifact "sha256:extra-id"
             :stage :read-source
             :span (dissoc injected-fallback-span :source)
             :token-id nil
             :form-id nil
             :facts {}}]
           @hash-calls))
    (is (= "sha256:fallback-diagnostic" (:diagnostic-id payload)))
    (is (= ["sha256:extra-id"] (:involved-artifacts payload)))
    (is (= {} (:facts payload)))))

(def remap-routes
  [["STAGE1READER001" "C2-DELIMITER" "L1-DELIMITER"]
   ["STAGE1READER002" "C2-DELIMITER" "L1-DELIMITER"]
   ["STAGE1READER003" "C2-STRING" "L1-STRING"]
   ["STAGE1READER004" "C2-EXTENSION" "L1-READER-EXTENSION"]
   ["STAGE1READER005" "C2-MAP" "L1-MAP-ARITY"]
   ["STAGE1READER007" "C2-NUMERIC" "L1-NUMERIC"]
   ["L1-SOURCE-ENCODING" "C2-ENCODING" "L1-SOURCE-ENCODING"]
   ["L1-SOURCE-EXTENSION" "C2-EXTENSION" "L1-SOURCE-EXTENSION"]
   ["L1-DELIMITER" "C2-DELIMITER" "L1-DELIMITER"]
   ["L1-STRING" "C2-STRING" "L1-STRING"]
   ["L1-NUMERIC" "C2-NUMERIC" "L1-NUMERIC"]
   ["L1-IDENTIFIER" "C2-IDENTIFIER" "L1-IDENTIFIER"]
   ["L1-NS-SHAPE" "C2-NS-SHAPE" "L1-NS-SHAPE"]
   ["L1-MAP-ARITY" "C2-MAP" "L1-MAP-ARITY"]
   ["L1-METADATA" "C2-METADATA" "L1-METADATA"]
   ["L1-READER-EXTENSION" "C2-EXTENSION" "L1-READER-EXTENSION"]])

(defn- fallback-reader-state
  [old-id data]
  {:artifact :gravity/reader-state
   :stage (if (contains? #{"STAGE1READER003"
                           "STAGE1READER004"
                           "STAGE1READER007"}
                         old-id)
             :lexical-tokenization
             :recursive-form-building)
   :byte-offset 4
   :line 3
   :column 2
   :token-id (:token-id data)
   :form-id (:form-id data)})

(deftest remap-exception-covers-stage1-and-all-l1-routes
  (doseq [[old-id expected-id expected-owner] remap-routes]
    (let [data {:id old-id
                :message "legacy-message"
                :cause-message "preferred-cause"
                :source-span (span 4 9)
                :source-id source-id
                :token-id :tok-4
                :form-id :form-4
                :raw "raw-literal"
                :facts {:route old-id}
                :reader-options :subject-options
                :keep-me {:route old-id}}
          {:keys [result calls]} (capture-remap data)
          [_ path subject extra] (first calls)]
      (testing old-id
        (is (= :captured result))
        (is (= source-path path))
        (is (= data subject))
        (is (= expected-id (first (last calls))))
        (is (= expected-owner (:remapped-from extra)))
        (if (str/starts-with? old-id "STAGE1")
          (is (= old-id (:reader-engine-diagnostic extra)))
          (is (nil? (:reader-engine-diagnostic extra))))
        (is (= "preferred-cause" (:cause-message extra)))
        (is (= standard-options (:reader-options extra)))
        (is (= (fallback-reader-state old-id data)
               (:reader-state extra)))
        (is (= {:route old-id} (:keep-me extra)))))))

(deftest remap-preserves-supplied-reader-state-and-rethrows-unknown
  (let [supplied-state {:artifact :supplied-reader-state
                        :stage :fixture
                        :byte-offset 99
                        :marker :preserve}
        data {:id "L1-STRING"
              :message "legacy"
              :source-span (span 1 2)
              :source-id source-id
              :reader-state supplied-state}
        {:keys [calls]} (capture-remap data)
        [_ _ _ extra] (first calls)]
    (is (= supplied-state (:reader-state extra)))
    (let [ex (ex-info "unknown reader error"
                      {:id "NOT-A-C2-DIAGNOSTIC"
                       :message "unknown"})
          observed
          (try
            (diagnostics/with-operations
             {:standard-reader-options standard-options}
             #(diagnostics/c2-reader-remap-exception! source-path ex))
            nil
            (catch clojure.lang.ExceptionInfo error error))]
      (is (identical? ex observed))
      (is (= {:id "NOT-A-C2-DIAGNOSTIC" :message "unknown"}
             (ex-data observed))))))

(deftest remap-reader-options-retain-legacy-subject-precedence
  (let [failures (atom [])
        hash-calls (atom [])
        data {:id "L1-STRING"
              :message "legacy"
              :source-span (span 4 9)
              :source-id source-id
              :reader-options :subject-options}
        operations
        (capture-terminal-fail-operations
         failures
         {:source-span (fn [_ _] (span 0 0))
          :reader-canonical-hash
          (fn [value]
            (swap! hash-calls conj value)
            "sha256:remapped-diagnostic")
          :standard-reader-options standard-options})]
    (diagnostics/with-operations
     operations
     #(diagnostics/c2-reader-remap-exception! source-path
                                               (ex-info "legacy" data)))
    (let [[id _ payload] (first @failures)]
      (is (= "C2-STRING" id))
      ;; c2-reader-fail! checks subject before extra, so remapping preserves
      ;; this legacy precedence quirk even though extra carries standard opts.
      (is (= :subject-options (:reader-options payload)))
      (is (= "sha256:remapped-diagnostic" (:diagnostic-id payload)))
      (is (= 1 (count @hash-calls))))))

(deftest override-routes-select-matching-token-and-fallback-first-token
  (let [tokens [{:token-id :tok-first
                 :decoded :other
                 :raw "first"
                 :span (span 0 1)}
                {:token-id :tok-encoding
                 :decoded :encoding
                 :raw "encoding"
                 :span (span 1 2)}
                {:token-id :tok-abbrev
                 :decoded :abbrev
                 :raw "abbrev"
                 :span (span 2 3)}
                {:token-id :tok-hash
                 :decoded :hash
                 :raw "hash"
                 :span (span 3 4)}]
        source-unit {:source-id source-id
                     :reader-options standard-options}
        routes [[:encoding :tok-encoding "encoding"]
                [:abbrev :tok-abbrev "abbrev"]
                [:hash :tok-hash "hash"]]]
    (doseq [[kind token-id raw] routes]
      (let [failures (atom [])]
        (diagnostics/with-operations
         (capture-fail-operations failures {})
         #(diagnostics/c2-reader-validate-overrides!
           source-path {:fail kind :extension-tag kind}
           source-unit tokens))
        (let [[id path subject extra] (first @failures)]
          (testing (str kind)
            (is (= (get diagnostics/c2-reader-override-diagnostics kind) id))
            (is (= source-path path))
            (is (= source-id (:source-id subject)))
            (is (= token-id (:token-id subject)))
            (is (= raw (:raw subject)))
            (is (= standard-options (:reader-options subject)))
            (is (= kind (:extension-tag subject)))
            (is (= [kind] (:missing-fields extra)))))))
    (let [failures (atom [])]
      (diagnostics/with-operations
       (capture-fail-operations failures {})
         #(diagnostics/c2-reader-validate-overrides!
           source-path {:fail :encoding} source-unit
         [{:token-id :tok-fallback
           :decoded :fallback
           :raw "fallback"
           :span (span 8 9)}]))
      (let [[id _ subject extra] (first @failures)]
        (is (= "C2-ENCODING" id))
        (is (= :tok-fallback (:token-id subject)))
        (is (= "fallback" (:raw subject)))
        (is (= [:encoding] (:missing-fields extra)))))
    (doseq [overrides [nil {} {:fail :unknown} {:extension-tag :ignored}]]
      (let [failures (atom [])]
        (diagnostics/with-operations
         (capture-fail-operations failures {})
         #(diagnostics/c2-reader-validate-overrides!
           source-path overrides source-unit tokens))
        (is (empty? @failures) (str "no-op override " overrides))))))

(deftest operation-and-scalar-validation-is-strict
  (doseq [operations [nil [] {:unknown identity} {:fail! :not-a-function}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (diagnostics/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (diagnostics/with-operations {} :not-a-function)))
  (doseq [operations
          [{:c2-reader-diagnostic-ids nil}
           {:c2-reader-diagnostic-ids []}
           {:c2-reader-diagnostic-ids [""]}
           {:c2-reader-diagnostic-ids [:not-string]}
           {:c2-reader-governing-document nil}
           {:c2-reader-governing-document ""}
           {:c2-reader-governing-document :not-string}
           {:c2-reader-rejected-designs nil}
           {:c2-reader-rejected-designs []}
           {:c2-reader-rejected-designs [{:diagnostic "C2"}]}
           {:c2-reader-rejected-designs
            [{:diagnostic :not-string :fixture "fixture" :rejected-design :x}]}
           {:c2-reader-rejected-designs
            [{:diagnostic "C2" :fixture :not-string :rejected-design :x}]}
           {:c2-reader-override-diagnostics nil}
           {:c2-reader-override-diagnostics {}}
           {"c2-reader-override-diagnostics" {:encoding "C2"}}
           {:c2-reader-override-diagnostics {:encoding ""}}
           {:standard-reader-options nil}
           {:standard-reader-options
            {:retain-comments :not-boolean
             :enabled-features #{:fixture}
             :extension-policy "policy"}}
           {:standard-reader-options
            {:retain-comments true
             :enabled-features [:fixture]
             :extension-policy "policy"}}
           {:standard-reader-options
            {:retain-comments true
             :enabled-features #{"fixture"}
             :extension-policy "policy"}}
           {:standard-reader-options
            {:retain-comments true
             :enabled-features #{:fixture}
             :extension-policy ""}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (diagnostics/with-operations operations (fn [] :unused)))
        (str "rejected operations " operations)))
  (is (= :ok
         (diagnostics/with-operations
          {:c2-reader-diagnostic-ids ["C2-X"]
           :c2-reader-governing-document "document"
           :c2-reader-rejected-designs
           [{:diagnostic "C2-X" :fixture "fixture" :rejected-design nil}]
           :c2-reader-override-diagnostics {:x "C2-X"}
           :standard-reader-options standard-options}
          (fn [] :ok))))
  (is (thrown? clojure.lang.ExceptionInfo
               (diagnostics/call-entrypoint-body :unknown identity [])))
  (is (thrown? clojure.lang.ExceptionInfo
               (diagnostics/call-entrypoint-body :c2-reader-message :not-a-function [])))
  (is (thrown? clojure.lang.ExceptionInfo
               (diagnostics/call-entrypoint-body :c2-reader-message
                                                 diagnostics/c2-reader-message
                                                 :not-sequential)))
  (is (= (diagnostics/c2-reader-message "C2-HASH")
         (diagnostics/call-entrypoint-body
          :c2-reader-message diagnostics/c2-reader-message ["C2-HASH"])))
  (let [missing-operation
        (try
          (diagnostics/with-operations {}
           #(diagnostics/c2-reader-fail! "C2-HASH" source-path {} {}))
          nil
          (catch clojure.lang.ExceptionInfo ex ex))]
    (is (= :source-span (:operation (ex-data missing-operation))))))

(deftest captured-original-and-recursive-interposition-are-preserved
  (let [calls (atom [])]
    (diagnostics/with-operations
     {:c2-reader-message
      (fn [id]
        (swap! calls conj id)
        (diagnostics/c2-reader-message id))}
     #(is (= "reader artifact identity is unstable or incomplete"
              (diagnostics/c2-reader-message "C2-HASH"))))
    (is (= ["C2-HASH"] @calls)))
  (let [calls (atom [])]
    (diagnostics/with-operations
     {:c2-reader-message
      (fn [id]
        (swap! calls conj id)
        :replacement)}
     #(is (= "reader artifact identity is unstable or incomplete"
              (diagnostics/call-entrypoint-body
               :c2-reader-message diagnostics/c2-reader-message
               ["C2-HASH"]))))
    (is (empty? @calls)))
  (let [calls (atom [])
        failures (atom [])]
    (diagnostics/with-operations
     (capture-terminal-fail-operations
      failures
      {:source-span (fn [_ _] (span 0 1))
       :reader-canonical-hash (fn [_] "sha256:captured")
       :c2-reader-message
       (fn [id]
         (swap! calls conj id)
         (diagnostics/c2-reader-message id))})
     #(diagnostics/c2-reader-fail! "C2-HASH" source-path {} {}))
    (is (= ["C2-HASH"] @calls))
    (is (= "reader artifact identity is unstable or incomplete"
           (second (first @failures))))))

(deftest private-contract-public-parity-dependency-and-authority-boundary
  (let [contract-var (get (ns-interns 'gravity.c2-reader-diagnostics)
                          'namespace-contract)
        contract (var-get contract-var)
        public-api (set (keys (:public-api contract)))]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c2-reader-diagnostics (:namespace contract)))
    (is (= public-api (set (keys (ns-publics 'gravity.c2-reader-diagnostics)))))
    (is (= #{'c2-reader-diagnostic-ids
             'c2-reader-governing-document
             'c2-reader-rejected-designs
             'c2-reader-override-diagnostics
             'with-operations
             'call-entrypoint-body
             'c2-reader-source-overrides
             'c2-reader-message
             'c2-reader-fail!
             'c2-reader-remap-exception!
             'c2-reader-validate-overrides!}
           public-api))
    (is (= #{:fail! :source-span :reader-canonical-hash
             :c2-reader-source-overrides :c2-reader-message
             :c2-reader-fail! :c2-reader-remap-exception!
             :c2-reader-validate-overrides!
             :c2-reader-diagnostic-ids :c2-reader-governing-document
             :c2-reader-rejected-designs :c2-reader-override-diagnostics
             :standard-reader-options}
           (get-in contract [:operation-interposition :accepted-keys])))
    (is (true? (get-in contract
                       [:operation-interposition :partial-overrides?])))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['clojure.core 'clojure.string]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c2-reader-authority
                   :canonical-c2-reader-product-authority
                   :sh03-reader-product-authentication
                   :source-reading
                   :source-authentication
                   :canonical-source-identity
                   :cache-reuse-authority
                   :diagnostic-policy-authority
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (= [:hosted-c2-reader-diagnostic-catalog
            :hosted-c2-reader-diagnostic-payload-policy
            :hosted-c2-reader-fixture-override-routing
            :hosted-c2-reader-exception-remapping]
           (:owns (get-in contract [:ownership]))))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:canonical-c2-authority? contract)))
    (is (false? (:source-authentication? contract)))
    (is (false? (:cache-reuse-authority? contract)))
    (is (false? (:proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.diagnostics)))))
