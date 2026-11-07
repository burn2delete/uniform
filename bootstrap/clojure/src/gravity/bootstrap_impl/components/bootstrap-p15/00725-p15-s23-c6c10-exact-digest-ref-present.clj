

(defn p15-s23-c6c10-exact-digest-ref-present?
  [value]
  (gravity.checked-core/exact-digest-ref-present? value
   {:digest-ref-shape? #'p15-s23-c6c10-digest-ref-shape?}))

(defn p15-s23-c6c10-structure-kind
  [value]
  (gravity.checked-core/structure-kind value))

(declare p15-s23-c6c10-strict-first-mismatch)

(defn p15-s23-c6c10-strict-map-index
  [source-path value]
  (gravity.checked-core/strict-map-index source-path value
   {:canonical-identity #'p15-s23-c6c10-canonical-identity}))

(defn p15-s23-c6c10-strict-first-mismatch
  [source-path expected actual path]
  (gravity.checked-core/strict-first-mismatch source-path expected actual path
   {:structure-kind #'p15-s23-c6c10-structure-kind
    :canonical-identity #'p15-s23-c6c10-canonical-identity
    :strict-map-index #'p15-s23-c6c10-strict-map-index
    :canonical-sort-key #'p15-s23-c6c10-canonical-sort-key
    :strict-first-mismatch #'p15-s23-c6c10-strict-first-mismatch
    :canonical-digest #'p15-s23-c6c10-canonical-digest}))

(defn p15-s23-c6c10-strict-structure!
  [source-path expected actual missing-fact]
  (gravity.checked-core/strict-structure! source-path expected actual missing-fact
   {:strict-first-mismatch #'p15-s23-c6c10-strict-first-mismatch
    :canonical-digest #'p15-s23-c6c10-canonical-digest
    :canonical-record #'p15-s23-c6c10-canonical-record
    :host-fail! #'p15-s23-c6c10-host-fail!}))

(defn p15-s23-c6c10-canonical-identical?
  [source-path left right]
  (gravity.checked-core/canonical-identical? source-path left right
   {:canonical-identity #'p15-s23-c6c10-canonical-identity}))

(defn p15-s23-c6c10-rehydrate-candidate-template!
  [source-path expected-raw expected-sealed candidate-sealed resolved-digests]
  (gravity.checked-core/rehydrate-candidate-template! source-path expected-raw expected-sealed candidate-sealed resolved-digests
   {:digest-ref-shape? #'p15-s23-c6c10-digest-ref-shape?
    :structure-kind #'p15-s23-c6c10-structure-kind
    :canonical-identity #'p15-s23-c6c10-canonical-identity
    :strict-map-index #'p15-s23-c6c10-strict-map-index
    :resolve-digest-references! #'p15-s23-c6c10-resolve-digest-references!
    :canonical-identical? #'p15-s23-c6c10-canonical-identical?
    :unique-resolved-values! #'p15-s23-c6c10-unique-resolved-values!}))