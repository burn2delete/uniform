(defn- p15-s23-b3-llvm-linux-elf-header-valid?
  [header kind]
  (and (string? header)
       (boolean (re-find #"(?i)Format:\s*elf64-x86-64" header))
       (boolean (re-find #"(?i)(Arch|Architecture):\s*x86[-_]64" header))
       (boolean (re-find #"(?i)Machine:\s*EM_X86_64" header))
       (if (= :object kind)
         (boolean (re-find #"(?i)Type:\s*(Relocatable|REL)" header))
         (boolean (re-find #"(?i)Type:\s*(Executable|EXEC)" header)))))

; Current-source execution only; this does not renew migration authority.
(def ^:private p15-s23-b3-llvm-linux-elf-prefix-plan
  (delay
    (let [source (.getCanonicalPath
                  (io/file "bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity"))
          rule (c-backend-stage2-plan-emitter-source-rule! source :jvm)]
      (p15-s23-stage2-compiler-artifact-plan
       (:emitter rule) source (slurp source)))))

(defn- p15-s23-b3-llvm-linux-elf-bytes-valid?
  [bytes kind]
  (and (instance? (Class/forName "[B") bytes)
       (true?
        (p15-s23-stage2-runtime-execute-function
         {:engine :gravity-elf-prefix-host-runner :compiler-artifact-plan? true}
         @p15-s23-b3-llvm-linux-elf-prefix-plan
         'w5-b13-linux-elf-header-prefix-valid?
         [(mapv #(Byte/toUnsignedInt %) bytes) kind]))))

(def ^:private p15-s23-b3-llvm-linux-required-tools
  #{"clang" "llc" "opt" "llvm-as" "llvm-dis" "llvm-readobj"
    "llvm-objdump" "ld.lld"})

(defn- p15-s23-b3-llvm-linux-tool-snapshot-valid?
  [text snapshot]
  (let [lines (->> (str/split-lines (or text ""))
                   (map str/trim)
                   (remove str/blank?))]
    (and (= p15-s23-b3-llvm-linux-required-tools (set (keys snapshot)))
         (= (count lines) (count snapshot))
         (every?
          (fn [[tool value]]
            (and (contains? p15-s23-b3-llvm-linux-required-tools tool)
                 (map? value)
                 (string? (:path value))
                 (str/starts-with? (:path value) "/")
                 (re-matches #"sha256:[0-9a-f]{64}" (:hash value))))
          snapshot))))

(defn- p15-s23-b3-llvm-linux-emulation-observation
  [text]
  (let [text (or text "")
        machine (second (re-find #"(?m)^uname -m ([^\s]+)$" text))
        kernel (second (re-find #"(?m)^kernel ([^\s]+)$" text))
        binfmt (second (re-find #"(?m)^binfmt-status(?: ([^\n]+))?$" text))
        qemu (second (re-find #"(?m)^qemu-x86_64-static ([^\n]+)$" text))]
    (when (and (= "x86_64" machine)
               (string? kernel)
               (or (string? binfmt) (str/includes? text "binfmt-status unavailable"))
               (or (string? qemu)
                   (str/includes? text "qemu-x86_64-static unavailable")))
      {:machine machine
       :kernel kernel
       :kernel-hash (str "sha256:" (sha256-hex kernel))
       :binfmt-state (or binfmt "unavailable")
       :qemu-state (if qemu :available :unavailable)})))

(defn- p15-s23-b3-llvm-linux-tool-snapshot
  [text]
  (into (sorted-map)
        (keep (fn [line]
                (when-let [[_ tool path hash]
                           (re-matches
                            #"^([^\s]+)\s+([^\s]+)\s+(sha256:[0-9a-f]{64})\s*$"
                            (str/trim line))]
                  [tool {:path path :hash hash}])))
        (str/split-lines (or text ""))))
