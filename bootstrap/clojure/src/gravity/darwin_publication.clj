(ns gravity.darwin-publication
  "JDK 26/Darwin descriptor-relative bundle publication.

  This namespace owns only the native descriptor lifecycle and raw bounded
  publication failures.  It does not select compiler targets, construct
  semantic artifacts, choose public diagnostics, or expose a command route."
  (:require [clojure.string :as str])
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

(def ^:private namespace-contract
  {:namespace 'gravity.darwin-publication
   :contract-boundary :darwin-descriptor-relative-publication
   :public-api
   {'open-target! {:arglists '([output-directory])}
    'stage-bundle! {:arglists '([target file-specs])}
    'commit-staged-bundle! {:arglists '([staged success-value])}
    'verify-published-bundle! {:arglists '([receipt file-specs])}
    'abort-staged-bundle! {:arglists '([target-or-staged])}}
   :owns
   [:native-runtime-preflight :held-parent-descriptor
    :descriptor-relative-staging :descriptor-relative-file-io
    :descriptor-relative-inventory :exclusive-native-rename
    :descriptor-relative-cleanup :descriptor-relative-verification
    :raw-bounded-failure-carriers]
   :does-not-own
   [:compiler-semantics :artifact-identity :diagnostic-rule-selection
    :governance-authority :cli-presentation :public-exposure
    :release-credit :self-hosting-credit]
   :requires ['clojure.core 'clojure.string]
   :forbids ['gravity.bootstrap 'gravity.diagnostics 'gravity.cli]
   :host {:jdk-feature 26 :jdk-version "26.0.1"
          :os-name "Mac OS X" :os-arch "aarch64"}
   :public? false :release? false :self-hosted? false})

(def ^:private provider-version 1)
(def ^:private maximum-file-bytes (* 8 1024 1024))
(def ^:private maximum-path-bytes 4096)
(def ^:private maximum-leaf-bytes 255)
(def ^:private maximum-cleanup-entries 64)
(def ^:private maximum-cleanup-depth 4)
(def ^:private stat-byte-count 144)
(def ^:private dirent-maximum-byte-count 1048)
(def ^:private path-buffer-byte-count 4096)

(def ^:private absolute-directory-open-flags 0x21100000)
(def ^:private relative-directory-open-flags 0x21103000)
(def ^:private exclusive-file-open-flags 0x21003a01)
(def ^:private unique-file-read-flags 0x21003000)

(def ^:private at-removedir 0x0080)
(def ^:private relative-unique-stat-flags 0xa800)
(def ^:private relative-bounded-stat-flags 0x2800)
(def ^:private relative-cleanup-file-flags 0)
(def ^:private relative-cleanup-directory-flags at-removedir)

(def ^:private f-getpath 50)
(def ^:private rename-excl 0x04)
(def ^:private rename-nofollow-any 0x10)
(def ^:private rename-resolve-beneath 0x20)
(def ^:private exclusive-rename-flags 0x34)

(def ^:private s-ifmt 0xf000)
(def ^:private s-ifdir 0x4000)
(def ^:private s-ifreg 0x8000)
(def ^:private owner-only-directory-mode 0700)
(def ^:private published-directory-mode 0755)
(def ^:private executable-file-mode 0755)
(def ^:private nonexecutable-file-mode 0644)

(def ^:private enoent 2)
(def ^:private eintr 4)
(def ^:private eexist 17)
(def ^:private acl-type-extended 0x100)

(def ^:private fixed-file-names
  #{"program.c" "program.h" "program.o" "program"
    "manifest.edn" "provenance.edn" "conformance.edn"})

(def ^:dynamic ^:private *operation-checkpoint*
  (fn [_event _bounded-context] nil))

(def ^:private context-controls
  ;; Publication contexts are identity capabilities.  Keeping lifecycle
  ;; authority in a weak side table avoids the public JVM fields emitted for
  ;; deftype fields while retaining terminal results for as long as a caller
  ;; still holds the corresponding context.
  (Collections/synchronizedMap (WeakHashMap.)))

(deftype ^:private PublicationContext
  [publication-receipt]
  clojure.lang.ILookup
  (valAt [_ key]
    (when (= :publication-receipt key)
      publication-receipt))
  (valAt [_ key not-found]
    (if (= :publication-receipt key)
      publication-receipt
      not-found))
  Object
  (toString [_] "#<gravity.darwin-publication/context>"))

