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
