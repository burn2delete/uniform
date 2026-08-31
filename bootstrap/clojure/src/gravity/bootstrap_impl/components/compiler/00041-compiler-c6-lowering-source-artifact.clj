

(defn compiler-c6-lowering-source-artifact
  [source-path source-text]
  (let [c5-artifact (or *compiler-c6-authenticated-resolution-input*
                        (compiler-c5-resolution-source-artifact source-path
                                                                source-text))
        sh06-input? (= :gravity/sh06-resolution-artifact (:kind c5-artifact))
        sh06-report (when sh06-input?
                      (sh06-resolution-artifact-verification c5-artifact))
        _ (when (and sh06-input? (not= :passed (:status sh06-report)))
            (c6-lowering-fail! "C6-VERIFY" source-path
                               {:stage :core-lowering}
                               {:missing-fields [:fresh-authenticated-sh06-resolution]}))
        expanded-stream (if sh06-input?
                          (mapv (fn [syntax]
                                  (-> syntax
                                      (assoc :syntax-id (:syntax/id syntax)
                                             :generated-origin (:origin syntax))
                                      (dissoc :origin)))
                                (get-in c5-artifact
                                        [:sh05-macro-artifact :expanded-syntax-stream]))
                          (get-in c5-artifact
                                  [:c4-macro-expansion-artifact :expanded-syntax-stream]))
        records (when-not sh06-input?
                  (read-source-form-records source-path source-text))
        forms (mapv :form (if sh06-input? expanded-stream records))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)]
    (c6-call c6/c6-lowering-artifact
             source-path module c5-artifact expanded-stream)))