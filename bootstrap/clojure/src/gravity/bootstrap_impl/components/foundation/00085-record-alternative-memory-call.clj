

(defn record-alternative-memory-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [kind (:alternative-memory-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :alternative-memory-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :provider
        (record-checker! checker :alternative-memory-provider-declarations
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :version (or (dispatch-arg-value args 1)
                                              "fixture-1")
                                 :kind :memory-system
                                 :profiles (or (dispatch-arg-value args 2)
                                               #{(:profile @ctx)})
                                 :targets (or (dispatch-arg-value args 3)
                                              #{(:target @ctx)})
                                 :allocation-families (or (dispatch-arg-value args 4)
                                                          #{:alloc/arena})
                                 :allocation-policy (or (dispatch-arg-value args 5)
                                                        {:hidden false
                                                         :bounded true})
                                 :contracts (or (dispatch-arg-value args 6)
                                                #{'gravity.contracts/MemorySafety})
                                 :proof-artifacts (or (dispatch-arg-value args 7)
                                                      #{})
                                 :conformance-suite (or (dispatch-arg-value args 8)
                                                        :gravity.conformance/memory)
                                 :deterministic-selection? true
                                 :capability-scope :memory
                                 :safe-code-guarantee :no-undefined-behavior}))

        :allocation-strategy
        (record-checker! checker :alternative-memory-allocation-strategies
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :family (or (dispatch-arg-value args 1)
                                             :alloc/arena)
                                 :hidden? (true? (dispatch-arg-value args 2))
                                 :bounded? (not (false? (dispatch-arg-value args 3)))
                                 :can-fail? (true? (dispatch-arg-value args 4))
                                 :may-block? (true? (dispatch-arg-value args 5))
                                 :profile-policy (or (dispatch-arg-value args 6)
                                                     :explicit-allocation)
                                 :allocation-effect-recorded? true
                                 :provider-recorded? true}))

        :lifetime-facts
        (record-checker! checker :alternative-memory-lifetime-facts
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :region (dispatch-arg-value args 1)
                                 :fact-families (or (dispatch-arg-value args 2)
                                                   #{:lifetime :aliasing
                                                     :ownership :region
                                                     :escape
                                                     :initialization})
                                 :escape (or (dispatch-arg-value args 3)
                                             :no-escape)
                                 :aliasing :unique-or-shared-immutable
                                 :borrow-valid-until :scope-end
                                 :serialized? true}))

        :unsafe-boundary-audit
        (record-checker! checker :alternative-memory-unsafe-boundary-audits
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :safe-wrapper (dispatch-arg-value args 1)
                                 :unsafe-op (dispatch-arg-value args 2)
                                 :invariant (dispatch-arg-value args 3)
                                 :evidence (or (dispatch-arg-value args 4)
                                               #{:alignment-check
                                                 :bounds-check
                                                 :capability-scope})
                                 :unsafe-visible? true
                                 :safe-api-boundary? true
                                 :audit-status :passed}))

        :layout-metadata
        (record-checker! checker :alternative-memory-layout-metadata
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :type-id (dispatch-arg-value args 1)
                                 :alignment (or (dispatch-arg-value args 2)
                                                :align-8)
                                 :size (or (dispatch-arg-value args 3) 0)
                                 :endianness (or (dispatch-arg-value args 4)
                                                 :little-endian)
                                 :layout-facts-serialized? true
                                 :backend-consumable? true}))

        :runtime-check
        (record-checker! checker :alternative-memory-runtime-checks
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :check-kind (or (dispatch-arg-value args 1)
                                                 :bounds)
                                 :status (or (dispatch-arg-value args 2)
                                             :runtime-checked)
                                 :residual-check (or (dispatch-arg-value args 3)
                                                     :retained)
                                 :source-span-recorded? true
                                 :capability-scope :memory}))

        :release-evidence
        (record-checker! checker :alternative-memory-release-evidence
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :resource (dispatch-arg-value args 1)
                                 :release-mode (or (dispatch-arg-value args 2)
                                                   :exactly-once)
                                 :leak-status (or (dispatch-arg-value args 3)
                                                  :no-leak)
                                 :double-release-status :rejected
                                 :release-proof :recorded}))

        :device-map
        (record-checker! checker :alternative-memory-device-maps
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :address-space (or (dispatch-arg-value args 1)
                                                    :device/global)
                                 :synchronization (or (dispatch-arg-value args 2)
                                                      :host-device-sync)
                                 :mmio-map (or (dispatch-arg-value args 3)
                                               :device/register-map)
                                 :width :u32
                                 :alignment :u32
                                 :volatile-semantics :preserved
                                 :ordering :declared
                                 :capability-scope :hardware/mmio}))

        :ffi-allocator
        (record-checker! checker :alternative-memory-ffi-allocator-records
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :foreign-library (dispatch-arg-value args 1)
                                 :allocator (dispatch-arg-value args 2)
                                 :release (dispatch-arg-value args 3)
                                 :compatibility (or (dispatch-arg-value args 4)
                                                    :compatible)
                                 :ownership-transfer :recorded
                                 :nullability :recorded
                                 :lifetime :recorded
                                 :thread-affinity :recorded}))

        :conformance-report
        (record-checker! checker :alternative-memory-conformance-reports
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :suite (or (dispatch-arg-value args 1)
                                            :gravity.conformance/memory)
                                 :status (or (dispatch-arg-value args 2)
                                             :passed)
                                 :covered-tests (or (dispatch-arg-value args 3)
                                                    #{:leak :double-release
                                                      :bounds :uninit
                                                      :alias
                                                      :device-sync
                                                      :mmio-width})
                                 :positive-fixtures :passed
                                 :negative-fixtures :passed}))

        :safety-classification
        (record-checker! checker :alternative-memory-safety-classifications
                         (merge record
                                {:provider-id (dispatch-arg-value args 0)
                                 :operation (dispatch-arg-value args 1)
                                 :outcomes (or (dispatch-arg-value args 2)
                                               #{:proven-safe
                                                 :runtime-checked
                                                 :rejected
                                                 :unsafe-island})
                                 :evidence-id (dispatch-arg-value args 3)
                                 :safe-code-guarantee :no-undefined-behavior}))

        nil)
      record)))