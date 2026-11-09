(ns ci.public-diagnostic-report
  "Read retained public-assessment diagnostics without replay or mutation."
  (:refer-clojure :exclude [run!])
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import [java.io PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file FileVisitOption Files LinkOption Path Paths]
           [java.security MessageDigest]))

(def diagnostic-schema
  "gravity/ci-public-diagnostic-report-v1")

(def ^:private producer-schema
  "gravity/ci-public-release-diagnostic-v1")

(def ^:private accepted-result-keys
  [:check-result :run-result :compile-result :executable-result])

(def ^:private rejected-result-keys
  [:result])

(def ^:private marker-edges
  #{:enter :return :throw :diagnostic-captured :manifest-written
    :diagnostic-capture-failed :diagnostic-capture-skipped :unavailable})

(def ^:private no-follow-links
  (make-array LinkOption 0))

(defn- fail!
  ([message] (fail! message {}))
  ([message data]
   (throw (ex-info message data))))

(defn- nonempty-string!
  [label value]
  (when-not (and (string? value) (not (str/blank? value)))
    (fail! (str label " must be a non-empty string")
           {:label label :type (some-> value class .getName)}))
  value)

(defn- sha256-bytes
  [^bytes bytes]
  (let [digest (doto (MessageDigest/getInstance "SHA-256")
                 (.update bytes))]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff))
                    (.digest digest)))))

