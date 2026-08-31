(ns gravity.darwin-publication.specs
  "Internal Darwin publication specs operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.path :refer :all]
            [gravity.darwin-publication.stat :refer :all])
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

(defn normalized-file-specs
  [file-specs]
  (when-not (and (map? file-specs)
                 (= fixed-file-names (set (keys file-specs))))
    (failure! :validate-bundle :invalid-file-set
              {:expected-file-count 7
               :observed-file-count
               (when (map? file-specs) (count file-specs))}))
  (into
   (sorted-map)
   (map
    (fn [[logical-path spec]]
      (let [expected-mode
            (if (= "program" logical-path)
              executable-file-mode nonexecutable-file-mode)
            bytes (:bytes spec)
            mode (:mode spec)]
        (when-not
         (and (valid-leaf? logical-path)
              (bytes? bytes)
              (<= 0 (alength ^bytes bytes) maximum-file-bytes)
              (= expected-mode mode))
          (failure! :validate-bundle :invalid-file-specification
                    {:logical-path logical-path
                     :expected-mode expected-mode
                     :observed-mode mode
                     :observed-byte-count
                     (when (bytes? bytes) (alength ^bytes bytes))}))
        (let [private-bytes (aclone ^bytes bytes)]
          [logical-path
           {:bytes private-bytes
            :mode mode
            :byte-count (alength ^bytes private-bytes)
            :content-hash (sha256-bytes private-bytes)}]))))
   file-specs))

(defn random-staging-leaf
  []
  (let [bytes (byte-array 16)]
    (.nextBytes (SecureRandom.) bytes)
    (str ".gravity-c17-"
         (apply str
                (map #(format "%02x" (bit-and % 0xff)) bytes)))))
