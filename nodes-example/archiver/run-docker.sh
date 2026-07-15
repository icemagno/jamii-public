#! /bin/sh

docker run \
  --name jamii-archiver \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8556:8556 \
  -p 30310:30310 \
  -p 42020:42020 \
  -d magnoabreu/jamii-archiver:0.1.0-alpha 


