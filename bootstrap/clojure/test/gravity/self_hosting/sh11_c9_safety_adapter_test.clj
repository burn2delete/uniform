(ns gravity.self-hosting.sh11-c9-safety-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh10-c8-ownership-adapter-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh11_c9_safety_adapter_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH11-C9-SAFETY-ADAPTER-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- compile-plan
  [relative-path]
  (let [source-path (str (.resolve @root relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c9-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")))

(def ^:private c10-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity")))

;; Required stable-candidate execution command (the namespace itself does not
;; promise deftest order):
;; clojure -J-Xmx8g -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-iteration-cache-runner --fail-fast --test-var gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-source-api-is-complete --test-var gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-identity-binding-is-sequential-and-exact --test-var gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-adapter-binds-one-real-read --test-var gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-generic-classifier-and-substitutions-fail-closed --test-var gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-authenticated-gravity-boundary --max-cache-entries 1

(defn- invoke-plan
  [plan engine function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine engine :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-c9 [function arguments]
  (invoke-plan c9-plan :gravity-sh11-c9-identity-test function arguments))

(defn- invoke-c10 [function arguments]
  (invoke-plan c10-plan :gravity-sh11-c9-safety-test function arguments))

(defn- sh10-var [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh10-c8-ownership-adapter-test name)))

(defn- sh09-var [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh09-c7-effect-adapter-test name)))

(defn- digest [ordinal]
  (format "sha256:%064x" (long ordinal)))

(defn- resolve-requests
  [requests first-ordinal]
  (mapv (fn [ordinal request]
          {:request request :digest (digest ordinal)})
        (range first-ordinal (+ first-ordinal (count requests)))
        requests))

(defn- resolve-real-requests
  [requests]
  (mapv (fn [request]
          {:request request
           :digest
           (str "sha256:" (bootstrap/sha256-hex (pr-str request)))})
        requests))

(defn- node-bound-carrier
  [full-width-vectors final-width]
  (vec
   (concat
    (repeat full-width-vectors (vec (repeat 1024 :leaf)))
    [(vec (repeat final-width :leaf))])))

(defn- prepared-c9
  ([] (prepared-c9 "/checkout-a/function.gravity"))
  ([actual-path]
  (let [prepared ((sh10-var 'prepared-bound) actual-path)
        c8-bound (:bound prepared)
        c8-verification (:binding-verification prepared)
        owned
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [c8-bound c8-verification])
        fact-template
        (invoke-c9
         'sh10-authenticated-ownership-identity-requests
         [c8-bound c8-verification owned])
        fact-resolutions
        (resolve-requests (:fact-requests fact-template) 101)
        core-template
        (invoke-c9
         'sh10-authenticated-ownership-core-identity-request
         [c8-bound c8-verification owned fact-resolutions])
        resolved
        {:fact-resolutions fact-resolutions
         :core-resolution
         {:request (:core-request core-template) :digest (digest 201)}}
        bound
        (invoke-c9
         'sh10-bind-authenticated-ownership-identities
         [c8-bound c8-verification owned resolved])
        verification
        (invoke-c9
         'sh10-verify-authenticated-ownership-identities
         [c8-bound c8-verification owned resolved bound])]
    {:prepared prepared
     :c8-bound c8-bound
     :c8-verification c8-verification
     :owned owned
     :fact-template fact-template
     :fact-resolutions fact-resolutions
     :core-template core-template
     :resolved resolved
     :bound bound
     :verification verification})))

(defn- prepared-c10
  ([] (prepared-c10 "/checkout-a/function.gravity"))
  ([actual-path]
  (let [c9 (prepared-c9 actual-path)
        safety
        (invoke-c10
         'sh11-build-authenticated-safety-core
         [(:bound c9) (:verification c9)])
        proof-template
        (invoke-c10
         'sh11-authenticated-safety-identity-requests
         [(:bound c9) (:verification c9) safety])
        proof-resolution
        {:request (:proof-request proof-template) :digest (digest 301)}
        result-template
        (invoke-c10
         'sh11-authenticated-safety-result-identity-request
         [(:bound c9) (:verification c9) safety proof-resolution])
        result-resolution
        {:request (:result-request result-template) :digest (digest 302)}
        core-template
        (invoke-c10
         'sh11-authenticated-safety-core-identity-request
         [(:bound c9) (:verification c9) safety
          proof-resolution result-resolution])
        resolved
        {:proof-resolution proof-resolution
         :result-resolution result-resolution
         :core-resolution
         {:request (:core-request core-template) :digest (digest 303)}}
        bound
        (invoke-c10
         'sh11-bind-authenticated-safety-identities
         [(:bound c9) (:verification c9) safety resolved])
        verification
        (invoke-c10
         'sh11-verify-authenticated-safety-identities
         [(:bound c9) (:verification c9) safety resolved bound])]
    (assoc c9
           :safety safety
           :proof-template proof-template
           :proof-resolution proof-resolution
           :result-template result-template
           :result-resolution result-resolution
           :safety-core-template core-template
           :safety-resolved resolved
           :safety-bound bound
           :safety-verification verification))))

(deftest sh11-c9-identity-binding-is-sequential-and-exact
  (let [prepared (prepared-c9)
        bound (:bound prepared)
        identity (first (:fact-identities bound))
        fact-request
        (get-in prepared [:fact-resolutions 0 :request])
        fact-input (:identity-input fact-request)
        ownership-request
        (get-in bound [:ownership-core :ownership-requests 0])
        ownership-result
        (get-in bound [:ownership-core :ownership-results 0])
        ownership-fact
        (get-in ownership-result [:ownership-facts 0])]
    (is (= :accepted (:status (:owned prepared))))
    (is (= :accepted (:status (:fact-template prepared))))
    (is (= :accepted (:status (:core-template prepared))))
    (is (= :accepted (:status bound)))
    (is (= :passed (:status (:verification prepared))))
    (is (= (:fact-id-request identity)
           (get-in bound
                   [:ownership-core :ownership-results 0
                    :ownership-facts 0 :fact-id-request])))
    (is (= :gravity/sh10-ownership-fact-identity-v2
           (:domain fact-request)))
    (is (= ownership-fact (:fact fact-input)))
    (is (= (:fact-id-request ownership-fact)
           (:fact-id-request fact-input)))
    (is (= ownership-request (:ownership-request fact-input)))
    (is (= ownership-result (:ownership-result fact-input)))
    (is (= (:source-span ownership-fact) (:source-span fact-input)))
    (is (= (:origin-chain ownership-fact) (:origin-chain fact-input)))
    (is (= (:profile ownership-request) (:profile fact-input)))
    (is (= (:target ownership-request) (:target fact-input)))
    (is (= (:fact-identities bound)
           (get-in (:core-template prepared)
                   [:core-request :identity-input
                    :ordered-fact-identities])))
    (is (= :gravity/sh10-ownership-core-identity-v2
           (get-in (:core-template prepared) [:core-request :domain])))
    (is (= (:identity-input (:owned prepared))
           (get-in (:core-template prepared)
                   [:core-request :identity-input
                    :ownership-core-identity-input])))
    (is (= (get-in (:resolved prepared) [:core-resolution :digest])
           (:ownership-core-identity-id bound)))
    (is (= :coordinator-resolution-shape-checked
           (:identity-binding-status bound)))
    (is (some #{:trusted-digest-resolution} (:pending bound)))
    (is (not (contains? (:identity-input bound) :provenance)))
    (is (= :rejected
           (:status
            (invoke-c9
             'sh10-bind-authenticated-ownership-identities
             [(:c8-bound prepared) (:c8-verification prepared)
              (:owned prepared)
              (assoc-in (:resolved prepared)
                        [:core-resolution :digest]
                        (get-in (:resolved prepared)
                                [:fact-resolutions 0 :digest]))]))))))

(deftest sh11-c9-safety-adapter-binds-one-real-read
  (let [prepared (prepared-c10)
        path-peer (prepared-c10 "/checkout-b/function.qst")
        safety (:safety prepared)
        proof-request (:proof-request (:proof-template prepared))
        result-template (:result-template prepared)
        operation (:operation result-template)
        result (:result result-template)
        result-request (:result-request result-template)
        core-request
        (:core-request (:safety-core-template prepared))
        bound (:safety-bound prepared)
        proof-resolution (:proof-resolution prepared)
        result-resolution (:result-resolution prepared)
        core-resolution
        (get-in prepared [:safety-resolved :core-resolution])]
    (is (= :accepted (:status safety)))
    (is (= :coordinator-digest-required (:identity-resolution safety)))
    (is (not (contains? safety :safety-result)))
    (is (= :accepted (:status (:proof-template prepared))))
    (is (= :load (:kind operation)))
    (is (= :persistent-immutable
           (get-in operation [:facts :ownership-kind])))
    (is (= :initialized (get-in operation [:facts :initialization])))
    (is (= :available (get-in operation [:facts :availability])))
    (is (= :read (get-in operation [:facts :event])))
    (is (= :proven-safe (:outcome result)))
    (is (= :persistent-immutable-read
           (get-in result [:proofs 0 :claim])))
    (is (= :SAFE-PERSISTENT-READ
           (get-in result [:outcomes 0 :specialized-safe-rule])))
    (is (= :external-coordinator-digest-boundary
           (get-in result [:proofs 0 :provider])))
    (is (= proof-request (:request proof-resolution)))
    (is (= (:digest proof-resolution) (:proof-id result-request)))
    (is (= operation (:operation result-request)))
    (is (= result (:result result-request)))
    (is (= (:result-verification result-template)
           (:verification result-request)))
    (is (= (:ownership-core-identity-id (:bound prepared))
           (:ownership-core-identity-id result-request)))
    (is (= (:digest proof-resolution) (:proof-id core-request)))
    (is (= (:digest result-resolution) (:result-id core-request)))
    (is (= result (:result core-request)))
    (is (= (:result-verification result-template)
           (:result-verification core-request)))
    (is (= :accepted (:status bound)))
    (is (= :passed (:status (:safety-verification prepared))))
    (is (= (:digest proof-resolution) (:proof-id bound)))
    (is (= (:digest result-resolution) (:result-id bound)))
    (is (= (:digest core-resolution) (:safety-core-id bound)))
    (is (= [:trusted-digest-resolution
            :independent-canonical-digest-verification
            :authoritative-safety-certificate]
           (:nonclaims bound)))
    (is (not (contains? (:identity-input bound) :provenance)))
    (is (= (:provenance safety) (:provenance bound)))
    (is (= (:proof-template prepared) (:proof-template path-peer)))
    (is (= (:result-template prepared) (:result-template path-peer)))
    (is (= (:safety-core-template prepared)
           (:safety-core-template path-peer)))
    (is (= (:identity-input bound)
           (get-in path-peer [:safety-bound :identity-input])))
    (is (not= (:provenance bound)
              (get-in path-peer [:safety-bound :provenance])))
    (doseq [value-type
            [:gravity.type/integer :gravity.type/bool :gravity.type/string]]
      (let [typed-proof (assoc proof-request :value-type value-type)
            typed-resolution
            {:request typed-proof :digest (:digest proof-resolution)}
            typed-operation
            (invoke-c10
             'sh11-resolved-load-operation
             [typed-proof (:digest typed-resolution)])
            typed-result
            (invoke-c10
             'sh11-classify-authenticated-load
             [typed-operation typed-proof typed-resolution])]
        (is (= value-type (get-in typed-operation [:facts :value-type])))
        (is (= :proven-safe (:outcome typed-result)))))
    (let [unsupported-proof
          (assoc proof-request :value-type :gravity.type/decimal)
          unsupported-resolution
          {:request unsupported-proof :digest (:digest proof-resolution)}
          unsupported-operation
          (invoke-c10
           'sh11-resolved-load-operation
           [unsupported-proof (:digest unsupported-resolution)])]
      (is (= :rejected
             (:status
              (invoke-c10
               'sh11-classify-authenticated-load
               [unsupported-operation unsupported-proof
                unsupported-resolution])))))))

(deftest sh11-generic-classifier-and-substitutions-fail-closed
  (let [prepared (prepared-c10)
        operation (:operation (:result-template prepared))
        proof-request (:proof-request (:proof-template prepared))
        proof-resolution (:proof-resolution prepared)
        generic (invoke-c10 'sh11-classify-operation [operation])
        altered-runtime (assoc operation :runtime-check {:check-id (digest 401)})
        altered-unsafe (assoc operation :unsafe-audit {:audit-id (digest 402)})
        altered-state (assoc-in operation [:facts :availability] :moved)
        altered-proof-digest
        (assoc-in (:safety-resolved prepared)
                  [:proof-resolution :digest]
                  (digest 499))
        altered-proof-request
        (assoc-in proof-resolution [:request :profile] :runtime)
        altered-result-request
        (assoc-in (:result-resolution prepared)
                  [:request :domain] :gravity/substituted-result)
        altered-core-request
        (assoc-in (get-in prepared
                          [:safety-resolved :core-resolution])
                  [:request :domain] :gravity/substituted-core)
        proof-digest
        (get-in prepared [:safety-resolved :proof-resolution :digest])
        result-digest
        (get-in prepared [:safety-resolved :result-resolution :digest])
        proof-result-collision-result-resolution
        (assoc (:result-resolution prepared) :digest proof-digest)
        proof-result-collision-core-template
        (invoke-c10
         'sh11-authenticated-safety-core-identity-request
         [(:bound prepared) (:verification prepared) (:safety prepared)
          proof-resolution proof-result-collision-result-resolution])
        proof-result-collision-resolved
        {:proof-resolution proof-resolution
         :result-resolution proof-result-collision-result-resolution
         :core-resolution
         {:request (:core-request proof-result-collision-core-template)
          :digest
          (get-in prepared [:safety-resolved :core-resolution :digest])}}
        bind-result
        (fn [resolved]
          (invoke-c10
           'sh11-bind-authenticated-safety-identities
           [(:bound prepared) (:verification prepared)
            (:safety prepared) resolved]))]
    (is (= :rejected (:status generic)))
    (is (= :malformed-safety-operation
           (get-in generic [:diagnostics 0 :reason])))
    (doseq [candidate [altered-runtime altered-unsafe altered-state]]
      (is (= :rejected
             (:status
              (invoke-c10
               'sh11-classify-authenticated-load
               [candidate proof-request proof-resolution])))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-authenticated-safety-result-identity-request
             [(:bound prepared) (:verification prepared)
              (:safety prepared) altered-proof-request]))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-authenticated-safety-core-identity-request
             [(:bound prepared) (:verification prepared)
              (:safety prepared) proof-resolution
              altered-result-request]))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-bind-authenticated-safety-identities
             [(:bound prepared) (:verification prepared)
              (:safety prepared)
              (assoc-in (:safety-resolved prepared)
                        [:core-resolution] altered-core-request)]))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-bind-authenticated-safety-identities
             [(:bound prepared) (:verification prepared)
              (:safety prepared) altered-proof-digest]))))
    (doseq [resolved
            [proof-result-collision-resolved
             (assoc-in (:safety-resolved prepared)
                       [:core-resolution :digest] proof-digest)
             (assoc-in (:safety-resolved prepared)
                       [:core-resolution :digest] result-digest)]]
      (let [rejected (bind-result resolved)]
        (is (= :rejected (:status rejected)))
        (is (= :duplicate-safety-identity-digest
               (get-in rejected [:diagnostics 0 :reason])))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-bind-authenticated-safety-identities
             [(:bound prepared) (:verification prepared)
              (:safety prepared)
              (assoc-in (:safety-resolved prepared)
                        [:proof-resolution :digest] "not-a-sha")]))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-build-authenticated-safety-core
             [(assoc (:bound prepared)
                     :ownership-core-identity-id (digest 777))
              (:verification prepared)]))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-build-authenticated-safety-core
             [(:bound prepared)
              (assoc (:verification prepared) :status :rejected)]))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-authenticated-safety-identity-requests
             [(:bound prepared) (:verification prepared)
              (assoc (:safety prepared) :pending [])]))))
    (doseq [candidate
            [(assoc-in (:safety-bound prepared)
                       [:operation :facts :availability] :moved)
             (assoc-in (:safety-bound prepared)
                       [:safety-result :outcome] :rejected)
             (assoc-in (:safety-bound prepared)
                       [:identity-input :proof-id] (digest 778))
             (assoc (:safety-bound prepared)
                    :provenance {:actual-source-path "/forged"})]]
      (is (= :rejected
             (:status
              (invoke-c10
               'sh11-verify-authenticated-safety-identities
               [(:bound prepared) (:verification prepared)
                (:safety prepared) (:safety-resolved prepared)
                candidate])))))
    (is (= :rejected
           (:status
            (invoke-c10
             'sh11-verify-authenticated-safety-identities
             [(:bound prepared) (:verification prepared)
              (:safety prepared) (:safety-resolved prepared)
              (assoc (:safety-bound prepared) :pending [])]))))))

