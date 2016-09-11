[![Build Status](https://travis-ci.org/BatchIQ/nifi-executescript-samples.svg)](https://travis-ci.org/BatchIQ/nifi-executescript-samples)

# NiFi ExecuteScript Samples
This repo contains samples scripts for use with Apache NiFi's ExecuteScript processor.
Additionally, the repo may be cloned and modified to unit test custom scripts using NiFi's mock framework.

## Sample Scripts
Scripts are designed to demonstrate basic techniques in various languages:

| Topic | Javascript | Python |
| :--- | :--- | :--- |
| Reading and writing flowfile attributes | [attributes.js](src/test/resources/javascript/attributes.js) | [attributes.py](src/test/resources/python/attributes.py) |
| Logging | [log.js](src/test/resources/javascript/log.js) | [log.py](src/test/resources/python/log.py) |
| Transforming an input flowfile to a single output | [transform.js](src/test/resources/javascript/transform.js) | [transform.py](src/test/resources/python/transform.py) |
| Splitting an input flowfile to multiple outputs | [split.js](src/test/resources/javascript/split.js) | [split.py](src/test/resources/python/split.py) |
| Writting to counter metrics | [counter.js](src/test/resources/javascript/counter.js) | [counter.py](src/test/resources/python/counter.py) |

## Contributing
Please help.  These sample scripts are very likely to be buggy, unnecessarily complicated, misguided, downright stupid, or all of the above.
Please open an issue for bugs and new contributions.

## License
Apache License 2.0
