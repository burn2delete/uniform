(ns gravity.darwin-publication.context
  "Internal Darwin publication context operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all])
  (:import [java.lang.foreign Arena FunctionDescriptor Linker
            Linker$Option MemoryLayout MemoryLayout$PathElement MemorySegment
            ValueLayout]
           [java.lang.invoke VarHandle$AccessMode]
           [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file InvalidPathException Paths]
           [java.security MessageDigest SecureRandom]
           [java.util Collections WeakHashMap]))

(def context-controls
  ;; Publication contexts are identity capabilities.  Keeping lifecycle
  ;; authority in a weak side table avoids the public JVM fields emitted for
  ;; deftype fields while retaining terminal results for as long as a caller
  ;; still holds the corresponding context.
  (Collections/synchronizedMap (WeakHashMap.)))

(deftype PublicationContext
  [publication-receipt]
  clojure.lang.ILookup
  (valAt [_ key]
    (when (= :publication-receipt key)
      publication-receipt))
  (valAt [_ key not-found]
    (if (= :publication-receipt key)
      publication-receipt
      not-found))
  Object
  (toString [_] "#<gravity.darwin-publication/context>"))

(defn publication-context [receipt]
  (PublicationContext. receipt))

;; `deftype` emits a constructor var; it is an implementation detail and must
;; not widen the namespace's five-function raw API.
(alter-meta! #'->PublicationContext assoc :private true)

(defn register-context!
  [context control token]
  (.put context-controls context
        {:control control
         :token token})
  context)

(defn context-entry
  [context]
  (when (instance? PublicationContext context)
    (.get context-controls context)))


(defn context-state!
  [context operation]
  (let [entry (context-entry context)
        control (:control entry)
        token (:token entry)]
    (when-not
     (and entry
          (instance? clojure.lang.Atom control)
          (some? token))
      (failure! operation :invalid-provider-context))
    (let [state @control]
      (when-not (and (map? state)
                     (= :gravity/darwin-descriptor-publication
                        (:provider state))
                     (= provider-version (:provider-version state))
                     (identical? token (:token state))
                     (integer? (:generation state))
                     (keyword? (:phase state)))
        (failure! operation :invalid-provider-control-state))
      state)))

(defn update-control!
  [context expected-phases next-state operation]
  (loop []
    (let [state (context-state! context operation)
          control (:control (context-entry context))]
        (when-not (contains? expected-phases (:phase state))
          (failure! operation :invalid-provider-lifecycle))
        (let [candidate
              (-> (next-state state)
                  (assoc :generation (inc (:generation state))))]
          (if (compare-and-set! control state candidate)
            candidate
            (recur))))))

(defn mark-failed!
  [context expected-phase]
  (update-control! context #{expected-phase}
                   #(assoc % :phase :failed) :mark-failed))
