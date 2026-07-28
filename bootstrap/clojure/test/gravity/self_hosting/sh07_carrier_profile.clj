(ns gravity.self-hosting.sh07-carrier-profile
  "Read-only, bounded structural profiler for large bootstrap carrier values.

  The profiler does not construct SH-07 artifacts, invoke lowering, rewrite
  values, or persist evidence. Its digest-reference savings are hypothetical
  size estimates; authenticated-envelope integration remains coordinator-owned."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import [java.io PushbackReader]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files]
           [java.security MessageDigest]
           [java.util IdentityHashMap]))

(def default-options
  {:maximum-nodes 67108864
   :maximum-depth 512
   :maximum-container-width 65536
   :maximum-input-bytes 1073741824
   :minimum-indexed-subtree-nodes 128
   :maximum-distinct-subtree-fingerprints 250000
   :largest-subtree-count 24
   :hypothetical-digest-reference-nodes 2})

(def ^:private option-keys
  (set (keys default-options)))

(defn- fail!
  [id message facts]
  (throw
   (ex-info message
            (assoc facts
                   :id id
                   :slice "SH-07"
                   :profiler :gravity/sh07-carrier-profile-v1))))

(defn- aggregate-kind
  [value]
  (cond
    (or (map? value) (instance? java.util.Map value)) :map
    (or (set? value) (instance? java.util.Set value)) :set
    (vector? value) :vector
    (list? value) :list
    (or (sequential? value)
        (instance? java.util.List value)
        (instance? java.util.Collection value)) :sequence
    :else nil))

(defn- canonical-scalar
  [value]
  (when (aggregate-kind value)
    (fail! "SH07-CARRIER-PROFILE-KEY"
           "Carrier profiler map keys must be scalar values"
           {:key-class (.getName (class value))
            :reason :aggregate-key}))
  (when-not
      (or (nil? value)
          (boolean? value)
          (string? value)
          (char? value)
          (keyword? value)
          (symbol? value)
          (integer? value)
          (ratio? value)
          (decimal? value)
          (float? value)
          (instance? java.util.Date value)
          (instance? java.util.UUID value))
    (fail! "SH07-CARRIER-PROFILE-KEY"
           "Carrier profiler map keys must use the canonical EDN scalar domain"
           {:key-class (.getName (class value))
            :reason :unsupported-scalar-key}))
  (let [serialized (pr-str value)
        class-name (if (nil? value) "nil" (.getName (class value)))]
    {:class-name class-name
     :serialized serialized
     :sort-token [class-name serialized]}))

(defn- scalar-label
  [value]
  (:serialized (canonical-scalar value)))

(defn- display-label
  [value]
  (let [label (scalar-label value)]
    (if (< 96 (count label))
      (str (subs label 0 93) "...")
      label)))

(defn- fail-width!
  [path observed maximum]
  (fail! "SH07-CARRIER-PROFILE-WIDTH"
         "Carrier profiler container width exceeds its configured bound"
         {:path path
          :observed observed
          :maximum maximum}))

(defn- bounded-members
  [value maximum path]
  (loop [remaining (seq value)
         width 0
         members (transient [])]
    (cond
      (nil? remaining)
      (persistent! members)

      (= width maximum)
      (fail-width! path (inc width) maximum)

      :else
      (recur (next remaining)
             (inc width)
             (conj! members (first remaining))))))

