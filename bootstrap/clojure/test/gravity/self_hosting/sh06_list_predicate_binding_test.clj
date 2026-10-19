(ns gravity.self-hosting.sh06-list-predicate-binding-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; C5/L3 recognition for an already declared seed primitive, not typed List
;; predicate semantics, host lookup, runtime execution or module admission.
(defn- source-path []
  ;; This retained inline-source label is not an on-disk Gravity fixture.
  (.getPath (io/file (.getParentFile
                     (io/file (.toURI (io/resource "gravity/self_hosting/sh06_list_predicate_binding_test.clj"))))
                    "list-predicate-binding-inline.gravity")))

(defn- source [body]
  (str "(ns example.list-predicate-binding (:profile :hosted) (:target :jvm)"
       " (:effects #{}) (:capabilities #{}) (:safety :safe))\n" body "\n"))

(deftest sh06-list-predicate-binding-authenticated-source
  ;; Three tiny real C2 -> C3 -> SH05 -> C5 source attempts. No raw seed plan,
  ;; supplied accepted receipt or rewritten runtime subject substitutes for C5.
  (let [path (source-path)
        text (source (str "(defn core [value] [(list? value) (gravity.core/list? value)])\n"
                          "(defn shadow [list? value] (list? value))\n"
                          "(defn quoted [] '(listish? clojure.core/list?))"))
        _ (println :list-predicate-binding-phase :accepted-source)
        artifact (bootstrap/compiler-c5-resolution-source-artifact path text)
        request (get-in artifact [:gravity-resolution-boundary :authenticated-resolution-request])
        bindings (get-in artifact [:binding-table :bindings])
        rows (get-in artifact [:gravity-resolution-boundary :resolved-analysis :resolution-table])
        references (:references request)
        by-id (into {} (map (juxt :binding-id identity)) bindings)
        core (filterv #(and (= 'list? (:name %)) (= :core (:binding-class %))) bindings)
        declared (filterv #(= 'list? (:name %)) (:core-bindings request))
        uses (filterv #(contains? '#{list? gravity.core/list?} (:symbol %)) rows)
        selected (mapv #(get by-id (:binding-id %)) uses)
        core-uses (filterv #(= :core (:binding-class %)) selected)
        lexical-uses (filterv #(= :lexical (:binding-class %)) selected)]
    (doseq [[label actual expected]
            [[:core-inventory (contains? bootstrap/sh06-resolution-core-symbols 'list?) true]
             [:seed-inventory (contains? bootstrap/p15-s23-stage2-compiler-artifact-builtins 'list?) true]
             [:accepted (:status artifact) :accepted]
             [:fresh-verifier (:status (bootstrap/sh06-resolution-artifact-verification artifact)) :passed]
             [:one-core-binding (count core) 1]
             [:one-declaration (count declared) 1]
             [:core-metadata ((juxt :namespace :kind :visibility :type-ref) (first core))
              ['gravity.core :var :public :gravity.type/core-var]]
             [:original-definition
              (select-keys (first core) [:definition-syntax-id :definition-artifact-id :source-span])
              (select-keys (first declared) [:definition-syntax-id :definition-artifact-id :source-span])]
             [:hosted-available (contains? (set (:profile-set (first core))) :hosted) true]
             [:target (:target-set (first core)) [:all]]
             [:effects (:effects (first core)) []]
             [:capabilities (:capabilities (first core)) []]
             [:three-executable-uses (count uses) 3]
             [:two-core-uses (count core-uses) 2]
             [:one-lexical-use (count lexical-uses) 1]
             [:same-core-identity (set (map :binding-id core-uses)) #{(:binding-id (first core))}]
             [:lexical-metadata ((juxt :name :kind) (first lexical-uses)) ['list? :local]]
             [:distinct-lexical-identity
              (not= (:binding-id (first core)) (:binding-id (first lexical-uses))) true]]]
      (is (= expected actual) label))
    (doseq [row uses
            :let [reference (first (filter #(= (:syntax-id row) (:syntax-id %)) references))]]
      (is (= :operator (:position reference)))
      (is (= (:symbol reference) (:symbol row)))
      (is (string? (:syntax-id reference)))
      (is (= path (get-in reference [:source-span :primary :source]))))
    (is (not-any? #(contains? '#{listish? clojure.core/list?} (:symbol %)) references))
    (doseq [[operator expected reason]
            [['listish? "C5-UNRESOLVED" :no-legal-binding]
             ['clojure.core/list? "C5-ALIAS" :unknown-namespace-alias]]]
      (let [returned (atom ::not-returned)
            _ (println :list-predicate-binding-phase operator)
            failure (try
                      (reset! returned
                              (bootstrap/compiler-c5-resolution-source-artifact
                               path (source (str "(defn rejected [value] (" operator " value))"))))
                      nil
                      (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (doseq [[label actual expected-value]
                [[:no-artifact @returned ::not-returned]
                 [:rule (:id failure) expected]
                 [:symbol (:symbol failure) operator]
                 [:reason (get-in failure [:facts :reason]) reason]
                 [:candidates (:candidate-bindings failure) []]
                 [:syntax (string? (:syntax-id failure)) true]
                 [:span (get-in failure [:source-span :primary :source]) path]
                 [:policy [(:profile failure) (:target failure)] [:hosted :jvm]]]]
          (is (= expected-value actual) [operator label]))))))
