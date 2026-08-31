(ns gravity.self-hosting.sh01-exact-tree-warm-worker
  "Exact-tree-bound, non-authoritative warm JVM development test worker."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test]
            [gravity.development-test-runner :as runner])
  (:import (java.io BufferedReader ByteArrayOutputStream InputStreamReader
                    OutputStream OutputStreamWriter PushbackReader StringReader)
           (java.nio ByteBuffer)
           (java.nio.charset CodingErrorAction)
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
(def ^:private namespace-catalog-snapshot runner/namespace-catalog)
(def ^:private namespace-authority
  (set (map (comp str :namespace) namespace-catalog-snapshot)))

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

(defn- bounded-writer []
  (let [retained (ByteArrayOutputStream.)
        observed (atom 0)
        stream (proxy [OutputStream] []
      (write
        ([value]
         (let [current @observed]
           (swap! observed inc)
           (when (< current maximum-output-length)
             (.write retained (int value)))))
        ([bytes offset length]
         (let [remaining (max 0 (- maximum-output-length @observed))
               accepted (int (min remaining length))]
           (swap! observed + length)
           (when (pos? accepted)
             (.write retained ^bytes bytes (int offset) accepted)))))
      (flush [])
      (close []))]
    {:writer (OutputStreamWriter. stream StandardCharsets/UTF_8)
     :retained retained :observed observed}))

(defn- strict-utf8-prefix [^bytes bytes]
  (loop [length (alength bytes)]
    (let [decoded
          (try
            (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                            (.onMalformedInput CodingErrorAction/REPORT)
                            (.onUnmappableCharacter CodingErrorAction/REPORT))]
              (str (.decode decoder (ByteBuffer/wrap bytes 0 length))))
            (catch java.nio.charset.CharacterCodingException _ nil))]
      (if (some? decoded)
        decoded
        (if (and (pos? length) (<= (- (alength bytes) length) 3))
          (recur (dec length))
          "")))))

(defn- bounded-writer-result [{:keys [^OutputStreamWriter writer
                                      ^ByteArrayOutputStream retained observed]}]
  (.flush writer)
  {:text (strict-utf8-prefix (.toByteArray retained))
   :truncated? (> @observed maximum-output-length)})

(defn- fatal-cause [throwable]
  (loop [cause throwable]
    (cond
      (nil? cause) nil
      (or (instance? ThreadDeath cause)
          (instance? VirtualMachineError cause)
          (instance? LinkageError cause)) cause
      :else (recur (.getCause ^Throwable cause)))))

