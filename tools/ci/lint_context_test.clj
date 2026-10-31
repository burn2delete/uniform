(ns ci.lint-context-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]])
  (:import [java.util.zip ZipEntry ZipOutputStream]))

(binding [*command-line-args* nil] (load-file "tools/ci/lint.clj"))
(require '[ci.lint :as lint] '[ci.lint-context :as context])

(def old-path (str context/source-root "gravity/bootstrap_impl/old.clj"))
(def new-path (str context/source-root "gravity/bootstrap_impl/new.clj"))
(def stray-path (str context/source-root "gravity/bootstrap_impl/stray.clj"))
(defn utf8 [text] (.getBytes ^String text "UTF-8"))
(defn inputs [paths]
  (merge {context/bootstrap (utf8 "(ns gravity.bootstrap)\n(clojure.core/require 'gravity.bootstrap-impl.loader)\n(gravity.bootstrap-impl.loader/load-components! \"gravity/bootstrap_impl/load_order.edn\")\n")
          context/loader (context/local-bytes! context/loader)
          context/manifest (utf8 (pr-str (mapv (fn [path] {:resource (subs path (count context/source-root))}) paths)))}
         (into {} (for [path paths] [path (utf8 "(defn carried [value] (let [unused 1] value))\n")]))))
(defn reader-for [data]
  (fn [path] (or (get data path) (throw (ex-info "Missing fixture resource" {:path path})))))

(defn with-archive [data modes selected f]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-lint-base-" (make-array java.nio.file.attribute.FileAttribute 0)))
        archive (io/file directory "source.zip") inventory (io/file directory "inventory")]
    (try
      (with-open [zip (ZipOutputStream. (io/output-stream archive))]
        (doseq [[path bytes] (sort-by key data)]
          (.putNextEntry zip (ZipEntry. path))
          (.write zip ^bytes bytes)
          (.closeEntry zip)))
      (spit inventory (apply str (for [path (sort (keys data))]
                                   (str (get modes path "100644") " blob " (apply str (repeat 40 "0")) "\t" path "\u0000"))))
      (f (context/archived-plan! archive inventory selected))
      (finally (doseq [file (reverse (file-seq directory))] (.delete file))))))

(deftest added-fragment-keeps-both-owning-contexts
  (let [base (inputs [old-path]) current (inputs [old-path new-path])]
    (with-archive base {} [new-path]
      (fn [plan]
        (is (= [context/bootstrap old-path] (:paths plan)))
        (is (= (set (keys base)) (set (keys (:inputs plan)))))
        (is (not (contains? (:inputs plan) new-path)))))
    (is (= [context/bootstrap old-path new-path]
           (:paths (context/selected-plan! (reader-for current) [new-path]))))
    (is (thrown? Exception (context/selected-plan! (reader-for (assoc current context/manifest (get base context/manifest))) [new-path])))))

(deftest removed-and-renamed-fragments-keep-base-source-identity
  (let [base (inputs [old-path]) current (inputs [new-path])]
    (with-archive base {} [context/manifest new-path]
      (fn [plan]
        (is (= [context/bootstrap old-path] (:paths plan)))
        (is (= (seq (get base old-path)) (seq (get-in plan [:inputs old-path]))))))
    (with-archive base {} [context/manifest old-path]
      (fn [plan] (is (= [context/bootstrap old-path] (:paths plan)))))
    (is (= [context/bootstrap new-path]
           (:paths (context/selected-plan! (reader-for current) [context/manifest new-path]))))))

(deftest existing-paths-and-unsupported-base-contexts-still-refuse
  (let [base (inputs [old-path])]
    (is (thrown? Exception (with-archive (assoc base stray-path (utf8 "(def stray true)")) {} [stray-path] identity)))
    (is (thrown? Exception (with-archive base {old-path "120000"} [new-path] identity)))
    (doseq [changed [(assoc base context/loader (utf8 "changed loader"))
                     (assoc base context/bootstrap (utf8 "(ns different.namespace)"))
                     (assoc base context/manifest (utf8 "[{:resource \"gravity/bootstrap_impl/old.clj\"} {:resource \"gravity/bootstrap_impl/old.clj\"}]"))
                     (assoc base context/manifest (utf8 "[{:resource \"gravity/bootstrap_impl/../old.clj\"}]"))
                     (assoc base context/manifest (utf8 "[{:resource \"gravity/bootstrap_impl/old.clj\"}] []"))
                     (assoc base old-path (utf8 "(def malformed ["))]]
      (is (thrown? Exception (with-archive changed {} [new-path] identity)))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(defn findings [plan]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-lint-findings-" (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try
      (let [view (context/assemble! directory plan)
            result (sh/sh "clj-kondo" "--repro" "--cache" "false" "--config" "{:output {:format :edn}}" "--lint" "-"
                          :in (slurp (:file view)))
            raw (edn/read-string (:out result))]
        (is (#{0 2 3} (:exit result)))
        (mapv #(context/remap! view %) (:findings raw)))
      (finally (doseq [file (reverse (file-seq directory))] (.delete file))))))

(deftest new-source-is-linted-and-existing-diagnostics-are-preserved
  (let [base (inputs [old-path])
        current (assoc (inputs [old-path new-path]) new-path (utf8 "(def added (missing-function))\n"))]
    (with-archive base {} [context/manifest new-path]
      (fn [plan]
        (let [before (findings plan)
              after (findings (context/selected-plan! (reader-for current) [context/manifest new-path]))
              lines (str/split-lines (String. ^bytes (get base old-path) "UTF-8"))
              result (lint/classify before after {old-path {:before lines :after lines :changes []}})]
          (is (some #(and (= old-path (:filename %)) (= :unused-binding (:type %))) before))
          (is (= before (:baseline-warnings result)))
          (is (some #(and (= new-path (:filename %)) (= :unresolved-symbol (:type %))) (:blocking result)))
          (is (every? #(= new-path (:filename %)) (:blocking result)))
          (let [renamed (findings (context/selected-plan! (reader-for (inputs [new-path])) [new-path]))]
            (is (= renamed (:blocking (lint/classify before renamed {}))))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(defn -main [& _]
  (let [result (run-tests 'ci.lint-context-test)]
    (shutdown-agents)
    (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1))))
