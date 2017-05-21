# ispn-backend-poc
Cass vs ISPN PoC for Hawkular Alerting specific scenarios

## Context

Hawkular Alerting requires a backend to store trigger definitions, generated events/alerts and actions history.

In a preliminar phase Hawkular Alerting relied on a pure SQL design moved into a Cassandra backend.

Motivation to choose a Cassandra backend was focus to consistency and unified the backend usages within Hawkular project.

But main performance tasks of Hawkular Alerting are focus on detection anomalies rather on high intensive read/write from the backend.
 
So, in the context of a next light weight architecture of Hawkular Alerting, the backend decision could be revisite.
 
## ISPN 9

The motivation to study ISPN 9 as backend for Alerting can be summary on the following arguments:

- ISPN is currently part of the core architecture of Hawkular Alerting.
- ISPN 9 brings new off-heap features.
- Indexing engine features matches with Hawkular Alerting query use cases.
- ISPN 9 storage options makes interesting to delegate the backend (memory, filesystem, database) as part of final configuration.
  
## Preliminar testing

```
[1] Using ISPN with file async storage for data and indexes

Start ISPN Test
Loaded:20162ms
Filtered by ctime > 900000 and (trigger0 or trigger1): 20000
Queried:23335ms

[3] Cassandra backend for comparison    
    
Start Cassandra Test
Loaded:208539ms
Filtered by ctime > 900000 and (trigger0 or trigger1): 20000
Queried:212586ms  
```

So, the aim is not to compare ISPN vs Cassandra in general use cases, as it is clear that both technologies are different, 
but for specific use cases Alerting could get benefits of using ISPN 9 for storing definitions, events/alerts and actions history.
