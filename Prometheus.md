

## Metrics

### Apache HttpClient

* `httpcomponents_httpclient_pool_total_connections` - number of connections in pool
* `http_connections_created_time_seconds_count` - total connections created
* `http_connections_created_time_seconds_sum` - total time spent creating connections
* `1000 * (http_connections_created_time_seconds_sum/http_connections_created_time_seconds_count)` - average time spent creating connections


### Netty HttpClient

* `reactor_netty_connection_provider_total_connections` - number of connections in pool
* `reactor_netty_http_client_connect_time_seconds_count` - total connections created
* `reactor_netty_http_client_connect_time_seconds_sum` - total time spent creating connections (http)
* `reactor_netty_http_client_connect_time_seconds_sum + reactor_netty_http_client_tls_handshake_time_seconds_sum` - total time spent creating connections (https)
* `1000 * (reactor_netty_http_client_connect_time_seconds_sum/reactor_netty_http_client_connect_time_seconds_count)` - average time spent creating connections (http)
* `1000 * ((reactor_netty_http_client_connect_time_seconds_sum + reactor_netty_http_client_tls_handshake_time_seconds_sum)/reactor_netty_http_client_connect_time_seconds_count)` - average time spent creating connections (https)