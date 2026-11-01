(declare sh03-reader-resolve-source-path sh03-reader-current-binding!)

(defn- p15-s23-c6c10-validate-current-reader-program!
  [request-source expected-path binding emitter-rule]
  ;; Only the existing Reader owner constructs this binding. This helper is
  ;; not a public acceptor of supplied plans, Reader products or reports.
  (let [contract c6-source-contract/current-reader-program
        expected (:binding contract)
        plan (:plan binding)
        exports (into {} (map (fn [[name _]]
                               [name (select-keys (get-in plan [:functions name]) [:arity :params])]))
                      (:exports contract))
        rule-hash (str "sha256:"
                       (sha256-hex (pr-str (c-backend-canonical-value (:emitter emitter-rule)))))
        rule-record (p15-s23-stage2-plan-emitter-rule-record (:emitter emitter-rule))]
    (when-not (= expected (select-keys binding (keys expected)))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-program-identities {}))
    (when-not (= expected-path (p15-s23-c6c10-canonical-file-path (:source-path binding)))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-source-path {}))
    (when-not (= (:exports contract) exports)
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-export-shapes {}))
    (when-not (and (= (get-in contract [:plan-emitter :source-content-hash])
                      p15-s23-stage2-compiler-expected-source-content-hash)
                   (= (get-in contract [:plan-emitter :source-byte-count])
                      p15-s23-stage2-compiler-expected-source-byte-count)
                   (= :complete (:status rule-record))
                   (= rule-record (:rule-record emitter-rule))
                   (= (get-in contract [:plan-emitter :source-rule-hash])
                      rule-hash (:source-rule-hash emitter-rule)
                      (get-in plan [:compiler :emitter-source-rule-hash]))
                   (= (:declared-expression-labels contract)
                      (select-keys (:compiler plan)
                                   (keys (:declared-expression-labels contract)))))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-plan-emitter-join {}))
    binding))

(defn p15-s23-c6c10-current-reader-program!
  [request-source]
  ;; Authenticate actual C6/compiler source inputs before and after the actual
  ;; selected-source Reader owner. No caller-selected path, pins or binding.
  (let [before (p15-s23-c6c10-authenticated-source-binding-inputs! request-source 2)
        path (p15-s23-c6c10-canonical-file-path (sh03-reader-resolve-source-path))
        binding (sh03-reader-current-binding! request-source path)
        after (p15-s23-c6c10-authenticated-source-binding-inputs! request-source 2)
        emitter-rule (:emitter-rule after)
        source-before (dissoc (:source-snapshot before) :source-text)
        source-after (dissoc (:source-snapshot after) :source-text)]
    (when-not (and (= source-before source-after)
                   (= (select-keys (:emitter-rule before) [:source-path :source-rule-hash])
                      (select-keys emitter-rule [:source-path :source-rule-hash])))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :stable-current-reader-dependency-inputs {}))
    (p15-s23-c6c10-validate-current-reader-program! request-source path binding emitter-rule)
    {:binding binding
     :invocation {:reader-source-path path
                  :authenticated-envelope-options {:source :development-current}}
     :verification-scope :selected-source-reader-plan
     ;; Actual envelope state must be obtained from fresh C2 products. A plan
     ;; match supplies neither a fabricated state nor frozen admission.
     :reader-result-executed? false :envelope-state-observed? false}))
