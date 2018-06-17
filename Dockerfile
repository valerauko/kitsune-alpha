FROM clojure:lein-alpine

WORKDIR /root

CMD ["lein", "run"]
