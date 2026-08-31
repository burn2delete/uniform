(ns gravity.p15-native-packet-binding.source-identity
  (:require [clojure.java.io :as io])
  (:import [java.nio.file Files LinkOption Path]
           [java.nio.file.attribute BasicFileAttributes]))

(defn repository-root [no-follow-options bounds-fail!]
  (let [resource (io/resource "gravity/p15_native_packet_binding.clj")]
    (when-not resource
      (bounds-fail! nil "native packet binding source is not on the classpath"
                    {:missing-fact :binding-source-resource}))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (bounds-fail! nil "repository root is unavailable"
                      {:missing-fact :repository-root})

        (Files/isRegularFile (.resolve candidate "deps.edn") no-follow-options)
        candidate

        :else
        (recur (.getParent candidate))))))

(defn file-identity
  [relative root no-follow-options identity-file-limit bounds-fail!
   sha256-bytes-hex]
  (let [path (.resolve ^Path root relative)
        before (Files/readAttributes path BasicFileAttributes
                                     no-follow-options)]
    (when-not (and (.isRegularFile before)
                   (<= (.size before) identity-file-limit))
      (bounds-fail! relative "native runtime identity file is unavailable"
                    {:maximum-identity-file-bytes identity-file-limit
                     :missing-fact :bounded-regular-runtime-identity-file}))
    (let [bytes (Files/readAllBytes path)
          after (Files/readAttributes path BasicFileAttributes
                                      no-follow-options)]
      (when-not (and (.isRegularFile after)
                     (= (.fileKey before) (.fileKey after))
                     (= (.size before) (.size after))
                     (= (.size after) (alength bytes)))
        (bounds-fail! relative "native runtime identity changed while reading"
                      {:missing-fact :stable-runtime-identity-file}))
      {:path relative
       :content-hash (str "sha256:" (sha256-bytes-hex bytes))})))
