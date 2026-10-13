
(defn- p15-s23-sh07-combine-decorations
  [depth node-budget open separator close decorations]
  (let [node-count
        (inc (reduce + 0 (map :node-occurrences decorations)))
        maximum-depth
        (reduce max depth (map :maximum-depth decorations))]
    (if (> node-count node-budget)
      (p15-s23-sh07-decoration-failed :node-count-limit)
      (let [maximum-output-bytes
            (:maximum-output-bytes
             p15-s23-sh07-canonical-decoration-limits)
            builder (StringBuilder.)
            append!
            (fn [bytes text]
              (let [observation
                    (p15-s23-sh07-observe-utf8-byte-count
                     text bytes maximum-output-bytes)]
                (if (not= :accepted (:status observation))
                  nil
                  (do (.append builder ^String text)
                      (:observed-after observation)))))
            first-bytes (append! 0 open)]
        (if (nil? first-bytes)
          (p15-s23-sh07-decoration-failed :output-byte-limit)
          (loop [remaining decorations bytes first-bytes first? true]
            (if (empty? remaining)
              (if-let [complete-bytes (append! bytes close)]
                {:status :decorated
                 :text (.toString builder)
                 :node-occurrences node-count
                 :maximum-depth maximum-depth
                 :output-utf8-bytes complete-bytes}
                (p15-s23-sh07-decoration-failed :output-byte-limit))
              (let [with-separator
                    (if first? bytes (append! bytes separator))
                    with-text
                    (when with-separator
                      (append! with-separator (:text (first remaining))))]
                (if with-text
                  (recur (rest remaining) with-text false)
                  (p15-s23-sh07-decoration-failed
                   :output-byte-limit))))))))))

(def ^:private p15-s23-sh07-o-c2-carrier-keys
  #{:artifact :c2-reader-artifact :root-adapted-form-id
    :decimal-replay-evidence})

(defn- p15-s23-sh07-o-c2-carrier?
  [carrier]
  (and (map? carrier)
       (= p15-s23-sh07-o-c2-carrier-keys (set (keys carrier)))
       (= :gravity/sh07-oracle-c2-carrier-v1 (:artifact carrier))
       (map? (:decimal-replay-evidence carrier))))

