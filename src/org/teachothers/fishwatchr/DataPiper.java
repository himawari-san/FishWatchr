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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;


public class DataPiper {

	public static final String DEFAULT_PATH_SUFFIX = "0";
	private static final int N_PATH_SUFFIX = 5;
	private static final int READ_BUFFER_SIZE = 1024 * 1024; // 1MB
	private static final String RESERVE_PATH_SUFFIX = "yOWBYinGjbilj6HbR3Ya";

	private String pipeServer;	
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    private int nPathSuffix = N_PATH_SUFFIX;
    

	public DataPiper(String pipeServer) {
		super();
		this.pipeServer = pipeServer.endsWith("/") ? pipeServer : pipeServer + "/";
	}


	
	public int getnPathSuffix() {
		return nPathSuffix;
	}

	public void setnPathSuffix(int nPathSuffix) {
		this.nPathSuffix = nPathSuffix;
	}


	public PipeMessage getMessage(String path, int nRetry) throws IOException, URISyntaxException, InterruptedException {
		PipeMessage message = new PipeMessage();
		
		for(int i = 0; i < nRetry; i++){
			for(int suffix : getRandamOrderedSuffixes(nPathSuffix)) {
				message = getMessage(path + suffix);

				if(message.getErrorCode() > 0) {
					continue;
				} else {
					return message;
				}
			}
		}
		
		return message;				
	}

	
	public PipeMessage getMessage(String path) throws IOException, URISyntaxException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.build();

		
		HttpResponse<String> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (InterruptedException e) {
			postMessage(path, new PipeMessage());
			throw new InterruptedException(); 
		}
		
		if(isErrorResponse(response)) {
			System.err.println(response.statusCode());
			System.err.println(response.body());
			PipeMessage errorMessage = new PipeMessage(path);
			errorMessage.setErrorCode(response.statusCode());
			return errorMessage;
		} else {
			BufferedReader in = new BufferedReader(new StringReader(response.body()));

			String line;
			StringBuffer lines = new StringBuffer();
			while ((line = in.readLine()) != null) {
				lines.append(line + "\n");
			}

			return PipeMessage.encode(lines.toString());
		}
	}
	
	
	public void postMessage(String path, PipeMessage message, int nRetry) throws IOException, URISyntaxException, InterruptedException {
		for(int i = 0; i < nRetry; i++){
			for(int suffix : getRandamOrderedSuffixes(nPathSuffix)) {
				if(postMessage(path + suffix, message) > 0) {
					continue;
				} else {
					System.err.println("postMessage/3: succeeded!");
					return;
				}
			}
		}

		throw new IOException("通信(postMessage)に失敗しました。");
	}
	
	
	public int postMessage(String path, PipeMessage message) throws IOException, URISyntaxException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

	    HttpRequest request = HttpRequest.newBuilder()
	                .POST(HttpRequest.BodyPublishers.ofString(message.toString()))
	                .uri(pipeURL)
	                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
	                .build();
	    
        HttpResponse<String> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (InterruptedException e) {
			// close pipe
			getMessage(path);
			throw new InterruptedException();
		}

		if(isErrorResponse(response)) {
			System.err.println("postMessage/2:" + response.statusCode());
			System.err.println("postMessage/2:" + response.body());
			return response.statusCode();
		} else {
			return 0;
		}
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

	
	public void postFile(String path, Path[] filePaths, Consumer<Long> readLenth) throws IOException, InterruptedException, URISyntaxException, ExecutionException {
		URI pipeURL = new URI(pipeServer + path);
		
		try (
				PipedOutputStream pipeOut = new PipedOutputStream();
				TarArchiveOutputStream tarOut = new TarArchiveOutputStream(pipeOut);
				PipedInputStream pipeIn = new PipedInputStream(pipeOut)) {

			HttpRequest request = HttpRequest.newBuilder()
					.uri(pipeURL)
					.POST(HttpRequest.BodyPublishers.ofInputStream(() -> pipeIn))
					.build();

			Future<?> f = Executors.newSingleThreadExecutor().submit(new Callable<>() {
				@Override
				public Void call() throws IOException {
					for (Path filePath : filePaths) {
						tarOut.putArchiveEntry(new TarArchiveEntry(filePath.toFile(), filePath.toFile().getName()));
						BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(filePath));
						byte buf[] = new byte[READ_BUFFER_SIZE];
						long nr = 0;
						int len = 0;
						while((len = bis.read(buf)) != -1) {
							nr += len;
							tarOut.write(buf, 0, len);
							readLenth.accept(nr);
						}
						tarOut.flush();
						tarOut.closeArchiveEntry();
						bis.close();
					}
					
					tarOut.close();
					pipeOut.close();
					
					return null;
				}
			});

			// TODO
			// Handle response according to the status code
			httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();

			// pipe is expected to be disconnected by interruption
			f.get();
		} catch (InterruptedException | ExecutionException e) {
			//	Execute pipeIn.close(), tarOut.close(), pipeOut.close() automatically
			throw e;
		}
	}


	public void getTarFile(String pipePath, Path rootPath, Consumer<Long> readSize) throws URISyntaxException, IOException, ExecutionException, InterruptedException {
		URI pipeURL = new URI(pipeServer + pipePath);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.build();

		// TODO
		// Handle response according to status code
		final HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

		try {
			Executors.newSingleThreadExecutor().submit(new Callable<Void>() {

				@Override
				public Void call() throws IOException, InterruptedException, ExecutionException {
					try (TarArchiveInputStream tarInput = new TarArchiveInputStream(response.body())) {
						TarArchiveEntry entry;
						byte buf[] = new byte[READ_BUFFER_SIZE];
						
						while((entry = tarInput.getNextTarEntry()) != null) {
							long nr = 0;
							Path filePath = rootPath.resolve(entry.getName());
							try( BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath))){
								int readLength;
								while((readLength = tarInput.read(buf)) != -1) {
									bos.write(buf, 0, readLength);
									nr += readLength;
									readSize.accept(nr);
								}
								bos.close();
							}
						}
					}

					return null;
				}
			})
			.get();
		} catch (InterruptedException e) {
			response.body().close();
			throw e;
		}
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
	
	
	static public String getUrlParameterPath(String str) {
		String result;
		
		try {
			StringBuffer buf = new StringBuffer();
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte md5bytes[] = md5.digest(str.getBytes());
			
			for(byte b : md5bytes) {
				int i = b < 0 ? b + 256 : b;
				buf.append(Integer.toHexString(i));
			}
			result = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			result = str;
		}
		
		return result;
	}
	
	
	public PipeMessage reservePath(String path) throws IOException, URISyntaxException, InterruptedException {
		return getMessage(path+RESERVE_PATH_SUFFIX);
	}
	
	
	public void releasePath(String path) throws IOException, URISyntaxException, InterruptedException {
		postMessage(path+RESERVE_PATH_SUFFIX, new PipeMessage());
	}

	
	private boolean isErrorResponse(HttpResponse<?> response) {
		int responseCode = response.statusCode();
		
		return responseCode >= 400 && responseCode < 500 ? true : false;
	}
	
	
	// result[0] is always 0.
	private int[] getRandamOrderedSuffixes (int max) {
		ArrayList<Integer> seed = new ArrayList<Integer>();
		int[] result = new int[max];
		
		for(int i = 1; i < max; i++) {
			seed.add(i);
		}
		
		result[0] = 0;

		for(int i = 1; i < max; i++) {
			int j = ThreadLocalRandom.current().nextInt(seed.size());
			result[i] = seed.remove(j);
		}
		
		return result;
	}
}
