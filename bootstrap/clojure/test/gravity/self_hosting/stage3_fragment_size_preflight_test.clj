(ns gravity.self-hosting.stage3-fragment-size-preflight-test
  "Cheap, exact C7 fragment-size preflight.

  The loader reads one bounded C7 source snapshot through the authenticated
  C2/C3 reader, applies the same public SH-05 `sh05-expanded-form` projection
  used by the authority, removes namespace forms in source order, and counts
  roots recursively with public `sh07-core-children`.  It intentionally does
  not execute the expensive macro carrier, SH-06, or SH-07 artifact builders;
  source/plan identity remains the responsibility of the fixed source-plan
  contract batch."
  (:require [clojure.test :refer [deftest is]]))

(def ^:private c7-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private maximum-fragment-forms 1024)

;; Bounded historical regression fixture.  It is intentionally independent of
;; the live manifest: ordinal 178 from the f986 snapshot counted 1227 records
;; and must continue to be rejected by the exact child semantics.
(def ^:private historical-f986-fixture
  {:source-sha256 "sha256:4f9ff8f11b347afc17984acd558fdbb925cdbc8e1f1e329997ff7a04930ac320"
   :ordinal 178
   :expected-observed 1227
   :maximum maximum-fragment-forms})

(def ^:private historical-f986-expanded-root-source
  "(defn sh08-ft-function-type-core-artifact [core-artifact bindings]\n  (if (map? core-artifact)\n    (let [shape (sh08-validate-core-shape core-artifact)\n          module (if (map? (get core-artifact :module))\n                   (get core-artifact :module)\n                   {})\n          products (sh08-ft-products-valid?\n                    core-artifact shape bindings)]\n      (if (= (get products :status) :accepted)\n        (let [recursive-proof\n              (if (> (count (get products :recursion-components)) 0)\n                (sh08-ft-recursive-proof\n                 (get shape :nodes)\n                 (get products :definitions)\n                 bindings\n                 (get products :function-records)\n                 (get products :calls)\n                 (get products :call-edges)\n                 (get products :recursion-components)\n                 module)\n                {:status :not-applicable :reason :no-recursive-component})\n              higher-order-proof\n              (sh08-ft-higher-order-proof\n               (get shape :nodes)\n               (get products :definitions)\n               bindings\n               (get products :function-records)\n               (get products :calls)\n               (get products :call-edges)\n               module)]\n          (let [unsupported-higher-order?\n                (= :unsupported-higher-order-primitive-type\n                   (get higher-order-proof :reason))]\n            (if (if unsupported-higher-order?\n                  true\n                  (if (> (count (get products :recursion-components)) 0)\n                (if (= :accepted (get recursive-proof :status)) false true)\n                false))\n              (if unsupported-higher-order?\n                {:artifact :gravity/sh08-function-typed-core-template\n                 :schema-version 3\n                 :status :rejected\n                 :scope :capture-free-higher-order-fixed-arity-one-hop\n                 :authentication-status\n                 :host-resolved-b47-verification-boundary\n                 :module module\n                 :sh07-shaped-artifact-id (get core-artifact :artifact-id)\n                 :diagnostics\n                 [(sh08-ft-diagnostic\n                   \"C7-ANNOTATION\"\n                   (get higher-order-proof :node)\n                   module\n                   :supported-higher-order-primitive-type\n                   (get higher-order-proof :actual)\n                   :unsupported-higher-order-primitive-type\n                   {})]\n                 :pending [:recursive-type-annotations\n                           :higher-order-functions :records :unions\n                           :protocols :generics :casts :dynamic-boundaries]}\n                {:artifact :gravity/sh08-function-typed-core-template\n                 :schema-version 3\n                 :status :rejected\n                 :scope :first-order-fixed-arity-functions-locals-calls\n                 :authentication-status\n                 :host-resolved-b47-verification-boundary\n                 :module module\n                 :sh07-shaped-artifact-id (get core-artifact :artifact-id)\n                 :diagnostics\n                 [(sh08-ft-recursive-diagnostic recursive-proof module)]\n                 :pending [:recursive-type-annotations\n                           :higher-order-functions :records :unions\n                           :protocols :generics :casts :dynamic-boundaries]})\n              (let [base-function-skeletons\n                (loop [remaining (get products :function-records)\n                       result {}]\n                  (if (= (count remaining) 0)\n                    result\n                    (let [record (first remaining)]\n                      (recur\n                       (rest remaining)\n                       (assoc result\n                              (get record :function-syntax-id)\n                               (sh08-ft-function-type-skeleton\n                               record module))))))\n                nodes (get products :node-table)\n                recursive?\n                (= :accepted (get recursive-proof :status))\n                higher-order?\n                (= :accepted (get higher-order-proof :status))\n                function-skeletons\n                (if recursive?\n                  (loop [remaining (keys base-function-skeletons)\n                         result base-function-skeletons]\n                    (if (= (count remaining) 0)\n                      result\n                      (let [function-id (first remaining)\n                            function (get result function-id)]\n                        (if (= function-id (get recursive-proof :function-syntax-id))\n                          (recur\n                           (rest remaining)\n                           (assoc result function-id\n                                  (sh08-ft-recursive-complete-function\n                                   function recursive-proof)))\n                          (recur (rest remaining) result)))))\n                  (if higher-order?\n                    (loop [remaining (keys base-function-skeletons)\n                           result base-function-skeletons]\n                      (if (= (count remaining) 0)\n                        result\n                        (let [function-id (first remaining)\n                              function (get result function-id)]\n                          (if (= function-id\n                                 (get (get (get higher-order-proof :identity)\n                                           :record)\n                                      :function-syntax-id))\n                            (recur\n                             (rest remaining)\n                             (assoc result function-id\n                                    (sh08-ft-higher-order-complete-function\n                                     function\n                                     (get higher-order-proof\n                                          :identity-function-type))))\n                            (if (= function-id\n                                   (get (get (get higher-order-proof :apply)\n                                             :record)\n                                        :function-syntax-id))\n                              (recur\n                               (rest remaining)\n                               (assoc result function-id\n                                      (sh08-ft-higher-order-complete-function\n                                       function\n                                       (get higher-order-proof\n                                            :apply-function-type))))\n                              (recur (rest remaining) result))))))\n                    base-function-skeletons))\n                raw-inference\n                (sh08-ft-infer-acyclic-with-context\n                 (get shape :nodes)\n                 nodes\n                 (get products :lexical-bindings)\n                 (get products :definitions)\n                 (get products :calls)\n                 (get products :call-edges)\n                 function-skeletons\n                 (if recursive?\n                   {(get (get recursive-proof :definition) :binding-id)\n                    (get function-skeletons\n                         (get recursive-proof :function-syntax-id))}\n                   (if higher-order?\n                     (get higher-order-proof :initial-binding-types)\n                     {}))\n                 (sh08-ft-inference-round-bound\n                  (get shape :nodes)\n                  (get products :function-records)\n                  (get products :calls)\n                  (get products :lexical-bindings))\n                 (if recursive?\n                   recursive-proof\n                   (if higher-order?\n                     higher-order-proof\n                     {})))\n                unknown-node\n                (if (= (get raw-inference :convergence-status)\n                       :converged)\n                  (sh08-ft-first-unknown-node\n                   (get shape :nodes)\n                   (get raw-inference :type-table))\n                  nil)\n                inference\n                (if (map? unknown-node)\n                  (assoc\n                   raw-inference\n                   :diagnostics\n                   (conj\n                    (get raw-inference :diagnostics)\n                    {:node unknown-node\n                     :rule \"C7-ANNOTATION\"\n                     :reason :unsupported-or-unresolved-core-node-type\n                     :expected :concrete-node-type-or-diagnostic\n                     :actual :gravity.type/unknown}))\n                  raw-inference)\n                functions\n                (sh08-ft-function-facts\n                 (get inference :function-types)\n                 nodes module)\n                final-functions\n                (loop [remaining functions values []]\n                  (if (= (count remaining) 0)\n                    values\n                    (if (if (sh08-ft-every-known?\n                             (loop [params (get (first remaining)\n                                                :parameters)\n                                    result []]\n                               (if (= (count params) 0)\n                                 (conj result\n                                       (get (first remaining) :return))\n                                 (recur\n                                  (rest params)\n                                  (conj result (get (first params) :type)))))\n                             )\n                          true\n                          false)\n                      (recur (rest remaining) (conj values (first remaining)))\n                      (recur\n                       (rest remaining)\n                       (conj values\n                             (assoc (first remaining)\n                                    :status :unresolved))))))]\n            (if (> (count (get inference :diagnostics)) 0)\n              {:artifact :gravity/sh08-function-typed-core-template\n               :schema-version 3\n               :status :rejected\n               :scope :first-order-fixed-arity-functions-locals-calls\n               :authentication-status\n               :host-resolved-b47-verification-boundary\n               :module module\n               :sh07-shaped-artifact-id (get core-artifact :artifact-id)\n               :diagnostics\n               (sh08-ft-render-inference-diagnostics\n                (get inference :diagnostics) nodes module)\n               :pending [:recursive-type-annotations\n                         :higher-order-functions :records :unions\n                         :protocols :generics :casts :dynamic-boundaries]}\n              (if (loop [remaining final-functions]\n                    (if (= (count remaining) 0)\n                      true\n                      (if (= (get (first remaining) :status) :inferred)\n                        (recur (rest remaining))\n                        false)))\n                (let [local-facts\n                      (sh08-ft-local-facts\n                       (get products :lexical-bindings)\n                       (get inference :type-table)\n                       (get inference :binding-types)\n                       nodes module)\n                      call-facts\n                      (sh08-ft-call-facts-with-context\n                       (get products :calls)\n                       (get products :call-edges)\n                       nodes\n                       (get inference :type-table)\n                       (get inference :function-types)\n                       module\n                       higher-order-proof)\n                      combined-constraints\n                      (if recursive?\n                        (sh08-ft-add-values-to-set\n                         (sh08-ft-recursive-constraint-ledger recursive-proof)\n                         (get inference :constraints))\n                        (get inference :constraints))\n                      convergence\n                      {:status (get inference :convergence-status)\n                       :round-count (get inference :round-count)\n                       :round-bound (get inference :round-bound)\n                       :proof\n                       :monotone-finite-type-fact-propagation}\n                      identity-input-base\n                      {:domain (if recursive?\n                                :gravity/sh08-recursive-function-typed-core-v1\n                                :gravity/sh08-function-typed-core-v3)\n                       :sh07-shaped-artifact-id\n                       (get core-artifact :artifact-id)\n                       :module module\n                       :function-records\n                       (get products :function-records)\n                       :call-edges (get products :call-edges)\n                       :recursion-components\n                       (get products :recursion-components)\n                       :lexical-bindings\n                       (get products :lexical-bindings)\n                       :function-type-table final-functions\n                       :local-binding-facts local-facts\n                       :call-type-facts call-facts\n                       :constraint-ledger combined-constraints\n                       :type-table (get inference :type-table)\n                       :convergence convergence\n                       :pending [:recursive-type-annotations\n                                 :higher-order-functions :records :unions\n                                 :protocols :generics :casts\n                                 :dynamic-boundaries]}\n                      identity-input\n                      (if recursive?\n                        (assoc\n                         (assoc identity-input-base\n                                :pending\n                                (sh08-ft-recursive-pending\n                                 (get identity-input-base :pending)))\n                         :recursive-proof\n                         (sh08-ft-recursive-proof-input recursive-proof))\n                        (if higher-order?\n                          (assoc\n                           (assoc\n                            (assoc identity-input-base\n                                   :domain\n                                   :gravity/sh08-authoritative-higher-order-type-v1)\n                             :pending\n                             (sh08-ft-higher-order-pending\n                              (get identity-input-base :pending)))\n                           :higher-order-proof\n                           (sh08-ft-higher-order-proof-input\n                            higher-order-proof))\n                          identity-input-base))\n                      typed-core-base\n                      {:artifact :gravity/typed-core\n                       :core-input (get core-artifact :artifact-id)\n                       :module module\n                       :types (get inference :type-table)\n                       :constraints combined-constraints\n                       :function-types final-functions\n                       :local-bindings local-facts\n                       :calls call-facts\n                       :diagnostics []}\n                      typed-core\n                      (if recursive?\n                        (assoc\n                         (assoc typed-core-base\n                                :recursive-proof\n                                (sh08-ft-recursive-proof-input\n                                 recursive-proof))\n                         :recursive-call-facts\n                         (sh08-ft-recursive-call-proof-facts\n                          recursive-proof call-facts))\n                        (if higher-order?\n                          (assoc\n                           (assoc typed-core-base\n                                  :higher-order-proof\n                                  (sh08-ft-higher-order-proof-input\n                                   higher-order-proof))\n                           :higher-order-call-facts\n                           (sh08-ft-higher-order-call-proof-facts\n                            higher-order-proof))\n                          typed-core-base))]\n                  (sh08-ft-recursive-result-fields\n                   (sh08-ft-higher-order-result-fields\n                   {:artifact :gravity/sh08-function-typed-core-template\n                   :schema-version 3\n                   :status :accepted\n                   :scope (if recursive?\n                            :bounded-named-self-recursive-positive-fixed-arity-positional-literal-base\n                            (if higher-order?\n                              :capture-free-higher-order-fixed-arity-one-hop\n                              :first-order-fixed-arity-functions-locals-calls))\n                   :authentication-status\n                   :host-resolved-b47-verification-boundary\n                   :module module\n                   :sh07-shaped-artifact-id\n                   (get core-artifact :artifact-id)\n                   :function-products\n                   {:function-records (get products :function-records)\n                    :call-edges (get products :call-edges)\n                    :recursion-components\n                    (get products :recursion-components)\n                    :lexical-bindings\n                    (get products :lexical-bindings)}\n                   :function-type-table final-functions\n                   :local-binding-facts local-facts\n                   :call-type-facts call-facts\n                   :constraint-ledger combined-constraints\n                   :type-table (get inference :type-table)\n                   :convergence convergence\n                   :typed-core typed-core\n                   :diagnostics []\n                   :artifact-id-request identity-input\n                   :identity-input identity-input\n                   :identity-resolution :coordinator-digest-required\n                   :provenance (get core-artifact :provenance)\n                   :pending (get identity-input :pending)}\n                   higher-order-proof)\n                   recursive-proof\n                   call-facts))\n                {:artifact :gravity/sh08-function-typed-core-template\n                 :schema-version 3\n                 :status :rejected\n                 :scope :first-order-fixed-arity-functions-locals-calls\n                 :authentication-status\n                 :host-resolved-b47-verification-boundary\n                 :module module\n                 :sh07-shaped-artifact-id (get core-artifact :artifact-id)\n                 :diagnostics\n                 [(sh08-ft-diagnostic\n                   \"C7-GENERIC\" {}\n                   module :fully-inferred-function-types\n                   final-functions\n                   :unresolved-function-type {})]\n                 :pending [:recursive-type-annotations\n                           :higher-order-functions :records :unions\n                           :protocols :generics :casts :dynamic-boundaries]}))))))\n        {:artifact :gravity/sh08-function-typed-core-template\n         :schema-version 3\n         :status :rejected\n         :scope :first-order-fixed-arity-functions-locals-calls\n         :authentication-status\n         :host-resolved-b47-verification-boundary\n         :module module\n         :sh07-shaped-artifact-id (get core-artifact :artifact-id)\n         :diagnostics\n         [(sh08-ft-diagnostic\n           \"C7-VERIFY\" {}\n           module :coherent-b47-function-products\n           (get products :reason)\n           (get products :reason) {})]\n         :pending [:recursive-type-annotations\n                   :higher-order-functions :records :unions\n                   :protocols :generics :casts :dynamic-boundaries]}))\n    {:artifact :gravity/sh08-function-typed-core-template\n     :schema-version 3\n     :status :rejected\n     :scope :first-order-fixed-arity-functions-locals-calls\n     :authentication-status\n     :host-resolved-b47-verification-boundary\n     :module {}\n     :diagnostics\n     [(sh08-ft-diagnostic\n       \"C7-VERIFY\" {} {} :sh07-canonical-core-map core-artifact\n       :sh07-shaped-core-map-required {})]}));\n\n")

