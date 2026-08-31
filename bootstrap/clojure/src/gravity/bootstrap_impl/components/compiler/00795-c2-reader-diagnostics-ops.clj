

(defn- c2-reader-diagnostics-ops
  []
  {:fail! fail!
   :source-span source-span
   :reader-canonical-hash reader-canonical-hash
   :standard-reader-options standard-reader-options
   :c2-reader-source-overrides c2-reader-source-overrides
   :c2-reader-message c2-reader-message
   :c2-reader-fail! c2-reader-fail!
   :c2-reader-remap-exception! c2-reader-remap-exception!
   :c2-reader-validate-overrides! c2-reader-validate-overrides!
   :c2-reader-diagnostic-ids c2-reader-diagnostic-ids
   :c2-reader-governing-document c2-reader-governing-document
   :c2-reader-rejected-designs c2-reader-rejected-designs
   :c2-reader-override-diagnostics c2-reader-override-diagnostics})