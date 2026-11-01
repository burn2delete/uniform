(ns gravity.bootstrap.module-literals
  "Temporary seed literal data, after parsing and before ordinary plan lowering.
  The caller owns diagnostics and all source/profile/admission decisions.")

(defn materialize!
  "Validate the seed literal shape and return inert data. The diagnostic Var
  is read at the original refusal site; it must throw on unsupported shape."
  [fail-var value]
  (loop [pending [[value false]]]
    (if (empty? pending)
      (letfn [(materialize [item]
                (cond
                  (and (seq? item) (= 'quote (first item))) (second item)
                  (map? item) (into {} (map (fn [[k v]] [(materialize k) (materialize v)])) item)
                  (vector? item) (mapv materialize item)
                  (set? item) (into #{} (map materialize) item)
                  :else item))]
        (materialize value))
      (let [[item quoted?] (peek pending)
            quote? (and (not quoted?) (seq? item) (= 'quote (first item)) (= 2 (count item)))
            collection? (or (vector? item) (map? item) (set? item) (and quoted? (seq? item)))]
        (when-not (or quote? collection? (nil? item) (boolean? item) (number? item)
                      (char? item) (keyword? item) (string? item) (and quoted? (symbol? item)))
          ((deref fail-var) :unsupported-literal-data {}))
        (recur (cond
                 quote? (conj (pop pending) [(second item) true])
                 collection? (into (pop pending) (map #(vector % quoted?))
                                   (if (map? item) (mapcat identity item) item))
                 :else (pop pending)))))))

(alter-meta! #'materialize! assoc :gravity/namespace-contract
             {:contracts ["D1" "D2" "D3" "D8" "D9" "L1" "L2" "L3" "C1" "BOOT1" "BOOT2" "BOOT8" "TEST2"]
              :contract-boundary :temporary-seed-module-literal-data
              :inputs [:caller-diagnostic-var :already-parsed-native-form]
              :outputs [:literal-data :caller-diagnostic]
              :diagnostic-owner :gravity.bootstrap
              :diagnostics ["BOOT2006"]
              :dependency-direction :bootstrap-to-module-literals
              :admission :none
              :preserves [:literal-values :quote-inertness :caller-refusal-shape]
              :unsupported [:reader-decoding :source-authentication :profile-legality
                            :checked-core :backend-execution :migration :self-hosting
                            :release :seed-retirement]})
