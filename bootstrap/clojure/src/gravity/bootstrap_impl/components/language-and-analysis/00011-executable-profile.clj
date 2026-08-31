

(defn executable-profile!
  [source-path module forms]
  (when-not (supported-profiles (:profile module))
    (if (some #(contains-form-op? 'host-reflect %) forms)
      (fail! "P7-HOST-REFLECTION"
             "host reflection is not legal for this bootstrap profile"
             {:source-span (source-span source-path 0)
              :profile (:profile module)
              :remediation "Use :hosted for the stage0 hosted bootstrap or remove host reflection."})
      (fail! "P1-PROFILE-UNSUPPORTED"
             "stage0 execution supports only the :hosted profile"
             {:source-span (source-span source-path 0)
              :profile (:profile module)
              :supported supported-profiles
              :remediation "Use (:profile :hosted) for executable stage0 modules."}))))
(declare macro-expansion-ops
         local-macro-symbol
         parse-param-list
         bind-macro-arguments
         expand-template-items
         macro-env-value
         expand-template
         parse-syntax-template
         builtin-defn-output
         builtin-when-output
         thread-first-step
         builtin-thread-first-output
         builtin-macros
         built-in-registry
         parse-defmacro-form
         macro-registry
         macro-namespace-entry
         macro-build-effect-record
         macro-build-grants
         assert-build-effects!
         collect-let-bindings
         assert-hygiene!
         assert-generated-profile!
         assert-generated-unsafe!
         expand-macro-form
         expansion-generated-origin
         macro-call
         expand-child-form
         expand-form-children
         expansion-trace-record
         distinct-by-pr-str
         expand-syntax-object
         macro-source-artifact-from-records)

(defn local-macro-symbol
  [module name]
  (macro-expansion/local-macro-symbol module name))