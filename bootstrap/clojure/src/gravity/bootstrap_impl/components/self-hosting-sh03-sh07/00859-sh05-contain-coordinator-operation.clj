

(defn sh05-contain-coordinator-operation!
  [source-path responsibility operation]
  (try
    (operation)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (sh05-macro-boundary-fail!
       "C4-DEPTH" source-path responsibility nil
       {:contained-host-error (.getName (class error))}))
    (catch clojure.lang.ExceptionInfo error
      (if (contains? (set c4-macro-diagnostic-ids)
                     (:id (ex-data error)))
        (throw error)
        (sh05-macro-boundary-fail!
         "C4-TRACE" source-path responsibility nil
         {:contained-diagnostic (:id (ex-data error))})))
    (catch Throwable error
      (sh05-macro-boundary-fail!
       "C4-RETURN" source-path responsibility nil
       {:contained-host-error (.getName (class error))
        :cause-message (.getMessage error)}))))

(defn sh05-resolve-expansion-template!
  [source-path expansion-template resolved-digests]
  ;; Only these schema-declared positions carry coordinator digest controls.
  ;; Macro input and generated semantic forms remain opaque data, including
  ;; ordinary maps whose shape happens to resemble a digest reference.
  (let [output-reference {:digest-ref 0}
        provenance-reference {:digest-ref 1}
        output-reference-slots
        [[:artifact-id]
         [:output-syntax-id]
         [:macro-expansion-trace 0 :output-syntax-ids 0]
         [:metadata-ledger 0 :output-syntax-id]
         [:generated-origin-source-map 0 :output-syntax-id]]]
    (when-not
     (and (= 2 (count resolved-digests))
          (every? #(= output-reference (get-in expansion-template %))
                  output-reference-slots)
          (= provenance-reference
             (:provenance-binding-id expansion-template)))
      (sh05-macro-boundary-fail!
       "C4-TRACE" source-path :declared-macro-output-reference-slots
       {:output-references
        (mapv #(get-in expansion-template %) output-reference-slots)
        :provenance-reference
        (:provenance-binding-id expansion-template)}
       {:resolved-digest-count (count resolved-digests)}))
    (-> (reduce (fn [template slot]
                  (assoc-in template slot (first resolved-digests)))
                expansion-template output-reference-slots)
        (assoc :provenance-binding-id (second resolved-digests)))))

(defn sh05-run-macro-request!
  [source-path binding request]
  (let [raw
        (sh05-macro-execute!
         source-path binding 'sh05-expand-macro-template [request])
        digest-requests (:digest-requests raw)
        resolved-digests
        (sh05-contain-coordinator-operation!
         source-path :bounded-ordered-macro-digest-resolution
         #(sh05-macro-resolve-digests! source-path digest-requests))]
    (if (= :rejected (:status raw))
      (sh05-contain-coordinator-operation!
       source-path :structured-macro-rejection-packaging
       #(sh05-macro-raise-rejection! source-path raw resolved-digests))
      (let [template-verification
            (sh05-macro-execute!
             source-path binding 'sh05-verify-macro-template
             [(:expansion-template raw) digest-requests])
            resolved-expansion
            (sh05-contain-coordinator-operation!
             source-path :bounded-macro-reference-resolution
             #(sh05-resolve-expansion-template!
               source-path (:expansion-template raw) resolved-digests))
            resolved-verification
            (sh05-macro-execute!
             source-path binding 'sh05-verify-macro-resolved
             [resolved-expansion digest-requests resolved-digests])]
        (when-not (and (= :passed (:status template-verification))
                       (= :passed (:status resolved-verification)))
          (sh05-macro-boundary-fail!
           "C4-TRACE" source-path :fresh-gravity-macro-verification
           {:template template-verification
            :resolved resolved-verification} {}))
        {:raw-template-result
         (select-keys raw
                      [:artifact :schema-version :status
                       :expansion-template])
         :digest-requests digest-requests
         :resolved-digests resolved-digests
         :resolved-expansion resolved-expansion
         :template-verification
         (select-keys template-verification
                      [:artifact :schema-version :status :rule])
         :resolved-verification
         (select-keys resolved-verification
                      [:artifact :schema-version :status :rule])}))))

(defn sh05-source-syntax-stream
  [c3-artifact]
  (filterv #(not= :generated-form (get-in % [:form :kind]))
           (:syntax-object-stream c3-artifact)))

(defn sh05-expanded-form
  [form]
  (if (sh05-defn-form? form)
    (let [[_ name parameters & body] form]
      (list 'def name (apply list 'fn parameters body)))
    form))

(defn sh05-package-trace
  [run profile target]
  (let [expansion (:resolved-expansion run)
        trace (first (:macro-expansion-trace expansion))
        origin (get-in expansion [:generated-origin-source-map 0 :origin])]
    {:artifact :gravity/macro-expansion-step
     :step (:step trace)
     :macro 'defn
     :macro-version (:macro-version trace)
     :definition-span (:definition-span trace)
     :call-site-span (:call-site-span trace)
     :input-syntax-id (first (:input-syntax-ids trace))
     :output-syntax-id (first (:output-syntax-ids trace))
     :hygiene (:hygiene trace)
     :build-effects (:build-effects trace)
     :capabilities (:capabilities trace)
     :generated-origin [origin]
     :profile profile
     :target target
     :trace-replay-id (:trace-replay-id trace)
     :diagnostics []}))

(defn sh05-expanded-syntax-object
  [syntax form run]
  (if run
    (let [expansion (:resolved-expansion run)
          output-id (:output-syntax-id expansion)
          origin (get-in expansion [:generated-origin-source-map 0 :origin])]
      (-> syntax
          (assoc :artifact :gravity/expanded-syntax-object
                 :form (sh05-expanded-form form)
                 :syntax/id output-id
                 :expanded-syntax-id output-id
                 :phase :macro-expanded)
          (update :origin #(conj (vec %) origin))))
    (assoc syntax
           :artifact :gravity/expanded-syntax-object
           :form form
           :expanded-syntax-id (:syntax/id syntax)
           :phase :macro-expanded)))

(defn sh05-expanded-graph
  [expanded-stream trace]
  (let [nodes
        (mapv (fn [syntax]
                {:id (:expanded-syntax-id syntax)
                 :kind :expanded-syntax
                 :form (:form syntax)})
              expanded-stream)
        node-ids (set (map :id nodes))
        edges
        (mapv (fn [step]
                {:from (:input-syntax-id step)
                 :to (:output-syntax-id step)})
              trace)]
    {:artifact :gravity/sh05-expanded-syntax-graph
     :nodes nodes
     :edges edges
     :node-ids node-ids
     :status :complete}))

(defn sh05-expanded-graph-valid?
  [graph]
  (let [node-ids (set (map :id (:nodes graph)))]
    (and (= node-ids (:node-ids graph))
         (every? (fn [edge]
                   (and (contains? node-ids (:to edge))
                        (or (contains? node-ids (:from edge))
                            (string? (:from edge)))))
                 (:edges graph)))))