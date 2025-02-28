;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.datasource.core
  "Component for managing database datasources.

  This component is responsible for the lifecycle management of database
  connections within the PolyDoc application. It utilizes Integrant to
  handle the initialization and shutdown of a database datasource."
  (:require
   [integrant.core :as ig]
   [next.jdbc :as jdbc]
   [polydoc.config.interface :as c]
   [polydoc.logger.interface :as l]))

;; Component key
(def datasource-component-key :polydoc/datasource)

;; Datasource protocol
;;
;; This protocol defines the interface for the datasource component.
(defprotocol Datasource
  "Interface for a database datasource component.

  Defines the contract for datasource components, outlining the operations
  for managing the datasource lifecycle and accessing the underlying
  datasource."
  (init [this] "Initializes the datasource component.")
  (get-datasource [this] "Returns the underlying database datasource.")
  (stop [this] "Stops (halts) the datasource."))

;; JDBC Datasource adapter
;;
;; This adapter uses `next.jdbc` to create and manage a database datasource.
(defrecord JdbcDatasource [config datasource]
  Datasource
  (init [this]
    (if (nil? datasource)
      (let [ds (jdbc/get-datasource config)]
        (assoc this :datasource ds))
      this))
  (get-datasource [_] datasource)
  (stop [this]
    (assoc this :datasource nil)))

(defn create-datasource
  "Factory function to create a JDBC datasource based on the provided config."
  [config]
  (->JdbcDatasource config nil))

(defmethod ig/init-key datasource-component-key [_ _]
  (l/info "Initializing datasource component")
  (try
    (->> :polydoc/dbconfig
         c/get-config
         create-datasource
         init)
    (catch Exception e
      (l/error "Failed to initialize datasource component" e))))

(defmethod ig/halt-key! datasource-component-key [_ ds]
  (l/info "Shutting down datasource component")
  (stop ds))
