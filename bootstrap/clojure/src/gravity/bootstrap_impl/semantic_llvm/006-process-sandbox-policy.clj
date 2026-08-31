(def p15-s23-b3-llvm-max-tool-output-bytes 65536)
(def p15-s23-b3-llvm-max-emitted-file-bytes (* 8 1024 1024))
(def p15-s23-b3-llvm-tool-timeout-ms 30000)

(def ^:private p15-s23-b3-llvm-developer-directory
  "/Library/Developer/CommandLineTools")

(def p15-s23-b3-llvm-environment-policy
  {:inherited-environment? false
   :fixed-values {"PATH" "/usr/bin:/bin:/usr/sbin:/sbin"
                  "LC_ALL" "C"
                  "LANG" "C"}
   :private-physical-values ["HOME" "TMPDIR"]
   :forbidden-prefixes ["DYLD_" "CCC_" "LLVM_"]
   :forbidden-names ["SDKROOT" "MACOSX_DEPLOYMENT_TARGET" "CPATH"
                     "LIBRARY_PATH"]})

(defn- p15-s23-b3-llvm-sha256-bytes
  [bytes]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest bytes)
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and % 0xff))
                         (.digest digest))))))

(defn- p15-s23-b3-llvm-read-bounded-stream
  [candidate stream source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :read-bounded-tool-stream)
  (with-open [input stream
              output (java.io.ByteArrayOutputStream.)]
    (let [buffer (byte-array 4096)
          digest (java.security.MessageDigest/getInstance "SHA-256")]
      (loop [total 0
             retained 0
             truncated? false]
        (let [read (.read input buffer)]
          (if (neg? read)
            (let [bytes (.toByteArray output)]
              {:bytes bytes
               :text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
               :stream-read-complete? true
               :total-byte-count total
               :retained-byte-count retained
               :truncated? truncated?
               :hash
               (str "sha256:"
                    (apply str
                           (map #(format "%02x" (bit-and % 0xff))
                                (.digest digest))))})
            (let [remaining (- p15-s23-b3-llvm-max-tool-output-bytes
                               retained)
                  keep-count (max 0 (min remaining read))
                  next-total (+ total read)]
              (.update digest buffer 0 keep-count)
              (when (pos? keep-count)
                (.write output buffer 0 keep-count))
              (if (> next-total p15-s23-b3-llvm-max-tool-output-bytes)
                (let [bytes (.toByteArray output)]
                  {:bytes bytes
                   :text
                   (String. bytes java.nio.charset.StandardCharsets/UTF_8)
                   :stream-read-complete? false
                   :limit-exceeded? true
                   :total-byte-count next-total
                   :retained-byte-count (+ retained keep-count)
                   :truncated? true
                   :hash
                   (str "sha256:"
                        (apply str
                               (map #(format "%02x" (bit-and % 0xff))
                                    (.digest digest))))})
                (recur next-total
                       (+ retained keep-count)
                       (or truncated? (< keep-count read)))))))))))

(defn- p15-s23-b3-llvm-destroy-process-tree!
  [candidate process source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :destroy-process-tree)
  (let [root (.toHandle process)
        descendants
        (with-open [stream (.descendants root)]
          (vec (iterator-seq
                (.iterator (.limit stream (long 65))))))]
    (when (> (count descendants) 64)
      (.destroyForcibly root)
      (doseq [handle descendants]
        (try (.destroyForcibly ^java.lang.ProcessHandle handle)
             (catch Exception _ nil)))
      (p15-s23-b3-llvm-fail!
       "B3-TARGET" source-path {}
       {:missing-fact :bounded-tool-descendant-count}))
    ;; Remove the root's ability to create more descendants first, then stop
    ;; every captured child.  Recheck the entire bounded set before returning.
    (let [root-requested?
          (try (.destroyForcibly root) (catch Exception _ false))
          descendant-requests
          (mapv (fn [handle]
                  (try (.destroyForcibly ^java.lang.ProcessHandle handle)
                       (catch Exception _ false)))
                descendants)
          deadline (+ (System/nanoTime) 2000000000)]
      (loop []
        (let [root-alive? (.isAlive root)
              alive-descendants
              (count (filter #(.isAlive ^java.lang.ProcessHandle %)
                             descendants))]
          (if (and (or root-alive? (pos? alive-descendants))
                   (< (System/nanoTime) deadline))
            (do (Thread/sleep 10) (recur))
            {:kill-requested? true
             :root-kill-requested? (boolean root-requested?)
             :descendant-count (count descendants)
             :descendant-kill-request-count
             (count (filter true? descendant-requests))
             :root-alive-after-kill? root-alive?
             :descendants-alive-after-kill alive-descendants
             :captured-process-set-reaped?
             (and (not root-alive?) (zero? alive-descendants))}))))))
