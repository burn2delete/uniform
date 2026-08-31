

(defn jvm-backend-file-artifact
  ([path] (jvm-backend-file-artifact path {}))
  ([path options]
   (jvm-backend-source-artifact
    path (read-gravity-source-text path) options)))