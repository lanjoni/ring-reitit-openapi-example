(ns example.core
  (:require [reitit.ring.spec]
            [reitit.coercion.malli]
            [reitit.ring.malli]
            [ring.adapter.jetty :as jetty]
            [example.server :as server]))

(defonce server (atom nil))

(defonce all-started-servers (atom []))

(do
  (defn run-jetty [& args]
    (let [server (apply ring.adapter.jetty/run-jetty args)]
      (swap! all-started-servers conj server)
      server))
  (alter-meta! #'run-jetty
               (fn [current-meta]
                 (merge (meta #'ring.adapter.jetty/run-jetty)
                        (select-keys current-meta [:line :column :file :name :ns]))))
  #'run-jetty)

(defn start-server! [port]
  (locking server
    (if @server
      ::server-already-running
      (let [new-server (run-jetty #'server/app
                                  {:port port
                                   :join? false})]
        (reset! server new-server)
        ::server-started))))

(defn stop-server! []
  (locking server
    (if @server
      (do (.stop @server)
          (reset! server nil)
          ::server-stopped)
      ::server-already-stopped)))

(defn -main
  "Start webserver"
  [& args]
  (start-server! 3000)
  (println "Server started running at port 3000"))

(comment
  ;; if you're running a REPL,
  ;; just evaluate this to start
  (start-server! 3000)
  ;; and this to stop
  (stop-server!))
