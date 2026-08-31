(ns gravity.module-analysis.dependencies
  (:require [clojure.string :as str]))

(defn parse-options
  [{:keys [fail! source-span]} source-path entry option-items]
  (when-not (even? (count option-items))
    (fail! "L3-UNKNOWN-ALIAS"
           "namespace dependency options must be key/value pairs"
           {:source-span (source-span source-path 0)
            :entry entry
            :remediation "Use dependency entries such as [gravity.io :as io :profile :hosted]."}))
  (loop [items option-items options {}]
    (if-let [[k v & more] (seq items)]
      (do
        (when-not (keyword? k)
          (fail! "L3-UNKNOWN-ALIAS"
                 "namespace dependency option keys must be keywords"
                 {:source-span (source-span source-path 0)
                  :entry entry
                  :option k
                  :remediation "Use keyword options such as :as, :refer, :profile, :effects, or :boundary."}))
        (recur more (assoc options k v)))
      options)))

(defn parse-dependency-entry
  [{:keys [fail! source-span parse-options]} source-path kind entry]
  (when-not (and (vector? entry) (symbol? (first entry)))
    (fail! "L3-UNKNOWN-ALIAS"
           "namespace dependency entry must start with a module symbol"
           {:source-span (source-span source-path 0)
            :entry entry
            :remediation "Use entries such as [gravity.io :as io]."}))
  (let [[module & option-items] entry
        options (parse-options source-path entry option-items)
        alias (:as options)
        refer (:refer options)
        effects (or (:effects options) #{})
        capabilities (or (:capabilities options) #{})]
    (when (and alias (not (symbol? alias)))
      (fail! "L3-UNKNOWN-ALIAS" "namespace alias must be a symbol"
             {:source-span (source-span source-path 0)
              :entry entry :alias alias
              :remediation "Use :as with a symbolic alias."}))
    (when (= :all refer)
      (fail! "L3-AMBIGUOUS-NAME"
             "wildcard imports are rejected for stable stage0 modules"
             {:source-span (source-span source-path 0)
              :entry entry
              :remediation "Import explicit public symbols instead of :refer :all."}))
    (when (some #(str/starts-with? (name %) "private-")
                (if (vector? refer) refer []))
      (fail! "L3-PRIVATE-IMPORT"
             "private definitions cannot be imported as public API"
             {:source-span (source-span source-path 0)
              :entry entry :refer refer
              :remediation "Export a public facade or remove the private import."}))
    {:kind kind :module module :alias alias
     :refer (cond (nil? refer) [] (vector? refer) refer :else [refer])
     :profile (:profile options) :boundary (:boundary options)
     :edge (:edge options) :facade (:facade options)
     :evidence (or (:evidence options) #{}) :artifact (:artifact options)
     :artifact-schema (:artifact-schema options) :runtime (:runtime options)
     :memory (:memory options) :generated? (boolean (:generated? options))
     :matrix-override (:matrix-override options)
     :producer-effects (or (:producer-effects options) #{})
     :producer-capabilities (or (:producer-capabilities options) #{})
     :safety-evidence (or (:safety-evidence options) #{})
     :provider (:provider options) :effects effects :capabilities capabilities
     :visibility (or (:visibility options) :public)}))

(defn parse-dependencies
  [{:keys [parse-dependency-entry]} source-path kind entries]
  (mapv #(parse-dependency-entry source-path kind %) entries))
