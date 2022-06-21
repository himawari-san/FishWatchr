package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;






public class FileSharingDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int N_RETRY = 2;
	private static final int WIDTH = 450;
	private static final int HEIGHT = 550;
	private User user;
	private Path commentFilePath;
	private Path mediaFilePath;
	private FTextField pathField;
	private DataPiper pipe;
	private Consumer<Path> receiveSuccess;
	private Consumer<ArrayList<Path>> collectSuccess;


	public FileSharingDialog(String pipeServer, User user, Path commentFilePath, Path mediaFilePath,
			Consumer<Path> receiveSuccess, Consumer<ArrayList<Path>> collectSuccess) {
		super();
		this.user = user;
		this.commentFilePath = commentFilePath;
		this.mediaFilePath = mediaFilePath;
		this.pipe = new DataPiper(pipeServer);
		this.receiveSuccess = receiveSuccess;
		this.collectSuccess = collectSuccess;
		ginit();
	}

	
	private void ginit(){
		int nTab = 0;
		
		setModal(true);
		setSize(WIDTH, HEIGHT);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("ファイル共有");

		
		JPanel idPanel = new JPanel();
		JLabel usernameLabel = new JLabel("注釈者 / セッション");
		usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, usernameLabel.getFont().getSize()));
		JLabel usernameBody = new JLabel(user.getUserName() + " / " + user.getGroupName());
		JLabel pathLabel = new JLabel("共有名");
		pathLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, pathLabel.getFont().getSize()));
		pathField = new FTextField("a");
		idPanel.setLayout(new GridLayout(2, 2, 1, 10));
		idPanel.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new EmptyBorder(15, 5, 15, 5)));
		idPanel.add(usernameLabel);
		idPanel.add(usernameBody);
		idPanel.add(pathLabel);
		idPanel.add(pathField);
		idPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 250)); // the height value must be set, but doesn't work properly.

		JTabbedPane tabbedpane = new JTabbedPane();

		SendPanel sendPanel = new SendPanel();
		ReceivePanel receivePanel = new ReceivePanel();
		CollectPanel collectPanel = new CollectPanel();
		DistributePanel distributePanel = new DistributePanel();
		
		tabbedpane.add(sendPanel);
		tabbedpane.setTitleAt(nTab++, "送信");

		tabbedpane.add(receivePanel);
		tabbedpane.setTitleAt(nTab++, "受信");
		
		tabbedpane.add(collectPanel);
		tabbedpane.setTitleAt(nTab++, "収集");

		tabbedpane.add(distributePanel);
		tabbedpane.setTitleAt(nTab++, "配布");

		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		mainPanel.add(idPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(10,10)));
		mainPanel.add(tabbedpane);
		mainPanel.add(Box.createRigidArea(new Dimension(10,10)));
		getContentPane().add(mainPanel);
		
		tabbedpane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				System.err.println("tab number:" + tabbedpane.getSelectedIndex());
				sendPanel.cancelAction();
				receivePanel.cancelAction();
				distributePanel.cancelAction();
				collectPanel.cancelAction();

				sendPanel.clear();
				receivePanel.clear();
				distributePanel.clear();
				collectPanel.clear();
			}
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				sendPanel.cancelAction();
				receivePanel.cancelAction();
				distributePanel.cancelAction();
				collectPanel.cancelAction();
			}
		});
	}

	
	private class MessagePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		JTextArea textArea = new JTextArea("");

		public MessagePanel(String title) {
			setLayout(new BorderLayout());
			setBorder(new TitledBorder(new EtchedBorder(), title));
			JScrollPane scrollPane = new JScrollPane();
			textArea = new JTextArea("");
			textArea.setLineWrap(true);
			scrollPane.setViewportView(textArea);
			add(Box.createRigidArea(new Dimension(5, 5)), BorderLayout.NORTH);
			add(scrollPane, BorderLayout.CENTER);
		}

		
		public void append(String str) {
			textArea.append(str);
			textArea.setCaretPosition(textArea.getText().length());
		}
		
		public void clear() {
			textArea.setText("");
		}
	}

	
	private abstract class PipeActionPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private PipeActionButton button  = null;
		
		public void setButton(PipeActionButton button) {
			this.button = button;
		}
		
		public void cancelAction() {
			if(button != null) {
				button.cancelAction();
			}
		}
		
		abstract public void clear();
	}
	
	
	
	private class SendPanel extends PipeActionPanel {
		
		private static final long serialVersionUID = 1L;
		private MemberPanel memberPanel = new MemberPanel();
		private JCheckBox fileOptionCheckBox = new JCheckBox("動画ファイルを含む");
		private MessagePanel messagePanel = new MessagePanel("システムメッセージ");

		public SendPanel() {

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			JPanel memberListPanel = new JPanel();
			memberPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
			memberListPanel.setLayout(new BoxLayout(memberListPanel, BoxLayout.PAGE_AXIS));
			memberListPanel.add(Box.createRigidArea(new Dimension(5, 5)));
			memberListPanel.add(memberPanel);
			memberListPanel.add(Box.createRigidArea(new Dimension(5,5)));
			JPanel fileOptionPanel = new JPanel();
			fileOptionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			fileOptionPanel.add(fileOptionCheckBox);
			memberListPanel.add(fileOptionPanel);
			memberListPanel.setBorder(new TitledBorder(new EtchedBorder(), "送信の相手"));
			
			JPanel buttonPanel = new JPanel();
			SendButton sendButton = new SendButton(memberPanel, messagePanel, fileOptionCheckBox);
			setButton(sendButton);
			buttonPanel.add(sendButton);

			add(Box.createRigidArea(new Dimension(10,10)));
			add(memberListPanel);
			add(Box.createRigidArea(new Dimension(10,10)));
			add(messagePanel);
			add(buttonPanel);
		}

		@Override
		public void clear() {
			memberPanel.clear();
			fileOptionCheckBox.setSelected(false);
			messagePanel.clear();
		}
	}
	
	
	private class ReceivePanel extends PipeActionPanel {
		
		private static final long serialVersionUID = 1L;
		private MemberPanel memberPanel = new MemberPanel();
		private MessagePanel messagePanel = new MessagePanel("システムメッセージ");

		public ReceivePanel() {

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			JPanel memberListPanel = new JPanel();
			memberPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
			memberListPanel.setLayout(new BoxLayout(memberListPanel, BoxLayout.PAGE_AXIS));
			memberListPanel.add(Box.createRigidArea(new Dimension(5, 5)));
			memberListPanel.add(memberPanel);
			memberListPanel.add(Box.createRigidArea(new Dimension(10, 10)));
			memberListPanel.setBorder(new TitledBorder(new EtchedBorder(), "受信の相手"));
			
			JPanel buttonPanel = new JPanel();
			ReceiveButton receiveButton = new ReceiveButton(memberPanel, messagePanel);
			setButton(receiveButton);
			buttonPanel.add(receiveButton);

			add(Box.createRigidArea(new Dimension(10,10)));
			add(memberListPanel);
			add(Box.createRigidArea(new Dimension(10,10)));
			add(messagePanel);
			add(buttonPanel);
		}

		@Override
		public void clear() {
			memberPanel.clear();
			messagePanel.clear();
		}
	}
	

	private class CollectPanel extends PipeActionPanel {
		
		private static final long serialVersionUID = 1L;
		private static final int MemberListPanel_Height = 150;
		private MemberListPanel memberListPanel = new MemberListPanel();
		private MessagePanel messagePanel = new MessagePanel("システムメッセージ");
		private CollectButton collectButton = new CollectButton(memberListPanel, messagePanel);

		public CollectPanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			memberListPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, MemberListPanel_Height));
			memberListPanel.setMinimumSize(new Dimension(Short.MAX_VALUE, MemberListPanel_Height));
			memberListPanel.setPreferredSize(new Dimension(Short.MAX_VALUE, MemberListPanel_Height));
			
			JPanel buttonPanel = new JPanel();
			setButton(collectButton);
			buttonPanel.add(collectButton);

			add(Box.createRigidArea(new Dimension(10,10)));
			add(memberListPanel);
			add(Box.createRigidArea(new Dimension(10,10)));
			add(messagePanel);
			add(buttonPanel);
		}

		@Override
		public void clear() {
			memberListPanel.clear();
			messagePanel.clear();
			collectButton.initState();
		}
	}

	
	private class DistributePanel extends PipeActionPanel {
		
		private static final long serialVersionUID = 1L;
		private static final int MemberListPanel_Height = 150;
		private MemberListPanel memberListPanel = new MemberListPanel(false);
		private MessagePanel messagePanel = new MessagePanel("システムメッセージ");
		private JProgressBar progressBar = new JProgressBar();
		private DistributeButton distributeButton = new DistributeButton(memberListPanel, messagePanel, progressBar);

		public DistributePanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			memberListPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, MemberListPanel_Height));
			memberListPanel.setMinimumSize(new Dimension(Short.MAX_VALUE, MemberListPanel_Height));
			memberListPanel.setPreferredSize(new Dimension(Short.MAX_VALUE, MemberListPanel_Height));

			JPanel progressPanel = new JPanel();
			JLabel progressLabel = new JLabel("進行状況：");
			progressBar.setPreferredSize(new Dimension(300, 10));
			progressPanel.add(progressLabel);
			progressPanel.add(progressBar);

			JPanel buttonPanel = new JPanel();
			setButton(distributeButton);
			buttonPanel.add(distributeButton);

			add(Box.createRigidArea(new Dimension(10,10)));
			add(memberListPanel);
