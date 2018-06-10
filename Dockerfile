FROM clojure:lein-alpine

WORKDIR /root

CMD ["lein", "ring", "server-headless"]
