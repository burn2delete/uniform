(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-evaluate-decoded-ast [ast locals stack]
  (loop [remaining ast locals locals stack stack]
    (if (empty? remaining)
      {:locals locals :stack stack}
      (let [node (first remaining)]
        (case (:op node)
          :i32.const
          (recur (rest remaining) locals (conj stack (:value node)))
          :local.get
          (recur (rest remaining) locals
                 (conj stack (get locals (:index node))))
          :local.set
          (recur (rest remaining)
                 (assoc locals (:index node) (peek stack)) (pop stack))
          :i32.eq
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (= left right) 1 0))))
          :i32.lt-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (< left right) 1 0))))
          :i32.le-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (<= left right) 1 0))))
          :i32.gt-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (> left right) 1 0))))
          :i32.ge-s
          (let [right (peek stack) stack (pop stack)
                left (peek stack) stack (pop stack)]
            (recur (rest remaining) locals
                   (conj stack (if (>= left right) 1 0))))
          :if-i32
          (let [condition (peek stack)
                branch (if (zero? condition) (:else node) (:then node))
                result (p15-s23-b4-wasm-evaluate-decoded-ast
                        branch locals (pop stack))]
            (recur (rest remaining) (:locals result) (:stack result))))))))

(defn p15-s23-b4-wasm-flatten-decoded-ast [ast]
  (vec
   (mapcat (fn [node]
             (if (= :if-i32 (:op node))
               (concat (p15-s23-b4-wasm-flatten-decoded-ast (:then node))
                       (p15-s23-b4-wasm-flatten-decoded-ast (:else node))
                       [node])
               [node]))
           ast))))
