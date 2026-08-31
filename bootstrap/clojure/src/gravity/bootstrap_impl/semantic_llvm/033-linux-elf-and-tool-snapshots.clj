(defn- p15-s23-b3-llvm-linux-elf-header-valid?
  [header kind]
  (and (string? header)
       (boolean (re-find #"(?i)Format:\s*elf64-x86-64" header))
       (boolean (re-find #"(?i)(Arch|Architecture):\s*x86[-_]64" header))
       (boolean (re-find #"(?i)Machine:\s*EM_X86_64" header))
       (if (= :object kind)
         (boolean (re-find #"(?i)Type:\s*(Relocatable|REL)" header))
         (boolean (re-find #"(?i)Type:\s*(Executable|EXEC)" header)))))

(defn- p15-s23-b3-llvm-linux-elf-bytes-valid?
  [bytes kind]
  (let [byte-at (fn [index]
                  (bit-and (aget ^bytes bytes index) 0xff))
        u16-le (fn [offset]
                 (+ (byte-at offset)
                    (bit-shift-left (byte-at (inc offset)) 8)))
        expected-type (if (= :object kind) 1 2)]
    (and (instance? (Class/forName "[B") bytes)
         (>= (alength ^bytes bytes) 20)
         (= [0x7f 0x45 0x4c 0x46]
            (mapv byte-at (range 4)))
         (= 2 (byte-at 4))
         (= 1 (byte-at 5))
         (= 1 (byte-at 6))
         (= expected-type (u16-le 16))
         (= 62 (u16-le 18)))))

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
