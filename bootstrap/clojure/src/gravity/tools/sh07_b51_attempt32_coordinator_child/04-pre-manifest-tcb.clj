
(defn pre-manifest-tcb!
  "Authenticate the entrypoint and three codec leaves from C objects before
  loading any project namespace."
  [options entrypoint-namespace entrypoint-path]
  (show-content-core! options)
  (let [entrypoint-spec
        {:ordinal 0 :path entrypoint-path :namespace entrypoint-namespace
         :requires-project-namespaces []}
        row-for
        (fn [spec]
          (let [object (git-path-object-core! options (:path spec))
                materialized
                (materialized-source-bytes-core!
                 options (:path spec) (:response-max-bytes transport-limits))
                parsed (source-namespace-core! (:bytes object) (:path spec))]
            (when-not
             (and (Arrays/equals ^bytes (:bytes object) ^bytes materialized)
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
        entrypoint (row-for entrypoint-spec)
        members (mapv row-for codec-tcb-specs)
        codec-tcb {:schema :gravity/sh07-b51-codec-tcb-v1
                   :load-order codec-tcb-load-order :members members}
        result {:entrypoint entrypoint :codec-tcb codec-tcb}]
    (when-not (and (= pre-manifest-row-keys (set (keys entrypoint)))
                   (every? #(= pre-manifest-row-keys (set (keys %))) members)
                   (= (mapv :namespace members) codec-tcb-load-order)
                   (every? #(= (:raw-sha256 %)
                               (:materialized-raw-sha256 %))
                           (into [entrypoint] members)))
      (reject! :trusted-tcb))
    result))

(defn- resolve-public! [namespace-name var-name]
  (or (ns-resolve (symbol namespace-name) (symbol var-name))
      (reject! :missing-public-var
               {:namespace namespace-name :var var-name})))

(defn load-codec! []
  (require (symbol codec-namespace))
  (let [transport @(resolve-public! codec-namespace "transport-api")
        api
        (merge {:H (resolve-public! codec-namespace "H")
                :W (resolve-public! codec-namespace "W")
                :R (resolve-public! codec-namespace "R")
                :limits @(resolve-public! codec-namespace "transport-limits")}
               transport)]
    (when-not (= (dissoc transport-limits :deadline-ms) (:limits api))
      (reject! :codec-limits {:actual (:limits api)}))
    api))

(defn- parse-recursive-tree-core! [bytes]
  (let [records (nul-records-core! bytes :classpath-tree-grammar)
        rows
        (mapv
         (fn [record]
           (let [match
                 (re-matches #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                             record)]
             (when-not match (reject! :classpath-tree-grammar))
             {:mode (nth match 1) :type (nth match 2)
              :oid (nth match 3) :path (nth match 4)}))
         records)]
    (when-not (and (seq rows)
                   (= (count rows) (count (set (map :path rows))))
                   (every? #(and (= "100644" (:mode %))
                                 (= "blob" (:type %))
                                 (str/starts-with? (:path %)
                                                   classpath-source-prefix))
                           rows))
      (reject! :classpath-tree))
    rows))

(defn- git-blob-core! [options oid]
  (let [operation
        (git-operation-core! options :cat-file-blob
                             ["cat-file" "blob" oid]
                             (:response-max-bytes transport-limits))]
    (:stdout-bytes operation)))

(defn- file-key-core! [^Path path]
  (let [attributes
        (Files/readAttributes
         path BasicFileAttributes
         (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
        key (.fileKey ^BasicFileAttributes attributes)]
    (when-not key (reject! :classpath-file-key {:path (str path)}))
    (str key)))

(defn- canonical-file-core! [path]
  (let [value (Paths/get path (make-array String 0))
        absolute (.normalize (.toAbsolutePath value))]
    (when-not (and (Files/isRegularFile
                    absolute
                    (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-core? absolute))
      (reject! :classpath-file {:path path}))
    (.toRealPath absolute (make-array LinkOption 0))))

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
                    (let [bytes
                          (read-bounded-core! input 67108864
                                              :classpath-jar-entry-overflow)
                          parsed
                          (source-namespace-core! bytes (.getName entry))]
                      (when parsed
                        [(:namespace parsed)
                         {:namespace (:namespace parsed)
                          :resource (.getName entry)
                          :byte-count (alength ^bytes bytes)
                          :source-sha256 ((:R codec) bytes)}])))))
               vec)
          by-namespace (into {} records)]
      (when-not (= (count records) (count by-namespace))
        (reject! :classpath-jar-namespace-duplicate {:path (str path)}))
      by-namespace)))

(defn- jar-classpath-entry-core! [path spec codec]
  (let [canonical (canonical-file-core! path)
        filename (str (.getFileName canonical))
        expected-tail (str (:version spec) ".jar")]
    (when-not (str/ends-with? filename expected-tail)
      (reject! :classpath-coordinate
               {:ordinal (:ordinal spec) :file filename
                :version (:version spec)}))
    (let [bytes
          (with-open [input (Files/newInputStream
                             canonical
                             (make-array java.nio.file.OpenOption 0))]
            (read-bounded-core! input 1073741824 :classpath-jar-overflow))
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
