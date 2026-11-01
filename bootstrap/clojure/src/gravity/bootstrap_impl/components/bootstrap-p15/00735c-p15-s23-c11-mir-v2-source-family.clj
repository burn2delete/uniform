(defn p15-s23-c11-mir-v2-source-family!
  "A semantic family gate on already authenticated Core, never MIR authority."
  [source-path checked-core version]
  (when (= 2 version)
    (let [parameter (p15-s23-c11-mir-entry-parameter! source-path checked-core)]
      (p15-s23-c11-mir-require!
       (bool-branch-join/source-family? checked-core (some? parameter))
       "C11-MODULE" source-path checked-core
       :v2-bool-branch-join-source-family)))
  (when (= 3 version)
    (p15-s23-c11-mir-require-trusted-carrier! source-path :flat-bool-original-core checked-core :reject)
    (p15-s23-c11-mir-require!
     (bool-branch-join/flat-sequence-source-family? checked-core)
     "C11-MODULE" source-path checked-core :v3-flat-bool-sequence-source-family))
  nil)
