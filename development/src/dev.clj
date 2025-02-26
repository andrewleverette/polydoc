(ns dev
  (:require
   [integrant.core :as ig]
   [polydoc.config.interface :as c]
   [polydoc.database.interface :as q]))

(comment
  (c/refresh)
  (def config {:polydoc/datasource nil
               :polydoc/database (ig/ref :polydoc/datasource)})
  config
  (def system (ig/init config))
  system

  (def db (:polydoc/database system))

  db

  (q/execute! db {:select [:*]
                  :from [:roles]})

  (ig/halt! system))
