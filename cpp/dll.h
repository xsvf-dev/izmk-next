#ifndef IZMK_DLL
#define IZMK_DLL

#include <jni.h>

#define JNI_REG_CLASS "ovo/xsvf/AgentMain"

JNIEXPORT jclass JNICALL defineClass(JNIEnv*, jclass, jstring, jobject, jbyteArray);

static JNINativeMethod gMethods[] = {
	{ (char*)"defineClass", (char*)"(Ljava/lang/String;Ljava/lang/ClassLoader;[B)Ljava/lang/Class;", (void*)defineClass },
};

#endif