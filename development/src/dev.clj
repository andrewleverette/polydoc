(ns dev
  (:require
   [integrant.core :as ig]
   [polydoc.config.interface :as c]
   [polydoc.logger.interface :as l]
   [polydoc.migrations.interface :as m]))

(comment
  (c/refresh)
  (def config (c/get-config [:polydoc/systems :migrations-cli]))
  config
  (def system (ig/init config))
  system

  (def migrations (:polydoc/migrations system))
  migrations

  (m/init-schema migrations)

  (ig/halt! system))
