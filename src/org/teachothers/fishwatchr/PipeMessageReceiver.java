package org.teachothers.fishwatchr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


public class PipeMessageReceiver implements Runnable {
	
	public static final int ERROR_PATH_ALREADY_USED = 1;
	public static final int ERROR_INTERRUPTED = 2;
	public static final int ERROR_UNABLE_TO_RESERVE = 3;
	private static final long RESERVER_WAIT = 1500; // ms


	ExecutorService poolMessageAgent;
	DataPiper pipe;
	String pipeHost;
	String path;
	int nWaiter;
	PipeMessageWaiter waiters[];
	Consumer<PipeMessage> messageConsumer;
	Consumer<Integer> errorConsumer;
	ConcurrentHashMap<String, PipeMessage> messageMap = new ConcurrentHashMap<String, PipeMessage>();
	
	
	public PipeMessageReceiver(DataPiper pipe, String path, Consumer<PipeMessage> messageConsumer, Consumer<Integer> errorConsumer) {
		this.pipe = pipe;
		this.path = path;
		this.messageConsumer = messageConsumer;
		this.errorConsumer = errorConsumer;
		nWaiter = pipe.getnPathSuffix();
		waiters = new PipeMessageWaiter[nWaiter];
		poolMessageAgent = Executors.newFixedThreadPool(nWaiter);
	}


	@Override
	public void run() {
		BlockingQueue<Future<?>> queue = new ArrayBlockingQueue<>(nWaiter);
		
		var reserver = Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				try {
					// Keep reserving the path
					pipe.reservePath(path);
					
					// reservePath() ends if the path has been already reserved
					poolMessageAgent.shutdownNow();
					errorConsumer.accept(ERROR_PATH_ALREADY_USED);
				} catch (IOException | URISyntaxException  e) {
					poolMessageAgent.shutdownNow();
					errorConsumer.accept(ERROR_UNABLE_TO_RESERVE);
					e.printStackTrace();
				} catch (InterruptedException e) {
					errorConsumer.accept(ERROR_INTERRUPTED);
				}
			}
		});
		

		// Wait to confirm whether the path is reserved 
		try {
			Thread.sleep(RESERVER_WAIT);
		} catch (InterruptedException e1) {
			errorConsumer.accept(ERROR_INTERRUPTED);
			reserver.cancel(true);
			return;
		}

		for(int i = 0; i < nWaiter; i++) {
			waiters[i] = new PipeMessageWaiter(String.valueOf(i));
			var f = poolMessageAgent.submit(waiters[i]);
			queue.add(f);
		}

		for (Future<?> f : queue) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("PipeMessageReceiver is shutdown.");
				poolMessageAgent.shutdownNow();
				reserver.cancel(true);
			}
		}
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

	
	
	private class PipeMessageWaiter implements Runnable {
		private String suffix;
		private boolean loopFlag = true;
		
		public PipeMessageWaiter(String suffix) {
			this.suffix = suffix;
		}
		
		@Override
		public void run()  {
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
		}
	}
}

