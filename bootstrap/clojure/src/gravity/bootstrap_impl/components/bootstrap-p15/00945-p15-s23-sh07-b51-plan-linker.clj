(declare c-backend-canonical-value c4-artifact-id reader-canonical-value
         p15-s23-compiler-source-form-record-from-text
         p15-s23-compiler-def-value
         p15-s23-stage2-compiler-artifact-augmented-emitter
         sh06-resolution-definition-records sh06-resolution-core-records
         sh06-resolution-analysis-inputs
         p15-s23-sh07-b51-derive-callsite-census
         p15-s23-stage2-construct-emitted-core-plan
         p15-s23-sh07-b51-plan-valid-structure?
         p15-s23-sh07-b51-plan-links-valid-structure?
         p15-s23-sh07-b51-execution-bundle-core-valid-structure?
         p15-s23-sh07-b51-execution-bundle-valid-structure?
         p15-s23-sh07-b51-execution-bundle-authorized?
         p15-s23-sh07-b51-authoritative-linked-products
         p15-s23-sh07-b51-current-authoritative-linked-products-v4
         ^:private p15-s23-sh07-b51-issue-top-level-validation-receipt!
         ^:private p15-s23-sh07-b51-retire-admitted-runtime-context!
         p15-s23-stage2-runtime-execute-function
         ^:private p15-s23-stage2-runtime-execute-function-scoped)

(def p15-s23-sh07-b51-module-specs
  [{:module-ordinal 0
    :module 'gravity.sh07-b51.producer-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 1
    :module 'gravity.sh07-b51.producer-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 2
    :module 'gravity.sh07-b51.producer-c
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-resolve-digest-preimage]}
   {:module-ordinal 3
    :module 'gravity.sh07-b51.producer-d
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-build-template
               sh07-b51-verify-template
               sh07-b51-resolve-template
               sh07-b51-verify-resolved
               sh07-b51-build-final-artifact]}
   {:module-ordinal 4
    :module 'gravity.sh07-b51.root6-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
    :owner :r6-independent-verifier
    :exports []}
   {:module-ordinal 5
    :module 'gravity.sh07-b51.root6-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
    :owner :r6-independent-verifier
    :exports '[sh07-b51-build-independent-verifier-binding]}
   {:module-ordinal 6
    :module 'gravity.sh07-b51.root8
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"
    :owner :r8-finalizer
    :exports '[sh07-b51-finalize-rejection]}])

(def p15-s23-sh07-b51-public-root-specs
  [{:root-ordinal 1 :function 'sh07-b51-build-template :arity 1
    :module-ordinal 3}
   {:root-ordinal 2 :function 'sh07-b51-verify-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 3 :function 'sh07-b51-resolve-digest-preimage :arity 3
    :module-ordinal 2}
   {:root-ordinal 4 :function 'sh07-b51-resolve-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 5 :function 'sh07-b51-verify-resolved :arity 4
    :module-ordinal 3}
   {:root-ordinal 6
    :function 'sh07-b51-build-independent-verifier-binding :arity 6
    :module-ordinal 5}
   {:root-ordinal 7 :function 'sh07-b51-build-final-artifact :arity 7
    :module-ordinal 3}
   {:root-ordinal 8 :function 'sh07-b51-finalize-rejection :arity 1
    :module-ordinal 6}])

(def ^:private p15-s23-sh07-b51-m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def ^:private p15-s23-sh07-b51-definition-keys
  #{:name :arity :visibility :source-hash})

(def ^:private p15-s23-sh07-b51-external-binding-keys
  #{:symbol :callee-module-ordinal :callee-module :callee-source-sha256
    :callee-definition-source-hash :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-builtin-call-keys
  #{:caller :caller-function-source-hash :builtin :arity
    :compiler-internal?})

(def ^:private p15-s23-sh07-b51-edge-keys
  #{:ordinal :caller-module-source-id :caller-function
    :caller-function-source-hash :callsite-path :callee-module-source-id
    :callee-function :callee-function-source-hash :arity :owner-edge
    :authorization})

(def ^:private p15-s23-sh07-b51-census-edge-keys
  #{:ordinal :caller-module-ordinal :caller-module :caller-function
    :callsite-path :callee-module-ordinal :callee-module :callee-function
    :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-m1-keys
  #{:artifact :schema-version :manifest-id :module-source-ids :edges})

(def ^:private p15-s23-sh07-b51-m3-keys
  #{:artifact :schema-version :manifest-id :module-plans :routes
    :public-roots :bundle-core-id :compile-census-certificate-ids
    :bundle-id})

(def ^:private p15-s23-sh07-b51-bundle-core-keys
  #{:domain :schema-version :manifest-id :module-plans :routes
    :public-roots})

