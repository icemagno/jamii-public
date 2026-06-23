#! /bin/sh

docker run \
  --name jamii-node-1 \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8545:8545 \
  -p 30303:30303 \
  -p 42090:42090 \
  -d magnoabreu/jamii-node:0.1.0-alpha 


