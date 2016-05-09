

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import main.Main;

public class BPMonitor extends MIDlet{

	/**
	 * コンストラクタ 
	 */
	public BPMonitor() {
		// 注意事項：
		// 機器によっては、このメソッドを抜けるまで画面の再描画が行われない場合があります。
		// コンストラクタではあくまで準備に徹し、描画やスレッドの開始はstartAppの中で行いましょう。
		
		//メインループの起動
		new Main(this);
	}
	/**
	 * アプリケーションが終了する際に呼ばれます。
	 * 
	 * @param unconditional
	 */
	public void destroyApp( boolean unconditional ) throws MIDletStateChangeException {
		// TODO ここでデータの保存を行ったりします。
		
		// 注意事項：
		// MIDlet#notifyDestroyed()によるアプリケーションの自発的な停止時には呼ばれません。
		// MIDPが想定している機器では、データの保存には時間がかかる場合があるため、
		// 保存すべきデータは可能な限りコンパクトにすべきです。
		// 機器によっては終了に費やせる時間に制限を設ける場合があるため、ここで大量の処理を
		// 行わずにすむような工夫が必要です。
	}

	/**
	 * アプリケーションが一時停止される際に呼ばれます。
	 */
	public void pauseApp() {
		// 注意事項：
		// 機器によっては一時停止までに費やせる時間に制限を設ける場合があるため、ここで大量の処理を
		// 行わずにすむような工夫が必要です。
	}

	/**
	 * MIDletの起動準備が整い、開始（再開）される際に呼ばれます。
	 */
	public void startApp() throws MIDletStateChangeException {
		// 注意事項：
		// 一時停止からの再開時にも呼ばれるため、ここにアプリケーションの初期化処理を記述すると
		// 新規起動時しか初期化する必要の無いものを再度初期化してしまう恐れがあります。
	}
}
