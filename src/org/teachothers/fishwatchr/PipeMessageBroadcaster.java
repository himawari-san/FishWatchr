package org.teachothers.fishwatchr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


public class PipeMessageBroadcaster implements Callable<PipeMessage> {
	public static final String SUFFIX_SENDER_PATH = "_waiter";
	public static final String SUFFIX_FIRST_SENDER_PATH = SUFFIX_SENDER_PATH + "0";

	ExecutorService poolMessageAgent;
	DataPiper pipe;
	String pipeHost;
	String path;
	int nSender;
	PipeMessage message;
	PipeMessageSender senders[];
	Consumer<PipeMessage> messageConsumer;
	Consumer<Exception> errorConsumer;
	HashMap<String, PipeMessage> messageMap = new HashMap<String, PipeMessage>();
	
	
	public PipeMessageBroadcaster(DataPiper pipe, int nSender, String path, PipeMessage message, Consumer<PipeMessage> messageConsumer, Consumer<Exception> errorConsumer) {
		poolMessageAgent = Executors.newFixedThreadPool(nSender);
		this.pipe = pipe;
		this.nSender = nSender;
		this.path = path;
		this.message = message;
		this.messageConsumer = messageConsumer;
		this.errorConsumer = errorConsumer;
		senders = new PipeMessageSender[nSender];
	}


	@Override
	public PipeMessage call() {
		BlockingQueue<Future<PipeMessage>> queue = new ArrayBlockingQueue<>(nSender);
		System.err.println("nwa:" + nSender);

		for(int i = 0; i < nSender; i++) {
			senders[i] = new PipeMessageSender(i);
			var f = poolMessageAgent.submit(senders[i]);
			queue.add(f);
		}

		PipeMessage message = null;
		for (Future<PipeMessage> f : queue) {
			try {
				f.get();
				break;
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return message;
	}
	
	
	public PipeMessage getMap(String name) {
		return messageMap.get(name);
	}
	
	
	public int getSize() {
		return messageMap.size();
	}

	
	public void stop() {
		poolMessageAgent.shutdown();
		poolMessageAgent.shutdownNow();
//		for(Runnable waiter : poolMessageReciever.shutdownNow()) {
//			((Waiter)waiter).disconnect();
//		}
	}
	
	
	private class PipeMessageSender implements Callable<PipeMessage> {
		private int id;
		private boolean loopFlag = true;
//		private Consumer<PipeMessage> messageConsumer;
		
		public PipeMessageSender(int id) {
			this.id = id;
//			this.message = message;
//			this.messageConsumer = messageConsumer;
		}
		
		@Override
		public PipeMessage call()  {

			while(loopFlag) {
				try {
					PipeMessage newMessage = new PipeMessage(
							message.getSenderName(),
							DataPiper.generatePath(message.getSenderName()+path));
					System.err.println("m:" + newMessage.toString());
					pipe.postMessage(path + SUFFIX_SENDER_PATH + String.valueOf(id), newMessage);
					messageMap.put(message.getSenderName(), newMessage);
					messageConsumer.accept(newMessage);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// must close the path
					e.printStackTrace();
				} finally {
				}
			}

			return message;
		}
		
		
		public void stop() {
			loopFlag = false;
		}
	}
}

