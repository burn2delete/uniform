(defn- p15-s23-b3-llvm-sanitized-complete-diagnostic
  [data]
  (when (and (map? data)
             (contains? p15-s23-b3-llvm-diagnostic-rules (:id data))
             (= (:id data) (:rule data))
             (map? (:primary data))
             (map? (:facts data)))
    (let [primary (:primary data)
          source-path
          (or (get-in primary [:span :source])
              (get-in primary [:span :file])
              "<b3-llvm>")
          subject
          {:artifact-id (:artifact primary)
           :syntax-id (:syntax-id primary)
           :op-id (:mir-operation-id primary)
           :source-span (:span primary)
           :source {:origin-id (:origin-id primary)}}
          rebuilt
          (p15-s23-b3-llvm-diagnostic-record
           (:id data) source-path subject (:facts data))
          required (set c15-diagnostic-required-fields)]
      (when (and (every? (set (keys data)) required)
                 (= rebuilt (select-keys data (keys rebuilt))))
        rebuilt))))

;; Linux LLVM development boundary.  This is intentionally separate from the
;; old host publication helpers below: the authenticated B3 route consumes one
;; exact canonical target and can never fall through to a host compiler or a
;; Darwin/Mach-O transaction.  Runtime-derived Docker/ELF observations are
;; supplied only by the later development integration; no identity is guessed
;; here.
(def p15-s23-b3-llvm-linux-image
  "silkeh/clang@sha256:ae2f3deffd84470fbb2904cfb990db208a5f9880b4bcf9d3eae080a50a8900b4")

(def p15-s23-b3-llvm-linux-platform "linux/amd64")

(declare p15-s23-b3-llvm-semantic-input
         p15-s23-b3-llvm-artifact-id
         p15-s23-b3-llvm-actual-path-binding-id
         p15-s23-c13-c14-b1-semantic-id
         p15-s23-c13-c14-b1-actual-path-binding-id)

(defn- p15-s23-b3-llvm-linux-target-valid?
  [target]
  (and (map? target)
       (= :llvm-x86_64-linux (:canonical-target target))
       (= :llvm-x86_64-linux (:target target))
       (= "x86_64-unknown-linux-gnu" (:triple target))
       (= "x86_64-unknown-linux-gnu" (:target-triple target))
       (= :elf (:object-format target))
       (= :x86_64 (:architecture target))
       (= "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
          (:data-layout target))))

(defn- p15-s23-b3-llvm-linux-target-preflight!
  [source-path c11-artifact context]
  (let [mir (:mir-module c11-artifact)
        c11-target (:target-request mir)
        metadata (:target-request-metadata mir)
        context-target (:requested-target context)
        expected-metadata
        {:requested-target :llvm-x86_64-linux
         :source-target :jvm
         :identity-bearing? false
         :downstream-lowering-required? true}]
    (when-not (= :llvm-x86_64-linux context-target)
      (p15-s23-b3-llvm-fail!
       "B3-TARGET" source-path context
       {:missing-fact :canonical-linux-target-required
        :requested-target context-target}))
    (when-not (= :llvm-x86_64-linux c11-target)
      (p15-s23-b3-llvm-fail!
       "B3-TARGET" source-path c11-artifact
       {:missing-fact :canonical-c11-target-request-required
        :requested-target c11-target}))
    (when-not (= expected-metadata metadata)
      (p15-s23-b3-llvm-fail!
       "B3-TARGET" source-path c11-artifact
       {:missing-fact :canonical-target-request-metadata-required
        :requested-target c11-target}))
    (when-not (p15-s23-b3-llvm-linux-target-valid?
              (merge p15-s23-b3-llvm-policy
                     {:triple (:target-triple p15-s23-b3-llvm-policy)}))
      (p15-s23-b3-llvm-fail!
       "B3-TARGET" source-path p15-s23-b3-llvm-policy
       {:missing-fact :linux-elf-target-policy-required
        :requested-target c11-target}))
    :passed))

