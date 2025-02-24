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

(ns polydoc.migrations-cli.core
  "Command-line interface for managing database migrations using Migratus.

  This namespace defines the CLI application for PolyDoc's database migrations.
  It provides commands to apply migrations, rollback migrations, check migration
  status, and create new migration files."
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [integrant.core :as ig]
   [polydoc.config.interface :as c]
   [polydoc.migrations.interface :as m]))

(defn init-system
  []
  (let [config (c/get-config [:polydoc/systems :migrations-cli])]
    (ig/init config)))

(def cli-options
  [["-h" "--help" "Show help"]]) ; Top-level help option

(def usage
  (str "PolyDoc Migration CLI\n\n"
       "Usage: polydoc-migration-cli <command> [options]\n\n"
       "Commands:\n"
       "  create <NAME> Create a new migration\n"
       "  list          List pending migrations\n"
       "  run <ACTION>  Run migration subcommands\n"
       "\n"
       "Run Subcommands (under 'run'):\n"
       "  migrate         Run pending migrations\n"
       "  rollback        Rollback the last migration\n\n"
       "Options:\n"
       "  -h, --help      Show help\n"))

(def supported-commands
  #{"create" "list" "run"})

(def supported-subcommands
  {:run #{"migrate" "rollback"}})

(defn is-supported-command?
  [command]
  (supported-commands command))

(defn is-supported-subcommand?
  [command action]
  ((supported-subcommands command) action))

(defn error-message
  [errors]
  (str "Error(s) during command line parsing:\n" (str/join "\n" errors)))

(defn exit
  "Exit the application with the specified status code and message."
  [status msg]
  (println msg)
  (System/exit status))

(defn process-create-command
  "Create a new migration file with the specified name."
  [migrations name]
  (if (string? name)
    (try
      (println (str "Creating migration '" name "'..."))
      (m/create! migrations name)
      {:ok? true
       :exit-message "Migration created successfully."}
      (catch Exception e
        {:exit-message (str "Failed to create migration: " (ex-message e))}))
    {:exit-message "Missing command line argument <NAME> for 'create' command."}))

(defn process-list-command
  "List pending migrations."
  [migrations]
  (try
    (println "Checking for pending migrations...")
    (let [pending (m/pending migrations)
          exit-message (if (seq pending)
                         (str "Pending migrations:\n\t- " (str/join (interpose "\n\t- " pending)))
                         "No pending migrations.")]
      {:ok? true
       :exit-message exit-message})
    (catch Exception e
      {:exit-message (str "Failed to list pending migrations: " (ex-message e))})))

(defn process-run-command
  "Run a migration subcommand. Supported subcommands are 'migrate' and 'rollback'."
  [migrations action]
  (if (is-supported-subcommand? :run action)
    (try
      (println (str "Running " action " action..."))
      (case action
        "migrate" (m/migrate! migrations)
        "rollback" (m/rollback! migrations))
      {:ok? true
       :exit-message (str action " action completed successfully")}
      (catch Exception e
        {:exit-message (str "Failed to run " action " action: " (ex-message e))}))
    {:exit-message (str "Unknown or missing command line argument for <ACTION> for 'run' command")}))

(defn process-command
  "Wrapper function to process a command and its arguments."
  [command args]
  (let [system (init-system)
        migrations (:polydoc/migrations system)
        {:keys [ok? exit-message]} (case command
                                     "create" (process-create-command migrations (first args))
                                     "list" (process-list-command migrations)
                                     "run" (process-run-command migrations (first args))
                                     {:exit-message (str "Unknown command: " command)})]
    (ig/halt! system)
    (exit (if ok? 0 1) exit-message)))

(defn -main
  "Entry point for the migration CLI application.

  Parses command-line arguments using cli-matic and executes the
  corresponding command handler function."
  [& args]
  (let [{:keys [options arguments errors]} (cli/parse-opts args cli-options)
        [command & command-args] arguments]
    (cond
      errors (exit 1 (error-message errors))
      (:help options) (exit 0 usage)
      (is-supported-command? command) (process-command command command-args)
      :else (exit 1 (str "Missing or invalid command.\n\n" usage)))))

