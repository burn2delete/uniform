(ns gravity.macro-expansion.policy
  (:require [clojure.set :as set]
            [gravity.macro-expansion.operations :as operations]
            [gravity.macro-expansion.registry :as registry]
            [gravity.macro-expansion.templates :as templates]))

(defn assert-build-effects!
  [module macro call-span ops]
  (let [used (or (:uses-build-effects macro) (:build-effects macro) #{})
        declared (:build-effects macro)
        grants ((operations/operation ops :macro-build-grants
                                      registry/build-grants)
                module)
        undeclared (first (remove declared used))
        ungranted (first (remove grants used))]
    (when undeclared
      (operations/fail!
       ops "L4-BUILD-EFFECT" "macro used an undeclared build effect"
       {:source-span call-span
        :macro (:identity macro)
        :effect undeclared
        :declared-effects declared
        :remediation "Declare build effects in the macro definition."}))
    (when ungranted
      (operations/fail!
       ops "L4-BUILD-EFFECT"
       "macro used a build effect not granted by the build policy"
       {:source-span call-span
        :macro (:identity macro)
        :effect ungranted
        :declared-effects declared
        :granted-build-effects grants
        :remediation
        "Grant the build effect in project metadata or remove the effect."}))))

(defn collect-let-bindings
  [form ops]
  (let [form-op (operations/operation ops :form-op? operations/form-op?)
        recurse (fn [value] (collect-let-bindings value ops))]
    (cond
      (form-op 'let form)
      (concat (take-nth 2 (second form))
              (mapcat recurse (drop 2 form)))
      (seq? form) (mapcat recurse form)
      (coll? form) (mapcat recurse form)
      :else [])))

(defn assert-hygiene!
  [macro args output call-span ops]
  (let [collect-bindings
        (operations/operation
         ops :collect-let-bindings
         (fn [form] (collect-let-bindings form ops)))
        introduced (set (filter symbol? (collect-bindings output)))
        caller-symbols
        (set (mapcat (operations/operation ops :collect-symbols
                                           operations/collect-symbols)
                     args))
        accidental (first (set/intersection introduced caller-symbols))]
    (when (and accidental (not= :explicit-capture (:hygiene-policy macro)))
      (operations/fail!
       ops "L4-HYGIENE-CAPTURE"
       "macro expansion would accidentally capture a caller binding"
       {:source-span call-span
        :macro (:identity macro)
        :symbol accidental
        :hygiene-policy (:hygiene-policy macro)
        :remediation
        "Use a fresh generated binding or mark the macro as explicit capture."}))))

(defn assert-generated-profile!
  [module macro output call-span ops]
  (when (and (not= :hosted (:profile module))
             ((operations/operation ops :contains-form-op?
                                    operations/contains-form-op?)
              'host-reflect output))
    (operations/fail!
     ops "L4-GENERATED-PROFILE"
     "macro generated code that violates the caller profile"
     {:source-span call-span
      :macro (:identity macro)
      :profile (:profile module)
      :generated-form output
      :remediation
      "Generated code must pass the caller profile, not the macro implementation profile."})))

(defn assert-generated-unsafe!
  [module macro output call-span ops]
  (when (and ((operations/operation ops :contains-form-op?
                                    operations/contains-form-op?)
              'unsafe output)
             (not (:allow-unsafe? macro))
             (#{:safe :safe-optimized nil} (:safety module)))
    (operations/fail!
     ops "L4-GENERATED-UNSAFE"
     "macro generated unsafe code without an explicit unsafe policy"
     {:source-span call-span
      :macro (:identity macro)
      :safety (:safety module)
      :generated-form output
      :remediation
      "Make unsafe generation explicit and attach an unsafe audit record."})))

(defn expand-macro-form
  [module macro args call-span ops]
  (let [assert-build
        (operations/operation
         ops :assert-build-effects!
         (fn [m mac span] (assert-build-effects! m mac span ops)))
        bind-args
        (operations/operation
         ops :bind-macro-arguments
         (fn [mac values span]
           (templates/bind-macro-arguments mac values span ops)))
        parse-template
        (operations/operation
         ops :parse-syntax-template
         (fn [mac span] (templates/parse-syntax-template mac span ops)))
        expand-template
        (operations/operation
         ops :expand-template
         (fn [env template] (templates/expand-template env template ops)))
        assert-hygiene
        (operations/operation
         ops :assert-hygiene!
         (fn [mac values output span]
           (assert-hygiene! mac values output span ops)))
        assert-profile
        (operations/operation
         ops :assert-generated-profile!
         (fn [m mac output span]
           (assert-generated-profile! m mac output span ops)))
        assert-unsafe
        (operations/operation
         ops :assert-generated-unsafe!
         (fn [m mac output span]
           (assert-generated-unsafe! m mac output span ops)))]
    (assert-build module macro call-span)
    (let [output (if-let [expander (:expander macro)]
                   (expander args call-span)
                   (let [env (bind-args macro args call-span)
                         template (parse-template macro call-span)]
                     (expand-template env template)))]
      (when (= :source (:kind macro))
        (assert-hygiene macro args output call-span))
      (assert-profile module macro output call-span)
      (assert-unsafe module macro output call-span)
      output)))
