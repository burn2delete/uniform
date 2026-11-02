(ns gravity.self-hosting.sh10-ownership-transition-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
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

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c9-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-10")

(defn- fixture-relative-path
  [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c9-plan
  (delay (compile-plan c9-source-relative-path)))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "ownership-transitions" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "ownership-transitions" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-ownership-transitions" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-ownership-transitions" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh10-ownership-transition-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-c9
  [function arguments]
  (invoke c9-plan function arguments))

(defn- request
  [plan function]
  (invoke plan function []))

(defn- check
  [request]
  (invoke-c9 'sh10-check-ownership-request [request]))

(def ^:private accepted-functions
  '[sh10-initialize-read-request
    sh10-many-immutable-request
    sh10-exclusive-mutable-request
    sh10-bounded-escape-request])

(def ^:private rejected-cases
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

(deftest sh10-source-and-fixtures-compile-as-gravity
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c9-plan)))
  (is (= :meta (get-in @c9-plan [:module :profile])))
  (is (= :jvm (get-in @c9-plan [:module :target])))
  (doseq [function
          '[sh10-ownership-policy
            sh10-check-ownership-request
            sh10-verify-ownership-result]]
    (is (map? (get-in @c9-plan [:functions function])) function))
  (let [policy (invoke-c9 'sh10-ownership-policy [])]
    (is (= :gravity/sh10-ownership-policy (:artifact policy)))
    (is (= :owned-mutable (:ownership-kind policy)))
    (is (contains? (:events policy) :borrow-immutable))
    (is (contains? (:events policy) :move))
    (is (= #{:function-return :closure-capture :global-or-static-storage}
           (:escape-destinations policy)))
    (is (some #{:region-lifetimes} (:pending policy)))
    (is (some #{:authenticated-effectful-or-nonprimitive-sh09-adapter}
              (:pending policy))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "accepted" "ownership-transitions" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "accepted" "ownership-transitions" ".qst")))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-ownership-transitions" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-ownership-transitions" ".qst")))))
  (doseq [plan [accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan)))))

