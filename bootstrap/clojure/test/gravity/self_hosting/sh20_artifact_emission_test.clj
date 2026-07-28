(ns gravity.self-hosting.sh20-artifact-emission-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh20_artifact_emission_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-20 test source is not on the classpath"
                {:id "SH20-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH20-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-20")

(defn- fixture-relative-path
  [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay (compile-plan (str fixture-root "/artifact_bundle_engine.gravity"))))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "artifact-bundles" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "artifact-bundles" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-artifact-bundles" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-artifact-bundles" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh20-artifact-emission-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine
  [function arguments]
  (invoke engine-plan function arguments))

(defn- request
  [plan function]
  (invoke plan function []))

(defn- build
  [request]
  (invoke-engine 'sh20-build-bundle [request]))

(def ^:private rejected-cases
  {'sh20-schema-gap-request ["B13-SCHEMA" :schema-gap]
   'sh20-content-hash-gap-request ["B13-HASH" :content-hash-gap]
   'sh20-input-hash-gap-request ["B13-HASH" :input-hash-gap]
   'sh20-provenance-gap-request
   ["B13-PROVENANCE" :provenance-gap]
   'sh20-source-map-gap-request
   ["B13-SOURCEMAP" :source-map-gap]
   'sh20-evidence-gap-request ["B13-EVIDENCE" :evidence-gap]
   'sh20-conformance-gap-request
   ["B13-CONFORMANCE" :conformance-gap]
   'sh20-target-gap-request ["B13-TARGET" :target-metadata-gap]
   'sh20-graph-cycle-request
   ["B13-GRAPH" :artifact-graph-invalid]
   'sh20-reproducibility-gap-request
   ["B13-REPRODUCIBILITY" :reproducibility-gap]
   'sh20-release-gap-request ["B13-RELEASE" :release-evidence-gap]})

(deftest sh20-engine-and-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh20-artifact-policy
            sh20-build-bundle
            sh20-verify-bundle]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (let [policy (invoke-engine 'sh20-artifact-policy [])]
    (is (= :gravity/sh20-artifact-policy (:artifact policy)))
    (is (= 1 (:schema-version policy)))
    (is (= :gravity/canonical-edn-v1
           (:canonical-encoding policy)))
    (is (= #{:object :executable} (:artifact-kinds policy)))
    (is (= 128 (:maximum-source-map-entries policy)))
    (is (= 16 (:maximum-origin-entries policy)))
    (is (= 128 (:maximum-dependencies policy)))
    (is (= 256 (:maximum-dependency-edges policy)))
    (is (contains? (:diagnostics policy) "B13-GRAPH"))
    (is (contains? (:diagnostics policy) "B13-CONFORMANCE"))
    (is (contains? (:diagnostics policy) "B13-VERIFY"))
    (is (some #{:authenticated-sh17-target-output}
              (:pending policy)))
    (is (some #{:target-byte-emission} (:pending policy))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "accepted" "artifact-bundles" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "accepted" "artifact-bundles" ".qst")))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-artifact-bundles" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-artifact-bundles" ".qst"))))))

(deftest sh20-builds-portable-development-and-release-bundles
  (doseq [function
          '[sh20-development-object-request
            sh20-release-executable-request]]
    (testing (str function)
      (let [gravity-request (request accepted-gravity-plan function)
            qst-request (request accepted-qst-plan function)
            gravity-bundle (build gravity-request)
            qst-bundle (build qst-request)
            encoded (pr-str gravity-bundle)
            decoded (edn/read-string encoded)]
        (is (= gravity-request qst-request))
        (is (= gravity-bundle qst-bundle))
        (is (= :accepted (:status gravity-bundle)))
        (is (empty? (:diagnostics gravity-bundle)))
        (is (= gravity-bundle decoded))
        (is (= :gravity/artifact-manifest
               (get-in gravity-bundle [:manifest :artifact])))
        (is (= (:kind gravity-request)
               (get-in gravity-bundle [:manifest :kind])))
        (is (= (:content-hash gravity-request)
               (get-in gravity-bundle [:manifest :content-hash])))
        (is (= (:source-map gravity-request)
               (get-in gravity-bundle [:source-map :entries])))
        (is (= (:dependencies gravity-request)
               (get-in gravity-bundle
                       [:dependency-graph :nodes])))
        (is (= (:dependency-edges gravity-request)
               (get-in gravity-bundle
                       [:dependency-graph :edges])))
        (is (= (:safety (:evidence gravity-request))
               (:safety-summary gravity-bundle)))
        (is (= (:actual-source-path gravity-request)
               (get-in gravity-bundle
                       [:provenance :actual-source-path])))
        (is (= (:actual-output-path gravity-request)
               (get-in gravity-bundle
                       [:provenance :actual-output-path])))
        (is (= :passed
               (:status
                (invoke-engine
                 'sh20-verify-bundle
                 [gravity-request gravity-bundle]))))))))

(deftest sh20-keeps-checkout-paths-outside-identity-inputs
  (let [first-request
        (request
         accepted-gravity-plan 'sh20-release-executable-request)
        second-request
        (request
         accepted-gravity-plan
         'sh20-release-executable-alternate-path-request)
        first-bundle (build first-request)
        second-bundle (build second-request)
        first-identity (:identity-input first-bundle)
        second-identity (:identity-input second-bundle)]
    (is (not= (:actual-source-path first-request)
              (:actual-source-path second-request)))
    (is (not= (:actual-output-path first-request)
              (:actual-output-path second-request)))
    (is (= first-identity second-identity))
    (is (= (:artifact-id first-request)
           (:artifact-id first-identity)))
    (is (= (:build-id first-request)
           (:build-id first-identity)))
    (is (= (:manifest first-bundle) (:manifest second-bundle)))
    (is (= (:dependency-graph first-bundle)
           (:dependency-graph second-bundle)))
    (is (= (:compiler-identity first-bundle)
           (:compiler-identity second-bundle)))
    (is (= (:semantic-record (:provenance first-bundle))
           (:semantic-record (:provenance second-bundle))))
    (is (not= (:source-map first-bundle)
              (:source-map second-bundle)))
    (is (not= (:provenance first-bundle)
              (:provenance second-bundle)))
    (is (not (str/includes? (pr-str first-identity) "/checkout-a/")))
    (is (not (str/includes? (pr-str second-identity) "/checkout-b/")))
    (is (= :passed
           (:status
            (invoke-engine
             'sh20-verify-bundle [first-request first-bundle]))))
    (is (= :passed
           (:status
            (invoke-engine
             'sh20-verify-bundle [second-request second-bundle]))))))

(deftest sh20-rejects-every-incomplete-artifact-family-structurally
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-bundle (build gravity-request)
            qst-bundle (build qst-request)
            diagnostic (first (:diagnostics gravity-bundle))]
        (is (= gravity-request qst-request))
        (is (= gravity-bundle qst-bundle))
        (is (= :rejected (:status gravity-bundle)))
        (is (= 1 (count (:diagnostics gravity-bundle))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :artifact-emission (:stage diagnostic)))
        (is (= (:artifact-id gravity-request)
               (:artifact-id diagnostic)))
        (is (= (:kind gravity-request)
               (:artifact-kind diagnostic)))
        (is (= (:backend gravity-request) (:backend diagnostic)))
        (is (= (:profile gravity-request) (:profile diagnostic)))
        (is (= (:target gravity-request) (:target diagnostic)))
        (is (= (:source-span gravity-request)
               (:source-span diagnostic)))
        (is (= (:origin-chain gravity-request)
               (:generated-origin-chain diagnostic)))
        (is (keyword? (:remediation diagnostic)))))))

(defn- diagnostic-rule
  [bundle]
  (get-in bundle [:diagnostics 0 :rule]))

(deftest sh20-validates-a-closed-bounded-topological-graph
  (let [request
        (request accepted-gravity-plan 'sh20-release-executable-request)
        dependencies (:dependencies request)
        first-dependency (first dependencies)
        second-dependency (second dependencies)
        edges (:dependency-edges request)
        graph-cases
        [(assoc request :dependencies
                [first-dependency first-dependency])
         (assoc request :dependencies
                [first-dependency
                 (assoc second-dependency :topological-ordinal 1)])
         (assoc request :dependencies
                [second-dependency first-dependency])
         (assoc request :dependencies
                [(assoc first-dependency
                        :artifact-id (:artifact-id request))
                 second-dependency])
         (assoc request :dependencies
                [(assoc first-dependency
                        :node-id (:artifact-node-id request))
                 second-dependency])
         (assoc request :dependency-edges
                (assoc-in edges [0 :to-node] :dependency/missing))
         (assoc request :dependency-edges
                (assoc-in edges [1 :edge-id]
                          (:edge-id (first edges))))
         (assoc request :dependency-edges
                (-> edges
                    (assoc-in [1 :from-node]
                              (:from-node (first edges)))
                    (assoc-in [1 :to-node]
                              (:to-node (first edges)))))
         (assoc request :dependency-edges (subvec edges 0 2))
         (assoc request :dependency-edges
                [(assoc (first edges)
                        :from-node :dependency/runtime
                        :to-node :dependency/stdlib-core)])
         (assoc request :dependency-edges
                [(assoc (first edges)
                        :from-node :artifact/root
                        :to-node :artifact/root)])
         (assoc request :dependency-edges
                (vec
                 (map-indexed
                  (fn [index edge]
                    (assoc edge :edge-ordinal (inc index)))
                  edges)))
         (assoc request :dependencies
                (mapv
                 (fn [index]
                   {:node-id (keyword (str "node-" index))
                    :artifact-id (keyword (str "artifact-" index))
                    :topological-ordinal (inc index)
                    :content-hash (:content-hash first-dependency)})
                 (range 129)))
         (assoc request :dependency-edges
                (vec (repeat 257 (first edges))))]]
    (doseq [candidate graph-cases]
      (let [bundle (build candidate)]
        (is (= :rejected (:status bundle)))
        (is (= "B13-GRAPH" (diagnostic-rule bundle)))
        (is (= :dependency-graph
               (get-in bundle [:diagnostics 0 :field])))))))

(deftest sh20-enforces-path-neutral-identity-and-binds-artifact-identity
  (let [request
        (request accepted-gravity-plan 'sh20-release-executable-request)
        valid-origin
        (first (get-in request
                       [:source-map 0 :generated-origin-chain]))
        path-cases
        [[(assoc-in request [:evidence :proofs]
                    "/checkout-secret/proof")
          "B13-EVIDENCE"]
         [(assoc-in request
                    [:source-map 0 :generated-origin-chain 0 :path]
                    "/checkout-secret/generated")
          "B13-SOURCEMAP"]
         [(assoc-in request [:target-metadata :runtime-providers 0]
                    "/checkout-secret/provider")
          "B13-TARGET"]
         [(assoc-in request [:dependencies 0 :artifact-id]
                    "/checkout-secret/dependency")
          "B13-GRAPH"]
         [(assoc request :origin-chain
                 [(assoc valid-origin
                         :path "/checkout-secret/origin")])
          "B13-SCHEMA"]]
        artifact-request (assoc request :artifact-id :other-compiler)
        build-request (assoc request :build-id :gravity-build/other)
        identity (:identity-input (build request))
        artifact-identity (:identity-input (build artifact-request))
        build-identity (:identity-input (build build-request))]
    (doseq [[candidate rule] path-cases]
      (let [bundle (build candidate)]
        (is (= :rejected (:status bundle)))
        (is (= rule (diagnostic-rule bundle)))))
    (is (not (str/includes? (pr-str identity) "/checkout-a/")))
    (is (not= identity artifact-identity))
    (is (not= identity build-identity))
    (is (= :other-compiler (:artifact-id artifact-identity)))
    (is (= :gravity-build/other (:build-id build-identity)))))

(deftest sh20-rejects-malformed-hashes-maps-evidence-and-release-policy
  (let [request
        (request accepted-gravity-plan 'sh20-release-executable-request)
        source-entry (first (:source-map request))
        origin (first (:generated-origin-chain source-entry))
        malformed
        [[(assoc request :artifact-id false)
          "B13-SCHEMA"]
         [(assoc request :backend "gravity.backend/c")
          "B13-SCHEMA"]
         [(assoc request :profile :development)
          "B13-SCHEMA"]
         [(assoc-in request [:content-hash :digest] false)
          "B13-HASH"]
         [(assoc-in request [:content-hash :digest]
                    "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG")
          "B13-HASH"]
         [(assoc request :content-hash
                 (assoc (:content-hash request) :ambient-path "/tmp"))
          "B13-HASH"]
         [(assoc-in request [:source-map 0 :source-span :start-byte] 9)
          "B13-SOURCEMAP"]
         [(assoc request :source-map [source-entry source-entry])
          "B13-SOURCEMAP"]
         [(assoc-in request
                    [:source-map 0 :generated-origin-chain]
                    (vec (repeat 17 origin)))
          "B13-SOURCEMAP"]
         [(assoc-in request
                    [:source-map 0 :generated-origin-chain 0 :kind]
                    :ambient-file)
          "B13-SOURCEMAP"]
         [(assoc-in request [:evidence :safety :outcomes]
                    [:proven-safe :invented-outcome])
          "B13-EVIDENCE"]
         [(assoc-in request [:evidence :effects] false)
          "B13-EVIDENCE"]
         [(assoc-in request [:evidence :conformance] false)
          "B13-CONFORMANCE"]
         [(assoc-in request [:release-evidence :sbom] false)
          "B13-RELEASE"]
         [(assoc-in request [:reproducibility :nondeterminism]
                    [:ambient-clock])
          "B13-RELEASE"]
         [(assoc-in request [:reproducibility :declared-environment]
                    [:target :compiler])
          "B13-RELEASE"]]]
    (doseq [[candidate rule] malformed]
      (let [bundle (build candidate)]
        (is (= :rejected (:status bundle)))
        (is (= rule (diagnostic-rule bundle)))))))

(deftest sh20-bounds-source-maps-and-recomputes-with-precise-substitution
  (let [request
        (request accepted-gravity-plan 'sh20-release-executable-request)
        excessive-source-map
        (build
         (assoc request :source-map
                (vec (repeat 129 (first (:source-map request))))))
        target-substitution
        (build
         (assoc-in request [:target-metadata :target] :linux-x86-64))
        noncanonical-content
        (build
         (assoc-in request [:content-hash :canonical-input] false))
        valid-bundle (build request)
        substituted-bundle
        (assoc-in valid-bundle [:manifest :kind] :object)
        verification
        (invoke-engine
         'sh20-verify-bundle [request substituted-bundle])
        substituted-request
        (assoc request :artifact-id :substituted-compiler)
        request-verification
        (invoke-engine
         'sh20-verify-bundle [substituted-request valid-bundle])
        diagnostic (first (:diagnostics verification))]
    (is (= "B13-SOURCEMAP" (diagnostic-rule excessive-source-map)))
    (is (= "B13-TARGET" (diagnostic-rule target-substitution)))
    (is (= "B13-HASH" (diagnostic-rule noncanonical-content)))
    (is (= :rejected (:status verification)))
    (is (= "B13-VERIFY" (:rule diagnostic)
           (:diagnostic-id diagnostic)))
    (is (= :artifact-emission-verification (:stage diagnostic)))
    (is (= :request-or-candidate-substitution (:reason diagnostic)))
    (is (= :manifest (:field diagnostic)))
    (is (= :recompute-request-and-supply-the-exact-artifact-bundle
           (:remediation diagnostic)))
    (is (= :rejected (:status request-verification)))
    (is (= "B13-VERIFY"
           (get-in request-verification [:diagnostics 0 :rule])))
    (is (= :manifest
           (get-in request-verification [:diagnostics 0 :field])))))
