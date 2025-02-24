(ns dev
  (:require
   [integrant.core :as ig]
   [polydoc.config.interface :as c]))

(comment
  (c/refresh)
  (def config (c/get-config [:polydoc/systems :migrations-cli]))
  config
  (def system (ig/init config))
  system

  (def migrations (:polydoc/migrations system))
  migrations

  (ig/halt! system))
