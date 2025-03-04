(ns polydoc.webserver.interface
  (:require [polydoc.webserver.core :as core]))

(defn create-webserver
  "Creates a new webserver"
  [tag handler config]
  (core/create-webserver tag handler config))

(defn start
  "Starts the webserver with the given handler and config"
  [webserver]
  (core/start webserver))

(defn stop
  "Stops the webserver"
  [webserver]
  (core/stop webserver))
