package net.drailing.kunai.local;

import java.net.UnknownHostException;

import net.drailing.kunai.local.ui.UI;

public class Main {

	public static void main(String[] args) {
		
		if(args.length == 2){
			
			try {
				KunaiSocket socket = new KunaiSocket(Integer.valueOf(args[1]));
				socket.start();
				
				KunaiWebServer webserver = new KunaiWebServer(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
				webserver.startServer();
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}

			
		}else{
			new UI();
		}
		
	}

}
