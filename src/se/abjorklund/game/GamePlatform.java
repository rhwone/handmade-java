package se.abjorklund.game;

import se.abjorklund.game.controller.GameButtonState;
import se.abjorklund.game.controller.GameControllerInput;
import se.abjorklund.win32.JNIPlatform;

public class GamePlatform {
    private static final JNIPlatform JNI_PLATFORM = new JNIPlatform();
    private static Game game;


    public static void main(String[] args) {
        game = new Game();
        JNI_PLATFORM.start();
    }

    public static byte[] win32platform_gameUpdateAndRender(int moveUpHalfTransitionCount,
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
                                                           float stickAverageY
    ) {
        GameControllerInput gameControllerInput = mapScalarsToGameControllerInput(moveUpHalfTransitionCount,
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

        return game.renderWeirdGradient(gameControllerInput);
    }

    public static short[] win32platform_getSoundSamples(int sampleCount, int samplesPerSecond, int toneHz) {
        return game.outputSound(sampleCount, samplesPerSecond, toneHz);
    }

    private static GameControllerInput mapScalarsToGameControllerInput(int moveUpHalfTransitionCount,
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
