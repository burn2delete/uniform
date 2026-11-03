(ns gravity.bootstrap.rebuild.inputs
  "Authenticate and consume one pinned stage0 compiler input snapshot.

  This namespace is bootstrap tooling.  It does not define Uniform semantics,
  issue compiler authority, or certify self-hosting.  The development-build
  entry point materializes the exact committed source snapshot named by the
  declaration, then invokes the repository's existing stage0 CLI against real
  fixtures."

  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.digest :as digest])
  (:import [java.io File]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path]))

(def schema :gravity/bootstrap-inputs-v2)

(def ^:private expected-source-revision
  {:commit "d351310d5821a983fac2ad0fee4c0be96711b4b4"
   :tree "1d7c95cf054bbc3e1719bc39cb35ac7bbaf13049"})

(def ^:private expected-scope
  {:compiler-profiles [:meta]
   :program-profiles [:meta :hosted]
   :targets [:jvm]
   :supported-modules [:reader :syntax :expand :resolve :core]
   :accepted-fixtures ["bootstrap/clojure/fixtures/accepted/profile-accepted-meta.gravity"]
   :rejected-fixtures ["bootstrap/clojure/fixtures/rejected/core-unknown-form.gravity"]})

(def ^:private expected-comparison
  {:modes [:manifest :conformance :diagnostics]
   :artifact-kinds [:gravity/stage0-core-artifact :diagnostic-stream]})

(def ^:private expected-source-roots
  [{:path "bootstrap/gravity"
    :role :gravity-compiler-source
    :extensions [".gravity" ".qst"]}
   {:path "bootstrap/clojure/src"
    :role :clojure-seed-source
    :extensions [".clj" ".cljc" ".cljs" ".edn"]}
   {:path "tools"
    :role :bootstrap-tooling-source
    :extensions [".clj" ".cljc" ".edn"]}])

;; Load-order files deliberately overlap the seed source root.  They remain
;; direct inputs as well as source-root entries because their role is both
;; source content and an ordered build input.
(def ^:private expected-direct-inputs
  [{:path "deps.edn"
    :role :build-lock
    :sha256 "sha256:a578c91e5980690b28f92f1f324abf55bca56e2386dc384fe3368f204b6628d2"}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
    :role :load-order
    :sha256 "sha256:b17383213ccc7eb95b6acf038ba63f2163ada45809f88159e11d11e1b48b8c66"}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_early_order.edn"
    :role :load-order
    :sha256 "sha256:5206a4347700e91d8f852181bf2c5077119060587287f3bdba352dea02074699"}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_mid_order.edn"
    :role :load-order
    :sha256 "sha256:9965cc33a27d60eac78b07de9e6a8ccabcf2c2f54a5d3ca3128e0948e4a5a07b"}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late_order.edn"
    :role :load-order
    :sha256 "sha256:a8412e332493fde52bcebf349b441887811ef95d1f06f2b085c7cb6d58ee31e9"}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_tail_order.edn"
    :role :load-order
    :sha256 "sha256:2ae1d76a2feefb39afbe7c571c4c4b0bd4e158963a279f5ae4e671dc4c60965d"}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm_order.edn"
    :role :load-order
    :sha256 "sha256:4be1a0c9205b198566ed239d43837c848acd9f45dbfbdefce69d650a5d199a5e"}])

(def ^:private expected-source-graph-sha256
  "sha256:c78ccb773f2e89ee3c7aaf7190703d2a3ed23f301ed76fd0bf75135a08556c50")

;; This binds every field of the checked-in declaration, including nested
;; inventories and boundary data, before any source path is consumed.
(def ^:private expected-manifest-sha256
  "sha256:ced6c4ac05c3215a627f1bdac5d8e6f3cd966ad9be0f35fe9dbb555457c6b808")

(def ^:private expected-classpath
  {:paths ["bootstrap/clojure/src" "tools"]
   :deps-file "deps.edn"
   :alias :gravity})

(def ^:private expected-load-orders
  ["bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
   "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_early_order.edn"
   "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_mid_order.edn"
   "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late_order.edn"
   "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_tail_order.edn"
   "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm_order.edn"])

(def ^:private expected-compiler-command
  ["clojure" "-Srepro" "-M:gravity" "core"])

(def ^:private expected-clojure-version "1.12.5.1654")
(def ^:private expected-seed-artifact-id
  "sha256:dfdee131460216a84fef22e537bb0d0579965f8f70338d748323dff26f8e5a8b")