;; `deftype` emits a constructor var; it is an implementation detail and must
;; not widen the namespace's five-function raw API.
(alter-meta! #'->PublicationContext assoc :private true)

(defn- register-context!
  [context control token]
  (.put context-controls context
        {:control control
         :token token})
  context)

(defn- context-entry
  [context]
  (when (instance? PublicationContext context)
    (.get context-controls context)))

(defn- failure-ex
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

(defn- failure!
  ([operation reason]
   (failure! operation reason {}))
  ([operation reason data]
   (throw (failure-ex operation reason data))))

(defn- interrupt-like?
  [error]
  (or (instance? InterruptedException error)
      (instance? java.nio.channels.ClosedByInterruptException error)
      (instance? java.io.InterruptedIOException error)))

(defn- rethrow-interrupt!
  [error]
  (when (interrupt-like? error)
    (.interrupt (Thread/currentThread))
    (throw error))
  error)

(defn- utf8-byte-count
  [text]
  (when (string? text)
    (alength (.getBytes ^String text StandardCharsets/UTF_8))))

(defn- valid-leaf?
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

(defn- output-location
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

(defn- function-descriptor
  [return-layout argument-layouts]
  (FunctionDescriptor/of
   return-layout
   (into-array MemoryLayout argument-layouts)))

(defn- symbol!
  [lookup name]
  (let [candidate (.find lookup name)]
    (when (.isEmpty candidate)
      (failure! :native-preflight :missing-native-symbol
                {:missing-symbol name}))
    (.get candidate)))

(defn- bind-captured!
  [linker lookup capture name return-layout argument-layouts
   & linker-options]
  (.downcallHandle
   linker (symbol! lookup name)
   (function-descriptor return-layout argument-layouts)
   (into-array Linker$Option (into [capture] linker-options))))

(defn- bind-direct!
  [linker lookup name return-layout argument-layouts
   & linker-options]
  (.downcallHandle
   linker (symbol! lookup name)
   (function-descriptor return-layout argument-layouts)
   (into-array Linker$Option linker-options)))

(defn- native-runtime!
  []
  (let [native-access-enabled?
        (.isNativeAccessEnabled (.getModule clojure.lang.RT))]
    (when-not
     (and (= 26 (.feature (Runtime/version)))
          (= "26.0.1" (System/getProperty "java.version"))
          (= "Mac OS X" (System/getProperty "os.name"))
          (= "aarch64" (System/getProperty "os.arch")))
      (failure! :native-preflight :unsupported-host-runtime))
    (when-not native-access-enabled?
      (failure! :native-preflight :native-access-disabled
                {:native-access-enabled? false}))
    (try
      (let [linker (Linker/nativeLinker)
            lookup (.defaultLookup linker)
            capture (Linker$Option/captureCallState
                     (into-array String ["errno"]))
            state-layout (Linker$Option/captureStateLayout)
            errno-handle
            (.toMethodHandle
             (.varHandle
              state-layout
              (into-array
               MemoryLayout$PathElement
               [(MemoryLayout$PathElement/groupElement "errno")]))
             VarHandle$AccessMode/GET)
            variadic2 (Linker$Option/firstVariadicArg 2)
            variadic3 (Linker$Option/firstVariadicArg 3)]
        {:state-layout state-layout
         :errno-handle errno-handle
         :open
         (bind-captured!
          linker lookup capture "open" ValueLayout/JAVA_INT
          [ValueLayout/ADDRESS ValueLayout/JAVA_INT ValueLayout/JAVA_INT]
          variadic2)
         :openat
         (bind-captured!
          linker lookup capture "openat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS
           ValueLayout/JAVA_INT ValueLayout/JAVA_INT]
          variadic3)
         :mkdirat
         (bind-captured!
          linker lookup capture "mkdirat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_SHORT])
         :fstat
         (bind-captured!
          linker lookup capture "fstat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS])
         :fstatat
         (bind-captured!
          linker lookup capture "fstatat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/ADDRESS
           ValueLayout/JAVA_INT])
         :fcntl-address
         (bind-captured!
          linker lookup capture "fcntl" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/JAVA_INT ValueLayout/ADDRESS]
          variadic2)
         :write
         (bind-captured!
          linker lookup capture "write" ValueLayout/JAVA_LONG
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_LONG])
         :pread
         (bind-captured!
          linker lookup capture "pread" ValueLayout/JAVA_LONG
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_LONG
           ValueLayout/JAVA_LONG])
         :fchmod
         (bind-captured!
          linker lookup capture "fchmod" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/JAVA_SHORT])
         :fsync
         (bind-captured!
          linker lookup capture "fsync" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT])
         :fdopendir
         (bind-captured!
          linker lookup capture "fdopendir" ValueLayout/ADDRESS
          [ValueLayout/JAVA_INT])
         :readdir
         (bind-captured!
          linker lookup capture "readdir" ValueLayout/ADDRESS
          [ValueLayout/ADDRESS])
         :closedir
         (bind-captured!
          linker lookup capture "closedir" ValueLayout/JAVA_INT
          [ValueLayout/ADDRESS])
         :unlinkat
         (bind-captured!
          linker lookup capture "unlinkat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_INT])
         :renameatx-np
         (bind-captured!
          linker lookup capture "renameatx_np" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_INT
           ValueLayout/ADDRESS ValueLayout/JAVA_INT])
         :close
         (bind-captured!
          linker lookup capture "close" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT])
         :geteuid
         (bind-captured!
          linker lookup capture "geteuid" ValueLayout/JAVA_INT [])
         :acl-get-fd-np
         (bind-captured!
          linker lookup capture "acl_get_fd_np" ValueLayout/ADDRESS
          [ValueLayout/JAVA_INT ValueLayout/JAVA_INT])
         :acl-free
         (bind-captured!
          linker lookup capture "acl_free" ValueLayout/JAVA_INT
          [ValueLayout/ADDRESS])
         :error-pointer
         (bind-direct!
          linker lookup "__error" ValueLayout/ADDRESS [])})
      (catch clojure.lang.ExceptionInfo error
        (throw error))
      (catch Exception error
        (rethrow-interrupt! error)
        (failure! :native-preflight :ffi-binding-unavailable)))))

(defn- captured-call
  [runtime arena operation arguments]
  (let [state (.allocate ^Arena arena (:state-layout runtime))
        value
        (.invokeWithArguments
         (get runtime operation)
         (object-array (into [state] arguments)))]
    {:state state :value value}))

(defn- captured-errno
  [runtime call]
  (int
   (.invokeWithArguments
    (:errno-handle runtime)
    (object-array [(:state call) (long 0)]))))

