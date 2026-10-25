(ns gravity.self-hosting.sh03-unmetered-reader-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
(def ^:private current-plan
  (delay
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
     source-path (slurp source-path))))

(defn- execute [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh03-unmetered-current-source-test :compiler-artifact-plan? true}
   @current-plan function arguments))

(defn- input
  ([text] (input text "src/unmetered.gravity" "/declared/unmetered.gravity"))
  ([text retain-trivia] (assoc-in (input text) [2 :retain-trivia] retain-trivia))
  ([text logical path]
   (let [bytes (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)]
     [{:artifact :gravity/sh03-source-unit :schema-version 1
       :project-root-id (str "sha256:" (apply str (repeat 64 "0")))
       :logical-source-id logical
       :source-content-hash (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
       :source-byte-count (alength bytes) :encoding :utf-8
       :actual-path-provenance {:path path :extension ".gravity" :source-kind :gravity}}
      (mapv #(bit-and 255 %) bytes)
      {:artifact :gravity/sh03-reader-policy :schema-version 1
       :retain-trivia true :enabled-reader-tags [[105 110 115 116] [117 117 105 100]]}])))

(deftest unmetered-reader-preserves-products-and-fresh-tamper-rejection
  (let [arguments (input "; comment\n[1 :a \"\u00e9\ud83d\ude00\" 2/3] ^:private x")
        result (execute 'sh03-read-source-unit arguments)]
    (is (= :accepted (:status result)))
    (is (= {} (:bounds result)))
    (is (not (contains? (:semantic-reader-template result) :semantic-work-units)))
    (is (= 6 (count (:digest-requests result))))
    (is (= :accepted (:status (execute 'sh03-verify-reader-result (conj arguments result)))))
    (doseq [changed [(assoc result :bounds {:work-allowance 1})
                     (assoc-in result [:token-stream 0 :kind] :fabricated)
                     (assoc-in result [:source-unit :bytes-hash]
                               (str "sha256:" (apply str (repeat 64 "f"))))]]
      (is (= :rejected (:status (execute 'sh03-verify-reader-result
                                         (conj arguments changed))))))))

(deftest unmetered-reader-accepts-long-logical-and-provenance-paths
  (let [logical (str (apply str (repeat 2048 "a")) ".gravity")
        path (str "/" (apply str (repeat 4097 "p")) ".gravity")
        result (execute 'sh03-read-source-unit (input "[1]" logical path))]
    (is (= :accepted (:status result)))
    (is (= path (get-in result [:actual-path-provenance :path])))
    (is (= logical (get-in result [:source-unit :logical-source-id])))))

(deftest numeric-normalization-is-not-deferred-by-spelling-cost
  (let [spelling (apply str (repeat 257 "9"))
        result (execute 'sh03-read-source-unit (input spelling))
        descriptors (map :descriptor (:semantic-value-table result))]
    (is (= :accepted (:status result)))
    (is (= [:integer] (mapv :kind descriptors)))
    (is (not-any? #(= :reader-semantic-work-boundary (:normalization-reason %))
                  descriptors))
    (is (empty? (get-in result [:semantic-error-deferment-record :deferred-literal-records])))))

(deftest grammar-encoding-and-byte-identity-checks-remain
  (doseq [[text rule] [["[1)" "C2-DELIMITER"] ["{:a}" "C2-MAP"]
                      ["#{1 1}" "C2-SET"] ["1e+" "C2-NUMERIC"]
                      ["^1 x" "C2-METADATA"] ["#unknown 1" "C2-EXTENSION"]]]
    (testing text
      (let [result (execute 'sh03-read-source-unit (input text))]
        (is (= :rejected (:status result)))
        (is (= rule (get-in result [:diagnostics 0 :id]))))))
  (let [[unit _ policy] (input "a")
        malformed (execute 'sh03-read-source-unit [unit [128] policy])
        wrong-count (execute 'sh03-read-source-unit [(assoc unit :source-byte-count 2) [97] policy])]
    (is (= :rejected (:status malformed)))
    (is (= "C2-ENCODING" (get-in malformed [:diagnostics 0 :id])))
    (is (= :rejected (:status wrong-count)))))

(deftest source-file-snapshot-has-no-four-megabyte-project-quota
  (let [path (java.nio.file.Files/createTempFile
              "reader-unmetered-source-" ".gravity"
              (make-array java.nio.file.attribute.FileAttribute 0))
        bytes (byte-array 4194305)]
    (try
      (java.nio.file.Files/write path bytes (make-array java.nio.file.OpenOption 0))
      (is (java.util.Arrays/equals
           bytes (bootstrap/sh03-reader-read-target-source-bytes! (str path))))
      (finally (java.nio.file.Files/deleteIfExists path)))))
