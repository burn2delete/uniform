(ns gravity.c3-syntax-diagnostics
  "Hosted Stage0 C3 diagnostic catalog, payload policy, and override routing.

  This leaf owns compatibility diagnostics only. It does not authenticate C2
  or SH04 products, validate canonical C3 semantics, or grant proof,
  self-hosting, attestation, or release authority."
  (:require [gravity.c3-syntax-diagnostics.policy :as policy]))

(def c3-syntax-diagnostic-ids
  ["C3-SHAPE"
   "C3-ID"
   "C3-SPAN"
   "C3-ORIGIN"
   "C3-HYGIENE"
   "C3-CAPTURE"
   "C3-METADATA"
   "C3-FACT-STALE"
   "C3-SERIALIZE"])

(def c3-syntax-governing-document
  "docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md")

(def c3-syntax-rejected-designs
  [{:diagnostic "C3-SHAPE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c3-shape.gravity"
    :rejected-design :raw-list-macro-api}
   {:diagnostic "C3-ID"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c3-id.gravity"
    :rejected-design :unstable-syntax-identity}
   {:diagnostic "C3-SPAN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c3-span.gravity"
    :rejected-design :syntax-without-resolvable-span}
   {:diagnostic "C3-ORIGIN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c3-origin.gravity"
    :rejected-design :syntax-without-source-or-generated-origin}
   {:diagnostic "C3-HYGIENE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c3-hygiene.gravity"
    :rejected-design :hidden-hygiene-state}
   {:diagnostic "C3-CAPTURE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c3-capture.gravity"
    :rejected-design :undeclared-or-accidental-capture}
   {:diagnostic "C3-METADATA"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c3-metadata.gravity"
    :rejected-design :metadata-loss-or-invalid-shape}
   {:diagnostic "C3-FACT-STALE"
    :fixture
    "bootstrap/clojure/fixtures/rejected/compiler-c3-fact-stale.gravity"
    :rejected-design :stale-semantic-fact-use}
   {:diagnostic "C3-SERIALIZE"
    :fixture
    "bootstrap/clojure/fixtures/rejected/compiler-c3-serialize.gravity"
    :rejected-design :non-round-tripping-syntax-artifact}])

(def c3-syntax-override-diagnostics
  {:shape "C3-SHAPE"
   :id "C3-ID"
   :span "C3-SPAN"
   :origin "C3-ORIGIN"
   :hygiene "C3-HYGIENE"
   :capture "C3-CAPTURE"
   :metadata "C3-METADATA"
   :fact-stale "C3-FACT-STALE"
   :serialize "C3-SERIALIZE"})

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private namespace-contract policy/namespace-contract)

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C3 syntax diagnostic thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C3 syntax diagnostics require operation " key)
                    {:operation key}))))

(defn- operation-value [key default]
  (if (contains? *operations* key) (get *operations* key) default))

(defn c3-syntax-source-overrides [module]
  (if-let [operation (current-operation :c3-syntax-source-overrides)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys* :c3-syntax-source-overrides)]
      (operation module))
    (get-in module [:metadata :compiler :c3-syntax] {})))

(defn c3-syntax-overrides-from-forms [forms]
  (if-let [operation (current-operation :c3-syntax-overrides-from-forms)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys*
                    :c3-syntax-overrides-from-forms)]
      (operation forms))
    (let [ns-form (first forms)
          metadata-clause
          (when (and (seq? ns-form) (= 'ns (first ns-form)))
            (first (filter #(and (seq? %) (= :metadata (first %)))
                           (drop 2 ns-form))))]
      (get-in (second metadata-clause) [:compiler :c3-syntax] {}))))

(defn c3-syntax-message [id]
  (if-let [operation (current-operation :c3-syntax-message)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys* :c3-syntax-message)]
      (operation id))
    (case id
      "C3-SHAPE" "syntax object fields are malformed or incomplete"
      "C3-ID" "syntax object identity is unstable or inconsistent"
      "C3-SPAN" "syntax object span is not resolvable"
      "C3-ORIGIN" "syntax object origin chain is missing or broken"
      "C3-HYGIENE" "syntax object hygiene context is malformed or hidden"
      "C3-CAPTURE" "syntax object captures a binding without explicit capture"
      "C3-METADATA" "syntax object metadata is invalid or lost"
      "C3-FACT-STALE" "syntax object facts are stale after transformation"
      "C3-SERIALIZE" "syntax object artifact does not round-trip"
      "syntax object model failed")))

(defn c3-syntax-fail! [id source-path subject extra]
  (if-let [operation (current-operation :c3-syntax-fail!)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys* :c3-syntax-fail!)]
      (operation id source-path subject extra))
    (invoke
     :fail!
     id
     (c3-syntax-message id)
     (merge
      {:source-span (or (:source-span subject)
                        (:span subject)
                        (invoke :source-span source-path 0))
       :diagnostic-family :c3-syntax-object
       :stage :syntax-object-model
       :document-id "C3"
       :expected-document
       (operation-value :c3-syntax-governing-document
                        c3-syntax-governing-document)
       :syntax-id (or (:syntax-id subject) (:syntax/id subject))
       :form-kind (:form-kind subject)
       :phase (:phase subject)
       :producer (:producer subject)
       :origin-chain (:origin subject)
       :hygiene-summary (:hygiene subject)
       :remediation
       "Rebuild syntax objects with stable ids, spans, origin chains, exposed hygiene, versioned facts, and round-tripping serialization."}
      extra))))

(defn c3-syntax-validate-overrides! [source-path overrides]
  (if-let [operation (current-operation :c3-syntax-validate-overrides!)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys* :c3-syntax-validate-overrides!)]
      (operation source-path overrides))
    (when-let [fail-kind (:fail overrides)]
      (when-let [id (get (operation-value :c3-syntax-override-diagnostics
                                          c3-syntax-override-diagnostics)
                         fail-kind)]
        (c3-syntax-fail!
         id source-path
         {:source-span (invoke :source-span source-path 0)
          :producer :fixture-override
          :form-kind fail-kind
          :hygiene {:marks [] :captures []}}
         {:missing-fields [fail-kind]})))))
