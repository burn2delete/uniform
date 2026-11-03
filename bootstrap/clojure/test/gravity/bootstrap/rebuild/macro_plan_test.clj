(ns gravity.bootstrap.rebuild.macro-plan-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.bootstrap.rebuild.macro-plan :as macro-plan])
  (:import [java.nio.file Files]))

(def ^:private macro-source
  "language/compiler/src/gravity/macro.gravity")

(def ^:private identity-keys
  [:source-path :source-byte-count :source-content-hash :source-mode
   :plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash :public-function-hashes
   :public-function-shapes])

(defn- temporary-root
  []
  (.toFile (Files/createTempDirectory
            "uniform-macro-plan-test-"
            (make-array java.nio.file.attribute.FileAttribute 0))))

(defn- delete-tree!
  [root]
  (doseq [path (reverse (file-seq root))]
    (when (.exists path)
      (Files/deleteIfExists (.toPath path)))))

(defn- current-payload
  []
  (let [component (macro-plan/development-macro-plan)
        binding (bootstrap/sh05-macro-build-binding! macro-source
                                                     :development-current)]
    {:schema (:schema component)
     :component-id (:component-id component)
     :source (:source component)
     :identities (select-keys binding identity-keys)
     :plan (:plan binding)}))

(defn- caught
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      error)))

(deftest persisted-plan-identities-reject-substitution
  (let [payload (current-payload)
        valid (macro-plan/validate-persisted-plan! payload)
        tampered (update-in payload [:plan :functions]
                            assoc
                            'tampered
                            {:arity 0 :params [] :instructions []})
        wrong-shape (assoc-in payload
                              [:plan :functions 'sh05-expand-macro-template
                               :params]
                              ['wrong-request])
        wrong-source (assoc-in payload
                               [:identities :source-content-hash]
                               "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
    (is (= payload valid))
    (is (= :plan-identities
           (:reason (ex-data (caught #(macro-plan/validate-persisted-plan!
                                       tampered)))))
        "a changed plan body cannot retain copied identity labels")
    (is (= :plan-shape
           (:reason (ex-data (caught #(macro-plan/validate-persisted-plan!
                                       wrong-shape)))))
        "the entry ABI is checked before execution")
    (is (= :source-plan-binding
           (:reason (ex-data (caught #(macro-plan/validate-persisted-plan!
                                       wrong-source)))))
        "the source identity is checked before plan execution")))

(deftest output-collision-does-not-overwrite
  (let [root (temporary-root)
        component (macro-plan/development-macro-plan)
        plan-file (io/file root (:output-plan-path component))]
    (try
      (io/make-parents plan-file)
      (spit plan-file "pre-existing-output")
      (let [error (caught #(macro-plan/run-development-macro-plan! "." root))]
        (is (= :output-exists (:reason (ex-data error))))
        (is (= "pre-existing-output" (slurp plan-file))))
      (finally
        (delete-tree! root)))))

(deftest child-reloads-plan-and-observes-template-and-rejection
  (let [root (temporary-root)]
    (try
      (let [result (macro-plan/run-development-macro-plan! "." root)
            record (:record result)
            accepted (:accepted record)
            rejected (:rejected record)]
        (is (= :development-current
               (get-in result [:component :source-mode])))
        (is (= :complete (get-in result [:producer-summary :status])))
        (is (= true (get-in record [:execution-child :loaded-plan?])))
        (is (= true (get-in record [:execution-child :fresh-c3-request?])))
        (is (= :clojure-stage0-seed
               (get-in record [:boundary :compiled-by])))
        (is (= :clojure-stage2-generic-rule-runner
               (get-in record [:boundary :executed-by])))
        (is (= :non-authoritative (get-in record [:boundary :authority])))
        (is (= :gravity/sh05-macro-template-result (:artifact accepted)))
        (is (= 2 (:digest-request-count accepted)))
        (is (= :accepted (:status accepted)))
        (is (= 'gravity.core/defn (:macro accepted)))
        (is (= :gravity/sh05-macro-template-result (:artifact rejected)))
        (is (= 1 (:digest-request-count rejected)))
        (is (= "C4-RETURN" (:diagnostic rejected)))
        (is (.isFile (io/file (:plan-path result))))
        (is (.isFile (io/file (:record-path result))))
        (is (seq (get-in accepted [:expanded-syntax-recipe :children]))))
      (finally
        (delete-tree! root)))))
