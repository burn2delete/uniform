(ns gravity.self-hosting.pr453-current-source-handoff-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c6-c10.source-contract :as c6-source-contract]
            [gravity.c6-c10.source-interface :as c6-source-interface]
            [gravity.c11-mir.source-contract :as c11-source-contract]
            [gravity.c11-mir.source-interface :as c11-source-interface]))

(defn- temp-source-path []
  (let [path (java.nio.file.Files/createTempFile
              "gravity-pr453-handoff-" ".gravity"
              (make-array java.nio.file.attribute.FileAttribute 0))]
    (.toString path)))

(defn- source-text []
  (str "(ns gravity.pr453.handoff (:profile :hosted) (:target :jvm) "
       "(:effects #{}) (:capabilities #{}) (:safety :safe) "
       "(:exports [main]))\n"
       "(defn main [] (if true false true))\n"))

(defn- failure-data [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest v2-current-c6-source-owner-produces-a-real-binding
  (let [path (temp-source-path)]
    (try
      (let [binding (bootstrap/p15-s23-c6c10-source-binding!
                     path 2 :development-current)
            abi c6-source-contract/current-compiler-program-abi
            current-program (:source-program binding)]
        (is (= 2 (:source-contract-version binding)))
        (is (= :development-current (:source-mode binding)))
        (is (= :complete (:status binding)))
        (is (= (:source-relative-path abi)
               (:source-relative-path current-program)))
        (is (= (:required-functions abi) (:function-shapes binding)))
        (is (= (:source-byte-count current-program)
               (:source-byte-count binding)))
        (is (= (:source-content-hash current-program)
               (:source-content-hash binding))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest current-source-owner-rejects-unsupported-version-at-public-entry
  (let [path (temp-source-path)
        compile-var (ns-resolve 'gravity.bootstrap
                                'p15-s23-c6c10-compile-source-binding)]
    (try
      (let [data (failure-data
                  #(@compile-var path 4 :development-current))]
        (is (= "C6-VERIFY" (:id data)))
        (is (= :explicit-current-source-selection (:missing-fact data))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest current-c11-owner-rejects-unsupported-version-before-source-resolution
  (let [resolutions (atom 0)
        resolver-var (ns-resolve 'gravity.bootstrap
                                'p15-s23-c11-mir-resolve-source-path)
        data (failure-data
              (fn [] (with-redefs-fn
                 {resolver-var (fn [] (swap! resolutions inc) "unreachable.gravity")}
                 #(bootstrap/p15-s23-c11-mir-source-binding!
                   "pr453-c11-unsupported-current.gravity" :jvm 4
                   :development-current))))]
    (is (= "C11-VERIFY" (:id data)))
    (is (= :explicit-current-source-selection (:missing-fact data)))
    (is (zero? @resolutions))))

(deftest current-c6-owner-rejects-reader-source-drift-before-builder
  (let [path (temp-source-path)
        source-path (bootstrap/p15-s23-c6c10-resolve-source-path)
        canonical-source-path (.getCanonicalPath (java.io.File. source-path))
        source-bytes (java.nio.file.Files/size (.toPath (java.io.File. source-path)))
        source-binding-var (ns-resolve 'gravity.bootstrap
                                       'p15-s23-c6c10-source-binding!)
        reader-var (ns-resolve 'gravity.bootstrap
                               'p15-s23-c6c10-current-reader-program!)]
    (try
      (let [fake-binding {:status :complete
                          :source-path source-path
                          :source-byte-count source-bytes
                          :source-content-hash "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}
            drifted-snapshot {:canonical-source-path canonical-source-path
                              :source-byte-count source-bytes
                              :source-content-hash "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"}
            data
            (failure-data
             (fn [] (with-redefs-fn
                {source-binding-var (fn [& _] fake-binding)
                 reader-var (fn [& _]
                              {:binding {}
                               :invocation {:reader-source-path source-path}
                               :c6-source-snapshot {:before drifted-snapshot
                                                    :after drifted-snapshot}})}
                #(bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
                  (bootstrap/p15-s23-stage2-gravity-checked-core-context
                   path (source-text) :jvm)
                  {:interface :gravity/c6-c10 :version 2}))))]
        (is (= "C6-VERIFY" (:id data)))
        (is (= :stable-current-c6-source-binding (:missing-fact data))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest v2-current-reader-owner-consumes-its-own-source
  (let [path (temp-source-path)
        reader-var (ns-resolve 'gravity.bootstrap
                               'p15-s23-c6c10-current-reader-program!)]
    (try
      (let [result (@reader-var path)
            binding (:binding result)
            contract c6-source-contract/current-reader-program
            binding-contract (:binding contract)]
        (is (= :selected-source-reader-plan (:verification-scope result)))
        (is (= :development-current
               (get-in result [:invocation :authenticated-envelope-options :source])))
        (is (= (.getCanonicalPath
                (java.io.File. (:source-relative-path contract)))
               (.getCanonicalPath
                (java.io.File. (get-in result [:binding :source-path])))))
        (is (= (:source-byte-count binding-contract)
               (:source-byte-count binding))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest v2-current-expression-owner-fixed-route-remains-fail-closed
  (let [rule (bootstrap/c-backend-stage2-plan-emitter-source-rule!
              "<pr453-expression-binding-probe>" :jvm)
        data (failure-data
              #(bootstrap/p15-s23-stage2-compiler-artifact-binding!
                (:emitter rule) "<pr453-expression-binding-probe>" :jvm 2))]
    ;; The historical v2 record is deliberately not rewritten when the
    ;; current emitter advances.  The public owner has a separate
    ;; development-current route below.
    (is (= "P15S23Q002" (:id data)))
    (is (= :current-expression-source-regular-file (:missing-fact data)))))

(deftest v2-current-expression-owner-derives-development-source
  (let [owner-var (ns-resolve
                   'gravity.bootstrap
                   'p15-s23-stage2-current-expression-development-binding!)
        source-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        binding (@owner-var "<pr453-development-expression-binding-probe>")
        contract c6-source-contract/current-expression-program]
    (is (= :complete (:status binding)))
    (is (= 2 (:source-contract-version binding)))
    (is (= :development-current (:source-mode binding)))
    (is (= source-path (:source-path binding)))
    (is (not= (:source-content-hash contract)
              (:source-content-hash binding)))
    (is (= (:source-content-hash binding)
           (get-in binding [:plan :source :sha256])))
    (is (= (get-in binding [:plan :plan-id])
           (get-in binding [:source-program :plan-id])))
    (is (= (:functions-semantic-hash binding)
           (get-in binding [:source-program :functions-semantic-hash])))
    (is (= (:function-count binding)
           (get-in binding [:source-program :function-count])))))

(deftest development-expression-owner-rejects-abi-drift
  (let [owner-var (ns-resolve
                   'gravity.bootstrap
                   'p15-s23-stage2-current-expression-development-binding!)
        plan-var (ns-resolve
                  'gravity.bootstrap
                  'p15-s23-stage2-compiler-artifact-plan)
        original-plan @plan-var
        data
        (failure-data
         #(with-redefs-fn
            {plan-var
             (fn [& args]
               (update-in (apply original-plan args)
                          [:functions 'p15-s23-compile-expr :arity]
                          inc))}
            (fn [] (@owner-var "<pr453-development-expression-abi-probe>"))))]
    (is (= "P15S23Q002" (:id data)))
    (is (= :current-expression-function-shape (:missing-fact data)))))

(deftest development-expression-owner-rejects-source-drift
  (let [owner-var (ns-resolve
                   'gravity.bootstrap
                   'p15-s23-stage2-current-expression-development-binding!)
        snapshot-var (ns-resolve
                      'gravity.bootstrap
                      'p15-s23-stage2-current-expression-development-source-snapshot!)
        original-snapshot @snapshot-var
        calls (atom 0)
        data
        (failure-data
         #(with-redefs-fn
            {snapshot-var
             (fn [path]
               (let [snapshot (original-snapshot path)]
                 (if (= 2 (swap! calls inc))
                   (assoc snapshot :path (str (:path snapshot) ".drift"))
                   snapshot)))}
            (fn [] (@owner-var "<pr453-development-expression-drift-probe>"))))]
    (is (= "P15S23Q002" (:id data)))
    (is (= :stable-current-expression-source-compilation
           (:missing-fact data)))))

(deftest v2-c6-public-construction-consumes-current-source-provenance
  (let [path (temp-source-path)
        text (source-text)
        context (bootstrap/p15-s23-stage2-gravity-checked-core-context
                 path text :jvm)]
    (try
      (let [artifact
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context {:interface :gravity/c6-c10 :version 2})
            report
            (bootstrap/p15-s23-stage2-gravity-checked-core-verification-report
             artifact context {:interface :gravity/c6-c10 :version 2})]
        (is (= :gravity/p15-s23-stage2-gravity-checked-core-artifact
               (:kind artifact)))
        (is (= {:interface :gravity/c6-c10 :version 2}
               (:source-interface report)))
        (is (= :selected-current-source-host-seed
               (:verification-scope report)))
        (is (= :development-unadmitted
               (:admission-status report)))
        (is (= :gravity/c6-reader-owner-metadata-projection
               (get-in report [:source-policy-receipt :projection-contract :artifact])))
        (is (= :development-current
               (get-in report [:source-policy-receipt :expression-binding :source-mode])))
        (is (= :passed (:status report))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest v2-c11-public-construction-consumes-real-c6-artifact
  (let [path (temp-source-path)
        text (source-text)
        context (bootstrap/p15-s23-stage2-gravity-checked-core-context
                 path text :jvm)
        c6-header {:interface :gravity/c6-c10 :version 2}
        c11-header {:interface :gravity/c11-mir :version 2}]
    (try
      (let [checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context c6-header)
            artifact
            (bootstrap/p15-s23-stage2-c11-mir-artifact
             checked-core context c11-header)
            report
            (bootstrap/p15-s23-stage2-c11-mir-verification-report
             artifact checked-core context c11-header)]
        (is (= :gravity/p15-s23-c11-authenticated-mir-artifact
               (:kind artifact)))
        (is (= 2 (:schema-version artifact)))
        (is (= :passed (:status (:verification-report artifact))))
        (is (= :passed (:status report)))
        (is (= {:interface :gravity/c11-mir :version 2}
               (:source-interface report)))
        (is (= {:interface :gravity/c11-mir :version 2}
               (:source-interface (:source-rule report))))
        (is (= :gravity-source-pure
               (get-in report [:checked-core-ingress :checked-core-ingress-mode])))
        (is (= :development-unadmitted
               (get-in report [:checked-core-ingress :source-policy :admission-status])))
        (is (false? (get-in report [:source-rule :self-hosted?]))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest v2-c13-c14-b1-packet-selection-consumes-current-c11-owner
  ;; The C6 and C11 producers and the public four-argument packet entrypoint
  ;; are real.  Stop at the downstream owner boundary here: this is bounded
  ;; source-selection evidence, not whole C13/C14/B1, native-C, release,
  ;; migration, or self-hosting authority.
  (let [path (temp-source-path)
        text (source-text)
        context (bootstrap/p15-s23-stage2-gravity-checked-core-context
                 path text :jvm)
        c6-header {:interface :gravity/c6-c10 :version 2}
        c11-header {:interface :gravity/c11-mir :version 2}
        build-var (ns-resolve
                   'gravity.bootstrap
                   'p15-s23-c13-c14-b1-build-verified-internal!)
        bind-var (ns-resolve
                  'gravity.bootstrap
                  'p15-s23-c13-c14-b1-bind-selected-source-contract)
        selected (atom nil)
        downstream-calls (atom 0)]
    (try
      (let [checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context c6-header)
            artifact
            (bootstrap/p15-s23-stage2-c11-mir-artifact
             checked-core context c11-header)
            tampered-artifact
            (assoc-in artifact [:source-rule :source-mode] :legacy-pinned)
            invoke
            (fn [candidate]
              (with-redefs-fn
                {build-var
                 (fn [& _]
                   (swap! downstream-calls inc)
                   {:status :accepted-for-bounded-llvm})
                 bind-var
                 (fn [packet selection]
                   (reset! selected selection)
                   (assoc packet :source-contract-selection selection))}
                #(bootstrap/p15-s23-stage2-c13-c14-b1-packet-from-c11!
                  candidate checked-core context c11-header)))
            packet (invoke artifact)
            selected-rule (:source-rule @selected)
            tampered-data (failure-data #(invoke tampered-artifact))]
        (is (= :accepted-for-bounded-llvm (:status packet)))
        (is (= :development-current (:source-mode selected-rule)))
        (is (= (get-in artifact [:source-rule :source-content-hash])
               (:source-content-hash selected-rule)))
        (is (= "C11-VERIFY" (:id tampered-data)))
        (is (some? (:missing-fact tampered-data)))
        (is (= 1 @downstream-calls)))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest v2-c11-rejects-forged-core-and-policy-before-c11-source
  (let [path (temp-source-path)
        text (source-text)
        context (bootstrap/p15-s23-stage2-gravity-checked-core-context
                 path text :jvm)
        header {:interface :gravity/c11-mir :version 2}]
    (try
      (let [checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context {:interface :gravity/c6-c10 :version 2})
            forged-core (assoc checked-core :artifact-id "sha256:forged-core")
            forged-core-data
            (failure-data
             #(bootstrap/p15-s23-stage2-c11-mir-artifact
               forged-core context header))
            changed-context (assoc context :source-text (str text " "))
            changed-policy-data
            (failure-data
             #(bootstrap/p15-s23-stage2-c11-mir-artifact
               checked-core changed-context header))]
        (is (= "C6-VERIFY" (:id forged-core-data)))
        (is (= :exact-gravity-artifact-template-replay
               (:missing-fact forged-core-data)))
        (is (= "C6-VERIFY" (:id changed-policy-data)))
        (is (= :exact-public-source-verification-context
               (:missing-fact changed-policy-data))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest public-source-headers-dispatch-v3-and-reject-unsupported-routes-before-construction
  (let [calls (atom 0)
        fresh-var (ns-resolve 'gravity.bootstrap
                              'p15-s23-c6c10-fresh-construction)
        fresh @fresh-var
        invoke (fn [header]
                 (try
                   (with-redefs-fn
                     {fresh-var (fn [& args]
                                  (swap! calls inc)
                                  (if (= 3 (second args))
                                    {:artifact :synthetic-v3-artifact}
                                    (apply fresh args)))}
                     #(bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
                       {:source-path "/tmp/pr453.invalid.gravity"
                        :source-text (source-text)
                        :requested-target :jvm}
                       header))
                   (catch clojure.lang.ExceptionInfo error
                     (ex-data error))))]
    (is (= :synthetic-v3-artifact
           (invoke {:interface :gravity/c6-c10 :version 3})))
    (is (= 1 @calls))
    (doseq [[header expected]
            [[{:interface :gravity/c6-c10 :version 1} :explicit-interface-version]
             [{:interface :gravity/c11-mir :version 2} :interface-id]]]
      (is (= expected (:reason (invoke header))))
      (is (= 1 @calls)))
    (is (= 2 (c6-source-interface/interface-version!
              {:interface :gravity/c6-c10 :version 2})))
    (is (= 3 (c6-source-interface/interface-version!
              {:interface :gravity/c6-c10 :version 3})))
    (is (= 2 (c11-source-interface/public-interface-version!
              {:interface :gravity/c11-mir :version 2})))))

(deftest public-c11-source-headers-dispatch-v3-and-reject-foreign-headers
  (is (= 3 (c11-source-interface/public-interface-version!
            {:interface :gravity/c11-mir :version 3})))
  (doseq [[header expected]
          [[{:interface :gravity/c11-mir :version 3}
            :explicit-interface-version]
           [{:interface :gravity/c6-c10 :version 2}
            :interface-id]]]
    (let [data
          (try
            (bootstrap/p15-s23-stage2-c11-mir-artifact
             {} {} header)
            nil
            (catch clojure.lang.ExceptionInfo error
              (ex-data error)))]
      (is (= expected (:reason data)))
      (is (= "C11-VERIFY" (:id data))))))

(deftest v2-c11-current-source-owner-binds-live-source-bytes
  (let [contract c11-source-contract/v2-current-source-contract-abi
        path (bootstrap/p15-s23-c11-mir-resolve-source-path)
        bytes (java.nio.file.Files/readAllBytes
               (.toPath (java.io.File. path)))
        binding (bootstrap/p15-s23-c11-mir-source-binding!
                 "pr453-c11-current-source.gravity" :jvm 2 :development-current)]
    (is (= :gravity/c11-mir (:interface contract)))
    (is (= 2 (:version contract)))
    (is (= :development-current (:source-mode binding)))
    (is (= (alength bytes) (:source-byte-count binding)))
    (is (= (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
           (:source-content-hash binding)))
    (is (= (:source-relative-path contract)
           (get-in binding [:source-program :source-relative-path])))))

(deftest wrong-v2-reader-policy-is-rejected-before-c2-production
  (let [calls (atom 0)
        ingress-var (ns-resolve 'gravity.bootstrap
                                'p15-s23-c6c10-private-ingress-products)
        ingress @ingress-var
        c2-var (ns-resolve 'gravity.bootstrap
                           'p15-s23-stage2-c2-c3-front-end-products)
        c2 @c2-var
        result
        (try
          (with-redefs-fn
            {c2-var (fn [& args]
                      (swap! calls inc)
                      (apply c2 args))}
            #(ingress "/tmp/pr453-policy.gravity" (source-text)
                      "/tmp/not-the-current-reader.gravity" 2 nil))
          nil
          (catch clojure.lang.ExceptionInfo error
            (ex-data error)))]
    (is (= "C6-VERIFY" (:id result)))
    (is (= :selected-current-reader-invocation-policy
           (:missing-fact result)))
    (is (zero? @calls))))
