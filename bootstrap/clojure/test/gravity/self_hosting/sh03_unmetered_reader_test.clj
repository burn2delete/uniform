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

(deftest envelope-selection-comes-from-the-host-invocation
  (let [path "src/invocation-context.gravity"
        bytes (.getBytes "[1]" java.nio.charset.StandardCharsets/UTF_8)
        project (bootstrap/reader-project-context-for-source path)
        relative bootstrap/sh03-reader-source-relative-path
        explicit {:reader-source-path relative
                  :authenticated-envelope-options {:source :development-current}}
        selected (bootstrap/sh03-reader-resolved-result!
                  path bytes project bootstrap/standard-reader-options relative)
        absolute (.getCanonicalPath (java.io.File. relative))
        selected-absolute (bootstrap/sh03-reader-resolved-result!
                           path bytes project bootstrap/standard-reader-options absolute)
        default (bootstrap/sh03-reader-resolved-result!
                 path bytes project bootstrap/standard-reader-options)
        options (deref (ns-resolve 'gravity.bootstrap 'sh03-reader-authenticated-envelope-options))
        failure (fn [context]
                  (try (options path context) nil
                       (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (is (= (assoc explicit :reader-source-path absolute) (:invocation-context selected)))
    (is (= (assoc explicit :reader-source-path absolute) (:invocation-context selected-absolute)))
    (is (= absolute (get-in selected [:plan-binding :source-path])
                    (get-in selected-absolute [:plan-binding :source-path])))
    (is (= {:source :development-current}
           (options path (assoc explicit :reader-source-path absolute))))
    (is (nil? (:invocation-context default)))
    (is (= {:source :development-current} (options path explicit)))
    (is (nil? (options path nil)))
    (doseq [invalid [{:artifact :gravity/sh03-current-source-reader-plan-binding}
                     (assoc explicit :extra true)
                     (assoc explicit :reader-source-path "/other/reader.gravity")
                     (assoc explicit :authenticated-envelope-options {})
                     (assoc explicit :authenticated-envelope-options {:source :unknown})]]
      (is (= "C2-HASH" (:id (failure invalid)))))
    (let [calls (atom 0)
          foreign (proxy [java.util.HashMap] []
                    (entrySet [] (swap! calls inc) (throw (ex-info "foreign" {}))))]
      (is (= "C2-HASH" (:id (failure foreign))))
      (is (zero? @calls)))))

(deftest public-current-reader-reaches-the-real-envelope-verifier
  (let [path "src/public-current-context.gravity"
        text "(ns reader.current (:profile :core) (:target :jvm) (:effects #{}) (:capabilities #{})) (defn value [] 1)"
        reader (.getCanonicalPath (java.io.File. bootstrap/sh03-reader-source-relative-path))
        project (bootstrap/reader-project-context-for-source path)
        before (slurp reader :encoding "UTF-8")
        invocation {:reader-source-path reader
                    :authenticated-envelope-options {:source :development-current}}
        artifact (bootstrap/compiler-c2-reader-source-artifact path text project nil nil reader)
        envelope (get-in artifact [:gravity-reader-boundary :authenticated-envelope])
        authority (var-get (ns-resolve 'gravity.bootstrap 'sh03-reader-internal-product-authority))
        verifier (var-get (ns-resolve 'gravity.bootstrap 'sh03-c3-precomputed-c2-verify!))
        failure (fn [f] (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (is (= :gravity/stage0-c2-reader-document-artifact (:kind artifact)))
    (is (= 2 (get-in envelope [:sealed-artifact :schema-version])))
    (is (= :development-unadmitted (get-in envelope [:source-rule :admission-status])))
    (is (= artifact (verifier authority path artifact invocation)))
    (is (map? (failure #(verifier authority path artifact))))
    (is (map? (failure #(verifier authority path artifact
                                 (assoc invocation :authenticated-envelope-options {})))))
    (is (map? (failure #(verifier authority path artifact
                                 (assoc invocation :reader-source-path "other/reader.gravity")))))
    (is (map? (failure #(verifier authority path
                                 (assoc-in artifact [:gravity-reader-boundary :authenticated-envelope
                                                     :semantic-envelope-id] "sha256:changed")
                                 invocation))))
    (is (map? (failure #(bootstrap/compiler-c2-reader-source-artifact path text project))))
    ;; The selected source really changes between production and verification.
    ;; This isolated-test worktree's original bytes are restored even on error.
    (try
      (spit reader (str before "\n; invocation source changed\n") :encoding "UTF-8")
      (is (map? (failure #(verifier authority path artifact invocation))))
      (finally (spit reader before :encoding "UTF-8")))
    (is (= before (slurp reader :encoding "UTF-8")))
    (is (= artifact (verifier authority path artifact invocation)))))

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
