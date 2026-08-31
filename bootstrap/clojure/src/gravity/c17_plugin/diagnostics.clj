(ns gravity.c17-plugin.diagnostics
  "C17 source overrides, failures, and deterministic diagnostic projection."
  (:require [clojure.string :as str]))

(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c17-plugin])
      (get-in module [:metadata :compiler :verification])
      {}))

(defn fail! [config id source-path subject extra]
  ((:fail config)
   id
   (get (:diagnostic-messages config) id
        "compiler plugin/pass API validation failed")
   (merge {:source-span (or (:source-span subject)
                            ((:source-span config) source-path 0))
           :diagnostic-family :compiler-plugin-api
           :stage (or (:stage subject) :c17-compiler-plugin)
           :plugin-id (:plugin-id subject)
           :package-id (:package-id subject)
           :version (:version subject)
           :pass-id (:pass-id subject)
           :manifest-entry (:manifest-entry subject)
           :requested-capability (:requested-capability subject)
           :trust-level (:trust-level subject)
           :compiler-api-version (:compiler-api-version subject)
           :artifact-id (:artifact-id subject)
           :remediation "Load plugins through a versioned manifest, explicit trust grant, scoped compiler capabilities, sandboxed build effects, verifier-checked outputs, cache-key integration, and C17 diagnostics."}
          extra)))

(defn validate-source-overrides! [config source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get (:override-diagnostics config) fail-kind)]
      (when (contains? (set (:diagnostic-ids config)) id)
        ((:plugin-fail! config)
         id source-path
         {:stage subject-kind
          :plugin-id 'gravity.plugins.stage0/loop-fuser
          :package-id 'gravity/stage0-loop-fuser
          :version "0.1.0"
          :pass-id subject-kind
          :manifest-entry fail-kind
          :requested-capability :compiler/ir-transform
          :trust-level :sandboxed
          :compiler-api-version "1"
          :artifact-id (str "c17-plugin-artifact-" (name fail-kind))}
         {:missing-fields [fail-kind]})))))

(defn diagnostic-stream [config source-path plugin-manifest input-id]
  {:artifact :gravity/c17-plugin-diagnostic-stream
   :stage :c17-compiler-plugin
   :input-artifact input-id
   :ordering-key [:rule :plugin :pass]
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity (if (= id "C17-TRUST") :error :warning)
            :stage :c17-compiler-plugin
            :message-key (keyword "plugin"
                                  (str/lower-case
                                   (str/replace id #"_" "-")))
            :primary {:span ((:source-span config) source-path index)
                      :syntax-id (str "c17-plugin-syntax-" index)
                      :artifact input-id}
            :plugin-id (:plugin plugin-manifest)
            :package-id (get-in plugin-manifest [:package :name])
            :version (get-in plugin-manifest [:package :version])
            :pass-id (get (:passes plugin-manifest) 0)
            :manifest-entry (keyword (str/lower-case (subs id 4)))
            :requested-capability :compiler/ir-transform
            :trust-level (:trust plugin-manifest)
            :compiler-api-version (:api-version plugin-manifest)
            :source-or-artifact-id input-id
            :facts {:manifest-hash (:manifest-hash plugin-manifest)
                    :capability-scopes
                    (get-in plugin-manifest
                            [:capability-scopes :compiler/ir-transform])
                    :rule id}
            :remediation [{:kind :repair-plugin-manifest}
                          {:kind :rerun-plugin-verifier}]
            :redactions []
            :ordering-key [id (:plugin plugin-manifest)
                           (get (:passes plugin-manifest) 0)]})
         (:diagnostic-ids config)
         (range))
   :status :complete})