(def ^:private p15-s23-sh07-b51-root8-external-callees
  '[sh07-b51-build-template
    sh07-b51-entrypoint-envelope
    sh07-b51-exact-keys?
    sh07-b51-kind-remediation
    sh07-b51-kind-rule
    sh07-b51-nonnegative-integer?
    sh07-b51-pending-rejected-envelope-shape?
    sh07-b51-pending-rejection
    sh07-b51-raw-carrier-shape?
    sh07-b51-semantic-coordinate
    sh07-b51-sha256-id?
    sh07-b51-template-shape-for-plan?])

;; Attempt 30 cannot acquire implementation authority until both independent
;; census certificates and the resulting M3/root snapshot have been frozen.
;; These deliberately non-digest values fail every authority check and must be
;; replaced only by a separately reviewed immutable implementation snapshot.
(def ^:private p15-s23-sh07-b51-expected-m3-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-a-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-b-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-semantic-root
  :gravity/provisional-no-authority)

;; Historical public name retained only so older host code fails closed rather
;; than resolving a removed Var.  It is not an accepted M3-v1 pin.
(def p15-s23-sh07-b51-expected-execution-bundle-id
  p15-s23-sh07-b51-expected-m3-id)

(def p15-s23-sh07-b51-host-pin-boundary-id
  "sha256:ecae8acf77cf8c64588aef4cad380c0a7873baab1b282398b03f532ae94b0a6e")

(def p15-s23-sh07-b51-runtime-token-boundary-id
  "sha256:844b3ea1b9bd75f416bfb51ff2c40e9a8aed1803813d6a11b3e54e44e75809ad")

(def ^:private p15-s23-sh07-b51-admitted-runtime-contexts
  (java.util.HashMap.))
(def ^:private p15-s23-sh07-b51-admitted-runtime-context-queue
  (java.lang.ref.ReferenceQueue.))
(def ^:private p15-s23-sh07-b51-registry-prune-maximum 16)
(def ^:private p15-s23-sh07-b51-maximum-external-calls 65536)

(def ^:private p15-s23-sh07-b51-runtime-authority-keys
  #{:sh07-b51-runtime-context-token
    :sh07-b51-runtime-token-boundary-id
    :sh07-b51-linked-carrier-v4
    :sh07-b51-linked-carrier-v3
    :sh07-b51-module-source-descriptors
    :sh07-b51-link-manifest
    :sh07-b51-module-plans
    :sh07-b51-execution-bundle
    :sh07-b51-external-call-trace})

(deftype ^:private P15S23Sh07B51IdentityTokenQuery
  [token ^int identity-hash]
  Object
  (hashCode [_] identity-hash)
  (equals [_ other]
    (and (instance? java.lang.ref.WeakReference other)
         (identical? token
                     (.get ^java.lang.ref.WeakReference other)))))

(alter-meta! #'->P15S23Sh07B51IdentityTokenQuery assoc :private true)

(defn- p15-s23-sh07-b51-weak-identity-token-key [token]
  (let [identity-hash (System/identityHashCode token)]
    (proxy [java.lang.ref.WeakReference]
      [token p15-s23-sh07-b51-admitted-runtime-context-queue]
      (hashCode [] identity-hash)
      (equals [other]
        (let [registered-token (proxy-super get)]
          (and registered-token
               (instance? java.lang.ref.WeakReference other)
               (identical?
                registered-token
                (.get ^java.lang.ref.WeakReference other))))))))

(defn- p15-s23-sh07-b51-token-query [token]
  (P15S23Sh07B51IdentityTokenQuery.
   token (System/identityHashCode token)))

(defn- p15-s23-sh07-b51-prune-admitted-runtime-contexts! []
  (loop [remaining p15-s23-sh07-b51-registry-prune-maximum]
    (when (pos? remaining)
      (when-let [key (.poll ^java.lang.ref.ReferenceQueue
                            p15-s23-sh07-b51-admitted-runtime-context-queue)]
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts key)
        (recur (dec remaining))))))

(defn- p15-s23-sh07-b51-register-runtime-context! [context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [token (.get ^java.lang.ref.WeakReference (:token-ref context))]
      (.put ^java.util.HashMap p15-s23-sh07-b51-admitted-runtime-contexts
            (p15-s23-sh07-b51-weak-identity-token-key token)
            (java.lang.ref.WeakReference. context))))
  context)

(defn- p15-s23-sh07-b51-runtime-context-by-token [token]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          context (when context-ref
                    (.get ^java.lang.ref.WeakReference context-ref))]
      (when (and context-ref (nil? context))
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query))
      context)))
