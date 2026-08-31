

(defn p18-shell-run
  ([args]
   (p18-shell-run nil args))
  ([env args]
   (let [process-builder (ProcessBuilder.
                          ^java.util.List
                          (into ["/bin/bash" "-c" "\"$@\"" "gravity-p18-shell"]
                                (mapv str args)))
         process-env (.environment process-builder)
         stdout-file (java.io.File/createTempFile "gravity-p18-stdout-" ".txt")
         stderr-file (java.io.File/createTempFile "gravity-p18-stderr-" ".txt")
         timeout-ms 60000
         wait-for-exit
         (fn [process timeout-ms]
           (.waitFor process timeout-ms
                     java.util.concurrent.TimeUnit/MILLISECONDS))
         read-output
         (fn [file]
           (if (.exists file)
             (slurp file)
             ""))]
     (when env
       (doseq [[k v] env]
         (.put process-env (str k) (str v))))
     (try
       (.redirectOutput process-builder stdout-file)
       (.redirectError process-builder stderr-file)
       (let [process (.start process-builder)]
         (if (wait-for-exit process timeout-ms)
           {:exit (.exitValue process)
            :out (read-output stdout-file)
            :err (read-output stderr-file)}
           (do
             (.destroyForcibly process)
             (wait-for-exit process 5000)
             {:exit 124
              :out (read-output stdout-file)
              :err (str (read-output stderr-file)
                        "\nprocess timed out after "
                        timeout-ms
                        "ms: "
                        (str/join " " args))})))
       (finally
         (.delete stdout-file)
         (.delete stderr-file))))))