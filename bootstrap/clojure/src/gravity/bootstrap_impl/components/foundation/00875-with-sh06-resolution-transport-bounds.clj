

(defmacro with-sh06-resolution-transport-bounds
  [& body]
  ;; Compatibility spelling for existing SH06 callers; no quota bindings.
  `(do ~@body))
