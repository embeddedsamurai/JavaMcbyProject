

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import main.Main;

public class BPMonitor extends MIDlet{

	/**
	 * �R���X�g���N�^ 
	 */
	public BPMonitor() {
		// ���ӎ����F
		// �@��ɂ���ẮA���̃��\�b�h�𔲂���܂ŉ�ʂ̍ĕ`�悪�s���Ȃ��ꍇ������܂��B
		// �R���X�g���N�^�ł͂����܂ŏ����ɓO���A�`���X���b�h�̊J�n��startApp�̒��ōs���܂��傤�B
		
		//���C�����[�v�̋N��
		new Main(this);
	}
	/**
	 * �A�v���P�[�V�������I������ۂɌĂ΂�܂��B
	 * 
	 * @param unconditional
	 */
	public void destroyApp( boolean unconditional ) throws MIDletStateChangeException {
		// TODO �����Ńf�[�^�̕ۑ����s�����肵�܂��B
		
		// ���ӎ����F
		// MIDlet#notifyDestroyed()�ɂ��A�v���P�[�V�����̎����I�Ȓ�~���ɂ͌Ă΂�܂���B
		// MIDP���z�肵�Ă���@��ł́A�f�[�^�̕ۑ��ɂ͎��Ԃ�������ꍇ�����邽�߁A
		// �ۑ����ׂ��f�[�^�͉\�Ȍ���R���p�N�g�ɂ��ׂ��ł��B
		// �@��ɂ���Ă͏I���ɔ�₹�鎞�Ԃɐ�����݂���ꍇ�����邽�߁A�����ő�ʂ̏�����
		// �s�킸�ɂ��ނ悤�ȍH�v���K�v�ł��B
	}

	/**
	 * �A�v���P�[�V�������ꎞ��~�����ۂɌĂ΂�܂��B
	 */
	public void pauseApp() {
		// ���ӎ����F
		// �@��ɂ���Ă͈ꎞ��~�܂łɔ�₹�鎞�Ԃɐ�����݂���ꍇ�����邽�߁A�����ő�ʂ̏�����
		// �s�킸�ɂ��ނ悤�ȍH�v���K�v�ł��B
	}

	/**
	 * MIDlet�̋N�������������A�J�n�i�ĊJ�j�����ۂɌĂ΂�܂��B
	 */
	public void startApp() throws MIDletStateChangeException {
		// ���ӎ����F
		// �ꎞ��~����̍ĊJ���ɂ��Ă΂�邽�߁A�����ɃA�v���P�[�V�����̏������������L�q�����
		// �V�K�N������������������K�v�̖������̂��ēx���������Ă��܂����ꂪ����܂��B
	}
}
