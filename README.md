# LCMAP REST Service

**IMPORTANT**: This project is under active development and should be
considered a work-in-progress.

#### Contents

* [About](#about-)
* [Usage](#usage-)
  * [Versioned Service API](#versioned-service-api-)
  * [Sample Model](#sample-model-)
* [Structure](#structure-)
* [Development](#development-)
* [Deployment](#deployment-)
  * [Testing & Staging](#testing--staging-)
  * [Production](#production-)
* [License](#license-)

## About [&#x219F;](#contents)

The Land Change Monitoring Assessment and Projection (LCMAP) system will
provide the science community with real-time access to decades of Landsat
Analysis Ready Data (ARD). To make this system available to the public
scientific computing community, an API service will be provided. The
LCMAP REST service API is codified in this project and prototypes the
functionality necessary to support the public service.


## Documentation [&#x219F;](#contents)

The server code is slowly being updated with docstrings. Generating documentation for the server is available here:

* [http://usgs-eros.github.io/lcmap-rest/current](http://usgs-eros.github.io/lcmap-rest/current/)

However, it may be of more interest and use to view the client documentation for the LCMAP service:

* [http://usgs-eros.github.io/lcmap-client-docs/current](http://usgs-eros.github.io/lcmap-client-docs/current)


## Usage [&#x219F;](#contents)


### Versioned Service API [&#x219F;](#contents)

The LCMAP service version is selected via an ``Accept`` header, as demonstrated
with the following:

```bash
$ curl -v -X POST \
    -H "Accept: application/vnd.usgs.lcmap.v0.0+json" \
    -d "username=alice" \
    -d "password=secret" \
    http://localhost:1077/api/auth/login
```


### Sample Model [&#x219F;](#contents)

The LCMAP REST service provides a sample execution API for testing purposes.
To kick off a new job, you can run something like the following:

```bash
curl -v -X POST \
  -H "Accept: application/vnd.usgs.lcmap.v0.0+json" \
  -H "X-AuthToken: 3efc6475b5034309af00549a77b7a6e3" \
  -d "seconds=15" \
  -d "year=2016" \
  http://localhost:1077/api/models/sample/os-process
```


## Structure [&#x219F;](#contents)

The codebase for lcmap-rest is structured along the following lines:

* ``lcmap-rest.api`` - This contains all the code that actually returns HTTP
  response data. This is also where all the service routes are defined. Actual
  "business logic" is in the rest of the codebase. If you find yourself adding
  new features with more than just Ring repoonse and status calls, then you
  need to reorganize that logic someplace other than ``lcmap-rest.api``. Under
  the ``lcmap-rest.api`` namespace we have the following:
    * ``compatibility`` - supports alternate APIs, e.g., WMTS
    * ``data`` - resources for querying stored data
    * ``jobs`` - resources for submitting, checking, and updating jobs
    * ``models`` - model execution resources
    * ``notifications`` - subscribtion and notification resources
    * ``operations`` - resources for chaining REST calls in a manner analgous
      to that used in functional programming languages (composition, map,
      reduce, etc.)
    * ``users`` - resources for user data
* ``lcmap-rest.auth`` - Logic for logging in and out of the LCMAP system
* ``lcmap-rest.components`` - This is where all major parts of the system are
  defined so that they may be easily started and stopped in the correct order.
  Example components are application configuration, logging, database
  connections, event services, and the REST services HTTP daemon.
* ``lcmap-rest.dev`` - The default module for development mode; loaded
  automatically when one executes ``lein repl``
* ``lcmap-rest.exceptions`` - Custom LCMAP exceptions
* ``lcmap-rest.job`` - Job tracking business logic
* ``lcmap-rest.logger`` - Logging support setup
* ``lcmap-rest.user`` - User management logic and storage functions
* ``lcmap-rest.util`` - Utility functions


## Development [&#x219F;](#contents)

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


## Deployment [&#x219F;](#contents)

TBD


### Testing & Staging [&#x219F;](#contents)

TBD


### Production [&#x219F;](#contents)

TBD


# License [&#x219F;](#contents)

Copyright © 2015 United States Government

NASA Open Source Agreement, Version 1.3
