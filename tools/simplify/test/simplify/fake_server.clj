(ns simplify.fake-server
  "Protocol fixture only: no model inference, tools, network, or agent execution."
  (:require [clojure.data.json :as json]))

(defn emit! [value] (println (json/write-str value)) (flush))
(defn -main [& [mode]]
  (doseq [line (line-seq (java.io.BufferedReader. *in*))]
    (let [{:keys [id method params]} (json/read-str line :key-fn keyword)]
      (case method
        "initialize" (case mode
                       "initialize-exit" (do (binding [*out* *err*] (println "Fixture state database cannot open") (flush))
                                              (System/exit 17))
                       "initialize-malformed" (emit! {:id id :result "malformed initialization"})
                       "initialize-stall" (Thread/sleep 60000)
                       "initialize-approval" (do (emit! {:id 100 :method "item/commandExecution/requestApproval" :params {}})
                                                  (emit! {:id id :result {}}))
                       "initialize-approval-error" (do (emit! {:id 100 :method "item/commandExecution/requestApproval" :params {}})
                                                        (emit! {:id id :error {:message "Fixture approval declined"}}))
                       (emit! {:id id :result {}}))
        "initialized" nil
        "thread/start" (emit! {:id id :result {:thread {:id "fixture-thread"} :model "fixture-no-inference"}})
        "turn/interrupt" (do (emit! {:id id :result {}})
                             (emit! {:method "turn/completed"
                                     :params {:turn {:id "fixture-turn" :status "interrupted"}}}))
        "command/exec"
        (let [stream? (#{"stream-cleanup" "stream-assertion" "stream-capped" "stream-utf8" "stream-denied-zero"} mode)
              output (case mode
                       "stream-cleanup" "Owned process cleanup failed: Operation not permitted (sysctl failed)"
                       "stream-assertion" "Fixture assertion failed"
                       "stream-denied-zero" "Fixture cache: Operation not permitted"
                       "fixture streamed output")]
          (if (= "stream-utf8" mode)
            (let [bytes (.getBytes "path-\u00e9.clj\u0000" "UTF-8")]
              (doseq [stream ["stdout" "stderr"]
                      chunk [(java.util.Arrays/copyOfRange bytes 0 6)
                             (java.util.Arrays/copyOfRange bytes 6 (alength bytes))]]
                (emit! {:method "command/exec/outputDelta"
                        :params {:stream stream :deltaBase64 (.encodeToString (java.util.Base64/getEncoder) chunk)}})))
            (when stream?
            (emit! {:method "command/exec/outputDelta"
                    :params {:stream "stderr" :capReached (= mode "stream-capped")
                             :deltaBase64 (.encodeToString (java.util.Base64/getEncoder) (.getBytes output "UTF-8"))}})))
          (let [dependency-failed? (and (= "doctor-dependency-failure" mode) (some #{"-Spath"} (:command params)))
                ;; The installed CLI warns on recomputation, but a cached
                ;; classpath can return silently unless -Sforce is present.
                undeclared? (and (= "doctor-undeclared-alias" mode)
                                 (some #{"-M:missing-alias"} (:command params))
                                 (some #{"-Sforce"} (:command params)))
                doctor? (#{"doctor" "doctor-dependency-failure" "doctor-undeclared-alias"} mode)]
            (when (= "command-malformed-approval" mode)
              (emit! {:id 100 :method "item/commandExecution/requestApproval" :params {}}))
            (emit! {:id id :result
                    (case mode
                      ("command-malformed" "command-malformed-approval") {}
                      "command-nonmap" "malformed command"
                      "command-exit-type" {:exitCode "zero"}
                      "command-stdout-type" {:exitCode 0 :stdout 42}
                      "command-stderr-type" {:exitCode 0 :stderr []}
                      {:exitCode (if (or dependency-failed? (#{"failed" "stream-cleanup" "stream-assertion"} mode)) 1 0)
                       :stdout (cond stream? ""
                                     (and doctor? (= "-e" (nth (:command params) 2 nil))) ":uniform/doctor-ready\n"
                                     doctor? "/fixture/resolved-classpath"
                                     :else "fixture command")
                       :stderr (cond dependency-failed? "Could not resolve fixture Maven dependency"
                                     undeclared? "WARNING: Specified aliases are undeclared and are not being used: [:missing-alias]\n"
                                     :else "")})})))
        "turn/start" (do
                       ;; Events may precede the request response.
                       (emit! {:method "item/completed" :params {:item {:type "agentMessage" :phase "commentary" :text "working"}}})
                       (when (= mode "approval")
                         (emit! {:id 100 :method "item/commandExecution/requestApproval" :params {}}))
                       (emit! {:id id :result {:turn {:id "fixture-turn"}}})
                       (when (= mode "unfinished-action")
                         (emit! {:method "item/started"
                                 :params {:item {:type "commandExecution" :id "fixture-active" :status "inProgress"}}})
                         (emit! {:method "item/completed"
                                 :params {:item {:type "commandExecution" :id "fixture-active"
                                                 :exitCode nil :status "completed"}}}))
                       (when (= mode "tool-output-data")
                         (emit! {:method "item/completed"
                                 :params {:item {:type "commandExecution" :id "fixture-source-read"
                                                 :command "fixture-read" :exitCode 0 :status "completed"
                                                 :aggregatedOutput "(def refusal-example \"Permission denied\")"}}}))
                       (when (#{"tool-denied-zero" "tool-denied-nonzero"} mode)
                         ;; A completed tool can report an OS denial while its
                         ;; shell exits zero. No command is actually executed.
                         (emit! {:method "item/completed"
                                 :params {:item {:type "commandExecution" :id "fixture-command"
                                                 :command "fixture-read" :cwd "/fixture"
                                                 :exitCode (if (= mode "tool-denied-zero") 0 1)
                                                 :status "completed"
                                                 :aggregatedOutput "xcrun: error: fixture cache: Operation not permitted"}}}))
                       (when-not (#{"tool-denied-zero" "tool-denied-nonzero"} mode)
                       (emit! {:method "item/completed"
                               :params {:item {:type "agentMessage" :phase "final_answer"
                                               :text (if (= mode "malformed") "{}"
                                                         (json/write-str {:verdict "approved" :base "fixture-base" :candidate "fixture-candidate"
                                                                          :tree "fixture-tree"
                                                                          :summary (if (= mode "quoted-denial")
                                                                                     "The supplied example says Permission denied"
                                                                                     "Fixture only") :findings []}))}}})
                       (emit! {:method "turn/completed" :params {:turn {:id "fixture-turn" :status (case mode "failed" "failed" "interrupted" "interrupted" "completed")}}})))
        nil))))
