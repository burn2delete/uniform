(ns gravity.darwin-publication.native-call
  "Internal Darwin publication native call operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all])
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

(defn ^:dynamic captured-call
  [runtime arena operation arguments]
  (let [state (.allocate ^Arena arena (:state-layout runtime))
        value
        (.invokeWithArguments
         (get runtime operation)
         (object-array (into [state] arguments)))]
    {:state state :value value}))

(defn captured-errno
  [runtime call]
  (int
   (.invokeWithArguments
    (:errno-handle runtime)
    (object-array [(:state call) (long 0)]))))

(defn null-address?
  [value]
  (or (nil? value)
      (and (instance? MemorySegment value)
           (zero? (.address ^MemorySegment value)))))

(defn int-call!
  [runtime arena operation arguments failure-reason]
  (loop []
    (let [call (captured-call runtime arena operation arguments)
          value (int (:value call))]
      (if (neg? value)
        (let [errno (captured-errno runtime call)]
          (if (= eintr errno)
            (recur)
            (failure! operation failure-reason
                      {:return-code value :errno errno})))
        value))))

(defn ^:dynamic int-call-result
  [runtime arena operation arguments]
  (let [call (captured-call runtime arena operation arguments)
        value (int (:value call))]
    (if (neg? value)
      {:value value :errno (captured-errno runtime call)}
      {:value value})))

(defn ^:dynamic long-call-result
  [runtime arena operation arguments]
  (let [call (captured-call runtime arena operation arguments)
        value (long (:value call))]
    (if (neg? value)
      {:value value :errno (captured-errno runtime call)}
      {:value value})))

(defn address-call-result
  [runtime arena operation arguments]
  (let [call (captured-call runtime arena operation arguments)
        value (:value call)]
    (if (null-address? value)
      {:value value :errno (captured-errno runtime call)}
      {:value value})))

(defn close-fd!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (let [{:keys [value errno]}
          (int-call-result runtime arena :close [file-descriptor])]
      (when (neg? value)
        ;; POSIX leaves descriptor state unspecified after EINTR.  Never retry.
        (failure! operation :descriptor-close-failed
                  {:return-code value :errno errno}))))
  nil)

(defn close-fd-quietly!
  [runtime file-descriptor]
  (when (and runtime (integer? file-descriptor) (not (neg? file-descriptor)))
    (try
      (with-open [arena (Arena/ofConfined)]
        (int-call-result runtime arena :close [file-descriptor]))
      (catch Throwable _ nil)))
  nil)
