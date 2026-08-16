(ns gravity.self-hosting.w5-stage-equivalence-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-stage-equivalence-verifier-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_stage_equivalence_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 stage-equivalence verifier test is not on the classpath"
        {:id "W5-SE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-SE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_stage_equivalence_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-stage-equivalence")

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
  (delay
   (compile-plan (fixture-path "accepted" "stage-equivalence" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan (fixture-path "accepted" "stage-equivalence" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-stage-equivalence" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-stage-equivalence" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-stage-equivalence-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- compare-request [value]
  (invoke engine-plan 'w5-stage-equivalence-compare [value]))

(defn- all-equivalent-outcomes? [outcomes index]
  (if (= index (count outcomes))
    true
    (if (= :equivalent
           (get (get (get outcomes index) :outcome) :status))
      (all-equivalent-outcomes? outcomes (+ index 1))
      false)))

(def ^:private rejected-cases
  {'w5-invalid-request-shape ["BOOT7001" :request-shape]
   'w5-invalid-lineage ["BOOT7001" :lineage]
   'w5-invalid-artifact-drift ["BOOT7002" :artifact-drift]
   'w5-invalid-paired-stage-b-artifact ["BOOT7002" :artifact]
   'w5-invalid-manifest-drift ["BOOT7002" :manifest-drift]
   'w5-invalid-diagnostic-drift ["BOOT7003" :diagnostic-drift]
   'w5-invalid-diagnostic-shape ["BOOT7003" :diagnostic-evidence]
   'w5-invalid-diagnostic-evidence ["BOOT7003" :diagnostic-evidence]
   'w5-invalid-span-drift ["BOOT7003" :diagnostic-span-drift]
   'w5-invalid-conformance-drift ["BOOT7006" :conformance-drift]
   'w5-invalid-paired-conformance-transcript
   ["BOOT7006" :conformance-drift]
   'w5-invalid-runtime-output-drift ["BOOT7006" :runtime-output-drift]
   'w5-invalid-ir-normalization ["BOOT7002" :ir-normalization-drift]
   'w5-invalid-ir-renaming ["BOOT7002" :artifact]
   'w5-invalid-missing-stage-output ["BOOT7005" :missing-stage-output]
   'w5-invalid-unreviewed-delta ["BOOT7004" :unreviewed-delta]
   'w5-invalid-performance-bound ["BOOT7007" :performance-bound]
   'w5-invalid-environment ["BOOT7001" :environment]
   'w5-invalid-environment-random-seed ["BOOT7001" :environment]
   'w5-invalid-network ["BOOT7001" :environment]
   'w5-invalid-lock ["BOOT7001" :lock]
   'w5-invalid-toolchain ["BOOT7001" :toolchain]
   'w5-invalid-toolchain-compiler-id ["BOOT7001" :toolchain]
   'w5-invalid-toolchain-source-id ["BOOT7001" :toolchain]
   'w5-invalid-target ["BOOT7001" :target]
   'w5-invalid-cross-target ["BOOT7001" :target]
   'w5-invalid-fallback ["BOOT7001" :target]
   'w5-invalid-producer-authority ["BOOT7001" :producer-authority]})

(deftest w5-stage-equivalence-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :jvm (get (get @plan :module) :target))))
  (doseq [function
          '[w5-stage-equivalence-policy
            w5-stage-equivalence-modes
            w5-stage-equivalence-diagnostic-catalog
            w5-sha256-id?
            w5-stage-equivalence-request-valid?
            w5-stage-equivalence-compare
            w5-stage-equivalence-execute
            w5-stage-equivalence-run
            w5-stage-equivalence-recompute
            w5-stage-equivalence-verify
            w5-stage-equivalence-verify-result]]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (doseq [[family basename]
          [["accepted" "stage-equivalence"]
           ["rejected" "invalid-stage-equivalence"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-stage-equivalence-policy-is-static-and-nonauthority
  (let [policy (invoke engine-plan 'w5-stage-equivalence-policy [])
        modes (invoke engine-plan 'w5-stage-equivalence-modes [])
        catalog
        (invoke engine-plan 'w5-stage-equivalence-diagnostic-catalog [])]
    (is (= :gravity/w5-stage-equivalence-policy (:artifact policy)))
    (is (= [:artifact :manifest :diagnostic :conformance :runtime-output
            :ir-modulo-id :reviewed-delta]
           (:required-modes policy) modes))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-platforms policy)))
    (is (= [{:target :darwin :invokes-clojure? false
             :links-jvm? false :fallback? false}
            {:target :darwin-arm64 :invokes-clojure? false
             :links-jvm? false :fallback? false}
            {:target :darwin-x86_64 :invokes-clojure? false
             :links-jvm? false :fallback? false}
            {:target :windows :invokes-clojure? false
             :links-jvm? false :fallback? false}]
           (:unsupported-target-policies policy)))
    (is (false? (:candidate-invokes-clojure? policy)))
    (is (false? (:candidate-links-jvm? policy)))
    (is (true? (:no-fallback? policy)))
    (is (false? (:cross-target-inference? policy)))
    (is (false? (:darwin-fallback? policy)))
    (is (= :jvm (:stage2-harness-target policy)))
    (is (true? (:static-only? policy)))
    (is (= :incomplete (:closure-status policy)))
    (is (= :blocked (:completion-status policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (= :non-authority (:authority catalog)))
    (is (= (set ["BOOT7001" "BOOT7002" "BOOT7003" "BOOT7004"
                 "BOOT7005" "BOOT7006" "BOOT7007"])
           (set (:diagnostics catalog))))))

(deftest w5-stage-equivalence-accepts-static-record-but-blocks-completion
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [request-value (request plan 'w5-stage-equivalence-request)
          result (compare-request request-value)]
      (is (true? (invoke engine-plan
                          'w5-stage-equivalence-request-valid?
                          [request-value])))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:closure-status result)))
      (is (= :blocked (:completion-status result)))
      (is (= :blocked (:verification-status result)))
      (is (= :non-authority (:authority result)))
      (is (= :llvm-x86_64-linux (:candidate-target result)))
      (is (= (get request-value :target) (:target result)))
      (is (false? (:candidate-invokes-clojure? result)))
      (is (false? (:candidate-links-jvm? result)))
      (is (true? (:no-fallback? result)))
      (is (= 7 (count (:outcomes result))))
      (is (all-equivalent-outcomes? (:outcomes result) 0))
      (is (= :reviewed-delta
             (get (get (get (:outcomes result) 6) :outcome) :reason)))
      (is (= :stage-a (get (get request-value :stage-a) :stage-id)))
      (is (= (get (get request-value :stage-a) :output-artifact-id)
             (get (get request-value :transition-a-b)
                  :child-compiler-input-artifact-id)
             (get (get request-value :stage-b)
                  :compiler-input-artifact-id)))
      (is (= (get (get request-value :stage-b) :output-artifact-id)
             (get (get request-value :transition-b-c)
                  :child-compiler-input-artifact-id)
             (get (get request-value :stage-c)
                  :compiler-input-artifact-id)))
      (is (= :pending (get (get result :execution) :native-sol-evidence)))
      (is (empty? (:diagnostics result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (not (str/includes? (pr-str (:identity-input result))
                              "/checkout-a/")))
      (is (str/includes? (pr-str (:provenance result)) "/checkout-a/")))))

(deftest w5-stage-equivalence-identity-is-path-neutral-and-provenance-retains-path
  (let [left-request (request accepted-gravity-plan
                              'w5-stage-equivalence-request)
        right-request
        (request accepted-gravity-plan
                 'w5-stage-equivalence-alternate-path-request)
        left (compare-request left-request)
        right (compare-request right-request)]
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b/")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a/"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b/"))))

(deftest w5-stage-equivalence-rejected-fixture-covers-every-mode-and-control
  (doseq [[function-name [expected-rule expected-reason]] rejected-cases]
    (testing (str function-name)
      (doseq [[accepted-plan rejected-plan]
              [[accepted-gravity-plan rejected-gravity-plan]
               [accepted-qst-plan rejected-qst-plan]]]
        (let [base (request accepted-plan 'w5-stage-equivalence-request)
              invalid (invoke rejected-plan function-name [base])
              result (compare-request invalid)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= expected-reason (get (get diagnostic :facts) :reason)))
          (is (= :w5-stage-equivalence (:stage diagnostic)))
          (is (= :llvm-x86_64-linux (:candidate-target diagnostic)))
          (is (= (get base :target) (:target diagnostic)))
          (is (false? (:candidate-invokes-clojure?
                       (get diagnostic :facts))))
          (is (false? (:candidate-links-jvm?
                       (get diagnostic :facts))))
          (is (true? (:no-fallback? (get diagnostic :facts))))
          (is (= #{:source-id :start-byte :end-byte :line :column
                   :actual-source-path}
                 (set (keys (:source-span diagnostic)))))
          (is (= {:source-id :gravity.self-hosting/w5-stage-equivalence
                  :start-byte 0
                  :end-byte 0
                  :line 1
                  :column 1
                  :actual-source-path
                  (get (get base :provenance) :actual-comparison-path)}
                 (:source-span diagnostic)))
          (is (map? (:provenance diagnostic)))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result)))
          (is (false? (:public-authority? result)))
          (is (= :non-authority (:authority result))))))))

(deftest w5-stage-equivalence-paired-reference-drift-rejects-coherent-substitution
  (doseq [[function-name product-index field]
          [['w5-invalid-paired-stage-b-artifact 0 :stage-b]
           ['w5-invalid-paired-conformance-transcript
            3 :accepted-transcript]]]
    (let [base (request accepted-gravity-plan
                         'w5-stage-equivalence-request)
          invalid (invoke rejected-gravity-plan function-name [base])
          result (compare-request invalid)
          product (get (get invalid :products) product-index)
          left (get (get product :left) field)
          right (get (get product :right) field)
          diagnostic (first (:diagnostics result))]
      (is (= left right))
      (is (= (if (= product-index 0)
               (get-in base [:stage-b :output-artifact-id])
               (get-in base [:transcripts :accepted :transcript-id]))
             (if (= product-index 0)
               (get-in invalid [:stage-b :output-artifact-id])
               (get-in invalid [:transcripts :accepted :transcript-id]))))
      (is (= :rejected (:status result)))
      (is (= (if (= product-index 0)
               "BOOT7002"
               "BOOT7006")
             (:rule diagnostic)))
      (is (= (if (= product-index 0)
               :artifact
               :conformance-drift)
             (get (:facts diagnostic) :reason))))))

(deftest w5-stage-equivalence-coherence-mutators-preserve-other-fields
  (let [base (request accepted-gravity-plan
                       'w5-stage-equivalence-request)
        random-seed-invalid
        (invoke rejected-gravity-plan
                'w5-invalid-environment-random-seed [base])
        compiler-id-invalid
        (invoke rejected-gravity-plan
                'w5-invalid-toolchain-compiler-id [base])
        source-id-invalid
        (invoke rejected-gravity-plan
                'w5-invalid-toolchain-source-id [base])
        base-environment-b (get base :environment-b)
        base-toolchain-b (get base :toolchain-b)]
    (is (= (get base-environment-b :environment-id)
           (get (get random-seed-invalid :environment-b) :environment-id)))
    (is (= (get base-environment-b :toolchain-id)
           (get (get random-seed-invalid :environment-b) :toolchain-id)))
    (is (= (get base-environment-b :locale)
           (get (get random-seed-invalid :environment-b) :locale)))
    (is (not= (get base-environment-b :random-seed)
              (get (get random-seed-invalid :environment-b) :random-seed)))
    (is (= (get base-toolchain-b :toolchain-id)
           (get (get compiler-id-invalid :toolchain-b) :toolchain-id)
           (get (get source-id-invalid :toolchain-b) :toolchain-id)))
    (is (= (get base-toolchain-b :version)
           (get (get compiler-id-invalid :toolchain-b) :version)
           (get (get source-id-invalid :toolchain-b) :version)))
    (is (not= (get base-toolchain-b :compiler-id)
              (get (get compiler-id-invalid :toolchain-b) :compiler-id)))
    (is (not= (get base-toolchain-b :source-id)
              (get (get source-id-invalid :toolchain-b) :source-id)))))

(deftest w5-stage-equivalence-boot-diagnostic-families-are-reachable-and-exclusive
  (let [codes (set (map first (vals rejected-cases)))]
    (is (= #{"BOOT7001" "BOOT7002" "BOOT7003" "BOOT7004"
             "BOOT7005" "BOOT7006" "BOOT7007"}
           codes))
    (is (= 7 (count codes))))
  (let [catalog (invoke engine-plan
                        'w5-stage-equivalence-diagnostic-catalog [])]
    (is (contains? (set (:reasons catalog)) :diagnostic-evidence)))
  (doseq [[reason code]
          [[:artifact "BOOT7002"]
           [:missing-stage-output "BOOT7005"]
           [:performance-bound "BOOT7007"]
           [:conformance-drift "BOOT7006"]
           [:lineage "BOOT7001"]
           [:diagnostic-evidence "BOOT7003"]]]
    (is (= code
           (invoke engine-plan 'w5-se-diagnostic-rule [reason])))))

(deftest w5-stage-equivalence-result-verifier-recomputes-and-rejects-substitution
  (let [request-value (request accepted-gravity-plan
                               'w5-stage-equivalence-request)
        result (compare-request request-value)
        verification
        (invoke engine-plan 'w5-stage-equivalence-verify-result
                [request-value result])
        substituted
        (invoke rejected-gravity-plan
                'w5-stage-equivalence-substituted-result [result])
        substituted-verification
        (invoke engine-plan 'w5-stage-equivalence-verify-result
                [request-value substituted])]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :incomplete (:closure-status verification)))
    (is (= :blocked (:completion-status verification)))
    (is (= :rejected (:status substituted-verification)))
    (is (= "BOOT7001"
           (get (get (get substituted-verification :diagnostics) 0) :rule)))
    (is (= :non-authority (:authority substituted-verification)))
    (is (true? (:clojure-seed-boundary? substituted-verification)))))
