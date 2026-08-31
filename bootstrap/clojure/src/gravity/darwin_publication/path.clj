(ns gravity.darwin-publication.path
  "Internal Darwin publication path operations."
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

(defn utf8-byte-count
  [text]
  (when (string? text)
    (alength (.getBytes ^String text StandardCharsets/UTF_8))))

(defn valid-leaf?
  [leaf]
  (let [byte-count (utf8-byte-count leaf)]
    (and (string? leaf)
         (not (str/blank? leaf))
         (not (contains? #{"." ".."} leaf))
         (not (str/includes? leaf "/"))
         (not (str/includes? leaf "\u0000"))
         (not-any? #(Character/isISOControl ^char %) leaf)
         (integer? byte-count)
         (<= 1 byte-count maximum-leaf-bytes))))

(defn output-location
  [output-directory]
  (let [byte-count (utf8-byte-count output-directory)
        parsed
        (when (and (string? output-directory)
                   (integer? byte-count)
                   (<= 1 byte-count maximum-path-bytes)
                   (not (str/includes? output-directory "\u0000"))
                   (not-any? #(Character/isISOControl ^char %)
                             output-directory))
          (try
            (Paths/get output-directory (make-array String 0))
            (catch InvalidPathException _ nil)))
        absolute (when parsed (.normalize (.toAbsolutePath parsed)))
        parent (when absolute (.getParent absolute))
        leaf (when absolute (some-> absolute .getFileName str))]
    (when-not (and parsed absolute parent (valid-leaf? leaf)
                   (= absolute (.normalize absolute)))
      (failure! :validate-output :invalid-output-location))
    {:requested-output output-directory
     :destination-path (.toString absolute)
     :parent-path (.toString parent)
     :destination-leaf leaf}))
