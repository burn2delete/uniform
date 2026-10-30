

(defn
 sh05-contain-coordinator-operation!
 [source-path responsibility operation]
 (gravity.macro/contain-operation!
  source-path
  responsibility
  operation
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-resolve-expansion-template!
 [source-path expansion-template resolved-digests]
 (gravity.macro/resolve-expansion-template!
  source-path
  expansion-template
  resolved-digests
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-run-macro-request!
 [source-path binding request]
 (gravity.macro/expand
  source-path
  binding
  request
  ^{:line 112, :column 65}
  (macro-stage-operations)))

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