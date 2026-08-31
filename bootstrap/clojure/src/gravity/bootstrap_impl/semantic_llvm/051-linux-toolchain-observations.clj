(defn-
 semantic-llvm-linux-toolchain-observations!
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
     run-step]}
   state
   object-header
   (get-in object-header-step [:result :stdout :text])
   executable-header
   (get-in executable-header-step [:result :stdout :text])
   clang-version-text
   (get-in clang-step [:result :stdout :text])
   llvm-version-text
   (get-in llvm-step [:result :stdout :text])
   observed-triple
   (str/trim (get-in triple-step [:result :stdout :text]))
   image-observation-text
   (get-in image-step [:result :stdout :text])
   emulation-text
   (get-in emulation-step [:result :stdout :text])
   emulation-observation
   (p15-s23-b3-llvm-linux-emulation-observation emulation-text)
   tool-snapshot-text
   (get-in hashes-step [:result :stdout :text])
   tool-snapshot
   (p15-s23-b3-llvm-linux-tool-snapshot tool-snapshot-text)
   _
   (when-not
    (and
     (str/includes? image-observation-text image)
     (str/includes? clang-version-text "20.1.8")
     (str/includes? llvm-version-text "20.1.8")
     (= target observed-triple)
     emulation-observation
     (p15-s23-b3-llvm-linux-tool-snapshot-valid?
      tool-snapshot-text
      tool-snapshot)
     (p15-s23-b3-llvm-linux-elf-bytes-valid?
      (:bytes object-snapshot)
      :object)
     (p15-s23-b3-llvm-linux-elf-bytes-valid?
      (:bytes executable-snapshot)
      :executable)
     (p15-s23-b3-llvm-linux-elf-header-valid? object-header :object)
     (p15-s23-b3-llvm-linux-elf-header-valid?
      executable-header
      :executable))
    (p15-s23-b3-llvm-fail!
     "B3-TARGET"
     source-path
     {}
     {:missing-fact :pinned-llvm-20-1-8-linux-toolchain,
      :observed-format :target-or-tool-version-mismatch}))
   expected-exit
   (:expected-exit-code lowering)
   observed-exit
   (get-in run-step [:result :exit-code])
   _
   (when-not
    (= expected-exit observed-exit)
    (p15-s23-b3-llvm-fail!
     "B14-DIFFERENTIAL"
     source-path
     {}
     {:missing-fact :linux-process-result-parity,
      :expected-exit-code expected-exit,
      :exit-code observed-exit}))
   records
   (mapv
    :record
    [version-step
     info-step
     image-step
     clang-step
     llvm-step
     triple-step
     hashes-step
     emulation-step
     object-step
     link-step
     object-header-step
     executable-header-step
     sections-step
     providers-step
     run-step])
   object-content
   (p15-s23-b3-llvm-snapshot-content object-snapshot)
   executable-content
   (p15-s23-b3-llvm-snapshot-content executable-snapshot)]
  (assoc
   state
   :object-header
   object-header
   :executable-header
   executable-header
   :clang-version-text
   clang-version-text
   :llvm-version-text
   llvm-version-text
   :observed-triple
   observed-triple
   :image-observation-text
   image-observation-text
   :emulation-text
   emulation-text
   :emulation-observation
   emulation-observation
   :tool-snapshot-text
   tool-snapshot-text
   :tool-snapshot
   tool-snapshot
   :expected-exit
   expected-exit
   :observed-exit
   observed-exit
   :records
   records
   :object-content
   object-content
   :executable-content
   executable-content)))
