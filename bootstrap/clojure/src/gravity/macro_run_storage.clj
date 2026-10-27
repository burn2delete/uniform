(ns gravity.macro-run-storage
  "Fresh SH05 run-storage shape checks without repeated temporary key sets.
  No verification report, source identity or acceptance result is retained.")

(defn- exact-keys? [value expected-mask expected]
  (if (identical? clojure.lang.PersistentArrayMap (class value))
    (loop [remaining (seq (keys value)) seen 0]
      (if remaining
        (let [key (first remaining)]
          (if (keyword? key)
            ;; Do not stop on an extra keyword: the original set construction
            ;; still visits later keys and may report a foreign-key error.
            (recur (next remaining)
                   (bit-or seen
                           (long (case key
                                   :template-verification 1 :raw-template-result 2
                                   :resolved-expansion 4 :digest-requests 8
                                   :resolved-digests 16 :resolved-verification 32
                                   :schema-version 64 :status 128 :artifact 256
                                   :expansion-template 512 :rule 1024
                                   2048))))
            (= expected (set (keys value)))))
        (= expected-mask seen)))
    ;; Preserve arbitrary map/key callbacks and errors on unsupported carriers.
    (= expected (set (keys value)))))

(defn exact? [runs]
  (every?
   (fn [run]
     (and
      (exact-keys? run 63
                   #{:template-verification :raw-template-result :resolved-expansion
                     :digest-requests :resolved-digests :resolved-verification})
      (exact-keys? (:raw-template-result run) 960
                   #{:schema-version :status :artifact :expansion-template})
      (exact-keys? (:template-verification run) 1472
                   #{:rule :schema-version :status :artifact})
      (exact-keys? (:resolved-verification run) 1472
                   #{:rule :schema-version :status :artifact})))
   runs))
