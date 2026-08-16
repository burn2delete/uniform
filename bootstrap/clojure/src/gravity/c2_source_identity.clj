(ns gravity.c2-source-identity
  "Hosted Stage0 C2 source identity and reader-record projections.

  This leaf consumes an explicit project context and already-produced reader
  records.  It does not discover project roots, read or decode source bytes,
  construct reader products, authenticate SH03, or grant proof, cache-reuse,
  self-hosting, attestation, or release authority."
  (:require [clojure.string :as str]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def ^:private function-operation-keys
  #{:sha256-hex
    :reader-canonical-hash
    :gravity-source-extension
    :gravity-source-kind
    :reader-normalize-relative-path
    :reader-platform-neutral-absolute-path?
    :reader-valid-project-relative-path?
    :reader-explicit-project-context
    :reader-valid-options?
    :reader-validate-options!
    :reader-project-root-record
    :reader-source-identity-inputs
    :c2-source-unit-record
    :c2-token-record
    :c2-form-record
    :c2-literal-records
    :c2-trivia-records})
(def ^:private operation-keys function-operation-keys)

(def ^:private namespace-contract
  {:namespace 'gravity.c2-source-identity
   :contract-boundary :hosted-c2-source-identity-and-reader-record-projection
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'call-entrypoint-body {:arglists '([operation-key operation args])}
    'reader-normalize-relative-path {:arglists '([path])}
    'reader-platform-neutral-absolute-path?
    {:arglists '([path])}
    'reader-valid-project-relative-path?
    {:arglists '([path])}
    'reader-explicit-project-context {:arglists '([project-context])}
    'reader-valid-options? {:arglists '([reader-options])}
    'reader-validate-options! {:arglists '([reader-options])}
    'reader-project-root-record {:arglists '([project-context])}
    'reader-source-identity-inputs
    {:arglists '([source-text reader-options project-context])}
    'c2-source-unit-record
    {:arglists '([source-path source-text reader-options project-context])}
    'c2-token-record {:arglists '([token source-unit])}
    'c2-form-record {:arglists '([record source-unit])}
    'c2-literal-records {:arglists '([form-tree])}
    'c2-trivia-records {:arglists '([token-stream])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :entrypoint-requirements
    {'reader-source-identity-inputs #{:sha256-hex}
     'c2-source-unit-record
     #{:sha256-hex :reader-canonical-hash
       :gravity-source-extension :gravity-source-kind}}}
   :artifact-inputs [:explicit-project-context
                     :source-text
                     :reader-options
                     :source-path-provenance
                     :hosted-c2-token-records
                     :hosted-c2-form-records]
   :artifact-outputs [:hosted-c2-source-unit
                      :hosted-c2-token-records
                      :hosted-c2-form-records
                      :hosted-c2-literal-records
                      :hosted-c2-trivia-records]
   :ownership
   {:owns [:hosted-c2-project-relative-identity-normalization
           :hosted-c2-reader-options-validation
           :hosted-c2-source-unit-record-projection
           :hosted-c2-token-record-projection
           :hosted-c2-form-record-projection
           :hosted-c2-literal-record-projection
           :hosted-c2-trivia-record-projection]
    :does-not-own [:filesystem-project-root-discovery
                   :project-root-authority
                   :source-reading
                   :source-byte-decoding
                   :source-extension-policy
                   :source-authentication
                   :reader-tokenization
                   :reader-form-construction
                   :canonical-c2-reader-authority
                   :canonical-c2-reader-product-authority
                   :sh03-reader-product-authentication
                   :diagnostic-policy
                   :diagnostic-policy-authority
                   :cache-reuse-authority
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
   :project-root-authority? false
   :source-reading? false
   :source-authentication? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 source-identity operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 source-identity operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 source-identity operation must be a function"
                      {:operation key :value (get operations key)}))))
  operations)

(defn with-operations
  [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 source-identity thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations]
    (thunk)))

