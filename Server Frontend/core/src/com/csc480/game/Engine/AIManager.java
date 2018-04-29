package com.csc480.game.Engine;

import com.badlogic.gdx.utils.Json;
import com.csc480.game.Engine.Model.AI;
import com.csc480.game.Engine.Model.Board;
import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class AIManager {
    Socket managerSocket;
    AI theAI[];
    private int state;


    public AIManager(){
        theAI = new AI[4];
        try{
            connectSocket();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        state = 0;
        for(int i = 0; i < 4; i++){
            theAI[i] = new AI();
        }
    }

    public void connectSocket(){
        try{
            if(managerSocket != null){
                managerSocket.disconnect();
            }

            managerSocket = null;
            managerSocket = IO.socket("http://localhost:3000");
            managerSocket.connect();
        } catch (URISyntaxException e){
            System.err.println(e);
        }
    }

    public void reconnectSocket(){
        try{
            /*IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = false;
            managerSocket = IO.socket("http://localhost:3000");*/
            managerSocket.connect();
            //upateAI();
        } catch (Exception e){
            System.err.println(e);
        }
    }

    public void updateAI(){
        for(AI a: theAI){
            a.update();
        }
    }

    public void socketEvents(){
        managerSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Connected AIManager to backend");
                //updateAI();
                //print on connection
            }
        }).on("whoAreYou", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject response = new JSONObject();
                response.put("isSF", true);
                managerSocket.emit("whoAreYou", response);
                System.out.println("Received and sent 'whoAreYou'");
                for(AI a: theAI){
                    if(a.state == 2){
                        a.state = 1;
                    }
                }
                updateAI();
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("AIManager disconnected, attempting reconnect");
                reconnectSocket();
            }
        }).on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("AIManager reconnecting...");
            }
        }).on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("reconnect attempt");
            }
        }).on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("reconnect failed");
            }
        }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect timeout AI MANAGER");
            }
        }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("AIManager reconnect");
                /*for(int i = 0; i < 4; i++){
                    theAI[i] = new AI();
                }*/
            }
        }).on("removeAI", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("AIManager got removeAI");
                try{
                    JSONObject data = (JSONObject) args[0];
                    int position = data.getInt("position");
                    if(theAI[position] != null){
                        //theAI[position].disconnectAI();
                        theAI[position].playerSpot = true;
                        theAI[position].mySocket.disconnect();
                        theAI[position].mySocket = null;
                    }
                    //theAI[position] = null;
                } catch(ArrayIndexOutOfBoundsException e){
                    System.err.println(e);
                }
            }
        }).on("connectAI", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Reconnecting an AI");
                try {
                    JSONObject data = (JSONObject) args[0];
                    System.out.println(data.toString());
                    int position = data.getInt("position");
                    //reconnect an AI
                    theAI[position].ReConnectSocket();
                    theAI[position].playerSpot = false;
                } catch (ArrayIndexOutOfBoundsException e){
                    System.err.println(e);
                }
            }
        }).on("updateState", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray players = data.getJSONArray("players");
                    for (int i = 0; i < theAI.length; i++) {
                        JSONObject player = (JSONObject) players.get(i);
                        int index = player.getInt("position");
                        boolean isAI;
                        try {
                            /*isAI = player.getBoolean("isAI");
                            if(isAI && theAI[i] == null){
                                theAI[i] = new AI();
                            }
                            else if(!isAI){

                            }*/
                        } catch (JSONException e) {
                            //the
                            //isAI = true;
                            /*//todo reconnect an AI at that position
                            theAI[index].disconnectAI();
                            theAI[index] = null;
                            theAI[index] = new AI();*/
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

}
