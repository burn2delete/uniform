

(defn p18-t04-parse-compile-request
  "Parse the public compile command without selecting a backend implicitly.

  The historical form (`compile source -o output`) remains the JVM-backed
  bootstrap command.  A target flag makes the backend choice explicit; the C
  target is routed through the real C backend. JS aliases select the checked
  module producer unless legacy runtime-derived lowering is explicit."
  [args]
  (let [{:keys [source-path target-argument output-path output-option
                lowering-argument]}
        ((requiring-resolve 'gravity.cli.compile-request/parse-shape!)
         args
         (fn [facts]
           (p18-t04-fail! "P18T04002" facts)))
        target (some-> target-argument str/lower-case keyword
                       js-ts-backend-canonical-target)
        lowering-mode (some-> lowering-argument str/lower-case keyword)]
    (when (and lowering-mode
               (not= :verified-mir lowering-mode)
               (not (or (contains? c-backend-supported-targets target)
                        (= js-ts-backend-target target)
                        (= jvm-backend-target target))))
      (p18-t04-fail! "P18T04002"
                     {:source source-path
                      :command args
                      :target target
                      :lowering-mode lowering-mode
                      :missing-fields [:runtime-derived-target]
                      :remediation "Use --target c, jvm, js, or js-ts with --lowering runtime-derived; the verified-MIR C candidate is not publicly exposed."}))
    {:source-path source-path
     :target target
     :target-argument target-argument
     :target-requested? (some? target)
     :output-path output-path
     :output-option output-option
     :lowering-mode lowering-mode
     :lowering-argument lowering-argument
     :lowering-requested? (some? lowering-mode)}))

(defn p18-t04-parse-compile-output-path
  [args]
  (:output-path (p18-t04-parse-compile-request args)))
