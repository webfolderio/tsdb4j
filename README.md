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

Example
------

```java
public static void main(String[] args) {
  Path path = createTempDirectory("tsdb4j");
  Database database = new Database(path, "example");
  // create & open database
  database.create(1, Database.VOLUME_MIN_SIZE, true);
  if (!database.open()) {
    database.close();
      throw new RuntimeException("db open failed!");
    }
  Instant now = now();
  try (Session session = database.createSession()) {
  // insert dummy data
  session.add(now.plusSeconds(1), "cpu.usage location=Tallinn", 20.10D);
  session.add(now.plusSeconds(2), "cpu.usage location=Tallinn", 21.20D);
  session.add(now.plusSeconds(3), "cpu.usage location=Tallinn", 22.30D);
  session.add(now.plusSeconds(4), "cpu.usage location=Tallinn", 23.40D);
  // build select criteria
  SelectCriteria select = SelectCriteria.builder()
                                          .select("cpu.usage")
                                          .from(now)
                                          .to(now.plusSeconds(5))
                                        .build();
  List<String[]> list = new ArrayList<>();
  // query data
  try (SimpleCursor cursor = session.query(select)) {
    while (cursor.hasNext()) {
      String series = cursor.next();
      Tag tag = cursor.getTags().get(0);
      list.add(new String[] {
                series,
                cursor.getMetric(),
                valueOf(cursor.getValue()),
                tag.getName(),
                tag.getValue(),
                cursor.getTimestampAsInstant().toString()
              });
            }
          }
      System.out.println(of(new String[] {
          "Series", "Metric", "Value", "Tag Name", "Tag Value", "Timestamp"
        }, list.toArray(new String[][] { })));
     }
  database.close();
  database.delete();
  deleteIfExists(path);
}
```

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