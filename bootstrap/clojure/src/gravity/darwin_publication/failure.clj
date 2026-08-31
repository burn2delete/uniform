(ns gravity.darwin-publication.failure
  "Internal Darwin publication failure operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all])
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

(defn failure-ex
  [operation reason data]
  (ex-info
   "Darwin descriptor publication failed"
   (merge
    {:gravity.darwin-publication/error true
     :provider :gravity/darwin-descriptor-publication
     :provider-version provider-version
     :operation operation
     :reason reason}
    (select-keys data
                 [:errno :return-code :logical-path :expected-file-count
                  :observed-file-count :expected-byte-count
                  :observed-byte-count :output-collision?
                  :native-access-enabled? :missing-symbol
                  :expected-mode :observed-mode
                  :cleanup-complete? :residue-possible?]))))

(defn failure!
  ([operation reason]
   (failure! operation reason {}))
  ([operation reason data]
   (throw (failure-ex operation reason data))))

(defn interrupt-like?
  [error]
  (or (instance? InterruptedException error)
      (instance? java.nio.channels.ClosedByInterruptException error)
      (instance? java.io.InterruptedIOException error)))

(defn rethrow-interrupt!
  [error]
  (when (interrupt-like? error)
    (.interrupt (Thread/currentThread))
    (throw error))
  error)
