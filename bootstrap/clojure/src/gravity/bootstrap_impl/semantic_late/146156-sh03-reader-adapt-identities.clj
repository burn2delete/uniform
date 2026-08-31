; Semantic decomposition of committed HEAD reader line 146156.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh03-reader-adapt-products!-identities
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-bytes
     result
     source-unit
     source-id
     source-content-id
     scalar-boundaries
     raw-tokens
     raw-forms
     semantic-index]}
   state
   _
   (sh03-reader-semantic-value-closure!
    source-path
    source-bytes
    source-content-id
    scalar-boundaries
    raw-tokens
    raw-forms
    semantic-index)
   token-id-map
   (into
    {}
    (map-indexed (fn [index token] [(:token-id token) (keyword (str "tok-" index))]) raw-tokens))
   form-id-map
   (into
    {}
    (map-indexed (fn [index form] [(:form-id form) (keyword (str "form-" index))]) raw-forms))
   _
   (doseq
    [[form-id record reference]
     (map
      vector
      (:top-level-form-ids result)
      (:top-level-parsed-records result)
      (:parsed-semantic-values result))]
    (when-not
     (= form-id (:form-id record))
     (sh03-reader-boundary-fail!
      source-path
      :form-bound-sh03-reader-top-level-record
      record
      {:expected-form-id form-id}))
    (sh03-reader-form-value-reference! source-path (:value record) form-id)
    (sh03-reader-form-value-reference! source-path reference form-id))
   host-values
   (try
    (sh03-reader-host-values!
     source-path
     source-bytes
     source-content-id
     scalar-boundaries
     raw-forms
     semantic-index)
    (catch
     InterruptedException
     interrupted
     (.interrupt (Thread/currentThread))
     (throw interrupted))
    (catch clojure.lang.ExceptionInfo error (throw error))
    (catch
     StackOverflowError
     error
     (sh03-reader-boundary-fail!
      source-path
      :bounded-sh03-reader-adapter-host-stack
      {}
      {:contained-host-error (.getName (class error))}))
    (catch
     Throwable
     error
     (sh03-reader-boundary-fail!
      source-path
      :contained-sh03-reader-adapter-value-failure
      {}
      {:contained-host-error (.getName (class error)), :cause-message (.getMessage error)})))]
  (clojure.core/assoc
   state
   :token-id-map
   token-id-map
   :form-id-map
   form-id-map
   :host-values
   host-values)))