(defn- map-entries
  [value maximum path]
  (let [entries (bounded-members value maximum path)
        canonicalized
        (mapv
         (fn [[key child :as entry]]
           {:entry entry
            :key key
            :child child
            :canonical (canonical-scalar key)})
         entries)
        collisions
        (->> canonicalized
             (group-by (comp :sort-token :canonical))
             vals
             (filter #(< 1 (count %)))
             first)]
    (when collisions
      (fail! "SH07-CARRIER-PROFILE-KEY"
             "Carrier profiler map keys have colliding canonical encodings"
             {:reason :canonical-key-collision
              :key-classes
              (mapv #(get-in % [:canonical :class-name]) collisions)
              :key-serialization
              (get-in (first collisions) [:canonical :serialized])}))
    (sort-by (comp :sort-token :canonical) canonicalized)))

(defn- aggregate-children
  [kind value maximum path]
  (case kind
    :map
    (let [entries (map-entries value maximum path)]
      {:width (count entries)
       :children
       (vec
        (mapcat
         (fn [{:keys [key child]}]
           [{:edge [:map-key (display-label key)]
             :value key
             :map-key? true}
            {:edge [:map-value (display-label key)]
             :value child
             :map-value? true
             :section key}])
         entries))})

    :set
    (let [members (bounded-members value maximum path)]
      {:width (count members)
       :children
       (mapv
        (fn [child]
          {:edge [:set-member] :value child})
        members)})

    (let [members (bounded-members value maximum path)]
      {:width (count members)
       :children
       (mapv
        (fn [index child]
          {:edge [kind index] :value child})
        (range)
        members)})))

(defn- sha256-parts
  [parts]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (doseq [part parts]
      (.update digest (.getBytes (str part) StandardCharsets/UTF_8))
      (.update digest (byte-array [(byte 0)])))
    (str
     "sha256:"
     (apply
      str
      (map
       #(format "%02x" (bit-and 0xff %))
       (.digest digest))))))

(defn- scalar-profile
  [value]
  (let [serialized (pr-str value)
        bytes (.getBytes serialized StandardCharsets/UTF_8)
        class-name (if (nil? value) "nil" (.getName (class value)))]
    {:fingerprint
     (sha256-parts ["scalar" class-name serialized])
     :nodes 1
     :aggregate-nodes 0
     :scalar-serialization-bytes (alength bytes)
     :maximum-depth 0
     :maximum-width 0
     :kind [:scalar class-name]}))

(defn- update-kind-count
  [counts kind]
  (swap! counts update kind (fnil inc 0)))

(defn- canonical-key-compare
  [left right]
  (compare (pr-str left) (pr-str right)))

(defn- add-largest-subtree!
  [largest maximum candidate]
  (when (pos? maximum)
    (swap!
     largest
     (fn [entries]
       (->> (conj entries candidate)
            (sort-by
             (juxt
              (comp - :nodes)
              :fingerprint
              (comp pr-str :path)))
            (take maximum)
            vec)))))

(defn- index-subtree!
  [index truncated? eligible-count omitted-count options result path]
  (when (<= (:minimum-indexed-subtree-nodes options) (:nodes result))
    (swap! eligible-count inc)
    (let [fingerprint (:fingerprint result)
          existing (get @index fingerprint)]
      (cond
        existing
        (swap!
         index update fingerprint
         (fn [record]
           (-> record
               (update :occurrences inc)
               (update :paths
                       (fn [paths]
                         (if (< (count paths) 4)
                           (conj paths path)
                           paths))))))

        (< (count @index)
           (:maximum-distinct-subtree-fingerprints options))
        (swap!
         index assoc fingerprint
         {:fingerprint fingerprint
          :nodes-per-copy (:nodes result)
          :aggregate-nodes-per-copy (:aggregate-nodes result)
          :scalar-serialization-bytes-per-copy
          (:scalar-serialization-bytes result)
          :occurrences 1
          :paths [path]})

        :else
        (do
          (swap! omitted-count inc)
          (reset! truncated? true))))))

(defn- normalize-options
  [options]
  (when-not (map? options)
    (fail! "SH07-CARRIER-PROFILE-OPTIONS"
           "Carrier profiler options must be a map"
           {:value options}))
  (when-let [unknown
             (first
              (sort-by pr-str
                       (remove option-keys (keys options))))]
    (fail! "SH07-CARRIER-PROFILE-OPTIONS"
           "Carrier profiler option is not recognized"
           {:option unknown
            :value (get options unknown)
            :reason :unknown-option}))
  (let [options (merge default-options options)]
    (doseq [key [:maximum-nodes
                 :maximum-depth
                 :maximum-container-width
                 :maximum-input-bytes
                 :minimum-indexed-subtree-nodes
                 :maximum-distinct-subtree-fingerprints
                 :largest-subtree-count
                 :hypothetical-digest-reference-nodes]]
      (when-not (and (integer? (get options key))
                     (not (neg? (get options key))))
        (fail! "SH07-CARRIER-PROFILE-OPTIONS"
               "Carrier profiler bounds must be nonnegative integers"
               {:option key :value (get options key)})))
    options))

(defn profile-value
  "Profile an in-memory carrier value without changing it.

  Structurally identical aggregate subtrees are indexed by canonical SHA-256.
  Indexing is bounded; `:duplicate-index-truncated?` reports whether new
  fingerprints were omitted after reaching that bound."
  ([value]
   (profile-value value {}))
  ([value supplied-options]
   (let [options (normalize-options supplied-options)
         active (IdentityHashMap.)
         kinds (atom (sorted-map-by canonical-key-compare))
         duplicate-index (atom {})
         duplicate-index-truncated? (atom false)
         eligible-indexed-subtrees (atom 0)
         omitted-indexed-subtrees (atom 0)
         largest (atom [])
         visited-nodes (atom 0)
         started (System/nanoTime)]
     (letfn
         [(walk [current path depth]
            (let [observed (swap! visited-nodes inc)]
              (when (< (:maximum-nodes options) observed)
                (fail! "SH07-CARRIER-PROFILE-NODES"
                       "Carrier profiler node count exceeds its configured bound"
                       {:path path
                        :observed observed
                        :maximum (:maximum-nodes options)})))
            (when (< (:maximum-depth options) depth)
              (fail! "SH07-CARRIER-PROFILE-DEPTH"
                     "Carrier profiler depth exceeds its configured bound"
                     {:path path
                      :observed depth
                      :maximum (:maximum-depth options)}))
            (if-let [kind (aggregate-kind current)]
              (do
                (when (.containsKey active current)
                  (fail! "SH07-CARRIER-PROFILE-CYCLE"
                         "Carrier profiler encountered an identity cycle"
                         {:path path
                          :first-path (.get active current)}))
                (.put active current path)
                (try
                  (let [{:keys [children width]}
                        (aggregate-children
                         kind current
                         (:maximum-container-width options)
                         path)
                        child-results
                        (mapv
                         (fn [{:keys [edge value] :as child}]
                           (assoc
                            child
                            :result
                            (walk value (conj path edge) (inc depth))))
                         children)
                        fingerprints
                        (case kind
                          :map
                          (->> child-results
                               (partition 2)
                               (map
                               (fn [[key-child value-child]]
                                  ["entry"
                                   (get-in key-child
                                           [:result :fingerprint])
                                   (get-in value-child
                                           [:result :fingerprint])]))
                               (mapcat identity)
                               vec)

                          :set
                          (->> child-results
                               (map #(get-in % [:result :fingerprint]))
                               sort
                               vec)

                          (mapv
                           #(get-in % [:result :fingerprint])
                           child-results))
                        result
                        {:fingerprint
                         (sha256-parts
                          (into ["aggregate" (name kind)] fingerprints))
                         :nodes
                         (inc (reduce + 0 (map #(get-in % [:result :nodes])
                                               child-results)))
                         :aggregate-nodes
                         (inc
                          (reduce
                           +
                           0
                           (map #(get-in % [:result :aggregate-nodes])
                                child-results)))
                         :scalar-serialization-bytes
                         (reduce
                          +
                          0
                          (map
                           #(get-in
                             %
                             [:result :scalar-serialization-bytes])
                           child-results))
                         :maximum-depth
                         (reduce
                          max depth
                          (map #(get-in % [:result :maximum-depth])
                               child-results))
                         :maximum-width
                         (reduce
                          max width
                          (map #(get-in % [:result :maximum-width])
                               child-results))
                         :kind kind
                         :root-children
                         (when (zero? depth) child-results)}]
                    (update-kind-count kinds kind)
                    (index-subtree!
                     duplicate-index duplicate-index-truncated?
                     eligible-indexed-subtrees omitted-indexed-subtrees
                     options result path)
                    (when
                        (<= (:minimum-indexed-subtree-nodes options)
                            (:nodes result))
                      (add-largest-subtree!
                       largest
                       (:largest-subtree-count options)
                       (select-keys
                        (assoc result :path path)
                        [:path :kind :nodes :aggregate-nodes
                         :scalar-serialization-bytes :fingerprint])))
                    result)
                  (finally
                    (.remove active current))))
              (let [result (assoc (scalar-profile current)
                                  :maximum-depth depth)]
                (update-kind-count kinds (:kind result))
                result)))]
       (let [root (walk value [] 0)
             reference-nodes
             (:hypothetical-digest-reference-nodes options)
             duplicates
             (->> @duplicate-index
                  vals
                  (filter #(< 1 (:occurrences %)))
                  (map
                   (fn [record]
                     (let [extra-copies (dec (:occurrences record))
                           saved-per-copy
                           (max
                            0
                            (- (:nodes-per-copy record)
                               reference-nodes))]
                       (assoc
                        record
                        :hypothetical-reference-nodes reference-nodes
                        :hypothetical-node-savings
                        (* extra-copies saved-per-copy)))))
                  (sort-by
                   (juxt
                    (comp - :hypothetical-node-savings)
                    (comp - :nodes-per-copy)
                    :fingerprint))
                  vec)
             sections
             (->> (:root-children root)
                  (keep
                   (fn [{:keys [map-value? section result]}]
                     (when map-value?
                       {:section section
                        :nodes (:nodes result)
                        :aggregate-nodes (:aggregate-nodes result)
                        :scalar-serialization-bytes
                        (:scalar-serialization-bytes result)
                        :fingerprint (:fingerprint result)})))
                  (sort-by (comp pr-str :section))
                  vec)]
         {:schema :gravity/sh07-carrier-profile-v1
          :status :profiled
          :read-only? true
          :root-fingerprint (:fingerprint root)
          :measurements
          (select-keys
           root
           [:nodes :aggregate-nodes :scalar-serialization-bytes
            :maximum-depth :maximum-width])
          :node-kind-counts @kinds
          :top-level-sections sections
          :largest-subtrees @largest
          :repeated-subtrees duplicates
          :duplicate-index
          (let [indexed-occurrences
                (reduce + 0 (map :occurrences (vals @duplicate-index)))]
            {:minimum-subtree-nodes
             (:minimum-indexed-subtree-nodes options)
             :eligible-subtree-occurrences @eligible-indexed-subtrees
             :indexed-subtree-occurrences indexed-occurrences
             :omitted-subtree-occurrences @omitted-indexed-subtrees
             :indexed-fingerprints (count @duplicate-index)
             :maximum-distinct-fingerprints
             (:maximum-distinct-subtree-fingerprints options)
             :complete? (not @duplicate-index-truncated?)
             :truncated? @duplicate-index-truncated?})
          :hypothetical-reference-estimate
          {:reference-nodes reference-nodes
           :candidate-groups (count duplicates)
           :overlapping-node-savings-upper-bound
           (reduce + 0 (map :hypothetical-node-savings duplicates))
           :non-overlapping-plan-computed? false
           :semantic-rewrite-authorized? false}
          :elapsed-ms
          (long (/ (- (System/nanoTime) started) 1000000))
          :claims
          {:measurement-only? true
           :artifact-authority? false
           :authenticated-reference-integration? false
           :sh07-complete? false}})))))

(defn profile-edn-file
  "Read and profile one bounded EDN carrier file."
  ([path]
   (profile-edn-file path {}))
  ([path supplied-options]
   (let [options (normalize-options supplied-options)
         file (.getCanonicalFile (io/file path))
         bytes (Files/size (.toPath file))]
     (when (< (:maximum-input-bytes options) bytes)
       (fail! "SH07-CARRIER-PROFILE-INPUT"
              "Carrier EDN exceeds the pre-parse byte bound"
              {:path (.getPath file)
               :observed bytes
               :maximum (:maximum-input-bytes options)}))
     (with-open [reader (PushbackReader. (io/reader file))]
       (let [value (edn/read {:eof ::eof} reader)
             trailing (edn/read {:eof ::eof} reader)]
         (when (= ::eof value)
           (fail! "SH07-CARRIER-PROFILE-INPUT"
                  "Carrier EDN input is empty"
                  {:path (.getPath file)}))
         (when-not (= ::eof trailing)
           (fail! "SH07-CARRIER-PROFILE-INPUT"
                  "Carrier EDN input contains multiple top-level values"
                  {:path (.getPath file)}))
         (assoc
          (profile-value value options)
          :input
          {:path (.getPath file)
           :serialized-bytes bytes}))))))

(defn -main
  [& arguments]
  (when-not (and (= 2 (count arguments))
                 (= "--edn" (first arguments)))
    (fail! "SH07-CARRIER-PROFILE-USAGE"
           "Use --edn <carrier.edn>"
           {:arguments (vec arguments)}))
  (println (pr-str (profile-edn-file (second arguments)))))
