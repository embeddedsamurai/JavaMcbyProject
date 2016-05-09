/*
 * 作成日: 2005/9/10
 *
 * Copyright 2001 - 2008 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: SDCard.java,v 1.1 2005/9/10 06:11:11 esamurai Exp $
 */


package sd;

import java.io.InputStream;
import java.io.OutputStream;




public abstract class SDCard{

	/**
		* InputStream バイト単位で連続的な入力を行う全てのクラスの
		* スーパークラスです。
		*/
	protected InputStream is;

	/**
		* OutputStream バイト単位で連続的な出力を行う全てのクラスの
		* スーパークラスです
		*/
	protected OutputStream os;

	/**
		* 入力ストリームからデータを1バイト読み出します
		* 読み出したデータは0～255のint型の値として返す
		*/
	public abstract int read();

	/**
		* 入力ストリームからデータを1行読み出します
		*/
	public abstract byte[] readLine();

	/**
		* 読み出したデータのバイト数を返す
		* このメソッドは、read(data, 0, data.length) と記述するのと等価です。
		*/
	public abstract int read(byte[] rdata);

	/**
		*
		* 入力ストリームから指定バイト数分のデータを指定された byte 配列の指定位置へ読み出します
		*
		* このメソッドは指定バイト数のデータの読み出しが完了するか、
		* 入力ストリームが終端に達するか
		* あるいは例外が throw されるまでブロックします。
		*
		* このメソッドで例外が throw されるのは以下の場合です。
		*
		* 初回のデータの読み出し中に入力ストリームが終端に達した以外の要因で
		* 読み出しに失敗した場合は IOException を throw します。
		*
		* index が負数あるいは index + length が data のサイズを超える場合は
		* IndexOutOfBoundsException が throw されます。
		*
		* data が null 場合は NullPointerException が throw されます。
		*
		* これらの例外が throw された場合でも、そこまでに読み込んだデータは
		* data に格納されます。
		*
		* 返り値として実際に読み込んだバイト数を返します。
		* 入力ストリームが終端に達していてデータが読み出せなかった場合は -1 を返します。
		*/
	public abstract int read(byte[] rdata,int offset,int length) throws Exception;

	/**
		*
			*/
	public abstract void write(final byte[] data);

	/**
		*
		*/
	public abstract void write(final byte[] data,final int offset,final int size);

	/**
		* ファイルをオープンします
		*/
	public abstract void open(final String fname,final int mode);

	/**
		* ファイルをクローズします
		*/
	public abstract void close();

	/**
		* バッファ中のデータをフラッシュします
		*/
	public abstract void flush();
}
