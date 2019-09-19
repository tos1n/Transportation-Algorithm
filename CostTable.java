import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

public class CostTable {

    private Factory[] factories;
    private Warehouse[] warehouses;

    private HashMap<Factory, HashMap<Warehouse, Cell>> costTable;
    private Integer[][] solutionTable;

    public CostTable(Factory[] factories, Warehouse[] warehouses) {

	this.factories = factories;
	this.warehouses = warehouses;

	costTable = new HashMap<Factory, HashMap<Warehouse, Cell>>();

	for (Factory factory : factories) {

	    HashMap<Warehouse, Cell> warehouseMap = costTable.get(factory);

	    if (warehouseMap == null) {
		warehouseMap = new HashMap<Warehouse, Cell>();
		costTable.put(factory, warehouseMap);
	    }

	    for (Warehouse warehouse : warehouses) {
		Cell cell = new Cell();
		warehouseMap.put(warehouse, cell);
	    }

	}

	solutionTable = new Integer[factories.length][warehouses.length];

	for (int i = 0; i < factories.length; i++) {
	    for (int j = 0; j < warehouses.length; j++) {
		solutionTable[i][j] = 0;
	    }
	}

    }

    public void optimizeSolution() {

	boolean continueOptimizing = true;

	while (continueOptimizing) {

	    ArrayList<Solution> solutions = new ArrayList<Solution>();

	    for (Factory factory : factories) {
		for (Warehouse warehouse : warehouses) {

		    if (getCell(factory, warehouse).getCurrentSupply() == 0) {

			Solution solution = new Solution();
			SolutionStep solutionStep = new SolutionStep();

			solutionStep.setFactory(factory);
			solutionStep.setWarehouse(warehouse);
			solutionStep.setScale(1);

			solution.addSolutionStep(solutionStep);
			solutions.add(solution);

		    }

		}
	    }

	    continueOptimizing = false;

	    for (Solution solution : solutions) {
		computeSolutionSteps(solution);
		if (optimize(solution)) {
		    continueOptimizing = true;
		}
	    }

	}

    }

    private void computeSolutionSteps(Solution solution) {

	ArrayList<SolutionStep> solutionSteps = solution.getSolutionSteps();
	SolutionStep initialStep = solutionSteps.get(0);

	SolutionStep currentStep = initialStep;

	ArrayList<Cell> visitedCells = new ArrayList<Cell>();
	visitedCells.add(getCell(currentStep.getFactory(), currentStep.getWarehouse()));

//	System.out.printf("Starting at (factory,source): (%s, %s)\n", initialStep.getFactory().getName(),
//		initialStep.getWarehouse().getName());

	while (true) {

	    if (currentStep.getScale() > 0) {

		boolean newStepFound = false;

		for (Warehouse warehouse : warehouses) {

		    Cell cell = getCell(currentStep.getFactory(), warehouse);

		    if (visitedCells.contains(cell)) {
			continue;
		    }

		    visitedCells.add(cell);

		    if (cell.getCurrentSupply() <= 0) {
			continue;
		    }

		    SolutionStep newStep = new SolutionStep();
		    newStep.setFactory(currentStep.getFactory());
		    newStep.setWarehouse(warehouse);
		    newStep.setScale(-1 * currentStep.getScale());

		    solution.addSolutionStep(newStep);

//		    System.out.printf("Moved from (factory,source): (%s, %s) to (factory,source): (%s, %s)\n",
//			    currentStep.getFactory().getName(), currentStep.getWarehouse().getName(),
//			    newStep.getFactory().getName(), newStep.getWarehouse().getName());

		    currentStep = newStep;

		    newStepFound = true;

		    if (warehouse.equals(initialStep.getWarehouse())) {
			return;
		    }

		    break;

		}

		if (!newStepFound) {

		    solutionSteps.remove(currentStep);

		    if (solutionSteps.size() <= 0) {
			return; // No valid solution.
		    }

		    currentStep = solutionSteps.get(solution.getSolutionSteps().size() - 1);

		}

	    } else if (currentStep.getScale() < 0) {

		boolean newStepFound = false;

		for (Factory factory : factories) {

		    Cell cell = getCell(factory, currentStep.getWarehouse());

		    if (visitedCells.contains(cell)) {
			continue;
		    }

		    visitedCells.add(cell);

		    if (cell.getCurrentSupply() <= 0) {
			continue;
		    }

		    SolutionStep newStep = new SolutionStep();
		    newStep.setFactory(factory);
		    newStep.setWarehouse(currentStep.getWarehouse());
		    newStep.setScale(-1 * currentStep.getScale());

		    solution.addSolutionStep(newStep);

//		    System.out.printf("Moved from (factory,source): (%s, %s) to (factory,source): (%s, %s)\n",
//			    currentStep.getFactory().getName(), currentStep.getWarehouse().getName(),
//			    newStep.getFactory().getName(), newStep.getWarehouse().getName());

		    currentStep = newStep;

		    newStepFound = true;

		    if (factory.equals(initialStep.getFactory())) {
			return;
		    }

		    break;

		}

		if (!newStepFound) {

		    solutionSteps.remove(currentStep);

		    if (solutionSteps.size() <= 0) {
			return; // No valid solution.
		    }

		    currentStep = solutionSteps.get(solution.getSolutionSteps().size() - 1);

		}

	    } else {
		throw new RuntimeException("SolutionStep's scale should not be 0...");
	    }

	}

    }

