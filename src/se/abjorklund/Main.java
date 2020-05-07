//package se.abjorklund;
//
//import com.sun.jna.Library;
//import com.sun.jna.Native;
//import com.sun.jna.Platform;
//import com.sun.jna.Structure;
//import com.sun.jna.win32.StdCallLibrary;
//import se.abjorklund.win32.Win32;
//import se.abjorklund.win32.Win32State;
//
//public class Main {
//
//    public interface CLibrary extends Library {
//        CLibrary INSTANCE = (CLibrary)
//                Native.load((Platform.isWindows() ? "msvcrt" : "c"),
//                        CLibrary.class);
//
//        void printf(String format, Object... args);
//    }
//
//    public interface Kernel32 extends StdCallLibrary {
//        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
//
//        @Structure.FieldOrder({ "wYear", "wMonth", "wDayOfWeek", "wDay", "wHour", "wMinute", "wSecond", "wMilliseconds" })
//        class SYSTEMTIME extends Structure {
//            public short wYear;
//            public short wMonth;
//            public short wDayOfWeek;
//            public short wDay;
//            public short wHour;
//            public short wMinute;
//            public short wSecond;
//            public short wMilliseconds;
//        }
//
//        void GetSystemTime(SYSTEMTIME result);
//    }
//
//
//
//    public static void main(String[] args) {
//        CLibrary.INSTANCE.printf("Hello, World\n");
//        for (int i=0;i < args.length;i++) {
//            CLibrary.INSTANCE.printf("Argument %d: %s\n", i, args[i]);
//        }
//
//        Kernel32 k32 = Kernel32.INSTANCE;
//
//        Kernel32.SYSTEMTIME result = new Kernel32.SYSTEMTIME();
//        k32.GetSystemTime(result);
//
//        CLibrary.INSTANCE.printf("" + result.wYear + "-" + result.wMonth + "-" + result.wDay);
//
//
//        Win32State win32State = new Win32State();
//
//        Win32 win32 = new Win32();
//
//    }
//}
