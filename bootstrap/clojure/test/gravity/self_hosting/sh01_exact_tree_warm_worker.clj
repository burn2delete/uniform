(ns gravity.self-hosting.sh01-exact-tree-warm-worker
  "Exact-tree-bound, non-authoritative warm JVM development test worker."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.development-test-runner :as runner])
  (:import (java.io BufferedReader InputStreamReader PushbackReader StringReader
                    StringWriter)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption Path Paths)
           (java.util.concurrent TimeUnit)))

(def request-schema :gravity/exact-tree-warm-worker-request-v1)
(def response-schema :gravity/exact-tree-warm-worker-response-v1)
(def ready-schema :gravity/exact-tree-warm-worker-ready-v1)
(def ^:private response-prefix "WARM_DEV ")
(def ^:private maximum-line-length 65536)
(def ^:private maximum-output-length (* 256 1024))
(def ^:private maximum-requests 256)
(def ^:private oid-pattern #"[0-9a-f]{40,64}")

(defn- fail! [id message data]
  (throw (ex-info message (assoc data :id id))))

(defn- strict-edn [text]
  (let [eof (Object.)]
    (with-open [reader (PushbackReader. (StringReader. text))]
      (let [value (edn/read {:eof eof :readers {}} reader)
            trailing (edn/read {:eof eof :readers {}} reader)]
        (when (or (identical? eof value) (not (identical? eof trailing)))
          (fail! "WARM-REQUEST-EDN" "request must contain exactly one EDN value" {}))
        value))))

(defn- bounded-text [value]
  (let [value (str value)]
    {:text (subs value 0 (min maximum-output-length (count value)))
     :truncated? (> (count value) maximum-output-length)}))

(defn- exact-runner-args! [args]
  (when-not (and (vector? args) (<= 4 (count args) 64)
                 (every? #(and (string? %) (<= 1 (count %) 1024)) args))
    (fail! "WARM-REQUEST-ARGS" "args must be a bounded string vector" {}))
  (loop [remaining (seq args) namespaces 0 selectors 0]
    (if-let [option (first remaining)]
      (case option
        "--namespace" (if-let [value (second remaining)]
                        (recur (nnext remaining) (inc namespaces) selectors)
                        (fail! "WARM-REQUEST-ARGS" "--namespace requires a value" {}))
        "--exact" (if-let [value (second remaining)]
                    (recur (nnext remaining) namespaces (inc selectors))
                    (fail! "WARM-REQUEST-ARGS" "--exact requires a value" {}))
        "--fail-fast" (recur (next remaining) namespaces selectors)
        (fail! "WARM-REQUEST-ARGS" "worker accepts only exact development selections"
               {:option option}))
      (when-not (and (pos? namespaces) (pos? selectors))
        (fail! "WARM-REQUEST-ARGS"
               "worker requires at least one namespace and exact selector" {}))))
  args)

(defn validate-request! [request expected]
  (when-not (map? request)
    (fail! "WARM-REQUEST-SHAPE" "request must be a map" {}))
  (let [operation (:operation request)
        allowed (if (= :shutdown operation)
                  #{:schema :request-id :operation :candidate-commit :candidate-tree}
                  #{:schema :request-id :operation :candidate-commit :candidate-tree :args})]
    (when-not (= allowed (set (keys request)))
      (fail! "WARM-REQUEST-SHAPE" "request keys are not closed"
             {:keys (vec (sort (map str (keys request))))}))
    (when-not (= request-schema (:schema request))
      (fail! "WARM-REQUEST-SCHEMA" "request schema is unsupported" {}))
    (when-not (and (string? (:request-id request))
                   (<= 1 (count (:request-id request)) 160))
      (fail! "WARM-REQUEST-ID" "request id must be bounded text" {}))
    (when-not (contains? #{:run-exact :shutdown} operation)
      (fail! "WARM-REQUEST-OPERATION" "operation is unsupported" {}))
    (doseq [field [:candidate-commit :candidate-tree]]
      (when-not (= (field expected) (field request))
        (fail! "WARM-TREE-MISMATCH" "request identity differs from worker identity"
               {:field field :expected (field expected) :actual (field request)})))
    (when (= :run-exact operation) (exact-runner-args! (:args request)))
    request))

(defn- git-text [^Path root args]
  (let [builder (doto (ProcessBuilder. ^java.util.List (into ["git"] args))
                  (.directory (.toFile root))
                  (.redirectErrorStream true))
        env (.environment builder)]
    (.put env "GIT_OPTIONAL_LOCKS" "0")
    (let [process (.start builder)
          output (String. (.readAllBytes (.getInputStream process)) StandardCharsets/UTF_8)]
      (when-not (and (.waitFor process 10 TimeUnit/SECONDS)
                     (zero? (.exitValue process)))
        (.destroyForcibly process)
        (fail! "WARM-GIT" "git identity observation failed" {:args args}))
      (str/trim output))))

(defn observe-identity [^Path root]
  (let [commit (git-text root ["rev-parse" "--verify" "HEAD"])
        tree (git-text root ["rev-parse" "--verify" "HEAD^{tree}"])
        status (git-text root ["status" "--porcelain=v1" "--untracked-files=all" "--no-renames"])]
    (when-not (and (re-matches oid-pattern commit) (re-matches oid-pattern tree))
      (fail! "WARM-GIT" "git returned malformed identities" {}))
    {:candidate-commit commit :candidate-tree tree :clean? (str/blank? status)}))

(defn- response [request outcome exit-code elapsed-ns out err request-count]
  (merge {:schema response-schema :authority :non-authoritative
          :authoritative? false :request-id (:request-id request)
          :candidate-commit (:candidate-commit request)
          :candidate-tree (:candidate-tree request)
          :outcome outcome :exit-code exit-code :elapsed-ns elapsed-ns
          :request-count request-count}
         (let [{out-text :text out-cut :truncated?} (bounded-text out)
               {err-text :text err-cut :truncated?} (bounded-text err)]
           {:stdout out-text :stderr err-text
            :output-truncated? (or out-cut err-cut)})))

(defn- invoke-runner [args]
  (let [out (StringWriter.) err (StringWriter.) started (System/nanoTime)]
    (try
      (let [exit (binding [*out* out *err* err] (runner/run-cli! args))]
        {:exit exit :elapsed-ns (- (System/nanoTime) started)
         :out (str out) :err (str err)})
      (catch VirtualMachineError error (throw error))
      (catch ThreadDeath error (throw error))
      (catch InterruptedException error
        (.interrupt (Thread/currentThread))
        (throw error))
      (catch Throwable error
        {:exit 2 :elapsed-ns (- (System/nanoTime) started)
         :out (str out) :err (str err (.getName (class error)) ": " (.getMessage error))}))))

(defn run-session!
  [lines emit! expected observe! execute! max-count]
  (loop [remaining (seq lines) request-count 0]
    (when-let [line (first remaining)]
      (when (> (count line) maximum-line-length)
        (fail! "WARM-REQUEST-BOUNDS" "request line exceeds bound" {}))
      (let [request (validate-request! (strict-edn line) expected)
            live-before (observe!)]
        (when-not (= (assoc expected :clean? true) live-before)
          (fail! "WARM-TREE-DRIFT" "worker checkout changed after startup"
                 {:expected expected :actual live-before}))
        (if (= :shutdown (:operation request))
          (emit! (response request :shutdown 0 0 "" "" request-count))
          (let [result (execute! (:args request))
                next-count (inc request-count)
                live-after (observe!)
                drift? (not= (assoc expected :clean? true) live-after)
                exit (if drift? 2 (:exit result))
                outcome (cond drift? :tree-drift (zero? exit) :passed :else :failed)]
            (emit! (response request outcome exit (:elapsed-ns result)
                             (:out result) (:err result) next-count))
            (when (and (zero? exit) (< next-count max-count))
              (recur (next remaining) next-count))))))))

(defn- parse-cli [args]
  (loop [remaining (seq args) result {:max-requests 64}]
    (if-let [option (first remaining)]
      (let [value (second remaining)]
        (when-not value (fail! "WARM-CLI" "option requires a value" {:option option}))
        (case option
          "--expected-commit" (recur (nnext remaining) (assoc result :candidate-commit value))
          "--expected-tree" (recur (nnext remaining) (assoc result :candidate-tree value))
          "--max-requests" (recur (nnext remaining) (assoc result :max-requests (parse-long value)))
          (fail! "WARM-CLI" "unknown option" {:option option})))
      result)))

(defn -main [& args]
  (try
    (let [{:keys [candidate-commit candidate-tree max-requests] :as expected} (parse-cli args)
          expected (select-keys expected [:candidate-commit :candidate-tree])
          root (.toRealPath (.toAbsolutePath (Paths/get (System/getProperty "user.dir")
                                                       (make-array String 0)))
                            (make-array LinkOption 0))
          observed (observe-identity root)]
      (when-not (and (re-matches oid-pattern (or candidate-commit ""))
                     (re-matches oid-pattern (or candidate-tree ""))
                     (integer? max-requests) (<= 1 max-requests maximum-requests)
                     (= (assoc expected :clean? true) observed))
        (fail! "WARM-STARTUP-IDENTITY" "startup identities are not exact and clean"
               {:expected expected :observed observed}))
      (println (str response-prefix (pr-str {:schema ready-schema :authority :non-authoritative
                                             :candidate-commit candidate-commit
                                             :candidate-tree candidate-tree})))
      (flush)
      (with-open [reader (BufferedReader. (InputStreamReader. System/in StandardCharsets/UTF_8))]
        (run-session! (line-seq reader)
                      #(do (println (str response-prefix (pr-str %))) (flush))
                      expected #(observe-identity root) invoke-runner max-requests))
      (shutdown-agents))
    (catch Throwable error
      (binding [*out* *err*]
        (println (pr-str {:schema response-schema :outcome :rejected
                          :id (or (:id (ex-data error)) "WARM-UNEXPECTED")
                          :message (.getMessage error)})))
      (System/exit 2))))
