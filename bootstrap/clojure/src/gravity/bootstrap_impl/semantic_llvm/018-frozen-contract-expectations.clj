(defn p15-s23-b3-llvm-expected-source-target-selection
  []
  {:source-declaration-target :jvm
   :requested-lowering-target :llvm
   :selection :explicit-bootstrap-seed-target-override
   :reason :checked-core-seed-contract
   :direct-source-declared-llvm? false})

(defn p15-s23-b3-llvm-expected-profile-contract
  []
  {:name :hosted :validated? true})

(defn p15-s23-b3-llvm-expected-target-contract
  []
  (merge
   {:request :llvm
    :canonical-target :llvm-x86_64-linux
    :triple (:target-triple p15-s23-b3-llvm-policy)
    :data-layout (:data-layout p15-s23-b3-llvm-policy)
    :cpu (:cpu p15-s23-b3-llvm-policy)
    :features (:features p15-s23-b3-llvm-policy)
    :object-format (:object-format p15-s23-b3-llvm-policy)
    :architecture (:architecture p15-s23-b3-llvm-policy)
    :relocation-model (:relocation-model p15-s23-b3-llvm-policy)
    :code-model (:code-model p15-s23-b3-llvm-policy)
    :optimization-level (:optimization-level p15-s23-b3-llvm-policy)
    :minimum-os-version (:minimum-os-version p15-s23-b3-llvm-policy)
    :sanitizers (:sanitizers p15-s23-b3-llvm-policy)
    :instrumentation (:instrumentation p15-s23-b3-llvm-policy)
    :backend :gravity.backend/llvm
    :tier :experimental
    :exposure :internal}
   (p15-s23-b3-llvm-expected-source-target-selection)))

(defn p15-s23-b3-llvm-expected-b3-target-record
  []
  (select-keys p15-s23-b3-llvm-policy
               [:target-triple :data-layout :cpu :features
                :object-format :architecture :relocation-model
                :code-model :optimization-level :minimum-os-version
                :sanitizers :instrumentation]))

(defn p15-s23-b3-llvm-expected-b13-target
  []
  {:triple (:target-triple p15-s23-b3-llvm-policy)
   :canonical-target :llvm-x86_64-linux
   :architecture :x86_64 :object-format :elf
   :minimum-os-version :not-applicable
   :cpu (:cpu p15-s23-b3-llvm-policy)
   :features (:features p15-s23-b3-llvm-policy)
   :relocation-model (:relocation-model p15-s23-b3-llvm-policy)
   :code-model (:code-model p15-s23-b3-llvm-policy)
   :optimization-level (:optimization-level p15-s23-b3-llvm-policy)
   :sanitizers (:sanitizers p15-s23-b3-llvm-policy)
   :instrumentation (:instrumentation p15-s23-b3-llvm-policy)})

(defn p15-s23-b3-llvm-expected-build-target
  []
  (merge
   (p15-s23-b3-llvm-expected-b13-target)
   {:data-layout (:data-layout p15-s23-b3-llvm-policy)
    :calling-convention :sysv-amd64
    :integer-carrier :i64
    :process-result :i32
    :gravity-exception-unwind :none
    :platform-unwind-metadata :dwarf-cfi
    :tls-model :not-applicable}))

(defn p15-s23-b3-llvm-expected-runtime-contract
  []
  {:gravity-runtime-providers []
   :platform-runtime-providers
   [:linux/process-startup :linux/elf-loader :linux/glibc-2.36]
   :status :no-gravity-helpers-platform-runtime-required
   :full-conformance? false})

(defn p15-s23-b3-llvm-expected-abi-contract
  []
  {:calling-convention :sysv-amd64
   :object-format :elf :architecture :x86_64
   :integer-carrier :i64 :process-result :i32
   :gravity-exception-unwind :none
   :platform-unwind-metadata :dwarf-cfi})

(defn p15-s23-b3-llvm-expected-b3-abi-record
  []
  {:calling-convention :sysv-amd64
   :entrypoint 'main :parameter-types [] :return-type :i32
   :integer-carrier :i64 :process-result-range [0 255]
   :gravity-exception-unwind :none
   :platform-unwind-metadata :dwarf-cfi
   :tls-model :not-applicable})

(defn p15-s23-b3-llvm-expected-provider-contract
  []
  {:semantic []
   :build [:llvm-clang-20.1.8 :llvm-llc-20.1.8 :llvm-opt-20.1.8
           :llvm-as-20.1.8 :llvm-dis-20.1.8 :llvm-readobj-20.1.8
           :llvm-objdump-20.1.8 :llvm-lld-20.1.8
           :linux-x86_64-process-loader]
   :platform [:linux/process-startup :linux/elf-loader
              :linux/glibc-2.36]})
