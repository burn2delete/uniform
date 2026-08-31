(defn- p15-s23-b3-llvm-reference-oracle!
  [candidate context lowering]
  (p15-s23-b3-llvm-require-authority!
   candidate (:source-path context) :execute-reference-oracle)
  (let [source-path (:source-path context)
        packet
        (stage2-runtime-derived-packet
         source-path (:source-text context) :llvm-x86_64-linux)
        packet-context
         (p15-s23-closed-runtime-packet-context
         source-path (:source-text context) :llvm-x86_64-linux)
        result (get-in packet [:closed-plan-execution-record
                               :entrypoint-result])
        expected-exit (p15-s23-b3-llvm-scalar-exit-code result)]
    (when-not (and (p15-s23-closed-runtime-packet-authentic?
                    packet packet-context)
                   (or (nil? result)
                       (boolean? result)
                       (p15-s23-b3-llvm-signed-i64? result))
                   (<= 0 expected-exit 255)
                   (= result (:semantic-result lowering))
                   (= expected-exit (:expected-exit-code lowering))
                   (empty? (get-in packet
                                   [:closed-plan-execution-record :stdout])))
      (p15-s23-b3-llvm-fail!
       "B14-DIFFERENTIAL" source-path {}
       {:missing-fact :reference-mir-lowering-semantic-parity
        :expected-exit-code (:expected-exit-code lowering)}))
    {:artifact :gravity/b14-reference-oracle
     :status :passed
     :reference-result result
     :expected-exit-code expected-exit
     :reference-result-hash
     (p15-s23-c11-mir-digest result)
     :reference-packet-id (:packet-id packet)
     :stdout-byte-count 0
     :clojure-seed-boundary? true}))

(defn- p15-s23-b3-llvm-stable-toolchain-projection
  "Return only replay-stable toolchain/program facts.  Docker transcripts,
  kernel text, and process-record bookkeeping remain in the evidence envelope
  but never become semantic identity inputs."
  [toolchain]
  {:status (:status toolchain)
   :image (:image toolchain)
   :platform (:platform toolchain)
   :pull-policy (:pull-policy toolchain)
   :network (:network toolchain)
   :target (:target toolchain)
   :layout (:layout toolchain)
   :commands (:commands toolchain)
   :container
   (select-keys (:container toolchain)
                [:platform :network :pull-policy :workspace-mount
                 :architecture :emulation-observation])
   :emulation
   (select-keys (:qemu-binfmt toolchain)
                [:status :machine :binfmt-state :qemu-state])
   :tool-paths (select-keys (:tool-paths toolchain) [:paths])
   :tool-hashes (select-keys (:tool-hashes toolchain) [:hashes])
   :llvm-ir (select-keys (:llvm-ir toolchain) [:content-hash :byte-count])
   :elf-object
   (select-keys (:elf-object toolchain)
                [:content-hash :byte-count :format :architecture :header])
   :elf-executable
   (select-keys (:elf-executable toolchain)
                [:content-hash :byte-count :format :architecture :header])
   :process-result
   (select-keys (:process-result toolchain)
                [:expected-exit-code :observed-exit-code :stdout-hash
                 :stderr-hash :matched?])})

(defn p15-s23-b3-llvm-semantic-input
  [artifact]
  (let [base
        (dissoc artifact :artifact-id :semantic-id
                :actual-path-binding-id :actual-path-provenance)]
    (cond-> base
      (contains? base :c13-c14-b1-packet)
      (update :c13-c14-b1-packet
              p15-s23-c13-c14-b1-reproducible-projection)
      true
      (update :toolchain-evidence dissoc
              :physical-tool-provenance :actual-publication-path)
      (contains? base :toolchain-evidence)
      (update :toolchain-evidence
              p15-s23-b3-llvm-stable-toolchain-projection))))

(defn p15-s23-b3-llvm-artifact-id
  [artifact]
  (p15-s23-c11-mir-digest
   (p15-s23-b3-llvm-semantic-input artifact)))

(defn- p15-s23-b3-llvm-replay-projection
  "Stable C11 -> C13 -> C14 -> B1 -> B3 replay identity.  This projection
  intentionally excludes volatile Docker host/container transcripts while
  retaining every target, tool, program, ELF, and process fact required by
  the development replay contract."
  [artifact]
  (let [lowering (:lowering artifact)
        b13-files (get-in artifact [:b13-record :artifact-files])
        process-result (get-in artifact [:b14-record :process-result])]
    {:kind (:kind artifact)
     :target (:target artifact)
     :target-policy
     (select-keys (:target-policy artifact)
                  [:canonical-target :target :target-triple :data-layout
                   :cpu :features :object-format :architecture :abi
                   :calling-convention :unwind-strategy :minimum-os-version])
     :c13-c14-b1
     (p15-s23-c13-c14-b1-reproducible-projection
      (:c13-c14-b1-packet artifact))
     :lowering
     {:llvm-ir-content-hash
      (str "sha256:" (sha256-hex (:llvm-ir lowering)))
      :expected-exit-code (:expected-exit-code lowering)
      :semantic-result (:semantic-result lowering)
      :result-type (:result-type lowering)}
     :toolchain
     (p15-s23-b3-llvm-stable-toolchain-projection
      (:toolchain-evidence artifact))
     :artifact-files
     (into (sorted-map)
           (map (fn [[kind file]]
                  [kind (select-keys file
                                     [:artifact-kind :logical-path :status
                                      :content-hash :byte-count :format
                                      :architecture :header])]))
           b13-files)
     :process-result
     (select-keys process-result
                  [:expected-exit-code :observed-exit-code :stdout-hash
                   :stderr-hash :matched?])
     :claims (:claims artifact)}))

(defn p15-s23-b3-llvm-actual-path-binding-id
  [semantic-id actual-path-provenance]
  (p15-s23-c11-mir-digest
   {:kind :gravity/b3-llvm-actual-path-binding
    :semantic-id semantic-id
    :actual-path-provenance actual-path-provenance}))

(defn- p15-s23-b3-llvm-content-binding
  [value]
  {:content-id (p15-s23-c11-mir-digest value)
   :entry-count (if (coll? value) (count value) 1)})

(defn- p15-s23-b3-llvm-c11-verifier-record
  [c11-report]
  (select-keys c11-report
               [:status :mir-id :semantic-replay-parity
                :execution-tcb :independent-verifier
                :b1-preflight]))
