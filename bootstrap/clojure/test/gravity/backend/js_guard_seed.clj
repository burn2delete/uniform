(ns ^{:clj-kondo/config '{:lint-as {gravity.backend.js-guard-seed/unavailable-operations clojure.core/declare}
                           :linters {:unused-namespace {:exclude [clojure.set clojure.string]}}}}
  gravity.backend.js-guard-seed
  "Operation-level B6 test harness for the ordinary single-module stage2 seed."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.bootstrap.module-literals :as literals]
            [gravity.build.reader-bridge :as declared-reader-bridge]
            [gravity.diagnostics :refer [fail!]]
            [gravity.digest :as digest]
            [gravity.module-analysis :as modules]
            [gravity.reader-host-oracle :as reader]
            [gravity.reader-namespace :as reader-ns]
            [gravity.runtime :as runtime]))

;; Load the same compiler and instruction executor components as gravity.bootstrap,
;; into this test namespace. Hooks for other modes fail if a rule reaches them.
(defn- unavailable [operation]
  (throw (ex-info "B6 rule test requires a pure, ordinary stage2 module"
                  {:unsupported operation})))

(defmacro ^:private unavailable-operations [& operations]
  `(do ~@(for [operation operations]
           `(defn- ~operation [& ~'_] (unavailable '~operation)))))

(unavailable-operations
 p15-s23-stage2-sort-values p15-s23-stage2-module-fail!
 reader-canonical-hash sh06-resolution-semantic-span
 p15-s23-sh07-compiler-internal-caller-authorized? p15-s23-sh07-compiler-internal-provenance
 p15-s23-sh07-b51-external-instruction p15-s23-stage2-module-compile-call)

(declare defn-form? p15-s23-stage2-compiler-artifact-builtins p15-s23-stage2-seed-compile-function
         p15-s23-stage2-compiler-pinned-source! p15-s23-compiler-source-form-record-from-text
         p15-s23-stage2-plan-emitter-fail! p15-s23-stage2-runtime-execute-instructions-scoped
         p15-s23-stage2-runtime-recur-signal? p15-s23-stage2-runtime-recur-values
         p15-s23-stage2-runtime-recur-fail! p15-s23-stage2-runtime-execute-values-scoped)

;; The shared pin and reader components call these host ports directly.
(def sha256-bytes-hex digest/sha256-bytes-hex)
(def read-source-form-records reader/read-source-form-records-host-oracle)
(def validate-ns-syntax! reader-ns/validate-ns-syntax!)
(def parse-module modules/parse-module)

