;; Copyright (C) 2025 Andrew Leverette. All rights reserved.

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; You must not remove this notice, or any other, from this software.

(ns polydoc.documents.core
  (:require
   [polydoc.database.interface :as db]
   [polydoc.documents.parser :as parser]))

(defprotocol DocumentRepository
  "Interface for document repository"
  (search-documents [this params])
  (get-document-by-id [this id]))

(defrecord SqlDocumentRepository [db]
  DocumentRepository
  (search-documents [_ params]
    (let [base-query-map {:select [:d.* :dmv.*]
                          :from [[:documents :d]]
                          :left-join [[:document_type_summary_metadata_config :config] [:= :d.document_type :config.document_type]
                                      [:document_metadata_values :dmv] [:and
                                                                        [:= :d.document_id :dmv.document_id]
                                                                        [:= :config.attribute_definition_id :dmv.attribute_definition_id]]]}
          query-params (parser/parse-query-params :search params)
          query-map (cond-> base-query-map
                      query-params (merge base-query-map query-params))
          flat-results (db/execute! db query-map)]
      (parser/parse-document-search-results flat-results)))
  (get-document-by-id [_ id]
    (let [flat-results (db/execute! db {:select [:*]
                                        :from [[:documents :d]]
                                        :left-join [[:document_metadata_values :dmv] [:= :d.document_id :dmv.document_id]]
                                        :where [:= :d.document_id [:uuid id]]})]
      (parser/parse-document-result flat-results))))

(defmulti create-document-repository
  (fn [tag _] tag))

(defmethod create-document-repository :sql [_ db] (->SqlDocumentRepository db))
