(ns polydoc.config.core
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
  (try
    (read-env-file (get-env))
    (catch Exception e
      (println "Error loading config for environment:" (ex-message e))
      (println "Error details:" (ex-cause e))
      (throw e))))

(def ^:private config
  "PolyDoc configuration map"
  (load-config))

(defn get-config
  ([ks] (get-config ks nil))
  ([ks default]
   (let [s (if (sequential? ks) ks [ks])]
     (get-in config s default))))
