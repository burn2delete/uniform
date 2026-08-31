

(defn p18-t06-diagnostic-record
  [id fixture candidate facts]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p18-t06-final-release
   :fixture fixture
   :source-span {:source p18-t06-release-binary-path}
   :message (get p18-t06-diagnostic-messages id)
   :release-binary-path p18-t06-release-binary-path
   :release-boundary-path p18-t06-release-boundary-path
   :candidate-summary
   (select-keys candidate
                [:target :status :final-release? :clojure-seed-boundary?])
   :facts facts
   :remediation :repair_p18_t06_final_release_evidence})

(defn p18-t06-hmac-sha256
  [key payload]
  (let [mac (javax.crypto.Mac/getInstance "HmacSHA256")
        secret (javax.crypto.spec.SecretKeySpec.
                (.getBytes key "UTF-8")
                "HmacSHA256")
        bytes (.getBytes payload "UTF-8")]
    (.init mac secret)
    (apply str
           (map #(format "%02x" (bit-and % 0xff))
                (.doFinal mac bytes)))))