(ns gravity.reader.pure-binding-test
  "Compiler-free modeled result checks. The dispatch leaf is replaced in every
  execution test: no real handler, Reader producer or provider is invoked."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.reader.dispatch :as dispatch]
            [gravity.reader.normal-dispatch-test :as normal]))

(defn- hash-id [digit] (str "sha256:" (apply str (repeat 64 digit))))

(defn- unit []
  (let [policy-id (dispatch/pure-policy-id)
        options {:extension-policy policy-id}]
    {:source-id (hash-id "a") :bytes-hash (hash-id "b")
     :extension-policy policy-id :reader-options options
     :identity-inputs {:extension-policy policy-id :reader-options options}}))

(def ^:private implementation
  {:source-content-hash (hash-id "c") :plan-semantic-hash (hash-id "d")
   :functions-semantic-hash (hash-id "e")})

(def ^:private form
  {:form-id "form/0" :children ["form/1"]
   :span {:byte-start 0 :byte-end 15
          :start {:line 1 :column 1 :column-unit :unicode-scalar :char 0 :byte 0}
          :end {:line 1 :column 16 :column-unit :unicode-scalar :char 15 :byte 15}}})

(defn- failure [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- modeled [f]
  (let [calls (atom [])]
    (with-redefs [dispatch/dispatch
                  (fn [policy tag seed provider]
                    (swap! calls conj {:tag tag :provider provider})
                    {:value seed
                     :invocation {:policy-id (:id policy) :tag tag
                                  :handler (get-in policy [:registry tag :handler])}})]
      (f calls))))

(deftest original-and-fresh-pure-results-compare
  (modeled
   (fn [calls]
     (let [result (:pure-result
                   (dispatch/dispatch-pure-tag (unit) 'text form "payload" implementation))]
       (is (= 2 (count @calls)))
       (is (= "payload" (get-in result [:returned-seed :datum])))
       (is (= implementation (get-in result [:input :implementation])))
       (is (= 'gravity.reader/read-text (get-in result [:input :handler])))
       (is (= result (dispatch/verify-pure-result!
                      (unit) 'text form "payload" implementation result)))
       (is (= 3 (count @calls)))
       (is (every? #(nil? (:provider %)) @calls))))))

(deftest substitutions-refuse-before-new-dispatch
  (modeled
   (fn [calls]
     (let [result (:pure-result
                   (dispatch/dispatch-pure-tag (unit) 'text form "payload" implementation))]
       (doseq [[path value]
               [[[:input :policy-id] (hash-id "0")]
                [[:input :source-content-hash] (hash-id "0")]
                [[:input :implementation :source-content-hash] (hash-id "0")]
                [[:input :handler] 'gravity.reader/read-input]
                [[:input :payload-form-id] "another-payload"]
                [[:input :seed :source-id] (hash-id "0")]
                [[:input :seed :form-id] "another-form"]
                [[:input :seed :datum] "substitute"]
                [[:input :seed :span :byte-end] 99]]]
         (testing (str path)
           (reset! calls [])
           (is (= :pure-result-binding
                  (:reason (failure #(dispatch/verify-pure-result!
                                      (unit) 'text form "payload" implementation
                                      (assoc-in result path value))))))
           (is (empty? @calls))))))))

(deftest returned-result-must-match-fresh-computation
  (modeled
   (fn [calls]
     (let [result (:pure-result
                   (dispatch/dispatch-pure-tag (unit) 'text form "payload" implementation))]
       (doseq [changed [(assoc-in result [:returned-seed :datum] "tampered")
                        (assoc result :result-hash (hash-id "0"))
                        (assoc-in result [:invocation :handler] 'gravity.reader/read-input)]]
         (reset! calls [])
         (is (= :pure-result-reconstruction
                (:reason (failure #(dispatch/verify-pure-result!
                                    (unit) 'text form "payload" implementation changed)))))
         (is (= 1 (count @calls))))))))

(deftest untrusted-carriers-and-policy-joins-refuse
  (modeled
   (fn [calls]
     (let [result (:pure-result
                   (dispatch/dispatch-pure-tag (unit) 'text form "payload" implementation))]
       (reset! calls [])
       (is (= :native-result
              (:reason (failure #(dispatch/verify-pure-result!
                                  (unit) 'text form "payload" implementation
                                  (assoc result :invocation (Object.)))))))
       (doseq [path [[:extension-policy] [:reader-options :extension-policy]
                     [:identity-inputs :extension-policy]
                     [:identity-inputs :reader-options :extension-policy]]]
         (is (= :policy-hash
                (:reason (failure #(dispatch/validate-pure-tags!
                                    (assoc-in (unit) path (hash-id "0")) ['text]))))))
       (is (empty? @calls))))))

(deftest complete-population-refuses-before-any-modeled-handler
  (modeled
   (fn [calls]
     ;; Exact production host walk is installed by normal-dispatch-test.
     ;; Empty semantic index is deliberate: population refusal must precede
     ;; payload traversal as well as dispatch, including a preceding pure tag.
     (doseq [[tag reason] [['resource :effectful-normal-unadmitted]
                           ['unknown :unknown-tag]]]
       (let [forms [{:form-id "pure" :kind :tagged-literal
                     :tag-codepoints (mapv int "text")}
                    {:form-id "refusal" :kind :tagged-literal
                     :tag-codepoints (mapv int (str tag))}]]
         (is (= reason
                (:reason (failure #(normal/sh03-reader-host-values!
                                    "modeled.gravity" (byte-array 0) (hash-id "b")
                                    {} forms {} (unit) implementation)))))
         (is (empty? @calls)))))))

(deftest paired-normal-source-definitions
  (doseq [case-name ["normal-pure" "normal-unknown" "normal-effect"]]
    (let [root "bootstrap/clojure/fixtures/reader-extension-binding/"
          source (slurp (str root case-name ".qst"))]
      (is (= source (slurp (str root case-name ".gravity"))))
      (is (.startsWith source "(ns reader.extension-")))))

(deftest private-implementation-effects-control-admission
  (modeled
   (fn [calls]
     (let [table (ns-resolve 'gravity.reader.dispatch 'handlers)]
       (with-redefs-fn
         {table (assoc-in @table ['gravity.reader/read-text :effects] #{:filesystem/read})}
         #(is (= :effectful-normal-unadmitted
                 (:reason (failure (fn [] (dispatch/validate-pure-tags!
                                           (unit) ['uuid 'text])))))))
       (is (empty? @calls))))))
