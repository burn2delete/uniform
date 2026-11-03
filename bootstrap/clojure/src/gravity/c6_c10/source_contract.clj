(ns gravity.c6-c10.source-contract
  "Reviewed compiler-program identities, not Core or front-end admission.
   The Reader/expression dependency contract must be completed separately before
   the explicit current public Core constructor/verifier is wired.")

(def current-compiler-program
  ;; Historical source-contract DATA.  Public current-v2 construction uses the
  ;; explicit owner-produced development route below; this record remains
  ;; available to the fixed source-contract selector and legacy controls.
  {:version 2
   :source-relative-path "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity"
   :source-byte-count 186870
   :source-content-hash "sha256:0e7e30e15bc68dabdcee3cfea4c91740d82808204c5fe322d67ccd0ffdb76b98"
   :plan-semantic-hash "sha256:0cfce809aeefed1cbf34e8176dad01527ff1f8055ee43cff93c7ed634aeac4a9"
   :functions-semantic-hash "sha256:a85c2096fca9e456ffcd45b71c053c938e0c7f62d2082594493e0b13e0e6c821"
   :builder-semantic-hash "sha256:0153c36e3ec0dc9c121c5850b68477b8f0a3d87dcdd4e98f41e6a47ab4c60342"
   :verifier-semantic-hash "sha256:84ada992e0bf1bbd127b5927c699aa74e9f5aec82b75557d1e363f516408f0d4"
   :required-functions
   {'c6-c10-build-checked-core-template {:arity 1 :params ['input]}
    'c6-c10-verify-checked-core-template {:arity 3 :params ['input 'template 'requests]}}
   ;; These are the observed complete source manifest and existing Merkle wire
   ;; format. They are source identity/encoding, not a function/work allowance.
   :function-count 170 :chunk-size 64 :chunk-counts [64 64 42]})

(def current-compiler-program-abi
  "Stable ABI facts for the explicit current-source owner.

   The owner derives source bytes and all semantic identities at invocation;
   only these exported names and shapes are fixed here.  This is not a source
   pin and does not select the historical DATA record above."
  {:version 2
   :source-relative-path (:source-relative-path current-compiler-program)
   :builder-function 'c6-c10-build-checked-core-template
   :verifier-function 'c6-c10-verify-checked-core-template
   :required-functions (:required-functions current-compiler-program)})

(def flat-bool-current-compiler-program-abi
  "Stable ABI for the development-current v3 source owner.

   The v3 semantic family uses the same current Gravity C6 builder and
   verifier, but its source-family selector is distinct from the v2 route.
   Bytes, plan identities, and function identities are derived from the
   selected source at invocation; no historical v3 digest is used here."
  {:version 3
   :semantic-family :closed-flat-bool-literal-sequence
   :source-relative-path (:source-relative-path current-compiler-program)
   :builder-function (:builder-function current-compiler-program-abi)
   :verifier-function (:verifier-function current-compiler-program-abi)
   :required-functions (:required-functions current-compiler-program-abi)})

;; Independently reviewed observation of the changed C6 program. This record is
;; not selected by a public version3 route until the complete C6/C11 transaction
;; is installed and reviewed. Historical version1/version2 records are unchanged.
(def flat-bool-compiler-program
  {:version 3
   :semantic-family :closed-flat-bool-literal-sequence
   :source-relative-path "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity"
   :source-byte-count 186745
   :source-content-hash "sha256:7d14fef9a3ca0f47fd7ec301d96cd6c0b57c4c6a3f91a0839c673e8c702a6396"
   :plan-semantic-hash "sha256:478007025328dec1aaf5fd5546cdc4813e3c62d4abeccad1a1a9bec2a9b5b07f"
   :functions-semantic-hash "sha256:a507d12cfb35cbc219ce8b4f2690ca4633a75445a354f807d44202dde32f24d5"
   :builder-semantic-hash "sha256:724437315b8cefd7d0261691920f1f76685636e90ac5e5972fc83477a5e0f5ed"
   :verifier-semantic-hash "sha256:992165c95972cde92fbf34eea355333f389a76be6d28944dff6f75f4ff25296d"
   :required-functions (:required-functions current-compiler-program)
   :function-count 170 :chunk-size 64 :chunk-counts [64 64 42]})

