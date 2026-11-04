(ns simplify.github
  "GitHub Projects v2 plus repository issues/milestones. Uses the existing gh login."
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [simplify.runtime :as rt]))

(def workflow-states ["Proposed" "Ready" "Implementing" "Review" "CI" "Merge queue" "Done" "Attention" "Not planned"])
(def labels
  [["simplify" "5319e7" "Managed simplification campaign"]
   ["simplify:campaign" "1d76db" "Standing objective and audit history"]
   ["simplify:redundant-work" "c5def5" "Avoid unnecessary repeated execution"]
   ["simplify:abstraction" "bfdadc" "Remove unjustified abstraction"]
   ["simplify:duplication" "d4c5f9" "Consolidate equivalent implementation"]
   ["simplify:obsolete" "ededed" "Remove unused or superseded machinery"]])

(defn- graphql-errors-from-output [endpoint output]
  (when (= "graphql" endpoint)
    (try
      (let [payload (json/read-str output :key-fn keyword)]
        (when (and (map? payload) (vector? (:errors payload)))
          (:errors payload)))
      (catch Exception _ nil))))

(defn api
  ([ctx endpoint] (api ctx "GET" endpoint nil))
  ([ctx method endpoint body]
   (let [key (rt/digest (pr-str [method endpoint]))
         _ (when (or (get-in @(:state ctx) [:restricted-actions key])
                     (rt/shared-restriction ctx :api key))
             (rt/fail! "Restricted GitHub action cannot be retried" {:endpoint endpoint :restricted true}))
         result (rt/command ctx (get-in ctx [:config :repo-dir])
                            (cond-> ["gh" "api" "--method" method endpoint]
                              body (into ["--input" "-"]))
                            (when body (json/write-str body)))]
     (when-not (zero? (:exit result))
       (let [restricted (rt/authorization-denied? (str (:err result) "\n" (:out result)))
             errors (graphql-errors-from-output endpoint (:out result))]
         (when restricted
           (let [refusal {:method method :endpoint endpoint :error (:err result)}]
             (rt/update! ctx assoc-in [:restricted-actions key] refusal)
             (rt/shared-restrict! ctx :api key refusal)))
         (rt/fail! "GitHub API failed; no automatic mutation retry"
                   (cond-> {:endpoint endpoint :exit (:exit result) :error (:err result) :restricted restricted}
                     (seq errors) (assoc :errors errors)))))
     (when-not (str/blank? (:out result)) (json/read-str (:out result) :key-fn keyword)))))

(defn graphql [ctx query variables]
  (let [response (api ctx "POST" "graphql" {:query query :variables variables})]
    (when (seq (:errors response))
      (let [restricted (boolean (some #(or (#{"FORBIDDEN" "UNAUTHORIZED" "RATE_LIMITED"} (:type %))
                                           (re-find #"(?i)(resource not accessible|requires authentication|bad credentials|rate limit|forbidden)"
                                                    (or (:message %) ""))) (:errors response)))]
        (when restricted
          (let [key (rt/digest (pr-str ["POST" "graphql"]))
                refusal {:method "POST" :endpoint "graphql" :errors (:errors response)}]
            (rt/update! ctx assoc-in [:restricted-actions key] refusal)
            (rt/shared-restrict! ctx :api key refusal)))
        (rt/fail! "GitHub GraphQL failed" {:errors (:errors response) :restricted restricted})))
    (:data response)))

(defn pages [ctx endpoint]
  (loop [page 1 result []]
    (let [items (api ctx (str endpoint (if (str/includes? endpoint "?") "&" "?") "per_page=100&page=" page))]
      (if (empty? items) result (recur (inc page) (into result items))))))

(defn repo-path [ctx suffix] (str "repos/" (get-in ctx [:config :repository]) (when (seq suffix) (str "/" suffix))))

