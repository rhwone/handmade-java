#include "platform.h"

#include <windows.h>
#include <stdio.h>
#include <malloc.h>
#include <xinput.h>
#include <dsound.h>
#include <cstring>

#include "win32platform.h"

#include "se_abjorklund_win32_JNIPlatform.h"

// TODO(casey): This is a global for now.
global_variable bool32 globalRunning;
global_variable bool32 globalPause;
global_variable win32_offscreen_buffer globalBackbuffer;
global_variable LPDIRECTSOUNDBUFFER globalSecondaryBuffer;
global_variable int64 globalPerfCountFrequency;
global_variable HINSTANCE globalhinstDLL;
global_variable JNIEnv *globalJNIEnv;
global_variable jobject globalThisObjPointer;
global_variable byte *globalVideoBuffer;

#define VIDEO_BUFFER_SIZE (1280 * 740) * 4

// NOTE(casey): XInputGetState
#define X_INPUT_GET_STATE(name) DWORD WINAPI name(DWORD dwUserIndex, XINPUT_STATE *pState)
typedef X_INPUT_GET_STATE(x_input_get_state);
X_INPUT_GET_STATE(xInputGetStateStub)
{
    return (ERROR_DEVICE_NOT_CONNECTED);
}
global_variable x_input_get_state *xInputGetState_ = xInputGetStateStub;
#define XInputGetState xInputGetState_

// NOTE(casey): XInputSetState
#define X_INPUT_SET_STATE(name) DWORD WINAPI name(DWORD dwUserIndex, XINPUT_VIBRATION *pVibration)
typedef X_INPUT_SET_STATE(x_input_set_state);
X_INPUT_SET_STATE(xInputSetStateStub)
{
    return (ERROR_DEVICE_NOT_CONNECTED);
}
global_variable x_input_set_state *xInputSetState_ = xInputSetStateStub;
#define XInputSetState xInputSetState_

#define DIRECT_SOUND_CREATE(name) HRESULT WINAPI name(LPCGUID pcGuidDevice, LPDIRECTSOUND *ppDS, LPUNKNOWN pUnkOuter)
typedef DIRECT_SOUND_CREATE(direct_sound_create);

// Java

internal void gameUpdateAndRender(GameInput *newInput)
{

    int moveUpHalfTransitionCount = newInput->controller.moveUp.halfTransitionCount;
    bool32 moveUpEndedDown = newInput->controller.moveUp.endedDown;

    int moveDownHalfTransitionCount = newInput->controller.moveDown.halfTransitionCount;
    bool32 moveDownEndedDown = newInput->controller.moveDown.endedDown;

    int moveLeftHalfTransitionCount = newInput->controller.moveLeft.halfTransitionCount;
    bool32 moveLeftEndedDown = newInput->controller.moveLeft.endedDown;

    int moveRightHalfTransitionCount = newInput->controller.moveRight.halfTransitionCount;
    bool32 moveRightEndedDown = newInput->controller.moveRight.endedDown;

    int actionUpHalfTransitionCount = newInput->controller.actionUp.halfTransitionCount;
    bool32 actionUpEndedDown = newInput->controller.actionUp.endedDown;

    int actionDownHalfTransitionCount = newInput->controller.actionDown.halfTransitionCount;
    bool32 actionDownEndedDown = newInput->controller.actionDown.endedDown;

    int actionLeftHalfTransitionCount = newInput->controller.actionLeft.halfTransitionCount;
    bool32 actionLeftEndedDown = newInput->controller.actionLeft.endedDown;

    int actionRightHalfTransitionCount = newInput->controller.actionRight.halfTransitionCount;
    bool32 actionRightEndedDown = newInput->controller.actionRight.endedDown;

    int leftShoulderHalfTransitionCount = newInput->controller.leftShoulder.halfTransitionCount;
    bool32 leftShoulderEndedDown = newInput->controller.leftShoulder.endedDown;

    int rightShoulderHalfTransitionCount = newInput->controller.rightShoulder.halfTransitionCount;
    bool32 rightShoulderEndedDown = newInput->controller.rightShoulder.endedDown;

    int backHalfTransitionCount = newInput->controller.back.halfTransitionCount;
    bool32 backEndedDown = newInput->controller.back.endedDown;

    int startHalfTransitionCount = newInput->controller.start.halfTransitionCount;
    bool32 startEndedDown = newInput->controller.start.endedDown;

    bool32 isConnected = newInput->controller.isConnected;
    bool32 isAnalog = newInput->controller.isAnalog;
    real32 stickAverageX = newInput->controller.stickAverageX;
    real32 stickAverageY = newInput->controller.stickAverageY;

    jclass gameClass = globalJNIEnv->FindClass("se/abjorklund/game/GamePlatform");
    jmethodID getVideoBufferId = globalJNIEnv->GetStaticMethodID(gameClass, "win32platform_gameUpdateAndRender", "(IZIZIZIZIZIZIZIZIZIZIZIZZZFF)[B");

    jbyteArray javaVideoBuffer = (jbyteArray)globalJNIEnv->CallStaticObjectMethod(
        gameClass,
        getVideoBufferId,
        moveUpHalfTransitionCount,
        moveUpEndedDown,
        moveDownHalfTransitionCount,
        moveDownEndedDown,
        moveLeftHalfTransitionCount,
        moveLeftEndedDown,
        moveRightHalfTransitionCount,
        moveRightEndedDown,
        actionUpHalfTransitionCount,
        actionUpEndedDown,
        actionDownHalfTransitionCount,
        actionDownEndedDown,
        actionLeftHalfTransitionCount,
        actionLeftEndedDown,
        actionRightHalfTransitionCount,
        actionRightEndedDown,
        leftShoulderHalfTransitionCount,
        leftShoulderEndedDown,
        rightShoulderHalfTransitionCount,
        rightShoulderEndedDown,
        backHalfTransitionCount,
        backEndedDown,
        startHalfTransitionCount,
        startEndedDown,
        isConnected,
        isAnalog,
        stickAverageX,
        stickAverageY);

    jsize lengthOfArray = globalJNIEnv->GetArrayLength(javaVideoBuffer);
    jbyte *elements = globalJNIEnv->GetByteArrayElements(javaVideoBuffer, NULL);

    std::memcpy(globalVideoBuffer, elements, lengthOfArray);
    globalJNIEnv->DeleteLocalRef(gameClass);
    globalJNIEnv->ReleaseByteArrayElements(javaVideoBuffer, elements, JNI_ABORT);
}

