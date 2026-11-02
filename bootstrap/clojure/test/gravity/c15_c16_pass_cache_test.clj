(ns gravity.c15-c16-pass-cache-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c15-c16-pass-cache :as cache]
            [gravity.c15-diagnostics :as c15]
            [gravity.c16-incremental :as c16]
            [gravity.digest :as digest]
            [gravity.pass-cache :as pass-cache])
  (:import [java.nio.file Files LinkOption Path]
           [java.nio.file.attribute FileAttribute]))

(defn- id
  [value]
  (str "sha256:" (digest/sha256-hex (pr-str value))))

(defn- delete-tree!
  [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^Path %) (vec (.toArray paths))))]
        (Files/deleteIfExists ^Path path)))))

(defmacro with-temporary-directory
  [[binding] & body]
  `(let [~binding (Files/createTempDirectory
                   "gravity-c15-c16-pass-cache-"
                   (make-array FileAttribute 0))]
     (try
       ~@body
       (finally (delete-tree! ~binding)))))

(def module
  {:module 'gravity.c15-c16-cache-test
   :source-path "c15-c16-cache-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c14-artifact
  {:kind :gravity/stage0-c14-target-lowering-artifact
   :task "P06-D093"
   :artifact-id (id :c14-artifact)
   :governing-document "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"
   :target-artifact-manifest {:artifact :gravity/target-artifact-manifest}
   :capability-based-proof {:status :complete}})

(defn- c15-operations
  []
  {:read-source-form-records
   (fn [_ _]
     [{:form '(ns gravity.c15-c16-cache-test (:profile :hosted))}])
   :validate-ns-syntax! (fn [_ _] nil)
   :parse-module (fn [_ _] module)
   :compiler-c14-lowering-source-artifact (fn [_ _] c14-artifact)})

(defn- c16-operations
  [c15-artifact]
  {:read-source-form-records
   (fn [_ _]
     [{:form '(ns gravity.c15-c16-cache-test (:profile :hosted))}])
   :validate-ns-syntax! (fn [_ _] nil)
   :parse-module (fn [_ _] module)
   :compiler-c15-diagnostics-source-artifact (fn [_ _] c15-artifact)})

(defn- context
  []
  {:c14-artifact-id (:artifact-id c14-artifact)
   :semantic-bindings
   {:compiler-id (id :compiler)
    :capability-policy-id (id :capability-policy)
    :facet-set-id (id :facets)
    :provider-manifest-id (id :providers)
    :package-lock-id (id :package-lock)
    :diagnostic-schema-id (id :diagnostic-schema)}
   :dependency-graph-id (id :dependency-graph)
   :build-effect-replay-id (id :build-effect-replay)
   :profile-id (id :hosted-profile)
   :target-id (id :jvm-target)
   :policy-ids (vec (sort [(id :diagnostic-policy)
                           (id :incremental-policy)]))
   :provenance {:provenance-id (id :source-provenance)
                :source-path "c15-c16-cache-test.gravity"
                :metadata {}}
   :diagnostic-stream-ids {:c15 (id :c15-diagnostic-stream)
                           :c16 (id :c16-diagnostic-stream)}
   :producer-binding-ids {:c15 (id :c15-producer)
                          :c16 (id :c16-producer)}
   :validation-binding-ids {:c15 (id :c15-validator)
                            :c16 (id :c16-validator)}
   :authority-scope :c15-c16-local-cache})

(defn- operations
  [calls]
  {:produce-c15!
   (fn []
     (swap! calls update :c15 inc)
     (c15/with-operations
       (c15-operations)
       #(c15/compiler-c15-diagnostics-source-artifact
         "c15-c16-cache-test.gravity" "source")))
   :validate-c15!
   (fn [artifact]
     (c15/with-operations
       (c15-operations)
       #(c15/c15-diagnostics-validate!
         "c15-c16-cache-test.gravity" artifact)))
   :produce-c16!
   (fn [c15-artifact]
     (swap! calls update :c16 inc)
     (c16/with-operations
       (c16-operations c15-artifact)
       #(c16/compiler-c16-incremental-source-artifact
         "c15-c16-cache-test.gravity" "source")))
   :validate-c16!
   (fn [artifact]
     (c16/with-operations
       (c16-operations (:c15-diagnostics-artifact artifact))
       #(c16/c16-incremental-validate!
         "c15-c16-cache-test.gravity" artifact)))
   :artifact-id-of
   (fn [artifact]
     (:artifact-id artifact))})

(defn- key-pair
  [value]
  (let [c15-request (cache/c15-stage-request value)
        c16-request (cache/c16-stage-request value (id :c15-output))]
    [(get (pass-cache/stage-cache-key c15-request) :semantic-key-id)
     (get (pass-cache/stage-cache-key c16-request) :semantic-key-id)]))

(deftest contract-is-exactly-adjacent-local-and-nonauthoritative
  (let [contract (cache/c15-c16-pass-cache-contract)
        publics (ns-publics 'gravity.c15-c16-pass-cache)]
    (is (= :hosted-stage0-c15-c16-generic-v2-cache-integration
           (:contract-boundary contract)))
    (is (= #{'c15-c16-pass-cache-contract 'c15-stage-request
             'c16-stage-request 'lookup-or-compute!}
           (set (keys publics))))
    (is (= (set (keys publics))
           (set (keys (:public-api contract)))))
    (is (= :gravity/stage0-c15-compiler-diagnostics-artifact
           (get-in contract [:pass-contracts 0 :output])
           (get-in contract [:pass-contracts 1 :input])))
    (is (= :none (get-in contract [:authority :ceiling])))
    (is (false? (get-in contract [:authority :authoritative?])))
    (is (some #{:compiler-authority} (:does-not-own contract)))
    (is (some #{:proof-authority} (:does-not-own contract)))
    (is (= #{'clojure.core 'clojure.edn 'gravity.pass-cache 'gravity.pass-execution}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest exact-c16-invalidators-change-an-adjacent-stage-key
  (let [base (context)
        expected (key-pair base)
        mutations
        [(assoc base :c14-artifact-id (id :changed-c14))
         (assoc-in base [:semantic-bindings :compiler-id]
                   (id :changed-compiler))
         (assoc-in base [:semantic-bindings :capability-policy-id]
                   (id :changed-capability-policy))
         (assoc-in base [:semantic-bindings :facet-set-id]
                   (id :changed-facets))
         (assoc-in base [:semantic-bindings :provider-manifest-id]
                   (id :changed-provider))
         (assoc-in base [:semantic-bindings :package-lock-id]
                   (id :changed-lock))
         (assoc-in base [:semantic-bindings :diagnostic-schema-id]
                   (id :changed-diagnostic-schema))
         (assoc base :dependency-graph-id (id :changed-dependencies))
         (assoc base :build-effect-replay-id (id :changed-replay))
         (assoc base :profile-id (id :changed-profile))
         (assoc base :target-id (id :changed-target))
         (assoc base :policy-ids [(id :changed-policy)])
         (assoc-in base [:provenance :provenance-id]
                   (id :changed-provenance))
         (assoc-in base [:diagnostic-stream-ids :c15]
                   (id :changed-c15-diagnostics))
         (assoc-in base [:diagnostic-stream-ids :c16]
                   (id :changed-c16-diagnostics))
         (assoc-in base [:producer-binding-ids :c15]
                   (id :changed-c15-producer))
         (assoc-in base [:producer-binding-ids :c16]
                   (id :changed-c16-producer))
         (assoc base :authority-scope :changed-local-scope)]]
    (doseq [mutation mutations]
      (is (not= expected (key-pair mutation))))
    ;; Validator identity is deliberately not a semantic key field.  Generic
    ;; cache entries bind it independently and reject stale validator reuse.
    (is (= expected
           (key-pair
            (assoc-in base [:validation-binding-ids :c15]
                      (id :changed-c15-validator)))))))

(deftest adjacent-real-passes-store-hit-and-compose-one-internal-edge
  (with-temporary-directory [directory]
    (let [store (pass-cache/open-local-store directory)
          calls (atom {:c15 0 :c16 0})
          first-result
          (cache/lookup-or-compute! store (context) (operations calls))
          second-result
          (cache/lookup-or-compute! store (context) (operations calls))]
      (is (= {:c15 1 :c16 1} @calls))
      (is (= [:stored :stored]
             [(get-in first-result [:c15-cache-evidence :status])
              (get-in first-result [:c16-cache-evidence :status])]))
      (is (= [:hit :hit]
             [(get-in second-result [:c15-cache-evidence :status])
              (get-in second-result [:c16-cache-evidence :status])])
          (pr-str {:c15 (:c15-cache-evidence second-result)
                   :c16 (:c16-cache-evidence second-result)}))
      (is (= :gravity/stage0-c15-compiler-diagnostics-artifact
             (get-in second-result [:c15-artifact :kind])))
      (is (= :gravity/stage0-c16-incremental-compilation-artifact
             (get-in second-result [:c16-artifact :kind])))
      (is (= 2 (count (get-in second-result [:evidence-dag :receipts]))))
      (is (= 2 (count (get-in second-result [:evidence-dag :contracts]))))
      (is (= 1 (count (get-in second-result [:evidence-dag :edges]))))
      (is (= :none
             (get-in second-result [:evidence-dag :authority
                                    :effective-level])))
      (is (= (:evidence-root-id second-result)
             (get-in second-result [:evidence-dag :evidence-root-id])))
      (is (false? (:release-authority? second-result)))
      (is (false? (:proof-authority? second-result))))))

(deftest malformed-context-operations-and-stale-validator-fail-closed
  (let [base (context)]
    (doseq [invalid [(assoc base :unknown true)
                     (dissoc base :profile-id)
                     (assoc base :policy-ids (reverse (:policy-ids base)))
                     (assoc-in base [:semantic-bindings :compiler-id]
                               "sha256:bad")]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (cache/c15-stage-request invalid)))))
  (with-temporary-directory [directory]
    (let [store (pass-cache/open-local-store directory)
          calls (atom {:c15 0 :c16 0})
          base (context)
          _ (cache/lookup-or-compute! store base (operations calls))
          changed (assoc-in base [:validation-binding-ids :c15]
                            (id :replacement-c15-validator))
          result (cache/lookup-or-compute! store changed (operations calls))]
      (is (= :miss (get-in result [:c15-cache-evidence :status])))
      (is (= :withheld
             (get-in result [:c15-cache-evidence :cache-publication])))
      (is (= {:c15 2 :c16 1} @calls)))
  (with-temporary-directory [directory]
    (let [store (pass-cache/open-local-store directory)]
      (is (thrown? clojure.lang.ExceptionInfo
                   (cache/lookup-or-compute!
                    store (context) {:produce-c15! identity})))))))
