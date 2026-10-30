(ns ci.lint-archive-test
  "Non-evaluating archive controls for newly registered Bootstrap fragments."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is run-tests]]))
(load-file "tools/ci/lint_context.clj")
(require '[ci.lint-context :as context])
(def old-fragment (str context/source-root "gravity/bootstrap_impl/old.clj"))
(def new-fragment (str context/source-root "gravity/bootstrap_impl/added.clj"))
(defn utf8 [s] (.getBytes ^String s "UTF-8"))
(def base-inputs
  {context/bootstrap (context/local-bytes! context/bootstrap)
   context/loader (context/local-bytes! context/loader)
   context/manifest (utf8 "[{:resource \"gravity/bootstrap_impl/old.clj\"}]")
   old-fragment (utf8 "(def old-value 1)\n")})
(defn read-inputs [inputs]
  (fn [p] (or (get inputs p) (throw (ex-info "Missing model input" {:path p})))))
(defn with-archive [inputs f]
  (let [dir (.toFile (java.nio.file.Files/createTempDirectory "lint-base-archive-" (make-array java.nio.file.attribute.FileAttribute 0)))
        archive (io/file dir "base.zip") inventory (io/file dir "inventory")]
    (try
      (with-open [out (java.util.zip.ZipOutputStream. (io/output-stream archive))]
        (doseq [[p bytes] inputs]
          (.putNextEntry out (java.util.zip.ZipEntry. p))
          (.write out ^bytes bytes) (.closeEntry out)))
      (spit inventory (apply str (for [p (keys inputs)] (str "100644 blob " (apply str (repeat 40 "0")) "\t" p "\u0000"))))
      (f archive inventory)
      (finally (doseq [file [inventory archive dir]] (.delete file))))))
(deftest added-fragment-does-not-become-a-required-baseline-file
  (with-archive base-inputs
    (fn [archive inventory]
      (let [base (context/archived-plan! archive inventory [context/bootstrap old-fragment new-fragment])]
        (is (some #{old-fragment} (:paths base)))
        (is (not-any? #{new-fragment} (:paths base)))
        (is (= (seq (get base-inputs old-fragment)) (seq (get-in base [:inputs old-fragment])))))
      (is (empty? (:paths (context/archived-plan! archive inventory [new-fragment])))))))
(deftest current-head-and-retained-base-dependencies-stay-strict
  (let [head (assoc base-inputs context/manifest
                    (utf8 "[{:resource \"gravity/bootstrap_impl/old.clj\"} {:resource \"gravity/bootstrap_impl/added.clj\"}]")
                    new-fragment (utf8 "(def added-value old-value)\n"))]
    (is (some #{new-fragment} (:paths (context/selected-plan! (read-inputs head) [new-fragment]))))
    (is (thrown? Exception (context/selected-plan! (read-inputs base-inputs) [new-fragment])))
    (is (thrown? Exception (context/selected-plan! (read-inputs (dissoc head new-fragment)) [new-fragment]))))
  (with-archive (dissoc base-inputs old-fragment)
    (fn [archive inventory]
      (is (thrown? Exception (context/archived-plan! archive inventory [context/bootstrap new-fragment])))))
  (with-archive (assoc base-inputs old-fragment (utf8 "(def broken ["))
    (fn [archive inventory]
      (is (thrown? Exception (context/archived-plan! archive inventory [context/bootstrap new-fragment]))))))
(deftest bootstrap-is-not-loaded
  (is (nil? (find-ns 'gravity.bootstrap))))
(let [r (run-tests)]
  (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))
