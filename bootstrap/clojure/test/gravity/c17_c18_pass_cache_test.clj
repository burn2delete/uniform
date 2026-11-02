(ns gravity.c17-c18-pass-cache-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.c15-c16-pass-cache :as upstream-cache]
            [gravity.c15-diagnostics :as c15]
            [gravity.c16-incremental :as c16]
            [gravity.c17-c18-pass-cache :as cache]
            [gravity.c17-c18-pass-cache.upstream-boundary :as upstream-boundary]
            [gravity.c17-c18-pass-cache.validation :as validation]
            [gravity.c17-plugin :as c17]
            [gravity.c18-verification :as c18]
            [gravity.digest :as digest]
            [gravity.pass-cache :as pass-cache]
            [gravity.pass-execution :as pass-execution]
            [gravity.pass-execution.dag :as dag])
  (:import [java.nio.file Files LinkOption Path]
           [java.nio.file.attribute FileAttribute]))

(defn- id [value]
  (str "sha256:" (digest/sha256-hex (pr-str value))))

(defn- delete-tree! [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^Path %)
                                     (vec (.toArray paths))))]
        (Files/deleteIfExists ^Path path)))))

(defn- with-temporary-directory
  [test-fn]
  (let [directory (Files/createTempDirectory
                   "gravity-c17-c18-pass-cache-"
                   (make-array FileAttribute 0))]
    (try
      (test-fn directory)
      (finally (delete-tree! directory)))))

(def module
  {:module 'gravity.c17-c18-cache-test
   :source-path "c17-c18-cache-test.gravity"
   :profile :hosted :target :jvm :effects #{} :capabilities #{}
   :safety :safe :metadata {}})

(def c14-artifact
  {:kind :gravity/stage0-c14-target-lowering-artifact
   :task "P06-D093" :artifact-id (id :c14-artifact)
   :governing-document "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"
   :target-artifact-manifest {:artifact :gravity/target-artifact-manifest}
   :capability-based-proof {:status :complete}})

