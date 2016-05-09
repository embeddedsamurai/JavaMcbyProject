package filter;

public class FIRFilter {

	/**  �t�B���^�̎��� */
	private int order;	
	/** ���͂̕⊮�f�[�^�p�z��(�t�B���^�̎������K�v)*/
	private double[] gapInput;
	
	/**
	 * �R���X�g���N�^
	 *  
	 * @param order ����
	 */
	public FIRFilter(int order) {
		this.order = order;
		
		//�t�B���^�̕⊮�p�f�[�^�z��
		gapInput = new double[order];
		
		//������
		for(int i = 0; i < gapInput.length ; i++){
			gapInput[i] = 0;
		}
	}	
	
	/**
   	 *FIR�t�B���^�[�����s����֐�
	 * 
	 * @param input   ���͒l�z��
	 * @param output  �o�͒l�̔z��
	 * @param coff     �W���̔z��     
	 */
	public synchronized void filter(final double[] input,double[] output
			                       ,final double[] coff){	
		//50Hz �V���O���m�b�`�t�B���^-
		for(int i = coff.length - 1; i < input.length; i++){
			output[i] = 0;
			
			//��Βl���������Ȃ萳�����v�Z���ł��Ȃ��Ȃ邱�Ƃ��킯�邽��
			//�����Z�ƈ����Z���킯��			
			double[] am = new double[coff.length];						
			for(int j = 0; j < coff.length ; j++){
				am[j] = coff[j]*input[i-j];
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
	 * �t�B���^��������(�o�͂̒[�̖����f�[�^���Ȃ������߂ɁA�⊮�f�[�^����͂���) *FIR�t�B���^
	 * 
	 * @param input     �t�B���^�̓��̓f�[�^
	 * @param output    �t�B���^�̏o�͂��i�[�����  (�o�͂Ɠ��͂̔z��͓����ł��n�j)
	 * @param gapInput  ���͂̕⊮�f�[�^�p�z��    (�t�B���^�̎������K�v)     
	 * @param coff�@�@�@�@   �W���̔z��
	 */
	public synchronized void filterRapper(final double[] input,double[] output,
									       final double[] coff){
		
		//�t�B���^�ۊǗp�f�[�^�̃T�C�Y
		final int gapSize = gapInput.length;
		//�t�B���^�p�o�b�t�@(������͕��ƁA�⊮�f�[�^���̑傫�����m��)
		double[] tmpInput = new double[input.length+gapSize];
		double[] tmpOutput= new double[input.length+gapSize];
		
		//�⊮�̂��ߑO��̓��͂������
		for(int i = 0; i < gapSize ; i++){
			tmpInput[i]  = gapInput[i];			
		}
		//����̓��̓f�[�^������
		for(int i = 0 ; i < input.length ; i++){			
			tmpInput[i + gapSize] = input[i];
		}
		
		//�t�B���^����
		filter(tmpInput,tmpOutput,coff);
		
		//����̕⊮�̂��߂ɍ���̓��͂�����Ă���
		for(int i = 0; i < gapSize ; i++){
			gapInput[i]  = tmpInput[tmpInput.length - gapSize + i];			
		}
		
		//�o�͂֏o�͌��ʂ��R�s�[		
		for(int i = 0 ; i < output.length ; i++){
			output[i] = tmpOutput[i + gapSize];
		}
	}
}
