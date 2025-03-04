(ns polydoc.documents.repository
  (:require [polydoc.documents.parser :as parser]
            [polydoc.database.interface :as db]))

(defprotocol DocumentRepository
  "Interface for document repository"
  (search-documents [this params])
  (search-documents-count [this params])
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
  (search-documents-count [this params]
    (let [params-minus-pagination (dissoc params :offset :limit)
          results (search-documents this params-minus-pagination)]
      (count results)))
  (get-document-by-id [_ id]
    (let [flat-results (db/execute! db {:select [:*]
                                        :from [[:documents :d]]
                                        :left-join [[:users :u] [:= :d.uploaded_by_user_id :u.user_id]
                                                    [:document_metadata_values :dmv] [:= :d.document_id :dmv.document_id]]
                                        :where [:= :d.document_id [:uuid id]]})]
      (parser/parse-document-result flat-results))))