(defn- exact-runner-args! [args]
  (when-not (and (vector? args) (<= 4 (count args) 64)
                 (every? #(and (string? %) (<= 1 (count %) 1024)) args))
    (fail! "WARM-REQUEST-ARGS" "args must be a bounded string vector" {}))
  (loop [remaining (seq args) namespaces 0 selectors 0]
    (if-let [option (first remaining)]
      (case option
        "--namespace" (if-let [value (second remaining)]
                        (do
                          (when-not (contains? namespace-authority value)
                            (fail! "WARM-REQUEST-NAMESPACE"
                                   "namespace is outside the frozen development catalog"
                                   {:namespace value}))
                          (recur (nnext remaining) (inc namespaces) selectors))
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

(defn- bounded-process-output [process]
  (let [stream (.getInputStream process)
        output (ByteArrayOutputStream.)
        buffer (byte-array 4096)]
    (loop [total 0]
      (let [read (.read stream buffer)]
        (if (neg? read)
          (.toString output (.name StandardCharsets/UTF_8))
          (let [next-total (+ total read)]
            (when (> next-total maximum-output-length)
              (.destroyForcibly process)
              (fail! "WARM-GIT-BOUNDS" "git output exceeds bound" {}))
            (.write output buffer 0 read)
            (recur next-total)))))))

(defn- git-text [^Path root args]
  (let [builder (doto (ProcessBuilder. ^java.util.List (into ["git"] args))
                  (.directory (.toFile root))
                  (.redirectErrorStream true))
        env (.environment builder)]
    (.put env "GIT_OPTIONAL_LOCKS" "0")
    (let [process (.start builder)
          output-future (future (bounded-process-output process))
          exited? (.waitFor process 10 TimeUnit/SECONDS)]
      (when-not (and exited? (zero? (.exitValue process)))
        (.destroyForcibly process)
        (future-cancel output-future)
        (fail! "WARM-GIT" "git identity observation failed" {:args args}))
      (let [timeout (Object.)
            output (deref output-future 1000 timeout)]
        (when (identical? timeout output)
          (.destroyForcibly process)
          (future-cancel output-future)
          (fail! "WARM-GIT" "git output capture timed out" {:args args}))
        (str/trim output)))))

(defn observe-identity [^Path root]
  (let [commit (git-text root ["rev-parse" "--verify" "HEAD"])
        tree (git-text root ["rev-parse" "--verify" "HEAD^{tree}"])
        status (git-text root ["status" "--porcelain=v1" "--untracked-files=all" "--no-renames"])]
    (when-not (and (re-matches oid-pattern commit) (re-matches oid-pattern tree))
      (fail! "WARM-GIT" "git returned malformed identities" {}))
    {:candidate-commit commit :candidate-tree tree :clean? (str/blank? status)}))

(defn- response [request outcome exit-code elapsed-ns result request-count]
  (merge {:schema response-schema :authority :non-authoritative
          :authoritative? false :request-id (:request-id request)
          :candidate-commit (:candidate-commit request)
          :candidate-tree (:candidate-tree request)
          :outcome outcome :exit-code exit-code :elapsed-ns elapsed-ns
          :request-count request-count}
         (let [{out-text :text out-cut :truncated?} (bounded-text (:out result))
               {err-text :text err-cut :truncated?} (bounded-text (:err result))]
           {:stdout out-text :stderr err-text
            :output-truncated? (boolean (or out-cut err-cut
                                            (:out-truncated? result)
                                            (:err-truncated? result)))})))

(defn- invoke-runner [args]
  (let [out-capture (bounded-writer) err-capture (bounded-writer)
        out (:writer out-capture) err (:writer err-capture)
        started (System/nanoTime)]
    (try
      (let [exit (binding [*out* out *err* err test/*test-out* out]
                   (runner/run-cli! args))
            out-result (bounded-writer-result out-capture)
            err-result (bounded-writer-result err-capture)]
        {:exit exit :elapsed-ns (- (System/nanoTime) started)
         :out (:text out-result) :err (:text err-result)
         :out-truncated? (:truncated? out-result)
         :err-truncated? (:truncated? err-result)})
      (catch Throwable error
        (cond
          (instance? InterruptedException error)
          (do (.interrupt (Thread/currentThread)) (throw error))

          (fatal-cause error)
          (throw (fatal-cause error))

          :else
          (let [out-result (bounded-writer-result out-capture)
                err-result (bounded-writer-result err-capture)]
            {:exit 2 :elapsed-ns (- (System/nanoTime) started)
             :out (:text out-result)
             :err (str (:text err-result) (.getName (class error)) ": " (.getMessage error))
             :out-truncated? (:truncated? out-result)
             :err-truncated? (:truncated? err-result)}))))))

(defn run-session!
  [lines emit! expected observe! execute! max-count]
  (loop [remaining (seq lines) request-count 0]
    (if-let [line (first remaining)]
      (do
      (when (> (count line) maximum-line-length)
        (fail! "WARM-REQUEST-BOUNDS" "request line exceeds bound" {}))
      (let [request (validate-request! (strict-edn line) expected)
            live-before (observe!)]
        (when-not (= namespace-catalog-snapshot runner/namespace-catalog)
          (fail! "WARM-CATALOG-DRIFT" "development namespace catalog changed" {}))
        (when-not (= (assoc expected :clean? true) live-before)
          (fail! "WARM-TREE-DRIFT" "worker checkout changed after startup"
                 {:expected expected :actual live-before}))
        (if (= :shutdown (:operation request))
          (do (emit! (response request :shutdown 0 0 {:out "" :err ""} request-count)) 0)
          (let [result (execute! (:args request))
                next-count (inc request-count)
                live-after (observe!)
                catalog-drift? (not= namespace-catalog-snapshot runner/namespace-catalog)
                drift? (or catalog-drift?
                           (not= (assoc expected :clean? true) live-after))
                exit (if drift? 2 (:exit result))
                outcome (cond catalog-drift? :catalog-drift
                              drift? :tree-drift
                              (zero? exit) :passed
                              :else :failed)]
            (emit! (response request outcome exit (:elapsed-ns result)
                             result next-count))
            (cond
              (not (zero? exit)) exit
              (< next-count max-count) (recur (next remaining) next-count)
              :else 0)))))
      0)))

(defn- read-bounded-line [^BufferedReader reader]
  (let [line (StringBuilder.)]
    (loop []
      (let [character (.read reader)]
        (cond
          (neg? character) (when (pos? (.length line)) (.toString line))
          (= character (int \newline)) (.toString line)
          :else (do
                  (when (>= (.length line) maximum-line-length)
                    (fail! "WARM-REQUEST-BOUNDS" "request line exceeds bound" {}))
                  (when-not (= character (int \return))
                    (.append line (char character)))
                  (recur)))))))

(defn- bounded-lines [^BufferedReader reader]
  (lazy-seq
   (when-let [line (read-bounded-line reader)]
     (cons line (bounded-lines reader)))))

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
      (let [exit (with-open [reader (BufferedReader. (InputStreamReader. System/in StandardCharsets/UTF_8))]
                   (run-session! (bounded-lines reader)
                                 #(do (println (str response-prefix (pr-str %))) (flush))
                                 expected #(observe-identity root) invoke-runner max-requests))]
        (shutdown-agents)
        (when-not (zero? exit) (System/exit exit))))
    (catch Throwable error
      (cond
        (instance? InterruptedException error)
        (do (.interrupt (Thread/currentThread)) (throw error))

        (fatal-cause error)
        (throw (fatal-cause error))

        :else
        (do
          (binding [*out* *err*]
            (println (pr-str {:schema response-schema :outcome :rejected
                              :id (or (:id (ex-data error)) "WARM-UNEXPECTED")
                              :message (.getMessage error)})))
          (System/exit 2))))))
