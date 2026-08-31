

(defn c2-reader-capability-proof
  [artifact]
  (c2-reader-product-projection-call
   :c2-reader-capability-proof
   c2-reader-product-projection/c2-reader-capability-proof
   artifact))

(defn c2-reader-validate!
  [source-path artifact]
  (let [proof (c2-reader-capability-proof artifact)]
    (doseq [[field id] [[:source-unit-hash-stable? "C2-HASH"]
                        [:token-and-form-spans-present? "C2-HASH"]
                        [:abbreviation-origins-present? "C2-ABBREV"]
                        [:literal-facts-present? "C2-STRING"]
                        [:trivia-retained? "C2-HASH"]
                        [:extension-policy-recorded? "C2-EXTENSION"]
                        [:incremental-hashes-stable? "C2-HASH"]
                        [:diagnostics-covered? "C2-HASH"]
                        [:lexical-token-stream? "C2-HASH"]
                        [:nested-form-tree? "C2-HASH"]]]
      (when-not (get proof field)
        (c2-reader-fail! id source-path
                         {:stage :read-source
                          :source-id (get-in artifact
                                             [:source-unit-record :source-id])
                          :source-span (or (get-in artifact
                                                   [:token-stream 0 :span])
                                           (source-span source-path 0))
                          :reader-options (get-in artifact
                                                  [:source-unit-record
                                                   :reader-options])}
                         {:missing-fields [field]}))))
  :complete)

(defn c2-reader-overrides-from-forms
  [forms]
  (c2-reader-product-projection-call
   :c2-reader-overrides-from-forms
   c2-reader-product-projection/c2-reader-overrides-from-forms
   forms))

(defn c2-reader-extension-invocations
  [form-tree]
  (c2-reader-product-projection-call
   :c2-reader-extension-invocations
   c2-reader-product-projection/c2-reader-extension-invocations
   form-tree))