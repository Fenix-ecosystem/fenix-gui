package me.felek.fenix.VM;

import com.raylib.Colors;
import com.raylib.Raylib;

import java.io.IOException;
import java.io.OutputStream;

import static com.raylib.Raylib.*;

public class Terminal extends OutputStream {
    private static class TerminalCharacter {
        char character = ' ';
        Raylib.Color foreground = Colors.WHITE;
        Raylib.Color background = Colors.BLACK;

        public void set(char character, Color foreground, Color background) {
            this.character = character;
            this.foreground = foreground;
            this.background = background;
        }
    }

    private final int columns;
    private final int rows;
    private final TerminalCharacter[][] screenBuffer;

    private int cursorX;
    private int cursorY;

    private Color defaultForegroundColor = Colors.LIGHTGRAY;
    private Color defaultBackgroundColor = Colors.BLACK;

    private int charCellWidth;
    private int charCellHeight;
    private int fontSize;

    public Terminal(int columns, int rows) {
        this.rows = rows;
        this.columns = columns;
        this.screenBuffer = new TerminalCharacter[rows][columns];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                screenBuffer[y][x] = new TerminalCharacter();
            }
        }
    }

    public void setCharAt(int x, int y, char ch) {
        screenBuffer[x][y] = new TerminalCharacter();
        screenBuffer[x][y].character = ch;
    }

    public void calculateSize(int screenWidthPx, int screenHeightPx) {
        if (rows <= 0 || columns <= 0) return;
        this.charCellHeight = screenHeightPx / rows;
        this.charCellWidth = screenWidthPx / columns;
        this.fontSize = this.charCellHeight;
    }

    public void clear(char ch) {
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                screenBuffer[y][x].set(ch, defaultForegroundColor, defaultBackgroundColor);
            }
        }

        cursorX = 0;
        cursorY = 0;
    }

    @Override
    public void write(int b) throws IOException {
        char ch = (char) b;

        switch (ch) {
            case '\n':
                cursorY++;
                cursorX = 0;
                break;
            case '\r':
                cursorX = 0;
                break;
            case '\b':
                if (cursorX > 0) {
                    cursorX--;
                    screenBuffer[cursorY][cursorX].character = ' ';
                }
                break;
            default:
                if (ch >= 32) {
                    screenBuffer[cursorY][cursorX].set(ch, defaultForegroundColor, defaultBackgroundColor);
                    cursorX++;
                }
                break;
        }

        handleCursorWrapAndScroll();
    }

    public void handleCursorWrapAndScroll() {
        if (cursorX >= columns) {
            cursorX = 0;
            cursorY++;
        }

        if (cursorY >= rows) {
            for (int y = 0; y < rows - 1; y++) {
                screenBuffer[y] = screenBuffer[y + 1];
            }
            screenBuffer[rows - 1] = new TerminalCharacter[columns];
            for (int x = 0; x < columns; x++) {
                screenBuffer[rows - 1][x] = new TerminalCharacter();
            }
            cursorY = rows - 1;
        }
    }

    public void draw(Font font) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                TerminalCharacter tc = screenBuffer[y][x];
                int pixelX = x * charCellWidth;
                int pixelY = y * charCellHeight;

                DrawRectangle(pixelX, pixelY, charCellWidth, charCellHeight, tc.background);

                if (tc.character != ' ') {
                    Vector2 position = new Vector2().x(pixelX).y(pixelY);
                    DrawTextEx(font, String.valueOf(tc.character), position, fontSize, 1, tc.foreground);
                }
            }
        }

        if (((int)(GetTime() * 2.0)) % 2 == 0) {
            int cursorPixelX = cursorX * charCellWidth;
            int cursorPixelY = cursorY * charCellHeight;
            DrawRectangle(cursorPixelX, cursorPixelY, charCellWidth, charCellHeight, new Color().r((byte) 255).g((byte) 255).b((byte) 255).a((byte) 128));
        }
    }

    public int getFontSize() {
        return fontSize;
    }
}
