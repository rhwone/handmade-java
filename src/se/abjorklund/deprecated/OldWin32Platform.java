package se.abjorklund.deprecated;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import se.abjorklund.game.Game;
import se.abjorklund.game.GameOffscreenBuffer;
import se.abjorklund.deprecated.jna.IGdi32;
import se.abjorklund.deprecated.jna.IKernel32;
import se.abjorklund.deprecated.jna.IUser32;
import se.abjorklund.deprecated.jna.dsound.DSoundJNI;
import se.abjorklund.deprecated.jna.xaudio2.XAudio2JNI;
import se.abjorklund.deprecated.jna.xinput.*;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import static com.sun.jna.platform.win32.WinGDI.BI_RGB;
import static java.awt.event.KeyEvent.*;
import static se.abjorklund.deprecated.jna.IUser32.*;
import static se.abjorklund.deprecated.jna.xinput.XInput.XUSER_MAX_COUNT;
import static se.abjorklund.deprecated.jna.xinput.XInputGamepad.*;


public class OldWin32Platform implements WindowProc {

    private static final Game game = new Game();

    private static final DWORD SRCCOPY = new DWORD(13369376);
    public static final int SIZE_OF_INT = 4;

    public static final double PI_32 = 3.14159265359f;

    private final WNDCLASSEX windowClass;
    private static boolean running;
    private static final Win32OffscreenBuffer globalBackBuffer = new Win32OffscreenBuffer();
    public static final LARGE_INTEGER PERF_COUNTER_FREQUENCY = new LARGE_INTEGER();

    //JNA WINAPI Instances
    private static XInput XINPUT;
    private static IKernel32 IKERNEL32;
    private final IUser32 IUSER32;
    private final IGdi32 IGDI32;

    //CUSTOM JNI INSTANCES
    private static final DSoundJNI D_SOUND_JNI = new DSoundJNI();
    private static final XAudio2JNI X_AUDIO_2_JNI = new XAudio2JNI();

    public OldWin32Platform() {
        String osName = System.getProperty("os.name");
        System.out.println("Running on: " + osName);
        String windowClassName = "MyWindowClass";

        IKERNEL32 = IKernel32.INSTANCE;
        IUSER32 = IUser32.INSTANCE;
        IGDI32 = IGdi32.INSTANCE;
        try {
            XINPUT = XInputFactory.getInstance();
        } catch (XInputLibraryNotFound ex) {
            ex.printStackTrace();
        }
        HMODULE instance = Kernel32.INSTANCE.GetModuleHandle("");

        IKERNEL32.QueryPerformanceFrequency(PERF_COUNTER_FREQUENCY);

        windowClass = new WNDCLASSEX();
        windowClass.style = CS_HREDRAW | CS_VREDRAW;
        windowClass.hInstance = instance;
        windowClass.lpfnWndProc = OldWin32Platform.this;
        windowClass.lpszClassName = windowClassName;
    }

    private void win32resizeDIBSection(Win32OffscreenBuffer buffer, int width, int height) {
        if (buffer.getMemory() != null) {
            IKERNEL32.VirtualFree(buffer.getMemory(), null, MEM_RELEASE);
        }

        buffer.setWidth(width);
        buffer.setHeight(height);
        buffer.setBytesPerPixel(4);
        buffer.setPitch(width * buffer.getBytesPerPixel());

        WinGDI.BITMAPINFOHEADER infoHeader = new WinGDI.BITMAPINFOHEADER();
        infoHeader.biSize = 40;
        infoHeader.biWidth = buffer.getWidth();
        infoHeader.biHeight = -buffer.getHeight(); //Negative as specified by the API in order to get top-down DIB. Origin is upper left corner.
        infoHeader.biPlanes = 1;
        infoHeader.biBitCount = 32;
        infoHeader.biCompression = BI_RGB;
        WinGDI.BITMAPINFO info = new WinGDI.BITMAPINFO();
        info.bmiHeader = infoHeader;
        buffer.setInfo(info);

        int size = (width * height) * buffer.getBytesPerPixel();
        SIZE_T bitmapMemorySize = new SIZE_T(size);

        Pointer memory = IKERNEL32.VirtualAlloc(Pointer.NULL, bitmapMemorySize, MEM_COMMIT, PAGE_READWRITE);
        buffer.setMemory(memory);
    }

