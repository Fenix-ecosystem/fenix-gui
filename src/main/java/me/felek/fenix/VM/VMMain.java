package me.felek.fenix.VM;

import me.felek.fenix.asm.FenixAssembler;
import me.felek.fenix.asm.ints.InterruptionManager;
import me.felek.fenix.compiler.Interpreter;
import me.felek.fenix.compiler.Lexer;
import me.felek.fenix.compiler.Preprocessor;
import me.felek.fenix.compiler.Token;
import me.felek.fenix.gui.io.TerminalIO;

import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.raylib.Raylib.Font;
import me.felek.fenix.video.VideoMode;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class VMMain {

    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    public static final int CONSOLE_COLUMNS = 80;
    public static final int CONSOLE_ROWS = 28;

    public static final int VGA_WIDTH = 320;
    public static final int VGA_HEIGHT = 200;

    public static void main(String[] args) throws Exception {
        InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "FenixVM");
        SetTargetFPS(60);

        Terminal terminal = new Terminal(CONSOLE_COLUMNS, CONSOLE_ROWS);
        terminal.calculateSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        Font font = LoadFontEx("ARIAL.ttf", terminal.getFontSize(), (IntBuffer) null, 0);

        TerminalIO terminalIO = new TerminalIO(terminal);

        FenixAssembler vm = new FenixAssembler(262144);
        vm.attachIO(terminalIO);

        InterruptionManager im = new InterruptionManager(vm, terminalIO);
        vm.registerInterruptionManager(im);

        String programFile = "main.fnx";

        if (args.length > 0) {
            programFile = args[0];
        }

        String source = Files.readString(Path.of(programFile));


        Preprocessor preprocessor = new Preprocessor();
        source = preprocessor.preprocess(source);
        System.out.println(source);

        List<Token> tokens = Lexer.tokenize(source);
        int[] bytecode = Interpreter.compile(tokens);

        Thread vmThread = new Thread(() -> vm.start(bytecode));
        vmThread.start();

        float scaleX = (float) SCREEN_WIDTH / VGA_WIDTH;
        float scaleY = (float) SCREEN_HEIGHT / VGA_HEIGHT;

        while (!WindowShouldClose()) {

            int key = GetCharPressed();
            while (key > 0) {
                im.pushKey(key);
                key = GetCharPressed();
            }

            if (IsKeyPressed(KEY_ENTER)) {
                im.pushKey('\n');
            }

            if (IsKeyPressed(KEY_F1)) {
                new InfoFrame(vm.getMemory(), vm.getCurrentDisk());
            }


            if (IsKeyPressed(KEY_BACKSPACE)) {
                im.pushKey('\b');
            }

            BeginDrawing();
            ClearBackground(BLACK);

            if (vm.getMode() == VideoMode.CONSOLE) {
                terminal.draw(font);
            } else if (vm.getMode() == VideoMode.VGA) {
                for (int x = 0; x < VGA_WIDTH; x++) {
                    for (int y = 0; y < VGA_HEIGHT; y++) {
                        int mem_addr = FenixAssembler.VGA_START + (y * VGA_WIDTH) + x;

                        int color = vm.getMemory().get(mem_addr);

                        if (color != 0) {
                            Color c = getVGAColor(color);

                            DrawRectangle((int) (x * scaleX),
                                    (int) (y * scaleY),
                                    (int) Math.ceil(scaleX),
                                    (int) Math.ceil(scaleY),
                                    c);
                        }
                    }
                }
            }

            EndDrawing();
        }

        vmThread.interrupt();
        UnloadFont(font);
        CloseWindow();
    }

    private static Color getVGAColor(int num) {
        switch (num) {
            case 0: return BLACK;
            case 1: return BLUE;
            case 2: return GREEN;
            case 3: return new Color().r((byte)0).g((byte)170).b((byte)170).a((byte)255);
            case 4: return RED;
            case 5: return MAGENTA;
            case 6: return BROWN;
            case 7: return LIGHTGRAY;
            case 8: return DARKGRAY;
            case 9: return new Color().r((byte)85).g((byte)85).b((byte)255).a((byte)255);
            case 10: return LIME;
            case 11: return SKYBLUE;
            case 12: return new Color().r((byte)255).g((byte)85).b((byte)85).a((byte)255);
            case 13: return PINK;
            case 14: return YELLOW;
            case 15: return WHITE;
        }
        if (num > 15) {
            int r = (num * 3) % 256;
            int g = (num * 7) % 256;
            int b = (num * 11) % 256;
            return new Color().r((byte)r).g((byte)g).b((byte)b).a((byte)255);
        }

        return MAGENTA;
    }
}
