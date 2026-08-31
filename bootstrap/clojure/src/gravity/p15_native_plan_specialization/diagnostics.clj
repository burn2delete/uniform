(ns gravity.p15-native-plan-specialization.diagnostics)

(defn fail!
  [id message source-path facts]
  (throw
   (ex-info message
            (merge {:id id
                    :diagnostic id
                    :severity :error
                    :stage :p15-native-plan-specialization
                    :source-path (or source-path "packet:unknown")
                    :target :c
                    :profile :native
                    :fallback-status :rejected
                    :public-command-route? false
                    :self-hosted? false
                    :release-ready? false
                    :compiler-authored-in-gravity? false
                    :provider-authored-in-gravity? false}
                   facts))))

(defn authentication-fail!
  [source-path message facts]
  (fail! "P15NS001" message source-path
         (merge {:diagnostic-family :authenticated-packet-context
                 :remediation :supply_exact_authenticated_stage2_packet_context}
                facts)))

(defn unsupported-fail!
  [source-path message facts]
  (fail! "P15NS002" message source-path
         (merge {:diagnostic-family :unsupported-native-plan-specialization
                 :remediation :restrict_to_public_runtime_derived_c_subset}
                facts)))

(defn bounds-fail!
  [source-path message facts]
  (fail! "P15NS003" message source-path
         (merge {:diagnostic-family :native-plan-specialization-bound
                 :remediation :reduce_the_authenticated_plan_or_output}
                facts)))

(defn helper-contract-fail!
  [source-path message facts]
  (fail! "P15GCE001" message source-path
         (merge {:diagnostic-family :gravity-c-emitter-helper-contract
                 :remediation :restore_the_pinned_gravity_c_emitter_helper}
                facts)))

(defn helper-rejected-fail!
  [source-path message facts]
  (fail! "P15GCE002" message source-path
         (merge {:diagnostic-family :gravity-c-emitter-authenticated-subset
                 :remediation :restrict_to_printable_ascii_string_println_and_str}
                facts)))
