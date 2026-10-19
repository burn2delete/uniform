(ns gravity.self-hosting.stage2-module-vector-call-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; L2 ordered evaluation/quote, STD3 ordered Vectors, L3 authenticated imports.
;; This tests temporary seed development, not C7/C8 legality or admission.
(def ^:private runtime {:engine :stage2-module-vector-call-test})
(defn- fixture [family name extension]
  (loop [root (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/stage2_module_vector_call_test.clj"))))]
    (if (.isFile (io/file root "deps.edn"))
      (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" family "/" name extension)))
      (recur (.getParentFile root)))))
(defn- diagnostic [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest stage2-module-vector-fixture-parity
  (doseq [[family name] [["modules" "module-vector-root"] ["modules" "module-vector-leaf"]
                         ["rejected" "module-vector-private-import"]]]
    (let [g (fixture family name ".gravity") q (fixture family name ".qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (bootstrap/read-forms g (slurp g)) (bootstrap/read-forms q (slurp q)))))))

(deftest stage2-module-vector-call-path-development-kernel
  ;; Normalized route DATA checks path compilation, not genuine C5 authority.
  (let [form '[(leaf/identity :first)
               [(let [x (leaf/identity :inner)] (leaf/identity x))]
               '(leaf/hidden :data)
               (leaf/pair (leaf/identity :left) (leaf/identity :right))]
        paths [[0 0] [0 1 0 1 1] [0 1 0 2] [0 3] [0 3 1] [0 3 2]]
        sites (bootstrap/p15-s23-stage2-module-call-sites {:name 'main :body [form]})
        routes (into {} (map (fn [{:keys [path symbol]}]
                              [['main path] {:route-id path :caller-function 'main :callsite-path path :symbol symbol}]) sites))
        emitter {:special-form-rules {'let {:op :let} 'quote {:op :quote}}}
        module {:p15-s23-stage2-module-routes routes :p15-s23-sh07-enclosing-function 'main
                :p15-s23-sh07-callsite-path [0]}
        compiled (bootstrap/p15-s23-stage2-seed-compile-expr emitter module #{} form)
        calls (filter #(= :compiler-module-call (:op %))
                      (bootstrap/p15-s23-stage2-module-instructions {:functions {'main {:instructions [compiled]}}}))]
    (is (= paths (mapv :path sites)))
    (is (= (set paths) (set (map :callsite-path calls))))
    (is (= 6 (count calls) (count (set (map :route-id calls)))))
    (is (= :vector-literal (:op compiled)))
    (is (= :vector-literal (get-in compiled [:items 1 :op])))
    (is (= 'x (get-in compiled [:items 1 :items 0 :body 0 :args 0 :name])))
    (is (= {:op :quote :value '(leaf/hidden :data)} (get-in compiled [:items 2])))
    (is (= [] (bootstrap/p15-s23-stage2-module-call-sites {:name 'empty :body [[]]})))
    (doseq [body ['(fn [x] [(leaf/identity x)])
                  '{:blocked [(leaf/identity :value)]}
                  '#{[(leaf/identity :value)]}
                  '[(fn [x] (leaf/identity x))]
                  '[{:blocked (leaf/identity :value)}]]]
      (is (= :unsupported-nested-module-call
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites {:name 'bad :body [body]})) [:facts :reason]))))
    (let [seen (atom []) original bootstrap/p15-s23-stage2-seed-compile-expr]
      (doseq [context [{:p15-s23-sh07-callsite-path [9]}
                       {:p15-s23-sh07-callsite-path [9] :p15-s23-stage2-module-routes {}}]
              value [[:first :second] {:key :value} #{:item} []]]
        (reset! seen [])
        (let [result (with-redefs [bootstrap/p15-s23-stage2-seed-compile-expr
                                  (fn [_ m locals item]
                                    (swap! seen conj [m locals item]) {:op :literal :value item})]
                       (bootstrap/p15-s23-stage2-seed-compile-collection emitter context '#{x} value))]
          (if (and (vector? value) (contains? context :p15-s23-stage2-module-routes))
            (is (= (mapv #(vector 9 %) (range (count value)))
                   (mapv #(get-in % [0 :p15-s23-sh07-callsite-path]) @seen)))
            (is (every? #(identical? context (first %)) @seen)))
          (is (every? #(= '#{x} (second %)) @seen))
          (when (vector? value) (is (= value (mapv :value (:items result)))))))
      (is (identical? original bootstrap/p15-s23-stage2-seed-compile-expr)))))

(deftest stage2-module-vector-recur-diagnostics-development-kernel
  (doseq [body ['[(recur)]
                '[(do (recur) :after)]
                '[(recur) (loop [x] x)]
                '[(throw) (loop [:x :x] :x)]]]
    (let [definition {:source-path "normalized-vector.gravity" :name 'bad :params [] :body [body]}
          original (diagnostic #(bootstrap/p15-s23-stage2-seed-validate-tail-body! definition [body] 0 true))]
      (is (some? original))
      (if (some #(and (seq? %) (= 'loop (first %))) body)
        (is (= original (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites definition))))
        (do (is (= "L2-RECUR-TARGET" (:id original)))
            (is (= :non-tail-position (:reason original))))))))

(deftest stage2-module-vector-authenticated-composition
  ;; G is the actual C5/bundle/runtime case; paired Q is parser-only above.
  (let [leaf-path (fixture "modules" "module-vector-leaf" ".gravity")
        path (fixture "modules" "module-vector-root" ".gravity") text (slurp path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        _ (println :vector-module-phase :leaf-c5)
        leaf (bootstrap/compiler-c5-resolution-file-artifact leaf-path)
        c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [leaf]}
        _ (println :vector-module-phase :root-c5)
        caller (bootstrap/compiler-c5-resolution-file-artifact path c5-inputs)
        dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path (slurp leaf-path))
        inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
        _ (println :vector-module-phase :bundle)
        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text inputs)
        root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle)))
        routes (:routes bundle)
        instructions (bootstrap/p15-s23-stage2-module-instructions (:plan root))]
    (is (= :accepted (:status leaf) (:status caller)))
    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
    (is (= :not-checked (get-in bundle [:boundary :effects])))
    (is (false? (get-in bundle [:boundary :self-hosted?])))
    (is (= #{:memory/allocate :io/write} (get-in root [:plan :effect-summary :declared])))
    (is (= #{'main} (set (map :name (:exports root)))))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in root [:plan :source :sha256])))
    (is (= 6 (count routes) (count (filter #(= :compiler-module-call (:op %)) instructions))))
    (is (= (mapv :syntax-id (filter #(contains? '#{leaf/identity leaf/pair} (:symbol %))
                                  (get-in caller [:gravity-resolution-boundary :authenticated-resolution-request :references])))
           (mapv :reference-syntax-id routes)))
    (doseq [route routes]
      (is (= (:artifact-id leaf) (:callee-module-id route) (get-in route [:origin :ordinary-module-artifact-id]))))
    (let [result (atom nil)
          _ (println :vector-module-phase :runtime)
          output (with-out-str (reset! result (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'main [])))]
      (is (= ["kept" ["kept"] '(leaf/hidden :data) [:left :right]] @result))
      (is (= "first\nloop\nleft\nright\n" output)))
    (let [changed (assoc-in bundle [:routes 0 :callsite-path] [999])
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id changed) (:modules changed) (:routes changed))
          forged (assoc changed :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))
          failure (atom nil)
          output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'main []))))]
      (is (= (:bundle-id forged) (bootstrap/reader-canonical-hash identity)))
      (is (= :module-bundle-reconstruction-mismatch (get-in @failure [:facts :reason])))
      (is (= "" output)))
    (let [bad (fixture "rejected" "module-vector-private-import" ".gravity")
          failure (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact bad c5-inputs))]
      (is (= "C5-PRIVATE" (:id failure)))
      (is (= bad (get-in failure [:source-span :source]))))
    (let [changed (str/replace-first text "(leaf/identity (do (println \"first\") prefix))"
                                     "(leaf/identity (do (println \"first\") prefix) :extra)")
          artifact (bootstrap/compiler-c5-resolution-source-artifact path changed c5-inputs)
          failure (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                emitter path changed (assoc inputs :caller-resolution-artifact artifact)))]
      (is (not= text changed))
      (is (= :accepted (:status artifact)))
      (is (= "L2-FUNCTION-ARITY" (:id failure)))
      (is (= [1 2] [(:expected-arity failure) (:actual-arity failure)])))))
