(ns gravity.self-hosting.sh07-iteration-cache-runner
  "Runs focused SH-07 namespaces with bounded process-local caches.

  This runner is iteration acceleration only. Its result is always
  non-authoritative, and a fresh authoritative run remains required."
  (:require [clojure.test :as test]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-semantic-carrier :as semantic-carrier]
            [gravity.self-hosting-test-runner :as test-runner]))

(def ^:private default-maximum-entries 4)

(def ^:private empty-cache-counters
  {:sh06-hits 0 :sh06-misses 0
   :core-hits 0 :core-misses 0
   :verification-hits 0 :verification-misses 0
   :semantic-hits 0 :semantic-misses 0 :semantic-joined 0
   :semantic-published 0 :semantic-evictions 0
   :semantic-failed-producers 0 :semantic-rejected-hits 0})

(def ^:dynamic ^:private *iteration-cache-counters* nil)

(def ^:private c10-semantic-test-vars
  #{'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-has-exact-authentic-coverage
    'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-calls-lookups-and-quotes-are-exact
    'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-is-deterministic-path-neutral-and-provenanced
    'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-replay-and-high-value-alterations-are-contained})

(defn- exact-c10-semantic-selection?
  [selection]
  (let [units (:units selection)]
    (and (= :test-vars (:mode selection))
         (= (count c10-semantic-test-vars) (count units))
         (= c10-semantic-test-vars (set units)))))

(deftype ^:private IdentityKey [value]
  Object
  (equals [_ other]
    (and (instance? IdentityKey other)
         (identical? value (.-value ^IdentityKey other))))
  (hashCode [_]
    (System/identityHashCode value)))

(defn- identity-key
  [value]
  (IdentityKey. value))

(defn- sha256
  [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest
             (.getBytes (str value)
                        java.nio.charset.StandardCharsets/UTF_8))
    (apply str
           (map #(format "%02x" (bit-and % 0xff))
                (.digest digest)))))

(defn- cache-context
  []
  {:request-schema-version 15
   :adapter bootstrap/sh07-core-adapter-contract
   :checked-core-source-content-hash
   bootstrap/sh07-core-expected-source-content-hash
   :checked-core-plan-semantic-hash
   bootstrap/sh07-core-expected-plan-semantic-hash
   :java-runtime-version (System/getProperty "java.runtime.version")
   :clojure-version (clojure-version)})

(defn- resolution-key
  [context artifact]
  [context
   (identity-key artifact)
   (:artifact-id artifact)
   (get-in artifact [:provenance :source-path])
   (get-in artifact
           [:gravity-resolution-boundary :resolved-analysis
            :semantic-projection-id])])

