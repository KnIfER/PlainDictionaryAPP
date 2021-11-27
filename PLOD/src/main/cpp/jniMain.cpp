#include "com_knziha_plod_plaindict_MainActivityUIBase.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

extern "C" {
jstring Java_com_knziha_plod_plaindict_MainActivityUIBase_getString(JNIEnv* env, jobject)
{
    return env->NewStringUTF("testZlib!!!");
}

jboolean Java_com_knziha_plod_plaindict_MainActivityUIBase_testPakVal(JNIEnv* env, jobject, jstring pn)
{
    return !strncmp((env)->GetStringUTFChars(pn, 0), "com.knziha.plod.plaindict", 25);
}

jint Java_com_knziha_plod_plaindict_MainActivityUIBase_getPseudoCode(JNIEnv *, jobject, jint input) {
    // 1721624788 -> 31
    if(input%73==0xf&&input%101==0x63) {
        return 1721624788%64+0xb;
    }
    return input%0xf;
}

}