    /**
     * 
     * @param solution
     * @return {@code true} if the current solution improved the table (i.e. there
     *         might still be more room for improvement) and {@code false}
     *         otherwise.
     */
    private boolean optimize(Solution solution) {

	int cost = calculateSolutionCost(solution);

	if (cost < 0) {

	    int leaving = 0;

	    for (SolutionStep solutionStep : solution.getSolutionSteps()) {

		if (solutionStep.getScale() > 0) {
		    continue;
		}

		Factory factory = solutionStep.getFactory();
		Warehouse warehouse = solutionStep.getWarehouse();

		Cell cell = getCell(factory, warehouse);

		if (leaving == 0 || cell.getCurrentSupply() < leaving) {
		    leaving = cell.getCurrentSupply();
		}

	    }

	    for (SolutionStep solutionStep : solution.getSolutionSteps()) {

		Factory factory = solutionStep.getFactory();
		Warehouse warehouse = solutionStep.getWarehouse();

		Cell cell = getCell(factory, warehouse);

		cell.setCurrentSupply(cell.getCurrentSupply() + (solutionStep.getScale() * leaving));

	    }

//	    System.out.println("Table after this round of optimization:");
//	    System.out.println(this.toString());

	    return true;

	}

//	System.out.println("Optimal solution found!");

	return false;

    }

    private int calculateSolutionCost(Solution solution) {

	int cost = 0;

	for (SolutionStep solutionStep : solution.getSolutionSteps()) {

	    Cell cell = getCell(solutionStep.getFactory(), solutionStep.getWarehouse());
	    cost += solutionStep.getScale() * cell.getPrice();

	}

	return cost;

    }

    public void minimumCellCost() {

	while (!isDemandMet()) {
	    nextMinimumCell();
	}

    }

    private void nextMinimumCell() {

	Cell minCostCell = null;
	Factory minFactory = null;
	Warehouse minWarehouse = null;

	for (Factory factory : factories) {

	    int factorySupply = getRemainingSupply(factory);

	    if (factorySupply <= 0) {
		continue;
	    }

	    for (Warehouse warehouse : warehouses) {

		int warehouseDemand = getRemainingDemand(warehouse);

		if (warehouseDemand <= 0) {
		    continue;
		}

		Cell cell = getCell(factory, warehouse);

		if (minCostCell == null || cell.getPrice() < minCostCell.getPrice()) {
		    minCostCell = cell;
		    minFactory = factory;
		    minWarehouse = warehouse;
		}

	    }

	}

	int minFactorySupply = getRemainingSupply(minFactory);
	int minWarehouseDemand = getRemainingDemand(minWarehouse);

	minCostCell.setCurrentSupply(Math.min(minFactorySupply, minWarehouseDemand));

    }

    private boolean isDemandMet() {

	for (Warehouse warehouse : warehouses) {

	    if (getRemainingDemand(warehouse) > 0) {
		return false;
	    }

	}

	return true;

    }

    private int getRemainingDemand(Warehouse warehouse) {

	int demand = warehouse.getDemand();

	for (Factory factory : factories) {
	    demand -= costTable.get(factory).get(warehouse).getCurrentSupply();
	}

	return demand;

    }

    private int getRemainingSupply(Factory factory) {

	int supply = factory.getProduction();

	for (Warehouse warehouse : warehouses) {
	    supply -= costTable.get(factory).get(warehouse).getCurrentSupply();
	}

	return supply;

    }

    public void setCellCost(int factoryIndex, int warehouseIndex, int cost) {

	Factory factory = factories[factoryIndex];
	Warehouse warehouse = warehouses[warehouseIndex];

	setCellCost(factory, warehouse, cost);

    }

    public void setCellCost(Factory factory, Warehouse warehouse, int cost) {
	getCell(factory, warehouse).setPrice(cost);
    }

    public Integer getCellCost(int factoryIndex, int warehouseIndex) {

	Factory factory = factories[factoryIndex];
	Warehouse warehouse = warehouses[warehouseIndex];

	return getCellCost(factory, warehouse);

    }

    public Integer getCellCost(Factory factory, Warehouse warehouse) {
	return getCell(factory, warehouse).getPrice();
    }

    private Cell getCell(Factory factory, Warehouse warehouse) {
	return costTable.get(factory).get(warehouse);
    }

    public void addCosts(Integer[] costs) {

	int factoryIndex = 0;
	int warehouseIndex = 0;

	for (Integer cost : costs) {

	    setCellCost(factoryIndex, warehouseIndex, cost);

	    warehouseIndex++;

	    if (warehouseIndex >= warehouses.length) {
		warehouseIndex = 0;
		factoryIndex++;
	    }

	}

    }

    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb);

    @Override
    public String toString() {

	formatter.format("%10s", "");

	for (Warehouse warehouse : warehouses) {

	    formatter.format("%15s ", warehouse.getName());

	}

	formatter.format("%10s\n", "supply");

	for (Factory factory : factories) {

	    formatter.format("%10s", factory.getName());

	    for (Warehouse warehouse : warehouses) {
		formatter.format("%10s(%d)%d ", "", getCell(factory, warehouse).getCurrentSupply(),
			getCellCost(factory, warehouse));
	    }

	    formatter.format("%10s\n", factory.getProduction());

	}

	String s = sb.toString();
	sb.setLength(0);

	return s;

    }

}
