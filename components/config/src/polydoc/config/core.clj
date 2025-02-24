;; Copyright (C) 2025 Andrew Leverette

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

(ns polydoc.config.core
  "Component for managing application configuration.

  This component is responsible for loading and providing access to
  the application's configuration settings. It loads configuration from
  environment-specific EDN files located in the `env/` directory.

  The configuration is loaded once when the component is initialized and
  can be refreshed at runtime if needed.  It provides functions to retrieve
  configuration values by keys or key paths.

  Configuration files are expected to be in EDN format and named according
  to the environment (e.g., `env/local.env.edn`, `env/dev.env.edn`, `env/prod.env.edn`).
  The environment is determined by the `POLYDOC_ENV` environment variable,
  defaulting to 'local' if the variable is not set."
  (:require
   [clojure.java.io :as io]
   [integrant.core :as ig]))

(defn- get-env
  []
  (or (System/getenv "POLYDOC_ENV") "local"))

(defn- parse-config-str
  [str]
  (let [content (ig/read-string str)]
    (if (map? content)
      content
      (throw (ex-info "Invalid config file. Expected a map" {:content content})))))

(defn- read-file-content
  [filepath]
  (let [f (io/file filepath)]
    (if (.exists f)
      (slurp f)
      (throw (ex-info "Config file not found" {:filepath filepath})))))

(defn- read-env-file
  [env]
  (let [filepath (str "env/" env ".env.edn")]
    (->> filepath
         read-file-content
         parse-config-str)))

(defn- load-config []
  (let [env (get-env)]
    (try
      (read-env-file env)
      (catch Exception e
        (throw (ex-info "Error loading config" {:env env} e))))))

(def ^:private config
  "PolyDoc configuration map"
  (load-config))

(defn get-config
  ([ks] (get-config ks nil))
  ([ks default]
   (let [s (if (sequential? ks) ks [ks])]
     (get-in config s default))))

(defn refresh
  []
  (alter-var-root #'config (constantly (load-config))))
