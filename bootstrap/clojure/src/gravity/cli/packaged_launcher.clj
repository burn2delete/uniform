(ns gravity.cli.packaged-launcher
  "Explicit bootstrap-hosted launch of the current packaged JVM CLI.

  The frozen shell wrapper owns the historical package path.  This entrypoint
  keeps package selection explicit after that historical artifact is archived,
  and fails closed when the development package has not been built."
  (:require [clojure.string :as str]))

(def packaged-jar-path
  "target/development-loop-batches/phase-18/jvm-cli/gravity-jvm-cli.jar")

(defn packaged-jvm-cli?
  []
  (= "true" (System/getProperty "gravity.packaged.jvm.cli")))

(defn- runtime-classpath
  []
  (->> (str/split
        (System/getProperty "java.class.path")
        (re-pattern java.io.File/pathSeparator))
       (remove str/blank?)
       (remove #(or (= % "bootstrap/clojure/src")
                    (= % "./bootstrap/clojure/src")
                    (= % packaged-jar-path)
                    (str/ends-with? % "/bootstrap/clojure/src")
                    (str/ends-with? % "/gravity-jvm-cli.jar")))
       (str/join java.io.File/pathSeparator)))

(defn- java-executable
  []
  (let [candidate (java.io.File.
                   (System/getProperty "java.home")
                   "bin/java")]
    (when (.isFile candidate)
      (.getPath candidate))))

(defn packaged-command
  [& arguments]
  ;; The frozen Java Main remains in the package for historical/bin
  ;; compatibility. The current package alias starts the existing lightweight
  ;; Clojure launcher so help/version do not enter gravity.bootstrap.
  (vec (concat [(or (java-executable) "java")
                "-Dgravity.packaged.jvm.cli=true"
                "-cp"
                (str packaged-jar-path
                     java.io.File/pathSeparator
                     (runtime-classpath))
                "clojure.main" "-m" "gravity.cli.launcher"]
               arguments)))

(defn packaged-jar-present?
  []
  (.isFile (java.io.File. packaged-jar-path)))

(defn- runtime-class-major
  []
  (try
    (Long/parseLong
     (first (str/split (System/getProperty "java.class.version") #"\.")))
    (catch Exception _ nil)))

(defn- packaged-main-class-major
  []
  (try
    (with-open [jar-file (java.util.jar.JarFile. packaged-jar-path)
                input (java.io.DataInputStream.
                       (.getInputStream
                        jar-file
                        (.getEntry jar-file "gravity/cli/Main.class")))]
      (let [header (byte-array 8)]
        (.readFully input header)
        (when (= [202 254 186 190]
                 (mapv #(bit-and (aget header %) 0xff) (range 4)))
          (+ (* 256 (bit-and (aget header 6) 0xff))
             (bit-and (aget header 7) 0xff)))))
    (catch Exception _ nil)))

(defn packaged-toolchain-diagnostic
  []
  (let [jar-major (packaged-main-class-major)
        runtime-major (runtime-class-major)
        java-path (java-executable)]
    (when (or (nil? java-path)
              (nil? jar-major)
              (nil? runtime-major)
              (> jar-major runtime-major))
      {:id "P18T02008"
       :message "packaged JVM CLI class version is unavailable or newer than the active Java runtime"
       :bootstrap-stage :phase18-packaged-jvm-cli
       :bootstrap-hosted? true
       :packaged-jvm-cli? true
       :seedless-release? false
       :jar-class-major (or jar-major "unknown")
       :runtime-class-major (or runtime-major "unknown")
       :remediation "Use a compatible Java runtime or rebuild the bootstrap-hosted launcher for a declared Java release floor."})))

(defn packaged-missing-diagnostic
  []
  {:id "P18T02009"
   :message "explicit packaged JVM CLI requires the current development package"
   :bootstrap-stage :phase18-packaged-jvm-cli
   :bootstrap-hosted? true
   :packaged-jvm-cli? true
   :seedless-release? false
   :missing-fact :packaged-jvm-cli-artifact
   :artifact-path packaged-jar-path
   :remediation
   "Run clojure -M -e '(require (quote gravity.bootstrap)) (gravity.bootstrap/p18-t02-build-packaged-jvm-cli!)' and retry clojure -M:packaged-gravity."})

(defn -main
  [& arguments]
  (if (packaged-jvm-cli?)
    (do
      (require 'gravity.bootstrap)
      (apply (or (ns-resolve 'gravity.bootstrap '-main)
                 (throw (IllegalStateException.
                         "bootstrap CLI entrypoint is unavailable")))
             arguments))
    (if-not (packaged-jar-present?)
      (do
        (binding [*out* *err*]
          (prn (packaged-missing-diagnostic)))
        (System/exit 1))
      (if-let [diagnostic (packaged-toolchain-diagnostic)]
        (do
          (binding [*out* *err*]
            (prn diagnostic))
          (System/exit 1))
        (let [process (-> (ProcessBuilder. (java.util.ArrayList.
                                            (apply packaged-command arguments)))
                          (.inheritIO)
                          (.start))]
          (System/exit (.waitFor process)))))))
