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
