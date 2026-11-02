(ns gravity.self-hosting.sh01-clojure-loc-test
  "Focused tests for the incremental Clojure source LOC guardrail."
  (:require [clojure.test :refer [deftest is testing]])
  (:import (java.nio.file Files Path)
           (java.nio.file.attribute FileAttribute)))

(System/setProperty "gravity.clojure-source-loc.library" "true")
(load-file "tools/validate_clojure_loc.clj")
(alias 'loc 'gravity.clojure-source-loc)

(def ^:private baseline
  {:schema :gravity/clojure-source-loc-baseline-v1
   :maximum-physical-lines 250
   :source-roots ["src"]
   :extensions [".clj" ".cljc"]
   :oversized-files {"src/legacy.clj" 251}})

(defn- temp-directory []
  (Files/createTempDirectory "gravity-clojure-loc-"
                             (make-array FileAttribute 0)))

(defn- delete-tree! [^Path root]
  (when (Files/exists root (make-array java.nio.file.LinkOption 0))
    (doseq [path (reverse (file-seq (.toFile root)))]
      (Files/deleteIfExists (.toPath ^java.io.File path)))))

(defmacro with-tree [[binding] & body]
  `(let [~binding (temp-directory)]
     (try
       ~@body
       (finally
         (delete-tree! ~binding)))))

(defn- write-lines! [^Path root relative count]
  (let [file (.resolve root relative)]
    (Files/createDirectories (.getParent file) (make-array FileAttribute 0))
    (spit (.toFile file) (apply str (repeat count "(def value 1)\n")))
    file))

(defn- violation-ids [report]
  (mapv :id (:violations report)))

(deftest canonical-baseline-covers-current-oversized-source-files
  (let [report (loc/validate-repository)
        configured (loc/read-baseline ".")]
    (is (= :passed (:status report)) (pr-str report))
    (is (<= (:baseline-count report) (:source-file-count report)))
    (is (= (count (:oversized-files configured)) (:baseline-count report)))
    (is (empty? (:violations report)))))

(deftest compliant-and-shrinking-baselined-files-pass
  (with-tree [root]
    (write-lines! root "src/ok.clj" 250)
    (write-lines! root "src/legacy.clj" 251)
    (is (= :passed (:status (loc/validate-tree root baseline))))))

(deftest new-oversized-source-files-are-rejected
  (with-tree [root]
    (write-lines! root "src/legacy.clj" 251)
    (write-lines! root "src/new.clj" 251)
    (let [report (loc/validate-tree root baseline)]
      (is (= :failed (:status report)))
      (is (= ["LOC003"] (violation-ids report)))
      (is (= "src/new.clj" (get-in report [:violations 0 :path]))))))

(deftest baselined-files-cannot-grow-or-remain-exceptions-after-compliance
  (testing "growth"
    (with-tree [root]
      (write-lines! root "src/legacy.clj" 252)
      (is (= ["LOC005"]
             (violation-ids (loc/validate-tree root baseline))))))
  (testing "stale exception"
    (with-tree [root]
      (write-lines! root "src/legacy.clj" 250)
      (is (= ["LOC004"]
             (violation-ids (loc/validate-tree root baseline)))))))

(deftest malformed-and-missing-baseline-inputs-fail-closed
  (with-tree [root]
    (write-lines! root "src/legacy.clj" 251)
    (is (= ["LOC001"]
           (violation-ids
            (loc/validate-tree root (assoc baseline :unexpected true)))))
    (is (= ["LOC002"]
           (violation-ids
            (loc/validate-tree root (assoc-in baseline [:oversized-files "src/missing.clj"] 251)))))))
