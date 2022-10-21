#!/bin/sh
# https://hpratap.medium.com/access-hipster-session-based-authentication-protected-api-through-curl-1eb2d5f65fb0
# obtain and save an authentication token from server. [or postman]
url="https://game5.thegraid.com:8445/api/authenticate"
resp=$(curl -Ss $url -d '{"username":"admin","password":"admin"}' -H "Content-Type: application/json")
# -H "Accept: application/json" -H "User-Agent:"
echo resp="$resp"
# extract the final "quoted" string:
tokn=$(echo "$resp" | grep "id_token" | grep -o '"[^ ]*"$' | tr -d "\"" | tee ./tokn)
#echo cat tokn: $(cat ./tokn)
#curl -d '{"username":"admin","password":"admin"}' -H "Content-Type: application/json" https://lobby2.thegraid.com:8442/api/authenticate && echo