// Win32

DEBUG_PLATFORM_FREE_FILE_MEMORY(DEBUGPlatformFreeFileMemory)
{
    if (memory)
    {
        VirtualFree(memory, 0, MEM_RELEASE);
    }
}

DEBUG_PLATFORM_READ_ENTIRE_FILE(DEBUGPlatformReadEntireFile)
{
    DebugReadFileResult result = {};

    HANDLE fileHandle = CreateFileA(filename, GENERIC_READ, FILE_SHARE_READ, 0, OPEN_EXISTING, 0, 0);
    if (fileHandle != INVALID_HANDLE_VALUE)
    {
        LARGE_INTEGER fileSize;
        if (GetFileSizeEx(fileHandle, &fileSize))
        {
            uint32 fileSize32 = safeTruncateUInt64(fileSize.QuadPart);
            result.contents = VirtualAlloc(0, fileSize32, MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);
            if (result.contents)
            {
                DWORD bytesRead;
                if (ReadFile(fileHandle, result.contents, fileSize32, &bytesRead, 0) &&
                    (fileSize32 == bytesRead))
                {
                    // NOTE(casey): File read successfully
                    result.contentsSize = fileSize32;
                }
                else
                {
                    // TODO(casey): Logging
                    DEBUGPlatformFreeFileMemory(thread, result.contents);
                    result.contents = 0;
                }
            }
            else
            {
                // TODO(casey): Logging
            }
        }
        else
        {
            // TODO(casey): Logging
        }

        CloseHandle(fileHandle);
    }
    else
    {
        // TODO(casey): Logging
    }

    return (result);
}

DEBUG_PLATFORM_WRITE_ENTIRE_FILE(DEBUGPlatformWriteEntireFile)
{
    bool32 result = false;

    HANDLE fileHandle = CreateFileA(filename, GENERIC_WRITE, 0, 0, CREATE_ALWAYS, 0, 0);
    if (fileHandle != INVALID_HANDLE_VALUE)
    {
        DWORD BytesWritten;
        if (WriteFile(fileHandle, memory, memorySize, &BytesWritten, 0))
        {
            // NOTE(casey): File read successfully
            result = (BytesWritten == memorySize);
        }
        else
        {
            // TODO(casey): Logging
        }

        CloseHandle(fileHandle);
    }
    else
    {
        // TODO(casey): Logging
    }

    return (result);
}

inline FILETIME
win32GetLastWriteTime(char *Filename)
{
    FILETIME LastWriteTime = {};

    WIN32_FILE_ATTRIBUTE_DATA Data;
    if (GetFileAttributesEx(Filename, GetFileExInfoStandard, &Data))
    {
        LastWriteTime = Data.ftLastWriteTime;
    }

    return (LastWriteTime);
}

internal void
win32LoadXInput(void)
{
    // TODO(casey): Test this on Windows 8
    HMODULE XInputLibrary = LoadLibraryA("xinput1_4.dll");
    if (!XInputLibrary)
    {
        // TODO(casey): Diagnostic
        XInputLibrary = LoadLibraryA("xinput9_1_0.dll");
    }

    if (!XInputLibrary)
    {
        // TODO(casey): Diagnostic
        XInputLibrary = LoadLibraryA("xinput1_3.dll");
    }

    if (XInputLibrary)
    {
        XInputGetState = (x_input_get_state *)GetProcAddress(XInputLibrary, "XInputGetState");
        if (!XInputGetState)
        {
            XInputGetState = xInputGetStateStub;
        }

        XInputSetState = (x_input_set_state *)GetProcAddress(XInputLibrary, "XInputSetState");
        if (!XInputSetState)
        {
            XInputSetState = xInputSetStateStub;
        }

        // TODO(casey): Diagnostic
    }
    else
    {
        // TODO(casey): Diagnostic
    }
}

internal void
win32InitDSound(HWND Window, int32 samplesPerSecond, int32 BufferSize)
{
    // NOTE(casey): Load the library
    HMODULE DSoundLibrary = LoadLibraryA("dsound.dll");
    if (DSoundLibrary)
    {
        // NOTE(casey): Get a DirectSound object! - cooperative
        direct_sound_create *DirectSoundCreate = (direct_sound_create *)
            GetProcAddress(DSoundLibrary, "DirectSoundCreate");

        // TODO(casey): Double-check that this works on XP - DirectSound8 or 7??
        LPDIRECTSOUND DirectSound;
        if (DirectSoundCreate && SUCCEEDED(DirectSoundCreate(0, &DirectSound, 0)))
        {
            WAVEFORMATEX WaveFormat = {};
            WaveFormat.wFormatTag = WAVE_FORMAT_PCM;
            WaveFormat.nChannels = 2;
            WaveFormat.nSamplesPerSec = samplesPerSecond;
            WaveFormat.wBitsPerSample = 16;
            WaveFormat.nBlockAlign = (WaveFormat.nChannels * WaveFormat.wBitsPerSample) / 8;
            WaveFormat.nAvgBytesPerSec = WaveFormat.nSamplesPerSec * WaveFormat.nBlockAlign;
            WaveFormat.cbSize = 0;

            if (SUCCEEDED(DirectSound->SetCooperativeLevel(Window, DSSCL_PRIORITY)))
            {
                DSBUFFERDESC BufferDescription = {};
                BufferDescription.dwSize = sizeof(BufferDescription);
                BufferDescription.dwFlags = DSBCAPS_PRIMARYBUFFER;

                // NOTE(casey): "Create" a primary buffer
                // TODO(casey): DSBCAPS_GLOBALFOCUS?
                LPDIRECTSOUNDBUFFER PrimaryBuffer;
                if (SUCCEEDED(DirectSound->CreateSoundBuffer(&BufferDescription, &PrimaryBuffer, 0)))
                {
                    HRESULT Error = PrimaryBuffer->SetFormat(&WaveFormat);
                    if (SUCCEEDED(Error))
                    {
                        // NOTE(casey): We have finally set the format!
                        OutputDebugStringA("Primary buffer format was set.\n");
                    }
                    else
                    {
                        // TODO(casey): Diagnostic
                    }
                }
                else
                {
                    // TODO(casey): Diagnostic
                }
            }
            else
            {
                // TODO(casey): Diagnostic
            }

            // TODO(casey): DSBCAPS_GETCURRENTPOSITION2
            DSBUFFERDESC BufferDescription = {};
            BufferDescription.dwSize = sizeof(BufferDescription);
            BufferDescription.dwFlags = DSBCAPS_GETCURRENTPOSITION2;
            BufferDescription.dwBufferBytes = BufferSize;
            BufferDescription.lpwfxFormat = &WaveFormat;
            HRESULT Error = DirectSound->CreateSoundBuffer(&BufferDescription, &globalSecondaryBuffer, 0);
            if (SUCCEEDED(Error))
            {
                OutputDebugStringA("Secondary buffer created successfully.\n");
            }
        }
        else
        {
            // TODO(casey): Diagnostic
        }
    }
    else
    {
        // TODO(casey): Diagnostic
    }
}

