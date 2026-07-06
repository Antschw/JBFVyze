package fr.antschw.bfv.infrastructure.window;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMENU;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Supprime la caption native d'un Stage JavaFX {@code DECORATED} sous Windows
 * tout en conservant l'intégralité des comportements natifs (Snap Layouts,
 * resize par les bords, ombres DWM, animations minimize/maximize, Aero Snap,
 * menu système).
 * <p>
 * Principe (identique à FlatLaf/IntelliJ/Chromium) : la fenêtre garde son style
 * {@code WS_OVERLAPPEDWINDOW}, mais sa WndProc est sous-classée pour :
 * <ul>
 *   <li>{@code WM_NCCALCSIZE} : étendre la zone client jusqu'en haut de la
 *       fenêtre (la caption disparaît, les bordures gauche/droite/bas restent
 *       gérées nativement) ;</li>
 *   <li>{@code WM_NCHITTEST} : redonner à Windows la sémantique des zones
 *       (HTCAPTION pour le drag, HTTOP pour le resize haut, HTMINBUTTON /
 *       HTMAXBUTTON / HTCLOSE au-dessus des boutons JavaFX — c'est ce qui
 *       déclenche le flyout Snap Layouts de Windows 11).</li>
 * </ul>
 * Les clics et le survol en zone non-client n'atteignent jamais JavaFX : ils
 * sont interceptés ici et relayés à {@link TitleBarMetrics}.
 * <p>
 * Tous les appels publics doivent se faire sur le FX Application Thread —
 * c'est aussi le thread sur lequel Glass pompe les messages Win32, donc la
 * WndProc peut lire les bounds JavaFX sans synchronisation.
 */
