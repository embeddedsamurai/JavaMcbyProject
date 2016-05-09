package util;

public class DoubleRingBuffer {

    /** バッファ */
    private final double[] buffer;
    /** 次にPutする場所 */
    private int tail;
    /** 次にTakeする場所 */
    private int head;
    /** バッファ内の現在のデータ数*/
    private int count;
    
    /** 最大バッファサイズ */
    public final int MAX_SIZE;

    public DoubleRingBuffer(int maxbuffer) {
        this.buffer = new double[maxbuffer];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
        MAX_SIZE = maxbuffer;
    }

    /**
     * バッファにデータを追加する
     * @param data
     * @throws InterruptedException
     */
    public synchronized void put(double data) throws InterruptedException/*OverflowIntQueueException*/ {

        //System.out.println(Thread.currentThread().getName() + " puts " + data + " counter " + count);

        while (count >= buffer.length) {
        	//キューが満杯の間
            wait();
        }
        //バッファにデータを追加する
        buffer[tail] = data;
        //バッファの追加インデックスを一つ進める
        tail = (tail + 1) % buffer.length;
        //バッファのデータ数カウンタを一つ進める
        count++;
        notifyAll();
    }

    /**
     * バッファからデータを取り出す
     * @return
     * @throws InterruptedException
     */
    public synchronized double get() throws InterruptedException /*EmptyIntQueueException*/ {
        while (count <= 0) {
        	//キューは空の間
            wait();
        }
        //バッファからデータを取り出す
        double read_data = buffer[head];
        //バッファの取り出しインデックスを一つ進める
        head = (head + 1) % buffer.length;
        //バッファのデータ数カウンタを一つ減らす
        count--;
        notifyAll();
        //System.out.println(Thread.currentThread().getName() + " gets " + read_data + " counter " + count);
        return read_data;
    }

	/**
	 * 現在バッファに格納されているデータの数
	 * @return バッファに格納されているデータ数
	 */
	public synchronized int getSize(){
		return count;
	}


}
