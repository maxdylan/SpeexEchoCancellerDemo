#include <jni.h>

#include <string.h>

#include "include/speex/speex_bits.h"
#include "include/speex/speex_echo.h"
#include "include/speex/speex_preprocess.h"
#include "include/speex/speex.h"

static int codec_open = 0;

static int dec_frame_size;
static int enc_frame_size;

static int nInitSuccessFlag = 0;
static int m_nFrameSize = 0;
static int m_nFilterLen = 0;
static int m_nSampleRate = 0;
static int nSampleTimeLong = 0;
static SpeexEchoState *m_pState;
static SpeexPreprocessState *m_pPreprocessorState;
static int iArg = 0;

static SpeexBits ebits, dbits;
void *enc_state;
void *dec_state;

//static JavaVM *gJavaVM;

extern "C"
JNIEXPORT jint JNICALL Java_com_xintu_speexechocanceller_Speex_open
        (JNIEnv *env, jobject obj, jint compression) {
    int tmp;

    if (codec_open++ != 0)
        return (jint) 0;

    speex_bits_init(&ebits);
    speex_bits_init(&dbits);

    enc_state = speex_encoder_init(&speex_nb_mode);
    dec_state = speex_decoder_init(&speex_nb_mode);
    tmp = compression;
    speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
    speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);
    speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);

    return (jint) 0;
}

