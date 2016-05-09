package util;

public class DoubleRingBuffer {

    /** �o�b�t�@ */
    private final double[] buffer;
    /** ����Put����ꏊ */
    private int tail;
    /** ����Take����ꏊ */
    private int head;
    /** �o�b�t�@���̌��݂̃f�[�^��*/
    private int count;
    
    /** �ő�o�b�t�@�T�C�Y */
    public final int MAX_SIZE;

    public DoubleRingBuffer(int maxbuffer) {
        this.buffer = new double[maxbuffer];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
        MAX_SIZE = maxbuffer;
    }

    /**
     * �o�b�t�@�Ƀf�[�^��ǉ�����
     * @param data
     * @throws InterruptedException
     */
    public synchronized void put(double data) throws InterruptedException/*OverflowIntQueueException*/ {

        //System.out.println(Thread.currentThread().getName() + " puts " + data + " counter " + count);

        while (count >= buffer.length) {
        	//�L���[�����t�̊�
            wait();
        }
        //�o�b�t�@�Ƀf�[�^��ǉ�����
        buffer[tail] = data;
        //�o�b�t�@�̒ǉ��C���f�b�N�X����i�߂�
        tail = (tail + 1) % buffer.length;
        //�o�b�t�@�̃f�[�^���J�E���^����i�߂�
        count++;
        notifyAll();
    }

    /**
     * �o�b�t�@����f�[�^�����o��
     * @return
     * @throws InterruptedException
     */
    public synchronized double get() throws InterruptedException /*EmptyIntQueueException*/ {
        while (count <= 0) {
        	//�L���[�͋�̊�
            wait();
        }
        //�o�b�t�@����f�[�^�����o��
        double read_data = buffer[head];
        //�o�b�t�@�̎��o���C���f�b�N�X����i�߂�
        head = (head + 1) % buffer.length;
        //�o�b�t�@�̃f�[�^���J�E���^������炷
        count--;
        notifyAll();
        //System.out.println(Thread.currentThread().getName() + " gets " + read_data + " counter " + count);
        return read_data;
    }

	/**
	 * ���݃o�b�t�@�Ɋi�[����Ă���f�[�^�̐�
	 * @return �o�b�t�@�Ɋi�[����Ă���f�[�^��
	 */
	public synchronized int getSize(){
		return count;
	}


}