;; Observed through the existing selected-source Reader owner at the current
;; main composition. These do not attest a Reader result or an SH02 envelope
;; and do not replace the historical Reader admission policy. The owner source
;; advanced after the earlier review record; v2 binds this exact current
;; snapshot while v1 remains on its historical pin.
(def current-reader-program
  {:source-relative-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
   :binding
   {:artifact :gravity/sh03-current-source-reader-plan-binding
    :status :complete :semantic-authority :gravity-source
    :compiled-by :clojure-stage0-seed
    :executed-by :clojure-stage2-generic-rule-runner
    :generic-bridge-residual? true :self-hosted? false
    :source-byte-count 246369
    :source-content-hash "sha256:eec041127cb2785f92e7e2c79d14350d869a0ea8a0194ea3da55f75d52099205"
    :plan-semantic-hash "sha256:cf23056d9f691c24fc7344445723d69e288dd1b755db3199086d4608323d4aa6"
    :functions-semantic-hash "sha256:7be1bd97600db2d9cab88e7ac8cbf10e9644fa2e120309a9b892e11c3c83a701"
    :function-count 232
    :function-names-hash "sha256:32401a608a596e84fac2663ea87efbfa2c758afa150c8a50242d84a6be92c78a"
    :function-shapes-hash "sha256:3ccb1d0fc93c71951a1660e3a6a6fe069c3166cce5f08d87bfb07b9d979555ee"
    :entrypoint-semantic-hash "sha256:6dddc9a5a656148c1edd3dde8eb4d31c8962ba5327ba0d60285acc8d40f4bea3"
    :verifier-semantic-hash "sha256:625bbeccf94f75fedbd6232e4a54b1a44435f06bf07352b8d19b1a35196038a7"
    :builtin-functions-hash "sha256:03394c173b55bcb279070adc77d0494ee334c42505d814593f46109000dc1400"
    :instruction-summary {:let 165 :if 743 :builtin-call 2031 :set-literal 50
                          :map-literal 301 :recur 117 :vector-literal 219 :loop 83
                          :quote 3 :literal 4222 :function-call 564 :local 2993}
    :observed-instruction-nodes 11491 :observed-instruction-depth 20}
   :exports
   {'sh03-read-source-unit {:arity 3 :params ['source-unit 'source-bytes 'reader-policy]}
    'sh03-verify-reader-result {:arity 4 :params ['source-unit 'source-bytes 'reader-policy 'result]}}
   :plan-emitter
   {:source-content-hash "sha256:57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527"
    :source-byte-count 128499
    :source-rule-hash "sha256:64acc2f135aa947398fb74b395f0135517fc5099bf6412b01a26ef1f2f47394f"}
   ;; These pre-existing plan fields are declared labels, not observations of
   ;; the separately required current expression-lowering emitter program.
   :declared-expression-labels
   {:emitter-source-content-hash "sha256:e273535bc06e5f67eb5a54ddcba0ef726814bc9dbe484df73bf7d3ab56d1e949"
    :emitter-semantic-hash "sha256:2004f388034a182483d7b9c28f4e6d6baa76126d29867c50183b0329fa5e0ddc"}})

