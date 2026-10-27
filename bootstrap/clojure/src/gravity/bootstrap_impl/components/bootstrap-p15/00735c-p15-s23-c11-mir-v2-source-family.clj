(require '[gravity.checked-core.bool-branch-join :as bool-branch-join])

(defn p15-s23-c11-mir-v2-source-family!
  "A semantic family gate on already authenticated Core, never MIR authority."
  [source-path checked-core version]
  (when (= 2 version)
    (let [parameter (p15-s23-c11-mir-entry-parameter! source-path checked-core)]
      (p15-s23-c11-mir-require!
       (bool-branch-join/source-family? checked-core (some? parameter))
       "C11-MODULE" source-path checked-core
       :v2-bool-branch-join-source-family)))
  nil)
