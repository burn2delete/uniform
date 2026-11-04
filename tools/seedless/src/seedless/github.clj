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

(defn verify-project! [ctx project]
  (when-not (or (= (:id project) (get-in @(:state ctx) [:project :id]))
                (= (:id project) (get-in @(:state ctx) [:effects "create-project" :result :id])))
    (let [identity (:node (graphql ctx "query($id:ID!){node(id:$id){... on ProjectV2{id readme repositories(first:100){nodes{nameWithOwner}}}}}"
                                   {:id (:id project)}))]
      (when-not (and (= (:id project) (:id identity))
                     (str/includes? (or (:readme identity) "") (project-marker ctx))
                     (some #(= (get-in ctx [:config :repository]) (:nameWithOwner %))
                           (get-in identity [:repositories :nodes])))
        (rt/fail! "Same-title Project has no verified Seedless identity" {:project project})))))

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
        available (transport/projects ctx (:id owner))
        known-id (get-in @(:state ctx) [:project :id])
        known (some #(when (= known-id (:id %)) %) available)
        matches (filter #(= (get-in ctx [:config :project-title]) (:title %)) available)
        _ (when (and known-id (nil? known)) (rt/fail! "Recorded Project is missing" {}))
        _ (when (> (count matches) 1) (rt/fail! "Ambiguous Project title" {}))
        project (or known (first matches)
                    (rt/effect! ctx "create-project"
                                #(get-in (graphql ctx "mutation($owner:ID!,$title:String!){createProjectV2(input:{ownerId:$owner,title:$title}){projectV2{id number title url}}}"
                                                  {:owner (:id owner) :title (get-in ctx [:config :project-title])})
                                         [:createProjectV2 :projectV2])))
        _ (verify-project! ctx project)
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
            (rt/fail! "Project Status update returned unexpected options" {}))
        repository (api ctx (repo-path ctx ""))]
    (rt/update! ctx assoc :project (assoc project :workflow workflow))
    (rt/effect! ctx "link-project"
                #(graphql ctx "mutation($project:ID!,$repo:ID!){linkProjectV2ToRepository(input:{projectId:$project,repositoryId:$repo}){repository{id}}}"
                          {:project (:id project) :repo (:node_id repository)}))
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

(defn milestone! [ctx stage]
  (let [title (str (get-in ctx [:config :campaign]) " / " stage)]
    (or (some #(when (= title (:title %)) %) (pages ctx (repo-path ctx "milestones?state=all")))
        (rt/effect! ctx (str "milestone/" stage)
                    #(api ctx "POST" (repo-path ctx "milestones")
                          {:title title :description "Work associated with this SH stage. Closed issues do not establish milestone acceptance; the existing roadmap and evidence remain authoritative."})))))

(defn issue! [ctx key title body labels milestone]
  (let [identity (marker (rt/digest (pr-str [(get-in ctx [:config :campaign]) key])))]
    (or (some #(when (and (not (:pull_request %)) (str/includes? (or (:body %) "") identity)) %)
              (pages ctx (repo-path ctx "issues?state=all&labels=seedless")))
        (rt/effect! ctx (str "issue/" key)
                    #(api ctx "POST" (repo-path ctx "issues")
                          (cond-> {:title title :body (str body "\n\n" identity) :labels (into ["seedless"] labels)}
                            milestone (assoc :milestone milestone)))))))

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
