(ns gravity.self-hosting.sh07-b51-path-selector-spelling-test
  "L1/C2 spelling and direct-source B51 selector regressions.

  Reader cases execute the owning Gravity reader. The production helper cases
  use the existing Clojure seed leaf-evaluation pattern, not whole-module
  compilation, migration acceptance, or H6 evidence. Attempt5 defines selectors
  as finite path notation; the schema fields remain :target-kind and :loop."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-prefix "bootstrap/gravity/src/gravity/sh07_b51/")

(defn- selected-definitions [file names]
  (binding [*read-eval* false *data-readers* {} *default-data-reader-fn* nil]
    (with-open [reader (java.io.PushbackReader. (io/reader (str source-prefix file)))]
      (loop [selected {}]
        (let [form (read {:eof ::eof} reader)]
          (if (= ::eof form)
            (do (when-not (= names (set (keys selected)))
                  (throw (ex-info "Production helper selection is incomplete"
                                  {:file file :expected names
                                   :actual (set (keys selected))})))
                selected)
            (recur (if (and (seq? form) (= 'defn (first form))
                            (contains? names (second form)))
                     (assoc selected (second form) form) selected))))))))

(defn- with-source-helpers
  "Evaluate only the fixed original defns; supply no replacement operations."
  [selections f]
  (let [definitions (reduce-kv (fn [result file names]
                                 (merge result (selected-definitions file names)))
                               {} selections)
        runtime-symbol (gensym "gravity.b51-selector-leaf-")
        runtime (create-ns runtime-symbol)]
    (try
      (binding [*ns* runtime]
        (clojure.core/refer 'clojure.core)
        (doseq [function (keys definitions)] (intern runtime function))
        (doseq [form (vals definitions)] (eval form)))
      (f (fn [function & arguments]
           (apply (deref (ns-resolve runtime function)) arguments)))
      (finally (remove-ns runtime-symbol)))))

(defn- read-current-source [path text]
  ;; The explicit development seam authenticates current reader bytes without
  ;; renewing the separate pinned migration binding or accepting saved products.
  (bootstrap/compiler-c2-reader-source-artifact
   path text (bootstrap/reader-project-context-for-source path) nil nil
   (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))))

(defn- diagnostic [path text]
  (try
    (read-current-source path text)
    nil
    (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest owning-reader-accepts-lawful-selector-and-retains-invalid-spelling
  ;; Both extensions reach the real reader and verifier; no precomputed products.
  (doseq [extension [".gravity" ".qst"]]
    (let [path (str "selector-spelling" extension)
          prefix "; selector\n"
          lawful ":target-kind-loop"
          invalid ":target-kind=:loop"
          artifact (read-current-source path (str prefix lawful "\n"))
          form (first (:form-tree artifact))
          rejected (diagnostic path (str prefix invalid "\n"))]
      (testing extension
        (is (= [:target-kind-loop] (:parsed-semantic-values artifact)))
        (is (= :keyword (:kind form)))
        (is (= lawful (:raw form)))
        (is (= path (get-in form [:span :source])))
        (is (= (count prefix) (get-in form [:span :byte-start])))
        (is (= (+ (count prefix) (count lawful))
               (get-in form [:span :byte-end])))
        (is (= "C2-IDENTIFIER" (:id rejected)))
        (is (= "L1-IDENTIFIER" (:remapped-from rejected)))
        (is (= invalid (:raw-spelling rejected)))
        (is (= path (get-in rejected [:source-span :source])))
        (is (= :malformed-keyword (get-in rejected [:reader-state :reason])))
        (is (= (count prefix) (get-in rejected [:source-span :byte-start])))
        (is (= (+ (count prefix) (count invalid))
               (get-in rejected [:source-span :byte-end])))
        (is (= {:line 2 :column 1}
               (select-keys (get-in rejected [:source-span :start])
                            [:line :column])))))))

(def ^:private producer-helpers
  {"producer_a.gravity"
   '#{sh07-b51-exact-keys? sh07-b51-lowercase-hex? sh07-b51-sha256-id?
      sh07-b51-internal-reference?}
   "producer_c.gravity"
   '#{sh07-b51-reference-path-step sh07-b51-reference-path-values
      sh07-b51-rejected-category-reference-paths sh07-b51-resolve-controlled-path}})

(def ^:private loop-path
  [:recur-targets :each :slot-ids :target-kind-loop :each])

(def ^:private reference-zero
  {:artifact :gravity/sh07-internal-digest-reference :schema-version 1
   :ordinal 0 :authority :sh07-digest-resolver})

(def ^:private digest-zero (str "sha256:" (apply str (repeat 64 "0"))))
(def ^:private digest-one (str "sha256:" (apply str (repeat 64 "1"))))

(def ^:private function-target
  ;; Attempt5 function schema and producer_d omit loop-only :slot-ids.
  {:target-id digest-zero :target-kind :function
   :owner-core-node-id digest-zero :owner-form-id digest-zero
   :owner-syntax-id digest-zero :arity 0 :binding-ids []
   :body-scope-id digest-zero :parent-target-id nil
   :type-compatibility :pending-sh08})

(deftest producer-selector-traversal-and-resolution-preserve-fields-and-order
  (with-source-helpers
    producer-helpers
    (fn [invoke]
      (let [reference-one (assoc reference-zero :ordinal 1)
            loop-target {:target-kind :loop :marker :kept
                         :slot-ids [reference-one reference-zero]}
            function-target {:target-kind :function :marker :skipped}
            preimage {:recur-targets [function-target loop-target]}
            paths (invoke 'sh07-b51-rejected-category-reference-paths)]
        (is (= loop-path (last paths)))
        (is (= {:status :accepted :values [reference-one reference-zero]}
               (invoke 'sh07-b51-reference-path-values preimage (last paths))))
        (is (= {:status :accepted
                :value {:recur-targets
                        [function-target
                         (assoc loop-target :slot-ids [digest-one digest-zero])]}}
               (invoke 'sh07-b51-resolve-controlled-path
                       preimage (last paths) [digest-zero digest-one])))
        (is (= {:status :accepted :states [{:value :selected
                                           :parent {:target-kind :loop}}]}
               (invoke 'sh07-b51-reference-path-step
                       [{:value :ignored :parent {:target-kind :function}}
                        {:value :selected :parent {:target-kind :loop}}]
                       :target-kind-loop)))
        (is (= :path-kind-parent
               (:reason (invoke 'sh07-b51-reference-path-step
                                [{:value :bad :parent []}] :target-kind-loop))))
        (is (= :path-key-missing
               (:reason (invoke 'sh07-b51-reference-path-values
                                {:recur-targets [{:target-kind :loop}]} loop-path))))
        (is (= :reference-ordinal-range
               (:reason (invoke 'sh07-b51-resolve-controlled-path
                                preimage loop-path [digest-zero]))))
        (is (= :path-kind-position
               (:reason (invoke 'sh07-b51-resolve-controlled-path
                                reference-zero [:target-kind-loop] [digest-zero]))))))))

(def ^:private root-helpers
  {"root6_a.gravity"
   '#{sh07-b51-r6-exact-keys? sh07-b51-r6-nonnegative-integer?
      sh07-b51-r6-lowercase-hex? sh07-b51-r6-sha256-id?
      sh07-b51-r6-internal-reference? sh07-b51-r6-path-values
      sh07-b51-r6-append sh07-b51-r6-path-rows sh07-b51-r6-prefix-paths
      sh07-b51-r6-tier3-core-paths sh07-b51-r6-tier3-ordinary-core-paths
      sh07-b51-r6-tier3-noncore-groups sh07-b51-r6-tier3-registry}
   "root6_b.gravity" '#{sh07-b51-r6-materialize-registered-path}})

(deftest independent-selector-registry-traversal-and-materialization
  (with-source-helpers
    root-helpers
    (fn [invoke]
      (let [rows (invoke 'sh07-b51-r6-tier3-registry :b51-vector-frontier-rejected)
            row (first (filter #(= (into [:products] loop-path) (:path %)) rows))
            reference-one (assoc reference-zero :ordinal 1)
            loop-target {:target-kind :loop :slot-ids [reference-one reference-zero]}
            preimage {:products {:recur-targets [function-target loop-target]}}
            materialized (invoke 'sh07-b51-r6-materialize-registered-path
                                 preimage nil (:path row)
                                 [digest-zero digest-one] 0 100)]
        (is (= :sh07-b51-binding-slot-id (:target-purpose row)))
        (is (= 1 (count (filter #(= (:path row) (:path %)) rows))))
        (is (not-any? #(= (:path row) (:path %))
                      (invoke 'sh07-b51-r6-tier3-registry :legacy-v16-accepted)))
        (is (= {:status :accepted :values [reference-one reference-zero]}
               (invoke 'sh07-b51-r6-path-values preimage (:path row))))
        (is (= :accepted (:status materialized)))
        (is (= {:products {:recur-targets
                           [function-target
                            (assoc loop-target :slot-ids [digest-one digest-zero])]}}
               (:value materialized)))
        (is (< 0 (:remaining-work materialized) 100))
        (is (= :reference-prefix
               (:reason (invoke 'sh07-b51-r6-materialize-registered-path
                                preimage nil (:path row) [digest-zero] 0 100))))
        (is (= :registry-key-missing
               (:reason (invoke 'sh07-b51-r6-path-values
                                {:recur-targets [{:target-kind :loop}]} loop-path))))
        (is (= :materialization-work
               (:reason (invoke 'sh07-b51-r6-materialize-registered-path
                                preimage nil (:path row) [digest-zero] 0 0))))
        (is (= :materialization-depth
               (:reason (invoke 'sh07-b51-r6-materialize-registered-path
                                preimage nil (:path row) [digest-zero] 257 100))))))))

(deftest loop-selector-skips-lawful-function-records-before-field-lookup
  (with-source-helpers
    (merge producer-helpers root-helpers)
    (fn [invoke]
      (let [one (assoc reference-zero :ordinal 1)
            loop-target {:target-kind :loop :slot-ids [one reference-zero]}
            resolved-loop (assoc loop-target :slot-ids [digest-one digest-zero])
            cases [[[] [] [] 2]
                   [[function-target] [] [function-target] 4]
                   [[loop-target] [one reference-zero] [resolved-loop] 7]
                   [[{:target-kind :loop :slot-ids [one one]}] [one one]
                    [{:target-kind :loop :slot-ids [digest-one digest-one]}] 7]
                   [[function-target loop-target] [one reference-zero]
                    [function-target resolved-loop] 9]
                   [[loop-target function-target] [one reference-zero]
                    [resolved-loop function-target] 9]
                   [[loop-target function-target loop-target]
                    [one reference-zero one reference-zero]
                    [resolved-loop function-target resolved-loop] 14]]]
        (doseq [[targets selected resolved work] cases]
          (testing (pr-str (mapv :target-kind targets))
            (let [preimage {:recur-targets targets}
                  expected {:recur-targets resolved}
                  materialized (invoke 'sh07-b51-r6-materialize-registered-path
                                       preimage nil loop-path
                                       [digest-zero digest-one] 0 work)]
              (is (= {:status :accepted :values selected}
                     (invoke 'sh07-b51-r6-path-values preimage loop-path)
                     (invoke 'sh07-b51-reference-path-values preimage loop-path)))
              (is (= {:status :accepted :value expected :remaining-work 0}
                     materialized))
              (is (= {:status :accepted :value expected}
                     (invoke 'sh07-b51-resolve-controlled-path
                             preimage loop-path [digest-zero digest-one])))
              (is (= :materialization-work
                     (:reason (invoke 'sh07-b51-r6-materialize-registered-path
                                      preimage nil loop-path
                                      [digest-zero digest-one] 0 (dec work)))))
              (is (= (pr-str (filterv #(= :function (:target-kind %)) targets))
                     (pr-str (filterv #(= :function (:target-kind %))
                                      (get-in materialized [:value :recur-targets]))))))))))))

(deftest loop-selector-preserves-selected-errors-and-resource-priorities
  (with-source-helpers
    root-helpers
    (fn [invoke]
      (let [tokens [:slot-ids :target-kind-loop :each]
            materialize (fn [value prefix depth work]
                          (invoke 'sh07-b51-r6-materialize-registered-path
                                  value nil tokens prefix depth work))]
        (is (= {:status :accepted :value function-target :remaining-work 0}
               (materialize function-target [] 255 2)))
        (is (= :materialization-work
               (:reason (materialize function-target [] 255 1))))
        (is (= :materialization-depth
               (:reason (materialize function-target [] 256 1))))
        (doseq [value [{:target-kind :loop} []]]
          (let [reason (if (map? value) :registry-key-missing :registry-map-required)]
            ;; The current map/key error still precedes a child's exhausted bound.
            (is (= reason (:reason (materialize value [] 256 1))))))
        (doseq [value [{:target-kind :unknown} {}
                      {:target-kind :unknown :slot-ids []} {:slot-ids []}]]
          (is (= :registry-key-missing (:reason (materialize value [] 0 100))))
          (is (= :registry-key-missing
                 (:reason (invoke 'sh07-b51-r6-path-values value tokens)))))
        (is (= :registry-vector-required
               (:reason (invoke 'sh07-b51-r6-path-values
                                {:target-kind :loop :slot-ids :wrong} tokens))))
        (doseq [function ['sh07-b51-r6-path-values
                          'sh07-b51-r6-materialize-registered-path]]
          (let [arguments [function-target [:other :target-kind-loop :each]]
                result (if (= function 'sh07-b51-r6-path-values)
                         (apply invoke function arguments)
                         (invoke function function-target nil
                                 (second arguments) [] 0 100))]
            (is (= :registry-key-missing (:reason result)))))
        (doseq [[value reason]
                [[{:target-kind :loop :slot-ids :wrong} :registry-vector-required]
                 [{:target-kind :loop :slot-ids [:wrong]} :registered-reference-shape]
                 [{:target-kind :loop :slot-ids [reference-zero]} :reference-prefix]]]
          (is (= reason (:reason (materialize value [] 0 100)))))
        (is (= :resolved-id-shape
               (:reason (materialize {:target-kind :loop :slot-ids [reference-zero]}
                                     [:wrong] 0 100))))
        (is (= {:status :accepted :value {:target-kind :loop :slot-ids []}
                :remaining-work 0}
               (materialize {:target-kind :loop :slot-ids []} [] 254 3)))
        (is (= :materialization-depth
               (:reason (materialize {:target-kind :loop :slot-ids []} [] 255 3))))
        (let [preimage {:recur-targets [function-target]}
              path-at-limit (into (vec (repeat 251 :optional)) loop-path)]
          (is (= {:status :accepted :values []}
                 (invoke 'sh07-b51-r6-path-values preimage path-at-limit)))
          (is (= :registry-path-depth
                 (:reason (invoke 'sh07-b51-r6-path-values
                                  preimage (into [:optional] path-at-limit)))))
          (is (= :registry-key-missing
                 (:reason (invoke 'sh07-b51-r6-path-values
                                  {:recur-targets [{:target-kind :loop}]}
                                  (into [:optional :optional :optional]
                                        path-at-limit))))))))))
