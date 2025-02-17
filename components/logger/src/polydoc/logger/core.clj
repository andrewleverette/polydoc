(ns polydoc.logger.core
  (:require
   [taoensso.telemere :as t]
   [polydoc.config.interface :as c]))

(t/set-min-level! :log (c/get-config :logger/min-level :info))

(defn log
  ([level message] (t/log! level message))
  ([level message data] (t/log! {:level level :data data} message)))
