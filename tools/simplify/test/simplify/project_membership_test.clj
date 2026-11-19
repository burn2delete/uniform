(ns simplify.project-membership-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [simplify.github :as gh]
            [simplify.runtime :as rt])
  (:import [java.nio.file Files]))

(defn temp-dir []
  (str (Files/createTempDirectory "uniform-project-membership-test-"
                                  (make-array java.nio.file.attribute.FileAttribute 0))))

(defn context []
  (let [dir (temp-dir)
        config {:repo-dir dir :state-dir dir}]
    (merge {:state-dir dir
            :state-file (str (io/file dir "state.edn"))
            :log-lock (Object.)
            :state (atom {:schema 1 :tasks {} :jobs {} :effects {}})
            :config config}
           (rt/coordination dir config))))

(def duplicate-error
  (ex-info "GitHub GraphQL failed"
           {:errors [{:type "UNPROCESSABLE"
                      :message "Content already exists in this project"}]
            :restricted false}))

(defn membership-page [nodes has-next? end-cursor]
  {:node {:items {:nodes nodes
                  :pageInfo {:hasNextPage has-next?
                             :endCursor end-cursor}}}})

(defn project-context []
  (doto (context)
    (rt/update! assoc :project {:id "project"})))

(defn calls-of [calls kind]
  (filter #(if (map? %) (= kind (:kind %)) (= kind %)) @calls))

(defn transport-command [calls response-fn]
  (fn [_ _ argv input]
    (is (= ["gh" "api" "--method" "POST" "graphql" "--input" "-"] argv))
    (let [{:keys [query variables]} (json/read-str input :key-fn keyword)
          kind (if (str/starts-with? query "query") :read :add)
          call {:kind kind :query query :variables variables}]
      (swap! calls conj call)
      (response-fn call calls))))

(deftest duplicate-add-is-read-back-once-without-repeating-add
  (let [ctx (project-context)
        calls (atom [])]
    (with-redefs [gh/graphql (fn [_ query variables]
                               (cond
                                 (str/starts-with? query "query")
                                 (do (swap! calls conj {:kind :read :variables variables})
                                     (if (= 1 (count (calls-of calls :read)))
                                       (membership-page [] false nil)
                                       (membership-page [{:id "item-after-race"
                                                          :content {:id "issue"}}] false nil)))

                                 (str/starts-with? query "mutation")
                                 (do (swap! calls conj {:kind :add :variables variables})
                                     (throw duplicate-error))

                                 :else (throw (AssertionError. (str "Unexpected GraphQL operation: " query)))))]
      (is (= "item-after-race" (gh/item! ctx {:node_id "issue"})))
      (is (= "item-after-race" (gh/item! ctx {:node_id "issue"})))
      (is (= 1 (count (calls-of calls :add))))
      (is (= 2 (count (calls-of calls :read))))
      (is (= :done (get-in @(:state ctx) [:effects "project-item/project/issue" :status])))
      (is (= "item-after-race" (get-in @(:state ctx) [:effects "project-item/project/issue" :result]))))))

(deftest duplicate-add-from-real-graphql-transport-is-read-back-once
  (let [ctx (project-context)
        calls (atom [])]
    (with-redefs [rt/command
                  (transport-command
                   calls
                   (fn [{:keys [kind]} calls]
                     (if (= :read kind)
                       {:exit 0
                        :out (json/write-str
                              {:data (if (= 1 (count (calls-of calls :read)))
                                        (membership-page [] false nil)
                                        (membership-page [{:id "item-after-transport-race"
                                                           :content {:id "issue"}}] false nil))})
                        :err ""}
                       {:exit 1
                        :out (json/write-str {:errors [{:type "UNPROCESSABLE"
                                                        :message "Content already exists in this project"}]})
                        :err "GraphQL: Content already exists in this project"})))]
      (is (= "item-after-transport-race" (gh/item! ctx {:node_id "issue"})))
      (is (= 1 (count (calls-of calls :add))))
      (is (= 2 (count (calls-of calls :read))))
      (is (= :done (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))

(deftest project-controls-settlement-can-scope-an-approved-pr-integration
  (let [ctx (assoc-in (context) [:config :repository] "owner/repo") k (rt/digest (pr-str ["POST" "graphql"]))
        local {:method "POST" :endpoint "graphql" :error "rate limit exceeded"}
        auth {:kind :single-pr-composition :pr 5 :repo "owner/repo" :active-issues {:increment 4}}
        raw (pr-str auth)
        scope {:task-key "finding" :candidate "head" :authority-raw raw :authority-sha256 (rt/digest raw)}
        ctx (assoc ctx :pr-integration scope :coordinator-owner (atom true))
        tuple {:base "base" :candidate "head" :tree "tree"}
        task (merge tuple {:pr {:node_id "PR" :number 5} :issue {:node_id "issue" :number 4}
                           :checks (assoc tuple :result :passed :hosted-ci (assoc tuple :result :passed))
                           :review (assoc tuple :verdict "approved")
                           :pr-composition {:stage :approved :authority-sha256 (:authority-sha256 scope)
                                            :authority auth}})
        hold {:kind :project-controls-pagination :scope :all-cursors :state-file (:state-file ctx)
              :legacy-api-key k :retained-local-guard local}
        calls (atom [])]
    (rt/update! ctx assoc :project {:id "project" :workflow {:id "status" :options [{:name "Merge queue" :id "queue"}]}}
                :tasks {"finding" task} :restricted-actions {k local}
                :effects {"project-item/project/issue" {:status :done :result "item"}})
    (rt/shared-restrict! ctx :project-controls "project" hold)
    (with-redefs [rt/command (fn [_ _ _ body] (swap! calls conj body)
                               {:exit 0 :out (json/write-str {:data {:repository {:pullRequest {:reviewThreads
                                              {:nodes [] :pageInfo {:hasNextPage false}}}}}})})]
      (is (gh/review-threads-clear? ctx 5))
      (gh/draft! ctx (:pr task) false "head")
      (gh/status! ctx (:issue task) "Merge queue")
      (is (= 3 (count @calls)))
      (is (thrown? Exception (gh/review-threads-clear? ctx 6)))
      (is (thrown? Exception (gh/graphql ctx "query{viewer{login}}" {})))
      (is (thrown? Exception (gh/review-threads-clear? (dissoc ctx :pr-integration) 5)))
      (is (thrown? Exception (gh/review-threads-clear? (assoc-in ctx [:pr-integration :candidate] "other") 5)))
      (let [denied (assoc local :failed-request {:phase :provider-returned :body-sha256 (rt/digest (first @calls))})]
        (rt/update! ctx assoc-in [:restricted-actions k] denied)
        (rt/coordinate! ctx (fn [s] [(assoc-in s [:restrictions :project-controls "project" :retained-local-guard] denied) nil]))
        (is (thrown? Exception (gh/review-threads-clear? ctx 5)))
        (is (= 3 (count @calls)))
        (rt/update! ctx assoc-in [:restricted-actions k] local)
        (rt/coordinate! ctx (fn [s] [(assoc-in s [:restrictions :project-controls "project" :retained-local-guard] local) nil])))
      (rt/shared-restrict! ctx :api k {:method "POST" :endpoint "graphql" :error "new episode"})
      (is (thrown? Exception (gh/review-threads-clear? ctx 5)))
      (is (= 3 (count @calls))))))

(deftest local-scheduling-uses-journaled-tracking-and-preserves-refusals
  (let [repo "owner/repo" issue {:node_id "i" :number 3} k (rt/digest (pr-str ["POST" "graphql"]))
        legacy {:method "POST" :endpoint "graphql" :error "historical provider refusal"}
        ctx (-> (project-context) (assoc :coordinator-owner (atom true) :local-scheduling {:project "project" :repository repo})
                (assoc-in [:config :repository] repo))
        hold {:kind :project-controls-pagination :scope :all-cursors :project "project" :state-file (:state-file ctx)
              :legacy-api-key k :retained-local-guard legacy}
        calls (atom [])]
    (rt/update! ctx assoc :repository repo :campaign-issue issue :restricted-actions {k legacy}
                :project {:id "project" :workflow {:id "wf" :options [{:id "opt" :name "Implementing"}]}})
    (rt/shared-restrict! ctx :project-controls "project" hold)
    (with-redefs [rt/command (transport-command calls
                               (fn [{:keys [query]} _]
                                 {:exit 0 :out (json/write-str
                                                {:data (if (str/includes? query "projectItems")
                                                         {:node {:projectItems {:nodes [{:id "item" :project {:id "project"}}]
                                                                                :pageInfo {:hasNextPage false}}}}
                                                         {:updateProjectV2ItemFieldValue {:projectV2Item {:id "item"}}})})}))]
      (gh/status! ctx issue "Implementing")
      (is (= 2 (count @calls)))
      (is (str/includes? (:query (first @calls)) "on Issue{projectItems"))
      (is (thrown? Exception (gh/graphql ctx "query { node { items { fieldValues } } }" {})))
      (is (thrown? Exception (gh/item! ctx {:node_id "foreign"})))
      (let [{:keys [query variables]} (last @calls)]
        (is (thrown? Exception (gh/graphql ctx query variables)) "Completed tracking cannot be reissued directly")
        (rt/update! ctx assoc-in [:restricted-actions k :failed-request] {:phase :provider-returned})
        (is (thrown? Exception (gh/status! ctx issue "Implementing")))
        (rt/update! ctx assoc-in [:restricted-actions k] legacy)
        (is (thrown? Exception (gh/status! ctx issue "Implementing")))) ; Prior tracking refusal stays terminal.
      (rt/shared-restrict! ctx :api k legacy)
      (is (thrown? Exception (gh/graphql ctx (:query (first @calls)) (:variables (first @calls)))))
      (is (= 2 (count @calls))))))

(deftest recovery-can-distinguish-a-provider-refusal-from-a-later-blocked-request
  (doseq [response [{:exit 1 :err "HTTP 403: rate limit exceeded" :out ""}
                    {:exit 0 :err "" :out (json/write-str {:errors [{:type "RATE_LIMITED"}]})}]]
    (let [ctx (context) calls (atom [])
          attempt (fn [id arguments]
                    (rt/effect! ctx id #(gh/graphql ctx "query Fixture { viewer { login } }" arguments)))
          failure (fn [id] (get-in @(:state ctx) [:effects id :failed-request]))]
      (with-redefs [rt/command (fn [_ _ _ input] (swap! calls conj input) response)]
        (is (thrown? Exception (attempt "first" {:cursor "A"})))
        (is (thrown? Exception (attempt "second" {:cursor "B"})))
        (is (= 1 (count @calls)))
        (is (= (rt/digest (first @calls)) (:body-sha256 (failure "first"))))
        (is (= :provider-returned (:phase (failure "first"))))
        (is (= :before-call (:phase (failure "second"))))
        (is (not= (:body-sha256 (failure "first")) (:body-sha256 (failure "second"))))
        (is (= (failure "first")
               (:failed-request (rt/shared-restriction ctx :api (rt/digest (pr-str ["POST" "graphql"]))))))))))

(deftest inspected-status-throttle-preserves-items-and-allows-independent-work
  (let [ctx (assoc (context) :coordinator-owner (atom true))
        k (rt/digest (pr-str ["POST" "graphql"]))
        query @#'gh/update-status-mutation
        variables (fn [item] {:project "p" :item item :field "wf" :option "ready"})
        request (fn [item phase]
                  {:method "POST" :endpoint "graphql" :phase phase
                   :body-sha256 (rt/digest (json/write-str {:query query :variables (variables item)}))})
        local {:method "POST" :endpoint "graphql" :error "gh: API rate limit exceeded for 192.0.2.1. (HTTP 403)"
               :failed-request (request "a" :provider-returned)}
        id "project-status/p/A/1" calls (atom [])]
    (rt/update! ctx assoc :restricted-actions {k local}
                :project {:id "p" :workflow {:id "wf" :options [{:id "ready"}]}}
                :tracking-intents {"project-status/p/A" id "project-status/p/B" "project-status/p/B/2"}
                :effects {id {:status :restricted :failed-request (:failed-request local)}
                          "project-status/p/B/2" {:status :restricted :failed-request (request "b" :before-call)}
                          "project-item/p/A" {:status :done :result "a"}
                          "project-item/p/B" {:status :done :result "b"}})
    (rt/shared-restrict! ctx :api k local)
    (is (thrown? Exception (gh/settle-status-limit! ctx "project-status/p/B/2")))
    (let [before @(:state ctx)]
      (is (= id (:effect (gh/settle-status-limit! ctx id))))
      (is (= before @(:state ctx))))
    (let [peer (assoc ctx :state (atom {}) :state-file "another-campaign")]
      (with-redefs [rt/command (fn [_ _ _ payload]
                                 (swap! calls conj payload)
                                 (cond
                                   (str/includes? payload "forbidden") {:exit 1 :err "gh: forbidden (HTTP 403)"
                                                                       :out "{\"errors\":[{\"type\":\"RATE_LIMITED\"}]}"}
                                   (str/includes? payload "throttled") {:exit 0 :out "{\"errors\":[{\"type\":\"RATE_LIMITED\"}]}"}
                                   :else {:exit 0 :out "{\"data\":{}}"}))]
        (doseq [controller [ctx peer] item ["a" "b"]]
          (is (thrown? Exception (gh/graphql controller query (assoc (variables item) :option "done")))))
        (is (empty? @calls))
        (is (= {} (gh/graphql peer query (variables "independent"))))
        (is (thrown? Exception (gh/graphql ctx query (variables "throttled"))))
        (is (thrown? Exception (gh/graphql peer query (variables "throttled"))))
        (is (= {} (gh/graphql peer query (variables "another-item"))))
        (is (thrown? Exception (gh/graphql peer query (variables "forbidden"))))
        (is (thrown? Exception (gh/graphql ctx query (variables "unrelated-after-denial"))))
        (is (= 4 (count @calls)))))))

(deftest malformed-or-mixed-graphql-output-does-not-admit-membership
  (doseq [[label output error-text]
          [["invalid JSON" "not-json" "GraphQL: Content already exists in this project"]
           ["mixed errors"
            (json/write-str {:errors [{:type "UNPROCESSABLE"
                                       :message "Content already exists in this project"}
                                      {:type "VALIDATION" :message "another error"}]})
            "GraphQL: Content already exists in this project"]]]
    (testing label
      (let [ctx (project-context)
            calls (atom [])]
        (with-redefs [rt/command
                      (transport-command
                       calls
                       (fn [{:keys [kind]} _]
                         (if (= :read kind)
                           {:exit 0
                            :out (json/write-str {:data (membership-page [] false nil)})
                            :err ""}
                           {:exit 1 :out output :err error-text})))]
          (is (thrown? Exception (gh/item! ctx {:node_id "issue"})))
          (is (= 1 (count (calls-of calls :read))))
          (is (= 1 (count (calls-of calls :add))))
          (is (= :uncertain (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))))

(deftest restricted-graphql-transport-output-does-not-admit-membership
  (doseq [[label error-type error-text]
          [["forbidden" "FORBIDDEN" "GraphQL: Resource not accessible"]
           ["rate limited" "RATE_LIMITED" "GraphQL: Rate limit exceeded"]
           ["unauthorized" "UNAUTHORIZED" "GraphQL: Requires authentication"]]]
    (testing label
      (let [ctx (project-context)
            calls (atom [])]
        (with-redefs [rt/command
                      (transport-command
                       calls
                       (fn [{:keys [kind]} _]
                         (if (= :read kind)
                           {:exit 0
                            :out (json/write-str {:data (membership-page [] false nil)})
                            :err ""}
                           {:exit 1
                            :out (json/write-str {:errors [{:type error-type :message error-text}]})
                            :err error-text})))]
          (is (thrown? Exception (gh/item! ctx {:node_id "issue"})))
          (is (= 1 (count (calls-of calls :read))))
          (is (= 1 (count (calls-of calls :add))))
          (is (= :restricted (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))))

(deftest non-graphql-api-output-does-not-expose-graphql-errors
  (let [ctx (project-context)
        output (json/write-str {:errors [{:type "UNPROCESSABLE"
                                          :message "Content already exists in this project"}]})]
    (with-redefs [rt/command (fn [_ _ _ _] {:exit 1 :out output :err "request failed"})]
      (try
        (gh/api ctx "POST" "repos/example/items" {})
        (is false "Expected non-GraphQL API failure")
        (catch clojure.lang.ExceptionInfo error
          (is (nil? (:errors (ex-data error))))
          (is (= "repos/example/items" (:endpoint (ex-data error))))
          (is (= 1 (:exit (ex-data error)))))))))

(deftest preexisting-membership-still-skips-add-across-pages
  (let [ctx (project-context)
        calls (atom [])]
    (with-redefs [gh/graphql (fn [_ query variables]
                               (is (str/starts-with? query "query"))
                               (swap! calls conj variables)
                               (if (:after variables)
                                 (membership-page [{:id "item-existing"
                                                    :content {:id "issue"}}] false nil)
                                 (membership-page [{:id "other-item"
                                                    :content {:id "other"}}] true "next")))]
      (is (= "item-existing" (gh/item! ctx {:node_id "issue"})))
      (is (= 2 (count @calls)))
      (is (every? #(nil? (:after %)) (take 1 @calls)))
      (is (= "next" (:after (second @calls))))
      (is (= :done (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))

(deftest duplicate-add-without-exact-readback-remains-uncertain
  (doseq [[label postread]
          [["empty" (fn [_ _ _] (membership-page [] false nil))]
           ["unavailable" (fn [& _] {})]]]
    (testing label
      (let [ctx (project-context)
            calls (atom [])]
        (with-redefs [gh/graphql (fn [_ query variables]
                                   (cond
                                     (str/starts-with? query "query")
                                     (do (swap! calls conj :read)
                                         (if (= 1 (count (calls-of calls :read)))
                                           (membership-page [] false nil)
                                           (postread nil query variables)))

                                     (str/starts-with? query "mutation")
                                     (do (swap! calls conj :add)
                                         (throw duplicate-error))

                                     :else (throw (AssertionError. "Unexpected GraphQL operation"))))]
          (is (thrown-with-msg? Exception
                                #"Duplicate Project membership has no verified readback|evidence is unavailable"
                                (gh/item! ctx {:node_id "issue"})))
          (is (= 1 (count (calls-of calls :add))))
          (is (= 2 (count (calls-of calls :read))))
          (is (= :uncertain (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))))

(deftest duplicate-read-restriction-is-not-retried
  (let [ctx (project-context)
        calls (atom [])
        restricted-read (ex-info "GitHub GraphQL failed"
                                 {:errors [{:type "FORBIDDEN" :message "Resource not accessible"}]
                                  :restricted true})]
    (with-redefs [gh/graphql (fn [_ query _]
                               (cond
                                 (str/starts-with? query "query")
                                 (do (swap! calls conj :read)
                                     (if (= 1 (count (calls-of calls :read)))
                                       (membership-page [] false nil)
                                       (throw restricted-read)))

                                 (str/starts-with? query "mutation")
                                 (do (swap! calls conj :add)
                                     (throw duplicate-error))

                                 :else (throw (AssertionError. "Unexpected GraphQL operation"))))]
      (is (thrown-with-msg? Exception #"GitHub GraphQL failed" (gh/item! ctx {:node_id "issue"})))
      (is (= 1 (count (calls-of calls :add))))
      (is (= 2 (count (calls-of calls :read))))
      (is (= :restricted (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))

(deftest unrelated-or-restricted-add-fails-without-postread
  (doseq [[label add-error expected-status]
          [["unrelated" (ex-info "GitHub GraphQL failed"
                                  {:errors [{:type "VALIDATION" :message "invalid content"}]
                                   :restricted false})
            :uncertain]
           ["duplicate without restriction proof" (ex-info "GitHub GraphQL failed"
                                                              {:errors [{:type "UNPROCESSABLE"
                                                                         :message "Content already exists in this project"}]})
            :uncertain]
           ["restricted" (ex-info "GitHub GraphQL failed"
                                   {:errors [{:type "FORBIDDEN" :message "Resource not accessible"}]
                                    :restricted true})
            :restricted]]]
    (testing label
      (let [ctx (project-context)
            calls (atom [])]
        (with-redefs [gh/graphql (fn [_ query _]
                                   (cond
                                     (str/starts-with? query "query")
                                     (do (swap! calls conj :read)
                                         (membership-page [] false nil))

                                     (str/starts-with? query "mutation")
                                     (do (swap! calls conj :add)
                                         (throw add-error))

                                     :else (throw (AssertionError. "Unexpected GraphQL operation"))))]
          (is (thrown? Exception (gh/item! ctx {:node_id "issue"})))
          (is (= 1 (count (calls-of calls :add))))
          (is (= 1 (count (calls-of calls :read))))
          (is (= expected-status (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))))
