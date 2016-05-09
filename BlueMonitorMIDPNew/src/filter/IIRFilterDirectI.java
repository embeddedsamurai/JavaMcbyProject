package filter;

public class IIRFilterDirectI {

	/**  �t�B���^�̎��� */
	private int order;	
	/** ���͂̕⊮�f�[�^�p�z��(�t�B���^�̎������K�v)*/
	private double[] gapInput;
	/** ���͂̕⊮�f�[�^�p�z��(�t�B���^�̎������K�v)*/
	private double[] gapOutput;
	
	/**
	 * �R���X�g���N�^ 
	 * @param order ����
	 */
	public IIRFilterDirectI(int order) {
		this.order = order;
		
		//�t�B���^�̕⊮�p�f�[�^�z��
		gapInput = new double[order];
		gapOutput = new double[order];
		
		//������
		for(int i = 0; i < gapInput.length ; i++){
			gapInput[i] = 0;
			gapOutput[i] = 0;
		}
		
	}
	
	/**
   	 * ���ڌ^�h IIR�t�B���^�[�����s����֐�
	 * 
	 * @param input   ���͒l�z��
	 * @param output  �o�͒l�̔z��
	 * @param num     ���q�W���̔z��     
	 * @param den�@�@�@�@ ����W���̔z��
	 */
	public synchronized void filter(final double[] input,double[] output
			                       ,final double[] num, final double[] den){	
	
		for(int i = den.length - 1; i < input.length; i++){
			output[i] = 0;
			
			//��Βl���������Ȃ萳�����v�Z���ł��Ȃ��Ȃ邱�Ƃ��킯�邽��
			//�����Z�ƈ����Z���킯��			
			double[] am = new double[num.length + den.length];						
			for(int j = 0; j < num.length ; j++){
				am[j] = num[j]*input[i-j];
				am[j+num.length] = -1 * (den[j]*output[i-j]);
			}			
			//�����Z
			for(int j = 0; j < am.length ; j++){
				if(am[j] >= 0){
					output[i] += am[j];
				}
			}
			//�����Z
			for(int j = 0; j < am.length ; j++){
				if(am[j] < 0){
					output[i] += am[j];
				}
			}		
		}		
	}
	
	/**
	 * �t�B���^��������(�o�͂̒[�̖����f�[�^���Ȃ������߂ɁA�⊮�f�[�^����͂���) *IIR�t�B���^
	 * 
	 * @param input     �t�B���^�̓��̓f�[�^
	 * @param output    �t�B���^�̏o�͂��i�[����� (�o�͂Ɠ��͂̔z��͓����ł��n�j)
	 * @param gapInput  ���͂̕⊮�f�[�^�p�z��(�t�B���^�̎������K�v)
	 * @param gapOutput �o�͂̕⊮�f�[�^�p�z��(�t�B���^�̎������K�v)
	 * @param num     ���q�W���̔z��     
	 * @param den�@�@�@�@ ����W���̔z��
	 */
	public synchronized void doFilter(final double[] input,double[] output,						
									       final double[] num,final double[] den){
		
		//�t�B���^�ۊǗp�f�[�^�̃T�C�Y
		final int gapSize = gapInput.length;
		//�t�B���^�p�o�b�t�@(������͕��ƁA�⊮�f�[�^���̑傫�����m��)
		double[] tmpInput = new double[input.length+gapSize];
		double[] tmpOutput= new double[output.length+gapSize];
		
		//�⊮�̂��ߑO��̓��͂Əo�͂������
		for(int i = 0; i < gapSize ; i++){
			tmpInput[i]  = gapInput[i];
			tmpOutput[i] = gapOutput[i];
		}
		//����̓��̓f�[�^������
		for(int i = 0 ; i < input.length ; i++){			
			tmpInput[i + gapSize] = input[i];
		}
		
		//�t�B���^����
		filter(tmpInput,tmpOutput,num,den);
		
		//����̕⊮�̂��߂ɍ���̒[�̏o�͂ƁA���͂�����Ă���
		for(int i = 0; i < gapSize ; i++){
			gapInput[i]  = tmpInput[tmpInput.length - gapSize + i];
			gapOutput[i] = tmpOutput[tmpOutput.length - gapSize + i];
		}
		
		//�o�͂֏o�͌��ʂ��R�s�[		
		for(int i = 0 ; i < output.length ; i++){
			output[i] = tmpOutput[i + gapSize];
		}
	}
}
