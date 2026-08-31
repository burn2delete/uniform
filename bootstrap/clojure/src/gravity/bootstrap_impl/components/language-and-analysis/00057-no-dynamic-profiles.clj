

(def no-dynamic-profiles #{:core :kernel :firmware :hardware})
(def no-scheduler-profiles #{:core :firmware :hardware})

(def profile-denied-effects
  {:core #{:io/write :filesystem/read :filesystem/write :network/http
           :thread/spawn :time/schedule :reflection/use :dynamic/eval}
   :firmware #{:io/write :network/http :thread/spawn :time/schedule
               :reflection/use :dynamic/eval}
   :hardware #{:io/write :network/http :thread/spawn :time/schedule
               :reflection/use :dynamic/eval :time/read}})

(def typed-internal-effects #{:control/recur :state/write :error/throw})