(ns gravity.c5-name-resolution.bindings
  (:require [gravity.c5-name-resolution.config :as config]
            [gravity.c5-name-resolution.operations :as ops]))

(defn c5-package-record [module]
  {:name (or (get-in module [:metadata :package]) 'gravity/stage0-local)
   :version (or (get-in module [:metadata :package-version]) "0.0.0-stage0")})

(defn c5-binding-id [binding]
  (str "sha256:" (ops/sha256-hex
                  (pr-str (select-keys binding [:name :kind :namespace :package
                                                :visibility :profile-set :target-set
                                                :type-ref :effects :capabilities
                                                :safety :source-span])))))

(defn c5-binding-identity [binding]
  (let [stable (select-keys binding [:name :kind :namespace :package :visibility
                                     :profile-set :target-set :type-ref :effects
                                     :capabilities :safety :source-span :artifact])]
    (assoc stable :binding-id ((ops/op-fn :c5-binding-id c5-binding-id) stable))))

(defn c5-definition-binding [module definition artifact-id]
  ((ops/op-fn :c5-binding-identity c5-binding-identity)
   {:name (:name definition) :kind (:kind definition) :namespace (:module module)
    :package ((ops/op-fn :c5-package-record c5-package-record) module)
    :visibility (:visibility definition) :profile-set #{(:profile module)}
    :target-set #{(:target module)}
    :type-ref (case (:kind definition) :schema :gravity.type/schema
                    :protocol :gravity.type/protocol :macro :gravity.syntax/macro
                    :function :gravity.type/function :gravity.type/value)
    :effects (:latent-effects definition) :capabilities (:required-capabilities definition)
    :safety (:safety definition) :source-span (:source-span definition) :artifact artifact-id}))

(defn c5-special-form-binding [sym module]
  ((ops/op-fn :c5-binding-identity c5-binding-identity)
   {:name sym :kind :special-form :namespace 'gravity.core
    :package {:name 'gravity/core :version "stage0"} :visibility :public
    :profile-set (ops/op-value :known-source-profiles config/known-source-profiles)
    :target-set (ops/op-value :supported-targets config/supported-targets)
    :type-ref :gravity.syntax/special-form :effects #{} :capabilities #{} :safety :safe
    :source-span {:source "gravity.core" :form-index 0} :artifact (:module module)}))

(defn c5-core-binding [sym module]
  ((ops/op-fn :c5-binding-identity c5-binding-identity)
   {:name sym :kind :var :namespace 'gravity.core
    :package {:name 'gravity/core :version "stage0"} :visibility :public
    :profile-set (ops/op-value :known-source-profiles config/known-source-profiles)
    :target-set (ops/op-value :supported-targets config/supported-targets)
    :type-ref :gravity.type/core-var :effects (if (= 'println sym) #{:io/write} #{})
    :capabilities (if (= 'println sym) #{:io/stdout} #{}) :safety :safe
    :source-span {:source "gravity.core" :form-index 0} :artifact (:module module)}))

(defn c5-type-binding [sym module]
  ((ops/op-fn :c5-binding-identity c5-binding-identity)
   {:name sym :kind :type :namespace 'gravity.core
    :package {:name 'gravity/core :version "stage0"} :visibility :public
    :profile-set (ops/op-value :known-source-profiles config/known-source-profiles)
    :target-set (ops/op-value :supported-targets config/supported-targets)
    :type-ref :gravity.type/type :effects #{} :capabilities #{} :safety :safe
    :source-span {:source "gravity.core" :form-index 0} :artifact (:module module)}))

(defn c5-import-binding [module dependency imported-name artifact-id]
  ((ops/op-fn :c5-binding-identity c5-binding-identity)
   {:name imported-name :kind (if (= :import (:kind dependency)) :foreign-var :var)
    :namespace (:module dependency)
    :package {:name (symbol (str (:module dependency))) :version "stage0"}
    :visibility (:visibility dependency)
    :profile-set #{(or (:profile dependency) (:profile module))}
    :target-set #{(:target module)}
    :type-ref (if (= :import (:kind dependency)) :gravity.interop/foreign-value
                  :gravity.type/imported-var)
    :effects (:effects dependency) :capabilities (:capabilities dependency)
    :safety (if (= :import (:kind dependency)) :boundary-checked :safe)
    :source-span (ops/source-span (:source-path module) 0) :artifact artifact-id}))

(defn c5-alias-table [module]
  (mapv (fn [dependency]
          {:alias (:alias dependency) :namespace (:module dependency) :kind (:kind dependency)
           :package {:name (symbol (str (:module dependency))) :version "stage0"}
           :profile (:profile dependency) :target (:target module) :effects (:effects dependency)
           :capabilities (:capabilities dependency) :visibility (:visibility dependency)
           :boundary (or (:boundary dependency) (when (= :core (:profile dependency)) :pure-core))})
        (filter :alias (concat (:requires module) (:imports module)))))

(defn c5-import-export-table [module]
  {:artifact :gravity/c5-import-export-table
   :requires (mapv #(select-keys % [:module :alias :refer :profile :boundary :effects :capabilities :visibility]) (:requires module))
   :foreign-imports (mapv #(select-keys % [:module :alias :refer :profile :boundary :effects :capabilities :visibility]) (:imports module))
   :exports (:exports module) :status :complete})

(defn c5-definition-bindings [module module-artifact c4-artifact]
  (mapv #((ops/op-fn :c5-definition-binding c5-definition-binding) module % (:artifact-id c4-artifact))
        (:definitions module-artifact)))

(defn c5-macro-bindings [module c4-artifact]
  (mapv (fn [entry]
          ((ops/op-fn :c5-binding-identity c5-binding-identity)
           {:name (:macro entry) :kind :macro :namespace (:namespace entry)
            :package ((ops/op-fn :c5-package-record c5-package-record) module)
            :visibility :private :profile-set #{(:profile module)} :target-set #{(:target module)}
            :type-ref :gravity.syntax/macro :effects #{} :capabilities (:capabilities entry)
            :safety :safe :source-span (ops/source-span (:source-path module) 0)
            :artifact (:artifact-id c4-artifact)}))
        (get-in c4-artifact [:macro-environment :macro-vars])))
