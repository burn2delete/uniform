

(defn p18-t02-write-edn!
  [path value]
  (p18-ensure-dir! (.getParent (java.io.File. path)))
  (spit path (str (pr-str value) "\n")))