public final class WindowsTitleBarDecoration {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsTitleBarDecoration.class);

    // Messages Win32
    private static final int WM_DESTROY = 0x0002;
    private static final int WM_GETMINMAXINFO = 0x0024;
    private static final int WM_WINDOWPOSCHANGING = 0x0046;
    private static final int WM_WINDOWPOSCHANGED = 0x0047;
    private static final int WM_SYSCOMMAND = 0x0112;
    private static final int WM_MOUSEMOVE = 0x0200;
    private static final int WM_ENTERSIZEMOVE = 0x0231;
    private static final int WM_EXITSIZEMOVE = 0x0232;
    private static final int WM_NCCALCSIZE = 0x0083;
    private static final int WM_NCHITTEST = 0x0084;
    private static final int WM_NCMOUSEMOVE = 0x00A0;
    private static final int WM_NCLBUTTONDOWN = 0x00A1;
    private static final int WM_NCLBUTTONUP = 0x00A2;
    private static final int WM_NCLBUTTONDBLCLK = 0x00A3;
    private static final int WM_NCRBUTTONUP = 0x00A5;
    private static final int WM_NCMOUSELEAVE = 0x02A2;

    // Codes de hit-test
    private static final int HTCLIENT = TitleBarMetrics.HTCLIENT;
    private static final int HTCAPTION = TitleBarMetrics.HTCAPTION;
    private static final int HTTOP = 12;
    private static final int HTTOPLEFT = 13;
    private static final int HTTOPRIGHT = 14;

    // Divers Win32
    private static final int GWLP_WNDPROC = -4;
    private static final int SM_CYSIZEFRAME = 33;
    private static final int SM_CXPADDEDBORDER = 92;
    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;
    private static final int SWP_NOZORDER = 0x0004;
    private static final int SWP_FRAMECHANGED = 0x0020;
    private static final int TPM_RIGHTBUTTON = 0x0002;
    private static final int TPM_RETURNCMD = 0x0100;
    private static final int TME_LEAVE = 0x0002;
    private static final int TME_NONCLIENT = 0x0010;
    private static final int MF_BYCOMMAND = 0x0000;
    private static final int MF_ENABLED = 0x0000;
    private static final int MF_GRAYED = 0x0001;
    private static final int SC_SIZE = 0xF000;
    private static final int SC_MOVE = 0xF010;
    private static final int SC_MAXIMIZE = 0xF030;
    private static final int SC_RESTORE = 0xF120;
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

    /** Trace détaillée de la WndProc : -Dbfvyze.titlebar.trace=true ou BFVYZE_TITLEBAR_TRACE=true. */
    private static final boolean TRACE = flag("bfvyze.titlebar.trace", "BFVYZE_TITLEBAR_TRACE", false);
    /** Kill-switch : -Dbfvyze.titlebar.native=false ou BFVYZE_TITLEBAR_NATIVE=false → barre système standard. */
    private static final boolean ENABLED = flag("bfvyze.titlebar.native", "BFVYZE_TITLEBAR_NATIVE", true);
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger("titlebar.trace");

    private Stage stage;
    private TitleBarMetrics metrics;
    private HWND hwnd;
    private Pointer defaultWndProc;
    /** Référence forte obligatoire : sans elle le callback JNA est collecté par le GC → crash natif. */
    private WinUser.WindowProc wndProc;
    private int pressedHitTest = TitleBarMetrics.HTNOWHERE;
    /** Vrai pendant la boucle modale de move/size native (drag de la fenêtre, resize). */
    private boolean inSizeMove;
    private boolean installed;

    /** Indique si la personnalisation native est possible sur cette machine. */
    public static boolean isSupported() {
        return ENABLED && System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static boolean flag(String property, String envVar, boolean defaultValue) {
        String value = System.getProperty(property, System.getenv(envVar));
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Installe la décoration. À appeler sur le FX Application Thread, après
     * {@code stage.show()}.
     *
     * @throws IllegalStateException si le HWND est introuvable ou le
     *                               sous-classement impossible
     */
    public void install(Stage stage, TitleBarMetrics metrics) {
        if (installed) {
            throw new IllegalStateException("Decoration already installed");
        }
        this.stage = stage;
        this.metrics = metrics;
        this.hwnd = findStageHwnd(stage);

        this.wndProc = this::handleMessage;
        this.defaultWndProc = User32Ex.INSTANCE.SetWindowLongPtr(hwnd, GWLP_WNDPROC, wndProc);
        if (defaultWndProc == null) {
            throw new IllegalStateException("SetWindowLongPtr(GWLP_WNDPROC) failed");
        }
        installed = true;

        // Force le recalcul du frame pour que WM_NCCALCSIZE soit rejoué immédiatement.
        User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
        LOGGER.info("Native title bar decoration installed (hwnd={})", hwnd);
    }

    /** Synchronise le frame DWM (micro-bordure, transitions) avec le thème. */
    public void setDarkMode(boolean dark) {
        if (!installed) {
            return;
        }
        IntByReference value = new IntByReference(dark ? 1 : 0);
        int hr = DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, value, 4);
        if (hr != 0) {
            LOGGER.warn("DwmSetWindowAttribute(DWMWA_USE_IMMERSIVE_DARK_MODE) returned 0x{}", Integer.toHexString(hr));
        }
    }

    /** Restaure la WndProc d'origine (appelé aussi automatiquement sur WM_DESTROY). */
    public void uninstall() {
        if (installed) {
            User32Ex.INSTANCE.SetWindowLongPtr(hwnd, GWLP_WNDPROC, defaultWndProc);
            installed = false;
        }
    }

    // ------------------------------------------------------------------
    // WndProc
    // ------------------------------------------------------------------

    private LRESULT handleMessage(HWND hwnd, int msg, WPARAM wParam, LPARAM lParam) {
        try {
            return switch (msg) {
                case WM_NCCALCSIZE -> onNcCalcSize(msg, wParam, lParam);
                case WM_NCHITTEST -> onNcHitTest(msg, wParam, lParam);
                case WM_NCMOUSEMOVE -> onNcMouseMove(msg, wParam, lParam);
                case WM_MOUSEMOVE, WM_NCMOUSELEAVE -> {
                    pressedHitTest = TitleBarMetrics.HTNOWHERE;
                    metrics.fireHover(TitleBarMetrics.HTNOWHERE);
                    yield callDefault(msg, wParam, lParam);
                }
                case WM_NCLBUTTONDOWN, WM_NCLBUTTONDBLCLK -> onNcLeftButtonDown(msg, wParam, lParam);
                case WM_NCLBUTTONUP -> onNcLeftButtonUp(msg, wParam, lParam);
                case WM_NCRBUTTONUP -> onNcRightButtonUp(msg, wParam, lParam);
                case WM_ENTERSIZEMOVE, WM_EXITSIZEMOVE -> {
                    // Pendant la boucle modale de move/size, nos interceptions NC
                    // sont désactivées : le relâchement qui termine un drag ne doit
                    // jamais être interprété comme un clic sur un bouton caption.
                    inSizeMove = msg == WM_ENTERSIZEMOVE;
                    pressedHitTest = TitleBarMetrics.HTNOWHERE;
                    trace(() -> msg == WM_ENTERSIZEMOVE ? "WM_ENTERSIZEMOVE" : "WM_EXITSIZEMOVE");
                    yield callDefault(msg, wParam, lParam);
                }
                case WM_WINDOWPOSCHANGING -> onWindowPosChanging(msg, wParam, lParam);
                case WM_WINDOWPOSCHANGED -> {
                    trace(() -> describeWindowPos(msg, lParam));
                    yield callDefault(msg, wParam, lParam);
                }
                case WM_GETMINMAXINFO -> {
                    LRESULT result = callDefault(msg, wParam, lParam);
                    trace(() -> describeMinMaxInfo(lParam));
                    yield result;
                }
                case WM_SYSCOMMAND -> {
                    trace(() -> String.format("WM_SYSCOMMAND wParam=0x%X", wParam.intValue()));
                    yield callDefault(msg, wParam, lParam);
                }
                case WM_DESTROY -> {
                    Pointer original = defaultWndProc;
                    uninstall();
                    yield User32Ex.INSTANCE.CallWindowProc(original, hwnd, msg, wParam, lParam);
                }
                default -> callDefault(msg, wParam, lParam);
            };
        } catch (RuntimeException e) {
            // Une exception qui remonte dans du code natif serait fatale : on
            // se rabat sur le comportement par défaut.
            LOGGER.error("WndProc failure on msg 0x{}", Integer.toHexString(msg), e);
            return callDefault(msg, wParam, lParam);
        }
    }

    /**
     * Étend la zone client jusqu'au bord haut de la fenêtre : la caption
     * native disparaît, les bordures gauche/droite/bas restent celles
     * calculées par la WndProc d'origine (resize et ombre DWM intacts).
     */
    private LRESULT onNcCalcSize(int msg, WPARAM wParam, LPARAM lParam) {
        if (wParam.intValue() == 0) {
            return callDefault(msg, wParam, lParam);
        }
        // lParam → NCCALCSIZE_PARAMS, dont le premier membre est rgrc[0] (RECT
        // proposé : left/top/right/bottom en ints). On ne touche qu'au top.
        Pointer rect = new Pointer(lParam.longValue());
        int proposedTop = rect.getInt(4);
        callDefault(msg, wParam, lParam);
        // Maximisée, la fenêtre déborde de l'écran de l'épaisseur du frame :
        // on réinjecte cet offset pour ne pas rogner le haut de l'UI.
        rect.setInt(4, isZoomed() ? proposedTop + frameThicknessPhysical() : proposedTop);
        return new LRESULT(0);
    }

    private LRESULT onNcHitTest(int msg, WPARAM wParam, LPARAM lParam) {
        LRESULT def = callDefault(msg, wParam, lParam);
        if (def.intValue() != HTCLIENT) {
            // Bordures gauche/droite/bas : comportement natif inchangé.
            return def;
        }
        long packed = lParam.longValue();
        // Extraction signée : les coordonnées écran peuvent être négatives (multi-écrans).
        POINT point = new POINT((short) (packed & 0xFFFF), (short) ((packed >>> 16) & 0xFFFF));
        User32Ex.INSTANCE.ScreenToClient(hwnd, point);

        // Le haut de la zone client couvre désormais le frame de resize.
        if (!isZoomed() && point.y >= 0 && point.y < frameThicknessPhysical()) {
            RECT client = new RECT();
            User32Ex.INSTANCE.GetClientRect(hwnd, client);
            int border = frameThicknessPhysical();
            if (point.x < border) {
                return new LRESULT(HTTOPLEFT);
            }
            if (point.x > client.right - border) {
                return new LRESULT(HTTOPRIGHT);
            }
            return new LRESULT(HTTOP);
        }

        double sceneX = point.x / stage.getOutputScaleX();
        double sceneY = point.y / stage.getOutputScaleY();
        return new LRESULT(metrics.hitTest(sceneX, sceneY));
    }

    /**
     * Protège les redimensionnements imposés par le shell pendant un drag
     * (Aero Snap au relâchement) contre la réécriture de Glass.
     * <p>
     * Sur WM_WINDOWPOSCHANGING, Glass (JavaFX) applique sa logique de
     * transition d'écran ({@code WinWindow.notifyMoving}) : pendant un
     * déplacement, si le rectangle cible chevauche la frontière de deux
     * moniteurs, il recalcule la taille à partir de la taille FX courante et
     * écrase celle proposée — le snap est alors déplacé mais jamais
     * redimensionné. Un drag utilisateur ne change jamais la taille de
     * lui-même : toute taille proposée pendant {@code inSizeMove} vient du
     * shell (snap) et doit être appliquée telle quelle, donc on restaure le
     * rectangle original si Glass l'a modifié. Hors drag (resize aux bords,
     * Win+Flèches, vrai changement de DPI), Glass garde son comportement.
     */
    private LRESULT onWindowPosChanging(int msg, WPARAM wParam, LPARAM lParam) {
        trace(() -> describeWindowPos(msg, lParam));
        Pointer windowPos = new Pointer(lParam.longValue());
        int x = windowPos.getInt(16);
        int y = windowPos.getInt(20);
        int cx = windowPos.getInt(24);
        int cy = windowPos.getInt(28);
        int flags = windowPos.getInt(32);

        LRESULT result = callDefault(msg, wParam, lParam);

        // Un tick de déplacement porte aussi cx/cy (inchangés) : seul un écart
        // avec la taille actuelle signale un vrai resize imposé par le shell.
        boolean shellResize = false;
        if (inSizeMove && (flags & SWP_NOSIZE) == 0) {
            RECT current = new RECT();
            User32.INSTANCE.GetWindowRect(hwnd, current);
            shellResize = cx != current.right - current.left || cy != current.bottom - current.top;
        }
        if (shellResize) {
            boolean altered = windowPos.getInt(16) != x || windowPos.getInt(20) != y
                    || windowPos.getInt(24) != cx || windowPos.getInt(28) != cy
                    || windowPos.getInt(32) != flags;
            if (altered) {
                trace(() -> String.format(
                        "Glass altered shell-initiated resize during drag, restoring x=%d y=%d cx=%d cy=%d", x, y, cx, cy));
                windowPos.setInt(16, x);
                windowPos.setInt(20, y);
                windowPos.setInt(24, cx);
                windowPos.setInt(28, cy);
                windowPos.setInt(32, flags);
            }
        }
        return result;
    }

    private LRESULT onNcMouseMove(int msg, WPARAM wParam, LPARAM lParam) {
        if (inSizeMove) {
            return callDefault(msg, wParam, lParam);
        }
        int hitTest = wParam.intValue();
        metrics.fireHover(isCaptionButton(hitTest) ? hitTest : TitleBarMetrics.HTNOWHERE);
        // Nécessaire pour recevoir WM_NCMOUSELEAVE quand la souris quitte la barre.
        User32Ex.TRACKMOUSEEVENT tme = new User32Ex.TRACKMOUSEEVENT();
        tme.cbSize = tme.size();
        tme.dwFlags = TME_LEAVE | TME_NONCLIENT;
        tme.hwndTrack = hwnd;
        User32Ex.INSTANCE.TrackMouseEvent(tme);
        return callDefault(msg, wParam, lParam);
    }

    private LRESULT onNcLeftButtonDown(int msg, WPARAM wParam, LPARAM lParam) {
        int hitTest = wParam.intValue();
        if (!inSizeMove && isCaptionButton(hitTest)) {
            pressedHitTest = hitTest;
            trace(() -> "WM_NCLBUTTONDOWN consumed, ht=" + hitTest);
            return new LRESULT(0);
        }
        return callDefault(msg, wParam, lParam);
    }

    private LRESULT onNcLeftButtonUp(int msg, WPARAM wParam, LPARAM lParam) {
        int hitTest = wParam.intValue();
        // Ne consommer le relâchement que si le down correspondant a été vu :
        // un up orphelin (fin de drag, fin de boucle modale…) doit suivre le
        // chemin natif pour ne pas perturber Aero Snap.
        if (!inSizeMove && isCaptionButton(hitTest) && hitTest == pressedHitTest) {
            pressedHitTest = TitleBarMetrics.HTNOWHERE;
            trace(() -> "WM_NCLBUTTONUP consumed, action ht=" + hitTest);
            // Sortir du dispatch natif avant de toucher au Stage.
            Platform.runLater(() -> metrics.fireAction(hitTest));
            return new LRESULT(0);
        }
        pressedHitTest = TitleBarMetrics.HTNOWHERE;
        return callDefault(msg, wParam, lParam);
    }

    /** Clic droit sur la barre → menu système natif, comme une fenêtre standard. */
    private LRESULT onNcRightButtonUp(int msg, WPARAM wParam, LPARAM lParam) {
        if (inSizeMove || wParam.intValue() != HTCAPTION) {
            return callDefault(msg, wParam, lParam);
        }
        long packed = lParam.longValue();
        int screenX = (short) (packed & 0xFFFF);
        int screenY = (short) ((packed >>> 16) & 0xFFFF);

        HMENU menu = User32Ex.INSTANCE.GetSystemMenu(hwnd, false);
        boolean zoomed = isZoomed();
        User32Ex.INSTANCE.EnableMenuItem(menu, SC_RESTORE, MF_BYCOMMAND | (zoomed ? MF_ENABLED : MF_GRAYED));
        User32Ex.INSTANCE.EnableMenuItem(menu, SC_MAXIMIZE, MF_BYCOMMAND | (zoomed ? MF_GRAYED : MF_ENABLED));
        User32Ex.INSTANCE.EnableMenuItem(menu, SC_SIZE, MF_BYCOMMAND | (zoomed ? MF_GRAYED : MF_ENABLED));
        User32Ex.INSTANCE.EnableMenuItem(menu, SC_MOVE, MF_BYCOMMAND | (zoomed ? MF_GRAYED : MF_ENABLED));

        int command = User32Ex.INSTANCE.TrackPopupMenu(
                menu, TPM_RETURNCMD | TPM_RIGHTBUTTON, screenX, screenY, 0, hwnd, null);
        if (command != 0) {
            User32.INSTANCE.PostMessage(hwnd, WM_SYSCOMMAND, new WPARAM(command), new LPARAM(0));
        }
        return new LRESULT(0);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private LRESULT callDefault(int msg, WPARAM wParam, LPARAM lParam) {
        return User32Ex.INSTANCE.CallWindowProc(defaultWndProc, hwnd, msg, wParam, lParam);
    }

    private static void trace(Supplier<String> message) {
        if (TRACE) {
            TRACE_LOGGER.info("{}", message.get());
        }
    }

    /** Décode la structure WINDOWPOS pointée par lParam (layout x64). */
    private static String describeWindowPos(int msg, LPARAM lParam) {
        Pointer p = new Pointer(lParam.longValue());
        int x = p.getInt(16);
        int y = p.getInt(20);
        int cx = p.getInt(24);
        int cy = p.getInt(28);
        int flags = p.getInt(32);
        StringBuilder flagNames = new StringBuilder();
        if ((flags & SWP_NOSIZE) != 0) flagNames.append(" NOSIZE");
        if ((flags & SWP_NOMOVE) != 0) flagNames.append(" NOMOVE");
        if ((flags & SWP_NOZORDER) != 0) flagNames.append(" NOZORDER");
        if ((flags & SWP_FRAMECHANGED) != 0) flagNames.append(" FRAMECHANGED");
        return String.format("%s x=%d y=%d cx=%d cy=%d flags=0x%X%s",
                msg == WM_WINDOWPOSCHANGING ? "WM_WINDOWPOSCHANGING" : "WM_WINDOWPOSCHANGED",
                x, y, cx, cy, flags, flagNames);
    }

    /** Décode la structure MINMAXINFO pointée par lParam (layout x64). */
    private static String describeMinMaxInfo(LPARAM lParam) {
        Pointer p = new Pointer(lParam.longValue());
        return String.format(
                "WM_GETMINMAXINFO maxSize=%dx%d maxPos=(%d,%d) minTrack=%dx%d maxTrack=%dx%d",
                p.getInt(8), p.getInt(12), p.getInt(16), p.getInt(20),
                p.getInt(24), p.getInt(28), p.getInt(32), p.getInt(36));
    }

    private static boolean isCaptionButton(int hitTest) {
        return hitTest == TitleBarMetrics.HTMINBUTTON
                || hitTest == TitleBarMetrics.HTMAXBUTTON
                || hitTest == TitleBarMetrics.HTCLOSE;
    }

    private boolean isZoomed() {
        return User32Ex.INSTANCE.IsZoomed(hwnd);
    }

    /** Épaisseur physique (px) du frame de resize, incluant le padded border. */
    private int frameThicknessPhysical() {
        return User32.INSTANCE.GetSystemMetrics(SM_CYSIZEFRAME)
                + User32.INSTANCE.GetSystemMetrics(SM_CXPADDEDBORDER);
    }

    /**
     * Retrouve le HWND du Stage sans toucher aux internals JavaFX : titre
     * temporairement unique + FindWindow, avec vérification du PID.
     */
    private static HWND findStageHwnd(Stage stage) {
        String originalTitle = stage.getTitle();
        String probe = "bfvyze-hwnd-" + UUID.randomUUID();
        stage.setTitle(probe);
        try {
            HWND hwnd = User32.INSTANCE.FindWindow(null, probe);
            if (hwnd == null) {
                throw new IllegalStateException("Stage HWND not found via FindWindow");
            }
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
            if (pid.getValue() != Kernel32.INSTANCE.GetCurrentProcessId()) {
                throw new IllegalStateException("FindWindow matched a foreign process window");
            }
            return hwnd;
        } finally {
            stage.setTitle(originalTitle);
        }
    }

    // ------------------------------------------------------------------
    // Bindings JNA
    // ------------------------------------------------------------------

    /** Fonctions user32 absentes (ou incertaines) du User32 de jna-platform. */
    private interface User32Ex extends StdCallLibrary {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        Pointer SetWindowLongPtr(HWND hwnd, int index, WinUser.WindowProc wndProc);

        Pointer SetWindowLongPtr(HWND hwnd, int index, Pointer wndProc);

        LRESULT CallWindowProc(Pointer prevWndProc, HWND hwnd, int msg, WPARAM wParam, LPARAM lParam);

        boolean ScreenToClient(HWND hwnd, POINT point);

        boolean GetClientRect(HWND hwnd, RECT rect);

        boolean IsZoomed(HWND hwnd);

        HMENU GetSystemMenu(HWND hwnd, boolean revert);

        boolean EnableMenuItem(HMENU menu, int idEnableItem, int enable);

        int TrackPopupMenu(HMENU menu, int flags, int x, int y, int reserved, HWND hwnd, Pointer rect);

        boolean TrackMouseEvent(TRACKMOUSEEVENT tme);

        @Structure.FieldOrder({"cbSize", "dwFlags", "hwndTrack", "dwHoverTime"})
        class TRACKMOUSEEVENT extends Structure {
            public int cbSize;
            public int dwFlags;
            public HWND hwndTrack;
            public int dwHoverTime;
        }
    }

    private interface DwmApi extends StdCallLibrary {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);

        int DwmSetWindowAttribute(HWND hwnd, int attribute, IntByReference value, int size);
    }
}
