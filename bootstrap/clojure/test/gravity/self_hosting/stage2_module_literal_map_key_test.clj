(ns gravity.self-hosting.stage2-module-literal-map-key-test
  (:require [clojure.java.io :as io] [clojure.string :as str] [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap] [gravity.self-hosting.stage2-module-map-call-test]))

;; L1 literal kinds, L2 source evaluation and L3 original references.
;; Temporary seed only; no checked Gravity Map, whole-W5 or authority claim.
(defn- path [relative]
  (loop [root (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/stage2_module_literal_map_key_test.clj"))))]
    (if (.isFile (io/file root "deps.edn")) (.getPath (io/file root relative)) (recur (.getParentFile root)))))
(defn- fixture [family name extension]
  (path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" family "/module-literal-map-key-" name extension)))
(defn- diagnostic [f] (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))
(def ^:private records-path
  [:artifact :sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :form-tree])
(defn- source [body keys]
  ;; Supplied records only: fill the real C2 String kind absent in old helper.
  (update-in ((ns-resolve 'gravity.self-hosting.stage2-module-map-call-test 'supplied-source) body keys)
             records-path #(mapv (fn [record] (if (string? (:value record)) (assoc record :kind :string) record)) %)))
(defn- definition [source]
  (merge (get-in source [:functions 'main]) {:source-path (:source-path source)
          :p15-s23-stage2-map-order (bootstrap/p15-s23-stage2-module-map-order source)}))

(deftest stage2-module-literal-map-key-fixture-parity
  (doseq [[family name] [["modules" "root"] ["modules" "leaf"] ["rejected" "private"]]]
    (let [g (fixture family name ".gravity") q (fixture family name ".qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (bootstrap/read-forms g (slurp g)) (bootstrap/read-forms q (slurp q))))))
  (let [original (path "bootstrap/gravity/src/gravity/self_hosting/w5_trust_provenance_verifier.gravity")
        subject (fixture "modules" "leaf" ".gravity")
        select (fn [file name] (filterv #(and (seq? %) (= name (second %))) (bootstrap/read-forms file (slurp file))))]
    (doseq [name '[w5-trust-diagnostic-ids w5-trust-provenance-diagnostic-catalog]]
      (is (= 1 (count (select original name)) (count (select subject name))))
      (is (= (select original name) (select subject name))))))

(deftest stage2-module-literal-map-key-reference-development-kernel
  (let [strings ["z" "b" "y" "a" "x" ":tag" "\uD83D\uDE00" "\uD83D\uDE01" "d"]
        families [strings (conj strings :tag)]]
    (doseq [keys families
            :let [pairs (mapv #(vector % (list 'leaf/identity (list 'do (list 'println "value") 'local))) keys)
                  ;; Default sorted-map is valid only for the String-only family.
                  maps (cond-> [(into {} pairs) (apply array-map (mapcat identity pairs))]
                         (every? string? keys) (conj (into (sorted-map) pairs)))]
            seed maps actual maps]
      (let [body (list 'let ['local :local] seed) input (source body keys)
            [caller syntax] ((ns-resolve 'gravity.self-hosting.stage2-module-map-call-test 'reference-input)
                             input (list 'let ['local :local] actual))
            sites (bootstrap/p15-s23-stage2-module-call-sites (definition input))
            index (bootstrap/p15-s23-stage2-module-map-reference-index caller syntax (definition input))
            refs (filterv #(= 'leaf/identity (:symbol %)) (get-in caller [:request :references]))
            by-key (zipmap (map first actual) refs) slots (zipmap (map first seed) (range))]
        (is (= (into {} (map (fn [key] [[0 2 (inc (* 2 (get slots key)))] (get by-key key)])) keys) index))
        (is (= (mapv #(vector 0 2 (inc (* 2 (get slots %)))) keys) (mapv :path sites)))
        (is (= (mapv by-key keys) (mapv #(get index (:path %)) sites)))
        (is (= (count keys) (count index) (count (set (map :syntax-id (vals index))))))))
    (let [value {":tag" :string :tag :keyword "\uD83D\uDE00" :smile "\uD83D\uDE01" :beam}
          input (source value [":tag" :tag "\uD83D\uDE00" "\uD83D\uDE01"])
          order (:p15-s23-stage2-map-order (definition input)) row (get order ['main [0]])]
      (is (= [":tag" :tag "\uD83D\uDE00" "\uD83D\uDE01"] (mapv #(get (:host-keys row) %) (:entry-order row))))
      (is (= 4 (count (:host-keys row)))))))

(deftest stage2-module-literal-map-key-rejections-development-kernel
  (let [input (source {"a" :first "b" :second :tag :keyword} ["a" "b" :tag])
        records (get-in input records-path)
        key-index (fn [value] (first (keep-indexed #(when (and (= value (:value %2))
                                                            (contains? #{:string :keyword} (:kind %2))) %1) records)))
        map-index (first (keep-indexed #(when (= :map (:kind %2)) %1) records))
        children (:children (get records map-index))]
    (doseq [[label changed]
            [[:string-kind (assoc-in input (conj records-path (key-index "a") :kind) :keyword)]
             [:keyword-kind (assoc-in input (conj records-path (key-index :tag) :kind) :string)]
             [:scalar-class (assoc-in input (conj records-path (key-index "a") :value) :a)]
             [:missing-child (assoc-in input (conj records-path map-index :children 0) :missing)]
             [:duplicate-child (assoc-in input (conj records-path map-index :children 2) (first children))]
             ;; Supplied decoded rows only, not an actual fresh C2 escape read.
             [:decoded-equivalent (-> input
                                      (assoc-in (conj records-path (key-index "b") :value) "a")
                                      (assoc-in (conj records-path (key-index "b") :raw) "\"\\u0061\""))]]]
      (is (= "BOOT2006" (:id (diagnostic #(definition changed)))) label))
    (is (= :map-source-distinct-literal-key-bijection
           (get-in (diagnostic #(definition (assoc-in input (conj records-path (key-index "b") :value) "a"))) [:facts :reason]))))
  (doseq [key [0 'local [:key] {:key :value} '(str "computed")]]
    (is (= :map-source-distinct-literal-key-bijection
           (get-in (diagnostic #(definition (source {key :value} []))) [:facts :reason]))))
  (doseq [body ['#{ {"key" (leaf/identity :blocked)} } '(fn [] {"key" (leaf/identity :blocked)})]]
    (is (= :unsupported-nested-module-call
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites (definition (source body ["key"])))) [:facts :reason]))))
  (let [quoted '(quote {0 (leaf/hidden :data) "key" :inert}) input (source quoted [])]
    (is (= {} (:p15-s23-stage2-map-order (definition input))))
    (is (= [] (bootstrap/p15-s23-stage2-module-call-sites (definition input)))))
  (doseq [[value keys expected] [[{"a" '(recur) "z" '(throw)} ["z" "a"] "L2-THROW-ILLEGAL"]
                                [{"a" '(throw) "z" '(recur)} ["z" "a"] "L2-RECUR-TARGET"]]]
    (let [body (list 'do value '(loop [x] x)) d (definition (source body keys))
          original (diagnostic #(bootstrap/p15-s23-stage2-seed-validate-tail-body! d [body] 0 true))]
      (is (= expected (:id original)))
      (is (= original (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites d)))))))

(deftest stage2-module-literal-map-key-seed-handoff-development-kernel
  ;; Genuine tiny C5 -> original fresh module-source -> original raw compiler.
  ;; This is the context handoff control, not public bundle/runtime evidence.
  (let [p (fixture "modules" "leaf" ".gravity")
        text (str "(ns example.literal-map-handoff (:profile :meta) (:target :jvm)"
                  " (:exports [main]) (:effects #{:memory/allocate}) (:capabilities #{}) (:safety :safe))\n"
                  "(defn main [] {:artifact :fixture :schema-version 1 :diagnostics [] :remediation {\"key\" :value}})\n")
        artifact (bootstrap/compiler-c5-resolution-source-artifact p text)
        selected (bootstrap/p15-s23-stage2-module-source artifact)
        syntax (first (filter #(and (seq? (:form %)) (= 'main (second (:form %)))) (:syntax selected)))
        actual (nth (nth (:form syntax) 2) 2)
        seed (first (get-in selected [:functions 'main :body]))
        row (get (:map-order selected) ['main [0]])
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! p :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan-raw emitter p text {} (:map-order selected))
        compiled (get-in plan [:functions 'main :instructions 0])]
    (is (= :accepted (:status artifact)))
    (is (= (:artifact-id artifact) (:module-id selected)))
    (is (= actual seed))
    (is (not= (mapv first actual) (mapv first seed)))
    (is (= (mapv first seed) (:host-keys row)))
    (is (= [:artifact :schema-version :diagnostics :remediation]
           (mapv #(get-in % [:key :value]) (:entries compiled))))
    (is (= :map-literal (:op compiled)))
    (is (= :map-source-order-mismatch
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-map-entries
                                {:p15-s23-stage2-map-order (:map-order selected)
                                 :p15-s23-sh07-enclosing-function 'main}
                                actual [0])) [:facts :reason])))))

(deftest stage2-module-literal-map-key-authenticated-composition
  ;; Five G C5 attempts, genuine no-import leaf bundle and original public gates.
  (let [leaf-path (fixture "modules" "leaf" ".gravity") root-path (fixture "modules" "root" ".gravity")
        text (slurp root-path) emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! root-path :jvm))
        _ (println :literal-map-phase :leaf-c5) leaf (bootstrap/compiler-c5-resolution-file-artifact leaf-path)
        c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [leaf]}
        _ (println :literal-map-phase :root-c5) caller (bootstrap/compiler-c5-resolution-file-artifact root-path c5-inputs)
        _ (println :literal-map-phase :leaf-bundle)
        leaf-bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path (slurp leaf-path)
                     {:caller-resolution-artifact leaf :dependency-plans []})
        independent (:plan (first (:modules leaf-bundle)))
        inputs {:caller-resolution-artifact caller :dependency-plans [independent]}
        _ (println :literal-map-phase :root-bundle)
        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter root-path text inputs)
        root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle))) routes (:routes bundle)
        refs (into {} (map (juxt :syntax-id identity))
                   (get-in caller [:gravity-resolution-boundary :authenticated-resolution-request :references]))]
    (is (= :accepted (:status leaf) (:status caller)))
    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
    (is (= :not-checked (get-in bundle [:boundary :effects])))
    (is (= [] (:routes leaf-bundle)))
    (is (= independent (:plan (first (remove #(= (:root-module-id bundle) (:module-id %)) (:modules bundle))))))
    (is (= #{'main} (set (map :name (:exports root)))))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in root [:plan :source :sha256])))
    (is (= 12 (count routes) (count (set (map :reference-syntax-id routes)))))
    (doseq [route routes]
      (is (= (:symbol route) (:symbol (get refs (:reference-syntax-id route)))))
      (is (= (:artifact-id leaf) (:callee-module-id route) (get-in route [:origin :ordinary-module-artifact-id]))))
    (let [result (atom nil) _ (println :literal-map-phase :runtime)
          output (with-out-str (reset! result (bootstrap/p15-s23-stage2-runtime-execute-function {:engine :literal-map-test} bundle 'main [])))]
      (is (= [:z [:y] :a [:left :right] :string :keyword :smile :beam :escaped :provide-exact-versioned-request
              {:authority :non-authority :clojure-seed-boundary? true :self-hosted? false :release? false :public-authority? false}
              '(leaf/hidden {0 :inert "data" :quoted})] @result))
      (is (= "z\nb\ny\na\nxl\nxr\nstring-tag\nkeyword-tag\nsmile\nbeam\nescaped-d\n" output)))
    (let [changed-route (assoc (first routes) :reference-syntax-id (:reference-syntax-id (second routes)))
          changed-route (assoc changed-route :route-id (bootstrap/reader-canonical-hash
                                                       {:domain :gravity/stage2-module-route-v1 :route (dissoc changed-route :route-id)}))
          changed (assoc-in bundle [:routes 0] changed-route)
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id bundle) (:modules bundle) (:routes changed))
          forged (assoc changed :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))
          failure (atom nil) output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function
                                                                              {:engine :literal-map-test} forged 'main []))))]
      (is (= (:bundle-id forged) (bootstrap/reader-canonical-hash identity)))
      (is (= :module-bundle-reconstruction-mismatch (get-in @failure [:facts :reason])))
      (is (= "" output)))
    (let [bad (fixture "rejected" "private" ".gravity")
          failure (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact bad c5-inputs))]
      (is (= "C5-PRIVATE" (:id failure)))
      (is (= bad (get-in failure [:source-span :source]))))
    (doseq [[label changed expected]
            [[:arity (-> text
                         (str/replace "(leaf/identity (do (println \"z\") :z))" "(leaf/identity :z :extra)")
                         (str/replace "(leaf/identity (do (println \"escaped-d\") :escaped))" "(leaf/identity :escaped :extra :extra)")) "L2-FUNCTION-ARITY"]
             [:numeric (str/replace-first text "{\"z\" " "{0 ") :map-source-distinct-literal-key-bijection]]]
      (let [_ (println :literal-map-phase label) artifact (bootstrap/compiler-c5-resolution-source-artifact root-path changed c5-inputs)
            failure (atom nil) output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                                                                emitter root-path changed (assoc inputs :caller-resolution-artifact artifact)))))]
        (is (not= text changed))
        (is (= :accepted (:status artifact)))
        (is (= expected (if (= label :arity) (:id @failure) (get-in @failure [:facts :reason]))))
        (is (= "" output))
        (when (= label :arity)
          (is (= [1 2] [(:expected-arity @failure) (:actual-arity @failure)]))
          (is (= root-path (get-in @failure [:source-span :primary :source]))))))))
