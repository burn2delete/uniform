(ns gravity.reader.dispatch-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.reader.dispatch :as reader]))

(def ^:private input-path
  "bootstrap/clojure/fixtures/accepted/reader-extension-input.txt")
(def ^:private seed
  {:source-id "sha256:source-fixture" :form-id "form-1"
   :span {:byte-start 0 :byte-end 8} :datum "payload"})
(def ^:private text-entry
  {:handler 'gravity.reader/read-text :build-effects #{}
   :capabilities #{} :profiles #{:meta}})
(def ^:private input-entry
  {:handler 'gravity.reader/read-input :build-effects #{:filesystem/read}
   :capabilities #{:fs/read} :profiles #{:meta}})
(def ^:private policy
  {:id "reader-dispatch-fixture-v1" :profile :meta
   :registry {'text text-entry 'resource input-entry}
   :enabled-tags #{'text 'resource} :build-effects #{:filesystem/read}
   :capabilities #{:fs/read}})

(defn- failure [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(defn- observe-handlers [f]
  ;; Wrap real implementations, retaining both execution and their return values.
  ;; This observes dispatch; it never substitutes a fabricated accepted result.
  (let [table-var (ns-resolve 'gravity.reader.dispatch 'handlers)
        table @table-var
        calls (atom [])
        observed (into {} (for [[id implementation] table]
                            [id (update implementation :execute
                                        (fn [execute]
                                          (fn [seed provider]
                                            (swap! calls conj id)
                                            (execute seed provider))))]))]
    (with-redefs-fn {table-var observed} #(f calls))))

(defn- provider [calls]
  {:id "declared-fixture-provider"
   :grant {:id "read-fixture" :principal 'gravity.reader/read-input
           :provider "declared-fixture-provider" :phase :build
           :lifetime :invocation :capability :fs/read :input input-path}
   :invoke (fn [path]
             (swap! calls conj path)
             (slurp path :encoding "UTF-8"))})

(deftest registered-pure-handler-executes-and-records-selected-policy
  (observe-handlers
   (fn [calls]
     (let [result (binding [*data-readers* {'text (fn [_] (throw (Exception. "ambient")))}]
                    (reader/dispatch policy 'text seed nil))
           record (:invocation result)]
       (is (= seed (:value result)))
       (is (= ['gravity.reader/read-text] @calls))
       (is (= "reader-dispatch-fixture-v1" (:policy-id record)))
       (is (= 'gravity.reader/read-text (:handler record)))
       (is (= text-entry (get-in record [:policy-selection :entry])))
       (is (= (select-keys seed [:source-id :form-id :span])
              (select-keys record [:source-id :form-id :span])))
       (is (= :invoked (:status record)))
       (is (= #{} (:build-effects record)))
       (is (nil? (:provider-id record)))
       (is (= "" (get-in (reader/dispatch policy 'text (assoc seed :datum "") nil)
                          [:value :datum])))))))

(deftest explicitly-granted-handler-reads-real-declared-input
  (observe-handlers
   (fn [calls]
     (let [effects (atom [])
           original (assoc seed :datum input-path)
           result (reader/dispatch policy 'resource original (provider effects))]
       (is (= "registered handler payload\n" (get-in result [:value :datum])))
       (is (= (dissoc original :datum) (dissoc (:value result) :datum)))
       (is (= ['gravity.reader/read-input] @calls))
       (is (= [input-path] @effects))
       (is (= #{:filesystem/read} (get-in result [:invocation :build-effects])))
       (is (= #{:fs/read} (get-in result [:invocation :capabilities])))
       (is (= "read-fixture" (get-in result [:invocation :grant-id])))
       (is (= "declared-fixture-provider" (get-in result [:invocation :provider-id])))))))

(deftest unknown-and-disallowed-tags-reject-before-any-handler
  (observe-handlers
   (fn [calls]
     (doseq [[selected tag reason]
             [[policy 'unknown :unknown-tag]
              [(assoc policy :enabled-tags #{}) 'resource :disallowed-tag]
              [(assoc policy :profile :core) 'resource :disallowed-profile]
              [(assoc-in policy [:registry 'text :handler] 'foreign/read)
               'text :unknown-handler]]]
       (let [effects (atom [])
             result (failure #(reader/dispatch selected tag seed (provider effects)))]
         (is (= {:id "C2-EXTENSION" :reason reason} result))
         (is (empty? @calls))
         (is (empty? @effects)))))))

(deftest effects-and-capabilities-cannot-be-erased-by-registry-claims
  (observe-handlers
   (fn [calls]
     (doseq [selected [(assoc policy :build-effects #{})
                       (assoc-in policy [:registry 'resource :build-effects] #{})
                       (assoc policy :capabilities #{})
                       (assoc-in policy [:registry 'resource :capabilities] #{})
                       (assoc-in policy [:registry 'resource :pure?] true)]]
       (let [effects (atom [])
             result (failure #(reader/dispatch selected 'resource
                                               (assoc seed :datum input-path)
                                               (provider effects)))]
         (is (= "C2-EXTENSION" (:id result)))
         (is (empty? @calls))
         (is (empty? @effects)))))))

(deftest grants-are-bound-to-handler-provider-phase-lifetime-capability-and-input
  (observe-handlers
   (fn [calls]
     (doseq [[key replacement] [[:principal 'foreign/read] [:provider "other"]
                               [:phase :runtime] [:lifetime :permanent]
                               [:capability :fs/write] [:input "other-file"]]]
       (let [effects (atom [])
             binding (assoc-in (provider effects) [:grant key] replacement)
             result (failure #(reader/dispatch policy 'resource
                                               (assoc seed :datum input-path) binding))]
         (is (= :grant (:reason result)))
         (is (empty? @calls))
         (is (empty? @effects)))))))

(deftest untrusted-containers-and-callables-do-not-become-handlers
  (let [calls (atom 0)
        foreign (proxy [java.util.HashMap] []
                  (entrySet [] (swap! calls inc) (throw (Exception. "callback"))))]
    (doseq [selected [foreign (assoc policy :registry foreign)
                      (with-meta policy {:untrusted true})
                      (assoc-in policy [:registry 'text :handler]
                                (fn [_] (swap! calls inc)))]]
      (is (= "C2-EXTENSION"
             (:id (failure #(reader/dispatch selected 'text seed nil))))))
    (is (= "C2-EXTENSION"
           (:id (failure #(reader/dispatch policy 'text foreign nil)))))
    (is (zero? @calls))
    (is (= :unicode
           (:reason (failure #(reader/dispatch policy 'text
                                               (assoc seed :datum "\ud800") nil)))))))
