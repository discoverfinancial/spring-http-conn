# Script depends on autocannon installed globally - https://github.com/mcollina/autocannon

endpoint=http://localhost:8081/rc/test/1

connections=10
duration_sec=30

autocannon --connections ${connections} --duration ${duration_sec} ${endpoint}
