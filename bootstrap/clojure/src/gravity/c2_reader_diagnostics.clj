(ns gravity.c2-reader-diagnostics
  "Hosted Stage0 C2 reader diagnostic catalog, payload policy, and override routing.

  This leaf owns compatibility diagnostics only. It does not read source,
  authenticate C2 or SH03 products, or grant proof, cache-reuse, self-hosting,
  attestation, or release authority."
  (:require [clojure.string :as str]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail!
    :source-span
    :reader-canonical-hash
    :c2-reader-source-overrides
    :c2-reader-message
    :c2-reader-fail!
    :c2-reader-remap-exception!
    :c2-reader-validate-overrides!})

(def ^:private scalar-operation-keys
  #{:c2-reader-diagnostic-ids
    :c2-reader-governing-document
    :c2-reader-rejected-designs
    :c2-reader-override-diagnostics
    :standard-reader-options})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(def ^:private namespace-contract
  {:namespace 'gravity.c2-reader-diagnostics
   :contract-boundary :hosted-c2-reader-diagnostic-policy
   :public-api
   {'c2-reader-diagnostic-ids {:kind :constant}
    'c2-reader-governing-document {:kind :constant}
    'c2-reader-rejected-designs {:kind :constant}
    'c2-reader-override-diagnostics {:kind :constant}
    'with-operations {:arglists '([operations thunk])}
    'call-entrypoint-body {:arglists '([operation-key operation args])}
    'c2-reader-source-overrides {:arglists '([module])}
    'c2-reader-message {:arglists '([id])}
    'c2-reader-fail! {:arglists '([id source-path subject extra])}
    'c2-reader-remap-exception! {:arglists '([source-path ex])}
    'c2-reader-validate-overrides!
    {:arglists '([source-path overrides source-unit token-stream])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :entrypoint-requirements
    {'c2-reader-fail! #{:fail! :source-span :reader-canonical-hash}
     'c2-reader-remap-exception!
     #{:c2-reader-fail! :standard-reader-options}
     'c2-reader-validate-overrides! #{:c2-reader-fail!}}}
   :ownership
   {:owns [:hosted-c2-reader-diagnostic-catalog
           :hosted-c2-reader-diagnostic-payload-policy
           :hosted-c2-reader-fixture-override-routing
           :hosted-c2-reader-exception-remapping]
    :does-not-own [:canonical-c2-reader-authority
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
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :source-authentication? false
   :cache-reuse-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- valid-string-vector?
  [value]
  (and (vector? value)
       (seq value)
       (every? #(and (string? %) (seq %)) value)))

(defn- valid-rejected-designs?
  [value]
  (and (vector? value)
       (seq value)
       (every? (fn [entry]
                 (and (map? entry)
                      (string? (:diagnostic entry))
                      (seq (:diagnostic entry))
                      (string? (:fixture entry))
                      (seq (:fixture entry))
                      (contains? entry :rejected-design)))
               value)))

(defn- valid-override-map?
  [value]
  (and (map? value)
       (seq value)
       (every? keyword? (keys value))
       (every? #(and (string? %) (seq %)) (vals value))))

(defn- valid-standard-reader-options?
  [value]
  (and (map? value)
       (boolean? (:retain-comments value))
       (set? (:enabled-features value))
       (every? keyword? (:enabled-features value))
       (string? (:extension-policy value))
       (seq (:extension-policy value))))

(defn- validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 reader diagnostics operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 reader diagnostics operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 reader diagnostics operation must be a function"
                      {:operation key :value (get operations key)}))))
  (doseq [[key predicate message]
          [[:c2-reader-diagnostic-ids
            valid-string-vector?
            "C2 diagnostic identifiers must be a nonempty string vector"]
           [:c2-reader-governing-document
            #(and (string? %) (seq %))
            "C2 governing document must be a nonempty string"]
           [:c2-reader-rejected-designs
            valid-rejected-designs?
            "C2 rejected designs must be a nonempty vector of shaped maps"]
           [:c2-reader-override-diagnostics
            valid-override-map?
            "C2 override diagnostics must map keywords to nonempty strings"]
           [:standard-reader-options
            valid-standard-reader-options?
            "standard reader options must have strict hosted shape"]]
          :when (contains? operations key)]
    (when-not (predicate (get operations key))
      (throw (ex-info message {:operation key :value (get operations key)}))))
  operations)

(defn with-operations
  [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 reader diagnostics thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn call-entrypoint-body
  "Enter one extracted function body while retaining recursive bootstrap Var
  interposition. This is the narrow compatibility trampoline used by
  bootstrap wrappers; ordinary leaf callers should use with-operations."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "C2 reader diagnostics entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "C2 reader diagnostics entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "C2 reader diagnostics entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(defn- current-operation
  [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defn- invoke
  [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C2 reader diagnostics require operation " key)
                    {:operation key}))))

(defn- operation-value
  [key default]
  (if (contains? *operations* key)
    (get *operations* key)
    default))

(defn- required-operation-value
  [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (throw (ex-info (str "C2 reader diagnostics require operation " key)
                    {:operation key}))))

(defmacro ^:private definterposable
  [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(def c2-reader-diagnostic-ids
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

(def c2-reader-governing-document
  "docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md")

(def c2-reader-rejected-designs
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

(def c2-reader-override-diagnostics
  {:encoding "C2-ENCODING"
   :abbrev "C2-ABBREV"
   :hash "C2-HASH"})

(definterposable c2-reader-source-overrides :c2-reader-source-overrides
  [module]
  (get-in module [:metadata :compiler :c2-reader] {}))

(definterposable c2-reader-message :c2-reader-message
  [id]
  (case id
    "C2-ENCODING" "source decoding failed or used an undeclared encoding"
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
    "C2-HASH" "reader artifact identity is unstable or incomplete"
    "reader document coverage failed"))

(definterposable c2-reader-fail! :c2-reader-fail!
  [id source-path subject extra]
  (let [raw-span (or (:source-span subject)
                     (:source-span extra)
                     (invoke :source-span source-path 0))
        source-id (or (:source-id subject)
                      (:source-id extra)
                      (get-in subject [:primary :artifact])
                      (get-in extra [:primary :artifact]))
        span (cond-> raw-span
               (and source-id (not (:file raw-span)))
               (assoc :file source-id))
        raw (or (:raw subject) (:raw-spelling subject)
                (:raw extra) (:raw-spelling extra))
        token-id (or (:token-id subject) (:token-id extra))
        form-id (or (:form-id subject) (:form-id extra))
        facts (merge (or (:facts subject) {})
                     (or (:facts extra) {}))
        remediation
        "Regenerate reader artifacts with deterministic decoding, spans, raw literal facts, extension policy, and stable incremental hashes."
        governing-document
        (operation-value :c2-reader-governing-document
                         c2-reader-governing-document)
        defaults
        {:artifact :gravity/diagnostic
         :diagnostic-id
         (invoke :reader-canonical-hash
                 {:rule (keyword id)
                  :primary-artifact source-id
                  :stage :read-source
                  :span (dissoc span :source)
                  :token-id token-id
                  :form-id form-id
                  :facts facts})
         :rule (keyword id)
         :severity :error
         :source-id source-id
         :source-span span
         :primary {:span span :artifact source-id}
         :related []
         :origin-chain [{:kind :source
                         :source-id source-id
                         :path source-path}]
         :profile nil
         :target nil
         :facts facts
         :diagnostic-family :c2-reader
         :stage :read-source
         :document-id "C2"
         :expected-document governing-document
         :involved-artifacts (cond-> [] source-id (conj source-id))
         :token-id token-id
         :form-id form-id
         :raw-spelling raw
         :reader-options (or (:reader-options subject)
                             (:reader-options extra))
         :extension-tag (or (:extension-tag subject)
                            (:extension-tag extra))
         :reader-state {:artifact :gravity/reader-state
                        :stage :read-source
                        :byte-offset (:byte-start span)
                        :line (get-in span [:start :line])
                        :column (get-in span [:start :column])
                        :token-id token-id
                        :form-id form-id}
         :redactions []
         :lifecycle :active
         :remediation remediation
         :remediation-records [{:kind :fix-reader-source}]}
        payload (-> (merge defaults extra)
                    (assoc :artifact (:artifact defaults)
                           :diagnostic-id (:diagnostic-id defaults)
                           :rule (:rule defaults)
                           :severity (:severity defaults)
                           :source-id source-id
                           :source-span span
                           :primary (:primary defaults)
                           :facts facts
                           :diagnostic-family :c2-reader
                           :stage :read-source
                           :document-id "C2"
                           :expected-document governing-document
                           :token-id (:token-id defaults)
                           :form-id (:form-id defaults)
                           :raw-spelling raw
                           :reader-options (:reader-options defaults)
                           :extension-tag (:extension-tag defaults)
                           :remediation remediation
                           :remediation-records
                           [{:kind :fix-reader-source}]))]
    (invoke :fail! id (c2-reader-message id) payload)))

(definterposable c2-reader-remap-exception! :c2-reader-remap-exception!
  [source-path ex]
  (let [data (ex-data ex)
        old-id (:id data)
        cause (str (or (:cause-message data) (:message data)))
        reader-engine-diagnostic
        (when (and (string? old-id) (str/starts-with? old-id "STAGE1"))
          old-id)
        owner-id (case old-id
                   ("STAGE1READER001" "STAGE1READER002") "L1-DELIMITER"
                   "STAGE1READER003" "L1-STRING"
                   "STAGE1READER004" "L1-READER-EXTENSION"
                   "STAGE1READER005" "L1-MAP-ARITY"
                   "STAGE1READER007" "L1-NUMERIC"
                   old-id)
        id (cond
             (= "L1-SOURCE-ENCODING" owner-id) "C2-ENCODING"
             (= "L1-SOURCE-EXTENSION" owner-id) "C2-EXTENSION"
             (= "L1-DELIMITER" owner-id) "C2-DELIMITER"
             (= "L1-STRING" owner-id) "C2-STRING"
             (= "L1-NUMERIC" owner-id) "C2-NUMERIC"
             (= "L1-IDENTIFIER" owner-id) "C2-IDENTIFIER"
             (= "L1-NS-SHAPE" owner-id) "C2-NS-SHAPE"
             (= "L1-MAP-ARITY" owner-id) "C2-MAP"
             (= "L1-METADATA" owner-id) "C2-METADATA"
             (= "L1-READER-EXTENSION" owner-id) "C2-EXTENSION"
             (str/includes? cause "Duplicate key") "C2-SET"
             :else owner-id)
        span (:source-span data)
        reader-state
        (or (:reader-state data)
            {:artifact :gravity/reader-state
             :stage (if (contains? #{"STAGE1READER003"
                                     "STAGE1READER004"
                                     "STAGE1READER007"}
                                   old-id)
                      :lexical-tokenization
                      :recursive-form-building)
             :byte-offset (:byte-start span)
             :line (get-in span [:start :line])
             :column (get-in span [:start :column])
             :token-id (:token-id data)
             :form-id (:form-id data)})
        diagnostic-ids
        (operation-value :c2-reader-diagnostic-ids c2-reader-diagnostic-ids)
        reader-options
        (required-operation-value :standard-reader-options)]
    (if (contains? (set diagnostic-ids) id)
      (let [preserved-fields
            (dissoc data :id :message :diagnostic-family :reader-options)]
        (c2-reader-fail!
         id source-path data
         (cond-> (assoc preserved-fields
                        :cause-message (or (:cause-message data)
                                           (:message data))
                        :reader-options reader-options
                        :reader-state reader-state)
           (and owner-id (not= owner-id id))
           (assoc :remapped-from owner-id)

           reader-engine-diagnostic
           (assoc :reader-engine-diagnostic reader-engine-diagnostic))))
      (throw ex))))

(definterposable c2-reader-validate-overrides! :c2-reader-validate-overrides!
  [source-path overrides source-unit token-stream]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get (operation-value :c2-reader-override-diagnostics
                                        c2-reader-override-diagnostics)
                       fail-kind)]
      (let [failure-token (or (some #(when (= fail-kind (:decoded %)) %)
                                    token-stream)
                              (first token-stream))]
        (c2-reader-fail! id source-path
                         {:source-id (:source-id source-unit)
                          :source-span (:span failure-token)
                          :token-id (:token-id failure-token)
                          :raw (:raw failure-token)
                          :reader-options (:reader-options source-unit)
                          :extension-tag (:extension-tag overrides)}
                         {:missing-fields [fail-kind]})))))
