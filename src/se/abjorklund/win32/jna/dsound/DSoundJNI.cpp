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

#define DIRECT_SOUND_CREATE(name) HRESULT WINAPI name(LPCGUID pcGuidDevice, LPDIRECTSOUND *ppDS,   LPUNKNOWN pUnkOuter)
typedef DIRECT_SOUND_CREATE(direct_sound_create);


internal void win32FillSoundBuffer(DWORD byteToLock, DWORD bytesToWrite, jshort *samples)
{
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
        
        
        DWORD region1SampleCount = region1Size/globalSoundOutput.bytesPerSample;;
        S16 *sampleOut = (S16*)region1;
        for(DWORD sampleIndex = 0; sampleIndex < region1SampleCount; ++sampleIndex)
        {
            R32 t = 2.0*pi32*(R32)globalSoundOutput.runningSampleIndex / (R32)globalSoundOutput.wavePeriod;
            R32 sineValue = sinf(t);
            S16 sampleValue = (S16) (sineValue * globalSoundOutput.toneVolume);
            *sampleOut++ = sampleValue;
            *sampleOut++ = sampleValue;
            
            ++globalSoundOutput.runningSampleIndex;
        }
        
        DWORD region2SampleCount = region2Size/globalSoundOutput.bytesPerSample;
        sampleOut = (S16*)region2;
        for(DWORD sampleIndex = 0; sampleIndex < region2SampleCount; ++sampleIndex)
        {
            R32 t = 2.0*pi32*(R32)globalSoundOutput.runningSampleIndex / (R32)globalSoundOutput.wavePeriod;
            R32 sineValue = sinf(t);
            S16 sampleValue = (S16) (sineValue * globalSoundOutput.toneVolume);
            
            *sampleOut++ = sampleValue;
            *sampleOut++ = sampleValue;
            
            ++globalSoundOutput.runningSampleIndex;
        }
        
        globalSecondaryBuffer->Unlock(region1, region1Size, region2, region2Size);
        
    }
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_initDSound
(JNIEnv *env, jobject thisObject)
{
    
    globalSoundOutput = {};
    
    globalSoundOutput.toneHz = 256;
    globalSoundOutput.samplesPerSecond = 48000;
    globalSoundOutput.toneVolume = 3000;
    globalSoundOutput.runningSampleIndex = 0;
    globalSoundOutput.wavePeriod = globalSoundOutput.samplesPerSecond/globalSoundOutput.toneHz;
    globalSoundOutput.bytesPerSample = sizeof(S16)*2;
    globalSoundOutput.secondaryBufferSize = globalSoundOutput.samplesPerSecond * globalSoundOutput.bytesPerSample;
    
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
            waveFormat.nSamplesPerSec = globalSoundOutput.samplesPerSecond;
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
            bufferDescription.dwBufferBytes = globalSoundOutput.secondaryBufferSize;
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
    
    jshort *samples = {};
    win32FillSoundBuffer(0, globalSoundOutput.secondaryBufferSize, samples);
    globalSecondaryBuffer->Play(0, 0, DSBPLAY_LOOPING);
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_startPlayingBuffer
(JNIEnv *env, jobject thisObject)
{
    globalSecondaryBuffer->Play(0, 0, DSBPLAY_LOOPING);
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_dsound_DSoundJNI_playSound(JNIEnv *env, jobject thisObject, jshortArray javaSoundBuffer)
{
    jboolean isCopy = 0;
    jshort *samples = env->GetShortArrayElements(javaSoundBuffer, &isCopy);
    
    DWORD playCursor;
    DWORD writeCursor;
    
    jshort *zero = samples + 0;
    jshort *ten = samples + 10;
    jshort *twentyfive = samples + 25;
    jshort *twohundred = samples + 200;
    
    printf("%d\n", *zero);
    printf("%d\n", *ten);
    printf("%d\n", *twentyfive);
    printf("%d\n", *twohundred);
    
    
    //printf("playSquareWave called\n");
    
    if(SUCCEEDED(globalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor)))
    {
        printf("GetCurrentPosition succeeded\n");
        printf("");
        
        DWORD byteToLock = (globalSoundOutput.runningSampleIndex*globalSoundOutput.bytesPerSample) % globalSoundOutput.secondaryBufferSize;
        DWORD bytesToWrite;
        if(byteToLock == playCursor)
        {
            bytesToWrite = 0;
        }
        else if(byteToLock > playCursor)
        {
            bytesToWrite = (globalSoundOutput.secondaryBufferSize - byteToLock);
            bytesToWrite += playCursor;
        }
        else {
            bytesToWrite = playCursor - byteToLock;
        }
        
        win32FillSoundBuffer(byteToLock, bytesToWrite, samples);
    }
}
#if 0
internal void createSineWave()
{
    DWORD byteToLock = 0;
    DWORD targetCursor = 0;
    DWORD bytesToWrite = 0;
    DWORD playCursor = 0;
    DWORD writeCursor = 0;
    B32 soundIsValid = false;
    // TODO(casey): Tighten up sound logic so that we know where we should be
    // writing to and can anticipate the time spent in the game update.
    if(SUCCEEDED(GlobalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor)))
    {
        byteToLock = ((SoundOutput.RunningSampleIndex*SoundOutput.BytesPerSample) %
                      SoundOutput.SecondaryBufferSize);
        
        targetCursor =
            ((playCursor +
              (SoundOutput.LatencySampleCount*SoundOutput.BytesPerSample)) %
             SoundOutput.SecondaryBufferSize);
        if(byteToLock > targetCursor)
        {
            bytesToWrite = (SoundOutput.SecondaryBufferSize - byteToLock);
            bytesToWrite += targetCursor;
        }
        else
        {
            bytesToWrite = targetCursor - byteToLock;
        }
        
        soundIsValid = true;
    }
    
    game_sound_output_buffer SoundBuffer = {};
    SoundBuffer.SamplesPerSecond = SoundOutput.SamplesPerSecond;
    SoundBuffer.SampleCount = bytesToWrite / SoundOutput.BytesPerSample;
    SoundBuffer.Samples = Samples;
    
    S16 sineWaveBuffer [globalSoundOutput.secondaryBufferSize] = {};
    
    local_persist R32 tSine;
    S16 toneVolume = 3000;
    int wavePeriod = globalSoundOutput.samplesPerSecond/globalSoundOutput.toneHz;
    
    S16 *sampleOut = sineWaveBuffer;
    for(int sampleIndex = 0;
        sampleIndex < SoundBuffer->SampleCount;
        ++sampleIndex)
    {
        // TODO(casey): Draw this out for people
        real32 SineValue = sinf(tSine);
        int16 SampleValue = (int16)(SineValue * ToneVolume);
        *sampleOut++ = SampleValue;
        *sampleOut++ = SampleValue;
        
        tSine += 2.0f*Pi32*1.0f/(real32)WavePeriod;
    }
}
#endif