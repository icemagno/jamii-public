#! /bin/sh

docker run \
  --name jamii-node-4 \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8548:8548 \
  -p 30306:30306 \
  -p 42093:42093 \
  -d magnoabreu/jamii-node:0.1.0-alpha 


