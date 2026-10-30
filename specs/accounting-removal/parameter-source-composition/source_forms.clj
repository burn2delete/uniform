(ns source-forms)
(defn forms [text]
  (let [eof (Object.)
        starts (vec (cons 0 (map inc (keep-indexed #(when (= %2 \newline) %1) text))))
        position (fn [r] (+ (get starts (dec (.getLineNumber r)) (count text)) (dec (.getColumnNumber r))))]
    (with-open [r (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. text))]
      (binding [*read-eval* false]
        (loop [out []]
          (let [value (read {:eof eof} r)]
            (if (identical? eof value) out
              (let [line (:line (meta value))
                    start (when line (get starts (dec line))) end (position r)]
                (recur (conj out {:form value :start start :end end}))))))))))
(defn named [entries name] (first (filter #(and (seq? (:form %)) (= name (second (:form %)))) entries)))
