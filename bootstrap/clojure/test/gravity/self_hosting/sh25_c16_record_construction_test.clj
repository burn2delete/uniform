(ns gravity.self-hosting.sh25-c16-record-construction-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; Development execution only: no caller adoption or validation authority.
(def ^:private source-path
  (.getCanonicalPath
   (io/file "language/compiler/src/gravity/compiler/c16_incremental_compilation_design.gravity")))

(def ^:private source-text (delay (slurp source-path)))

(def ^:private plan
  (delay
    (let [emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path @source-text))))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-c16-record-construction-development
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- source-contract [contract-name]
  ;; Read constant contract data for the oracle; constructors execute above.
  (with-open [reader (java.io.PushbackReader.
                      (java.io.StringReader. @source-text))]
    (loop []
      (let [form (edn/read {:eof ::eof} reader)]
        (cond
          (= ::eof form)
          (throw (ex-info "C16 contract not found" {:contract contract-name}))
          (and (seq? form) (= 'def (first form))
               (= contract-name (second form)))
          (nth form 2)
          :else (recur))))))

(deftest cache-key-executes-supplied-fields-and-contract
  (let [arguments
        [:type-check "sha256:source" "sha256:reader" "sha256:syntax"
         "sha256:macro" "sha256:namespace" "sha256:profile"
         "sha256:target" "sha256:compiler" "sha256:pass"
         {:graph "sha256:dependencies" :facts #{:source-spans :proofs}}
         {:replay "sha256:replay" :provenance {:source "build.gravity"}}
         #{:build/read-file} [:core :types]]
        expected
        (merge
         {:artifact :gravity/cache-key
          :contract (source-contract 'c16-cache-key-contract)}
         (zipmap [:stage :source :reader :syntax :macro-expansion :namespace
                  :profile :target :compiler :pass-contract :dependencies
                  :build-effects :capabilities :language-facets]
                 arguments))]
    (is (= expected (invoke 'build-c16-cache-key arguments)))))
