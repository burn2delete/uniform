
(defn-
 sh06-resolution-artifact-verification-bounded*
 ([artifact construction?]
  (sh06-resolution-artifact-verification-bounded*
   artifact
   construction?
   :gravity.bootstrap/legacy))
 ([artifact construction? context]
  (#'gravity.resolution/verify-current*
   artifact
   construction?
   context
   (resolution-stage-operations))))
