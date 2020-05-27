#include <windows.h>
#include <stdint.h>
#include <stdio.h>
#include <stddef.h>
#include <limits.h>
#include <float.h>
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

global_variable LPDIRECTSOUNDBUFFER globalSecondaryBuffer;
global_variable U32 runningSampleIndex;
global_variable U32 squareWavePeriod;
global_variable U32 halfSquareWavePeriod;
global_variable U32 bytesPerSample;
global_variable U32 secondaryBufferSize;
global_variable U32 toneVolume;
global_variable B32 soundIsPlaying;
global_variable HWND *windowHandle;
global_variable U32 samplesPerSecond;
global_variable U32 toneHz;

#define DIRECT_SOUND_CREATE(name) HRESULT WINAPI name(LPCGUID pcGuidDevice, LPDIRECTSOUND *ppDS,  LPUNKNOWN pUnkOuter)
typedef DIRECT_SOUND_CREATE(direct_sound_create);


JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_initDSound
(JNIEnv *env, jobject thisObject)
{
    toneHz = 256;
    samplesPerSecond = 48000;
    toneVolume = 3000;
    runningSampleIndex = 0;
    squareWavePeriod = samplesPerSecond/toneHz;
    halfSquareWavePeriod = squareWavePeriod / 2;
    bytesPerSample = sizeof(S16)*2;
    secondaryBufferSize = samplesPerSecond * bytesPerSample;
    
    soundIsPlaying = false;
    
    HMODULE dSoundLibrary = LoadLibraryA("dsound.dll");
    if(dSoundLibrary)
    {
        printf("dSoundLibrary loaded\n");
        
        direct_sound_create *directSoundCreate = (direct_sound_create*) GetProcAddress(dSoundLibrary, "DirectSoundCreate");
        
        LPDIRECTSOUND directSound;
        if(directSoundCreate && SUCCEEDED(directSoundCreate(0, &directSound, 0)))
        
        {
            printf("DirectSoundCreate succeeded\n");
            
            WAVEFORMATEX waveFormat = {};
            waveFormat.wFormatTag = WAVE_FORMAT_PCM;
            waveFormat.nChannels = 2;
            waveFormat.nSamplesPerSec = samplesPerSecond;
            waveFormat.wBitsPerSample = 16;
            waveFormat.nBlockAlign = (waveFormat.nChannels * waveFormat.wBitsPerSample) / 8;
            waveFormat.nAvgBytesPerSec = waveFormat.nSamplesPerSec*waveFormat.nBlockAlign;
            waveFormat.cbSize = 0;
            
            HWND hWnd = GetForegroundWindow();
            if (hWnd == NULL)
            {
                hWnd = GetDesktopWindow();
            }
            
            if(SUCCEEDED(directSound->SetCooperativeLevel(hWnd, DSSCL_PRIORITY)))
            {
                printf("SetCooperativeLevel succeeded\n");
                
                
                DSBUFFERDESC bufferDescription = {};
                bufferDescription.dwSize = sizeof(bufferDescription);
                bufferDescription.dwFlags = DSBCAPS_PRIMARYBUFFER;
                bufferDescription.dwBufferBytes = 0;
                
                
                LPDIRECTSOUNDBUFFER primaryBuffer;
                if(SUCCEEDED(directSound->CreateSoundBuffer(&bufferDescription, &primaryBuffer, 0))) 
                {
                    printf("Primary buffer created successfully.\n");
                    
                    if(SUCCEEDED(primaryBuffer->SetFormat(&waveFormat)))
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
            bufferDescription.dwBufferBytes = secondaryBufferSize;
            bufferDescription.lpwfxFormat = &waveFormat;
            HRESULT error = directSound->CreateSoundBuffer(&bufferDescription, &globalSecondaryBuffer, 0);
            if(SUCCEEDED(error))
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
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_startPlayingBuffer
(JNIEnv *env, jobject thisObject)
{
    globalSecondaryBuffer->Play(0, 0, DSBPLAY_LOOPING);
}


JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_playSquareWave(JNIEnv *env, jobject thisObject)
{
    printf("squareWavePeriod: %d\n", squareWavePeriod);
    printf("runningSampleIndex: %d\n", runningSampleIndex);
    printf("secondaryBufferSize: %d\n", secondaryBufferSize);
    
    DWORD playCursor;
    DWORD writeCursor;
    
    //printf("playSquareWave called\n");
    
    if(SUCCEEDED(globalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor)))
    {
        printf("GetCurrentPosition succeeded\n");
        printf("");
        
        DWORD byteToLock = runningSampleIndex*bytesPerSample % secondaryBufferSize;
        DWORD bytesToWrite;
        if(byteToLock == playCursor)
        {
            bytesToWrite = secondaryBufferSize;
        }
        else if(byteToLock > playCursor)
        {
            bytesToWrite = (secondaryBufferSize - byteToLock);
            bytesToWrite += playCursor;
        }
        else {
            bytesToWrite = playCursor - byteToLock;
        }
        
        VOID *region1;
        DWORD region1Size;
        VOID *region2;
        DWORD region2Size;
        
        if(SUCCEEDED(globalSecondaryBuffer->Lock(
            byteToLock,
            bytesToWrite,
            &region1, &region1Size,
            &region2, &region2Size,
            0)))
        {
            printf("Buffer locked successfully\n");
            
            
            DWORD region1SampleCount = region1Size/bytesPerSample;;
            S16 *sampleOut = (S16*)region1;
            for(DWORD sampleIndex = 0; sampleIndex < region1SampleCount; ++sampleIndex)
            {
                //printf("*sampleOut: %p\n", sampleOut);
                S16 sampleValue = ((runningSampleIndex++ / halfSquareWavePeriod) % 2) ? toneVolume : -toneVolume;
                *sampleOut++ = sampleValue;
                *sampleOut++ = sampleValue;
                //printf("sampleValue: %d\n", sampleValue);
            }
            
            DWORD region2SampleCount = region2Size/bytesPerSample;
            sampleOut = (S16*)region2;
            for(DWORD sampleIndex = 0; sampleIndex < region2SampleCount; ++sampleIndex)
            {
                S16 sampleValue = ((runningSampleIndex++ / halfSquareWavePeriod) % 2) ? toneVolume : -toneVolume;
                *sampleOut++ = sampleValue;
                *sampleOut++ = sampleValue;
            }
            
            globalSecondaryBuffer->Unlock(region1, region1Size, region2, region2Size);
            
        }
    }
    
    printf("soundIsPlaying: %d\n", soundIsPlaying);
    
    if(!soundIsPlaying)
    {
        printf("Playing sound\n");
        globalSecondaryBuffer->Play(0, 0, DSBPLAY_LOOPING);
        soundIsPlaying = true;
    }
    
}



