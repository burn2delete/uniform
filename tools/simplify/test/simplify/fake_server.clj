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
        "command/exec" (emit! {:id id :result {:exitCode (if (= mode "failed") 1 0) :stdout "fixture command" :stderr ""}})
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
