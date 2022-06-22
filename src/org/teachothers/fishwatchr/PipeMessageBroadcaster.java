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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


public class PipeMessageBroadcaster implements Runnable {
	
	public static final int ERROR_PATH_ALREADY_USED = 1;
	public static final int ERROR_INTERRUPTED = 2;
	public static final int ERROR_UNABLE_TO_RESERVE = 3;
	private static final long RESERVER_WAIT = 1500; // ms

	ExecutorService poolMessageAgent;
	DataPiper pipe;
	String pipeHost;
	String path;
	int nSender;
	PipeMessage message;
	PipeMessageSender senders[];
	Consumer<PipeMessage> messageConsumer;
	Consumer<Integer> errorConsumer;
	ConcurrentHashMap<String, PipeMessage> messageMap = new ConcurrentHashMap<String, PipeMessage>();
	
	
	public PipeMessageBroadcaster(DataPiper pipe, String path, PipeMessage message, Consumer<PipeMessage> messageConsumer, Consumer<Integer> errorConsumer) {
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
	public void run() {
		BlockingQueue<Future<?>> queue = new ArrayBlockingQueue<>(nSender);
		
		// Reserve the path
		var reserver = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
				public Void call() throws IOException, URISyntaxException, InterruptedException {
					pipe.reservePath(path);
					// reservePath() ends if the path has been already reserved
					errorConsumer.accept(ERROR_PATH_ALREADY_USED);
					return null;
				}
			});
		
		try {
			reserver.get(RESERVER_WAIT, TimeUnit.MILLISECONDS);
			// reservePath() ends if the path has been already reserved
			return;
		} catch (InterruptedException | ExecutionException e) {
			errorConsumer.accept(ERROR_UNABLE_TO_RESERVE);
			pipe.releasePath(path);
			reserver.cancel(true);
			poolMessageAgent.shutdownNow();
			e.printStackTrace();
			return;
		} catch (TimeoutException e) {
			System.err.println("reserved!!");
		}



		for(int i = 0; i < nSender; i++) {
			senders[i] = new PipeMessageSender(String.valueOf(i));
			var f = poolMessageAgent.submit(senders[i]);
			queue.add(f);
		}

		for (Future<?> f : queue) {
			try {
				f.get();
				break;
			} catch (InterruptedException | ExecutionException e) {
				break;
			}
		}
		
		System.err.println("PipeMessageBroadcaster is shutdown.");
		pipe.releasePath(path);
		poolMessageAgent.shutdownNow();
		reserver.cancel(true);
	}
	
	
	public PipeMessage getMap(String key) {
		return messageMap.get(key);
	}
	
	
	public PipeMessage setMap(String key, PipeMessage message) {
		return messageMap.put(key, message);
	}
	
	
	public boolean isMapped(String key) {
		return messageMap.containsKey(key);
	}
	
	
	public int getSize() {
		return messageMap.size();
	}

	
	
	private class PipeMessageSender implements Runnable {
		private String suffix;
		private boolean loopFlag = true;
		
		public PipeMessageSender(String suffix) {
			this.suffix = suffix;
		}
		
		@Override
		public void run()  {

			while(loopFlag) {
				try {
					PipeMessage newMessage = new PipeMessage(
							message.getSenderName(),
							DataPiper.generatePath(message.getSenderName()+path));
					newMessage.setStatus(message.getStatus());
					
					int errorCode = pipe.postMessage(path + suffix, newMessage);
					if(errorCode > 0) {
						loopFlag = false;
						errorConsumer.accept(ERROR_PATH_ALREADY_USED);
					} else {
						messageConsumer.accept(newMessage);
					}
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// postMessage() closes connection internally
					loopFlag = false;
				}
			}
		}
	}
}

