package xeadDriver;

/*
 * Copyright (c) 2016 WATANABE kozo <qyf05466@nifty.com>,
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
	public static final String PRODUCT_NAME = "X-TEA Driver";
	public static final String FULL_VERSION  = "V1.R3.M2";
	public static final String VERSION  = "1.3.2";
	public static final String FORMAT_VERSION  = "1.2";
	public static final String COPYRIGHT = "Copyright 2016 DBC Ltd.";
	public static final String URL_DBC = "http://homepage2.nifty.com/dbc/";
	//1.3.2
	//・XFTextField,XFDateField等について、アルファベットのベースライン以下が見えにくい問題を改善した
	//・TableOperatorのログ書き込みで、SQL文の最長を1000桁にした
	//・実行中の機能上でEscキーが押された場合、全機能を閉じてメニューに戻るようにした
	//・セッション明細を記録する際に機能名が長すぎると異常終了する問題を修正した
	//・TableOperatorとTableEvaluatorでのaddKeyValueでの空白値の処理ロジックを改善した
	//・XF110のバッチ項目がバッチテーブルのPKである場合に入力不可になる問題を修正した
	//・XF110の明細項目がバッチテーブルへの外部キーである場合に入力不可になるようにした
	//・更新機能にSTRING値の桁数検査を組み込んだ
	//・フィールド情報の検索関数をセッション関数として組み込んだ
	//・TableEvaluatorの更新機能にチェックオンリーパラメータを組み込んだ
	//・TableEvaluatorの更新機能にSTRING値の桁数検査を組み込んだ
	//
	//1.3.1
	//・BYTEAフィールドの扱いに関して全体的に改善した
	//・論理削除の自動化機構を廃止した
	//・XF100で、検索結果が１件だけの場合に明細処理を自動起動するためのオプションを設けた
	//
	//1.3.0
	//・テーブル定義の範囲KEY定義を扱うステップを除去した
	//・メニュー定義のクロスチェッカーのロード定義を扱うステップを除去した
	//・削除確認ダイアログの表示タイミングを改善した
	//・更新系機能タイプでのエラーメッセージの扱いを改善した
	//・TableEvaluatorクラスを組み込んだ
	//・「ハッシュ方式」に追随する関数を組み込んだ
	//・Admin Emailの項目に対応した
	//
	//1.2.22
	//・XF100の初期メッセージの扱いを改善した
	//・XF300のレスポンス向上のために、明細タブの選択時に明細テーブルを読み込むようにした
	//・スプラッシュのメッセージや進度表示を改善した
	//・XFHttpRequestの応答オブジェクトをテキストデータとするとともに、encoding制御を組み込んだ
	//・XFInputDialogのテキストエリアの文字列をコピー可能にした
	//
	//1.2.21
	//・Session#parseStringToXmlDocument(...)のバグを修正した
	//・Session#getOffsetDate(...)の過去方向の計算のバグを修正した
	//
	//1.2.20
	//・getFYearOfDate(...)とgetMSeqOfDate(...)が、関連するシステム制御データが存在しなくても正常動作するようにした
	//・XFTableOperatorのサーバ処理で接続異状が起きたときの動きを改善した
	//・XF110の確認リストで表示されるメッセージの不具合を修正した
	//
	//1.2.19
	//・XF110,XF310について、明細項目でvaluesListを設定した場合のリストボックス表示の問題を修正
	//・XFInputAssist項目について動作を改善
	//・XF200におけるSKの重複エラーのメッセージを改善
	//
	//1.2.18
	//・前方一致であるようなフィールドIDをテーブルに含めると、XF300,310,390でエラーが起こり得る問題を修正
	//・XF110,200,310について、見出し域の非表示フィールドのNULLエラーメッセージにフィールド名を含めるように改善

	//・XF300について、キーなしで起動された場合のキー入力ダイアログで右上のX印を押した場合の動きに関するバグを修正
	//・ReferChecker構築スレッドの中でスクリプトの記述エラーを示すとブランク表示される特性に対応した
	//
	//1.2.17
	//・sessionにDB接続定義を得るためのプロパティを加えた
	//・画像フィールドの高さ設定が指定行数に正確に従っていなかった問題を修正した
	//・画像フィールドの再表示ボタンの幅を狭めてアイコンを設定した
	//・PostgreSQL向けのJDBCドライバをアップグレードした
	//
	//1.2.16
	//・sessionのメソッドにgetFileName()を加えた
	//・sessionのメソッドにgetSystemProperty(id)を加えた
	//・一覧形式でのキャプション初期値をフィールド定義の「カラム名」にした
	//・起動時のスプラッシュの形式を改善した
	//・XF100,XF110で値リスト系のフィルター条件が効かなくなっていた問題を修正した
	//・XF110でのバッチテーブルのキーマップが２回目の起動時にクリアされない問題を修正した
	//・TableOperatorで数値フィールドにScript変数の数値をaddするとエラーになる問題を修正した
	//
	//1.2.15
	//・XF110_SubListでのカラムサイズの変更にTableCellsEditorが追随しない問題を修正した
	//・ツール名をX-TEA　Driverに変更した
	//・H2 Database EngineとMS Accessに対応した
	//・Float型小数桁の最大値を９に設定した
	//
	//1.2.14
	//・Java1.8に対応するために、sort処理をcomparatorからcomparableベースに修正した
	//
	//1.2.13
	//・XF110,XF310の明細上のリストボックスの内容を表示するとフリーズすることがある問題を修正した
	//・プロンプタ関数としてXF100,XF300を起動して、行を選ばずに終了したときに不要なメッセージが表示される問題を修正した
	//
	//1.2.12
	//・XF110のバッチプログラム実行確認用のチェックボックスの配置に関するダイアログサイズ設定のロジックを改善した
	//・XF110の選択リストでスペースキーでも選択できるようにした
	//・XF310で編集可能項目が存在しない場合、起動直後にループするバグを修正した
	//・更新系機能のプロンプト関数の実行時に不明な交換フィールドがあった場合、警告メッセージを出すようにした
	//・XF200,XF300,XF310が、DISABLED_BUTTON_LISTのパラメータを受け取れるようにした
	//・「更新不可」のフィールドの値を更新系パネル機能で更新できないようにした
	//・XF000のコンソールのレイアウトや終了メッセージの処理ロジックを改善した
	//・XF100,XF110のフィルター条件の区分選択用関数とのやり取りに関するバグを修正した
	//・XF200のプロンプトリストフィールドにおいて、追加時に初期化されない場合があるバグを修正した
	//・sessionのinputDialogでZEROFILLタイプの項目を使えるようにした
	//・XF200のBOOLEANタイプのフィールドの初期化ルーチンのバグを修正
	//・利用していた郵便番号検索WEBサービスの終了にともなって別のサービスに差し替えた
	//
	//1.2.11
	//・XF200でレコードが削除されたときの確認メッセージに削除ボタンのキャプションを組み込むようにした
	//・XF200,XF110,XF310の見出し域でのリストコンポーネントの動作に関するバグを修正した
	//・XF100,XF110,XF300,XF310,XF390について、明細テーブルの「読込前スクリプト」を読取開始前に１度だけ実行するようにした
	//・フィールドの「コメント」について表示様式を微調整した
	//・XF390について、複数の明細テーブルを扱えるようにした
	//
	//1.2.10
	//・sessionにformatTime(...)の関数を追加するとともに、日時計算関数を改善した
	//・CheckListDialogの使い勝手を改善した
	//・XF300の見出しレコードが更新されたときは、ツリービューを更新するようにした
	//・XF200,XF110の見出し域でのリストコンポーネントの動作に関するバグを修正した
	//・XF310の起動時の明細行追加処理に関するバグを修正した
	//
	//1.2.9
	//・XF310の行追加ダイアログでのボタン記述のフォントサイズをボタンサイズに応じて可変にした
	//・整数フィールド向けに「時刻(HH:MM)」の編集タイプを設けた
	//・XF310とXF110の明細行上、およびXF200,XF110の見出し域でのリストコンポーネントの動作に関するバグを修正した
	//・XF100,XF110,XF300の検索条件が日付の場合、レコード上の対応項目がnullのときにエラーになるバグを修正した
	//
	//1.2.8
	//・編集モードでのvarchar表示コンポーネントの動きを改善した
	//・XF110,310の明細行上でのValueList設定に関するバグを修正した
	//・ValueListの対応にともなうテキストフィールドでのエラー色とフォーカス設定のバグを修正した
	//・追加モードにおいてフラグフィールドにfalse値を初期設定するようにした
	//
	//1.2.7
	//・ツリービューでの見出しレコード切替に対応するために、XF300のタブ無効化ルーチンの位置を変更した
	//・XF300の構成ツリー用のタイトルに対応した
	//・XF310の開始時に明細行がゼロ件であれば、追加行操作を暗黙的に起動するようにした
	//・漢字入力フィールドの変換モードを示す下線が見えなくなっていた問題を修正した
	//・セッションのテーブル操作オブジェクトを使うと、セッション終了時にコミットされないトランザクションが残る問題を修正した
	//・XF100の「指定機能の起動」でパラメータをNULLではなく、受け取ったパラメータをそのまま渡すようにした
	//
	//1.2.6
	//・XF200,XF310のフィールドにプロンプタが設定されている場合、交換フィールドを非表示フィールドとして組み込んだ
	//・見出しテーブルと明細テーブルが同一である場合、XF300の明細機能起動時のパラメータを明細行固有のキーマップのみにした
	//
	//1.2.5
	//・テーブルスクリプト中のデータソースオブジェクトにvalueListのプロパティを追加した
	//・XF310の行追加ルーチンにBYTEA型向けの設定ロジックが抜けていた問題を修正した
	//・XF310の行高の設定ミスを修正した
	//・XF110の更新リストの年月項目、および年項目に関する設定ミスを修正した
	//
	//1.2.4
	//・起動時にJavaのバージョンをチェックするようにした
	//・XF300の明細タブを無効化するためのパラメータを設けた
	//・instance関数としてsetVariant(...)とgetVariant(...)を設けた
	//・テーブルスクリプト中のデータソースオブジェクトにenabledのプロパティを追加した
	//・DATETIMEのデータ型を導入した
	//・メニューの画像表示のロジックと様式を改善した
	//・BYTEAのデータ型を処理するためのロジックを組み込んだ
	//
	//1.2.3
	//・getTaxAmount(...)のセッション関数で日付がnullの場合に異常終了せずに税額０円を返すようにした
	//・エラー発生時にはエラーログのみを書きだして、テーブル操作ログを書かないようにした（テーブル操作ログが長すぎることがあるため）
	//・XF300の見出し域フィールドが入力可能になるケースがある問題を修正した
	//・XF390で結合フィールドを一覧順に選ぶと区分フィールドの出力でエラーになる問題を修正した
	//・DBとの接続が切れている場合に再接続をガイドするようにした
	//・XF110で多段表示できなくなっていた問題を修正した
	//
	//1.2.2
	//・入力フィールドにフォーカスが当たったとき、値を全選択状態にするようにした
	//・カラム数上限255の制限をなくすために、EXCEL出力についてxlsからxlsx形式に変更した
	//・XF110,200,310について、更新排他制御フィールドが載っていないテーブルを扱う際に異常終了するのでなく、更新排他制御をしないようにした
	//・XF110,310について、スプリットラインの初期設定値のずれを修正した
	//・XF300,310について、キー入力ダイアログのサイズを修正した
	//propertiesの読み取りロジックを改善した
	//
	//1.2.1
	//・XF100,110の読込単位行数の扱いを刷新した
	//・XF110,200,310のメッセージ表示域のサイズ設定のロジックを改善した
	//・桁数なしのVARCHARのフィールドで項目値が表示されなかった問題を修正した
	//
	//1.2.0
	//・フォントをシステム定義によって可変にした
	//・基本フォントサイズを14から18に変更してパネルのレイアウトロジックを変更した
	//・機能ボタンのフォントサイズをボタン幅に合わせて調整するようにした
	//・DB接続処理まわりの仕様を改善した
	//・SQL Serverに対応した（ただしjarにドライバを組み込んだのみでロジック上の変更なし）
	//・XF100,110の読込単位行数の扱いを改善するとともにと、最大表示行数に関するロジックを組み込んだ
	//
	//34
	//・XF200の追加モードでのプロンプトリストフィールドの初期値設定に関する問題を修正した
	//・日付フィールドの初期値設定に関する問題を修正した
	//・WEBサービスにPUT命令を送信するためのセッション関数をcreateServiceRequest(...)として作り替えた
	//・ハッシュ関数を利用するためのセッション関数としてgetDigestedValue(...)を追加した
	//・XEAD Serverで利用するために、Sessionのコンストラクタや初期化ルーチンの構成を変更し、closeメソッドをpublicにした
	//・XEAD Serverで利用するために、XFExecutableとXFScriptableをpublicなインタフェースとして独立させた
	//・セッション履歴の項目「処理系」について、XEAD DriverとXEAD Serverのバージョン値を接頭語で区別できるようにした
	//・パスワード変更ダイアログの新パスワードの入力フィールドを２つにした
	//
	//33
	//・XF100,XF110で日時型フィールドを検索条件として指定した場合の問題を修正した
	//・セッションパラメータSKIP_PRELOADの扱いに関する問題を修正した
	//・KBCALENDARのユーザ定義区分が登録されていなければログインできないようにした
	//・セッションのＩＰ情報をセットする際に、TXIPADDRESSのフィールド長に合わせて値を短縮するようにした
	//
	//32
	//・Webサービスを利用するための関数session.requestWebService(...)を追加した
	//・XMLデータやJSONデータを処理するためのSession関数を追加した
	//・XF110,XF200,XF310で区分系項目がリスト表示されないケースが生じていた問題を修正した
	//
	//31
	//・XF300,XF310のキー入力ダイアログのキー検索ロジックを改善した
	//・XF110,XF310のプロンプトが設定されたカラムの編集設定の仕様を改善した
	//・XF110,XF200,XF310に表示専用の値リストフィールドと区分フィールド用のコンポーネントクラスを導入した
	//・TableOperatorのgetValueOf(...)で返し値がnullであれば''を、また後ブランクを除去して返すようにした
	//・XF110での関連機能起動チェックボックスの位置設定ロジックを改善した
	//・XF200で更新専用とした場合、更新ボタンに指定のキャプション値をセットするようにした（従来は指定にかかわら"更新"をセットしていた）
	//・XF390のスクリプト変数の設定ロジックに含まれていたバグを修正
	//・XF110,XF200,XF310でリストボックスの幅制御が出来なかった問題を修正
	//・SKIP_PRELOADのアプリケーションパラメータに対応した
	//・ログイン時にLOGIN_PERMITTEDのシステム変数が登録されていない場合の動きを改善した
	//
	//30
	//・セッション開始時にセッションテーブルの「処理系バージョン」にDriverのバージョンを記録するようにした
	//・TableOperatorのメソッドとしてsetDistinctFields(...)を追加した
	//・InputDialogのフィールドのメソッドとしてgetItemCount(),setFileChooser(...)を追加した
	//・session関数としてcreateTextFileOperator(...),existsFile(...),deleteFile(...),renameFile(...)を追加した
	//・oracleへの対応にともなうSelect文設定処理に含まれていたバグを修正した
	//・XF100,XF300の明細フィールドの非表示オプションに対応した
	//・XF110とXF310の見出し域のプロンプトフィールドの編集可設定をXF200に合わせた
	//・XF110,XF200,XF310,XF390について同一定義向けに連続実行した際に生じる問題を修正した
	//
	//29
	//・session関数としてgetMonthlyExchangeRate(currency, date, type)を追加した
	//・スプラッシュ上のメッセージのミスを修正した
	//・XF100の明細行から機能の起動に失敗した場合のメッセージ表示方式を改善した
	//・XF300のタブ別の初期表示メッセージを改善した
	//・oracleへの対応のために、明細テーブル向けに生成されるSelect文を調整した
	//
	//28
	//・XF100,XF110,XF310で同一機能向けに２回目を起動した場合の不具合を修正した
	//・パネル系機能タイプで、２回目を起動した場合の固定Whereの取り込みの不具合を修正した
	//・XF100,XF110,XF300の区分系絞込み条件の表示設定まわりを改善した
	//・session.executeProgram(...)の終了メッセージをダイアログ表示させるのをやめて、戻り値にした
	//・Menu上でのEscapeキーをログアウト要求にあてがった
	//・XF110,XF310での明細行のエラーメッセージのハンドリングを改善した
	//・XF110,XF310で結合フィールドを検索する前にそれらを初期化するようにした
	//・XF310でブランク行を追加して値を入力した後のチェックでブランクチェックがスキップされる問題を修正
	//・XF110でバッチフィールドを伴わない場合、「次へ」を押すと固まってしまうバグを修正した
	//
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
	private String font_;

	public DialogAbout(JDialog parent, String font) {
		super(parent);
		parent_ = parent;
		font_ = font;
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

		labelName.setFont(new java.awt.Font(font_, 1, 20));
		labelName.setHorizontalAlignment(SwingConstants.CENTER);
		labelName.setText(PRODUCT_NAME);
		labelName.setBounds(new Rectangle(0, 8, 240, 22));
		labelVersion.setFont(new java.awt.Font(font_, 0, 16));
		labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
		labelVersion.setText(FULL_VERSION);
		labelVersion.setBounds(new Rectangle(0, 32, 240, 20));
		labelCopyright.setFont(new java.awt.Font(font_, 0, 16));
		labelCopyright.setHorizontalAlignment(SwingConstants.CENTER);
		labelCopyright.setText(COPYRIGHT);
		labelCopyright.setBounds(new Rectangle(0, 53, 240, 20));
		labelURL.setFont(new java.awt.Font(font_, 0, 14));
		labelURL.setHorizontalAlignment(SwingConstants.CENTER);
		labelURL.setText("<html><u><font color='blue'>" + URL_DBC);
		labelURL.setBounds(new Rectangle(0, 75, 240, 20));
		labelURL.addMouseListener(new About_labelURL_mouseAdapter(this));
		insetsPanel3.setLayout(null);
		insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
		insetsPanel3.setPreferredSize(new Dimension(250, 80));
		insetsPanel3.add(labelName, null);
		insetsPanel3.add(labelVersion, null);
		insetsPanel3.add(labelCopyright, null);
		insetsPanel3.add(labelURL, null);

		buttonOK.setText("OK");
		buttonOK.setFont(new java.awt.Font(font_, 0, 16));
		buttonOK.addActionListener(this);
		insetsPanel1.add(buttonOK, null);

		panel1.add(insetsPanel1, BorderLayout.SOUTH);
		panel1.add(panel2, BorderLayout.NORTH);
		panel2.setPreferredSize(new Dimension(350, 100));
		panel2.add(insetsPanel2, BorderLayout.CENTER);
		panel2.add(insetsPanel3, BorderLayout.EAST);

		this.setTitle("About X-TEA Driver");
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
