#include <windows.h>
#include <stdint.h>
#include <stdio.h>
#include <stddef.h>
#include <limits.h>
#include <float.h>
#include <math.h>
#include <xaudio2.h>
#include "se_abjorklund_win32_jna_xaudio2_XAudio2JNI.h"

#ifdef _XBOX //Big-Endian
#define fourccRIFF 'RIFF'
#define fourccDATA 'data'
#define fourccFMT 'fmt '
#define fourccWAVE 'WAVE'
#define fourccXWMA 'XWMA'
#define fourccDPDS 'dpds'
#endif

#ifndef _XBOX //Little-Endian
#define fourccRIFF 'FFIR'
#define fourccDATA 'atad'
#define fourccFMT ' tmf'
#define fourccWAVE 'EVAW'
#define fourccXWMA 'AMWX'
#define fourccDPDS 'sdpd'
#endif

static IXAudio2 *pXAudio2;
static IXAudio2MasteringVoice *pMasterVoice;

HRESULT FindChunk(HANDLE hFile, DWORD fourcc, DWORD &dwChunkSize, DWORD &dwChunkDataPosition)
{
    HRESULT hr = S_OK;
    if (INVALID_SET_FILE_POINTER == SetFilePointer(hFile, 0, NULL, FILE_BEGIN))
        return HRESULT_FROM_WIN32(GetLastError());

    DWORD dwChunkType;
    DWORD dwChunkDataSize;
    DWORD dwRIFFDataSize = 0;
    DWORD dwFileType;
    DWORD bytesRead = 0;
    DWORD dwOffset = 0;

    while (hr == S_OK)
    {
        DWORD dwRead;
        if (0 == ReadFile(hFile, &dwChunkType, sizeof(DWORD), &dwRead, NULL))
            hr = HRESULT_FROM_WIN32(GetLastError());

        if (0 == ReadFile(hFile, &dwChunkDataSize, sizeof(DWORD), &dwRead, NULL))
            hr = HRESULT_FROM_WIN32(GetLastError());

        switch (dwChunkType)
        {
        case fourccRIFF:
            dwRIFFDataSize = dwChunkDataSize;
            dwChunkDataSize = 4;
            if (0 == ReadFile(hFile, &dwFileType, sizeof(DWORD), &dwRead, NULL))
                hr = HRESULT_FROM_WIN32(GetLastError());
            break;

        default:
            if (INVALID_SET_FILE_POINTER == SetFilePointer(hFile, dwChunkDataSize, NULL, FILE_CURRENT))
                return HRESULT_FROM_WIN32(GetLastError());
        }

        dwOffset += sizeof(DWORD) * 2;

        if (dwChunkType == fourcc)
        {
            dwChunkSize = dwChunkDataSize;
            dwChunkDataPosition = dwOffset;
            return S_OK;
        }

        dwOffset += dwChunkDataSize;

        if (bytesRead >= dwRIFFDataSize)
            return S_FALSE;
    }

    return S_OK;
}

HRESULT ReadChunkData(HANDLE hFile, void *buffer, DWORD buffersize, DWORD bufferoffset)
{
    HRESULT hr = S_OK;
    if (INVALID_SET_FILE_POINTER == SetFilePointer(hFile, bufferoffset, NULL, FILE_BEGIN))
        return HRESULT_FROM_WIN32(GetLastError());
    DWORD dwRead;
    if (0 == ReadFile(hFile, buffer, buffersize, &dwRead, NULL))
        hr = HRESULT_FROM_WIN32(GetLastError());
    return hr;
}

HANDLE LoadFile(char *filePath)
{
    HRESULT hr = S_OK;
#ifdef _XBOX
    char *strFileName = filePath;
#else
    TCHAR *strFileName = TEXT(filePath);
#endif
    // Open the file
    HANDLE hFile = CreateFile(
        strFileName,
        GENERIC_READ,
        FILE_SHARE_READ,
        NULL,
        OPEN_EXISTING,
        0,
        NULL);

    if (INVALID_HANDLE_VALUE == hFile)
    {
        printf("INVALID_HANDLE_VALUE\n");
    }

    if (INVALID_SET_FILE_POINTER == SetFilePointer(hFile, 0, NULL, FILE_BEGIN))
    {
        printf("INVALID_SET_FILE_POINTER\n");
    }

    return hFile;
}

void StartPlayingFile(HANDLE hFile)
{
    WAVEFORMATEXTENSIBLE wfx = {0};
    XAUDIO2_BUFFER xaudio2Buffer = {0};
    DWORD dwChunkSize;
    DWORD dwChunkPosition;
    //check the file type, should be fourccWAVE or 'XWMA'
    FindChunk(hFile, fourccRIFF, dwChunkSize, dwChunkPosition);
    DWORD filetype;
    ReadChunkData(hFile, &filetype, sizeof(DWORD), dwChunkPosition);
    if (filetype != fourccWAVE){
        //Handle this
    }

    FindChunk(hFile, fourccFMT, dwChunkSize, dwChunkPosition);
    ReadChunkData(hFile, &wfx, dwChunkSize, dwChunkPosition);

    //fill out the audio data buffer with the contents of the fourccDATA chunk
    FindChunk(hFile, fourccDATA, dwChunkSize, dwChunkPosition);
    BYTE *pDataBuffer = new BYTE[dwChunkSize];
    ReadChunkData(hFile, pDataBuffer, dwChunkSize, dwChunkPosition);

    xaudio2Buffer.AudioBytes = dwChunkSize;      //size of the audio buffer in bytes
    xaudio2Buffer.pAudioData = pDataBuffer;      //buffer containing audio data
    xaudio2Buffer.Flags = XAUDIO2_END_OF_STREAM; // tell the source voice not to expect any data after this buffer
    xaudio2Buffer.LoopCount = XAUDIO2_LOOP_INFINITE;
    xaudio2Buffer.LoopBegin = 0;

    IXAudio2SourceVoice *pSourceVoice;
    pXAudio2->CreateSourceVoice(&pSourceVoice, (WAVEFORMATEX *)&wfx);

    pSourceVoice->SubmitSourceBuffer(&xaudio2Buffer);

    pSourceVoice->Start(0);
}

HRESULT init()
{
    HRESULT hr;
    hr = CoInitializeEx(nullptr, COINIT_MULTITHREADED);
    if (FAILED(hr))
    {
        return hr;
    }

    printf("COM initialized\n");

    pXAudio2 = nullptr;
    if (FAILED(hr = XAudio2Create(&pXAudio2, 0, XAUDIO2_DEFAULT_PROCESSOR)))
        return hr;

    printf("XAudio2 Engine created\n");

    pMasterVoice = nullptr;
    if (FAILED(hr = pXAudio2->CreateMasteringVoice(&pMasterVoice)))
        return hr;

    printf("Mastering voice created\n");

    HANDLE alarm02File = LoadFile("G:\\handmade-java\\src\\se\\abjorklund\\win32\\jna\\xaudio2\\Alarm02.wav");
    StartPlayingFile(alarm02File);
 
    HANDLE ring09File = LoadFile("G:\\handmade-java\\src\\se\\abjorklund\\win32\\jna\\xaudio2\\Ring09.wav");
    StartPlayingFile(ring09File);

    return hr;
}

JNIEXPORT void JNICALL Java_se_abjorklund_win32_jna_xaudio2_XAudio2JNI_initXAudio2(JNIEnv *enb, jobject thisObject)
{
    init();
}