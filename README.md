# LCMAP REST Service

## About

TBD

## Usage

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
17:14:29.863 [nREPL-worker-6] INFO  lcmap-rest.components.config - Setting up LCMAP configuration ...
17:14:29.864 [nREPL-worker-6] INFO  lcmap-rest.components.config - Using lein profile: dev
17:14:29.864 [nREPL-worker-6] INFO  lcmap-rest.components.logger - Setting up LCMAP logging ...
17:14:29.864 [nREPL-worker-6] INFO  lcmap-rest.components.logger - Using log-level :info
17:14:29.907 [nREPL-worker-6] INFO  lcmap-rest.components.db - Starting DB client ...
```

Any subsequent changes to source files (including ``project.clj``) can be
brought into the REPL by resetting (reloading, rereading, etc.):

```clojure
lcmap-rest.dev=> (reset)
```

A quick test for this is to change the ``log-level`` in ``project.clj`` as the
change is immediately obvious.

### Production

TBD

## Versioned Service API

The LCMAP service version is selected via an ``Accept`` header, as demonstrated
with the following:

```bash
$ curl -H "Accept: application/vnd.usgs.lcmap.v0.0+json" \
    http://localhost:8080/api/L1/T/Landsat/8/SurfaceReflectance
```
