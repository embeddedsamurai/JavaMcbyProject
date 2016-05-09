package util;

public class ByteQueue{
 //--���s����O �L���[���� ---
 public class EmptyIntQueueException extends RuntimeException{
 	public EmptyIntQueueException(){}
 }

 //--���s����O �L���[�����t
 public class OverflowIntQueueException extends RuntimeException{
 	public OverflowIntQueueException(){}
 }

 int max;   //�L���[�̗e��
 int num;   //���݂̃f�[�^��
 int front; //�擪�v�f�J�[�\��
 int rear;  //�����v�f�J�[�\��
 byte[] que; //�L���[�{��

 //�R���X�g���N�^
 public ByteQueue(int capacity){
 	num = front = rear = 0;
	max = capacity;
	try{
		que= new byte[max]; //�L���[�{�̗p�̔z��𐶐�
	}catch(OutOfMemoryError e){
	    max=0; //�����ł��Ȃ�����
	}
 }

 //�L���[�Ƀf�[�^���G���L���[
 public synchronized void  enque(byte x) throws OverflowIntQueueException{
 	if(num >= max)
		throw new OverflowIntQueueException(); //�L���[�͖��t

	que[rear++]=x;
	num++;

	if(rear == max) rear=0;
 }


 //�L���[����f�[�^���f�L���[
 public synchronized byte deque() throws EmptyIntQueueException{
 	if (num <= 0)
		throw new EmptyIntQueueException(); //�L���[�͋�
	byte x = que[front++];
	num--;

	if(front == max) front=0;

	return x;
 }

 //�L���[����f�[�^���s�[�N(�擪�f�[�^��`��)
 public int peek() throws EmptyIntQueueException{
 	if ( num <= 0 )
		throw new EmptyIntQueueException(); //�L���[�͋�

	return que[front];
 }

 //�L���[����ɂ���
 public void clear(){
 	num = front = rear = 0;
 }

 //�L���[�̗e�ʂ�Ԃ�
 public int capacity(){
 	return max;
 }

 //�L���[�ɒ~�����Ă���f�[�^����Ԃ�
 public int size(){
 	return num;
 }

 //�L���[�͋�ł��邩
 public boolean isEmpty(){
 	return num <= 0;
 }

 //�L���[�͖��t�ł��邩
 public boolean isFull(){
  return num >= max;
 }

 /*
 //�L���[���̑S�f�[�^��擪�������̏��ɕ\��
 void dump(){
 	if (num <= 0)
	  System.out.println("�L���[�͋�ł�");
	else{
		for(int i=0;i < num;i++)
		 System.out.print(que[(i + front) % max] + " ");

		 System.out.println();
	}
 }
 */

}
