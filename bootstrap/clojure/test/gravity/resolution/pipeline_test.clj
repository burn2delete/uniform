(ns gravity.resolution.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.resolution :as resolution]))

(defn- seed-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Required resolution seed seam is absent" {:name name}))))
(defn- operations [] ((seed-var 'resolution-stage-operations)))
(defn- input [field] ((:input (operations)) field))
(defn- caught [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error error)))
(def ^:private source
  (.getCanonicalPath (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/current-cohort-unmetered.gravity")))
(def ^:private invocation
  {:reader-source-path b/sh03-reader-source-relative-path
   :authenticated-envelope-options {:source :development-current}})
(def ^:private real-artifact
  (delay
    (b/compiler-c5-resolution-source-artifact
     source (slurp source :encoding "UTF-8")
     {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
     (.getCanonicalPath (io/file b/sh03-reader-source-relative-path)) invocation)))
(defn- real-binding [] (b/sh06-resolution-current-binding! source))

(deftest contract-and-operation-inventories-agree
  (let [publics (ns-publics 'gravity.resolution)
        contract (:gravity/namespace-contract (meta #'resolution/execute!))]
    (is (= 55 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (= (set (keys (operations))) (set (:operation-inputs contract))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (publics name)))))
      (is (= contract (:gravity/namespace-contract (meta (publics name))))))
    (is (true? (.isDynamic ^clojure.lang.Var (seed-var '*sh06-resolution-operation-binding*))))
    (is (not (contains? (ns-aliases 'gravity.resolution) 'b)))
    (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :forbids])))))

(deftest declaration-records-preserve-qualified-identity-and-origin
  ;; These supplied syntax records test normalization, not C4 authentication.
  (let [ops (operations)
        span {:byte-start 7 :byte-end 20 :line-start 2 :column-start 3 :ignored true}
        module {:module 'example.records :exports ['public-value 'other/qualified]
                :profile :meta :target :jvm :effects #{:io/write}
                :capabilities #{:io/stdout} :safety :safe}
        parent {:artifact-id "synthetic-expanded-parent"
                :expanded-syntax-stream
                [{:form '(def public-value 1) :span span :syntax/id "syntax-public"}
                 {:form '(defn private-value [x] x) :span span :syntax/id "syntax-private"}
                 {:form '(def other/qualified (fn [x] x)) :span span :syntax/id "syntax-qualified"}
                 {:form '(defconst answer 42) :span span :syntax/id "syntax-constant"}
                 {:form '(defn 17 [] nil) :span span :syntax/id "syntax-invalid-name"}
                 {:form '(println public-value) :span span :syntax/id "syntax-expression"}]}
        records (resolution/definition-records module parent ops)
        [_public _private qualified constant] records]
    (is (= ['public-value 'private-value 'qualified 'answer] (mapv :name records)))
    (is (= [:public :private :public :private] (mapv :visibility records)))
    (is (= [:var :function :function :compile-time-constant] (mapv :kind records)))
    (is (= 'other (:namespace qualified)))
    (is (= {:name 'other :version "workspace"} (:package qualified)))
    (is (= [:gravity.type/value :gravity.type/function :gravity.type/function :gravity.type/value]
           (mapv :type-ref records)))
    (is (= [0 1 2 3] (mapv #(get-in % [:semantic-span :ordinal]) records)))
    (is (= "syntax-constant" (:definition-syntax-id constant)))
    (doseq [record records]
      (is (= span (:source-span record)))
      (is (= {:ordinal (get-in record [:semantic-span :ordinal])
              :byte-start 7 :byte-end 20 :line-start 2 :column-start 3} (:semantic-span record)))
      (is (= "synthetic-expanded-parent" (:definition-artifact-id record)))
      (is (= [[:meta] [:jvm] [:io/write] [:io/stdout] :safe :namespace]
             ((juxt :profile-set :target-set :effects :capabilities :safety :binding-class) record))))
    (doseq [[form expected] [['(defmacro m [x] x) :macro]
                            ['(defschema S {}) :schema]
                            ['(defprotocol P) :protocol]
                            ['(let [x 1] x) nil]
                            ['plain-symbol nil]]]
      (is (= expected (resolution/definition-kind form))))
    (is (= {:ordinal 9} (resolution/semantic-span nil 9)))
    (is (= {:name 'example.records :version "workspace"}
           (resolution/package-record 'example.records)))))

(deftest upstream-identities-preserve-precedence-and-deterministic-fallbacks
  (let [ops (operations)
        parent {:artifact-id "parent" :expanded-syntax-stream-id "stream"
                :macro-expansion-trace-id "trace"}
        with-c2 (assoc-in parent [:gravity-macro-boundary :authenticated-sh04-artifact
                                  :c2-reader-artifact :source-unit-record :source-id] "c2-source")
        with-revision (assoc-in with-c2 [:gravity-macro-boundary :authenticated-sh04-artifact
                                         :gravity-syntax-boundary :reader-source-revision :revision-id] "revision")
        with-envelope (assoc-in parent [:gravity-macro-boundary :authenticated-envelope
                                         :semantic-envelope-id] "envelope")]
    (is (= "revision" (resolution/source-revision-id with-revision ops)))
    (is (= "c2-source" (resolution/source-revision-id with-c2 ops)))
    (is (= "envelope" (resolution/envelope-id with-envelope ops)))
    (doseq [operation [resolution/source-revision-id resolution/envelope-id]]
      (let [id (operation parent ops)]
        (is (re-matches #"sha256:[0-9a-f]{64}" id))
        (is (= id (operation (into (array-map) (reverse (seq parent))) ops)))
        (is (not= id (operation (assoc parent :macro-expansion-trace-id "changed") ops)))))
    (is (not= (resolution/source-revision-id parent ops) (resolution/envelope-id parent ops)))
    (is (not= (resolution/envelope-id parent ops)
              (resolution/envelope-id (assoc parent :artifact-id "changed") ops)))))

(deftest core-catalog-and-explicit-overrides-retain-their-boundaries
  (let [ops (operations) records (resolution/core-records ops)
        by-name (into {} (map (juxt :name identity)) records)
        candidates [{:namespace 'one :name 'x :identity :first}
                    {:namespace 'two :name 'x :identity :other}
                    {:namespace 'one :name 'x :identity :later}]
        overrides {:candidate-bindings candidates}]
    (is (= (sort (keys by-name)) (map :name records)))
    (is (= (range (count records)) (map #(get-in % [:semantic-span :ordinal]) records)))
    (is (= (count records) (count by-name)))
    (is (= :special-form (:kind (by-name 'let))))
    (is (= :type (:kind (by-name 'I64))))
    (is (= :var (:kind (by-name 'count))))
    (is (= [[:io/write] [:io/stdout]] ((juxt :effects :capabilities) (by-name 'println))))
    (is (= [[] []] ((juxt :effects :capabilities) (by-name 'count))))
    (is (= [:all] (:target-set (by-name 'I64))))
    (is (= (vec (sort (input :known-source-profiles))) (:profile-set (by-name 'let))))
    (is (every? #(= :safe (:safety %)) records))
    (is (= 1 (count (set (map :definition-artifact-id records)))))
    (is (= (count records) (count (set (map :definition-syntax-id records)))))
    (is (= overrides (resolution/request-overrides {:metadata {:compiler {:sh06-request overrides}}})))
    (doseq [value [nil [] "not-a-record"]]
      (is (= {} (resolution/request-overrides {:metadata {:compiler {:sh06-request value}}}))))
    (is (= :first (:identity (resolution/candidate-for overrides 'one 'x))))
    (is (= :other (:identity (resolution/candidate-for overrides 'two 'x))))
    (is (nil? (resolution/candidate-for overrides 'one 'missing)))))

(deftest dependency-records-preserve-explicit-boundary-facts
  ;; Supplied declarations exercise record normalization, not module admission.
  (let [ops (operations)
        module {:module 'example.consumer :profile :meta :target :jvm}
        required {:module 'example.library :alias 'library :refer ['value]
                  :profile :core :effects #{:io/read} :capabilities #{:io/input}}
        imported {:module 'foreign.library :alias 'foreign :kind :import
                  :boundary :typed-ffi :visibility :private}
        overrides {:candidate-bindings [{:namespace 'example.library :name 'value
                                         :target :wasm :visibility :private
                                         :capabilities #{:library/read}}
                                        {:namespace 'example.library :name 'later
                                         :target :llvm :visibility :public}]}
        records (resolution/import-records source (assoc module :requires [required] :imports [imported]) overrides ops)
        [namespace-record foreign-record] records]
    (is (= ['example.library 'foreign.library] (mapv :namespace records)))
    (is (= ['library 'foreign] (mapv :alias records)))
    (is (= [:namespace :foreign] (mapv :kind records)))
    (is (= :core (:profile namespace-record)))
    (is (= [:wasm] (:targets namespace-record)))
    (is (= [:library/read] (:capabilities namespace-record)))
    (is (= [:io/read] (:effects namespace-record)))
    (is (= :private (:visibility namespace-record)))
    (is (= :pure-core-api (:boundary namespace-record)))
    (is (= ['value] (:refer namespace-record)))
    (is (= [:safe :boundary-checked] (mapv :safety records)))
    (is (= :typed-ffi (:boundary foreign-record)))
    (is (= :meta (:profile foreign-record)))
    (is (= [:jvm] (:targets foreign-record)))
    (is (= [0 1] (mapv #(get-in % [:semantic-span :dependency-ordinal]) records)))
    (is (every? :foreign-record-complete? records))
    (is (= (:source-span namespace-record) (:source-span foreign-record)))
    (doseq [bad [(dissoc imported :alias) (dissoc imported :boundary)
                 (assoc imported :module "not-a-symbol")]]
      (is (false? (boolean (:foreign-record-complete?
                           (resolution/dependency-record source module bad {} 0 ops))))))
    (is (= records (resolution/import-records source (assoc module :requires [required] :imports [imported])
                                             (into (array-map) (reverse (seq overrides))) ops)))
    (is (not= (:dependency-artifact-id namespace-record)
              (:dependency-artifact-id
               (resolution/dependency-record source module (assoc required :boundary :schema) overrides 0 ops))))))

(deftest import-and-explicit-bindings-retain-precedence-and-provenance
  (let [ops (operations)
        module {:module 'example.consumer :source-path source :profile :meta :target :jvm
                :requires [{:refer ['referred]}] :imports []}
        dependency {:namespace 'example.library :kind :namespace :refer ['referred]
                    :profile :core :targets [:wasm] :visibility :private
                    :effects [:io/read] :capabilities [:library/read] :safety :safe
                    :source-span {:source source :form-index 0} :dependency-artifact-id "dependency-id"}
        candidates [{:namespace 'example.library :name 'referred :visibility :public
                     :target-set [:llvm] :capabilities [:candidate/read]}
                    {:namespace 'example.library :name 'explicit :visibility :private}
                    {:namespace 'example.library :name 'explicit :visibility :public}
                    {:namespace 'other.library :name 'unrelated}]
        bindings (resolution/import-binding-records module [dependency] {:candidate-bindings candidates} ops)
        [referred explicit] bindings]
    (is (= ['referred 'explicit] (mapv :name bindings)))
    (is (= [:public :private] (mapv :visibility bindings)))
    (is (= [:llvm] (:target-set referred)))
    (is (= [:candidate/read] (:capabilities referred)))
    (is (= [:library/read] (:capabilities explicit)))
    (is (= [:core] (:profile-set referred)))
    (is (= [:io/read] (:effects referred)))
    (is (= :gravity.type/imported-var (:type-ref referred)))
    (is (= {:name 'example.library :version "workspace"} (:package referred)))
    (is (= [0 1] (mapv #(get-in % [:semantic-span :binding-ordinal]) bindings)))
    (is (every? #(= :import (:binding-class %)) bindings))
    (is (every? #(= "dependency-id" (:definition-artifact-id %)) bindings))
    (is (every? #(= (:source-span dependency) (:source-span %)) bindings))
    (is (= 2 (count (set (map :definition-syntax-id bindings)))))
    (let [foreign (first (resolution/import-binding-records nil [(assoc dependency :kind :foreign)] {} ops))]
      (is (= [:foreign :gravity.interop/foreign-value] ((juxt :kind :type-ref) foreign))))
    (let [parent {:artifact-id "expanded-parent"}
          overrides {:candidate-bindings [{:name 'referred :namespace 'example.library}
                                          {:name 'local-candidate :kind :function}
                                          {:name 'qualified :namespace 'other.library :profile :core
                                           :target-set [:wasm] :effects [:io/read]
                                           :capabilities [:library/read] :visibility :private}]}
          explicit-records (resolution/explicit-candidate-records module parent overrides ops)
          [local qualified] explicit-records]
      (is (= ['local-candidate 'qualified] (mapv :name explicit-records)))
      (is (= ['example.consumer 'other.library] (mapv :namespace explicit-records)))
      (is (= [:function :var] (mapv :kind explicit-records)))
      (is (= [[:meta] [:core]] (mapv :profile-set explicit-records)))
      (is (= [[:jvm] [:wasm]] (mapv :target-set explicit-records)))
      (is (= :private (:visibility qualified)))
      (is (= [:io/read] (:effects qualified)))
      (is (= [:library/read] (:capabilities qualified)))
      (is (= [0 1] (mapv #(get-in % [:semantic-span :explicit-candidate]) explicit-records)))
      (is (= :safe (:safety local)))
      (is (not= (:definition-artifact-id local)
                (:definition-artifact-id
                 (first (resolution/explicit-candidate-records module {:artifact-id "changed-parent"} overrides ops))))))))

(deftest pattern-binding-paths-preserve-order-and-recovery-boundaries
  (let [ops (operations)
        pattern '[left [right _]]
        expected '[{:name left :path [7 0]} {:name right :path [7 1 0]}]]
    (is (= expected (resolution/match-pattern-binding-paths pattern [7] ops)))
    (is (= expected (resolution/unique-match-binding-paths pattern [7] ops)))
    (is (= expected (resolution/let-loop-binding-paths pattern [7])))
    (is (= '[{:name one :path [1]} {:name two :path [5 0]}]
           (resolution/match-pattern-binding-paths
            (array-map :first 'one :when '(predicate one) :last '[two]) [] ops)))
    (is (= [] (resolution/match-pattern-binding-paths '(quote literal) [] ops)))
    (is (= [] (resolution/match-pattern-binding-paths '_ [] ops)))
    (is (nil? (resolution/unique-match-binding-paths '[one [one]] [] ops)))
    ;; Let/loop recovery enumerates each vector leaf once. This is not L7
    ;; admission of duplicate bindings or variadic destructuring.
    (is (= '[{:name one :path [0]} {:name two :path [1 0]} {:name tail :path [4]}]
           (resolution/let-loop-binding-paths '[one [two one] _ & tail] [])))
    (is (= '[{:name value :path [3]}] (resolution/let-loop-binding-paths 'value [3])))
    (doseq [valid ['[one [two _]] '[1 :tag nil true false "text"]
                   '{:kind :tag :payload [value]} '(quote literal)]]
      (is (true? (resolution/fixed-vector-pattern? valid ops))))
    (doseq [unsupported ['[one & tail] '{:when true :value value}
                         '(Constructor value) '{not-a-keyword value}]]
      (is (false? (resolution/fixed-vector-pattern? unsupported ops))))))

(deftest lexical-collection-preserves-sequential-visibility-and-context
  ;; These supplied expanded records test collection, not C4 authentication.
  (let [ops (operations)
        syntax {:syntax/id "synthetic-expanded-owner" :span {:source "synthetic" :line-start 4}
                :form '(fn self [items :- I64]
                         (let [[left [right _]] items copy left]
                           (self (+ copy right))))}
        parent {:expanded-syntax-stream [syntax]}
        analysis (resolution/analysis-inputs {} parent {:shadow-policy {:forbidden ['left]}} ops)
        scopes (:lexical-scopes analysis) refs (:references analysis)
        scopes-by-id (into {} (map (juxt :scope-id identity)) scopes)
        visible (fn [reference]
                  (set (mapcat #(map :name (:bindings (scopes-by-id %))) (:scope-chain reference))))
        left-ref (first (filter #(= 'left (:symbol %)) refs))
        copy-ref (first (filter #(= 'copy (:symbol %)) refs))
        self-ref (first (filter #(= 'self (:symbol %)) refs))
        bindings (mapcat :bindings scopes)]
    (is (= analysis (resolution/analysis-inputs nil parent {:shadow-policy {:forbidden ['left]}} ops)))
    (is (= #{'self 'items 'left 'right 'copy} (set (map :name bindings))))
    (is (not (some #(= '_ (:name %)) bindings)))
    (is (not (some #(= 'I64 (:name %)) bindings)))
    (is (= #{'self 'items 'left 'right} (visible left-ref)))
    (is (= #{'self 'items 'left 'right 'copy} (visible copy-ref)))
    (is (contains? (visible self-ref) 'self))
    (is (some #(and (= 'I64 (:symbol %)) (= :type (:position %))) refs))
    (is (false? (:allow-shadow? (first (filter #(= 'left (:name %)) bindings)))))
    (is (every? #(= (:span syntax) (:source-span %)) refs))
    (is (= (range 1 (inc (count refs))) (map #(get-in % [:semantic-span :ordinal]) refs)))
    (is (= (count refs) (count (set (map :syntax-id refs)))))
    (is (= (count scopes) (count (set (map :scope-id scopes)))))
    (doseq [name '[semantic-late-sh06-resolution-analysis-inputs-parameter-names
                  semantic-late-sh06-resolution-analysis-inputs-make-traversal]]
      (is (nil? (ns-resolve 'gravity.bootstrap name)))
      (is (nil? (ns-resolve 'gravity.resolution name))))))

(deftest dependency-edge-normalization-retains-origins-and-order
  (let [ops (operations) span {:source "dependency" :line-start 2}
        imports [{:namespace 'library :kind :namespace :boundary :pure-core-api :source-span span}]
        result (resolution/dependency-edges
                source {:module 'consumer} imports
                {:module-graph {:edges [['library 'next]
                                        {:from 'next :to 'last :kind :generated :boundary :schema}]}} ops)]
    (is (= [['consumer 'library] ['library 'next] ['next 'last]]
           (mapv (juxt :from :to) result)))
    (is (= [:namespace :namespace :generated] (mapv :kind result)))
    (is (= [:pure-core-api nil :schema] (mapv :boundary result)))
    (is (= [{:dependency-edge 0} {:explicit-edge 0} {:explicit-edge 1}] (mapv :semantic-span result)))
    (is (= span (:source-span (first result))))
    (is (= [] (resolution/dependency-edges source {:module 'consumer} [] {} ops)))))

(deftest product-projections-retain-analysis-and-compatibility-records
  ;; Supplied analysis exercises projection only, without C5 authentication.
  (let [ops (operations)
        analysis {:binding-table [{:binding-id "binding" :source-span {:line-start 3}}]
                  :resolution-table [{:binding-id "binding" :resolution-order :lexical}]
                  :alias-table [{:alias 'lib}]
                  :dependency-records [{:namespace 'library}]
                  :exports ['value] :lexical-scope-graph [{:scope-id "scope"}]
                  :dependency-graph {:nodes ['consumer 'library] :edges []}
                  :cross-profile-edge-report [{:boundary :pure-core-api}]
                  :incremental-invalidation-inputs {:imports ['library]}}
        product (resolution/product-projections analysis ops)]
    (is (= (assoc analysis :artifact :gravity/namespace-analysis) (:namespace-analysis product)))
    (is (= (:binding-table analysis) (get-in product [:binding-table :bindings])))
    (is (= (:resolution-table product) (get-in product [:binding-table :resolution-table])))
    (is (= ((seed-var 'sh06-resolution-order-compatibility) :lexical)
           (get-in product [:resolution-table 0 :resolution-kind])))
    (is (= (:alias-table analysis) (get-in product [:alias-table :aliases])))
    (is (= (:dependency-records analysis) (get-in product [:import-export-table :imports])))
    (is (= (:exports analysis) (get-in product [:import-export-table :exports])))
    (is (= (:lexical-scope-graph analysis) (get-in product [:lexical-scope-graph :scopes])))
    (is (= (:cross-profile-edge-report analysis) (get-in product [:cross-profile-edge-report :records])))
    (is (= (:incremental-invalidation-inputs analysis) (get-in product [:incremental-invalidation-keys :inputs])))
    (is (not= (get-in product [:incremental-invalidation-keys :invalidation-id])
              (get-in (resolution/product-projections (assoc analysis :incremental-invalidation-inputs {}) ops)
                      [:incremental-invalidation-keys :invalidation-id])))))

(deftest identity-chunks-preserve-representation-and-semantic-invalidation
  (let [ops (operations) values (vec (range 17))
        chunks (resolution/identity-chunks values ops)
        changed (resolution/identity-chunks (assoc values 16 :changed) ops)]
    (is (= [] (resolution/identity-chunks [] ops)))
    (is (= [0 1] (mapv :chunk-index chunks)))
    (is (= [16 1] (mapv :entry-count chunks)))
    (is (= chunks (resolution/identity-chunks (seq values) ops)))
    (is (= (first chunks) (first changed)))
    (is (not= (second chunks) (second changed)))))

(deftest artifact-identities-and-summaries-retain-the-existing-semantic-boundary
  ;; These pure identity helpers do not authenticate supplied chunks or artifacts.
  (let [ops (operations)
        artifact {:kind :resolution :slice :SH-06 :task :normalization
                  :gravity-resolution-boundary
                  {:adapter-contract :test :resolved-analysis {:artifact-id "analysis"}
                   :authenticated-resolution-request {:module {:source-revision-id "source"}}}
                  :sh05-macro-artifact {:artifact-id "macro" :expanded-syntax-stream-id "stream"
                                        :macro-expansion-trace-id "trace"}
                  :binding-table {:bindings [{:binding-id "binding" :source-span {:line-start 1}}]}
                  :resolution-table [{:binding-id "binding" :source-span {:line-start 2}}]
                  :dependency-graph {:nodes ['consumer 'library]
                                     :edges [{:from 'consumer :to 'library :source-span {:line-start 3}}]}
                  :incremental-invalidation-keys {:invalidation-id "invalidation"}}
        chunks (resolution/product-identity-chunks artifact ops)
        preimage (resolution/artifact-identity-input artifact ops)
        id (resolution/artifact-id artifact ops)
        summary (resolution/envelope-summary artifact ops)
        relocated (-> artifact
                      (assoc-in [:binding-table :bindings 0 :source-span :line-start] 30)
                      (assoc-in [:resolution-table 0 :source-span :line-start] 40)
                      (assoc-in [:dependency-graph :edges 0 :source-span :line-start] 50))]
    (is (= preimage (resolution/artifact-identity-input-with-chunks artifact chunks ops)))
    (is (= id ((:reader-canonical-hash ops) preimage)))
    (is (= summary (resolution/envelope-summary-with-chunks artifact chunks)))
    (is (= [1 1 1] ((juxt :binding-count :reference-count :dependency-edge-count) summary)))
    (is (= "source" (:source-revision-id summary)))
    (is (= id (resolution/artifact-id relocated ops)))
    (is (= (:source-span (first (:resolution-table artifact))) {:line-start 2}))
    (doseq [changed [(assoc-in artifact [:binding-table :bindings 0 :binding-id] "other")
                     (assoc-in artifact [:resolution-table 0 :binding-id] "other")
                     (assoc-in artifact [:dependency-graph :edges 0 :to] 'other)
                     (assoc-in artifact [:sh05-macro-artifact :macro-expansion-trace-id] "other")
                     (assoc-in artifact [:incremental-invalidation-keys :invalidation-id] "other")]]
      (is (not= id (resolution/artifact-id changed ops))))))

(deftest envelope-descriptor-retains-digest-lineage-and-actual-path-inputs
  ;; Descriptor projection is DATA construction, not envelope authentication.
  (let [base (operations) digests (atom []) properties (atom []) fields (atom [])
        ops (assoc base
                   :p15-s23-c6c10-canonical-digest
                   (fn [path value]
                     (swap! digests conj [path value])
                     ((:reader-canonical-hash base) value))
                   :input (fn [field] (swap! fields conj field) ((:input base) field))
                   :system-property (fn [name]
                                      (swap! properties conj name)
                                      (str "observed-directory-" (count @properties))))
        binding {:source-content-hash "source-hash" :source-byte-count 17
                 :plan-semantic-hash "plan-hash" :functions-semantic-hash "functions-hash"
                 :public-function-hashes {'sh06-build-resolution-template "builder-hash"}
                 :public-function-shapes [['sh06-build-resolution-template 1]]}
        summary {:artifact-id "semantic-product" :source-revision-id "source-revision"}
        descriptor (resolution/envelope-descriptor source binding summary ops)
        evidence ((:reader-canonical-hash base) (second (first @digests)))
        sealed ((:reader-canonical-hash base) (second (nth @digests 2)))]
    (is (= [[source {:domain :gravity/sh06-resolution-envelope-evidence-v1
                     :summary summary :plan-semantic-hash "plan-hash"}]
            [source {:domain :gravity/sh06-resolution-result-identity-v1
                     :semantic-input {:summary summary}}]
            [source {:resolution-result "semantic-product"}]] @digests))
    (is (= ["user.dir" "user.dir"] @properties))
    (is (= {:sh06-resolution-envelope-stage 1 :sh06-resolution-sealed-artifact-kind 1
            :sh06-resolution-source-relative-path 1} (frequencies @fields)))
    (is (= {:source-path source :workspace-root "observed-directory-1"
            :invocation-root "observed-directory-2"} (:actual-path-provenance descriptor)))
    (is (= :gravity/private-authenticated-envelope-descriptor (:artifact descriptor)))
    (is (= 2 (:schema-version descriptor)))
    (is (= "builder-hash" (get-in descriptor [:source-revision :builder-semantic-hash])))
    (is (= "source-hash" (get-in descriptor [:source-revision :source-content-hash])))
    (is (= (:public-function-shapes binding) (get-in descriptor [:source-revision :function-shapes])))
    (is (= summary (get-in descriptor [:semantic-projections 0 :value])))
    (is (= (count summary) (get-in descriptor [:semantic-projections 0 :entry-count])))
    (is (= [evidence] (get-in descriptor [:fact-transitions 0 :evidence-ids])))
    (is (= {evidence {:status :checked}} (get-in descriptor [:proof-composite :proof-certificate-table])))
    (is (= "semantic-product" (get-in descriptor [:lineage 0 :semantic-id])))
    (is (= sealed (get-in descriptor [:lineage 0 :artifact-id])))
    (is (= evidence (get-in descriptor [:lineage 0 :verification-id])))
    (is (= [evidence] (get-in descriptor [:reference-closure :fact-reference-ids])))
    (is (= [evidence] (get-in descriptor [:reference-closure :proof-reference-ids])))
    (is (= [] (get-in descriptor [:reference-closure :runtime-check-reference-ids])))
    (is (= [:identity-subject-equality :digest-graph-reachability]
           (get-in descriptor [:preservation :residual-checks])))
    (let [failure (ex-info "digest refused" {:id "UNIT-DIGEST"})
          calls-before @properties]
      (is (identical? failure
                      (caught #(resolution/envelope-descriptor
                                source binding summary
                                (assoc ops :p15-s23-c6c10-canonical-digest
                                       (fn [& _] (throw failure)))))))
      (is (= calls-before @properties)))))

(deftest boundary-key-selection-adds-only-the-existing-module-carrier
  (let [ops (operations) expected (input :sh06-resolution-boundary-keys)
        ordinary {:authenticated-resolution-request {:artifact :gravity/sh06-ordinary-module-resolution-request}}]
    (is (= expected (resolution/boundary-keys-for {} ops)))
    (is (= expected (resolution/boundary-keys-for nil ops)))
    (is (= expected (resolution/boundary-keys-for {:authenticated-resolution-request {:artifact :other}} ops)))
    (is (= (conj expected :module-inputs) (resolution/boundary-keys-for ordinary ops)))
    (is (= #{:supplied-key :module-inputs}
           (resolution/boundary-keys-for ordinary (assoc ops :input (fn [_] #{:supplied-key})))))))

(deftest base-request-normalization-preserves-parent-and-source-fields
  ;; Supplied expanded DATA exercises normalization, not C4 owner admission.
  (let [ops (operations) span {:line-start 3 :column-start 1}
        parent {:artifact-id "synthetic-parent" :expanded-syntax-stream-id "expanded"
                :macro-expansion-trace-id "trace"
                :expanded-syntax-stream
                [{:syntax/id "ns" :span span
                  :form '(ns example.request (:profile :meta) (:target :jvm)
                             (:exports [exported]) (:effects #{}) (:capabilities #{}) (:safety :safe))}
                 {:syntax/id "fn" :span span :form '(defn exported [value] value)}]}
        request (resolution/base-request source parent {} ops)]
    (is (= :gravity/sh06-authenticated-c4-resolution-request (:artifact request)))
    (is (= 1 (:schema-version request)))
    (is (= 'example.request (get-in request [:module :namespace])))
    (is (= "synthetic-parent" (get-in request [:module :c4-artifact-id])))
    (is (= (get-in request [:module :source-revision-id])
           (get-in request [:macro-expansion-binding :source-revision-id])))
    (is (= ["synthetic-parent" "expanded"]
           ((juxt :c4-artifact-id :macro-result-id) (:macro-expansion-binding request))))
    (is (= {:actual-source-path source} (:provenance request)))
    (is (= ['exported] (mapv :name (:definitions request))))
    (is (= :public (:visibility (first (:definitions request)))))
    (is (= (resolution/core-records ops) (:core-bindings request)))
    (is (some #(= 'value (:symbol %)) (:references request)))
    (is (= [] (:imports request)))
    (is (not (contains? request :module-premises)))))

(deftest module-request-projection-retains-verified-views-and-header-provenance
  (let [span {:line-start 1 :column-start 1}
        parent {:expanded-syntax-stream [{:syntax/id "header" :span span}]}
        base {:imports [{:namespace 'library :source-span :old :semantic-span {:ordinal 2}}]
              :definitions [] :module {:namespace 'consumer}}
        views [{:artifact-id "verified-by-owner" :analysis {}}]
        inputs {:module-artifacts [:supplied-to-owner]}
        expected (resolution/module-request parent base views)
        calls (atom []) original resolution/module-request
        wrapper (seed-var 'resolution-module-request-from-base)
        owner (seed-var 'sh06-resolution-module-input-views!)]
    (is (= :gravity/sh06-ordinary-module-resolution-request (:artifact expected)))
    (is (= 2 (:schema-version expected)))
    (is (identical? views (:module-premises expected)))
    (is (= span (get-in expected [:imports 0 :source-span])))
    (is (= {:ordinal 2 :namespace-syntax-id "header"} (get-in expected [:imports 0 :semantic-span])))
    (is (= {:semantic-span {:namespace-syntax-id "header"} :source-span span} (:namespace-source expected)))
    (with-redefs-fn
      {owner (fn [path value] (swap! calls conj [:owner path value]) views)
       #'resolution/module-request (fn [& args] (swap! calls conj (into [:stage] args)) (apply original args))}
      #(is (= expected (wrapper source parent inputs base))))
    (is (= [[:owner source inputs] [:stage parent base views]] @calls))
    (let [failure (ex-info "module owner refused" {:id "UNIT-MODULE"})]
      (reset! calls [])
      (with-redefs-fn
        {owner (fn [& _] (throw failure))
         #'resolution/module-request (fn [& _] (swap! calls conj :unexpected-stage))}
        #(is (identical? failure (caught (fn [] (wrapper source parent inputs base))))))
      (is (= [] @calls)))))

(deftest diagnostic-origin-recovery-requires-one-exact-generated-reference
  ;; These joined records do not constitute parent/request authentication.
  (let [ops (operations) span {:line-start 4 :column-start 1 :line-end 4 :column-end 12}
        origin [{:kind :source :source source} {:kind :generated :producer :unit}]
        parent {:artifact-id "parent" :expanded-syntax-stream-id "expanded"
                :expanded-syntax-stream [{:syntax/id "owner" :span span :origin origin}]}
        revision (resolution/source-revision-id parent ops)
        reference {:name 'missing :symbol 'missing :qualifier nil :position :expression :scope-chain []
                   :source-span span :semantic-span {:ordinal 1}
                   :syntax-id ((:reader-canonical-hash ops)
                               {:domain :gravity/sh06-reference-syntax-v1 :owner-syntax-id "owner"
                                :ordinal 1 :symbol 'missing :position :expression})}
        request {:references [reference]
                 :module {:namespace 'example.request :profile :meta :target :jvm
                          :source-revision-id revision :c4-artifact-id "parent"}
                 :macro-expansion-binding {:c4-artifact-id "parent" :source-revision-id revision
                                           :macro-result-id "expanded"}
                 :provenance {:actual-source-path source}}
        diagnostic {:diagnostic-family :c5-name-resolution :stage :name-resolution :id "C5-UNRESOLVED"
                    :candidate-bindings [] :syntax-id (:syntax-id reference) :symbol 'missing
                    :source-span (assoc span :source source) :namespace 'example.request
                    :active-profile :meta :target :jvm
                    :facts {:reason :no-legal-binding :semantic-span {:ordinal 1}
                            :c4-artifact-id "parent" :source-revision-id revision
                            :rule-specific (merge (select-keys reference [:name :qualifier :position :scope-chain])
                                                  {:reason :no-legal-binding :candidate-count 0})}}]
    (is (identical? origin (resolution/diagnostic-origin source parent request diagnostic ops)))
    (doseq [changed [(assoc diagnostic :origin-chain [])
                     (assoc diagnostic :diagnostic-family :other)
                     (assoc diagnostic :symbol 'other)
                     (assoc diagnostic :active-profile :core)
                     (assoc diagnostic :syntax-id "other")
                     (assoc-in diagnostic [:facts :rule-specific :scope-chain] [:other])
                     (assoc-in diagnostic [:facts :rule-specific :candidate-count] 1)
                     (assoc-in diagnostic [:facts :semantic-span :ordinal] 0)
                     (assoc-in diagnostic [:facts :semantic-span :ordinal] 2)]]
      (is (nil? (resolution/diagnostic-origin source parent request changed ops))))
    (is (nil? (resolution/diagnostic-origin source (update parent :expanded-syntax-stream into (:expanded-syntax-stream parent))
                                            request diagnostic ops)))
    (is (nil? (resolution/diagnostic-origin source (assoc-in parent [:expanded-syntax-stream 0 :origin] [{:kind :source}])
                                            request diagnostic ops)))))

(deftest private-candidate-products-preserve-identity-without-publication-proof
  (let [base-ops (operations) calls (atom [])
        ops (assoc base-ops :sh06-resolution-sh02-descriptor
                   (fn [& args] (swap! calls conj args) {:private-descriptor true}))
        parent {:artifact-id "parent" :expanded-syntax-stream-id "expanded" :macro-expansion-trace-id "trace"}
        request {:module {:source-revision-id "revision"}}
        binding {:plan {:private true} :source-text "private" :input-key {:private true} :reader-invocation {:private true}
                 :source-content-hash "source" :plan-semantic-hash "plan"}
        run {:raw-template-result {:template true} :raw-analysis {:raw true}
             :resolved-analysis {:artifact-id "analysis" :binding-table [] :resolution-table []
                                 :dependency-graph {:nodes [] :edges []} :incremental-invalidation-inputs {}}
             :digest-requests [] :resolved-digests [] :template-verification {:status :passed}
             :resolved-verification {:status :passed}}
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}]
    (doseq [contextual? [false true]]
      (reset! calls [])
      (let [{:keys [artifact descriptor]} (resolution/assemble-candidate source parent contextual? inputs request binding run ops)
            boundary (:gravity-resolution-boundary artifact)]
        (is (= :gravity/sh06-resolution-artifact (:kind artifact)))
        (is (= :accepted (:status artifact)))
        (is (nil? (:capability-based-proof artifact)))
        (is (= {:private-descriptor true} descriptor))
        (is (= 1 (count @calls)))
        (is (= [source binding (resolution/envelope-summary artifact ops)] (vec (first @calls))))
        (is (= (resolution/artifact-id artifact ops) (:artifact-id artifact)))
        (is (= (dissoc binding :plan :source-text :input-key :reader-invocation) (:plan-binding boundary)))
        (is (= (not contextual?) (contains? boundary :module-inputs)))
        (is (= {:source-path source :source-revision-id "revision"} (:provenance artifact)))
        (is (= (:resolved-analysis run) (:resolved-analysis boundary)))
        (is (= (input :c5-resolution-governing-document) (:governing-document artifact)))
        (is (= (input :c5-resolution-diagnostic-ids) (get-in artifact [:pass :rejects])))
        (is (false? (get-in artifact [:execution-boundary :self-hosted?])))
        (is (not (contains? boundary :authenticated-envelope)))))
    (is (= :none (get-in (:gravity/namespace-contract (meta #'resolution/assemble-candidate))
                        [:public-api 'assemble-candidate :admission])))))

(deftest actual-nested-bindings-emit-and-out-of-scope-binding-rejects
  (let [compile-fixture (fn [name]
                          (let [path (.getCanonicalPath
                                      (io/file (str "bootstrap/clojure/fixtures/self-hosting/sh-06/" name ".gravity")))]
                            (b/compiler-c5-resolution-source-artifact
                             path (slurp path :encoding "UTF-8")
                             {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                             (.getCanonicalPath (io/file b/sh03-reader-source-relative-path)) invocation)))
        accepted (compile-fixture "lexical-normalization")
        rejected (caught #(compile-fixture "lexical-leak"))]
    (is (= :accepted (:status accepted)))
    (is (= "sha256:0fdf519ad7657aa8b291b7aa46df085324bb1fb6bff3cb290b5ecbaf5c8152f2"
           (:artifact-id accepted)))
    (is (= 276 (count (get-in accepted [:binding-table :bindings]))))
    (is (= 21 (count (:resolution-table accepted))))
    (is (= :passed (:status (b/sh06-resolution-artifact-verification accepted :gravity.bootstrap/legacy invocation))))
    (is (= "C5-UNRESOLVED" (:id (ex-data rejected))))
    (is (= 'right (:symbol (ex-data rejected))))))

(deftest actual-source-emits-bindings-and-rejects-an-unresolved-reference
  (let [artifact @real-artifact binding (real-binding) ops (operations)
        request (get-in artifact [:gravity-resolution-boundary :authenticated-resolution-request])
        accepted (resolution/execute! source binding 'sh06-build-resolution-template [request] ops)
        last-reference (dec (count (:references request)))
        invalid (-> request
                    (assoc-in [:references last-reference :symbol] 'missing-value)
                    (assoc-in [:references last-reference :name] 'missing-value))
        rejected (resolution/execute! source binding 'sh06-build-resolution-template [invalid] ops)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted (:status accepted)))
    (is (= "sha256:49d37a2f0d79fb8902e3be9928cab495e734997f5e3e15bfee186ae5b9ed8bc1"
           (:artifact-id artifact)))
    (is (some #(and (= 'value (:symbol %)) (= :local (:resolution-kind %)))
              (:resolution-table artifact)))
    (is (some #(and (= 'identity-value (:name %)) (= :public (:visibility %)))
              (get-in artifact [:binding-table :bindings])))
    (is (= source (get-in artifact [:provenance :source-path])))
    (is (= :development-unadmitted (:revision-status binding)))
    (is (= (:source-content-hash binding) (get-in binding [:plan :source :sha256])))
    (is (= :rejected (:status rejected)))
    (is (some #(= "C5-UNRESOLVED" (:rule %)) (:diagnostics rejected)))
    ;; Both spellings also execute in the before/after artifact parity evidence.
    (is (= (slurp source :encoding "UTF-8")
           (slurp (.getCanonicalPath (io/file "bootstrap/clojure/fixtures/self-hosting/sh-06/current-cohort-unmetered.qst"))
                  :encoding "UTF-8")))))

(deftest source-identities-and-pinned-admission-remain-distinct
  (let [ops (operations) current (resolution/read-current-source! source ops)
        original-input (:input ops)
        pinned-ops (assoc ops :input (fn [field]
                                      (case field
                                        :sh06-resolution-expected-source-byte-count (:source-byte-count current)
                                        :sh06-resolution-expected-source-content-hash (:source-content-hash current)
                                        (original-input field))))]
    (is (= current (b/sh06-resolution-read-current-source! source)))
    (is (= current (resolution/read-pinned-source! source pinned-ops)))
    (is (= [:exact-pinned-resolution-source]
           (:missing-fields (ex-data (caught #(b/sh06-resolution-read-pinned-source! source))))))
    (doseq [[field bad] [[:sh06-resolution-expected-source-byte-count (inc (:source-byte-count current))]
                         [:sh06-resolution-expected-source-content-hash "sha256:forged"]]]
      (let [changed (assoc pinned-ops :input (fn [key] (if (= key field) bad ((:input pinned-ops) key))))]
        (is (= "C5-UNRESOLVED" (:id (ex-data (caught #(resolution/read-pinned-source! source changed))))))))))

(deftest plan-substitutions-and-cache-drift-fail-closed
  (let [binding (real-binding) plan (:plan binding) ops (operations)
        identities (resolution/plan-identities plan ops)
        build-ops (assoc ops
                         :c-backend-stage2-plan-emitter-source-rule! (fn [& _] {:emitter :unit-emitter})
                         :sh06-resolution-plan-identities (constantly identities))]
    (is (= identities (b/sh06-resolution-plan-identities plan)))
    (doseq [bad [(assoc plan :kind :forged)
                 (assoc-in plan [:module :module] 'forged.module)
                 (assoc-in plan [:module :profile] :kernel)
                 (assoc-in plan [:module :effects] #{:io/write})
                 (assoc-in plan [:source :sha256] "sha256:forged")]]
      (is (= [:pinned-resolution-plan-and-functions]
             (:missing-fields (ex-data
                               (caught #(resolution/build-plan-binding!
                                         source (assoc build-ops :p15-s23-stage2-compiler-artifact-plan
                                                       (fn [& _] bad)))))))))
    (let [key (:input-key (b/sh06-resolution-current-binding! source))
          cache (atom nil) builds (atom 0) original-input (:input ops)
          cached-ops (assoc ops
                            :input (fn [field]
                                     (case field
                                       :sh06-resolution-cached-binding cache
                                       :*sh06-resolution-operation-binding* nil
                                       (original-input field)))
                            :sh06-resolution-current-source-key (constantly key)
                            :sh06-resolution-build-binding! (fn [_] (swap! builds inc) binding))
          first-binding (resolution/current-plan-binding! source cached-ops)]
      (is (identical? first-binding (resolution/current-plan-binding! source cached-ops)))
      (is (= 1 @builds))
      (is (= [:resolution-toolchain-drift]
             (:missing-fields (ex-data
                               (caught #(resolution/current-plan-binding!
                                         source (assoc cached-ops :sh06-resolution-current-source-key
                                                       (constantly (assoc key :toolchain :changed)))))))))
      (is (= [:resolution-source-or-toolchain-drift]
             (:missing-fields (ex-data
                               (caught #(resolution/binding-current!
                                         source first-binding
                                         (assoc cached-ops :sh06-resolution-current-source-key
                                                (constantly (assoc key :source :changed))))))))))))

(deftest scoped-operation-binding-and-host-containment-are-preserved
  (let [v (seed-var '*sh06-resolution-operation-binding*)
        binding (b/sh06-resolution-current-binding! source)
        scoped (assoc binding :reader-invocation invocation)
        ops (operations)]
    (with-bindings {v scoped}
      (is (identical? scoped (input :*sh06-resolution-operation-binding*)))
      (is (identical? scoped (resolution/current-plan-binding! source ops))))
    (doseq [error [(StackOverflowError. "unit") (AssertionError. "unit")
                   (LinkageError. "unit") (ex-info "unit" {:id "UNIT"})
                   (Exception. "unit")]]
      (let [data (ex-data (caught #(resolution/execute!
                                   source binding 'unit []
                                   (assoc ops :p15-s23-stage2-runtime-execute-function (fn [& _] (throw error))))))]
        (is (= "C5-UNRESOLVED" (:id data)))
        (is (seq (:missing-fields data)))))
    (let [interrupted (InterruptedException. "unit")
          observed (try
                     (resolution/execute! source binding 'unit []
                                          (assoc ops :p15-s23-stage2-runtime-execute-function
                                                 (fn [& _] (throw interrupted))))
                     (catch InterruptedException error [error (.isInterrupted (Thread/currentThread))])
                     (finally (Thread/interrupted)))]
      (is (identical? interrupted (first observed)))
      (is (true? (second observed))))))

(defn- with-report-context [operation]
  (let [ctx (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                   :phase :construction :epoch 0 :receipts [] :executions {}
                   :reuses {} :check-catalogs {} :failed-report-executions 0})]
    (try
      (with-bindings {(seed-var '*sh07-proof-transaction-context*) ctx} (operation ctx))
      (finally (swap! ctx assoc :open? false :receipts [])))))
(def ^:private unit-artifact {:artifact-id "unit" :semantic-projection-id "projection" :alias-table-id "aliases"})
(def ^:private unit-report {:status :passed :checks {:receipt-unit? true} :failed-checks []})
(defn- with-unit-verifier [operation]
  ;; Test actual receipt qualification/retention with isolated rule/admission ports.
  ;; This unit supplies no semantic, source-authentication or migration evidence.
  (with-redefs-fn
    {(seed-var 'sh06-resolution-context-validation) (fn [& _] {:status :passed})
     (seed-var 'sh06-resolution-artifact-verification-contained) (fn [& _] unit-report)}
    operation))
(defn- unit-verify [] (b/sh06-resolution-artifact-verification unit-artifact {}))
(defn- assert-interposition-misses [interpose]
  (with-unit-verifier
    #(with-report-context
       (fn [ctx]
         (is (= unit-report (unit-verify)))
         (is (= unit-report (unit-verify)))
         (is (= 1 (get-in @ctx [:executions :sh06])))
         (is (= 1 (get-in @ctx [:reuses :sh06])))
         (interpose (fn [] (dotimes [_ 2] (is (= unit-report (unit-verify))))))
         (is (= 3 (get-in @ctx [:executions :sh06])))
         (is (= 1 (get-in @ctx [:reuses :sh06])))
         (is (= unit-report (unit-verify)))
         (is (= 2 (get-in @ctx [:reuses :sh06])))))))

(deftest every-stage-and-operation-var-invalidates-old-receipts
  (let [stage-vars (set (concat (vals (ns-publics 'gravity.resolution))
                                [#'resolution/verify-current* #'resolution/verify* #'resolution/verify-contained*]))
        vars (set (concat stage-vars
                         (filter var? (vals (operations)))
                         [(seed-var 'resolution-stage-operations)]))]
    (doseq [v vars]
      (let [original @v
            replacement (if (stage-vars v)
                          (fn [& _] (throw (ex-info "Qualification invoked stage code" {})))
                          ;; Shared SHA ports are legitimately used later to hash the
                          ;; receipt catalog. Preserve them while changing root identity.
                          (fn [& args] (apply original args)))]
        (assert-interposition-misses #(with-redefs-fn {v replacement} %)))))
  (let [v (seed-var 'resolution-stage-defaults) copied (reduce-kv assoc {} @v)]
    (is (= @v copied))
    (is (not (identical? @v copied)))
    (assert-interposition-misses #(with-redefs-fn {v copied} %))))

(deftest request-catalog-roots-invalidate-old-receipts
  (doseq [name '[c5-resolution-diagnostic-ids c5-resolution-governing-document
                sh06-resolution-adapter-contract]]
    (let [v (seed-var name) original @v
          replacement (if (set? original) (conj original :unit-extra) :unit-different)]
      (is (not= original replacement))
      (assert-interposition-misses #(with-redefs-fn {v replacement} %)))))

(deftest descriptor-configuration-roots-invalidate-old-receipts
  (doseq [name '[sh06-resolution-envelope-stage sh06-resolution-sealed-artifact-kind
                sh06-resolution-boundary-keys]]
    (let [v (seed-var name) original @v
          replacement (if (set? original) (conj original :unit-extra) :unit-different)]
      (is (not= original replacement))
      (assert-interposition-misses #(with-redefs-fn {v replacement} %)))))

(deftest equal-valued-configuration-cache-and-thread-bindings-invalidate-receipts
  (doseq [name ['sh06-resolution-source-relative-path 'sh06-resolution-expected-source-content-hash
                'sh06-resolution-public-function-shapes 'sh06-resolution-cached-binding
                'known-source-profiles 'sh06-resolution-all-core-symbols
                'sh06-resolution-special-symbols 'sh06-resolution-type-symbols]]
    (let [v (seed-var name) original @v
          replacement (cond (string? original) (String. ^String original)
                            (map? original) (reduce-kv assoc {} original)
                            (set? original) (into #{} (map identity) original)
                            :else (atom @original))]
      (is (not (identical? original replacement)))
      (assert-interposition-misses #(with-redefs-fn {v replacement} %))))
  (let [v #'resolution/execute! original @v dynamic? (.isDynamic v)]
    (try
      (.setDynamic v true)
      (assert-interposition-misses #(with-bindings {v original} (%)))
      (finally (.setDynamic v dynamic?)))))

(deftest legitimate-scoped-bindings-reuse-but-the-actual-plan-is-an-epoch-input
  (let [v (seed-var '*sh06-resolution-operation-binding*)
        original-root (.getRawRoot ^clojure.lang.Var v)
        binding {:plan {:body :original} :source-content-hash "same" :plan-semantic-hash "same"}]
    (with-unit-verifier
      #(with-report-context
         (fn [ctx]
           (with-bindings {v binding}
             (is (= unit-report (unit-verify)))
             (is (= unit-report (unit-verify))))
           (is (= 1 (get-in @ctx [:executions :sh06])))
           (is (= 1 (get-in @ctx [:reuses :sh06])))
           (with-bindings {v (assoc-in binding [:plan :body] :changed)}
             (is (= unit-report (unit-verify))))
           (is (= 2 (get-in @ctx [:executions :sh06])))
           (with-bindings {v binding} (is (= unit-report (unit-verify))))
           (is (= 2 (get-in @ctx [:reuses :sh06]))))))
    (is (identical? original-root (.getRawRoot ^clojure.lang.Var v)))))

(deftest actual-public-verification-retains-normal-scoped-reuse
  (let [artifact @real-artifact]
    (with-report-context
      (fn [ctx]
        (is (= :passed (:status (b/sh06-resolution-artifact-verification artifact :gravity.bootstrap/legacy invocation))))
        (is (= :passed (:status (b/sh06-resolution-artifact-verification artifact :gravity.bootstrap/legacy invocation))))
        (is (= 1 (get-in @ctx [:executions :sh06])))
        (is (= 1 (get-in @ctx [:reuses :sh06])))))))

(deftest construction-short-seam-tracks-the-new-stage-dependencies
  (let [environment (seed-var 'resolution-construction-environment)
        roots-fn (seed-var 'resolution-construction-roots)
        check! (seed-var 'resolution-construction-check!)
        reads (atom 0)
        expected (set (concat (vals (ns-publics 'gravity.resolution))
                              [#'resolution/verify-current* #'resolution/verify* #'resolution/verify-contained*
                               (seed-var 'resolution-stage-operations)
                               (seed-var 'resolution-stage-defaults)
                               (seed-var 'resolution-stage-input)
                               (seed-var 'sh06-resolution-dependency-record)
                               (seed-var 'source-span)
                               (seed-var 'resolution-stage-system-property)
                               (seed-var 'p15-s23-c6c10-canonical-digest)
                               (seed-var 'sh06-resolution-envelope-stage)
                               (seed-var 'sh06-resolution-sealed-artifact-kind)
                               (seed-var 'sh06-resolution-boundary-keys)
                               (seed-var 'c5-resolution-diagnostic-ids)
                               (seed-var 'c5-resolution-governing-document)
                               (seed-var 'sh06-resolution-adapter-contract)]))]
    ;; Isolate the existing readonly drift seam; no parent/owner admission runs.
    (with-redefs-fn
      {environment (fn [] (swap! reads inc) {})}
      #(let [roots (roots-fn)]
         (is (every? (set (map first roots)) expected))
         (doseq [v expected]
           (let [original @v
                 replacement (cond (map? original) (reduce-kv assoc {} original)
                                   (set? original) (into #{} (map identity) original)
                                   (vector? original) (mapv identity original)
                                   (string? original) (String. ^String original)
                                   (keyword? original) :unit-different
                                   :else (fn [& args] (apply original args)))
                 error (caught (fn []
                                 (with-redefs-fn {v replacement}
                                   (fn [] (check! source {} {} roots :gravity.bootstrap/unavailable)))))]
             (is (= [:construction-parent-dependency-drift] (:missing-fields (ex-data error)))
                 (str (:name (meta v)) " " (pr-str (ex-data error))))))
         (is (zero? @reads))))))

(deftest invocation-rejection-precedes-binding-and-stage-work
  (let [calls (atom 0) with-invocation (seed-var 'sh06-resolution-with-reader-invocation)]
    (with-redefs-fn
      {(seed-var 'sh03-reader-authenticated-envelope-options)
       (fn [& _] (throw (ex-info "invalid invocation" {:id "UNIT-INVOCATION"})))
       #'resolution/current-plan-binding! (fn [& _] (swap! calls inc))}
      #(is (= "UNIT-INVOCATION"
              (:id (ex-data (caught (fn [] (with-invocation source {:invalid true} identity))))))))
    (is (zero? @calls))))

(deftest interrupted-execution-unwinds-production-receipt-lifetime
  (let [interrupt (InterruptedException. "resolution execution") cleanup (atom nil)
        fail? (atom false) observer (seed-var '*sh07-proof-transaction-cleanup-observer*)
        prerequisites {(seed-var 'sh07-core-assert-pinned-revision!) (fn [& _] {})
                       (seed-var 'sh07-proof-transaction-source-snapshot) (fn [_] {})
                       (seed-var 'sh07-proof-transaction-core-snapshot) (fn [] {})
                       (seed-var 'sh06-resolution-context-validation) (fn [& _] {:status :passed})
                       (seed-var 'sh06-resolution-artifact-verification-contained)
                       (fn [& _]
                         (if @fail?
                           (resolution/execute! source {} 'unit [] (operations)) unit-report))}
        observed
        (try
          ;; Production lifetime, isolated Core snapshot preludes: no C6 admission.
          (with-redefs-fn prerequisites
            #(with-bindings {observer (fn [value] (reset! cleanup value))}
               (b/sh07-core-file-proof-transaction
                source
                (fn [_]
                  (unit-verify)
                  (reset! fail? true)
                  (with-redefs-fn {(seed-var 'p15-s23-stage2-runtime-execute-function)
                                  (fn [& _] (throw interrupt))} unit-verify)))))
          (catch InterruptedException error [error (.isInterrupted (Thread/currentThread))])
          (finally (Thread/interrupted)))]
    (is (identical? interrupt (first observed)))
    (is (true? (second observed)))
    (is (= {:open? false :cleanup-complete? true :retained-receipt-count 0}
           (select-keys @cleanup [:open? :cleanup-complete? :retained-receipt-count])))))

(deftest verifier-preflight-refuses-opaque-carriers-before-semantic-inspection
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) 7)
                 (equals [_] (swap! callbacks conj :equals) false)
                 (toString [] (swap! callbacks conj :print) "opaque"))
        ops (assoc (operations)
                   :sh06-resolution-artifact-verification-bounded*
                   (fn [& _] (swap! callbacks conj :semantic-verification)))]
    (doseq [value [opaque {:untrusted opaque} [opaque]]]
      (let [report (#'resolution/verify* value false :gravity.bootstrap/legacy ops)]
        (is (= :failed (:status report)))
        (is (false? (get-in report [:checks :trusted-artifact-carrier?])))
        (is (nil? (:gravity-verification report)))
        (is (nil? (:upstream-verification report)))))
    (is (empty? @callbacks))))

(deftest verifier-preflight-retains-shape-and-component-order
  ;; Isolated admission port records dispatch only, never owner acceptance.
  (let [calls (atom [])
        boundary (zipmap ((:sh06-resolution-boundary-keys-for (operations)) {}) (repeat nil))
        artifact (assoc (zipmap @(seed-var 'sh06-resolution-artifact-keys) (repeat nil))
                        :gravity-resolution-boundary boundary)
        ops (assoc (operations)
                   :sh06-resolution-component-validations
                   (fn [_] (swap! calls conj :components) {:unit {:status :passed}})
                   :sh06-resolution-artifact-verification-bounded*
                   (fn [& args] (swap! calls conj [:semantic args]) unit-report))]
    (is (= :failed (:status (#'resolution/verify* (dissoc artifact :kind) false :gravity.bootstrap/legacy ops))))
    (is (empty? @calls))
    (is (= unit-report (#'resolution/verify* artifact false :gravity.bootstrap/legacy ops)))
    (is (= [:components [:semantic (list artifact false)]] @calls))
    (reset! calls [])
    (is (= unit-report (#'resolution/verify* artifact true {:context :unit} ops)))
    (is (= [:components [:semantic (list artifact true {:context :unit})]] @calls))
    (reset! calls [])
    (let [report (#'resolution/verify* artifact false :gravity.bootstrap/legacy
                                    (assoc ops :sh06-resolution-component-validations
                                           (fn [_] (swap! calls conj :components) {:unit {:status :failed}})))]
      (is (= :failed (:status report)))
      (is (= [:trusted-semantic-components?] (:failed-checks report)))
      (is (= [:components] @calls)))))

(deftest verifier-contained-failures-preserve-the-original-host-boundary
  (doseq [error [(Exception. "unit") (AssertionError. "unit")
                 (LinkageError. "unit") (StackOverflowError. "unit")
                 (OutOfMemoryError. "unit")]]
    (let [ops (assoc (operations) :sh06-resolution-artifact-verification*
                     (fn [& _] (throw error)))
          report (#'resolution/verify-contained* {} false :gravity.bootstrap/legacy ops)]
      (is (= (resolution/verification-failure error) report))
      (is (= :failed (:status report)))
      (is (= [(:contained-host-error report) [:contained-host-resource-failure?]]
             [(.getName (class error)) (:failed-checks report)]))))
  (let [error (InterruptedException. "unit")
        observed (try
                   (#'resolution/verify-contained* {} false :gravity.bootstrap/legacy
                                                (assoc (operations) :sh06-resolution-artifact-verification*
                                                       (fn [& _] (throw error))))
                   (catch InterruptedException caught [caught (.isInterrupted (Thread/currentThread))])
                   (finally (Thread/interrupted)))]
    (is (identical? error (first observed)))
    (is (true? (second observed)))))

(deftest verifier-requires-fresh-replay-current-products-and-public-proof
  (let [artifact @real-artifact
        verify (fn [value construction? ops]
                 ((seed-var 'sh06-resolution-with-reader-invocation)
                  source invocation
                  #(#'resolution/verify-contained* value construction? :gravity.bootstrap/legacy ops)))
        ops (operations)]
    (is (= :passed (:status (verify artifact false ops))))
    (is (= :passed (:status ((seed-var 'sh06-resolution-with-reader-invocation)
                             source invocation #(resolution/verify artifact :gravity.bootstrap/legacy ops)))))
    (is (= '([artifact context operations]) (:arglists (meta #'resolution/verify))))
    (is (nil? ((ns-publics 'gravity.resolution) 'verify-current*)))
    (is (nil? ((ns-publics 'gravity.resolution) 'verify*)))
    (is (nil? ((ns-publics 'gravity.resolution) 'verify-contained*)))
    (let [without-proof (assoc artifact :capability-based-proof nil)]
      (is (= :passed (:status (verify without-proof true ops))))
      (let [report (verify without-proof false ops)]
        (is (= :failed (:status report)))
        (is (= [:embedded-capability-proof-current?] (:failed-checks report)))))
    (doseq [[path value expected]
            [[[:artifact-id] "sha256:changed" :artifact-identity-current?]
             [[:provenance :source-revision-id] "changed" :provenance-current?]
             [[:gravity-resolution-boundary :raw-template-result] {} :raw-template-result-current?]
             [[:gravity-resolution-boundary :authenticated-envelope] {} :fresh-sh02-envelope-verified?]
             [[:capability-based-proof :status] :failed :embedded-capability-proof-current?]]]
      (let [report (verify (assoc-in artifact path value) false ops)]
        (is (= :failed (:status report)))
        (is (some #{expected} (:failed-checks report)))))
    (doseq [[key replacement expected]
            [[:sh06-resolution-verify-sh05 (fn [& _] {:status :failed}) :fresh-upstream-sh05-verification?]
             [:sh06-resolution-run-request! (fn [& _] (throw (ex-info "unit replay refusal" {:id "C5-UNRESOLVED"})))
              :fresh-gravity-request-replay?]
             [:p15-s23-stage2-sh02-descriptor-envelope-verify! (fn [& _] (throw (ex-info "unit envelope refusal" {})))
              :fresh-sh02-envelope-verified?]]]
      (let [report (with-redefs-fn {(get ops key) replacement} #(verify artifact false ops))]
        (is (= :failed (:status report)))
        (is (some #{expected} (:failed-checks report)))))))

(deftest verifier-configuration-roots-invalidate-old-receipts
  (doseq [name '[sh06-resolution-artifact-keys sh06-resolution-pass-contract
                sh06-resolution-execution-boundary-contract]]
    (let [v (seed-var name) original @v
          replacement (cond (map? original) (reduce-kv assoc {} original)
                            (set? original) (into #{} (map identity) original))]
      (is (= original replacement))
      (is (not (identical? original replacement)))
      (assert-interposition-misses #(with-redefs-fn {v replacement} %)))))

(deftest declared-digest-materialization-preserves-indices-and-semantic-preimages
  (let [ops (operations)
        refs [{:digest-ref 0} {:digest-ref 1}]
        span {:source source :line-start 2}
        raw {:binding-table [{:binding-id (refs 0) :name 'a} {:binding-id (refs 1) :name 'b}]
             :resolution-table [{:binding-id (refs 1) :syntax-id "reference" :source-span span}]
             :identity-preimage {:binding-identities refs :resolutions []}
             :incremental-invalidation-inputs {:binding-ids refs}
             :artifact-id {:digest-ref 2}}
        requests [{:ordinal 0 :purpose :sh06-binding-id :preimage {:name 'a}}
                  {:ordinal 1 :purpose :sh06-binding-id :preimage {:name 'b}}
                  {:ordinal 2 :purpose :sh06-namespace-analysis-id :preimage :ignored}]
        digests (resolution/resolve-digest-requests! source raw requests ops)
        resolved (resolution/resolve-analysis! source raw digests ops)]
    (is (= 3 (count digests)))
    (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %) digests))
    (is (= (subvec digests 0 2) (mapv :binding-id (:binding-table resolved))))
    (is (= (subvec digests 0 2) (get-in resolved [:identity-preimage :binding-identities])))
    (is (= (subvec digests 0 2) (get-in resolved [:incremental-invalidation-inputs :binding-ids])))
    (is (= (digests 1) (get-in resolved [:resolution-table 0 :binding-id])))
    (is (= span (get-in resolved [:resolution-table 0 :source-span])))
    (is (= (digests 2) (:artifact-id resolved)))
    (is (not (contains? (get-in resolved [:identity-preimage :resolutions 0]) :source-span)))
    (is (= digests (resolution/resolve-digest-requests!
                   source (assoc-in raw [:resolution-table 0 :source-span] {:source "changed"}) requests ops)))
    (is (not= digests (resolution/resolve-digest-requests!
                      source raw (assoc-in requests [0 :preimage :name] 'changed) ops)))
    (is (= {:digest-ref 2} (:artifact-id (resolution/resolve-analysis! source raw (subvec digests 0 2) true ops))))
    (doseq [[changed expected] [[[] :resolution-digest-requests]
                                [(pop requests) :complete-resolution-digest-requests]
                                [(assoc-in requests [0 :ordinal] 1) :ordered-resolution-digest-requests]]]
      (is (= [expected] (:missing-fields (ex-data (caught #(resolution/resolve-digest-requests! source raw changed ops)))))))
    (doseq [bad [nil {:digest-ref -1} {:digest-ref 0.5} {:digest-ref 9} {:digest-ref 0 :extra true}]]
      (is (= [:unit-slot] (:missing-fields (ex-data (caught #(resolution/resolve-declared-id! source bad digests :unit-slot ops)))))))
    (is (= 0 (resolution/digest-reference {:digest-ref 0})))
    (doseq [bad [nil {} {:digest-ref -1} {:digest-ref 0.5} {:digest-ref 0 :extra true}]]
      (is (nil? (resolution/digest-reference bad))))))

(deftest execution-rejects-host-carriers-before-invoking-the-resolver
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (toString [] (swap! calls inc) "opaque")
                 (hashCode [] (swap! calls inc) 7)
                 (equals [_] (swap! calls inc) false))
        ops (assoc (operations) :sh06-resolution-execute!
                   (fn [& _] (swap! calls inc)))]
    (doseq [request [opaque {:opaque opaque}]]
      (let [error (caught #(resolution/run-request! source {} request ops))]
        (is (= "C5-UNRESOLVED" (:id (ex-data error))))
        (is (= [:untrusted-sh06-resolution-carrier] (:missing-fields (ex-data error))))))
    (is (zero? @calls))))

(deftest execution-phase-failures-stop-before-later-materialization
  ;; Unit control flow only: supplied reports cannot confer admission authority.
  (let [calls (atom []) template {:status :accepted :namespace-analysis-template {} :digest-requests []}
        ops (assoc (operations)
                   :sh06-resolution-require-carrier! (fn [_ carrier _] (swap! calls conj carrier) {:status :passed})
                   :sh06-resolution-execute!
                   (fn [_ _ function _]
                     (swap! calls conj function)
                     (case function
                       sh06-build-resolution-template template
                       sh06-verify-resolution-template {:status :failed}))
                   :sh06-resolution-resolve-digest-requests! (fn [& _] (swap! calls conj :unexpected-digests))
                   :sh06-resolution-resolve-analysis! (fn [& _] (swap! calls conj :unexpected-analysis)))
        error (caught #(resolution/run-request! source {} {} ops))]
    (is (= [:fresh-gravity-resolution-template-verification] (:missing-fields (ex-data error))))
    (is (= [:authenticated-sh06-resolution-request 'sh06-build-resolution-template
            :gravity-resolution-template 'sh06-verify-resolution-template] @calls))
    (reset! calls [])
    (let [error (caught #(resolution/run-request! source {} {}
                                                (assoc ops :sh06-resolution-execute! (fn [& _] {:status :unknown}))))]
      (is (= [:gravity-resolution-result-status] (:missing-fields (ex-data error)))))
    (is (= [:authenticated-sh06-resolution-request :gravity-resolution-template] @calls))))

(deftest stage-execution-emits-real-verified-analysis-and-real-unresolved-rejection
  ;; Execute the actual current Gravity leaf on explicit normalized requests.
  ;; Source authentication and publication remain with their owning boundaries.
  (let [id #(format "sha256:%064x" %)
        request {:artifact :gravity/sh06-authenticated-c4-resolution-request :schema-version 1
                 :module {:namespace 'stage.execution :package 'stage :profile :hosted :target :jvm
                          :safety :safe :effects [] :capabilities [] :exports []
                          :source-revision-id (id 1) :c4-artifact-id (id 2)}
                 :definitions [] :imports [] :import-bindings [] :lexical-scopes []
                 :references [] :core-bindings [] :dependency-edges []
                 :macro-expansion-binding {:artifact :gravity/sh05-macro-expansion-binding :schema-version 1
                                           :source-revision-id (id 1) :c4-artifact-id (id 2)
                                           :macro-result-id (id 3) :authenticated-envelope-id (id 4)}
                 :provenance {:actual-source-path source}}
        binding (real-binding)
        run (resolution/run-request! source binding request (operations))
        rejected (assoc request :references [{:syntax-id (id 9) :symbol 'missing :qualifier nil :name 'missing
                                              :position :expression :scope-chain [] :semantic-span {} :source-span nil}])
        error (caught #(resolution/run-request! source binding rejected (operations)))]
    (is (= :accepted (get-in run [:raw-template-result :status])))
    (is (= :passed (get-in run [:template-verification :status])))
    (is (= :passed (get-in run [:resolved-verification :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (get-in run [:resolved-analysis :artifact-id])))
    (is (= "C5-UNRESOLVED" (:id (ex-data error))))
    (is (= 'missing (:symbol (ex-data error))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:diagnostic-id (ex-data error))))
    (is (= source (get-in (ex-data error) [:source-span :source])))))
