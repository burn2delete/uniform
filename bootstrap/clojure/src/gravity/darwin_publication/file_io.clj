(ns gravity.darwin-publication.file-io
  "Internal Darwin publication file io operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.path :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
            [gravity.darwin-publication.stat :refer :all]
            [gravity.darwin-publication.specs :refer :all])
  (:import [java.lang.foreign Arena FunctionDescriptor Linker
            Linker$Option MemoryLayout MemoryLayout$PathElement MemorySegment
            ValueLayout]
           [java.lang.invoke VarHandle$AccessMode]
           [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file InvalidPathException Paths]
           [java.security MessageDigest SecureRandom]
           [java.util Collections WeakHashMap]))

(defn mkdir-relative!
  [runtime parent-descriptor]
  (loop [attempt 0]
    (when (>= attempt 8)
      (failure! :create-staging :bounded-staging-name-exhausted))
    (let [leaf (random-staging-leaf)
          result
          (with-open [arena (Arena/ofConfined)]
            (let [name (.allocateFrom arena ^String leaf)]
              (int-call-result runtime arena :mkdirat
                               [parent-descriptor name
                                (short owner-only-directory-mode)])))]
      (cond
        (zero? (:value result)) leaf
        (= eexist (:errno result)) (recur (inc attempt))
        :else
        (failure! :create-staging :staging-directory-create-failed
                  result)))))

(defn write-all!
  [runtime file-descriptor bytes logical-path]
  (when (pos? (alength ^bytes bytes))
    (with-open [arena (Arena/ofConfined)]
      (let [total (alength ^bytes bytes)
            source (.allocate arena (long total) (long 1))
            _ (.copyFrom source (MemorySegment/ofArray ^bytes bytes))]
        (loop [offset 0]
          (when (< offset total)
            (let [remaining (- total offset)
                  segment (.asSlice source (long offset) (long remaining))
                  result
                  (long-call-result runtime arena :write
                                    [file-descriptor segment (long remaining)])
                  written (:value result)]
              (cond
                (and (neg? written) (= eintr (:errno result)))
                (recur offset)

                (neg? written)
                (failure! :write-file :file-write-failed
                          (assoc result :logical-path logical-path))

                (zero? written)
                (failure! :write-file :zero-progress-file-write
                          {:logical-path logical-path
                           :expected-byte-count total
                           :observed-byte-count offset})

                :else (recur (+ offset written)))))))))
  nil)

(defn pread-exact!
  [runtime file-descriptor byte-count logical-path]
  (if (zero? byte-count)
    (byte-array 0)
    (with-open [arena (Arena/ofConfined)]
      (let [target (.allocate arena (long byte-count) (long 1))]
        (loop [offset 0]
          (if (= offset byte-count)
            (let [buffer (.asByteBuffer target)
                  bytes (byte-array byte-count)]
              (.get ^ByteBuffer buffer bytes)
              bytes)
            (let [remaining (- byte-count offset)
                  segment (.asSlice target (long offset) (long remaining))
                  result
                  (long-call-result runtime arena :pread
                                    [file-descriptor segment (long remaining)
                                     (long offset)])
                  read-count (:value result)]
              (cond
                (and (neg? read-count) (= eintr (:errno result)))
                (recur offset)

                (neg? read-count)
                (failure! :read-file :file-readback-failed
                          (assoc result :logical-path logical-path))

                (zero? read-count)
                (failure! :read-file :short-file-readback
                          {:logical-path logical-path
                           :expected-byte-count byte-count
                           :observed-byte-count offset})

                :else (recur (+ offset read-count))))))))))

(defn chmod-fd!
  [runtime file-descriptor mode operation]
  (with-open [arena (Arena/ofConfined)]
    (int-call! runtime arena :fchmod
               [file-descriptor (short mode)] :descriptor-chmod-failed))
  nil)

(defn fsync-fd!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (int-call! runtime arena :fsync
               [file-descriptor] :descriptor-sync-failed))
  nil)

