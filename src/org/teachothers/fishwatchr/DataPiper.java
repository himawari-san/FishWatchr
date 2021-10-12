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
import java.net.http.HttpResponse;
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

	
	private InputStream getInputStream(String path) throws IOException {
		URL pipeURL = new URL(pipeServer + path);
		URLConnection urlCon = pipeURL.openConnection();

		HttpURLConnection httpCon = (HttpURLConnection) pipeURL.openConnection();

		return urlCon.getInputStream();
	}
	
	
	private OutputStream getOutStream(String path) throws IOException {
		URL pipeURL = new URL(pipeServer + path);

		HttpURLConnection httpCon = (HttpURLConnection) pipeURL.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");

		return httpCon.getOutputStream();
	}

	
	public void getFile(String path) throws IOException {
		InputStream inputStream = getInputStream(path);
		FileOutputStream fos = new FileOutputStream(new File("/tmp/aaa"));
		byte[] data = new byte[1024];
		int nRead;
		
		while((nRead = inputStream.read(data)) > 0) {
			fos.write(data, 0, nRead);
		}
		
		fos.close();
	}
	
	
	public SimpleMessage getMessage2(String path) throws IOException {
		URL pipeURL = new URL(pipeServer + path);
//		URLConnection urlCon = pipeURL.openConnection();

		HttpURLConnection httpCon = (HttpURLConnection) pipeURL.openConnection();
		httpCon.connect();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
		SimpleMessage message = new SimpleMessage();

		String line;
		while((line = in.readLine()) != null) {
			int p = line.indexOf(KEY_VALUE_SEPARATOR);
			
			if(p != -1) {
				String key = line.substring(0, p);
				if(!key.isEmpty()) {
					message.put(key, line.substring(p + 1));
					System.err.println("kv:" + key + "," + message.get(key));
					continue;
				}
			}
			
			System.err.println("Warning(getMap%FilePiper, invalid data):" + line);
		}
		in.close();
		int response = httpCon.getResponseCode();
		System.err.println("res getMessage:" + response + "," + path);
	
		httpCon.disconnect();
		
		return message;
	}

	public SimpleMessage getMessage(String path) throws IOException, URISyntaxException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);

		HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(pipeURL)
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
//                .header("Content-Type", "application/json")
                .build();
		
		  HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	        // print status code
	        System.out.println(response.statusCode());

	        // print response body
	        System.out.println(response.body());
	        
	        BufferedReader in = new BufferedReader(new StringReader(response.body()));
			SimpleMessage message = new SimpleMessage();
	        
//	        response.body().toString()
	        
			String line;
	        while((line = in.readLine()) != null) {
				int p = line.indexOf(KEY_VALUE_SEPARATOR);
				
				if(p != -1) {
					String key = line.substring(0, p);
					if(!key.isEmpty()) {
						message.put(key, line.substring(p + 1));
						System.err.println("kv:" + key + "," + message.get(key));
						continue;
					}
				}
				
				System.err.println("Warning(getMap%FilePiper, invalid data):" + line);
			}
		
	        
	        return message;
	}

	
	public void putMessage(String path, String message) throws URISyntaxException, IOException, InterruptedException {
		URI pipeURL = new URI(pipeServer + path);
//		
//	    HttpClient httpClient = HttpClient.newBuilder()
//	            .version(HttpClient.Version.HTTP_2)
//	            .connectTimeout(Duration.ofSeconds(10))
//	            .build();
	    
	    
	    HttpRequest request = HttpRequest.newBuilder()
	                .POST(HttpRequest.BodyPublishers.ofString(message))
	                .uri(pipeURL)
	                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
	                .build();
	    
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.out.println(response.statusCode());

        // print response body
        System.out.println(response.body());


	}
	
	public void put(String path, String message) throws IOException {
		URL pipeURL = new URL(pipeServer + path);

		HttpURLConnection httpCon = (HttpURLConnection) pipeURL.openConnection();
		httpCon.setRequestMethod("POST");
		httpCon.setDoOutput(true);
//		httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

		DataOutputStream out = new DataOutputStream(httpCon.getOutputStream());
//		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
		out.writeBytes(message);
		out.flush();
		out.close();
		int response = httpCon.getResponseCode();
		System.err.println("res put:" + response + "," + path);
	       BufferedReader inputReader = new BufferedReader(
	               new InputStreamReader(httpCon.getInputStream()));
	           String inputLine;
	           StringBuffer response2 = new StringBuffer();
	    
	           while ((inputLine = inputReader.readLine()) != null) {
	               response2.append(inputLine);
	           }
	           inputReader.close();
	           System.out.println(response2.toString());
		httpCon.disconnect();
	}

	
	public String sendUserInformation(String username, String basePath) {
		Random rand = new Random();
		
		for(int i = 0; i < N_RETRY; i++) {
			int pathSX = rand.nextInt(N_SCAN_PATH);
			String path = basePath + pathSX;
			StringBuffer message = new StringBuffer();
			
			message.append(SimpleTextMap.DATA_ID_KEY + DataPiper.KEY_VALUE_SEPARATOR + username + "\n");
			
			try {
				System.err.println("send:" + path);
				putMessage(path, message.toString());
				return path;
			} catch (IOException | URISyntaxException | InterruptedException e) {
				System.err.println("Warning(FileSharingPane): Retry sendUserInformation()");
				continue;
			}
			
		}
		
		return null; // failure
	}

	
}
