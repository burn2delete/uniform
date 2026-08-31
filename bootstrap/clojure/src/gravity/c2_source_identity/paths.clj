(ns gravity.c2-source-identity.paths
  (:require [clojure.string :as str]))

(defn normalize-relative-path
  [path]
  (let [slash-path (str/replace (str path) "\\" "/")]
    (->> (str/split slash-path #"/")
         (reduce (fn [segments segment]
                   (cond
                     (or (str/blank? segment) (= "." segment))
                     segments

                     (= ".." segment)
                     (if (and (seq segments) (not= ".." (peek segments)))
                       (pop segments)
                       (conj segments segment))

                     :else
                     (conj segments segment)))
                 [])
         (str/join "/"))))

(defn platform-neutral-absolute-path?
  [path]
  (let [slash-path (str/replace (str path) "\\" "/")]
    (or (str/starts-with? slash-path "/")
        (boolean (re-find #"(?i)^[a-z]:" slash-path)))))

(defn valid-project-relative-path?
  [normalize-relative-path platform-neutral-absolute-path? path]
  (let [normalized-path (normalize-relative-path path)]
    (and (not (platform-neutral-absolute-path? path))
         (not (str/blank? normalized-path))
         (not= ".." (first (str/split normalized-path #"/"))))))
