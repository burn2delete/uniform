

(defn- p18-t04-verified-mir-c-source-path-lexically-valid?
  [source-path]
  (let [character-count (when (string? source-path) (count source-path))
        utf8-byte-count
        (when (and (string? source-path)
                   (<= 1 character-count 4096))
          (alength (.getBytes ^String source-path
                             java.nio.charset.StandardCharsets/UTF_8)))]
    (and (string? source-path)
         (<= 1 character-count 4096)
         (<= 1 (or utf8-byte-count 0) 4096)
         (not (str/blank? source-path))
         (not (str/includes? source-path "\u0000"))
         (not-any? #(Character/isISOControl ^char %) source-path)
         (qst-or-gravity-source? source-path))))

(defn- p18-t04-verified-mir-c-output-lexical-record
  [output-path]
  (let [character-count (when (string? output-path) (count output-path))
        utf8-byte-count
        (when (and (string? output-path)
                   (<= 1 character-count 4096))
          (alength (.getBytes ^String output-path
                             java.nio.charset.StandardCharsets/UTF_8)))
        relative
        (when (and (string? output-path)
                   (<= 1 character-count 4096)
                   (<= 1 (or utf8-byte-count 0) 4096)
                   (not (str/blank? output-path))
                   (not (str/includes? output-path "\u0000"))
                   (not-any? #(Character/isISOControl ^char %) output-path)
                   (not (str/includes? output-path "\\")))
          (try
            (java.nio.file.Paths/get output-path (make-array String 0))
            (catch java.nio.file.InvalidPathException _ nil)))
        normalized-relative (when relative (.normalize relative))
        valid?
        (boolean
         (and relative
              (not (.isAbsolute relative))
              (= output-path (.toString normalized-relative))
              (<= 2 (.getNameCount normalized-relative))
              (= "target"
                 (.toString (.getName normalized-relative 0)))))]
    {:character-count character-count
     :utf8-byte-count utf8-byte-count
     :normalized-relative normalized-relative
     :valid? valid?}))

(defn p18-t04-verified-mir-c-request!
  [request]
  (when-not
   (and (map? request)
        (= :verified-mir (:lowering-mode request))
        (= "verified-mir" (:lowering-argument request))
        (true? (:lowering-requested? request)))
    (p18-t04-fail!
     "P18T04002"
     {:source (or (:source-path request) "bin/gravity")
      :missing-fields [:exact-verified-mir-lowering]
      :remediation
      "Use the exact option pair --lowering verified-mir."}))
  (when-not (:target-requested? request)
    (p18-t04-fail!
     "P18T04002"
     {:source (:source-path request)
      :missing-fields [:target]
      :remediation
      "The experimental verified-MIR route requires --target c."}))
  (when-not (and (= :c (:target request))
                 (= "c" (:target-argument request)))
    (p15-s23-c-backend-fail!
     "C14-TARGET" (:source-path request) {}
     {:missing-fact :exact-verified-mir-c-target
      :requested-target (:target request)
      :requested-target-argument (:target-argument request)
      :source-target :jvm
      :bounded-reason :verified-mir-c-target-selection}))
  (when-not (= "-o" (:output-option request))
    (p18-t04-fail!
     "P18T04002"
     {:source (:source-path request)
      :missing-fields
      [(if (:output-path request)
         :exact-bundle-output-option
         :output-path)]
      :remediation
      "Use the exact bundle-directory option -o target/<bundle-directory>."}))
  (when-not
   (p18-t04-verified-mir-c-source-path-lexically-valid?
    (:source-path request))
    (p18-t04-fail!
     "P18T04002"
     {:source (or (:source-path request) "bin/gravity")
      :missing-fields [:co-canonical-bounded-source-path]
      :remediation
      "Use a bounded .qst or .gravity source path."}))
  (when-not
   (:valid?
    (p18-t04-verified-mir-c-output-lexical-record
     (:output-path request)))
    (p18-t04-fail!
     "P18T04002"
     {:source (:source-path request)
      :missing-fields [:normalized-repository-target-bundle-directory]
      :remediation
      "Use a normalized relative path target/<bundle-directory>."}))
  request)

(defn- p18-t04-verified-mir-c-public-governance-fail!
  [id request missing-fact required-evidence remediation]
  (p17-governance-fail!
   id (:source-path request)
   {:document-id "GOV7" :missing-fact missing-fact}
   {:rule id
    :profile :hosted
    :target :c
    :scope :p18-t04-verified-mir-c-public-command
    :required-evidence required-evidence
    :affected-profiles [:hosted]
    :affected-targets [:c]
    :release-gate :blocked
    :fallback-status :public-exposure-disabled
    :remediation remediation}))

(defn- p18-t04-verified-mir-c-public-authority!
  "Pure GOV7 authority gate. Request shape is checked before governance, but
  no repository, source, output, toolchain, native, or publication state is
  observed. This temporary gate is deliberately unconditional: policy
  booleans are not substitutes for authenticated feature-specific records."
  [request]
  (let [request (p18-t04-verified-mir-c-request! request)]
    (p18-t04-verified-mir-c-public-governance-fail!
     "GOV7006" request :feature-specific-gov4-security-review-record
     [:gov4-security-review-record]
     (str "Use the established bootstrap compile routes instead. "
          "Maintainers must complete and bind the feature-specific GOV4 "
          "security review before exposing the proposed verified-MIR C "
          "candidate."))))

(defn p18-t04-verified-mir-c-output-directory!
  "Resolve and validate an exclusive repo-root target bundle before source IO."
  [source-path output-path]
  (let [{:keys [character-count utf8-byte-count normalized-relative]
         lexical-valid? :valid?}
        (p18-t04-verified-mir-c-output-lexical-record output-path)]
    (when-not lexical-valid?
      (p18-t04-fail!
       "P18T04002"
       {:source source-path
        :output-character-count character-count
        :output-byte-count utf8-byte-count
        :missing-fields [:normalized-repository-target-bundle-directory]
        :remediation
        "Use a normalized relative path target/<bundle-directory> without empty, dot, dot-dot, trailing, or backslash segments."}))
    (let [root (.normalize (.toAbsolutePath (p18-t04-repository-root-path)))
          target-root (.resolve root "target")
          destination (.normalize (.resolve root normalized-relative))
          parent (.getParent destination)
          nofollow
          (into-array java.nio.file.LinkOption
                      [java.nio.file.LinkOption/NOFOLLOW_LINKS])
          parent-attributes
          (when parent
            (try
              (java.nio.file.Files/readAttributes
               parent java.nio.file.attribute.BasicFileAttributes nofollow)
              (catch java.nio.channels.ClosedByInterruptException interrupted
                (.interrupt (Thread/currentThread))
                (throw interrupted))
              (catch java.io.InterruptedIOException interrupted
                (.interrupt (Thread/currentThread))
                (throw interrupted))
              (catch java.io.IOException _ nil)
              (catch SecurityException _ nil)))
          parent-real
          (when parent-attributes
            (try
              (.toRealPath parent (make-array java.nio.file.LinkOption 0))
              (catch java.nio.channels.ClosedByInterruptException interrupted
                (.interrupt (Thread/currentThread))
                (throw interrupted))
              (catch java.io.InterruptedIOException interrupted
                (.interrupt (Thread/currentThread))
                (throw interrupted))
              (catch java.io.IOException _ nil)
              (catch SecurityException _ nil)))
          collision?
          (or (java.nio.file.Files/exists destination nofollow)
              (java.nio.file.Files/isSymbolicLink destination))
          non-symlink-ancestors?
          (loop [ancestor parent]
            (cond
              (nil? ancestor) false
              (java.nio.file.Files/isSymbolicLink ancestor) false
              (= root ancestor) true
              (not (.startsWith ancestor root)) false
              :else (recur (.getParent ancestor))))]
      (when-not
       (and (.startsWith destination target-root)
            (not= destination target-root)
            parent parent-attributes (.isDirectory parent-attributes)
            parent-real (= parent parent-real)
            non-symlink-ancestors?
            (not collision?))
        (p15-s23-c-backend-fail!
         "C14-INPUT" source-path {}
         {:missing-fact :collision-free-repository-target-bundle-directory
          :source-target :jvm
          :requested-target :c
          :bounded-reason
          (cond
            collision? :output-collision
            (nil? parent-attributes) :missing-output-parent
            (not= parent parent-real) :symlink-output-ancestor
            (not non-symlink-ancestors?) :untrusted-output-ancestor
            :else :output-outside-repository-target)}))
      (.toString destination))))

(defn- p18-t04-verified-mir-c-source-snapshot-fail!
  [source-path missing-fact bounded-reason extra]
  (p15-s23-c-backend-fail!
   "C14-INPUT" source-path {}
   (merge {:missing-fact missing-fact
           :source-target :jvm :requested-target :c
           :bounded-reason bounded-reason}
          extra)))

(def ^:private p18-t04-darwin-stat-byte-count 144)
(def ^:private p18-t04-darwin-at-fdcwd -2)
(def ^:private p18-t04-darwin-at-symlink-nofollow-any 0x0800)
(def ^:private p18-t04-darwin-open-read-nofollow-flags 0x21000000)
(def ^:private p18-t04-darwin-f-getpath 50)
(def ^:private p18-t04-darwin-s-ifmt 0xf000)