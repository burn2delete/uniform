

(defn- c-backend-start-output-pump!
  [input-stream stream-kind]
  (let [outcome (promise)
        thread
        (Thread.
         (fn []
           (try
             (deliver outcome
                      (assoc (*c-backend-process-read-stream-fn*
                              input-stream *c-backend-process-max-output-bytes*)
                             :stream stream-kind))
             (catch InterruptedException error
               ;; Preserve the exact worker interruption for the supervising
               ;; main thread; it is not an ordinary read error.
               (deliver outcome
                        {:stream stream-kind
                         :fatal-error error}))
             (catch Error error
               ;; OOME and ThreadDeath retain identity across the bounded
               ;; pump handoff and are rethrown by the main process path.
               (deliver outcome
                        {:stream stream-kind
                         :fatal-error error}))
             (catch Exception error
               (deliver outcome
                        {:stream stream-kind
                         :read-error error}))
             ))
         (str "gravity-c-backend-" (name stream-kind) "-pump"))]
    (.setDaemon thread true)
    (try
      (.start thread)
      (catch Throwable error
        (try (.close ^java.io.InputStream input-stream)
             (catch Throwable cleanup
               (.addSuppressed ^Throwable error ^Throwable cleanup)))
        (throw error)))
    {:thread thread
     :input input-stream
     :outcome outcome
     :stream stream-kind}))

(defn- c-backend-output-pump-failure
  [pump]
  (when (realized? (:outcome pump))
    (let [outcome @(:outcome pump)]
      (when (or (:decode-error outcome)
                (:fatal-error outcome)
                (:read-error outcome))
        outcome))))

(defn- c-backend-await-process!
  [process pumps]
  (let [deadline (+ (System/nanoTime)
                    (* *c-backend-process-timeout-ms* 1000000))]
    (loop []
      (if-let [failure (some c-backend-output-pump-failure pumps)]
        {:status :output-failure :failure failure}
        (cond
          (.waitFor process 10 java.util.concurrent.TimeUnit/MILLISECONDS)
          {:status :finished}

          (>= (System/nanoTime) deadline)
          {:status :timeout}

          :else
          (recur))))))

(defn- c-backend-fail-output-pump!
  [process failure source-path target role]
  (let [fatal (:fatal-error failure)
        termination
        (try
          (c-backend-terminate-process-tree! process source-path target)
          (catch Throwable cleanup
            (if fatal
              (do
                (.addSuppressed ^Throwable fatal ^Throwable cleanup)
                nil)
              (throw cleanup))))]
    (when fatal
      (when (instance? InterruptedException fatal)
        (.interrupt (Thread/currentThread)))
      (throw ^Throwable fatal))
    (if (:limit-exceeded? failure)
      (c-backend-fail!
       "B2-DIALECT" "C backend process output exceeded its bound"
       source-path target nil
       (assoc (dissoc failure :limit-exceeded?)
              :maximum-byte-count *c-backend-process-max-output-bytes*
              :observed-byte-count (:total-byte-count failure)
              :missing-fact :bounded-c-backend-process-output
              :role role
              :termination termination))
      (c-backend-fail!
       "B2-DIALECT" "C backend process output could not be read"
       source-path target nil
       {:missing-fact :c-backend-process-output-read
        :stream (:stream failure)
        :role role
        :decode-error? (boolean (:decode-error failure))
        :cause-message
        (some-> (or (:read-error failure) (:decode-error failure))
                ^Throwable
                .getMessage)
        :stream-read-complete? (:stream-read-complete? failure)
        :hash (:hash failure)
        :termination termination}))))

(defn- c-backend-finish-output-pump!
  [pump source-path target]
  (let [thread ^Thread (:thread pump)]
    (.join thread 2000)
    (when (.isAlive thread)
      (try (.close ^java.io.InputStream (:input pump))
           (catch Exception _ nil))
      (.interrupt thread)
      (.join thread 2000)
      (when (.isAlive thread)
        (c-backend-fail!
         "B2-DIALECT" "C backend process output pump did not terminate"
         source-path target nil
         {:missing-fact :c-backend-process-output-pump-termination
          :stream (:stream pump)})))
    @(:outcome pump)))

(defn- c-backend-clean-output-pump!
  [pump]
  (let [thread ^Thread (:thread pump)
        caller (Thread/currentThread)
        restore-interrupt? (atom (.isInterrupted caller))
        cleanup-error (atom nil)
        record-error!
        (fn [error]
          (when (instance? InterruptedException error)
            (reset! restore-interrupt? true)
            ;; InterruptedException clears the flag.  Keep it clear until all
            ;; bounded cleanup steps have had a chance to run.
            (Thread/interrupted))
          (if-let [existing @cleanup-error]
            (.addSuppressed ^Throwable existing ^Throwable error)
            (reset! cleanup-error error)))]
    ;; A pre-existing caller interrupt would make join throw immediately and
    ;; skip containment cleanup.  Temporarily clear it, then restore it after
    ;; close/join/interrupt/rejoin/alive verification completes.
    (when @restore-interrupt?
      (Thread/interrupted))
    (try
      (try
        (.close ^java.io.InputStream (:input pump))
        (catch Throwable error
          (record-error! error)))
      (try
        (.join thread 2000)
        (catch Throwable error
          (record-error! error)))
      (when (.isAlive thread)
        (.interrupt thread)
        (try
          (.join thread 2000)
          (catch Throwable error
            (record-error! error))))
      (when (.isAlive thread)
        (record-error!
         (ex-info
          "C backend process output pump cleanup did not terminate"
          {:id "B2-DIALECT"
           :missing-fact :c-backend-process-output-pump-cleanup
           :stream (:stream pump)})))
      @cleanup-error
      (finally
        (when @restore-interrupt?
          (.interrupt caller))))))