extern "C"
JNIEXPORT jint Java_com_xintu_speexechocanceller_Speex_encode
        (JNIEnv *env, jobject obj, jshortArray lin, jint offset,
         jbyteArray encoded, jint size, jshortArray echo,
         jboolean doEchoCancel) {

    jshort buffer[enc_frame_size];
    jshort echoBuffer[enc_frame_size];
    jshort doEchoBuffer[enc_frame_size];
    jbyte output_buffer[enc_frame_size];
    int nsamples = (size - 1) / enc_frame_size + 1;
    int i, tot_bytes = 0;
    unsigned char doEcho = doEchoCancel;

    if (!codec_open)
        return 0;

    speex_bits_reset(&ebits);

    for (i = 0; i < nsamples; i++) {
        env->GetShortArrayRegion(lin, offset + i * enc_frame_size,
                                 enc_frame_size, buffer);
        if (doEcho) {
            env->GetShortArrayRegion(echo, 0, enc_frame_size, echoBuffer);
            speex_echo_cancellation(m_pState, buffer, echoBuffer, doEchoBuffer);
            speex_preprocess_run(m_pPreprocessorState, doEchoBuffer);
            speex_encode_int(enc_state, doEchoBuffer, &ebits);
        } else {
            speex_preprocess_run(m_pPreprocessorState, buffer);
            speex_encode_int(enc_state, buffer, &ebits);
        }
    }
    //env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
    //speex_encode_int(enc_state, buffer, &ebits);

    tot_bytes = speex_bits_write(&ebits, (char *) output_buffer,
                                 enc_frame_size);
    env->SetByteArrayRegion(encoded, 0, tot_bytes,
                            output_buffer);

    return (jint) tot_bytes;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_xintu_speexechocanceller_Speex_decode
        (JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin,
         jint size) {

    jbyte buffer[dec_frame_size];
    jshort output_buffer[dec_frame_size];
    jsize encoded_length = size;

    if (!codec_open)
        return 0;

    env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
    speex_bits_read_from(&dbits, (char *) buffer, encoded_length);
    speex_decode_int(dec_state, &dbits, output_buffer);
    env->SetShortArrayRegion(lin, 0, dec_frame_size,
                             output_buffer);

    return (jint) dec_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_xintu_speexechocanceller_Speex_getFrameSize
        (JNIEnv *env, jobject obj) {

    if (!codec_open)
        return 0;
    return (jint) enc_frame_size;

}

extern "C"
JNIEXPORT void JNICALL Java_com_xintu_speexechocanceller_Speex_close
        (JNIEnv *env, jobject obj) {

    if (--codec_open != 0)
        return;

    speex_bits_destroy(&ebits);
    speex_bits_destroy(&dbits);
    speex_decoder_destroy(dec_state);
    speex_encoder_destroy(enc_state);
}

//初始化回音消除参数
/*
 * jint frame_size        帧长      一般都是  80,160,320
 * jint filter_length     尾长      一般都是  80*25 ,160*25 ,320*25
 * jint sampling_rate     采样频率  一般都是  8000，16000，32000
 * 比如初始化
 *  InitAudioAEC(80, 80*25,8000)   //8K，10毫秒采样一次
 *  InitAudioAEC(160,160*25,16000) //16K，10毫秒采样一次
 *  InitAudioAEC(320,320*25,32000) //32K，10毫秒采样一次
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_xintu_speexechocanceller_Speex_initSpeexAec(JNIEnv *env, jobject thiz,
                                                     jint frame_size,
                                                     jint filter_length,
                                                     jint sampling_rate) {
    if (nInitSuccessFlag == 1)
        return 1;

    m_nFrameSize = frame_size;
    m_nFilterLen = filter_length;
    m_nSampleRate = sampling_rate;

//计算采样时长，即是10毫秒，还是20毫秒，还是30毫秒
    nSampleTimeLong = (frame_size / (sampling_rate / 100)) * 10;

    m_pState = speex_echo_state_init(m_nFrameSize, m_nFilterLen);
    if (m_pState == NULL)
        return -1;

    m_pPreprocessorState = speex_preprocess_state_init(m_nFrameSize,
                                                       m_nSampleRate);
    if (m_pPreprocessorState == NULL)
        return -2;

    iArg = m_nSampleRate;
    speex_echo_ctl(m_pState, SPEEX_SET_SAMPLING_RATE, &iArg);
    speex_preprocess_ctl(m_pPreprocessorState, SPEEX_PREPROCESS_SET_ECHO_STATE,
                         m_pState);

    nInitSuccessFlag = 1;

    return 1;
}

//执行回音消除
/*
 参数：
 jbyteArray recordArray  录音数据
 jbyteArray playArray    放音数据
 jbyteArray szOutArray
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_xintu_speexechocanceller_Speex_speexAec(JNIEnv *env, jobject thiz,
                                                 jbyteArray recordArray,
                                                 jbyteArray playArray,
                                                 jbyteArray szOutArray) {
    if (nInitSuccessFlag == 0)
        return 0;

    jbyte *recordBuffer = env->GetByteArrayElements(
            recordArray, 0);
    jbyte *playBuffer = env->GetByteArrayElements(playArray, 0);
    jbyte *szOutBuffer = env->GetByteArrayElements(szOutArray, 0);

    speex_echo_cancellation(m_pState, (spx_int16_t *) recordBuffer,
                            (spx_int16_t *) playBuffer,
                            (spx_int16_t *) szOutBuffer);
    int flag = speex_preprocess_run(m_pPreprocessorState,
                                    (spx_int16_t *) szOutBuffer);

    env->ReleaseByteArrayElements(recordArray, recordBuffer, 0);
    env->ReleaseByteArrayElements(playArray, playBuffer, 0);
    env->ReleaseByteArrayElements(szOutArray, szOutBuffer, 0);

    return 1;
}

//退出
extern "C"
JNIEXPORT jint JNICALL Java_com_xintu_speexechocanceller_Speex_exitSpeexDsp(
        JNIEnv *env, jobject thiz) {
    if (nInitSuccessFlag == 0)
        return 0;

    if (m_pState != NULL) {
        speex_echo_state_destroy(m_pState);
        m_pState = NULL;
    }
    if (m_pPreprocessorState != NULL) {
        speex_preprocess_state_destroy(m_pPreprocessorState);
        m_pPreprocessorState = NULL;
    }

    nInitSuccessFlag = 0;

    return 1;
}
