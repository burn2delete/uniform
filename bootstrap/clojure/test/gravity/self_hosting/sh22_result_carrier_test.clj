(ns gravity.self-hosting.sh22-result-carrier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; Results retain request identity and must pass fresh semantic verification.
(def ^:private root
  (delay (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_result_carrier_test.clj")))]
           (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative) emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-result-carrier." extension))
(def ^:private fixtures
  (delay (into {} (for [family ["accepted" "rejected"] extension ["gravity" "qst"]]
                   [[family extension] (compile-plan (fixture-path family extension))]))))
(defn- invoke [plan function arguments]
  (b/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-standard-library-leaf :compiler-artifact-plan? true} plan function arguments))
(defn- call [function arguments] (invoke @engine function arguments))
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result] (= :passed (:status (call 'sh22-verify-result [input result]))))
(defn- base-request [extension]
  (invoke (get @fixtures ["accepted" extension]) 'sh22-result-carrier-request []))
(defn- deep-request [extension depth]
  (invoke (get @fixtures ["accepted" extension]) 'sh22-result-carrier-depth-request [depth]))

(deftest sh22-result-carrier-accepts-deep-and-large-semantic-values
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension)]
      (doseq [input [(deep-request extension 33)
                     (assoc base :arguments [[] (apply str (repeat 65537 "a"))])]]
        (let [result (handle input)]
          (is (true? (call 'sh22-request-common-valid? [input])))
          (is (= :accepted (:status result)))
          (is (= [(get-in input [:arguments 1])] (:value result)))
          (is (= {:status :accepted} (call 'sh22-carrier-preflight [result])))
          (is (verified? input result))
          (is (not (verified? input (assoc result :value [:forged])))))))))

(deftest sh22-rejection-preserves-large-identities-and-provenance
  (doseq [extension ["gravity" "qst"]
          id [(nth (iterate vector :id) 33) (apply str (repeat 65537 "i")) :ordinary false 0 [] {}]]
    (let [input (assoc (base-request extension) :request-id id :arguments [7 :leaf])
          result (handle input)]
      (is (true? (call 'sh22-request-common-valid? [input])))
      (is (= :rejected (:status result)))
      (is (= id (:request-id result) (get-in result [:diagnostics 0 :request-id])))
      (is (= "STD3002" (get-in result [:diagnostics 0 :rule])))
      (is (= :expected-vector (get-in result [:diagnostics 0 :reason])))
      (is (= (:source-span input) (get-in result [:diagnostics 0 :source-span])))
      (is (= (:origin-chain input) (get-in result [:diagnostics 0 :origin-chain])))
      (is (verified? input result))
      (is (not (verified? input (assoc-in result [:diagnostics 0 :reason] :forged)))))))

(deftest sh22-result-carrier-paired-producers-and-forgery-controls
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity"))) (slurp (path (fixture-path family "qst"))))))
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension) accepted (handle base)
          assoc-input (assoc base :operation :vector-assoc :arguments [(:value accepted) 0 :changed])
          associated (handle assoc-input)]
      (is (= :accepted (:status accepted) (:status associated)))
      (is (= [:changed] (:value associated)))
      (is (verified? base accepted))
      (is (verified? assoc-input associated))
      (doseq [candidate [(assoc accepted :value [:forged])
                         (assoc-in accepted [:facts :allocation] :none)
                         (assoc accepted :identity-input (call 'sh22-identity-input [(deep-request extension 33)]))]]
        (is (= :rejected (:status (call 'sh22-verify-result [base candidate]))))))))

(deftest sh22-carrier-validation-rejects-cycles-without-confusing-shared-aliases
  (let [physical (object-array [:self nil])
        cycle (clojure.lang.PersistentArrayMap. physical)
        shared [:leaf]]
    (aset physical 1 cycle)
    (is (= {:status :rejected :reason :cyclic-carrier}
           (call 'sh22-carrier-preflight [cycle])))
    (is (= {:status :rejected :reason :cyclic-carrier}
           (call 'sh22-carrier-preflight [[cycle]])))
    (is (= {:status :accepted} (call 'sh22-carrier-preflight [[shared shared]])))
    (doseq [extension ["gravity" "qst"]]
      (let [input (base-request extension)
            result (handle input)
            verification (call 'sh22-verify-result [input (assoc result :value cycle)])]
        (is (= :rejected (:status verification)))
        (is (= :invalid-verification-candidate (get-in verification [:diagnostics 0 :reason]))))))
  (let [calls (atom 0)
        deny #(do (swap! calls inc) (throw (AssertionError. "carrier callback")))
        key (proxy [Object] [] (hashCode [] (deny)) (equals [_] (deny)) (toString [] (deny)))
        map (clojure.lang.PersistentArrayMap. (object-array [key :inert]))
        backing (proxy [clojure.lang.APersistentVector] []
                  (count [] (deny)) (nth [_] (deny)) (assocN [_ _] (deny))
                  (cons [_] (deny)) (empty [] (deny)) (pop [] (deny)))
        view (clojure.lang.APersistentVector$SubVector. nil backing 0 1)]
    (is (= {:status :accepted} (call 'sh22-carrier-preflight [map])))
    (is (= {:status :rejected :reason :unsupported-native-carrier}
           (call 'sh22-carrier-preflight [view])))
    (doseq [array [nil (object-array [:dangling])]]
      (is (= {:status :rejected :reason :unsupported-native-carrier}
             (call 'sh22-carrier-preflight [(clojure.lang.PersistentArrayMap. array)]))))
    (is (zero? @calls))))
