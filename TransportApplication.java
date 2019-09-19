import java.io.FileNotFoundException;
import java.text.ParseException;

public class TransportApplication {

    public static void main(String[] args) {

	String fileName = null;

	try {

	    fileName = args[0];

	    DataFileParser parser = new DataFileParser();

	    CostTable costTable = parser.parseFile(fileName);

	    TransportApplication fot = new TransportApplication();

	    fot.findOptimalTransport(costTable);

	} catch (ArrayIndexOutOfBoundsException ex) {
	    System.err.println("You must profile a data file name to read the input from.");
	    System.exit(1);
	} catch (FileNotFoundException ex) {
	    System.err.printf("The file %s does not exist.\n", fileName);
	    System.exit(2);
	} catch (ParseException ex) {
	    ex.printStackTrace();
	    System.exit(3);
	}

    }

    private void findOptimalTransport(CostTable costTable) {

	System.out.println("Here's the current cost table:");
	System.out.println(costTable.toString());

	costTable.minimumCellCost();

	System.out.println("Initial solution:");
	System.out.println(costTable.toString());

	// iterate solution

	costTable.optimizeSolution();

	System.out.println("Final solution:");
	System.out.println(costTable.toString());

    }

}
