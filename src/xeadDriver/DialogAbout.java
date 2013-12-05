package xeadDriver;

/*
 * Copyright (c) 2012 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;

public class DialogAbout extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * Application Information
	 */
	public static final String PRODUCT_NAME = "XEAD[zi:d] Driver";
	public static final String FULL_VERSION  = "V1.R1.M27";
	//27
	//・スクリプトで進捗バーを制御するための関数session.startProgress(...)を追加した
	//・メニュー属性「プリロード指定機能」に対応した
	//・XF310のキー入力ダイアログのクローズ操作のバグを修正
	//・テーブルスクリプトの「一時保留」に対応した
	//・画像ファイルフィールドについてファイルが存在しない場合の表現を改善した
	//･画像ファイルフィールドを更新対象の明細フィールドとして置いた場合の表現を改善した
	//・各種追加処理において、自動採番フィールドが結合フィールドとして含まれていると採番レコードが無意味にカウントアップされるバグを修正
	//･機能内で同一のselect文が実行された場合にはテーブル操作ログに記録しないようにした（ログのサイズを減らすため）
	//･XF110,310の明細行上にカーソルがあるときに更新ボタンが無効化されるようにした（そのまま更新すると最下行の値がメモリー上に残っていることがあるため）
	//･VARCHAR向けのテキストド入力域にフォーカスがある場合、EnterキーをTabキーにあてがっていたが、エラーチェックのキーに変更した
	//
	//26
	//・InputDialogにダイアログの幅を指定するためのメソッドsetWidth(...)を組み込んだ
	//・InputDialogのフィールドにsetValue(...)された場合に、値をフィールドサイズに合わせるようにした
	//・画像をフィールドの指定サイズに合わせて縮小／拡大するようにするとともに、画像をクリックすればオリジナルサイズで表示されるようにした
	//・XF390の明細項目が数値の場合に異常終了することがある問題を修正した
	//・クロスチェックの対象テーブルを静的結合されたものに限るようにした
	//・クロスチェックの除外指定に対応した
	//・結合キーが数字フィールドの場合にクロスチェックが正常に作動しない問題を修正
	//・XF110の全選択用チェックボックスをチェックすると右隣の項目での並び替えが起こっていたバグを修正
	//
	//25
	//・日付フィールドが利用するカレンダーコンポーネントをセッション共有オブジェクトにした
	//・カレンダーコンポーネントの表示位置の制御ロジックを改善した
	//・カレンダーコンポーネントがカレンダー区分を受け取れるようにした（日付フィールドの修正については未完）
	//
	//24
	//・ReferCheckerでFKに含まれる数字フィールドがNULLであった場合に対応
	//・カレンダーコンポーネントで、休日テーブルとユーザ定義区分の「カレンダー区分」を処理するようにした
	//・日付処理系の３つのセッション関数で、休日テーブルの「カレンダー区分」を処理するようにした
	//
	//23
	//・マイナス不可の数値フィールドでマイナス入力が可能になることがあるバグを修正
	//・XF100,110,300の一覧順にエイリアス指定されたフィールドを含めると異状終了するバグを修正
	//・XF310でカラムサイズを変更しても、編集選択行のカラムサイズが変化しないバグを修正
	//・クロスチェッカーのログイン時ロードの仕様を組み込んだ
	//・機能の終了時にクロスチェッカーの生成スレッドをキャンセルするようにした
	//・XF100,110,300の検索条件のプロンプトオプション「候補リスト」に対応した
	//・処理テーブルの「固定Where」にセッション属性の他にユーザ属性も指定できるようにした
	//・XF390で、指定によって合計値や合計行の設定がおかしくなるバグを修正
	//・XF000で、実行時刻にLIST式やREPEAT式を指定できるようにした
	//
	//22
	//・メニュータブの選択時に下部に表示されるメッセージを、操作援助用URLが指定されているかどうかで切り替えるようにした
	//・XFTextFieldのタイプがDATETIMEの場合のフィールド幅を広げた
	//・XF310でのブランク行追加のロジックを改善した
	//・XF300から明細行をXF200でコピーした後で一覧をリフレッシュするようにした
	//・CheckListDialogで戻るボタンを使うとリストがクリアされないバグを修正
	//
	//21
	//・InputDialogのaddFieldメソッドにおいて表示域のデフォルトを0（上部配置）とした
	//・「固定Where」にセッション属性を組み込めるようにした
	//・Session関数setNextNumber(id, nextNumber)を組み込んだ
	//・Session関数isValidTime(time, format)を組み込んだ
	//・Session関数getMinutesBetweenTimes(timeFrom, timeThru)を組み込んだ
	//・Session関数getOffsetDateTime(date, time, minutes, countType)を組み込んだ
	//・Session関数getOffsetYearMonth(yearMonth, months)を組み込んだ
	//・Session関数setSystemVariant(id, value)を組み込んだ
	//・URLタイプのフィールドにローカルファイル名を指定できるようにした
	//・XF200が"EDIT"のINSTANCE_MODEを渡せば編集モードで起動されるようにした
	//・XF200,310で、プロンプタ関数から受け取るフィールドが含まれない場合に非表示フィールドとして組み込むようにした
	//・XF310の行追加リストに明細項目が指定されていない場合にはメッセージ出力して終了するようにした
	//・XF310の行追加リストでWhere条件が指定されていないケースに対応した
	//・XF310で追加された直後にはエラー表示しないようにした
	//・XF100,110,300について、初期表示オプションに対応した
	//・XF100から機能を起動した後の終了コードの値にしたがって明細一覧を更新するようにした
	//・XF100,110,300,310について、セルの配色設定を改善した
	//・TableOperatorでシングルクォーテーションを含むデータを扱えるようにした
	//・TableOperatorで日付項目=''のaddKeyValueがされた場合に、内部で'is NULL'に置き換えるようにした(!=''の場合には'is not NULL') 
	//・TableOperatorで日付項目''のaddValueがされた場合に、内部で'NULL'に置き換えるようにした
	//・検索条件フィールドに複数のOR条件を設定するために、session.getCheckListDialog()を追加した
	//・Sessionの自動採番処理のロジックを改善した
	//・XF110,200,310で、リストボックスかプロンプタが設定されているフィールドについて、これらによって値設定されるフィールドのうちの一部が編集不可であれば無効にしていたが、これをやめた。編集不可であっても値を更新したいケースがあるため。
	//＜修正された問題＞
	//・Sessionの税額計算関数の検索ロジックのバグを修正した
	//・XF100,110,300で、カラム別のソートを指定した場合、EXCEL出力をすると見出しに<u>が付加されてしまうバグを修正した
	//・数字フィールドに対して自動採番を設定すると*AUTOが表示されないバグを修正した
	//・VARCHAR項目を１行表示にした場合、スクロールバーが常時表示されていたバグを修正した
	//・XF100,110,200,300,310について、数字フィールドにプロンプト設定した際の動きに関するバグを修正した
	//・XF110,310について、明細行毎の編集可／不可設定が正しく反映されていなかったバグを修正した
	//・XF290,390で例外メッセージをダイアログ表示するステップの細かいバグを修正した
	//・XF310で「値リストフィールド」に関する扱いが抜け落ちていたバグを修正した
	//・XF310を連続実行した場合、DividerLocationの位置が再設定されないことのあるバグを修正した
	//・XF310で見出し域に入力可能項目が存在しない場合、明細行のエラー項目にフォーカスが当たらないバグを修正
	//・XF310で新規追加された明細行についてのユニーク制約チェックに関するバグを修正した
	//・XF110で一次テーブルの「結合テーブル読み込み前・更新前スクリプト」の実行ステップが抜けていたバグを修正
	//・XF110でバッチテーブル処理機能がブランクでも実行されてしまうバグを修正
	//・XF110で検索条件をゼロ個にすると表示がおかしくなるバグを修正
	//・XF110で見出し域のリストボックスについて、選択初期値が関連するバッチフィールドに反映されないバグを修正
	//・XF100,110で年月型フィールドで検索条件を指定すると異常終了するバグを修正した
	public static final String FORMAT_VERSION  = "1.1";
	public static final String COPYRIGHT = "Copyright 2013 DBC,Ltd.";
	public static final String URL_DBC = "http://homepage2.nifty.com/dbc/";
	/**
	 * Components on dialog
	 */
	private JPanel panel1 = new JPanel();
	private JPanel panel2 = new JPanel();
	private JPanel insetsPanel1 = new JPanel();
	private JPanel insetsPanel2 = new JPanel();
	private JPanel insetsPanel3 = new JPanel();
	private JButton buttonOK = new JButton();
	private JLabel imageLabel = new JLabel();
	private JLabel labelName = new JLabel();
	private JLabel labelVersion = new JLabel();
	private JLabel labelCopyright = new JLabel();
	private JLabel labelURL = new JLabel();
	private ImageIcon imageXead = new ImageIcon();
	private HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
	private Desktop desktop = Desktop.getDesktop();
	private JDialog parent_;

	public DialogAbout(JDialog parent) {
		super(parent);
		parent_ = parent;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception  {
	 	imageXead = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.Session.class.getResource("title.png")));
		imageLabel.setIcon(imageXead);
		panel1.setLayout(new BorderLayout());
		panel1.setBorder(BorderFactory.createEtchedBorder());
		panel2.setLayout(new BorderLayout());
		insetsPanel2.setLayout(new BorderLayout());
		insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		insetsPanel2.setPreferredSize(new Dimension(75, 52));
		insetsPanel2.add(imageLabel, BorderLayout.EAST);
		//
		labelName.setFont(new java.awt.Font("Serif", 1, 16));
		labelName.setHorizontalAlignment(SwingConstants.CENTER);
		labelName.setText(PRODUCT_NAME);
		labelName.setBounds(new Rectangle(-5, 9, 190, 18));
		labelVersion.setFont(new java.awt.Font("Dialog", 0, 12));
		labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
		labelVersion.setText(FULL_VERSION);
		labelVersion.setBounds(new Rectangle(-5, 32, 190, 15));
		labelCopyright.setFont(new java.awt.Font("Dialog", 0, 12));
		labelCopyright.setHorizontalAlignment(SwingConstants.CENTER);
		labelCopyright.setText(COPYRIGHT);
		labelCopyright.setBounds(new Rectangle(-5, 53, 190, 15));
		labelURL.setFont(new java.awt.Font("Dialog", 0, 12));
		labelURL.setHorizontalAlignment(SwingConstants.CENTER);
		labelURL.setText("<html><u><font color='blue'>" + URL_DBC);
		labelURL.setBounds(new Rectangle(-5, 73, 190, 15));
		labelURL.addMouseListener(new About_labelURL_mouseAdapter(this));
		insetsPanel3.setLayout(null);
		insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
		insetsPanel3.setPreferredSize(new Dimension(190, 80));
		insetsPanel3.add(labelName, null);
		insetsPanel3.add(labelVersion, null);
		insetsPanel3.add(labelCopyright, null);
		insetsPanel3.add(labelURL, null);
		//
		buttonOK.setText("OK");
		buttonOK.addActionListener(this);
		insetsPanel1.add(buttonOK, null);
		//
		panel1.add(insetsPanel1, BorderLayout.SOUTH);
		panel1.add(panel2, BorderLayout.NORTH);
		panel2.setPreferredSize(new Dimension(270, 90));
		panel2.add(insetsPanel2, BorderLayout.CENTER);
		panel2.add(insetsPanel3, BorderLayout.EAST);
		//
		this.setTitle("About XEAD Driver");
		this.getContentPane().add(panel1, null);
		this.setResizable(false);
	}

	public void request() {
		insetsPanel1.getRootPane().setDefaultButton(buttonOK);
		Dimension dlgSize = this.getPreferredSize();
		Dimension frmSize = parent_.getSize();
		Point loc = parent_.getLocation();
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x + 30, (frmSize.height - dlgSize.height) / 2 + loc.y + 30);
		this.pack();
		super.setVisible(true);
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	void cancel() {
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonOK) {
			cancel();
		}
	}

	void labelURL_mouseClicked(MouseEvent e) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			desktop.browse(new URI(URL_DBC));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "The Site is inaccessible.");
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	void labelURL_mouseEntered(MouseEvent e) {
		setCursor(htmlEditorKit.getLinkCursor());
	}

	void labelURL_mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
}

class About_labelURL_mouseAdapter extends java.awt.event.MouseAdapter {
	DialogAbout adaptee;
	About_labelURL_mouseAdapter(DialogAbout adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.labelURL_mouseClicked(e);
	}
	public void mouseEntered(MouseEvent e) {
		adaptee.labelURL_mouseEntered(e);
	}
	public void mouseExited(MouseEvent e) {
		adaptee.labelURL_mouseExited(e);
	}
}
