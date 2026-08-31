(ns gravity.self-hosting.sh01-impact-test-planner
  "Slice-aware, fail-closed test planning for the self-hosting backlog.

  This is a leaf utility: it reads the coordinator-owned ownership record and
  backlog, but does not modify either one. Test and fixture ownership follows
  the SH-01 naming conventions, while exact Stage0 Clojure source/test paths
  use their reserved component owner and slice closure. A new leaf test remains
  parallel-safe without an edit to the central runner."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [gravity.self-hosting.sh01-component-test-dependencies
             :as component-dependencies]
            [gravity.self-hosting-test-runner :as runner]))

(def ^:private slice-count 30)

(declare valid-slice? path-slice)

;; These reviewed namespaces are verification infrastructure.  The general
;; self-hosting runner discovers and can execute them, but they are not SH-00
;; through SH-29 leaf tests and therefore must never enter the slice planner's
;; catalog (or be inferred as SH-07 by a broad fallback).
(def ^:private reviewed-non-slice-namespaces
  #{'gravity.self-hosting.a1-canonical-schema-test
    'gravity.self-hosting.p15-public-native-admission-test
    'gravity.self-hosting.p18-t00-orchestration-test
    'gravity.self-hosting.p18-t00-semantics-test
    'gravity.self-hosting.stage3-fragment-size-preflight-test
    'gravity.self-hosting.stage3-verification-runner-test
    'gravity.self-hosting.w5-b13-artifact-emitter-test
    'gravity.self-hosting.w5-b14-backend-conformance-verifier-test
    'gravity.self-hosting.w5-c16-incremental-executor-test
    'gravity.self-hosting.w5-c17-plugin-executor-test
    'gravity.self-hosting.w5-c18-pass-verifier-test
    'gravity.self-hosting.w5-compiler-identity-verifier-test
    'gravity.self-hosting.w5-compiler-pipeline-verifier-test
    'gravity.self-hosting.w5-domain-schema-ai-executor-test
    'gravity.self-hosting.w5-full-language-evidence-verifier-test
    'gravity.self-hosting.w5-ir-lowering-executor-test
    'gravity.self-hosting.w5-performance-math-executor-test
    'gravity.self-hosting.w5-reader-module-executor-test
    'gravity.self-hosting.w5-stage-equivalence-verifier-test
    'gravity.self-hosting.w5-stage-rebuild-orchestrator-test
    'gravity.self-hosting.w5-subsystem-closure-verifier-test
    'gravity.self-hosting.w5-tooling-conformance-verifier-test
    'gravity.self-hosting.w5-trust-provenance-verifier-test
    'gravity.self-hosting.w5-typed-effect-safety-executor-test
    ;; Request-local optimization coverage is verification infrastructure, not
    ;; a countable SH-00..SH-29 leaf namespace.  Keep it outside the slice
    ;; catalog so adding it cannot make ordinary impact planning fail closed.
    'gravity.self-hosting.macro-operations-normalization-test})

(def ^:private exact-test-var-routes
  "Reviewed exact test-var ownership for bounded development routes.

  These routes deliberately narrow execution below a namespace boundary.  The
  route still owns the complete test source path and slice; only the listed
  vars are allowed to run during ordinary iteration.  A fresh namespace or
  integration run remains required for authoritative evidence."
  (sorted-map
   "b8-regression"
   {:slice "SH-07"
    :owner :sh-core
    :path "bootstrap/clojure/test/gravity/self_hosting/sh07_try_catch_test.clj"
    :namespace 'gravity.self-hosting.sh07-try-catch-test
    :test-vars
    '[gravity.self-hosting.sh07-try-catch-test/sh07-b8-authenticated-input-substitution-fails-before-lowering
      gravity.self-hosting.sh07-try-catch-test/sh07-b8-public-proof-is-bounded-and-does-not-claim-later-facts]
    :selection-mode :exact-test-vars
    :resource-class :memory-heavy}))

(def ^:private exact-test-var-route-keys
  #{:slice :owner :path :namespace :test-vars :selection-mode
    :resource-class})

(defn exact-test-var-route-names
  "Returns the closed exact-test-var route catalog in deterministic order."
  []
  (vec (keys exact-test-var-routes)))

(declare exact-test-var-route-options)

(defn exact-test-var-route
  "Returns one reviewed exact-test-var route or fails closed."
  [route-name]
  (let [route-name (when (string? route-name) route-name)]
    (if-let [route (get exact-test-var-routes route-name)]
      (exact-test-var-route-options route)
      (throw
       (ex-info
        "Unknown SH-01 exact test-var route"
        {:id "SH01-IMPACT-ROUTE"
         :route route-name
         :available (exact-test-var-route-names)})))))

