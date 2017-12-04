#include <jni.h>
#include <u.h>
#include <libc.h>
#include <draw.h>
#include <memdraw.h>

void absmousetrack(int, int, int, ulong);
ulong ticks(void);
int dt_main(int, char**);
int screenWidth = 800;
int screenHeight = 1232;
Point mousept = {0, 0};
int buttons = 0;
unsigned char* screenData();

JNIEXPORT void JNICALL
Java_org_echoline_drawterm_MainActivity_setPass(
        JNIEnv *env,
        jobject obj,
        jstring str) {
    setenv("PASS", (char*)(*env)->GetStringUTFChars(env, str, 0), 1);
}

JNIEXPORT void JNICALL
Java_org_echoline_drawterm_MainActivity_setWidth(
        JNIEnv *env,
        jobject obj,
        jint width) {
    screenWidth = width;
}

JNIEXPORT void JNICALL
Java_org_echoline_drawterm_MainActivity_setHeight(
        JNIEnv *env,
        jobject obj,
        jint height) {
    screenHeight = height;
}

JNIEXPORT jbyteArray JNICALL
Java_org_echoline_drawterm_MainActivity_getScreenData(
        JNIEnv *env,
        jobject obj) {
    jbyteArray buf = (*env)->NewByteArray(env, screenHeight * screenWidth * 4);
    (*env)->SetByteArrayRegion(env, buf, 0, screenWidth * screenHeight * 4, (jbyte*)screenData());
    return buf;
}

JNIEXPORT jint JNICALL
Java_org_echoline_drawterm_MainActivity_dtmain(
        JNIEnv *env,
        jobject obj,
        jobjectArray argv) {
    int i, ret;
    jboolean isCopy;
    char **args = (char **) malloc((*env)->GetArrayLength(env, argv) * sizeof(char *));

    for (i = 0; i < (*env)->GetArrayLength(env, argv); i++) {
        jobject str = (jobject) (*env)->GetObjectArrayElement(env, argv, i);
        args[i] = strdup((char*)(*env)->GetStringUTFChars(env, (jstring)str, 0));
    }
    args[i] = NULL;

    ret = dt_main(i, args);

    for (i = 0; args[i] != NULL; i++) {
        free(args[i]);
    }
    free(args);

    return ret;
}

JNIEXPORT void JNICALL
Java_org_echoline_drawterm_MainActivity_setMouse(
        JNIEnv *env,
        jobject obj,
        jintArray args) {
    jboolean isCopy;
    jint *data;
    if ((*env)->GetArrayLength(env, args) < 3)
        return;
    data = (*env)->GetIntArrayElements(env, args, &isCopy);
    mousept.x = data[0];
    mousept.y = data[1];
    buttons = data[2];
    (*env)->ReleaseIntArrayElements(env, args, data, 0);
    absmousetrack(mousept.x, mousept.y, buttons, ticks());
}
