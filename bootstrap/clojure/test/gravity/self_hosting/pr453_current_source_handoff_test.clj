(ns gravity.self-hosting.pr453-current-source-handoff-test
  (:require [clojure.test :refer [deftest is testing]]
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

(deftest v2-current-c6-source-owner-produces-a-real-binding
  (let [path (temp-source-path)]
    (try
      (let [binding (bootstrap/p15-s23-c6c10-source-binding! path 2)
            contract c6-source-contract/current-compiler-program]
        (is (= 2 (:source-contract-version binding)))
        (is (= :complete (:status binding)))
        (is (= (:source-byte-count contract) (:source-byte-count binding)))
        (is (= (:source-content-hash contract) (:source-content-hash binding)))
        (is (= (:required-functions contract) (:function-shapes binding))))
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

(deftest v2-current-expression-owner-produces-a-real-binding
  (let [source-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        rule (bootstrap/c-backend-stage2-plan-emitter-source-rule!
              "<pr453-expression-binding-probe>" :jvm)
        binding (bootstrap/p15-s23-stage2-compiler-artifact-binding!
                 (:emitter rule) "<pr453-expression-binding-probe>" :jvm 2)
        contract c6-source-contract/current-expression-program]
    (is (= 2 (:source-contract-version binding)))
    (is (= (:source-content-hash contract) (:source-content-hash binding)))
    (is (= (:semantic-hash contract) (:semantic-hash binding)))
    (is (= (:function-count contract) (:function-count binding)))
    (is (= source-path (:source-path binding)))))

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
        (is (= :passed (:status report))))
      (finally
        (java.nio.file.Files/deleteIfExists (.toPath (java.io.File. path)))))))

(deftest public-v2-headers-reject-unsupported-routes-before-construction
  (let [calls (atom 0)
        fresh-var (ns-resolve 'gravity.bootstrap
                              'p15-s23-c6c10-fresh-construction)
        fresh @fresh-var
        invoke (fn [header]
                 (try
                   (with-redefs-fn
                     {fresh-var (fn [& args]
                                  (swap! calls inc)
                                  (apply fresh args))}
                     #(bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
                       {:source-path "/tmp/pr453.invalid.gravity"
                        :source-text (source-text)
                        :requested-target :jvm}
                       header))
                   nil
                   (catch clojure.lang.ExceptionInfo error
                     (ex-data error))))]
    (doseq [[header expected]
            [[{:interface :gravity/c6-c10 :version 1} :explicit-interface-version]
             [{:interface :gravity/c6-c10 :version 3} :interface-version-class-or-value]
             [{:interface :gravity/c11-mir :version 2} :interface-id]]]
      (is (= expected (:reason (invoke header))))
      (is (zero? @calls)))
    (is (= 2 (c6-source-interface/interface-version!
              {:interface :gravity/c6-c10 :version 2})))
    (is (= 2 (c11-source-interface/public-interface-version!
              {:interface :gravity/c11-mir :version 2})))))

(deftest public-c11-v2-route-rejects-unsupported-header-before-ingress
  (doseq [[header expected]
          [[{:interface :gravity/c11-mir :version 3}
            :interface-version-class-or-value]
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

(deftest v2-c11-contract-binds-the-current-source-bytes
  (let [contract c11-source-contract/v2-source-contract
        path (bootstrap/p15-s23-c11-mir-resolve-source-path)
        bytes (java.nio.file.Files/readAllBytes
               (.toPath (java.io.File. path)))]
    (is (= :gravity/c11-mir (:interface contract)))
    (is (= 2 (:version contract)))
    (is (= (:source-byte-count contract) (alength bytes)))
    (is (= (:source-content-hash contract)
           (str "sha256:" (bootstrap/sha256-bytes-hex bytes))))))

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
