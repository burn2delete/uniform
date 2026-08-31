

(defmacro with-sh06-resolution-transport-bounds
  [& body]
  `(binding [p15-s23-c6c10-max-carrier-nodes
             (:maximum-carrier-nodes sh06-resolution-transport-bounds)
             p15-s23-c6c10-max-carrier-depth
             (:maximum-carrier-depth sh06-resolution-transport-bounds)
             p15-s23-c6c10-max-container-width
             (:maximum-container-width sh06-resolution-transport-bounds)
             p15-s23-c6c10-max-digest-requests 8192]
     ~@body))