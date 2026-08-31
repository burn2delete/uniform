

(defn runtime-service-table
  [input-id]
  {:artifact :gravity/runtime-service-table
   :input-artifact input-id
   :services
   {:startup {:linked #{:minimal-native-startup}
              :generated #{:reset-vector :startup-glue}
              :delegated #{:jvm-main :js-module-load}
              :external #{}
              :forbidden #{}}
    :panic {:linked #{:panic-runtime}
            :generated #{:trap-stub :panic-branch}
            :delegated #{:host-error-adapter}
            :external #{}
            :forbidden #{}}
    :allocator {:linked #{:arena-provider}
                :generated #{:static-allocation-map}
                :delegated #{:host-allocator-adapter}
                :external #{:deployment-allocator/provider}
                :forbidden #{:hidden-allocator}}
    :gc {:linked #{}
         :generated #{}
         :delegated #{:managed-host-gc}
         :external #{}
         :forbidden #{:firmware :kernel :hardware :gpu-device}}
    :scheduler {:linked #{:declared-task-scheduler}
                :generated #{}
                :delegated #{:managed-host-scheduler}
                :external #{:workflow-provider/scheduler}
                :forbidden #{:firmware :hardware}}
    :reflection {:linked #{}
                 :generated #{}
                 :delegated #{:typed-host-adapter}
                 :external #{}
                 :forbidden #{:no-runtime :native-safe-default}}
    :dynamic-eval {:linked #{}
                   :generated #{}
                   :delegated #{:repl-evaluator}
                   :external #{}
                   :forbidden #{:no-runtime :release-artifact}}
    :filesystem {:linked #{}
                 :generated #{}
                 :delegated #{:host-fs-adapter}
                 :external #{:deployment-fs/provider}
                 :forbidden #{:firmware-without-provider}}
    :network {:linked #{}
              :generated #{}
              :delegated #{:host-network-adapter}
              :external #{:deployment-network/provider}
              :forbidden #{:no-runtime}}
    :ffi {:linked #{:ffi-trampoline}
          :generated #{:ffi-marshalling}
          :delegated #{:host-ffi-adapter}
          :external #{}
          :forbidden #{:firmware-safe-default}}
    :model {:linked #{}
            :generated #{}
            :delegated #{:model-provider-adapter}
            :external #{:ai/model-provider}
            :forbidden #{:no-runtime :native-default}}
    :observability {:linked #{:diagnostic-buffer}
                    :generated #{:trace-event-schema}
                    :delegated #{:host-log-adapter}
                    :external #{:observability-sink/provider}
                    :forbidden #{}}}
   :classification-kinds #{:linked :generated :delegated :external
                           :forbidden}
   :status :complete})