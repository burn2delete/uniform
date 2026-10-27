(ns gravity.checked-core.bool-branch-join
  "Semantic grammar on owner-authenticated Core; no authentication or safety authority.")

(defn source-family? [core parameter?]
  (let [nodes (:core-nodes core) root (last nodes)
        literals (filter #(= :literal (:source-operation %)) nodes)]
    (and (or (true? parameter?) (false? parameter?))
         (vector? nodes)
         (= (if parameter?
              [:parameter :local :truthy :literal :literal :if :function]
              [:literal :truthy :literal :literal :if :function])
            (mapv :source-operation nodes))
         (= :gravity/bool (get-in root [:type :return]))
         (= (if parameter? [:gravity/bool] []) (get-in root [:type :params]))
         (every? #(= :gravity/bool (:type %)) (butlast nodes))
         (every? #(identical? Boolean (class (get-in % [:attributes :value]))) literals))))
