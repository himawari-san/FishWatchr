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


public class PipeMessageReceiver implements Callable<Void> {

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
	public Void call() {
		BlockingQueue<Future<Void>> queue = new ArrayBlockingQueue<>(nWaiter);

		for(int i = 0; i < nWaiter; i++) {
			waiters[i] = new PipeMessageWaiter(String.valueOf(i));
			var f = poolMessageAgent.submit(waiters[i]);
			queue.add(f);
		}

		for (Future<Void> f : queue) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("PipeMessageReceiver is shutdown.");
				poolMessageAgent.shutdownNow();
				errorConsumer.accept(e);
			}
		}

		return null;
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

	
	
	private class PipeMessageWaiter implements Callable<Void> {
		private String suffix;
		private boolean loopFlag = true;
		
		public PipeMessageWaiter(String suffix) {
			this.suffix = suffix;
		}
		
		@Override
		public Void call()  {
			while(loopFlag) {
				try {
					PipeMessage message = pipe.getMessage(path + suffix);
					messageConsumer.accept(message);
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					loopFlag = false;
					// postMessage() closes connection internally
				}
			}

			return null;
		}
	}
}

