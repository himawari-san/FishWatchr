ディスカッション観察支援ツール『FishWatchr』 ver.0.9.1 (2015-09-11)
Copyright 2014-2015 山口昌也(大学共同利用機関法人 人間文化研究機構 国立国語研究所)

１．本パッケージの内容
  - vlc/            ... VLC ライブラリ，および，プラグイン
                        (ver.2.2.1, Windows 32bit版)
  - jre/            ... JRE ver.1.8.0_60 (Windows 32bit版)
  - FishWatchr.exe  ... FishWatchr プログラム(Windows 専用)
  - fishwatchr.jar  ... FishWatchr プログラム(Mac, Linux, Windows 共用)
  - README.txt      ... このファイル
  - COPYING.txt     ... GPL 3.0 ライセンス文書

２．利用方法
  - 利用方法は，『FishWatchr』のホームページをご覧ください。
    (http://www2.ninjal.ac.jp/lrc の
     ディスカッション観察支援ツール『FishWatchr』)

  - FishWatchr プログラムは，GPL 3.0 ライセンスにて配布します。詳細は，
    COPYING を参照してください。また，ソースファイルは，
    https://github.com/himawari-san/FishWatchr にて配布しています。


３．その他
  - 最新情報は，大学共同利用機関法人 人間文化研究機構 国立国語研究所
    の Web ページ（http://www2.ninjal.ac.jp/lrc/）で公開しています。
  - 不具合のご報告，ご意見などについては，himawari@ninjal.ac.jp までお
    願いいたします。お返事の約束はいたしかねますが，今後の開発に活用
    させていただきます。

　- 本パッケージの jre フォルダには，Windows 版 JRE が含まれています。
    これらのフォルダ中のファイルの著作権は，Oracle が保持しています。
    詳しくは，jre フォルダ 中の LICENSE, README.txt などをご覧ください。

　- 本パッケージの vlc フォルダには，VLC media player のライブラリ，お
    よび，プラグインが含まれています。これらのフォルダ中のファイルの著
    作権は，VLC authors and VideoLAN が保持しています。詳しくは，vlc
    フォルダ 中の COPYING.txt などをご覧ください。

　- 本パッケージの作成にあたっては，次のソフトウェア，および，助成金
    の支援を受けました。関係者の方々に深く感謝いたします。

    -- VLC media player (http://www.videolan.org/)
    -- vlcj (https://github.com/caprica/vlcj)
    -- JSPS科研費（26560135，「即時性と教育効果を考慮した協調学習過程の
       構造化手法に関する研究」）


４．履歴
  2015-09-11: 『FishWatchr』ver.0.9.1 公開
    - ネットワーク上のメディアデータ（YouTube なども含む）に対して注記
      付けできるように機能を拡張
    - キーボードからアノテーションボタンを押せるように機能を拡張
    - パッケージに jre を同梱。Mac OS X 用パッケージを独立
    - 音声のパワー表示を WAV ファイルの録音・再生時だけ表示するよう変更
    - 注記付け結果の再生時に時間設定のスライダが動作しない不具合を修正
    - Ctrl-p で再生を一時停止した直後，停止ボタンを押すと，再生が開始さ
      れてしまう不具合を修正
    - ウィンドウのサイズを変更した時に動作が不安定になる不具合を修正

  2015-06-08: 『FishWatchr』ver.0.9 公開
    - ディスカッション全体から注記づけ結果を閲覧する機能を追加
    - 従来の注記づけ結果とパワー表示は，詳細表示として，ディスカッショ
      ンの全体表示とタブで切り替えられるよう変更
    - Windows 環境において，メディアファイルのファイル名やパスに全角文
      字を含むと再生できない不具合を修正

  2015-05-11: 『FishWatchr』ver.0.8.1 公開
    - Windows 環境で VLC ライブラリを自動検出できない不具合を修正

  2015-05-01: 『FishWatchr』ver.0.8 公開
