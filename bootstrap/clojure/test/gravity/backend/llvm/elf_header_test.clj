(ns gravity.backend.llvm.elf-header-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap])
  (:import [java.nio ByteBuffer ByteOrder]))

; Current-source implementation feedback; no migration or release authority.
(defn- compile-plan [relative-path]
  (let [source (.getCanonicalPath (io/file relative-path))
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source (slurp source))))

(def ^:private engine-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity")))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-elf-prefix-development :compiler-artifact-plan? true}
   @engine-plan function arguments))

(defn- prefix-valid? [octets kind]
  (invoke 'w5-b13-linux-elf-header-prefix-valid? [octets kind]))

(defn- header [type]
  [127 69 76 70 2 1 1 0 0 0 0 0 0 0 0 0 type 0 62 0])

; Independent test oracle: Java's binary decoder reads unsigned little-endian
; shorts, rather than duplicating the Uniform multiply/add implementation.
; This is a prefix oracle, not complete ELF inspection or authentication.
(defn- oracle [octets kind]
  (boolean
   (and (vector? octets)
        (<= 20 (count octets))
        (every? #(and (integer? %) (<= 0 % 255)) octets)
        (let [buffer (doto (ByteBuffer/wrap (byte-array (map unchecked-byte octets)))
                       (.order ByteOrder/LITTLE_ENDIAN))]
          (and (= [127 69 76 70 2 1 1] (subvec octets 0 7))
               (= (if (= :object kind) 1 2)
                  (Short/toUnsignedInt (.getShort buffer 16)))
               (= 62 (Short/toUnsignedInt (.getShort buffer 18))))))))

(defn- check-prefix [expected octets kind]
  (let [actual (prefix-valid? octets kind)]
    (is (= expected (oracle octets kind)))
    (is (= expected actual))
    (is (boolean? actual))))

(deftest current-uniform-elf-prefix-checkpoint
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @engine-plan)))
  (doseq [[kind type] [[:object 1] [:executable 2] [:other 2] [nil 2]
                     [:llvm-object 2] ["object" 2]]]
    (let [octets (header type)]
      (check-prefix true octets kind)
      (check-prefix true (into octets [0 255 42]) kind)
      (check-prefix false (header (if (= 1 type) 2 1)) kind))))

(deftest inspected-fields-match-independent-oracle
  (doseq [[kind type] [[:object 1] [:executable 2]]
          index [0 1 2 3 4 5 6 16 17 18 19]
          :let [octets (header type) original (get octets index)]
          value (distinct [0 255 original (inc original)])]
    (check-prefix (= original value) (assoc octets index value) kind)))

(deftest type-and-machine-bytes-are-independent-little-endian-fields
  (doseq [[kind type] [[:object 1] [:executable 2]]
          [offset expected] [[16 type] [18 62]]
          value [0 1 2 3 61 62 63 255]]
    ; Mutate each half while holding the other half fixed.
    (check-prefix (= expected value) (assoc (header type) offset value) kind)
    (check-prefix (zero? value) (assoc (header type) (inc offset) value) kind))
  (doseq [[kind type] [[:object 1] [:executable 2]]]
    (check-prefix false (assoc (header type) 16 0 17 type) kind)
    (check-prefix false (assoc (header type) 18 0 19 62) kind)))

(deftest prefix-length-and-uninspected-octets
  (doseq [[kind type] [[:object 1] [:executable 2]]]
    (doseq [length (range 20)]
      (check-prefix false (subvec (header type) 0 length) kind))
    (doseq [index (range 7 16)]
      (check-prefix true (assoc (header type) index 255) kind))
    ; Neither padding nor trailing bytes supply additional ELF guarantees.
    (check-prefix true (into (header type) (range 256)) kind)
    (check-prefix true (into (header type) (repeat 44 255)) kind)))

(deftest malformed-octet-carriers-return-boolean-false
  (let [octets (header 1)]
    (doseq [carrier [nil false 20 "ELF" {:octets octets} (set octets)
                     (apply list octets) (seq octets)
                     (byte-array (map unchecked-byte octets))]]
      (check-prefix false carrier :object))
    (doseq [value [-1 256 1.0 1/2 nil true "0" \0 :invalid [0] {} #{0} '(0)]
            index [0 7 19 275]]
      ; A late invalid element must be rejected even after a valid prefix.
      (check-prefix false (assoc (into octets (range 256)) index value) :object))))

(deftest opaque-inputs-remain-inert
  (let [touches (atom [])
        payload (reify
                  clojure.lang.Seqable
                  (seq [_] (swap! touches conj :seq) (throw (ex-info "opaque" {})))
                  Object
                  (toString [_] (swap! touches conj :string) "opaque"))
        callback (fn [] (swap! touches conj :call))
        deferred (delay (swap! touches conj :force))]
    (is (= ""
           (with-out-str
             (doseq [value [payload callback deferred '(println "escaped")]]
               (check-prefix false value :object)
               (check-prefix false (conj (header 1) value) :object)
               ; Only the keyword :object selects ET_REL; other kinds are data.
               (check-prefix true (header 2) value)))))
    (is (empty? @touches))
    (is (false? (realized? deferred)))))

(deftest wrong-arity-fails-through-runtime-execution
  (doseq [arguments [[] [(header 1)] [(header 1) :object :extra]]]
    (let [failure (try
                    (invoke 'w5-b13-linux-elf-header-prefix-valid? arguments)
                    nil
                    (catch clojure.lang.ExceptionInfo error (ex-data error)))]
      (is (= "L2-FUNCTION-ARITY" (:id failure)))
      (is (= 'w5-b13-linux-elf-header-prefix-valid? (:function failure)))
      (is (= 2 (:expected-arity failure)))
      (is (= (count arguments) (:actual-arity failure)))
      (is (= (get-in @engine-plan [:source :path])
             (get-in failure [:source-span :source]))))))

(deftest valid-prefix-does-not-confer-artifact-authority
  ; Twenty bytes cannot contain a complete ELF64 header, let alone an object.
  (check-prefix true (header 1) :object)
  (let [policy (invoke 'w5-b13-policy [])]
    (is (= :llvm-x86_64-linux (:target policy)))
    (is (= :stage2-static-only (:scope policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:release-eligible? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:public-authority? policy)))))

(deftest b13-module-rejection-branches-execute-with-explicit-else
  ; The prefix route compiles the whole B13 module. Exercise the outer
  ; provenance/evidence branches that previously lacked required else operands.
  (let [fixture-plan
        (compile-plan
         "bootstrap/clojure/fixtures/self-hosting/w5-b13/accepted/artifact-emission.gravity")
        request
        (bootstrap/p15-s23-stage2-runtime-execute-function
         {:engine :gravity-elf-prefix-development :compiler-artifact-plan? true}
         fixture-plan 'w5-b13-artifact-emission-request [])]
    (doseq [[function paths]
            [['w5-b13-provenance-links-valid?
              [[:compiler-provenance :source-id] [:source-debug-map :source-id]]]
             ['w5-b13-evidence-valid?
              [[:safety-proof-certificate-bundle :unexpected]
               [:safety-proof-certificate-bundle :artifact]]]]]
      (is (true? (invoke function [request])))
      (doseq [path paths]
        (is (false? (invoke function [(assoc-in request path :mismatch)])))))))
