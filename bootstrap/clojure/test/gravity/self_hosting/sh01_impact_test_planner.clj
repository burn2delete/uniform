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
            [gravity.self-hosting-test-runner :as runner]))

(def ^:private slice-count 30)

;; These reviewed namespaces are verification infrastructure.  The general
;; self-hosting runner discovers and can execute them, but they are not SH-00
;; through SH-29 leaf tests and therefore must never enter the slice planner's
;; catalog (or be inferred as SH-07 by a broad fallback).
(def ^:private reviewed-non-slice-namespaces
  #{'gravity.self-hosting.a1-canonical-schema-test
    'gravity.self-hosting.p15-public-native-admission-test
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
    'gravity.self-hosting.w5-typed-effect-safety-executor-test})

(def ^:private required-fixed-stage-infrastructure-namespaces
  #{'gravity.self-hosting.stage3-fragment-size-preflight-test
    'gravity.self-hosting.stage3-verification-runner-test})

(def ^:private component-cross-cutting-reason
  :reserved-component-owner-has-no-slice)

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
  (let [module-owner (get (:module-owners record) relative)
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
  []
  (let [namespaces (vec (runner/dedicated-test-namespaces))
        duplicates (duplicate-namespaces namespaces)
        discovered (set namespaces)
        missing-infrastructure
        (set/difference required-fixed-stage-infrastructure-namespaces
                        discovered)
        unknown-non-slice
        (->> namespaces
             (remove
              #(or (contains? reviewed-non-slice-namespaces %)
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
             :slice (namespace-slice namespace)}))
         vec)))

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
  [catalog]
  (let [catalog (when (some? catalog) (vec catalog))
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

                    (not= (:slice entry) (namespace-slice (:namespace entry)))
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
    catalog))

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
   (let [options (or options {})]
     {:ownership
      (if (contains? options :ownership)
        (:ownership options)
        (ownership-record))
      :dependencies
      (if (contains? options :dependencies)
        (:dependencies options)
        (backlog-dependencies))
      :catalog-delay
      (delay
       (if (contains? options :catalog)
         (:catalog options)
         (test-catalog)))})))

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

(defn- resource-class
  [slice]
  (cond
    (= "SH-07" slice) :memory-heavy
    (contains? #{"SH-26" "SH-27" "SH-28" "SH-29"} slice) :exclusive
    :else :normal))

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
  (let [catalog (-> context context-catalog validate-test-catalog)
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
        shards
        (->> namespaces
             (mapv
              (fn [namespace]
                (let [slice (namespace-slice namespace)]
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
            catalog (-> context context-catalog validate-test-catalog)
            _ (validate-unresolved-present-dedicated-test-paths!
               catalog
               classified)
            namespaces
            (->> catalog
                 (filter #(contains? selected (:slice %)))
                 (mapv :namespace)
                 sort
                 vec)
            shards
            (->> namespaces
                 (mapv
                  (fn [namespace]
                    (let [slice (namespace-slice namespace)]
                      {:namespace namespace
                       :slice slice
                       :resource-class (resource-class slice)}))))]
        (merge
         {:schema :gravity/sh01-impact-test-plan-v1
          :direct-slices (vec (sort direct))
          :affected-slices (vec (sort selected))
          :changed-paths changed-paths
          :classifications classified
          :namespaces namespaces
          :shards shards
          :ignored-paths
          (->> classified
              (filter #(= :unrelated (:classification %)))
              (mapv :path))}
         (component-plan-fields classified))))))

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
     (let [catalog (-> context context-catalog validate-test-catalog)
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

(defn- process-lines
  [& command]
  (let [process
        (-> (ProcessBuilder. ^java.util.List (vec command))
            (.directory (path "."))
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

(defn changed-paths
  "Returns tracked and untracked working-tree paths in deterministic order."
  []
  (->> (concat
        (process-lines "git" "diff" "--name-only" "HEAD")
        (process-lines
         "git" "ls-files" "--others" "--exclude-standard"))
       distinct
       sort
       vec))

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
