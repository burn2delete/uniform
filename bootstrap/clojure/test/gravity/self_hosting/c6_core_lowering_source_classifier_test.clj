(ns gravity.self-hosting.c6-core-lowering-source-classifier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c6-core-lowering :as c6]
            [gravity.c6-core-lowering.lowering :as lowering]
            [gravity.c6-core-lowering.source-adapter :as source-adapter]))

;; This is a current-source development execution check.  It does not renew
;; migration evidence, source pins, or a self-hosting claim.
(def ^:private repository-root
  (delay
    (let [resource
          (io/resource
           "gravity/self_hosting/c6_core_lowering_source_classifier_test.clj")]
      (when-not resource
        (throw
         (ex-info
          "C6 source classifier test is not on the classpath"
          {:id "C6-SOURCE-CLASSIFIER-TEST-SOURCE"})))
      (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
        (cond
          (nil? candidate)
          (throw
           (ex-info
            "Repository root could not be located"
            {:id "C6-SOURCE-CLASSIFIER-REPOSITORY-ROOT"}))

          (.isFile (.toFile (.resolve candidate "deps.edn")))
          candidate

          :else
          (recur (.getParent candidate)))))))

(def ^:private compiler-api
  {:c-backend-stage2-plan-emitter-source-rule!
   bootstrap/c-backend-stage2-plan-emitter-source-rule!
   :p15-s23-stage2-compiler-artifact-plan
   bootstrap/p15-s23-stage2-compiler-artifact-plan})

(def ^:private source-plan
  (delay
    (source-adapter/current-source-plan @repository-root compiler-api)))

(def ^:private source-operation
  (delay
    (source-adapter/c6-form->core-form-operation
     bootstrap/p15-s23-stage2-runtime-execute-function
     @source-plan)))

(def ^:private module
  {:module 'gravity.c6-source-classifier-test
   :source-path "c6-source-classifier-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def ^:private c5-artifact
  {:kind :gravity/stage0-c5-name-resolution-artifact
   :artifact-id "sha256:c6-source-classifier-test"
   :namespace-analysis {:namespace 'gravity.c6-source-classifier-test}
   :binding-table {:bindings [{:binding-id "c6-source-classifier-binding"}]}
   :alias-table {}
   :dependency-graph {:nodes [] :edges []}})

(defn- syntax [id form]
  {:syntax-id id
   :form form
   :span {:source "c6-source-classifier-test.gravity" :form-index id}
   :origin :source
   :generated-origin []
   :metadata {}})

(deftest current-source-classifier-preserves-hosted-leaf-parity
  (let [classify @source-operation]
    (doseq [core-form '[quote if do let fn loop recur def var set! try throw match]]
      (is (= core-form (classify (list core-form :argument)))
          (str "current source core-form classification for " core-form)))
    (doseq [[form expected]
            [['answer :symbol]
             [42 :literal]
             ['(if true 1 2) 'if]
             ['(unsafe value) :declared-primitive]
             ['(unknown value) :call]]]
      (is (= expected (classify form))
          (str "current source classification for " (pr-str form))))))

(deftest current-source-classifier-is-used-by-c6-lowering-caller
  (let [calls (atom [])
        classify (fn [form]
                   (swap! calls conj form)
                   (@source-operation form))
        lowered
        (with-redefs [lowering/c6-form->core-form
                      (fn [_]
                        (throw
                         (ex-info
                          "hosted Clojure classifier was called"
                          {:id "C6-SOURCE-CLASSIFIER-HOST-FALLBACK"})))]
          (c6/with-operations
            {:c6-form->core-form classify}
            #(c6/c6-lowering-artifact
              (:source-path module)
              module
              c5-artifact
              [(syntax "if" '(if true 1 2))])))
        root (first (:core-node-table lowered))]
    (testing "the existing C6 caller consumes the source-owned operation"
      (is (= 'if (:form root)))
      (is (= 3 (count (:children root))))
      (is (= 4 (count @calls))))
    (testing "the source operation retains the real lowering-gap rejection"
      (let [failure
            (try
              (with-redefs [lowering/c6-form->core-form
                            (fn [_]
                              (throw
                               (ex-info
                                "hosted Clojure classifier was called"
                                {:id "C6-SOURCE-CLASSIFIER-HOST-FALLBACK"})))]
                (c6/with-operations
                  {:c6-form->core-form @source-operation}
                  #(c6/c6-lowering-artifact
                    (:source-path module)
                    module
                    c5-artifact
                    [(syntax "defn" '(defn answer [] 1))])))
              nil
              (catch clojure.lang.ExceptionInfo error
                (ex-data error)))]
        (is (= "C6-LOWERING-GAP" (:id failure)))
        (is (= 'defn (:lowering-rule failure)))))))