(defn- base-operations [input-key input-artifact]
  {:read-source-form-records
   (fn [_ _] [{:form '(ns gravity.c17-c18-cache-test (:profile :hosted))}])
   :validate-ns-syntax! (fn [_ _] nil)
   :parse-module (fn [_ _] module)
   input-key (fn [_ _] input-artifact)})

(defn- upstream-context []
  {:c14-artifact-id (:artifact-id c14-artifact)
   :semantic-bindings
   {:compiler-id (id :compiler) :capability-policy-id (id :capability-policy)
    :facet-set-id (id :facets) :provider-manifest-id (id :providers)
    :package-lock-id (id :package-lock)
    :diagnostic-schema-id (id :diagnostic-schema)}
   :dependency-graph-id (id :dependency-graph)
   :build-effect-replay-id (id :build-effect-replay)
   :profile-id (id :hosted-profile) :target-id (id :jvm-target)
   :policy-ids (vec (sort [(id :diagnostic-policy) (id :incremental-policy)]))
   :provenance {:provenance-id (id :source-provenance)
                :source-path "c17-c18-cache-test.gravity" :metadata {}}
   :diagnostic-stream-ids {:c15 (id :c15-stream) :c16 (id :c16-stream)}
   :producer-binding-ids {:c15 (id :c15-producer) :c16 (id :c16-producer)}
   :validation-binding-ids {:c15 (id :c15-validator) :c16 (id :c16-validator)}
   :authority-scope :c15-c18-local-cache})

(defn- downstream-context []
  (-> (upstream-context)
      (dissoc :c14-artifact-id)
      (assoc :diagnostic-stream-ids
             {:c17 (id :c17-stream) :c18 (id :c18-stream)}
             :producer-binding-ids
             {:c17 (id :c17-producer) :c18 (id :c18-producer)}
             :validation-binding-ids
             {:c17 (id :c17-validator) :c18 (id :c18-validator)})))

(defn- upstream-operations [calls]
  {:produce-c15!
   (fn []
     (swap! calls update :c15 inc)
     (c15/with-operations
       (base-operations :compiler-c14-lowering-source-artifact c14-artifact)
       #(c15/compiler-c15-diagnostics-source-artifact
         "c17-c18-cache-test.gravity" "source")))
   :validate-c15!
   (fn [artifact]
     (c15/with-operations
       (base-operations :compiler-c14-lowering-source-artifact c14-artifact)
       #(c15/c15-diagnostics-validate! "c17-c18-cache-test.gravity" artifact)))
   :produce-c16!
   (fn [c15-artifact]
     (swap! calls update :c16 inc)
     (c16/with-operations
       (base-operations :compiler-c15-diagnostics-source-artifact c15-artifact)
       #(c16/compiler-c16-incremental-source-artifact
         "c17-c18-cache-test.gravity" "source")))
   :validate-c16!
   (fn [artifact]
     (c16/with-operations
       (base-operations :compiler-c15-diagnostics-source-artifact
                        (:c15-diagnostics-artifact artifact))
       #(c16/c16-incremental-validate! "c17-c18-cache-test.gravity" artifact)))
   :artifact-id-of (fn [artifact] (:artifact-id artifact))})

(defn- downstream-operations [calls]
  {:produce-c17!
   (fn [c16-artifact]
     (swap! calls update :c17 inc)
     (c17/with-operations
       (base-operations :compiler-c16-incremental-source-artifact c16-artifact)
       #(c17/compiler-c17-plugin-source-artifact
         "c17-c18-cache-test.gravity" "source")))
   :validate-c17!
   (fn [artifact]
     (c17/with-operations
       (base-operations :compiler-c16-incremental-source-artifact
                        (:c16-incremental-artifact artifact))
       #(c17/c17-plugin-validate! "c17-c18-cache-test.gravity" artifact)))
   :produce-c18!
   (fn [c17-artifact]
     (swap! calls update :c18 inc)
     (c18/with-operations
       (base-operations :compiler-c17-plugin-source-artifact c17-artifact)
       #(c18/compiler-c18-verification-source-artifact
         "c17-c18-cache-test.gravity" "source")))
   :validate-c18!
   (fn [artifact]
     (c18/with-operations
       (base-operations :compiler-c17-plugin-source-artifact
                        (:c17-plugin-artifact artifact))
       #(c18/c18-verification-validate!
         "c17-c18-cache-test.gravity" artifact)))
   :artifact-id-of (fn [artifact] (:artifact-id artifact))})

(defn- upstream-result [store calls]
  (upstream-cache/lookup-or-compute!
   store (upstream-context) (upstream-operations calls)))

(deftest contract-is-exactly-downstream-local-and-nonauthoritative
  (let [contract (cache/c17-c18-pass-cache-contract)
        publics (ns-publics 'gravity.c17-c18-pass-cache)]
    (is (= :hosted-stage0-c17-c18-generic-v2-cache-integration
           (:contract-boundary contract)))
    (is (= #{'c17-c18-pass-cache-contract 'c17-stage-request
             'c18-stage-request 'lookup-or-compute!}
           (set (keys publics))))
    (is (= :none (get-in contract [:authority :ceiling])))
    (is (false? (get-in contract [:authority :authoritative?])))
    (is (some #{:release-gate-authority} (:does-not-own contract)))
    (is (= #{'clojure.core 'clojure.edn
             'gravity.pass-cache 'gravity.pass-execution}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest real-c17-c18-store-hit-and-compose-four-pass-chain
  (with-temporary-directory
    (fn [directory]
      (let [store (pass-cache/open-local-store directory)
            calls (atom {:c15 0 :c16 0 :c17 0 :c18 0})
            upstream (upstream-result store calls)
            first-result
            (cache/lookup-or-compute!
             store upstream (downstream-context) (downstream-operations calls))
            second-result
            (cache/lookup-or-compute!
             store upstream (downstream-context) (downstream-operations calls))]
        (is (= {:c15 1 :c16 1 :c17 1 :c18 1} @calls))
        (is (= [:stored :stored]
               [(get-in first-result [:c17-cache-evidence :status])
                (get-in first-result [:c18-cache-evidence :status])]))
        (is (= [:hit :hit]
               [(get-in second-result [:c17-cache-evidence :status])
                (get-in second-result [:c18-cache-evidence :status])]))
        (is (= :gravity/stage0-c17-compiler-plugin-artifact
               (get-in second-result [:c17-artifact :kind])))
        (is (= :gravity/stage0-c18-compiler-verification-artifact
               (get-in second-result [:c18-artifact :kind])))
        (is (= 4 (count (get-in second-result [:evidence-dag :receipts]))))
        (is (= 4 (count (get-in second-result [:evidence-dag :contracts]))))
        (is (= 3 (count (get-in second-result [:evidence-dag :edges]))))
        (is (= :none
               (get-in second-result [:evidence-dag :authority :effective-level])))
        (is (= (:evidence-root-id second-result)
               (get-in second-result [:evidence-dag :evidence-root-id])))
        (is (false? (:release-authority? second-result)))
        (is (false? (:proof-authority? second-result)))))))

(deftest default-orchestration-reuses-validated-boundaries-once
  (with-temporary-directory
    (fn [directory]
      (let [store (pass-cache/open-local-store directory)
            calls (atom {:c15 0 :c16 0 :c17 0 :c18 0})
            upstream (upstream-result store calls)
            observed (atom [])
            original-upstream upstream-boundary/validate-upstream!
            original-context validation/validate-context!
            original-operations validation/validate-operations!
            original-c17-receipt upstream-boundary/validate-c17-receipt!
            result
            (with-redefs
             [upstream-boundary/validate-upstream!
              (fn [& args]
                (swap! observed conj :upstream)
                (apply original-upstream args))
              validation/validate-context!
              (fn [value]
                (swap! observed conj :context)
                (original-context value))
              validation/validate-operations!
              (fn [value]
                (swap! observed conj :operations)
                (original-operations value))
              upstream-boundary/validate-c17-receipt!
              (fn [& args]
                (swap! observed conj :c17-receipt)
                (apply original-c17-receipt args))]
              (cache/lookup-or-compute!
               store upstream (downstream-context)
               (downstream-operations calls)))]
        (is (= [:upstream :context :operations :c17-receipt] @observed))
        (is (= [:stored :stored]
               [(get-in result [:c17-cache-evidence :status])
                (get-in result [:c18-cache-evidence :status])]))))))

(deftest default-orchestration-composes-the-final-dag-once
  (with-temporary-directory
    (fn [directory]
      (let [store (pass-cache/open-local-store directory)
            calls (atom {:c15 0 :c16 0 :c17 0 :c18 0})
            upstream (upstream-result store calls)
            compose-calls (atom 0)
            original-compose dag/compose-evidence-dag
            result
            (with-redefs
             [dag/compose-evidence-dag
              (fn [& args]
                (swap! compose-calls inc)
                (apply original-compose args))]
              (cache/lookup-or-compute!
               store upstream (downstream-context)
               (downstream-operations calls)))]
        ;; One call validates the supplied upstream root; one builds the final
        ;; four-receipt DAG. The final root does not recompose it a third time.
        (is (= 2 @compose-calls))
        (is (= (:evidence-root-id result)
               (get-in result [:evidence-dag :evidence-root-id])))))))

(deftest interposed-composer-and-root-callbacks-remain-live
  (with-temporary-directory
    (fn [directory]
      (let [store (pass-cache/open-local-store directory)
            calls (atom {:c15 0 :c16 0 :c17 0 :c18 0})
            upstream (upstream-result store calls)
            observed (atom [])
            original-compose pass-execution/compose-evidence-dag
            original-root pass-execution/evidence-root]
        (with-redefs
         [pass-execution/compose-evidence-dag
          (fn [& args]
            (swap! observed conj :compose)
            (apply original-compose args))
          pass-execution/evidence-root
          (fn [& args]
            (swap! observed conj :root)
            (apply original-root args))]
          (cache/lookup-or-compute!
           store upstream (downstream-context)
           (downstream-operations calls)))
        ;; The upstream boundary and the final interposed root are both
        ;; validated; only the final composition is interposed once.
        (is (= [:root :compose :root] @observed))))))

(deftest public-stage-request-interposition-remains-live
  (with-temporary-directory
    (fn [directory]
      (let [store (pass-cache/open-local-store directory)
            calls (atom {:c15 0 :c16 0 :c17 0 :c18 0})
            upstream (upstream-result store calls)
            c17-original cache/c17-stage-request
            c18-original cache/c18-stage-request
            observed (atom [])
            result
            (with-redefs
             [cache/c17-stage-request
              (fn [context value]
                (swap! observed conj :c17)
                (c17-original context value))
              cache/c18-stage-request
              (fn [context receipt]
                (swap! observed conj :c18)
                (c18-original context receipt))]
              (cache/lookup-or-compute!
               store upstream (downstream-context)
               (downstream-operations calls)))]
        (is (= [:c17 :c18] @observed))
        (is (= :gravity/stage0-c18-compiler-verification-artifact
               (get-in result [:c18-artifact :kind])))))))

(deftest upstream-root-and-current-validation-bindings-fail-closed
  (with-temporary-directory
    (fn [directory]
      (let [store (pass-cache/open-local-store directory)
            calls (atom {:c15 0 :c16 0 :c17 0 :c18 0})
            upstream (upstream-result store calls)
            invalid (assoc-in upstream [:evidence-dag :evidence-root-id]
                              (id :forged-root))]
        (try
          (cache/c17-stage-request (downstream-context) invalid)
          (is false "forged upstream DAG must be rejected")
          (catch clojure.lang.ExceptionInfo error
            (is (= "C16-STALE" (:id (ex-data error))))))
        (cache/lookup-or-compute!
         store upstream (downstream-context) (downstream-operations calls))
        (let [changed (assoc-in (downstream-context)
                                [:validation-binding-ids :c17]
                                (id :replacement-c17-validator))
              result (cache/lookup-or-compute!
                      store upstream changed (downstream-operations calls))]
          (is (= :miss (get-in result [:c17-cache-evidence :status])))
          (is (= :withheld
                 (get-in result [:c17-cache-evidence :cache-publication])))
          (is (= {:c15 1 :c16 1 :c17 2 :c18 1} @calls)))))))

(deftest malformed-context-and-operations-are-rejected-before-execution
  (with-temporary-directory
    (fn [directory]
      (let [store (pass-cache/open-local-store directory)
            calls (atom {:c15 0 :c16 0 :c17 0 :c18 0})
            upstream (upstream-result store calls)]
        (doseq [invalid [(assoc (downstream-context) :unknown true)
                         (dissoc (downstream-context) :profile-id)
                         (assoc (downstream-context) :policy-ids
                                (reverse (:policy-ids (downstream-context))))]]
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/c17-stage-request invalid upstream))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/c18-stage-request
                      (assoc (downstream-context) :unknown true)
                      {:output-artifact-id (id :forged-c17)
                       :output-facts #{}})))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/c18-stage-request
                      (downstream-context)
                      {:output-artifact-id (id :forged-c17)
                       :output-facts #{}})))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup-or-compute!
                      store upstream (downstream-context)
                      {:produce-c17! identity})))))))
