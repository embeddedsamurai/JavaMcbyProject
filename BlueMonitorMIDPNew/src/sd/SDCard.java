/*
 * �쐬��: 2005/9/10
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
		* InputStream �o�C�g�P�ʂŘA���I�ȓ��͂��s���S�ẴN���X��
		* �X�[�p�[�N���X�ł��B
		*/
	protected InputStream is;

	/**
		* OutputStream �o�C�g�P�ʂŘA���I�ȏo�͂��s���S�ẴN���X��
		* �X�[�p�[�N���X�ł�
		*/
	protected OutputStream os;

	/**
		* ���̓X�g���[������f�[�^��1�o�C�g�ǂݏo���܂�
		* �ǂݏo�����f�[�^��0�`255��int�^�̒l�Ƃ��ĕԂ�
		*/
	public abstract int read();

	/**
		* ���̓X�g���[������f�[�^��1�s�ǂݏo���܂�
		*/
	public abstract byte[] readLine();

	/**
		* �ǂݏo�����f�[�^�̃o�C�g����Ԃ�
		* ���̃��\�b�h�́Aread(data, 0, data.length) �ƋL�q����̂Ɠ����ł��B
		*/
	public abstract int read(byte[] rdata);

	/**
		*
		* ���̓X�g���[������w��o�C�g�����̃f�[�^���w�肳�ꂽ byte �z��̎w��ʒu�֓ǂݏo���܂�
		*
		* ���̃��\�b�h�͎w��o�C�g���̃f�[�^�̓ǂݏo�����������邩�A
		* ���̓X�g���[�����I�[�ɒB���邩
		* ���邢�͗�O�� throw �����܂Ńu���b�N���܂��B
		*
		* ���̃��\�b�h�ŗ�O�� throw �����͈̂ȉ��̏ꍇ�ł��B
		*
		* ����̃f�[�^�̓ǂݏo�����ɓ��̓X�g���[�����I�[�ɒB�����ȊO�̗v����
		* �ǂݏo���Ɏ��s�����ꍇ�� IOException �� throw ���܂��B
		*
		* index ���������邢�� index + length �� data �̃T�C�Y�𒴂���ꍇ��
		* IndexOutOfBoundsException �� throw ����܂��B
		*
		* data �� null �ꍇ�� NullPointerException �� throw ����܂��B
		*
		* �����̗�O�� throw ���ꂽ�ꍇ�ł��A�����܂łɓǂݍ��񂾃f�[�^��
		* data �Ɋi�[����܂��B
		*
		* �Ԃ�l�Ƃ��Ď��ۂɓǂݍ��񂾃o�C�g����Ԃ��܂��B
		* ���̓X�g���[�����I�[�ɒB���Ă��ăf�[�^���ǂݏo���Ȃ������ꍇ�� -1 ��Ԃ��܂��B
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
		* �t�@�C�����I�[�v�����܂�
		*/
	public abstract void open(final String fname,final int mode);

	/**
		* �t�@�C�����N���[�Y���܂�
		*/
	public abstract void close();

	/**
		* �o�b�t�@���̃f�[�^���t���b�V�����܂�
		*/
	public abstract void flush();
}
