; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-adapter-records
 [state]
 (clojure.core/let
  [{:keys
    [record
     context
     digest?
     context-envelope-valid?
     expected-record-hash
     expected-actual-path-binding-base
     expected-actual-path-binding
     runtime-source-file
     runtime-source-file-valid?
     runtime-source-file-hash
     contract-binding
     authority-binding
     adapter-record
     adapter-decisions
     adapter-actions
     validation
     io-write-active?
     capture-invoked?
     expected-adapter-record
     expected-decision-count
     expected-action-count
     expected-provider-ids
     expected-grant-ids
     required-decision-fields
     required-action-fields
     invocation
     verification-replay
     execution
     expected-validation-hash]}
   state]
  (clojure.core/and
   (=
    (dissoc
     (p15-s23-reference-runtime-authority
      nil
      {:observed-operation-set (if io-write-active? #{:println} #{})})
     :failure-injection)
    (:authority adapter-record))
   (= expected-decision-count (count adapter-decisions))
   (= expected-action-count (count adapter-actions))
   (every?
    (fn* [p1__1187#] (set/subset? required-decision-fields (set (keys p1__1187#))))
    adapter-decisions)
   (every?
    (fn* [p1__1188#] (set/subset? required-action-fields (set (keys p1__1188#))))
    adapter-actions)
   (every?
    (fn*
     [p1__1189#]
     (= (:decision-id p1__1189#) (p15-s23-reference-runtime-hash (dissoc p1__1189# :decision-id))))
    adapter-decisions)
   (every?
    (fn*
     [p1__1190#]
     (= (:record-id p1__1190#) (p15-s23-reference-runtime-hash (dissoc p1__1190# :record-id))))
    adapter-actions)
   (=
    (:record-hash adapter-record)
    (p15-s23-reference-runtime-hash (dissoc adapter-record :record-hash)))
   (= (:runtime-adapter-record-hash context) (:record-hash adapter-record))
   (true? (:reference-interpreter? adapter-record))
   (false? (:deployment-runtime? adapter-record))
   (true? (:clojure-seed-boundary? adapter-record))
   (false? (:self-hosted? adapter-record))
   (vector? (:runtime-decision-record-ids context))
   (= expected-decision-count (count (:runtime-decision-record-ids context)))
   (vector? (:runtime-action-record-ids context))
   (= expected-action-count (count (:runtime-action-record-ids context)))
   (= (mapv :decision-id adapter-decisions) (:runtime-decision-record-ids context))
   (= (mapv :record-id adapter-actions) (:runtime-action-record-ids context))
   (every? digest? (:runtime-decision-record-ids context))
   (every? digest? (:runtime-action-record-ids context))
   (digest? (:runtime-adapter-record-hash context)))))
