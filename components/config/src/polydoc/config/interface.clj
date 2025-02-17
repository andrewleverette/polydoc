(ns polydoc.config.interface
  (:require [polydoc.config.core :as config]))

(defn get-config
  "Given a sequence of keys, return the value of the config at those keys.
  Returns nil or default if not found."
  ([ks] (config/get-config ks))
  ([ks default] (config/get-config ks default)))
