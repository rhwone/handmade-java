#include <windows.h>
#include <stdio.h>
#include <malloc.h>

#include "se_abjorklund_win32_platform_JNIPlatform.h"

typedef BOOL platform_loop(JNIEnv *env, jobject thisObj);

static platform_loop *platformLoop;

JNIEXPORT void JNICALL Java_se_abjorklund_win32_platform_JNIPlatform_start(JNIEnv *env, jobject thisObj)
{

    HMODULE platformDLL = LoadLibraryA("D:\\dev\\handmade-java\\src\\se\\abjorklund\\win32\\platform\\win32platform.dll");

    if (platformDLL)
    {
        printf("Platform DLL loaded successfully\n");
    }
    
    platformLoop = (platform_loop *)GetProcAddress(platformDLL, "startPlatformLoop");

    printf("If DLL Main\n");
    if (platformLoop)
    {
        printf("DLLMain found\n");
        platformLoop(env, thisObj);
    }

    else
    {
        DWORD lastError = GetLastError();
        printf("error %d \n", lastError);
    }
}
