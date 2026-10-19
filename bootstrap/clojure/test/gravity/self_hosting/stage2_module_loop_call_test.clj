(ns gravity.self-hosting.stage2-module-loop-call-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; L2 sequential loop scope/nearest recur and L3 source-bound imported calls.
;; This remains temporary, unadmitted compiler-artifact seed execution.
(def ^:private runtime {:engine :stage2-module-loop-call-test})
(defn- fixture [family name extension]
  (loop [root (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/stage2_module_loop_call_test.clj"))))]
    (if (.isFile (io/file root "deps.edn"))
      (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" family "/" name extension)))
      (recur (.getParentFile root)))))
(defn- diagnostic [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest stage2-module-loop-fixture-parity
  (doseq [[family name] [["modules" "module-loop-root"] ["modules" "module-loop-leaf"]
                         ["rejected" "module-loop-private-import"]]]
    (let [g (fixture family name ".gravity") q (fixture family name ".qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (bootstrap/read-forms g (slurp g)) (bootstrap/read-forms q (slurp q)))))))

(deftest stage2-module-loop-call-path-development-kernel
  ;; Normalized routes exercise only path/lexical compilation, not C5 authority.
  (let [form '(loop [again (leaf/identity true) value (leaf/identity :outer)]
                (leaf/identity value)
                (if again
                  (recur (leaf/identity false) (leaf/identity value))
                  (let [outer value]
                    (loop [again (leaf/identity true) value (leaf/identity :inner)]
                      (if again (recur false value) (leaf/pair outer value))))))
        sites (bootstrap/p15-s23-stage2-module-call-sites {:name 'main :body [form]})
        paths [[0 1 1] [0 1 3] [0 2] [0 3 2 1] [0 3 2 2]
               [0 3 3 2 1 1] [0 3 3 2 1 3] [0 3 3 2 2 3]]
        routes (into {} (map (fn [{:keys [path symbol]}]
                              [['main path] {:route-id path :caller-function 'main :callsite-path path :symbol symbol}]) sites))
        emitter {:special-form-rules {'let {:op :let} 'if {:op :if} 'quote {:op :quote}}}
        module {:p15-s23-stage2-module-routes routes :p15-s23-sh07-enclosing-function 'main
                :p15-s23-sh07-callsite-path [0]}
        compiled (bootstrap/p15-s23-stage2-seed-compile-expr emitter module #{} form)
        instructions (bootstrap/p15-s23-stage2-module-instructions {:functions {'main {:instructions [compiled]}}})
        calls (filter #(= :compiler-module-call (:op %)) instructions)]
    (is (= :passed (bootstrap/p15-s23-stage2-seed-validate-recur! module form 0 true)))
    (is (= paths (mapv :path sites)))
    (is (= (set paths) (set (map :callsite-path calls))))
    (is (= 8 (count calls) (count (set (map :route-id calls)))))
    (is (= 2 (count (filter #(= :loop (:op %)) instructions))))
    (is (= 'value (get-in compiled [:body 0 :args 0 :name])))
    (is (= 'value (get-in compiled [:body 1 :then :args 1 :args 0 :name])))
    (is (= 'value (get-in compiled [:body 1 :else :bindings 0 :expr :name])))
    (is (= [:outer :inner]
           [(get-in compiled [:bindings 1 :expr :args 0 :value])
            (get-in compiled [:body 1 :else :body 0 :bindings 1 :expr :args 0 :value])]))
    (is (= [] (bootstrap/p15-s23-stage2-module-call-sites
                {:name 'data :body ['(loop [payload '(leaf/hidden :data)] payload)]})))
    (doseq [body ['(fn [x] (loop [y x] (leaf/identity y)))
                  '[(loop [x :value] (leaf/identity x))]
                  '(loop [x [(leaf/identity :value)]] x)
                  '(loop [x :value] {:key (leaf/identity x)})]]
      (is (= :unsupported-nested-module-call
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites {:name 'bad :body [body]})) [:facts :reason]))))
    (let [seen (atom []) original bootstrap/p15-s23-stage2-seed-compile-expr]
      (doseq [context [{:p15-s23-sh07-callsite-path [9]}
                       {:p15-s23-sh07-callsite-path [9] :p15-s23-stage2-module-routes {}}]]
        (reset! seen [])
        (with-redefs [bootstrap/p15-s23-stage2-seed-compile-expr
                      (fn [_ m locals value]
                        (swap! seen conj [m locals value]) {:op :literal :value value})]
          (bootstrap/p15-s23-stage2-seed-compile-loop emitter context #{} '[x :first y x] '[y x]))
        (is (= (if (contains? context :p15-s23-stage2-module-routes)
                 [[9 1 1] [9 1 3] [9 2] [9 3]] (repeat 4 [9]))
               (map #(get-in % [0 :p15-s23-sh07-callsite-path]) @seen)))
        (is (= [#{} '#{x} '#{x y} '#{x y}] (map second @seen)))
        (when-not (contains? context :p15-s23-stage2-module-routes)
          (is (every? #(identical? context (first %)) @seen))))
      (is (identical? original bootstrap/p15-s23-stage2-seed-compile-expr)))))

(deftest stage2-module-loop-recur-diagnostics-development-kernel
  (let [module {:source-path "normalized-loop.gravity"}]
    (doseq [[form target expected]
            [['(recur :x) nil :missing-target]
             ['(loop [x :x] (do (recur x) :after)) 0 :non-tail-position]
             ['(loop [x (recur)] x) 0 :non-tail-position]
             ['(loop [x :x] (recur)) 0 :arity-mismatch]
             ['(loop [x] x) 0 :invalid-loop-bindings]
             ['(loop [:x :x] :x) 0 :invalid-loop-binding-name]
             ['(loop [x :x]) 0 :missing-loop-body]
             ['(loop [x :x] (loop [a :a b :b] (recur a))) 0 :arity-mismatch]]]
      (let [failure (diagnostic #(bootstrap/p15-s23-stage2-seed-validate-recur! module form target true))]
        (is (= "L2-RECUR-TARGET" (:id failure)))
        (is (= expected (:reason failure)))
        (is (= "normalized-loop.gravity" (get-in failure [:source-span :source])))))
    (doseq [form ['(loop [x] x) '(loop [:x :x] :x)]]
      (let [original (diagnostic #(bootstrap/p15-s23-stage2-seed-validate-recur! module form 0 true))
            walker (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites
                                 (assoc module :name 'bad :body [form])))]
        (is (= (select-keys original [:id :form :reason :target-arity :actual-arity :source-span])
               (select-keys walker [:id :form :reason :target-arity :actual-arity :source-span])))))
    (let [first-bad '(loop [x] x) second-bad '(loop [:x :x] :x)
          failure (diagnostic #(bootstrap/p15-s23-stage2-seed-validate-recur!
                                module (list 'do first-bad second-bad) 0 true))]
      (is (= first-bad (:form failure))))))

(deftest stage2-module-loop-authenticated-composition
  ;; G runs original C5/bundle/runtime. Q parity above grants no Q typed credit.
  (let [leaf-path (fixture "modules" "module-loop-leaf" ".gravity")
        path (fixture "modules" "module-loop-root" ".gravity")
        text (slurp path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        _ (println :loop-module-phase :leaf-c5)
        leaf (bootstrap/compiler-c5-resolution-file-artifact leaf-path)
        module-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [leaf]}
        _ (println :loop-module-phase :root-c5)
        caller (bootstrap/compiler-c5-resolution-file-artifact path module-inputs)
        dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path (slurp leaf-path))
        inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
        _ (println :loop-module-phase :bundle)
        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text inputs)
        root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle)))
        routes (:routes bundle)
        instructions (bootstrap/p15-s23-stage2-module-instructions (:plan root))]
    (is (= :accepted (:status leaf) (:status caller)))
    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
    (is (= :not-checked (get-in bundle [:boundary :effects])))
    (is (false? (get-in bundle [:boundary :self-hosted?])))
    (is (= #{'main} (set (map :name (:exports root)))))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in root [:plan :source :sha256])))
    (is (= 2 (count (filter #(= :loop (:op %)) instructions))))
    (is (= 10 (count routes) (count (filter #(= :compiler-module-call (:op %)) instructions))))
    (is (= #{(:artifact-id leaf)} (set (map :callee-module-id routes))))
    (is (= (mapv :syntax-id (filter #(contains? '#{leaf/identity leaf/pair} (:symbol %))
                                  (get-in caller [:gravity-resolution-boundary :authenticated-resolution-request :references])))
           (mapv :reference-syntax-id routes)))
    (doseq [route routes]
      (is (= (:artifact-id caller) (:caller-module-id route)))
      (is (= (:artifact-id leaf) (get-in route [:origin :ordinary-module-artifact-id]))))
    (let [result (atom nil)
          _ (println :loop-module-phase :runtime)
          output (with-out-str (reset! result (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'main [])))]
      (is (= ["outer" "inner"] @result))
      ;; Both loops really transfer once; initializers are not repeated.
      (is (= "flag\ninit\nbody\nstep\nbody\nnested\n" output)))
    (let [changed (assoc-in bundle [:routes 0 :callsite-path] [999])
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id changed) (:modules changed) (:routes changed))
          forged (assoc changed :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))
          failure (atom nil)
          output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'main []))))]
      (is (= (:bundle-id forged) (bootstrap/reader-canonical-hash identity)))
      (is (= :module-bundle-reconstruction-mismatch (get-in @failure [:facts :reason])))
      (is (= "" output)))
    (let [bad-path (fixture "rejected" "module-loop-private-import" ".gravity")
          failure (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact bad-path module-inputs))]
      (is (= "C5-PRIVATE" (:id failure)))
      (is (= bad-path (get-in failure [:source-span :source]))))
    (let [changed (str/replace-first text "(leaf/identity (do (println \"flag\") true))"
                                     "(leaf/identity (do (println \"flag\") true) :extra)")
          artifact (bootstrap/compiler-c5-resolution-source-artifact path changed module-inputs)
          failure (atom nil)
          output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                                           emitter path changed (assoc inputs :caller-resolution-artifact artifact)))))]
      (is (not= text changed))
      (is (= :accepted (:status artifact)))
      (is (= "L2-FUNCTION-ARITY" (:id @failure)))
      (is (= 1 (:expected-arity @failure)))
      (is (= 2 (:actual-arity @failure)))
      (is (= "" output)))))
