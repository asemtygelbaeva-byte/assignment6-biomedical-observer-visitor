# Assignment 6 Biomedical Observer and Visitor

Assem Tugelbay | SE-2540 | Variant 2

Synthetic ECG, PPG and EEG sources notify typed subscribers.
A patient subtree is inspected when an anomaly event arrives.

Requires Java 17 and Maven.

```sh
sh run.sh
mvn test
```

This baseline includes four visitors, five sample listeners and a four-thread stress test.
Separate commits will demonstrate adding a visitor and adding an element type.
