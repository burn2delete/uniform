(ns gravity.self-hosting.sh23-hermetic-package-build-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh23_hermetic_package_build_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-23 test source is not on the classpath"
                {:id "SH23-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH23-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-23")

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
  (delay
   (compile-plan
    "bootstrap/gravity/src/gravity/self_hosting/hermetic_package_build.gravity")))

(def ^:private sh20-engine-plan
  (delay
   (compile-plan
    "bootstrap/clojure/fixtures/self-hosting/sh-20/artifact_bundle_engine.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "hermetic-builds" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "hermetic-builds" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-hermetic-builds" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-hermetic-builds" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh23-hermetic-package-build-leaf
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
  (invoke-engine 'sh23-build [request]))

(def ^:private rejected-cases
  {'sh23-missing-lock-request
   ["PKG5004" :bootstrap-lockfile-record-required]
   'sh23-online-request
   ["PKG7003" :offline-lockfile-required]
   'sh23-capability-expansion-request
   ["PKG5002" :dependency-capability-expansion]
   'sh23-forbidden-effect-request
   ["PKG2001" :undeclared-or-unsupported-build-effect]
   'sh23-uncontrolled-environment-request
   ["PKG7005" :filesystem-order-must-be-sorted]
   'sh23-implicit-target-request
   ["PKG11002" :explicit-target-manifest-required]
   'sh23-illegal-meta-request
   ["BOOT3002" :compiler-program-not-legal]
   'sh23-library-gap-request
   ["PKG3003" :stdlib-conformance-identity-required]
   'sh23-missing-dependency-request
   ["PKG2004" :missing-cyclic-or-forward-component-dependency]
   'sh23-cycle-request
   ["PKG2004" :missing-cyclic-or-forward-component-dependency]
   'sh23-order-mismatch-request
   ["PKG2004" :component-order-mismatch]
   'sh23-path-identity-input-request
   ["PKG3003" :component-source-identity-required]
   'sh23-inexact-project-authority-request
   ["PKG1005" :exact-project-build-effects-required]
   'sh23-inexact-build-authority-request
   ["PKG2001" :undeclared-or-unsupported-build-effect]
   'sh23-extra-locked-package-request
   ["PKG5004" :locked-package-set-mismatch]
   'sh23-unlocked-component-package-request
   ["PKG5004" :component-standard-library-dependency-not-locked]
   'sh23-malformed-forward-ordinal-request
   ["PKG2004" :component-ordinal-must-be-integer]
   'sh23-unsupported-backend-request
   ["PKG11001" :unsupported-bootstrap-backend]})

(deftest sh23-engine-and-co-canonical-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh23-build-policy sh23-build sh23-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [[family basename]
          [["accepted" "hermetic-builds"]
           ["rejected" "invalid-hermetic-builds"]]]
    (is (= (slurp
            (path (fixture-relative-path family basename ".gravity")))
           (slurp
            (path (fixture-relative-path family basename ".qst")))))))

(deftest sh23-policy-is-bounded-hermetic-and-honest
  (let [policy (invoke-engine 'sh23-build-policy [])]
    (is (= :gravity/sh23-hermetic-build-policy (:artifact policy)))
    (is (= 1 (:schema-version policy)))
    (is (= 32 (:maximum-packages policy)))
    (is (= 32 (:maximum-components policy)))
    (is (= 128 (:maximum-component-dependencies policy)))
    (is (= 16 (:maximum-build-authorities policy)))
    (is (= #{:meta} (:allowed-profiles policy)))
    (is (= #{:portable-mir} (:allowed-targets policy)))
    (is (contains? (:forbidden-effects policy) :build/network))
    (is (contains? (:forbidden-effects policy) :shell/exec))
    (is (contains? (:diagnostics policy) "PKG2004"))
    (is (contains? (:diagnostics policy) "PKG7005"))
    (is (some #{:general-semver-resolution} (:pending policy)))
    (is (some #{:network-registry} (:pending policy)))
    (is (some #{:build-action-execution} (:pending policy)))
    (is (some #{:seedless-bootstrap} (:pending policy)))))

(deftest sh23-resolves-fixed-lock-and-builds-deterministic-actions
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [request (request plan 'sh23-hermetic-build-request)
          result (build request)
          encoded (pr-str result)
          decoded (edn/read-string encoded)]
      (is (= :accepted (:status result)))
      (is (empty? (:diagnostics result)))
      (is (= result decoded))
      (is (= :fixed-lockfile (get-in result [:resolution :mode])))
      (is (true? (get-in result [:resolution :offline])))
      (is (= "sha256:sh23-lock"
             (get-in result [:resolution :lock-id])))
      (is (= 3 (get-in result [:build-graph :component-count])))
      (is (true? (get-in result [:build-graph :acyclic])))
      (is (= [:component/reader
              :component/analyzer
              :component/emitter]
             (get-in result [:build-graph :component-order])))
      (is (= [1 2 3] (mapv :ordinal (:actions result))))
      (is (= [:component/reader
              :component/analyzer
              :component/emitter]
             (mapv :action-id (:actions result))))
      (is (every? #(= #{:build/read-file :build/write-artifact}
                      (:effects %))
                  (:actions result)))
      (is (= 3 (count (:artifact-requests result))))
      (is (= :passed
             (:status
              (invoke-engine
               'sh23-verify-result [request result])))))))

(deftest sh23-links-sh21-sh22-and-emits-valid-sh20-requests
  (let [request
        (request accepted-gravity-plan 'sh23-hermetic-build-request)
        result (build request)]
    (is (= :gravity/sh21-meta-legality-result
           (get-in result [:identity-input :legality :artifact])))
    (is (= :accepted
           (get-in result [:identity-input :legality :status])))
    (is (= :gravity/standard-library-module-manifest
           (get-in result [:identity-input :library :artifact])))
    (is (= "sha256:sh22-conformance"
           (get-in result [:identity-input :library :conformance-id])))
    (doseq [artifact-request (:artifact-requests result)]
      (let [bundle
            (invoke
             sh20-engine-plan 'sh20-build-bundle [artifact-request])]
        (is (= :accepted (:status bundle)))
        (is (= :gravity/artifact-manifest
               (get-in bundle [:manifest :artifact])))
        (is (= :meta (get-in bundle [:manifest :profile])))
        (is (= :portable-mir
               (get-in bundle [:manifest :target])))
        (is (= :passed
               (:status
                (invoke
                 sh20-engine-plan
                 'sh20-verify-bundle
                 [artifact-request bundle]))))))))

(deftest sh23-keeps-physical-paths-outside-semantic-identity
  (let [first-request
        (request
         accepted-gravity-plan 'sh23-hermetic-build-request)
        second-request
        (request
         accepted-gravity-plan
         'sh23-hermetic-build-alternate-path-request)
        first-result (build first-request)
        second-result (build second-request)
        first-bundles
        (mapv
         #(invoke sh20-engine-plan 'sh20-build-bundle [%])
         (:artifact-requests first-result))
        second-bundles
        (mapv
         #(invoke sh20-engine-plan 'sh20-build-bundle [%])
         (:artifact-requests second-result))]
    (is (not= (mapv :actual-source-path (:components first-request))
              (mapv :actual-source-path (:components second-request))))
    (is (= (:identity-input first-result)
           (:identity-input second-result)))
    (is (= (:actions first-result) (:actions second-result)))
    (is (= (mapv :identity-input first-bundles)
           (mapv :identity-input second-bundles)))
    (is (every? #(= :accepted (:status %))
                (concat first-bundles second-bundles)))
    (is (= (mapv #(dissoc % :actual-source-path
                          :actual-output-path :source-span :source-map)
                 (:artifact-requests first-result))
           (mapv #(dissoc % :actual-source-path
                          :actual-output-path :source-span :source-map)
                 (:artifact-requests second-result))))
    (is (not= (:provenance first-result)
              (:provenance second-result)))
    (is (not (str/includes?
              (pr-str (:identity-input first-result)) "/checkout-a/")))
    (is (not (str/includes?
              (pr-str (:identity-input second-result)) "/checkout-b/")))))

(deftest sh23-rejects-every-incomplete-build-family-structurally
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-result (build gravity-request)
            qst-result (build qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :hermetic-package-build (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= :passed
               (:status
                (invoke-engine
                 'sh23-verify-result
                 [gravity-request gravity-result]))))))))

(deftest sh23-verifier-recomputes-and-rejects-substitutions
  (let [request
        (request accepted-gravity-plan 'sh23-hermetic-build-request)
        result (build request)
        changed
        (assoc-in result [:actions 1 :output-id] :artifact/substituted)
        verification
        (invoke-engine 'sh23-verify-result [request changed])
        diagnostic (first (:diagnostics verification))]
    (is (= :rejected (:status verification)))
    (is (= "SH23-VERIFY" (:rule diagnostic)))
    (is (= :build-result-substitution (:reason diagnostic)))
    (is (= :hermetic-package-build (:stage diagnostic)))))
