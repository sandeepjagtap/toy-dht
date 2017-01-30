package com.tw.ds;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;

public class App {

    public static void main(String[] args) {
        System.out.println("app name = " + args[0]);

        System.out.println("start key = " + args[1]);
        System.out.println("end key = " + args[2]);

        System.out.println("otherApp1Port = " + args[3]);
        System.out.println("otherApp2Port = " + args[4]);
        System.out.println("otherApp3Port = " + args[5]);

        ActorSystem appSystem = ActorSystem.create("appSystem", ConfigFactory.load((args[0])));
        Object port = appSystem.provider().getDefaultAddress().port().get();

        System.out.println("port = " + port);


        ActorRef appActor = appSystem.actorOf
                (Props.create(AppActor.class, args[1], args[2], Arrays.asList(args[3], args[4], args[5])), "appActor");

        System.out.println("appActor = " + appActor.path());



    }


}
