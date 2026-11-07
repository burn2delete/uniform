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

(defn- expanded-request [base]
  (let [text (apply str (repeat 4097 "C"))
        keywords (mapv #(keyword (str "module-" %)) (range 33))
        spans (mapv (fn [index]
                      {:source-id :compiler
                       :start-byte index :end-byte (inc index)})
                    (range 257))
        diagnostics (mapv #(hash-map :code text :source-span %
                                    :severity :error)
                          spans)
        operations (mapv (fn [ordinal]
                           {:ordinal ordinal
                            :opcode (if (zero? ordinal) :literal :call)
                            :operands (if (zero? ordinal)
                                        [] (vec (repeat (if (= ordinal 256)
                                                          1025 65)
                                                        0)))})
                         (range 257))
        ir (fn [prefix]
             {:normalized-operations operations
              :id-renaming (mapv (fn [ordinal]
                                   {:original (str prefix ordinal
                                                   (if (= ordinal 256) text ""))
                                    :ordinal ordinal})
                                 (range 257))
              :target-independent true})
        left {:builder-note (str "left-" text)}
        right {:builder-note (str "right-" text)}]
    (-> base
        (assoc :recipe-id text :lock-id text)
        (assoc-in [:environment-a :allowed-environment] keywords)
        (assoc-in [:environment-b :allowed-environment] keywords)
        (replace-mode-product :manifest
                              #(assoc % :left {:modules keywords}
                                        :right {:modules keywords}))
        (replace-mode-product :diagnostic
                              #(assoc % :left diagnostics :right diagnostics
                                        :source-spans spans))
        (replace-mode-product :runtime-output
                              #(assoc % :left {:stdout text :stderr text :exit 255}
                                        :right {:stdout text :stderr text :exit 255}))
        (replace-mode-product :ir-modulo-id
                              #(assoc % :left (ir "stage-n:")
                                        :right (ir "stage-n-plus-one:")))
        (replace-mode-product :reviewed-delta
                              #(assoc % :left left :right right))
        (update-in [:accepted-deltas 0]
                   #(assoc % :left left :right right
                             :reviewer text :policy-id text :reason text)))))

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

