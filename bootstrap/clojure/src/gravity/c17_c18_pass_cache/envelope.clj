(ns gravity.c17-c18-pass-cache.envelope
  "Bounded C17/C18 cache envelope encoding and decoding."
  (:require [gravity.pass-cache.envelope-codec :as codec]
            [gravity.c17-c18-pass-cache.validation :as validation]))

(def ^:private maximum-envelope-characters (* 8 1024 1024))

(def ^:private envelope-fields
  #{:artifact :schema-version :stage :artifact-id :payload-edn})

(defn- codec-options
  []
  {:artifact-tag :gravity/c17-c18-pass-cache-envelope
   :envelope-fields envelope-fields
   :maximum-envelope-characters maximum-envelope-characters
   :require-sha256! validation/require-sha256!
   :sha256-id? validation/sha256-id?
   :fail! validation/fail!
   :label "C17/C18"})

(defn encode!
  [stage artifact operations]
  (codec/encode! stage artifact operations (codec-options)))

(defn decode!
  [stage envelope validate operations]
  (codec/decode! stage envelope validate operations (codec-options)))