(deftest sh11-c9-safety-source-api-is-complete
  (let [c9-functions (:functions @c9-plan)
        c10-functions (:functions @c10-plan)
        policy (invoke-c10 'sh11-authenticated-safety-adapter-policy [])
        c9-at-bound (node-bound-carrier 15 1007)
        c9-over-bound (node-bound-carrier 15 1008)
        c10-at-bound (node-bound-carrier 31 991)
        c10-over-bound (node-bound-carrier 31 992)
        wide-vector (vec (repeat 1025 :leaf))
        wide-map (into {} (map (fn [n] [n n]) (range 513)))
        wide-set (set (range 513))
        depth-at-bound (nth (iterate vector :leaf) 16)
        depth-over-bound (nth (iterate vector :leaf) 17)
        fixture-base
        (.resolve @root
                  "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted")
        gravity-bytes
        (java.nio.file.Files/readAllBytes
         (.resolve fixture-base "function-single-bool-call.gravity"))
        qst-bytes
        (java.nio.file.Files/readAllBytes
         (.resolve fixture-base "function-single-bool-call.qst"))]
    (is (= {:arity 3 :params ['bound 'verification 'owned]}
           (select-keys
            (get c9-functions
                 'sh10-authenticated-ownership-identity-requests)
            [:arity :params])))
    (is (= {:arity 4
            :params ['bound 'verification 'owned 'fact-resolutions]}
           (select-keys
            (get c9-functions
                 'sh10-authenticated-ownership-core-identity-request)
            [:arity :params])))
    (is (= {:arity 2 :params ['bound 'verification]}
           (select-keys
            (get c10-functions 'sh11-build-authenticated-safety-core)
            [:arity :params])))
    (is (= {:arity 5
            :params ['bound 'verification 'safety
                     'proof-resolution 'result-resolution]}
           (select-keys
            (get c10-functions
                 'sh11-authenticated-safety-core-identity-request)
            [:arity :params])))
    (is (= #{:load} (:accepted-operation-kinds policy)))
    (is (not (contains?
              (:operation-kinds
               (invoke-c10 'sh11-safety-policy []))
              :load)))
    (is (some #{:authenticated-non-persistent-read-safety-families}
              (:pending (invoke-c10 'sh11-safety-policy []))))
    (is (not (some #{:authenticated-sh09-sh10-adapter}
                   (:pending (invoke-c10 'sh11-safety-policy [])))))
    (is (java.util.Arrays/equals gravity-bytes qst-bytes))
    (is (= {:status :accepted :nodes 16384}
           (invoke-c9 'sh10-identity-carrier-preflight [c9-at-bound])))
    (is (= :identity-carrier-node-bound
           (:reason
            (invoke-c9 'sh10-identity-carrier-preflight [c9-over-bound]))))
    (is (= {:status :accepted :nodes 32768}
           (invoke-c10 'sh11-authenticated-identity-preflight
                       [c10-at-bound])))
    (is (= :authenticated-identity-node-bound
           (:reason
            (invoke-c10 'sh11-authenticated-identity-preflight
                        [c10-over-bound]))))
    (doseq [[invoke function width-reason depth-reason seq-reason]
            [[invoke-c9 'sh10-identity-carrier-preflight
              :identity-carrier-width-bound
              :identity-carrier-depth-bound
              :identity-carrier-seq-unsupported]
             [invoke-c10 'sh11-authenticated-identity-preflight
              :authenticated-identity-width-bound
              :authenticated-identity-depth-bound
              :authenticated-identity-seq-unsupported]]]
      (is (= :accepted (:status (invoke function [depth-at-bound]))))
      (is (= depth-reason (:reason (invoke function [depth-over-bound]))))
      (doseq [carrier [wide-vector wide-map wide-set]]
        (is (= width-reason (:reason (invoke function [carrier])))))
      (is (= seq-reason (:reason (invoke function [(list :leaf)])))))))

(deftest sh11-c9-safety-authenticated-gravity-boundary
  ;; Stable-candidate boundary only: one .gravity SH-08 carrier, no .qst build.
  ;; The documented exact runner selects this last, after the four explicit
  ;; synthetic selectors, so fail-fast skips the cold carrier on prefix failure.
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
         "accepted" "function-single-bool-call" ".gravity")
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
        effect-template
        (invoke-c8
         'sh09-authenticated-effect-identity-requests
         [typed typed-verification effected])
        effect-resolutions (resolve-real-requests (:requests effect-template))
        effect-bound
        (invoke-c8
         'sh09-bind-authenticated-effect-identities
         [typed typed-verification effected effect-resolutions])
        effect-binding-verification
        (invoke-c8
         'sh09-verify-authenticated-effect-identities
         [typed typed-verification effected effect-resolutions effect-bound])
        owned
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [effect-bound effect-binding-verification])
        ownership-verification
        (invoke-c9
         'sh10-verify-authenticated-ownership-core
         [effect-bound effect-binding-verification owned])
        ownership-fact-template
        (invoke-c9
         'sh10-authenticated-ownership-identity-requests
         [effect-bound effect-binding-verification owned])
        ownership-fact-resolutions
        (resolve-real-requests (:fact-requests ownership-fact-template))
        ownership-core-template
        (invoke-c9
         'sh10-authenticated-ownership-core-identity-request
         [effect-bound effect-binding-verification owned
          ownership-fact-resolutions])
        ownership-resolved
        {:fact-resolutions ownership-fact-resolutions
         :core-resolution
         {:request (:core-request ownership-core-template)
          :digest
          (str "sha256:"
               (bootstrap/sha256-hex
                (pr-str (:core-request ownership-core-template))))}}
        ownership-bound
        (invoke-c9
         'sh10-bind-authenticated-ownership-identities
         [effect-bound effect-binding-verification owned ownership-resolved])
        ownership-binding-verification
        (invoke-c9
         'sh10-verify-authenticated-ownership-identities
         [effect-bound effect-binding-verification owned ownership-resolved
          ownership-bound])
        safety
        (invoke-c10
         'sh11-build-authenticated-safety-core
         [ownership-bound ownership-binding-verification])
        proof-template
        (invoke-c10
         'sh11-authenticated-safety-identity-requests
         [ownership-bound ownership-binding-verification safety])
        proof-resolution
        {:request (:proof-request proof-template)
         :digest
         (str "sha256:"
              (bootstrap/sha256-hex (pr-str (:proof-request proof-template))))}
        result-template
        (invoke-c10
         'sh11-authenticated-safety-result-identity-request
         [ownership-bound ownership-binding-verification safety
          proof-resolution])
        result-resolution
        {:request (:result-request result-template)
         :digest
         (str "sha256:"
              (bootstrap/sha256-hex (pr-str (:result-request result-template))))}
        safety-core-template
        (invoke-c10
         'sh11-authenticated-safety-core-identity-request
         [ownership-bound ownership-binding-verification safety
          proof-resolution result-resolution])
        safety-resolved
        {:proof-resolution proof-resolution
         :result-resolution result-resolution
         :core-resolution
         {:request (:core-request safety-core-template)
          :digest
          (str "sha256:"
               (bootstrap/sha256-hex
                (pr-str (:core-request safety-core-template))))}}
        safety-bound
        (invoke-c10
         'sh11-bind-authenticated-safety-identities
         [ownership-bound ownership-binding-verification safety
          safety-resolved])
        safety-verification
        (invoke-c10
         'sh11-verify-authenticated-safety-identities
         [ownership-bound ownership-binding-verification safety
          safety-resolved safety-bound])]
    (is (= :accepted (:status typed)))
    (is (= :passed (:status typed-verification)))
    (is (= :accepted (:status effected)))
    (is (= :passed (:status effect-verification)))
    (is (= :accepted (:status effect-bound)))
    (is (= :passed (:status effect-binding-verification)))
    (is (= :accepted (:status owned)))
    (is (= :passed (:status ownership-verification)))
    (is (= :accepted (:status ownership-fact-template)))
    (is (= :accepted (:status ownership-core-template)))
    (is (= :accepted (:status ownership-bound)))
    (is (= :passed (:status ownership-binding-verification)))
    (is (= :accepted (:status safety)))
    (is (= :accepted (:status proof-template)))
    (is (= :accepted (:status result-template)))
    (is (= :accepted (:status safety-core-template)))
    (is (= :accepted (:status safety-bound)))
    (is (= :passed (:status safety-verification)))
    (is (= :load (get-in safety-bound [:operation :kind])))
    (is (= :gravity.type/bool
           (get-in safety-bound [:operation :facts :value-type])))
    (is (= :persistent-immutable
           (get-in safety-bound [:operation :facts :ownership-kind])))
    (is (= :read (get-in safety-bound [:operation :facts :event])))
    (is (= :proven-safe (get-in safety-bound [:safety-result :outcome])))
    (is (= :persistent-immutable-read
           (get-in safety-bound [:safety-result :proofs 0 :claim])))
    (is (= (:provenance typed) (:provenance effected)
           (:provenance effect-bound) (:provenance owned)
           (:provenance ownership-bound) (:provenance safety)
           (:provenance safety-bound)))
    (is (not (contains? (:identity-input safety-bound) :provenance)))
    (is (= (:ownership-core-identity-id ownership-bound)
           (get-in safety-bound
                   [:operation :facts :ownership-core-identity-id])))
    (is (= (:ownership-fact-id (first (:fact-identities ownership-bound)))
           (:ownership-fact-id (:operation safety-bound))))
    (doseq [candidate
            [(assoc-in safety-bound [:operation :facts :event] :move)
             (assoc-in safety-bound [:safety-result :outcome] :rejected)
             (assoc-in safety-bound [:identity-input :proof-id] (digest 901))
             (assoc safety-bound :pending [])]]
      (is (= :rejected
             (:status
              (invoke-c10
               'sh11-verify-authenticated-safety-identities
               [ownership-bound ownership-binding-verification safety
                safety-resolved candidate])))))))
