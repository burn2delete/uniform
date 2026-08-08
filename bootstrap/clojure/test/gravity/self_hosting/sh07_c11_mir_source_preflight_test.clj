(ns gravity.self-hosting.sh07-c11-mir-source-preflight-test
  "Bounded, source-only C11 MIR preflight.

  The three fixed selectors share one authenticated source snapshot.  The
  preflight deliberately stops at reader data: it does not invoke compiler,
  artifact, or authority code.  All structural walks are iterative and have
  explicit limits so malformed source cannot turn a cheap gate into an
  unbounded allocation or a stack overflow."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing thrown?]]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c11_mir_source_preflight_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C11 source preflight is not on the classpath"
        {:id "SH07-C11-PREFLIGHT-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C11-PREFLIGHT-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")
(def ^:private expected-source-byte-count 113008)
(def ^:private expected-source-revision-id
  "sha256:95fd82d9484d0a1b7a93b3da10ed6c490c7b051e253da0eb1eb58f0f08334fe3")
(def ^:private invalid-export-clause ::invalid-export-clause)
(def ^:private invalid-definition-duplicates ::invalid-definition-duplicates)

;; These limits are intentionally independent of the moving C10 export list.
;; The current C11 source is substantially below each bound.  The byte bound
;; is one byte over the pinned size so a changed file is still authenticated by
;; the exact SHA check rather than being read without a finite ceiling.
(def ^:private maximum-source-bytes (inc expected-source-byte-count))
(def ^:private maximum-top-level-forms 4096)
(def ^:private maximum-form-nodes 250000)
(def ^:private maximum-form-depth 512)
(def ^:private maximum-collection-width 1024)
(def ^:private maximum-diagnostics 64)
(def ^:private maximum-diagnostic-message-bytes 256)

(def ^:dynamic *source-snapshot-loader*
  "Test seam for a pre-authenticated snapshot map.  Production leaves nil."
  nil)

(def ^:dynamic *snapshot-before-open-hook*
  "Test seam invoked after initial stat and before descriptor opening."
  nil)

(defn- bounded-message
  [message]
  (let [value (str message)]
    (if (<= (count value) maximum-diagnostic-message-bytes)
      value
      (str (subs value 0 (- maximum-diagnostic-message-bytes 3)) "..."))))

(defn- failure
  ([id message]
   (failure id message {}))
  ([id message data]
   (throw (ex-info (bounded-message message)
                   (merge {:id id} (dissoc data :bytes :source-text))))))

(def ^:private no-follow-link-options
  (into-array java.nio.file.LinkOption
              [java.nio.file.LinkOption/NOFOLLOW_LINKS]))

(defn- basic-file-state
  [path]
  (let [attributes
        (java.nio.file.Files/readAttributes
         path java.nio.file.attribute.BasicFileAttributes
         no-follow-link-options)
        owner
        (try
          (when-let [view
                     (java.nio.file.Files/getFileAttributeView
                      path
                      java.nio.file.attribute.FileOwnerAttributeView
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

(defn- same-file-state?
  [left right]
  (and (= (:regular? left) (:regular? right))
       (= (:directory? left) (:directory? right))
       (= (:symlink? left) (:symlink? right))
       (= (:file-key left) (:file-key right))
       (= (:size left) (:size right))
       (= (:modified left) (:modified right))
       (= (:owner left) (:owner right))))

(defn- relative-path-components
  [relative]
  (let [path (java.nio.file.Paths/get relative (make-array String 0))]
    (when (.isAbsolute path)
      (failure "SH07-C11-PREFLIGHT-PATH-CONTAINMENT"
               "C11 source path must be relative"))
    (when (some #(= ".." (str %)) (iterator-seq (.iterator path)))
      (failure "SH07-C11-PREFLIGHT-PATH-CONTAINMENT"
               "C11 source path escapes repository root"))
    path))

(defn- safe-contained-source-path
  "Resolve a source path while rejecting symlinked final/intermediate parts.

  The returned map includes every component's descriptor state.  The caller
  compares those states before and after its descriptor read, closing the
  parent replacement window that a final-path check alone would leave."
  [repository relative]
  (let [root-path (.toAbsolutePath (.normalize ^java.nio.file.Path repository))
        relative-path (relative-path-components relative)
        root-state (basic-file-state root-path)]
    (when (or (:symlink? root-state) (not (:directory? root-state)))
      (failure "SH07-C11-PREFLIGHT-PATH-CONTAINMENT"
               "C11 repository root must be a non-symlink directory"))
    (when (nil? (:file-key root-state))
      (failure "SH07-C11-PREFLIGHT-PATH-IDENTITY"
               "C11 repository root identity is unavailable"))
    (loop [index 0
           current root-path
           states [root-state]]
      (if (= index (.getNameCount relative-path))
        {:path current
         :root root-path
         :relative relative
         :components states}
        (let [next (.resolve current (.getName relative-path index))
              state (basic-file-state next)
              final? (= index (dec (.getNameCount relative-path)))]
          (when (nil? (:file-key state))
            (failure "SH07-C11-PREFLIGHT-PATH-IDENTITY"
                     "C11 source path component identity is unavailable"))
          (when (or (:symlink? state)
                    (if final?
                      (not (:regular? state))
                      (not (:directory? state))))
            (failure "SH07-C11-PREFLIGHT-PATH-CONTAINMENT"
                     "C11 source has an unsafe path component"))
          (recur (inc index) next (conj states state)))))))

(defn- sha256-id
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        ^bytes bytes)]
    (str
     "sha256:"
     (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- strict-utf8
  [bytes source-path]
  (let [decoder
        (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
          (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
          (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (java.nio.ByteBuffer/wrap ^bytes bytes)))
      (catch java.nio.charset.CharacterCodingException error
        (throw
         (ex-info
          "C11 source is not strict UTF-8"
          {:id "SH07-C11-PREFLIGHT-SOURCE-UTF8"
           :source-path (str source-path)}
          error))))))

(defn- read-source-snapshot
  "Read one bounded, no-follow descriptor snapshot and authenticate it.

  Path metadata is sampled before opening, immediately after opening, and
  after the bounded channel read.  The decoded text and digest are derived
  only from those exact bytes."
  [source-path]
  (let [location (if (map? source-path)
                   source-path
                   {:path (.toPath (io/file source-path))})
        path (:path location)
        component-states (:components location)
        state-before (basic-file-state path)]
    (when (or (:symlink? state-before) (not (:regular? state-before)))
      (failure "SH07-C11-PREFLIGHT-SOURCE-FILE"
               "C11 source must be a regular non-symlink file"
               {:source-path (str path)}))
    (when (nil? (:file-key state-before))
      (failure "SH07-C11-PREFLIGHT-SOURCE-IDENTITY"
               "C11 source file identity is unavailable"
               {:source-path (str path)}))
    (when (> (:size state-before) maximum-source-bytes)
      (failure "SH07-C11-PREFLIGHT-SOURCE-BYTES"
               "C11 source exceeds bounded input"
               {:source-path (str path)
                :observed (:size state-before)
                :maximum maximum-source-bytes}))
    (let [open-options
          (into-array java.nio.file.OpenOption
                      [java.nio.file.StandardOpenOption/READ
                       java.nio.file.LinkOption/NOFOLLOW_LINKS])]
      (when *snapshot-before-open-hook*
        (*snapshot-before-open-hook*))
      (let [location-open
            (when (and (:root location) (:relative location))
              (safe-contained-source-path (:root location)
                                          (:relative location)))]
        (when (and component-states
                   (not= component-states (:components location-open)))
          (failure "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
                   "C11 source path components changed before descriptor read"))
      (with-open [channel (java.nio.channels.FileChannel/open path open-options)]
        (let [state-open (basic-file-state path)
              channel-size-before (.size channel)]
          (when (or (not (same-file-state? state-before state-open))
                    (not= channel-size-before (:size state-before)))
            (failure "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
                     "C11 source changed before descriptor read"
                     {:source-path (str path)}))
          (let [buffer (java.nio.ByteBuffer/allocate maximum-source-bytes)]
            (loop [zero-reads 0]
              (if (= (.position buffer) maximum-source-bytes)
                nil
                (let [read-count (.read channel buffer)]
                  (cond
                    (= -1 read-count) nil
                    (and (zero? read-count) (< zero-reads 2))
                    (recur (inc zero-reads))
                    (zero? read-count)
                    (failure "SH07-C11-PREFLIGHT-SOURCE-READ"
                             "C11 source channel made no progress")
                    :else
                    (recur 0)))))
            (let [observed-bytes (.position buffer)
                  channel-size-after (.size channel)
                  state-after (basic-file-state path)
                  location-after
                  (when (and (:root location) (:relative location))
                    (safe-contained-source-path (:root location)
                                                (:relative location)))]
              (when (or (>= observed-bytes maximum-source-bytes)
                        (not= channel-size-before channel-size-after)
                        (not= channel-size-before observed-bytes)
                        (not (same-file-state? state-before state-after))
                        (and component-states
                             (not= component-states
                                   (:components location-after))))
                (failure "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
                         "C11 source changed during bounded descriptor read"
                         {:source-path (str path)
                          :observed-bytes observed-bytes}))
              (.flip buffer)
              (let [bytes (byte-array observed-bytes)]
                (.get buffer bytes)
                {:bytes bytes
                 :source-text (strict-utf8 bytes path)
                 :source-sha256 (sha256-id bytes)
                 :file-key (:file-key state-before)
                 :size observed-bytes
                 :modified (:modified state-before)
                 :owner (:owner state-before)})))))))))

(defn- source-path
  []
  (:path (safe-contained-source-path @root source-relative-path)))

(defn- load-source-snapshot
  []
  (let [snapshot
        (if *source-snapshot-loader*
          (*source-snapshot-loader*)
          (read-source-snapshot
           (safe-contained-source-path @root source-relative-path)))
        bytes (:bytes snapshot)]
    (when (or (not (bytes? bytes))
              (not= expected-source-byte-count (alength ^bytes bytes)))
      (failure "SH07-C11-PREFLIGHT-SOURCE-BINDING"
               "C11 source byte count does not match the authenticated pin"))
    (when (not= expected-source-revision-id (sha256-id bytes))
      (failure "SH07-C11-PREFLIGHT-SOURCE-BINDING"
               "C11 source digest does not match the authenticated pin"))
    (assoc snapshot :authenticated? true)))

(def ^:private source-snapshot (delay (load-source-snapshot)))

(defn- source-bytes
  []
  ;; Return a copy so callers cannot mutate the process-local authenticated
  ;; snapshot held by the delay.
  (aclone ^bytes (:bytes @source-snapshot)))

(defn- source-text
  []
  (:source-text @source-snapshot))

(defn- bounded-elements
  [value maximum id message]
  (loop [remaining (seq value)
         result []]
    (cond
      (nil? remaining) result
      (> (count result) maximum)
      (failure id message {:maximum maximum})
      :else (recur (next remaining) (conj result (first remaining))))))

(defn- read-forms
  [source]
  (binding [*read-eval* false]
    (with-open [reader
                (clojure.lang.LineNumberingPushbackReader.
                 (java.io.StringReader. ^String source))]
      (loop [forms []]
        (let [form (read {:eof ::eof} reader)]
          (if (= ::eof form)
            forms
            (do
              (when (>= (count forms) maximum-top-level-forms)
                (failure "SH07-C11-PREFLIGHT-FORM-COUNT"
                         "C11 source has too many top-level forms"
                         {:maximum maximum-top-level-forms}))
              (recur (conj forms form)))))))))

(def ^:private source-forms-cache
  (delay (read-forms (source-text))))

(defn- source-forms
  []
  @source-forms-cache)

(defn- form-children
  [form]
  (if (coll? form)
    (bounded-elements form maximum-collection-width
                      "SH07-C11-PREFLIGHT-COLLECTION-WIDTH"
                      "C11 source collection is wider than the bounded gate")
    []))

(defn- analyze-form-sequence
  "Iteratively inspect forms and return bounded shape diagnostics."
  [forms]
  (let [roots (bounded-elements forms maximum-top-level-forms
                                "SH07-C11-PREFLIGHT-FORM-COUNT"
                                "C11 source has too many top-level forms")]
    (loop [pending (reduce (fn [stack form]
                             (conj stack [form 0]))
                           []
                           (reverse roots))
           node-count 0
           invalid-if-forms []
           duplicate-diagnostics []
           maximum-depth-seen 0]
      (if (empty? pending)
        {:forms roots
         :node-count node-count
         :maximum-depth maximum-depth-seen
         :invalid-if-forms invalid-if-forms
         :diagnostics duplicate-diagnostics}
        (let [[form depth] (peek pending)
              pending (pop pending)]
          (when (>= node-count maximum-form-nodes)
            (failure "SH07-C11-PREFLIGHT-FORM-NODES"
                     "C11 source has too many recursive form nodes"
                     {:maximum maximum-form-nodes}))
          (when (> depth maximum-form-depth)
            (failure "SH07-C11-PREFLIGHT-FORM-DEPTH"
                     "C11 source exceeds bounded form depth"
                     {:maximum maximum-form-depth}))
          (let [children (form-children form)
                invalid-if?
                (and (seq? form)
                     (= 'if (first form))
                     (not= 4 (count children)))
                invalid-if-forms
                (if invalid-if?
                  (do
                    (when (>= (count invalid-if-forms) maximum-diagnostics)
                      (failure "SH07-C11-PREFLIGHT-DIAGNOSTICS"
                               "C11 source produced too many diagnostics"
                               {:maximum maximum-diagnostics}))
                    (conj invalid-if-forms form))
                  invalid-if-forms)
                child-depth (inc depth)
                pending
                (reduce (fn [stack child]
                          (conj stack [child child-depth]))
                        pending
                        (reverse children))]
            (recur pending
                   (inc node-count)
                   invalid-if-forms
                   duplicate-diagnostics
                   (max maximum-depth-seen depth))))))))

(def ^:private source-analysis-cache
  (delay (analyze-form-sequence (source-forms))))

(defn- invalid-source-if-forms
  [forms]
  (:invalid-if-forms (analyze-form-sequence forms)))

(defn- definition-names
  "Return a frequency map of top-level def/defn names."
  [forms]
  (reduce
   (fn [frequencies form]
     (if (and (seq? form)
              (#{'def 'defn} (first form))
              (symbol? (second form)))
       (update frequencies (second form) (fnil inc 0))
       frequencies))
   {}
   (bounded-elements forms maximum-top-level-forms
                     "SH07-C11-PREFLIGHT-FORM-COUNT"
                     "C11 source has too many top-level forms")))

(defn- duplicate-definition-names
  [forms]
  (set (keep (fn [[name frequency]]
               (when (> frequency 1) name))
             (definition-names forms))))

(defn- export-names
  [forms]
  (let [forms (bounded-elements forms maximum-top-level-forms
                                "SH07-C11-PREFLIGHT-FORM-COUNT"
                                "C11 source has too many top-level forms")
        _ (analyze-form-sequence forms)
        namespace-form (first forms)]
    (if (and (seq? namespace-form)
             (= 'ns (first namespace-form))
             (symbol? (second namespace-form)))
      (let [clauses
            (filter
             #(and (seq? %) (= :exports (first %)))
             (drop 2 namespace-form))]
        (if (= 1 (count clauses))
          (let [clause (first clauses)
                values (second clause)]
            (if (and (= 2 (count clause))
                     (vector? values)
                     (seq values)
                     (every? symbol? values)
                     (= (count values) (count (set values))))
              values
              invalid-export-clause))
          invalid-export-clause))
      invalid-export-clause)))

(defn- missing-export-definitions
  [forms]
  (let [forms (bounded-elements forms maximum-top-level-forms
                                "SH07-C11-PREFLIGHT-FORM-COUNT"
                                "C11 source has too many top-level forms")
        exports (export-names forms)
        definitions (definition-names forms)]
    (if (= invalid-export-clause exports)
      invalid-export-clause
      (if (seq (duplicate-definition-names forms))
        invalid-definition-duplicates
        (set (remove definitions exports))))))

(deftest sh07-c11-source-binding-is-exact
  (let [bytes (source-bytes)]
    (is (= expected-source-byte-count (alength bytes)))
    (is (= expected-source-revision-id (sha256-id bytes))))
  (testing "strict UTF-8 rejects malformed bytes without a platform decoder"
    (is (try
          (strict-utf8 (byte-array [(unchecked-byte 0xc3) (byte 0x28)])
                       "synthetic")
          false
          (catch clojure.lang.ExceptionInfo error
            (= "SH07-C11-PREFLIGHT-SOURCE-UTF8"
               (:id (ex-data error)))))))
  (testing "path authentication rejects containment escapes and symlink leaves"
    (let [temporary
          (java.nio.file.Files/createTempDirectory
           "sh07-c11-path-test" (make-array java.nio.file.attribute.FileAttribute 0))
          real-file (.resolve temporary "real.gravity")
          link-file (.resolve temporary "link.gravity")
          oversized-file (.resolve temporary "oversized.gravity")
          replacement-file (.resolve temporary "replacement.gravity")
          moved-original (.resolve temporary "original.gravity")]
      (try
        (java.nio.file.Files/write
         real-file
         (.getBytes "(ns synthetic (:exports [x])) (def x 1)" "UTF-8")
         (make-array java.nio.file.OpenOption 0))
        (java.nio.file.Files/write
         oversized-file
         (byte-array maximum-source-bytes)
         (make-array java.nio.file.OpenOption 0))
        (is (thrown? clojure.lang.ExceptionInfo
                     (read-source-snapshot oversized-file)))
        (is (thrown? clojure.lang.ExceptionInfo
                     (safe-contained-source-path temporary "../escape.gravity")))
        (try
          (java.nio.file.Files/createSymbolicLink
           link-file real-file
           (make-array java.nio.file.attribute.FileAttribute 0))
          (is (thrown? clojure.lang.ExceptionInfo
                       (read-source-snapshot link-file)))
          (catch java.nio.file.FileSystemException _
            (is true))
          (catch UnsupportedOperationException _
            (is true)))
        (java.nio.file.Files/write
         replacement-file
         (.getBytes "(ns replacement (:exports [x])) (def x 1)" "UTF-8")
         (make-array java.nio.file.OpenOption 0))
        (binding [*snapshot-before-open-hook*
                  (fn []
                    (java.nio.file.Files/move real-file moved-original)
                    (java.nio.file.Files/move replacement-file real-file))]
          (is (thrown? clojure.lang.ExceptionInfo
                       (read-source-snapshot real-file))))
        (finally
          (java.nio.file.Files/deleteIfExists link-file)
          (java.nio.file.Files/deleteIfExists real-file)
          (java.nio.file.Files/deleteIfExists replacement-file)
          (java.nio.file.Files/deleteIfExists moved-original)
          (java.nio.file.Files/deleteIfExists oversized-file)
          (java.nio.file.Files/deleteIfExists temporary))))))

(deftest sh07-c11-source-control-form-arities-are-exact
  (let [analysis @source-analysis-cache]
    (is (empty? (:invalid-if-forms analysis)))
    (is (empty? (duplicate-definition-names (:forms analysis))))
    (testing "both under- and over-arity if forms fail the same source-only gate"
      (is (= 1 (count (invalid-source-if-forms '[(if true)]))))
      (is (= 1
             (count
              (invalid-source-if-forms
               '[(if true :then :else :extra)])))))
    (testing "deep and wide synthetic carriers fail closed before recursion grows"
      (let [deep (reduce (fn [form _] (list form))
                         'leaf
                         (range (inc maximum-form-depth)))
            wide (apply list (cons 'if (cons true
                                              (repeat maximum-collection-width :x))))]
        (is (thrown? clojure.lang.ExceptionInfo
                     (invalid-source-if-forms [deep])))
        (is (thrown? clojure.lang.ExceptionInfo
                     (invalid-source-if-forms [wide])))))))

(deftest sh07-c11-source-exports-have-definitions
  (let [analysis @source-analysis-cache
        forms (:forms analysis)
        exports (export-names forms)]
    (is (vector? exports))
    (is (seq exports))
    (is (empty? (missing-export-definitions forms))))
  (testing "malformed namespace and export clauses fail closed"
    (doseq [forms
            ['[(not-ns example (:exports [x])) (def x 1)]
             '[(ns example (:exports [x] :trailing)) (def x 1)]
             '[(ns example (:exports [x]) (:exports [y])) (def x 1) (def y 2)]
             '[(ns example (:exports (x))) (def x 1)]
             '[(ns example (:exports [x :not-a-symbol])) (def x 1)]
             '[(ns example (:exports []))]
             '[(ns example (:exports [x x])) (def x 1)]
             '[(ns example) (def x 1)]]]
      (is (= invalid-export-clause (missing-export-definitions forms)))))
  (testing "duplicate top-level def and defn names fail closed"
    (is (= '{value 2}
           (definition-names
            '[(ns example (:exports [value]))
              (def value 1)
              (defn value [] 2)])))
    (is (= invalid-definition-duplicates
           (missing-export-definitions
            '[(ns example (:exports [value]))
              (def value 1)
              (defn value [] 2)]))))
  (testing "a structurally valid export must resolve to a top-level definition"
    (is (= '#{missing}
           (missing-export-definitions
            '[(ns example (:exports [present missing]))
              (def present 1)])))
    (is (empty?
         (missing-export-definitions
          '[(ns example (:exports [value function]))
            (def value 1)
            (defn function [] value)])))))
