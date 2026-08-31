(defn- p15-s23-b3-llvm-linux-artifact-record
  [c11-artifact checked-core context bridge-packet lowering binding options]
  (let [source-path (:source-path context)
        toolchain (p15-s23-b3-llvm-linux-development-evidence
                   source-path lowering options)
        toolchain-complete? (= :complete (:status toolchain))
        artifact-status (if toolchain-complete?
                          :development-emulation-observed
                          :incomplete)
        bridge-id (:artifact-id bridge-packet)
        c13-id (get-in bridge-packet [:c13 :artifact-id])
        c14-id (get-in bridge-packet [:c14 :artifact-id])
        b1-id (get-in bridge-packet [:b1 :artifact-id])
        lowering-id
        (p15-s23-c11-mir-digest
         (dissoc lowering :clojure-seed-boundary? :self-hosted?))
        b3-record
        {:artifact :gravity/b3-internal-llvm-x86_64-linux-record
         :schema-version 1
         :status artifact-status
         :target (p15-s23-b3-llvm-expected-b3-target-record)
         :lowering-id lowering-id
         :gravity-source-rule (dissoc binding :plan :source-path)
         :abi-record (p15-s23-b3-llvm-expected-b3-abi-record)
         :runtime-record (p15-s23-b3-llvm-expected-runtime-contract)
         :provider-record {:build-authority
                           {:providers
                            (:build (p15-s23-b3-llvm-expected-provider-contract))}
                           :program-effects #{}
                           :program-capabilities #{}}
         :pass-record (p15-s23-b3-llvm-expected-pass-record)
         :diagnostics []
         :claims (:claims p15-s23-b3-llvm-policy)
         :clojure-seed-boundary? true
         :self-hosted? false}
        b13-record
        {:artifact :gravity/b13-bounded-llvm-emission-record
         :schema-version 1
         :status artifact-status
         :build-id (p15-s23-c11-mir-digest
                    {:kind :gravity/b13-bounded-llvm-build-identity
                     :lowering-id lowering-id
                     :bridge-id bridge-id})
         :target (p15-s23-b3-llvm-expected-b13-target)
         :inputs {:checked-core (:artifact-id checked-core)
                  :mir (:mir-id c11-artifact)
                  :c13 c13-id :c14 c14-id :b1 b1-id
                  :authenticated-packet bridge-id
                  :lowering lowering-id}
         :artifact-files
         {:llvm-ir {:artifact-kind :llvm-ir :logical-path "program.ll"
                    :status :constructed :content-hash
                    (str "sha256:" (sha256-hex (:llvm-ir lowering)))
                    :byte-count
                    (alength (.getBytes ^String (:llvm-ir lowering)
                                       java.nio.charset.StandardCharsets/UTF_8))
                    :bytes (.getBytes ^String (:llvm-ir lowering)
                                      java.nio.charset.StandardCharsets/UTF_8)}
          :object
          (if toolchain-complete?
            (merge {:artifact-kind :elf-x86_64-object
                    :logical-path "program.o" :status :observed}
                   (select-keys (:elf-object toolchain)
                                [:bytes :byte-count :content-hash :format
                                 :architecture :header]))
            {:artifact-kind :elf-x86_64-object :logical-path "program.o"
             :status :runtime-derived-required :format :elf
             :architecture :x86_64})
          :executable
          (if toolchain-complete?
            (merge {:artifact-kind :elf-x86_64-executable
                    :logical-path "program" :status :observed}
                   (select-keys (:elf-executable toolchain)
                                [:bytes :byte-count :content-hash :format
                                 :architecture :header]))
            {:artifact-kind :elf-x86_64-executable
             :logical-path "program" :status :runtime-derived-required
             :format :elf :architecture :x86_64})}
         :providers (:platform-runtime-providers p15-s23-b3-llvm-policy)
         :abi-layout {:calling-convention :sysv-amd64
                      :data-layout (:data-layout p15-s23-b3-llvm-policy)
                      :unwind-strategy :dwarf-cfi}
         :claims (:claims p15-s23-b3-llvm-policy)
         :clojure-seed-boundary? true :self-hosted? false}
        b14-record
        {:artifact :gravity/b14-bounded-llvm-differential-record
         :schema-version 1 :status artifact-status
         :evidence-id
         (when toolchain-complete?
           (p15-s23-c11-mir-digest
            {:kind :gravity/b14-bounded-llvm-differential-record
             :process-result (:process-result toolchain)
             :elf-object (select-keys (:elf-object toolchain)
                                      [:content-hash :byte-count :format
                                       :architecture :header])
             :elf-executable (select-keys (:elf-executable toolchain)
                                          [:content-hash :byte-count :format
                                           :architecture :header])}))
         :target (p15-s23-b3-llvm-expected-b13-target)
         :process-result
         (if toolchain-complete?
           (:process-result toolchain)
           {:status :runtime-derived-required})
         :same-result?
         (if toolchain-complete?
           (= (:expected-exit-code (:process-result toolchain))
              (:observed-exit-code (:process-result toolchain)))
           :not-established)
         :clojure-seed-boundary? true :self-hosted? false}
        c18-record
        {:artifact :gravity/c18-bounded-llvm-risk-trust-record
         :schema-version 1
         :status (if toolchain-complete?
                   :internal-experimental-observed
                   :incomplete)
         :evidence-id
         (when toolchain-complete?
           (p15-s23-c11-mir-digest
            {:kind :gravity/c18-bounded-llvm-risk-trust-record
             :toolchain (select-keys toolchain
                                     [:image :platform :target
                                      :tool-hashes :qemu-binfmt])
             :provenance (:provenance toolchain)
             :replay (:replay toolchain)}))
         :release-gate :closed :public-target-gate :closed
         :self-hosting-gate :closed
         :required-evidence (:required-evidence p15-s23-b3-llvm-policy)
         :clojure-seed-boundary? true :self-hosted? false}
        provenance {:source source-path
                    :c11-source (get-in c11-artifact
                                        [:provenance :actual-paths :c11-source])
                    :c13-source (get-in bridge-packet
                                        [:actual-path-provenance :c13-source])
                    :c14-source (get-in bridge-packet
                                        [:actual-path-provenance :c14-source])
                    :b1-source (get-in bridge-packet
                                        [:actual-path-provenance :b1-source])
                    :b3-source (:source-path binding)}
        base0 {:kind (:final-artifact-kind p15-s23-b3-llvm-policy)
              :schema-version 1
              :status artifact-status
              :target (:canonical-target p15-s23-b3-llvm-policy)
              :target-policy p15-s23-b3-llvm-policy
              :c11 c11-artifact
              :c13-c14-b1-packet bridge-packet
              :c14-request (get-in bridge-packet [:c14 :request])
              :b1-packet (:b1 bridge-packet)
              :b3-record b3-record :b13-record b13-record
              :b14-record b14-record :c18-record c18-record
              :lowering lowering :toolchain-evidence toolchain
              :actual-path-provenance provenance
              :diagnostics [] :seed-boundary? true
              :clojure-seed-boundary? true :c11-llvm-credit? false
              :target-lowering-credit? false :backend-credit? false
              :public-target? false :release-credit? false
              :self-hosted? false :whole-language? false
              :claims (:claims p15-s23-b3-llvm-policy)}
        replay-projection (p15-s23-b3-llvm-replay-projection base0)
        base (assoc base0
                    :replay-projection replay-projection
                    :replay-projection-id
                    (p15-s23-c11-mir-digest replay-projection))
        semantic-id (p15-s23-b3-llvm-artifact-id base)
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind (:kind base) :schema-version 1 :semantic-id semantic-id})]
    (assoc base :semantic-id semantic-id :artifact-id artifact-id
           :actual-path-binding-id
           (p15-s23-b3-llvm-actual-path-binding-id semantic-id provenance))))
