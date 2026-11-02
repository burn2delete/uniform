(ns gravity.self-hosting.sh09-effect-legality-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh09-body-effect-test :as body-effects]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh09_effect_legality_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-09 test source is not on the classpath"
                {:id "SH09-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH09-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c8-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-09")

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
    (if (= relative-path c8-source-relative-path)
      (body-effects/compile-c8-module-bundle source-path)
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path source-text))))

(def ^:private c8-plan
  (delay (compile-plan c8-source-relative-path)))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "normalized-effect-requests" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "normalized-effect-requests" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "illegal-effect-requests" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "illegal-effect-requests" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh09-effect-legality-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-c8
  [function arguments]
  (invoke c8-plan function arguments))

(defn- request
  [plan function]
  (invoke plan function []))

(defn- check
  [request]
  (invoke-c8 'sh09-check-effect-request [request]))

(def ^:private accepted-functions
  '[sh09-pure-request
    sh09-compiler-read-request
    sh09-build-read-request])

(def ^:private rejected-cases
  {'sh09-undeclared-request
   ["C8-UNDECLARED" :effect-not-declared :source-declaration]
   'sh09-profile-request
   ["C8-PROFILE" :slice-profile-mismatch :profile]
   'sh09-capability-request
   ["C8-CAPABILITY"
    :capability-not-allowed-by-all-authorities
    :capability-intersection]
   'sh09-ambient-request
   ["C8-CAPABILITY" :explicit-authority-required :grant]
   'sh09-build-request
   ["C8-BUILD" :build-effect-not-granted :build-policy]
   'sh09-replay-request
   ["C8-REPLAY" :replay-sensitive-effect-without-record :replay]
   'sh09-order-request
   ["C8-ORDER" :effect-ordering-mismatch :ordering]
   'sh09-runtime-request
   ["C8-RUNTIME" :provider-does-not-support-request :provider]
   'sh09-unknown-request
   ["C8-UNKNOWN" :unregistered-effect :effect-registry]
   'sh09-phase-request
   ["C8-PROFILE" :slice-phase-mismatch :phase]
   'sh09-malformed-request
   ["C8-VERIFY" :malformed-normalized-effect-request :request-shape]})

(deftest sh09-source-and-fixtures-compile-as-gravity
  (is (= :gravity/stage2-compiler-artifact-module-bundle (:kind @c8-plan)))
  (is (= :meta (get-in (body-effects/module-root-plan @c8-plan) [:module :profile])))
  (is (= :jvm (get-in (body-effects/module-root-plan @c8-plan) [:module :target])))
  (doseq [function
          '[sh09-effect-policy
            sh09-check-effect-request
            sh09-verify-effect-result]]
    (is (map? (get-in (body-effects/module-root-plan @c8-plan) [:functions function])) function))
  (let [policy (invoke-c8 'sh09-effect-policy [])]
    (is (= :gravity/sh09-effect-policy (:artifact policy)))
    (is (= 2 (:version policy)))
    (is (= #{:meta} (:profiles policy)))
    (is (= #{:jvm} (:targets policy)))
    (is (= #{:build} (:phases policy)))
    (is (contains? (:effects policy) :compiler/read-ir))
    (is (contains? (:effects policy) :build/read-file))
    (is (not-any? #{:authenticated-sh08-adapter} (:pending policy)))
    (is (some #{:authenticated-sh08-function-and-effectful-adapters}
              (:pending policy))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "accepted" "normalized-effect-requests" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "accepted" "normalized-effect-requests" ".qst")))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "rejected" "illegal-effect-requests" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "rejected" "illegal-effect-requests" ".qst")))))
  (doseq [plan [accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan)))))

(deftest sh09-accepts-pure-and-explicitly-authorized-meta-operations
  (let [gravity-requests
        (mapv #(request accepted-gravity-plan %) accepted-functions)
        qst-requests
        (mapv #(request accepted-qst-plan %) accepted-functions)
        results (mapv check gravity-requests)
        [pure compiler-read build-read] results]
    (is (= gravity-requests qst-requests))
    (is (= results (mapv check qst-requests)))
    (is (= [:accepted :accepted :accepted]
           (mapv :status results)))
    (is (= #{} (:direct-effects pure) (:residual-effects pure)))
    (is (nil? (:capability-proof pure)))
    (is (nil? (:replay-obligation pure)))
    (is (= #{:compiler/read-ir} (:direct-effects compiler-read)))
    (is (= :compiler/ir-read
           (get-in compiler-read
                   [:capability-proof :capability])))
    (is (= :gravity.compiler/read-only-ir
           (get-in compiler-read
                   [:capability-proof :provider])))
    (is (nil? (:replay-obligation compiler-read)))
    (is (= #{:build/read-file} (:direct-effects build-read)))
    (is (= :recorded
           (get-in build-read [:replay-obligation :status])))
    (is (= "sha256:1111111111111111111111111111111111111111111111111111111111111111"
           (get-in build-read
                   [:replay-obligation :record :content-id])))
    (is (= :generated
           (get-in build-read
                   [:preserves :origin-chain 0 :kind])))
    (doseq [[request result] (map vector gravity-requests results)]
      (is (= request (get-in result [:identity-input :request])))
      (is (= (:core-node-id request)
             (get-in result [:preserves :core-node-id])))
      (is (= (:syntax-id request)
             (get-in result [:preserves :syntax-id])))
      (is (= (:source-span request)
             (get-in result [:preserves :source-span])))
      (is (= :passed
             (:status
              (invoke-c8
               'sh09-verify-effect-result [request result])))))))

(deftest sh09-rejects-every-legality-gap-with-stable-diagnostics
  (doseq [[function [rule reason denied-layer]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-result (check gravity-request)
            qst-result (check qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= denied-layer (:denied-layer diagnostic)))
        (is (= :effect-checking (:stage diagnostic)))
        (is (= (:core-node-id gravity-request)
               (:core-node-id diagnostic)))
        (is (= (:syntax-id gravity-request)
               (:syntax-id diagnostic)))
        (is (= (:source-span gravity-request)
               (:source-span diagnostic)))
        (is (= (:origin-chain gravity-request)
               (:generated-origin-chain diagnostic)))
        (is (= (:profile gravity-request) (:profile diagnostic)))
        (is (= (:target gravity-request) (:target diagnostic)))
        (is (= (:phase gravity-request) (:phase diagnostic)))
        (is (keyword? (:remediation diagnostic)))))))

(deftest sh09-fails-closed-on-policy-and-result-substitution
  (let [request
        (request accepted-gravity-plan 'sh09-compiler-read-request)
        result (check request)
        mismatched-capability
        (check (assoc request
                      :required-capability
                      :compiler/ir-transform))
        missing-authority-mode
        (check (dissoc request :authority-mode))
        invalid-authority-collection
        (check (assoc request :declared-effects {}))
        substituted (assoc result :status :substituted)
        verification
        (invoke-c8
         'sh09-verify-effect-result [request substituted])]
    (is (= :rejected (:status mismatched-capability)))
    (is (= "C8-VERIFY"
           (get-in mismatched-capability [:diagnostics 0 :rule])))
    (is (= :capability-policy-mismatch
           (get-in mismatched-capability [:diagnostics 0 :reason])))
    (is (= :rejected (:status missing-authority-mode)))
    (is (= "C8-VERIFY"
           (get-in missing-authority-mode [:diagnostics 0 :rule])))
    (is (= :malformed-normalized-effect-request
           (get-in missing-authority-mode [:diagnostics 0 :reason])))
    (is (= :rejected (:status invalid-authority-collection)))
    (is (= "C8-VERIFY"
           (get-in invalid-authority-collection [:diagnostics 0 :rule])))
    (is (= :malformed-normalized-effect-request
           (get-in invalid-authority-collection
                   [:diagnostics 0 :reason])))
    (is (= :rejected (:status verification)))
    (is (= "C8-VERIFY"
           (get-in verification [:diagnostics 0 :rule])))
    (is (= :effect-result-substitution
           (get-in verification [:diagnostics 0 :reason])))
    (is (= result (check request)))))

(deftest sh09-request-shape-rejects-invalid-source-lineage-containers
  (let [base-request
        (request accepted-gravity-plan 'sh09-compiler-read-request)
        probes
        [(assoc base-request :source-span [:not-a-source-span-map])
         (assoc base-request :origin-chain {:not :an-origin-vector})]]
    (doseq [probe probes]
      (is (false?
           (invoke-c8 'sh09-valid-request-shape? [probe])))
      (let [result (check probe)]
        (is (= :rejected (:status result)))
        (is (= "C8-VERIFY"
               (get-in result [:diagnostics 0 :rule])))
        (is (= :malformed-normalized-effect-request
               (get-in result [:diagnostics 0 :reason])))))))

(deftest sh09-rejects-scope-authority-replay-and-slice-escapes
  (let [compiler-request
        (request accepted-gravity-plan 'sh09-compiler-read-request)
        build-request
        (request accepted-gravity-plan 'sh09-build-read-request)
        pure-request
        (request accepted-gravity-plan 'sh09-pure-request)
        probes
        [[(assoc compiler-request :authority-mode :none)
          "C8-CAPABILITY" :explicit-authority-required]
         [(assoc compiler-request :profile :hosted)
          "C8-PROFILE" :slice-profile-mismatch]
         [(assoc compiler-request :phase :runtime)
          "C8-PROFILE" :slice-phase-mismatch]
         [(assoc compiler-request :target :native)
          "C8-RUNTIME" :slice-target-mismatch]
         [(assoc-in compiler-request [:grant :scope]
                    {:ir-level :untyped-core})
          "C8-CAPABILITY" :grant-does-not-authorize-request]
         [(assoc-in build-request [:build-policy :hermetic] false)
          "C8-VERIFY" :malformed-normalized-effect-request]
         [(assoc build-request :replay-record true)
          "C8-REPLAY" :replay-sensitive-effect-without-record]
         [(assoc-in build-request [:replay-record :request-id]
                    :substituted)
          "C8-REPLAY" :replay-sensitive-effect-without-record]
         [(assoc compiler-request :replay-record true)
          "C8-VERIFY" :unexpected-replay-record]
         [(assoc pure-request :provider {:id :ambient}
                              :grant {:id :ambient})
          "C8-VERIFY" :pure-operation-carries-authority]
         [(assoc pure-request
                 :declared-capabilities #{:ambient/process})
          "C8-VERIFY" :pure-operation-carries-authority]]]
    (doseq [[probe rule reason] probes]
      (let [result (check probe)]
        (is (= :rejected (:status result)))
        (is (= rule (get-in result [:diagnostics 0 :rule])))
        (is (= reason (get-in result [:diagnostics 0 :reason])))))))

(deftest sh09-accepts-requests-beyond-retired-input-quotas
  (let [request
        (assoc
         (request accepted-gravity-plan 'sh09-compiler-read-request)
         :padding (vec (repeat 9000 0)))
        qst-request
        (assoc
         (request accepted-qst-plan 'sh09-compiler-read-request)
         :padding (vec (repeat 9000 0)))
        result (check request)
        qst-result (check qst-request)
        verification
        (invoke-c8 'sh09-verify-effect-result [request result])]
    (is (= request qst-request))
    (is (= result qst-result))
    (is (= :accepted (:status result)))
    (is (= request (get-in result [:identity-input :request])))
    (is (= :passed (:status verification)))))
