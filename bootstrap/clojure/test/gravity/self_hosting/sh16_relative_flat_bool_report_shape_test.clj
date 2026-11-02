(ns gravity.self-hosting.sh16-relative-flat-bool-report-shape-test
  "Relative synthetic DATA only: no producer invocation or admission credit."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh16-mir-optimization-test]))

(defn- call [name arguments]
  ((var-get (ns-resolve 'gravity.self-hosting.sh16-mir-optimization-test
                       'invoke-engine)) name arguments))

(defn- relative? [mir report]
  (call 'c13-relative-flat-bool-report-shape? [mir report]))

(defn- supplied []
  (let [root (str "sha256:" (apply str (repeat 64 "a")))]
    [{:source-core root :module-id 'synthetic
      :functions {'main {:blocks {:entry {}}}}}
     {:artifact :gravity/sh12-branch-join-mir-report :schema-version 1
      :scope :closed-flat-bool-literal-sequence :phase :verification :status :passed
      :checked-core-artifact-id root :mir-module-id 'synthetic
      :checks [:single-function-linear :blocks-terminators-and-cfg-edges
               :same-block-definitions-uses-and-return-dominance
               :type-fact-preservation :effect-order-preservation
               :ownership-capability-and-profile-preservation
               :safety-outcome-preservation :source-origin-preservation]
      :preserves [:type-facts :effect-facts :ownership-facts :capability-facts :safety-facts
                  :profile-target-facts :source-spans :source-origin-map :generated-origin-chains
                  :control-flow-edges :data-flow-edges :dominance-facts]
      :diagnostics [] :nonclaims [:generic-c11-verifier :independent-acceptance
                                 :optimizer-credit :backend-credit :self-hosted?]}]))

(deftest relative-flat-report-exact-shape-and-identity-joins
  (let [[mir report] (supplied)]
    (is (true? (relative? mir report)))
    ;; Coherent caller-chosen identities still pass: this is deliberately DATA,
    ;; not authentication, MIR verification or a source-specific contract.
    (is (true? (relative? (assoc mir :module-id 'other)
                         (assoc report :mir-module-id 'other))))
    (doseq [field (keys report)]
      (is (false? (relative? mir (dissoc report field))) (str field)))
    (doseq [[field value] [[:extra true] [:artifact :foreign] [:schema-version 3]
                           [:scope :closed-i64-parameter-linear] [:phase :construction]
                           [:status :rejected] [:checked-core-artifact-id :stale]
                           [:mir-module-id 'stale] [:diagnostics [:failure]]
                           [:preserves []] [:nonclaims []]]]
      (is (false? (relative? mir (assoc report field value))) (str field)))
    (doseq [candidate [nil [] (dissoc mir :source-core)
                      (assoc mir :source-core :invalid) (assoc mir :module-id "synthetic")]]
      (is (false? (relative? candidate report))))))

(deftest relative-flat-report-complete-ordered-checks
  (let [[mir report] (supplied) checks (:checks report)]
    (doseq [index (range (count checks))]
      (is (false? (relative? mir (assoc report :checks
                                      (vec (concat (subvec checks 0 index)
                                                   (subvec checks (inc index)))))))))
    (doseq [candidate [(vec (reverse checks)) (conj checks (first checks))
                      (assoc checks 7 :unknown) (apply list checks) (set checks)
                      (assoc checks 2 :entry-parameter-and-local-dominance)]]
      (is (false? (relative? mir (assoc report :checks candidate)))))))

(deftest relative-flat-report-foreign-carriers-before-comparison
  (let [[mir report] (supplied) callbacks (atom 0)
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks inc) false)
                 (hashCode [] (swap! callbacks inc) 1231)
                 (toString [] (swap! callbacks inc) "opaque"))]
    (doseq [[m r] [[opaque report] [mir opaque]
                   [mir (java.util.HashMap. report)]
                   [(java.util.HashMap. mir) report]
                   [mir (assoc report :checks (conj (:checks report) opaque))]
                   [mir (assoc report :diagnostics [opaque])]
                   [(assoc mir :extra opaque) report]
                   [mir (assoc report :schema-version 9223372036854775808N)]
                   [mir (assoc report :checks (map identity (:checks report)))]]]
      (reset! callbacks 0)
      (is (false? (relative? m r)))
      (is (zero? @callbacks)))))

(deftest relative-flat-report-does-not-extend-legacy-admission
  (let [[mir report] (supplied)
        legacy (-> report (assoc :scope :closed-i64-parameter-linear)
                   (assoc-in [:checks 2] :entry-parameter-and-local-dominance))]
    (is (false? (call 'c13-reconstruction-report-valid? [mir report])))
    (is (true? (call 'c13-reconstruction-report-valid? [mir legacy])))
    (is (false? (relative? mir legacy)))
    (let [resource (io/resource "gravity/self_hosting/sh16_relative_flat_bool_report_shape_test.clj")
          root (loop [p (.getParent (.toPath (io/file (.toURI resource))))]
                 (if (.isFile (.toFile (.resolve p "deps.edn"))) p (recur (.getParent p))))
          source (slurp (str (.resolve root "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")))
          forms (binding [*read-eval* false]
                  (with-open [r (java.io.PushbackReader. (java.io.StringReader. source))]
                    (loop [result []]
                      (let [form (read {:eof ::eof} r)]
                        (if (= ::eof form) result (recur (conj result form)))))))]
      ;; No source reference outside its own definition, including exports.
      (is (= 1 (count (filter #{'c13-relative-flat-bool-report-shape?}
                              (tree-seq coll? seq forms))))))))