(def ^:dynamic *artifact-loader*
  "Injection seam returning `{:expanded-source-forms ... :children-fn ...}`."
  nil)

(def ^:dynamic *authority-stream-loader*
  "Optional differential seam.  A fixture may return a SH-05 artifact whose
  `:expanded-syntax-stream` forms must equal this preflight's forms.  The
  production path leaves it nil so no full macro expansion is executed."
  nil)

(defn- resolve-var
  [qualified-symbol]
  (or (requiring-resolve qualified-symbol)
      (throw
       (ex-info "Required Stage3 preflight helper is absent"
                {:id "STAGE3-FRAGMENT-HELPER-ABSENT"
                 :symbol qualified-symbol}))))

(defn- sha256-text
  [source-text]
  (let [bytes (.getBytes ^String source-text "UTF-8")]
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and (int %) 0xff))
                     (.digest
                      (doto (java.security.MessageDigest/getInstance "SHA-256")
                        (.update bytes))))))))

(defn- sha256-bytes
  [bytes]
  (let [digest (doto (java.security.MessageDigest/getInstance "SHA-256")
                 (.update ^bytes bytes))]
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and (int %) 0xff))
                     (.digest digest))))))

(defn- read-source-snapshot
  [source-path]
  (let [maximum-bytes 1048576
        path (.toPath (java.io.File. source-path))
        no-follow (into-array java.nio.file.LinkOption
                              [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        open-options (into-array java.nio.file.OpenOption
                                 [java.nio.file.StandardOpenOption/READ
                                  java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (java.nio.file.Files/readAttributes
         path java.nio.file.attribute.BasicFileAttributes no-follow)]
    (when (or (java.nio.file.Files/isSymbolicLink path)
              (not (.isRegularFile attributes)))
      (throw
       (ex-info "Stage3 preflight source must be a regular non-symlink file"
                {:id "STAGE3-FRAGMENT-SOURCE-FILE"
                 :source-path source-path})))
    (when (> (.size attributes) maximum-bytes)
      (throw
       (ex-info "Stage3 preflight source snapshot exceeds bounded input"
                {:id "STAGE3-FRAGMENT-SOURCE-BYTES"
                 :source-path source-path
                 :observed (.size attributes)
                 :maximum maximum-bytes})))
    (with-open [channel (java.nio.channels.FileChannel/open path open-options)]
      (let [channel-size-before (.size channel)
            buffer (java.nio.ByteBuffer/allocate (inc maximum-bytes))]
        (loop []
          (let [read-count (.read channel buffer)]
            (when (and (pos? read-count)
                       (< (.position buffer) (inc maximum-bytes)))
              (recur))))
        (let [position (.position buffer)
              channel-size-after (.size channel)
              attributes-after
              (java.nio.file.Files/readAttributes
               path java.nio.file.attribute.BasicFileAttributes no-follow)]
            (when (or (>= position (inc maximum-bytes))
                    (not= channel-size-before channel-size-after)
                    (not= channel-size-before position)
                    (not= (.size attributes) (.size attributes-after))
                    (not= (.fileKey attributes) (.fileKey attributes-after))
                    (not= (.lastModifiedTime attributes)
                          (.lastModifiedTime attributes-after))
                    (not= (.size attributes) position))
            (throw
             (ex-info "Stage3 preflight source changed during bounded read"
                      {:id "STAGE3-FRAGMENT-SOURCE-MUTATED"
                       :source-path source-path
                       :before-size (.size attributes)
                       :after-size (.size attributes-after)
                       :observed-bytes position})))
          (.flip buffer)
          (let [bytes (byte-array position)]
            (.get buffer bytes)
            (let [decoder (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                            (.onMalformedInput
                             java.nio.charset.CodingErrorAction/REPORT)
                            (.onUnmappableCharacter
                             java.nio.charset.CodingErrorAction/REPORT))
                  source-text
                  (try
                    (str (.decode decoder (java.nio.ByteBuffer/wrap bytes)))
                    (catch java.nio.charset.CharacterCodingException error
                      (throw
                       (ex-info "Stage3 preflight source is not strict UTF-8"
                                {:id "STAGE3-FRAGMENT-SOURCE-UTF8"
                                 :source-path source-path}
                                error))))]
              {:bytes bytes
               :source-text source-text
               :source-sha256 (sha256-bytes bytes)})))))))

(defn- source-file
  []
  (let [repository-root (or (System/getProperty "gravity.repository.root") ".")]
    (.getPath (java.io.File. repository-root c7-source-relative-path))))

(defn canonical-form-tree-size
  "Count a root and all descendants with canonical SH-07 child semantics."
  [children-fn root]
  (when-not (ifn? children-fn)
    (throw
     (ex-info "Stage3 preflight requires canonical sh07-core-children"
              {:id "STAGE3-FRAGMENT-CHILDREN-HELPER-MISSING"})))
  (loop [frontier [root]
         count 0]
    (if (empty? frontier)
      count
      (let [value (peek frontier)
            tail (pop frontier)
            children (vec (children-fn value))]
        (recur (into tail children) (inc count))))))

(defn- root-form-name
  [form]
  (let [head (when (coll? form) (first form))]
    (when (#{'def 'defn} head)
      (second form))))

(defn differential-expanded-stream!
  "Prove a fixture's C3/sh05-expanded-form forms equal an SH-05 stream.

  This is a small regression seam rather than the production preflight path;
  callers pass the exact `(:form ...)` projection from a macro artifact."
  [expanded-forms macro-artifact]
  (let [authority-forms (mapv :form (:expanded-syntax-stream macro-artifact))]
    (when-not (= (vec expanded-forms) authority-forms)
      (throw
       (ex-info "C3/sh05-expanded-form stream differs from SH-05 carrier"
                {:id "STAGE3-FRAGMENT-SH05-DIFFERENTIAL"
                 :expected authority-forms
                 :actual (vec expanded-forms)})))
    {:status :passed
     :form-count (count authority-forms)
     :stream-equal? true
     :authority :non-authoritative
     :authoritative? false}))

(defn- default-artifact-loader
  []
  (let [source-path (source-file)
        snapshot-before (read-source-snapshot source-path)
        source-text (:source-text snapshot-before)
        c3-artifact ((resolve-var
                      'gravity.bootstrap/compiler-c3-syntax-source-artifact)
                     source-path source-text)
        _ ((resolve-var 'gravity.bootstrap/c3-syntax-validate!)
           source-path c3-artifact)
        snapshot-after (read-source-snapshot source-path)
        _ (when-not (= (:source-sha256 snapshot-before)
                       (:source-sha256 snapshot-after))
            (throw
             (ex-info "C7 source changed during exact preflight"
                      {:id "STAGE3-FRAGMENT-SOURCE-MUTATED"
                       :source-path source-path
                       :before (:source-sha256 snapshot-before)
                       :after (:source-sha256 snapshot-after)})))
        forms (vec (get-in c3-artifact
                           [:c2-reader-artifact :parsed-semantic-values]))
        expanded-form-fn (resolve-var 'gravity.bootstrap/sh05-expanded-form)
        expanded-forms (mapv expanded-form-fn forms)
        source-unit (get-in c3-artifact
                            [:c2-reader-artifact :source-unit-record])]
    (when (and (:bytes-hash source-unit)
               (not= (:bytes-hash source-unit)
                     (:source-sha256 snapshot-before)))
      (throw
       (ex-info "C3 source identity differs from bounded preflight snapshot"
                {:id "STAGE3-FRAGMENT-SOURCE-IDENTITY"
                 :source-path source-path
                 :snapshot (:source-sha256 snapshot-before)
                 :c3 (:bytes-hash source-unit)})))
    {:c3-artifact c3-artifact
     :source-forms forms
     :expanded-source-forms expanded-forms
     :source-path source-path
     :source-sha256 (or (:bytes-hash source-unit)
                        (:source-sha256 snapshot-before)
                        (sha256-text source-text))
     :source-id (:source-id source-unit)
     :children-fn (resolve-var 'gravity.bootstrap/sh07-core-children)
     :expanded-form-fn expanded-form-fn}))

(defn fragment-root-records
  "Return exact root sizes from C2/C3 forms and public child semantics.

  The namespace form is removed exactly as SH-07 removes it.  No projected
  Stage2 plan `:module` forms are consulted: that projection intentionally
  drops source forms."
  [{:keys [expanded-source-forms source-path source-sha256 source-id
           children-fn]}]
  (let [forms (vec expanded-source-forms)
        ns-forms (filter #(and (seq? %) (= 'ns (first %))) forms)
        executable (vec (remove #(and (seq? %) (= 'ns (first %))) forms))]
    (when-not (= 1 (count ns-forms))
      (throw
       (ex-info "C7 source must contain exactly one namespace form"
                {:id "STAGE3-FRAGMENT-NS-FORM"
                 :source-path source-path
                 :namespace-form-count (count ns-forms)})))
    (mapv
     (fn [ordinal form]
       {:ordinal ordinal
        :function (root-form-name form)
        :observed (canonical-form-tree-size children-fn form)
        :maximum maximum-fragment-forms
        :source-path source-path
        :source-sha256 source-sha256
        :source-id source-id})
     (range)
     executable)))

(defn source-control-form-arity-errors
  "Return every reader form whose `if` head does not have exactly four items.

  The traversal is pure and injection-friendly: it uses the same canonical
  child function as the fragment metric and records a stable path for each
  malformed form."
  [children-fn forms]
  (when-not (ifn? children-fn)
    (throw
     (ex-info "Stage3 source-control preflight requires canonical children"
              {:id "STAGE3-FRAGMENT-CHILDREN-HELPER-MISSING"})))
  (loop [pending (mapv (fn [ordinal form] [ordinal [] form])
                       (range) forms)
         errors []]
    (if (empty? pending)
      (vec errors)
      (let [[ordinal path form] (first pending)
            tail (subvec pending 1)
            malformed? (and (seq? form)
                            (= 'if (first form))
                            (not= 4 (count form)))
            errors' (if malformed?
                      (conj errors
                            {:ordinal ordinal
                             :path path
                             :observed (count form)
                             :expected 4})
                      errors)
            children (vec (children-fn form))
            next-items
            (map-indexed
             (fn [index child]
               [ordinal (conj path index) child])
             children)]
        (recur (into tail next-items) errors')))))

(defn check-source-control-form-arity!
  [loaded]
  (let [errors (source-control-form-arity-errors
                (:children-fn loaded)
                (:expanded-source-forms loaded))]
    (when-let [first-error (first errors)]
      (throw
       (ex-info "C7 source contains an `if` form with the wrong arity"
                (merge {:id "STAGE3-FRAGMENT-SOURCE-CONTROL-IF-ARITY"
                        :source-path (:source-path loaded)
                        :source-sha256 (:source-sha256 loaded)}
                       first-error))))
    {:status :passed
     :checked-form-count (count (:expanded-source-forms loaded))
     :errors []
     :source-path (:source-path loaded)
     :source-sha256 (:source-sha256 loaded)
     :authority :non-authoritative
     :authoritative? false}))

(defn- historical-f986-regression!
  [children-fn expanded-form-fn]
  (when-not (ifn? expanded-form-fn)
    (throw
     (ex-info "Stage3 historical fragment regression requires sh05-expanded-form"
              {:id "STAGE3-FRAGMENT-SH05-HELPER-MISSING"})))
  (let [{:keys [expected-observed] :as fixture}
        historical-f986-fixture
        form (read-string historical-f986-expanded-root-source)
        expanded-form (expanded-form-fn form)
        observed (canonical-form-tree-size children-fn expanded-form)]
    (when-not (= expected-observed observed)
      (throw
       (ex-info "Historical f986 fragment fixture changed"
                {:id "STAGE3-FRAGMENT-HISTORICAL-FIXTURE"
                 :expected expected-observed
                 :observed observed})))
    (assoc fixture
           :function (root-form-name expanded-form)
           :observed observed)))

(defn check-fragment-root-bound!
  "Fail closed if an exact root exceeds the C6 1024-record bound."
  [loaded-plan]
  (let [records (fragment-root-records loaded-plan)
        offending (first (filter #(> (:observed %) maximum-fragment-forms)
                                 records))
        differential
        (when *authority-stream-loader*
          (differential-expanded-stream!
           (:expanded-source-forms loaded-plan)
           (*authority-stream-loader* loaded-plan)))]
    (when offending
      (throw
       (ex-info "C7 Stage2 fragment root exceeds the C6 bound"
                {:id "STAGE3-FRAGMENT-ROOT-BOUND"
                 :function (:function offending)
                 :ordinal (:ordinal offending)
                 :observed (:observed offending)
                 :maximum maximum-fragment-forms
                 :source-path (:source-path offending)
                 :source-sha256 (:source-sha256 offending)
                 :source-id (:source-id offending)})))
    {:status :passed
     :exact? true
     :maximum maximum-fragment-forms
     :fragment-count (count records)
     :records records
     :first-offender nil
     :source-path (:source-path loaded-plan)
     :source-sha256 (:source-sha256 loaded-plan)
     :source-id (:source-id loaded-plan)
     :differential differential
     :regression-evidence
     {:historical-f986-first-offender
      (historical-f986-regression!
       (:children-fn loaded-plan)
       (:expanded-form-fn loaded-plan))}
     :authority :non-authoritative
     :authoritative? false}))

(defn run-fragment-size-preflight!
  []
  (check-fragment-root-bound!
   (if *artifact-loader* (*artifact-loader*) (default-artifact-loader))))

(deftest stage3-fragment-size-preflight
  (let [result (run-fragment-size-preflight!)]
    (is (= :passed (:status result)))
    (is (true? (:exact? result)))
    (is (every? #(<= (:observed %) (:maximum %)) (:records result)))
    (is (string? (:source-path result)))
    (is (string? (:source-sha256 result)))
    (is (= :non-authoritative (:authority result)))
    (is (false? (:authoritative? result)))))
