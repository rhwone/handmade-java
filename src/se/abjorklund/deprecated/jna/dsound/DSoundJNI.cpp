#include <windows.h>
#include <stdint.h>
#include <stdio.h>
#include <stddef.h>
#include <limits.h>
#include <float.h>
#include <math.h>
#include <dsound.h>
#include "se_abjorklund_win32_jna_dsound_DSoundJNI.h"

typedef int8_t Int8;
typedef int16_t Int16;
typedef int32_t Int32;
typedef int64_t Int64;
typedef Int32 Bool32;

typedef uint8_t UInt8;
typedef uint16_t UInt16;
typedef uint32_t UInt32;
typedef uint64_t UInt64;

typedef intptr_t Intptr;
typedef uintptr_t UIntptr;

typedef size_t MemoryIndex;

typedef float Real32;
typedef double Real64;

typedef Int8 S8;
typedef Int16 S16;
typedef Int32 S32;
typedef Int64 S64;
typedef Bool32 B32;

typedef UInt8 U8;
typedef UInt16 U16;
typedef UInt32 U32;
typedef UInt64 U64;

typedef Real32 R32;
typedef Real64 R64;

typedef uintptr_t UMM;

#define internal static
#define local_persist static
#define global_variable static

#define pi32 3.14159265359f

struct Win32SoundOutput
{
    U32 runningSampleIndex;
    U32 wavePeriod;
    U32 halfWavePeriod;
    U32 bytesPerSample;
    U32 secondaryBufferSize;
    U32 toneVolume;
    U32 samplesPerSecond;
    U32 toneHz;
};

global_variable LPDIRECTSOUNDBUFFER globalSecondaryBuffer;
global_variable Win32SoundOutput globalSoundOutput;
global_variable B32 initialized = false;

#define DIRECT_SOUND_CREATE(name) HRESULT WINAPI name(LPCGUID pcGuidDevice, LPDIRECTSOUND *ppDS, LPUNKNOWN pUnkOuter)
typedef DIRECT_SOUND_CREATE(direct_sound_create);

