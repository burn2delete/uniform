(ns gravity.build.package-policy-fixtures
  "Shared supplied package-policy data for the package-policy and package-pipeline tests.

  These values are intentionally synthetic, non-admitting inputs.  Keeping them
  in a normal support namespace lets consumers use the same records without
  loading a test namespace or reaching through private vars.")

;; Synthetic facts are deliberately non-admitting, including :verified labels.
(def record
  {:package "gravity/http" :version "2.1.4" :registry :gravity-public
   :content-hash "sha256:http-214" :provenance "prov:gravity-http"
   :capabilities [:http/client] :safety :reviewed
   :yank-status :active :revocation-status :clear
   :signature-status :verified :provenance-status :verified})

(def request
  {:record record :locked-records [record] :mode :select
   :registry :gravity-public :visibility :public :registry-grants {}
   :requested-range "^2.1.0" :constraint-chain ["app -> gravity/http ^2.1.0"]
   :profile :hosted :target :jvm-21
   :policy {:id :release :yanked :reject :revoked :reject}})
