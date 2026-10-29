(ns gravity.self-hosting.downstream-selection-test
  "Compiler-free controls of the actual packet entrypoint with injected owners.
   Synthetic values and injected diagnostics are NONACCEPTANCE evidence."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is]]))

(def ^:dynamic *packet-source-path*
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-016-p15-s23-c13-c14-b1-final-record-through-p15-s23-stage2-c13-c14-b1-verification-report.clj")

(defn- read-forms [text]
  (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [form (read {:eof ::eof} r)]
          (if (= ::eof form) out (recur (conj out form))))))))

(defn- definition [forms sym]
  (or (first (filter #(and (seq? %) (#{'defn 'defn-} (first %))
                          (= sym (second %))) (tree-seq coll? seq forms)))
      (throw (ex-info "Missing source definition" {:name sym}))))

(defn- with-dispatch [names owners f]
  ;; Evaluate only the public Clojure dispatch form, never the bootstrap loader
  ;; or Gravity source, and supply every referenced owner explicitly.
  (let [space (create-ns (gensym "downstream-controls-"))
        forms (mapv #(definition (read-forms (slurp *packet-source-path*)) %) names)
        references (set (filter #(and (symbol? %) (.startsWith (name %) "p15-s23-"))
                                (tree-seq coll? seq forms)))]
    (try
      (binding [*ns* space]
        (clojure.core/refer 'clojure.core)
        (doseq [sym references]
          (intern space sym (or (get owners sym)
                                (fn [& _] (throw (ex-info "Unexpected owner" {:owner sym}))))))
        (doseq [form forms] (eval form))
        (f (var-get (ns-resolve space (last names)))))
      (finally (remove-ns (ns-name space))))))

(defn- with-entrypoint [owners f]
  (with-dispatch ['p15-s23-stage2-c13-c14-b1-packet-from-c11!] owners f))

(def native-revision "f66d9ff4554815b89ee7d51ee73b7961f0c33a44")

(defn- pinned-source [path digest]
  ;; Read immutable dependency source; never execute a producer/consumer job.
  (let [{:keys [exit out]} (shell/sh "git" "show" (str native-revision ":" path))
        actual (when (zero? exit)
                 (format "%064x" (java.math.BigInteger.
                                   1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                              (.getBytes out "UTF-8")))))]
    (when-not (= digest actual)
      (throw (ex-info "Pinned interface source unavailable or changed" {:source path})))
    (read-forms out)))

(defn- with-pinned-c11 [f]
  ;; Load actual pinned header/dispatch definitions into isolated namespaces.
  ;; Private artifact*/report* remain injected: semantic authentication is NOT run.
  (let [header (create-ns (gensym "header-controls-"))
        c11 (create-ns (gensym "c11-controls-"))
        api (create-ns (gensym "c11-api-controls-"))]
    (try
      (doseq [[space forms alias-name alias-ns]
              [[header (pinned-source "bootstrap/clojure/src/gravity/source_interface_header.clj"
                                      "55bfa783c61d2a2c4e97dd31aadb48ecf012259edd26aef8033b7598665aaf23") nil nil]
               [c11 (pinned-source "bootstrap/clojure/src/gravity/c11_mir/source_interface.clj"
                                   "4fa3f5cc22aa267e26647b5b705a5176e5200ad0779f251e4060b4d466a6747e") 'header header]]]
        (binding [*ns* space]
          (clojure.core/refer 'clojure.core)
          (when alias-name (alias alias-name (ns-name alias-ns)))
          (doseq [form (rest forms)] (eval form))))
      (binding [*ns* api]
        (clojure.core/refer 'clojure.core)
        (alias 'c11-source-interface (ns-name c11))
        (intern api 'artifact* (fn [& _] (throw (ex-info "Uninjected artifact owner" {}))))
        (intern api 'report* (fn [& _] (throw (ex-info "Uninjected report owner" {}))))
        (let [forms (pinned-source
                      "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00744-opaque-c11-construction-token-definitions.clj"
                      "f307126663bd367606e89e34fb29a9487417533bab6988314fa300b0c0757bbb")]
          (doseq [sym '[p15-s23-stage2-c11-mir-artifact p15-s23-stage2-c11-mir-verification-report]]
            (eval (definition forms sym))))
        (f api))
      (finally (doseq [space [api c11 header]] (remove-ns (ns-name space)))))))

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

(deftest pinned-public-c11-dispatch-composes-with-downstream
  (with-pinned-c11
    (fn [api]
      (let [calls (atom [])
            core {:schema-version 1} context {:synthetic true}
            artifact {:kind :gravity/p15-s23-c11-authenticated-mir-artifact :schema-version 3}
            report {:status :passed :synthetic true}
            public-report (var-get (ns-resolve api 'p15-s23-stage2-c11-mir-verification-report))]
        (alter-var-root (ns-resolve api 'report*)
                        (constantly (fn [a c x v]
                                      (swap! calls conj v)
                                      (is (identical? artifact a))
                                      (is (identical? core c))
                                      (is (identical? context x)) report)))
        (with-entrypoint
          {'p15-s23-c11-ingress-source-path (fn [_] "synthetic")
           'p15-s23-stage2-c11-mir-verification-report public-report
           'p15-s23-c13-c14-b1-build-verified-internal!
           (fn [_ _ _ _ fresh] (is (identical? report fresh)) :synthetic)}
          (fn [invoke]
            (is (= :synthetic (invoke artifact core context {:interface :gravity/c11-mir :version 3})))
            (is (= [3] @calls))
            (doseq [[a h reason]
                    [[artifact {:interface :gravity/c11-mir :version 4} :interface-version-class-or-value]
                     [artifact {:interface :gravity/c6-c10 :version 3} :interface-id]
                     [artifact {:interface :gravity/c11-mir :version 3N} :interface-version-class-or-value]
                     [(assoc artifact :schema-version 2) {:interface :gravity/c11-mir :version 3} :mixed-c11-interface-versions]
                     [artifact nil :interface-map-class]
                     [artifact (with-meta {:interface :gravity/c11-mir :version 3} {:x true}) :interface-header-metadata]]]
              (reset! calls [])
              (is (= reason (:reason (try (invoke a core context h) nil
                                          (catch clojure.lang.ExceptionInfo e (ex-data e))))))
              (is (= [] @calls)))))))))

(deftest versioned-packet-replay-uses-pinned-public-constructor
  (with-pinned-c11
    (fn [api]
      (let [calls (atom []) core {:schema-version 1} context {:synthetic true}
            header {:interface :gravity/c11-mir :version 3}
            artifact {:kind :gravity/p15-s23-c11-authenticated-mir-artifact :schema-version 3}
            packet {:semantic-id :semantic :artifact-id :artifact :actual-path-binding-id :binding}
            public-artifact (var-get (ns-resolve api 'p15-s23-stage2-c11-mir-artifact))]
        (alter-var-root (ns-resolve api 'artifact*)
                        (constantly (fn [c x v]
                                      (is (identical? core c)) (is (identical? context x))
                                      (swap! calls conj [:construct v]) artifact)))
        (with-dispatch
          '[p15-s23-c13-c14-b1-selected-verification-report
            p15-s23-stage2-c13-c14-b1-verification-report]
          {'p15-s23-c11-ingress-source-path (fn [_] "synthetic")
           'p15-s23-stage2-c11-mir-artifact public-artifact
           'p15-s23-c13-c14-b1-verification-preflight! (fn [& _] (swap! calls conj :preflight))
           'p15-s23-stage2-c13-c14-b1-packet-from-c11!
           (fn [a c x h]
             (is (identical? artifact a)) (is (identical? core c))
             (is (identical? context x)) (is (identical? header h))
             (swap! calls conj :versioned-packet) packet)
           'p15-s23-c13-c14-b1-build-internal! (fn [& _] (swap! calls conj :legacy-packet) packet)
           'p15-s23-c11-mir-require-strict-structure! (fn [& _] (swap! calls conj :strict))
           'p15-s23-c13-c14-b1-semantic-id (fn [_] :semantic)
           'p15-s23-c11-mir-digest (fn [_] :artifact)
           'p15-s23-c13-c14-b1-actual-path-binding-id (fn [& _] :binding)
           'p15-s23-c13-c14-b1-contextual-report-record (fn [_] :synthetic-report)
           'p15-s23-b3-llvm-contain-exception! (fn [_ _ error] (throw error))}
          (fn [invoke]
            (is (= :synthetic-report (invoke packet core context header)))
            (is (= [[:construct 3] :preflight :versioned-packet :strict] @calls))
            (reset! calls [])
            (is (= :synthetic-report (invoke packet core context)))
            (is (= [:preflight [:construct 1] :legacy-packet :strict] @calls))
            (reset! calls [])
            (is (= :interface-map-class
                   (:reason (try (invoke packet core context nil) nil
                                 (catch clojure.lang.ExceptionInfo e (ex-data e))))))
            (is (= [] @calls))))))))

(deftest versioned-verification-wrappers-preserve-selection
  (binding [*packet-source-path*
            "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-017-p15-s23-stage2-c13-c14-b1-verify-through-p15-s23-stage2-sh02-authenticated-envelope-artifact.clj"]
    (let [calls (atom []) header {:interface :gravity/c11-mir :version 3}]
      (with-dispatch
        '[p15-s23-stage2-c13-c14-b1-verify! p15-s23-stage2-c13-c14-b1-authentic?]
        {'p15-s23-stage2-c13-c14-b1-verification-report
         (fn [& args] (swap! calls conj args) {:status :passed})}
        (fn [authentic?]
          (is (true? (authentic? :packet :core :context header)))
          (is (identical? header (last (first @calls))))
          (reset! calls [])
          (is (true? (authentic? :packet :core :context)))
          (is (= [[:packet :core :context]] (mapv vec @calls)))
          (is (false? (authentic? :packet))))))))
