(ns polydoc.webserver.core
  (:require
   [ring.adapter.jetty :as jetty]
   [polydoc.logger.interface :as l]))

(defprotocol Webserver
  (start [this])
  (stop [this]))

(defrecord JettyWebserver [handler config server]
  Webserver
  (start [this]
    (l/info "Starting webserver..." config)
    (let [server (jetty/run-jetty handler config)]
      (assoc this :server server)))
  (stop [this]
    (l/info "Stopping webserver...")
    (when server
      (.stop server)
      (assoc this :server nil))))

(defmulti create-webserver
  "Creates a new webserver"
  (fn [tag _ _] tag))

(defmethod create-webserver :jetty
  [_ handler config]
  (->JettyWebserver handler config nil))
