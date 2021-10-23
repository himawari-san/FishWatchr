package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public class FileSharingPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	private static final int N_PIPE_WATCHER = 5;
	private static final int N_SCAN_PATH = 2;
	private static final int N_RETRY = 10;
	private String pipeServer;
	private SimpleMessageMap messageMap = new SimpleMessageMap();
	private String[] recieversStr = {"john", "paul"};
	private DefaultListModel<String> model = new DefaultListModel<String>();
	private JList<String> recieverList = new JList<String>(model);
//	private MemberFinder memberFinder = null;
	private String username;
	private Path commentFilePath;


	public FileSharingPane(String pipeServer, String username, Path commentFilePath) {
		super();
		this.pipeServer = pipeServer;
		this.username = username;
		this.commentFilePath = commentFilePath;
		this.commentFilePath = Paths.get("/home/masaya/Downloads/GLS1901_merged/GLS1901.mp4.merged_bunseki.xml");
		System.err.println("cf:" + commentFilePath);
		ginit();
		
	}
	
	
	private void ginit(){
		int nTab = 0;
		
		setPreferredSize(new Dimension(300, 300));
		
		JPanel idPanel = new JPanel();
		JLabel usernameLabel = new JLabel("Username");
		JLabel usernameBody = new JLabel(username);
		JLabel pathLabel = new JLabel("Path:");
		JTextField pathField = new JTextField("a");
		idPanel.setLayout(new GridLayout(2, 2, 1, 3));
		idPanel.add(usernameLabel);
		idPanel.add(usernameBody);
		idPanel.add(pathLabel);
		idPanel.add(pathField);
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10, 10));
		
		JTabbedPane tabbedpane = new JTabbedPane();
		mainPanel.add(idPanel, BorderLayout.NORTH);
		mainPanel.add(tabbedpane, BorderLayout.CENTER);
//		mainPanel.add(recieversScrollPane);
//		mainPanel.add(scanButton);

		// submit tab
		JPanel submitPanel = new JPanel();
		submitPanel.setLayout(new BorderLayout());
		JPanel submitButtonPanel = new JPanel();
		JButton submitButton = new JButton("Submit");
		submitButtonPanel.add(submitButton);

		JPanel submitDisplayPanel = new JPanel();
//		submitDisplayPanel.setLayout(new BorderLayout());
		JLabel submitLabel = new JLabel("提出します。");
		JProgressBar submitionProgress = new JProgressBar();
		submitDisplayPanel.add(submitLabel, BorderLayout.CENTER);
		submitDisplayPanel.add(submitionProgress, BorderLayout.SOUTH);
		submitPanel.add(submitDisplayPanel, BorderLayout.CENTER);
		submitPanel.add(submitButtonPanel, BorderLayout.SOUTH);
		tabbedpane.add(submitPanel);
		tabbedpane.setTitleAt(nTab++, "Submit");
		
		// recieve tab
		JPanel recievePanel = new JPanel();
		tabbedpane.add(recievePanel);
		JButton recieveButton = new JButton("recieve");
		recievePanel.add(recieveButton);
		tabbedpane.setTitleAt(nTab++, "recieve");
		
		// collect tab
		JPanel collectPanel = new JPanel();
		tabbedpane.add(collectPanel);
		JButton collectButton1 = new JButton("scan");
		JButton collectButton2 = new JButton("collect");
		JScrollPane membersScrollPane = new JScrollPane();
		membersScrollPane.setViewportView(recieverList);
		collectPanel.add(membersScrollPane);
		collectPanel.add(collectButton1);
		collectPanel.add(collectButton2);
		tabbedpane.setTitleAt(nTab++, "Collect");
		
		
		// distribute tab
		JPanel distributePanel = new JPanel();
		tabbedpane.add(distributePanel);
		JButton distributeButton = new JButton("Distribute");
//		recievePanel.add(recieveButton);
		tabbedpane.setTitleAt(nTab++, "Distribute");

		
		
		setMessage(mainPanel);
		
		tabbedpane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				System.err.println("tab number:" + tabbedpane.getSelectedIndex());