(def ^:private expected-dependency-jars
  [{:file "core.specs.alpha-0.4.74.jar"
    :sha256 "sha256:eb73ac08cf49ba840c88ba67beef11336ca554333d9408808d78946e0feb9ddb"}
   {:file "spec.alpha-0.5.238.jar"
    :sha256 "sha256:94cd99b6ea639641f37af4860a643b6ed399ee5a8be5d717cff0b663c8d75077"}])

(def ^:private expected-fixtures
  [{:path "bootstrap/clojure/fixtures/accepted/profile-accepted-meta.gravity"
    :role :accepted-fixture
    :profile :meta
    :target :jvm
    :expect :accepted
    :artifact-kind :gravity/stage0-core-artifact
    :diagnostic-code nil
    :required-artifact-fields [:kind :pass :module :macro-expansion-trace
                                :expanded-syntax-object-stream :expanded-core-ast
                                :core-node-source-map :core-form-kind-records
                                :evaluation-order-metadata
                                :latent-function-effect-records :call-records
                                :diagnostics]
    :sha256 "sha256:1fdcf2d990c377f332a36c2442dd8efbdd903093932f833dbb391a6d0d4b1627"}
   {:path "bootstrap/clojure/fixtures/rejected/core-unknown-form.gravity"
    :role :rejected-fixture
    :profile :hosted
    :target :jvm
    :expect :rejected
    :artifact-kind nil
    :diagnostic-code "L2-UNKNOWN-CORE-FORM"
    :required-artifact-fields []
    :sha256 "sha256:4855c53c335fe83bca1628994869d4f231eed88042da237d51bc4846d573388d"}])

(def ^:private required-keys
  #{:schema :input-id :stage :compiler :source-revision :source-roots
    :direct-inputs :fixture-inputs :load-orders :classpath :toolchain
    :source-graph :scope :execution :comparison})

