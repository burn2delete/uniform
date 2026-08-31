(ns gravity.p15-native-packet-binding.diagnostics)

(defn fail! [id message source-path facts]
  (throw
   (ex-info message
            (merge {:id id
                    :diagnostic id
                    :severity :error
                    :stage :p15-native-packet-binding
                    :source-path (or source-path "packet:unknown")
                    :target :c
                    :profile :native
                    :runtime-provider :gravity.native/libsystem-stdio-v1
                    :fallback-status :rejected
                    :public-command-route? false
                    :self-hosted? false
                    :release-ready? false}
                   facts))))

(defn auth-fail! [source-path message facts]
  (fail! "P15NP001" message source-path
         (merge {:diagnostic-family :packet-authentication
                 :remediation :supply_exact_authenticated_stage2_packet_context}
                facts)))

(defn plan-fail! [source-path message facts]
  (fail! "P15NP002" message source-path
         (merge {:diagnostic-family :unsupported-native-packet-plan
                 :remediation :use_bounded_scalar_str_println_entrypoint}
                facts)))

(defn bounds-fail! [source-path message facts]
  (fail! "P15NP003" message source-path
         (merge {:diagnostic-family :native-packet-wire-or-bound
                 :remediation :reduce_or_repair_native_runtime_packet}
                facts)))
