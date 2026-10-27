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
