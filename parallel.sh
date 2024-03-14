# Script depends on autocannon installed globally - https://github.com/mcollina/autocannon

endpoint=http://localhost:8081/rc/test/1

reqs=$1

autocannon --connections ${reqs} --amount ${reqs} ${endpoint}