(defn- strict-utf8
  [^bytes bytes label]
  (try
    (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                    (.onMalformedInput CodingErrorAction/REPORT)
                    (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (.toString (.decode decoder (ByteBuffer/wrap bytes))))
    (catch Throwable error
      (fail! (str "Malformed UTF-8 in " label)
             {:label label :error-class (.getName (class error))}))))

(defn- root-path!
  [artifact-dir]
  (nonempty-string! ":artifact-dir" artifact-dir)
  (let [path (.normalize (.toAbsolutePath
                          (Paths/get artifact-dir (make-array String 0))))]
    (when (Files/isSymbolicLink path)
      (fail! "Artifact directory must not be a symbolic link"
             {:path (str path)}))
    (when-not (Files/isDirectory path no-follow-links)
      (fail! "Artifact directory is not a directory"
             {:path (str path)}))
    path))

(defn- path-segments
  [^Path path]
  (mapv #(str (.getName path %)) (range (.getNameCount path))))

(defn- canonical-relative!
  [value label]
  (nonempty-string! label value)
  (when (or (str/includes? value "\u0000")
            (str/includes? value "\\"))
    (fail! (str label " contains an unsupported path character")
           {:label label :path value}))
  (let [path (Paths/get value (make-array String 0))
        segments (path-segments path)]
    (when (.isAbsolute path)
      (fail! (str label " must be relative to the artifact directory")
             {:label label :path value}))
    (when (or (empty? segments)
              (some #{"." ".."} segments))
      (fail! (str label " contains traversal or an empty path")
             {:label label :path value}))
    (str (.normalize path))))

(defn- confined-path!
  [^Path root relative label]
  (let [relative (canonical-relative! relative label)
        path (.normalize (.resolve root relative))]
    (when-not (.startsWith path root)
      (fail! (str label " escapes the artifact directory")
             {:label label :path relative :root (str root)}))
    (loop [cursor path]
      (when (and cursor (not= cursor root))
        (when (Files/isSymbolicLink cursor)
          (fail! (str label " resolves through a symbolic link")
                 {:label label :path relative}))
        (recur (.getParent cursor))))
    (when-not (Files/exists path no-follow-links)
      (fail! (str label " does not exist")
             {:label label :path relative}))
    (when-not (Files/isRegularFile path no-follow-links)
      (fail! (str label " is not a regular file")
             {:label label :path relative}))
    {:relative relative :path path}))

(defn- confined-directory!
  [^Path root relative label]
  (let [relative (canonical-relative! relative label)
        path (.normalize (.resolve root relative))]
    (when-not (.startsWith path root)
      (fail! (str label " escapes the artifact directory")
             {:label label :path relative :root (str root)}))
    (loop [cursor path]
      (when (and cursor (not= cursor root))
        (when (Files/isSymbolicLink cursor)
          (fail! (str label " resolves through a symbolic link")
                 {:label label :path relative}))
        (recur (.getParent cursor))))
    (when-not (Files/isDirectory path no-follow-links)
      (fail! (str label " is not a directory")
             {:label label :path relative}))
    {:relative relative :path path}))

(defn- read-file-data!
  [^Path root relative label]
  (let [{:keys [relative path]} (confined-path! root relative label)
        bytes (try
                (Files/readAllBytes path)
                (catch Throwable error
                  (fail! (str "Cannot read " label)
                         {:label label :path relative
                          :error-class (.getName (class error))})))]
    {:path relative
     :bytes (alength bytes)
     :sha256 (sha256-bytes bytes)
     :raw bytes}))

(defn- read-text-data!
  [^Path root relative label]
  (let [data (read-file-data! root relative label)]
    (assoc data :text (strict-utf8 (:raw data) label))))

(defn- read-one-edn!
  [text label]
  (try
    (let [eof (Object.)
          readers {:readers {}
                   :default (fn [tag _value]
                              (fail! (str "Unsupported EDN tag in " label)
                                     {:label label :tag tag}))}]
      (binding [*read-eval* false]
        (with-open [reader (PushbackReader. (StringReader. text))]
          (let [value (edn/read (assoc readers :eof eof) reader)
                extra (when-not (identical? eof value)
                        (edn/read (assoc readers :eof eof) reader))]
            (when (identical? eof value)
              (fail! (str label " is empty") {:label label}))
            (when-not (identical? eof extra)
              (fail! (str label " contains more than one EDN form")
                     {:label label}))
            value))))
    (catch clojure.lang.ExceptionInfo error
      (throw error))
    (catch Throwable error
      (fail! (str "Malformed EDN in " label)
             {:label label :error-class (.getName (class error))}))))

(defn- require-map!
  [value label]
  (when-not (map? value)
    (fail! (str label " must be an EDN map")
           {:label label :type (some-> value class .getName)}))
  value)

(defn- require-keyword!
  [value label]
  (when-not (keyword? value)
    (fail! (str label " must be a keyword")
           {:label label :type (some-> value class .getName)}))
  value)

(defn- require-integer!
  [value label]
  (when-not (integer? value)
    (fail! (str label " must be an integer")
           {:label label :type (some-> value class .getName)}))
  value)

(defn- object-id!
  [value label]
  (when-not (and (string? value)
                 (re-matches #"(?:[0-9a-fA-F]{40}|[0-9a-fA-F]{64})" value))
    (fail! (str label " must be a 40- or 64-hex object identity")
           {:label label :type (some-> value class .getName)}))
  value)

(defn- positive-decimal!
  [value label]
  (let [text (when (or (string? value) (integer? value)) (str value))]
    (when-not (and text
                   (re-matches #"[0-9]+" text)
                   (pos? (.signum (java.math.BigInteger. text))))
      (fail! (str label " must be a positive decimal identity")
             {:label label :type (some-> value class .getName)}))
    text))

(defn- all-regular-files!
  [^Path root]
  (try
    (with-open [stream (Files/walk root (make-array FileVisitOption 0))]
      (->> (iterator-seq (.iterator stream))
           (map (fn [^Path path]
                  (when (Files/isSymbolicLink path)
                    (fail! "Artifact tree contains a symbolic link"
                           {:path (str (.relativize root path))}))
                  path))
           (filter #(Files/isRegularFile ^Path % no-follow-links))
           (mapv #(str (.relativize root ^Path %)))))
    (catch clojure.lang.ExceptionInfo error
      (throw error))
    (catch Throwable error
      (fail! "Cannot inspect artifact directory"
             {:root (str root) :error-class (.getName (class error))}))))

(def ^:private checksum-line
  #"^([0-9a-fA-F]{64})  (.+)$")

(defn- strip-leading-dot-slash
  [value]
  (if (str/starts-with? value "./")
    (subs value 2)
    value))

(defn- parse-checksum-entry!
  [line line-number]
  (let [[_ digest path] (re-matches checksum-line line)]
    (when-not (and digest path)
      (fail! "Malformed SHA256SUMS entry"
             {:line line-number}))
    {:path (canonical-relative! (strip-leading-dot-slash path)
                                "SHA256SUMS path")
     :sha256 (str/lower-case digest)}))

(defn- verify-checksums!
  [^Path root]
  (let [manifest (read-text-data! root "SHA256SUMS" "SHA256SUMS")
        lines (str/split-lines (:text manifest))
        _ (when (empty? lines)
            (fail! "SHA256SUMS is empty" {}))
        entries (mapv parse-checksum-entry! lines (range 1 (inc (count lines))))
        paths (mapv :path entries)
        duplicates (->> paths frequencies
                        (filter (fn [[_ count]] (> count 1)))
                        (mapv first))
        actual (vec (remove #(= "SHA256SUMS" %) (all-regular-files! root)))
        listed (set paths)
        actual-set (set actual)]
    (when (seq duplicates)
      (fail! "SHA256SUMS contains duplicate paths" {:paths duplicates}))
    (when (contains? listed "SHA256SUMS")
      (fail! "SHA256SUMS must not list itself" {}))
    (when-not (= listed actual-set)
      (fail! "SHA256SUMS does not exactly cover the regular artifact files"
             {:missing (vec (sort (set/difference actual-set listed)))
              :extra (vec (sort (set/difference listed actual-set)))}))
    (doseq [{:keys [path sha256]} entries]
      (let [data (read-file-data! root path (str "SHA256SUMS entry " path))]
        (when-not (= sha256 (:sha256 data))
          (fail! "SHA256SUMS digest does not match the artifact file"
                 {:path path :expected sha256 :actual (:sha256 data)}))))
    {:file {:path "SHA256SUMS"
            :bytes (:bytes manifest)
            :sha256 (:sha256 manifest)}
     :entries (count entries)
     :paths actual
     :by-path (into {} (map (juxt :path :sha256) entries))}))

(defn- artifact-reference!
  [value label]
  (nonempty-string! label value)
  (when (.isAbsolute (Paths/get value (make-array String 0)))
    (fail! (str label " must be a relative artifact path")
           {:label label :path value}))
  (let [value (strip-leading-dot-slash value)
        value (if (str/starts-with? value "target/ci/")
                (subs value (count "target/ci/"))
                value)]
    (canonical-relative! value label)))

(defn- file-identity
  [data]
  (select-keys data [:path :bytes :sha256]))

(defn- verify-file-against-checksum!
  [data checksums label]
  (when-not (= (:sha256 data) (get-in checksums [:by-path (:path data)]))
    (fail! (str label " differs from SHA256SUMS")
           {:path (:path data)
            :expected (get-in checksums [:by-path (:path data)])
            :actual (:sha256 data)}))
  data)

(defn- child-status
  [value]
  (let [status (:status value)]
    (cond
      (keyword? status) status
      (string? status) (keyword status)
      :else (fail! "Child receipt status must be a keyword or string"
                   {:field :status :type (some-> status class .getName)}))))

(defn- validate-child-receipt!
  [value label]
  (require-map! value label)
  (require-integer! (:exit-code value) (str label " :exit-code"))
  (object-id! (:commit value) (str label " :commit"))
  (positive-decimal! (:run-id value) (str label " :run-id"))
  (positive-decimal! (:run-attempt value) (str label " :run-attempt"))
  (require-keyword! (child-status value) (str label " :status"))
  value)

(defn- checkout-identity!
  [data]
  (let [lines (str/split-lines (:text data))]
    (when-not (= 2 (count lines))
      (fail! "checkout.txt must contain exactly commit and tree lines"
             {:line-count (count lines)}))
    (let [[commit tree] (map str/trim lines)]
      (when (or (str/blank? commit) (str/blank? tree))
        (fail! "checkout.txt contains a blank identity" {}))
      {:commit (object-id! commit "checkout.txt commit")
       :tree (object-id! tree "checkout.txt tree")})))

(defn- run-url!
  [data run-id]
  (let [lines (str/split-lines (:text data))
        url (some-> (first lines) str/trim)]
    (when-not (and (= 1 (count lines)) (not (str/blank? url)))
      (fail! "run-url.txt must contain exactly one non-empty URL" {}))
    (let [path (try
                 (.getPath (java.net.URI. url))
                 (catch Throwable error
                   (fail! "run-url.txt is not a valid URI"
                          {:error-class (.getName (class error))})))
          run-id-pattern (java.util.regex.Pattern/quote (str run-id))]
      (when-not (and (string? path)
                     (re-find (re-pattern (str "/actions/runs/"
                                               run-id-pattern "$"))
                              path))
        (fail! "run-url.txt path does not end in the child receipt run identity"
               {:run-id run-id})))
    url))

(defn- read-child-context!
  [^Path root checksums]
  (let [checkout (verify-file-against-checksum!
                  (read-text-data! root "checkout.txt" "checkout.txt")
                  checksums "checkout.txt")
        run-url-file (verify-file-against-checksum!
                      (read-text-data! root "run-url.txt" "run-url.txt")
                      checksums "run-url.txt")
        child (verify-file-against-checksum!
               (read-text-data! root "public-release-subset.edn"
                                "public-release-subset.edn")
               checksums "public-release-subset.edn")
        child-value (read-one-edn! (:text child) "public-release-subset.edn")
        _ (validate-child-receipt! child-value "public-release-subset.edn")
        checkout-value (checkout-identity! checkout)
        run-id (str (:run-id child-value))
        _ (when-not (= (:commit checkout-value) (:commit child-value))
            (fail! "Child receipt commit disagrees with checkout.txt"
                   {:checkout (:commit checkout-value)
                    :child (:commit child-value)}))
        run-url (run-url! run-url-file run-id)
        summary-path? (contains? (:by-path checksums) "public-release-summary.edn")
        summary (when summary-path?
                  (verify-file-against-checksum!
                   (read-file-data! root "public-release-summary.edn"
                                    "public-release-summary.edn")
                   checksums "public-release-summary.edn"))]
    {:child child-value
     :checkout checkout-value
     :run-url run-url
     :recorded {:commit (:commit checkout-value)
                :tree (:tree checkout-value)
                :run-id run-id
                :run-url run-url
                :attempt (:run-attempt child-value)}
     :files (cond-> {:checkout (file-identity checkout)
                     :run-url (file-identity run-url-file)
                     :child-receipt (file-identity child)}
              summary (assoc :summary (file-identity summary)))}))

(defn- marker-line?
  [line]
  (str/starts-with? (str/trim line)
                    "{:marker \"ci-public-release-subset\""))

(defn- read-markers!
  [^Path root checksums]
  (if-not (contains? (:by-path checksums) "public-release-subset.log")
    {:available? false :markers [] :file nil}
    (let [data (verify-file-against-checksum!
                (read-text-data! root "public-release-subset.log"
                                 "public-release-subset.log")
                checksums "public-release-subset.log")
          markers (->> (str/split-lines (:text data))
                       (filter marker-line?)
                       (map-indexed
                        (fn [index line]
                          (let [value (read-one-edn! line
                                                     (str "public-release-subset.log line "
                                                          (inc index)))]
                            (require-map! value "diagnostic marker")
                            (when-not (= "ci-public-release-subset" (:marker value))
                              (fail! "Unexpected public diagnostic marker"
                                     {:line (inc index)}))
                            (require-keyword! (:phase value) "diagnostic marker :phase")
                            (let [edge (require-keyword! (:edge value)
                                                         "diagnostic marker :edge")]
                              (when-not (contains? marker-edges edge)
                                (fail! "Unsupported public diagnostic marker edge"
                                       {:line (inc index) :edge edge})))
                            value)))
                       vec)]
      {:available? true :markers markers :file (file-identity data)})))

(defn- record-file-name
  [relative]
  (when-let [[_ phase ordinal]
             (re-matches #"p18-public-test-artifacts/run-[^/]+/(accepted-fixture|rejected-fixture)-([0-9]+)-[^/]+\.edn"
                         relative)]
    {:phase (keyword phase)
     :ordinal (bigint ordinal)}))

(defn- manifest-file-name
  [relative]
  (when-let [[_ status]
             (re-matches #"p18-public-test-artifacts/run-[^/]+/(partial-manifest|manifest)-[^/]+\.edn"
                         relative)]
    {:status (if (= "partial-manifest" status) :partial :final)}))

(defn- nested-output-identity
  [value record-path field-key label]
  (when-not (map? value)
    (fail! (str label " must be a map")
           {:label label :type (some-> value class .getName)}))
  (require-integer! (:exit value) (str label " :exit"))
  (when-not (string? (:out value))
    (fail! (str label " :out must be a string") {:label label}))
  (when-not (string? (:err value))
    (fail! (str label " :err must be a string") {:label label}))
  {:exit (:exit value)
   :out {:record-path record-path
         :field-path [:record field-key :out]
         :utf8-bytes (alength (.getBytes ^String (:out value) StandardCharsets/UTF_8))
         :sha256 (sha256-bytes (.getBytes ^String (:out value) StandardCharsets/UTF_8))}
   :err {:record-path record-path
         :field-path [:record field-key :err]
         :utf8-bytes (alength (.getBytes ^String (:err value) StandardCharsets/UTF_8))
         :sha256 (sha256-bytes (.getBytes ^String (:err value) StandardCharsets/UTF_8))}})

(defn- record-output
  [phase record record-path label]
  (let [result-keys (case phase
                      :accepted-fixture accepted-result-keys
                      :rejected-fixture rejected-result-keys
                      (fail! "Unsupported diagnostic record phase"
                             {:phase phase :path record-path}))]
    (into {}
          (map (fn [key]
                 (let [missing (Object.)
                       value (get record key missing)]
                   (when (identical? missing value)
                     (fail! "Diagnostic record is missing a required result"
                            {:path record-path :phase phase :field key}))
                   [key (nested-output-identity value record-path key
                                                (str label " nested " key))]))
               result-keys))))

(defn- read-record!
  [^Path root checksums relative expected-name]
  (let [data (verify-file-against-checksum!
              (read-file-data! root relative (str "record " relative))
              checksums
              (str "record " relative))
        text (strict-utf8 (:raw data) (str "record " relative))
        envelope (read-one-edn! text (str "record " relative))
        _ (require-map! envelope (str "record " relative))
        _ (when-not (= producer-schema (:schema envelope))
            (fail! "Unsupported diagnostic record schema"
                   {:path relative :schema (:schema envelope)}))
        _ (when-not (= :diagnostic (:authority envelope))
            (fail! "Diagnostic record authority is not :diagnostic"
                   {:path relative :authority (:authority envelope)}))
        _ (when-not (= false (:authoritative? envelope))
            (fail! "Diagnostic record must be non-authoritative"
                   {:path relative :authoritative? (:authoritative? envelope)}))
        phase (require-keyword! (:phase envelope) (str "record " relative " :phase"))
        ordinal (require-integer! (:ordinal envelope) (str "record " relative " :ordinal"))
        _ (when-not (pos? ordinal)
            (fail! "Diagnostic record ordinal must be positive" {:path relative :ordinal ordinal}))
        expected-phase (:phase expected-name)
        expected-ordinal (:ordinal expected-name)
        _ (when-not (= phase expected-phase)
            (fail! "Diagnostic record phase disagrees with its filename"
                   {:path relative :filename-phase expected-phase :record-phase phase}))
        _ (when-not (= ordinal expected-ordinal)
            (fail! "Diagnostic record ordinal disagrees with its filename"
                   {:path relative :filename-ordinal expected-ordinal :record-ordinal ordinal}))
        record (require-map! (:record envelope) (str "record " relative " :record"))
        fixture (nonempty-string! (str "record " relative " :fixture") (:fixture record))]
    {:path relative
     :file-identity (file-identity data)
     :phase phase
     :ordinal ordinal
     :fixture fixture
     :record-keys (vec (sort-by str (keys record)))
     :results (record-output phase record relative (str "record " relative))}))

(defn- manifest-record-entry!
  [entry label]
  (require-map! entry label)
  (let [path (artifact-reference! (:path entry) (str label " :path"))]
    {:path path
     :phase (require-keyword! (:phase entry) (str label " :phase"))
     :ordinal (require-integer! (:ordinal entry) (str label " :ordinal"))
     :bytes (require-integer! (:bytes entry) (str label " :bytes"))
     :sha256 (nonempty-string! (str label " :sha256") (:sha256 entry))}))

(defn- read-manifest!
  [^Path root checksums relative]
  (let [data (verify-file-against-checksum!
              (read-file-data! root relative (str "manifest " relative))
              checksums
              (str "manifest " relative))
        text (strict-utf8 (:raw data) (str "manifest " relative))
        envelope (read-one-edn! text (str "manifest " relative))
        _ (require-map! envelope (str "manifest " relative))
        _ (when-not (= producer-schema (:schema envelope))
            (fail! "Unsupported diagnostic manifest schema"
                   {:path relative :schema (:schema envelope)}))
        _ (when-not (= :diagnostic (:authority envelope))
            (fail! "Diagnostic manifest authority is not :diagnostic"
                   {:path relative :authority (:authority envelope)}))
        _ (when-not (= false (:authoritative? envelope))
            (fail! "Diagnostic manifest must be non-authoritative"
                   {:path relative :authoritative? (:authoritative? envelope)}))
        status (require-keyword! (:status envelope) (str "manifest " relative " :status"))
        expected-status (:status (manifest-file-name relative))
        _ (when-not (= status expected-status)
            (fail! "Diagnostic manifest status disagrees with its filename"
                   {:path relative :filename-status expected-status :status status}))
        directory (nonempty-string! (str "manifest " relative " :directory") (:directory envelope))
        records (:records envelope)
        _ (when-not (vector? records)
            (fail! "Diagnostic manifest records must be a vector"
                   {:path relative :type (some-> records class .getName)}))
        entries (mapv manifest-record-entry!
                      records
                      (map #(str "manifest " relative " record " %) (range (count records))))
        duplicate-ordinals (->> (map :ordinal entries) frequencies
                                (filter (fn [[_ count]] (> count 1)))
                                (mapv first))
        ordered-entries (vec (sort-by :ordinal entries))]
    (when (seq duplicate-ordinals)
      (fail! "Diagnostic manifest contains duplicate record ordinals"
             {:path relative :ordinals duplicate-ordinals}))
    (when-not (= entries ordered-entries)
      (fail! "Diagnostic manifest records are not in ordinal order"
             {:path relative :ordinals (mapv :ordinal entries)}))
    (confined-directory! root (artifact-reference! directory
                                                   (str "manifest " relative " :directory"))
                         (str "manifest " relative " :directory"))
    {:path relative
     :file relative
     :file-identity (file-identity data)
     :status status
     :records ordered-entries
     :record-count (count entries)}))

(defn- verify-manifest-records!
  [manifest records]
  (let [entries (:records manifest)
        ordinals (mapv :ordinal entries)
        expected-ordinals (vec (range 1 (inc (count entries))))
        record-by-ordinal (into {} (map (juxt :ordinal identity) records))]
    (when-not (= expected-ordinals ordinals)
      (fail! "Diagnostic manifest is not a contiguous returned prefix"
             {:path (:path manifest) :ordinals ordinals}))
    (doseq [entry entries]
      (let [record (get record-by-ordinal (:ordinal entry))]
        (when-not record
          (fail! "Diagnostic manifest references a missing record"
                 {:manifest (:path manifest) :ordinal (:ordinal entry)}))
        (when-not (= (:path entry) (:path (:file-identity record)))
          (fail! "Diagnostic manifest record path disagrees with record identity"
                 {:manifest (:path manifest) :entry-path (:path entry)
                  :record-identity (:file-identity record)}))
        (when-not (= (:phase entry) (:phase record))
          (fail! "Diagnostic manifest record phase disagrees with record"
                 {:manifest (:path manifest) :entry-path (:path entry)
                  :entry-phase (:phase entry)
                  :record-identity (select-keys record [:path :phase :ordinal])}))
        (when-not (= (:ordinal entry) (:ordinal record))
          (fail! "Diagnostic manifest record ordinal disagrees with record"
                 {:manifest (:path manifest) :entry-path (:path entry)
                  :entry-ordinal (:ordinal entry)
                  :record-identity (select-keys record [:path :phase :ordinal])}))
        (when-not (= (:bytes entry) (get-in record [:file-identity :bytes]))
          (fail! "Diagnostic manifest record byte count disagrees with record"
                 {:manifest (:path manifest) :entry-path (:path entry)
                  :entry-bytes (:bytes entry)
                  :record-identity (:file-identity record)}))
        (when-not (= (:sha256 entry) (get-in record [:file-identity :sha256]))
          (fail! "Diagnostic manifest record digest disagrees with record"
                 {:manifest (:path manifest) :entry-path (:path entry)
                  :entry-sha256 (:sha256 entry)
                  :record-identity (:file-identity record)}))))))

(defn- validate-records-and-manifests!
  [^Path root checksums]
  (let [paths (:paths checksums)
        manifest-paths (->> paths (keep #(when (manifest-file-name %) %)) sort vec)
        raw-manifests (mapv #(read-manifest! root checksums %) manifest-paths)
        referenced-record-paths (->> raw-manifests
                                     (mapcat #(map :path (:records %)))
                                     set)
        filename-record-paths (->> paths (keep #(when (record-file-name %) %)) set)
        record-paths (vec (sort (set/union filename-record-paths referenced-record-paths)))
        records (mapv (fn [relative]
                        (read-record! root checksums relative
                                      (or (record-file-name relative)
                                          (fail! "Record path has no supported filename"
                                                 {:path relative}))))
                      record-paths)
        duplicate-ordinals (->> (map :ordinal records) frequencies
                                (filter (fn [[_ count]] (> count 1)))
                                (mapv first))]
    (when (seq duplicate-ordinals)
      (fail! "Diagnostic records contain duplicate ordinals"
             {:ordinals duplicate-ordinals}))
    (doseq [manifest raw-manifests]
      (verify-manifest-records! manifest records))
    (let [sorted-manifests (vec (sort-by (juxt :record-count
                                               #(if (= :final (:status %)) 1 0)
                                               :path)
                                         raw-manifests))
          latest (last sorted-manifests)
          index-status (if latest (:status latest) :not-observed)]
      {:records (vec (sort-by :ordinal records))
       :manifests (mapv #(select-keys % [:path :file-identity :status :record-count])
                        sorted-manifests)
       :latest-indexed (if latest (:record-count latest) :not-observed)
       :index-status index-status
       :partial-count (count (filter #(= :partial (:status %)) raw-manifests))
       :final-count (count (filter #(= :final (:status %)) raw-manifests))})))

(defn- marker-record-key
  [marker]
  [(:phase marker) (:ordinal marker)])

(defn- verify-capture-markers!
  [markers records]
  (let [capture-markers (filter #(= :diagnostic-captured (:edge %)) markers)
        by-key (into {} (map (juxt marker-record-key identity) capture-markers))
        record-keys (set (map marker-record-key records))
        marker-keys (set (keys by-key))]
    (when-not (= (count capture-markers) (count by-key))
      (fail! "Duplicate diagnostic-captured marker" {}))
    (when (seq (set/difference marker-keys record-keys))
      (fail! "Capture marker references a missing record"
             {:markers (vec (set/difference marker-keys record-keys))}))
    (doseq [record records]
      (when-let [marker (get by-key [(:phase record) (:ordinal record)])]
        (let [path (artifact-reference! (:path marker) "capture marker :path")
              identity (:file-identity record)]
          (when-not (and (= path (:path identity))
                         (= (:bytes marker) (:bytes identity))
                         (= (:sha256 marker) (:sha256 identity)))
            (fail! "Capture marker file identity disagrees with record"
                   {:record identity
                    :marker-path path
                    :marker-bytes (:bytes marker)
                    :marker-sha256 (:sha256 marker)}))
          (when-not (= (:phase record) (:phase marker))
            (fail! "Capture marker phase disagrees with record"
                   {:path (:path identity)
                    :marker-phase (:phase marker)}))
          (when-not (= (:ordinal record) (:ordinal marker))
            (fail! "Capture marker ordinal disagrees with record"
                   {:path (:path identity)
                    :marker-ordinal (:ordinal marker)})))))
    {:count (count capture-markers)
     :available? (boolean (seq markers))}))

(defn- verify-manifest-markers!
  [markers manifests]
  (let [manifest-markers (filter #(= :manifest-written (:edge %)) markers)
        by-path (into {} (map (fn [marker]
                                [(artifact-reference! (:path marker)
                                                      "manifest marker :path")
                                 marker])
                              manifest-markers))
        manifest-paths (set (map :path manifests))]
    (when-not (= (count manifest-markers) (count by-path))
      (fail! "Duplicate manifest-written marker" {}))
    (when (seq (set/difference (set (keys by-path)) manifest-paths))
      (fail! "Manifest marker references a missing manifest"
             {:markers (vec (set/difference (set (keys by-path)) manifest-paths))}))
    (doseq [manifest manifests]
      (when-let [marker (get by-path (:path manifest))]
        (let [identity (:file-identity manifest)]
          (when-not (and (= (:bytes marker) (:bytes identity))
                         (= (:sha256 marker) (:sha256 identity))
                         (= (:status marker) (:status manifest)))
            (fail! "Manifest marker file identity disagrees with manifest"
                   {:manifest-path (:path manifest)
                    :marker-path (:path marker)
                    :marker-status (:status marker)
                    :marker-bytes (:bytes marker)
                    :marker-sha256 (:sha256 marker)})))))
    {:count (count manifest-markers)
     :available? (boolean (seq markers))}))

(defn- elapsed-ms
  [receipt]
  (let [started (:started-ms receipt)
        finished (:finished-ms receipt)]
    (when (and (number? started) (number? finished))
      (- finished started))))

(defn- assessment
  [receipt]
  {:status (child-status receipt)
   :exit-code (:exit-code receipt)
   :timeout-seconds (:timeout-seconds receipt)
   :elapsed-ms (elapsed-ms receipt)})

(defn report
  "Read one extracted public-release artifact directory without mutation.

  The returned map is diagnostic only. It preserves recorded assessment failure
  and timeout state and never treats an index as release or completion proof."
  [artifact-dir]
  (let [root (root-path! artifact-dir)
        checksums (verify-checksums! root)
        context (read-child-context! root checksums)
        marker-data (read-markers! root checksums)
        index-data (validate-records-and-manifests! root checksums)
        marker-values (:markers marker-data)
        capture-markers (verify-capture-markers! marker-values (:records index-data))
        manifest-markers (verify-manifest-markers! marker-values (:manifests index-data))
        records (mapv (fn [record]
                        (assoc (select-keys record [:phase :fixture :ordinal
                                                    :record-keys :results])
                               :file (:file-identity record)))
                      (:records index-data))
        observed {:entered (if (:available? marker-data)
                             (count (filter #(and (= :enter (:edge %))
                                                  (#{:accepted-fixture :rejected-fixture}
                                                   (:phase %)))
                                            marker-values))
                             :not-observed)
                  :return-markers (if (:available? marker-data)
                                    (count (filter #(and (= :return (:edge %))
                                                         (#{:accepted-fixture :rejected-fixture}
                                                          (:phase %)))
                                                   marker-values))
                                    :not-observed)
                  :captured (count records)
                  :indexed (:latest-indexed index-data)
                  :partial-manifests (:partial-count index-data)
                  :final-manifests (:final-count index-data)}]
    {:schema diagnostic-schema
     :authority :diagnostic
     :authoritative? false
     :recorded (:recorded context)
     :assessment (assessment (:child context))
     :observed observed
     :index-status (:index-status index-data)
     :records records
     :manifests (mapv (fn [manifest]
                        {:path (:path manifest)
                         :status (:status manifest)
                         :record-count (:record-count manifest)
                         :file (:file-identity manifest)})
                      (:manifests index-data))
     :inputs {:checksums (select-keys checksums [:file :entries])
              :files (cond-> (:files context)
                       (:file marker-data)
                       (assoc :marker-log (:file marker-data)))
              :identity {:basis :recorded-artifact-files
                         :provider-authenticated? false
                         :git-source-authenticated? false
                         :capture-markers (if (:available? marker-data)
                                            (:count capture-markers)
                                            :not-observed)
                         :manifest-markers (if (:available? marker-data)
                                             (:count manifest-markers)
                                             :not-observed)}}
     :nonclaims [:recorded-identities-not-provider-authenticated
                 :recorded-identities-not-git-source-authenticated
                 :records-remain-in-original-files
                 :no-inline-command-text
                 :no-redaction-or-secret-preservation-claim
                 :partial-index-is-not-assessment-completion
                 :final-index-is-not-assessment-completion
                 :diagnostic-not-release-or-admission-authority
                 :no-replay-provider-call-or-public-test-execution
                 :hard-kill-may-leave-unfinished-files
                 :no-fsync-or-crash-proof-claim]}))

(defn- validate-options!
  [options]
  (when-not (map? options)
    (fail! "Diagnostic report options must be a map"
           {:type (some-> options class .getName)}))
  (when-not (= #{:artifact-dir} (set (keys options)))
    (fail! "Diagnostic report accepts exactly :artifact-dir"
           {:keys (vec (sort-by str (keys options)))}))
  (nonempty-string! ":artifact-dir" (:artifact-dir options))
  options)

(defn run!
  "Print one untruncated EDN report for exactly one artifact directory option."
  [options]
  (validate-options! options)
  (let [value (report (:artifact-dir options))]
    (binding [*print-length* nil
              *print-level* nil
              *print-readably* true]
      (println (pr-str value)))
    0))
