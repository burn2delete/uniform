(ns gravity.c2-pass-cache.source-snapshot-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.c2-pass-cache :as cache])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path StandardOpenOption]))

(defn- delete-tree!
  [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (.toArray paths)))]
        (Files/deleteIfExists ^Path path)))))

(defn- with-temporary-directory
  [thunk]
  (let [directory (Files/createTempDirectory
                   "gravity-c2-source-snapshot-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (thunk directory)
      (finally
        (delete-tree! directory)))))

(deftest accepted-different-sources-are-hashed-once-each
  (with-temporary-directory (fn [directory]
    (let [source-a (.resolve directory "a.gravity")
          source-b (.resolve directory "b.gravity")
          bytes-a (.getBytes "first source\n" StandardCharsets/UTF_8)
          bytes-b (.getBytes "second source\n" StandardCharsets/UTF_8)
          digest-var (ns-resolve 'gravity.digest 'sha256-bytes-hex)
          digest (var-get digest-var)
          calls (atom [])]
      (Files/write source-a bytes-a
                   (into-array OpenOption [StandardOpenOption/CREATE_NEW
                                           StandardOpenOption/WRITE]))
      (Files/write source-b bytes-b
                   (into-array OpenOption [StandardOpenOption/CREATE_NEW
                                           StandardOpenOption/WRITE]))
      (with-redefs-fn
        {digest-var (fn [bytes]
                      (swap! calls conj (vec bytes))
                      (digest bytes))}
        #(let [snapshot-a (cache/source-snapshot! source-a)
               snapshot-b (cache/source-snapshot! source-b)]
           (is (= [(vec bytes-a) (vec bytes-b)] @calls))
           (is (= (str "sha256:" (digest bytes-a))
                  (:bytes-hash snapshot-a)))
           (is (= (str "sha256:" (digest bytes-b))
                  (:bytes-hash snapshot-b)))))))))

(deftest changed-second-read-bytes-are-rejected
  (with-temporary-directory (fn [directory]
    (let [source (.resolve directory "mutating.gravity")
          bytes (.getBytes "stable source\n" StandardCharsets/UTF_8)
          read-var (ns-resolve 'gravity.c2-pass-cache 'read-channel-exact!)
          read (var-get read-var)
          reads (atom 0)]
      (Files/write source bytes
                   (into-array OpenOption [StandardOpenOption/CREATE_NEW
                                           StandardOpenOption/WRITE]))
      (let [error
            (with-redefs-fn
              {read-var
               (fn [channel size diagnostic-id description]
                 (let [observed (read channel size diagnostic-id description)]
                   (if (= 2 (swap! reads inc))
                     (let [changed (aclone ^bytes observed)]
                       (aset-byte changed 0
                                  (unchecked-byte
                                   (bit-xor 1 (aget ^bytes changed 0))))
                       changed)
                     observed)))}
              (fn []
                (try
                  (cache/source-snapshot! source)
                  nil
                  (catch Throwable throwable
                    throwable))))]
        (is (= 2 @reads))
        (is (= "C16-KEY" (:id (ex-data error)))))))))
