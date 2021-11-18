package org.teachothers.fishwatchr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

public class PipeMessageResponser implements Callable<Long> {
	private DataPiper pipe;
	private String path;
	private PipeMessage message;
	private static ArrayBlockingQueue<Integer> idQueue = new ArrayBlockingQueue<Integer>(2, false, Arrays.asList(0,1));

	
	public PipeMessageResponser(DataPiper pipe, String path, PipeMessage message) {
		this.pipe = pipe;
		this.path = path;
		this.message = message;
	}

	public Long call() throws IOException, URISyntaxException, InterruptedException {
			int pathID = idQueue.poll();
			String newPath = path + pathID;
			System.err.println("wait:" + path + pathID);
			pipe.postMessage(newPath, message);
			idQueue.add(pathID);
			System.err.println("consume:" + path);
			return Thread.currentThread().getId();
	}

	
}