internal win32_window_dimension
win32GetWindowDimension(HWND Window)
{
    win32_window_dimension Result;

    RECT ClientRect;
    GetClientRect(Window, &ClientRect);
    Result.Width = ClientRect.right - ClientRect.left;
    Result.Height = ClientRect.bottom - ClientRect.top;

    return (Result);
}

internal void
win32ResizeDIBSection(win32_offscreen_buffer *Buffer, int Width, int Height)
{
    // TODO(casey): Bulletproof this.
    // Maybe don't free first, free after, then free first if that fails.

    if (Buffer->memory)
    {
        VirtualFree(Buffer->memory, 0, MEM_RELEASE);
    }

    Buffer->Width = Width;
    Buffer->Height = Height;

    int bytesPerPixel = 4;
    Buffer->bytesPerPixel = bytesPerPixel;

    // NOTE(casey): When the biHeight field is negative, this is the clue to
    // Windows to treat this bitmap as top-down, not bottom-up, meaning that
    // the first three bytes of the image are the color for the top left pixel
    // in the bitmap, not the bottom left!
    Buffer->Info.bmiHeader.biSize = sizeof(Buffer->Info.bmiHeader);
    Buffer->Info.bmiHeader.biWidth = Buffer->Width;
    Buffer->Info.bmiHeader.biHeight = -Buffer->Height;
    Buffer->Info.bmiHeader.biPlanes = 1;
    Buffer->Info.bmiHeader.biBitCount = 32;
    Buffer->Info.bmiHeader.biCompression = BI_RGB;

    // NOTE(casey): Thank you to Chris Hecker of Spy Party fame
    // for clarifying the deal with StretchDIBits and BitBlt!
    // No more DC for us.
    int BitmapMemorySize = (Buffer->Width * Buffer->Height) * bytesPerPixel;
    Buffer->memory = VirtualAlloc(0, BitmapMemorySize, MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);
    Buffer->Pitch = Width * bytesPerPixel;

    // TODO(casey): Probably clear this to black
}

internal void
win32DisplayBufferInWindow(win32_offscreen_buffer *Buffer,
                           HDC DeviceContext, int WindowWidth, int WindowHeight)
{
    // NOTE(casey): For prototyping purposes, we're going to always blit
    // 1-to-1 pixels to make sure we don't introduce artifacts with
    // stretching while we are learning to code the renderer!
    StretchDIBits(DeviceContext,
                  0, 0, Buffer->Width, Buffer->Height,
                  0, 0, Buffer->Width, Buffer->Height,
                  Buffer->memory,
                  &Buffer->Info,
                  DIB_RGB_COLORS, SRCCOPY);
}

internal LRESULT CALLBACK
win32MainWindowCallback(HWND Window,
                        UINT Message,
                        WPARAM WParam,
                        LPARAM LParam)
{
    LRESULT Result = 0;

    switch (Message)
    {
    case WM_CLOSE:
    {
        // TODO(casey): Handle this with a message to the user?
        globalRunning = false;
    }
    break;

    case WM_ACTIVATEAPP:
    {
#if 0
            if(WParam == TRUE)
            {
                SetLayeredWindowAttributes(window, RGB(0, 0, 0), 255, LWA_ALPHA);
            }
            else
            {
                SetLayeredWindowAttributes(window, RGB(0, 0, 0), 64, LWA_ALPHA);
            }
#endif
    }
    break;

    case WM_DESTROY:
    {
        // TODO(casey): Handle this as an error - recreate window?
        globalRunning = false;
    }
    break;

    case WM_SYSKEYDOWN:
    case WM_SYSKEYUP:
    case WM_KEYDOWN:
    case WM_KEYUP:
    {
        Assert(!"Keyboard input came in through a non-dispatch message!");
    }
    break;

    case WM_PAINT:
    {
        PAINTSTRUCT Paint;
        HDC DeviceContext = BeginPaint(Window, &Paint);
        win32_window_dimension Dimension = win32GetWindowDimension(Window);
        win32DisplayBufferInWindow(&globalBackbuffer, DeviceContext,
                                   Dimension.Width, Dimension.Height);
        EndPaint(Window, &Paint);
    }
    break;

    default:
    {
        //            OutputDebugStringA("default\n");
        Result = DefWindowProcA(Window, Message, WParam, LParam);
    }
    break;
    }

    return (Result);
}

internal void
win32ClearBuffer(Win32SoundOutput *SoundOutput)
{
    VOID *Region1;
    DWORD Region1Size;
    VOID *Region2;
    DWORD Region2Size;
    if (SUCCEEDED(globalSecondaryBuffer->Lock(0, SoundOutput->secondaryBufferSize,
                                              &Region1, &Region1Size,
                                              &Region2, &Region2Size,
                                              0)))
    {
        // TODO(casey): assert that Region1Size/Region2Size is valid
        uint8 *DestSample = (uint8 *)Region1;
        for (DWORD ByteIndex = 0;
             ByteIndex < Region1Size;
             ++ByteIndex)
        {
            *DestSample++ = 0;
        }

        DestSample = (uint8 *)Region2;
        for (DWORD ByteIndex = 0;
             ByteIndex < Region2Size;
             ++ByteIndex)
        {
            *DestSample++ = 0;
        }

        globalSecondaryBuffer->Unlock(Region1, Region1Size, Region2, Region2Size);
    }
}

