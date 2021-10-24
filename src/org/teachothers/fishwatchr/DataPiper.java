package org.teachothers.fishwatchr;

import java.io.BufferedInputStream;
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
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;


public class DataPiper {
	public static final String MESSAGE_KEY_PATH = "path";
	public static final String MESSAGE_KEY_USERNAME = "username";
	public static final String KEY_VALUE_SEPARATOR = ":";
	private static final int N_PIPE_WATCHER = 5;
	private static final int N_SCAN_PATH = 2;
	private static final int N_RETRY = 10;

	private String pipeServer;	
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

	public DataPiper(String pipeServer) {
		super();
		this.pipeServer = pipeServer.endsWith("/") ? pipeServer : pipeServer + "/";
	}



	public SimpleMessage getMessage(String path) throws IOException, URISyntaxException, InterruptedException {
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

		return SimpleMessage.encode(lines.toString());
	}
	
	
	public void postMessage(String path, SimpleMessage message) throws URISyntaxException, IOException, InterruptedException {
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

	
	public void postFile(String path, Path filePath) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		System.err.println("pf1:" + path);
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

	
	public void postFile(String path, Path[] filePaths, Consumer<String> c) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		System.err.println("pf1:" + path);
		
		try (
				PipedOutputStream pipeOut = new PipedOutputStream();
				TarArchiveOutputStream tarOut = new TarArchiveOutputStream(pipeOut);
				PipedInputStream pipeIn = new PipedInputStream(pipeOut)) {

			System.err.println("hey1");
			HttpRequest request = HttpRequest.newBuilder().uri(pipeURL)
					.POST(HttpRequest.BodyPublishers.ofInputStream(() -> pipeIn)).build();

			System.err.println("hey2");
			Executors.newSingleThreadExecutor().submit(new Runnable() {
				@Override
				public void run() {
					for (Path filePath : filePaths) {
						float fileLength = filePath.toFile().length() / 1024 / 1024; // MB
						try {
							tarOut.putArchiveEntry(new TarArchiveEntry(filePath.toFile()));
							BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(filePath));
							byte buf[] = new byte[4096*16];
							long nr = 0;
							int len = 0;
							while((len = bis.read(buf)) != -1) {
								nr += len;
								tarOut.write(buf, 0, len);
//								c.accept(String.valueOf(nr));
								c.accept(String.format("%s (%.0f%%, %.1fMB)", filePath.getFileName().toString(), nr/fileLength/1024/1024*100, fileLength));
							}
//							tarOut.write(Files.readAllBytes(filePath));
							tarOut.closeArchiveEntry();
							c.accept("- " + filePath.getFileName().toString());
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

			System.err.println("hey3");
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			System.err.println("hey4");
		// print status code
			System.err.println("post res:" + response.statusCode());

			System.err.println("hey5");
			// print response body
			System.err.println(response.body());

		}
	}

	
	
	
	public void getFile(String pipePath, Path downloadedFilePath) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + pipePath);

		System.err.println("gf0");
		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.setHeader("User-Agent", "FishWatchr")
				.build();

		System.err.println("gf1");
		HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(downloadedFilePath));

		System.err.println("gf2");
		// print status code
		System.err.println(response.statusCode());

		// print response body
		System.err.println(response.body());
	}

	
	public String getUserInformation(String basePath) {
		Random rand = new Random();
		
		for(int i = 0; i < N_RETRY; i++) {
			int pathSX = rand.nextInt(N_SCAN_PATH);
			String path = basePath + pathSX;
			
			try {
				SimpleMessage message = getMessage(path);
				System.err.println("send path:" + path);
				System.err.println("send message:" + message.toString());
				System.err.println("send id:" + message.getID());
				return message.getID();
			} catch (IOException | URISyntaxException | InterruptedException e) {
				System.err.println("Warning(FileSharingPane): Retry getUserInformation()");
				continue;
			}
			
		}
		
		return null; // failure
	}
	

	public String sendUserInformation(String username, String basePath) {
		Random rand = new Random();
		
		for(int i = 0; i < N_RETRY; i++) {
			int pathSX = rand.nextInt(N_SCAN_PATH);
			String path = basePath + pathSX;
			System.err.println("rc path:" + path + "," + username);
			
			SimpleMessage message = new SimpleMessage(username);

			// username
			message.put(MESSAGE_KEY_USERNAME, username);
			message.put(MESSAGE_KEY_PATH,  generatePath(username + basePath));

			try {
				System.err.println("send path:" + path);
				System.err.println("send message:" + message.toString());
				System.err.println("send id:" + message.getID());
				postMessage(path, message);
				return message.get(MESSAGE_KEY_PATH);
			} catch (IOException | URISyntaxException | InterruptedException e) {
				System.err.println("Warning(FileSharingPane): Retry sendUserInformation()");
				continue;
			}
			
		}
		
		return null; // failure
	}

	
	public String generatePath(String seed) {
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
}
