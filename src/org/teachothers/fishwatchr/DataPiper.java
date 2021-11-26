package org.teachothers.fishwatchr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;


public class DataPiper {
	public static final String MESSAGE_KEY_PATH = "path";
	public static final String MESSAGE_KEY_USERNAME = "username";
	public static final String MESSAGE_KEY_DATASIZE = "datasize";
	public static final String MESSAGE_KEY_TYPE = "type";
	public static final String MESSAGE_VALUE_TYPE_DISTRIBUTE = "distribute";
	
	public static final String KEY_VALUE_SEPARATOR = ":";
//	public static final int BASE_FILE_SIZE = 1024; // KB

	private static final int N_SCAN_PATH = 2;
	private static final int N_RETRY = 20;
	private static final int Data_BUFFER_SIZE = 1024 * 1024; // 1MB

	private BodyHandlerWrapper handlerWrapper;
	private String pipeServer;	
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

	public DataPiper(String pipeServer) {
		super();
		this.pipeServer = pipeServer.endsWith("/") ? pipeServer : pipeServer + "/";
	}



	public PipeMessage getMessage(String path) throws IOException, URISyntaxException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.build();

		System.err.println("call getm0:" + path);
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		
		System.err.println("call getm1:" + path);

		// print status code
		System.out.println(response.statusCode());

		// print response body
		System.out.println(response.body());

		BufferedReader in = new BufferedReader(new StringReader(response.body()));

		String line;
		StringBuffer lines = new StringBuffer();
		while ((line = in.readLine()) != null) {
			lines.append(line + "\n");
		}

