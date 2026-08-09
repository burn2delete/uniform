(ns gravity.c5-name-resolution-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.c5-name-resolution :as c5]))

(def module
  {:module 'demo.main
   :source-path "demo.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :exports ['main 'value]
   :requires [{:kind :require :module 'shared.core :alias 'shared
               :refer ['checksum] :profile :core :boundary :pure-core
               :effects #{} :capabilities #{} :visibility :public}]
   :imports [{:kind :import :module 'java.time.Instant :alias 'instant
              :refer [] :profile :hosted :boundary :interop
              :effects #{} :capabilities #{} :visibility :public}]
   :metadata {:package 'demo/pkg
              :compiler {:c5-resolution {}}}})

(def syntax-stream
  [{:syntax-id "syntax-1"
    :form '(def value 1)
    :span {:source "demo.gravity" :form-index 1}}
   {:syntax-id "syntax-2"
    :form '(defn main [x] (let [y x] (do y shared/checksum demo.main/value I64 m +)))
    :span {:source "demo.gravity" :form-index 2}}])

(def c4-artifact
  {:kind :gravity/stage0-c4-macro-expansion-artifact
   :artifact-id "sha256:c4"
   :expanded-syntax-stream syntax-stream
   :macro-expansion-trace []
   :macro-environment {:macro-vars [{:macro 'm
                                     :namespace 'demo.main
                                     :capabilities #{}}]}
   :generated-origin-source-map {:status :complete}})

(def module-artifact
  {:definitions [{:name 'value :kind :var :visibility :public
                  :source-span {:source "demo.gravity" :form-index 1}
                  :latent-effects #{} :required-capabilities []}
                 {:name 'main :kind :function :visibility :public
                  :source-span {:source "demo.gravity" :form-index 2}
                  :latent-effects #{} :required-capabilities []}]})

(def records
  (mapv (fn [syntax] {:form (:form syntax)}) syntax-stream))

(def injected-operations
  {:read-source-form-records (fn [_ _] records)
   :validate-ns-syntax! (fn [_ _] nil)
   :parse-module (fn [_ _] module)
   :compiler-c4-macro-source-artifact (fn [_ _] c4-artifact)
   :module-source-artifact (fn [_ _] module-artifact)})

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c5/c5-engine-contract)]
    (is (= 'gravity.c5-name-resolution (:namespace contract)))
    (is (true? (:compatibility-only? contract)))
    (is (true? (:override-driven-diagnostics? contract)))
    (is (false? (:canonical-c5-authority? contract)))
    (is (false? (:cycle-analysis-complete? contract)))
    (is (= #{'gravity.bootstrap 'gravity.diagnostics}
           (set (get-in contract [:dependency-direction :forbids]))))
    (is (= (set (keys c5/public-api))
           (set (keys (ns-publics 'gravity.c5-name-resolution)))))
    (doseq [[name spec] c5/public-api
            :when (:arglists spec)]
      (is (= (:arglists spec)
             (:arglists (meta (get (ns-publics 'gravity.c5-name-resolution)
                                   name))))))
    (is (not (contains? (set (keys (ns-aliases 'gravity.c5-name-resolution)))
                        'gravity.bootstrap)))
    (is (not (contains? (set (keys (ns-aliases 'gravity.c5-name-resolution)))
                        'gravity.diagnostics)))))

(deftest operation-map-is-validated-and-supports-interposition
  (is (try
        (c5/with-operations nil (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"must be a map" (.getMessage error)))))
  (is (try
        (c5/with-operations {:not-an-operation identity}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"unknown keys" (.getMessage error)))))
  (is (try
        (c5/with-operations {:sha256-hex 1} (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"must be callable" (.getMessage error)))))
  (is (try
        (c5/with-operations {:sha256-hex :keyword-is-invokable}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"must be callable" (.getMessage error)))))
  (is (try
        (c5/with-operations {:known-source-profiles [:hosted]}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"known-source-profiles" (.getMessage error)))))
  (is (try
        (c5/with-operations {:supported-targets #{:jvm "native"}}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"supported-targets" (.getMessage error)))))
  (is (try
        (c5/with-operations {:c5-resolution-diagnostic-ids #{"C5"}}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"invalid shape" (.getMessage error)))))
  (is (try
        (c5/with-operations {:c5-resolution-governing-document :c5}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"invalid shape" (.getMessage error)))))
  (is (try
        (c5/with-operations {:c5-resolution-rejected-designs [:not-a-map]}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"invalid shape" (.getMessage error)))))
  (is (try
        (c5/with-operations {:c5-resolution-override-diagnostics
                             {:alias :not-a-string}}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"invalid shape" (.getMessage error)))))
  (is (try
        (c5/with-operations {:c5-special-form-symbols #{:if}}
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"invalid shape" (.getMessage error)))))
  (is (try
        (c5/with-operations {:supported-targets #{} }
          (fn [] :unreachable))
        false
        (catch clojure.lang.ExceptionInfo error
          (re-find #"non-empty keyword set" (.getMessage error)))))
  (is (= #{:jvm :wasm}
         (:target-set
          (c5/with-operations {:supported-targets #{:jvm :wasm}}
            #(c5/c5-special-form-binding 'if module)))))
  (is (= {:source-wrapper :interposed}
         (c5/with-operations
           {:compiler-c5-resolution-source-artifact
            (fn [_ _] {:source-wrapper :interposed})}
           #(c5/compiler-c5-resolution-file-artifact
             "bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity"))))
  (is (= "sha256:interposed"
         (c5/with-operations {:sha256-hex (constantly "interposed")}
           #(c5/c5-binding-id {:name 'x :kind :var})))))

(deftest binding-engine-emits-resolution-products
  (let [artifact (c5/with-operations
                   injected-operations
                   #(c5/compiler-c5-resolution-source-artifact
                     "demo.gravity" "ignored"))
        records (:bindings (:binding-table artifact))
        orders (set (map :resolution-order records))]
    (is (= :gravity/stage0-c5-name-resolution-artifact (:kind artifact)))
    (is (= #{:local :namespace :alias-qualified :fully-qualified
             :type-position :special-form :core-auto-import}
           (set/intersection orders
                             #{:local :namespace :alias-qualified
                               :fully-qualified :type-position
                               :special-form :core-auto-import})))
    (is (some #(= :local (:resolution-order %)) records))
    (is (some #(= :namespace (:resolution-order %)) records))
    (is (some #(= :alias-qualified (:resolution-order %)) records))
    (is (some #(= :fully-qualified (:resolution-order %)) records))
    (is (some #(= :type-position (:resolution-order %)) records))
    (is (some #(= :macro (:kind %))
              (:namespace-bindings (:binding-table artifact))))
    (is (= :complete (get-in artifact [:namespace-analysis :status])))
    (is (= :complete (get-in artifact [:dependency-graph :status])))
    (is (= :stable (get-in artifact [:incremental-invalidation-keys :status])))
    (is (= :passed (get-in artifact [:resolution-verification-report :status])))
    (is (= :complete (:status (:capability-based-proof artifact))))))

(deftest override-driven-diagnostics-retain-c5-identities
  (doseq [[fail-kind diagnostic message]
          [[:unresolved "C5-UNRESOLVED" "symbol has no resolvable binding"]
           [:ambiguous "C5-AMBIGUOUS" "symbol has multiple legal bindings"]
           [:private "C5-PRIVATE" "private binding is accessed outside its namespace boundary"]
           [:alias "C5-ALIAS" "namespace alias is unknown or duplicated"]
           [:shadow "C5-SHADOW" "lexical binding shadows a namespace binding illegally"]
           [:cycle "C5-CYCLE" "namespace dependency graph contains an illegal cycle"]
           [:cross-profile "C5-CROSS-PROFILE" "cross-profile import lacks an accepted boundary"]
           [:capability "C5-CAPABILITY" "imported binding requires an unavailable capability"]
           [:target "C5-TARGET" "imported binding is incompatible with the active target"]
           [:foreign "C5-FOREIGN" "foreign import record is malformed"]]]
    (let [failure (atom nil)
          operations (assoc injected-operations
                            :fail! (fn [id _ data]
                                     (reset! failure (assoc data :id id))
                                     ::failed))]
      (is (= message (c5/c5-resolution-message diagnostic)))
      (is (= ::failed
             (c5/with-operations operations
               #(c5/c5-resolution-validate-overrides!
                 "fixture.gravity" module {:fail fail-kind}))))
      (is (= diagnostic (:id @failure)))
      (is (= :name-resolution (:stage @failure)))
      (is (= (str "fixture/" (name fail-kind))
             (str (:symbol @failure)))))))
