(ns gravity.self-hosting.stage2-module-map-call-test
  (:require [clojure.java.io :as io] [clojure.string :as str] [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap] [gravity.self-hosting.stage2-module-map-order-test]))

;; L2 evaluation order and L3 original bindings, NOT STD3 Map enumeration.
;; Temporary seed composition; effects remain unchecked and Q is parser-only.
(defn- diagnostic [f] (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))
(defn- fixture [family name extension]
  (loop [root (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/stage2_module_map_call_test.clj"))))]
    (if (.isFile (io/file root "deps.edn"))
      (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" family "/" name extension)))
      (recur (.getParentFile root)))))
(defn- supplied-source [body keys]
  ;; Reuse supplied C2-shaped records, never a genuine C5 acceptance receipt.
  ((ns-resolve 'gravity.self-hosting.stage2-module-map-order-test 'normalized-source) body keys))
(defn- with-order [source]
  (merge (get-in source [:functions 'main])
         {:source-path (:source-path source)
          :p15-s23-stage2-map-order (bootstrap/p15-s23-stage2-module-map-order source)}))
(defn- reference-input [source actual-body]
  (let [syntax (assoc (first (:syntax source)) :span {:source "normalized-map.gravity" :byte-start 9 :byte-end 999}
                      :form (list 'def 'main (list 'fn [] actual-body)))
        prefix {:syntax/id :prefix :span {:byte-start 0 :byte-end 8} :form '(def prefix (fn [] :prefix))}
        request (bootstrap/sh06-resolution-analysis-inputs {} {:expanded-syntax-stream [prefix syntax]} {})]
    [(assoc source :map-order (:p15-s23-stage2-map-order (with-order source)) :request request) syntax]))

(deftest stage2-module-map-fixture-parity
  (doseq [[family name] [["modules" "module-map-caller"] ["modules" "module-map-leaf"]
                         ["rejected" "module-map-private-import"]]]
    (let [g (fixture family name ".gravity") q (fixture family name ".qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (bootstrap/read-forms g (slurp g)) (bootstrap/read-forms q (slurp q)))))))

(deftest stage2-module-map-reference-development-kernel
  (let [keys [:z :b :y :a :x :c :w :d :v]
        pairs (mapv #(vector % (list 'leaf/identity (list 'do (list 'println (name %)) 'local))) keys)
        maps [(into {} pairs) (into (sorted-map) pairs) (apply array-map (mapcat identity pairs))]]
    (doseq [seed maps actual maps]
      (let [body (list 'let ['local :local] seed) source (supplied-source body keys)
            [caller syntax] (reference-input source (list 'let ['local :local] actual))
            definition (with-order source)
            index (bootstrap/p15-s23-stage2-module-map-reference-index caller syntax definition)
            sites (bootstrap/p15-s23-stage2-module-call-sites definition)
            original (filterv #(= 'leaf/identity (:symbol %)) (get-in caller [:request :references]))
            by-key (zipmap (map first actual) original)
            slots (zipmap (map first seed) (range))
            expected (into {} (map (fn [key] [[0 2 (inc (* 2 (get slots key)))] (get by-key key)])) keys)]
        (is (= expected index))
        (is (= (mapv #(vector 0 2 (inc (* 2 (get slots %)))) keys) (mapv :path sites)))
        (is (= (mapv by-key keys) (mapv #(get index (:path %)) sites)))
        (is (= 9 (count sites) (count (set (map :syntax-id (vals index))))))
        (is (every? #(seq (:scope-chain %)) (vals index)))))
    (let [body (list 'let ['local :local] (first maps)) source (supplied-source body keys)
          [caller syntax] (reference-input source body) definition (with-order source)
          refs (get-in caller [:request :references])
          op (first (keep-indexed #(when (= 'leaf/identity (:symbol %2)) %1) refs))
          local (first (keep-indexed #(when (= 'local (:symbol %2)) %1) refs))
          bads [(assoc-in caller [:request :references local :symbol] 'other)
                (update-in caller [:request :references] #(into (conj (subvec % 0 2) (nth % 3) (nth % 2)) (drop 4 %)))
                (assoc-in caller [:request :references op :qualifier] 'other)
                (assoc-in caller [:request :references op :name] 'other)
                (assoc-in caller [:request :references op :position] :expression)
                (assoc-in caller [:request :references op :syntax-id] :forged)
                (assoc-in caller [:request :references op :semantic-span :ordinal] 999)
                (assoc-in caller [:request :references op :source-span :byte-start] 8)
                (update-in caller [:request :references] #(vec (concat (subvec % 0 op) (subvec % (inc op)))))
                (update-in caller [:request :references] conj (nth refs op))]]
      (doseq [bad bads]
        (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-module-map-reference-index bad syntax definition))))))
      (is (= :map-reference-occurrence-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-map-reference-index
                                   caller (assoc syntax :syntax/id :other-owner) definition)) [:facts :reason])))
      (is (= :unsupported-map-reference-header
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-map-reference-index
                                   caller (assoc syntax :form '(def main (fn [x :- String] :body))) definition)) [:facts :reason])))
      (is (= :map-reference-work-limit
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-map-reference-index
                                   (assoc-in caller [:request :references] (vec (repeat 65537 (first refs)))) syntax definition)) [:facts :reason])))
      (is (= :map-source-order-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-map-reference-index
                                   (assoc-in caller [:map-order ['main [0 2]] :host-keys] [:wrong]) syntax definition)) [:facts :reason]))))))

(deftest stage2-module-map-context-fences-development-kernel
  (doseq [body ['#{ {:k (leaf/identity :x)} } '(fn [] {:k (leaf/identity :x)})
                '{:k #{(leaf/identity :x)}} '{:k (fn [] (leaf/identity :x))}]]
    (let [source (supplied-source body [:k]) definition (with-order source)]
      (is (seq (:p15-s23-stage2-map-order definition)))
      (is (= :unsupported-nested-module-call
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites definition)) [:facts :reason])))))
  (let [body '(let [x {:z (leaf/identity :z) :a [(leaf/identity :a)]}]
                (loop [again (leaf/identity false)]
                  (if again (recur (leaf/identity false)) (leaf/identity x))))
        source (supplied-source body [:z :a]) sites (bootstrap/p15-s23-stage2-module-call-sites (with-order source))]
    (is (= ['(leaf/identity :z) '(leaf/identity :a) '(leaf/identity false)
            '(leaf/identity false) '(leaf/identity x)] (mapv :form sites)))
    (is (= (count sites) (count (set (map :path sites))))))
  (let [body '(quote {:k (leaf/hidden :data)}) source (supplied-source body [:k])]
    (is (= {} (:p15-s23-stage2-map-order (with-order source))))
    (is (= [] (bootstrap/p15-s23-stage2-module-call-sites (with-order source)))))
  (doseq [[value keys expected] [[{:a '(recur) :z '(throw)} [:z :a] "L2-THROW-ILLEGAL"]
                                [{:a '(throw) :z '(recur)} [:z :a] "L2-RECUR-TARGET"]]]
    (let [body (list 'do value '(loop [x] x)) source (supplied-source body keys) definition (with-order source)
          original (diagnostic #(bootstrap/p15-s23-stage2-seed-validate-tail-body! definition [body] 0 true))]
      (is (= expected (:id original)))
      (is (= original (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites definition)))))))

(deftest stage2-module-map-authenticated-composition
  ;; Six G C5 attempts; Q only parser parity. Original fresh public gates remain.
  (let [leaf-path (fixture "modules" "module-map-leaf" ".gravity") path (fixture "modules" "module-map-caller" ".gravity")
        text (slurp path) emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        _ (println :map-call-phase :leaf-c5) leaf (bootstrap/compiler-c5-resolution-file-artifact leaf-path)
        c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [leaf]}
        _ (println :map-call-phase :caller-c5) caller (bootstrap/compiler-c5-resolution-file-artifact path c5-inputs)
        dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path (slurp leaf-path))
        inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
        _ (println :map-call-phase :bundle) bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text inputs)
        root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle)))
        request (get-in caller [:gravity-resolution-boundary :authenticated-resolution-request])
        refs (into {} (map (juxt :syntax-id identity)) (:references request)) routes (:routes bundle)]
    (is (= :accepted (:status leaf) (:status caller)))
    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
    (is (= :not-checked (get-in bundle [:boundary :effects])))
    (is (= #{'main} (set (map :name (:exports root)))))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in root [:plan :source :sha256])))
    (is (= 10 (count routes) (count (set (map :reference-syntax-id routes)))))
    (doseq [route routes]
      (is (= (:symbol route) (:symbol (get refs (:reference-syntax-id route)))))
      (is (= (:artifact-id leaf) (:callee-module-id route) (get-in route [:origin :ordinary-module-artifact-id]))))
    (let [result (atom nil) _ (println :map-call-phase :runtime)
          output (with-out-str (reset! result (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :map-call-test} bundle 'main [])))]
      (is (= ["kept" :shadow [:y] :a [:left :right] :c :w '(leaf/hidden :data) :v] @result))
      (is (= "z\nb\ny\na\nxl\nxr\nc\nw\nv\n" output)))
    (let [changed-route (assoc (first routes) :reference-syntax-id (:reference-syntax-id (second routes)))
          changed-route (assoc changed-route :route-id (bootstrap/reader-canonical-hash
                                                       {:domain :gravity/stage2-module-route-v1 :route (dissoc changed-route :route-id)}))
          changed (assoc-in bundle [:routes 0] changed-route)
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id bundle) (:modules bundle) (:routes changed))
          forged (assoc changed :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))
          failure (atom nil) output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function
                                                                              {:engine :map-call-test} forged 'main []))))]
      (is (= (:bundle-id forged) (bootstrap/reader-canonical-hash identity)))
      (is (= :module-bundle-reconstruction-mismatch (get-in @failure [:facts :reason])))
      (is (= "" output)))
    (let [bad (fixture "rejected" "module-map-private-import" ".gravity")
          failure (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact bad c5-inputs))]
      (is (= "C5-PRIVATE" (:id failure)))
      (is (= bad (get-in failure [:source-span :source]))))
    (doseq [[label changed expected]
            [[:arity (-> text
                         (str/replace "(leaf/identity (do (println \"z\") prefix))" "(leaf/identity prefix :extra)")
                         (str/replace "(leaf/identity (do (println \"v\") :v))" "(leaf/identity :v :extra :extra)")) "L2-FUNCTION-ARITY"]
             [:set (str/replace text "(leaf/identity (do (println \"z\") prefix))" "#{ {:blocked (leaf/identity :blocked)} }") :unsupported-nested-module-call]
             [:fn (str/replace text "(leaf/identity (do (println \"z\") prefix))" "(fn [] {:blocked (leaf/identity :blocked)})") :unsupported-nested-module-call]]]
      (let [_ (println :map-call-phase label) artifact (bootstrap/compiler-c5-resolution-source-artifact path changed c5-inputs)
            failure (atom nil) output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                                                                emitter path changed (assoc inputs :caller-resolution-artifact artifact)))))]
        (is (not= text changed))
        (is (= :accepted (:status artifact)))
        (is (= expected (if (= label :arity) (:id @failure) (get-in @failure [:facts :reason]))))
        (is (= "" output))
        (when (= label :arity)
          (is (= [1 2] [(:expected-arity @failure) (:actual-arity @failure)]))
          (is (= path (get-in @failure [:source-span :source]))))))))
