(ns gravity.c3-literal-projection.characters
  "Internal occurrence-preserving C2 Character projection for the C3 adapter.
  Consumes the existing authenticated reader report; grants no authority.")

(defn source-kind!
  [seed form-record c2-artifact integrity-report]
  (let [forms (filterv #(= (:form-id form-record) (:form-id %)) (:form-tree c2-artifact))
        literals (filterv #(= (:form-id form-record) (:form-id %)) (:literal-decoding-records c2-artifact))
        literal (first literals) value (:form seed)
        scalar? (if (char? value)
                  (not (<= 0xd800 (int value) 0xdfff))
                  (when (and (map? value)
                             (= #{:artifact :schema-version :codepoint :text} (set (keys value)))
                             (= :gravity/unicode-scalar-character (:artifact value))
                             (= 1 (:schema-version value))
                             (integer? (:codepoint value))
                             (<= 0x10000 (:codepoint value) 0x10ffff))
                    (= (:text value) (String. (Character/toChars (int (:codepoint value)))))))]
    (when-not (and (:authentic? integrity-report)
                   (= 1 (count forms) (count literals)) (= form-record (first forms))
                   (= (:form-id seed) (:form-id form-record))
                   (= :character (:kind form-record) (:kind literal))
                   (= [] (:children form-record))
                   (= value (:value form-record) (:decoded literal))
                   (= (:raw form-record) (:raw literal)) (= (:span form-record) (:span literal)) scalar?)
      (throw (ex-info "Character source kind requires its exact authenticated C2 occurrence"
                      {:id "C3-ORIGIN" :stage :syntax-object :reason :character-c2-occurrence-mismatch
                       :form-id (:form-id form-record)})))
    :character))
