#! /bin/sh

docker run \
  --name jamii-node-3 \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8547:8547 \
  -p 30305:30305 \
  -p 42092:42092 \
  -d magnoabreu/jamii-node:0.1.0-alpha 


