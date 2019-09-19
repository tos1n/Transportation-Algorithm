import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class DataFileParser {

    public CostTable parseFile(String fileName) throws FileNotFoundException, ParseException {

	BufferedReader reader = new BufferedReader(new FileReader(fileName));

	Warehouse[] warehouses = null;
	ArrayList<Factory> factories = new ArrayList<Factory>();

	ArrayList<Integer> costs = new ArrayList<Integer>();

	try {

	    ParseMode parseMode = ParseMode.WARE_HOUSES;

	    String line = null;

	    while ((line = reader.readLine()) != null) {

		if (line.isEmpty()) {
		    continue;
		}

		switch (parseMode) {
		case WARE_HOUSES:

		    String[] warehouseData = line.split("\\s+");

		    if (warehouseData.length < 2 || !warehouseData[0].toLowerCase().equals("costs")
			    || !warehouseData[warehouseData.length - 1].toLowerCase().equals("supply")) {
			throw new ParseException("File needs to start with a line to define all warehouses.", 0);
		    }

		    warehouses = createWarehouses(Arrays.copyOfRange(warehouseData, 1, warehouseData.length - 1));

		    parseMode = ParseMode.COSTS;

		    break;
		case COSTS:

		    String[] supplyData = line.split("\\s+");

		    if (supplyData[0].toLowerCase().equals("demand")) {
			parseMode = ParseMode.DEMANDS;
		    } else {

			Factory factory = new Factory();

			factory.setName(supplyData[0]);
			factory.setProduction(Integer.parseInt(supplyData[supplyData.length - 1]));

			factories.add(factory);

			for (int i = 1; i < supplyData.length - 1; i++) {
			    costs.add(Integer.parseInt(supplyData[i]));
			}

			break;
		    }

		case DEMANDS:

		    String[] warehouseDemandData = line.split("\\s+");

		    if (warehouseDemandData.length < 1 || !warehouseDemandData[0].toLowerCase().equals("demand")) {
			throw new ParseException("Last line needs to specify the demands of the warehouses.", 1);
		    }

		    updateWarehouseDemands(warehouses,
			    Arrays.copyOfRange(warehouseDemandData, 1, warehouseDemandData.length));

		    break;
		}

	    }

	} catch (IOException ex) {
	    ex.printStackTrace();
	}

	CostTable costTable = new CostTable(factories.toArray(new Factory[factories.size()]), warehouses);

	costTable.addCosts(costs.toArray(new Integer[costs.size()]));

	return costTable;

    }

    private Warehouse[] createWarehouses(String[] warehouseNames) {

	Warehouse[] warehouses = new Warehouse[warehouseNames.length];

	for (int i = 0; i < warehouseNames.length; i++) {

	    String warehouseName = warehouseNames[i];

	    Warehouse warehouse = new Warehouse();

	    warehouse.setName(warehouseName);

	    warehouses[i] = warehouse;

	}

	return warehouses;

    }

    private void updateWarehouseDemands(Warehouse[] warehouses, String[] warehouseDemandsData) {

	for (int i = 0; i < warehouseDemandsData.length; i++) {
	    warehouses[i].setDemand(Integer.parseInt(warehouseDemandsData[i]));
	}

    }

    private enum ParseMode {
	WARE_HOUSES, COSTS, DEMANDS
    }

}
