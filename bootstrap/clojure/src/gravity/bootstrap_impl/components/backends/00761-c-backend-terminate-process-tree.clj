

(defn- c-backend-terminate-process-tree!
  [process source-path target]
  (let [root (.toHandle process)
        captured (atom {})
        observation-overflow? (atom false)
        deadline (+ (System/nanoTime) 2000000000)]
    (loop []
      (let [snapshot
            (try
              (c-backend-process-descendants process)
              (catch InterruptedException interrupted
                (.interrupt (Thread/currentThread))
                (throw interrupted)))
            merge-result
            (c-backend-merge-census-handles
             @captured (:handles snapshot) *c-backend-process-max-descendants*)
            _ (when (or (:overflow? snapshot) (:overflow? merge-result))
                (reset! observation-overflow? true))
            _ (when-not @observation-overflow?
                (reset! captured (:captured merge-result)))]
        (doseq [handle (cons root (vals @captured))]
          (try
            (.destroyForcibly ^java.lang.ProcessHandle handle)
            (catch Exception _ nil)))
        ;; A containment diagnostic must never preempt the kill requests for
        ;; handles that were already captured.  More descendants may exist
        ;; beyond the bounded snapshot, so this remains a fail-closed error
        ;; rather than process-tree proof.
        (when @observation-overflow?
          (c-backend-fail!
           "B2-DIALECT" "C backend process descendant set exceeded its bound"
           source-path target nil
           {:missing-fact :bounded-c-backend-process-descendants
            :maximum-descendants *c-backend-process-max-descendants*
            :observed-descendants (count @captured)
            :captured-kill-requested? true
            :whole-process-tree-reaping-proved? false})))
        (let [alive
              (filter #(.isAlive ^java.lang.ProcessHandle %)
                      (cons root (vals @captured)))]
          (if (and (seq alive) (< (System/nanoTime) deadline))
            (do
              (try
                (Thread/sleep 10)
                (catch InterruptedException interrupted
                  (.interrupt (Thread/currentThread))
                  (throw interrupted)))
              (recur))
            (let [final-descendants (c-backend-process-descendants process)
                  final-merge
                  (c-backend-merge-census-handles
                   @captured (:handles final-descendants)
                   *c-backend-process-max-descendants*)]
              ;; The final snapshot is still a consumer of the global bound.
              ;; Fail before retaining or concatenating a new identity; doing
              ;; otherwise would briefly construct an over-cap kill set.
              (when (or (:overflow? final-descendants)
                        (:overflow? final-merge))
                (c-backend-fail!
                 "B2-DIALECT"
                 "C backend process descendant set exceeded its bound"
                 source-path target nil
                 {:missing-fact :bounded-c-backend-process-descendants
                  :maximum-descendants *c-backend-process-max-descendants*
                  :observed-descendants (count @captured)
                  :final-snapshot-overflow? true
                  :captured-kill-requested? true
                  :whole-process-tree-reaping-proved? false}))
              (reset! captured (:captured final-merge))
              (let [final-handles
                    (vals
                     (into
                      {}
                      (map (fn [handle]
                             [(.pid ^java.lang.ProcessHandle handle) handle]))
                      (concat (vals @captured) [root])))
                    final-alive
                    (filter #(.isAlive ^java.lang.ProcessHandle %)
                            final-handles)
                    result
                    {:kill-requested? true
                     :captured-process-count (inc (count @captured))
                     :descendant-count (count @captured)
                     :alive-process-count (count final-alive)
                     :root-alive-after-kill? (.isAlive root)
                     :captured-process-set-reaped? (empty? final-alive)
                     ;; ProcessHandle.descendants is only a snapshot.  It cannot
                     ;; prove that a descendant did not reparent or fork between
                     ;; enumeration and termination; only an OS containment
                     ;; primitive such as a process group/job can prove that.
                     :os-process-containment? false
                     :whole-process-tree-reaping-proved? false}]
                (when-not (:captured-process-set-reaped? result)
                  (c-backend-fail!
                   "B2-DIALECT"
                   "C backend process tree could not be reaped fail-closed"
                   source-path target nil
                   {:missing-fact :c-backend-process-tree-reaping
                    :termination result}))
                result)))))))

