(ns gravity.c2-lexical-validation.product
  (:require [clojure.string :as str]))

(defn lexical-product-validation
  [{:keys [utf8-slice span-encloses? spans-source-ordered? form-graph-metrics]}
   source-text token-stream form-tree root-form-ids]
  (let [source-bytes (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)
        source-byte-count (alength source-bytes)
        token-ids (mapv :token-id token-stream)
        form-ids (mapv :form-id form-tree)
        token-ids-unique? (= token-ids (vec (distinct token-ids)))
        form-ids-unique? (= form-ids (vec (distinct form-ids)))
        root-form-ids-unique? (= (vec root-form-ids) (vec (distinct root-form-ids)))
        tokens-by-id (into {} (map (juxt :token-id identity) token-stream))
        forms-by-id (into {} (map (juxt :form-id identity) form-tree))
        form-id-set (set form-ids)
        root-id-set (set root-form-ids)
        child-ids (mapcat :children form-tree)
        child-frequency (frequencies child-ids)
        parentless-id-set (set (keep #(when (nil? (:parent-form-id %))
                                        (:form-id %)) form-tree))
        root-form-ids-resolve? (every? #(contains? forms-by-id %) root-form-ids)
        token-links-resolve-exactly-once?
        (and token-ids-unique?
             (every? (fn [form]
                       (and (contains? tokens-by-id (:open-token form))
                            (contains? tokens-by-id (:close-token form)))) form-tree))
        child-links-resolve-exactly-once?
        (and form-ids-unique? (every? #(contains? forms-by-id %) child-ids))
        parent-links-resolve-exactly-once?
        (and form-ids-unique?
             (every? #(or (nil? (:parent-form-id %))
                          (contains? forms-by-id (:parent-form-id %))) form-tree))
        form-links-resolve? (and token-links-resolve-exactly-once?
                                 child-links-resolve-exactly-once?
                                 parent-links-resolve-exactly-once?)
        children-unique? (every? #(= (count (:children %))
                                      (count (distinct (:children %)))) form-tree)
        roots-parentless? (and root-form-ids-resolve?
                               (every? #(nil? (:parent-form-id (forms-by-id %)))
                                       root-form-ids))
        declared-roots-match-parentless? (= root-id-set parentless-id-set)
        non-root-single-parent?
        (every? (fn [form]
                  (let [form-id (:form-id form) parent-id (:parent-form-id form)]
                    (if (contains? root-id-set form-id)
                      (and (nil? parent-id) (zero? (get child-frequency form-id 0)))
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
                               (:children parent))) form-tree))
        no-orphans? (every? #(if (contains? root-id-set %)
                                (zero? (get child-frequency % 0))
                                (= 1 (get child-frequency % 0))) form-ids)
        reachable-form-ids
        (loop [pending (vec root-form-ids) seen #{}]
          (if-let [form-id (first pending)]
            (let [remaining (subvec pending 1) form (forms-by-id form-id)]
              (if (or (contains? seen form-id) (nil? form))
                (recur remaining seen)
                (recur (into remaining (:children form)) (conj seen form-id))))
            seen))
        all-forms-reachable? (= form-id-set reachable-form-ids)
        graph-metrics (form-graph-metrics form-tree)
        acyclic? (:acyclic? graph-metrics)
        children-source-ordered?
        (every? (fn [form]
                  (let [children (mapv forms-by-id (:children form))]
                    (and (every? some? children)
                         (spans-source-ordered? (mapv :span children))))) form-tree)
        root-forms-source-ordered?
        (let [roots (mapv forms-by-id root-form-ids)]
          (and (every? some? roots) (spans-source-ordered? (mapv :span roots))))
        parent-spans-enclose?
        (and form-links-resolve?
             (every? (fn [form]
                       (and (span-encloses? (:span form)
                                            (:span (tokens-by-id (:open-token form))))
                            (span-encloses? (:span form)
                                            (:span (tokens-by-id (:close-token form))))
                            (every? #(span-encloses? (:span form)
                                                     (:span (forms-by-id %)))
                                    (:children form)))) form-tree))
        collection-delimiters-resolve?
        (and token-links-resolve-exactly-once?
             (every? (fn [form]
                       (if-let [collection-kind (:collection-kind form)]
                         (let [open (tokens-by-id (:open-token form))
                               close (tokens-by-id (:close-token form))
                               expected-open ({:list ["(" :list-open]
                                               :vector ["[" :vector-open]
                                               :map ["{" :map-open]
                                               :set ["#{" :set-open]} collection-kind)
                               expected-close ({:list ")" :vector "]" :map "}" :set "}"}
                                               collection-kind)]
                           (and expected-open
                                (= (first expected-open) (:raw open))
                                (= (second expected-open) (:kind open))
                                (= expected-close (:raw close)) (= :close (:kind close))))
                         true)) form-tree))
        maps-even-logical-children? (every? #(if (= :map (:collection-kind %))
                                                (even? (count (:children %))) true) form-tree)
        max-depth (:max-form-depth graph-metrics)
        valid-byte-range? (fn [span]
                            (and (map? span) (integer? (:byte-start span))
                                 (integer? (:byte-end span))
                                 (<= 0 (:byte-start span) (:byte-end span)
                                     source-byte-count)))
        root-raw (set (keep (fn [form-id]
                              (let [form (forms-by-id form-id)]
                                (when (and form (or (:collection-kind form)
                                                   (seq (:children form))))
                                  (:raw form)))) root-form-ids))
        token-raw-slices-exact?
        (every? (fn [token]
                  (let [span (:span token)]
                    (and (string? (:raw token)) (pos? (count (:raw token)))
                         (valid-byte-range? span)
                         (= (:raw token) (utf8-slice source-bytes (:byte-start span)
                                                      (:byte-end span)))))) token-stream)
        form-raw-slices-exact?
        (every? (fn [form]
                  (let [span (:span form)]
                    (and (string? (:raw form)) (pos? (count (:raw form)))
                         (valid-byte-range? span)
                         (= (:raw form) (utf8-slice source-bytes (:byte-start span)
                                                     (:byte-end span)))))) form-tree)
        graph-valid? (every? true?
                              [token-ids-unique? form-ids-unique? root-form-ids-unique?
                               root-form-ids-resolve? token-links-resolve-exactly-once?
                               child-links-resolve-exactly-once?
                               parent-links-resolve-exactly-once? children-unique?
                               roots-parentless? declared-roots-match-parentless?
                               non-root-single-parent? parent-child-bidirectional? no-orphans?
                               all-forms-reachable? acyclic? children-source-ordered?
                               root-forms-source-ordered? parent-spans-enclose?
                               collection-delimiters-resolve? maps-even-logical-children?
                               form-raw-slices-exact?])]
    {:artifact :gravity/c2-lexical-product-validation
     :ordered-token-ids-unique? token-ids-unique?
     :form-ids-unique? form-ids-unique?
     :root-form-ids-unique? root-form-ids-unique?
     :token-count-exceeds-top-level-form-count? (> (count token-stream)
                                                     (count root-form-ids))
     :token-raw-slices-exact? token-raw-slices-exact?
     :form-raw-slices-exact? form-raw-slices-exact?
     :token-provenance-complete?
     (every? #(and (:source-id %) (:source-path %)
                   (= (:source-id %) (get-in % [:span :file]))
                   (get-in % [:span :start :line]) (get-in % [:span :start :column])
                   (get-in % [:span :end :line]) (get-in % [:span :end :column])) token-stream)
     :no-token-contains-top-level-form?
     (not-any? (fn [token] (some #(str/includes? (:raw token) %) root-raw)) token-stream)
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
     :max-form-depth max-depth :graph-valid? graph-valid?
     :status (if graph-valid? :passed :failed)}))
