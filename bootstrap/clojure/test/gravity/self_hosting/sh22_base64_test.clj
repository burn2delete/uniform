(ns gravity.self-hosting.sh22-base64-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b])
  (:import [java.util Base64]))

;; Initial executable group kernel; full input traversal is not yet exposed.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_base64_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))

(def ^:private engine
  (delay
    (let [source (str (io/file @root "bootstrap/gravity/src/gravity/stdlib/base64.gravity"))
          emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
      (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- encode-group [octets]
  (b/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-base64 :compiler-artifact-plan? true}
   @engine 'base64-encode-group [octets]))

(defn- jdk-encode [octets]
  (.encodeToString (Base64/getEncoder) (byte-array (map unchecked-byte octets))))

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
