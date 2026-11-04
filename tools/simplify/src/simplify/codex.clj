(ns simplify.codex
  "Version-pinned app-server JSON-RPC client. One managed process per bounded job."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [simplify.runtime :as rt]
            [simplify.schema :as schema])
  (:import [java.util.concurrent LinkedBlockingQueue]))

(defn send! [client message]
  (rt/event! (:ctx client) :rpc-send {:job (:job client) :message message})
  (let [writer (:writer client)]
    (locking writer
      (.write ^java.io.Writer writer (str (json/write-str message) "\n"))
      (.flush ^java.io.Writer writer))))

(defn receive! [client message]
  (rt/event! (:ctx client) :rpc-receive
             {:job (:job client) :message (if (= "command/exec/outputDelta" (:method message))
                                           (update message :params dissoc :deltaBase64) message)})
  (cond
    (and (:id message) (:method message))
    (do
      (reset! (:restricted client) message)
      ;; Never approve a server-initiated privileged operation or invoke an unknown tool.
      (if (#{"item/commandExecution/requestApproval" "item/fileChange/requestApproval"} (:method message))
        (send! client {:id (:id message) :result {:decision "decline"}})
        (send! client {:id (:id message) :error {:code -32601 :message "Harness does not authorize this request"}})))
    (:id message) (when-let [response (get @(:pending client) (:id message))] (deliver response message))
    (= "command/exec/outputDelta" (:method message))
    (let [params (:params message)
          output (String. (.decode (java.util.Base64/getDecoder) ^String (:deltaBase64 params)) "UTF-8")]
      (when-let [capture (:command-output client)]
        (swap! capture conj {:stream (:stream params) :output output}))
      (when (and (:capReached params) (:capture-truncated client))
        (reset! (:capture-truncated client) true))
      (rt/event! (:ctx client) :sandbox-command-output {:job (:job client) :stream (:stream params) :output output :capture-truncated (:capReached params)}))
    :else (.put ^LinkedBlockingQueue (:events client) message)))

(defn open! [ctx job cwd]
  (let [process (.start (doto (ProcessBuilder. ^java.util.List (vec (get-in ctx [:config :codex-command])))
                         (.directory (io/file cwd))))
        _ (when-let [processes (:processes ctx)] (swap! processes conj process))
        client {:ctx ctx :job job :process process :writer (io/writer (.getOutputStream process) :encoding "UTF-8")
                :pending (atom {}) :next-id (atom 0) :restricted (atom nil) :closed (atom false)
                :command-output (atom []) :capture-truncated (atom false) :events (LinkedBlockingQueue.)}
        reader (future
                 (try
                   (with-open [input (io/reader (.getInputStream process) :encoding "UTF-8")]
                     (doseq [line (line-seq input)] (receive! client (json/read-str line :key-fn keyword))))
                   (catch Exception e
                     (rt/event! ctx :rpc-reader-error {:job job :message (.getMessage e)}))
                   (finally
                     (reset! (:closed client) true)
                     (doseq [[_ p] @(:pending client)] (deliver p {:error {:message "App-server connection closed"}}))
                     (.put ^LinkedBlockingQueue (:events client) {:method "harness/closed"}))))
        stderr (future
                 (with-open [input (io/reader (.getErrorStream process) :encoding "UTF-8")]
                   (doseq [line (line-seq input)] (rt/event! ctx :app-server-stderr {:job job :line line}))))]
    (assoc client :reader reader :stderr stderr)))

(defn request! [client method params]
  (let [id (swap! (:next-id client) inc) response (promise)]
    (swap! (:pending client) assoc id response)
    (try
      (when @(:closed client) (rt/fail! "App-server is already closed" {:method method}))
      (send! client {:id id :method method :params params})
      (let [result @response]
        (when-let [error (:error result)] (rt/fail! "App-server request failed" {:method method :error error}))
        (:result result))
      (finally (swap! (:pending client) dissoc id)))))

(defn close! [client]
  (try (.close ^java.io.Writer (:writer client)) (catch Exception _ nil))
  (.destroy ^Process (:process client))
  (when-not (.waitFor ^Process (:process client) 5 java.util.concurrent.TimeUnit/SECONDS)
    (rt/event! (:ctx client) :app-server-shutdown-watchdog {:job (:job client)})
    (.destroyForcibly ^Process (:process client)))
  (.waitFor ^Process (:process client))
  (when-let [processes (get-in client [:ctx :processes])] (swap! processes disj (:process client)))
  @(:reader client)
  @(:stderr client))

(defn command!
  "Execute checks without inference. Model-selected argv stays inside the worker sandbox."
  [ctx job-id cwd argv]
  (let [client (open! ctx job-id cwd)]
    (try
      (request! client "initialize" {:clientInfo (or (get-in ctx [:config :client-info])
                                                   {:name "uniform_simplify" :title "Uniform Simplify" :version "0.1.0"})})
      (send! client {:method "initialized" :params {}})
      (let [response (request! client "command/exec"
                             {:command argv :cwd cwd :processId job-id :streamStdoutStderr true
                              :timeoutMs (get-in ctx [:config :check-timeout-ms])
                              :sandboxPolicy {:type "workspaceWrite" :writableRoots [cwd] :networkAccess false}})
            chunks @(:command-output client)
            stream (fn [name] (apply str (map :output (filter #(= name (:stream %)) chunks))))
            result (assoc response
                          :stdout (if (seq (:stdout response)) (:stdout response) (stream "stdout"))
                          :stderr (if (seq (:stderr response)) (:stderr response) (stream "stderr"))
                          :capture-truncated @(:capture-truncated client))
            output (str (:stdout result) "\n" (:stderr result))
            cleanup-unknown? (boolean (re-find #"(?i)owned process cleanup failed" output))
            ended? (and (number? (:exitCode result)) (not cleanup-unknown?))]
        (when @(:restricted client)
          (rt/fail! "Check encountered a restricted request; not retrying"
                    {:restricted true :writer-ended? ended? :result result}))
        (when-not (zero? (:exitCode result))
          (rt/fail! "Sandboxed check failed"
                    {:command argv :result result :writer-ended? ended?
                     :restricted (or (rt/authorization-denied? output)
                                     (boolean (re-find #"(?i)operation not permitted|sysctl failed" output)))}))
        result)
      (finally (close! client)))))

(defn await-turn! [client turn-id]
  (loop [messages []]
    (let [{:keys [method params]} (.take ^LinkedBlockingQueue (:events client))]
      (case method
        "harness/closed" (rt/fail! "App-server closed before turn completion" {:job (:job client)})
        "item/completed" (let [item (:item params)]
                           (recur (if (= "agentMessage" (:type item)) (conj messages item) messages)))
        "turn/completed" (if (= turn-id (get-in params [:turn :id]))
                           (do
                             (rt/update! (:ctx client) assoc-in [:jobs (:job client) :turn-terminal?] true)
                             (when @(:restricted client) (rt/fail! "Agent encountered a restricted request; not retrying" {:restricted true}))
                             (when-not (= "completed" (get-in params [:turn :status]))
                               (rt/fail! "Agent turn did not complete" {:turn (:turn params)}))
                             (or (:text (last (filter #(= "final_answer" (:phase %)) messages)))
                                 (:text (last messages))
                                 (rt/fail! "Agent returned no final output" {})))
                           (recur messages))
        (recur messages)))))

(defn job! [ctx job-id cwd mode instructions prompt output-schema]
  (let [client (open! ctx job-id cwd)]
    (try
      (request! client "initialize" {:clientInfo (or (get-in ctx [:config :client-info])
                                                   {:name "uniform_simplify" :title "Uniform Simplify" :version "0.1.0"})})
      (send! client {:method "initialized" :params {}})
      (let [model (get-in ctx [:config :model])
            started (request! client "thread/start"
                                    (cond-> {:cwd cwd :approvalPolicy "never" :sandbox (if (= mode :write) "workspace-write" "read-only")
                                             :developerInstructions instructions}
                                      model (assoc :model model)))
            thread (get-in started [:thread :id])
            _ (rt/update! ctx assoc-in [:jobs job-id] {:thread thread :status :running :mode mode :cwd cwd
                                                       :model (:model started) :instruction-sources (:instructionSources started)
                                                       :prompt-sha256 (rt/digest prompt)})
            _ (spit (io/file (:state-dir ctx) (str job-id ".prompt.edn")) (pr-str {:instructions instructions :input prompt :schema output-schema :model model}))
            turn (get-in (request! client "turn/start"
                                  {:threadId thread :input [{:type "text" :text prompt}] :outputSchema output-schema
                                   :sandboxPolicy (if (= mode :write)
                                                    {:type "workspaceWrite" :writableRoots [cwd] :networkAccess false}
                                                    {:type "readOnly"})}) [:turn :id])
            _ (rt/update! ctx assoc-in [:jobs job-id :turn] turn)
            output (schema/validate! output-schema (json/read-str (await-turn! client turn) :key-fn keyword))]
        (rt/update! ctx update-in [:jobs job-id] assoc :status :completed :output output)
        output)
      (catch Exception e
        (rt/update! ctx update-in [:jobs job-id] merge {:status :attention :message (.getMessage e) :restricted (boolean (:restricted (ex-data e)))})
        (throw (ex-info (.getMessage e)
                        (assoc (or (ex-data e) {}) :writer-ended?
                               (true? (get-in @(:state ctx) [:jobs job-id :turn-terminal?]))) e)))
      (finally (close! client)))))
