(ns gravity.self-hosting.sh07-c13-mir-optimization-shape-preflight-test
  "Moving-source C13 shape admission only.

  This lane reads the Gravity source as data.  It does not load the bootstrap,
  compile an artifact, resolve a plan, execute a function, or make any
  authority claim."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")

;; The moving source may grow, but this shape gate still admits one explicit
;; C13 public surface.  Keep this list synchronized with the source contract:
;; the final three names are the evidence-boundary exports added by C13.
(def ^:private expected-export-names
  '[c13-mir-optimization-contract
    c13-optimization-pass-contract
    c13-invalidation-ledger-contract
    c13-preservation-contract
    c13-proof-certificate-contract
    c13-safety-preservation-contract
    c13-nondeterminism-contract
    c13-domain-optimization-contract
    c13-optimization-diagnostic-catalog
    build-c13-pass-contract
    build-c13-decision-record
    build-c13-invalidation-entry
    build-c13-check-elision-gate
    c13-build-bounded-identity-optimized-mir
    verify-c13-mir-optimization-passes
    sh16-optimization-policy
    sh16-optimize
    sh16-verify
    sh16-c13-evidence-input-valid?
    sh16-build-c13-evidence-boundary
    sh16-verify-c13-evidence-boundary])

(def ^:dynamic *source-snapshot-loader*
  "Test seam for byte snapshots; production leaves nil."
  nil)

(def ^:dynamic *snapshot-before-open-hook*
  "Test seam invoked after the initial path observation and before opening."
  nil)

(def ^:private reader-eof-marker (Object.))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c13_mir_optimization_shape_preflight_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C13 shape preflight is not on the classpath"
        {:id "SH07-C13-SHAPE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C13-SHAPE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- source-path
  []
  (.resolve @root source-relative-path))

(defn- failure
  ([id message]
   (failure id message {}))
  ([id message data]
   (throw (ex-info message (merge {:id id} data)))))

(def ^:private no-follow-link-options
  (into-array java.nio.file.LinkOption
              [java.nio.file.LinkOption/NOFOLLOW_LINKS]))

(defn- basic-attributes
  [path]
  (let [attributes
        (java.nio.file.Files/readAttributes
         path java.nio.file.attribute.BasicFileAttributes
         no-follow-link-options)
        owner
        (try
          (when-let [view
                     (java.nio.file.Files/getFileAttributeView
                      path java.nio.file.attribute.FileOwnerAttributeView
                      no-follow-link-options)]
            (.getOwner view))
          (catch UnsupportedOperationException _ nil))]
    {:path path
     :regular? (.isRegularFile attributes)
     :directory? (.isDirectory attributes)
     :symlink? (java.nio.file.Files/isSymbolicLink path)
     :file-key (.fileKey attributes)
     :size (.size attributes)
     :modified (.lastModifiedTime attributes)
     :owner owner}))

(defn- attributes-signature
  [attributes]
  (select-keys attributes [:regular? :directory? :symlink? :file-key
                           :size :modified :owner]))

(defn- same-file-state?
  [left right]
  (= (attributes-signature left) (attributes-signature right)))

