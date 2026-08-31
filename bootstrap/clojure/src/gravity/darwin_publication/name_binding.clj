(ns gravity.darwin-publication.name-binding
  "Internal Darwin publication name binding operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.native-call :refer :all]
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

(defn staging-name-bound-to-descriptor?
  [state]
  (let [runtime (:runtime state)
        current
        (fstatat-result runtime (:parent-descriptor state)
                        (:staging-leaf state)
                        relative-unique-stat-flags)]
    (and (zero? (:value current))
         (same-identity? (:staging-stat state) (:stat current))
         (= s-ifdir (bit-and s-ifmt (get-in current [:stat :mode]))))))

(defn descriptor-paths-stable?
  [state]
  (let [runtime (:runtime state)
        parent-path
        (descriptor-path! runtime (:parent-descriptor state)
                          :revalidate-parent)
        staging-path
        (descriptor-path! runtime (:staging-descriptor state)
                          :revalidate-staging)]
    (and (= (:parent-path state) parent-path)
         (= (str parent-path "/" (:staging-leaf state)) staging-path))))

(defn destination-absent?
  [state]
  (let [result
        (fstatat-result (:runtime state) (:parent-descriptor state)
                        (:destination-leaf state)
                        relative-unique-stat-flags)]
    (cond
      (zero? (:value result)) false
      (= enoent (:errno result)) true
      :else
      (failure! :authenticate-destination
                :destination-absence-check-failed result))))