(defn verify-relative-file!
  [runtime staging-descriptor effective-user logical-path expected]
  (let [descriptor
        (open-relative! runtime staging-descriptor logical-path
                        unique-file-read-flags 0
                        :open-file :unique-file-open-failed)]
    (try
      (let [stat (fstat! runtime descriptor :verify-file)
            _ (assert-no-extended-acl! runtime descriptor :verify-file)
            bytes (pread-exact! runtime descriptor
                                (:byte-count expected) logical-path)]
        (when-not
         (and (regular-stat-valid?
               stat effective-user (:mode expected) (:byte-count expected))
              (= (:content-hash expected) (sha256-bytes bytes))
              (java.util.Arrays/equals
               ^bytes (:bytes expected) ^bytes bytes))
          (failure! :verify-file :file-content-or-metadata-mismatch
                    {:logical-path logical-path
                     :expected-byte-count (:byte-count expected)
                     :observed-byte-count (:byte-count stat)
                     :expected-mode (:mode expected)
                     :observed-mode (bit-and 0x0fff (:mode stat))}))
        {:byte-count (:byte-count expected)
         :content-hash (:content-hash expected)
         :mode (:mode expected)
         :identity-hash (identity-hash stat)})
      (finally
        (close-fd! runtime descriptor :verify-file)))))

(defn create-relative-file!
  [runtime staging-descriptor effective-user logical-path expected]
  (let [descriptor
        (open-relative! runtime staging-descriptor logical-path
                        exclusive-file-open-flags 0600
                        :create-file :exclusive-file-create-failed)]
    (try
      (write-all! runtime descriptor (:bytes expected) logical-path)
      (chmod-fd! runtime descriptor (:mode expected) :finalize-file-mode)
      (assert-no-extended-acl! runtime descriptor :finalize-file-mode)
      (fsync-fd! runtime descriptor :sync-file)
      (let [stat (fstat! runtime descriptor :verify-created-file)]
        (when-not
         (regular-stat-valid?
          stat effective-user (:mode expected) (:byte-count expected))
          (failure! :verify-created-file :created-file-metadata-mismatch
                    {:logical-path logical-path
                     :expected-byte-count (:byte-count expected)
                     :observed-byte-count (:byte-count stat)
                     :expected-mode (:mode expected)
                     :observed-mode (bit-and 0x0fff (:mode stat))})))
      (finally
        (close-fd! runtime descriptor :create-file))))
  (verify-relative-file!
   runtime staging-descriptor effective-user logical-path expected))

(defn strict-utf8
  [bytes operation]
  (try
    (let [decoder
          (doto (.newDecoder StandardCharsets/UTF_8)
            (.onMalformedInput CodingErrorAction/REPORT)
            (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (str (.decode decoder (ByteBuffer/wrap ^bytes bytes))))
    (catch CharacterCodingException _
      (failure! operation :invalid-utf8-directory-entry))))

(defn dirent-name!
  [entry]
  (let [header (.reinterpret ^MemorySegment entry (long 21))
        record-byte-count
        (bit-and 0xffff
                 (int (.get header ValueLayout/JAVA_SHORT (long 16))))
        name-byte-count
        (bit-and 0xffff
                 (int (.get header ValueLayout/JAVA_SHORT (long 18))))]
    (when-not
     (and (<= 22 record-byte-count dirent-maximum-byte-count)
          (<= 0 name-byte-count maximum-leaf-bytes)
          (<= (+ 21 name-byte-count 1) record-byte-count))
      (failure! :inventory :invalid-directory-entry-layout))
    (let [record (.reinterpret ^MemorySegment entry (long record-byte-count))
          terminator
          (int (.get record ValueLayout/JAVA_BYTE
                     (long (+ 21 name-byte-count))))
          bytes (byte-array name-byte-count)]
      (when-not (zero? terminator)
        (failure! :inventory :unterminated-directory-entry))
      (dotimes [index name-byte-count]
        (aset-byte bytes index
                   (.get record ValueLayout/JAVA_BYTE
                         (long (+ 21 index)))))
      (strict-utf8 bytes :inventory))))
