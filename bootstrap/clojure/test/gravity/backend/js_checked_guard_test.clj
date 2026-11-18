(ns gravity.backend.js-checked-guard-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]))

(def b6-owner "language/compiler/src/gravity/backend/b6_javascript_typescript_backend_design.gravity")

(defn assert-original-guard [path invoke]
  (let [source {:span {:start {:line 10 :column 3}}}
        application {:source {:path path} :mir-module {:source-map {:add source}}}
        guard {:op-id :guard :opcode :runtime-check :result :sum-token
               :operands [:left :right]
               :facts {:runtime-check-id :check :guarded-operation-id :add}}
        add {:op-id :add :opcode :integer-add :type :gravity/i64 :source source
             :result :sum :operands [:sum-token :left :right]
             :facts {:runtime-check-id :check}}
        lower (fn [operation previous]
                (invoke 'b6-js-operation [application operation previous]))]
    (is (= {:kind :checked-add-guard :check-id :check :guarded-operation-id :add}
           (select-keys (lower guard nil) [:kind :check-id :guarded-operation-id])))
    (is (= {:kind :guarded-add :type :bigint :checked-sum :sum-token
            :operands [:left :right] :guard-operation-id :guard :check-id :check}
           (select-keys (lower add guard)
                        [:kind :type :checked-sum :operands :guard-operation-id :check-id])))
    (doseq [previous [nil (assoc guard :opcode :local) (assoc guard :result :other-token)
                      (assoc guard :operands [:right :left])
                      (assoc-in guard [:facts :guarded-operation-id] :other-add)
                      (assoc-in guard [:facts :runtime-check-id] :other-check)]]
      (let [result (lower add previous)
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= ["B6-NUMERIC" :original-checked-add-guard-token :add path]
               [(:diagnostic-id diagnostic) (:missing-fact diagnostic)
                (:operation-id diagnostic) (get-in diagnostic [:source-span :source])]))))))

(deftest checked-add-requires-original-guard
  (let [path (.getCanonicalPath (io/file "examples/checked-js/acceptance.gravity"))
        invoke ((requiring-resolve 'gravity.backend.js-guard-seed/compile-owner) b6-owner)]
    (assert-original-guard path invoke)))
