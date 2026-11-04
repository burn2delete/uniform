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

(defn- rejects-with
  [program rule reason]
  (let [result (check program)]
    (is (= :rejected (:status result)))
    (is (= rule (get-in result [:diagnostics 0 :rule])))
    (is (= reason (get-in result [:diagnostics 0 :reason])))
    (is (nil? (:identity-input result)))))

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
   'sh21-unknown-effect-program
   ["P3-HERMETIC" :effect-not-allowed-in-hermetic-meta]
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
   ["P3-PASS-CONTRACT" :compiler-lineage-cycle]})

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

(deftest sh21-policy-is-hermetic-and-honest
  (let [policy (invoke-engine 'sh21-meta-policy [])]
    (is (= :gravity/sh21-meta-legality-policy (:artifact policy)))
    (is (= 1 (:version policy)))
    (is (= :meta (:profile policy)))
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

(deftest sh21-preserves-long-origins-and-rejects-malformed-chains
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (program plan 'sh21-meta-program)
          module-chain
          (mapv #(assoc (first (get-in base [:modules 1 :origin-chain]))
                        :source-id (str "source-" %))
                (range 65))
          generated-chain
          (mapv #(assoc (first (get-in base [:modules 1 :generated-code
                                           0 :origin-chain]))
                        :source-id (str "generated-" %))
                (range 65))
          program (-> base
                      (assoc-in [:modules 1 :origin-chain] module-chain)
                      (assoc-in [:modules 1 :generated-code 0 :origin-chain]
                                generated-chain))
          result (check program)]
      (is (= :accepted (:status result)))
      (is (= module-chain
             (get-in result [:identity-input :modules 1 :origin-chain])))
      (is (= module-chain
             (get-in result [:provenance :modules 1 :origin-chain])))
      (is (= generated-chain
             (get-in result [:identity-input :modules 1 :generated-code
                             0 :origin-chain])))
      (is (= result (check program)))
      (is (= :passed
             (:status (invoke-engine 'sh21-verify-result [program result]))))
      (doseq [[location reason]
              [[[:modules 1 :origin-chain] :module-origin-chain-required]
               [[:modules 1 :generated-code 0 :origin-chain]
                :generated-origin-chain-required]]
              malformed [nil [] '(:source)]]
        (let [rejected (check (assoc-in program location malformed))]
          (is (= :rejected (:status rejected)))
          (is (= "P3-SOURCE-MAP"
                 (get-in rejected [:diagnostics 0 :rule])))
          (is (= reason (get-in rejected [:diagnostics 0 :reason]))))))))

(deftest sh21-traverses-large-module-and-dependency-graphs
  (let [base (program accepted-gravity-plan 'sh21-meta-program)
        template (second (:modules base))
        ids (mapv #(str "module-" %) (range 258))
        ;; The final module has 257 dependencies. One branch has 67 edges,
        ;; so a valid graph also crosses the former cycle-search depth cap.
        modules (mapv (fn [index]
                        (assoc template
                               :module-id (ids index)
                               :dependencies
                               (cond
                                 (= index 257) (subvec ids 0 257)
                                 (<= 1 index 66) [(ids (dec index))]
                                 :else [])))
                      (range 258))
        program (assoc base :modules modules :module-order ids)
        result (check program)]
    (is (= :accepted (:status result)))
    (is (= 258 (:module-count result)))
    (is (= 323 (:dependency-count result)))
    (is (= ids (:module-order result)
           (get-in result [:dependency-graph :order])
           (mapv :module-id (get-in result [:identity-input :modules]))))
    (is (= (subvec ids 0 257)
           (get-in result [:identity-input :modules 257 :dependencies])))
    (is (= result (check program)))
    (is (= :passed
           (:status (invoke-engine 'sh21-verify-result [program result]))))
    (doseq [[location value rule reason]
            [[[:modules 257 :dependencies 256] :compiler/missing
              "P3-PASS-CONTRACT" :missing-module-dependency]
             [[:modules 257 :dependencies 256] (ids 0)
              "P3-PASS-CONTRACT" :duplicate-module-dependency]
             [[:modules 0 :dependencies] [(ids 66)]
              "P3-PASS-CONTRACT" :compiler-module-dependency-cycle]
             [[:modules 257 :dependencies] '(:compiler/reader)
              "P3-PASS-CONTRACT" :dependency-vector-required]
             [[:modules 257 :effects] #{:build/network}
              "P3-HERMETIC" :effect-not-allowed-in-hermetic-meta]]]
      (testing (str location " " reason)
        (rejects-with (assoc-in program location value) rule reason)))))

(deftest sh21-traverses-large-module-record-populations
  (let [base (program accepted-gravity-plan 'sh21-meta-program)
        input (first (get-in base [:modules 0 :build-inputs]))
        generated (first (get-in base [:modules 1 :generated-code]))
        inputs (mapv #(assoc input :input-token (str "input-" %)
                             :content-id (str "sha256:input-" %))
                     (range 65))
        records (mapv #(assoc generated :generated-id (str "generated-" %))
                      (range 65))
        lineage (mapv #(str "sha256:compiler-" %) (range 65))
        facts (fn [prefix] (set (map #(str prefix %) (range 65))))
        contract (assoc (get-in base [:modules 1 :pass-contract])
                        :requires (facts "required-")
                        :preserves (facts "preserved-")
                        :invalidates (facts "invalidated-")
                        :regenerates (facts "regenerated-")
                        :artifacts (mapv #(str "artifact-" %) (range 65)))
        program (-> base
                    (assoc-in [:modules 0 :build-inputs] inputs)
                    (assoc-in [:modules 1 :generated-code] records)
                    (assoc-in [:modules 1 :compiler-lineage] lineage)
                    (assoc-in [:modules 1 :pass-contract] contract))
        result (check program)]
    (is (= :accepted (:status result)))
    (is (= inputs (get-in result [:identity-input :modules 0 :build-inputs])))
    (is (= records (get-in result [:identity-input :modules 1 :generated-code])))
    (is (= lineage (get-in result [:identity-input :modules 1 :compiler-lineage])))
    (is (= contract (get-in result [:identity-input :modules 1 :pass-contract])))
    (is (= result (check program)))
    (is (= :passed
           (:status (invoke-engine 'sh21-verify-result [program result]))))
    (doseq [[location value rule reason]
            [[[:modules 0 :build-inputs 64 :replayable] false
              "P3-HERMETIC" :invalid-or-nonreplayable-build-input]
             [[:modules 0 :build-inputs 64 :capability] :build/read-env
              "P3-HERMETIC" :invalid-or-nonreplayable-build-input]
             [[:modules 1 :compiler-lineage 64] nil
              "P3-PASS-CONTRACT" :compiler-lineage-identity-required]
             [[:modules 1 :compiler-lineage 64] (first lineage)
              "P3-PASS-CONTRACT" :compiler-lineage-cycle]
             [[:modules 1 :pass-contract :profile] :hosted
              "P3-PASS-CONTRACT" :pass-profile-must-be-meta]
             [[:modules 1 :pass-contract :invalidates]
              (conj (:invalidates contract) "preserved-64")
              "P3-FACT-INVALIDATION" :preserved-and-invalidated-fact-overlap]
             [[:modules 1 :pass-contract :regenerates]
              (conj (:regenerates contract) "preserved-64")
              "P3-FACT-INVALIDATION" :preserved-and-regenerated-fact-overlap]
             [[:modules 1 :generated-code 64 :target-profile] :unknown
              "P3-GENERATED-PROFILE" :target-profile-not-recognized]
             [[:modules 1 :generated-code 64 :recheck-required] false
              "P3-GENERATED-PROFILE" :generated-code-recheck-required]
             [[:modules 1 :generated-code 64 :safety-provenance] nil
              "P3-GENERATED-SAFETY" :generated-safety-provenance-required]
             [[:modules 1 :generated-code 64 :origin-chain] []
              "P3-SOURCE-MAP" :generated-origin-chain-required]]]
      (testing (str location " " reason)
        (rejects-with (assoc-in program location value) rule reason)))
    ;; This is a coherent result from a different valid input, not just an
    ;; inconsistent edit to the stored output.
    (let [substitute (check (assoc-in program [:modules 0 :build-inputs 64
                                             :content-id]
                                     "sha256:substituted-input"))
          verification (invoke-engine 'sh21-verify-result [program substitute])]
      (is (= :accepted (:status substitute)))
      (is (= :rejected (:status verification)))
      (is (= "P3-PASS-CONTRACT" (get-in verification [:diagnostics 0 :rule])))
      (is (= :meta-legality-result-substitution
             (get-in verification [:diagnostics 0 :reason]))))))

(deftest sh21-retains-the-exact-authority-policy-without-population-checks
  (let [base (program accepted-gravity-plan 'sh21-meta-program)
        policy (invoke-engine 'sh21-meta-policy [])
        effects (:allowed-effects policy)
        capabilities #{:compiler/ir-read :compiler/ir-transform :compiler/plugin
                       :build/read-file :build/write-artifact :build/read-env
                       :build/target-probe}
        source-input (first (get-in base [:modules 0 :build-inputs]))
        inputs [source-input
                (assoc source-input :kind :environment :effect :build/env
                       :capability :build/read-env :input-token :environment)
                (assoc source-input :kind :target-manifest :effect :build/target-probe
                       :capability :build/target-probe :input-token :target)]
        program (-> base
                    (assoc-in [:modules 0 :effects] effects)
                    (assoc-in [:modules 0 :capabilities] capabilities)
                    (assoc-in [:modules 0 :build-inputs] inputs))]
    (is (= #{:memory/allocate :compiler/read-ir :compiler/write-ir :compiler/plugin
             :build/read-file :build/write-artifact :build/env :build/target-probe}
           effects))
    (is (= :accepted (:status (check program))))
    (rejects-with
     (assoc-in program [:modules 0 :effects]
               (into effects (map #(keyword "forbidden" (str %)) (range 17))))
     "P3-HERMETIC" :effect-not-allowed-in-hermetic-meta)
    (rejects-with
     (assoc-in program [:modules 0 :capabilities]
               (into capabilities (map #(keyword "ungranted" (str %)) (range 17))))
     "P3-COMPILER-CAPABILITY" :effect-capability-set-mismatch)
    (rejects-with
     (assoc-in program [:modules 0 :runtime-assumptions]
               (conj (:allowed-runtime-assumptions policy) :ambient-host-globals))
     "P3-HERMETIC" :runtime-assumption-not-allowed)))

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
