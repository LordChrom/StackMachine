import Assembler.Parser;
import Emulator.Instruction;
import Emulator.Processor;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static boolean debugMode = false;

    public static void main(String... args){
        if(args.length>0)
            debugMode =args[0].equals("debug");

        Instruction[] program;
        if(debugMode)
            program = Parser.parseFile("C:\\Users\\Slime\\Documents\\Projects\\Stackinator\\program.txt");
        else
            program = Parser.parseFile("program.txt");

        Processor p = new Processor();
        p.exec(program,debugMode);
        System.out.println("\n\nFinal Stack State\n------\n"+p.showStack());
        filePrint("output.txt",p.outputLog.toString());
//        p.printStack();
    }


    private static void filePrint(String name, String... fileLines){
//        if(debug)
//            name = "C:\\Users\\Slime\\Documents\\Projects\\Stackinator\\"+name;
        try (FileWriter writer = new FileWriter(name)) {
            for (int i = 0; i < fileLines.length; i++) {
                writer.write(fileLines[i]);
                if(i< fileLines.length-1)
                    writer.write("\r\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
