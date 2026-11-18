(ns gravity.self-hosting.sh07-c12-domain-ir-shape-preflight-test
  "Moving-source C12 shape admission only.

  This lane reads the Gravity source as data.  It does not load the bootstrap,
  compile an artifact, resolve a plan, execute a function, or make any
  authority claim."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(def ^:private source-relative-path
  "language/compiler/src/gravity/compiler/c12_domain_ir_architecture.gravity")

;; Keep this source-only check independent of the C12 compiler.  Its iterative
;; lexical and shape walks validate syntax without imposing size quotas.

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

  Rechecking these states around the read is a pathname-integrity
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
  "Read one stable-path snapshot.

  The final path and each intermediate component are nofollow regular
  observations before/open/after, with a one-byte growth probe.  This is a
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
          (let [expected-size (:size state-before)
                probe-size (inc expected-size)
                buffer (java.nio.ByteBuffer/allocate 8192)
                output (java.io.ByteArrayOutputStream.)]
            (loop [zero-reads 0]
              (when (< (.size output) probe-size)
                (.clear buffer)
                (.limit buffer
                        (int (min (.capacity buffer)
                                  (- probe-size (.size output)))))
                (let [read-count (.read channel buffer)]
                  (cond
                    (= -1 read-count) nil
                    (and (zero? read-count) (< zero-reads 2))
                    (recur (inc zero-reads))
                    (zero? read-count)
                    (failure "SH07-C12-SHAPE-SOURCE-READ"
                             "C12 source channel made no progress")
                    :else
                    (do
                      (.flip buffer)
                      (let [chunk (byte-array (.remaining buffer))]
                        (.get buffer chunk)
                        (.write output chunk 0 (alength chunk)))
                      (recur 0))))))
            (let [observed-bytes (.size output)
                  channel-size-after (.size channel)
                  state-after (basic-attributes path)
                  location-after
                  (when (and (:root location) (:relative location))
                    (safe-contained-source-path (:root location)
                                                (:relative location)))]
              (when (or (> observed-bytes expected-size)
                        (not= channel-size-before channel-size-after)
                        (not= channel-size-before observed-bytes)
                        (not (same-file-state? state-before state-after))
                        (and (:components location)
                             (not= (:components location)
                                   (:components location-after))))
                (failure "SH07-C12-SHAPE-SOURCE-MUTATED"
                         "C12 source changed during descriptor read"
                         {:observed-bytes observed-bytes}))
              {:path path
               :bytes (.toByteArray output)
               :file-key (:file-key state-before)
               :size observed-bytes
               :modified (:modified state-before)
               :owner (:owner state-before)
               :attributes (attributes-signature state-before)})))))))

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
    (assoc loaded
           :bytes bytes
           :text (strict-utf8 bytes)
           :size (alength ^bytes bytes))))

(def ^:private source-snapshot-cache
  (delay (load-source-snapshot)))

(defn- source-snapshot
  []
  @source-snapshot-cache)

