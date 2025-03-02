(ns dev
  (:require
   [integrant.core :as ig]
   [polydoc.config.interface :as c]
   [polydoc.documents.interface :as docs]))

(comment
  (c/refresh)
  (def config {:polydoc/datasource nil
               :polydoc/database (ig/ref :polydoc/datasource)})
  config
  (def system (ig/init config))
  system

  (def dbi (:polydoc/database system))

  (def docs-repo (docs/create-document-repository :sql dbi))

  ; (def grouped-results (docs/get-document-by-id docs-repo "1a2b3c4d-e5f6-4789-90ab-cdef01234567"))

  (docs/search-documents docs-repo {:file-type "pdf"})

  (ig/halt! system))
