package ovo.xsvf;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

public class JNAUtil {
    public static boolean tryEnableAnsiSupport() {
        WinNT.HANDLE hStdOut = Kernel32.INSTANCE.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
        if (hStdOut == null || hStdOut.equals(WinNT.INVALID_HANDLE_VALUE)) {
            return false;
        }

        IntByReference modeRef = new IntByReference(0);
        if (!Kernel32.INSTANCE.GetConsoleMode(hStdOut, modeRef)) {
            return false;
        }

        int mode = modeRef.getValue();

        if ((mode & 0x0004) != 0) return true;
        else return Kernel32.INSTANCE.SetConsoleMode(hStdOut, mode | 0x0004);
    }
}
