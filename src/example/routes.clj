(ns example.routes
  (:require [reitit.ring.spec]
            [reitit.coercion.malli]
            [reitit.openapi :as openapi]
            [reitit.ring.malli]))

(def routes
  [["/openapi.json"
    {:get {:no-doc true
           :openapi {:info {:title "my-api"
                            :description "openapi3 docs with [malli](https://github.com/metosin/malli) and reitit-ring"
                            :version "0.0.1"}
                     ;; used in /secure APIs below
                     :components {:securitySchemes {"auth" {:type :apiKey
                                                            :in :header
                                                            :name "Example-Api-Key"}}}}
           :handler (openapi/create-openapi-handler)}}]

   ["/pizza"
    {:get {:summary "Fetch a pizza | Multiple content-types, multiple examples"
           :responses {200 {:content {"application/json" {:description "Fetch a pizza as json"
                                                          :schema [:map
                                                                   [:color :keyword]
                                                                   [:pineapple :boolean]]
                                                          :examples {:white {:description "White pizza with pineapple"
                                                                             :value {:color :white
                                                                                     :pineapple true}}
                                                                     :red {:description "Red pizza"
                                                                           :value {:color :red
                                                                                   :pineapple false}}}}
                                      "application/edn" {:description "Fetch a pizza as edn"
                                                         :schema [:map
                                                                  [:color :keyword]
                                                                  [:pineapple :boolean]]
                                                         :examples {:red {:description "Red pizza with pineapple"
                                                                          :value (pr-str {:color :red :pineapple true})}}}}}}
           :handler (fn [_request]
                      {:status 200
                       :body {:color :red
                              :pineapple true}})}
     :post {:summary "Create a pizza | Multiple content-types, multiple examples"
            :request {:content {"application/json" {:description "Create a pizza using json"
                                                    :schema [:map
                                                             [:color :keyword]
                                                             [:pineapple :boolean]]
                                                    :examples {:purple {:value {:color :purple
                                                                                :pineapple false}}}}
                                "application/edn" {:description "Create a pizza using EDN"
                                                   :schema [:map
                                                            [:color :keyword]
                                                            [:pineapple :boolean]]
                                                   :examples {:purple {:value (pr-str {:color :purple
                                                                                       :pineapple false})}}}}}
            :responses {200 {:content {:default {:description "Success"
                                                 :schema [:map [:success :boolean]]
                                                 :example {:success true}}}}}
            :handler (fn [_request]
                       {:status 200
                        :body {:success true}})}}]


   ["/contact"
    {:get {:summary "Search for a contact | Customizing via malli properties"
           :parameters {:query [:map
                                [:limit {:title "How many results to return? Optional."
                                         :optional true
                                         :json-schema/default 30
                                         :json-schema/example 10}
                                 int?]
                                [:email {:title "Email address to search for"
                                         :json-schema/format "email"}
                                 string?]]}
           :responses {200 {:content {:default {:schema [:vector
                                                         [:map
                                                          [:name {:json-schema/example "Heidi"}
                                                           string?]
                                                          [:email {:json-schema/example "heidi@alps.ch"}
                                                           string?]]]}}}}
           :handler (fn [_request]
                      {:status 200
                       :body [{:name "Heidi"
                               :email "heidi@alps.ch"}]})}}]


   ["/secure"
    {:tags #{"secure"}
     :openapi {:security [{"auth" []}]}}
    ["/get"
     {:get {:summary "endpoint authenticated with a header"
            :responses {200 {:body [:map [:secret :string]]}
                        401 {:body [:map [:error :string]]}}
            :handler (fn [request]
                       ;; In a real app authentication would be handled by middleware
                       (if (= "secret" (get-in request [:headers "example-api-key"]))
                         {:status 200
                          :body {:secret "I am a marmot"}}
                         {:status 401
                          :body {:error "unauthorized"}}))}}]]])