#if 0
internal void
win32FillSoundBuffer(Win32SoundOutput *SoundOutput, DWORD ByteToLock, DWORD BytesToWrite,
                     GameSoundOutputBuffer *SourceBuffer)
{
    // TODO(casey): More strenuous test!
    VOID *Region1;
    DWORD Region1Size;
    VOID *Region2;
    DWORD Region2Size;
    if (SUCCEEDED(globalSecondaryBuffer->Lock(ByteToLock, BytesToWrite,
                                              &Region1, &Region1Size,
                                              &Region2, &Region2Size,
                                              0)))
    {
        // TODO(casey): assert that Region1Size/Region2Size is valid

        // TODO(casey): Collapse these two loops
        DWORD Region1SampleCount = Region1Size / SoundOutput->bytesPerSample;
        int16 *DestSample = (int16 *)Region1;
        int16 *SourceSample = SourceBuffer->samples;
        for (DWORD SampleIndex = 0;
             SampleIndex < Region1SampleCount;
             ++SampleIndex)
        {
            *DestSample++ = *SourceSample++;
            *DestSample++ = *SourceSample++;
            ++SoundOutput->runningSampleIndex;
        }

        DWORD Region2SampleCount = Region2Size / SoundOutput->bytesPerSample;
        DestSample = (int16 *)Region2;
        for (DWORD SampleIndex = 0;
             SampleIndex < Region2SampleCount;
             ++SampleIndex)
        {
            *DestSample++ = *SourceSample++;
            *DestSample++ = *SourceSample++;
            ++SoundOutput->runningSampleIndex;
        }

        globalSecondaryBuffer->Unlock(Region1, Region1Size, Region2, Region2Size);
    }
}
#endif

internal void
win32ProcessKeyboardMessage(GameButtonState *NewState, bool32 IsDown)
{
    if (NewState->endedDown != IsDown)
    {
        NewState->endedDown = IsDown;
        ++NewState->halfTransitionCount;
    }
}

internal void
win32ProcessXInputDigitalButton(DWORD XInputButtonState,
                                GameButtonState *OldState, DWORD ButtonBit,
                                GameButtonState *NewState)
{
    NewState->endedDown = ((XInputButtonState & ButtonBit) == ButtonBit);
    NewState->halfTransitionCount = (OldState->endedDown != NewState->endedDown) ? 1 : 0;
}

internal real32
win32ProcessXInputStickValue(SHORT Value, SHORT DeadZoneThreshold)
{
    real32 Result = 0;

    if (Value < -DeadZoneThreshold)
    {
        Result = (real32)((Value + DeadZoneThreshold) / (32768.0f - DeadZoneThreshold));
    }
    else if (Value > DeadZoneThreshold)
    {
        Result = (real32)((Value - DeadZoneThreshold) / (32767.0f - DeadZoneThreshold));
    }

    return (Result);
}

internal void
win32ProcessPendingMessages(Win32State *state, GameControllerInput *keyboardController)
{
    MSG message;
    while (PeekMessage(&message, 0, 0, 0, PM_REMOVE))
    {
        switch (message.message)
        {
        case WM_QUIT:
        {
            globalRunning = false;
        }
        break;

        case WM_SYSKEYDOWN:
        case WM_SYSKEYUP:
        case WM_KEYDOWN:
        case WM_KEYUP:
        {
            uint32 vKCode = (uint32)message.wParam;

            // NOTE(casey): Since we are comparing WasDown to IsDown,
            // we MUST use == and != to convert these bit tests to actual
            // 0 or 1 values.
            bool32 wasDown = ((message.lParam & (1 << 30)) != 0);
            bool32 isDown = ((message.lParam & (1 << 31)) == 0);
            if (wasDown != isDown)
            {
                if (vKCode == 'W')
                {
                    win32ProcessKeyboardMessage(&keyboardController->moveUp, isDown);
                }
                else if (vKCode == 'A')
                {
                    win32ProcessKeyboardMessage(&keyboardController->moveLeft, isDown);
                }
                else if (vKCode == 'S')
                {
                    win32ProcessKeyboardMessage(&keyboardController->moveDown, isDown);
                }
                else if (vKCode == 'D')
                {
                    win32ProcessKeyboardMessage(&keyboardController->moveRight, isDown);
                }
                else if (vKCode == 'Q')
                {
                    win32ProcessKeyboardMessage(&keyboardController->leftShoulder, isDown);
                }
                else if (vKCode == 'E')
                {
                    win32ProcessKeyboardMessage(&keyboardController->rightShoulder, isDown);
                }
                else if (vKCode == VK_UP)
                {
                    win32ProcessKeyboardMessage(&keyboardController->actionUp, isDown);
                }
                else if (vKCode == VK_LEFT)
                {
                    win32ProcessKeyboardMessage(&keyboardController->actionLeft, isDown);
                }
                else if (vKCode == VK_DOWN)
                {
                    win32ProcessKeyboardMessage(&keyboardController->actionDown, isDown);
                }
                else if (vKCode == VK_RIGHT)
                {
                    win32ProcessKeyboardMessage(&keyboardController->actionRight, isDown);
                }
                else if (vKCode == VK_ESCAPE)
                {
                    win32ProcessKeyboardMessage(&keyboardController->start, isDown);
                }
                else if (vKCode == VK_SPACE)
                {
                    win32ProcessKeyboardMessage(&keyboardController->back, isDown);
                }
#if HANDMADE_INTERNAL
                else if (vKCode == 'P')
                {
                    if (isDown)
                    {
                        globalPause = !globalPause;
                    }
                }
#endif
            }

            bool32 altKeyWasDown = (message.lParam & (1 << 29));
            if ((vKCode == VK_F4) && altKeyWasDown)
            {
                globalRunning = false;
            }
        }
        break;

        default:
        {
            TranslateMessage(&message);
            DispatchMessageA(&message);
        }
        break;
        }
    }
}

inline LARGE_INTEGER
win32GetWallClock(void)
{
    LARGE_INTEGER result;
    QueryPerformanceCounter(&result);
    return (result);
}

inline real32
win32GetSecondsElapsed(LARGE_INTEGER start, LARGE_INTEGER end)
{
    real32 Result = ((real32)(end.QuadPart - start.QuadPart) /
                     (real32)globalPerfCountFrequency);
    return (Result);
}

