(ns gravity.self-hosting.module-census-test
  "Complete development runtime manifest validation without a census allowance."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh01-stage2-runtime-iteration-test]))

(def ^:private runtime-plan
  (delay
    (let [path "bootstrap/gravity/src/gravity/runtime.gravity"
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))
(defn- invoke [name & arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :module-census-test} @runtime-plan name (vec arguments)))
(defn- fixture []
  ((ns-resolve 'gravity.self-hosting.sh01-stage2-runtime-iteration-test 'module-literal-runtime-input)))
(defn- with-literals [n]
  (let [{:keys [plan root]} (fixture)
        roots (mapv #(assoc (root (+ % 3) :literal (symbol (str "extra" %))) :type :gravity.type/bool) (range n))
        initializers (mapv (fn [root] {:ordinal (:ordinal root) :binding-id (:binding-id root)
                                      :instruction {:op :literal :value true}}) roots)]
    (-> plan
        (update-in [:module-initialization :roots] into roots)
        (update-in [:module-initialization :initializers] into initializers))))
(defn- census [plan] (invoke 'p15-s23-runtime-module-census plan))
(defn- execute [plan] (invoke 'p15-s23-runtime-execute-closed-plan plan))

(deftest complete-many-root-census-and-execution
  (let [plan (with-literals 4097)
        roots (get-in plan [:module-initialization :roots])
        literals (filterv #(= :literal (:kind %)) roots)
        checked (census plan)
        result (execute plan)
        expected (mapv :value (:items (:initializer (fixture))))]
    (is (= :accepted (:status checked)))
    (is (not (contains? checked :work-count)))
    (is (true? (= (into {} (map (juxt :binding-id identity) literals)) (:definitions checked))))
    (is (true? (= literals (mapv :definition (:initializers checked)))))
    (is (= :complete (:status result)))
    (is (= [expected expected] (:entrypoint-result result)))
    (is (= "" (:stdout result)))
    (is (true? (= (mapv :binding-id literals)
                  (get-in result [:module-initialization :initialized-binding-ids]))))
    (is (= 4098 (count (get-in result [:module-initialization :validated-constructor-occurrences]))))))

(defn- main-last [plan]
  (let [roots (get-in plan [:module-initialization :roots])
        reordered (vec (concat (subvec roots 0 2) (subvec roots 3) [(get roots 2)]))
        ordered (mapv #(assoc %2 :ordinal %1) (range) reordered)
        ordinals (into {} (map (juxt :binding-id :ordinal) ordered))]
    (-> plan
        (assoc-in [:module-initialization :roots] ordered)
        (assoc-in [:functions 'main :module-root] (peek ordered))
        (update-in [:module-initialization :initializers]
                   #(mapv (fn [item] (assoc item :ordinal (ordinals (:binding-id item)))) %)))))

(deftest invalid-manifests-fail-before-initialization
  (let [small (with-literals 4) wide (with-literals 4097)
        late-main (main-last small)
        last-root (dec (count (get-in small [:module-initialization :roots])))
        last-initializer (dec (count (get-in small [:module-initialization :initializers])))
        cases [[(assoc-in wide [:module-initialization :roots 4099 :definition-source] nil)
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :ordinal] -1)
                :module-initialization-root-source]
               [(-> small
                    (assoc-in [:module-initialization :roots last-root :binding-id] "binding-modes")
                    (assoc-in [:module-initialization :initializers last-initializer :binding-id] "binding-modes"))
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :name] 'modes)
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :definition-core-node-id] "initializer-modes")
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :value-core-node-id] "definition-modes")
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :definition-core-node-id]
                          (get-in small [:module-initialization :roots last-root :value-core-node-id]))
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :type] :gravity.type/keyword)
                :module-initialization-type]
               [(assoc-in small [:module-initialization :initializers last-initializer :ordinal] -1)
                :module-initialization-source-order]
               [(assoc-in small [:module-initialization :initializers last-initializer :instruction] {:op :foreign})
                :unsupported-module-initializer]
               [(update-in small [:module-initialization :initializers] pop)
                :module-initialization-source-order]
               [(assoc-in late-main [:functions 'main :params] nil)
                :module-initialization-function-source]
               [(assoc-in late-main [:functions 'main :type-input :function-core-node-id] "forged")
                :module-initialization-function-source]
               [(assoc-in small [:functions 'orphan] (get-in small [:functions 'main]))
                :module-initialization-census]
               [(assoc-in small [:module-initialization :extra] true)
                :invalid-module-initialization-manifest]]
        started (atom 0)
        scoped-var (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped-var]
    (is (= :accepted (:status (census late-main))))
    (with-redefs-fn
      {scoped-var (fn [& arguments]
                    (when (= 'p15-s23-runtime-initialize-module (nth arguments 2))
                      (swap! started inc))
                    (apply original arguments))}
      (fn []
        ;; Positive observation proves this is the actual initialization path.
        (is (= :complete (:status (execute small))))
        (is (= 1 @started))
        (doseq [[changed reason] cases]
          (testing (str reason)
            (reset! started 0)
            (let [checked (census changed) result (execute changed)]
              (is (= :rejected (:status checked) (:status result)))
              (is (= reason (:reason checked) (get-in result [:diagnostic :reason])))
              (is (not (contains? checked :work-count)))
              (is (= "" (:stdout result)))
              (is (zero? @started)))))
        (doseq [[value element reason] [[:duplicate :gravity.type/keyword :duplicate-set-literal-keyword]
                                        ["duplicate" :gravity.type/string :duplicate-set-literal-string]]]
          (reset! started 0)
          (let [changed (-> small
                            (assoc-in [:module-initialization :roots last-root :type]
                                      {:kind :gravity.type/set :shape :literal :element-types [element element]})
                            (assoc-in [:module-initialization :initializers last-initializer :instruction]
                                      {:op :set-literal :items [{:op :literal :value value} {:op :literal :value value}]}))
                checked (census changed) result (execute changed)]
            (is (= :rejected (:status checked) (:status result)))
            (is (= :unsupported-module-initializer (:reason checked) (get-in result [:diagnostic :reason])))
            (is (= reason (:initializer-reason checked)))
            (is (not (contains? checked :work-count)))
            (is (zero? @started))))))))
