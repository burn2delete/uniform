

(defn p15-s23-seed-readable-pr-str
  [source-path args]
  (binding [*p15-s23-seed-readable-owned-failures*
            (java.util.IdentityHashMap.)]
    (try
      (let [preflight
            (p15-s23-seed-readable-preflight! source-path args)
            snapshot-args (:snapshot-args preflight)
            output
            (str/join " "
                      (map #(p15-s23-seed-readable-value-text source-path %)
                           snapshot-args))
            output-bytes (p15-s23-seed-readable-utf8-bytes output)
            maximum-output-bytes
            (:maximum-output-bytes p15-s23-seed-readable-printer-limits)]
        (when (> output-bytes maximum-output-bytes)
          (p15-s23-seed-readable-printer-fail!
           source-path :output-byte-limit
           {:observed-output-bytes output-bytes
            :maximum-output-bytes maximum-output-bytes}))
        output)
      (catch clojure.lang.ExceptionInfo ex
        (if (.remove ^java.util.IdentityHashMap
                     *p15-s23-seed-readable-owned-failures* ex)
          (throw ex)
          (p15-s23-seed-readable-printer-fail!
           source-path :contained-seed-printer-failure {})))
      (catch InterruptedException _
        (.interrupt (Thread/currentThread))
        (p15-s23-seed-readable-printer-fail!
         source-path :contained-seed-printer-failure
         {:interrupt-restored? true}))
      (catch Exception _
        (p15-s23-seed-readable-printer-fail!
         source-path :contained-seed-printer-failure {}))
      (catch java.lang.ThreadDeath fatal
        (throw fatal))
      (catch VirtualMachineError fatal
        (throw fatal))
      (catch LinkageError fatal
        (throw fatal))
      (catch Throwable _
        (p15-s23-seed-readable-printer-fail!
         source-path :contained-seed-printer-failure {})))))

(defn p15-s23-seed-readable-normalized-rest
  [value]
  (let [tail (rest value)
        tail-class (class tail)]
    (if (or (identical? clojure.lang.PersistentList tail-class)
            (identical? clojure.lang.PersistentList$EmptyList tail-class))
      tail
      (apply list tail))))