    private void run() throws IOException, UnsupportedAudioFileException {
        win32resizeDIBSection(globalBackBuffer, 1280, 740);
        // register window class
        ATOM registerClassResult = IUSER32.RegisterClassEx(windowClass);
        if (registerClassResult != null) {
            if (registerClassResult.intValue() != 0) {
                HWND window = IUSER32.CreateWindowEx(0, windowClass.lpszClassName, "Java JNA Graphics & Sound Experiment",
                        WS_OVERLAPPEDWINDOW | WS_VISIBLE,
                        CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
                        null, null, windowClass.hInstance, null);
                if (window != null) {
                    running = true;
                    int xOffset = 0;
                    int yOffset = 0;
                    String currentDir = System.getProperty("user.dir");
                    System.out.println("Current dir using System:" + currentDir);
                    File audioFile = new File("src/se/abjorklund/win32/jna/xaudio2/Ring09.wav");

                    AudioInputStream audioInputStream = AudioSystem
                            .getAudioInputStream(audioFile.getAbsoluteFile());

                    int available = audioInputStream.available();
                    byte[] buffer = new byte[available];

                    audioInputStream.read(buffer);


                    //D_SOUND_JNI.initDSound();
                    X_AUDIO_2_JNI.initXAudio2(buffer);

                    /*byte[] newBuffer = Arrays.copyOf(buffer, available);

                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(newBuffer);
                    AudioInputStream newInputStream = new AudioInputStream(byteArrayInputStream, audioInputStream.getFormat(), audioInputStream.getFrameLength());

                    Clip clip = AudioSystem.getClip();
                    clip.open(newInputStream);
                    clip.start();*/

                    LARGE_INTEGER lastCounter = new LARGE_INTEGER();
                    IKERNEL32.QueryPerformanceCounter(lastCounter);

                    int hz = 256;
                    int squareWaveCounter = 0;
                    int squareWavePeriod = 48000 / hz;

                    while (running) {
                        MSG message = new MSG();
                        while (IUSER32.PeekMessage(message, null, 0, 0, PM_REMOVE)) {

                            if (message.message == WM_QUIT) {
                                running = false;
                            }

                            IUSER32.TranslateMessage(message);
                            IUSER32.DispatchMessage(message);
                        }

                        if (XINPUT != null) {
                            for (int controllerIndex = 0; controllerIndex < XUSER_MAX_COUNT; ++controllerIndex) {
                                XInputState controllerState = new XInputState();
                                if (XINPUT.XInputGetState(controllerIndex, controllerState) == ERROR_SUCCESS) {
                                    XInputGamepad gamepad = controllerState.Gamepad;
                                    short wButtons = gamepad.wButtons;

                                    int up = (wButtons & XINPUT_GAMEPAD_DPAD_UP);
                                    int down = (wButtons & XINPUT_GAMEPAD_DPAD_DOWN);
                                    int left = (wButtons & XINPUT_GAMEPAD_DPAD_LEFT);
                                    int right = (wButtons & XINPUT_GAMEPAD_DPAD_RIGHT);
                                    int start = (wButtons & XINPUT_GAMEPAD_START);
                                    int back = (wButtons & XINPUT_GAMEPAD_BACK);
                                    int leftShoulder = (wButtons & XINPUT_GAMEPAD_LEFT_SHOULDER);
                                    int rightShoulder = (wButtons & XINPUT_GAMEPAD_RIGHT_SHOULDER);
                                    int aButton = (wButtons & XINPUT_GAMEPAD_A);
                                    int bButton = (wButtons & XINPUT_GAMEPAD_B);
                                    int xButton = (wButtons & XINPUT_GAMEPAD_X);
                                    int yButton = (wButtons & XINPUT_GAMEPAD_Y);

                                    short stickLX = gamepad.sThumbLX;
                                    short stickLY = gamepad.sThumbLY;
                                    short stickRX = gamepad.sThumbRX;
                                    short stickRY = gamepad.sThumbRY;

                                    if (aButton != 0) {
                                        XInputVibration vibration = new XInputVibration();
                                        vibration.wLeftMotorSpeed = 60000;
                                        vibration.wRightMotorSpeed = 60000;
                                        XINPUT.XInputSetState(0, vibration);
                                    }

                                    if (aButton == 0) {
                                        XInputVibration vibration = new XInputVibration();
                                        vibration.wLeftMotorSpeed = 0;
                                        vibration.wRightMotorSpeed = 0;
                                        XINPUT.XInputSetState(0, vibration);
                                    }

                                    if (stickLX != 0) {
                                        xOffset += stickLX * 5;
                                    }
                                    if (stickLY != 0) {
                                        yOffset -= stickLY * 5;
                                    }
                                }
                            }
                        }
                        GameOffscreenBuffer gameOffscreenBuffer = getGameOffscreenBuffer();
                        game.gameUpdateAndRender(gameOffscreenBuffer, xOffset, yOffset);

                        HDC deviceContext = IUSER32.GetDC(window);
                        Win32WindowDimension dimension = getWindowDimension(window);

                        win32DisplayBufferInWindow(globalBackBuffer, deviceContext,
                                dimension.getWidth(), dimension.getHeight(),
                                0, 0, dimension.getWidth(), dimension.getHeight());

                        LARGE_INTEGER endCounter = new LARGE_INTEGER();
                        IKERNEL32.QueryPerformanceCounter(endCounter);

                        if (false) {
                            long counterElapsed = endCounter.getValue() - lastCounter.getValue();
                            long msPerFrame = (1000 * counterElapsed) / PERF_COUNTER_FREQUENCY.getValue();
                            long FPS = PERF_COUNTER_FREQUENCY.getValue() / counterElapsed;
                            System.out.println("Milliseconds per frame: " + msPerFrame + ". " + "FPS: " + FPS);
                            lastCounter = endCounter;
                        }
                    }
                }
            } else {
                //TODO(anders): logging
            }
        } else {
            //TODO(anders): logging
        }
    }

