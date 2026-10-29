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
        contract-space (create-ns (gensym "contract-controls-"))
        api (create-ns (gensym "c11-api-controls-"))]
    (try
      (doseq [[space forms alias-name alias-ns]
              [[header (pinned-source "bootstrap/clojure/src/gravity/source_interface_header.clj"
                                      "55bfa783c61d2a2c4e97dd31aadb48ecf012259edd26aef8033b7598665aaf23") nil nil]
               [c11 (pinned-source "bootstrap/clojure/src/gravity/c11_mir/source_interface.clj"
                                   "4fa3f5cc22aa267e26647b5b705a5176e5200ad0779f251e4060b4d466a6747e") 'header header]
               [contract-space (pinned-source "bootstrap/clojure/src/gravity/c11_mir/source_contract.clj"
                                              "3d538701ae77a5a14bbf2d3beab37e738168dd4f1157af9c9f4b0115e040cebe") nil nil]]]
        (binding [*ns* space]
          (clojure.core/refer 'clojure.core)
          (when alias-name (alias alias-name (ns-name alias-ns)))
          (doseq [form (rest forms)] (eval form))))
      (binding [*ns* api]
        (clojure.core/refer 'clojure.core)
        (alias 'c11-source-interface (ns-name c11))
        (alias 'c11-source-contract (ns-name contract-space))
        (let [forms (pinned-source
                      "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00732-p15-s23-c11-mir-owned-upstream-diagnostic.clj"
                      "4cef77f8f0afa7990609dd8c07ddeae2ec85033efbf0cf4db4ae31053e7f7bab")
              selected (mapv #(definition forms %)
                             '[p15-s23-c11-mir-contract p15-s23-c11-mir-versioned-record
                               p15-s23-c11-mir-selected-builder-function p15-s23-c11-mir-selected-verifier-function
                               p15-s23-c11-mir-contract-binding])]
          ;; Legacy constants are not supplied by this v3 dependency fixture.
          ;; Resolve them for compilation; executing a missing legacy branch fails.
          (doseq [sym (filter #(and (symbol? %) (.startsWith (name %) "p15-s23-"))
                             (tree-seq coll? seq selected))]
            (intern api sym ::unavailable-legacy-constant))
          (doseq [form selected] (eval form)))
        (eval (definition
                (pinned-source "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00741-p15-s23-c11-mir-map-blocks.clj"
                               "52318894cdda3bbba54ae0dd7c893f50567e33767665d241af6f7db6f6276c8d")
                'p15-s23-c11-mir-source-rule-record))
        (intern api 'artifact* (fn [& _] (throw (ex-info "Uninjected artifact owner" {}))))
        (intern api 'report* (fn [& _] (throw (ex-info "Uninjected report owner" {}))))
        (let [forms (pinned-source
                      "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00744-opaque-c11-construction-token-definitions.clj"
                      "f307126663bd367606e89e34fb29a9487417533bab6988314fa300b0c0757bbb")]
          (doseq [sym '[p15-s23-stage2-c11-mir-artifact p15-s23-stage2-c11-mir-verification-report]]
            (eval (definition forms sym))))
        (f api))
      (finally (doseq [space [api contract-space c11 header]] (remove-ns (ns-name space)))))))

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
       'p15-s23-c13-c14-b1-selected-source-contract!
       (fn [a c x fresh h]
         (is (every? true? (map identical? [a c x fresh h] [artifact core context report header])))
         (swap! calls conj :selection) :selection)
       'p15-s23-c13-c14-b1-bind-selected-source-contract
       (fn [packet selection] (is (= :selection selection)) packet)
       'p15-s23-c13-c14-b1-build-verified-internal!
       (fn [_ a c x fresh]
         (swap! calls conj :downstream)
         (is (identical? artifact a)) (is (identical? core c))
         (is (identical? context x)) (is (identical? report fresh))
         :synthetic-downstream)}
      (fn [invoke]
        (is (= :synthetic-downstream (invoke artifact core context header)))
        (is (= [:owner :selection :downstream] @calls))))))

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
           'p15-s23-c13-c14-b1-selected-source-contract! (fn [& _] :synthetic-selection)
           'p15-s23-c13-c14-b1-bind-selected-source-contract (fn [packet _] packet)
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
           'p15-s23-b3-llvm-contain-exception!
           (fn [& _] (swap! calls conj :unexpected-containment)
             (throw (ex-info "Owner rejection reached backend containment" {})))}
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

(deftest selected-source-contract-uses-native-fixed-rule-and-policy-joins
  (with-pinned-c11
    (fn [api]
      (let [select-binding (var-get (ns-resolve api 'p15-s23-c11-mir-contract-binding))
            make-rule (var-get (ns-resolve api 'p15-s23-c11-mir-source-rule-record))
            binding (select-binding 3) rule (make-rule binding)
            header {:interface :gravity/c11-mir :version 3}
            ;; Synthetic policy identities remain distinct. Their real digest
            ;; computation and fresh authority are owned by C11, not this test.
            policy-binding {:source-policy-binding-id :synthetic-core-domain
                            :source-policy-hash :synthetic-c11-domain}
            policy {:synthetic-policy true}
            ingress {:schema-version 3 :source-policy policy}
            artifact {:source-rule rule :checked-core-ingress ingress
                      :verification-report {:checked-core-source-policy-binding policy-binding}
                      :mir-module {:pass-execution-record {:checked-core-source-policy-binding policy-binding}}
                      :b1-preflight {:binding {:checked-core-source-policy-binding policy-binding}}}
            report {:source-rule rule :checked-core-ingress ingress}
            core {:schema-version 1} context {:synthetic true} calls (atom [])]
        (is (= :closed-flat-bool-literal-sequence (:semantic-family binding)))
        (is (= "sha256:4ca2bf8c551c9b0e7aefbeab84e7f7c9b5742bdeee10f68e74aff3470d35b3dd"
               (:source-content-hash binding)))
        (with-dispatch
          '[p15-s23-c13-c14-b1-selected-source-contract!]
          {'p15-s23-c11-ingress-source-path (fn [_] "synthetic")
           'p15-s23-c11-mir-contract-binding select-binding
           'p15-s23-c11-mir-source-rule-record make-rule
           'p15-s23-c11-mir-require-strict-structure!
           ;; Injected relative comparator, not owner authentication evidence.
           (fn [_ expected observed fact]
             (when-not (= (pr-str expected) (pr-str observed))
               (throw (ex-info "Injected structural rejection" {:missing-fact fact}))))
           'p15-s23-c11-mir-require-core-policy-data!
           (fn [c x p v]
             (is (identical? core c)) (is (identical? context x))
             (is (identical? policy p)) (is (= 3 v))
             (swap! calls conj :policy))
           'p15-s23-c11-mir-core-policy-binding
           (fn [v p] (is (= 3 v)) (is (identical? policy p)) policy-binding)}
          (fn [select!]
            (let [selection (select! artifact core context report header)]
              (is (identical? header (:source-interface selection)))
              (is (= binding (:source-contract selection)))
              (is (= rule (:source-rule selection)))
              (is (identical? policy-binding (:checked-core-source-policy-binding selection)))
              (is (= [:policy] @calls)))
            (doseq [[a r fact]
                    [[(assoc-in artifact [:source-rule :source-content-hash] "stale") report
                      :selected-downstream-c11-source-contract]
                     [artifact (assoc-in report [:source-rule :semantic-family] :other)
                      :selected-downstream-c11-source-contract]
                     [(assoc-in artifact [:source-rule :source-byte-count] 597279N) report
                      :selected-downstream-c11-source-contract]
                     [artifact (assoc-in report [:checked-core-ingress :source-policy] {:crossed true})
                      :selected-downstream-core-ingress]
                     [(assoc-in artifact [:verification-report :checked-core-source-policy-binding]
                                (assoc policy-binding :source-policy-hash :synthetic-core-domain)) report
                      :selected-downstream-core-policy-binding]
                     [(update-in artifact [:mir-module :pass-execution-record] dissoc :checked-core-source-policy-binding)
                      report :selected-downstream-core-policy-binding]
                     [(assoc-in artifact [:b1-preflight :binding :checked-core-source-policy-binding]
                                {:source-policy-binding-id :crossed :source-policy-hash :crossed})
                      report :selected-downstream-core-policy-binding]]]
              (is (= fact (:missing-fact
                            (try (select! a core context r header) nil
                                 (catch clojure.lang.ExceptionInfo e (ex-data e)))))))))))))

(deftest selected-source-contract-is-included-in-packet-identities
  (let [packet {:kind :synthetic-packet :c11 {:artifact-id :synthetic-c11}
                :actual-path-provenance {:source :synthetic}}
        selection {:source-interface {:interface :gravity/c11-mir :version 3}
                   :source-contract {:source-content-hash :synthetic}}]
    (with-dispatch
      '[p15-s23-c13-c14-b1-bind-selected-source-contract]
      {'p15-s23-c13-c14-b1-semantic-id
       (fn [p] (dissoc p :semantic-id :artifact-id :actual-path-binding-id))
       'p15-s23-c11-mir-digest identity
       'p15-s23-c13-c14-b1-actual-path-binding-id vector}
      (fn [bind-selection]
        (let [result (bind-selection packet selection)
              changed (bind-selection packet (assoc selection :source-contract :crossed))]
          (is (identical? selection (get-in result [:c11 :source-contract-selection])))
          (is (= :synthetic-c11 (get-in result [:c11 :artifact-id])))
          (is (not= (:semantic-id result) (:semantic-id changed)))
          (is (not= (:artifact-id result) (:artifact-id changed)))
          (is (not= (:actual-path-binding-id result) (:actual-path-binding-id changed)))))))
  (binding [*packet-source-path* "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/001-source-authority-and-paths.clj"]
    (with-dispatch
      '[p15-s23-c13-c14-b1-semantic-input]
      {'p15-s23-c13-c14-b1-path-neutral-value identity
       'p15-s23-c13-c14-b1-stage-semantic-projection identity}
      (fn [project]
        (is (= {:source-contract-selection :synthetic-selection}
               (:c11 (project {:c11 {:source-contract-selection :synthetic-selection}}))))))))

(deftest versioned-declarations-preserve-owner-definitions-without-fallback
  (let [space (create-ns (gensym "declaration-controls-"))
        form (first (filter #(and (seq? %) (= 'declare (first %))
                                 (= 'p15-s23-c11-mir-contract-binding (second %)))
                            (tree-seq coll? seq (read-forms (slurp *packet-source-path*)))))]
    (try
      (binding [*ns* space]
        (clojure.core/refer 'clojure.core)
        (intern space 'p15-s23-c11-mir-contract-binding :existing-owner)
        (eval form)
        (is (= :existing-owner (var-get (ns-resolve space 'p15-s23-c11-mir-contract-binding))))
        (doseq [sym '[p15-s23-c11-mir-require-core-policy-data! p15-s23-c11-mir-core-policy-binding]]
          (is (false? (bound? (ns-resolve space sym))))))
      (finally (remove-ns (ns-name space))))))
