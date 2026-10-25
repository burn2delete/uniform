(ns gravity.self-hosting.sh07-host-invocation-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- implementation [name] (ns-resolve 'gravity.bootstrap name))

(def ^:private current
  {:reader-source-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
   :authenticated-envelope-options {:source :development-current}})

(deftest macro-and-core-receipts-cannot-cross-host-selection
  ;; Exercise the real receipt kernel and public verifier wrappers. The owner
  ;; bodies are spies: this proves receipt isolation, not artifact acceptance.
  (doseq [[entry body] [[b/sh05-macro-artifact-verification 'sh05-macro-artifact-verification*]
                       [b/sh05-macro-capability-based-proof-for-construction 'sh05-macro-artifact-verification*]
                       [b/sh07-core-artifact-verification 'sh07-core-artifact-verification*]]]
    (let [context (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                         :phase :construction :epoch 0 :receipts [] :check-catalogs {}
                         :executions {} :reuses {} :failed-report-executions 0})
          artifact {:artifact-id "sha256:receipt-unit" :provenance {:source-path "unit.gravity"}
                    :invocation-context current}
          calls (atom [])
          verify (fn [& arguments]
                   (swap! calls conj (last arguments))
                   {:artifact :test/report :status :passed :checks {:owner-executed? true}
                    :failed-checks []})]
      (with-redefs-fn
        {(implementation body) verify
         (implementation 'sh03-reader-authenticated-envelope-options)
         (fn [_ invocation]
           (assert (= current invocation))
           (:authenticated-envelope-options invocation))}
        #(with-bindings {(implementation '*sh07-proof-transaction-context*) context}
           (entry artifact current)
           (entry artifact)
           (entry artifact current)
           (entry artifact)))
      (is (= [current nil] @calls))
      (is (= 2 (count (:receipts @context))))
      (is (= #{nil current} (set (map #(get-in % [:verifier-epoch :invocation]) (:receipts @context)))))
      (is (= 2 (reduce + (vals (:reuses @context))))))))

(deftest public-development-selection-is-explicit-through-both-caller-edges
  ;; Target reading is intentionally replaced in this routing unit.
  (let [calls (atom [])]
    (with-redefs [b/read-gravity-source-text (constantly "retained source")
                  b/sh03-reader-resolve-source-path (constantly (:reader-source-path current))
                  b/compiler-c5-resolution-source-artifact
                  (fn [& arguments] (swap! calls conj [:resolution (vec arguments)]) ::parent)
                  b/sh07-core-from-resolution-artifact
                  (fn [& arguments] (swap! calls conj [:core (vec arguments)]) ::product)]
      (is (= ::product (b/sh07-public-core-file-artifact "unit.gravity" {:reader :development-current}))))
    (is (= [[:resolution ["unit.gravity" "retained source"
                          {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                          (:reader-source-path current) current]]
            [:core [::parent current]]] @calls))))
