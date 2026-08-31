; Semantic decomposition of HEAD reader line 10125.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-early-record-boundary-safety-call!-dispatch!
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
      semantic-early-record-boundary-safety-call!-unhandled
      result__1419__auto__)
     (recur (clojure.core/next remaining__1417__auto__))
     result__1419__auto__)))))

(defn
 record-boundary-safety-call!
 [checker ctx node operator args effects capabilities return-type spec]
 (when-let
  [kind (:safe-boundary-kind spec)]
  (let
   [record
    {:capabilities capabilities,
     :safety-mode (:safety @ctx),
     :source-span (:source-span node),
     :return-type return-type,
     :generated-origin-chain (:generated-origin node),
     :node-id (:node-id node),
     :effects effects,
     :operator operator,
     :safe-boundary-kind kind,
     :target (:target @ctx),
     :profile (:profile @ctx)}]
   (semantic-early-record-boundary-safety-call!-dispatch!
    kind
    checker
    record
    args
    [semantic-early-record-boundary-safety-call!-safe7!
     semantic-early-record-boundary-safety-call!-safe8!
     semantic-early-record-boundary-safety-call!-safe9!
     semantic-early-record-boundary-safety-call!-safe11!])
   record)))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-record-boundary-safety-call!-unhandled
    semantic-early-record-boundary-safety-call!-dispatch!
    semantic-early-record-boundary-safety-call!-safe7!
    semantic-early-record-boundary-safety-call!-safe8!
    semantic-early-record-boundary-safety-call!-safe9!
    semantic-early-record-boundary-safety-call!-safe11!]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
