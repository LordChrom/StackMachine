package Emulator;

@FunctionalInterface
public interface Operation {
    int exec(int a, int b, Processor p);
}
