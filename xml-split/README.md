# xml-split
Using StAX to split xml file by tag name.
* **jvm**: the built-in library provided by JVM
* **woodstox**: the implementation from FasterXML ([code](https://github.com/FasterXML/woodstox))

## Usage
```bash
$ java -jar xml-split-jar-with-dependencies.jar \
    --input <input_xml> \
    --output <output_xml> \
    --tag <tag_name>
```

## Performance
For 4m records (file size: 1.2GB), reducing 8% time when using **woodstox** lib.
* **--impl jvm**: 6 seconds
* **--impl woodstox**: 5.5 seconds