(ns stuttaford.client.start
  (:require [cljs.reader :as edn]
            [datascript.core :as d]
            [goog.dom :as dom]
            [rum.core :as rum])
  (:import goog.dom.query))

(def COMPONENT-ATTR "data-component")
(def COMPONENT-DOM-QUERY (str "[" COMPONENT-ATTR "]"))

(defn read-edn-from-inner-script-tag [element]
  (some->> (dom/getElementsByTagNameAndClass "script" nil element)
           array-seq
           first
           .-textContent
           edn/read-string))

(defn prepare-datascript-db [state]
  (if-let [{:keys [datoms schema]} (:db state)]
    (let [datoms (map (partial apply d/datom) datoms)]
      (assoc state :db @(d/conn-from-datoms datoms schema)))
    state))

(defn start-component! [common-state component-type->fn element]
  (when-let [component-fn (get component-type->fn
                               (.getAttribute element COMPONENT-ATTR))]
    (when-let [state (read-edn-from-inner-script-tag element)]
      (swap! common-state merge (prepare-datascript-db state)))
    (rum/mount (component-fn common-state) element)))

(defn start-all-components! [common-state component-type->fn]
  (doseq [element (array-seq (goog.dom.query COMPONENT-DOM-QUERY))]
    (start-component! common-state component-type->fn element)))
