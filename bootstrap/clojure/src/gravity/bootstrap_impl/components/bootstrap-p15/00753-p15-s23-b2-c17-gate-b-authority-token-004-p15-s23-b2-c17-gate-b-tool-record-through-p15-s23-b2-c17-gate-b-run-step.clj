(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-tool-record
  [step command result]
  (let [normalize-output
        (fn [text]
          (case step
            (:sdk-path :clang-path :ld-path :otool-path)
            "<physical-path>\n"
            :clang-version
            (str/replace (or text "") #"(?m)^InstalledDir: .+$"
                         "InstalledDir: <effective-clang-directory>")
            :file-version
            (str/replace (or text "") "/usr/share/file/magic"
                         "<file-magic-source>")
            (or text "")))
        stdout-text (normalize-output (get-in result [:stdout :text]))
        stderr-text (normalize-output (get-in result [:stderr :text]))
        stdout-bytes
        (.getBytes stdout-text java.nio.charset.StandardCharsets/UTF_8)
        stderr-bytes
        (.getBytes stderr-text java.nio.charset.StandardCharsets/UTF_8)]
    {:artifact :gravity/b2-c17-bounded-tool-step
     :step step
     :command-contract
     (mapv p15-s23-b2-c17-gate-b-normalized-command-argument command)
     :finished? (:finished? result)
     :timed-out? (:timed-out? result)
     :termination (:termination result)
     :exit-code (:exit-code result)
     :stdout-byte-count (alength ^bytes stdout-bytes)
     :stderr-byte-count (alength ^bytes stderr-bytes)
     :stdout-truncated? (get-in result [:stdout :truncated?])
     :stderr-truncated? (get-in result [:stderr :truncated?])
     :stdout-hash (p15-s23-b2-c17-gate-b-sha256-bytes stdout-bytes)
     :stderr-hash (p15-s23-b2-c17-gate-b-sha256-bytes stderr-bytes)
     :semantic-output-normalized? true
     :environment-policy p15-s23-b2-c17-gate-b-environment-policy
     :raw-output-retained? false}))

(def ^:private p15-s23-b2-c17-gate-b-target-triple
  "arm64-apple-macosx14.0.0")
(def ^:private p15-s23-b2-c17-gate-b-clang-path
  "/Library/Developer/CommandLineTools/usr/bin/clang")
(def ^:private p15-s23-b2-c17-gate-b-ld-path
  "/Library/Developer/CommandLineTools/usr/bin/ld")
(def ^:private p15-s23-b2-c17-gate-b-otool-path
  "/Library/Developer/CommandLineTools/usr/bin/otool")
(def ^:private p15-s23-b2-c17-gate-b-otool-real-path
  "/Library/Developer/CommandLineTools/usr/bin/llvm-otool")
(def ^:private p15-s23-b2-c17-gate-b-sdk-locator-path
  "/Library/Developer/CommandLineTools/SDKs/MacOSX.sdk")
(def ^:private p15-s23-b2-c17-gate-b-sdk-path
  "/Library/Developer/CommandLineTools/SDKs/MacOSX26.5.sdk")
(def ^:private p15-s23-b2-c17-gate-b-file-magic-content
  {:byte-count 7273344
   :content-hash
   "sha256:38fc8af9d342a3a1d32a626195314a913ee255d8cbd259067d665ea55735b7c0"})

(def ^:private p15-s23-b2-c17-gate-b-c-flags
  ["-std=c17" "-Wall" "-Wextra" "-Werror" "-Wconversion"
   "-Wsign-conversion" "-pedantic"])

