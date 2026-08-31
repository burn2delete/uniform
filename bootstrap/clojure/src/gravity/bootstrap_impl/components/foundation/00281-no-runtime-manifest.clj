

(defn no-runtime-manifest
  [module input-id]
  {:artifact :gravity/no-runtime-manifest
   :input-artifact input-id
   :runtime :none
   :profile (or (:profile module) :firmware)
   :target {:backend :c :platform :bare-metal}
   :startup :reset-handler
   :memory {:static :stage0-static-map
            :stack :stage0-stack-bound
            :heap :none}
   :generated-support #{:bounds-checks :numeric-checks :panic-trap-stub
                        :resource-cleanup :startup-glue
                        :static-dispatch-table :mmio-accessor}
   :linked #{}
   :delegated #{}
   :external #{:target/mmio}
   :forbidden #{:gc :scheduler :dynamic-eval :reflection :host-io
                :classloading :managed-exceptions :hidden-allocator
                :dynamic-loader :repl :host-exceptions
                :runtime-dispatch}
   :hidden-runtime-services []
   :startup-reset-record
   {:artifact :gravity/no-runtime-startup-reset-record
    :reset-vector :reset-handler
    :entry-symbol "gravity_reset"
    :initialization-order [:copy-data :zero-bss :init-stack
                           :init-static-mmio :call-main]
    :interrupt-table [{:interrupt :timer :handler :timer_tick
                       :capability :interrupt/register}
                      {:interrupt :uart :handler :uart_rx
                       :capability :hardware/mmio}]
    :calling-convention :c-freestanding
    :handoff :user-main
    :status :complete}
   :memory-map
   {:artifact :gravity/no-runtime-memory-map
    :regions [{:name :flash :start "0x00000000" :size 65536
               :permissions #{:read :execute}}
              {:name :sram :start "0x20000000" :size 16384
               :permissions #{:read :write}}
              {:name :mmio :start "0x40000000" :size 4096
               :permissions #{:read :write} :capability :hardware/mmio}]
    :alignment 4
    :status :complete}
   :section-layout
   {:artifact :gravity/no-runtime-section-layout
    :sections [{:name ".text" :region :flash :alignment 4}
               {:name ".rodata" :region :flash :alignment 4}
               {:name ".data" :region :sram :alignment 4}
               {:name ".bss" :region :sram :alignment 4}
               {:name ".stack" :region :sram :alignment 8}]
    :status :complete}
   :stack-bound-report
   {:artifact :gravity/no-runtime-stack-bound-report
    :stack-bytes 2048
    :method :conservative-static-bound
    :recursion :rejected
    :status :bounded}
   :static-allocation-report
   {:artifact :gravity/no-runtime-static-allocation-report
    :static-objects [{:symbol "gravity_uart_state" :bytes 16
                      :region :sram}
                     {:symbol "gravity_counter" :bytes 8
                      :region :sram}]
    :heap :none
    :status :complete}
   :failure-policy
   {:artifact :gravity/no-runtime-failure-policy
    :panic :trap
    :trap-symbol "gravity_trap"
    :result-errors :explicit-result
    :reset-policy :explicit-reset-record
    :hardware-signal :explicit-interrupt-record
    :status :complete}
   :forbidden-service-report
   {:artifact :gravity/no-runtime-forbidden-service-report
    :forbidden-services #{:gc :scheduler :dynamic-eval :reflection
                          :host-io :classloading :managed-exceptions
                          :hidden-allocator :dynamic-loader :repl
                          :host-exceptions :runtime-dispatch}
    :observed-hidden-services []
    :runtime-dependencies-absent? true
    :status :passed}
   :proof-record
   {:artifact :gravity/no-runtime-proof-record
    :boundedness :proved-by-static-bound
    :initialization :proved-by-startup-order
    :check-elision :not-elided-without-proof
    :heap-allocation :rejected
    :dynamic-dispatch :lowered-or-rejected
    :boot-smoke-evidence :stage0-structural-simulation
    :status :complete}
   :status :complete})