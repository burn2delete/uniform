
(defn p15-s23-sh07-b51-current-authoritative-linked-products-v4 []
  (if-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
    (:products authority)
    (throw (ex-info "Attempt32 authority is not frozen"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :provisional-no-authority
                     :result-committed? false
                     :output-committed? false}))))

(defn- p15-s23-sh07-b51-v4-bounded-carrier-equal? [supplied current]
  (= supplied current))

(defn p15-s23-sh07-b51-linked-carrier-v4-authorized? [carrier]
  (try
    (boolean
     (and (p15-s23-sh07-b51-v4-carrier-preflight? carrier)
          (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
            (and (p15-s23-sh07-b51-v4-bounded-carrier-equal?
                  carrier (:carrier authority))
                 (p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
                  carrier)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-linked-carrier-v4-runtime-products-match?
  [carrier descriptors manifest plans bundle]
  (try
    (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
      (let [current (:carrier authority)]
        (and (identical? carrier current)
             (= descriptors (:module-source-descriptors current))
             (= manifest (:link-manifest current))
             (= bundle (:execution-bundle current))
             (= plans (mapv :plan (:module-plan-carriers current))))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

;; Compatibility for the unchanged Stage2 internal-builtin component.  The
;; value accepted here is carrier-v4; carrier-v3 domains always reject.
(defn p15-s23-sh07-b51-linked-carrier-v3-runtime-products-match?
  [carrier descriptors manifest plans bundle]
  (p15-s23-sh07-b51-linked-carrier-v4-runtime-products-match?
   carrier descriptors manifest plans bundle))

(def ^:private p15-s23-sh07-b51-v4-preflight-placeholder
  :gravity/bounded-carrier-preflight-placeholder)

(defn- p15-s23-sh07-b51-v4-shallow-raw-carrier-coordinate
  [raw]
  ;; This pass examines only root entries.  It never descends into a value and
  ;; never calls count on the supplied map; the 65,537th entry rejects before
  ;; any unbounded root traversal can occur.
  (when (map? raw)
    (let [maximum-width
          (:carrier-maximum-width p15-s23-sh07-b51-attempt32-profile-bounds)
          maximum-scalar
          (:carrier-maximum-scalar-utf8
           p15-s23-sh07-b51-attempt32-profile-bounds)]
      (loop [remaining (seq raw) width 0 found? false carrier nil]
        (if (nil? remaining)
          (when found? {:carrier carrier :root-width width})
          (when (< width maximum-width)
            (let [entry (first remaining)
                  root-key (key entry)
                  key-text
                  (p15-s23-sh07-b51-v4-scalar-text root-key maximum-scalar)
                  key-bytes
                  (when key-text
                    (p15-s23-sh07-b51-v4-bounded-utf8-count
                     key-text maximum-scalar))
                  coordinate?
                  (= :v18-executable-descriptor root-key)]
              (when (and key-text (<= key-bytes maximum-scalar)
                         (not (and found? coordinate?)))
                (recur (next remaining) (inc width)
                       (or found? coordinate?)
                       (if coordinate? (val entry) carrier))))))))))

(defn p15-s23-sh07-b51-supplied-raw-v4-preflight-profile
  [raw]
  (try
    (when-let
     [coordinate
      (p15-s23-sh07-b51-v4-shallow-raw-carrier-coordinate raw)]
      (let [outer
            (assoc raw :v18-executable-descriptor
                   p15-s23-sh07-b51-v4-preflight-placeholder)
            outer-profile
            (p15-s23-sh07-b51-v4-bounded-value-profile
             outer
             (p15-s23-sh07-b51-v4-carrier-profile-limits
              (:outer-request-maximum-collection-count
               p15-s23-sh07-b51-attempt32-profile-bounds)))]
        ;; The nested carrier profile starts only after the shallow pass and
        ;; independent placeholder outer profile have completed.  Its first
        ;; operation is the complete 524,288-collection iterative pass; the
        ;; plan key is not read until that pass succeeds.
        (when outer-profile
          (when-let
           [carrier-profile
            (p15-s23-sh07-b51-v4-carrier-preflight-profile
             (:carrier coordinate))]
            {:outer-profile outer-profile
             :carrier-profile carrier-profile}))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ nil)))

(defn p15-s23-sh07-b51-supplied-raw-v4-preflight? [raw]
  (boolean (p15-s23-sh07-b51-supplied-raw-v4-preflight-profile raw)))

(defn- p15-s23-sh07-b51-v4-raw-joins-valid? [raw]
  (and (p15-s23-sh07-b51-exact-keys? raw p15-s23-sh07-b51-v4-raw-keys)
       (= :gravity/sh07-b51-raw-adapter-request-linked-v4 (:artifact raw))
       (= 4 (:schema-version raw))
       (= :sh07-b51-vector-destructuring (:scope raw))
       (= p15-s23-sh07-b51-v4-predecessor-id
          (:executable-closure-descriptor-id
           (:predecessor-executable-descriptor raw)))
       (every? map?
               ((juxt :authenticated-sh06-request
                      :authenticated-sh06-verification-report
                      :sh06-resolution-artifact
                      :predecessor-executable-descriptor
                      :predecessor-contract-carrier
                      :predecessor-raw-outcome
                      :physical-invocation) raw))))

(defn- p15-s23-sh07-b51-v4-step3-admit
  [raw authority]
  (let [supplied-carrier (:v18-executable-descriptor raw)
        current-carrier (:carrier authority)]
    (and (p15-s23-sh07-b51-v4-bounded-carrier-equal?
          supplied-carrier current-carrier)
         (p15-s23-sh07-b51-v4-raw-joins-valid? raw)
         (p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
          supplied-carrier))))

(defn p15-s23-sh07-b51-linked-raw-v4-authorized? [raw]
  (try
    (boolean
     (and (p15-s23-sh07-b51-supplied-raw-v4-preflight? raw)
          (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
            (p15-s23-sh07-b51-v4-step3-admit raw authority))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-v4-route-and-arity
  [products root-ordinal args]
  (let [bundle (:execution-bundle products)
        roots (:public-roots bundle)
        root (when (and (integer? root-ordinal) (<= 1 root-ordinal 8)
                        (vector? roots) (= 8 (count roots)))
               (nth roots (dec root-ordinal) nil))
        specification
        (when root (nth p15-s23-sh07-b51-public-root-specs
                        (dec root-ordinal) nil))
        expected-owner
        (when root
          (case root-ordinal
            6 :r6-independent-verifier
            8 :r8-finalizer
            :r1-producer))
        plan (when root
               (first (filter #(= (:plan-id root) (:plan-id %))
                              (:module-plans products))))
        descriptor
        (when specification
          (nth (:module-source-descriptors products)
               (:module-ordinal specification) nil))]
    (when (and (vector? args) root specification plan descriptor
               (= expected-owner (:owner descriptor))
               (= (select-keys specification [:root-ordinal :function :arity])
                  (select-keys root [:root-ordinal :function :arity]))
               (= (:plan-id plan) (:plan-id root))
               (= (:module-source-id descriptor)
                  (:sh07-b51-module-source-id plan))
               (= (:arity root) (count args))
               (= (:arity root)
                  (get-in plan [:functions (:function root) :arity])))
      {:root root :plan plan})))

(defn- p15-s23-sh07-b51-v4-attach-runtime-token
  [authority raw route args runtime]
  (let [products (:products authority)
        carrier (:carrier authority)]
    (p15-s23-sh07-b51-register-admitted-runtime-context
     products carrier raw route args runtime)))

(defn- p15-s23-sh07-b51-v4-admission-proof
  [raw root-ordinal args]
  (try
    (when (p15-s23-sh07-b51-supplied-raw-v4-preflight? raw)
      (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
        (when (p15-s23-sh07-b51-v4-step3-admit raw authority)
          (when-let [route
                     (p15-s23-sh07-b51-v4-route-and-arity
                      (:products authority) root-ordinal args)]
            {:authority authority :route route}))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ nil)))

(defn p15-s23-sh07-b51-admit-linked-raw-v4
  [raw root-ordinal args runtime]
  (when-let [{:keys [authority route]}
             (p15-s23-sh07-b51-v4-admission-proof raw root-ordinal args)]
    ;; Compatibility admission reports no live token authority. Receipt and
    ;; context lifetime exist only inside the synchronous execution boundary.
    (assoc route
           :products (:products authority)
           :runtime (p15-s23-sh07-b51-unadmitted-runtime-context runtime))))
