(ns gravity.types.private-map-source-test
  "Compiler-free PMK source cells; native type and entry proof are explicit seams."
  (:require [clojure.test :refer [deftest is]]
            [gravity.types.private-map-cycle-test :as source]))

(defn model [{:keys [path prefix]}]
  (let [space (create-ns (symbol (str "gravity.types.private-map-source-model-" prefix)))
        defs (source/definitions path) named #(symbol (str prefix "-" %))
        validation (atom 0) proof-budgets (atom [])]
    (binding [*ns* space]
      (refer 'clojure.core)
      (doseq [name [(named "ft-all-true?") (named "all?") (named "typed-all")]]
        (intern space name #(every? true? %)))
      (intern space (named "lc-validate-types")
              (fn [xs] (swap! validation inc)
                {:status (if (every? keyword? xs) :accepted :rejected)}))
      (intern space (named "pmk-proof")
              (fn [& args] (swap! proof-budgets conj (last args))
                {:status :accepted :work-count 5 :key-domain-work 7 :proof {:source-model true}}))
      (doseq [suffix ["sse-ids?" "mkeys-optional-id?" "mkeys-binding?" "mkeys-ids?" "nq-ids?"
                      "pmk-source" "pmk-header" "pmk-constraint" "pmk-call-fact"]]
        (eval (get defs (named suffix)))))
    {:space space :prefix prefix :defs defs :validation validation :proof-budgets proof-budgets}))
(def models (delay (mapv model source/owners)))
(defn invoke [m suffix & args]
  (apply (var-get (ns-resolve (:space m) (symbol (str (:prefix m) "-" suffix)))) args))

(defn fixture []
  (let [module {:profile :meta :target :jvm}
        record {:function-syntax-id :private :function-core-node-id :private-core}
        binding {:binding-id :keys-binding :definition-syntax-id :keys-definition :name 'keys
                 :binding-class :core :kind :var :namespace 'gravity.core
                 :package {:name 'gravity/core :version "bootstrap-contract-v1"}
                 :type-ref :gravity.type/core-var :safety :safe :effects [] :capabilities []
                 :profile-set [:meta] :target-set [:all]}
        node {:node-id :call :core-form :call :source {:form-id :call-form :syntax-id :call-syntax}
              :children [:operator :argument] :resolved-binding-ids [:keys-binding]
              :attributes {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1]
                           :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
              :evaluation {:kind :operator-then-arguments :owner-function-syntax-id :private
                           :order [{:index 0 :core-node-id :operator} {:index 1 :core-node-id :argument}]}}
        operator {:node-id :operator :core-form :reference :source {:form-id :operator-form :syntax-id :operator-syntax}
                  :resolved-binding-ids [:keys-binding]
                  :attributes {:symbol 'keys :position :operator :binding-id :keys-binding
                               :binding-class :core :definition-syntax-id :keys-definition}}
        argument {:node-id :argument :source {:form-id :argument-form :syntax-id :argument-syntax}}
        call {:core-node-id :call :operator-node-id :operator :operator-binding-id :keys-binding
              :argument-node-ids [:argument] :ordered-evaluation-node-ids [:operator :argument]
              :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :call :caller-function-syntax-id :private
              :caller-function-core-node-id :private-core :callee-binding-id :keys-binding
              :callee-definition-syntax-id :keys-definition :callee-function-syntax-id nil
              :callee-function-core-node-id nil :argument-core-node-ids [:argument]
              :ordered-evaluation-node-ids [:operator :argument] :evaluation-order :operator-then-arguments
              :classification :nonlocal-or-nonfunction}]
    {:core {:module module :function-records [record]} :module module :node node :call call :edge edge
     :nodes {:call node :operator operator :argument argument} :bindings {:keys-binding binding}
     :types {:argument :gravity.type/map}}))
(defn cell [m f]
  (apply invoke m "pmk-source" (map f [:core :node :call :edge :nodes :bindings :types :module])))
(def records
  (delay (mapv #(hash-map :function-syntax-id (keyword (str "other-" %))
                         :function-core-node-id (keyword (str "other-core-" %))) (range 65536))))

(deftest source-census-and-profile-widths-no-longer-reserve-work
  (let [f (fixture) wide (assoc-in f [:core :function-records]
                                (conj @records (first (get-in f [:core :function-records]))))
        profiles (assoc-in f [:bindings :keys-binding :profile-set] (vec (repeat 32768 :meta)))
        targets (assoc-in f [:bindings :keys-binding :target-set] (vec (repeat 32768 :all)))]
    (doseq [m @models :let [baseline (cell m f)]]
      (is (= :accepted (:status baseline)))
      (is (not (contains? baseline :work-count)))
      (is (not (contains? (:descriptor baseline) :profile-validation-work)))
      (is (= baseline (cell m wide)))
      (is (= :accepted (:status (cell m profiles))))
      (is (= :accepted (:status (cell m targets))))
      (is (not (contains? (:descriptor baseline) :construction-work)))
      (is (= :none (get-in baseline [:descriptor :query-policy :flow-refinement])))
      (is (= :possible (get-in baseline [:descriptor :allocation-policy :empty-result]))))))

(deftest late-invalid-and-source-corruption-keep-semantic-refusals
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "opaque hash" {})))
                 (equals [_] (swap! callbacks conj :equals) (throw (ex-info "opaque equality" {})))
                 (toString [] (swap! callbacks conj :format) (throw (ex-info "opaque rendering" {}))))
        f (fixture) wide (conj @records (first (get-in f [:core :function-records])))
        corrupt [(assoc-in f [:core :function-records] (assoc wide 65535 opaque))
                 (assoc-in f [:bindings :keys-binding :profile-set] (conj (vec (repeat 32768 :meta)) opaque))
                 (assoc-in f [:bindings :keys-binding :target-set] (conj (vec (repeat 32768 :all)) opaque))
                 (assoc-in f [:call :operator-binding-id] opaque)
                 (assoc-in f [:edge :ordinal] -1)
                 (assoc-in f [:edge :caller-function-core-node-id] :forged)
                 (assoc-in f [:call :evaluation-order] :arguments-first)
                 (assoc-in f [:node :attributes :argument-count] 2)
                 (assoc-in f [:bindings :keys-binding :safety] :unsafe)
                 (assoc-in f [:bindings :keys-binding :effects] [:io/read])
                 (assoc-in f [:module :profile] :kernel)
                 (assoc-in f [:core :function-records] [])]]
    (doseq [m @models]
      (doseq [bad corrupt]
        (is (= {:status :rejected :reason :private-map-keys-source-mismatch} (cell m bad)))
        (is (empty? @callbacks)))
      (is (= {:status :rejected :reason :private-map-keys-finite-keyword-map-type-required}
             (cell m (assoc-in f [:types :argument] opaque))))
      (is (empty? @callbacks))
      (let [before @(:validation m)]
        (cell m (assoc-in f [:call :operator-binding-id] opaque))
        (is (= before @(:validation m)) "Invalid physical IDs refuse before type admission")))))

