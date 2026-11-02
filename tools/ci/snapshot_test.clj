(ns ci.snapshot-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(binding [*command-line-args* ["test-load"]]
  (load-file "tools/ci/snapshot.clj"))
(require '[ci.snapshot :as snapshot])

(defn git! [directory & arguments]
  (let [result (apply sh/sh (concat ["git"] arguments [:dir (str directory)]))]
    (when-not (zero? (:exit result))
      (throw (ex-info "Snapshot fixture Git operation failed" result)))
    (str/trim (:out result))))

(defn with-fixture-repo [f]
  (let [directory (.toFile (Files/createTempDirectory
                            "uniform-snapshot-recipe-"
                            (make-array FileAttribute 0)))]
    (try
      (f directory)
      (finally
        (doseq [file (reverse (file-seq directory))]
          (io/delete-file file true))))))

(deftest exact-commit-archive-recipe-is-repeatable
  (with-fixture-repo
    (fn [directory]
      (git! directory "init" "--quiet")
      (git! directory "config" "user.name" "Snapshot fixture")
      (git! directory "config" "user.email" "snapshot@example.invalid")
      (spit (io/file directory "source.gravity") "(defn fixture [] 1)\n")
      (git! directory "add" "source.gravity")
      (git! directory "commit" "--quiet" "-m" "Snapshot fixture")
      (let [sha (git! directory "rev-parse" "HEAD")
            first-archive (io/file directory "first.tar.gz")
            second-archive (io/file directory "second.tar.gz")
            run-archive! (fn [archive]
                           (let [result (apply sh/sh
                                               (concat (snapshot/archive-command sha archive)
                                                       [:dir (str directory)]))]
                             (is (zero? (:exit result)) (:err result))
                             result))]
        (run-archive! first-archive)
        (run-archive! second-archive)
        (is (= (snapshot/sha256 first-archive)
               (snapshot/sha256 second-archive)))))))

(let [result (run-tests 'ci.snapshot-test)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
