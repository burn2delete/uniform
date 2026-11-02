
(defn p15-s23-stage2-compiler-artifact-plan-context?
  [plan]
  (and (true? (:compiler-artifact-plan? plan))
       (= :gravity/stage2-compiler-artifact-plan (:kind plan))
       (= :meta (:profile (:module plan)))
       (= :jvm (:target (:module plan)))
       (= #{} (:effects (:module plan)))
       (= #{} (:capabilities (:module plan)))
       (= :safe (:safety (:module plan)))
       (= :p15-s23-stage2-expression-lowering
          (:stage (:compiler plan)))
       (or
        ;; Frozen B47/V1 authority remains byte-for-byte compatible.
        (= 'gravity.checked-core (get-in plan [:module :module]))
        ;; The SH02-SH06 bootstrap stages are admitted only through their
        ;; closed independently pinned source/plan/current-emitter table.
        (p15-s23-stage2-pinned-augmented-plan-authorized? plan)
        ;; Attempt24 executes the separately authored emitter artifact only
        ;; when its complete source/compiler identity is current.  Merely
        ;; naming the module does not grant compiler-only builtin authority.
        (and (= 'gravity.bootstrap.p15-s23.emitter
                (get-in plan [:module :module]))
             (= p15-s23-stage2-compiler-artifact-expected-source-content-hash
                (get-in plan [:source :sha256]))
             (= p15-s23-stage2-compiler-artifact-expected-source-content-hash
                (get-in plan [:compiler :source-content-hash]))
             (= p15-s23-stage2-compiler-artifact-expected-semantic-hash
                (get-in plan [:compiler :semantic-hash]))
             (= (p15-s23-stage2-compiler-artifact-expected-artifact-hash)
                (get-in plan [:compiler :artifact-hash]))))))

(defn p15-s23-stage2-runtime-augmented-plan-context?
  [runtime plan]
  (or (p15-s23-stage2-compiler-artifact-plan-context? plan)
      (p15-s23-sh07-b51-runtime-augmented-plan-authorized? runtime plan)))

(defn- p15-s23-stage2-runtime-add
  [args]
  (gravity.runtime/hosted-add args))

(defn- p15-s23-stage2-runtime-subtract
  [args]
  (gravity.runtime/hosted-subtract args))

(defn- p15-s23-stage2-runtime-multiply
  [args]
  (gravity.runtime/hosted-multiply args))

(defn- p15-s23-stage2-runtime-divide
  [args]
  (gravity.runtime/hosted-divide args))

(defn- p15-s23-stage2-runtime-equal
  [args]
  (gravity.runtime/hosted-equal args))

(defn- p15-s23-stage2-runtime-less-than
  [args]
  (gravity.runtime/hosted-less-than args))

(defn- p15-s23-stage2-runtime-greater-than
  [args]
  (gravity.runtime/hosted-greater-than args))

(defn- p15-s23-stage2-runtime-less-than-or-equal
  [args]
  (gravity.runtime/hosted-less-than-or-equal args))

(defn- p15-s23-stage2-runtime-greater-than-or-equal
  [args]
  (gravity.runtime/hosted-greater-than-or-equal args))

(defn- p15-s23-stage2-runtime-str
  [args]
  (gravity.runtime/hosted-str args))

(defn- p15-s23-stage2-runtime-fail-builtin-error!
  [plan callee ex]
  (gravity.runtime/fail-builtin-error! #'fail! plan callee ex))

(defn- p15-s23-stage2-runtime-invoke-get
  ([plan collection key]
   (gravity.runtime/hosted-get #'p15-s23-stage2-runtime-fail-builtin-error! plan collection key))
  ([plan collection key not-found]
   (gravity.runtime/hosted-get #'p15-s23-stage2-runtime-fail-builtin-error! plan collection key not-found)))