(defn- p15-s23-sh07-o-c2-decoration
  [carrier]
  (try
    (let [artifact (:c2-reader-artifact carrier)
          forms (:form-tree artifact)
          adapted-forms
          (mapv #(-> %
                     (assoc :child-form-ids (:children %))
                     (dissoc :children))
                forms)
          forms-by-id (into {} (map (juxt :form-id identity)) adapted-forms)
          root-id (:root-adapted-form-id carrier)
          replays (:decimal-replay-evidence carrier)
          limits p15-s23-sh07-canonical-decoration-limits]
      (when-not
       (and (= (:artifact-id artifact) (c2-reader-artifact-id artifact))
            (sh03-reader-final-decimal-occurrence-projection-valid? artifact)
            (= (count adapted-forms) (count forms-by-id))
            (map? (get forms-by-id root-id)))
        (throw (ex-info "source oracle C2 carrier is unauthenticated"
                        {:reason :source-integrity-mismatch})))
      (letfn
          [(decorate-scalar [form depth]
             (let [kind (:kind form)
                   rendered
                   (if (= :decimal kind)
                     (let [source-evidence
                           {:c2-reader-artifact artifact
                            :adapted-form-id (:form-id form)
                            :adapted-token-id (:open-token form)}
                           source-occurrence
                           (p15-s23-sh07-o-decimal-occurrence source-evidence)
                           source-entry
                           (:semantic-value-entry source-occurrence)
                           source-key (:semantic-key source-entry)
                           _
                           (when-not
                            (p15-s23-sh07-o-decimal-authentic-key? source-key)
                             (throw
                              (ex-info
                               "source State C has no exact current-C2 key"
                               {:reason
                                :unsupported-decimal-readable-spelling})))
                           key source-key
                           replay-evidence
                           (get replays [(:form-id form) (:open-token form)])
                           replay-occurrence
                           (p15-s23-sh07-o-decimal-occurrence replay-evidence)]
                       (when (= (p15-s23-sh07-o-decimal-occurrence-identity
                                 source-evidence source-occurrence)
                                (p15-s23-sh07-o-decimal-occurrence-identity
                                 replay-evidence replay-occurrence))
                         (throw (ex-info "source oracle replay is not fresh"
                                         {:reason
                                          :unsupported-decimal-readable-spelling})))
                       (let [text (p15-s23-sh07-o-decimal-decoration
                                   key replay-evidence)
                             observation
                             (p15-s23-sh07-observe-utf8-byte-count
                              text 0 (:maximum-scalar-bytes limits))]
                         (when-not (= :accepted (:status observation))
                           (throw (ex-info "decimal scalar G20 rejection"
                                           {:reason :scalar-byte-limit})))
                         {:text text
                          :output-utf8-bytes
                          (:observed-after observation)}))
                     (p15-s23-sh07-o-scalar-text (:value form)))
                   text (:text rendered)]
               {:status :decorated :text text :node-occurrences 1
                :maximum-depth depth
                :output-utf8-bytes (:output-utf8-bytes rendered)}))
           (decorate-form [form-id depth node-budget active]
             (if (<= node-budget 0)
               (p15-s23-sh07-decoration-failed :node-count-limit)
               (if (> depth (:maximum-depth limits))
                 (p15-s23-sh07-decoration-failed :depth-limit)
                 (let [form (get forms-by-id form-id)
                       collection-kind (or (:collection-kind form)
                                           (when (contains? #{:vector :list
                                                              :set :map}
                                                            (:kind form))
                                             (:kind form)))
                       child-ids (:child-form-ids form)]
                   (if-not form
                     (p15-s23-sh07-decoration-failed
                      :source-integrity-mismatch)
                     (if-not collection-kind
                       (decorate-scalar form depth)
                       (if (contains? active form-id)
                         (p15-s23-sh07-decoration-failed :cyclic-value)
                         (if (> (count child-ids)
                                (:maximum-collection-width limits))
                           (p15-s23-sh07-decoration-failed
                            :collection-width-limit)
                           (let [next-active (conj active form-id)
                                 decorated
                                 (mapv #(decorate-form
                                         % (inc depth) (dec node-budget)
                                         next-active)
                                       child-ids)
                                 failed? (some #(not= :decorated (:status %))
                                               decorated)]
                             (if failed?
                               (p15-s23-sh07-decoration-failed
                                (if (contains? #{:set :map} collection-kind)
                                  :unordered-decoration-failure
                                  (:reason (first
                                            (filter #(not= :decorated
                                                           (:status %))
                                                    decorated)))))
                               (case collection-kind
                                 :vector
                                 (p15-s23-sh07-combine-decorations
                                  depth node-budget "[" " " "]" decorated)
                                 :list
                                 (p15-s23-sh07-combine-decorations
                                  depth node-budget "(" " " ")" decorated)
                                 :set
                                 (let [ordered
                                       (p15-s23-sh07-stable-order-by
                                        #(p15-s23-sh07-canonical-text-compare
                                          (:text %1) (:text %2))
                                        decorated)]
                                   (if (some true?
                                             (map #(= (:text %1) (:text %2))
                                                  ordered (rest ordered)))
                                     (p15-s23-sh07-decoration-failed
                                      :source-integrity-mismatch)
                                     (p15-s23-sh07-combine-decorations
                                      depth node-budget "#{" " " "}" ordered)))
                                 :map
                                 (if (odd? (count decorated))
                                   (p15-s23-sh07-decoration-failed
                                    :source-integrity-mismatch)
                                   (let [entries
                                         (mapv (fn [[key-result value-result]]
                                                 {:key-result key-result
                                                  :value-result value-result})
                                               (partition 2 decorated))
                                         ordered
                                         (p15-s23-sh07-stable-order-by
                                          (fn [left right]
                                            (let [key-order
                                                  (p15-s23-sh07-canonical-text-compare
                                                   (get-in left
                                                           [:key-result :text])
                                                   (get-in right
                                                           [:key-result :text]))]
                                              (if (zero? key-order)
                                                (p15-s23-sh07-canonical-text-compare
                                                 (get-in left
                                                         [:value-result :text])
                                                 (get-in right
                                                         [:value-result :text]))
                                                key-order)))
                                          entries)
                                         collisions?
                                         (some true?
                                               (map #(= (get-in %1
                                                                [:key-result
                                                                 :text])
                                                        (get-in %2
                                                                [:key-result
                                                                 :text]))
                                                    ordered (rest ordered)))]
                                     (if collisions?
                                       (p15-s23-sh07-decoration-failed
                                        :source-integrity-mismatch)
                                       (p15-s23-sh07-combine-decorations
                                        depth node-budget "{" ", " "}"
                                        (mapv
                                         (fn [entry]
                                           {:text
                                            (str (get-in entry
                                                         [:key-result :text])
                                                 " "
                                                 (get-in entry
                                                         [:value-result :text]))
                                            :node-occurrences
                                            (+ (:node-occurrences
                                                (:key-result entry))
                                               (:node-occurrences
                                                (:value-result entry)))
                                            :maximum-depth
                                            (max (:maximum-depth
                                                  (:key-result entry))
                                                 (:maximum-depth
                                                  (:value-result entry)))})
                                         ordered))))))))))))))))]
        (decorate-form root-id 0 (:maximum-nodes limits) #{})))
    (catch clojure.lang.ExceptionInfo error
      (p15-s23-sh07-decoration-failed
       (or (:reason (ex-data error)) :contained-decoration-failure)))
    (catch Exception _
      (p15-s23-sh07-decoration-failed :contained-decoration-failure))))