//				if(memberFinder != null) {
//					memberFinder.pool.shutdownNow();
//				}
			}
		});
		
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataPiper pipe = new DataPiper(pipeServer);
				String basePath = pathField.getText();
				String selectvalues[] = {};
				JOptionPane op = new JOptionPane("messages", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, selectvalues, null);
				final JDialog jd = op.createDialog(FileSharingPane.this, "title");
				System.err.println("ps:" + basePath + "," + commentFilePath);
				PipeSender ps = new PipeSender(pipe, basePath, username, commentFilePath,
						(str)->{
							Executors.newSingleThreadExecutor().submit(new Runnable() {
								@Override
								public void run() {
									submitLabel.setText(str);
								}
							});
						});
				Executors.newSingleThreadExecutor().submit(ps);
			}
		});
		
		
		collectButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String basePath = pathField.getText();
				MemberFinder memberFinder = new MemberFinder(N_SCAN_PATH, basePath,
						(e)->{
							JOptionPane.showMessageDialog(FileSharingPane.this, e.getMessage());
							System.err.println(e.getMessage());
						});
				Executors.newSingleThreadExecutor().submit(memberFinder);
				
			}
		});
		
		
		collectButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataPiper pipe = new DataPiper(pipeServer);
				String downloadedFilename = "fwdata";
				Path downloadedFilePath = commentFilePath.getParent().resolve(downloadedFilename);
				String username = recieverList.getModel().getElementAt(0);
				SimpleMessage message = messageMap.get(username);
				System.err.println("it1:" + recieverList.getModel().getElementAt(0));
				System.err.println("it2:" + messageMap.get(username));
				try {
					pipe.getFile(message.get(DataPiper.MESSAGE_KEY_PATH), downloadedFilePath);
				} catch (IOException | URISyntaxException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		
		
		recieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataPiper pipe = new DataPiper(pipeServer);
				String pathBase = pathField.getText();
				String selectvalues[] = {};
				JOptionPane op = new JOptionPane("messages", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, selectvalues, null);
				final JDialog jd = op.createDialog(FileSharingPane.this, "title");
				if(pathBase.isEmpty()) {
					pathBase = "a";
				}
				PipeReciever pr = new PipeReciever(pipe, pathBase, username,
						(str)->{
							System.err.println("open!");
							Executors.newSingleThreadExecutor().submit(new Runnable() {
								@Override
								public void run() {
									jd.setVisible(true);
								}
							});
						});
				Executors.newSingleThreadExecutor().submit(pr);
			}
		});
		
	}

	
	public void addReciever(SimpleMessage message) {
		System.err.println("add:" + message.get("username"));
		DefaultListModel<String> model = (DefaultListModel<String>) recieverList.getModel();
		model.addElement(message.get("username"));
	}
	
	
	
	
	class PipeSender implements Callable<Void> {

		private DataPiper pipe;
		private String pathBase;
		private String username;
		private Path filePath;
		private Consumer<String> c;
	
		
		public PipeSender(DataPiper pipe, String pathBase, String username, Path filePath, Consumer<String> c) {
			this.pipe = pipe;
			this.pathBase = pathBase;
			this.username = username;
			this.filePath = filePath;
			this.c = c;
		}

		@Override
		public Void call()  {
			c.accept("送信準備中です！");
			String newPath = pipe.sendUserInformation(username, pathBase);
			if(newPath == null) {
				return null;
			}
			
			c.accept("送信準備完了です！");
			System.err.println("post path!:" + newPath);
			System.err.println("post file!:" + filePath);
			try {
				pipe.postFile(newPath, filePath);
			} catch (URISyntaxException | IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			c.accept("送信完了しました");
			
			return null;
		}
		
	}

	
	class PipeReciever implements Callable<Void> {

		private DataPiper pipe;
		private String pathBase;
		private String username;
		private Consumer<String> c;
	
		
		public PipeReciever(DataPiper pipe, String pathBase, String username, Consumer<String> c) {
			this.pipe = pipe;
			this.pathBase = pathBase;
			this.username = username;
			this.c = c;
		}

		@Override
		public Void call() {
			c.accept("test1");
			String newPath = pipe.sendUserInformation(username, pathBase);
			if(newPath == null) {
				return null;
			}
			c.accept(null);
			c.accept("test2");
			
//			try {
//				System.err.println("recieving!:" + newPath);
//				recieve(newPath);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			System.err.println("recieved:" + newPath);
			
			
			return null;
		}
		
		
//		public void recieve(String path) throws IOException {
//			pipe.getFile(path);
//		}
		
	}
	
	class MemberFinder implements Callable<Void> {
		ExecutorService pool;
		DataPiper pipe;
		String path;
		Consumer<Exception> c;
		
		public MemberFinder(int nPool, String path, Consumer<Exception> c) {
			pool = Executors.newFixedThreadPool(nPool);
			pipe = new DataPiper(pipeServer);
			this.path = path;
			this.c = c;
		}

		@Override
		public Void call() {
			BlockingQueue<Future<Long>> queue = new ArrayBlockingQueue<>(10);
			
			for(int i = 0; i < N_PIPE_WATCHER; i++) {
				System.err.println("pathpw:" + path);
				PipeWatcher pw = new PipeWatcher(pipe, path,
						(message)-> {
							String messageID = message.getID();
							if(messageID.isEmpty()) {
								return;
							}
							System.err.println("mid:" + messageID);
							messageMap.put(messageID, message);
							addReciever(message);
						});
				Future<Long> f = pool.submit(pw);
				queue.add(f);
			}
			
			for(Future<Long> f : queue) {
				try {
					Long id = f.get();
				} catch (InterruptedException | ExecutionException e) {
					pool.shutdownNow();
					c.accept(e);
					e.printStackTrace();

					return null;
				}
			}
			
			pool.shutdown();
			
			return null;
		}
	}

}
