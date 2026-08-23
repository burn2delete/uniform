(ns gravity.self-hosting.sh01-language-boundary-test
  "Fail-closed source-language boundary for bootstrap, seed, and tooling work."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]])
  (:import [java.nio.file Files Path]
           [java.nio.file.attribute FileAttribute]
           [java.security DigestInputStream MessageDigest]))

(def ^:private contract-relative-path "contracts/language-boundary.edn")

(def ^:private canonical-programming-language-extensions
  [".py" ".pyw" ".pyi" ".pyx" ".java" ".c" ".h" ".cc" ".cpp" ".cxx" ".hpp" ".hh" ".hxx"
   ".m" ".mm" ".swift" ".rs" ".go" ".js" ".jsx" ".ts" ".tsx" ".rb"
   ".php" ".pl" ".pm" ".lua" ".dart" ".kt" ".kts" ".scala" ".cs"
   ".fs" ".fsx" ".vb" ".sh" ".bash" ".zsh" ".fish" ".ps1" ".r" ".jl"
   ".ex" ".exs" ".erl" ".hrl" ".hs" ".lhs" ".ml" ".mli" ".asm" ".s"])

(def ^:private canonical-non-lisp-shebang-interpreters
  ["bash" "deno" "fish" "lua" "node" "perl" "php" "python" "python2"
   "python3" "ruby" "sh" "swift" "zsh"])

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh01_language_boundary_test.clj")]
    (when-not resource
      (throw (ex-info "Language-boundary test source is not on the classpath"
                      {:id "LANGUAGE-BOUNDARY-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "LANGUAGE-BOUNDARY-REPOSITORY-ROOT"}))

        (and (Files/isRegularFile (.resolve candidate "deps.edn")
                                  (make-array java.nio.file.LinkOption 0))
             (Files/isRegularFile (.resolve candidate contract-relative-path)
                                  (make-array java.nio.file.LinkOption 0)))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- read-contract
  [root-path]
  (edn/read-string
   {:readers *data-readers*}
   (slurp (.toFile (.resolve ^Path root-path contract-relative-path)))))

(defn- closed-keys!
  [value expected context]
  (when-not (map? value)
    (throw (ex-info "Language-boundary contract object must be a map"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-SHAPE"
                     :context context})))
  (when-not (= expected (set (keys value)))
    (throw (ex-info "Language-boundary contract object has unexpected keys"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-KEYS"
                     :context context
                     :expected (vec (sort expected))
                     :actual (vec (sort (keys value)))}))))

(defn- entry-map!
  [entries suffix classification context]
  (when-not (vector? entries)
    (throw (ex-info "Frozen language entries must be a vector"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-ENTRIES"
                     :context context})))
  (let [pairs
        (mapv
         (fn [entry]
           (when-not (and (vector? entry)
                          (= 2 (count entry))
                          (string? (first entry))
                          (string? (second entry))
                          (or (nil? suffix)
                              (str/ends-with? (first entry) suffix))
                          (re-matches #"[0-9a-f]{64}" (second entry)))
             (throw (ex-info "Frozen language entry is malformed"
                             {:id "LANGUAGE-BOUNDARY-CONTRACT-ENTRY"
                              :context context
                              :entry entry})))
           entry)
         entries)
        paths (mapv first pairs)]
    (when-not (= (count paths) (count (set paths)))
      (throw (ex-info "Frozen language paths must be unique"
                      {:id "LANGUAGE-BOUNDARY-CONTRACT-DUPLICATE"
                       :context context})))
    (when-not (= paths (vec (sort paths)))
      (throw (ex-info "Frozen language paths must be sorted"
                      {:id "LANGUAGE-BOUNDARY-CONTRACT-ORDER"
                       :context context})))
    (into {} (map (fn [[path digest]]
                    [path {:sha256 digest :classification classification}])
                  pairs))))

(defn- exact-vector!
  [value context]
  (when-not (and (vector? value)
                 (seq value)
                 (every? string? value)
                 (= (count value) (count (set value))))
    (throw (ex-info "Language-boundary vector must contain unique strings"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-VECTOR"
                     :context context})))
  value)

(defn validate-contract!
  "Validates the closed EDN policy and returns its normalized frozen inventory."
  [contract]
  (closed-keys!
   contract
   #{:schema :policy :scan-roots :ignored-directory-paths
     :ignored-directory-names
     :permitted-lisp-extensions
     :permitted-data-extensions :programming-language-extensions
     :non-lisp-shebang-interpreters
     :legacy-python :legacy-java :legacy-shell :legacy-c}
   :root)
  (when-not (= :gravity/language-boundary-v1 (:schema contract))
    (throw (ex-info "Unexpected language-boundary schema"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-SCHEMA"})))
  (closed-keys!
   (:policy contract)
   #{:bootstrap-seed-tooling-language :successor-languages
     :python-authority :legacy-python-policy :legacy-java-policy
     :legacy-shell-policy :new-non-lisp-policy}
   :policy)
  (when-not (= {:bootstrap-seed-tooling-language :clojure
                :successor-languages [:gravity :uniform]
                :python-authority :none
                :legacy-python-policy :frozen-removal-only
                :legacy-java-policy :frozen-removal-only
                :legacy-shell-policy :frozen-removal-only
                :new-non-lisp-policy :reject}
               (:policy contract))
    (throw (ex-info "Language policy does not encode the required transition"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-POLICY"})))
  (doseq [[key expected]
          [[:scan-roots ["."]]
           [:ignored-directory-paths [".cpcache" ".git" "target"]]
           [:ignored-directory-names ["__pycache__"]]
           [:permitted-lisp-extensions [".clj" ".cljc" ".cljs" ".gravity" ".qst"]]
           [:permitted-data-extensions [".edn"]]]]
    (exact-vector! (get contract key) key)
    (when-not (= expected (get contract key))
      (throw (ex-info "Language-boundary contract list is not canonical"
                      {:id "LANGUAGE-BOUNDARY-CONTRACT-CANONICAL"
                       :key key :expected expected :actual (get contract key)}))))
  (exact-vector! (:programming-language-extensions contract)
                 :programming-language-extensions)
  (when-not (= canonical-programming-language-extensions
               (:programming-language-extensions contract))
    (throw (ex-info "Programming-language denial catalog is not canonical"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-EXTENSIONS"})))
  (exact-vector! (:non-lisp-shebang-interpreters contract)
                 :non-lisp-shebang-interpreters)
  (when-not (= canonical-non-lisp-shebang-interpreters
               (:non-lisp-shebang-interpreters contract))
    (throw (ex-info "Non-Lisp shebang catalog is not canonical"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-SHEBANGS"})))
  (closed-keys! (:legacy-python contract)
                #{:classification :authority :entries}
                :legacy-python)
  (when-not (= {:classification :frozen-removal-only-debt :authority :none}
               (select-keys (:legacy-python contract)
                            [:classification :authority]))
    (throw (ex-info "Legacy Python must carry no authority"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-PYTHON-AUTHORITY"})))
  (closed-keys! (:legacy-java contract)
                #{:classification :entries}
                :legacy-java)
  (when-not (= :frozen-host-shim-debt
               (get-in contract [:legacy-java :classification]))
    (throw (ex-info "Legacy Java must be frozen host-shim debt"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-JAVA-CLASSIFICATION"})))
  (closed-keys! (:legacy-shell contract)
                #{:classification :entries}
                :legacy-shell)
  (when-not (= :frozen-removal-only-shell-launcher-debt
               (get-in contract [:legacy-shell :classification]))
    (throw (ex-info "Legacy shell launchers must be frozen debt"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-SHELL-CLASSIFICATION"})))
  (closed-keys! (:legacy-c contract)
                #{:classification :entries}
                :legacy-c)
  (when-not (= :frozen-removal-only-target-and-native-debt
               (get-in contract [:legacy-c :classification]))
    (throw (ex-info "Legacy C must be frozen non-Lisp debt"
                    {:id "LANGUAGE-BOUNDARY-CONTRACT-C-CLASSIFICATION"})))
  (let [python
        (entry-map! (get-in contract [:legacy-python :entries])
                    ".py" :frozen-removal-only-debt :legacy-python)
        java
        (entry-map! (get-in contract [:legacy-java :entries])
                    ".java" :frozen-host-shim-debt :legacy-java)
        shell
        (entry-map! (get-in contract [:legacy-shell :entries])
                    nil :frozen-removal-only-shell-launcher-debt
                    :legacy-shell)
        c
        (entry-map! (get-in contract [:legacy-c :entries])
                    ".c" :frozen-removal-only-target-and-native-debt
                    :legacy-c)]
    (when-not (= 1 (count java))
      (throw (ex-info "Exactly one legacy Java host shim is permitted"
                      {:id "LANGUAGE-BOUNDARY-CONTRACT-JAVA-COUNT"
                       :count (count java)})))
    (when-not (= (+ (count python) (count java) (count shell) (count c))
                 (count (merge python java shell c)))
      (throw (ex-info "Frozen language inventories must be disjoint"
                      {:id "LANGUAGE-BOUNDARY-CONTRACT-OVERLAP"})))
    {:python python :java java :shell shell :c c
     :all (merge python java shell c)}))

(defn- sha256
  [file]
  (let [digest (MessageDigest/getInstance "SHA-256")
        buffer (byte-array 8192)]
    (with-open [input (DigestInputStream. (io/input-stream file) digest)]
      (loop []
        (when-not (= -1 (.read input buffer))
          (recur))))
    (apply str (map #(format "%02x" (bit-and 0xff %)) (.digest digest)))))

(defn- normalized-relative
  [root-path file]
  (-> (str (.relativize (.normalize ^Path root-path)
                        (.normalize (.toPath ^java.io.File file))))
      (str/replace java.io.File/separator "/")))

(defn- extension
  [relative]
  (some-> (re-find #"(\.[^./]+)$" relative) second str/lower-case))

(defn- scannable-directory?
  [root-path file ignored-directory-paths ignored-directory-names]
  (let [relative (normalized-relative root-path file)]
    (and (.isDirectory ^java.io.File file)
         (not (Files/isSymbolicLink (.toPath ^java.io.File file)))
         (not (contains? (set ignored-directory-paths) relative))
         (not (contains? (set ignored-directory-names) (.getName file))))))

(defn- pruned-file-seq
  [root-path root-file ignored-directory-paths ignored-directory-names]
  (tree-seq
   #(scannable-directory? root-path %
                          ignored-directory-paths
                          ignored-directory-names)
   #(or (seq (.listFiles ^java.io.File %)) [])
   root-file))

(defn- scanned-files
  [root-path scan-roots ignored-directory-paths ignored-directory-names]
  (->> scan-roots
       (map #(.toFile (.resolve ^Path root-path ^String %)))
       (filter #(.isDirectory ^java.io.File %))
       (mapcat #(pruned-file-seq root-path %
                                 ignored-directory-paths
                                 ignored-directory-names))
       (filter #(.isFile ^java.io.File %))
       (map (fn [file] [(normalized-relative root-path file) file]))
       (into (sorted-map))))

(defn- non-lisp-shebang-interpreter
  [file configured]
  (with-open [reader (io/reader file)]
    (let [line (some-> (.readLine reader) str/trim str/lower-case)]
      (when (and line (str/starts-with? line "#!"))
        (let [configured (set configured)]
          (some
           (fn [token]
             (let [base (last (str/split token #"/"))
                   normalized (if (re-matches #"python[0-9.]*" base)
                                "python"
                                base)]
               (when (contains? configured normalized) normalized)))
           (str/split (subs line 2) #"\s+")))))))

(defn validate-tree
  "Returns a deterministic report. Frozen files may disappear but may not change."
  [root-path contract]
  (let [{:keys [all]} (validate-contract! contract)
        files (scanned-files root-path
                             (:scan-roots contract)
                             (:ignored-directory-paths contract)
                             (:ignored-directory-names contract))
        programming (set (:programming-language-extensions contract))
        permitted (set (concat (:permitted-lisp-extensions contract)
                               (:permitted-data-extensions contract)))
        extension-introduced
        (for [[relative _] files
              :let [suffix (extension relative)]
              :when (and (contains? programming suffix)
                         (not (contains? permitted suffix))
                         (not (contains? all relative)))]
          {:id "LANGUAGE-BOUNDARY-NON-LISP-INTRODUCED"
           :path relative :extension suffix})
        shebang-introduced
        (for [[relative file] files
              :let [suffix (extension relative)
                    interpreter
                    (when (nil? suffix)
                      (non-lisp-shebang-interpreter
                       file (:non-lisp-shebang-interpreters contract)))]
              :when (and interpreter (not (contains? all relative)))]
          {:id "LANGUAGE-BOUNDARY-NON-LISP-SHEBANG-INTRODUCED"
           :path relative :interpreter interpreter})
        modified
        (for [[relative frozen] all
              :let [file (get files relative)
                    expected-sha (:sha256 frozen)
                    classification (:classification frozen)]
              :when (and file (not= expected-sha (sha256 file)))]
          {:id "LANGUAGE-BOUNDARY-FROZEN-DEBT-MODIFIED"
           :path relative :classification classification})
        violations
        (->> (concat extension-introduced shebang-introduced modified)
             (sort-by (juxt :path :id))
             vec)]
    (array-map
     :schema :gravity/language-boundary-report-v1
     :status (if (empty? violations) :passed :failed)
     :python-authority :none
     :scanned-roots (:scan-roots contract)
     :frozen-present (count (filter #(contains? files %) (keys all)))
     :frozen-retired (count (remove #(contains? files %) (keys all)))
     :violations violations)))

(defn- write-file!
  [root-path relative content]
  (let [target (.resolve ^Path root-path ^String relative)]
    (Files/createDirectories (.getParent target)
                             (make-array FileAttribute 0))
    (spit (.toFile target) content)
    target))

(defn- delete-tree!
  [root-path]
  (when (Files/exists root-path (make-array java.nio.file.LinkOption 0))
    (doseq [file (reverse (file-seq (.toFile root-path)))]
      (Files/deleteIfExists (.toPath ^java.io.File file)))))

(defmacro with-temp-tree
  [[binding] & body]
  `(let [~binding (Files/createTempDirectory
                   "gravity-language-boundary-"
                   (make-array FileAttribute 0))]
     (try
       ~@body
       (finally
         (delete-tree! ~binding)))))

(defn- fixture-contract
  [python-sha java-sha]
  {:schema :gravity/language-boundary-v1
   :policy {:bootstrap-seed-tooling-language :clojure
            :successor-languages [:gravity :uniform]
            :python-authority :none
            :legacy-python-policy :frozen-removal-only
            :legacy-java-policy :frozen-removal-only
            :legacy-shell-policy :frozen-removal-only
            :new-non-lisp-policy :reject}
   :scan-roots ["."]
   :ignored-directory-paths [".cpcache" ".git" "target"]
   :ignored-directory-names ["__pycache__"]
   :permitted-lisp-extensions [".clj" ".cljc" ".cljs" ".gravity" ".qst"]
   :permitted-data-extensions [".edn"]
   :programming-language-extensions canonical-programming-language-extensions
   :non-lisp-shebang-interpreters canonical-non-lisp-shebang-interpreters
   :legacy-python {:classification :frozen-removal-only-debt
                   :authority :none
                   :entries [["tools/legacy.py" python-sha]]}
   :legacy-java {:classification :frozen-host-shim-debt
                 :entries [["bootstrap/legacy/Main.java" java-sha]]}
   :legacy-shell {:classification :frozen-removal-only-shell-launcher-debt
                  :entries []}
   :legacy-c {:classification :frozen-removal-only-target-and-native-debt
              :entries []}})

(deftest repository-language-boundary-is-closed-and-satisfied
  (let [contract (read-contract @root)
        normalized (validate-contract! contract)
        report (validate-tree @root contract)]
    (is (= #{"bootstrap/clojure/java/gravity/cli/Main.java"}
           (set (keys (:java normalized)))))
    (is (= #{"bin/gravity" "bin/gravity-bootstrap"}
           (set (keys (:shell normalized)))))
    (is (= :passed (:status report)) (pr-str (:violations report)))
    (is (= :none (:python-authority report)))))

(deftest new-python-and-java-are-rejected
  (with-temp-tree [tree]
    (let [legacy-python (write-file! tree "tools/legacy.py" "old python\n")
          legacy-java (write-file! tree "bootstrap/legacy/Main.java" "old java\n")
          contract (fixture-contract (sha256 (.toFile legacy-python))
                                     (sha256 (.toFile legacy-java)))]
      (write-file! tree "experiments/new_tool.py" "print('forbidden')\n")
      (write-file! tree "tools/escape.pyw" "#!/usr/bin/python3\n")
      (write-file! tree "bootstrap/new/Launcher.java" "class Launcher {}\n")
      (write-file! tree "src/new_runtime.rs" "fn main() {}\n")
      (is (= [["LANGUAGE-BOUNDARY-NON-LISP-INTRODUCED" "bootstrap/new/Launcher.java"]
              ["LANGUAGE-BOUNDARY-NON-LISP-INTRODUCED" "experiments/new_tool.py"]
              ["LANGUAGE-BOUNDARY-NON-LISP-INTRODUCED" "src/new_runtime.rs"]
              ["LANGUAGE-BOUNDARY-NON-LISP-INTRODUCED" "tools/escape.pyw"]
              ]
             (mapv (juxt :id :path) (:violations (validate-tree tree contract))))))))

(deftest extensionless-non-lisp-shebangs-are-rejected-repository-wide
  (with-temp-tree [tree]
    (let [legacy-python (write-file! tree "tools/legacy.py" "old python\n")
          legacy-java (write-file! tree "bootstrap/legacy/Main.java" "old java\n")
          contract (fixture-contract (sha256 (.toFile legacy-python))
                                     (sha256 (.toFile legacy-java)))]
      (write-file! tree "bin/new-launcher" "#!/usr/bin/env bash\nexit 0\n")
      (write-file! tree "examples/analyzer" "#!/usr/bin/python3.12\n")
      (is (= [["LANGUAGE-BOUNDARY-NON-LISP-SHEBANG-INTRODUCED"
               "bin/new-launcher"]
              ["LANGUAGE-BOUNDARY-NON-LISP-SHEBANG-INTRODUCED"
               "examples/analyzer"]]
             (mapv (juxt :id :path)
                   (:violations (validate-tree tree contract))))))))

(deftest frozen-shell-launcher-may-be-removed-but-not-modified
  (with-temp-tree [tree]
    (let [legacy-python (write-file! tree "tools/legacy.py" "old python\n")
          legacy-java (write-file! tree "bootstrap/legacy/Main.java" "old java\n")
          launcher (write-file! tree "bin/gravity" "#!/usr/bin/env bash\nexit 0\n")
          contract
          (assoc-in
           (fixture-contract (sha256 (.toFile legacy-python))
                             (sha256 (.toFile legacy-java)))
           [:legacy-shell :entries]
           [["bin/gravity" (sha256 (.toFile launcher))]])]
      (spit (.toFile launcher) "#!/usr/bin/env bash\nexit 1\n")
      (is (= [{:id "LANGUAGE-BOUNDARY-FROZEN-DEBT-MODIFIED"
               :path "bin/gravity"
               :classification :frozen-removal-only-shell-launcher-debt}]
             (:violations (validate-tree tree contract))))
      (Files/delete launcher)
      (is (= :passed (:status (validate-tree tree contract)))))))

(deftest modified-legacy-python-is-rejected-but-removal-is-accepted
  (with-temp-tree [tree]
    (let [legacy-python (write-file! tree "tools/legacy.py" "old python\n")
          legacy-java (write-file! tree "bootstrap/legacy/Main.java" "old java\n")
          contract (fixture-contract (sha256 (.toFile legacy-python))
                                     (sha256 (.toFile legacy-java)))]
      (spit (.toFile legacy-python) "changed python\n")
      (is (= [{:id "LANGUAGE-BOUNDARY-FROZEN-DEBT-MODIFIED"
               :path "tools/legacy.py"
               :classification :frozen-removal-only-debt}]
             (:violations (validate-tree tree contract))))
      (Files/delete legacy-python)
      (is (= :passed (:status (validate-tree tree contract))))
      (is (= 1 (:frozen-retired (validate-tree tree contract)))))))

(deftest clojure-gravity-uniform-and-edn-remain-permitted
  (with-temp-tree [tree]
    (let [legacy-python (write-file! tree "tools/legacy.py" "old python\n")
          legacy-java (write-file! tree "bootstrap/legacy/Main.java" "old java\n")
          contract (fixture-contract (sha256 (.toFile legacy-python))
                                     (sha256 (.toFile legacy-java)))]
      (write-file! tree "tools/gate.clj" "(ns gate)\n")
      (write-file! tree "bootstrap/seed.cljc" "(ns seed)\n")
      (write-file! tree "src/module.gravity" "(def answer 42)\n")
      (write-file! tree "src/module.qst" "(def answer 42)\n")
      (write-file! tree "tools/policy.edn" "{:ok true}\n")
      (is (= :passed (:status (validate-tree tree contract)))))))

(deftest closed-contract-rejects-policy-expansion
  (with-temp-tree [tree]
    (let [legacy-python (write-file! tree "tools/legacy.py" "old python\n")
          legacy-java (write-file! tree "bootstrap/legacy/Main.java" "old java\n")
          contract (assoc (fixture-contract (sha256 (.toFile legacy-python))
                                            (sha256 (.toFile legacy-java)))
                          :escape-hatch true)]
      (is (= "LANGUAGE-BOUNDARY-CONTRACT-KEYS"
             (:id (try
                    (validate-tree tree contract)
                    nil
                    (catch clojure.lang.ExceptionInfo failure
                      (ex-data failure)))))))))
