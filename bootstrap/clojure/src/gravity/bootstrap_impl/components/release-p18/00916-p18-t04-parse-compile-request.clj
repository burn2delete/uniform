

(defn p18-t04-parse-compile-request
  "Parse the public compile command without selecting a backend implicitly.

  The historical form (`compile source -o output`) remains the JVM-backed
  bootstrap command.  A target flag makes the backend choice explicit; the C
  target is routed through the real C backend below and all unsupported target
  values are rejected by its structured C14 boundary before lowering."
  [args]
  (let [[_ source-path & options] args]
    (when-not source-path
      (p18-t04-fail! "P18T04002"
                     {:source "bin/gravity"
                      :command args
                      :missing-fields [:source-path]}))
    (loop [remaining (vec options)
           target nil
           output-path nil
           lowering-mode nil
           output-option nil]
      (if (empty? remaining)
        (let [target-argument target
              lowering-argument lowering-mode
              target (some-> target str/lower-case keyword
                             js-ts-backend-canonical-target)
              lowering-mode (some-> lowering-mode str/lower-case keyword)]
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
           :lowering-requested? (some? lowering-mode)})
        (let [option (first remaining)
              rest-options (subvec remaining 1)]
          (cond
            (#{"-o" "--output"} option)
            (do
              (when (or (empty? rest-options) output-path)
                (p18-t04-fail! "P18T04002"
                               {:source source-path
                                :command args
                                :option option
                                :missing-fields (if (empty? rest-options)
                                                  [:output-path]
                                                  [:duplicate-output-option])}))
              (let [candidate (first rest-options)]
                (when-not (p18-t04-output-path-allowed? candidate)
                  (p18-t04-fail! "P18T04002"
                                 {:source source-path
                                  :command args
                                  :output-path candidate
                                  :allowed-output-roots ["target/"
                                                          "<current-directory>"]}))
                (recur (subvec rest-options 1) target candidate
                       lowering-mode option)))

            (= "--target" option)
            (do
              (when (or (empty? rest-options) target)
                (p18-t04-fail! "P18T04002"
                               {:source source-path
                                :command args
                                :option option
                                :missing-fields (if (empty? rest-options)
                                                  [:target]
                                                  [:duplicate-target-option])}))
              (let [candidate (first rest-options)]
                (when (str/blank? (str candidate))
                  (p18-t04-fail! "P18T04002"
                                 {:source source-path
                                  :command args
                                  :option option
                                  :missing-fields [:target]}))
                (recur (subvec rest-options 1) candidate output-path
                       lowering-mode output-option)))

            (= "--lowering" option)
            (do
              (when (or (empty? rest-options) lowering-mode)
                (p18-t04-fail! "P18T04002"
                               {:source source-path
                                :command args
                                :option option
                                :missing-fields (if (empty? rest-options)
                                                  [:lowering-mode]
                                                  [:duplicate-lowering-option])}))
              (let [candidate (first rest-options)]
                (when (str/blank? (str candidate))
                  (p18-t04-fail! "P18T04002"
                                 {:source source-path
                                  :command args
                                  :option option
                                  :missing-fields [:lowering-mode]}))
                (recur (subvec rest-options 1) target output-path
                       candidate output-option)))

            :else
            (p18-t04-fail! "P18T04002"
                           {:source source-path
                            :command args
                            :unsupported-option option
                            :expected-forms [["compile"
                                              "<file.qst|file.gravity>"]
                                             ["compile"
                                              "<file.qst|file.gravity>"
                                              "-o" "<executable>"]
                                             ["compile"
                                              "<file.qst|file.gravity>"
                                              "--target" "c"
                                              "-o" "<executable>"]
                                             ["compile"
                                              "<file.qst|file.gravity>"
                                             "--target" "c"
                                             "--lowering" "runtime-derived"
                                              "-o" "<executable>"]]})))))))

(defn p18-t04-parse-compile-output-path
  [args]
  (:output-path (p18-t04-parse-compile-request args)))