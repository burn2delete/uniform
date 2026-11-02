

(defn js-ts-backend-node-version!
  [source-path]
  (gravity.backend.js/node-version! #'js-ts-backend-run-node-process! #'js-ts-backend-fail! source-path))

(defn js-ts-backend-output-paths
  [output-path]
  (gravity.backend.js/output-paths output-path))

(defn js-ts-backend-delete-tree!
  [path]
  (gravity.backend.js/delete-tree! path))

(defn js-ts-backend-stage-files!
  [output-path paths contents source-path]
  (gravity.backend.js/stage-files! #'c-backend-output-path-allowed? #'js-ts-backend-fail! #'js-ts-backend-delete-tree! output-path paths contents source-path))
