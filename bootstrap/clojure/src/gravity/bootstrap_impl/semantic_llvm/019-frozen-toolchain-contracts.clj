(defn p15-s23-b3-llvm-expected-pass-record
  []
  {:passes [:gravity-b3-bounded-lowering
            :independent-seed-reconstruction
            :clang-ir-verification-and-codegen
            :llvm-lld-link]
   :optimization-level :O0
   :ub-sensitive-flags []
   :metadata-preserved? true})

(defn p15-s23-b3-llvm-expected-source-rule
  []
  {:source-content-hash p15-s23-b3-llvm-expected-source-content-hash
   :source-byte-count p15-s23-b3-llvm-source-byte-count
   :plan-semantic-hash p15-s23-b3-llvm-expected-plan-semantic-hash
   :functions-semantic-hash
   p15-s23-b3-llvm-expected-functions-semantic-hash
   :builder-semantic-hash p15-s23-b3-llvm-expected-builder-semantic-hash
   :function-shapes p15-s23-b3-llvm-required-functions})

(defn p15-s23-b3-llvm-expected-toolchain-static-record
  []
  {:artifact :gravity/b3-llvm-toolchain-fingerprint
   :image "silkeh/clang@sha256:ae2f3deffd84470fbb2904cfb990db208a5f9880b4bcf9d3eae080a50a8900b4"
   :platform "linux/amd64"
   :pull-policy :never
   :network :none
   :llvm-version "20.1.8"
   :tool-paths {:status :runtime-derived-required :paths []}
   :tool-hashes {:status :runtime-derived-required :hashes {}}
   :observed-target-triple (:target-triple p15-s23-b3-llvm-policy)
   :canonical-target :llvm-x86_64-linux
   :target-policy-source :pinned-not-host-inferred
   :development-emulation-authority
   {:status :runtime-derived-required
    :docker-host {}
    :docker-engine {}
    :qemu-binfmt {}
    :container {}}
   :observed-environment-only? true
   :native-authority? false})

(defn p15-s23-b3-llvm-expected-command-contracts
  []
  (let [target (:target-triple p15-s23-b3-llvm-policy)
        image (:image (p15-s23-b3-llvm-expected-toolchain-static-record))
        prefix ["docker" "run" "--rm" "--network=none" "--platform"
                "linux/amd64" "--pull=never" "--mount"
                "<workspace-bind:/work>" "--workdir" "/work" image]]
    {:docker-version ["docker" "version" "--format" "{{json .Server}}"]
     :docker-info ["docker" "info" "--format" "{{json .}}"]
     :image-inspect ["docker" "image" "inspect" image]
     :emulation-environment
     (into prefix
           ["sh" "-lc"
            "arch=$(uname -m); kernel=$(uname -r); printf 'uname -m %s\\nkernel %s\\n' \"$arch\" \"$kernel\"; if [ -r /proc/sys/fs/binfmt_misc/status ]; then printf 'binfmt-status '; cat /proc/sys/fs/binfmt_misc/status; else printf '%s\\n' 'binfmt-status unavailable'; fi; if command -v qemu-x86_64-static >/dev/null 2>&1; then p=$(command -v qemu-x86_64-static); printf 'qemu-x86_64-static %s\\n' \"$p\"; else printf '%s\\n' 'qemu-x86_64-static unavailable'; fi"])
     :clang-version (into prefix ["clang" "--version"])
     :clang-target-triple
     (into prefix ["clang" "-target" target "-print-target-triple"])
     :llvm-version (into prefix ["llvm-readobj" "--version"])
     :llvm-tool-hashes
     (into prefix
           ["sh" "-lc"
            "set -eu; for t in clang llc opt llvm-as llvm-dis llvm-readobj llvm-objdump ld.lld; do p=$(command -v \"$t\"); test -n \"$p\"; case \"$p\" in /*) ;; *) exit 64 ;; esac; h=$(sha256sum \"$p\" | cut -d' ' -f1); printf '%s %s sha256:%s\\n' \"$t\" \"$p\" \"$h\"; done"])
     :llvm-to-object
     (into prefix ["clang" "-target" target "-x" "ir"
                   "-Werror=override-module" "-O0" "-fPIC"
                   "-mcmodel=small" "-mcpu=generic" "-c"
                   "/work/program.ll" "-o" "/work/program.o"])
     :link
     (into prefix ["clang" "-target" target "-fuse-ld=lld"
                   "-no-pie" "/work/program.o" "-o" "/work/program"])
     :elf-header
     (into prefix ["llvm-readobj" "--file-headers" "/work/program.o"
                   "/work/program"])
     :elf-sections
     (into prefix ["llvm-readobj" "--sections" "/work/program.o"
                   "/work/program"])
     :runtime-providers
     (into prefix ["llvm-readobj" "--needed-libs" "/work/program"])
     :run (into prefix ["/work/program"])}))

(defn- p15-s23-b3-llvm-contract-bindings
  [c11-artifact checked-core c11-report]
  (let [mir (:mir-module c11-artifact)
        capability-proof-table (:capability-proof-table mir)
        proof-certificate-table (:proof-certificate-table mir)
        dependency-contract
        {:source-core (:artifact-id checked-core)
         :c11-source-rule (:source-rule c11-artifact)
         :c11-pass (get-in mir [:pass-execution-record :record-id])
         :b3-source p15-s23-b3-llvm-expected-source-content-hash}
        c11-verifier-record
        (p15-s23-b3-llvm-c11-verifier-record c11-report)]
    {:profile
     (p15-s23-b3-llvm-content-binding
      (p15-s23-b3-llvm-expected-profile-contract))
     :target
     (p15-s23-b3-llvm-content-binding
      (p15-s23-b3-llvm-expected-target-contract))
     :abi
     (p15-s23-b3-llvm-content-binding
      (p15-s23-b3-llvm-expected-abi-contract))
     :runtime
     (p15-s23-b3-llvm-content-binding
      (p15-s23-b3-llvm-expected-runtime-contract))
     :providers
     (p15-s23-b3-llvm-content-binding
      (p15-s23-b3-llvm-expected-provider-contract))
     :effects
     (p15-s23-b3-llvm-content-binding
      {:declared #{} :table (:effect-table mir)})
     :capabilities
     (p15-s23-b3-llvm-content-binding
      {:declared #{} :table (:capability-table mir)
       :proof-table capability-proof-table})
     :safety (p15-s23-b3-llvm-content-binding (:safety-table mir))
     :proofs
     (p15-s23-b3-llvm-content-binding
      {:capability capability-proof-table
       :certificates proof-certificate-table})
     :source-map (p15-s23-b3-llvm-content-binding (:source-map mir))
     :dependencies (p15-s23-b3-llvm-content-binding dependency-contract)
     :c11-verifier
     {:content-id (p15-s23-c11-mir-digest c11-verifier-record)
      :entry-count 1}}))
