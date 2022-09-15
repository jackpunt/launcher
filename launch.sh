#!/bin/sh
# provoke launch of gi184.json
giid=${1:=148}
url="https://game5.thegraid.com:8445/api/launcher/launch"
curl $url -d@gi$giid.json -H "Content-Type: application/json" -H "Authorization: Bearer $(cat ./tokn)" && echo