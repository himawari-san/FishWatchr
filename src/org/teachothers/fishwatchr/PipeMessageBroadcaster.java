package org.teachothers.fishwatchr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


public class PipeMessageBroadcaster implements Callable<PipeMessage> {

	ExecutorService poolMessageAgent;
	DataPiper pipe;
	String pipeHost;
	String path;
	int nSender;
	PipeMessage message;
	PipeMessageSender senders[];
	Consumer<PipeMessage> messageConsumer;
	Consumer<Exception> errorConsumer;
	ConcurrentHashMap<String, PipeMessage> messageMap = new ConcurrentHashMap<String, PipeMessage>();
	
	
	public PipeMessageBroadcaster(DataPiper pipe, String path, PipeMessage message, Consumer<PipeMessage> messageConsumer, Consumer<Exception> errorConsumer) {
		this.pipe = pipe;
		this.nSender = pipe.getnPathSuffix();
		this.path = path;
		this.message = message;
		this.messageConsumer = messageConsumer;
		this.errorConsumer = errorConsumer;
		senders = new PipeMessageSender[nSender];
		poolMessageAgent = Executors.newFixedThreadPool(nSender);
	}


	@Override
	public PipeMessage call() {
		BlockingQueue<Future<Void>> queue = new ArrayBlockingQueue<>(nSender);

		for(int i = 0; i < nSender; i++) {
			senders[i] = new PipeMessageSender(String.valueOf(i));
			var f = poolMessageAgent.submit(senders[i]);
			queue.add(f);
		}

		PipeMessage message = null;
		for (Future<Void> f : queue) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("PipeMessageBroadcaster is shutdown.");
				poolMessageAgent.shutdown();
				poolMessageAgent.shutdownNow();
			}
		}
		
		return message;
	}
	
	
	public PipeMessage getMap(String key) {
		return messageMap.get(key);
	}
	
	
	public PipeMessage setMap(String key, PipeMessage message) {
		return messageMap.put(key, message);
	}
	
	
	public int getSize() {
		return messageMap.size();
	}

	
	
	private class PipeMessageSender implements Callable<Void> {
		private String suffix;
		private boolean loopFlag = true;
		
		public PipeMessageSender(String suffix) {
			this.suffix = suffix;
		}
		
		@Override
		public Void call()  {

			while(loopFlag) {
				try {
					PipeMessage newMessage = new PipeMessage(
							message.getSenderName(),
							DataPiper.generatePath(message.getSenderName()+path));
					pipe.postMessage(path + suffix, newMessage);
					messageConsumer.accept(newMessage);
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// postMessage() closes connection internally
					loopFlag = false;
				}
			}

			return null;
		}
	}
}

