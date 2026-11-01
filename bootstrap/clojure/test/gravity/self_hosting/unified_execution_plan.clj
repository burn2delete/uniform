(ns gravity.self-hosting.unified-execution-plan
  "Closed, declarative verification planning for bounded development and fresh acceptance.

  The catalog is intentionally a compatibility-phase projection.  This
  namespace owns selection and policy validation only; process launchers,
  resource brokers, caches, and the authoritative proof executor remain
  substrates until independently migrated and reviewed."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh07-conformance-table :as conformance])
  (:import (java.nio.charset StandardCharsets)
           (java.nio ByteBuffer)
           (java.nio.channels FileChannel)
           (java.nio.file Files LinkOption Paths StandardOpenOption)
           (java.security MessageDigest)))

(def catalog-path "contracts/verification-catalog.edn")
(def catalog-schema :gravity/verification-catalog-v1)
(def plan-schema :gravity/unified-execution-plan-v1)
(def receipt-schema :gravity/unified-execution-receipt-v1)
(def artifact-invocation-schema :gravity/unified-artifact-invocation-v1)
(def artifact-operation :run-fast-artifact)
(def namespace-authority-schema
  :gravity/verification-namespace-authority-v1)
(def ^:private artifact-reference-schema
  :gravity/authenticated-artifact-result-reference-v1)
(def ^:private artifact-reference-keys
  #{:schema :reference-id :operation :base-plan-id :binding-id :row-id :stage
    :projection-id :result-id :selected-artifact-id
    :source-content-hash :source-provenance-id :source-relative-path
    :source-provenance :cache-decision :execution-boundary :authority
    :status})
(def ^:private artifact-reference-id-keys
  [:schema :operation :base-plan-id :binding-id :row-id :stage
   :projection-id :result-id :selected-artifact-id
   :source-content-hash :source-provenance-id :source-relative-path
   :source-provenance :cache-decision :execution-boundary :authority
   :status])
(def ^:private receipt-keys
  #{:schema :version :plan-id :route-id :lane :authority :authoritative?
    :authority-ceiling :summary :selection :closure-identity :cache :execution
    :status :diagnostics :evidence :integrity :fresh-replayable?
    :required-evidence :nonclaims :fresh-authoritative-gate-required?
    :receipt-id})

(def ^:private catalog-keys
  #{:schema :version :authority :authoritative? :phase :complete?
    :description :lanes :resource-classes :modules :routes :suites
    :legacy-mechanisms :coverage :evidence-policy :conformance})
(def ^:private conformance-keys
  #{:source-of-truth :schema :authoritative? :suites :nonclaims :authority
    :suite-order :coverage :preserved-bespoke :complete? :version})
(def ^:private lane-keys
  #{:id :kind :authority :authoritative? :incremental? :bounded? :warm?
    :reproducible? :cache-policy :execution-mode :receipt-policy :nonclaims})
