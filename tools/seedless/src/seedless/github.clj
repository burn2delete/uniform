(ns seedless.github
  "Seedless Project policy over the existing GitHub transport."
  (:require [clojure.string :as str]
            [seedless.schema :as schema]
            [simplify.github :as transport]
            [simplify.runtime :as rt]))

(def api transport/api)
(def graphql transport/graphql)
(def pages transport/pages)
(def repo-path transport/repo-path)
(def states transport/workflow-states)
(def item! transport/item!)
(def status! transport/status!)
(def relation! transport/relation!)

(defn marker [key] (str "<!-- uniform-seedless:" key " -->"))
(defn project-marker [ctx]
  (marker (str "project/" (get-in ctx [:config :repository]))))

(defn- project-identity! [ctx project]
  (:node (graphql ctx "query($id:ID!){node(id:$id){... on ProjectV2{id owner{... on User{id login} ... on Organization{id login}} readme repositories(first:100){nodes{nameWithOwner}}}}}"
                  {:id (:id project)})))

(defn- owner-matches? [ctx expected-owner remote-owner]
  (let [expected-id (:id expected-owner)
        expected-login (first (str/split (get-in ctx [:config :repository]) #"/"))]
    (and (map? remote-owner)
         (if expected-id
           (= expected-id (:id remote-owner))
           (= expected-login (:login remote-owner))))))

(defn- verified-project-identity? [ctx expected-owner project identity]
  (and (map? identity)
       (= (:id project) (:id identity))
       (owner-matches? ctx expected-owner (:owner identity))
       (str/includes? (or (:readme identity) "") (project-marker ctx))
       (some #(= (get-in ctx [:config :repository]) (:nameWithOwner %))
             (get-in identity [:repositories :nodes]))))

(defn verify-project!
  ([ctx project] (verify-project! ctx project (transport/owner-info ctx)))
  ([ctx project expected-owner]
   (let [identity (project-identity! ctx project)]
     (when-not (verified-project-identity? ctx expected-owner project identity)
       (rt/fail! "Project has no verified Seedless owner, marker and repository identity"
                 {:project project :identity identity :owner expected-owner}))
     identity)))

(defn- project-repository-linked? [ctx identity]
  (some #(= (get-in ctx [:config :repository]) (:nameWithOwner %))
        (get-in identity [:repositories :nodes])))

(defn- project-identity-repositories [identity]
  (keep :nameWithOwner (get-in identity [:repositories :nodes])))

(defn- project-identity-compatible? [ctx identity]
  (let [repository (get-in ctx [:config :repository])
        repositories (set (project-identity-repositories identity))]
    (and (or (str/blank? (:readme identity))
             (str/includes? (or (:readme identity) "") (project-marker ctx)))
         (every? #{repository} repositories))))

(defn- project-identity-complete? [ctx identity]
  (and (project-identity-compatible? ctx identity)
       (str/includes? (or (:readme identity) "") (project-marker ctx))
       (project-repository-linked? ctx identity)))

(defn- ensure-effect-reconciled! [ctx id]
  (let [effect (get-in @(:state ctx) [:effects id])
        status (:status effect)]
    (cond
      (nil? effect) nil
      (= :done status) nil
      (#{:pending :uncertain :restricted} status)
      ;; Calling effect! deliberately raises without invoking an operation.  A
      ;; durable external write must be reconciled before setup can continue.
      (rt/effect! ctx id (fn [] nil))
      :else (rt/fail! "Effect has an unknown status; refusing replay"
                      {:effect id :prior effect}))))

(defn- initialize-project-identity! [ctx project expected-owner repository]
  (let [marker-effect (str "project-identity-marker/" (:id project))
        repository-effect (str "project-identity-repository/" (:id project))
        _ (ensure-effect-reconciled! ctx marker-effect)
        _ (ensure-effect-reconciled! ctx repository-effect)
        identity (project-identity! ctx project)
        _ (when-not (and (map? identity)
                         (= (:id project) (:id identity))
                         (owner-matches? ctx expected-owner (:owner identity)))
            (rt/fail! "Created Project owner identity is not verified"
                      {:project project :identity identity :owner expected-owner}))
        marker? (str/includes? (or (:readme identity) "") (project-marker ctx))
        linked? (project-repository-linked? ctx identity)]
    (cond
      (project-identity-complete? ctx identity) identity
      (not (project-identity-compatible? ctx identity))
      (rt/fail! "Created Project has unrelated remote identity; refusing initialization"
                {:project project :identity identity})
      :else
      (do
        (when-not marker?
          (rt/effect! ctx marker-effect
                      #(let [result (graphql ctx "mutation($id:ID!,$readme:String!){updateProjectV2(input:{projectId:$id,readme:$readme}){projectV2{id readme}}}"
                                             {:id (:id project) :readme (project-marker ctx)})
                             returned-id (get-in result [:updateProjectV2 :projectV2 :id])]
                         (when-not (= (:id project) returned-id)
                           (rt/fail! "Project identity marker update returned the wrong Project"
                                     {:project project :result result}))
                         result)))
        (when-not linked?
          (when-not (and (map? repository) (string? (:node_id repository)))
            (rt/fail! "Repository identity is unavailable for Project initialization"
                      {:project project :repository repository}))
          (rt/effect! ctx repository-effect
                      #(let [result (graphql ctx "mutation($project:ID!,$repo:ID!){linkProjectV2ToRepository(input:{projectId:$project,repositoryId:$repo}){repository{id}}}"
                                             {:project (:id project) :repo (:node_id repository)})
                             returned-id (get-in result [:linkProjectV2ToRepository :repository :id])]
                         (when-not (= (:node_id repository) returned-id)
                           (rt/fail! "Project repository link returned the wrong repository"
                                     {:project project :repository repository :result result}))
                         result)))
        identity))))

(defn views! [ctx project fields]
  (let [views (get-in (graphql ctx "query($id:ID!){node(id:$id){... on ProjectV2{views(first:100){nodes{id name layout}}}}}"
                               {:id (:id project)}) [:node :views :nodes])
        visible (mapv :id (filter #(contains? #{"Title" "Status" "Milestone" "Labels" "Parent issue" "Sub-issue progress" "Linked pull requests"} (:name %)) fields))]
    (doseq [[title layout filter-text] [["All work" "TABLE_LAYOUT" "label:seedless"]
                                        ["Workflow" "BOARD_LAYOUT" "label:seedless"]
                                        ["SH milestones" "TABLE_LAYOUT" "label:seedless"]
                                        ["Attention" "TABLE_LAYOUT" "label:seedless status:Attention"]]]
      (let [view (or (first (filter #(= title (:name %)) views))
                     (when (= title "All work") (first (filter #(= "View 1" (:name %)) views)))
                     (rt/effect! ctx (str "view/" title)
                                 #(get-in (graphql ctx "mutation($project:ID!,$name:String!,$layout:ProjectV2ViewLayout!){createProjectV2View(input:{projectId:$project,name:$name,layout:$layout}){projectV2View{id}}}"
                                                   {:project (:id project) :name title :layout layout})
                                          [:createProjectV2View :projectV2View])))]
        (rt/effect! ctx (str "view-config/" title)
                    #(graphql ctx "mutation($id:ID!,$name:String!,$layout:ProjectV2ViewLayout!,$config:ProjectV2ViewConfigurationInput,$filter:String!){updateProjectV2View(input:{viewId:$id,name:$name,layout:$layout,configuration:$config,filter:$filter}){projectV2View{id}}}"
                              {:id (:id view) :name title :layout layout
                               :config {:visibleFieldIds visible} :filter filter-text}))))))

(defn setup! [ctx]
  (let [owner (transport/owner-info ctx)
        _ (ensure-effect-reconciled! ctx "create-project")
        _ (ensure-effect-reconciled! ctx "link-project")
        create-effect (get-in @(:state ctx) [:effects "create-project"])
        created-project (when create-effect
                          (when-not (= :done (:status create-effect))
                            (rt/fail! "Create Project effect is not complete; refusing recovery"
                                      {:effect create-effect}))
                          (let [project (:result create-effect)]
                            (when-not (and (map? project) (string? (:id project)))
                              (rt/fail! "Create Project effect has no exact Project identity"
                                        {:effect create-effect}))
                            project))
        available (transport/projects ctx (:id owner))
        known-id (get-in @(:state ctx) [:project :id])
        known (some #(when (= known-id (:id %)) %) available)
        matches (filter #(= (get-in ctx [:config :project-title]) (:title %)) available)
        _ (when (and known-id (nil? known)) (rt/fail! "Recorded Project is missing" {}))
        _ (when (and (nil? known) (nil? created-project) (> (count matches) 1))
            (rt/fail! "Ambiguous Project title" {}))
        [project created-by-effect?] (cond
                                       known [known false]
                                       created-project [created-project true]
                                       (seq matches) [(first matches) false]
                                       :else [(rt/effect! ctx "create-project"
                                                          #(get-in (graphql ctx "mutation($owner:ID!,$title:String!){createProjectV2(input:{ownerId:$owner,title:$title}){projectV2{id number title url}}}"
                                                                            {:owner (:id owner) :title (get-in ctx [:config :project-title])})
                                                                   [:createProjectV2 :projectV2])) true])
        repository (api ctx (repo-path ctx ""))
        _ (if created-by-effect?
            (do (initialize-project-identity! ctx project owner repository)
                ;; Re-query after both durable identity writes. Later metadata
                ;; writes must never be the first proof of Project ownership.
                (verify-project! ctx project owner))
            (verify-project! ctx project owner))
        fields (transport/fields ctx (:id project))
        status (some #(when (= "Status" (:name %)) %) fields)
        existing (set (map :name (:options status)))
        _ (when-not (or (= existing (set states)) (= existing #{"Todo" "In Progress" "Done"}))
            (rt/fail! "Refusing to replace an unrelated Status field" {:field status}))
        workflow (if (= existing (set states)) status
                     (rt/effect! ctx "configure-status"
                                 #(get-in (graphql ctx "mutation($field:ID!,$options:[ProjectV2SingleSelectFieldOptionInput!]){updateProjectV2Field(input:{fieldId:$field,singleSelectOptions:$options}){projectV2Field{... on ProjectV2SingleSelectField{id name options{id name}}}}}"
                                                   {:field (:id status)
                                                    :options (mapv (fn [name]
                                                                     (cond-> {:name name :color (case name "Done" "GREEN" "Attention" "RED" "Not planned" "GRAY" "BLUE")
                                                                              :description (str "Seedless workflow: " name)}
                                                                       (some (fn [option] (= name (:name option))) (:options status))
                                                                       (assoc :id (:id (some (fn [option] (when (= name (:name option)) option)) (:options status)))))) states)})
                                          [:updateProjectV2Field :projectV2Field])))
        _ (when-not (= (set states) (set (map :name (:options workflow))))
            (rt/fail! "Project Status update returned unexpected options" {}))]
    (rt/update! ctx assoc :project (assoc project :workflow workflow))
    (let [existing-labels (set (map :name (pages ctx (repo-path ctx "labels"))))]
      (doseq [label (concat ["seedless" "seedless:campaign"]
                            (map #(str "seedless:" %) (concat schema/workstreams schema/kinds)))
              :when (not (contains? existing-labels label))]
        (rt/effect! ctx (str "label/" label)
                    #(api ctx "POST" (repo-path ctx "labels")
                          {:name label :color "5319e7" :description "Seedless workstream or evidence scope; no completion authority"}))))
    (rt/effect! ctx "project-description"
                #(graphql ctx "mutation($id:ID!,$readme:String!){updateProjectV2(input:{projectId:$id,shortDescription:\"Replace the Clojure seed through independently verified Uniform adoption.\",readme:$readme}){projectV2{id}}}"
                          {:id (:id project)
                           :readme (str "# Uniform Seedless\n\n" (get-in ctx [:config :objective])
                                        "\n\nMilestones group SH stages. Labels show compiler/runtime/build and evidence scope. Done means a verified PR merge, not completion of an SH milestone. Existing repository roadmaps and accepted evidence own milestone status. No milestone is closed automatically.\n\nCampaign completion requires the existing self-host verifier and independent exact-revision review. An empty audit is insufficient. Group SH milestones by Milestone using GitHub's view menu.\n\n"
                                        (project-marker ctx))}))
    (views! ctx project fields)
    project))

(defn- identity-digest [ctx kind stage key]
  (rt/digest (pr-str [(get-in ctx [:config :repository])
                      (get-in ctx [:config :campaign])
                      (get-in ctx [:config :objective])
                      stage kind key])))

(defn- milestone-marker [ctx stage]
  (marker (str "milestone/" (identity-digest ctx "milestone" stage nil))))

(defn- issue-stage [body]
  (or (second (re-find #"(?m)^Stage:\s*([^;\s]+)" (or body ""))) "campaign"))

(defn- issue-marker [ctx key stage]
  (marker (str "issue/" (identity-digest ctx "issue" stage key))))

(defn milestone! [ctx stage]
  (let [title (str (get-in ctx [:config :campaign]) " / " stage)
        identity (milestone-marker ctx stage)
        effect-id (str "milestone/" stage)
        _ (ensure-effect-reconciled! ctx effect-id)
        prior (get-in @(:state ctx) [:effects effect-id])
        matches (filter #(= title (:title %)) (pages ctx (repo-path ctx "milestones?state=all")))
        exact (filter #(str/includes? (or (:description %) "") identity) matches)]
    (cond
      (and (= 1 (count matches)) (= 1 (count exact))) (first exact)
      (seq matches) (rt/fail! "Milestone title has a different or ambiguous Seedless identity"
                              {:title title :stage stage :identity identity :matches matches})
      (= :done (:status prior)) (rt/fail! "Recorded milestone effect has no verified remote identity"
                                          {:effect prior :title title :identity identity})
      :else
      (rt/effect! ctx effect-id
                  #(api ctx "POST" (repo-path ctx "milestones")
                        {:title title
                         :description (str "Work associated with this SH stage. Closed issues do not establish milestone acceptance; the existing roadmap and evidence remain authoritative.\n\n"
                                           identity)})))))

(defn issue! [ctx key title body labels milestone]
  (let [stage (issue-stage body)
        identity (issue-marker ctx key stage)
        effect-id (str "issue/" key)
        _ (ensure-effect-reconciled! ctx effect-id)
        prior (get-in @(:state ctx) [:effects effect-id])
        issues (pages ctx (repo-path ctx "issues?state=all&labels=seedless"))
        existing (some #(when (and (not (:pull_request %))
                                   (str/includes? (or (:body %) "") identity)) %)
                       issues)]
    (or existing
        (if (= :done (:status prior))
          (rt/fail! "Recorded issue effect has no verified remote identity"
                    {:effect prior :key key :identity identity})
          (rt/effect! ctx effect-id
                      #(api ctx "POST" (repo-path ctx "issues")
                            (cond-> {:title title :body (str body "\n\n" identity)
                                     :labels (into ["seedless"] labels)}
                              milestone (assoc :milestone milestone))))))))

(defn comment! [ctx issue key body]
  (let [path (repo-path ctx (str "issues/" (:number issue) "/comments"))]
    (when-not (some #(str/includes? (or (:body %) "") (marker key)) (pages ctx path))
      (rt/effect! ctx (str "comment/" (:number issue) "/" key)
                  #(api ctx "POST" path {:body (str body "\n\n" (marker key))})))))

(defn open-review-threads? [ctx number]
  (loop [after nil]
    (let [connection (get-in (graphql ctx "query($owner:String!,$repo:String!,$number:Int!,$after:String){repository(owner:$owner,name:$repo){pullRequest(number:$number){reviewThreads(first:100,after:$after){nodes{isResolved} pageInfo{hasNextPage endCursor}}}}}"
                                      (let [[owner repo] (str/split (get-in ctx [:config :repository]) #"/")]
                                        {:owner owner :repo repo :number number :after after}))
                             [:repository :pullRequest :reviewThreads])]
      (when-not connection (rt/fail! "Cannot inspect PR review threads" {:number number}))
      (cond (some #(not (:isResolved %)) (:nodes connection)) true
            (get-in connection [:pageInfo :hasNextPage]) (recur (get-in connection [:pageInfo :endCursor]))
            :else false))))
