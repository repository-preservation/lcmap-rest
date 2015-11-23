# auth-server

This is just a test auth server which provides a minimal number of payloads
emulating the USGS EROS ERS authentication server.

## Usage

```bash
$ (cd lcmap-rest/test/support && lein run)
```

At that point, the test server's endpoint will be available at
``localhost:8888`` with the following resources:

* ``/api/auth``
* ``/api/me``
