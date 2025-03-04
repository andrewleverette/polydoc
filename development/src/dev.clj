(ns dev
  (:require
   [integrant.core :as ig]
   [polydoc.config.interface :as c]))

(comment
  (c/refresh)
  (def config (c/get-config [:polydoc/systems :srm-api]))
  config
  (def system (ig/init config))
  system

  (ig/halt! system))