#if 0

internal void
win32DebugDrawVertical(win32_offscreen_buffer *backbuffer,
                       int x, int top, int bottom, uint32 color)
{
    if(top <= 0)
    {
        top = 0;
    }

    if(bottom > backbuffer->height)
    {
        bottom = backbuffer->height;
    }
    
    if((X >= 0) && (X < backbuffer->width))
    {
        uint8 *pixel = ((uint8 *)backbuffer->memory +
                        x*backbuffer->bytesPerPixel +
                        top*backbuffer->pitch);
        for(int y = yop;
            y < bottom;
            ++y)
        {
            *(uint32 *)pixel = color;
            pixel += backbuffer->pitch;
        }
    }
}

inline void
Win32DrawSoundBufferMarker(win32_offscreen_buffer *Backbuffer,
                           Win32SoundOutput *soundOutput,
                           real32 C, int PadX, int Top, int Bottom,
                           DWORD Value, uint32 Color)
{
    real32 XReal32 = (C * (real32)Value);
    int X = PadX + (int)XReal32;
    Win32DebugDrawVertical(Backbuffer, X, Top, Bottom, Color);
}

internal void
Win32DebugSyncDisplay(win32_offscreen_buffer *Backbuffer,
                      int MarkerCount, win32_debug_time_marker *Markers,
                      int CurrentMarkerIndex,
                      Win32SoundOutput *soundOutput, real32 targetSecondsPerFrame)
{
    int PadX = 16;
    int PadY = 16;

    int LineHeight = 64;
    
    real32 C = (real32)(Backbuffer->Width - 2*PadX) / (real32)soundOutput->secondaryBufferSize;
    for(int MarkerIndex = 0;
        MarkerIndex < MarkerCount;
        ++MarkerIndex)
    {
        win32_debug_time_marker *ThisMarker = &Markers[MarkerIndex];
        Assert(ThisMarker->outputPlayCursor < soundOutput->secondaryBufferSize);
        Assert(ThisMarker->outputWriteCursor < soundOutput->secondaryBufferSize);
        Assert(ThisMarker->outputLocation < soundOutput->secondaryBufferSize);
        Assert(ThisMarker->outputByteCount < soundOutput->secondaryBufferSize);
        Assert(ThisMarker->flipPlayCursor < soundOutput->secondaryBufferSize);
        Assert(ThisMarker->flipWriteCursor < soundOutput->secondaryBufferSize);

        DWORD PlayColor = 0xFFFFFFFF;
        DWORD WriteColor = 0xFFFF0000;
        DWORD ExpectedFlipColor = 0xFFFFFF00;
        DWORD PlayWindowColor = 0xFFFF00FF;

        int Top = PadY;
        int Bottom = PadY + LineHeight;
        if(MarkerIndex == CurrentMarkerIndex)
        {
            Top += LineHeight+PadY;
            Bottom += LineHeight+PadY;

            int FirstTop = Top;
            
            Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, Top, Bottom, ThisMarker->outputPlayCursor, PlayColor);
            Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, Top, Bottom, ThisMarker->outputWriteCursor, WriteColor);

            Top += LineHeight+PadY;
            Bottom += LineHeight+PadY;

            Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, Top, Bottom, ThisMarker->outputLocation, PlayColor);
            Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, Top, Bottom, ThisMarker->outputLocation + ThisMarker->outputByteCount, WriteColor);

            Top += LineHeight+PadY;
            Bottom += LineHeight+PadY;

            Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, FirstTop, Bottom, ThisMarker->expectedFlipPlayCursor, ExpectedFlipColor);
        }        
        
        Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, Top, Bottom, ThisMarker->flipPlayCursor, PlayColor);
        Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, Top, Bottom, ThisMarker->flipPlayCursor + 480*soundOutput->bytesPerSample, PlayWindowColor);
        Win32DrawSoundBufferMarker(Backbuffer, soundOutput, C, PadX, Top, Bottom, ThisMarker->flipWriteCursor, WriteColor);
    }
}

#endif