(defn- p15-s23-b3-llvm-linux-command-contract-valid?
  [commands]
  (let [image p15-s23-b3-llvm-linux-image
        docker-run-prefix
        ["docker" "run" "--rm" "--network=none" "--platform"
         p15-s23-b3-llvm-linux-platform "--pull=never" "--mount"
         "<workspace-bind:/work>" "--workdir" "/work" image]]
    (and (map? commands)
         (= ["docker" "version" "--format" "{{json .Server}}"]
            (:docker-version commands))
         (= ["docker" "info" "--format" "{{json .}}"]
            (:docker-info commands))
         (= ["docker" "image" "inspect" image]
            (:image-inspect commands))
         (= (into docker-run-prefix
                  ["sh" "-lc"
                   "arch=$(uname -m); kernel=$(uname -r); printf 'uname -m %s\\nkernel %s\\n' \"$arch\" \"$kernel\"; if [ -r /proc/sys/fs/binfmt_misc/status ]; then printf 'binfmt-status '; cat /proc/sys/fs/binfmt_misc/status; else printf '%s\\n' 'binfmt-status unavailable'; fi; if command -v qemu-x86_64-static >/dev/null 2>&1; then p=$(command -v qemu-x86_64-static); printf 'qemu-x86_64-static %s\\n' \"$p\"; else printf '%s\\n' 'qemu-x86_64-static unavailable'; fi"])
            (:emulation-environment commands))
         (= (into docker-run-prefix ["clang" "--version"])
            (:clang-version commands))
         (= (into docker-run-prefix
                  ["clang" "-target" "x86_64-unknown-linux-gnu"
                   "-print-target-triple"])
            (:clang-target-triple commands))
         (= (into docker-run-prefix ["llvm-readobj" "--version"])
            (:llvm-version commands))
         (= (into docker-run-prefix
                  ["sh" "-lc"
                   "set -eu; for t in clang llc opt llvm-as llvm-dis llvm-readobj llvm-objdump ld.lld; do p=$(command -v \"$t\"); test -n \"$p\"; case \"$p\" in /*) ;; *) exit 64 ;; esac; h=$(sha256sum \"$p\" | cut -d' ' -f1); printf '%s %s sha256:%s\\n' \"$t\" \"$p\" \"$h\"; done"])
            (:llvm-tool-hashes commands))
         (= (into docker-run-prefix
                  ["clang" "-target" "x86_64-unknown-linux-gnu"
                   "-x" "ir" "-Werror=override-module" "-O0" "-fPIC"
                   "-mcmodel=small" "-mcpu=generic" "-c"
                   "/work/program.ll" "-o" "/work/program.o"])
            (:llvm-to-object commands))
         (= (into docker-run-prefix
                  ["clang" "-target" "x86_64-unknown-linux-gnu"
                   "-fuse-ld=lld" "-no-pie" "/work/program.o" "-o"
                   "/work/program"])
            (:link commands))
         (= (into docker-run-prefix
                  ["llvm-readobj" "--file-headers" "/work/program.o"
                   "/work/program"])
            (:elf-header commands))
         (= (into docker-run-prefix
                  ["llvm-readobj" "--sections" "/work/program.o"
                   "/work/program"])
            (:elf-sections commands))
         (= (into docker-run-prefix
                  ["llvm-readobj" "--needed-libs" "/work/program"])
            (:runtime-providers commands))
         (= (into docker-run-prefix ["/work/program"])
            (:run commands)))))

(defn- p15-s23-b3-llvm-linux-docker-prefix
  [workspace]
  ["docker" "run" "--rm" "--network=none" "--platform"
   p15-s23-b3-llvm-linux-platform "--pull=never" "--mount"
   (str "type=bind,src=" (.toString ^java.nio.file.Path workspace)
        ",dst=/work") "--workdir" "/work"
   p15-s23-b3-llvm-linux-image])

(defn- p15-s23-b3-llvm-linux-docker-command
  [workspace arguments]
  (into (p15-s23-b3-llvm-linux-docker-prefix workspace) arguments))

(defn- p15-s23-b3-llvm-linux-run-step!
  [candidate workspace source-path step command]
  ;; Every development observation is a Docker invocation.  This guard is
  ;; deliberately before the process helper so a future edit cannot route a
  ;; step to a host compiler or launcher.
  (when-not (and (vector? command) (= "docker" (first command)))
    (p15-s23-b3-llvm-fail!
     "B3-TARGET" source-path {}
     {:missing-fact :linux-development-docker-only
      :tool-step step}))
  (when (some? (some #(= "run" %) command))
    (let [expected-mount
          (str "type=bind,src=" (.toString ^java.nio.file.Path workspace)
               ",dst=/work")]
      (when-not (and (some #(= "--network=none" %) command)
                     (some #(= "--platform" %) command)
                     (some #(= "linux/amd64" %) command)
                     (some #(= "--pull=never" %) command)
                     (some #(= "--mount" %) command)
                     (some #(= "/work" %) command)
                     (some #(= expected-mount %) command))
      (p15-s23-b3-llvm-fail!
       "B3-TARGET" source-path {}
       {:missing-fact :pinned-linux-container-bind-and-isolation
        :tool-step step}))))
  (swap! p15-s23-b3-llvm-tool-observation-state
         (fn [state]
           (-> state
               (update :total inc)
               (update-in [:steps step] (fnil inc 0)))))
  (let [result
        (p15-s23-b3-llvm-run-process
         candidate workspace command p15-s23-b3-llvm-tool-timeout-ms
         source-path)
        record (p15-s23-b3-llvm-tool-record step command result)]
    (let [run-step? (= :run step)
          bounded-exit? (and (integer? (:exit-code result))
                             (<= 0 (:exit-code result) 255))]
      (when-not (and (:finished? result)
                     (not (:timed-out? result))
                     (or (and run-step? bounded-exit?)
                         (and (not run-step?)
                              (= 0 (:exit-code result))))
                   (not (get-in result [:stdout :truncated?]))
                   (not (get-in result [:stderr :truncated?])))
        (p15-s23-b3-llvm-fail!
         "B3-TARGET" source-path {}
         {:missing-fact :linux-development-docker-step-failed
          :tool-step step :exit-code (:exit-code result)
          :stdout-hash (get-in result [:stdout :hash])
          :stderr-hash (get-in result [:stderr :hash])
          :timed-out? (:timed-out? result)})))
    {:record record :result result}))
