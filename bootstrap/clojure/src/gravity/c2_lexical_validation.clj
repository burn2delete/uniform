(ns gravity.c2-lexical-validation
  "Hosted Stage0 C2 UTF-8 span, form-graph, and lexical-product validation.

  This leaf validates already-produced hosted reader records. It does not read
  source, execute or authenticate SH03, construct canonical C2 products, or
  grant proof, self-hosting, attestation, or release authority."
  (:require [clojure.string :as str]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private operation-keys
  #{:c2-utf8-slice
    :c2-span-encloses?
    :c2-spans-source-ordered?
    :c2-form-graph-metrics
    :c2-lexical-product-validation})

(def ^:private namespace-contract
  {:namespace 'gravity.c2-lexical-validation
   :contract-boundary :hosted-c2-lexical-product-validation
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c2-utf8-slice {:arglists '([source-bytes byte-start byte-end])}
    'c2-span-encloses? {:arglists '([parent child])}
    'c2-spans-source-ordered? {:arglists '([spans])}
    'c2-form-graph-metrics {:arglists '([form-tree])}
    'c2-lexical-product-validation
    {:arglists '([source-text token-stream form-tree root-form-ids])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true}
   :artifact-inputs [:hosted-c2-token-stream :hosted-c2-form-tree]
   :artifact-outputs [:hosted-c2-lexical-product-validation]
   :ownership
   {:owns [:hosted-c2-utf8-slice-validation
           :hosted-c2-span-validation
           :hosted-c2-form-graph-validation
           :hosted-c2-lexical-product-validation]
    :does-not-own [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :source-reading
                   :tokenization
                   :form-construction
                   :complete-lexical-conformance-authority
                   :diagnostic-policy
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 lexical-validation operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 lexical-validation operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 lexical-validation operation must be a function"
                      {:operation key :value (get operations key)}))))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 lexical-validation thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if-let [operation# (current-operation ~key)]
       (binding [*active-operation-keys*
                 (conj *active-operation-keys* ~key)]
         (operation# ~@arguments))
       (do ~@body))))

(definterposable c2-utf8-slice :c2-utf8-slice
  [source-bytes byte-start byte-end]
  (String. (java.util.Arrays/copyOfRange source-bytes byte-start byte-end)
           java.nio.charset.StandardCharsets/UTF_8))

(definterposable c2-span-encloses? :c2-span-encloses?
  [parent child]
  (and (map? parent)
       (map? child)
       (integer? (:byte-start parent))
       (integer? (:byte-end parent))
       (integer? (:byte-start child))
       (integer? (:byte-end child))
       (<= (:byte-start parent) (:byte-start child))
       (>= (:byte-end parent) (:byte-end child))))

(definterposable c2-spans-source-ordered? :c2-spans-source-ordered?
  [spans]
  (every? (fn [[left right]]
            (and (map? left)
                 (map? right)
                 (integer? (:byte-end left))
                 (integer? (:byte-start right))
                 (<= (:byte-end left) (:byte-start right))))
          (partition 2 1 spans)))