int main(HINSTANCE instance,
         HINSTANCE prevInstance,
         LPSTR commandLine,
         int showCode)
{
    Win32State win32State = {};

    LARGE_INTEGER perfCountFrequencyResult;
    QueryPerformanceFrequency(&perfCountFrequencyResult);
    globalPerfCountFrequency = perfCountFrequencyResult.QuadPart;

    // NOTE(casey): Set the Windows scheduler granularity to 1ms
    // so that our Sleep() can be more granular.
    UINT desiredSchedulerMS = 1;
    bool32 sleepIsGranular = (timeBeginPeriod(desiredSchedulerMS) == TIMERR_NOERROR);

    win32LoadXInput();

    WNDCLASSA windowClass = {};

    win32ResizeDIBSection(&globalBackbuffer, 1280, 720);

    windowClass.style = CS_HREDRAW | CS_VREDRAW;
    windowClass.lpfnWndProc = win32MainWindowCallback;
    windowClass.hInstance = instance;
    //    WindowClass.hIcon;
    windowClass.lpszClassName = "HandmadeHeroWindowClass";

    if (RegisterClassA(&windowClass))
    {
        HWND window =
            CreateWindowExA(
                0, // WS_EX_TOPMOST|WS_EX_LAYERED,
                windowClass.lpszClassName,
                "Handmade Hero",
                WS_OVERLAPPEDWINDOW | WS_VISIBLE,
                CW_USEDEFAULT,
                CW_USEDEFAULT,
                CW_USEDEFAULT,
                CW_USEDEFAULT,
                0,
                0,
                instance,
                0);
        if (window)
        {
            Win32SoundOutput soundOutput = {};

            // TODO(casey): How do we reliably query on this on Windows?
            int monitorRefreshHz = 60;
            HDC refreshDC = GetDC(window);
            int win32RefreshRate = GetDeviceCaps(refreshDC, VREFRESH);
            ReleaseDC(window, refreshDC);
            if (win32RefreshRate > 1)
            {
                monitorRefreshHz = win32RefreshRate;
            }
            real32 gameUpdateHz = (monitorRefreshHz / 2.0f);
            real32 targetSecondsPerFrame = 1.0f / (real32)gameUpdateHz;

            // TODO(casey): Make this like sixty seconds?
            soundOutput.samplesPerSecond = 48000;
            soundOutput.bytesPerSample = sizeof(int16) * 2;
            soundOutput.secondaryBufferSize = soundOutput.samplesPerSecond * soundOutput.bytesPerSample;
            // TODO(casey): Actually compute this variance and see
            // what the lowest reasonable value is.
            soundOutput.safetyBytes = (int)(((real32)soundOutput.samplesPerSecond * (real32)soundOutput.bytesPerSample / gameUpdateHz) / 3.0f);
            win32InitDSound(window, soundOutput.samplesPerSecond, soundOutput.secondaryBufferSize);
            win32ClearBuffer(&soundOutput);
            globalSecondaryBuffer->Play(0, 0, DSBPLAY_LOOPING);

            globalRunning = true;

#if 0
            // NOTE(casey): This tests the PlayCursor/WriteCursor update frequency
            // On the Handmade Hero machine, it was 480 samples.
            while(globalRunning)
            {
                DWORD playCursor;
                DWORD writeCursor;
                globalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor);

                char textBuffer[256];
                _snprintf_s(textBuffer, sizeof(textBuffer),
                            "PC:%u WC:%u\n", playCursor, writeCursor);
                OutputDebugStringA(textBuffer);
            }
#endif

            // TODO(casey): Pool with bitmap VirtualAlloc
            int16 *samples = (int16 *)VirtualAlloc(0, soundOutput.secondaryBufferSize,
                                                   MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);

#if HANDMADE_INTERNAL
            LPVOID baseAddress = (LPVOID)Terabytes(2);
#else
            LPVOID baseAddress = 0;
#endif

            GameMemory gameMemory = {};
            gameMemory.permanentStorageSize = Megabytes(64);
            gameMemory.transientStorageSize = Gigabytes(1);
            gameMemory.DEBUGPlatformFreeFileMemory = DEBUGPlatformFreeFileMemory;
            gameMemory.DEBUGPlatformReadEntireFile = DEBUGPlatformReadEntireFile;
            gameMemory.DEBUGPlatformWriteEntireFile = DEBUGPlatformWriteEntireFile;

            // TODO(casey): Handle various memory footprints (USING SYSTEM METRICS)
            // TODO(casey): Use MEM_LARGE_PAGES and call adjust token
            // privileges when not on Windows XP?
            win32State.totalSize = gameMemory.permanentStorageSize + gameMemory.transientStorageSize;
            win32State.gameMemoryBlock = VirtualAlloc(baseAddress, (size_t)win32State.totalSize,
                                                      MEM_RESERVE | MEM_COMMIT,
                                                      PAGE_READWRITE);
            gameMemory.permanentStorage = win32State.gameMemoryBlock;
            gameMemory.transientStorage = ((uint8 *)gameMemory.permanentStorage +
                                           gameMemory.permanentStorageSize);

            if (samples && gameMemory.permanentStorage && gameMemory.transientStorage)
            {
                GameInput input[2] = {};
                GameInput *newInput = &input[0];
                GameInput *oldInput = &input[1];

                LARGE_INTEGER lastCounter = win32GetWallClock();
                LARGE_INTEGER flipWallClock = win32GetWallClock();

                int debugTimeMarkerIndex = 0;
                Win32DebugTimeMarker debugTimeMarkers[30] = {0};

                DWORD audioLatencyBytes = 0;
                real32 audioLatencySeconds = 0;
                bool32 soundIsValid = false;

                uint32 loadCounter = 0;

                uint64 lastCycleCount = __rdtsc();
                while (globalRunning)
                {
                    // TODO(casey): Zeroing macro
                    // TODO(casey): We can't zero everything because the up/down state will
                    // be wrong!!!
                    GameControllerInput *oldKeyboardController = GetController(oldInput);
                    GameControllerInput *newKeyboardController = GetController(newInput);
                    *newKeyboardController = {};
                    newKeyboardController->isConnected = true;
                    for (int buttonIndex = 0;
                         buttonIndex < ArrayCount(newKeyboardController->buttons);
                         ++buttonIndex)
                    {
                        newKeyboardController->buttons[buttonIndex].endedDown =
                            oldKeyboardController->buttons[buttonIndex].endedDown;
                    }

                    win32ProcessPendingMessages(&win32State, newKeyboardController);

                    if (!globalPause)
                    {
                        POINT mouseP;
                        GetCursorPos(&mouseP);
                        ScreenToClient(window, &mouseP);
                        newInput->mouseX = mouseP.x;
                        newInput->mouseY = mouseP.y;
                        newInput->mouseZ = 0; // TODO(casey): Support mousewheel?
                        win32ProcessKeyboardMessage(&newInput->mouseButtons[0],
                                                    GetKeyState(VK_LBUTTON) & (1 << 15));
                        win32ProcessKeyboardMessage(&newInput->mouseButtons[1],
                                                    GetKeyState(VK_MBUTTON) & (1 << 15));
                        win32ProcessKeyboardMessage(&newInput->mouseButtons[2],
                                                    GetKeyState(VK_RBUTTON) & (1 << 15));
                        win32ProcessKeyboardMessage(&newInput->mouseButtons[3],
                                                    GetKeyState(VK_XBUTTON1) & (1 << 15));
                        win32ProcessKeyboardMessage(&newInput->mouseButtons[4],
                                                    GetKeyState(VK_XBUTTON2) & (1 << 15));

                        // TODO(casey): Need to not poll disconnected controllers to avoid
                        // xinput frame rate hit on older libraries...
                        // TODO(casey): Should we poll this more frequently

                        GameControllerInput *oldController = GetController(oldInput);
                        GameControllerInput *newController = GetController(newInput);

                        XINPUT_STATE controllerState;
                        if (XInputGetState(0, &controllerState) == ERROR_SUCCESS)
                        {
                            newController->isConnected = true;
                            newController->isAnalog = oldController->isAnalog;

                            // NOTE(casey): This controller is plugged in
                            // TODO(casey): See if ControllerState.dwPacketNumber increments too rapidly
                            XINPUT_GAMEPAD *pad = &controllerState.Gamepad;

                            // TODO(casey): This is a square deadzone, check XInput to
                            // verify that the deadzone is "round" and show how to do
                            // round deadzone processing.
                            newController->stickAverageX = win32ProcessXInputStickValue(
                                pad->sThumbLX, XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE);
                            newController->stickAverageY = win32ProcessXInputStickValue(
                                pad->sThumbLY, XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE);
                            if ((newController->stickAverageX != 0.0f) ||
                                (newController->stickAverageY != 0.0f))
                            {
                                newController->isAnalog = true;
                            }

                            if (pad->wButtons & XINPUT_GAMEPAD_DPAD_UP)
                            {
                                newController->stickAverageY = 1.0f;
                                newController->isAnalog = false;
                            }

                            if (pad->wButtons & XINPUT_GAMEPAD_DPAD_DOWN)
                            {
                                newController->stickAverageY = -1.0f;
                                newController->isAnalog = false;
                            }

                            if (pad->wButtons & XINPUT_GAMEPAD_DPAD_LEFT)
                            {
                                newController->stickAverageX = -1.0f;
                                newController->isAnalog = false;
                            }

                            if (pad->wButtons & XINPUT_GAMEPAD_DPAD_RIGHT)
                            {
                                newController->stickAverageX = 1.0f;
                                newController->isAnalog = false;
                            }

                            real32 threshold = 0.5f;
                            win32ProcessXInputDigitalButton(
                                (newController->stickAverageX < -threshold) ? 1 : 0,
                                &oldController->moveLeft, 1,
                                &newController->moveLeft);
                            win32ProcessXInputDigitalButton(
                                (newController->stickAverageX > threshold) ? 1 : 0,
                                &oldController->moveRight, 1,
                                &newController->moveRight);
                            win32ProcessXInputDigitalButton(
                                (newController->stickAverageY < -threshold) ? 1 : 0,
                                &oldController->moveDown, 1,
                                &newController->moveDown);
                            win32ProcessXInputDigitalButton(
                                (newController->stickAverageY > threshold) ? 1 : 0,
                                &oldController->moveUp, 1,
                                &newController->moveUp);

                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->actionDown, XINPUT_GAMEPAD_A,
                                                            &newController->actionDown);
                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->actionRight, XINPUT_GAMEPAD_B,
                                                            &newController->actionRight);
                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->actionLeft, XINPUT_GAMEPAD_X,
                                                            &newController->actionLeft);
                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->actionUp, XINPUT_GAMEPAD_Y,
                                                            &newController->actionUp);
                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->leftShoulder, XINPUT_GAMEPAD_LEFT_SHOULDER,
                                                            &newController->leftShoulder);
                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->rightShoulder, XINPUT_GAMEPAD_RIGHT_SHOULDER,
                                                            &newController->rightShoulder);

                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->start, XINPUT_GAMEPAD_START,
                                                            &newController->start);
                            win32ProcessXInputDigitalButton(pad->wButtons,
                                                            &oldController->back, XINPUT_GAMEPAD_BACK,
                                                            &newController->back);
                        }
                        else
                        {
                            // NOTE(casey): The controller is not available
                            newController->isConnected = false;
                        }

                        gameUpdateAndRender(newInput);
                        globalBackbuffer.memory = (void *)globalVideoBuffer;
