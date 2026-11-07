(ns gravity.resolution.stage0-namespace-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.module-analysis.clauses :as clauses]
            [gravity.module-analysis.dependencies :as dependencies]
            [gravity.module-analysis.module :as module]
            [gravity.resolution.stage0-namespace :as stage]))

(defn- fail! [id message data] (throw (ex-info message (assoc data :id id))))
(defn- source-span [path index] {:source path :form-index index})
(defn- diagnostic [thunk]
  (try (thunk) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn- thrown [thunk]
  (try (thunk) nil (catch Throwable error error)))
(defn- parser-operations []
  {:require-ns b/require-ns :parse-clause b/parse-clause
   :single-clause-value b/single-clause-value :clause-args b/clause-args
   :parse-dependencies b/parse-dependencies :fail! fail! :source-span source-span
   :known-source-profiles #{:hosted :core} :supported-profiles #{:hosted :core}
   :supported-targets #{:jvm} :bootstrap-target-supported? #{:jvm}})
(def ^:private basic-ports {:fail! fail! :source-span source-span})
(def ^:private valid-forms
  '[(ns parser.unit (:profile :hosted)) (defn main [] nil)])

(deftest child-contract-is-complete-and-does-not-expand-sh06
  (let [publics (ns-publics 'gravity.resolution.stage0-namespace)
        contract (:gravity/namespace-contract (meta #'stage/parse-module))
        operation-inputs
        '{require-ns [:ns-form? :fail! :source-span]
          parse-clause [:fail! :source-span]
          single-clause-value [:fail! :source-span]
          clause-args []
          parse-options [:fail! :source-span]
          parse-dependency-entry [:fail! :source-span :parse-options]
          parse-dependencies [:parse-dependency-entry]
          parse-module [:require-ns :parse-clause :single-clause-value :clause-args
                        :parse-dependencies :fail! :source-span :known-source-profiles
                        :supported-profiles :supported-targets :bootstrap-target-supported?]}]
    (is (= (set (keys operation-inputs)) (set (keys publics))))
    (is (= 55 (count (ns-publics 'gravity.resolution))))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (= '[clojure.string] (get-in contract [:dependency-direction :requires])))
    (is (false? (:canonical-l3-authority? contract)))
    (is (false? (:canonical-c5-admission? contract)))
    (doseq [[name operation] publics :let [api (get-in contract [:public-api name])]]
      (is (fn? @operation))
      (is (= (:arglists (meta operation)) (:arglists api)))
      (is (= contract (:gravity/namespace-contract (meta operation))))
      (is (= :stage0-native-namespace-declaration-parser (:contract-boundary api)))
      (is (= :caller-owned-native-stage0-data (:input-boundary api)))
      (is (= (get operation-inputs name) (:operation-inputs api)))
      (is (= (if (= 'clause-args name) :none :existing-entry-captured-values)
             (:callback-selection api)))
      (is (= :none (:admission api)))
      (is (false? (:authenticates-inputs? api)))
      (is (false? (:proves-legality? api)))
      (is (false? (:grants-authority? api))))))

(deftest clause-operations-preserve-source-values-and-returning-failure-behavior
  (let [path (Object.) first-form '(ns parser.unit) calls (atom [])
        ports {:ns-form? (fn [form] (swap! calls conj [:classify form]) :truthy)
               :fail! fail! :source-span source-span}]
    (is (identical? first-form (stage/require-ns ports path [first-form '(ignored)])))
    (is (= [[:classify first-form]] @calls))
    (is (= [:profile [:hosted]] (stage/parse-clause basic-ports path '(:profile :hosted))))
    (let [result (diagnostic #(stage/require-ns (assoc ports :ns-form? (constantly false)) path [first-form]))]
      (is (= "L3-NS-MISSING" (:id result)))
      (is (identical? path (get-in result [:source-span :source])))))
  (let [calls (atom []) clause [:profile :hosted]
        ports {:fail! (fn [id _ data] (swap! calls conj [id data]) false) :source-span source-span}]
    (is (= [:profile [:hosted]] (stage/parse-clause ports "unit.gravity" clause)))
    (is (= ["L3-NS-CLAUSE"] (mapv first @calls)))
    (is (identical? clause (get-in @calls [0 1 :clause])))))

(deftest single-clause-cardinality-and-argument-order-are-preserved
  (let [value (Object.) marker (Object.) calls (atom [])
        ports {:fail! (fn [id _ data] (swap! calls conj [id data]) marker) :source-span source-span}]
    (is (identical? value (stage/single-clause-value ports "unit.gravity" {:x [value]} :x true)))
    (is (nil? (stage/single-clause-value ports "unit.gravity" {} :x false)))
    (is (identical? marker (stage/single-clause-value ports "unit.gravity" {} :x true)))
    (is (identical? marker (stage/single-clause-value ports "unit.gravity" {:x [1 2]} :x false)))
    (is (= ["L3-NS-MISSING" "L3-PROFILE-MULTIPLE"] (mapv first @calls)))
    (is (= :x (get-in @calls [0 1 :missing])))
    (is (= :x (get-in @calls [1 1 :clause])))
    (is (= [value :second :third] (stage/clause-args {:x [[value :second] '(:third)]} :x)))
    (is (= [] (stage/clause-args {} :missing)))))

(deftest options-retain-last-value-nil-false-and-original-entry-identity
  (let [path (Object.) entry (Object.) value (Object.)
        result (stage/parse-options basic-ports path entry [:as :old :as value :x nil :y false])]
    (is (identical? value (:as result)))
    (is (contains? result :x)) (is (nil? (:x result))) (is (false? (:y result)))
    (doseq [items [[:as] ['alias :value]]]
      (let [failure (diagnostic #(stage/parse-options basic-ports path entry items))]
        (is (= "L3-UNKNOWN-ALIAS" (:id failure)))
        (is (identical? entry (:entry failure)))
        (is (identical? path (get-in failure [:source-span :source]))))))
  (let [ids (atom []) ports {:fail! (fn [id & _] (swap! ids conj id) false) :source-span source-span}]
    (is (= {:as nil} (stage/parse-options ports "unit.gravity" :entry [:as])))
    (is (= ["L3-UNKNOWN-ALIAS"] @ids))))

(deftest dependency-records-preserve-metadata-defaults-and-source-order
  (let [entry '[dep.core :as d :refer [value] :profile :core :boundary :pure :edge :direct
                :facade dep.facade :evidence #{:checked} :artifact :object :artifact-schema :schema
                :runtime :none :memory :managed :generated? true :matrix-override :matrix
                :producer-effects #{:io/write} :producer-capabilities #{:io/stdout}
                :safety-evidence #{:runtime-checked} :provider :owner :effects #{:io/write}
                :capabilities #{:io/stdout} :visibility :private]
        ports (assoc basic-ports :parse-options b/parse-options)
        record (stage/parse-dependency-entry ports "unit.gravity" :require entry)]
    (is (= {:kind :require :module 'dep.core :alias 'd :refer '[value] :profile :core
            :boundary :pure :edge :direct :facade 'dep.facade :evidence #{:checked}
            :artifact :object :artifact-schema :schema :runtime :none :memory :managed
            :generated? true :matrix-override :matrix :producer-effects #{:io/write}
            :producer-capabilities #{:io/stdout} :safety-evidence #{:runtime-checked}
            :provider :owner :effects #{:io/write} :capabilities #{:io/stdout} :visibility :private}
           record))
    (let [defaults (stage/parse-dependency-entry ports "unit.gravity" :import '[dep.empty])]
      (is (= [] (:refer defaults))) (is (= #{} (:effects defaults)))
      (is (= #{} (:capabilities defaults))) (is (= :public (:visibility defaults)))
      (is (false? (:generated? defaults))))
    (is (= '[first.core dep.core last.core]
           (mapv :module (stage/parse-dependencies {:parse-dependency-entry b/parse-dependency-entry}
                                                 "unit.gravity" :require ['[first.core] entry '[last.core]]))))))

(deftest dependency-shape-refusals-keep-the-existing-owner-order
  (doseq [[entry expected] [['(dep.core :as d) "L3-UNKNOWN-ALIAS"]
                          ['[dep.core :as "d"] "L3-UNKNOWN-ALIAS"]
                          ['[dep.core :refer :all] "L3-AMBIGUOUS-NAME"]
                          ['[dep.core :refer [private-value]] "L3-PRIVATE-IMPORT"]]]
    (let [result (diagnostic #(stage/parse-dependency-entry (assoc basic-ports :parse-options b/parse-options)
                                                         "unit.gravity" :require entry))]
      (is (= expected (:id result)))
      (is (identical? entry (:entry result))))))

(deftest module-declaration-keeps-all-fields-and-defaults
  (let [path (Object.) forms '[(ns parser.rich (:profile :hosted) (:target :jvm)
                               (:requires [dep.core :as d :profile :core]) (:imports [host.api :as h])
                               (:exports [main]) (:effects #{:io/write}) (:capabilities #{:io/stdout})
                               (:safety :safe-optimized) (:providers [stdout])
                               (:metadata {:package parser/app}) (:doc "Parser declaration"))
                              (defn main [] nil)]
        declaration (stage/parse-module (parser-operations) path forms)]
    (is (identical? path (:source-path declaration)))
    (is (= {:module 'parser.rich :profile :hosted :target :jvm :exports '[main]
            :effects #{:io/write} :capabilities #{:io/stdout} :safety :safe-optimized
            :providers '[stdout] :metadata {:package 'parser/app} :doc "Parser declaration"
            :forms '[(defn main [] nil)]}
           (dissoc declaration :source-path :requires :imports)))
    (is (= '[dep.core] (mapv :module (:requires declaration))))
    (is (= '[host.api] (mapv :module (:imports declaration))))
    (is (not (contains? declaration :artifact-id)))
    (is (not (contains? declaration :source-hash))))
  (let [result (stage/parse-module (parser-operations) "unit.gravity" valid-forms)]
    (is (= [:jvm #{} #{} [] [] [] :safe [] {} nil]
           ((juxt :target :effects :capabilities :requires :imports :exports :safety :providers :metadata :doc) result)))))

(deftest opaque-operation-map-is-captured-once-by-child-and-legacy-adapter
  (doseq [parse [stage/parse-module module/parse-module]]
    (let [events (atom []) original (Error. "original callback") late (Error. "late callback")
          values (atom (parser-operations))
          _ (swap! values assoc
                   :fail! (fn [id & _] (swap! events conj [:original-failure id]) (throw original))
                   :require-ns (fn [path forms]
                                 (swap! events conj :require)
                                 (swap! values assoc :fail! (fn [& _] (throw late))
                                        :known-source-profiles #{:unknown} :bootstrap-target-supported? (constantly false))
                                 (b/require-ns path forms)))
          carrier (reify clojure.lang.ILookup
                    (valAt [_ key] (swap! events conj [:lookup key]) (get @values key))
                    (valAt [_ key default] (swap! events conj [:lookup key]) (get @values key default)))
          result (thrown #(parse carrier "unit.gravity" '[(ns parser.capture (:profile :unknown))]))]
      (is (identical? original result))
      ;; The native eleven-key destructure uses its legacy hash-map order,
      ;; while all values are still captured before the first parser call.
      (is (= [[:lookup :fail!] [:lookup :known-source-profiles] [:lookup :source-span]
              [:lookup :clause-args] [:lookup :bootstrap-target-supported?] [:lookup :single-clause-value]
              [:lookup :parse-dependencies] [:lookup :supported-targets] [:lookup :require-ns]
              [:lookup :supported-profiles] [:lookup :parse-clause]
              :require [:original-failure "P1-PROFILE-UNSUPPORTED"]]
             @events)))))

(deftest compatibility-adapters-preserve-arglists-and-stage-interposition
  (doseq [[legacy moved arguments]
          [[#'clauses/require-ns #'stage/require-ns [{:ns-form? identity :fail! fail! :source-span source-span} :path [:forms]]]
           [#'clauses/parse-clause #'stage/parse-clause [basic-ports :path :clause]]
           [#'clauses/single-clause-value #'stage/single-clause-value [basic-ports :path {} :key false]]
           [#'clauses/clause-args #'stage/clause-args [{} :key]]
           [#'dependencies/parse-options #'stage/parse-options [basic-ports :path :entry []]]
           [#'dependencies/parse-dependency-entry #'stage/parse-dependency-entry [(assoc basic-ports :parse-options identity) :path :require :entry]]
           [#'dependencies/parse-dependencies #'stage/parse-dependencies [{:parse-dependency-entry identity} :path :require []]]
           [#'module/parse-module #'stage/parse-module [(parser-operations) :path valid-forms]]]]
    (let [marker (Object.) seen (atom nil)]
      (is (= (:arglists (meta legacy)) (:arglists (meta moved))))
      (is (identical? marker (with-redefs-fn {moved (fn [& args] (reset! seen (vec args)) marker)}
                              #(apply legacy arguments))))
      (is (= arguments @seen)))))

(deftest real-source-macro-artifacts-and-executable-plans-remain-deterministic
  (is (nil? (var-get #'b/*authenticated-source-form-records*)))
  (doseq [path ["examples/hello.gravity" "examples/hello.qst"
               "bootstrap/clojure/fixtures/accepted/namespace-module.gravity"
               "bootstrap/clojure/fixtures/accepted/namespace-module.qst"]]
    (let [text (slurp path :encoding "UTF-8") original b/parse-module declarations (atom [])
          artifact (with-redefs [b/parse-module (fn [& args]
                                                (let [result (apply original args)]
                                                  (swap! declarations conj result) result))]
                     (b/macro-source-artifact path text))]
      (is (= :gravity/stage0-macro-artifact (:kind artifact)))
      (is (= artifact (b/macro-source-artifact path text)))
      (is (= 1 (count @declarations)))
      (is (= path (:source-path (first @declarations)) (get-in artifact [:module :source-path])))
      (is (seq (:expanded-syntax-object-stream artifact)))
      (is (every? :syntax-id (:generated-origin-source-map artifact)))
      (if (= 'app.server (get-in artifact [:module :module]))
        (do (is (= '[gravity.io shared.core] (mapv :module (:requires (first @declarations)))))
            (is (= '[java.time.Instant] (mapv :module (:imports (first @declarations)))))
            (is (= '[main start] (:exports (first @declarations))))
            (is (= '[stdout] (:providers (first @declarations)))))
        (let [input (assoc (:module artifact) :forms (:expanded-forms artifact))
              plan (b/stage0-compiled-core-plan path text input)
              report (b/stage0-compiled-plan-compiler-report plan input)]
          (is (= plan (b/stage0-compiled-core-plan path text input)))
          (is (= path (get-in plan [:source :path])))
          (is (= (str "sha256:" (b/sha256-hex text)) (get-in plan [:source :sha256])))
          (is (= (:plan-id plan) (:compiled-plan-id report)))
          (is (= "Hello Gravity\n" (b/execute-stage0-compiled-plan plan))))))))

(deftest real-parser-refusals-and-earlier-reader-control-stay-distinct
  (is (nil? (var-get #'b/*authenticated-source-form-records*)))
  (doseq [[path text expected calls]
          [["bootstrap/clojure/fixtures/rejected/multiple-profile.gravity" nil "L3-PROFILE-MULTIPLE" 1]
           ["bootstrap/clojure/fixtures/rejected/private-import.gravity" nil "L3-PRIVATE-IMPORT" 1]
           ["parser-profile.gravity" "(ns parser.profile (:profile :unknown))" "P1-PROFILE-UNSUPPORTED" 1]
           ["parser-target.gravity" "(ns parser.target (:profile :hosted) (:target :unknown))" "B1-TARGET-UNSUPPORTED" 1]
           ["parser-required.gravity" "(ns parser.required)" "L3-NS-MISSING" 1]
           ["parser-shape.gravity" "(ns parser.shape (:profile :hosted) :malformed)" "L1-NS-SHAPE" 0]]]
    (let [original b/parse-module seen (atom 0) emitted (atom nil)
          result (with-redefs [b/parse-module (fn [& args] (swap! seen inc) (apply original args))]
                   (diagnostic #(reset! emitted (b/macro-source-artifact path (or text (slurp path :encoding "UTF-8"))))))]
      (is (= expected (:id result)))
      (is (= calls @seen))
      (is (= path (get-in result [:source-span :source])))
      (is (nil? @emitted)))))
