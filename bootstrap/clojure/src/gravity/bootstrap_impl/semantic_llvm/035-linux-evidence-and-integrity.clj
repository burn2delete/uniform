(defn- p15-s23-b3-llvm-linux-development-evidence
  [source-path lowering options]
  (let [commands (p15-s23-b3-llvm-expected-command-contracts)
        _ (when-not (p15-s23-b3-llvm-linux-command-contract-valid? commands)
            (p15-s23-b3-llvm-fail!
             "B3-TARGET" source-path {}
             {:missing-fact :closed-linux-docker-command-contract}))
        run? (true? (:run-linux-development-tools? options))]
    (if run?
      (p15-s23-b3-llvm-linux-toolchain-transaction!
       p15-s23-b3-llvm-finalization-token source-path lowering)
      {:artifact :gravity/b3-llvm-linux-development-toolchain-evidence
     :schema-version 1
     :status :incomplete
     :authoritative? false
     :native-authority? false
     :image p15-s23-b3-llvm-linux-image
     :platform p15-s23-b3-llvm-linux-platform
     :pull-policy :never
     :network :none
     :llvm-version "20.1.8"
     :target (:target-triple p15-s23-b3-llvm-policy)
     :layout (:data-layout p15-s23-b3-llvm-policy)
     :commands commands
     :docker-host {:status :runtime-derived-required}
     :docker-engine {:status :runtime-derived-required}
     :container {:status :runtime-derived-required}
     :qemu-binfmt {:status :runtime-derived-required}
     :tool-paths {:status :runtime-derived-required}
     :tool-hashes {:status :runtime-derived-required}
     :llvm-ir {:status :runtime-derived-required}
     :elf-object {:status :runtime-derived-required
                  :logical-path "program.o"
                  :format :elf :architecture :x86_64}
     :elf-executable {:status :runtime-derived-required
                      :logical-path "program"
                      :format :elf :architecture :x86_64}
     :process-result {:status :runtime-derived-required
                      :expected-exit-code (:expected-exit-code lowering)}
     :b13 :runtime-derived-required
     :b14 :runtime-derived-required
     :c18 :runtime-derived-required
     :provenance {:status :runtime-derived-required}
     :replay {:status :runtime-derived-required}
     :clojure-seed-boundary? true
     :public? false
     :self-hosted? false
     :release? false})))

