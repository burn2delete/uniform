

(def sh03-reader-maximum-source-bytes 1048576)

(defn sh03-reader-read-target-source-bytes!
  [path]
  (let [nio-path (.toPath (java.io.File. path))
        nofollow (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           nio-path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (c2-reader-fail!
             "C2-ENCODING" path
             {:stage :read-source
              :source-span (source-span path 0)
              :reader-options standard-reader-options}
             {:facts {:failure-kind :source-file-attributes
                      :contained-host-error (.getName (class error))}
              :cause-message (.getMessage error)})))]
    (when-not (and attributes
                   (.isRegularFile attributes)
                   (<= (.size attributes)
                       sh03-reader-maximum-source-bytes))
      (c2-reader-fail!
       "C2-HASH" path
       {:stage :read-source
        :source-span (source-span path 0)
        :reader-options standard-reader-options}
       {:missing-fields [:bounded-regular-source-file]
        :facts {:regular-file? (boolean (and attributes
                                             (.isRegularFile attributes)))
                :observed-source-byte-count
                (when attributes (.size attributes))
                :maximum-source-bytes sh03-reader-maximum-source-bytes}}))
    (let [limit (inc sh03-reader-maximum-source-bytes)
          buffer (byte-array limit)
          observed
          (try
            (with-open [input
                        (java.nio.file.Files/newInputStream
                         nio-path
                         (into-array java.nio.file.OpenOption
                                     [java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
              (loop [offset 0]
                (if (= offset limit)
                  offset
                  (let [count (.read input buffer offset (- limit offset))]
                    (if (= -1 count)
                      offset
                      (recur (+ offset count)))))))
            (catch Exception error
              (c2-reader-fail!
               "C2-ENCODING" path
               {:stage :read-source
                :source-span (source-span path 0)
                :reader-options standard-reader-options}
               {:facts {:failure-kind :bounded-source-byte-read
                        :contained-host-error (.getName (class error))}
                :cause-message (.getMessage error)})))]
      (when (> observed sh03-reader-maximum-source-bytes)
        (c2-reader-fail!
         "C2-HASH" path
         {:stage :read-source
          :source-span (source-span path 0)
          :reader-options standard-reader-options}
         {:missing-fields [:bounded-source-byte-snapshot]
          :facts {:observed-source-byte-count observed
                  :maximum-source-bytes sh03-reader-maximum-source-bytes}}))
      (java.util.Arrays/copyOf buffer observed))))