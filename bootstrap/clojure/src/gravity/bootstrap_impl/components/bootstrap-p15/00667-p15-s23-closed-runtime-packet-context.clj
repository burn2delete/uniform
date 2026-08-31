

(defn p15-s23-closed-runtime-packet-context
  [source-path source-text requested-target]
  {:source-path source-path
   :source-text source-text
   :source-content-hash (str "sha256:" (sha256-hex source-text))
   :requested-target requested-target})