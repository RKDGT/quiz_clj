(ns simple-quiz.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::quiz
 (fn [db]
   (:data db)))

(re-frame/reg-sub
 ::results
 (fn [db]
   (get-in db [:data :results])))
