(ns gravity.darwin-publication.stat
  "Internal Darwin publication stat operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all]
            [gravity.darwin-publication.native-call :refer :all])
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

(defn sha256-bytes
  [bytes]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.update digest ^bytes bytes)
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and % 0xff))
                     (.digest digest))))))

(defn sha256-text
  [text]
  (sha256-bytes (.getBytes (str text) StandardCharsets/UTF_8)))

(defn stat-record
  [segment]
  (let [mode (bit-and 0xffff
                      (int (.get ^MemorySegment segment
                                 ValueLayout/JAVA_SHORT (long 4))))
        link-count (bit-and 0xffff
                            (int (.get ^MemorySegment segment
                                       ValueLayout/JAVA_SHORT (long 6))))]
    {:device (int (.get ^MemorySegment segment ValueLayout/JAVA_INT (long 0)))
     :mode mode
     :link-count link-count
     :inode (long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 8)))
     :uid (Integer/toUnsignedLong
           (int (.get ^MemorySegment segment ValueLayout/JAVA_INT (long 16))))
     :gid (Integer/toUnsignedLong
           (int (.get ^MemorySegment segment ValueLayout/JAVA_INT (long 20))))
     :modified-time
     [(long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 48)))
      (long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 56)))]
     :changed-time
     [(long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 64)))
      (long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 72)))]
     :birth-time
     [(long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 80)))
      (long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 88)))]
     :byte-count
     (long (.get ^MemorySegment segment ValueLayout/JAVA_LONG (long 96)))}))

(defn fstat!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (let [buffer (.allocate arena (long stat-byte-count) (long 8))]
      (int-call! runtime arena :fstat [file-descriptor buffer]
                 :descriptor-stat-failed)
      (stat-record buffer))))

(defn fstatat-result
  [runtime directory-descriptor leaf flags]
  (with-open [arena (Arena/ofConfined)]
    (let [name (.allocateFrom arena ^String leaf)
          buffer (.allocate arena (long stat-byte-count) (long 8))
          result
          (int-call-result runtime arena :fstatat
                           [directory-descriptor name buffer (int flags)])]
      (if (neg? (:value result))
        result
        (assoc result :stat (stat-record buffer))))))

(defn descriptor-path!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (let [buffer (.allocate arena (long path-buffer-byte-count) (long 1))]
      (int-call! runtime arena :fcntl-address
                 [file-descriptor (int f-getpath) buffer]
                 :descriptor-path-failed)
      (.getString buffer (long 0) StandardCharsets/UTF_8))))

(defn effective-user-id!
  [runtime]
  (with-open [arena (Arena/ofConfined)]
    (let [call (captured-call runtime arena :geteuid [])]
      (Integer/toUnsignedLong (int (:value call))))))

(defn assert-no-extended-acl!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (let [result
          (address-call-result runtime arena :acl-get-fd-np
                               [file-descriptor (int acl-type-extended)])]
      (if (null-address? (:value result))
        (when-not (= enoent (:errno result))
          (failure! operation :access-control-list-inspection-failed result))
        (let [rejection
              (failure-ex operation
                          :nontrivial-extended-access-control-list {})
              released
              (int-call-result runtime arena :acl-free [(:value result)])]
          (when (neg? (:value released))
            (.addSuppressed
             ^Throwable rejection
             (failure-ex operation :access-control-list-release-failed
                         released)))
          (throw rejection)))))
  nil)

(defn identity-record
  [stat]
  ;; Directory link counts change when staging and destination entries are
  ;; added or removed; they are validity facts, not stable identity inputs.
  (select-keys stat [:device :inode :mode :uid]))

(defn identity-hash
  [stat]
  (sha256-text (pr-str (identity-record stat))))

(defn directory-stat-valid?
  [stat effective-user required-mode]
  (and (= s-ifdir (bit-and s-ifmt (:mode stat)))
       (= effective-user (:uid stat))
       (pos? (:link-count stat))
       (zero? (bit-and (:mode stat) 0x12))
       (or (nil? required-mode)
           (= required-mode (bit-and 0x0fff (:mode stat))))))

(defn regular-stat-valid?
  [stat effective-user expected-mode expected-byte-count]
  (and (= s-ifreg (bit-and s-ifmt (:mode stat)))
       (= effective-user (:uid stat))
       (= 1 (:link-count stat))
       (= expected-mode (bit-and 0x0fff (:mode stat)))
       (= expected-byte-count (:byte-count stat))))

(defn checkpoint!
  [event state]
  (*operation-checkpoint*
   event
   {:requested-parent (:parent-path state)
    :staging-name (:staging-leaf state)
    :destination-name (:destination-leaf state)})
  nil)

(defn open-absolute-directory!
  [runtime parent-path]
  (with-open [arena (Arena/ofConfined)]
    (let [path (.allocateFrom arena ^String parent-path)
          result
          (int-call-result runtime arena :open
                           [path (int absolute-directory-open-flags) (int 0)])]
      (when (neg? (:value result))
        (failure! :open-parent :parent-descriptor-open-failed result))
      (:value result))))

(defn open-relative!
  [runtime directory-descriptor leaf flags mode operation reason]
  (with-open [arena (Arena/ofConfined)]
    (let [name (.allocateFrom arena ^String leaf)
          result
          (int-call-result runtime arena :openat
                           [directory-descriptor name (int flags) (int mode)])]
      (when (neg? (:value result))
        (failure! operation reason
                  (assoc result :logical-path leaf)))
      (:value result))))

(defn same-identity?
  [left right]
  (= (select-keys left [:device :inode :mode :uid])
     (select-keys right [:device :inode :mode :uid])))

(defn same-object?
  [left right]
  ;; Cleanup must still recognize the held staging directory after a hostile
  ;; mode mutation or after its planned 0700 -> 0755 transition.  Type and
  ;; mode remain separately verified before commit; device/inode/owner bind
  ;; the parent name to the already-held directory for removal.
  (= (select-keys left [:device :inode :uid])
     (select-keys right [:device :inode :uid])))
