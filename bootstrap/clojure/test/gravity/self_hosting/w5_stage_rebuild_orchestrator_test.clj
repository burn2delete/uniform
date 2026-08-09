(ns gravity.self-hosting.w5-stage-rebuild-orchestrator-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-stage-rebuild-orchestrator-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_stage_rebuild_orchestrator_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 stage-rebuild test source is not on the classpath"
        {:id "W5-SR-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-SR-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_stage_rebuild_orchestrator.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-stage-rebuild")

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan engine-source)))
(def ^:private accepted-gravity-plan
  (delay (compile-plan
          (fixture-path "accepted" "stage-rebuild" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan
          (fixture-path "accepted" "stage-rebuild" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (fixture-path "rejected" "invalid-stage-rebuild" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (fixture-path "rejected" "invalid-stage-rebuild" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-stage-rebuild-orchestrator
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- orchestrate [request-value]
  (invoke engine-plan 'w5-stage-rebuild-orchestrate [request-value]))

(def ^:private rejected-cases
  {'w5-stage-rebuild-invalid-catalog-request
   "W5-SR-SOURCE-CATALOG"
   'w5-stage-rebuild-invalid-span-request
   "W5-SR-SOURCE-CATALOG"
   'w5-stage-rebuild-invalid-span-order-request
   "W5-SR-SOURCE-CATALOG"
   'w5-stage-rebuild-invalid-provenance-request
   "W5-SR-SOURCE-CATALOG"
   'w5-stage-rebuild-invalid-source-keyset-request
   "W5-SR-SOURCE-CATALOG"
   'w5-stage-rebuild-invalid-source-entry-keyset-request
   "W5-SR-SOURCE-CATALOG"
   'w5-stage-rebuild-invalid-source-identity-request
   "W5-SR-SOURCE-IDENTITY"
   'w5-stage-rebuild-invalid-source-unit-coherent-substitution-request
   "W5-SR-SOURCE-IDENTITY"
   'w5-stage-rebuild-invalid-stage-order-request
   "W5-SR-STAGE-ORDER"
   'w5-stage-rebuild-invalid-lineage-request
   "W5-SR-STAGE-LINEAGE"
   'w5-stage-rebuild-invalid-stage-provenance-request
   "W5-SR-STAGE-LINEAGE"
   'w5-stage-rebuild-invalid-stage-keyset-request
   "W5-SR-STAGE-LINEAGE"
   'w5-stage-rebuild-invalid-executable-identity-request
   "W5-SR-EXECUTABLE"
   'w5-stage-rebuild-invalid-compiler-keyset-request
   "W5-SR-EXECUTABLE"
   'w5-stage-rebuild-invalid-artifact-set-request
   "W5-SR-ARTIFACT-SET"
   'w5-stage-rebuild-invalid-artifact-provenance-request
   "W5-SR-ARTIFACT-SET"
   'w5-stage-rebuild-invalid-artifact-keyset-request
   "W5-SR-ARTIFACT-SET"
   'w5-stage-rebuild-invalid-artifact-lineage-request
   "W5-SR-ARTIFACT-SET"
   'w5-stage-rebuild-invalid-recipe-request
   "W5-SR-RECIPE"
   'w5-stage-rebuild-invalid-environment-request
   "W5-SR-ENVIRONMENT"
   'w5-stage-rebuild-invalid-lock-request
   "W5-SR-LOCK"
   'w5-stage-rebuild-invalid-toolchain-request
   "W5-SR-TOOLCHAIN"
   'w5-stage-rebuild-invalid-target-request
   "W5-SR-TARGET"
   'w5-stage-rebuild-invalid-conformance-request
   "W5-SR-CONFORMANCE"
   'w5-stage-rebuild-invalid-matrix-request
   "W5-SR-MATRIX"
   'w5-stage-rebuild-invalid-equivalence-request
   "W5-SR-EQUIVALENCE"
   'w5-stage-rebuild-invalid-boot7-compiler-operands-request
   "W5-SR-EQUIVALENCE"
   'w5-stage-rebuild-invalid-boot7-artifact-operands-request
   "W5-SR-EQUIVALENCE"
   'w5-stage-rebuild-invalid-coherent-id-substitution-request
   "W5-SR-STAGE-LINEAGE"
   'w5-stage-rebuild-invalid-evidence-class-request
   "W5-SR-EVIDENCE"
   'w5-stage-rebuild-invalid-evidence-keyset-request
   "W5-SR-EVIDENCE"
   'w5-stage-rebuild-invalid-no-fallback-request
   "W5-SR-AUTHORITY"
   'w5-stage-rebuild-invalid-authority-request
   "W5-SR-AUTHORITY"})

(deftest w5-stage-rebuild-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-stage-rebuild-policy
            w5-stage-rebuild-stage-order
            w5-stage-rebuild-target-contract
            w5-stage-rebuild-diagnostic-catalog
            w5-stage-rebuild-diagnostic
            w5-stage-rebuild-span-valid?
            w5-stage-rebuild-diagnostic-source-span
            w5-stage-rebuild-source-inventory
            w5-stage-rebuild-boot5-matrix
            w5-stage-rebuild-boot5-matrix-valid?
            w5-stage-rebuild-boot7-equivalence-valid?
            w5-stage-rebuild-identity-input
            w5-stage-rebuild-orchestrate
            w5-stage-rebuild-rebuild
            w5-stage-rebuild-run
            w5-stage-rebuild-execute
            w5-stage-rebuild-verify
            w5-stage-rebuild-recompute
            w5-stage-rebuild-structural-recompute
            w5-stage-rebuild-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function)))
  (doseq [[family basename]
          [["accepted" "stage-rebuild"]
           ["rejected" "invalid-stage-rebuild"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst"))))))

(deftest w5-stage-rebuild-policy-is-static-and-nonauthority
  (let [policy (invoke engine-plan 'w5-stage-rebuild-policy [])
        target (invoke engine-plan 'w5-stage-rebuild-target-contract [])]
    (is (= :gravity/w5-stage-rebuild-orchestrator-policy
           (:artifact policy)))
    (is (= :stage-owned-nonauthority (:scope policy)))
    (is (= :jvm (:harness-target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= :llvm-x86_64-linux (:target target)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :object-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:stage1 :stage2 :stage3] (:stage-order policy)))
    (is (some #{"W5-SR-SOURCE-CATALOG"} (:rules
                                           (invoke engine-plan
                                                   'w5-stage-rebuild-diagnostic-catalog
                                                   []))))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (= :non-authority (:authority policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-platforms policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (is (= [{:target :darwin :invokes-clojure? false :links-jvm? false
             :fallback? false}
            {:target :darwin-arm64 :invokes-clojure? false :links-jvm? false
             :fallback? false}
            {:target :darwin-x86_64 :invokes-clojure? false :links-jvm? false
             :fallback? false}
            {:target :windows :invokes-clojure? false :links-jvm? false
             :fallback? false}]
           (:unsupported-target-policies policy)))
    (is (false? (:cross-target-inference? policy)))
    (is (false? (:darwin-fallback? policy)))
    (is (true? (:no-fallback? policy)))
    (is (= :sysv-amd64 (:abi target)))
    (is (true? (:no-fallback? target)))))

(deftest w5-stage-rebuild-accepts-positive-record-but-blocks-completion
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [request-value (request plan 'w5-stage-rebuild-request)
          result (orchestrate request-value)
          stages (:stages result)
          sets (:artifact-sets result)
          inventory (:source-inventory request-value)
          entries (:entries inventory)
          compiler (:compiler request-value)
          equivalence (:boot7-equivalence-input request-value)]
      (is (= :accepted (:status result)))
      (is (= :incomplete (:completion result)))
      (is (= :blocked (get-in result [:verifier-gate :decision])))
      (is (false? (get-in result [:verifier-gate :release-eligible?])))
      (is (= 3 (count (:stage-order result))))
      (is (= :llvm-x86_64-linux
             (get-in result [:candidate-target :target])))
      (is (= :sysv-amd64
             (get-in result [:candidate-target :abi])))
      (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
             (get-in result [:candidate-target :unsupported-targets])))
      (is (every? #(and (= false (:invokes-clojure? %))
                        (= false (:links-jvm? %))
                        (= false (:fallback? %)))
                  (get-in result [:candidate-target
                                  :unsupported-target-policies])))
      (is (false? (get-in result [:candidate-target
                                   :candidate-invokes-clojure?])))
      (is (false? (get-in result [:candidate-target
                                   :candidate-links-jvm?])))
      (is (true? (get-in result [:candidate-target :no-fallback?])))
      (is (= :stage1 (:stage-id (get stages :stage1))))
      (is (= :stage2 (:stage-id (get stages :stage2))))
      (is (= :stage3 (:stage-id (get stages :stage3))))
      (is (= [:compiler-source-a :compiler-source-b :compiler-source-c]
             (mapv :source-unit-id entries)))
      (is (= [[] [:compiler-source-a]
              [:compiler-source-a :compiler-source-b]]
             (mapv :dependencies entries)))
      (is (= [(:inventory-id inventory) (:inventory-id inventory)]
             [(:source-inventory-id compiler)
              (:compiler-source-inventory-id compiler)]))
      (is (= (mapv :content-id entries)
             (mapv :compiler-source-id
                   [(get stages :stage1)
                    (get stages :stage2)
                    (get stages :stage3)])))
      (is (= (:artifact-executable-id (get stages :stage1))
             (:compiled-by-parent-executable-id (get stages :stage2))))
      (is (= (:artifact-executable-id (get stages :stage1))
             (:compiled-by-executable-id (get stages :stage2))))
      (is (= (:artifact-executable-id (get stages :stage2))
             (:compiled-by-parent-executable-id (get stages :stage3))))
      (is (= (:artifact-executable-id (get stages :stage2))
             (:compiled-by-executable-id (get stages :stage3))))
      (is (= (:compiler-executable-id (get stages :stage1))
             (:compiled-by-executable-id (get sets :stage1))))
      (is (= (:artifact-executable-id (get stages :stage1))
             (:compiled-by-executable-id (get sets :stage2))))
      (is (= (:artifact-executable-id (get stages :stage2))
             (:compiled-by-executable-id (get sets :stage3))))
      (is (true? (invoke engine-plan
                         'w5-stage-rebuild-boot5-matrix-valid?
                         [(:boot5-matrix request-value)])))
      (is (true? (invoke engine-plan
                         'w5-stage-rebuild-boot7-equivalence-valid?
                         [request-value])))
      (is (= (get-in stages [:stage2 :artifact-executable-id])
             (:compiler-a-id equivalence)))
      (is (= (get-in stages [:stage3 :artifact-executable-id])
             (:compiler-b-id equivalence)))
      (is (= (get-in sets [:stage2 :artifact-set-id])
             (:artifact-a-id equivalence)))
      (is (= (get-in sets [:stage3 :artifact-set-id])
             (:artifact-b-id equivalence)))
      (is (= :structural-only (:recomputation-mode result)))
      (is (= :pending (get-in result [:hash-validation :status])))
      (is (= :pending (get-in result [:hash-validation :raw-byte-hashes])))
      (is (= :pending (get-in result [:hash-validation :canonical-byte-hashes])))
      (is (every? #(= :pending (:actual-execution-status %))
                  (vals stages)))
      (is (every? #(= :pending (:independent-evidence-status %))
                  (vals stages)))
      (is (every? #(= :pending (:status %)) (vals sets)))
      (is (every? #(= :pending
                      (get-in % [:artifacts 0 :status]))
                  (vals sets)))
      (is (empty? (:diagnostics result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (= :non-authority (:authority result)))
      (is (not (str/includes? (pr-str request-value) ":source-ownership")))
      (is (not (str/includes? (pr-str request-value) ":check-only")))
      (is (not (str/includes? (pr-str request-value) ":replay"))))))

(deftest w5-stage-rebuild-invalid-span-uses-deterministic-fallback
  (let [base (request accepted-gravity-plan 'w5-stage-rebuild-request)
        invalid (invoke rejected-gravity-plan
                        'w5-stage-rebuild-invalid-span-request
                        [base])
        result (orchestrate invalid)
        diagnostic (first (:diagnostics result))]
    (is (= :rejected (:status result)))
    (is (true? (invoke engine-plan 'w5-stage-rebuild-span-valid?
                        [(:source-span diagnostic)])))
    (is (= :gravity.self-hosting/w5-stage-rebuild
           (get-in diagnostic [:source-span :source-id])))
    (is (= 0 (get-in diagnostic [:source-span :start-byte])))
    (is (= 0 (get-in diagnostic [:source-span :end-byte])))))

(deftest w5-stage-rebuild-source-and-boot7-coherent-substitutions-reject
  (doseq [plan [rejected-gravity-plan rejected-qst-plan]]
    (let [source-request
          (request plan
                   'w5-stage-rebuild-invalid-source-unit-coherent-substitution-request)
          source-entries (get-in source-request [:source-inventory :entries])
          source-result (orchestrate source-request)
          compiler-request
          (request plan
                   'w5-stage-rebuild-invalid-boot7-compiler-operands-request)
          compiler-input (:boot7-equivalence-input compiler-request)
          compiler-report (:boot7-equivalence-report compiler-request)
          compiler-result (orchestrate compiler-request)
          artifact-request
          (request plan
                   'w5-stage-rebuild-invalid-boot7-artifact-operands-request)
          artifact-input (:boot7-equivalence-input artifact-request)
          artifact-report (:boot7-equivalence-report artifact-request)
          artifact-result (orchestrate artifact-request)]
      (is (= [(get-in source-entries [0 :source-unit-id])]
             (get-in source-entries [1 :dependencies])))
      (is (= [(get-in source-entries [0 :source-unit-id])
              (get-in source-entries [1 :source-unit-id])]
             (get-in source-entries [2 :dependencies])))
      (is (= :rejected (:status source-result)))
      (is (= "W5-SR-SOURCE-IDENTITY"
             (get-in source-result [:diagnostics 0 :rule])))
      (is (= (:compiler-a-id compiler-input)
             (:compiler-a-id compiler-report)))
      (is (not= (get-in compiler-request
                        [:stages :stage2 :artifact-executable-id])
                (:compiler-a-id compiler-input)))
      (is (= :rejected (:status compiler-result)))
      (is (= "W5-SR-EQUIVALENCE"
             (get-in compiler-result [:diagnostics 0 :rule])))
      (is (= (:artifact-a-id artifact-input)
             (:artifact-a-id artifact-report)))
      (is (not= (get-in artifact-request
                        [:artifact-sets :stage2 :artifact-set-id])
                (:artifact-a-id artifact-input)))
      (is (= :rejected (:status artifact-result)))
      (is (= "W5-SR-EQUIVALENCE"
             (get-in artifact-result [:diagnostics 0 :rule]))))))

(deftest w5-stage-rebuild-identity-is-path-neutral-and-provenance-retains-path
  (let [left-request (request accepted-gravity-plan
                               'w5-stage-rebuild-request)
        right-request
        (request accepted-gravity-plan
                 'w5-stage-rebuild-alternate-path-request)
        left (orchestrate left-request)
        right (orchestrate right-request)]
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b/")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a/"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b/"))))

(deftest w5-stage-rebuild-rejected-fixture-covers-every-family
  (doseq [[function-name expected-rule] rejected-cases]
    (testing (str function-name)
      (doseq [[accepted-plan rejected-plan]
              [[accepted-gravity-plan rejected-gravity-plan]
               [accepted-qst-plan rejected-qst-plan]]]
        (let [base (request accepted-plan 'w5-stage-rebuild-request)
              invalid (invoke rejected-plan function-name [base])
              result (orchestrate invalid)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= :stage-rebuild (:stage diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :llvm-x86_64-linux (:target diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (map? (:provenance diagnostic)))
          (is (keyword? (:remediation diagnostic)))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result)))
          (is (false? (:public-authority? result))))))))

(deftest w5-stage-rebuild-result-verifier-recomputes
  (let [request-value (request accepted-gravity-plan
                               'w5-stage-rebuild-request)
        result (orchestrate request-value)
        verification
        (invoke engine-plan 'w5-stage-rebuild-verify-result
                [request-value result])
        altered (assoc result :completion :candidate-complete)
        altered-verification
        (invoke engine-plan 'w5-stage-rebuild-verify-result
                [request-value altered])]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :rejected (:status altered-verification)))
    (is (= "W5-SR-SUBSTITUTION"
           (get-in altered-verification [:diagnostics 0 :rule])))
    (is (= :non-authority (:authority altered-verification)))))
