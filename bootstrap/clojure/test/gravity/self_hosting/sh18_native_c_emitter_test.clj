(ns gravity.self-hosting.sh18-native-c-emitter-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh18-native-toolchain-harness :as harness])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh18_native_c_emitter_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-18 emitter test source is not on the classpath"
                {:id "SH18-EMITTER-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH18-EMITTER-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-18")

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
    (str fixture-root "/native_c_emitter.gravity"))))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "native-c-emission" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "native-c-emission" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-native-c-emission" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-native-c-emission" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh18-native-c-emitter-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine
  [function arguments]
  (invoke engine-plan function arguments))

(defn- fixture-request
  [plan function]
  (invoke plan function []))

(defn- emit
  [request]
  (invoke-engine 'sh18-emit-native-c [request]))

(defn- nested-value
  [depth leaf]
  (loop [remaining depth
         value leaf]
    (if (zero? remaining)
      value
      (recur (dec remaining) [value]))))

(defn require-native-toolchain!
  []
  (let [toolchain (harness/discover-toolchain)]
    (when-not (= :available (:status toolchain))
      (throw
       (ex-info
        "SH-18 authoritative execution requires an external C compiler"
        {:id "SH18-EMITTER-TOOLCHAIN-REQUIRED"
         :toolchain toolchain})))
    toolchain))

(def ^:private rejected-cases
  {'sh18-unverified-mir-request
   ["B1-INPUT" :unverified-or-malformed-mir]
   'sh18-wrong-profile-request
   ["B1-PROFILE" :profile-ineligible]
   'sh18-unsupported-dialect-request
   ["B2-DIALECT" :unsupported-c-target]
   'sh18-foreign-target-request
   ["B2-DIALECT" :unsupported-c-target]
   'sh18-hidden-runtime-request
   ["B2-RUNTIME" :runtime-service-mismatch]
   'sh18-missing-capability-request
   ["B1-CAPABILITY" :missing-output-authority]
   'sh18-unchecked-arithmetic-request
   ["B2-UB" :unchecked-integer-arithmetic]
   'sh18-unsupported-operation-request
   ["B1-UNSUPPORTED" :unsupported-program-shape]
   'sh18-incomplete-source-map-request
   ["B1-METADATA" :incomplete-source-map]})

(deftest sh18-native-c-engine-and-paired-fixtures-compile
  (doseq [plan
          [engine-plan
           accepted-gravity-plan
           accepted-qst-plan
           rejected-gravity-plan
           rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan
           (:kind @plan))))
  (doseq [function
          '[sh18-native-c-policy
            sh18-emit-native-c
            sh18-verify-native-c-emission]]
    (is (map? (get-in @engine-plan [:functions function]))
        function))
  (let [policy (invoke-engine 'sh18-native-c-policy [])]
    (is (= :gravity/sh18-native-c-emitter-policy
           (:artifact policy)))
    (is (= :gravity/mir-module (:input-kind policy)))
    (is (= :native (:profile policy)))
    (is (= :gravity.backend/c (:backend policy)))
    (is (= :hosted-c11 (:dialect policy)))
    (is (= :minimal-native (:runtime-family policy)))
    (is (= :argv-checked-sum (:program-kind policy)))
    (is (= 3 (:maximum-functions policy)))
    (is (= 5 (:maximum-operation-families policy)))
    (is (= 5 (count (:supported-operations policy))))
    (is (= 64 (:maximum-carrier-depth policy)))
    (is (contains? (:diagnostics policy) "B2-UB"))
    (is (some #{:authenticated-sh17-input}
              (:pending policy)))
    (is (some #{:coordinator-routing}
              (:pending policy))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "accepted" "native-c-emission" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "accepted" "native-c-emission" ".qst")))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-native-c-emission" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-native-c-emission" ".qst"))))))

