(ns gravity.self-hosting.sh07-b51-v9-codec-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.io ByteArrayInputStream InputStream]
           [java.math BigInteger]
           [java.nio.charset StandardCharsets]))

(defn- bytes=
  [left right]
  (java.util.Arrays/equals ^bytes left ^bytes right))

(def ^:private strict-read-wire (:strict-read-wire codec/transport-api))
(def ^:private bounded-wire-bytes (:bounded-wire-bytes codec/transport-api))
(def ^:private read-bounded-bytes (:read-bounded-bytes codec/transport-api))
(def ^:private read-bounded-frame (:read-bounded-frame codec/transport-api))
(def ^:private bounded-byte-count! (:bounded-byte-count! codec/transport-api))
(def ^:private unsigned-byte-compare
  (:unsigned-byte-compare codec/transport-api))
(def ^:private unsigned-wire-sort (:unsigned-wire-sort codec/transport-api))

(defn- wire-text
  [value]
  (String. ^bytes (codec/W value) StandardCharsets/UTF_8))

(defn- reason
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo failure
      (is (= "SH07-B51-CODEC" (:id (ex-data failure))))
      (:reason (ex-data failure)))))

(deftest h-w-and-r-match-the-exact-v9-contract
  (let [left (array-map :z #{:b :a} :a [(array-map :y 2 :x 1)])
        right (array-map :a [(array-map :x 1 :y 2)] :z #{:a :b})
        expected-h
        (letfn [(rcv [item]
                  (canonical/reader-canonical-value rcv item))]
          (canonical/reader-canonical-hash digest/sha256-hex rcv left))]
    (is (= expected-h (codec/H left)))
    (is (= (codec/H left) (codec/H right)))
    (is (bytes= (codec/W left) (codec/W right)))
    (is (= "{:a [{:x 1, :y 2}], :z #{:a :b}}\n" (wire-text left)))
    (is (= "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
           (codec/R (byte-array 0))))
    (is (= (str "sha256:" (digest/sha256-bytes-hex (codec/W left)))
           (codec/R (codec/W left))))))

(deftest strict-wire-round-trips-the-closed-plain-edn-domain
  (let [value {:nil nil
               :boolean true
               :integer 9223372036854775808N
               :string "Gravity \u03bb \uD83D\uDE80"
               :character \newline
               :keyword :gravity/value
               :symbol 'gravity/value
               :map {{:nested :key} '(1 2)}
               :vector [1 2]
               :set #{"z" "\u00e9"}
               :list '(quote [1 2])}
        bytes (bounded-wire-bytes :request value)]
    (is (= value (strict-read-wire :request bytes)))
    (is (= value
           (read-bounded-frame :request (ByteArrayInputStream. bytes))))
    (is (bytes= bytes (codec/W (strict-read-wire :request bytes))))))

(deftest strict-wire-rejects-malformed-or-noncanonical-input-before-use
  (testing "malformed and unmappable UTF-8 never receives replacement decoding"
    (doseq [bytes [(byte-array [(unchecked-byte 0xc3) (byte 0x28)])
                   (byte-array [(unchecked-byte 0xed)
                                (unchecked-byte 0xa0)
                                (unchecked-byte 0x80)])]]
      (is (= :malformed-utf8
             (reason #(strict-read-wire :request bytes))))))
  (testing "duplicate map keys are found by the lexical preflight"
    (is (= :duplicate-map-key
           (reason #(strict-read-wire
                     :request (.getBytes "{:a 1, :a 2}\n" "UTF-8")))))
    (is (= :duplicate-map-key
           (reason #(strict-read-wire
                     :request (.getBytes "{:a {:b 1 :b 2}}\n" "UTF-8"))))))
  (testing "the wire is exactly one plain EDN form plus LF and EOF"
    (doseq [[source expected]
            [["" :empty-wire]
             ["{:a 1}" :wire-divergence]
             ["{:a 1}\n\n" :wire-divergence]
             ["{:b 2 :a 1}\n" :wire-divergence]
             ["1\n2\n" :multiple-forms]
             ["#gravity/not-allowed {:a 1}\n" :reader-dispatch]
             ["1.0\n" :non-plain-edn]
             ["1/2\n" :non-plain-edn]]]
      (is (= expected
             (reason #(strict-read-wire
                       :request (.getBytes source "UTF-8"))))
          source)))
  (testing "encoder inputs are strict values, not arbitrary host sequences"
    (is (= :non-plain-edn (reason #(codec/W (map identity [1 2])))))
    (is (= :non-plain-edn (reason #(codec/H 1.0))))
    (is (= :non-unicode-scalar (reason #(codec/W "\ud800")))))
  (testing "comparator collisions reject before a sorted map can erase a key"
    (let [collision-map
          (into (sorted-map-by
                 (fn [left right]
                   (compare (.getName (class left)) (.getName (class right)))))
                [[(long 1) :long] [(BigInteger/valueOf 1) :big-integer]])]
      (is (= 2 (count collision-map)))
      (is (= :comparator-key-collision (reason #(codec/W collision-map)))))))

(deftest transport-bounds-are-literal-inclusive-and-role-closed
  (is (= {:request-max-bytes 4194304
          :response-max-bytes 1073741824
          :stderr-max-bytes 1048576}
         codec/transport-limits))
  (doseq [[role maximum]
          [[:request 4194304]
           [:response 1073741824]
           [:stderr 1048576]]]
    (is (= maximum (bounded-byte-count! role maximum)))
    (is (= :transport-byte-count
           (reason #(bounded-byte-count! role (inc maximum))))))
  (is (= :transport-byte-count
         (reason #(bounded-byte-count! :request -1))))
  (is (= :transport-role
         (reason #(bounded-byte-count! :checker-output 0))))
  (is (= :wire-role
         (reason #(strict-read-wire :stderr (byte-array 0)))))
  (is (= :wire-role
         (reason #(bounded-wire-bytes :stderr nil)))))

(deftest bounded-drain-fails-on-the-first-byte-past-the-request-cap
  (let [remaining (volatile! 4194305)
        input
        (proxy [InputStream] []
          (read
            ([]
             (if (zero? @remaining)
               -1
               (do (vswap! remaining dec) 0)))
            ([buffer offset length]
             (if (zero? @remaining)
               -1
               (let [amount (int (min @remaining length))]
                 (java.util.Arrays/fill ^bytes buffer offset (+ offset amount)
                                        (byte 0))
                 (vswap! remaining - amount)
                 amount)))))]
    (is (= :transport-byte-count
           (reason #(read-bounded-bytes :request input))))))

(deftest unsigned-byte-order-is-octet-based-prefix-aware-and-tie-closed
  (is (neg? (unsigned-byte-compare
             (byte-array [(byte 0x7f)])
             (byte-array [(unchecked-byte 0x80)]))))
  (is (pos? (unsigned-byte-compare
             (byte-array [(unchecked-byte 0xff)])
             (byte-array [(byte 0x00)]))))
  (is (neg? (unsigned-byte-compare
             (byte-array [(byte 1)])
             (byte-array [(byte 1) (byte 0)]))))
  (is (zero? (unsigned-byte-compare
              (byte-array [(unchecked-byte 0xff)])
              (byte-array [(unchecked-byte 0xff)]))))
  (is (= [{:key "z"} {:key "\u00e9"}]
         (unsigned-wire-sort :key [{:key "\u00e9"} {:key "z"}])))
  (is (= :duplicate-wire-sort-key
         (reason #(unsigned-wire-sort
                   (constantly :same) [{:ordinal 0} {:ordinal 1}])))))
