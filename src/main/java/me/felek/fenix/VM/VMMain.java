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

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class VMMain {

    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    public static final int CONSOLE_COLUMNS = 80;
    public static final int CONSOLE_ROWS = 28;

    public static void main(String[] args) throws Exception {
        InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "FenixVM");
        SetTargetFPS(60);

        Terminal terminal = new Terminal(CONSOLE_COLUMNS, CONSOLE_ROWS);
        terminal.calculateSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        Font font = LoadFontEx("arial.ttf", terminal.getFontSize(), (IntBuffer) null, 0);

        TerminalIO terminalIO = new TerminalIO(terminal);

        FenixAssembler vm = new FenixAssembler(65536);
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

        List<Token> tokens = Lexer.tokenize(source);
        int[] bytecode = Interpreter.compile(tokens);

        Thread vmThread = new Thread(() -> vm.start(bytecode));
        vmThread.start();

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

            terminal.draw(font);

            EndDrawing();
        }

        vmThread.interrupt();
        UnloadFont(font);
        CloseWindow();
    }
}
