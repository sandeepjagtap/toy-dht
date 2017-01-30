package com.tw.ds;

import akka.actor.*;
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
        this.actorPathStr = serializedActorPath.replaceFirst("#.*","" );

    }

    @Override
    public void onReceive(Object message) throws Throwable {

        ShardOperationsHandler.handleSharding(this, message);
        CacheOperationsHandler.handlePutAndGet(this, message);
    }

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


     static class CacheOperationsHandler {

        static void handlePutAndGet(AppActor appActor, Object message) {

            if (message instanceof PutMessage) {
                PutMessage putMessage = (PutMessage) message;
                ActorPath actorPath = appActor.findShard(putMessage.getKey());

                if (actorPath != null) {
                    if (actorPath == appActor.getSelf().path()) {
                        appActor.partitionedMap.put(putMessage.getKey(), putMessage.getValue());
                        System.out.println(appActor.partitionedMap);
                    } else {
                        System.out.println("putMessage = " + putMessage);
                        appActor.getContext().actorSelection(actorPath).tell(message, appActor.self());
                    }
                }

            }

            if (message instanceof ReadMessage) {
                ReadMessage readMessage = (ReadMessage) message;
                ActorPath actorPath = appActor.findShard(readMessage.getKey());
                if (actorPath == appActor.getSelf().path()) {
                    Integer value = appActor.partitionedMap.get(readMessage.getKey());
                    appActor.getSender().tell(new ReadMessageAnswer(readMessage.getKey(), value), appActor.self());
                } else {
                    appActor.getContext().actorSelection(actorPath).tell(message, appActor.getSender());
                }
            }
        }

     }

     static class ShardOperationsHandler {

        static void handleSharding(AppActor appActor, Object message) {

            if (message instanceof ActorIdentity) {
                ActorIdentity identity = (ActorIdentity) message;
                String path = identity.correlationId().toString();
                ActorSelection actorSelection = appActor.getContext().actorSelection(path);
                if (identity.getRef() == null) {
                    actorSelection.tell(new Identify(path), appActor.getSelf());
                } else {
                    appActor.getContext().watch(identity.getRef());
                    actorSelection.tell(new ShardMessage(appActor.startKey, appActor.endKey), appActor.getSelf());
                }
            }

            if (message instanceof ShardMessage) {
                ShardMessage shardMessage = (ShardMessage) message;
                Shard shard = new Shard(shardMessage.getStartKey(), shardMessage.getEndKey());
                appActor.shardMap.put(shard, appActor.getSender().path());
                System.out.println("ShardMap is = " + appActor.shardMap);
            }

            if (message instanceof Terminated) {

            }

        }
    }

}