(defn- p15-s23-b3-llvm-linux-evidence-integrity!
  "Validate a completed local envelope before any fresh Docker replay.  This
  turns object/hash/lineage/provenance tampering into a deterministic B3
  rejection without spending a tool observation."
  [artifact source-path]
  (let [packet (:c13-c14-b1-packet artifact)
        c11 (:c11 packet)
        c13 (:c13 packet)
        c14 (:c14 packet)
        b1 (:b1 packet)
        b13-files (get-in artifact [:b13-record :artifact-files])
        actual-path-provenance (:actual-path-provenance artifact)
        semantic-id (p15-s23-b3-llvm-artifact-id artifact)
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:kind artifact)
          :schema-version (:schema-version artifact)
          :semantic-id semantic-id})
        actual-path-binding-id
        (p15-s23-b3-llvm-actual-path-binding-id
         semantic-id actual-path-provenance)
        packet-semantic-id (p15-s23-c13-c14-b1-semantic-id packet)
        packet-artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:kind packet)
          :schema-version (:schema-version packet)
          :semantic-id packet-semantic-id})
        packet-actual-path-binding-id
        (p15-s23-c13-c14-b1-actual-path-binding-id
         packet-semantic-id (:actual-path-provenance packet))
        byte-array-class (Class/forName "[B")
        check-file
        (fn [kind expected-format expected-architecture header-kind]
          (let [file (get b13-files kind)
                bytes (:bytes file)]
            (when-not (and (map? file)
                           (= :observed (:status file))
                           (instance? byte-array-class bytes)
                           (= (alength ^bytes bytes) (:byte-count file))
                           (= (p15-s23-b3-llvm-sha256-bytes bytes)
                              (:content-hash file))
                           (= expected-format (:format file))
                           (= expected-architecture (:architecture file))
                           (p15-s23-b3-llvm-linux-elf-bytes-valid?
                            bytes header-kind)
                           (p15-s23-b3-llvm-linux-elf-header-valid?
                            (:header file) header-kind))
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path artifact
               {:missing-fact :local-elf-artifact-integrity
                :artifact-file kind}))))]
    (when-not (= semantic-id (:semantic-id artifact))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path artifact
       {:missing-fact :local-semantic-id-recompute
        :expected semantic-id :observed (:semantic-id artifact)}))
    (when-not (= artifact-id (:artifact-id artifact))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path artifact
       {:missing-fact :local-artifact-id-recompute
        :expected artifact-id :observed (:artifact-id artifact)}))
    (when-not (= actual-path-binding-id (:actual-path-binding-id artifact))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path artifact
       {:missing-fact :local-actual-path-binding-recompute
        :expected actual-path-binding-id
        :observed (:actual-path-binding-id artifact)}))
    (when-not (= [packet-semantic-id packet-artifact-id
                 packet-actual-path-binding-id]
                ((juxt :semantic-id :artifact-id :actual-path-binding-id)
                 packet))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path artifact
       {:missing-fact :local-c13-c14-b1-packet-identity
        :expected [packet-semantic-id packet-artifact-id
                   packet-actual-path-binding-id]
        :observed ((juxt :semantic-id :artifact-id :actual-path-binding-id)
                   packet)}))
    ;; Packet identity includes the embedded stage ids, so packet/outer
    ;; recomputation alone cannot establish that those ids still authenticate
    ;; their C13, C14 request, C14, and B1 contents.  Reuse the exact sidecar
    ;; derivations before any contextual replay can observe Docker or LLVM.
    (p15-s23-c13-c14-b1-sidecar-evidence! artifact)
    (when-not (and (= :llvm-x86_64-linux (:target artifact))
                   (= p15-s23-b3-llvm-policy (:target-policy artifact))
                   (= (:artifact-id c11)
                      (get-in c13 [:input :c11-artifact-id]))
                   (= (:artifact-id c13)
                      (get-in c14 [:request :input :artifact-id]))
                   (= (:artifact-id c14)
                      (get-in b1 [:backend-manifest :c14-artifact-id]))
                   (= (:artifact-id packet)
                      (get-in artifact [:c13-c14-b1-packet :artifact-id]))
                   (= :llvm-x86_64-linux
                      (get-in artifact [:b13-record :target :canonical-target]))
                   (= :elf
                      (get-in artifact [:b13-record :target :object-format]))
                   (= :x86_64
                      (get-in artifact [:b13-record :target :architecture])))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path artifact
       {:missing-fact :local-c13-c14-b1-target-lineage}))
    (check-file :object :elf :x86_64 :object)
    (check-file :executable :elf :x86_64 :executable)
    (let [toolchain (get artifact :toolchain-evidence)
          snapshot-text (get-in toolchain [:tool-hashes :snapshot])
          snapshot (p15-s23-b3-llvm-linux-tool-snapshot snapshot-text)]
      (when-not (and (= :complete (:status toolchain))
                     (p15-s23-b3-llvm-linux-tool-snapshot-valid?
                      snapshot-text snapshot)
                     (= snapshot (get-in toolchain [:tool-hashes :hashes]))
                     (= (set (keys snapshot))
                        (set (keys (get-in toolchain [:tool-paths :paths]))))
                     (= (into (sorted-map)
                              (map (fn [[tool value]]
                                     [tool (:path value)]))
                              snapshot)
                        (get-in toolchain [:tool-paths :paths]))
                     (= (p15-s23-b3-llvm-stable-toolchain-projection toolchain)
                        (get-in artifact [:replay-projection :toolchain])))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path artifact
         {:missing-fact :local-toolchain-projection-integrity}))
      (when-not (and (= :observed (get-in toolchain [:qemu-binfmt :status]))
                     (= "x86_64" (get-in toolchain [:qemu-binfmt :machine]))
                     (contains? #{:available :unavailable}
                                (get-in toolchain [:qemu-binfmt :qemu-state]))
                     (contains? #{"enabled" "disabled" "unavailable"}
                                (get-in toolchain [:qemu-binfmt :binfmt-state])))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path artifact
         {:missing-fact :local-emulation-observation-integrity})))
    :passed))
