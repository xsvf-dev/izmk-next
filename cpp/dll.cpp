#include "dll.h"

static int registerNativeMethods(JNIEnv* env, const char* className,
	JNINativeMethod* gMethods, int numMethods)
{
	jclass clazz;
	clazz = env->FindClass(className);
	if (clazz == NULL) {
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}


static int registerNatives(JNIEnv* env)
{
	if (!registerNativeMethods(env, JNI_REG_CLASS, gMethods,
		sizeof(gMethods) / sizeof(gMethods[0])))
		return JNI_FALSE;

	return JNI_TRUE;
}


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_4) != JNI_OK) {
		return -1;
	}

	if (!registerNatives(env)) {
		return -1;
	}

	return JNI_VERSION_1_4;
}

JNIEXPORT jclass JNICALL defineClass(JNIEnv* env, jclass object, jstring name, jobject classLoader, jbyteArray data) {
	return env->DefineClass(env->GetStringUTFChars(name, NULL), classLoader, 
		env->GetByteArrayElements(data, NULL), env->GetArrayLength(data));
}
