(ns gravity.self-hosting.sh22-sha256-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]
            [gravity.release :as release]))

;; Current-source plan execution and differential development evidence only.
;; No migration, native execution, release or seed-retirement claim.
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

(defn- hash-octets [arguments]
  (call 'sh22-sha256 [arguments]))

(defn- octet-array [values]
  (byte-array (map unchecked-byte values)))

(defn- jdk-sha256 [values]
  (apply str (map #(format "%02x" (bit-and 255 %))
                  (.digest (java.security.MessageDigest/getInstance "SHA-256")
                           (octet-array values)))))

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
           (for [carrier [nil {} '([]) "abc" #{[]} (byte-array [0])]]
             [carrier :expected-argument-vector])
           (for [arguments [[] [[] []] [[] [] []]]]
             [arguments :invalid-arity])
           (for [values [nil {} '(0) #{0} "abc" :bytes (byte-array [0])]]
             [[values] :expected-octet-vector])
           (for [value [-1 256 1.0 1/2 false nil :byte 'byte [] {}]]
             [[[0 value 255]] :non-octet]))]
    (doseq [operation [pad hash-octets]]
      (is (= {:status :rejected
              :diagnostics [{:rule "SH22SHA001" :reason reason}]}
             (operation arguments)))))
  (let [renderings (atom 0)
        opaque (proxy [Object] []
                 (toString [] (swap! renderings inc)
                   (throw (ex-info "opaque input rendered" {}))))]
    (doseq [operation [pad hash-octets]]
      (is (= :expected-argument-vector (get-in (operation opaque) [:diagnostics 0 :reason])))
      (is (= :expected-octet-vector (get-in (operation [opaque]) [:diagnostics 0 :reason])))
      (is (= :non-octet (get-in (operation [[opaque]]) [:diagnostics 0 :reason])))
      (is (= :invalid-arity (get-in (operation [opaque opaque]) [:diagnostics 0 :reason]))))
    (is (zero? @renderings))))

(deftest sh22-sha256-computes-complete-known-answers
  (doseq [[text expected]
          [["" "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"]
           ["abc" "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"]
           ["abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1"]
           [(str "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmn"
                 "hijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu")
            "cf5b16a778af8380036ce59e7b0492370b249b11e8f07a51afac45037afee9d1"]]]
    (let [values (mapv int text)
          result (hash-octets [values])]
      (is (= {:status :accepted :value expected} result))
      (is (= (jdk-sha256 values) (:value result)))
      (is (= (digest/sha256-bytes-hex (octet-array values)) (:value result)))
      (is (= (release/bytes-sha256 (octet-array values)) (:value result))))))

(deftest sh22-sha256-computes-all-octets-padding-boundaries-and-multiple-blocks
  (doseq [size [55 56 63 64 65 119 120 127 128 129 256 1024]]
    (let [values (vec (take size (cycle (range 256))))
          result (hash-octets [values])]
      (is (= :accepted (:status result)))
      (is (= (jdk-sha256 values) (:value result)) (str "byte length " size))
      (is (re-matches #"[0-9a-f]{64}" (:value result)))))
  ;; Arbitrary nontext bytes, including invalid UTF-8, exercise computation
  ;; beyond the standard vectors. The independent oracle is test-only.
  (let [random (java.util.Random. 220256)
        values (vec (repeatedly 193 #(.nextInt random 256)))
        changed (update values 91 #(mod (inc %) 256))
        actual (:value (hash-octets [values]))
        other (:value (hash-octets [changed]))]
    (is (= (jdk-sha256 values) actual))
    (is (= (jdk-sha256 changed) other))
    (is (not= actual other))
    (is (= actual (:value (hash-octets [values]))))))

(deftest sh22-sha256-does-not-accept-claimed-digests-or-provider-requests
  (let [values [97 98 99]
        correct (jdk-sha256 values)
        wrong (apply str (repeat 64 "0"))]
    ;; Both correct and incorrect supplied answers reject, rather than being
    ;; trusted or replayed. Only an octet vector is an accepted input.
    (doseq [claim [correct wrong]]
      (is (= {:status :rejected
              :diagnostics [{:rule "SH22SHA001" :reason :invalid-arity}]}
             (hash-octets [values claim])))
      (is (= :expected-octet-vector
             (get-in (hash-octets [{:bytes values :digest claim}])
                     [:diagnostics 0 :reason]))))
    (is (= :expected-octet-vector
           (get-in (hash-octets [{:operation :sha256 :arguments values}])
                   [:diagnostics 0 :reason])))
    (is (not= wrong (:value (hash-octets [values]))))))

(deftest sh22-sha256-existing-release-caller-consumes-uniform-provider
  ;; Authentication still uses host hashing; only the adopted release digest
  ;; computation is tripwired. Exercise the actual P18 caller, not a test port.
  (let [file (java.io.File/createTempFile "uniform-sha256-" ".bin")
        execute bootstrap/p15-s23-stage2-runtime-execute-function
        calls (atom [])]
    (try
      (doseq [values [[0 255 128 192 175 97] (vec (range 256)) []]]
        (with-open [output (io/output-stream file)]
          (.write output (octet-array values)))
        (reset! calls [])
        (with-redefs [release/bytes-sha256
                      (fn [_] (throw (AssertionError. "unexpected release digest")))
                      bootstrap/p15-s23-stage2-runtime-execute-function
                      (fn [runtime plan callee arguments]
                        (when (= 'sh22-sha256 callee)
                          (swap! calls conj [(get-in plan [:module :module]) arguments]))
                        (execute runtime plan callee arguments))]
          (is (= (str "sha256:" (jdk-sha256 values))
                 (bootstrap/p18-file-sha256 (.getPath file))))
          (is (= [['gravity.stdlib.sha256 [[values]]]] @calls))))
      (finally (.delete file)))))

(defn- rejection [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error error)))

(deftest sh22-sha256-release-rejects-missing-or-changed-bindings
  (let [plan @engine
        resolver (ns-resolve 'gravity.bootstrap 'p15-s23-sh07-governed-source-file)
        invoke #(bootstrap/p18-bytes-sha256 (byte-array 0))
        executions (atom 0)]
    (with-redefs [release/bytes-sha256
                  (fn [_] (throw (AssertionError. "legacy digest must not run")))
                  bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [& _] (swap! executions inc))]
      (with-redefs-fn {resolver (constantly nil)}
        #(is (= {:rule "SH20SHA002" :reason :missing-kernel-source}
                (ex-data (rejection invoke)))))
      (doseq [[changed reason]
              [[{} :kernel-source-plan-mismatch]
               [(assoc-in plan [:source :sha256] "sha256:changed") :kernel-source-plan-mismatch]
               [(update plan :functions dissoc 'sh22-sha256) :kernel-abi-mismatch]
               [(assoc-in plan [:functions 'sh22-sha256 :arity] 2) :kernel-abi-mismatch]
               [(assoc-in plan [:functions 'sh22-sha256 :params] ['other]) :kernel-abi-mismatch]
               [(assoc-in plan [:module :module] 'ambient.sha256) :kernel-abi-mismatch]
               [(assoc-in plan [:module :effects] #{:io}) :kernel-abi-mismatch]
               [(assoc plan :plan-id "sha256:changed") :kernel-plan-id-mismatch]]]
        (with-redefs [bootstrap/p15-s23-stage2-compiler-artifact-plan (fn [& _] changed)]
          (is (= {:rule "SH20SHA002" :reason reason}
                 (ex-data (rejection invoke))))))
      (let [file (java.io.File/createTempFile "uniform-sha256-source-" ".gravity")
            source (io/file @root "bootstrap/gravity/src/gravity/stdlib/sha256.gravity")]
        (try
          (io/copy source file)
          (with-redefs-fn
            {resolver (constantly file)
             #'bootstrap/p15-s23-stage2-compiler-artifact-plan
             (fn [& _] (spit file "\n; source changed during construction\n" :append true) plan)}
            #(is (= {:rule "SH20SHA002" :reason :kernel-source-changed}
                    (ex-data (rejection invoke)))))
          (finally (.delete file))))
      (is (zero? @executions)))))

(deftest sh22-sha256-retains-explicit-internal-policy-and-audit-nonclaims
  (let [policy (call 'sh22-sha256-policy [])]
    (is (= :sha-256 (:algorithm policy)))
    (is (= :unkeyed-content-hash (:mode policy)))
    (is (= :uniform-source (:provider policy)))
    (is (= :internal (:visibility policy)))
    (is (= :restricted-to-unkeyed-content-identity (:primitive-policy policy)))
    (is (= :unreviewed (:audit-status policy)))
    (is (= :experimental (:stability policy)))
    (is (= :explicit-modulo-4294967296 (:word-arithmetic policy)))
    (is (= #{:octet-validation :padding :compression :hex-rendering}
           (:implemented policy)))
    (doseq [field [:text-decoding :canonicalization :constant-time-claim
                   :secret-handling-claim]]
      (is (false? (get policy field))))
    (is (= #{} (:effects policy)))
    (is (= #{} (:capabilities policy)))))