(defn- exact-test-var-route-options
  [route]
  (let [{:keys [slice owner path namespace test-vars selection-mode
                resource-class]} route
        route-keys (when (map? route) (set (keys route)))
        missing-keys (vec (sort (set/difference exact-test-var-route-keys
                                                (or route-keys #{}))))
        extra-keys (vec (sort (set/difference (or route-keys #{})
                                              exact-test-var-route-keys)))
        invalid-vars
        (->> test-vars
             (remove
              #(and (symbol? %)
                    (= namespace (symbol (clojure.core/namespace %)))))
             vec)]
    (when-not (and (map? route)
                   (= exact-test-var-route-keys route-keys)
                   (valid-slice? slice)
                   (keyword? owner)
                   (string? path)
                   (symbol? namespace)
                   (vector? test-vars)
                   (seq test-vars)
                   (= (count test-vars) (count (distinct test-vars)))
                   (= :exact-test-vars selection-mode)
                   (keyword? resource-class)
                   (empty? invalid-vars))
      (throw
       (ex-info
        "SH-01 exact test-var route metadata is malformed"
        {:id "SH01-IMPACT-ROUTE-CONTRACT"
         :route route
         :missing-keys missing-keys
         :extra-keys extra-keys
         :invalid-test-vars invalid-vars})))
    route))

(def ^:private required-fixed-stage-infrastructure-namespaces
  #{'gravity.self-hosting.stage3-fragment-size-preflight-test
    'gravity.self-hosting.stage3-verification-runner-test})

(def ^:private component-cross-cutting-reason
  :reserved-component-owner-has-no-slice)

(defn- standalone-test-index
  [record dependency-index]
  (let [owners (:standalone-test-owners record)
        dependencies (:standalone-tests dependency-index)
        dependencies-by-path (into {} (map (juxt :path identity)) dependencies)
        owner-paths (set (keys owners))
        dependency-paths (set (keys dependencies-by-path))]
    (when-not (and (map? owners)
                   (= owner-paths dependency-paths))
      (throw
       (ex-info
        "Standalone incremental tests require matching ownership and dependency records"
        {:id "SH01-STANDALONE-TEST-CONTRACT"
         :ownership-only-paths
         (vec (sort (set/difference owner-paths dependency-paths)))
         :dependency-only-paths
         (vec (sort (set/difference dependency-paths owner-paths)))})))
    (let [entries
          (mapv
           (fn [relative]
             (let [owner (get owners relative)
                   dependency (get dependencies-by-path relative)
                   slice (:slice owner)
                   expected-owner
                   (get-in record [:slice-owners slice :leaf-owner])]
               (when-not (and (= #{:namespace :slice :owner}
                                 (set (keys owner)))
                              (= (:namespace owner) (:namespace dependency))
                              (valid-slice? slice)
                              (= expected-owner (:owner owner)))
                 (throw
                  (ex-info
                   "Standalone incremental test ownership is malformed or disagrees with its dependency record"
                   {:id "SH01-STANDALONE-TEST-CONTRACT"
                    :path relative
                    :owner owner
                    :dependency dependency
                    :expected-owner expected-owner})))
               (merge dependency
                      {:slice slice
                       :owner (:owner owner)
                       :classification :standalone-test})))
           (sort owner-paths))]
      {:by-path (into (sorted-map) (map (juxt :path identity)) entries)
       :by-namespace
       (into (sorted-map) (map (juxt :namespace identity)) entries)})))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh01_impact_test_planner.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-01 impact planner source is not on the classpath"
        {:id "SH01-IMPACT-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH01-IMPACT-ROOT"}))

        (and (.isFile (.toFile (.resolve path "deps.edn")))
             (.isFile
              (.toFile
               (.resolve path "docs/self-hosting-slice-ownership.edn"))))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (.toFile (.resolve @root relative)))

(defn- ownership-record
  []
  (edn/read-string
   {:readers *data-readers*}
   (slurp (path "docs/self-hosting-slice-ownership.edn"))))

(defn- slice-id
  [number]
  (format "SH-%02d" number))

(defn- all-slices
  []
  (set (map slice-id (range slice-count))))

(defn- namespace-slice
  [namespace]
  (some->> (re-find
            #"^gravity\.self-hosting\.sh(\d{2})(?:-|$)"
            (str namespace))
           second
           parse-long
           slice-id))

(defn- path-slice
  [relative]
  (or
   (some->> (re-find
             #"^bootstrap/clojure/test/gravity/self_hosting/sh(\d{2})(?:_|/)"
             relative)
            second
            parse-long
            slice-id)
   (some->> (re-find
             #"^bootstrap/clojure/fixtures/self-hosting/sh-(\d{2})(?:-|/|$)"
             relative)
            second
            parse-long
            slice-id)))

(defn validate-backlog-dependency-entries
  "Validates and canonicalizes parsed backlog dependency entries."
  [entries]
  (let [entries (vec entries)
        expected (all-slices)
        actual (set (map first entries))
        duplicate-slices
        (->> entries
             (map first)
             frequencies
             (keep (fn [[slice count]]
                     (when (< 1 count) slice)))
             sort
             vec)
        referenced
        (into #{} (mapcat second) entries)
        missing (set/difference expected actual)
        unexpected (set/difference actual expected)
        unknown-dependencies (set/difference referenced expected)]
    (when (or (seq duplicate-slices)
              (seq missing)
              (seq unexpected)
              (seq unknown-dependencies))
      (throw
       (ex-info
        "The self-hosting backlog must define exactly SH-00 through SH-29"
        {:id "SH01-IMPACT-BACKLOG"
         :duplicate-slices duplicate-slices
         :missing-slices (vec (sort missing))
         :unexpected-slices (vec (sort unexpected))
         :unknown-dependencies (vec (sort unknown-dependencies))})))
    (into (sorted-map) entries)))

(defn backlog-dependencies
  "Returns the validated dependency map encoded by the countable backlog table."
  []
  (->> (str/split-lines
        (slurp (path "docs/self-hosting-slice-backlog.md")))
       (keep
        (fn [line]
          (let [columns (mapv str/trim (str/split line #"\|"))]
            (when (and (<= 4 (count columns))
                       (re-matches #"`SH-\d{2}`" (get columns 1 "")))
              (let [id (subs (get columns 1) 1 6)
                    dependencies
                    (set (re-seq #"SH-\d{2}" (get columns 3 "")))]
                [id dependencies])))))
       validate-backlog-dependency-entries))

(defn downstream-closure
  "Expands directly affected slices to all transitive dependants."
  [dependencies direct]
  (loop [affected (set direct)]
    (let [expanded
          (into
           affected
           (for [[slice requirements] dependencies
                 :when (seq (set/intersection affected requirements))]
             slice))]
      (if (= affected expanded)
        affected
        (recur expanded)))))

(defn- coordinator-prefixes
  [record]
  (let [coordinator (:coordinator-owned record)]
    (concat
     (:legacy-fixture-prefixes coordinator)
     (:generated-evidence-prefixes coordinator)
     (:coverage-prefixes coordinator))))

(defn- coordinator-exact-paths
  [record]
  (let [coordinator (:coordinator-owned record)]
    (set
     (mapcat
      coordinator
      [:central-routing
       :host-boundary-modules
       :host-boundary-tests
       :integration-surfaces
       :coordination-state
       :coverage-generator-paths
       :completion-state-paths]))))

(defn- relevant-owned-root?
  [relative]
  (or
   (str/starts-with? relative "bootstrap/gravity/src/")
   (boolean
    (re-matches
     #"bootstrap/clojure/src/gravity/[^/]+\.clj"
     relative))
   (boolean
    (re-matches
     #"bootstrap/clojure/test/gravity/[^/]+_test\.clj"
     relative))
   (str/starts-with?
    relative
    "bootstrap/clojure/test/gravity/self_hosting/")
   (str/starts-with?
    relative
    "bootstrap/clojure/fixtures/self-hosting/")))

(defn- stage0-component-id
  "Derives a Stage0 component id from an exact top-level source/test stem."
  [relative]
  (some-> (re-find
           #"^bootstrap/clojure/(?:src|test)/gravity/([^/]+?)(?:_test)?\.clj$"
           relative)
          second
          (str/replace "_test" "")
          (str/replace "_" "-")))

(defn- stage0-component-metadata
  "Returns reviewed component identity only for an exact reserved stem.

  A path is enriched only when the reserved id, owner, and canonical paired
  source/test paths agree.  This keeps synthetic or future paths fail-closed
  instead of guessing a component namespace from a filename alone.
  "
  [record relative module-owner]
  (when (and module-owner
             (or (re-matches #"bootstrap/clojure/src/gravity/[^/]+\.clj"
                             relative)
                 (re-matches #"bootstrap/clojure/test/gravity/[^/]+_test\.clj"
                             relative)))
    (let [component-id (stage0-component-id relative)
          reserved (:reserved-leaf-modules record)
          reserved-owner (get reserved component-id)
          stem (some-> component-id (str/replace "-" "_"))
          source-path (when stem
                        (str "bootstrap/clojure/src/gravity/" stem ".clj"))
          test-path (when stem
                      (str "bootstrap/clojure/test/gravity/" stem "_test.clj"))
          expected-path
          (cond
            (str/starts-with? relative "bootstrap/clojure/src/") source-path
            (str/starts-with? relative "bootstrap/clojure/test/") test-path)]
      (when (and (string? component-id)
                 (contains? reserved component-id)
                 (= module-owner reserved-owner)
                 (= relative expected-path))
        {:component-id component-id
         :component-owner module-owner
         :component-source-path source-path
         :component-test-path test-path
         :test-namespace
         (symbol (str "gravity." component-id "-test"))}))))

(defn- component-identities
  [classified]
  (->> classified
       (keep
        (fn [entry]
          (when (:component-id entry)
            (select-keys entry
                         [:component-id
                          :component-owner
                          :component-source-path
                          :component-test-path
                          :test-namespace
                          :component-cross-cutting?
                          :component-cross-cutting-reason]))))
       distinct
       (sort-by (juxt :component-id :component-test-path))
       vec))

(defn- component-plan-fields
  [classified]
  (let [components (component-identities classified)
        cross-cutting
        (->> classified
             (filter :component-cross-cutting?)
             (map :path)
             sort
             vec)]
    (cond->
     {:component-identities components
      :component-test-namespaces (mapv :test-namespace components)}
      (seq cross-cutting)
      (assoc
       :authority :non-authoritative
       :authoritative? false
       :non-authoritative? true
       :component-cross-cutting? true
       :component-cross-cutting-reason component-cross-cutting-reason
       :component-cross-cutting-paths cross-cutting))))

(defn classify-path
  "Classifies a repository-relative path for impact planning.

  Shared coordinator surfaces conservatively affect every slice. Dedicated
  tests and fixtures derive their one slice from the SH-01 path convention.
  Exact Stage0 Clojure paths derive a component identity only when the
  reserved id, owner, and paired stem agree. A reviewed component whose owner
  has no slice mapping conservatively selects all slices and remains explicitly
  non-authoritative. A path inside an owned self-hosting root that matches
  neither rule is unowned and causes changed-file planning to fail closed."
  [record relative]
  (let [standalone-owner (get (:standalone-test-owners record) relative)
        module-owner (get (:module-owners record) relative)
        slice-owners (:slice-owners record)
        owner-slices
        (when module-owner
          (set
           (for [[slice ownership] slice-owners
                 :when (= module-owner (:leaf-owner ownership))]
             slice)))
        dedicated-slice (path-slice relative)
        component-metadata (stage0-component-metadata
                            record
                            relative
                            module-owner)
        coordinator?
        (or
         (contains? (coordinator-exact-paths record) relative)
         (some #(str/starts-with? relative %)
               (coordinator-prefixes record)))]
    (cond
      coordinator?
      (merge
       {:path relative
        :classification :coordinator
        :slices (vec (sort (all-slices)))}
       component-metadata)

      standalone-owner
      {:path relative
       :classification :module
       :slices [(:slice standalone-owner)]
       :test-namespace (:namespace standalone-owner)}

      dedicated-slice
      {:path relative
       :classification :dedicated
       :slices [dedicated-slice]}

      (= :master-coordinator module-owner)
      (merge
       {:path relative
        :classification :coordinator
        :slices (vec (sort (all-slices)))}
       component-metadata)

      (seq owner-slices)
      (merge
       {:path relative
        :classification :module
        :slices (vec (sort owner-slices))}
       component-metadata)

      component-metadata
      (merge
       {:path relative
        :classification :module
        :slices (vec (sort (all-slices)))
        :authority :non-authoritative
        :authoritative? false
        :non-authoritative? true
        :component-cross-cutting? true
        :component-cross-cutting-reason component-cross-cutting-reason}
       component-metadata)

      (relevant-owned-root? relative)
      {:path relative :classification :unowned :slices []}

      :else
      {:path relative :classification :unrelated :slices []})))

(defn- duplicate-namespaces
  [namespaces]
  (->> namespaces
       frequencies
       (keep
        (fn [[namespace count]]
          (when (< 1 count)
            namespace)))
       sort
       vec))

(defn- test-catalog
  ([]
   (let [record (ownership-record)
         dependencies (component-dependencies/dependency-index)]
     (test-catalog (standalone-test-index record dependencies))))
  ([standalone-index]
  (let [namespaces (vec (runner/dedicated-test-namespaces))
        duplicates (duplicate-namespaces namespaces)
        discovered (set namespaces)
        standalone-by-namespace (:by-namespace standalone-index)
        missing-infrastructure
        (set/difference required-fixed-stage-infrastructure-namespaces
                        discovered)
        unknown-non-slice
        (->> namespaces
             (remove
              #(or (contains? reviewed-non-slice-namespaces %)
                   (contains? standalone-by-namespace %)
                   (namespace-slice %)))
             distinct
             sort
             vec)]
    ;; The general runner owns duplicate/collision detection.  Keep the same
    ;; fail-closed contract here as well so a changed discovery implementation
    ;; cannot hide a duplicate fixed-stage namespace before it is filtered.
    (when (seq duplicates)
      (throw
       (ex-info
        "Dedicated self-hosting test namespaces must map to one file"
        {:id "SH01-TEST-NAMESPACE-COLLISION"
         :collisions
         (into
          (sorted-map)
          (for [namespace duplicates]
            [namespace
             (vec (repeat (get (frequencies namespaces) namespace)
                          :discovered))]))})))
    ;; Require the reviewed infrastructure pair to remain discoverable.  A
    ;; rename/removal must be surfaced as catalog drift instead of silently
    ;; shrinking the fixed-stage boundary.
    (when (or (seq missing-infrastructure)
              (seq unknown-non-slice))
      (throw
       (ex-info
        "The discovered self-hosting catalog has fixed-stage or unknown non-slice drift"
        {:id "SH01-IMPACT-CATALOG"
         :missing-fixed-stage-infrastructure
         (vec (sort missing-infrastructure))
         :unknown-non-slice-namespaces unknown-non-slice
         :discovered-namespaces (vec (sort namespaces))})))
    (->> namespaces
         (remove #(contains? reviewed-non-slice-namespaces %))
         (map
         (fn [namespace]
            {:namespace namespace
             :slice (or (namespace-slice namespace)
                        (get-in standalone-by-namespace
                                [namespace :slice]))}))
         vec))))

(defn- valid-slice?
  [slice]
  (and (string? slice)
       (contains? (all-slices) slice)))

(defn validate-test-catalog
  "Validates the complete discovered dedicated-test catalog.

  Catalog discovery is an input boundary, rather than a selection hint.  A
  missing namespace-to-slice mapping, a namespace outside SH-00 through
  SH-29, a duplicate namespace identity, or a malformed catalog entry
  therefore fails closed before any namespace can be selected.  The catalog
  is allowed to have no tests for a particular slice; every discovered entry
  must simply map to one of the bounded slices exactly once.
  "
  ([catalog]
   (validate-test-catalog catalog {:by-namespace {}}))
  ([catalog standalone-index]
  (let [catalog (when (some? catalog) (vec catalog))
        standalone-by-namespace (:by-namespace standalone-index)
        duplicate-details
        (when catalog
          (->> catalog
               (filter map?)
               (filter #(some? (:namespace %)))
               (group-by :namespace)
               (keep
                (fn [[namespace entries]]
                  (when (< 1 (count entries))
                    {:namespace namespace
                     :count (count entries)
                     :entries (vec entries)})))
               (sort-by (comp str :namespace))
               vec))
        malformed-entries
        (if (or (nil? catalog) (empty? catalog))
          [{:entry nil
            :reason (if (nil? catalog) :catalog-nil :catalog-empty)}]
          (->> catalog
               (keep
                (fn [entry]
                  (cond
                    (not (map? entry))
                    {:entry entry :reason :entry-not-map}

                    (nil? (:namespace entry))
                    {:entry entry :reason :namespace-missing}

                    (not (valid-slice? (:slice entry)))
                    {:entry entry :reason :slice-invalid}

                    (not= (:slice entry)
                          (or (namespace-slice (:namespace entry))
                              (get-in standalone-by-namespace
                                      [(:namespace entry) :slice])))
                    {:entry entry :reason :namespace-slice-mismatch}

                    :else
                    nil)))
               vec))
        malformed
        (into
         malformed-entries
         (map
          (fn [{:keys [namespace count entries]}]
            {:namespace namespace
             :count count
             :entries entries
             :reason :namespace-duplicate})
          duplicate-details))]
    (when (seq malformed)
      (throw
       (ex-info
        "The discovered SH-01 test catalog must contain unique namespaces mapped to SH-00 through SH-29"
        {:id "SH01-IMPACT-CATALOG"
         :entries malformed
         :catalog catalog
         :duplicate-namespaces (mapv :namespace duplicate-details)
         :duplicate-entries duplicate-details
         :slices
         (->> malformed
              (keep (fn [{:keys [entry]}]
                      (when (map? entry) (:slice entry))))
              distinct
              (sort-by str)
              vec)})))
    catalog)))

(defn planning-context
  "Creates an isolated, request-local planner context.

  Ownership and dependency records are read once when the context is built.
  The discovered test catalog is delayed until a request has passed its
  syntactic, slice, and ownership checks; once forced, the delay memoizes the
  catalog for this context only.  No global mutable selection cache is used.

  The optional map is intended for tests and callers that already loaded
  coordinator inputs.  `:catalog` may be supplied to avoid discovery, while
  `:ownership` and `:dependencies` replace the corresponding coordinator
  records.
  "
  ([]
   (planning-context {}))
  ([options]
   (let [options (or options {})
         ownership
         (if (contains? options :ownership)
           (:ownership options)
           (ownership-record))
         component-dependencies
         (if (contains? options :component-dependencies)
           (:component-dependencies options)
           (component-dependencies/dependency-index))
         standalone-index
         (standalone-test-index ownership component-dependencies)]
     {:ownership
      ownership
      :dependencies
      (if (contains? options :dependencies)
        (:dependencies options)
        (backlog-dependencies))
      :component-dependencies
      component-dependencies
      :standalone-test-index standalone-index
      :catalog-delay
      (delay
       (if (contains? options :catalog)
         (:catalog options)
         (test-catalog standalone-index)))})))

(defn- context-catalog
  [context]
  (cond
    (contains? context :catalog)
    (:catalog context)

    (contains? context :catalog-delay)
    @(:catalog-delay context)

    :else
    (throw
     (ex-info
      "SH-01 planner context is missing its test catalog"
      {:id "SH01-IMPACT-CONTEXT"}))))

(declare dedicated-test-path? dedicated-test-namespace repository-path-exists?)

(defn- resource-class
  [slice]
  (cond
    (= "SH-07" slice) :memory-heavy
    (contains? #{"SH-26" "SH-27" "SH-28" "SH-29"} slice) :exclusive
    :else :normal))

(defn- reviewed-component-selection
  [dependency-index entry]
  (let [relative (:path entry)
        source-entry (get-in dependency-index [:by-source-path relative])
        test-entry (get-in dependency-index [:by-test-path relative])]
    (cond
      source-entry
      (do
        (when-not (= (:component-id source-entry) (:component-id entry))
          (throw
           (ex-info
            "Reviewed component dependency disagrees with Stage0 ownership"
            {:id "SH01-COMPONENT-DEPENDENCY-CONTRACT"
             :path relative
             :dependency-component (:component-id source-entry)
             :ownership-component (:component-id entry)})))
        {:selection :reviewed-component-dependencies
         :reason :reviewed-component-source
         :component-id (:component-id source-entry)
         :tests (:tests source-entry)
         :test-namespaces (mapv :namespace (:tests source-entry))})

      test-entry
      (let [test-id (or (:component-id test-entry) (:test-id test-entry))]
        (when (and (:component-id entry)
                   (not= test-id (:component-id entry)))
          (throw
           (ex-info
            "Reviewed test dependency disagrees with Stage0 ownership"
            {:id "SH01-COMPONENT-DEPENDENCY-CONTRACT"
             :path relative
             :dependency-component test-id
             :ownership-component (:component-id entry)})))
        (if (repository-path-exists? relative)
          {:selection :exact-test-path
           :reason :reviewed-component-test
           :component-id test-id
           :tests [(select-keys test-entry
                                [:path :namespace :jvm-group :test-policy
                                 :cache-closure
                                 :cache-closure-valid?])]
           :test-namespaces [(:namespace test-entry)]}
          (let [component
                (get-in dependency-index
                        [:by-component-id test-id])]
            {:selection :reviewed-component-dependencies
             :reason :deleted-reviewed-component-test
             :component-id test-id
             :tests (:tests component)
             :test-namespaces (mapv :namespace (:tests component))})))

      :else
      nil)))

(defn- exact-dedicated-selection
  [catalog entry]
  (when (and (= :dedicated (:classification entry))
             (dedicated-test-path? (:path entry)))
    (when-let [namespace (dedicated-test-namespace catalog (:path entry))]
      {:selection :exact-test-path
       :reason :changed-dedicated-test
       :tests [{:path (:path entry) :namespace namespace}]
       :test-namespaces [namespace]})))

(defn- development-selection
  [dependency-index catalog entry]
  (or (reviewed-component-selection dependency-index entry)
      (exact-dedicated-selection catalog entry)))

(defn- enrich-development-selections
  [dependency-index catalog classified]
  (mapv
   (fn [entry]
     (if-let [selection (development-selection dependency-index catalog entry)]
       (merge entry
              {:development-selection (:selection selection)
               :development-invalidation-reason (:reason selection)
               :development-component-id (:component-id selection)
               :development-tests (:tests selection)
               :development-test-namespaces (:test-namespaces selection)})
       (assoc entry
              :development-selection
              (if (= :unrelated (:classification entry))
                :ignored
                :slice-closure)
              :development-invalidation-reason
              (if (= :unrelated (:classification entry))
                :unrelated-path
                :unreviewed-or-deleted-path))))
   classified))

(defn- exact-development-namespaces
  [classified]
  (->> classified
       (mapcat :development-test-namespaces)
       distinct
       sort
       vec))

(defn- closure-owned?
  [ownership closure]
  (every?
   #(contains? #{:coordinator :dedicated :module}
               (:classification (classify-path ownership (:path %))))
   (:inputs closure)))

(defn- exact-development-job-metadata
  [ownership classified]
  (into
   {}
   (for [entry classified
         test-entry (:development-tests entry)
         :let [namespace (:namespace test-entry)]
         :when (:development-component-id entry)]
     [namespace
      (cond->
       {:component-id (:development-component-id entry)
        :batch-key (str "component/"
                        (:development-component-id entry)
                        "/"
                        (:jvm-group test-entry))
        :test-policy (:test-policy test-entry)}
        (and (:cache-closure-valid? test-entry)
             (closure-owned? ownership (:cache-closure test-entry)))
        (assoc :cache-closure (:cache-closure test-entry)
               :cache-closure-authorized? true))])))

(defn- dedicated-test-path?
  "Returns true when a path names a dedicated self-hosting test file.

  The path convention is intentionally narrower than `path-slice`: a fixture
  or a Gravity source module must select the complete catalog for its slice,
  while a directly changed test can safely select just its namespace when the
  runner can discover that namespace.
  "
  [relative]
  (boolean
   (re-matches
    #"^bootstrap/clojure/test/gravity/self_hosting/sh\d{2}_.+_test\.clj$"
    relative)))

(defn- dedicated-test-namespace
  "Returns a discovered namespace for a dedicated test path, or nil.

  A path can be syntactically slice-owned after its file has been deleted.
  Callers distinguish that conservative fallback from a present file whose
  namespace discovery failed.
  "
  [catalog relative]
  (when (dedicated-test-path? relative)
    (let [filename (last (str/split relative #"/"))
          namespace
          (-> filename
              (str/replace #"\.clj$" "")
              (str/replace "_" "-")
              (as-> value (symbol (str "gravity.self-hosting." value))))]
      (when (some #(= namespace (:namespace %)) catalog)
        namespace))))

(defn- repository-path-exists?
  [relative]
  (.exists (path relative)))

(defn- unresolved-present-dedicated-test-paths
  [catalog classified]
  (->> classified
       (keep
        (fn [entry]
          (when (and (= :dedicated (:classification entry))
                     (dedicated-test-path? (:path entry))
                     (nil? (dedicated-test-namespace catalog (:path entry)))
                     (repository-path-exists? (:path entry)))
            (:path entry))))
       distinct
       sort
       vec))

(defn- validate-unresolved-present-dedicated-test-paths!
  [catalog classified]
  (let [paths (unresolved-present-dedicated-test-paths catalog classified)]
    (when (seq paths)
      (throw
       (ex-info
        "A present dedicated self-hosting test path is not in the discovered test catalog"
        {:id "SH01-IMPACT-NAMESPACE"
         :paths paths
         :namespaces
         (->> paths
              (map
               (fn [relative]
                 (let [filename (last (str/split relative #"/"))]
                   (-> filename
                       (str/replace #"\.clj$" "")
                       (str/replace "_" "-")
                       (as-> value (symbol (str "gravity.self-hosting." value)))))))
              vec)
         :reason :unresolved-present-test})))))

(defn- canonical-iteration-slices
  [requested]
  (let [values
        (cond
          (nil? requested) []
          (string? requested) [requested]
          (sequential? requested) requested
          (set? requested) requested
          :else [requested])
        values (vec values)
        invalid
        (->> values
             (remove #(and (string? %) (contains? (all-slices) %)))
             distinct
             (sort-by str)
             vec)]
    (when (empty? values)
      (throw
       (ex-info
        "Iteration mode requires at least one explicitly requested slice"
        {:id "SH01-IMPACT-ITERATION-SLICE"
         :slices []})))
    (when (seq invalid)
      (throw
       (ex-info
        "Requested iteration slices must belong to the SH-00 through SH-29 plan"
        {:id "SH01-IMPACT-SLICE"
         :slices invalid})))
    (set values)))

(defn- iteration-deferred-entries
  "Returns changed owned paths that this non-authoritative request does not run.

  Coordinator paths are always deferred. Leaf fixture/module paths are only
  run when their slice was explicitly requested. A directly changed test is a
  safe exception: when its namespace is discoverable, only that namespace is
  run even if its slice was not explicitly requested.
  "
  [classified requested catalog]
  (let [catalog-test-namespaces
        (set (map :namespace catalog))]
    (->> classified
         (keep
          (fn [entry]
            (let [classification (:classification entry)
                  slices (set (:slices entry))
                  test-namespace
                  (when (= :dedicated classification)
                    (dedicated-test-namespace catalog (:path entry)))
                  explicitly-selected?
                  (seq (set/intersection requested slices))]
              (cond
                (= :coordinator classification)
                entry

                (and (= :dedicated classification)
                     (dedicated-test-path? (:path entry))
                     (contains? catalog-test-namespaces test-namespace))
                nil

                explicitly-selected?
                nil

                (#{:dedicated :module} classification)
                entry

                :else
                nil))))
         vec)))

(defn- build-iteration-plan
  [context classified changed-paths requested]
  (let [catalog (validate-test-catalog
                 (context-catalog context)
                 (:standalone-test-index context))
        requested (canonical-iteration-slices requested)
        _ (validate-unresolved-present-dedicated-test-paths!
           catalog
           classified)
        direct-test-entries
        (filter
         #(and (= :dedicated (:classification %))
               (dedicated-test-path? (:path %)))
         classified)
        exact-test-namespaces
        (->> direct-test-entries
             (keep #(dedicated-test-namespace catalog (:path %)))
             set)
        test-path-slices
        (->> direct-test-entries
             (filter
              #(or
                (seq (set/intersection requested (set (:slices %))))
                (dedicated-test-namespace catalog (:path %))
                (not (repository-path-exists? (:path %)))))
             (mapcat :slices)
             set)
        deleted-test-slices
        (->> direct-test-entries
             (filter
              #(and (nil? (dedicated-test-namespace catalog (:path %)))
                    (not (repository-path-exists? (:path %)))))
             (mapcat :slices)
             set)
        catalog-selection-slices
        (set/union requested deleted-test-slices)
        selected-slices
        (set/union requested test-path-slices)
        changed-path-slices
        (into #{} (mapcat :slices) classified)
        invalid-slices
        (set/difference
         (set/union selected-slices changed-path-slices)
         (all-slices))
        deferred
        (iteration-deferred-entries classified requested catalog)
        deferred-coordinator
        (->> deferred
             (filter #(= :coordinator (:classification %)))
             (map :path)
             sort
             vec)
        deferred-other
        (->> deferred
             (remove #(= :coordinator (:classification %)))
             (map :path)
             sort
             vec)
        namespaces
        (->> catalog
             (filter #(contains? catalog-selection-slices (:slice %)))
             (map :namespace)
             (concat exact-test-namespaces)
             distinct
             sort
             vec)
        catalog-by-namespace
        (into {} (map (juxt :namespace :slice)) catalog)
        shards
        (->> namespaces
             (mapv
              (fn [namespace]
                (let [slice (get catalog-by-namespace namespace)]
                  {:namespace namespace
                   :slice slice
                   :resource-class (resource-class slice)}))))]
    (when (seq invalid-slices)
      (throw
       (ex-info
        "Requested slices must belong to the SH-00 through SH-29 plan"
        {:id "SH01-IMPACT-SLICE"
         :slices (vec (sort invalid-slices))})))
    (merge
     {:schema :gravity/sh01-impact-test-plan-v1
      :mode :iteration
      :authority :non-authoritative
      :non-authoritative? true
      :authoritative? false
      :iteration? true
      :iteration-slices (vec (sort requested))
      :full-gate-deferred? true
      :full-gate-deferred-reason
      "The authoritative dependency-expanded gate is deferred for slice iteration feedback."
      :deferred-coordinator-paths deferred-coordinator
      :deferred-other-affected-paths deferred-other
      :deferred-paths (vec (concat deferred-coordinator deferred-other))
      :direct-slices (vec (sort selected-slices))
      :affected-slices (vec (sort selected-slices))
      :changed-paths changed-paths
      :classifications classified
      :namespaces namespaces
      :shards shards
      :ignored-paths
      (->> classified
           (filter #(= :unrelated (:classification %)))
           (mapv :path))}
     (component-plan-fields classified))))

(defn- planner-context-for-request
  [request]
  (or (:planning-context request)
      (:context request)
      (planning-context)))

(defn- validate-exact-test-var-route-owner!
  "Admit an exact route only when its owner is live and unambiguous.

  The route catalog is reviewed code, while the slice owner is mutable
  coordinator state.  Keeping both identities in the admission check prevents
  a stale route from selecting a leaf after ownership moves.  The owner entry
  is intentionally closed as well: an extra or malformed owner field cannot
  be silently ignored by the route planner.
  "
  [route context]
  (let [ownership (when (map? context) (:ownership context))
        slice-owners (when (map? ownership) (:slice-owners ownership))
        slice (:slice route)
        live-entry (when (map? slice-owners) (get slice-owners slice))
        live-owner (when (map? live-entry) (:leaf-owner live-entry))
        live-entry-keys (when (map? live-entry) (set (keys live-entry)))
        expected-entry-keys #{:integration-owner :leaf-owner}]
    (cond
      (not (map? context))
      (throw
       (ex-info
        "Exact test-var route planning requires one planner context"
        {:id "SH01-IMPACT-ROUTE-OWNER"
         :route-path (:path route)
         :reason :context-missing}))

      (not (map? ownership))
      (throw
       (ex-info
        "Exact test-var route planning requires the live ownership record"
        {:id "SH01-IMPACT-ROUTE-OWNER"
         :slice slice
         :reason :ownership-missing}))

      (not (map? slice-owners))
      (throw
       (ex-info
        "Exact test-var route planning requires live slice owners"
        {:id "SH01-IMPACT-ROUTE-OWNER"
         :slice slice
         :reason :slice-owners-malformed}))

      (not (map? live-entry))
      (throw
       (ex-info
        "Exact test-var route has no live owner entry for its slice"
        {:id "SH01-IMPACT-ROUTE-OWNER"
         :slice slice
         :reason :slice-owner-missing
         :slice-owners (vec (sort-by str (keys slice-owners)))}))

      (not= expected-entry-keys live-entry-keys)
      (throw
       (ex-info
        "Exact test-var route live owner entry has an unexpected shape"
        {:id "SH01-IMPACT-ROUTE-OWNER"
         :slice slice
         :reason :slice-owner-shape
         :expected-keys expected-entry-keys
         :actual-keys live-entry-keys}))

      (not (and (keyword? (:integration-owner live-entry))
                (keyword? live-owner)))
      (throw
       (ex-info
        "Exact test-var route live owner entry is malformed"
        {:id "SH01-IMPACT-ROUTE-OWNER"
         :slice slice
         :reason :slice-owner-malformed
         :live-entry live-entry}))

      (not= (:owner route) live-owner)
      (throw
       (ex-info
        "Exact test-var route owner disagrees with the live slice owner"
        {:id "SH01-IMPACT-ROUTE-OWNER"
         :slice slice
         :route-owner (:owner route)
         :live-owner live-owner
         :reason :owner-drift}))

      :else
      route)))

(defn- validate-exact-test-var-route-dependency!
  "Require the route path and namespace to resolve to one catalog identity.

  This is the route-specific counterpart to the ordinary planner's dependency
  checks.  It runs against the same request-local catalog that build-plan will
  consume, so a stale or substituted test entry cannot be admitted as an exact
  route merely because its path still looks slice-shaped.
  "
  [route context]
  (let [catalog (validate-test-catalog
                 (context-catalog context)
                 (:standalone-test-index context))
        catalog-namespace (dedicated-test-namespace catalog (:path route))]
    (when-not (and (= (:slice route) (path-slice (:path route)))
                   (= (:namespace route) catalog-namespace))
      (throw
       (ex-info
        "Exact test-var route path and namespace disagree with the test dependency catalog"
        {:id "SH01-IMPACT-ROUTE-CONTRACT"
         :route-path (:path route)
         :route-slice (:slice route)
         :route-namespace (:namespace route)
         :catalog-namespace catalog-namespace
         :reason :dependency-identity})))
    route))

(defn- invalid-slices!
  [slices]
  (let [invalid (set/difference (set slices) (all-slices))]
    (when (seq invalid)
      (throw
       (ex-info
        "Requested slices must belong to the SH-00 through SH-29 plan"
        {:id "SH01-IMPACT-SLICE"
         :slices (vec (sort-by str invalid))}))))
  slices)

(defn- unowned-paths!
  [classified]
  (let [unowned
        (->> classified
             (filter #(= :unowned (:classification %)))
             (mapv :path))]
    (when (seq unowned)
      (throw
       (ex-info
        "Changed self-hosting paths must have exactly one SH-01 owner"
        {:id "SH01-IMPACT-UNOWNED"
         :paths unowned})))
    unowned))

(defn build-plan
  "Builds a deterministic plan for directly selected slices or changed paths.

  All coordinator inputs belong to one request-local context.  Slice and
  ownership failures are checked before forcing the catalog delay so malformed
  requests cannot trigger filesystem discovery.
  "
  [request]
  (let [request (or request {})
        direct-slices (set (or (:direct-slices request) #{}))
        changed-paths (->> (or (:changed-paths request) []) distinct sort vec)
        expand-dependants? (boolean (:expand-dependants? request))
        iteration-request?
        (or (contains? request :iteration-slices)
            (contains? request :iteration-slice))
        iteration-slices
        (if (contains? request :iteration-slices)
          (:iteration-slices request)
          (:iteration-slice request))
        context (planner-context-for-request request)
        record (:ownership context)
        dependencies (:dependencies context)
        classified (mapv #(classify-path record %) changed-paths)
        path-slices (into #{} (mapcat :slices classified))
        direct (set/union direct-slices path-slices)
        _ (invalid-slices! direct)
        unowned (unowned-paths! classified)]
    (if iteration-request?
      (let [iteration-slices (canonical-iteration-slices iteration-slices)]
        (build-iteration-plan
         context
         classified
         changed-paths
         iteration-slices))
      (let [selected
            (if expand-dependants?
              (downstream-closure dependencies direct)
              direct)
            catalog (validate-test-catalog
                     (context-catalog context)
                     (:standalone-test-index context))
            _ (validate-unresolved-present-dedicated-test-paths!
               catalog
               classified)
            classified
            (enrich-development-selections
             (:component-dependencies context)
             catalog
             classified)
            exact-namespaces
            (exact-development-namespaces classified)
            job-metadata-by-namespace
            (exact-development-job-metadata record classified)
            execution-path-slices
            (->> classified
                 (filter #(= :slice-closure
                             (:development-selection %)))
                 (mapcat :slices)
                 set)
            execution-direct
            (set/union direct-slices execution-path-slices)
            execution-selected
            (if expand-dependants?
              (downstream-closure dependencies execution-direct)
              execution-direct)
            namespaces
            (->> catalog
                 (filter #(contains? execution-selected (:slice %)))
                 (map :namespace)
                 (concat exact-namespaces)
                 distinct
                 sort
                 vec)
            catalog-by-namespace
            (into {} (map (juxt :namespace :slice)) catalog)
            shards
            (->> namespaces
                 (mapv
                  (fn [namespace]
                    (let [slice (get catalog-by-namespace namespace)]
                      (cond->
                       {:namespace namespace
                        :slice slice
                        :resource-class (resource-class slice)}
                        (contains? job-metadata-by-namespace namespace)
                        (merge (get job-metadata-by-namespace namespace)))))))]
        (merge
         (cond->
          {:schema :gravity/sh01-impact-test-plan-v1
          :direct-slices (vec (sort direct))
          :affected-slices (vec (sort selected))
          :execution-direct-slices (vec (sort execution-direct))
          :execution-affected-slices (vec (sort execution-selected))
          :changed-paths changed-paths
          :classifications classified
          :namespaces namespaces
          :development-selected-namespaces namespaces
          :shards shards
          :ignored-paths
          (->> classified
               (filter #(= :unrelated (:classification %)))
               (mapv :path))}
           (seq exact-namespaces)
           (assoc :authority :non-authoritative
                  :authoritative? false
                  :non-authoritative? true
                  :selection-mode :exact-development-tests))
         (component-plan-fields classified))))))

(defn build-test-var-plan
  "Builds a closed, non-authoritative plan for one exact test-var route.

  The route owns one complete dedicated test path but narrows development
  execution to its reviewed vars.  A caller may provide the exact changed path
  (for base-aware planning); any additional or mixed selection input fails
  closed before ordinary slice planning is attempted.  The returned plan
  retains the normal SH-01 slice/dependency metadata and adds the exact vars
  that a bounded runner may execute in one JVM.
  "
  ([route-name]
   (build-test-var-plan route-name {}))
  ([route-name options]
   (let [route (exact-test-var-route route-name)
         options (or options {})
         allowed #{:context :changed-paths}
         extra (vec (sort (map str (set/difference (set (keys options))
                                                   allowed))))
         changed-paths
         (if (contains? options :changed-paths)
           (->> (or (:changed-paths options) []) distinct sort vec)
           [(:path route)])]
     (when (seq extra)
       (throw
        (ex-info
         "Exact test-var routes do not accept mixed or extra selections"
         {:id "SH01-IMPACT-ROUTE-SELECTION"
          :route route-name
          :extra-options extra})))
     (when-not (= [(:path route)] changed-paths)
       (throw
        (ex-info
         "Exact test-var route changed paths must match its owned path exactly"
         {:id "SH01-IMPACT-ROUTE-PATH"
          :route route-name
          :expected [(:path route)]
          :changed-paths changed-paths})))
     (let [context (if (contains? options :context)
                     (:context options)
                     (planning-context))]
       ;; Validate the live ownership identity before the normal planner can
       ;; admit the path.  The same request-local context is then passed into
       ;; build-plan so ownership, catalog, and dependency identities cannot
       ;; change between admission and plan construction.
       (validate-exact-test-var-route-owner! route context)
       (validate-exact-test-var-route-dependency! route context)
       (let [plan (build-plan {:changed-paths changed-paths
                               :expand-dependants? false
                               :context context})
             classification (first (:classifications plan))
             exact-dependency?
             (and (= 1 (count (:classifications plan)))
                  (= (:path route) (:path classification))
                  (= :dedicated (:classification classification))
                  (= [(:slice route)] (:slices classification))
                  (= :exact-test-path
                     (:development-selection classification))
                  (= [(:namespace route)]
                     (:development-test-namespaces classification)))]
         (when-not (and (= [(:namespace route)] (:namespaces plan))
                        exact-dependency?)
           (throw
            (ex-info
             "Exact test-var route identity disagrees with SH-01 ownership or dependency planning"
             {:id "SH01-IMPACT-ROUTE-CONTRACT"
              :route route-name
              :expected {:path (:path route)
                         :slice (:slice route)
                         :namespace (:namespace route)
                         :classification :dedicated
                         :development-selection :exact-test-path}
              :actual {:namespaces (:namespaces plan)
                       :classification classification}})))
         (assoc plan
                :selection-mode :exact-test-vars
                :route route-name
                :route-owner (:owner route)
                :route-path (:path route)
                :test-vars (:test-vars route)
                :test-var-count (count (:test-vars route))
                :resource-class (:resource-class route)
                :route-authority :non-authoritative
                :authoritative? false
                :non-authoritative? true))))))

(defn build-namespace-plan
  "Builds a non-authoritative plan for exact dedicated test namespaces.

  This leaf iteration mode validates names against the same discovered catalog
  as slice planning but deliberately does not expand to sibling namespaces or
  downstream slices."
  ([requested]
   (build-namespace-plan requested (planning-context)))
  ([requested context]
   (let [namespaces (mapv #(if (symbol? %) % (symbol (str %))) requested)]
     (when (empty? namespaces)
       (throw
        (ex-info "At least one dedicated namespace is required"
                 {:id "SH01-IMPACT-NAMESPACE-EMPTY"})))
     (when-not (= (count namespaces) (count (distinct namespaces)))
       (throw
        (ex-info "Dedicated namespaces must be unique"
                 {:id "SH01-IMPACT-NAMESPACE-DUPLICATE"
                  :namespaces namespaces})))
     ;; A slice encoded in a requested namespace is a syntactic input and can
     ;; be rejected without discovering the catalog.  Namespaces without an
     ;; encoded slice still require catalog lookup so their ownership can be
     ;; checked normally.
     (let [invalid-requested-slices
           (->> namespaces
                (keep
                 (fn [namespace]
                   (let [slice (namespace-slice namespace)]
                     (when (and slice (not (valid-slice? slice)))
                       namespace))))
                sort
                vec)]
       (when (seq invalid-requested-slices)
         (throw
          (ex-info
           "Requested namespace has no valid SH-00 through SH-29 slice"
           {:id "SH01-IMPACT-NAMESPACE-SLICE"
            :namespaces invalid-requested-slices}))))
     (let [catalog (validate-test-catalog
                    (context-catalog context)
                    (:standalone-test-index context))
           by-namespace (into {} (map (juxt :namespace identity)) catalog)
           unknown (vec (sort (remove #(contains? by-namespace %) namespaces)))
           invalid-slice-namespaces
           (->> namespaces
                (filter
                 (fn [namespace]
                   (let [slice (:slice (get by-namespace namespace))]
                     (and (contains? by-namespace namespace)
                          (not (valid-slice? slice))))))
                sort
                vec)]
       (when (seq unknown)
         (throw
          (ex-info "Requested namespace is not in the dedicated test catalog"
                   {:id "SH01-IMPACT-NAMESPACE"
                    :namespaces unknown})))
       (when (seq invalid-slice-namespaces)
         (throw
          (ex-info "Requested namespace has no valid SH-00 through SH-29 slice"
                   {:id "SH01-IMPACT-NAMESPACE-SLICE"
                    :namespaces invalid-slice-namespaces})))
       (let [namespaces (vec (sort namespaces))
             shards
             (mapv
              (fn [namespace]
                (let [slice (:slice (get by-namespace namespace))]
                  {:namespace namespace
                   :slice slice
                   :resource-class (resource-class slice)}))
              namespaces)
             slices (vec (sort (distinct (map :slice shards))))]
         {:schema :gravity/sh01-impact-test-plan-v1
          :authority :non-authoritative
          :authoritative? false
          :selection-mode :exact-namespaces
          :direct-slices slices
          :affected-slices slices
          :changed-paths []
          :classifications []
          :namespaces namespaces
          :shards shards
          :ignored-paths []})))))

(defn- process-lines-in
  [working-directory & command]
  (let [working-directory
        (if (instance? java.nio.file.Path working-directory)
          (.toFile ^java.nio.file.Path working-directory)
          (io/file working-directory))
        process
        (-> (ProcessBuilder. ^java.util.List (vec command))
            (.directory working-directory)
            (.redirectErrorStream true)
            .start)
        output (slurp (.getInputStream process))
        exit-code (.waitFor process)]
    (when-not (zero? exit-code)
      (throw
       (ex-info
        "Repository change discovery failed"
        {:id "SH01-IMPACT-GIT"
         :command command
         :exit exit-code
         :output output})))
    (remove str/blank? (str/split-lines output))))

(defn- process-lines
  [& command]
  (apply process-lines-in (path ".") command))

(defn- one-git-commit
  [kind values details]
  (let [values (vec values)]
    (when-not (and (= 1 (count values))
                   (re-matches #"[0-9a-f]{40}" (first values)))
      (throw
       (ex-info
        "Repository base discovery did not produce one commit"
        (merge {:id "SH01-IMPACT-BASE"
                :kind kind
                :values values}
               details))))
    (first values)))

(defn change-discovery
  "Returns deterministic committed and working change discovery metadata.

  With an explicit base ref, committed paths are read from
  merge-base(base, HEAD)..HEAD. Tracked and untracked working paths are always
  included. The three sources remain separate in the result and their union is
  returned under `:changed-paths`."
  ([]
   (change-discovery nil (path ".")))
  ([base-ref]
   (change-discovery base-ref (path ".")))
  ([base-ref working-directory]
   (when (and (some? base-ref)
              (or (not (string? base-ref))
                  (str/blank? base-ref)
                  (some #(Character/isISOControl (int %)) base-ref)))
     (throw
      (ex-info
       "Incremental base ref must be one nonempty line"
       {:id "SH01-IMPACT-BASE" :base-ref base-ref})))
   (let [run #(apply process-lines-in working-directory %)
         base-commit
         (when base-ref
           (try
             (one-git-commit
              :base-commit
              (run ["git" "rev-parse" "--verify" "--end-of-options"
                    (str base-ref "^{commit}")])
              {:base-ref base-ref})
             (catch clojure.lang.ExceptionInfo exception
               (throw
                (ex-info
                 "Incremental base ref could not be resolved"
                 {:id "SH01-IMPACT-BASE"
                  :base-ref base-ref
                  :cause-id (:id (ex-data exception))}
                 exception)))))
         merge-base
         (when base-commit
           (try
             (one-git-commit
              :merge-base
              (run ["git" "merge-base" base-commit "HEAD"])
              {:base-ref base-ref :base-commit base-commit})
             (catch clojure.lang.ExceptionInfo exception
               (throw
                (ex-info
                 "Incremental base ref has no usable merge base with HEAD"
                 {:id "SH01-IMPACT-BASE"
                  :base-ref base-ref
                  :base-commit base-commit
                  :cause-id (:id (ex-data exception))}
                 exception)))))
         committed-paths
         (if merge-base
           (vec (run ["git" "diff" "--name-only" merge-base "HEAD"]))
           [])
         tracked-working-paths
         (vec (run ["git" "diff" "--name-only" "HEAD"]))
         untracked-paths
         (vec (run ["git" "ls-files" "--others" "--exclude-standard"]))
         changed-paths
         (->> (concat committed-paths
                      tracked-working-paths
                      untracked-paths)
              distinct
              sort
              vec)]
     {:schema :gravity/sh01-change-discovery-v1
      :authority :non-authoritative
      :authoritative? false
      :base-ref base-ref
      :base-commit base-commit
      :merge-base merge-base
      :committed-paths (vec (sort (distinct committed-paths)))
      :tracked-working-paths
      (vec (sort (distinct tracked-working-paths)))
      :untracked-paths (vec (sort (distinct untracked-paths)))
      :changed-paths changed-paths})))

(defn changed-paths
  "Returns committed branch plus tracked/untracked working paths when based."
  ([] (:changed-paths (change-discovery)))
  ([base-ref] (:changed-paths (change-discovery base-ref)))
  ([base-ref working-directory]
   (:changed-paths (change-discovery base-ref working-directory))))

(defn- owner-slices
  [owner]
  (let [record (ownership-record)
        owner-keyword (keyword owner)]
    (set
     (for [[slice ownership] (:slice-owners record)
           :when (= owner-keyword (:leaf-owner ownership))]
       slice))))

(defn- iteration-arguments
  [arguments plan-only?]
  (loop [remaining arguments
         changed? false
         slices []]
    (if (empty? remaining)
      (do
        (when-not changed?
          (throw
           (ex-info
            "Iteration mode requires --changed"
            {:id "SH01-IMPACT-USAGE"
             :arguments arguments})))
        (when (empty? slices)
          (throw
           (ex-info
            "Iteration mode requires at least one --iteration-slice value"
            {:id "SH01-IMPACT-ITERATION-SLICE"
             :arguments arguments})))
        {:plan-only? plan-only?
         :request {:changed-paths (changed-paths)
                   :iteration-slices (set slices)}})
      (let [argument (first remaining)]
        (cond
          (= "--changed" argument)
          (do
            (when changed?
              (throw
               (ex-info
                "--changed may only be provided once"
                {:id "SH01-IMPACT-USAGE"
                 :arguments arguments})))
            (recur (next remaining) true slices))

          (= "--iteration-slice" argument)
          (let [value (second remaining)]
            (when (or (nil? value) (str/starts-with? value "--"))
              (throw
               (ex-info
                "--iteration-slice requires a slice id"
                {:id "SH01-IMPACT-ITERATION-SLICE"
                 :arguments arguments})))
            (recur (nnext remaining) changed? (conj slices value)))

          :else
          (throw
           (ex-info
            "Unsupported SH-01 iteration planner arguments"
            {:id "SH01-IMPACT-USAGE"
             :arguments arguments})))))))

(defn- parse-arguments
  [arguments]
  (let [arguments (vec arguments)
        plan-only? (boolean (some #{"--plan"} arguments))
        args (vec (remove #{"--plan"} arguments))]
    (if (some #{"--iteration-slice"} args)
      (iteration-arguments args plan-only?)
      (cond
      (= ["--changed"] args)
      {:plan-only? plan-only?
       :request {:changed-paths (changed-paths)
                 :expand-dependants? true}}

      (and (= 2 (count args)) (= "--slice" (first args)))
      {:plan-only? plan-only?
       :request {:direct-slices #{(second args)}}}

      (and (= 2 (count args)) (= "--owner" (first args)))
      (let [slices (owner-slices (second args))]
        (when (empty? slices)
          (throw
           (ex-info
            "Requested leaf owner does not own a self-hosting slice"
            {:id "SH01-IMPACT-OWNER"
             :owner (second args)})))
        {:plan-only? plan-only?
         :request {:direct-slices slices}})

      :else
      (throw
       (ex-info
        "Unsupported SH-01 impact planner arguments"
        {:id "SH01-IMPACT-USAGE"
         :arguments arguments
         :supported
         [["--changed" "--plan"]
          ["--changed" "--iteration-slice" "SH-NN" "--plan"]
          ["--slice" "SH-NN" "--plan"]
          ["--owner" "leaf-owner" "--plan"]]}))))))

(defn- run-plan
  [plan]
  (let [namespaces (:namespaces plan)]
    (doseq [namespace namespaces]
      (require namespace))
    (let [result (apply test/run-tests namespaces)]
      (when (or (pos? (:fail result)) (pos? (:error result)))
        (System/exit 1))
      result)))

(defn -main
  [& arguments]
  (let [{:keys [plan-only? request]} (parse-arguments arguments)
        plan (build-plan request)]
    (prn plan)
    (when-not plan-only?
      (let [result (run-plan plan)]
        (println
         (str
          "SH-01 impact validation passed: "
          (:test result) " tests, "
          (:pass result) " assertions, "
          (count (:namespaces plan)) " namespaces"))))))