(defn- c-backend-process-read-stream
  "Drain INPUT completely while retaining a strict UTF-8 prefix.

  Every wire byte is hashed and consumed, including bytes beyond the bounded
  evidence prefix.  UTF-8 decoding uses REPORT mode; malformed input is
  remembered while the drain continues so a child cannot remain blocked on a
  full pipe merely because its output was invalid."
  [^java.io.InputStream input capture-limit-bytes]
  (with-open [input input]
    (let [limit (long capture-limit-bytes)
          _ (when (neg? limit)
              (throw (ex-info "negative native process capture limit"
                              {:limit limit})))
          digest (java.security.MessageDigest/getInstance "SHA-256")
          decoder (-> java.nio.charset.StandardCharsets/UTF_8
                      (.newDecoder)
                      (.onMalformedInput
                       java.nio.charset.CodingErrorAction/REPORT)
                      (.onUnmappableCharacter
                       java.nio.charset.CodingErrorAction/REPORT))
          retained (StringBuilder.)
          retained-bytes (atom 0)
          retained-complete? (atom true)
          append-output!
          (fn [^java.nio.CharBuffer output]
            (.flip output)
            (let [chunk (.toString output)]
              (loop [index 0]
                (when (and @retained-complete?
                           (< index (.length chunk)))
                  (let [code-point (Character/codePointAt chunk index)
                        code-point-text (String. (Character/toChars code-point))
                        code-point-bytes
                        (.getBytes code-point-text
                                   java.nio.charset.StandardCharsets/UTF_8)
                        next-bytes (+ @retained-bytes
                                      (alength code-point-bytes))]
                    (if (<= next-bytes limit)
                      (do
                        (.append retained code-point-text)
                        (reset! retained-bytes next-bytes)
                        (recur (+ index (Character/charCount code-point))))
                      ;; Do not retain a partial code point at the cap.  The
                      ;; decoder still consumes it and subsequent bytes.
                      (reset! retained-complete? false))))))
            (.clear output))
          decode-chunk!
          (fn [^bytes bytes end-of-input?]
            (let [input-buffer (java.nio.ByteBuffer/wrap bytes)]
              (loop []
                (let [output (java.nio.CharBuffer/allocate 4096)
                      result (.decode decoder input-buffer output end-of-input?)]
                  (append-output! output)
                  (cond
                    (.isError result) (.throwException result)
                    (.isOverflow result) (recur)
                    :else
                    (let [remaining (.remaining input-buffer)
                          carry (byte-array remaining)]
                      (when (pos? remaining)
                        (.get input-buffer carry))
                      carry))))))
          flush-decoder!
          (fn []
            (loop []
              (let [output (java.nio.CharBuffer/allocate 4096)
                    result (.flush decoder output)]
                (append-output! output)
                (cond
                  (.isError result) (.throwException result)
                  (.isOverflow result) (recur)
                  :else nil))))
          buffer (byte-array 8192)]
      (loop [total 0
             carry (byte-array 0)
             decode-error nil]
        (let [read-count (.read input buffer)]
          (if (neg? read-count)
            (let [decode-error
                  (if decode-error
                    decode-error
                    (try
                      (let [final-carry (decode-chunk! carry true)]
                        (when (pos? (alength final-carry))
                          ;; An incomplete sequence at EOF must take the
                          ;; decoder's REPORT error path.
                          (decode-chunk! final-carry true))
                        (flush-decoder!)
                        nil)
                      (catch java.nio.charset.CharacterCodingException error
                        error)))]
              {:text (.toString retained)
               :byte-count total
               :total-byte-count total
               :retained-byte-count @retained-bytes
               :truncated? (or (> total limit)
                               (not @retained-complete?))
               :limit-exceeded? (> total limit)
               :stream-read-complete? true
               :decode-error decode-error
               :hash (str "sha256:"
                          (apply str
                                 (map #(format "%02x" (bit-and % 0xff))
                                      (.digest digest))))})
            (let [next-total (+ total read-count)
                  combined (byte-array (+ (alength carry) read-count))
                  _ (when (pos? (alength carry))
                      (System/arraycopy carry 0 combined 0 (alength carry)))
                  _ (System/arraycopy buffer 0 combined (alength carry)
                                       read-count)
                  ;; Hash the original wire bytes, not the retained prefix.
                  _ (.update digest buffer 0 read-count)
                  decode-state
                  (if decode-error
                    {:carry carry :error decode-error}
                    (try
                      {:carry (decode-chunk! combined false)
                       :error nil}
                      (catch java.nio.charset.CharacterCodingException error
                        ;; Stop decoding after the first malformed sequence,
                        ;; but continue consuming and hashing the wire stream.
                        {:carry (byte-array 0) :error error})))]
              (recur next-total (:carry decode-state) (:error decode-state)))))))))

(def ^:dynamic *c-backend-process-read-stream-fn*
  c-backend-process-read-stream)