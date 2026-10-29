(ns gravity.syntax.selected-git-configuration-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.syntax.selected-git-configuration :as config]))

(def current "refs/heads/codex/syntax-selected-revision-inputs")
(def opaque "[core]\n\tbare = false\n[credential]\n\thelper = private-secret\n")
(defn tracking [branch merge]
  (str "[branch \"" branch "\"]\n\tremote = origin\n\tmerge = " merge "\n"))
(defn candidate [s] (config/candidate (.getBytes ^String s "ISO-8859-1") current))

(deftest candidate-is-inactive-and-keeps-opaque-bytes
  (let [a (candidate opaque)
        b (candidate (str opaque (tracking "codex/other" "refs/heads/main")))
        c (candidate (str opaque (tracking "codex/other" "refs/heads/codex/other")))]
    (is (= (:opaque a) (:opaque b) (:opaque c)))
    (is (not= (:raw a) (:raw b) (:raw c)))
    (is (= [{:branch "codex/other" :remote "origin" :merge "refs/heads/main"}] (:excluded b)))
    (is (false? (:active-binding? b)))
    (is (= :unestablished (:native-configuration-coverage b)))
    (is (not (.contains (pr-str b) "private-secret")))
    (is (not= (:opaque a) (:opaque (candidate (str opaque "\n")))))
    (is (not= (:opaque a) (:opaque (candidate (.replace opaque "false" "true")))))))

(deftest unsupported-or-meaningful-inputs-remain-strict
  (doseq [block [(tracking "codex/syntax-selected-revision-inputs" "refs/heads/main")
                 (tracking "other" "refs/heads/main")
                 "[branch \"codex/other\"]\n\tremote = elsewhere\n\tmerge = refs/heads/main\n"
                 (str (tracking "codex/other" "refs/heads/main") "\trebase = true\n")
                 "[branch \"codex/other\"]\r\n\tremote = origin\r\n\tmerge = refs/heads/main\r\n"
                 "[branch \"codex/other\"]\n\tremote = origin\n\tmerge = refs/heads/main"]]
    (testing block
      (let [r (candidate (str opaque block))]
        (is (empty? (:excluded r)))
        (is (= (:raw r) (:opaque r))))))
  (doseq [extra ["[include]\n\tpath = private\n" "[IncludeIf \"onbranch:main\"]\n\tpath = private\n"
                 "[include.path]\n" "[core]\n\tvalue = continued\\\n"]]
    (let [r (candidate (str opaque extra (tracking "codex/other" "refs/heads/main")))]
      (is (= :strict (:mode r)))
      (is (= (:raw r) (:opaque r))))))

(deftest arbitrary-opaque-bytes-and-section-boundaries-survive
  (let [s (str opaque (char 255) "\n" (tracking "codex/other" "refs/heads/main") " [unknown]\nsecret\n")
        r (candidate s)]
    (is (= 1 (count (:excluded r))))
    (is (= (:opaque (candidate (str opaque (char 255) "\n [unknown]\nsecret\n"))) (:opaque r)))
    (is (not (.contains (pr-str r) "secret"))))
  (let [r (config/candidate (.getBytes (tracking "codex/other" "refs/heads/main") "ISO-8859-1") nil)]
    (is (= :strict (:mode r)))
    (is (= (:raw r) (:opaque r)))))

(deftest authenticated-delta-shapes-have-equal-candidate-projections
  ;; The historical receipt authenticates these exact edits, not a writer.
  ;; This fixture checks the transformation shapes; it is not the old config.
  (let [rv2 #(tracking "codex/rv2-owned-git-bookends" %)
        appended (tracking "codex/loop-accounting-cleanup-20261029"
                           "refs/heads/codex/loop-accounting-cleanup-20261029")
        later (str (tracking "codex/cold-quote-metadata-disposition-20261029"
                             "refs/heads/codex/cold-quote-metadata-disposition-20261029")
                   (tracking "codex/loop-accounting-main-20261029" "refs/heads/main"))
        a (str opaque (rv2 "refs/heads/main"))
        b (str a appended)
        c (str opaque (rv2 "refs/heads/codex/rv2-owned-git-bookends") appended)
        d (str c later)]
    (is (= 126 (count appended)))
    (is (= 24 (- (count c) (count b))))
    (is (= 231 (count later)))
    (is (apply = (map (comp :opaque candidate) [a b c d])))))
