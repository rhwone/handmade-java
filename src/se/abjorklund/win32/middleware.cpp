#include <windows.h>
#include <stdio.h>
#include <malloc.h>

#include "se_abjorklund_win32_JNIPlatform.h"

typedef BOOL platform_loop(JNIEnv *env, jobject thisObj, void *buffer, jint screenWidth, jint screenHeight);

static platform_loop *platformLoop;

JNIEXPORT void JNICALL Java_se_abjorklund_win32_JNIPlatform_start(JNIEnv *env, jobject thisObj, jobject videoBuffer, jint screenWidth, jint screenHeight)
{

    HMODULE platformDLL = LoadLibraryA("D:\\dev\\handmade-java\\src\\se\\abjorklund\\win32\\win32platform.dll");

    if (platformDLL)
    {
        printf("Platform DLL loaded successfully\n");

        platformLoop = (platform_loop *)GetProcAddress(platformDLL, "startPlatformLoop");

        void *buffer = env->GetDirectBufferAddress(videoBuffer);
        if(buffer){
            printf("buffer found\n");
        }
        printf("If DLL Main\n");
        if (platformLoop)
        {
            printf("DLLMain found\n");
            platformLoop(env, thisObj, buffer, screenWidth, screenHeight);
        }

        else
        {
            DWORD lastError = GetLastError();
            printf("error %d \n", lastError);
        }
    }
}