internal void win32FillSoundBuffer(DWORD byteToLock, DWORD bytesToWrite, jshort *samples)
{
    printf("%d\n", byteToLock);
    printf("%d\n", bytesToWrite);

    VOID *region1;
    DWORD region1Size;
    VOID *region2;
    DWORD region2Size;

    if (samples)
    {
        jshort *zero = samples + 0;
        jshort *ten = samples + 10;
        jshort *twentyfive = samples + 25;
        jshort *twohundred = samples + 200;

        printf("%d\n", *zero);
        printf("%d\n", *ten);
        printf("%d\n", *twentyfive);
        printf("%d\n", *twohundred);
    }

    if (SUCCEEDED(globalSecondaryBuffer->Lock(
            byteToLock,
            bytesToWrite,
            &region1, &region1Size,
            &region2, &region2Size,
            0)))
    {
        printf("Buffer locked successfully\n");

        DWORD region1SampleCount = region1Size / globalSoundOutput.bytesPerSample;
        ;
        S16 *sampleOut = (S16 *)region1;
        for (DWORD sampleIndex = 0; sampleIndex < region1SampleCount; ++sampleIndex)
        {
            R32 t = 2.0 * pi32 * (R32)globalSoundOutput.runningSampleIndex / (R32)globalSoundOutput.wavePeriod;
            R32 sineValue = sinf(t);
            S16 sampleValue = (S16)(sineValue * globalSoundOutput.toneVolume);
            *sampleOut++ = sampleValue;
            *sampleOut++ = sampleValue;

            ++globalSoundOutput.runningSampleIndex;
        }

        DWORD region2SampleCount = region2Size / globalSoundOutput.bytesPerSample;
        sampleOut = (S16 *)region2;
        for (DWORD sampleIndex = 0; sampleIndex < region2SampleCount; ++sampleIndex)
        {
            R32 t = 2.0 * pi32 * (R32)globalSoundOutput.runningSampleIndex / (R32)globalSoundOutput.wavePeriod;
            R32 sineValue = sinf(t);
            S16 sampleValue = (S16)(sineValue * globalSoundOutput.toneVolume);

            *sampleOut++ = sampleValue;
            *sampleOut++ = sampleValue;

            ++globalSoundOutput.runningSampleIndex;
        }

        /* for(DWORD sampleIndex = 0; sampleIndex < region1SampleCount; ++sampleIndex)
        {
            short sampleValue = (S16)(javaSamples + (sampleIndex * sizeof(S16)));
            
            *sampleOut++ = sampleValue;
            *sampleOut++ = sampleValue;
            
            ++globalSoundOutput.runningSampleIndex;
            bufferSampleIndex++;
            bufferSampleIndex++;
        }
        
        DWORD region2SampleCount = region2Size/globalSoundOutput.bytesPerSample;
        sampleOut = (S16*)region2;
        for(DWORD sampleIndex = bufferSampleIndex; sampleIndex < region2SampleCount; ++sampleIndex)
        {       
            *sampleOut++ = (S16)*javaSamples + (sampleIndex * sizeof(S16));
            *sampleOut++ = (S16)*javaSamples + (sampleIndex * sizeof(S16));
            
            ++globalSoundOutput.runningSampleIndex;
        } */

        globalSecondaryBuffer->Unlock(region1, region1Size, region2, region2Size);
    }
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_initDSound(JNIEnv *env, jobject thisObject)
{

    globalSoundOutput = {};

    globalSoundOutput.toneHz = 256;
    globalSoundOutput.samplesPerSecond = 48000;
    globalSoundOutput.toneVolume = 3000;
    globalSoundOutput.runningSampleIndex = 0;
    globalSoundOutput.wavePeriod = globalSoundOutput.samplesPerSecond / globalSoundOutput.toneHz;
    globalSoundOutput.bytesPerSample = sizeof(S16) * 2;
    globalSoundOutput.secondaryBufferSize = globalSoundOutput.samplesPerSecond * globalSoundOutput.bytesPerSample;

    HMODULE dSoundLibrary = LoadLibraryA("dsound.dll");
    if (dSoundLibrary)
    {
        printf("dSoundLibrary loaded\n");

        direct_sound_create *directSoundCreate = (direct_sound_create *)GetProcAddress(dSoundLibrary, "DirectSoundCreate");

        LPDIRECTSOUND directSound;
        if (directSoundCreate && SUCCEEDED(directSoundCreate(0, &directSound, 0)))
        {
            printf("DirectSoundCreate succeeded\n");

            WAVEFORMATEX waveFormat = {};
            waveFormat.wFormatTag = WAVE_FORMAT_PCM;
            waveFormat.nChannels = 2;
            waveFormat.nSamplesPerSec = globalSoundOutput.samplesPerSecond;
            waveFormat.wBitsPerSample = 16;
            waveFormat.nBlockAlign = (waveFormat.nChannels * waveFormat.wBitsPerSample) / 8;
            waveFormat.nAvgBytesPerSec = waveFormat.nSamplesPerSec * waveFormat.nBlockAlign;
            waveFormat.cbSize = 0;

            HWND hWnd = GetForegroundWindow();
            if (hWnd == NULL)
            {
                hWnd = GetDesktopWindow();
            }

            if (SUCCEEDED(directSound->SetCooperativeLevel(hWnd, DSSCL_PRIORITY)))
            {
                printf("SetCooperativeLevel succeeded\n");

                DSBUFFERDESC bufferDescription = {};
                bufferDescription.dwSize = sizeof(bufferDescription);
                bufferDescription.dwFlags = DSBCAPS_PRIMARYBUFFER;
                bufferDescription.dwBufferBytes = 0;

                LPDIRECTSOUNDBUFFER primaryBuffer;
                if (SUCCEEDED(directSound->CreateSoundBuffer(&bufferDescription, &primaryBuffer, 0)))
                {
                    printf("Primary buffer created successfully.\n");

                    if (SUCCEEDED(primaryBuffer->SetFormat(&waveFormat)))
                    {
                        printf("SetFormat for primary buffer succeeded\n");
                    }
                    else
                    {
                        //TODO: diagnostics
                    }
                }
                else
                {
                    //TODO: diagnostics
                }
            }
            else
            {
                //TODO: diagnostics
            }

            DSBUFFERDESC bufferDescription = {};
            bufferDescription.dwSize = sizeof(bufferDescription);
            bufferDescription.dwFlags = 0;
            bufferDescription.dwBufferBytes = globalSoundOutput.secondaryBufferSize;
            bufferDescription.lpwfxFormat = &waveFormat;
            HRESULT error = directSound->CreateSoundBuffer(&bufferDescription, &globalSecondaryBuffer, 0);
            if (SUCCEEDED(error))
            {
                printf("Secondary buffer created successfully.\n");
            }
        }
        else
        {
            //TODO: diagnostics
        }
    }
    else
    {
        //TODO: diagnostics
    }
    initialized = true;
    jshort *samples = {};
    win32FillSoundBuffer(0, globalSoundOutput.secondaryBufferSize, samples);
    globalSecondaryBuffer->Play(0, 0, DSBPLAY_LOOPING);
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_startPlayingBuffer(JNIEnv *env, jobject thisObject)
{
    globalSecondaryBuffer->Play(0, 0, DSBPLAY_LOOPING);
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_playSound(JNIEnv *env, jobject thisObject, jshortArray javaSoundBuffer)
{
    jboolean isCopy = 0;
    jshort *samples = env->GetShortArrayElements(javaSoundBuffer, &isCopy);

    DWORD playCursor;
    DWORD writeCursor;

    if (SUCCEEDED(globalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor)))
    {
        printf("GetCurrentPosition succeeded\n");
        printf("");

        DWORD byteToLock = (globalSoundOutput.runningSampleIndex * globalSoundOutput.bytesPerSample) % globalSoundOutput.secondaryBufferSize;
        DWORD bytesToWrite;
        if (byteToLock == playCursor)
        {
            bytesToWrite = 0;
        }
        else if (byteToLock > playCursor)
        {
            bytesToWrite = (globalSoundOutput.secondaryBufferSize - byteToLock);
            bytesToWrite += playCursor;
        }
        else
        {
            bytesToWrite = playCursor - byteToLock;
        }

        win32FillSoundBuffer(byteToLock, bytesToWrite, samples);
    }
}

/*
     * Class:     se_abjorklund_win32_jna_dsound_DSoundJNI
     * Method:    getSoundBufferByteInfo
     * Signature: ()Lse/abjorklund/win32/jna/dsound/DSoundJNI/DSoundByteInfo;
     */
JNIEXPORT jobject JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_getSoundBufferByteInfo(JNIEnv *env, jobject thisObject)
{
    DWORD playCursor;
    DWORD writeCursor;

    if (SUCCEEDED(globalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor)))
    {
        printf("GetCurrentPosition succeeded\n");
        printf("");

        DWORD byteToLock = (globalSoundOutput.runningSampleIndex * globalSoundOutput.bytesPerSample) % globalSoundOutput.secondaryBufferSize;
        DWORD bytesToWrite;
        if (byteToLock == playCursor)
        {
            bytesToWrite = 0;
        }
        else if (byteToLock > playCursor)
        {
            bytesToWrite = (globalSoundOutput.secondaryBufferSize - byteToLock);
            bytesToWrite += playCursor;
        }
        else
        {
            bytesToWrite = playCursor - byteToLock;
        }

        win32FillSoundBuffer(byteToLock, bytesToWrite, 0);
    }

    return 0;
}

/*
     * Class:     se_abjorklund_win32_jna_dsound_DSoundJNI
     * Method:    getCurrentPosition
     * Signature: ()Lse/abjorklund/win32/jna/dsound/DSoundJNI/DSoundCursorInfo;
     */
JNIEXPORT jobject JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_getCurrentPosition(JNIEnv *env, jobject thisObject)
{
    jclass javaLocalClass = env->FindClass("se/abjorklund/win32/jna/dsound/DSoundJNI$DSoundCursorInfo");

    if (javaLocalClass == NULL)
    {
        printf("Find Class Failed.\n");
    }
    else
    {
        printf("Found class.\n");
    }

    jclass javaGlobalClass = reinterpret_cast<jclass>(env->NewGlobalRef(javaLocalClass));

    jmethodID javaConstructor = env->GetMethodID(javaGlobalClass, "<init>", "(Lse/abjorklund/win32/jna/dsound/DSoundJNI;III)V");

    if (javaConstructor == NULL)
    {
        printf("Find method Failed.\n");
    }
    else
    {
        printf("Found method.\n");
    }
    DWORD pCursor = {};
    DWORD wCursor = {};
    HRESULT hResult = globalSecondaryBuffer->GetCurrentPosition(&pCursor, &wCursor);

    printf("pCursor: %d\n", pCursor);
    printf("wCursor: %d\n", wCursor);

    jobject result = env->NewObject(javaLocalClass, javaConstructor, thisObject, hResult, pCursor, wCursor);
    return result;
}

/*
     * Class:     se_abjorklund_win32_jna_dsound_DSoundJNI
     * Method:    getGlobalSoundOutput
     * Signature: ()Lse/abjorklund/win32/jna/dsound/DSoundJNI/DSoundGlobalSoundOutput;
     */
JNIEXPORT jobject JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_getGlobalSoundOutput(JNIEnv *env, jobject thisObject)
{
    jobject result = 0;
    if (initialized)
    {

        jclass javaLocalClass = env->FindClass("se/abjorklund/win32/jna/dsound/DSoundJNI$DSoundGlobalSoundOutput");

        if (javaLocalClass == NULL)
        {
            printf("Find Class Failed.\n");
        }
        else
        {
            printf("Found class.\n");
        }

        jclass javaGlobalClass = reinterpret_cast<jclass>(env->NewGlobalRef(javaLocalClass));

        jmethodID javaConstructor = env->GetMethodID(javaGlobalClass, "<init>", "(Lse/abjorklund/win32/jna/dsound/DSoundJNI;IIIIIII)V");

        if (javaConstructor == NULL)
        {
            printf("Find method Failed.\n");
        }
        else
        {
            printf("Found method.\n");
        }

        result = env->NewObject(javaLocalClass, javaConstructor, thisObject, globalSoundOutput.toneHz, globalSoundOutput.samplesPerSecond, globalSoundOutput.toneVolume, globalSoundOutput.runningSampleIndex, globalSoundOutput.wavePeriod, globalSoundOutput.bytesPerSample, globalSoundOutput.secondaryBufferSize);
    }
    else
    {
        printf("DSoundJNI not initialized.");
    }

    return result;
}