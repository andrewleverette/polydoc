;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.documents.core
  (:require
   [integrant.core :as ig]
   [polydoc.logger.interface :as l]
   [polydoc.documents.repository :as repo]))

(def document-repository-component-key :polydoc/documents)

(defmulti create-document-repository
  (fn [tag _] tag))

(defmethod create-document-repository :sql [_ db] (repo/->SqlDocumentRepository db))

(defn search-documents
  [repository params]
  (repo/search-documents repository params))

(defn search-documents-count
  [repository params]
  (repo/search-documents-count repository params))

(defn get-document-by-id
  [repository id]
  (repo/get-document-by-id repository id))

(defmethod ig/init-key document-repository-component-key [_ {:keys [tag db]}]
  (l/info "Creating document repository")
  (create-document-repository tag db))
