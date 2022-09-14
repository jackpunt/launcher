#!/bin/sh
# provoke launch of gi184.json
url="https://game5.thegraid.com:8445/api/launcher/launch"
curl $url -d@gi184.json -H "Content-Type: application/json" -H "Authorization: Bearer $(cat ./tokn)" && echo