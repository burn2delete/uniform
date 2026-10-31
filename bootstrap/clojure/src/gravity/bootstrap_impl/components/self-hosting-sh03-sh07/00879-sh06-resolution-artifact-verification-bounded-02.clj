
(defn-
 sh06-resolution-artifact-verification-bounded*
 ([artifact construction?]
  (sh06-resolution-artifact-verification-bounded*
   artifact
   construction?
   :gravity.bootstrap/legacy))
 ([artifact construction? context]
  (with-sh06-resolution-transport-bounds
   (#'gravity.resolution/verify-current*
    artifact
    construction?
    context
    (resolution-stage-operations)))))
