(def ^:private p15-s23-runtime-call-operations
  {:authorize-external-link #'p15-s23-sh07-b51-runtime-authorize-external-link
   :execute-values-scoped #'p15-s23-stage2-runtime-execute-values-scoped
   :execute-external-machine #'p15-s23-sh07-b51-runtime-execute-external-machine
   :execute-function-scoped #'p15-s23-stage2-runtime-execute-function-scoped})

(defn- p15-s23-stage2-runtime-execute-call-scoped [runtime plan env instruction public-invocation-scope]
  (gravity.runtime/execute-call-scoped runtime plan env instruction public-invocation-scope p15-s23-runtime-call-operations))

(defn- p15-s23-stage2-module-scope? [scope]
  (= :gravity/stage2-module-invocation (:kind scope)))

(defn- p15-s23-stage2-module-member! [bundle id]
  (p15-s23-stage2-module-one! (filterv #(= id (:module-id %)) (:modules bundle)) :runtime-module-member))

(defn- p15-s23-stage2-module-entry-scope! [plan callee args scope]
  (if (p15-s23-stage2-module-scope? scope)
    (let [entry (p15-s23-stage2-module-member! (:bundle scope) (:module-id scope))
          definition (get-in entry [:plan :functions callee])]
      (when-not (and (= #{:kind :bundle :module-id :function} (set (keys scope)))
                     (= plan (:plan entry)) (map? definition)
                     (= (:arity definition) (count (:params definition))))
        (p15-s23-stage2-module-fail! :runtime-plan-or-function-member {:function callee}))
      (when-not (= (count args) (:arity definition))
        (p15-s23-stage2-runtime-fail-call-arity! "L2-FUNCTION-ARITY" plan callee args (:arity definition)))
      (assoc scope :function callee))
    scope))

(defn- p15-s23-stage2-module-authorize-call! [plan instruction scope]
  (when-not (p15-s23-stage2-module-scope? scope)
    (p15-s23-stage2-module-fail! :ordinary-module-invocation-required {}))
  (let [bundle (:bundle scope)
        caller (p15-s23-stage2-module-member! bundle (:module-id scope))
        route (p15-s23-stage2-module-one! (filterv #(= (:route-id instruction) (:route-id %)) (:routes bundle)) :runtime-callsite-route)
        callee (p15-s23-stage2-module-member! bundle (:callee-module-id route))
        export (p15-s23-stage2-module-one! (filterv #(= (:callee-binding-id route) (:binding-id %)) (:exports callee)) :runtime-public-export)
        target (get-in callee [:plan :functions (:callee-function route)])
        callsites (filterv #(and (= :compiler-module-call (:op %)) (= (:route-id route) (:route-id %)))
                          (p15-s23-stage2-module-instructions
                           {:functions {(:function scope) (get-in caller [:plan :functions (:function scope)])}}))]
    (when-not (and (= plan (:plan caller)) (= (:module-id scope) (:caller-module-id route))
                   (= (:function scope) (:caller-function route) (:caller-function instruction))
                   (= (:callsite-path route) (:callsite-path instruction))
                   (= (:symbol route) (:function instruction))
                   (= 1 (count callsites)) (= instruction (first callsites))
                   (= :public (:visibility export)) (= :function (:kind export))
                   (= (:callee-function route) (:name export))
                   (= (:callee-module-id route) (get-in export [:semantic-span :ordinary-module-artifact-id] (:module-id callee)))
                   (= (get-in callee [:resolution-artifact :gravity-resolution-boundary :resolved-analysis :module-contract :namespace])
                      (:namespace export) (get-in callee [:plan :module :module]))
                   (map? target) (= (:arity route) (:arity target) (count (:params target)) (count (:args instruction))))
      (p15-s23-stage2-module-fail! :runtime-route-export-or-arity {:route-id (:route-id instruction)}))
    {:entry callee :route route}))

(defn- p15-s23-stage2-module-execute-call [runtime plan env instruction scope]
  ;; Validate the actual source instruction, route, export and arity first.
  (let [{:keys [entry route]} (p15-s23-stage2-module-authorize-call! plan instruction scope)
        args (p15-s23-stage2-runtime-execute-values-scoped
              runtime plan env (:args instruction) :recur-inside-function-argument scope)
        callee-scope (assoc scope :module-id (:module-id entry) :function (:callee-function route))]
    (p15-s23-stage2-runtime-execute-function-scoped runtime (:plan entry) (:callee-function route) args callee-scope)))

(defn p15-s23-stage2-module-execute-function [runtime bundle function args]
  (when-not (map? runtime) (p15-s23-stage2-module-fail! :runtime-map-required {}))
  (when (some #(and (keyword? %) (str/starts-with? (name %) "sh07-b51-")) (keys runtime))
    (p15-s23-stage2-module-fail! :admission-runtime-forbidden {}))
  (let [bundle (p15-s23-stage2-module-validate-bundle! bundle)
        root (p15-s23-stage2-module-member! bundle (:root-module-id bundle))]
    (when-not (and (symbol? function) (nil? (namespace function))
                   (map? (get-in root [:plan :functions function])))
      (p15-s23-stage2-module-fail! :root-function-member-required {:function function}))
    ;; This data is derived only here, after full reconstruction, and threaded
    ;; lexically through private calls. Public raw instruction entry has nil.
    (p15-s23-stage2-runtime-execute-function-scoped
     runtime (:plan root) function args
     {:kind :gravity/stage2-module-invocation :bundle bundle :module-id (:module-id root) :function function})))
