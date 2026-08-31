(ns gravity.c9-ownership-checker.lifecycles
  "Lifetime, escape, region, and arena projections for the hosted C9 facade.")

(defn lifetime-interval-map [module]
  {:artifact :gravity/c9-lifetime-interval-map :module (:module module)
   :intervals
   (sorted-map
    "lt-lexical" {:kind :lexical :start :function-entry :end :function-exit
                  :owner "owner-1" :allowed-escapes #{} :invalidates [:scope-exit]}
    "lt-borrow-read" {:kind :borrow :start :borrow-enter :end :borrow-exit
                      :owner "owner-2" :allowed-escapes #{}
                      :invalidates [:owner-move :owner-consume]}
    "lt-borrow-write" {:kind :mutable-borrow :start :borrow-mut-enter :end :borrow-mut-exit
                       :owner "owner-2" :allowed-escapes #{}
                       :invalidates [:owner-move :owner-consume]}
    "lt-region-outer" {:kind :region :start :region-enter :end :region-exit
                       :owner "region-outer" :allowed-escapes #{:copy :serialize}
                       :invalidates [:region-exit]}
    "lt-arena-generation-1" {:kind :arena-generation :start :arena-reset
                             :end :arena-reset-next :owner "arena-main"
                             :allowed-escapes #{} :invalidates [:arena-reset]}
    "lt-provider-scope" {:kind :provider-scope :start :provider-enter :end :provider-exit
                         :owner "provider-memory" :allowed-escapes #{}
                         :invalidates [:provider-revoke]}
    "lt-structured-task" {:kind :structured-task :start :task-spawn :end :task-join
                          :owner "owner-task" :allowed-escapes #{:ownership-transfer}
                          :invalidates [:task-end]}
    "lt-callback" {:kind :callback :start :callback-enter :end :callback-return
                   :owner "foreign-callback" :allowed-escapes #{}
                   :invalidates [:callback-return]}
    "lt-generated-artifact" {:kind :generated-artifact :start :macro-expansion
                             :end :artifact-serialization :owner "generated-origin"
                             :allowed-escapes #{:artifact-provenance}
                             :invalidates [:compiler-pass-invalidation]})
   :status :complete})

(defn escape-analysis-report [module]
  {:artifact :gravity/c9-escape-analysis-report :module (:module module)
   :legal-escapes [{:destination :function-return :mode :persistent-copy :status :accepted}
                   {:destination :actor-message :mode :ownership-transfer :status :accepted}
                   {:destination :ffi-call :mode :borrowed-for-call-duration :status :accepted}
                   {:destination :generated-artifact :mode :provenance-only :status :accepted}]
   :illegal-escapes-covered-by-diagnostics
   ["C9-BORROW-ESCAPE" "C9-REGION-ESCAPE" "C9-TRANSFER"]
   :status :complete})

(defn region-lifetime-graph [module]
  {:artifact :gravity/c9-region-lifetime-graph :module (:module module)
   :regions
   (sorted-map
    "region-outer" {:scope "scope-outer" :lifetime "lt-region-outer"
                    :allocations ["region-value-config"] :escapes []
                    :provider :region/provider :status :accepted}
    "region-inner" {:scope "scope-inner" :parent "region-outer"
                    :lifetime "lt-region-inner" :allocations ["region-value-scratch"]
                    :escapes [] :provider :region/provider :status :accepted})
   :nested-references [{:from "region-inner" :to "region-outer"
                        :direction :inner-may-borrow-outer :status :accepted}]
   :rejected-escape-families ["C9-REGION-ESCAPE"] :status :complete})

(defn arena-generation-graph [module]
  {:artifact :gravity/c9-arena-generation-graph :module (:module module)
   :arenas
   (sorted-map
    "arena-main" {:provider :arena/provider :thread-affinity :task-local
                  :generations [{:generation "gen-0" :allocations ["arena-node-old"]
                                 :reset-node "core-node-arena-reset" :valid? false}
                                {:generation "gen-1" :allocations ["arena-node-current"]
                                 :valid? true}]
                  :runtime-generation-checks? true :status :accepted})
   :reset-invalidation [{:arena "arena-main" :invalidated-generation "gen-0"
                         :replacement-generation "gen-1" :status :recorded}]
   :rejected-generation-families ["C9-ARENA-GENERATION"] :status :complete})
