# LCMAP REST Service

## About

TBD

## Usage

### Development

Start the REST server and dependent components with the following:

```bash
$ lein trampoline run
```

(The trampoline lein task is needed in order to properly handle ``SIGINT`` and ``SIGTERM``.)

### Production

TBD

## Versioned Service API

The LCMAP service version is selected via an ``Accept`` header, as demonstrated
with the following:

```bash
$ curl -H "Accept: application/vnd.usgs.lcmap.v0.0+json" \
    http://localhost:8080/api/L1/T/Landsat/8/SurfaceReflectance
```
