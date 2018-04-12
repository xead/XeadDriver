package xeadDriver;

/*
 * Copyright (c) 2018 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;

public class DialogAbout extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * Application Information
	 */
	public static final String PRODUCT_NAME = "X-TEA Driver";
	public static final String FULL_VERSION  = "V1.R3.M19";
	public static final String VERSION  = "1.3.19";
	public static final String FORMAT_VERSION  = "1.2";
	public static final String COPYRIGHT = "Copyright 2018 DBC Ltd.";
	public static final String URL_DBC = "http://dbc.in.coocan.jp/";
	//1.3.19
	//�EXFInputDialog�̃t�B�[���h��setDirectoryChooser(...)�̃��\�b�h��ǉ�����
	//#InputDialog��ListBox�t�B�[���h��setEditable()��setEnabled()���֘A������
	//�ETableOperator��TableEvaluator�ł�VARCHAR�f�[�^�̈������W�b�N�����P����
	//
	//1.3.18
	//�EgetTaxAmount(...)�Ń}�C�i�X�l�v�Z�̃o�O���C������
	//�EXF110,200,300,310�ł̃t�B�[���h�̃R�����g�\����html�^�O���g����悤�ɂ���
	//�EXF300�ŏ��������������g����悤�ɂ���ƂƂ��ɁA���׃e�[�u�����ڂ̃t�B���^�����O�d�l�����P����
	//�EXF100,110,200,300,310�ŁA�Z�b�V����ID�̃��x���Ƀ��O�\���̃����N��\����
	//
	//1.3.17
	//�EXF110,200,310�̃}�X�^�[�n�v�����v�g���X�g�ɏ�Ƀu�����N�s������悤�ɂ����i���[�U�ɖ����I�ɑI�����Ă��炤���߁j
	//
	//1.3.16
	//�EXF100,110,300�Ō��������ɃZ�b�V����������ݒ肵�ē��͕s�ɂ���ƃG���[�ɂȂ�����C������
	//�E�X�V�s�e�[�u����CopyTableRecords(...)�ň����Ȃ��悤�ɂ���
	//�Evarchar���ڂł̏���T�C�Y�`�F�b�N���͂���Ă��������C������
	//
	//1.3.15
	//�EInputDialog��setFileChooser(...)��CurrentDirectory���w��ł���悤�ɂ���
	//�ETextFileOperator��resetCursor()�̃��\�b�h��g�ݍ���
	//�EXF100,300�ł̖��׍s��̃|�b�v�A�b�v���j���[�̎d�l�����P����
	//�E�X�V�n�@�\�^�C�v�ŁA�������ڂ�NULL�s�ł��o�^���ɃG���[�ɂȂ�Ȃ��o�O���C������
	//
	//1.3.14
	//�EExcel����I�u�W�F�N�g�ɁA�V�[�g�̃f�[�^�s���𓾂邽�߂̃��\�b�h��g�ݍ���
	//�ETable����I�u�W�F�N�g�ł̃G���[�n���h�����O�����P����
	//�EXF000�ł̃��b�Z�[�W�n���h�����O�����P����
	//�EXF100,110,200,300,310�ŁA���ڂɃ}�E�X��u�����ۂ̐����\���d�l�����P����
	//
	//1.3.13
	//�E���j���[���o�R�����ɓ���@�\�݂̂����s�ł���悤�ɂ���
	//�EcopyTableRecords(...)�̎d�l�����P����
	//
	//1.3.12
	//�EXF310�ł̖��׍s�폜�G���[�̕\���l�������P����
	//�EcopyTableRecords(...)�̎d�l�����P����
	//�E�����t�B�[���h���u�ҏW�Ȃ��v�ɐݒ肵���ꍇ�A�[���Ȃ�΃u�����N�\��������悤�ɂ����i�����t�B�[���h���L�[�Ƃ����ꍇ�A�C�ӓ��͂̊O���L�[���ڂ��w�肵�Ȃ��ꍇ�Ƀu�����N�Ŏ������߁j
	//�E�T�uDB�ɐڑ��ł��Ȃ��Ă��Z�b�V�����𗧂��グ��悤�ɂ���
	//
	//1.3.11
	//�EXF100,110,300,390�ł́A���׃e�[�u���Ǎ��O�X�N���v�g�̎��s�^�C�~���O�����P�����i�ȑO�́A���s�O�X�N���v�g�ō��ڒl���Z�b�g���Ă��A���̌�ŏ����������^�C�~���O�ɒu����Ă����j
	//�Esession�֐���compressTable(...)�ɂ��āA�e�[�u��ID�̖�����*��u���˂΃W�F�l���b�N�w��ł��Ȃ��悤�ɂ���
	//�E�V����session�֐��Ƃ��āAcopyTableRecords(...)��showDialogToChooseFile(...)��ǉ�����
	//�E���j���[���Esc�L�[�������΁A�m�F�_�C�A���O�Ȃ��Ń��O�I�t�����悤�ɂ���
	//
	//1.3.10
	//�E�摜�t�B�[���h�̃t�@�C���������w�肾�����ꍇ�̕\���`�������P����
	//�EXF110,200,310�ŁA�敪�t�B�[���h�Ƀv�����v�^�@�\�������N����ƁA�敪�L�q�łȂ��敪�l���\�����������C������
	//�EXF100,300�ŁA���׍s�̃R���e�L�X�g���j���[����֘A�@�\���N���ł���悤�ɂ���
	//�EXF310�ŁA�{�^���N���@�\�̌������ڂ̕s�������o�����ۂ̃G���[���b�Z�[�W�����P����
	//
	//1.3.9
	//�EXF100�ōi�荞�ݏ��������ׂĔ�\���ɂ����ꍇ�̕\���`�������P����
	//�EXF300�Œǉ��ł��閾�׃^�u�̍ő吔��10����20�ɑ��₵��
	//�EXF000�Ŏ��������l��NOW�ɑΉ�����
	//�EXF000�ŏI�����b�Z�[�W���R���\�[����łQ�s�����������C������
	//�E�Z�������pWEB�T�[�r�X�̂��߂̃v���L�V�ݒ��g�ݍ���
	//
	//1.3.8
	//�Esession���\�b�h�Ƃ��āA�����Ȃ���createJsonObject()��ǉ�����
	//�Esession.getUserVariantDescription(...)��ǉ�����
	//�session.getDigestedValue(...)����������
	//�E�V�X�e������e�[�u���́u����Ńe�[�u���v���y���ŗ��ɍ��킹�č��ς��A������������߂�session���\�b�h��ǉ�����
	//�E�u���[�U�l�v���擾�E�ۊǂ��邽�߂�instance���\�b�h��g�ݍ���
	//�EXF200�̍X�V���[�h�ɂ����āA�X�V�֎~���ڂ��X�V�����悤�ȑI�����v�����v�g����Ȃ��ꂽ�ꍇ�A�G���[���b�Z�[�W�������ċ��ۂ���悤�ɂ���
	//
	//1.3.7
	//�Eaccdb������DB�h���C�o���X�V����
	//�E�X�N���v�g�ł̃t�B�[���h�F�̐ݒ肪�����Ȃ��Ȃ��Ă��������C������
	//�EExcel�𑀍삷�邽�߂̃N���XXFExcelFileOperator�Ƃ���𐶐�����session���\�b�h��ǉ�����
	//�EWEB�T�[�r�X�֘A��session���\�b�h���������ǉ�����
	//�EXFTableEvaluator��OrderBy��Key�𓯎��Ɏw�肷��ƈُ�I����������C������
	//
	//1.3.6
	//�EXF310�ŁA�s�ǉ��p�@�\���N���ł���悤�ɂȂ����̂ŁA�u�s�ǉ��ݒ�v��p�~����
	//�EXF200,310�ŁA�X�V�Ώۃe�[�u���ɍX�V���ڂ��Ȃ��ꍇ�Ɉُ�I������o�O���C������
	//�EXF290,390�ŁA�����̃t�B�[���h���ҏW���ꂸ�ɏo�͂����o�O���C������
	//
	//1.3.5
	//�EXF310�ōs�ǉ��p�̋@�\���N������悤�ɂ���
	//�EXF310�́u���׍s��0���Ȃ�Βǉ���������J�n�v�ɑΉ�����
	//�EXF310�̖��׍��ڂ���v�����v�^�@�\�ɑ΂��Č��o�����ڂ̒l��n���Ȃ��Ȃ��Ă��������C������
	//�EXF100,110,300�̍i���ݏ����ő啶���������̋�ʂ����Ȃ��悤�ɂ���
	//�E���ȎQ�ƃe�[�u���̑����̓ǂݎ��Ɋւ���s����C������
	//�EXF100,110�ł̌Œ�Where���g���ƈُ�I����������C������
	//�EXF310�̖��׍s�ǉ��̍ۂɍs�Ԃ��J�E���g�A�b�v����Ȃ����Ƃ̂���o�O���C������
	//�EXF310�Œǉ������΂���̖��׍s���������͂����ɍ폜�ł���悤�ɂ���
	//
	//1.3.4
	//�EXF310�Ō��o�����R�[�h�폜�p�̃{�^�����g����悤�ɂ���
	//�E�K��e�[�u����ǉ����邱�ƂŁAXF100,XF110�ł̃t�B���^�[�l�����[�U���ɋL�^�E���������悤�ɂ���
	//�E���[�UID�̒����⃉�x�������O�C���_�C�A���O�ɔ��f�����悤�ɂ���
	//�EXF300�Ŗ��׃^�u��I�񂾂Ƃ��̓���Ɋւ���ׂ����o�O���C������
	//
	//1.3.3
	//�EXF100,XF200,XF300����̋@�\�N���ɂ����āA�w�肳�ꂽ�Œ�l�̒ǉ��p�����[�^��n���悤�ɂ���
	//�E��L�̉��P�ɂƂ��Ȃ��āAXF200�̍X�V��p�ݒ��p�~����
	//�EXF200�ŁA�L�[�l�w���ADD���[�h�ŋN�����ꂽ�ꍇ�A���ʃ��[�h�Ŏn�܂�悤�ɂ���
	//
	//1.3.2
	//�EXFTextField,XFDateField���ɂ��āA�A���t�@�x�b�g�̃x�[�X���C���ȉ��������ɂ����������P����
	//�ETableOperator�̃��O�������݂ŁASQL���̍Œ���1000���ɂ���
	//�E���s���̋@�\���Esc�L�[�������ꂽ�ꍇ�A�S�@�\����ă��j���[�ɖ߂�悤�ɂ���
	//�E�Z�b�V�������ׂ��L�^����ۂɋ@�\������������ƈُ�I����������C������
	//�ETableOperator��TableEvaluator�ł�addKeyValue�ł̋󔒒l�̏������W�b�N�����P����
	//�EXF110�̃o�b�`���ڂ��o�b�`�e�[�u����PK�ł���ꍇ�ɓ��͕s�ɂȂ�����C������
	//�EXF110�̖��׍��ڂ��o�b�`�e�[�u���ւ̊O���L�[�ł���ꍇ�ɓ��͕s�ɂȂ�悤�ɂ���
	//�E�X�V�@�\��STRING�l�̌���������g�ݍ���
	//�E�t�B�[���h���̌����֐����Z�b�V�����֐��Ƃ��đg�ݍ���
	//�ETableEvaluator�̍X�V�@�\�Ƀ`�F�b�N�I�����[�p�����[�^��g�ݍ���
	//�ETableEvaluator�̍X�V�@�\��STRING�l�̌���������g�ݍ���
	//
	//1.3.1
	//�EBYTEA�t�B�[���h�̈����Ɋւ��đS�̓I�ɉ��P����
	//�E�_���폜�̎������@�\��p�~����
	//�EXF100�ŁA�������ʂ��P�������̏ꍇ�ɖ��׏����������N�����邽�߂̃I�v�V������݂���
	//
	//1.3.0
	//�E�e�[�u����`�͈̔�KEY��`�������X�e�b�v����������
	//�E���j���[��`�̃N���X�`�F�b�J�[�̃��[�h��`�������X�e�b�v����������
	//�E�폜�m�F�_�C�A���O�̕\���^�C�~���O�����P����
	//�E�X�V�n�@�\�^�C�v�ł̃G���[���b�Z�[�W�̈��������P����
	//�ETableEvaluator�N���X��g�ݍ���
	//�E�u�n�b�V�������v�ɒǐ�����֐���g�ݍ���
	//�EAdmin Email�̍��ڂɑΉ�����
	//
	//1.2.22
	//�EXF100�̏������b�Z�[�W�̈��������P����
	//�EXF300�̃��X�|���X����̂��߂ɁA���׃^�u�̑I�����ɖ��׃e�[�u����ǂݍ��ނ悤�ɂ���
	//�E�X�v���b�V���̃��b�Z�[�W��i�x�\�������P����
	//�EXFHttpRequest�̉����I�u�W�F�N�g���e�L�X�g�f�[�^�Ƃ���ƂƂ��ɁAencoding�����g�ݍ���
	//�EXFInputDialog�̃e�L�X�g�G���A�̕�������R�s�[�\�ɂ���
	//
	//1.2.21
	//�ESession#parseStringToXmlDocument(...)�̃o�O���C������
	//�ESession#getOffsetDate(...)�̉ߋ������̌v�Z�̃o�O���C������
	//
	//1.2.20
	//�EgetFYearOfDate(...)��getMSeqOfDate(...)���A�֘A����V�X�e������f�[�^�����݂��Ȃ��Ă����퓮�삷��悤�ɂ���
	//�EXFTableOperator�̃T�[�o�����Őڑ��ُ󂪋N�����Ƃ��̓��������P����
	//�EXF110�̊m�F���X�g�ŕ\������郁�b�Z�[�W�̕s����C������
	//
	//1.2.19
	//�EXF110,XF310�ɂ��āA���׍��ڂ�valuesList��ݒ肵���ꍇ�̃��X�g�{�b�N�X�\���̖����C��
	//�EXFInputAssist���ڂɂ��ē�������P
	//�EXF200�ɂ�����SK�̏d���G���[�̃��b�Z�[�W�����P
	//
	//1.2.18
	//�E�O����v�ł���悤�ȃt�B�[���hID���e�[�u���Ɋ܂߂�ƁAXF300,310,390�ŃG���[���N���蓾������C��
	//�EXF110,200,310�ɂ��āA���o����̔�\���t�B�[���h��NULL�G���[���b�Z�[�W�Ƀt�B�[���h�����܂߂�悤�ɉ��P

	//�EXF300�ɂ��āA�L�[�Ȃ��ŋN�����ꂽ�ꍇ�̃L�[���̓_�C�A���O�ŉE���X����������ꍇ�̓����Ɋւ���o�O���C��
	//�EReferChecker�\�z�X���b�h�̒��ŃX�N���v�g�̋L�q�G���[�������ƃu�����N�\�����������ɑΉ�����
	//
	//1.2.17
	//�Esession��DB�ڑ���`�𓾂邽�߂̃v���p�e�B��������
	//�E�摜�t�B�[���h�̍����ݒ肪�w��s���ɐ��m�ɏ]���Ă��Ȃ����������C������
	//�E�摜�t�B�[���h�̍ĕ\���{�^���̕������߂ăA�C�R����ݒ肵��
	//�EPostgreSQL������JDBC�h���C�o���A�b�v�O���[�h����
	//
	//1.2.16
	//�Esession�̃��\�b�h��getFileName()��������
	//�Esession�̃��\�b�h��getSystemProperty(id)��������
	//�E�ꗗ�`���ł̃L���v�V���������l���t�B�[���h��`�́u�J�������v�ɂ���
	//�E�N�����̃X�v���b�V���̌`�������P����
	//�EXF100,XF110�Œl���X�g�n�̃t�B���^�[�����������Ȃ��Ȃ��Ă��������C������
	//�EXF110�ł̃o�b�`�e�[�u���̃L�[�}�b�v���Q��ڂ̋N�����ɃN���A����Ȃ������C������
	//�ETableOperator�Ő��l�t�B�[���h��Script�ϐ��̐��l��add����ƃG���[�ɂȂ�����C������
	//
	//1.2.15
	//�EXF110_SubList�ł̃J�����T�C�Y�̕ύX��TableCellsEditor���ǐ����Ȃ������C������
	//�E�c�[������X-TEA�@Driver�ɕύX����
	//�EH2 Database Engine��MS Access�ɑΉ�����
	//�EFloat�^�������̍ő�l���X�ɐݒ肵��
	//
	//1.2.14
	//�EJava1.8�ɑΉ����邽�߂ɁAsort������comparator����comparable�x�[�X�ɏC������
	//
	//1.2.13
	//�EXF110,XF310�̖��׏�̃��X�g�{�b�N�X�̓��e��\������ƃt���[�Y���邱�Ƃ���������C������
	//�E�v�����v�^�֐��Ƃ���XF100,XF300���N�����āA�s��I�΂��ɏI�������Ƃ��ɕs�v�ȃ��b�Z�[�W���\�����������C������
	//
	//1.2.12
	//�EXF110�̃o�b�`�v���O�������s�m�F�p�̃`�F�b�N�{�b�N�X�̔z�u�Ɋւ���_�C�A���O�T�C�Y�ݒ�̃��W�b�N�����P����
	//�EXF110�̑I�����X�g�ŃX�y�[�X�L�[�ł��I���ł���悤�ɂ���
	//�EXF310�ŕҏW�\���ڂ����݂��Ȃ��ꍇ�A�N������Ƀ��[�v����o�O���C������
	//�E�X�V�n�@�\�̃v�����v�g�֐��̎��s���ɕs���Ȍ����t�B�[���h���������ꍇ�A�x�����b�Z�[�W���o���悤�ɂ���
	//�EXF200,XF300,XF310���ADISABLED_BUTTON_LIST�̃p�����[�^���󂯎���悤�ɂ���
	//�E�u�X�V�s�v�̃t�B�[���h�̒l���X�V�n�p�l���@�\�ōX�V�ł��Ȃ��悤�ɂ���
	//�EXF000�̃R���\�[���̃��C�A�E�g��I�����b�Z�[�W�̏������W�b�N�����P����
	//�EXF100,XF110�̃t�B���^�[�����̋敪�I��p�֐��Ƃ̂����Ɋւ���o�O���C������
	//�EXF200�̃v�����v�g���X�g�t�B�[���h�ɂ����āA�ǉ����ɏ���������Ȃ��ꍇ������o�O���C������
	//�Esession��inputDialog��ZEROFILL�^�C�v�̍��ڂ��g����悤�ɂ���
	//�EXF200��BOOLEAN�^�C�v�̃t�B�[���h�̏��������[�`���̃o�O���C��
	//�E���p���Ă����X�֔ԍ�����WEB�T�[�r�X�̏I���ɂƂ��Ȃ��ĕʂ̃T�[�r�X�ɍ����ւ���
	//
	//1.2.11
	//�EXF200�Ń��R�[�h���폜���ꂽ�Ƃ��̊m�F���b�Z�[�W�ɍ폜�{�^���̃L���v�V������g�ݍ��ނ悤�ɂ���
	//�EXF200,XF110,XF310�̌��o����ł̃��X�g�R���|�[�l���g�̓���Ɋւ���o�O���C������
	//�EXF100,XF110,XF300,XF310,XF390�ɂ��āA���׃e�[�u���́u�Ǎ��O�X�N���v�g�v��ǎ�J�n�O�ɂP�x�������s����悤�ɂ���
	//�E�t�B�[���h�́u�R�����g�v�ɂ��ĕ\���l�������������
	//�EXF390�ɂ��āA�����̖��׃e�[�u����������悤�ɂ���
	//
	//1.2.10
	//�Esession��formatTime(...)�̊֐���ǉ�����ƂƂ��ɁA�����v�Z�֐������P����
	//�ECheckListDialog�̎g����������P����
	//�EXF300�̌��o�����R�[�h���X�V���ꂽ�Ƃ��́A�c���[�r���[���X�V����悤�ɂ���
	//�EXF200,XF110�̌��o����ł̃��X�g�R���|�[�l���g�̓���Ɋւ���o�O���C������
	//�EXF310�̋N�����̖��׍s�ǉ������Ɋւ���o�O���C������
	//
	//1.2.9
	//�EXF310�̍s�ǉ��_�C�A���O�ł̃{�^���L�q�̃t�H���g�T�C�Y���{�^���T�C�Y�ɉ����ĉςɂ���
	//�E�����t�B�[���h�����Ɂu����(HH:MM)�v�̕ҏW�^�C�v��݂���
	//�EXF310��XF110�̖��׍s��A�����XF200,XF110�̌��o����ł̃��X�g�R���|�[�l���g�̓���Ɋւ���o�O���C������
	//�EXF100,XF110,XF300�̌������������t�̏ꍇ�A���R�[�h��̑Ή����ڂ�null�̂Ƃ��ɃG���[�ɂȂ�o�O���C������
	//
	//1.2.8
	//�E�ҏW���[�h�ł�varchar�\���R���|�[�l���g�̓��������P����
	//�EXF110,310�̖��׍s��ł�ValueList�ݒ�Ɋւ���o�O���C������
	//�EValueList�̑Ή��ɂƂ��Ȃ��e�L�X�g�t�B�[���h�ł̃G���[�F�ƃt�H�[�J�X�ݒ�̃o�O���C������
	//�E�ǉ����[�h�ɂ����ăt���O�t�B�[���h��false�l�������ݒ肷��悤�ɂ���
	//
	//1.2.7
	//�E�c���[�r���[�ł̌��o�����R�[�h�ؑւɑΉ����邽�߂ɁAXF300�̃^�u���������[�`���̈ʒu��ύX����
	//�EXF300�̍\���c���[�p�̃^�C�g���ɑΉ�����
	//�EXF310�̊J�n���ɖ��׍s���[�����ł���΁A�ǉ��s������ÖٓI�ɋN������悤�ɂ���
	//�E�������̓t�B�[���h�̕ϊ����[�h�����������������Ȃ��Ȃ��Ă��������C������
	//�E�Z�b�V�����̃e�[�u������I�u�W�F�N�g���g���ƁA�Z�b�V�����I�����ɃR�~�b�g����Ȃ��g�����U�N�V�������c������C������
	//�EXF100�́u�w��@�\�̋N���v�Ńp�����[�^��NULL�ł͂Ȃ��A�󂯎�����p�����[�^�����̂܂ܓn���悤�ɂ���
	//
	//1.2.6
	//�EXF200,XF310�̃t�B�[���h�Ƀv�����v�^���ݒ肳��Ă���ꍇ�A�����t�B�[���h���\���t�B�[���h�Ƃ��đg�ݍ���
	//�E���o���e�[�u���Ɩ��׃e�[�u��������ł���ꍇ�AXF300�̖��׋@�\�N�����̃p�����[�^�𖾍׍s�ŗL�̃L�[�}�b�v�݂̂ɂ���
	//
	//1.2.5
	//�E�e�[�u���X�N���v�g���̃f�[�^�\�[�X�I�u�W�F�N�g��valueList�̃v���p�e�B��ǉ�����
	//�EXF310�̍s�ǉ����[�`����BYTEA�^�����̐ݒ胍�W�b�N�������Ă��������C������
	//�EXF310�̍s���̐ݒ�~�X���C������
	//�EXF110�̍X�V���X�g�̔N�����ځA����єN���ڂɊւ���ݒ�~�X���C������
	//
	//1.2.4
	//�E�N������Java�̃o�[�W�������`�F�b�N����悤�ɂ���
	//�EXF300�̖��׃^�u�𖳌������邽�߂̃p�����[�^��݂���
	//�Einstance�֐��Ƃ���setVariant(...)��getVariant(...)��݂���
	//�E�e�[�u���X�N���v�g���̃f�[�^�\�[�X�I�u�W�F�N�g��enabled�̃v���p�e�B��ǉ�����
	//�EDATETIME�̃f�[�^�^�𓱓�����
	//�E���j���[�̉摜�\���̃��W�b�N�Ɨl�������P����
	//�EBYTEA�̃f�[�^�^���������邽�߂̃��W�b�N��g�ݍ���
	//
	//1.2.3
	//�EgetTaxAmount(...)�̃Z�b�V�����֐��œ��t��null�̏ꍇ�Ɉُ�I�������ɐŊz�O�~��Ԃ��悤�ɂ���
	//�E�G���[�������ɂ̓G���[���O�݂̂����������āA�e�[�u�����샍�O�������Ȃ��悤�ɂ����i�e�[�u�����샍�O���������邱�Ƃ����邽�߁j
	//�EXF300�̌��o����t�B�[���h�����͉\�ɂȂ�P�[�X����������C������
	//�EXF390�Ō����t�B�[���h���ꗗ���ɑI�ԂƋ敪�t�B�[���h�̏o�͂ŃG���[�ɂȂ�����C������
	//�EDB�Ƃ̐ڑ����؂�Ă���ꍇ�ɍĐڑ����K�C�h����悤�ɂ���
	//�EXF110�ő��i�\���ł��Ȃ��Ȃ��Ă��������C������
	//
	//1.2.2
	//�E���̓t�B�[���h�Ƀt�H�[�J�X�����������Ƃ��A�l��S�I����Ԃɂ���悤�ɂ���
	//�E�J���������255�̐������Ȃ������߂ɁAEXCEL�o�͂ɂ���xls����xlsx�`���ɕύX����
	//�EXF110,200,310�ɂ��āA�X�V�r������t�B�[���h���ڂ��Ă��Ȃ��e�[�u���������ۂɈُ�I������̂łȂ��A�X�V�r����������Ȃ��悤�ɂ���
	//�EXF110,310�ɂ��āA�X�v���b�g���C���̏����ݒ�l�̂�����C������
	//�EXF300,310�ɂ��āA�L�[���̓_�C�A���O�̃T�C�Y���C������
	//properties�̓ǂݎ�胍�W�b�N�����P����
	//
	//1.2.1
	//�EXF100,110�̓Ǎ��P�ʍs���̈��������V����
	//�EXF110,200,310�̃��b�Z�[�W�\����̃T�C�Y�ݒ�̃��W�b�N�����P����
	//�E�����Ȃ���VARCHAR�̃t�B�[���h�ō��ڒl���\������Ȃ����������C������
	//
	//1.2.0
	//�E�t�H���g���V�X�e����`�ɂ���ĉςɂ���
	//�E��{�t�H���g�T�C�Y��14����18�ɕύX���ăp�l���̃��C�A�E�g���W�b�N��ύX����
	//�E�@�\�{�^���̃t�H���g�T�C�Y���{�^�����ɍ��킹�Ē�������悤�ɂ���
	//�EDB�ڑ������܂��̎d�l�����P����
	//�ESQL Server�ɑΉ������i������jar�Ƀh���C�o��g�ݍ��񂾂݂̂Ń��W�b�N��̕ύX�Ȃ��j
	//�EXF100,110�̓Ǎ��P�ʍs���̈��������P����ƂƂ��ɂƁA�ő�\���s���Ɋւ��郍�W�b�N��g�ݍ���
	//
	//34
	//�EXF200�̒ǉ����[�h�ł̃v�����v�g���X�g�t�B�[���h�̏����l�ݒ�Ɋւ�������C������
	//�E���t�t�B�[���h�̏����l�ݒ�Ɋւ�������C������
	//�EWEB�T�[�r�X��PUT���߂𑗐M���邽�߂̃Z�b�V�����֐���createServiceRequest(...)�Ƃ��č��ւ���
	//�E�n�b�V���֐��𗘗p���邽�߂̃Z�b�V�����֐��Ƃ���getDigestedValue(...)��ǉ�����
	//�EXEAD Server�ŗ��p���邽�߂ɁASession�̃R���X�g���N�^�⏉�������[�`���̍\����ύX���Aclose���\�b�h��public�ɂ���
	//�EXEAD Server�ŗ��p���邽�߂ɁAXFExecutable��XFScriptable��public�ȃC���^�t�F�[�X�Ƃ��ēƗ�������
	//�E�Z�b�V���������̍��ځu�����n�v�ɂ��āAXEAD Driver��XEAD Server�̃o�[�W�����l��ړ���ŋ�ʂł���悤�ɂ���
	//�E�p�X���[�h�ύX�_�C�A���O�̐V�p�X���[�h�̓��̓t�B�[���h���Q�ɂ���
	//
	//33
	//�EXF100,XF110�œ����^�t�B�[���h�����������Ƃ��Ďw�肵���ꍇ�̖����C������
	//�E�Z�b�V�����p�����[�^SKIP_PRELOAD�̈����Ɋւ�������C������
	//�EKBCALENDAR�̃��[�U��`�敪���o�^����Ă��Ȃ���΃��O�C���ł��Ȃ��悤�ɂ���
	//�E�Z�b�V�����̂h�o�����Z�b�g����ۂɁATXIPADDRESS�̃t�B�[���h���ɍ��킹�Ēl��Z�k����悤�ɂ���
	//
	//32
	//�EWeb�T�[�r�X�𗘗p���邽�߂̊֐�session.requestWebService(...)��ǉ�����
	//�EXML�f�[�^��JSON�f�[�^���������邽�߂�Session�֐���ǉ�����
	//�EXF110,XF200,XF310�ŋ敪�n���ڂ����X�g�\������Ȃ��P�[�X�������Ă��������C������
	//
	//31
	//�EXF300,XF310�̃L�[���̓_�C�A���O�̃L�[�������W�b�N�����P����
	//�EXF110,XF310�̃v�����v�g���ݒ肳�ꂽ�J�����̕ҏW�ݒ�̎d�l�����P����
	//�EXF110,XF200,XF310�ɕ\����p�̒l���X�g�t�B�[���h�Ƌ敪�t�B�[���h�p�̃R���|�[�l���g�N���X�𓱓�����
	//�ETableOperator��getValueOf(...)�ŕԂ��l��null�ł����''���A�܂���u�����N���������ĕԂ��悤�ɂ���
	//�EXF110�ł̊֘A�@�\�N���`�F�b�N�{�b�N�X�̈ʒu�ݒ胍�W�b�N�����P����
	//�EXF200�ōX�V��p�Ƃ����ꍇ�A�X�V�{�^���Ɏw��̃L���v�V�����l���Z�b�g����悤�ɂ����i�]���͎w��ɂ������"�X�V"���Z�b�g���Ă����j
	//�EXF390�̃X�N���v�g�ϐ��̐ݒ胍�W�b�N�Ɋ܂܂�Ă����o�O���C��
	//�EXF110,XF200,XF310�Ń��X�g�{�b�N�X�̕����䂪�o���Ȃ����������C��
	//�ESKIP_PRELOAD�̃A�v���P�[�V�����p�����[�^�ɑΉ�����
	//�E���O�C������LOGIN_PERMITTED�̃V�X�e���ϐ����o�^����Ă��Ȃ��ꍇ�̓��������P����
	//
	//30
	//�E�Z�b�V�����J�n���ɃZ�b�V�����e�[�u���́u�����n�o�[�W�����v��Driver�̃o�[�W�������L�^����悤�ɂ���
	//�ETableOperator�̃��\�b�h�Ƃ���setDistinctFields(...)��ǉ�����
	//�EInputDialog�̃t�B�[���h�̃��\�b�h�Ƃ���getItemCount(),setFileChooser(...)��ǉ�����
	//�Esession�֐��Ƃ���createTextFileOperator(...),existsFile(...),deleteFile(...),renameFile(...)��ǉ�����
	//�Eoracle�ւ̑Ή��ɂƂ��Ȃ�Select���ݒ菈���Ɋ܂܂�Ă����o�O���C������
	//�EXF100,XF300�̖��׃t�B�[���h�̔�\���I�v�V�����ɑΉ�����
	//�EXF110��XF310�̌��o����̃v�����v�g�t�B�[���h�̕ҏW�ݒ��XF200�ɍ��킹��
	//�EXF110,XF200,XF310,XF390�ɂ��ē����`�����ɘA�����s�����ۂɐ���������C������
	//
	//29
	//�Esession�֐��Ƃ���getMonthlyExchangeRate(currency, date, type)��ǉ�����
	//�E�X�v���b�V����̃��b�Z�[�W�̃~�X���C������
	//�EXF100�̖��׍s����@�\�̋N���Ɏ��s�����ꍇ�̃��b�Z�[�W�\�����������P����
	//�EXF300�̃^�u�ʂ̏����\�����b�Z�[�W�����P����
	//�Eoracle�ւ̑Ή��̂��߂ɁA���׃e�[�u�������ɐ��������Select���𒲐�����
	//
	//28
	//�EXF100,XF110,XF310�œ���@�\�����ɂQ��ڂ��N�������ꍇ�̕s����C������
	//�E�p�l���n�@�\�^�C�v�ŁA�Q��ڂ��N�������ꍇ�̌Œ�Where�̎�荞�݂̕s����C������
	//�EXF100,XF110,XF300�̋敪�n�i���ݏ����̕\���ݒ�܂������P����
	//�Esession.executeProgram(...)�̏I�����b�Z�[�W���_�C�A���O�\��������̂���߂āA�߂�l�ɂ���
	//�EMenu��ł�Escape�L�[�����O�A�E�g�v���ɂ��Ă�����
	//�EXF110,XF310�ł̖��׍s�̃G���[���b�Z�[�W�̃n���h�����O�����P����
	//�EXF110,XF310�Ō����t�B�[���h����������O�ɂ���������������悤�ɂ���
	//�EXF310�Ńu�����N�s��ǉ����Ēl����͂�����̃`�F�b�N�Ńu�����N�`�F�b�N���X�L�b�v���������C��
	//�EXF110�Ńo�b�`�t�B�[���h�𔺂�Ȃ��ꍇ�A�u���ցv�������ƌł܂��Ă��܂��o�O���C������
	//
	//27
	//�E�X�N���v�g�Ői���o�[�𐧌䂷�邽�߂̊֐�session.startProgress(...)��ǉ�����
	//�E���j���[�����u�v�����[�h�w��@�\�v�ɑΉ�����
	//�EXF310�̃L�[���̓_�C�A���O�̃N���[�Y����̃o�O���C��
	//�E�e�[�u���X�N���v�g�́u�ꎞ�ۗ��v�ɑΉ�����
	//�E�摜�t�@�C���t�B�[���h�ɂ��ăt�@�C�������݂��Ȃ��ꍇ�̕\�������P����
	//��摜�t�@�C���t�B�[���h���X�V�Ώۂ̖��׃t�B�[���h�Ƃ��Ēu�����ꍇ�̕\�������P����
	//�E�e��ǉ������ɂ����āA�����̔ԃt�B�[���h�������t�B�[���h�Ƃ��Ċ܂܂�Ă���ƍ̔ԃ��R�[�h�����Ӗ��ɃJ�E���g�A�b�v�����o�O���C��
	//��@�\���œ����select�������s���ꂽ�ꍇ�ɂ̓e�[�u�����샍�O�ɋL�^���Ȃ��悤�ɂ����i���O�̃T�C�Y�����炷���߁j
	//�XF110,310�̖��׍s��ɃJ�[�\��������Ƃ��ɍX�V�{�^���������������悤�ɂ����i���̂܂܍X�V����ƍŉ��s�̒l���������[��Ɏc���Ă��邱�Ƃ����邽�߁j
	//�VARCHAR�����̃e�L�X�g�h���͈�Ƀt�H�[�J�X������ꍇ�AEnter�L�[��Tab�L�[�ɂ��Ă����Ă������A�G���[�`�F�b�N�̃L�[�ɕύX����
	//
	//26
	//�EInputDialog�Ƀ_�C�A���O�̕����w�肷�邽�߂̃��\�b�hsetWidth(...)��g�ݍ���
	//�EInputDialog�̃t�B�[���h��setValue(...)���ꂽ�ꍇ�ɁA�l���t�B�[���h�T�C�Y�ɍ��킹��悤�ɂ���
	//�E�摜���t�B�[���h�̎w��T�C�Y�ɍ��킹�ďk���^�g�傷��悤�ɂ���ƂƂ��ɁA�摜���N���b�N����΃I���W�i���T�C�Y�ŕ\�������悤�ɂ���
	//�EXF390�̖��׍��ڂ����l�̏ꍇ�Ɉُ�I�����邱�Ƃ���������C������
	//�E�N���X�`�F�b�N�̑Ώۃe�[�u����ÓI�������ꂽ���̂Ɍ���悤�ɂ���
	//�E�N���X�`�F�b�N�̏��O�w��ɑΉ�����
	//�E�����L�[�������t�B�[���h�̏ꍇ�ɃN���X�`�F�b�N������ɍ쓮���Ȃ������C��
	//�EXF110�̑S�I��p�`�F�b�N�{�b�N�X���`�F�b�N����ƉE�ׂ̍��ڂł̕��ёւ����N�����Ă����o�O���C��
	//
	//25
	//�E���t�t�B�[���h�����p����J�����_�[�R���|�[�l���g���Z�b�V�������L�I�u�W�F�N�g�ɂ���
	//�E�J�����_�[�R���|�[�l���g�̕\���ʒu�̐��䃍�W�b�N�����P����
	//�E�J�����_�[�R���|�[�l���g���J�����_�[�敪���󂯎���悤�ɂ����i���t�t�B�[���h�̏C���ɂ��Ă͖����j
	//
	//24
	//�EReferChecker��FK�Ɋ܂܂�鐔���t�B�[���h��NULL�ł������ꍇ�ɑΉ�
	//�E�J�����_�[�R���|�[�l���g�ŁA�x���e�[�u���ƃ��[�U��`�敪�́u�J�����_�[�敪�v����������悤�ɂ���
	//�E���t�����n�̂R�̃Z�b�V�����֐��ŁA�x���e�[�u���́u�J�����_�[�敪�v����������悤�ɂ���
	//
	//23
	//�E�}�C�i�X�s�̐��l�t�B�[���h�Ń}�C�i�X���͂��\�ɂȂ邱�Ƃ�����o�O���C��
	//�EXF100,110,300�̈ꗗ���ɃG�C���A�X�w�肳�ꂽ�t�B�[���h���܂߂�ƈُ�I������o�O���C��
	//�EXF310�ŃJ�����T�C�Y��ύX���Ă��A�ҏW�I���s�̃J�����T�C�Y���ω����Ȃ��o�O���C��
	//�E�N���X�`�F�b�J�[�̃��O�C�������[�h�̎d�l��g�ݍ���
	//�E�@�\�̏I�����ɃN���X�`�F�b�J�[�̐����X���b�h���L�����Z������悤�ɂ���
	//�EXF100,110,300�̌��������̃v�����v�g�I�v�V�����u��⃊�X�g�v�ɑΉ�����
	//�E�����e�[�u���́u�Œ�Where�v�ɃZ�b�V���������̑��Ƀ��[�U�������w��ł���悤�ɂ���
	//�EXF390�ŁA�w��ɂ���č��v�l�⍇�v�s�̐ݒ肪���������Ȃ�o�O���C��
	//�EXF000�ŁA���s������LIST����REPEAT�����w��ł���悤�ɂ���
	//
	//22
	//�E���j���[�^�u�̑I�����ɉ����ɕ\������郁�b�Z�[�W���A���쉇���pURL���w�肳��Ă��邩�ǂ����Ő؂�ւ���悤�ɂ���
	//�EXFTextField�̃^�C�v��DATETIME�̏ꍇ�̃t�B�[���h�����L����
	//�EXF310�ł̃u�����N�s�ǉ��̃��W�b�N�����P����
	//�EXF300���疾�׍s��XF200�ŃR�s�[������ňꗗ�����t���b�V������悤�ɂ���
	//�ECheckListDialog�Ŗ߂�{�^�����g���ƃ��X�g���N���A����Ȃ��o�O���C��
	//
	//21
	//�EInputDialog��addField���\�b�h�ɂ����ĕ\����̃f�t�H���g��0�i�㕔�z�u�j�Ƃ���
	//�E�u�Œ�Where�v�ɃZ�b�V����������g�ݍ��߂�悤�ɂ���
	//�ESession�֐�setNextNumber(id, nextNumber)��g�ݍ���
	//�ESession�֐�isValidTime(time, format)��g�ݍ���
	//�ESession�֐�getMinutesBetweenTimes(timeFrom, timeThru)��g�ݍ���
	//�ESession�֐�getOffsetDateTime(date, time, minutes, countType)��g�ݍ���
	//�ESession�֐�getOffsetYearMonth(yearMonth, months)��g�ݍ���
	//�ESession�֐�setSystemVariant(id, value)��g�ݍ���
	//�EURL�^�C�v�̃t�B�[���h�Ƀ��[�J���t�@�C�������w��ł���悤�ɂ���
	//�EXF200��"EDIT"��INSTANCE_MODE��n���ΕҏW���[�h�ŋN�������悤�ɂ���
	//�EXF200,310�ŁA�v�����v�^�֐�����󂯎��t�B�[���h���܂܂�Ȃ��ꍇ�ɔ�\���t�B�[���h�Ƃ��đg�ݍ��ނ悤�ɂ���
	//�EXF310�̍s�ǉ����X�g�ɖ��׍��ڂ��w�肳��Ă��Ȃ��ꍇ�ɂ̓��b�Z�[�W�o�͂��ďI������悤�ɂ���
	//�EXF310�̍s�ǉ����X�g��Where�������w�肳��Ă��Ȃ��P�[�X�ɑΉ�����
	//�EXF310�Œǉ����ꂽ����ɂ̓G���[�\�����Ȃ��悤�ɂ���
	//�EXF100,110,300�ɂ��āA�����\���I�v�V�����ɑΉ�����
	//�EXF100����@�\���N��������̏I���R�[�h�̒l�ɂ��������Ė��׈ꗗ���X�V����悤�ɂ���
	//�EXF100,110,300,310�ɂ��āA�Z���̔z�F�ݒ�����P����
	//�ETableOperator�ŃV���O���N�H�[�e�[�V�������܂ރf�[�^��������悤�ɂ���
	//�ETableOperator�œ��t����=''��addKeyValue�����ꂽ�ꍇ�ɁA������'is NULL'�ɒu��������悤�ɂ���(!=''�̏ꍇ�ɂ�'is not NULL') 
	//�ETableOperator�œ��t����''��addValue�����ꂽ�ꍇ�ɁA������'NULL'�ɒu��������悤�ɂ���
	//�E���������t�B�[���h�ɕ�����OR������ݒ肷�邽�߂ɁAsession.getCheckListDialog()��ǉ�����
	//�ESession�̎����̔ԏ����̃��W�b�N�����P����
	//�EXF110,200,310�ŁA���X�g�{�b�N�X���v�����v�^���ݒ肳��Ă���t�B�[���h�ɂ��āA�����ɂ���Ēl�ݒ肳���t�B�[���h�̂����̈ꕔ���ҏW�s�ł���Ζ����ɂ��Ă������A�������߂��B�ҏW�s�ł����Ă��l���X�V�������P�[�X�����邽�߁B
	//���C�����ꂽ��聄
	//�ESession�̐Ŋz�v�Z�֐��̌������W�b�N�̃o�O���C������
	//�EXF100,110,300�ŁA�J�����ʂ̃\�[�g���w�肵���ꍇ�AEXCEL�o�͂�����ƌ��o����<u>���t������Ă��܂��o�O���C������
	//�E�����t�B�[���h�ɑ΂��Ď����̔Ԃ�ݒ肷���*AUTO���\������Ȃ��o�O���C������
	//�EVARCHAR���ڂ��P�s�\���ɂ����ꍇ�A�X�N���[���o�[���펞�\������Ă����o�O���C������
	//�EXF100,110,200,300,310�ɂ��āA�����t�B�[���h�Ƀv�����v�g�ݒ肵���ۂ̓����Ɋւ���o�O���C������
	//�EXF110,310�ɂ��āA���׍s���̕ҏW�^�s�ݒ肪���������f����Ă��Ȃ������o�O���C������
	//�EXF290,390�ŗ�O���b�Z�[�W���_�C�A���O�\������X�e�b�v�ׂ̍����o�O���C������
	//�EXF310�Łu�l���X�g�t�B�[���h�v�Ɋւ��鈵�������������Ă����o�O���C������
	//�EXF310��A�����s�����ꍇ�ADividerLocation�̈ʒu���Đݒ肳��Ȃ����Ƃ̂���o�O���C������
	//�EXF310�Ō��o����ɓ��͉\���ڂ����݂��Ȃ��ꍇ�A���׍s�̃G���[���ڂɃt�H�[�J�X��������Ȃ��o�O���C��
	//�EXF310�ŐV�K�ǉ����ꂽ���׍s�ɂ��Ẵ��j�[�N����`�F�b�N�Ɋւ���o�O���C������
	//�EXF110�ňꎟ�e�[�u���́u�����e�[�u���ǂݍ��ݑO�E�X�V�O�X�N���v�g�v�̎��s�X�e�b�v�������Ă����o�O���C��
	//�EXF110�Ńo�b�`�e�[�u�������@�\���u�����N�ł����s����Ă��܂��o�O���C��
	//�EXF110�Ō����������[���ɂ���ƕ\�������������Ȃ�o�O���C��
	//�EXF110�Ō��o����̃��X�g�{�b�N�X�ɂ��āA�I�������l���֘A����o�b�`�t�B�[���h�ɔ��f����Ȃ��o�O���C��
	//�EXF100,110�ŔN���^�t�B�[���h�Ō����������w�肷��ƈُ�I������o�O���C������

	/**
	 * Components on dialog
	 */
	private JPanel panel1 = new JPanel();
	private JPanel panel2 = new JPanel();
	private JPanel insetsPanel1 = new JPanel();
	private JPanel insetsPanel2 = new JPanel();
	private JPanel insetsPanel3 = new JPanel();
	private JButton buttonOK = new JButton();
	private JLabel imageLabel = new JLabel();
	private JLabel labelName = new JLabel();
	private JLabel labelVersion = new JLabel();
	private JLabel labelCopyright = new JLabel();
	private JLabel labelURL = new JLabel();
	private ImageIcon imageXead = new ImageIcon();
	private HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
	private Desktop desktop = Desktop.getDesktop();
	private JDialog parent_;
	private String font_;

	public DialogAbout(JDialog parent, String font) {
		super(parent);
		parent_ = parent;
		font_ = font;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception  {
	 	imageXead = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.Session.class.getResource("title.png")));
		imageLabel.setIcon(imageXead);
		panel1.setLayout(new BorderLayout());
		panel1.setBorder(BorderFactory.createEtchedBorder());
		panel2.setLayout(new BorderLayout());
		insetsPanel2.setLayout(new BorderLayout());
		insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		insetsPanel2.setPreferredSize(new Dimension(75, 52));
		insetsPanel2.add(imageLabel, BorderLayout.EAST);

		labelName.setFont(new java.awt.Font(font_, 1, 20));
		labelName.setHorizontalAlignment(SwingConstants.CENTER);
		labelName.setText(PRODUCT_NAME);
		labelName.setBounds(new Rectangle(0, 8, 240, 22));
		labelVersion.setFont(new java.awt.Font(font_, 0, 16));
		labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
		labelVersion.setText(FULL_VERSION);
		labelVersion.setBounds(new Rectangle(0, 32, 240, 20));
		labelCopyright.setFont(new java.awt.Font(font_, 0, 16));
		labelCopyright.setHorizontalAlignment(SwingConstants.CENTER);
		labelCopyright.setText(COPYRIGHT);
		labelCopyright.setBounds(new Rectangle(0, 53, 240, 20));
		labelURL.setFont(new java.awt.Font(font_, 0, 14));
		labelURL.setHorizontalAlignment(SwingConstants.CENTER);
		labelURL.setText("<html><u><font color='blue'>" + URL_DBC);
		labelURL.setBounds(new Rectangle(0, 75, 240, 20));
		labelURL.addMouseListener(new About_labelURL_mouseAdapter(this));
		insetsPanel3.setLayout(null);
		insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
		insetsPanel3.setPreferredSize(new Dimension(250, 80));
		insetsPanel3.add(labelName, null);
		insetsPanel3.add(labelVersion, null);
		insetsPanel3.add(labelCopyright, null);
		insetsPanel3.add(labelURL, null);

		buttonOK.setText("OK");
		buttonOK.setFont(new java.awt.Font(font_, 0, 16));
		buttonOK.addActionListener(this);
		insetsPanel1.add(buttonOK, null);

		panel1.add(insetsPanel1, BorderLayout.SOUTH);
		panel1.add(panel2, BorderLayout.NORTH);
		panel2.setPreferredSize(new Dimension(350, 100));
		panel2.add(insetsPanel2, BorderLayout.CENTER);
		panel2.add(insetsPanel3, BorderLayout.EAST);

		this.setTitle("About X-TEA Driver");
		this.getContentPane().add(panel1, null);
		this.setResizable(false);
	}

	public void request() {
		insetsPanel1.getRootPane().setDefaultButton(buttonOK);
		Dimension dlgSize = this.getPreferredSize();
		Dimension frmSize = parent_.getSize();
		Point loc = parent_.getLocation();
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x + 30, (frmSize.height - dlgSize.height) / 2 + loc.y + 30);
		this.pack();
		super.setVisible(true);
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	void cancel() {
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonOK) {
			cancel();
		}
	}

	void labelURL_mouseClicked(MouseEvent e) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			desktop.browse(new URI(URL_DBC));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "The Site is inaccessible.");
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	void labelURL_mouseEntered(MouseEvent e) {
		setCursor(htmlEditorKit.getLinkCursor());
	}

	void labelURL_mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
}

class About_labelURL_mouseAdapter extends java.awt.event.MouseAdapter {
	DialogAbout adaptee;
	About_labelURL_mouseAdapter(DialogAbout adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.labelURL_mouseClicked(e);
	}
	public void mouseEntered(MouseEvent e) {
		adaptee.labelURL_mouseEntered(e);
	}
	public void mouseExited(MouseEvent e) {
		adaptee.labelURL_mouseExited(e);
	}
}
