

(defn stage2-runtime-derived-packet-fail!
  [id message source-path requested-target subject extra]
  (fail! id message
         (merge {:severity :error
                 :stage :stage2-runtime-derived-packet
                 :diagnostic-family :c14-target-lowering
                 :backend :target-neutral-stage2
                 :target requested-target
                 :source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :primary {:span (or (:source-span subject)
                                     (source-span source-path 0))}
                 :fallback-status :rejected}
                extra)))

(def stage2-runtime-derived-source-targets
  ;; :llvm, the exact Linux LLVM target, and :wasm are admitted only to the
  ;; internal pure checked-core/C11
  ;; construction path.  Effectful reference authority remains explicitly
  ;; JVM-only below.
  #{:jvm :c :c-hosted :c11 :js :js-ts :llvm :llvm-x86_64-linux :wasm})

(def p15-s23-closed-runtime-operations
  #{:literal :quote :local :builtin-call :println :do :if :let})

(def p15-s23-closed-runtime-max-depth 128)
(def p15-s23-closed-runtime-max-nodes 128)

(defn p15-s23-closed-runtime-inferred-type
  [instruction local-types depth]
  (if (or (> depth p15-s23-closed-runtime-max-depth)
          (not (map? instruction)))
    :unknown
    (case (:op instruction)
      :literal
      (let [value (:value instruction)]
        (cond
          (integer? value) :gravity/integer
          (boolean? value) :gravity/bool
          (string? value) :gravity/string
          (nil? value) :gravity/nil
          :else :unknown))

      :quote
      (let [value (:value instruction)]
        (cond
          (integer? value) :gravity/integer
          (boolean? value) :gravity/bool
          (string? value) :gravity/string
          (nil? value) :gravity/nil
          :else :unknown))

      :local
      (get local-types (:name instruction) :unknown)

      :builtin-call
      (let [function (:function instruction)
            arguments (:args instruction)]
        (cond
          (= 'str function) :gravity/string
          (and (contains? '#{= < <= > >=} function)
               (vector? arguments)
               (= 2 (count arguments))
               (every?
                #(= :gravity/integer
                    (p15-s23-closed-runtime-inferred-type
                     % local-types (inc depth)))
                arguments))
          :gravity/bool
          :else :unknown))

      :println :gravity/nil

      :do
      (let [body (:body instruction)]
        (if (and (vector? body) (seq body))
          (p15-s23-closed-runtime-inferred-type
           (peek body) local-types (inc depth))
          :gravity/nil))

      :if
      (let [then-type
            (p15-s23-closed-runtime-inferred-type
             (:then instruction) local-types (inc depth))
            else-type
            (p15-s23-closed-runtime-inferred-type
             (:else instruction) local-types (inc depth))]
        (if (= then-type else-type) then-type :unknown))

      :let
      (let [bindings (:bindings instruction)
            body (:body instruction)]
        (if (and (vector? bindings)
                 (vector? body)
                 (every? #(and (map? %)
                               (symbol? (:name %))
                               (map? (:expr %)))
                         bindings))
          (let [next-types
                (reduce
                 (fn [types binding]
                   (assoc
                    types (:name binding)
                    (p15-s23-closed-runtime-inferred-type
                     (:expr binding) types (inc depth))))
                 local-types bindings)]
            (if (seq body)
              (p15-s23-closed-runtime-inferred-type
               (peek body) next-types (inc depth))
              :gravity/nil))
          :unknown))

      :unknown)))