;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.srm-api.handler
  (:require
   [camel-snake-kebab.core :as csk]
   [muuntaja.core :as m]
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [polydoc.config.core :as c]
   [polydoc.srm-api.controllers.documents :as docs]))

(def base-api-url "/api/srm")

(defn- response
  ([status] (response status nil))
  ([status body]
   {:status (or status 404)
    :body body}))

(defn health
  [_]
  (let [env (c/get-config :polydoc/environment)]
    (response 200 {:status "ok" :environment env})))

(defn- route->handler
  [app]
  {:health {:get health}
   :search-documents {:get (fn [_] {:status 200 :body "Searching documents"})}
   :get-document {:get {:parameters {:path-params {:id uuid?}}}
                  :handler (partial docs/get-document (:documents-repository app))}})

(defn- router
  [route-map]
  (ring/ring-handler
   (ring/router
    [base-api-url
     ["/v1"
      ["/health" (:health route-map)]
      ["/search" ["/documents" (:search-documents route-map)]]
      ["/documents/:id" (:get-document route-map)]]]
    {:data {:muuntaja (m/create
                       (assoc-in
                        m/default-options
                        [:formats "application/json" :encoder-opts]
                        {:encode-key-fn (comp csk/->camelCase name) :strip-nils true}))
            :middleware [muuntaja/format-middleware]}})))

(defn create-handler
  [app]
  (router (route->handler app)))
