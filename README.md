# LCMAP REST Service


## About

The Land Change Monitoring Assessment and Projection (LCMAP) system will
provide the science community with real-time access to decades of Landsat
Analysis Ready Data (ARD). To make this system available to the public
scientific computing community, an API service will be provided. The
LCMAP REST service API is codified in this project and prototypes the
functionality necessary to support the public service.


### Development

To start the REST server and dependent components, do the following:

```bash
$ lein trampoline run
```

(The ``trampoline`` lein task is needed in order to properly handle ``SIGINT``
and ``SIGTERM``.)


To run the system in the REPL, simply start the REPL:

```bash
$ lein repl
```

This will drop you into the ``lcmap-rest.dev`` namespace:
```clojure
lcmap-rest.dev=>
```

From here you can start the system components and also reset them after changes
to lcmap-rest files:

```clojure
lcmap-rest.dev=> (run)
17:14:29.863 [nREPL-worker-6] INFO  l-r.c.config - Setting up LCMAP configuration ...
17:14:29.864 [nREPL-worker-6] INFO  l-r.c.config - Using lein profile: dev
17:14:29.864 [nREPL-worker-6] INFO  l-r.c.logger - Setting up LCMAP logging ...
17:14:29.864 [nREPL-worker-6] INFO  l-r.c.logger - Using log-level :info
17:14:29.907 [nREPL-worker-6] INFO  l-r.c.db - Starting DB client ...
```

Any subsequent changes to source files (including ``project.clj``) can be
brought into the REPL by resetting (reloading, rereading, etc.):

```clojure
lcmap-rest.dev=> (reset)
:reloading (lcmap-rest.util lcmap-rest.job.db)
```

A quick test for this is to change the ``log-level`` in ``project.clj`` and
then run ``(reset)``, since the difference in log output will be obvious.

We use kibit to double-check idiomatic use of Clojure (we've got Rubyists,
Pythonistas, Erlangers, and old Lispers using this software, so the probability
for non-idiomatic contributions is high!). To check the code base, simply run
the lein plugin:

```bash
$ lein kibit
```

### Production

TBD

## Usage

### Versioned Service API

The LCMAP service version is selected via an ``Accept`` header, as demonstrated
with the following:

```bash
$ curl -H "Accept: application/vnd.usgs.lcmap.v0.0+json" \
    http://localhost:8080/api/L1/T/Landsat/8/SurfaceReflectance
```

### Sample Model

The LCMAP REST service provides a sample execution API for testing purposes.
To kick off a new job, you can run something like the following:

```bash
curl -v -X POST \
  -H "Accept: application/vnd.usgs.lcmap.v0.0+json" \
  'http://localhost:8080/api/L3/sample/model?seconds=15&year=2016'
```
