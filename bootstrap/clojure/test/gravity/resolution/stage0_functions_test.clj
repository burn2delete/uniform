(ns gravity.resolution.stage0-functions-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.digest :as digest]
            [gravity.resolution.stage0-functions :as stage]))

(def ^:private table-operations
  {:function-definition-form #'b/function-definition-form
   :validate-seed-function-names! #'b/validate-seed-function-names!
   :normalize-seed-function-header #'b/normalize-seed-function-header
   :validate-function-definition! #'b/validate-function-definition!})
(def ^:private header-operations
  (assoc table-operations :seed-declared-header? #'b/seed-declared-header?))
(defn- observed-error [f] (try (f) nil (catch Throwable error error)))
(defn- normalize-header [path definition]
  (@#'stage/normalize-seed-function-header #'b/fail! #'b/seed-declared-header? path definition definition))

(deftest child-contract-has-three-public-apis-and-four-private-bridges
  (let [publics (ns-publics 'gravity.resolution.stage0-functions)
        contract (:gravity/namespace-contract (meta #'stage/stage0-function-table))
        names '[function-definition-form validate-function-definition! stage0-function-table
                seed-declared-header? normalize-seed-function-header validate-seed-function-names!
                validate-seed-source-headers!]]
    (is (= '#{function-definition-form validate-function-definition! stage0-function-table} (set (keys publics))))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (= 4 (count (:internal-api contract))))
    (is (= 55 (count (ns-publics 'gravity.resolution))))
    (is (empty? (ns-aliases 'gravity.resolution.stage0-functions)))
    (is (= '[clojure.core] (get-in contract [:dependency-direction :requires])))
    (is (false? (:source-reading? contract)))
    (is (false? (:canonical-c5-admission? contract)))
    (is (false? (:canonical-core-or-type-authority? contract)))
    (doseq [name names :let [operation (ns-resolve 'gravity.resolution.stage0-functions name)
                            api (get (merge (:public-api contract) (:internal-api contract)) name)]]
      (is (fn? @operation))
      (is (= (:arglists (meta operation)) (:arglists api)))
      (is (= (not (contains? publics name)) (boolean (:private (meta operation)))))
      (is (= :none (:admission api)))
      (is (false? (:authenticates-inputs? api)))
      (is (false? (:proves-legality? api)))
      (is (false? (:grants-authority? api))))))

(deftest classification-retains-defn-def-fn-fallback-and-late-second-classifier
  (is (= {:name 'f :params '[x] :body '(x) :definition-form :defn}
         (b/function-definition-form "source.gravity" '(defn f [x] x))))
  (is (= {:name 'f :params '[x] :body '(x) :definition-form :def-fn}
         (b/function-definition-form "source.gravity" '(def f (fn [x] x)))))
  (is (nil? (b/function-definition-form "source.gravity" '(ignored f))))
  (let [events (atom []) second-owner (clojure.lang.Var/create (fn [_] (throw (Exception. "early root"))))
        first-owner (clojure.lang.Var/create
                     (fn [form]
                       (swap! events conj [:first form])
                       (alter-var-root second-owner (constantly (fn [value] (swap! events conj [:second value]) true))) false))
        form '(def f (fn [] :value))]
    (is (= :def-fn (:definition-form (stage/function-definition-form first-owner second-owner (Object.) form))))
    (is (= [[:first form] [:second form]] @events))))

(deftest normalization-preserves-declarations-and-uninterpreted-type-tokens
  (let [token (Object.) params '[x :- UnknownType y :- (Vector Int)]
        raw {:name 'f :params params :body '(:- Result (+ x y)) :origin token}
        result (normalize-header "typed.gravity" raw)]
    (is (= '[x y] (:params result)))
    (is (= '((+ x y)) (:body result)))
    (is (= {:parameters params :return '[:- Result]} (:source-declarations result)))
    (is (identical? params (get-in result [:source-declarations :parameters])))
    (is (identical? token (:origin result))))
  (let [raw {:name 'f :params '[x] :body '((typed/return (quote Int) (do x)))}
        result (normalize-header "wrapper.gravity" raw)]
    (is (= '(x) (:body result)))
    (is (= '[typed/return (quote Int)] (get-in result [:source-declarations :return]))))
  (let [raw {:name 'f :params '[x] :body '(x)}]
    (is (identical? raw (normalize-header "plain.gravity" raw))))
  (doseq [[params body reason]
          [['[1 :- Int] '(1) :parameter-name-required]
           ['[x :-] '(x) :atomic-parameter-type-required]
           ['[x :- (Vector Int Extra)] '(x) :parameter-type-shape-required]
           ['[] '((typed/return Int (do 1))) :malformed-return-declaration-wrapper]
           ['[] '(:- 1 2) :atomic-return-type-required]
           ['[] '(:- Int :- Int 1) :repeated-return-declaration]]]
    (let [error (observed-error #(normalize-header "bad.gravity" {:name 'f :params params :body body}))]
      (is (= "L2-FUNCTION-PARAMS" (:id (ex-data error))))
      (is (= reason (:reason (ex-data error))))
      (is (= "bad.gravity" (get-in (ex-data error) [:source-span :source]))))))

(deftest legacy-normalizer-reads-definition-fields-once-before-header-callback
  (let [events (atom []) values {:name 'f :params [] :body [1]}
        definition (reify clojure.lang.ILookup
                     (valAt [_ key] (swap! events conj key) (get values key))
                     (valAt [_ key fallback] (swap! events conj key) (get values key fallback)))]
    (with-redefs [b/seed-declared-header? (fn [actual]
                                           (is (identical? definition actual))
                                           (swap! events conj :header) false)]
      (is (identical? definition (@#'b/normalize-seed-function-header "native.gravity" definition))))
    (is (= [:name :params :body :header] @events))))

(deftest record-and-duplicate-validation-retain-facts-and-returning-failures
  (doseq [[definition reason]
          [[{:name 'f :params '[x x] :body [1]} :duplicate-parameter-names]
           [{:name 'f :params '[x & xs] :body [1]} :variadic-parameters-unsupported]]]
    (let [error (observed-error #(stage/validate-function-definition! #'b/fail! "params.qst" definition))]
      (is (= "L2-FUNCTION-PARAMS" (:id (ex-data error))))
      (is (= reason (:reason (ex-data error))))
      (is (identical? (:params definition) (:params (ex-data error))))))
  (let [path (Object.) marker (Object.) calls (atom [])
        failure (clojure.lang.Var/create (fn [& args] (swap! calls conj args) marker))]
    (is (identical? marker (stage/validate-function-definition! failure path {:name 1 :params [] :body []})))
    (is (= ["stage0 function name must be a symbol" "stage0 function body cannot be empty"] (mapv second @calls)))
    (is (every? #(identical? path (get-in (nth % 2) [:source-span :source])) @calls))
    (reset! calls [])
    (is (identical? marker (@#'stage/validate-seed-function-names! failure path [{:name 'f} {:name 'f}])))
    (is (= "L2-DUPLICATE-FUNCTION" (ffirst @calls)))
    (is (= 'f (:function (nth (first @calls) 2)))))
  (is (nil? (stage/validate-function-definition! nil "ok.gravity" {:name 'f :params [] :body [nil]}))))

(deftest table-assembly-selects-validator-after-normalization
  (let [events (atom []) path (Object.) raw {:name 'f :params [] :body [1]}
        validate (clojure.lang.Var/create (fn [& _] (throw (Exception. "early validator"))))
        normalize (clojure.lang.Var/create
                   (fn [source definition]
                     (swap! events conj [:normalize source definition])
                     (alter-var-root validate (constantly (fn [p d] (swap! events conj [:validate p d]))))
                     definition))
        classify (clojure.lang.Var/create (fn [p form] (swap! events conj [:classify p form]) raw))
        names (clojure.lang.Var/create (fn [p definitions]
                                        (swap! events conj [:names p]) (doall definitions)))
        ops {:function-definition-form classify :validate-seed-function-names! names
             :normalize-seed-function-header normalize :validate-function-definition! validate}
        result (stage/stage0-function-table ops {:source-path path :forms [:form]})]
    (is (= {'f (assoc raw :arity 0 :body-form-count 1)} result))
    (is (= [[:names path] [:classify path :form] [:normalize path raw] [:validate path raw]] @events))))

(deftest source-header-validation-selects-predicate-and-validator-before-arguments
  (let [events (atom []) raw {:name 'f :params [] :body [1]}
        predicate (clojure.lang.Var/create (fn [_] (swap! events conj :predicate-original) true))
        validate (clojure.lang.Var/create (fn [_ definition] (swap! events conj [:validator-original definition])))
        classify (clojure.lang.Var/create
                  (fn [_ _]
                    (swap! events conj :classify)
                    (alter-var-root predicate (constantly (fn [_] (swap! events conj :predicate-late) true))) raw))
        normalize (clojure.lang.Var/create
                   (fn [_ definition]
                     (swap! events conj :normalize)
                     (alter-var-root validate (constantly (fn [& _] (throw (Exception. "late validator"))))) definition))
        names (clojure.lang.Var/create (fn [& _] (swap! events conj :names)))
        ops {:function-definition-form classify :seed-declared-header? predicate
             :validate-seed-function-names! names :normalize-seed-function-header normalize
             :validate-function-definition! validate}]
    (is (nil? (@#'stage/validate-seed-source-headers! ops "header.gravity" [:form])))
    (is (= [:classify :predicate-original :names :predicate-late :normalize [:validator-original raw]] @events))))

(deftest owners-propagate-exact-exceptions-without-remapping
  (doseq [error [(ex-info "info" {:id :owner}) (Exception. "host") (Error. "fatal")]
          :let [failure (clojure.lang.Var/create (fn [& _] (throw error)))]]
    (doseq [run [#(stage/function-definition-form failure nil nil '(defn f [] 1))
                 #(stage/validate-function-definition! failure nil {:name 1})
                 #(@#'stage/normalize-seed-function-header failure #'b/seed-declared-header? nil
                    {:name 'f :params '[x :-] :body [1]} {:name 'f :params '[x :-] :body [1]})
                 #(@#'stage/validate-seed-function-names! failure nil [{:name 'f} {:name 'f}])
                 #(stage/stage0-function-table (assoc table-operations :validate-seed-function-names! failure) {:forms []})
                 #(@#'stage/validate-seed-source-headers! (assoc header-operations :seed-declared-header? failure) nil ['(defn f [] 1)])]]
      (is (identical? error (observed-error run))))))

(deftest legacy-adapters-preserve-public-private-signatures-and-projections
  (let [path (Object.) token (Object.) params '[x] body '(x)
        raw {:name 'f :params params :body body :origin token}
        cases [[#'b/function-definition-form #'stage/function-definition-form false
                [path '(defn f [x] x)] [#'b/defn-form? #'b/def-function-form? path '(defn f [x] x)]]
               [#'b/seed-declared-header? #'stage/seed-declared-header? true [raw] [{:params params :body body}]]
               [#'b/normalize-seed-function-header #'stage/normalize-seed-function-header true [path raw]
                [#'b/fail! #'b/seed-declared-header? path {:name 'f :params params :body body} raw]]
               [#'b/validate-function-definition! #'stage/validate-function-definition! false [path raw]
                [#'b/fail! path {:name 'f :params params :body body}]]
               [#'b/validate-seed-function-names! #'stage/validate-seed-function-names! true [path [raw]] [#'b/fail! path [raw]]]
               [#'b/stage0-function-table #'stage/stage0-function-table false [{:source-path path}] [table-operations {:source-path path}]]
               [#'b/validate-seed-source-headers! #'stage/validate-seed-source-headers! true [path []] [header-operations path []]]]]
    (doseq [[legacy target private? values expected] cases :let [calls (atom [])]]
      (is (= private? (boolean (:private (meta legacy)))))
      (with-redefs-fn {target (fn [& actual] (swap! calls conj (vec actual)) token)}
        #(is (identical? token (apply @legacy values))))
      (is (= [expected] @calls)))
    (is (= '([source-path form]) (:arglists (meta #'b/function-definition-form))))
    (is (= '([source-path {:keys [name params body]}]) (:arglists (meta #'b/validate-function-definition!))))
    (is (= '([module]) (:arglists (meta #'b/stage0-function-table))))))

(deftest paired-real-source-retains-tables-declarations-plan-identity-and-refusals
  (doseq [extension [".gravity" ".qst"]
          [definition expected]
          [["(def f (fn [x :- Int] :- Int (+ x 2)))" "5\n"]
           ["(def f (fn [x x] x))" "L2-FUNCTION-PARAMS"]
           ["(def f (fn [x] x)) (def f (fn [x] x))" "L2-DUPLICATE-FUNCTION"]]]
    (let [path (str "functions" extension)
          source (str "(ns functions (:profile :hosted) (:target :jvm) (:effects #{:io/write}) (:capabilities #{:io/stdout}))\n"
                      definition " (def main (fn [] (println (f 3))))")
          _ (is (nil? (var-get #'b/*authenticated-source-form-records*)))
          artifact (b/macro-source-artifact path source)
          module (assoc (:module artifact) :forms (:expanded-forms artifact))]
      (if (= expected "5\n")
        (let [table (b/stage0-function-table module) plan (b/stage0-compiled-core-plan path source module)]
          (is (= '[x] (get-in table ['f :params])))
          (is (= '[x :- Int] (get-in table ['f :source-declarations :parameters])))
          (is (= expected (b/run-main module) (b/execute-stage0-compiled-plan plan)))
          (is (= path (get-in plan [:source :path])))
          (is (= (str "sha256:" (digest/sha256-hex source)) (get-in plan [:source :sha256])))
          (is (= (:plan-id plan) (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))))))
        (let [error (observed-error #(b/stage0-function-table module))]
          (is (= expected (:id (ex-data error))))
          (is (= path (get-in (ex-data error) [:source-span :source]))))))))
