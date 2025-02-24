;; Copyright (C) 2025 Andrew Leverette

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program. If not, see <https://www.gnu.org/licenses/>.

(ns polydoc.migrations.core
  "Component for managing database migrations using Migratus.

  This component provides an interface for applying, rolling back, and
  managing database schema migrations for the PolyDoc application. It
  wraps the Migratus library and uses Integratant to manage the lifecycle."
  (:require
   [integrant.core :as ig]
   [migratus.core :as migratus]
   [polydoc.config.interface :as c]
   [polydoc.logger.interface :as l]
   [polydoc.datasource.interface :as ds]))

(def migrations-component-key :polydoc/migrations)

(defprotocol Migrations
  "Interface for the database migrations component. Defines the operations
  supported for managing database schema changes."
  (create! [this name] "Creates the up/down migration files with the current timestamp and given name.")
  (init-schema! [this] "Applies the initial database schema.")
  (migrate! [this] "Runs pending database migrations.")
  (pending [this]  "Lists pending migrations.")
  (rollback! [this] "Rolls back the last applied database migration."))
  ;
(defrecord MigratusMigrations [config]
  Migrations
  (create! [_ name]
    (l/info (str "Creating migration '" name "'..."))
    (migratus/create config name)
    :success)
  (init-schema! [_]
    (l/info "Applying initial database schema...")
    (migratus/init config))
  (migrate! [_]
    (l/info "Running migrations...")
    (migratus/migrate config))
  (pending [_]
    (l/info "Checking for pending migrations...")
    (migratus/pending-list config))
  (rollback! [_]
    (l/info "Rolling back last migration...")
    (migratus/rollback config)))

(defn create-migrations
  [datasource]
  (let [base-config (c/get-config :polydoc/migrations)
        config-with-datasoruce (assoc base-config :db {:datasource (ds/get-datasource datasource)})]
    (->MigratusMigrations config-with-datasoruce)))

(defmethod ig/init-key migrations-component-key [_ datasource]
  (l/info "Initializing migrations component")
  (create-migrations datasource))

(defmethod ig/halt-key! migrations-component-key [_ migrations]
  (l/info "Shutting down migrations component")
  (assoc migrations :db nil))
