; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-early-call-specific-diagnostic-dispatch!
 [operator__1393__auto__ node__1394__auto__ handlers__1395__auto__]
 (clojure.core/loop
  [remaining__1396__auto__ (clojure.core/seq handlers__1395__auto__)]
  (clojure.core/if-let
   [handler__1397__auto__ (clojure.core/first remaining__1396__auto__)]
   (clojure.core/let
    [result__1398__auto__
     (handler__1397__auto__ operator__1393__auto__ node__1394__auto__)]
    (if
     (clojure.core/identical?
      semantic-early-call-specific-diagnostic-unhandled
      result__1398__auto__)
     (recur (clojure.core/next remaining__1396__auto__))
     result__1398__auto__))
   nil)))

(defn
 call-specific-diagnostic!
 [operator node]
 (if-let
  [diagnostic
   (or
    (get safe-memory-direct-diagnostics operator)
    (get profile-direct-diagnostics operator))]
  (typed-diagnostic!
   (:id diagnostic)
   (:message diagnostic)
   node
   (or
    (:remediation diagnostic)
    "Satisfy the owning profile or safety document contract before lowering.")
   (merge
    (dissoc diagnostic :id :message :rule)
    {:safe-rule (:rule diagnostic), :active-profile (:profile node)}))
  (semantic-early-call-specific-diagnostic-dispatch!
   operator
   node
   [semantic-early-call-specific-diagnostic-l05-l06!
    semantic-early-call-specific-diagnostic-l08-l09!
    semantic-early-call-specific-diagnostic-l10-l11!
    semantic-early-call-specific-diagnostic-l12!
    semantic-early-call-specific-diagnostic-l13!
    semantic-early-call-specific-diagnostic-l14!
    semantic-early-call-specific-diagnostic-l15!
    semantic-early-call-specific-diagnostic-l16!
    semantic-early-call-specific-diagnostic-l17!
    semantic-early-call-specific-diagnostic-l18!
    semantic-early-call-specific-diagnostic-l19!
    semantic-early-call-specific-diagnostic-safe1!])))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-call-specific-diagnostic-unhandled
    semantic-early-call-specific-diagnostic-dispatch!
    semantic-early-call-specific-diagnostic-l05-l06!
    semantic-early-call-specific-diagnostic-l08-l09!
    semantic-early-call-specific-diagnostic-l10-l11!
    semantic-early-call-specific-diagnostic-l12!
    semantic-early-call-specific-diagnostic-l13!
    semantic-early-call-specific-diagnostic-l14!
    semantic-early-call-specific-diagnostic-l15!
    semantic-early-call-specific-diagnostic-l16!
    semantic-early-call-specific-diagnostic-l17!
    semantic-early-call-specific-diagnostic-l18!
    semantic-early-call-specific-diagnostic-l19!
    semantic-early-call-specific-diagnostic-safe1!]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
