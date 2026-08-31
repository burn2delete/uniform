(ns gravity.p15-public-native-admission.contract
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]))

(def namespace-contract
  {:namespace 'gravity.p15-public-native-admission
   :contract-boundary :p15-w4-dependency-authentication-only
   :public-api
   {'public-native-admission-contract
    {:arglists '([])
     :returns :exact-versioned-consumer-contract}
    'default-public-native-admission
    {:arglists '([])
     :returns :incomplete-fail-closed-decision}
    'validate-public-native-admission
    {:arglists '([request])
     :returns :structured-admission-decision}
    'public-native-admission?
    {:arglists '([request])
     :returns :boolean}
    'verified-public-route-handoff?
    {:arglists '([route])
     :returns :boolean}}
   :artifact-inputs [:parsed-w1-observation :parsed-w2-observation
                     :parsed-w3-observation :independent-raw-content-hashes
                     :independent-replay-artifact-ids
                     :independent-replay-raw-content-hashes
                     :synthetic-hostile-replay-identities]
   :artifact-outputs [:negative-only-dependency-decision]
   :ownership
   {:owns [:exact-w1-w2-w3-consumer-contract
           :fail-closed-predicate
           :cross-workstream-identity-binding
           :checkout-root-id-derivation]
    :does-not-own [:artifact-reads :raw-content-hash-computation
                   :replay-semantic-id-computation :path-admission
                   :process-launch :public-route-selection :seed-retirement
                   :self-hosting :release-claims]}
   :dependency-direction
   {:requires ['clojure.core 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics
              'java.io 'java.nio 'java.lang.ProcessBuilder]}
   :bootstrap-hosted? true
   :public? false
   :public-route? false
   :clojure-seed-boundary? true
   :self-hosted? false
   :release? false})

(def public-native-admission-contract
  "The exact W4 consumer request contract.

  `:pins` and `:observations` carry exact synthetic replay and checkout-root
  identities around an unchanged producer handoff envelope.  Producer payloads
  bind implementation A/tree; external fields bind payload-containing B/tree,
  so no payload embeds B or a later C identity.  Replay-owner policies remain
  explicitly unfrozen.  This v1 contract performs no replay reads, accepts no
  reader or verifier callback, and is restricted to hostile negative inputs.
  It cannot authenticate dependencies or authorize source, output, or process
  I/O."
  {:artifact request-artifact
   :schema-version request-schema
   :pins
   {:keys pin-keys
    :workstreams producer-order
    :producer-policies producer-policies
    :identity-separation
    {:producer-payload [:implementation-commit :implementation-tree]
     :external-payload-containing
     [:payload-containing-commit :payload-containing-tree]
     :requires-distinct? true
     :implementation-a-in-payload? true
     :payload-containing-b-in-payload? false
     :later-c-in-payload? false}
    :replay-observation
    {:fields [:replay-artifact-path :replay-artifact-kind
              :replay-schema :replay-schema-version :replay-artifact-id
              :replay-raw-content-hash :checkout-root-id
              :checkout-root-commit :checkout-root-tree]
     :synthetic-hostile-only? true
     :replay-io-performed? false
     :producer-raw-content-hash-equality-required? false
     :semantic-and-raw-roles-distinct? true
     :semantic-and-raw-value-inequality-required? false
     :verifier-binding-defensive-only? true}
    :path-policy (into {} (map (fn [[workstream policy]]
                                 [workstream (:artifact-path policy)])
                               producer-policies))}
   :observations
   {:keys observation-keys
    :replay-fields
    [:replay-artifact-path :replay-artifact-kind
     :replay-schema :replay-schema-version :replay-artifact-id
     :replay-raw-content-hash :checkout-root-id
     :checkout-root-commit :checkout-root-tree]
    :w1-json-key-serialization w1-json-key-serialization
    :consumer-handoff-keys consumer-handoff-keys
    :verifier-keys verifier-keys
    :review-keys review-keys
    :claims-keys claims-keys
    :binding-keys binding-key-sets
    :nested-binding-keys
    {:identity-binding-method identity-binding-keys
     :os-gate os-gate-keys
     :process-tree-containment process-tree-containment-keys
     :w1-provenance-edges w1-provenance-edges-keys
     :abi abi-keys
     :w2-effects w2-effects-keys
     :w2-capabilities w2-capabilities-keys}}
   :replay-owner-policy-exact-keys replay-policy-keys
   :replay-owner-policies replay-policies
   :replay-diagnostic-precedence replay-diagnostic-order
   :future-request-v2 future-request-v2
   :source-extensions source-extensions
   :payload-containing-commit-registry
   w6-payload-containing-commit-registry
   :decision
   {:artifact admission-artifact
    :schema-version admission-schema
    :status :negative-only-v1
    :success :unreachable
    :current-gates [:w1-replay-contract-unfrozen
                    :w2-replay-contract-unfrozen
                    :w3-native-replay-schema-unfrozen]
    :public-result-cardinality {:diagnostics 1 :rejections 1}
    :nil-request-terminal :missing-reviewed-w1-w2-w3-observations
    :public-predicate-nonmap-uses-default? true
    :request-shape-precedence :first-existing-shape-issue
    :replay-structure-precedence :first-match-exact-replay-diagnostic-order
    :valid-unfrozen-owner-terminal :w1-replay-contract-unfrozen
    :later-owner-blockers-emitted? false
    :legacy-validation-after-terminal? false
    :legacy-diagnostics-exposed? false
    :replay-io? false
    :route :separate-reviewed-public-route-artifact}})

(def default-public-native-admission
  "Fail-closed result before reviewed W1/W2/W3 evidence is available."
  {:artifact admission-artifact
   :schema-version admission-schema
   :status :incomplete
   :decision :dependency-interface-incomplete
   :id p18-id
   :diagnostic p18-id
   :diagnostics [{:id p18-id
                  :code :missing-reviewed-w1-w2-w3-observations
                  :path [:observations]}]
   :rejections [{:id p18-id
                 :code :missing-reviewed-w1-w2-w3-observations
                 :path [:observations]}]
   :dependencies-authenticated? false
   :dependency-interface? false
   :dependencies? false
   :bounded-native-route-admitted? false
   :io-authorized? false
   :public-route? false
   :clojure-seed-boundary? true
   :self-hosted? false
   :release? false})
