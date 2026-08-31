(ns gravity.p15-native-packet-binding.wire
  (:require [clojure.string :as str]))

(defn build-wire!
  [source-path source-text runtime-rule-sha lowered
   {:keys [instruction-limit stack-limit output-limit value-limit packet-limit
           utf8-bytes hex-encode sha256-bytes-hex bounds-fail!]}]
  (let [instructions (conj (:instructions lowered) "halt")
        instruction-count (count instructions)
        payload (str (str/join "\n" instructions) "\n")
        payload-bytes (utf8-bytes payload)
        stdout-bytes (utf8-bytes (:stdout lowered))
        source-path-bytes (utf8-bytes source-path)]
    (when (> instruction-count instruction-limit)
      (bounds-fail! source-path "native instruction count exceeds bound"
                    {:observed-instructions instruction-count
                     :maximum-instructions instruction-limit
                     :missing-fact :bounded-native-instruction-count}))
    (when (> (:maximum-relative-depth lowered) stack-limit)
      (bounds-fail! source-path "native value stack exceeds bound"
                    {:observed-stack-depth (:maximum-relative-depth lowered)
                     :maximum-stack-values stack-limit
                     :missing-fact :bounded-native-value-stack}))
    (when (> (alength stdout-bytes) output-limit)
      (bounds-fail! source-path "native stdout exceeds bound"
                    {:observed-output-bytes (alength stdout-bytes)
                     :maximum-output-bytes output-limit
                     :missing-fact :bounded-native-output}))
    (when (or (zero? (alength source-path-bytes))
              (> (alength source-path-bytes) value-limit)
              (not (re-matches #"[A-Za-z0-9/._-]+" source-path)))
      (bounds-fail! source-path "source path is not representable by native wire"
                    {:observed-source-path-bytes (alength source-path-bytes)
                     :maximum-source-path-bytes value-limit
                     :missing-fact :canonical-native-source-path}))
    (let [source-sha (sha256-bytes-hex (utf8-bytes source-text))
          payload-sha (sha256-bytes-hex payload-bytes)
          text (str "gravity-native-runtime-v1\n"
                    "rule-sha256 " runtime-rule-sha "\n"
                    "source-path-hex " (hex-encode source-path-bytes) "\n"
                    "source-sha256 " source-sha "\n"
                    "payload-sha256 " payload-sha "\n"
                    "instruction-count " instruction-count "\n"
                    "--\n" payload)
          bytes (utf8-bytes text)]
      (when (> (alength bytes) packet-limit)
        (bounds-fail! source-path "native packet wire exceeds bound"
                      {:observed-packet-bytes (alength bytes)
                       :maximum-packet-bytes packet-limit
                       :missing-fact :bounded-native-packet}))
      {:format "gravity-native-runtime-v1"
       :text text
       :bytes bytes
       :content-hash (str "sha256:" (sha256-bytes-hex bytes))
       :rule-sha256 runtime-rule-sha
       :source-path-hex (hex-encode source-path-bytes)
       :source-sha256 source-sha
       :payload payload
       :payload-sha256 payload-sha
       :instruction-count instruction-count
       :packet-bytes (alength bytes)})))
