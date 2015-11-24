# auth-server

This is just a test auth server which provides a minimal number of payloads
emulating the USGS EROS ERS authentication server.

## Usage

From the ``lcmap-rest`` cloned directory:

```bash
$ (cd test/support/auth-server && lein run)
```

At that point, the test server's endpoint will be available at
``localhost:8888`` with the following resources:

* ``/api/auth``
* ``/api/me``

Test bad credentials:

```bash
curl -X POST -d 'username=bob' -d 'password=abc123' \
  http://localhost:8888/api/auth
```
```json
{"data":null,"errors":["Invalid username/password"],"status":20}
```

Test missing username:

```bash
curl -X POST -d 'password=abc123' http://localhost:8888/api/auth
```
```json
{"data":null,"errors":["Username is required"],"status":31}
```

Test missing password:

```bash
curl -X POST -d 'username=bob' http://localhost:8888/api/auth
```
```json
{"data":null,"errors":["Password is required"],"status":31}
```

Test good credentials:

```bash
curl -X POST -d 'username=alice' -d 'password=secret' \
  http://localhost:8888/api/auth
```
```json
{"data":{"authToken":"3efc6475b5034309af00549a77b7a6e3"},"status":10}
```

Test user data with good token:

```bash
curl -X GET -H "X-AuthToken: 3efc6475b5034309af00549a77b7a6e3" \
  http://localhost:8888/api/me
```
```json
{"data": {"affiliation":"U.S. Federal Government",
          "agency":"Geological Survey (USGS)",
          "contact_id":"001010111",
          "email":"alice@usgs.gov",
          "roles":["RPUBLIC","LANDSAT8CUST"],
          "username":"alice"},
 "status":10}
```

Test user data with bad token:

```bash
curl -v -X GET -H "X-AuthToken: abcdef123456" http://localhost:8888/api/me
```

There will be no body response from that call, just a ``403 Forbidden`` HTTP
status in the response.
