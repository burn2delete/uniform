
(defn sh06-c6-lowering-from-resolution-artifact
  [resolution-artifact]
  (let [source-path (or (get-in resolution-artifact
                                [:provenance :source-path])
                        "<sh06-c6-resolution-input>")
        report (sh06-resolution-artifact-verification resolution-artifact)]
    (when-not (and (= :gravity/sh06-resolution-artifact
                      (:kind resolution-artifact))
                   (= :passed (:status report)))
      (c6-lowering-fail!
       "C6-VERIFY" source-path {:stage :core-lowering}
       {:missing-fields [:fresh-authenticated-sh06-resolution]}))
    (binding [*compiler-c6-authenticated-resolution-input*
              resolution-artifact]
      (compiler-c6-lowering-source-artifact source-path ""))))