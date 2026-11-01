(ns gravity.reader.identity-expectations
  "Independent test equations for the two existing downstream artifact contracts."
  (:require [gravity.c2-artifact-identity.canonical :as reader-encoding]
            [gravity.digest :as digest]))

(defn- text [value]
  (binding [*print-length* nil *print-level* nil *print-meta* false
            *print-dup* false *print-readably* true *print-namespace-maps* false]
    (pr-str value)))

(defn- hex [value]
  (apply str (map #(format "%02x" (bit-and 255 %))
                  (.getBytes ^String value java.nio.charset.StandardCharsets/UTF_8))))

(defn native-form [value]
  ;; This is a test encoder for already admitted artifact DATA, not an admission
  ;; API. No production identity, projection, canonicalizer or operation port runs.
  (assert (or (not (instance? clojure.lang.IObj value)) (nil? (meta value))))
  (cond
    (nil? value) [:nil]
    (boolean? value) [:boolean value]
    (integer? value) [:integer (str (biginteger value))]
    (ratio? value) [:ratio (str (numerator value)) (str (denominator value))]
    (instance? java.math.BigDecimal value)
    [:decimal (str (.unscaledValue ^java.math.BigDecimal value)) (long (.scale ^java.math.BigDecimal value))]
    (string? value) [:string value]
    (char? value) [:character (int value)]
    (keyword? value) [:keyword (namespace value) (name value)]
    (symbol? value) [:symbol (namespace value) (name value)]
    (map? value)
    (do (assert (contains? #{clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap clojure.lang.PersistentTreeMap} (class value)))
        (when (= clojure.lang.PersistentTreeMap (class value))
          (assert (identical? clojure.lang.RT/DEFAULT_COMPARATOR (.comparator ^clojure.lang.Sorted value))))
        [:map (->> value
                   (map (fn [[k v]] [(native-form k) (native-form v)]))
                   (sort-by #(hex (text [:gravity/canonical-sort-v1 (first %)])))
                   (mapv (fn [[k v]] [:entry k v])))])
    (set? value)
    (do (assert (contains? #{clojure.lang.PersistentHashSet clojure.lang.PersistentTreeSet} (class value)))
        (when (= clojure.lang.PersistentTreeSet (class value))
          (assert (identical? clojure.lang.RT/DEFAULT_COMPARATOR (.comparator ^clojure.lang.Sorted value))))
        [:set (vec (sort-by #(hex (text [:gravity/canonical-sort-v1 %])) (map native-form value)))])
    (vector? value) [:vector (mapv native-form value)]
    (list? value) [:list (mapv native-form value)]
    :else (throw (ex-info "Outside the artifact test encoding domain" {:class (class value)}))))

(defn macro-hash [preimage]
  (str "sha256:" (digest/sha256-hex (text [:gravity/canonical-edn-v1 (native-form preimage)]))))

(defn resolution-hash [preimage]
  (letfn [(encode [value] (reader-encoding/reader-canonical-value encode value))]
    (reader-encoding/reader-canonical-hash digest/sha256-hex encode preimage)))

(defn- path-neutral [value]
  (cond
    (map? value)
    (let [clean (into {} (for [[k v] value
                              :when (not (#{:workspace-root :invocation-root :actual-source-path} k))]
                          [k (path-neutral v)]))
          source (or (:file clean) (:source clean))]
      (if (and (contains? clean :byte-start) source)
        (cond-> clean (contains? clean :source) (assoc :source source)
                      (contains? clean :file) (assoc :file source))
        clean))
    (vector? value) (mapv path-neutral value)
    (set? value) (set (map path-neutral value))
    (seq? value) (apply list (map path-neutral value))
    :else value))

(defn- run-summary [run]
  {:raw-result {:artifact (get-in run [:raw-template-result :artifact])
                :schema-version (get-in run [:raw-template-result :schema-version])
                :status (get-in run [:raw-template-result :status])}
   :digest-contract (mapv #(select-keys % [:ordinal :purpose]) (:digest-requests run))
   :semantic-resolved-digest (first (:resolved-digests run))
   :resolved-expansion (select-keys (:resolved-expansion run)
                                  [:artifact :schema-version :artifact-id :status :macro :macro-version
                                   :input-syntax-id :output-syntax-id :semantic-binding :profile :target :phase :bounds])
   :trace-replay-id (get-in run [:resolved-expansion :macro-expansion-trace 0 :trace-replay-id])
   :template-verification (:template-verification run)
   :resolved-verification (:resolved-verification run)})

(def macro-plan-fields
  [:artifact :status :semantic-authority :compiled-by :executed-by
   :generic-bridge-residual? :self-hosted? :source-language :source-byte-count
   :source-content-hash :plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash :public-function-hashes :public-function-shapes])

(defn macro-preimage [artifact]
  (let [boundary (:gravity-macro-boundary artifact)
        runs (:expansion-runs boundary)]
    (path-neutral
     {:domain :gravity/sh05-macro-source-artifact-v2
      :kind (:kind artifact) :slice (:slice artifact) :task (:task artifact)
      :status (:status artifact) :document-set (:document-set artifact)
      :governing-document (:governing-document artifact)
      :execution-boundary (:execution-boundary artifact)
      :macro-environment (:macro-environment artifact)
      :pass (:pass artifact) :diagnostics (:diagnostics artifact)
      :expanded-defn-count (:expanded-defn-count artifact)
      :expanded-syntax-stream-id (:expanded-syntax-stream-id artifact)
      :macro-expansion-trace-id (:macro-expansion-trace-id artifact)
      :sh04-syntax-artifact (:sh04-syntax-artifact artifact)
      :boundary-contract (select-keys boundary [:slice :owner :adapter-contract :target-source-reread?
                                               :clojure-adapter-residual? :self-hosted?])
      :plan-binding (select-keys (:plan-binding boundary) macro-plan-fields)
      :expansion-run-count (count runs)
      :expansion-run-chunks
      (mapv (fn [chunk-index entries]
              {:chunk-index chunk-index :start-ordinal (ffirst entries) :item-count (count entries)
               :items (mapv (fn [[ordinal run]] {:ordinal ordinal :summary (run-summary run)}) entries)})
            (range) (partition-all 16 (map-indexed vector runs)))})))

(defn macro-id [artifact] (macro-hash (macro-preimage artifact)))

(defn- resolution-chunks [entries]
  (mapv (fn [index chunk]
          {:chunk-index index :entry-count (count chunk)
           :chunk-id (resolution-hash {:domain :gravity/sh06-resolution-identity-chunk-v1
                                      :chunk-index index :entries (vec chunk)})})
        (range) (partition-all 16 entries)))

(defn resolution-preimage [artifact]
  {:domain :gravity/sh06-resolution-artifact-v1
   :kind (:kind artifact) :slice (:slice artifact) :task (:task artifact)
   :adapter-contract (get-in artifact [:gravity-resolution-boundary :adapter-contract])
   :analysis-artifact-id (get-in artifact [:gravity-resolution-boundary :resolved-analysis :artifact-id])
   :source-revision-id (get-in artifact [:gravity-resolution-boundary :authenticated-resolution-request :module :source-revision-id])
   :upstream-artifact-id (get-in artifact [:sh05-macro-artifact :artifact-id])
   :upstream-syntax-stream-id (get-in artifact [:sh05-macro-artifact :expanded-syntax-stream-id])
   :upstream-trace-id (get-in artifact [:sh05-macro-artifact :macro-expansion-trace-id])
   :binding-id-chunks (resolution-chunks (mapv :binding-id (get-in artifact [:binding-table :bindings])))
   :resolution-chunks (resolution-chunks (mapv #(dissoc % :source-span) (:resolution-table artifact)))
   :dependency-graph-id (resolution-hash {:domain :gravity/sh06-dependency-graph-v1
                                         :nodes (get-in artifact [:dependency-graph :nodes])
                                         :edges (mapv #(dissoc % :source-span) (get-in artifact [:dependency-graph :edges]))})
   :invalidation-id (get-in artifact [:incremental-invalidation-keys :invalidation-id])})

(defn resolution-id [artifact] (resolution-hash (resolution-preimage artifact)))
