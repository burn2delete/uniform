; Semantic decomposition of HEAD reader line 10816.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-early-record-safety-conformance-call!-dispatch!
 [kind__1412__auto__
  checker__1413__auto__
  record__1414__auto__
  args__1415__auto__
  handlers__1416__auto__]
 (clojure.core/loop
  [remaining__1417__auto__ (clojure.core/seq handlers__1416__auto__)]
  (clojure.core/when-let
   [handler__1418__auto__ (clojure.core/first remaining__1417__auto__)]
   (clojure.core/let
    [result__1419__auto__
     (handler__1418__auto__
      kind__1412__auto__
      checker__1413__auto__
      record__1414__auto__
      args__1415__auto__)]
    (if
     (clojure.core/identical?
      semantic-early-record-safety-conformance-call!-unhandled
      result__1419__auto__)
     (recur (clojure.core/next remaining__1417__auto__))
     result__1419__auto__)))))

(defn
 record-safety-conformance-call!
 [checker ctx node operator args effects capabilities return-type spec]
 (when-let
  [kind (:safety-conformance-kind spec)]
  (let
   [record
    {:capabilities capabilities,
     :safety-mode (:safety @ctx),
     :source-span (:source-span node),
     :safety-conformance-kind kind,
     :return-type return-type,
     :generated-origin-chain (:generated-origin node),
     :node-id (:node-id node),
     :effects effects,
     :operator operator,
     :target (:target @ctx),
     :profile (:profile @ctx)}]
   (semantic-early-record-safety-conformance-call!-dispatch!
    kind
    checker
    record
    args
    [semantic-early-record-safety-conformance-call!-safe12!
     semantic-early-record-safety-conformance-call!-safe13!
     semantic-early-record-safety-conformance-call!-safe15!
     semantic-early-record-safety-conformance-call!-safe16!])
   record)))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-record-safety-conformance-call!-unhandled
    semantic-early-record-safety-conformance-call!-dispatch!
    semantic-early-record-safety-conformance-call!-safe12!
    semantic-early-record-safety-conformance-call!-safe13!
    semantic-early-record-safety-conformance-call!-safe15!
    semantic-early-record-safety-conformance-call!-safe16!]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
