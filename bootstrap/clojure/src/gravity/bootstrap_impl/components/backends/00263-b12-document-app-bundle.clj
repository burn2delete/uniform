

(def b12-document-app-bundle
  (str
   "{:bundle-id \"org.gravity.stage0\"\n"
   " :platform :ios\n"
   " :architecture :arm64\n"
   " :entrypoint :GravityStage0App\n"
   " :ui-bridge :swiftui\n"
   " :status :complete}\n"))

(def b12-document-permission-manifest-fixture
  (str
   "{:permissions [{:name :network\n"
   "                :capability :network/request\n"
   "                :runtime-request :declared\n"
   "                :denial-policy :return-error}\n"
   "               {:name :notifications\n"
   "                :capability :notification/send\n"
   "                :runtime-request :declared\n"
   "                :denial-policy :disable-feature}]\n"
   " :hidden-background-work :rejected\n"
   " :status :complete}\n"))

(defn b12-document-bundle-structurally-valid?
  [text]
  (and (str/includes? text "org.gravity.stage0")
       (str/includes? text ":platform :ios")
       (str/includes? text ":architecture :arm64")
       (str/includes? text ":entrypoint :GravityStage0App")))

(defn b12-document-permission-structurally-valid?
  [text]
  (and (str/includes? text ":capability :network/request")
       (str/includes? text ":runtime-request :declared")
       (str/includes? text ":denial-policy :return-error")
       (str/includes? text ":hidden-background-work :rejected")))

(defn b12-document-diagnostic-stream
  [source-path input-id]
  (gravity.backend.mobile/diagnostic-stream
   source-path input-id
   {:source-span (fn [path index] (source-span path index))
    :diagnostic-ids (fn [] b12-document-diagnostic-ids)
    :domain-anchor (fn [id] (b12-document-domain-anchor id))
    :missing-policy (fn [id] (b12-document-missing-policy id))}))
