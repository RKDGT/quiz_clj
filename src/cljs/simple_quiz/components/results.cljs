(ns simple-quiz.components.results
  (:require
   [re-frame.core :as re-frame]
   [simple-quiz.subs :as subs]))

(def id-res (atom 0))

(defmulti question-answers (fn [question] (:type question)))

(defmethod question-answers
  "free-text"
  [question]
  [:div.free-text-res
   [:p.amount-free-text (str (count (:values question)) " answers")]
   [:div.free-text-answers
    (for [value (:values question)]
      (do
        (swap! id-res inc @id-res)
        ^{:key @id-res} [:p value]))]
   ])

(defn frequence-calc [values summary-amount]
  (js/Math.round (* (/ (second values) summary-amount) 100)))

(defn frequens-table [values summary-amount]
   [:div.freq-table
     (for [answer values]
      (do
        (swap! id-res inc @id-res)
        ^{:key @id-res}
        [:div.freq-table-elem
         [:p (name (first answer))]
         [:p
          (str (frequence-calc answer summary-amount) "%")]]))])

(defn column-diagram [values summary-amount]
  [:div.colmn-diagram
   (let [scale (/ 100 (apply max (map second values)))]
     (for [answer values]
       (do
         (swap! id-res inc @id-res)
         ^{:key @id-res} [:div.col-of-diagr {:style {:width (str (/ 100 (+ (count values) 0.5)) "%") :height (str (* (second answer) scale) "%")}}
                          [:span.show-stats-detail (str (js/Math.round (* (/ (second answer) summary-amount) 100)) "%")]])))])

(defn choise-answers [question]
   (let [values (seq (:values question))
         freq-sum (reduce + (map second (seq (:values question))))]
     [:div.choice-answers
      [:p.amount-free-text (str freq-sum " answers")]
      [frequens-table values freq-sum]
      [column-diagram values freq-sum]]))

(defmethod question-answers
  "single-choice"
  [question]
  [:div.single-choice
   [choise-answers question]])

(defmethod question-answers
  "multiple-choice"
  [question]
  [:div.multiple-choice
   [choise-answers question]])

(defn question-card [question] 
  [:div.question
   [:div.quesion-header
    [:h2 (:question question)]]
   [question-answers question]])

(defn results []
  (fn []
    (let [res  @(re-frame/subscribe [::subs/results])]
     [:div.questions
      [:h1 (:title res)]
      (for [question (:questions res)]
        (do
          (swap! id-res inc @id-res)
          ^{:key @id-res} [question-card question]))])))