(ns task-projection-links
  "Document-relative task links; no CLI, task discovery or write operations."
  (:require [clojure.string :as str] [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")

(def link-pattern #"\[([^\[\]<>!\\\n]+)\]\(([^\s()<>\"`\\]+)\)")
(defn fail [component message] (task-projection/fail (str component " " message)))

(defn relative-target [component document target]
  ;; Pure lexical resolution; existence and symlink containment are separate.
  (let [uri (try (java.net.URI. target) (catch java.net.URISyntaxException _ nil))]
    (when-not (and uri (nil? (.getScheme uri)) (nil? (.getAuthority uri))
                   (nil? (.getFragment uri)) (nil? (.getQuery uri))
                   (not (str/starts-with? target "/"))
                   (not (str/includes? target "%")) (seq (.getPath uri)))
      (fail component "relative links must name repository files without fragments or queries"))
    (let [path (.normalize (.resolve (.getParent (.toPath (io/file document))) target))]
      (when (or (.isAbsolute path) (.startsWith path ".."))
        (fail component "link escapes repository"))
      path)))

(defn rebase-links [component field source destination]
  (str/replace field link-pattern
    (fn [[whole label target]]
      (if (str/starts-with? target "https:") whole
        (let [path (relative-target component source target)
              rebased (.relativize (.getParent (.toPath (io/file destination))) path)]
          (str "[" label "](" rebased ")"))))))

(defn validate-field [component task-path field]
  (task-projection/validate-links {:component component}
    (str/replace field link-pattern
      (fn [[whole _ target]]
        (if (str/starts-with? target "https:") whole
          (do (relative-target component task-path target) ""))))))

(defn validate-targets [component task-path rows]
  ;; Metadata checks only: never read target contents or write a target.
  (let [root (.toRealPath (.toPath (io/file ".")) (make-array java.nio.file.LinkOption 0))]
    (doseq [row rows field (vals (dissoc row :id)) [_ _ target] (re-seq link-pattern field)
            :when (not (str/starts-with? target "https:"))]
      (let [path (relative-target component task-path target)
            file (io/file (str path))]
        (when-not (.isFile file) (fail component "link target is not an existing file"))
        (when-not (.startsWith (.toRealPath (.toPath file) (make-array java.nio.file.LinkOption 0)) root)
          (fail component "link target resolves outside repository")))))
  rows)
