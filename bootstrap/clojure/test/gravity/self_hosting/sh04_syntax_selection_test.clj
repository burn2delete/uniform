(ns gravity.self-hosting.sh04-syntax-selection-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [gravity.bootstrap :as b]))

(defn- owner [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "missing owner helper" {:name name}))))
(defn- diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn- reader-invocation []
  {:reader-source-path (.getCanonicalPath
                        (java.io.File. (b/sh03-reader-resolve-source-path)))
   :authenticated-envelope-options {:source :development-current}})
(defn- invocation []
  (assoc (reader-invocation) :syntax-source-selection
         {:schema :gravity/sh04-development-syntax-selection-v1
          :source-path (.getCanonicalPath
                        (java.io.File. (b/sh04-syntax-resolve-source-path)))}))
(defn- selection [value]
  ((owner 'sh04-syntax-selection!) "<sh04-selection-test>" value))

(deftype ForeignSelection [calls]
  Object
  (hashCode [_] (swap! calls update :hash inc) 7)
  (equals [_ _] (swap! calls update :equals inc) false)
  (toString [_] (swap! calls update :string inc) "foreign-selection")
  clojure.lang.IMeta
  (meta [_] (swap! calls update :meta inc) nil))

(deftest selection-is-explicit-and-malformed-presence-never-falls-back
  (is (nil? (selection nil)))
  (is (nil? (selection (reader-invocation))))
  (doseq [value [nil false {} {:schema :unknown :source-path "ignored"}
                 {:schema :gravity/sh04-development-syntax-selection-v1 :source-path 4}
                 (assoc (:syntax-source-selection (invocation)) :expected-hash "caller")]]
    (let [failure (diagnostic #(selection (assoc (reader-invocation)
                                                 :syntax-source-selection value)))]
      (is (= "C3-ID" (:id failure)))
      (is (= [:explicit-syntax-selection] (:missing-fields failure))))))

(deftest outer-invocation-remains-a-closed-Reader-contract
  (let [failure (diagnostic #(b/sh04-syntax-current-binding!
                              "<sh04-selection-test>"
                              (assoc (reader-invocation) :unexpected true)))]
    (is (= "C2-HASH" (:id failure)))
    (is (= [:explicit-sh03-reader-invocation-source-selection]
           (:missing-fields failure)))))