		return PipeMessage.encode(lines.toString());
	}
	
	
	public PipeMessage getMessage(String path, Duration timeout) throws IOException, URISyntaxException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.timeout(timeout)
				.build();

        handlerWrapper = new BodyHandlerWrapper(HttpResponse.BodyHandlers.ofString());

		
		HttpResponse<String> response = httpClient.send(request, handlerWrapper);
		

		// print status code
		System.out.println(response.statusCode());

		// print response body
		System.out.println(response.body());

		BufferedReader in = new BufferedReader(new StringReader(response.body()));

		String line;
		StringBuffer lines = new StringBuffer();
		while ((line = in.readLine()) != null) {
			lines.append(line + "\n");
		}

		return PipeMessage.encode(lines.toString());
	}
	

	public void cancel() {
		System.err.println("hey cancel");
		handlerWrapper.cancel();
	}
	
	public PipeMessage getMessage2(String path) throws IOException, URISyntaxException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		// print status code
		System.out.println(response.statusCode());

		// print response body
		System.out.println(response.body());

		BufferedReader in = new BufferedReader(new StringReader(response.body()));

		String line;
		StringBuffer lines = new StringBuffer();
		while ((line = in.readLine()) != null) {
			lines.append(line + "\n");
		}

		return PipeMessage.encode(lines.toString());
	}
	
	
	public void postMessage(String path, PipeMessage message) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

	    HttpRequest request = HttpRequest.newBuilder()
	                .POST(HttpRequest.BodyPublishers.ofString(message.toString()))
	                .uri(pipeURL)
	                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
	                .build();
	    
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.err.println(response.statusCode());

        // print response body
        System.err.println(response.body());
	}

	
	public void postMessage(String path, PipeMessage message, Duration timeout) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

	    HttpRequest request = HttpRequest.newBuilder()
	                .POST(HttpRequest.BodyPublishers.ofString(message.toString()))
	                .uri(pipeURL)
	                .timeout(timeout)
	                .build();
	    
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.err.println(response.statusCode());

        // print response body
        System.err.println(response.body());
	}
	
	public void postFile(String path, Path filePath) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

	    HttpRequest request = HttpRequest.newBuilder()
	    			.uri(pipeURL)
	                .POST(HttpRequest.BodyPublishers.ofFile(filePath))
	                .build();
	    
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.err.println("post res:" + response.statusCode());

        // print response body
        System.err.println(response.body());
	}

	
	public void postFile(String path, Path[] filePaths, Consumer<Long> readLenth) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);
		
		try (
				PipedOutputStream pipeOut = new PipedOutputStream();
				TarArchiveOutputStream tarOut = new TarArchiveOutputStream(pipeOut);
				PipedInputStream pipeIn = new PipedInputStream(pipeOut)) {

			HttpRequest request = HttpRequest.newBuilder()
					.uri(pipeURL)
					.POST(HttpRequest.BodyPublishers.ofInputStream(() -> pipeIn))
					.build();

			Executors.newSingleThreadExecutor().submit(new Runnable() {
				@Override
				public void run() {
					for (Path filePath : filePaths) {
						try {
							tarOut.putArchiveEntry(new TarArchiveEntry(filePath.toFile(), filePath.toFile().getName()));
							BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(filePath));
							byte buf[] = new byte[Data_BUFFER_SIZE];
							long nr = 0;
							int len = 0;
							while((len = bis.read(buf)) != -1) {
								nr += len;
								tarOut.write(buf, 0, len);
								readLenth.accept(nr);
							}
							tarOut.closeArchiveEntry();
						} catch (IOException e) {
							System.err.println("Error(DataPiper): Can't create a tar file.");
							e.printStackTrace();
						}
					}
					try {
						tarOut.close();
						pipeOut.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		// print status code
			System.err.println("post res:" + response.statusCode());

			// print response body
			System.err.println(response.body());

		}
	}


	public void getTarFile(String pipePath, Path rootPath, Consumer<Long> readSize) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + pipePath);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.build();

		HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(response.body())) {
			TarArchiveEntry entry;
			byte buf[] = new byte[Data_BUFFER_SIZE];
			
			while((entry = tarInput.getNextTarEntry()) != null) {
				long nr = 0;
				Path filePath = rootPath.resolve(entry.getName());
//				readSize.accept("-" + entry.getName() + "\n");
				BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath));

				int readLength;
				while((readLength = tarInput.read(buf)) != -1) {
					bos.write(buf, 0, readLength);
					nr += readLength;
					readSize.accept(nr);
				}
				bos.close();
			}
		}
		
		System.err.println(response.statusCode());
	}
	
	
	public void getFile(String pipePath, Path downloadedFilePath) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + pipePath);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.setHeader("User-Agent", "FishWatchr")
				.build();

		HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(downloadedFilePath));

		// print status code
		System.err.println(response.statusCode());

		// print response body
		System.err.println(response.body());
	}

	
	static public String generatePath(String seed) {
		String result;
		
		try {
			StringBuffer buf = new StringBuffer();
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte md5bytes[] = md5.digest(String.format("%s\t%d", seed, ThreadLocalRandom.current().nextLong()).getBytes());
			
			for(byte b : md5bytes) {
				int i = b < 0 ? b + 256 : b;
				buf.append(Integer.toHexString(i));
			}
			result = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			result = seed + String.valueOf((new Date()).getTime());
		}
		
		return result;
	}
	
	
    private static class SubscriberWrapper implements BodySubscriber<String> {
        private final CountDownLatch latch;
        private final BodySubscriber<String> subscriber;
        private Subscription subscription;

        private SubscriberWrapper(BodySubscriber<String> subscriber, CountDownLatch latch) {
            this.subscriber = subscriber;
            this.latch = latch;
        }

        @Override
        public CompletionStage<String> getBody() {
            return subscriber.getBody();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            subscriber.onSubscribe(subscription);
            this.subscription = subscription;
            latch.countDown();
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            subscriber.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
            subscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }

        public void cancel() {
            subscription.cancel();
            System.out.println("\r\n----------CANCEL!!!------------");
        }
    }

    private static class BodyHandlerWrapper implements BodyHandler<String> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final BodyHandler<String> handler;
        private SubscriberWrapper subscriberWrapper;

        private BodyHandlerWrapper(BodyHandler<String> handler) {
            this.handler = handler;
        }

        @Override
        public BodySubscriber<String> apply(ResponseInfo responseInfo) {
            subscriberWrapper = new SubscriberWrapper(handler.apply(responseInfo), latch);
            return subscriberWrapper;
        }

        public void cancel() {
            CompletableFuture.runAsync(() -> {
                try {
                    latch.await();
                    subscriberWrapper.cancel();
                } catch (InterruptedException e) {}
            });
        }
    }

	
}
