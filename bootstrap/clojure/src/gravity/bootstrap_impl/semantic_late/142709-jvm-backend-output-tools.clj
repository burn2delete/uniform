; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-output-tools
 [state]
 (clojure.core/let
  [{:keys [source-path source-text output-path emit?]}
   state
   {:keys [output parent]}
   (when emit? (jvm-backend-preflight-output! (str output-path) source-path))
   javac-version
   (jvm-backend-tool-version! *jvm-backend-javac-command* source-path)
   java-version
   (jvm-backend-tool-version! *jvm-backend-java-command* source-path)]
  (clojure.core/assoc
   state
   :output
   output
   :parent
   parent
   :javac-version
   javac-version
   :java-version
   java-version)))