(deftest header-handoff-retains-only-separate-result-charge
  (doseq [m @models :let [f (fixture) args (mapv f [:core :node :call :edge :nodes :bindings :types])
                        _ (reset! (:proof-budgets m) [])
                        header (apply invoke m "pmk-header" (concat args [{} 20]))]]
    (is (= :accepted (:status header)))
    (is (= 8 (:work-count header)))
    (is (= [17] @(:proof-budgets m)))
    (is (= {:source-model true} (get-in header [:descriptor :key-domain-proof])))
    (is (not (contains? (:descriptor header) :profile-validation-work)))
    (is (= {:status :rejected :reason :private-map-keys-source-mismatch :work-count 3}
           (apply invoke m "pmk-header"
                  (concat (assoc args 0 (assoc (:core f) :function-records [])) [{} 20]))))
    (is (= [17] @(:proof-budgets m)) "Source refusal never reaches entry proof")
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest complete-descriptor-parity-and-stale-source-field-refusal
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "opaque cost hash" {})))
                 (equals [_] (swap! callbacks conj :equals) (throw (ex-info "opaque cost equality" {})))
                 (toString [] (swap! callbacks conj :format) (throw (ex-info "opaque cost rendering" {}))))
        f (fixture) args (mapv f [:core :node :call :edge :nodes :bindings :types])
        headers (mapv #(apply invoke % "pmk-header" (concat args [{} 20])) @models)]
    (is (apply = (map :descriptor headers)))
    (doseq [[m header] (map vector (rest @models) (rest headers))
            :let [descriptor (:descriptor header)
                  fact (invoke m "pmk-call-fact" descriptor (:module f))
                  forged (invoke m "pmk-call-fact" (assoc descriptor :profile-validation-work 0) (:module f))
                  name (symbol (str (:prefix m) "-pmk-fact-census"))]]
      (binding [*ns* (:space m)] (eval (get (:defs m) name)))
      (is (= {:status :accepted :items [fact]}
             (invoke m "pmk-fact-census" [fact] (:module f) {:call descriptor} :call)))
      (is (= {:status :rejected :reason :private-map-keys-source-mismatch}
             (invoke m "pmk-fact-census" [forged] (:module f) {:call descriptor} :call)))
      (doseq [[field value] [[:construction-work 0] [:construction-work 1]
                             [:construction-work-basis :one-static-operation-with-separate-existing-runtime-census]
                             [:construction-work opaque] [:construction-work-basis opaque]]]
        (is (= {:status :rejected :reason :private-map-keys-source-mismatch}
               (invoke m "pmk-fact-census"
                       [(invoke m "pmk-call-fact" (assoc descriptor field value) (:module f))]
                       (:module f) {:call descriptor} :call)))
        (is (empty? @callbacks))))))

(deftest semantic-construction-facts-survive-report-removal
  (doseq [m @models :let [f (fixture) args (mapv f [:core :node :call :edge :nodes :bindings :types])
                        descriptor (:descriptor (apply invoke m "pmk-header" (concat args [{} 20])))
                        row (invoke m "pmk-constraint" descriptor)]]
    (is (not (contains? descriptor :construction-work)))
    (is (not (contains? descriptor :construction-work-basis)))
    (is (not (some #{:construction-work} (:invalidation descriptor))))
    (is (not (contains? (:solution row) :construction-work)))
    (is (= {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/keyword}
           (:result-type descriptor)))
    (is (= [:memory/allocate] (:direct-effects descriptor)))
    (is (= :each-stored-key-once (get-in descriptor [:query-policy :selection])))
    (when (= "body" (:prefix m))
      (binding [*ns* (:space m)] (eval (get (:defs m) 'body-pmk-allocation)))
      (let [allocation (invoke m "pmk-allocation" (:node f) {:operator {:type descriptor}})]
        (is (not (contains? allocation :construction-work)))
        (is (not (contains? allocation :construction-work-basis)))
        (is (= (:allocation-policy descriptor) (:allocation-policy allocation)))
        (is (= (:storage-policy descriptor) (:storage-policy allocation)))
        (is (= (:key-domain-proof descriptor) (:key-domain-proof allocation)))
        (is (true? (:normal-completion-possible? allocation)))
        (is (nil? (:extent allocation)))))))
