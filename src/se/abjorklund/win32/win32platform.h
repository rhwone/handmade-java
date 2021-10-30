#if !defined(WIN32_HANDMADE_H)
/* ========================================================================
   $File: $
   $Date: $
   $Revision: $
   $Creator: Casey Muratori $
   $Notice: (C) Copyright 2014 by Molly Rocket, Inc. All Rights Reserved. $
   ======================================================================== */

struct win32_offscreen_buffer
{
    // NOTE(casey): Pixels are alwasy 32-bits wide, Memory Order BB GG RR XX
    BITMAPINFO Info;
    void *memory;
    int Width;
    int Height;
    int Pitch;
    int bytesPerPixel;
};

struct win32_window_dimension
{
    int Width;
    int Height;
};

struct Win32SoundOutput
{
    int samplesPerSecond;
    uint32 runningSampleIndex;
    int bytesPerSample;
    DWORD secondaryBufferSize;
    DWORD safetyBytes;
    real32 tSine;
    int latencySampleCount;
    int toneHz;
    // TODO(casey): Should running sample index be in bytes as well
    // TODO(casey): Math gets simpler if we add a "bytes per second" field?
};

struct Win32DebugTimeMarker
{
    DWORD outputPlayCursor;
    DWORD outputWriteCursor;
    DWORD outputLocation;
    DWORD outputByteCount;
    DWORD expectedFlipPlayCursor;
    
    DWORD flipPlayCursor;
    DWORD flipWriteCursor;
};

struct Win32GameCode
{
    HMODULE gameCodeDLL;
    FILETIME DLLLastWriteTime;

    // IMPORTANT(casey): Either of the callbacks can be 0!  You must
    // check before calling.
    game_update_and_render *updateAndRender;
    game_get_sound_samples *getSoundSamples;

    bool32 isValid;
};

#define WIN32_STATE_FILE_NAME_COUNT MAX_PATH
struct Win32ReplayBuffer
{
    HANDLE fileHandle;
    HANDLE memoryMap;
    char fileName[WIN32_STATE_FILE_NAME_COUNT];
    void *memoryBlock;
};
struct Win32State
{
    uint64 totalSize;
    void *gameMemoryBlock;
    Win32ReplayBuffer replayBuffers[4];
    
    HANDLE recordingHandle;
    int inputRecordingIndex;

    HANDLE playbackHandle;
    int inputPlayingIndex;
    
    char EXEFileName[WIN32_STATE_FILE_NAME_COUNT];
    char *onePastLastEXEFileNameSlash;
};

#define WIN32_HANDMADE_H
#endif
