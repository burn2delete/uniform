(ns gravity.self-hosting.sh07-c15-diagnostics-shape-preflight-test
  "Bounded, source-only moving-source admission for the Gravity C15 module."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")
(def ^:private maximum-source-bytes (* 512 1024))
(def ^:private maximum-top-level-forms 4096)
(def ^:private maximum-reader-nodes 250000)
(def ^:private maximum-reader-depth 256)
(def ^:private maximum-collection-width 1024)
(def ^:private expected-source-bytes 35228)
(def ^:private expected-source-sha256
  "ffb56f136f13172c1e366ae60a7514402ece009ccb3b018e2c50c9cb96b1d58a")
(def ^:private expected-exports
  '[c15-diagnostic-shape-contract
    c15-diagnostic-stream-contract
    c15-diagnostic-catalog
    build-c15-diagnostic
    verify-c15-diagnostic-stream
    sh15-diagnostic-input-valid?
    sh15-build-diagnostic-boundary
    sh15-verify-diagnostic-boundary])
(def ^:private eof-marker (Object.))
(def ^:private nofollow
  (into-array java.nio.file.LinkOption
              [java.nio.file.LinkOption/NOFOLLOW_LINKS]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c15_diagnostics_shape_preflight_test.clj")]
    (when-not resource
      (throw (ex-info "SH15 source preflight is not on the classpath"
                      {:id "SH07-C15-SHAPE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH07-C15-SHAPE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- basic-attributes [path]
  (let [attributes
        (java.nio.file.Files/readAttributes
         path java.nio.file.attribute.BasicFileAttributes nofollow)]
    {:regular? (.isRegularFile attributes)
     :symlink? (java.nio.file.Files/isSymbolicLink path)
     :file-key (.fileKey attributes)
     :size (.size attributes)
     :modified (.lastModifiedTime attributes)}))

(defn- read-bounded-source []
  (let [path (.resolve @root source-relative-path)
        before (basic-attributes path)]
    (when (or (:symlink? before) (not (:regular? before)))
      (throw (ex-info "C15 source must be a regular non-symlink file"
                      {:id "SH07-C15-SHAPE-SOURCE-FILE"})))
    (when (nil? (:file-key before))
      (throw (ex-info "C15 source identity is unavailable"
                      {:id "SH07-C15-SHAPE-SOURCE-IDENTITY"})))
    (when (> (:size before) maximum-source-bytes)
      (throw (ex-info "C15 source exceeds byte bound"
                      {:id "SH07-C15-SHAPE-SOURCE-BOUND"
                       :maximum maximum-source-bytes})))
    (with-open [channel
                (java.nio.channels.FileChannel/open
                 path
                 (into-array java.nio.file.OpenOption
                             [java.nio.file.StandardOpenOption/READ
                              java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
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
                (throw (ex-info "C15 source read made no progress"
                                {:id "SH07-C15-SHAPE-SOURCE-READ"}))
                :else (recur 0)))))
        (let [observed (.position buffer)
              after (basic-attributes path)]
          (when (or (> observed maximum-source-bytes)
                    (not= observed (:size before))
                    (not= before after)
                    (not= observed (.size channel)))
            (throw (ex-info "C15 source changed during bounded read"
                            {:id "SH07-C15-SHAPE-SOURCE-MUTATED"})))
          (.flip buffer)
          (let [bytes (byte-array observed)]
            (.get buffer bytes)
            {:path path :bytes bytes :size observed
             :file-key (:file-key before)}))))))

(defn- strict-utf8 [bytes]
  (let [decoder
        (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
          (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
          (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (java.nio.ByteBuffer/wrap bytes)))
      (catch java.nio.charset.CharacterCodingException exception
        (throw (ex-info "C15 source is not strict UTF-8"
                        {:id "SH07-C15-SHAPE-SOURCE-UTF8"}
                        exception))))))

(defn- lexical-delimiters! [text]
  (let [length (.length ^String text)
        pairs {\( \) \[ \] \{ \}}
        closing #{\) \] \}}]
    (loop [index 0 stack [] mode :normal escaped? false]
      (if (= index length)
        (do
          (when-not (#{:normal :comment} mode)
            (throw (ex-info "unterminated C15 lexical literal"
                            {:id "SH07-C15-SHAPE-LEXICAL"})))
          (when (seq stack)
            (throw (ex-info "unclosed C15 delimiter"
                            {:id "SH07-C15-SHAPE-LEXICAL"})))
          true)
        (let [character (.charAt ^String text index)]
          (case mode
            :comment
            (recur (inc index) stack
                   (if (or (= character \newline) (= character \return))
                     :normal :comment)
                   false)
            :string
            (cond
              escaped? (recur (inc index) stack :string false)
              (= character \\) (recur (inc index) stack :string true)
              (= character \" ) (recur (inc index) stack :normal false)
              :else (recur (inc index) stack :string false))
            :character
            (recur (inc index) stack :normal false)
            (cond
              (= character \;) (recur (inc index) stack :comment false)
              (= character \" ) (recur (inc index) stack :string false)
              (= character \\) (recur (inc index) stack :character false)
              (contains? pairs character)
              (let [next-stack (conj stack character)]
                (when (> (count next-stack) maximum-reader-depth)
                  (throw (ex-info "C15 lexical depth exceeds bound"
                                  {:id "SH07-C15-SHAPE-LEXICAL-DEPTH"})))
                (recur (inc index) next-stack :normal false))
              (contains? closing character)
              (if (or (empty? stack)
                      (not= character (pairs (peek stack))))
                (throw (ex-info "unmatched C15 delimiter"
                                {:id "SH07-C15-SHAPE-LEXICAL"}))
                (recur (inc index) (pop stack) :normal false))
              :else (recur (inc index) stack :normal false))))))))

(defn- read-forms [text]
  (binding [*read-eval* false]
    (lexical-delimiters! text)
    (with-open [reader (java.io.PushbackReader.
                        (java.io.StringReader. text))]
      (loop [forms []]
        (let [form (read {:eof eof-marker} reader)]
          (if (identical? eof-marker form)
            forms
            (do
              (when (>= (count forms) maximum-top-level-forms)
                (throw (ex-info "C15 top-level form count exceeds bound"
                                {:id "SH07-C15-SHAPE-FORM-BOUND"})))
              (recur (conj forms form)))))))))

(defn- bounded-children [value]
  (let [entries
        (cond
          (map? value) (mapcat (fn [[key item]] [key item]) value)
          (or (vector? value) (set? value) (seq? value)) value
          :else [])
        children (vec (take (inc maximum-collection-width) entries))]
    (when (> (count children) maximum-collection-width)
      (throw (ex-info "C15 collection width exceeds bound"
                      {:id "SH07-C15-SHAPE-WIDTH"})))
    children))

(defn- source-tree-stats [forms]
  (loop [frontier (list [forms 0]) nodes 0 bad-if []]
    (if (empty? frontier)
      {:nodes nodes :bad-if bad-if}
      (let [[value depth] (peek frontier)
            remaining (pop frontier)
            next-nodes (inc nodes)]
        (when (> next-nodes maximum-reader-nodes)
          (throw (ex-info "C15 reader tree exceeds node bound"
                          {:id "SH07-C15-SHAPE-NODE-BOUND"})))
        (when (> depth maximum-reader-depth)
          (throw (ex-info "C15 reader tree exceeds depth bound"
                          {:id "SH07-C15-SHAPE-DEPTH-BOUND"})))
        (let [children (bounded-children value)
              next-bad-if
              (if (and (seq? value) (= 'if (first value))
                       (not= 4 (count value)))
                (conj bad-if value)
                bad-if)]
          (recur (into remaining
                       (map #(vector % (inc depth)) children))
                 next-nodes next-bad-if))))))

(defn- exports-vector [ns-form]
  (let [clauses
        (vec (filter #(and (seq? %) (= :exports (first %)))
                     (drop 2 ns-form)))]
    (when (and (= 1 (count clauses))
               (= 2 (count (first clauses)))
               (vector? (second (first clauses))))
      (second (first clauses)))))

(defn- definition-names [forms]
  (keep (fn [form]
          (when (and (seq? form) (#{'def 'defn} (first form))
                     (symbol? (second form)))
            (second form)))
        forms))

(defn- sha256 [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) digest))))

(defn- parse-source []
  (let [{:keys [bytes] :as snapshot} (read-bounded-source)
        text (strict-utf8 bytes)
        forms (read-forms text)]
    (assoc snapshot :text text :forms forms
           :tree (source-tree-stats forms))))

(def ^:private parsed-source (delay (parse-source)))

(deftest sh07-c15-diagnostics-source-shape-and-control
  (let [{:keys [size forms tree]} @parsed-source
        ns-form (first forms)
        namespace-forms (filter #(and (seq? %) (= 'ns (first %))) forms)]
    (is (<= size maximum-source-bytes))
    (is (= 1 (count namespace-forms)))
    (is (= ns-form (first namespace-forms)))
    (is (= 'gravity.compiler.c15-compiler-diagnostics (second ns-form)))
    (is (<= (:nodes tree) maximum-reader-nodes))
    (is (empty? (:bad-if tree)))
    (testing "under- and over-arity if forms fail shape admission"
      (is (seq (:bad-if (source-tree-stats '[(if true 1)]))))
      (is (seq (:bad-if (source-tree-stats '[(if true 1 2 3)])))))
    (testing "strict UTF-8 rejects malformed source bytes"
      (let [failure
            (try
              (strict-utf8 (byte-array [(unchecked-byte 0xff)]))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (= "SH07-C15-SHAPE-SOURCE-UTF8" (:id (ex-data failure))))))
    (testing "the source-only lane never compiles or dereferences c15-plan"
      (is (nil? (ns-resolve *ns* 'c15-plan)))
      (is (nil? (ns-resolve *ns* 'compile-plan)))
      (is (nil? (ns-resolve *ns* 'invoke-c15))))))

(deftest sh07-c15-diagnostics-export-completeness-and-source-identity
  (let [{:keys [bytes size forms]} @parsed-source
        exports (exports-vector (first forms))
        definitions (vec (definition-names forms))]
    (is (= expected-exports exports))
    (is (= (count definitions) (count (distinct definitions))))
    (is (every? (set definitions) exports))
    (testing "malformed, duplicate, missing, and trailing export clauses fail"
      (is (nil? (exports-vector '(ns sample (:exports missing)))))
      (is (nil? (exports-vector '(ns sample (:exports [x]) (:exports [x])))))
      (is (nil? (exports-vector '(ns sample (:profile :meta)))))
      (is (nil? (exports-vector '(ns sample (:exports [x] :trailing))))))
    (testing "an exported undefined symbol fails completeness"
      (let [sample-forms '[(ns sample (:exports [defined missing]))
                           (def defined 1)]
            sample-exports (exports-vector (first sample-forms))
            sample-definitions (set (definition-names sample-forms))]
        (is (false? (every? sample-definitions sample-exports)))))
    (is (= expected-source-bytes size))
    (is (= expected-source-sha256 (sha256 bytes)))))
