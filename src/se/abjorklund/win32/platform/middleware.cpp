/* ========================================================================
   $File: $
   $Date: $
   $Revision: $
   $Creator: Casey Muratori $
   $Notice: (C) Copyright 2014 by Molly Rocket, Inc. All Rights Reserved. $
   ======================================================================== */

/*
  TODO(casey):  THIS IS NOT A FINAL PLATFORM LAYER!!!

  - Saved game locations
  - Getting a handle to our own executable file
  - Asset loading path
  - Threading (launch a thread)
  - Raw Input (support for multiple keyboards)
  - Sleep/timeBeginPeriod
  - ClipCursor() (for multimonitor support)
  - Fullscreen support
  - WM_SETCURSOR (control cursor visibility)
  - QueryCancelAutoplay
  - WM_ACTIVATEAPP (for when we are not the active application)
  - Blit speed improvements (BitBlt)
  - Hardware acceleration (OpenGL or Direct3D or BOTH??)
  - GetKeyboardLayout (for French keyboards, international WASD support)

  Just a partial list of stuff!!
*/

#include <windows.h>
#include <stdio.h>
#include <malloc.h>

#include "se_abjorklund_win32_platform_JNIPlatform.h"

typedef BOOL test_func();

static test_func *testFunc;

JNIEXPORT void JNICALL Java_se_abjorklund_win32_platform_JNIPlatform_start(JNIEnv *env, jobject thisObj)
{

    HMODULE platformDLL = LoadLibraryA("D:\\dev\\handmade-java\\src\\se\\abjorklund\\win32\\platform\\win32platform.dll");

    if (platformDLL)
    {
        printf("Platform DLL loaded successfully\n");
    }
    
    testFunc = (test_func *)GetProcAddress(platformDLL, "test");

    printf("If DLL Main\n");
    if (testFunc)
    {
        printf("DLLMain found\n");
        testFunc();
    }

    else
    {
        DWORD lastError = GetLastError();
        printf("error %d \n", lastError);
    }
}
