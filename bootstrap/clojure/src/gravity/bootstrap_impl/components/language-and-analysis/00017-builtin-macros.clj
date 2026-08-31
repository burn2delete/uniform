

(def builtin-macros
  (-> macro-expansion/builtin-macros
      (assoc-in ['defn :expander] builtin-defn-output)
      (assoc-in ['when :expander] builtin-when-output)
      (assoc-in ['-> :expander] builtin-thread-first-output)))