#if 0
                        LARGE_INTEGER audioWallClock = win32GetWallClock();
                        real32 fromBeginToAudioSeconds = win32GetSecondsElapsed(flipWallClock, audioWallClock);

                        DWORD playCursor;
                        DWORD writeCursor;
                        if (globalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor) == DS_OK)
                        {
                            /* NOTE(casey):

                               Here is how sound output computation works.

                               We define a safety value that is the number
                               of samples we think our game update loop
                               may vary by (let's say up to 2ms)
                       
                               When we wake up to write audio, we will look
                               and see what the play cursor position is and we
                               will forecast ahead where we think the play
                               cursor will be on the next frame boundary.

                               We will then look to see if the write cursor is
                               before that by at least our safety value.  If
                               it is, the target fill position is that frame
                               boundary plus one frame.  This gives us perfect
                               audio sync in the case of a card that has low
                               enough latency.

                               If the write cursor is _after_ that safety
                               margin, then we assume we can never sync the
                               audio perfectly, so we will write one frame's
                               worth of audio plus the safety margin's worth
                               of guard samples.
                            */
                            if (!soundIsValid)
                            {
                                soundOutput.runningSampleIndex = writeCursor / soundOutput.bytesPerSample;
                                soundIsValid = true;
                            }

                            DWORD byteToLock = ((soundOutput.runningSampleIndex * soundOutput.bytesPerSample) %
                                                soundOutput.secondaryBufferSize);

                            DWORD expectedSoundBytesPerFrame =
                                (int)((real32)(soundOutput.samplesPerSecond * soundOutput.bytesPerSample) /
                                      gameUpdateHz);
                            real32 secondsLeftUntilFlip = (targetSecondsPerFrame - fromBeginToAudioSeconds);
                            DWORD expectedBytesUntilFlip = (DWORD)((secondsLeftUntilFlip / targetSecondsPerFrame) * (real32)expectedSoundBytesPerFrame);

                            DWORD expectedFrameBoundaryByte = playCursor + expectedBytesUntilFlip;

                            DWORD safeWriteCursor = writeCursor;
                            if (safeWriteCursor < playCursor)
                            {
                                safeWriteCursor += soundOutput.secondaryBufferSize;
                            }
                            Assert(safeWriteCursor >= playCursor);
                            safeWriteCursor += soundOutput.safetyBytes;

                            bool32 audioCardIsLowLatency = (safeWriteCursor < expectedFrameBoundaryByte);

                            DWORD targetCursor = 0;
                            if (audioCardIsLowLatency)
                            {
                                targetCursor = (expectedFrameBoundaryByte + expectedSoundBytesPerFrame);
                            }
                            else
                            {
                                targetCursor = (writeCursor + expectedSoundBytesPerFrame +
                                                soundOutput.safetyBytes);
                            }
                            targetCursor = (targetCursor % soundOutput.secondaryBufferSize);

                            DWORD bytesToWrite = 0;
                            if (byteToLock > targetCursor)
                            {
                                bytesToWrite = (soundOutput.secondaryBufferSize - byteToLock);
                                bytesToWrite += targetCursor;
                            }
                            else
                            {
                                bytesToWrite = targetCursor - byteToLock;
                            }

                            GameSoundOutputBuffer soundBuffer = {};
                            soundBuffer.samplesPerSecond = soundOutput.samplesPerSecond;
                            soundBuffer.sampleCount = bytesToWrite / soundOutput.bytesPerSample;
                            soundBuffer.samples = samples;
                            if (game.getSoundSamples)
                            {
                                game.getSoundSamples(&thread, &gameMemory, &soundBuffer);
                            }

#if HANDMADE_INTERNAL
                            Win32DebugTimeMarker *marker = &debugTimeMarkers[debugTimeMarkerIndex];
                            marker->outputPlayCursor = playCursor;
                            marker->outputWriteCursor = writeCursor;
                            marker->outputLocation = byteToLock;
                            marker->outputByteCount = bytesToWrite;
                            marker->expectedFlipPlayCursor = expectedFrameBoundaryByte;

                            DWORD unwrappedWriteCursor = writeCursor;
                            if (unwrappedWriteCursor < playCursor)
                            {
                                unwrappedWriteCursor += soundOutput.secondaryBufferSize;
                            }
                            audioLatencyBytes = unwrappedWriteCursor - playCursor;
                            audioLatencySeconds =
                                (((real32)audioLatencyBytes / (real32)soundOutput.bytesPerSample) /
                                 (real32)soundOutput.samplesPerSecond);

#if 0
                            char textBuffer[256];
                            _snprintf_s(textBuffer, sizeof(textBuffer),
                                        "BTL:%u TC:%u BTW:%u - PC:%u WC:%u DELTA:%u (%fs)\n",
                                        byteToLock, targetCursor, bytesToWrite,
                                        playCursor, writeCursor, audioLatencyBytes, audioLatencySeconds);
                            OutputDebugStringA(textBuffer);
#endif
#endif
                            win32FillSoundBuffer(&soundOutput, byteToLock, bytesToWrite, &soundBuffer);
                        }
                        else
                        {
                            soundIsValid = false;
                        }
