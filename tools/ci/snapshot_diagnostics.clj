(ns ci.snapshot-diagnostics
  "Read-only post-launch dependency evidence. Never resolves or launches anything."
  (:require [clojure.java.io :as io] [clojure.string :as str])
  (:import [java.nio.file Files LinkOption] [java.security MessageDigest]))

(def coordinates [["clojure" "1.12.5"] ["spec.alpha" "0.5.238"]
                  ["core.specs.alpha" "0.4.74"]])
(def text-cap 262144)
(def jar-cap 33554432)

(defn collect [root home install capture]
  (let [roots (mapv (fn [[path label]]
                      [(.getCanonicalPath (io/file path)) label
                       (str (.normalize (.toAbsolutePath (.toPath (io/file path)))))])
                    [[root "$WORKSPACE"] [install "$CLOJURE_INSTALL"]
                     [home "$HOME"] [capture "$CAPTURE"]])
        normalize (fn [s]
                    (let [s (reduce (fn [s [path label lexical]]
                                      (-> s (str/replace path label) (str/replace lexical label)))
                                    (str s) roots)]
                      ;; Unknown absolute paths and URL credentials are not artifact data.
                      (-> s (str/replace #"(?i)https?://[^\s\"<>]+" "<url-redacted>")
                          (str/replace #"(?<![\w$])/(?:[^\s\"\]\[{}(),:]+)" "<external-path>"))))
        bounded (fn [file cap]
                  (when (> (.length file) cap) (throw (ex-info "Capture size exceeded" {:id :capture-limit})))
                  (with-open [in (io/input-stream file) out (java.io.ByteArrayOutputStream.)]
                    (let [buffer (byte-array 8192)]
                      (loop [n 0]
                        (let [k (.read in buffer)]
                          (when (pos? k)
                            (when (> (+ n k) cap) (throw (ex-info "Capture grew" {:id :capture-limit})))
                            (.write out buffer 0 k) (recur (+ n k))))))
                    (.toByteArray out)))
        inspect (fn [file text? cap]
                  (try
                    (let [p (.toPath file) real (.getCanonicalPath file)
                          symlink? (Files/isSymbolicLink p)
                          absolute (str (.normalize (.toAbsolutePath p)))
                          allowed? (some (fn [[r _ lexical]]
                                           (when (str/starts-with? absolute (str lexical "/"))
                                             (= real (str r (subs absolute (count lexical)))))) roots)]
                      (cond
                        (not (.exists file)) {:status :missing :symlink? symlink?}
                        (or symlink? (not allowed?)) {:status :linked-or-outside-path :symlink? symlink?}
                        (not (Files/isRegularFile p (make-array LinkOption 0))) {:status :not-regular}
                        :else (let [bytes (bounded file cap)]
                                (cond-> {:status :observed :size (alength bytes) :symlink? symlink?
                                         :resolved-path (normalize real)
                                         :sha256 (format "%064x" (java.math.BigInteger. 1
                                                                                        (.digest (MessageDigest/getInstance "SHA-256") bytes)))}
                                  text? (assoc :text (normalize (String. bytes java.nio.charset.StandardCharsets/UTF_8)))))))
                    (catch Exception e {:status :unavailable :error-class (.getName (class e))
                                        :id (:id (ex-data e))})))
        cache (io/file root ".cpcache")
        cache-linked? (not= (.getCanonicalPath cache) (str (ffirst roots) "/.cpcache"))
        names (when-not cache-linked?
                (sort (filter #(re-matches #"[0-9]+\.(cp|basis|manifest)" %)
                              (or (seq (.list cache)) []))))
        files (concat
               [["installed-deps" (io/file install "lib/clojure/deps.edn") true text-cap]
                ["launcher-path" (io/file capture "launcher.txt") true text-cap]
                ["init-stderr" (io/file capture "init.stderr") true text-cap]]
               (for [[artifact version] coordinates extension ["pom" "jar"]]
                 [(str artifact "/" version "." extension)
                  (io/file home ".m2/repository/org/clojure" artifact version (str artifact "-" version "." extension))
                  (= extension "pom") (if (= extension "pom") text-cap jar-cap)])
               (when (<= (count names) 16)
                 (for [name names] [(str "classpath-cache/" name) (io/file cache name) true text-cap])))]
    {:schema :uniform/snapshot-dependency-diagnostic-v1
     :kind :post-launch-read-only :dependency-resolution-performed false
     :command ["clojure" "-J-Xmx256m" "-Srepro" "-M" "tools/ci/snapshot.clj"]
     :cache-status (cond cache-linked? :linked-path (> (count names) 16) :capture-file-limit
                         (not (.isDirectory cache)) :missing :else :observed)
     :limits {:text-bytes-per-file text-cap :jar-bytes-per-file jar-cap :cache-files 16}
     :files (into (sorted-map) (map (fn [[label file text? cap]] [label (inspect file text? cap)]) files))
     :nonclaims [:original-failure-repaired :genuine-execution :containment :release]}))

(defn -main [install capture]
  (let [result (collect "." (System/getProperty "user.home") install capture)
        text (str (pr-str result) "\n")
        text (if (<= (alength (.getBytes text java.nio.charset.StandardCharsets/UTF_8)) 8388608)
               text (str (pr-str {:schema (:schema result) :status :artifact-capture-limit}) "\n"))]
    (.mkdirs (io/file "target/ci"))
    (spit "target/ci/snapshot-dependency-diagnostic.edn" text)))

(when (= "collect" (first *command-line-args*))
  (apply -main (rest *command-line-args*)))