(defn- null-address?
  [value]
  (or (nil? value)
      (and (instance? MemorySegment value)
           (zero? (.address ^MemorySegment value)))))

(defn- int-call!
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

(defn- int-call-result
  [runtime arena operation arguments]
  (let [call (captured-call runtime arena operation arguments)
        value (int (:value call))]
    (if (neg? value)
      {:value value :errno (captured-errno runtime call)}
      {:value value})))

(defn- long-call-result
  [runtime arena operation arguments]
  (let [call (captured-call runtime arena operation arguments)
        value (long (:value call))]
    (if (neg? value)
      {:value value :errno (captured-errno runtime call)}
      {:value value})))

(defn- address-call-result
  [runtime arena operation arguments]
  (let [call (captured-call runtime arena operation arguments)
        value (:value call)]
    (if (null-address? value)
      {:value value :errno (captured-errno runtime call)}
      {:value value})))

(defn- close-fd!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (let [{:keys [value errno]}
          (int-call-result runtime arena :close [file-descriptor])]
      (when (neg? value)
        ;; POSIX leaves descriptor state unspecified after EINTR.  Never retry.
        (failure! operation :descriptor-close-failed
                  {:return-code value :errno errno}))))
  nil)

(defn- close-fd-quietly!
  [runtime file-descriptor]
  (when (and runtime (integer? file-descriptor) (not (neg? file-descriptor)))
    (try
      (with-open [arena (Arena/ofConfined)]
        (int-call-result runtime arena :close [file-descriptor]))
      (catch Throwable _ nil)))
  nil)

(defn- sha256-bytes
  [bytes]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.update digest ^bytes bytes)
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and % 0xff))
                     (.digest digest))))))

(defn- sha256-text
  [text]
  (sha256-bytes (.getBytes (str text) StandardCharsets/UTF_8)))

(defn- stat-record
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

(defn- fstat!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (let [buffer (.allocate arena (long stat-byte-count) (long 8))]
      (int-call! runtime arena :fstat [file-descriptor buffer]
                 :descriptor-stat-failed)
      (stat-record buffer))))

(defn- fstatat-result
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

(defn- descriptor-path!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (let [buffer (.allocate arena (long path-buffer-byte-count) (long 1))]
      (int-call! runtime arena :fcntl-address
                 [file-descriptor (int f-getpath) buffer]
                 :descriptor-path-failed)
      (.getString buffer (long 0) StandardCharsets/UTF_8))))

(defn- effective-user-id!
  [runtime]
  (with-open [arena (Arena/ofConfined)]
    (let [call (captured-call runtime arena :geteuid [])]
      (Integer/toUnsignedLong (int (:value call))))))

