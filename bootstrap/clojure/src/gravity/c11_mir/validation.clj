(ns
 gravity.c11-mir.validation
 (:require
  [gravity.c11-mir.operations :as operations]
  [gravity.c11-mir.records :as records]))
(defn
 fail!
 [configuration id source-path subject extra]
 (operations/invoke
  :c11-mir-fail!
  (fn
   [rule path item fields]
   (operations/invoke
    :fail!
    (fn
     [r text payload]
     (throw (ex-info text (assoc (or payload {}) :id r))))
    rule
    (operations/invoke :c11-mir-message records/message rule)
    (merge
     {:diagnostic-family :c11-mir-specification,
      :remediation
      "Regenerate target-independent MIR from C10 safety-checked core with type, effect, ownership, capability, safety, proof, profile, target, and source-origin facts.",
      :block (or (:block item) :fixture/block),
      :source-span
      (or
       (:source-span item)
       (get-in item [:source :span])
       (records/source-span path 0)),
      :stage :mir-construction,
      :origin-chain
      (or
       (:generated-origin item)
       (get-in item [:source :origin-chain])
       []),
      :operation-id
      (or (:operation-id item) (:op-id item) :fixture/operation),
      :function (or (:function item) :fixture/function),
      :missing-fact (:missing-fact item),
      :mir-module (or (:mir-module item) :fixture/mir-module),
      :document-id "C11",
      :target-request (or (:target-request item) (:target item)),
      :expected-document (:c11-mir-governing-document configuration),
      :profile (:profile item)}
     fields)))
  id
  source-path
  subject
  extra))
(defn
 validate-overrides!
 [configuration source-path module overrides]
 (when-let
  [kind (:fail overrides)]
  (when-let
   [id (get (:c11-mir-override-diagnostics configuration) kind)]
   (fail!
    configuration
    id
    source-path
    {:source-span (records/source-span source-path 0),
     :operation-id (keyword "fixture" (name kind)),
     :profile (:profile module),
     :target-request (:target module),
     :missing-fact kind}
    {:missing-fields [kind]}))))
(defn
 verifier
 [configuration module operations* data-flow anchors diagnostics]
 (let
  [families
   (set (map :family operations*))
   present
   records/present?
   diags?
   (=
    (set (:c11-mir-diagnostic-ids configuration))
    (set (map :diagnostic (:diagnostics diagnostics))))]
  {:module-shape-valid? (= :gravity/mir-module (:artifact module)),
   :operation-family-coverage-complete?
   (=
    (set (:c11-mir-required-operation-families configuration))
    families),
   :blocks-terminate?
   (every?
    (fn* [p1__138#] (present (:terminator p1__138#)))
    (mapcat (comp vals :blocks) (vals (:functions module)))),
   :domain-anchors-valid?
   (every?
    (fn*
     [p1__144#]
     (and
      (present (:anchor-id p1__144#))
      (present (:fallback p1__144#))))
    anchors),
   :origins-linked?
   (every?
    (fn* [p1__143#] (present (get-in p1__143# [:source :span])))
    operations*),
   :status
   (if
    (and
     (= :gravity/mir-module (:artifact module))
     (every? (fn* [p1__146#] (present (:type p1__146#))) operations*)
     (every?
      (fn*
       [p1__147#]
       (or
        (empty? (:effects p1__147#))
        (not= :none (:ordering p1__147#))))
      operations*)
     (every?
      (fn* [p1__148#] (present (get-in p1__148# [:facts :safety])))
      operations*)
     (every?
      (fn* [p1__149#] (present (get-in p1__149# [:source :span])))
      operations*)
     (every?
      (fn*
       [p1__150#]
       (and
        (present (:anchor-id p1__150#))
        (present (:fallback p1__150#))))
      anchors)
     (=
      (set (:c11-mir-required-operation-families configuration))
      families)
     diags?)
    :passed
    :failed),
   :types-present?
   (every? (fn* [p1__140#] (present (:type p1__140#))) operations*),
   :safety-linked?
   (every?
    (fn* [p1__142#] (present (get-in p1__142# [:facts :safety])))
    operations*),
   :effect-ordering-present?
   (every?
    (fn*
     [p1__141#]
     (or
      (empty? (:effects p1__141#))
      (not= :none (:ordering p1__141#))))
    operations*),
   :artifact :gravity/c11-mir-verifier-report,
   :diagnostics-covered? diags?,
   :target-independent?
   (not-any?
    (fn* [p1__145#] (= :target-specific (:family p1__145#)))
    operations*),
   :dominance-valid?
   (every?
    (fn* [p1__139#] (= :passed (:dominance-status p1__139#)))
    data-flow)}))
(defn
 proof
 [artifact]
 (let
  [v (:mir-verifier-report artifact)]
  {:operation-family-coverage-complete?
   (:operation-family-coverage-complete? v),
   :domain-anchors-valid? (:domain-anchors-valid? v),
   :origins-linked? (:origins-linked? v),
   :safety-outcomes-linked? (:safety-linked? v),
   :blocks-terminated? (:blocks-terminate? v),
   :status :complete,
   :effect-ordering-present? (:effect-ordering-present? v),
   :operations-typed? (:types-present? v),
   :diagnostics-covered? (:diagnostics-covered? v),
   :module-serialized? (:module-shape-valid? v),
   :verifier-passed? (= :passed (:status v)),
   :target-independent? (:target-independent? v)}))
(defn
 validate!
 [configuration source-path artifact]
 (let
  [proof* (operations/invoke :c11-mir-capability-proof proof artifact)]
  (doseq
   [[field id]
    [[:module-serialized? "C11-MODULE"]
     [:blocks-terminated? "C11-BLOCK"]
     [:operations-typed? "C11-TYPE"]
     [:effect-ordering-present? "C11-EFFECT"]
     [:safety-outcomes-linked? "C11-SAFETY"]
     [:origins-linked? "C11-ORIGIN"]
     [:domain-anchors-valid? "C11-DOMAIN"]
     [:target-independent? "C11-TARGET-LEAK"]
     [:operation-family-coverage-complete? "C11-VERIFY"]
     [:diagnostics-covered? "C11-VERIFY"]
     [:verifier-passed? "C11-VERIFY"]]]
   (when-not
    (get proof* field)
    (fail!
     configuration
     id
     source-path
     {:stage :mir-construction}
     {:missing-fields [field]}))))
 :complete)
