(ns gravity.self-hosting.w5-compiler-pipeline-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_compiler_pipeline_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info "W5 pipeline verifier test source is not on the classpath"
                {:id "W5-PIPELINE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-PIPELINE-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private engine-source
  "bootstrap/gravity/src/gravity/compiler/w5_compiler_pipeline_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-pipeline")

(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan engine-source)))
(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "pipeline-verification" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "pipeline-verification" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-pipeline-verification" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-pipeline-verification" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-compiler-pipeline-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine [function arguments]
  (invoke engine-plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- request-at [plan function source-path]
  (invoke plan function [source-path]))

(defn- verify [request-value]
  (invoke-engine 'w5-pipeline-verify [request-value]))

(def ^:private rejected-cases
  {'w5-pipeline-invalid-pipeline "C1-PIPELINE"
   'w5-pipeline-invalid-pass-contract "C1-PASS-CONTRACT"
   'w5-pipeline-invalid-evidence-drop "C1-EVIDENCE-DROP"
   'w5-pipeline-invalid-backend "C1-UNCHECKED-BACKEND"
   'w5-pipeline-invalid-domain-anchor "C1-DOMAIN-ANCHOR"
   'w5-pipeline-invalid-manifest "C1-MANIFEST"
   'w5-pipeline-invalid-self-host "C1-SELF-HOST"
   'w5-pipeline-invalid-encoding "C2-ENCODING"
   'w5-pipeline-invalid-delimiter "C2-DELIMITER"
   'w5-pipeline-invalid-string "C2-STRING"
   'w5-pipeline-invalid-numeric "C2-NUMERIC"
   'w5-pipeline-invalid-identifier "C2-IDENTIFIER"
   'w5-pipeline-invalid-namespace "C2-NS-SHAPE"
   'w5-pipeline-invalid-map "C2-MAP"
   'w5-pipeline-invalid-set "C2-SET"
   'w5-pipeline-invalid-metadata "C2-METADATA"
   'w5-pipeline-invalid-abbreviation "C2-ABBREV"
   'w5-pipeline-invalid-extension "C2-EXTENSION"
   'w5-pipeline-invalid-hash "C2-HASH"
   'w5-pipeline-invalid-type-mismatch "C7-TYPE-MISMATCH"
   'w5-pipeline-invalid-annotation "C7-ANNOTATION"
   'w5-pipeline-invalid-dynamic "C7-DYNAMIC"
   'w5-pipeline-invalid-cast "C7-CAST"
   'w5-pipeline-invalid-nullability "C7-NULLABILITY"
   'w5-pipeline-invalid-generic "C7-GENERIC"
   'w5-pipeline-invalid-protocol "C7-PROTOCOL"
   'w5-pipeline-invalid-layout "C7-LAYOUT"
   'w5-pipeline-invalid-schema "C7-SCHEMA"
   'w5-pipeline-invalid-type-verify "C7-VERIFY"
   'w5-pipeline-invalid-undeclared-effect "C8-UNDECLARED"
   'w5-pipeline-invalid-effect-profile "C8-PROFILE"
   'w5-pipeline-invalid-capability "C8-CAPABILITY"
   'w5-pipeline-invalid-build-effect "C8-BUILD"
   'w5-pipeline-invalid-replay "C8-REPLAY"
   'w5-pipeline-invalid-order "C8-ORDER"
   'w5-pipeline-invalid-runtime "C8-RUNTIME"
   'w5-pipeline-invalid-unknown-effect "C8-UNKNOWN"
   'w5-pipeline-invalid-effect-verify "C8-VERIFY"
   'w5-pipeline-invalid-use-after-move "C9-USE-AFTER-MOVE"
   'w5-pipeline-invalid-use-after-consume "C9-USE-AFTER-CONSUME"
   'w5-pipeline-invalid-borrow-escape "C9-BORROW-ESCAPE"
   'w5-pipeline-invalid-mut-alias "C9-MUT-ALIAS"
   'w5-pipeline-invalid-move-while-borrowed "C9-MOVE-WHILE-BORROWED"
   'w5-pipeline-invalid-region-escape "C9-REGION-ESCAPE"
   'w5-pipeline-invalid-arena-generation "C9-ARENA-GENERATION"
   'w5-pipeline-invalid-linear-leak "C9-LINEAR-LEAK"
   'w5-pipeline-invalid-linear-double "C9-LINEAR-DOUBLE"
   'w5-pipeline-invalid-transfer "C9-TRANSFER"
   'w5-pipeline-invalid-ownership-runtime "C9-RUNTIME-CHECK"
   'w5-pipeline-invalid-unsafe "C9-UNSAFE"
   'w5-pipeline-invalid-no-outcome "C10-NO-OUTCOME"
   'w5-pipeline-invalid-proof "C10-PROOF"
   'w5-pipeline-invalid-check "C10-CHECK"
   'w5-pipeline-invalid-unsafe-safety "C10-UNSAFE"
   'w5-pipeline-invalid-generated "C10-GENERATED"
   'w5-pipeline-invalid-taint "C10-TAINT"
   'w5-pipeline-invalid-safety-capability "C10-CAPABILITY"
   'w5-pipeline-invalid-ffi "C10-FFI"
   'w5-pipeline-invalid-numeric-safety "C10-NUMERIC"
   'w5-pipeline-invalid-optimization "C10-OPTIMIZATION"
   'w5-pipeline-invalid-source-link "C1-MANIFEST"
   'w5-pipeline-invalid-stage-link "C1-MANIFEST"
   'w5-pipeline-invalid-adjacent-stage-link "C1-MANIFEST"
   'w5-pipeline-invalid-stage-artifact-identity "C1-MANIFEST"
   'w5-pipeline-invalid-pass-link "C1-MANIFEST"
   'w5-pipeline-invalid-adjacent-pass-link "C1-MANIFEST"
   'w5-pipeline-invalid-appended-stage "C1-PIPELINE"
   'w5-pipeline-invalid-appended-pass "C1-PIPELINE"
   'w5-pipeline-invalid-target-link "C1-MANIFEST"
   'w5-pipeline-invalid-artifact-link "C1-MANIFEST"
   'w5-pipeline-invalid-identity-link "C1-MANIFEST"
   'w5-pipeline-invalid-provenance-link "C1-MANIFEST"
   'w5-pipeline-invalid-request-shape "C1-PIPELINE"})

(deftest w5-pipeline-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function '[w5-pipeline-policy
                     w5-pipeline-stage-order
                     w5-pipeline-target-contract
                     w5-pipeline-diagnostic
                     w5-pipeline-diagnostic-valid?
                     w5-pipeline-identity-input
                     w5-pipeline-request-shape-valid?
                     w5-pipeline-cross-links-valid?
                     w5-pipeline-verify
                     w5-pipeline-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (is (= (slurp (path (fixture-path "accepted" "pipeline-verification" ".gravity")))
         (slurp (path (fixture-path "accepted" "pipeline-verification" ".qst")))))
  (is (= (slurp (path (fixture-path "rejected" "invalid-pipeline-verification" ".gravity")))
         (slurp (path (fixture-path "rejected" "invalid-pipeline-verification" ".qst"))))))

(deftest w5-pipeline-policy-is-exact-target-and-nonauthority
  (let [policy (invoke-engine 'w5-pipeline-policy [])
        target (:target-contract policy)]
    (is (= :gravity/w5-compiler-pipeline-policy (:artifact policy)))
    (is (= :meta (:profile policy)))
    (is (= :llvm-x86_64-linux (:target target)))
    (is (= :linux (:os target)))
    (is (= :x86_64 (:arch target)))
    (is (= :llvm (:backend target)))
    (is (= :elf (:object-format target)))
    (is (= :sysv-amd64 (:abi target)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets target)))
    (doseq [entry (:unsupported-target-policies target)]
      (is (= :unsupported (:support entry)))
      (is (false? (:invokes-clojure? entry)))
      (is (false? (:links-jvm? entry)))
      (is (false? (:fallback? entry))))
    (is (= 16 (count (:pipeline-order policy))))
    (is (= [:read-source :build-syntax :macro-expand :resolve-names
            :lower-to-core :type-check :effect-check :profile-validate
            :safety-analyze :build-mir :verify-mir :optimize-mir
            :lower-domain-ir :verify-domain-ir :lower-target :emit-artifacts]
           (:pipeline-order policy)))
    (is (some #{"C1-PIPELINE"} (:diagnostics policy)))
    (is (some #{"C2-HASH"} (:diagnostics policy)))
    (is (some #{"C7-VERIFY"} (:diagnostics policy)))
    (is (some #{"C8-CAPABILITY"} (:diagnostics policy)))
    (is (some #{"C9-UNSAFE"} (:diagnostics policy)))
    (is (some #{"C10-NO-OUTCOME"} (:diagnostics policy)))
    (is (= [:active :suppressed-by-policy :redacted :resolved-by-fix
            :stale-after-edit]
           (:diagnostic-lifecycle policy)))
    (is (true? (get-in policy [:flags :clojure-seed-boundary?])))
    (is (false? (get-in policy [:flags :self-hosted?])))
    (is (false? (get-in policy [:flags :release?])))
    (is (false? (get-in policy [:flags :public-authority?])))
    (is (= :non-authority (get-in policy [:flags :authority])))))

(deftest w5-pipeline-accepts-complete-bounded-record-but-blocks-release
  (doseq [[plan function source-path]
          [[accepted-gravity-plan 'w5-pipeline-verification-request nil]
           [accepted-qst-plan 'w5-pipeline-verification-request-at
            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-pipeline/accepted/pipeline-verification.qst"]]]
    (let [request-value
          (if (= function 'w5-pipeline-verification-request)
            (request plan function)
            (request-at plan function source-path))
          result (verify request-value)]
      (is (= :accepted (:status result)))
      (is (= [] (:diagnostics result)))
      (is (= 16 (count (:pipeline-order result))))
      (is (= :llvm-x86_64-linux (get-in result [:target-contract :target])))
      (is (= :blocked (get-in result [:verifier-gate :decision])))
      (is (false? (get-in result [:verifier-gate :release-eligible?])))
      (is (= :incomplete (:completion result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (= :non-authority (:authority result)))
      (is (= :passed
             (:status
              (invoke-engine 'w5-pipeline-verify-result
                             [request-value result])))))))

(deftest w5-pipeline-identity-is-path-neutral-and-provenance-is-real
  (let [left-request
        (request accepted-gravity-plan
                 'w5-pipeline-verification-request)
        right-request
        (request accepted-gravity-plan
                 'w5-pipeline-verification-alternate-path-request)
        left (verify left-request)
        right (verify right-request)]
    (is (= :accepted (:status left)))
    (is (= :accepted (:status right)))
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (.contains (pr-str (:identity-input left)) "/checkout-a/")))
    (is (not (.contains (pr-str (:identity-input right)) "/checkout-b/")))
    (is (.contains (get-in left [:provenance :actual-source-path])
                   "/checkout-a/"))
    (is (.contains (get-in right [:provenance :actual-source-path])
                   "/checkout-b/"))))

(deftest w5-pipeline-provenance-retains-source-kind
  (let [gravity-result
        (verify (request accepted-gravity-plan
                         'w5-pipeline-verification-request))
        qst-result
        (verify (request-at accepted-qst-plan
                            'w5-pipeline-verification-request-at
                            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-pipeline/accepted/pipeline-verification.qst"))]
    (is (= :accepted (:status gravity-result)))
    (is (= :accepted (:status qst-result)))
    (is (str/ends-with? (get-in gravity-result
                                [:provenance :actual-source-path])
                        ".gravity"))
    (is (str/ends-with? (get-in qst-result
                                [:provenance :actual-source-path])
                        ".qst"))))

(deftest w5-pipeline-rejects-every-named-c1-through-c10-family
  (doseq [[function expected-rule] rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan]
              [[accepted-gravity-plan rejected-gravity-plan]
               [accepted-qst-plan rejected-qst-plan]]]
        (let [base
              (if (= accepted-plan accepted-qst-plan)
                (request-at accepted-plan
                            'w5-pipeline-verification-request-at
                            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-pipeline/accepted/pipeline-verification.qst")
                (request accepted-plan 'w5-pipeline-verification-request))
              invalid (invoke rejected-plan function [base])
              result (verify invalid)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= :gravity/diagnostic (:artifact diagnostic)))
          (is (.startsWith (:diagnostic-id diagnostic) "diagnostic:"))
          (is (= :error (:severity diagnostic)))
          (is (= :active (:lifecycle diagnostic)))
          (is (keyword? (:message-key diagnostic)))
          (is (= #{:span :syntax-id :artifact}
                 (set (keys (:primary diagnostic)))))
          (is (= #{:role :span :artifact}
                 (set (keys (first (:related diagnostic))))))
          (is (seq (:origin-chain diagnostic)))
          (is (= (:artifact (:primary diagnostic))
                 (get-in diagnostic [:involved-artifacts :input])))
          (is (= (:artifact (first (:related diagnostic)))
                 (get-in diagnostic [:involved-artifacts :output])))
          (is (= expected-rule (get-in diagnostic [:facts :failure-rule])))
          (is (= (:stage diagnostic)
                 (get-in diagnostic [:facts :failing-stage])))
          (is (true? (get-in diagnostic [:facts :source-span-preserved])))
          (is (true? (get-in diagnostic
                             [:facts :generated-origin-preserved])))
          (is (= (:source-span invalid)
                 (get-in diagnostic
                         [:facts :provenance-recovery :requested-span])))
          (is (= (get-in invalid [:provenance :generated-origin-chain])
                 (get-in diagnostic
                         [:facts :provenance-recovery
                          :requested-origin-chain])))
          (is (= 1 (count (:remediation diagnostic))))
          (is (keyword? (get-in diagnostic [:remediation 0 :kind])))
          (is (= [] (:redactions diagnostic)))
          (is (true? (invoke-engine 'w5-pipeline-diagnostic-valid?
                                    [invalid diagnostic])))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result)))
          (is (false? (:public-authority? result))))))))

(deftest w5-pipeline-stage-and-pass-graphs-are-adjacent
  (let [request-value (request accepted-gravity-plan
                               'w5-pipeline-verification-request)
        stages (:stage-artifacts request-value)
        passes (:passes request-value)
        artifact-identities (:artifact-identities request-value)]
    (doseq [index (range 0 (count stages))]
      (is (= (nth artifact-identities index)
             (:artifact-identity (nth stages index)))))
    (doseq [index (range 1 (count stages))]
      (is (= (:output-id (nth stages (dec index)))
             (:input-id (nth stages index))))
      (is (= (:output-ir (nth passes (dec index)))
             (:input-ir (nth passes index))))
      (is (= (:input-id (nth stages index))
             (:input-ir (nth passes index))))
      (is (= (:output-id (nth stages index))
             (:output-ir (nth passes index)))))))

(deftest w5-pipeline-rejects-appended-valid-stage-and-pass-records
  (doseq [[accepted-plan rejected-plan source-path]
          [[accepted-gravity-plan rejected-gravity-plan nil]
           [accepted-qst-plan rejected-qst-plan
            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-pipeline/accepted/pipeline-verification.qst"]]]
    (let [base (if source-path
                 (request-at accepted-plan
                             'w5-pipeline-verification-request-at source-path)
                 (request accepted-plan 'w5-pipeline-verification-request))]
      (doseq [mutator ['w5-pipeline-invalid-appended-stage
                       'w5-pipeline-invalid-appended-pass]]
        (let [invalid (invoke rejected-plan mutator [base])
              result (verify invalid)]
          (is (= 17 (count (if (= mutator
                                  'w5-pipeline-invalid-appended-stage)
                             (:stage-artifacts invalid)
                             (:passes invalid)))))
          (is (false? (invoke-engine 'w5-pipeline-request-shape-valid?
                                     [invalid])))
          (is (false? (invoke-engine 'w5-pipeline-cross-links-valid?
                                     [invalid])))
          (is (= :rejected (:status result)))
          (is (= "C1-PIPELINE"
                 (get-in result [:diagnostics 0 :rule]))))))))

(deftest w5-pipeline-c15-validator-rejects-structured-substitution
  (let [request-value (request accepted-gravity-plan
                               'w5-pipeline-verification-request)
        invalid (invoke rejected-gravity-plan
                        'w5-pipeline-invalid-adjacent-stage-link
                        [request-value])
        result (verify invalid)
        diagnostic (first (:diagnostics result))
        substituted (assoc-in diagnostic [:primary :span :end] -1)
        id-substituted (assoc diagnostic :diagnostic-id "diagnostic:forged")]
    (is (true? (invoke-engine 'w5-pipeline-diagnostic-valid?
                              [invalid diagnostic])))
    (is (false? (invoke-engine 'w5-pipeline-diagnostic-valid?
                               [invalid substituted])))
    (is (false? (invoke-engine 'w5-pipeline-diagnostic-valid?
                               [invalid id-substituted])))))

(deftest w5-pipeline-c15-validator-rejects-coherent-semantic-forgery
  (let [request-value (request accepted-gravity-plan
                               'w5-pipeline-verification-request)
        invalid (invoke rejected-gravity-plan
                        'w5-pipeline-invalid-adjacent-stage-link
                        [request-value])
        diagnostic (first (:diagnostics (verify invalid)))
        forged-rule "C1-PIPELINE"
        forged
        (-> diagnostic
            (assoc :rule forged-rule)
            (assoc :message-key :compiler.pipeline.forged-failure)
            (assoc :remediation [{:kind :accept-disconnected-graph}])
            (assoc-in [:facts :failure-rule] forged-rule))
        forged
        (assoc forged :diagnostic-id
               (str "diagnostic:" forged-rule ":" (:stage forged) ":"
                    (get-in forged [:primary :artifact]) ":"
                    (get-in forged [:facts :pass-id]) ":"
                    (get-in forged [:facts :pass-version]) ":"
                    (get-in forged [:facts :risk])))]
    (is (= forged-rule (get-in forged [:facts :failure-rule])))
    (is (.startsWith (:diagnostic-id forged)
                     (str "diagnostic:" forged-rule ":")))
    (is (false? (invoke-engine 'w5-pipeline-diagnostic-valid?
                               [invalid forged])))))

(deftest w5-pipeline-c15-malformed-provenance-is-explicit-fallback
  (doseq [[accepted-plan rejected-plan source-path]
          [[accepted-gravity-plan rejected-gravity-plan nil]
           [accepted-qst-plan rejected-qst-plan
            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-pipeline/accepted/pipeline-verification.qst"]]]
    (let [base (if source-path
                 (request-at accepted-plan
                             'w5-pipeline-verification-request-at source-path)
                 (request accepted-plan 'w5-pipeline-verification-request))
          span-invalid
          (invoke rejected-plan 'w5-pipeline-invalid-diagnostic-span [base])
          span-diagnostic (first (:diagnostics (verify span-invalid)))
          origin-invalid
          (invoke rejected-plan 'w5-pipeline-invalid-diagnostic-origin [base])
          origin-diagnostic (first (:diagnostics (verify origin-invalid)))
          both-invalid
          (invoke rejected-plan 'w5-pipeline-invalid-diagnostic-provenance [base])
          both-diagnostic (first (:diagnostics (verify both-invalid)))]
      (is (= :active (:lifecycle span-diagnostic)))
      (is (false? (get-in span-diagnostic
                          [:facts :source-span-preserved])))
      (is (true? (get-in span-diagnostic
                         [:facts :generated-origin-preserved])))
      (is (= :explicit-fallback
             (get-in span-diagnostic
                     [:facts :provenance-recovery :span-source])))
      (is (= {:source-id :gravity.self-hosting/w5-pipeline
              :start 0 :end 0}
             (get-in span-diagnostic [:primary :span])))
      (is (= :request
             (get-in origin-diagnostic
                     [:facts :provenance-recovery :span-source])))
      (is (true? (get-in origin-diagnostic
                         [:facts :source-span-preserved])))
      (is (false? (get-in origin-diagnostic
                          [:facts :generated-origin-preserved])))
      (is (= :explicit-fallback
             (get-in origin-diagnostic
                     [:facts :provenance-recovery :origin-source])))
      (is (= [{:kind :source
               :source-id :gravity.self-hosting/w5-pipeline
               :origin-id "diagnostic/explicit-provenance-fallback"}]
             (:origin-chain origin-diagnostic)))
      (is (false? (get-in both-diagnostic
                          [:facts :source-span-preserved])))
      (is (false? (get-in both-diagnostic
                          [:facts :generated-origin-preserved])))
      (doseq [[invalid diagnostic]
              [[span-invalid span-diagnostic]
               [origin-invalid origin-diagnostic]
               [both-invalid both-diagnostic]]]
        (is (true? (invoke-engine 'w5-pipeline-diagnostic-valid?
                                  [invalid diagnostic])))))))

(deftest w5-pipeline-c15-rejects-lifecycle-and-coherent-provenance-substitution
  (let [request-value (request accepted-gravity-plan
                               'w5-pipeline-verification-request)
        invalid (invoke rejected-gravity-plan
                        'w5-pipeline-invalid-adjacent-stage-link
                        [request-value])
        result (verify invalid)
        diagnostic (first (:diagnostics result))
        alternate-span {:source-id :fixture/w5-pipeline :start 400 :end 401}
        span-substituted
        (-> diagnostic
            (assoc-in [:primary :span] alternate-span)
            (assoc-in [:related 0 :span] alternate-span)
            (assoc-in [:facts :provenance-recovery :requested-span]
                      alternate-span))
        alternate-origins
        [{:kind :source :source-id :fixture/w5-pipeline
          :origin-id "diagnostic/coherent-substitution"}]
        origin-substituted
        (-> diagnostic
            (assoc :origin-chain alternate-origins)
            (assoc-in [:facts :provenance-recovery :requested-origin-chain]
                      alternate-origins))
        lifecycle-substituted
        (assoc diagnostic :lifecycle :resolved-by-fix)
        span-result-substituted
        (assoc-in result [:diagnostics 0] span-substituted)
        origin-result-substituted
        (assoc-in result [:diagnostics 0] origin-substituted)]
    (is (= :passed
           (:status (invoke-engine 'w5-pipeline-verify-result
                                   [invalid result]))))
    (is (false? (invoke-engine 'w5-pipeline-diagnostic-valid?
                               [invalid span-substituted])))
    (is (false? (invoke-engine 'w5-pipeline-diagnostic-valid?
                               [invalid origin-substituted])))
    (is (false? (invoke-engine 'w5-pipeline-diagnostic-valid?
                               [invalid lifecycle-substituted])))
    (is (= :rejected
           (:status (invoke-engine 'w5-pipeline-verify-result
                                   [invalid span-result-substituted]))))
    (is (= :rejected
           (:status (invoke-engine 'w5-pipeline-verify-result
                                   [invalid origin-result-substituted]))))))

(deftest w5-pipeline-recomputes-after-mutation-and-rejects-substitution
  (let [request-value (request accepted-gravity-plan
                               'w5-pipeline-verification-request)
        result (verify request-value)
        substituted (assoc-in result [:verifier-gate :decision] :accepted)
        substitution-check
        (invoke-engine 'w5-pipeline-verify-result
                       [request-value substituted])
        semantic-mutation
        (assoc-in request-value [:passes 2 :output-ir] :substituted-ir)
        mutation-check
        (invoke-engine 'w5-pipeline-verify-result
                       [semantic-mutation result])
        producer-flags
        (assoc result :clojure-seed-boundary? false
               :self-hosted? true :release? true)
        producer-check
        (invoke-engine 'w5-pipeline-verify-result
                       [request-value producer-flags])]
    (is (= :passed
           (:status
            (invoke-engine 'w5-pipeline-verify-result
                           [request-value result]))))
    (is (= :rejected (:status substitution-check)))
    (is (= "C1-SELF-HOST"
           (get-in substitution-check [:diagnostics 0 :rule])))
    (is (= :rejected (:status mutation-check)))
    (is (= "C1-SELF-HOST"
           (get-in mutation-check [:diagnostics 0 :rule])))
    (is (= :rejected (:status producer-check)))
    (is (= "C1-SELF-HOST"
           (get-in producer-check [:diagnostics 0 :rule])))))
