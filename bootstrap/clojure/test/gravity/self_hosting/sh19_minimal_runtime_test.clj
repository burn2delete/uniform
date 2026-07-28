(ns gravity.self-hosting.sh19-minimal-runtime-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh19_minimal_runtime_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-19 test source is not on the classpath"
                {:id "SH19-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH19-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-19")

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
  (delay (compile-plan (str fixture-root "/minimal_runtime_engine.gravity"))))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "runtime-requests" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "runtime-requests" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-runtime-requests" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-runtime-requests" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh19-minimal-runtime-leaf
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
  (invoke-engine 'sh19-handle-request [request]))

(def ^:private accepted-functions
  '[sh19-startup-request
    sh19-allocation-request
    sh19-no-allocation-request
    sh19-string-request
    sh19-file-read-request
    sh19-stdout-request
    sh19-stderr-request
    sh19-panic-request
    sh19-exit-request])

(def ^:private rejected-cases
  {'sh19-malformed-request
   ["R1-MANIFEST" :malformed-runtime-request]
   'sh19-missing-effect-request
   ["R11-GRANT" :effect-or-capability-not-granted]
   'sh19-missing-capability-request
   ["R11-GRANT" :effect-or-capability-not-granted]
   'sh19-wrong-provider-request
   ["R3-SERVICE" :missing-or-mismatched-provider]
   'sh19-oversized-allocation-request
   ["R3-ALLOCATOR" :invalid-allocation]
   'sh19-no-allocation-violation-request
   ["R3-ALLOCATOR" :invalid-allocation]
   'sh19-invalid-utf8-request
   ["R3-SERVICE" :invalid-utf8]
   'sh19-invalid-startup-request
   ["R1-STARTUP" :invalid-startup]
   'sh19-oversized-file-read-request
   ["R3-SERVICE" :invalid-file-read]
   'sh19-invalid-stream-request
   ["R3-SERVICE" :invalid-stream]
   'sh19-invalid-panic-request
   ["R3-PANIC" :invalid-panic]
   'sh19-invalid-exit-request
   ["R3-SERVICE" :invalid-exit]})

