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

(deftest current-reader-parameter-construction-and-fresh-replay-reach-native-boundary
  ;; Genuine development composition; historical pinned admission stays closed.
  (binding [b/*p15-s23-stage2-compiler-artifact-binding*
            @(var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'current-emitter))]
    (let [root @(owner 'root)
          path (str (.resolve root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source.gravity"))
          text (slurp path)
          reader (str (.resolve root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
          invocation {:reader-source-path reader :authenticated-envelope-options {:source :development-current}}
          observed (atom []) actual-macro b/sh05-macro-artifact-from-c3-artifact
          source-products
          (fn [binding-data]
            (with-redefs [b/sh05-macro-artifact-from-c3-artifact
                          (fn [& arguments]
                            (swap! observed conj (vec arguments))
                            (apply actual-macro arguments))]
              ((owner 'parameter-source-products) path text binding-data reader)))
          products (stage :original-source-products #(source-products nil))
          call (owner 'invoke) plan (owner 'parameter-c6-plan)
          raw (stage :original-c6 #(call plan 'c6-c10-build-checked-core-template [(:envelope products)]))
          fresh (stage :fresh-source-products #(source-products (:compiled-binding-data products)))
          replay (stage :fresh-c6-replay #(call plan 'c6-c10-build-checked-core-template [(:envelope fresh)]))
          upstream (stage :fresh-c6-verifier
                          #(call plan 'c6-c10-verify-checked-core-template
                                 [(:envelope fresh) (:artifact-template raw) (:digest-requests replay)]))
          sealed (b/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (b/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (b/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
          mir (stage :gravity-mir #(call (owner 'c11-plan) 'sh12-build-branch-join-mir [core]))
          gravity (stage :gravity-verifier #(call (owner 'c11-plan) 'sh12-verify-branch-join-mir [core mir]))
          native (stage :native-boundary #(diagnostic (fn [] (b/p15-s23-c11-mir-validate-constructed! path core mir))))
          parent (second (first @observed))
          binder-id (get-in core [:core-nodes 0 :node-id])
          entry (get-in mir [:functions 'main :entry])]
      (is (= :accepted (:status raw)))
      (is (= raw replay))
      (is (= :passed (:status upstream) (:status gravity)))
      (is (= (str "sha256:" (b/sha256-hex text)) (:source-content-hash core)))
      (is (= 2 (count @observed)))
      (doseq [arguments @observed]
        (is (= 3 (count arguments)))
        (is (= path (first arguments)))
        (is (= invocation (last arguments))))
      (is (= [binder-id] (get-in mir [:functions 'main :params])))
      (is (= {:value-id binder-id :parameter-index 0 :block-id entry}
             (get-in mir [:data-flow-graph :definitions binder-id])))
      (is (= :parameter (get-in mir [:data-flow-graph :values binder-id :kind])))
      (is (not-any? #(= binder-id (:op-id %)) (mapcat :instructions (vals (get-in mir [:functions 'main :blocks])))))
      ;; This separately records the next Native boundary, not compatibility.
      (is (= {:id "C11-MODULE" :missing-fact :authenticated-c11-function-envelope :fallback-status :rejected}
             (select-keys native [:id :missing-fact :fallback-status])))
      (testing "real owner validation rejects invalid invocation and substituted parent/source"
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
