(ns gravity.self-hosting.downstream-selection-test
  "Compiler-free controls of the actual packet entrypoint with injected owners.
   Synthetic values and injected diagnostics are NONACCEPTANCE evidence."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]))

(def source-path
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-016-p15-s23-c13-c14-b1-final-record-through-p15-s23-stage2-c13-c14-b1-verification-report.clj")

(defn- entrypoint []
  (with-open [r (java.io.PushbackReader. (io/reader source-path))]
    (binding [*read-eval* false]
      (let [outer (read r)]
        (first (filter #(and (seq? %) (= 'defn (first %))
                             (= 'p15-s23-stage2-c13-c14-b1-packet-from-c11!
                                (second %))) (drop 2 outer)))))))

(defn- with-entrypoint [owners f]
  ;; Evaluate only the public Clojure dispatch form, never the bootstrap loader
  ;; or Gravity source, and supply every referenced owner explicitly.
  (let [space (create-ns (gensym "downstream-controls-"))
        form (entrypoint)
        names (set (filter #(and (symbol? %) (.startsWith (name %) "p15-s23-"))
                           (tree-seq coll? seq form)))]
    (try
      (binding [*ns* space]
        (clojure.core/refer 'clojure.core)
        (doseq [sym names]
          (intern space sym (or (get owners sym)
                                (fn [& _] (throw (ex-info "Unexpected owner" {:owner sym}))))))
        (eval form)
        (f (var-get (ns-resolve space (second form)))))
      (finally (remove-ns (ns-name space))))))

(deftest explicit-header-and-fresh-report-forwarding
  (let [artifact (Object.) core (Object.) context (Object.)
        header {:interface :gravity/c11-mir :version 3}
        report {:status :passed :synthetic true} calls (atom [])]
    (with-entrypoint
      {'p15-s23-c11-ingress-source-path (fn [_] "synthetic")
       'p15-s23-stage2-c11-mir-verification-report
       (fn [& args] (swap! calls conj :owner)
         (is (= 4 (count args)))
         (doseq [[actual expected] (map vector args [artifact core context header])]
           (is (identical? actual expected))) report)
       'p15-s23-c13-c14-b1-build-verified-internal!
       (fn [_ a c x fresh]
         (swap! calls conj :downstream)
         (is (identical? artifact a)) (is (identical? core c))
         (is (identical? context x)) (is (identical? report fresh))
         :synthetic-downstream)}
      (fn [invoke]
        (is (= :synthetic-downstream (invoke artifact core context header)))
        (is (= [:owner :downstream] @calls))))))

(deftest owner-rejections-precede-all-downstream-work
  ;; These are injected owner outcomes, not execution of the real header,
  ;; policy or authentication validators. The entrypoint must preserve each
  ;; owning diagnostic and must not fall back to a legacy/reconstruction path.
  (doseq [reason [:unsupported-version :mixed-c11-interface-versions
                  :altered-semantic-family :crossed-core-context-receipt
                  :changed-reader-result :changed-reader-source
                  :changed-expression-binding]]
    (let [failure (ex-info "Injected owner rejection"
                           {:id "C11-MODULE" :missing-fact reason})
          calls (atom 0)]
      (with-entrypoint
        {'p15-s23-stage2-c11-mir-verification-report
         (fn [& _] (swap! calls inc) (throw failure))}
        (fn [invoke]
          (is (identical? failure
                         (try (invoke :synthetic-artifact :synthetic-core
                                      :synthetic-context
                                      {:interface :gravity/c11-mir :version 3})
                              nil (catch clojure.lang.ExceptionInfo e e))))
          (is (= 1 @calls)))))))

(deftest legacy-arity-retains-original-route
  (let [calls (atom [])]
    (with-entrypoint
      {'p15-s23-c11-ingress-source-path (fn [_] "synthetic")
       'p15-s23-c13-c14-b1-build-internal!
       (fn [_ a c x] (swap! calls conj [a c x]) :legacy-sentinel)}
      (fn [invoke]
        (is (= :legacy-sentinel (invoke :artifact :core :context)))
        (is (= [[:artifact :core :context]] @calls))))))