    private GameOffscreenBuffer getGameOffscreenBuffer() {
        GameOffscreenBuffer gameOffscreenBuffer = new GameOffscreenBuffer();
        gameOffscreenBuffer.setBytesPerPixel(globalBackBuffer.getBytesPerPixel());
        gameOffscreenBuffer.setMemory(globalBackBuffer.getMemory());
        gameOffscreenBuffer.setWidth(globalBackBuffer.getWidth());
        gameOffscreenBuffer.setHeight(globalBackBuffer.getHeight());
        gameOffscreenBuffer.setPitch(globalBackBuffer.getPitch());
        return gameOffscreenBuffer;
    }

    private void win32DisplayBufferInWindow(Win32OffscreenBuffer buffer, HDC deviceContext, int windowWidth, int windowHeight,
                                            int x, int y, int width, int height) {
        IGDI32.StretchDIBits(deviceContext,
                /*x, y, width, height,
                x, y, width, height,*/
                0, 0, windowWidth, windowHeight,
                0, 0, buffer.getWidth(), buffer.getHeight(),
                buffer.getMemory(), buffer.getInfo(), new UINT(WinGDI.DIB_RGB_COLORS), SRCCOPY);
    }

    Win32WindowDimension getWindowDimension(HWND window) {
        Win32WindowDimension result = new Win32WindowDimension();

        RECT clientRect = new RECT();
        IUSER32.GetClientRect(window, clientRect);
        result.setWidth(clientRect.right - clientRect.left);
        result.setHeight(clientRect.bottom - clientRect.top);

        return result;
    }

    public LRESULT callback(HWND window, int message, WPARAM wParam, LPARAM lParam) {
        LRESULT result = new LRESULT(0);
        int vkCode = wParam.intValue();
        long lParamValue = lParam.intValue();
        boolean wasDown = ((lParamValue & (1 << 30)) != 0);
        boolean isDown = ((lParamValue & (1 << 31)) == 0);
        boolean altKeyWasDown = ((lParamValue & (1 << 29)) != 0);
        switch (message) {
            case WM_SIZE: {
                System.out.println("WM_SIZE");
            }
            break;
            case WM_DESTROY: {
                running = false;
                System.out.println("WM_DESTROY");
            }
            break;
            case WM_SYSKEYDOWN: {

            }
            break;
            case WM_SYSKEYUP: {

            }
            break;
            case WM_KEYDOWN: {
                if (vkCode == VK_ESCAPE) {
                    System.out.print("ESCAPE ");
                    if (isDown) {
                        System.out.println("isDown");
                    }
                } else if ((vkCode == VK_F4) && altKeyWasDown) {
                    running = false;
                }
            }
            break;
            case WM_KEYUP: {
                if (vkCode == 'W') {
                } else if (vkCode == 'A') {
                } else if (vkCode == 'S') {
                } else if (vkCode == 'D') {
                } else if (vkCode == 'Q') {
                } else if (vkCode == 'E') {
                } else if (vkCode == VK_UP) {
                } else if (vkCode == VK_DOWN) {
                } else if (vkCode == VK_LEFT) {
                } else if (vkCode == VK_RIGHT) {
                } else if (vkCode == VK_ESCAPE) {
                    System.out.print("ESCAPE ");
                    if (isDown) {
                        System.out.println("isDown");
                    }
                    if (wasDown) {
                        System.out.println("wasDown");
                    }
                } else if (vkCode == VK_SPACE) {
                }

                int uh = lParam.intValue() & (1 << 30);
            }
            break;
            case WM_CLOSE: {
                running = false;
                System.out.println("WM_CLOSE");
            }
            break;
            case WM_ACTIVATE: {
                System.out.println("WM_ACTIVATE");
            }
            break;
            case WM_PAINT: {
                PAINTSTRUCT paintstruct = new PAINTSTRUCT();
                HDC deviceContext = IUser32.INSTANCE.BeginPaint(window, paintstruct);
                int x = paintstruct.rcPaint.left;
                int y = paintstruct.rcPaint.top;
                int width = paintstruct.rcPaint.left - paintstruct.rcPaint.right;
                int height = paintstruct.rcPaint.bottom - paintstruct.rcPaint.top;

                Win32WindowDimension dimension = getWindowDimension(window);
                win32DisplayBufferInWindow(globalBackBuffer, deviceContext, dimension.getWidth(), dimension.getHeight(), x, y, width, height);

                IUSER32.EndPaint(window, paintstruct);
            }
            break;
            default: {
                //System.out.println("DEFAULT");
                result = IUSER32.DefWindowProc(window, message, wParam, lParam);
            }
            break;
        }
        return result;
    }

    public int getLastError() {
        int rc = IKERNEL32.GetLastError();

        if (rc != 0)
            System.out.println("error: " + rc);

        return rc;
    }

}