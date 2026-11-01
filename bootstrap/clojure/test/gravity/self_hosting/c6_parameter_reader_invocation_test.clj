(ns gravity.self-hosting.c6-parameter-reader-invocation-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh12-branch-join-mir-test]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(defn- diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn- owner [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))
(defn- stage [label operation]
  (prn {:parameter-context-stage label :event :begin}) (flush)
  (let [start (System/nanoTime) value (operation)]
    (prn {:parameter-context-stage label :event :complete
          :elapsed-ms (quot (- (System/nanoTime) start) 1000000)}) (flush)
    value))

(deftest resolved-parameter-forwards-explicit-context-and-preserves-legacy-arities
  ;; Routing unit only: the sentinel stops before any artifact is consumed.
  ;; These marker values confer no compiler acceptance or source authority.
  (let [source "fixture.gravity" text "fixture" front {} parent {:marker :c3-parent}
        invocation {:reader-source-path "reader.gravity"
                    :authenticated-envelope-options {:source :development-current}}
        macro-parent {:marker :macro-parent} resolved {:marker :resolved}
        sentinel (ex-info "routing observed" {:routing :observed})
        calls (atom [])]
    (with-redefs [b/sh05-macro-artifact-from-c3-artifact
                  (fn [& arguments] (swap! calls conj [:macro arguments]) macro-parent)
                  b/sh06-resolution-artifact-from-sh05-artifact
                  (fn [& arguments] (swap! calls conj [:resolve arguments]) resolved)
                  b/sh06-resolution-source-artifact
                  (fn [& arguments] (swap! calls conj [:source arguments]) resolved)
                  b/sh07-core-authenticated-request
                  (fn [& arguments] (swap! calls conj [:request arguments]) (throw sentinel))]
      (doseq [arguments [[source text front] [source text front nil] [source text front nil nil]]]
        (reset! calls [])
        (is (= {:routing :observed} (diagnostic #(apply b/p15-s23-c6c10-resolved-parameter arguments))))
        (is (= [[:source [source text]] [:request [resolved]]] @calls)))
      (doseq [arguments [[source text front parent] [source text front parent nil]]]
        (reset! calls [])
        (is (= {:routing :observed} (diagnostic #(apply b/p15-s23-c6c10-resolved-parameter arguments))))
        (is (= [[:macro [source parent]] [:resolve [source macro-parent]] [:request [resolved]]] @calls)))
      (reset! calls [])
      (is (= {:routing :observed}
             (diagnostic #(b/p15-s23-c6c10-resolved-parameter source text front parent invocation))))
      (is (= [[:macro [source parent invocation]]
              [:resolve [source macro-parent
                         {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                         (:reader-source-path invocation) invocation]]
              [:request [resolved :gravity.bootstrap/legacy invocation]]] @calls))
      (is (identical? invocation (last (second (first @calls)))))
      (is (identical? invocation (last (second (second @calls)))))
      (is (identical? invocation (last (second (last @calls)))))
      (reset! calls [])
      (is (= ["C6-VERIFY" :explicit-reader-c3-parent]
             ((juxt :id :missing-fact)
              (diagnostic #(b/p15-s23-c6c10-resolved-parameter source text front nil invocation)))))
      (is (empty? @calls)))))

(defn- current-parameter-case
  ([binding-data] (current-parameter-case binding-data ".gravity"))
  ([binding-data extension]
   (when-not (contains? #{".gravity" ".qst"} extension)
     (throw (ex-info "Unsupported parameter fixture extension"
                     {:id "C6-TEST-SOURCE-EXTENSION" :extension extension})))
  ;; One genuine source traversal. Compiled binding DATA may be reused, never
  ;; a source ingress, accepted Core or verifier result.
  (binding [b/*p15-s23-stage2-compiler-artifact-binding*
            @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'current-emitter))]
    (let [root @(owner 'root)
          path (str (.resolve root (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source" extension)))
          text (slurp path) reader (str (.resolve root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
          invocation {:reader-source-path reader :authenticated-envelope-options {:source :development-current}}
          observed (atom []) actual-macro b/sh05-macro-artifact-from-c3-artifact
          products (stage :source-products
                          #(with-redefs [b/sh05-macro-artifact-from-c3-artifact
                                         (fn [& arguments]
                                           (swap! observed conj (vec arguments))
                                           (apply actual-macro arguments))]
                             ((owner 'parameter-source-products) path text binding-data reader)))
          raw (stage :c6-build #((owner 'invoke) (owner 'parameter-c6-plan)
                                'c6-c10-build-checked-core-template [(:envelope products)]))]
      (is (= :accepted (:status raw)))
      (when-not (= :accepted (:status raw))
        (throw (ex-info "Original C6 construction rejected" {:id "C6-TEST-CONSTRUCTION"})))
      (is (= (str "sha256:" (b/sha256-hex text)) (:source-content-hash (:envelope products))))
      (is (= 1 (count @observed)))
      (doseq [arguments @observed]
        (is (= 3 (count arguments)))
        (is (= path (first arguments)))
        (is (= invocation (last arguments))))
      {:root root :path path :text text :reader reader :invocation invocation
       :parent (second (first @observed)) :products products :raw raw}))))

(defn- check-parameter-replay [raw fresh]
  ;; The original template is untrusted input until current C6 reconstructs it.
  (binding [b/*p15-s23-stage2-compiler-artifact-binding*
            @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'current-emitter))]
    (let [{:keys [root path text invocation parent products]} fresh
          replay (:raw fresh) call (owner 'invoke) plan (owner 'parameter-c6-plan)
          _ (is (= raw replay))
          _ (when-not (= raw replay)
              (throw (ex-info "Complete original/fresh C6 results differ" {:id "C6-TEST-REPLAY-MISMATCH"})))
          upstream (stage :fresh-c6-verifier
                          #(call plan 'c6-c10-verify-checked-core-template
                                 [(:envelope products) (:artifact-template raw) (:digest-requests replay)]))
          _ (is (= :passed (:status upstream)))
          _ (when-not (= :passed (:status upstream))
              (throw (ex-info "Fresh C6 verifier rejected" {:id "C6-TEST-REPLAY-REJECTED"})))
          sealed (b/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (b/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (b/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
          mir (stage :gravity-mir #(call (owner 'c11-plan) 'sh12-build-branch-join-mir [core]))
          gravity (stage :gravity-verifier #(call (owner 'c11-plan) 'sh12-verify-branch-join-mir [core mir]))
          native (stage :native-boundary #(b/p15-s23-c11-mir-validate-constructed! path core mir))
          binder-id (get-in core [:core-nodes 0 :node-id]) entry (get-in mir [:functions 'main :entry])
          actual-macro b/sh05-macro-artifact-from-c3-artifact]
      (is (= :passed (:status gravity)))
      (is (= (str "sha256:" (b/sha256-hex text)) (:source-content-hash core)))
      (is (= [binder-id] (get-in mir [:functions 'main :params])))
      (is (= {:value-id binder-id :parameter-index 0 :block-id entry}
             (get-in mir [:data-flow-graph :definitions binder-id])))
      (is (= :parameter (get-in mir [:data-flow-graph :values binder-id :kind])))
      (is (not-any? #(= binder-id (:op-id %)) (mapcat :instructions (vals (get-in mir [:functions 'main :blocks])))))
      ;; The original rejection remains historical evidence. The new narrow
      ;; Native entry-value path must now accept this same genuine artifact.
      (is (= :passed (:status native)))
      (prn {:parameter-native-boundary (select-keys native [:status :operation-count])}) (flush)
      (testing "real owner validation rejects invalid invocation and substituted parent/source"
        (is (= ["C3-FACT-STALE" [:reader-products-authentic?]]
               ((juxt :id :missing-fields) (diagnostic #(actual-macro path parent))))))
      (testing "explicit context never grants source authority"
        (doseq [[supplied-invocation missing]
                [[(assoc invocation :authority :supplied) :explicit-sh03-reader-invocation-source-selection]
                 [(assoc invocation :authenticated-envelope-options {}) :explicit-sh03-reader-invocation-source-selection]
                 [(assoc invocation :reader-source-path (str (.resolve root "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")))
                  :selected-current-sh03-reader-source]]]
          (let [failure (diagnostic #(actual-macro path parent supplied-invocation))]
            (is (= ["C2-HASH" [missing]] ((juxt :id :missing-fields) failure)))))
        (doseq [[supplied-path supplied-parent]
                [[(str path ".other.gravity") parent]
                 [path (assoc-in parent [:c2-reader-artifact :source-unit-record :bytes-hash] "sha256:stale")]]]
          (let [failure (diagnostic #(actual-macro supplied-path supplied-parent invocation))]
            (is (= ["C4-RETURN" [:authenticated-c3-syntax-parent]]
                   ((juxt :id :missing-fields) failure)))))
        (let [stale (assoc-in parent [:c2-reader-artifact :sh03-reader-authentication :reader-source-revision :source-content-hash]
                              (str "sha256:" (apply str (repeat 64 "0"))))
              failure (diagnostic #(b/c3-syntax-validate! path stale invocation))]
          (is (= ["C3-FACT-STALE" [:reader-products-authentic?]] ((juxt :id :missing-fields) failure))))))))

(deftest current-reader-parameter-construction-and-fresh-replay-reach-native-boundary
  ;; This ordinary single-JVM composition can also be checked in two bounded
  ;; children: serialize only genuine original C6 DATA, then reconstruct fresh
  ;; source products and compare the complete result before owner verification.
  (let [original (current-parameter-case nil)
        fresh (current-parameter-case (get-in original [:products :compiled-binding-data]))]
    (check-parameter-replay (:raw original) fresh)))
