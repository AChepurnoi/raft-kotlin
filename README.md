# raft-kotlin 

[![Build Status](https://travis-ci.com/AChepurnoi/raft-kotlin.svg?token=dFANEVvUn3HF3pZ9jc1Z&branch=master)](https://travis-ci.com/AChepurnoi/raft-kotlin)
[![codecov](https://codecov.io/gh/AChepurnoi/raft-kotlin/branch/master/graph/badge.svg?token=CLFO7pW9FP)](https://codecov.io/gh/AChepurnoi/raft-kotlin)
<img align="right" width="200" height="200" src="https://raft.github.io/logo/annie-solo.png">

Kotlin implementation of raft consensus algorithm 

Raft is a consensus algorithm that is designed to be easy to understand. It's equivalent to Paxos in fault-tolerance and performance

**Repository provides an example implementation and 
show-case usage with in-memory key-value storage**

## 👨‍💻 How to run (key-value example)
#### Building
```
#Cloning repository
git clone https://github.com/AChepurnoi/raft-kotlin.git

#Building jar file
./gradlew jar
```


#### Running docker environment
```
docker-compose up --build
```

To interact with cluster you can use cURL:

```
#Set test=hello
docker run --rm --net=raft-kt_default hortonworks/alpine-curl:3.1 curl --request POST --url node_one:8000/test --data 'hello' ; echo
```


```
#Read test value
docker run --rm --net=raft-kt_default hortonworks/alpine-curl:3.1 curl --request GET --url node_one:8000/test ; echo
```

#### Other

Key-value implementation uses `307 Redirect` to redirect requests from slaves to master. 

This require you to be able to resolve the IP from configuration (You should interact with HTTP server only from docker network e.g. you container)

Another option is to run jar files locally with proper env configuration
 
To read node list from env, raft env configuration uses the following notation:
```
NODES=[ID]:[HOST]:[PORT],[ID]:[HOST]:[PORT]...
```


## 🔑 Example App (key-value storage) Implementation
Shows the example of how raft module can be used to 
implement distributed in-memory key-value storage.

Current implementation exposes two endpoints:
```
# Set `key={request_body}
POST HOST/{key} 

#Returns value of the `key` or `Nil` if key does not exist
GET HOST/{key}
```


Key-value HTTP server uses `8000` port by default

## 🔨 Raft Implementation

Exposes `RaftNode` class for clients to create a cluster node, 
actions to mutate state of the cluster 
and method to fetch current state.


Components:
* State
* Log
* gRPC Client/Server
* Clock
* Actions
* Raft Controller

## ⚙️ Testing
* Unit tests - `actions`, `clock`, `log`, `state` and `RaftController` classes are tested 
* Integration tests - `RaftClusterTesting` class contains different test cases for living cluster (With `LocalRaftNode` instead of `GrpcClusterNode`)
* Key-Value container testing - kv cluster testing - `Not implemented yet`


## ⛳️ Points to improve
*This is not production ready implementaton and very likely there are bugs*

* Refactoring
* Revisit `@Volatile` and Mutex usages
* Implement persistent log storage
* Implement snapshoting

## 🔗 References

[Instructors-guide-to-raft](https://thesquareplanet.com/blog/instructors-guide-to-raft/)

[Students-guide-to-raft](https://thesquareplanet.com/blog/students-guide-to-raft/)

[Raft visualization](http://thesecretlivesofdata.com/raft/)

[Raft resources](https://raft.github.io/)