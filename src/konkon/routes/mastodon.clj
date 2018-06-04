(ns konkon.routes.mastodon)

(def routes
  ["/api"
    {:data {:summary "Mastodon compatible API"}}
    ["/v1"
      ["/accounts"
        ["/:id"
          {:get {:handler todo}}
          ["/followers"
            {:get {:handler todo}}]
          ["/following"
            {:get {:handler todo}}]
          ["/statuses"
            {:get {:handler todo}}]
          ["/follow"
            {:post {:handler todo}}]
          ["/unfollow"
            {:post {:handler todo}}]
          ["/block"
            {:post {:handler todo}}]
          ["/unblock"
            {:post {:handler todo}}]
          ["/mute"
            {:post {:handler todo}}]
          ["/unmute"
            {:post {:handler todo}}]
          ["/lists"
            {:get {:handler todo}}]]
        ["/relationships"
          {:get {:handler todo}}]
        ["/search"
          {:get {:handler todo}}]
        ["/verify_credentials"
          {:get {:handler todo}}]
        ["/update_credentials"
          {:patch {:handler todo}}]]
      ["/apps"
        {:post {:handler todo}}]
      ["/blocks"
        {:get {:handler todo}}]
      ["/domain_blocks"
        {:get {:handler todo}}
        {:post {:handler todo}}
        {:delete {:handler todo}}]
      ["/favourites"
        {:get {:handler todo}}]
      ["/follow_requests"
        {:get {:handler todo}}
        ["/:id"
          ["/authorize"
            {:post {:handler todo}}]
          ["/reject"
            {:post {:handler todo}}]]]
      ["/follow"
        {:post {:handler todo
                :summary "Follow a remote user"}}]
      ["/instance"
        {:get {:handler todo}}]
      ["/custom_emojis"
        {:get {:handler todo}}]
      ["/lists"
        {:get {:handler todo}
         :post {:handler todo}}
        ["/:id"
          {:get {:handler todo}
           :put {:handler todo}
           :delete {:handler todo}}
          ["/accounts"
            {:get {:handler todo}
             :post {:handler todo}
             :delete {:handler todo}}]]]]
    ["/v2"]])
