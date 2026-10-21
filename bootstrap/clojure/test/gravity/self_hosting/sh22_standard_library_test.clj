(ns gravity.self-hosting.sh22-standard-library-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh22_standard_library_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-22 test source is not on the classpath"
                {:id "SH22-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH22-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-22")

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
    "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "library-requests" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "library-requests" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-library-requests" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-library-requests" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-standard-library-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine
  [function arguments]
  (invoke engine-plan function arguments))

(defn- request
  [plan function]
  (invoke plan function []))

(defn- handle
  [request]
  (invoke-engine 'sh22-handle-request [request]))

(def ^:private accepted-functions
  '[sh22-vector-assoc-request
    sh22-vector-append-request
    sh22-vector-nth-request
    sh22-map-assoc-request
    sh22-map-get-request
    sh22-set-conj-request
    sh22-set-contains-keyword-request
    sh22-set-contains-symbol-request
    sh22-set-contains-integer-request
    sh22-set-equal-keyword-request
    sh22-utf8-request
    sh22-checked-add-request
    sh22-checked-sub-request
    sh22-checked-mul-request
    sh22-checked-nonnegative-quot-request
    sh22-error-request
    sh22-file-read-request
    sh22-stdout-request
    sh22-artifact-request
    sh22-meta-request
    sh22-canonical-request])

(def ^:private rejected-cases
  {'sh22-malformed-request
   ['sh22-vector-assoc-request "STD1001" :malformed-library-request]
   'sh22-out-of-bounds-request
   ['sh22-vector-assoc-request "STD3002" :index-out-of-bounds]
   'sh22-oversized-collection-request
   ['sh22-vector-assoc-request "STD3001" :collection-bound-exceeded]
   'sh22-full-vector-append-request
   ['sh22-vector-append-request "STD3001" :collection-bound-exceeded]
   'sh22-invalid-utf8-request
   ['sh22-vector-assoc-request "STD4001" :invalid-utf8]
   'sh22-overflow-request
   ['sh22-vector-assoc-request "STD5001" :integer-overflow]
   'sh22-sub-overflow-request
   ['sh22-checked-sub-request "STD5001" :integer-overflow]
   'sh22-mul-overflow-request
   ['sh22-checked-mul-request "STD5001" :integer-overflow]
   'sh22-zero-divisor-request
   ['sh22-checked-nonnegative-quot-request "STD5002" :positive-divisor-required]
   'sh22-missing-capability-request
   ['sh22-file-read-request "STD8001" :missing-io-authority]
   'sh22-missing-encoding-request
   ['sh22-file-read-request "STD8003" :encoding-required]
   'sh22-invalid-artifact-request
   ['sh22-vector-assoc-request "STD10003" :target-required]
   'sh22-missing-meta-origin-request
   ['sh22-meta-request "STD15001" :origin-chain-required]
   'sh22-noncanonical-map-request
   ['sh22-vector-assoc-request
    "STD10003" :noncanonical-or-over-bound-value]
   'sh22-missing-allocation-effect-request
   ['sh22-vector-assoc-request "STD1002" :undeclared-library-effect]
   'sh22-unexpected-capability-request
   ['sh22-vector-assoc-request "STD1003" :unexpected-library-capability]
   'sh22-invalid-arity-request
   ['sh22-vector-assoc-request "STD1001" :invalid-arity]
   'sh22-missing-experimental-opt-in-request
   ['sh22-vector-assoc-request "STD1001" :malformed-library-request]
   'sh22-allocation-policy-mismatch-request
   ['sh22-vector-assoc-request "STD1001" :allocation-policy-mismatch]
   'sh22-duplicate-map-key-request
   ['sh22-map-assoc-request "STD10003" :expected-ordered-map]
   'sh22-duplicate-set-value-request
   ['sh22-set-conj-request "STD10003" :expected-ordered-set]
   'sh22-duplicate-keyword-after-hit-request
   ['sh22-set-contains-keyword-request "STD10003" :expected-ordered-set]
   'sh22-duplicate-symbol-after-hit-request
   ['sh22-set-contains-symbol-request "STD10003" :expected-ordered-set]
   'sh22-duplicate-integer-after-hit-request
   ['sh22-set-contains-integer-request "STD10003" :expected-ordered-set]
   'sh22-duplicate-keyword-equality-request
   ['sh22-set-equal-keyword-request "STD10003" :expected-ordered-set]
   'sh22-spoofed-utf8-request
   ['sh22-stdout-request "STD4001" :validated-text-facts-mismatch]
   'sh22-nested-physical-path-request
   ['sh22-canonical-request "STD1001" :malformed-library-request]})

(deftest sh22-engine-and-co-canonical-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh22-library-policy
            sh22-module-manifest
            sh22-handle-request
            sh22-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [[family basename]
          [["accepted" "library-requests"]
           ["rejected" "invalid-library-requests"]]]
    (is (= (slurp
            (path (fixture-relative-path family basename ".gravity")))
           (slurp
            (path (fixture-relative-path family basename ".qst")))))))

(deftest sh22-policy-and-manifest-state-bounded-experimental-surface
  (let [policy (invoke-engine 'sh22-library-policy [])
        manifest (invoke-engine 'sh22-module-manifest [])
        exports (:exports manifest)]
    (is (= :gravity/sh22-bootstrap-library-policy (:artifact policy)))
    (is (= 1 (:version policy)))
    (is (= 256 (:maximum-collection-elements policy)))
    (is (= 256 (:maximum-utf8-bytes policy)))
    (is (= 32 (:maximum-carrier-depth policy)))
    (is (= 16 (:maximum-canonical-depth policy)))
    (is (= 512 (:maximum-carrier-width policy)))
    (is (= 65536 (:maximum-carrier-nodes policy)))
    (is (true? (:experimental-opt-in-required policy)))
    (is (= 21 (count (:operation-set policy))))
    (is (contains? (:diagnostics policy) "STD3002"))
    (is (contains? (:diagnostics policy) "STD8001"))
    (is (some #{:authenticated-sh19-runtime-services}
              (:pending policy)))
    (is (some #{:complete-standard-library} (:pending policy)))
    (is (= :gravity/standard-library-module-manifest
           (:artifact manifest)))
    (is (= 'gravity.stdlib.self-hosting-core (:module manifest)))
    (is (= :experimental (:stability manifest)))
    (is (empty? (:unsafe-islands manifest)))
    (is (= 21 (count exports)))
    (is (every? #(= :experimental (:stability %)) exports))
    (is (every? #(= #{:meta} (:profiles %)) exports))
    (is (every? string? (map :type exports)))
    (is (every? set? (map :effects exports)))
    (is (every? set? (map :capabilities exports)))
    (is (every? keyword? (map :allocation exports)))))

(deftest sh22-executes-persistent-bounded-collection-helpers
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [assoc-request (request plan 'sh22-vector-assoc-request)
          original (first (:arguments assoc-request))
          assoc-result (handle assoc-request)
          nth-result (handle (request plan 'sh22-vector-nth-request))
          map-result (handle (request plan 'sh22-map-assoc-request))
          get-result (handle (request plan 'sh22-map-get-request))
          set-result (handle (request plan 'sh22-set-conj-request))]
      (is (= [10 99 30] (:value assoc-result)))
      (is (= [10 20 30] original))
      (is (true?
           (get-in assoc-result
                   [:facts :persistent-value-semantics])))
      (is (= 30 (:value nth-result)))
      (is (= [[:reader :ready] [:syntax :ready] [:mir :ready]]
             (get-in map-result [:value :entries])))
      (is (= :some (get-in get-result [:value :variant])))
      (is (= :ready (get-in get-result [:value :value])))
      (is (= [:reader :syntax :mir]
             (get-in set-result [:value :values])))
      (is (every? #(= :accepted (:status %))
                  [assoc-result nth-result map-result
                   get-result set-result])))))

(deftest sh22-validates-utf8-and-performs-checked-i64-arithmetic
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [utf8-result (handle (request plan 'sh22-utf8-request))
          add-result (handle (request plan 'sh22-checked-add-request))]
      (is (= :accepted (:status utf8-result)))
      (is (= [65 226 130 172]
             (get-in utf8-result [:value :bytes])))
      (is (= 4 (get-in utf8-result [:value :byte-count])))
      (is (= 2 (get-in utf8-result [:value :scalar-count])))
      (is (= :unicode-scalar
             (get-in utf8-result [:facts :boundary-policy])))
      (is (= :accepted (:status add-result)))
      (is (= :ok (get-in add-result [:value :variant])))
      (is (= 42 (get-in add-result [:value :value])))
      (is (= :checked-i64
             (get-in add-result [:facts :numeric-mode]))))))

(deftest sh22-builds-errors-artifact-data-and-stable-meta-views
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [error-result (handle (request plan 'sh22-error-request))
          artifact-result
          (handle (request plan 'sh22-artifact-request))
          meta-result (handle (request plan 'sh22-meta-request))]
      (is (= :gravity/error-value
             (get-in error-result [:value :artifact])))
      (is (= :compiler/invalid-form
             (get-in error-result [:value :code])))
      (is (false? (get-in error-result [:facts :host-exception])))
      (is (= [[:artifact-id "sha256:fixture"]
              [:kind :compiler-module]
              [:profile :meta]
              [:target :portable-mir]
              [:provenance [:reader :syntax :checked-core]]]
             (get-in artifact-result [:value :fields])))
      (is (= :fixed-schema-field-order
             (get-in artifact-result [:facts :canonical-order])))
      (is (= :gravity/stable-syntax-view
             (get-in meta-result [:value :artifact])))
      (is (= [:mark/root]
             (get-in meta-result [:value :hygiene-marks])))
      (is (= :compile (get-in meta-result [:value :phase])))
      (is (true?
           (get-in meta-result
                   [:facts :ordinary-recheck-required]))))))

(deftest sh22-io-wrappers-emit-authorized-provider-requests-only
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [file-result (handle (request plan 'sh22-file-read-request))
          stdout-result (handle (request plan 'sh22-stdout-request))]
      (is (= :accepted (:status file-result)))
      (is (= :gravity/runtime-service-request
             (get-in file-result [:value :artifact])))
      (is (= :file-read (get-in file-result [:value :operation])))
      (is (= :filesystem/read
             (get-in file-result [:value :effect])))
      (is (= :fs/read (get-in file-result [:value :capability])))
      (is (= :required
             (get-in file-result [:facts :provider-execution])))
      (is (false?
           (get-in file-result [:facts :ambient-authority])))
      (is (= :stream-write
             (get-in stdout-result [:value :operation])))
      (is (= :stdout (get-in stdout-result [:value :stream])))
      (is (= :io/stdout
             (get-in stdout-result [:value :capability]))))))

(deftest sh22-canonical-data-is-explicit-and-path-neutral
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [first-request (request plan 'sh22-canonical-request)
          second-request
          (request plan 'sh22-canonical-alternate-path-request)
          first-result (handle first-request)
          second-result (handle second-request)]
      (is (= :accepted (:status first-result)))
      (is (= :ordered-map (first (:value first-result))))
      (is (= :explicit-ordered-representation
             (get-in first-result [:facts :canonical-order])))
      (is (false? (get-in first-result [:facts :host-map-order])))
      (is (not= (:actual-source-path first-request)
                (:actual-source-path second-request)))
      (is (= (:identity-input first-result)
             (:identity-input second-result)))
      (is (= (:value first-result) (:value second-result)))
      (is (not=
           (get-in first-result [:provenance :actual-source-path])
           (get-in second-result [:provenance :actual-source-path]))))))

(deftest sh22-rejects-invalid-library-operations-before-provider-use
  (doseq [[accepted-plan rejected-plan]
          [[accepted-gravity-plan rejected-gravity-plan]
           [accepted-qst-plan rejected-qst-plan]]]
    (doseq [[mutation [base-function rule reason]] rejected-cases]
      (testing (str mutation)
        (let [base (request accepted-plan base-function)
              invalid (invoke rejected-plan mutation [base])
              result (handle invalid)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (nil? (:value result))))))))

(deftest sh22-canonical-carrier-depth-is-bounded-without-host-failure
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh22-canonical-request)
          nested (nth (iterate vector :leaf) 20)
          result (handle (assoc base :arguments [nested]))]
      (is (= :rejected (:status result)))
      (is (= "STD10003" (get-in result [:diagnostics 0 :rule])))
      (is (= :noncanonical-or-over-bound-value
             (get-in result [:diagnostics 0 :reason]))))))

(deftest sh22-verifier-recomputes-and-rejects-substitution
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [request (request plan 'sh22-checked-add-request)
          result (handle request)
          substituted (assoc-in result [:value :value] 43)]
      (is (= :passed
             (:status
              (invoke-engine
               'sh22-verify-result [request result]))))
      (let [verification
            (invoke-engine
             'sh22-verify-result [request substituted])]
        (is (= :rejected (:status verification)))
        (is (= "STD10003"
               (get-in verification [:diagnostics 0 :rule])))
        (is (= :library-result-substitution
               (get-in verification [:diagnostics 0 :reason])))))))

(deftest sh22-verifier-rejects-over-bound-candidates-before-equality
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [request (request plan 'sh22-checked-add-request)
          candidate (vec (repeat 513 :candidate))
          verification
          (invoke-engine 'sh22-verify-result [request candidate])]
      (is (= :rejected (:status verification)))
      (is (= "STD10003"
             (get-in verification [:diagnostics 0 :rule])))
      (is (= :verification-candidate-over-bound
             (get-in verification [:diagnostics 0 :reason]))))))

(deftest sh22-utf8-declared-byte-bound-is-executable-and-fail-closed
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh22-utf8-request)
          at-bound
          (handle (assoc base :arguments [(vec (repeat 256 65))]))
          over-bound
          (handle (assoc base :arguments [(vec (repeat 257 65))]))]
      (is (= :accepted (:status at-bound)))
      (is (= 256 (get-in at-bound [:value :byte-count])))
      (is (= 256 (get-in at-bound [:value :scalar-count])))
      (is (= :rejected (:status over-bound)))
      (is (= "STD4001" (get-in over-bound [:diagnostics 0 :rule])))
      (is (= :text-bound-exceeded
             (get-in over-bound [:diagnostics 0 :reason]))))))

(deftest sh22-utf8-validation-covers-boundaries-and-invalid-classes
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh22-utf8-request)]
      (doseq [bytes [[0] [127] [194 128] [224 160 128]
                     [237 159 191] [240 144 128 128]
                     [244 143 191 191]]]
        (is (= :accepted
               (:status (handle (assoc base :arguments [bytes]))))
            bytes))
      (doseq [bytes [[128] [192 128] [224 128 128]
                     [237 160 128] [226 130]
                     [240 128 128 128] [244 144 128 128]
                     [245 128 128 128]]]
        (let [result (handle (assoc base :arguments [bytes]))]
          (is (= :rejected (:status result)) bytes)
          (is (= "STD4001"
                 (get-in result [:diagnostics 0 :rule]))
              bytes)
          (is (= :invalid-utf8
                 (get-in result [:diagnostics 0 :reason]))
              bytes))))))

(deftest sh22-checked-i64-boundaries-do-not-use-overflowing-host-add
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh22-checked-add-request)
          accepted [[9223372036854775807 0 9223372036854775807]
                    [-9223372036854775808 0 -9223372036854775808]
                    [9223372036854775806 1 9223372036854775807]
                    [-9223372036854775807 -1 -9223372036854775808]]
          rejected [[9223372036854775807 1]
                    [-9223372036854775808 -1]
                    [9223372036854775808 0]
                    [-9223372036854775809 0]]]
      (doseq [[left right expected] accepted]
        (let [result
              (handle (assoc base :arguments [left right]))]
          (is (= :accepted (:status result)))
          (is (= expected (get-in result [:value :value])))))
      (doseq [[left right] rejected]
        (let [result
              (handle (assoc base :arguments [left right]))]
          (is (= :rejected (:status result)))
          (is (= "STD5001"
                 (get-in result [:diagnostics 0 :rule]))))))))

(deftest sh22-checked-i64-subtraction-executes-before-its-result-is-verified
  ;; STD5/D8: guard subtraction itself, not addition to a negated right operand.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh22-checked-sub-request)
          accepted [[40 2 38] [0 0 0] [7 -2 9] [-7 2 -9]
                    [9223372036854775807 0 9223372036854775807]
                    [-9223372036854775808 0 -9223372036854775808]
                    [-9223372036854775808 -9223372036854775808 0]
                    [-9223372036854775808 -1 -9223372036854775807]
                    [9223372036854775807 9223372036854775807 0]
                    [-1 -9223372036854775808 9223372036854775807]
                    [-9223372036854775807 1 -9223372036854775808]
                    [9223372036854775806 -1 9223372036854775807]]
          export (first (filter #(= :checked-sub-i64 (:name %))
                                (:exports (invoke-engine 'sh22-module-manifest []))))]
      (is (= #{:meta} (:profiles export)))
      (is (= :experimental (:stability export)))
      (is (= #{} (:effects export) (:capabilities export)))
      (is (= :none (:allocation export)))
      (is (= ["STD5001"] (:errors export)))
      (is (= "I64 I64 -> (Result I64 NumericError)" (:type export)))
      (doseq [[left right expected] (concat accepted
                                         (for [left [-3 -1 0 1 3] right [-3 -1 0 1 3]]
                                           [left right (- left right)]))]
        (let [input (assoc base :arguments [left right]) result (handle input)]
          (is (= :accepted (:status result)) (pr-str input))
          (is (= {:artifact :gravity/result :variant :ok :value expected} (:value result)))
          (is (= {:numeric-mode :checked-i64 :safety-outcome :proven-safe :allocation :none}
                 (:facts result)))
          (is (= [left right] (get-in result [:identity-input :arguments])))
          (is (= :checked-sub-i64 (get-in result [:identity-input :operation])))
          (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
      (doseq [[arguments reason]
              [[[-9223372036854775808 1] :integer-overflow]
               [[9223372036854775807 -1] :integer-overflow]
               [[0 -9223372036854775808] :integer-overflow]
               [[9223372036854775807 -9223372036854775808] :integer-overflow]
               [[-9223372036854775808 9223372036854775807] :integer-overflow]
               [[9223372036854775808 0] :integer-out-of-range]
               [[0 -9223372036854775809] :integer-out-of-range]
               [[nil 1] :expected-integer] [[1 false] :expected-integer]
               [["1" 1] :expected-integer]]]
        (let [input (assoc base :arguments arguments) result (handle input)]
          (is (= :rejected (:status result)))
          (is (= "STD5001" (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (= (:source-span input) (get-in result [:diagnostics 0 :source-span])))
          (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
      (doseq [[input rule reason]
              (concat (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                           [[] [1] [1 2 3]])
                      [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                       [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                       [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                       [(assoc base :operation :checked-sub-unknown) "STD1001" :malformed-library-request]])]
        (let [result (handle input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))))
      (let [result (handle base)]
        (doseq [changed [(assoc-in result [:value :value] 42)
                         (assoc-in result [:facts :numeric-mode] :wrapping-i64)
                         (assoc-in result [:identity-input :operation] :checked-add-i64)]]
          (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed])))))))))

(deftest sh22-checked-i64-multiplication-guards-actual-seed-quotient-thresholds
  ;; STD5/SAFE9: exercise every sign quadrant and both operand orders through
  ;; the actual Gravity leaf; no product is a probe for whether it overflows.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh22-checked-mul-request)
          accepted [[6 7 42] [0 -9223372036854775808 0]
                    [1 -9223372036854775808 -9223372036854775808]
                    [1 9223372036854775807 9223372036854775807]
                    [-1 9223372036854775807 -9223372036854775807]
                    [-1 -9223372036854775807 9223372036854775807]
                    [-2 4611686018427387904 -9223372036854775808]
                    [-2 -4611686018427387903 9223372036854775806]
                    [2 4611686018427387903 9223372036854775806]
                    [3037000499 3037000499 9223372030926249001]
                    [-3037000499 -3037000499 9223372030926249001]
                    [-3037000499 3037000499 -9223372030926249001]]
          export (first (filter #(= :checked-mul-i64 (:name %))
                                (:exports (invoke-engine 'sh22-module-manifest []))))]
      (is (= #{:meta} (:profiles export)))
      (is (= :experimental (:stability export)))
      (is (= #{} (:effects export) (:capabilities export)))
      (is (= :none (:allocation export)))
      (is (= ["STD5001"] (:errors export)))
      (is (= "I64 I64 -> (Result I64 NumericError)" (:type export)))
      (doseq [[left right expected] (concat accepted
                                         (for [left [-2 -1 0 1 2] right [-2 -1 0 1 2]]
                                           [left right (* left right)]))
              [a b] (distinct [[left right] [right left]])]
        (let [input (assoc base :arguments [a b]) result (handle input)]
          (is (= :accepted (:status result)) (pr-str input))
          (is (= {:artifact :gravity/result :variant :ok :value expected} (:value result)))
          (is (= {:numeric-mode :checked-i64 :safety-outcome :proven-safe :allocation :none}
                 (:facts result)))
          (is (= [a b] (get-in result [:identity-input :arguments])))
          (is (= :checked-mul-i64 (get-in result [:identity-input :operation])))
          (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
      (doseq [[left right] [[-9223372036854775808 -1] [9223372036854775807 2]
                           [-9223372036854775808 2] [-2 -4611686018427387904]
                           [3037000500 3037000500] [-3037000500 -3037000500]
                           [-3037000500 3037000500]]
              arguments (distinct [[left right] [right left]])]
        (let [input (assoc base :arguments arguments) result (handle input)]
          (is (= :rejected (:status result)) (pr-str input))
          (is (= "STD5001" (get-in result [:diagnostics 0 :rule])))
          (is (= :integer-overflow (get-in result [:diagnostics 0 :reason])))
          (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
      (doseq [[arguments reason] [[[9223372036854775808 0] :integer-out-of-range]
                                 [[0 -9223372036854775809] :integer-out-of-range]
                                 [[nil 0] :expected-integer] [[0 false] :expected-integer]
                                 [["1" 1] :expected-integer]]]
        (let [input (assoc base :arguments arguments) result (handle input)]
          (is (= :rejected (:status result)))
          (is (= "STD5001" (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (= (:source-span input) (get-in result [:diagnostics 0 :source-span])))))
      (doseq [[input rule reason]
              (concat (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                           [[] [1] [1 2 3]])
                      [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                       [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                       [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                       [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]])]
        (let [result (handle input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))))
      (let [result (handle base)]
        (doseq [changed [(assoc-in result [:value :value] 43)
                         (assoc-in result [:facts :numeric-mode] :wrapping-i64)
                         (assoc-in result [:identity-input :operation] :checked-add-i64)]]
          (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed])))))))))

(deftest sh22-checked-nonnegative-i64-quotient-checks-before-division
  ;; STD5/SAFE9 and reader's nonnegative decimal-digit client: no negative
  ;; rounding law, coercion or general-Integer division is selected here.
  (let [builtin #'bootstrap/p15-s23-stage2-runtime-invoke-builtin
        original @builtin
        observe (fn [input]
                  (let [calls (atom [])
                        result (with-redefs-fn
                                 {builtin (fn [& arguments]
                                            (when (= 'quot (nth arguments (- (count arguments) 2)))
                                              (swap! calls conj (last arguments)))
                                            (apply original arguments))}
                                 #(handle input))]
                    [result @calls]))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (request plan 'sh22-checked-nonnegative-quot-request)
            export (first (filter #(= :checked-nonnegative-quot-i64 (:name %))
                                  (:exports (invoke-engine 'sh22-module-manifest []))))]
        (is (= #{:meta} (:profiles export)))
        (is (= :experimental (:stability export)))
        (is (= #{} (:effects export) (:capabilities export)))
        (is (= :none (:allocation export)))
        (is (= ["STD5001" "STD5002"] (:errors export)))
        (is (= "I64 I64 -> (Result I64 NumericError)" (:type export)))
        (doseq [[left right expected]
                [[0 1 0] [1 1 1] [9 10 0] [10 10 1] [11 10 1]
                 [0 Long/MAX_VALUE 0] [1 Long/MAX_VALUE 0]
                 [Long/MAX_VALUE 1 Long/MAX_VALUE]
                 [Long/MAX_VALUE 10 922337203685477580]
                 [Long/MAX_VALUE Long/MAX_VALUE 1]
                 [(bigint 11) (biginteger 10) 1]]]
          (let [input (assoc base :arguments [left right]) [result calls] (observe input)
                value (get-in result [:value :value])]
            (is (= :accepted (:status result)))
            (is (= {:artifact :gravity/result :variant :ok :value expected} (:value result)))
            (is (= {:numeric-mode :checked-i64 :safety-outcome :proven-safe :allocation :none} (:facts result)))
            (is (= [[left right]] calls))
            (is (and (integer? value) (<= 0 value left)))
            (is (= [left right] (get-in result [:identity-input :arguments])))
            (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
        (doseq [[function arguments divisor expected]
                [['sh22-checked-sub-request [100 1] 10 9]
                 ['sh22-checked-mul-request [11 10] 10 11]]]
          (let [producer (assoc (request plan function) :arguments arguments)
                produced (handle producer)
                input (assoc base :arguments [(get-in produced [:value :value]) divisor])
                [result calls] (observe input)]
            (is (= :accepted (:status produced) (:status result)))
            (is (= expected (get-in result [:value :value])))
            (is (= [(:arguments input)] calls))
            (doseq [[request result] [[producer produced] [input result]]]
              (is (= :passed (:status (invoke-engine 'sh22-verify-result [request result])))))))
        (doseq [[arguments rule reason]
                [[[1 0] "STD5002" :positive-divisor-required]
                 [[0 0] "STD5002" :positive-divisor-required]
                 [[1 -1] "STD5002" :positive-divisor-required]
                 [[1 Long/MIN_VALUE] "STD5002" :positive-divisor-required]
                 [[-1 1] "STD5002" :nonnegative-dividend-required]
                 [[Long/MIN_VALUE -1] "STD5002" :nonnegative-dividend-required]
                 [[-1 0] "STD5002" :nonnegative-dividend-required]
                 [[9223372036854775808 1] "STD5001" :integer-out-of-range]
                 [[1 9223372036854775808] "STD5001" :integer-out-of-range]
                 [[-9223372036854775809 1] "STD5001" :integer-out-of-range]
                 [[-1 9223372036854775808] "STD5001" :integer-out-of-range]
                 [[nil 1] "STD5001" :expected-integer] [[1 false] "STD5001" :expected-integer]
                 [[1.0 1] "STD5001" :expected-integer] [[1 "10"] "STD5001" :expected-integer]
                 [[-1 :zero] "STD5001" :expected-integer]]]
          (let [input (assoc base :arguments arguments) [result calls] (observe input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (= [] calls))
            (is (= (:source-span input) (get-in result [:diagnostics 0 :source-span])))
            (is (= (:origin-chain input) (get-in result [:diagnostics 0 :origin-chain])))
            (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
        (doseq [[input rule reason]
                (concat (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                             [[] [1] [1 2 3]])
                        [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                         [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                         [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                         [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]
                         [(assoc base :operation nil) "STD8001" :missing-io-authority]])]
          (let [[result calls] (observe input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (= [] calls))))
        (let [result (handle base)]
          (doseq [changed [(assoc-in result [:value :value] 13)
                           (assoc-in result [:facts :numeric-mode] :wrapping-i64)
                           (assoc-in result [:identity-input :arguments 1] 0)]]
            (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed]))))))))))

(deftest sh22-keyword-set-membership-executes-with-full-family-and-uniqueness-checks
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        observe (fn [input]
                  (let [counts (atom {:census 0 :unique 0 :search 0})
                        result (with-redefs-fn
                                 {scoped (fn [& args]
                                           (when-let [kind (get {'sh22-keyword-vector? :census
                                                                 'sh22-vector-unique? :unique
                                                                 'sh22-vector-contains? :search} (nth args 2))]
                                             (swap! counts update kind inc))
                                           (apply original args))}
                                 #(handle input))]
                    [result @counts]))
        ordered (fn [values] {:artifact :gravity/ordered-set :values values})]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (request plan 'sh22-set-contains-keyword-request)
            values (mapv #(keyword "member" (str "k" %)) (range 256))
            export (first (filter #(= :ordered-set-contains-keyword (:name %))
                                  (:exports (invoke-engine 'sh22-module-manifest []))))]
        (is (= #{:meta} (:profiles export)))
        (is (= :experimental (:stability export)))
        (is (= #{} (:effects export) (:capabilities export)))
        (is (= :none (:allocation export)))
        (is (= "(OrderedSet Keyword) Keyword -> (Result Bool CollectionError)" (:type export)))
        (doseq [[members needle expected]
                [[[] :reader false] [[:reader] :reader true] [[:reader] :syntax false]
                 [[:reader :syntax] :reader true] [[:syntax :reader] :reader true]
                 [[:left/name :right/name] :left/name true]
                 [[:left/name :right/name] :name false]
                 [[:left/name] :right/name false]
                 [values :member/k0 true] [values :member/k255 true] [values :member/absent false]]]
          (let [input (assoc base :arguments [(ordered members) needle])
                before (:arguments input) result (handle input)]
            (is (= :accepted (:status result)))
            (is (= expected (:value result)))
            (is (= {:collection-kind :ordered-set :member-family :keyword
                    :equality :keyword-value :safety-outcome :proven-safe
                    :allocation :none :persistent-value-semantics true} (:facts result)))
            (is (= before (:arguments input) (get-in result [:identity-input :arguments])))
            (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
        ;; Bounds and the complete member/needle family check dominate equality.
        (doseq [[input rule reason census]
                [[(assoc base :arguments [(ordered (conj values :member/k256)) :member/k0])
                  "STD3001" :collection-bound-exceeded 0]
                 [(assoc base :arguments [(ordered [:reader "reader"]) :reader])
                  "STD10003" :expected-keyword-ordered-set 1]
                 [(assoc base :arguments [(ordered [:reader 'reader]) :reader])
                  "STD10003" :expected-keyword-ordered-set 1]
                 [(assoc base :arguments [(ordered [:reader]) "reader"])
                  "STD10003" :expected-keyword 1]
                 [(assoc base :arguments [(ordered [:reader]) 'reader])
                  "STD10003" :expected-keyword 1]
                 [(assoc base :arguments [#{:reader} :reader])
                  "STD10003" :expected-ordered-set 0]]]
          (let [[result counts] (observe input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (= {:census census :unique 0 :search 0} counts))))
        (let [input (assoc base :arguments [(ordered [:reader :syntax :reader]) :reader])
              [result counts] (observe input)]
          (is (= :rejected (:status result)))
          (is (= :expected-ordered-set (get-in result [:diagnostics 0 :reason])))
          (is (pos? (:unique counts)))
          (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result])))))
        (doseq [[input rule reason]
                (concat (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                             [[] [(ordered [])] [(ordered []) :reader :extra]])
                        [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                         [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                         [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                         [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]])]
          (let [result (handle input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))))
        (let [callbacks (atom 0) opaque (fn [] (swap! callbacks inc))]
          (doseq [[input reason census]
                  [[(assoc base :arguments [(ordered [:reader opaque]) :reader])
                    :expected-keyword-ordered-set 1]
                   [(assoc base :arguments [(ordered [:reader]) opaque]) :expected-keyword 1]
                   [(assoc base :origin-chain [{:kind :source :source-id opaque}])
                    :malformed-library-request 0]]]
            (let [[result counts] (observe input)]
              (is (= :rejected (:status result)))
              (is (= reason (get-in result [:diagnostics 0 :reason])))
              (is (= {:census census :unique 0 :search 0} counts))
              (is (zero? @callbacks)))))
        (let [result (handle base)]
          (doseq [changed [(assoc result :value false)
                           (assoc-in result [:facts :member-family] :string)
                           (assoc-in result [:identity-input :arguments 1] :absent)]]
            (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed]))))))))))

(deftest sh22-symbol-set-membership-consumes-real-insertion-results
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        ordered (fn [values] {:artifact :gravity/ordered-set :values values})
        observe (fn [input]
                  (let [counts (atom {:census 0 :unique 0 :search 0})
                        result (with-redefs-fn
                                 {scoped (fn [& args]
                                           (when-let [kind (get {'sh22-symbol-vector? :census
                                                                 'sh22-vector-unique? :unique
                                                                 'sh22-vector-contains? :search} (nth args 2))]
                                             (swap! counts update kind inc))
                                           (apply original args))}
                                 #(handle input))]
                    [result @counts]))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (request plan 'sh22-set-contains-symbol-request)
            values (mapv #(symbol "member" (str "s" %)) (range 256))
            export (first (filter #(= :ordered-set-contains-symbol (:name %))
                                  (:exports (invoke-engine 'sh22-module-manifest []))))]
        (is (= #{:meta} (:profiles export)))
        (is (= :experimental (:stability export)))
        (is (= #{} (:effects export) (:capabilities export)))
        (is (= :none (:allocation export)))
        (is (= "(OrderedSet Symbol) Symbol -> (Result Bool CollectionError)" (:type export)))
        (doseq [[members needle expected]
                [[[] 'reader false] [['reader] 'reader true] [['reader] 'syntax false]
                 [['reader 'syntax] 'reader true] [['syntax 'reader] 'reader true]
                 [['left/name 'right/name] (symbol "left" "name") true]
                 [['left/name 'right/name] 'name false] [['left/name] 'right/name false]
                 [['throw 'filesystem/read] 'throw true]
                 [values 'member/s0 true] [values 'member/s255 true] [values 'member/absent false]]]
          (let [input (assoc base :arguments [(ordered members) needle])
                before (:arguments input) result (handle input)]
            (is (= :accepted (:status result)))
            (is (= expected (:value result)))
            (is (= {:collection-kind :ordered-set :member-family :symbol
                    :equality :symbol-value :safety-outcome :proven-safe
                    :allocation :none :persistent-value-semantics true} (:facts result)))
            (is (= before (:arguments input) (get-in result [:identity-input :arguments])))
            (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
        (doseq [[members value] [[[] 'reader] [['reader] 'compiler/compile]
                                 [['reader 'compiler/compile] (symbol "compiler" "compile")]]]
          (let [insertion (assoc (request plan 'sh22-set-conj-request) :arguments [(ordered members) value])
                inserted (handle insertion)
                query (assoc base :arguments [(:value inserted) value]) result (handle query)]
            (is (= :accepted (:status inserted) (:status result)))
            (is (true? (:value result)))
            (is (= (ordered members) (first (:arguments insertion))))
            (is (= (ordered (if (some #{value} members) members (conj members value))) (:value inserted)))
            (doseq [[input output] [[insertion inserted] [query result]]]
              (is (= :passed (:status (invoke-engine 'sh22-verify-result [input output])))))))
        (doseq [[input rule reason census]
                [[(assoc base :arguments [(ordered (conj values 'member/s256)) 'member/s0])
                  "STD3001" :collection-bound-exceeded 0]
                 [(assoc base :arguments [(ordered ['reader "reader"]) 'reader])
                  "STD10003" :expected-symbol-ordered-set 1]
                 [(assoc base :arguments [(ordered ['reader :reader]) 'reader])
                  "STD10003" :expected-symbol-ordered-set 1]
                 [(assoc base :arguments [(ordered ['reader nil]) 'reader])
                  "STD10003" :expected-symbol-ordered-set 1]
                 [(assoc base :arguments [(ordered ['reader]) "reader"])
                  "STD10003" :expected-symbol 1]
                 [(assoc base :arguments [(ordered ['reader]) :reader])
                  "STD10003" :expected-symbol 1]
                 [(assoc base :arguments [(ordered ['reader]) nil])
                  "STD10003" :expected-symbol 1]
                 [(assoc base :arguments [#{'reader} 'reader])
                  "STD10003" :expected-ordered-set 0]]]
          (let [[result counts] (observe input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (= {:census census :unique 0 :search 0} counts))))
        (let [input (assoc base :arguments [(ordered ['reader 'compiler/compile (symbol "reader")]) 'reader])
              result (handle input)]
          (is (= :rejected (:status result)))
          (is (= :expected-ordered-set (get-in result [:diagnostics 0 :reason])))
          (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result])))))
        (doseq [[input rule reason]
                (concat (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                             [[] [(ordered [])] [(ordered []) 'reader :extra]])
                        [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                         [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                         [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                         [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]])]
          (let [result (handle input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))))
        (let [[result counts] (observe (assoc base :operation nil))]
          (is (= :rejected (:status result)))
          (is (= {:census 0 :unique 0 :search 0} counts)))
        (let [callbacks (atom 0) opaque (fn [] (swap! callbacks inc))]
          (doseq [[arguments reason] [[[(ordered ['reader opaque]) 'reader] :expected-symbol-ordered-set]
                                      [[(ordered ['reader]) opaque] :expected-symbol]]]
            (let [[result counts] (observe (assoc base :arguments arguments))]
              (is (= :rejected (:status result)))
              (is (= reason (get-in result [:diagnostics 0 :reason])))
              (is (= {:census 1 :unique 0 :search 0} counts))
              (is (zero? @callbacks)))))
        (let [result (handle base)]
          (doseq [changed [(assoc result :value false)
                           (assoc-in result [:facts :member-family] :keyword)
                           (assoc-in result [:identity-input :arguments 1] 'absent)]]
            (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed]))))))))))

(deftest sh22-integer-set-membership-consumes-real-insertion-results
  ;; Reader codepoints and compiler/runtime arity Sets are DATA witnesses,
  ;; not execution of those complete clients or an implicit i64 restriction.
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        ordered (fn [values] {:artifact :gravity/ordered-set :values values})
        above (inc (bigint Long/MAX_VALUE)) below (dec (bigint Long/MIN_VALUE))
        observe (fn [input]
                  (let [counts (atom {:census 0 :unique 0 :search 0})
                        result (with-redefs-fn
                                 {scoped (fn [& args]
                                           (when-let [kind (get {'sh22-integer-vector? :census
                                                                 'sh22-vector-unique? :unique
                                                                 'sh22-vector-contains? :search} (nth args 2))]
                                             (swap! counts update kind inc))
                                           (apply original args))}
                                 #(handle input))]
                    [result @counts]))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (request plan 'sh22-set-contains-integer-request)
            values (vec (range 256))
            export (first (filter #(= :ordered-set-contains-integer (:name %))
                                  (:exports (invoke-engine 'sh22-module-manifest []))))]
        (is (= #{:meta} (:profiles export)))
        (is (= :experimental (:stability export)))
        (is (= #{} (:effects export) (:capabilities export)))
        (is (= :none (:allocation export)))
        (is (= "(OrderedSet Integer) Integer -> (Result Bool CollectionError)" (:type export)))
        (doseq [[members needle expected]
                [[[] 32 false] [[32] 32 true] [[32] 44 false]
                 [[9 32 44] 32 true] [[44 32 9] 32 true]
                 [[-1 0 1] -1 true] [[-1 0 1] 0 true] [[-1 0 1] 2 false]
                 [[Long/MIN_VALUE Long/MAX_VALUE] Long/MIN_VALUE true]
                 [[Long/MIN_VALUE Long/MAX_VALUE] Long/MAX_VALUE true]
                 [[below above] above true] [[below above] below true]
                 [[32] (biginteger 32) true] [[above] (biginteger above) true]
                 ;; Actual existing reader/arity member values, without changing
                 ;; Unicode ingestion or claiming typed C7/runtime dispatch.
                 [[9 10 13 32 44] 10 true] [[9 10 13 32 44] 65 false]
                 [[2 3] (count [:left :right]) true] [[2 3] (count [:only]) false]
                 [[1 2] (count []) false]
                 [values 0 true] [values 255 true] [values 256 false]]]
          (let [input (assoc base :arguments [(ordered members) needle])
                before (:arguments input) result (handle input)]
            (is (= :accepted (:status result)))
            (is (= expected (:value result)))
            (is (= {:collection-kind :ordered-set :member-family :integer
                    :equality :integer-value :safety-outcome :proven-safe
                    :allocation :none :persistent-value-semantics true} (:facts result)))
            (is (= before (:arguments input) (get-in result [:identity-input :arguments])))
            (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
        (doseq [[members value] [[[] 32] [[9 10] 32] [[9 32] 32] [[below] above]]]
          (let [insertion (assoc (request plan 'sh22-set-conj-request) :arguments [(ordered members) value])
                inserted (handle insertion) query (assoc base :arguments [(:value inserted) value])
                result (handle query)]
            (is (= :accepted (:status inserted) (:status result)))
            (is (true? (:value result)))
            (is (= (ordered members) (first (:arguments insertion))))
            (is (= (ordered (if (some #{value} members) members (conj members value))) (:value inserted)))
            (doseq [[input output] [[insertion inserted] [query result]]]
              (is (= :passed (:status (invoke-engine 'sh22-verify-result [input output])))))))
        (doseq [[arguments rule reason census]
                (concat
                 [[[(ordered (conj values 256)) 0] "STD3001" :collection-bound-exceeded 0]
                  [[#{32} 32] "STD10003" :expected-ordered-set 0]
                  [[{:artifact :gravity/ordered-set} 32] "STD10003" :expected-ordered-set 0]
                  [[(assoc (ordered [32]) :extra true) 32] "STD10003" :expected-ordered-set 0]
                  [[{:artifact :gravity/ordered-set :values '(32)} 32] "STD10003" :expected-ordered-set 0]]
                 (mapcat (fn [other]
                           [[[(ordered [32 other]) 32] "STD10003" :expected-integer-ordered-set 1]
                            [[(ordered []) other] "STD10003" :expected-integer 1]])
                         [32.0 "32" 'codepoint :codepoint \space true false nil [] {}]))]
          (let [[result counts] (observe (assoc base :arguments arguments))]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (= {:census census :unique 0 :search 0} counts))))
        (let [input (assoc base :arguments [(ordered [32 44 (bigint 32)]) 32]) result (handle input)]
          (is (= :rejected (:status result)))
          (is (= :expected-ordered-set (get-in result [:diagnostics 0 :reason])))
          (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result])))))
        (doseq [[input rule reason]
                (concat (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                             [[] [(ordered [])] [(ordered []) 32 :extra]])
                        [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                         [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                         [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                         [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]])]
          (let [result (handle input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))))
        (let [[result counts] (observe (assoc base :operation nil))]
          (is (= :rejected (:status result)))
          (is (= {:census 0 :unique 0 :search 0} counts)))
        (let [callbacks (atom 0) opaque (fn [] (swap! callbacks inc))]
          (doseq [[arguments reason] [[[(ordered [32 opaque]) 32] :expected-integer-ordered-set]
                                      [[(ordered []) opaque] :expected-integer]]]
            (let [[result counts] (observe (assoc base :arguments arguments))]
              (is (= :rejected (:status result)))
              (is (= reason (get-in result [:diagnostics 0 :reason])))
              (is (= {:census 1 :unique 0 :search 0} counts))
              (is (zero? @callbacks)))))
        (let [result (handle base)]
          (doseq [changed [(assoc result :value false)
                           (assoc-in result [:facts :member-family] :keyword)
                           (assoc-in result [:identity-input :arguments 1] 65)]]
            (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed]))))))))))

(deftest sh22-keyword-set-equality-consumes-real-insertion-results
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        ordered (fn [values] {:artifact :gravity/ordered-set :values values})
        observe (fn [input]
                  (let [counts (atom {:census 0 :unique 0 :compare 0})
                        result (with-redefs-fn
                                 {scoped (fn [& args]
                                           (when-let [kind (get {'sh22-keyword-vector? :census
                                                                 'sh22-vector-unique? :unique
                                                                 'sh22-keyword-set-equal-values? :compare} (nth args 2))]
                                             (swap! counts update kind inc))
                                           (apply original args))}
                                 #(handle input))]
                    [result @counts]))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (request plan 'sh22-set-equal-keyword-request)
            values (mapv #(keyword "member" (str "k" %)) (range 256))
            export (first (filter #(= :ordered-set-equal-keyword (:name %))
                                  (:exports (invoke-engine 'sh22-module-manifest []))))]
        (is (= #{:meta} (:profiles export)))
        (is (= :experimental (:stability export)))
        (is (= #{} (:effects export) (:capabilities export)))
        (is (= :none (:allocation export)))
        (is (= "(OrderedSet Keyword) (OrderedSet Keyword) -> (Result Bool CollectionError)" (:type export)))
        (doseq [[left right expected]
                [[[] [] true] [[] [:reader] false] [[:reader] [] false]
                 [[:reader :syntax] [:syntax :reader] true]
                 [[:left/name] [(keyword "left" "name")] true]
                 [[:left/name] [:right/name] false] [[:reader :syntax] [:reader :mir] false]
                 [values (vec (reverse values)) true]
                 [values (assoc values 255 :member/other) false]]]
          (let [input (assoc base :arguments [(ordered left) (ordered right)])
                before (:arguments input) result (handle input)]
            (is (= :accepted (:status result)))
            (is (= expected (:value result)))
            (is (= {:collection-kind :ordered-set :member-family :keyword
                    :equality :keyword-set-value :safety-outcome :proven-safe
                    :allocation :none :persistent-value-semantics true} (:facts result)))
            (is (= before (:arguments input) (get-in result [:identity-input :arguments])))
            (is (= :passed (:status (invoke-engine 'sh22-verify-result [input result]))))))
        (doseq [[members value right] [[[] :reader [:reader]]
                                      [[:reader] :syntax [:syntax :reader]]
                                      [[:reader :syntax] :reader [:syntax :reader]]]]
          (let [insertion (assoc (request plan 'sh22-set-conj-request) :arguments [(ordered members) value])
                inserted (handle insertion)
                query (assoc base :arguments [(:value inserted) (ordered right)]) result (handle query)]
            (is (= :accepted (:status inserted) (:status result)))
            (is (true? (:value result)))
            (is (= (ordered members) (first (:arguments insertion))))
            (doseq [[input output] [[insertion inserted] [query result]]]
              (is (= :passed (:status (invoke-engine 'sh22-verify-result [input output])))))))
        ;; Even a count mismatch or apparent first hit cannot bypass validation.
        (doseq [[left right reason census unique?]
                [[(conj values :member/k256) [:reader] :collection-bound-exceeded 0 false]
                 [[:reader] (conj values :member/k256) :collection-bound-exceeded 0 false]
                 [[:reader "reader"] [:reader] :expected-keyword-ordered-set 1 false]
                 [[:reader] [:reader 'reader] :expected-keyword-ordered-set 2 false]
                 [[:reader] [:reader nil] :expected-keyword-ordered-set 2 false]
                 [[:reader :reader] [:reader] :expected-ordered-set 2 true]
                 [[:reader] [:reader :syntax :reader] :expected-ordered-set 2 true]
                 [[] [:reader :reader] :expected-ordered-set 2 true]]]
          (let [input (assoc base :arguments [(ordered left) (ordered right)])
                [result counts] (observe input)]
            (is (= :rejected (:status result)))
            (is (= (if (= reason :collection-bound-exceeded) "STD3001" "STD10003")
                   (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (= census (:census counts)))
            (is (= unique? (pos? (:unique counts))))
            (is (zero? (:compare counts)))))
        (doseq [arguments [[nil (ordered [])] [(ordered []) nil]
                           [#{:reader} (ordered [])] [(ordered []) [:reader]]]]
          (let [[result counts] (observe (assoc base :arguments arguments))]
            (is (= :rejected (:status result)))
            (is (= :expected-ordered-set (get-in result [:diagnostics 0 :reason])))
            (is (= {:census 0 :unique 0 :compare 0} counts))))
        (doseq [[input rule reason]
                (concat (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                             [[] [(ordered [])] [(ordered []) (ordered []) :extra]])
                        [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                         [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                         [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                         [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]])]
          (let [result (handle input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))))
        (let [[result counts] (observe (assoc base :operation nil))]
          (is (= :rejected (:status result)))
          (is (= {:census 0 :unique 0 :compare 0} counts)))
        (let [callbacks (atom 0) opaque (fn [] (swap! callbacks inc))]
          (doseq [[left right] [[[opaque] [:reader]] [[:reader] [:reader opaque]]]]
            (let [[result counts] (observe (assoc base :arguments [(ordered left) (ordered right)]))]
              (is (= :rejected (:status result)))
              (is (= :expected-keyword-ordered-set (get-in result [:diagnostics 0 :reason])))
              (is (zero? (:unique counts)))
              (is (zero? (:compare counts)))
              (is (zero? @callbacks)))))
        (let [result (handle base)]
          (doseq [changed [(assoc result :value false)
                           (assoc-in result [:facts :member-family] :symbol)
                           (assoc-in result [:identity-input :arguments 1 :values] [:other])]]
            (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed]))))))))))

(deftest sh22-vector-append-executes-with-real-nth-and-assoc-consumers
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        observe (fn [input]
                  (let [calls (atom 0)
                        result (with-redefs-fn
                                 {scoped (fn [& args]
                                           (when (= 'sh22-vector-append-result (nth args 2))
                                             (swap! calls inc))
                                           (apply original args))}
                                 #(handle input))]
                    [result @calls]))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (request plan 'sh22-vector-append-request)
            export (first (filter #(= :vector-append (:name %))
                                  (:exports (invoke-engine 'sh22-module-manifest []))))]
        (is (= #{:meta} (:profiles export)))
        (is (= :experimental (:stability export)))
        (is (= #{:memory/allocate} (:effects export)))
        (is (= #{} (:capabilities export)))
        (is (= :bounded-by-result (:allocation export)))
        (is (= "(Vector a) a -> (Result (Vector a) CollectionError)" (:type export)))
        (doseq [[prefix value] [[[] nil] [[] false] [[:reader :syntax] :mir]
                                [[false nil '(throw "inert")] {:nested [nil false]}]
                                [(vec (range 255)) :last]]]
          (let [input (assoc base :arguments [prefix value])
                [appended calls] (observe input) output (:value appended)
                coordinate (count prefix)
                nth-input (assoc (request plan 'sh22-vector-nth-request) :arguments [output coordinate])
                nth-result (handle nth-input)
                assoc-input (assoc (request plan 'sh22-vector-assoc-request)
                                   :arguments [output coordinate :replaced])
                assoc-result (handle assoc-input)]
            (is (= :accepted (:status appended)))
            (is (vector? output))
            (is (= 1 calls))
            (is (= (conj prefix value) output))
            (is (= prefix (vec (take coordinate output))))
            (is (= [prefix value] (:arguments input) (get-in appended [:identity-input :arguments])))
            (is (= {:collection-kind :vector :safety-outcome :proven-safe
                    :allocation :bounded-by-result :persistent-value-semantics true} (:facts appended)))
            (is (= :accepted (:status nth-result) (:status assoc-result)))
            (is (= value (:value nth-result)))
            (is (= (conj prefix :replaced) (:value assoc-result)))
            (is (= (conj prefix value) output (:value appended)))
            (doseq [[subject result] [[input appended] [nth-input nth-result] [assoc-input assoc-result]]]
              (is (= :passed (:status (invoke-engine 'sh22-verify-result [subject result])))))))
        (doseq [[input rule reason]
                (concat
                 (map #(vector (assoc base :arguments [(vec (repeat % nil)) :new])
                               "STD3001" :collection-bound-exceeded) [256 257])
                 (map #(vector (assoc base :arguments [% :new]) "STD3002" :expected-vector)
                      [nil '(:reader) {:a :b} #{:reader}])
                 (map #(vector (assoc base :arguments %) "STD1001" :invalid-arity)
                      [[] [[]] [[] :new :extra]])
                 [[(assoc base :effects #{} :arguments [[] nil]) "STD1002" :undeclared-library-effect]
                  [(assoc base :operation nil) "STD1001" :allocation-policy-mismatch]
                  [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                  [(assoc base :allocation-policy :none) "STD1001" :allocation-policy-mismatch]
                  [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]])]
          (let [[result calls] (observe input)]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason (get-in result [:diagnostics 0 :reason])))
            (is (zero? calls))))
        (let [callbacks (atom 0) inert (fn [] (swap! callbacks inc))
              input (assoc base :arguments [[inert] inert]) result (handle input)]
          (is (= :accepted (:status result)))
          (is (= [inert inert] (:value result)))
          (is (zero? @callbacks)))
        (let [result (handle base)]
          (doseq [changed [(assoc result :value [:syntax :reader :mir])
                           (assoc result :value [:reader :syntax :wrong])
                           (assoc-in result [:facts :allocation] :none)
                           (assoc-in result [:identity-input :arguments 1] :wrong)]]
            (is (= :rejected (:status (invoke-engine 'sh22-verify-result [base changed]))))))))))

(deftest sh22-collection-boundaries-run-before-recursive-search
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [entries (mapv (fn [index] [index index]) (range 256))
          values (vec (range 256))
          map-base (request plan 'sh22-map-assoc-request)
          set-base (request plan 'sh22-set-conj-request)
          map-update
          (handle
           (assoc map-base
                  :arguments
                  [{:artifact :gravity/ordered-map :entries entries}
                   255
                   :updated]))
          map-growth
          (handle
           (assoc map-base
                  :arguments
                  [{:artifact :gravity/ordered-map :entries entries}
                   256
                   :new]))
          set-existing
          (handle
           (assoc set-base
                  :arguments
                  [{:artifact :gravity/ordered-set :values values}
                   255]))
          set-growth
          (handle
           (assoc set-base
                  :arguments
                  [{:artifact :gravity/ordered-set :values values}
                   256]))]
      (is (= :accepted (:status map-update)))
      (is (= :updated
             (get-in map-update [:value :entries 255 1])))
      (is (= :rejected (:status map-growth)))
      (is (= "STD3001" (get-in map-growth [:diagnostics 0 :rule])))
      (is (= :accepted (:status set-existing)))
      (is (= 256 (count (get-in set-existing [:value :values]))))
      (is (= :rejected (:status set-growth)))
      (is (= "STD3001" (get-in set-growth [:diagnostics 0 :rule]))))))
