(ns gravity.self-hosting.list-construction-admission-current-test
  "Current-main normalized DATA matrices for the unmetered List owner paths.

  These controls exercise C7, the C8 body consumer, and the C14 lowering
  consumer independently.  They do not provide source admission, native
  authority, or authenticated pipeline evidence."
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(def ^:private widths [0 1023 1024 1025 2049])
(def ^:private upstream 'gravity.self-hosting.sh08-function-call-type-test)

(defn- upstream-var
  [name]
  (require upstream)
  (or (ns-resolve upstream name)
      (throw (ex-info "Missing current list-construction helper" {:name name}))))

(defn- list-input
  [width]
  ((upstream-var 'list-construction-input)
   (vec (repeat width :gravity.type/string))))

(defn- derive-list
  [input]
  ((upstream-var 'list-construction-derived) input))

(defn- c7
  [function arguments]
  ((upstream-var 'invoke-c7) function arguments))

(defn- body-source
  [core node call edge nodes bindings types module validation]
  (kernel-support/leaf 'body-lc-source core node call edge nodes bindings types module validation))

(defn- lowering-source
  [plan core node call edge nodes bindings types module validation]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   kernel-support/stage2-runtime plan 'p15-s23-lc-source
   [core node call edge nodes bindings types module validation]))

(deftest c7-list-construction-admission-current-data
  ;; Normalized DATA exercises the current C7 owner.  It carries no source or
  ;; authentication authority and must not reintroduce accounting fields.
  (doseq [width widths]
    (let [{:keys [core node call edge nodes bindings types forms] :as input}
          (list-input width)
          result (derive-list input)
          context {:ordering-core core
                   :ordering-operators {(:operator-node-id call) call}
                   :ordering-edges {(:node-id node) edge}
                   :lookup-nodes nodes
                   :ordering-bindings bindings
                   :association-forms forms}
          stepped (c7 'sh08-lc-step [node types {} {} context])]
      (is (= :accepted (:status result)) (pr-str (select-keys result [:status :reason])))
      (is (= {:kind :gravity.type/list
              :element-types (vec (repeat width :gravity.type/string))
              :shape :literal}
             (:type result)))
      (is (= result stepped))
      (is (not-any? #(contains? result %)
                    [:work-count :operand-work :result-work :construction-work]))
      (is (= width (count (get-in result [:descriptor :value-origin :operand-node-ids]))))
      (when (pos? width)
        (let [late-id (peek (:argument-node-ids call))
              invalid (derive-list (assoc-in input [:types late-id] :gravity.type/unknown))
              source-id (get-in nodes [late-id :source :form-id])
              stale-source (derive-list (assoc-in input [:forms source-id :parent-form-id] :sibling))
              stale-edge (assoc-in input [:edge :evaluation-order] :right-first)
              stale-edge-result (derive-list stale-edge)
              validation (c7 'sh08-lc-validate-types
                             [(mapv types (:argument-node-ids call))])]
          (is (= [:rejected :list-construction-operand-type-required]
                 ((juxt :status :reason) invalid)))
          (is (= [:rejected :list-construction-source-mismatch]
                 ((juxt :status :reason) stale-source)))
          ;; The public derive wrapper reports the failed source/edge header as
          ;; an operand rejection; the owning source helper itself exposes the
          ;; edge-shape refusal directly.
          (is (= [:rejected :list-construction-operand-type-required]
                 ((juxt :status :reason) stale-edge-result)))
          (is (nil? (c7 'sh08-lc-source
                        [core node call (:edge stale-edge) nodes bindings types
                         (:module core) validation])))
          (is (not-any? #(contains? invalid %)
                        [:work-count :operand-work :result-work :construction-work])))))))

(deftest c8-list-construction-admission-current-data
  ;; The body consumer receives the same normalized DATA and independently
  ;; checks the current C7 validation, call shape, and edge shape.
  (doseq [width widths]
    (let [{:keys [core node call edge nodes bindings types module]}
          (list-input width)
          validation (c7 'sh08-lc-validate-types
                         [(mapv types (:argument-node-ids call))])
          header (body-source core node call edge nodes bindings types module validation)
          expected (c7 'sh08-lc-source
                       [core node call edge nodes bindings types module validation])]
      (is (map? header) (pr-str (select-keys header [:status :reason])))
      (is (= expected header))
      (is (= width (count (:argument-types header))))
      (is (nil? (body-source core node (assoc call :extra true) edge
                             nodes bindings types module validation)))
      (is (nil? (body-source core node call (assoc edge :extra true)
                             nodes bindings types module validation)))
      (doseq [bad [nil {:status :rejected} {:status :accepted :work-count 0}]]
        (is (nil? (body-source core node call edge nodes bindings types module bad))))
      (when (pos? width)
        (let [late-id (peek (:argument-node-ids call))
              bad-types (assoc types late-id :gravity.type/unknown)
              bad-validation (c7 'sh08-lc-validate-types
                                 [(mapv bad-types (:argument-node-ids call))])]
          (is (nil? (body-source core node call edge nodes bindings bad-types module
                                 bad-validation))))))))

(deftest c14-list-construction-admission-current-data
  ;; C14's lowering source owner is exercised directly with normalized DATA;
  ;; this is not an authenticated source-to-lowering composition claim.
  (let [source-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                           source-path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan
              emitter source-path (slurp source-path))]
    (doseq [width widths]
      (let [{:keys [core node call edge nodes bindings types module]}
            (list-input width)
            validation (c7 'sh08-lc-validate-types
                           [(mapv types (:argument-node-ids call))])
            header (lowering-source plan core node call edge nodes bindings types module validation)
            expected (c7 'sh08-lc-source
                         [core node call edge nodes bindings types module validation])]
        (is (map? header) (pr-str (select-keys header [:status :reason])))
        (is (= expected header))
        (is (= width (count (:argument-types header))))
        (is (nil? (lowering-source plan core node (assoc call :extra true) edge
                                   nodes bindings types module validation)))
        (is (nil? (lowering-source plan core node call (assoc edge :extra true)
                                   nodes bindings types module validation)))
        (doseq [bad [nil {:status :rejected} {:status :accepted :work-count 0}]]
          (is (nil? (lowering-source plan core node call edge nodes bindings types module bad))))
        (when (pos? width)
          (let [late-id (peek (:argument-node-ids call))
                bad-types (assoc types late-id :gravity.type/unknown)
                bad-validation (c7 'sh08-lc-validate-types
                                   [(mapv bad-types (:argument-node-ids call))])]
            (is (nil? (lowering-source plan core node call edge nodes bindings bad-types module
                                       bad-validation)))))))))