(defn- lexical-delimiter-depth!
  "Validate source delimiters iteratively before invoking the host reader.

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
           escaped? false]
      (if (= index length)
        (do
          (when-not (#{:normal :comment} mode)
            (failure "SH07-C12-SHAPE-LEXICAL-DELIMITERS"
                     "C12 source has an unterminated lexical literal"))
          (when (seq delimiters)
            (failure "SH07-C12-SHAPE-LEXICAL-DELIMITERS"
                     "C12 source has unclosed delimiters"))
          {:maximum-depth maximum-seen})
        (let [character (.charAt ^String source index)
              next-character (when (< (inc index) length)
                               (.charAt ^String source (inc index)))]
          (case mode
            :comment
            (if (or (= character \newline) (= character \return))
              (recur (inc index) delimiters maximum-seen :normal false)
              (recur (inc index) delimiters maximum-seen :comment false))

            :string
            (cond
              escaped?
              (recur (inc index) delimiters maximum-seen :string false)
              (= character \\)
              (recur (inc index) delimiters maximum-seen :string true)
              (= character \")
              (recur (inc index) delimiters maximum-seen :normal false)
              :else
              (recur (inc index) delimiters maximum-seen :string false))

            :regex
            (cond
              escaped?
              (recur (inc index) delimiters maximum-seen :regex false)
              (= character \\)
              (recur (inc index) delimiters maximum-seen :regex true)
              (= character \")
              (recur (inc index) delimiters maximum-seen :normal false)
              :else
              (recur (inc index) delimiters maximum-seen :regex false))

            :character
            ;; One codepoint is enough to hide a delimiter in a character literal.
            (recur (inc index) delimiters maximum-seen :normal false)

            ;; :normal
            (cond
              (= character \;)
              (recur (inc index) delimiters maximum-seen :comment false)
              (= character \")
              (recur (inc index) delimiters maximum-seen :string false)
              (and (= character \#) (= next-character \"))
              (recur (+ index 2) delimiters maximum-seen :regex false)
              (= character \\)
              (recur (inc index) delimiters maximum-seen :character false)
              (contains? matching-delimiter character)
              (let [next-delimiters (conj delimiters character)
                    next-depth (count next-delimiters)]
                (recur (inc index) next-delimiters
                       (max maximum-seen next-depth) :normal false))
              (contains? closing character)
              (if (or (empty? delimiters)
                      (not= character
                            (matching-delimiter (peek delimiters))))
                (failure "SH07-C12-SHAPE-LEXICAL-DELIMITERS"
                         "C12 source has an unmatched or mismatched delimiter")
                (recur (inc index) (pop delimiters) maximum-seen :normal false))
              :else
              (recur (inc index) delimiters maximum-seen :normal false))))))))

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
            (recur (conj forms form))))))))

(defn- reader-children
  [value]
  (when (instance? clojure.lang.LazySeq value)
    (failure "SH07-C12-SHAPE-LAZY-SEQ"
             "C12 source reader produced a lazy sequence"))
  (cond
    (map? value)
    (vec (mapcat (fn [[key child]] [key child]) value))

    (or (vector? value) (set? value) (seq? value))
    (vec value)

    :else
    []))

(defn- validate-reader-values!
  [forms]
  (loop [frontier (list forms)]
    (if (seq frontier)
      (let [value (peek frontier)
            frontier (pop frontier)]
        (recur (into frontier (reader-children value))))
      forms)))

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
              (swap! bad conj value)))
          (recur (into frontier (reader-children value))))
        @bad))))

(defn- parse-source-shape
  [snapshot]
  (let [{:keys [text]} snapshot
        forms (validate-reader-values! (read-forms text))
        ns-form (first forms)
        exports (exports-vector ns-form)]
    (assoc snapshot
           :forms forms
           :ns-form ns-form
           :exports exports
           :if-shape-errors (if-shapes forms))))

(def ^:private parsed-source-cache
  (delay (parse-source-shape (source-snapshot))))

(defn- parse-shape
  []
  @parsed-source-cache)

(def ^:private non-authority-note
  "This moving-source shape check is non-authoritative: it does not compile, verify, lower, or execute C12.")

