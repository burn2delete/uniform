

(def p15-s23-reference-runtime-expected-derived-facts-hash
  "sha256:66928c7eff9d8e5e136c820f465d2bc164decadeb2cfbcd1fdbb04e01b5fe7d1")

(def p15-s23-reference-runtime-expected-function-hashes
  {'main
   "sha256:fbf50ab4c4fa4fc8405e030a2e0e621a970cdf64ba22d0406b216fb8d3a9d510"
   'p15-s23-runtime-concat
   "sha256:5173d24e43121241cb727629889e0f5f693e3af7b0be58a67328bb659c36fb66"
   'p15-s23-runtime-evaluate-arguments
   "sha256:462652c55c0fef78d045f01c60502c37f9c26038469c05102bbf265b17d3bc94"
   'p15-s23-runtime-evaluate-bindings
   "sha256:5c520b294277ecb0dfe7d4ef32aab43d132506ae388c5f751ba231cb79b8efc5"
   'p15-s23-runtime-evaluate-closed-instruction
   "sha256:ed8380d2290e1deb251f7bbecc4b964908dd2fbf465ee721142f62a9c0a5a2d8"
   'p15-s23-runtime-evaluate-sequence
   "sha256:24029a3eb0e3e235170208c9d2cef0948224b191b34609b3a01d0ed670a9521c"
   'p15-s23-runtime-execute-closed-plan
   "sha256:c4fa3df78d5d90b0d3301c592c10018a449ba1dace51dfa7af3f9c3299fadc0e"
   'p15-s23-runtime-format-value
   "sha256:aad26457a87d3a799a2d1812d937e3ff4fb826dfcf447ae29a0ff5c0e71ae5e6"
   'p15-s23-runtime-println-output
   "sha256:b680821f5b5b26fa15ada288f49104c5b9ce29f1e34a0df96133ad744cbb2144"
   'p15-s23-runtime-println-two
   "sha256:a714e5ade9d974cc910b9c8bf03e79fafba168c5d064562197f5a175169edd14"
   'p15-s23-runtime-println-value
   "sha256:a1140b1b75f955c274414ed0d76de81fc3e8e0497fc0ac993f684ed03f6f394b"})

(def p15-s23-stage2-runtime-artifact-expected-closed-function-hashes
  {'p15-s23-runtime-evaluate-arguments
   "sha256:462652c55c0fef78d045f01c60502c37f9c26038469c05102bbf265b17d3bc94"
   'p15-s23-runtime-println-output
   "sha256:b680821f5b5b26fa15ada288f49104c5b9ce29f1e34a0df96133ad744cbb2144"
   'p15-s23-runtime-evaluate-bindings
   "sha256:5c520b294277ecb0dfe7d4ef32aab43d132506ae388c5f751ba231cb79b8efc5"
   'p15-s23-runtime-evaluate-sequence
   "sha256:24029a3eb0e3e235170208c9d2cef0948224b191b34609b3a01d0ed670a9521c"
   'p15-s23-runtime-evaluate-closed-instruction
   "sha256:ed8380d2290e1deb251f7bbecc4b964908dd2fbf465ee721142f62a9c0a5a2d8"
   'p15-s23-runtime-execute-closed-plan
   "sha256:c4fa3df78d5d90b0d3301c592c10018a449ba1dace51dfa7af3f9c3299fadc0e"})

(def p15-s23-stage2-runtime-artifact-println-over-two-boundary
  :host-compatibility)

(def p15-s23-stage2-runtime-artifact-required-effects
  #{:memory/allocate :io/write})

(def p15-s23-stage2-runtime-artifact-required-capabilities
  #{:memory/allocator :io/stdout})

(declare p15-s23-closed-runtime-max-depth
         p15-s23-closed-runtime-max-nodes
         p15-s23-closed-runtime-plan-validation!)

(def p15-s23-stage2-runtime-artifact-required-function-shape
  {:arity 1
   :params ['value]
   :instructions [{:op :builtin-call
                   :function 'str
                   :args [{:op :local :name 'value}]}]})

(def p15-s23-stage2-runtime-artifact-required-concat-function-shape
  {:arity 2
   :params ['left 'right]
   :instructions [{:op :builtin-call
                   :function 'str
                   :args [{:op :local :name 'left}
                          {:op :local :name 'right}]}]})

(def p15-s23-stage2-runtime-artifact-required-println-function-shape
  {:arity 1
   :params ['value]
   :instructions [{:op :println
                   :effect :io/write
                   :capability :io/stdout
                   :args [{:op :builtin-call
                           :function 'str
                           :args [{:op :local :name 'value}]}]}]})

(def p15-s23-stage2-runtime-artifact-required-println-two-function-shape
  {:arity 2
   :params ['left 'right]
   :instructions [{:op :println
                   :effect :io/write
                   :capability :io/stdout
                   :args [{:op :builtin-call
                           :function 'str
                           :args [{:op :local :name 'left}
                                  {:op :literal :value " "}
                                  {:op :local :name 'right}]}]}]})