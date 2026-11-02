(ns gravity.build.package-selection-fixtures
  "Shared supplied package-selection data for package selection tests.

  The records describe deterministic, non-admitting package metadata.  They
  are test inputs only and have no package or artifact authority.")

(def package-record
  {:package "gravity/http" :version "2.1.4" :registry :public
   :content-hash "sha256:http-214" :provenance "prov:http"
   :capabilities [:http/client] :effects [:network/http]
   :safety :reviewed :unsafe-islands [] :features [] :dependencies []
   :variants [{:profile :hosted :target :jvm-21 :artifact "http:jvm"}]
   :yank-status :active :revocation-status :clear
   :signature-status :unverified :provenance-status :unverified})

(def constraint-record
  {:package "gravity/http" :version "2.1.4" :chain ["app" "gravity/http"]})

(def request
  {:constraints [constraint-record] :candidates [package-record]
   :locked-records [] :mode :select :unsafe-policy :deny
   :allowed-effects #{:network/http} :allowed-capabilities #{:http/client}
   :context {:registry :public :visibility :public :registry-grants {}
             :profile :hosted :target :jvm-21
             :policy {:id :standard :yanked :reject :revoked :reject}}})
