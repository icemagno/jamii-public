#! /bin/sh

docker run \
  --name jamii-node-2 \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8546:8546 \
  -p 30304:30304 \
  -p 42091:42091 \
  -d magnoabreu/jamii-node:0.1.0-alpha 


