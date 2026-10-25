(ns gravity.self-hosting.sh22-meta-metadata-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; STD15/C3 metadata preservation in the existing bounded stable-view API.
;; The coarse test projection is not a complete/authenticated C3 boundary.
(def ^:private root
  (delay (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_meta_metadata_test.clj")))]
           (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative) emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(def ^:private syntax-engine (delay (compile-plan "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-meta-metadata." extension))
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
  (invoke (get @fixtures ["accepted" extension]) 'sh22-meta-metadata-request []))
(defn- with-metadata [input metadata]
  (assoc-in input [:arguments 0 :metadata] metadata))

(defn- c3-syntax [metadata]
  (let [descriptor (assoc (edn/read-string (slurp (path "bootstrap/clojure/fixtures/self-hosting/sh-04/accepted/template-descriptor.gravity")))
                          :metadata metadata)
        raw (invoke @syntax-engine 'c3-syntax-build-template [descriptor])
        requests (:digest-requests raw)
        resolved (reduce (fn [out request]
                           (conj out (b/p15-s23-c6c10-canonical-digest
                                      "<sh22-c3-meta>"
                                      (b/p15-s23-c6c10-resolve-digest-references!
                                       "<sh22-c3-meta>" (:preimage request) (count requests) (:ordinal request) out))))
                         [] requests)
        syntax (b/p15-s23-c6c10-resolve-digest-references!
                "<sh22-c3-meta>" (:syntax-template raw) (count requests) nil resolved)
        verification (invoke @syntax-engine 'c3-syntax-verify-resolved
                             [syntax requests resolved (:reader-binding raw) (:reader-source-revision raw)])]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status verification)))
    syntax))

(deftest sh22-meta-view-preserves-metadata-and-legacy-shape
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity"))) (slurp (path (fixture-path family "qst"))))))
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension)
          full (into {} (map #(vector (keyword (str "m" %)) %) (range 64)))]
      (doseq [metadata [{} {:doc "compiler syntax"} {:flag false :absent nil}
                        {'compiler/tag 'SyntaxValue :nested [{:value false} '(throw :inert)]} full]]
        (let [input (with-metadata base metadata) result (handle input)
              original (get-in input [:arguments 0])]
          (is (= :accepted (:status result)))
          (is (= metadata (get-in result [:value :metadata])))
          (is (= original (dissoc (:value result) :artifact)))
          (is (= :stable (get-in result [:facts :public-ir-view])))
          (is (true? (get-in result [:facts :ordinary-recheck-required])))
          (is (verified? input result))))
      (let [legacy (update-in base [:arguments 0] dissoc :metadata) result (handle legacy)]
        (is (= :accepted (:status result)))
        (is (not (contains? (:value result) :metadata)))
        (is (= (get-in legacy [:arguments 0]) (dissoc (:value result) :artifact)))
        (is (verified? legacy result))))))

(deftest sh22-meta-view-consumes-real-c3-metadata-with-preserved-context
  (doseq [metadata [{} {:doc "compiler source" :tag 'SyntaxValue} {'compiler/flag false :none nil}]]
    (let [syntax (c3-syntax metadata) span (get-in syntax [:span :primary])
          source-id (get-in syntax [:source :source-id])
          projection {:syntax-id (:syntax-id syntax) :value (get-in syntax [:form :value])
                      :source-span {:source-id source-id :start (:byte-start span) :end (:byte-end span)}
                      :hygiene-marks (get-in syntax [:hygiene :marks]) :namespace-context (:namespace syntax)
                      :phase (:phase syntax) :origin-chain [{:kind :source :source-id source-id}]
                      :metadata (:metadata syntax)}]
      (doseq [extension ["gravity" "qst"]]
        (let [input (assoc (base-request extension) :arguments [projection]) result (handle input)]
          (is (= :accepted (:status result)))
          (is (= projection (dissoc (:value result) :artifact)))
          (is (= metadata (get-in result [:value :metadata])))
          (is (= metadata (:metadata syntax)))
          (is (verified? input result)))))))

(deftest sh22-meta-view-rejects-invalid-metadata-before-view-construction
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension)
          invalid [nil false [] "metadata" {"doc" "wrong key"} {0 :wrong-key}
                   (into {} (map #(vector (keyword (str "m" %)) %) (range 65)))]]
      (doseq [metadata invalid]
        (let [input (with-metadata base metadata) result (handle input)]
          (is (= :rejected (:status result)))
          (is (= "STD15001" (get-in result [:diagnostics 0 :rule])))
          (is (= :invalid-metadata (get-in result [:diagnostics 0 :reason])))
          (is (not (contains? result :value)))
          (is (verified? input result))))
      (doseq [function ['sh22-meta-metadata-nil-request 'sh22-meta-metadata-vector-request 'sh22-meta-metadata-key-request]]
        (let [input (invoke (get @fixtures ["rejected" extension]) function [base]) result (handle input)]
          (is (= :rejected (:status result)))
          (is (= :invalid-metadata (get-in result [:diagnostics 0 :reason])))
          (is (verified? input result)))))))

(deftest sh22-meta-view-metadata-authority-and-substitution-fail-closed
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension)]
      (doseq [[input rule reason]
              [[(assoc base :effects #{}) "STD1002" :undeclared-library-effect]
               [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
               [(assoc base :allocation-policy :none) "STD1001" :allocation-policy-mismatch]
               [(assoc base :arguments []) "STD1001" :invalid-arity]
               [(assoc-in base [:arguments 0 :extra] true) "STD15001" :syntax-fields-invalid]
               [(update-in base [:arguments 0] dissoc :origin-chain) "STD15001" :syntax-fields-invalid]]]
        (let [result (handle input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (verified? input result))))
      (let [result (handle base)]
        (doseq [changed [(update result :value dissoc :metadata)
                         (assoc-in result [:value :metadata] {})
                         (assoc-in result [:identity-input :arguments 0 :metadata] {})
                         (assoc-in result [:facts :ordinary-recheck-required] false)
                         (assoc-in result [:provenance :source-span :end] 65)]]
          (let [report (call 'sh22-verify-result [base changed])]
            (is (= :rejected (:status report)))
            (is (= :library-result-substitution (get-in report [:diagnostics 0 :reason])))))))))
