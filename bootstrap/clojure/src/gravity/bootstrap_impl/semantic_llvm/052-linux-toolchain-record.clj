(defn-
 semantic-llvm-linux-toolchain-record
 [candidate
  source-path
  lowering
  workspace
  ir-path
  object-path
  executable-path
  image
  target
  primary-failure
  state]
 (let
  [{:keys
    [version-step
     info-step
     image-step
     clang-step
     llvm-step
     triple-step
     hashes-step
     emulation-step
     object-step
     object-snapshot
     link-step
     executable-snapshot
     object-header-step
     executable-header-step
     sections-step
     providers-step
     run-step
     object-header
     executable-header
     clang-version-text
     llvm-version-text
     observed-triple
     image-observation-text
     emulation-text
     emulation-observation
     tool-snapshot-text
     tool-snapshot
     expected-exit
     observed-exit
     records
     object-content
     executable-content]}
   state]
  {:llvm-ir
   {:content-hash (str "sha256:" (sha256-hex (:llvm-ir lowering))),
    :byte-count
    (alength
     (.getBytes
      (:llvm-ir lowering)
      java.nio.charset.StandardCharsets/UTF_8))},
   :elf-object
   (assoc
    object-content
    :bytes
    (:bytes object-snapshot)
    :format
    :elf
    :architecture
    :x86_64
    :header
    object-header),
   :authoritative? false,
   :native-authority? false,
   :tool-hashes
   {:status :observed,
    :record-hash (get-in hashes-step [:record :stdout-hash]),
    :hashes
    (into
     (sorted-map)
     (map (fn [[tool value]] [tool (:hash value)]))
     tool-snapshot),
    :snapshot tool-snapshot-text},
   :docker-host
   {:observation-hash (get-in version-step [:record :stdout-hash]),
    :raw-output (get-in version-step [:result :stdout :text])},
   :release? false,
   :c18 :observed,
   :layout (:data-layout p15-s23-b3-llvm-policy),
   :elf-header-output-hashes
   {:object (get-in object-header-step [:record :stdout-hash]),
    :executable
    (get-in executable-header-step [:record :stdout-hash])},
   :schema-version 1,
   :replay
   {:status :observed,
    :tool-record-count (count records),
    :identity
    (p15-s23-c11-mir-digest
     {:kind :gravity/b3-linux-development-replay,
      :commands (mapv :command-contract records),
      :tool-records (mapv :step records)})},
   :image-observation
   {:observation-hash (get-in image-step [:record :stdout-hash]),
    :raw-output (get-in image-step [:result :stdout :text])},
   :docker-engine
   {:observation-hash (get-in info-step [:record :stdout-hash]),
    :raw-output (get-in info-step [:result :stdout :text])},
   :b13 :observed,
   :pull-policy :never,
   :self-hosted? false,
   :tool-paths
   {:status :observed,
    :llvm-version-output (get-in llvm-step [:record :stdout-hash]),
    :paths
    (into
     (sorted-map)
     (map (fn [[tool value]] [tool (:path value)]))
     tool-snapshot),
    :snapshot tool-snapshot-text},
   :commands (p15-s23-b3-llvm-expected-command-contracts),
   :b14 :observed,
   :llvm-version "20.1.8",
   :status :complete,
   :container
   {:status :observed,
    :workspace-mount "/work",
    :network :none,
    :platform p15-s23-b3-llvm-linux-platform,
    :pull-policy :never,
    :architecture (:machine emulation-observation),
    :emulation-observation
    (select-keys
     emulation-observation
     [:machine :binfmt-state :qemu-state])},
   :public? false,
   :elf-executable
   (assoc
    executable-content
    :bytes
    (:bytes executable-snapshot)
    :format
    :elf
    :architecture
    :x86_64
    :header
    executable-header),
   :provenance
   {:status :observed,
    :source-path source-path,
    :identity
    (p15-s23-c11-mir-digest
     {:kind :gravity/b3-linux-development-provenance,
      :source-path source-path,
      :target target,
      :image image,
      :tool-records (mapv :step records)})},
   :image image,
   :artifact :gravity/b3-llvm-linux-development-toolchain-evidence,
   :network :none,
   :target target,
   :qemu-binfmt
   {:qemu-state (:qemu-state emulation-observation),
    :observation-hash (get-in emulation-step [:record :stdout-hash]),
    :binfmt-state (:binfmt-state emulation-observation),
    :command (get-in emulation-step [:record :command-contract]),
    :status :observed,
    :machine (:machine emulation-observation),
    :kernel (:kernel emulation-observation),
    :kernel-hash (:kernel-hash emulation-observation),
    :raw-output emulation-text},
   :process-result
   {:stderr-hash (get-in run-step [:result :stderr :hash]),
    :observed-exit-code observed-exit,
    :stdout-hash (get-in run-step [:result :stdout :hash]),
    :stderr-text (get-in run-step [:result :stderr :text]),
    :stdout-text (get-in run-step [:result :stdout :text]),
    :stderr-byte-count
    (get-in run-step [:result :stderr :total-byte-count]),
    :stdout-byte-count
    (get-in run-step [:result :stdout :total-byte-count]),
    :matched? true,
    :expected-exit-code expected-exit},
   :platform p15-s23-b3-llvm-linux-platform,
   :clojure-seed-boundary? true,
   :tool-records records}))
