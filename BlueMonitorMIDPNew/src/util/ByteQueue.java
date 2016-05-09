package util;

public class ByteQueue{
 //--実行時例外 キューが空 ---
 public class EmptyIntQueueException extends RuntimeException{
 	public EmptyIntQueueException(){}
 }

 //--実行時例外 キューが満杯
 public class OverflowIntQueueException extends RuntimeException{
 	public OverflowIntQueueException(){}
 }

 int max;   //キューの容量
 int num;   //現在のデータ数
 int front; //先頭要素カーソル
 int rear;  //末尾要素カーソル
 byte[] que; //キュー本体

 //コンストラクタ
 public ByteQueue(int capacity){
 	num = front = rear = 0;
	max = capacity;
	try{
		que= new byte[max]; //キュー本体用の配列を生成
	}catch(OutOfMemoryError e){
	    max=0; //生成できなかった
	}
 }

 //キューにデータをエンキュー
 public synchronized void  enque(byte x) throws OverflowIntQueueException{
 	if(num >= max)
		throw new OverflowIntQueueException(); //キューは満杯

	que[rear++]=x;
	num++;

	if(rear == max) rear=0;
 }


 //キューからデータをデキュー
 public synchronized byte deque() throws EmptyIntQueueException{
 	if (num <= 0)
		throw new EmptyIntQueueException(); //キューは空
	byte x = que[front++];
	num--;

	if(front == max) front=0;

	return x;
 }

 //キューからデータをピーク(先頭データを覗く)
 public int peek() throws EmptyIntQueueException{
 	if ( num <= 0 )
		throw new EmptyIntQueueException(); //キューは空

	return que[front];
 }

 //キューを空にする
 public void clear(){
 	num = front = rear = 0;
 }

 //キューの容量を返す
 public int capacity(){
 	return max;
 }

 //キューに蓄えられているデータ数を返す
 public int size(){
 	return num;
 }

 //キューは空であるか
 public boolean isEmpty(){
 	return num <= 0;
 }

 //キューは満杯であるか
 public boolean isFull(){
  return num >= max;
 }

 /*
 //キュー内の全データを先頭→末尾の順に表示
 void dump(){
 	if (num <= 0)
	  System.out.println("キューは空です");
	else{
		for(int i=0;i < num;i++)
		 System.out.print(que[(i + front) % max] + " ");

		 System.out.println();
	}
 }
 */

}
