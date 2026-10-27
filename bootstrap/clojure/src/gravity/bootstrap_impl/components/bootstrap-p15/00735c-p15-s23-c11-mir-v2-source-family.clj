(defn p15-s23-c11-mir-v2-source-family!
  "A semantic family gate on already authenticated Core, never MIR authority."
  [source-path checked-core version]
  (when (= 2 version)
    (let [nodes (:core-nodes checked-core)
          parameter (p15-s23-c11-mir-entry-parameter! source-path checked-core)
          operations (mapv :source-operation nodes)
          root (last nodes)
          literals (filter #(= :literal (:source-operation %)) nodes)]
      (p15-s23-c11-mir-require!
       (and (vector? nodes)
            (= (if parameter
                 [:parameter :local :truthy :literal :literal :if :function]
                 [:literal :truthy :literal :literal :if :function])
               operations)
            (= :gravity/bool (get-in root [:type :return]))
            (= (if parameter [:gravity/bool] []) (get-in root [:type :params]))
            (every? #(= :gravity/bool (:type %)) (butlast nodes))
            (every? #(identical? Boolean (class (get-in % [:attributes :value]))) literals))
       "C11-MODULE" source-path checked-core
       :v2-bool-branch-join-source-family)))
  nil)
