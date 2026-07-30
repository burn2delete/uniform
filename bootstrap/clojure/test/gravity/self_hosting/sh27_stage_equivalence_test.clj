(ns gravity.self-hosting.sh27-stage-equivalence-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh27_stage_equivalence_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-27 stage-equivalence test is not on the classpath"
        {:id "SH27-TEST-SOURCE"})))
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH27-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-27")

(defn- compile-plan [relative]
  (let [source-path (path relative)
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
    (str fixture-root "/stage_equivalence_engine.gravity"))))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (str fixture-root "/accepted/stage-equivalence.gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (str fixture-root "/accepted/stage-equivalence.qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (str fixture-root
         "/rejected/invalid-stage-equivalence.gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (str fixture-root
         "/rejected/invalid-stage-equivalence.qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh27-stage-equivalence-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- compare-request [value]
  (invoke engine-plan 'sh27-compare-stages [value]))

(defn- replace-mode-product [request mode transform]
  (update
   request :products
   (fn [products]
     (mapv
      (fn [product]
        (if (= mode (:mode product))
          (transform product)
          product))
      products))))

(deftest sh27-engine-and-fixtures-compile
  (doseq [plan
          [engine-plan accepted-gravity-plan accepted-qst-plan
           rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan
           (:kind @plan))))
  (doseq [function
          '[sh27-stage-equivalence-policy
            sh27-compare-stages
            sh27-verify-comparison]]
    (is (map? (get-in @engine-plan [:functions function]))))
  (let [policy
        (invoke engine-plan 'sh27-stage-equivalence-policy [])]
    (is (= [:artifact :manifest :diagnostic :conformance
            :runtime-output :ir-modulo-id :reviewed-delta]
           (:required-modes policy)))
    (is (= ["BOOT7001" "BOOT7002" "BOOT7003" "BOOT7004"
            "BOOT7005" "BOOT7006" "BOOT7007"]
           (:diagnostics policy)))
    (is (= [:argument :literal :call :branch :jump :phi
            :runtime-check :return :error-return :panic]
           (:target-independent-opcodes policy)))
    (is (some #{:authenticated-sh26-stage-pair}
              (:pending policy)))
    (is (false? (:self-hosted? policy)))))

(deftest sh27-co-canonical-fixtures-compare-all-seven-modes
  (let [gravity-request
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        qst-request
        (request accepted-qst-plan
                 'sh27-equivalent-stage-request)
        gravity-result (compare-request gravity-request)
        qst-result (compare-request qst-request)]
    (is (= gravity-request qst-request))
    (is (= gravity-result qst-result))
    (is (= :accepted (:status gravity-result)))
    (is (= 7 (count (:outcomes gravity-result))))
    (is (every?
         #(= :equivalent (get-in % [:outcome :status]))
         (:outcomes gravity-result)))
    (is (= :reviewed-delta
           (get-in gravity-result
                   [:outcomes 6 :outcome :reason])))
    (is (empty? (:diagnostics gravity-result)))
    (is (= :passed
           (:status
            (invoke
             engine-plan 'sh27-verify-comparison
             [gravity-request gravity-result]))))))

(deftest sh27-identity-is-path-neutral-and-provenance-retains-paths
  (let [left-request
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        right-request
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-alternate-path-request)
        left (compare-request left-request)
        right (compare-request right-request)]
    (is (= (:identity-input left)
           (:identity-input right)))
    (is (not= (:provenance left)
              (:provenance right)))
    (is (= (str "sha256:" (apply str (repeat 64 "a")))
           (get-in left
                   [:identity-input :environment-a
                    :toolchain-id])))
    (is (= :accepted (:status left)))
    (is (= :accepted (:status right)))))

(deftest sh27-rejects-each-bounded-equivalence-family
  (doseq [[function rule reason]
          [['sh27-missing-lineage-request
            "BOOT7001" :invalid-request]
           ['sh27-artifact-drift-request
            "BOOT7002" :artifact-drift]
           ['sh27-diagnostic-drift-request
            "BOOT7003" :diagnostic-drift]
           ['sh27-unreviewed-delta-request
            "BOOT7004" :unreviewed-delta]
           ['sh27-conformance-failure-request
            "BOOT7006" :conformance-failure]
           ['sh27-missing-output-request
            "BOOT7005" :missing-stage-output]]]
    (testing (str function)
      (let [gravity
            (compare-request
             (request rejected-gravity-plan function))
            qst
            (compare-request
             (request rejected-qst-plan function))]
        (is (= gravity qst))
        (is (= :rejected (:status gravity)))
        (is (= rule
               (get-in gravity [:diagnostics 0 :rule])))
        (is (= reason
               (get-in gravity
                       [:diagnostics 0 :facts :reason])))
        (is (string?
             (get-in gravity
                     [:diagnostics 0 :source-span :source])))))))

(deftest sh27-result-and-request-alterations-fail-closed
  (let [request
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        result (compare-request request)
        altered-result
        (assoc-in result [:outcomes 0 :outcome :reason] :altered)
        verification
        (invoke
         engine-plan 'sh27-verify-comparison
         [request altered-result])
        uncontrolled
        (assoc-in request [:environment-b :network] :enabled)
        missing-mode
        (assoc request :modes
               [:artifact :manifest :diagnostic])]
    (is (= :rejected (:status verification)))
    (is (= :rejected (:status (compare-request uncontrolled))))
    (is (= :rejected (:status (compare-request missing-mode))))
    (is (= "BOOT7001"
           (get-in
            (compare-request uncontrolled)
            [:diagnostics 0 :rule])))))

(deftest sh27-mode-products-require-exact-canonical-shapes
  (let [base
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        alterations
        [[:canonical-form
          (replace-mode-product
           base :artifact
           #(assoc % :canonical-form :canonical-manifest))]
         [:artifact-value
          (replace-mode-product
           base :artifact #(assoc % :left :not-a-content-id))]
         [:artifact-paths
          (replace-mode-product
           base :artifact
           #(assoc % :left "/tmp/compiler"
                     :right "/tmp/compiler"))]
         [:artifact-short-hashes
          (replace-mode-product
           base :artifact
           #(assoc % :left "sha256:abc" :right "sha256:abc"))]
         [:artifact-uppercase-hash
          (replace-mode-product
           base :artifact
           #(assoc % :right
                   (str "sha256:"
                        (apply str (repeat 64 "A")))))]
         [:manifest-shape
          (replace-mode-product
           base :manifest #(assoc % :right [:reader :syntax]))]
         [:manifest-duplicates
          (replace-mode-product
           base :manifest
           #(assoc % :right {:modules [:reader :reader]}))]
         [:runtime-output-shape
          (replace-mode-product
           base :runtime-output #(assoc % :right {:exit 0}))]
         [:runtime-exit-bound
          (replace-mode-product
           base :runtime-output
           #(assoc-in % [:right :exit] 256))]
         [:reviewed-value-shape
          (replace-mode-product
           base :reviewed-delta #(assoc % :right :stage-label))]
         [:product-source-span
          (replace-mode-product
           base :artifact #(assoc % :source-spans [{}]))]]]
    (doseq [[label altered] alterations]
      (testing (str label)
        (let [result (compare-request altered)]
          (is (= :rejected (:status result)))
          (is (= "BOOT7001"
                 (get-in result [:diagnostics 0 :rule]))))))))

(deftest sh27-diagnostic-spans-are-exact-and-bounded
  (let [base
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        alterations
        [[:missing-span-fields
          (replace-mode-product
           base :diagnostic
           #(assoc-in % [:right 0 :source-span] {}))]
         [:reversed-span
          (replace-mode-product
           base :diagnostic
           #(assoc-in % [:right 0 :source-span]
                      {:source-id :compiler
                       :start-byte 12
                       :end-byte 10}))]
         [:unknown-severity
          (replace-mode-product
           base :diagnostic
           #(assoc-in % [:right 0 :severity] :fatal))]
         [:extra-span-field
          (replace-mode-product
           base :diagnostic
           #(assoc-in % [:right 0 :source-span :line] 1))]]]
    (doseq [[label altered] alterations]
      (testing (str label)
        (let [result (compare-request altered)]
          (is (= :rejected (:status result)))
          (is (= "BOOT7005"
                 (get-in result [:diagnostics 0 :rule])))
          (is (= :diagnostic-shape
                 (get-in result
                         [:diagnostics 0 :facts :reason]))))))))

(deftest sh27-ir-normalization-and-renaming-are-structurally-bound
  (let [base
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        alterations
        [[:target-dependent
          (replace-mode-product
           base :ir-modulo-id
           #(assoc-in % [:right :target-independent] false))]
         [:noncanonical-ordinal
          (replace-mode-product
           base :ir-modulo-id
           #(assoc-in %
                      [:right :normalized-operations 1 :ordinal] 3))]
         [:forward-operand
          (replace-mode-product
           base :ir-modulo-id
           #(assoc-in %
                      [:right :normalized-operations 0 :operands] [1]))]
         [:duplicate-original
          (replace-mode-product
           base :ir-modulo-id
           #(assoc-in %
                      [:right :id-renaming 1 :original]
                      "stage-n-plus-one:literal"))]
         [:duplicate-ordinal
          (replace-mode-product
           base :ir-modulo-id
           #(assoc-in %
                      [:right :id-renaming 1 :ordinal] 0))]
         [:target-specific-opcode
          (replace-mode-product
           base :ir-modulo-id
           #(assoc-in %
                      [:right :normalized-operations 0 :opcode]
                      :wasm/i32-add))]
         [:missing-renaming
          (replace-mode-product
           base :ir-modulo-id
           #(update-in % [:right :id-renaming] pop))]]]
    (doseq [[label altered] alterations]
      (testing (str label)
        (let [result (compare-request altered)]
          (is (= :rejected (:status result)))
          (is (= "BOOT7005"
                 (get-in result [:diagnostics 0 :rule])))
          (is (= :ir-shape
                 (get-in result
                         [:diagnostics 0 :facts :reason]))))))))

(deftest sh27-identities-deltas-and-toolchains-are-unique-and-bound
  (let [base
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        delta (first (:accepted-deltas base))
        duplicate-product
        (assoc-in base [:products 1 :product-id]
                  (get-in base [:products 0 :product-id]))
        duplicate-mode
        (assoc-in base [:products 1 :mode] :artifact)
        alterations
        [[:missing-toolchain
          (update base :environment-b dissoc :toolchain-id)]
         [:empty-toolchain
          (assoc-in base [:environment-b :toolchain-id] "")]
         [:physical-toolchain-path
          (assoc-in base [:environment-b :toolchain-id]
                    "/usr/bin/cc")]
         [:short-toolchain-digest
          (assoc-in base [:environment-b :toolchain-id]
                    "sha256:abc")]
         [:uppercase-toolchain-digest
          (assoc-in base [:environment-b :toolchain-id]
                    (str "sha256:"
                         (apply str (repeat 64 "A"))))]
         [:malformed-toolchain-prefix
          (assoc-in base [:environment-b :toolchain-id]
                    (str "blake3:"
                         (apply str (repeat 64 "a"))))]
         [:duplicate-product duplicate-product]
         [:duplicate-mode duplicate-mode]
         [:duplicate-delta-id
          (update base :accepted-deltas conj
                  (assoc delta
                         :product-id :product/other-reviewed))]
         [:duplicate-delta-product
          (update base :accepted-deltas conj
                  (assoc delta :delta-id :delta/second))]
         [:orphan-delta
          (assoc-in base [:accepted-deltas 0 :product-id]
                    :product/not-present)]
         [:wrong-delta-mode
          (assoc-in base [:accepted-deltas 0 :mode] :artifact)]
         [:missing-reviewer
          (assoc-in base [:accepted-deltas 0 :reviewer] "")]
         [:missing-policy
          (assoc-in base [:accepted-deltas 0 :policy-id] "")]
         [:altered-bound-value
          (assoc-in base
                    [:accepted-deltas 0 :right :builder-note]
                    "different-stage")]]]
    (doseq [[label altered] alterations]
      (testing (str label)
        (let [result (compare-request altered)]
          (is (= :rejected (:status result)))
          (is (= "BOOT7001"
                 (get-in result [:diagnostics 0 :rule]))))))))

(deftest sh27-product-order-is-bound-to-declared-mode-order
  (let [base
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        reordered
        (assoc base :products
               (assoc (:products base)
                      0 (get (:products base) 1)
                      1 (get (:products base) 0)))
        first-result (compare-request base)
        second-result (compare-request base)
        rejected (compare-request reordered)]
    (is (= :accepted (:status first-result)))
    (is (= first-result second-result))
    (is (= (:modes first-result)
           (mapv :mode (:outcomes first-result))))
    (is (= :rejected (:status rejected)))
    (is (= "BOOT7001"
           (get-in rejected [:diagnostics 0 :rule])))
    (is (= :invalid-request
           (get-in rejected [:diagnostics 0 :facts :reason])))))

(deftest sh27-overdeep-carriers-fail-closed
  (let [overdeep
        (reduce (fn [value _] [value])
                :leaf
                (range 70))
        result (compare-request overdeep)
        verification
        (invoke
         engine-plan 'sh27-verify-comparison
         [(request accepted-gravity-plan
                   'sh27-equivalent-stage-request)
          overdeep])]
    (is (= :rejected (:status result)))
    (is (= "BOOT7005"
           (get-in result [:diagnostics 0 :rule])))
    (is (= :carrier-depth-bound
           (get-in result [:diagnostics 0 :facts :reason])))
    (is (= :rejected (:status verification)))
    (is (= :candidate-carrier-bound
           (:candidate verification)))))

(deftest sh27-fixtures-are-byte-identical
  (is
   (=
    (slurp
     (path
      (str fixture-root "/accepted/stage-equivalence.gravity")))
    (slurp
     (path
      (str fixture-root "/accepted/stage-equivalence.qst")))))
  (is
   (=
    (slurp
     (path
      (str fixture-root
           "/rejected/invalid-stage-equivalence.gravity")))
    (slurp
     (path
      (str fixture-root
           "/rejected/invalid-stage-equivalence.qst"))))))
