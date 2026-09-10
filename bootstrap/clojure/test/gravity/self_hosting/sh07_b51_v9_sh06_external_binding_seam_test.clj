(ns gravity.self-hosting.sh07-b51-v9-sh06-external-binding-seam-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "required SH06 lexical seam is absent"
                      {:symbol symbol}))))

(defn- invoke-private [symbol & arguments]
  (apply (required-var symbol) arguments))

(defn- c5-failure? [thunk]
  (try
    (thunk)
    false
    (catch clojure.lang.ExceptionInfo error
      (= "C5-UNRESOLVED" (:id (ex-data error))))))

(deftest strict-context-arities-are-explicit-and-nil-fails-closed
  (testing "the governed lexical arities are present"
    (is (= '([source-path sh05-artifact]
             [source-path sh05-artifact context])
           (:arglists (meta (required-var 'sh06-resolution-request)))))
    (is (= '([artifact] [artifact context])
           (:arglists
            (meta (required-var 'sh06-resolution-artifact-verification)))))
    (is (= '([resolution-artifact] [resolution-artifact context])
           (:arglists (meta (required-var 'sh07-core-authenticated-request)))))
    (is (= '([sh05-artifact]
             [source-path sh05-artifact]
             [source-path sh05-artifact context])
           (:arglists
            (meta
             (required-var
              'sh06-resolution-artifact-from-sh05-artifact))))))
  (testing "nil never selects an ordinary implementation"
    (is (c5-failure?
         #(bootstrap/sh06-resolution-request "module.gravity" nil nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-artifact-from-sh05-artifact
           "module.gravity" nil nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-context-root nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-context-plan-binding!
           "module.gravity" nil)))))

(defn- digest [character]
  (str "sha256:" (apply str (repeat 64 character))))

(defn- m0-row [ordinal module owner]
  {:module-source-id (digest (char (+ (int \a) ordinal)))
   :module-ordinal ordinal :module module :owner owner
   :logical-source-path (str "module-" ordinal ".gravity")
   :profile :meta :target :jvm :effects #{} :capabilities #{}
   :safety :safe :definitions [] :external-bindings []})

(defn- minimal-authority-family []
  (let [symbol 'provider
        source-hash (digest \f)
        caller-base (m0-row 0 'gravity.caller :r1-producer)
        callee-base (m0-row 1 'gravity.provider :r1-producer)
        external {:symbol symbol :callee-module-ordinal 1
                  :callee-module (:module callee-base)
                  :callee-source-sha256 (digest \b)
                  :callee-definition-source-hash source-hash
                  :arity 1 :owner-edge :producer-internal}
        caller (assoc caller-base :external-bindings [external])
        callee (assoc callee-base
                      :source-sha256 (digest \b)
                      :definitions [{:name symbol :arity 1
                                     :visibility :private
                                     :source-hash source-hash}])
        descriptors (into [caller callee]
                          (mapv (fn [ordinal]
                                  (m0-row ordinal
                                          (symbol (str "gravity.m" ordinal))
                                          :r1-producer))
                                (range 2 7)))
        edge {:ordinal 0
              :caller-module-source-id (:module-source-id caller)
              :caller-function 'caller
              :caller-function-source-hash (digest \e)
              :callsite-path [0]
              :callee-module-source-id (:module-source-id callee)
              :callee-function symbol
              :callee-function-source-hash source-hash
              :arity 1 :owner-edge :producer-internal
              :authorization :authenticated-b51-namespace-external}
        manifest {:manifest-id (digest \9) :edges [edge]}]
    {:descriptors descriptors :manifest manifest}))

(deftest visibility-is-granted-only-by-an-exact-authenticated-edge
  (let [{:keys [descriptors manifest]} (minimal-authority-family)
        project
        #(invoke-private 'sh06-resolution-c5-expected-projection!
                         "module-0.gravity" 0 descriptors %)]
    (testing "an exact M1 grant promotes the provider into a public SH06 import"
      (let [projection (project manifest)
            binding (first (:import-bindings projection))]
        (is (= 'provider (:name binding)))
        (is (= :public (:visibility binding)))
        (is (= :import (:binding-class binding)))
        (is (= {:source :external-binding} (:source-span binding)))
        (is (= [0]
               (get-in binding
                       [:semantic-span :authorized-edge-ordinals])))))
    (testing "removing authorization rejects before a projection is returned"
      (is (c5-failure?
           #(project (assoc-in manifest [:edges 0 :authorization]
                               :unauthorized)))))
    (testing "changing the owner edge also rejects"
      (is (c5-failure?
           #(project (assoc-in manifest [:edges 0 :owner-edge]
                               :root6-internal)))))))

(deftest ordinary-and-strict-paths-have-no-ambient-authority-seam
  (let [bounded (meta (required-var
                       'sh06-resolution-artifact-verification-bounded*))
        candidate (meta (required-var
                         'sh06-resolution-source-artifact-candidate))]
    (is (:private bounded))
    (is (:private candidate))
    (is (= '([artifact construction?]
             [artifact construction? context])
           (:arglists bounded)))
    (is (= '([source-path sh05-artifact legacy?]
             [source-path sh05-artifact legacy? context])
           (:arglists candidate)))))
