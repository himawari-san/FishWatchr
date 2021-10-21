package org.teachothers.fishwatchr;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Random;


public class DataPiper {
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

	
	public void getFile(Path path) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		HttpRequest request = HttpRequest.newBuilder()
				.GET().uri(pipeURL)
				.setHeader("User-Agent", "FishWatchr")
				.build();

		HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(path));

		// print status code
		System.err.println(response.statusCode());

		// print response body
		System.err.println(response.body());
	}

	

	public String sendUserInformation(String username, String basePath) {
		Random rand = new Random();
		
		for(int i = 0; i < N_RETRY; i++) {
			int pathSX = rand.nextInt(N_SCAN_PATH);
			String path = basePath + pathSX;
			System.err.println("rc path:" + path + "," + username);
			
			SimpleMessage message = new SimpleMessage(username);

			// username
			message.put("username", username);
			
			try {
				System.err.println("send path:" + path);
				System.err.println("send message:" + message.toString());
				System.err.println("send id:" + message.getID());
				postMessage(path, message);
				return message.getID();
			} catch (IOException | URISyntaxException | InterruptedException e) {
				System.err.println("Warning(FileSharingPane): Retry sendUserInformation()");
				continue;
			}
			
		}
		
		return null; // failure
	}

	
}