(def ^:private lane-cache-keys
  #{:mode :authoritative? :fresh-replayable-required?})
(def ^:private lane-receipt-keys
  #{:schema :required? :authority-ceiling})
(def ^:private resource-keys
  #{:id :max-concurrency :timeout-ms :max-output-bytes})
(def ^:private module-keys
  #{:id :owner :source-paths :artifact-request :ownership-scope
    :required-evidence})
(def ^:private artifact-stage-order
  "The closed compiler-artifact stages that a module may request.

  A module's request names the earliest artifact needed by a route.  The
  source path and owner are projected into each plan so downstream adapters do
  not infer provenance from a filename or route id."
  [:source :reader :syntax :macroexpansion :resolution :checked-core])
(def ^:private artifact-stages (set artifact-stage-order))
(def ^:private artifact-stage-rank
  (zipmap artifact-stage-order (range)))
(def ^:private fixture-source-binding-schema
  :gravity/fixture-source-binding-v1)
(def ^:private fixture-source-binding-keys
  #{:schema :plan-id :catalog-id :route-id :suite-id :row-id :status
    :module-id :module-owner :stage :gravity-path :qst-path
    :fixture-sha256 :fixture-bytes :authenticated? :binding-id})
(def ^:private artifact-request-keys
  #{:module-id :module-owner :stage :source-path})
(def ^:private selection-keys #{:kind :namespace :selectors})
(def ^:private closure-keys
  #{:module-ids :paths :complete? :identity-inputs})
(def ^:private closure-optional-keys #{:fixture-suite-ids})
(def ^:private bounds-keys #{:timeout-ms :max-output-bytes})
(def ^:private route-cache-keys #{:mode :key-inputs :authoritative?})
(def ^:private route-receipt-keys #{:schema :required-fields})
(def ^:private legacy-mechanism-keys
  #{:route-ids :label-policy :membership-semantics})
(def ^:private legacy-label-policy-keys #{:kind :prefix})
(def ^:private allowed-legacy-mechanisms
  #{:iteration-cache-runner :parallel-shard-runner})
(def ^:private allowed-legacy-label-kinds #{:strip-prefix})
(def ^:private route-keys
  #{:id :module-id :replacement-for :migration-status :replaces? :exhaustive?
    :lanes :selection :dependency-closure :resource-class :bounds
    :cache-policy :execution-mode :executor :receipt-policy :required-evidence
    :authority-ceiling :nonclaims})
(def ^:private suite-keys
  #{:id :owner :route-ids :fixture-policy :conformance-kind})
(def ^:private coverage-keys
  #{:owner :completeness :exhaustive? :replacement-authority
    :unmigrated-responsibilities :source-of-truth :route-ids :accounting})
(def ^:private coverage-accounting-keys
  #{:route-count :module-count :suite-count :selector-count
    :exact-var-selector-count :proof-contract-selector-count
    :fixture-suite-count :accepted-fixture-count :rejected-fixture-count
    :fixture-pair-count})
(def ^:private namespace-authority-keys
  #{:schema :authority :authoritative? :manifest-id :entries :projection-id})
(def ^:private namespace-authority-entry-keys
  #{:namespace :path :selectors :route-ids})
(def ^:private evidence-policy-keys
  #{:required :authority-ceilings :fresh-acceptance-requires})
(def ^:private plan-keys
  #{:schema :version :catalog-id :route-id :lane :authority :authoritative?
    :authority-ceiling :selection :dependency-closure :closure-identity
    :artifact-request
    :resource-policy :cache-policy :execution-mode :executor
    :receipt-generation :required-evidence :nonclaims :guards
    :fresh-authoritative-gate-required? :plan-id})
(def ^:private artifact-plan-keys
  "The ordinary plan shape plus one content-bound operation extension.

  Ordinary plans never acquire this key, so their canonical value and plan id
  remain byte-identical.  The extension is still validated by the same plan
  engine and receipt path; it is not a second executable plan subsystem."
  (conj plan-keys :artifact-invocation))
(def ^:private artifact-invocation-keys
  #{:schema :version :operation :base-plan-id :row-id :stage :binding-id :binding})
(def ^:private artifact-row-id-max-length 256)
(def ^:private allowed-lanes #{:fast :fresh})
(def ^:private allowed-cache-modes #{:eligible :disabled :forbidden})
(def ^:private allowed-execution-modes
  #{:warm-bounded-process :fresh-isolated-process})
(def ^:private allowed-authority-ceilings #{:non-authoritative :acceptance-only})
(def ^:private allowed-selection-kinds #{:exact-vars :proof-contract-module-catalog})
(def ^:private executor-keys #{:adapter :bounded? :fresh-capable? :authority})
(def ^:private executor-request-keys
  #{:operation :shard :result-contract-id})
(def ^:private cached-result-contract-id
  :gravity/sh07-cached-shard-result-v1)
(def ^:private cache-observation-schema
  :gravity/unified-cache-observation-v1)
(def ^:private cache-observation-keys
  #{:schema :result-contract-id :artifact :shard :context :counters
    :elapsed-ms :cache-authoritative? :fresh-authoritative-run-required?})
(def ^:private cache-observation-context-keys
  #{:request-schema-version :adapter :checked-core-source-content-hash
    :checked-core-plan-semantic-hash :java-runtime-version :clojure-version})
(def ^:private cache-observation-counter-keys
  #{:sh06-hits :sh06-misses :core-hits :core-misses
    :verification-hits :verification-misses})
(def ^:private cache-observation-max-count 1000000)
(def ^:private cache-observation-max-elapsed-ms (* 24 60 60 1000))
(def ^:private cache-observation-max-text-length 1024)
(def ^:private cached-artifact :gravity/sh07-cached-shard-result)
(def ^:private allowed-executors
  #{:exact-tree-warm-worker :authoritative-proof-runner})
(def ^:private required-cache-key-inputs
  #{:dependency-closure :source-bytes :tool :policy})
(def ^:private maximum-resource-timeout-ms (* 4 60 60 1000))
(def ^:private maximum-resource-output-bytes (* 256 1024))
;; Validate arbitrary-precision input before narrowing to the machine integer
;; representation retained by the existing plan shape.  Calling `long` on an
;; untrusted BigInt before this comparison can throw a host exception (or
;; silently narrow on some runtimes), bypassing the stable UEP diagnostic.
(def ^:private maximum-safe-long (bigint Long/MAX_VALUE))
(def ^:private maximum-repository-relative-path-length 4096)
(def ^:private drive-path-pattern #"(?i)^[a-z]:.*")

(defn- fail!
  [id message data]
  (throw (ex-info message (merge {:id id} data))))

(defn- ensure!
  [value id message data]
  (when-not value
    (fail! id message data))
  value)

(defn- sorted-values
  [values]
  (vec (sort-by pr-str values)))

(defn- exact-keys!
  [value allowed id path]
  (ensure! (map? value) id "Expected a map in closed verification schema"
           {:path path :value value})
  (let [actual (set (keys value))]
    (when-not (= actual allowed)
      (fail! id "Verification schema has missing or unknown keys"
             {:path path
              :missing (sorted-values (set/difference allowed actual))
              :unknown (sorted-values (set/difference actual allowed))})))
  value)

(defn- positive-integer!
  [value id path]
  (ensure! (and (integer? value)
                (pos? value)
                (<= value maximum-safe-long))
           id "Verification bound must be a positive integer"
           {:path path :value value})
  (long value))

(defn- non-negative-integer!
  [value id path]
  (ensure! (and (integer? value)
                (not (neg? value))
                (<= value maximum-safe-long))
           id "Verification count must be a non-negative integer"
           {:path path :value value})
  (long value))

(defn- boolean!
  [value id path]
  (ensure! (boolean? value) id "Verification policy value must be boolean"
           {:path path :value value})
  value)

(defn- non-empty-vector!
  [value id path]
  (ensure! (and (vector? value) (seq value))
           id "Verification value must be a non-empty vector"
           {:path path :value value})
  value)

(defn- normalized-repository-relative-path?
  [value]
  (and (string? value)
       (not (str/blank? value))
       (<= (count value) maximum-repository-relative-path-length)
       (not (str/starts-with? value "/"))
       (not (str/starts-with? value "~"))
       (not (str/includes? value "\\"))
       (not (re-matches drive-path-pattern value))
       (every? (fn [character]
                 (let [code (int character)]
                   (<= 32 code 126)))
               value)
       (let [segments (str/split value #"/" -1)]
         (and (every? #(and (not (str/blank? %))
                             (not= "." %)
                             (not= ".." %))
                       segments)
              (= value (str/join "/" segments))))))

(defn- validate-lane!
  [lane-id lane]
  (exact-keys! lane lane-keys "UEP-CATALOG-LANE-KEYS" [:lanes lane-id])
  (ensure! (= lane-id (:id lane)) "UEP-CATALOG-LANE-ID"
           "Lane id does not match its catalog key"
           {:path [:lanes lane-id] :expected lane-id :actual (:id lane)})
  (ensure! (contains? allowed-lanes lane-id) "UEP-CATALOG-LANE"
           "Unknown verification lane" {:lane lane-id})
  (doseq [key [:authoritative? :incremental? :bounded? :warm? :reproducible?]]
    (boolean! (get lane key) "UEP-CATALOG-LANE-BOOLEAN" [:lanes lane-id key]))
  (ensure! (keyword? (:authority lane)) "UEP-CATALOG-LANE-AUTHORITY"
           "Lane authority must be a keyword" {:lane lane-id})
  (ensure! (contains? allowed-execution-modes (:execution-mode lane))
           "UEP-CATALOG-LANE-MODE" "Lane has an unknown execution mode"
           {:lane lane-id :execution-mode (:execution-mode lane)})
  (exact-keys! (:cache-policy lane) lane-cache-keys
               "UEP-CATALOG-LANE-CACHE-KEYS" [:lanes lane-id :cache-policy])
  (ensure! (contains? allowed-cache-modes (get-in lane [:cache-policy :mode]))
           "UEP-CATALOG-LANE-CACHE" "Lane has an unknown cache mode"
           {:lane lane-id :mode (get-in lane [:cache-policy :mode])})
  (boolean! (get-in lane [:cache-policy :authoritative?])
            "UEP-CATALOG-LANE-CACHE-BOOLEAN"
            [:lanes lane-id :cache-policy :authoritative?])
  (boolean! (get-in lane [:cache-policy :fresh-replayable-required?])
            "UEP-CATALOG-LANE-CACHE-BOOLEAN"
            [:lanes lane-id :cache-policy :fresh-replayable-required?])
  (exact-keys! (:receipt-policy lane) lane-receipt-keys
               "UEP-CATALOG-LANE-RECEIPT-KEYS" [:lanes lane-id :receipt-policy])
  (ensure! (= receipt-schema (get-in lane [:receipt-policy :schema]))
           "UEP-CATALOG-LANE-RECEIPT" "Lane receipt schema is not recognized"
           {:lane lane-id :schema (get-in lane [:receipt-policy :schema])})
  (boolean! (get-in lane [:receipt-policy :required?])
            "UEP-CATALOG-LANE-RECEIPT" [:lanes lane-id :receipt-policy :required?])
  (ensure! (contains? allowed-authority-ceilings
                        (get-in lane [:receipt-policy :authority-ceiling]))
           "UEP-CATALOG-LANE-RECEIPT" "Lane receipt authority ceiling is unknown"
           {:lane lane-id})
  (non-empty-vector! (:nonclaims lane) "UEP-CATALOG-LANE-NONCLAIMS"
                     [:lanes lane-id :nonclaims])
  (when (= :fast lane-id)
    (ensure! (and (= :non-authoritative (:authority lane))
                  (false? (:authoritative? lane))
                  (:incremental? lane)
                  (:bounded? lane)
                  (:warm? lane)
                  (= :eligible (get-in lane [:cache-policy :mode]))
                  (= :warm-bounded-process (:execution-mode lane)))
             "UEP-CATALOG-FAST-POLICY"
             "Fast lane is not bounded, warm, cacheable, and non-authoritative"
             {:lane lane-id}))
  (when (= :fresh lane-id)
    (ensure! (and (= :authoritative (:authority lane))
                  (:authoritative? lane)
                  (false? (:incremental? lane))
                  (:bounded? lane)
                  (false? (:warm? lane))
                  (:reproducible? lane)
                  (= :forbidden (get-in lane [:cache-policy :mode]))
                  (= :fresh-isolated-process (:execution-mode lane)))
             "UEP-CATALOG-FRESH-POLICY"
             "Fresh lane is not isolated, cache-free, reproducible, and bounded"
             {:lane lane-id}))
  lane)

(defn- validate-resource!
  [class-id resource]
  (exact-keys! resource resource-keys "UEP-CATALOG-RESOURCE-KEYS"
               [:resource-classes class-id])
  (ensure! (= class-id (:id resource)) "UEP-CATALOG-RESOURCE-ID"
           "Resource id does not match its catalog key"
           {:path [:resource-classes class-id]})
  (doseq [key [:max-concurrency :timeout-ms :max-output-bytes]]
    (positive-integer! (get resource key) "UEP-CATALOG-RESOURCE-BOUND"
                       [:resource-classes class-id key]))
  (ensure! (<= (:timeout-ms resource) maximum-resource-timeout-ms)
           "UEP-CATALOG-RESOURCE-BOUND" "Resource timeout exceeds global bound" {})
  (ensure! (<= (:max-output-bytes resource) maximum-resource-output-bytes)
           "UEP-CATALOG-RESOURCE-BOUND" "Resource output exceeds global bound" {})
  resource)

(defn- validate-module!
  [module-id module]
  (exact-keys! module module-keys "UEP-CATALOG-MODULE-KEYS" [:modules module-id])
  (ensure! (= module-id (:id module)) "UEP-CATALOG-MODULE-ID"
           "Module id does not match its catalog key" {:module module-id})
  (ensure! (keyword? (:owner module)) "UEP-CATALOG-MODULE-OWNER"
           "Module owner must be a keyword" {:module module-id})
  (non-empty-vector! (:source-paths module) "UEP-CATALOG-MODULE-SOURCES"
                     [:modules module-id :source-paths])
  (ensure! (every? string? (:source-paths module)) "UEP-CATALOG-MODULE-SOURCES"
           "Module source paths must be strings" {:module module-id})
  (ensure! (= (count (:source-paths module))
              (count (distinct (:source-paths module))))
           "UEP-CATALOG-MODULE-SOURCES"
           "Module source paths must be unique" {:module module-id})
  (doseq [path (:source-paths module)]
    (ensure! (normalized-repository-relative-path? path)
             "UEP-CATALOG-MODULE-SOURCES"
             "Module source path is not a normalized repository-relative path"
             {:module module-id :path path}))
  (ensure! (contains? artifact-stages (:artifact-request module))
           "UEP-CATALOG-MODULE-ARTIFACT"
           "Module artifact request stage is not recognized"
           {:module module-id :artifact-request (:artifact-request module)})
  (ensure! (keyword? (:ownership-scope module)) "UEP-CATALOG-MODULE-OWNERSHIP"
           "Module ownership scope must be a keyword" {:module module-id})
  (non-empty-vector! (:required-evidence module) "UEP-CATALOG-MODULE-EVIDENCE"
                     [:modules module-id :required-evidence])
  module)

(defn- module-artifact-request
  "Project one module's reviewed artifact request into a closed plan value.

  The catalog currently owns one source path per module.  Keep the projection
  explicit and fail closed if a future module needs a multi-source artifact
  until the pipeline has a corresponding source-unit graph request."
  [module-id module]
  (ensure! (= 1 (count (:source-paths module)))
           "UEP-CATALOG-MODULE-ARTIFACT"
           "A single-source artifact request is required by this pipeline"
           {:module module-id :source-paths (:source-paths module)})
  {:module-id module-id
   :module-owner (:owner module)
   :stage (:artifact-request module)
   :source-path (first (:source-paths module))})

(defn- validate-artifact-request!
  [request modules path]
  (exact-keys! request artifact-request-keys
               "UEP-ARTIFACT-REQUEST-KEYS" path)
  (ensure! (string? (:module-id request))
           "UEP-ARTIFACT-REQUEST"
           "Artifact request module id must be a string"
           {:path path :module-id (:module-id request)})
  (let [module (get modules (:module-id request))]
    (ensure! (map? module)
             "UEP-ARTIFACT-REQUEST"
             "Artifact request references an unknown module"
             {:path path :module-id (:module-id request)})
    (ensure! (keyword? (:module-owner request))
             "UEP-ARTIFACT-REQUEST"
             "Artifact request module owner must be a keyword"
             {:path path :module-owner (:module-owner request)})
    (ensure! (= (:module-owner request) (:owner module))
             "UEP-ARTIFACT-REQUEST"
             "Artifact request module owner does not match the catalog"
             {:path path :module-id (:module-id request)})
    (ensure! (contains? artifact-stages (:stage request))
             "UEP-ARTIFACT-REQUEST"
             "Artifact request stage is not recognized"
             {:path path :stage (:stage request)})
    (ensure! (and (string? (:source-path request))
                  (some #{(:source-path request)} (:source-paths module)))
             "UEP-ARTIFACT-REQUEST"
             "Artifact request source path is not owned by its module"
             {:path path :module-id (:module-id request)
              :source-path (:source-path request)}))
  request)

(defn- validate-selection!
  [route-id selection]
  (exact-keys! selection selection-keys "UEP-CATALOG-SELECTION-KEYS"
               [:routes route-id :selection])
  (ensure! (contains? allowed-selection-kinds (:kind selection))
           "UEP-CATALOG-SELECTION-KIND" "Route selection kind is unknown"
           {:route route-id :kind (:kind selection)})
  (ensure! (string? (:namespace selection)) "UEP-CATALOG-SELECTION-NAMESPACE"
           "Route selection namespace must be a string" {:route route-id})
  (non-empty-vector! (:selectors selection) "UEP-CATALOG-SELECTION-SELECTORS"
                     [:routes route-id :selection :selectors])
  (ensure! (and (every? string? (:selectors selection))
                (= (count (:selectors selection))
                   (count (distinct (:selectors selection)))))
           "UEP-CATALOG-SELECTION-SELECTORS"
           "Route selectors must be distinct strings" {:route route-id})
  selection)

(defn- route-test-relative-path
  "Derive the reviewed test path from a route selection namespace.

  This is the same closed namespace-to-path mapping used by route validation;
  projections must not infer a different path from a route id or filesystem
  discovery."
  [namespace]
  (str (-> namespace
           (str/replace "." "/")
           (str/replace "-" "_"))
       ".clj"))

(defn- validate-closure!
  [route-id closure module-ids]
  (ensure! (map? closure) "UEP-CATALOG-CLOSURE-KEYS"
           "Dependency closure must be a map" {:route route-id})
  (let [unknown (set/difference (set (keys closure))
                                (set/union closure-keys closure-optional-keys))]
    (when (seq unknown)
      (fail! "UEP-CATALOG-CLOSURE-KEYS"
             "Dependency closure has unknown keys"
             {:route route-id :unknown (sorted-values unknown)})))
  (doseq [key closure-keys]
    (ensure! (contains? closure key) "UEP-CATALOG-CLOSURE-KEYS"
             "Dependency closure omits a required key" {:route route-id :key key}))
  (when-let [suite-ids (:fixture-suite-ids closure)]
    (non-empty-vector! suite-ids "UEP-CATALOG-CLOSURE-FIXTURES"
                       [:routes route-id :dependency-closure :fixture-suite-ids])
    (ensure! (every? string? suite-ids) "UEP-CATALOG-CLOSURE-FIXTURES"
             "Fixture suite ids must be strings" {:route route-id})
    (ensure! (= (count suite-ids) (count (distinct suite-ids)))
             "UEP-CATALOG-CLOSURE-FIXTURES"
             "Fixture suite ids must be distinct" {:route route-id}))
  (non-empty-vector! (:module-ids closure) "UEP-CATALOG-CLOSURE-MODULES"
                     [:routes route-id :dependency-closure :module-ids])
  (ensure! (every? string? (:module-ids closure)) "UEP-CATALOG-CLOSURE-MODULES"
           "Dependency closure module ids must be strings" {:route route-id})
  (ensure! (every? #(contains? module-ids %) (:module-ids closure))
           "UEP-CATALOG-CLOSURE-MODULES" "Dependency closure references unknown module"
           {:route route-id :module-ids (:module-ids closure)})
  (non-empty-vector! (:paths closure) "UEP-CATALOG-CLOSURE-PATHS"
                     [:routes route-id :dependency-closure :paths])
  (ensure! (every? string? (:paths closure)) "UEP-CATALOG-CLOSURE-PATHS"
           "Dependency closure paths must be strings" {:route route-id})
  (ensure! (= (count (:paths closure))
              (count (distinct (:paths closure))))
           "UEP-CATALOG-CLOSURE-PATHS"
           "Dependency closure paths must be unique"
           {:route route-id})
  (doseq [path (:paths closure)]
    (ensure! (normalized-repository-relative-path? path)
             "UEP-CATALOG-CLOSURE-PATHS"
             "Dependency closure path is not a normalized repository-relative path"
             {:route route-id :path path}))
  (boolean! (:complete? closure) "UEP-CATALOG-CLOSURE-COMPLETE"
            [:routes route-id :dependency-closure :complete?])
  (ensure! (:complete? closure) "UEP-CATALOG-CLOSURE-INCOMPLETE"
           "An execution route may not use an incomplete dependency closure"
           {:route route-id})
  (non-empty-vector! (:identity-inputs closure) "UEP-CATALOG-CLOSURE-IDENTITY"
                     [:routes route-id :dependency-closure :identity-inputs])
  (ensure! (every? keyword? (:identity-inputs closure))
           "UEP-CATALOG-CLOSURE-IDENTITY"
           "Dependency identity inputs must be keywords" {:route route-id})
  closure)

(defn- validate-route!
  [route-id route modules resources lanes]
  (exact-keys! route route-keys "UEP-CATALOG-ROUTE-KEYS" [:routes route-id])
  (ensure! (= route-id (:id route)) "UEP-CATALOG-ROUTE-ID"
           "Route id does not match its catalog key" {:route route-id})
  (ensure! (contains? modules (:module-id route)) "UEP-CATALOG-ROUTE-MODULE"
           "Route references an unknown module" {:route route-id})
  (non-empty-vector! (:lanes route) "UEP-CATALOG-ROUTE-LANES"
                     [:routes route-id :lanes])
  (ensure! (and (every? #(contains? lanes %) (:lanes route))
                (= (count (:lanes route)) (count (distinct (:lanes route)))))
           "UEP-CATALOG-ROUTE-LANES" "Route lanes must be known and distinct"
           {:route route-id :lanes (:lanes route)})
  (validate-selection! route-id (:selection route))
  (validate-closure! route-id (:dependency-closure route) (set (keys modules)))
  (ensure! (some #{(:module-id route)}
                 (get-in route [:dependency-closure :module-ids]))
           "UEP-CATALOG-ROUTE-CLOSURE" "Route closure omits its owning module"
           {:route route-id :module-id (:module-id route)})
  (let [closure-paths (set (get-in route [:dependency-closure :paths]))]
    (doseq [module-id (get-in route [:dependency-closure :module-ids])
            :let [source-paths (get-in modules [module-id :source-paths])]]
      (ensure! (every? closure-paths source-paths)
               "UEP-CATALOG-ROUTE-CLOSURE"
               "Route closure omits a referenced module source path"
               {:route route-id :module-id module-id
                :missing (sorted-values (remove closure-paths source-paths))})))
  (let [namespace (:namespace (:selection route))
        test-relative (route-test-relative-path namespace)
        required-paths [(str "bootstrap/clojure/test/" test-relative)
                        "contracts/stage0-incremental-test-dependencies.edn"]
        closure-paths (set (get-in route [:dependency-closure :paths]))]
    (ensure! (every? closure-paths required-paths)
             "UEP-CATALOG-ROUTE-CLOSURE"
             "Route closure omits its test source or reviewed dependency contract"
             {:route route-id
              :missing (sorted-values (remove closure-paths required-paths))}))
  (ensure! (contains? resources (:resource-class route))
           "UEP-CATALOG-ROUTE-RESOURCE" "Route references an unknown resource class"
           {:route route-id :resource-class (:resource-class route)})
  (exact-keys! (:bounds route) bounds-keys "UEP-CATALOG-ROUTE-BOUNDS"
               [:routes route-id :bounds])
  (let [resource (get resources (:resource-class route))]
    (doseq [key [:timeout-ms :max-output-bytes]]
      (positive-integer! (get-in route [:bounds key])
                         "UEP-CATALOG-ROUTE-BOUNDS"
                         [:routes route-id :bounds key])
      (ensure! (<= (get-in route [:bounds key])
                   (get resource key))
               "UEP-CATALOG-ROUTE-BOUNDS"
               "Route bound exceeds its resource-class ceiling"
               {:route route-id :bound key
                :value (get-in route [:bounds key])
                :ceiling (get resource key)})))
  (exact-keys! (:cache-policy route) route-cache-keys
               "UEP-CATALOG-ROUTE-CACHE-KEYS" [:routes route-id :cache-policy])
  (ensure! (contains? allowed-cache-modes (get-in route [:cache-policy :mode]))
           "UEP-CATALOG-ROUTE-CACHE" "Route cache mode is unknown"
           {:route route-id})
  (boolean! (get-in route [:cache-policy :authoritative?])
            "UEP-CATALOG-ROUTE-CACHE" [:routes route-id :cache-policy :authoritative?])
  (non-empty-vector! (get-in route [:cache-policy :key-inputs])
                     "UEP-CATALOG-ROUTE-CACHE"
                     [:routes route-id :cache-policy :key-inputs])
  (ensure! (every? keyword? (get-in route [:cache-policy :key-inputs]))
           "UEP-CATALOG-ROUTE-CACHE" "Route cache key inputs must be keywords"
           {:route route-id})
  (ensure! (set/subset? required-cache-key-inputs
                          (set (get-in route [:cache-policy :key-inputs])))
           "UEP-CATALOG-ROUTE-CACHE"
           "Route cache key inputs omit dependency, source, tool, or policy identity"
           {:route route-id :required required-cache-key-inputs
            :actual (get-in route [:cache-policy :key-inputs])})
  (ensure! (contains? allowed-execution-modes (:execution-mode route))
           "UEP-CATALOG-ROUTE-MODE" "Route execution mode is unknown"
           {:route route-id})
  (let [executor (:executor route)
        allowed-keys (if (and (map? executor) (contains? executor :request))
                       (conj executor-keys :request)
                       executor-keys)]
    (exact-keys! executor allowed-keys "UEP-CATALOG-ROUTE-EXECUTOR-KEYS"
                 [:routes route-id :executor])
    (when (and (map? executor) (contains? executor :request))
      (let [request (:request executor)]
        (exact-keys! request executor-request-keys
                     "UEP-CATALOG-ROUTE-EXECUTOR-REQUEST-KEYS"
                     [:routes route-id :executor :request])
        (ensure! (= :cached-shard (:operation request))
                 "UEP-CATALOG-ROUTE-EXECUTOR-REQUEST"
                 "Executor request operation is not the cached-shard operation"
                 {:route route-id :operation (:operation request)})
        (ensure! (and (string? (:shard request))
                      (not (str/blank? (:shard request)))
                      (<= 1 (count (:shard request)) 128))
                 "UEP-CATALOG-ROUTE-EXECUTOR-REQUEST"
                 "Executor cached shard must be bounded nonempty text"
                 {:route route-id :shard (:shard request)})
        (ensure! (= cached-result-contract-id (:result-contract-id request))
                 "UEP-CATALOG-ROUTE-EXECUTOR-REQUEST"
                 "Executor cached result contract is not recognized"
                 {:route route-id
                  :result-contract-id (:result-contract-id request)})
        (ensure! (and (= [:fast] (:lanes route))
                      (= :warm-bounded-process (:execution-mode route))
                      (= :eligible (get-in route [:cache-policy :mode]))
                      (= :non-authoritative (:authority-ceiling route)))
                 "UEP-CATALOG-ROUTE-EXECUTOR-REQUEST-POLICY"
                 "Cached executor requests require fast eligible non-authoritative execution"
                 {:route route-id}))))
  (ensure! (contains? allowed-executors (get-in route [:executor :adapter]))
           "UEP-CATALOG-ROUTE-EXECUTOR" "Route executor adapter is unknown"
           {:route route-id :adapter (get-in route [:executor :adapter])})
  (boolean! (get-in route [:executor :bounded?]) "UEP-CATALOG-ROUTE-EXECUTOR"
            [:routes route-id :executor :bounded?])
  (boolean! (get-in route [:executor :fresh-capable?]) "UEP-CATALOG-ROUTE-EXECUTOR"
            [:routes route-id :executor :fresh-capable?])
  (ensure! (keyword? (get-in route [:executor :authority]))
           "UEP-CATALOG-ROUTE-EXECUTOR" "Executor authority must be a keyword"
           {:route route-id})
  (exact-keys! (:receipt-policy route) route-receipt-keys
               "UEP-CATALOG-ROUTE-RECEIPT-KEYS" [:routes route-id :receipt-policy])
  (ensure! (= receipt-schema (get-in route [:receipt-policy :schema]))
           "UEP-CATALOG-ROUTE-RECEIPT" "Route receipt schema is not recognized"
           {:route route-id})
  (non-empty-vector! (get-in route [:receipt-policy :required-fields])
                     "UEP-CATALOG-ROUTE-RECEIPT"
                     [:routes route-id :receipt-policy :required-fields])
  (non-empty-vector! (:required-evidence route) "UEP-CATALOG-ROUTE-EVIDENCE"
                     [:routes route-id :required-evidence])
  (ensure! (contains? allowed-authority-ceilings (:authority-ceiling route))
           "UEP-CATALOG-ROUTE-AUTHORITY" "Route authority ceiling is unknown"
           {:route route-id})
  (non-empty-vector! (:nonclaims route) "UEP-CATALOG-ROUTE-NONCLAIMS"
                     [:routes route-id :nonclaims])
  (ensure! (and (vector? (:replacement-for route))
                (seq (:replacement-for route))
                (every? keyword? (:replacement-for route))
                (= (count (:replacement-for route))
                   (count (distinct (:replacement-for route)))))
           "UEP-CATALOG-ROUTE-MIGRATION" "Route legacy mechanisms must be keywords"
           {:route route-id})
  (ensure! (= :compatibility-projection (:migration-status route))
           "UEP-CATALOG-ROUTE-MIGRATION"
           "Initial routes are compatibility projections, not replacements"
           {:route route-id})
  (boolean! (:replaces? route) "UEP-CATALOG-ROUTE-MIGRATION"
            [:routes route-id :replaces?])
  (boolean! (:exhaustive? route) "UEP-CATALOG-ROUTE-MIGRATION"
            [:routes route-id :exhaustive?])
  (ensure! (and (false? (:replaces? route)) (false? (:exhaustive? route)))
           "UEP-CATALOG-ROUTE-MIGRATION"
           "A compatibility projection cannot claim replacement or exhaustiveness"
           {:route route-id})
  (doseq [lane-id (:lanes route)]
    (let [lane (get lanes lane-id)
          route-mode (:execution-mode route)
          cache-mode (get-in route [:cache-policy :mode])]
      (if (= :fast lane-id)
        (ensure! (and (= :non-authoritative (:authority-ceiling route))
                      (= :warm-bounded-process route-mode)
                      (= :eligible cache-mode)
                      (= :exact-tree-warm-worker
                         (get-in route [:executor :adapter]))
                      (true? (get-in route [:executor :bounded?]))
                      (false? (get-in route [:executor :fresh-capable?]))
                      (= :non-authoritative (get-in route [:executor :authority]))
                      (false? (get-in route [:cache-policy :authoritative?])))
                 "UEP-CATALOG-ROUTE-FAST-POLICY"
                 "Fast route policy is not cacheable and non-authoritative"
                 {:route route-id})
        (ensure! (and (= :acceptance-only (:authority-ceiling route))
                      (= :fresh-isolated-process route-mode)
                      (= :forbidden cache-mode)
                      (= :authoritative-proof-runner
                         (get-in route [:executor :adapter]))
                      (true? (get-in route [:executor :bounded?]))
                      (true? (get-in route [:executor :fresh-capable?]))
                      (= :acceptance-capable (get-in route [:executor :authority]))
                      (false? (get-in route [:cache-policy :authoritative?])))
                 "UEP-CATALOG-ROUTE-FRESH-POLICY"
                 "Fresh route policy is not isolated and cache-free"
                 {:route route-id}))))
  route)

(defn- validate-legacy-mechanism!
  [mechanism declaration routes]
  (ensure! (contains? allowed-legacy-mechanisms mechanism)
           "UEP-CATALOG-LEGACY-MECHANISM"
           "Legacy mechanism is not reviewed"
           {:mechanism mechanism})
  (exact-keys! declaration legacy-mechanism-keys
               "UEP-CATALOG-LEGACY-MECHANISM-KEYS"
               [:legacy-mechanisms mechanism])
  (let [route-ids (:route-ids declaration)
        label-policy (:label-policy declaration)]
    (ensure! (= (case mechanism
                 :iteration-cache-runner :explicit-catalog-selection
                 :parallel-shard-runner :exact-historical-membership)
                (:membership-semantics declaration))
             "UEP-CATALOG-LEGACY-MEMBERSHIP"
             "Legacy mechanism membership semantics do not match its reviewed contract"
             {:mechanism mechanism
              :membership-semantics (:membership-semantics declaration)})
    (non-empty-vector! route-ids "UEP-CATALOG-LEGACY-ROUTES"
                       [:legacy-mechanisms mechanism :route-ids])
    (ensure! (and (every? string? route-ids)
                  (= (count route-ids) (count (distinct route-ids)))
                  (= route-ids (vec (sort route-ids))))
             "UEP-CATALOG-LEGACY-ROUTES"
             "Legacy mechanism routes must be distinct strings in sorted order"
             {:mechanism mechanism :route-ids route-ids})
    (exact-keys! label-policy legacy-label-policy-keys
                 "UEP-CATALOG-LEGACY-LABEL-KEYS"
                 [:legacy-mechanisms mechanism :label-policy])
    (ensure! (contains? allowed-legacy-label-kinds (:kind label-policy))
             "UEP-CATALOG-LEGACY-LABEL"
             "Legacy mechanism label policy is unknown"
             {:mechanism mechanism :kind (:kind label-policy)})
    (let [prefix (:prefix label-policy)]
      (ensure! (and (string? prefix)
                    (not (str/blank? prefix))
                    (<= (count prefix) maximum-repository-relative-path-length)
                    (not (str/includes? prefix "\u0000")))
               "UEP-CATALOG-LEGACY-LABEL"
               "Legacy mechanism label prefix is outside its bound"
               {:mechanism mechanism :prefix prefix})
      (doseq [route-id route-ids]
        (let [route (get routes route-id)]
          (ensure! (map? route)
                   "UEP-CATALOG-LEGACY-ROUTE"
                   "Legacy mechanism references an unknown route"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (some #{mechanism} (:replacement-for route))
                   "UEP-CATALOG-LEGACY-ROUTE"
                   "Legacy mechanism route does not declare its replacement"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (= :compatibility-projection
                      (:migration-status route))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route is not a compatibility projection"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (and (false? (:replaces? route))
                        (false? (:exhaustive? route)))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route cannot replace an authoritative route"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (= [:fast] (:lanes route))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route must remain fast-only"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (= :warm-bounded-process (:execution-mode route))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route must remain warm and bounded"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (= :exact-tree-warm-worker
                      (get-in route [:executor :adapter]))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route executor is not the reviewed worker"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (false? (get-in route [:executor :fresh-capable?]))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route cannot be fresh-capable"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (= :non-authoritative
                      (get-in route [:executor :authority]))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route executor cannot confer authority"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (= :non-authoritative (:authority-ceiling route))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route authority ceiling is too high"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (and (= :eligible (get-in route [:cache-policy :mode]))
                        (false? (get-in route [:cache-policy :authoritative?])))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route cache policy is not fast-only"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (= :exact-vars (get-in route [:selection :kind]))
                   "UEP-CATALOG-LEGACY-POLICY"
                   "Legacy mechanism route must select exact test vars"
                   {:mechanism mechanism :route-id route-id})
          (ensure! (and (str/starts-with? route-id prefix)
                        (pos? (- (count route-id) (count prefix))))
                   "UEP-CATALOG-LEGACY-LABEL"
                   "Legacy mechanism route cannot produce a non-empty label"
                   {:mechanism mechanism :route-id route-id
                    :prefix prefix}))))))

(defn- validate-suite!
  [suite index route-ids]
  (exact-keys! suite suite-keys "UEP-CATALOG-SUITE-KEYS" [:suites index])
  (ensure! (string? (:id suite)) "UEP-CATALOG-SUITE-ID"
           "Suite id must be a string" {:index index})
  (ensure! (keyword? (:owner suite)) "UEP-CATALOG-SUITE-OWNER"
           "Suite owner must be a keyword" {:suite (:id suite)})
  (non-empty-vector! (:route-ids suite) "UEP-CATALOG-SUITE-ROUTES"
                     [:suites index :route-ids])
  (ensure! (= (count (:route-ids suite))
              (count (distinct (:route-ids suite))))
           "UEP-CATALOG-SUITE-ROUTES"
           "Suite route ids must be unique"
           {:suite (:id suite)})
  (ensure! (every? #(contains? route-ids %) (:route-ids suite))
           "UEP-CATALOG-SUITE-ROUTES" "Suite references an unknown route"
           {:suite (:id suite) :route-ids (:route-ids suite)})
  suite)

(defn- validate-conformance-closure!
  [route-id route-module-id closure conformance]
  (when-let [suite-ids (:fixture-suite-ids closure)]
    (ensure! (map? conformance) "UEP-CATALOG-CLOSURE-FIXTURES"
             "Fixture suite references require the conformance table" {:route route-id})
    (let [suites (:suites conformance)
          known (set (map :id suites))
          unknown (set/difference (set suite-ids) known)]
      (when (seq unknown)
        (fail! "UEP-CATALOG-CLOSURE-FIXTURES"
               "Dependency closure references an unknown fixture suite"
               {:route route-id :unknown (sorted-values unknown)}))
      (doseq [suite (filter #(contains? (set suite-ids) (:id %)) suites)]
        (ensure! (= route-module-id (:module-id suite))
                 "UEP-CATALOG-CLOSURE-FIXTURE-OWNER"
                 "Fixture suite module ownership differs from its route"
                 {:route route-id :route-module-id route-module-id
                  :suite (:id suite) :suite-module-id (:module-id suite)}))
      (doseq [suite (filter #(contains? (set suite-ids) (:id %)) suites)
              row (concat (:accepted suite) (:rejected suite))]
        (doseq [key [:gravity :qst]]
          (let [name (get row key)]
            (ensure! (and (string? name)
                          (= name (.. (java.io.File. name) getName))
                          (not (str/includes? name ".."))
                          (not (str/includes? name "/"))
                          (not (str/includes? name "\\")))
                     "UEP-CATALOG-CLOSURE-FIXTURES"
                     "Fixture row contains an unsafe path component"
                     {:route route-id :suite (:id suite) :key key :value name})))))))

(defn validate-catalog
  "Validate and return a closed verification catalog.

  Unknown keys, duplicate ownership paths, incomplete closures, policy
  contradictions, and compatibility routes that claim replacement all fail
  with stable `UEP-*` diagnostic ids."
  [value]
  (exact-keys! value catalog-keys "UEP-CATALOG-KEYS" [])
  (ensure! (= catalog-schema (:schema value)) "UEP-CATALOG-SCHEMA"
           "Verification catalog schema is not recognized" {:schema (:schema value)})
  (ensure! (= 1 (:version value)) "UEP-CATALOG-VERSION"
           "Verification catalog version is not recognized" {:version (:version value)})
  (ensure! (= :non-authoritative (:authority value)) "UEP-CATALOG-AUTHORITY"
           "Catalog itself cannot confer authority" {:authority (:authority value)})
  (boolean! (:authoritative? value) "UEP-CATALOG-AUTHORITY" [:authoritative?])
  (ensure! (false? (:authoritative? value)) "UEP-CATALOG-AUTHORITY"
           "Catalog itself cannot be authoritative" {})
  (ensure! (= :compatibility (:phase value)) "UEP-CATALOG-PHASE"
           "Catalog must identify its compatibility phase" {:phase (:phase value)})
  (boolean! (:complete? value) "UEP-CATALOG-COMPLETENESS" [:complete?])
  (ensure! (false? (:complete? value)) "UEP-CATALOG-COMPLETENESS"
           "The initial catalog cannot claim exhaustive coverage" {})
  (ensure! (string? (:description value)) "UEP-CATALOG-DESCRIPTION"
           "Catalog description must be a string" {})
  (exact-keys! (:lanes value) allowed-lanes "UEP-CATALOG-LANES" [:lanes])
  (doseq [[id lane] (:lanes value)] (validate-lane! id lane))
  (ensure! (map? (:resource-classes value)) "UEP-CATALOG-RESOURCES"
           "Resource classes must be a map" {})
  (ensure! (contains? (:resource-classes value) :exclusive)
           "UEP-CATALOG-RESOURCES" "Exclusive resource capacity must be declared" {})
  (doseq [[id resource] (:resource-classes value)]
    (validate-resource! id resource))
  (ensure! (map? (:modules value)) "UEP-CATALOG-MODULES"
           "Modules must be a map" {})
  (let [module-paths (mapcat (fn [[id module]]
                               (map (fn [path] [path id]) (:source-paths module)))
                             (:modules value))
        duplicate-paths (->> module-paths
                             (group-by first)
                             (filter #(> (count (val %)) 1))
                             (map key)
                             vec)]
    (when (seq duplicate-paths)
      (fail! "UEP-CATALOG-MODULE-OWNERSHIP"
             "A source path has more than one module owner"
             {:paths (sorted-values duplicate-paths)})))
  (doseq [[id module] (:modules value)] (validate-module! id module))
  (ensure! (map? (:routes value)) "UEP-CATALOG-ROUTES"
           "Routes must be a map" {})
  (doseq [[id route] (:routes value)]
    (ensure! (string? id) "UEP-CATALOG-ROUTE-ID"
             "Route keys must be strings" {:route id})
    (validate-route! id route (:modules value) (:resource-classes value)
                     (:lanes value))
    (validate-conformance-closure! id (:module-id route) (:dependency-closure route)
                                   (:conformance value)))
  (exact-keys! (:legacy-mechanisms value) allowed-legacy-mechanisms
               "UEP-CATALOG-LEGACY-MECHANISMS"
               [:legacy-mechanisms])
  (doseq [[mechanism declaration] (:legacy-mechanisms value)]
    (validate-legacy-mechanism! mechanism declaration (:routes value)))
  (let [request-shards
        (->> (:routes value)
             (keep (fn [[_ route]]
                     (get-in route [:executor :request :shard])))
             vec)]
    (ensure! (= (count request-shards) (count (distinct request-shards)))
             "UEP-CATALOG-CACHED-EXECUTOR-REQUEST"
             "Cached executor request shards must be unique"
             {:shards (vec request-shards)}))
  (let [route-ids (set (keys (:routes value)))]
    (non-empty-vector! (:suites value) "UEP-CATALOG-SUITES" [:suites])
    (ensure! (= (count (:suites value))
                (count (distinct (map :id (:suites value)))))
             "UEP-CATALOG-SUITES"
             "Suite ids must be unique" {})
    (doseq [[index suite] (map-indexed vector (:suites value))]
      (validate-suite! suite index route-ids))
    (let [suite-route-ids (vec (mapcat :route-ids (:suites value)))]
      (ensure! (= (count suite-route-ids)
                  (count (distinct suite-route-ids)))
               "UEP-CATALOG-SUITES"
               "A route may be assigned to only one generated suite"
               {:duplicates (->> suite-route-ids frequencies
                                 (keep (fn [[route-id count]]
                                         (when (> count 1) route-id)))
                                 sorted-values)}))
    (exact-keys! (:coverage value) coverage-keys "UEP-CATALOG-COVERAGE-KEYS"
                 [:coverage])
    (ensure! (= :compatibility-phase (:completeness (:coverage value)))
             "UEP-CATALOG-COVERAGE" "Coverage must remain compatibility-phase"
             {})
    (boolean! (:exhaustive? (:coverage value)) "UEP-CATALOG-COVERAGE"
              [:coverage :exhaustive?])
    (ensure! (false? (:exhaustive? (:coverage value))) "UEP-CATALOG-COVERAGE"
             "Coverage cannot claim exhaustiveness before migration" {})
    (ensure! (= :none (:replacement-authority (:coverage value)))
             "UEP-CATALOG-COVERAGE" "Coverage cannot claim replacement authority"
             {})
    (non-empty-vector! (:unmigrated-responsibilities (:coverage value))
                       "UEP-CATALOG-COVERAGE" [:coverage :unmigrated-responsibilities])
    (ensure! (= :routes (:source-of-truth (:coverage value)))
             "UEP-CATALOG-COVERAGE" "Coverage source must be route data" {})
    (ensure! (vector? (:route-ids (:coverage value)))
             "UEP-CATALOG-COVERAGE"
             "Coverage route accounting must be an ordered vector" {})
    (ensure! (= (vec (sort route-ids)) (:route-ids (:coverage value)))
             "UEP-CATALOG-COVERAGE"
             "Coverage route accounting must use deterministic route order"
             {:expected (vec (sort route-ids))
              :actual (:route-ids (:coverage value))})
    (ensure! (= route-ids (set (:route-ids (:coverage value))))
             "UEP-CATALOG-COVERAGE" "Coverage route accounting is not exact"
             {:expected route-ids :actual (:route-ids (:coverage value))})
    (ensure! (= (count (:route-ids (:coverage value)))
                (count route-ids))
             "UEP-CATALOG-COVERAGE"
             "Coverage route accounting contains duplicate route ids"
             {:route-ids (:route-ids (:coverage value))})
    (exact-keys! (:accounting (:coverage value)) coverage-accounting-keys
                 "UEP-CATALOG-COVERAGE-ACCOUNTING" [:coverage :accounting])
    (doseq [key coverage-accounting-keys]
      (non-negative-integer! (get-in value [:coverage :accounting key])
                             "UEP-CATALOG-COVERAGE-ACCOUNTING"
                             [:coverage :accounting key]))
    (exact-keys! (:evidence-policy value) evidence-policy-keys
                 "UEP-CATALOG-EVIDENCE-KEYS" [:evidence-policy])
    (non-empty-vector! (:required (:evidence-policy value))
                       "UEP-CATALOG-EVIDENCE" [:evidence-policy :required])
    (ensure! (set/subset? (set (:authority-ceilings (:evidence-policy value)))
                          allowed-authority-ceilings)
             "UEP-CATALOG-EVIDENCE" "Evidence policy contains an unknown authority"
             {})
    (non-empty-vector! (:fresh-acceptance-requires (:evidence-policy value))
                       "UEP-CATALOG-EVIDENCE"
                       [:evidence-policy :fresh-acceptance-requires]))
  (let [conformance (:conformance value)]
    (ensure! (map? conformance) "UEP-CATALOG-CONFORMANCE"
             "Verification catalog must include a reviewed conformance table" {})
    (exact-keys! conformance conformance-keys "UEP-CATALOG-CONFORMANCE-KEYS"
                 [:conformance])
    (ensure! (= :gravity/sh07-conformance-table-v1 (:schema conformance))
             "UEP-CATALOG-CONFORMANCE" "Conformance table schema is unknown" {})
    (ensure! (= :reviewed-fixture-rows (:source-of-truth conformance))
             "UEP-CATALOG-CONFORMANCE" "Conformance table source is not reviewed" {})
    (boolean! (:authoritative? conformance) "UEP-CATALOG-CONFORMANCE"
              [:conformance :authoritative?])
    (ensure! (false? (:authoritative? conformance)) "UEP-CATALOG-CONFORMANCE"
             "Conformance table cannot be authoritative" {})
    (ensure! (= :non-authoritative (:authority conformance))
             "UEP-CATALOG-CONFORMANCE" "Conformance table authority is invalid" {})
    (boolean! (:complete? conformance) "UEP-CATALOG-CONFORMANCE"
              [:conformance :complete?])
    (ensure! (false? (:complete? conformance)) "UEP-CATALOG-CONFORMANCE"
             "Conformance table cannot claim complete migration" {})
    (doseq [suite (:suites conformance)]
      (ensure! (contains? (:modules value) (:module-id suite))
               "UEP-CATALOG-CONFORMANCE-MODULE"
               "Conformance suite references an unowned manifest module"
               {:suite-id (:id suite) :module-id (:module-id suite)}))
    (conformance/validate-table conformance)
    (non-empty-vector! (:suites conformance) "UEP-CATALOG-CONFORMANCE"
                       [:conformance :suites])
    (non-empty-vector! (:suite-order conformance) "UEP-CATALOG-CONFORMANCE"
                       [:conformance :suite-order])
    (non-empty-vector! (:preserved-bespoke conformance)
                       "UEP-CATALOG-CONFORMANCE"
                       [:conformance :preserved-bespoke]))
  value)

(defn- repository-root
  []
  (loop [path (.toPath (io/file (System/getProperty "user.dir")))]
    (let [candidate (.resolve path catalog-path)]
      (if (Files/isRegularFile candidate (into-array LinkOption []))
        path
        (when-let [parent (.getParent path)]
          (recur parent))))))

(defn read-catalog
  "Read a catalog from a bounded UTF-8 EDN file without evaluating forms."
  ([] (read-catalog catalog-path))
  ([path]
   (let [file (io/file (str path))
         file (if (.isAbsolute file)
                file
                (io/file (or (some-> (repository-root) str) ".") (str path)))]
     (ensure! (and (Files/isRegularFile (.toPath file)
                                        (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (not (Files/isSymbolicLink (.toPath file))))
              "UEP-CATALOG-READ"
              "Verification catalog file is missing" {:path (str path)})
     (let [maximum (* 1024 1024)
           buffer (ByteBuffer/allocate (inc maximum))
           _ (with-open [channel
                         (FileChannel/open
                          (.toPath file)
                          (into-array java.nio.file.OpenOption
                                      [StandardOpenOption/READ
                                       LinkOption/NOFOLLOW_LINKS]))]
               (loop []
                 (when (.hasRemaining buffer)
                   (let [read-count (.read channel buffer)]
                     (cond
                       (= -1 read-count) nil
                       (pos? read-count) (recur)
                       :else (fail! "UEP-CATALOG-READ"
                                    "Verification catalog read made no progress"
                                    {:path (str path)}))))))
           byte-count (.position buffer)
           _ (ensure! (<= byte-count maximum) "UEP-CATALOG-READ"
                      "Verification catalog exceeds the bounded input size"
                      {:path (str path) :bytes byte-count :maximum maximum})
           bytes (java.util.Arrays/copyOf (.array buffer) byte-count)]
       (try
         (with-open [reader (java.io.PushbackReader.
                             (java.io.StringReader. (String. bytes StandardCharsets/UTF_8)))]
           (let [eof (Object.) value (edn/read {:eof eof} reader)
                 trailing (edn/read {:eof eof} reader)]
             (ensure! (and (not (identical? eof value)) (identical? eof trailing))
                      "UEP-CATALOG-PARSE" "Verification catalog must contain exactly one EDN form"
                      {:path (str path)})
             value))
         (catch Throwable error
           (fail! "UEP-CATALOG-PARSE" "Verification catalog EDN is malformed"
                  {:path (str path) :class (.getName (class error))})))))))

(def catalog (validate-catalog (read-catalog)))

(defn route-names
  "Return catalog route ids in deterministic order."
  ([] (route-names catalog))
  ([catalog-value]
   (validate-catalog catalog-value)
   (vec (sort (keys (:routes catalog-value))))))

(defn route
  "Return a closed catalog route or fail with a stable diagnostic."
  ([route-id] (route catalog route-id))
  ([catalog-value route-id]
   (validate-catalog catalog-value)
   (ensure! (string? route-id) "UEP-ROUTE-ID" "Route id must be a string"
            {:route route-id})
   (or (get-in catalog-value [:routes route-id])
       (fail! "UEP-ROUTE-UNKNOWN" "Unknown verification route"
              {:route route-id :available (route-names catalog-value)}))))

(defn- canonicalize
  [value]
  (cond
    (map? value)
    (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
          (map (fn [[key item]] [key (canonicalize item)]))
          value)

    (set? value) (vec (sort-by pr-str (map canonicalize value)))
    (vector? value) (mapv canonicalize value)
    (seq? value) (mapv canonicalize value)
    :else value))

(defn- identity-of
  [value]
  (str "sha256:" (digest/sha256-hex (pr-str (canonicalize value)))))

(defn manifest-id
  "Return the content identity of a validated verification manifest.

  This is deliberately derived from the manifest value rather than from a
  hand-maintained version string.  Callers that receive a manifest from an
  untrusted source must still invoke `validate-catalog` (or
  `validate-manifest-projections!`) before using the identity for routing or
  cache keys."
  ([ ] (manifest-id catalog))
  ([manifest]
   (validate-catalog manifest)
   (identity-of manifest)))

(defn- route-catalog-value [manifest]
  (into (sorted-map) (:routes manifest)))

(defn- legacy-route-catalog-value [manifest]
  ;; This projection is the broad migration-responsibility inventory. It must
  ;; include every route that declares a legacy replacement (including routes
  ;; intentionally absent from the exact historical compatibility projection).
  ;; Keep the latter in `legacy-mechanism-projection` so the two memberships do
  ;; not silently collapse into one route catalog.
  (->> (:routes manifest)
       (mapcat (fn [[route-id route]]
                 (map (fn [legacy] [legacy route-id])
                      (:replacement-for route))))
       (group-by first)
       (map (fn [[legacy pairs]]
              [legacy (vec (sort (map second pairs)))]))
       (into (sorted-map))))

(defn- legacy-route-label-value
  [declaration route-id]
  (let [prefix (get-in declaration [:label-policy :prefix])]
    (subs route-id (count prefix))))

(defn- module-ownership-index-value [manifest]
  (into (sorted-map)
        (mapcat
         (fn [[module-id module]]
           (map (fn [path]
                  [path {:module-id module-id :owner (:owner module)}])
                (:source-paths module)))
         (:modules manifest))))

(defn- route-dependency-closures-value [manifest]
  (into (sorted-map)
        (map (fn [[route-id route]]
               [route-id (:dependency-closure route)]))
        (:routes manifest)))

(defn- coverage-accounting-value [manifest]
  (let [routes (:routes manifest)
        modules (:modules manifest)
        suites (:suites manifest)
        conformance-suites (vec (get-in manifest [:conformance :suites]))
        accepted-count (reduce + 0 (map #(count (:accepted %)) conformance-suites))
        rejected-count (reduce + 0 (map #(count (:rejected %)) conformance-suites))]
    {:source-of-truth :verification-catalog
     :route-ids (vec (sort (keys routes)))
     :route-count (count routes)
     :module-ids (vec (sort (keys modules)))
     :module-count (count modules)
     :suite-ids (vec (sort (map :id suites)))
     :suite-count (count suites)
     :selector-count
     (reduce + 0 (map #(count (get-in % [:selection :selectors])) (vals routes)))
     :exact-var-selector-count
     (reduce + 0 (map #(if (= :exact-vars (get-in % [:selection :kind]))
                         (count (get-in % [:selection :selectors])) 0)
                       (vals routes)))
     :proof-contract-selector-count
     (reduce + 0 (map #(if (= :proof-contract-module-catalog
                               (get-in % [:selection :kind]))
                         (count (get-in % [:selection :selectors])) 0)
                       (vals routes)))
     :fixture-suite-count (count conformance-suites)
     :accepted-fixture-count accepted-count
     :rejected-fixture-count rejected-count
     :fixture-pair-count (+ accepted-count rejected-count)}))

(defn route-catalog
  "Generate the closed route projection owned by the verification manifest.

  The result is a deterministic sorted map whose values are the reviewed
  route records.  No route metadata is copied into a second hand-maintained
  catalog; compatibility runners can consume this projection while their
  legacy selectors remain under parity review."
  ([ ] (route-catalog catalog))
  ([manifest]
   (validate-catalog manifest)
   (route-catalog-value manifest)))

(defn legacy-route-catalog
  "Generate a legacy-mechanism-to-route projection from the manifest.

  Legacy names are labels for migration accounting only; they do not become
  executable authorities. Every route's `:replacement-for` declarations are
  included, so this broad projection may contain more routes than the exact
  historical membership returned by `legacy-mechanism-projection`."
  ([ ] (legacy-route-catalog catalog))
  ([manifest]
   (validate-catalog manifest)
   (legacy-route-catalog-value manifest)))

(defn legacy-mechanism-projection
  "Return one reviewed legacy mechanism and its presentation labels.

  Route membership and label policy come from the authenticated catalog; the
  returned labels are a compatibility serialization only and confer no
  execution or acceptance authority."
  ([mechanism] (legacy-mechanism-projection catalog mechanism))
  ([manifest mechanism]
   (validate-catalog manifest)
   (let [declaration (get-in manifest [:legacy-mechanisms mechanism])]
     (ensure! (map? declaration)
              "UEP-LEGACY-MECHANISM"
              "Legacy mechanism is not present in the reviewed catalog"
              {:mechanism mechanism})
     {:mechanism mechanism
      :route-ids (:route-ids declaration)
      :labels (mapv #(legacy-route-label-value declaration %)
                    (:route-ids declaration))
      :membership-semantics (:membership-semantics declaration)
      :label-policy (:label-policy declaration)})))

(defn module-ownership-index
  "Generate a path-to-owner projection from the reviewed module manifest.

  Every source path has exactly one owner because catalog validation rejects
  duplicate ownership.  Values retain both module identity and owner so
  downstream planners do not need to infer ownership from filenames."
  ([ ] (module-ownership-index catalog))
  ([manifest]
   (validate-catalog manifest)
   (module-ownership-index-value manifest)))

(defn route-dependency-closures
  "Generate the route-to-closure projection from the reviewed manifest."
  ([ ] (route-dependency-closures catalog))
  ([manifest]
   (validate-catalog manifest)
   (route-dependency-closures-value manifest)))

(defn coverage-accounting
  "Compute deterministic coverage accounting from manifest-owned records.

  Declared coverage is checked against this projection by
  `validate-manifest-projections!`; callers must not treat these counts as
  SH roadmap or authoritative proof evidence."
  ([ ] (coverage-accounting catalog))
  ([manifest]
   (validate-catalog manifest)
   (coverage-accounting-value manifest)))

(defn validate-manifest-projections!
  "Validate all generated manifest projections and return them.

  This is the admission point for consumers that need ownership, routing,
  dependency closure, or coverage accounting.  It intentionally reports a
  non-authoritative projection: the manifest and its generated views do not
  confer SH roadmap, proof, release, safety, performance, or integration
  authority.  Unknown routes, omitted suite assignments, duplicate ownership,
  and stale declared counts fail closed with stable diagnostics."
  ([ ] (validate-manifest-projections! catalog))
  ([manifest]
   (validate-catalog manifest)
   (let [routes (route-catalog-value manifest)
         legacy-routes (legacy-route-catalog-value manifest)
         ownership (module-ownership-index-value manifest)
         closures (route-dependency-closures-value manifest)
         accounting (coverage-accounting-value manifest)
         route-ids (set (keys routes))
         declared-route-ids (vec (get-in manifest [:coverage :route-ids]))
         declared-accounting (get-in manifest [:coverage :accounting])
         suite-route-ids (vec (mapcat :route-ids (:suites manifest)))
         conformance (:conformance manifest)
         conformance-suites (vec (:suites conformance))
         declared-conformance (get-in conformance [:coverage])]
     (ensure! (= route-ids (set declared-route-ids))
              "UEP-MANIFEST-COVERAGE-ROUTES"
              "Manifest coverage route ids disagree with generated routes"
              {:declared declared-route-ids :generated (:route-ids accounting)})
     (ensure! (= (count declared-route-ids) (count route-ids))
              "UEP-MANIFEST-COVERAGE-ROUTES"
              "Manifest coverage route ids contain duplicates"
              {:declared declared-route-ids})
     (ensure! (= route-ids (set suite-route-ids))
              "UEP-MANIFEST-SUITE-ROUTES"
              "Manifest suites do not account for every generated route"
              {:routes (sorted-values route-ids)
               :suite-routes suite-route-ids})
     (ensure! (= (count suite-route-ids) (count route-ids))
              "UEP-MANIFEST-SUITE-ROUTES"
              "Manifest suite route assignments contain duplicates"
              {:suite-routes suite-route-ids})
     (ensure! (= (:route-count accounting) (count (:routes manifest)))
              "UEP-MANIFEST-COVERAGE-COUNT"
              "Generated route count is inconsistent"
              {:accounting accounting})
     (ensure! (= declared-accounting
                 (select-keys accounting coverage-accounting-keys))
              "UEP-MANIFEST-COVERAGE-ACCOUNTING"
              "Declared coverage accounting is stale"
              {:declared declared-accounting :generated accounting})
     (when (map? declared-conformance)
       (ensure! (= (:suite-count declared-conformance)
                   (:fixture-suite-count accounting))
                "UEP-MANIFEST-CONFORMANCE-COUNT"
                "Declared conformance suite count is stale"
                {:declared declared-conformance :generated accounting})
       (ensure! (= (:accepted-count declared-conformance)
                   (:accepted-fixture-count accounting))
                "UEP-MANIFEST-CONFORMANCE-COUNT"
                "Declared accepted fixture count is stale"
                {:declared declared-conformance :generated accounting})
       (ensure! (= (:rejected-count declared-conformance)
                   (:rejected-fixture-count accounting))
                "UEP-MANIFEST-CONFORMANCE-COUNT"
                "Declared rejected fixture count is stale"
                {:declared declared-conformance :generated accounting})
       (ensure! (= (:pair-count declared-conformance)
                   (:fixture-pair-count accounting))
                "UEP-MANIFEST-CONFORMANCE-COUNT"
                "Declared fixture pair count is stale"
                {:declared declared-conformance :generated accounting}))
     (doseq [[route-id route] routes]
       (let [module-id (:module-id route)
             module (get-in manifest [:modules module-id])
             closure (get closures route-id)]
         (ensure! (= route (get-in manifest [:routes route-id]))
                  "UEP-MANIFEST-ROUTE-PROJECTION"
                  "Generated route projection changed its reviewed route"
                  {:route route-id})
         (ensure! (= (:dependency-closure route) closure)
                  "UEP-MANIFEST-CLOSURE-PROJECTION"
                  "Generated dependency closure projection is stale"
                  {:route route-id})
         (ensure! (every? #(= {:module-id module-id
                               :owner (:owner module)}
                              (get ownership %))
                          (:source-paths module))
                  "UEP-MANIFEST-OWNERSHIP-PROJECTION"
                  "Generated ownership projection is stale"
                  {:route route-id :module-id module-id})))
     {:schema :gravity/verification-manifest-projection-v1
      :authority :non-authoritative
      :authoritative? false
      :manifest-id (identity-of manifest)
      :routes routes
      :legacy-routes legacy-routes
      :module-ownership ownership
      :dependency-closures closures
      :coverage accounting})))

(defn- fast-namespace-route?
  [[_ route]]
  ;; Only routes that the warm, non-authoritative worker may execute contribute
  ;; namespace authority.  In particular, the fresh proof-contract route is
  ;; deliberately excluded even though it also carries a selection namespace.
  (and (= [:fast] (:lanes route))
       (= :exact-vars (get-in route [:selection :kind]))
       (= :warm-bounded-process (:execution-mode route))
       (= :exact-tree-warm-worker (get-in route [:executor :adapter]))
       (true? (get-in route [:executor :bounded?]))
       (false? (get-in route [:executor :fresh-capable?]))
       (= :non-authoritative (get-in route [:executor :authority]))
       (= :non-authoritative (:authority-ceiling route))
       (false? (get-in route [:cache-policy :authoritative?]))))

(defn- validate-namespace-authority-projection*
  "Validate one generated non-authoritative namespace authority projection.

  The projection is a bounded transport view of validated fast exact routes;
  it is not an additional route catalog and cannot confer acceptance authority.
  Callers receiving a projection from an untrusted source must validate it
  before using its entries for namespace admission."
  [projection]
  (exact-keys! projection namespace-authority-keys
               "UEP-NAMESPACE-AUTHORITY-KEYS"
               [:namespace-authority])
  (ensure! (= namespace-authority-schema (:schema projection))
           "UEP-NAMESPACE-AUTHORITY-SCHEMA"
           "Namespace authority projection schema is unknown"
           {:schema (:schema projection)})
  (ensure! (= :non-authoritative (:authority projection))
           "UEP-NAMESPACE-AUTHORITY-AUTHORITY"
           "Namespace authority projection cannot confer authority"
           {:authority (:authority projection)})
  (boolean! (:authoritative? projection)
            "UEP-NAMESPACE-AUTHORITY-AUTHORITY"
            [:namespace-authority :authoritative?])
  (ensure! (false? (:authoritative? projection))
           "UEP-NAMESPACE-AUTHORITY-AUTHORITY"
           "Namespace authority projection cannot be authoritative" {})
  (doseq [[field value] [[:manifest-id (:manifest-id projection)]
                         [:projection-id (:projection-id projection)]]]
    (ensure! (and (string? value)
                  (re-matches #"sha256:[0-9a-f]{64}" value))
             "UEP-NAMESPACE-AUTHORITY-IDENTITY"
             "Namespace authority identity is malformed"
             {:field field :value value}))
  (let [entries (:entries projection)]
    (ensure! (and (vector? entries)
                  (seq entries)
                  (= (count entries)
                     (count (distinct (map :namespace entries))))
                  (= entries (vec (sort-by #(str (:namespace %)) entries))))
             "UEP-NAMESPACE-AUTHORITY-ENTRIES"
             "Namespace authority entries are not a bounded ordered set"
             {:entries entries})
    (doseq [[index entry] (map-indexed vector entries)]
      (exact-keys! entry namespace-authority-entry-keys
                   "UEP-NAMESPACE-AUTHORITY-ENTRY-KEYS"
                   [:namespace-authority :entries index])
      (let [namespace (:namespace entry)
            path (:path entry)
            selectors (:selectors entry)]
        (ensure! (and (symbol? namespace)
                      (not (str/blank? (str namespace)))
                      (<= (count (str namespace)) 1024))
                 "UEP-NAMESPACE-AUTHORITY-NAMESPACE"
                 "Namespace authority namespace is malformed"
                 {:index index :namespace namespace})
        (ensure! (and (string? path)
                      (normalized-repository-relative-path? path)
                      (= (str "bootstrap/clojure/test/"
                              (route-test-relative-path (str namespace)))
                         path))
                 "UEP-NAMESPACE-AUTHORITY-PATH"
                 "Namespace authority path is not canonical"
                 {:index index :namespace namespace :path path})
        (ensure! (and (vector? selectors)
                      (seq selectors)
                      (= (count selectors) (count (distinct selectors)))
                      (every? #(and (string? %)
                                    (not (str/blank? %))
                                    (<= (count %) 1024))
                              selectors))
                 "UEP-NAMESPACE-AUTHORITY-SELECTORS"
                 "Namespace authority selectors are not bounded and distinct"
                 {:index index :namespace namespace})
        (let [route-ids (:route-ids entry)]
          (ensure! (and (vector? route-ids)
                        (seq route-ids)
                        (every? #(and (string? %)
                                      (not (str/blank? %))
                                      (<= (count %) 1024))
                                route-ids))
                   "UEP-NAMESPACE-AUTHORITY-ROUTES"
                   "Namespace authority route ownership is not bounded and ordered"
                   {:index index :namespace namespace :route-ids route-ids})
          (ensure! (= route-ids (vec (sort route-ids)))
                   "UEP-NAMESPACE-AUTHORITY-ROUTES"
                   "Namespace authority route ownership is not ordered"
                   {:index index :namespace namespace :route-ids route-ids})
          (ensure! (= (count route-ids) (count (distinct route-ids)))
                   "UEP-NAMESPACE-AUTHORITY-ROUTES"
                   "Namespace authority route ownership is not distinct"
                   {:index index :namespace namespace :route-ids route-ids})))))
  (let [semantic (dissoc projection :projection-id)]
    (ensure! (= (:projection-id projection) (identity-of semantic))
             "UEP-NAMESPACE-AUTHORITY-ID"
             "Namespace authority projection identity is stale"
             {:expected (identity-of semantic)
              :actual (:projection-id projection)}))
  projection)

(declare namespace-authority-projection-value)

(defn validate-namespace-authority-projection
  "Validate a generated namespace projection, optionally binding its manifest.

  The one-argument form validates the closed projection shape.  Consumers
  should prefer the two-argument form when they hold the manifest so a
  projection cannot be replayed under a different catalog identity."
  ([projection]
   (validate-namespace-authority-projection* projection))
  ([manifest projection]
   (validate-catalog manifest)
   (validate-namespace-authority-projection* projection)
   (let [expected-value (namespace-authority-projection-value manifest)
         expected (assoc expected-value
                         :projection-id (identity-of expected-value))]
     (ensure! (= projection expected)
              "UEP-NAMESPACE-AUTHORITY-MANIFEST"
              "Namespace authority projection is not the current manifest projection"
              {:expected (identity-of expected-value)
               :actual (:projection-id projection)}))
   projection))

(defn- namespace-authority-projection-value
  [manifest]
  (let [groups (->> (:routes manifest)
                    (filter fast-namespace-route?)
                    (group-by (comp :namespace :selection second)))
        entries
        (->> groups
             (sort-by key)
             (mapv (fn [[namespace routes]]
                     (let [selectors
                           (->> routes
                                (sort-by first)
                                (mapcat #(get-in (val %) [:selection :selectors]))
                                distinct
                                vec)
                           route-ids (->> routes (map first) sort vec)]
                       {:namespace (symbol namespace)
                        :path (str "bootstrap/clojure/test/"
                                   (route-test-relative-path namespace))
                        :selectors selectors
                        :route-ids route-ids}))))]
    {:schema namespace-authority-schema
     :authority :non-authoritative
     :authoritative? false
     :manifest-id (identity-of manifest)
     :entries entries}))

(defn namespace-authority-projection
  "Generate the bounded namespace authority view from the reviewed manifest.

  This projection intentionally contains only fast exact-variable routes for
  the exact-tree warm worker.  Fresh/proof routes remain outside this
  development authority, and no route is created by this view."
  ([] (namespace-authority-projection catalog))
  ([manifest]
   (validate-catalog manifest)
   (let [projection (namespace-authority-projection-value manifest)]
     (validate-namespace-authority-projection
      (assoc projection :projection-id (identity-of projection))))))

(defn- request-keys!
  [request]
  (ensure! (map? request) "UEP-REQUEST-SHAPE"
           "Execution request must be a map" {})
  (let [allowed #{:route-id :lane :selection :dependency-closure
                  :resource-policy :cache-policy :execution-mode :receipt}
        unknown (set/difference (set (keys request)) allowed)]
    (when (seq unknown)
      (fail! "UEP-REQUEST-KEYS" "Execution request has unknown keys"
             {:unknown (sorted-values unknown)})))
  request)

(defn- validate-request-overrides!
  [request route lane-id]
  (when (contains? request :selection)
    (ensure! (= (:selection request) (:selection route))
             "UEP-REQUEST-SELECTION" "Selection override is not the reviewed route"
             {:route (:route-id request)}))
  (when (contains? request :dependency-closure)
    (ensure! (= (:dependency-closure request) (:dependency-closure route))
             "UEP-REQUEST-CLOSURE" "Dependency closure override is not exact"
             {:route (:route-id request)}))
  (when (contains? request :execution-mode)
    (ensure! (= (:execution-mode request) (:execution-mode route))
             "UEP-REQUEST-MODE" "Execution mode override is not the reviewed mode"
             {:route (:route-id request)}))
  (when (contains? request :resource-policy)
    (let [policy (:resource-policy request)]
      (exact-keys! policy #{:resource-class :timeout-ms :max-output-bytes}
                   "UEP-REQUEST-RESOURCE-KEYS" [:request :resource-policy])
      (ensure! (= (:resource-class route) (:resource-class policy))
               "UEP-REQUEST-RESOURCE" "Resource-class override is not reviewed"
               {:route (:route-id request)})
      (doseq [key [:timeout-ms :max-output-bytes]]
        (positive-integer! (get policy key) "UEP-REQUEST-RESOURCE"
                           [:request :resource-policy key])
        (ensure! (<= (get policy key) (get-in route [:bounds key]))
                 "UEP-REQUEST-RESOURCE" "Resource override exceeds route bound"
                 {:route (:route-id request) :key key}))))
  (when (contains? request :cache-policy)
    (let [policy (:cache-policy request)
          mode (:mode policy)]
      (exact-keys! policy #{:mode} "UEP-REQUEST-CACHE-KEYS"
                   [:request :cache-policy])
      (ensure! (contains? allowed-cache-modes mode) "UEP-REQUEST-CACHE"
               "Cache override is unknown" {:mode mode})
      (if (= :fast lane-id)
        (ensure! (contains? #{:eligible :disabled} mode) "UEP-REQUEST-CACHE"
                 "Fast lane can only retain or disable its cache"
                 {:mode mode})
        (ensure! (= :forbidden mode) "UEP-REQUEST-CACHE"
                 "Fresh acceptance cannot enable a cache" {:mode mode}))))
  (when (contains? request :receipt)
    (let [receipt (:receipt request)]
      (exact-keys! receipt #{:required?} "UEP-REQUEST-RECEIPT-KEYS"
                   [:request :receipt])
      (ensure! (true? (:required? receipt)) "UEP-REQUEST-RECEIPT"
               "Receipt generation cannot be disabled" {})))
  request)

(declare fixture-source-binding)

(defn- validate-artifact-envelope!
  "Bound and close the artifact extension before hashing the enclosing plan."
  [invocation]
  (exact-keys! invocation artifact-invocation-keys
               "UEP-ARTIFACT-INVOCATION"
               [:plan :artifact-invocation])
  (ensure! (= artifact-invocation-schema (:schema invocation))
           "UEP-ARTIFACT-INVOCATION" "Artifact invocation schema is unknown" {})
  (ensure! (= 1 (:version invocation))
           "UEP-ARTIFACT-INVOCATION" "Artifact invocation version is unknown" {})
  (ensure! (= artifact-operation (:operation invocation))
           "UEP-ARTIFACT-INVOCATION" "Artifact operation is unknown" {})
  (doseq [key [:base-plan-id :binding-id]]
    (ensure! (and (string? (get invocation key))
                  (re-matches #"sha256:[0-9a-f]{64}" (get invocation key)))
             "UEP-ARTIFACT-INVOCATION" "Artifact invocation identity is invalid"
             {:key key}))
  (ensure! (and (string? (:row-id invocation))
                (not (str/blank? (:row-id invocation)))
                (<= (count (:row-id invocation)) artifact-row-id-max-length))
           "UEP-ARTIFACT-ROW" "Artifact fixture row id is invalid or oversized" {})
  (ensure! (and (keyword? (:stage invocation))
                (contains? artifact-stages (:stage invocation)))
           "UEP-ARTIFACT-STAGE" "Artifact stage is not recognized" {})
  (let [binding (:binding invocation)]
    (exact-keys! binding fixture-source-binding-keys
                 "UEP-ARTIFACT-BINDING" [:plan :artifact-invocation :binding])
    (ensure! (= fixture-source-binding-schema (:schema binding))
             "UEP-ARTIFACT-BINDING" "Artifact binding schema is unknown" {})
    (ensure! (= (:base-plan-id invocation) (:plan-id binding))
             "UEP-ARTIFACT-BINDING" "Artifact binding plan identity is stale" {})
    (ensure! (= (:binding-id invocation) (:binding-id binding))
             "UEP-ARTIFACT-BINDING" "Artifact binding identity is stale" {})
    (ensure! (= (:row-id invocation) (:row-id binding))
             "UEP-ARTIFACT-BINDING" "Artifact binding row identity is stale" {})
    (ensure! (= (:stage invocation) (:stage binding))
             "UEP-ARTIFACT-BINDING" "Artifact binding stage is stale" {})
    (doseq [key [:plan-id :catalog-id :binding-id :fixture-sha256]]
      (ensure! (and (string? (get binding key))
                    (re-matches #"sha256:[0-9a-f]{64}" (get binding key)))
               "UEP-ARTIFACT-BINDING" "Artifact binding identity is invalid"
               {:key key}))
    (doseq [key [:route-id :suite-id :row-id :module-id]]
      (let [value (get binding key)]
        (ensure! (and (string? value) (not (str/blank? value))
                      (<= (count value) artifact-row-id-max-length))
                  "UEP-ARTIFACT-BINDING" "Artifact binding text is invalid"
                  {:key key})))
    (ensure! (and (keyword? (:module-owner binding))
                  (<= (count (str (:module-owner binding))) artifact-row-id-max-length))
             "UEP-ARTIFACT-BINDING" "Artifact binding owner is invalid" {})
    (ensure! (contains? #{:accepted :rejected} (:status binding))
             "UEP-ARTIFACT-BINDING" "Artifact binding status is invalid" {})
    (doseq [key [:gravity-path :qst-path]]
      (ensure! (normalized-repository-relative-path? (get binding key))
               "UEP-ARTIFACT-BINDING-PATH" "Artifact binding path is unsafe"
               {:key key}))
    (ensure! (and (integer? (:fixture-bytes binding))
                  (pos? (:fixture-bytes binding))
                  (<= (:fixture-bytes binding) conformance/maximum-fixture-peer-bytes))
             "UEP-ARTIFACT-BINDING" "Artifact fixture byte count is invalid" {})
    (ensure! (true? (:authenticated? binding))
             "UEP-ARTIFACT-BINDING" "Artifact binding is not authenticated" {}))
  invocation)

(defn validate-plan
  "Validate a generated plan, including its content identity."
  [plan-value]
  (ensure! (map? plan-value) "UEP-PLAN-SHAPE" "Execution plan must be a map" {})
  (let [artifact? (contains? plan-value :artifact-invocation)
        expected-keys (if artifact? artifact-plan-keys plan-keys)]
    (exact-keys! plan-value expected-keys "UEP-PLAN-KEYS" [:plan]))
  (when (contains? plan-value :artifact-invocation)
    (validate-artifact-envelope! (:artifact-invocation plan-value)))
  (let [without-id (dissoc plan-value :plan-id)
        expected-id (identity-of without-id)]
    (ensure! (= plan-schema (:schema plan-value)) "UEP-PLAN-SCHEMA"
             "Execution plan schema is not recognized" {:schema (:schema plan-value)})
    (ensure! (= expected-id (:plan-id plan-value)) "UEP-PLAN-ID"
             "Execution plan identity does not match its content"
             {:expected expected-id :actual (:plan-id plan-value)}))
  (ensure! (= (identity-of catalog) (:catalog-id plan-value))
           "UEP-PLAN-CATALOG" "Execution plan catalog identity is stale" {})
  (ensure! (= 1 (:version plan-value)) "UEP-PLAN-VERSION"
           "Execution plan version is not recognized" {})
  (let [route-value (route (:route-id plan-value))
        lane-id (:lane plan-value)
        lane-value (get-in catalog [:lanes lane-id])
        module-id (:module-id route-value)
        module (get-in catalog [:modules module-id])
        expected-artifact-request (module-artifact-request module-id module)
        artifact-invocation (:artifact-invocation plan-value)
        artifact? (contains? plan-value :artifact-invocation)
        expected-evidence (if artifact?
                           (:required-evidence module)
                           (:required-evidence route-value))]
    (ensure! (some #{lane-id} (:lanes route-value)) "UEP-PLAN-LANE"
             "Execution plan route/lane admission is stale" {})
    (ensure! (= (:selection route-value) (:selection plan-value))
             "UEP-PLAN-SELECTION" "Execution plan selection was altered" {})
    (ensure! (= (:dependency-closure route-value)
                (:dependency-closure plan-value))
             "UEP-PLAN-CLOSURE" "Execution plan dependency closure was altered" {})
    (ensure! (= (identity-of (:dependency-closure route-value))
                (:closure-identity plan-value))
             "UEP-PLAN-CLOSURE" "Execution plan closure identity is stale" {})
    (validate-artifact-request! (:artifact-request plan-value)
                                (:modules catalog)
                                [:plan :artifact-request])
    (ensure! (= expected-artifact-request (:artifact-request plan-value))
             "UEP-PLAN-ARTIFACT"
             "Execution plan artifact request was altered or is stale"
             {:expected expected-artifact-request
              :actual (:artifact-request plan-value)})
    (ensure! (= (:execution-mode route-value) (:execution-mode plan-value))
             "UEP-PLAN-MODE" "Execution plan execution mode was altered" {})
    (ensure! (= (:executor route-value) (:executor plan-value))
             "UEP-PLAN-EXECUTOR" "Execution plan executor was altered" {})
    (ensure! (= expected-evidence (:required-evidence plan-value))
             "UEP-PLAN-EVIDENCE" "Execution plan required evidence was altered" {})
    (ensure! (= (:nonclaims route-value) (:nonclaims plan-value))
             "UEP-PLAN-NONCLAIMS" "Execution plan nonclaims were altered" {})
    (ensure! (= {:schema receipt-schema
                 :required? true
                 :deterministic? true
                 :write-once? (= :fresh lane-id)
                 :authority-ceiling (:authority-ceiling route-value)}
                (:receipt-generation plan-value))
             "UEP-PLAN-RECEIPT" "Execution plan receipt policy was altered" {})
    (ensure! (= {:source-authentication :pre-and-post-execution
                 :dependency-closure :complete-and-rechecked
                 :mutation-detection :before-and-after
                 :provenance :semantic-and-actual-path
                 :fresh-cache-observation (= :fresh lane-id)}
                (:guards plan-value))
             "UEP-PLAN-GUARDS" "Execution plan integrity guards were altered" {})
    (ensure! (= (= :fresh lane-id)
                (:fresh-authoritative-gate-required? plan-value))
             "UEP-PLAN-AUTHORITY" "Fresh authority gate was altered" {})
    (ensure! (= (if (= :fresh lane-id) :acceptance-capable :non-authoritative)
                (:authority plan-value))
             "UEP-PLAN-AUTHORITY" "Execution plan authority was widened" {})
    (ensure! (and (map? lane-value) (= lane-id (:id lane-value)))
             "UEP-PLAN-LANE" "Execution plan lane is not catalog-derived" {})
    (ensure! (= (:authority-ceiling route-value) (:authority-ceiling plan-value))
             "UEP-PLAN-AUTHORITY" "Execution plan authority ceiling was altered" {})
    (ensure! (false? (:authoritative? plan-value)) "UEP-PLAN-AUTHORITY"
             "Execution plan cannot be authoritative by itself" {})
    (when artifact?
      (ensure! (and (map? artifact-invocation)
                    (= artifact-invocation-keys (set (keys artifact-invocation)))
                    (= artifact-invocation-schema
                       (:schema artifact-invocation))
                    (= 1 (:version artifact-invocation))
                    (= artifact-operation (:operation artifact-invocation)))
               "UEP-ARTIFACT-INVOCATION"
               "Artifact invocation is not a closed operation extension" {})
      (let [base-plan (assoc (dissoc plan-value :artifact-invocation :plan-id)
                             :required-evidence (:required-evidence route-value)
                             :plan-id (:base-plan-id artifact-invocation))]
        (ensure! (= (:plan-id base-plan)
                    (:base-plan-id artifact-invocation))
                 "UEP-ARTIFACT-BASE-PLAN"
                 "Artifact invocation base-plan identity is stale" {})
        ;; Revalidate the ordinary plan projection before accepting the
        ;; operation extension.  This keeps lane, closure, resource, cache,
        ;; and executor policy owned by the one ordinary plan contract.
        (validate-plan base-plan)
        (ensure! (and (string? (:row-id artifact-invocation))
                      (not (str/blank? (:row-id artifact-invocation)))
                      (<= (count (:row-id artifact-invocation))
                          artifact-row-id-max-length))
                 "UEP-ARTIFACT-ROW"
                 "Artifact fixture row id is invalid or oversized" {})
        (ensure! (and (keyword? (:stage artifact-invocation))
                      (contains? artifact-stages (:stage artifact-invocation))
                      (<= (get artifact-stage-rank (:stage artifact-invocation))
                          (get artifact-stage-rank
                               (get-in base-plan [:artifact-request :stage]))))
                 "UEP-ARTIFACT-STAGE"
                 "Artifact stage is outside the module request" {})
        (let [binding (fixture-source-binding base-plan
                                               (:row-id artifact-invocation)
                                               (:stage artifact-invocation))]
          (ensure! (= binding (:binding artifact-invocation))
                   "UEP-ARTIFACT-BINDING"
                   "Artifact fixture binding is stale or altered" {})
          (ensure! (= (:binding-id binding)
                      (:binding-id artifact-invocation))
                   "UEP-ARTIFACT-BINDING"
                   "Artifact fixture binding identity is stale" {}))))
    (exact-keys! (:resource-policy plan-value)
                 #{:resource-class :timeout-ms :max-output-bytes}
                 "UEP-PLAN-RESOURCE" [:plan :resource-policy])
    (ensure! (= (:resource-class route-value)
                (:resource-class (:resource-policy plan-value)))
             "UEP-PLAN-RESOURCE" "Execution plan resource class was altered" {})
    (doseq [key [:timeout-ms :max-output-bytes]]
      (positive-integer! (get-in plan-value [:resource-policy key])
                         "UEP-PLAN-RESOURCE" [:plan :resource-policy key])
      (ensure! (<= (get-in plan-value [:resource-policy key])
                   (get-in route-value [:bounds key]))
               "UEP-PLAN-RESOURCE" "Execution plan resource bound was widened"
               {:key key}))
    (ensure! (= (dissoc (:cache-policy route-value) :mode)
                (dissoc (:cache-policy plan-value) :mode))
             "UEP-PLAN-CACHE" "Execution plan cache policy was altered" {})
    (ensure! (contains? allowed-cache-modes
                        (get-in plan-value [:cache-policy :mode]))
             "UEP-PLAN-CACHE" "Execution plan cache mode is unknown" {})
    (if (= :fresh lane-id)
      (ensure! (= :forbidden (get-in plan-value [:cache-policy :mode]))
               "UEP-PLAN-CACHE" "Fresh execution plan cannot observe a cache" {})
      (ensure! (contains? #{:eligible :disabled}
                          (get-in plan-value [:cache-policy :mode]))
               "UEP-PLAN-CACHE" "Fast execution plan cache mode is invalid" {})))
  plan-value)

(def ^:dynamic *resource-lease*
  "Explicit resource context required by the current-JVM fast adapter.

  The unified coordinator will bind this after acquiring the reviewed broker
  lease. An absent lease is a blocked result, never an implicit admission."
  nil)

(def ^:private max-snapshot-file-bytes (* 16 1024 1024))
(def ^:private max-snapshot-entries 8192)
(def ^:private max-snapshot-total-bytes (* 64 1024 1024))

(defn- snapshot-root []
  (.toRealPath
   (.toAbsolutePath (Paths/get (System/getProperty "user.dir")
                               (make-array String 0)))
   (make-array LinkOption 0)))

(defn- contained-path
  [^java.nio.file.Path root relative]
  (let [candidate (.normalize (.resolve root ^String relative))]
    (ensure! (.startsWith candidate root) "UEP-SNAPSHOT-PATH"
             "Dependency closure path escapes the checkout" {:path relative})
    (loop [parent root
           names (seq (iterator-seq (.iterator (.relativize root candidate))))]
      (if-let [name (first names)]
        (let [next-path (.resolve ^java.nio.file.Path parent ^java.nio.file.Path name)]
          (ensure! (not (Files/isSymbolicLink next-path)) "UEP-SNAPSHOT-SYMLINK"
                   "Dependency closure path contains a symbolic link" {:path relative})
          (recur next-path (next names)))
        candidate))))

(defn- file-snapshot
  [^java.nio.file.Path path relative]
  (ensure! (Files/isRegularFile path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
           "UEP-SNAPSHOT-FILE" "Dependency closure entry is not a regular file"
           {:path relative})
  (let [attributes (Files/readAttributes path "basic:size,lastModifiedTime,fileKey"
                                         (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
        digest (MessageDigest/getInstance "SHA-256")
        buffer (ByteBuffer/allocate 8192)
        total (atom 0)]
    (with-open [channel (Files/newByteChannel path
                                              (into-array java.nio.file.OpenOption
                                                          [StandardOpenOption/READ
                                                           LinkOption/NOFOLLOW_LINKS]))]
      (loop []
        (.clear buffer)
        (let [read (.read channel buffer)]
          (when (pos? read)
            (swap! total + read)
            (ensure! (<= @total max-snapshot-file-bytes) "UEP-SNAPSHOT-BOUND"
                     "Dependency closure file exceeds the snapshot bound"
                     {:path relative :bytes @total})
            (.flip buffer)
            (let [chunk (byte-array (.remaining buffer))]
              (.get buffer chunk)
              (.update digest chunk))
            (recur)))))
    (let [after (Files/readAttributes path "basic:size,lastModifiedTime,fileKey"
                                      (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
      (ensure! (= attributes after) "UEP-SNAPSHOT-MUTATED"
               "Dependency closure file changed while it was being read"
               {:path relative})
      {:path relative :bytes @total
       :sha256 (str "sha256:" (apply str (map #(format "%02x" (bit-and % 0xff))
                                              (.digest digest))))})))

(defn- conformance-fixture-paths
  [suite-ids]
  (if (seq suite-ids)
    (let [suites (get-in catalog [:conformance :suites])
          by-id (into {} (map (juxt :id identity) suites))
          unknown (set/difference (set suite-ids) (set (keys by-id)))]
      (when (seq unknown)
        (fail! "UEP-SNAPSHOT-FIXTURE-SUITE"
               "Execution closure references an unknown fixture suite"
               {:unknown (sorted-values unknown)}))
      (let [paths
            (mapcat
             (fn [suite-id]
               (let [suite (get by-id suite-id)
                     root (:fixture-root suite)]
                 (mapcat
                  (fn [row]
                    (for [key [:gravity :qst]]
                      (let [name (get row key)]
                        (ensure! (and (string? root) (string? name)
                                      (= name (.. (java.io.File. name) getName))
                                      (not (str/includes? name ".."))
                                      (not (str/includes? name "\\")))
                                 "UEP-SNAPSHOT-FIXTURE-PATH"
                                 "Conformance fixture path is unsafe"
                                 {:suite suite-id :key key :value name})
                        (str root "/" name))))
                  (concat (:accepted suite) (:rejected suite)))))
             suite-ids)
            paths (vec paths)]
        (ensure! (= (count paths) (count (distinct paths)))
                 "UEP-SNAPSHOT-FIXTURE-PATH"
                 "Conformance fixture rows contain duplicate paths"
                 {:duplicates (sorted-values
                               (map key (filter #(> (count (val %)) 1)
                                                (group-by identity paths))))})
        paths))
    []))

(defn- closure-snapshot
  [plan-value]
  (let [root (snapshot-root)
        fixture-paths (conformance-fixture-paths
                       (get-in plan-value [:dependency-closure :fixture-suite-ids]))
        state (atom {:entries [] :bytes 0})
        append! (fn [entry]
                  (let [{:keys [entries bytes]} @state
                        next-count (inc (count entries))
                        next-bytes (+ bytes (:bytes entry))]
                    ;; Check before retaining the entry so an unbounded tree can
                    ;; never be materialized in memory.
                    (ensure! (<= next-count max-snapshot-entries)
                             "UEP-SNAPSHOT-BOUND" "Dependency closure has too many files"
                             {:entries next-count :maximum max-snapshot-entries})
                    (ensure! (<= next-bytes max-snapshot-total-bytes)
                             "UEP-SNAPSHOT-BOUND" "Dependency closure exceeds total snapshot bound"
                             {:bytes next-bytes :maximum max-snapshot-total-bytes})
                    (swap! state (fn [_] {:entries (conj entries entry)
                                          :bytes next-bytes}))))]
    (doseq [relative (distinct (concat (get-in plan-value [:dependency-closure :paths])
                                       fixture-paths))]
      (let [path (contained-path root relative)]
        (ensure! (Files/exists path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                 "UEP-SNAPSHOT-MISSING" "Dependency closure entry is missing"
                 {:path relative})
        (if (Files/isDirectory path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
          (with-open [stream (Files/walk path (make-array java.nio.file.FileVisitOption 0))]
            (doseq [entry (iterator-seq (.iterator stream))
                    :when (not (Files/isDirectory ^java.nio.file.Path entry
                                                   (into-array LinkOption [LinkOption/NOFOLLOW_LINKS])))]
              (let [entry-relative (str (.relativize root entry))]
                (ensure! (not (Files/isSymbolicLink entry))
                         "UEP-SNAPSHOT-SYMLINK"
                         "Dependency closure directory contains a symbolic link"
                         {:path entry-relative})
                (append! (file-snapshot entry entry-relative)))))
          (append! (file-snapshot path relative)))))
    (let [entries (vec (sort-by :path (:entries @state)))]
      {:entries entries :identity (identity-of entries)})))

(defn- invoke-closed-executor
  [plan-value]
  (if-let [invocation (:artifact-invocation plan-value)]
    ;; Artifact operations use the already-authenticated fast lane adapter.
    ;; The row and stage are read from the plan extension, never supplied as
    ;; an unbound execute-time argument.
    (if-let [adapter (try (requiring-resolve
                           'gravity.self-hosting.execution-lanes/run-fast-artifact!)
                          (catch Throwable _ nil))]
      (let [;; The fast adapter derives its fixture binding from the ordinary
            ;; plan identity.  An artifact wrapper carries a different plan
            ;; identity and module evidence, so reconstruct the exact
            ;; ordinary projection before crossing that seam.
            base-plan (assoc (dissoc plan-value :artifact-invocation :plan-id)
                             :required-evidence
                             (:required-evidence (route (:route-id plan-value)))
                             :plan-id (:base-plan-id invocation))]
        (adapter base-plan (:row-id invocation) (:stage invocation)))
      (fail! "UEP-ARTIFACT-ADAPTER-BOUNDARY"
             "Artifact execution requires the authenticated fast lane adapter"
             {:route-id (:route-id plan-value)
              :required-adapter :exact-tree-warm-worker}))
    (case (get-in plan-value [:executor :adapter])
      :exact-tree-warm-worker
      ;; Resolve the concrete two-lane adapter lazily to avoid a namespace cycle.
      ;; Absence is a closed-executor failure, never permission to fall back to
      ;; an uncoordinated current-JVM mechanism.
      (if-let [adapter (try (requiring-resolve
                             'gravity.self-hosting.execution-lanes/run-fast!)
                            (catch Throwable _ nil))]
        (adapter plan-value)
        (fail! "UEP-FAST-ADAPTER-BOUNDARY"
               "Fast execution requires the brokered lane adapter"
               {:route-id (:route-id plan-value)
                :required-execution-mode (:execution-mode plan-value)
                :required-adapter :exact-tree-warm-worker}))
      :authoritative-proof-runner
      (if-let [adapter (try (requiring-resolve
                             'gravity.self-hosting.execution-lanes/run-fresh!)
                            (catch Throwable _ nil))]
        (adapter plan-value)
        (fail! "UEP-FRESH-ADAPTER-BOUNDARY"
               "Fresh acceptance requires the isolated authoritative proof runner"
               {:route-id (:route-id plan-value)
                :required-execution-mode (:execution-mode plan-value)
                :required-adapter :authoritative-proof-runner
                :migration-gate :isolated-proof-runner-integration}))
      (fail! "UEP-EXECUTOR-ADAPTER" "No closed executor adapter is registered"
             {:adapter (get-in plan-value [:executor :adapter])}))))

(defn plan
  "Build a deterministic closed execution plan from a route/lane request.

  Only the route and lane are required. Optional policy maps may tighten the
  reviewed values but may never add selectors, omit closure inputs, or enable
  cache/authority in the fresh lane."
  [request]
  (request-keys! request)
  (ensure! (string? (:route-id request)) "UEP-REQUEST-ROUTE"
           "Execution request route-id must be a string" {})
  (ensure! (contains? allowed-lanes (:lane request)) "UEP-REQUEST-LANE"
           "Execution request lane is unknown" {:lane (:lane request)})
  (let [route-value (route (:route-id request))
        lane-id (:lane request)
        lane (get-in catalog [:lanes lane-id])]
    (ensure! (some #{lane-id} (:lanes route-value)) "UEP-REQUEST-LANE"
             "Route is not admitted to the requested lane"
             {:route (:route-id request) :lane lane-id})
    (validate-request-overrides! request route-value lane-id)
    (let [artifact-request
          (module-artifact-request (:module-id route-value)
                                   (get-in catalog [:modules (:module-id route-value)]))
          resource-policy
          (merge {:resource-class (:resource-class route-value)}
                 (:bounds route-value)
                 (:resource-policy request))
          requested-cache-mode
          (or (get-in request [:cache-policy :mode])
              (get-in route-value [:cache-policy :mode]))
          cache-policy (assoc (:cache-policy route-value)
                              :mode requested-cache-mode)
          receipt-generation
          {:schema receipt-schema
           :required? true
           :deterministic? true
           :write-once? (= :fresh lane-id)
           :authority-ceiling (:authority-ceiling route-value)}
          base
          {:schema plan-schema
           :version 1
           :catalog-id (identity-of catalog)
           :route-id (:route-id request)
           :lane lane-id
           ;; A fresh lane is acceptance-capable, but this plan is not itself
           ;; proof. The governing proof contract remains the authority gate.
           :authority (if (= :fresh lane-id) :acceptance-capable :non-authoritative)
           :authoritative? false
           :authority-ceiling (:authority-ceiling route-value)
           :selection (:selection route-value)
           :dependency-closure (:dependency-closure route-value)
           :closure-identity (identity-of (:dependency-closure route-value))
           :artifact-request artifact-request
           :resource-policy resource-policy
           :cache-policy cache-policy
           :execution-mode (:execution-mode route-value)
           :executor (:executor route-value)
           :receipt-generation receipt-generation
           :required-evidence (:required-evidence route-value)
           :nonclaims (:nonclaims route-value)
           :guards {:source-authentication :pre-and-post-execution
                    :dependency-closure :complete-and-rechecked
                    :mutation-detection :before-and-after
                    :provenance :semantic-and-actual-path
                    :fresh-cache-observation (= :fresh lane-id)}
           :fresh-authoritative-gate-required? (= :fresh lane-id)}]
      (assoc base :plan-id (identity-of base)))))

(def build-plan plan)

(defn legacy-route-selection
  "Bind one catalog label to a normal fast plan without executing it.

  This transport retains the full catalog identity and all existing plan
  policy. Explicit iteration membership is new selection, not historical
  runnable parity or replacement authority."
  [mechanism label]
  (let [{:keys [route-ids labels]} (legacy-mechanism-projection mechanism)
        route-id (when (string? label)
                   (some (fn [[id candidate]] (when (= label candidate) id))
                         (map vector route-ids labels)))]
    (ensure! route-id "UEP-LEGACY-LABEL" "Unknown catalog route label"
             {:mechanism mechanism :label label :available labels})
    {:mechanism mechanism :label label
     :plan (plan {:route-id route-id :lane :fast})}))

(defn validate-legacy-route-selection
  "Authenticate a label selection against the current complete catalog."
  [selection]
  (exact-keys! selection #{:mechanism :label :plan}
               "UEP-LEGACY-SELECTION-KEYS" [:legacy-selection])
  (validate-plan (:plan selection))
  (ensure! (= selection (legacy-route-selection (:mechanism selection)
                                               (:label selection)))
           "UEP-LEGACY-SELECTION" "Catalog label selection was altered" {})
  selection)

(defn artifact-plan
  "Build a fast artifact operation as an extension of one ordinary plan.

  Callers declare only the reviewed fixture row and earliest artifact stage.
  The binding is derived from the canonical ordinary plan and embedded in the
  resulting content identity.  The ordinary plan itself is never changed, and
  the operation is dispatched only by the one-arity `execute-plan` entrypoint."
  [request]
  (ensure! (map? request) "UEP-ARTIFACT-REQUEST"
           "Artifact execution request must be a map" {})
  (let [allowed (conj #{:route-id :lane :selection :dependency-closure
                         :resource-policy :cache-policy :execution-mode :receipt}
                      :artifact-invocation)
        unknown (set/difference (set (keys request)) allowed)]
    (when (seq unknown)
      (fail! "UEP-ARTIFACT-REQUEST-KEYS"
             "Artifact execution request has unknown keys"
             {:unknown (sorted-values unknown)}))
    (ensure! (= :fast (:lane request))
             "UEP-ARTIFACT-LANE"
             "Artifact operations are admitted only on the fast lane" {})
    (let [input (:artifact-invocation request)]
      (exact-keys! input #{:row-id :stage}
                   "UEP-ARTIFACT-REQUEST-INVOCATION-KEYS"
                   [:request :artifact-invocation])
      (ensure! (and (string? (:row-id input))
                    (not (str/blank? (:row-id input)))
                    (<= (count (:row-id input)) artifact-row-id-max-length))
               "UEP-ARTIFACT-ROW"
               "Artifact fixture row id is invalid or oversized"
               {:row-id (:row-id input)})
      (ensure! (and (keyword? (:stage input))
                    (contains? artifact-stages (:stage input)))
               "UEP-ARTIFACT-STAGE"
               "Artifact stage is outside the closed pipeline"
               {:stage (:stage input)})
      (let [base (plan (dissoc request :artifact-invocation))
            binding (fixture-source-binding base (:row-id input) (:stage input))
            module (get-in catalog
                           [:modules (:module-id (:artifact-request base))])
            invocation {:schema artifact-invocation-schema
                        :version 1
                        :operation artifact-operation
                        :base-plan-id (:plan-id base)
                        :row-id (:row-id input)
                        :stage (:stage input)
                        :binding-id (:binding-id binding)
                        :binding binding}
            extended (assoc base
                            :required-evidence (:required-evidence module)
                            :artifact-invocation invocation)]
        (let [artifact (assoc extended
                              :plan-id (identity-of (dissoc extended :plan-id)))]
          (validate-plan artifact)
          artifact)))))

(defn- canonical-artifact-reference-id
  [value]
  (if-let [id-fn (try (requiring-resolve
                      'gravity.self-hosting.execution-lanes/artifact-reference-id)
                     (catch Throwable _ nil))]
    ((var-get id-fn) value)
    (fail! "UEP-ARTIFACT-REFERENCE"
           "Artifact reference identity helper is unavailable" {})))

(defn- artifact-failure-reference
  [invocation status cache-decision]
  (let [base {:schema artifact-reference-schema
              :operation artifact-operation
              :base-plan-id (:base-plan-id invocation)
              :binding-id (:binding-id invocation)
              :row-id (:row-id invocation)
              :stage (:stage invocation)
              :projection-id nil :result-id nil :selected-artifact-id nil
              :source-content-hash nil
              :source-provenance-id nil :source-relative-path nil
              :source-provenance nil :cache-decision cache-decision
              :execution-boundary nil :authority nil :status status}]
    (assoc base :reference-id (canonical-artifact-reference-id base))))

(defn- fixture-source-row
  [table fixture-suite-ids row-id]
  (ensure! (and (vector? fixture-suite-ids) (seq fixture-suite-ids))
           "UEP-FIXTURE-BINDING-CLOSURE"
           "Fixture source binding requires a non-empty fixture-suite closure"
           {:row-id row-id :fixture-suite-ids fixture-suite-ids})
  (let [table-suites (:suites table)
        known-suite-ids (set (map :id table-suites))
        unknown-suite-ids (set/difference (set fixture-suite-ids)
                                          known-suite-ids)]
    (ensure! (empty? unknown-suite-ids)
             "UEP-FIXTURE-BINDING-CLOSURE"
             "Fixture source binding closure references an unknown suite"
             {:row-id row-id :unknown (sorted-values unknown-suite-ids)})
    (let [rows (vec
                (mapcat (fn [suite]
                          (concat (:accepted suite) (:rejected suite)))
                        (filter #(contains? (set fixture-suite-ids) (:id %))
                                table-suites)))]
      (let [matches (vec (keep (fn [suite]
                                 (when-let [row (some #(when (= row-id (:id %)) %)
                                                      (concat (:accepted suite)
                                                              (:rejected suite)))]
                                   [suite row]))
                               (filter #(contains? (set fixture-suite-ids) (:id %))
                                       table-suites)))]
        (ensure! (= 1 (count matches))
                 "UEP-FIXTURE-BINDING-ROW"
                 "Fixture row is not uniquely contained by the execution closure"
                 {:row-id row-id
                  :matches (mapv (comp :id first) matches)
                  :fixture-suite-ids fixture-suite-ids})
        (first matches)))))

(defn- fixture-source-binding-path
  [suite row key]
  (let [path (str (:fixture-root suite) "/" (name (:status row)) "/" (get row key))]
    (ensure! (normalized-repository-relative-path? path)
             "UEP-FIXTURE-BINDING-PATH"
             "Fixture source binding path is not normalized and repository-relative"
             {:row-id (:id row) :key key :path path})
    path))

(defn fixture-source-binding
  "Derive one authenticated, static fixture-source binding from a closed plan.

  The caller supplies only the reviewed row id and the earliest requested
  compiler-artifact stage.  The plan is validated against the canonical route
  and closure first; the canonical conformance table then revalidates fixture
  ownership, pair presence, no-follow path handling, bounded peer bytes, and
  byte identity.  The returned binding id includes the plan id, row, stage,
  and all derived binding fields.  This projection performs no compilation,
  execution, cache access, or authority admission.
  "
  [plan-value row-id requested-stage]
  (validate-plan plan-value)
  (ensure! (string? row-id)
           "UEP-FIXTURE-BINDING-ROW"
           "Fixture source binding row id must be a string"
           {:row-id row-id})
  (let [requested-rank (get artifact-stage-rank requested-stage)
        declared-stage (get-in plan-value [:artifact-request :stage])
        declared-rank (get artifact-stage-rank declared-stage)]
    (ensure! (some? requested-rank)
             "UEP-FIXTURE-BINDING-STAGE"
             "Fixture source binding stage is not recognized"
             {:row-id row-id :stage requested-stage})
    (ensure! (and (some? declared-rank) (<= requested-rank declared-rank))
             "UEP-FIXTURE-BINDING-STAGE"
             "Fixture source binding stage is later than the module artifact request"
             {:row-id row-id :requested-stage requested-stage
              :declared-stage declared-stage})
    ;; `fixture-pair-binding` validates the canonical table and rechecks the
    ;; selected pair through its existing bounded no-follow reader before this
    ;; projection can expose content identity to a downstream request.
    (let [table (:conformance catalog)
          fixture-suite-ids (get-in plan-value
                                    [:dependency-closure :fixture-suite-ids])
          [suite row] (fixture-source-row table fixture-suite-ids row-id)
          pair (conformance/fixture-pair-binding table row-id)
          ;; `validate-plan` has already authenticated this route; avoid a
          ;; second full catalog/conformance walk while projecting its owner.
          route-value (get-in catalog [:routes (:route-id plan-value)])
          module-id (:module-id route-value)
          artifact-request (:artifact-request plan-value)
          module (get-in catalog [:modules module-id])
          gravity-path (fixture-source-binding-path suite row :gravity)
          qst-path (fixture-source-binding-path suite row :qst)]
      (ensure! (= module-id (:module-id artifact-request))
               "UEP-FIXTURE-BINDING-OWNER"
               "Fixture source binding module does not match the plan"
               {:row-id row-id :module-id module-id
                :artifact-module-id (:module-id artifact-request)})
      (ensure! (= module-id (:module-id suite))
               "UEP-FIXTURE-BINDING-OWNER"
               "Fixture source binding suite is not owned by the plan module"
               {:row-id row-id :suite-id (:id suite)
                :module-id module-id :suite-module-id (:module-id suite)})
      (ensure! (= (:module-owner artifact-request) (:owner module))
               "UEP-FIXTURE-BINDING-OWNER"
               "Fixture source binding owner is stale"
               {:row-id row-id :module-id module-id})
      (ensure! (and (= (:id suite) (:suite-id pair))
                    (= (:id row) (:row-id pair))
                    (= (:status row) (:status pair)))
               "UEP-FIXTURE-BINDING-PAIR"
               "Fixture source binding pair identity is stale"
               {:row-id row-id :suite-id (:id suite)
                :pair-suite-id (:suite-id pair)})
      (ensure! (= #{:schema :suite-id :row-id :status :sha256 :bytes :authenticated?}
                  (set (keys pair)))
               "UEP-FIXTURE-BINDING-PAIR"
               "Fixture source binding pair contract is not closed"
               {:row-id row-id :pair pair})
      (ensure! (and (= conformance/fixture-pair-binding-schema (:schema pair))
                    (true? (:authenticated? pair))
                    (string? (:sha256 pair))
                    (re-matches #"sha256:[0-9a-f]{64}" (:sha256 pair))
                    (integer? (:bytes pair))
                    (pos? (:bytes pair))
                    (<= (:bytes pair) conformance/maximum-fixture-peer-bytes))
               "UEP-FIXTURE-BINDING-PAIR"
               "Fixture source binding pair content identity is malformed"
               {:row-id row-id :pair pair})
      (let [binding-fields {:suite-id (:id suite)
                            :row-id (:id row)
                            :status (:status row)
                            :module-id module-id
                            :module-owner (:module-owner artifact-request)
                            :stage requested-stage
                            :gravity-path gravity-path
                            :qst-path qst-path
                            :fixture-sha256 (:sha256 pair)
                            :fixture-bytes (:bytes pair)}
            binding-id (identity-of {:plan-id (:plan-id plan-value)
                                     :row-id row-id
                                     :stage requested-stage
                                     :binding binding-fields})
            binding (merge {:schema fixture-source-binding-schema
                            :plan-id (:plan-id plan-value)
                            :catalog-id (:catalog-id plan-value)
                            :route-id (:route-id plan-value)
                            :authenticated? true
                            :binding-id binding-id}
                           binding-fields)]
        (exact-keys! binding
                     fixture-source-binding-keys
                     "UEP-FIXTURE-BINDING-KEYS"
                     [:fixture-source-binding])
        (ensure! (re-matches #"sha256:[0-9a-f]{64}" binding-id)
                 "UEP-FIXTURE-BINDING-ID"
                 "Fixture source binding identity is not a SHA-256 id"
                 {:row-id row-id :binding-id binding-id})
        binding))))

(defn- bounded-exact-integer?
  "Check a bounded integer without coercing BigInt through a machine long."
  [value minimum maximum]
  (and (integer? value)
       (<= minimum value maximum)))

(defn- bounded-observation-text?
  [value]
  (and (string? value)
       (not (str/blank? value))
       (<= (count value) cache-observation-max-text-length)))

(defn- canonical-cached-shard-plan?
  "Return true only for the validated fast cached-shard executor shape.

  This intentionally inspects declarative plan data rather than requiring the
  lane adapter, avoiding a namespace cycle and preventing generic/fresh plans
  from accepting cache observations merely because they carry an artifact."
  [plan-value]
  (let [request (get-in plan-value [:executor :request])
        route-value (get-in catalog [:routes (:route-id plan-value)])
        canonical-resource-policy
        (when route-value
          (merge {:resource-class (:resource-class route-value)}
                 (:bounds route-value)))]
    (and (= :fast (:lane plan-value))
         (= :eligible (get-in plan-value [:cache-policy :mode]))
         (= :warm-bounded-process (:execution-mode plan-value))
         (= :exact-tree-warm-worker (get-in plan-value [:executor :adapter]))
         (= :non-authoritative (:authority plan-value))
         (= :non-authoritative (:authority-ceiling plan-value))
         (false? (:authoritative? plan-value))
         (= canonical-resource-policy (:resource-policy plan-value))
         (map? request)
         (= #{:operation :shard :result-contract-id} (set (keys request)))
         (= :cached-shard (:operation request))
         (= cached-result-contract-id (:result-contract-id request))
         (string? (:shard request))
         (not (str/blank? (:shard request))))))

(defn- cached-artifact?
  [value]
  (and (map? value) (= cached-artifact (:artifact value))))

(defn- cached-artifacts
  [result]
  (let [artifacts (:artifacts result)]
    ;; Results from the lane use a bounded vector.  Do not realize arbitrary
    ;; lazy sequences supplied by an injected callback while checking for a
    ;; cached artifact; a direct raw map is checked without realization too.
    (cond
      (vector? artifacts) (vec (filter cached-artifact? artifacts))
      (cached-artifact? artifacts) [artifacts]
      :else [])))

(defn- validate-cache-observation!
  [plan-value observation raw-artifacts]
  (exact-keys! observation cache-observation-keys
               "UEP-RESULT-CACHE-OBSERVATION-KEYS"
               [:result :cache-observation])
  (ensure! (= cache-observation-schema (:schema observation))
           "UEP-RESULT-CACHE-OBSERVATION"
           "Cache observation schema is not recognized"
           {:schema (:schema observation)})
  (let [request (get-in plan-value [:executor :request])]
    (ensure! (= cached-result-contract-id
                (:result-contract-id observation)
                (:result-contract-id request))
             "UEP-RESULT-CACHE-OBSERVATION"
             "Cache observation result contract is not recognized"
             {:expected cached-result-contract-id
              :actual (:result-contract-id observation)})
    (ensure! (= cached-artifact (:artifact observation))
             "UEP-RESULT-CACHE-OBSERVATION"
             "Cache observation artifact is not recognized"
             {:artifact (:artifact observation)})
    (ensure! (= (:shard request) (:shard observation))
             "UEP-RESULT-CACHE-OBSERVATION"
             "Cache observation shard does not match its plan"
             {:expected (:shard request) :actual (:shard observation)}))
  (let [context (:context observation)]
    (exact-keys! context cache-observation-context-keys
                 "UEP-RESULT-CACHE-OBSERVATION-CONTEXT-KEYS"
                 [:result :cache-observation :context])
    (ensure! (bounded-exact-integer? (:request-schema-version context)
                                     1 1000)
             "UEP-RESULT-CACHE-OBSERVATION-CONTEXT"
             "Cache observation request schema version is outside its bound"
             {:value (:request-schema-version context)})
    (ensure! (keyword? (:adapter context))
             "UEP-RESULT-CACHE-OBSERVATION-CONTEXT"
             "Cache observation adapter identity must be a keyword"
             {:value (:adapter context)})
    (doseq [key [:checked-core-source-content-hash
                 :checked-core-plan-semantic-hash
                 :java-runtime-version
                 :clojure-version]]
      (ensure! (bounded-observation-text? (get context key))
               "UEP-RESULT-CACHE-OBSERVATION-CONTEXT"
               "Cache observation context text is outside its bound"
               {:key key :value (get context key)})))
  (let [counters (:counters observation)]
    (exact-keys! counters cache-observation-counter-keys
                 "UEP-RESULT-CACHE-OBSERVATION-COUNTER-KEYS"
                 [:result :cache-observation :counters])
    (doseq [key (sort-by name cache-observation-counter-keys)]
      (ensure! (bounded-exact-integer? (get counters key)
                                       0 cache-observation-max-count)
               "UEP-RESULT-CACHE-OBSERVATION-COUNTER"
               "Cache observation counter is outside its bound"
               {:key key :value (get counters key)})))
  (ensure! (bounded-exact-integer? (:elapsed-ms observation)
                                   0 cache-observation-max-elapsed-ms)
           "UEP-RESULT-CACHE-OBSERVATION"
           "Cache observation elapsed time is outside its bound"
           {:elapsed-ms (:elapsed-ms observation)})
  (ensure! (false? (:cache-authoritative? observation))
           "UEP-RESULT-CACHE-OBSERVATION-AUTHORITY"
           "Cache observation cannot provide authoritative evidence" {})
  (ensure! (true? (:fresh-authoritative-run-required? observation))
           "UEP-RESULT-CACHE-OBSERVATION-AUTHORITY"
           "Cache observation must require a fresh authoritative run" {})
  (when-let [raw (first raw-artifacts)]
    (let [expected {:schema cache-observation-schema
                    :result-contract-id
                    (get-in plan-value [:executor :request :result-contract-id])
                    :artifact (:artifact raw)
                    :shard (:shard raw)
                    :context (:cache-context raw)
                    :counters (:cache raw)
                    :elapsed-ms (:elapsed-ms raw)
                    :cache-authoritative? (:cache-authoritative? raw)
                    :fresh-authoritative-run-required?
                    (:fresh-authoritative-run-required? raw)}]
      (ensure! (= expected observation)
               "UEP-RESULT-CACHE-OBSERVATION-MISMATCH"
               "Cache observation does not match its authenticated raw artifact"
               {:expected expected :actual observation})))
  observation)

(defn- validate-result
  [plan-value result]
  (ensure! (map? result) "UEP-RESULT-SHAPE" "Execution result must be a map" {})
  (let [allowed #{:status :test-count :pass :fail :error :diagnostics :evidence
                 :artifacts :mutation-detected? :fresh-replayable?
                 :provenance-checked? :source-authenticated?
                 :closure-verified? :cache-decision :cache-observation}]
    (let [unknown (set/difference (set (keys result)) allowed)]
      (when (seq unknown)
        (fail! "UEP-RESULT-KEYS" "Execution result has unknown keys"
               {:unknown (sorted-values unknown)}))))
  (let [cached-plan? (canonical-cached-shard-plan? plan-value)
        raw-artifacts (cached-artifacts result)
        observation-present? (contains? result :cache-observation)]
    (when-not cached-plan?
      (ensure! (not observation-present?)
               "UEP-RESULT-CACHE-OBSERVATION"
               "Cache observations are admitted only for canonical cached-shard plans"
               {:route-id (:route-id plan-value) :lane (:lane plan-value)}))
    (when cached-plan?
      (when observation-present?
        (ensure! (= 1 (count raw-artifacts))
                 "UEP-RESULT-CACHE-OBSERVATION"
                 "A cache observation requires one raw cached artifact"
                 {:count (count raw-artifacts)}))
      (when (seq raw-artifacts)
        (ensure! observation-present?
                 "UEP-RESULT-CACHE-OBSERVATION-REQUIRED"
                 "A cached raw artifact requires its cache observation"
                 {:route-id (:route-id plan-value)}))
      (when (= :passed (:status result))
        (ensure! (and observation-present? (= 1 (count raw-artifacts)))
                 "UEP-RESULT-CACHE-OBSERVATION-REQUIRED"
                 "A passing cached result requires one raw artifact and observation"
                 {:route-id (:route-id plan-value)}))
      (when observation-present?
        (validate-cache-observation! plan-value (:cache-observation result)
                                     raw-artifacts))))
  (ensure! (contains? #{:passed :failed :blocked} (:status result))
           "UEP-RESULT-STATUS" "Execution result status is unknown"
           {:status (:status result)})
  (doseq [key [:test-count :pass :fail :error]]
    (when (contains? result key)
      (ensure! (and (integer? (get result key))
                    (not (neg? (get result key)))
                    (<= (get result key) maximum-safe-long))
               "UEP-RESULT-COUNT" "Execution result count is invalid"
               {:key key :value (get result key)})))
  ;; `:test-count` counts test vars while pass/fail/error count assertion
  ;; outcomes in clojure.test. They intentionally do not share a denominator.
  (when (contains? result :diagnostics)
    (ensure! (vector? (:diagnostics result)) "UEP-RESULT-DIAGNOSTICS"
             "Execution diagnostics must be a vector" {}))
  (when (contains? result :evidence)
    (ensure! (vector? (:evidence result)) "UEP-RESULT-EVIDENCE"
             "Execution evidence must be a vector" {}))
  (doseq [key [:mutation-detected? :fresh-replayable? :provenance-checked?
               :source-authenticated? :closure-verified?]]
    (when (contains? result key)
      (boolean! (get result key) "UEP-RESULT-GUARD" [:result key])))
  (when (contains? result :cache-decision)
    (ensure! (contains? #{:hit :miss :disabled :forbidden}
                        (:cache-decision result))
             "UEP-RESULT-CACHE" "Execution cache decision is unknown" {}))
  (let [status (:status result)
        fresh? (= :fresh (:lane plan-value))
        passed? (= :passed status)]
    (when passed?
      (ensure! (true? (:source-authenticated? result)) "UEP-RESULT-SOURCE"
               "A passing result must authenticate source bytes" {})
      (ensure! (true? (:closure-verified? result)) "UEP-RESULT-CLOSURE"
               "A passing result must recheck dependency closure" {})
      (ensure! (true? (:provenance-checked? result)) "UEP-RESULT-PROVENANCE"
               "A passing result must check semantic and actual-path provenance" {})
      (ensure! (false? (:mutation-detected? result)) "UEP-RESULT-MUTATION"
               "A passing result cannot report source mutation" {})
      (ensure! (set/subset? (set (:required-evidence plan-value))
                            (set (:evidence result)))
               "UEP-RESULT-EVIDENCE"
               "A passing result omits required evidence" {})
      (when fresh?
        (ensure! (true? (:fresh-replayable? result))
                 "UEP-RESULT-FRESH-REPLAYABLE"
                 "A passing fresh result must remain independently replayable" {})
        (ensure! (contains? #{:disabled :forbidden}
                            (or (:cache-decision result) :forbidden))
                 "UEP-RESULT-FRESH-CACHE"
                 "A fresh result cannot observe a development cache" {}))))
  (merge {:test-count 0 :pass 0 :fail 0 :error 0
          :diagnostics [] :evidence []
          :mutation-detected? false
          :fresh-replayable? false
          :provenance-checked? false
          :source-authenticated? false
          :closure-verified? false
          :cache-decision (if (= :fresh (:lane plan-value)) :forbidden :miss)}
         result))

(defn- validate-artifact-reference-binding!
  [plan-value invocation reference]
  (let [binding (:binding invocation)
        provenance (:source-provenance reference)
        root (repository-root)
        canonical-root (when root
                         (str (.toRealPath ^java.nio.file.Path root
                                           (make-array LinkOption 0))))
        canonical-source (when root
                          (try
                            (str (.toRealPath
                                  (contained-path root (:gravity-path binding))
                                  (make-array LinkOption 0)))
                            (catch Throwable _ nil)))]
    (ensure! (= (:fixture-sha256 binding)
                (:source-content-hash reference))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference source hash is stale" {})
    (ensure! (= (:gravity-path binding)
                (:source-relative-path reference)
                (:project-relative-path provenance))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference source path is stale" {})
    (ensure! (= canonical-root (:project-root-path provenance))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference project root is stale" {})
    (ensure! (= canonical-source (:actual-path provenance))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference actual path is stale" {})
    (ensure! (= (:fixture-bytes binding) (:byte-count provenance))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference source byte count is stale" {})
    (ensure! (= (:fixture-sha256 binding) (:content-hash provenance))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference provenance hash is stale" {})
    (ensure! (= (:source-provenance-id reference)
                (canonical-artifact-reference-id provenance))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference provenance identity is stale" {})
    (ensure! (= {:mode :fast :isolation :same-jvm
                 :isolated? false :acceptance-authority? false
                 :fresh-authoritative-run-required? true}
                (:execution-boundary reference))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference execution boundary is stale" {})
    (ensure! (= {:authoritative? false
                 :ceiling :non-authoritative
                 :development-only? true
                 :nonclaims (:nonclaims plan-value)}
                (:authority reference))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact reference authority is stale" {})
    reference))

(defn- normalize-artifact-result
  [plan-value raw-result]
  (let [invocation (:artifact-invocation plan-value)
        _ (ensure! (map? raw-result) "UEP-ARTIFACT-RESULT"
                   "Artifact adapter result must be a map" {})
        allowed-result-keys
        #{:status :projection :authority :execution-boundary :diagnostics
          :mutation-detected? :provenance-checked? :source-authenticated?
          :closure-verified? :fresh-replayable? :cache-decision :evidence
          :artifact-reference}
        unknown (set/difference (set (keys raw-result)) allowed-result-keys)
        _ (when (seq unknown)
            (fail! "UEP-ARTIFACT-RESULT-KEYS"
                   "Artifact adapter result has unknown keys"
                   {:unknown (sorted-values unknown)}))
        reference (:artifact-reference raw-result)
        ;; Projection, authority, and execution-boundary are authenticated by
        ;; the lane and retained only in the compact reference.  They are not
        ;; part of the ordinary UEP result contract/receipt shape.
        result (select-keys raw-result
                            #{:status :diagnostics :mutation-detected?
                              :fresh-replayable? :provenance-checked?
                              :source-authenticated? :closure-verified?
                              :cache-decision :evidence})
        status (:status result)
        diagnostics-value (if (contains? result :diagnostics)
                            (:diagnostics result)
                            [])
        _ (ensure! (and (vector? diagnostics-value)
                        (<= (count diagnostics-value) 64))
                   "UEP-ARTIFACT-DIAGNOSTICS"
                   "Artifact diagnostics must be a bounded vector" {})
        _ (doseq [[index diagnostic] (map-indexed vector diagnostics-value)]
            (ensure! (and (map? diagnostic)
                          (= #{:id :message} (set (keys diagnostic)))
                          (string? (:id diagnostic))
                          (not (str/blank? (:id diagnostic)))
                          (<= (count (:id diagnostic)) 128)
                          (string? (:message diagnostic))
                          (not (str/blank? (:message diagnostic)))
                          (<= (count (:message diagnostic)) 4096))
                     "UEP-ARTIFACT-DIAGNOSTICS"
                     "Artifact diagnostic is not a closed bounded map"
                     {:index index :diagnostic diagnostic}))
        diagnostics diagnostics-value
        _ (ensure! (or (not= :passed status) (some? reference))
                   "UEP-ARTIFACT-REFERENCE"
                   "A passing artifact result requires an authenticated reference" {})
        reference
        (if reference
          (do
            (ensure! (and (map? reference)
                          (= artifact-reference-keys (set (keys reference)))
                          (= artifact-reference-schema (:schema reference)))
                     "UEP-ARTIFACT-REFERENCE"
                     "Artifact adapter reference is not a closed value" {})
            (if-let [verifier (try (requiring-resolve
                                    'gravity.self-hosting.execution-lanes/verify-artifact-reference)
                                   (catch Throwable _ nil))]
              ((var-get verifier) reference)
              (ensure! false "UEP-ARTIFACT-REFERENCE"
                       "Artifact reference verifier is unavailable" {}))
            (ensure! (= artifact-operation (:operation reference))
                     "UEP-ARTIFACT-REFERENCE"
                     "Artifact reference operation is unknown" {})
            (ensure! (= (:base-plan-id invocation) (:base-plan-id reference))
                     "UEP-ARTIFACT-REFERENCE"
                     "Artifact reference base plan is stale" {})
            (ensure! (= (:binding-id invocation) (:binding-id reference))
                     "UEP-ARTIFACT-REFERENCE"
                     "Artifact reference binding is stale" {})
            (ensure! (= (:row-id invocation) (:row-id reference))
                     "UEP-ARTIFACT-REFERENCE"
                     "Artifact reference row is stale" {})
            (ensure! (= (:stage invocation) (:stage reference))
                     "UEP-ARTIFACT-REFERENCE"
                     "Artifact reference stage is stale" {})
            (ensure! (= status (:status reference))
                     "UEP-ARTIFACT-REFERENCE"
                     "Artifact reference status is stale" {})
            (when (= :passed status)
              (validate-artifact-reference-binding! plan-value invocation reference))
            reference)
          (artifact-failure-reference invocation status
                                      (or (:cache-decision result) :forbidden)))]
    {:result (cond-> result
               (= :passed status)
               (assoc :evidence (vec (:required-evidence plan-value))))
     :reference reference}))

(defn- execute-plan*
  "Execute a validated plan through an injected bounded substrate callback.

  The callback receives only the immutable plan and must return a closed result
  map. This function generates a deterministic receipt identity while keeping
  elapsed time observational (and outside the identity). It never grants
  integration, proof, release, safety, performance, self-hosting, or seed
  retirement authority."
  [plan-value execute-fn injected?]
   (validate-plan plan-value)
   (ensure! (fn? execute-fn) "UEP-EXECUTOR" "Execution callback must be callable" {})
   (when (and injected? (contains? plan-value :artifact-invocation))
     (fail! "UEP-ARTIFACT-EXECUTOR"
            "Artifact plans must use the authenticated one-arity dispatcher" {}))
   (let [artifact? (contains? plan-value :artifact-invocation)
         started (System/nanoTime)
         raw-result (try
                      (execute-fn plan-value)
                 (catch InterruptedException error
                   (.interrupt (Thread/currentThread))
                   (throw error))
                 (catch VirtualMachineError error
                   (throw error))
                 (catch ThreadDeath error
                   (throw error))
                 (catch LinkageError error
                   (throw error))
                 (catch Exception error
                   (let [data (when (instance? clojure.lang.ExceptionInfo error)
                                (ex-data error))]
                     {:status :failed
                      :diagnostics [(cond-> {:id (or (:id data) "UEP-EXECUTION-THREW")
                                             :message "Execution substrate raised an exception"}
                                       (not artifact?)
                                       (assoc :class (.getName (class error))
                                              :data (dissoc data :id)))]})))
         {:keys [result reference]}
         (if (contains? plan-value :artifact-invocation)
           (normalize-artifact-result plan-value raw-result)
           {:result raw-result})
         result (validate-result plan-value result)
         cache-mode (get-in plan-value [:cache-policy :mode])
         cache-decision (:cache-decision result)
         cache-base {:mode cache-mode
                     :decision cache-decision
                     :authoritative? false}
         cache-value (if (contains? result :cache-observation)
                       (assoc cache-base :observation (:cache-observation result))
                       cache-base)
         receipt-base
         {:schema receipt-schema
          :version 1
          :plan-id (:plan-id plan-value)
          :route-id (:route-id plan-value)
          :lane (:lane plan-value)
          :authority (:authority plan-value)
          :authoritative? false
          :authority-ceiling (:authority-ceiling plan-value)
          :status (:status result)
          :summary (cond-> (select-keys result [:test-count :pass :fail :error])
                     reference
                     (assoc :artifact reference))
          :selection (:selection plan-value)
          :closure-identity (:closure-identity plan-value)
          :cache cache-value
          :execution {:mode (:execution-mode plan-value)
                      :resource-policy (:resource-policy plan-value)
                      :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))}
          :diagnostics (:diagnostics result)
          :evidence (:evidence result)
          :integrity {:source-authenticated? (:source-authenticated? result)
                      :dependency-closure-verified? (:closure-verified? result)
                      :provenance-checked? (:provenance-checked? result)
                      :mutation-detected? (:mutation-detected? result)}
          :fresh-replayable? (:fresh-replayable? result)
          :required-evidence (:required-evidence plan-value)
          :nonclaims (:nonclaims plan-value)
          :fresh-authoritative-gate-required?
          (:fresh-authoritative-gate-required? plan-value)}
         ;; Cache counters and elapsed time are observations of this run, not
         ;; identity inputs.  Provenance (schema, contract, artifact, shard,
         ;; context, and authority flags) remains in the receipt identity so
         ;; observations cannot be rebound to a different cached product.
         receipt-identity (cond-> (dissoc receipt-base :execution)
                            (contains? result :cache-observation)
                            (update-in [:cache :observation]
                                       #(dissoc % :counters :elapsed-ms)))
         receipt-id (identity-of receipt-identity)]
     (assoc receipt-base :receipt-id receipt-id)))

(defn execute-plan
  "Execute one validated plan through the closed execution engine.

  Artifact plans are dispatched only by this one-arity entrypoint; the
  two-arity callback seam remains available for ordinary focused tests."
  ([plan-value]
   (execute-plan* plan-value invoke-closed-executor false))
  ([plan-value execute-fn]
   (execute-plan* plan-value execute-fn true)))

(defn verify-receipt
  "Verify the closed receipt identity emitted by `execute-plan`.

  This is an integrity check only; it does not grant any acceptance or
  integration authority.  Artifact references are verified at their compact
  adapter-issued boundary and remain nested inside the ordinary receipt shape."
  ([receipt] (verify-receipt nil receipt))
  ([plan-value receipt]
  (exact-keys! receipt receipt-keys "UEP-RECEIPT-KEYS" [:receipt])
  (ensure! (= receipt-schema (:schema receipt))
           "UEP-RECEIPT-SCHEMA" "Receipt schema is not recognized" {})
  (ensure! (= 1 (:version receipt))
           "UEP-RECEIPT-VERSION" "Receipt version is not recognized" {})
  (when plan-value
    (validate-plan plan-value)
    (doseq [[path expected actual]
            [[ :plan-id (:plan-id plan-value) (:plan-id receipt)]
             [ :route-id (:route-id plan-value) (:route-id receipt)]
             [ :lane (:lane plan-value) (:lane receipt)]
             [ :selection (:selection plan-value) (:selection receipt)]
             [ :closure-identity (:closure-identity plan-value)
               (:closure-identity receipt)]
             [ :required-evidence (:required-evidence plan-value)
               (:required-evidence receipt)]
             [ :nonclaims (:nonclaims plan-value) (:nonclaims receipt)]]]
      (ensure! (= expected actual) "UEP-RECEIPT-PLAN"
               "Receipt is bound to another execution plan" {:path path}))
    (when-let [invocation (:artifact-invocation plan-value)]
      (let [reference (get-in receipt [:summary :artifact])]
        (ensure! reference "UEP-RECEIPT-ARTIFACT"
                 "Artifact plan receipt lacks its operation reference" {})
        (doseq [[field expected actual]
                [[:base-plan-id (:base-plan-id invocation)
                  (:base-plan-id reference)]
                 [:binding-id (:binding-id invocation)
                  (:binding-id reference)]
                 [:row-id (:row-id invocation) (:row-id reference)]
                 [:stage (:stage invocation) (:stage reference)]]]
          (ensure! (= expected actual) "UEP-RECEIPT-ARTIFACT"
                   "Receipt artifact reference is bound to another invocation"
                   {:field field}))
        (when (= :passed (:status receipt))
          (validate-artifact-reference-binding! plan-value invocation reference))))
    (when (and (nil? (:artifact-invocation plan-value))
               (contains? (:summary receipt) :artifact))
      (fail! "UEP-RECEIPT-ARTIFACT"
             "Ordinary plan receipt cannot carry an artifact reference" {})))
  (ensure! (and (string? (:receipt-id receipt))
                (re-matches #"sha256:[0-9a-f]{64}" (:receipt-id receipt)))
           "UEP-RECEIPT-ID" "Receipt identity is malformed" {})
  (ensure! (and (map? (:summary receipt))
                (empty? (set/difference (set (keys (:summary receipt)))
                                        #{:test-count :pass :fail :error :artifact})))
           "UEP-RECEIPT-SUMMARY" "Receipt summary is not closed" {})
  (when-let [reference (get-in receipt [:summary :artifact])]
    (ensure! (and (map? reference)
                  (= artifact-reference-keys (set (keys reference)))
                  (= artifact-reference-schema (:schema reference))
                  (= artifact-operation (:operation reference))
                  (= (:status receipt) (:status reference)))
             "UEP-RECEIPT-ARTIFACT" "Receipt artifact reference is not closed" {})
    (if-let [verifier (try (requiring-resolve
                            'gravity.self-hosting.execution-lanes/verify-artifact-reference)
                           (catch Throwable _ nil))]
      ((var-get verifier) reference)
      (ensure! false "UEP-RECEIPT-ARTIFACT"
               "Artifact reference verifier is unavailable" {}))
    (when (= :passed (:status receipt))
      (doseq [field [:projection-id :result-id :selected-artifact-id
                     :source-content-hash :source-provenance-id
                     :source-relative-path :source-provenance
                     :execution-boundary :authority]]
        (ensure! (some? (get reference field))
                 "UEP-RECEIPT-ARTIFACT"
                 "Passed receipt artifact reference is incomplete" {:field field}))))
  (let [identity (cond-> (dissoc receipt :execution :receipt-id)
                   (contains? (get-in receipt [:cache]) :observation)
                   (update-in [:cache :observation]
                              #(dissoc % :counters :elapsed-ms)))]
    (ensure! (= (:receipt-id receipt) (identity-of identity))
             "UEP-RECEIPT-ID" "Receipt identity does not match its content" {}))
  receipt))

(def run-plan execute-plan)
