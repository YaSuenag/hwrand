#include <jni.h>


/*
 * These functions are defined in random.S
 */
void fill_with_rdrand(unsigned char data[], int len);
void fill_with_rdseed(unsigned char data[], int len);


/*
 * Class:     com_yasuenag_hwrand_x86_internal_JNIHelper
 * Method:    checkCPUFeatures
 * Signature: ()V
 */
JNIEXPORT void JNICALL
      Java_com_yasuenag_hwrand_x86_internal_JNIHelper_checkCPUFeatures
                                                     (JNIEnv *env, jclass cls){
  int ebx, ecx;
  jfieldID rdrandFieldID, rdseedFieldID;
  jclass unsatisfiedLinkErrorCls;

  unsatisfiedLinkErrorCls = (*env)->FindClass(env,
                                       "java/lang/UnsatisfiedLinkError");

  rdrandFieldID = (*env)->GetStaticFieldID(env, cls, "supportedRDRAND", "Z");
  if(rdrandFieldID == NULL){
    (*env)->ThrowNew(env, unsatisfiedLinkErrorCls,
                                "Could not find supportedRDRAND field.");
    return;
  }

  rdseedFieldID = (*env)->GetStaticFieldID(env, cls, "supportedRDSEED", "Z");
  if(rdseedFieldID == NULL){
    (*env)->ThrowNew(env, unsatisfiedLinkErrorCls,
                                "Could not find supportedRDSEED field.");
    return;
  }

  asm volatile ("cpuid" : "=c" (ecx) : "a" (1) : "%ebx", "%edx");
  (*env)->SetStaticBooleanField(env, cls, rdrandFieldID, (ecx >> 30) & 1);

  asm volatile ("cpuid" : "=b" (ebx) : "a" (7), "c" (0) : "%edx");
  (*env)->SetStaticBooleanField(env, cls, rdseedFieldID, (ebx >> 18) & 1);
}

/*
 * Class:     com_yasuenag_hwrand_x86_RdRand
 * Method:    engineNextBytes
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_yasuenag_hwrand_x86_RdRand_engineNextBytes
                                   (JNIEnv *env, jobject obj, jbyteArray bytes){
  jint length;
  jbyte *native_bytes;

  length = (*env)->GetArrayLength(env, bytes);
  native_bytes = (*env)->GetPrimitiveArrayCritical(env, bytes, NULL);
  if(native_bytes == NULL){

    if((*env)->ExceptionCheck(env) == JNI_FALSE){
      jclass runtimeExceptionCls = (*env)->FindClass(env,
                                                "java/lang/RuntimeException");
      (*env)->ThrowNew(env, runtimeExceptionCls,
                                "Could not get native array memory.");
    }

    return;
  }

  fill_with_rdrand(native_bytes, length);

  (*env)->ReleasePrimitiveArrayCritical(env, bytes, native_bytes, 0);
}

/*
 * Class:     com_yasuenag_hwrand_x86_RdSeed
 * Method:    engineNextBytes
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_yasuenag_hwrand_x86_RdSeed_engineNextBytes
                                   (JNIEnv *env, jobject obj, jbyteArray bytes){
  jint length;
  jbyte *native_bytes;

  length = (*env)->GetArrayLength(env, bytes);
  native_bytes = (*env)->GetPrimitiveArrayCritical(env, bytes, NULL);
  if(native_bytes == NULL){

    if((*env)->ExceptionCheck(env) == JNI_FALSE){
      jclass runtimeExceptionCls = (*env)->FindClass(env,
                                                "java/lang/RuntimeException");
      (*env)->ThrowNew(env, runtimeExceptionCls,
                                "Could not get native array memory.");
    }

    return;
  }

  fill_with_rdseed(native_bytes, length);

  (*env)->ReleasePrimitiveArrayCritical(env, bytes, native_bytes, 0);
}

