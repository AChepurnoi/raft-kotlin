# raft-kotlin 

[![Build Status](https://travis-ci.com/AChepurnoi/raft-kotlin.svg?token=dFANEVvUn3HF3pZ9jc1Z&branch=master)](https://travis-ci.com/AChepurnoi/raft-kotlin)
[![codecov](https://codecov.io/gh/AChepurnoi/raft-kotlin/branch/master/graph/badge.svg?token=CLFO7pW9FP)](https://codecov.io/gh/AChepurnoi/raft-kotlin)

![picture](https://raft.github.io/logo/annie-solo.png)

Kotlin implementation of raft consensus algorithm 

## 🗄Project structure
### ◎ key-value-example
Example of how raft module can be used to 
implement distributed key-value storage.
Current implementation exposes two endpoints:
* POST /{key} - Sets `key={request_body}`
* GET /{key} - Returns value of the `key` or `Nil` if key does not exist

### ◎ raft
Raft implementation. 
Exposes `RaftNode` class for clients to create a cluster node, 
actions to mutate state of the cluster 
and method to fetch current state.

## 🔨Raft Implementation
Components:
* State 
* Log
* GRPC Client/Server
* Clock
* Actions
* Raft Controller

## ⚙️Testing
* Unit tests - `actions`, `clock`, `log`, `state` and `RaftController` classes are tested 
* Integration tests - `RaftClusterTesting` class contains different test cases for living cluster (With `LocalRaftNode` instead of `GrpcClusterNode`)
* Key-Value container testing - kv cluster testing - `Not implemented yet`

## 🔗References

[Instructors-guide-to-raft](https://thesquareplanet.com/blog/instructors-guide-to-raft/)

[Students-guide-to-raft](https://thesquareplanet.com/blog/students-guide-to-raft/)

[Raft visualization](http://thesecretlivesofdata.com/raft/)

[Raft resources](https://raft.github.io/)

## ⛳️Points to improve
*This is not production ready implementaton and most likely there are some amount of hidden bugs*

* Refactoring
* Revisit `@Volatile` and Mutex usages
* Implement persistent log storage
* Implement snapshoting