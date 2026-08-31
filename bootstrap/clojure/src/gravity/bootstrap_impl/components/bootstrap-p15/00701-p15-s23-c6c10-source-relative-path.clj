

;; ---------------------------------------------------------------------------
;; Gravity-owned bounded C6-C10 checked-core bridge (FL-P06-T02 slice)
;; ---------------------------------------------------------------------------

(def p15-s23-c6c10-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity")

(def p15-s23-c6c10-builder-function
  'c6-c10-build-checked-core-template)

(def p15-s23-c6c10-verifier-function
  'c6-c10-verify-checked-core-template)

(def p15-s23-c6c10-source-byte-count 141562)

(def p15-s23-c6c10-expected-source-content-hash
  "sha256:0299511c26c8b2a191309a4a4358528de397c88551ed98a2aed03d172067b2a5")

;; Filled from the dedicated canonical encoder below.  These pins are not the
;; legacy emitter's pr-str identities and are checked before either exported
;; Gravity function may execute.
(def p15-s23-c6c10-expected-plan-semantic-hash
  "sha256:c0abe759e32feb1810cbb477bba2e1db6bf41735b5e06beed8f0308babd24339")

(def p15-s23-c6c10-expected-functions-semantic-hash
  "sha256:79984c3b2cfd56f64ee161cb6d9611be55c2a8d614120c9ebd2477d5835a4aff")

(def p15-s23-c6c10-expected-builder-semantic-hash
  "sha256:1e14438321405f6f0126fab7ddebd6e4f1450e6074f5a832a56ec53ed64252f7")

(def p15-s23-c6c10-expected-verifier-semantic-hash
  "sha256:5695bc710e197df7b43efb9288480db43ae1e56a1da4d54a12180fd9e3b6906a")

(def p15-s23-c6c10-required-functions
  {p15-s23-c6c10-builder-function
   {:arity 1 :params ['input]}
   p15-s23-c6c10-verifier-function
   {:arity 3 :params ['input 'template 'requests]}})

(def p15-s23-c6c10-canonical-map-classes
  #{"clojure.lang.PersistentArrayMap"
    "clojure.lang.PersistentHashMap"
    "clojure.lang.PersistentTreeMap"})

(def p15-s23-c6c10-canonical-vector-classes
  #{"clojure.lang.PersistentVector"
    "clojure.lang.APersistentVector$SubVector"})

(def p15-s23-c6c10-canonical-set-classes
  #{"clojure.lang.PersistentHashSet"
    "clojure.lang.PersistentTreeSet"})

(def p15-s23-c6c10-canonical-list-classes
  #{"clojure.lang.PersistentList"
    "clojure.lang.PersistentList$EmptyList"})

(def ^:dynamic p15-s23-c6c10-max-carrier-nodes 65536)
(def ^:dynamic p15-s23-c6c10-max-carrier-depth 64)
(def ^:dynamic p15-s23-c6c10-max-container-width 128)
(def ^:dynamic p15-s23-c6c10-max-scalar-bytes 65536)
(def ^:dynamic p15-s23-c6c10-max-total-scalar-bytes (* 8 1024 1024))
(def p15-s23-c6c10-max-integer-bits 256)
(def ^:dynamic p15-s23-c6c10-max-digest-requests 2048)
(def p15-s23-c6c10-max-source-bytes (* 1024 1024))

(declare p15-s23-c6c10-canonical-digest)

(defn p15-s23-c6c10-upstream-diagnostic-contract
  [rule]
  (cond
    (= "L2-BUILTIN-ARITY" rule)
    {:stage :core-language-semantics
     :family :l2-core-language-semantics
     :document-id "L2"
     :expected-document (get stage1-bootstrap-governing-documents "L2")}

    (contains? (set c6-lowering-diagnostic-ids) rule)
    {:stage :core-lowering
     :family :c6-ast-core-lowering
     :document-id "C6"
     :expected-document c6-lowering-governing-document}

    (contains? (set c7-type-diagnostic-ids) rule)
    {:stage :type-check
     :family :c7-type-checker
     :document-id "C7"
     :expected-document c7-type-governing-document}

    (contains? (set c8-effect-diagnostic-ids) rule)
    {:stage :effect-check
     :family :c8-effect-checker
     :document-id "C8"
     :expected-document c8-effect-governing-document}

    (contains? (set c9-ownership-diagnostic-ids) rule)
    {:stage :ownership-lifetime-region-check
     :family :c9-ownership-checker
     :document-id "C9"
     :expected-document c9-ownership-governing-document}

    (contains? (set c10-safety-diagnostic-ids) rule)
    {:stage :safety-analysis
     :family :c10-safety-analysis
     :document-id "C10"
     :expected-document c10-safety-governing-document}

    :else
    {:stage :core-lowering
     :family :c6-ast-core-lowering
     :document-id "C6"
     :expected-document c6-lowering-governing-document}))

(defn p15-s23-c6c10-owned-upstream-data
  [data]
  (if (some? *p15-s23-c11-upstream-diagnostic-owner*)
    (assoc data
           ::c11-upstream-diagnostic-owner
           *p15-s23-c11-upstream-diagnostic-owner*)
    data))

(defn p15-s23-c6c10-diagnostic-semantic-span
  [span]
  (let [span (if (map? span) span {})
        position
        (fn [candidate]
          (let [candidate (if (map? candidate) candidate {})]
            (into {}
                  (keep (fn [key]
                          (let [value (get candidate key)]
                            (cond
                              (and (not= :column-unit key)
                                   (integer? value)
                                   (<= 0 value Long/MAX_VALUE))
                              [key (long value)]

                              (and (= :column-unit key)
                                   (keyword? value))
                              [key value]

                              :else nil))))
                  [:line :column :column-unit :char :byte])))]
    (cond-> {}
      (seq (position (:start span)))
      (assoc :start (position (:start span)))
      (seq (position (:end span)))
      (assoc :end (position (:end span)))
      (and (integer? (:byte-start span))
           (<= 0 (:byte-start span) Long/MAX_VALUE))
      (assoc :byte-start (long (:byte-start span)))
      (and (integer? (:byte-end span))
           (<= 0 (:byte-end span) Long/MAX_VALUE))
      (assoc :byte-end (long (:byte-end span)))
      (and (integer? (:form-index span))
           (<= 0 (:form-index span) Long/MAX_VALUE))
      (assoc :form-index (long (:form-index span))))))

(defn p15-s23-c6c10-diagnostic-semantic-id
  [candidate]
  (let [text (when (or (keyword? candidate)
                       (symbol? candidate)
                       (string? candidate))
               (str candidate))]
    (when (and (string? text) (<= (.length ^String text) 256))
      candidate)))