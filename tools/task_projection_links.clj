(ns task-projection-links
  "Document-relative task links and bounded repository output writes."
  (:require [clojure.string :as str] [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")

(def link-pattern #"\[([^\[\]<>!\\\n]+)\]\(([^\s()<>\"`\\]+)\)")
(defn fail [component message] (task-projection/fail (str component " " message)))

(def nofollow-links
  (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS]))

(defn- repository-root []
  (.toRealPath (.toPath (io/file ".")) (make-array java.nio.file.LinkOption 0)))

(defn safe-output-path
  "Return a repository-relative output path after rejecting symlinked parents and targets."
  [component path]
  (let [root (repository-root)
        relative (.normalize (.toPath (io/file path)))]
    (when (or (.isAbsolute relative) (.startsWith relative ".."))
      (fail component "output path must remain repository-relative"))
    (let [target (.normalize (.resolve root relative))
          parent (.getParent target)
          parent-relative (.relativize root parent)]
      (when-not (.startsWith target root)
        (fail component "output path escapes repository"))
      (loop [cursor root parts (iterator-seq (.iterator parent-relative))]
        (when (seq parts)
          (let [next-path (.resolve cursor (str (first parts)))]
            (when (java.nio.file.Files/isSymbolicLink next-path)
              (fail component "output parent must not be a symbolic link"))
            (recur next-path (next parts)))))
      (when-not (java.nio.file.Files/isDirectory parent nofollow-links)
        (fail component "output parent must be an existing directory"))
      (when (java.nio.file.Files/isSymbolicLink target)
        (fail component "output target must not be a symbolic link"))
      target)))

(defn write-file!
  "Write a generated repository file only after the no-symlink boundary check."
  [component path text]
  (let [target (safe-output-path component path)]
    (java.nio.file.Files/write target (.getBytes ^String text "UTF-8")
                               (make-array java.nio.file.OpenOption 0))
    target))

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
