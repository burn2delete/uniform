(ns seedless.project-identity-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [seedless.github :as gh]
            [simplify.github :as transport]
            [simplify.runtime :as rt])
  (:import [java.nio.file Files]))

(def default-config
  {:repository "fixture/repo"
   :project-title "Uniform Seedless"
   :campaign "fixture"
   :objective "Fixture seed removal"
   :repo-dir "."})

(defn context
  ([] (context {} {}))
  ([config state]
   (let [root (.toFile (Files/createTempDirectory "seedless-identity-test-"
                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
         state-dir (io/file root "state")]
     (.mkdirs state-dir)
     {:root root
      :state-dir (str state-dir)
      :state-file (str (io/file state-dir "state.edn"))
      :control-file (str (io/file state-dir "control.edn"))
      :log-lock (Object.)
      :processes (atom #{})
      :config (merge default-config config)
      :state (atom (merge {:schema :seedless/v1 :tasks {} :jobs {} :effects {}} state))})))

(defn delete-tree! [root]
  (doseq [file (reverse (file-seq (io/file root)))]
    (Files/deleteIfExists (.toPath file))))

(defn identity-marker [ctx kind stage key]
  (gh/marker (str kind "/"
                  (rt/digest (pr-str [(get-in ctx [:config :repository])
                                      (get-in ctx [:config :campaign])
                                      (get-in ctx [:config :objective])
                                      stage kind key])))))

(deftest recorded-project-id-still-requires-remote-identity
  (doseq [identity [{:owner {:id "other-owner"}
                     :readme (gh/marker "project/fixture/repo")
                     :repositories {:nodes [{:nameWithOwner "fixture/repo"}]}}
                    {:owner {:id "owner"}
                     :readme "unrelated"
                     :repositories {:nodes [{:nameWithOwner "fixture/repo"}]}}
                    {:owner {:id "owner"}
                     :readme (gh/marker "project/fixture/repo")
                     :repositories {:nodes [{:nameWithOwner "other/repo"}]}}]]
    (let [ctx (context {} {:project {:id "recorded"}})
          calls (atom 0)]
      (try
        (with-redefs [gh/graphql (fn [& _]
                                   (swap! calls inc)
                                   {:node (assoc identity :id "recorded")})]
          (is (thrown-with-msg? Exception #"verified"
                                (gh/verify-project! ctx {:id "recorded"} {:id "owner"})))
          (is (= 1 @calls)))
        (finally (delete-tree! (:root ctx)))))))

(deftest created-project-is-bootstrapped-before-project-metadata
  (let [ctx (context)
        operations (atom [])
        readme (atom "")
        repositories (atom [])
        fields [{:id "status" :name "Status"
                 :options (mapv (fn [name] {:id (str "option-" name) :name name})
                                transport/workflow-states)}]
        project {:id "created" :title "Uniform Seedless"}]
    (try
      (with-redefs [transport/owner-info (fn [& _] {:id "owner"})
                    transport/projects (fn [& _] [])
                    transport/fields (fn [& _]
                                       (swap! operations conj :fields)
                                       fields)
                    gh/pages (fn [& _] [])
                    gh/api (fn
                             ([_ _] {:node_id "repo-node"})
                             ([_ method endpoint body]
                              (swap! operations conj [:api method endpoint body])
                              {}))
                    gh/graphql (fn [_ query variables]
                                 (cond
                                   (str/includes? query "createProjectV2(input")
                                   (do (swap! operations conj :create)
                                       {:createProjectV2 {:projectV2 project}})

                                   (and (str/includes? query "updateProjectV2(input:")
                                        (= (:readme variables) (gh/project-marker ctx)))
                                   (do (swap! operations conj :marker)
                                       (reset! readme (:readme variables))
                                       {:updateProjectV2 {:projectV2 {:id "created" :readme @readme}}})

                                   (str/includes? query "linkProjectV2ToRepository")
                                   (do (swap! operations conj :repository)
                                       (reset! repositories [{:nameWithOwner "fixture/repo"}])
                                       {:linkProjectV2ToRepository {:repository {:id "repo-node"}}})

                                   (str/includes? query "owner{")
                                   {:node {:id "created" :owner {:id "owner"}
                                           :readme @readme
                                           :repositories {:nodes @repositories}}}

                                   (str/includes? query "views(first:")
                                   {:node {:views {:nodes []}}}

                                   (str/includes? query "createProjectV2View(")
                                   {:createProjectV2View {:projectV2View {:id (:name variables)}}}

                                   (str/includes? query "updateProjectV2View(")
                                   {:updateProjectV2View {:projectV2View {:id (:id variables)}}}

                                   (str/includes? query "updateProjectV2(input:")
                                   {:updateProjectV2 {:projectV2 {:id "created"}}}

                                   :else {}))]
        (gh/setup! ctx)
        (let [ops @operations]
          (is (< (.indexOf ops :marker) (.indexOf ops :fields)))
          (is (< (.indexOf ops :repository) (.indexOf ops :fields)))
          (is (= "created" (get-in @(:state ctx) [:project :id])))
          (is (= :done (get-in @(:state ctx) [:effects "project-identity-marker/created" :status])))
          (is (= :done (get-in @(:state ctx) [:effects "project-identity-repository/created" :status])))))
      (finally (delete-tree! (:root ctx))))))

(deftest recovered-create-effect-does-not-adopt-title-drift
  (let [ctx (context {} {:effects {"create-project" {:status :done
                                                     :result {:id "created" :title "Uniform Seedless"}}}})
        requested (atom [])]
    (try
      (with-redefs [transport/owner-info (fn [& _] {:id "owner"})
                    transport/projects (fn [& _] [{:id "old" :title "Uniform Seedless"}])
                    gh/api (fn
                             ([_ _] {:node_id "repo-node"})
                             ([_ _ _ _] (throw (ex-info "unexpected write" {}))))
                    gh/graphql (fn [_ _ variables]
                                 (swap! requested conj variables)
                                 {:node {:id "created" :owner {:id "other-owner"}
                                         :readme ""
                                         :repositories {:nodes []}}})]
        (try
          (gh/setup! ctx)
          (is false "setup should reject drifted recovered Project")
          (catch clojure.lang.ExceptionInfo error
            (is (= "created" (get-in (ex-data error) [:project :id]))))))
      (is (= [{:id "created"}] @requested))
      (finally (delete-tree! (:root ctx))))))

(deftest pending-or-restricted-create-effects-are-not-replayed
  (doseq [status [:pending :restricted :bogus]]
    (let [ctx (context {} {:effects {"create-project" {:status status}}})
          calls (atom 0)]
      (try
        (with-redefs [transport/owner-info (fn [& _] {:id "owner"})
                      transport/projects (fn [& _] (swap! calls inc) [])
                      gh/graphql (fn [& _] (swap! calls inc) {})]
          (is (thrown? Exception (gh/setup! ctx)))
          (is (zero? @calls)))
        (finally (delete-tree! (:root ctx)))))))

(deftest pending-artifact-effects-cannot-adopt-existing-remote-objects
  (let [issue-ctx (context {} {:effects {"issue/slice-a" {:status :pending}}})
        issue-body (str "Stage: SH-07\n\n"
                        (identity-marker issue-ctx "issue" "SH-07" "slice-a"))
        milestone-ctx (context {} {:effects {"milestone/SH-07" {:status :pending}}})
        milestone-description (identity-marker milestone-ctx "milestone" "SH-07" nil)
        calls (atom 0)]
    (try
      (with-redefs [gh/pages (fn [_ endpoint]
                               (if (str/includes? endpoint "/issues?")
                                 [{:pull_request false :body issue-body}]
                                 [{:title "fixture / SH-07"
                                   :description milestone-description}]))
                    gh/api (fn [& _] (swap! calls inc) {})]
        (is (thrown? Exception
                     (gh/issue! issue-ctx "slice-a" "Replace slice-a" "Stage: SH-07" [] nil)))
        (is (thrown? Exception (gh/milestone! milestone-ctx "SH-07")))
        (is (zero? @calls)))
      (finally
        (delete-tree! (:root issue-ctx))
        (delete-tree! (:root milestone-ctx))))))

(deftest same-title-wrong-identity-milestone-is-rejected
  (let [ctx (context)
        calls (atom 0)
        title "fixture / SH-07"]
    (try
      (with-redefs [gh/pages (fn [& _] [{:title title :description "belongs to another objective"}])
                    gh/api (fn [& _] (swap! calls inc) {})]
        (is (thrown? Exception (gh/milestone! ctx "SH-07")))
        (is (zero? @calls)))
      (finally (delete-tree! (:root ctx))))))

(deftest issue-identity-binds-repository-campaign-objective-stage-and-key
  (let [ctx (context)
        calls (atom [])
        body "Stage: SH-07; workstream: compiler.\nEvidence: fixture"
        identity (identity-marker ctx "issue" "SH-07" "slice-a")]
    (try
      (with-redefs [gh/pages (fn [& _] [{:title "Replace slice-a"
                                         :body "old campaign marker"}])
                    gh/api (fn [_ method endpoint payload]
                             (swap! calls conj [method endpoint payload])
                             {:number 7})]
        (gh/issue! ctx "slice-a" "Replace slice-a" body ["seedless:compiler"] nil)
        (is (= 1 (count @calls)))
        (is (= identity (second (re-find #"(<!-- uniform-seedless:issue/[0-9a-f]+ -->)" (get-in @calls [0 2 :body]))))))
      (finally (delete-tree! (:root ctx))))))
