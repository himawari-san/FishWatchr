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


public class PipeMessageReceiver implements Callable<PipeMessage> {

	ExecutorService poolMessageAgent;
	DataPiper pipe;
	String pipeHost;
	String path;
	int nWaiter;
	PipeMessageWaiter waiters[];
	Consumer<PipeMessage> messageConsumer;
	Consumer<Exception> errorConsumer;
	ConcurrentHashMap<String, PipeMessage> messageMap = new ConcurrentHashMap<String, PipeMessage>();
	
	
	public PipeMessageReceiver(DataPiper pipe, String path, Consumer<PipeMessage> messageConsumer, Consumer<Exception> errorConsumer) {
		this.pipe = pipe;
		this.path = path;
		this.messageConsumer = messageConsumer;
		this.errorConsumer = errorConsumer;
		nWaiter = pipe.getnPathSuffix();
		waiters = new PipeMessageWaiter[nWaiter];
		poolMessageAgent = Executors.newFixedThreadPool(nWaiter);
	}


	@Override
	public PipeMessage call() {
		BlockingQueue<Future<PipeMessage>> queue = new ArrayBlockingQueue<>(nWaiter);

		for(int i = 0; i < nWaiter; i++) {
			waiters[i] = new PipeMessageWaiter(String.valueOf(i));
			var f = poolMessageAgent.submit(waiters[i]);
			queue.add(f);
		}

		PipeMessage message = null;
		for (Future<PipeMessage> f : queue) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				System.err.println("PipeMessageReceiver is shutdown.");
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

	
	
	private class PipeMessageWaiter implements Callable<PipeMessage> {
		private String suffix;
		private boolean loopFlag = true;
		
		public PipeMessageWaiter(String suffix) {
			this.suffix = suffix;
		}
		
		@Override
		public PipeMessage call()  {
			PipeMessage message = new PipeMessage();

			while(loopFlag) {
				try {
					message = pipe.getMessage(path + suffix);
//					setMap(message.getSenderName(), message);

					if(message.getType() == PipeMessage.TYPE_NORMAL) {
						messageConsumer.accept(message);
					}
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					stop();
					System.err.println("ps: close connection:" + path + suffix);
					// close the connection
					try {
						pipe.postMessage(path + suffix, new PipeMessage());
					} catch (URISyntaxException | IOException | InterruptedException e1) {
						System.err.println("Error: Can't stop getMessage to " + path + suffix);
						e1.printStackTrace();
					}
				}
			}

			return message;
		}
		
		
		public void stop() {
			loopFlag = false;
		}
	}
}