(load "/gravity/bootstrap_impl/components/foundation/00516-defn-form"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00645-p15-s23-stage2-runtime-artifact-source-relative-path"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00574-p15-s23-stage2-plan-emitter-required-preserves"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00575-p15-s23-stage2-seed-validate-recur"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00575-p15-s23-stage2-module-map-order"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00576-p15-s23-stage2-seed-compile-expr")

(def ^:dynamic *p15-s23-seed-readable-owned-failures* nil)

(unavailable-operations
 p15-s23-seed-readable-preflight! p15-s23-seed-readable-value-text p15-s23-seed-readable-printer-fail!
 p15-s23-stage2-compiler-artifact-expected-artifact-hash p15-s23-stage2-pinned-augmented-plan-authorized?
 p15-s23-sh07-b51-runtime-augmented-plan-authorized? p15-s23-sh07-runtime-invoke-compiler-internal-builtin
 p15-s23-stage2-runtime-execute-function p15-s23-sh07-b51-runtime-authorize-external-link
 p15-s23-sh07-b51-runtime-execute-external-machine
 validate-module-effects! p15-s23-stage2-runtime-artifact-invoke p15-s23-stage2-module-execute-call
 p15-s23-stage2-native-carrier-header p15-s23-stage2-hosted-native-data)

(def ^:private p15-s23-stage2-runtime-fail-call-arity! (partial runtime/fail-call-arity! #'fail!))
(def p15-s23-stage2-runtime-assert-min-arity! (partial runtime/assert-min-arity! #'p15-s23-stage2-runtime-fail-call-arity!))
(def p15-s23-stage2-runtime-assert-exact-arity! (partial runtime/assert-exact-arity! #'p15-s23-stage2-runtime-fail-call-arity!))
(def p15-s23-stage2-runtime-assert-between-arity! (partial runtime/assert-between-arity! #'p15-s23-stage2-runtime-fail-call-arity!))
(def p15-s23-stage2-runtime-assert-even-arity! (partial runtime/assert-even-arity! #'p15-s23-stage2-runtime-fail-call-arity!))

(load "/gravity/bootstrap_impl/components/bootstrap-p15/00553-p15-s23-seed-readable-pr-str"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00587-p15-s23-stage2-runtime-executor-rule-record-02"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00588-p15-s23-stage2-runtime-invoke-builtin"
      "/gravity/bootstrap_impl/components/foundation/00589-p15s23stage2runtimerecursignal"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00590-p15-s23-stage2-runtime-recur-signal"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00591-p15-s23-stage2-runtime-invoke-unary-or-generic-builtin"
      "/gravity/bootstrap_impl/components/bootstrap-p15/00592-p15-s23-stage2-runtime-execute-instruction")

(defn- ordinary-scope! [& args]
  (when (some? (last args)) (unavailable :invocation-scope)))

(defn- p15-s23-stage2-runtime-execute-function-scoped [context plan callee args scope]
  (runtime/execute-function-scoped
   context plan callee args scope
   {:native-scope! declared-reader-bridge/native-scope!
    :module-entry-scope! ordinary-scope! :registered-scope? declared-reader-bridge/registered-scope?
    :assert-function-entry! declared-reader-bridge/assert-function-entry!
    :assert-scoped-function-entry! ordinary-scope! :module-scope? some?
    :fail! fail! :fail-call-arity! p15-s23-stage2-runtime-fail-call-arity!
    :bind-values runtime/bind-values :execute-instructions-scoped p15-s23-stage2-runtime-execute-instructions-scoped
    :recur-signal? p15-s23-stage2-runtime-recur-signal? :recur-values p15-s23-stage2-runtime-recur-values
    :recur-fail! p15-s23-stage2-runtime-recur-fail!}))

(defn- external-call! [& _] (unavailable :external-function-call))

(defn p15-s23-stage2-runtime-execute-call-scoped [context plan env instruction scope]
  (runtime/execute-call-scoped
   context plan env instruction scope
   {:execute-values-scoped #'p15-s23-stage2-runtime-execute-values-scoped
    :execute-function-scoped #'p15-s23-stage2-runtime-execute-function-scoped
    :authorize-external-link #'external-call! :execute-external-machine #'external-call!}))

(defn- plan-emitter [owner]
  (let [path "bootstrap/gravity/p15_s23/compiler.gravity"
        pinned (p15-s23-stage2-compiler-pinned-source! path owner :jvm "P15S23Q001" p15-s23-stage2-plan-emitter-fail!)
        forms (:forms (p15-s23-compiler-source-form-record-from-text path (:source-text pinned)))
        rules (for [[op name value] forms :when (= ['def 'p15-s23-stage2-plan-emitter] [op name])] value)]
    (when-not (and (= 1 (count rules)) (map? (first rules)))
      (unavailable :plan-emitter))
    (update-in (first rules) [:call-rules :builtin-functions] into p15-s23-stage2-compiler-artifact-builtins)))

(defn- invalid-literal [reason _] (unavailable reason))

(defn- function-entry [[_ name params & body]]
  (when-not (and (vector? params) (every? simple-symbol? params)
                 (not-any? #{'&} params) (= (count params) (count (set params)))
                 (seq body) (not= :- (first body)))
    (unavailable [:function-header name]))
  [name {:name name :params params :arity (count params) :body body}])

(defn compile-owner [path]
  (let [module (:module (p15-s23-compiler-source-form-record-from-text path (slurp path :encoding "UTF-8")))
        forms (:forms module)
        names (map second forms)
        expected {:module 'gravity.backend.b6-javascript-typescript-backend-design
                  :profile :meta :target :jvm :safety :safe :effects #{} :capabilities #{}
                  :requires [] :imports [] :providers []}]
    (when-not (and (= expected (select-keys module (keys expected)))
                   (every? simple-symbol? names) (= (count names) (count (set names)))
                   (every? #(and (seq? %) (or (defn-form? %) (and (= 'def (first %)) (= 3 (count %))))) forms))
      (unavailable :module-declarations))
    (let [functions (into {} (comp (filter defn-form?) (map function-entry)) forms)
          data (into {} (for [[op name value] forms :when (= 'def op)]
                          [name (literals/materialize! #'invalid-literal value)]))
          compiler-module (assoc module :function-table functions :p15-s23-stage2-module-literals data)
          emitter (plan-emitter path)
          plan {:source {:path path} :module module
                :functions (update-vals functions #(p15-s23-stage2-seed-compile-function emitter compiler-module %))}]
      (fn [callee args] (p15-s23-stage2-runtime-execute-function-scoped {} plan callee args nil)))))