(defn call-entrypoint-body
  "Enter one extracted function body with one-shot bootstrap interposition.

  The bypass binding is consumed by the first invocation of the supplied
  operation Var, so recursive calls can still observe an injected operation
  without recursing through the captured original indefinitely."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "C2 source-identity entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "C2 source-identity entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "C2 source-identity entrypoint args must be sequential"
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
    (throw (ex-info (str "C2 source-identity requires operation " key)
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

(definterposable reader-normalize-relative-path
  :reader-normalize-relative-path
  [path]
  (let [slash-path (str/replace (str path) "\\" "/")]
    (->> (str/split slash-path #"/")
         (reduce (fn [segments segment]
                   (cond
                     (or (str/blank? segment) (= "." segment))
                     segments

                     (= ".." segment)
                     (if (and (seq segments) (not= ".." (peek segments)))
                       (pop segments)
                       (conj segments segment))

                     :else
                     (conj segments segment)))
                 [])
         (str/join "/"))))

(definterposable reader-platform-neutral-absolute-path?
  :reader-platform-neutral-absolute-path?
  [path]
  (let [slash-path (str/replace (str path) "\\" "/")]
    (or (str/starts-with? slash-path "/")
        (boolean (re-find #"(?i)^[a-z]:" slash-path)))))

(definterposable reader-valid-project-relative-path?
  :reader-valid-project-relative-path?
  [path]
  (let [normalized-path (reader-normalize-relative-path path)]
    (and (not (reader-platform-neutral-absolute-path? path))
         (not (str/blank? normalized-path))
         (not= ".." (first (str/split normalized-path #"/"))))))

(definterposable reader-explicit-project-context
  :reader-explicit-project-context
  [project-context]
  (let [project-root-id (:project-root-id project-context)
        project-relative-path (:project-relative-path project-context)
        normalized-path (when (string? project-relative-path)
                          (reader-normalize-relative-path
                           project-relative-path))]
    (when-not (and (string? project-root-id)
                   (re-matches #"sha256:[0-9a-f]{64}" project-root-id)
                   (string? normalized-path)
                   (reader-valid-project-relative-path?
                    project-relative-path))
      (throw
       (ex-info
        "reader project context requires a project-root id and relative path"
        {:id "C2-HASH"
         :project-context project-context
         :normalized-project-relative-path normalized-path
         :missing-fields
         (vec (remove #(get project-context %)
                      [:project-root-id :project-relative-path]))})))
    (assoc project-context
           :project-relative-path
           normalized-path)))

(definterposable reader-valid-options?
  :reader-valid-options?
  [reader-options]
  (and (map? reader-options)
       (boolean? (:retain-comments reader-options))
       (set? (:enabled-features reader-options))
       (string? (:extension-policy reader-options))
       (boolean
        (re-matches #"sha256:[0-9a-f]{64}"
                    (:extension-policy reader-options)))))

(definterposable reader-validate-options!
  :reader-validate-options!
  [reader-options]
  (when-not (reader-valid-options? reader-options)
    (throw
     (ex-info
      "reader options must be deterministic and content-addressed"
      {:id "C2-HASH"
       :reader-options reader-options
       :required-fields
       {:retain-comments :boolean
        :enabled-features :set
        :extension-policy :sha256-lowercase-hex}})))
  reader-options)

(definterposable reader-project-root-record
  :reader-project-root-record
  [project-context]
  (let [context (reader-explicit-project-context project-context)]
    {:path (:project-root-path context)
     :project-root-id (:project-root-id context)}))

(definterposable reader-source-identity-inputs
  :reader-source-identity-inputs
  [source-text reader-options project-context]
  (let [context (reader-explicit-project-context project-context)
        options (reader-validate-options! reader-options)]
    {:project-root-id (:project-root-id context)
     :project-relative-path (:project-relative-path context)
     :encoding :utf-8
     :bytes-hash (str "sha256:" (invoke :sha256-hex source-text))
     :reader-options options
     :enabled-features (:enabled-features options)
     :extension-policy (:extension-policy options)}))

(definterposable c2-source-unit-record
  :c2-source-unit-record
  [source-path source-text reader-options project-context]
  (let [context (reader-explicit-project-context project-context)
        identity-inputs (reader-source-identity-inputs source-text
                                                       reader-options
                                                       context)
        project-root (reader-project-root-record context)]
    (merge
     {:artifact :gravity/source-unit
      :source-id (invoke :reader-canonical-hash identity-inputs)
      :path source-path
      :extension (invoke :gravity-source-extension source-path)
      :source-kind (invoke :gravity-source-kind source-path)
      :project-relative-path (:project-relative-path context)
      :project-root (:project-root-id context)
      :project-root-record project-root
      :identity-inputs identity-inputs}
     (select-keys identity-inputs
                  [:encoding :bytes-hash :reader-options
                   :enabled-features :extension-policy]))))

(definterposable c2-token-record
  :c2-token-record
  [token source-unit]
  (let [source-id (:source-id source-unit)]
    (-> token
        (assoc :token-id (keyword (str "tok-" (:index token)))
               :source-id source-id
               :source-path (:path source-unit)
               :span (assoc (:span token) :file source-id)
               :trivia-before []
               :reader-origin :source)
        (dissoc :index))))

(definterposable c2-form-record
  :c2-form-record
  [record source-unit]
  (let [source-id (:source-id source-unit)]
    (-> record
        (assoc :source-id source-id
               :source-path (:path source-unit)
               :span (assoc (:span record) :file source-id)
               :origin (merge {:kind :source
                               :source-id source-id
                               :source-path (:path source-unit)}
                              (when (map? (:origin record))
                                (:origin record)))))))

(definterposable c2-literal-records
  :c2-literal-records
  [form-tree]
  (let [literal-kinds #{:nil :boolean :integer :ratio :decimal :string
                        :character :symbol :keyword :tagged-literal}
        candidates (filter #(contains? literal-kinds (:kind %)) form-tree)]
    (mapv
     (fn [idx {:keys [kind raw value span tag form-id]}]
       {:literal-id (keyword (str "lit-" idx))
        :form-id form-id
        :kind kind
        :raw raw
        :decoded value
        :span span
        :facts
        (case kind
          :integer
          {:radix (cond
                    (re-find #"^[+-]?0[xX]" raw) 16
                    (re-find #"^[+-]?0[bB]" raw) 2
                    :else 10)
           :sign (cond
                   (str/starts-with? raw "-") :negative
                   (str/starts-with? raw "+") :explicit-positive
                   :else :unsigned)
           :exact? true}
          :ratio
          (let [[numerator denominator] (str/split raw #"/" 2)]
            {:numerator-spelling numerator
             :denominator-spelling denominator
             :exact? true})
          :decimal
          {:exponent-spelling (second (re-find #"([eE][+-]?[0-9]+)" raw))
           :exact? false}
          :string
          {:escapes (mapv (fn [[match offset]]
                            {:raw match :character-offset offset})
                          (map vector
                               (re-seq #"\\(?:[btnfr\"\\]|u[0-9A-Fa-f]{4})"
                                       raw)
                               (keep-indexed (fn [offset ch]
                                               (when (= \\ ch) offset))
                                             raw)))}
          :character {:escape raw}
          :symbol {:namespace (namespace value)}
          :keyword {:namespace (namespace value)}
          :tagged-literal {:tag tag}
          {})})
     (range)
     candidates)))

(definterposable c2-trivia-records
  :c2-trivia-records
  [token-stream]
  (mapv (fn [token]
          {:trivia-id (:token-id token)
           :kind (:kind token)
           :raw (:raw token)
           :span (:span token)
           :source-id (:source-id token)
           :source-path (:source-path token)})
        (filter :trivia? token-stream)))
