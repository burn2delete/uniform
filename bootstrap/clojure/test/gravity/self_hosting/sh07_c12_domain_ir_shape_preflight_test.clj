(ns gravity.self-hosting.sh07-c12-domain-ir-shape-preflight-test
  "Moving-source C12 shape admission only.

  This lane reads the Gravity source as data.  It does not load the bootstrap,
  compile an artifact, resolve a plan, execute a function, or make any
  authority claim."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")

;; Keep this admission lane independent of the C12 compiler and bounded before
;; decoding/parsing.  512 KiB permits the moving source while remaining a
;; small source-only check.  Reader nodes, depth, collection width, lexical
;; tokens, and delimiters have separate finite bounds; traversal is iterative
;; so pathological nesting cannot consume the host call stack.
(def ^:private maximum-source-bytes (* 512 1024))
(def ^:private maximum-top-level-forms 4096)
(def ^:private maximum-reader-tree-nodes 250000)
(def ^:private maximum-reader-tree-depth 256)
(def ^:private maximum-collection-width 1024)
(def ^:private maximum-diagnostics 64)
(def ^:private maximum-lexical-tokens 250000)
(def ^:private maximum-lexical-delimiters 250000)

(def ^:dynamic *source-snapshot-loader*
  "Test seam for bounded byte snapshots; production leaves nil."
  nil)

(def ^:dynamic *snapshot-before-open-hook*
  "Test seam invoked after the initial path observation and before opening."
  nil)

(def ^:private reader-eof-marker (Object.))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c12_domain_ir_shape_preflight_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C12 shape preflight is not on the classpath"
        {:id "SH07-C12-SHAPE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C12-SHAPE-REPOSITORY-ROOT"}))
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
      (failure "SH07-C12-SHAPE-PATH-CONTAINMENT"
               "C12 source path must be relative"))
    (when (some #(= ".." (str %)) (iterator-seq (.iterator path)))
      (failure "SH07-C12-SHAPE-PATH-CONTAINMENT"
               "C12 source path escapes repository root"))
    path))

(defn- safe-contained-source-path
  "Observe every source path component without following symlinks.

  Rechecking these states around the bounded read is a pathname-integrity
  check, not a held-descriptor or SecureDirectoryStream race proof."
  [repository relative]
  (let [root-path (.toAbsolutePath (.normalize ^java.nio.file.Path repository))
        relative-path (relative-path-components relative)
        root-state (basic-attributes root-path)]
    (when (or (:symlink? root-state) (not (:directory? root-state)))
      (failure "SH07-C12-SHAPE-PATH-CONTAINMENT"
               "C12 repository root must be a non-symlink directory"))
    (when (nil? (:file-key root-state))
      (failure "SH07-C12-SHAPE-PATH-IDENTITY"
               "C12 repository root file identity is unavailable"))
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
            (failure "SH07-C12-SHAPE-PATH-IDENTITY"
                     "C12 source path component identity is unavailable"))
          (when (or (:symlink? state)
                    (if final?
                      (not (:regular? state))
                      (not (:directory? state))))
            (failure "SH07-C12-SHAPE-PATH-CONTAINMENT"
                     "C12 source has an unsafe path component"))
          (recur (inc index) next (conj states state)))))))

