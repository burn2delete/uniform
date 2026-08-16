(ns gravity.self-hosting.sh10-c8-ownership-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh09-c7-effect-adapter-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh10_c8_ownership_adapter_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH10-C8-ADAPTER-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c9-relative-path
  "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")

(defn- compile-c9-plan
  []
  (let [source-path (str (.resolve @root c9-relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c9-plan (delay (compile-c9-plan)))

(defn- invoke-c9
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh10-c8-ownership-adapter-test
    :compiler-artifact-plan? true}
   @c9-plan function arguments))

(defn- sh09-var
  [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh09-c7-effect-adapter-test name)))

(defn- prepared-bound-products
  [typed verification]
  (let [effected ((sh09-var 'build) typed verification)
        invoke-c8 (sh09-var 'invoke-c8)
        template
        (invoke-c8
         'sh09-authenticated-effect-identity-requests
         [typed verification effected])
        digests
        (repeat
         (count (:requests template))
         "sha256:1111111111111111111111111111111111111111111111111111111111111111")
        resolved
        (mapv (fn [request digest]
                {:request request :digest digest})
              (:requests template) digests)
        bound
        (invoke-c8
         'sh09-bind-authenticated-effect-identities
         [typed verification effected resolved])
        binding-verification
        (invoke-c8
         'sh09-verify-authenticated-effect-identities
         [typed verification effected resolved bound])]
    {:typed typed
     :typed-verification verification
     :effected effected
     :resolved resolved
     :bound bound
     :binding-verification binding-verification
     :invoke-c8 invoke-c8}))

(defn- prepared-bound
  [actual-path]
  (let [typed ((sh09-var 'function-typed-result) actual-path)]
    (prepared-bound-products
     typed ((sh09-var 'function-verification) typed))))

(defn- prepared-primitive-bound
  [actual-path]
  (let [typed ((sh09-var 'typed-result) actual-path)]
    (prepared-bound-products
     typed ((sh09-var 'upstream-verification) typed))))

(deftest sh10-c8-adapter-source-api-and-policy-are-exact
  (let [functions (:functions @c9-plan)
        policy (invoke-c9 'sh10-authenticated-sh09-adapter-policy [])]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @c9-plan)))
    (is (= {:arity 0 :params []}
           (select-keys
            (get functions 'sh10-authenticated-sh09-adapter-policy)
            [:arity :params])))
    (is (= {:arity 2 :params ['bound 'verification]}
           (select-keys
            (get functions 'sh10-build-authenticated-ownership-core)
            [:arity :params])))
    (is (= {:arity 3 :params ['bound 'verification 'candidate]}
           (select-keys
            (get functions 'sh10-verify-authenticated-ownership-core)
            [:arity :params])))
    (is (= #{:gravity.type/integer
             :gravity.type/bool
             :gravity.type/string}
           (:accepted-types policy)))
    (is (= #{:pure-authenticated-sh08-primitive-typed-core
             :declared-pure-call-effects-with-thrown-effects-pending}
           (:accepted-upstream-scopes policy)))
    (is (= :persistent-immutable (:ownership-kind policy)))
    (is (= [:read] (:accepted-events policy)))
    (is (some #{:authenticated-effectful-or-nonprimitive-sh09-adapter}
              (:pending (invoke-c9 'sh10-ownership-policy []))))))

(deftest sh10-c8-adapter-accepts-persistent-primitive-read
  (let [a (prepared-bound "/checkout-a/function.gravity")
        b (prepared-bound "/checkout-b/function.qst")
        result-a
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [(:bound a) (:binding-verification a)])
        result-b
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [(:bound b) (:binding-verification b)])
        request (first (:ownership-requests result-a))
        ownership (first (:ownership-results result-a))]
    (is (= :accepted (:status result-a) (:status result-b)))
    (is (= :persistent-immutable-pure-primitive-reads
           (:scope result-a)))
    (is (= :persistent-immutable (:ownership-kind request)))
    (is (= :initialized (:initialization request)))
    (is (= [:read] (mapv :op (:events request))))
    (is (= :accepted (:status ownership)))
    (is (= :persistent-immutable-read-only (:scope ownership)))
    (is (= [:read]
           (mapv :operation (:ownership-facts ownership))))
    (is (= (:identity-input result-a) (:identity-input result-b)))
    (is (not= (:provenance result-a) (:provenance result-b)))
    (is (not (contains? (:identity-input result-a) :provenance)))
    (is (= :passed
           (:status
            (invoke-c9
             'sh10-verify-authenticated-ownership-core
             [(:bound a) (:binding-verification a) result-a]))))))

(deftest sh10-c8-adapter-accepts-primitive-type-family
  (let [prepared (prepared-primitive-bound "/checkout-a/primitive.gravity")
        result
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [(:bound prepared) (:binding-verification prepared)])]
    (is (= :accepted (:status result)))
    (is (= 2 (count (:ownership-requests result))))
    (is (= #{:gravity.type/integer :gravity.type/bool}
           (set
            (map
             (fn [request]
               (get-in
                (:bound prepared)
                [:effected-core :type-table (:value-id request)]))
             (:ownership-requests result)))))
    (is (every? #(= :persistent-immutable (:ownership-kind %))
                (:ownership-requests result)))
    (is (every? #(= :persistent-immutable-read-only (:scope %))
                (:ownership-results result)))
    (is (true?
         (invoke-c9
          'sh10-authenticated-primitive-type?
          [:gravity.type/string])))))

(deftest sh10-c8-adapter-rejects-mutation-and-non-read-events
  (let [prepared (prepared-bound "/checkout-a/function.gravity")
        bound (:bound prepared)
        binding-verification (:binding-verification prepared)
        accepted
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [bound binding-verification])
        altered-bound
        (assoc-in bound
                  [:effected-core :type-table
                   (first (keys (:fact-identities bound)))]
                  :gravity.type/decimal)
        altered-verification
        ((:invoke-c8 prepared)
         'sh09-verify-authenticated-effect-identities
         [(:typed prepared) (:typed-verification prepared)
          (:effected prepared) (:resolved prepared) altered-bound])
        move-request
        (assoc (first (:ownership-requests accepted))
               :events
               [{:event-id "move-event"
                 :op :move
                 :destination-owner "other-owner"
                 :source-span {:source "synthetic"}
                 :origin-chain []}])]
    (is (= :rejected (:status altered-verification)))
    (is (= :rejected
           (:status
            (invoke-c9
             'sh10-build-authenticated-ownership-core
             [altered-bound altered-verification]))))
    (is (= :rejected
           (:status
            (invoke-c9 'sh10-check-ownership-request [move-request]))))
    (is (= :malformed-normalized-ownership-request
           (get-in
            (invoke-c9 'sh10-check-ownership-request [move-request])
            [:diagnostics 0 :reason])))
    (is (= :rejected
           (:status
            (invoke-c9
             'sh10-verify-authenticated-ownership-core
             [bound binding-verification
              (assoc accepted :pending [])]))))))

(deftest sh10-c8-adapter-authenticated-gravity-boundary
  ;; Stable-candidate boundary only: one .gravity SH-08 carrier, no .qst build.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (require 'gravity.self-hosting.sh08-primitive-function-type-test)
  (let [fixture-namespace
        'gravity.self-hosting.sh08-function-call-type-test
        primitive-namespace
        'gravity.self-hosting.sh08-primitive-function-type-test
        fixture-artifact
        (var-get (ns-resolve fixture-namespace 'fixture-artifact))
        function-request
        (var-get (ns-resolve fixture-namespace 'function-request))
        invoke-c7
        (var-get (ns-resolve primitive-namespace 'invoke-c7))
        invoke-c8 (sh09-var 'invoke-c8)
        artifact
        (fixture-artifact
         "accepted" "function-value-typed-bool" ".gravity")
        request (function-request artifact)
        typed
        (invoke-c7 'sh08-function-type-core-artifact [request])
        typed-verification
        (invoke-c7 'sh08-verify-function-type-result [request typed])
        effected
        (invoke-c8
         'sh09-build-authenticated-pure-effect-result
         [typed typed-verification])
        effect-verification
        (invoke-c8
         'sh09-verify-authenticated-pure-effect-result
         [typed typed-verification effected])
        template
        (invoke-c8
         'sh09-authenticated-effect-identity-requests
         [typed typed-verification effected])
        resolved
        (mapv
         (fn [identity-request]
           {:request identity-request
            :digest
            (str "sha256:"
                 (bootstrap/sha256-hex (pr-str identity-request)))})
         (:requests template))
        bound
        (invoke-c8
         'sh09-bind-authenticated-effect-identities
         [typed typed-verification effected resolved])
        binding-verification
        (invoke-c8
         'sh09-verify-authenticated-effect-identities
         [typed typed-verification effected resolved bound])
        ownership
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [bound binding-verification])
        fresh
        (invoke-c9
         'sh10-verify-authenticated-ownership-core
         [bound binding-verification ownership])]
    (is (= :accepted (:status typed)))
    (is (= :passed (:status typed-verification)))
    (is (= :accepted (:status effected)))
    (is (= :passed (:status effect-verification)))
    (is (= :accepted (:status template)))
    (is (= :accepted (:status bound)))
    (is (= :passed (:status binding-verification)))
    (is (= :accepted (:status ownership)))
    (is (= :passed (:status fresh)))
    (is (= (count (:effect-requests effected))
           (count (:ownership-requests ownership))
           (count (:ownership-results ownership))))
    (is (every? #(= :persistent-immutable (:ownership-kind %))
                (:ownership-requests ownership)))
    (is (every? #(= [:read] (mapv :op (:events %)))
                (:ownership-requests ownership)))
    (is (every? #(= :accepted (:status %))
                (:ownership-results ownership)))
    (is (every? #(= [:read] (mapv :operation (:ownership-facts %)))
                (:ownership-results ownership)))
    (is (= (:provenance typed) (:provenance effected)
           (:provenance bound) (:provenance ownership)))
    (is (not (contains? (:identity-input ownership) :provenance)))
    (doseq [candidate
            [(assoc ownership :scope :owned-mutable)
             (assoc-in ownership
                       [:ownership-requests 0 :ownership-kind]
                       :owned-mutable)
             (assoc ownership :pending [])]]
      (is (= :rejected
             (:status
              (invoke-c9
               'sh10-verify-authenticated-ownership-core
               [bound binding-verification candidate])))))))
