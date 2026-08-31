(ns gravity.p15-public-native-admission.decision
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn validate-cross-bindings
  [observations]
  (let [w1 (get-in observations [:w1 :consumer-handoff :bindings])
        w2 (get-in observations [:w2 :consumer-handoff :bindings])
        w3 (get-in observations [:w3 :consumer-handoff :bindings])
        w1-path [:observations :w1 :consumer-handoff :bindings]
        w2-path [:observations :w2 :consumer-handoff :bindings]
        w3-path [:observations :w3 :consumer-handoff :bindings]]
    (cond-> []
      (not= (:carrier-artifact-id w1)
            (:accepted-carrier-artifact-id w2))
      (conj (issue :w1-to-w2-carrier-artifact-cross-binding-mismatch
                   (conj w2-path :accepted-carrier-artifact-id)))

      (not= (:carrier-content-hash w1)
            (:accepted-carrier-content-hash w2))
      (conj (issue :w1-to-w2-carrier-content-cross-binding-mismatch
                   (conj w2-path :accepted-carrier-content-hash)))

      (not= (:provider-artifact-id w2)
            (:admitted-executable-artifact-id w3))
      (conj (issue :w2-to-w3-provider-artifact-cross-binding-mismatch
                   (conj w3-path :admitted-executable-artifact-id)))

      (not= (:provider-executable-path w2)
            (:admitted-executable-path w3))
      (conj (issue :w2-to-w3-provider-path-cross-binding-mismatch
                   (conj w3-path :admitted-executable-path)))

      (not= (:provider-executable-content-hash w2)
            (:admitted-executable-content-hash w3))
      (conj (issue :w2-to-w3-provider-content-cross-binding-mismatch
                   (conj w3-path :admitted-executable-content-hash)))

      (not (same-identity? (:target w1)
                           (os-gate-target (:os-gate w3))))
      (conj (issue :w1-target-to-w3-os-gate-cross-binding-mismatch
                   (conj w3-path :os-gate :target)))

      (not (same-identity? (:target w1)
                           (get-in w2 [:abi :target])))
      (conj (issue :w1-target-to-w2-abi-cross-binding-mismatch
                   (conj w2-path :abi :target))))))

(defn base-decision
  []
  {:artifact admission-artifact
   :schema-version admission-schema
   :id p18-id
   :diagnostic p18-id
   :bounded-native-route-admitted? false
   :io-authorized? false
   :public-route? false
   :clojure-seed-boundary? true
   :self-hosted? false
   :release? false})

(defn decision-with-issues
  [status decision issues]
  (merge (base-decision)
         {:status status
          :decision decision
          :diagnostics (vec issues)
          :rejections (vec issues)
          :dependencies-authenticated? false
          :dependency-interface? false
          :dependencies? false}))

(defn request-has-no-dependency-evidence?
  [request]
  (and (map? request)
       (or (empty? (keys request))
           (and (contains? request :pins)
                (contains? request :observations)
                (not (contains? request :artifact))
                (not (contains? request :schema-version))
                (not (contains? request :source-extension))))
       (or (not (map? (:pins request)))
           (not (map? (:observations request))))
       (not-any? some? (when (map? (:pins request))
                         (vals (:pins request))))
       (not-any? some? (when (map? (:observations request))
                         (vals (:observations request))))))

(defn validate-request-shape
  [request]
  (cond-> []
    (not (map? request))
    (conj (issue :request-not-a-map []))

    (and (map? request)
         (not (exact-keys? request
                          #{:artifact :schema-version :pins
                            :observations :source-extension})))
    (conj (issue :request-keys-not-exact []))

    (and (map? request)
         (exact-keys? request
                      #{:artifact :schema-version :pins
                        :observations :source-extension})
         (not (same-identity? (:artifact request) request-artifact)))
    (conj (issue :request-artifact-mismatch [:artifact]))

    (and (map? request)
         (exact-keys? request
                      #{:artifact :schema-version :pins
                        :observations :source-extension})
         (not= (:schema-version request) request-schema))
    (conj (issue :request-schema-version-mismatch [:schema-version]))

    (and (map? request)
         (exact-keys? request
                      #{:artifact :schema-version :pins
                        :observations :source-extension})
         (not (contains? source-extensions (:source-extension request))))
    (conj (issue :unsupported-source-extension [:source-extension]))

    (and (map? request) (map? (:pins request))
         (not= (set (keys (:pins request))) (set producer-order)))
    (conj (issue :pin-workstream-keys-not-exact [:pins]))

    (and (map? request) (map? (:observations request))
         (not= (set (keys (:observations request))) (set producer-order)))
    (conj (issue :observation-workstream-keys-not-exact [:observations]))))
