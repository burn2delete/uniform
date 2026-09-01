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

(def ^:private catalog-keys
  #{:schema :version :authority :authoritative? :phase :complete?
    :description :lanes :resource-classes :modules :routes :suites
    :coverage :evidence-policy :conformance})
(def ^:private conformance-keys
  #{:source-of-truth :schema :authoritative? :suites :nonclaims :authority
    :suite-order :coverage :preserved-bespoke :complete? :version})
(def ^:private lane-keys
  #{:id :kind :authority :authoritative? :incremental? :bounded? :warm?
    :reproducible? :cache-policy :execution-mode :receipt-policy :nonclaims})
(def ^:private lane-cache-keys
  #{:mode :authoritative? :fresh-replay-required?})
(def ^:private lane-receipt-keys
  #{:schema :required? :authority-ceiling})
(def ^:private resource-keys
  #{:id :max-concurrency :timeout-ms :max-output-bytes :max-test-vars})
(def ^:private module-keys
  #{:id :owner :source-paths :artifact-request :ownership-scope
    :required-evidence})
(def ^:private selection-keys #{:kind :namespace :selectors})
(def ^:private closure-keys
  #{:module-ids :paths :complete? :identity-inputs})
(def ^:private closure-optional-keys #{:fixture-suite-ids})
(def ^:private bounds-keys #{:timeout-ms :max-output-bytes :max-test-vars})
(def ^:private route-cache-keys #{:mode :key-inputs :authoritative?})
(def ^:private route-receipt-keys #{:schema :required-fields})
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
(def ^:private evidence-policy-keys
  #{:required :authority-ceilings :fresh-acceptance-requires})
(def ^:private plan-keys
  #{:schema :version :catalog-id :route-id :lane :authority :authoritative?
    :authority-ceiling :selection :dependency-closure :closure-identity
    :resource-policy :cache-policy :execution-mode :executor
    :receipt-generation :required-evidence :nonclaims :guards
    :fresh-authoritative-gate-required? :plan-id})
(def ^:private allowed-lanes #{:fast :fresh})
(def ^:private allowed-cache-modes #{:eligible :disabled :forbidden})
(def ^:private allowed-execution-modes
  #{:warm-bounded-process :fresh-isolated-process})
(def ^:private allowed-authority-ceilings #{:non-authoritative :acceptance-only})
(def ^:private allowed-selection-kinds #{:exact-vars :proof-contract-module-catalog})
(def ^:private executor-keys #{:adapter :bounded? :fresh-capable? :authority})
(def ^:private allowed-executors
  #{:current-jvm-exact-vars :authoritative-proof-runner})
(def ^:private required-cache-key-inputs
  #{:dependency-closure :source-bytes :tool :policy})
(def ^:private maximum-resource-timeout-ms (* 4 60 60 1000))
(def ^:private maximum-resource-output-bytes (* 256 1024))
(def ^:private maximum-resource-test-vars 4096)
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
  (ensure! (and (integer? value) (pos? (long value)))
           id "Verification bound must be a positive integer"
           {:path path :value value})
  (long value))

(defn- non-negative-integer!
  [value id path]
  (ensure! (and (integer? value) (not (neg? (long value))))
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
  (boolean! (get-in lane [:cache-policy :fresh-replay-required?])
            "UEP-CATALOG-LANE-CACHE-BOOLEAN"
            [:lanes lane-id :cache-policy :fresh-replay-required?])
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
  (doseq [key [:max-concurrency :timeout-ms :max-output-bytes :max-test-vars]]
    (positive-integer! (get resource key) "UEP-CATALOG-RESOURCE-BOUND"
                       [:resource-classes class-id key]))
  (ensure! (<= (:timeout-ms resource) maximum-resource-timeout-ms)
           "UEP-CATALOG-RESOURCE-BOUND" "Resource timeout exceeds global bound" {})
  (ensure! (<= (:max-output-bytes resource) maximum-resource-output-bytes)
           "UEP-CATALOG-RESOURCE-BOUND" "Resource output exceeds global bound" {})
  (ensure! (<= (:max-test-vars resource) maximum-resource-test-vars)
           "UEP-CATALOG-RESOURCE-BOUND" "Resource selector bound exceeds global bound" {})
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
  (ensure! (= :checked-core (:artifact-request module))
           "UEP-CATALOG-MODULE-ARTIFACT"
           "Only the checked-core artifact request is supported in this phase"
           {:module module-id :artifact-request (:artifact-request module)})
  (ensure! (keyword? (:ownership-scope module)) "UEP-CATALOG-MODULE-OWNERSHIP"
           "Module ownership scope must be a keyword" {:module module-id})
  (non-empty-vector! (:required-evidence module) "UEP-CATALOG-MODULE-EVIDENCE"
                     [:modules module-id :required-evidence])
  module)

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
        test-relative (-> namespace
                          (str/replace "." "/")
                          (str/replace "-" "_")
                          (str (if (str/ends-with? namespace "-test") ".clj"
                                   ".clj")))
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
    (doseq [key [:timeout-ms :max-output-bytes :max-test-vars]]
      (positive-integer! (get-in route [:bounds key])
                         "UEP-CATALOG-ROUTE-BOUNDS"
                         [:routes route-id :bounds key])
      (ensure! (<= (long (get-in route [:bounds key]))
                   (long (get resource key)))
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
  (exact-keys! (:executor route) executor-keys "UEP-CATALOG-ROUTE-EXECUTOR-KEYS"
               [:routes route-id :executor])
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
                      (= :current-jvm-exact-vars
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
  (when-let [conformance (:conformance value)]
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
         (edn/read-string (String. bytes StandardCharsets/UTF_8))
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
  (->> (:routes manifest)
       (mapcat (fn [[route-id route]]
                 (map (fn [legacy] [legacy route-id])
                      (:replacement-for route))))
       (group-by first)
       (map (fn [[legacy pairs]]
              [legacy (vec (sort (map second pairs)))]))
       (into (sorted-map))))

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

  Legacy names are labels for compatibility and migration accounting only;
  they do not become executable authorities.  A mechanism can map to several
  declarative routes, so the value is a deterministic vector of route ids."
  ([ ] (legacy-route-catalog catalog))
  ([manifest]
   (validate-catalog manifest)
   (legacy-route-catalog-value manifest)))

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
        (ensure! (<= (long (get policy key)) (long (get-in route [:bounds key])))
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

(defn validate-plan
  "Validate a generated plan, including its content identity."
  [plan-value]
  (ensure! (map? plan-value) "UEP-PLAN-SHAPE" "Execution plan must be a map" {})
  (exact-keys! plan-value plan-keys "UEP-PLAN-KEYS" [:plan])
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
        lane-value (get-in catalog [:lanes lane-id])]
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
    (ensure! (= (:execution-mode route-value) (:execution-mode plan-value))
             "UEP-PLAN-MODE" "Execution plan execution mode was altered" {})
    (ensure! (= (:executor route-value) (:executor plan-value))
             "UEP-PLAN-EXECUTOR" "Execution plan executor was altered" {})
    (ensure! (= (:required-evidence route-value) (:required-evidence plan-value))
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
    (exact-keys! (:resource-policy plan-value)
                 #{:resource-class :timeout-ms :max-output-bytes :max-test-vars}
                 "UEP-PLAN-RESOURCE" [:plan :resource-policy])
    (ensure! (= (:resource-class route-value)
                (:resource-class (:resource-policy plan-value)))
             "UEP-PLAN-RESOURCE" "Execution plan resource class was altered" {})
    (doseq [key [:timeout-ms :max-output-bytes :max-test-vars]]
      (positive-integer! (get-in plan-value [:resource-policy key])
                         "UEP-PLAN-RESOURCE" [:plan :resource-policy key])
      (ensure! (<= (long (get-in plan-value [:resource-policy key]))
                   (long (get-in route-value [:bounds key])))
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

(defn- exact-var-symbol
  [namespace selector]
  (let [namespace-symbol (symbol namespace)
        test-symbol (symbol selector)
        test-var (ns-resolve namespace-symbol test-symbol)]
    (ensure! (and (var? test-var) (:test (meta test-var)))
             "UEP-EXECUTOR-TEST" "Catalog selector does not resolve to a test var"
             {:namespace namespace :selector selector})
    test-var))

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

(defn- run-current-jvm-exact-vars
  "Run a fast-lane exact-var route in the current JVM.

  This adapter is explicitly non-authoritative. It performs no process
  isolation and therefore cannot satisfy the fresh acceptance lane. The
  existing warm worker may replace this substrate after exact parity review."
  [plan-value]
  (ensure! (= :fast (:lane plan-value)) "UEP-EXECUTOR-LANE"
           "The current-JVM adapter is restricted to the fast lane" {})
  (ensure! (= :current-jvm-exact-vars (get-in plan-value [:executor :adapter]))
           "UEP-EXECUTOR-ADAPTER" "Unexpected current-JVM adapter request" {})
  (if-not (some? *resource-lease*)
    {:status :blocked
     :diagnostics [{:id "UEP-RESOURCE-LEASE-REQUIRED"
                    :message "Current-JVM execution requires an explicit resource lease"}]
     :evidence []
     :mutation-detected? false
     :fresh-replay? false
     :provenance-checked? false
     :source-authenticated? false
     :closure-verified? false
     :cache-decision :disabled}
    (if-not (and (map? *resource-lease*)
                 (= true (:bounded-executor? *resource-lease*))
                 (= true (:timeout-enforced? *resource-lease*))
                 (integer? (:deadline-ns *resource-lease*))
                 (> (long (:deadline-ns *resource-lease*)) (System/nanoTime)))
      {:status :blocked
       :diagnostics [{:id "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
                      :message "Current-JVM execution requires a reviewed bounded executor context"}]
       :evidence [] :mutation-detected? false :fresh-replay? false
       :provenance-checked? false :source-authenticated? false
       :closure-verified? false :cache-decision :disabled}
      (let [selection (:selection plan-value)
            namespace (:namespace selection)
            selectors (:selectors selection)]
    (ensure! (<= (count selectors)
                 (long (get-in plan-value [:resource-policy :max-test-vars])))
             "UEP-EXECUTOR-BOUND" "Selector count exceeds the reviewed resource bound"
             {:count (count selectors)
              :maximum (get-in plan-value [:resource-policy :max-test-vars])})
    ;; Snapshot before loading the namespace: require may execute top-level code.
    (let [before (closure-snapshot plan-value)]
      (require (symbol namespace))
      (let [
          vars (mapv #(exact-var-symbol namespace %) selectors)
          counters (ref test/*initial-report-counters*)]
      (binding [test/*report-counters* counters]
        (test/test-vars vars))
      (let [after (closure-snapshot plan-value)
            unchanged? (= before after)
            {:keys [test pass fail error]} @counters]
        {:status (if (and unchanged? (zero? fail) (zero? error)) :passed :failed)
         :test-count test
         :pass pass
         :fail fail
         :error error
         :diagnostics (if unchanged? [] [{:id "UEP-SNAPSHOT-MUTATED"
                                          :message "Dependency closure changed during execution"}])
         :evidence (:required-evidence plan-value)
         :mutation-detected? (not unchanged?)
         :fresh-replay? false
         :provenance-checked? unchanged?
         :source-authenticated? unchanged?
         :closure-verified? unchanged?
         :cache-decision :miss})))))))

(defn- invoke-closed-executor
  [plan-value]
  (case (get-in plan-value [:executor :adapter])
    :current-jvm-exact-vars (run-current-jvm-exact-vars plan-value)
    :authoritative-proof-runner
    (fail! "UEP-FRESH-ADAPTER-BOUNDARY"
           "Fresh acceptance requires the isolated authoritative proof runner"
           {:route-id (:route-id plan-value)
            :required-execution-mode (:execution-mode plan-value)
            :required-adapter :authoritative-proof-runner
            :migration-gate :isolated-proof-runner-integration})
    (fail! "UEP-EXECUTOR-ADAPTER" "No closed executor adapter is registered"
           {:adapter (get-in plan-value [:executor :adapter])})))

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
    (let [resource-policy
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

(defn- validate-result
  [plan-value result]
  (ensure! (map? result) "UEP-RESULT-SHAPE" "Execution result must be a map" {})
  (let [allowed #{:status :test-count :pass :fail :error :diagnostics :evidence
                 :artifacts :mutation-detected? :fresh-replay?
                 :provenance-checked? :source-authenticated?
                 :closure-verified? :cache-decision}]
    (let [unknown (set/difference (set (keys result)) allowed)]
      (when (seq unknown)
        (fail! "UEP-RESULT-KEYS" "Execution result has unknown keys"
               {:unknown (sorted-values unknown)}))))
  (ensure! (contains? #{:passed :failed :blocked} (:status result))
           "UEP-RESULT-STATUS" "Execution result status is unknown"
           {:status (:status result)})
  (doseq [key [:test-count :pass :fail :error]]
    (when (contains? result key)
      (ensure! (and (integer? (get result key)) (not (neg? (long (get result key)))))
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
  (doseq [key [:mutation-detected? :fresh-replay? :provenance-checked?
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
        (ensure! (true? (:fresh-replay? result)) "UEP-RESULT-FRESH-REPLAY"
                 "A passing fresh result must report independent replay" {})
        (ensure! (contains? #{:disabled :forbidden}
                            (or (:cache-decision result) :forbidden))
                 "UEP-RESULT-FRESH-CACHE"
                 "A fresh result cannot observe a development cache" {}))))
  (merge {:test-count 0 :pass 0 :fail 0 :error 0
          :diagnostics [] :evidence []
          :mutation-detected? false
          :fresh-replay? false
          :provenance-checked? false
          :source-authenticated? false
          :closure-verified? false
          :cache-decision (if (= :fresh (:lane plan-value)) :forbidden :miss)}
         result))

(defn execute-plan
  "Execute a validated plan through an injected bounded substrate callback.

  The callback receives only the immutable plan and must return a closed result
  map. This function generates a deterministic receipt identity while keeping
  elapsed time observational (and outside the identity). It never grants
  integration, proof, release, safety, performance, self-hosting, or seed
  retirement authority."
  ([plan-value]
   (execute-plan plan-value invoke-closed-executor))
  ([plan-value execute-fn]
   (validate-plan plan-value)
   (ensure! (fn? execute-fn) "UEP-EXECUTOR" "Execution callback must be callable" {})
   (let [started (System/nanoTime)
         result (try
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
                      :diagnostics [{:id (or (:id data) "UEP-EXECUTION-THREW")
                                     :message "Execution substrate raised an exception"
                                     :class (.getName (class error))
                                     :data (dissoc data :id)}]})))
         result (validate-result plan-value result)
         cache-mode (get-in plan-value [:cache-policy :mode])
         cache-decision (:cache-decision result)
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
          :summary (select-keys result [:test-count :pass :fail :error])
          :selection (:selection plan-value)
          :closure-identity (:closure-identity plan-value)
          :cache {:mode cache-mode
                  :decision cache-decision
                  :authoritative? false}
          :execution {:mode (:execution-mode plan-value)
                      :resource-policy (:resource-policy plan-value)
                      :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))}
          :diagnostics (:diagnostics result)
          :evidence (:evidence result)
          :integrity {:source-authenticated? (:source-authenticated? result)
                      :dependency-closure-verified? (:closure-verified? result)
                      :provenance-checked? (:provenance-checked? result)
                      :mutation-detected? (:mutation-detected? result)}
          :fresh-replay? (:fresh-replay? result)
          :required-evidence (:required-evidence plan-value)
          :nonclaims (:nonclaims plan-value)
          :fresh-authoritative-gate-required?
          (:fresh-authoritative-gate-required? plan-value)}
         receipt-id (identity-of (dissoc receipt-base :execution))]
     (assoc receipt-base :receipt-id receipt-id))))

(def run-plan execute-plan)
