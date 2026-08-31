(ns gravity.cli.compile-command
  "Dispatch for the bootstrap compile command."
  (:refer-clojure :exclude [run!]))

(defn- operation-value
  [resolve-operation operation]
  (var-get (resolve-operation operation)))

(defn- invoke
  [resolve-operation operation & arguments]
  (apply (resolve-operation operation) arguments))

(defn run!
  [resolve-operation arguments]
  (let [path (second arguments)
        request (invoke resolve-operation
                        'p18-t04-parse-compile-request arguments)
        {:keys [target output-path target-requested? lowering-mode]} request]
    (if (= :verified-mir lowering-mode)
      (prn
       (invoke resolve-operation
               'p18-t04-compile-experimental-verified-mir-c-target-file!
               request))
      (if target-requested?
        (cond
          (= :jvm target)
          (if lowering-mode
            (prn (invoke resolve-operation
                         'p18-t04-compile-jvm-target-file!
                         path output-path target lowering-mode))
            (if output-path
              (prn (invoke resolve-operation
                           'p18-t04-compile-executable-file!
                           path output-path))
              (prn (invoke resolve-operation 'compile-file path))))

          (= (operation-value resolve-operation 'js-ts-backend-target) target)
          (prn (invoke resolve-operation
                       'p18-t04-compile-js-ts-target-file!
                       path output-path target lowering-mode))

          (contains? (operation-value resolve-operation
                                      'c-backend-supported-targets)
                     target)
          (prn (invoke resolve-operation
                       'p18-t04-compile-c-target-file!
                       path output-path target lowering-mode))

          :else
          (let [span (invoke resolve-operation 'source-span path 0)]
            (invoke
             resolve-operation
             'fail!
             "C14-TARGET"
             "requested compile target is unsupported"
             {:severity :error
              :stage :target-selection
              :diagnostic-family :c14-target-lowering
              :source-span span
              :primary {:span span}
              :target target
              :backend nil
              :supported-targets [:jvm :c :c-hosted :c11 :js :js-ts]
              :missing-fact :supported-target
              :remediation
              (str "Select jvm, c, js, or js-ts; other documented targets "
                   "remain unimplemented.")})))
        (if output-path
          (prn (invoke resolve-operation
                       'p18-t04-compile-executable-file!
                       path output-path))
          (prn (invoke resolve-operation 'compile-file path)))))))