(deftest sh10-accepts-initiation-borrow-move-and-bounded-lifetime-flows
  (let [gravity-requests
        (mapv #(request accepted-gravity-plan %) accepted-functions)
        qst-requests
        (mapv #(request accepted-qst-plan %) accepted-functions)
        results (mapv check gravity-requests)
        [initialized many-immutable exclusive-mutable bounded-escape]
        results]
    (is (= gravity-requests qst-requests))
    (is (= results (mapv check qst-requests)))
    (is (= [:accepted :accepted :accepted :accepted]
           (mapv :status results)))
    (is (= :initialized
           (get-in initialized [:state :initialization])))
    (is (= :available
           (get-in initialized [:state :availability])))
    (is (= [:initialize :read]
           (mapv :operation (:ownership-facts initialized))))
    (is (= :moved (get-in many-immutable [:state :availability])))
    (is (= "owner-destination"
           (get-in many-immutable [:state :current-owner])))
    (is (= 0
           (get-in many-immutable
                   [:state :immutable-borrow-count])))
    (is (= false
           (get-in many-immutable
                   [:state :immutable-borrows :borrow-a])))
    (is (= false
           (get-in many-immutable
                   [:state :immutable-borrows :borrow-b])))
    (is (= 6 (count (:ownership-facts many-immutable))))
    (is (= nil
           (get-in exclusive-mutable [:state :mutable-borrow-id])))
    (is (= :available
           (get-in exclusive-mutable [:state :availability])))
    (is (= :escape-borrow
           (get-in bounded-escape [:ownership-facts 1 :operation])))
    (is (= :function-return
           (get-in bounded-escape
                   [:ownership-facts 1 :escape-destination])))
    (doseq [[request result] (map vector gravity-requests results)]
      (is (= request (get-in result [:identity-input :request])))
      (is (= (:value-id request)
             (get-in result [:preserves :value-id])))
      (is (= (:type-fact-id request)
             (get-in result [:preserves :type-fact-id])))
      (is (= (:effect-fact-id request)
             (get-in result [:preserves :effect-fact-id])))
      (is (= (:source-span request)
             (get-in result [:preserves :source-span])))
      (is (= :passed
             (:status
              (invoke-c9
               'sh10-verify-ownership-result [request result])))))
    (doseq [result results
            fact (:ownership-facts result)]
      (is (= :gravity/c9-ownership-fact (:artifact fact)))
      (is (= "value-buffer" (:value-id fact)))
      (is (= "owner-local" (:owner-id fact)))
      (is (= :meta (:profile fact)))
      (is (= :jvm (:target fact)))
      (is (map? (:source-span fact)))
      (is (vector? (:origin-chain fact))))
    (doseq [result results]
      (is (= (count (:ownership-facts result))
             (count
              (set
               (mapv :fact-id-request
                     (:ownership-facts result)))))))))

(deftest sh10-ending-a-borrow-requires-an-active-non-nil-identity
  ;; Original normalized ownership kernel; no C8/source admission is implied.
  (let [base (request accepted-gravity-plan 'sh10-exclusive-mutable-request)
        [borrow end read] (:events base)
        immutable (assoc borrow :op :borrow-immutable :borrow-id :immutable)
        invalid-end (assoc end :event-id :invalid-end
                           :source-span {:source "end-borrow-identity.gravity"
                                         :start-byte 3 :end-byte 4}
                           :origin-chain [{:from :end-borrow-identity}])
        verify (fn [r result]
                 (invoke-c9 'sh10-verify-ownership-result [r result]))]
    (doseq [prefix [[] [borrow end] [immutable] [borrow]]
            missing? [false true]]
      (let [before (check (assoc base :events prefix))
            event (if missing? (dissoc invalid-end :borrow-id)
                      (assoc invalid-end :borrow-id nil))
            r (assoc base :events (conj prefix event))
            result (check r)
            diagnostic (first (:diagnostics result))]
        (is (= :accepted (:status before)))
        (is (= :rejected (:status result)))
        (is (= ["C9-UNSAFE" :end-of-inactive-borrow :end-borrow :invalid-end]
               ((juxt :rule :reason :operation :event-id) diagnostic)))
        (is (= (:state before) (:state result)))
        (is (= (:ownership-facts before) (:ownership-facts result)))
        (is (not-any? #(= :invalid-end (:event-id %)) (:ownership-facts result)))
        (is (= [(:source-span event) (:origin-chain event)]
               [(:source-span diagnostic) (:generated-origin-chain diagnostic)]))
        (is (= r (get-in result [:identity-input :request])))
        (is (= :passed (:status (verify r result))))
        (is (= :rejected (:status (verify r (assoc result :status :accepted)))))))
    ;; False is an existing legal, non-nil identity, not an inactive sentinel.
    (doseq [operation [:borrow-immutable :borrow-mutable]]
      (let [r (assoc base :events [(assoc borrow :op operation :borrow-id false)
                                  (assoc end :borrow-id false)])
            result (check r)]
        (is (= :accepted (:status result)))
        (is (= [false false] (mapv :borrow-id (:ownership-facts result))))
        (is (nil? (get-in result [:state :mutable-borrow-id])))
        (is (= 0 (get-in result [:state :immutable-borrow-count])))
        (is (= :passed (:status (verify r result))))
        (is (= :rejected (:status (verify r (assoc-in result [:ownership-facts 1 :borrow-id] nil)))))))
    (doseq [r [(assoc base :events [(assoc invalid-end :borrow-id :unknown)])
               (assoc base :events [borrow end (assoc end :event-id :duplicate-end)])]]
      (let [result (check r)]
        (is (= :rejected (:status result)))
        (is (= :end-of-inactive-borrow (get-in result [:diagnostics 0 :reason])))
        (is (= :passed (:status (verify r result))))))
    (doseq [function ['sh10-many-immutable-request 'sh10-exclusive-mutable-request]]
      (let [r (request accepted-gravity-plan function) result (check r)]
        (is (= :accepted (:status result)))
        (is (= :passed (:status (verify r result))))))
    (doseq [[initialization prefix rule reason]
            [[:uninitialized [] "L10-UNINIT-READ" :operation-before-initialization]
             [:initialized [(assoc read :event-id :move :op :move :destination-owner "next-owner")]
              "C9-USE-AFTER-MOVE" :operation-after-move]
             [:initialized [(assoc read :event-id :consume :op :consume)]
              "C9-USE-AFTER-CONSUME" :operation-after-consume]]]
      (let [r (assoc base :initialization initialization
                          :events (conj prefix (dissoc invalid-end :borrow-id)))
            result (check r)]
        (is (= :rejected (:status result)))
        (is (= [rule reason] ((juxt :rule :reason) (first (:diagnostics result)))))
        (is (= :passed (:status (verify r result))))))))

(deftest sh10-executes-the-declared-event-bound-without-host-recursion
  ;; Exercise the original normalized kernel, not public C8/source admission.
  (let [base (request accepted-gravity-plan 'sh10-exclusive-mutable-request)
        read (last (:events base))
        reads (mapv #(assoc read :event-id %) (range 1024))
        verify (fn [r result] (invoke-c9 'sh10-verify-ownership-result [r result]))
        observe (fn [r]
                  (try (check r)
                       (catch StackOverflowError error
                         {:host-error (.getName (class error))})))
        maximum (observe (assoc base :events reads))]
    (is (= :accepted (:status maximum)) (pr-str (select-keys maximum [:status :host-error])))
    ;; Keep latent checks explicit when the original builder cannot finish.
    (when (= :accepted (:status maximum))
      (doseq [kind [:owned-mutable :persistent-immutable]]
        (let [r (assoc base :ownership-kind kind :events reads)
              result (if (= kind :owned-mutable) maximum (check r))
              facts (:ownership-facts result)]
          (is (= :accepted (:status result)))
          (is (= 1024 (count facts)))
          (is (= (mapv :event-id reads) (mapv :event-id facts)))
          (is (every? #(= :read (:operation %)) facts))
          (is (= (:state-before (first facts)) (:state result) (:state-after (last facts))))
          (is (= r (get-in result [:identity-input :request])))
          (is (= :passed (:status (verify r result))))
          (is (= :rejected (:status (verify r (update result :ownership-facts pop)))))))
      (doseq [r [(assoc base :events (conj reads (assoc read :event-id 1024)))
                 (assoc base :events (assoc-in reads [1023 :event-id] 0))
                 (assoc base :ownership-kind :persistent-immutable
                             :events (assoc-in reads [1023 :op] :write))]]
        (let [result (check r)]
          (is (= :rejected (:status result)))
          (is (= ["C9-UNSAFE" :malformed-normalized-ownership-request]
                 ((juxt :rule :reason) (first (:diagnostics result)))))
          (is (empty? (:ownership-facts result)))
          (is (= :passed (:status (verify r result))))))
      (let [failure (assoc read :event-id 1023 :op :end-borrow :borrow-id nil
                               :source-span {:source "last-event.gravity" :start-byte 8 :end-byte 9}
                               :origin-chain [{:from :last-event}])
            r (assoc base :events (assoc reads 1023 failure))
            result (check r) diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= 1023 (count (:ownership-facts result))))
        (is (= ["C9-UNSAFE" :end-of-inactive-borrow 1023]
               ((juxt :rule :reason :event-id) diagnostic)))
        (is (= [(:source-span failure) (:origin-chain failure)]
               [(:source-span diagnostic) (:generated-origin-chain diagnostic)]))
        (is (= (:state maximum) (:state result)))
        (is (= :passed (:status (verify r result))))
        (is (= :rejected (:status (verify r (assoc-in result [:diagnostics 0 :event-id] 0)))))))))

(deftest sh10-rejects-invalid-state-transitions-structurally
  (doseq [[function [rule reason operation]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-result (check gravity-request)
            qst-result (check qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= operation (:operation diagnostic)))
        (is (= :ownership-checking (:stage diagnostic)))
        (is (= (:value-id gravity-request) (:value-id diagnostic)))
        (is (= (:owner-id gravity-request) (:owner-id diagnostic)))
        (is (= (:profile gravity-request) (:profile diagnostic)))
        (is (= (:target gravity-request) (:target diagnostic)))
        (is (map? (:source-span diagnostic)))
        (is (vector? (:generated-origin-chain diagnostic)))
        (is (keyword? (:remediation diagnostic)))))))

(deftest sh10-fails-closed-on-request-event-and-result-substitution
  (let [request
        (request accepted-gravity-plan 'sh10-exclusive-mutable-request)
        result (check request)
        wrong-kind
        (check (assoc request :ownership-kind :persistent-immutable))
        missing-events
        (check (dissoc request :events))
        missing-type-fact
        (check (dissoc request :type-fact-id))
        malformed-effect-fact
        (check (assoc request :effect-fact-id "effect-fact-pure"))
        malformed-source-span
        (check (assoc request :source-span :not-a-map))
        malformed-origin-chain
        (check (assoc request :origin-chain :not-a-vector))
        duplicate-event-ids
        (check
         (assoc
          request
          :events
          [{:event-id :duplicate
            :op :read
            :source-span {}
            :origin-chain []}
           {:event-id :duplicate
            :op :read
            :source-span {}
            :origin-chain []}]))
        malformed-event
        (check (assoc request
                      :events
                      [{:event-id :bad
                        :op :read
                        :source-span {}
                        :origin-chain :not-a-vector}]))
        malformed-lifetime
        (check
         (assoc
          request
          :events
          [{:event-id :borrow
            :op :borrow-immutable
            :borrow-id :borrow
            :source-span {}
            :origin-chain []}
           {:event-id :escape
            :op :escape-borrow
            :borrow-id :borrow
            :destination-lifetime-end :not-a-number
            :source-span {}
            :origin-chain []}]))
        malformed-destination
        (check
         (assoc
          request
          :events
          [{:event-id :borrow
            :op :borrow-immutable
            :borrow-id :borrow
            :source-span {}
            :origin-chain []}
           {:event-id :escape
            :op :escape-borrow
            :borrow-id :borrow
            :destination-lifetime-end 8
            :escape-destination :unsupported-destination
            :source-span {}
            :origin-chain []}]))
        oversized
        (check
         (assoc
          request
          :events
          (vec
           (repeat
            1025
            {:event-id :read
             :op :read
             :source-span {}
             :origin-chain []}))))
        substituted (assoc result :status :substituted)
        verification
        (invoke-c9
         'sh10-verify-ownership-result [request substituted])
        generated-origin-request
        (assoc request
               :ownership-kind :persistent-immutable
               :origin-chain nil
               :generated-origin-chain
               [{:kind :generated :generator :sh10-test}])
        generated-origin-result (check generated-origin-request)]
    (doseq [candidate
            [wrong-kind missing-events missing-type-fact
             malformed-effect-fact duplicate-event-ids]]
      (is (= :rejected (:status candidate)))
      (is (= "C9-UNSAFE"
             (get-in candidate [:diagnostics 0 :rule])))
      (is (= :malformed-normalized-ownership-request
             (get-in candidate [:diagnostics 0 :reason])))
      (is (= (:source-span request)
             (get-in candidate [:diagnostics 0 :source-span])))
      (is (= (:origin-chain request)
             (get-in candidate
                     [:diagnostics 0 :generated-origin-chain]))))
    (doseq [candidate [malformed-source-span malformed-origin-chain]]
      (is (= :rejected (:status candidate)))
      (is (= "C9-UNSAFE"
             (get-in candidate [:diagnostics 0 :rule])))
      (is (= :malformed-normalized-ownership-request
             (get-in candidate [:diagnostics 0 :reason]))))
    (is (= {}
           (get-in malformed-source-span [:diagnostics 0 :source-span])))
    (is (= []
           (get-in malformed-origin-chain
                   [:diagnostics 0 :generated-origin-chain])))
    (is (= :rejected (:status malformed-event)))
    (is (= "C9-UNSAFE"
           (get-in malformed-event [:diagnostics 0 :rule])))
    (is (= :malformed-ownership-event
           (get-in malformed-event [:diagnostics 0 :reason])))
    (is (= (:origin-chain request)
           (get-in malformed-event
                   [:diagnostics 0 :generated-origin-chain])))
    (is (= :rejected (:status malformed-lifetime)))
    (is (= "C9-UNSAFE"
           (get-in malformed-lifetime [:diagnostics 0 :rule])))
    (is (= :malformed-ownership-event
           (get-in malformed-lifetime [:diagnostics 0 :reason])))
    (is (= :rejected (:status malformed-destination)))
    (is (= "C9-UNSAFE"
           (get-in malformed-destination [:diagnostics 0 :rule])))
    (is (= :malformed-ownership-event
           (get-in malformed-destination [:diagnostics 0 :reason])))
    (is (= :rejected (:status oversized)))
    (is (= "C9-UNSAFE"
           (get-in oversized [:diagnostics 0 :rule])))
    (is (= :malformed-normalized-ownership-request
           (get-in oversized [:diagnostics 0 :reason])))
    (is (= :rejected (:status verification)))
    (is (= "C9-UNSAFE"
           (get-in verification [:diagnostics 0 :rule])))
    (is (= :ownership-result-substitution
           (get-in verification [:diagnostics 0 :reason])))
    (is (= (:source-span request)
           (get-in verification [:diagnostics 0 :source-span])))
    (is (= (:origin-chain request)
           (get-in verification
                   [:diagnostics 0 :generated-origin-chain])))
    (is (= [{:kind :generated :generator :sh10-test}]
           (get-in generated-origin-result
                   [:diagnostics 0 :generated-origin-chain])))
    (is (= result (check request)))))
