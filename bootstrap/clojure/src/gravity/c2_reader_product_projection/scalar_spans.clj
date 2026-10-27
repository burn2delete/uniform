(ns gravity.c2-reader-product-projection.scalar-spans
  "Project scalar coordinates from retained Reader root slices, not UTF16 :char.
  The caller still owns validation/authentication of the supplied products.")

(defn- fail! [reason]
  (throw (ex-info "Retained Reader roots do not supply matching scalar spans"
                  {:boundary :reader-root-scalar-spans :reason reason})))

(defn root-spans [products forms-by-id]
  (when (contains? products :sh03-reader-result)
    (let [raw (:sh03-reader-result products)
          raw-forms (:form-tree raw)
          raw-roots (:top-level-form-ids raw)
          roots (:root-form-ids products)]
      (when-not (and (map? raw) (vector? raw-forms) (vector? raw-roots) (vector? roots))
        (fail! :root-mapping))
      (let [raw-by-id (into {} (map (juxt :form-id identity) raw-forms))]
      ;; Raw and adapted IDs occupy different domains; their established root
      ;; order and exact byte intervals join them without inventing new IDs.
      (when-not (and (= (count raw-forms) (count raw-by-id))
                     (= (count roots) (count raw-roots))
                     (= roots (mapv :form-id (:parsed-records products)))
                     (= (count roots) (count (set roots)))
                     (= (count raw-roots) (count (set raw-roots))))
        (fail! :root-mapping))
      (into {}
        (map
          (fn [root raw-root]
            (let [form (forms-by-id root) slice (:raw (raw-by-id raw-root))
                  start (:scalar-start slice) end (:scalar-end slice)
                  span (:span form)]
              (when-not (and form (= :gravity/source-slice (:artifact slice))
                             (= 1 (:schema-version slice)) (= :utf-8 (:encoding slice))
                             (integer? start) (integer? end) (<= 0 start end)
                             (integer? (:byte-start slice)) (integer? (:byte-end slice))
                             (<= 0 (:byte-start slice) (:byte-end slice))
                             (= (select-keys span [:byte-start :byte-end])
                                (select-keys slice [:byte-start :byte-end]))
                             (every? (fn [key] (or (not (contains? span key))
                                                   (= (get span key) (get slice key))))
                                     [:scalar-start :scalar-end]))
                (fail! :source-slice))
              [root {:scalar-start start :scalar-end end}]))
          roots raw-roots))))))
