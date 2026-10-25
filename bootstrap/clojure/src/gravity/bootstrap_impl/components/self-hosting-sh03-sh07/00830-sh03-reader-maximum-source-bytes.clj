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
             {:stage :read-source :source-span (source-span path 0)
              :reader-options standard-reader-options}
             {:facts {:failure-kind :source-file-attributes
                      :contained-host-error (.getName (class error))}
              :cause-message (.getMessage error)})))]
    (when-not (.isRegularFile attributes)
      (c2-reader-fail!
       "C2-ENCODING" path
       {:stage :read-source :source-span (source-span path 0)
        :reader-options standard-reader-options}
       {:missing-fields [:regular-source-file]}))
    (try
      (with-open [input
                  (java.nio.file.Files/newInputStream
                   nio-path
                   (into-array java.nio.file.OpenOption
                               [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
                  output (java.io.ByteArrayOutputStream.)]
        ;; transferTo batches I/O without imposing a project intake quota.
        (.transferTo input output)
        (.toByteArray output))
      (catch Exception error
        (c2-reader-fail!
         "C2-ENCODING" path
         {:stage :read-source :source-span (source-span path 0)
          :reader-options standard-reader-options}
         {:facts {:failure-kind :source-byte-read
                  :contained-host-error (.getName (class error))}
          :cause-message (.getMessage error)})))))
