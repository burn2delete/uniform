(ns gravity.self-hosting.sh03-source-slice-decode-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- bytes-of [text]
  (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8))
(defn- raw-slice [start end scalar-start scalar-end]
  {:artifact :gravity/source-slice :schema-version 1 :encoding :utf-8
   :source-content-id "source-content" :byte-start start :byte-end end
   :scalar-start scalar-start :scalar-end scalar-end})
(defn- decode [bytes boundaries raw]
  (bootstrap/sh03-reader-source-slice-text!
   "fixture.gravity" bytes "source-content" boundaries raw))
(defn- refuses? [f]
  (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest source-boundaries-retain-complete-unicode-and-byte-count-joins
  (is (= {0 0 1 1 3 2 7 3 8 4}
         (bootstrap/sh03-reader-source-scalar-boundaries!
          "fixture.gravity" "A\u03bb\uD83D\uDE42Z" (bytes-of "A\u03bb\uD83D\uDE42Z"))))
  (is (= {0 0} (bootstrap/sh03-reader-source-scalar-boundaries!
                "fixture.gravity" "" (byte-array 0))))
  (is (refuses? #(bootstrap/sh03-reader-source-scalar-boundaries!
                 "fixture.gravity" "abc" (byte-array 0))))
  (is (refuses? #(bootstrap/sh03-reader-source-scalar-boundaries!
                 "fixture.gravity" "\u03bb" (bytes-of "x")))))

(deftest slices-preserve-strict-unicode-and-do-not-copy-the-source-array
  (let [text "A\u03bb\uD83D\uDE42Z" bytes (bytes-of text)
        original (aclone bytes) boundaries {0 0 1 1 3 2 7 3 8 4}
        decoder (ns-resolve 'gravity.bootstrap 'sh03-reader-decode-buffer!)
        decode-buffer @decoder seen (atom [])]
    (with-redefs-fn
      {decoder (fn [path raw buffer]
                 (swap! seen conj [(identical? bytes (.array ^java.nio.ByteBuffer buffer))
                                   (.position ^java.nio.ByteBuffer buffer)
                                   (.limit ^java.nio.ByteBuffer buffer)])
                 (decode-buffer path raw buffer))}
      #(do (is (= text (decode bytes boundaries (raw-slice 0 8 0 4))))
           (is (= "\u03bb\uD83D\uDE42" (decode bytes boundaries (raw-slice 1 7 1 3))))
           (is (= "" (decode bytes boundaries (raw-slice 3 3 2 2))))))
    (is (= [[true 0 8] [true 1 7] [true 3 3]] @seen))
    (is (java.util.Arrays/equals ^bytes original ^bytes bytes))
    (is (= text (bootstrap/sh03-reader-decode-raw-bytes! "fixture.gravity" {} bytes)))))

(deftest slice-identity-ranges-and-scalar-boundaries-still-refuse-substitution
  (let [bytes (bytes-of "A\u03bbZ") boundaries {0 0 1 1 3 2 4 3}
        raw (raw-slice 1 3 1 2)]
    (doseq [bad [(assoc raw :source-content-id "other")
                 (assoc raw :schema-version 2) (assoc raw :encoding :ascii)
                 (assoc raw :extra true) (dissoc raw :scalar-end)
                 (assoc raw :byte-start -1) (assoc raw :byte-end 5)
                 (assoc raw :byte-start 4) (assoc raw :byte-end 2)
                 (assoc raw :scalar-start 0)]]
      (is (refuses? #(decode bytes boundaries bad))))
    (is (refuses? #(decode bytes {1 1 3 3} (raw-slice 1 3 1 3))))
    (is (refuses? #(bootstrap/sh03-reader-accepted-raw-text!
                   "fixture.gravity" bytes "source-content" boundaries raw
                   {:byte-start 0 :byte-end 3})))))

(deftest malformed-utf8-remains-rejected-by-both-decoding-paths
  (let [bytes (byte-array [(unchecked-byte 0xc3) (unchecked-byte 0x28)])]
    (is (refuses? #(decode bytes {0 0 2 1} (raw-slice 0 2 0 1))))
    (is (refuses? #(bootstrap/sh03-reader-decode-raw-bytes! "fixture.gravity" {} bytes)))))
