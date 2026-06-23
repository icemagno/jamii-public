#! /bin/sh

docker run \
  --name jamii-node-5 \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8549:8549 \
  -p 30307:30307 \
  -p 42094:42094 \
  -d magnoabreu/jamii-node:0.1.0-alpha 


