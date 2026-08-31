

(defn- p18-t04-verified-mir-c-source-native-read-bytes!
  [binding arena state source-path file-descriptor initial-byte-count]
  (let [capacity (inc initial-byte-count)
        buffer (.allocate arena (long capacity) (long 1))]
    (loop [offset 0]
      (let [remaining (- capacity offset)
            read-count
            (p18-t04-verified-mir-c-source-native-read-call
             binding state file-descriptor
             (.asSlice buffer (long offset) (long remaining)) remaining)]
        (cond
          (neg? read-count)
          (p18-t04-verified-mir-c-source-snapshot-fail!
           source-path :stable-bounded-source-snapshot
           :opened-source-read-failed
           {:captured-errno
            (p18-t04-verified-mir-c-source-native-errno binding state)})

          (zero? read-count)
          (do
            (when-not (= offset initial-byte-count)
              (p18-t04-verified-mir-c-source-snapshot-fail!
               source-path :stable-bounded-source-snapshot
               :opened-source-size-changed-during-snapshot
               {:initial-byte-count initial-byte-count
                :observed-byte-count offset}))
            (.toArray (.asSlice buffer (long 0) (long offset))
                      java.lang.foreign.ValueLayout/JAVA_BYTE))

          (> (+ offset read-count) initial-byte-count)
          (p18-t04-verified-mir-c-source-snapshot-fail!
           source-path :stable-bounded-source-snapshot
           :opened-source-size-grew-during-snapshot
           {:maximum-byte-count
            p18-t04-verified-mir-c-maximum-source-bytes
            :initial-byte-count initial-byte-count
            :observed-byte-count (+ offset read-count)})

          :else
          (recur (+ offset (int read-count))))))))

(defn- p18-t04-verified-mir-c-source-native-close
  [binding state file-descriptor]
  (p18-t04-verified-mir-c-source-native-int-call
   binding :close state [file-descriptor]))

(defn- p18-t04-verified-mir-c-with-source-descriptor!
  [binding arena state source-path actual-path f]
  (let [file-descriptor
        (p18-t04-verified-mir-c-source-native-open!
         binding arena state source-path actual-path)
        close-attempted? (atom false)]
    (try
      (let [result (f file-descriptor)
            _ (reset! close-attempted? true)
            close-code
            (p18-t04-verified-mir-c-source-native-close
             binding state file-descriptor)]
        (when-not (zero? close-code)
          (p18-t04-verified-mir-c-source-snapshot-fail!
           source-path :descriptor-bound-source-snapshot
           :opened-source-close-failed
           {:captured-errno
            (p18-t04-verified-mir-c-source-native-errno binding state)}))
        result)
      (finally
        (when-not @close-attempted?
          (try
            (p18-t04-verified-mir-c-source-native-close
             binding state file-descriptor)
            (catch Throwable _ nil)))))))