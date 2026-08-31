

(defn p18-shell-single-quote
  [text]
  (str "'" (str/replace (str text) "'" "'\"'\"'") "'"))