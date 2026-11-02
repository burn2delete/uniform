(ns gravity.build
  "Two native Stage0 metadata checks; no build execution or admission."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  {:namespace 'gravity.build
   :contract-boundary :stage0-build-metadata-checks
   :governing-contracts '[D1 D2 D3 D8 D9 PKG1 PKG2 PKG3 PKG6 PKG7 PKG10 PKG11 C1 C15 C16 BOOT8 TEST2]
   :public-api {'check-graph-effects! {:arglists '([fail-var module build-graph]) :admission :none}
                'check-offline-recipe! {:arglists '([fail-var module reproducible-build-recipe]) :admission :none}}
   :artifact-inputs [:trusted-native-module :supplied-build-graph :supplied-recipe :diagnostic-var]
   :artifact-outputs [:nil-on-accepted-metadata :unchanged-pkg2001-or-pkg7003-failure]
   :diagnostic-ownership {:decides ["PKG2001" "PKG7003"]
                          :construction :explicit-existing-owner-var
                          :unchanged [:message :source-span :subject :missing-fields :stage :severity]}
   :dependency-direction {:requires ['clojure.set] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release
                                    'gravity.build.invocation 'gravity.build.declared-inputs]}
   :supported [:declared-node-effect-subset :disabled-recipe-network]
   :unsupported [:general-build-validation :input-authentication :capability-grants :build-execution
                 :dependency-resolution :cache-reuse :reproducibility-proof :signing :sbom
                 :release :migration :self-hosting :seed-retirement]
   :authenticates-inputs? false :performs-io? false :issues-certificates? false
   :test-owner 'gravity.build.pipeline-test :bootstrap-hosted? true
   :clojure-seed-boundary? true :self-hosted? false})

(defn check-graph-effects!
  [fail-var module build-graph]
  (when-not (every? #(set/subset? (:effects %)
                                  (:declared-effects build-graph))
                    (:nodes build-graph))
    ((deref fail-var)
     "PKG2001" module build-graph
     {:missing-fields [:declared-effects]})))

(defn check-offline-recipe!
  [fail-var module reproducible-build-recipe]
  (when-not (= :disabled
               (get-in reproducible-build-recipe
                       [:environment :network]))
    ((deref fail-var)
     "PKG7003" module reproducible-build-recipe
     {:missing-fields [:controlled-network]})))

(doseq [operation [#'check-graph-effects! #'check-offline-recipe!]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
