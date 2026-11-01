(ns gravity.c6-c10.source-contract
  "Reviewed compiler-program identities, not Core or front-end admission.
   The Reader/expression dependency contract must be completed separately before
   the explicit current public Core constructor/verifier is wired.")

(def current-compiler-program
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

;; Observed through the existing selected-source Reader owner, independently
;; reviewed as plan identities only. These do not attest a Reader result or an
;; SH02 envelope and do not replace the historical Reader admission policy.
(def current-reader-program
  {:source-relative-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
   :binding
   {:artifact :gravity/sh03-current-source-reader-plan-binding
    :status :complete :semantic-authority :gravity-source
    :compiled-by :clojure-stage0-seed
    :executed-by :clojure-stage2-generic-rule-runner
    :generic-bridge-residual? true :self-hosted? false
    :source-byte-count 231666
    :source-content-hash "sha256:a74e3d1d3bc3d7627493586c78367844b6ba41980cb65b2bee1638fe3273928a"
    :plan-semantic-hash "sha256:71f5ab0216a0030fa3d78eef5efdb14db58628636f3c430f4eb4e0d7b6b3576a"
    :functions-semantic-hash "sha256:f2ff5ef5c42e208e519bc2e1df85635982a2710f8626073a748089651d76bacb"
    :function-count 221
    :function-names-hash "sha256:2dd178c923bff9433d5c5af59d45d3d20edf2fe6384449c6a0b8c8d30dcebf76"
    :function-shapes-hash "sha256:4d03f0efe5bf682d51cbb91c783bdd24ca7b738b9a48ef4cab45b6dc3a4f7866"
    :entrypoint-semantic-hash "sha256:6dddc9a5a656148c1edd3dde8eb4d31c8962ba5327ba0d60285acc8d40f4bea3"
    :verifier-semantic-hash "sha256:625bbeccf94f75fedbd6232e4a54b1a44435f06bf07352b8d19b1a35196038a7"
    :builtin-functions-hash "sha256:03394c173b55bcb279070adc77d0494ee334c42505d814593f46109000dc1400"
    :instruction-summary {:let 150 :if 681 :builtin-call 1857 :set-literal 39
                          :map-literal 271 :recur 105 :vector-literal 195 :loop 77
                          :literal 3736 :function-call 537 :local 2803}
    :observed-instruction-nodes 10451 :observed-instruction-depth 20}
   :exports
   {'sh03-read-source-unit {:arity 3 :params ['source-unit 'source-bytes 'reader-policy]}
    'sh03-verify-reader-result {:arity 4 :params ['source-unit 'source-bytes 'reader-policy 'result]}}
   :plan-emitter
   {:source-content-hash "sha256:be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854"
    :source-byte-count 128447
    :source-rule-hash "sha256:40c39d9c65d9d23d442eb87f1e0610a87c24daf145e87ec6695f3c4023b3d42c"}
   ;; These pre-existing plan fields are declared labels, not observations of
   ;; the separately required current expression-lowering emitter program.
   :declared-expression-labels
   {:emitter-source-content-hash "sha256:e273535bc06e5f67eb5a54ddcba0ef726814bc9dbe484df73bf7d3ab56d1e949"
    :emitter-semantic-hash "sha256:2004f388034a182483d7b9c28f4e6d6baa76126d29867c50183b0329fa5e0ddc"}})

(def current-expression-program
  {:version 2
   :source-relative-path "bootstrap/gravity/p15_s23/emitter.gravity"
   :source-byte-count 1670330
   :source-content-hash "sha256:23d49699bf187d56714257f88056bc0805ed158c4a76e7ea85af660c41d39d9c"
   :semantic-hash "sha256:a9de1c01ed15a2c6a3bf9bef755767efeaf8f7bbca1a7c187c62f3a59f1dab71"
   :functions-semantic-hash "sha256:dccbfee8f6dd0e5ab6999e0218150e042132e98a1967bc90be340e58d52fdc42"
   :artifact-hash "sha256:012faac14da10c65740a09b8deddff68c4422f47761ee205c958ab16f0127283"
   :function-count 692
   :function-names-hash "sha256:4d4fd4e3de06a6aea26a974d183bb8fe63346356c4b271d05adb2b90764d5410"
   :instruction-summary {:let 1432 :if 7733 :builtin-call 35831 :set-literal 256
                         :map-literal 2435 :recur 887 :vector-literal 2334 :loop 815
                         :quote 471 :literal 49732 :function-call 3273 :local 36322}
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
