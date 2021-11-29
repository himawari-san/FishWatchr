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


public class PipeMessageReceiver implements Callable<PipeMessage> {

	ExecutorService poolMessageReceiver;
	DataPiper pipe;
	String pipeHost;
	String path;
	int nWaiter;
	PipeMessageWaiter waiters[];
	Consumer<PipeMessage> messageConsumer;
	Consumer<Exception> errorConsumer;
	HashMap<String, PipeMessage> messageMap = new HashMap<String, PipeMessage>();
	
	
	public PipeMessageReceiver(DataPiper pipe, int nWaiter, String path, Consumer<PipeMessage> messageConsumer, Consumer<Exception> errorConsumer) {
		poolMessageReceiver = Executors.newFixedThreadPool(nWaiter);
		this.pipe = pipe;
		this.nWaiter = nWaiter;
		this.path = path;
		this.messageConsumer = messageConsumer;
		this.errorConsumer = errorConsumer;
		waiters = new PipeMessageWaiter[nWaiter];
		System.err.println("pmr!!!");
	}


	@Override
	public PipeMessage call() {
		BlockingQueue<Future<PipeMessage>> queue = new ArrayBlockingQueue<>(nWaiter);
		System.err.println("nwa:" + nWaiter);

		for(int i = 0; i < nWaiter; i++) {
			System.err.println("w:" + i);
			waiters[i] = new PipeMessageWaiter(String.valueOf(i), messageConsumer);
			var f = poolMessageReceiver.submit(waiters[i]);
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
	
	public PipeMessage getMap(String key) {
		return messageMap.get(key);
	}
	
	
	public PipeMessage setMap(String key, PipeMessage message) {
		return messageMap.put(key, message);
	}
	
	
	public int getSize() {
		return messageMap.size();
	}

	
	public void stop() {
		poolMessageReceiver.shutdown();
		poolMessageReceiver.shutdownNow();
//		for(Runnable waiter : poolMessageReciever.shutdownNow()) {
//			((Waiter)waiter).disconnect();
//		}
	}
	
	
	private class PipeMessageWaiter implements Callable<PipeMessage> {
		private String suffix;
		private boolean loopFlag = true;
		private Consumer<PipeMessage> messageConsumer;
		
		public PipeMessageWaiter(String suffix, Consumer<PipeMessage> messageConsumer) {
			this.suffix = suffix;
			this.messageConsumer = messageConsumer;
		}
		
		@Override
		public PipeMessage call()  {
			PipeMessage message = new PipeMessage();

			while(loopFlag) {
				try {
					message = pipe.getMessage(path + suffix);
					setMap(message.getSenderName(), message);

					if(message.getType() == PipeMessage.TYPE_NORMAL) {
						messageConsumer.accept(message);
					}
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

