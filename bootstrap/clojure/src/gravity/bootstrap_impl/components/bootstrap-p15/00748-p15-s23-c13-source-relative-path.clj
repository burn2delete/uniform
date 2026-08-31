

;; ---------------------------------------------------------------------------
;; Authentic C11 -> C13 -> C14 -> B1 packet for bounded LLVM
;; ---------------------------------------------------------------------------

(def p15-s23-c13-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")
(def p15-s23-c14-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")
(def p15-s23-b1-source-relative-path
  "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity")

(def p15-s23-c13-source-byte-count 126249)
(def p15-s23-c13-expected-source-content-hash
  "sha256:0cac3e273677061bf11144f3dc3520d93c4c67dbf607e5f03d68312f35265aad")
(def p15-s23-c13-expected-plan-semantic-hash
  "sha256:3ca443670addd91ff7e271b29f5e0e22f19b2bb9dd4df38154e31e04d794fb5f")
(def p15-s23-c13-expected-functions-semantic-hash
  "sha256:3f32029679ca91ca0e8da01f0e326f624adaa42a46b00df17007624918478204")
(def p15-s23-c13-expected-builder-semantic-hash
  "sha256:91f68328307a940c1e1bc5c6ea9d5c0c2a90bedd057c55c49f0fc9955e8cdae3")
(def p15-s23-c13-builder-function
  'c13-build-bounded-identity-optimized-mir)
(def p15-s23-c13-required-functions
  {'c13-bounded-identity-input-valid?
   {:arity 2 :params ['mir 'evidence]}
   'c13-operation-ids
   {:arity 2 :params ['instructions 'result]}
   'c13-operation-order-from-blocks
   {:arity 3 :params ['blocks 'block-order 'result]}
   'c13-bounded-operation-count?
   {:arity 1 :params ['mir]}
   'c13-bounded-operation-order
   {:arity 1 :params ['mir]}
   'c13-unique-operation-ids?
   {:arity 2 :params ['operation-ids 'seen]}
   'c13-bounded-mir-operation-shape-valid?
   {:arity 2 :params ['mir 'operation-order]}
   'c13-build-bounded-identity-optimized-mir
   {:arity 2 :params ['mir 'evidence]}
   'sh16-c13-evidence-input-valid?
   {:arity 1 :params ['request]}
   'sh16-build-c13-evidence-boundary
   {:arity 1 :params ['request]}
   'sh16-verify-c13-evidence-boundary
   {:arity 2 :params ['request 'candidate]}})

(def p15-s23-c14-source-byte-count 173349)
(def p15-s23-c14-expected-source-content-hash
  "sha256:18843bb01315cd5a55902fd8c9f38875d93aeba3c2b2fd1e3252afa2107114aa")
(def p15-s23-c14-expected-plan-semantic-hash
  "sha256:4f99da2865ca59349ea873bf96511960c1c80a6fc572d92a390323bfa5517589")
(def p15-s23-c14-expected-functions-semantic-hash
  "sha256:8c0e7f82172fc104ee4debaa6a0991018ca8076c8c50c8e2c00dc60d676e0980")
(def p15-s23-c14-expected-builder-semantic-hash
  "sha256:982cf0d77a2026c55699d3ac44a0d0d9f0b29f1a214e2b54ab9bc2d97c5ab5b7")
(def p15-s23-c14-builder-function
  'c14-build-bounded-llvm-lowering-record)
(def p15-s23-c14-required-functions
  {'c14-bounded-optimized-input-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-llvm-bounded-mir-surface-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-llvm-process-result-valid?
   {:arity 3 :params ['mir 'operations 'operations-by-id]}
   'c14-llvm-first-invalid-constant
   {:arity 1 :params ['operations]}
   'c14-bounded-policy-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-build-bounded-llvm-lowering-record
   {:arity 2 :params ['optimized 'policy]}})

(def p15-s23-c14-c-builder-function
  'c14-build-bounded-c-lowering-record)
(def p15-s23-c14-c-expected-builder-semantic-hash
  "sha256:3209bb1605d40c7a466a53eb01d6cf7a4d243210ce6a0c189ceb3b15564f09ee")
(def p15-s23-c14-c-required-functions
  {'c14-bounded-optimized-input-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-c-target-valid?
   {:arity 1 :params ['target]}
   'c14-c-dependencies-valid?
   {:arity 2 :params ['optimized 'dependencies]}
   'c14-c-bounded-mir-surface-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-c-target-envelope-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-c-policy-core-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-c-contract-bindings-match?
   {:arity 2 :params ['optimized 'policy]}
   'c14-c-policy-evidence-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-c-policy-scope-valid?
   {:arity 1 :params ['policy]}
   'c14-bounded-c-policy-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-build-bounded-c-lowering-record
   {:arity 2 :params ['optimized 'policy]}})

(def p15-s23-c14-wasm-builder-function
  'c14-build-bounded-wasm-lowering-record)
(def p15-s23-c14-wasm-expected-builder-semantic-hash
  "sha256:fe20574f102acb55063e64223715adf2e2fa40a25e9e147616ee554f9989b3fa")
(def p15-s23-c14-wasm-required-functions
  {'c14-bounded-optimized-input-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-wasm-target-valid? {:arity 1 :params ['target]}
   'c14-wasm-bounded-mir-surface-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-wasm-dependencies-valid?
   {:arity 2 :params ['optimized 'dependencies]}
   'c14-wasm-contract-bindings-match?
   {:arity 2 :params ['optimized 'policy]}
   'c14-wasm-policy-evidence-valid?
   {:arity 2 :params ['optimized 'policy]}
   'c14-wasm-policy-valid? {:arity 2 :params ['optimized 'policy]}
   'c14-wasm-first-invalid-constant {:arity 1 :params ['operations]}
   'c14-build-bounded-wasm-lowering-record
   {:arity 2 :params ['optimized 'policy]}})

(def p15-s23-b1-source-byte-count 141751)
(def p15-s23-b1-expected-source-content-hash
  "sha256:75a1fd3364636249919f9853bd3ace2e77fc5e69aaf7289b12d754924ed9eefe")
(def p15-s23-b1-expected-plan-semantic-hash
  "sha256:9f6e095b79061119d9bbdeae40776656c40b822dbb67f2f5ead5104084a628eb")
(def p15-s23-b1-expected-functions-semantic-hash
  "sha256:6d81c3e571e8bbdde906b98d12e350f3626bad2c9851002a4c3bc3e5c1eec084")
(def p15-s23-b1-expected-builder-semantic-hash
  "sha256:53b3d1b61fc09ce735169ed5f5324bb2839da27a85862e471e7b7dfc0ebb4a33")
(def p15-s23-b1-builder-function
  'b1-build-bounded-llvm-authenticated-packet)
(def p15-s23-b1-required-functions
  {'b1-bounded-c14-input-valid?
   {:arity 1 :params ['lowering]}
   'b1-bounded-llvm-manifest-valid?
   {:arity 1 :params ['backend-manifest]}
   'b1-build-bounded-llvm-authenticated-packet
   {:arity 2 :params ['lowering 'backend-manifest]}})

(def p15-s23-b1-c-builder-function
  'b1-build-bounded-c-authenticated-packet)
(def p15-s23-b1-c-expected-builder-semantic-hash
  "sha256:9fa86940d7874c4260fea5e6c38bc6d01a5bbd3115bdd1e2c8c8d2bbf4d82760")
(def p15-s23-b1-c-required-functions
  {'b1-bounded-c14-c-input-valid?
   {:arity 1 :params ['lowering]}
   'b1-bounded-c-manifest-valid?
   {:arity 1 :params ['backend-manifest]}
   'b1-build-bounded-c-authenticated-packet
   {:arity 2 :params ['lowering 'backend-manifest]}})

(def p15-s23-b1-wasm-builder-function
  'b1-build-bounded-wasm-authenticated-packet)
(def p15-s23-b1-wasm-expected-builder-semantic-hash
  "sha256:e958c2fca6c24ef6eea840cd75b38137fe047cd06a49677175be70855f29e50d")
(def p15-s23-b1-wasm-required-functions
  {'b1-wasm-content-bindings-valid?
   {:arity 2 :params ['remaining 'bindings]}
   'b1-c14-wasm-target-valid? {:arity 1 :params ['target]}
   'b1-c14-wasm-contract-bindings-valid?
   {:arity 1 :params ['request]}
   'b1-c14-wasm-dependencies-valid? {:arity 1 :params ['request]}
   'b1-bounded-c14-wasm-request-valid? {:arity 1 :params ['request]}
   'b1-bounded-c14-wasm-eligibility-valid?
   {:arity 1 :params ['eligibility]}
   'b1-c14-wasm-source-rule-valid? {:arity 1 :params ['rule]}
   'b1-c14-wasm-payload-valid? {:arity 1 :params ['lowering]}
   'b1-bounded-c14-wasm-input-valid? {:arity 1 :params ['lowering]}
   'b1-bounded-wasm-manifest-valid?
   {:arity 1 :params ['backend-manifest]}
   'b1-build-bounded-wasm-authenticated-packet
   {:arity 2 :params ['lowering 'backend-manifest]}})

(def p15-s23-sh02-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")
(def p15-s23-sh02-source-byte-count 59495)
(def p15-s23-sh02-expected-source-content-hash
  "sha256:04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71")
(def p15-s23-sh02-expected-plan-semantic-hash
  "sha256:125e012806bddf996f23e357bd33309c9bbd40927ce0f2c841e69b39c1740922")
(def p15-s23-sh02-expected-functions-semantic-hash
  "sha256:e88f53c2994f5d8d4577f6df9f6531a750c2808ef0a2b4b5d297f64e7b45e26e")
(def p15-s23-sh02-expected-builder-semantic-hash
  "sha256:19b21ed1e94563631c25b502f5297fa1e33070f0a62fefc480bcba5984b02a7a")
(def p15-s23-sh02-expected-verifier-semantic-hash
  "sha256:e52b201a81ef82f857aaabc68cb2d6a8f0f4505f853c555816023f5dad294a77")
(def p15-s23-sh02-expected-function-count 72)
(def p15-s23-sh02-builder-function
  'authenticated-envelope-build-template)
(def p15-s23-sh02-verifier-function
  'authenticated-envelope-verify-template)
(def p15-s23-sh02-required-functions
  {'authenticated-envelope-build-template
   {:arity 1 :params ['descriptor]}
   'authenticated-envelope-verify-template
   {:arity 3 :params ['descriptor 'artifact-template 'digest-requests]}})