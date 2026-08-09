(ns gravity.self-hosting.w5-b13-artifact-emitter-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later JVM clearance command (intentionally not run in this change):
; clojure -M:test --namespace gravity.self-hosting.w5-b13-artifact-emitter-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_b13_artifact_emitter_test.clj")]
    (when-not resource
      (throw
       (ex-info "W5 B13 test source is not on the classpath"
                {:id "W5-B13-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-B13-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-b13")
(def ^:private stage2-harness-target :jvm)

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- declared-stage2-harness-target? [relative-path]
  (boolean
   (re-find #"\(:target\s+:jvm\)"
            (slurp (path relative-path)))))

(defn- compile-plan [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path stage2-harness-target))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan engine-source)))
(def ^:private accepted-gravity-plan
  (delay (compile-plan
          (fixture-path "accepted" "artifact-emission" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan
          (fixture-path "accepted" "artifact-emission" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (fixture-path "rejected" "invalid-artifact-emission" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (fixture-path "rejected" "invalid-artifact-emission" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-b13-artifact-emitter
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- request-at [plan function source-path]
  (invoke plan function [source-path]))

(defn- emit [request-value]
  (invoke engine-plan 'w5-b13-emit [request-value]))

(def ^:private rejected-cases
  {'w5-b13-invalid-schema-request "B13-SCHEMA"
   'w5-b13-invalid-hash-request "B13-HASH"
   'w5-b13-invalid-coherent-supplied-hash-request "B13-HASH"
   'w5-b13-invalid-provenance-request "B13-PROVENANCE"
   'w5-b13-invalid-provenance-missing-status-request "B13-PROVENANCE"
   'w5-b13-invalid-provenance-extra-key-request "B13-PROVENANCE"
   'w5-b13-invalid-compiler-artifact-request "B13-PROVENANCE"
   'w5-b13-invalid-compiler-status-request "B13-PROVENANCE"
   'w5-b13-invalid-compiler-version-request "B13-PROVENANCE"
   'w5-b13-invalid-pass-contract-request "B13-PROVENANCE"
   'w5-b13-invalid-dependency-artifact-request "B13-PROVENANCE"
   'w5-b13-invalid-dependency-status-request "B13-PROVENANCE"
   'w5-b13-invalid-dependency-generator-request "B13-PROVENANCE"
   'w5-b13-invalid-dependency-type-request "B13-PROVENANCE"
   'w5-b13-invalid-dependency-substitution-request "B13-PROVENANCE"
   'w5-b13-invalid-coherent-dependency-substitution-request "B13-PROVENANCE"
   'w5-b13-invalid-manifest-provenance-extra-key-request "B13-SCHEMA"
   'w5-b13-invalid-manifest-source-link-request "B13-PROVENANCE"
   'w5-b13-invalid-compiler-link-request "B13-PROVENANCE"
   'w5-b13-invalid-toolchain-link-request "B13-PROVENANCE"
   'w5-b13-invalid-sourcemap-request "B13-SOURCEMAP"
   'w5-b13-invalid-sourcemap-phase-request "B13-SOURCEMAP"
   'w5-b13-invalid-sourcemap-reversed-span-request "B13-SOURCEMAP"
   'w5-b13-invalid-sourcemap-origin-request "B13-SOURCEMAP"
   'w5-b13-invalid-evidence-request "B13-EVIDENCE"
   'w5-b13-invalid-evidence-missing-request "B13-EVIDENCE"
   'w5-b13-invalid-evidence-extra-key-request "B13-EVIDENCE"
   'w5-b13-invalid-evidence-type-request "B13-EVIDENCE"
   'w5-b13-invalid-evidence-substitution-request "B13-EVIDENCE"
   'w5-b13-invalid-evidence-effect-substitution-request "B13-EVIDENCE"
   'w5-b13-invalid-unsafe-audit-request "B13-EVIDENCE"
   'w5-b13-invalid-target-record-link-request "B13-TARGET"
   'w5-b13-invalid-target-request "B13-TARGET"
   'w5-b13-invalid-manifest-target-features-request "B13-TARGET"
   'w5-b13-invalid-coherent-target-features-request "B13-TARGET"
   'w5-b13-invalid-pointer-width-request "B13-TARGET"
   'w5-b13-invalid-calling-convention-request "B13-TARGET"
   'w5-b13-invalid-binary-format-request "B13-TARGET"
   'w5-b13-invalid-architecture-request "B13-TARGET"
   'w5-b13-invalid-evidence-link-request "B13-EVIDENCE"
   'w5-b13-invalid-conformance-request "B13-CONFORMANCE"
   'w5-b13-invalid-conformance-complete-request "B13-CONFORMANCE"
   'w5-b13-invalid-conformance-substitution-request "B13-CONFORMANCE"
   'w5-b13-invalid-manifest-conformance-link-request "B13-CONFORMANCE"
   'w5-b13-invalid-reproducibility-request "B13-REPRODUCIBILITY"
   'w5-b13-invalid-release-request "B13-RELEASE"
   'w5-b13-invalid-release-gate-missing-request "B13-RELEASE"
   'w5-b13-invalid-release-gate-extra-key-request "B13-RELEASE"
   'w5-b13-invalid-release-gate-substitution-request "B13-RELEASE"
   'w5-b13-invalid-non-authority-missing-request "B13-SCHEMA"
   'w5-b13-invalid-non-authority-extra-key-request "B13-SCHEMA"
   'w5-b13-invalid-non-authority-release-request "B13-SCHEMA"
   'w5-b13-invalid-non-authority-self-host-request "B13-SCHEMA"
   'w5-b13-invalid-non-authority-public-request "B13-SCHEMA"
   'w5-b13-invalid-non-authority-seed-request "B13-SCHEMA"
   'w5-b13-invalid-non-authority-full-language-request "B13-SCHEMA"
   'w5-b13-invalid-graph-request "B13-GRAPH"
   'w5-b13-invalid-graph-wrong-root-request "B13-GRAPH"
   'w5-b13-invalid-graph-disconnected-request "B13-GRAPH"
   'w5-b13-invalid-graph-missing-stage-request "B13-GRAPH"
   'w5-b13-invalid-graph-reordered-stage-request "B13-GRAPH"
   'w5-b13-invalid-graph-reordered-pass-request "B13-GRAPH"
   'w5-b13-invalid-graph-mislabeled-stage-request "B13-GRAPH"
   'w5-b13-invalid-graph-digest-link-request "B13-GRAPH"
   'w5-b13-invalid-graph-origin-request "B13-GRAPH"})

(deftest w5-b13-engine-and-co-canonical-fixtures-compile-through-stage2
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-b13-policy
            w5-b13-diagnostic-catalog
            w5-b13-target-coherent?
            w5-b13-request-shape-valid?
            w5-b13-target-records-valid?
            w5-b13-target-record-links-valid?
            w5-b13-manifest-valid?
            w5-b13-hash-valid?
            w5-b13-provenance-valid?
            w5-b13-source-map-valid?
            w5-b13-evidence-valid?
            w5-b13-conformance-valid?
            w5-b13-reproducibility-valid?
            w5-b13-release-gate-valid?
            w5-b13-graph-valid?
            w5-b13-identity-input
            w5-b13-emit
            w5-b13-artifact-emission
            w5-b13-execute
            w5-b13-run
            w5-b13-recompute
            w5-b13-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [[family basename]
          [["accepted" "artifact-emission"]
           ["rejected" "invalid-artifact-emission"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-b13-jvm-source-harness-is-distinct-from-llvm-artifact-target
  (testing "the stage2 parser target is explicit in every executable source"
    (is (= :jvm stage2-harness-target))
    (doseq [relative-path
            [engine-source
             (fixture-path "accepted" "artifact-emission" ".gravity")
             (fixture-path "accepted" "artifact-emission" ".qst")
             (fixture-path "rejected" "invalid-artifact-emission" ".gravity")
             (fixture-path "rejected" "invalid-artifact-emission" ".qst")]]
      (is (declared-stage2-harness-target? relative-path) relative-path)))
  (testing "source harness target never changes the artifact candidate"
    (let [policy (invoke engine-plan 'w5-b13-policy [])]
      (is (= :llvm-x86_64-linux (:target policy)))
      (is (= :llvm-x86_64-linux (:candidate-target policy))))))

(deftest w5-b13-policy-is-static-nonauthority-and-target-exact
  (let [policy (invoke engine-plan 'w5-b13-policy [])
        target (:target-scope policy)]
    (is (= :gravity/w5-b13-artifact-emission-policy (:artifact policy)))
    (is (= :meta (:profile policy)))
    (is (= :llvm-x86_64-linux (:target policy)))
    (is (= :stage2-static-only (:scope policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :object-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-platforms policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (doseq [entry (:unsupported-target-policies policy)]
      (is (= :unsupported (:support entry)))
      (is (false? (:invokes-clojure? entry)))
      (is (false? (:links-jvm? entry)))
      (is (false? (:fallback? entry))))
    (is (false? (:cross-target-inference? policy)))
    (is (false? (:darwin-fallback? policy)))
    (is (false? (:release-eligible? policy)))
    (is (= :linux (:os target)))
    (is (= :x86_64 (:arch target)))
    (is (= :llvm (:backend target)))
    (is (= :elf (:object-format target)))
    (is (= :sysv-amd64 (:abi target)))
    (is (= :none (:runtime target)))
    (is (= #{} (:providers target)))
    (is (= #{} (:runtime-providers target)))
    (is (= 10 (count (:diagnostics policy))))
    (is (true? (:static-only? policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (= :non-authority (:authority policy)))
    (is (= :clojure-bootstrap
           (get-in policy [:residual-boundaries :stage2-compiler-plan])))
    (is (= :jvm (get-in policy [:residual-boundaries :stage2-runtime])))))

(deftest w5-b13-accepted-emission-contains-all-output-artifacts
  (doseq [[plan function source-suffix]
          [[accepted-gravity-plan 'w5-b13-artifact-emission-request ".gravity"]
           [accepted-qst-plan 'w5-b13-artifact-emission-request-at ".qst"]]]
    (let [request-value
          (if (= function 'w5-b13-artifact-emission-request)
            (request plan function)
            (request-at plan function
                        (str "/checkout-a/artifact-emission" source-suffix)))
          result (emit request-value)]
      (is (= :accepted (:status result)))
      (is (= :gravity/w5-b13-artifact-emission (:artifact result)))
      (is (= :llvm-x86_64-linux (:target result)))
      (is (= :llvm-x86_64-linux (:candidate-target result)))
      (is (= :linux (get-in result [:candidate-platform :os])))
      (is (= :x86_64 (get-in result [:candidate-platform :arch])))
      (is (= :llvm (get-in result [:candidate-platform :backend])))
      (is (= :elf (get-in result [:candidate-platform :object-format])))
      (is (= :sysv-amd64 (get-in result [:candidate-platform :abi])))
      (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
             (:unsupported-targets result)))
      (doseq [entry (:unsupported-target-policies result)]
        (is (= :unsupported (:support entry)))
        (is (false? (:invokes-clojure? entry)))
        (is (false? (:links-jvm? entry)))
        (is (false? (:fallback? entry))))
      (is (false? (:cross-target-inference? result)))
      (is (false? (:darwin-fallback? result)))
      (is (false? (:release-eligible? result)))
      (is (true? (:static-only? result)))
      (is (empty? (:diagnostics result)))
      (is (= 1 (count (:artifact-manifests result))))
      (is (= 1 (count (:content-hash-records result))))
      (is (= :complete (get-in result [:artifact-graph :status])))
      (is (= :source-forms (get-in result [:artifact-graph :root])))
      (is (= (:artifact-id result)
             (get-in result [:artifact-graph :artifact-id])))
      (is (= 20 (count (get-in result [:artifact-graph :nodes]))))
      (is (= 19 (count (get-in result [:artifact-graph :edges]))))
      (is (= [:source-forms :reader :syntax-objects :macro-expansion
              :core-gravity-ast :name-resolution :type-checking
              :effect-checking :profile-validation :capability-validation
              :ownership-lifetime-analysis :safety-analysis :gravity-mir
              :verify-mir :domain-ir-lowering :verify-domain-ir
              :optimization :target-lowering
              :artifact-emission :package-provenance-recording]
             (mapv :node-id (get-in result [:artifact-graph :nodes]))))
      (doseq [index (range 0 19)]
        (let [nodes (get-in result [:artifact-graph :nodes])
              edge (get-in result [:artifact-graph :edges index])]
          (is (= (:node-id (nth nodes index)) (:from edge)))
          (is (= (:node-id (nth nodes (inc index))) (:to edge)))
          (is (= (:to edge) (:origin edge)))
          (is (<= (get-in edge [:source-span :start-byte])
                  (get-in edge [:source-span :end-byte])))))
      (is (= :preserved (get-in result [:source-debug-map :status])))
      (is (= [:source-forms :reader :syntax-objects :macro-expansion
              :core-gravity-ast :name-resolution :type-checking
              :effect-checking :profile-validation :capability-validation
              :ownership-lifetime-analysis :safety-analysis :gravity-mir
              :verify-mir :domain-ir-lowering :verify-domain-ir
              :optimization :target-lowering
              :artifact-emission :package-provenance-recording]
             (get-in result [:source-debug-map :phases])))
      (is (= 20 (count (get-in result [:source-debug-map :locations]))))
      (doseq [location (get-in result [:source-debug-map :locations])]
        (is (= (:phase location) (:origin location)))
        (is (<= (get-in location [:source-span :start-byte])
                (get-in location [:source-span :end-byte]))))
      (is (= :complete (get-in result [:compiler-provenance :status])))
      (is (= :complete (get-in result [:dependency-provenance :status])))
      (is (= :structural-complete (get-in result [:provenance :status])))
      (is (= :structural-complete
             (get-in result [:artifact-emission-results
                             :provenance-status])))
      (is (false? (get-in result [:capability-based-proof
                                  :provenance-complete?])))
      (is (true? (get-in result [:capability-based-proof
                                 :provenance-structurally-complete?])))
      (is (= #{:artifact :compiler-id :compiler-version :generator-id
               :pass-pipeline :pass-contract :source-id :status}
             (set (keys (:compiler-provenance result)))))
      (is (= #{:artifact :dependency-graph-id :dependencies :generator
               :target-toolchain :runtime-providers :status}
             (set (keys (:dependency-provenance result)))))
      (is (= #{:artifact :source-id :actual-source-path :compiler-id
               :generator-id :pass-pipeline :dependency-graph-id
               :dependencies :status}
             (set (keys (:provenance result)))))
      (is (= ["gravity/compiler-core-v1"
              "gravity/llvm-lowering-contract-v1"]
             (get-in result [:dependency-provenance :dependencies])))
      (is (= (get-in result [:dependency-provenance :dependencies])
             (get-in result [:manifest :provenance
                             :dependency-inventory])))
      (is (= (get-in result [:dependency-provenance :dependencies])
             (get-in result [:artifact-graph :dependencies])))
      (is (= :structural-complete
             (get-in result [:safety-proof-certificate-bundle :status])))
      (is (= :safe
             (get-in result [:safety-proof-certificate-bundle :safety-mode])))
      (is (= [:proven-safe]
             (get-in result
                     [:safety-proof-certificate-bundle :safety-outcomes])))
      (is (= []
             (get-in result
                     [:safety-proof-certificate-bundle
                      :unsafe-audit-records])))
      (is (= #{} (get-in result [:manifest :evidence :effects])))
      (is (= #{} (get-in result [:manifest :evidence :capabilities])))
      (is (= :structural-complete
             (get-in result [:artifact-emission-results :evidence-status])))
      (is (false? (get-in result [:capability-based-proof
                                  :evidence-bundle-complete?])))
      (is (true? (get-in result [:capability-based-proof
                                 :evidence-bundle-structurally-complete?])))
      (is (= :complete (get-in result [:effect-capability-summary :status])))
      (is (= :complete (get-in result [:runtime-provider-summary :status])))
      (is (= :complete
             (get-in result [:target-runtime-abi-layout-summary :status])))
      (is (= #{:x86_64 :sse2}
             (get-in result [:manifest :target-features])))
      (is (= (get-in result [:manifest :target-features])
             (get-in result
                     [:target-runtime-abi-layout-summary :target-features])))
      (is (= {:architecture :x86_64
              :binary-format :elf
              :calling-convention :sysv-amd64
              :pointer-width 64
              :endianness :little}
             (get-in result [:manifest :abi-layout])))
      (is (= (get-in result [:manifest :abi-layout])
             (get-in result
                     [:target-runtime-abi-layout-summary :abi-layout])))
      (is (= :pending-unverified
             (get-in result [:content-hash-records 0 :status])))
      (is (false? (get-in result
                          [:content-hash-records 0
                           :cryptographically-verified?])))
      (is (= :pending-unverified
             (get-in result [:reproducibility-record :status])))
      (is (false? (get-in result
                          [:reproducibility-record
                           :cryptographically-verified?])))
      (is (false? (get-in result [:reproducibility-record :reproducible?])))
      (is (= :pending-unverified
             (get-in result [:conformance-evidence-reference :status])))
      (is (= :structural-crosslink-only
             (get-in result [:conformance-evidence-reference :verification])))
      (is (false? (get-in result
                          [:conformance-evidence-reference
                           :independently-verified?])))
      (is (= :incomplete
             (get-in result [:artifact-emission-results :status])))
      (is (= :incomplete
             (get-in result [:capability-based-proof :status])))
      (is (false? (get-in result
                          [:capability-based-proof
                           :content-hashes-complete?])))
      (is (false? (get-in result
                          [:capability-based-proof :reproducible?])))
      (is (false? (get-in result
                          [:capability-based-proof
                           :conformance-evidence-complete?])))
      (is (true? (get-in result
                         [:capability-based-proof
                          :conformance-evidence-structurally-linked?])))
      (is (= :incomplete (:completion result)))
      (is (true? (:blocked? result)))
      (is (= (get-in request-value
                      [:conformance-evidence-reference :evidence-id])
             (get-in result [:manifest :evidence :conformance])))
      (is (= :blocked-development-only
             (get-in result [:release-gate-record
                             :release-grade-artifact-status])))
      (is (= #{:artifact :release-grade-artifact-status :reason
               :blocked-downstream :diagnostic-on-release-attempt :authority
               :clojure-seed-boundary? :self-hosted? :release?
               :public-authority? :full-language? :non-authority :status}
             (set (keys (:release-gate-record result)))))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (= {:clojure-seed-boundary? true
              :self-hosted? false
              :release? false
              :public-authority? false
              :full-language? false
              :authority :non-authority}
             (:non-authority result)))
      (is (= (:non-authority result)
             (get-in result [:release-gate-record :non-authority])))
      (is (= :non-authority (:authority result))))))

(deftest w5-b13-provenance-uses-the-requested-source-kind
  (let [gravity-request
        (request-at accepted-gravity-plan 'w5-b13-artifact-emission-request-at
                    "/checkout-a/artifact-emission.gravity")
        qst-request
        (request-at accepted-qst-plan 'w5-b13-artifact-emission-request-at
                    "/checkout-a/artifact-emission.qst")
        gravity-result (emit gravity-request)
        qst-result (emit qst-request)]
    (is (= :accepted (:status gravity-result)))
    (is (= :accepted (:status qst-result)))
    (is (str/ends-with? (get-in gravity-result [:provenance :actual-source-path])
                        ".gravity"))
    (is (str/ends-with? (get-in qst-result [:provenance :actual-source-path])
                        ".qst"))
    (is (str/ends-with? (get-in gravity-result
                                [:source-debug-map :actual-source-path])
                        ".gravity"))
    (is (str/ends-with? (get-in qst-result
                                [:source-debug-map :actual-source-path])
                        ".qst"))))

(deftest w5-b13-identity-is-path-neutral-and-provenance-is-path-bearing
  (let [left-request
        (request accepted-gravity-plan
                 'w5-b13-artifact-emission-request)
        right-request
        (request accepted-gravity-plan
                 'w5-b13-artifact-emission-alternate-path-request)
        left (emit left-request)
        right (emit right-request)]
    (is (= :accepted (:status left)))
    (is (= :accepted (:status right)))
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (str/includes? (str (:provenance left)) "/checkout-a/"))
    (is (str/includes? (str (:provenance right)) "/checkout-b/"))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b/")))
    (is (not (str/includes? (pr-str (:identity-input left)) ":jvm")))
    (is (not (str/includes? (pr-str (:identity-input right)) ":darwin")))))

(deftest w5-b13-rejected-fixture-covers-every-diagnostic-family
  (doseq [[function expected-rule] rejected-cases]
    (testing (str function)
      (let [gravity-request
            (request accepted-gravity-plan
                     'w5-b13-artifact-emission-request)
            qst-request
            (request-at accepted-qst-plan
                        'w5-b13-artifact-emission-request-at
                        "/checkout-a/artifact-emission.qst")
            gravity-invalid
            (invoke rejected-gravity-plan function [gravity-request])
            qst-invalid
            (invoke rejected-qst-plan function [qst-request])
            gravity-result (emit gravity-invalid)
            qst-result (emit qst-invalid)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= (dissoc gravity-invalid :provenance :source-debug-map)
               (dissoc qst-invalid :provenance :source-debug-map)))
        (is (= (dissoc gravity-result :provenance)
               (dissoc qst-result :provenance)))
        (is (= :rejected (:status gravity-result)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= expected-rule (:diagnostic-id diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= :artifact-emission (:stage diagnostic)))
        (is (= :gravity.backend/llvm (:backend diagnostic)))
        (is (= :meta (:profile diagnostic)))
        (is (= :llvm-x86_64-linux (:target diagnostic)))
        (is (map? (:source-span diagnostic)))
        (is (keyword? (:remediation diagnostic)))
        (is (true? (:clojure-seed-boundary? gravity-result)))
        (is (false? (:self-hosted? gravity-result)))
        (is (false? (:release? gravity-result)))
        (is (false? (:public-authority? gravity-result)))
        (is (= :non-authority (:authority gravity-result)))))))

(deftest w5-b13-rejects-substituted-result-by-recomputation
  (let [request-value
        (request accepted-gravity-plan 'w5-b13-artifact-emission-request)
        result (emit request-value)
        verification
        (invoke engine-plan 'w5-b13-verify-result [request-value result])
        substituted (assoc-in result [:manifest :content-hash]
                              "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
        substituted-verification
        (invoke engine-plan 'w5-b13-verify-result
                [request-value substituted])]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :rejected (:status substituted-verification)))
    (is (= "B13-HASH"
           (get-in substituted-verification [:diagnostics 0 :rule])))))