#endif
                        LARGE_INTEGER workCounter = win32GetWallClock();
                        real32 workSecondsElapsed = win32GetSecondsElapsed(lastCounter, workCounter);

                        // TODO(casey): NOT TESTED YET!  PROBABLY BUGGY!!!!!
                        real32 secondsElapsedForFrame = workSecondsElapsed;
                        if (secondsElapsedForFrame < targetSecondsPerFrame)
                        {
                            if (sleepIsGranular)
                            {
                                DWORD sleepMS = (DWORD)(1000.0f * (targetSecondsPerFrame -
                                                                   secondsElapsedForFrame));
                                if (sleepMS > 0)
                                {
                                    Sleep(sleepMS);
                                }
                            }

                            real32 testSecondsElapsedForFrame = win32GetSecondsElapsed(lastCounter,
                                                                                       win32GetWallClock());
                            if (testSecondsElapsedForFrame < targetSecondsPerFrame)
                            {
                                // TODO(casey): LOG MISSED SLEEP HERE
                            }

                            while (secondsElapsedForFrame < targetSecondsPerFrame)
                            {
                                secondsElapsedForFrame = win32GetSecondsElapsed(lastCounter,
                                                                                win32GetWallClock());
                            }
                        }
                        else
                        {
                            // TODO(casey): MISSED FRAME RATE!
                            // TODO(casey): Logging
                        }

                        LARGE_INTEGER endCounter = win32GetWallClock();
                        real32 MSPerFrame = 1000.0f * win32GetSecondsElapsed(lastCounter, endCounter);
                        lastCounter = endCounter;

                        win32_window_dimension dimension = win32GetWindowDimension(window);
                        HDC deviceContext = GetDC(window);
                        win32DisplayBufferInWindow(&globalBackbuffer, deviceContext,
                                                   dimension.Width, dimension.Height);
                        ReleaseDC(window, deviceContext);

                        flipWallClock = win32GetWallClock();
#if HANDMADE_INTERNAL
                        // NOTE(casey): This is debug code
                        {
                            DWORD playCursor;
                            DWORD writeCursor;
                            if (globalSecondaryBuffer->GetCurrentPosition(&playCursor, &writeCursor) == DS_OK)
                            {
                                Assert(debugTimeMarkerIndex < ArrayCount(debugTimeMarkers));
                                Win32DebugTimeMarker *Marker = &debugTimeMarkers[debugTimeMarkerIndex];
                                Marker->flipPlayCursor = playCursor;
                                Marker->flipWriteCursor = writeCursor;
                            }
                        }
#endif

                        GameInput *temp = newInput;
                        newInput = oldInput;
                        oldInput = temp;
                        // TODO(casey): Should I clear these here?

#if 0
                        uint64 EndCycleCount = __rdtsc();
                        uint64 CyclesElapsed = EndCycleCount - lastCycleCount;
                        lastCycleCount = EndCycleCount;
                    
                        real64 FPS = 0.0f;
                        real64 MCPF = ((real64)CyclesElapsed / (1000.0f * 1000.0f));

                        char FPSBuffer[256];
                        _snprintf_s(FPSBuffer, sizeof(FPSBuffer),
                                    "%.02fms/f,  %.02ff/s,  %.02fmc/f\n", MSPerFrame, FPS, MCPF);
                        OutputDebugStringA(FPSBuffer);
#endif

#if HANDMADE_INTERNAL
                        ++debugTimeMarkerIndex;
                        if (debugTimeMarkerIndex == ArrayCount(debugTimeMarkers))
                        {
                            debugTimeMarkerIndex = 0;
                        }
#endif
                    }
                }
            }
            else
            {
                // TODO(casey): Logging
            }
        }
        else
        {
            // TODO(casey): Logging
        }
    }
    else
    {
        // TODO(casey): Logging
    }

    return (0);
}

BOOL DllMain(
    HINSTANCE hinstDLL,
    DWORD fdwReason,
    LPVOID lpvReserved)
{
    if (fdwReason == 0)
    {
        globalhinstDLL = hinstDLL;
    }
    return true;
}

extern "C"
{
    __declspec(dllexport) void startPlatformLoop(JNIEnv *env, jobject thisObj)
    {
        globalVideoBuffer = new byte[VIDEO_BUFFER_SIZE];
        globalJNIEnv = env;
        globalThisObjPointer = thisObj;
        main(globalhinstDLL, NULL, NULL, NULL);
    }
}
