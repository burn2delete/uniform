(ns gravity.self-hosting.sh10-ownership-transition-fixture-support
  "Shared SH-10 ownership-transition plans and normalized request adapters.

  The source and paired positive/negative plans stay delayed exactly as they
  were in the owning suite.  This namespace contains no test vars and is safe
  for both the SH-10 tests and the ownership pipeline consumer."
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh10_ownership_transition_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-10 test source is not on the classpath"
                {:id "SH10-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH10-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c9-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-10")

(defn fixture-relative-path
  [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def c9-plan
  (delay (compile-plan c9-source-relative-path)))

(def accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "ownership-transitions" ".gravity"))))

(def accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "ownership-transitions" ".qst"))))

(def rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-ownership-transitions" ".gravity"))))

(def rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-ownership-transitions" ".qst"))))

(defn invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh10-ownership-transition-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn invoke-c9
  [function arguments]
  (invoke c9-plan function arguments))

(defn request
  [plan function]
  (invoke plan function []))

(defn check
  [request]
  (invoke-c9 'sh10-check-ownership-request [request]))

(def accepted-functions
  '[sh10-initialize-read-request
    sh10-many-immutable-request
    sh10-exclusive-mutable-request
    sh10-bounded-escape-request])

(def rejected-cases
  {'sh10-uninitialized-read-request
   ["L10-UNINIT-READ" :operation-before-initialization :read]
   'sh10-use-after-move-request
   ["C9-USE-AFTER-MOVE" :operation-after-move :read]
   'sh10-use-after-consume-request
   ["C9-USE-AFTER-CONSUME" :operation-after-consume :read]
   'sh10-mutable-alias-request
   ["C9-MUT-ALIAS"
    :mutable-borrow-with-active-immutable-aliases
    :borrow-mutable]
   'sh10-owner-read-during-mutable-request
   ["C9-MUT-ALIAS"
    :owner-read-during-active-mutable-borrow
    :read]
   'sh10-move-while-borrowed-request
   ["C9-MOVE-WHILE-BORROWED"
    :move-during-active-borrow
    :move]
   'sh10-consume-while-borrowed-request
   ["C9-UNSAFE"
    :consume-during-active-borrow
    :consume]
   'sh10-borrow-escape-request
   ["C9-BORROW-ESCAPE" :borrow-outlives-owner :escape-borrow]
   'sh10-wrong-borrow-escape-request
   ["C9-BORROW-ESCAPE" :escape-of-inactive-borrow :escape-borrow]
   'sh10-missing-borrow-escape-request
   ["C9-UNSAFE" :malformed-ownership-event :escape-borrow]
   'sh10-missing-destination-escape-request
   ["C9-UNSAFE" :malformed-ownership-event :escape-borrow]
   'sh10-wrong-borrow-end-request
   ["C9-UNSAFE" :end-of-inactive-borrow :end-borrow]
   'sh10-malformed-event-request
   ["C9-UNSAFE" :malformed-ownership-event :future/ownership]
   'sh10-double-initialize-request
   ["C9-UNSAFE" :double-initialization :initialize]})
