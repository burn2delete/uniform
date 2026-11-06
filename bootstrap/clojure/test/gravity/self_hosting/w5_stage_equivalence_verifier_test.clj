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
          [[:request-shape "BOOT7001"]
           [:target "BOOT7001"]
           [:lineage "BOOT7001"]
           [:environment "BOOT7001"]
           [:lock "BOOT7001"]
           [:toolchain "BOOT7001"]
           [:producer-authority "BOOT7001"]
           [:artifact "BOOT7002"]
           [:artifact-drift "BOOT7002"]
           [:manifest-drift "BOOT7002"]
           [:ir-normalization-drift "BOOT7002"]
           [:artifact-shape "BOOT7002"]
           [:diagnostic-drift "BOOT7003"]
           [:diagnostic-span-drift "BOOT7003"]
           [:diagnostic-shape "BOOT7003"]
           [:diagnostic-evidence "BOOT7003"]
           [:unreviewed-delta "BOOT7004"]
           [:missing-stage-output "BOOT7005"]
           [:conformance-drift "BOOT7006"]
           [:runtime-output-drift "BOOT7006"]
           [:performance-bound "BOOT7007"]
           [:unknown "BOOT7001"]]]
    (is (= code
           (invoke engine-plan 'w5-se-diagnostic-rule [reason])))))

(deftest w5-stage-equivalence-optional-input-retains-digest-legality
  (let [base (request accepted-gravity-plan 'w5-stage-equivalence-request)
        stage (:stage-a base)]
    (doseq [input [nil (:source-id stage)]]
      (is (true? (invoke engine-plan 'w5-se-stage-valid?
                         [(assoc stage :compiler-input-artifact-id input)
                          :stage-a 0]))))
    (doseq [input ["" "sha256:bad" (apply str (repeat 4097 "a")) 0]]
      (is (false? (invoke engine-plan 'w5-se-stage-valid?
                          [(assoc stage :compiler-input-artifact-id input)
                           :stage-a 0]))))))

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

(deftest w5-stage-equivalence-traverses-spans-with-long-paths
  (let [base (request accepted-gravity-plan 'w5-stage-equivalence-request)
        span (assoc (get-in base [:products 0 :source-spans 0])
                    :actual-source-path (apply str (repeat 4097 "a")))
        expanded (assoc-in base [:products 0 :source-spans]
                           (vec (repeat 33 span)))
        relocated (-> expanded
                      (assoc-in
                       [:products 0 :source-spans 32 :actual-source-path]
                       (apply str (repeat 4097 "b")))
                      (assoc-in [:provenance :actual-request-path]
                                (apply str (repeat 4097 "b"))))
        malformed (assoc-in expanded
                           [:products 0 :source-spans 32 :start-byte] -1)
        result (compare-request expanded)
        relocated-result (compare-request relocated)]
    (is (= :accepted (:status result)))
    (is (= 7 (count (:outcomes result))))
    (is (= 33 (count (get-in result
                            [:identity-input :products 0 :source-spans]))))
    (is (= result (compare-request expanded)))
    (is (= :passed
           (:status (invoke engine-plan 'w5-stage-equivalence-verify-result
                            [expanded result]))))
    (is (= :accepted (:status relocated-result)))
    (is (= (:identity-input result) (:identity-input relocated-result)))
    (is (not= (:provenance result) (:provenance relocated-result)))
    (doseq [invalid [malformed
                     (assoc-in expanded [:products 0 :source-spans] {})
                     (update-in expanded [:products 0] dissoc :source-spans)]]
      (let [rejected (compare-request invalid)]
        (is (= :rejected (:status rejected)))
        (is (= "BOOT7002" (get-in rejected [:diagnostics 0 :rule])))
        (is (= :artifact (get-in rejected [:diagnostics 0 :facts :reason])))))))

(defn- assert-recomputed-comparison [request-value]
  (let [result (compare-request request-value)
        verification
        (invoke engine-plan 'w5-stage-equivalence-verify-result
                [request-value result])]
    (is (= :accepted (:status result)))
    (is (= [:artifact :manifest :diagnostic :conformance :runtime-output
            :ir-modulo-id :reviewed-delta]
           (:modes result) (mapv :mode (:outcomes result))))
    (is (all-equivalent-outcomes? (:outcomes result) 0))
    (is (= result (compare-request request-value)))
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :non-authority (:authority verification)))
    (is (= :blocked (:completion-status verification)))
    result))

(defn- assert-rejected-comparison [request-value rule reason]
  (let [result (compare-request request-value)
        diagnostic (first (:diagnostics result))]
    (is (= :rejected (:status result)))
    (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
    (is (= reason (get-in diagnostic [:facts :reason])))))

(deftest w5-stage-equivalence-stage-bindings-reject-substitution-on-either-side
  (let [base (request accepted-gravity-plan 'w5-stage-equivalence-request)]
    (is (= :accepted (:status (compare-request base))))
    (doseq [[product-index mutator drift-reason]
            [[0 'w5-invalid-artifact-drift :artifact-drift]
             [1 'w5-invalid-manifest-drift :manifest-drift]]]
      (let [invalid (invoke rejected-gravity-plan mutator [base])
            replacement (get-in invalid [:products product-index :right])]
        (doseq [sides [[:left] [:right] [:left :right]]]
          (testing (str "product " product-index " substituted on " sides)
            (assert-rejected-comparison
             (reduce (fn [request-value side]
                       (assoc-in request-value [:products product-index side]
                                 replacement))
                     base sides)
             "BOOT7002"
             (if (and (= product-index 0) (= 2 (count sides)))
               :artifact
               drift-reason))))))))

(deftest w5-stage-equivalence-traverses-large-diagnostic-records
  (let [long-message (apply str (repeat 4097 "m"))
        expand
        (fn [base path-character]
          (let [entry (get-in base [:products 2 :left 0])
                diagnostics
                (mapv (fn [index]
                        (-> entry
                            (assoc :message (str long-message index))
                            (assoc-in [:source-span :start-byte] index)
                            (assoc-in [:source-span :end-byte] (inc index))
                            (assoc-in [:source-span :actual-source-path]
                                      (apply str (repeat 4097 path-character)))))
                      (range 65))
                spans (mapv :source-span (take 33 diagnostics))]
            (-> base
                (assoc-in [:products 2 :left] diagnostics)
                (assoc-in [:products 2 :right]
                          (mapv #(update-in % [:source-span :actual-source-path]
                                            str "/right")
                                diagnostics))
                (assoc-in [:transcripts :accepted :diagnostics] diagnostics)
                (assoc-in [:transcripts :rejected :diagnostics] diagnostics)
                (assoc-in [:transcripts :accepted :source-spans] spans)
                (assoc-in [:transcripts :rejected :source-spans] spans))))
        expanded (expand (request accepted-gravity-plan
                                  'w5-stage-equivalence-request) "a")
        relocated (expand (request accepted-gravity-plan
                                   'w5-stage-equivalence-alternate-path-request)
                          "b")
        result (assert-recomputed-comparison expanded)
        relocated-result (compare-request relocated)
        identity-entries (get-in result [:identity-input :products 2 :left])]
    (is (= 65 (count identity-entries)))
    (is (= (str long-message 64) (:message (last identity-entries))))
    (is (= 64 (get-in (last identity-entries) [:source-span :start-byte])))
    (is (= identity-entries
           (get-in result [:identity-input :products 2 :right])))
    (is (= :accepted (:status relocated-result)))
    (is (= (:identity-input result) (:identity-input relocated-result)))
    (is (not= (:provenance result) (:provenance relocated-result)))
    (is (= 33 (count (get-in result [:transcripts :accepted :source-spans]))))
    (is (= (apply str (repeat 4097 "a"))
           (get-in result [:transcripts :accepted :diagnostics 64
                           :source-span :actual-source-path])))
    (doseq [[label invalid rule reason]
            [["malformed final diagnostic"
              (assoc-in expanded [:products 2 :right 64 :severity] :unknown)
              "BOOT7003" :diagnostic-evidence]
             ["malformed final diagnostic span"
              (assoc-in expanded
                        [:products 2 :right 64 :source-span :start-byte] -1)
              "BOOT7003" :diagnostic-evidence]
             ["empty message after former limit"
              (assoc-in expanded [:products 2 :left 64 :message] "")
              "BOOT7003" :diagnostic-evidence]
             ["drift after former limit"
              (assoc-in expanded [:products 2 :right 64 :message]
                        (str long-message "changed"))
              "BOOT7003" :diagnostic-drift]
             ["span drift after former limit"
              (assoc-in expanded
                        [:products 2 :right 64 :source-span :end-byte] 66)
              "BOOT7003" :diagnostic-span-drift]
             ["malformed final transcript diagnostic"
              (assoc-in expanded
                        [:transcripts :rejected :diagnostics 64 :code] "")
              "BOOT7003" :diagnostic-evidence]
             ["malformed final transcript span"
              (assoc-in expanded
                        [:transcripts :accepted :source-spans 32 :column] -1)
              "BOOT7003" :diagnostic-evidence]]]
      (testing label
        (assert-rejected-comparison invalid rule reason)))))

(deftest w5-stage-equivalence-traverses-large-ir-and-operand-vectors
  (let [base (request accepted-gravity-plan 'w5-stage-equivalence-request)
        operations
        (conj (mapv (fn [ordinal]
                      {:ordinal ordinal :opcode :argument :operands []})
                    (range 256))
              {:ordinal 256 :opcode :call :operands (vec (range 256))})
        ir (fn [prefix]
             {:normalized-operations operations
              :id-renaming (mapv (fn [ordinal]
                                   {:original (str prefix ordinal)
                                    :ordinal ordinal})
                                 (range 257))
              :target-independent true})
        expanded (-> base
                     (assoc-in [:products 5 :left] (ir "left-"))
                     (assoc-in [:products 5 :right] (ir "right-")))
        result (assert-recomputed-comparison expanded)
        substituted-request
        (-> expanded
            (assoc-in [:products 5 :left :normalized-operations 256 :opcode]
                      :phi)
            (assoc-in [:products 5 :right :normalized-operations 256 :opcode]
                      :phi))
        substituted-result (compare-request substituted-request)
        verification
        (invoke engine-plan 'w5-stage-equivalence-verify-result
                [expanded substituted-result])]
    (is (= operations
           (get-in result [:identity-input :products 5 :left
                           :normalized-operations])))
    (is (= 257 (count (get-in result [:identity-input :products 5 :right
                                     :id-renaming]))))
    (is (= :accepted (:status substituted-result)))
    (is (= :rejected (:status verification)))
    (is (= "BOOT7001" (get-in verification [:diagnostics 0 :rule])))
    (is (= :producer-authority
           (get-in verification [:diagnostics 0 :facts :reason])))
    (doseq [[label suffix value]
            [["invalid final operation ordinal"
              [:normalized-operations 256 :ordinal] 255]
             ["invalid final opcode"
              [:normalized-operations 256 :opcode] :unknown]
             ["self reference beyond former operand limit"
              [:normalized-operations 256 :operands 32] 256]
             ["forward reference at final operand"
              [:normalized-operations 256 :operands 255] 257]
             ["negative final operand"
              [:normalized-operations 256 :operands 255] -1]
             ["noninteger final operand"
              [:normalized-operations 256 :operands 255] "0"]
             ["duplicate final renaming ordinal" [:id-renaming 256 :ordinal] 0]
             ["duplicate final renaming name"
              [:id-renaming 256 :original] "right-0"]
             ["renaming outside actual population" [:id-renaming 256 :ordinal] 257]
             ["incomplete renaming" [:id-renaming]
              (pop (:id-renaming (ir "right-")))]]]
      (testing label
        (assert-rejected-comparison
         (assoc-in expanded (into [:products 5 :right] suffix) value)
         "BOOT7002" :artifact)))
    (assert-rejected-comparison
     (assoc-in expanded [:products 5 :right :normalized-operations 256 :opcode]
               :phi)
     "BOOT7002" :ir-normalization-drift)))

(deftest w5-stage-equivalence-traverses-large-reviewed-delta-vectors
  (let [base (request accepted-gravity-plan 'w5-stage-equivalence-request)
        delta (first (:accepted-deltas base))
        deltas (mapv (fn [index]
                       (assoc delta :delta-id (keyword "delta" (str index))))
                     (range 17))
        expanded (assoc base :accepted-deltas deltas)
        result (assert-recomputed-comparison expanded)]
    (is (= deltas (:accepted-deltas result)
           (get-in result [:identity-input :accepted-deltas])))
    (doseq [[field value]
            [[:status :pending]
             [:reviewer ""]
             [:policy-id ""]
             [:reason ""]
             [:mode :artifact]
             [:product-id :product/missing]
             [:left {:builder-note "unreviewed change"}]
             [:right {:builder-note "unreviewed change"}]]]
      (testing (str "invalid final delta " field)
        (assert-rejected-comparison
         (assoc-in expanded [:accepted-deltas 16 field] value)
         "BOOT7004" :unreviewed-delta)))))
