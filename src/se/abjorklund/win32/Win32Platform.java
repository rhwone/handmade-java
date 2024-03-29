package se.abjorklund.win32;

import se.abjorklund.game.Game;
import se.abjorklund.game.controller.GameButtonState;
import se.abjorklund.game.controller.GameControllerInput;
import se.abjorklund.win32.JNIPlatform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Win32Platform {
    private static final JNIPlatform JNI_PLATFORM = new JNIPlatform();
    private static final Game game = new Game();

    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 540;
    public static final int BYTES_PER_PIXEL = 4;

    public static ByteBuffer VIDEO_BUFFER;

    public static void main(String[] args) {
        int videoBufferSize = SCREEN_WIDTH * SCREEN_HEIGHT * BYTES_PER_PIXEL;
        VIDEO_BUFFER = createVideoBuffer(videoBufferSize);

        JNI_PLATFORM.start(VIDEO_BUFFER, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private static ByteBuffer createVideoBuffer(int bufferSize) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferSize);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer;
    }

    public static void gameUpdateAndRender(
            int moveUpHalfTransitionCount,
            boolean moveUpEndedDown,
            int moveDownHalfTransitionCount,
            boolean moveDownEndedDown,
            int moveLeftHalfTransitionCount,
            boolean moveLeftEndedDown,
            int moveRightHalfTransitionCount,
            boolean moveRightEndedDown,
            int actionUpHalfTransitionCount,
            boolean actionUpEndedDown,
            int actionDownHalfTransitionCount,
            boolean actionDownEndedDown,
            int actionLeftHalfTransitionCount,
            boolean actionLeftEndedDown,
            int actionRightHalfTransitionCount,
            boolean actionRightEndedDown,
            int leftShoulderHalfTransitionCount,
            boolean leftShoulderEndedDown,
            int rightShoulderHalfTransitionCount,
            boolean rightShoulderEndedDown,
            int backHalfTransitionCount,
            boolean backEndedDown,
            int startHalfTransitionCount,
            boolean startEndedDown,
            boolean isConnected,
            boolean isAnalog,
            float stickAverageX,
            float stickAverageY,
            float dT
    ) {
        GameControllerInput gameControllerInput = mapGameControllerInput(moveUpHalfTransitionCount,
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
                stickAverageY
        );

        if (gameControllerInput.getBack().endedDown()) {
            System.exit(0);
        }

        game.DEBUG_render(gameControllerInput, dT);
    }

    public static short[] getSoundSamples(int sampleCount, int samplesPerSecond, int toneHz) {
        return new short[16]; // game.outputSound(sampleCount, samplesPerSecond, toneHz);
    }

    private static GameControllerInput mapGameControllerInput(int moveUpHalfTransitionCount,
                                                              boolean moveUpEndedDown,
                                                              int moveDownHalfTransitionCount,
                                                              boolean moveDownEndedDown,
                                                              int moveLeftHalfTransitionCount,
                                                              boolean moveLeftEndedDown,
                                                              int moveRightHalfTransitionCount,
                                                              boolean moveRightEndedDown,
                                                              int actionUpHalfTransitionCount,
                                                              boolean actionUpEndedDown,
                                                              int actionDownHalfTransitionCount,
                                                              boolean actionDownEndedDown,
                                                              int actionLeftHalfTransitionCount,
                                                              boolean actionLeftEndedDown,
                                                              int actionRightHalfTransitionCount,
                                                              boolean actionRightEndedDown,
                                                              int leftShoulderHalfTransitionCount,
                                                              boolean leftShoulderEndedDown,
                                                              int rightShoulderHalfTransitionCount,
                                                              boolean rightShoulderEndedDown,
                                                              int backHalfTransitionCount,
                                                              boolean backEndedDown,
                                                              int startHalfTransitionCount,
                                                              boolean startEndedDown,
                                                              boolean isConnected,
                                                              boolean isAnalog,
                                                              float stickAverageX,
                                                              float stickAverageY) {
        return new GameControllerInput(isConnected,
                isAnalog,
                stickAverageX,
                stickAverageY,
                mapGameButtonState(moveUpHalfTransitionCount, moveUpEndedDown),
                mapGameButtonState(moveDownHalfTransitionCount, moveDownEndedDown),
                mapGameButtonState(moveLeftHalfTransitionCount, moveLeftEndedDown),
                mapGameButtonState(moveRightHalfTransitionCount, moveRightEndedDown),
                mapGameButtonState(actionUpHalfTransitionCount, actionUpEndedDown),
                mapGameButtonState(actionDownHalfTransitionCount, actionDownEndedDown),
                mapGameButtonState(actionLeftHalfTransitionCount, actionLeftEndedDown),
                mapGameButtonState(actionRightHalfTransitionCount, actionRightEndedDown),
                mapGameButtonState(leftShoulderHalfTransitionCount, leftShoulderEndedDown),
                mapGameButtonState(rightShoulderHalfTransitionCount, rightShoulderEndedDown),
                mapGameButtonState(backHalfTransitionCount, backEndedDown),
                mapGameButtonState(startHalfTransitionCount, startEndedDown)
        );
    }

    private static GameButtonState mapGameButtonState(int halfTransitionCount, boolean endedDown) {
        return new GameButtonState(halfTransitionCount, endedDown);
    }
}
