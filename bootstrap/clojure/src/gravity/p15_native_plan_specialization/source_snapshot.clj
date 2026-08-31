(ns gravity.p15-native-plan-specialization.source-snapshot
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path Paths]
           [java.nio.file.attribute BasicFileAttributes]))

(defn repository-root
  [helper-contract-fail!]
  (let [resource (io/resource "gravity/p15_native_plan_specialization.clj")]
    (when-not resource
      (helper-contract-fail!
       nil
       "native plan specialization source is not on the classpath"
       {:missing-fact :specialization-source-resource}))
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (helper-contract-fail!
         nil
         "repository root is unavailable for the Gravity C emitter helper"
         {:missing-fact :repository-root})

        (Files/isRegularFile (.resolve ^Path candidate "deps.edn")
                             (make-array LinkOption 0))
        candidate

        :else
        (recur (.getParent ^Path candidate))))))

(defn helper-source-path!
  [request-source repository-root helper-source-relative no-follow-options
   helper-contract-fail!]
  (let [repository-root (.normalize (.toAbsolutePath ^Path repository-root))
        relative-path (Paths/get helper-source-relative (make-array String 0))
        source-path (.normalize (.resolve repository-root relative-path))]
    (when-not (.startsWith source-path repository-root)
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper escaped the repository root"
       {:missing-fact :bounded-helper-source-location
        :source-path (str source-path)}))
    (loop [current repository-root
           components (seq (iterator-seq
                            (.iterator
                             (.relativize repository-root source-path))))]
      (let [attributes
            (try
              (Files/readAttributes current BasicFileAttributes
                                    no-follow-options)
              (catch java.io.IOException error
                (helper-contract-fail!
                 request-source
                 "Gravity C emitter helper path is unreadable"
                 {:missing-fact :bounded-helper-source-location
                  :source-path (str source-path)
                  :observed-component (str current)
                  :cause-message (.getMessage error)})))]
        (when (or (.isSymbolicLink attributes)
                  (and (seq components) (not (.isDirectory attributes)))
                  (and (nil? components) (not (.isRegularFile attributes))))
          (helper-contract-fail!
           request-source
           "Gravity C emitter helper path is not a regular non-symlink file"
           {:missing-fact :bounded-helper-source-location
            :source-path (str source-path)
            :observed-component (str current)
            :symbolic-link? (.isSymbolicLink attributes)
            :directory? (.isDirectory attributes)
            :regular-file? (.isRegularFile attributes)}))
        (if-let [component (first components)]
          (recur (.resolve ^Path current ^Path component) (next components))
          {:source-path source-path
           :attributes attributes})))))

(defn strict-utf8
  [request-source source-path bytes helper-contract-fail!]
  (try
    (let [decoder
          (doto (.newDecoder StandardCharsets/UTF_8)
            (.onMalformedInput CodingErrorAction/REPORT)
            (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes))))
    (catch java.nio.charset.CharacterCodingException error
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper is not strict UTF-8"
       {:missing-fact :strict-utf8-helper-source
        :source-path (str source-path)
        :cause-message (.getMessage error)}))))

(defn helper-source-snapshot!
  [request-source
   {:keys [helper-source-path! strict-utf8 max-helper-source-bytes
           no-follow-options helper-contract-fail!]}]
  (let [{:keys [source-path attributes]} (helper-source-path! request-source)
        before-size (.size ^BasicFileAttributes attributes)]
    (when (> before-size max-helper-source-bytes)
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper source exceeds its bounded snapshot size"
       {:maximum-helper-source-bytes max-helper-source-bytes
        :observed-helper-source-bytes before-size
        :missing-fact :bounded-helper-source-snapshot}))
    (let [buffer (ByteBuffer/allocate (inc max-helper-source-bytes))]
      (try
        (with-open [channel
                    (java.nio.channels.FileChannel/open
                     source-path
                     (into-array OpenOption
                                 [java.nio.file.StandardOpenOption/READ
                                  LinkOption/NOFOLLOW_LINKS]))]
          (let [channel-size-before (.size channel)
                observed-byte-count
                (loop [zero-reads 0]
                  (if-not (.hasRemaining buffer)
                    (.position buffer)
                    (let [read-count (.read channel buffer)]
                      (cond
                        (neg? read-count) (.position buffer)
                        (zero? read-count)
                        (if (= 8 zero-reads)
                          (helper-contract-fail!
                           request-source
                           "Gravity C emitter helper source did not make progress"
                           {:missing-fact :bounded-helper-source-read
                            :source-path (str source-path)})
                          (recur (inc zero-reads)))
                        :else (recur 0)))))
                channel-size-after (.size channel)
                after
                (try
                  (Files/readAttributes source-path BasicFileAttributes
                                        no-follow-options)
                  (catch java.io.IOException error
                    (helper-contract-fail!
                     request-source
                     "Gravity C emitter helper source disappeared while reading"
                     {:missing-fact :stable-helper-source-snapshot
                      :source-path (str source-path)
                      :cause-message (.getMessage error)})))
                bytes (java.util.Arrays/copyOf (.array buffer)
                                               observed-byte-count)
                content-hash
                (str "sha256:" (bootstrap/sha256-bytes-hex bytes))]
            (when-not
             (and (= channel-size-before channel-size-after
                     (long observed-byte-count))
                  (= (.fileKey ^BasicFileAttributes attributes)
                     (.fileKey ^BasicFileAttributes after))
                  (= (.lastModifiedTime ^BasicFileAttributes attributes)
                     (.lastModifiedTime ^BasicFileAttributes after))
                  (= (.size ^BasicFileAttributes attributes)
                     (.size ^BasicFileAttributes after)
                     (long observed-byte-count))
                  (<= observed-byte-count max-helper-source-bytes))
              (helper-contract-fail!
               request-source
               "Gravity C emitter helper source changed while being read"
               {:missing-fact :stable-helper-source-snapshot
                :source-path (str source-path)
                :observed-helper-source-bytes observed-byte-count}))
            {:source-path (str source-path)
             :source-byte-count observed-byte-count
             :source-content-hash content-hash
             :source-text (strict-utf8 request-source source-path bytes)}))
        (catch java.io.IOException error
          (helper-contract-fail!
           request-source
           "Gravity C emitter helper source cannot be read"
           {:missing-fact :stable-helper-source-snapshot
            :source-path (str source-path)
            :cause-message (.getMessage error)}))))))