(defn- assert-no-extended-acl!
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

(defn- identity-record
  [stat]
  ;; Directory link counts change when staging and destination entries are
  ;; added or removed; they are validity facts, not stable identity inputs.
  (select-keys stat [:device :inode :mode :uid]))

(defn- identity-hash
  [stat]
  (sha256-text (pr-str (identity-record stat))))

(defn- directory-stat-valid?
  [stat effective-user required-mode]
  (and (= s-ifdir (bit-and s-ifmt (:mode stat)))
       (= effective-user (:uid stat))
       (pos? (:link-count stat))
       (zero? (bit-and (:mode stat) 0x12))
       (or (nil? required-mode)
           (= required-mode (bit-and 0x0fff (:mode stat))))))

(defn- regular-stat-valid?
  [stat effective-user expected-mode expected-byte-count]
  (and (= s-ifreg (bit-and s-ifmt (:mode stat)))
       (= effective-user (:uid stat))
       (= 1 (:link-count stat))
       (= expected-mode (bit-and 0x0fff (:mode stat)))
       (= expected-byte-count (:byte-count stat))))

(defn- checkpoint!
  [event state]
  (*operation-checkpoint*
   event
   {:requested-parent (:parent-path state)
    :staging-name (:staging-leaf state)
    :destination-name (:destination-leaf state)})
  nil)

(defn- open-absolute-directory!
  [runtime parent-path]
  (with-open [arena (Arena/ofConfined)]
    (let [path (.allocateFrom arena ^String parent-path)
          result
          (int-call-result runtime arena :open
                           [path (int absolute-directory-open-flags) (int 0)])]
      (when (neg? (:value result))
        (failure! :open-parent :parent-descriptor-open-failed result))
      (:value result))))

(defn- open-relative!
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

(defn- same-identity?
  [left right]
  (= (select-keys left [:device :inode :mode :uid])
     (select-keys right [:device :inode :mode :uid])))

(defn- same-object?
  [left right]
  ;; Cleanup must still recognize the held staging directory after a hostile
  ;; mode mutation or after its planned 0700 -> 0755 transition.  Type and
  ;; mode remain separately verified before commit; device/inode/owner bind
  ;; the parent name to the already-held directory for removal.
  (= (select-keys left [:device :inode :uid])
     (select-keys right [:device :inode :uid])))

(defn- context-state!
  [context operation]
  (let [entry (context-entry context)
        control (:control entry)
        token (:token entry)]
    (when-not
     (and entry
          (instance? clojure.lang.Atom control)
          (some? token))
      (failure! operation :invalid-provider-context))
    (let [state @control]
      (when-not (and (map? state)
                     (= :gravity/darwin-descriptor-publication
                        (:provider state))
                     (= provider-version (:provider-version state))
                     (identical? token (:token state))
                     (integer? (:generation state))
                     (keyword? (:phase state)))
        (failure! operation :invalid-provider-control-state))
      state)))

(defn- update-control!
  [context expected-phases next-state operation]
  (loop []
    (let [state (context-state! context operation)
          control (:control (context-entry context))]
        (when-not (contains? expected-phases (:phase state))
          (failure! operation :invalid-provider-lifecycle))
        (let [candidate
              (-> (next-state state)
                  (assoc :generation (inc (:generation state))))]
          (if (compare-and-set! control state candidate)
            candidate
            (recur))))))

(defn- mark-failed!
  [context expected-phase]
  (update-control! context #{expected-phase}
                   #(assoc % :phase :failed) :mark-failed))

(defn open-target!
  "Open and authenticate an output parent without following symlinks.

  The returned value is an opaque, single-use provider context.  Callers must
  pass it to `stage-bundle!` or `abort-staged-bundle!`."
  [output-directory]
  (let [location (output-location output-directory)
        runtime (native-runtime!)
        parent-descriptor (atom nil)]
    (try
      (let [descriptor
            (open-absolute-directory! runtime (:parent-path location))
            _ (reset! parent-descriptor descriptor)
            effective-user (effective-user-id! runtime)
            parent-stat (fstat! runtime descriptor :authenticate-parent)
            _ (assert-no-extended-acl!
               runtime descriptor :authenticate-parent)
            parent-path
            (descriptor-path! runtime descriptor :authenticate-parent)
            destination
            (fstatat-result runtime descriptor
                            (:destination-leaf location)
                            relative-unique-stat-flags)]
        (when-not
         (and (= (:parent-path location) parent-path)
              (directory-stat-valid? parent-stat effective-user nil))
          (failure! :authenticate-parent :untrusted-parent-descriptor))
        (cond
          (zero? (:value destination))
          (failure! :authenticate-destination :destination-exists
                    {:output-collision? true})

          (not= enoent (:errno destination))
          (failure! :authenticate-destination
                    :destination-absence-check-failed destination))
        (let [token (Object.)
              control
              (atom {:provider :gravity/darwin-descriptor-publication
                     :provider-version provider-version
                     :phase :target-open
                     :generation 0
                     :token token
                     :runtime runtime
                     :parent-descriptor descriptor
                     :staging-descriptor nil
                     :staging-leaf nil
                     :staging-stat nil
                     :parent-path parent-path
                     :destination-path (:destination-path location)
                     :destination-leaf (:destination-leaf location)
                     :effective-user effective-user
                     :parent-stat parent-stat
                     :parent-identity-hash (identity-hash parent-stat)})]
          (register-context! (PublicationContext. nil) control token)))
      (catch Throwable error
        (when-let [descriptor @parent-descriptor]
          (close-fd-quietly! runtime descriptor))
        (rethrow-interrupt! error)
        (throw error)))))

(defn- normalized-file-specs
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

(defn- random-staging-leaf
  []
  (let [bytes (byte-array 16)]
    (.nextBytes (SecureRandom.) bytes)
    (str ".gravity-c17-"
         (apply str
                (map #(format "%02x" (bit-and % 0xff)) bytes)))))

(defn- mkdir-relative!
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

(defn- write-all!
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

(defn- pread-exact!
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

(defn- chmod-fd!
  [runtime file-descriptor mode operation]
  (with-open [arena (Arena/ofConfined)]
    (int-call! runtime arena :fchmod
               [file-descriptor (short mode)] :descriptor-chmod-failed))
  nil)

(defn- fsync-fd!
  [runtime file-descriptor operation]
  (with-open [arena (Arena/ofConfined)]
    (int-call! runtime arena :fsync
               [file-descriptor] :descriptor-sync-failed))
  nil)

(defn- verify-relative-file!
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

(defn- create-relative-file!
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

(defn- strict-utf8
  [bytes operation]
  (try
    (let [decoder
          (doto (.newDecoder StandardCharsets/UTF_8)
            (.onMalformedInput CodingErrorAction/REPORT)
            (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (str (.decode decoder (ByteBuffer/wrap ^bytes bytes))))
    (catch CharacterCodingException _
      (failure! operation :invalid-utf8-directory-entry))))

(defn- dirent-name!
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

(defn- directory-inventory!
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

(defn- unlink-relative-result
  [runtime directory-descriptor leaf flags]
  (with-open [arena (Arena/ofConfined)]
    (let [name (.allocateFrom arena ^String leaf)]
      (int-call-result runtime arena :unlinkat
                       [directory-descriptor name (int flags)]))))

(defn- valid-descriptor?
  [descriptor]
  (and (integer? descriptor) (not (neg? descriptor))))

(defn- close-owned-descriptor-result!
  [runtime descriptor]
  (if-not (valid-descriptor? descriptor)
    true
    (try
      (with-open [arena (Arena/ofConfined)]
        (not (neg? (:value
                    (int-call-result runtime arena :close [descriptor])))))
      (catch Exception error
        (rethrow-interrupt! error)
        false))))

(declare cleanup-directory-result! abort-staged-bundle!)

(defn- cleanup-entry-result!
  [runtime directory-descriptor leaf depth remaining]
  (if-not (and (valid-leaf? leaf)
               (pos? (swap! remaining dec)))
    false
    (try
      (let [snapshot
            (fstatat-result runtime directory-descriptor leaf
                            relative-bounded-stat-flags)]
        (cond
          (= enoent (:errno snapshot)) true
          (neg? (:value snapshot)) false

          (= s-ifdir (bit-and s-ifmt (get-in snapshot [:stat :mode])))
          (if (>= depth maximum-cleanup-depth)
            false
            (let [child
                  (open-relative! runtime directory-descriptor leaf
                                  relative-directory-open-flags 0
                                  :cleanup :cleanup-directory-open-failed)
                  closed? (atom false)
                  cleaned?
                  (try
                    (cleanup-directory-result!
                     runtime child (inc depth) remaining)
                    (finally
                      ;; The descriptor is local ownership and is closed once,
                      ;; including when a fatal error escapes recursion.
                      (reset! closed?
                              (close-owned-descriptor-result!
                               runtime child))))
                  removed
                  (when (and cleaned? @closed?)
                    (unlink-relative-result
                     runtime directory-descriptor leaf
                     relative-cleanup-directory-flags))]
              (and cleaned? @closed? (zero? (:value removed)))))

          :else
          (let [removed
                (unlink-relative-result runtime directory-descriptor leaf
                                        relative-cleanup-file-flags)]
            (or (zero? (:value removed)) (= enoent (:errno removed))))))
      (catch Exception error
        (rethrow-interrupt! error)
        false))))

(defn- cleanup-directory-result!
  [runtime directory-descriptor depth remaining]
  (let [complete? (atom true)
        names
        (try
          (directory-inventory! runtime directory-descriptor
                                maximum-cleanup-entries)
          (catch Exception error
            (rethrow-interrupt! error)
            (reset! complete? false)
            fixed-file-names))]
    (doseq [name (sort names)]
      (when-not
       (cleanup-entry-result! runtime directory-descriptor name
                              depth remaining)
        (reset! complete? false)))
    (try
      (when-not (empty?
                 (directory-inventory! runtime directory-descriptor
                                       maximum-cleanup-entries))
        (reset! complete? false))
      (catch Exception error
        (rethrow-interrupt! error)
        (reset! complete? false)))
    @complete?))

(defn- claim-abort!
  [context]
  (loop []
    (let [state (context-state! context :abort)
          control (:control (context-entry context))]
        (case (:phase state)
          :committed {:claimed? false :terminal-state state}
          :aborted {:claimed? false :terminal-state state}
          (:target-open :staged :failed)
          (let [candidate
                (-> state
                    (assoc :phase :aborting
                           :parent-descriptor nil
                           :staging-descriptor nil)
                    (update :generation inc))]
            (if (compare-and-set! control state candidate)
              {:claimed? true :owned state}
              (recur)))
          (failure! :abort :invalid-provider-lifecycle)))))

(defn- attach-incomplete-cleanup!
  [error cleanup]
  (when-not (:cleanup-complete? cleanup)
    (.addSuppressed
     ^Throwable error
     (failure-ex :cleanup :cleanup-incomplete
                 {:cleanup-complete? false
                  :residue-possible? true})))
  error)

(defn- abort-after-failure!
  [context error]
  (try
    (attach-incomplete-cleanup! error (abort-staged-bundle! context))
    (catch Throwable cleanup-error
      (.addSuppressed ^Throwable error ^Throwable cleanup-error)))
  (rethrow-interrupt! error)
  (throw error))

(defn abort-staged-bundle!
  "Best-effort descriptor-relative cleanup for an open target or staging value.

  Cleanup never follows a replacement path and never removes a staging name
  whose current identity differs from the held staging descriptor."
  [target]
  (let [{:keys [claimed? terminal-state owned]} (claim-abort! target)]
    (if-not claimed?
      (if (= :committed (:phase terminal-state))
        {:status :already-committed
         :published? true
         :cleanup-applicable? false
         :native-calls 0}
        {:status :already-aborted
         :published? false
         :cleanup-complete?
         (true? (:cleanup-complete? terminal-state))
         :residue-possible?
         (true? (:residue-possible? terminal-state))
         :native-calls 0})
      (let [runtime (:runtime owned)
            complete? (atom true)
            remaining (atom (inc maximum-cleanup-entries))
            parent-descriptor (:parent-descriptor owned)
            staging-descriptor (:staging-descriptor owned)
            staging-leaf (:staging-leaf owned)
            staging-stat (:staging-stat owned)]
        (try
          (when (valid-descriptor? staging-descriptor)
            (when-not
             (cleanup-directory-result!
              runtime staging-descriptor 0 remaining)
              (reset! complete? false)))
          (finally
            (when-not
             (close-owned-descriptor-result! runtime staging-descriptor)
              (reset! complete? false))))
        (when (and (valid-descriptor? parent-descriptor)
                   (valid-leaf? staging-leaf))
          (if-not staging-stat
            (reset! complete? false)
            (try
              (let [current
                    (fstatat-result runtime parent-descriptor staging-leaf
                                    relative-bounded-stat-flags)]
                (cond
                  (= enoent (:errno current)) nil
                  (and (zero? (:value current))
                       (same-object? staging-stat (:stat current)))
                  (let [removed
                        (unlink-relative-result
                         runtime parent-descriptor staging-leaf
                         relative-cleanup-directory-flags)]
                    (when-not (or (zero? (:value removed))
                                  (= enoent (:errno removed)))
                      (reset! complete? false)))
                  :else (reset! complete? false)))
              (catch Exception error
                (rethrow-interrupt! error)
                (reset! complete? false)))))
        (when-not
         (close-owned-descriptor-result! runtime parent-descriptor)
          (reset! complete? false))
        (update-control! target #{:aborting}
                         #(assoc % :phase :aborted
                                 :staging-leaf nil :staging-stat nil
                                 :file-specs nil :file-records nil
                                 :publication-receipt nil
                                 :cleanup-complete? @complete?
                                 :residue-possible? (not @complete?))
                         :finish-abort)
        {:status :aborted
         :published? false
         :cleanup-complete? @complete?
         :residue-possible? (not @complete?)}))))

(defn- staging-name-bound-to-descriptor?
  [state]
  (let [runtime (:runtime state)
        current
        (fstatat-result runtime (:parent-descriptor state)
                        (:staging-leaf state)
                        relative-unique-stat-flags)]
    (and (zero? (:value current))
         (same-identity? (:staging-stat state) (:stat current))
         (= s-ifdir (bit-and s-ifmt (get-in current [:stat :mode]))))))

(defn- descriptor-paths-stable?
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

(defn- destination-absent?
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

(defn stage-bundle!
  "Create, write, sync, inventory, and verify a seven-file private bundle.

  The result remains private.  Its publication receipt is only valid if a
  later `commit-staged-bundle!` succeeds."
  [target file-specs]
  (let [specs
        (try
          (normalized-file-specs file-specs)
          (catch Throwable error
            (abort-after-failure! target error)))
        starting
        (update-control! target #{:target-open}
                         #(assoc % :phase :staging)
                         :stage-bundle)
        runtime (:runtime starting)
        parent-descriptor (:parent-descriptor starting)
        unowned-staging-descriptor (atom nil)]
    (try
      (let [staging-leaf
            (mkdir-relative! runtime parent-descriptor)
            _ (update-control! target #{:staging}
                               #(assoc % :staging-leaf staging-leaf)
                               :record-staging-name)
            descriptor
            (open-relative! runtime parent-descriptor
                            staging-leaf relative-directory-open-flags 0
                            :open-staging
                            :staging-directory-open-failed)
            _ (reset! unowned-staging-descriptor descriptor)
            _ (update-control! target #{:staging}
                               #(assoc % :staging-descriptor descriptor)
                               :record-staging-descriptor)
            _ (reset! unowned-staging-descriptor nil)
              stat (fstat! runtime descriptor :authenticate-staging)
              _ (assert-no-extended-acl!
                 runtime descriptor :authenticate-staging)
              descriptor-path
              (descriptor-path! runtime descriptor :authenticate-staging)
              expected-path (str (:parent-path starting) "/" staging-leaf)
              name-stat
              (fstatat-result runtime parent-descriptor
                              staging-leaf relative-unique-stat-flags)
              _
              (when-not
               (and (= expected-path descriptor-path)
                    (directory-stat-valid?
                     stat (:effective-user starting)
                     owner-only-directory-mode)
                    (zero? (:value name-stat))
                    (same-identity? stat (:stat name-stat)))
                (failure! :authenticate-staging
                          :untrusted-staging-descriptor))
              authenticated
              (update-control! target #{:staging}
                               #(assoc % :staging-stat stat)
                               :record-staging-identity)
              _ (checkpoint! :staging-handle-opened authenticated)
              initial-file-records
              (into
               (sorted-map)
               (map
                (fn [[logical-path expected]]
                  [logical-path
                   (create-relative-file!
                    runtime descriptor (:effective-user starting)
                    logical-path expected)]))
               specs)
              _ (chmod-fd! runtime descriptor published-directory-mode
                           :finalize-staging-mode)
              _ (assert-no-extended-acl!
                 runtime descriptor :finalize-staging-mode)
              _ (fsync-fd! runtime descriptor :sync-staging-directory)
              before-final-state (context-state! target :stage-bundle)
              _ (checkpoint! :before-final-staging-verification
                             before-final-state)
              final-stat (fstat! runtime descriptor :verify-staging)
              _ (assert-no-extended-acl!
                 runtime descriptor :verify-staging)
              final-name-stat
              (fstatat-result runtime parent-descriptor
                              staging-leaf relative-unique-stat-flags)
              inventory (directory-inventory! runtime descriptor)
              final-file-records
              (into
               (sorted-map)
               (map
                (fn [[logical-path expected]]
                  [logical-path
                   (verify-relative-file!
                    runtime descriptor (:effective-user starting)
                    logical-path expected)]))
               specs)
              _
              (when-not
               (and (= fixed-file-names inventory)
                    (= initial-file-records final-file-records)
                    (directory-stat-valid?
                     final-stat (:effective-user starting)
                     published-directory-mode)
                    (zero? (:value final-name-stat))
                    (same-identity? final-stat (:stat final-name-stat)))
                (failure! :verify-staging :staging-contract-mismatch
                          {:expected-file-count 7
                           :observed-file-count (count inventory)
                           :expected-mode published-directory-mode
                           :observed-mode
                           (bit-and 0x0fff (:mode final-stat))}))
              publisher-evidence
              {:provider :gravity/darwin-descriptor-publication
               :provider-version provider-version
               :jdk-version "26.0.1" :jdk-feature 26
               :native-access-enabled? true
               :ffi-provider :jdk-26-foreign-function-and-memory
               :native-library :darwin-libsystem
               :symbol "renameatx_np"
               :commit-primitive :darwin-renameatx-np
               :errno-read-policy :failure-only
               :path-identity-linearization
               :held-parent-and-staging-directory-descriptors
                 :guarantee-scope
               #{:descriptor-bound-parent :descriptor-relative-staging
                 :resolve-beneath :no-symlink-traversal
                 :exclusive-destination :unique-regular-files
                 :exact-directory-inventory
                 :no-extended-access-control-lists}
               :flags
               {:rename-excl rename-excl
                :rename-nofollow-any rename-nofollow-any
                :rename-resolve-beneath rename-resolve-beneath
                :combined exclusive-rename-flags}
               :parent-identity-hash (:parent-identity-hash starting)
               :staging-identity-hash (identity-hash final-stat)
               :source-directory-trailing-slash? true
               :postcommit-close-failures-change-result? false
               :crash-durable-publication? false
               :same-euid-concurrent-mutation-resistant? false}
              receipt
              {:status :published-atomically-after-final-verification
               :actual-output-directory (:destination-path starting)
               :file-records
               (into
                (sorted-map)
                (map (fn [[name record]]
                       [name (select-keys record
                                          [:byte-count :content-hash :mode])]))
                final-file-records)
               :publisher-evidence publisher-evidence
               :mode-policy
               {:directory "0755" :executable "0755"
                :nonexecutable "0644"}}
              _ (update-control! target #{:staging}
                                 #(assoc % :phase :staged
                                           :staging-stat final-stat
                                           :file-specs specs
                                           :file-records final-file-records
                                           :publication-receipt receipt)
                                 :finish-staging)]
        (let [{:keys [control token]} (context-entry target)]
          (register-context! (PublicationContext. receipt) control token)))
      (catch Throwable error
        (when-let [descriptor @unowned-staging-descriptor]
          (close-owned-descriptor-result! runtime descriptor))
        (try
          (mark-failed! target :staging)
          (catch Throwable transition-error
            (.addSuppressed ^Throwable error ^Throwable transition-error)))
        (abort-after-failure! target error)))))

(defn- revalidate-staged-bundle!
  [staged state check-destination?]
  (let [runtime (:runtime state)
        parent-descriptor (:parent-descriptor state)
        staging-descriptor (:staging-descriptor state)
        parent-stat
        (fstat! runtime parent-descriptor :revalidate-parent)
        _ (assert-no-extended-acl!
           runtime parent-descriptor :revalidate-parent)
        staging-stat
        (fstat! runtime staging-descriptor :revalidate-staging)
        _ (assert-no-extended-acl!
           runtime staging-descriptor :revalidate-staging)
        inventory (directory-inventory! runtime staging-descriptor)
        file-records
        (into
         (sorted-map)
         (map
          (fn [[logical-path expected]]
            [logical-path
             (verify-relative-file!
              runtime staging-descriptor
              (:effective-user state) logical-path expected)]))
         (:file-specs state))]
    (when-not
     (and (= (:publication-receipt state)
             (:publication-receipt staged))
          (same-identity? (:parent-stat state) parent-stat)
          (same-identity? (:staging-stat state) staging-stat)
          (descriptor-paths-stable? state)
          (staging-name-bound-to-descriptor? state)
          (= fixed-file-names inventory)
          (= (:file-records state) file-records)
          (or (not check-destination?)
              (destination-absent? state)))
      (failure! :precommit :descriptor-or-content-identity-changed
                {:expected-file-count 7
                 :observed-file-count (count inventory)}))
    staged))

(defn commit-staged-bundle!
  "Publish a verified private bundle and return an already-built success value.

  The native rename is the only publication linearization point.  Descriptor
  cleanup after success is best effort and cannot change the returned result."
  [staged success-value]
  (let [committing
        (update-control! staged #{:staged}
                         #(assoc % :phase :committing)
                         :commit)]
    (try
      (checkpoint! :before-final-name-binding committing)
      (revalidate-staged-bundle! staged committing true)
      (checkpoint! :before-native-rename committing)
      ;; Permit the destination checkpoint race so RENAME_EXCL remains the
      ;; collision linearizer, then reverify every other staged fact.
      (revalidate-staged-bundle! staged committing false)
      (let [runtime (:runtime committing)
            result
            (with-open [arena (Arena/ofConfined)]
              (let [source
                    ;; Darwin RENAME_NOFOLLOW_ANY renames a final symlink.
                    ;; A trailing slash requires the source itself to be a
                    ;; directory, while the validated base remains one leaf.
                    (.allocateFrom arena
                                   (str (:staging-leaf committing) "/"))
                    destination
                    (.allocateFrom arena
                                   ^String (:destination-leaf committing))]
                (int-call-result
                 runtime arena :renameatx-np
                 [(:parent-descriptor committing) source
                  (:parent-descriptor committing) destination
                  (int exclusive-rename-flags)])))]
        (when (neg? (:value result))
          (failure! :commit
                    (if (= eexist (:errno result))
                      :destination-collision :exclusive-rename-failed)
                    (assoc result
                           :output-collision? (= eexist (:errno result)))))
        ;; Consume fd ownership in the control atom before closing.  A close
        ;; failure may already have released and recycled the integer.
        (update-control! staged #{:committing}
                         #(assoc % :phase :committed
                                   :parent-descriptor nil
                                   :staging-descriptor nil
                                   :staging-leaf nil
                                   :staging-stat nil
                                   :file-specs nil
                                   :file-records nil
                                   :publication-receipt nil)
                         :finish-commit)
        (close-owned-descriptor-result!
         runtime (:staging-descriptor committing))
        (close-owned-descriptor-result!
         runtime (:parent-descriptor committing))
        success-value)
      (catch Throwable error
        (when (= :committing
                 (:phase (context-state! staged :commit-failure)))
          (try
            (mark-failed! staged :committing)
            (catch Throwable transition-error
              (.addSuppressed ^Throwable error ^Throwable transition-error))))
        (abort-after-failure! staged error)))))

(defn- publisher-evidence-valid?
  [evidence]
  (and
   (= :gravity/darwin-descriptor-publication (:provider evidence))
   (= provider-version (:provider-version evidence))
   (= ["26.0.1" 26 true
       :jdk-26-foreign-function-and-memory
       :darwin-libsystem "renameatx_np" :darwin-renameatx-np
       :failure-only
       :held-parent-and-staging-directory-descriptors]
      ((juxt :jdk-version :jdk-feature :native-access-enabled?
             :ffi-provider :native-library :symbol :commit-primitive
             :errno-read-policy :path-identity-linearization)
       evidence))
   (= #{:descriptor-bound-parent :descriptor-relative-staging
        :resolve-beneath :no-symlink-traversal
        :exclusive-destination :unique-regular-files
        :exact-directory-inventory
        :no-extended-access-control-lists}
      (:guarantee-scope evidence))
   (= {:rename-excl rename-excl
       :rename-nofollow-any rename-nofollow-any
       :rename-resolve-beneath rename-resolve-beneath
       :combined exclusive-rename-flags}
      (:flags evidence))
   (string? (:parent-identity-hash evidence))
   (string? (:staging-identity-hash evidence))
   (true? (:source-directory-trailing-slash? evidence))
   (false? (:postcommit-close-failures-change-result? evidence))
   (false? (:crash-durable-publication? evidence))
   (false? (:same-euid-concurrent-mutation-resistant? evidence))))

(defn- open-published-directory!
  [receipt]
  (let [location (output-location (:actual-output-directory receipt))
        runtime (native-runtime!)
        parent-descriptor (atom nil)
        published-descriptor (atom nil)]
    (try
      (let [parent
            (open-absolute-directory! runtime (:parent-path location))
            _ (reset! parent-descriptor parent)
            effective-user (effective-user-id! runtime)
            parent-stat (fstat! runtime parent :verify-parent)
            _ (assert-no-extended-acl! runtime parent :verify-parent)
            parent-path
            (descriptor-path! runtime parent :verify-parent)
            published
            (open-relative! runtime parent (:destination-leaf location)
                            relative-directory-open-flags 0
                            :verify-publication
                            :published-directory-open-failed)
            _ (reset! published-descriptor published)
            published-stat
            (fstat! runtime published :verify-publication)
            _ (assert-no-extended-acl!
               runtime published :verify-publication)
            published-path
            (descriptor-path! runtime published :verify-publication)]
        (when-not
         (and (= (:parent-path location) parent-path)
              (= (:destination-path location) published-path)
              (directory-stat-valid? parent-stat effective-user nil)
              (directory-stat-valid?
               published-stat effective-user published-directory-mode))
          (failure! :verify-publication
                    :published-directory-provenance-mismatch))
        {:runtime runtime
         :parent-descriptor parent
         :published-descriptor published
         :effective-user effective-user
         :parent-stat parent-stat
         :published-stat published-stat
         :location location})
      (catch Throwable error
        (close-fd-quietly! runtime @published-descriptor)
        (close-fd-quietly! runtime @parent-descriptor)
        (rethrow-interrupt! error)
        (throw error)))))

(defn verify-published-bundle!
  "Reopen and verify a published bundle through held descriptors."
  [receipt file-specs]
  (let [specs (normalized-file-specs file-specs)
        expected-file-records
        (into
         (sorted-map)
         (map (fn [[name spec]]
                [name (select-keys spec
                                   [:byte-count :content-hash :mode])]))
         specs)]
    (when-not
     (and (map? receipt)
          (= :published-atomically-after-final-verification
             (:status receipt))
          (= expected-file-records (:file-records receipt))
          (= {:directory "0755" :executable "0755"
              :nonexecutable "0644"}
             (:mode-policy receipt))
          (publisher-evidence-valid? (:publisher-evidence receipt)))
      (failure! :verify-publication :invalid-publication-receipt))
    (let [opened (open-published-directory! receipt)
          runtime (:runtime opened)]
      (try
        (let [publisher (:publisher-evidence receipt)
              inventory
              (directory-inventory! runtime (:published-descriptor opened))
              file-records
              (into
               (sorted-map)
               (map
                (fn [[logical-path expected]]
                  [logical-path
                   (verify-relative-file!
                    runtime (:published-descriptor opened)
                    (:effective-user opened) logical-path expected)]))
               specs)]
          (when-not
           (and (= fixed-file-names inventory)
                (= expected-file-records
                   (into
                    (sorted-map)
                    (map (fn [[name record]]
                           [name (select-keys record
                                              [:byte-count :content-hash
                                               :mode])]))
                    file-records))
                (= (:parent-identity-hash publisher)
                   (identity-hash (:parent-stat opened)))
                (= (:staging-identity-hash publisher)
                   (identity-hash (:published-stat opened))))
            (failure! :verify-publication
                      :published-bundle-content-or-identity-mismatch
                      {:expected-file-count 7
                       :observed-file-count (count inventory)}))
          {:status :passed
           :publication :descriptor-relative-exclusive-rename
           :file-count 7
           :file-records expected-file-records
           :publisher-evidence publisher})
        (finally
          (close-fd-quietly! runtime (:published-descriptor opened))
          (close-fd-quietly! runtime (:parent-descriptor opened)))))))
