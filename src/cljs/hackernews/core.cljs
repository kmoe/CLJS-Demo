(ns hackernews.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [hackernews.utils :refer [send!]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [alts! chan put!]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils & constants(ns hackernews.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [hackernews.utils :refer [send!]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [alts! chan put!]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils & constants.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(enable-console-print!)

(def hacker-news-source
  "https://hn.algolia.com/api/v1/search_by_date?tags=story&hitsPerPage=200")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UI Rendering
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcomponent headline-view
  [hit owner]
  (render [_]
    (let [{:keys [title url author]} hit]
      (html [:h4
             [:a {:href url}
              title]
              " - "
              [:a {:href (str "https://news.ycombinator.com/user?id=" author)} author]]
            ))))

(defcomponent root-view
  [data owner]
  (render [_]
    (html
      [:div.container
       [:div.row
        [:div.col-xs-10.col-xs-offset-2
         [:h1 (:text data)]]]

       [:div.row
        [:div.col-xs-2
         [:button.btn.btn-primary.pull-right
          {:on-click (send! owner {:type :load-request})}
          "Load"]]
        [:div.col-xs-10
         (om/build-all headline-view
                       (-> data :headlines :hits)
                       {:key :objectID})]]

       [:div.row
        [:div.col-xs-10.col-xs-offset-2
         [:code (pr-str data)]]]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event Handling
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-hackernews-data
  []
  (go
    {:type :load-response
     :response (<! (http/get hacker-news-source
                             {:with-credentials? false}))}))

(defn handle-event!
  [app event]
  (case (:type event)
    :load-request (update-in app [:events] conj (fetch-hackernews-data))

    :load-response (let [{:keys [response]} event]
                     (if (= (:status response) 200)
                       (assoc app :headlines (:body response))
                       (assoc app :errors response)))
    (assoc app :last-event event)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; App setup.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce app-state
  (atom {:text "Hello Funjs!"
         :events #{}}))

(defn main
  []
  (let [ui-events (chan)]
    (om/root root-view
             app-state
             {:target (. js/document (getElementById "app"))
              :shared {:event-channel ui-events}})

    (swap! app-state update :events conj ui-events)

    (go-loop []
      (let [[event channel] (alts! (seq (@app-state :events)))]
        (if (nil? event)
          (swap! app-state update :events disj channel)
          (do
            (swap! app-state handle-event! event)))
        (recur)))))
.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(enable-console-print!)

(def hacker-news-source
  "https://hn.algolia.com/api/v1/search_by_date?tags=story&hitsPerPage=200")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UI Rendering
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcomponent headline-view
  [hit owner]
  (render [_]
    (let [{:keys [title url]} hit]
      (html [:h4
             [:a {:href url}
              title]]))))

(defcomponent root-view
  [data owner]
  (render [_]
    (html
      [:div.container
       [:div.row
        [:div.col-xs-10.col-xs-offset-2
         [:h1 (:text data)]]]

       [:div.row
        [:div.col-xs-2
         [:button.btn.btn-primary.pull-right
          {:on-click (send! owner {:type :load-request})}
          "Load"]]
        [:div.col-xs-10
         (om/build-all headline-view
                       (-> data :headlines :hits)
                       {:key :objectID})]]

       [:div.row
        [:div.col-xs-10.col-xs-offset-2
         [:code (pr-str data)]]]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event Handling
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-hackernews-data
  []
  (go
    {:type :load-response
     :response (<! (http/get hacker-news-source
                             {:with-credentials? false}))}))

(defn handle-event!
  [app event]
  (case (:type event)
    :load-request (update-in app [:events] conj (fetch-hackernews-data))

    :load-response (let [{:keys [response]} event]
                     (if (= (:status response) 200)
                       (assoc app :headlines (:body response))
                       (assoc app :errors response)))
    (assoc app :last-event event)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; App setup.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce app-state
  (atom {:text "Hello Funjs!"
         :events #{}}))

(defn main
  []
  (let [ui-events (chan)]
    (om/root root-view
             app-state
             {:target (. js/document (getElementById "app"))
              :shared {:event-channel ui-events}})

    (swap! app-state update :events conj ui-events)

    (go-loop []
      (let [[event channel] (alts! (seq (@app-state :events)))]
        (if (nil? event)
          (swap! app-state update :events disj channel)
          (do
            (swap! app-state handle-event! event)))
        (recur)))))
