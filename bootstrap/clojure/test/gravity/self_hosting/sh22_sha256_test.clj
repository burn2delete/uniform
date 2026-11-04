(ns gravity.self-hosting.sh22-sha256-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; Implementation checkpoint: actual Uniform padding execution, without a
;; digest, caller-adoption, migration, release or seed-retirement claim.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_sha256_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file
        (recur (.getParentFile file))))))

(def ^:private engine
  (delay
    (let [source (str (io/file @root "bootstrap/gravity/src/gravity/stdlib/sha256.gravity"))
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- call [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-sha256 :compiler-artifact-plan? true}
   @engine function arguments))

(defn- pad [arguments]
  (call 'sh22-sha256-pad [arguments]))

(defn- expected-padding [values]
  (let [size (count values)
        zeros (mod (- 55 size) 64)
        trailer (.array (doto (java.nio.ByteBuffer/allocate 8)
                         (.putLong (* 8 (long size)))))]
    (into (into (conj values 128) (repeat zeros 0))
          (map #(bit-and 255 %) trailer))))

(deftest sh22-sha256-padding-executes-boundaries-and-literal-octets
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @engine)))
  (doseq [values (concat [[] [97 98 99] (vec (range 256))]
                         (for [size [31 32 55 56 63 64 65 119 120 127 128 129 1024]]
                           (vec (take size (cycle (range 256))))))]
    (let [result (pad [values]) padded (:value result)]
      (is (= :accepted (:status result)))
      (is (= (expected-padding values) padded))
      (is (= values (subvec padded 0 (count values))))
      (is (zero? (mod (count padded) 64)))))
  (is (not= (:value (pad [[0 1 2]])) (:value (pad [[0 1 3]])))))

(deftest sh22-sha256-length-trailer-preserves-full-format-width
  ;; Exercise the 64-bit SHA length encoding directly without allocating an
  ;; impossible input vector. These are representability cases, not quotas.
  (doseq [size [0 1 31 32 255 256 536870912 1152921504606846976
                2305843009213693951]]
    (let [bits (* 8N size)]
      (is (= (mapv #(int (mod (quot bits (reduce *' 1 (repeat % 256))) 256))
                   (range 7 -1 -1))
             (call 'sha256-length-trailer [size]))))))

(deftest sh22-sha256-padding-rejects-malformed-carriers-and-non-octets
  (doseq [[arguments reason]
          (concat
           (for [carrier [nil {} '([]) "abc" #{[]}]]
             [carrier :expected-argument-vector])
           (for [arguments [[] [[] []] [[] [] []]]]
             [arguments :invalid-arity])
           (for [values [nil {} '(0) #{0} "abc" :bytes]]
             [[values] :expected-octet-vector])
           (for [value [-1 256 1.0 1/2 false nil :byte 'byte [] {}]]
             [[[0 value 255]] :non-octet]))]
    (is (= {:status :rejected
            :diagnostics [{:rule "SH22SHA001" :reason reason}]}
           (pad arguments))))
  (let [renderings (atom 0)
        opaque (proxy [Object] []
                 (toString [] (swap! renderings inc)
                   (throw (ex-info "opaque input rendered" {}))))]
    (is (= :expected-argument-vector (get-in (pad opaque) [:diagnostics 0 :reason])))
    (is (= :expected-octet-vector (get-in (pad [opaque]) [:diagnostics 0 :reason])))
    (is (= :non-octet (get-in (pad [[opaque]]) [:diagnostics 0 :reason])))
    (is (zero? @renderings))))
