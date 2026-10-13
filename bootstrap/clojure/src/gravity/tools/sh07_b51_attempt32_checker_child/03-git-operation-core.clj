
(defn- git-operation-core! [options operation arguments maximum]
  (run-project-free-process!
   (get-in options [:environment :working-directory])
   (into ["/usr/bin/git" "-C"
          (get-in options [:environment :working-directory])]
         arguments)
   maximum operation))

(defn- nul-records-core! [bytes reason]
  (let [text (strict-utf8-core! bytes reason)]
    (cond
      (empty? text) []
      (not (str/ends-with? text "\u0000")) (reject! reason)
      :else (vec (butlast (str/split text #"\u0000" -1))))))

(defn- parse-show-core! [bytes]
  (let [match
        (re-matches #"([0-9a-f]{40})\n([0-9a-f]{40})\n([0-9a-f ]*)\n"
                    (strict-utf8-core! bytes :git-show-grammar))]
    (when-not match (reject! :git-show-grammar))
    {:commit (nth match 1) :tree (nth match 2)
     :parents (let [parents (nth match 3)]
                (if (str/blank? parents) [] (str/split parents #" ")))}))

(defn- parse-tree-rows-core! [bytes]
  (mapv
   (fn [record]
     (let [match
           (re-matches #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                       record)]
       (when-not match (reject! :git-ls-tree-grammar))
       {:mode (nth match 1) :type (nth match 2)
        :oid (nth match 3) :path (nth match 4)}))
   (nul-records-core! bytes :git-ls-tree-grammar)))

(defn- git-path-object-core! [options path]
  (let [commit (get-in options [:content :commit])
        tree (git-operation-core! options :ls-tree-z-requested
                                  ["ls-tree" "-z" commit "--" path]
                                  1048576)
        rows (parse-tree-rows-core! (:stdout-bytes tree))
        row (first rows)]
    (when-not (and (= 1 (count rows)) (= path (:path row))
                   (= "100644" (:mode row)) (= "blob" (:type row)))
      (reject! :c-source-object {:path path}))
    (let [blob (git-operation-core! options :cat-file-blob
                                    ["cat-file" "blob" (:oid row)]
                                    (:response-max-bytes transport-limits))
          bytes (:stdout-bytes blob)]
      {:path path :mode "100644" :blob-oid (:oid row)
       :byte-count (alength ^bytes bytes)
       :raw-sha256 (sha256-bytes-core bytes) :bytes bytes})))

(defn- source-forms-core! [bytes]
  (strict-utf8-core! bytes :trusted-tcb-utf8)
  (with-open [reader
              (LineNumberingPushbackReader.
               (InputStreamReader. (ByteArrayInputStream. bytes)
                                   StandardCharsets/UTF_8))]
    (binding [*read-eval* false]
      (let [eof (Object.)]
        (loop [forms []]
          (let [form (read {:eof eof :read-cond :allow :features #{:clj}}
                           reader)]
            (if (identical? eof form) forms
                (recur (conj forms form)))))))))

(defn- require-spec-namespace-core [spec]
  (cond (symbol? spec) spec
        (and (vector? spec) (symbol? (first spec))) (first spec)
        :else nil))

(defn- source-namespace-core! [bytes path]
  (let [forms (source-forms-core! bytes)
        ns-forms (filter #(and (seq? %) (= 'ns (first %))) forms)]
    (when (and (seq ns-forms)
               (or (not= 1 (count ns-forms))
                   (not= (first forms) (first ns-forms))))
      (reject! :trusted-tcb-namespace {:path path}))
    (when-let [ns-form (first ns-forms)]
      {:namespace (second ns-form)
       :requires-project-namespaces
       (->> (drop 2 ns-form)
            (filter #(and (seq? %) (= :require (first %))))
            (mapcat rest) (keep require-spec-namespace-core)
            (filter #(str/starts-with? (str %) "gravity.")) vec)})))

(defn- symlink-free-core? [^Path path]
  (loop [current (.normalize (.toAbsolutePath path))]
    (cond (nil? current) true
          (Files/isSymbolicLink current) false
          :else (recur (.getParent current)))))

(defn- materialized-source-core! [options path]
  (when-not (str/starts-with? path classpath-source-prefix)
    (reject! :materialized-source-path {:path path}))
  (let [root (Paths/get (first (:classpath options)) (make-array String 0))
        file (.normalize (.resolve root
                                   (subs path (count classpath-source-prefix))))]
    (when-not (and (.startsWith file root)
                   (Files/isRegularFile
                    file (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? file))
      (reject! :materialized-source-path {:path path}))
    (with-open [input (Files/newInputStream
                       file (make-array java.nio.file.OpenOption 0))]
      (read-bounded-core! input (:response-max-bytes transport-limits)
                          :materialized-source-overflow))))

(defn pre-manifest-tcb!
  "Authenticate the child entrypoint and codec leaves from C before codec load."
  [options]
  (let [show (git-operation-core!
              options :show-tree
              ["show" "-s" "--format=%H%n%T%n%P"
               (get-in options [:content :commit])] 4096)
        shown (parse-show-core! (:stdout-bytes show))
        _ (when-not (= (:content options) (select-keys shown [:commit :tree]))
            (reject! :content-coordinate))
        entry-spec
        {:ordinal 0
         :path "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
         :namespace 'gravity.tools.sh07-b51-attempt32-checker-child
         :requires-project-namespaces []}
        row-for
        (fn [spec]
          (let [object (git-path-object-core! options (:path spec))
                materialized (materialized-source-core! options (:path spec))
                parsed (source-namespace-core! (:bytes object) (:path spec))]
            (when-not (and (Arrays/equals ^bytes (:bytes object)
                                          ^bytes materialized)
                           (= (:namespace spec) (:namespace parsed))
                           (= (:requires-project-namespaces spec)
                              (:requires-project-namespaces parsed)))
              (reject! :trusted-tcb-source {:path (:path spec)}))
            {:ordinal (:ordinal spec) :path (:path spec)
             :namespace (:namespace spec) :mode "100644"
             :blob-oid (:blob-oid object) :byte-count (:byte-count object)
             :raw-sha256 (:raw-sha256 object)
             :materialized-raw-sha256 (sha256-bytes-core materialized)
             :requires-project-namespaces
             (:requires-project-namespaces spec)}))
        entrypoint (row-for entry-spec)
        members (mapv row-for codec-tcb-specs)
        result
        {:entrypoint entrypoint
         :codec-tcb {:schema :gravity/sh07-b51-codec-tcb-v1
                     :load-order codec-tcb-load-order :members members}}]
    (when-not (and (= pre-manifest-row-keys (set (keys entrypoint)))
                   (every? #(= pre-manifest-row-keys (set (keys %))) members)
                   (= codec-tcb-load-order (mapv :namespace members))
                   (every? #(= (:raw-sha256 %)
                               (:materialized-raw-sha256 %))
                           (into [entrypoint] members)))
      (reject! :trusted-tcb))
    result))

(defn- resolve-public! [namespace-name var-name]
  (or (ns-resolve (symbol namespace-name) (symbol var-name))
      (reject! :missing-public-var
               {:namespace namespace-name :var var-name})))

(defn load-codec!
  "Load the one shared V9 codec and its immutable transport API value."
  []
  (require (symbol codec-namespace))
  (let [transport @(resolve-public! codec-namespace "transport-api")
        api (merge {:H (resolve-public! codec-namespace "H")
                    :W (resolve-public! codec-namespace "W")
                    :R (resolve-public! codec-namespace "R")
                    :limits @(resolve-public! codec-namespace
                                              "transport-limits")}
                   transport)]
    (when-not (= (dissoc transport-limits :deadline-ms) (:limits api))
      (reject! :codec-limits {:actual (:limits api)}))
    api))

(defn- file-key-core! [^Path path]
  (let [attributes
        (Files/readAttributes
         path BasicFileAttributes
         (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
        key (.fileKey ^BasicFileAttributes attributes)]
    (when-not key (reject! :classpath-file-key {:path (str path)}))
    (str key)))

(defn- canonical-file-core! [path]
  (let [value (.normalize (.toAbsolutePath
                           (Paths/get path (make-array String 0))))]
    (when-not (and (Files/isRegularFile
                    value (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? value))
      (reject! :classpath-file {:path path}))
    (.toRealPath value (make-array LinkOption 0))))

(defn- jar-source-records-core! [^Path path codec]
  (with-open [jar (JarFile. (.toFile path))]
    (let [records
          (->> (enumeration-seq (.entries jar))
               (remove #(.isDirectory %))
               (filter #(or (str/ends-with? (.getName %) ".clj")
                            (str/ends-with? (.getName %) ".cljc")))
               (keep
                (fn [entry]
                  (with-open [input (.getInputStream jar entry)]
                    (let [bytes (read-bounded-core!
                                 input 67108864 :classpath-jar-entry-overflow)
                          parsed (source-namespace-core! bytes (.getName entry))]
                      (when parsed
                        [(:namespace parsed)
                         {:namespace (:namespace parsed)
                          :resource (.getName entry)
                          :byte-count (alength ^bytes bytes)
                          :source-sha256 ((:R codec) bytes)}])))))
               vec)
          result (into {} records)]
      (when-not (= (count records) (count result))
        (reject! :classpath-jar-namespace-duplicate {:path (str path)}))
      result)))

(defn- jar-classpath-entry-core! [path spec codec]
  (let [canonical (canonical-file-core! path)
        filename (str (.getFileName canonical))]
    (when-not (str/ends-with? filename (str (:version spec) ".jar"))
      (reject! :classpath-coordinate
               {:ordinal (:ordinal spec) :file filename}))
    (let [bytes (with-open [input (Files/newInputStream
                                   canonical
                                   (make-array java.nio.file.OpenOption 0))]
                  (read-bounded-core! input 1073741824
                                      :classpath-jar-overflow))
          records (jar-source-records-core! canonical codec)
          namespaces (->> (keys records) (sort-by str) vec)]
      (when-not (some #{(:required-namespace spec)} namespaces)
        (reject! :classpath-coordinate
                 {:ordinal (:ordinal spec)
                  :required-namespace (:required-namespace spec)}))
      {:entry
       {:ordinal (:ordinal spec) :kind :jar
        :logical-coordinate
        {:artifact (:logical-coordinate spec) :version (:version spec)}
        :canonical-path (str canonical) :file-key (file-key-core! canonical)
        :byte-count (alength ^bytes bytes) :sha256 ((:R codec) bytes)
        :provided-namespaces namespaces}
       :external-source-records records})))