//			add(Box.createRigidArea(new Dimension(10,10)));
			add(progressPanel);
			add(Box.createRigidArea(new Dimension(10,10)));
			add(messagePanel);
			add(buttonPanel);
		}

		@Override
		public void clear() {
			memberListPanel.clear();
			messagePanel.clear();
			distributeButton.initState();
			progressBar.setValue(progressBar.getMinimum());
		}
	}

	
	
	private class MemberListPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private JProgressBar progressBar = new JProgressBar();
		private MemberListModel memberListModel = new MemberListModel();
		private JList<MemberPanel> memberList = new JList<MemberPanel>(memberListModel);
		private boolean enableProgressBar = true;

		
		public MemberListPanel() {
			this(true);
		}
		
		public MemberListPanel(boolean enableProgressBar) {
			this.enableProgressBar = enableProgressBar;
			
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBorder(new TitledBorder(new EtchedBorder(), "メンバー"));

			JPanel listPanel = new JPanel();
			JScrollPane listScrollPane = new JScrollPane();

			listPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
			listPanel.add(listScrollPane);

			listScrollPane.setViewportView(memberList);
			listScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			memberList.setCellRenderer(new MemberCellRenderer());

			add(listScrollPane);
		}
		
		public void addMember(PipeMessage message) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					MemberPanel m = new MemberPanel();
					m.setMember(message.getSenderName());
					m.initBar(0, (int)(message.getDataSize()));
					m.setEnableProgressBar(enableProgressBar);
					memberListModel.add(0, m);
				}
			});
		}
		
		
		public void clear() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					memberListModel.clear();
				}
			});
		}
		
		public int getMemberSize() {
			return ((DefaultListModel<MemberPanel>)memberList.getModel()).getSize();
		}

		public MemberPanel getMemberPanelAt(int i) {
			return ((DefaultListModel<MemberPanel>)memberList.getModel()).getElementAt(i);
		}

		public void update(int i) {
			memberListModel.update(i);
		}

		public void setValue(int v) {
			progressBar.setValue(v);
		}
		
		
		class MemberCellRenderer extends MemberPanel implements ListCellRenderer<MemberPanel>{
			private static final long serialVersionUID = 1L;

			public MemberCellRenderer() {
			}

			public Component getListCellRendererComponent(JList list, MemberPanel value, int index, boolean isSelected, boolean cellHasFocus){
				return value;
			}
		}
		
		
		class MemberListModel extends DefaultListModel<MemberPanel> {
			private static final long serialVersionUID = 1L;
			
		    public void update(int index)
		    {
		        fireContentsChanged(this, index, index);
		    }
		}
	}
	
	
	private class MemberPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private static final String MEMBER_NOT_FOUND = "（未確定）";
		private JLabel nameLabel = new JLabel(MEMBER_NOT_FOUND);
		private JProgressBar progressBar = new JProgressBar();

		public MemberPanel() {
			setLayout(new GridLayout(1, 2));
			add(nameLabel);
			add(progressBar);
		}

		
		public void setMember(String str) {
			System.err.println("h:" + SwingUtilities.isEventDispatchThread());
			nameLabel.setText(str);
		}
		
		
		public String getMember() {
			return nameLabel.getText();
		}
		
		
		public void clear() {
			nameLabel.setText(MEMBER_NOT_FOUND);
			progressBar.setValue(progressBar.getMinimum());
		}

		public void initBar(int min, int max) {
			progressBar.setMinimum(min);
			progressBar.setMaximum(max);
		}

		public void setValue(int v) {
			progressBar.setValue(v);
		}
		
		public void setEnableProgressBar(boolean enabled) {
			progressBar.setVisible(enabled);
		}
	}

	
	private class ReceiveButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private final String[] labels = {"相手を探索", "キャセル", "受信を実行", "キャセル", "閉じる"};
		
		private String newPath = "";
		private long dataSize = 0;
		private Path saveRootPath = null;
		private Path savePath = null;

		
		public ReceiveButton(MemberPanel memberPanel, MessagePanel messagePanel) {
			setLabels(labels);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					switch (getStatus()) {
					case STATUS_SEARCH:

						if(commentFilePath.getParent() == null) {
							if(Util.getDefaultDirPath() == null) {
								JOptionPane.showMessageDialog(ReceiveButton.this, "保存用のフォルダを作成できません。\n動作が確認できている観察結果をFishWatchrに読み込んだ上で，再度実行してみてください。");
								return;
							}
							saveRootPath = Util.getDefaultDirPath();
						} else {
							saveRootPath = commentFilePath.getParent();
						}

						
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								messagePanel.append("- メンバーを探しています。\n");
								String basePath = getBasePathStr();
								newPath = DataPiper.generatePath(user.getUserName() + basePath);
								PipeMessage myInfo = new PipeMessage(user.getUserName(), newPath);
								PipeMessage memberInfo = null;
								try {
									pipe.postMessage(basePath, myInfo, N_RETRY);
									memberInfo = pipe.getMessage(newPath);

									String memberName = memberInfo.getSenderName();
									memberPanel.setMember(memberName);
									messagePanel.append("- " + memberName + "が見つかりました。\n");

									// For Distribute
									if(memberInfo.getStatus() == PipeMessage.STATUS_CONTINUED) { // will be registered
										messagePanel.append("- " + memberName + "が送信するまで，お待ちください。\n");
										memberInfo = pipe.getMessage(newPath);
									} else if(memberInfo.getStatus() == PipeMessage.STATUS_DUPLICATED) { // duplicated
										JOptionPane.showMessageDialog(ReceiveButton.this, "同じメンバー名のユーザが登録されていたため，" + memberName + "に接続を拒否されました。");
										messagePanel.append("- 同じメンバー名のユーザが登録されていたため，" + memberName + "に接続を拒否されました。\n");
										initState();
										memberPanel.clear();
										return;
									} else {
										messagePanel.append("- " + memberName + "が送信を開始するまで，お待ちください。\n");
									}

									dataSize =  memberInfo.getDataSize();
									newPath = memberInfo.getPath();
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											int result = JOptionPane.showConfirmDialog(ReceiveButton.this, 
													memberName + "からファイルを受け取る場合は「OK」ボタンを押してください。",
													"受信の確認", JOptionPane.OK_CANCEL_OPTION);
											if(result == JOptionPane.OK_OPTION) {
												setStatus(STATUS_EXECUTE);
												ReceiveButton.this.doClick();
												messagePanel.append("- " + "受信中です。\n");
											} else {
												messagePanel.append("- " + "キャンセルしました。\n");
												initState();
												return;
											}
										}
									});
								} catch (URISyntaxException | IOException e) {
									JOptionPane.showMessageDialog(ReceiveButton.this, e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									initState();
									return;
								}
							}
						});
						
						setStatus(STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
					case STATUS_CANCEL2:
						messagePanel.append("- キャンセルしました。\n");
						memberPanel.clear();
						future.cancel(true);
						break;
					case STATUS_EXECUTE:
						setStatus(STATUS_CANCEL2);
						
						String memberName = memberPanel.getMember();
						savePath = Util.getUniquePath(saveRootPath, memberName);
						
						try {
							Files.createDirectories(savePath);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(ReceiveButton.this,
									"保存用のフォルダが作成できませんでした。\n" + savePath.toAbsolutePath() + "\n" + e.getMessage());
							initState();
							return;
						}

						memberPanel.initBar(0, (int)dataSize);

						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							
							@Override
							public void run() {
								try {
									pipe.getTarFile(newPath, savePath,
											(readSize) -> {
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														memberPanel.setValue(readSize.intValue());
													}
												});
											});
								} catch (URISyntaxException | ExecutionException | IOException e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(ReceiveButton.this, "データ送信が中断されるなどして，受信が完了しませんでした。\n" + e.getMessage());
											messagePanel.append("- キャンセルしました。\n");
											initState();
											memberPanel.clear();
										}
									});
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(ReceiveButton.this, "受信をキャンセルしました。");
											memberPanel.clear();
											initState();
										}
									});
									return;
								}

								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										messagePanel.append("- 受信が完了しました\n");
										messagePanel.append("- 保存先：" + savePath + "\n");
										JOptionPane.showMessageDialog(ReceiveButton.this, 
												"受信が完了しました。\n「OK」を押すと，再生が開始します。\n"
												+ "保存先：" + savePath + "\n");
										setStatus(STATUS_FINISH);
										ReceiveButton.this.doClick();
									}
								});
							}
						});
						
						break;
					case STATUS_FINISH:
						FileSharingDialog.this.setVisible(false);
						receiveSuccess.accept(savePath);
						break;
					default:
						break;
					}
				}
			});
		}
	}

	

	private class DistributeButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private final String[] labels = {"相手を探索", "キャンセル", "配布を実行", "キャンセル"};
		private PipeMessageReceiver messageReceiver = null;
		
		public DistributeButton(MemberListPanel memberListPanel, MessagePanel messagePanel, JProgressBar progressBar) {
			setLabels(labels);

			addActionListener(new ActionListener() {
				Path filePaths[] = (mediaFilePath == null || mediaFilePath.toString().matches("^https?:/.+")) 
						? new Path[] {commentFilePath} : new Path[] {commentFilePath, mediaFilePath};

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String basePath = getBasePathStr();
					
					switch (getStatus()) {
					case STATUS_SEARCH:
						if(commentFilePath.getParent() == null) {
							JOptionPane.showMessageDialog(DistributeButton.this, "配布する観察結果をFishWatchrに読み込んでください。");
							return;
						}
						messagePanel.append("- 次のファイルが配布対象です。\n");
						for(Path sendFilePath : filePaths) {
							messagePanel.append("-- " + sendFilePath.toString() + "\n");
						}
						
						messageReceiver = new PipeMessageReceiver(pipe, basePath,
								(memberMessage) -> {
									String memberName = memberMessage.getSenderName();
									String tempPath = memberMessage.getPath();
									PipeMessage myInfo = new PipeMessage(user.getUserName(), tempPath);
									
									if(messageReceiver.isMapped(memberName)) {
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												messagePanel.append("- " + memberName + "の接続が再度ありましたが，既登録なので，追加しません。\n");
												myInfo.setStatus(PipeMessage.STATUS_DUPLICATED);
												try {
													pipe.postMessage(tempPath, myInfo);
												} catch (IOException | URISyntaxException | InterruptedException e) {
													e.printStackTrace();
												}
											}
										});
										return;
									} else {
										myInfo.setStatus(PipeMessage.STATUS_CONTINUED);
									}

									messageReceiver.setMap(memberName, memberMessage);
									memberListPanel.addMember(memberMessage);
									messagePanel.append("- " + memberName + "をメンバーリストに追加しました。\n");
									try {
										pipe.postMessage(tempPath, myInfo);
									} catch (IOException | URISyntaxException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (InterruptedException e) {
										initState();
									}
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											setStatus(STATUS_EXECUTE);
										}
									});
								}, 
								(ex) -> {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											if(getStatus() != STATUS_CANCEL) { // for future.cancel in STATUS_EXECUTE
												return;
											}

											if(ex == PipeMessageReceiver.ERROR_PATH_ALREADY_USED) {
												JOptionPane.showMessageDialog(DistributeButton.this, "このパスはすでに使用されています。別のパスを使用してください。");
												messagePanel.append("- キャンセルしました。\n");
											} else if(ex == PipeMessageBroadcaster.ERROR_UNABLE_TO_RESERVE) {
												JOptionPane.showMessageDialog(DistributeButton.this, "ネットワークに接続できませんでした。処理を中止します。");
												messagePanel.append("- キャンセルしました。\n");
											}
											initState();
											memberListPanel.clear();
										}
									});
								});
						future = Executors.newSingleThreadExecutor().submit(messageReceiver);

						messagePanel.append("- メンバーを探しています。\n");
						setStatus(STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
					case STATUS_CANCEL2:
						messagePanel.append("- キャンセルしました。\n");
						future.cancel(true);
						initState();
						break;
					case STATUS_EXECUTE:
						int nSenders = memberListPanel.getMemberSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(DistributeButton.this, "メンバーが見つかっていません。");
							return;
						}
						
						setStatus(STATUS_CANCEL2);

						// Stop PipeMessageReceiver
						future.cancel(true);

						
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								String newPath = DataPiper.generatePath(user.getUserName() + basePath) + "?n=" + nSenders;
								PipeMessage myInfo = new PipeMessage(user.getUserName(), newPath);
								long dataSize = Util.getTotalFilesize(filePaths);
								myInfo.setDataSize(dataSize);
								progressBar.setMinimum(0);
								progressBar.setMaximum((int)dataSize);
								
								for(int i = 0; i < nSenders; i++) {
									MemberPanel memberPanel = memberListPanel.getMemberPanelAt(i);
									String memberName = memberPanel.getMember();
									PipeMessage memberInfo = messageReceiver.getMap(memberName);

									try {
										pipe.postMessage(memberInfo.getPath(), myInfo);
									} catch (URISyntaxException | IOException e) {
										JOptionPane.showMessageDialog(DistributeButton.this, e.getMessage());
										initState();
										memberListPanel.clear();
										return;
									} catch (InterruptedException e) {
										// postMessage() closes the pipe internally
										initState();
										memberListPanel.clear();
										return;
									}

								}

								try {
									pipe.postFile(newPath, filePaths, 
											(readLength)->{
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														progressBar.setValue(readLength.intValue());
													}
												});
											});
									setEnabled(true);
								} catch (URISyntaxException | ExecutionException | IOException e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(DistributeButton.this, "データ送信が中断されるなどして，配布が完了しませんでした。\n" + e.getMessage());
											initState();
											memberListPanel.clear();
											progressBar.setValue(progressBar.getMinimum());
										}
									});
									return;
								} catch (InterruptedException e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(DistributeButton.this, "配布を中止しました。");
											// postMessage() closes the pipe internally
											initState();
											memberListPanel.clear();
											progressBar.setValue(progressBar.getMinimum());
										}
									});
									return;
								}
								
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										messagePanel.append("- 配布が完了しました。\n");
										setStatus(STATUS_SEARCH);
										memberListPanel.clear();
										progressBar.setValue(progressBar.getMinimum());
										
										JOptionPane.showMessageDialog(DistributeButton.this, "配布が完了しました。");
									}
								});
							}
						});
						break;
					default:
						break;
					}
				}
			});
		}
	}
	
	
	private class CollectButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private static final String CollectionDir = "_COLLECTION";
		private final String[] labels = {"相手を探索", "キャンセル", "収集を実行", "キャンセル", "閉じる"};
		private PipeMessageBroadcaster messageBroadcaster = null;
		private Path saveRootPath = null;
		private ArrayList<Path> savePaths = new ArrayList<Path>();
	
		public CollectButton(MemberListPanel memberListPanel, MessagePanel messagePanel) {
			setLabels(labels);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					switch (getStatus()) {
					case STATUS_SEARCH:
						if(commentFilePath.getParent() == null) {
							JOptionPane.showMessageDialog(CollectButton.this, "動作が確認できている観察結果をFishWatrchrに読み込んだ上で，再度実行してください。");
								return;
						} else {
							saveRootPath = Util.getUniquePath(commentFilePath.getParent(), CollectionDir);
						}
						
						String basePath = getBasePathStr();
						PipeMessage myInfo = new PipeMessage(user.getUserName(), "");
						
						messageBroadcaster = new PipeMessageBroadcaster(pipe, basePath, myInfo,
								(updatedMessage) -> {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											String newPath = updatedMessage.getPath();

											try {
												PipeMessage memberInfo = pipe.getMessage(newPath);
												String senderName = memberInfo.getSenderName();
												if(messageBroadcaster.isMapped(senderName)) {
													messagePanel.append("- " + senderName + "の接続が再度ありましたが，既登録なので，追加しません。\n");
													return;
												}
												messageBroadcaster.setMap(senderName, memberInfo);
												memberListPanel.addMember(memberInfo);
												messagePanel.append("- " + senderName + "をメンバーリストに追加しました。\n");
												setStatus(STATUS_EXECUTE);
											} catch (URISyntaxException | IOException e) {
												JOptionPane.showMessageDialog(CollectButton.this, e.getMessage());
												initState();
												return;
											} catch (InterruptedException e) {
												// getMessage() closes the pipe internally
												initState();
												return;
											}
										}
									});
								}, (ex) -> {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											if(getStatus()  != STATUS_CANCEL) { // for future.cancel in STATUS_EXECUTE
												return;
											}
											
											if(ex == PipeMessageBroadcaster.ERROR_PATH_ALREADY_USED) {
												JOptionPane.showMessageDialog(CollectButton.this, "このパスはすでに使用されています。別のパスを使用してください。");
												messagePanel.append("- キャンセルしました。\n");
											} else if(ex == PipeMessageBroadcaster.ERROR_UNABLE_TO_RESERVE) {
												JOptionPane.showMessageDialog(CollectButton.this, "ネットワークに接続できませんでした。処理を中止します。");
												messagePanel.append("- キャンセルしました。\n");
											}
											initState();
										}
									});
								});
						future = Executors.newSingleThreadExecutor().submit(messageBroadcaster);
						messagePanel.append("- メンバーを探しています。\n");
						setStatus(STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
					case STATUS_CANCEL2:
						messagePanel.append("- キャンセルしました。\n");
						future.cancel(true);
						initState();
						break;
					case STATUS_EXECUTE:
						int nSenders = memberListPanel.getMemberSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(CollectButton.this, "メンバーが見つかっていません。");
							return;
						}

						
						setStatus(STATUS_CANCEL2);
						
						// Stop MessageBroadcaster
						future.cancel(true);
						
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								BlockingQueue<Future<Void>> queue = new ArrayBlockingQueue<>(nSenders);
								
								for(int i = 0; i < nSenders; i++) {
									final int i2 = i;
									MemberPanel memberPanel = memberListPanel.getMemberPanelAt(i);
									String memberName = memberPanel.getMember();
									Path savePath = saveRootPath.resolve(memberName);
											
									try {
										Files.createDirectories(savePath);
									} catch (IOException e) {
										messagePanel.append("保存用のフォルダを作成できません。\n" + memberName + "のファイルの保存処理をキャンセルします。\n");
										continue;
									}
									
									PipeMessage message = messageBroadcaster.getMap(memberName);
									
									var f = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
										@Override
										public Void call() throws URISyntaxException, IOException, InterruptedException, ExecutionException {
											try {
												pipe.getTarFile(message.getPath(), savePath, (readSize) -> {
													SwingUtilities.invokeLater(new Runnable() {
														@Override
														public void run() {
															memberPanel.setValue(readSize.intValue());
															memberListPanel.update(i2);
														}
													});
												});
											} catch (ExecutionException e) {
												throw new ExecutionException(memberName, e);
											}
											messagePanel.append("- 保存完了：" + memberName + ", " + savePath.toString() + "\n");

											return null;
										}
									});
									
									queue.add(f);
									savePaths.add(savePath);
								}
								

								for(Future<Void> f : queue) {
									try {
										f.get();
									} catch (InterruptedException e) {
										f.cancel(true);
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												initState();
												memberListPanel.clear();
											}
										});
										return;
									} catch (ExecutionException e) {
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												JOptionPane.showMessageDialog(CollectButton.this,
														// extract memberName
														e.getMessage().replaceFirst("^.*:", "") + "のデータ収集は途中でキャンセルされました。\n");
											}
										});
									}
								}
								
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										messagePanel.append("- 収集が完了しました。読み込み中のデータと収集したデータをマージをします。\n");
										JOptionPane.showMessageDialog(CollectButton.this,
												"収集が完了しました。\n"
												+ "「OK」ボタンを押すと，読み込み中のデータと収集したデータをマージをします。");
										setStatus(STATUS_FINISH);
										CollectButton.this.doClick();
									}
								});
						}
						});
