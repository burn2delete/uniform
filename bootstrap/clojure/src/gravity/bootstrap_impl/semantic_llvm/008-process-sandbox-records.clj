(defn- p15-s23-b3-llvm-tool-record
  [step command result]
  (let [stdout-text
        (p15-s23-b3-llvm-normalized-tool-output
         step (get-in result [:stdout :text]))
        stderr-text
        (p15-s23-b3-llvm-normalized-tool-output
         step (get-in result [:stderr :text]))
        stdout-bytes
        (.getBytes stdout-text java.nio.charset.StandardCharsets/UTF_8)
        stderr-bytes
        (.getBytes stderr-text java.nio.charset.StandardCharsets/UTF_8)]
    {:artifact :gravity/b3-bounded-tool-step
     :step step
     :command-contract
     (mapv p15-s23-b3-llvm-normalized-command-argument command)
     :finished? (:finished? result)
     :timed-out? (:timed-out? result)
     :termination (:termination result)
     :exit-code (:exit-code result)
     :stdout-byte-count (alength stdout-bytes)
     :stderr-byte-count (alength stderr-bytes)
     :stdout-retained-byte-count (alength stdout-bytes)
     :stderr-retained-byte-count (alength stderr-bytes)
     :stdout-truncated? (get-in result [:stdout :truncated?])
     :stderr-truncated? (get-in result [:stderr :truncated?])
     :stdout-hash (p15-s23-b3-llvm-sha256-bytes stdout-bytes)
     :stderr-hash (p15-s23-b3-llvm-sha256-bytes stderr-bytes)
     :semantic-output-normalized? true
     :environment-policy p15-s23-b3-llvm-environment-policy
     :raw-output-retained? false}))

(defn- p15-s23-b3-llvm-closed-tool-command?
  [step command]
  (let [target (:target-triple p15-s23-b3-llvm-policy)
        sdk (when (vector? command) (get command 4))
        executable (when (vector? command) (first command))
        tool?
        (fn [name]
          (and (string? executable)
               (.startsWith ^String executable "/")
               (str/ends-with? executable (str "/usr/bin/" name))))]
    (case step
      :xcrun-version
      (= command ["/usr/bin/xcrun" "--version"])
      :file-version
      (= command ["/usr/bin/file" "--version"])
      :clang-path
      (= command ["/usr/bin/xcrun" "--find" "clang"])
      :ld-path
      (= command ["/usr/bin/xcrun" "--find" "ld"])
      :otool-path
      (= command ["/usr/bin/xcrun" "--find" "otool"])
      :otool-version
      (and (tool? "llvm-otool") (= command [executable "--version"]))
      :sdk-path
      (= command ["/usr/bin/xcrun" "--sdk" "macosx"
                  "--show-sdk-path"])
      :sdk-version
      (= command ["/usr/bin/xcrun" "--sdk" "macosx"
                  "--show-sdk-version"])
      :clang-version
      (and (tool? "clang") (= command [executable "--version"]))
      :clang-target-triple
      (and (tool? "clang")
           (= command [executable "-target" target
                       "-print-target-triple"]))
      :clang-default-target
      (and (tool? "clang")
           (= command [executable "-print-target-triple"]))
      :linker-version
      (and (tool? "ld") (= command [executable "-v"]))
      :llvm-to-object
      (and (tool? "clang")
           (string? sdk)
           (str/starts-with? sdk "/")
           (str/ends-with? sdk ".sdk")
           (= command
              [executable "-target" target
               "-isysroot" sdk
               "-x" "ir" "-Werror=override-module" "-O0" "-fPIC"
               "-mcmodel=small" "-mcpu=generic"
               "-Xclang" "-target-feature" "-Xclang" "+v8a"
               "-Xclang" "-target-feature" "-Xclang" "+fp-armv8"
               "-Xclang" "-target-feature" "-Xclang" "+neon"
               "-c" "program.ll" "-o" "program.o"]))
      :link
      (and (tool? "clang")
           (string? sdk)
           (str/starts-with? sdk "/")
           (str/ends-with? sdk ".sdk")
           (string? (get command 6))
           (str/starts-with? (get command 6) "-fuse-ld=/")
           (str/ends-with? (get command 6) "/usr/bin/ld")
           (= command
               [executable "-target" target
               "-isysroot" sdk "-Wl,-reproducible"
               (get command 6) "program.o" "-o" "program"]))
      :file-format
      (= command ["/usr/bin/file" "program.o" "program"])
      :mach-o-header
      (and (tool? "llvm-otool")
           (= command [executable "-hv" "program.o" "program"]))
      :mach-o-load-commands
      (and (tool? "llvm-otool")
           (= command [executable "-l" "program.o" "program"]))
      :runtime-providers
      (and (tool? "llvm-otool")
           (= command [executable "-L" "program"]))
      :run (= command ["./program"])
      false)))
