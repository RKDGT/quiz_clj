(ns simple-quiz.events
  (:require
   [re-frame.core :as re-frame]
   [simple-quiz.db :as db]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]))

(defn formed-result [questions]
    (for [question (:questions questions)]
      {:question (:question question)
                     :type (:type question)
                     :values (if (= (:type question) "free-text")
                               []
                               (zipmap (:values question) (vec (replicate (count (:values question)) 0))))}))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 :request
 (fn []
   {:http-xhrio {:uri "http://localhost:3000/question"
                 :method :get
                 :timeout 10000
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:process-response]
                 :on-failure [:bad-response]}}))

(re-frame/reg-event-db
 :process-response
 (fn [db [_ response]]
     (assoc db :quiz response)
   ))

(re-frame/reg-event-db
 :bad-response
 (fn [_ [_ response]]
   (println response)))