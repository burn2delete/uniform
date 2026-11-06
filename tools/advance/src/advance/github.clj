(ns advance.github
  "Implementation Project controls; repository task files retain outcome authority."
  (:require [simplify.github :as gh]
            [simplify.runtime :as rt]))

(def controls {"Priority" ["Highest" "High" "Normal" "Low"]
               "Control" ["Active" "Paused" "Blocked"]})

(defn open-prs [ctx]
  (mapv #(select-keys % [:number :html_url :title :body :draft :head :base])
        (gh/pages ctx (gh/repo-path ctx "pulls?state=open"))))

(defn candidate-reference [reference]
  (cond-> reference
    (:repo reference) (update :repo select-keys [:id :full_name :html_url :default_branch :fork])
    (:user reference) (update :user select-keys [:id :login :html_url :type])))

(defn candidate-files [ctx pulls]
  (let [raw (mapv (fn [pull]
                    (assoc pull :files (mapv #(select-keys % [:filename :previous_filename :status])
                                            (gh/pages ctx (gh/repo-path ctx (str "pulls/" (:number pull) "/files"))))))
                  pulls)
        path (str (:state-dir ctx) "/provider-input-" (java.util.UUID/randomUUID) ".edn")
        reference {:path path :sha256 (rt/digest (pr-str raw))}]
    ;; Preserve the full provider context outside the bounded API message.
    ;; Selection still receives every candidate, full body, ref and file scope.
    (rt/write-edn! path raw)
    (mapv #(cond-> (assoc % :raw-provider-context reference)
             (contains? % :head) (update :head candidate-reference)
             (contains? % :base) (update :base candidate-reference)) raw)))

(defn setup! [ctx]
  (gh/setup! ctx)
  (let [project (get-in @(:state ctx) [:project :id])
        existing (gh/fields ctx project)]
    (doseq [[title options] controls]
      (let [field (first (filter #(= title (:name %)) existing))]
        (if field
          (when-not (= (set options) (set (map :name (:options field))))
            (rt/fail! "Project control field has unexpected options" {:field field}))
          (rt/effect! ctx (str "control-field/" title)
            #(gh/graphql ctx "mutation($project:ID!,$name:String!,$options:[ProjectV2SingleSelectFieldOptionInput!]){createProjectV2Field(input:{projectId:$project,dataType:SINGLE_SELECT,name:$name,singleSelectOptions:$options}){projectV2Field{... on ProjectV2SingleSelectField{id name options{id name}}}}}"
                         {:project project :name title
                          :options (mapv (fn [option] {:name option :color "BLUE" :description option}) options)})))))))

(defn settings [ctx]
  (loop [cursor nil result {}]
    (let [page (get-in (gh/graphql ctx
                        "query($project:ID!,$after:String){node(id:$project){... on ProjectV2{items(first:100,after:$after){nodes{content{... on Issue{number}} fieldValues(first:100){nodes{... on ProjectV2ItemFieldSingleSelectValue{name field{... on ProjectV2SingleSelectField{name}}}}}} pageInfo{hasNextPage endCursor}}}}}"
                        {:project (get-in @(:state ctx) [:project :id]) :after cursor}) [:node :items])]
      (when-not page (rt/fail! "Project controls could not be read" {}))
      (let [result (reduce (fn [m item]
                             (if-let [number (get-in item [:content :number])]
                               (assoc m number (into {} (keep (fn [field]
                                                               (when-let [name (when (contains? controls (get-in field [:field :name]))
                                                                                (get-in field [:field :name]))]
                                                                 [name (:name field)]))
                                                             (get-in item [:fieldValues :nodes])))) m)) result (:nodes page))]
        (if (get-in page [:pageInfo :hasNextPage])
          (recur (get-in page [:pageInfo :endCursor]) result) result)))))

(defn runnable? [settings number]
  (not (#{"Paused" "Blocked"} (get-in settings [number "Control"]))))

(defn priority [settings number]
  (get {"Highest" 0 "High" 1 "Normal" 2 "Low" 3} (get-in settings [number "Priority"]) 2))