(deftest sh27-stage-transition-binds-parent-output-to-child-compiler
  (let [request
        (request accepted-gravity-plan
                 'sh27-equivalent-stage-request)
        transition (:stage-transition request)
        stage-a (:stage-a request)
        stage-b (:stage-b request)
        result (compare-request request)]
    (is (= (:stage-id stage-a)
           (:parent-stage-id transition)))
    (is (= (:compiler-id stage-a)
           (:parent-compiler-id transition)
           (:child-compiler-parent-id transition)))
    (is (= (:artifact-set-id stage-a)
           (:parent-output-artifact-set-id transition)
           (:child-compiler-input-artifact-set-id transition)))
    (is (= (:stage-id stage-b)
           (:child-stage-id transition)))
    (is (= (:compiler-id stage-b)
           (:child-compiler-id transition)))
    (is (= (:compiler-lineage-id stage-b)
           (:child-compiler-lineage-id transition)))
    (is (= transition
           (get-in result [:identity-input :stage-transition])))
    (is (not (contains? transition :actual-artifact-root)))
    (is (= :accepted (:status result)))))

(deftest sh27-rejects-each-bounded-equivalence-family
  (doseq [[function rule reason]
          [['sh27-missing-lineage-request
            "BOOT7001" :invalid-request]
           ['sh27-mismatched-lineage-request
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

(deftest sh27-populations-do-not-limit-seven-mode-comparison
  (let [expanded (expanded-request
                  (request accepted-gravity-plan 'sh27-equivalent-stage-request))
        companion (expanded-request
                   (request accepted-qst-plan 'sh27-equivalent-stage-request))
        result (compare-request expanded)
        diagnostics (get-in expanded [:products 2 :left])
        operations (get-in expanded [:products 5 :left :normalized-operations])]
    (is (= expanded companion))
    (is (= :accepted (:status result)))
    (is (= (:modes expanded) (mapv :mode (:outcomes result))))
    (is (= 7 (count (:products expanded)) (count (:outcomes result))))
    (is (= 1 (count (:accepted-deltas expanded))))
    ;; Repeated backward operands are legal; the two IRs alone cross the
    ;; former aggregate node threshold without adding products or deltas.
    (is (> (* 2 (reduce + (map #(count (:operands %)) operations))) 32768))
    (is (> (count (:operands (last operations))) 1024))
    (is (> (* 2 (count diagnostics) (count (:code (first diagnostics))))
           1048576))
    (is (= (:products expanded) (get-in result [:identity-input :products])))
    (is (= (:environment-a expanded)
           (get-in result [:identity-input :environment-a])))
    (is (= (:accepted-deltas expanded) (:accepted-deltas result)))
    (is (= result (compare-request expanded) (compare-request companion)))
    (is (= :passed
           (:status (invoke engine-plan 'sh27-verify-comparison
                            [expanded result]))))))

(deftest sh27-late-invalid-population-entries-fail-closed
  (let [expanded (expanded-request
                  (request accepted-gravity-plan 'sh27-equivalent-stage-request))]
    (doseq [[label altered rule reason]
            [[:late-module-type
              (assoc-in expanded [:products 1 :right :modules 32] "module")
              "BOOT7001" :invalid-request]
             [:late-module-duplicate
              (assoc-in expanded [:products 1 :right :modules 32] :module-0)
              "BOOT7001" :invalid-request]
             [:late-environment-type
              (assoc-in expanded [:environment-b :allowed-environment 32] "env")
              "BOOT7001" :invalid-request]
             [:late-environment-duplicate
              (assoc-in expanded [:environment-b :allowed-environment 32] :module-0)
              "BOOT7001" :invalid-request]
             [:late-product-span
              (assoc-in expanded [:products 2 :source-spans 256 :end-byte] 0)
              "BOOT7001" :invalid-request]
             [:late-diagnostic-span
              (assoc-in expanded [:products 2 :right 256 :source-span :end-byte] 0)
              "BOOT7005" :diagnostic-shape]
             [:late-diagnostic-code
              (assoc-in expanded [:products 2 :right 256 :code] "")
              "BOOT7005" :diagnostic-shape]
             [:late-opcode
              (assoc-in expanded [:products 5 :right :normalized-operations 256 :opcode]
                        :wasm/i32-add)
              "BOOT7005" :ir-shape]
             [:late-operation-ordinal
              (assoc-in expanded [:products 5 :right :normalized-operations 256 :ordinal]
                        257)
              "BOOT7005" :ir-shape]
             [:late-forward-operand
              (assoc-in expanded [:products 5 :right :normalized-operations 256
                                 :operands 1024] 256)
              "BOOT7005" :ir-shape]
             [:late-negative-operand
              (assoc-in expanded [:products 5 :right :normalized-operations 256
                                 :operands 1024] -1)
              "BOOT7005" :ir-shape]
             [:late-renaming-range
              (assoc-in expanded [:products 5 :right :id-renaming 256 :ordinal] 257)
              "BOOT7005" :ir-shape]
             [:late-renaming-duplicate-ordinal
              (assoc-in expanded [:products 5 :right :id-renaming 256 :ordinal] 0)
              "BOOT7005" :ir-shape]
             [:late-renaming-duplicate-original
              (assoc-in expanded [:products 5 :right :id-renaming 256 :original]
                        (get-in expanded [:products 5 :right :id-renaming 0 :original]))
              "BOOT7005" :ir-shape]
             [:late-renaming-missing
              (update-in expanded [:products 5 :right :id-renaming] pop)
              "BOOT7005" :ir-shape]
             [:unreviewed-long-delta
              (assoc expanded :accepted-deltas [])
              "BOOT7004" :unreviewed-delta]
             [:unbound-long-delta
              (assoc-in expanded [:accepted-deltas 0 :right :builder-note] "other")
              "BOOT7001" :invalid-request]
             [:long-output-exit-range
              (assoc-in expanded [:products 4 :right :exit] 256)
              "BOOT7001" :invalid-request]]]
      (testing (name label)
        (let [result (compare-request altered)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :facts :reason]))))))))

(deftest sh27-expanded-results-remain-bound-to-exact-request
  (let [expanded (expanded-request
                  (request accepted-gravity-plan 'sh27-equivalent-stage-request))
        substituted (replace-mode-product
                     expanded :manifest
                     #(-> %
                          (assoc-in [:left :modules 32] :substituted)
                          (assoc-in [:right :modules 32] :substituted)))
        candidate (compare-request substituted)]
    (is (= :accepted (:status candidate)))
    (is (= :rejected
           (:status (invoke engine-plan 'sh27-verify-comparison
                            [expanded candidate]))))))

(deftest sh27-aggregate-text-does-not-limit-seven-mode-comparison
  (let [base (request accepted-gravity-plan
                      'sh27-equivalent-stage-request)
        code (apply str (repeat 4096 "C"))
        entries (mapv (fn [index]
                        {:code code
                         :source-span {:source-id :compiler
                                       :start-byte index
                                       :end-byte (inc index)}
                         :severity :error})
                      (range 256))
        expanded (replace-mode-product
                  base :diagnostic
                  #(assoc % :left entries :right entries))
        result (compare-request expanded)
        invalid (replace-mode-product
                 expanded :diagnostic
                 #(assoc-in % [:right 255 :source-span :end-byte] 0))]
    ;; The two valid diagnostic vectors alone exceed the former scalar quota.
    (is (> (* 2 (count entries) (count code)) 1048576))
    (is (= :accepted (:status result)))
    (is (= 7 (count (:outcomes result))))
    (is (= entries (get-in result [:identity-input :products 2 :right])))
    (is (= result (compare-request expanded)))
    (is (= :passed
           (:status (invoke engine-plan 'sh27-verify-comparison
                            [expanded result]))))
    (let [rejected (compare-request invalid)]
      (is (= :rejected (:status rejected)))
      (is (= "BOOT7005" (get-in rejected [:diagnostics 0 :rule])))
      (is (= :diagnostic-shape
             (get-in rejected [:diagnostics 0 :facts :reason]))))))

(deftest sh27-malformed-carriers-fail-structurally-without-rendering
  (let [base (request accepted-gravity-plan
                      'sh27-equivalent-stage-request)
        overdeep
        (reduce (fn [value _] [value])
                :leaf
                (range 70))
        opaque (proxy [Object] []
                 (toString []
                   (throw (ex-info "Opaque payload was rendered" {}))))]
    (doseq [malformed [overdeep opaque
                      (assoc base :recipe-id overdeep)
                      (assoc base :recipe-id opaque)]]
      (let [result (compare-request malformed)]
        (is (= :rejected (:status result)))
        (is (= "BOOT7001" (get-in result [:diagnostics 0 :rule])))
        (is (= :invalid-request
               (get-in result [:diagnostics 0 :facts :reason])))))
    (let [malformed (replace-mode-product
                     base :diagnostic
                     #(assoc-in % [:right 0 :code] opaque))
          result (compare-request malformed)]
      (is (= :rejected (:status result)))
      (is (= :diagnostic-shape
             (get-in result [:diagnostics 0 :facts :reason]))))
    (let [expanded (expanded-request base)]
      (doseq [payload [overdeep opaque]
              [entry-path reason]
              [[[:products 2 :right 256 :code] :diagnostic-shape]
               [[:products 2 :right 256 :source-span] :diagnostic-shape]
               [[:products 5 :right :normalized-operations 256 :operands 1024]
                :ir-shape]
               [[:products 5 :right :id-renaming 256 :original] :ir-shape]]]
        (let [result (compare-request (assoc-in expanded entry-path payload))]
          (is (= :rejected (:status result)))
          (is (= "BOOT7005" (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :facts :reason]))))))
    (doseq [candidate [overdeep opaque]]
      (is (= :rejected
             (:status (invoke engine-plan 'sh27-verify-comparison
                              [base candidate])))))))

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
