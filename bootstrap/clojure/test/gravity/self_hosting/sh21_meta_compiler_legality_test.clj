(ns gravity.self-hosting.sh21-meta-compiler-legality-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh21_meta_compiler_legality_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-21 test source is not on the classpath"
                {:id "SH21-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH21-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-21")

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
    "bootstrap/gravity/src/gravity/self_hosting/meta_compiler_legality.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "meta-program" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "meta-program" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-meta-programs" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-meta-programs" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh21-meta-legality-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine
  [function arguments]
  (invoke engine-plan function arguments))

(defn- program
  [plan function]
  (invoke plan function []))

(defn- check
  [program]
  (invoke-engine 'sh21-check-program [program]))

(def ^:private rejected-cases
  {'sh21-nonhermetic-program
   ["P3-HERMETIC" :hermetic-mode-required]
   'sh21-wrong-profile-program
   ["P3-GENERATED-PROFILE" :compiler-module-profile-must-be-meta]
   'sh21-forbidden-effect-program
   ["P3-HERMETIC" :effect-not-allowed-in-hermetic-meta]
   'sh21-missing-capability-program
   ["P3-COMPILER-CAPABILITY" :effect-capability-set-mismatch]
   'sh21-extra-capability-program
   ["P3-COMPILER-CAPABILITY" :effect-capability-set-mismatch]
   'sh21-oversized-effect-set-program
   ["P3-BUILD-EFFECT" :effect-bound-exceeded]
   'sh21-nonreplayable-input-program
   ["P3-HERMETIC" :invalid-or-nonreplayable-build-input]
   'sh21-missing-input-program
   ["P3-HERMETIC" :declared-build-input-set-mismatch]
   'sh21-malformed-dependencies-program
   ["P3-PASS-CONTRACT" :dependency-vector-required]
   'sh21-missing-dependency-program
   ["P3-PASS-CONTRACT" :missing-module-dependency]
   'sh21-cycle-program
   ["P3-PASS-CONTRACT" :compiler-module-dependency-cycle]
   'sh21-duplicate-module-program
   ["P3-PASS-CONTRACT" :duplicate-module-identity]
   'sh21-order-mismatch-program
   ["P3-PASS-CONTRACT" :explicit-module-order-required]
   'sh21-nontopological-order-program
   ["P3-PASS-CONTRACT" :module-order-not-topological]
   'sh21-missing-pass-output-program
   ["P3-PASS-CONTRACT" :pass-output-required]
   'sh21-missing-regenerates-program
   ["P3-PASS-CONTRACT" :regenerates-set-required]
   'sh21-fact-overlap-program
   ["P3-FACT-INVALIDATION"
    :preserved-and-invalidated-fact-overlap]
   'sh21-oversized-pass-facts-program
   ["P3-FACT-INVALIDATION" :preserved-fact-bound-exceeded]
   'sh21-invalid-generated-profile-program
   ["P3-GENERATED-PROFILE" :target-profile-not-recognized]
   'sh21-unchecked-generated-code-program
   ["P3-GENERATED-PROFILE" :generated-code-recheck-required]
   'sh21-missing-generated-safety-program
   ["P3-GENERATED-SAFETY" :generated-safety-provenance-required]
   'sh21-runtime-capture-program
   ["P3-PHASE" :runtime-value-captured-at-compile-time]
   'sh21-missing-origin-program
   ["P3-SOURCE-MAP" :module-origin-chain-required]
   'sh21-runtime-assumption-program
   ["P3-HERMETIC" :runtime-assumption-not-allowed]
   'sh21-missing-lineage-program
   ["P3-PASS-CONTRACT" :compiler-lineage-required]
   'sh21-cyclic-lineage-program
   ["P3-PASS-CONTRACT" :compiler-lineage-cycle]
   'sh21-oversized-program
   ["P3-PASS-CONTRACT" :module-bound-exceeded]})

(deftest sh21-engine-and-co-canonical-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh21-meta-policy sh21-check-program sh21-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [[family basename]
          [["accepted" "meta-program"]
           ["rejected" "invalid-meta-programs"]]]
    (is (= (slurp
            (path (fixture-relative-path family basename ".gravity")))
           (slurp
            (path (fixture-relative-path family basename ".qst")))))))

(deftest sh21-policy-is-bounded-hermetic-and-honest
  (let [policy (invoke-engine 'sh21-meta-policy [])]
    (is (= :gravity/sh21-meta-legality-policy (:artifact policy)))
    (is (= 1 (:version policy)))
    (is (= :meta (:profile policy)))
    (is (= 64 (:maximum-modules policy)))
    (is (= 256 (:maximum-dependencies policy)))
    (is (= 16 (:maximum-effects-per-module policy)))
    (is (= 16 (:maximum-capabilities-per-module policy)))
    (is (= 64 (:maximum-pass-facts-per-set policy)))
    (is (= 64 (:maximum-pass-artifacts policy)))
    (is (= 32 (:maximum-compiler-lineage policy)))
    (is (= 64 (:maximum-generated-records-per-module policy)))
    (is (contains? (:allowed-effects policy) :compiler/read-ir))
    (is (contains? (:allowed-effects policy) :build/read-file))
    (is (contains? (:forbidden-ambient-effects policy)
                   :build/network))
    (is (contains? (:forbidden-ambient-effects policy)
                   :shell/exec))
    (is (contains? (:allowed-generated-target-profiles policy)
                   :native))
    (is (contains? (:diagnostics policy) "P3-HERMETIC"))
    (is (contains? (:diagnostics policy) "P3-SOURCE-MAP"))
    (is (some #{:all-authoritative-compiler-modules}
              (:pending policy)))
    (is (some #{:compiler-executable-under-meta}
              (:pending policy)))
    (is (some #{:seedless-execution} (:pending policy)))))

(deftest sh21-accepts-explicit-hermetic-meta-module-graph
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [program (program plan 'sh21-meta-program)
          result (check program)]
      (is (= :accepted (:status result)))
      (is (= :meta (:profile result)))
      (is (true? (:hermetic result)))
      (is (false? (:ambient-authority result)))
      (is (= 3 (:module-count result)))
      (is (= 2 (:dependency-count result)))
      (is (= [:compiler/reader :compiler/analyzer :compiler/emitter]
             (:module-order result)))
      (is (true? (get-in result [:dependency-graph :acyclic])))
      (is (= (:module-order result)
             (get-in result [:dependency-graph :order])))
      (is (empty? (:diagnostics result)))
      (is (= :passed
             (:status
              (invoke-engine
               'sh21-verify-result [program result])))))))

(deftest sh21-enforces-exact-effects-capabilities-and-replayable-inputs
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [program (program plan 'sh21-meta-program)
          modules (:modules program)
          reader (first modules)
          analyzer (second modules)
          emitter (nth modules 2)
          input (first (:build-inputs reader))]
      (is (= #{:memory/allocate :build/read-file}
             (:effects reader)))
      (is (= #{:build/read-file} (:capabilities reader)))
      (is (= :gravity/meta-build-input (:artifact input)))
      (is (= :source-file (:kind input)))
      (is (= :build/read-file (:effect input)))
      (is (= :build/read-file (:capability input)))
      (is (true? (:replayable input)))
      (is (string? (:content-id input)))
      (is (= #{:memory/allocate
               :compiler/read-ir
               :compiler/write-ir}
             (:effects analyzer)))
      (is (= #{:compiler/ir-read :compiler/ir-transform}
             (:capabilities analyzer)))
      (is (= #{:memory/allocate
               :compiler/read-ir
               :build/write-artifact}
             (:effects emitter)))
      (is (= #{:compiler/ir-read :build/write-artifact}
             (:capabilities emitter))))))

(deftest sh21-preserves-pass-generated-code-and-bootstrap-provenance
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [program (program plan 'sh21-meta-program)
          result (check program)
          analyzer (second (:modules program))
          contract (:pass-contract analyzer)
          generated (first (:generated-code analyzer))
          identity-module
          (second (get-in result [:identity-input :modules]))]
      (is (= :meta (:profile contract)))
      (is (= :syntax-tree (:input contract)))
      (is (= :checked-core (:output contract)))
      (is (= #{:source-spans :origin-chains :hygiene}
             (:preserves contract)))
      (is (= #{:syntax-index} (:invalidates contract)))
      (is (= #{:types :effects :profile-valid :safety-outcomes}
             (:regenerates contract)))
      (is (= :ordinary-compiler-pipeline
             (:recheck-boundary generated)))
      (is (true? (:recheck-required generated)))
      (is (= :runtime-checked
             (get-in generated
                     [:safety-provenance :safety-outcome])))
      (is (map? (:source-span generated)))
      (is (seq (:origin-chain generated)))
      (is (= ["sha256:compiler-stage" "sha256:seed-stage"]
             (:compiler-lineage identity-module)))
      (is (= :compiler/analyzer (:module-id identity-module))))))

(deftest sh21-keeps-physical-paths-outside-semantic-identity
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [first-program (program plan 'sh21-meta-program)
          second-program
          (program plan 'sh21-meta-program-alternate-path)
          first-result (check first-program)
          second-result (check second-program)]
      (is (= :accepted (:status first-result)))
      (is (= :accepted (:status second-result)))
      (is (not=
           (mapv :actual-source-path (:modules first-program))
           (mapv :actual-source-path (:modules second-program))))
      (is (= (:identity-input first-result)
             (:identity-input second-result)))
      (is (not= (:provenance first-result)
                (:provenance second-result)))
      (is (= (:dependency-graph first-result)
             (:dependency-graph second-result))))))

(deftest sh21-rejects-illegal-meta-programs-before-execution
  (doseq [[accepted-plan rejected-plan]
          [[accepted-gravity-plan rejected-gravity-plan]
           [accepted-qst-plan rejected-qst-plan]]]
    (let [base (program accepted-plan 'sh21-meta-program)]
      (doseq [[mutation [rule reason]] rejected-cases]
        (testing (str mutation)
          (let [invalid (invoke rejected-plan mutation [base])
                result (check invalid)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (nil? (:identity-input result)))))))))

(deftest sh21-verifier-recomputes-and-rejects-substitution
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [program (program plan 'sh21-meta-program)
          result (check program)
          substituted (assoc result :ambient-authority true)]
      (is (= :passed
             (:status
              (invoke-engine
               'sh21-verify-result [program result]))))
      (let [verification
            (invoke-engine
             'sh21-verify-result [program substituted])]
        (is (= :rejected (:status verification)))
        (is (= "P3-PASS-CONTRACT"
               (get-in verification [:diagnostics 0 :rule])))
        (is (= :meta-legality-result-substitution
               (get-in verification [:diagnostics 0 :reason])))))))
