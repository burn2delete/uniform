(ns gravity.checked-core.bool-branch-join
  "Semantic grammars on owner-authenticated Core; no authentication or safety authority.")

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

(defn flat-sequence-source-family?
  "Canonical explicit-do grammar only; caller first authenticates native Core."
  [core]
  (let [nodes (:core-nodes core)]
    (and (vector? nodes) (<= 3 (count nodes))
         (let [n (- (count nodes) 2) literals (subvec nodes 0 n)
               sequence-node (nth nodes n) root (peek nodes)
               ids (mapv :node-id nodes) literal-ids (subvec ids 0 n)]
           (and (= (count ids) (count (set ids)))
                (not (contains? core :resolved-parameter))
                (= [] (:lexical-binding-records core))
                (= [(:node-id root)] (:root-node-ids core))
                (= :function (:source-operation root))
                (= {:params [] :return :gravity/bool :latent-effects #{}
                    :capabilities #{} :throws #{}
                    :ownership-constraints #{:persistent-immutable-shareable}
                    :profile-constraints #{:hosted}} (:type root))
                (= [:functions 'main] (:path root))
                (= [(:node-id sequence-node)] (:operands root))
                (= :do (:source-operation sequence-node))
                (= :gravity/bool (:type sequence-node))
                (= [:functions 'main :body 0] (:path sequence-node))
                (= n (get-in sequence-node [:attributes :body-count]))
                (vector? (:operands sequence-node))
                (= literal-ids (:operands sequence-node))
                (every? true?
                  (map-indexed
                    (fn [i node]
                      (and (= :literal (:source-operation node)) (= :gravity/bool (:type node))
                           (= [:functions 'main :body 0 :body i] (:path node))
                           (= [] (:operands node))
                           (identical? Boolean (class (get-in node [:attributes :value]))))) literals)))))))
