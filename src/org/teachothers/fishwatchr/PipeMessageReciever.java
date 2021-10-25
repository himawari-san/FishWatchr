package org.teachothers.fishwatchr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class PipeMessageReciever implements Callable<Long> {
	private DataPiper pipe;
	private String path;
	private Consumer<SimpleMessage> consumer;
	private static ArrayBlockingQueue<Integer> idQueue = new ArrayBlockingQueue<Integer>(2, false, Arrays.asList(0,1));

	
	public PipeMessageReciever(DataPiper pipe, String path, Consumer<SimpleMessage> consumer) {
		this.pipe = pipe;
		this.path = path;
		this.consumer = consumer;
	}

	public Long call() throws IOException, URISyntaxException, InterruptedException {
			int pathID = idQueue.poll();
			System.err.println("wait:" + path + pathID);
			consumer.accept(pipe.getMessage(path + pathID));
			idQueue.add(pathID);
			System.err.println("consume:" + path);
			return Thread.currentThread().getId();
	}

}
