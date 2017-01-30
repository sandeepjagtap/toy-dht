package com.tw.ds;

import akka.actor.*;
import akka.japi.Procedure;
import akka.serialization.Serialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppActor extends UntypedActor {


    private Map<Shard, ActorPath> shardMap = new HashMap<>();
    private int startKey;
    private int endKey;

    private Map<Integer, Integer> partitionedMap = new HashMap<>();

    private final String actorPathStr;


    public AppActor(String startKey, String endKey, List<String> otherAppPorts) {

        this.startKey = Integer.parseInt(startKey);
        this.endKey = Integer.parseInt(endKey);

        shardMap.put(new Shard(this.startKey, this.endKey), getSelf().path());

        otherAppPorts.forEach(port -> {
            String path = "akka.tcp://appSystem@127.0.0.1:" + port + "/user/appActor";
            getContext().actorSelection(path).
                    tell(new Identify(path), getSelf());
        });

        String serializedActorPath = Serialization.serializedActorPath(getSelf());
        this.actorPathStr = serializedActorPath.replaceFirst("#.*", "");

    }

    @Override
    public void onReceive(Object message) throws Throwable {

        if (message instanceof ActorIdentity) {
            ActorIdentity identity = (ActorIdentity) message;
            String path = identity.correlationId().toString();
            ActorSelection actorSelection = getContext().actorSelection(path);
            if (identity.getRef() == null) {
                actorSelection.tell(new Identify(path), getSelf());
            } else {
                actorSelection.tell(new ShardMessage(startKey, endKey), getSelf());
            }
        } else if (message instanceof ShardMessage) {
            ShardMessage shardMessage = (ShardMessage) message;
            Shard shard = new Shard(shardMessage.getStartKey(), shardMessage.getEndKey());
            shardMap.put(shard, getSender().path());
            System.out.println("ShardMap is = " + shardMap);
            if (shardMap.keySet().size() == 4) {
                getSelf().tell(new Status.Success(""), getSelf());
            }
        } else if (message instanceof Status.Success) {
            getContext().become(cacheOperationsHandler);

        } else {

        }

    }


    Procedure<Object> cacheOperationsHandler = new Procedure<Object>() {

        @Override
        public void apply(Object message) throws Exception {
            if (message instanceof PutMessage) {
                PutMessage putMessage = (PutMessage) message;
                ActorPath actorPath = findShard(putMessage.getKey());

                if (actorPath != null) {
                    if (actorPath == getSelf().path()) {
                        partitionedMap.put(putMessage.getKey(), putMessage.getValue());
                        System.out.println(partitionedMap);
                    } else {
                        getContext().actorSelection(actorPath).tell(message, getSelf());
                    }
                }
            } else if (message instanceof ReadMessage) {
                ReadMessage readMessage = (ReadMessage) message;
                ActorPath actorPath = findShard(readMessage.getKey());
                if (actorPath == getSelf().path()) {
                    Integer value = partitionedMap.get(readMessage.getKey());
                    ActorRef sender = getSender();
                    ReadMessageAnswer msg = new ReadMessageAnswer(readMessage.getKey(), value);
                    ActorRef self = self();
                    sender.tell(msg, self);
                } else {
                    getContext().actorSelection(actorPath).tell(message, getSender());
                }
            }
        }
    };


    private ActorPath findShard(int key) {
        Set<Shard> shards = shardMap.keySet();
        try {
            ActorPath actorPath = shardMap.get(shards.stream().filter(shard -> shard.wraps(key)).findAny().get());
            System.out.println("actorPath of Shard is = " + actorPath);
            return actorPath;
        } catch (java.util.NoSuchElementException e) {
            return null;
        }

    }


}