(defn- relative-path-components
  [relative]
  (let [path (java.nio.file.Paths/get relative (make-array String 0))]
    (when (.isAbsolute path)
      (failure "SH07-C13-SHAPE-PATH-CONTAINMENT"
               "C13 source path must be relative"))
    (when (some #(= ".." (str %)) (iterator-seq (.iterator path)))
      (failure "SH07-C13-SHAPE-PATH-CONTAINMENT"
               "C13 source path escapes repository root"))
    path))

(defn- safe-contained-source-path
  "Observe every source path component without following symlinks.

  Rechecking these states around the read is a pathname-integrity
  check, not a held-descriptor or SecureDirectoryStream race proof."
  [repository relative]
  (let [root-path (.toAbsolutePath (.normalize ^java.nio.file.Path repository))
        relative-path (relative-path-components relative)
        root-state (basic-attributes root-path)]
    (when (or (:symlink? root-state) (not (:directory? root-state)))
      (failure "SH07-C13-SHAPE-PATH-CONTAINMENT"
               "C13 repository root must be a non-symlink directory"))
    (when (nil? (:file-key root-state))
      (failure "SH07-C13-SHAPE-PATH-IDENTITY"
               "C13 repository root file identity is unavailable"))
    (loop [index 0
           current root-path
           states [root-state]]
      (if (= index (.getNameCount relative-path))
        {:path current
         :root root-path
         :relative relative
         :components states}
        (let [next (.resolve current (.getName relative-path index))
              state (basic-attributes next)
              final? (= index (dec (.getNameCount relative-path)))]
          (when (nil? (:file-key state))
            (failure "SH07-C13-SHAPE-PATH-IDENTITY"
                     "C13 source path component identity is unavailable"))
          (when (or (:symlink? state)
                    (if final?
                      (not (:regular? state))
                      (not (:directory? state))))
            (failure "SH07-C13-SHAPE-PATH-CONTAINMENT"
                     "C13 source has an unsafe path component"))
          (recur (inc index) next (conj states state)))))))

(defn- read-source-snapshot
  "Read one stable-path snapshot.\n\n  The final path and each intermediate component are nofollow regular\n  observations before/open/after, with an EOF growth probe.  This is a\n  pathname-integrity check, not an adversarial open-descriptor identity proof."
  [location]
  (let [path (:path location) state-before (basic-attributes path)]
    (when (or (:symlink? state-before) (not (:regular? state-before)))
      (failure "SH07-C13-SHAPE-SOURCE-FILE" "C13 source must be a regular non-symlink file"))
    (when (nil? (:file-key state-before))
      (failure "SH07-C13-SHAPE-SOURCE-IDENTITY" "C13 source file identity is unavailable"))
    (when *snapshot-before-open-hook* (*snapshot-before-open-hook*))
    (let [location-open (when (and (:root location) (:relative location))
                          (safe-contained-source-path (:root location) (:relative location)))]
      (when (and (:components location) (not= (:components location) (:components location-open)))
        (failure "SH07-C13-SHAPE-SOURCE-MUTATED" "C13 source path components changed before open"))
      (with-open [channel (java.nio.channels.FileChannel/open
                            path
                            (into-array
                              java.nio.file.OpenOption
                              [java.nio.file.StandardOpenOption/READ
                               java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
        (let [state-open (basic-attributes path) channel-size-before (.size channel)]
          (when (or
                  (not (same-file-state? state-before state-open))
                  (not= channel-size-before (:size state-before)))
            (failure "SH07-C13-SHAPE-SOURCE-MUTATED" "C13 source changed before descriptor read"))
          (let [buffer (java.nio.ByteBuffer/allocate (Math/toIntExact (inc channel-size-before)))]
            (loop [zero-reads 0]
              (if (= (.position buffer) (.capacity buffer))
                nil
                (let [read-count (.read channel buffer)]
                  (cond
                    (= -1 read-count) nil
                    (and (zero? read-count) (< zero-reads 2)) (recur (inc zero-reads))
                    (zero? read-count) (failure
                                         "SH07-C13-SHAPE-SOURCE-READ"
                                         "C13 source channel made no progress")
                    :else (recur 0)))))
            (let [observed-bytes (.position buffer)
                  channel-size-after (.size channel)
                  state-after (basic-attributes path)
                  location-after (when (and (:root location) (:relative location))
                                   (safe-contained-source-path
                                     (:root location)
                                     (:relative location)))]
              (when (or
                      (not= channel-size-before channel-size-after)
                      (not= channel-size-before observed-bytes)
                      (not (same-file-state? state-before state-after))
                      (and
                        (:components location)
                        (not= (:components location) (:components location-after))))
                (failure
                  "SH07-C13-SHAPE-SOURCE-MUTATED"
                  "C13 source changed during descriptor read"
                  {:observed-bytes observed-bytes}))
              (.flip buffer)
              (let [bytes (byte-array observed-bytes)]
                (.get buffer bytes)
                {:path path,
                 :bytes bytes,
                 :file-key (:file-key state-before),
                 :size observed-bytes,
                 :modified (:modified state-before),
                 :owner (:owner state-before),
                 :attributes (attributes-signature state-before)}))))))))


(defn- strict-utf8
  [bytes]
  (let [decoder
        (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
          (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
          (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (java.nio.ByteBuffer/wrap bytes)))
      (catch java.nio.charset.CharacterCodingException exception
        (throw
         (ex-info
          "C13 source is not strict UTF-8"
          {:id "SH07-C13-SHAPE-SOURCE-UTF8"}
          exception))))))

(defn- load-source-snapshot
  "Load bytes from a stable-path snapshot; injected bytes remain non-authoritative."
  []
  (let [loaded (if *source-snapshot-loader*
                 (*source-snapshot-loader*)
                 (read-source-snapshot (safe-contained-source-path @root source-relative-path)))
        bytes (:bytes loaded)]
    (when-not (instance? (Class/forName "[B") bytes)
      (failure "SH07-C13-SHAPE-SOURCE-BYTES" "C13 source snapshot did not provide byte data"))
    (assoc loaded :bytes bytes :text (strict-utf8 bytes) :size (alength ^bytes bytes))))


(def ^:private source-snapshot-cache
  (delay (load-source-snapshot)))

(defn- source-snapshot
  []
  @source-snapshot-cache)





(defn- lexical-delimiter-depth!
  "Validate source delimiters before invoking the recursive host reader.\n\n  Strings, regexes, comments, and character literals are skipped\n  conservatively.  This is a lexical guard, not a replacement for reading."
  [source]
  (let [length (.length ^String source) matching-delimiter {\( \), \[ \], \{ \}} closing #{\) \] \}}]
    (loop [index 0 delimiters [] mode :normal escaped? false]
      (if (= index length)
        (do
          (when-not (#{:normal :comment} mode)
            (failure
              "SH07-C13-SHAPE-LEXICAL-DELIMITERS"
              "C13 source has an unterminated lexical literal"))
          (when (seq delimiters)
            (failure "SH07-C13-SHAPE-LEXICAL-DELIMITERS" "C13 source has unclosed delimiters"))
          true)
        (let [character (.charAt ^String source index)
              next-character (when (< (inc index) length) (.charAt ^String source (inc index)))]
          (case
            mode
            :comment
            (if (or (= character \newline) (= character \return))
              (recur (inc index) delimiters :normal false)
              (recur (inc index) delimiters :comment false))
            :string
            (cond
              escaped? (recur (inc index) delimiters :string false)
              (= character \\) (recur (inc index) delimiters :string true)
              (= character \") (recur (inc index) delimiters :normal false)
              :else (recur (inc index) delimiters :string false))
            :regex
            (cond
              escaped? (recur (inc index) delimiters :regex false)
              (= character \\) (recur (inc index) delimiters :regex true)
              (= character \") (recur (inc index) delimiters :normal false)
              :else (recur (inc index) delimiters :regex false))
            :character
            (recur (inc index) delimiters :normal false)
            (cond
              (= character \;) (recur (inc index) delimiters :comment false)
              (= character \") (recur (inc index) delimiters :string false)
              (and (= character \#) (= next-character \")) (recur
                                                             (+ index 2)
                                                             delimiters
                                                             :regex
                                                             false)
              (= character \\) (recur (inc index) delimiters :character false)
              (contains? matching-delimiter character) (let [next-delimiters
                                                             (conj delimiters character)]
                                                         (recur
                                                           (inc index)
                                                           next-delimiters
                                                           :normal
                                                           false))
              (contains? closing character) (if (or
                                                  (empty? delimiters)
                                                  (not=
                                                    character
                                                    (matching-delimiter (peek delimiters))))
                                              (failure
                                                "SH07-C13-SHAPE-LEXICAL-DELIMITERS"
                                                "C13 source has an unmatched or mismatched delimiter")
                                              (recur (inc index) (pop delimiters) :normal false))
              (Character/isWhitespace character) (recur (inc index) delimiters :normal false)
              :else (recur (inc index) delimiters :normal false))))))))


(defn- read-forms [text]
  (binding [*read-eval* false]
    (lexical-delimiter-depth! text)
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
      (loop [forms []]
        (let [form (read {:eof reader-eof-marker} reader)]
          (if (identical? reader-eof-marker form) forms (recur (conj forms form))))))))




(defn- reader-children [value]
  (cond
    (map? value) (vec (mapcat (fn [[key item]] [key item]) value))
    (coll? value) (vec value)
    :else []))


(defn- reader-tree-status [forms]
  (loop [frontier (list forms)]
    (if (empty? frontier)
      {:lazy? false}
      (let [value (peek frontier) remaining (pop frontier)]
        (if (instance? clojure.lang.LazySeq value)
          {:lazy? true}
          (recur (into remaining (reader-children value))))))))


(defn- ns-form?
  [form]
  (and (seq? form)
       (= 'ns (first form))
       (symbol? (second form))
       (not (clojure.string/blank? (name (second form))))
       (every? #(and (seq? %)
                     (keyword? (first %)))
               (drop 2 form))))

(defn- exports-clauses
  [ns-form]
  (filter #(and (seq? %)
                (= :exports (first %)))
          (drop 2 ns-form)))

(defn- unique-namespace-clause
  [ns-form key]
  (let [clauses (vec (filter #(and (seq? %)
                                   (= key (first %)))
                             (drop 2 ns-form)))]
    (when (= 1 (count clauses))
      (first clauses))))

(defn- exports-vector
  [ns-form]
  (let [clauses (vec (exports-clauses ns-form))]
    (when (and (= 1 (count clauses))
               (= 2 (count (first clauses)))
               (vector? (second (first clauses))))
      (second (first clauses)))))

(defn- top-level-definitions
  [forms]
  (->> forms
       (keep (fn [form]
               (when (and (seq? form)
                          (#{'def 'defn} (first form))
                          (symbol? (second form)))
                 [(second form) form])))
       (into {})))

(defn- duplicate-definition-names
  [forms]
  (->> forms
       (keep (fn [form]
               (when (and (seq? form)
                          (#{'def 'defn} (first form))
                          (symbol? (second form)))
                 (second form))))
       frequencies
       (keep (fn [[name frequency]]
               (when (> frequency 1) name)))
       set))

(defn- if-shapes [forms]
  (let [bad (atom [])]
    (loop [frontier (list forms)]
      (if (seq frontier)
        (let [value (peek frontier) frontier (pop frontier)]
          (when (and (seq? value) (= 'if (first value)))
            (when-not (= 4 (count value)) (swap! bad conj value)))
          (recur (into frontier (reader-children value))))
        @bad))))


(defn- let-shapes
  "Check that every let has a binding vector and at least one body form."
  [forms]
  (let [bad (atom [])]
    (loop [frontier (list forms)]
      (if (seq frontier)
        (let [value (peek frontier) frontier (pop frontier)]
          (when (and (seq? value) (= 'let (first value)))
            (when-not (and
                        (>= (count value) 3)
                        (vector? (second value))
                        (even? (count (second value))))
              (swap! bad conj value)))
          (recur (into frontier (reader-children value))))
        @bad))))


(defn- parse-source-shape []
  (let [{:keys [text], :as snapshot} (source-snapshot)
        forms (read-forms text)
        tree (reader-tree-status forms)
        ns-form (first forms)
        exports (exports-vector ns-form)]
    (assoc
      snapshot
      :forms
      forms
      :tree
      tree
      :ns-form
      ns-form
      :exports
      exports
      :if-shape-errors
      (if-shapes forms)
      :let-shape-errors
      (let-shapes forms))))


(def ^:private parsed-source-cache
  (delay (parse-source-shape)))

(defn- parse-shape
  []
  @parsed-source-cache)

(def ^:private non-authority-note
  "This moving-source shape check is non-authoritative: it does not compile, verify, lower, or execute C13.")

(deftest
  sh07-c13-mir-optimization-source-shape-and-control
  (testing
    non-authority-note
    (let [{:keys [forms ns-form tree if-shape-errors let-shape-errors]} (parse-shape)]
      (is (seq forms))
      (let [namespace-forms (filter #(and (seq? %) (= 'ns (first %))) forms)]
        (is (= 1 (count namespace-forms)))
        (is (= (first forms) (first namespace-forms))))
      (is (ns-form? ns-form))
      (is (= '(:profile :meta) (unique-namespace-clause ns-form :profile)))
      (is (= '(:target :jvm) (unique-namespace-clause ns-form :target)))
      (is (= '(:effects #{}) (unique-namespace-clause ns-form :effects)))
      (is (= '(:capabilities #{}) (unique-namespace-clause ns-form :capabilities)))
      (is (= '(:safety :safe) (unique-namespace-clause ns-form :safety)))
      (is (= 1 (count (exports-clauses ns-form))))
      (is (= 2 (count (first (exports-clauses ns-form)))))
      (is (= (set expected-export-names) (set (exports-vector ns-form))))
      (is (not (:lazy? tree)))
      (is (empty? if-shape-errors))
      (is (empty? let-shape-errors))
      (testing
        "lexical guard handles strings, regexes, comments, and chars"
        (is (true? (lexical-delimiter-depth! "(\"}\" #\"[\" ; )\n \\( )"))))
      (testing
        "reader evaluation is rejected; deep delimiters remain valid"
        (is (thrown? Throwable (read-forms "#=(+ 1 2)")))
        (is
          (true?
            (lexical-delimiter-depth!
              (str (apply str (repeat 257 "(")) (apply str (repeat 257 ")")))))))
      (testing
        "lexical malformed delimiters and literals fail before reading"
        (doseq [invalid [")" "([)]" "(" "\"unterminated" "#\"unterminated" "\\"]]
          (is (thrown? clojure.lang.ExceptionInfo (lexical-delimiter-depth! invalid)))))
      (testing
        "wide collections remain valid; duplicate definitions remain visible"
        (is (= 1025 (count (reader-children (vec (repeat 1025 :x))))))
        (is
          (=
            '#{hidden}
            (duplicate-definition-names '[(def hidden 1) (def hidden 2) (def exported 1)]))))
      (testing
        "injected bytes are decoded, not injected source text"
        (let [text "(ns synthetic (:exports [x])) (def x 1)"
              bytes (.getBytes text "UTF-8")
              loaded (binding [*source-snapshot-loader* (fn [] {:bytes bytes, :text "wrong text"})]
                       (load-source-snapshot))]
          (is (= text (:text loaded)))))
      (testing
        "path replacement is rejected by the explicit race seam"
        (let [temporary (java.nio.file.Files/createTempDirectory
                          "sh07-c13-shape-race"
                          (make-array java.nio.file.attribute.FileAttribute 0))
              source (.resolve temporary "source.gravity")
              replacement (.resolve temporary "replacement.gravity")
              moved (.resolve temporary "moved.gravity")]
          (try
            (java.nio.file.Files/write
              source
              (.getBytes "(ns source)" "UTF-8")
              (make-array java.nio.file.OpenOption 0))
            (java.nio.file.Files/write
              replacement
              (.getBytes "(ns replacement)" "UTF-8")
              (make-array java.nio.file.OpenOption 0))
            (let [location (safe-contained-source-path temporary "source.gravity")]
              (binding [*snapshot-before-open-hook* (fn []
                                                      (java.nio.file.Files/move
                                                        source
                                                        moved
                                                        (make-array java.nio.file.CopyOption 0))
                                                      (java.nio.file.Files/move
                                                        replacement
                                                        source
                                                        (make-array java.nio.file.CopyOption 0)))]
                (is (thrown? clojure.lang.ExceptionInfo (read-source-snapshot location)))))
            (finally
              (java.nio.file.Files/deleteIfExists source)
              (java.nio.file.Files/deleteIfExists replacement)
              (java.nio.file.Files/deleteIfExists moved)
              (java.nio.file.Files/deleteIfExists temporary))))))))


(deftest sh07-c13-source-snapshot-has-no-byte-quota
  (let [directory (java.nio.file.Files/createTempDirectory
                   "sh07-c13-source-snapshot"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        source (.resolve directory "source.gravity")
        text (str "(ns snapshot)\n;" (apply str (repeat 600000 "x")) "\n")
        bytes (.getBytes text "UTF-8")]
    (try
      (java.nio.file.Files/write source bytes (make-array java.nio.file.OpenOption 0))
      (let [snapshot (read-source-snapshot
                      (safe-contained-source-path directory "source.gravity"))
            decoded (binding [*source-snapshot-loader* (fn [] snapshot)]
                      (load-source-snapshot))]
        (is (= (alength bytes) (:size snapshot)))
        (is (= (seq bytes) (seq (:bytes snapshot))))
        (is (= text (:text decoded))))
      (finally
        (java.nio.file.Files/deleteIfExists source)
        (java.nio.file.Files/deleteIfExists directory)))))

(deftest sh07-c13-mir-optimization-export-completeness
  (testing non-authority-note
    (let [{:keys [forms ns-form exports]} (parse-shape)
          definitions (top-level-definitions forms)
          defn-forms (filter #(and (seq? %)
                                   (= 'defn (first %))) forms)]
      (is (vector? exports))
      (is (every? symbol? exports))
      (is (every? #(not (clojure.string/blank? (name %))) exports))
      (is (= (count exports) (count (set exports))))
      (is (= (set expected-export-names) (set exports)))
      (is (every? #(contains? definitions %) exports))
      (is (empty? (duplicate-definition-names forms)))
      (is (every? #(and (= 4 (count %))
                        (symbol? (second %))
                        (vector? (nth % 2))
                        (some? (nth % 3)))
                  defn-forms))
      (is (= 'gravity.compiler.c13-mir-optimization-passes
             (second ns-form))))))
