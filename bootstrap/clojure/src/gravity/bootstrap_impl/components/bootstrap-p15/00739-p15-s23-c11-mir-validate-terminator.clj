(defn p15-s23-c11-mir-validate-terminator!
  [source-path checked-core block expected]
  (gravity.mir/validate-terminator!
    #'p15-s23-c11-mir-require! p15-s23-c11-mir-terminator-keys
    source-path checked-core block expected))

(defn p15-s23-c11-mir-validate-cfg!
  [source-path checked-core mir envelope operation-products]
  (gravity.mir/validate-cfg!
    #'p15-s23-c11-mir-require! #'p15-s23-c11-mir-validate-terminator!
    source-path checked-core mir envelope operation-products))

(defn p15-s23-c11-mir-valid-dominance-edge?
  ([edge blocks operation-by-id definitions position-by-id join]
   (p15-s23-c11-mir-valid-dominance-edge? edge blocks operation-by-id definitions position-by-id join nil))
  ([edge blocks operation-by-id definitions position-by-id join parameter]
   (gravity.mir/valid-dominance-edge? edge blocks operation-by-id definitions position-by-id join parameter)))

(defn p15-s23-c11-mir-valid-terminator-use?
  [use blocks operation-by-id position-by-id]
  (gravity.mir/valid-terminator-use? use blocks operation-by-id position-by-id))
