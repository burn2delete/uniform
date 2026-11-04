(ns ci.lint-context-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]])
  (:import [java.util.zip ZipEntry ZipOutputStream]))

(when-not (find-ns 'ci.lint-context)
  (load-file "tools/ci/lint_context.clj"))
(require '[ci.lint-context :as context])

(defn utf8 [text]
  (.getBytes ^String text "UTF-8"))

(def old-path (str context/source-root "gravity/bootstrap_impl/old.clj"))
(def new-path (str context/source-root "gravity/bootstrap_impl/new.clj"))
(defn manifest-bytes [paths]
  (utf8 (pr-str (mapv #(hash-map :resource (subs % (count context/source-root))) paths))))

(def bootstrap-base
  {context/bootstrap (context/local-bytes! context/bootstrap)
   context/loader (context/local-bytes! context/loader)
   context/manifest (manifest-bytes [old-path])
   old-path (utf8 "(defn carried [value] value)\n")})

(def bootstrap-current
  (assoc bootstrap-base
         context/manifest (manifest-bytes [old-path new-path])
         new-path (utf8 "(defn added [value] value)\n")))

(defn reader-for [data]
  (fn [path]
    (or (get data path)
        (throw (ex-info "Missing fixture resource" {:path path})))))

(defn with-archive [data modes selected f]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-lint-base-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        archive (io/file directory "source.zip")
        inventory (io/file directory "inventory")]
    (try
      (with-open [zip (ZipOutputStream. (io/output-stream archive))]
        (doseq [[path bytes] (sort-by key data)]
          (.putNextEntry zip (ZipEntry. path))
          (.write zip ^bytes bytes)
          (.closeEntry zip)))
      (spit inventory
            (apply str
                   (for [path (sort (keys data))]
                     (str (get modes path "100644") " blob "
                          (apply str (repeat 40 "0")) "\t" path "\u0000"))))
      (f (context/archived-plan! archive inventory selected))
      (finally
        (doseq [file (reverse (file-seq directory))]
          (.delete file))))))

(defn owner-cases []
  [{:owner context/facade
    :prefix context/facade-prefix
    :plan (context/facade-plan! context/local-bytes!)
    :fragment (str context/facade-prefix "04-envelope-valid.clj")}
   {:owner context/checker
    :prefix context/checker-prefix
    :plan (context/checker-plan! context/local-bytes!)
    :fragment (str context/checker-prefix "02-source-product.clj")}
   {:owner context/c2-pass-cache
    :prefix context/c2-pass-cache-prefix
    :plan (context/c2-pass-cache-plan! context/local-bytes!)
    :fragment (str context/c2-pass-cache-prefix "source_snapshot.clj")}])

(deftest archived-bootstrap-selection-keeps-owning-plan
  (let [loader-sha (context/sha256 (get bootstrap-current context/loader))]
    (with-archive bootstrap-base {} [context/manifest new-path]
      (fn [plan]
        (is (= [context/bootstrap old-path] (:paths plan)))
        (is (= (set (keys bootstrap-base)) (set (keys (:inputs plan)))))
        (is (not (contains? (:inputs plan) new-path)))))
    (is (= [context/bootstrap old-path new-path]
           (:paths (context/selected-plan! (reader-for bootstrap-current)
                                           [context/manifest new-path]
                                           loader-sha))))))

(deftest archived-current-owner-prefixes-include-resources
  (doseq [{:keys [owner prefix plan fragment]} (owner-cases)]
    (let [missing (str prefix "missing-current.clj")
          archive-inputs (assoc (:inputs plan)
                                context/loader
                                (context/local-bytes! context/loader))]
      (with-archive archive-inputs {} [fragment missing]
        (fn [archived]
          (is (= (:paths plan) (:paths archived)) (str owner))
          (is (contains? (set (:paths archived)) owner))
          (is (not (contains? (set (:paths archived)) missing)))))
      (is (= (:paths plan)
             (:paths (context/selected-plan! context/local-bytes!
                                             [fragment]
                                             (context/sha256 (context/local-bytes! context/loader)))))))))

(deftest companion-load-manifests-select-context-without-becoming-source-fragments
  (let [loader-sha (context/sha256 (get bootstrap-current context/loader))
        read-bytes (reader-for bootstrap-current)]
    (doseq [manifest [context/semantic-mid-manifest context/semantic-late-manifest]]
      (let [plan (context/selected-plan! read-bytes [manifest] loader-sha)]
        (is (context/whole-context-path? manifest))
        (is (= [context/bootstrap old-path new-path] (:paths plan)))
        (is (not (contains? (:namespace-by-path plan) manifest)))))
    (doseq [unlisted [(str context/source-root "gravity/bootstrap_impl/unlisted.clj")
                     (str context/source-root "gravity/bootstrap_impl/unlisted.edn")]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Changed fragment is absent"
                           (context/selected-plan! read-bytes [unlisted] loader-sha))))))

(deftest archived-loader-and-prefix-boundaries-fail-closed
  (let [changed-loader (assoc bootstrap-base context/loader (utf8 "(def changed-loader true)\n"))
        loader-error (atom nil)]
    (try
      (with-archive changed-loader {} [old-path] identity)
      (catch clojure.lang.ExceptionInfo error
        (reset! loader-error error)))
    (is (= "Bootstrap loader changed; lint context requires independent review"
           (.getMessage @loader-error)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (with-archive bootstrap-base {old-path "120000"} [old-path] identity)))
    (doseq [path [(str context/facade-prefix "unregistered.clj")
                  (str context/checker-prefix "unregistered.clj")
                  (str context/c2-pass-cache-prefix "unregistered.clj")]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (context/selected-plan! context/local-bytes! [path]))))))
