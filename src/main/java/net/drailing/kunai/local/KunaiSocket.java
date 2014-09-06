package net.drailing.kunai.local;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import net.drailing.kunai.local.pojos.JoinRoomData;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;

public class KunaiSocket {

	private String ipAddress;
	private SocketIOServer server;

	public KunaiSocket(int port) throws UnknownHostException{
		
		ipAddress = InetAddress.getLocalHost().getHostAddress();
		
		Configuration config = new Configuration();
	    config.setHostname(ipAddress);
	    config.setPort(port);

	    server = new SocketIOServer(config);
	    server.addListeners(this);
	    
	}
	
	public void start(){
		server.start();
	}
	
	public void stop(){
		server.stop();
	}
	
	@OnEvent("joinRoom")
	public void onJoinRoomHandler(SocketIOClient client, JoinRoomData data, AckRequest ackRequest){
		
		if(data.r == null || data.t == null){
			logicError(client, "This is a server error, please reload your page.");
			return;
		}
		
		//if client reloads page, we leave room and join again later
		client.leaveRoom(data.r);
		
		//check if room has open slots
		Collection<SocketIOClient> roomClients = server.getRoomOperations(data.r).getClients();
		int count = roomClients.size();
		if(count >= 2){
			logicError(client, "Sorry, this room is currently in use.");
			return;
		}else if(count == 1){
			
			SocketIOClient check = roomClients.iterator().next();
			String type = check.get("type");
			if(type != null && type.equals(data.t)){
				logicError(client, "Sorry, this room is currently in use.");
				return;
			}
		}
		
		//client can join room 
		client.joinRoom(data.r);
		count++;
		client.set("room", data.r);
		client.set("type", data.t);
		
		client.sendEvent("successfulJoined", "");
		server.getRoomOperations(data.r).sendEvent("roomUpdate", count);
		
	}
	
	@OnEvent("submitFullText")
	public void onSubmitFullTextHandler(SocketIOClient client, String txt, AckRequest ackRequest){
		if(validateRoom(client.get("room"))){
			server.getRoomOperations(client.get("room")).sendEvent("receiveFullText", txt);
		}
	}
	
	@OnEvent("submitSingleChar")
	public void onSubmitSingleCharHandler(SocketIOClient client, String c, AckRequest ackRequest){
		if(validateRoom(client.get("room"))){
			server.getRoomOperations(client.get("room")).sendEvent("receiveSingleChar", c);
		}
	}
	
	@OnEvent("triggerEnter")
	public void onTriggerEnterHandler(SocketIOClient client, Object unused, AckRequest ackRequest){
		if(validateRoom(client.get("room"))){
			server.getRoomOperations(client.get("room")).sendEvent("receiveEnterTrigger", "");
		}
	}
	
	@OnEvent("triggerBack")
	public void onTriggerBackHandler(SocketIOClient client, Object unused, AckRequest ackRequest){
		if(validateRoom(client.get("room"))){
			server.getRoomOperations(client.get("room")).sendEvent("receiveBackTrigger", "");
		}
	}
	
	private boolean validateRoom(String roomName){
		return (server.getRoomOperations(roomName).getClients().size() == 2);
	}
	
	@OnConnect
	public void onConnectHandler(SocketIOClient client) {}

    @OnDisconnect
    public void onDisconnectHandler(SocketIOClient client) {
    	
    	if(client.get("room") == null || client.get("room").isEmpty()){
    		return;
    	}
    	
    	
    	client.leaveRoom(client.get("room"));
    	int count = server.getRoomOperations(client.get("room")).getClients().size();
    	
    	if(count < 2){
    		server.getRoomOperations(client.get("room")).sendEvent("roomUpdate", count);
    	}else{
    		//should never happen
    		logicError(client, "This is a server error, please reload your page.");
    	}
    	
    }
	
    private void logicError(SocketIOClient socket, String errorMsg){
    	socket.sendEvent("lError", errorMsg);
    }
}