(defn- p15-s23-b2-c17-gate-b-closed-tool-command?
  [step command accepted-exit-code diagnostic-id]
  (let [clang p15-s23-b2-c17-gate-b-clang-path
        ld p15-s23-b2-c17-gate-b-ld-path
        otool p15-s23-b2-c17-gate-b-otool-real-path
        sdk p15-s23-b2-c17-gate-b-sdk-path
        target p15-s23-b2-c17-gate-b-target-triple
        flags p15-s23-b2-c17-gate-b-c-flags
        expected
        (case step
          :xcrun-version
          [["/usr/bin/xcrun" "--version"] 0 "B2-DIALECT"]
          :file-version
          [["/usr/bin/file" "--version"] 0 "B2-DIALECT"]
          :clang-path
          [["/usr/bin/xcrun" "--find" "clang"] 0 "B2-DIALECT"]
          :ld-path
          [["/usr/bin/xcrun" "--find" "ld"] 0 "B2-ABI"]
          :otool-path
          [["/usr/bin/xcrun" "--find" "otool"] 0 "B2-ABI"]
          :sdk-path
          [["/usr/bin/xcrun" "--sdk" "macosx" "--show-sdk-path"]
           0 "B2-DIALECT"]
          :sdk-version
          [["/usr/bin/xcrun" "--sdk" "macosx" "--show-sdk-version"]
           0 "B2-DIALECT"]
          :clang-version [[clang "--version"] 0 "B2-DIALECT"]
          :clang-target
          [[clang "-target" target "-print-target-triple"]
           0 "B2-DIALECT"]
          :clang-default-target
          [[clang "-print-target-triple"] 0 "B2-DIALECT"]
          :ld-version [[ld "-v"] 0 "B2-ABI"]
          :otool-version [[otool "--version"] 0 "B2-ABI"]
          :c17-syntax
          [(vec (concat [clang "-target" target "-isysroot" sdk]
                        flags ["-fsyntax-only" "program.c"]))
           0 "B2-DIALECT"]
          :c17-compile
          [(vec (concat [clang "-target" target "-isysroot" sdk]
                        flags ["-O0" "-fPIC" "-mcmodel=small"
                               "-mcpu=generic"
                               "-Xclang" "-target-feature"
                               "-Xclang" "+v8a"
                               "-Xclang" "-target-feature"
                               "-Xclang" "+fp-armv8"
                               "-Xclang" "-target-feature"
                               "-Xclang" "+neon"
                               "-c" "program.c" "-o" "program.o"]))
           0 "B2-UB"]
          :c17-link
          [[clang "-target" target "-isysroot" sdk
            "-Wl,-reproducible" (str "-fuse-ld=" ld)
            "program.o" "-o" "program"]
           0 "B2-ABI"]
          :file-format
          [["/usr/bin/file" "program.o" "program"] 0 "B2-ABI"]
          :mach-o-header
          [[otool "-hv" "program.o" "program"] 0 "B2-ABI"]
          :mach-o-load-commands
          [[otool "-l" "program.o" "program"] 0 "B2-ABI"]
          :runtime-providers
          [[otool "-L" "program"] 0 "B2-RUNTIME"]
          :run
          [["./program"] accepted-exit-code "B14-DIFFERENTIAL"]
          nil)]
    (and expected
         (= command (nth expected 0))
         (= accepted-exit-code (nth expected 1))
         (= diagnostic-id (nth expected 2)))))

(defn- p15-s23-b2-c17-gate-b-run-step!
  [candidate directory source-path step command accepted-exit-code
   diagnostic-id]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :run-closed-c17-tool-step)
  (when-not
   (and (keyword? step) (vector? command)
        (every? string? command)
        (integer? accepted-exit-code)
        (<= 0 accepted-exit-code 255)
        (contains? p15-s23-c-backend-diagnostic-rules diagnostic-id)
        (p15-s23-b2-c17-gate-b-closed-tool-command?
         step command accepted-exit-code diagnostic-id))
    (p15-s23-c-backend-fail!
     "B2-MANIFEST" source-path {}
     {:missing-fact :closed-enumerated-c17-tool-command
      :tool-step step}))
  (swap! p15-s23-b2-c17-gate-b-tool-state
         (fn [state]
           (-> state
               (update :total inc)
               (update-in [:steps step] (fnil inc 0)))))
  (let [result
        (try
          (p15-s23-b2-c17-gate-b-run-process
           candidate directory command 30000 source-path)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch clojure.lang.ExceptionInfo exception
            (throw exception))
          (catch Exception exception
            (p15-s23-b2-c17-gate-b-rethrow-interrupt! exception)
            (p15-s23-c-backend-fail!
             diagnostic-id source-path {}
             {:missing-fact :bounded-c-tool-process-start
              :tool-step step
              :stderr-hash
              (str "sha256:"
                   (sha256-hex (.getName (class exception))))})))
        record (p15-s23-b2-c17-gate-b-tool-record
                step command result)]
    (when-not (and (:finished? result)
                   (not (:timed-out? result))
                   (= accepted-exit-code (:exit-code result))
                   (not (get-in result [:stdout :truncated?]))
                   (not (get-in result [:stderr :truncated?])))
      (p15-s23-c-backend-fail!
       diagnostic-id source-path {}
       {:missing-fact :bounded-successful-c17-tool-step
        :tool-step step
        :exit-code (:exit-code result)
        :expected-exit-code accepted-exit-code
        :stdout-byte-count (get-in result [:stdout :total-byte-count])
        :stderr-byte-count (get-in result [:stderr :total-byte-count])
        :stdout-hash (get-in result [:stdout :hash])
        :stderr-hash (get-in result [:stderr :hash])
        :timed-out? (:timed-out? result)}))
    {:record record :result result})))
