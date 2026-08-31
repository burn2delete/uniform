(def ^:private semantic-llvm-authority-holder
  (let [finalization-token (Object.)
        tool-observation-state (atom {:total 0 :steps {}})]
    {:finalization-token finalization-token
     :tool-observation-state tool-observation-state}))

(def ^:private p15-s23-b3-llvm-finalization-token
  (:finalization-token semantic-llvm-authority-holder))

(def ^:private p15-s23-b3-llvm-tool-observation-state
  (:tool-observation-state semantic-llvm-authority-holder))
