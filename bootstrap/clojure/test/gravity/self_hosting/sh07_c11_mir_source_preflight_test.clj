(ns gravity.self-hosting.sh07-c11-mir-source-preflight-test
  "Source-only C11 MIR preflight without project input quotas.

  Structural selectors share one source snapshot; historical admission pins
  remain a separate binding check. The preflight stops at reader data and
  does not invoke compiler or artifact code. Lexical delimiter validation
  precedes the host reader, and structural walks are iterative. Host reader,
  stack and heap limits remain implementation boundaries."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

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
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 330835)
(def ^:private expected-source-revision-id
  "sha256:cf7af2e3a7709bcc3ce5056cf75bab1bb0b4ac01c6627fe3d1f3d90d5c83c0aa")
(def ^:private expected-source-exports
  '[c11-mir-contract
    c11-build-mir-pass-contract
    c11-mir-module-contract
    c11-mir-function-contract
    c11-mir-block-contract
    c11-mir-terminator-contract
    c11-mir-instruction-contract
    c11-mir-value-contract
    c11-mir-source-map-contract
    c11-mir-verifier-contract
    c11-mir-diagnostic-catalog
    build-c11-mir-module
    build-c11-mir-function
    build-c11-mir-block
    build-c11-mir-terminator
    build-c11-mir-instruction
    build-c11-mir-value
    build-c11-mir-source-map
    verify-c11-mir-module
    c11-build-target-independent-mir
    sh12-branch-join-mir-policy
    sh12-build-branch-join-mir
    sh12-verify-branch-join-mir
    sh12-authenticated-c10-mir-adapter-policy
    sh12-authenticated-c10-mir-carrier-preflight
    sh12-authenticated-c10-mir-input-valid?
    sh12-build-authenticated-c10-mir-template
    sh12-authenticated-c10-mir-identity-request
    sh12-authenticated-c10-mir-module-identity-request
    sh12-bind-authenticated-c10-mir
    sh12-verify-authenticated-c10-mir
    sh13-control-flow-policy
    sh13-run-module
    sh13-verify-execution])
(def ^:private invalid-export-clause ::invalid-export-clause)
(def ^:private invalid-definition-duplicates ::invalid-definition-duplicates)

(def ^:dynamic *source-snapshot-loader*
  "Test seam for a pre-authenticated snapshot map.  Production leaves nil."
  nil)

(def ^:dynamic *snapshot-before-open-hook*
  "Test seam invoked after initial stat and before descriptor opening."
  nil)

(def ^:private reader-eof-marker (Object.))

