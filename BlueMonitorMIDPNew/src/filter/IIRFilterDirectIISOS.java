package filter;

public class IIRFilterDirectIISOS {
	
	/**  �Z�N�V�����̐� */
	private int section;	
	
	/** �v�Z�o�b�t�@�p�⊮�f�[�^�̔z�� */
	private double[][] gapUn;
		
	/** ���͂̕⊮�f�[�^�p�z��(�t�B���^�̎������K�v)*/
	private double[] gapInput;

	/**
	 * �R���X�g���N�^
	 * 
	 * @param section �Z�N�V�����̐�
	 */
	public IIRFilterDirectIISOS(int order) {
		this.section = order;
		
		//�t�B���^�̕⊮�p�f�[�^�z��
		gapInput = new double[2];
		
		//������
		for(int i = 0; i < gapInput.length ; i++){
			gapInput[i] = 0;
		}
		
		//�v�Z�o�b�t�@�p�⊮�f�[�^
		gapUn = new double[order][2];
			
		//������
		for(int i = 0; i < order ; i++){
			for(int j = 0; j < gapUn[i].length; j++){
				gapUn[i][j] = 0;
			}
		}
	}
	
	/**
	 * �t�B���^��������(�o�͂̒[�̖����f�[�^���Ȃ������߂ɁA�⊮�f�[�^����͂���)
	 * (�񎟃Z�N�V�����̌p���ڑ��̒��ڌ`II IIR�t�B���^�[)
	 * @param input         �t�B���^�̓��̓f�[�^
	 * @param output        �t�B���^�̏o�͂��i�[����� (�o�͂Ɠ��͂̔z��͓����ł��n�j)
	 * @param num           �e�Z�N�V�����̕��q�W�����i�[����z��
	 * @param den		   	�e�Z�N�V�����̕���W�����i�[����z��			
	 * @param gain        �@�@�@�@�@	�e�Z�N�V�����̃Q�C�����i�[����z��
	 */
	public void doFilter(final double[] input, double[] output
	        			,final double[][] num, final double[][] den
	        			,final double[] gain){
		
		//�t�B���^�p�o�b�t�@(������͕��ƁA�⊮�f�[�^���̑傫�����m��)
		double[] tmpInput  = new double[input.length+2];
		double[] tmpOutput = new double[output.length+2];
		//�⊮�̂��ߑO��̓��͂������
		for(int i = 0; i < 2 ; i++){
			tmpInput[i]  = gapInput[i];			
		}
		//����̓��̓f�[�^������
		for(int i = 0 ; i < input.length ; i++){			
			tmpInput[i+2] = input[i];
		}
		
		//�v�Z�p�̃o�b�t�@(������⊮�̂��߂ɑO��̃f�[�^������)
		double[][] un = new double[section][input.length  + 2];
		double[][] yn = new double[section][output.length + 2];
		//�⊮�̂��ߑO��̌v�Z�p�o�b�t�@�̃f�[�^�������
		for(int i = 0; i < section ; i++){
			for(int j = 0; j < 2 ; j++){
				un[i][j] = gapUn[i][j];
				yn[i][j] = 0;
			}				
		}		
				
		//�t�B���^����
		filter(tmpInput,tmpOutput,num,den,un,yn,gain);
		
		//����̕⊮�̂��߂ɍ���̒[�̓��͂�����Ă���
		for(int i = 0; i < 2 ; i++){
			gapInput[i]  = tmpInput[tmpInput.length - 2 + i];			
		}
		
		for(int i = 0; i < section ; i++){
			for(int j = 0; j < 2 ; j++){
				gapUn[i][j] = un[i][un[i].length - 2 + j];				
			}				
		}
				
		//�o�͂֏o�͌��ʂ��R�s�[		
		for(int i = 0 ; i < output.length ; i++){
			output[i] = tmpOutput[i + 2];
		}
	
	}
	
	/**
	 * �t�B���^����������
	 * @param input		���̓f�[�^
	 * @param output	�o�̓f�[�^
	 * @param num		���q�W��
	 * @param den		����W��
	 * @param un        �v�Z�p�o�b�t�@ 
	 * @param yn		�v�Z�p�o�b�t�@
	 * @param gain		�Q�C���̔z��  
	 */
	public void filter(final double[] input, double[] output
	        		   ,final double[][] num, final double[][] den
	        		   ,double[][] un,double[][] yn
	        		   ,final double[] gain){

		for(int i = 2; i < input.length ; i++){
						
			un[0][i] -= den[0][1]*un[0][i-1] + den[0][2]*un[0][i-2] + input[i]; 	 						
			yn[0][i] = num[0][0]*un[0][i] + num[0][1]*un[0][i-1] + num[0][2]*un[0][i-2];
			
			for(int j = 1; j < section ; j++){
				un[j][i] -= den[j][1]*un[j][i-1] + den[j][2]*un[j][i-2] + yn[j-1][i]*gain[j-1];	 						
				yn[j][i] = num[j][0]*un[j][i] + num[j][1]*un[j][i-1] + num[j][2]*un[j][i-2];			
			}
			output[i] = yn[section-1][i]*gain[section-1];

		}		

	}
}
