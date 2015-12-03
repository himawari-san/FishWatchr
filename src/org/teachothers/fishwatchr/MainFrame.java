/*
    Copyright (C) 2014-2015 Masaya YAMAGUCHI

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String VERSION = "Ver.0.9.3 [20151104]";
	private static final String COPYRIGHT = "Copyright(c) 2014-2015 Masaya YAMAGUCHI";
	private static final int TASK_INTERVAL = 250;
	private static final int THRESHOLD_CLICK_INTERVAL = 800; // ms
	private static final int TAB_STATUS_GLOBAL_VIEW = 0;
	private static final int TAB_STATUS_DETAIL_VIEW = 1;
	private static final String FILE_PREFIX = "fw";
	
	public static final String USER_NOT_SPECIFIED = "noname";
	public static final int MAX_DISCUSSERS = 8;
	public static final int COMMENT_PANEL_HEIGHT = 270;
	public static final int BUTTON_PANEL_HEIGHT = 50;
	public static final int TIMELINE_PANEL_WIDTH = 512;
	public static final int TIMELINE_PANEL_HEIGHT = 360;
	
	
	
	private SoundPlayer soundPlayer;
	private SoundPanel soundPanel;

	private JTabbedPane timeLineTabbedPane;
	private JPanel displayPanel;
	private JPanel timeLinePanel;
	private AnnotationGlobalViewer annotationGlobalViewPanel;
	private JPanel moviePanel;
	private JPanel commentPanel;
	private JPanel buttonPanel;

	private JScrollPane scrollCommentTablePane;
	private CommentTable commentTable;
	private JScrollPane scrollCommentArea;
	private JTextArea commentArea;
	private JPanel operationPanel;
	private JPanel playerOperationPanel;
	private JPanel timeOperationPanel;

	private JButton soundPlayButton;
	private JButton soundForwardButton;
	private JButton soundBackwardButton;
	private JButton soundStopButton;
	private JButton soundRecordButton;

	private TimeDisplay timeCurrent;
	private TimeDisplay timeEnd;
	private TimeSlider timeSlider;

	private CommentTableModel ctm;

	private JMenuBar jMenuBar;
	private JMenu jMenuFile;
	private JMenuItem jMenuItemFileOpen;
	private JMenuItem jMenuItemURLOpen;
	private JMenuItem jMenuItemFileSave;
	private JMenuItem jMenuItemFileMerge;
	private JMenuItem jMenuItemFileExport;
	private JMenuItem jMenuItemFileExit;
	private JMenu jMenuControl;
	private JMenuItem jMenuItemControlPlayPause;
	private JMenuItem jMenuItemControlStop;
	private JMenuItem jMenuItemControlRecord;
	private JMenuItem jMenuItemControlSkipBackward;
	private JMenuItem jMenuItemControlSkipForward;
	private JMenuItem jMenuItemControlJumpToPrevComment;
	private JMenuItem jMenuItemControlJumpToNextComment;
	private JMenuItem jMenuItemControlScroll;
	private JMenuItem jMenuItemControlPlayRateUp;
	private JMenuItem jMenuItemControlPlayRateDown;
	private JMenuItem jMenuItemControlPlayRateReset;
	private JMenu jMenuAnnotation;
	private JMenuItem jMenuItemAnnotationYourName;
	private JMenuItem jMenuItemAnnotationDiscussers;
	private JMenuItem jMenuItemAnnotationAnnotation;
	private JMenuItem jMenuItemAnnotationOrderDiscusser;
	private JMenuItem jMenuItemAnnotationOrderType;
	private JMenuItem jMenuItemAnnotationMulti;
	private JMenuItem jMenuItemAnnotationTimeCorrection;
	private JMenu jMenuOption;
	private JMenuItem jMenuItemOptionTextOverlay;
	private JMenuItem jMenuItemOptionVideoRatio;
	private JMenuItem jMenuItemOptionSkipTime;
	private JMenuItem jMenuItemOptionJumpAdjustment;
	private JMenuItem jMenuItemOptionFocusRange;
	private JMenuItem jMenuItemOptionRecorderMode;
	private JMenuItem jMenuItemOptionViewSyncMode;
	private JMenuItem jMenuItemOptionWaveform;
	private JMenu jMenuHelp;
	private JMenuItem jMenuItemHelpVersion;

	private CommentList commentList;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<CommentButton> commentButtons;

	private String xf = "";
	private String mf = "";

	private DiscussersPanel discussersPanel;

	private ArrayList<User> discussers;
	private User commenter;

	private Timer timer;

	private String systemName;

	private int skipTime = 5000; // ジャンプ時のスキップ量（sec）
	private int adjustmentTimeAtJump = -2000; // ジャンプ時補正（再生，msec）
	// private int adjustmentJumpAtComment = 5; // ジャンプ時補正（コメント，行）
	private float playRate = 1.0f; // 再生速度
	private int iVideoAspectRate = 0;
	private int iTextOverlayStyle = 0;

	private int buttonType = CommentButton.BUTTON_TYPE_DISCUSSER; // ボタンタイプの初期値（討論者優先）

	// コメントテーブルの幅
	private int columnWidth[] = { 35, 150, 150, 150, 150, 2048 };

	// 録音+アノテーション or アノテーションのみ
	private boolean isRecorderMode = true;

	// テーブル表示同期
	private boolean isViewSyncMode = false;

	private boolean flagWaveform = false;

	private boolean isSoundPanelEnable = false;
	
	private String userHomeDir = "";
	
	// 強調表示の範囲（現在再生中のコメントから前後 x msec）
	private int focusRange = 10000; // msec
	
	private SysConfig config = new SysConfig();
	
	public MainFrame(String systemName) {
		this.systemName = systemName;

		setWindowTitle("");

		// コメントリスト
		commentList = new CommentList();
		// commenter 初期値
		commenter = new User(USER_NOT_SPECIFIED);

		// discussers 初期値
		discussers = new ArrayList<User>();
		// commentTypes 初期値
		commentTypes = new ArrayList<CommentType>();
		// 初期値の設定
//		addDefaultDiscussers();
//		addDefaultCommentTypes();
		config.load(commentTypes, discussers);
		

		soundPlayer = new SoundPlayer(this);

		ctm = new CommentTableModel(commentList, discussers, commentTypes);
		
		userHomeDir = System.getProperty("user.home");
		if(userHomeDir == null) {
			userHomeDir = "";
			System.err.println("Warning(MainFrame): Can not get the user homedir.");
		}
		
		
		ginit();
	}

	
	public void addDefaultDiscussers(){
		discussers.clear();
		discussers.add(new User("話者１"));
		discussers.add(new User("話者２"));
		discussers.add(new User("話者３"));
		discussers.add(new User("話者４"));
		discussers.add(new User("不特定"));
		discussers.add(new User("誤り"));
	}

	
	public void addDefaultCommentTypes(){
		commentTypes.clear();
		commentTypes.add(new CommentType("意見", Color.red));
		commentTypes.add(new CommentType("質問", Color.ORANGE));
		commentTypes.add(new CommentType("管理", Color.blue));
		commentTypes.add(new CommentType("相づち", Color.green));
		commentTypes.add(new CommentType("確認", Color.cyan));
		commentTypes.add(new CommentType("その他", Color.yellow));
		commentTypes.add(new CommentType("誤り", Color.magenta));
		commentTypes.add(new CommentType("", new Color(10,10,10)));
		commentTypes.add(new CommentType("", new Color(60,60,60)));
		commentTypes.add(new CommentType("", new Color(110,110,110)));
		commentTypes.add(new CommentType("", new Color(160,160,160)));
		commentTypes.add(new CommentType("", new Color(210,210,210)));
	}

	public void init() {
		// soundPanel.init();
		// timerStart();
	}

	public void timerStart() {
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new DrawGraphTask(), 0, TASK_INTERVAL);
	}

	public void ginit() {
		jMenuBar = getJMenuBar();
		setJMenuBar(jMenuBar);
		jMenuItemOptionRecorderMode.setSelected(isRecorderMode);
		jMenuItemOptionViewSyncMode.setSelected(isViewSyncMode);
		jMenuItemOptionWaveform.setSelected(flagWaveform);

		displayPanel = getDisplayPanel();
		commentPanel = getCommentPanel(); // get after getJMenuBar();
		commentPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
				COMMENT_PANEL_HEIGHT));
		getContentPane().add(displayPanel, BorderLayout.CENTER);
		getContentPane().add(commentPanel, BorderLayout.SOUTH);
		soundRecordButton.setForeground(Color.red);
		
		changeStateStop();



		// 閉じるボタンでいきなり閉じるのを抑制する
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				processesBeforeExit();
			}
		});

		// テーブル編集中のキー入力制御
		// CTRL-↑などが所得できないための対策
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {
					@Override
					public boolean dispatchKeyEvent(KeyEvent e) {
						if (e.getSource().getClass().getName()
								.endsWith(".CommentTable")
								|| commentTable.isAncestorOf(e.getComponent())) {
							if (e.isControlDown()
									&& (e.getKeyCode() == KeyEvent.VK_UP
											|| e.getKeyCode() == KeyEvent.VK_DOWN
											|| e.getKeyCode() == KeyEvent.VK_LEFT
											|| e.getKeyCode() == KeyEvent.VK_RIGHT
											|| e.getKeyCode() == KeyEvent.VK_SLASH
											||
											// e.getKeyCode() ==
											// KeyEvent.VK_PLUS ||
											// e.getKeyCode() ==
											// KeyEvent.VK_MINUS ||
											e.getKeyCode() == KeyEvent.VK_S
											|| e.getKeyCode() == KeyEvent.VK_P || e
											.getKeyCode() == KeyEvent.VK_J)) {
								KeyboardFocusManager
										.getCurrentKeyboardFocusManager()
										.redispatchEvent(jMenuBar, e);
								return true;
							}
						}
						return false;
					}
				});
	}

	private JPanel getCommentPanel() {
		if (commentPanel == null) {

			commentPanel = new JPanel();
			commentPanel.setLayout(new BorderLayout());
			operationPanel = getOperationPanel();
			buttonPanel = getButtonPanel();
			int bbbbb;
			buttonPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, BUTTON_PANEL_HEIGHT));
			commentPanel.add(operationPanel, BorderLayout.NORTH);
			commentPanel.add(buttonPanel, BorderLayout.SOUTH);

			commentTable = new CommentTable(ctm);
			commentTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					int row = commentTable.getSelectedRow();
					int column = commentTable.getSelectedColumn();
					ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
					if (column == Comment.F_COMMENT) {
						System.out.println("コメント欄がクリックされました");
					} else if (e.getClickCount() == 2) {
						Comment selectedComment = filteredCommentList.get(row);
						long commentTime = commentList.unifiedCommentTime(selectedComment)
								+ adjustmentTimeAtJump; // msec

						if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_STOP) {
							if(mf.isEmpty() || (!SoundPlayer.isPlayable(mf) && !mf.matches("^https?://.+"))){
								JOptionPane.showMessageDialog(MainFrame.this, "再生できるファイルではありません。\n" + mf);
								return;
							}

							if(!soundPlayer.setFile(mf, jMenuItemOptionWaveform.isSelected())){
//								JOptionPane.showMessageDialog(MainFrame.this, "再生が開始できません。\n" + mf);
								return;
							}
							isSoundPanelEnable = soundPlayer.getSoundBufferEnable();

							timeSlider.setEnabled(true);

							changeStatePlay();
							soundPlayer.myPlay();
							soundPlayer.setPlayPoint(commentTime);
							timerStart();
						} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
							changeStatePlay();
							soundPlayer.myPlay();
							soundPlayer.setPlayPoint(commentTime);
						} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PLAY) {
							soundPlayer.setPlayPoint(commentTime);
						}
						System.out.println("ここにジャンプして，再生します。" + commentTime
								+ " msec");
					}
				}
			});

			scrollCommentTablePane = new JScrollPane(commentTable);
			commentPanel.add(scrollCommentTablePane, BorderLayout.CENTER);
			for (int i = 0; i < columnWidth.length; i++) {
				commentTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
				commentTable.getColumn(Comment.headers[i]).setMinWidth(0);
				commentTable.getColumn(Comment.headers[i]).setMaxWidth(
						columnWidth[i]);
			}

			commentArea = new JTextArea();
			scrollCommentArea = new JScrollPane(commentArea);
			scrollCommentArea.setPreferredSize(new Dimension(0, 50));
		}
		return commentPanel;
	}

	private JPanel getOperationPanel() {
		if (operationPanel == null) {
			operationPanel = new JPanel();
			operationPanel.setLayout(new BoxLayout(operationPanel,
					BoxLayout.X_AXIS));
			playerOperationPanel = getPlayerOperationPanel();
			timeOperationPanel = getTimeOperationPanel();
			operationPanel.add(playerOperationPanel);
			operationPanel.add(timeOperationPanel);
		}

		return operationPanel;
	}

	private JPanel getPlayerOperationPanel() {
		if (playerOperationPanel == null) {
			playerOperationPanel = new JPanel();

			soundPlayButton = new JButton("▶");
			soundForwardButton = new JButton("▶▶");
			soundBackwardButton = new JButton("◀◀");
//			soundStopButton = new JButton("■");
			soundStopButton = new JButton(String.valueOf('\u25A0'));
			soundRecordButton = new JButton("●");

			playerOperationPanel.add(soundBackwardButton);
			playerOperationPanel.add(soundPlayButton);
			playerOperationPanel.add(soundForwardButton);
			playerOperationPanel.add(soundStopButton);
			playerOperationPanel.add(soundRecordButton);

			soundRecordButton.setMnemonic(KeyEvent.VK_R);
			soundPlayButton.setMnemonic(KeyEvent.VK_P);
			// 誤動作を避けるため，やめた
			// soundStopButton.setMnemonic(KeyEvent.VK_S);
			soundForwardButton.setMnemonic(KeyEvent.VK_RIGHT);
			soundBackwardButton.setMnemonic(KeyEvent.VK_LEFT);

			soundRecordButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							try {
								saveCommentList();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							SimpleDateFormat today = new SimpleDateFormat("yyyyMMdd");
							String basename = userHomeDir + File.separator 
									+ FILE_PREFIX + today.format(new Date()) + "_" + commenter;

							if (jMenuItemOptionRecorderMode.isSelected()) {
								mf = getUniqueFilename(basename + SoundPlayer.SOUNDFILE_EXTENSION);
								xf = mf + CommentList.FILE_SUFFIX;
								isSoundPanelEnable = true;
							} else {
								mf = "";
								xf = getUniqueFilename(basename + CommentList.FILE_SUFFIX);
							}
							
							setWindowTitle(xf);
							// for testing
//							commentTable.resetPosition();
							commentList.clear();
							commentList.setMediaFilename(mf);
							commentList.setSetName(xf, commenter);
							ctm.refreshFilter();
							ctm.fireTableDataChanged();

							soundPlayer.init();
							soundPlayer.setDefaultRecordingParameters();
//							soundPlayer.setTargetFilename(targetFilename);

							timeSlider.setMinimum(0);
							timeSlider.setMaximum(SoundPlayer.LIMIT_RECODING_TIME);
							timeEnd.setTime(SoundPlayer.LIMIT_RECODING_TIME);
//							timerStart();
							changeStateRecord();
							soundPlayer.myRecord(mf, jMenuItemOptionRecorderMode.isSelected());
							timerStart();
							commentList.setStartTime(soundPlayer.getStartTime());
						}
					});

			soundStopButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							int playerState = soundPlayer.getPlayerState();
							soundPlayer.myStop();
							timer.cancel();
							if(playerState == SoundPlayer.PLAYER_STATE_RECORD && !jMenuItemOptionRecorderMode.isSelected()){
								changeStateStop();
							}
						}
					});

			soundPlayButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if(!mf.isEmpty()
									&& !SoundPlayer.isPlayable(mf)
									&& !mf.matches("^https?://.+")){
								JOptionPane.showMessageDialog(MainFrame.this, "再生できるファイルではありません。");
								return;
							}

							if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PLAY) {
								changeStatePause();
								soundPlayer.myPause();
								return;
							} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
								changeStatePlay();
								soundPlayer.myResume();
								return;
							} else {
								if (mf.isEmpty() && xf.isEmpty()) {
									if(!setTargetFile(true)){
										return;
									}
								}

								// アノテーションだけのデータの場合
								if(mf.isEmpty()){
									JOptionPane.showMessageDialog(MainFrame.this, "対応する音声ファイルがありません。");
									return;
								}

								// 再開した時のため
								timeSlider.setEnabled(true);

								changeStatePlay();
								soundPlayer.myPlay();
								timerStart();
							}
						}
					});

			soundForwardButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							System.err.println("forward!");
							soundPlayer.forward(skipTime); // msec
						}
					});

			soundBackwardButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							System.err.println("backward!");
							soundPlayer.backward(skipTime); // msec
						}
					});
		}

		return playerOperationPanel;
	}

	
	private boolean setTargetFile(boolean isFile) {
		try {
			saveCommentList();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(MainFrame.this, "コメントデータのバックアップ保存時にエラーが発生しました。\n" + e);
			e.printStackTrace();
			return false;
		}

		playRate = 1.0f;
		iVideoAspectRate = 0;

		commentTable.resetPosition();
		setWindowTitle(xf);
		String selectedFilename;
		if(isFile){
			selectedFilename = chooseTargetFile();
		} else {
			JOptionPane pane = new JOptionPane("例： https://www.youtube.com/watch?v=your_input", JOptionPane.PLAIN_MESSAGE);
			pane.setWantsInput(true);
			pane.setInputValue("");
			pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			JDialog dialog = pane.createDialog(this, "URLの入力");
			dialog.setSize(500, 150);
			dialog.setVisible(true);
			selectedFilename = (String) pane.getInputValue();
			if(pane.getValue() == null || selectedFilename.isEmpty()){
				return false;
			}
			mf = selectedFilename;
			xf = getUniqueFilename(userHomeDir + File.separator
					+ FILE_PREFIX + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "_" + commenter + CommentList.FILE_SUFFIX);
			commentList.clear();
			commentList.setStartTime(new Date());
			commentList.setMediaFilename(mf);
			ctm.refreshFilter();
			setDefaultButton();
			updateButtonPanel(buttonType);
			ctm.fireTableDataChanged();
		}

		
		// 読み込んだファイルがメディアファイルの場合
		if(SoundPlayer.isPlayable(selectedFilename)){
			String oldMf = mf;
			String oldXf = xf;
			mf = selectedFilename;
			xf = mf + CommentList.FILE_SUFFIX;
			if(new File(xf).exists()){
				String newXf = getUniqueFilename(xf);
				int result = JOptionPane.showConfirmDialog(this, "注釈ファイル（" + new File(xf).getName() + "）がすでに存在します。\n" +
						"「はい」を選択すると，新規に注釈ファイル（" + new File(newXf).getName() + "）を生成します。\n" +
						"「いいえ」を選択すると，既存の注釈ファイルを読み込みます。\n" +
						"「取り消し」を選択すると，処理を中止します。");
				
				switch(result){
					case JOptionPane.OK_OPTION:
						commentList.clear();
//						commentList.setSetName(xf, commenter);
						commentList.setStartTime(new Date());
						commentList.setMediaFilename(mf);
						ctm.refreshFilter();
						setDefaultButton();
						updateButtonPanel(buttonType);
						ctm.fireTableDataChanged();
						xf = newXf;
						break;
					case JOptionPane.NO_OPTION:
						selectedFilename = xf;
						break;
					case JOptionPane.CANCEL_OPTION:
						xf = oldXf;
						mf = oldMf;
						return false;
				}
			} else {
				commentList.clear();
				commentList.setStartTime(new Date());
				commentList.setMediaFilename(mf);
				ctm.refreshFilter();
				setDefaultButton();
				updateButtonPanel(buttonType);
				ctm.fireTableDataChanged();
			}
		}
		// 読み込んだファイルが注釈ファイル(XML)の場合
		else if(selectedFilename.endsWith(CommentList.FILE_SUFFIX)){
			xf = selectedFilename;
			try {
				mf = commentList.load(selectedFilename, commentTypes, discussers, false);
			} catch (XPathExpressionException | ParseException
					| ParserConfigurationException
					| SAXException | IOException e2) {
				JOptionPane.showMessageDialog(MainFrame.this, "データファイルの読み込み時にエラーが発生しました。\n" + e2);
				e2.printStackTrace();
				return false;
			}
			ctm.refreshFilter();
			addDefaultButton();
			updateButtonPanel(buttonType);
			ctm.fireTableDataChanged();

			if(mf.isEmpty()){
				return false;
			}
		} else if (selectedFilename.isEmpty()) {
			timeSlider.setMinimum(0);
			timeSlider.setEnabled(false);
			return false;
		} else if (isFile && !SoundPlayer.isPlayable(selectedFilename)) {
			// 注釈ファイルも存在しない場合。する場合は，ここをスルーするはず
			timeSlider.setMinimum(0);
			timeSlider.setEnabled(false);
			return false;
		}
		
		if(!soundPlayer.setFile(mf, jMenuItemOptionWaveform.isSelected())){
			JOptionPane.showMessageDialog(MainFrame.this, "再生が開始できません。\n" + mf);
			mf = "";
			xf = "";
			return false;
		}
		isSoundPanelEnable = soundPlayer.getSoundBufferEnable();

		
		commentList.setSetName(xf, commenter);

		setWindowTitle(xf);
		timerStart();
		timeSlider.setMinimum(0);
		timeSlider.setMaximum((int) soundPlayer.getSoundLength());
		timeSlider.setEnabled(true);
		timeEnd.setTime((int) soundPlayer.getSoundLength());
		annotationGlobalViewPanel.updatePanel();
		return true;
	}

	
	// すでにファイル yyy.xml が存在する場合は，yyy.001.xml を返す
	static public String getUniqueFilename(String filename){
		String nameBody;
		String suffix;
		int p = filename.lastIndexOf(".");
		int c = 1;
		
		if(p != -1){
			nameBody = filename.substring(0, p);
			suffix = filename.substring(p, filename.length());
		} else {
			nameBody = filename;
			suffix = "";
		}

		if(new File(filename).exists()){
			while(true){
				String newFilename = nameBody + String.format(".%03d", c++) + suffix;
				if(new File(newFilename).exists()){
					continue;
				} else {
					return newFilename;
				}
			}
		} else {
			return filename;
		}
	}

	
	private void setDefaultButton(){
		addDefaultCommentTypes();
		addDefaultDiscussers();
	}

	
	private void addDefaultButton(){
		boolean flagCommentTypesExist = false;
		boolean flagDiscussersExist = false;
		
		for(CommentType commentType: commentTypes){
			if(!commentType.getType().isEmpty()){
				flagCommentTypesExist = true;
				break;
			}
		}	
		
		if(!flagCommentTypesExist){
			addDefaultCommentTypes();
		}
		

		for(User discusser: discussers){
			if(!discusser.getName().isEmpty()){
				flagDiscussersExist = true;
				break;
			}
		}	

		if(!flagDiscussersExist){
			addDefaultDiscussers();
		}
	}
	

	
	private void saveCommentList() throws IOException {
		// 編集中のセルはキャンセル
		if(commentTable.isEditing()){
//			JOptionPane.showMessageDialog(this, "編集中のセルは値を確定させてください。");
			commentTable.getCellEditor().cancelCellEditing();
		}

		if (commentList.isModified() || (!xf.isEmpty() && !(new File(xf).exists()))) {
			if(xf.isEmpty()){
				xf = userHomeDir + File.separator + "fw_noname.xml";
				System.err.println("Warning(MainFrame): ファイル名がつけられていません。\n" + xf + "として保存します。");
			}
			
			String message = commentList.save(xf, commentTypes, discussers);
			JOptionPane.showMessageDialog(this, message);
			return;
		}
	}

	private void processesBeforeExit() {

		switch (soundPlayer.getPlayerState()) {
		case SoundPlayer.PLAYER_STATE_RECORD:
				JOptionPane.showMessageDialog(this, "データ記録中です。\n終了ボタンを押して，記録を終了させください。");
				return;
		case SoundPlayer.PLAYER_STATE_PAUSE:
				soundPlayer.myResume();
		case SoundPlayer.PLAYER_STATE_PLAY:
			soundPlayer.myStop();
			break;
		default:
		}

		try {
			saveCommentList();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "データ保存時に問題が発生したため，強制終了します。\n" + xf + " の内容を確認してください。\n問題があれば，バックアップファイルを参照してください。\n" + e.getMessage());
			e.printStackTrace();
//			System.exit(0);
		}

		System.exit(0);
	}


	
	public void changeState(int state){
		switch(state){
		case SoundPlayer.PLAYER_STATE_PAUSE:
			changeStatePause(); break;
		case SoundPlayer.PLAYER_STATE_PLAY:
			changeStatePlay(); break;
		case SoundPlayer.PLAYER_STATE_RECORD:
			changeStateRecord(); break;
		case SoundPlayer.PLAYER_STATE_STOP:
			changeStateStop(); break;
		}
	}
	
	
	private void changeStateRecord() {
		soundStopButton.setEnabled(true);
		soundRecordButton.setEnabled(false);
		soundPlayButton.setEnabled(false);
		soundForwardButton.setEnabled(false);
		soundBackwardButton.setEnabled(false);
		jMenuItemFileOpen.setEnabled(false);
		jMenuItemURLOpen.setEnabled(false);
		jMenuItemFileSave.setEnabled(false);
		jMenuItemFileMerge.setEnabled(false);
		timeSlider.setEnabled(false);
	}

	private void changeStateStop() {
		// セルの編集内容の確定
		if (commentTable.getCellEditor() != null) {
			commentTable.getCellEditor().stopCellEditing();
		}
		
		// saveAndClearCommentList();
		soundStopButton.setEnabled(false);
		soundRecordButton.setEnabled(true);
		soundPlayButton.setEnabled(true);
		soundForwardButton.setEnabled(false);
		soundBackwardButton.setEnabled(false);
		soundPlayButton.setText("▶");
		jMenuItemFileOpen.setEnabled(true);
		jMenuItemURLOpen.setEnabled(true);
		jMenuItemFileSave.setEnabled(true);
		jMenuItemFileMerge.setEnabled(true);
		jMenuItemControlSkipBackward.setEnabled(false);
		jMenuItemControlSkipForward.setEnabled(false);
		jMenuItemControlPlayPause.setEnabled(true);
		jMenuItemControlRecord.setEnabled(true);
		jMenuItemControlStop.setEnabled(false);
		jMenuItemControlJumpToPrevComment.setEnabled(false);
		jMenuItemControlJumpToNextComment.setEnabled(false);
//		soundPlayer.setPlayPoint(0);
		timeCurrent.setTime(0);
		timeSlider.setValue(0);
		timeSlider.setEnabled(false);
		soundPanel.repaint();
		discussersPanel.repaintComponents();
		commentTable.initState();
		soundPlayer.setOverlayText("");
	}

	private void changeStatePlay() {
		soundStopButton.setEnabled(true);
		soundRecordButton.setEnabled(false);
		soundPlayButton.setEnabled(true);
		soundForwardButton.setEnabled(true);
		soundBackwardButton.setEnabled(true);
		soundPlayButton.setText("〓");
		jMenuItemFileOpen.setEnabled(false);
		jMenuItemURLOpen.setEnabled(false);
		jMenuItemFileSave.setEnabled(true);
		jMenuItemFileMerge.setEnabled(false);
		jMenuItemControlSkipBackward.setEnabled(true);
		jMenuItemControlSkipForward.setEnabled(true);
		jMenuItemControlPlayPause.setEnabled(true);
		jMenuItemControlRecord.setEnabled(false);
		jMenuItemControlStop.setEnabled(true);
		jMenuItemControlJumpToPrevComment.setEnabled(true);
		jMenuItemControlJumpToNextComment.setEnabled(true);
		// timeSlider.setEnabled(true);
	}

	private void changeStatePause() {
		soundStopButton.setEnabled(true);
		soundRecordButton.setEnabled(false);
		soundPlayButton.setEnabled(true);
		soundForwardButton.setEnabled(true);
		soundBackwardButton.setEnabled(true);
		soundPlayButton.setText("▶");
		jMenuItemFileOpen.setEnabled(false);
		jMenuItemURLOpen.setEnabled(false);
		jMenuItemFileSave.setEnabled(true);
		jMenuItemControlSkipBackward.setEnabled(true);
		jMenuItemControlSkipForward.setEnabled(true);
		jMenuItemControlPlayPause.setEnabled(true);
		jMenuItemControlRecord.setEnabled(false);
		jMenuItemControlStop.setEnabled(true);
		// timeSlider.setEnabled(true);
	}

	private JPanel getTimeOperationPanel() {
		if (timeOperationPanel == null) {
			timeOperationPanel = new JPanel();
			timeOperationPanel.setLayout(new BoxLayout(timeOperationPanel,
					BoxLayout.X_AXIS));
			timeCurrent = new TimeDisplay();
			timeCurrent.setPreferredSize(new Dimension(75, 20));
			timeCurrent.setTime(0);
			timeEnd = new TimeDisplay();
			timeEnd.setTime(0);
			// timeDisplay.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			timeSlider = new TimeSlider();
			timeSlider.setPreferredSize(new Dimension(250, 20));
			timeSlider.setMinimum(0);
			timeSlider.setValue(0);
			timeSlider.setMajorTickSpacing(300);
			timeSlider.setMinorTickSpacing(60);
			timeSlider.setPaintTicks(true);
			timeSlider.addChangeListener(new ChangeListener() {
				boolean isDragged = false;
				int value = 0;

				public void stateChanged(ChangeEvent e) {
					int currentState = soundPlayer.getPlayerState();
					
					// ドラッグしている時
					if (timeSlider.getValueIsAdjusting()) {
						soundPlayer.myPause();
						timer.cancel();
						value = timeSlider.getValue();
						isDragged = true;
						// ドラッグし終わった時
					} else if (isDragged && !timeSlider.getValueIsAdjusting()) {
						if(currentState != SoundPlayer.PLAYER_STATE_PAUSE){
							soundPlayer.myResume();
						}
						soundPlayer.setPlayPoint(value * 1000);
						isDragged = false;
						// スライダーが一瞬元に戻るのを防ぐため，次の soundGraphBuffer が更新されるのを待つ
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						timerStart();
					}
				}
			});
			timeOperationPanel.add(timeCurrent);
			timeOperationPanel.add(timeSlider);
			timeOperationPanel.add(timeEnd);
		}

		return timeOperationPanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			commentButtons = new ArrayList<CommentButton>();

			updateButtonPanel(buttonType);
		}
		return buttonPanel;
	}


	private JPanel getDisplayPanel() {
		if (displayPanel == null) {
			displayPanel = new JPanel();
			displayPanel.setLayout(new BorderLayout());
			displayPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

			timeLinePanel = getTimeLinePanel();
			annotationGlobalViewPanel = new AnnotationGlobalViewer(ctm, soundPlayer, discussers, commentTypes);
			annotationGlobalViewPanel.setFocusRange(focusRange);

			moviePanel = getMoviePanel();
			moviePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			timeLineTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			timeLineTabbedPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			
			timeLineTabbedPane.addTab("全体", annotationGlobalViewPanel);
			timeLineTabbedPane.addTab("詳細", timeLinePanel);
			timeLineTabbedPane.setPreferredSize(new Dimension(TIMELINE_PANEL_WIDTH, TIMELINE_PANEL_HEIGHT));
			timeLineTabbedPane.setSelectedIndex(TAB_STATUS_DETAIL_VIEW); // デフォルトは詳細
			displayPanel.add(timeLineTabbedPane, BorderLayout.WEST);
			displayPanel.add(moviePanel, BorderLayout.CENTER);
		}
		return displayPanel;
	}

	
	Timer t;
	private JPanel getMoviePanel() {
		if (moviePanel == null) {
			moviePanel = new JPanel();
			moviePanel.setLayout(new BorderLayout());
			moviePanel.add(soundPlayer.getMediaplayerComponent());
			soundPlayer.getMediaplayerComponent().setOpaque(false); // これがないと背景がおかしくなる
			moviePanel.addComponentListener(new ComponentAdapter() {
				public void componentResized(final ComponentEvent ev) {
					if(t != null) t.cancel();
					t = new Timer();
					t.schedule(new TimerTask() {
						public void run() {
							Dimension size = ev.getComponent().getSize();
							if(timer != null){
								timer.cancel();
							}
							soundPlayer.resizeMediaPlayer(size.width, size.height);
							annotationGlobalViewPanel.updatePanel();
							timerStart();
							System.err.println(ev.getComponent().getSize());
						}
					}, 200);
				}
			});
		}
		return moviePanel;
	}

	
	private JPanel getTimeLinePanel() {
		if (timeLinePanel == null) {
			timeLinePanel = new JPanel();
			timeLinePanel.setLayout(new BoxLayout(timeLinePanel,
					BoxLayout.Y_AXIS));
			// timeLinePanel.setPreferredSize(new
			// Dimension((int)(FishWatchAR.WINDOW_WIDTH*3),
			// FishWatchAR.VIEWER_HEIGHT * FishWatchAR.nUsers));
			timeLinePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			soundPanel = getSoundPanel();
//			soundPanel.setOpaque(false);
			timeLinePanel.add(soundPanel);
			discussersPanel = new DiscussersPanel(discussers, MAX_DISCUSSERS, ctm, soundPlayer);
			timeLinePanel.add(discussersPanel);

		}
		return timeLinePanel;
	}


	private SoundPanel getSoundPanel() {
		if (soundPanel == null) {
			soundPanel = new SoundPanel(soundPlayer);
			soundPanel
					.setPreferredSize(new Dimension(
							FishWatchr.WINDOW_WIDTH,
							FishWatchr.SOUND_VIEWER_HEIGHT));
			soundPanel
					.setMaximumSize(new Dimension(FishWatchr.WINDOW_WIDTH,
							FishWatchr.SOUND_VIEWER_HEIGHT));
			soundPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		}
		return soundPanel;
	}

	public JMenuBar getJMenuBar() {
		if (jMenuBar == null) {
			jMenuBar = new JMenuBar();
			jMenuFile = getJMenuFile();
			jMenuControl = getJMenuControl();
			jMenuAnnotation = getJMenuAnnotation();
			jMenuOption = getJMenuOption();
			jMenuHelp = getJMenuHelp();
			jMenuBar.add(jMenuFile);
			jMenuBar.add(jMenuControl);
			jMenuBar.add(jMenuAnnotation);
			jMenuBar.add(jMenuOption);
			jMenuBar.add(jMenuHelp);
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.addKeyEventPostProcessor(new KeyEventPostProcessor() {

						@Override
						public boolean postProcessKeyEvent(KeyEvent e) {
							// TODO Auto-generated method stub
							// KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(CommentTable.this,
							// e);
							return false;
						}
					});

			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.addKeyEventDispatcher(new KeyEventDispatcher() {

						@Override
						public boolean dispatchKeyEvent(KeyEvent e) {
							// KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(MainFrame.this,
							// e);
							// TODO Auto-generated method stub
							return false;
						}
					});
			setFocusable(true);

		}
		return jMenuBar;
	}

	private JMenu getJMenuFile() {
		if (jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setText("ファイル");
			jMenuFile.add(getJMenuItemFileOpen());
			jMenuFile.add(getJMenuItemURLOpen());
			jMenuFile.add(getJMenuItemFileSave());
			jMenuFile.add(getJMenuItemFileExport());
			jMenuFile.add(getJMenuItemFileMerge());
			jMenuFile.add(getJMenuItemFileExit());
		}
		return jMenuFile;
	}

	private JMenuItem getJMenuItemFileOpen() {
		if (jMenuItemFileOpen == null) {
			jMenuItemFileOpen = new JMenuItem("ファイルを開く");
			jMenuItemFileOpen.setAccelerator(KeyStroke.getKeyStroke('O',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileOpen
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayer.myStop();
							if(!setTargetFile(true)){
								return;
							}
							int aaa;
							changeStatePlay();
							soundPlayer.myPlay();
							timerStart();
						}
					});
		}
		return jMenuItemFileOpen;
	}

	private JMenuItem getJMenuItemURLOpen() {
		if (jMenuItemURLOpen == null) {
			jMenuItemURLOpen = new JMenuItem("URLを開く");
			jMenuItemURLOpen.setAccelerator(KeyStroke.getKeyStroke('U',
					KeyEvent.CTRL_MASK, false));
			jMenuItemURLOpen
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayer.myStop();
							if(!setTargetFile(false)){
								return;
							}
							int aaa;
							changeStatePlay();
							soundPlayer.myPlay();
							timerStart();
						}
					});
		}
		return jMenuItemURLOpen;
	}

	private String chooseFile(FileFilter filter, int fileType, boolean isSaveDialog) {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(fileType);

		if (filter != null) {
			
			jfc.setFileFilter(filter);
		}

		int result;
		if(isSaveDialog){
			result = jfc.showSaveDialog(MainFrame.this);
		} else {
			result = jfc.showOpenDialog(MainFrame.this);
		}
		
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				return jfc.getSelectedFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	
	private String chooseTargetFile() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String allSuffix[] = Arrays.copyOf(SoundPlayer.getPlayableFileExtensions(), SoundPlayer.getPlayableFileExtensions().length+1);
		allSuffix[SoundPlayer.getPlayableFileExtensions().length] = "xml";
		
		FileFilter fishWatchrFilter = new FishWatchrFileFilter("注記・音声・動画ファイル", FishWatchrFileFilter.TYPE_ALL);
		FileFilter mediaFilter = new FishWatchrFileFilter("音声・動画ファイル", FishWatchrFileFilter.TYPE_MEDIA);
		FileFilter xmlFilter = new FishWatchrFileFilter("注記ファイル", FishWatchrFileFilter.TYPE_XML);
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(fishWatchrFilter);
		jfc.addChoosableFileFilter(xmlFilter);
		jfc.addChoosableFileFilter(mediaFilter);

		int result = jfc.showOpenDialog(MainFrame.this);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				return jfc.getSelectedFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	

	class FishWatchrFileFilter extends FileFilter {

		static final int TYPE_ALL = 0; 
		static final int TYPE_MEDIA = 1;
		static final int TYPE_XML = 2;
		String description;
		int type = TYPE_ALL; 
		

		public FishWatchrFileFilter(String description){
			this.description = description;
		}
		
		public FishWatchrFileFilter(String description, int type){
			this.description = description;
			if(type > TYPE_XML){
				this.type = TYPE_ALL;
			} else {
				this.type = type;
			}
		}
		
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			
			String filename = f.getName();
			
			if(filename.endsWith(SoundPlayer.SOUNDFILE_EXTENSION) &&
					(type == TYPE_ALL || type == TYPE_MEDIA)){
				// 生成元のメディアファイルがないか調べる
				String candidate = filename.replaceFirst(SoundPlayer.SOUNDFILE_EXTENSION + "$",  "");
				if(SoundPlayer.isPlayable(candidate)){
					for(String child: new File(f.getParent()).list()){
						// 生成元の wav ファイルがある場合は，リストに表示しない
						if(child.equals(candidate)){
							return false;
						}
					}
				}
				return true;
			} else if(SoundPlayer.isPlayable(filename) &&
					(type == TYPE_ALL || type == TYPE_MEDIA)){
				return true;
			} else if(filename.endsWith(CommentList.FILE_SUFFIX) &&
					(type == TYPE_ALL || type == TYPE_XML)){
				return true;
			} else {
				return false;
			}
		}

		public String getDescription() {
			return description;
		}
	}
	
	
	private JMenuItem getJMenuItemFileSave() {
		if (jMenuItemFileSave == null) {
			jMenuItemFileSave = new JMenuItem("注釈結果を保存");
			jMenuItemFileSave.setAccelerator(KeyStroke.getKeyStroke('S',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileSave
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String message = "";
							try {
								if(xf.isEmpty()){
									JOptionPane.showMessageDialog(MainFrame.this, "ファイル名が未指定のため，保存できません。\n新規に注釈をつける場合は，録音状態で行ってください。");
									return;
								} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PLAY) {
									soundPlayer.myPause();
									message = commentList.save(xf, commentTypes, discussers);
									soundPlayer.myResume();
								} else {
									message = commentList.save(xf, commentTypes, discussers);
								}
							} catch (IOException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
							}
							
							if(!message.isEmpty()){
								JOptionPane.showMessageDialog(MainFrame.this, message);
							}
						}
					});
		}
		return jMenuItemFileSave;
	}

	
	private JMenuItem getJMenuItemFileExport() {
		if (jMenuItemFileExport == null) {
			jMenuItemFileExport = new JMenuItem("注釈結果をエクスポート");
			jMenuItemFileExport.setAccelerator(KeyStroke.getKeyStroke('E',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileExport
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								exportAsTSV();
							} catch (IOException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.this,
										"ファイルの書き込み時にエラーが発生しました。\n" + e1.toString());
							}
						}
					});
		}
		return jMenuItemFileExport;
	}

	
	private void exportAsTSV() throws IOException{
		String saveFilename = chooseFile(null, JFileChooser.FILES_ONLY, true);
		if(saveFilename.isEmpty()){
			JOptionPane.showMessageDialog(MainFrame.this, "ファイルが指定されなかったので，処理を中止します。");
			return;
		} else if(!saveFilename.endsWith(".tsv")){
			saveFilename += ".tsv";
		}
		
		File saveFile = new File(saveFilename);
		if(saveFile.exists()){
			int response = JOptionPane.showConfirmDialog(MainFrame.this, saveFile.getName() + "は，すでに存在します。\n上書きしますか？", "上書きの確認", JOptionPane.OK_CANCEL_OPTION);
			if(response != JOptionPane.OK_OPTION){
				JOptionPane.showMessageDialog(MainFrame.this, "ファイルの書き込みを中止します。");
				return;
			}
		}
		
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveFile)));
		
		
		for(Comment comment: commentList){
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < Comment.N_Field; i++){
				sb.append(comment.getAt(i).toString() + "\t");
			}
			pw.println(sb.toString().replaceFirst("\t$", ""));
		}
		
		pw.close();
		JOptionPane.showMessageDialog(MainFrame.this, "ファイル(" + saveFile.getName() + ")への書き込みが完了しました。");
	}
	
	
	private JMenuItem getJMenuItemFileMerge() {
		if (jMenuItemFileMerge == null) {
			jMenuItemFileMerge = new JMenuItem("注釈ファイルを合併");
			jMenuItemFileMerge.setAccelerator(KeyStroke.getKeyStroke('M',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileMerge
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayer.myStop();

							try {
								saveCommentList();
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(MainFrame.this, "コメントデータのバックアップ保存時にエラーが発生しました。\n" +
										"処理を中断します。\n" + e1);
								e1.printStackTrace();
								return;
							}

							String targetDir = chooseFile(null, JFileChooser.DIRECTORIES_ONLY, false); // String[0] は空の配列
							if (targetDir.isEmpty()) {
								JOptionPane.showMessageDialog(MainFrame.this,
										"フォルダが選択されなかったので，処理を中止します");
								return;
							}

							try {
								mf = commentList.merge(targetDir, MainFrame.this.commentTypes, MainFrame.this.discussers);
								xf = getUniqueFilename(mf + ".merged.xml");
								commentList.setModified(true);
								JOptionPane.showMessageDialog(MainFrame.this, "マージ後の設定ファイルは，" + xf + " です。");
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(MainFrame.this,
										"マージの過程でエラーが発生しました。処理を中止します。\n" + e1);
								return;
							}

							ctm.refreshFilter();
							addDefaultButton();
							updateButtonPanel(buttonType);
							ctm.fireTableDataChanged();

							if(!soundPlayer.setFile(mf, jMenuItemOptionWaveform.isSelected())){
								JOptionPane.showMessageDialog(MainFrame.this, "再生が開始できません。\n" + mf);
								mf = "";
								xf = "";
								return;
							}
							isSoundPanelEnable = soundPlayer.getSoundBufferEnable();
							commentList.setSetName(xf, commenter);

							setWindowTitle(xf);
							timerStart();
							timeSlider.setMinimum(0);
							timeSlider.setMaximum((int) soundPlayer.getSoundLength());
							timeSlider.setEnabled(true);
							timeEnd.setTime((int) soundPlayer.getSoundLength());
							annotationGlobalViewPanel.updatePanel();

							changeStatePlay();
						}
					});
		}
		return jMenuItemFileMerge;
	}

	
	private JMenuItem getJMenuItemFileExit() {
		if (jMenuItemFileExit == null) {
			jMenuItemFileExit = new JMenuItem("終了");
			jMenuItemFileExit.setAccelerator(KeyStroke.getKeyStroke('Q',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileExit
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							processesBeforeExit();
						}
					});
		}
		return jMenuItemFileExit;
	}

	private JMenu getJMenuControl() {
		if (jMenuControl == null) {
			jMenuControl = new JMenu();
			jMenuControl.setText("コントロール");
			jMenuControl.add(getJMenuItemControlPlayAndPause());
			jMenuControl.add(getJMenuItemControlSkipBackward());
			jMenuControl.add(getJMenuItemControlSkipForward());
			jMenuControl.add(getJMenuItemControlStop());
			jMenuControl.add(getJMenuItemControlRecord());
			jMenuControl.addSeparator();
			jMenuControl.add(getJMenuItemControlJumpToPrevComment());
			jMenuControl.add(getJMenuItemControlJumpToNextComment());
			jMenuControl.add(getJMenuItemControlScroll());
			jMenuControl.addSeparator();
			jMenuControl.add(getJMenuItemControlPlayRateUp());
			jMenuControl.add(getJMenuItemControlPlayRateDown());
			jMenuControl.add(getJMenuItemControlPlayRateReset());
		}
		return jMenuControl;
	}

	private JMenuItem getJMenuItemControlPlayRateUp() {
		if (jMenuItemControlPlayRateUp == null) {
			jMenuItemControlPlayRateUp = new JMenuItem("再生速度＋");
			jMenuItemControlPlayRateUp.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_PERIOD, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlPlayRateUp
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (playRate < 2.0) {
								playRate += 0.2f;
								soundPlayer.setPlayRate(playRate);
							}
						}
					});
		}
		return jMenuItemControlPlayRateUp;
	}

	private JMenuItem getJMenuItemControlPlayRateDown() {
		if (jMenuItemControlPlayRateDown == null) {
			jMenuItemControlPlayRateDown = new JMenuItem("再生速度ー");
			jMenuItemControlPlayRateDown.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlPlayRateDown
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (playRate > 0) {
								playRate -= 0.1f;
								soundPlayer.setPlayRate(playRate);
							}
						}
					});
		}
		return jMenuItemControlPlayRateDown;
	}

	private JMenuItem getJMenuItemControlPlayRateReset() {
		if (jMenuItemControlPlayRateReset == null) {
			jMenuItemControlPlayRateReset = new JMenuItem("再生速度リセット");
			jMenuItemControlPlayRateReset.setAccelerator(KeyStroke
					.getKeyStroke(KeyEvent.VK_SLASH, InputEvent.CTRL_DOWN_MASK,
							false));
			jMenuItemControlPlayRateReset
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							playRate = 1.0f;
							soundPlayer.setPlayRate(playRate);
						}
					});
		}
		return jMenuItemControlPlayRateReset;
	}

	private JMenuItem getJMenuItemControlPlayAndPause() {
		if (jMenuItemControlPlayPause == null) {
			jMenuItemControlPlayPause = new JMenuItem("再生・一時停止（■〓）");
			jMenuItemControlPlayPause.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlPlayPause
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayButton.doClick();
						}
					});
		}
		return jMenuItemControlPlayPause;
	}

	private JMenuItem getJMenuItemControlStop() {
		if (jMenuItemControlStop == null) {
			jMenuItemControlStop = new JMenuItem("停止（■）");
			jMenuItemControlStop.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK, false));
			jMenuItemControlStop
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundStopButton.doClick();
						}
					});
		}
		return jMenuItemControlStop;
	}

	private JMenuItem getJMenuItemControlRecord() {
		if (jMenuItemControlRecord == null) {
			jMenuItemControlRecord = new JMenuItem("録音（●）");
			jMenuItemControlRecord
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundRecordButton.doClick();
						}
					});
		}
		return jMenuItemControlRecord;
	}

	private JMenuItem getJMenuItemControlSkipBackward() {
		if (jMenuItemControlSkipBackward == null) {
			jMenuItemControlSkipBackward = new JMenuItem("スキップ（◀◀）");
			jMenuItemControlSkipBackward.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlSkipBackward
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundBackwardButton.doClick();
						}
					});
		}
		return jMenuItemControlSkipBackward;
	}

	private JMenuItem getJMenuItemControlSkipForward() {
		if (jMenuItemControlSkipForward == null) {
			jMenuItemControlSkipForward = new JMenuItem("スキップ（▶▶）");
			jMenuItemControlSkipForward.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlSkipForward
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundForwardButton.doClick();
						}
					});
		}
		return jMenuItemControlSkipForward;
	}

	private long prevSkipTime = 0;
	private int prevSkipCommentIndex = -1;
	
	private JMenuItem getJMenuItemControlJumpToPrevComment() {
		if (jMenuItemControlJumpToPrevComment == null) {
			jMenuItemControlJumpToPrevComment = new JMenuItem("前のコメント");
			jMenuItemControlJumpToPrevComment.setAccelerator(KeyStroke
					.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK,
							false));
			jMenuItemControlJumpToPrevComment
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int currentCommentIndex = commentTable.getCurrentCommentPosition();
							ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
							long now = new Date().getTime();
							if(now - prevSkipTime < THRESHOLD_CLICK_INTERVAL){
								if(prevSkipCommentIndex - 1 >= 0){
									currentCommentIndex = prevSkipCommentIndex - 1;
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									prevSkipTime = now;	
									return;
								}
							} else {
								if(currentCommentIndex == CommentTable.UNDEFINED){
									currentCommentIndex = commentTable.getNearCommentPosition(soundPlayer.getElapsedTime(), false);
									if(currentCommentIndex == CommentTable.UNDEFINED){
										prevSkipTime = now;	
										return;
									}
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									if(currentCommentIndex - 1 >= 0){
										currentCommentIndex--;
										prevSkipCommentIndex = currentCommentIndex;
									}
								}
							}
							prevSkipTime = now;	
							Comment nextComment;
							nextComment = filteredCommentList.get(currentCommentIndex);
							soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump);
							if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
								// 300msec くらいにしないと数秒戻ってしまう
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment)+300);
							} else {
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump);
							}
						}
					});
		}
		return jMenuItemControlJumpToPrevComment;
	}

	private JMenuItem getJMenuItemControlJumpToNextComment() {
		if (jMenuItemControlJumpToNextComment == null) {
			jMenuItemControlJumpToNextComment = new JMenuItem("次のコメント");
			jMenuItemControlJumpToNextComment.setAccelerator(KeyStroke
					.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK,
							false));
			jMenuItemControlJumpToNextComment
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int currentCommentIndex = commentTable.getCurrentCommentPosition();
							ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
							long now = new Date().getTime();
							if(now - prevSkipTime < THRESHOLD_CLICK_INTERVAL){
								if(prevSkipCommentIndex + 1 < filteredCommentList.size()){
									currentCommentIndex = prevSkipCommentIndex + 1;
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									prevSkipTime = now;	
									return;
								}
							} else {
								if(currentCommentIndex == CommentTable.UNDEFINED){
									currentCommentIndex = commentTable.getNearCommentPosition(soundPlayer.getElapsedTime(), true);
									if(currentCommentIndex == CommentTable.UNDEFINED){
										prevSkipTime = now;	
										return;
									}
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									if(currentCommentIndex + 1 < filteredCommentList.size()){
										currentCommentIndex++;
										prevSkipCommentIndex = currentCommentIndex;
									}
									
								}
							}
							prevSkipTime = now;	
							Comment nextComment;
							nextComment = filteredCommentList.get(currentCommentIndex);
							if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
								// 300msec くらいにしないと数秒戻ってしまう
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment)+300);
							} else {
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump);
							}
						}
					});
		}
		return jMenuItemControlJumpToNextComment;
	}

	private JMenuItem getJMenuItemControlScroll() {
		if (jMenuItemControlScroll == null) {
			jMenuItemControlScroll = new JMenuItem("再生位置の注釈表示");
			jMenuItemControlScroll.setAccelerator(KeyStroke.getKeyStroke('J',
					KeyEvent.CTRL_MASK, false));
			jMenuItemControlScroll
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							System.err.println("jump!!");
							commentTable.setViewCenter(soundPlayer.getElapsedTime());
						}
					});
		}
		return jMenuItemControlScroll;
	}	
	

	private JMenu getJMenuAnnotation() {
		if (jMenuAnnotation == null) {
			jMenuAnnotation = new JMenu();
			jMenuAnnotation.setText("注釈");
			jMenuAnnotation.add(getJMenuItemAnnotationYourName());
			jMenuAnnotation.add(getJMenuItemAnnotationDiscussers());
			jMenuAnnotation.add(getJMenuItemAnnotationAnnotation());
			jMenuAnnotation.addSeparator();
			// jMenuAnnotation.add(getJMenuAnnotationOption());
			jMenuAnnotation.add(getJMenuItemAnnotationOrderDiscusser());
			jMenuAnnotation.add(getJMenuItemAnnotationOrderType());
			if (buttonType == CommentButton.BUTTON_TYPE_DISCUSSER) {
				jMenuItemAnnotationOrderDiscusser.setSelected(true);
			} else {
				jMenuItemAnnotationOrderType.setSelected(true);
			}
			ButtonGroup bg = new ButtonGroup();
			bg.add(jMenuItemAnnotationOrderDiscusser);
			bg.add(jMenuItemAnnotationOrderType);
			jMenuAnnotation.addSeparator();
			jMenuAnnotation.add(getJMenuItemAnnotationMulti());
		}
		return jMenuAnnotation;
	}

	private void updateButtonPanel(int newButtonType) {
		buttonType = newButtonType;
		buttonPanel.removeAll();
		commentButtons.clear();
		boolean isAnnotationMulti = false;
		
		if(jMenuItemAnnotationMulti != null){
			isAnnotationMulti = jMenuItemAnnotationMulti.isSelected();
		}

		int i = 0;
		if (buttonType == CommentButton.BUTTON_TYPE_DISCUSSER) {
			for (User discusser : discussers) {
				if (!discusser.getName().isEmpty()) {
					CommentButton newCommentButton = new CommentButton(ctm,
							soundPlayer, isAnnotationMulti,
							discusser, commentTypes, commenter);
					newCommentButton.setActionKey(i++);
					newCommentButton.setPreferredSize(new Dimension(80, 40));
					commentButtons.add(newCommentButton);
					buttonPanel.add(newCommentButton);
				}
			}
		} else if (buttonType == CommentButton.BUTTON_TYPE_COMMENT) {
			for (CommentType commentType : commentTypes) {
				if (!commentType.getType().isEmpty()) {
					CommentButton newCommentButton = new CommentButton(ctm,
							soundPlayer, isAnnotationMulti,
							commentType, discussers, commenter);
					newCommentButton.setActionKey(i++);
					newCommentButton.setPreferredSize(new Dimension(80, 40));
					commentButtons.add(newCommentButton);
					buttonPanel.add(newCommentButton);
				}
			}

		}
		buttonPanel.updateUI();
		discussersPanel.updateCompoments();
	}

	private JMenuItem getJMenuItemAnnotationOrderDiscusser() {
		if (jMenuItemAnnotationOrderDiscusser == null) {
			jMenuItemAnnotationOrderDiscusser = new JRadioButtonMenuItem(
					"話者優先");
			jMenuItemAnnotationOrderDiscusser
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateButtonPanel(CommentButton.BUTTON_TYPE_DISCUSSER);
						}
					});
		}
		return jMenuItemAnnotationOrderDiscusser;
	}

	private JMenuItem getJMenuItemAnnotationOrderType() {
		if (jMenuItemAnnotationOrderType == null) {
			jMenuItemAnnotationOrderType = new JRadioButtonMenuItem("ラベル優先");
			jMenuItemAnnotationOrderType
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateButtonPanel(CommentButton.BUTTON_TYPE_COMMENT);
						}
					});
		}
		return jMenuItemAnnotationOrderType;
	}

	private JMenuItem getJMenuItemAnnotationMulti() {
		if (jMenuItemAnnotationMulti == null) {
			jMenuItemAnnotationMulti = new JCheckBoxMenuItem("同時注釈");
			jMenuItemAnnotationMulti
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							for (CommentButton commentButton : commentButtons) {
								commentButton
										.setMultiAnnotation(jMenuItemAnnotationMulti
												.isSelected());
							}
						}
					});
		}
		return jMenuItemAnnotationMulti;

	}

	private JMenuItem getJMenuItemAnnotationYourName() {
		if (jMenuItemAnnotationYourName == null) {
			jMenuItemAnnotationYourName = new JMenuItem("注釈者名");
			jMenuItemAnnotationYourName
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String currentCommenterName = commenter.getName() == null ? "(未設定)"
									: commenter.getName();
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, "現在の設定値: "
											+ currentCommenterName, "注釈者名",
									JOptionPane.PLAIN_MESSAGE);
							if(inputValue == null || inputValue.isEmpty()){
								JOptionPane.showMessageDialog(null, "注釈者名が空です。");
							} else if(inputValue.matches(".*[\\s<>&'\"].*")){
								JOptionPane.showMessageDialog(null, inputValue + "\nには，使用できない文字（<>\"\'& および空白）が含まれているため，設定値を反映しませんでした。");
							} else {
								commenter.setName(inputValue);
							}
						}
					});
		}
		return jMenuItemAnnotationYourName;
	}

	private JMenuItem getJMenuItemAnnotationDiscussers() {
		if (jMenuItemAnnotationDiscussers == null) {
			jMenuItemAnnotationDiscussers = new JMenuItem("話者");
			jMenuItemAnnotationDiscussers
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							DiscusserSettingPanel discusserSettingPanel = new DiscusserSettingPanel(
									discussers, MAX_DISCUSSERS);
							int selectedValue = JOptionPane.showConfirmDialog(
									null, discusserSettingPanel, "話者の設定",
									JOptionPane.OK_CANCEL_OPTION);
							if (selectedValue == JOptionPane.CANCEL_OPTION) {
								return;
							}
							String invalidItems = discusserSettingPanel.updateNewValue();
							if(!invalidItems.isEmpty()){
								JOptionPane.showMessageDialog(null, invalidItems + "\nには，使用できない文字（<>\"\'&）が含まれているため，設定値を反映しませんでした。");
							}
							discussersPanel.updateCompoments();
							if (buttonType == CommentButton.BUTTON_TYPE_DISCUSSER) {
								updateButtonPanel(buttonType);
							}
						}
					});
		}
		return jMenuItemAnnotationDiscussers;
	}

	private JMenuItem getJMenuItemAnnotationAnnotation() {
		if (jMenuItemAnnotationAnnotation == null) {
			jMenuItemAnnotationAnnotation = new JMenuItem("ラベル");
			jMenuItemAnnotationAnnotation
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {

							AnnotationSettingPanel annotationSettingPanel = new AnnotationSettingPanel(
									commentTypes);
							int selectedValue = JOptionPane.showConfirmDialog(
									null, annotationSettingPanel, "ラベルの設定",
									JOptionPane.OK_CANCEL_OPTION);
							if (selectedValue == JOptionPane.CANCEL_OPTION) {
								return;
							}
							String invalidItems = annotationSettingPanel.updateNewValue();
							if(!invalidItems.isEmpty()){
								JOptionPane.showMessageDialog(null, invalidItems + "\nには，使用できない文字（<>\"\'&）が含まれているため，設定値を反映しませんでした。");
							}
							discussersPanel.updateCompoments();
							if (buttonType == CommentButton.BUTTON_TYPE_COMMENT) {
								updateButtonPanel(buttonType);
							}
						}
					});
		}
		return jMenuItemAnnotationAnnotation;

	}

	private JMenu getJMenuOption() {
		if (jMenuOption == null) {
			jMenuOption = new JMenu();
			jMenuOption.setText("オプション");
			jMenuOption.add(getJMenuItemOptionTextOverlay());
			jMenuOption.add(getJMenuItemOptionVideoRatio());
			jMenuOption.add(getJMenuItemOptionSkipTime());
			jMenuOption.add(getJMenuItemOptionJumpAdjustment());
//			jMenuOption.add(getJMenuItemAnnotationTimeCorrection());
			jMenuOption.add(getJMenuItemOptionFocusRange());
			jMenuOption.addSeparator();
			jMenuOption.add(getJMenuItemOptionRecorderMode());
			jMenuOption.add(getJMenuItemOptionViewSyncMode());
			jMenuOption.add(getJMenuItemOptionWaveform());
		}
		return jMenuOption;
	}

	
	private JMenuItem getJMenuItemOptionTextOverlay() {
		String[] textOverlayStyles = soundPlayer.getAvailableTextOverlayStyles();

		if (jMenuItemOptionTextOverlay == null) {
			jMenuItemOptionTextOverlay = new JMenu("テキスト表示");
			ButtonGroup itemGroup = new ButtonGroup();
			for (int i = 0; i < textOverlayStyles.length; i++) {
				final int ii = i;
				JMenuItem item = new JRadioButtonMenuItem(textOverlayStyles[i]);
				item.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						soundPlayer.setTextOverlayStyle(ii);
					}
				});
				jMenuItemOptionTextOverlay.add(item);
				itemGroup.add(item);
				if (i == iTextOverlayStyle) {
					item.setSelected(true);
				}
			}
		}
		return jMenuItemOptionTextOverlay;
	}
	
	
	private JMenuItem getJMenuItemOptionVideoRatio() {
		String[] videoAspectRatios = soundPlayer.getAvailableVideoAspectRatio();

		if (jMenuItemOptionVideoRatio == null) {
			jMenuItemOptionVideoRatio = new JMenu("画面アスペクト比");
			ButtonGroup itemGroup = new ButtonGroup();
			for (int i = 0; i < videoAspectRatios.length; i++) {
				String strVideoAspectRatio = videoAspectRatios[i];
				String[] strRatio = strVideoAspectRatio.split(":");
				final float videoAspectRatio = Float.parseFloat(strRatio[0])
						/ Float.parseFloat(strRatio[1]);
				final int ii = i;
				JMenuItem item = new JRadioButtonMenuItem(strVideoAspectRatio);
				item.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(timer != null){
							timer.cancel();
						}
						soundPlayer.setVideoAspectRatio(videoAspectRatio);
						timerStart();
						int playerState = soundPlayer.getPlayerState();
						if(playerState == SoundPlayer.PLAYER_STATE_PAUSE){
							changeStatePause();
						} else if(playerState == SoundPlayer.PLAYER_STATE_PLAY){
							changeStatePlay();
						}
						iVideoAspectRate = ii;
					}
				});
				strVideoAspectRatio.replaceAll("f", "");
				strVideoAspectRatio.replaceAll("/", ":");
				jMenuItemOptionVideoRatio.add(item);
				itemGroup.add(item);
				if (i == iVideoAspectRate) {
					item.setSelected(true);
				}
			}
		}
		return jMenuItemOptionVideoRatio;
	}

	private JMenuItem getJMenuItemOptionSkipTime() {
		if (jMenuItemOptionSkipTime == null) {
			jMenuItemOptionSkipTime = new JMenuItem("スキップ時間");
			jMenuItemOptionSkipTime
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, "現在の設定値: " + skipTime
											+ " (msec)", "スキップ時間",
									JOptionPane.PLAIN_MESSAGE);
							if (inputValue != null) {
								skipTime = Integer.parseInt(inputValue);
							}
						}
					});
		}
		return jMenuItemOptionSkipTime;
	}

	private JMenuItem getJMenuItemOptionJumpAdjustment() {
		if (jMenuItemOptionJumpAdjustment == null) {
			jMenuItemOptionJumpAdjustment = new JMenuItem("再生位置の補正");
			jMenuItemOptionJumpAdjustment
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, "現在の設定値: "
											+ adjustmentTimeAtJump + " (msec)",
									"再生位置の補正", JOptionPane.PLAIN_MESSAGE);
							if (inputValue != null) {
								adjustmentTimeAtJump = Integer
										.parseInt(inputValue);
							}
						}
					});
		}
		return jMenuItemOptionJumpAdjustment;
	}

	
	private JMenuItem getJMenuItemOptionFocusRange() {
		if (jMenuItemOptionFocusRange == null) {
			jMenuItemOptionFocusRange = new JMenuItem("強調表示範囲");
			jMenuItemOptionFocusRange
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, "現在の設定値: 前後 " + focusRange / 1000
											+ " (sec)", "強調表示範囲",
									JOptionPane.PLAIN_MESSAGE);
							if (inputValue != null) {
								focusRange = Integer.parseInt(inputValue) * 1000;
							}
							annotationGlobalViewPanel.setFocusRange(focusRange);
						}
					});
		}
		return jMenuItemOptionFocusRange;
	}

	
	
	private JMenuItem getJMenuItemOptionRecorderMode() {
		if (jMenuItemOptionRecorderMode == null) {
			jMenuItemOptionRecorderMode = new JCheckBoxMenuItem("録音モード");
			jMenuItemOptionRecorderMode.setAccelerator(KeyStroke.getKeyStroke('R',
					KeyEvent.CTRL_MASK, false));
			jMenuItemOptionRecorderMode
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(jMenuItemOptionRecorderMode.isSelected()){
								soundRecordButton.setForeground(Color.red);
							} else {
								soundRecordButton.setForeground(Color.black);
							}
						}
					});
		}
		return jMenuItemOptionRecorderMode;
	}


	private JMenuItem getJMenuItemOptionViewSyncMode() {
		if (jMenuItemOptionViewSyncMode == null) {
			jMenuItemOptionViewSyncMode = new JCheckBoxMenuItem("注釈連動再生");
			jMenuItemOptionViewSyncMode.setAccelerator(KeyStroke.getKeyStroke('Y',
					KeyEvent.CTRL_MASK, false));
			jMenuItemOptionViewSyncMode
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// 何もしないが，チェックボックスの状態が変わる。
						}
					});
		}
		return jMenuItemOptionViewSyncMode;
	}


	private JMenuItem getJMenuItemOptionWaveform() {
		if (jMenuItemOptionWaveform == null) {
			jMenuItemOptionWaveform = new JCheckBoxMenuItem("波形表示");
			jMenuItemOptionWaveform
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog(null,  "このオプションは次回ファイル読み込み時から有効になります。");
						}
					});
		}
		return jMenuItemOptionWaveform;
	}

	
	
	// 仕様が確定してから使用する
	private JMenuItem getJMenuItemAnnotationTimeCorrection() {
		if (jMenuItemAnnotationTimeCorrection == null) {
			jMenuItemAnnotationTimeCorrection = new JMenuItem("注釈ファイル同期補正");
			jMenuItemAnnotationTimeCorrection
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(commentList.size() == 0){
								JOptionPane.showMessageDialog(null, "データが読み込まれていません");
								return;
							}
							TimeCorrectionSettingPanel timeOffsetSettingPanel = new TimeCorrectionSettingPanel(commentList);
							int selectedValue = JOptionPane.showConfirmDialog(
									null, timeOffsetSettingPanel, "同期補正",
									JOptionPane.OK_CANCEL_OPTION);
							 if(selectedValue == JOptionPane.CANCEL_OPTION){
								 return;
							 } else {
								 timeOffsetSettingPanel.applyData();
								 commentTable.revalidate();
								 commentTable.repaint();
								 discussersPanel.updateCompoments();
//								 discussersPanel.repaint();
//								 commentTable.ctm.fireTableDataChanged();
							 }
						}
					});
		}
		return jMenuItemAnnotationTimeCorrection;
	}

	private JMenu getJMenuHelp() {
		if (jMenuHelp == null) {
			jMenuHelp = new JMenu();
			jMenuHelp.setText("ヘルプ");
			jMenuHelp.add(getJMenuItemHelpVersion());
		}
		return jMenuHelp;
	}

	private JMenuItem getJMenuItemHelpVersion() {
		if (jMenuItemHelpVersion == null) {
			jMenuItemHelpVersion = new JMenuItem(systemName + "について");
			jMenuItemHelpVersion
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog(MainFrame.this,
									systemName + " " + VERSION + "\n"
											+ System.getProperty("java.vm.name")
											 + ", Ver." + System.getProperty("java.version")  + "\n"
												+ COPYRIGHT,
											systemName + "について",
									JOptionPane.INFORMATION_MESSAGE);
						}
					});
		}
		return jMenuItemHelpVersion;
	}

	public void setWindowTitle(String filename) {
		if (filename == null || filename.isEmpty()) {
			filename = "未指定";
		}
		setTitle("[" + filename + "] - " + systemName);
	}

	class DrawGraphTask extends TimerTask {
		int time;

		public void run() {
			if(timeLineTabbedPane.getSelectedIndex() == TAB_STATUS_DETAIL_VIEW){
				if(isSoundPanelEnable){
					soundPanel.repaint();
				}
				discussersPanel.repaintComponents();
			} else {
				annotationGlobalViewPanel.repaint();
			}
			soundPlayer.updateVlcInfo();
			if (jMenuItemOptionRecorderMode.isSelected()) {
				time = soundPlayer.getElapsedTime();
			} else {
				time = soundPlayer.getCurrentRecordingTime();
			}
			soundPlayer.setOverlayText(commentTable.getCurrentComment());
			timeCurrent.setTime(time / 1000);
			timeSlider.setValue(time / 1000);
			commentTable.indicateCurrentComment(time, focusRange);
			if(jMenuItemOptionViewSyncMode.isSelected()){
				commentTable.setViewCenter(time);
			}
		}
	}
}
