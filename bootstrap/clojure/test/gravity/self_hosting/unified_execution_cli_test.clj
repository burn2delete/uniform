(ns gravity.self-hosting.unified-execution-cli-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.execution-lanes :as lanes]
            [gravity.self-hosting.unified-execution-cli :as cli]
            [gravity.self-hosting.unified-execution-plan :as plan]))

(defn- parse-error [args]
  (try (cli/parse-args args)
       nil
       (catch clojure.lang.ExceptionInfo error
         (:id (ex-data error)))))

(defn- invoke
  ([args] (invoke args {}))
  ([args bindings]
   (let [out (java.io.StringWriter.) err (java.io.StringWriter.)
         code (binding [*out* out *err* err
                        cli/*plan-fn* (or (:plan-fn bindings) cli/*plan-fn*)
                        cli/*executor-fn* (or (:executor-fn bindings)
                                              cli/*executor-fn*)
                        cli/*route-fn* (or (:route-fn bindings) cli/*route-fn*)
                        cli/*route-names-fn* (or (:route-names-fn bindings)
                                                 cli/*route-names-fn*)
                        cli/*legacy-mechanism-projection-fn*
                        (or (:legacy-mechanism-projection-fn* bindings)
                            cli/*legacy-mechanism-projection-fn*)]
                (cli/run! args))]
     {:code code :stdout (str out) :stderr (str err)})))

(defn- one-edn [text]
  (let [reader (java.io.PushbackReader.
                (java.io.StringReader. text))
        eof (Object.)]
    (with-open [reader reader]
      (let [value (edn/read {:eof eof} reader)
            trailing (edn/read {:eof eof} reader)]
        (is (not (identical? eof value)))
        (is (identical? eof trailing))
        value))))

(def ^:private fake-route
  {:id "any-route" :resource-class :normal
   :bounds {:timeout-ms 900000 :max-output-bytes 262144}})

(defn- valid-fast-plan []
  (plan/plan {:route-id "sh07-b11-contract" :lane :fast}))

(defn- valid-result
  ([status] (valid-result status []))
  ([status diagnostics]
   {:status status :test-count 1 :pass (if (= :passed status) 1 0)
    :fail 0 :error 0 :diagnostics diagnostics
    :evidence [:selected-tests :dependency-closure :fixture-pairing
               :diagnostics :receipt]
    :mutation-detected? false :fresh-replayable? true
    :provenance-checked? true :source-authenticated? true
    :closure-verified? true :cache-decision :miss}))

;; Independent compatibility oracle.  Keep this literal so the projection
;; cannot make its own expected output self-justifying.
(def ^:private expected-legacy-shards
  ["accepted"
   "b12-contract-accepted"
   "b12-identity-verification"
   "b12-rejected-regression"
   "identity-lineage"
   "regression-a"
   "regression-b"
   "rejected"
   "verification"])

(def ^:private expected-legacy-counts
  {"accepted" 8
   "b12-contract-accepted" 8
   "b12-identity-verification" 6
   "b12-rejected-regression" 3
   "identity-lineage" 2
   "regression-a" 10
   "regression-b" 11
   "rejected" 1
   "verification" 4})

(def ^:private expected-help-value
  ;; Frozen full help record from the pre-routing CLI. Compatibility routing is
  ;; deliberately omitted from this structured baseline so adding a migration
  ;; mechanism cannot silently change the established help contract.
  {:schema :gravity/unified-execution-cli-help-v1
   :status :help
   :required ["--route ID" "--lane fast|fresh"]
   :optional ["--coordination-root ABSOLUTE" "--timeout-ms N"
              "--max-output-bytes N" "--cache-mode eligible|disabled|forbidden"
              "--plan-only"]
   :standalone ["--help" "--list-routes"]})

(deftest parser-requires-the-two-primary-options
  (is (= {:mode :run :route-id "r" :lane :fast}
         (cli/parse-args ["--route" "r" "--lane" "fast"])))
  (is (= "UEP-CLI-REQUIRED" (parse-error [])))
  (is (= "UEP-CLI-REQUIRED" (parse-error ["--route" "r"])))
  (is (= "UEP-CLI-REQUIRED" (parse-error ["--lane" "fast"])))
  (is (= :fresh (:lane (cli/parse-args ["--route" "r" "--lane" "fresh"])))))

(deftest parser-rejects-closed-surface-violations
  (doseq [[args id]
          [[["--route" "r" "--route" "s" "--lane" "fast"] "UEP-CLI-DUPLICATE"]
           [["--route" "r" "--lane" "fast" "--lane" "fresh"] "UEP-CLI-DUPLICATE"]
           [["--route" "r" "--lane" "fast" "--plan-only" "--plan-only"] "UEP-CLI-DUPLICATE"]
           [["--route" "r" "--lane" "fast" "--unknown"] "UEP-CLI-UNKNOWN"]
           [["--route" "r" "--lane" "fast" "file"] "UEP-CLI-POSITIONAL"]
           [["--route" "r" "--lane"] "UEP-CLI-VALUE"]
           [["--route" "r" "--lane" "slow"] "UEP-CLI-LANE"]
           [["--route" "r" "--lane" "fast" "--cache-mode" "wat"] "UEP-CLI-CACHE-MODE"]
           [["--route" "r" "--lane" "fast" "--coordination-root" "relative"] "UEP-CLI-ROOT"]
           [["--route" "r" "--lane" "fast" "--timeout-ms" "0"] "UEP-CLI-INTEGER"]
           [["--route" "r" "--lane" "fast" "--max-output-bytes" "no"] "UEP-CLI-INTEGER"]]]
    (is (= id (parse-error args)) (pr-str args)))
  (is (= "UEP-CLI-INCOMPATIBLE" (parse-error ["--help" "--lane" "fast"])))
  (is (= "UEP-CLI-INCOMPATIBLE" (parse-error ["--list-routes" "--help"]))))

(deftest parser-bounds-argument-count-and-text
  (is (= "UEP-CLI-ARGUMENTS"
         (parse-error (vec (repeat 33 "--plan-only")))))
  (is (= "UEP-CLI-ARGUMENTS"
         (parse-error (repeat "--plan-only"))))
  (is (= "UEP-CLI-ARGUMENTS"
         (parse-error ["--route" (apply str (repeat 4097 "x"))
                       "--lane" "fast"])))
  (is (= "UEP-CLI-ARGUMENTS"
         (parse-error ["--route" "r\u0000x" "--lane" "fast"]))))

(deftest help-and-list-are-standalone-and-edn
  (let [{:keys [code stdout stderr]} (invoke ["--help"])]
    (is (zero? code))
    (is (empty? stderr))
    (is (= :help (:status (one-edn stdout)))))
  (let [{:keys [code stdout stderr]}
        (invoke ["--list-routes"] {:route-names-fn (constantly ["r1" "r2"])})]
    (is (zero? code))
    (is (empty? stderr))
    (is (= {:schema :gravity/unified-execution-cli-routes-v1
            :status :listed :routes ["r1" "r2"]}
           (one-edn stdout)))))

(deftest help-structure-preserves-the-base-contract
  (let [{:keys [code stdout stderr]} (invoke ["--help"])]
    (is (zero? code))
    (is (empty? stderr))
    (is (= expected-help-value (one-edn stdout)))))

(deftest legacy-compatibility-parser-is-closed-and-standalone
  (is (= {:mode :legacy :mechanism :parallel-shard-runner :operation :list}
         (cli/parse-args ["--legacy-mechanism" "parallel-shard-runner"
                         "--list"])))
  (is (= {:mode :legacy :mechanism :parallel-shard-runner :operation :check}
         (cli/parse-args ["--legacy-mechanism" "parallel-shard-runner"
                         "--check"])))
  (doseq [[args id]
          [[ ["--legacy-mechanism" "parallel-shard-runner"]
             "UEP-CLI-LEGACY-USAGE"]
           [ ["--legacy-mechanism" "parallel-shard-runner" "--list"
              "accepted"] "UEP-CLI-POSITIONAL"]
           [ ["--legacy-mechanism" "parallel-shard-runner" "--list" "--check"]
             "UEP-CLI-DUPLICATE"]]]
    (is (= id (parse-error args)) (pr-str args))))

(deftest legacy-compatibility-rejects-unknown-mechanism
  (let [{:keys [code stdout stderr]}
        (invoke ["--legacy-mechanism" "not-a-known-mechanism" "--list"])
        error (one-edn stderr)]
    (is (= 2 code))
    (is (empty? stdout))
    (is (= "UEP-LEGACY-MECHANISM" (:id error)))))

(deftest legacy-compatibility-list-and-check-preserve-sh07-bytes
  (let [{:keys [code stdout stderr]}
        (invoke ["--legacy-mechanism" "parallel-shard-runner" "--list"])]
    (is (zero? code))
    (is (empty? stderr))
    (is (= (str (clojure.string/join "\n" expected-legacy-shards) "\n")
           stdout)))
  (let [{:keys [code stdout stderr]}
        (invoke ["--legacy-mechanism" "parallel-shard-runner" "--check"])]
    (is (zero? code))
    (is (empty? stderr))
    (is (= (str (pr-str (into (sorted-map) expected-legacy-counts)) "\n")
           stdout))))

(deftest legacy-compatibility-shard-diagnostics-remain-stable
  (let [unknown
        (try
          (#'cli/legacy-shard-route :parallel-shard-runner :accepted)
          nil
          (catch clojure.lang.ExceptionInfo error
            [(ex-message error) (:id (ex-data error))]))]
    (is (= ["Unknown SH-07 parallel test shard" "SH07-PARALLEL-SHARD"]
           unknown)))
  (let [missing-test
        (try
          (with-redefs [cli/*legacy-mechanism-projection-fn*
                        (constantly {:route-ids ["fake-route"]
                                     :labels ["fake"]})
                        cli/*route-fn*
                        (constantly {:selection {:kind :exact-vars
                                                 :namespace "gravity.self-hosting.unified-execution-plan"
                                                 :selectors ["catalog-keys"]}})]
            (#'cli/resolve-legacy-shard :parallel-shard-runner "fake"))
          nil
          (catch clojure.lang.ExceptionInfo error
            (:id (ex-data error))))]
    (is (= "SH07-PARALLEL-SHARD-TEST" missing-test))))

(deftest plan-only-builds-one-declarative-request-without-execution
  (let [requests (atom []) executions (atom 0)
        expected (valid-fast-plan)
        result (invoke ["--route" "any-route" "--lane" "fast"
                        "--timeout-ms" "10" "--plan-only"]
                       {:route-fn (fn [_] fake-route)
                        :plan-fn (fn [request]
                                   (swap! requests conj request)
                                   expected)
                        :executor-fn (fn [_] (swap! executions inc))})]
    (is (zero? (:code result)))
    (is (empty? (:stderr result)))
    (is (= expected (one-edn (:stdout result))))
    (is (= [{:route-id "any-route" :lane :fast
             :resource-policy {:resource-class :normal
                               :timeout-ms 10 :max-output-bytes 262144}}]
           @requests))
    (is (zero? @executions))))

(deftest partial-resource-options-tighten-reviewed-route-bounds
  (doseq [[option value expected]
          [["--timeout-ms" "10" {:timeout-ms 10 :max-output-bytes 262144}]
           ["--max-output-bytes" "10" {:timeout-ms 900000 :max-output-bytes 10}]]]
    (let [requests (atom [])
          result (invoke ["--route" "any-route" "--lane" "fast"
                          option value "--plan-only"]
                         {:route-fn (fn [_] fake-route)
                          :plan-fn (fn [request]
                                     (swap! requests conj request)
                                     (valid-fast-plan))})]
      (is (zero? (:code result)))
      (is (= expected (select-keys (:resource-policy (first @requests))
                                   [:timeout-ms :max-output-bytes]))))))

(deftest cache-mode-forwards-and-fresh-cache-is-rejected
    (let [requests (atom [])
        result (invoke ["--route" "any-route" "--lane" "fast"
                        "--cache-mode" "disabled" "--plan-only"]
                       {:plan-fn (fn [request]
                                   (swap! requests conj request)
                                   (valid-fast-plan))})]
    (is (zero? (:code result)))
    (is (= {:mode :disabled} (:cache-policy (first @requests)))))
  (let [result (invoke ["--route" "sh07-authoritative" "--lane" "fresh"
                        "--cache-mode" "eligible" "--plan-only"])
        error (one-edn (:stderr result))]
    (is (= 2 (:code result)))
    (is (empty? (:stdout result)))
    (is (= "UEP-REQUEST-CACHE" (:id error)))))

(deftest over-route-bounds-are-left-to-the-plan-validator
  (let [result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"
                        "--timeout-ms" "900001" "--plan-only"])
        error (one-edn (:stderr result))]
    (is (= 2 (:code result)))
    (is (= "UEP-REQUEST-RESOURCE" (:id error))))
  (let [result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"
                        "--max-output-bytes" "262145" "--plan-only"])
        error (one-edn (:stderr result))]
    (is (= 2 (:code result)))
    (is (= "UEP-REQUEST-RESOURCE" (:id error)))))

(deftest real-manifest-plan-only-does-not-call-an-executor
  (let [called (atom false)
        result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"
                        "--plan-only"]
                       {:executor-fn (fn [_] (reset! called true)
                                      (throw (ex-info "must not execute"
                                                      {:id "CLI-EXECUTED"})))})
        plan-value (one-edn (:stdout result))]
    (is (zero? (:code result)))
    (is (empty? (:stderr result)))
    (is (= :gravity/unified-execution-plan-v1 (:schema plan-value)))
    (is (= "sh07-b11-contract" (:route-id plan-value)))
    (is (= :fast (:lane plan-value)))
    (is (false? @called))))

(deftest execution-binds-explicit-root-and-maps-status-to-exit
  (let [seen (atom [])
        plan-value (valid-fast-plan)
        execute (fn [value]
                  (swap! seen conj [value lanes/*coordination-root*])
                  (valid-result :passed))
        result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"
                        "--coordination-root" "/private/tmp/coord"]
                       {:plan-fn (constantly plan-value)
                        :executor-fn execute})]
    (is (zero? (:code result)))
    (is (= :passed (:status (one-edn (:stdout result)))))
    (is (= [[plan-value "/private/tmp/coord"]] @seen)))
  (doseq [status [:failed :blocked]]
    (let [result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"]
                         {:plan-fn (constantly (valid-fast-plan))
                          :executor-fn (constantly {:status status})})]
      (is (= 1 (:code result)))
      (is (= status (:status (one-edn (:stdout result))))))))

(deftest injected-executor-result-still-crosses-plan-validation
  (let [result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"]
                       {:executor-fn (constantly {:status :passed :bogus true})})
        error (one-edn (:stderr result))]
    (is (= 2 (:code result)))
    (is (empty? (:stdout result)))
    (is (= "UEP-RESULT-KEYS" (:id error)))))

(deftest default-fast-execution-without-root-is-blocked
  (let [result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"])
        receipt (one-edn (:stdout result))]
    (is (= 1 (:code result)))
    (is (empty? (:stderr result)))
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-LEASE-REQUIRED"
           (-> receipt :diagnostics first :id)))))

(deftest omitted-root-is-bound-as-nil-instead-of-inferred
  (let [seen (atom ::unset)
        result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"]
                       {:plan-fn (constantly (valid-fast-plan))
                        :executor-fn (fn [_]
                                      (reset! seen lanes/*coordination-root*)
                                      {:status :blocked})})]
    (is (= 1 (:code result)))
    (is (nil? @seen))))

(deftest planning-errors-are-one-edn-stderr-and-no-stdout
  (let [result (invoke ["--route" "missing" "--lane" "fast"]
                       {:plan-fn (fn [_]
                                   (throw (ex-info "unknown route" {:id "UEP-ROUTE-UNKNOWN"})))})
        error (one-edn (:stderr result))]
    (is (= 2 (:code result)))
    (is (empty? (:stdout result)))
    (is (= cli/cli-error-schema (:schema error)))
    (is (= :rejected (:status error)))
    (is (= "UEP-ROUTE-UNKNOWN" (:id error)))))

(deftest injected-fatal-exceptions-are-not-converted-to-cli-errors
  (doseq [fatal [(InterruptedException. "stop") (OutOfMemoryError. "fatal")
                 (ThreadDeath.)]]
    (let [out (java.io.StringWriter.) err (java.io.StringWriter.)]
      (is (thrown? Throwable
                   (binding [*out* out *err* err
                             cli/*plan-fn* (constantly (valid-fast-plan))
                             cli/*executor-fn* (fn [_] (throw fatal))]
                     (cli/run! ["--route" "sh07-b11-contract" "--lane" "fast"]))))
      (is (empty? (str out)))
      (is (empty? (str err))))))

(deftest output-bound-rejects-oversized-receipts-without-partial-stdout
  (let [large (apply str (repeat 262145 "x"))
        result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"]
                       {:executor-fn (constantly
                                      (valid-result :passed [{:payload large}]))})
        error (one-edn (:stderr result))]
    (is (= 2 (:code result)))
    (is (empty? (:stdout result)))
    (is (= "UEP-CLI-OUTPUT-BOUND" (:id error)))
    (is (<= (alength (.getBytes (:stderr result)
                                java.nio.charset.StandardCharsets/UTF_8))
            262144))))

(deftest oversized-and-non-edn-diagnostics-use-a-small-fallback
  (doseq [payload [(apply str (repeat 300000 "\\n"))
                   (Object.)
                   (map identity (range 32))]]
    (let [result (invoke ["--route" "r" "--lane" "fast" "--plan-only"]
                         {:plan-fn (fn [_]
                                     (throw (ex-info "diagnostic" {
                                                     :id "ORIGINAL-ID"
                                                     :payload payload})))})
          error (one-edn (:stderr result))]
      (is (= 2 (:code result)))
      (is (empty? (:stdout result)))
      (is (= "UEP-CLI-OUTPUT-BOUND" (:id error)))
      (is (= "ORIGINAL-ID" (:original-id error)))
      (is (<= (alength (.getBytes (:stderr result)
                                  java.nio.charset.StandardCharsets/UTF_8))
              262144)))))

(deftest unsafe-diagnostic-id-cannot-break-the-fallback
  (let [huge-id (bigint (apply str (repeat 300000 "7")))
        result (invoke ["--route" "r" "--lane" "fast" "--plan-only"]
                         {:plan-fn (fn [_]
                                   (throw (ex-info "diagnostic"
                                                   {:id huge-id
                                                    :payload (Object.)})))})
        error (one-edn (:stderr result))]
    (is (= 2 (:code result)))
    (is (empty? (:stdout result)))
    (is (= "UEP-CLI-OUTPUT-BOUND" (:id error)))
    (is (= "UEP-CLI-ERROR" (:original-id error)))))

(deftest aggregate-output-preflight-catches-large-containers-and-scalars
  (doseq [payload [(into {} (map (fn [index]
                                   [index (apply str (repeat 4096 "x"))])
                                 (range 128)))
                   (bigint (apply str (repeat 300000 "9")))
                   (symbol (apply str (repeat 100000 "s")))
                   (keyword (apply str (repeat 100000 "k")))
                   (symbol "a b")
                   (keyword "a b")
                   (map identity (range 32))]]
    (let [result (invoke ["--route" "sh07-b11-contract" "--lane" "fast"]
                         {:executor-fn (constantly
                                        (valid-result :passed [{:payload payload}]))})
          error (one-edn (:stderr result))]
      (is (= 2 (:code result)))
      (is (empty? (:stdout result)))
    (is (= "UEP-CLI-OUTPUT-BOUND" (:id error)))))
  (let [value (with-meta {:status :passed} {:unprintable (Object.)})
        plan-value (with-meta (valid-fast-plan) (meta value))
        out (binding [*print-meta* true *print-length* 1 *print-level* 0]
              (invoke ["--route" "sh07-b11-contract" "--lane" "fast" "--plan-only"]
                      {:plan-fn (constantly plan-value)}))]
    (is (zero? (:code out)))
    (is (= (valid-fast-plan) (one-edn (:stdout out))))))
