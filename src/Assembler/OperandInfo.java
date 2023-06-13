package Assembler;

public class OperandInfo {
    public boolean numeric;
    public boolean remove;
    public String name;
    public int val;

    public OperandInfo(boolean numeric, boolean remove, String name, int val) {
        this.numeric = numeric;
        this.remove = remove;
        this.name = name;
        this.val = val;
    }
}