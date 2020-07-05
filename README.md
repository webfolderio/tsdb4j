## What is tsdb4j

tsdb4j is a Java driver for [Akumuli](https://github.com/akumuli/Akumuli) which makes time-series fast and easy.

## Features

* **Minimal third-party dependency**: tsdb4j avoids external dependencies except [Akumuli](https://github.com/akumuli/Akumuli) and [nanojson](https://github.com/mmastrac/nanojson).

* **Multiplatform**: tsdb4j supports Windows, MacOS and Linux (Centos, Ubuntu, Alpline).

* **Zero Management, No Learning Curve**: It takes only seconds to download, install, and run it successfully.

Supported Java Versions
-----------------------

Oracle/OpenJDK, GraalVM & Substrate VM.
Both the JRE and the JDK are suitable for use with this library.

__Note__: We only support LTS versions (8 & 11).

Supported Platforms
-------------------
tsdb4j has been tested under Windows, Ubuntu, Centos, Alpline and MacOS.

How it is tested
----------------
tsdb4j is regularly built and tested on Windows, Linux and MacOS.

License
-------
Licensed under the [Apache License](https://github.com/webfolderio/tsdb4j/blob/master/LICENSE).

Dependencies
------------

### Integration with libakumuli

tsdb4j communicates with Akumuli (libakumuli) via JNI and neither use TCP, UDP or HTTP protocol.

### Java Dependencies
[nanojson 1.6](https://github.com/mmastrac/nanojson) - library size: 29 KB

### Native Dependencies
[libakumuli 0.8.80](https://github.com/akumuli/Akumuli) - library size: 4 MB

### Statically Linked 
`tsdb4j.dll`, `tsdb4j.so` and `tsdb4j.dylib` staticly linked with Boost, apr, apr-util and sqlite3. Unlike libakumuli it's not required to install third-party dependicies with `yum` or `apt-get`.
