package org.teachothers.fishwatchr;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


public class PipeMemberFinder implements Callable<Void> {
	public static final String SUFFIX_RESPONSER_PATH = "_responser";
	private static final int N_PIPE_WATCHER = 5;
	private static final int N_RESPONSER = 5;

	ExecutorService poolMessageReciever;
	ExecutorService poolMessageResponser;
	DataPiper pipe;
	String path;
	PipeMessage response;
	Consumer<PipeMessage> messageConsumer;
	Consumer<Exception> errorConsumer;
	HashMap<String, PipeMessage> messageMap = new HashMap<String, PipeMessage>();
	
	
	public PipeMemberFinder(DataPiper pipe, int nPool, String path, PipeMessage response, Consumer<PipeMessage> messageConsumer, Consumer<Exception> errorConsumer) {
		poolMessageReciever = Executors.newFixedThreadPool(nPool);
		poolMessageResponser = Executors.newFixedThreadPool(nPool);
		this.pipe = pipe;
		this.path = path;
		this.response = response;
		this.messageConsumer = messageConsumer;
		this.errorConsumer = errorConsumer;
	}


	@Override
	public Void call() {
		BlockingQueue<Future<Long>> queue = new ArrayBlockingQueue<>(10);

		for (int i = 0; i < N_PIPE_WATCHER; i++) {
			System.err.println("pathpw:" + path);
			PipeMessageReciever pw = new PipeMessageReciever(pipe, path, (message) -> {
				String messageID = message.getID();
				if (messageID.isEmpty()) {
					return;
				}
				System.err.println("mid:" + messageID);
				messageMap.put(messageID, message);
				messageConsumer.accept(message);
			});
			Future<Long> f = poolMessageReciever.submit(pw);
			queue.add(f);
		}

		for (int i = 0; i < N_RESPONSER; i++) {
			System.err.println("res path:" + path + SUFFIX_RESPONSER_PATH);
			PipeMessageResponser pm = new PipeMessageResponser(pipe, path + SUFFIX_RESPONSER_PATH, response);
			Future<Long> f = poolMessageResponser.submit(pm);
			queue.add(f);
		}

		for (Future<Long> f : queue) {
			try {
				Long id = f.get();
				System.err.println("fid:" + id);
			} catch (InterruptedException | ExecutionException e) {
//				pipe.cancel();
				poolMessageReciever.shutdownNow();
				poolMessageResponser.shutdownNow();
				errorConsumer.accept(e);
				e.printStackTrace();

				return null;
			}
		}

		poolMessageReciever.shutdownNow();
		poolMessageResponser.shutdownNow();

		return null;
	}
	
	
	public PipeMessage getMap(String name) {
		return messageMap.get(name);
	}
	
	
	public int getSize() {
		return messageMap.size();
	}

	
	public void stop() {
		poolMessageReciever.shutdownNow();
		poolMessageResponser.shutdownNow();
	}
	
}