(defn- bounded-cache
  [maximum-entries counters hit-key miss-key]
  (let [state (atom {:order [] :values {} :flights {}})
        monitor (Object.)]
    (fn [key operation]
      (let [thread (Thread/currentThread)
            decision
            (locking monitor
              (if-let [entry (find (:values @state) key)]
                (do (swap! counters update hit-key inc)
                    [:hit (val entry)])
                (if-let [flight (get-in @state [:flights key])]
                  (if (identical? thread (:producer flight))
                    [:reentrant nil]
                    (do (swap! counters update hit-key inc)
                        [:wait (:result flight)]))
                  (let [result (promise)]
                    (swap! state assoc-in [:flights key]
                           {:producer thread :result result})
                    (swap! counters update miss-key inc)
                    [:produce result]))))]
        (case (first decision)
          :hit (second decision)
          :reentrant
          (throw
           (ex-info "Iteration cache operation reentered its own key"
                    {:id "SH07-ITERATION-CACHE-REENTRANT"}))
          :wait
          (let [{:keys [value error]} @(second decision)]
            (if error (throw error) value))
          :produce
          (let [result (second decision)]
            (try
              (let [value (operation)]
                (locking monitor
                  (let [{:keys [order values flights]} @state
                        order' (conj order key)
                        values' (assoc values key value)
                        overflow (- (count order') maximum-entries)
                        evicted (if (pos? overflow)
                                  (subvec order' 0 overflow)
                                  [])
                        retained-order (if (pos? overflow)
                                         (subvec order' overflow)
                                         order')]
                    (reset! state
                            {:order retained-order
                             :values (apply dissoc values' evicted)
                             :flights (dissoc flights key)})))
                (deliver result {:value value})
                value)
              (catch InterruptedException interrupted
                (locking monitor
                  (swap! state update :flights dissoc key))
                (deliver result {:error interrupted})
                (.interrupt thread)
                (throw interrupted))
              (catch Throwable error
                (locking monitor
                  (swap! state update :flights dissoc key))
                (deliver result {:error error})
                (throw error)))))))))

(defn- semantic-diagnostic!
  [reason source-path]
  (throw
   (ex-info "SH07 semantic cache rejected"
            {:artifact :gravity/sh07-semantic-cache-diagnostic
             :rule "SH07-SEMANTIC-CACHE" :severity :error
             :stage :development-cache :reason reason
             :source-path source-path :fail-closed true
             :fresh-authoritative-run-required? true})))

(defn- bounded-semantic-cache
  [maximum-entries maximum-bytes counters]
  (let [state (atom {:order [] :entries {} :bytes 0 :flights {}})
        monitor (Object.)]
    (fn [key operation]
      (let [thread (Thread/currentThread)
            decision
            (locking monitor
              (if-let [entry (get-in @state [:entries key])]
                (do (swap! counters update :semantic-hits inc)
                    [:hit (:value entry)])
                (if-let [flight (get-in @state [:flights key])]
                  (if (identical? thread (:producer flight))
                    [:reentrant nil]
                    (do (swap! counters update :semantic-joined inc)
                        [:wait (:result flight)]))
                  (let [result (promise)]
                    (swap! state assoc-in [:flights key]
                           {:producer thread :result result})
                    (swap! counters update :semantic-misses inc)
                    [:produce result]))))]
        (case (first decision)
          :hit (second decision)
          :reentrant (semantic-diagnostic! :reentrant-request nil)
          :wait
          (try
            (let [{:keys [value error]} @(second decision)]
              (if error (throw error) value))
            (catch InterruptedException interrupted
              (.interrupt thread)
              (throw interrupted)))
          :produce
          (let [result (second decision)]
            (try
              (let [value (operation)
                    weight (semantic-carrier/carrier-byte-count value)]
                (when (> weight maximum-bytes)
                  (semantic-diagnostic! :carrier-too-large nil))
                (locking monitor
                  (let [{:keys [order entries bytes flights]} @state
                        entries' (assoc entries key {:value value :bytes weight})
                        order' (conj order key)
                        bytes' (+ bytes weight)
                        [retained-order retained-entries retained-bytes evictions]
                        (loop [order order' entries entries' bytes bytes' evictions 0]
                          (if (or (> (count order) maximum-entries)
                                  (> bytes maximum-bytes))
                            (let [evicted (first order)
                                  removed (get entries evicted)]
                              (recur (subvec order 1)
                                     (dissoc entries evicted)
                                     (- bytes (:bytes removed))
                                     (inc evictions)))
                            [order entries bytes evictions]))]
                    (reset! state {:order retained-order
                                   :entries retained-entries
                                   :bytes retained-bytes
                                   :flights (dissoc flights key)})
                    (swap! counters update :semantic-published inc)
                    (swap! counters update :semantic-evictions + evictions)))
                (deliver result {:value value})
                value)
              (catch InterruptedException interrupted
                (locking monitor (swap! state update :flights dissoc key))
                (swap! counters update :semantic-failed-producers inc)
                (deliver result {:error interrupted})
                (.interrupt thread)
                (throw interrupted))
              (catch Throwable error
                (locking monitor (swap! state update :flights dissoc key))
                (swap! counters update :semantic-failed-producers inc)
                (deliver result {:error error})
                (throw error)))))))))

(defn- strict-source-snapshot
  [path source resolution]
  (let [resolved (java.nio.file.Paths/get path (make-array String 0))
        options (into-array java.nio.file.LinkOption
                            [java.nio.file.LinkOption/NOFOLLOW_LINKS])]
    (when (java.nio.file.Files/isSymbolicLink resolved)
      (semantic-diagnostic! :source-symlink path))
    (when-not (java.nio.file.Files/isRegularFile resolved options)
      (semantic-diagnostic! :source-not-regular path))
    (let [before (java.nio.file.Files/readAttributes
                  resolved java.nio.file.attribute.BasicFileAttributes options)
          bytes (java.nio.file.Files/readAllBytes resolved)
          after (java.nio.file.Files/readAttributes
                 resolved java.nio.file.attribute.BasicFileAttributes options)
          supplied (.getBytes ^String source java.nio.charset.StandardCharsets/UTF_8)
          extension (cond
                      (.endsWith path ".gravity") ".gravity"
                      (.endsWith path ".qst") ".qst"
                      :else nil)]
      (when (> (alength bytes) (* 16 1024 1024))
        (semantic-diagnostic! :source-too-large path))
      (when-not (and (= (.size before) (.size after))
                     (= (.lastModifiedTime before) (.lastModifiedTime after))
                     (= (.fileKey before) (.fileKey after))
                     (java.util.Arrays/equals bytes supplied))
        (semantic-diagnostic! :source-mutated path))
      (when-not extension (semantic-diagnostic! :invalid-extension path))
      {:source-path path :source-bytes bytes :source-byte-count (alength bytes)
       :source-bytes-sha256 (str "sha256:" (sha256 source))
       :source-extension extension :source-kind :gravity/source
       :sh06-semantic-projection-id
       (get-in resolution [:gravity-resolution-boundary :resolved-analysis
                           :semantic-projection-id])})))

(defn- run-product-from-artifact
  [artifact]
  (select-keys (:gravity-core-boundary artifact)
               [:raw-template-result :canonical-core-artifact :digest-requests
                :resolved-digests :template-verification
                :resolved-verification]))

(defn- exact-digest
  [source-path purpose preimage resolved]
  (let [resolved-preimage
        (bootstrap/sh07-core-resolve-digest-preimage!
         source-path purpose preimage resolved)]
    (bootstrap/reader-canonical-hash
     {:domain :gravity/sh07-declared-digest-v1
      :purpose purpose :preimage resolved-preimage})))

(defn- digest-index
  [product purpose]
  (let [indexes (keep-indexed #(when (= purpose (:purpose %2)) %1)
                              (:digest-requests product))]
    (when (= 1 (count indexes)) (first indexes))))

(defn- semantic-seams
  [source-path carrier]
  (letfn [(resolved [product]
            (reduce (fn [values request]
                      (conj values
                            (exact-digest source-path (:purpose request)
                                          (:preimage request) values)))
                    [] (:digest-requests product)))]
    {:resolve-digest #(exact-digest source-path %1 %2 %3)
     :digest-rebinding? (fn [_ product]
                          (= (:resolved-digests product) (resolved product)))
     :artifact-identity?
     (fn [_ product]
       (let [index (digest-index product :sh07-core-artifact-id)
             core (:canonical-core-artifact product)]
         (and (some? index)
              (= (:identity-preimage core)
                 (get-in product [:digest-requests index :preimage]))
              (= (:artifact-id core) (get-in product [:resolved-digests index])))))
     :provenance-identity?
     (fn [_ product]
       (let [index (digest-index product :sh07-core-provenance-binding-id)
             core (:canonical-core-artifact product)]
         (and (some? index)
              (= (:provenance-binding-preimage core)
                 (get-in product [:digest-requests index :preimage]))
              (= (:provenance-binding-id core)
                 (get-in product [:resolved-digests index])))))
     :lineage?
     (fn [request product]
       (let [lineage (:lineage request)
             core (:canonical-core-artifact product)]
         (and (= (:source-revision-id lineage)
                 (get-in core [:lineage :source-revision-id]))
              (= (:sh06-semantic-projection-id lineage)
                 (get-in core [:lineage :sh06-semantic-projection-id]))
              (= (:authenticated-sh06-artifact-id lineage)
                 (get-in core [:lineage :authenticated-sh06-artifact-id])))))
     :no-alias?
     (fn [stored bound]
       (not (identical? (:run-product stored) bound)))}))

(defn- current-sh07-plan-binding
  []
  (let [binding-var (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)]
    (when-not binding-var (semantic-diagnostic! :stale-pin nil))
    (dissoc @(var-get binding-var) :plan :source-path)))

(defn- artifact-from-bound-product
  [resolution request product]
  (let [core (:canonical-core-artifact product)
        source-path (get-in request [:provenance :actual-source-path])
        boundary
        {:slice :SH-07 :owner :gravity-source
         :adapter-contract bootstrap/sh07-core-adapter-contract
         :plan-binding (current-sh07-plan-binding)
         :authenticated-sh06-resolution-artifact resolution
         :authenticated-core-request request
         :raw-template-result (:raw-template-result product)
         :canonical-core-artifact core
         :digest-requests (:digest-requests product)
         :resolved-digests (:resolved-digests product)
         :template-verification (:template-verification product)
         :resolved-verification (:resolved-verification product)
         :authenticated-envelope-descriptor
         {:artifact :gravity/sh07-authenticated-envelope-descriptor
          :semantic-artifact-id (:artifact-id core)
          :source-revision-id (get-in request [:lineage :source-revision-id])
          :authenticated-sh06-artifact-id
          (get-in request [:lineage :authenticated-sh06-artifact-id])
          :sh06-semantic-projection-id
          (get-in request [:lineage :sh06-semantic-projection-id])}
         :authenticated-envelope
         {:artifact :gravity/sh07-authenticated-envelope
          :semantic-artifact-id (:artifact-id core)
          :authenticated-sh06-artifact-id
          (get-in request [:lineage :authenticated-sh06-artifact-id])
          :actual-source-path source-path}
         :target-source-reread? false :clojure-adapter-residual? true
         :self-hosted? false}
        artifact-base
        {:kind :gravity/sh07-core-artifact :status :accepted :slice :SH-07
         :task "SH-07-B47" :document-set ["L2" "L3" "L6" "L7" "L9" "C5" "C6"]
         :governing-document bootstrap/sh07-core-governing-document
         :artifact-id (:artifact-id core)
         :sh06-resolution-artifact resolution :gravity-core-boundary boundary
         :provenance {:source-path source-path}
         :pass {:name :c6-gravity-core-lowering-b47
                :input :authenticated-sh06-resolution
                :output :canonical-core :owner :gravity.checked-core}
         :execution-boundary
         {:gravity-owned
          [:core-template-construction :template-verification
           :control-flow-construction :control-flow-verification
           :reference-construction :reference-verification
           :var-reference-construction :var-reference-verification
           :call-construction :call-verification
           :function-record-construction :function-record-verification
           :call-edge-construction :call-edge-verification
           :recursion-component-construction :recursion-component-verification
           :keyword-map-lookup-construction :keyword-map-lookup-verification
           :lexical-binding-construction :lexical-binding-verification
           :loop-binding-construction :loop-binding-verification
           :recur-target-construction :recur-target-verification
           :recur-transfer-construction :recur-transfer-verification
           :mutation-construction :mutation-verification
           :error-transfer-construction :error-transfer-verification
           :error-handler-construction :error-handler-verification
           :match-branch-construction :match-branch-verification
           :match-decision-skeleton-construction
           :match-decision-skeleton-verification
           :match-pattern-record-construction
           :match-pattern-record-verification
           :fragment-partition-validation :fragment-ordered-lowering
           :module-assembly-validation :resolved-verification]
          :clojure-seed-owned
          [:plan-execution :sh06-projection-authentication :digest-resolution
           :envelope-binding :compatibility-routing
           :fragment-envelope-construction :final-artifact-binding]
          :downstream-fact-statuses {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
          :pending-lowering-families
          [:cross-file-module-linking :incremental-fragment-cache
           :parallel-fragment-lowering :whole-program-execution
           :alias-qualified-type-references :alias-qualified-var-references
           :alias-qualified-set-mutations :qualified-var-references
           :var-profile-legality-sh09 :keyword-default-value-lookup
           :general-callable-keywords :destructuring-bindings
           :variadic-function-recur :recur-type-compatibility
           :higher-order-and-cross-module-recursion :try-finally
           :try-protected-sequencing :try-handler-sequencing
           :map-list-set-record-constructor-patterns
           :variable-width-vector-patterns :duplicate-pattern-binding-policy
           :guard-patterns :match-exhaustiveness :match-result-type-join]
          :sh07-complete? false :self-hosted? false}
         :capability-based-proof nil :diagnostics []}
        upstream (bootstrap/sh06-resolution-artifact-verification resolution)
        proof (bootstrap/sh07-core-proof-from-checks
               (bootstrap/sh07-core-verification-checks
                artifact-base artifact-base upstream))]
    (assoc artifact-base :capability-based-proof proof)))

(defn- contained-verification-report
  ([artifact] (contained-verification-report artifact artifact))
  ([artifact expected]
  (let [upstream (bootstrap/sh06-resolution-artifact-verification
                  (:sh06-resolution-artifact artifact))
        checks (bootstrap/sh07-core-verification-checks artifact expected upstream)
        failed (vec (for [[check passed?] checks :when (not (true? passed?))]
                      check))
        boundary (:gravity-core-boundary artifact)]
    {:artifact :gravity/sh07-core-artifact-verification
     :status (if (empty? failed) :passed :failed)
     :checks checks :failed-checks failed
     :source-path (get-in artifact [:provenance :source-path])
     :template-verification (:template-verification boundary)
     :resolved-verification (:resolved-verification boundary)
     :upstream-verification upstream})))

(defn with-iteration-cache
  "Runs operation with bounded SH-06/SH-07 caches and returns value + metadata.

  Cache keys bind source content, the checked-core plan, adapter contract, and
  runtime. Results are deliberately non-authoritative."
  [{:keys [maximum-entries selection]
    :or {maximum-entries default-maximum-entries}}
   operation]
  (when-not (and (integer? maximum-entries) (pos? maximum-entries))
    (throw
     (ex-info "Iteration cache maximum must be a positive integer"
              {:id "SH07-ITERATION-CACHE-MAXIMUM"
               :maximum-entries maximum-entries})))
  (let [context (cache-context)
        semantic? (exact-c10-semantic-selection? selection)
        original-sh06 bootstrap/sh06-resolution-source-artifact
        original-core bootstrap/sh07-core-from-resolution-artifact
        original-verification bootstrap/sh07-core-artifact-verification
        counters (atom empty-cache-counters)
        resolution-inputs
        (java.util.Collections/synchronizedMap (java.util.IdentityHashMap.))
        verification-results
        (java.util.Collections/synchronizedMap (java.util.IdentityHashMap.))
        sh06-cache (bounded-cache maximum-entries counters
                                  :sh06-hits :sh06-misses)
        core-cache (bounded-cache maximum-entries counters
                                  :core-hits :core-misses)
        verification-cache
        (bounded-cache maximum-entries counters
                       :verification-hits :verification-misses)
        semantic-cache
        (bounded-semantic-cache (min maximum-entries 4)
                                (* 512 1024 1024) counters)
        cached-sh06
        (fn [path source]
          (let [resolution
                (sh06-cache [context path (sha256 source)]
                            #(original-sh06 path source))]
            (when semantic?
              (.put resolution-inputs resolution {:path path :source source}))
            resolution))
        cached-core
        (fn [resolution]
          (if-not semantic?
            (core-cache (resolution-key context resolution)
                        #(original-core resolution))
            (let [{:keys [path source]} (.get resolution-inputs resolution)
                  _ (when-not (and (string? path) (string? source))
                      (semantic-diagnostic! :source-missing nil))
                  snapshot (strict-source-snapshot path source resolution)
                  key (semantic-carrier/semantic-key snapshot)
                  fresh (atom nil)
                  stored
                  (semantic-cache
                   key
                   (fn []
                     (let [artifact (original-core resolution)
                           expected (original-core resolution)
                           report (contained-verification-report artifact expected)]
                       (when-not (= :passed (:status report))
                         (semantic-diagnostic! :producer-failed path))
                       (let [request (bootstrap/sh07-core-authenticated-request resolution)
                             carrier
                             (semantic-carrier/construct-carrier
                              snapshot resolution request
                              (run-product-from-artifact artifact))]
                         (reset! fresh {:artifact artifact :report report})
                         carrier))))]
              (if-let [{:keys [artifact report]} @fresh]
                (do (.put verification-results artifact report) artifact)
                (let [current-snapshot (strict-source-snapshot path source resolution)
                      request (bootstrap/sh07-core-authenticated-request resolution)
                      upstream (bootstrap/sh06-resolution-artifact-verification resolution)
                      bound (semantic-carrier/bind-carrier
                             stored current-snapshot resolution upstream request
                             (semantic-seams path stored))
                      artifact (artifact-from-bound-product
                                resolution request (:bound-run-product bound))
                      report (contained-verification-report artifact)]
                  (when-not (= :passed (:status report))
                    (swap! counters update :semantic-rejected-hits inc)
                    (semantic-diagnostic! :supplied-verification-failed path))
                  (.put verification-results artifact report)
                  artifact)))))
        cached-verification
        (fn [artifact]
          (if-let [report (when semantic? (.remove verification-results artifact))]
            (do (swap! counters update :verification-hits inc) report)
            (verification-cache
             [context (identity-key artifact) (:artifact-id artifact)]
             #(original-verification artifact))))
        started (System/nanoTime)
        value
        (binding [*iteration-cache-counters* counters]
          (with-redefs [bootstrap/sh06-resolution-source-artifact cached-sh06
                        bootstrap/sh07-core-from-resolution-artifact cached-core
                        bootstrap/sh07-core-artifact-verification
                        cached-verification]
            (operation)))]
    {:value value
     :cache @counters
     :cache-context context
     :semantic-cache-enabled? semantic?
     :semantic-maximum-entries (when semantic? (min maximum-entries 4))
     :semantic-maximum-canonical-bytes (when semantic? (* 512 1024 1024))
     :maximum-entries maximum-entries
     :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
     :authority :non-authoritative
     :authoritative? false
     :cache-authoritative? false
     :fresh-authoritative-run-required? true}))

(defn- parse-positive-integer
  [option value]
  (try
    (let [parsed (Long/parseLong value)]
      (when-not (pos? parsed)
        (throw (NumberFormatException.)))
      parsed)
    (catch NumberFormatException _
      (throw
       (ex-info "Expected a positive integer"
                {:id "SH07-ITERATION-CACHE-ARGUMENT"
                 :option option :value value})))))

(defn parse-arguments
  [arguments]
  (loop [remaining (vec arguments)
         result {:namespaces []
                 :maximum-entries default-maximum-entries
                 :fail-fast? false
                 :test-var nil}]
    (if (empty? remaining)
      (do
        (let [test-vars
              (cond
                (:test-var result) [(:test-var result)]
                (seq (:test-vars result)) (:test-vars result)
                :else [])]
          (when (and (empty? (:namespaces result))
                     (empty? test-vars))
          (throw
           (ex-info "At least one --namespace or one --test-var is required"
                    {:id "SH07-ITERATION-CACHE-SELECTION"})))
          (when (and (seq (:namespaces result)) (seq test-vars))
          (throw
           (ex-info "Namespace and test-var selections cannot be combined"
                    {:id "SH07-ITERATION-CACHE-SELECTION-CONFLICT"
                     :namespaces (:namespaces result)
                     :test-vars test-vars})))
          (when (and (= 1 (count test-vars)) (:fail-fast? result))
          (throw
           (ex-info "--fail-fast requires more than one selected unit"
                    {:id "SH07-ITERATION-CACHE-FAIL-FAST-SELECTION"
                     :test-var (first test-vars)}))))
        (when-not (= (count (:namespaces result))
                     (count (distinct (:namespaces result))))
          (throw
           (ex-info "Iteration cache namespaces must be unique"
                    {:id "SH07-ITERATION-CACHE-DUPLICATE-NAMESPACE"
                     :namespaces (:namespaces result)})))
        result)
      (let [option (first remaining)]
        (case option
          "--fail-fast"
          (recur (subvec remaining 1)
                 (assoc result :fail-fast? true))

          "--namespace"
          (let [value (second remaining)]
            (when (nil? value)
              (throw
               (ex-info "Iteration cache option requires a value"
                        {:id "SH07-ITERATION-CACHE-USAGE"
                         :option option})))
            (let [namespace (symbol value)]
              ;; Delegate ownership validation to the coordinator runner.
              (test-runner/select-tests ["--namespace" value])
              (recur (subvec remaining 2)
                     (update result :namespaces conj namespace))))

          "--max-cache-entries"
          (let [value (second remaining)]
            (when (nil? value)
              (throw
               (ex-info "Iteration cache option requires a value"
                        {:id "SH07-ITERATION-CACHE-USAGE"
                         :option option})))
            (recur (subvec remaining 2)
                   (assoc result :maximum-entries
                          (parse-positive-integer option value))))

          "--test-var"
          (let [value (second remaining)]
            (when (nil? value)
              (throw
               (ex-info "Iteration cache option requires a value"
                        {:id "SH07-ITERATION-CACHE-USAGE"
                         :option option})))
            (let [test-var (symbol value)
                  namespace-name (namespace test-var)]
              (when-not namespace-name
                (throw
                 (ex-info "Test-var selection must be namespace-qualified"
                          {:id "SH07-ITERATION-CACHE-TEST-VAR"
                           :test-var test-var})))
              (let [selected
                    (cond
                      (:test-var result) [(:test-var result)]
                      (seq (:test-vars result)) (:test-vars result)
                      :else [])]
                (when (some #{test-var} selected)
                (throw
                 (ex-info "Iteration cache test-var selections must be unique"
                          {:id "SH07-ITERATION-CACHE-DUPLICATE-TEST-VAR"
                           :test-vars (conj (vec selected) test-var)}))))
              ;; Ownership remains anchored in the discovered test catalog.
              (test-runner/select-tests ["--namespace" namespace-name])
              (recur (subvec remaining 2)
                     (if (:test-var result)
                       (-> result
                           (assoc :test-var nil)
                           (assoc :test-vars
                                  [(:test-var result) test-var]))
                       (if (seq (:test-vars result))
                         (update result :test-vars conj test-var)
                         (assoc result :test-var test-var))))))

          (throw
           (ex-info "Unsupported iteration cache option"
                    {:id "SH07-ITERATION-CACHE-USAGE"
                     :option option
                     :supported ["--namespace" "--test-var"
                                 "--max-cache-entries" "--fail-fast"]})))))))

(defn- cache-snapshot
  []
  (if *iteration-cache-counters*
    @*iteration-cache-counters*
    empty-cache-counters))

(defn- cache-delta
  [before after]
  (merge-with - after before))

(defn- namespace-symbol
  [namespace]
  (if (instance? clojure.lang.Namespace namespace)
    (ns-name namespace)
    namespace))

(defn- failed-summary?
  [summary]
  (or (pos? (:fail summary))
      (pos? (:error summary))))

(defn- new-failure?
  [before after]
  (or (> (:fail after 0) (:fail before 0))
      (> (:error after 0) (:error before 0))))

(defn- qualified-test-var-symbol
  [test-var]
  (let [metadata (meta test-var)]
    (symbol (str (ns-name (:ns metadata)))
            (str (:name metadata)))))

(defn- fail-fast-test-ns
  [namespace]
  (binding [test/*report-counters* (ref test/*initial-report-counters*)]
    (let [namespace-object (the-ns namespace)
          skipped-test-vars (atom [])]
      (test/do-report {:type :begin-test-ns :ns namespace-object})
      (if-let [hook
               (find-var
                (symbol (str (ns-name namespace-object)) "test-ns-hook"))]
        ((var-get hook))
        (let [namespace-vars (vec (vals (ns-interns namespace-object)))
              once-fixture
              (test/join-fixtures
               (:clojure.test/once-fixtures (meta namespace-object)))
              each-fixture
              (test/join-fixtures
               (:clojure.test/each-fixtures (meta namespace-object)))]
          (when (seq namespace-vars)
            (once-fixture
             (fn []
               (loop [remaining namespace-vars]
                 (when-let [test-var (first remaining)]
                   (let [tail (subvec remaining 1)]
                     (if (:test (meta test-var))
                       (let [before @test/*report-counters*]
                         (each-fixture #(test/test-var test-var))
                         (if (new-failure?
                              before @test/*report-counters*)
                           (reset!
                            skipped-test-vars
                            (mapv qualified-test-var-symbol
                                  (filter (comp :test meta) tail)))
                           (recur tail)))
                       (recur tail))))))))))
      (test/do-report {:type :end-test-ns :ns namespace-object})
      {:summary @test/*report-counters*
       :skipped-test-vars @skipped-test-vars})))

(defn- test-namespace
  [namespace fail-fast?]
  (if fail-fast?
    (fail-fast-test-ns namespace)
    {:summary (test/test-ns namespace)
     :skipped-test-vars []}))

(defn- run-tests-with-telemetry
  [namespaces fail-fast?]
  (let [namespace-results (atom [])]
    (letfn [(run-namespace [namespace]
              (let [started (System/nanoTime)
                    before (cache-snapshot)
                    test-run (atom nil)]
                (try
                  (let [result (test-namespace namespace fail-fast?)]
                    (reset! test-run result)
                    result)
                  (finally
                    (let [skipped-test-vars
                          (or (:skipped-test-vars @test-run) [])
                          result
                          {:namespace (namespace-symbol namespace)
                           :elapsed-ms
                           (long
                            (/ (- (System/nanoTime) started) 1000000))
                           :cache (cache-delta before (cache-snapshot))
                           :completed? (some? @test-run)
                           :skipped-test-vars skipped-test-vars
                           :authority :non-authoritative}]
                      (swap! namespace-results conj result)
                      (println
                       (pr-str
                        (assoc result
                               :artifact
                               :gravity/sh07-iteration-namespace-result)))
                      (flush))))))
            (finish [summaries skipped-namespaces skipped-test-vars]
              (let [test-result
                    (assoc (apply merge-with + summaries) :type :summary)]
                (test/do-report test-result)
                {:test-result test-result
                 :namespace-results @namespace-results
                 :stopped-early?
                 (boolean (or (seq skipped-namespaces)
                              (seq skipped-test-vars)))
                 :skipped-namespaces (vec skipped-namespaces)
                 :skipped-test-vars (vec skipped-test-vars)}))]
      (loop [remaining (vec namespaces)
             summaries []]
        (let [namespace (first remaining)
              {:keys [summary skipped-test-vars]}
              (run-namespace namespace)
              summaries' (conj summaries summary)
              tail (subvec remaining 1)
              failed? (failed-summary? summary)]
          (if (or (empty? tail) (and fail-fast? failed?))
            (finish summaries'
                    (if (and fail-fast? failed?) tail [])
                    skipped-test-vars)
            (recur tail summaries')))))))

(defn run-namespaces
  [{:keys [namespaces maximum-entries fail-fast?]}]
  (doseq [namespace namespaces]
    (require namespace))
  (let [cached
        (with-iteration-cache
          {:maximum-entries maximum-entries
           :selection {:mode :namespaces :units (vec namespaces)}}
          #(run-tests-with-telemetry namespaces fail-fast?))
        {:keys [test-result namespace-results stopped-early?
                skipped-namespaces skipped-test-vars]} (:value cached)]
    (-> cached
        (dissoc :value)
        (assoc :artifact :gravity/sh07-iteration-cache-run
               :namespaces namespaces
               :fail-fast? (boolean fail-fast?)
               :stopped-early? stopped-early?
               :skipped-namespaces skipped-namespaces
               :skipped-test-vars skipped-test-vars
               :namespace-results namespace-results
               :test-result test-result
               :ok? (and (zero? (:fail test-result))
                         (zero? (:error test-result)))))))

(defn- resolve-test-var
  [test-var]
  (let [namespace-symbol (symbol (namespace test-var))
        var-symbol (symbol (name test-var))]
    (require namespace-symbol)
    (let [namespace-object (find-ns namespace-symbol)
          resolved (get (ns-interns namespace-object) var-symbol)]
      (when-not (and resolved
                     (identical? namespace-object (:ns (meta resolved)))
                     (:test (meta resolved)))
        (throw
         (ex-info "Selected var is not a discovered test"
                  {:id "SH07-ITERATION-CACHE-TEST-VAR"
                   :test-var test-var
                   :namespace namespace-symbol})))
      resolved)))

(defn run-test-var
  [{:keys [test-var maximum-entries]}]
  (let [resolved (resolve-test-var test-var)
        namespace-symbol (symbol (namespace test-var))
        cached
        (with-iteration-cache
          {:maximum-entries maximum-entries
           :selection {:mode :test-vars :units [test-var]}}
          (fn []
            (let [started (System/nanoTime)
                  before (cache-snapshot)
                  test-result (test/run-test-var resolved)
                  var-result
                  {:test-var test-var
                   :namespace namespace-symbol
                   :elapsed-ms
                   (long (/ (- (System/nanoTime) started) 1000000))
                   :cache (cache-delta before (cache-snapshot))
                   :authority :non-authoritative}]
              (println
               (pr-str
                (assoc var-result
                       :artifact :gravity/sh07-iteration-test-var-result)))
              (flush)
              {:test-result test-result
               :test-var-result var-result})))
        {:keys [test-result test-var-result]} (:value cached)]
    (-> cached
        (dissoc :value)
        (assoc :artifact :gravity/sh07-iteration-cache-run
               :test-var test-var
               :namespaces [namespace-symbol]
               :test-var-result test-var-result
               :test-result test-result
               :ok? (and (zero? (:fail test-result))
                         (zero? (:error test-result)))))))

(defn- run-batch-test-var
  [test-var resolved selection-index remaining-test-vars]
  (let [namespace-symbol (symbol (namespace test-var))
        started (System/nanoTime)
        before (cache-snapshot)
        completed (atom nil)]
    (try
      (let [test-result (test/run-test-var resolved)
            test-var-result
            {:test-var test-var
             :selection-index selection-index
             :namespace namespace-symbol
             :elapsed-ms
             (long (/ (- (System/nanoTime) started) 1000000))
             :cache (cache-delta before (cache-snapshot))
             :test-result test-result
             :ok? (not (failed-summary? test-result))
             :completed? true
             :authority :non-authoritative}
            result {:test-result test-result
                    :test-var-result test-var-result}]
        (reset! completed result)
        result)
      (finally
        (let [test-var-result
              (or
               (:test-var-result @completed)
               {:test-var test-var
                :selection-index selection-index
                :namespace namespace-symbol
                :elapsed-ms
                (long (/ (- (System/nanoTime) started) 1000000))
                :cache (cache-delta before (cache-snapshot))
                :ok? false
                :completed? false
                :skipped-test-vars remaining-test-vars
                :authority :non-authoritative})]
          (println
           (pr-str
            (assoc test-var-result
                   :artifact :gravity/sh07-iteration-test-var-result)))
          (flush))))))

(defn run-test-vars
  [{:keys [test-vars maximum-entries fail-fast?]}]
  (let [resolved-vars (mapv (juxt identity resolve-test-var) test-vars)
        cached
        (with-iteration-cache
          {:maximum-entries maximum-entries
           :selection {:mode :test-vars :units (vec test-vars)}}
          (fn []
            (loop [remaining resolved-vars
                   selection-index 0
                   test-results []
                   test-var-results []]
              (let [[test-var resolved] (first remaining)
                    tail (subvec remaining 1)
                    {:keys [test-result test-var-result]}
                    (run-batch-test-var
                     test-var resolved selection-index (mapv first tail))
                    test-results' (conj test-results test-result)
                    test-var-results' (conj test-var-results test-var-result)
                    failed? (failed-summary? test-result)]
                (if (or (empty? tail) (and fail-fast? failed?))
                  {:test-result
                   (assoc
                    (apply merge-with + (map #(dissoc % :type) test-results'))
                    :type :summary)
                   :test-var-results test-var-results'
                   :stopped-early? (boolean (seq tail))
                   :skipped-test-vars
                   (if (and fail-fast? failed?)
                     (mapv first tail)
                     [])}
                  (recur tail (inc selection-index)
                         test-results' test-var-results'))))))
        {:keys [test-result test-var-results stopped-early?
                skipped-test-vars]} (:value cached)
        namespaces
        (vec (distinct (map (comp symbol namespace) test-vars)))]
    (-> cached
        (dissoc :value)
        (assoc :artifact :gravity/sh07-iteration-cache-run
               :selection-mode :exact-test-vars
               :test-vars test-vars
               :namespaces namespaces
               :fail-fast? (boolean fail-fast?)
               :stopped-early? stopped-early?
               :skipped-test-vars skipped-test-vars
               :test-var-results test-var-results
               :test-result test-result
               :ok? (and (zero? (:fail test-result))
                         (zero? (:error test-result)))))))

(defn run-selection
  [selection]
  (cond
    (:test-var selection) (run-test-var selection)
    (seq (:test-vars selection)) (run-test-vars selection)
    :else (run-namespaces selection)))

(defn -main
  [& arguments]
  (let [result (run-selection (parse-arguments arguments))]
    (println (pr-str result))
    (flush)
    (shutdown-agents)
    (when-not (:ok? result)
      (System/exit 1))))
