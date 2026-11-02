(ns gravity.self-hosting.stage2-module-transitive-call-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; L2 lexical evaluation/L3 acyclic imported origins/BOOT2 finite seed subset.
;; Normalized kernels below do not replace the genuine C5 composition.
(def ^:private runtime {:engine :stage2-module-transitive-call-test})
(defn- fixture [family name extension]
  (loop [root (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/stage2_module_transitive_call_test.clj"))))]
    (if (.isFile (io/file root "deps.edn"))
      (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" family "/" name extension)))
      (recur (.getParentFile root)))))
(defn- diagnostic [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn- reason [operation] (get-in (diagnostic operation) [:facts :reason]))
(defn- id [n] (format "sha256:%064x" n))
(defn- artifact [n children]
  {:artifact-id (id n) :gravity-resolution-boundary {:module-inputs {:module-artifacts children}}})

(deftest stage2-module-transitive-fixture-parity
  (doseq [[family name] [["modules" "module-chain-root"] ["modules" "module-chain-middle"]
                         ["modules" "module-chain-leaf"] ["rejected" "module-chain-private-import"]]]
    (let [g (fixture family name ".gravity") q (fixture family name ".qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (bootstrap/read-forms g (slurp g)) (bootstrap/read-forms q (slurp q)))))))

(deftest stage2-module-transitive-closure-development-kernel
  (let [c (artifact 3 []) b (artifact 2 [c]) a (artifact 1 [b c])
        collect bootstrap/p15-s23-stage2-module-closure
        seen (atom 0)]
    (is (= [a b c] (collect a)))
    (is (= [(artifact 1 [c b]) b c] (collect (artifact 1 [c b]))))
    (let [repeated (artifact 1 (vec (repeat 257 c)))
          expanded (collect repeated)]
      (is (= [repeated c] expanded)))
    (let [unique (artifact 1 (mapv #(artifact % (if (= % 2) [(artifact 258 [])] [])) (range 2 258)))
          expanded (collect unique)]
      (is (= 258 (count expanded)))
      (is (= (set (map id (range 1 259))) (set (map :artifact-id expanded)))))
    (is (= :conflicting-module-artifact
           (reason #(collect (artifact 1 [b (assoc c :extra :different-provenance)])))))
    (doseq [[value expected]
            [[(assoc c :artifact-id :not-a-digest) :module-artifact-id-required]
             [(assoc c :untrusted (Object.)) :bounded-module-artifact-carrier]]]
      (with-redefs [bootstrap/p15-s23-stage2-module-source (fn [_] (swap! seen inc))]
        (is (= expected (reason #(bootstrap/p15-s23-stage2-module-bundle
                                  {} "unused" "unused" {:caller-resolution-artifact value :dependency-plans []}))))))
    (is (zero? @seen))))

(deftest stage2-module-instruction-traversal-development-kernel
  (let [instructions (vec (repeat 262145 {:op :literal :value nil}))
        plan {:functions {'main {:instructions instructions}}}
        result (bootstrap/p15-s23-stage2-module-instructions plan)]
    (is (= 262145 (count result)))
    (is (= instructions result))))

(deftest stage2-module-transitive-routing-development-kernel
  ;; Deliberately normalized source/compile substitutions isolate bundle logic;
  ;; only the source test below exercises original C5 freshness and compilation.
  (let [c (artifact 3 []) b (artifact 2 [c]) a (artifact 1 [b])
        emitter {:fixture :emitter}
        sources (into {} (map (fn [[x namespace imports]]
                               [(:artifact-id x) {:artifact x :module-id (:artifact-id x)
                                                  :source-path (str namespace) :source-text (str namespace)
                                                  :module {:namespace namespace} :request {:imports imports}
                                                  :functions {}}])
                             [[a 'example.root [:b]] [b 'example.middle [:c]] [c 'example.leaf []]]))
        route (fn [caller callee]
                {:route-id (:artifact-id caller) :caller-module-id (:artifact-id caller)
                 :caller-function 'relay :callsite-path [0 1 1] :callee-module-id (:artifact-id callee)})
        ra (route a b) rb (route b c)
        raw (fn raw
              ([emitter path text index] (raw emitter path text index nil))
              ([_ path _ index _map-order]
               (let [index (not-empty index)]
                 {:plan-id (bootstrap/reader-canonical-hash [path index])
                  :module {:module (symbol path)} :functions {} :route-index index})))
        bp (raw emitter "example.middle" "example.middle" {['relay [0 1 1]] rb})
        cp (raw emitter "example.leaf" "example.leaf" nil)
        build (fn [plans] (bootstrap/p15-s23-stage2-module-bundle
                           emitter "example.root" "example.root" {:caller-resolution-artifact a :dependency-plans plans}))
        source-calls (atom [])]
    (with-redefs [bootstrap/p15-s23-stage2-module-source
                  (fn [x] (swap! source-calls conj (:artifact-id x)) (get sources (:artifact-id x)))
                  bootstrap/p15-s23-stage2-module-shadowing-check! (fn [& _])
                  bootstrap/c-backend-stage2-plan-emitter-source-rule! (fn [& _] {:emitter emitter})
                  bootstrap/p15-s23-stage2-module-routes
                  (fn [source _] (get {(:artifact-id a) [ra] (:artifact-id b) [rb]} (:module-id source) []))
                  bootstrap/p15-s23-stage2-module-exports (fn [_] [])
                  bootstrap/p15-s23-stage2-compiler-artifact-plan-raw raw]
      (let [bundle (build [bp cp])
            indexes (into {} (map (juxt :module-id #(get-in % [:plan :route-index])) (:modules bundle)))]
        (is (= [ra rb] (:routes bundle)))
        (is (= {['relay [0 1 1]] ra} (get indexes (:artifact-id a))))
        (is (= {['relay [0 1 1]] rb} (get indexes (:artifact-id b))))
        (is (nil? (get indexes (:artifact-id c))))
        (is (= bundle (build [cp bp])))
        (is (= [(id 1) (id 2) (id 3)] (mapv :module-id (:modules bundle)))))
      ;; Exact old two-module identity/route order with the original raw leaf.
      (let [bundle (bootstrap/p15-s23-stage2-module-bundle
                    emitter "example.middle" "example.middle" {:caller-resolution-artifact b :dependency-plans [cp]})
            entries [(bootstrap/p15-s23-stage2-module-entry (get sources (id 2)) bp)
                     (bootstrap/p15-s23-stage2-module-entry (get sources (id 3)) cp)]
            identity (bootstrap/p15-s23-stage2-module-identity emitter (id 2) entries [rb])]
        (is (= {:kind :gravity/stage2-compiler-artifact-module-bundle :schema-version 1
                :root-module-id (id 2) :emitter emitter :modules entries :routes [rb]
                :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity)
                :boundary bootstrap/p15-s23-stage2-module-boundary} bundle)))
      (doseq [plans [[bp] [bp cp cp] [bp bp]]]
        (reset! source-calls [])
        (is (= :source-emitter-or-dependency-set (reason #(build plans))))
        (is (= [] @source-calls)))
      (doseq [field [:namespace :source-path]]
        (let [conflict (if (= field :namespace)
                         (assoc-in sources [(id 3) :module :namespace] 'example.middle)
                         (assoc-in sources [(id 3) :source-path] "example.middle"))]
          (with-redefs [bootstrap/p15-s23-stage2-module-source #(get conflict (:artifact-id %))]
            (is (= :source-emitter-or-dependency-set (reason #(build [bp cp])))))))
      (is (= :independent-raw-plan-mismatch (reason #(build [(assoc bp :route-index {}) cp]))))
      (with-redefs [bootstrap/p15-s23-stage2-module-routes (fn [& _] (vec (repeat 65537 ra)))]
        (is (= :module-route-limit (reason #(build [bp cp]))))))))

(deftest stage2-module-transitive-authenticated-composition
  ;; One genuine G chain; Q bytes/parser parity above is not typed/execution credit.
  (let [paths (mapv #(fixture "modules" % ".gravity")
                    ["module-chain-leaf" "module-chain-middle" "module-chain-root"])
        [cp bp ap] paths [ct bt at] (mapv slurp paths)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! ap :jvm))
        _ (println :transitive-phase :leaf-c5)
        c (bootstrap/compiler-c5-resolution-file-artifact cp)
        ci {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [c]}
        _ (println :transitive-phase :middle-c5)
        b (bootstrap/compiler-c5-resolution-file-artifact bp ci)
        bi {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [b]}
        _ (println :transitive-phase :root-c5)
        a (bootstrap/compiler-c5-resolution-file-artifact ap bi)
        c-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter cp ct)
        _ (println :transitive-phase :middle-bundle)
        middle (bootstrap/p15-s23-stage2-compiler-artifact-plan
                 emitter bp bt {:caller-resolution-artifact b :dependency-plans [c-plan]})
        b-plan (:plan (first (filter #(= (:artifact-id b) (:module-id %)) (:modules middle))))
        inputs {:caller-resolution-artifact a :dependency-plans [b-plan c-plan]}
        _ (println :transitive-phase :root-bundle)
        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter ap at inputs)
        routes (:routes bundle)
        by-id (into {} (map (juxt :module-id identity) (:modules bundle)))]
    (is (= [:accepted :accepted :accepted] (mapv :status [c b a])))
    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
    (is (= :not-checked (get-in bundle [:boundary :effects])))
    (is (false? (get-in bundle [:boundary :self-hosted?])))
    (is (= #{(:artifact-id a) (:artifact-id b) (:artifact-id c)} (set (keys by-id))))
    (is (= [(:artifact-id a) (:artifact-id b)] (mapv :caller-module-id routes)))
    (is (= [(:artifact-id b) (:artifact-id c)] (mapv :callee-module-id routes)))
    (is (= [['relay [0 1 1]] ['relay [0 1 1]]] (mapv (juxt :caller-function :callsite-path) routes)))
    (is (= 2 (count routes) (count (set (map :route-id routes)))))
    (is (= c-plan (:plan (get by-id (:artifact-id c)))))
    (is (= b-plan (:plan (get by-id (:artifact-id b)))))
    (doseq [[artifact path text] [[a ap at] [b bp bt] [c cp ct]]]
      (let [entry (get by-id (:artifact-id artifact))]
        (is (= artifact (:resolution-artifact entry)))
        (is (= path (get-in artifact [:provenance :source-path])))
        (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in entry [:plan :source :sha256])))))
    (doseq [route routes]
      (let [callee (get by-id (:callee-module-id route))
            export (first (filter #(= (:callee-binding-id route) (:binding-id %)) (:exports callee)))]
        (is (= (:callee-module-id route) (get-in route [:origin :ordinary-module-artifact-id])))
        (is (= :public (:visibility export)))
        (is (= 1 (:arity route) (get-in callee [:plan :functions (:callee-function route) :arity])))))
    (is (= #{'main} (set (map :name (:exports (get by-id (:artifact-id a)))))))
    (is (= #{'relay} (set (map :name (:exports (get by-id (:artifact-id b)))))))
    (is (= :source-emitter-or-dependency-set
           (reason #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter ap at (assoc inputs :dependency-plans [b-plan])))))
    (let [result (atom nil)
          _ (println :transitive-phase :runtime)
          output (with-out-str (reset! result (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'main [])))]
      (is (= "kept" @result))
      (is (= "argument\nleaf\nmiddle\nroot\n" output)))
    (let [changed (assoc-in bundle [:routes 1 :origin :ordinary-module-artifact-id] (:artifact-id a))
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id changed) (:modules changed) (:routes changed))
          forged (assoc changed :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))
          failure (atom nil)
          output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'main []))))]
      (is (= (:bundle-id forged) (bootstrap/reader-canonical-hash identity)))
      (is (= :module-bundle-reconstruction-mismatch (get-in @failure [:facts :reason])))
      (is (= "" output)))
    (let [path (fixture "rejected" "module-chain-private-import" ".gravity")
          _ (println :transitive-phase :private-c5)
          failure (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact path ci))]
      (is (= "C5-PRIVATE" (:id failure)))
      (is (map? (:source-span failure))))))
