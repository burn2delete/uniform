(ns gravity.self-hosting.stage2-module-lexical-call-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; L2 sequential lexical scope/L3 exact imported origins. This is explicitly
;; unadmitted Clojure seed module execution, not checked user-program linking.
(def ^:private runtime {:engine :stage2-module-lexical-call-test})
(defn- fixture [family name extension]
  (loop [root (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/stage2_module_lexical_call_test.clj"))))]
    (if (.isFile (io/file root "deps.edn"))
      (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" family "/" name extension)))
      (recur (.getParentFile root)))))
(defn- diagnostic [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn- invoke [bundle entry]
  (let [value (atom nil)
        output (with-out-str
                 (reset! value (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle entry [])))]
    {:value @value :output output}))

(deftest stage2-module-let-fixture-parity
  (doseq [[family name] [["modules" "module-let-caller"] ["modules" "module-let-leaf"]
                         ["rejected" "module-let-private-import"]]]
    (let [g (fixture family name ".gravity") q (fixture family name ".qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (bootstrap/read-forms g (slurp g)) (bootstrap/read-forms q (slurp q)))))))

(deftest stage2-module-let-call-path-development-kernel
  ;; Normalized compiler routing DATA, not a substitute for authentic C5 below.
  (let [form '(let [x :outer y (leaf/identity x) z (leaf/identity y)]
                (leaf/identity z)
                (let [x :inner z (leaf/identity x)]
                  (leaf/identity y)
                  (leaf/identity z)))
        sites (bootstrap/p15-s23-stage2-module-call-sites {:name 'main :body [form]})
        paths [[0 1 3] [0 1 5] [0 2] [0 3 1 3] [0 3 2] [0 3 3]]
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
    (is (= 'x (get-in compiled [:bindings 1 :expr :args 0 :name])))
    (is (= 'x (get-in compiled [:body 1 :bindings 1 :expr :args 0 :name])))
    (is (= 'y (get-in compiled [:body 1 :body 0 :args 0 :name])))
    (is (= 'z (get-in compiled [:body 1 :body 1 :args 0 :name])))
    (is (= [[0 2] [0 3]]
           (mapv :path (bootstrap/p15-s23-stage2-module-call-sites
                        {:name 'empty-bindings :body ['(let [] (leaf/identity :first) (leaf/identity :second))]}))))
    (is (= [] (bootstrap/p15-s23-stage2-module-call-sites
                {:name 'data :body ['(let [payload '(leaf/hidden :data)] payload)]})))
    (doseq [body ['(fn [x] (loop [y x] (leaf/identity y)))
                  '(fn [x] (let [y x] (leaf/identity y)))
                  '{:blocked (let [x :value] (leaf/identity x))}
                  '(let [x #{(leaf/identity :value)}] x)]]
      (is (= :unsupported-nested-module-call
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites {:name 'bad :body [body]})) [:facts :reason]))))
    (is (= :symbol-only-sequential-let-required
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites
                                 {:name 'bad :body ['(let [[x] :value] (leaf/identity x))]})) [:facts :reason])))
    (is (= "L2-UNKNOWN-SYMBOL"
           (:id (diagnostic #(bootstrap/p15-s23-stage2-seed-compile-expr
                              emitter module #{} '(let [x y y :later] x))))))
    (let [original bootstrap/p15-s23-stage2-seed-compile-expr seen (atom [])]
      (doseq [context [{:p15-s23-sh07-callsite-path [9]}
                       {:p15-s23-sh07-callsite-path [9] :p15-s23-stage2-module-routes {}}]]
        (reset! seen [])
        (with-redefs [bootstrap/p15-s23-stage2-seed-compile-expr
                      (fn [_ m locals value]
                        (swap! seen conj [(:p15-s23-sh07-callsite-path m) locals value])
                        {:op :literal :value value})]
          (bootstrap/p15-s23-stage2-seed-compile-let emitter context #{} '[x :first y x] '[y x]))
        (is (= (if (contains? context :p15-s23-stage2-module-routes)
                 [[9 1 1] [9 1 3] [9 2] [9 3]] (repeat 4 [9])) (map first @seen)))
        (is (= [#{} '#{x} '#{x y} '#{x y}] (map second @seen))))
      (is (identical? original bootstrap/p15-s23-stage2-seed-compile-expr)))))

(deftest stage2-module-let-authenticated-composition
  ;; G is the actual compiler/execution case. Q byte/parser parity is separate;
  ;; this does not claim fresh Q C5 or bundle execution.
  (let [leaf-path (fixture "modules" "module-let-leaf" ".gravity")
                 caller-path (fixture "modules" "module-let-caller" ".gravity")
                 text (slurp caller-path)
                 emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                 leaf (bootstrap/compiler-c5-resolution-file-artifact leaf-path)
                 module-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [leaf]}
                 caller (bootstrap/compiler-c5-resolution-file-artifact caller-path module-inputs)
                 dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path (slurp leaf-path))
                 inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                 bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path text inputs)
                 root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle)))
                 routes (:routes bundle)]
             (is (= :accepted (:status leaf) (:status caller)))
             (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
             (is (= :not-checked (get-in bundle [:boundary :effects])))
             (is (false? (get-in bundle [:boundary :self-hosted?])))
             (is (= #{'main} (set (map :name (:exports root)))))
             (is (= #{['main [0 1 3]] ['main [0 1 5 2]] ['main [0 2 1 5]] ['main [0 2 1 7]] ['quoted-data [0 2]]}
                    (set (map (juxt :caller-function :callsite-path) routes))))
             (is (= 5 (count routes) (count (set (map :route-id routes)))))
             (is (= #{'leaf/identity} (set (map :symbol routes))))
             (is (= (mapv :syntax-id (filter #(= 'leaf/identity (:symbol %))
                                            (get-in caller [:gravity-resolution-boundary :authenticated-resolution-request :references])))
                    (mapv :reference-syntax-id routes)))
             (is (= #{(:artifact-id leaf)} (set (map :callee-module-id routes))))
             (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in root [:plan :source :sha256])))
             (is (= {:value ["outer" '(leaf/hidden :data)] :output "first\nsecond\ninner\n"} (invoke bundle 'main)))
    (let [path (fixture "rejected" "module-let-private-import" ".gravity")
          failure (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact path module-inputs))]
      (is (= "C5-PRIVATE" (:id failure)))
      (is (map? (:source-span failure))))
    (let [changed (str/replace-first text "(leaf/identity x)" "(leaf/identity x x)")
          artifact (bootstrap/compiler-c5-resolution-source-artifact caller-path changed module-inputs)]
      (is (= :accepted (:status artifact)))
      (is (= "L2-FUNCTION-ARITY"
             (:id (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                 emitter caller-path changed (assoc inputs :caller-resolution-artifact artifact)))))))
      (let [changed (assoc-in bundle [:routes 0 :callsite-path] [999])
            identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id changed) (:modules changed) (:routes changed))
            forged (assoc changed :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))
            failure (atom nil)
            output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'main []))))]
        (is (= (:bundle-id forged) (bootstrap/reader-canonical-hash (:identity-input forged))))
        (is (= :module-bundle-reconstruction-mismatch (get-in @failure [:facts :reason])))
        (is (= "" output)))))
