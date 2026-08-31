(ns gravity.darwin-publication.inventory
  "Internal Darwin publication inventory operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.path :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
            [gravity.darwin-publication.stat :refer :all]
            [gravity.darwin-publication.file-io :refer :all])
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

(defn directory-inventory!
  ([runtime directory-descriptor]
   (directory-inventory! runtime directory-descriptor 16))
  ([runtime directory-descriptor maximum-entry-count]
   (when (.isVirtual (Thread/currentThread))
     (failure! :inventory :platform-thread-required))
   (let [duplicate
        ;; A new open file description avoids sharing the directory offset of
        ;; the held descriptor.  `openat(fd, ".", ...)` remains anchored to
        ;; the already-opened directory even if its external name changes.
        (open-relative! runtime directory-descriptor "."
                        relative-directory-open-flags 0
                        :inventory :directory-reopen-failed)
        duplicate-owned? (atom true)
        directory-stream (atom nil)
        primary (atom nil)]
    (try
      (let [opened
            (with-open [arena (Arena/ofConfined)]
              (address-call-result runtime arena :fdopendir [duplicate]))]
        (when (null-address? (:value opened))
          (close-fd-quietly! runtime duplicate)
          (reset! duplicate-owned? false)
          (failure! :inventory :fdopendir-failed opened))
        (reset! directory-stream (:value opened))
        (reset! duplicate-owned? false)
        (loop [names []]
          (when (> (count names) maximum-entry-count)
            (failure! :inventory :directory-inventory-limit
                      {:observed-file-count (count names)}))
          (let [error-pointer
                (.invokeWithArguments
                 (:error-pointer runtime) (object-array []))
                error-pointer
                (.reinterpret ^MemorySegment error-pointer (long 4))
                _ (.set error-pointer ValueLayout/JAVA_INT (long 0) (int 0))
                call
                (with-open [arena (Arena/ofConfined)]
                  (let [call (captured-call runtime arena :readdir
                                            [@directory-stream])
                        value (:value call)]
                    (if (null-address? value)
                      {:value value :errno (captured-errno runtime call)}
                      {:value value})))]
            (if (null-address? (:value call))
              (if (zero? (:errno call))
                (set names)
                (failure! :inventory :directory-read-failed call))
              (let [name (dirent-name! (:value call))]
                (recur
                 (if (contains? #{"." ".."} name)
                   names
                   (conj names name))))))))
      (catch Throwable error
        (reset! primary error)
        (rethrow-interrupt! error)
        (throw error))
      (finally
        (when @duplicate-owned?
          (close-fd-quietly! runtime duplicate))
        (when-let [stream @directory-stream]
          (let [result
                (try
                  (with-open [arena (Arena/ofConfined)]
                    (int-call-result runtime arena :closedir [stream]))
                  (catch Throwable error
                    {:close-error error}))]
            (cond
              (:close-error result)
              (if-let [error @primary]
                (.addSuppressed ^Throwable error ^Throwable (:close-error result))
                (throw ^Throwable (:close-error result)))

              (neg? (:value result))
              (let [close-error
                    (failure-ex :inventory :directory-stream-close-failed
                                result)]
                (if-let [error @primary]
                  (.addSuppressed ^Throwable error close-error)
                  (throw close-error)))))))))))
