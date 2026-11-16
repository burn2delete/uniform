(ns gravity.self-hosting.sh22-base64-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b])
  (:import [java.util Base64]))

;; Execute current Uniform source, with the JDK codec used only as an oracle.
;; This is focused implementation evidence, not production cache adoption,
;; authoritative migration, native execution or seed-retirement evidence.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_base64_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))

(def ^:private engine
  (delay
    (let [source (str (io/file @root "bootstrap/gravity/src/gravity/stdlib/base64.gravity"))
          emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
      (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- invoke [function arguments]
  (b/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-base64 :compiler-artifact-plan? true}
   @engine function arguments))

(defn- encode-group [octets] (invoke 'base64-encode-group [octets]))
(defn- encode [octets] (invoke 'base64-encode [octets]))

(defn- jdk-encode [octets]
  (.encodeToString (Base64/getEncoder) (byte-array (map unchecked-byte octets))))

(deftest whole-input-executes-standard-padded-base64
  ;; Complete RFC-style vectors cover each terminal residue and multi-group
  ;; traversal. In particular, complete groups must not acquire padding.
  (doseq [[text expected] [["" ""] ["f" "Zg=="] ["fo" "Zm8="]
                           ["foo" "Zm9v"] ["foob" "Zm9vYg=="]
                           ["fooba" "Zm9vYmE="] ["foobar" "Zm9vYmFy"]]]
    (let [octets (mapv int text)]
      (is (= {:status :accepted :value expected} (encode octets)))
      (is (= expected (jdk-encode octets)))))
  (doseq [size (concat (range 13) [63 64 65 255 256 257 258])]
    (let [octets (mapv #(mod % 256) (range size))
          result (encode octets)
          text (:value result)]
      (is (= {:status :accepted :value (jdk-encode octets)} result))
      (is (= (* 4 (quot (+ size 2) 3)) (count text)))
      (is (re-matches #"(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?" text)))))

(deftest all-octets-and-native-integer-carriers-remain-byte-sensitive
  (let [octets (vec (range 256))
        baseline (:value (encode octets))]
    (is (= (jdk-encode octets) baseline))
    (is (= (set "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=")
           (set baseline)))
    ;; Changes span group boundaries and the final padded group.
    (doseq [index [0 1 2 3 127 128 254 255]]
      (let [changed (assoc octets index (bit-xor 128 (get octets index)))
            result (encode changed)]
        (is (= {:status :accepted :value (jdk-encode changed)} result))
        (is (not= baseline (:value result))))))
  (doseq [convert [byte short int long bigint biginteger]]
    (let [octets (mapv convert [0 1 63 64 126 127])]
      (is (= {:status :accepted :value (jdk-encode octets)} (encode octets)))))
  (doseq [convert [short int long bigint biginteger]]
    (let [octets (mapv convert [128 129 254 255])]
      (is (= {:status :accepted :value (jdk-encode octets)} (encode octets))))))

(deftest uniform-traversal-executes-grouping-and-alphabet-lookup
  @engine
  (let [dispatch (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @dispatch
        groups (atom [])
        alphabet (atom [])
        octets [0 1 2 253 254 255 128]
        result (with-redefs-fn
                 {dispatch
                  (fn [runtime plan function arguments scope]
                    (case function
                      base64-group-text (swap! groups conj (vec (rest arguments)))
                      base64-alphabet-entry (swap! alphabet conj (first arguments))
                      nil)
                    (original runtime plan function arguments scope))}
                 #(encode octets))]
    (is (= {:status :accepted :value (jdk-encode octets)} result))
    (is (= [[0 3] [3 3] [6 1]] @groups))
    (is (= [0 0 4 2 63 31 59 63 32 0] @alphabet))))

(deftest whole-input-rejects-malformed-data-and-supplied-answers-inertly
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (toString [] (swap! calls inc) (throw (AssertionError. "opaque rendering"))))
        lazy-carrier (lazy-seq (swap! calls inc) (throw (AssertionError. "lazy traversal")))]
    (doseq [value [-1 256 Long/MIN_VALUE Long/MAX_VALUE -1N 256N
                   (biginteger -1) (biginteger 256) 1/2 1.0 1M
                   Double/NaN Double/POSITIVE_INFINITY nil false "0" \0 :octet opaque]]
      ;; Invalid elements after complete groups must still reject the input.
      (doseq [prefix [[] [0 1 2 3 4 5]]]
        (is (= {:status :rejected :rule "STD4001" :reason :expected-octet}
               (encode (conj prefix value))))))
    (doseq [value [nil false "Zg==" {:value "Zg=="}
                   {:octets [102] :value "Zg=="} {:status :accepted :value "Zg=="}
                   {:kind :vector :values [102]} #{102} '(102)
                   (byte-array [102]) (subvec [0 102] 1) lazy-carrier opaque]]
      (is (= {:status :rejected :rule "STD4001" :reason :expected-native-octet-vector}
             (encode value))))
    (doseq [arguments [[] [[102] "supplied-answer"] [opaque opaque opaque]]]
      (let [failure (try (invoke 'base64-encode arguments) nil
                         (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (= "L2-FUNCTION-ARITY" (:id failure)))
        (is (= 1 (:expected-arity failure)))
        (is (= (count arguments) (:actual-arity failure)))))
    (is (zero? @calls))))

(deftest terminal-groups-execute-uniform-alphabet-and-padding
  (doseq [[octets expected] [[[] ""] [[102] "Zg=="] [[102 111] "Zm8="]
                              [[102 111 111] "Zm9v"] [[0 0 0] "AAAA"]
                              [[255 255 255] "////"] [[251 239 190] "++++"]]]
    (let [result (encode-group octets)]
      (is (= {:status :accepted :value expected} result))
      (is (= (jdk-encode octets) (:value result)))))
  ;; Distinct high-bit and low-bit values must change the complete result.
  (doseq [octets [[0] [1] [127] [128] [255] [0 1] [1 0] [0 0 1] [1 0 0]]]
    (is (= (jdk-encode octets) (:value (encode-group octets))))))

(deftest terminal-groups-reject-non-octets-without-rendering-payloads
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (toString [] (swap! calls inc) (throw (AssertionError. "opaque rendering"))))]
    (doseq [value [-1 256 1/2 1.0 nil false "0" opaque]]
      (is (= {:status :rejected :rule "STD4001" :reason :expected-octet}
             (encode-group [value]))))
    (doseq [value [nil "Zg==" {:value "Zg=="} '(102) opaque]]
      (is (= {:status :rejected :rule "STD4001" :reason :expected-native-octet-vector}
             (encode-group value))))
    (is (zero? @calls)))
  (is (= {:status :rejected :rule "STD4001" :reason :invalid-group-extent}
         (encode-group [0 0 0 0]))))
