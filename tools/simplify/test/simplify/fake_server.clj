(ns simplify.fake-server
  "Protocol fixture only: no model inference, tools, network, or agent execution."
  (:require [clojure.data.json :as json]))

(defn emit! [value] (println (json/write-str value)) (flush))
(defn -main [& [mode]]
  (doseq [line (line-seq (java.io.BufferedReader. *in*))]
    (let [{:keys [id method]} (json/read-str line :key-fn keyword)]
      (case method
        "initialize" (emit! {:id id :result {}})
        "initialized" nil
        "thread/start" (emit! {:id id :result {:thread {:id "fixture-thread"} :model "fixture-no-inference"}})
        "command/exec"
        (let [stream? (#{"stream-cleanup" "stream-assertion" "stream-capped" "stream-utf8"} mode)
              output (case mode
                       "stream-cleanup" "Owned process cleanup failed: Operation not permitted (sysctl failed)"
                       "stream-assertion" "Fixture assertion failed"
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
          (emit! {:id id :result {:exitCode (if (#{"failed" "stream-cleanup" "stream-assertion"} mode) 1 0)
                                 :stdout (if stream? "" "fixture command") :stderr ""}}))
        "turn/start" (do
                       ;; Events may precede the request response.
                       (emit! {:method "item/completed" :params {:item {:type "agentMessage" :phase "commentary" :text "working"}}})
                       (when (= mode "approval")
                         (emit! {:id 100 :method "item/commandExecution/requestApproval" :params {}}))
                       (emit! {:id id :result {:turn {:id "fixture-turn"}}})
                       (emit! {:method "item/completed"
                               :params {:item {:type "agentMessage" :phase "final_answer"
                                               :text (if (= mode "malformed") "{}"
                                                         (json/write-str {:verdict "approved" :base "fixture-base" :candidate "fixture-candidate"
                                                                          :tree "fixture-tree" :summary "Fixture only" :findings []}))}}})
                       (emit! {:method "turn/completed" :params {:turn {:id "fixture-turn" :status (case mode "failed" "failed" "interrupted" "interrupted" "completed")}}}))
        nil))))