(deftest foreign-selection-rejects-before-callbacks
  (doseq [position [:invocation :selection :path :key]]
    (let [calls (atom {:hash 0 :equals 0 :string 0 :meta 0})
          foreign (ForeignSelection. calls)
          value (case position
                  :invocation foreign
                  :selection (assoc (reader-invocation)
                                    :syntax-source-selection foreign)
                  :path (assoc-in (invocation)
                                  [:syntax-source-selection :source-path] foreign)
                  :key (clojure.lang.PersistentArrayMap.
                        (object-array [foreign nil])))
          _ (is (= {:hash 0 :equals 0 :string 0 :meta 0} @calls))
          failure (diagnostic #(selection value))]
      (is (= "C3-ID" (:id failure)))
      (is (= [:native-syntax-selection] (:missing-fields failure)))
      (is (= {:hash 0 :equals 0 :string 0 :meta 0} @calls)))))

(deftest selected-source-is-a-real-snapshot-and-public-DATA-is-checked
  (let [source (selection (invocation))
        bytes (.getBytes ^String (:source-text source)
                         java.nio.charset.StandardCharsets/UTF_8)
        hash (format "%064x"
                     (java.math.BigInteger.
                      1
                      (.digest
                       (java.security.MessageDigest/getInstance "SHA-256")
                       bytes)))
        data ((owner 'sh04-development-syntax-data!)
              "<sh04-selection-test>" source)
        mutations [[":reference-order :prior-only" ":reference-order :unchecked"]
                   [":reference-order :prior-only"
                    ":unexpected true :reference-order :prior-only"]
                   ["(def c3-syntax-diagnostic-catalog"
                    "(def removed-diagnostic-catalog"]
                   [":stops [:macro-expansion :name-resolution :analysis]"
                    ":stops [:unchecked]"]]]
    (is (= (alength bytes) (:source-byte-count source)))
    (is (= (str "sha256:" hash) (:source-content-hash source)))
    (is (= #{'c3-syntax-object-contract 'c3-syntax-diagnostic-catalog}
           (set (keys data))))
    (doseq [[old new] mutations]
      (let [changed (update source :source-text str/replace old new)
            failure (diagnostic #((owner 'sh04-development-syntax-data!)
                                  "<sh04-selection-test>" changed))]
        (is (not= (:source-text source) (:source-text changed)))
        (is (= "C3-ID" (:id failure)))
        (is (= [:syntax-public-DATA-contract]
               (:missing-fields failure)))))))

(deftest all-eleven-exports-and-entry-shapes-are-required
  (let [source (selection (invocation))
        data ((owner 'sh04-development-syntax-data!)
              "<sh04-selection-test>" source)
        shapes b/sh04-syntax-public-function-shapes
        exports (vec (concat (keys shapes) (keys data)))
        plan {:kind :gravity/stage2-compiler-artifact-plan
              :compiler-artifact-plan? true
              :module {:module 'gravity.bootstrap.syntax :profile :meta :target :jvm
                       :effects #{} :capabilities #{} :safety :safe :exports exports}}
        check (owner 'sh04-development-syntax-interface!)
        identities {:public-function-shapes shapes}]
    (is (= 11 (count (check "<sh04-selection-test>" plan data identities))))
    (doseq [changed [(update-in plan [:module :exports] pop)
                     (update-in plan [:module :exports] conj 'unexpected)
                     (update-in plan [:module :exports] conj (first exports))]]
      (let [failure (diagnostic #(check "<sh04-selection-test>"
                                        changed data identities))]
        (is (= "C3-ID" (:id failure)))
        (is (= [:current-syntax-module-and-exports]
               (:missing-fields failure)))))
    (is (= [:current-syntax-module-and-exports]
           (:missing-fields
            (diagnostic #(check "<sh04-selection-test>" plan data
                                (assoc-in identities
                                          [:public-function-shapes
                                           'c3-syntax-build-template :arity]
                                          2))))))))

(deftest absent-selection-retains-the-frozen-source-guard-and-cache
  (let [cache @(owner 'sh04-syntax-cached-binding)
        before (realized? cache)
        legacy (b/sh04-syntax-current-binding!
                "<sh04-selection-test>")]
    (is (= 81241 b/sh04-syntax-expected-source-byte-count))
    (is (= :gravity/sh04-pinned-syntax-plan-binding (:artifact legacy)))
    (is (= :complete (:status legacy)))
    (doseq [invocation [nil (reader-invocation)]]
      (is (= :gravity/sh04-pinned-syntax-plan-binding
             (:artifact (b/sh04-syntax-current-binding!
                         "<sh04-selection-test>" invocation)))))
    (is (not= before (realized? cache)))))

(deftest selected-context-and-implementation-projection-are-separated
  (let [source (selection (invocation))
        context ((owner 'sh04-development-syntax-context!)
                 (:source-path source) (invocation))
        attempt (try
                  {:binding (b/sh04-syntax-assembly-binding!
                             "<sh04-selection-test>" (invocation))}
                  (catch clojure.lang.ExceptionInfo error
                    {:failure (ex-data error)}))]
    (is (= (invocation) context))
    (if-let [failure (:failure attempt)]
      (do
       ;; Current main still carries the pre-#512 c3-syntax-bounds export.
       ;; Preserve this diagnostic until the pending source-owner migration
       ;; removes that obsolete export; the positive branch is exercised once
       ;; the current owner source has the eleven-entry interface.
       (is (= "C3-ID" (:id failure)))
       (is (= [:current-syntax-module-and-exports]
              (:missing-fields failure))))
      (let [binding (:binding attempt)
            projection (:implementation-projection binding)]
        (is (= :development-current (:source-mode binding)))
        (is (= :development-unadmitted (:admission-status binding)))
        (is (= :development-unadmitted (:admission-status projection)))
        (is (= (:implementation-binding-id binding)
               (:binding-id projection)))
        (is (= #{:form :span :source :namespace :phase :profile :metadata
                 :hygiene :origin :facts :reader-binding :reader-source-revision
                 :ownership :version}
               (set (:identity-fields (var-get (owner 'sh04-development-syntax-contract))))))
        (is (not (contains? (:program-identity binding) :source-path)))
        (is (contains? projection :public-DATA))))))
