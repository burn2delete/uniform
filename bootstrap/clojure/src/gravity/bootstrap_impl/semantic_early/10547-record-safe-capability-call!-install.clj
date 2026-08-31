; Semantic decomposition of HEAD reader line 10547.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-early-record-safe-capability-call!-dispatch!
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
      semantic-early-record-safe-capability-call!-unhandled
      result__1419__auto__)
     (recur (clojure.core/next remaining__1417__auto__))
     result__1419__auto__)))))

(defn
 record-safe-capability-call!
 [checker ctx node operator args effects capabilities return-type spec]
 (when-let
  [kind (:safe-capability-kind spec)]
  (let
   [record
    {:capabilities capabilities,
     :safety-mode (:safety @ctx),
     :safe-capability-kind kind,
     :source-span (:source-span node),
     :return-type return-type,
     :generated-origin-chain (:generated-origin node),
     :node-id (:node-id node),
     :effects effects,
     :operator operator,
     :target (:target @ctx),
     :profile (:profile @ctx)}]
   (semantic-early-record-safe-capability-call!-dispatch!
    kind
    checker
    record
    args
    [semantic-early-record-safe-capability-call!-safe10!
     semantic-early-record-safe-capability-call!-safe14!])
   record)))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-record-safe-capability-call!-unhandled
    semantic-early-record-safe-capability-call!-dispatch!
    semantic-early-record-safe-capability-call!-safe10!
    semantic-early-record-safe-capability-call!-safe14!]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
