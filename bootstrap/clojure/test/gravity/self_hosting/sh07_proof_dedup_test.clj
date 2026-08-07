(ns gravity.self-hosting.sh07-proof-dedup-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- minimal-core-artifact
  []
  (let [module-assembly-manifest {}
        identity-preimage
        {:module-assembly-manifest module-assembly-manifest}
        artifact-id
        (bootstrap/reader-canonical-hash
         {:domain :gravity/sh07-declared-digest-v1
          :purpose :sh07-core-artifact-id
          :preimage identity-preimage})
        core
        {:artifact-id artifact-id
         :identity-preimage identity-preimage
         :fragment-manifest []
         :fragment-coverage {}
         :module-assembly-manifest module-assembly-manifest
         :declared-alias-table []
         :control-flow []
         :reference-uses []
         :var-references []
         :calls []
         :function-records []
         :call-edges []
         :recursion-components []
         :keyword-lookups []
         :lexical-bindings []
         :loop-bindings []
         :recur-targets []
         :recur-transfers []
         :mutations []
         :nodes []
         :error-transfers []
         :error-handlers []
         :match-branch-records []
         :match-decision-skeletons []
         :match-pattern-records []
         :provenance {:actual-source-path "synthetic.gravity"}}]
    {:kind :gravity/sh07-core-artifact
     :artifact-id artifact-id
     :gravity-core-boundary
     {:authenticated-core-request {:request :synthetic}
      :raw-template-result {:template :synthetic}
      :digest-requests []
      :resolved-digests []
      :canonical-core-artifact core
      :template-verification {:status :passed}
      :resolved-verification {:status :passed}}
     :provenance {:source-path "synthetic.gravity"}
     :capability-based-proof nil}))

(def ^:private expected-check-catalog
  #{:wrapper-schema-current?
    :wrapper-kind-current?
    :upstream-verification-passed?
    :semantic-artifact-id-current?
    :authenticated-request-replays?
    :gravity-template-replays?
    :digest-sequence-replays?
    :resolved-digests-replay?
    :canonical-core-replays?
    :fragment-manifest-replay?
    :fragment-coverage-replay?
    :module-assembly-manifest-replay?
    :module-replay?
    :declared-alias-table-replay?
    :control-flow-replays?
    :reference-uses-replay?
    :var-references-replay?
    :calls-replay?
    :keyword-lookups-replay?
    :lexical-bindings-replay?
    :loop-bindings-replay?
    :recur-targets-replay?
    :recur-transfers-replay?
    :mutations-replay?
    :error-transfers-replay?
    :error-handlers-replay?
    :match-branch-records-replay?
    :match-decision-skeletons-replay?
    :match-pattern-records-replay?
    :template-verification-passed?
    :resolved-verification-passed?
    :authoritative-products-replay?
    :stored-capability-proof-current?
    :provenance-retained?})

(deftest identical-construction-carrier-skips-replay-normalization
  (let [artifact (minimal-core-artifact)
        distinct-equal-artifact (with-meta artifact {:distinct-copy true})
        upstream {:status :passed}
        original bootstrap/sh07-core-exact-comparison-value
        self-calls (atom 0)
        distinct-calls (atom 0)
        self-checks
        (with-redefs [bootstrap/sh07-core-exact-comparison-value
                      (fn [value]
                        (swap! self-calls inc)
                        (original value))]
          (bootstrap/sh07-core-verification-checks
           artifact artifact upstream))
        distinct-checks
        (with-redefs [bootstrap/sh07-core-exact-comparison-value
                      (fn [value]
                        (swap! distinct-calls inc)
                        (original value))]
          (bootstrap/sh07-core-verification-checks
           artifact distinct-equal-artifact upstream))]
    (testing "the construction shortcut preserves every check and proof bit"
      (is (not (identical? artifact distinct-equal-artifact)))
      (is (= distinct-checks self-checks))
      (is (every? true? (vals self-checks)))
      (is (every? (set (keys self-checks)) expected-check-catalog))
      (is (= (bootstrap/reader-canonical-hash distinct-checks)
             (bootstrap/reader-canonical-hash self-checks)))
      (is (= :complete
             (:status (bootstrap/sh07-core-proof-from-checks self-checks)))))
    (testing "exact normalization remains for a distinct audit carrier"
      (is (< @self-calls @distinct-calls))
      (is (<= @self-calls 4))
      (is (pos? @distinct-calls)))))

