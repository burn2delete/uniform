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
    sh22-vector-nth-request
    sh22-map-assoc-request
    sh22-map-get-request
    sh22-set-conj-request
    sh22-utf8-request
    sh22-checked-add-request
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
   'sh22-invalid-utf8-request
   ['sh22-vector-assoc-request "STD4001" :invalid-utf8]
   'sh22-overflow-request
   ['sh22-vector-assoc-request "STD5001" :integer-overflow]
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
    (is (= 13 (count (:operation-set policy))))
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
    (is (= 13 (count exports)))
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
