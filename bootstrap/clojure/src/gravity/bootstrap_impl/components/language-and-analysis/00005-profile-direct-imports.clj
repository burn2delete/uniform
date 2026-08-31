

(def profile-direct-imports
  {:core #{:core}
   :meta #{:core :meta}
   :hosted #{:core :hosted}
   :native #{:core :native}
   :firmware #{:core :firmware}
   :kernel #{:core :kernel}
   :hardware #{:core :hardware}
   :distributed #{:core :distributed}
   :ai #{:core :distributed :ai}
   :gpu #{:core :gpu}
   :formal #{:core :formal}})