(defn- read-source-snapshot
  "Read one bounded stable-path snapshot.

  The final path and each intermediate component are nofollow regular
  observations before/open/after, with an EOF growth probe.  This is a
  pathname-integrity check, not an adversarial open-descriptor identity proof."
  [location]
  (let [path (:path location)
        state-before (basic-attributes path)]
    (when (or (:symlink? state-before) (not (:regular? state-before)))
      (failure "SH07-C12-SHAPE-SOURCE-FILE"
               "C12 source must be a regular non-symlink file"))
    (when (nil? (:file-key state-before))
      (failure "SH07-C12-SHAPE-SOURCE-IDENTITY"
               "C12 source file identity is unavailable"))
    (when (> (:size state-before) maximum-source-bytes)
      (failure "SH07-C12-SHAPE-SOURCE-BOUND"
               "C12 source exceeds bounded input"
               {:observed (:size state-before)
                :maximum maximum-source-bytes}))
    (when *snapshot-before-open-hook*
      (*snapshot-before-open-hook*))
    (let [location-open
          (when (and (:root location) (:relative location))
            (safe-contained-source-path (:root location) (:relative location)))]
      (when (and (:components location)
                 (not= (:components location) (:components location-open)))
        (failure "SH07-C12-SHAPE-SOURCE-MUTATED"
                 "C12 source path components changed before open"))
      (with-open [channel
                  (java.nio.channels.FileChannel/open
                   path
                   (into-array java.nio.file.OpenOption
                               [java.nio.file.StandardOpenOption/READ
                                java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
        (let [state-open (basic-attributes path)
              channel-size-before (.size channel)]
          (when (or (not (same-file-state? state-before state-open))
                    (not= channel-size-before (:size state-before)))
            (failure "SH07-C12-SHAPE-SOURCE-MUTATED"
                     "C12 source changed before descriptor read"))
          ;; Read one byte beyond the admitted ceiling.  Exact-bound files hit
          ;; EOF; a growth race is rejected without unbounded allocation.
          (let [buffer (java.nio.ByteBuffer/allocate (inc maximum-source-bytes))]
            (loop [zero-reads 0]
              (if (= (.position buffer) (.capacity buffer))
                nil
                (let [read-count (.read channel buffer)]
                  (cond
                    (= -1 read-count) nil
                    (and (zero? read-count) (< zero-reads 2))
                    (recur (inc zero-reads))
                    (zero? read-count)
                    (failure "SH07-C12-SHAPE-SOURCE-READ"
                             "C12 source channel made no progress")
                    :else
                    (recur 0)))))
            (let [observed-bytes (.position buffer)
                  channel-size-after (.size channel)
                  state-after (basic-attributes path)
                  location-after
                  (when (and (:root location) (:relative location))
                    (safe-contained-source-path (:root location)
                                                (:relative location)))]
              (when (or (> observed-bytes maximum-source-bytes)
                        (not= channel-size-before channel-size-after)
                        (not= channel-size-before observed-bytes)
                        (not (same-file-state? state-before state-after))
                        (and (:components location)
                             (not= (:components location)
                                   (:components location-after))))
                (failure "SH07-C12-SHAPE-SOURCE-MUTATED"
                         "C12 source changed during bounded descriptor read"
                         {:observed-bytes observed-bytes}))
              (.flip buffer)
              (let [bytes (byte-array observed-bytes)]
                (.get buffer bytes)
                {:path path
                 :bytes bytes
                 :file-key (:file-key state-before)
                 :size observed-bytes
                 :modified (:modified state-before)
                 :owner (:owner state-before)
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
          "C12 source is not strict UTF-8"
          {:id "SH07-C12-SHAPE-SOURCE-UTF8"}
          exception))))))

(defn- load-source-snapshot
  "Load bytes from a stable-path snapshot; injected bytes remain non-authoritative."
  []
  (let [loaded
        (if *source-snapshot-loader*
          (*source-snapshot-loader*)
          (read-source-snapshot
           (safe-contained-source-path @root source-relative-path)))
        bytes (:bytes loaded)]
    (when-not (instance? (Class/forName "[B") bytes)
      (failure "SH07-C12-SHAPE-SOURCE-BYTES"
               "C12 source snapshot did not provide byte data"))
    (when (> (alength ^bytes bytes) maximum-source-bytes)
      (failure "SH07-C12-SHAPE-SOURCE-BOUND"
               "C12 source snapshot exceeds bounded input"
               {:observed (alength ^bytes bytes)
                :maximum maximum-source-bytes}))
    (assoc loaded
           :bytes bytes
           :text (strict-utf8 bytes)
           :size (alength ^bytes bytes))))

(def ^:private source-snapshot-cache
  (delay (load-source-snapshot)))

(defn- source-snapshot
  []
  @source-snapshot-cache)

(defn- lexical-token
  [count]
  (let [next-count (inc count)]
    (when (> next-count maximum-lexical-tokens)
      (failure "SH07-C12-SHAPE-LEXICAL-BOUND"
               "C12 source exceeds bounded lexical token count"
               {:maximum maximum-lexical-tokens}))
    next-count))

(defn- lexical-delimiter
  [count]
  (let [next-count (inc count)]
    (when (> next-count maximum-lexical-delimiters)
      (failure "SH07-C12-SHAPE-LEXICAL-BOUND"
               "C12 source exceeds bounded lexical delimiter count"
               {:maximum maximum-lexical-delimiters}))
    next-count))

(defn- lexical-delimiter-depth!
  "Validate source delimiters before invoking the recursive host reader.

  Strings, regexes, comments, and character literals are skipped
  conservatively.  This is a lexical guard, not a replacement for reading."
  [source]
  (let [length (.length ^String source)
        matching-delimiter {\( \) \[ \] \{ \}}
        closing #{\) \] \}}]
    (loop [index 0
           delimiters []
           maximum-seen 0
           mode :normal
           escaped? false
           token-count 0
           delimiter-count 0]
      (if (= index length)
        (do
          (when-not (#{:normal :comment} mode)
            (failure "SH07-C12-SHAPE-LEXICAL-DELIMITERS"
                     "C12 source has an unterminated lexical literal"))
          (when (seq delimiters)
            (failure "SH07-C12-SHAPE-LEXICAL-DELIMITERS"
                     "C12 source has unclosed delimiters"))
          {:maximum-depth maximum-seen
           :token-count token-count
           :delimiter-count delimiter-count})
        (let [character (.charAt ^String source index)
              next-character (when (< (inc index) length)
                               (.charAt ^String source (inc index)))]
          (case mode
            :comment
            (if (or (= character \newline) (= character \return))
              (recur (inc index) delimiters maximum-seen :normal false
                     token-count delimiter-count)
              (recur (inc index) delimiters maximum-seen :comment false
                     token-count delimiter-count))

            :string
            (cond
              escaped?
              (recur (inc index) delimiters maximum-seen :string false
                     token-count delimiter-count)
              (= character \\)
              (recur (inc index) delimiters maximum-seen :string true
                     token-count delimiter-count)
              (= character \")
              (recur (inc index) delimiters maximum-seen :normal false
                     token-count delimiter-count)
              :else
              (recur (inc index) delimiters maximum-seen :string false
                     token-count delimiter-count))

            :regex
            (cond
              escaped?
              (recur (inc index) delimiters maximum-seen :regex false
                     token-count delimiter-count)
              (= character \\)
              (recur (inc index) delimiters maximum-seen :regex true
                     token-count delimiter-count)
              (= character \")
              (recur (inc index) delimiters maximum-seen :normal false
                     token-count delimiter-count)
              :else
              (recur (inc index) delimiters maximum-seen :regex false
                     token-count delimiter-count))

            :character
            ;; One codepoint is enough to hide a delimiter.  Named literals
            ;; continue as ordinary letters, conservatively bounded by tokens.
            (recur (inc index) delimiters maximum-seen :normal false
                   (lexical-token token-count) delimiter-count)

            ;; :normal
            (cond
              (= character \;)
              (recur (inc index) delimiters maximum-seen :comment false
                     token-count delimiter-count)
              (= character \")
              (recur (inc index) delimiters maximum-seen :string false
                     (lexical-token token-count) delimiter-count)
              (and (= character \#) (= next-character \"))
              (recur (+ index 2) delimiters maximum-seen :regex false
                     (lexical-token token-count) delimiter-count)
              (= character \\)
              (recur (inc index) delimiters maximum-seen :character false
                     (lexical-token token-count) delimiter-count)
              (contains? matching-delimiter character)
              (let [next-delimiters (conj delimiters character)
                    next-depth (count next-delimiters)
                    next-count (lexical-delimiter delimiter-count)]
                (when (> next-depth maximum-reader-tree-depth)
                  (failure "SH07-C12-SHAPE-LEXICAL-DEPTH"
                           "C12 source exceeds bounded lexical delimiter depth"
                           {:maximum maximum-reader-tree-depth}))
                (recur (inc index) next-delimiters
                       (max maximum-seen next-depth) :normal false
                       token-count next-count))
              (contains? closing character)
              (if (or (empty? delimiters)
                      (not= character
                            (matching-delimiter (peek delimiters))))
                (failure "SH07-C12-SHAPE-LEXICAL-DELIMITERS"
                         "C12 source has an unmatched or mismatched delimiter")
                (recur (inc index) (pop delimiters) maximum-seen :normal false
                       token-count (lexical-delimiter delimiter-count)))
              (Character/isWhitespace character)
              (recur (inc index) delimiters maximum-seen :normal false
                     token-count delimiter-count)
              :else
              (recur (inc index) delimiters maximum-seen :normal false
                     (lexical-token token-count) delimiter-count))))))))

(defn- read-forms
  [text]
  (binding [*read-eval* false]
    (lexical-delimiter-depth! text)
    (with-open [reader (java.io.PushbackReader.
                        (java.io.StringReader. text))]
      (loop [forms []]
        (let [form (read {:eof reader-eof-marker} reader)]
          (if (identical? reader-eof-marker form)
            forms
            (do
              (when (>= (count forms) maximum-top-level-forms)
                (failure "SH07-C12-SHAPE-FORM-COUNT"
                         "C12 source has too many top-level forms"
                         {:maximum maximum-top-level-forms}))
              (recur (conj forms form)))))))))

(defn- bounded-elements
  [value maximum id message]
  (loop [remaining (seq value)
         result []]
    (cond
      (nil? remaining) result
      (>= (count result) maximum)
      (failure id message {:maximum maximum})
      :else
      (recur (next remaining) (conj result (first remaining))))))

(defn- reader-children
  [value]
  (let [children
        (cond
          (map? value)
          (vec (mapcat (fn [[key value]] [key value])
                       (bounded-elements
                        value maximum-collection-width
                        "SH07-C12-SHAPE-COLLECTION-WIDTH"
                        "C12 source map is wider than the bounded gate")))

          (vector? value)
          (bounded-elements value maximum-collection-width
                            "SH07-C12-SHAPE-COLLECTION-WIDTH"
                            "C12 source collection is wider than the bounded gate")

          (set? value)
          (bounded-elements value maximum-collection-width
                            "SH07-C12-SHAPE-COLLECTION-WIDTH"
                            "C12 source collection is wider than the bounded gate")

          (seq? value)
          (bounded-elements value maximum-collection-width
                            "SH07-C12-SHAPE-COLLECTION-WIDTH"
                            "C12 source collection is wider than the bounded gate")

          :else
          [])]
    (when (> (count children) maximum-collection-width)
      (failure "SH07-C12-SHAPE-COLLECTION-WIDTH"
               "C12 source collection is wider than the bounded gate"
               {:maximum maximum-collection-width}))
    children))

(defn- reader-tree-stats
  [forms]
  (loop [frontier (list [forms 0])
         nodes 0
         maximum-depth 0
         maximum-width 0]
    (if (empty? frontier)
      {:nodes nodes
       :maximum-depth maximum-depth
       :maximum-width maximum-width
       :bounded? true
       :lazy? false}
      (let [[value depth] (peek frontier)
            frontier (pop frontier)]
        (cond
          (instance? clojure.lang.LazySeq value)
          {:nodes nodes
           :maximum-depth maximum-depth
           :maximum-width maximum-width
           :bounded? false
           :lazy? true}

          (> (inc nodes) maximum-reader-tree-nodes)
          {:nodes (inc nodes)
           :maximum-depth maximum-depth
           :maximum-width maximum-width
           :bounded? false
           :lazy? false}

          (> depth maximum-reader-tree-depth)
          {:nodes (inc nodes)
           :maximum-depth depth
           :maximum-width maximum-width
           :bounded? false
           :lazy? false}

          :else
          (let [children (reader-children value)]
            (recur
             (into frontier (map #(vector % (inc depth)) children))
             (inc nodes)
             (max maximum-depth depth)
             (max maximum-width (count children)))))))))

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

(defn- if-shapes
  [forms]
  (let [bad (atom [])]
    (loop [frontier (list forms)]
      (if (seq frontier)
        (let [value (peek frontier)
              frontier (pop frontier)]
          (when (and (seq? value) (= 'if (first value)))
            (when-not (= 4 (count value))
              (when (>= (count @bad) maximum-diagnostics)
                (failure "SH07-C12-SHAPE-DIAGNOSTICS"
                         "C12 source produced too many shape diagnostics"
                         {:maximum maximum-diagnostics}))
              (swap! bad conj value)))
          (recur (into frontier (reader-children value))))
        @bad))))

(defn- parse-source-shape
  []
  (let [{:keys [text] :as snapshot} (source-snapshot)
        forms (read-forms text)
        tree (reader-tree-stats forms)
        ns-form (first forms)
        exports (exports-vector ns-form)]
    (assoc snapshot
           :forms forms
           :tree tree
           :ns-form ns-form
           :exports exports
           :if-shape-errors (if-shapes forms))))

(def ^:private parsed-source-cache
  (delay (parse-source-shape)))

(defn- parse-shape
  []
  @parsed-source-cache)

(def ^:private non-authority-note
  "This moving-source shape check is non-authoritative: it does not compile, verify, lower, or execute C12.")

(deftest sh07-c12-domain-ir-source-shape-and-control
  (testing non-authority-note
    (let [{:keys [size forms ns-form tree if-shape-errors]} (parse-shape)]
      (is (<= size maximum-source-bytes))
      (is (seq forms))
      (let [namespace-forms (filter #(and (seq? %)
                                          (= 'ns (first %)))
                                    forms)]
        (is (= 1 (count namespace-forms)))
        (is (= (first forms) (first namespace-forms))))
      (is (ns-form? ns-form))
      (is (= 1 (count (exports-clauses ns-form))))
      (is (= 2 (count (first (exports-clauses ns-form)))))
      (is (vector? (exports-vector ns-form)))
      (is (:bounded? tree))
      (is (not (:lazy? tree)))
      (is (<= (:nodes tree) maximum-reader-tree-nodes))
      (is (<= (:maximum-depth tree) maximum-reader-tree-depth))
      (is (empty? if-shape-errors))
      (testing "lexical guard handles strings, regexes, comments, and chars"
        (is (= 1
               (:maximum-depth
                (lexical-delimiter-depth!
                 "(\"}\" #\"[\" ; )\n \\( )")))))
      (testing "reader evaluation and recursive-depth inputs fail closed"
        (is (thrown? Throwable
                     (read-forms "#=(+ 1 2)")))
        (let [deep-text
              (str (apply str (repeat (inc maximum-reader-tree-depth) "("))
                   (apply str (repeat (inc maximum-reader-tree-depth) ")")))]
          (is (thrown? clojure.lang.ExceptionInfo
                       (lexical-delimiter-depth! deep-text)))))
      (testing "lexical malformed delimiters and literals fail before reading"
        (doseq [invalid [")" "([)]" "(" "\"unterminated"
                        "#\"unterminated" "\\"]]
          (is (thrown? clojure.lang.ExceptionInfo
                       (lexical-delimiter-depth! invalid)))))
      (testing "collection and duplicate-definition bounds are source-only"
        (is (thrown? clojure.lang.ExceptionInfo
                     (reader-children
                      (vec (repeat (inc maximum-collection-width) :x)))))
        (is (= '#{hidden}
               (duplicate-definition-names
                '[(def hidden 1) (def hidden 2) (def exported 1)]))))
      (testing "injected bytes are decoded, not injected source text"
        (let [text "(ns synthetic (:exports [x])) (def x 1)"
              bytes (.getBytes text "UTF-8")
              loaded (binding [*source-snapshot-loader*
                               (fn [] {:bytes bytes :text "wrong text"})]
                       (load-source-snapshot))]
          (is (= text (:text loaded)))))
      (testing "path replacement is rejected by the explicit race seam"
        (let [temporary
              (java.nio.file.Files/createTempDirectory
               "sh07-c12-shape-race"
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
            (let [location (safe-contained-source-path temporary
                                                        "source.gravity")]
              (binding [*snapshot-before-open-hook*
                        (fn []
                          (java.nio.file.Files/move
                           source moved
                           (make-array java.nio.file.CopyOption 0))
                          (java.nio.file.Files/move
                           replacement source
                           (make-array java.nio.file.CopyOption 0)))]
                (is (thrown? clojure.lang.ExceptionInfo
                             (read-source-snapshot location)))))
            (finally
              (java.nio.file.Files/deleteIfExists source)
              (java.nio.file.Files/deleteIfExists replacement)
              (java.nio.file.Files/deleteIfExists moved)
              (java.nio.file.Files/deleteIfExists temporary))))))))

(deftest sh07-c12-domain-ir-export-completeness
  (testing non-authority-note
    (let [{:keys [forms ns-form exports]} (parse-shape)
          definitions (top-level-definitions forms)
          defn-forms (filter #(and (seq? %)
                                   (= 'defn (first %))) forms)]
      (is (vector? exports))
      (is (every? symbol? exports))
      (is (every? #(not (clojure.string/blank? (name %))) exports))
      (is (= (count exports) (count (set exports))))
      (is (every? #(contains? definitions %) exports))
      (is (empty? (duplicate-definition-names forms)))
      (is (every? #(and (= 4 (count %))
                        (symbol? (second %))
                        (vector? (nth % 2))
                        (some? (nth % 3)))
                  defn-forms))
      (is (= 'gravity.compiler.c12-domain-ir-architecture
             (second ns-form))))))
