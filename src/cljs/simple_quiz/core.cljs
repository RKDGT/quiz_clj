(ns simple-quiz.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [re-frame.core :as re-frame]
   [simple-quiz.components.quiz :as quiz-comp]
   [simple-quiz.events :as events]))
;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/stat" :stat]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))


(defn s []
  (re-frame/dispatch-sync [::events/read-results-json])
  [:p "lol"])
;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index quiz-comp/quiz
    :stat s
    :default nil))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
       [page])))

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [current-page] root-el)))

(defn init! []
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch [::events/read-quiz-json])
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (r/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
