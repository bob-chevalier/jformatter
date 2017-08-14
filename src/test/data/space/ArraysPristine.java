public class SomeClass {

    private static final int[] one = new int[1];
    private int[] two = {2, 3, 4};
    private int[][] four = new int[5][6];
    private int[][] five = {{7, 8},{9, 10},{11, 12}};
    private int @F [] @G [] six = new int @F [5] @G [6];
    private static final String[] seven = { "one", "two"};
    private static final String[] eight = { {"three", "four"}, { "five", "six"}};
    @Readonly
int[] nine = new int[1];
    @Writable
private static final int[] ten = new int[1];
    int eleven = someArray[1];
    int twelve = someMatrix[0][2 ];

}
