#!/bin/sh
# https://hpratap.medium.com/access-hipster-session-based-authentication-protected-api-through-curl-1eb2d5f65fb0
# obtain and save an authentication token from server. [or postman]
url="https://game5.thegraid.com:8445/api/authenticate"
# headers=("Content-Type: application/json")
headers=("Content-Type: application/json" "Accept: application/json, application/*+json" "User-Agent: Java/11.0.11" "Host: game5.thegraid.com:8445" "Connection: keep-alive")
hargs=(); for x in "${headers[@]}" ; do hargs+=("-H \"$x\"") ; done
# -Ss --> Silent but show errors:
# echo curl -Ss $url -d '{"username":"admin","password":"admin"}' ${hargs[@]}
resp=$(echo ${hargs[@]} | xargs curl -Ss $url -d '{"username":"admin","password":"admin"}' )
# resp=$(curl -Ss $url -d '{"username":"admin","password":"admin"}' ${hargs[@]} )
# -H "Accept: application/json" -H "User-Agent:"
echo "$resp"
# extract the final "quoted" string, and remove quotes:
tokn=$(echo "$resp" | grep "id_token" | grep -o '"[^ ]*"$' | tr -d "\"" | tee ./tokn)
#echo cat tokn: $(cat ./tokn)
#curl -d '{"username":"admin","password":"admin"}' -H "Content-Type: application/json" https://lobby2.thegraid.com:8442/api/authenticate && echo
