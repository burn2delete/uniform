(ns gravity.p15-native-packet-binding.authentication
  (:require [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(defn exact-context? [context sha256-text]
  (and (map? context)
       (= #{:source-path :source-text :source-content-hash :requested-target}
          (set (keys context)))
       (string? (:source-path context))
       (string? (:source-text context))
       (string? (:source-content-hash context))
       (= :c (:requested-target context))
       (= (:source-content-hash context)
          (sha256-text (:source-text context)))))

(defn validate-context! [context exact-context? auth-fail!]
  (let [source-path (when (map? context) (:source-path context))]
    (when-not (exact-context? context)
      (auth-fail! source-path "trusted packet context is not exact"
                  {:missing-fact :exact-trusted-packet-context
                   :required-context-keys
                   [:source-path :source-text :source-content-hash
                    :requested-target]}))
    (when-not (or (str/ends-with? source-path ".gravity")
                  (str/ends-with? source-path ".qst"))
      (auth-fail! source-path "trusted source extension is unsupported"
                  {:observed-extension
                   (second (re-find #"(\.[^./]+)$" source-path))
                   :supported-extensions [".gravity" ".qst"]
                   :missing-fact :gravity-source-extension}))
    context))

(defn validate-envelope! [packet context auth-fail!]
  (when-not (and (map? packet)
                 (= :gravity/target-neutral-stage2-runtime-packet
                    (:kind packet))
                 (= :complete (:status packet))
                 (= :c (:requested-target packet))
                 (= :accepted (get-in packet [:target-eligibility :status]))
                 (= :c (get-in packet
                               [:target-eligibility :requested-target])))
    (auth-fail! (:source-path context)
                "stage2 packet envelope is not eligible for native binding"
                {:observed-kind (:kind packet)
                 :observed-status (:status packet)
                 :observed-target (:requested-target packet)
                 :observed-target-eligibility (:target-eligibility packet)
                 :missing-fact :eligible-target-neutral-stage2-packet}))
  ;; Contextual authentication must precede plan traversal or lowering.
  (when-not (bootstrap/p15-s23-closed-runtime-packet-authentic? packet context)
    (auth-fail! (:source-path context)
                "stage2 packet does not authenticate against trusted source"
                {:missing-fact :authenticated-stage2-packet-and-context}))
  packet)