(def ^:private allowed-compiler-keys
  #{:source-language :seed-language :seed-artifact-id :command})
(def ^:private allowed-revision-keys #{:commit :tree})
(def ^:private allowed-root-keys #{:path :role :extensions})
(def ^:private allowed-direct-input-keys #{:path :role :sha256})
(def ^:private allowed-fixture-keys
  #{:path :role :profile :target :expect :artifact-kind :diagnostic-code
    :required-artifact-fields :sha256})
(def ^:private allowed-toolchain-keys
  #{:command :version :seed-artifact-id :dependency-jars :runtime :network})

(def ^:private diagnostic-codes
  {:malformed "BOOT-INPUTS-MALFORMED"
   :path "BOOT-INPUTS-PATH"
   :missing-root "BOOT-INPUTS-MISSING-ROOT"
   :missing-input "BOOT-INPUTS-MISSING-INPUT"
   :digest "BOOT-INPUTS-DIRECT-DIGEST-MISMATCH"
   :graph "BOOT-INPUTS-SOURCE-GRAPH-MISMATCH"
   :revision "BOOT-INPUTS-SOURCE-REVISION-MISMATCH"
   :toolchain "BOOT-INPUTS-TOOLCHAIN-MISMATCH"
   :fixture "BOOT-INPUTS-FIXTURE-MISMATCH"
   :execution "BOOT-INPUTS-REBUILD-FAILED"
   :incompatible "BOOT-INPUTS-INCOMPATIBLE"})

(defn- fail!
  [kind message data]
  (throw (ex-info message
                  (merge {:diagnostic (get diagnostic-codes kind)
                          :input-schema schema}
                         data))))

(defn- nonempty-string?
  [value]
  (and (string? value) (not (str/blank? value))))

(defn- sha256-hex
  [bytes]
  (digest/sha256-bytes-hex bytes))

(defn- sha256-id
  [bytes]
  (str "sha256:" (sha256-hex bytes)))

(defn- canonicalize
  "Make EDN identities deterministic across map insertion order."
  [value]
  (cond
    (map? value)
    (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
          (map (fn [[key nested]] [key (canonicalize nested)]) value))

    (vector? value) (mapv canonicalize value)
    (set? value) (->> value (map canonicalize) (sort-by pr-str) vec)
    (seq? value) (mapv canonicalize value)
    :else value))

(defn- canonical-bytes
  [value]
  (.getBytes (pr-str (canonicalize value)) StandardCharsets/UTF_8))

(defn- regular-file
  [root relative kind]
  (when-not (and (string? relative)
                 (not (str/blank? relative))
                 (not (.isAbsolute (io/file relative))))
    (fail! :path (str "bootstrap input path must be relative: " relative)
           {:path relative :kind kind}))
  (let [root-file (.getCanonicalFile (io/file (str root)))
        file (.getCanonicalFile (io/file root-file relative))
        root-path (.getPath root-file)
        file-path (.getPath file)
        prefix (str root-path File/separator)]
    (when-not (or (= root-path file-path)
                  (str/starts-with? file-path prefix))
      (fail! :path (str "bootstrap input escapes project root: " relative)
             {:path relative :kind kind}))
    file))

(defn- extension
  [^File file]
  (let [name (.getName file)
        index (.lastIndexOf name ".")]
    (if (neg? index) "" (subs name index))))

(defn- sha256-file
  [^File file]
  (sha256-id (Files/readAllBytes (.toPath file))))

(defn- validate-source-root!
  [root]
  (when-not (and (map? root)
                 (= allowed-root-keys (set (keys root)))
                 (nonempty-string? (:path root))
                 (keyword? (:role root))
                 (vector? (:extensions root))
                 (seq (:extensions root))
                 (every? #(and (string? %) (str/starts-with? % "."))
                         (:extensions root)))
    (fail! :malformed "invalid bootstrap source root declaration"
           {:root root}))
  root)

(defn- validate-direct-input!
  [input]
  (when-not (and (map? input)
                 (= allowed-direct-input-keys (set (keys input)))
                 (nonempty-string? (:path input))
                 (keyword? (:role input))
                 (and (string? (:sha256 input))
                      (re-matches #"sha256:[0-9a-f]{64}" (:sha256 input))))
    (fail! :malformed "invalid direct bootstrap input declaration"
           {:input input}))
  input)

(defn- validate-fixture-input!
  [fixture]
  (when-not (and (map? fixture)
                 (= allowed-fixture-keys (set (keys fixture)))
                 (nonempty-string? (:path fixture))
                 (keyword? (:role fixture))
                 (keyword? (:profile fixture))
                 (keyword? (:target fixture))
                 (contains? #{:accepted :rejected} (:expect fixture))
                 (or (nil? (:artifact-kind fixture))
                     (keyword? (:artifact-kind fixture)))
                 (or (nil? (:diagnostic-code fixture))
                     (and (string? (:diagnostic-code fixture))
                          (re-matches #"[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+"
                                      (:diagnostic-code fixture))))
                 (vector? (:required-artifact-fields fixture))
                 (and (string? (:sha256 fixture))
                      (re-matches #"sha256:[0-9a-f]{64}" (:sha256 fixture))))
    (fail! :malformed "invalid bootstrap fixture declaration"
           {:fixture fixture}))
  fixture)

(defn validate-manifest!
  "Validate the pinned declaration without reading source paths."
  [manifest]
  (when-not (and (map? manifest)
                 (= required-keys (set (keys manifest)))
                 (= schema (:schema manifest))
                 (nonempty-string? (:input-id manifest))
                 (= :stage0 (:stage manifest))
                 (map? (:compiler manifest))
                 (= allowed-compiler-keys (set (keys (:compiler manifest))))
                 (= :gravity (get-in manifest [:compiler :source-language]))
                 (= :clojure (get-in manifest [:compiler :seed-language]))
                 (= expected-seed-artifact-id
                    (get-in manifest [:compiler :seed-artifact-id]))
                 (= expected-compiler-command (get-in manifest [:compiler :command]))
                 (map? (:source-revision manifest))
                 (= allowed-revision-keys (set (keys (:source-revision manifest))))
                 (= expected-source-revision (:source-revision manifest))
                 (vector? (:source-roots manifest))
                 (= expected-source-roots (:source-roots manifest))
                 (= expected-direct-inputs (:direct-inputs manifest))
                 (vector? (:fixture-inputs manifest))
                 (= expected-fixtures (:fixture-inputs manifest))
                 (= expected-load-orders (:load-orders manifest))
                 (map? (:classpath manifest))
                 (= expected-classpath (:classpath manifest))
                 (map? (:toolchain manifest))
                 (= allowed-toolchain-keys (set (keys (:toolchain manifest))))
                 (= "clojure" (get-in manifest [:toolchain :command]))
                 (= expected-clojure-version (get-in manifest [:toolchain :version]))
                 (= expected-seed-artifact-id
                    (get-in manifest [:toolchain :seed-artifact-id]))
                 (= expected-dependency-jars
                    (get-in manifest [:toolchain :dependency-jars]))
                 (= :jvm (get-in manifest [:toolchain :runtime]))
                 (= :denied (get-in manifest [:toolchain :network]))
                 (map? (:source-graph manifest))
                 (= #{:sha256} (set (keys (:source-graph manifest))))
                 (= expected-source-graph-sha256
                    (get-in manifest [:source-graph :sha256]))
                 (= expected-scope (:scope manifest))
                 (map? (:execution manifest))
                 (= expected-comparison (:comparison manifest)))
    (fail! :malformed "invalid pinned bootstrap input manifest shape"
           {:manifest manifest}))
  (doseq [root (:source-roots manifest)]
    (validate-source-root! root))
  (doseq [input (:direct-inputs manifest)]
    (validate-direct-input! input))
  (doseq [fixture (:fixture-inputs manifest)]
    (validate-fixture-input! fixture))
  (when-not (= (count (:direct-inputs manifest))
               (count (set (map :path (:direct-inputs manifest)))))
    (fail! :malformed "pinned bootstrap direct inputs contain duplicate paths"
           {:paths (mapv :path (:direct-inputs manifest))}))
  (when-not (and (= :jvm (get-in manifest [:execution :target]))
                 (= :jvm (get-in manifest [:execution :backend]))
                 (= :clojure-stage0 (get-in manifest [:execution :runtime]))
                 (= [:clojure :jvm :filesystem]
                    (get-in manifest [:execution :host-boundaries]))
                 (= :denied (get-in manifest [:execution :network])))
    (fail! :incompatible "bootstrap input execution target is unsupported"
           {:execution (:execution manifest)}))
  (when-not (and (vector? (get-in manifest [:comparison :modes]))
                 (seq (get-in manifest [:comparison :modes]))
                 (vector? (get-in manifest [:comparison :artifact-kinds]))
                 (seq (get-in manifest [:comparison :artifact-kinds])))
    (fail! :malformed "invalid bootstrap comparison declaration"
           {:comparison (:comparison manifest)}))
  (when-not (= expected-manifest-sha256
             (sha256-id (canonical-bytes manifest)))
    (fail! :malformed "bootstrap manifest identity does not match declaration"
           {:expected expected-manifest-sha256
            :actual (sha256-id (canonical-bytes manifest))}))
  manifest)

(defn read-manifest
  "Read and structurally validate an EDN bootstrap input declaration."
  [manifest-path]
  (let [file (.getCanonicalFile (io/file manifest-path))]
    (when-not (.isFile file)
      (fail! :missing-input (str "bootstrap input manifest is missing: " manifest-path)
             {:path manifest-path}))
    (try
      (validate-manifest! (edn/read-string (slurp file)))
      (catch clojure.lang.ExceptionInfo error
        (throw error))
      (catch Exception error
        (fail! :malformed "bootstrap input manifest is not valid EDN"
               {:path manifest-path :cause (.getMessage error)})))))

(defn- root-files
  [project-root {:keys [path role extensions]}]
  (let [root (regular-file project-root path :source-root)]
    (when-not (.isDirectory root)
      (fail! :missing-root (str "bootstrap source root is missing: " path)
             {:path path :role role}))
    (let [root-path (.toPath root)]
      (with-open [stream (Files/walk root-path
                                  (make-array java.nio.file.FileVisitOption 0))]
        (->> (.iterator stream)
             iterator-seq
             (map (fn [^Path file]
                    (when (Files/isSymbolicLink file)
                      (fail! :path "symbolic links are not bootstrap inputs"
                             {:path (str file) :kind :source-root}))
                    file))
             (filter #(Files/isRegularFile ^Path % (make-array LinkOption 0)))
             (map #(.toFile ^Path %))
             (filter #(some #{(extension %)} extensions))
             (map (fn [^File file]
                    (let [relative (.relativize (.toPath (.getCanonicalFile (io/file project-root)))
                                                (.toPath (.getCanonicalFile file)))
                          relative (str/replace (.toString relative) File/separator "/")]
                      (regular-file project-root relative :source-root-file)
                      {:path relative
                       :role role
                       :bytes (.length file)
                       :sha256 (sha256-file file)})))
             vec)))))

(defn- direct-files
  [project-root direct-inputs]
  (mapv (fn [{:keys [path role sha256]}]
          (let [file (regular-file project-root path :direct-input)]
            (when-not (.isFile file)
              (fail! :missing-input (str "bootstrap direct input is missing: " path)
                     {:path path :role role}))
            (let [actual (sha256-file file)]
              (when-not (= sha256 actual)
                (fail! :digest (str "bootstrap direct input digest mismatch: " path)
                       {:path path :expected sha256 :actual actual :role role}))
              {:path path :role role :bytes (.length file) :sha256 actual})))
        direct-inputs))

(defn- merge-graph-entries
  [entries]
  (->> entries
       (reduce (fn [by-path entry] (assoc by-path (:path entry) entry)) {})
       vals
       (sort-by (juxt :path :role))
       vec))

(defn source-graph
  "Return the canonical, freshly observed source and pinned-input graph."
  [project-root manifest]
  (merge-graph-entries
   (concat (mapcat #(root-files project-root %) (:source-roots manifest))
           (direct-files project-root (:direct-inputs manifest))
           (direct-files project-root (:fixture-inputs manifest)))))

(defn source-graph-sha256
  "Hash a source graph after canonical ordering and serialization."
  [entries]
  (sha256-id (canonical-bytes entries)))

(defn- manifest-sha256
  [manifest]
  (sha256-id (canonical-bytes manifest)))

(defn- load-inputs-from-manifest!
  [project-root manifest]
  (let [entries (source-graph project-root manifest)
        actual (source-graph-sha256 entries)
        expected (get-in manifest [:source-graph :sha256])]
    (when-not (= expected actual)
      (fail! :graph "bootstrap source graph identity mismatch"
             {:expected expected :actual actual
              :input-id (:input-id manifest)
              :entry-count (count entries)}))
    {:manifest manifest
     :manifest-sha256 (manifest-sha256 manifest)
     :project-root (.getCanonicalPath (io/file project-root))
     :source-graph {:sha256 actual :entries entries}
     :compiler (:compiler manifest)
     :scope (:scope manifest)
     :execution (:execution manifest)
     :comparison (:comparison manifest)}))

(defn load-inputs!
  "Authenticate a manifest and all files under the supplied root."
  [project-root manifest-path]
  (load-inputs-from-manifest! project-root (read-manifest manifest-path)))

(defn- command-result
  [working-root command]
  (let [stdout-file (Files/createTempFile "gravity-bootstrap-stdout-" ".bin"
                                          (make-array java.nio.file.attribute.FileAttribute 0))
        stderr-file (Files/createTempFile "gravity-bootstrap-stderr-" ".bin"
                                          (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [process-builder (doto (ProcessBuilder. ^java.util.List (vec command))
                              (.directory (.getCanonicalFile (io/file (str working-root))))
                              (.redirectOutput (.toFile stdout-file))
                              (.redirectError (.toFile stderr-file)))
            process (try
                      (.start process-builder)
                      (catch Exception error
                        (fail! :execution "bootstrap command could not start"
                               {:command command :cause (.getMessage error)})))
            exit-code (.waitFor process)
            stdout (Files/readAllBytes stdout-file)
            stderr (Files/readAllBytes stderr-file)]
        {:command (vec command)
         :exit-code exit-code
         :stdout-bytes stdout
         :stderr-bytes stderr
         :stdout-sha256 (sha256-id stdout)
         :stderr-sha256 (sha256-id stderr)})
      (finally
        (Files/deleteIfExists stdout-file)
        (Files/deleteIfExists stderr-file)))))

(defn- command-text
  [bytes]
  (String. ^bytes bytes StandardCharsets/UTF_8))

(defn- output-edn
  [bytes fixture]
  (try
    (with-open [reader (java.io.PushbackReader.
                        (java.io.StringReader. (command-text bytes)))]
      (let [eof (Object.)
            value (edn/read {:eof eof} reader)
            trailing (edn/read {:eof eof} reader)]
        (when (or (identical? eof value)
                  (not (identical? eof trailing)))
          (fail! :fixture "accepted compiler output is not one EDN artifact"
                 {:path (:path fixture)}))
        value))
    (catch clojure.lang.ExceptionInfo error
      (throw error))
    (catch Exception error
      (fail! :fixture "accepted compiler output is not valid EDN"
             {:path (:path fixture) :cause (.getMessage error)}))))

(defn- valid-artifact?
  [artifact fixture]
  (and (map? artifact)
       (= (:artifact-kind fixture) (:kind artifact))
       (= :core-lowering (get-in artifact [:pass :name]))
       (= :expanded-syntax (get-in artifact [:pass :input]))
       (= :core-ast (get-in artifact [:pass :output]))
       (= (:profile fixture) (get-in artifact [:module :profile]))
       (= (:target fixture) (get-in artifact [:module :target]))
       (every? #(contains? artifact %) (:required-artifact-fields fixture))))

(defn- extract-diagnostic-codes
  [bytes]
  (->> (re-seq #"\b[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+\b"
                (command-text bytes))
       distinct
       vec))

(defn- git-output!
  [project-root arguments]
  (let [result (command-result project-root (into ["git"] arguments))]
    (when-not (zero? (:exit-code result))
      (fail! :revision "bootstrap git command failed"
             (select-keys result [:command :exit-code :stdout-sha256 :stderr-sha256])))
    (str/trim (command-text (:stdout-bytes result)))))

(defn- verify-revision!
  [project-root]
  (let [commit (git-output! project-root ["rev-parse" (:commit expected-source-revision)])
        tree (git-output! project-root ["rev-parse" (str (:commit expected-source-revision) "^{tree}")])]
    (when-not (= (:commit expected-source-revision) commit)
      (fail! :revision "bootstrap source commit resolved unexpectedly"
             {:expected (:commit expected-source-revision) :actual commit}))
    (when-not (= (:tree expected-source-revision) tree)
      (fail! :revision "bootstrap source tree resolved unexpectedly"
             {:expected (:tree expected-source-revision) :actual tree}))
    expected-source-revision))

(defn- archive-paths
  [manifest]
  (->> (concat (map :path (:source-roots manifest))
               (map :path (:direct-inputs manifest))
               (map :path (:fixture-inputs manifest)))
       distinct
       vec))

(defn- delete-tree!
  [path]
  (when (and path (Files/exists path (make-array LinkOption 0)))
    (with-open [stream (Files/walk path
                                  (make-array java.nio.file.FileVisitOption 0))]
      (doseq [^Path child (sort-by #(.getNameCount ^Path %)
                                   >
                                   (iterator-seq (.iterator stream)))]
        (Files/deleteIfExists child)))))

(defn- materialize-source!
  [project-root manifest]
  (verify-revision! project-root)
  (let [snapshot (Files/createTempDirectory "gravity-bootstrap-source-"
                                            (make-array java.nio.file.attribute.FileAttribute 0))
        archive (Files/createTempFile "gravity-bootstrap-source-" ".tar"
                                      (make-array java.nio.file.attribute.FileAttribute 0))
        archive-command (into ["git" "archive" "--format=tar"
                               "--output" (.toString archive)
                               (:commit expected-source-revision) "--"]
                              (archive-paths manifest))]
    (try
      (let [archive-result (command-result project-root archive-command)]
        (when-not (zero? (:exit-code archive-result))
          (fail! :revision "bootstrap source archive failed"
                 (select-keys archive-result [:command :exit-code :stdout-sha256 :stderr-sha256])))
        (let [extract-result (command-result project-root
                                              ["tar" "-xf" (.toString archive)
                                              "-C" (.toString snapshot)])]
          (when-not (zero? (:exit-code extract-result))
            (fail! :revision "bootstrap source archive extraction failed"
                   (select-keys extract-result [:command :exit-code :stdout-sha256 :stderr-sha256])))
          {:root snapshot
           :archive archive
           :archive-sha256 (sha256-file (.toFile archive))
           :revision expected-source-revision}))
      (catch Exception error
        (delete-tree! snapshot)
        (Files/deleteIfExists archive)
        (throw error)))))

(defn- classpath-entry-file
  [snapshot-root entry]
  (let [file (io/file entry)]
    (.getCanonicalFile
     (if (.isAbsolute file)
       file
       (io/file (str snapshot-root) entry)))))

(defn- child-classpath-entries
  [snapshot-root bytes]
  (let [classpath (str/trim (command-text bytes))
        separator (re-pattern (java.util.regex.Pattern/quote File/pathSeparator))]
    (when (str/blank? classpath)
      (fail! :toolchain "child Clojure -Spath returned an empty classpath" {}))
    (let [entries (->> (str/split classpath separator)
                       (map str/trim)
                       (remove str/blank?)
                       (map #(classpath-entry-file snapshot-root %))
                       vec)]
      (when-not (= (count entries) (count (set entries)))
        (fail! :toolchain "child Clojure -Spath returned duplicate entries"
               {:classpath classpath}))
      (doseq [^File entry entries]
        (when-not (.exists entry)
          (fail! :toolchain "child Clojure -Spath named a missing entry"
                 {:path (.getPath entry)})))
      entries)))

(defn- relative-classpath-path
  [snapshot-root ^File entry]
  (let [root (.toPath (.getCanonicalFile (io/file (str snapshot-root))))
        path (.toPath (.getCanonicalFile entry))]
    (if (.startsWith path root)
      (str/replace (.toString (.relativize root path)) File/separator "/")
      (.getPath (.getCanonicalFile entry)))))

(defn- child-classpath-record
  [snapshot-root classpath-bytes manifest]
  (let [entries (child-classpath-entries snapshot-root classpath-bytes)
        directories (filter #(.isDirectory ^File %) entries)
        jars (filter #(and (.isFile ^File %)
                           (str/ends-with? (.getName ^File %) ".jar"))
                     entries)
        unsupported (remove #(or (.isDirectory ^File %)
                                 (and (.isFile ^File %)
                                      (str/ends-with? (.getName ^File %) ".jar")))
                            entries)
        expected-directories (mapv #(classpath-entry-file snapshot-root %)
                                   (get-in manifest [:classpath :paths]))]
    (when-not (= (set expected-directories) (set directories))
      (fail! :toolchain "child Clojure classpath directories differ from declaration"
             {:expected (mapv #(.getPath ^File %) expected-directories)
              :actual (mapv #(.getPath ^File %) directories)}))
    (when (seq unsupported)
      (fail! :toolchain "child Clojure classpath contains an unsupported entry"
             {:entries (mapv #(.getPath ^File %) unsupported)}))
    (let [jar-records (mapv (fn [^File jar]
                              {:file (.getName jar)
                               :sha256 (sha256-file jar)})
                            jars)
          seeds (filter #(re-matches #"clojure-[^/]+\.jar" (:file %))
                        jar-records)]
      (when-not (= 1 (count seeds))
        (fail! :toolchain "child Clojure classpath did not select one seed jar"
               {:jars jar-records}))
      (let [seed (first seeds)
            dependency-jars (vec (remove #(= seed %) jar-records))]
        (when-not (= (get-in manifest [:toolchain :seed-artifact-id])
                     (:sha256 seed))
          (fail! :toolchain "child-selected Clojure seed identity does not match declaration"
                 {:expected (get-in manifest [:toolchain :seed-artifact-id])
                  :actual (:sha256 seed)
                  :file (:file seed)}))
        (when-not (= (get-in manifest [:toolchain :dependency-jars])
                     dependency-jars)
          (fail! :toolchain "child dependency jar identities do not match declaration"
                 {:expected (get-in manifest [:toolchain :dependency-jars])
                  :actual dependency-jars}))
        {:seed-artifact-id (:sha256 seed)
         :dependency-jars dependency-jars
         :classpath-entries
         (mapv (fn [^File entry]
                 (if (.isDirectory entry)
                   {:kind :directory
                    :path (relative-classpath-path snapshot-root entry)}
                   (let [jar (some #(when (= (:file %) (.getName entry)) %) jar-records)]
                     {:kind :jar
                      :file (:file jar)
                      :sha256 (:sha256 jar)})))
               entries)}))))

(defn- toolchain-record!
  [snapshot-root manifest]
  (let [describe (command-result snapshot-root ["clojure" "-Srepro" "-Sdescribe"])
        classpath (command-result snapshot-root ["clojure" "-Srepro" "-Spath"])
        describe-data (try
                        (edn/read-string (command-text (:stdout-bytes describe)))
                        (catch Exception error
                          (fail! :toolchain "clojure -Sdescribe did not return EDN"
                                 {:cause (.getMessage error)
                                  :stdout-sha256 (:stdout-sha256 describe)})))]
    (when-not (and (zero? (:exit-code describe))
                   (= expected-clojure-version (:version describe-data))
                   (zero? (:exit-code classpath)))
      (fail! :toolchain "declared Clojure toolchain was not observed"
             {:describe (select-keys describe [:exit-code :stdout-sha256 :stderr-sha256])
              :classpath (select-keys classpath [:exit-code :stdout-sha256 :stderr-sha256])
              :version (:version describe-data)}))
    (let [{:keys [seed-artifact-id dependency-jars classpath-entries]}
          (child-classpath-record snapshot-root (:stdout-bytes classpath) manifest)]
      {:command "clojure"
       :version (:version describe-data)
       :seed-artifact-id seed-artifact-id
       :dependency-jars dependency-jars
       :runtime :jvm
       :sdescribe-sha256 (:stdout-sha256 describe)
       :classpath-sha256 (:stdout-sha256 classpath)
       :classpath-entries classpath-entries})))

(defn- run-fixture!
  [snapshot-root manifest fixture]
  (let [result (command-result snapshot-root
                               (conj (get-in manifest [:compiler :command]) (:path fixture)))
        combined (byte-array (+ (alength ^bytes (:stdout-bytes result))
                                (alength ^bytes (:stderr-bytes result))))
        _ (System/arraycopy ^bytes (:stdout-bytes result) 0 combined 0
                            (alength ^bytes (:stdout-bytes result)))
        _ (System/arraycopy ^bytes (:stderr-bytes result) 0 combined
                            (alength ^bytes (:stdout-bytes result))
                            (alength ^bytes (:stderr-bytes result)))
        accepted? (= :accepted (:expect fixture))
        artifact (when accepted?
                   (output-edn (:stdout-bytes result) fixture))
        artifact-valid? (and accepted? (valid-artifact? artifact fixture))
        codes (when-not accepted? (extract-diagnostic-codes combined))
        passed? (if accepted?
                  (and (zero? (:exit-code result))
                       artifact-valid?)
                  (and (not (zero? (:exit-code result)))
                       (pos? (alength combined))
                       (some #{(:diagnostic-code fixture)} codes)))]
    (when-not passed?
      (fail! :fixture "stage0 compiler fixture did not meet its declared outcome"
             {:path (:path fixture)
              :expect (:expect fixture)
              :exit-code (:exit-code result)
              :stdout-sha256 (:stdout-sha256 result)
              :stderr-sha256 (:stderr-sha256 result)
              :diagnostic-codes codes
              :artifact-kind (when (map? artifact) (:kind artifact))}))
    (assoc (select-keys result [:command :exit-code :stdout-sha256 :stderr-sha256])
           :path (:path fixture)
           :expect (:expect fixture)
           :artifact-kind (when (map? artifact) (:kind artifact))
           :artifact-id (when accepted? (sha256-id (canonical-bytes artifact)))
           :diagnostic-id (when-not accepted? (sha256-id combined))
           :diagnostic-codes codes)))

(defn run-development-build!
  "Consume the pinned source snapshot through the existing stage0 CLI.

  The source revision is materialized in an isolated temporary root.  The
  compiler command and fixture outcomes are observed from that process; no
  caller-supplied executor or result identity is accepted as evidence."
  [project-root manifest-path]
  (let [manifest (read-manifest manifest-path)
        snapshot (materialize-source! project-root manifest)]
    (try
      (let [snapshot-root (.toString ^Path (:root snapshot))
            inputs (load-inputs-from-manifest! snapshot-root manifest)
            toolchain (toolchain-record! snapshot-root manifest)
            fixtures (mapv #(run-fixture! snapshot-root manifest %)
                           (:fixture-inputs manifest))
            accepted (some :artifact-id fixtures)]
        (when-not accepted
          (fail! :fixture "pinned bootstrap run produced no accepted artifact"
                 {:fixtures (mapv #(select-keys % [:path :expect :exit-code]) fixtures)}))
        {:inputs inputs
         :result {:artifact-id accepted
                  :manifest-sha256 (:manifest-sha256 inputs)
                  :source-graph-sha256 (get-in inputs [:source-graph :sha256])
                  :source-revision (:revision snapshot)
                  :source-archive-sha256 (:archive-sha256 snapshot)
                  :toolchain toolchain
                  :fixtures fixtures}})
      (finally
        (delete-tree! (:root snapshot))
        (Files/deleteIfExists (:archive snapshot))))))