;; The expression emitter also advanced in the current main composition. The
;; v2 handoff binds this observed source snapshot; v1 keeps its legacy route.
(def current-expression-program
  {:version 2
   :source-relative-path "bootstrap/gravity/p15_s23/emitter.gravity"
   :source-byte-count 1529728
   :source-content-hash "sha256:dad556ad561890e4c107144c1160966fd4440a65293ddbc186d47422ab273564"
   :semantic-hash "sha256:c36b13be7760b21e614ebf2025d84ef9c29343770d5e7d0f01d0a71832c41be2"
   :functions-semantic-hash "sha256:06c85e6c770abc1b283a0964fdcf92c1ff6ebf5540bbe9a88e8ec28f76609de4"
   :artifact-hash "sha256:2949abf023ad0b715607f73e77bde01f371ab1ab323bc278d310293c5c60952c"
   :function-count 699
   :function-names-hash "sha256:33ec54ac40041f7243f86a9d41ca39208e457a76dbc6a341a296567da440fe85"
   :instruction-summary {:let 1376 :if 7133 :builtin-call 32058 :set-literal 264
                         :map-literal 2204 :recur 884 :vector-literal 2335 :loop 811
                         :quote 474 :literal 44464 :function-call 3232 :local 31664}
   :required-functions
   {'p15-s23-compile-collection {:arity 4 :params ['emitter 'module 'locals 'form]}
    'p15-s23-compile-let {:arity 5 :params ['emitter 'module 'locals 'bindings 'body]}
    'p15-s23-compile-expr {:arity 4 :params ['emitter 'module 'locals 'form]}
    'p15-s23-compile-function {:arity 3 :params ['emitter 'module 'definition]}
    'p15-s23-summary-instructions {:arity 2 :params ['instructions 'summary]}
    'p15-s23-summary-map-entries {:arity 2 :params ['entries 'summary]}
    'p15-s23-summary-let-bindings {:arity 2 :params ['bindings 'summary]}
    'p15-s23-summary-instruction {:arity 2 :params ['instruction 'summary]}
    'p15-s23-assemble-functions {:arity 6 :params ['emitter 'module 'definitions 'functions 'bindings 'summary]}
    'p15-s23-assemble-plan-products {:arity 3 :params ['emitter 'module 'definitions]}}
   :required-export-semantic-hashes
   {'p15-s23-compile-let "sha256:f6c295e376e65964e26bc5eee619573e94aa0c279453626b14f060d9127038fb"
    'p15-s23-summary-let-bindings "sha256:02e4b22015b99448353142c9290ce6f982f87dd798ef76df5ca27811e9594fb6"
    'p15-s23-compile-collection "sha256:b743de74e05113363d2924a98a9ece9bf598d51585f9e167bbff1482587f1635"
    'p15-s23-assemble-functions "sha256:064bf88890612e7f185cee479938e8a32ccc44c0e299e495b8763c77f68513d0"
    'p15-s23-summary-instructions "sha256:629634307051947ff4221177448d702d97c9cf59e8c7cb9186a748a22a82d038"
    'p15-s23-compile-expr "sha256:65f994ce85de39f834c7df9a58e8e4f48eb97a6f9460d86d61107a2adfd18ca0"
    'p15-s23-summary-map-entries "sha256:7afa7ffd47385f8de87d262e22f502ef12eeed016feba9ad3108b56ff26ea655"
    'p15-s23-assemble-plan-products "sha256:94762513c25f336517725aafe44e5387f3b9cfba8914ea8d0648f7c9a683feea"
    'p15-s23-compile-function "sha256:a80b46f252d8621424553e95bcc6f42bae07427e409afeeff889a35c467ebb0c"
    'p15-s23-summary-instruction "sha256:9062748420f731b8f7c5f8483991a0a353c0fe3179a75d3c98ebff5ee36bfe7e"}
   :plan-emitter (:plan-emitter current-reader-program)
   :declared-plan-compiler-labels
   {:owner :gravity-source :stage :p15-s23-stage2-expression-lowering
    :compiled-by :clojure-stage0-seed :generic-bridge-residual? true
    :artifact-hash "sha256:e25ae2c9b7853627f8f9db45eb916b6b3775e28b51fbe6e29ecd734598ca8ac2"
    :source-content-hash "sha256:e273535bc06e5f67eb5a54ddcba0ef726814bc9dbe484df73bf7d3ab56d1e949"
    :semantic-hash "sha256:2004f388034a182483d7b9c28f4e6d6baa76126d29867c50183b0329fa5e0ddc"}})
