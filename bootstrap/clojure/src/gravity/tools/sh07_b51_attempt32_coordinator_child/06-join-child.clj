
(defn join-child!
  "Join writer, child, and concurrent bounded drains under one deadline.

  Parsing is deliberately absent here; the ordinal-3/7 exit observation is
  recorded before the coordinator validates the returned bytes."
  [started codec]
  (let [^Process process (:process started)
        executor (Executors/newFixedThreadPool 4)
        completions (ExecutorCompletionService. executor)
        request-bytes (:request-bytes started)
        stdout (.getInputStream process)
        stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        read-bounded (:read-bounded-bytes codec)
        stdout-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_] [:stdout (read-bounded :response stdout)])))
        stderr-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_] [:stderr (read-bounded :stderr stderr)])))
        writer-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     ((:bounded-byte-count! codec)
                      :request (alength ^bytes request-bytes))
                     (.write ^OutputStream stdin ^bytes request-bytes)
                     (.flush ^OutputStream stdin)
                     (.close ^OutputStream stdin)
                     [:writer
                      {:byte-count (alength ^bytes request-bytes)
                       :closed true}])))
        process-task
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     (.waitFor process)
                     [:process (.exitValue process)])))
        tasks [stdout-task stderr-task writer-task process-task]
        deadline (+ (System/nanoTime)
                    (* 1000000 (:deadline-ms transport-limits)))]
    (try
      (let [results
            (loop [remaining-tasks 4
                   results {}]
              (if (zero? remaining-tasks)
                results
                (let [remaining (remaining-millis deadline)
                      _ (when (zero? remaining)
                          (throw (TimeoutException. "child tasks")))
                      completed
                      (.poll completions remaining TimeUnit/MILLISECONDS)]
                  (when-not completed
                    (throw (TimeoutException. "child tasks")))
                  (let [[task value]
                        (try
                          (.get ^Future completed)
                          (catch ExecutionException error
                            (throw (or (.getCause error) error))))]
                    (recur (dec remaining-tasks)
                           (assoc results task value))))))
            writer (:writer results)
            stdout-bytes (:stdout results)
            stderr-bytes (:stderr results)]
        (assoc started
               :exit-code (:process results)
               :stdout-bytes stdout-bytes
               :stderr-bytes stderr-bytes
               :transport
               {:stdin-byte-count (:byte-count writer)
                :stdin-raw-sha256 ((:R codec) request-bytes)
                :stdin-closed-before-wait (:closed writer)
                :stdout-cap (:response-max-bytes transport-limits)
                :stderr-cap (:stderr-max-bytes transport-limits)
                :deadline-ms (:deadline-ms transport-limits)
                :stdout-drain-started-before-wait true
                :stderr-drain-started-before-wait true
                :stdout-drain-complete true
                :stderr-drain-complete true
                :timed-out false
                :destroy-called false
                :destroy-forcibly-called false
                :tasks-joined true}))
      (catch Throwable error
        (doseq [task tasks] (.cancel ^Future task true))
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (terminate-process! process)
        (reject! (if (instance? TimeoutException error)
                   :deadline :transport-task)
                 {:role (:role started)
                  :cause (.getName (class error))}))
      (finally
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (.shutdownNow executor)
        (.awaitTermination executor 1000 TimeUnit/MILLISECONDS)))))

(defn- load-git-evidence! []
  (require (symbol git-evidence-namespace))
  {:validate-eligibility-object!
   (resolve-public! git-evidence-namespace "validate-eligibility-object!")})

(defn- authenticate-eligibility-input!
  "Run the shared show/diff-tree/ls-tree/blob/ls-tree Q sequence and the full
  209-operation PRE reconstruction before constructing A or B."
  [options codec]
  (let [coordinate (:eligibility options)
        validate! (:validate-eligibility-object! (load-git-evidence!))
        result (validate! {:repository-root (:working-directory options)
                           :content (:content options)
                           :eligibility-coordinate coordinate})
        receipt (:receipt result)
        check (:eligibility-check result)
        observations (:observations result)
        pre-observations (:pre-discharge-observations result)
        transition (get-in receipt [:freeze :active-record-ref :transition])
        architecture-report-sha256
        (:architecture-report-sha256 transition)]
    (when (seq (:remaining-gaps result))
      (reject! :pre-discharge-reconstruction-unavailable))
    (when-not
     (and (exact-keys? result eligibility-result-keys)
          (exact-keys? receipt eligibility-receipt-keys)
          (not (contains? receipt :masked-root))
          (exact-keys? check eligibility-check-keys)
          (= coordinate (:coordinate result) (:coordinate check))
          (= 5 (:operation-count result))
          (dense-observations? 5 observations)
          (= observations (:observations check))
          (= 209 (:pre-discharge-git-operation-count result))
          (dense-observations? 209 pre-observations)
          (= pre-observations (:pre-discharge-observations check))
          (= eligibility-check-counts
             (select-keys check (keys eligibility-check-counts)))
          (every? true? (map check eligibility-check-matches))
          (= :immutable-q-coordinate-and-pre-discharge (:scope result))
          (= [] (:remaining-gaps result))
          (= :none (:authority result))
          (= (:eligibility-check-root result)
             ((:H codec)
              [:gravity/sh07-b51-eligibility-check-root-v1 check]))
          (sha256-id? architecture-report-sha256)
          (= (:content options) (:content receipt)))
      (reject! :eligibility-object-invalid))
    (assoc options
           :architecture-report-sha256 architecture-report-sha256
           :eligibility-preflight check
           :eligibility-check-root (:eligibility-check-root result))))

(defn- validate-child-eligibility!
  [joined codec tcb]
  (let [request (:request joined)
        expected-coordinate (:eligibility request)
        check (:eligibility-check tcb)]
    (when-not
     (and (= (:request-id joined)
             ((:H codec)
              [:gravity/sh07-b51-checker-request-id-v1 request]))
          (eligibility-check-valid? expected-coordinate check))
      (reject! :child-eligibility-check {:role (:role joined)}))
    check))