(definterposable c2-form-graph-metrics :c2-form-graph-metrics
  [form-tree]
  (let [forms-by-id (into {} (map (juxt :form-id identity) form-tree))
        form-ids (mapv :form-id form-tree)
        indegrees
        (reduce (fn [counts form]
                  (reduce (fn [result child-id]
                            (if (contains? forms-by-id child-id)
                              (update result child-id (fnil inc 0))
                              result))
                          counts
                          (:children form)))
                (zipmap form-ids (repeat 0))
                form-tree)
        initial-ids (filterv #(zero? (get indegrees % 0)) form-ids)
        initial-queue (reduce conj clojure.lang.PersistentQueue/EMPTY
                              initial-ids)]
    (loop [pending initial-queue
           remaining indegrees
           depths (zipmap initial-ids (repeat 1))
           processed 0
           max-depth 0]
      (if (empty? pending)
        {:acyclic? (= processed (count form-ids))
         :processed-form-count processed
         :max-form-depth max-depth}
        (let [form-id (peek pending)
              parent-depth (get depths form-id 1)
              children (filterv #(contains? forms-by-id %)
                                (:children (forms-by-id form-id)))
              [next-queue next-remaining next-depths]
              (reduce
               (fn [[queue counts known-depths] child-id]
                 (let [next-count (dec (get counts child-id 0))
                       child-depth (max (get known-depths child-id 1)
                                        (inc parent-depth))]
                   [(cond-> queue (zero? next-count) (conj child-id))
                    (assoc counts child-id next-count)
                    (assoc known-depths child-id child-depth)]))
               [(pop pending) remaining depths]
               children)]
          (recur next-queue next-remaining next-depths (inc processed)
                 (max max-depth parent-depth)))))))

(definterposable c2-lexical-product-validation :c2-lexical-product-validation
  [source-text token-stream form-tree root-form-ids]
  (let [source-bytes (.getBytes source-text
                                java.nio.charset.StandardCharsets/UTF_8)
        source-byte-count (alength source-bytes)
        token-ids (mapv :token-id token-stream)
        form-ids (mapv :form-id form-tree)
        token-ids-unique? (= token-ids (vec (distinct token-ids)))
        form-ids-unique? (= form-ids (vec (distinct form-ids)))
        root-form-ids-unique?
        (= (vec root-form-ids) (vec (distinct root-form-ids)))
        tokens-by-id (into {} (map (juxt :token-id identity) token-stream))
        forms-by-id (into {} (map (juxt :form-id identity) form-tree))
        form-id-set (set form-ids)
        root-id-set (set root-form-ids)
        child-ids (mapcat :children form-tree)
        child-frequency (frequencies child-ids)
        parentless-id-set
        (set (keep #(when (nil? (:parent-form-id %)) (:form-id %)) form-tree))
        root-form-ids-resolve?
        (every? #(contains? forms-by-id %) root-form-ids)
        token-links-resolve-exactly-once?
        (and token-ids-unique?
             (every? (fn [form]
                       (and (contains? tokens-by-id (:open-token form))
                            (contains? tokens-by-id (:close-token form))))
                     form-tree))
        child-links-resolve-exactly-once?
        (and form-ids-unique?
             (every? #(contains? forms-by-id %) child-ids))
        parent-links-resolve-exactly-once?
        (and form-ids-unique?
             (every? #(or (nil? (:parent-form-id %))
                          (contains? forms-by-id (:parent-form-id %)))
                     form-tree))
        form-links-resolve?
        (and token-links-resolve-exactly-once?
             child-links-resolve-exactly-once?
             parent-links-resolve-exactly-once?)
        children-unique?
        (every? #(= (count (:children %))
                    (count (distinct (:children %))))
                form-tree)
        roots-parentless?
        (and root-form-ids-resolve?
             (every? #(nil? (:parent-form-id (forms-by-id %)))
                     root-form-ids))
        declared-roots-match-parentless?
        (= root-id-set parentless-id-set)
        non-root-single-parent?
        (every?
         (fn [form]
           (let [form-id (:form-id form)
                 parent-id (:parent-form-id form)]
             (if (contains? root-id-set form-id)
               (and (nil? parent-id)
                    (zero? (get child-frequency form-id 0)))
               (let [parent (forms-by-id parent-id)]
                 (and parent
                      (= 1 (get child-frequency form-id 0))
                      (= 1 (count (filter #{form-id} (:children parent)))))))))
         form-tree)
        parent-child-bidirectional?
        (and non-root-single-parent?
             (every? (fn [parent]
                       (every? #(= (:form-id parent)
                                   (:parent-form-id (forms-by-id %)))
                               (:children parent)))
                     form-tree))
        no-orphans?
        (every? #(if (contains? root-id-set %)
                   (zero? (get child-frequency % 0))
                   (= 1 (get child-frequency % 0)))
                form-ids)
        reachable-form-ids
        (loop [pending (vec root-form-ids)
               seen #{}]
          (if-let [form-id (first pending)]
            (let [remaining (subvec pending 1)
                  form (forms-by-id form-id)]
              (if (or (contains? seen form-id) (nil? form))
                (recur remaining seen)
                (recur (into remaining (:children form))
                       (conj seen form-id))))
            seen))
        all-forms-reachable? (= form-id-set reachable-form-ids)
        graph-metrics (c2-form-graph-metrics form-tree)
        acyclic? (:acyclic? graph-metrics)
        children-source-ordered?
        (every? (fn [form]
                  (let [children (mapv forms-by-id (:children form))]
                    (and (every? some? children)
                         (c2-spans-source-ordered? (mapv :span children)))))
                form-tree)
        root-forms-source-ordered?
        (let [roots (mapv forms-by-id root-form-ids)]
          (and (every? some? roots)
               (c2-spans-source-ordered? (mapv :span roots))))
        parent-spans-enclose?
        (and form-links-resolve?
             (every?
              (fn [form]
                (and (c2-span-encloses? (:span form)
                                        (:span (tokens-by-id
                                                (:open-token form))))
                     (c2-span-encloses? (:span form)
                                        (:span (tokens-by-id
                                                (:close-token form))))
                     (every? #(c2-span-encloses?
                               (:span form) (:span (forms-by-id %)))
                             (:children form))))
              form-tree))
        collection-delimiters-resolve?
        (and token-links-resolve-exactly-once?
             (every?
              (fn [form]
                (if-let [collection-kind (:collection-kind form)]
                  (let [open (tokens-by-id (:open-token form))
                        close (tokens-by-id (:close-token form))
                        expected-open ({:list ["(" :list-open]
                                        :vector ["[" :vector-open]
                                        :map ["{" :map-open]
                                        :set ["#{" :set-open]}
                                       collection-kind)
                        expected-close ({:list ")" :vector "]" :map "}"
                                         :set "}"} collection-kind)]
                    (and expected-open
                         (= (first expected-open) (:raw open))
                         (= (second expected-open) (:kind open))
                         (= expected-close (:raw close))
                         (= :close (:kind close))))
                  true))
              form-tree))
        maps-even-logical-children?
        (every? #(if (= :map (:collection-kind %))
                   (even? (count (:children %)))
                   true)
                form-tree)
        max-depth (:max-form-depth graph-metrics)
        valid-byte-range?
        (fn [span]
          (and (map? span)
               (integer? (:byte-start span))
               (integer? (:byte-end span))
               (<= 0 (:byte-start span) (:byte-end span) source-byte-count)))
        root-raw
        (set (keep (fn [form-id]
                     (let [form (forms-by-id form-id)]
                       (when (and form
                                  (or (:collection-kind form)
                                      (seq (:children form))))
                         (:raw form))))
                   root-form-ids))
        token-raw-slices-exact?
        (every? (fn [token]
                  (let [span (:span token)]
                    (and (string? (:raw token))
                         (pos? (count (:raw token)))
                         (valid-byte-range? span)
                         (= (:raw token)
                            (c2-utf8-slice source-bytes
                                          (:byte-start span)
                                          (:byte-end span))))))
                token-stream)
        form-raw-slices-exact?
        (every? (fn [form]
                  (let [span (:span form)]
                    (and (string? (:raw form))
                         (pos? (count (:raw form)))
                         (valid-byte-range? span)
                         (= (:raw form)
                            (c2-utf8-slice source-bytes
                                          (:byte-start span)
                                          (:byte-end span))))))
                form-tree)
        graph-valid?
        (every? true?
                [token-ids-unique? form-ids-unique? root-form-ids-unique?
                 root-form-ids-resolve? token-links-resolve-exactly-once?
                 child-links-resolve-exactly-once?
                 parent-links-resolve-exactly-once? children-unique?
                 roots-parentless? declared-roots-match-parentless?
                 non-root-single-parent? parent-child-bidirectional?
                 no-orphans? all-forms-reachable? acyclic?
                 children-source-ordered? root-forms-source-ordered?
                 parent-spans-enclose? collection-delimiters-resolve?
                 maps-even-logical-children? form-raw-slices-exact?])]
    {:artifact :gravity/c2-lexical-product-validation
     :ordered-token-ids-unique? token-ids-unique?
     :form-ids-unique? form-ids-unique?
     :root-form-ids-unique? root-form-ids-unique?
     :token-count-exceeds-top-level-form-count?
     (> (count token-stream) (count root-form-ids))
     :token-raw-slices-exact? token-raw-slices-exact?
     :form-raw-slices-exact? form-raw-slices-exact?
     :token-provenance-complete?
     (every? #(and (:source-id %) (:source-path %)
                   (= (:source-id %) (get-in % [:span :file]))
                   (get-in % [:span :start :line])
                   (get-in % [:span :start :column])
                   (get-in % [:span :end :line])
                   (get-in % [:span :end :column]))
             token-stream)
     :no-token-contains-top-level-form?
     (not-any? (fn [token]
                 (some #(str/includes? (:raw token) %) root-raw))
               token-stream)
     :root-form-ids-resolve? root-form-ids-resolve?
     :token-links-resolve-exactly-once? token-links-resolve-exactly-once?
     :child-links-resolve-exactly-once? child-links-resolve-exactly-once?
     :parent-links-resolve-exactly-once? parent-links-resolve-exactly-once?
     :form-links-resolve? form-links-resolve?
     :children-unique? children-unique?
     :roots-parentless? roots-parentless?
     :declared-roots-match-parentless? declared-roots-match-parentless?
     :non-root-single-parent? non-root-single-parent?
     :parent-child-bidirectional? parent-child-bidirectional?
     :no-orphans? no-orphans?
     :all-forms-reachable? all-forms-reachable?
     :acyclic? acyclic?
     :children-source-ordered? children-source-ordered?
     :root-forms-source-ordered? root-forms-source-ordered?
     :parent-spans-enclose-children? parent-spans-enclose?
     :collection-delimiters-resolve? collection-delimiters-resolve?
     :maps-even-logical-children? maps-even-logical-children?
     :recursive-children-present? (boolean (some #(seq (:children %)) form-tree))
     :nested-depth-at-least-three? (>= max-depth 3)
     :max-form-depth max-depth
     :graph-valid? graph-valid?
     :status (if graph-valid? :passed :failed)}))