(deftest sh07-c12-domain-ir-source-shape-and-control
  (testing non-authority-note
    (let [{:keys [forms ns-form if-shape-errors]} (parse-shape)]
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
      (is (empty? if-shape-errors))
      (testing "lexical guard handles strings, regexes, comments, and chars"
        (is (= 1
               (:maximum-depth
                (lexical-delimiter-depth!
                 "(\"}\" #\"[\" ; )\n \\( )")))))
      (testing "reader evaluation is disabled and deeper legal forms are read"
        (is (thrown? Throwable
                     (read-forms "#=(+ 1 2)")))
        (let [depth 257
              deep-text (str (apply str (repeat depth "("))
                             "value"
                             (apply str (repeat depth ")")))]
          (is (= depth
                 (:maximum-depth (lexical-delimiter-depth! deep-text))))
          (is (= 1 (count (validate-reader-values!
                           (read-forms deep-text)))))))
      (testing "lexical malformed delimiters and literals fail before reading"
        (doseq [invalid [")" "([)]" "(" "\"unterminated"
                        "#\"unterminated" "\\"]]
          (is (thrown? clojure.lang.ExceptionInfo
                       (lexical-delimiter-depth! invalid)))))
      (testing "former form, collection, node, token, and delimiter ceilings do not refuse input"
        (is (= 4097 (count (read-forms (apply str (repeat 4097 "nil\n"))))))
        (is (= 1025 (count (reader-children (vec (repeat 1025 :x))))))
        (is (= 250001
               (count (validate-reader-values!
                       (vec (repeat 250001 :x))))))
        (is (= 0
               (:maximum-depth
                (lexical-delimiter-depth! (apply str (repeat 250001 "x "))))))
        (is (= 1
               (:maximum-depth
                (lexical-delimiter-depth! (apply str (repeat 125001 "()"))))))
        (is (= '#{hidden}
               (duplicate-definition-names
                '[(def hidden 1) (def hidden 2) (def exported 1)]))))
      (testing "lazy reader values are rejected without realization"
        (let [realized? (atom false)
              lazy-value (lazy-seq
                           (do
                             (reset! realized? true)
                             (list :hidden)))
              error (try
                      (validate-reader-values! [lazy-value])
                      nil
                      (catch clojure.lang.ExceptionInfo exception
                        exception))]
          (is (= "SH07-C12-SHAPE-LAZY-SEQ" (:id (ex-data error))))
          (is (false? @realized?))))
      (testing "malformed reader structures still fail"
        (doseq [invalid ["#=(+ 1 2)" "{:unpaired}" "([)]" "\"unterminated"]]
          (is (thrown? Throwable (read-forms invalid)))))
      (testing "injected bytes are decoded, not injected source text"
        (let [text "(ns synthetic (:exports [x])) (def x 1)"
              bytes (.getBytes text "UTF-8")
              loaded (binding [*source-snapshot-loader*
                               (fn [] {:bytes bytes :text "wrong text"})]
                       (load-source-snapshot))]
          (is (= text (:text loaded)))))
      (testing "invalid UTF-8 remains rejected"
        (let [bytes (byte-array [(unchecked-byte 195) (byte 40)])
              error (try
                      (binding [*source-snapshot-loader*
                                (fn [] {:bytes bytes})]
                        (load-source-snapshot))
                      nil
                      (catch clojure.lang.ExceptionInfo exception
                        exception))]
          (is (= "SH07-C12-SHAPE-SOURCE-UTF8"
                 (:id (ex-data error))))))
      (testing "actual current source above the former byte ceiling keeps its shape and exports"
        (let [baseline (parse-shape)
              original (source-snapshot)
              target-size 524289
              padding-size (- target-size (alength ^bytes (:bytes original)) 2)
              extended-text (str (:text original)
                                 "\n;"
                                 (apply str (repeat padding-size \x)))
              extended-bytes (.getBytes extended-text "UTF-8")
              temporary (java.nio.file.Files/createTempDirectory
                         "sh07-c12-large-source"
                         (make-array java.nio.file.attribute.FileAttribute 0))
              path (.resolve temporary "source.gravity")]
          (try
            (is (pos? padding-size))
            (is (= target-size (alength ^bytes extended-bytes)))
            (java.nio.file.Files/write
             path extended-bytes (make-array java.nio.file.OpenOption 0))
            (let [location (safe-contained-source-path temporary "source.gravity")
                  snapshot (read-source-snapshot location)
                  loaded (binding [*source-snapshot-loader* (fn [] snapshot)]
                           (load-source-snapshot))
                  parsed (parse-source-shape loaded)
                  forms (:forms parsed)
                  namespace-forms (filter #(and (seq? %)
                                                (= 'ns (first %)))
                                          forms)
                  definitions (top-level-definitions forms)]
              (is (= target-size (:size snapshot)))
              (is (= target-size (:size loaded)))
              (is (= 1 (count namespace-forms)))
              (is (ns-form? (:ns-form parsed)))
              (is (= (second (:ns-form baseline))
                     (second (:ns-form parsed))))
              (is (= (:exports baseline) (:exports parsed)))
              (is (every? #(contains? definitions %) (:exports parsed)))
              (is (empty? (duplicate-definition-names forms)))
              (is (empty? (:if-shape-errors parsed))))
            (finally
              (java.nio.file.Files/deleteIfExists path)
              (java.nio.file.Files/deleteIfExists temporary)))))
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