(defn requested-review-feedback [ctx candidate number]
  (let [reviews (pages ctx (repo-path ctx (str "pulls/" number "/reviews")))
        latest (vals (reduce (fn [by-user review]
                               (if (#{"APPROVED" "CHANGES_REQUESTED" "DISMISSED"} (:state review))
                                 (let [reviewer (or (get-in review [:user :id]) (get-in review [:user :login]))]
                                   (when-not reviewer (rt/fail! "Review has no observed reviewer identity" {:review review}))
                                   (assoc by-user reviewer review))
                                 by-user)) {} (sort-by :id reviews)))
        current (vec (filter #(and (= "CHANGES_REQUESTED" (:state %)) (= candidate (:commit_id %))) latest))]
    (when (seq current)
      (let [ids (set (map :id current))]
        {:github-reviews current
         :inline-comments (vec (filter #(contains? ids (:pull_request_review_id %))
                                       (pages ctx (repo-path ctx (str "pulls/" number "/comments")))))}))))

(defn head-workflow-state [ctx candidate]
  (let [runs (loop [page 1 result []]
               (let [response (api ctx (repo-path ctx (str "actions/runs?head_sha=" candidate "&per_page=100&page=" page)))
                     batch (:workflow_runs response) total (:total_count response)]
                 (when-not (and (vector? batch) (nat-int? total))
                   (rt/fail! "Current-head workflow evidence is unavailable" {:candidate candidate}))
                 (let [result (into result batch)]
                   (if (< (count result) total)
                     (do (when (empty? batch) (rt/fail! "Current-head workflow pagination did not advance" {:candidate candidate}))
                         (recur (inc page) result))
                     result))))
        runs (filter #(= candidate (:head_sha %)) runs)
        latest (mapv #(last (sort-by :id %)) (vals (group-by (juxt :workflow_id :event) runs)))
        required (get-in ctx [:config :required-workflows] [".github/workflows/ci.yml"])
        required-runs (filter #(contains? (set required) (:path %)) latest)
        failure? #(and (= "completed" (:status %))
                       (not (#{"success" "skipped" "neutral"} (:conclusion %))))
        state (cond
                (or (some failure? latest)
                    (some #(and (= "completed" (:status %)) (not= "success" (:conclusion %))) required-runs)) :failed
                (or (empty? required) (not-every? (set (map :path required-runs)) required)
                    (some #(not= "completed" (:status %)) latest)) :pending
                :else :passed)]
    {:state state :runs (mapv #(select-keys % [:id :workflow_id :event :path :head_sha :status :conclusion :run_attempt]) latest)}))

(defn review-threads-clear? [ctx pr-number]
  (let [[owner repository] (str/split (get-in ctx [:config :repository]) #"/")]
    (loop [cursor nil]
      (let [connection (get-in (graphql ctx "query($owner:String!,$name:String!,$number:Int!,$after:String){repository(owner:$owner,name:$name){pullRequest(number:$number){reviewThreads(first:100,after:$after){nodes{isResolved}pageInfo{hasNextPage endCursor}}}}}"
                                       {:owner owner :name repository :number pr-number :after cursor})
                               [:repository :pullRequest :reviewThreads])]
        (when-not (and (vector? (:nodes connection)) (boolean? (get-in connection [:pageInfo :hasNextPage])))
          (rt/fail! "PR review-thread evidence is unavailable" {:pr pr-number}))
        (cond
          (not-every? #(true? (:isResolved %)) (:nodes connection)) false
          (get-in connection [:pageInfo :hasNextPage])
          (let [next-cursor (get-in connection [:pageInfo :endCursor])]
            (when (or (nil? next-cursor) (= cursor next-cursor))
              (rt/fail! "PR review-thread cursor did not advance" {:pr pr-number}))
            (recur next-cursor))
          :else true)))))

(defn owner-info [ctx]
  (get (graphql ctx "query($login:String!){repositoryOwner(login:$login){id __typename}}"
                {:login (first (str/split (get-in ctx [:config :repository]) #"/"))}) :repositoryOwner))

(defn projects [ctx owner-id]
  (loop [cursor nil result []]
    (let [connection (get-in (graphql ctx "query($id:ID!,$after:String){node(id:$id){... on User{projectsV2(first:100,after:$after){nodes{id number title url} pageInfo{hasNextPage endCursor}}} ... on Organization{projectsV2(first:100,after:$after){nodes{id number title url} pageInfo{hasNextPage endCursor}}}}}"
                                     {:id owner-id :after cursor}) [:node :projectsV2])]
      (when-not connection (rt/fail! "Cannot list GitHub Projects for this owner" {}))
      (if (get-in connection [:pageInfo :hasNextPage])
        (recur (get-in connection [:pageInfo :endCursor]) (into result (:nodes connection)))
        (into result (:nodes connection))))))

(defn fields [ctx project-id]
  (get-in (graphql ctx "query($id:ID!){node(id:$id){... on ProjectV2{fields(first:100){nodes{... on ProjectV2Field{id name} ... on ProjectV2SingleSelectField{id name options{id name}}}}}}}"
                   {:id project-id}) [:node :fields :nodes]))

(defn views! [ctx project fs]
  (let [existing (get-in (graphql ctx "query($id:ID!){node(id:$id){... on ProjectV2{views(first:100){nodes{id name number layout filter}}}}}"
                                  {:id (:id project)}) [:node :views :nodes])
        visible (mapv :id (filter #(contains? #{"Title" "Status" "Milestone" "Labels" "Parent issue" "Sub-issue progress" "Linked pull requests"} (:name %)) fs))]
    (doseq [[name layout query-filter] (get-in ctx [:config :project-views] [["All work" "TABLE_LAYOUT" "label:simplify"]
                                 ["Workflow" "BOARD_LAYOUT" "label:simplify"]
                                 ["Audit rounds" "TABLE_LAYOUT" "label:simplify"]
                                 ["Attention" "TABLE_LAYOUT" "label:simplify status:Attention"]])]
      (let [view (or (first (filter #(= name (:name %)) existing))
                     (when (= name "All work") (first (filter #(= "View 1" (:name %)) existing)))
                     (rt/effect! ctx (str "view/" name)
                                 #(get-in (graphql ctx "mutation($project:ID!,$name:String!,$layout:ProjectV2ViewLayout!,$config:ProjectV2ViewConfigurationInput){createProjectV2View(input:{projectId:$project,name:$name,layout:$layout,configuration:$config}){projectV2View{id name number layout filter}}}"
                                                   {:project (:id project) :name name :layout layout :config {:visibleFieldIds visible}})
                                          [:createProjectV2View :projectV2View])))]
        (rt/effect! ctx (str "view-config/" name)
                    #(graphql ctx "mutation($id:ID!,$name:String!,$layout:ProjectV2ViewLayout!,$config:ProjectV2ViewConfigurationInput,$filter:String!){updateProjectV2View(input:{viewId:$id,name:$name,layout:$layout,configuration:$config,filter:$filter}){projectV2View{id}}}"
                              {:id (:id view) :name name :layout layout :config {:visibleFieldIds visible} :filter query-filter}))
        (rt/update! ctx assoc-in [:project :views name] (assoc view :name name :layout layout :filter query-filter))))))

(defn project-marker [ctx]
  (str "<!-- uniform-" (get-in ctx [:config :harness-id] "simplify") "-project:" (get-in ctx [:config :repository]) " -->"))

(defn verify-project! [ctx project]
  (when-not (or (= (:id project) (get-in @(:state ctx) [:project :id]))
                (= (:id project) (get-in @(:state ctx) [:effects "create-project" :result :id])))
    (let [identity (:node (graphql ctx "query($id:ID!){node(id:$id){... on ProjectV2{id readme repositories(first:100){nodes{nameWithOwner}}}}}"
                                   {:id (:id project)}))]
      (when-not (and (= (:id project) (:id identity))
                     (str/includes? (or (:readme identity) "") (project-marker ctx))
                     (some #(= (get-in ctx [:config :repository]) (:nameWithOwner %)) (get-in identity [:repositories :nodes])))
        (rt/fail! "Same-title Project has no verified repository identity; refusing to adopt it" {:project (:id project)})))))

(defn setup! [ctx]
  (let [owner (owner-info ctx)
        title (get-in ctx [:config :project-title])
        available (projects ctx (:id owner))
        known-id (get-in @(:state ctx) [:project :id])
        known (first (filter #(= known-id (:id %)) available))
        _ (when (and known-id (not known)) (rt/fail! "Recorded Project is missing; refusing to create a replacement" {:project known-id}))
        matches (filter #(= title (:title %)) available)
        _ (when (> (count matches) 1) (rt/fail! "Multiple projects share the configured title" {:title title}))
        project (or known (first matches)
                    (rt/effect! ctx "create-project"
                                #(get-in (graphql ctx "mutation($owner:ID!,$title:String!){createProjectV2(input:{ownerId:$owner,title:$title}){projectV2{id number title url}}}"
                                                  {:owner (:id owner) :title title}) [:createProjectV2 :projectV2])))
        _ (verify-project! ctx project)
        fs (fields ctx (:id project))
        status-field (first (filter #(= "Status" (:name %)) fs))
        _ (when-not (or (= (set workflow-states) (set (map :name (:options status-field))))
                        (= #{"Todo" "In Progress" "Done"} (set (map :name (:options status-field)))))
            (rt/fail! "Existing Status field is not a default or Simplify field; refusing to overwrite" {:field status-field}))
        workflow (if (= (set workflow-states) (set (map :name (:options status-field)))) status-field
                     (rt/effect! ctx "configure-status-field"
                                 (fn [] (get-in (graphql ctx "mutation($field:ID!,$options:[ProjectV2SingleSelectFieldOptionInput!]){updateProjectV2Field(input:{fieldId:$field,singleSelectOptions:$options}){projectV2Field{... on ProjectV2SingleSelectField{id name options{id name}}}}}"
                                                   {:field (:id status-field)
                                                    :options (mapv (fn [n] (cond-> {:name n :color (case n "Done" "GREEN" "Attention" "RED" "Not planned" "GRAY" "BLUE") :description (str "Simplify workflow: " n)}
                                                                           (some #(= n (:name %)) (:options status-field))
                                                                           (assoc :id (:id (first (filter #(= n (:name %)) (:options status-field))))))) workflow-states)})
                                          [:updateProjectV2Field :projectV2Field]))))
        _ (when-not (= (set workflow-states) (set (map :name (:options workflow))))
            (rt/fail! "Existing Status field options differ; refusing to overwrite" {:field workflow}))
        repository (api ctx (repo-path ctx ""))]
    (rt/update! ctx assoc :project (assoc project :workflow workflow))
    (views! ctx project fs)
    (rt/effect! ctx "link-project"
                #(graphql ctx "mutation($project:ID!,$repo:ID!){linkProjectV2ToRepository(input:{projectId:$project,repositoryId:$repo}){repository{id}}}"
                          {:project (:id project) :repo (:node_id repository)}))
    (let [existing (set (map :name (pages ctx (repo-path ctx "labels"))))]
      (doseq [[name color description] (get-in ctx [:config :labels] labels) :when (not (contains? existing name))]
        (rt/effect! ctx (str "label/" name) #(api ctx "POST" (repo-path ctx "labels") {:name name :color color :description description}))))
    (rt/effect! ctx "project-description"
                #(graphql ctx "mutation($id:ID!,$readme:String!,$description:String!){updateProjectV2(input:{projectId:$id,shortDescription:$description,readme:$readme}){projectV2{id}}}"
                          {:id (:id project)
                           :description (get-in ctx [:config :project-description] "Evidence-backed simplification: audit, implement, review, merge, repeat.")
                           :readme (str (get-in ctx [:config :project-readme]
                                         "# Uniform Simplify\n\nOne milestone per audit round; one issue per independently mergeable improvement. Campaign issue is the parent. Checklists describe execution steps; sub-issues are independent deliverables. Labels classify work; Status records execution state.\n\nWorkflow is a board; Audit rounds is a table showing Milestone; Attention filters Status = Attention. Show Parent issue, Sub-issue progress, Linked pull requests, Labels, and Milestone. In the Audit rounds view, group by Milestone using the view menu; the current API does not expose grouping configuration.\n\nOnly the coordinator merges. Done means actual merge verified, not agent completion or green CI. A round closes when every finding has a disposition. Campaign completion requires a fresh audit of main with no new justified findings.\n\nNo language, release, self-hosting, or seed-retirement claims are conferred by this project.\n\n") (project-marker ctx))}))
    (rt/event! ctx :project-ready {:project project})
    project))

(defn ensure-milestone! [ctx round]
  (let [title (str (get-in ctx [:config :campaign]) " / round " round)]
    (or (first (filter #(= title (:title %)) (pages ctx (repo-path ctx "milestones?state=all"))))
        (rt/effect! ctx (str "milestone/" round)
                    #(api ctx "POST" (repo-path ctx "milestones")
                          {:title title :description (str "Audit round " round ". Base commit and coverage are recorded on the campaign issue. Close after all findings have a disposition.")})))))

(defn marker
  ([key] (marker {} key))
  ([ctx key] (str "<!-- uniform-" (get-in ctx [:config :harness-id] "simplify") ":" key " -->")))
(defn ensure-issue! [ctx key title body milestone category]
  (let [cache (:issue-cache ctx)
        issues (or (when cache @cache)
                   (let [items (pages ctx (repo-path ctx (str "issues?state=all&labels=" (get-in ctx [:config :work-label] "simplify"))))]
                     (when cache (reset! cache items)) items))
        existing (first (filter #(and (not (:pull_request %)) (str/includes? (or (:body %) "") (marker ctx key))) issues))
        issue (or existing (rt/effect! ctx (str "issue/" key)
                  #(api ctx "POST" (repo-path ctx "issues")
                        (cond-> {:title title :body (str body "\n\n" (marker ctx key))
                                 :labels [(get-in ctx [:config :work-label] "simplify") category]}
                          milestone (assoc :milestone milestone)))))]
    (when (and cache (not existing)) (swap! cache conj issue))
    issue))

(defn ensure-finding-issue! [ctx key title body milestone category]
  ;; Existing state keeps legacy issue identities on resume. New lookups never
  ;; adopt another campaign's issue merely because a finding key is reused.
  (or (get-in @(:state ctx) [:tasks key :issue])
      (ensure-issue! ctx (str "finding/" (rt/digest (pr-str [(get-in ctx [:config :campaign]) key])))
                     title body milestone category)))

(defn existing-item [ctx project content]
  (loop [cursor nil]
    (let [connection (get-in (graphql ctx "query($project:ID!,$after:String){node(id:$project){... on ProjectV2{items(first:100,after:$after){nodes{id content{... on Issue{id}... on PullRequest{id}}}pageInfo{hasNextPage endCursor}}}}}"
                                     {:project project :after cursor}) [:node :items])
          found (first (filter #(= content (get-in % [:content :id])) (:nodes connection)))]
      (when-not (and (vector? (:nodes connection)) (boolean? (get-in connection [:pageInfo :hasNextPage])))
        (rt/fail! "Project membership evidence is unavailable" {:project project :content content}))
      (cond
        (:id found) (:id found)
        (get-in connection [:pageInfo :hasNextPage])
        (let [next-cursor (get-in connection [:pageInfo :endCursor])]
          (when (or (nil? next-cursor) (= cursor next-cursor))
            (rt/fail! "Project membership cursor did not advance" {:project project}))
          (recur next-cursor))
        :else nil))))

(defn- duplicate-project-item-error? [error]
  (let [{:keys [errors restricted]} (ex-data error)]
    (and (false? restricted)
         (= 1 (count errors))
         (= {:type "UNPROCESSABLE"
             :message "Content already exists in this project"}
            (select-keys (first errors) [:type :message])))))

(defn- add-item! [ctx project content]
  (try
    (get-in (graphql ctx "mutation($project:ID!,$content:ID!){addProjectV2ItemById(input:{projectId:$project,contentId:$content}){item{id}}}"
                     {:project project :content content})
            [:addProjectV2ItemById :item :id])
    (catch Exception error
      (if (duplicate-project-item-error? error)
        (or (existing-item ctx project content)
            (rt/fail! "Duplicate Project membership has no verified readback"
                      {:project project :content content :duplicate true}))
        (throw error)))))

(defn item! [ctx issue]
  ;; Sub-issue admission can add membership independently of this controller.
  (rt/effect! ctx (str "project-item/" (get-in @(:state ctx) [:project :id]) "/" (:node_id issue))
              #(or (existing-item ctx (get-in @(:state ctx) [:project :id]) (:node_id issue))
                   (add-item! ctx (get-in @(:state ctx) [:project :id]) (:node_id issue)))))

(defn status! [ctx issue status]
  (let [project (:project @(:state ctx))
        option (first (filter #(= status (:name %)) (get-in project [:workflow :options])))]
    (when-not (:id option) (rt/fail! "Unknown workflow status" {:status status}))
    (rt/tracking-effect! ctx (str "project-status/" (:id project) "/" (:node_id issue))
                         #(graphql ctx "mutation($project:ID!,$item:ID!,$field:ID!,$option:String!){updateProjectV2ItemFieldValue(input:{projectId:$project,itemId:$item,fieldId:$field,value:{singleSelectOptionId:$option}}){projectV2Item{id}}}"
                                   {:project (:id project) :item (item! ctx issue) :field (get-in project [:workflow :id]) :option (:id option)}))))

(defn relation! [ctx issue endpoint id]
  (let [path (repo-path ctx (str "issues/" (:number issue) "/" endpoint))]
    (when-not (some #(= id (:id %)) (pages ctx path))
      (rt/effect! ctx (str endpoint "/" (:number issue) "/" id)
                  #(api ctx "POST" path (if (= endpoint "sub_issues") {:sub_issue_id id} {:issue_id id}))))))

(defn comment! [ctx issue key body]
  (let [path (repo-path ctx (str "issues/" (:number issue) "/comments"))]
    (when-not (some #(str/includes? (:body %) (marker ctx key)) (pages ctx path))
      (rt/effect! ctx (str "comment/" (:number issue) "/" key)
                  #(api ctx "POST" path {:body (str body "\n\n" (marker ctx key))})))))

(defn draft! [ctx pr draft? candidate]
  (rt/effect! ctx (str (if draft? "draft/" "ready/") candidate)
    #(graphql ctx (if draft?
                    "mutation($id:ID!){convertPullRequestToDraft(input:{pullRequestId:$id}){pullRequest{id}}}"
                    "mutation($id:ID!){markPullRequestReadyForReview(input:{pullRequestId:$id}){pullRequest{id}}}")
              {:id (:node_id pr)})))
