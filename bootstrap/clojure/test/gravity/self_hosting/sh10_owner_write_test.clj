(ns gravity.self-hosting.sh10-owner-write-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private root
  (delay
    (loop [p (.getParent (.toPath (io/file (.toURI
                (io/resource "gravity/self_hosting/sh10_owner_write_test.clj")))))]
      (cond (nil? p) (throw (ex-info "repository root missing" {}))
            (.isFile (.toFile (.resolve p "deps.edn"))) p
            :else (recur (.getParent p))))))
(defn- path [p] (str (.resolve @root p)))
(def ^:private source "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")
(defn- fixture [family ext]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-10/" family "/owner-write" ext))
(defn- compile-plan [relative]
  (let [p (path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! p :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter p (slurp p))))
(def ^:private c9 (delay (compile-plan source)))
(def ^:private accepted (delay (compile-plan (fixture "accepted" ".gravity"))))
(def ^:private rejected (delay (compile-plan (fixture "rejected" ".gravity"))))
(defn- execute [plan function args]
  (bootstrap/p15-s23-stage2-runtime-execute-function
    {:engine :sh10-owner-write-development :compiler-artifact-plan? true}
    @plan function args))
(defn- invoke [function & args] (execute c9 function (vec args)))
(defn- request [events] (execute accepted 'owner-write-request [events]))
(defn- event [id op borrow] (execute accepted 'owner-write-event [id op borrow]))
(defn- check [r] (invoke 'sh10-check-ownership-request r))
(defn- verified? [r result]
  (= :passed (:status (invoke 'sh10-verify-ownership-result r result))))
(defn- scenario [r]
  {:artifact :gravity/sh10-authenticated-owned-mutable-scenario :schema-version 1
   :scenario-id (:request-id r) :core-node-id :value
   :initialization (:initialization r) :owner-lifetime-end (:owner-lifetime-end r)
   :events (mapv #(select-keys % [:event-id :op :borrow-id :destination-owner
                                :destination-lifetime-end :escape-destination :control-path]) (:events r))})

(deftest sh10-owner-write-fixtures-development-kernel
  (doseq [family ["accepted" "rejected"]]
    (let [g (slurp (path (fixture family ".gravity"))) q (slurp (path (fixture family ".qst")))]
      (is (= g q))
      ;; G request constructors execute below; Q has reader parity only.
      (is (= (bootstrap/read-forms (path (fixture family ".gravity")) g)
             (bootstrap/read-forms (path (fixture family ".qst")) q)))))
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c9)))
  (is (contains? (:events (invoke 'sh10-ownership-policy)) :write))
  (is (= :accepted (:status (check (execute accepted 'write-read-request [])))))
  (is (= :owner-write-during-active-borrow
         (get-in (check (execute rejected 'aliased-owner-write-request [:borrow-immutable])) [:diagnostics 0 :reason]))))

(deftest sh10-owner-write-transitions-development-kernel
  (doseq [r [(execute accepted 'write-read-request [])
             (request [(event :w0 :write nil) (event :r0 :read nil) (event :w1 :write nil)])
             (execute accepted 'write-after-end-request [:borrow-immutable])
             (execute accepted 'write-after-end-request [:borrow-mutable])
             (assoc (request [(event :i :initialize nil) (event :w :write nil) (event :r :read nil)])
                    :initialization :uninitialized)]]
    (let [result (check r) writes (filter #(= :write (:operation %)) (:ownership-facts result))]
      (is (= :accepted (:status result)))
      (is (= :owned-mutable-owner-write (:scope result)))
      (is (= [:initialized :available :owner 0 nil]
             ((juxt :initialization :availability :current-owner :immutable-borrow-count :mutable-borrow-id) (:state result))))
      (is (= (mapv :op (:events r)) (mapv :operation (:ownership-facts result))))
      (is (seq writes))
      (is (every? #(= (:state-before %) (:state-after %)) writes))
      (is (= r (get-in result [:identity-input :request])))
      (is (verified? r result))
      (is (= (select-keys r [:type-fact-id :effect-fact-id :source-span :origin-chain :profile :target])
             (select-keys (:preserves result) [:type-fact-id :effect-fact-id :source-span :origin-chain :profile :target])))
      (doseq [changed [(assoc result :scope :persistent-immutable-read-only)
                       (assoc-in result [:ownership-facts 0 :operation] :write-tampered)
                       (assoc-in result [:ownership-facts 0 :source-span] {})
                       (assoc-in result [:state :availability] :moved)]]
        (is (false? (verified? r changed))))))
  ;; A remaining second immutable borrow still forbids writing after the first ends.
  (let [r (request [(event :b1 :borrow-immutable :b1) (event :b2 :borrow-immutable :b2)
                    (event :e1 :end-borrow :b1) (event :w :write nil)])]
    (is (= :owner-write-during-active-borrow (get-in (check r) [:diagnostics 0 :reason]))))
  (doseq [acquisition [:borrow-immutable :borrow-mutable]]
    (let [r (execute rejected 'aliased-owner-write-request [acquisition])
          result (check r) d (first (:diagnostics result)) e (second (:events r))]
      (is (= [:rejected "C9-MUT-ALIAS" :owner-write-during-active-borrow]
             [(:status result) (:rule d) (:reason d)]))
      (is (= [:write :write :value :owner :meta :jvm]
             ((juxt :operation :event-id :value-id :owner-id :profile :target) d)))
      (is (= (:source-span e) (:source-span d)))
      (is (= (:origin-chain e) (:generated-origin-chain d)))
      (is (= (:control-path e) (:control-path d)))
      (is (= :end-all-borrows-before-owner-write (:remediation d)))
      (is (= 1 (count (:ownership-facts result))))
      (is (verified? r result))
      (is (false? (verified? r (assoc-in result [:diagnostics 0 :generated-origin-chain] []))))))
  (doseq [[events init rule reason]
          [[[(assoc (event :move :move nil) :destination-owner :other) (event :w :write nil)] :initialized
            "C9-USE-AFTER-MOVE" :operation-after-move]
           [[(event :consume :consume nil) (event :w :write nil)] :initialized
            "C9-USE-AFTER-CONSUME" :operation-after-consume]
           [[(event :w :write nil)] :uninitialized "L10-UNINIT-READ" :operation-before-initialization]]]
    (let [r (assoc (request events) :initialization init) result (check r)]
      (is (= [rule reason] ((juxt :rule :reason) (first (:diagnostics result)))))
      (is (verified? r result))))
  (doseq [ownership [:owned-mutable :persistent-immutable]]
    (let [r (assoc (request [(event :read :read nil)]) :ownership-kind ownership) result (check r)]
      (is (= :accepted (:status result)))
      (is (= (if (= :owned-mutable ownership) :owned-mutable-initialization-borrow-move-consume
                 :persistent-immutable-read-only) (:scope result)))))
  (let [r (assoc (request [(event :w :write nil)]) :ownership-kind :persistent-immutable)]
    (is (= :malformed-normalized-ownership-request (get-in (check r) [:diagnostics 0 :reason])))))

(deftest sh10-owner-write-shape-and-adapter-fences-development-kernel
  (let [w (event :w :write nil)]
    (is (true? (invoke 'sh10-valid-event-shape? w)))
    (doseq [bad [(dissoc w :event-id) (dissoc w :source-span) (dissoc w :origin-chain)
                 (assoc w :op :write-via-borrow) (assoc w :borrow-id :borrow)
                 (assoc w :destination-owner :other) (assoc w :destination-lifetime-end 3)
                 (assoc w :escape-destination :closure-capture)]]
      (let [r (request [bad]) result (check r)]
        (is (= ["C9-UNSAFE" (if (nil? (:event-id bad)) :malformed-normalized-ownership-request
                                :malformed-ownership-event)]
               ((juxt :rule :reason) (first (:diagnostics result)))))
        (is (verified? r result))))
    ;; Write does not cross a pure authenticated-C8 or runtime-borrow adapter.
    (let [s (scenario (request [w])) e (first (:events s))]
      (is (false? (invoke 'sh10-authenticated-owned-mutable-event-valid? e)))
      (is (false? (invoke 'sh10-runtime-mutable-borrow-event-valid? e)))
      (is (false? (invoke 'sh10-authenticated-owned-mutable-scenario-valid? s)))
      (is (false? (invoke 'sh10-runtime-mutable-borrow-scenario-valid?
                          (assoc s :artifact :gravity/sh10-runtime-checked-mutable-borrow-scenario))))
      (is (not (contains? (:accepted-events (invoke 'sh10-authenticated-owned-mutable-policy)) :write)))
      (is (not (contains? (:accepted-events (invoke 'sh10-runtime-mutable-borrow-policy)) :write))))
    (doseq [r [(request (vec (repeat 1025 w))) (request [w w])
               (assoc (request [w]) :effect-fact-id "not-an-effect-identity")]]
      (is (= :malformed-normalized-ownership-request (get-in (check r) [:diagnostics 0 :reason]))))))
