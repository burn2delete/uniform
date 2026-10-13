
(defn- instruction-children [instruction]
  (let [op (:op instruction)]
    (case op
      :literal (do (ensure! (= #{:op :value} (set (keys instruction)))
                            :malformed-m2-literal) [])
      :quote (do (ensure! (= #{:op :value} (set (keys instruction)))
                          :malformed-m2-quote) [])
      :local (do (ensure! (= #{:op :name} (set (keys instruction)))
                          :malformed-m2-local) [])
      :if (do (ensure! (= #{:op :test :then :else}
                           (set (keys instruction))) :malformed-m2-if)
              [(:test instruction) (:then instruction) (:else instruction)])
      :do (do (ensure! (and (= #{:op :body} (set (keys instruction)))
                            (vector? (:body instruction))) :malformed-m2-do)
              (:body instruction))
      :let (do (ensure! (and (= #{:op :bindings :body}
                                 (set (keys instruction)))
                             (vector? (:bindings instruction))
                             (vector? (:body instruction))
                             (every? #(exact-keys? % #{:name :expr})
                                     (:bindings instruction)))
                        :malformed-m2-let)
               (into (mapv :expr (:bindings instruction)) (:body instruction)))
      :loop (do (ensure! (and (= #{:op :binding-count :bindings :body}
                                  (set (keys instruction)))
                              (integer? (:binding-count instruction))
                              (= (:binding-count instruction)
                                 (count (:bindings instruction)))
                              (vector? (:bindings instruction))
                              (vector? (:body instruction))
                              (every? #(exact-keys? % #{:name :expr})
                                      (:bindings instruction)))
                         :malformed-m2-loop)
                (into (mapv :expr (:bindings instruction)) (:body instruction)))
      :recur
      (do (ensure! (and (= #{:op :args} (set (keys instruction)))
                        (vector? (:args instruction))) :malformed-m2-recur)
          (:args instruction))
      :builtin-call
      (do (ensure!
           (and (vector? (:args instruction))
                (or (= #{:op :function :args} (set (keys instruction)))
                    (= #{:op :function :args :compiler-internal-caller
                         :compiler-internal-provenance}
                       (set (keys instruction))))) :malformed-m2-builtin-call)
          (:args instruction))
      :function-call
      (do (ensure!
           (and (vector? (:args instruction))
                (or (= #{:op :function :args} (set (keys instruction)))
                    (= #{:op :function :args :external-link}
                       (set (keys instruction))))) :malformed-m2-function-call)
          (:args instruction))
      (:vector-literal :set-literal)
      (do (ensure! (and (= #{:op :items} (set (keys instruction)))
                        (vector? (:items instruction)))
                   :malformed-m2-collection)
          (:items instruction))
      :map-literal
      (do (ensure! (and (= #{:op :entries} (set (keys instruction)))
                        (vector? (:entries instruction))
                        (every? #(exact-keys? % #{:key :value})
                                (:entries instruction)))
                   :malformed-m2-map)
          (vec (mapcat (fn [entry] [(:key entry) (:value entry)])
                       (:entries instruction))))
      (reject! :unknown-m2-operation))))

(defn- scan-m2 [m2s]
  (let [function-owner
        (into {} (for [[module-ordinal plan] (map-indexed vector m2s)
                       function (keys (:functions plan))]
                   [function module-ordinal]))
        _ (ensure! (= (reduce + (map #(count (:functions %)) m2s))
                       (count function-owner)) :duplicate-m2-definition)
        records (transient [])
        calls (transient [])
        global-record (volatile! 0)]
    (doseq [[module-ordinal plan] (map-indexed vector m2s)]
      (let [instruction-ordinal (volatile! 0)
            ordered-functions (sort-by str (keys (:functions plan)))]
        (doseq [[function-coordinate caller]
                (map-indexed vector ordered-functions)]
          (let [definition (get-in plan [:functions caller])]
            (ensure! (and (map? definition) (vector? (:instructions definition)))
                     :malformed-m2-definition)
            (letfn [(visit [instruction path]
                      (ensure! (and (map? instruction) (keyword? (:op instruction)))
                               :malformed-m2-instruction)
                      (let [op (:op instruction)
                            callee (when (contains? #{:function-call
                                                     :builtin-call} op)
                                     (:function instruction))
                            kind (case op
                                   :function-call
                                   (if (= module-ordinal
                                          (get function-owner callee))
                                     :local :external)
                                   :builtin-call
                                   (if (contains? (set internal-builtins) callee)
                                     :internal-builtin :builtin)
                                   :not-a-call)
                            provenance? (contains? instruction
                                                   :compiler-internal-provenance)
                            link (get instruction :external-link)
                            own-ordinal @instruction-ordinal]
                        (ensure! (or (not= :function-call op)
                                     (contains? function-owner callee))
                                 :unresolved-m2-function)
                        (ensure! (or (not= :external kind) (map? link))
                                 :missing-m2-external-link)
                        (ensure! (or (not= :local kind) (nil? link))
                                 :unexpected-m2-external-link)
                        (ensure! (= provenance? (= :internal-builtin kind))
                                 :m2-provenance-presence-mismatch)
                        (vswap! instruction-ordinal inc)
                        (conj! records
                               {:record-ordinal @global-record
                                :module-ordinal module-ordinal
                                :instruction-ordinal own-ordinal
                                :caller caller :path path :op op
                                :call-classification kind :callee callee
                                :arity (when callee (count (:args instruction)))
                                :external-link link
                                :provenance-present provenance?
                                :provenance
                                (get instruction :compiler-internal-provenance)})
                        (vswap! global-record inc)
                        (when (contains? #{:local :external :builtin
                                          :internal-builtin} kind)
                          (conj! calls
                                 {:module-ordinal module-ordinal
                                  :instruction-ordinal own-ordinal
                                  :caller caller :kind kind :callee callee
                                  :arity (count (:args instruction))
                                  :external-link link
                                  :provenance
                                  (get instruction
                                       :compiler-internal-provenance)}))
                        (doseq [[child-ordinal child]
                                (map-indexed vector
                                             (instruction-children instruction))]
                          (visit child (conj path child-ordinal)))))]
              (doseq [[top-ordinal instruction]
                      (map-indexed vector (:instructions definition))]
                (visit instruction [function-coordinate top-ordinal])))))))
    {:owner-by-function function-owner
     :records (persistent! records)
     :calls (persistent! calls)}))

(defn- m2-identity-value [value]
  ;; M2 predates Attempt 30 and retains its existing C4-compatible identity.
  ;; Reconstruct that value locally; do not call the producer/compiler
  ;; normalizer whose independence this checker exists to test.
  (cond
    (map? value)
    (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
          (map (fn [[key item]] [(m2-identity-value key)
                                 (m2-identity-value item)])) value)
    (set? value) (vec (sort-by pr-str (map m2-identity-value value)))
    (vector? value) (mapv m2-identity-value value)
    (seq? value) (mapv m2-identity-value value)
    :else value))

(defn m2-plan-id [plan]
  ;; M2 plan IDs were created by C4 over an already canonicalized value.  Do
  ;; not pass that value through the reader identity domain a second time.
  (str "sha256:"
       (digest/sha256-hex
        (pr-str
         (m2-identity-value
          (-> plan
              (dissoc :plan-id)
              (update :source #(if (map? %) (dissoc % :path) %))
              (update :module #(if (map? %)
                                 (dissoc % :source-path)
                                 %))))))))