(deftest distinct-audit-carrier-still-detects-replay-drift
  (let [artifact (minimal-core-artifact)
        altered
        (assoc-in artifact
                  [:gravity-core-boundary :canonical-core-artifact :calls]
                  [{:callee 'changed}])
        checks
        (bootstrap/sh07-core-verification-checks
         artifact altered {:status :passed})
        proof (bootstrap/sh07-core-proof-from-checks checks)]
    (is (false? (:calls-replay? checks)))
    (is (false? (:canonical-core-replays? checks)))
    (is (= :failed (:status proof)))
    (is (some #{:calls-replay?} (:failed-checks proof)))))

(defn- minimal-resolution-artifact
  [macro-trace]
  {:artifact-id "sha256:synthetic-sh06"
   :gravity-resolution-boundary
   {:resolved-analysis
    {:module-contract
     {:namespace 'synthetic :profile :meta :target :jvm
      :safety :safe :effects [] :capabilities [] :exports []}
     :binding-table []
     :resolution-table []
     :alias-table []
     :lexical-scope-graph {:scope-count 0 :edge-count 0 :status :complete}}}
   :sh05-macro-artifact
   {:expanded-syntax-stream []
    :macro-expansion-trace macro-trace
    :gravity-macro-boundary
    {:authenticated-sh04-artifact
     {:c2-reader-artifact
      {:source-unit-record {:bytes-hash "sha256:synthetic-source"}}}}}})

(deftest precomputed-semantic-lineage-preserves-all-hashes
  (let [path-bearing-capabilities
        [{:capability :read-source
          :actual-source-path "/private/worktree/compiler.gravity"
          :workspace-root "/private/worktree"
          :source "gravity/compiler.gravity"
          :byte-start 17}]
        macro-trace
        [{:macro 'defn :profile :meta :target :jvm :step :expand
          :capabilities path-bearing-capabilities
          :build-effects [] :diagnostics []}]
        artifact (minimal-resolution-artifact macro-trace)
        semantic-trace
        (bootstrap/sh07-core-semantic-macro-trace macro-trace)
        original-neutral bootstrap/sh05-path-neutral-semantic-value
        original-hash bootstrap/reader-canonical-hash
        neutralizations (atom 0)
        sh05-domain-hashes (atom 0)
        lineage
        (with-redefs
         [bootstrap/sh05-path-neutral-semantic-value
          (fn [value]
            (when (identical? value path-bearing-capabilities)
              (swap! neutralizations inc))
            (original-neutral value))
          bootstrap/reader-canonical-hash
          (fn [value]
            (when (= :gravity/sh07-semantic-sh05-artifact-v1
                     (:domain value))
              (swap! sh05-domain-hashes inc))
            (original-hash value))]
          (bootstrap/sh07-core-lineage artifact))]
    (is (= 1 @neutralizations))
    (is (= 1 @sh05-domain-hashes))
    (is (= [{:capability :read-source
             :source "gravity/compiler.gravity"
             :byte-start 17}]
           (get-in semantic-trace [0 :capabilities])))
    (is (= '([resolution-artifact])
           (:arglists (meta #'bootstrap/sh07-core-lineage))))
    (is (thrown? clojure.lang.ArityException
                 (bootstrap/sh07-core-lineage artifact [])))
    (is (= lineage (bootstrap/sh07-core-lineage artifact)))
    (is (= (bootstrap/reader-canonical-hash lineage)
           (bootstrap/reader-canonical-hash
            (bootstrap/sh07-core-lineage artifact))))))
