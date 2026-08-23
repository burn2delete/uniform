(ns gravity.tooling.repository-hygiene
  "Reject tracked Python interpreter cache outputs after Python retirement."
  (:require [clojure.string :as str])
  (:import (java.nio ByteBuffer)
           (java.nio.charset CharacterCodingException CodingErrorAction StandardCharsets)
           (java.nio.file Path Paths)))

(defn tracked-python-cache-paths [paths]
  (->> paths
       (filter
        (fn [path]
          (let [parts (str/split path #"/")]
            (or (some #{"__pycache__"} parts)
                (str/ends-with? path ".pyc")
                (str/ends-with? path ".pyo")))))
       distinct
       sort
       vec))

(defn- strict-utf8 [bytes]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (str (.decode decoder (ByteBuffer/wrap bytes)))
      (catch CharacterCodingException exception
        (throw (ex-info "git index contains a non-UTF-8 path"
                        {:diagnostic "RH002"}
                        exception))))))

(defn git-tracked-paths
  ([] (git-tracked-paths (.normalize (.toAbsolutePath
                                      (Paths/get "" (make-array String 0))))))
  ([^Path root]
   (let [process (try
                   (-> (ProcessBuilder. ["git" "ls-files" "-z"])
                       (.directory (.toFile root))
                       (.start))
                   (catch java.io.IOException exception
                     (throw (ex-info (str "cannot start git ls-files: "
                                          (.getMessage exception))
                                     {:diagnostic "RH001"}
                                     exception))))
         stdout (.readAllBytes (.getInputStream process))
         stderr (.readAllBytes (.getErrorStream process))
         exit-code (.waitFor process)]
     (when-not (zero? exit-code)
       (throw (ex-info (str "git ls-files failed: "
                            (or (not-empty (str/trim (strict-utf8 stderr)))
                                exit-code))
                       {:diagnostic "RH001" :exit-code exit-code})))
     (->> (str/split (strict-utf8 stdout) #"\u0000" -1)
          (remove empty?)
          vec))))

(defn validate-repository
  ([] (validate-repository (.normalize (.toAbsolutePath
                                        (Paths/get "" (make-array String 0))))))
  ([^Path root]
   (tracked-python-cache-paths (git-tracked-paths root))))
