;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.srm-api.core
  "Search & Retrieval Module (SRM) API for PolyDoc."
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [polydoc.config.interface :as c]
   [polydoc.logger.interface :as l]
   [polydoc.webserver.interface :as webserver]
   [polydoc.srm-api.handler :as h]))

(def srm-api-component-key :polydoc/srm-api)

(defn start-api
  [app]
  (let [handler (h/create-handler app)
        webserver (webserver/create-webserver :jetty handler (:webconfig app))]
    (webserver/start webserver)))

(defmethod ig/init-key srm-api-component-key [_ app]
  (l/info "Starting SRM API")
  (start-api app))

(defmethod ig/halt-key! srm-api-component-key [_ srm-api]
  (l/info "Shutting down SRM API")
  (webserver/stop srm-api))

(defn -main
  [& args]
  (->> [:polydoc/systems :srm-api]
       c/get-config
       ig/init))
