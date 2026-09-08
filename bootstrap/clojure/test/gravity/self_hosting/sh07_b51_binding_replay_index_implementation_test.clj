(ns gravity.self-hosting.sh07-b51-binding-replay-index-implementation-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt30-producer-compact-verifier-test
             :as producer]))

(defn- digest-id [coordinate]
  (bootstrap/reader-canonical-hash coordinate))

(defn- binding-record [ordinal binding-class syntax-id]
  {:binding-id (digest-id [:binding ordinal])
   :name (symbol (str "binding-" ordinal))
   :kind :var
   :binding-class binding-class
   :namespace 'gravity.test.binding-replay
   :package {:name 'gravity.test :version "1"}
   :upstream-binding-id (digest-id [:upstream ordinal])
   :scope-id (when (= :lexical binding-class)
               (digest-id [:scope ordinal]))
   :definition-syntax-id syntax-id
   :definition-artifact-id (digest-id [:definition ordinal])
   :visibility :private
   :profile-set []
   :target-set []
   :type-ref :any
   :effects []
   :capabilities []
   :safety :safe
   :semantic-span {}})

(defn- form-record [ordinal syntax-id]
  {:form-id (digest-id [:form ordinal]) :syntax-id syntax-id})

(defn- request [forms bindings]
  {:forms forms :binding-table bindings
   :resolution-table [] :alias-table []})

(defn- reference-result [request indexes]
  (cond
    (or (get-in indexes [:forms :duplicate-primary?])
        (get-in indexes [:forms :duplicate-secondary?]))
    {:status :rejected :reason :form-index-duplicate :partial-products []}

    (or (get-in indexes [:bindings :duplicate-primary?])
        (get-in indexes [:bindings :duplicate-secondary?]))
    {:status :rejected :reason :binding-index-duplicate :partial-products []}

    :else
    (loop [remaining (:binding-table request) failing []]
      (if (empty? remaining)
        {:status :accepted :binding-ids failing}
        (let [binding (first remaining)
              count-found
              (count (filter #(= (:definition-syntax-id binding)
                                 (:syntax-id %))
                             (:forms request)))
              binding-class (:binding-class binding)]
          (cond
            (not (producer/invoke
                  'sh07-b51-v16-binding-record-base-shape? binding))
            {:status :rejected :reason :projected-binding-shape
             :partial-products []}

            (contains? #{:core :import} binding-class)
            (recur (rest remaining) failing)

            (= 1 count-found)
            (recur (rest remaining) failing)

            (and (zero? count-found) (= :namespace binding-class))
            (recur (rest remaining) (conj failing (:binding-id binding)))

            (zero? count-found)
            {:status :rejected :reason :non-waivable-missing-binding-form
             :partial-products []}

            :else
            {:status :rejected :reason :duplicate-binding-definition-form
             :partial-products []}))))))

(defn- successor-result [request]
  (let [indexes (producer/invoke 'sh07-b51-index-request request)]
    (producer/invoke 'sh07-b51-derived-no-form-failing-binding-ids
                     request indexes)))

(deftest exact-source-cutpoint-uses-the-existing-secondary-index-once
  (let [form (get @producer/producer-definitions
                  'sh07-b51-derived-no-form-failing-binding-ids)
        calls (vec (tree-seq coll? seq form))
        call-position
        (fn [function-name]
          (keep-indexed
           (fn [position node]
             (when (and (seq? node) (= function-name (first node)))
               position))
           calls))]
    (is (= 1 (count (filter #(and (seq? %)
                                  (= 'sh07-b51-find-form-by-syntax
                                     (first %)))
                            calls))))
    (is (zero? (count (filter #(and (seq? %)
                                    (= 'sh07-b51-find-by (first %)))
                              calls))))
    (is (= 1 (count (call-position
                     'sh07-b51-v16-binding-record-base-shape?))))
    (is (< (first (call-position 'sh07-b51-find-form-by-syntax))
           (first (call-position
                   'sh07-b51-v16-binding-record-base-shape?))))))

(deftest indexed-presence-matches-the-reference-scan-after-duplicate-guards
  (let [s0 (digest-id [:syntax 0])
        s1 (digest-id [:syntax 1])
        s2 (digest-id [:syntax 2])
        core (binding-record 0 :core s0)
        import (binding-record 1 :import s1)
        namespace-a (binding-record 2 :namespace s1)
        namespace-b (binding-record 3 :namespace s2)
        lexical (binding-record 4 :lexical s0)
        one-form (form-record 0 s0)
        cases
        {:one-form (request [one-form] [lexical])
         :no-form-core-import (request [] [core import])
         :ordered-no-form-namespace
         (request [] [namespace-a namespace-b])
         :missing-nonwaivable (request [] [lexical])
         :duplicate-form-id
         (request [(form-record 0 s0)
                   (assoc (form-record 1 s1) :form-id (:form-id one-form))]
                  [lexical])
         :duplicate-form-syntax
         (request [(form-record 0 s0) (form-record 1 s0)] [lexical])
         :duplicate-binding-id
         (request [] [namespace-a
                      (assoc namespace-b :binding-id
                             (:binding-id namespace-a))])
         :duplicate-binding-definition
         (request [] [namespace-a
                      (assoc namespace-b :definition-syntax-id
                             (:definition-syntax-id namespace-a))])}]
    (doseq [[label request] cases]
      (testing (name label)
        (let [indexes (producer/invoke 'sh07-b51-index-request request)]
          (is (= (reference-result request indexes)
                 (successor-result request))))))))

(deftest edge-census-remains-the-governed-size
  (let [products
        (bootstrap/p15-s23-sh07-b51-current-bundle-core-products)]
    (is (= 1480 (count (get-in products [:link-manifest :edges]))))
    (is (= 1
           (count
            (filter
             #(and (= 'sh07-b51-derived-no-form-failing-binding-ids
                      (:caller-function %))
                   (= 'sh07-b51-find-form-by-syntax (:callee-function %))
                   (= 2 (:arity %))
                   (= :producer-internal (:owner-edge %)))
             (get-in products [:link-manifest :edges])))))))
