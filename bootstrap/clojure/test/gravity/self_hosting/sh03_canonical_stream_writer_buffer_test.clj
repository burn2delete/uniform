(ns gravity.self-hosting.sh03-canonical-stream-writer-buffer-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh03-canonical-stream-digest-test]))

(defn- operation [name] (deref (ns-resolve 'gravity.bootstrap name)))
(defn- fixture-operation [name]
  (deref (ns-resolve 'gravity.self-hosting.sh03-canonical-stream-digest-test name)))

(defn- encode [value buffered?]
  (let [output (java.io.ByteArrayOutputStream.)
        encoder (java.io.OutputStreamWriter.
                 output java.nio.charset.StandardCharsets/UTF_8)
        calls (atom 0)
        characters (atom 0)
        counted (proxy [java.io.FilterWriter] [encoder]
                  (write
                    ([value]
                     ;; FilterWriter delegates each overload straight to its
                     ;; wrapped writer, including single String/array writes.
                     (swap! calls inc)
                     (swap! characters +
                            (cond (integer? value) 1
                                  (string? value) (.length ^String value)
                                  :else (alength ^chars value)))
                     (proxy-super write value))
                    ([value offset length]
                     (swap! calls inc)
                     (swap! characters + length)
                     (proxy-super write value offset length))))
        writer (if buffered? (java.io.BufferedWriter. counted 8192) counted)
        started (System/nanoTime)
        emitted (with-open [writer writer]
                ((operation 'sh03-reader-canonical-stream-write!)
                 "/declared/buffer-micro.gravity" value writer))]
    {:bytes (.toByteArray output) :emission emitted :encoder-calls @calls
     :characters @characters :elapsed-ns (- (System/nanoTime) started)}))

(deftest fixed-buffer-preserves-real-reader-token-encoding
  ;; Finite encoder experiment built from real reader rows. Repeating rows is
  ;; not a source admission or whole-input fit test; physical occurrences count.
  ((fixture-operation 'run-canonical-check)
   (fn []
     (let [reader-delay (fixture-operation 'actual-reader-result)
           result (:result @reader-delay)
           rows (:token-stream result)
           value {:token-stream (vec (take 1024 (cycle rows)))}
           validated ((operation 'sh03-reader-canonical-stream-validation!)
                      "/declared/buffer-micro.gravity" value)
           plain (encode value false)
           buffered (encode value true)
           reference ((fixture-operation 'old-record) value)
           expected (.getBytes ^String (:text reference)
                               java.nio.charset.StandardCharsets/UTF_8)]
       (is (= :accepted (:status result)))
       (is (= 1024 (count (:token-stream value))))
       (is (java.util.Arrays/equals ^bytes expected ^bytes (:bytes plain)))
       (is (java.util.Arrays/equals ^bytes expected ^bytes (:bytes buffered)))
       (is (= (digest/sha256-bytes-hex expected)
              (digest/sha256-bytes-hex (:bytes plain))
              (digest/sha256-bytes-hex (:bytes buffered))))
       (is (= {:status :passed} validated (:emission plain) (:emission buffered)))
       (is (= (:characters plain) (:characters buffered)))
       (is (= (.length ^String (:text reference)) (:characters plain)))
       (is (pos? (:characters buffered)))
       (is (< (* 10 (:encoder-calls buffered)) (:encoder-calls plain)))
       (prn {:observation :real-reader-token-writer
             :token-rows 1024 :bytes (alength ^bytes expected)
             :plain (dissoc plain :bytes)
             :buffered (dissoc buffered :bytes)
             :validation validated})))))

(deftest fixed-buffer-preserves-surrogate-pair-across-flush-boundary
  ((fixture-operation 'run-canonical-check)
   (fn []
     (let [empty-text (:text ((fixture-operation 'old-record) ""))
           payload-start (inc (.indexOf ^String empty-text "\""))
           value (str (apply str (repeat (- 8191 payload-start) "x")) "🙂λ\"\\\n")
           reference ((fixture-operation 'old-record) value)
           validated ((operation 'sh03-reader-canonical-stream-validation!)
                      "/declared/buffer-unicode.gravity" value)
           expected (.getBytes ^String (:text reference)
                               java.nio.charset.StandardCharsets/UTF_8)
           plain (encode value false)
           buffered (encode value true)]
       ;; The pair is split across the 8192-character writer buffer boundary.
       (is (Character/isHighSurrogate (.charAt ^String (:text reference) 8191)))
       (is (Character/isLowSurrogate (.charAt ^String (:text reference) 8192)))
       (is (java.util.Arrays/equals ^bytes expected ^bytes (:bytes plain)))
       (is (java.util.Arrays/equals ^bytes expected ^bytes (:bytes buffered)))
       (is (= {:status :passed} validated (:emission plain) (:emission buffered)))
       (is (= (str "sha256:" (digest/sha256-bytes-hex expected))
              ((operation 'sh03-reader-canonical-stream-digest)
               "/declared/buffer-unicode.gravity" value)))))))
