

(defn p16-std-diagnostics
  [document diagnostic-count]
  (let [number (Integer/parseInt (subs document 3))]
    (mapv #(format "STD%d%03d" number %)
          (range 1 (inc diagnostic-count)))))