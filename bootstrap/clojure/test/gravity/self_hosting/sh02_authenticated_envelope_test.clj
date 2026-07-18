(ns gravity.self-hosting.sh02-authenticated-envelope-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh02_authenticated_envelope_test.clj")]
    (when-not resource
      (throw (ex-info "SH-02 test source is not on the classpath"
                      {:id "SH02-TEST-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH02-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve path "deps.edn")))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private envelope-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-02")

(def ^:private envelope-bounds
  {:maximum-semantic-projections 64
   :maximum-fact-transitions 64
   :maximum-identity-subjects 64
   :maximum-lineage-records 32
   :maximum-reference-nodes 128
   :maximum-reference-edges 128
   :maximum-reference-depth 64
   :maximum-logical-source-path-code-units 128
   :maximum-reference-id-code-units 128
   :maximum-digest-requests 2048
   :maximum-carrier-nodes 65536
   :maximum-carrier-depth 64
   :maximum-container-width 128
   :maximum-scalar-bytes 65536
   :maximum-integer-bits 256})

(def ^:private fact-families
  [:type :effect :ownership :capability :safety
   :runtime-checks :capability-proofs :proofs :source-map :effect-order])

(defn- compile-plan
  [source-path target]
  (let [source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path target))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private envelope-plan
  (delay (compile-plan (path envelope-source-relative-path) :jvm)))

(defn- invoke-envelope
  [function-name arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :sh02-authenticated-envelope-leaf
    :compiler-artifact-plan? true}
   @envelope-plan function-name arguments))

(defn- canonical-id
  [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh02-authenticated-envelope-test>" value))

(defn- identity-id
  [domain preimage]
  (canonical-id {:domain domain :semantic-input preimage}))

(defn- source-hash
  [source-text]
  (str "sha256:" (bootstrap/sha256-hex source-text)))

(defn- byte-count
  [source-text]
  (alength (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)))

(defn- plan-semantic-id
  [plan]
  (bootstrap/p15-s23-c11-mir-digest
   (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input plan)))

(defn- function-shapes
  [plan]
  (into (sorted-map)
        (map (fn [[name function]]
               [name (select-keys function [:arity :params])]))
        (:functions plan)))

(defn- fact-transition
  [family evidence-id]
  (let [value {:family family :entries []}]
    {:name family
     :disposition :preserved
     :input value
     :output value
     :input-count (count value)
     :output-count (count value)
     :evidence-ids [evidence-id]}))

(defn- fixture-case
  [relative]
  (let [source-path (path (str fixture-root "/" relative))
        source-text (slurp source-path)
        plan (compile-plan source-path :jvm)]
    {:source-path source-path
     :source-text source-text
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan plan
     :plan-semantic-hash (plan-semantic-id plan)
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions plan))
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions plan) 'main))
     :function-shapes (function-shapes plan)}))

(defn- descriptor
  [{:keys [source-path source-content-hash source-byte-count
           plan-semantic-hash functions-semantic-hash
           builder-semantic-hash function-shapes] :as fixture}]
  (let [projection-names [:stage-record :reference-graph]
        identity-names [:fixture-source :fixture-plan]
        evidence-id
        (canonical-id
         {:domain :gravity/sh02-evidence-v1
          :source-content-hash source-content-hash
          :plan-semantic-hash plan-semantic-hash})
        source-preimage
        {:source-content-hash source-content-hash
         :source-byte-count source-byte-count}
        plan-preimage
        {:source-content-hash source-content-hash
         :plan-semantic-hash plan-semantic-hash
         :functions-semantic-hash functions-semantic-hash}
        stage-record
        {:source-content-hash source-content-hash
         :plan-semantic-hash plan-semantic-hash
         :functions-semantic-hash functions-semantic-hash
         :builder-semantic-hash builder-semantic-hash}
        reference-graph
        {:root-id "module"
         :node-ids ["module" "main" "return"]
         :edges [{:from "module" :role :contains :to "main"}
                 {:from "main" :role :returns :to "return"}]}
        facts (mapv #(fact-transition % evidence-id) fact-families)]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh02-fixture
     :artifact-kind :gravity/sh02-fixture-plan
     :source-revision
     {:owner :sh-envelope
      :source-language :gravity
      :logical-source-path "self-hosting/sh-02/envelope-comparison"
      :source-content-hash source-content-hash
      :source-byte-count source-byte-count
      :plan-semantic-hash plan-semantic-hash
      :functions-semantic-hash functions-semantic-hash
      :builder-function 'main
      :builder-semantic-hash builder-semantic-hash
      :function-shapes function-shapes}
     :projection-contract
     {:contract-kind :gravity/sh02-fixture-envelope-contract
      :contract-version 1
      :profile :hosted
      :target :jvm
      :required-semantic-projections projection-names
      :required-fact-families fact-families
      :required-identity-subjects identity-names}
     :semantic-projections
     [{:name :stage-record
       :role :complete-stage-projection
       :entry-count (count stage-record)
       :value stage-record}
      {:name :reference-graph
       :role :explicit-reference-closure-source
       :entry-count (count reference-graph)
       :value reference-graph}]
     :fact-transitions facts
     :effect-capability-relation
     {:effect-facts {:declared #{} :observed #{}}
      :capability-facts {:required #{} :granted #{}}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records [{:proof-id evidence-id :status :checked}]
      :proof-certificate-table {evidence-id {:status :checked}}
      :proof-summary {:required 1 :checked 1}
      :proof-usage [{:proof-id evidence-id :used-by :fixture-plan}]}
     :preservation
     {:requires fact-families
      :preserves fact-families
      :invalidates []
      :regenerates []
      :residual-checks [:identity-subject-equality
                        :digest-graph-reachability]}
     :identity-subjects
     [{:name :fixture-source
       :domain :gravity/sh02-fixture-source-v1
       :preimage source-preimage
       :observed-id
       (identity-id :gravity/sh02-fixture-source-v1 source-preimage)}
      {:name :fixture-plan
       :domain :gravity/sh02-fixture-plan-v1
       :preimage plan-preimage
       :observed-id
       (identity-id :gravity/sh02-fixture-plan-v1 plan-preimage)}]
     :lineage
     [{:stage :stage2-plan
       :artifact-kind :gravity/stage2-compiler-artifact-plan
       :semantic-id plan-semantic-hash
       :artifact-id (canonical-id {:plan plan-semantic-hash})
       :verification-id evidence-id
       :relation :produced-from-source}]
     :reference-closure
     {:root-id "module"
      :node-ids ["module" "main" "return"]
      :edges [{:from "module" :role :contains :to "main"}
              {:from "main" :role :returns :to "return"}]
      :fact-reference-ids ["fact/type" "fact/effect"]
      :origin-reference-ids ["origin/main"]
      :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids []
      :observed-node-count 3
      :observed-edge-count 2
      :observed-maximum-depth 2}
     :actual-path-provenance
     {:source-path source-path
      :workspace-root (str @root)
      :invocation-root (System/getProperty "user.dir")}
     :bounds envelope-bounds}))

(defn- digest-ref-ordinal
  [value]
  (when-not (and (map? value)
                 (= #{:digest-ref} (set (keys value)))
                 (nat-int? (:digest-ref value)))
    (throw (ex-info "Invalid SH-02 digest reference"
                    {:id "SH02-DIGEST-REFERENCE" :value value})))
  (:digest-ref value))

(defn- reachable-ordinals
  [requests roots]
  (loop [pending (mapv digest-ref-ordinal roots)
         reachable #{}]
    (if (empty? pending)
      reachable
      (let [ordinal (peek pending)
            pending (pop pending)]
        (if (contains? reachable ordinal)
          (recur pending reachable)
          (recur (into pending (:depends-on (get requests ordinal)))
                 (conj reachable ordinal)))))))

(defn- seal-builder-result!
  [raw-result]
  (let [requests (:digest-requests raw-result)
        request-count (count requests)
        roots (:digest-graph-roots raw-result)]
    (when-not (and (= :accepted (:status raw-result))
                   (vector? requests)
                   (pos? request-count)
                   (<= request-count 2048)
                   (= 2 (count roots))
                   (= (:semantic-envelope-root raw-result) (first roots))
                   (= (:provenance-binding-root raw-result) (second roots)))
      (throw (ex-info "Invalid SH-02 builder envelope"
                      {:id "SH02-BUILDER-ENVELOPE"})))
    (doseq [[ordinal request] (map-indexed vector requests)]
      (let [references
            (bootstrap/p15-s23-c6c10-collect-digest-ref-ordinals!
             "<sh02-builder-result>" (:preimage request)
             request-count ordinal)]
        (when-not (and (= #{:algorithm :depends-on :encoding :key
                            :ordinal :preimage}
                          (set (keys request)))
                       (= ordinal (:key request) (:ordinal request))
                       (= :sha256 (:algorithm request))
                       (= :gravity/canonical-edn-v1 (:encoding request))
                       (= (:depends-on request)
                          (vec (sort (distinct (:depends-on request)))))
                       (= (set references) (set (:depends-on request)))
                       (every? #(< % ordinal) (:depends-on request)))
          (throw (ex-info "Invalid SH-02 digest request graph"
                          {:id "SH02-DIGEST-GRAPH"
                           :ordinal ordinal})))))
    (when-not (= (set (range request-count))
                 (reachable-ordinals requests roots))
      (throw (ex-info "Unreachable SH-02 digest request"
                      {:id "SH02-DIGEST-REACHABILITY"})))
    (let [resolved-digests
          (reduce
           (fn [resolved request]
             (let [ordinal (:ordinal request)
                   resolved-preimage
                   (bootstrap/p15-s23-c6c10-resolve-digest-references!
                    "<sh02-builder-result>" (:preimage request)
                    request-count ordinal resolved)]
               (conj resolved
                     (canonical-id resolved-preimage))))
           [] requests)
          resolve-final
          (fn [value]
            (bootstrap/p15-s23-c6c10-resolve-digest-references!
             "<sh02-builder-result>" value request-count nil
             resolved-digests))
          resolved-checks (resolve-final (:identity-checks raw-result))
          mismatches
          (filterv #(not= (:computed-id %) (:observed-id %))
                   resolved-checks)]
      (when (seq mismatches)
        (throw (ex-info "SH-02 identity subject does not match"
                        {:id "SH02-IDENTITY-SUBJECT"
                         :subject-names (mapv :name mismatches)})))
      {:template (resolve-final (:artifact-template raw-result))
       :semantic-root (resolve-final (:semantic-envelope-root raw-result))
       :provenance-root
       (resolve-final (:provenance-binding-root raw-result))
       :roots (resolve-final roots)
       :requests requests
       :resolved-digests resolved-digests
       :identity-checks resolved-checks})))

(deftest gravity-leaf-compiles-with-the-settled-function-contract
  (let [functions (:functions @envelope-plan)]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @envelope-plan)))
    (is (= {:arity 1 :params ['descriptor]}
           (select-keys
            (get functions 'authenticated-envelope-build-template)
            [:arity :params])))
    (is (= {:arity 3
            :params ['descriptor 'artifact-template 'digest-requests]}
           (select-keys
            (get functions 'authenticated-envelope-verify-template)
            [:arity :params])))
    (is (= 72 (count functions)))
    (is (= #{} (get-in @envelope-plan [:module :effects])))
    (is (= #{} (get-in @envelope-plan [:module :capabilities])))
    (is (= :safe (get-in @envelope-plan [:module :safety])))))

(deftest co-canonical-fixtures-produce-one-semantic-root
  (let [gravity-case (fixture-case "accepted/envelope-comparison.gravity")
        qst-case (fixture-case "accepted/envelope-comparison.qst")
        gravity-descriptor (descriptor gravity-case)
        qst-descriptor (descriptor qst-case)
        gravity-raw
        (invoke-envelope 'authenticated-envelope-build-template
                         [gravity-descriptor])
        qst-raw
        (invoke-envelope 'authenticated-envelope-build-template
                         [qst-descriptor])
        gravity-sealed (seal-builder-result! gravity-raw)
        qst-sealed (seal-builder-result! qst-raw)
        gravity-verified
        (invoke-envelope
         'authenticated-envelope-verify-template
         [gravity-descriptor (:artifact-template gravity-raw)
          (:digest-requests gravity-raw)])
        qst-verified
        (invoke-envelope
         'authenticated-envelope-verify-template
         [qst-descriptor (:artifact-template qst-raw)
          (:digest-requests qst-raw)])]
    (is (= (:source-text gravity-case) (:source-text qst-case)))
    (is (= (:source-content-hash gravity-case)
           (:source-content-hash qst-case)))
    (is (= (:plan-semantic-hash gravity-case)
           (:plan-semantic-hash qst-case)))
    (is (= :accepted (:status gravity-raw) (:status qst-raw)))
    (is (= :template-replay-passed
           (:status gravity-verified) (:status qst-verified)))
    (is (= :pending-host-resolution
           (:identity-enforcement gravity-verified)
           (:identity-enforcement qst-verified)))
    (is (false? (:eligible-for-contextual-acceptance? gravity-verified)))
    (is (= (:semantic-root gravity-sealed)
           (:semantic-root qst-sealed)))
    (is (= (get-in gravity-sealed [:template :semantic-id])
           (get-in qst-sealed [:template :semantic-id])))
    (is (not= (:provenance-root gravity-sealed)
              (:provenance-root qst-sealed)))
    (is (= (:source-path gravity-case)
           (get-in gravity-sealed
                   [:template :actual-path-provenance :source-path])))
    (is (= (:source-path qst-case)
           (get-in qst-sealed
                   [:template :actual-path-provenance :source-path])))
    (is (= (:identity-checks gravity-sealed)
           (:identity-checks qst-sealed)))
    (is (= (:roots gravity-sealed)
           [(:semantic-root gravity-sealed)
            (:provenance-root gravity-sealed)]))))

(deftest semantic-and-provenance-roots-have-separate-dependencies
  (let [fixture (fixture-case "accepted/envelope-comparison.gravity")
        descriptor (descriptor fixture)
        raw (invoke-envelope 'authenticated-envelope-build-template
                             [descriptor])
        relocated-descriptor
        (assoc descriptor :actual-path-provenance
               {:source-path "/tmp/sh02-mirror/envelope-comparison.gravity"
                :workspace-root "/tmp/sh02-mirror"
                :invocation-root "/tmp/sh02-mirror"})
        relocated-raw
        (invoke-envelope 'authenticated-envelope-build-template
                         [relocated-descriptor])
        sealed (seal-builder-result! raw)
        relocated-sealed (seal-builder-result! relocated-raw)
        requests (:digest-requests raw)
        semantic-ordinal
        (digest-ref-ordinal (:semantic-envelope-root raw))
        provenance-ordinal
        (digest-ref-ordinal (:provenance-binding-root raw))
        semantic-request (get requests semantic-ordinal)
        provenance-request (get requests provenance-ordinal)
        semantic-text (pr-str (:preimage semantic-request))
        actual-path (get-in descriptor [:actual-path-provenance :source-path])]
    (is (= :gravity/authenticated-envelope-semantic-root-v1
           (get-in semantic-request [:preimage :domain])))
    (is (= :gravity/authenticated-envelope-provenance-binding-v1
           (get-in provenance-request [:preimage :domain])))
    (is (not (str/includes? semantic-text actual-path)))
    (is (not (str/includes? semantic-text (str @root))))
    (is (= (:semantic-envelope-root raw)
           (get-in provenance-request
                   [:preimage :semantic-envelope-id])))
    (is (some #{semantic-ordinal} (:depends-on provenance-request)))
    (is (< semantic-ordinal provenance-ordinal))
    (is (= (:semantic-root sealed) (:semantic-root relocated-sealed)))
    (is (not= (:provenance-root sealed)
              (:provenance-root relocated-sealed)))))

(deftest changed-products-and-stale-carriers-fail-closed
  (let [accepted (fixture-case "accepted/envelope-comparison.gravity")
        changed-gravity
        (fixture-case "rejected/mismatched-projection.gravity")
        changed-qst
        (fixture-case "rejected/mismatched-projection.qst")
        accepted-descriptor (descriptor accepted)
        changed-descriptor (descriptor changed-gravity)
        accepted-raw
        (invoke-envelope 'authenticated-envelope-build-template
                         [accepted-descriptor])
        replay-rejection
        (invoke-envelope
         'authenticated-envelope-verify-template
         [changed-descriptor (:artifact-template accepted-raw)
          (:digest-requests accepted-raw)])
        request-rejection
        (invoke-envelope
         'authenticated-envelope-verify-template
         [accepted-descriptor (:artifact-template accepted-raw)
          (assoc-in (:digest-requests accepted-raw)
                    [0 :preimage :projection-contract :target]
                    :wasm)])
        stale-identity-descriptor
        (assoc-in
         accepted-descriptor
         [:identity-subjects 0 :preimage :source-content-hash]
         (:source-content-hash changed-gravity))
        stale-identity-raw
        (invoke-envelope 'authenticated-envelope-build-template
                         [stale-identity-descriptor])
        stale-identity-verification
        (invoke-envelope
         'authenticated-envelope-verify-template
         [stale-identity-descriptor
          (:artifact-template stale-identity-raw)
          (:digest-requests stale-identity-raw)])
        identity-exception
        (try
          (seal-builder-result! stale-identity-raw)
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= (:source-text changed-gravity) (:source-text changed-qst)))
    (is (not= (:source-content-hash accepted)
              (:source-content-hash changed-gravity)))
    (is (= :rejected (:status replay-rejection)))
    (is (= :artifact-template-replay
           (get-in replay-rejection [:diagnostics 0 :reason])))
    (is (= :rejected (:status request-rejection)))
    (is (= :digest-request-replay
           (get-in request-rejection [:diagnostics 0 :reason])))
    (is (= :accepted (:status stale-identity-raw)))
    (is (= :template-replay-passed
           (:status stale-identity-verification)))
    (is (= :pending-host-resolution
           (:identity-enforcement stale-identity-verification)))
    (is (false?
         (:eligible-for-contextual-acceptance?
          stale-identity-verification)))
    (is (= "SH02-IDENTITY-SUBJECT" (:id (ex-data identity-exception))))))

(deftest invalid-fact-transitions-and-physical-semantic-values-are-rejected
  (let [fixture (fixture-case "accepted/envelope-comparison.gravity")
        base (descriptor fixture)
        changed-output
        (assoc-in base [:fact-transitions 0 :output :entries]
                  [:changed])
        changed-output
        (assoc-in changed-output [:fact-transitions 0 :output-count] 2)
        path-bearing
        (assoc-in base [:semantic-projections 0 :value :source-path]
                  "/tmp/substituted/source.gravity")
        path-bearing
        (assoc-in path-bearing [:semantic-projections 0 :entry-count] 5)
        absolute-logical-path
        (assoc-in base [:source-revision :logical-source-path]
                  "/tmp/not-a-logical-path.gravity")
        fact-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [changed-output])
        path-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [path-bearing])
        logical-path-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [absolute-logical-path])]
    (is (= :rejected (:status fact-result)))
    (is (= :fact-transitions
           (get-in fact-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status path-result)))
    (is (= :semantic-projections
           (get-in path-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status logical-path-result)))
    (is (= :source-revision
           (get-in logical-path-result [:diagnostics 0 :reason])))
    (is (true? (get-in path-result
                       [:containment :downstream-artifacts-forbidden])))))

(deftest external-digest-reference-shapes-never-alias-the-internal-dag
  (let [fixture (fixture-case "accepted/envelope-comparison.gravity")
        base (descriptor fixture)
        prior-alias
        (-> base
            (assoc-in [:semantic-projections 0 :value] {:digest-ref 0})
            (assoc-in [:semantic-projections 0 :entry-count] 1))
        future-alias
        (-> base
            (assoc-in [:semantic-projections 0 :value]
                      {:digest-ref 2047})
            (assoc-in [:semantic-projections 0 :entry-count] 1))
        nested-alias
        (assoc-in base
                  [:identity-subjects 0 :preimage :nested]
                  {:digest-ref 1})
        function-shape-alias
        (assoc-in base
                  [:source-revision :function-shapes 'main :digest]
                  {:digest-ref 2})
        results
        (mapv #(invoke-envelope
                'authenticated-envelope-build-template [%])
              [prior-alias future-alias nested-alias function-shape-alias])]
    (is (= [:rejected :rejected :rejected :rejected]
           (mapv :status results)))
    (is (= [:semantic-projections :semantic-projections
            :identity-subjects :source-revision]
           (mapv #(get-in % [:diagnostics 0 :reason]) results)))
    (is (every? #(true?
                  (get-in %
                          [:containment :downstream-artifacts-forbidden]))
                results))))

(deftest path-channels-and-reference-closure-are-recomputed
  (let [fixture (fixture-case "accepted/envelope-comparison.gravity")
        base (descriptor fixture)
        parent-segment
        (assoc-in base [:source-revision :logical-source-path]
                  "../checkout/module.gravity")
        nested-physical-path
        (assoc-in base
                  [:source-revision :function-shapes 'main :source-path]
                  "/tmp/checkout/module.gravity")
        absolute-reference
        (assoc base :reference-closure
               {:root-id "/tmp/checkout/module"
                :node-ids ["/tmp/checkout/module" "main" "return"]
                :edges
                [{:from "/tmp/checkout/module"
                  :role :contains :to "main"}
                 {:from "main" :role :returns :to "return"}]
                :fact-reference-ids ["fact/type"]
                :origin-reference-ids ["origin/main"]
                :proof-reference-ids
                (get-in base
                        [:reference-closure :proof-reference-ids])
                :runtime-check-reference-ids []
                :observed-node-count 3
                :observed-edge-count 2
                :observed-maximum-depth 2})
        disconnected
        (-> base
            (assoc-in [:reference-closure :node-ids]
                      ["module" "main" "return" "orphan"])
            (assoc-in [:reference-closure :observed-node-count] 4))
        wrong-depth
        (assoc-in base
                  [:reference-closure :observed-maximum-depth] 1)
        cyclic
        (-> base
            (update-in [:reference-closure :edges]
                       conj {:from "return" :role :loops-to :to "module"})
            (assoc-in [:reference-closure :observed-edge-count] 3))
        rejected
        (mapv #(invoke-envelope
                'authenticated-envelope-build-template [%])
              [parent-segment nested-physical-path absolute-reference
               disconnected wrong-depth])
        cyclic-result
        (invoke-envelope 'authenticated-envelope-build-template [cyclic])]
    (is (= [:rejected :rejected :rejected :rejected :rejected]
           (mapv :status rejected)))
    (is (= [:source-revision :source-revision
            :reference-closure :reference-closure :reference-closure]
           (mapv #(get-in % [:diagnostics 0 :reason]) rejected)))
    (is (= :accepted (:status cyclic-result)))
    (is (= :ordered-shortest-discovery-depth
           (get-in cyclic-result
                   [:artifact-template :reference-depth-metric])))
    (is (= :template-replay-passed
           (:status
            (invoke-envelope
             'authenticated-envelope-verify-template
             [cyclic (:artifact-template cyclic-result)
              (:digest-requests cyclic-result)]))))))

(deftest recursive-leaf-bounds-accept-the-maximum-and-reject-the-next-value
  (let [fixture (fixture-case "accepted/envelope-comparison.gravity")
        base (descriptor fixture)
        node-ids
        (into ["root"] (mapv #(str "node-" %) (range 1 128)))
        edges
        (mapv (fn [node-id]
                {:from "root" :role :contains :to node-id})
              (subvec node-ids 1 128))
        exact-closure
        {:root-id "root"
         :node-ids node-ids
         :edges edges
         :fact-reference-ids []
         :origin-reference-ids []
         :proof-reference-ids []
         :runtime-check-reference-ids []
         :observed-node-count 128
         :observed-edge-count 127
         :observed-maximum-depth 1}
        exact-graph
        (assoc base :reference-closure exact-closure)
        next-graph
        (assoc base :reference-closure
               (-> exact-closure
                   (update :node-ids conj "node-128")
                   (update :edges conj
                           {:from "root" :role :contains :to "node-128"})
                   (assoc :observed-node-count 129)
                   (assoc :observed-edge-count 128)))
        deep-outside-graph
        (assoc base :reference-closure
               (assoc exact-closure
                      :node-ids
                      (mapv #(str "wide-node-" %) (range 3000))
                      :edges []
                      :root-id "wide-node-0"
                      :observed-node-count 3000
                      :observed-edge-count 0
                      :observed-maximum-depth 0))
        exact-path
        (assoc-in base [:source-revision :logical-source-path]
                  (apply str (repeat 128 "a")))
        next-path
        (assoc-in base [:source-revision :logical-source-path]
                  (apply str (repeat 129 "a")))
        deep-outside-path
        (assoc-in base [:source-revision :logical-source-path]
                  (apply str (repeat 3000 "a")))
        exact-graph-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [exact-graph])
        next-graph-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [next-graph])
        deep-graph-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [deep-outside-graph])
        exact-path-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [exact-path])
        next-path-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [next-path])
        deep-path-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [deep-outside-path])]
    (is (= :accepted (:status exact-graph-result)))
    (is (= :rejected (:status next-graph-result)))
    (is (= :reference-closure
           (get-in next-graph-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status deep-graph-result)))
    (is (= :reference-closure
           (get-in deep-graph-result [:diagnostics 0 :reason])))
    (is (= :accepted (:status exact-path-result)))
    (is (= :rejected (:status next-path-result)))
    (is (= :source-revision
           (get-in next-path-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status deep-path-result)))
    (is (= :source-revision
           (get-in deep-path-result [:diagnostics 0 :reason])))))

(deftest one-leaf-contract-builds-distinct-c13-and-b1-envelopes
  (let [fixture (fixture-case "accepted/envelope-comparison.gravity")
        base (descriptor fixture)
        c13-descriptor
        (assoc base
               :stage :c13-optimize-mir
               :artifact-kind :gravity/c13-optimized-mir)
        b1-descriptor
        (assoc base
               :stage :b1-backend-interface
               :artifact-kind :gravity/b1-backend-packet)
        c13-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [c13-descriptor])
        b1-result
        (invoke-envelope 'authenticated-envelope-build-template
                         [b1-descriptor])
        c13-sealed (seal-builder-result! c13-result)
        b1-sealed (seal-builder-result! b1-result)]
    (is (= :accepted (:status c13-result) (:status b1-result)))
    (is (= :c13-optimize-mir
           (get-in c13-result [:artifact-template :stage])))
    (is (= :b1-backend-interface
           (get-in b1-result [:artifact-template :stage])))
    (is (not= (:semantic-root c13-sealed)
              (:semantic-root b1-sealed)))
    (is (= :gravity/c13-optimized-mir
           (get-in c13-sealed [:template :artifact-kind])))
    (is (= :gravity/b1-backend-packet
           (get-in b1-sealed [:template :artifact-kind])))))

(deftest every-authority-bearing-family-changes-the-semantic-root
  (let [fixture (fixture-case "accepted/envelope-comparison.gravity")
        base (descriptor fixture)
        base-result
        (invoke-envelope 'authenticated-envelope-build-template [base])
        repeated-result
        (invoke-envelope 'authenticated-envelope-build-template [base])
        base-root (:semantic-root (seal-builder-result! base-result))
        variants
        [(assoc-in base
                   [:effect-capability-relation :provider-selections]
                   [:fixture-provider])
         (assoc-in base [:proof-composite :proof-summary :checked] 2)
         (assoc-in base [:lineage 0 :relation]
                   :verified-from-source)
         (assoc-in base [:reference-closure :edges 0 :role]
                   :owns)
         (update-in base [:preservation :residual-checks]
                    conj :contextual-consistency)]
        variant-results
        (mapv #(invoke-envelope
                'authenticated-envelope-build-template [%])
              variants)
        variant-roots
        (mapv #(-> % seal-builder-result! :semantic-root)
              variant-results)
        replay-results
        (mapv
         (fn [variant]
           (invoke-envelope
            'authenticated-envelope-verify-template
            [variant (:artifact-template base-result)
             (:digest-requests base-result)]))
         variants)]
    (is (= base-result repeated-result))
    (is (= :accepted (:status base-result)))
    (is (= [:accepted :accepted :accepted :accepted :accepted]
           (mapv :status variant-results)))
    (is (every? #(not= base-root %) variant-roots))
    (is (= 5 (count (distinct variant-roots))))
    (is (= [:rejected :rejected :rejected :rejected :rejected]
           (mapv :status replay-results)))
    (is (= [:digest-request-replay :digest-request-replay
            :digest-request-replay :digest-request-replay
            :digest-request-replay]
           (mapv #(get-in % [:diagnostics 0 :reason])
                 replay-results)))))

(def ^:private coordinator-straight-line-source
  (str
   "(ns self-hosting.sh02.straight "
   "(:profile :hosted) (:target :jvm) (:safety :safe) "
   "(:effects #{}) (:capabilities #{}) (:exports [main]))\n"
   "(defn main [] 7)\n"))

(def ^:private coordinator-conditional-source
  (str
   "(ns self-hosting.sh02.conditional "
   "(:profile :hosted) (:target :jvm) (:safety :safe) "
   "(:effects #{}) (:capabilities #{}) (:exports [main]))\n"
   "(defn main []\n"
   "  (let [x 7]\n"
   "    (if (= x 7) (do x) 9)))\n"))

(defn- with-integration-roots
  [f]
  (let [left
        (java.nio.file.Files/createTempDirectory
         "gravity-sh02-integration-left-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        right
        (java.nio.file.Files/createTempDirectory
         "gravity-sh02-integration-right-"
         (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (f left right)
      (finally
        (doseq [directory [left right]
                file (reverse (file-seq (.toFile directory)))]
          (io/delete-file file true))))))

(defn- coordinator-integration-row
  [root label extension source]
  (let [file (.resolve root (str (name label) extension))
        _
        (java.nio.file.Files/writeString
         file source (make-array java.nio.file.OpenOption 0))
        source-path
        (.toString
         (.toRealPath file (make-array java.nio.file.LinkOption 0)))
        context
        (bootstrap/p15-s23-stage2-gravity-checked-core-context
         source-path source :llvm)
        checked-core
        (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
         context)
        c11
        (bootstrap/p15-s23-stage2-c11-mir-artifact
         checked-core context)
        packet
        (bootstrap/p15-s23-stage2-c13-c14-b1-packet-from-c11!
         c11 checked-core context)
        artifact
        (bootstrap/p15-s23-stage2-sh02-authenticated-envelope-artifact
         packet checked-core context)]
    {:label label
     :extension extension
     :source-path source-path
     :source source
     :context context
     :checked-core checked-core
     :c11 c11
     :packet packet
     :artifact artifact
     :trusted-carrier
     (bootstrap/p15-s23-trusted-carrier-validation
      artifact :default-only
      (:maximum-carrier-nodes envelope-bounds)
      (:maximum-carrier-depth envelope-bounds)
      (:maximum-digest-requests envelope-bounds))}))

(defn- structured-diagnostic
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(defn- coordinator-integration-proof
  []
  (binding
   [bootstrap/*p15-s23-c11-mir-diagnostic-context*
    {:requested-target :llvm}
    bootstrap/*additional-bootstrap-targets*
    bootstrap/stage2-runtime-derived-source-targets]
   (with-integration-roots
     (fn [left right]
       (let [before
             (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)
             straight-gravity
             (coordinator-integration-row
              left :straight ".gravity" coordinator-straight-line-source)
             straight-qst
             (coordinator-integration-row
              right :straight ".qst" coordinator-straight-line-source)
             conditional-gravity
             (coordinator-integration-row
              left :conditional ".gravity" coordinator-conditional-source)
             conditional-qst
             (coordinator-integration-row
              right :conditional ".qst" coordinator-conditional-source)
             repeated-artifact
             (bootstrap/p15-s23-stage2-sh02-authenticated-envelope-artifact
              (:packet straight-gravity)
              (:checked-core straight-gravity)
              (:context straight-gravity))
             straight-report
             (bootstrap/p15-s23-stage2-sh02-authenticated-envelope-verification-report
              (:artifact straight-gravity)
              (:checked-core straight-gravity)
              (:context straight-gravity))
             straight-verify
             (bootstrap/p15-s23-stage2-sh02-authenticated-envelope-verify!
              (:artifact straight-qst)
              (:checked-core straight-qst)
              (:context straight-qst))
             conditional-authentic?
             (bootstrap/p15-s23-stage2-sh02-authenticated-envelope-authentic?
              (:artifact conditional-qst)
              (:checked-core conditional-qst)
              (:context conditional-qst))
             substitute-id
             (canonical-id
              {:domain :gravity/sh02-substituted-projection-v1
               :semantic-input :different-projection})
             mismatched-projection
             (assoc-in
              (:artifact straight-gravity)
              [:envelopes :c13 :sealed-artifact
               :semantic-projection-bindings 0 :content-id]
              substitute-id)
             mismatch-diagnostic
             (structured-diagnostic
              #(bootstrap/p15-s23-stage2-sh02-authenticated-envelope-verification-report
                mismatched-projection
                (:checked-core straight-gravity)
                (:context straight-gravity)))
             repeated-mismatch-diagnostic
             (structured-diagnostic
              #(bootstrap/p15-s23-stage2-sh02-authenticated-envelope-verification-report
                mismatched-projection
                (:checked-core straight-gravity)
                (:context straight-gravity)))
             stale-authentic?
             (bootstrap/p15-s23-stage2-sh02-authenticated-envelope-authentic?
              (:artifact straight-gravity)
              (:checked-core conditional-gravity)
              (:context conditional-gravity))
             after
             (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)]
         {:rows
          {:straight-gravity straight-gravity
           :straight-qst straight-qst
           :conditional-gravity conditional-gravity
           :conditional-qst conditional-qst}
          :repeated-artifact repeated-artifact
          :straight-report straight-report
          :straight-verify straight-verify
          :conditional-authentic? conditional-authentic?
          :mismatched-projection mismatched-projection
          :mismatch-diagnostic mismatch-diagnostic
          :repeated-mismatch-diagnostic repeated-mismatch-diagnostic
          :stale-authentic? stale-authentic?
          :before before
          :after after})))))

(def ^:private coordinator-proof
  (delay (coordinator-integration-proof)))

(deftest genuine-c13-and-b1-packets-use-two-reusable-stage-envelopes
  (let [{:keys [rows straight-report straight-verify
                conditional-authentic? before after]}
        @coordinator-proof]
    (is (= before after))
    (is (= :passed (:status straight-report)))
    (is (= :passed straight-verify))
    (is (true? conditional-authentic?))
    (doseq [[label {:keys [artifact trusted-carrier]}] rows]
      (is (= :accepted (:status artifact)) label)
      (is (= #{:c13 :b1} (set (keys (:envelopes artifact)))) label)
      (is (= :passed (:status trusted-carrier)) label)
      (is (<= (:observed-nodes trusted-carrier)
              (:maximum-carrier-nodes envelope-bounds)) label)
      (is (<= (:observed-depth trusted-carrier)
              (:maximum-carrier-depth envelope-bounds)) label)
      (is (false?
           (bootstrap/p15-s23-stage2-sh02-authenticated-envelope-authentic?
            artifact)) label)
      (doseq [stage [:c13 :b1]]
        (let [stage-envelope (get-in artifact [:envelopes stage])
              replay (:gravity-template-replay stage-envelope)]
          (is (= :gravity/sh02-stage-authenticated-envelope
                 (:artifact stage-envelope)) [label stage])
          (is (= :accepted (:status stage-envelope)) [label stage])
          (is (= :template-replay-passed (:status replay)) [label stage])
          (is (= :pending-host-resolution
                 (:identity-enforcement replay)) [label stage])
          (is (false?
               (:eligible-for-contextual-acceptance? replay))
              [label stage]))))
    (is (= :gravity/sh02-contextual-verification-report
           (:artifact straight-report)))
    (is (= :template-replay-passed
           (:gravity-envelope-template-replay straight-report)))
    (is (= :passed (:host-digest-resolution straight-report)))
    (is (= :passed (:identity-subject-equality straight-report)))
    (is (= :passed (:fresh-envelope-reconstruction straight-report)))
    (is (= :not-performed
           (:external-tool-execution straight-report)))))

(deftest coordinator-envelope-identities-are-repeatable-and-path-separated
  (let [{:keys [rows repeated-artifact]} @coordinator-proof
        straight-gravity (get rows :straight-gravity)
        straight-qst (get rows :straight-qst)
        conditional-gravity (get rows :conditional-gravity)
        conditional-qst (get rows :conditional-qst)
        pairs [[straight-gravity straight-qst]
               [conditional-gravity conditional-qst]]]
    (is (= (:artifact straight-gravity) repeated-artifact))
    (doseq [[left right] pairs]
      (is (= (get-in left [:artifact :semantic-id])
             (get-in right [:artifact :semantic-id])))
      (is (= (get-in left [:artifact :artifact-id])
             (get-in right [:artifact :artifact-id])))
      (is (not= (get-in left [:artifact :actual-path-binding-id])
                (get-in right [:artifact :actual-path-binding-id])))
      (doseq [stage [:c13 :b1]]
        (is (= (get-in left
                       [:artifact :envelopes stage :semantic-envelope-id])
               (get-in right
                       [:artifact :envelopes stage :semantic-envelope-id])))
        (is (= (get-in left
                       [:artifact :envelopes stage :provenance-binding-id])
               (get-in right
                       [:artifact :envelopes stage :provenance-binding-id])))))
    (is (not= (get-in straight-gravity [:artifact :semantic-id])
              (get-in conditional-gravity [:artifact :semantic-id])))))

(deftest contextual-verification-rejects-mismatched-and-stale-carriers
  (let [{:keys [rows mismatched-projection mismatch-diagnostic
                repeated-mismatch-diagnostic stale-authentic?]}
        @coordinator-proof
        straight (get rows :straight-gravity)]
    (is (= :gravity/diagnostic (:artifact mismatch-diagnostic)))
    (is (= (:id mismatch-diagnostic) (:rule mismatch-diagnostic)))
    (is (keyword? (get-in mismatch-diagnostic [:facts :missing-fact])))
    (is (not (str/includes? (pr-str mismatch-diagnostic)
                            "ClassCastException")))
    (is (not (str/includes? (pr-str mismatch-diagnostic)
                            "StackOverflowError")))
    (is (= mismatch-diagnostic repeated-mismatch-diagnostic))
    (is (false? stale-authentic?))
    (is (not= (get-in mismatched-projection
                      [:envelopes :c13 :sealed-artifact
                       :semantic-projection-bindings 0 :content-id])
              (get-in (:artifact straight)
                      [:envelopes :c13 :sealed-artifact
                       :semantic-projection-bindings 0 :content-id])))))