(deftest sh19-engine-and-request-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh19-runtime-policy
            sh19-handle-request
            sh19-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (let [policy (invoke-engine 'sh19-runtime-policy [])]
    (is (= :gravity/sh19-minimal-runtime-policy (:artifact policy)))
    (is (= :minimal-self-hosting (:family policy)))
    (is (= 1048576 (:maximum-allocation-bytes policy)))
    (is (= 16777216 (:maximum-file-read-bytes policy)))
    (is (= 256 (:maximum-inline-utf8-bytes policy)))
    (is (= 4096 (:maximum-request-nodes policy)))
    (is (= 16384 (:maximum-result-nodes policy)))
    (is (= 16 (:maximum-request-depth policy)))
    (is (= 24 (:maximum-result-depth policy)))
    (is (= 512 (:maximum-container-width policy)))
    (is (= 64 (:maximum-map-width policy)))
    (is (= #{:abort-with-report :trap :result-boundary}
           (:allocation-failure-behaviors policy)))
    (is (= :external
           (get-in policy [:service-classification :file-read])))
    (is (= :generated
           (get-in policy
                   [:service-classification :string-from-utf8])))
    (is (contains? (:diagnostics policy) "R11-GRANT"))
    (is (not (contains? (:diagnostics policy) "R12-SCHEMA")))
    (is (some #{:authenticated-sh17-runtime-interface}
              (:pending policy)))
    (is (some #{:native-provider-execution} (:pending policy))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "accepted" "runtime-requests" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "accepted" "runtime-requests" ".qst")))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-runtime-requests" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-runtime-requests" ".qst"))))))

(deftest sh19-accepts-bounded-runtime-services-and-emits-provider-actions
  (let [gravity-requests
        (mapv #(request accepted-gravity-plan %) accepted-functions)
        qst-requests
        (mapv #(request accepted-qst-plan %) accepted-functions)
        results (mapv handle gravity-requests)
        [startup allocation no-allocation string-result file-read
         stdout stderr panic exit]
        results]
    (is (= gravity-requests qst-requests))
    (is (= results (mapv handle qst-requests)))
    (is (= (vec (repeat 9 :accepted)) (mapv :status results)))
    (is (= [:startup :allocate :no-allocation :string-from-utf8
            :file-read :stream-write :stream-write :panic
            :process-exit]
           (mapv #(get-in % [:action :operation]) results)))
    (is (= :gravity.compiler/main
           (get-in startup [:action :payload :entrypoint])))
    (is (= :bounded
           (get-in allocation
                   [:action :payload :allocation-policy])))
    (is (= 4096
           (get-in allocation [:action :payload :size-bytes])))
    (is (= :none
           (get-in no-allocation
                   [:action :payload :allocation-policy])))
    (is (= 0
           (get-in no-allocation [:action :payload :size-bytes])))
    (is (= nil (get-in no-allocation [:action :declared-effect])))
    (is (= nil (get-in no-allocation [:action :capability])))
    (is (= nil (get-in no-allocation [:action :provider])))
    (is (= :utf-8
           (get-in string-result [:action :payload :encoding])))
    (is (= 16
           (get-in string-result [:action :payload :byte-count])))
    (is (= :compiler/source-unit
           (get-in file-read [:action :payload :path-token])))
    (is (= :fs/read (get-in file-read [:action :capability])))
    (is (= :stdout (get-in stdout [:action :payload :stream])))
    (is (= :io/stdout (get-in stdout [:action :capability])))
    (is (= :stderr (get-in stderr [:action :payload :stream])))
    (is (= :io/stderr (get-in stderr [:action :capability])))
    (is (= :abort-with-report
           (get-in panic [:action :payload :failure-mode])))
    (is (= 0 (get-in exit [:action :payload :exit-code])))
    (doseq [[request result] (map vector gravity-requests results)]
      (is (empty? (:diagnostics result)))
      (is (= :grant (get-in result [:decision-log :decision])))
      (is (re-matches #"sha256:[0-9a-f]{64}"
                      (get-in result [:action :action-id])))
      (is (= (:action-id request)
             (get-in result [:action :action-id])))
      (is (= (:request-id request)
             (get-in result
                     [:action :action-identity :request-id])))
      (is (= (:provider request)
             (get-in result
                     [:action :action-identity :provider])))
      (is (= request
             (get-in result
                     [:action :action-identity
                      :semantic-request])))
      (is (= (:caller-artifact-id request)
             (get-in result
                     [:decision-log :caller-artifact-id])))
      (is (= (:source-span request)
             (get-in result [:decision-log :source-span])))
      (is (= (:origin-chain request)
             (get-in result [:decision-log :origin-chain])))
      (is (= {:policy-artifact
              :gravity/sh19-minimal-runtime-policy
              :policy-version 2}
             (get-in result [:decision-log :policy-lineage])))
      (is (= request (get-in result [:identity-input :request])))
      (is (= (:caller-artifact-id request)
             (get-in result [:preserves :caller-artifact-id])))
      (is (= (:principal request)
             (get-in result [:preserves :principal])))
      (is (= (:source-span request)
             (get-in result [:preserves :source-span])))
      (is (= :passed
             (:status
              (invoke-engine
               'sh19-verify-result [request result])))))))

(deftest sh19-validates-complete-bounded-utf8-without-host-decoding
  (let [base
        (request accepted-gravity-plan 'sh19-string-request)
        valid-byte-vectors
        [[]
         [0 127]
         [194 128]
         [223 191]
         [224 160 128]
         [237 159 191]
         [238 128 128]
         [240 144 128 128]
         [244 143 191 191]]
        invalid-byte-vectors
        [[128]
         [192 128]
         [193 191]
         [194]
         [224 159 128]
         [224 160]
         [237 160 128]
         [240 143 128 128]
         [240 144 128]
         [244 144 128 128]
         [245 128 128 128]
         [256]
         [1.5]
         (vec (repeat 257 65))]]
    (doseq [bytes valid-byte-vectors]
      (let [result (handle (assoc base :bytes bytes))]
        (is (= :accepted (:status result)) (str bytes))
        (is (= bytes (get-in result [:action :payload :bytes])))
        (is (= (count bytes)
               (get-in result [:action :payload :byte-count])))))
    (doseq [bytes invalid-byte-vectors]
      (let [result (handle (assoc base :bytes bytes))]
        (is (= :rejected (:status result)) (str bytes))
        (is (= "R3-SERVICE"
               (get-in result [:diagnostics 0 :rule])))
        (is (= :invalid-utf8
               (get-in result [:diagnostics 0 :reason])))))))

(deftest sh19-rejects-ambient-unbounded-and-malformed-runtime-actions
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-result (handle gravity-request)
            qst-result (handle qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :minimal-self-hosting
               (:runtime-family diagnostic)))
        (is (= (:request-id gravity-request)
               (:request-id diagnostic)))
        (is (= (:caller-artifact-id gravity-request)
               (:caller-artifact-id diagnostic)))
        (is (= (:principal gravity-request)
               (:principal diagnostic)))
        (is (= (:profile gravity-request) (:profile diagnostic)))
        (is (= (:target gravity-request) (:target diagnostic)))
        (is (= (:source-span gravity-request)
               (:source-span diagnostic)))
        (is (= (:origin-chain gravity-request)
               (:generated-origin-chain diagnostic)))
        (is (= :deny
               (get-in gravity-result [:decision-log :decision])))
        (is (keyword? (:remediation diagnostic)))))))

(deftest sh19-fails-closed-on-scope-bounds-and-result-substitution
  (let [file-request
        (request accepted-gravity-plan 'sh19-file-read-request)
        allocation-request
        (request accepted-gravity-plan 'sh19-allocation-request)
        exit-request
        (request accepted-gravity-plan 'sh19-exit-request)
        missing-scope
        (handle (update file-request :provider dissoc :scope))
        fractional-size
        (handle (assoc allocation-request :size-bytes 1.5))
        undefined-failure
        (handle
         (assoc allocation-request :failure-behavior :undefined))
        fractional-exit
        (handle (assoc exit-request :exit-code 1.5))
        malformed-origin
        (handle (assoc file-request :origin-chain :not-a-vector))
        substituted-profile
        (handle (assoc file-request :profile :hosted))
        substituted-target
        (handle (assoc file-request :target :wasm))
        valid-result (handle file-request)
        substituted-action
        (assoc-in valid-result [:action :payload :maximum-bytes] 16777216)
        verification
        (invoke-engine
         'sh19-verify-result [file-request substituted-action])]
    (is (= "R3-SERVICE"
           (get-in missing-scope [:diagnostics 0 :rule])))
    (doseq [result [fractional-size undefined-failure]]
      (is (= :rejected (:status result)))
      (is (= "R3-ALLOCATOR"
             (get-in result [:diagnostics 0 :rule]))))
    (is (= :invalid-exit
           (get-in fractional-exit [:diagnostics 0 :reason])))
    (is (= "R1-MANIFEST"
           (get-in malformed-origin [:diagnostics 0 :rule])))
    (doseq [result [substituted-profile substituted-target]]
      (is (= :rejected (:status result)))
      (is (= "R1-MANIFEST"
             (get-in result [:diagnostics 0 :rule]))))
    (is (= :rejected (:status verification)))
    (is (= "R1-MANIFEST"
           (get-in verification [:diagnostics 0 :rule])))
    (is (= :runtime-result-substitution
           (get-in verification [:diagnostics 0 :reason])))))

(deftest sh19-enforces-startup-order-and-global-structure-bounds
  (let [startup
        (request accepted-gravity-plan 'sh19-startup-request)
        file-request
        (request accepted-gravity-plan 'sh19-file-read-request)
        duplicate-init
        (handle
         (assoc startup
                :initialization-order [:runtime :runtime]
                :cleanup-order [:runtime :runtime]))
        non-reverse-cleanup
        (handle
         (assoc startup
                :cleanup-order [:runtime :compiler]))
        entrypoint-in-initialization
        (handle
         (assoc startup
                :initialization-order
                [:runtime :gravity.compiler/main]
                :cleanup-order
                [:gravity.compiler/main :runtime]))
        wide-request
        (handle
         (assoc file-request
                :origin-chain
                (vec (repeat 513 {:kind :source}))))
        long-string
        (handle
         (assoc-in file-request
                   [:source-span :source]
                   (apply str (repeat 1025 "x"))))
        node-heavy
        (handle
         (assoc file-request
                :origin-chain
                (vec
                 (repeat
                  32
                  {:nodes
                   (vec
                    (repeat
                     200
                     {:leaf :bounded-scalar}))}))))
        deep-value
        (reduce
         (fn [value _] {:next value})
         :leaf
         (range 25))
        deep-request
        (handle (assoc file-request :extra deep-value))
        oversized-candidate
        (invoke-engine
         'sh19-verify-result
         [file-request (vec (repeat 513 :candidate-node))])]
    (doseq [result
            [duplicate-init non-reverse-cleanup
             entrypoint-in-initialization]]
      (is (= :rejected (:status result)))
      (is (= "R1-STARTUP"
             (get-in result [:diagnostics 0 :rule]))))
    (doseq [result [wide-request long-string node-heavy deep-request]]
      (is (= :rejected (:status result)))
      (is (= :runtime-request-structural-bound-exceeded
             (get-in result [:diagnostics 0 :reason])))
      (is (nil? (get-in result [:identity-input :request]))))
    (is (= :rejected (:status oversized-candidate)))
    (is (= :runtime-result-bound-exceeded
           (get-in oversized-candidate [:diagnostics 0 :reason])))))

(deftest sh19-closes-no-allocation-failure-and-identity-policy
  (let [no-allocation
        (request accepted-gravity-plan 'sh19-no-allocation-request)
        allocation
        (request accepted-gravity-plan 'sh19-allocation-request)
        file-request
        (request accepted-gravity-plan 'sh19-file-read-request)
        invalid-no-allocation
        [(assoc no-allocation
                :declared-effects #{:filesystem/read})
         (assoc no-allocation
                :capability-grants #{:fs/read})
         (assoc no-allocation :resource :compiler-source-root)
         (assoc no-allocation :provider-scope {:root :ambient})]
        invalid-identities
        [(assoc file-request :request-id "sha256:short")
         (assoc file-request
                :action-id (:request-id file-request))
         (assoc file-request :caller-artifact-id :compiler)]
        invalid-provider-identities
        [(assoc-in file-request
                   [:provider :provider-id]
                   "sha256:not-hex")
         (assoc-in file-request
                   [:provider :scope :scope-id]
                   "sha256:short")]]
    (doseq [candidate invalid-no-allocation]
      (let [result (handle candidate)]
        (is (= :rejected (:status result)))
        (is (= :missing-or-mismatched-provider
               (get-in result [:diagnostics 0 :reason])))))
    (doseq [behavior [:abort-with-report :trap :result-boundary]]
      (is (= :accepted
             (:status
              (handle
               (assoc allocation :failure-behavior behavior))))))
    (is (= :invalid-allocation
           (get-in
            (handle
             (assoc allocation :failure-behavior :host-surprise))
            [:diagnostics 0 :reason])))
    (doseq [candidate invalid-identities]
      (let [result (handle candidate)]
        (is (= :rejected (:status result)))
        (is (= "R1-MANIFEST"
               (get-in result [:diagnostics 0 :rule])))))
    (doseq [candidate invalid-provider-identities]
      (let [result (handle candidate)]
        (is (= :rejected (:status result)))
        (is (= "R3-SERVICE"
               (get-in result [:diagnostics 0 :rule])))))))

(deftest sh19-rejects-unbound-or-malformed-provider-scopes
  (let [allocation
        (request accepted-gravity-plan 'sh19-allocation-request)
        file-request
        (request accepted-gravity-plan 'sh19-file-read-request)
        stdout
        (request accepted-gravity-plan 'sh19-stdout-request)
        stderr
        (request accepted-gravity-plan 'sh19-stderr-request)
        exit
        (request accepted-gravity-plan 'sh19-exit-request)
        candidates
        [(assoc-in allocation
                   [:provider :scope :principal]
                   :gravity/other-principal)
         (assoc-in allocation
                   [:provider :scope :maximum-bytes]
                   1048576)
         (assoc-in file-request
                   [:provider :scope :path-token]
                   :compiler/other-source)
         (assoc-in file-request
                   [:provider :scope :provider-id]
                   "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
         (assoc-in file-request [:provider :scope] {})
         (assoc-in stdout
                   [:provider :scope :grant]
                   :io/stderr)
         (assoc-in stderr
                   [:provider :scope :resource]
                   :stdout)
         (assoc-in exit
                   [:provider :scope :resource]
                   :all-processes)
         (assoc-in exit
                   [:provider :scope :maximum-exit-code]
                   65535)
         (assoc-in exit
                   [:provider :scope :extra-authority]
                   :ambient)]]
    (doseq [candidate candidates]
      (let [result (handle candidate)]
        (is (= :rejected (:status result)))
        (is (= "R3-SERVICE"
               (get-in result [:diagnostics 0 :rule])))
        (is (= :missing-or-mismatched-provider
               (get-in result [:diagnostics 0 :reason])))))))
