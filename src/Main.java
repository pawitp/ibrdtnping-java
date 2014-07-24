import ibrdtn.api.APIException;
import ibrdtn.api.ExtendedClient;
import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.PayloadBlock;
import ibrdtn.api.object.SingletonEndpoint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {

	public static void main(String[] args) throws Exception {
		final ExtendedClient exClient = new ExtendedClient();

		// We need to perform all actions in this thread
		ExecutorService executor = Executors.newSingleThreadExecutor();

		exClient.setHandler(new MyHandler(exClient, executor));

		// Connect to the daemon
		exClient.open();

		// Get destination from args or use localhost
		String destination;
		if (args.length > 0) {
			destination = args[0];
		} else {
			destination = exClient.getNodeName().toString() + "/echo";
		}

		// Create bundle to send
		final Bundle msg = new Bundle(new SingletonEndpoint(destination), 3600); // 3600 = lifetime

		// Send "DTNPING" text
		// WARNING: Do not pass in String, otherwise there will be garbage at the beginning
		//          (convert to byte[])
		msg.appendBlock(new PayloadBlock("DTNPING".getBytes()));

		// Send!
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Sending ping message");
					exClient.send(msg);
				} catch (APIException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
