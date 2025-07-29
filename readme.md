
I have defined custom annotation for logs and for entity delete
Also implemented thread safe saveOrUpdate by id based lock in java side(could do same in db on row level but prefered java for demo)
Also tested it thread safety by Thread Pool Executor and caught a case
Using Transactional after lock and in another class is on purpose to to solve above problem  

Annotation and AOP : https://github.com/yusufunlu/Java-Spring-Features/tree/main/src/main/java/com/yusufu/interviewproject/annotation
Concurrent save/update : https://github.com/yusufunlu/Java-Spring-Features/blob/main/src/main/java/com/yusufu/interviewproject/service/ProductServiceImpl.java#L44
Thread pool executor to force concurrency : https://github.com/yusufunlu/Java-Spring-Features/blob/main/src/test/java/com/yusufu/interviewproject/ProductServiceIntegrationTest.java#L39
Application Events : https://github.com/yusufunlu/Java-Spring-Features/tree/main/src/main/java/com/yusufu/interviewproject/event

Following features implemented or will be implemented
1. create a annotation
2. use controller methods by ResponseEntity
3. use ResponseStatus in exceptions 
4. use central exception handler
5. split project to multiple config and contexes
6. Spring Security Integration
7. check json produce and consume
8. different security types
9. use record
10. idompotent
11. native query
12. transactional
13. batch operation
14. use threads
15. stream apis
16. locks
17. websocket
18. kafka
19. distributed lock on redis or ignite
20. dockerise
21. Thread pool limits and bean definitions based on env(windows or lower level envs)
22. gradle scripts for tests, dockerise, deploy
23. Kafka in test without real kafka setup
24. rpc, rmi, stub demos and grpc or protobuff integration
25. secrets store in vaults or spring cloud
26. service discovery
27. compare to Jhipster features
28. flyway schema migration scripts
29. users and security roles





disabling bsic auth and security or using ResponseEntity doesn't solve whitelable