(defn- failure
  ([id message] (failure id message {}))
  ([id message data]
    (throw (ex-info (str message) (merge {:id id} (dissoc data :bytes :source-text))))))

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

  The returned map includes repeated-observation state for every component.
  The caller rechecks those states around the descriptor read; this is a
  pathname integrity check, not a held-dirfd or SecureDirectoryStream race
  proof."
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
  "Read a no-follow descriptor snapshot; decode and hash only its exact bytes."
  [source-path]
  (let [location (if (map? source-path)
                   source-path
                   {:path
                    (if (instance? java.nio.file.Path source-path)
                      source-path
                      (.toPath (io/file source-path)))})
        path (:path location)
        component-states (:components location)
        state-before (basic-file-state path)]
    (when (or (:symlink? state-before) (not (:regular? state-before)))
      (failure
        "SH07-C11-PREFLIGHT-SOURCE-FILE"
        "C11 source must be a regular non-symlink file"
        {:source-path (str path)}))
    (when (nil? (:file-key state-before))
      (failure
        "SH07-C11-PREFLIGHT-SOURCE-IDENTITY"
        "C11 source file identity is unavailable"
        {:source-path (str path)}))
    (when *snapshot-before-open-hook* (*snapshot-before-open-hook*))
    (when (and
            component-states
            (not=
              component-states
              (:components (safe-contained-source-path (:root location) (:relative location)))))
      (failure
        "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
        "C11 source path components changed before descriptor read"))
    (with-open [channel (java.nio.channels.FileChannel/open
                          path
                          (into-array
                            java.nio.file.OpenOption
                            [java.nio.file.StandardOpenOption/READ
                             java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
      (let [state-open (basic-file-state path) channel-size-before (.size channel)]
        (when (or
                (not (same-file-state? state-before state-open))
                (not= channel-size-before (:size state-before)))
          (failure
            "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
            "C11 source changed before descriptor read"
            {:source-path (str path)}))
        (let [output (java.io.ByteArrayOutputStream.)
              input (java.nio.channels.Channels/newInputStream channel)
              _ (.transferTo input output)
              bytes (.toByteArray output)
              observed-bytes (alength bytes)
              state-after (basic-file-state path)]
          (when (or
                  (not= channel-size-before (.size channel))
                  (not= channel-size-before observed-bytes)
                  (not (same-file-state? state-before state-after))
                  (and
                    component-states
                    (not=
                      component-states
                      (:components
                        (safe-contained-source-path (:root location) (:relative location))))))
            (failure
              "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
              "C11 source changed during descriptor read"
              {:source-path (str path), :observed-bytes observed-bytes}))
          {:bytes bytes,
           :source-text (strict-utf8 bytes path),
           :source-sha256 (sha256-id bytes),
           :file-key (:file-key state-before),
           :size observed-bytes,
           :modified (:modified state-before),
           :owner (:owner state-before)})))))

(defn- source-path
  []
  (:path (safe-contained-source-path @root source-relative-path)))

(defn- load-source-snapshot []
  (let [snapshot (if *source-snapshot-loader*
                   (*source-snapshot-loader*)
                   (read-source-snapshot (safe-contained-source-path @root source-relative-path)))
        bytes (:bytes snapshot)]
    (when-not (bytes? bytes)
      (failure "SH07-C11-PREFLIGHT-SOURCE-BYTES" "C11 source snapshot did not provide byte data"))
    (assoc
      snapshot
      :source-text
      (strict-utf8 bytes "C11 source snapshot")
      :source-sha256
      (sha256-id bytes)
      :authenticated?
      false)))

(defn- validate-source-binding!
  "Enforce the exact source pin for the dedicated binding selector only."
  [snapshot]
  (let [bytes (:bytes snapshot)]
    (when (or (not (bytes? bytes))
              (not= expected-source-byte-count (alength ^bytes bytes)))
      (failure "SH07-C11-PREFLIGHT-SOURCE-BINDING"
               "C11 source byte count does not match the authenticated pin"))
    (when (not= expected-source-revision-id (sha256-id bytes))
      (failure "SH07-C11-PREFLIGHT-SOURCE-BINDING"
               "C11 source digest does not match the authenticated pin"))
    (assoc snapshot
           :source-sha256 (sha256-id bytes)
           :authenticated? true)))

(def ^:private source-snapshot (delay (load-source-snapshot)))

(defn- proof-contract
  []
  (let [path (safe-contained-source-path @root proof-contract-relative-path)
        snapshot (read-source-snapshot path)]
    (edn/read-string (strict-utf8 (:bytes snapshot) "SH-07 proof contract"))))

(defn- source-bytes
  []
  ;; Return a copy so callers cannot mutate the process-local snapshot
  ;; held by the delay; exact source binding is enforced by its own selector.
  (aclone ^bytes (:bytes @source-snapshot)))

(defn- source-text
  []
  (:source-text @source-snapshot))

(defn- lexical-delimiter-depth!
  "Validate delimiter balance without recursively entering source text.\n\n  Strings, regex strings, comments, and one-character/named character\n  literals are skipped conservatively.  This is a lexical guard, not a\n  replacement for the Clojure reader."
  [source]
  (let [length (.length source) matching-delimiter {\( \), \[ \], \{ \}} closing #{\) \] \}}]
    (loop [index 0 delimiters [] maximum-seen 0 mode :normal escaped? false]
      (if (= index length)
        (do
          (when-not (#{:normal :comment} mode)
            (failure
              "SH07-C11-PREFLIGHT-LEXICAL-DELIMITERS"
              "C11 source has an unterminated lexical literal"))
          (when (seq delimiters)
            (failure "SH07-C11-PREFLIGHT-LEXICAL-DELIMITERS" "C11 source has unclosed delimiters"))
          maximum-seen)
        (let [character (.charAt source index)
              next-character (when (< (inc index) length) (.charAt source (inc index)))]
          (case
            mode
            :comment
            (if (or (= character \newline) (= character \return))
              (recur (inc index) delimiters maximum-seen :normal false)
              (recur (inc index) delimiters maximum-seen :comment false))
            :string
            (cond
              escaped? (recur (inc index) delimiters maximum-seen :string false)
              (= character \\) (recur (inc index) delimiters maximum-seen :string true)
              (= character \") (recur (inc index) delimiters maximum-seen :normal false)
              :else (recur (inc index) delimiters maximum-seen :string false))
            :regex
            (cond
              escaped? (recur (inc index) delimiters maximum-seen :regex false)
              (= character \\) (recur (inc index) delimiters maximum-seen :regex true)
              (= character \") (recur (inc index) delimiters maximum-seen :normal false)
              :else (recur (inc index) delimiters maximum-seen :regex false))
            :character
            (recur (inc index) delimiters maximum-seen :normal false)
            (cond
              (= character \;) (recur (inc index) delimiters maximum-seen :comment false)
              (= character \") (recur (inc index) delimiters maximum-seen :string false)
              (and (= character \#) (= next-character \")) (recur
                                                             (+ index 2)
                                                             delimiters
                                                             maximum-seen
                                                             :regex
                                                             false)
              (= character \\) (recur (inc index) delimiters maximum-seen :character false)
              (contains? matching-delimiter character) (let [next-delimiters
                                                             (conj delimiters character)
                                                             next-depth (count next-delimiters)]
                                                                                                        (recur
                                                           (inc index)
                                                           next-delimiters
                                                           (max maximum-seen next-depth)
                                                           :normal
                                                           false))
              (contains? closing character) (if (or
                                                  (empty? delimiters)
                                                  (not=
                                                    character
                                                    (matching-delimiter (peek delimiters))))
                                              (failure
                                                "SH07-C11-PREFLIGHT-LEXICAL-DELIMITERS"
                                                "C11 source has an unmatched or mismatched delimiter")
                                              (recur
                                                (inc index)
                                                (pop delimiters)
                                                maximum-seen
                                                :normal
                                                false))
              :else (recur (inc index) delimiters maximum-seen :normal false))))))))

(defn- read-forms [source]
  (binding [*read-eval* false]
    (lexical-delimiter-depth! source)
    (with-open [reader (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. source))]
      (loop [forms []]
        (let [form (read {:eof reader-eof-marker} reader)]
          (if (identical? reader-eof-marker form) forms (recur (conj forms form))))))))

(def ^:private source-forms-cache
  (delay (read-forms (source-text))))

(defn- source-forms
  []
  @source-forms-cache)

(defn- analyze-form-sequence
  "Iteratively inspect the source forms for invalid control-form arity."
  [forms]
  (let [roots (vec forms)]
    (loop [pending (vec (reverse roots)) invalid-if-forms []]
      (if (empty? pending)
        {:forms roots, :invalid-if-forms invalid-if-forms}
        (let [form (peek pending)
              children (if (coll? form) (vec form) [])
              invalid-if? (and (seq? form) (= 'if (first form)) (not= 4 (count children)))]
          (recur
            (into (pop pending) (reverse children))
            (if invalid-if? (conj invalid-if-forms form) invalid-if-forms)))))))

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
      (if (and (seq? form) (#{'defn 'def} (first form)) (symbol? (second form)))
        (update frequencies (second form) (fnil inc 0))
        frequencies))
    {}
    (vec forms)))

(defn- duplicate-definition-names
  [forms]
  (set (keep (fn [[name frequency]]
               (when (> frequency 1) name))
             (definition-names forms))))

(defn- export-names [forms]
  (let [forms (vec forms) _ (analyze-form-sequence forms) namespace-form (first forms)]
    (if (and
          (seq? namespace-form)
          (= 'ns (first namespace-form))
          (symbol? (second namespace-form)))
      (let [clauses (filter #(and (seq? %) (= :exports (first %))) (drop 2 namespace-form))]
        (if (= 1 (count clauses))
          (let [clause (first clauses) values (second clause)]
            (if (and
                  (= 2 (count clause))
                  (vector? values)
                  (seq values)
                  (every? symbol? values)
                  (= (count values) (count (set values))))
              values
              invalid-export-clause))
          invalid-export-clause))
      invalid-export-clause)))

(defn- missing-export-definitions [forms]
  (let [forms (vec forms) exports (export-names forms) definitions (definition-names forms)]
    (if (= invalid-export-clause exports)
      invalid-export-clause
      (if (seq (duplicate-definition-names forms))
        invalid-definition-duplicates
        (set (remove definitions exports))))))

(deftest
  sh07-c11-source-binding-is-exact
  (let [bytes (source-bytes)
        production-snapshot @source-snapshot
        contract (proof-contract)
        expectation (get-in
                      contract
                      [:authoritative-coverage-census :module-expectations :c11-mir])]
    (is (= expected-source-byte-count (alength bytes)))
    (is (= expected-source-revision-id (sha256-id bytes)))
    (is (:authenticated? (validate-source-binding! production-snapshot)))
    (is (= 'gravity.compiler.c11-mir-specification (:module-namespace expectation)))
    (is
      (=
        {:source-byte-count expected-source-byte-count,
         :source-bytes-sha256 expected-source-revision-id}
        (:source-binding expectation)))
    (testing
      "injected source text cannot override strict bytes decoding"
      (let [loaded (binding [*source-snapshot-loader* (fn []
                                                        {:bytes bytes, :source-text "wrong text"})]
                     (load-source-snapshot))]
        (is (= (strict-utf8 bytes "expected") (:source-text loaded)))))))

(deftest
  sh07-c11-source-snapshot-preserves-identity
  (testing
    "structural selectors can inspect a moving pin, but binding rejects it"
    (let [text "(ns synthetic (:exports [x])) (def x 1)"
          bytes (.getBytes text "UTF-8")
          loaded (binding [*source-snapshot-loader* (fn []
                                                      {:bytes bytes, :source-text "wrong text"})]
                   (load-source-snapshot))
          forms (read-forms (:source-text loaded))]
      (is (= text (:source-text loaded)))
      (is (empty? (invalid-source-if-forms forms)))
      (is (empty? (missing-export-definitions forms)))
      (is (thrown? clojure.lang.ExceptionInfo (validate-source-binding! loaded)))))
  (testing
    "strict UTF-8 rejects malformed bytes without a platform decoder"
    (is
      (try
        (strict-utf8 (byte-array [(unchecked-byte 195) (byte 40)]) "synthetic")
        false
        (catch
          clojure.lang.ExceptionInfo
          error
          (= "SH07-C11-PREFLIGHT-SOURCE-UTF8" (:id (ex-data error)))))))
  (testing
    "path authentication rejects containment escapes and symlink leaves"
    (let [temporary (java.nio.file.Files/createTempDirectory
                      "sh07-c11-path-test"
                      (make-array java.nio.file.attribute.FileAttribute 0))
          real-file (.resolve temporary "real.gravity")
          link-file (.resolve temporary "link.gravity")
          larger-file (.resolve temporary "oversized.gravity")
          former-limit-file (.resolve temporary "exact-bound.gravity")
          replacement-file (.resolve temporary "replacement.gravity")
          moved-original (.resolve temporary "original.gravity")
          real-directory (.resolve temporary "real-directory")
          intermediate-link (.resolve temporary "intermediate-link")
          nested-file (.resolve real-directory "nested.gravity")]
      (try
        (java.nio.file.Files/write
          real-file
          (.getBytes "(ns synthetic (:exports [x])) (def x 1)" "UTF-8")
          (make-array java.nio.file.OpenOption 0))
        (java.nio.file.Files/write
          larger-file
          (byte-array (inc 524288))
          (make-array java.nio.file.OpenOption 0))
        (java.nio.file.Files/write
          former-limit-file
          (byte-array 524288)
          (make-array java.nio.file.OpenOption 0))
        (let [snapshot (read-source-snapshot former-limit-file)]
          (is (= 524288 (:size snapshot)))
          (is (= 524288 (alength (:bytes snapshot)))))
        (is (= 524289 (:size (read-source-snapshot larger-file))))
        (is
          (thrown?
            clojure.lang.ExceptionInfo
            (safe-contained-source-path temporary "../escape.gravity")))
        (try
          (java.nio.file.Files/createSymbolicLink
            link-file
            real-file
            (make-array java.nio.file.attribute.FileAttribute 0))
          (is (thrown? clojure.lang.ExceptionInfo (read-source-snapshot link-file)))
          (catch java.nio.file.FileSystemException _ (is true))
          (catch UnsupportedOperationException _ (is true)))
        (java.nio.file.Files/createDirectory
          real-directory
          (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
          nested-file
          (.getBytes "(ns nested)" "UTF-8")
          (make-array java.nio.file.OpenOption 0))
        (try
          (java.nio.file.Files/createSymbolicLink
            intermediate-link
            real-directory
            (make-array java.nio.file.attribute.FileAttribute 0))
          (is
            (thrown?
              clojure.lang.ExceptionInfo
              (safe-contained-source-path temporary "intermediate-link/nested.gravity")))
          (catch java.nio.file.FileSystemException _ (is true))
          (catch UnsupportedOperationException _ (is true)))
        (java.nio.file.Files/write
          replacement-file
          (.getBytes "(ns replacement (:exports [x])) (def x 1)" "UTF-8")
          (make-array java.nio.file.OpenOption 0))
        (binding [*snapshot-before-open-hook* (fn []
                                                (java.nio.file.Files/move
                                                  real-file
                                                  moved-original
                                                  (make-array java.nio.file.CopyOption 0))
                                                (java.nio.file.Files/move
                                                  replacement-file
                                                  real-file
                                                  (make-array java.nio.file.CopyOption 0)))]
          (is (thrown? clojure.lang.ExceptionInfo (read-source-snapshot real-file))))
        (finally
          (java.nio.file.Files/deleteIfExists link-file)
          (java.nio.file.Files/deleteIfExists real-file)
          (java.nio.file.Files/deleteIfExists replacement-file)
          (java.nio.file.Files/deleteIfExists moved-original)
          (java.nio.file.Files/deleteIfExists former-limit-file)
          (java.nio.file.Files/deleteIfExists larger-file)
          (java.nio.file.Files/deleteIfExists intermediate-link)
          (java.nio.file.Files/deleteIfExists nested-file)
          (java.nio.file.Files/deleteIfExists real-directory)
          (java.nio.file.Files/deleteIfExists temporary))))))

(deftest
  sh07-c11-source-control-form-arities-are-exact
  (let [analysis @source-analysis-cache]
    (is (empty? (:invalid-if-forms analysis)))
    (is (empty? (duplicate-definition-names (:forms analysis))))
    (testing
      "both under- and over-arity if forms fail the same source-only gate"
      (is (= 1 (count (invalid-source-if-forms '[(if true)]))))
      (is (= 1 (count (invalid-source-if-forms '[(if true :then :else :extra)])))))
    (testing
      "a literal namespaced eof-like value cannot truncate later forms"
      (is (= 3 (count (read-forms "(ns synthetic) ::eof (def later 1)")))))
    (testing
      "the lexical guard ignores delimiters in strings, regexes, comments, and chars"
      (is (= 1 (lexical-delimiter-depth! "(\"}\" #\"[\" ; )\n \\( )"))))
    (testing
      "lexical modes handle escaped quotes and backslashes exactly"
      (is
        (=
          1
          (lexical-delimiter-depth!
            "(\"escaped \\\" quote and \\\\ slash )\" #\"escaped \\\" quote and \\\\ slash [\" \\) )"))))
    (testing
      "a trailing comment may terminate at EOF"
      (is (= 1 (count (read-forms "(ns synthetic) ; trailing comment")))))
    (testing
      "unterminated string, regex, and character modes fail closed"
      (doseq [unterminated ["\"unterminated" "#\"unterminated" "\\"]]
        (is (thrown? clojure.lang.ExceptionInfo (lexical-delimiter-depth! unterminated)))))
    (testing
      "unmatched, mismatched, and unclosed delimiters fail closed"
      (doseq [invalid [")" "([)]" "("]]
        (is (thrown? clojure.lang.ExceptionInfo (lexical-delimiter-depth! invalid)))))))

(deftest sh07-c11-source-shape-has-no-project-quotas
  (is (= 4097 (count (read-forms (apply str (repeat 4097 "nil "))))))
  (is (empty? (invalid-source-if-forms [(vec (repeat 250001 :leaf))])))
  (is (= 65 (count (invalid-source-if-forms (vec (repeat 65 '(if true)))))))
  (is (= 1 (count (invalid-source-if-forms [(apply list (cons 'if (repeat 1025 true)))]))))
  (is (= 513 (lexical-delimiter-depth! (str (apply str (repeat 513 "("))
                                           (apply str (repeat 513 ")"))))))
  (is (empty? (missing-export-definitions (source-forms)))))

(deftest sh07-c11-source-exports-have-definitions
  (let [analysis @source-analysis-cache
        forms (:forms analysis)
        exports (export-names forms)]
    (is (vector? exports))
    (is (seq exports))
    (is (= expected-source-exports exports))
    (is (empty? (missing-export-definitions forms))))
  (testing "the ordered export contract is exact"
    (is (not= expected-source-exports
              (vec (reverse expected-source-exports))))
    (is (not= expected-source-exports
              (conj expected-source-exports 'unreviewed-export))))
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
