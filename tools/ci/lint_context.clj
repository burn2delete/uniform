(ns ci.lint-context
  "Non-evaluating lint view of the reviewed Bootstrap resource loader."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.security MessageDigest]
           [java.util.zip ZipFile]))

(def source-root "bootstrap/clojure/src/")
(def bootstrap (str source-root "gravity/bootstrap.clj"))
(def manifest (str source-root "gravity/bootstrap_impl/load_order.edn"))
(def loader (str source-root "gravity/bootstrap_impl/loader.clj"))
(def loader-sha "ac79eb7b09a0ad5cef2b67e92e8669e555ec26877caa50e2599a2b6ec200aee7")

(defn source-text! [path bytes]
  (let [text (String. ^bytes bytes java.nio.charset.StandardCharsets/UTF_8)]
    (when (re-find #"\r(?!\n)" text)
      (throw (ex-info "Unsupported lone CR in lint context source" {:path path})))
    text))

(defn source-forms! [path bytes]
  (binding [*read-eval* false *data-readers* {}
            *default-data-reader-fn* (fn [tag _] (throw (ex-info "Unsupported source tag" {:tag tag :path path})))]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                        (java.io.StringReader. (source-text! path bytes)))]
      (let [eof (Object.)]
        (loop [forms []]
          (let [form (read {:eof eof} reader)]
            (if (identical? eof form) forms (recur (conj forms form)))))))))

(defn manifest! [bytes]
  (with-open [reader (java.io.PushbackReader.
                      (java.io.StringReader. (String. ^bytes bytes java.nio.charset.StandardCharsets/UTF_8)))]
    (let [eof (Object.) result (edn/read {:eof eof :readers {}} reader)]
      (when-not (and (vector? result) (seq result) (every? map? result)
                     (identical? eof (edn/read {:eof eof :readers {}} reader)))
        (throw (ex-info "Malformed or trailing Bootstrap load manifest" {})))
      result)))

(defn bootstrap-shape! [bytes]
  (let [[header & tail :as forms] (source-forms! bootstrap bytes)]
    (when-not (and (= 3 (count forms)) (= '(ns gravity.bootstrap) (take 2 header))
                   (= '[(clojure.core/require 'gravity.bootstrap-impl.loader)
                        (gravity.bootstrap-impl.loader/load-components! "gravity/bootstrap_impl/load_order.edn")]
                      tail))
      (throw (ex-info "Bootstrap entrypoint changed; unsupported lint assembly recipe" {})))))

(defn sha256 [bytes]
  (format "%064x" (java.math.BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") bytes))))

(defn resource-path! [resource]
  (when-not (and (string? resource) (str/starts-with? resource "gravity/bootstrap_impl/")
                 (str/ends-with? resource ".clj")
                 (every? #(not (#{"" "." ".."} %)) (str/split resource #"/"))
                 (not (str/includes? resource "\\")))
    (throw (ex-info "Unsupported Bootstrap resource path" {:resource resource})))
  (str source-root resource))

(defn local-bytes! [path]
  (loop [file (io/file path)]
    (when file
      (when (java.nio.file.Files/isSymbolicLink (.toPath file))
        (throw (ex-info "Lint context cannot follow symbolic links" {:path path})))
      (recur (.getParentFile file))))
  (java.nio.file.Files/readAllBytes (.toPath (io/file path))))

(defn plan! [read-bytes]
  (let [loader-bytes (read-bytes loader)
        _ (when-not (= loader-sha (sha256 loader-bytes))
            (throw (ex-info "Bootstrap loader changed; lint context requires independent review" {})))
        _ (bootstrap-shape! (read-bytes bootstrap))
        entries (manifest! (read-bytes manifest))
        paths (into [bootstrap] (map #(resource-path! (:resource %)) entries))]
    (when-not (= (count paths) (count (distinct paths)))
      (throw (ex-info "Duplicate Bootstrap resource in lint context" {})))
    {:paths paths :inputs (into {} (for [path (conj paths loader manifest)
                                         :let [bytes (read-bytes path)
                                               _ (when (str/ends-with? path ".clj") (source-forms! path bytes))]]
                                     [path bytes]))}))

(defn assemble! [directory {:keys [paths inputs]}]
  (let [target (io/file directory "gravity/bootstrap.clj")
        spans (atom [])
        row (atom 1)]
    (io/make-parents target)
    (with-open [writer (io/writer target)]
      (doseq [path paths]
        (when-not (= bootstrap path)
          ;; load-component! binds *ns* afresh for EVERY resource.
          (.write writer "(in-ns 'gravity.bootstrap)\n")
          (swap! row inc))
        (let [text (source-text! path (get inputs path))
              lines (str/split text #"\r?\n" -1)
              start @row]
          (.write writer text)
          (.write writer "\n")
          (swap! row + (count lines))
          (swap! spans conj {:path path :start start :end @row :lines lines}))))
    (let [result {:file (.getPath target)
                  :sha256 (sha256 (java.nio.file.Files/readAllBytes (.toPath target))) :spans @spans
                  :inputs (into (sorted-map) (for [[path bytes] inputs]
                                               [path {:sha256 (sha256 bytes) :bytes (alength ^bytes bytes)}]))}]
      (spit (io/file directory "source-map.edn") (str (pr-str result) "\n"))
      result)))

(defn remap! [{:keys [spans]} finding]
  (let [{:keys [row end-row col end-col]} finding
        span (some #(when (and (pos-int? row) (<= (:start %) row) (< row (:end %))) %) spans)]
    (when-not (and span (every? pos-int? [row end-row col end-col])
                   (<= row end-row) (< end-row (:end span))
                   (or (< row end-row) (<= col end-col))
                   (<= col (inc (count (nth (:lines span) (- row (:start span))))))
                   (<= end-col (inc (count (nth (:lines span) (- end-row (:start span)))))))
      (throw (ex-info "Lint finding has no exact single-source context span" {:finding finding})))
    (assoc finding :filename (:path span)
           :row (inc (- row (:start span))) :end-row (inc (- end-row (:start span))))))

(defn archived-plan! [archive inventory]
  (let [modes (into {} (for [entry (remove str/blank? (str/split (slurp inventory) #"\u0000"))
                             :let [[header path] (str/split entry #"\t" 2)]]
                         [path (first (str/split header #" "))]))]
    (with-open [zip (ZipFile. (io/file archive))]
      (plan! (fn [path]
               (when-not (#{"100644" "100755"} (get modes path))
                 (throw (ex-info "Missing or nonregular base context source" {:path path})))
               (let [entry (.getEntry zip path)]
                 (when-not entry (throw (ex-info "Missing archived context source" {:path path})))
                 (with-open [input (.getInputStream zip entry)] (.readAllBytes input))))))))