//						memberListPanel.clearMember();

						break;
					case STATUS_FINISH:
						FileSharingDialog.this.setVisible(false);
						collectSuccess.accept(savePaths);
						break;
					default:
						break;
					}
				}
			});
		}
	}

	
	private class SendButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private final String[] labels = {"相手を探索", "キャンセル", "送信を実行", "キャンセル"};
		
		private String newPath = "";
		private long dataSize = 0;
		
		public SendButton(MemberPanel memberPanel, MessagePanel messagePanel, JCheckBox fileOptionCheckBox) {
			setLabels(labels);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Path filePaths[] = (mediaFilePath == null || mediaFilePath.toString().matches("^https?:/.+") || !fileOptionCheckBox.isSelected()) 
							? new Path[] {commentFilePath} : new Path[] {commentFilePath, mediaFilePath};
							
					switch (getStatus()) {
					case STATUS_SEARCH:
						if(commentFilePath.getParent() == null) {
							JOptionPane.showMessageDialog(SendButton.this, "送信する観察結果をFishWatchrに読み込んでください。");
							return;
						}
						messagePanel.append("- 次のファイルが送信対象です。\n");
						for(Path sendFilePath : filePaths) {
							messagePanel.append("-- " + sendFilePath.toString() + "\n");
						}
						fileOptionCheckBox.setEnabled(false);
						
						messagePanel.append("- メンバーを探しています。\n");
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							
							@Override
							public void run() {
								String basePath = getBasePathStr();
								String memberName = "";
								try {
									PipeMessage memberInfo = pipe.getMessage(basePath, N_RETRY);
									if(memberInfo.getErrorCode() > 0) {
										throw new IOException("このパスでは接続できませんでした。");
									}
									memberName = memberInfo.getSenderName();
									newPath = memberInfo.getPath();
									memberPanel.setMember(memberName);
									messagePanel.append("- " + memberName + "が見つかりました。\n");
								} catch (URISyntaxException | IOException e) {
									JOptionPane.showMessageDialog(SendButton.this, e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// getMessage() closes the pipe internally
									initState();
									return;
								}

								try {
									final String memberName2 = memberName;
									dataSize = Util.getTotalFilesize(filePaths);

									PipeMessage myInfo = new PipeMessage(user.getUserName(), newPath);
									myInfo.setDataSize(dataSize);
									pipe.postMessage(newPath, myInfo);
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											int result = JOptionPane.showConfirmDialog(SendButton.this, 
													memberName2 + "へファイルを送る場合は「OK」ボタンを押してください。",
													"送信の確認", JOptionPane.OK_CANCEL_OPTION);
											if(result == JOptionPane.OK_OPTION) {
												setStatus(STATUS_EXECUTE);
												SendButton.this.doClick();
												messagePanel.append("- " + "送信中です。\n");
											} else {
												messagePanel.append("- " + "キャンセルしました。\n");
												fileOptionCheckBox.setEnabled(true);
												initState();
												return;
											}
										}
									});
								} catch (URISyntaxException | IOException e) {
									JOptionPane.showMessageDialog(SendButton.this, e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									initState();
									return;
								}
							}
						});

						setStatus(STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
					case STATUS_CANCEL2:
						messagePanel.append("- キャンセルしました。\n");
						fileOptionCheckBox.setEnabled(true);
						future.cancel(true);
						break;
					case STATUS_EXECUTE:
						setStatus(STATUS_CANCEL2);

						memberPanel.initBar(0, (int)dataSize);

						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								try {
									pipe.postFile(newPath, filePaths, 
											(readLength)->{
												SwingUtilities.invokeLater(new Runnable() {
													
													@Override
													public void run() {
														memberPanel.setValue(readLength.intValue());
													}
												});
											});
								} catch (URISyntaxException | IOException | ExecutionException e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(SendButton.this, "送信が中止されました。\n" + e.getMessage());	
											messagePanel.append("- 送信が中止されました。\n");
											initState();
											memberPanel.clear();
											fileOptionCheckBox.setEnabled(true);
										}
									});
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(SendButton.this, "送信が中止されました。");	
											messagePanel.append("- 送信が中止されました。\n");
											initState();
											memberPanel.clear();
											fileOptionCheckBox.setEnabled(true);
										}
									});
									return;
								}

								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										messagePanel.append("- 送信が完了しました\n");
										
										JOptionPane.showMessageDialog(SendButton.this, "送信が完了しました。\n");
										setStatus(STATUS_SEARCH);
										memberPanel.clear();
										fileOptionCheckBox.setEnabled(true);
									}
								});
							}
						});
						break;
					default:
						break;
					}
				}
			});
		}
	}
	
	
	private class PipeActionButton extends JButton {
		private static final long serialVersionUID = 1L;
		protected final static int STATUS_SEARCH = 0;
		protected final static int STATUS_CANCEL = 1;
		protected final static int STATUS_EXECUTE = 2;
		protected final static int STATUS_CANCEL2 = 3;
		protected final static int STATUS_FINISH = 4;

		private String[] labels = {"phase_search", "phase_action", "phase_finish"};

		private int status = STATUS_SEARCH;
		protected Future<?> future = null;
	
		public PipeActionButton() {
			super();
			setStatus(status);
		}
		
		public void setLabels(String[] labels) {
			this.labels = labels;
			setStatus(status);
		}
		
		public void initState() {
			status = STATUS_SEARCH;
			setStatus(status);
			setEnabled(true);
		}
		

		public void setStatus(int status) {
			this.status  = status;
			setText(labels[status]);
		}
		
		
		public int getStatus() {
			return status;
		}
		
		public void cancelAction() {
			if(future != null && !future.isDone()) {
				future.cancel(true);
			}
		}
		
		
		public String getBasePathStr() {
			return DataPiper.getUrlParameterPath(user.getGroupName() + pathField.getText());
		}
	}
}