(deftest sh18-native-c-emission-is-deterministic-and-synthetic-path-neutral
  (let [gravity-request
        (fixture-request
         accepted-gravity-plan 'sh18-accepted-native-request)
        qst-request
        (fixture-request
         accepted-qst-plan 'sh18-accepted-native-request)
        alternate-request
        (fixture-request
         accepted-gravity-plan
         'sh18-accepted-native-alternate-path-request)
        gravity-emission (emit gravity-request)
        qst-emission (emit qst-request)
        alternate-emission (emit alternate-request)
        source (:source gravity-emission)]
    (is (= gravity-request qst-request))
    (is (= gravity-emission qst-emission))
    (is (= :accepted (:status gravity-emission)))
    (is (empty? (:diagnostics gravity-emission)))
    (is (= :gravity/c-source-manifest
           (get-in gravity-emission [:manifest :artifact])))
    (is (= :hosted-c11
           (get-in gravity-emission [:manifest :dialect])))
    (is (= :runtime-checked
           (get-in gravity-emission
                   [:manifest :safety :outcome])))
    (is (= :mir/argv-checked-sum
           (get-in gravity-emission
                   [:manifest :input-artifact-id])))
    (is (= [:main :gravity-parse-i64 :gravity-checked-add]
           (get-in gravity-emission
                   [:manifest :function-graph :functions])))
    (is (= 2
           (count (get-in gravity-emission
                          [:manifest :helpers]))))
    (is (= #{:gravity-parse-i64 :gravity-checked-add}
           (set
            (map :target-symbol
                 (get-in gravity-emission
                         [:manifest :helpers])))))
    (is (= #{64 65 66 67 68}
           (set
            (map :exit-code
                 (get-in gravity-emission
                         [:manifest :failure-policies])))))
    (is (= :runtime-checks
           (get-in gravity-emission
                   [:manifest :helpers 0 :runtime-service])))
    (is (str/includes?
         source
         "static int gravity_checked_add"))
    (is (str/includes?
         source
         "parsed < INT64_MIN || parsed > INT64_MAX"))
    (is (str/includes?
         source
         "printf(\"sum=%\" PRId64"))
    (is (str/includes?
         source
         "integer addition overflow"))
    (is (not (str/includes? source "__attribute__")))
    (is (not (str/includes? source "#pragma")))
    (is (= (:identity-input gravity-emission)
           (:identity-input alternate-emission)))
    (is (= (:manifest gravity-emission)
           (:manifest alternate-emission)))
    (is (= (:source gravity-emission)
           (:source alternate-emission)))
    (is (not=
         (get-in gravity-emission
                 [:provenance :actual-source-path])
         (get-in alternate-emission
                 [:provenance :actual-source-path])))
    (is (not
         (str/includes?
          (pr-str (:identity-input gravity-emission))
          "/checkout-a/")))
    (is (= :passed
           (:status
            (invoke-engine
             'sh18-verify-native-c-emission
             [gravity-request gravity-emission]))))))

(deftest sh18-native-c-emitter-rejects-ineligible-inputs
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (let [gravity-request
            (fixture-request rejected-gravity-plan function)
            qst-request
            (fixture-request rejected-qst-plan function)
            gravity-result (emit gravity-request)
            qst-result (emit qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (nil? (:source gravity-result)))
        (is (nil? (:manifest gravity-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic)))
        (is (= rule (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :native-c-emission (:stage diagnostic)))
        (is (= :rejected (:fallback-status diagnostic)))
        (is (= reason (:missing-fact diagnostic)))
        (is (= :mir/main (:mir-operation diagnostic)))
        (is (= "/checkout-rejected/examples/argv-checked-sum.gravity"
               (:actual-source-path diagnostic)))
        (is (= :gravity.example/argv-sum
               (get-in diagnostic [:source-span :source-id])))
        (is (= :source
               (get-in diagnostic [:origin-chain 0 :kind])))
        (is (= :argv-checked-sum
               (:request-id diagnostic)))))))

(deftest sh18-native-c-emitter-rejects-semantic-request-alteration
  (let [request
        (fixture-request
         accepted-gravity-plan 'sh18-accepted-native-request)
        cases
        [[(assoc request :unexpected true)
          "B1-INPUT" :malformed-request]
         [(assoc-in request [:source-map 0 :mir-operation]
                    :mir/not-argv-parse)
          "B1-METADATA" :incomplete-source-map]
         [(assoc-in request [:source-map 1 :source-span :source-id]
                    :gravity.example/other)
          "B1-METADATA" :incomplete-source-map]
         [(assoc-in request [:source-map 1 :origin-chain 0 :source-id]
                    :gravity.example/other)
          "B1-METADATA" :incomplete-source-map]
         [(assoc-in request [:source-map 1 :generated-helper] false)
          "B1-METADATA" :incomplete-source-map]
         [(assoc-in request [:source-map 1 :source-span :end-byte] 129)
          "B1-METADATA" :incomplete-source-map]
         [(update-in
           request [:source-map 1 :origin-chain]
           conj
           {:kind :source
            :source-id :gravity.example/argv-sum})
          "B1-METADATA" :incomplete-source-map]
         [(assoc-in request [:program :functions]
                    [:main :gravity-checked-add])
          "B1-UNSUPPORTED" :unsupported-program-shape]
         [(assoc-in
           request [:program :functions]
           (apply list
                  (get-in request [:program :functions])))
          "B1-UNSUPPORTED" :unsupported-program-shape]
         [(assoc-in
           request [:program :operations]
           (apply list
                  (get-in request [:program :operations])))
          "B1-UNSUPPORTED" :unsupported-program-shape]
         [(assoc-in request [:runtime :services]
                    [:startup :stdout :stderr :process-exit])
          "B2-RUNTIME" :runtime-service-mismatch]
         [(assoc-in
           request [:runtime :services]
           (apply list
                  (get-in request [:runtime :services])))
          "B2-RUNTIME" :runtime-service-mismatch]
         [(assoc-in
           request [:runtime :forbidden]
           (apply list
                  (get-in request [:runtime :forbidden])))
          "B2-RUNTIME" :runtime-service-mismatch]
         [(assoc-in request [:target :triple] :foreign-native)
          "B2-DIALECT" :unsupported-c-target]]]
    (doseq [[altered rule reason] cases]
      (let [result (emit altered)
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)) altered)
        (is (= rule (:rule diagnostic)) altered)
        (is (= reason (:reason diagnostic)) altered)))))

(deftest sh18-native-c-boundaries-fail-closed-before-deep-comparison
  (let [request
        (fixture-request
         accepted-gravity-plan 'sh18-accepted-native-request)
        rejected-request
        (fixture-request
         rejected-gravity-plan 'sh18-unverified-mir-request)
        emission (emit request)
        rejected-emission (emit rejected-request)
        deep (nested-value 80 :leaf)
        deep-request (assoc request :unexpected deep)
        deep-request-result (emit deep-request)
        candidates
        [[request (assoc-in emission [:manifest :deep-probe] deep)]
         [rejected-request
          (assoc rejected-emission :deep-probe deep)]]]
    (is (= :rejected (:status deep-request-result)))
    (is (= :carrier-depth-bound
           (get-in deep-request-result
                   [:diagnostics 0 :reason])))
    (doseq [[verification-request candidate] candidates]
      (let [verification
            (invoke-engine
             'sh18-verify-native-c-emission
             [verification-request candidate])]
        (is (= :rejected (:status verification)))
        (is (= :candidate-result-carrier-bound
               (:reason verification)))
        (is (= :carrier-depth-bound
               (get-in verification
                       [:candidate-preflight :reason])))
        (is (= :omitted (:candidate-echo verification)))
        (is (not (contains? verification :candidate)))
        (is (not (contains? verification :expected)))
        (is (= "SH18-VERIFY"
               (get-in verification
                       [:diagnostics 0 :rule])))))))

(deftest sh18-native-c-verifier-rejects-result-substitution
  (let [request
        (fixture-request
         accepted-gravity-plan 'sh18-accepted-native-request)
        emission (emit request)
        altered
        (assoc emission :source (str (:source emission) "\n"))]
    (is (= :passed
           (:status
            (invoke-engine
             'sh18-verify-native-c-emission
             [request emission]))))
    (let [verification
          (invoke-engine
           'sh18-verify-native-c-emission
           [request altered])
          diagnostic (first (:diagnostics verification))]
      (is (= :rejected (:status verification)))
      (is (= "SH18-VERIFY" (:rule diagnostic)))
      (is (= :emission-substitution (:reason diagnostic))))))

(deftest sh18-gravity-emitted-c-compiles-and-executes-externally
  (let [require-toolchain?
        (= "1"
           (System/getenv
            "GRAVITY_SH18_REQUIRE_NATIVE_TOOLCHAIN"))
        toolchain
        (if require-toolchain?
          (require-native-toolchain!)
          (harness/discover-toolchain))]
    (is (contains? #{:available :unavailable}
                   (:status toolchain)))
    (when (= :unavailable (:status toolchain))
      (is (false? require-toolchain?))
      (is (seq (:candidates toolchain))))
    (when (= :available (:status toolchain))
      (let [request
            (fixture-request
             accepted-gravity-plan 'sh18-accepted-native-request)
            emission (emit request)]
        (harness/with-temporary-directory
          "gravity-sh18-emitted-c-"
          (fn [directory]
            (let [source-path (.resolve directory "gravity-emitted.c")
                  output-path (.resolve directory "gravity-emitted")
                  _ (Files/write
                     source-path
                     (.getBytes
                      ^String (:source emission)
                      StandardCharsets/UTF_8)
                     (make-array java.nio.file.OpenOption 0))
                  compilation
                  (harness/compile-c
                   {:toolchain toolchain
                    :source-path (str source-path)
                    :output-path (str output-path)
                    :working-directory (str directory)})
                  execution
                  (when (= :accepted (:status compilation))
                    (harness/execute-native
                     {:executable-path (str output-path)
                      :arguments ["20" "22"]
                      :working-directory (str directory)}))
                  overflow
                  (when (= :accepted (:status compilation))
                    (harness/execute-native
                     {:executable-path (str output-path)
                      :arguments [(str Long/MAX_VALUE) "1"]
                      :working-directory (str directory)}))
                  invalid-left
                  (when (= :accepted (:status compilation))
                    (harness/execute-native
                     {:executable-path (str output-path)
                      :arguments ["not-an-integer" "1"]
                      :working-directory (str directory)}))
                  missing-argument
                  (when (= :accepted (:status compilation))
                    (harness/execute-native
                     {:executable-path (str output-path)
                      :arguments ["1"]
                      :working-directory (str directory)}))]
              (is (= :accepted (:status compilation)) compilation)
              (is (= :host-native
                     (get-in request [:target :triple])))
              (is (= :host-native
                     (get-in emission
                             [:manifest :target :triple])))
              (is (string? (:target toolchain)))
              (is (not (str/blank? (:target toolchain))))
              (is (= (:target toolchain)
                     (:target compilation)))
              (is (= (:toolchain-id toolchain)
                     (:toolchain-id compilation)))
              (is (= :accepted (:status execution)) execution)
              (is (= 0 (get-in execution [:process :exit-code])))
              (is (= "sum=42\n"
                     (get-in execution [:process :stdout])))
              (is (= ""
                     (get-in execution [:process :stderr])))
              (is (= :rejected (:status overflow)) overflow)
              (is (= 67 (get-in overflow [:process :exit-code])))
              (is (= "integer addition overflow\n"
                     (get-in overflow [:process :stderr])))
              (is (= :rejected (:status invalid-left)) invalid-left)
              (is (= 65
                     (get-in invalid-left [:process :exit-code])))
              (is (= "invalid left integer\n"
                     (get-in invalid-left [:process :stderr])))
              (is (= :rejected
                     (:status missing-argument))
                  missing-argument)
              (is (= 64
                     (get-in missing-argument
                             [:process :exit-code])))
              (is (= "expected two integer arguments\n"
                     (get-in missing-argument
                             [:process :stderr])))
              (is (re-matches
                   #"sha256:[0-9a-f]{64}"
                   (:source-hash compilation)))
              (is (re-matches
                   #"sha256:[0-9a-f]{64}"
                   (:executable-hash compilation))))))))))
