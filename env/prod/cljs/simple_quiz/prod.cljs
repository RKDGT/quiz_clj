(ns simple-quiz.prod
  (:require [simple-quiz.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
