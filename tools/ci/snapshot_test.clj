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

(deftest shallow-snapshot-keeps-ten-log-entries-and-exposes-history-pin-boundary
  (with-fixture-repo
    (fn [directory]
      (let [remote (io/file directory "remote")
            work (io/file directory "work")
            clone (io/file directory "clone")
            full (io/file directory "full")]
        (.mkdirs remote)
        (.mkdirs work)
        (git! remote "init" "--bare" "--quiet")
        (git! work "init" "--quiet")
        (git! work "config" "user.name" "Snapshot fixture")
        (git! work "config" "user.email" "snapshot@example.invalid")
        (git! work "remote" "add" "origin" (.getPath remote))
        (spit (io/file work "value.txt") "1\n")
        (git! work "add" "value.txt")
        (git! work "commit" "--quiet" "-m" "snapshot-1")
        (let [pinned (git! work "rev-parse" "HEAD")]
          (doseq [index (range 2 13)]
            (spit (io/file work "value.txt") (str index "\n"))
            (git! work "add" "value.txt")
            (git! work "commit" "--quiet" "-m" (str "snapshot-" index)))
          (git! work "branch" "-M" "main")
          (git! work "push" "--quiet" "origin" "main")
          (let [remote-uri (str "file://" (.getCanonicalPath remote))]
            (git! directory "clone" "--quiet" "--depth=10" remote-uri "clone")
            (is (= "true" (git! clone "rev-parse" "--is-shallow-repository")))
            (is (= (mapv #(str "snapshot-" %) (range 12 2 -1))
                   (str/split-lines (git! clone "log" "-10" "--format=%s"))))
            (let [archive (io/file directory "snapshot.tar.gz")
                  head (git! clone "rev-parse" "HEAD")
                  result (apply sh/sh (concat (snapshot/archive-command head archive)
                                              [:dir (str clone)]))
                  listing (sh/sh "tar" "-tzf" (str archive))]
              (is (zero? (:exit result)) (:err result))
              (is (.isFile archive))
              (is (zero? (:exit listing)) (:err listing))
              (is (some #(str/ends-with? % "/value.txt") (str/split-lines (:out listing))))
              (is (= "12" (git! clone "show" "HEAD:value.txt"))))
            ;; A pinned source commit outside the retained ten-entry history
            ;; is unavailable in the shallow snapshot checkout.
            (is (thrown? clojure.lang.ExceptionInfo
                         (git! clone "show" (str pinned ":value.txt"))))
            ;; The same pin remains materializable from a full checkout.
            (git! directory "clone" "--quiet" remote-uri "full")
            (is (= "1" (git! full "show" (str pinned ":value.txt"))))))))))

(let [result (run-tests 'ci.snapshot-test)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
