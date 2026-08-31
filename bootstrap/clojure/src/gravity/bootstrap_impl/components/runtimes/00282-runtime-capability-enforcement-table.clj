

(defn runtime-capability-enforcement-table
  [input-id]
  {:artifact :gravity/runtime-capability-enforcement-table
   :input-artifact input-id
   :checks-do-not-grant-authority? true
   :rows
   [{:effect :filesystem/read
     :capability :fs/read
     :family :managed
     :status :runtime-checked
     :hook :host-fs-read-cap}
    {:effect :memory/mmio
     :capability :hardware/mmio
     :family :no-runtime
     :status :statically-declared-target-authority
     :hook :mmio-accessor-generation}
    {:effect :ai/model-call
     :capability :model/call
     :family :ai
     :status :runtime-checked
     :hook :model-provider-cap}
    {:effect :ffi/call
     :capability :ffi/c
     :family :ffi
     :status :runtime-checked
     :hook :ffi-boundary-cap}
    {:effect :runtime/observability
     :capability :observability/write
     :family :observability
     :status :runtime-checked
     :hook :observability-sink-cap}]
   :denied-ambient-authority #{:filesystem/read :network/http
                               :database/write :shell/exec :secrets/read
                               :ai/model-call :tool/call :ffi/call
                               :memory/raw}
   :status :complete})

(defn runtime-package-permission-record
  [input-id]
  {:artifact :gravity/runtime-package-permission-record
   :input-artifact input-id
   :package :gravity/runtime-stage0
   :effects {:requests #{}
             :denies #{:shell/exec :secrets/read :filesystem/write
                       :network/http :database/write :ai/model-call}}
   :capabilities {:requests #{}
                  :denies #{:shell/exec :secret/read :fs/write
                            :http/client :db/write :model/call}
                  :deployment-grants #{}}
   :runtime-handles {}
   :deployment-grants-separate? true
   :dependency-grants-authority? false
   :ambient-authority-rejected? true
   :status :complete})